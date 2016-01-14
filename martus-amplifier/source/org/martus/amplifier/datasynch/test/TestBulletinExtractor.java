/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.amplifier.datasynch.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import junit.framework.Assert;

import org.martus.amplifier.attachment.AttachmentStorageException;
import org.martus.amplifier.attachment.DataManager;
import org.martus.amplifier.attachment.FileSystemDataManager;
import org.martus.amplifier.common.AmplifierLocalization;
import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.datasynch.BulletinExtractor;
import org.martus.amplifier.lucene.LuceneBulletinIndexer;
import org.martus.amplifier.lucene.LuceneBulletinSearcher;
import org.martus.amplifier.main.EventDatesIndexedList;
import org.martus.amplifier.main.LanguagesIndexedList;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.search.AttachmentInfo;
import org.martus.amplifier.search.BulletinField;
import org.martus.amplifier.search.BulletinIndexException;
import org.martus.amplifier.search.BulletinIndexer;
import org.martus.amplifier.search.BulletinInfo;
import org.martus.amplifier.search.BulletinSearcher;
import org.martus.amplifier.search.Results;
import org.martus.amplifier.search.SearchConstants;
import org.martus.amplifier.test.AbstractAmplifierTestCase;
import org.martus.common.LoggerToNull;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.MockBulletinStore;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.DirectoryUtils;
import org.martus.util.MultiCalendar;
import org.martus.util.StreamCopier;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;


public class TestBulletinExtractor extends AbstractAmplifierTestCase
	implements SearchConstants
{
	public TestBulletinExtractor(String name) 
	{
		super(name);
	}

	protected void setUp() throws Exception 
	{
		super.setUp();
		security = new MockMartusSecurity();
		security.createKeyPair();
		attachmentManager = 
			new FileSystemDataManager(getTestBasePath(), security);
		MartusAmplifier.dataManager = attachmentManager;
		MartusAmplifier.localization = new AmplifierLocalization();
		store = new MockBulletinStore(this);
		LanguagesIndexedList.languagesIndexedSingleton = new LanguagesIndexedList(new File(getTestBasePath(),"langIndex"));
		EventDatesIndexedList.initialize(createTempFile());
	}

	protected void tearDown() throws Exception 
	{
		try 
		{
			attachmentManager.clearAllAttachments();
			DirectoryUtils.deleteEntireDirectoryTree(new File(basePath));
		} 
		finally 
		{
			super.tearDown();
		}
	}

	public void testIndexingLanguages() throws Exception
	{
		LanguagesIndexedList.languagesIndexedSingleton = new LanguagesIndexedList(createTempFile());
		LanguagesIndexedList.languagesIndexedSingleton.loadFromFile();
		
		Vector languages = LanguagesIndexedList.languagesIndexedSingleton.getIndexedValues();
		assertEquals("Should not have any yet file exists but is empty", 0, languages.size());
		
		String language = "eo";	
		
		BulletinIndexer indexer = getBulletinIndexer(); 
		try 
		{
			BulletinExtractor extractor = new BulletinExtractor(attachmentManager, indexer, security);
			extractor.indexLanguage(language);
			extractor.indexLanguage("");
		}
		finally
		{
			indexer.close();
		}
		languages = LanguagesIndexedList.languagesIndexedSingleton.getIndexedValues();
		assertEquals("Should only have esperanto", 1, languages.size());
		assertTrue("Should contain esperanto", languages.contains(language));
		
	}
	
	public void testSimpleExtraction() 
		throws Exception
	{
		clearTestData();
		BulletinIndexer indexer = null;
		Exception closeException = null;
		Bulletin b = createSampleBulletin(new File[0]);
		MultiCalendar today = new MultiCalendar();
		b.set(Bulletin.TAGEVENTDATE, MartusFlexidate.toBulletinFlexidateFormat(today, today));
		File f = createBulletinZipFile(b);
		
		try {
			indexer = getBulletinIndexer();
			BulletinExtractor extractor = 
				new BulletinExtractor(
					attachmentManager, indexer, security);
			extractor.extractAndStoreBulletin(f);
		} finally {
			if (indexer != null) {
				try {
					indexer.close();
				} catch (BulletinIndexException e) {
					closeException = e;
				}
			}
		}
		
		if (closeException != null) {
			throw closeException;
		}
		
		BulletinSearcher searcher = getBulletinSearcher();
		try {
			HashMap fields = new HashMap();
			fields.put(SearchResultConstants.RESULT_BASIC_QUERY_KEY, b.get(BulletinField.TAGAUTHOR));			
			Results results = searcher.search(fields);
			Assert.assertEquals(1, results.getCount());
			BulletinInfo info = 
				searcher.lookup(b.getUniversalId());
			Assert.assertNotNull(info);
			compareBulletins(b, info);
		} finally {
			searcher.close();
		}
	}

	public void testMoreComplicatedExtraction() 
		throws Exception
	{
		clearTestData();
		BulletinIndexer indexer = null;
		Exception closeException = null;
		Bulletin b = createSampleBulletin2(new File[0]);
		File f = createBulletinZipFile(b);
		
		try 
		{
			indexer = getBulletinIndexer();
			BulletinExtractor extractor = 
				new BulletinExtractor(
					attachmentManager, indexer, security);
			extractor.extractAndStoreBulletin(f);
			
			compareFieldDataPackets(b.getFieldDataPacket(), attachmentManager.getFieldDataPacket(b.getFieldDataPacket().getUniversalId()));
		} 
		finally 
		{
			if (indexer != null) 
			{
				try 
				{
					indexer.close();
				} 
				catch (BulletinIndexException e) 
				{
					closeException = e;
				}
			}
		}
		
		if (closeException != null) {
			throw closeException;
		}
		
		BulletinSearcher searcher = getBulletinSearcher();
		assertNotNull("bulletin wasn't saved?", searcher.lookup(b.getUniversalId()));
		try 
		{
			HashMap fields = new HashMap();
			fields.put(SearchResultConstants.RESULT_BASIC_QUERY_KEY, b.get(BulletinField.TAGAUTHOR));			
			Results results = searcher.search(fields);
			Assert.assertEquals(1, results.getCount());
			BulletinInfo info = 
				searcher.lookup(b.getUniversalId());
			Assert.assertNotNull(info);
			compareBulletins(b, info);
		} 
		finally 
		{
			searcher.close();
		}
	}
	
	public void testExtractionWithAttachments() 
		throws Exception
	{
		clearTestData();
		BulletinIndexer indexer = null;
		Exception closeException = null;
		File[] attachments = new File[2];
		attachments[0] = createAttachment("Attachment 1");
		attachments[1] = createAttachment("Attachment 2");
		Bulletin b = createSampleBulletin(attachments);
		MultiCalendar today = new MultiCalendar();
		b.set(Bulletin.TAGEVENTDATE, MartusFlexidate.toBulletinFlexidateFormat(today, today));
		store.saveBulletinForTesting(b);
		File f = createBulletinZipFile(b);
		
		try {
			indexer = getBulletinIndexer();
			BulletinExtractor extractor = 
				new BulletinExtractor(
					attachmentManager, indexer, security);
			extractor.extractAndStoreBulletin(f);
		} finally {
			if (indexer != null) {
				try {
					indexer.close();
				} catch (BulletinIndexException e) {
					closeException = e;
				}
			}
		}
		
		if (closeException != null) {
			throw closeException;
		}
		
		BulletinSearcher searcher = getBulletinSearcher();
		try {
			HashMap fields = new HashMap();
			fields.put(SearchResultConstants.RESULT_BASIC_QUERY_KEY, b.get(BulletinField.TAGAUTHOR));			
			Results results = searcher.search(fields);
			Assert.assertEquals(1, results.getCount());
			BulletinInfo info = 
				searcher.lookup(b.getUniversalId());
			Assert.assertNotNull(info);
			compareBulletins(b, info);
			compareAttachments(b.getAccount(), attachments, info.getAttachments());
		} finally {
			searcher.close();
		}
	}

	private void compareFieldDataPackets(FieldDataPacket original, FieldDataPacket retrievedData)
	{
		FieldSpec[] originalSpecs = original.getFieldSpecs().asArray();
		assertEquals(original.getUniversalId(), retrievedData.getUniversalId());
		for(int i = 0; i < originalSpecs.length; ++i)
		{
			String tag = originalSpecs[i].getTag();
			assertEquals(original.get(tag), retrievedData.get(tag));
		}
		assertEquals(original.getAuthorizedToReadKeys().toStringWithLabel(), retrievedData.getAuthorizedToReadKeys().toStringWithLabel());
	}
	
	private void compareBulletins(
		Bulletin bulletin, BulletinInfo retrievedData) 
		throws IOException
	{
		MiniLocalization localization = MartusAmplifier.localization;
		
		assertEquals(bulletin.getUniversalId(), retrievedData.getBulletinId());
		assertEquals(bulletin.getFieldDataPacket().getUniversalId(), retrievedData.getFieldDataPacketUId());
		Collection fields = BulletinField.getSearchableFields();
		for (Iterator iter = fields.iterator(); iter.hasNext();) {
			BulletinField field = (BulletinField) iter.next();
			if(field.isDateRangeField())
			{
				String dateOfBulletin = bulletin.get(field.getXmlId());
				MartusFlexidate mfd = localization.createFlexidateFromStoredData(dateOfBulletin);

				String startDate = localization.convertStoredDateToDisplay(mfd.getBeginDate().toIsoDateString());
				String startDateRetrieved = retrievedData.get(field.getIndexId()+"-start");
				assertEquals("Wrong start date?", startDate, startDateRetrieved);

				String endDateRetrieved = retrievedData.get(field.getIndexId()+"-end");
				if(mfd.hasDateRange())
				{
					String endDate = localization.convertStoredDateToDisplay(mfd.getEndDate().toIsoDateString());
					assertEquals("Wrong end date?", endDate, endDateRetrieved);
				}
				else
				{
					assertEquals("Non-blank end date?", "", endDateRetrieved);
				}
				continue;
			}
			
			Object retrievedValue = retrievedData.get(field.getIndexId());
			if (retrievedValue == null ) 
			{	
				retrievedValue = "";
			}	
						
			assertEquals(bulletin.get(field.getXmlId()), retrievedValue);
		}
	}
	
	private void compareAttachments(
		String accountId, File[] attachments, List retrieved) 
		throws IOException, AttachmentStorageException
	{
		Assert.assertEquals(attachments.length, retrieved.size());
		for (int i = 0; i < attachments.length; i++) {
			String s1 = fileToString(attachments[i]);
			AttachmentInfo info = (AttachmentInfo) retrieved.get(i);
			InputStream in = 
				attachmentManager.getAttachment(
					UniversalId.createFromAccountAndLocalId(accountId, info.getLocalId()));
			try {
				Assert.assertEquals(s1, inputStreamToString(in));
			} finally {
				in.close();
			}
		}
	}
	
	private Bulletin createSampleBulletin(File[] attachments) 
		throws Exception
	{
		Bulletin b = new Bulletin(security);
		b.set(BulletinField.TAGAUTHOR, "paul");
		b.set(BulletinField.TAGKEYWORDS, "testing");
		b.set(BulletinField.TAGENTRYDATE, "2003-04-30");
		for (int i = 0; i < attachments.length; i++) {
			b.addPublicAttachment(new AttachmentProxy(attachments[i]));
		}
		b.setAllPrivate(false);
		b.getFieldDataPacket().setEncrypted(false);
		b.setSealed();
		return b;
	}
	
	private Bulletin createSampleBulletin2(File[] attachments) 
		throws Exception
	{
		Bulletin b = new Bulletin(security);
		b.set(BulletinField.TAGAUTHOR, "flex");
		b.set(BulletinField.TAGKEYWORDS, "testing flexidate");
		b.set(BulletinField.TAGEVENTDATE, "2003-08-20,20030820+3");
		for (int i = 0; i < attachments.length; i++) {
			b.addPublicAttachment(new AttachmentProxy(attachments[i]));
		}
		b.setAllPrivate(false);
		b.getFieldDataPacket().setEncrypted(false);
		b.setSealed();
		return b;
	}

	private File createAttachment(String data) 
		throws IOException
	{
		return stringToFile(data);
	}
	
	private File stringToFile(String s) throws IOException
	{
		File temp = createTempFileFromName("$$$MartusAmpTempAttachment");
		InputStream in = new StringInputStreamWithSeek(s);
		OutputStream out = new FileOutputStream(temp);
		try {
			new StreamCopier().copyStream(in, out);
		} finally {
			out.close();
		}
		return temp;
	}
	
	private String fileToString(File f) throws IOException
	{
		InputStream in = new FileInputStream(f);
		try {
			return inputStreamToString(in);
		} finally {
			in.close();
		}
	}
	
	private File createBulletinZipFile(Bulletin b) 
		throws IOException, CryptoException			
	{
		File tempFile = createTempFileFromName("$$$MartusAmpBulletinExtractorTest");
		BulletinForTesting.saveToFile(getDatabase(), b, tempFile, security);
		return tempFile;
	}

	
	private void clearTestData() 
		throws AttachmentStorageException, BulletinIndexException
	{
		attachmentManager.clearAllAttachments();
		
		BulletinIndexer indexer = getBulletinIndexer();
		try {
			indexer.clearIndex();
		} finally {
			indexer.close();
		}	
	}
	
	private BulletinIndexer getBulletinIndexer() 
		throws BulletinIndexException
	{
		return new LuceneBulletinIndexer(getTestBasePath());
	}
	
	private BulletinSearcher getBulletinSearcher() throws Exception
	{
		return new LuceneBulletinSearcher(getTestBasePath(), new LoggerToNull());
	}
	
	private ReadableDatabase getDatabase()
	{
		return store.getDatabase();
	}

	private DataManager attachmentManager;
	private MartusCrypto security;
	private BulletinStore store;
}