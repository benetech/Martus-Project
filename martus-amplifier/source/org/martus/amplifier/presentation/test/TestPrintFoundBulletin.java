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

package org.martus.amplifier.presentation.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.velocity.context.Context;
import org.martus.amplifier.attachment.FileSystemDataManager;
import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.datasynch.BulletinExtractor;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.presentation.DoSearch;
import org.martus.amplifier.presentation.PrintFoundBulletin;
import org.martus.amplifier.search.BulletinField;
import org.martus.amplifier.search.BulletinIndexException;
import org.martus.amplifier.search.BulletinInfo;
import org.martus.amplifier.test.AbstractAmplifierTestCase;
import org.martus.amplifier.velocity.AmplifierServletRequest;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.bulletin.BulletinHtmlGenerator;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.util.DirectoryUtils;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeek;

public class TestPrintFoundBulletin extends AbstractAmplifierTestCase
{
	public TestPrintFoundBulletin(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(security == null)
		{
			security = new MockMartusSecurity();
			security.createKeyPair();
			MartusAmplifier.setStaticSecurity(security);
			MartusAmplifier.dataManager = new FileSystemDataManager(getTestBasePath(), security);
			MartusAmplifier.localization = new MiniLocalization();
		}
		if(b1 == null)
		{
			b1 = createTmpBulletin(1);
			b2 = createTmpBulletin(2);
			b3 = createTmpBulletin(3);
		}		
		addToDatabase(b1);
		addToDatabase(b2);
		addToDatabase(b3);
	}
	
	public void tearDown() throws Exception
	{
		super.tearDown();
		MartusAmplifier.dataManager.clearAllAttachments();
		DirectoryUtils.deleteEntireDirectoryTree(new File(basePath));
	}

	private Bulletin createTmpBulletin(int bulletinNumber) throws Exception
	{
		Bulletin b = new Bulletin(security);
		b.set(BulletinField.TAGAUTHOR, "paul"+bulletinNumber);
		b.set(BulletinField.TAGKEYWORDS, "testing"+bulletinNumber);
		b.set(BulletinField.TAGENTRYDATE, "2003-04-30");
		b.setAllPrivate(false);
		b.setSealed();
		b.getFieldDataPacket().setEncrypted(false);
		return b;
	}

	private void addToDatabase(Bulletin b) throws IOException, CryptoException, ZipException, RecordHiddenException
	{
		File tempFile = createTempFileFromName("$$$AMP_TestFoundBulletin");
		BulletinForTesting.saveToFile(((FileSystemDataManager)(MartusAmplifier.dataManager)).getDatabase(), b, tempFile, security);
		FieldDataPacket publicData = b.getFieldDataPacket();
		ZipFile bulletinZipFile = new ZipFile(tempFile);
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
		ZipEntryInputStreamWithSeek zipEntryPointForFieldDataPacket = BulletinExtractor.getInputStreamForZipEntry(bulletinZipFile, bhp.getAccountId(), bhp.getFieldDataPacketId());
		MartusAmplifier.dataManager.putDataPacket(publicData.getUniversalId(),zipEntryPointForFieldDataPacket);
		zipEntryPointForFieldDataPacket.close();
		bulletinZipFile.close();
	}
	
	public void testBasics() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = createSampleSearchResults(request, response);

		PrintFoundBulletin servlet = new PrintFoundBulletin();
		String templateName = servlet.selectTemplate(request, response, context);
		assertEquals("PrintFoundBulletin.vm", templateName);
	}
	
	public void testCurrentBulletin() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		request.parameters.put(SearchResultConstants.RESULT_SORTBY_KEY, "title" );
		MockAmplifierResponse response = null;
		Context context = createSampleSearchResults(request, response);
	
	
		PrintFoundBulletin servlet = new PrintFoundBulletin();
		String templateName = servlet.selectTemplate(request, response, context);
		assertEquals("PrintFoundBulletin.vm", templateName);
		BulletinInfo bulletinInfo1 = (BulletinInfo)context.get("bulletin");
		assertEquals("Bulletin 1's ID didn't match", b1.getUniversalId(), bulletinInfo1.getBulletinId());
		assertEquals("Bulletin 1's title didn't match", bulletin1Title, bulletinInfo1.get("title"));
		assertEquals("Bulletin 1's fdp Uid didn't match", b1.getFieldDataPacket().getUniversalId(), bulletinInfo1.getFieldDataPacketUId());
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(MartusAmplifier.localization);
		assertEquals("Bulletin 1's HTML didn't match",generator.getSectionHtmlString(b1.getFieldDataPacket()), context.get("htmlRepresntation"));
		
	}

	public void testSearchedFor() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = createSampleSearchResults(request, response);
	
	
		PrintFoundBulletin servlet = new PrintFoundBulletin();
		servlet.selectTemplate(request, response, context);
		assertEquals("Didn't get searchedFor correct", "title", context.get("searchedFor"));
	}
	

	public void testCurrentTotalBulletins() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = createSampleSearchResults(request, response);
	
	
		PrintFoundBulletin servlet = new PrintFoundBulletin();
		servlet.selectTemplate(request, response, context);
		assertEquals("Didn't get correct current bulletin", new Integer(1), context.get("currentBulletin"));
		assertEquals("Didn't get correct total bulletin count", new Integer(3), context.get("totalBulletins"));
	}

	private Context createSampleSearchResults(MockAmplifierRequest request, MockAmplifierResponse response) throws Exception
	{
		Context context = new MockContext();
		SearchResultsForTesting sr = new SearchResultsForTesting();
		request.putParameter("query", "title");
		request.parameters.put("index","1");
		request.parameters.put("searchedFor","title");
		sr.selectTemplate(request, response, context);
		clearContextSetBySearchResults(context);
		return context;
	}

	private void clearContextSetBySearchResults(Context context)
	{
		context.put("searchedFor", null);
		context.put("previousBulletin", null);
		context.put("nextBulletin", null);
		context.put("currentBulletin", null);
		context.put("totalBulletins", null);
	}

	static MockMartusSecurity security;
	static Bulletin b1 = null;
	static Bulletin b2 = null;
	static Bulletin b3 = null;

	final String bulletin1Title = "title 1";
	final String bulletin2Title = "title 2";
	final String bulletin3Title = "title 3";


	class SearchResultsForTesting extends DoSearch
	{
		public List getSearchResults(AmplifierServletRequest request)
			throws Exception, BulletinIndexException
		{
			if(request.getParameter("query")==null)
				throw new Exception("malformed query");
			
			Vector infos = new Vector();
			BulletinInfo bulletinInfo1 = new BulletinInfo(b1.getUniversalId());
			bulletinInfo1.set("title", bulletin1Title);
			bulletinInfo1.setFieldDataPacketUId(b1.getFieldDataPacket().getUniversalId().getLocalId());
			infos.add(bulletinInfo1);
			
			BulletinInfo bulletinInfo2 = new BulletinInfo(b2.getUniversalId());
			bulletinInfo2.set("title", bulletin2Title);
			File info2ContactInfo = createTempFile();
			info2ContactInfo.createNewFile();
			bulletinInfo2.setContactInfoFile(info2ContactInfo);
			bulletinInfo2.setFieldDataPacketUId(b2.getFieldDataPacket().getUniversalId().getLocalId());
			infos.add(bulletinInfo2);
			
			BulletinInfo bulletinInfo3 = new BulletinInfo(b3.getUniversalId());
			bulletinInfo3.set("title", bulletin3Title);
			bulletinInfo3.setFieldDataPacketUId(b3.getFieldDataPacket().getUniversalId().getLocalId());
			infos.add(bulletinInfo3);
			return infos;
		}

	}
}

