/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2014, Beneficent
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

package org.martus.server.forclients;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.DeleteRequestRecord;
import org.martus.common.database.FileDatabase;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.network.NetworkInterface;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.MockBulletinStore;
import org.martus.server.main.MartusServer;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;

public class TestServerForClients extends TestCaseEnhanced
{

	public TestServerForClients(String name)
	{
		super(name);
	}

	public static void main(String[] args)
	{
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		TRACE_BEGIN("setUp");

		if(clientSecurity == null)
		{
			clientSecurity = MockMartusSecurity.createClient();
			clientAccountId = clientSecurity.getPublicKeyString();
		}
		
		if(serverSecurity == null)
		{
			serverSecurity = MockMartusSecurity.createServer();
		}
		
		if(testServerSecurity == null)
		{
			testServerSecurity = MockMartusSecurity.createOtherServer();
		}

		if(hqSecurity == null)
		{
			hqSecurity = MockMartusSecurity.createHQ();
		}
		if(tempFile == null)
		{
			tempFile = createTempFileFromName("$$$MartusTestMartusServer");
			tempFile.delete();
		}
		if(store == null)
		{
			store = new MockBulletinStore(this);
			b1 = new Bulletin(clientSecurity);
			b1.setAllPrivate(false);
			b1.set(Bulletin.TAGTITLE, "Title1");
			b1.set(Bulletin.TAGPUBLICINFO, "Details1");
			b1.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails1");
			File attachment = createTempFile();
			FileOutputStream out = new FileOutputStream(attachment);
			out.write(b1AttachmentBytes);
			out.close();
			b1.addPublicAttachment(new AttachmentProxy(attachment));
			b1.addPrivateAttachment(new AttachmentProxy(attachment));
			HeadquartersKeys keys = new HeadquartersKeys();
			HeadquartersKey key1 = new HeadquartersKey(hqSecurity.getPublicKeyString());
			keys.add(key1);
			b1.setAuthorizedToReadKeys(keys);
			b1.setSealed();
			store.saveEncryptedBulletinForTesting(b1);
			b1 = BulletinLoader.loadFromDatabase(getClientDatabase(), DatabaseKey.createSealedKey(b1.getUniversalId()), clientSecurity);
	
			b2 = new Bulletin(clientSecurity);
			b2.set(Bulletin.TAGTITLE, "Title2");
			b2.set(Bulletin.TAGPUBLICINFO, "Details2");
			b2.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails2");
			b2.setSealed();
			store.saveEncryptedBulletinForTesting(b2);
			
			draft = new Bulletin(clientSecurity);
			draft.set(Bulletin.TAGPUBLICINFO, "draft public");
			draft.setDraft();
			store.saveEncryptedBulletinForTesting(draft);


			privateBulletin = new Bulletin(clientSecurity);
			privateBulletin.setAllPrivate(true);
			privateBulletin.set(Bulletin.TAGTITLE, "TitlePrivate");
			privateBulletin.set(Bulletin.TAGPUBLICINFO, "DetailsPrivate");
			privateBulletin.set(Bulletin.TAGPRIVATEINFO, "PrivateDetailsPrivate");
			privateBulletin.setSealed();
			store.saveEncryptedBulletinForTesting(privateBulletin);

			b1ZipString = BulletinForTesting.saveToZipString(getClientDatabase(), b1, clientSecurity);
			b1ZipBytes = StreamableBase64.decode(b1ZipString);
			b1ChunkBytes0 = new byte[100];
			b1ChunkBytes1 = new byte[b1ZipBytes.length - b1ChunkBytes0.length];
			System.arraycopy(b1ZipBytes, 0, b1ChunkBytes0, 0, b1ChunkBytes0.length);
			System.arraycopy(b1ZipBytes, b1ChunkBytes0.length, b1ChunkBytes1, 0, b1ChunkBytes1.length);
			b1ChunkData0 = StreamableBase64.encode(b1ChunkBytes0);
			b1ChunkData1 = StreamableBase64.encode(b1ChunkBytes1);
			
		}
		
		mockServer = new MockMartusServer(); 
		mockServer.setClientListenerEnabled(true);
		mockServer.verifyAndLoadConfigurationFiles();
		mockServer.setSecurity(testServerSecurity);
		testServer = mockServer.serverForClients;
		testServerInterface = new ServerSideNetworkHandler(testServer);

		TRACE_END();
	}

	protected void tearDown() throws Exception
	{
		TRACE_BEGIN("tearDown");

		assertEquals("isShutdownRequested", false, mockServer.isShutdownRequested());
		mockServer.deleteAllFiles();
		tempFile.delete();

		TRACE_END();
		super.tearDown();
	}
	
	public void testSealedReplacingDraft() throws Exception
	{
		BulletinStore serverStore = new BulletinStore();
		serverStore.setSignatureGenerator(serverSecurity);
		File serverDir = createTempDirectory();
		File fileDbDir = createTempDirectory();
		ServerFileDatabase serverdb = new ServerFileDatabase(fileDbDir, serverSecurity);
		serverStore.doAfterSigninInitialization(serverDir, serverdb);
		
		Bulletin draftThenSealedBulletin = new Bulletin(clientSecurity);
		draftThenSealedBulletin.setDraft();
		store.saveBulletinForTesting(draftThenSealedBulletin);
		File zipFile = createTempFileFromName("$$$MartusTestZipDraft");
		UniversalId uid = draftThenSealedBulletin.getUniversalId();
		BulletinForTesting.saveToFile(store.getDatabase(),draftThenSealedBulletin, zipFile, clientSecurity);
		
		serverStore.deleteAllData();
		Set draftLeafUids = serverStore.getAllBulletinLeafUids();
		assertNotContains(uid, draftLeafUids);

		serverStore.importZipFileToStoreWithSameUids(zipFile);
		zipFile.delete();
		draftLeafUids = serverStore.getAllBulletinLeafUids();
		assertContains(uid, draftLeafUids);
		
		draftThenSealedBulletin.setSealed();
		store.saveBulletinForTesting(draftThenSealedBulletin);
		zipFile = createTempFileFromName("$$$MartusTestZipSealed");
		BulletinForTesting.saveToFile(store.getDatabase(),draftThenSealedBulletin, zipFile, clientSecurity);

		serverStore.importZipFileToStoreWithSameUids(zipFile);
		zipFile.delete();
		Set sealedUids = serverStore.getAllBulletinLeafUids();
		assertContains(uid, sealedUids);
		store.deleteAllData();
		serverStore.deleteAllData();
		serverdb.deleteAllData();
	}

	public void testListFieldOfficeDraftBulletinIds() throws Exception
	{
		TRACE_BEGIN("testListFieldOfficeDraftBulletinIds");

		mockServer.setSecurity(serverSecurity);

		MartusCrypto fieldSecurity1 = clientSecurity;
		mockServer.allowUploads(fieldSecurity1.getPublicKeyString());

		MartusCrypto nonFieldSecurity = MockMartusSecurity.createOtherClient();
		mockServer.allowUploads(nonFieldSecurity.getPublicKeyString());

		Vector list1 = testServer.listFieldOfficeSealedBulletinIds(hqSecurity.getPublicKeyString(), fieldSecurity1.getPublicKeyString(), new Vector());
		assertNotNull("testListFieldOfficeBulletinSummaries returned null", list1);
		assertEquals("wrong length list 1", 2, list1.size());
		assertNotNull("null id1 [0] list1", list1.get(0));
		assertEquals(NetworkInterfaceConstants.OK, list1.get(0));
		
		MartusCrypto otherServerSecurity = MockMartusSecurity.createOtherServer();

		Bulletin bulletinSealed = new Bulletin(clientSecurity);
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(hqSecurity.getPublicKeyString());
		HeadquartersKey key2 = new HeadquartersKey(otherServerSecurity.getPublicKeyString());
		keys.add(key1);
		keys.add(key2);
		bulletinSealed.setAuthorizedToReadKeys(keys);
		bulletinSealed.setSealed();
		bulletinSealed.setAllPrivate(true);
		store.saveEncryptedBulletinForTesting(bulletinSealed);
		mockServer.uploadBulletin(clientSecurity.getPublicKeyString(), bulletinSealed.getLocalId(), BulletinForTesting.saveToZipString(getClientDatabase(), bulletinSealed, clientSecurity));

		Bulletin bulletinDraft = new Bulletin(clientSecurity);
		bulletinDraft.setAuthorizedToReadKeys(keys);
		bulletinDraft.setDraft();
		store.saveEncryptedBulletinForTesting(bulletinDraft);
		mockServer.uploadBulletin(clientSecurity.getPublicKeyString(), bulletinDraft.getLocalId(), BulletinForTesting.saveToZipString(getClientDatabase(), bulletinDraft, clientSecurity));

		Vector list2 = testServer.listFieldOfficeDraftBulletinIds(hqSecurity.getPublicKeyString(), fieldSecurity1.getPublicKeyString(), new Vector());
		assertEquals("wrong length list2", 2, list2.size());
		assertNotNull("null id1 [0] list2", list2.get(0));
		assertEquals(NetworkInterfaceConstants.OK, list2.get(0));
		String b1Summary = bulletinDraft.getLocalId() + "=" + bulletinDraft.getFieldDataPacket().getLocalId();
		
		Object[] rawInfos = (Object[]) list2.get(1);
		List<Object> infoList = Arrays.asList(rawInfos);
		Vector infos = new Vector(infoList);
		assertContains("missing bulletin Draft?",b1Summary, infos);
		
		Vector list3 = testServer.listFieldOfficeDraftBulletinIds(otherServerSecurity.getPublicKeyString(), fieldSecurity1.getPublicKeyString(), new Vector());
		assertEquals("wrong length list hq2", 2, list3.size());
		assertNotNull("null id1 [0] list hq2", list3.get(0));
		assertEquals(NetworkInterfaceConstants.OK, list3.get(0));
		String b1Summaryhq2 = bulletinDraft.getLocalId() + "=" + bulletinDraft.getFieldDataPacket().getLocalId();
		Object[] rawList3 = (Object[]) list3.get(1);
		Vector list3Ids = new Vector(Arrays.asList(rawList3));
		assertContains("missing bulletin Draft for HQ2?",b1Summaryhq2, list3Ids);
		
		TRACE_END();
	}

	public void testDeleteDraftBulletinsEmptyList() throws Exception
	{
		TRACE_BEGIN("testDeleteDraftBulletinsEmptyList");

		String[] allIds = {};
		String resultAllOk = testServer.deleteDraftBulletins(clientAccountId, getOriginalRequest(allIds), "some signature");
		assertEquals("Empty not ok?", NetworkInterfaceConstants.OK, resultAllOk);

		TRACE_END();
	}
	
	public void testDeleteDraftBulletinsThroughHandler() throws Exception
	{
		TRACE_BEGIN("testDeleteDraftBulletinsThroughHandler");

		String[] allIds = uploadSampleDrafts();
		Vector parameters = new Vector();
		parameters.add(new Integer(allIds.length));
		for (int i = 0; i < allIds.length; i++)
			parameters.add(allIds[i]);

		String sig = clientSecurity.createSignatureOfVectorOfStrings(parameters);
		Vector result = testServerInterface.deleteDraftBulletins(clientAccountId, parameters, sig);
		assertEquals("Result size?", 1, result.size());
		assertEquals("Result not ok?", NetworkInterfaceConstants.OK, result.get(0));

		TRACE_END();
	}
		
	public void testDeleteDraftBulletins() throws Exception
	{
		TRACE_BEGIN("testDeleteDraftBulletinsThroughHandler");


		String[] allIds = uploadSampleDrafts();
		for(int i = 0 ; i < allIds.length; ++i)
		{
			internalTestDelRecord(UniversalId.createFromAccountAndLocalId(clientAccountId,allIds[i]), "DEL already exists?", false);
		}
		String resultAllOk = testServer.deleteDraftBulletins(clientAccountId, getOriginalRequest(allIds), "signature");
		assertEquals("Good 3 not ok?", NetworkInterfaceConstants.OK, resultAllOk);
		BulletinStore testStoreDeleteDrafts = mockServer.getStore();
		assertEquals("Didn't delete all?", 0, testStoreDeleteDrafts.getBulletinCount());
		for(int i = 0 ; i < allIds.length; ++i)
		{
			internalTestDelRecord(UniversalId.createFromAccountAndLocalId(clientAccountId,allIds[i]), "DEL should exist.", true);
		}
		
		testStoreDeleteDrafts.deleteAllData();

		String[] twoGoodOneBad = uploadSampleDrafts();
		twoGoodOneBad[1] = "Not a valid local id";
		for(int i = 0 ; i < twoGoodOneBad.length; ++i)
		{
			internalTestDelRecord(UniversalId.createFromAccountAndLocalId(clientAccountId,twoGoodOneBad[i]), "DEL should not exist.", false);
		}
		String resultOneBad = testServer.deleteDraftBulletins(clientAccountId, getOriginalRequest(twoGoodOneBad), "signature");
		assertEquals("Two good one bad not incomplete?", NetworkInterfaceConstants.INCOMPLETE, resultOneBad);
		assertEquals("Didn't delete two?", 1, testStoreDeleteDrafts.getBulletinCount());
		boolean shouldExist = true;
		for(int i = 0 ; i < twoGoodOneBad.length; ++i)
		{
			internalTestDelRecord(UniversalId.createFromAccountAndLocalId(clientAccountId,twoGoodOneBad[i]), "DEL should:" + i + shouldExist, shouldExist);
			shouldExist = !shouldExist;
		}
		
		uploadSampleBulletin();
		
		int newRecordCount = testStoreDeleteDrafts.getBulletinCount();
		assertNotEquals("Didn't upload?", 1, newRecordCount);
		String[] justSealed = new String[] {b1.getLocalId()};
		String result = testServer.deleteDraftBulletins(clientAccountId, getOriginalRequest(justSealed), "signature");
		assertNotEquals("Sealed should not ok?", NetworkInterfaceConstants.OK, result);
		assertEquals("Deleted sealed?", newRecordCount, testStoreDeleteDrafts.getBulletinCount());
		internalTestDelRecord(b1.getUniversalId(), "DEL should not exist for sealed", false);

		TRACE_END();
	}

	private Vector getOriginalRequest(String[] allIds)
	{
		Vector ids = new Vector();
		ids.add(new Integer(allIds.length));
		for(int i = 0; i < allIds.length; i++)
		{
			ids.add(allIds[i]);
		}
		return ids;
	}

	private void internalTestDelRecord(UniversalId uid, String errorMessage, boolean shouldExist)
	{
		BulletinStore testStoreDeleteDrafts = mockServer.getStore();
		DatabaseKey delKey = DeleteRequestRecord.getDelKey(uid);
		if(shouldExist)
			assertTrue(errorMessage, testStoreDeleteDrafts.getDatabase().doesRecordExist(delKey));
		else
			assertFalse(errorMessage, testStoreDeleteDrafts.getDatabase().doesRecordExist(delKey));
	}

	String[] uploadSampleDrafts() throws Exception
	{
		BulletinStore testStoreUploadDrafts = mockServer.getStore();
		assertEquals("db not empty?", 0, testStoreUploadDrafts.getBulletinCount());
		Bulletin draft1 = new Bulletin(clientSecurity);
		uploadSampleBulletin(draft1);
		assertEquals("Didn't save 1?", 1, testStoreUploadDrafts.getBulletinCount());
		Bulletin draft2 = new Bulletin(clientSecurity);
		uploadSampleBulletin(draft2);
		assertEquals("Didn't save 2?", 2, testStoreUploadDrafts.getBulletinCount());
		Bulletin draft3 = new Bulletin(clientSecurity);
		uploadSampleBulletin(draft3);
		assertEquals("Didn't save 3?", 3, testStoreUploadDrafts.getBulletinCount());

		return new String[] {draft1.getLocalId(), draft2.getLocalId(), draft3.getLocalId()};
	}

	public void testDeleteDelRecordOnClientUploadDraftSealed() throws Exception
	{
		BulletinStore testStoreUploadDraftsSealeds = mockServer.getStore();
		testStoreUploadDraftsSealeds.deleteAllData();
		Bulletin draft1 = new Bulletin(clientSecurity);
		draft1.setDraft();
		uploadSampleBulletin(draft1);
		assertEquals("Didn't save 1?", 1, testStoreUploadDraftsSealeds.getBulletinCount());
		Bulletin draftThenSealed = new Bulletin(clientSecurity);
		draftThenSealed.setDraft();
		uploadSampleBulletin(draftThenSealed);
		assertEquals("Didn't save 2?", 2, testStoreUploadDraftsSealeds.getBulletinCount());

		internalTestDelRecord(draft1.getUniversalId(), "DEL already exists for draft1?", false);
		internalTestDelRecord(draftThenSealed.getUniversalId(), "DEL already exists for draft2?", false);
		
		String[] allIds = new String[] {draft1.getLocalId(), draftThenSealed.getLocalId()};
		String resultAllOk = testServer.deleteDraftBulletins(clientAccountId, getOriginalRequest(allIds), "signature");
		assertEquals("Good 2 not ok?", NetworkInterfaceConstants.OK, resultAllOk);
		assertEquals("Didn't delete all?", 0, testStoreUploadDraftsSealeds.getBulletinCount());

		internalTestDelRecord(draft1.getUniversalId(), "DEL does not exist for draft1?", true);
		internalTestDelRecord(draftThenSealed.getUniversalId(), "DEL does not exist for draft2?", true);
		
		uploadSampleBulletin(draft1);
		assertEquals("Didn't resave draft?", 1, testStoreUploadDraftsSealeds.getBulletinCount());
		draftThenSealed.setSealed();
		uploadSampleBulletin(draftThenSealed);
		assertEquals("Didn't resave sealed?", 2, testStoreUploadDraftsSealeds.getBulletinCount());
		
		internalTestDelRecord(draft1.getUniversalId(), "DEL should not exist after upload of new draft.", false);
		internalTestDelRecord(draftThenSealed.getUniversalId(), "DEL should not exist after upload of new sealed?", false);
	}
	
	public void testLoadingMagicWords() throws Exception
	{		
		TRACE_BEGIN("testLoadingMagicWords");

		String sampleMagicWord1 = "kef7873n2";
		String sampleMagicWord2 = "fjk5dlkg8";
		String inactiveMagicWord2 = "#" + sampleMagicWord2;
		String sampleGroup = "group name";
		String sampleMagicWord3 = "Magic3";
		String sampleMagicWord4 = "magic4";
		String sampleMagicWordline3 = sampleMagicWord3 + "	" + sampleGroup;
		String sampleMagicWordline4 = sampleMagicWord4 + "\t" + sampleGroup;
		String nonExistentMagicWord = "ThisIsNotAMagicWord";
		
		File file = testServer.getMagicWordsFile();
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.writeln(sampleMagicWord1);
		writer.writeln(inactiveMagicWord2);
		writer.writeln(sampleMagicWordline3);
		writer.writeln(sampleMagicWordline4);
		writer.close();

		MockMartusServer other = new MockMartusServer(mockServer.getDataDirectory());
		other.setClientListenerEnabled(true);
		other.verifyAndLoadConfigurationFiles();
		MartusCrypto otherServerSecurity = MockMartusSecurity.createOtherServer();
		other.setSecurity(otherServerSecurity);
		
		String worked = other.requestUploadRights("whatever", sampleMagicWord1);
		assertEquals("didn't work?", NetworkInterfaceConstants.OK, worked);
		
		worked = other.requestUploadRights("whatever2", sampleMagicWord1.toUpperCase());
		assertEquals("should ignore case sensitivity", NetworkInterfaceConstants.OK, worked);
		
		worked = other.requestUploadRights("whatever2", sampleMagicWord3);
		assertEquals("should ignore spaces", NetworkInterfaceConstants.OK, worked);
		
		worked = other.requestUploadRights("whatever2", sampleMagicWord4);
		assertEquals("should ignore other whitespace", NetworkInterfaceConstants.OK, worked);
		
		worked = other.requestUploadRights("whatever", sampleMagicWord2);
		assertEquals("should not work magicWord inactive", NetworkInterfaceConstants.REJECTED, worked);
		
		worked = other.requestUploadRights("whatever2", nonExistentMagicWord);
		assertEquals("should be rejected", NetworkInterfaceConstants.REJECTED, worked);
		
		other.deleteAllFiles();

		TRACE_END();
	}

	public void testAllowUploadsPersistToNextSession() throws Exception
	{
		TRACE_BEGIN("testAllowUploadsPersistToNextSession");

		testServer.clearCanUploadList();
		
		String sampleId = "2345235";
		String dummyMagicWord = "elwijfjf";
		
		testServer.allowUploads(sampleId, dummyMagicWord);
		MockMartusServer other = new MockMartusServer(mockServer.getDataDirectory());
		other.setSecurity(mockServer.getSecurity());
		other.setClientListenerEnabled(true);
		other.verifyAndLoadConfigurationFiles();
		assertEquals("didn't get saved/loaded?", true, other.canClientUpload(sampleId));
		other.deleteAllFiles();

		TRACE_END();
	}

	public void testBannedClients()
		throws Exception
	{
		TRACE_BEGIN("testBannedClients");

		String clientId = clientSecurity.getPublicKeyString();
		String hqId = hqSecurity.getPublicKeyString();
		File testFile = createTempFileFromName("test");
		testServer.loadBannedClients(testFile);
		
		File clientBanned = createTempFile();
		
		UnicodeWriter writer = new UnicodeWriter(clientBanned);
		writer.writeln(clientId);
		writer.close();
		
		String bogusStringParameter = "this is never used in this call. right?";

		testServer.allowUploads(clientId, null);
		testServer.allowUploads(hqId, null);
		testServer.loadBannedClients(clientBanned);

		Vector vecResult = null;
		vecResult = testServer.listMyDraftBulletinIds(clientId, new Vector());
		verifyErrorResult("listMyDraftBulletinIds", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listMyDraftBulletinIds", 0, testServer.getNumberActiveClients() );
		
		String strResult = testServer.requestUploadRights(clientId, bogusStringParameter);
		assertEquals("requestUploadRights", NetworkInterfaceConstants.REJECTED, strResult );
		assertEquals("requestUploadRights", 0, testServer.getNumberActiveClients() );
		
		strResult = testServer.putBulletinChunk(clientId, clientId, bogusStringParameter, 0, 0, 0, bogusStringParameter);
		assertEquals("putBulletinChunk client banned", NetworkInterfaceConstants.REJECTED, strResult);
		assertEquals("putBulletinChunk client banned", 0, testServer.getNumberActiveClients() );

		strResult = testServer.putBulletinChunk(hqId, clientId, bogusStringParameter, 0, 0, 0, bogusStringParameter);
		assertEquals("putBulletinChunk hq not banned but client is", NetworkInterfaceConstants.REJECTED, strResult);

		File noneBanned = createTempFile();
		writer = new UnicodeWriter(noneBanned);
		writer.writeln("");
		writer.close();
		testServer.loadBannedClients(noneBanned);
		strResult = testServer.putBulletinChunk(hqId, clientId, bogusStringParameter, 0, 0, 0, bogusStringParameter);
		assertEquals("putBulletinChunk hq and client not banned should get invalid data", NetworkInterfaceConstants.INVALID_DATA, strResult);
		testServer.clearCanUploadList();
		testServer.allowUploads(hqId, null);
		assertEquals("putBulletinChunk client can't upload but hq can should get invalid data", NetworkInterfaceConstants.INVALID_DATA, strResult);
		
		testServer.allowUploads(clientId, null);
		testServer.loadBannedClients(clientBanned);
		vecResult = testServer.getBulletinChunk(clientId, clientId, bogusStringParameter, 0, 0);
		verifyErrorResult("getBulletinChunk", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("getBulletinChunk", 0, testServer.getNumberActiveClients() );

		vecResult = testServer.getPacket(clientId, bogusStringParameter, bogusStringParameter, bogusStringParameter);
		verifyErrorResult("getPacket", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("getPacket", 0, testServer.getNumberActiveClients() );

		strResult = testServer.deleteDraftBulletins(clientId, new Vector(), "some signature" );
		assertEquals("deleteDraftBulletins", NetworkInterfaceConstants.REJECTED, strResult);
		assertEquals("deleteDraftBulletins", 0, testServer.getNumberActiveClients() );

		strResult = testServer.putContactInfo(clientId, new Vector() );
		assertEquals("putContactInfo", NetworkInterfaceConstants.REJECTED, strResult);		
		assertEquals("putContactInfo", 0, testServer.getNumberActiveClients() );

		vecResult = testServer.listFieldOfficeDraftBulletinIds(hqId, clientId, new Vector());
		verifyErrorResult("listFieldOfficeDraftBulletinIds1", vecResult, NetworkInterfaceConstants.OK );
		assertEquals("listFieldOfficeDraftBulletinIds1", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeAccounts(hqId);
		verifyErrorResult("listFieldOfficeAccounts1", vecResult, NetworkInterfaceConstants.OK );
		assertEquals("listFieldOfficeAccounts1", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeDraftBulletinIds(clientId, clientId, new Vector());
		verifyErrorResult("listFieldOfficeDraftBulletinIds2", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listFieldOfficeDraftBulletinIds2", 0, testServer.getNumberActiveClients() );
		
		vecResult = testServer.listFieldOfficeAccounts(clientId);
		verifyErrorResult("listFieldOfficeAccounts2", vecResult, NetworkInterfaceConstants.REJECTED );
		assertEquals("listFieldOfficeAccounts2", 0, testServer.getNumberActiveClients() );

		TRACE_END();
	}

	public void testGetNewsWithVersionInformation() throws Exception
	{
		TRACE_BEGIN("testGetNewsWithVersionInformation");

		MockServerForClients mockServerForClients = (MockServerForClients)testServer;
		mockServerForClients.loadBannedClients();
		
		final String firstNewsItem = "first news item";
		final String secondNewsItem = "second news item";
		final String thridNewsItem = "third news item";
		Vector twoNews = new Vector();
		twoNews.add(NetworkInterfaceConstants.OK);
		Vector resultNewsItems = new Vector();
		resultNewsItems.add(firstNewsItem);
		resultNewsItems.add(secondNewsItem);
		twoNews.add(resultNewsItems);
		mockServerForClients.newsResponse = twoNews;
	

		Vector noNewsForThisVersion = mockServerForClients.getNews(clientAccountId, "wrong version label" , "wrong version build date");
		assertEquals(2, noNewsForThisVersion.size());
		Object[] noNewsItems = (Object[]) noNewsForThisVersion.get(1);
		assertEquals(0, noNewsItems.length);
		

		String versionToUse = "2.3.4";
		mockServerForClients.newsVersionLabelToCheck = versionToUse;
		mockServerForClients.newsVersionBuildDateToCheck = "";
		Vector twoNewsItemsForThisClientsVersion = mockServerForClients.getNews(clientAccountId, versionToUse , "some version build date");
		Vector twoNewsItems = (Vector)twoNewsItemsForThisClientsVersion.get(1);
		assertEquals(2, twoNewsItems.size());
		assertEquals(firstNewsItem, twoNewsItems.get(0));
		assertEquals(secondNewsItem, twoNewsItems.get(1));


		String versionBuildDateToUse = "02/01/03";
		mockServerForClients.newsVersionLabelToCheck = "";
		mockServerForClients.newsVersionBuildDateToCheck = versionBuildDateToUse;

		Vector threeNews = new Vector();
		threeNews.add(NetworkInterfaceConstants.OK);
		resultNewsItems.add(thridNewsItem);
		threeNews.add(resultNewsItems);
		mockServerForClients.newsResponse = threeNews;

		Vector threeNewsItemsForThisClientsBuildVersion = mockServerForClients.getNews(clientAccountId, "some version label" , versionBuildDateToUse);
		Vector threeNewsItems = (Vector)threeNewsItemsForThisClientsBuildVersion.get(1);
		assertEquals(3, threeNewsItems.size());
		assertEquals(firstNewsItem, threeNewsItems.get(0));
		assertEquals(secondNewsItem, threeNewsItems.get(1));
		assertEquals(thridNewsItem, threeNewsItems.get(2));

		TRACE_END();
	}

	public void testGetNewsBannedClient() throws Exception
	{
		TRACE_BEGIN("testGetNewsBannedClient");
		Vector noNews = testServer.getNews(clientAccountId, "1.0.2", "03/03/03");
		assertEquals(2, noNews.size());
		assertEquals("ok", noNews.get(0));
		assertEquals(0, ((Object[])noNews.get(1)).length);

		testServer.clientsBanned.add(clientAccountId);
		Vector bannedNews = testServer.getNews(clientAccountId, "1.0.1", "01/01/03");
		testServer.clientsBanned.remove(clientAccountId);
		assertEquals(2, bannedNews.size());
		assertEquals("ok", bannedNews.get(0));
		Object[] newsItems = (Object[])bannedNews.get(1);
		assertEquals(1, newsItems.length);
		assertContains("account", (String)newsItems[0]);
		assertContains("blocked", (String)newsItems[0]);
		assertContains("Administrator", (String)newsItems[0]);

		TRACE_END();
	}

	private String createJsonTokenResponse(String accountId, String token)
	{
		return "{\""+MartusAccountAccessToken.MARTUS_ACCOUNT_ACCESS_TOKEN_JSON_TAG+"\":{\""+MartusAccountAccessToken.MARTUS_ACCESS_TOKEN_CREATION_DATE_JSON_TAG+"\":\"02/15/2014 13:30:45\",\""+MartusAccountAccessToken.MARTUS_ACCESS_TOKEN_JSON_TAG+"\":\""+token+"\",\""+MartusAccountAccessToken.MARTUS_ACCESS_ACCOUNT_ID_JSON_TAG+"\":\""+accountId+"\"}}}";
	}
	
	public void testGetMartusAccountAccessToken() throws Exception
	{
		TRACE_BEGIN("testGetMartusAccountAccessToken");

		MockServerForClients mockServerForClients = (MockServerForClients)testServer;
		mockServerForClients.loadBannedClients();
		mockServerForClients.loadConfigurationFiles();
		String emptyTokenInitiallyIndicatesNonResponseFromTokenAuthority = "";
		mockServerForClients.setAccessAccountJsonTokenResponse(emptyTokenInitiallyIndicatesNonResponseFromTokenAuthority);
		
		Vector clientTokenInfo = mockServerForClients.getMartusAccountAccessToken(clientAccountId);
		assertEquals(1, clientTokenInfo.size());
		assertEquals("no token available", clientTokenInfo.get(0));

		String invalidTokenGivenByTokenAuthority = createJsonTokenResponse(clientAccountId, invalidMartusAccessTokenString);
		mockServerForClients.setAccessAccountJsonTokenResponse(invalidTokenGivenByTokenAuthority);
		
		clientTokenInfo = mockServerForClients.getMartusAccountAccessToken(clientAccountId);
		assertEquals(1, clientTokenInfo.size());
		assertEquals("server error", clientTokenInfo.get(0));
		
		
		String validTokenGivenByToken1Authority = createJsonTokenResponse(clientAccountId, validMartusAccessToken1String);
		mockServerForClients.setAccessAccountJsonTokenResponse(validTokenGivenByToken1Authority);
		
		clientTokenInfo = mockServerForClients.getMartusAccountAccessToken(clientAccountId);
		assertEquals("ok", clientTokenInfo.get(0));
		assertEquals(2, clientTokenInfo.size());
		assertEquals(1, ((Object[])clientTokenInfo.get(1)).length);
		Object[] rawToken = (Object[])clientTokenInfo.get(1);
		MartusAccountAccessToken accessToken = new MartusAccountAccessToken(rawToken[0].toString());
		assertEquals("Should be same token as MTA was given", validMartusAccessToken1String, accessToken.getToken());

		String tokenAuthorityDownAfterGivenTokenForClient = "";
		mockServerForClients.setAccessAccountJsonTokenResponse(tokenAuthorityDownAfterGivenTokenForClient);
		
		clientTokenInfo = mockServerForClients.getMartusAccountAccessToken(clientAccountId);
		assertEquals(2, clientTokenInfo.size());
		assertEquals("ok", clientTokenInfo.get(0));
		assertEquals(1, ((Object[])clientTokenInfo.get(1)).length);
		rawToken = (Object[])clientTokenInfo.get(1);
		accessToken = new MartusAccountAccessToken(rawToken[0].toString());
		assertEquals("Should be same token as MTA was given initially", validMartusAccessToken1String, accessToken.getToken());
		
		MockMartusSecurity client2Security = MockMartusSecurity.createOtherClient();
		String clientAccount2Id = client2Security.getPublicKeyString();
		
		String validTokenGivenByToken2Authority = createJsonTokenResponse(clientAccount2Id, validMartusAccessToken2String);
		mockServerForClients.setAccessAccountJsonTokenResponse(validTokenGivenByToken2Authority);
		
		Vector clientTokenInfo2 = mockServerForClients.getMartusAccountAccessToken(clientAccount2Id);
		assertEquals(2, clientTokenInfo2.size());
		assertEquals("ok", clientTokenInfo2.get(0));
		assertEquals(1, ((Object[])clientTokenInfo2.get(1)).length);
		rawToken = (Object[])clientTokenInfo2.get(1);
		accessToken = new MartusAccountAccessToken(rawToken[0].toString());
		assertEquals("Should be same token as MTA was given for the second client", validMartusAccessToken2String, accessToken.getToken());
		
		clientTokenInfo2 = mockServerForClients.getMartusAccountAccessToken(clientAccount2Id);
		assertEquals(2, clientTokenInfo2.size());
		assertEquals("ok", clientTokenInfo2.get(0));
		assertEquals(1, ((Object[])clientTokenInfo2.get(1)).length);
		rawToken = (Object[])clientTokenInfo2.get(1);
		accessToken = new MartusAccountAccessToken(rawToken[0].toString());
		assertEquals("Calling a 2nd time with MTA returning same token should be same token as MTA was given for the second client", validMartusAccessToken2String, accessToken.getToken());

		mockServerForClients.setAccessAccountJsonTokenResponse(tokenAuthorityDownAfterGivenTokenForClient);
		
		clientTokenInfo = mockServerForClients.getMartusAccountAccessToken(clientAccountId);
		assertEquals(2, clientTokenInfo.size());
		assertEquals("ok", clientTokenInfo.get(0));
		assertEquals(1, ((Object[])clientTokenInfo.get(1)).length);
		rawToken = (Object[])clientTokenInfo.get(1);
		accessToken = new MartusAccountAccessToken(rawToken[0].toString());
		assertEquals("Should get back the original token for client1 since MTA is down.", validMartusAccessToken1String, accessToken.getToken());
		
		clientTokenInfo2 = mockServerForClients.getMartusAccountAccessToken(clientAccount2Id);
		assertEquals(2, clientTokenInfo2.size());
		assertEquals("ok", clientTokenInfo2.get(0));
		assertEquals(1, ((Object[])clientTokenInfo2.get(1)).length);
		rawToken = (Object[])clientTokenInfo2.get(1);
		accessToken = new MartusAccountAccessToken(rawToken[0].toString());
		assertEquals("Should be same token as MTA was given for the second client since MTA is down.", validMartusAccessToken2String, accessToken.getToken());
		
		TRACE_END();
	}	
	
	public void testGetMartusAccountAccessTokenBannedClients() throws Exception
	{
		TRACE_BEGIN("testGetMartusAccountAccessTokenBannedClients");

		ServerForClients maatTestServerBannedClients = testServer;
		maatTestServerBannedClients.loadBannedClients();
		maatTestServerBannedClients.loadConfigurationFiles();
		
		maatTestServerBannedClients.clientsBanned.add(clientAccountId);
		Vector bannedClientTokenInfo = maatTestServerBannedClients.getMartusAccountAccessToken(clientAccountId);
		maatTestServerBannedClients.clientsBanned.remove(clientAccountId);
		assertEquals(1, bannedClientTokenInfo.size());
		assertEquals(NetworkInterfaceConstants.REJECTED, bannedClientTokenInfo.get(0));
		
		TRACE_END();
	}
	
	public void testDoesFilenameMatchToken() throws Exception
	{
		String validTokenString = "11223344";

		MartusAccountAccessToken validToken = new MartusAccountAccessToken(validTokenString);
		String validTokenFilename = FileDatabase.buildTokenFilename(validToken);
		File validTokenFile = new File(validTokenFilename);
		assertTrue(MockServerForClients.doesFilenameMatchToken(validTokenFile, validToken.getToken()));
	}

	public void testGetMartusAccountIdFromAccessToken() throws Exception
	{
		TRACE_BEGIN("testGetMartusAccountIdFromAccessToken");

		MockServerForClients mockServerForClients = (MockServerForClients)testServer;
		mockServerForClients.loadBannedClients();
		mockServerForClients.loadConfigurationFiles();
		String emptyTokenInitiallyIndicatesNonResponseFromTokenAuthority = "";
		mockServerForClients.setAccessAccountJsonTokenResponse(emptyTokenInitiallyIndicatesNonResponseFromTokenAuthority);

		MockMartusSecurity client2Security = MockMartusSecurity.createOtherClient();
		String clientAccount2Id = client2Security.getPublicKeyString();
		MartusAccountAccessToken token1ToFind = new MartusAccountAccessToken(validMartusAccessToken1String);
		Vector clientAccountIdsForToken = mockServerForClients.getMartusAccountIdFromAccessToken(clientAccount2Id, token1ToFind);
		assertEquals(1, clientAccountIdsForToken.size());
		assertEquals("no token available", clientAccountIdsForToken.get(0));
		
		String invalidTokenGivenByTokenAuthority = createJsonTokenResponse(clientAccountId, invalidMartusAccessTokenString);
		mockServerForClients.setAccessAccountJsonTokenResponse(invalidTokenGivenByTokenAuthority);
		
		clientAccountIdsForToken = mockServerForClients.getMartusAccountIdFromAccessToken(clientAccount2Id, token1ToFind);
		assertEquals("server error", clientAccountIdsForToken.get(0));
		assertEquals(1, clientAccountIdsForToken.size());
		
		String validTokenGivenByToken1Authority = createJsonTokenResponse(clientAccountId, validMartusAccessToken1String);
		mockServerForClients.setAccessAccountJsonTokenResponse(validTokenGivenByToken1Authority);
		
		clientAccountIdsForToken = mockServerForClients.getMartusAccountIdFromAccessToken(clientAccount2Id, token1ToFind);
		assertEquals("ok", clientAccountIdsForToken.get(0));
		assertEquals(2, clientAccountIdsForToken.size());
		Object[] accountIdOjectArray = (Object[])clientAccountIdsForToken.get(1);
		assertEquals(1, accountIdOjectArray.length);
	
		assertEquals("Account for this token didn't match?", clientAccountId, (String)accountIdOjectArray[0]);

		String tokenAuthorityDownAfterGivenTokenForClient = "";
		mockServerForClients.setAccessAccountJsonTokenResponse(tokenAuthorityDownAfterGivenTokenForClient);
		
		clientAccountIdsForToken = mockServerForClients.getMartusAccountIdFromAccessToken(clientAccount2Id, token1ToFind);
		assertEquals("ok", clientAccountIdsForToken.get(0));
		assertEquals(2, clientAccountIdsForToken.size());
		accountIdOjectArray = (Object[])clientAccountIdsForToken.get(1);
		assertEquals("With MTA down we should still be able to get ClientId from Token", clientAccountId, (String)accountIdOjectArray[0]);
	
		String validTokenGivenByToken2Authority = createJsonTokenResponse(clientAccount2Id, validMartusAccessToken2String);
		mockServerForClients.setAccessAccountJsonTokenResponse(validTokenGivenByToken2Authority);
		MartusAccountAccessToken token2ToFind = new MartusAccountAccessToken(validMartusAccessToken2String);
		
		clientAccountIdsForToken = mockServerForClients.getMartusAccountIdFromAccessToken(clientAccountId, token2ToFind);
		assertEquals("ok", clientAccountIdsForToken.get(0));
		assertEquals(2, clientAccountIdsForToken.size());
		accountIdOjectArray = (Object[])clientAccountIdsForToken.get(1);
		assertEquals("Account for token2 didn't match?", clientAccount2Id, (String)accountIdOjectArray[0]);
		
		mockServerForClients.setAccessAccountJsonTokenResponse(tokenAuthorityDownAfterGivenTokenForClient);
		
		clientAccountIdsForToken = mockServerForClients.getMartusAccountIdFromAccessToken(clientAccountId, token1ToFind);
		assertEquals("ok", clientAccountIdsForToken.get(0));
		assertEquals(2, clientAccountIdsForToken.size());
		accountIdOjectArray = (Object[])clientAccountIdsForToken.get(1);
		assertEquals("MTA down Original Account1 for token1 didn't match?", clientAccountId, (String)accountIdOjectArray[0]);

		clientAccountIdsForToken = mockServerForClients.getMartusAccountIdFromAccessToken(clientAccountId, token2ToFind);
		assertEquals("ok", clientAccountIdsForToken.get(0));
		assertEquals(2, clientAccountIdsForToken.size());
		accountIdOjectArray = (Object[])clientAccountIdsForToken.get(1);
		assertEquals("MTA down Account2 for token2 didn't match?", clientAccount2Id, (String)accountIdOjectArray[0]);

		MartusAccountAccessToken nonExistentTokenToFind = new MartusAccountAccessToken(validMartusAccessToken3String);
		clientAccountIdsForToken = mockServerForClients.getMartusAccountIdFromAccessToken(clientAccount2Id, nonExistentTokenToFind);
		assertEquals(1, clientAccountIdsForToken.size());
		assertEquals("no token available", clientAccountIdsForToken.get(0));
		
		TRACE_END();
	}	

	public void testGetMartusAccountIdFromAccessTokenBannedClients() throws Exception
	{
		TRACE_BEGIN("testGetMartusAccountIdFromAccessTokenBannedClients");

		ServerForClients maatTestServerBannedClients = testServer;
		maatTestServerBannedClients.loadBannedClients();
		maatTestServerBannedClients.loadConfigurationFiles();
		
		maatTestServerBannedClients.clientsBanned.add(clientAccountId);
		MartusAccountAccessToken tokenToUse = new MartusAccountAccessToken("11223344");
		Vector bannedClientTokenInfo = maatTestServerBannedClients.getMartusAccountIdFromAccessToken(clientAccountId, tokenToUse);
		maatTestServerBannedClients.clientsBanned.remove(clientAccountId);
		assertEquals(1, bannedClientTokenInfo.size());
		assertEquals(NetworkInterfaceConstants.REJECTED, bannedClientTokenInfo.get(0));
		
		TRACE_END();
	}	

	public void testGetNews() throws Exception
	{
		TRACE_BEGIN("testGetNews");

		ServerForClients newsTestServer = testServer;
		newsTestServer.loadBannedClients();
		newsTestServer.loadConfigurationFiles();
		
		
		Vector noNews = newsTestServer.getNews(clientAccountId, "1.0.2", "03/03/03");
		assertEquals(2, noNews.size());
		assertEquals("ok", noNews.get(0));
		assertEquals(0, ((Object[])noNews.get(1)).length);
		
		File newsDirectory = newsTestServer.getNewsDirectory();
		newsDirectory.deleteOnExit();
		newsDirectory.mkdirs();
		
		noNews = newsTestServer.getNews(clientAccountId, "1.0.2", "03/03/03");
		assertEquals(2, noNews.size());
		assertEquals("ok", noNews.get(0));
		assertEquals(0, ((Object[])noNews.get(1)).length);
		
		
		File newsFile1 = new File(newsDirectory, "$$$news1.txt");
		newsFile1.deleteOnExit();
		File newsFile2 = new File(newsDirectory, "$$$news2_notice.info");
		newsFile2.deleteOnExit();
		File newsFile3 = new File(newsDirectory, "$$$news3.message");
		newsFile3.deleteOnExit();
		File tmpnewsFileEndingInTilde = new File(newsDirectory, "$$$news4.message~");
		tmpnewsFileEndingInTilde.deleteOnExit();
		File tmpNewsFileEndingInPound = new File(newsDirectory, "$$$news5.message#");
		tmpNewsFileEndingInPound.deleteOnExit();
		
		String newsText1 = "This is news item #1";
		String newsText2 = "This is news item #2";
		String newsText3 = "This is news item #3";
		String newsText4 = "This is news item #4";
		String newsText5 = "This is news item #5";
		
		//Order is important #2, then #3, then #1.
		UnicodeWriter writer = new UnicodeWriter(newsFile2);
		writer.write(newsText2);
		writer.close();
		Thread.sleep(1000); //Important to sleep to ensure order of files Most Recent News First
		
		writer = new UnicodeWriter(newsFile3);
		writer.write(newsText3);
		writer.close();
		Thread.sleep(1000);//Important to sleep to ensure order of files Most Recent News First
		
		writer = new UnicodeWriter(newsFile1);
		writer.write(newsText1);
		writer.close();
		
		writer = new UnicodeWriter(tmpnewsFileEndingInTilde);
		writer.write(newsText4);
		writer.close();
		writer = new UnicodeWriter(tmpNewsFileEndingInPound);
		writer.write(newsText5);
		writer.close();

		newsTestServer.clientsBanned.add(clientAccountId);
		Vector newsItems = newsTestServer.getNews(clientAccountId, "1.0.2", "03/03/03");
		newsTestServer.clientsBanned.remove(clientAccountId);

		assertEquals(2, newsItems.size());
		assertEquals("ok", newsItems.get(0));
		Object[] news = (Object[])newsItems.get(1);
		assertEquals(1, news.length);

		final String bannedText = "Your account has been blocked from accessing this server. " + 
		"Please contact the Server Policy Administrator for more information.";

		assertEquals(bannedText, news[0]);
		
		Date fileDate = new Date(newsFile1.lastModified());
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String NewsFileText1 = format.format(fileDate) + System.getProperty("line.separator") + UnicodeReader.getFileContents(newsFile1); 
		
		fileDate = new Date(newsFile2.lastModified());
		String NewsFileText2 = format.format(fileDate) + System.getProperty("line.separator") + UnicodeReader.getFileContents(newsFile2); 

		fileDate = new Date(newsFile3.lastModified());
		String NewsFileText3 = format.format(fileDate) + System.getProperty("line.separator") + UnicodeReader.getFileContents(newsFile3); 
		newsTestServer.loadConfigurationFiles();
		testServer.deleteStartupFiles();
		
		newsItems = newsTestServer.getNews(clientAccountId, "1.0.2", "03/03/03");
		assertEquals(2, newsItems.size());
		assertEquals("ok", newsItems.get(0));
		news = (Object[])newsItems.get(1);
		assertEquals(3, news.length);
		
		assertEquals(NewsFileText2, news[0]);
		assertEquals(NewsFileText3, news[1]);
		assertEquals(NewsFileText1, news[2]);
		TRACE_END();
	}
	
	public void testTestAccounts()	throws Exception
	{
		TRACE_BEGIN("testTestAccounts");
	
		String clientId = clientSecurity.getPublicKeyString();
		
		testServer.loadTestAccounts(createTempFile());
		assertEquals("nonexistant file should have 0 test accounts",0, testServer.getNumberOfTestAccounts());
		
		File testClient = createTempFile();
		
		UnicodeWriter writer = new UnicodeWriter(testClient);
		writer.writeln(clientId);
		writer.close();
		testServer.loadTestAccounts(testClient);
		assertEquals("1 test account should be active",1, testServer.getNumberOfTestAccounts());
		assertTrue("Tester's AccountID not found?", testServer.isTestAccount(clientId));
		
}

	public void testClientCounter()
	{
		TRACE_BEGIN("testClientCounter");

		assertEquals("getNumberActiveClients 1", 0, testServer.getNumberActiveClients());
		
		String clientId = clientSecurity.getPublicKeyString();
		testServer.clientConnectionStart(clientId);
		testServer.clientConnectionStart(clientId);
		assertEquals("getNumberActiveClients 2", 2, testServer.getNumberActiveClients());
		
		testServer.clientConnectionExit();
		testServer.clientConnectionExit();
		assertEquals("getNumberActiveClients 3", 0, testServer.getNumberActiveClients());

		TRACE_END();
	}
	
	void uploadSampleBulletin() 
	{
		mockServer.setSecurity(serverSecurity);
		mockServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString(), "silly magic word");
		mockServer.uploadBulletin(clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipString);
	}
	
	String uploadSampleBulletin(Bulletin bulletin) throws Exception
	{
		mockServer.setSecurity(serverSecurity);
		testServer.clearCanUploadList();
		mockServer.allowUploads(clientSecurity.getPublicKeyString());
		
		String draftZipString = BulletinForTesting.saveToZipString(getClientDatabase(), bulletin, clientSecurity);
		String result = mockServer.uploadBulletin(clientSecurity.getPublicKeyString(), bulletin.getLocalId(), draftZipString);
		assertEquals("upload failed?", NetworkInterfaceConstants.OK, result);
		return draftZipString;
	}
	
	String uploadBulletinChunk(MartusServer server, String authorId, String localId, int totalLength, int offset, int chunkLength, String data, MartusCrypto signer) throws Exception
	{
		String stringToSign = authorId + "," + localId + "," + Integer.toString(totalLength) + "," + 
					Integer.toString(offset) + "," + Integer.toString(chunkLength) + "," + data;
		byte[] bytesToSign = stringToSign.getBytes("UTF-8");
		byte[] sigBytes = signer.createSignatureOfStream(new ByteArrayInputStream(bytesToSign));
		String signature = StreamableBase64.encode(sigBytes);
		return server.uploadBulletinChunk(authorId, localId, totalLength, offset, chunkLength, data, signature);
	}
	
	void verifyErrorResult(String label, Vector vector, String expected )
	{
		assertTrue( label + " error size not at least 1?", vector.size() >= 1);
		assertEquals( label + " error wrong result code", expected, vector.get(0));
	}

	static MockClientDatabase getClientDatabase()
	{
		return (MockClientDatabase)store.getDatabase();
	}

	static MartusCrypto clientSecurity;
	static String clientAccountId;
	static MartusCrypto serverSecurity;
	static MartusCrypto testServerSecurity;
	static MartusCrypto hqSecurity;
	private static MockBulletinStore store;

	static Bulletin b1;
	static byte[] b1ZipBytes;
	static String b1ZipString;
	static byte[] b1ChunkBytes0;
	static byte[] b1ChunkBytes1;
	static String b1ChunkData0;
	static String b1ChunkData1;
	final static byte[] b1AttachmentBytes = {1,2,3,4,4,3,2,1};
	
	
	final static String validMartusAccessToken1String = "11223344";
	final static String validMartusAccessToken2String = "34482187";
	final static String validMartusAccessToken3String = "22334452";
	final static String invalidMartusAccessTokenString = "123456789";
	
	static Bulletin b2;
	static Bulletin privateBulletin;
	static Bulletin draft;

	static File tempFile;

	MockMartusServer mockServer; 
	ServerForClients testServer;
	NetworkInterface testServerInterface;
}
