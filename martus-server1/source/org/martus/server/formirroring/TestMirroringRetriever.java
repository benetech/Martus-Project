/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2007, Beneficent
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

package org.martus.server.formirroring;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.martus.common.FieldCollection;
import org.martus.common.LoggerInterface;
import org.martus.common.LoggerToNull;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.BulletinUploadRecord;
import org.martus.common.database.Database;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.DeleteRequestRecord;
import org.martus.common.database.MockDatabase;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.fieldspec.CustomFieldTemplate;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.mirroring.CallerSideMirroringGateway;
import org.martus.common.network.mirroring.CallerSideMirroringInterface;
import org.martus.common.network.mirroring.MirroringInterface;
import org.martus.common.network.mirroring.PassThroughMirroringGateway;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.MockBulletinStore;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.ServerForClients;
import org.martus.server.main.ServerBulletinStore;
import org.martus.util.DirectoryUtils;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


public class TestMirroringRetriever extends TestCaseEnhanced
{
	public TestMirroringRetriever(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		server = new MockMartusServer();
		MartusCrypto security = server.getSecurity();
		
		supplier = new FakeServerSupplier();
		supplier.authorizedCaller = security.getPublicKeyString();

		realHandler = new SupplierSideMirroringHandler(supplier, security);
		wrappedHandler = new PassThroughMirroringGateway(realHandler);
		realGateway = new CallerSideMirroringGateway(wrappedHandler);
		LoggerInterface logger = new LoggerToNull();
		realRetriever = new MirroringRetriever(server.getStore(), realGateway, "Dummy IP", logger);
		
	}
	
	public void tearDown() throws Exception
	{
		super.tearDown();
		server.deleteAllFiles();
	}
	
	public void testPullAllTemplates() throws Exception
	{
		String accountId1 = "123";
		String accountId2 = "ABC";
		
		CustomFieldTemplate cft1 = createTemplate("1");
		TemplateInfoForMirroring template1 = extractTemplateInfo(cft1);
		CustomFieldTemplate cft2a = createTemplate("2a");
		TemplateInfoForMirroring template2a = extractTemplateInfo(cft2a);
		CustomFieldTemplate cft2b = createTemplate("2b");
		TemplateInfoForMirroring template2b = extractTemplateInfo(cft2b);
		
		assertEquals(0, server.getStore().getListOfFormTemplatesForAccount(accountId1).size());
		realRetriever.pullAllTemplates();
		assertEquals(0, server.getStore().getListOfFormTemplatesForAccount(accountId1).size());
		
		supplier.addAccountToMirror(accountId1);
		supplier.addAccountToMirror(accountId2);
		supplier.addTemplateToMirror(accountId1, template1, cft1.getExportedTemplateAsBase64String(getSecurity()));
		supplier.addTemplateToMirror(accountId2, template2a, cft2a.getExportedTemplateAsBase64String(getSecurity()));
		supplier.addTemplateToMirror(accountId2, template2b, cft2b.getExportedTemplateAsBase64String(getSecurity()));
		
		NetworkResponse available1 = realGateway.getListOfFormTemplateInfos(getSecurity(), accountId1);
		assertEquals(NetworkInterfaceConstants.OK, available1.getResultCode());
		assertEquals(1, available1.getResultVector().size());
		
		NetworkResponse available2 = realGateway.getListOfFormTemplateInfos(getSecurity(), accountId2);
		assertEquals(NetworkInterfaceConstants.OK, available2.getResultCode());
		assertEquals(2, available2.getResultVector().size());

		assertTrue("Won't pull 1?", realRetriever.shouldPullTemplate(accountId1, template1));
		assertTrue("Won't pull 2a?", realRetriever.shouldPullTemplate(accountId2, template2a));
		assertTrue("Won't pull 2b?", realRetriever.shouldPullTemplate(accountId2, template2b));

		realRetriever.pullAllTemplates();
		assertEquals("Didn't pull 1?", 1, server.getStore().getListOfFormTemplatesForAccount(accountId1).size());
		assertEquals("Didn't pull 2a/2b?", 2, server.getStore().getListOfFormTemplatesForAccount(accountId2).size());

		verifyModifiedTime(accountId1, template1);
		verifyModifiedTime(accountId2, template2a);
		verifyModifiedTime(accountId2, template2b);
	}
	
	public void testShouldPullTemplate() throws Exception
	{
		String templateFilename = "filename.mct";
		long earlier = 27;
		long now = 39;
		long later = 55;
		long small = 10;
		long medium = 100;
		long large = 1000;
		
		TemplateInfoForMirroring current = new TemplateInfoForMirroring(templateFilename, now, medium);
		TemplateInfoForMirroring identical = new TemplateInfoForMirroring(templateFilename, now, medium);
		TemplateInfoForMirroring sameButEarlier = new TemplateInfoForMirroring(templateFilename, earlier, medium);
		TemplateInfoForMirroring largerEarlier = new TemplateInfoForMirroring(templateFilename, earlier, large);
		TemplateInfoForMirroring smallerEarlier = new TemplateInfoForMirroring(templateFilename, earlier, small);
		TemplateInfoForMirroring smallerSimultaneous = new TemplateInfoForMirroring(templateFilename, now, small);
		TemplateInfoForMirroring sameButLater = new TemplateInfoForMirroring(templateFilename, later, medium);
		TemplateInfoForMirroring largerLater = new TemplateInfoForMirroring(templateFilename, later, large);
		TemplateInfoForMirroring smallerLater = new TemplateInfoForMirroring(templateFilename, later, small);
		TemplateInfoForMirroring largerSimultaneous = new TemplateInfoForMirroring(templateFilename, now, large);
		
		assertFalse(MirroringRetriever.shouldPullTemplate(current, identical));
		assertFalse(MirroringRetriever.shouldPullTemplate(current, sameButEarlier));
		assertFalse(MirroringRetriever.shouldPullTemplate(current, largerEarlier));
		assertFalse(MirroringRetriever.shouldPullTemplate(current, smallerEarlier));
		assertFalse(MirroringRetriever.shouldPullTemplate(current, smallerSimultaneous));
		assertTrue(MirroringRetriever.shouldPullTemplate(current, sameButLater));
		assertTrue(MirroringRetriever.shouldPullTemplate(current, largerLater));
		assertTrue(MirroringRetriever.shouldPullTemplate(current, smallerLater));
		assertTrue(MirroringRetriever.shouldPullTemplate(current, largerSimultaneous));
	}

	private void verifyModifiedTime(String accountId, TemplateInfoForMirroring templateInfo) throws Exception
	{
		String filename = templateInfo.getFilename();
		File file = server.getStore().getFormTemplateFileFromAccount(accountId, filename);
		long mTime = Files.getLastModifiedTime(file.toPath()).toMillis();
		assertEquals("Time wasn't updated?", templateInfo.getLastModifiedMillis(), mTime);
	}

	private TemplateInfoForMirroring extractTemplateInfo(CustomFieldTemplate cft1) throws Exception
	{
		String title = cft1.getTitle();
		String filename = ServerForClients.calculateFileNameFromString(title);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if(!cft1.saveContentsToOutputStream(server.getSecurity(), out))
			throw new IOException("Unknown exception saving template to stream");
		out.flush();
		long now = new Date().getTime();
		long roundedMinuteAgo = now - now%1000 - 60*1000;
		long arbitrarySize = 5000;
		TemplateInfoForMirroring info = new TemplateInfoForMirroring(filename, roundedMinuteAgo, arbitrarySize);
		return info;
	}

	public CustomFieldTemplate createTemplate(String title) throws Exception 
	{
		return createTemplate(title, "");
	}

	public CustomFieldTemplate createTemplate(String title, String description)	throws Exception 
	{
		FieldCollection topFields = new FieldCollection(StandardFieldSpecs.getDefaultTopSetionFieldSpecs());
		FieldCollection bottomFields = new FieldCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		return new CustomFieldTemplate(title, description, topFields, bottomFields);
	}
	
	public void testGetNextItemToRetrieve() throws Exception
	{
		assertNull("item available right after constructor?", realRetriever.getNextItemToRetrieve());
		Vector items = new Vector();
		for(int i=0; i < 3; ++i)
		{
			UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
			BulletinMirroringInformation info = new BulletinMirroringInformation(uid);
			items.add(info);
			realRetriever.itemsToRetrieve.add(info);
		}

		for(int i=0; i < items.size(); ++i)
			assertEquals("wrong " + i + "?", items.get(i), realRetriever.getNextItemToRetrieve());

		assertNull("uid right after emptied?", realRetriever.getNextItemToRetrieve());
		assertNull("uid again after emptied?", realRetriever.getNextItemToRetrieve());
	}
	
	public void testGetNextItemToRetrieveWithEmptyAccount()
	{
		assertNull(realRetriever.accountsToRetrieve);
		realRetriever.accountsToRetrieve = new Vector();
		realRetriever.accountsToRetrieve.add("empty account");
		realRetriever.accountsToRetrieve.add("account that could have stuff in it");
		
		realRetriever.getNextItemToRetrieve();
		assertNull("Didn't check all accounts in one pass?", realRetriever.accountsToRetrieve);
	}
	
	public void testGetNextAccountToRetrieve() throws Exception
	{
		assertNull("account right after constructor?", realRetriever.getNextAccountToRetrieve());
		Vector accounts = new Vector();
		for(int i=0; i < 3; ++i)
			accounts.add(Integer.toString(i));
		
		assertNull(realRetriever.accountsToRetrieve);
		realRetriever.accountsToRetrieve = new Vector();
		realRetriever.accountsToRetrieve.addAll(accounts);
		for (int i = 0; i < accounts.size(); i++)
			assertEquals("wrong " + i + "?", accounts.get(i), realRetriever.getNextAccountToRetrieve());

		assertNull("account right after emptied?", realRetriever.getNextAccountToRetrieve());
		assertNull("account again after emptied?", realRetriever.getNextAccountToRetrieve());
	}
	
	public void testProcessNextBulletinSkipsIfNothingRecent() throws Exception
	{
		supplier.addAccountToMirror("Test account");
		realRetriever.pullNextBulletin();
		assertTrue("Should have set sleepUntil", realRetriever.sleepUntil > System.currentTimeMillis() + 2000);
	}
	
	public void testRetrieveOneBulletin() throws Exception
	{
		supplier.returnResultTag = MirroringInterface.RESULT_OK;
		
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		supplier.addZipData(uid, StreamableBase64.encode("some text"));
		File tempFile = createTempFile();
		tempFile.deleteOnExit();
		
		realRetriever.retrieveOneBulletin(tempFile, uid);
		assertEquals(uid.getAccountId(), supplier.gotAccount);
		assertEquals(uid.getLocalId(), supplier.gotLocalId);

		int expectedLength = StreamableBase64.decode((String)supplier.zipData.get(uid)).length;
		assertEquals("file wrong length?", expectedLength, tempFile.length());
	}
	
	public void testTickWithNewMirroringServer() throws Exception
	{
		TestCallerSideMirroringGateway newGateway = new TestCallerSideMirroringGateway(wrappedHandler);
		LoggerToNull logger = new LoggerToNull();
		MirroringRetriever newMirroringRetriever = new MirroringRetriever(server.getStore(), newGateway, "Dummy IP", logger);
		boolean makeSureDraftsAreMirrored = true;
		internalTestTick(newMirroringRetriever, makeSureDraftsAreMirrored);
		assertTrue(newGateway.listAvailableIdsForMirroringCalled);
		assertFalse(newGateway.listBulletinsForMirroringCalled);
	}

	public void testTickWithOldMirroringServer() throws Exception
	{
		SupplierSideMirroringHandler oldHandler = new OldSupplierSideMirroringHandler(supplier, server.getSecurity());
		CallerSideMirroringInterface wrappedOldHandler = new PassThroughMirroringGateway(oldHandler);
		TestCallerSideMirroringGateway oldGateway = new TestCallerSideMirroringGateway(wrappedOldHandler);
		LoggerToNull logger = new LoggerToNull();

		MirroringRetriever mirroringRetriever = new MirroringRetriever(server.getStore(), oldGateway, "Dummy IP", logger);
		boolean shouldDraftsBeMirrored = false;
		internalTestTick(mirroringRetriever, shouldDraftsBeMirrored);
		assertTrue(oldGateway.listBulletinsForMirroringCalled);
	}
	
	class TestCallerSideMirroringGateway extends CallerSideMirroringGateway
	{
		public TestCallerSideMirroringGateway(CallerSideMirroringInterface handlerToUse)
		{
			super(handlerToUse);
		}

		public NetworkResponse listAvailableIdsForMirroring(MartusCrypto signer, String authorAccountId) throws MartusSignatureException
		{
			listAvailableIdsForMirroringCalled = true;
			return super.listAvailableIdsForMirroring(signer, authorAccountId);
		}

		public NetworkResponse listBulletinsForMirroring(MartusCrypto signer, String authorAccountId) throws MartusSignatureException
		{
			listBulletinsForMirroringCalled = true;
			return super.listBulletinsForMirroring(signer, authorAccountId);
		}
		
		public boolean listAvailableIdsForMirroringCalled;
		public boolean listBulletinsForMirroringCalled;
	}
	
	class OldSupplierSideMirroringHandler extends SupplierSideMirroringHandler
	{
		public OldSupplierSideMirroringHandler(ServerSupplierInterface supplierToUse, MartusCrypto verifierToUse)
		{
			super(supplierToUse, verifierToUse);
		}

		int extractCommand(Object possibleCommand)
		{
			String cmdString = (String)possibleCommand;
			if(cmdString.equals(CMD_MIRRORING_LIST_AVAILABLE_IDS))
				return cmdUnknown;
			return super.extractCommand(possibleCommand);
		}
	}


	private void internalTestTick(MirroringRetriever mirroringRetriever, boolean draftsShouldBeMirrored) throws Exception, IOException, CryptoException, CreateDigestException, RecordHiddenException, InvalidPacketException, SignatureVerificationException
	{
		assertFalse("initial shouldsleep wrong?", mirroringRetriever.isSleeping());
		// get account list (empty)
		mirroringRetriever.pullNextBulletin();
		assertNull("tick asked for account?", supplier.gotAccount);
		assertNull("tick asked for id?", supplier.gotLocalId);
		assertTrue("not ready to sleep?", mirroringRetriever.isSleeping());
		
		BulletinStore serverStore = new MockBulletinStore(this);
		MartusCrypto otherServerSecurity = MockMartusSecurity.createOtherServer();

		MartusCrypto clientSecurity = MockMartusSecurity.createClient();
		supplier.addAccountToMirror(clientSecurity.getPublicKeyString());
		Vector expectedBulletinLocalIds = new Vector();
		HashMap delRecords = new HashMap();
		for(int i=0; i < 3; ++i)
		{
			boolean sealed = false;
			if(i == 0 || i == 2)
				sealed = true;
			boolean shouldCreateDeleteRequest = i < 2;

			MockDatabase db = (MockDatabase)serverStore.getDatabase();
			int countBeforeSave = db.getRecordCount();
			Bulletin b = createAndSaveBulletin(serverStore, clientSecurity, sealed);
			
			String bur = BulletinUploadRecord.createBulletinUploadRecord(b.getLocalId(), otherServerSecurity);
			BulletinUploadRecord.writeSpecificBurToDatabase(db, b.getBulletinHeaderPacket(), bur);

			if(shouldCreateDeleteRequest)
			{
				DeleteRequestRecord delRecord = new DeleteRequestRecord(b.getAccount(), new Vector(), "signature");
				mirroringRetriever.store.writeDel(b.getUniversalId(), delRecord);
			}

			DatabaseKey key = DatabaseKey.createKey(b.getUniversalId(), b.getStatus());
			String sigString = extractSigString(db, key, otherServerSecurity);
			
			supplier.addAvailableIdsToMirror(db, key, sigString);
			supplier.addBur(b.getUniversalId(), bur, b.getStatus());
			supplier.addZipData(b.getUniversalId(), getZipString(db, b, clientSecurity));
			if(b.isSealed() || draftsShouldBeMirrored)
			{
				supplier.addBulletinToMirror(key, sigString);
			}
			
			if(b.isSealed() || draftsShouldBeMirrored)
			{
				expectedBulletinLocalIds.add(b.getLocalId());
			}
			
			if(shouldCreateDeleteRequest)
				delRecords.put(b.getStatus(), b.getUniversalId());

			assertEquals(countBeforeSave + databaseRecordsPerBulletin, db.getRecordCount());
		}

		int totalBulletinsToMirror = expectedBulletinLocalIds.size();
		
		ReadableDatabase mirroringDataBase = mirroringRetriever.store.getDatabase();
		verifyDeleteRecords(mirroringDataBase, delRecords);

		ServerBulletinStore retrievingStore = server.getStore();
		mirroringRetriever.sleepUntil = System.currentTimeMillis() -1;
		assertEquals("before tick a", 0, retrievingStore.getBulletinCount());

		assertFalse("already sleeping?", mirroringRetriever.isSleeping());
		supplier.returnResultTag = MirroringInterface.RESULT_OK;
		Vector bulletinLocalIdsRetrieved = new Vector();
		
		mirroringRetriever.pullNextBulletin();
		assertEquals("tick1 wrong account?", clientSecurity.getPublicKeyString(), supplier.gotAccount);
		bulletinLocalIdsRetrieved.add(supplier.gotLocalId);
		assertEquals("tick1 bulletin count", 1, retrievingStore.getBulletinCount());
		assertFalse("tick1 fell asleep?", mirroringRetriever.isSleeping());

		mirroringRetriever.pullNextBulletin();
		assertEquals("tick2 wrong account?", clientSecurity.getPublicKeyString(), supplier.gotAccount);
		bulletinLocalIdsRetrieved.add(supplier.gotLocalId);
		assertEquals("tick2 bulletin count", 2, retrievingStore.getBulletinCount());
		assertFalse("tick2 fell asleep?", mirroringRetriever.isSleeping());

		if(draftsShouldBeMirrored)
		{
			mirroringRetriever.pullNextBulletin();
			assertEquals("tick3 wrong account?", clientSecurity.getPublicKeyString(), supplier.gotAccount);
			bulletinLocalIdsRetrieved.add(supplier.gotLocalId);
			assertEquals("tick3 bulletin count", 3, retrievingStore.getBulletinCount());
			assertFalse("tick3 fell asleep?", mirroringRetriever.isSleeping());
		}

		verifyGotAllExpectedIds(expectedBulletinLocalIds, bulletinLocalIdsRetrieved);
		
		verifyDeleteRecordsAreCorrectAfterPull(mirroringDataBase, delRecords, draftsShouldBeMirrored);
		
		mirroringRetriever.pullNextBulletin();
		assertEquals("after extra tick", totalBulletinsToMirror, retrievingStore.getBulletinCount());
		assertEquals("extra tick got uids?", 0, mirroringRetriever.itemsToRetrieve.size());
		assertTrue("not sleeping after extra tick?", mirroringRetriever.isSleeping());

		mirroringRetriever.pullNextBulletin();
		assertEquals("after extra tick2", totalBulletinsToMirror, retrievingStore.getBulletinCount());
		assertEquals("extra tick2 got uids?", 0, mirroringRetriever.itemsToRetrieve.size());
	}

	private void verifyDeleteRecordsAreCorrectAfterPull(
			ReadableDatabase mirroringDataBase, HashMap delRecords,
			boolean draftsShouldBeMirrored) {
		for(int i = 0; i < delRecords.size(); ++i)
		{
			UniversalId uid = (UniversalId)delRecords.get(BulletinConstants.STATUSSEALED);
			assertFalse("DEL record should have been deleted forpulled sealed", mirroringDataBase.doesRecordExist(DeleteRequestRecord.getDelKey(uid)));
			uid = (UniversalId)delRecords.get(BulletinConstants.STATUSDRAFT);
			if(draftsShouldBeMirrored)
				assertFalse("DEL record should have been deleted for pulled draft", mirroringDataBase.doesRecordExist(DeleteRequestRecord.getDelKey(uid)));
			else
				assertTrue("DEL record should not have been deleted for skipped draft", mirroringDataBase.doesRecordExist(DeleteRequestRecord.getDelKey(uid)));				
		}
	}

	private void verifyGotAllExpectedIds(Vector expectedBulletinLocalIds,
			Vector bulletinLocalIdsRetrieved) {
		for(int i = 0; i < expectedBulletinLocalIds.size(); ++i)
		{
			assertContains(expectedBulletinLocalIds.get(i), bulletinLocalIdsRetrieved);
		}
	}

	private void verifyDeleteRecords(ReadableDatabase mirroringDataBase,
			HashMap delRecords) {
		for(int i = 0; i < delRecords.size(); ++i)
		{
			UniversalId uid = (UniversalId)delRecords.get(BulletinConstants.STATUSSEALED);
			assertTrue("DEL record should exist for sealed", mirroringDataBase.doesRecordExist(DeleteRequestRecord.getDelKey(uid)));
			uid = (UniversalId)delRecords.get(BulletinConstants.STATUSDRAFT);
			assertTrue("DEL record should exist for draft", mirroringDataBase.doesRecordExist(DeleteRequestRecord.getDelKey(uid)));
		}
	}

	private String extractSigString(MockDatabase db, DatabaseKey key,
			MartusCrypto otherServerSecurity) throws IOException,
			InvalidPacketException, SignatureVerificationException {
		InputStreamWithSeek in = db.openInputStream(key, otherServerSecurity);
		byte[] sigBytes = BulletinHeaderPacket.verifyPacketSignature(in, otherServerSecurity);
		in.close();
		String sigString = StreamableBase64.encode(sigBytes);
		return sigString;
	}

	private Bulletin createAndSaveBulletin(BulletinStore serverStore,
			MartusCrypto clientSecurity, boolean sealed) throws Exception {
		Bulletin b = new Bulletin(clientSecurity);
		if(sealed)
			b.setSealed();
		else
			b.setDraft();
		serverStore.saveBulletinForTesting(b);
		return b;
	}
	
	public void testListPacketsWeWant() throws Exception
	{
		MartusCrypto clientSecurity = MockMartusSecurity.createClient();
		String accountId = clientSecurity.getPublicKeyString();
		Vector infos = new Vector();

		UniversalId hiddenUid1 = addNewUid(infos, accountId);
		UniversalId visibleUid = addNewUid(infos, accountId);
		UniversalId hiddenUid2 = addNewUid(infos, accountId);
		
		Database db = server.getWriteableDatabase();
		db.hide(hiddenUid1);
		db.hide(hiddenUid2);
		
		Vector result = realRetriever.listOnlyPacketsThatWeWantUsingLocalIds(accountId, infos);
		assertEquals("Didn't remove hidden?", 1, result.size());
		assertEquals("Wrong info?", visibleUid, ((BulletinMirroringInformation)result.get(0)).getUid());
	}
	
	public void testDoWeWantThis() throws Exception
	{
		UniversalId sealedHiddenUid = UniversalIdForTesting.createDummyUniversalId();
		UniversalId sealedNotHiddenUid = UniversalIdForTesting.createDummyUniversalId();
		UniversalId sealedWithDraftDelUid = UniversalIdForTesting.createDummyUniversalId();
		UniversalId draftHiddenUid = UniversalIdForTesting.createDummyUniversalId();
		UniversalId draftNotHiddenUid = UniversalIdForTesting.createDummyUniversalId();
		UniversalId draftWithDelUid = UniversalIdForTesting.createDummyUniversalId();
		Database db = server.getWriteableDatabase();
		db.hide(sealedHiddenUid);
		db.hide(draftHiddenUid);
		DeleteRequestRecord draftDelRecord = new DeleteRequestRecord(draftWithDelUid.getAccountId(), new Vector(), "signature");
		realRetriever.store.writeDel(draftWithDelUid, draftDelRecord);
		DeleteRequestRecord sealedWithDraftDelRecord = new DeleteRequestRecord(sealedWithDraftDelUid.getAccountId(), new Vector(), "signature");
		realRetriever.store.writeDel(sealedWithDraftDelUid, sealedWithDraftDelRecord);
		
		BulletinMirroringInformation sealedHidden = new BulletinMirroringInformation(sealedHiddenUid);
		BulletinMirroringInformation sealedNotHidden = new BulletinMirroringInformation(sealedNotHiddenUid);
		BulletinMirroringInformation sealedWithDraftDel = new BulletinMirroringInformation(sealedWithDraftDelUid);
		
		long sealedDraftsDelRecordmTime = MartusServerUtilities.getDateFromFormattedTimeStamp(sealedWithDraftDelRecord.timeStamp).getTime(); 
		long earlierTime = 123456789;
		sealedWithDraftDel.mTime = sealedDraftsDelRecordmTime - earlierTime; 
			
		BulletinMirroringInformation draftHidden = new BulletinMirroringInformation(draftHiddenUid);
		draftHidden.status = BulletinConstants.STATUSDRAFT;
		BulletinMirroringInformation draftNotHidden = new BulletinMirroringInformation(draftNotHiddenUid);
		draftNotHidden.status = BulletinConstants.STATUSDRAFT;
		BulletinMirroringInformation draftWithDel = new BulletinMirroringInformation(draftWithDelUid);
		draftWithDel.status = BulletinConstants.STATUSDRAFT;
		long draftsDelRecordmTime = MartusServerUtilities.getDateFromFormattedTimeStamp(draftDelRecord.timeStamp).getTime(); 
		draftWithDel.mTime = draftsDelRecordmTime - earlierTime; 

		//Nothing in Database
		assertFalse(realRetriever.doWeWantThis(sealedHidden));		
		assertTrue(realRetriever.doWeWantThis(sealedNotHidden));	
		assertTrue(realRetriever.doWeWantThis(sealedWithDraftDel));	
		assertFalse(realRetriever.doWeWantThis(draftHidden));		
		assertTrue(realRetriever.doWeWantThis(draftNotHidden));
		assertFalse(realRetriever.doWeWantThis(draftWithDel));		
		
		//Bulletins now exist in Database with newer mTimes
		db.writeRecord(DatabaseKey.createSealedKey(sealedNotHiddenUid), "Sealed Data");
		db.writeRecord(DatabaseKey.createDraftKey(draftNotHiddenUid), "Draft Data");
		assertFalse(realRetriever.doWeWantThis(sealedHidden));		
		assertFalse(realRetriever.doWeWantThis(sealedNotHidden));	
		assertTrue("Even if a Del request packet is newer than a sealed, we still want the sealed", realRetriever.doWeWantThis(sealedWithDraftDel));
		assertFalse(realRetriever.doWeWantThis(draftHidden));		
		assertFalse(realRetriever.doWeWantThis(draftNotHidden));
		assertFalse(realRetriever.doWeWantThis(draftWithDel));
		
		//Bulletins now exist in Database with older mTimes
		long futureTime = 1000000;
		sealedHidden.mTime = System.currentTimeMillis()+ futureTime;
		sealedNotHidden.mTime = System.currentTimeMillis()+ futureTime;
		sealedWithDraftDel.mTime = sealedDraftsDelRecordmTime + futureTime; 
		draftHidden.mTime = System.currentTimeMillis()+ futureTime;
		draftNotHidden.mTime = System.currentTimeMillis()+ futureTime;
		draftWithDel.mTime = draftsDelRecordmTime + futureTime; 
		
		assertFalse(realRetriever.doWeWantThis(sealedHidden));		
		assertFalse(realRetriever.doWeWantThis(sealedNotHidden));	
		assertTrue("We should retrieve a newer sealed bulletin with older draft delete record", realRetriever.doWeWantThis(sealedWithDraftDel));		
		assertFalse(realRetriever.doWeWantThis(draftHidden));		
		assertTrue("We should retrieve a newer draft bulletin with older draft bulletin", realRetriever.doWeWantThis(draftNotHidden));
		assertTrue("We should retrieve a newer draft bulletin with older del record", realRetriever.doWeWantThis(draftWithDel));
		
		//Sealed exists in the database and new draft trys to replace it.
		UniversalId sealed = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey sealedKey = DatabaseKey.createSealedKey(sealed);
		db.writeRecord(sealedKey, "Sealed Data");
		BulletinMirroringInformation draftOfSealed = new BulletinMirroringInformation(sealed);
		draftOfSealed.status = BulletinConstants.STATUSDRAFT;
		draftOfSealed.mTime = db.getmTime(sealedKey) + futureTime;
		assertFalse("Should not overwrite a sealed with a newer draft", realRetriever.doWeWantThis(draftOfSealed));		

	}
	
	public void testSaveZipFileToDatabaseWithSamemTime() throws Exception
	{
		File tmpPacketDir = createTempFileFromName("$$$testSaveZipFileToDatabaseWithSamemTime");
		tmpPacketDir.delete();
		tmpPacketDir.mkdir();

		MartusCrypto security = MockMartusSecurity.createServer();
		ServerFileDatabase db = new ServerFileDatabase(tmpPacketDir, security);
		db.initialize();
		MockMartusServer mock = new MockMartusServer(db);
		try
		{
			internalTestDatabasemTime(mock);
		}
		finally
		{
			mock.deleteAllFiles();
			DirectoryUtils.deleteEntireDirectoryTree(tmpPacketDir);
		}
	}

	private void internalTestDatabasemTime(MockMartusServer internalServer) throws Exception
	{
		MartusCrypto security = internalServer.getSecurity();
		BulletinStore store = internalServer.getStore();
		Database db = internalServer.getWriteableDatabase();
		
		Bulletin b1 = new Bulletin(security);
		b1.setSealed();
		store.saveBulletinForTesting(b1);
		Long mtime = new Long(b1.getBulletinHeaderPacket().getLastSavedTime());
		db.setmTime(DatabaseKey.createSealedKey(b1.getUniversalId()), mtime);

		DatabaseKey key = b1.getDatabaseKey();
		File zip1 = createTempFile();
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, key, zip1, security);

		store.deleteAllBulletins();
		internalServer.saveUploadedBulletinZipFile(b1.getAccount(), b1.getLocalId(), zip1);
		zip1.delete();
		assertTrue(db.doesRecordExist(key));
		String bur = db.readRecord(BulletinUploadRecord.getBurKey(key), security);
		assertContains("Zip entry mTime not equals store's mTime", MartusServerUtilities.getFormattedTimeStamp(db.getmTime(key)), bur);
	}
	
	private UniversalId addNewUid(Vector infos, String accountId)
	{
		UniversalId newUid = UniversalIdForTesting.createFromAccountAndPrefix(accountId, "H");
		Vector newInfo = new Vector();
		newInfo.add(newUid.getLocalId());
		infos.add(newInfo.toArray());
		return newUid;
	}
	
	private String getZipString(ReadableDatabase dbToExportFrom, Bulletin b, MartusCrypto signer) throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DatabaseKey[] packetKeys = BulletinZipUtilities.getAllPacketKeys(b.getBulletinHeaderPacket());
		BulletinZipUtilities.extractPacketsToZipStream(dbToExportFrom, packetKeys, out, signer);
		String zipString = StreamableBase64.encode(out.toByteArray());
		return zipString;
	}
	
	private MartusCrypto getSecurity()
	{
		return server.getSecurity();
	}

	final static int databaseRecordsPerBulletin = 4;

	MockMartusServer server;
	FakeServerSupplier supplier;
	SupplierSideMirroringHandler realHandler;
	CallerSideMirroringInterface wrappedHandler;
	CallerSideMirroringGateway realGateway;
	MirroringRetriever realRetriever;
}
