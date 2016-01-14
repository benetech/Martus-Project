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

package org.martus.common.bulletin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Vector;

import org.martus.common.Exceptions.InvalidBulletinStateException;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusUtilities;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin.BulletinState;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.database.MockDatabase;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeLanguage;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FieldTypeUnknown;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.ExtendedHistoryEntry;
import org.martus.common.packet.ExtendedHistoryList;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.test.BulletinForTesting;
import org.martus.common.test.MockBulletinStore;
import org.martus.common.utilities.DateUtilities;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;
import org.martus.util.TestCaseEnhanced;


public class TestBulletin extends TestCaseEnhanced
{
    public TestBulletin(String name) throws Exception
	{
        super(name);
    }

    public void setUp() throws Exception
    {
	    	super.setUp();
	    	if(tempFile1 == null)
	    	{
			tempFile1 = createTempFileWithData(sampleBytes1);
			tempFile2 = createTempFileWithData(sampleBytes2);
			tempFile3 = createTempFileWithData(sampleBytes3);
			tempFile4 = createTempFileWithData(sampleBytes4);
			tempFile5 = createTempFileWithData(sampleBytes5);
			tempFile6 = createTempFileWithData(sampleBytes6);
	    	}
		proxy1 = new AttachmentProxy(tempFile1);
		proxy2 = new AttachmentProxy(tempFile2);
		proxy3 = new AttachmentProxy(tempFile3);
		proxy4 = new AttachmentProxy(tempFile4);
		proxy5 = new AttachmentProxy(tempFile5);
		proxy6 = new AttachmentProxy(tempFile6);

		if(security == null)
		{
			security = MockMartusSecurity.createClient();
		}
		store = new MockBulletinStore(this);
    }

    public void testBasics() throws Exception
    {
		Bulletin b = new Bulletin(security);
		assertEquals(false, b.isFieldInPublicSection("Nope"));
		assertEquals(false, b.isFieldInPublicSection("Location"));
		assertEquals(true, b.isFieldInPublicSection("location"));
		assertEquals(false, b.isFieldInPublicSection("LOCATION"));
		assertEquals(false, b.isFieldInPublicSection(Bulletin.TAGPRIVATEINFO));

		assertEquals(false, b.isFieldInPrivateSection("LOCATION"));
		assertEquals(true, b.isFieldInPrivateSection(Bulletin.TAGPRIVATEINFO));

		b = new Bulletin(security);
		assertNotEquals("", b.getLocalId());

		assertEquals(security, b.getSignatureGenerator());

		assertEquals("account not initialized correctly?", security.getPublicKeyString(), b.getAccount());
		assertEquals("field data account?", security.getPublicKeyString(), b.getFieldDataPacket().getAccountId());
		
		assertEquals("Not a Legacy Bulletin", b.getBulletinType(), Bulletin.BulletinType.LEGACY_BULLETIN);
	}
    
    public void testCreateRecordBulletin() throws Exception
    {
    		Bulletin b = new Bulletin(security, Bulletin.BulletinType.RECORD, StandardFieldSpecs.getDefaultTopSectionFieldSpecs(), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
    		assertEquals("Bulletin created should be of type RECORD", b.getBulletinType(), Bulletin.BulletinType.RECORD);
    }
    
    public void testCreateNoteBulletin() throws Exception
    {
    		Bulletin b = new Bulletin(security, Bulletin.BulletinType.NOTE, StandardFieldSpecs.getDefaultTopSectionFieldSpecs(), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
    		assertEquals("Bulletin created should be of type NOTE", b.getBulletinType(), Bulletin.BulletinType.NOTE);
    }

    public void testPseudoFields() throws Exception
    {
    	Bulletin b = new Bulletin(security);
    	assertEquals(b.getLocalId(), b.get("_localId"));
    	assertEquals(b.getLastSavedDate(), b.get("_lastSavedDate"));
    }
    
    public void testGetFieldType()throws Exception
    {
	    	Bulletin b = new Bulletin(security);
	    	assertEquals(new FieldTypeUnknown(), b.getFieldType("Not a real field tag"));
	    	assertEquals(new FieldTypeDate(), b.getFieldType(BulletinConstants.TAGENTRYDATE));
	    	assertEquals(new FieldTypeDateRange(), b.getFieldType(BulletinConstants.TAGEVENTDATE));
	    	assertEquals(new FieldTypeMultiline(), b.getFieldType(BulletinConstants.TAGPRIVATEINFO));
    }
    
    public void testGetLastSavedDate() throws Exception
	{
	    	Bulletin b = new Bulletin(security);
	    	b.getBulletinHeaderPacket().updateLastSavedTime();
	    	assertEquals(DateUtilities.getTodayInStoredFormat(), b.getLastSavedDate());
	}
    
    public void testContainsXFormsData() throws Exception
    {
	    	Bulletin b = new Bulletin(security);
	    	final String sampleAuthor = "Daphne Moon";
	    	b.set(Bulletin.TAGAUTHOR, sampleAuthor);
	    	assertFalse(b.containsXFormsData());
	    	b.getFieldDataPacket().setXFormsInstanceAsString("instance");
	    	assertFalse(b.containsXFormsData());
	    	b.getFieldDataPacket().setXFormsModelAsString("model");
	    	assertTrue(b.containsXFormsData());
    }
    
    public void testContains() throws Exception
    {
	    	MiniLocalization localization = new MiniLocalization();
	    	
	    	Bulletin b = new Bulletin(security);
	    	final String sampleAuthor = "Daphne Moon";
	    	b.set(Bulletin.TAGAUTHOR, sampleAuthor);
	    	assertTrue("didn't find author?", b.contains(sampleAuthor, localization));
	    	
	    	b.set(Bulletin.TAGPRIVATEINFO, samplePrivate);
	    	assertTrue("didn't find private?", b.contains(samplePrivate, localization));

		b.addPublicAttachment(proxy1);
		assertTrue("didn't find public attachment?", b.contains(proxy1.getLabel(), localization));
    
		b.addPrivateAttachment(proxy2);
		assertTrue("didn't find private attachment?", b.contains(proxy2.getLabel(), localization));
    }
	
	public void testUnknownTags() throws Exception
	{
		Bulletin b = new Bulletin(security);
		assertFalse("already has unknown?", b.hasUnknownTags());
		
		b.getBulletinHeaderPacket().setHasUnknownTags(true);
		assertTrue("header unknown", b.hasUnknownTags());
		b.getBulletinHeaderPacket().setHasUnknownTags(false);
		
		b.getFieldDataPacket().setHasUnknownTags(true);
		assertTrue("fdp unknown", b.hasUnknownTags());
		b.getFieldDataPacket().setHasUnknownTags(false);
		
		b.getPrivateFieldDataPacket().setHasUnknownTags(true);
		assertTrue("private fdp unknown", b.hasUnknownTags());
		b.getPrivateFieldDataPacket().setHasUnknownTags(false);

		assertFalse("not back to normal?", b.hasUnknownTags());
	}
	
	public void testHasUnknownCustomField() throws Exception
	{
		Bulletin noUnknown = new Bulletin(security);
		assertFalse("has unknown custom?", noUnknown.hasUnknownCustomField());

		BulletinForTesting.shouldCreateUnknownStuffInCustomField = true;
		Bulletin withUnknown = new BulletinForTesting(security);
		assertTrue("no unknown custom?", withUnknown.hasUnknownCustomField());
		BulletinForTesting.clearShoulds();
	}

	public void testAllPrivate() throws Exception
	{
		Bulletin b = new Bulletin(security);
		assertEquals("not already all private?", true, b.isAllPrivate());
		b.setAllPrivate(false);
		assertEquals("still all private?", false, b.isAllPrivate());
		b.setAllPrivate(true);
		assertEquals("not all private?", true, b.isAllPrivate());

		BulletinHeaderPacket header = b.getBulletinHeaderPacket();
		assertNotNull("No header?", header);
		FieldDataPacket data = b.getFieldDataPacket();
		assertNotNull("No data packet?", data);
		assertEquals("data id", header.getFieldDataPacketId(), data.getLocalId());
		FieldDataPacket privateData = b.getPrivateFieldDataPacket();
		assertNotNull("No private data packet?", privateData);
		assertEquals("private data id", header.getPrivateFieldDataPacketId(), privateData.getLocalId());
		assertEquals("not really private?", true, privateData.isEncrypted());
	}

	public void testId() throws Exception
	{
		Bulletin b = new Bulletin(security);
		assertNotNull("Id was Null?", b.getLocalId());
		assertEquals("Id was empty?", false, b.getLocalId().length()==0);
	}

	public void testStatus() throws Exception
	{
		Bulletin b = new Bulletin(security);
		assertEquals(Bulletin.STATUSMUTABLE, b.getStatus());
		assertEquals("Should start as Mutable", true, b.isMutable());
		b.setMutable();
		assertEquals(Bulletin.STATUSMUTABLE, b.getStatus());
		assertEquals("Should be Mutable", true, b.isMutable());
		assertEquals("Not yet Immutable", false, b.isImmutable());
		b.setImmutable();
		assertEquals(Bulletin.STATUSIMMUTABLE, b.getStatus());
		assertEquals("No longer Mutable", false, b.isMutable());
		assertEquals("Now Immutable", true, b.isImmutable());
	}

	public void testEmpty() throws Exception
	{
		Bulletin b = new Bulletin(security);
		String today = DateUtilities.getTodayInStoredFormat();
		assertEquals(today, b.get("entrydate"));

		assertEquals(MartusFlexidate.toStoredDateFormat(MultiCalendar.UNKNOWN), b.get("eventdate"));

		assertEquals(Bulletin.STATUSMUTABLE, b.getStatus());
	}

	public void testGetSet() throws Exception
	{
		Bulletin b = new Bulletin(security);
		assertEquals("", b.get("NoSuchField"));
		b.set("NoSuchField", "hello");
		assertEquals("", b.get("NoSuchField"));

		assertEquals("", b.get("author"));
		b.set("author", "hello");
		assertEquals("hello", b.get("author"));
		assertEquals("", b.get("Author"));
		assertEquals("", b.get("AUTHOR"));

		b.set("location", "94404");
		assertEquals("94404", b.get("location"));
		b.set("author", "goodbye");
		assertEquals("goodbye", b.get("author"));

		b.set(Bulletin.TAGPRIVATEINFO, "secret");
		assertEquals("secret", b.get(Bulletin.TAGPRIVATEINFO));
	}

	public void testClear() throws Exception
	{
		String publicInfo = "public info";
		String privateInfo = "private info";

		Bulletin b = new Bulletin(security);
		b.set(Bulletin.TAGPUBLICINFO, publicInfo);
		b.set(Bulletin.TAGPRIVATEINFO, privateInfo);
		HeadquartersKey key1 = new HeadquartersKey("account1");
		HeadquartersKey key2 = new HeadquartersKey("account2");
		Vector keysToUse = new Vector();
		keysToUse.add(key1);
		keysToUse.add(key2);
		HeadquartersKeys keys = new HeadquartersKeys(keysToUse);
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
		b.setAuthorizedToReadKeys(keys);
		BulletinHistory history = new BulletinHistory();
		history.add("some local Id for version 1");
		b.setHistory(history);
		assertEquals("legacy key not set?", key1.getPublicKey(), bhp.getLegacyHQPublicKey());
		assertEquals("authorized to read not set?",keys.size(), b.getAuthorizedToReadKeys().size());
		assertEquals("authorized to upload not set?",keys.size(), bhp.getAuthorizedToUploadKeys().size());
		assertEquals("public info not set?", publicInfo, b.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info not set?", privateInfo, b.get(Bulletin.TAGPRIVATEINFO));
		assertEquals("Version not 2?", 2, b.getVersion());
		b.clearAllUserData();
		assertEquals("public info not cleared?", "", b.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info not cleared?", "", b.get(Bulletin.TAGPRIVATEINFO));
		assertEquals("legacy key not cleared?", "", bhp.getLegacyHQPublicKey());
		assertEquals("authorized to read not cleared?",0, b.getAuthorizedToReadKeys().size());
		assertEquals("authorized to upload not cleared?",0, bhp.getAuthorizedToUploadKeys().size());
		assertEquals("Version should still be 2", 2, b.getVersion());
		assertEquals("EventDate not unknown?", MartusFlexidate.toStoredDateFormat(MultiCalendar.UNKNOWN), b.get(Bulletin.TAGEVENTDATE));
	}

	public void testGetStandardFieldTypes()
	{
		FieldSpec[] standardPublicFields = StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray();
		for (int i = 0; i < standardPublicFields.length; i++)
		{
			FieldSpec spec = standardPublicFields[i];
			if(spec.getTag().equals("summary"))
				assertEquals(spec.getTag(), new FieldTypeMultiline(), spec.getType());
			else if(spec.getTag().equals("publicinfo"))
				assertEquals(spec.getTag(), new FieldTypeMultiline(), spec.getType());
			else if(spec.getTag().equals("eventdate"))
				assertEquals(spec.getTag(), new FieldTypeDateRange(), spec.getType());
			else if(spec.getTag().equals("entrydate"))
				assertEquals(spec.getTag(), new FieldTypeDate(), spec.getType());
			else if(spec.getTag().equals("language"))
				assertEquals(spec.getTag(), new FieldTypeLanguage(), spec.getType());
			else
				assertEquals(spec.getTag(), new FieldTypeNormal(), spec.getType());
		}

		FieldSpec[] standardPrivateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray();
		for (int i = 0; i < standardPrivateFields.length; i++)
		{
			FieldSpec spec = standardPrivateFields[i];
			if(spec.getTag().equals("privateinfo"))
				assertEquals(spec.getTag(), new FieldTypeMultiline(), spec.getType());
			else
				assertEquals(spec.getTag(), new FieldTypeNormal(), spec.getType());
		}
	}

	public void testEncryptPublicData() throws Exception
	{

		class MyMockDatabase extends MockClientDatabase
		{
			public void writeRecordEncrypted(DatabaseKey key, String record, MartusCrypto encrypter)
			{
				++encryptWasCalled;
			}
			public int encryptWasCalled;
		}

		BulletinStore testStore = new BulletinStore();
		File tempDir = createTempDirectory();
		MyMockDatabase db = new MyMockDatabase();
		testStore.doAfterSigninInitialization(tempDir, db);
		Bulletin b = new Bulletin(security);
		b.setImmutable();
		b.setAllPrivate(false);
		testStore.saveEncryptedBulletinForTesting(b);
		assertEquals("Didn't Encrypt or Encyrpted too many packets.", 1, db.encryptWasCalled);
	}

	public void testGetStatus() throws Exception
	{
		Bulletin b1 = new Bulletin(security);
		b1.set(Bulletin.TAGPUBLICINFO, "public info");
		b1.set(Bulletin.TAGPRIVATEINFO, "private info");
		b1.setImmutable();
		assertEquals("Not Immutable Status?", BulletinConstants.STATUSIMMUTABLE, b1.getStatus());
		b1.setMutable();
		assertEquals("Not Mutable Status?", BulletinConstants.STATUSMUTABLE, b1.getStatus());
	}

	public void testCreateDraftCopyOfFromDifferentAccount() throws Exception
	{
		Bulletin b1 = new Bulletin(security);
		b1.set(Bulletin.TAGPUBLICINFO, "public info");
		b1.set(Bulletin.TAGPRIVATEINFO, "private info");
		HeadquartersKey hq = new HeadquartersKey(security.getPublicKeyString());
		b1.setAuthorizedToReadKeys(new HeadquartersKeys(hq));
		b1.setImmutable();
		BulletinHistory localHistory = b1.getHistory();
		localHistory.add("history1");
		localHistory.add("history2");
		b1.setHistory(localHistory);
		store.saveEncryptedBulletinForTesting(b1);

		MartusCrypto hqSecurity = MockMartusSecurity.createHQ();
		MockBulletinStore hqStore = new MockBulletinStore(this);
		Bulletin b2 = new Bulletin(hqSecurity);
		b2.createDraftCopyOf(b1, getDb());
		
		ExtendedHistoryList newHistory = b2.getBulletinHeaderPacket().getExtendedHistory();
		assertEquals("Didn't copy old history to extended history?", 1, newHistory.size());
		ExtendedHistoryEntry copiedHistory = newHistory.getHistory(0);
		assertEquals("Didn't copy whole old history?", localHistory.size()+1, copiedHistory.getClonedHistory().size());
		assertEquals("Didn't add most recent id?", b1.getLocalId(), copiedHistory.getClonedHistory().get(2));
		assertEquals("Should not include any Authorized to Read Keys from bulletins copied that are not ours.", 0, b2.getAuthorizedToReadKeysIncludingPending().size());
		
		Bulletin b3 = new Bulletin(security);
		b3.createDraftCopyOf(b2, hqStore.getDatabase());
		
		ExtendedHistoryList thirdHistory = b3.getBulletinHeaderPacket().getExtendedHistory();
		assertEquals("Didn't copy history to third bulletin?", 2, thirdHistory.size());
		assertEquals("Wrong first history?", 3, thirdHistory.getHistory(0).getClonedHistory().size());
		assertEquals("Wrong second history?", 1, thirdHistory.getHistory(1).getClonedHistory().size());
		assertEquals("Should not include any Authorized to Read Keys from bulletins copied that are not ours.", 0, b3.getAuthorizedToReadKeysIncludingPending().size());
	}
	
	public void testCreateDraftCopyOf() throws Exception
	{
		Bulletin b1 = new Bulletin(security);
		b1.set(Bulletin.TAGPUBLICINFO, "public info");
		b1.set(Bulletin.TAGPRIVATEINFO, "private info");
		HeadquartersKey hq = new HeadquartersKey(security.getPublicKeyString());
		b1.setAuthorizedToReadKeys(new HeadquartersKeys(hq));
		b1.setImmutable();
		BulletinHistory localHistory = b1.getHistory();
		localHistory.add("history1");
		localHistory.add("history2");
		b1.setHistory(localHistory);
		
		ExtendedHistoryList extendedHistory = new ExtendedHistoryList();
		BulletinHistory otherHistory = new BulletinHistory();
		otherHistory.add("older1");
		extendedHistory.add(MockMartusSecurity.createOtherClient().getPublicKeyString(), otherHistory);
		store.saveEncryptedBulletinForTesting(b1);
		assertEquals(2, b1.getHistory().size());
		assertEquals(3, b1.getVersion());
		assertEquals(1, b1.getAuthorizedToReadKeys().size());
		
		Bulletin b2 = new Bulletin(security);
		b2.createDraftCopyOf(b1, getDb());
		assertEquals("Not Mutable?", Bulletin.STATUSMUTABLE, b2.getStatus());
		assertEquals("signer", b1.getSignatureGenerator(), b2.getSignatureGenerator());
		assertEquals("id unchanged", false, b2.getLocalId().equals(b1.getLocalId()));
		assertEquals("public info", b1.get(Bulletin.TAGPUBLICINFO), b2.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info", b1.get(Bulletin.TAGPRIVATEINFO), b2.get(Bulletin.TAGPRIVATEINFO));
		assertEquals("wrong status?", Bulletin.STATUSMUTABLE, b2.getStatus());
		assertEquals("wrong private?", b1.isAllPrivate(), b2.isAllPrivate());
		assertEquals("didn't add to local history?", b1.getHistory().size()+1, b2.getHistory().size());
		assertEquals("wrong newest local id?", b1.getLocalId(), b2.getHistory().get(b2.getHistory().size()-1));
		assertEquals("wrong version?", b1.getVersion()+1, b2.getVersion());
		assertEquals("AuthorizedToReadKeysIncludingPending sizes don't match?", b1.getAuthorizedToReadKeysIncludingPending().size(), b2.getAuthorizedToReadKeysIncludingPending().size());
		assertEquals("Any HQ Key should still be perserved?", b1.getAuthorizedToReadKeysIncludingPending().size(), b2.getAuthorizedToReadKeysIncludingPending().size());
		assertEquals("Pending HQ Keys for copy should match originals HQ Authorized Keys?", b1.getAuthorizedToReadKeys().size(), b2.getBulletinHeaderPacket().getAuthorizedToReadKeysPending().size());
		assertEquals("Legacy HQ Key now empty since Key is now Pending.", "", b2.getBulletinHeaderPacket().getLegacyHQPublicKey());

		ExtendedHistoryList oldHistory = b1.getBulletinHeaderPacket().getExtendedHistory();
		ExtendedHistoryList newHistory = b2.getBulletinHeaderPacket().getExtendedHistory();
		assertEquals("Didn't retain extended history?", oldHistory.size(), newHistory.size());
		assertEquals("Didn't copy whole old history?", oldHistory.getHistories(), newHistory.getHistories());
		
		
		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		b1.addPublicAttachment(a1);

		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		b1.addPrivateAttachment(a2);

		b2.createDraftCopyOf(b1, getDb());
		assertEquals("public attachment count", 1, b2.getPublicAttachments().length);
		assertEquals("private attachment count", 1, b2.getPrivateAttachments().length);
		AttachmentProxy clonedPublicAttachment = b2.getPublicAttachments()[0];
		assertEquals("public attachment1 data", a1, clonedPublicAttachment);
		AttachmentProxy clonedPrivateAttachment = b2.getPrivateAttachments()[0];
		assertEquals("private attachment data", a2, clonedPrivateAttachment);
		b2.createDraftCopyOf(b1, getDb());
		assertEquals("again public attachment count", 1, b2.getPublicAttachments().length);
		assertEquals("again private attachment count", 1, b2.getPrivateAttachments().length);
		assertEquals("again public attachment1 data", a1, clonedPublicAttachment);
		assertEquals("again private attachment data", a2, clonedPrivateAttachment);

		b1.setAllPrivate(false);
		b2.createDraftCopyOf(b1, getDb());
		assertTrue("didn't set private back to true?", b2.isAllPrivate());

		b1.setAllPrivate(true);
		b2.createDraftCopyOf(b1, getDb());
		assertEquals("didn't pull private true?", b1.isAllPrivate(), b2.isAllPrivate());

		store.saveBulletinForTesting(b1);

		b2.createDraftCopyOf(b1,getDb());
		clonedPublicAttachment = b2.getPublicAttachments()[0];
		clonedPrivateAttachment = b2.getPrivateAttachments()[0];
		assertNotEquals("didn't clone the public attachment?", a1.getUniversalId().getLocalId(), clonedPublicAttachment.getUniversalId().getLocalId());
		assertNotEquals("didn't clone the private attachment?", a2.getUniversalId().getLocalId(), clonedPrivateAttachment.getUniversalId().getLocalId());
		assertEquals("Public attachment label not the same?", a1.getLabel(), clonedPublicAttachment.getLabel());
		assertEquals("Private attachment label not the same?", a2.getLabel(), clonedPrivateAttachment.getLabel());

		
		b1.changeState(BulletinState.STATE_SAVE);
		b1.setMutable();
		Bulletin b3 = new Bulletin(security);
		b3.createDraftCopyOf(b1, getDb());
		assertEquals(2, b1.getHistory().size());
		assertEquals(0, b3.getHistory().size());
		
		Bulletin b4 = new Bulletin(security);
		b1.changeState(BulletinState.STATE_SNAPSHOT);
		b4.createDraftCopyOf(b1, getDb());
		assertEquals(2, b1.getHistory().size());
		assertEquals(3, b4.getHistory().size());
		assertTrue( b4.getHistory().contains(b1.getLocalId()));
	}
	
	public void testCreateDraftCopyOfWithXFormsData() throws Exception
	{
		verifyDraftCopyOfWithXFormsData(null, null);
		verifyDraftCopyOfWithXFormsData("", "");
		verifyDraftCopyOfWithXFormsData(getXFormsModelWithOnStringInputFieldXmlAsString(), getXFormsInstanceXmlAsString());
	}

	private void verifyDraftCopyOfWithXFormsData(String xFormsModelXmlAsString, String xFormsInstanceXmlAsString) throws Exception
	{
		Bulletin original = new Bulletin(security);
		original.getFieldDataPacket().setXFormsModelAsString(xFormsModelXmlAsString);
		original.getFieldDataPacket().setXFormsInstanceAsString(xFormsInstanceXmlAsString);

		Bulletin draftCopy = new Bulletin(security);
		draftCopy.createDraftCopyOf(original, getDb());
		
		assertEquals("Incorrect xforms model data?", original.getFieldDataPacket().getXFormsModelAString(), draftCopy.getFieldDataPacket().getXFormsModelAString());
		assertEquals("Incorrect xforms instance data?", original.getFieldDataPacket().getXFormsInstanceAsString(), draftCopy.getFieldDataPacket().getXFormsInstanceAsString());
	}

	public void testAuthorizedToReadDependantOnStatus() throws Exception
	{
		Bulletin b1 = new Bulletin(security);
		b1.set(Bulletin.TAGPUBLICINFO, "public info");
		b1.set(Bulletin.TAGPRIVATEINFO, "private info");
		HeadquartersKey hq = new HeadquartersKey(security.getPublicKeyString());
		b1.setAuthorizedToReadKeys(new HeadquartersKeys(hq));

		b1.changeState(BulletinState.STATE_SAVE);
		store.saveEncryptedBulletinForTesting(b1);
		assertEquals(0, b1.getAuthorizedToReadKeys().size());
		assertEquals(1, b1.getBulletinHeaderPacket().getAuthorizedToReadKeysPending().size());
		assertEquals(1, b1.getAuthorizedToReadKeysIncludingPending().size());
		assertEquals("", b1.getBulletinHeaderPacket().getLegacyHQPublicKey());
		
		MartusCrypto other = MockMartusSecurity.createHQ();
		HeadquartersKey hq2 = new HeadquartersKey(other.getPublicKeyString());
		HeadquartersKeys newHQs = b1.getAuthorizedToReadKeysIncludingPending();
		newHQs.add(hq2);
		b1.setAuthorizedToReadKeys(new HeadquartersKeys(newHQs));
		b1.changeState(BulletinState.STATE_SNAPSHOT);
		store.saveEncryptedBulletinForTesting(b1);
		assertEquals(0, b1.getAuthorizedToReadKeys().size());
		assertEquals(2, b1.getBulletinHeaderPacket().getAuthorizedToReadKeysPending().size());
		assertEquals(2, b1.getAuthorizedToReadKeysIncludingPending().size());
		assertEquals("", b1.getBulletinHeaderPacket().getLegacyHQPublicKey());

		Bulletin b1copy = new Bulletin(security);
		b1copy.createDraftCopyOf(b1, getDb());
		MartusCrypto other2 = MockMartusSecurity.createOtherClient();
		HeadquartersKey hq3 = new HeadquartersKey(other2.getPublicKeyString());
		HeadquartersKeys with3HQs = b1copy.getAuthorizedToReadKeysIncludingPending();
		assertEquals(2, with3HQs.size());
		with3HQs.add(hq3);
		assertEquals(3, with3HQs.size());
		b1copy.setAuthorizedToReadKeys(new HeadquartersKeys(with3HQs));
		assertEquals(3, b1copy.getAuthorizedToReadKeys().size());
		assertEquals(3, b1copy.getAuthorizedToReadKeysIncludingPending().size());
		
		b1copy.changeState(BulletinState.STATE_SHARED);
		store.saveEncryptedBulletinForTesting(b1copy);
		assertEquals(0, b1copy.getBulletinHeaderPacket().getAuthorizedToReadKeysPending().size());
		assertEquals(3, b1copy.getAuthorizedToReadKeysIncludingPending().size());
		assertEquals(3, b1copy.getAuthorizedToReadKeys().size());
		assertEquals(hq.getPublicKey(), b1copy.getBulletinHeaderPacket().getLegacyHQPublicKey());

		Bulletin b2 = new Bulletin(security);
		b2.createDraftCopyOf(b1copy, getDb());
		assertEquals("B2's HQ's should be empty", 0, b2.getAuthorizedToReadKeys().size());
		assertEquals("B2's HQ's should now be pending?", 3, b2.getAuthorizedToReadKeysIncludingPending().size());
		assertEquals("B2's HQ's should now be pending?", 3, b2.getBulletinHeaderPacket().getAuthorizedToReadKeysPending().size());
		assertEquals("Legacy HQKey should be empty", "", b2.getBulletinHeaderPacket().getLegacyHQPublicKey());
		
		
		Bulletin bulletinWith3HQsInitially = new Bulletin(security);
		bulletinWith3HQsInitially.addAuthorizedToReadKeys(with3HQs);
		bulletinWith3HQsInitially.changeState(BulletinState.STATE_SAVE);
		assertEquals(3, bulletinWith3HQsInitially.getAuthorizedToReadKeysIncludingPending().size());

		MartusCrypto otherClient = MockMartusSecurity.createOtherClient();
		HeadquartersKey hq4 = new HeadquartersKey(otherClient.getPublicKeyString());
		HeadquartersKeys oneHQ = new HeadquartersKeys(hq4);
		bulletinWith3HQsInitially.setAuthorizedToReadKeys(oneHQ);
		bulletinWith3HQsInitially.changeState(BulletinState.STATE_SAVE);
		assertEquals(1, bulletinWith3HQsInitially.getAuthorizedToReadKeysIncludingPending().size());
		assertTrue(bulletinWith3HQsInitially.getAuthorizedToReadKeysIncludingPending().contains(hq4));
	}

	public void testIsStringInArray()
	{
		String a = "abc";
		String b = "bcde";
		String c = "cdefg";
		String[] abc = new String[] {a,b,c};
		assertEquals("a not in abc?", true, MartusUtilities.isStringInArray(abc, a));
		assertEquals("b not in abc?", true, MartusUtilities.isStringInArray(abc, b));
		assertEquals("c not in abc?", true, MartusUtilities.isStringInArray(abc, c));
		assertEquals("x in abc?", false, MartusUtilities.isStringInArray(abc, "xyz"));
	}

	public void testGetToday()
	{
		TimeZone realTimeZone = TimeZone.getDefault();
		try
		{
			SimpleDateFormat realDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String realToday = realDateFormat.format(new Date());
			assertEquals("Today wrong in real time zone?", realToday, DateUtilities.getTodayInStoredFormat());
			for(int offset = -11; offset < 11; ++offset)
			{
				TimeZone.setDefault(new SimpleTimeZone(offset*1000*60*60, "test"));
				Calendar cal = new GregorianCalendar();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				String today = df.format(cal.getTime());

				assertEquals("Today wrong in time zone " + offset, today, DateUtilities.getTodayInStoredFormat());
			}
		}
		finally
		{
			TimeZone.setDefault(realTimeZone);
		}
	}

	public void testAddAttachment() throws Exception
	{
		Bulletin b = new Bulletin(security);
		assertEquals("no attachments yet", 0, b.getPublicAttachments().length);
		assertEquals("no private attachments yet", 0, b.getPrivateAttachments().length);

		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		AttachmentProxy a3 = new AttachmentProxy(tempFile1);
		AttachmentProxy a4 = new AttachmentProxy(tempFile2);
		AttachmentProxy a5 = new AttachmentProxy(tempFile3);
		AttachmentProxy a6 = new AttachmentProxy(tempFile3);
		b.addPublicAttachment(a1);
		assertEquals("added one", 1, b.getPublicAttachments().length);
		b.addPublicAttachment(a2);
		assertEquals("added another", 2, b.getPublicAttachments().length);
		b.addPublicAttachment(a3);
		assertEquals("added third", 3, b.getPublicAttachments().length);

		b.addPrivateAttachment(a4);
		assertEquals("added 4", 1, b.getPrivateAttachments().length);
		b.addPrivateAttachment(a5);
		assertEquals("added 5", 2, b.getPrivateAttachments().length);
		b.addPrivateAttachment(a6);
		assertEquals("added 6", 3, b.getPrivateAttachments().length);

		AttachmentProxy[] v = b.getPublicAttachments();
		assertEquals("a1 label", tempFile1.getName(), v[0].getLabel());
		assertEquals("a2 label", tempFile2.getName(), v[1].getLabel());
		assertEquals("a3 label", tempFile1.getName(), v[2].getLabel());

		AttachmentProxy[] vp = b.getPrivateAttachments();
		assertEquals("a4 label", tempFile2.getName(), vp[0].getLabel());
		assertEquals("a5 label", tempFile3.getName(), vp[1].getLabel());
		assertEquals("a6 label", tempFile3.getName(), vp[2].getLabel());
	}

	public void testGetAndSetHQPublicKey() throws Exception
	{
		Bulletin original = new Bulletin(security);
		assertEquals("HQKey already set?", 0, original.getAuthorizedToReadKeys().size());
		original.set(Bulletin.TAGPUBLICINFO, "public info");
		String key = "12345";
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(key);
		keys.add(key1);
		
		original.setAuthorizedToReadKeys(keys);
		assertEquals("HQKey not set?", key, (original.getAuthorizedToReadKeys().get(0)).getPublicKey());
		assertEquals("HQKey not set in public?", key, (original.getFieldDataPacket().getAuthorizedToReadKeys().get(0)).getPublicKey());
		assertEquals("HQKey not set in private?", key, (original.getPrivateFieldDataPacket().getAuthorizedToReadKeys().get(0)).getPublicKey());

		HeadquartersKeys moreKeys = new HeadquartersKeys();
		String publickey2 = "1234522";
		HeadquartersKey key2 = new HeadquartersKey(publickey2);
		moreKeys.add(key2);
		assertEquals(1, original.getAuthorizedToReadKeys().size());
		assertTrue(original.getAuthorizedToReadKeys().containsKey(key1.getPublicKey()));
		original.addAuthorizedToReadKeys(moreKeys);
		assertEquals(2, original.getAuthorizedToReadKeys().size());
		assertTrue(original.getAuthorizedToReadKeys().containsKey(key1.getPublicKey()));
		assertTrue(original.getAuthorizedToReadKeys().containsKey(key2.getPublicKey()));
		original.addAuthorizedToReadKeys(moreKeys);
		assertEquals(2, original.getAuthorizedToReadKeys().size());
	}
	
	public void testAllowOnlyTheseAuthorizedKeysToRead() throws Exception
	{
		Bulletin original = new Bulletin(security);
		String key1String = "12345";
		String key2String = "22343";
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(key1String);
		HeadquartersKey key2 = new HeadquartersKey(key2String);
		keys.add(key1);
		keys.add(key2);
		
		original.setAuthorizedToReadKeys(keys);
		original.changeState(BulletinState.STATE_SAVE);
		assertEquals("both keys not set?", 2, original.getAuthorizedToReadKeysIncludingPending().size());

		HeadquartersKeys only1Key = new HeadquartersKeys();
		only1Key.add(key2);
		original.allowOnlyTheseAuthorizedKeysToRead(only1Key);
		assertEquals("Should now only have 1 key Pending?", 1, original.getAuthorizedToReadKeysIncludingPending().size());
		
		assertTrue(original.getAuthorizedToReadKeysIncludingPending().containsKey(key2.getPublicKey()));
		
		original.setAuthorizedToReadKeys(keys);
		original.changeState(BulletinState.STATE_SHARED);
		assertEquals("both keys not set?", 2, original.getAuthorizedToReadKeys().size());

		original.allowOnlyTheseAuthorizedKeysToRead(only1Key);
		assertEquals("Should now only have 1 key?", 1, original.getAuthorizedToReadKeys().size());
		
		assertTrue(original.getAuthorizedToReadKeys().containsKey(key2.getPublicKey()));
	}
	
	public void testSetState() throws Exception
	{
		Bulletin savedBulletin = new Bulletin(security);
		savedBulletin.changeState(BulletinState.STATE_SAVE);
		assertFalse(savedBulletin.isSnapshot());
		assertFalse(savedBulletin.requiresNewCopyToEdit());

		savedBulletin.changeState(BulletinState.STATE_SAVE);
		assertFalse("Allowed to set state to Save multiple times", savedBulletin.isSnapshot());
		assertFalse(savedBulletin.requiresNewCopyToEdit());
		
		Bulletin versionedBulletin = new Bulletin(security);
		versionedBulletin.changeState(BulletinState.STATE_SNAPSHOT);
		assertTrue(versionedBulletin.isSnapshot());
		assertTrue(versionedBulletin.requiresNewCopyToEdit());

		try
		{
			versionedBulletin.changeState(BulletinState.STATE_SAVE);
			fail("Once state is SNAPSHOT, you can't change its State to Save without creating a new copy.");
		} 
		catch (InvalidBulletinStateException expectedException)
		{
		}
		try
		{
			versionedBulletin.changeState(BulletinState.STATE_SNAPSHOT);
			fail("Once state is SNAPSHOT, you can't change its State to SNAPSHOT without creating a new copy.");
		} 
		catch (InvalidBulletinStateException expectedException)
		{
		}
		try
		{
			versionedBulletin.changeState(BulletinState.STATE_SHARED);
			fail("Once state is SNAPSHOT, you can't change its State to SHARED without creating a new copy.");
		} 
		catch (InvalidBulletinStateException expectedException)
		{
		}
		
		Bulletin sharedBulletin = new Bulletin(security);
		sharedBulletin.changeState(BulletinState.STATE_SAVE);
		assertFalse(sharedBulletin.isSnapshot());
		sharedBulletin.changeState(BulletinState.STATE_SAVE);
		assertFalse(sharedBulletin.isSnapshot());
		sharedBulletin.changeState(BulletinState.STATE_SHARED);
		assertTrue(sharedBulletin.isSnapshot());
		assertTrue(sharedBulletin.requiresNewCopyToEdit());
		
		try
		{
			sharedBulletin.changeState(BulletinState.STATE_SAVE);
			fail("Once state is SHARED, you can't change its State to SAVE without creating a new copy.");
		} 
		catch (InvalidBulletinStateException expectedException)
		{
		}
		try
		{
			sharedBulletin.changeState(BulletinState.STATE_SNAPSHOT);
			fail("Once state is SHARED, you can't change its State to SNAPSHOT without creating a new copy.");
		} 
		catch (InvalidBulletinStateException expectedException)
		{
		}
		try
		{
			sharedBulletin.changeState(BulletinState.STATE_SHARED);
			fail("Once state is SHARED, you can't change its State to SHARED without creating a new copy.");
		} 
		catch (InvalidBulletinStateException expectedException)
		{
		}
		
		Bulletin legacyDraft = new Bulletin(security);
		legacyDraft.setMutable();
		assertFalse(legacyDraft.requiresNewCopyToEdit());
		
		try
		{
			legacyDraft.changeState(BulletinState.STATE_LEGACY_DRAFT);
			fail("You can not set the state to LEGACY");
		} 
		catch (InvalidBulletinStateException expectedException)
		{
		}
		
		Bulletin legacySealed = new Bulletin(security);
		legacySealed.setImmutable();
		assertTrue(legacySealed.requiresNewCopyToEdit());
		try
		{
			legacySealed.changeState(BulletinState.STATE_LEGACY_SEALED);
			fail("You can not set the state to LEGACY");
		} 
		catch (InvalidBulletinStateException expectedException)
		{
		}
	}
	
	public void testGetState() throws Exception
	{
		Bulletin b1 = new Bulletin(security);
		assertEquals(Bulletin.BulletinState.STATE_SAVE, b1.getState());
		MartusCrypto hq = MockMartusSecurity.createHQ();

		HeadquartersKeys hqKeys = new HeadquartersKeys(new HeadquartersKey(hq.getPublicKeyString()));
		b1.setAuthorizedToReadKeys(hqKeys);
		assertEquals(Bulletin.BulletinState.STATE_LEGACY_DRAFT, b1.getState());

		b1.setImmutable();
		assertEquals(Bulletin.BulletinState.STATE_LEGACY_SEALED, b1.getState());
		
		b1.changeState(BulletinState.STATE_SNAPSHOT);
		assertEquals(Bulletin.BulletinState.STATE_SNAPSHOT, b1.getState());

		Bulletin b2 = new Bulletin(security);
		b2.setAuthorizedToReadKeys(hqKeys);
		b2.changeState(BulletinState.STATE_SHARED);
		assertEquals(Bulletin.BulletinState.STATE_SHARED, b2.getState());
		
		Bulletin b3 = new Bulletin(security);
		b3.setAuthorizedToReadKeys(hqKeys);
		b3.changeState(BulletinState.STATE_SAVE);
		assertEquals(Bulletin.BulletinState.STATE_SAVE, b3.getState());
	}

	public void testSetImmutableOnServer() throws Exception
	{
		Bulletin b1 = new Bulletin(security);
		assertFalse(b1.getImmutableOnServer());
		b1.setImmutableOnServer(true);
		assertTrue(b1.getImmutableOnServer());
		
		Bulletin b2 = new Bulletin(security);
		b2.createDraftCopyOf(b1, getDb());
		assertTrue(b2.getImmutableOnServer());
	}	
	
	static MockDatabase getDb()
	{
		return (MockDatabase)store.getDatabase();
	}
	
	private static String getXFormsModelWithOnStringInputFieldXmlAsString()
	{
		return 	"<xforms_model><h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" >" +
				"<h:head><h:title>XForms Sample</h:title><model><instance><nm id=\"SampleForUnitTesting\" ><name/>" +
				"</nm></instance><bind nodeset=\"/nm/name\" type=\"string\" /></model></h:head>" +
				"<h:body><input ref=\"name\" ><label>some randon name</label><hint>(required)</hint></input></h:body></h:html></xforms_model>";
	}
	
	private static String getXFormsInstanceXmlAsString()
	{
		return "<xforms_instance><nm id=\"SampleForUnitTesting\"><name>some randon name</name></nm></xforms_instance>";
	}

	static final String samplePublic = "some public text";
	static final String samplePrivate = "a bit of private text";

	static final byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
	static final byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};
	static final byte[] sampleBytes3 = {6,5,0,4,7,5,5,4,4,0};
	static final byte[] sampleBytes4 = {12,34,56};
	static final byte[] sampleBytes5 = {9,8,7,6,5};
	static final byte[] sampleBytes6 = {1,3,5,7,9,11,13};
	static File tempFile1;
	static File tempFile2;
	static File tempFile3;
	static File tempFile4;
	static File tempFile5;
	static File tempFile6;
	static AttachmentProxy proxy1;
	static AttachmentProxy proxy2;
	static AttachmentProxy proxy3;
	static AttachmentProxy proxy4;
	static AttachmentProxy proxy5;
	static AttachmentProxy proxy6;

	private static BulletinStore store;
	static MartusCrypto security;
}
