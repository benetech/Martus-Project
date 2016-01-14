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

package org.martus.amplifier.lucene.test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.martus.amplifier.attachment.FileSystemDataManager;
import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.lucene.LuceneBulletinIndexer;
import org.martus.amplifier.lucene.LuceneBulletinSearcher;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.search.BulletinField;
import org.martus.amplifier.search.BulletinIndexException;
import org.martus.amplifier.search.BulletinIndexer;
import org.martus.amplifier.search.BulletinSearcher;
import org.martus.amplifier.search.SearchConstants;
import org.martus.amplifier.test.AbstractAmplifierTestCase;
import org.martus.common.FieldSpecCollection;
import org.martus.common.LoggerToNull;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UnicodeConstants;
import org.martus.util.DirectoryUtils;

public abstract class CommonSearchTest 
	extends AbstractAmplifierTestCase implements SearchConstants, SearchResultConstants
{
	protected CommonSearchTest(String name) 
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		MartusAmplifier.setStaticSecurity(new MockMartusSecurity());
		MartusAmplifier.getSecurity().createKeyPair();
		MartusAmplifier.dataManager = new FileSystemDataManager(getTestBasePath());
	}
	
	public void tearDown() throws Exception
	{
		super.tearDown();
		MartusAmplifier.dataManager.clearAllAttachments();
		DirectoryUtils.deleteEntireDirectoryTree(new File(basePath));
	}
	
	protected FieldDataPacket generateSampleData(UniversalId bulletinId) throws Exception
	{
		String summary = 
			"Today Paul ate an egg salad sandwich and a root beer for lunch.";

		HashMap fieldPairs = new HashMap();
		fieldPairs.put(SEARCH_AUTHOR_INDEX_FIELD, "Paul"); 
		fieldPairs.put(SEARCH_KEYWORDS_INDEX_FIELD, "ate egg salad root beer"); 
		fieldPairs.put(SEARCH_TITLE_INDEX_FIELD, "ZZZ for 2nd Lunch?");
		fieldPairs.put(SEARCH_ENTRY_DATE_INDEX_FIELD, "2003-05-11"); 
		fieldPairs.put(SEARCH_EVENT_DATE_INDEX_FIELD, "2003-04-10");
		fieldPairs.put(SEARCH_DETAILS_INDEX_FIELD, "menu");
		fieldPairs.put(SEARCH_LANGUAGE_INDEX_FIELD, "en");
		fieldPairs.put(SEARCH_ORGANIZATION_INDEX_FIELD, "test sample");
		fieldPairs.put(SEARCH_SUMMARY_INDEX_FIELD, summary);
		fieldPairs.put(SEARCH_LOCATION_INDEX_FIELD, "San Francisco, CA");
		fieldPairs.put(SAMPLE_CUSTOM_TAG, "I am a custom field");

		String[] attachmentLabels = new String[] {
				"att1Id", "Eggs.gif", 
				"att2Id", "Recipe.txt"
			};
		
		FieldDataPacket fdp = generateFieldDataPacket(bulletinId, fieldPairs, attachmentLabels);
		return fdp;
	}

	protected FieldDataPacket generateOtherLanguageData(UniversalId bulletinId) throws Exception
	{
		String summary = 
			"Today Paul ate an egg salad sandwich and a root beer for lunch.";

		HashMap fieldPairs = new HashMap();
		fieldPairs.put(SEARCH_AUTHOR_INDEX_FIELD, "Paul"); 
		fieldPairs.put(SEARCH_KEYWORDS_INDEX_FIELD, "ate egg salad root beer"); 
		fieldPairs.put(SEARCH_TITLE_INDEX_FIELD, "other language?");
		fieldPairs.put(SEARCH_ENTRY_DATE_INDEX_FIELD, "2003-05-11"); 
		fieldPairs.put(SEARCH_EVENT_DATE_INDEX_FIELD, "2003-04-10");
		fieldPairs.put(SEARCH_DETAILS_INDEX_FIELD, "menu");
		fieldPairs.put(SEARCH_LANGUAGE_INDEX_FIELD, "?");
		fieldPairs.put(SEARCH_ORGANIZATION_INDEX_FIELD, "test sample");
		fieldPairs.put(SEARCH_SUMMARY_INDEX_FIELD, summary);
		fieldPairs.put(SEARCH_LOCATION_INDEX_FIELD, "San Francisco, CA");

		String[] attachmentLabels = new String[] {
				"att1Id", "Eggs.gif", 
				"att2Id", "Recipe.txt"
			};
		
		FieldDataPacket fdp = generateFieldDataPacket(bulletinId, fieldPairs, attachmentLabels);
		return fdp;
	}
	
	protected FieldDataPacket generateSampleFlexiData(UniversalId bulletinId) throws Exception
	{
		HashMap fieldPairs = new HashMap();
		fieldPairs.put(SEARCH_AUTHOR_INDEX_FIELD, "Chuck"); 
		fieldPairs.put(SEARCH_KEYWORDS_INDEX_FIELD, "2003-08-20"); 
		fieldPairs.put(SEARCH_TITLE_INDEX_FIELD, "What's for Lunch?");
		fieldPairs.put(SEARCH_ENTRY_DATE_INDEX_FIELD, "2003-09-15"); 
		fieldPairs.put(SEARCH_EVENT_DATE_INDEX_FIELD, "2003-08-20,20030820+3");
		fieldPairs.put(SEARCH_DETAILS_INDEX_FIELD, "menu3");
		fieldPairs.put(SEARCH_LANGUAGE_INDEX_FIELD, "es");
		fieldPairs.put(SEARCH_ORGANIZATION_INDEX_FIELD, "test complex");
		fieldPairs.put(SEARCH_SUMMARY_INDEX_FIELD, "Today Chuck ate an egg2 salad2 sandwich and a root beer2 for lunch.");
		fieldPairs.put(SEARCH_LOCATION_INDEX_FIELD, "");

		FieldDataPacket fdp = generateFieldDataPacket(bulletinId, fieldPairs);
		return fdp;
	}

	protected FieldDataPacket generateSampleForeignCharData(UniversalId bulletinId) throws Exception
	{
		HashMap fieldPairs = new HashMap();
		fieldPairs.put(SEARCH_AUTHOR_INDEX_FIELD, "Charles"); 
		fieldPairs.put(SEARCH_KEYWORDS_INDEX_FIELD, "foreign b" + UnicodeConstants.ACCENT_E_LOWER); 
		fieldPairs.put(SEARCH_TITLE_INDEX_FIELD, "Foreign Chars ni" + UnicodeConstants.TILDE_N_LOWER + "os");
		fieldPairs.put(SEARCH_ENTRY_DATE_INDEX_FIELD, "2003-09-15"); 
		fieldPairs.put(SEARCH_EVENT_DATE_INDEX_FIELD, "2003-08-20,20030820+3");
		fieldPairs.put(SEARCH_DETAILS_INDEX_FIELD, "menu3");
		fieldPairs.put(SEARCH_LANGUAGE_INDEX_FIELD, "es");
		fieldPairs.put(SEARCH_ORGANIZATION_INDEX_FIELD, "test");
		fieldPairs.put(SEARCH_SUMMARY_INDEX_FIELD, "ni" + UnicodeConstants.TILDE_N_LOWER + "os");
		fieldPairs.put(SEARCH_LOCATION_INDEX_FIELD, "");

		String[] attachmentLabels = new String[] {
				"att1Id", "no.gif", 
				"att2Id", "none.txt"
			};
		
		FieldDataPacket fdp = generateFieldDataPacket(bulletinId, fieldPairs, attachmentLabels);
		return fdp;
	}
	
	
	protected FieldDataPacket generateEmptyFieldDataPacket(UniversalId bulletinId) throws Exception
	{
		return generateFieldDataPacket(bulletinId, new HashMap());
	}
	
	protected FieldDataPacket generateFieldDataPacket(UniversalId bulletinId, HashMap fieldPairs) throws Exception
	{
		return generateFieldDataPacket(bulletinId, fieldPairs, new String[0]);
	}
	
	
	protected FieldDataPacket generateFieldDataPacket(
		UniversalId bulletinId, HashMap fieldPairs,
		String[] attachmentsAssocList) throws Exception
	{

		UniversalId fieldUid = UniversalId.createFromAccountAndLocalId(
			bulletinId.getAccountId(), "TestField");
		FieldDataPacket fdp = new FieldDataPacket(fieldUid, getSampleFieldSpecs());
		
		addFields(fdp, fieldPairs);
		addAttachments(fdp, attachmentsAssocList);
					
		return fdp;
	}
	
	private FieldSpecCollection getSampleFieldSpecs()
	{
		FieldSpec[] normalFields = BulletinField.getDefaultSearchFieldSpecs();
		int normalFieldCount = normalFields.length + 1;
		
		FieldSpec[] withCustom = new FieldSpec[normalFieldCount];
		System.arraycopy(normalFields, 0, withCustom, 0, normalFields.length);
		withCustom[normalFields.length] = FieldSpec.createCustomField(SAMPLE_CUSTOM_TAG, SAMPLE_CUSTOM_LABEL, new FieldTypeNormal());
		
		return new FieldSpecCollection(withCustom);
	}

	private void addFields(FieldDataPacket fdp, HashMap fieldPairs)
	{
		Set keys = fieldPairs.keySet();
		Iterator iterator = keys.iterator(); 
		while(iterator.hasNext())
		{
			String tag = (String)iterator.next();
			String value = (String)fieldPairs.get(tag);
			fdp.set(tag, value);
		}
	}

	private void addAttachments(FieldDataPacket fdp, String[] attachmentsAssocList)
	{
		Assert.assertEquals(
			"Uneven assoc list: " + Arrays.asList(attachmentsAssocList), 
			0, attachmentsAssocList.length % 2);

		for (int i = 0; i < attachmentsAssocList.length; i += 2) 
		{
			String localId = attachmentsAssocList[i];
			UniversalId uid = UniversalId.createFromAccountAndLocalId(fdp.getAccountId(), localId);
			String label = attachmentsAssocList[i + 1];
			AttachmentProxy proxy = new AttachmentProxy(uid, label, null);
			fdp.addAttachment(proxy);
		}
	}

	protected void deleteIndexDir() throws BulletinIndexException
	{
		File indexDir = 
			LuceneBulletinIndexer.getIndexDir(getTestBasePath());
		File[] indexFiles = indexDir.listFiles();
		for (int i = 0; i < indexFiles.length; i++) {
			File indexFile = indexFiles[i];
			if (!indexFile.isFile()) {
				throw new BulletinIndexException(
					"Unexpected non-file encountered: " + indexFile);
			}
			indexFile.delete();
		}
		indexDir.delete();
	}
	

	protected BulletinIndexer openBulletinIndexer()
		throws BulletinIndexException 
	{
		return new LuceneBulletinIndexer(getTestBasePath());
	}

	protected BulletinSearcher openBulletinSearcher() throws Exception 
	{
		return new LuceneBulletinSearcher(getTestBasePath(), new LoggerToNull());
	}
	
	public static final String SAMPLE_CUSTOM_TAG = "CustomField1";
	public static final String SAMPLE_CUSTOM_LABEL = "Neat label!";
}