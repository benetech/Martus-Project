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

package org.martus.client.bulletinstore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore.AddOlderVersionToFolderFailedException;
import org.martus.client.bulletinstore.ClientBulletinStore.BulletinAlreadyExistsException;
import org.martus.client.core.AttachmentProxyFile;
import org.martus.client.core.FxBulletin;
import org.martus.client.core.FxBulletinField;
import org.martus.client.core.MartusClientXml;
import org.martus.client.core.TestBulletinFromXFormsLoaderConstants;
import org.martus.client.test.MockBulletinStore;
import org.martus.client.test.MockMartusApp;
import org.martus.common.BulletinSummary;
import org.martus.common.Exceptions.InvalidBulletinStateException;
import org.martus.common.FieldSpecCollection;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusXml;
import org.martus.common.MiniLocalization;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.Bulletin.BulletinState;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockDatabase;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.ExtendedHistoryList;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.Stopwatch;
import org.martus.util.TestCaseEnhanced;


public class TestClientBulletinStore extends TestCaseEnhanced
{
	static Stopwatch sw = new Stopwatch();
	
    public TestClientBulletinStore(String name) 
    {
        super(name);
    }

	public void TRACE(String text)
	{
		//System.out.println("before " + text + ": " + sw.elapsed());
		sw.start();
	}


    public void setUp() throws Exception
    {
    		super.setUp();
		testStore = new MockBulletinStore();
		db = (MockDatabase)testStore.getDatabase();
		security = (MockMartusSecurity)testStore.getSignatureGenerator();

    	if(tempFile1 == null)
    	{
			tempFile1 = createTempFileWithData(sampleBytes1);
			tempFile2 = createTempFileWithData(sampleBytes2);
    	}
    	
    	if(customPublicSpecs == null)
    	{
    		FieldSpec title = FieldSpec.createFieldSpec(new FieldTypeNormal());
    		title.setTag(Bulletin.TAGTITLE);
    		
    		customPublicSpecs =  new FieldSpecCollection(new FieldSpec[] {title});
    	}
    	if(customPrivateSpecs == null)
    	{
    		FieldSpec keyword = FieldSpec.createFieldSpec(new FieldTypeNormal());
    		keyword.setTag(Bulletin.TAGKEYWORDS);
    		FieldSpec author = FieldSpec.createFieldSpec(new FieldTypeNormal());
    		author.setTag(Bulletin.TAGAUTHOR);
    		
    		customPrivateSpecs = new FieldSpecCollection(new FieldSpec[] {keyword, author});   		    	
    	}
    }

    public void tearDown() throws Exception
    {
    		assertEquals("Still some mock streams open?", 0, db.getOpenStreamCount());
		testStore.deleteAllData();
		super.tearDown();
	}

    public void testBasics() throws Exception
    {
		TRACE("testBasics");

		BulletinFolder folder = testStore.createFolder("blah");
		assertEquals(false, (folder == null));

		Bulletin b = testStore.createEmptyBulletin();
		assertEquals("wrong author?", "", b.get("Author"));
		assertEquals("wrong account?", security.getPublicKeyString(), b.getAccount());
	}
    
    public void testIsFolderNameValid() throws Exception
    {
		MockBulletinStore clientStore = new MockBulletinStore(security);
		String emptyFolderName = "";
		String folderNameStartingWithSpace = " myFolder";
		String folderNameEndingWithSpace = "myFolder ";
		String validFolderName = "Some Folder Name";
		String invalidCharacterInFolderName = "Folder\nName"; 

		assertFalse("empty folder name is valid?", clientStore.isFolderNameValid(emptyFolderName));
		assertFalse("folders starting with a space is valid?", clientStore.isFolderNameValid(folderNameStartingWithSpace));
		assertTrue("a valid folder name is not valid?", clientStore.isFolderNameValid(validFolderName));
		assertFalse("a folder with an invalid character is valid?", clientStore.isFolderNameValid(invalidCharacterInFolderName));
		assertFalse("folders ending with a space is valid?", clientStore.isFolderNameValid(folderNameEndingWithSpace));
    }
    
    public void testGetAllVisibleFolders() throws Exception
	{
		MockBulletinStore clientStore = new MockBulletinStore(security);
		assertEquals("Should only have 4 folders", 4, clientStore.getAllFolders().size());
		assertEquals("Should only have 2 visible folders", 2, clientStore.getAllVisibleFolders().size());
		clientStore.createFolder("visible1");
		clientStore.createFolder("visible2");
		clientStore.createFolder("*invisible1");
		assertEquals("Should now have 7 folders", 7, clientStore.getAllFolders().size());
		assertEquals("Should now have 4 visible folders", 4, clientStore.getAllVisibleFolders().size());
	}
    
   public void testMigrateFoldersForBulletinVersioning() throws Exception
   {
		MockBulletinStore clientStore = new MockBulletinStore(security);
		Bulletin original = testStore.createEmptyBulletin();
		original.setImmutable();
		clientStore.saveBulletin(original);
		BulletinFolder folderA = clientStore.createFolder("A");
		BulletinFolder folderB = clientStore.createFolder("B");
		BulletinFolder invisiblefolderC = clientStore.createFolder("*C");
		
		UniversalId originalUid = original.getUniversalId();
		clientStore.addBulletinToFolder(folderA,originalUid);
		clientStore.addBulletinToFolder(folderB,originalUid);
		clientStore.addBulletinToFolder(invisiblefolderC,originalUid);
		
		assertTrue("original not in folder A?", folderA.contains(original));
		assertTrue("original not in folder B?", folderB.contains(original));
		assertTrue("original not in folder C?", invisiblefolderC.contains(original));
	
		Bulletin newVersion = testStore.createNewDraft(original, customPublicSpecs, customPrivateSpecs);
		clientStore.saveBulletin(newVersion);
		assertTrue("original should still be in folder A?", folderA.contains(original));
		assertTrue("original should still be in folder B?", folderB.contains(original));
		assertTrue("original should be in folder C?", invisiblefolderC.contains(original));
	
		folderA.add(newVersion);
		folderB.add(newVersion);
		invisiblefolderC.add(newVersion);
		
		assertTrue("original should still be in folder A for this test?", folderA.contains(original));
		assertTrue("original should still be in folder B for this test?", folderB.contains(original));
		assertTrue("original should still be in folder C?", invisiblefolderC.contains(original));
		assertTrue("newVersion not in folder A?", folderA.contains(newVersion));
		assertTrue("newVersion not in folder B?", folderB.contains(newVersion));
		assertTrue("newVersion not in folder C?", invisiblefolderC.contains(newVersion));
		
		clientStore.migrateFoldersForBulletinVersioning();
		assertFalse("original should not be in folder A after migration", folderA.contains(original));
		assertFalse("original should not be in folder B after migration", folderB.contains(original));
		assertTrue("original should still be in folder C after migration?", invisiblefolderC.contains(original));
		assertTrue("newVersion not in folder A after migration?", folderA.contains(newVersion));
		assertTrue("newVersion not in folder B after migration?", folderB.contains(newVersion));
		assertTrue("newVersion not in folder C after migration?", invisiblefolderC.contains(newVersion));
		
   }

    
	public void testRemoveBulletinFromAllFolders() throws Exception
	{
		Bulletin original = testStore.createEmptyBulletin();
		original.setImmutable();
		testStore.saveBulletin(original);
		testStore.setIsOnServer(original);
		assertTrue("original not on server?", testStore.isProbablyOnServer(original.getUniversalId()));

		Bulletin clone = testStore.createNewDraft(original, customPublicSpecs, customPrivateSpecs);
		testStore.saveBulletin(clone);
		testStore.setIsOnServer(clone);

		assertTrue("new version not on server?", testStore.isProbablyOnServer(clone.getUniversalId()));
		assertTrue("original still not on server?", testStore.isProbablyOnServer(original.getUniversalId()));

		testStore.removeBulletinFromAllFolders(clone);
		assertFalse("didn't remove original?", testStore.isProbablyOnServer(original.getUniversalId()));
	}
    
    public void testCreateDraftCopyOfMySealed() throws Exception
	{
    	Bulletin original = createImmutableBulletinWithAttachment(security);
    	
		Bulletin clone = testStore.createNewDraft(original, customPublicSpecs, customPrivateSpecs);
		assertEquals("wrong account?", testStore.getAccountId(), clone.getAccount());
		assertNotEquals("not new local id?", original.getLocalId(), clone.getLocalId());
		assertEquals("no data?", original.get(Bulletin.TAGTITLE), clone.get(Bulletin.TAGTITLE));
		assertEquals("did not clear authorized HQ?", 0, clone.getAuthorizedToReadKeys().size());
		assertEquals("did not move HQ to Pending?", 1, clone.getBulletinHeaderPacket().getAuthorizedToReadKeysPending().size());
		assertEquals("should still have 1 HQ in anyHQs since one is in pending", 1, clone.getAuthorizedToReadKeysIncludingPending().size());
		assertTrue("not Mutable?", clone.isMutable());
		assertEquals("wrong public field specs?", customPublicSpecs.size(), clone.getTopSectionFieldSpecs().size());
		assertEquals("wrong private field specs?", customPrivateSpecs.size(), clone.getBottomSectionFieldSpecs().size());
		BulletinHistory history = clone.getHistory();
		assertEquals("no history?", 1, history.size());
		assertEquals("wrong ancestor?", original.getLocalId(), history.get(0));
	}

    public void testCreateDraftCopyOfMyVersionedBulletin() throws Exception
 	{
		Bulletin originalMutable = createMutableBulletin(security);
		originalMutable.changeState(BulletinState.STATE_SNAPSHOT);
		Bulletin cloneMutable = testStore.createNewDraft(originalMutable, customPublicSpecs, customPrivateSpecs);
		assertTrue(originalMutable.isSnapshot());
		assertFalse(cloneMutable.isSnapshot());
		   
		Bulletin originalImmutable = createImmutableBulletinWithAttachment(security);
		originalImmutable.changeState(BulletinState.STATE_SNAPSHOT);
		Bulletin cloneImmutable = testStore.createNewDraft(originalMutable, customPublicSpecs, customPrivateSpecs);
		assertTrue(originalImmutable.isSnapshot());
		assertFalse(cloneImmutable.isSnapshot());
 	}
  
    public void testCreateDraftCopyOfMyDraftWithNewFieldSpecs() throws Exception
	{
		Bulletin original = createImmutableBulletinWithAttachment(security);
		original.setMutable();
		
		Bulletin clone = testStore.createNewDraft(original, customPublicSpecs, customPrivateSpecs);
		assertEquals("wrong account?", testStore.getAccountId(), clone.getAccount());
		assertNotEquals("not new local id?", original.getLocalId(), clone.getLocalId());
		assertEquals("no data?", original.get(Bulletin.TAGTITLE), clone.get(Bulletin.TAGTITLE));
		assertEquals("did not clear authorized HQ?", 0, clone.getAuthorizedToReadKeys().size());
		assertEquals("did not move HQ to Pending?", 1, clone.getBulletinHeaderPacket().getAuthorizedToReadKeysPending().size());
		assertEquals("should still have 1 HQ in anyHQs since one is in pending", 1, clone.getAuthorizedToReadKeysIncludingPending().size());
		assertTrue("not Mutable?", clone.isMutable());
		assertEquals("wrong public field specs?", customPublicSpecs.size(), clone.getTopSectionFieldSpecs().size());
		assertEquals("wrong private field specs?", customPrivateSpecs.size(), clone.getBottomSectionFieldSpecs().size());
		BulletinHistory history = clone.getHistory();
		assertEquals("has history?", 0, history.size());
 	}
    
    public void testCreateDraftCopyOfNotMyBulletin() throws Exception
	{
		MartusCrypto otherSecurity = MockMartusSecurity.createOtherClient();
		Bulletin original = createImmutableBulletinWithAttachment(otherSecurity);
		Bulletin clone = testStore.createNewDraft(original, customPublicSpecs, customPrivateSpecs);
		assertEquals("wrong account?", testStore.getAccountId(), clone.getAccount());
		assertNotEquals("not new local id?", original.getLocalId(), clone.getLocalId());
		assertEquals("no data?", original.get(Bulletin.TAGTITLE), clone.get(Bulletin.TAGTITLE));
		assertEquals("did not clear authorized HQ?", 0, clone.getAuthorizedToReadKeys().size());
		assertEquals("did not clear any pending HQs?", 0, clone.getBulletinHeaderPacket().getAuthorizedToReadKeysPending().size());
		assertEquals("We no longer keep HQs for copies of bulletins that were not ours.", 0, clone.getAuthorizedToReadKeysIncludingPending().size());
		assertTrue("not Mutable?", clone.isMutable());
		assertEquals("wrong public field specs?", customPublicSpecs.size(), clone.getTopSectionFieldSpecs().size());
		assertEquals("wrong private field specs?", customPrivateSpecs.size(), clone.getBottomSectionFieldSpecs().size());
		assertEquals("has history?", 0, clone.getHistory().size());
	}
    
    public void testCreateCloneWithTemplateAndDataFrom() throws Exception
    {
		MockBulletinStore clientStore = new MockBulletinStore(security);
		MartusCrypto otherSecurity = MockMartusSecurity.createOtherClient();
		Bulletin original = createImmutableBulletinWithAttachment(otherSecurity);
		original.setAuthorizedToReadKeys(new HeadquartersKeys(new HeadquartersKey(security.getPublicKeyString())));
		clientStore.saveBulletin(original);
		AttachmentProxy[] originalAttachments = original.getPublicAttachments();
		assertEquals("Original Attachment not added?", 1, originalAttachments.length);
		AttachmentProxyFile originalApf = AttachmentProxyFile.extractAttachment(clientStore, originalAttachments[0]);
		File originalFile = originalApf.getFile();
		assertNotNull(originalFile);
		assertTrue ("original file didn't end in .txt?", originalFile.getName().endsWith(ATTACHMENT_1_EXTENSION));
		originalApf.release();
	    	
		Bulletin clone = clientStore.createCloneWithTemplateAndDataFrom(original);
		AttachmentProxy[] cloneAttachmentsBeforeSave = clone.getPublicAttachments();
		AttachmentProxy cloneAttachmentProxyBeforeSave = cloneAttachmentsBeforeSave[0];
		assertNull(cloneAttachmentProxyBeforeSave.getFile());
		AttachmentProxyFile cloneApf = AttachmentProxyFile.extractAttachment(clientStore, cloneAttachmentProxyBeforeSave);
		File cloneFile = cloneApf.getFile();
		assertNotNull(cloneFile);
		assertTrue ("cloned attachment before save didn't end in .txt?", cloneFile.getName().endsWith(ATTACHMENT_1_EXTENSION));
		cloneApf.release();

		clientStore.saveBulletin(clone);
		AttachmentProxy[] cloneAttachmentsAfterSave = clone.getPublicAttachments();
		AttachmentProxyFile cloneApfAfterSave = AttachmentProxyFile.extractAttachment(clientStore, cloneAttachmentsAfterSave[0]);
		File cloneFileAfterSave = cloneApfAfterSave.getFile();
		assertEquals("Clone Attachment after save not added?", 1, cloneAttachmentsAfterSave.length);
		assertNotNull(cloneFileAfterSave);
		assertTrue ("cloned attachment after save didn't end in .txt?", cloneFileAfterSave.getName().endsWith(ATTACHMENT_1_EXTENSION));
		cloneApfAfterSave.release();

		assertEquals("wrong account?", testStore.getAccountId(), clone.getAccount());
		assertNotEquals("not new local id?", original.getLocalId(), clone.getLocalId());
		assertEquals("no data?", original.get(Bulletin.TAGTITLE), clone.get(Bulletin.TAGTITLE));
		assertEquals("did not clear authorized HQ?", 0, clone.getAuthorizedToReadKeys().size());
		assertEquals("did not clear any pending HQs?", 0, clone.getBulletinHeaderPacket().getAuthorizedToReadKeysPending().size());
		assertEquals("We no longer keep HQs for copies of bulletins that were not ours.", 0, clone.getAuthorizedToReadKeysIncludingPending().size());
		assertEquals("has history?", 0, clone.getHistory().size());
    }
    
    public void testCreateNewDraftWithCurrentTemplateButIdAndDataAndHistoryFrom() throws Exception
    {
		MockBulletinStore clientStore = new MockBulletinStore(security);
    	
    	Bulletin original = createMutableBulletin(security);

    	String customTag = "custom";
    	FieldSpec customFieldSpec = FieldSpec.createCustomField(customTag, "Label", new FieldTypeNormal());
    	original.getTopSectionFieldSpecs().add(customFieldSpec);
    	original.setAuthorizedToReadKeys(new HeadquartersKeys(new HeadquartersKey(security.getPublicKeyString())));
		original.set(customTag, "Whatever");
		
    	BulletinHistory fakeHistory = new BulletinHistory();
    	fakeHistory.add("SomeLocalId");
    	original.setHistory(fakeHistory);
    	ExtendedHistoryList fakeExtendedHistory = new ExtendedHistoryList();
    	BulletinHistory fakeOtherHistory = new BulletinHistory();
    	fakeOtherHistory.add("OtherLocalId");
    	fakeExtendedHistory.add(MockMartusSecurity.createClient().getPublicKeyString(), fakeOtherHistory);
    	BulletinHeaderPacket originalHeader = original.getBulletinHeaderPacket();
		originalHeader.setExtendedHistory(fakeExtendedHistory);
    	
    	original.getAuthorizedToReadKeys().add(new HeadquartersKey(MockMartusSecurity.createServer().getPublicKeyString()));
    	clientStore.saveBulletin(original);

    	Bulletin clone = clientStore.createNewDraftWithCurrentTemplateButIdAndDataAndHistoryFrom(original);
    	assertEquals(original.getUniversalId(), clone.getUniversalId());
    	assertEquals(original.get(customTag), clone.get(customTag));
    	assertEquals(original.getHistory().toString(), clone.getHistory().toString());
    	assertEquals(0, clone.getAuthorizedToReadKeys().size());

    	BulletinHeaderPacket cloneHeader = clone.getBulletinHeaderPacket();
		assertEquals(originalHeader.getExtendedHistory().size(), cloneHeader.getExtendedHistory().size());
    	assertEquals(originalHeader.getAuthorizedToReadKeysPending(), cloneHeader.getAuthorizedToReadKeysPending());
    }

   private Bulletin createImmutableBulletinWithAttachment(MartusCrypto otherSecurity) throws Exception
	{
		HeadquartersKeys oldHq = new HeadquartersKeys(new HeadquartersKey(fakeHqKey));
		Bulletin original = new Bulletin(otherSecurity);
		original.set(Bulletin.TAGTITLE, PUBLIC_DATA);
		original.set(Bulletin.TAGAUTHOR, PRIVATE_DATA);
		original.setAuthorizedToReadKeys(oldHq);
		File attachment = createAttachment(ATTACHMENT_1_DATA);
		original.addPublicAttachment(new AttachmentProxy(attachment));
		original.setImmutable();
		return original;
	}

	private File createAttachment(String data) throws IOException 
	{
		return stringToFile("$$$MartusBulletinTempAttachment", ATTACHMENT_1_EXTENSION, data);
	}

     private Bulletin createMutableBulletin(MartusCrypto otherSecurity) throws Exception
	{
		Bulletin b = createImmutableBulletinWithAttachment(otherSecurity);
		b.setMutable();
		return b;
	}

    public void testChooseBulletinToUpload() throws Exception
	{
		BulletinFolder outbox = testStore.createFolder("*My outbox");
		int count = 10;
		Bulletin[] bulletins = new Bulletin[count];
		Set bulletinsToBeSent = new HashSet();
		for(int i=0; i < count; ++i)
		{
			bulletins[i] = testStore.createEmptyBulletin();
			testStore.saveBulletin(bulletins[i]);
			UniversalId universalId = bulletins[i].getUniversalId();
			testStore.addBulletinToFolder(outbox, universalId);
			bulletinsToBeSent.add(universalId);
		}
		
		UniversalId uidRemoved = bulletins[3].getUniversalId();
		UniversalId uidDiscarded = bulletins[6].getUniversalId();
		UniversalId uidRemovedAndDiscarded = bulletins[9].getUniversalId();
		
		testStore.removeBulletinFromFolder(outbox, uidRemoved);
		testStore.removeBulletinFromFolder(outbox, uidRemovedAndDiscarded);
		
		BulletinFolder discarded = testStore.getFolderDiscarded();
		testStore.addBulletinToFolder(discarded, uidDiscarded);
		testStore.addBulletinToFolder(discarded, uidRemovedAndDiscarded);
		
		bulletinsToBeSent.remove(uidRemoved);
		bulletinsToBeSent.remove(uidRemovedAndDiscarded);
		bulletinsToBeSent.remove(uidDiscarded);
		
		Set bulletinsActuallySent = new HashSet();
		for(int startIndex=0; startIndex < count; ++startIndex)
		{
			UniversalId gotUid = testStore.chooseBulletinToUpload(outbox, startIndex).getUniversalId();
			bulletinsActuallySent.add(gotUid);
			assertNotEquals("Sent removed bulletin?", uidRemoved, gotUid);
			assertNotEquals("Sent discarded bulletin?", uidDiscarded, gotUid);
			assertNotEquals("Sent removed and discarded bulletin?", uidRemovedAndDiscarded, gotUid);
		}
		assertEquals("Didn't send expected bulletins?", bulletinsToBeSent, bulletinsActuallySent);
    	
	}
    
	public void testHasAnyNonDiscardedBulletins() throws Exception
	{
		Bulletin b1 = testStore.createEmptyBulletin();
		Bulletin b2 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b1);
		testStore.saveBulletin(b2);
		
		BulletinFolder outbox = testStore.createFolder("*My Outbox");
		testStore.addBulletinToFolder(outbox, b1.getUniversalId());
		testStore.addBulletinToFolder(outbox, b2.getUniversalId());

		BulletinFolder visible = testStore.createFolder("Other Folder");
		testStore.addBulletinToFolder(visible, b1.getUniversalId());
		testStore.addBulletinToFolder(visible, b2.getUniversalId());

		assertTrue("thinks some are discarded?", testStore.hasAnyNonDiscardedBulletins(outbox));
		
		BulletinFolder discarded = testStore.getFolderDiscarded();
		testStore.addBulletinToFolder(discarded, b1.getUniversalId());
		assertTrue("2 in x but all discarded?", testStore.hasAnyNonDiscardedBulletins(outbox));

		testStore.addBulletinToFolder(discarded, b2.getUniversalId());
		assertFalse("all in x and discarded means we don't have any that has not been discarded?", testStore.hasAnyNonDiscardedBulletins(outbox));
		testStore.removeBulletinFromFolder(visible, b1);
		testStore.removeBulletinFromFolder(visible, b2);
		assertFalse("doesn't see all are discarded?", testStore.hasAnyNonDiscardedBulletins(outbox));
	}
	
    public void testNeedsFolderMigration()
    {
    	assertFalse("normal store needs migration?", testStore.needsFolderMigration());
		testStore.createSystemFolder(ClientBulletinStore.OBSOLETE_OUTBOX_FOLDER);
		assertTrue("outbox doesn't trigger migration?", testStore.needsFolderMigration());
		testStore.deleteFolder(ClientBulletinStore.OBSOLETE_OUTBOX_FOLDER);
		testStore.createSystemFolder(ClientBulletinStore.OBSOLETE_DRAFT_FOLDER);
		assertTrue("drafts doesn't trigger migration?", testStore.needsFolderMigration());
		testStore.deleteFolder(ClientBulletinStore.OBSOLETE_DRAFT_FOLDER);
    }
    
    public void testMigrateFolders() throws Exception
    {

		BulletinFolder outbox = testStore.createSystemFolder(ClientBulletinStore.OBSOLETE_OUTBOX_FOLDER);
		Bulletin saved = testStore.createEmptyBulletin();
		testStore.saveBulletin(saved);
		testStore.addBulletinToFolder(outbox, saved.getUniversalId());
		
		BulletinFolder drafts = testStore.createSystemFolder(ClientBulletinStore.OBSOLETE_DRAFT_FOLDER);
		Bulletin draft = testStore.createEmptyBulletin();
		testStore.saveBulletin(draft);
		testStore.addBulletinToFolder(drafts, draft.getUniversalId());
	    	
		assertFalse("Already saved folders?", testStore.getFoldersFile().exists());
		assertTrue("Migration failed?", testStore.migrateFolders());
		assertTrue("Didn't save changes?", testStore.getFoldersFile().exists());
	
		assertEquals(2, testStore.getFolderSaved().getBulletinCount());
		assertEquals(1, testStore.getFolderSealedOutbox().getBulletinCount());
		assertEquals(0, testStore.getFolderSealedOutbox().find(saved.getUniversalId()));
		
		assertNull("Didn't remove outbox?", testStore.findFolder(ClientBulletinStore.OBSOLETE_OUTBOX_FOLDER));
		assertNull("Didn't remove drafts folder?", testStore.findFolder(ClientBulletinStore.OBSOLETE_DRAFT_FOLDER));
    	
    }
    
	public void testGetStandardFieldNames()
	{
		FieldSpec[] publicFields = StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray();
		Set publicTags = new HashSet();
		for(int i = 0; i < publicFields.length; ++i)
			publicTags.add(publicFields[i].getTag());
		assertEquals(true, publicTags.contains("author"));
		assertEquals(false, publicTags.contains("privateinfo"));
		assertEquals(false, publicTags.contains("nope"));
		assertEquals(true, publicTags.contains("language"));
		assertEquals(true, publicTags.contains("organization"));

		FieldSpec[] privateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray();
		Set privateTags = new HashSet();
		for(int i = 0; i < privateFields.length; ++i)
			privateTags.add(privateFields[i].getTag());
		
		assertEquals(true, privateTags.contains("privateinfo"));
		assertEquals(false, privateTags.contains("nope"));
	}

	public void testGetAllBulletinUids() throws Exception
	{
		TRACE("testGetAllBulletinUids");
		Set empty = testStore.getAllBulletinLeafUids();
		assertEquals("not empty?", 0, empty.size());

		Bulletin b = testStore.createEmptyBulletin();
		testStore.saveBulletin(b);
		Set one = testStore.getAllBulletinLeafUids();
		assertEquals("not one?", 1, one.size());
		assertTrue("wrong uid 1?", one.contains(b.getUniversalId()));

		Bulletin b2 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b2);
		Set two = testStore.getAllBulletinLeafUids();
		assertEquals("not two?", 2, two.size());
		assertTrue("missing 1?", two.contains(b.getUniversalId()));
		assertTrue("missing 2?", two.contains(b2.getUniversalId()));
	}

	public void testDestroyBulletin() throws Exception
	{
		TRACE("testDestroyBulletin");
		int originalRecordCount = db.getRecordCount();

		Bulletin b = testStore.createEmptyBulletin();
		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		b.addPublicAttachment(a1);
		assertEquals("added one", 1, b.getPublicAttachments().length);

		b.addPrivateAttachment(a2);
		assertEquals("added 4", 1, b.getPrivateAttachments().length);

		testStore.saveBulletin(b);
		BulletinFolder f = testStore.createFolder("test");
		f.add(b);
		testStore.destroyBulletin(b);
		assertEquals(0, testStore.getBulletinCount());
		assertEquals(0, f.getBulletinCount());
		assertEquals(originalRecordCount, db.getRecordCount());
		
		PartialBulletinCache cache = testStore.getCache();
		assertFalse("found a destroyed bulletin?", cache.isBulletinCached(b.getUniversalId()));
	}

	public void testGetFieldData() throws Exception
	{
		TRACE("testGetFieldData");
		assertEquals(0, testStore.getBulletinCount());
		String sampleSummary = "Summary Data";
		String sampleEventDate = "11-20-2002";
		Bulletin b = testStore.createEmptyBulletin();
		b.set(Bulletin.TAGSUMMARY, sampleSummary);
		b.set(Bulletin.TAGEVENTDATE, sampleEventDate);
		b.setMutable();
		testStore.saveBulletin(b);
		UniversalId uId = b.getUniversalId();
		assertEquals("Wrong summary?", sampleSummary, testStore.getFieldData(uId, Bulletin.TAGSUMMARY));
		assertEquals("Wrong event date?", sampleEventDate, testStore.getFieldData(uId, Bulletin.TAGEVENTDATE));
		assertEquals("Wrong status?", b.getStatus(), testStore.getFieldData(uId, Bulletin.TAGSTATUS));
		assertEquals("Unknown status not set?", "", testStore.getFieldData(uId, Bulletin.TAGWASSENT));
		testStore.setIsOnServer(b);
		assertEquals("Status not Sent?", ClientBulletinStore.WAS_SENT_YES, testStore.getFieldData(uId, Bulletin.TAGWASSENT));
		testStore.setIsNotOnServer(b);
		assertEquals("Status not unSent?", ClientBulletinStore.WAS_SENT_NO, testStore.getFieldData(uId, Bulletin.TAGWASSENT));

	}

	public void testSaveBulletin() throws Exception
	{
		TRACE("testSaveBulletin");

		final String initialSummary = "New bulletin";

		assertEquals(0, testStore.getBulletinCount());

		Bulletin b = testStore.createEmptyBulletin();
		b.set(Bulletin.TAGSUMMARY, initialSummary);

		testStore.saveBulletin(b);
		UniversalId uId = b.getUniversalId();
		assertEquals(1, testStore.getBulletinCount());
		assertEquals(false, (uId.toString().length() == 0));
		assertEquals("not saved initially?", initialSummary, testStore.getBulletinRevision(uId).get(Bulletin.TAGSUMMARY));

		// re-saving the same bulletin replaces the old one
		UniversalId id = b.getUniversalId();
		testStore.saveBulletin(b);
		assertEquals(1, testStore.getBulletinCount());
		assertEquals("Saving should keep same id", id, b.getUniversalId());
		assertEquals("not still saved?", initialSummary, testStore.getBulletinRevision(uId).get(Bulletin.TAGSUMMARY));

		// unsaved bulletin changes should not be in the store
		b.set(Bulletin.TAGSUMMARY, "not saved yet");
		assertEquals("saved without asking?", initialSummary, testStore.getBulletinRevision(uId).get(Bulletin.TAGSUMMARY));

		// saving a new bulletin with a non-empty id should retain that id
		int oldCount = testStore.getBulletinCount();
		b = testStore.createEmptyBulletin();
		UniversalId uid = b.getBulletinHeaderPacket().getUniversalId();
		testStore.saveBulletin(b);
		assertEquals(oldCount+1, testStore.getBulletinCount());
		assertEquals("b uid?", uid, b.getBulletinHeaderPacket().getUniversalId());

		b = testStore.getBulletinRevision(uid);
		assertEquals("store uid?", uid, b.getBulletinHeaderPacket().getUniversalId());
	}

	public void testSaveBulletinWithState() throws Exception
	{
		TRACE("testSaveBulletinWithState");

		final String initialSummary = "New bulletin";

		assertEquals(0, testStore.getBulletinCount());

		Bulletin b = testStore.createEmptyBulletin();
		b.set(Bulletin.TAGSUMMARY, initialSummary);
		b.changeState(BulletinState.STATE_SAVE);
		testStore.saveBulletin(b);
		
		UniversalId uId = b.getUniversalId();
		Bulletin retrievedBulletinSavedState = testStore.getBulletinRevision(uId);
		retrievedBulletinSavedState.changeState(BulletinState.STATE_SAVE);
		testStore.saveBulletin(retrievedBulletinSavedState);
		
		Bulletin versionStateBulletin = testStore.getBulletinRevision(uId);
		versionStateBulletin.changeState(BulletinState.STATE_SNAPSHOT);
		testStore.saveBulletin(versionStateBulletin);

		Bulletin retrievedVersionStateBulletin = testStore.getBulletinRevision(uId);
		try
		{
			retrievedVersionStateBulletin.changeState(BulletinState.STATE_SAVE);
			fail("A retrieved VersionState Bulletin should not allow the state to be changed without making a new version.");
		} 
		catch (InvalidBulletinStateException expected)
		{
		}
		
		try
		{
			retrievedVersionStateBulletin.changeState(BulletinState.STATE_SHARED);
			fail("A retrieved VersionState Bulletin should not allow the state to SHARED without making a new version.");
		} 
		catch (InvalidBulletinStateException expected)
		{
		}
	}

	public void testFindBulletinById() throws Exception
	{
		TRACE("testFindBulletinById");

		assertEquals(0, testStore.getBulletinCount());
		UniversalId uInvalidId = UniversalIdForTesting.createDummyUniversalId();
		Bulletin b = testStore.getBulletinRevision(uInvalidId);
		assertEquals(true, (b == null));

		b = testStore.createEmptyBulletin();
		b.set(BulletinConstants.TAGSUMMARY, "whoop-dee-doo");
		b.setMutable();
		testStore.saveBulletin(b);
		UniversalId id = b.getUniversalId();

		Bulletin b2 = testStore.getBulletinRevision(id);
		assertEquals(false, (b2 == null));
		assertEquals(b.get(BulletinConstants.TAGSUMMARY), b2.get(BulletinConstants.TAGSUMMARY));
		
		b.setImmutable();
		testStore.saveBulletin(b);

		Bulletin b3 = testStore.getBulletinRevision(id);
		assertEquals(false, (b3 == null));
		assertEquals(b.get(BulletinConstants.TAGSUMMARY), b3.get(BulletinConstants.TAGSUMMARY));
	}
	
	public void testCopyBulletin() throws Exception
	{
		TRACE("testCopyBulletin");
		assertEquals(0, testStore.getBulletinCount());
		
		ClientBulletinStore hqStore = createTempStore();
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(hqStore.getAccountId());
		keys.add(key1);
	
		Bulletin original = testStore.createEmptyBulletin();
		String originalTitle = "original Title!";
		original.set(BulletinConstants.TAGTITLE, originalTitle);
		original.setAuthorizedToReadKeys(keys);
		original.getBulletinHeaderPacket().setAuthorizedToReadKeysPending(keys);
		{
			BulletinHistory fakeHistory = new BulletinHistory();
			fakeHistory.add("older version");
			original.setHistory(fakeHistory);
		}
		{
			BulletinHistory extendedHistory = new BulletinHistory();
			extendedHistory.add("other version");
			ExtendedHistoryList extendedHistoryList = new ExtendedHistoryList();
			extendedHistoryList.add("Other account", extendedHistory);
			original.getBulletinHeaderPacket().setExtendedHistory(extendedHistoryList);
		}
		original.setMutable();
		testStore.saveBulletin(original);

		assertNotEquals("Original doesn't have history?", 0, original.getHistory().size());
		assertNotEquals("Original doesn't have extended history?", 0, original.getBulletinHeaderPacket().getExtendedHistory());

		UniversalId originalId = original.getUniversalId();
		String copyTitle = "Copy of original Title!";
		Bulletin copy = testStore.copyBulletinWithoutContactsOrHistory(originalId, copyTitle);
		String returnedCopy1Title = copy.get(Bulletin.TAGTITLE);
		assertNotEquals("Original Bulletin Id is the same as the Copy1's?",originalId.toString(), copy.getUniversalIdString());
		assertEquals("Copy should have a title its own title", copyTitle, returnedCopy1Title);
		assertEquals("Original Bulletin does not have a contact?", 1, original.getAuthorizedToReadKeys().size());
		assertEquals("Copy Bulletin has a contact?", 0, copy.getAuthorizedToReadKeysIncludingPending().size());
		assertEquals("Copy kept the history?", 0, copy.getHistory().size());
		assertEquals("Copy kept the extended history?", 0, copy.getBulletinHeaderPacket().getExtendedHistory().size());
		
		original.setImmutable();
		original.setImmutableOnServer(true);
		original.getBulletinHeaderPacket().setSnapshot(true);
		testStore.saveBulletin(original);
		Bulletin copyOfImmutable = testStore.copyBulletinWithoutContactsOrHistory(originalId, copyTitle);
		assertNotEquals("Original Bulletin Id is the same as the Copy2's?",originalId.toString(), copyOfImmutable.getUniversalIdString());
		assertEquals("Copy2 should have a title its own title", copyOfImmutable.get(Bulletin.TAGTITLE), returnedCopy1Title);
		assertEquals("Copy2 Bulletin does not have a contact?", 0, copyOfImmutable.getAuthorizedToReadKeysIncludingPending().size());
		assertEquals("Copy2 kept the history?", 0, copyOfImmutable.getHistory().size());
		assertEquals("Copy2 kept the extended history?", 0, copyOfImmutable.getBulletinHeaderPacket().getExtendedHistory().size());
		
		assertEquals("original lost its history?", 1, original.getHistory().size());
		assertEquals("original lost its extended history?", 1, original.getBulletinHeaderPacket().getExtendedHistory().size());
	}

	public void testDiscardBulletin() throws Exception
	{
		TRACE("testDiscardBulletin");

		BulletinFolder f = testStore.getFolderSaved();
		assertNotNull("Need Sent folder", f);
		BulletinFolder discarded = testStore.getFolderDiscarded();
		assertNotNull("Need Discarded folder", f);

		Bulletin start1 = testStore.createEmptyBulletin();
		testStore.saveBulletin(start1);
		f.add(start1);

		Bulletin b = f.getBulletinSorted(0);
		assertNotNull("Sent folder should have bulletins", b);

		assertEquals(true, f.contains(b));
		assertEquals(false, discarded.contains(b));
		testStore.discardBulletin(f, b);
		assertEquals("Bulletin wasn't discarded!", false, f.contains(b));
		assertEquals("Bulletin wasn't copied to Discarded", true, discarded.contains(b));

		Bulletin b2 = testStore.createEmptyBulletin();
		b2.set("subject", "amazing");
		testStore.saveBulletin(b2);
		BulletinFolder user1 = testStore.createFolder("1");
		BulletinFolder user2 = testStore.createFolder("2");
		user1.add(b2);
		user2.add(b2);

		assertEquals(true, user1.contains(b2));
		assertEquals(true, user2.contains(b2));
		assertEquals(false, discarded.contains(b2));
		testStore.discardBulletin(user1, b2);
		assertEquals("Bulletin wasn't discarded!", false, user1.contains(b2));
		assertEquals("Copy of bulletin accidentally discarded\n", true, user2.contains(b2));
		assertEquals("Should be in Discarded now", true, discarded.contains(b2));
		testStore.discardBulletin(user2, b2);
		assertEquals("Bulletin wasn't discarded!", false, user2.contains(b2));
		assertEquals("Should be in Discarded now", true, discarded.contains(b2));

		testStore.discardBulletin(discarded, b2);
		assertEquals("Should no longer be in Discarded", false, discarded.contains(b2));
		assertNull("Should no longer exist at all", testStore.getBulletinRevision(b2.getUniversalId()));
	}

	public void testRemoveBulletinFromFolder() throws Exception
	{
		TRACE("testRemoveBulletinFromFolder");

		BulletinFolder f = testStore.getFolderSaved();
		assertNotNull("Need Sent folder", f);

		Bulletin b1 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b1);
		f.add(b1);
		assertEquals(true, f.contains(b1));
		testStore.removeBulletinFromFolder(f, b1);
		assertEquals(false, f.contains(b1));
	}

	public void testCreateFolder()
	{
		TRACE("testCreateFolder");

		BulletinFolder folder = testStore.createFolder("blah");
		assertEquals(false, (folder == null));

		BulletinFolder folder2 = testStore.createFolder("blah");
		assertNull("Can't create two folders with same name", folder2);
	}

	public void testCreateOrFindFolder()
	{
		TRACE("testCreateOrFindFolder");

		assertNull("x shouldn't exist", testStore.findFolder("x"));
		BulletinFolder folder = testStore.createOrFindFolder("x");
		assertNotNull("Create x", folder);

		BulletinFolder folder2 = testStore.createOrFindFolder("x");
		assertEquals(folder, folder2);
	}

	public void testCreateSystemFolders()
	{
		TRACE("testCreateSystemFolders");

		BulletinFolder fSent = testStore.getFolderSaved();
		assertNotNull("Should have created Sent folder", fSent);

		BulletinFolder fDiscarded = testStore.getFolderDiscarded();
		assertNotNull("Should have created Discarded folder", fDiscarded);

		BulletinFolder fDraftOutbox = testStore.getFolderDraftOutbox();
		assertNotNull("Should have created DraftOutbox folder", fDraftOutbox);

		BulletinFolder fSealedOutbox = testStore.getFolderSealedOutbox();
		assertNotNull("No SealedOutbox?", fSealedOutbox);

		BulletinFolder fImport = testStore.getFolderImport();
		assertNotNull("No Import Folder?", fImport);
	}

	public void testFindFolder()
	{
		TRACE("testFindFolder");

		int count = testStore.getFolderCount();

		testStore.createFolder("peter");
		testStore.createFolder("paul");
		testStore.createFolder("john");
		testStore.createFolder("ringo");
		assertEquals(count+4, testStore.getFolderCount());

		BulletinFolder folder = testStore.findFolder("paul");
		assertEquals(false, (folder==null));
	}

	public void testSetFolderOrder()throws Exception
	{
		MockBulletinStore myStore = new MockBulletinStore();

		BulletinFolder folder1 = myStore.createFolder("1");
		BulletinFolder folder2 = myStore.createFolder("2");
		BulletinFolder folder3 = myStore.createFolder("3");
		
		Vector namesOriginal = myStore.getAllFolderNames();
		assertEquals(myStore.getFolderSaved().getName(), namesOriginal.get(0));
		assertEquals(myStore.getFolderDiscarded().getName(), namesOriginal.get(1));
		assertEquals(myStore.getFolderDraftOutbox().getName(), namesOriginal.get(2));
		assertEquals(myStore.getFolderSealedOutbox().getName(), namesOriginal.get(3));
		assertEquals(folder1.getName(), namesOriginal.get(4));
		assertEquals(folder2.getName(), namesOriginal.get(5));
		assertEquals(folder3.getName(), namesOriginal.get(6));

		Vector newOrder = new Vector();
		newOrder.add(folder2);
		newOrder.add(myStore.getFolderSaved());
		newOrder.add(folder3);
		newOrder.add(myStore.getFolderDraftOutbox());
		newOrder.add(folder1);
		newOrder.add(myStore.getFolderSealedOutbox());
		newOrder.add(myStore.getFolderDiscarded());

		myStore.setFolderOrder(newOrder);
		Vector namesReordered = myStore.getAllFolders();
		for(int i = 0; i < myStore.getFolderCount(); ++i )
		{
			assertEquals(newOrder.get(i), namesReordered.get(i));
		}
		
		newOrder.remove(3);
		try
		{
			myStore.setFolderOrder(newOrder);
			fail("Should have thrown since folder list was missing an entry");
		}
		catch(Exception expected)
		{
			
		}
		myStore.deleteAllData();
	}
	
	public void testRenameFolder()
	{
		TRACE("testRenameFolder");

		assertEquals(false, testStore.renameFolder("a", "b"));

		BulletinFolder folder = testStore.createFolder("a");
		assertEquals(true, testStore.renameFolder("a", "b"));
		assertEquals(null, testStore.findFolder("a"));
		assertEquals(folder, testStore.findFolder("b"));

		BulletinFolder f2 = testStore.createFolder("a");
		assertEquals(false, testStore.renameFolder("a", "b"));
		assertEquals(folder, testStore.findFolder("b"));
		assertEquals(f2, testStore.findFolder("a"));
		
		BulletinFolder f3 = testStore.createFolder("abc");
		assertEquals(false, testStore.renameFolder("abc", "*-   abcd"));
		assertEquals(f3, testStore.findFolder("abc"));
		
		assertEquals(false, testStore.renameFolder("abc", " abcd"));
		assertEquals(f3, testStore.findFolder("abc"));
		
		assertEquals(false, testStore.renameFolder("abc", "ab cd "));
		assertEquals(true, testStore.renameFolder("abc", "ab cd"));
		assertEquals(f3, testStore.findFolder("ab cd"));
		
		
		BulletinFolder f4 = testStore.createFolder("folder1");
		assertEquals(false, testStore.renameFolder("folder1", "fo--d"));
		assertEquals(f4, testStore.findFolder("folder1"));
		
		BulletinFolder f5 = testStore.createFolder("folder2");
		assertEquals(false, testStore.renameFolder("folder2", "fo@\\"));
		assertEquals(f5, testStore.findFolder("folder2"));
		
	}

	public void testDeleteFolder() throws Exception
	{
		TRACE("testDeleteFolder");

		assertEquals(false, testStore.deleteFolder("a"));
		BulletinFolder folder = testStore.createFolder("a");
		assertEquals(true, testStore.deleteFolder("a"));

		folder = testStore.createFolder("a");
		assertNotNull("Couldn't create folder a", folder);
		folder.preventDelete();
		assertEquals(false, testStore.deleteFolder("a"));
		folder = testStore.findFolder("a");
		assertNotNull("Should have been non-deletable", folder);

		folder = testStore.createFolder("b");
		assertNotNull("Couldn't create folder b", folder);
		Bulletin b = testStore.createEmptyBulletin();
		b.set("subject", "golly");
		testStore.saveBulletin(b);
		folder.add(b);
		assertEquals(true, folder.contains(b));
		testStore.deleteFolder("b");
		folder = testStore.getFolderDiscarded();
		assertEquals("B should be in discarded", true, folder.contains(b));
	}

	public void testMoveBulletin() throws Exception
	{
		TRACE("testMoveBulletin");

		BulletinFolder folderA = testStore.createFolder("a");
		BulletinFolder folderB = testStore.createFolder("b");
		Bulletin b = testStore.createEmptyBulletin();
		testStore.saveBulletin(b);
		assertEquals("not in a", false, folderA.contains(b));
		assertEquals("not in b", false, folderB.contains(b));

		testStore.moveBulletin(b, folderA, folderB);
		assertEquals("still not in a", false, folderA.contains(b));
		assertEquals("moved into b", true, folderB.contains(b));

		testStore.moveBulletin(b, folderB, folderA);
		assertEquals("now in a", true, folderA.contains(b));
		assertEquals("no longer in b", false, folderB.contains(b));

		testStore.moveBulletin(b, folderA, folderA);
		assertEquals("still in a", true, folderA.contains(b));
		assertEquals("still not in b", false, folderB.contains(b));

		testStore.moveBulletin(b, folderB, folderB);
		assertEquals("still in a", true, folderA.contains(b));
		assertEquals("still not in b again", false, folderB.contains(b));
	}
	
	public void testSetIsOnServer() throws Exception
	{
		Bulletin b = testStore.createEmptyBulletin();
		testStore.saveBulletin(b);
		testStore.setIsOnServer(b);
		assertTrue("not in on?", testStore.isProbablyOnServer(b.getUniversalId()));
		assertFalse("in not on?", testStore.isProbablyNotOnServer(b.getUniversalId()));
		testStore.setIsOnServer(b);
		assertTrue("not still in on?", testStore.isProbablyOnServer(b.getUniversalId()));
		assertFalse("now in not on?", testStore.isProbablyNotOnServer(b.getUniversalId()));
		
		testStore.setIsNotOnServer(b);
		testStore.setIsOnServer(b);
		assertTrue("not again in on?", testStore.isProbablyOnServer(b.getUniversalId()));
		assertFalse("still in not on?", testStore.isProbablyNotOnServer(b.getUniversalId()));
	}

	public void testSetIsNotOnServer() throws Exception
	{
		Bulletin b = testStore.createEmptyBulletin();
		testStore.saveBulletin(b);
		testStore.setIsNotOnServer(b);
		assertTrue("not in not on?", testStore.isProbablyNotOnServer(b.getUniversalId()));
		assertFalse("in on?", testStore.isProbablyOnServer(b.getUniversalId()));
		testStore.setIsNotOnServer(b);
		assertTrue("not still in not on?", testStore.isProbablyNotOnServer(b.getUniversalId()));
		assertFalse("now in on?", testStore.isProbablyOnServer(b.getUniversalId()));
		
		testStore.setIsOnServer(b);
		testStore.setIsNotOnServer(b);
		assertTrue("not again in not on?", testStore.isProbablyNotOnServer(b.getUniversalId()));
		assertFalse("still in on?", testStore.isProbablyOnServer(b.getUniversalId()));
	}
	
	public void testClearnOnServerLists() throws Exception
	{
		Bulletin on = testStore.createEmptyBulletin();
		testStore.saveBulletin(on);
		testStore.setIsOnServer(on);
		
		Bulletin off = testStore.createEmptyBulletin();
		testStore.saveBulletin(off);
		testStore.setIsNotOnServer(off);
		
		testStore.clearOnServerLists();
		
		assertFalse("on still on?", testStore.isProbablyOnServer(on.getUniversalId()));
		assertFalse("on now off?", testStore.isProbablyNotOnServer(on.getUniversalId()));
		assertFalse("off now on?", testStore.isProbablyOnServer(off.getUniversalId()));
		assertFalse("off still off?", testStore.isProbablyNotOnServer(off.getUniversalId()));
	}
	
	public void testUpdateOnServerLists() throws Exception
	{
		Bulletin sentButNotOnServer = createAndSaveBulletin();
		testStore.setIsOnServer(sentButNotOnServer);
		
		Bulletin unknownAndNotOnServer = createAndSaveBulletin();
		
		Bulletin unsentToAndNotOnServer = createAndSaveBulletin();
		testStore.setIsNotOnServer(unsentToAndNotOnServer);
		
		HashSet onServer = new HashSet();

		Bulletin sentAndOnServer = createAndSaveBulletin();
		testStore.setIsOnServer(sentAndOnServer);
		onServer.add(BulletinSummary.createFromBulletin(sentAndOnServer));
		
		Bulletin unknownButOnServer = createAndSaveBulletin();
		onServer.add(BulletinSummary.createFromBulletin(unknownButOnServer));

		Bulletin unsentButOnServer = createAndSaveBulletin();
		testStore.setIsNotOnServer(unsentButOnServer);
		onServer.add(BulletinSummary.createFromBulletin(unsentButOnServer));
		
		BulletinFolder draftOutbox = testStore.getFolderDraftOutbox();
		Bulletin draftInOutboxSentAndOnServer = createAndSaveBulletin();
		testStore.setIsOnServer(draftInOutboxSentAndOnServer);
		onServer.add(BulletinSummary.createFromBulletin(draftInOutboxSentAndOnServer));
		testStore.ensureBulletinIsInFolder(draftOutbox, draftInOutboxSentAndOnServer.getUniversalId());
		
		Bulletin draftInOutboxUnknownButOnServer = createAndSaveBulletin();
		onServer.add(BulletinSummary.createFromBulletin(draftInOutboxUnknownButOnServer));
		testStore.ensureBulletinIsInFolder(draftOutbox, draftInOutboxUnknownButOnServer.getUniversalId());

		Bulletin draftInOutboxUnsentButOnServer = createAndSaveBulletin();
		testStore.setIsNotOnServer(draftInOutboxUnsentButOnServer);
		onServer.add(BulletinSummary.createFromBulletin(draftInOutboxUnsentButOnServer));
		testStore.ensureBulletinIsInFolder(draftOutbox, draftInOutboxUnsentButOnServer.getUniversalId());

		testStore.getFoldersFile().delete();
		assertFalse("already saved folders?", testStore.getFoldersFile().exists());
		
		testStore.updateOnServerLists(onServer);
		
		assertTrue("thought sent; not on server", testStore.isProbablyNotOnServer(sentButNotOnServer.getUniversalId()));
		assertTrue("unknown; is on server", testStore.isProbablyNotOnServer(unknownAndNotOnServer.getUniversalId()));
		assertTrue("thought unsent; not on server", testStore.isProbablyNotOnServer(unsentToAndNotOnServer.getUniversalId()));
		
		assertTrue("thought sent; is on server", testStore.isProbablyOnServer(sentAndOnServer.getUniversalId()));
		assertTrue("unknown; is on server", testStore.isProbablyOnServer(unknownButOnServer.getUniversalId()));
		assertTrue("thought unsent; is on server", testStore.isProbablyOnServer(unsentButOnServer.getUniversalId()));
		
		assertTrue("thought sent; in draft outbox; on server", testStore.isProbablyOnServer(draftInOutboxSentAndOnServer.getUniversalId()));
		assertFalse("(1) unknown; in draft outbox; on server", testStore.isProbablyOnServer(draftInOutboxUnknownButOnServer.getUniversalId()));
		assertFalse("(2) unknown; in draft outbox; on server", testStore.isProbablyNotOnServer(draftInOutboxUnknownButOnServer.getUniversalId()));
		assertTrue("thought unsent; in draft outbox; on server", testStore.isProbablyNotOnServer(draftInOutboxUnsentButOnServer.getUniversalId()));
		
		assertTrue("didn't save folders?", testStore.getFoldersFile().exists());
	}
	
	private Bulletin createAndSaveBulletin() throws Exception
	{
		Bulletin b = testStore.createEmptyBulletin();
		testStore.saveBulletin(b);
		return b;
	}

	public void testAddBulletinToFolder() throws Exception
	{
		TRACE("testAddBulletinToFolder");

		Bulletin b = testStore.createEmptyBulletin();
		testStore.saveBulletin(b);
		UniversalId id = b.getUniversalId();
		BulletinFolder folder = testStore.createFolder("test");
		testStore.addBulletinToFolder(folder, id);
		assertEquals("now in folder", true, folder.contains(b));
		try
		{
			testStore.addBulletinToFolder(folder, id);
			fail("should have thrown exists exception");
		}
		catch (BulletinAlreadyExistsException expectedException)
		{
		}
		assertEquals("still in folder", true, folder.contains(b));
		UniversalId bFakeId = UniversalIdForTesting.createFromAccountAndPrefix("aa", "abc");
		testStore.addBulletinToFolder(folder, bFakeId);
		UniversalId badId2 = UniversalIdForTesting.createDummyUniversalId();
		assertEquals("bad bulletin", -1, folder.find(badId2));

	}
	
	public void testAddBulletinToFolderRemovesAncestors() throws Exception
	{
		FieldSpecCollection publicFields = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection privateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		BulletinFolder aFolder = testStore.createFolder("blah");

		Bulletin original = testStore.createEmptyBulletin();
		original.setImmutable();
		testStore.saveBulletin(original);
		testStore.addBulletinToFolder(aFolder, original.getUniversalId());
		assertEquals(1, aFolder.getBulletinCount());

		Bulletin firstClone = testStore.createNewDraft(original, publicFields, privateFields);
		firstClone.setImmutable();
		testStore.saveBulletin(firstClone);
		
		Bulletin unrelated = testStore.createEmptyBulletin();
		testStore.saveBulletin(unrelated);
		testStore.addBulletinToFolder(aFolder, unrelated.getUniversalId());
		assertEquals(2, aFolder.getBulletinCount());
		

		testStore.addBulletinToFolder(aFolder, firstClone.getUniversalId());
		assertEquals(2, aFolder.getBulletinCount());
		assertTrue("lost unrelated (1)?", aFolder.contains(unrelated));
		assertTrue("didn't update to first clone?", aFolder.contains(firstClone));
		
		Bulletin lastClone = testStore.createNewDraft(firstClone, publicFields, privateFields);
		lastClone.setImmutable();
		testStore.saveBulletin(lastClone);
		BulletinFolder otherFolder = testStore.getFolderDiscarded();
		testStore.addBulletinToFolder(otherFolder, lastClone.getUniversalId());
		assertEquals(2, aFolder.getBulletinCount());
		assertTrue("lost unrelated (2)?", aFolder.contains(unrelated));
		assertTrue("didn't update to later clone?", aFolder.contains(lastClone));
	}
	
	class BulletinUidCollector implements Database.PacketVisitor
	{
		public void visit(DatabaseKey key)
		{
			uids.add(key.getUniversalId());
		}

		Vector uids = new Vector();
	}
	
	public void testAddOriginalBulletinToFolderWithNewerVersion() throws Exception
	{
		FieldSpecCollection publicFields = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection privateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		MockBulletinStore clientStore = new MockBulletinStore(security);
		Bulletin original = clientStore.createEmptyBulletin();
		original.setImmutable();

		Bulletin clone = clientStore.createNewDraft(original, publicFields, privateFields);
		clone.setImmutable();
		clientStore.saveBulletinForTesting(clone);
		BulletinUidCollector collector = new BulletinUidCollector();
		clientStore.visitAllBulletinRevisions(collector);
		assertEquals("should have 1 bulletin", 1, collector.uids.size());
		clientStore.saveBulletinForTesting(original);
		BulletinUidCollector collector2 = new BulletinUidCollector();
		clientStore.visitAllBulletinRevisions(collector2);
		assertEquals("should now have 2 bulletin", 2, collector2.uids.size());

		BulletinFolder aFolder = clientStore.createFolder("a");
		clientStore.addBulletinToFolder(aFolder, clone.getUniversalId());
		assertEquals("Should only have 1 bulletin in folder", 1, aFolder.getBulletinCount());
		try
		{
			clientStore.addBulletinToFolder(aFolder, original.getUniversalId());
			fail("Should have thrown here.");
		}
		catch(AddOlderVersionToFolderFailedException expected)
		{
		}
		assertEquals("Should still only have 1 bulletin in folder since there is a newer version", 1, aFolder.getBulletinCount());
		clientStore.deleteAllBulletins();
	}
	
	public void testAddingBulletinVersionThenOriginalToVisibleAndInvisibleFolders() throws Exception
	{
		FieldSpecCollection publicFields = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection privateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		MockBulletinStore clientStore = new MockBulletinStore(security);
		Bulletin original = clientStore.createEmptyBulletin();
		original.setImmutable();

		Bulletin newerVersion = clientStore.createNewDraft(original, publicFields, privateFields);
		newerVersion.setImmutable();
		clientStore.saveBulletinForTesting(newerVersion);
		clientStore.saveBulletinForTesting(original);

		BulletinFolder visibleFolderA = clientStore.createFolder("a");
		assertTrue("Should be a visibleFolder", visibleFolderA.isVisible());
		clientStore.addBulletinToFolder(visibleFolderA, newerVersion.getUniversalId());
		assertEquals("Should only have 1 bulletin in visible folder", 1, visibleFolderA.getBulletinCount());
		try
		{
			clientStore.addBulletinToFolder(visibleFolderA, original.getUniversalId());
			fail("Should have thrown an exception");
		}
		catch(AddOlderVersionToFolderFailedException expected)
		{
		}
		assertEquals("Should still only have 1 bulletin in visible folder since there is a newer version", 1, visibleFolderA.getBulletinCount());
		assertTrue("Should still have newer version only", visibleFolderA.contains(newerVersion));

		BulletinFolder invisibleFolderC = clientStore.createFolder("*c");
		assertFalse("Should be an invisibleFolder", invisibleFolderC.isVisible());
		clientStore.addBulletinToFolder(invisibleFolderC, newerVersion.getUniversalId());
		assertEquals("Should only have 1 bulletin in invisible folder", 1, invisibleFolderC.getBulletinCount());
		clientStore.addBulletinToFolder(invisibleFolderC, original.getUniversalId());
		assertEquals("Should now have 2 bulletin in invisible folder", 2, invisibleFolderC.getBulletinCount());

		clientStore.deleteAllBulletins();
	}

	public void testAddingBulletinOriginalThenNewVersionToVisibleAndInvisibleFolders() throws Exception
	{
		FieldSpecCollection publicFields = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection privateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		MockBulletinStore clientStore = new MockBulletinStore(security);
		Bulletin original = clientStore.createEmptyBulletin();
		original.setImmutable();

		Bulletin newVersion = clientStore.createNewDraft(original, publicFields, privateFields);
		newVersion.setImmutable();
		clientStore.saveBulletinForTesting(original);

		BulletinFolder visibleFolderA = clientStore.createFolder("a");
		BulletinFolder visibleFolderB = clientStore.createFolder("b");
		BulletinFolder invisibleFolderC = clientStore.createFolder("*c");

		clientStore.addBulletinToFolder(visibleFolderA, original.getUniversalId());
		clientStore.addBulletinToFolder(visibleFolderB, original.getUniversalId());
		clientStore.addBulletinToFolder(invisibleFolderC, original.getUniversalId());
		
		assertEquals("Should only have 1 bulletin in visible folder A", 1, visibleFolderA.getBulletinCount());
		assertEquals("Should only have 1 bulletin in visible folder B", 1, visibleFolderB.getBulletinCount());
		assertEquals("Should only have 1 bulletin in invisible folder C", 1, invisibleFolderC.getBulletinCount());
		
		clientStore.saveBulletinForTesting(newVersion);
		clientStore.addBulletinToFolder(visibleFolderA, newVersion.getUniversalId());
		assertTrue("visibleFolder A should contain the new version", visibleFolderA.contains(newVersion));
		assertTrue("visibleFolder B should contain the new version", visibleFolderB.contains(newVersion));
		assertFalse("invisibleFolder C Should not contain the new version", invisibleFolderC.contains(newVersion));
		assertFalse("visibleFolder A should not contain the original version", visibleFolderA.contains(original));
		assertFalse("visibleFolder B should not contain the original version", visibleFolderB.contains(original));
		assertTrue("invisibleFolder C Should contain the original version", invisibleFolderC.contains(original));

		clientStore.addBulletinToFolder(invisibleFolderC, newVersion.getUniversalId());
		assertTrue("invisibleFolder C Should still contain the original version", invisibleFolderC.contains(original));
		assertTrue("invisibleFolder C Should now also contain the new version", invisibleFolderC.contains(newVersion));
		
		clientStore.deleteAllBulletins();
	}

	public void testFolderToXml() throws Exception
	{
		TRACE("testFolderToXml");

		BulletinFolder folder = testStore.createFolder("Test");
		String xml = testStore.folderToXml(folder);
		assertEquals(MartusClientXml.getFolderTagStart(folder) + MartusClientXml.getFolderTagEnd(), xml);

		Bulletin b = testStore.createEmptyBulletin();
		testStore.saveBulletin(b);
		folder.add(b);
		xml = testStore.folderToXml(folder);
		assertStartsWith(MartusClientXml.getFolderTagStart(folder), xml);
		assertContains(MartusXml.getIdTag(folder.getBulletinSorted(0).getUniversalIdString()), xml);
		assertEndsWith(MartusClientXml.getFolderTagEnd(), xml);

	}

	public void testFoldersToXml() throws Exception
	
	{
		TRACE("testFoldersToXml");

		int i;
		String expected;

		expected = MartusClientXml.getFolderListTagStart();
		Vector originalFolderNames = testStore.getAllFolderNames();
		for(i = 0; i < originalFolderNames.size(); ++i)
		{
			BulletinFolder folder = testStore.findFolder((String)originalFolderNames.get(i));
			expected += testStore.folderToXml(folder);
		}
		expected += MartusClientXml.getFolderListTagEnd();
		assertEquals(expected, testStore.foldersToXml());

		BulletinFolder f1 = testStore.createFolder("First");
		Bulletin b = testStore.createEmptyBulletin();
		testStore.saveBulletin(b);
		f1.add(b);

		expected = MartusClientXml.getFolderListTagStart();
		Vector updatedFolderNames = testStore.getAllFolderNames();
		for(i = 0; i < updatedFolderNames.size(); ++i)
		{
			BulletinFolder folder = testStore.findFolder((String)updatedFolderNames.get(i));
			expected += testStore.folderToXml(folder);
		}
		expected += MartusClientXml.getFolderListTagEnd();
		assertEquals(expected, testStore.foldersToXml());
	}

	public void testLoadXmlNoFolders()
	{
		TRACE("testLoadXmlNoFolders");

		int count = testStore.getFolderCount();
		String xml = "<FolderList></FolderList>";
		testStore.internalLoadFolders(xml);
		assertEquals(0, testStore.getBulletinCount());
		assertEquals(count, testStore.getFolderCount());
		assertNull("found?", testStore.findFolder("fromxml"));
	}

	public void testLoadXmlFolders()
	{
		TRACE("testLoadXmlFolders");

		int count = testStore.getFolderCount();
		String xml = "<FolderList><Folder name='closed' closed='true'></Folder><Folder name='open' closed='false'></Folder><Folder name='noOpenCloseStatus'></Folder></FolderList>";
		testStore.internalLoadFolders(xml);
		assertEquals(count+3, testStore.getFolderCount());
		
		BulletinFolder noOpenCloseStatus = testStore.findFolder("noOpenCloseStatus");
		assertNotNull("Folder 'noOpenCloseStatus' must exist", noOpenCloseStatus);
		assertTrue("Folder not open?", noOpenCloseStatus.isOpen());

		BulletinFolder openFolder = testStore.findFolder("open");
		assertNotNull("Folder 'open' must exist", openFolder);
		assertTrue("Folder not open?", openFolder.isOpen());	
		
		BulletinFolder closedFolder = testStore.findFolder("closed");
		assertNotNull("Folder 'closed' must exist", closedFolder);
		assertTrue("Folder not closed?", closedFolder.isClosed());
				
		assertNull("Folder 'someNonExistentFolder' must not exist", testStore.findFolder("someNonExistentFolder"));
	}

	public void testLoadXmlLegacyFolders() throws Exception
	{
		TRACE("testLoadXmlFolders");

		int systemFolderCount = testStore.getFolderCount();

		ClientBulletinStore tempStore = new MockBulletinStore();
		String xml = "<FolderList><Folder name='Sent Bulletins'></Folder><Folder name='new two'></Folder></FolderList>";
		tempStore.internalLoadFolders(xml);
		assertTrue("Legacy folder not detected?", tempStore.needsLegacyFolderConversion());
		assertEquals(systemFolderCount + 1, tempStore.getFolderCount());
		assertNotNull("Folder %Sent must exist", tempStore.findFolder("%Sent"));
		assertNull("Folder Sent Bulletins must not exist", tempStore.findFolder("Sent Bulletins"));
		assertNotNull("Folder two new must exist", tempStore.findFolder("new two"));
		assertNull("Folder three must not exist", tempStore.findFolder("three"));
		xml = "<FolderList><Folder name='%Sent'></Folder><Folder name='new two'></Folder></FolderList>";
		tempStore.internalLoadFolders(xml);
		assertFalse("Not Legacy folder didn't return false on load", tempStore.needsLegacyFolderConversion());
		tempStore.deleteAllData();
	}

	/* missing tests:
		- invalid xml (empty, badly nested tags, two root nodes)
		- <Id> not nested within <Folder>
		- <Field> not nested within <Bulletin>
		- <Folder> or <Bulletin> outside <FolderList> or <BulletinList>
		- Missing folder name attribute, bulletin id attribute, field name attribute
		- Empty bulletin id
		- Illegal bulletin id
		- Duplicate bulletin id
		- Folder id that is blank or isn't a bulletin
		- Folder name blank or duplicate
		- Bulletin field name isn't one of our predefined field names
		- Confirm that attributes are case-sensitive
	*/

	public void testDatabaseBulletins() throws Exception
	{
		TRACE("testDatabaseBulletins");

		assertEquals("empty", 0, testStore.getBulletinCount());

		Bulletin b = testStore.createEmptyBulletin();
		final String author = "Mr. Peabody";
		b.set(Bulletin.TAGAUTHOR, author);
		testStore.saveBulletin(b);
		testStore.saveFolders();
		assertEquals("saving", 1, testStore.getBulletinCount());
		assertEquals("keys", 3*testStore.getBulletinCount(), db.getRecordCount());

		ClientBulletinStore newStoreSameDatabase = new MockBulletinStore(db, testStore.getSignatureGenerator());
		newStoreSameDatabase.loadFolders();
		assertEquals("loaded", 1, newStoreSameDatabase.getBulletinCount());
		Bulletin b2 = newStoreSameDatabase.getBulletinRevision(b.getUniversalId());
		assertEquals("id", b.getLocalId(), b2.getLocalId());
		assertEquals("author", b.get(Bulletin.TAGAUTHOR), b2.get(Bulletin.TAGAUTHOR));
		assertEquals("wrong security?", testStore.getSignatureGenerator(), b2.getSignatureGenerator());
		newStoreSameDatabase.deleteAllData();
	}

	public void testDatabaseFolders() throws Exception
	{
		TRACE("testDatabaseFolders");

		final String folderName = "Gotta work";
		int systemFolderCount = testStore.getFolderCount();
		BulletinFolder f = testStore.createFolder(folderName);
		Bulletin b = testStore.createEmptyBulletin();
		testStore.saveBulletin(b);
		f.add(b);
		testStore.saveFolders();

		assertEquals("keys", 3*testStore.getBulletinCount(), db.getRecordCount());

		File storeRootDir = testStore.getStoreRootDir();
		ClientBulletinStore store = new ClientBulletinStore(security);
		store.doAfterSigninInitialization(storeRootDir, db);
		store.createFieldSpecCacheFromDatabase();
		assertEquals("before load", systemFolderCount, store.getFolderCount());
		store.loadFolders();
		assertEquals("loaded", 1+systemFolderCount, store.getFolderCount());
		BulletinFolder f2 = store.findFolder(folderName);
		assertNotNull("folder", f2);
		assertEquals("bulletins in folder", 1, f2.getBulletinCount());
		assertEquals("contains", true, f2.contains(b));
		store.deleteAllData();
	}

	public void testLoadAllDataWithErrors() throws Exception
	{
		TRACE("testLoadAllDataWithErrors");
		Bulletin b = testStore.createEmptyBulletin();
		testStore.saveBulletin(b);
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
		FieldDataPacket fdp = b.getFieldDataPacket();
		DatabaseKey headerKey = DatabaseKey.createLegacyKey(b.getUniversalId());
		UniversalId.createFromAccountAndLocalId(b.getAccount(), fdp.getLocalId());

		security.fakeSigVerifyFailure = true;
		testStore.loadFolders();

		security.fakeSigVerifyFailure = false;
		testStore.loadFolders();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		byte[] bytes = out.toByteArray();

		bytes[0] = '!';
		String invalidPacketString = new String(bytes, "UTF-8");
		db.writeRecord(headerKey, invalidPacketString);
		testStore.loadFolders();
	}

	public void testClearFolder() throws Exception
	{
		TRACE("testClearFolder");
		Bulletin b1 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b1);
		Bulletin b2 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b2);
		BulletinFolder folder = testStore.createFolder("blah");
		folder.add(b1);
		folder.add(b2);
		assertEquals(testStore.getBulletinCount(), folder.getBulletinCount());
		testStore.clearFolder("blah");
		assertEquals(0, folder.getBulletinCount());
	}

	public void testSave() throws Exception
	{
		TRACE("testSave");
		//TODO: This was moved in from TestBulletin, and it may
		//not be needed--compare with testSaveBulletin
		int oldCount = testStore.getBulletinCount();
		Bulletin b = testStore.createEmptyBulletin();
		b.set("author", "testsave");
		testStore.saveBulletin(b);
		assertEquals(oldCount+1, testStore.getBulletinCount());
		b = testStore.getBulletinRevision(b.getUniversalId());
		assertEquals("testsave", b.get("author"));
		boolean empty = (b.getLocalId().length() == 0);
		assertEquals("Saved ID must be non-empty\n", false, empty);

		Bulletin b2 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b2);
		assertNotEquals("Saved ID must be unique\n", b.getLocalId(), b2.getLocalId());

		testStore.saveBulletin(b2);
	}

	public void testLastSavedTime() throws Exception
	{
		TRACE("testLastSavedTime");
		Bulletin b = testStore.createEmptyBulletin();
		long createdTime = b.getLastSavedTime();
		assertEquals("time already set?", BulletinHeaderPacket.TIME_UNKNOWN, createdTime);

		Thread.sleep(200);
		testStore.saveBulletin(b);
		long firstSavedTime = b.getLastSavedTime();
		assertNotEquals("Didn't update time saved?", createdTime, firstSavedTime);
		long delta2 = Math.abs(firstSavedTime - System.currentTimeMillis());
		assertTrue("time wrong?", delta2 < 1000);

		Thread.sleep(200);
		Bulletin b2 = testStore.loadFromDatabase(DatabaseKey.createLegacyKey(b.getUniversalId()));
		long loadedTime = b2.getLastSavedTime();
		assertEquals("Didn't keep time saved?", firstSavedTime, loadedTime);
	}

	public void testClearFolderCausesSave() throws Exception
	{
		TRACE("testClearFolderCausesSave");

		testStore.deleteAllData();
		Bulletin b = testStore.createEmptyBulletin();
		BulletinFolder f = testStore.getFolderSaved();
		testStore.addBulletinToFolder(f, b.getUniversalId());

		testStore.deleteAllData();
		testStore.clearFolder(f.getName());
		assertTrue("clearFolder f ", testStore.getFoldersFile().exists());
		DatabaseKey bulletinKey = DatabaseKey.createLegacyKey(b.getUniversalId());
		assertNull("clearFolder b ", testStore.getDatabase().readRecord(bulletinKey, security));

		testStore.saveBulletin(b);
		testStore.destroyBulletin(b);
		assertNull("destroyBulletin b ", testStore.getDatabase().readRecord(bulletinKey, security));
	}

	public void testDeleteFolderCausesSave() throws Exception, IOException, CryptoException
	{
		TRACE("testDeleteFolderCausesSave");
		
		DatabaseKey foldersKey = DatabaseKey.createLegacyKey(UniversalIdForTesting.createDummyUniversalId());
		testStore.deleteAllData();
		Bulletin b = testStore.createEmptyBulletin();
		testStore.createFolder("z");
		db.discardRecord(foldersKey);
		testStore.deleteFolder("z");
		assertTrue("deleteFolder f ", testStore.getFoldersFile().exists());
		DatabaseKey bulletinKey = DatabaseKey.createLegacyKey(b.getUniversalId());
		assertNull("deleteFolder b ", testStore.getDatabase().readRecord(bulletinKey, security));
	}

	public void testRenameFolderCausesSave() throws Exception, IOException, CryptoException
	{
		TRACE("testRenameFolderCausesSave");

		DatabaseKey foldersKey = DatabaseKey.createLegacyKey(UniversalIdForTesting.createDummyUniversalId());
		testStore.deleteAllData();
		Bulletin b = testStore.createEmptyBulletin();
		testStore.createFolder("x");
		db.discardRecord(foldersKey);
		testStore.renameFolder("x", "b");
		assertTrue("renameFolder f ", testStore.getFoldersFile().exists());
		DatabaseKey bulletinKey = DatabaseKey.createLegacyKey(b.getUniversalId());
		assertNull("renameFolder b ", testStore.getDatabase().readRecord(bulletinKey, security));
	}

	public void testSaveFoldersDoesNotSaveBulletins() throws Exception, IOException, CryptoException
	{
		TRACE("testSaveFoldersDoesNotSaveBulletins");

		testStore.deleteAllData();
		Bulletin b = testStore.createEmptyBulletin();
		testStore.createFolder("a");
		testStore.saveFolders();
		assertTrue("createFolder f ", testStore.getFoldersFile().exists());
		DatabaseKey bulletinKey = DatabaseKey.createLegacyKey(b.getUniversalId());
		assertNull("createFolder b ", testStore.getDatabase().readRecord(bulletinKey, security));
	}

	public void testSaveBulletinDoesNotSaveFolders() throws Exception, IOException, CryptoException
	{
		TRACE("testSaveBulletinDoesNotSaveFolders");

		testStore.deleteAllData();
		Bulletin b = testStore.createEmptyBulletin();
		testStore.saveBulletin(b);

		assertEquals("save bulletin f ", false, testStore.getFoldersFile().exists());

		DatabaseKey bulletinKey = DatabaseKey.createLegacyKey(b.getUniversalId());
		assertNotNull("save bulletin b ", testStore.getDatabase().readRecord(bulletinKey, security));
	}

	public void testImportZipFileWithAttachmentSealed() throws Exception
	{
		TRACE("testImportZipFileWithAttachmentSealed");
		
		Bulletin original = testStore.createEmptyBulletin();
		DatabaseKey originalKey = DatabaseKey.createLegacyKey(original.getUniversalId());
		AttachmentProxy a = new AttachmentProxy(tempFile1);
		AttachmentProxy aPrivate = new AttachmentProxy(tempFile2);
		original.set(Bulletin.TAGTITLE, "abbc");
		original.set(Bulletin.TAGPRIVATEINFO, "priv");
		original.addPublicAttachment(a);
		original.addPrivateAttachment(aPrivate);
		original.setImmutable();
		testStore.saveBulletinForTesting(original);
		File zipFile = createTempFileFromName("$$$MartusTestZipSealed");
		Bulletin loaded = testStore.loadFromDatabase(originalKey);
		BulletinForTesting.saveToFile(db,loaded, zipFile, testStore.getSignatureVerifier());
		testStore.deleteAllData();
		assertEquals("still a record?", 0, db.getRecordCount());

		testStore.importZipFileToStoreWithSameUids(zipFile);
		assertEquals("Packet count incorrect", 5, db.getRecordCount());

		DatabaseKey headerKey = DatabaseKey.createLegacyKey(loaded.getBulletinHeaderPacket().getUniversalId());
		DatabaseKey dataKey = DatabaseKey.createLegacyKey(loaded.getFieldDataPacket().getUniversalId());
		DatabaseKey privateKey = DatabaseKey.createLegacyKey(loaded.getPrivateFieldDataPacket().getUniversalId());
		AttachmentProxy gotAttachment = loaded.getPublicAttachments()[0];
		DatabaseKey attachmentKey = DatabaseKey.createLegacyKey(gotAttachment.getUniversalId());
		AttachmentProxy gotPrivateAttachment = loaded.getPrivateAttachments()[0];
		DatabaseKey attachmentPrivateKey = DatabaseKey.createLegacyKey(gotPrivateAttachment.getUniversalId());

		assertTrue("Header Packet missing", db.doesRecordExist(headerKey));
		assertTrue("Data Packet missing", db.doesRecordExist(dataKey));
		assertTrue("Private Packet missing", db.doesRecordExist(privateKey));
		assertTrue("Attachment Packet missing", db.doesRecordExist(attachmentKey));
		assertTrue("Attachment Private Packet missing", db.doesRecordExist(attachmentPrivateKey));

		Bulletin reloaded = testStore.loadFromDatabase(originalKey);
		assertEquals("public?", original.get(Bulletin.TAGTITLE), reloaded.get(Bulletin.TAGTITLE));
		assertEquals("private?", original.get(Bulletin.TAGPRIVATEINFO), reloaded.get(Bulletin.TAGPRIVATEINFO));

		File tempRawFilePublic = createTempFileFromName("$$$MartusTestImpSealedZipRawPublic");
		BulletinLoader.extractAttachmentToFile(db, reloaded.getPublicAttachments()[0], security, tempRawFilePublic);
		byte[] rawBytesPublic = new byte[sampleBytes1.length];
		FileInputStream in = new FileInputStream(tempRawFilePublic);
		in.read(rawBytesPublic);
		in.close();
		assertEquals("wrong bytes", true, Arrays.equals(sampleBytes1, rawBytesPublic));

		File tempRawFilePrivate = createTempFileFromName("$$$MartusTestImpSealedZipRawPrivate");
		BulletinLoader.extractAttachmentToFile(db, reloaded.getPrivateAttachments()[0], security, tempRawFilePrivate);
		byte[] rawBytesPrivate = new byte[sampleBytes2.length];
		FileInputStream in2 = new FileInputStream(tempRawFilePrivate);
		in2.read(rawBytesPrivate);
		in2.close();
		assertEquals("wrong Private bytes", true, Arrays.equals(sampleBytes2, rawBytesPrivate));

		zipFile.delete();
		tempRawFilePublic.delete();
		tempRawFilePrivate.delete();
	}

	public void testImportZipFileBulletin() throws Exception
	{
		TRACE("testImportZipFileBulletin");
		File tempFile = createTempFile();

		Bulletin b = testStore.createEmptyBulletin();
		BulletinForTesting.saveToFile(db,b, tempFile, testStore.getSignatureVerifier());

		BulletinFolder folder = testStore.createFolder("test");

		testStore.importZipFileBulletin(tempFile, folder, false);
		assertEquals("not imported to store?", 1, testStore.getBulletinCount());
		assertEquals("not imported to folder?", 1, folder.getBulletinCount());
		assertNull("resaved with draft id?", testStore.getBulletinRevision(b.getUniversalId()));

		testStore.deleteAllData();
		folder = testStore.createFolder("test2");

		b.setImmutable();
		BulletinForTesting.saveToFile(db,b, tempFile, testStore.getSignatureVerifier());
		testStore.importZipFileBulletin(tempFile, folder, false);
		assertEquals("not imported to store?", 1, testStore.getBulletinCount());
		assertEquals("not imported to folder count?", 1, folder.getBulletinCount());
		assertEquals("not imported to folder uid?", 0, folder.find(b.getUniversalId()));
		assertNotNull("not saved with sealed id?", testStore.getBulletinRevision(b.getUniversalId()));

		BulletinFolder folder2 = testStore.createFolder("another");
		testStore.importZipFileBulletin(tempFile, folder2, false);
		assertEquals("imported to store again?", 1, testStore.getBulletinCount());
		assertEquals("not imported to another folder uid?", 0, folder2.find(b.getUniversalId()));
	}

	public void testImportZipFileBulletinNotMine() throws Exception
	{
		TRACE("testImportZipFileBulletinNotMine");
		File tempFile = createTempFile();

		Bulletin original = testStore.createEmptyBulletin();
		BulletinForTesting.saveToFile(db,original, tempFile, testStore.getSignatureVerifier());

		ClientBulletinStore importer = createTempStore();
		BulletinFolder folder = importer.createFolder("test");
		importer.importZipFileBulletin(tempFile, folder, false);

		Bulletin imported = folder.getBulletinSorted(0);
		assertEquals("changed uid?", original.getUniversalId(), imported.getUniversalId());
		importer.deleteAllData();
	}

	public void testImportZipFileFieldOffice() throws Exception
	{
		TRACE("testImportZipFileFieldOffice");
		File tempFile = createTempFile();

		ClientBulletinStore hqStore = createTempStore();

		Bulletin original = testStore.createEmptyBulletin();
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(hqStore.getAccountId());
		keys.add(key1);
		original.setAuthorizedToReadKeys(keys);
		original.setImmutable();
		BulletinForTesting.saveToFile(db,original, tempFile, testStore.getSignatureVerifier());

		BulletinFolder folder = hqStore.createFolder("test");
		hqStore.importZipFileBulletin(tempFile, folder, false);

		Bulletin imported = folder.getBulletinSorted(0);
		assertEquals("changed uid?", original.getUniversalId(), imported.getUniversalId());
		hqStore.deleteAllData();
	}

	public void testImportDraftZipFile() throws Exception
	{
		TRACE("testImportDraftZipFile");
		File tempFile = createTempFile();

		Bulletin b = testStore.createEmptyBulletin();
		BulletinForTesting.saveToFile(db,b, tempFile, testStore.getSignatureVerifier());
		UniversalId originalUid = b.getUniversalId();

		BulletinFolder folder = testStore.createFolder("test");
		testStore.importZipFileBulletin(tempFile, folder, true);
		assertEquals("Didn't fully import?", 1, testStore.getBulletinCount());
		assertNotNull("Not same ID?", testStore.getBulletinRevision(originalUid));

		testStore.importZipFileBulletin(tempFile, folder, false);
		assertEquals("Not different IDs?", 2, testStore.getBulletinCount());

	}

	public void testImportRecordZipFile() throws Exception
	{
		TRACE("testImportRecordZipFile");
		File tempFile = createTempFile();

		Bulletin b = testStore.createEmptyBulletin(Bulletin.BulletinType.RECORD);
		assertEquals(b.getBulletinHeaderPacket().getBulletinType(), Bulletin.BulletinType.RECORD);
		BulletinForTesting.saveToFile(db,b, tempFile, testStore.getSignatureVerifier());
		UniversalId originalUid = b.getUniversalId();

		BulletinFolder folder = testStore.createFolder("test");
		testStore.importZipFileBulletin(tempFile, folder, true);
		assertEquals("Didn't fully import?", 1, testStore.getBulletinCount());
		assertNotNull("Not same ID?", testStore.getBulletinRevision(originalUid));

		testStore.importZipFileBulletin(tempFile, folder, false);
		assertEquals("Not different IDs?", 2, testStore.getBulletinCount());
		Bulletin retrievedBulletin = testStore.getBulletinRevision(originalUid);
		assertEquals("not of type RECORD?", retrievedBulletin.getBulletinHeaderPacket().getBulletinType(), Bulletin.BulletinType.RECORD);

	}

	public void testImportZipFileWithAttachmentDraft() throws Exception
	{
		TRACE("testImportZipFileWithAttachmentDraft");

		ReusableChoices choices = new ReusableChoices("choicescode", "Choices Label");
		String aLabel = "Fabulous A";
		choices.add(new ChoiceItem("a", aLabel));
		String bLabel = "Excellent B";
		choices.add(new ChoiceItem("b", bLabel));
		MockMartusApp app = MockMartusApp.create(getName());
		FieldSpecCollection defaultSpecs = app.getStore().getTopSectionFieldSpecs();
		FieldSpecCollection specs = new FieldSpecCollection();
		for(int i = 0; i < defaultSpecs.size(); ++i)
			specs.add(defaultSpecs.get(i));
		specs.addReusableChoiceList(choices);
		CustomDropDownFieldSpec dropdown = new CustomDropDownFieldSpec();
		String customDropdownTag = "DropDownTag";
		dropdown.setTag(customDropdownTag);
		dropdown.setLabel("Dropdown");
		dropdown.addReusableChoicesCode(choices.getCode());
		specs.add(dropdown);
		
		Bulletin original = testStore.createEmptyBulletin(specs, StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		DatabaseKey originalKey = DatabaseKey.createLegacyKey(original.getUniversalId());
		AttachmentProxy a = new AttachmentProxy(tempFile1);
		AttachmentProxy aPrivate = new AttachmentProxy(tempFile2);
		original.set(Bulletin.TAGTITLE, "abc");
		original.set(Bulletin.TAGPRIVATEINFO, "private");
		original.set(customDropdownTag, bLabel);
		original.addPublicAttachment(a);
		original.addPrivateAttachment(aPrivate);
		testStore.saveBulletin(original);

		Bulletin loaded = testStore.loadFromDatabase(originalKey);

		File zipFile = createTempFileFromName("$$$MartusTestZipDraft");
		BulletinForTesting.saveToFile(db,loaded, zipFile, testStore.getSignatureVerifier());

		testStore.deleteAllData();
		assertEquals("still a record?", 0, db.getRecordCount());

		UniversalId savedAsId = testStore.importZipFileToStoreWithNewUids(zipFile);
		assertEquals("record count not 5?", 5, db.getRecordCount());

		DatabaseKey headerKey = DatabaseKey.createLegacyKey(loaded.getBulletinHeaderPacket().getUniversalId());
		DatabaseKey dataKey = DatabaseKey.createLegacyKey(loaded.getFieldDataPacket().getUniversalId());
		DatabaseKey privateKey = DatabaseKey.createLegacyKey(loaded.getPrivateFieldDataPacket().getUniversalId());
		AttachmentProxy gotAttachment = loaded.getPublicAttachments()[0];
		AttachmentProxy gotAttachmentPrivate = loaded.getPrivateAttachments()[0];
		DatabaseKey attachmentKey = DatabaseKey.createLegacyKey(gotAttachment.getUniversalId());
		DatabaseKey attachmentPrivateKey = DatabaseKey.createLegacyKey(gotAttachmentPrivate.getUniversalId());

		assertEquals("Header Packet present?", false, db.doesRecordExist(headerKey));
		assertEquals("Data Packet present?", false, db.doesRecordExist(dataKey));
		assertEquals("Private Packet present?", false, db.doesRecordExist(privateKey));
		assertEquals("Attachment Public Packet present?", false, db.doesRecordExist(attachmentKey));
		assertEquals("Attachment Private Packet present?", false, db.doesRecordExist(attachmentPrivateKey));
		assertEquals("custom field missing before load?", bLabel, original.get(customDropdownTag));

		Bulletin reloaded = testStore.loadFromDatabase(DatabaseKey.createLegacyKey(savedAsId));

		assertEquals("public?", original.get(Bulletin.TAGTITLE), reloaded.get(Bulletin.TAGTITLE));
		assertEquals("private?", original.get(Bulletin.TAGPRIVATEINFO), reloaded.get(Bulletin.TAGPRIVATEINFO));
		assertEquals("attachment", true, db.doesRecordExist(DatabaseKey.createLegacyKey(reloaded.getPublicAttachments()[0].getUniversalId())));
		assertEquals("attachment Private", true, db.doesRecordExist(DatabaseKey.createLegacyKey(reloaded.getPrivateAttachments()[0].getUniversalId())));
		assertEquals("custom field missing?", original.get(customDropdownTag), reloaded.get(customDropdownTag));

		ByteArrayOutputStream publicStream = new ByteArrayOutputStream();
		BulletinLoader.extractAttachmentToStream(db, reloaded.getPublicAttachments()[0], security, publicStream);
		byte[] rawBytes = publicStream.toByteArray();
		assertEquals("wrong bytes Public", true, Arrays.equals(sampleBytes1,rawBytes));

		ByteArrayOutputStream privateStream = new ByteArrayOutputStream();
		BulletinLoader.extractAttachmentToStream(db, reloaded.getPrivateAttachments()[0], security, privateStream);
		byte[] rawBytesPrivate = privateStream.toByteArray();
		assertEquals("wrong bytes Private", true, Arrays.equals(sampleBytes2, rawBytesPrivate));

		zipFile.delete();
	}

	public void testGetSetOfBulletinUniversalIdsInFolders() throws Exception
	{
		TRACE("testGetSetOfBulletinUniversalIdsInFolders");
		Set emptySet = testStore.getSetOfBulletinUniversalIdsInFolders();
		assertTrue("not empty to start?", emptySet.isEmpty());

		Bulletin b1 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b1);
		Bulletin b2 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b2);
		Set stillEmptySet = testStore.getSetOfBulletinUniversalIdsInFolders();
		assertTrue("not still empty", stillEmptySet.isEmpty());

		testStore.getFolderSaved().add(b1);
		testStore.getFolderDiscarded().add(b1);
		testStore.getFolderDiscarded().add(b2);
		Set two = testStore.getSetOfBulletinUniversalIdsInFolders();

		assertEquals("not two?", 2, two.size());
		assertTrue("Missing b1?", two.contains(b1.getUniversalId()));
		assertTrue("Missing b2?", two.contains(b2.getUniversalId()));
		
		Bulletin b3 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b3);
		testStore.getFolderImport().add(b3);
		Set three = testStore.getSetOfBulletinUniversalIdsInFolders();

		assertEquals("not two?", 3, three.size());
		assertTrue("Missing b1?", three.contains(b1.getUniversalId()));
		assertTrue("Missing b2?", three.contains(b2.getUniversalId()));
		assertTrue("Missing b3?", three.contains(b3.getUniversalId()));
		
	}

	public void testGetSetOfOrphanedBulletinUniversalIds() throws Exception
	{
		TRACE("testGetSetOfOrphanedBulletinUniversalIds");
		Set emptySet = testStore.getSetOfOrphanedBulletinUniversalIds();
		assertTrue("not empty to start?", emptySet.isEmpty());

		Bulletin b1 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b1);
		Bulletin b2 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b2);

		Set two = testStore.getSetOfOrphanedBulletinUniversalIds();
		assertEquals("not two?", 2, two.size());
		assertTrue("two Missing b1?", two.contains(b1.getUniversalId()));
		assertTrue("two Missing b2?", two.contains(b2.getUniversalId()));

		testStore.getFolderSaved().add(b1);
		testStore.getFolderDiscarded().add(b1);
		Set one = testStore.getSetOfOrphanedBulletinUniversalIds();
		assertEquals("not one?", 1, one.size());
		assertTrue("one Missing b2?", one.contains(b2.getUniversalId()));

		testStore.getFolderDraftOutbox().add(b2);
		one = testStore.getSetOfOrphanedBulletinUniversalIds();
		assertEquals("A bulletin only existing in a hidden folder is orphaned", 1,  one.size());

		testStore.getFolderSaved().add(b2);
		Set empty = testStore.getSetOfOrphanedBulletinUniversalIds();
		assertTrue("now b2 is in a visable folder so we should not have any orphans", empty.isEmpty());
	}

	public void testOrphansInHiddenFolders() throws Exception
	{
		TRACE("testOrphansInHiddenFolders");
		Bulletin b1 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b1);
		Bulletin b2 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b2);

		testStore.getFolderDraftOutbox().add(b1);
		assertEquals("hidden-only not an orphan?", true, testStore.isOrphan(b1.getUniversalId()));

		testStore.getFolderSaved().add(b2);
		testStore.getFolderDraftOutbox().add(b2);
		assertEquals("hidden-plus is an orphan?", false, testStore.isOrphan(b2.getUniversalId()));
	}

	public void testOrphansInVisibleFolders() throws Exception
	{
		TRACE("testOrphansInVisibleFolders");
		Bulletin b1 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b1);

		assertEquals("Not in any folder, bulletin not orphaned?", true, testStore.isOrphan(b1.getUniversalId()));
		testStore.getFolderSaved().add(b1);
		assertEquals("In a visible folder, bulletin is orphaned?", false, testStore.isOrphan(b1.getUniversalId()));
	}
	
	
	public void testQuarantineUnreadableBulletinsSimple() throws Exception
	{
		TRACE("testQuarantineUnreadableBulletinsSimple");
		assertEquals("found a bad bulletin in an empty database?", 0, testStore.quarantineUnreadableBulletins());
		Bulletin b1 = testStore.createEmptyBulletin();
		testStore.saveBulletin(b1);
		assertEquals("not one leaf?", 1, testStore.getBulletinCount());
		assertEquals("quarantined a good record?", 0, testStore.quarantineUnreadableBulletins());
		corruptBulletinHeader(b1);
		assertEquals("didn't claim to quarantine 1 record?", 1, testStore.quarantineUnreadableBulletins());
		DatabaseKey key = DatabaseKey.createLegacyKey(b1.getUniversalId());
		assertTrue("didn't actually quarantine our record?", testStore.getDatabase().isInQuarantine(key));
		assertEquals("didn't update leaf?", 0, testStore.getBulletinCount());
	}

	public void testQuarantineUnreadableBulletinsMany() throws Exception
	{
		TRACE("testQuarantineUnreadableBulletinsMany");
		final int totalCount = 20;
		Bulletin bulletins[] = new Bulletin[totalCount];
		for (int i = 0; i < bulletins.length; i++)
		{
			bulletins[i] = testStore.createEmptyBulletin();
			testStore.saveBulletin(bulletins[i]);
		}

		final int badCount = 4;
		DatabaseKey badKeys[] = new DatabaseKey[badCount];
		for (int i = 0; i < badKeys.length; i++)
		{
			int bulletinIndex = i * (totalCount/badCount);
			Bulletin b = bulletins[bulletinIndex];
			badKeys[i] = DatabaseKey.createLegacyKey(b.getUniversalId());
			corruptBulletinHeader(b);
		}

		assertEquals("wrong quarantine count?", badCount, testStore.quarantineUnreadableBulletins());
		for (int i = 0; i < badKeys.length; i++)
			assertTrue("didn't quarantine " + i, testStore.getDatabase().isInQuarantine(badKeys[i]));
	}

	private void corruptBulletinHeader(Bulletin b) throws Exception
	{
		UniversalId uid = b.getUniversalId();
		DatabaseKey key = DatabaseKey.createLegacyKey(uid);
		String goodData = db.readRecord(key, security);
		String badData = "x" + goodData;
		db.writeRecord(key, badData);
	}

	private ClientBulletinStore createTempStore() throws Exception
	{
		MockMartusSecurity tempSecurity = MockMartusSecurity.createOtherClient();
		return new MockBulletinStore(tempSecurity);
	}
	
	public void testScrubAllData() throws Exception
	{		
		TRACE("testScrubAllData");
		
		Bulletin b = testStore.createEmptyBulletin();
		testStore.saveBulletin(b);
		
		Vector one = testStore.getUidsOfAllBulletinRevisions();
		assertEquals("not one?", 1, one.size());		
		
		testStore.scrubAllData();
		Vector empty = testStore.getUidsOfAllBulletinRevisions();
		assertEquals("not empty?", 0, empty.size());			
	}	
	
	public void testFxBulletinWithXFormsEditing() throws Exception
	{
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(TestBulletinFromXFormsLoaderConstants.XFORMS_MODEL_INTERGER_FIELD);
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(TestBulletinFromXFormsLoaderConstants.XFORMS_INSTANCE_INTERGER_FIELD);

		MiniLocalization localization = new MiniLocalization();
		FxBulletin fxBulletin = new FxBulletin(localization);
		fxBulletin.copyDataFromBulletin(bulletin, testStore);

		final String XFORMS_AGE_TAG = "age";
		FieldSpec fieldSpec = fxBulletin.findFieldSpecByTag(XFORMS_AGE_TAG);
		assertTrue("Only field should be string?", fieldSpec.getType().isString());
		assertEquals("Incorrect field label?", TestBulletinFromXFormsLoaderConstants.AGE_LABEL, fieldSpec.getLabel());
		assertEquals("Incorrect field tag?", XFORMS_AGE_TAG, fieldSpec.getTag());
		FxBulletinField field = fxBulletin.getField(fieldSpec);
		assertEquals("Incorrect field value?", TestBulletinFromXFormsLoaderConstants.AGE_VALUE, field.getValue());

		final String newAge = "45";
		field.setValue(newAge);
		final String newTitle = "Some New Title";
		fxBulletin.getField(Bulletin.TAGTITLE).setValue(newTitle);
		
		fxBulletin.copyDataToBulletin(bulletin);
		assertFalse("xForms model/instance still exists?", bulletin.containsXFormsData());
		assertEquals("Title didn't update?", newTitle, bulletin.get(Bulletin.TAGTITLE));
		assertEquals("xForms Field wasn't updated?", newAge, bulletin.get(XFORMS_AGE_TAG));
		
		testStore.saveBulletin(bulletin);
		Bulletin loaded = testStore.loadFromDatabase(bulletin.getDatabaseKey());
		assertEquals("Title wasn't saved?", newTitle, loaded.get(Bulletin.TAGTITLE));
		assertEquals("xForms Field wasn't saved?", newAge, loaded.get(XFORMS_AGE_TAG));
		
	}
	
	private static MockBulletinStore testStore;
	private static MockMartusSecurity security;
	private static MockDatabase db;
	private static FieldSpecCollection customPublicSpecs;
	private static FieldSpecCollection customPrivateSpecs;

	private static File tempFile1;
	private static File tempFile2;
	private static final String PUBLIC_DATA = "oeiwjfio";
	private static final String PRIVATE_DATA = "test private";
	private static final byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
	private static final byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};
	private static final String fakeHqKey = "wwwllkjsfdkjf";
	private static final String ATTACHMENT_1_DATA = "Attachment 1's Data";
	private static final String ATTACHMENT_1_EXTENSION = ".txt";

}
