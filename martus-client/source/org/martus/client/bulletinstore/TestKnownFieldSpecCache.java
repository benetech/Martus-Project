/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.client.bulletinstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipFile;

import org.martus.client.swingui.fields.UiFieldContext;
import org.martus.client.test.MockMartusApp;
import org.martus.common.FieldSpecCollection;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.MessageFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.TestCaseEnhanced;

public class TestKnownFieldSpecCache extends TestCaseEnhanced
{
	public TestKnownFieldSpecCache(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		security = MockMartusSecurity.createClient();
		app = MockMartusApp.create(security, getName());
		ClientBulletinStore store = app.getStore();
		cache = store.knownFieldSpecCache;
	}
	
	public void tearDown() throws Exception
	{
		app.deleteAllFiles();
		assertEquals("Didn't clear cache?", 0, cache.getAllKnownFieldSpecs().size());
	}

	public void testClearAndInitialize() throws Exception
	{
		Bulletin one = createSampleBulletin(security);
		app.saveBulletin(one, app.getFolderDraftOutbox());
		Set specs = cache.getAllKnownFieldSpecs();
		assertEquals("wrong number of specs?", 2, specs.size());
		assertContains("public spec not found?", publicSpecs.asArray()[0], specs);
		assertContains("private spec not found?", privateSpecs.asArray()[0], specs);
	}

	public void testSaveBulletin() throws Exception
	{
		Bulletin withCustom = createSampleBulletin(security);
		app.saveBulletin(withCustom, app.getFolderDraftOutbox());
		Set specsAfterSave = cache.getAllKnownFieldSpecs();
		int newExpectedCount = publicSpecs.size() + privateSpecs.size();
		assertEquals("didn't add new specs?", newExpectedCount, specsAfterSave.size());
		assertContains("didn't add public?", publicSpecs.asArray()[0], specsAfterSave);
		assertContains("didn't add private?", privateSpecs.asArray()[0], specsAfterSave);
		
		// two fieldspecs with same tag name
	}

	public void testSaveXFormsBulletin() throws Exception
	{
		Bulletin normalBulletin = new Bulletin(security, StandardFieldSpecs.getDefaultTopSectionFieldSpecs(), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		app.saveBulletin(normalBulletin, app.getFolderDraftOutbox());

		Bulletin withXForms = createSampleXFormsBulletin(security);
		Set specsBeforeXFormsSaved = cache.getAllKnownFieldSpecs();
		app.saveBulletin(withXForms, app.getFolderDraftOutbox());
		Set specsAfterXFormsSaved = cache.getAllKnownFieldSpecs();
		int numberOfSpecsBeforeXFormsSaved = specsBeforeXFormsSaved.size();
		assertNotEquals("didn't add new specs?", numberOfSpecsBeforeXFormsSaved, specsAfterXFormsSaved.size());

		UiFieldContext contextToUse = new UiFieldContext();
		FieldSpecCollection allSpecs = new FieldSpecCollection();
		allSpecs.addAllSpecs(specsAfterXFormsSaved);
		contextToUse.setSectionFieldSpecs(allSpecs);

		String xFormsTag = "name";
		for (Iterator iterator = specsAfterXFormsSaved.iterator(); iterator.hasNext();)
		{
			FieldSpec newXFormSpec = (FieldSpec) iterator.next();
			if(newXFormSpec.getTag().equals(xFormsTag))
			{
				assertEquals("Label incorrect?", "some randon name", newXFormSpec.getLabel());
				assertEquals("Tag incorrect?", "name", newXFormSpec.getTag());
				assertTrue("Type incorrect?",  newXFormSpec.getType().isString());
				return;
			}
		}
		fail("Didn't find xForms Field?");
	}

	public void testIgnoreUnauthorizedBulletins() throws Exception
	{
		MockMartusSecurity otherSecurity = MockMartusSecurity.createOtherClient();

		MockMartusApp otherApp = MockMartusApp.create(otherSecurity, getName());
		Bulletin notOurs = createSampleBulletin(otherSecurity);
		notOurs.setAllPrivate(true);
		assertTrue("Not encrypting?", otherApp.getStore().mustEncryptPublicData());
		otherApp.saveBulletin(notOurs, otherApp.getFolderDraftOutbox());
		File zipFile = createTempFile();
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(otherApp.getStore().getDatabase(), notOurs.getDatabaseKey(), zipFile, otherSecurity);
		
		app.getStore().importBulletinZipFile(new ZipFile(zipFile));
		Set specsWithNotOurs = cache.getAllKnownFieldSpecs();
		assertEquals("didn't ignore other author's bulletin?",0, specsWithNotOurs.size());
		otherApp.deleteAllFiles();
	}

	public void testDeleteAndImportBulletin() throws Exception
	{
		int expectedCountAfterSaveOrImport = publicSpecs.size() + privateSpecs.size();
		Bulletin toImport = createSampleBulletin(security);
		app.saveBulletin(toImport, app.getFolderDraftOutbox());
		assertEquals("save didn't add specs?", expectedCountAfterSaveOrImport, cache.getAllKnownFieldSpecs().size());

		File zipFile = createTempFile();
		ClientBulletinStore store = app.getStore();
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(store.getDatabase(), toImport.getDatabaseKey(), zipFile, security);
		store.destroyBulletin(toImport);
		Set specsAfterDelete = cache.getAllKnownFieldSpecs();
		assertEquals("didn't remove specs from deleted bulletin?", 0, specsAfterDelete.size());
		
		ZipFile zip = new ZipFile(zipFile);
		store.importBulletinZipFile(zip);
		Set specsAfterImport = cache.getAllKnownFieldSpecs();
		assertEquals("didn't include imported specs?", expectedCountAfterSaveOrImport, specsAfterImport.size());
	}
	
	public void testSaveAndLoad() throws Exception
	{
		app.loadSampleData();
		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		cache.saveToStream(saved);
		ByteArrayInputStream loadable = new ByteArrayInputStream(saved.toByteArray());
		KnownFieldSpecCache reloaded = new KnownFieldSpecCache(new MockClientDatabase(), security);
		reloaded.loadFromStream(loadable);
		Set specs = reloaded.getAllKnownFieldSpecs();
		assertEquals("Didn't reload properly?", cache.getAllKnownFieldSpecs(), specs);
		assertEquals("Didn't load correct count?", sampleDataSpecTags.length, specs.size());
	}
	
	public void testSaveAndLoadReusableChoices() throws Exception
	{
		FieldSpecCollection topSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		ReusableChoices reusableChoices = new ReusableChoices("choices", "A");
		reusableChoices.add(new ChoiceItem("code1", "Label1"));
		reusableChoices.add(new ChoiceItem("code2", "Label2"));
		topSpecs.addReusableChoiceList(reusableChoices);
		Bulletin b = new Bulletin(security, topSpecs, StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		cache.revisionWasSaved(b);
		
		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		cache.saveToStream(saved);
		ByteArrayInputStream loadable = new ByteArrayInputStream(saved.toByteArray());
		KnownFieldSpecCache reloadedCache = new KnownFieldSpecCache(new MockClientDatabase(), security);
		reloadedCache.loadFromStream(loadable);
		ReusableChoices reloadedChoices = reloadedCache.getAllReusableChoiceLists().getChoices(reusableChoices.getCode());
		assertEquals(reusableChoices, reloadedChoices);
	}
	
	public void testLoadFromBadData() throws Exception
	{
		byte[] badData = {1, 22, 15, 121, 1, 0};
		ByteArrayInputStream badIn = new ByteArrayInputStream(badData);
		KnownFieldSpecCache scratch = new KnownFieldSpecCache(new MockClientDatabase(), security);
		try
		{
			scratch.loadFromStream(badIn);
			fail("Should have thrown");
		}
		catch(IOException ignoreExpected)
		{
		}
	}
	
	public void testSimilarDropdowns() throws Exception
	{
		ChoiceItem[] choices1 = {new ChoiceItem("a", "a a"), new ChoiceItem("b", "b b"),};
		ChoiceItem[] choices2 = {new ChoiceItem("a", "a-a"), new ChoiceItem("b", "b-b"),};
		DropDownFieldSpec spec1 = new DropDownFieldSpec(choices1);
		DropDownFieldSpec spec2 = new DropDownFieldSpec(choices2);
		
		Bulletin b1 = new Bulletin(security, new FieldSpecCollection(new FieldSpec[] {spec1}), new FieldSpecCollection());
		Bulletin b2 = new Bulletin(security, new FieldSpecCollection(new FieldSpec[] {spec2}), new FieldSpecCollection());
		
		assertEquals(0, cache.getAllKnownFieldSpecs().size());
		cache.revisionWasSaved(b1);
		cache.revisionWasSaved(b2);
		assertEquals(2, cache.getAllKnownFieldSpecs().size());
	}
	
	public void testConcatenationOfSimilarReusableChoices() throws Exception
	{
		String label1 = "antler";
		ChoiceItem[] choices1 = {new ChoiceItem("a", label1), new ChoiceItem("b", "b"),};
		ChoiceItem[] choices2 = {new ChoiceItem("a", "ant"), new ChoiceItem("b", "bobby"),};
		ChoiceItem[] choices3 = {new ChoiceItem("a", label1), new ChoiceItem("b", "bob"),};

		String listName = "choices";
		Bulletin b1 = createBulletinWithReusableChoices(choices1, listName);
		cache.revisionWasSaved(b1);
		assertEquals("Original choice item was modified?", label1, choices1[0].getLabel());
		Bulletin b2 = createBulletinWithReusableChoices(choices2, listName);
		cache.revisionWasSaved(b2);
		assertEquals("Original choice item was modified?", label1, choices1[0].getLabel());
		Bulletin b3 = createBulletinWithReusableChoices(choices3, listName);
		cache.revisionWasSaved(b3);
		assertEquals("Original choice item was modified?", label1, choices1[0].getLabel());

		ReusableChoices allChoices = cache.getAllReusableChoiceLists().getChoices("choices");
		String labelA = allChoices.findByCode("a").getLabel();
		assertEquals("antler twice?", labelA.indexOf(label1), labelA.lastIndexOf(label1));
		String labelB = allChoices.findByCode("b").getLabel() + "; ";
		assertContains("bob;", labelB);
	}

	private Bulletin createBulletinWithReusableChoices(ChoiceItem[] choices1, String listName) throws Exception
	{
		CustomDropDownFieldSpec spec = new CustomDropDownFieldSpec();
		spec.setTag("tag");
		spec.setLabel("label");
		spec.addReusableChoicesCode("code");
		
		ReusableChoices reusableChoices1 = new ReusableChoices(listName, "label");
		reusableChoices1.addAll(choices1);
		FieldSpecCollection topSpecs1 = new FieldSpecCollection(new FieldSpec[] {spec});
		topSpecs1.addReusableChoiceList(reusableChoices1);
		return new Bulletin(security, topSpecs1, new FieldSpecCollection());
	}
	
	public void testReallyBigFieldSpec() throws Exception
	{
		StringBuffer reallyLongString = new StringBuffer();
		String ten = "1234567890";
		String hundred = ten+ten+ten+ten+ten+ten+ten+ten+ten+ten;
		String fiveHundred = hundred+hundred+hundred+hundred+hundred;
		String thousand = fiveHundred + fiveHundred;
		for(int i = 0; i < 100; ++i)
			reallyLongString.append(thousand);
		
		MessageFieldSpec spec1 = new MessageFieldSpec();
		spec1.setLabel(reallyLongString.toString());
		
		Bulletin b = new Bulletin(security, new FieldSpecCollection(new FieldSpec[] {spec1}), new FieldSpecCollection());
		cache.revisionWasSaved(b);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		cache.saveToStream(out);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		cache.loadFromStream(in);
	}
	
	public void testCacheReusableChoiceLists() throws Exception
	{
		FieldSpecCollection topSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		ReusableChoices choices = new ReusableChoices("choices", "Choices:");
		choices.add(new ChoiceItem("a", "A"));
		choices.add(new ChoiceItem("b", "B"));
		choices.add(new ChoiceItem("c", "C"));
		topSpecs.addReusableChoiceList(choices);
		Bulletin b = new Bulletin(security, topSpecs, StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		assertEquals("Already have reusable choices?", 0, cache.getAllReusableChoiceLists().size());
		cache.revisionWasSaved(b);
		assertEquals("Didn't memorize reusable choices?", 1, cache.getAllReusableChoiceLists().size());
		cache.revisionWasRemoved(b.getUniversalId());
		assertEquals("Removing bulletin removed choices?", 1, cache.getAllReusableChoiceLists().size());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		cache.saveToStream(out);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		cache.loadFromStream(in);
		assertEquals("Didn't save and reload reusable choices?", 1, cache.getAllReusableChoiceLists().size());
		
		cache.clear();
		assertEquals("Didn't clear reusable choices?", 0, cache.getAllReusableChoiceLists().size());
	}

	private Bulletin createSampleBulletin(MartusCrypto authorSecurity) throws Exception
	{
		Bulletin b = new Bulletin(authorSecurity, publicSpecs, privateSpecs);
		b.set(publicSpecs.get(0).getTag(), "Just any text");
		b.set(privateSpecs.get(0).getTag(), "Just any text");
		return b;
	}
	
	private Bulletin createSampleXFormsBulletin(MartusCrypto authorSecurity) throws Exception
	{
		Bulletin b = new Bulletin(authorSecurity, StandardFieldSpecs.getDefaultTopSectionFieldSpecs(), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		b.getFieldDataPacket().setXFormsModelAsString(MockMartusApp.getXFormsModelWithOnStringInputFieldXmlAsString());
		b.getFieldDataPacket().setXFormsInstanceAsString(MockMartusApp.getXFormsInstanceXmlAsString());

		return b;
	}

	String[] sampleDataSpecTags = {
			"language", "title", "eventdate", "entrydate",  
			"author", "organization", "location",
			"summary", "keywords", "publicinfo", "privateinfo",
		};
	FieldSpecCollection publicSpecs = new FieldSpecCollection(new FieldSpec[] {FieldSpec.createCustomField("frodo", "Younger Baggins", new FieldTypeMultiline()),}); 
	FieldSpecCollection privateSpecs = new FieldSpecCollection(new FieldSpec[] {FieldSpec.createCustomField("bilbo", "Older Baggins", new FieldTypeDateRange()),});
	
	MockMartusSecurity security;
	MockMartusApp app;
	KnownFieldSpecCache cache;
}
