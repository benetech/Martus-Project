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

package org.martus.server.forclients;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.martus.amplifier.ServerCallbackInterface;
import org.martus.common.ContactInfo;
import org.martus.common.FieldCollection;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.LoggerInterface;
import org.martus.common.LoggerToNull;
import org.martus.common.MartusUtilities;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.BulletinUploadRecord;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.fieldspec.CustomFieldTemplate;
import org.martus.common.fieldspec.CustomFieldTemplate.FutureVersionException;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.network.NetworkInterface;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.PartialUploadStatus;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.MockBulletinStore;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.server.main.MartusServer;
import org.martus.server.main.ServerBulletinStore;
import org.martus.util.Base64;
import org.martus.util.StreamableBase64;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


public class TestMartusServer extends TestCaseEnhanced implements NetworkInterfaceConstants
{
	public TestMartusServer(String name) throws Exception
	{
		super(name);
		VERBOSE = false;

/*
 * This code creates a key pair and prints it, so you can 
 * use it to hard code in a test */
//		MartusSecurity security = new MartusSecurity(12345);
//		security.createKeyPair();
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		security.writeKeyPair(out, "test");
//		System.out.println(Base64.encode(out.toByteArray()));

	}

	public void setUp() throws Exception
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
		
		if(otherServerSecurity == null)
		{
			otherServerSecurity = MockMartusSecurity.createOtherServer();
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
		
		testServer = new MockMartusServer();
		testServer.serverForClients.loadBannedClients();
		testServer.setSecurity(serverSecurity);
		testServer.verifyAndLoadConfigurationFiles();
		testServerInterface = new ServerSideNetworkHandler(testServer.serverForClients);

		TRACE_END();
	}
	
	public void tearDown() throws Exception
	{
		TRACE_BEGIN("tearDown");

		assertEquals("isShutdownRequested", false, testServer.isShutdownRequested());
		testServer.deleteAllFiles();
		tempFile.delete();

		TRACE_END();
		super.tearDown();
	}
	
	public void testGetPartialUploadStatus() throws Exception
	{
		Vector emptyRawResult = testServer.getPartialUploadStatus(b1.getAccount(), b1.getLocalId(), new Vector());
		NetworkResponse emptyResponse = new NetworkResponse(emptyRawResult);
		Vector emptyResult = emptyResponse.getResultVector();
		Long emptyLength = new Long((String)emptyResult.get(0));
		String emptySha = (String) emptyResult.get(1);
		PartialUploadStatus emptyStatus = new PartialUploadStatus(emptyLength, emptySha);
		assertFalse("Partial upload already exists?", emptyStatus.hasPartialUpload());

		ServerBulletinStore serverStore = testServer.getStore();
		File interim = serverStore.getIncomingInterimFile(b1.getUniversalId());
		UnicodeWriter writer = new UnicodeWriter(interim);
		String sampleText = "This is just some sample text";
		writer.write(sampleText);
		writer.close();
		
		Vector partialRawResult = testServer.getPartialUploadStatus(b1.getAccount(), b1.getLocalId(), new Vector());
		NetworkResponse partialResponse = new NetworkResponse(partialRawResult);
		Vector partialResult = partialResponse.getResultVector();
		Long partialLength = new Long((String)partialResult.get(0));
		String partialSha = (String) partialResult.get(1);
		PartialUploadStatus partialStatus = new PartialUploadStatus(partialLength, partialSha);
		assertTrue("No partial upload?", partialStatus.hasPartialUpload());
		assertEquals("Wrong length?", sampleText.length(), partialStatus.lengthOfPartialUpload());
		assertEquals("Wrong SHA1?", MartusCrypto.createDigestString(sampleText), partialStatus.sha1OfPartialUpload());
	}
	
	public void testDoesDraftExist() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
	
		ServerBulletinStore serverStore = new ServerBulletinStore();
		serverStore.setSignatureGenerator(security);
		MockServerDatabase serverDatabase = new MockServerDatabase();
		serverStore.setDatabase(serverDatabase);

		BulletinStore clientStore = new BulletinStore();
		clientStore.setSignatureGenerator(security);
		MockClientDatabase clientDatabase = new MockClientDatabase();
		clientStore.setDatabase(clientDatabase);

		Bulletin bSealed = new Bulletin(security);
		bSealed.setSealed();
		clientStore.saveBulletinForTesting(bSealed);

		Bulletin bDraft = new Bulletin(security);
		bDraft.setDraft();
		clientStore.saveBulletinForTesting(bDraft);
		
		DatabaseKey key1 = bSealed.getDatabaseKey();
		File zip1 = createTempFile();
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(clientDatabase, key1, zip1, security);
		serverStore.saveZipFileToDatabase(zip1, security.getPublicKeyString());
		zip1.delete();
		
		DatabaseKey key2 = bDraft.getDatabaseKey(); 
		File zip2 = createTempFile();
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(clientDatabase, key2, zip2, security);
		serverStore.saveZipFileToDatabase(zip2, security.getPublicKeyString());
		zip2.delete();
		
		MockMartusServer server = new MockMartusServer((Database)serverStore.getDatabase());
		assertFalse(server.doesDraftExist(bSealed.getUniversalId()));
		assertTrue(server.doesDraftExist(bDraft.getUniversalId()));
		
		clientStore.deleteAllData();
		serverStore.deleteAllData();
		server.deleteAllFiles();
	}
	
	public void testListFieldOfficeAccountsErrorCondition() throws Exception
	{
		TRACE_BEGIN("testListFieldOfficeAccountsErrorCondition");

		class MockDatabaseThatFails extends MockServerDatabase
		{
			public InputStreamWithSeek openInputStream(DatabaseKey key, MartusCrypto decrypter)
			{
				if(shouldFail)
					return null;
				return super.openInputStream(key, decrypter);
			}
			
			boolean shouldFail;
		}
		MockDatabaseThatFails ourMockDatabase = new MockDatabaseThatFails(); 
		
		testServer.setSecurity(serverSecurity);
		testServer.getStore().setDatabase(ourMockDatabase);
		
		MartusCrypto fieldSecurity1 = clientSecurity;
		testServer.allowUploads(fieldSecurity1.getPublicKeyString());
		Bulletin bulletin = new Bulletin(clientSecurity);
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key = new HeadquartersKey(hqSecurity.getPublicKeyString());
		keys.add(key);
		bulletin.setAuthorizedToReadKeys(keys);
		bulletin.setSealed();
		store.saveEncryptedBulletinForTesting(bulletin);
		testServer.uploadBulletin(bulletin.getAccount(), bulletin.getLocalId(), BulletinForTesting.saveToZipString(getClientDatabase(), bulletin, clientSecurity));

		privateBulletin.setAuthorizedToReadKeys(keys);
		store.saveEncryptedBulletinForTesting(privateBulletin);
		testServer.uploadBulletin(privateBulletin.getAccount(), privateBulletin.getLocalId(), BulletinForTesting.saveToZipString(getClientDatabase(), privateBulletin, clientSecurity));

		testServer.getStore().clearCache();
		ourMockDatabase.shouldFail = true;
		Vector list2 = testServer.listFieldOfficeAccounts(hqSecurity.getPublicKeyString());
		assertEquals("wrong length", 1, list2.size());
		assertEquals(NetworkInterfaceConstants.SERVER_ERROR, list2.get(0));
		TRACE_END();
	}


	public void testHQProxyUploadBulletin() throws Exception
	{
		TRACE_BEGIN("testHQProxyUploadBulletin");
		testServer.serverForClients.clearCanUploadList();
		String allowedToUploadAccountId = clientSecurity.getPublicKeyString();
		testServer.allowUploads(allowedToUploadAccountId);
		String hqAccountId = hqSecurity.getPublicKeyString();
		testServer.allowUploads(hqAccountId);

		
		Bulletin b = new Bulletin(clientSecurity);
		b.set(Bulletin.TAGTITLE, "Title1");
		b.set(Bulletin.TAGPUBLICINFO, "Details1");
		b.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails1");
		HeadquartersKeys hqKey = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(hqAccountId);
		hqKey.add(key1);
		b.setAuthorizedToReadKeys(hqKey);
		b.setSealed();
		store.saveEncryptedBulletinForTesting(b);
		b = BulletinLoader.loadFromDatabase(getClientDatabase(), DatabaseKey.createSealedKey(b.getUniversalId()), clientSecurity);
		
		String draft1ZipString = BulletinForTesting.saveToZipString(getClientDatabase(), b, clientSecurity);
		byte[] draft1ZipBytes = StreamableBase64.decode(draft1ZipString);

		String result = testServer.putBulletinChunk(hqAccountId, allowedToUploadAccountId, b.getLocalId(), draft1ZipBytes.length, 0, 
				draft1ZipBytes.length, draft1ZipString);
		assertEquals(NetworkInterfaceConstants.OK, result);

		result = testServer.putBulletinChunk(allowedToUploadAccountId, allowedToUploadAccountId, b.getLocalId(), draft1ZipBytes.length, 0, 
			draft1ZipBytes.length, draft1ZipString);
		assertEquals(NetworkInterfaceConstants.DUPLICATE, result);
		
		TRACE_END();
	}
	
	public void testUploadMultiChunkBulletinWithLargerChunkSize() throws Exception
	{
		class StringLogger implements LoggerInterface
		{
			public void logDebug(String message) {
				addToLog(message);
			}

			public void logError(String message) {
				addToLog(message);
			}

			public void logError(Exception e) {
				addToLog(e.getMessage());
			}

			public void logError(String message, Exception e) {
				addToLog(message);
				addToLog(e.getClass().getName() + ": " + e.getMessage());
				e.printStackTrace();
			}

			public void logInfo(String message) {
				addToLog(message);
			}

			public void logNotice(String message) {
				addToLog(message);
			}

			public void logWarning(String message) {
				addToLog(message);
			}

			private void addToLog(String message) {
				logged += message + "\n";
			}

			public String logged;
		}
		
		LoggerInterface oldLogger = testServer.getLogger();
		StringLogger logger = new StringLogger();
		testServer.setLogger(logger);
		try
		{
			testServer.setSecurity(serverSecurity);
			testServer.serverForClients.clearCanUploadList();
			String accountId = clientSecurity.getPublicKeyString();
			testServer.allowUploads(accountId);
			int largeChunkSize = NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE + 1;
			byte[] rawData = new byte[largeChunkSize + 10];
			String data = Base64.encodeBytes(rawData, 0, largeChunkSize, Base64.DONT_BREAK_LINES);
			String result = testServer.putBulletinChunk(accountId, b1.getAccount(), b1.getLocalId(), largeChunkSize + 10, 0, largeChunkSize, data);
			assertNotContains("Log mentioned INVALID?", "INVALID", logger.logged);
			assertNotContains("Rejected larger chunk size?", NetworkInterfaceConstants.INVALID_DATA, result);
		}
		finally
		{
			testServer.setLogger(oldLogger);
		}
	}
	
	
	public void testUploadNotMyBulletin() throws Exception
	{
		TRACE_BEGIN("testUploadNotMyBulletin");
		testServer.serverForClients.clearCanUploadList();
		String authorAccountId = clientSecurity.getPublicKeyString();
		testServer.allowUploads(authorAccountId);
		String notAuthorizedAccountId = otherServerSecurity.getPublicKeyString();
		testServer.allowUploads(notAuthorizedAccountId);

		Bulletin b = new Bulletin(clientSecurity);
		b.set(Bulletin.TAGTITLE, "Title1");
		b.set(Bulletin.TAGPUBLICINFO, "Details1");
		b.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails1");
		b.setSealed();
		store.saveEncryptedBulletinForTesting(b);
		b = BulletinLoader.loadFromDatabase(getClientDatabase(), DatabaseKey.createSealedKey(b.getUniversalId()), clientSecurity);
		
		String draft1ZipString = BulletinForTesting.saveToZipString(getClientDatabase(), b, clientSecurity);
		byte[] draft1ZipBytes = StreamableBase64.decode(draft1ZipString);

		String result = testServer.putBulletinChunk(authorAccountId, authorAccountId, b.getLocalId(), draft1ZipBytes.length, 0, 
			draft1ZipBytes.length, draft1ZipString);
		assertEquals(NetworkInterfaceConstants.OK, result);
		
		result = testServer.putBulletinChunk(notAuthorizedAccountId, authorAccountId, b.getLocalId(), draft1ZipBytes.length, 0, 
				draft1ZipBytes.length, draft1ZipString);
		assertEquals(NetworkInterfaceConstants.NOTYOURBULLETIN, result);
		
		TRACE_END();
	}
	
	public void testGetNotMyBulletin() throws Exception
	{
		TRACE_BEGIN("testGetNotMyBulletin");

		testServer.setSecurity(serverSecurity);
		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		testServer.uploadBulletin(clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipString);
		
		MartusCrypto newClientSecurity = MockMartusSecurity.createOtherClient();

		Vector result = getBulletinChunk(newClientSecurity, testServerInterface, b1.getAccount(), b1.getLocalId(), 0, NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE);
		assertEquals("Succeeded?  You are not the owner or the HQ", NetworkInterfaceConstants.NOTYOURBULLETIN, result.get(0));

		TRACE_END();
	}

	public void testGetHQBulletin() throws Exception
	{
		TRACE_BEGIN("testGetHQBulletin");

		testServer.setSecurity(serverSecurity);
		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		testServer.uploadBulletin(clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipString);
		
		Vector result = getBulletinChunk(hqSecurity, testServerInterface, b1.getAccount(), b1.getLocalId(), 0, NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE);
		assertEquals("Failed? You are the HQ", NetworkInterfaceConstants.OK, result.get(0));

		TRACE_END();
	}

	public void testLoadHiddenPacketsList() throws Exception
	{
		String newline = "\n";
		String[] accountIds = {"silly account", "another account", "last account"};
		String[] localIds = {"local-1", "another-local", "third-local"};
		StringWriter noTrailingNewline = new StringWriter();
		noTrailingNewline.write(accountIds[0] + newline); 
		noTrailingNewline.write("  " + localIds[0] + newline);
		noTrailingNewline.write(" " + localIds[1] + newline);
		noTrailingNewline.write(accountIds[1] + newline); 
		noTrailingNewline.write(accountIds[2] + newline); 
		noTrailingNewline.write("  " + localIds[0] + "   " + localIds[2]);

		String noNewline = noTrailingNewline.toString();
		verifyLoadHiddenPacketsList(noNewline, accountIds, localIds);
		String oneNewline = noNewline + newline;
		verifyLoadHiddenPacketsList(oneNewline, accountIds, localIds);
		String twoNewlines = oneNewline + newline;
		verifyLoadHiddenPacketsList(twoNewlines, accountIds, localIds);
	}

	private void verifyLoadHiddenPacketsList(
		String isHiddenNoTrailingNewline,
		String[] accountIds,
		String[] localIds)
		throws Exception
	{
		MockMartusServer server = new MockMartusServer();
		byte[] bytes = isHiddenNoTrailingNewline.getBytes("UTF-8");
		UnicodeReader reader = new UnicodeReader(new ByteArrayInputStream(bytes));
		Vector hiddenPackets = MartusServerUtilities.getHiddenPacketsList(reader);		
		server.getStore().hidePackets(hiddenPackets, new LoggerToNull());
		ReadableDatabase db = server.getDatabase();
		assertTrue(db.isHidden(UniversalId.createFromAccountAndLocalId(accountIds[0], localIds[0])));
		assertTrue(db.isHidden(UniversalId.createFromAccountAndLocalId(accountIds[0], localIds[1])));
		assertTrue(db.isHidden(UniversalId.createFromAccountAndLocalId(accountIds[2], localIds[0])));
		assertTrue(db.isHidden(UniversalId.createFromAccountAndLocalId(accountIds[2], localIds[2])));
		server.deleteAllFiles();
	}
	
	public void testGetServerCompliance() throws Exception
	{
		TRACE_BEGIN("testGetServerCompliance");
		String serverComplianceString = "I am compliant";
		testServer.setComplianceStatement(serverComplianceString);
		Vector compliance = testServer.getServerCompliance();
		assertEquals(2, compliance.size());
		assertEquals("ok", compliance.get(0));
		Object[] result = (Object[])compliance.get(1);
		assertEquals(1, result.length);
		assertEquals(serverComplianceString, result[0]);
		TRACE_END();
	}
	
	public void testLegacyApiMethodNamesNonSSL()
	{
		TRACE_BEGIN("testLegacyApiMethodNamesNonSSL");

		Method[] methods = ServerSideNetworkHandlerForNonSSL.class.getMethods();
		Vector names = new Vector();
		for(int i=0; i < methods.length; ++i)
			names.add(methods[i].getName());
		// Note: These strings are legacy and can NEVER change
		assertContains("ping", names);
		assertContains("getServerInformation", names);
		assertNotContains("requestUploadRights", names);
		assertNotContains("uploadBulletin", names);
		assertNotContains("downloadBulletin", names);
		assertNotContains("listMyBulletinSummaries", names);

		TRACE_END();
	}


	public void testLegacyApiMethodNamesSSL()
	{
		TRACE_BEGIN("testLegacyApiMethodNamesSSL");

		Method[] methods = ServerSideNetworkHandler.class.getMethods();
		Vector names = new Vector();
		for(int i=0; i < methods.length; ++i)
			names.add(methods[i].getName());

		// Note: These strings are legacy and can NEVER change
		assertNotContains("ping", names);
		assertNotContains("requestUploadRights", names);
		assertNotContains("uploadBulletinChunk", names);
		assertNotContains("downloadMyBulletinChunk", names);
		assertNotContains("listMyBulletinSummaries", names);
		assertNotContains("downloadFieldOfficeBulletinChunk", names);
		assertNotContains("listFieldOfficeBulletinSummaries", names);
		assertNotContains("listFieldOfficeAccounts", names);
		assertNotContains("downloadFieldDataPacket", names);

		TRACE_END();
	}


	public void testPing() throws Exception
	{
		TRACE_BEGIN("testPing");

		assertEquals(NetworkInterfaceConstants.VERSION, testServer.ping());

		TRACE_END();
	}
	
	public void testCreateInterimBulletinFile() throws Exception
	{
		TRACE_BEGIN("testCreateInterimBulletinFile");

		testServer.setSecurity(serverSecurity);
		File nullZipFile = createTempFileFromName("$$$MartusServerBulletinZip");
		File nullZipSignatureFile = MartusUtilities.getSignatureFileFromFile(nullZipFile);
		nullZipSignatureFile.deleteOnExit();
		assertFalse("Both zip & sig Null files verified?", testServer.verifyBulletinInterimFile(nullZipFile, nullZipSignatureFile, serverSecurity.getPublicKeyString()));
		
		File validZipFile = createTempFile();
		FileOutputStream out = new FileOutputStream(validZipFile);
		out.write(file1Bytes);
		out.close();
		assertFalse("Valid zip Null sig files verified?", testServer.verifyBulletinInterimFile(validZipFile, nullZipSignatureFile, serverSecurity.getPublicKeyString()));

		File ZipSignatureFile = MartusUtilities.createSignatureFileFromFile(validZipFile, serverSecurity);
		ZipSignatureFile.deleteOnExit();
		File nullFile = createTempFile();
		assertFalse("Null zip Valid sig file verified?", testServer.verifyBulletinInterimFile(nullFile, ZipSignatureFile, serverSecurity.getPublicKeyString()));
		
		File invalidSignatureFile = createTempFile();
		FileOutputStream outInvalidSig = new FileOutputStream(invalidSignatureFile);
		outInvalidSig.write(file2Bytes);
		outInvalidSig.close();
		assertFalse("Valid zip, invalid signature file verified?", testServer.verifyBulletinInterimFile(validZipFile, invalidSignatureFile, serverSecurity.getPublicKeyString()));

		assertTrue("Valid zip with cooresponding signature file did not verify?", testServer.verifyBulletinInterimFile(validZipFile, ZipSignatureFile, serverSecurity.getPublicKeyString()));
	

		TRACE_END();
	}
	
	public void testPutContactInfoNotEncodedBackwardCompatible() throws Exception
	{
		TRACE_BEGIN("testPutContactInfoNotEncodedBackwardCompatible");

		Vector contactInfo = new Vector();
		String clientId = clientSecurity.getPublicKeyString();

		testServer.serverForClients.allowUploads(clientId, null);
		String resultIncomplete = testServer.putContactInfo(clientId, contactInfo);
		assertEquals("Empty ok?", INVALID_DATA, resultIncomplete);

		contactInfo.add("bogus data");
		resultIncomplete = testServer.putContactInfo(clientId, contactInfo);
		assertEquals("Incorrect not Incomplete?", INVALID_DATA, resultIncomplete);
		
		contactInfo.clear();
		contactInfo.add(clientId);
		contactInfo.add(new Integer(1));
		contactInfo.add("Data");
		contactInfo.add("invalid Signature");
		String invalidSig = testServer.putContactInfo(clientId, contactInfo);
		assertEquals("Invalid Signature", SIG_ERROR, invalidSig);		

		contactInfo.clear();
		contactInfo.add(clientId);
		contactInfo.add(new Integer(2));
		contactInfo.add("Data");
		contactInfo.add("Data2");
		String signature = clientSecurity.createSignatureOfVectorOfStrings(contactInfo);
		contactInfo.add(signature);
		testServer.allowUploads("differentAccountID");
		String incorrectAccoutResult = testServer.putContactInfo("differentAccountID", contactInfo);
		assertEquals("Incorrect Accout ", INVALID_DATA, incorrectAccoutResult);		

		ServerBulletinStore serverStore = testServer.getStore();
		assertFalse("Contact File already exists?", serverStore.doesContactInfoExist(clientId));		
		String correctResult = testServer.putContactInfo(clientId, contactInfo);
		assertEquals("Correct Signature", OK, correctResult);		

		assertTrue("File Doesn't exist?", serverStore.doesContactInfoExist(clientId));
		Database db = testServer.getWriteableDatabase();
		File contactFile = db.getContactInfoFile(clientId);
		assertTrue("Size too small", contactFile.length() > 200);

		FileInputStream contactFileInputStream = new FileInputStream(contactFile);
		DataInputStream in = new DataInputStream(contactFileInputStream);

		String inputPublicKey = in.readUTF();
		int inputDataCount = in.readInt();
		String inputData =  in.readUTF();
		String inputData2 =  in.readUTF();
		String inputSig = in.readUTF();
		in.close();

		assertEquals("Public key doesn't match", clientId, inputPublicKey);
		assertEquals("data size not two?", 2, inputDataCount);
		assertEquals("data not correct?", "Data", inputData);
		assertEquals("data2 not correct?", "Data2", inputData2);
		assertEquals("signature doesn't match?", signature, inputSig);		

		contactFile.delete();
		contactFile.getParentFile().delete();

		testServer.serverForClients.clientsBanned.add(clientId);
		String banned = testServer.putContactInfo(clientId, contactInfo);
		assertEquals("Client is banned should not accept contact info", REJECTED, banned);
		
		TRACE_END();
	}
	
	public void testPutGetFormTemplates() throws Exception
	{
		TRACE_BEGIN("testPutGetFormTemplates");
		File tmpPacketDir = createTempFileFromName("$$$testPutGetFormTemplates");
		tmpPacketDir.delete();
		tmpPacketDir.mkdir();

		MartusCrypto security = MockMartusSecurity.createServer();
		ServerFileDatabase db = new ServerFileDatabase(tmpPacketDir, security);
		db.initialize();
		MockMartusServer mock = new MockMartusServer(db);
		mock.serverForClients.loadBannedClients();
		mock.setSecurity(security);
		mock.verifyAndLoadConfigurationFiles();
		
		try
		{
			String formTemplateTitle = "New Form Title";
			String formTemplateDescription = "New Form Description";
			FieldCollection defaultFieldsTopSection = new FieldCollection(StandardFieldSpecs.getDefaultTopSetionFieldSpecs().asArray());
			FieldCollection defaultFieldsBottomSection = new FieldCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray());
			CustomFieldTemplate template = new CustomFieldTemplate(formTemplateTitle, formTemplateDescription, defaultFieldsTopSection, defaultFieldsBottomSection);
			String formTemplateData = template.getExportedTemplateAsBase64String(clientSecurity);

			Vector formTemplateVectorForNetworkCall = new Vector();
			formTemplateVectorForNetworkCall.add(formTemplateData);
			String clientId = clientSecurity.getPublicKeyString();

			
			mock.serverForClients.clientsBanned.add(clientId);
			Vector result = mock.serverForClients.putFormTemplate(clientId, formTemplateVectorForNetworkCall);
			assertEquals("Client is banned should not accept form Template", REJECTED, result.get(0));
			result = mock.serverForClients.getListOfFormTemplates(clientId, clientId);
			assertEquals("Client is banned should not allow to retrieve a list of FormTemplates", REJECTED, result.get(0));
			result = mock.serverForClients.getFormTemplate(clientId, clientId, formTemplateTitle);
			assertEquals("Client is banned should not allow to retrieve a form", REJECTED, result.get(0));
			
			mock.serverForClients.clientsBanned.clear();
			mock.serverForClients.clientsThatCanUpload.clear();
			
			
			String unknownClientId = "Some Other Client Server Doesnt Have An Account For";
			assertFalse(mock.serverForClients.doesAccountExist(unknownClientId));
			assertFalse("Make sure calling doesAccountExist doesn't create the account as a side effect", mock.serverForClients.doesAccountExist(unknownClientId));

			result = mock.serverForClients.getListOfFormTemplates(clientId, unknownClientId);
			assertEquals("Account's templates trying to be retrieved doesn't exist on server should return ACCOUNT_NOT_FOUND",ACCOUNT_NOT_FOUND, result.get(0));
			
			result = mock.serverForClients.getListOfFormTemplates(clientId, clientId);
			assertEquals("Client is no longer banned and should be allowed to retrieve FormTemplates but there are no form templates to retrieve a list",OK, result.get(0));
			result = mock.serverForClients.getFormTemplate(clientId, clientId, formTemplateTitle);
			assertEquals("Client is no longer banned and should be allowed to retrieve FormTemplates but there are no form templates to retrieve",FORM_TEMPLATE_DOES_NOT_EXIST, result.get(0));
			
			mock.serverForClients.clientsThatCanUpload.add(clientId);
			result = mock.serverForClients.putFormTemplate(clientId, formTemplateVectorForNetworkCall);
			assertEquals("Client is no longer banned and is allowed to upload, should accept this template", OK, result.get(0));
			result = mock.serverForClients.getListOfFormTemplates(clientId, clientId);
			assertEquals("Client should retrive a list of forms",OK, result.get(0));
			NetworkResponse response = new NetworkResponse(result);
			Vector ObjectArrayFormTemplateTitlesAndDescriptions = response.getResultVector();
			Object[] innerResult = (Object[]) ObjectArrayFormTemplateTitlesAndDescriptions.get(0);
			Vector formTemplateTitlesAndDescriptions  = new Vector(Arrays.asList(innerResult));

			assertEquals("Didn't return same title of form?", formTemplateTitle, formTemplateTitlesAndDescriptions.get(0));
			assertEquals("Didn't return same description of form?", formTemplateDescription, formTemplateTitlesAndDescriptions.get(1));
			result = mock.serverForClients.getFormTemplate(clientId, clientId, "Some form which doesn't exist");
			assertEquals("Client should be unable to retrive a non-existent form",FORM_TEMPLATE_DOES_NOT_EXIST, result.get(0));

			result = mock.serverForClients.getFormTemplate(clientId, clientId, formTemplateTitle);
			assertEquals("Client should retrive the form requested",OK, result.get(0));
			assertEquals("No data returned?", 2, result.size());
			Object[] resultObject = (Object[])result.get(1);
			assertEquals(1, resultObject.length);
			String base64FormTemplateData = (String)resultObject[0];
			StringReader reader = new StringReader(base64FormTemplateData);			
			File formTemplateTempFile = File.createTempFile("$$$FormTemplate", null);
			formTemplateTempFile.deleteOnExit();
			FileOutputStream output = new FileOutputStream(formTemplateTempFile);
			StreamableBase64.decode(reader, output);
			output.flush();
			output.close();
			CustomFieldTemplate templateReturned = importFormTemplate(formTemplateTempFile);
			formTemplateTempFile.delete();
			assertEquals("Didn't return same title of form?", formTemplateTitle, templateReturned.getTitle());
			assertEquals("Didn't return same description of form?", formTemplateDescription, templateReturned.getDescription());
		}
		finally
		{
			mock.deleteAllFiles();
			DirectoryUtils.deleteEntireDirectoryTree(tmpPacketDir);
		}
		
		TRACE_END();
	}

	private CustomFieldTemplate importFormTemplate(File formTemplateTempFile) throws FutureVersionException, IOException 
	{
		FileInputStreamWithSeek inputStream = new FileInputStreamWithSeek(formTemplateTempFile);
		try
		{
			CustomFieldTemplate templateReturned = new CustomFieldTemplate();
			templateReturned.importTemplate(serverSecurity, inputStream);

			return templateReturned;
		}
		finally
		{
			inputStream.close();
		}
	}

	public void testPutContactInfoEncoded() throws Exception
	{
		TRACE_BEGIN("testPutContactInfoEncoded");

		Vector contactInfoManual = new Vector();
		String clientId = clientSecurity.getPublicKeyString();

		testServer.serverForClients.allowUploads(clientId, null);
		String resultIncomplete = testServer.putContactInfo(clientId, contactInfoManual);
		assertEquals("Empty ok?", INVALID_DATA, resultIncomplete);

		contactInfoManual.add(NetworkInterfaceConstants.BASE_64_ENCODED);
		contactInfoManual.add("bogus data");
		resultIncomplete = testServer.putContactInfo(clientId, contactInfoManual);
		assertEquals("Incorrect not Incomplete?", INVALID_DATA, resultIncomplete);
		
		contactInfoManual.clear();
		contactInfoManual.add(NetworkInterfaceConstants.BASE_64_ENCODED);
		contactInfoManual.add(clientId);
		contactInfoManual.add(new Integer(1));
		contactInfoManual.add("Data");
		contactInfoManual.add("invalid Signature");
		String invalidSig = testServer.putContactInfo(clientId, contactInfoManual);
		assertEquals("Invalid Signature", SIG_ERROR, invalidSig);		

		String author = "Author";
		String phoneNumber = "Phone number";
		ContactInfo contactInfo = new ContactInfo(author, "org", "email", "web", phoneNumber, "address");
		MartusCrypto signer = clientSecurity;
		Vector contactInfoEncoded = contactInfo.getSignedEncodedVector(signer);
		
		testServer.allowUploads("differentAccountID");
		String incorrectAccoutResult = testServer.putContactInfo("differentAccountID", contactInfoEncoded);
		assertEquals("Incorrect Accout ", INVALID_DATA, incorrectAccoutResult);		

		File contactFile = testServer.getWriteableDatabase().getContactInfoFile(clientId);
		assertFalse("Contact File already exists?", contactFile.exists());		

		Vector decodedContactInfo = ContactInfo.decodeContactInfoVectorIfNecessary(contactInfoEncoded);
		String correctResultWithDecodedContactInfoSent = testServer.putContactInfo(clientId, decodedContactInfo);
		assertEquals("Encoded Config Info Correct Signature", OK, correctResultWithDecodedContactInfoSent);		
		
		String correctResultWithEncodedContactInfoSend = testServer.putContactInfo(clientId, contactInfoEncoded);
		assertEquals("Encoded Config Info Correct Signature", OK, correctResultWithEncodedContactInfoSend);		
	
		
		assertTrue("File Doesn't exist?", contactFile.exists());
		assertTrue("Size too small", contactFile.length() > 200);

		FileInputStream contactFileInputStream = new FileInputStream(contactFile);
		DataInputStream in = new DataInputStream(contactFileInputStream);

		String inputPublicKey = in.readUTF();
		int inputDataCount = in.readInt();
		String authorInputData = in.readUTF();
		in.readUTF();
		in.readUTF();
		in.readUTF();
		String phoneInputData = in.readUTF();
		in.close();

		assertEquals("Public key doesn't match", clientId, inputPublicKey);
		assertEquals("data size not six?", 6, inputDataCount);
		assertEquals("Author not correct?", author, authorInputData);
		assertEquals("Phone Number not correct?", phoneNumber, phoneInputData);

		contactFile.delete();
		contactFile.getParentFile().delete();

		testServer.serverForClients.clientsBanned.add(clientId);
		String banned = testServer.putContactInfo(clientId, contactInfoEncoded);
		assertEquals("Client is banned should not accept contact info", REJECTED, banned);
		
		TRACE_END();
	}
	
	public void testPutContactInfoThroughHandler() throws Exception
	{
		TRACE_BEGIN("testPutContactInfoThroughHandler");

		String clientId = clientSecurity.getPublicKeyString();

		Vector parameters = new Vector();
		parameters.add(clientId);
		parameters.add(new Integer(1));
		parameters.add("Data");
		String signature = clientSecurity.createSignatureOfVectorOfStrings(parameters);
		parameters.add(signature);

		String sig = clientSecurity.createSignatureOfVectorOfStrings(parameters);

		testServer.allowUploads(clientId);
		Vector result = testServerInterface.putContactInfo(clientId, parameters, sig);
		File contactFile = testServer.getWriteableDatabase().getContactInfoFile(clientId);
		assertEquals("Result size?", 1, result.size());
		assertEquals("Result not ok?", OK, result.get(0));

		contactFile.delete();
		contactFile.getParentFile().delete();

		TRACE_END();
	}

	public void testGetContactInfo() throws Exception
	{
		TRACE_BEGIN("testGetContactInfo");

		String author = "Author";
		String phone = "Phone number";
		MartusCrypto signer = clientSecurity;
		ContactInfo contactInfo = new ContactInfo(author, "org", "email", "web", phone, "address");
		
		Vector encodedContactInfo = contactInfo.getSignedEncodedVector(signer);
		Vector decodedContactInfo = ContactInfo.decodeContactInfoVectorIfNecessary(encodedContactInfo);
		String clientId = clientSecurity.getPublicKeyString();
		String signature = (String)encodedContactInfo.get(encodedContactInfo.size()-1);

		Vector nothingReturned = testServer.getContactInfo(clientId);
		assertEquals("No contactInfo should return null", NetworkInterfaceConstants.ITEM_NOT_FOUND, nothingReturned.get(0));
		testServer.allowUploads(clientId);
		testServer.putContactInfo(clientId, encodedContactInfo);
		Vector infoReturned = testServer.getContactInfo(clientId);
		assertEquals("Should be ok", NetworkInterfaceConstants.OK, infoReturned.get(0));	
		Vector contactInfoReturnedEncoded = (Vector)infoReturned.get(1);
		Vector contactInfoReturnedDecoded = ContactInfo.decodeContactInfoVectorIfNecessary(contactInfoReturnedEncoded);
			
		assertEquals("Incorrect size Encoded",encodedContactInfo.size(), contactInfoReturnedEncoded.size());
		assertEquals("Incorrect size Decoded",decodedContactInfo.size(), contactInfoReturnedDecoded.size());
		assertEquals("Public key doesn't match", clientId, contactInfoReturnedDecoded.get(0));
		assertEquals("data size not two?", 6, ((Integer)contactInfoReturnedDecoded.get(1)).intValue());
		assertEquals("data not correct?", author, contactInfoReturnedDecoded.get(2));
		assertEquals("data2 not correct?", phone, contactInfoReturnedDecoded.get(6));
		assertEquals("signature doesn't match?", signature, contactInfoReturnedDecoded.get(8));		

		TRACE_END();
	}

	public void testGetAccountInformation() throws Exception
	{
		TRACE_BEGIN("testGetAccountInformation");

		testServer.setSecurity(serverSecurity);

		String knownAccountId = testServer.getAccountId();
		assertNotNull("null account?", knownAccountId);
		
		Vector serverInfo = testServer.getServerInformation();
		assertEquals(3, serverInfo.size());
		assertEquals(NetworkInterfaceConstants.OK, serverInfo.get(0));

		String accountId = (String)serverInfo.get(1);
		String sig = (String)serverInfo.get(2);

		assertEquals("Got wrong account back?", knownAccountId, accountId);
		verifyAccountInfo("bad test sig?", accountId, sig);

		TRACE_END();
	}
	
	public void testGetAccountInformationNoAccount() throws Exception
	{
		TRACE_BEGIN("testGetAccountInformationNoAccount");

		MockMartusServer serverWithoutKeypair = new MockMartusServer();
		serverWithoutKeypair.getSecurity().clearKeyPair();

		Vector errorInfo = serverWithoutKeypair.getServerInformation();
		assertEquals(2, errorInfo.size());
		assertEquals(NetworkInterfaceConstants.SERVER_ERROR, errorInfo.get(0));

		serverWithoutKeypair.deleteAllFiles();
		TRACE_END();
	}

	private void verifyAccountInfo(String label, String accountId, String sig) throws 
			UnsupportedEncodingException, 
			MartusSignatureException, 
			InvalidBase64Exception 
	{
		byte[] accountIdBytes = StreamableBase64.decode(accountId);

		ByteArrayInputStream in = new ByteArrayInputStream(accountIdBytes);
		byte[] expectedSig = serverSecurity.createSignatureOfStream(in);
		assertEquals(label + " encoded sig wrong?", StreamableBase64.encode(expectedSig), sig);

		ByteArrayInputStream dataInClient = new ByteArrayInputStream(accountIdBytes);
		boolean ok1 = clientSecurity.isValidSignatureOfStream(accountId, dataInClient, StreamableBase64.decode(sig));
		assertEquals(label + " client verifySig failed", true, ok1);

		ByteArrayInputStream dataInServer = new ByteArrayInputStream(accountIdBytes);
		boolean ok2 = serverSecurity.isValidSignatureOfStream(accountId, dataInServer, StreamableBase64.decode(sig));
		assertEquals(label + " server verifySig failed", true, ok2);
	}
	
	public void testUploadBulletinOneChunkOnly() throws Exception
	{
		TRACE_BEGIN("testUploadBulletinOneChunkOnly");

		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ZipBytes.length, b1ZipString, clientSecurity));

		ReadableDatabase db = testServer.getDatabase();
		assertNotNull("no database?", db);
		DatabaseKey key = DatabaseKey.createSealedKey(b1.getUniversalId());
		Bulletin got = BulletinLoader.loadFromDatabase(db, key, clientSecurity);
		assertEquals("id", b1.getLocalId(), got.getLocalId());

		assertEquals(NetworkInterfaceConstants.DUPLICATE, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ZipBytes.length, b1ZipString, clientSecurity));

		TRACE_END();
	}
	
	public void testUploadBulletinChunks() throws Exception
	{
		TRACE_BEGIN("testUploadBulletinChunks");

		ReadableDatabase serverDatabase = testServer.getDatabase();
		
		DatabaseKey headerKey = DatabaseKey.createSealedKey(b1.getUniversalId());
		DatabaseKey burKey = BulletinUploadRecord.getBurKey(headerKey);
		assertFalse("BUR already exists?", serverDatabase.doesRecordExist(burKey));

		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));

		assertTrue("BUR not created?", serverDatabase.doesRecordExist(burKey));
		TRACE_END();
	}	

	public void testUploadTwoBulletinsByChunks() throws Exception
	{
		TRACE_BEGIN("testUploadTwoBulletinsByChunks");

		String b2ZipString = BulletinForTesting.saveToZipString(getClientDatabase(), b2, clientSecurity);

		byte[] b2ZipBytes = StreamableBase64.decode(b2ZipString);
		byte[] b2ChunkBytes0 = new byte[100];
		byte[] b2ChunkBytes1 = new byte[b2ZipBytes.length - b2ChunkBytes0.length];
		System.arraycopy(b2ZipBytes, 0, b2ChunkBytes0, 0, b2ChunkBytes0.length);
		System.arraycopy(b2ZipBytes, b2ChunkBytes0.length, b2ChunkBytes1, 0, b2ChunkBytes1.length);
		String b2ChunkData0 = StreamableBase64.encode(b2ChunkBytes0);
		String b2ChunkData1 = StreamableBase64.encode(b2ChunkBytes1);

		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b2.getLocalId(), b2ZipBytes.length, 0, b2ChunkBytes0.length, b2ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b2.getLocalId(), b2ZipBytes.length, b2ChunkBytes0.length, b2ChunkBytes1.length, b2ChunkData1, clientSecurity));

		TRACE_END();
	}

	public void testUploadBulletinChunkAtZeroRestarts() throws Exception
	{
		TRACE_BEGIN("testUploadBulletinChunkAtZeroRestarts");

		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));

		TRACE_END();
	}

	public void testUploadBulletinChunkTooLarge() throws Exception
	{
		TRACE_BEGIN("testUploadBulletinChunkTooLarge");

		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE*2, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE*2, b1ChunkBytes0.length, NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE+1, b1ChunkData1, clientSecurity));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE+1, b1ChunkData1, clientSecurity));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));


		TRACE_END();
	}

	public void testUploadBulletinTotalSizeWrong() throws Exception
	{
		TRACE_BEGIN("testUploadBulletinTotalSizeWrong");

		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), 90, 0, 100, "", clientSecurity));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length-1, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));

		TRACE_END();
	}
	
	public void testUploadBulletinChunkInvalidOffset() throws Exception
	{
		TRACE_BEGIN("testUploadBulletinChunkInvalidOffset");

		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		assertEquals("1 chunk invalid offset -1",NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, -1, b1ZipBytes.length, b1ZipString, clientSecurity));
		assertEquals("1 chunk invalid offset 1",NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 1, b1ZipBytes.length, b1ZipString, clientSecurity));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length-1, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));
			
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length+1, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));

		TRACE_END();
	}
	
	public void testUploadBulletinChunkDataLengthIncorrect() throws Exception
	{
		TRACE_BEGIN("testUploadBulletinChunkDataLengthIncorrect");

		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length-1, b1ChunkData1, clientSecurity));
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length+1, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));

		TRACE_END();
	}
	
	public void testUploadChunkBadRequestSignature() throws Exception
	{
		TRACE_BEGIN("testUploadChunkBadRequestSignature");

		testServer.serverForClients.clearCanUploadList();
		String authorId = clientSecurity.getPublicKeyString();
		testServer.allowUploads(authorId);

		String localId = b1.getLocalId();
		int totalLength = b1ZipBytes.length;
		int chunkLength = b1ChunkBytes0.length;
		assertEquals("allowed bad sig?", NetworkInterfaceConstants.SIG_ERROR, testServer.uploadBulletinChunk(authorId, localId, totalLength, 0, chunkLength, b1ChunkData0, "123"));

		String stringToSign = authorId + "," + localId + "," + Integer.toString(totalLength) + "," + 
					Integer.toString(0) + "," + Integer.toString(chunkLength) + "," + b1ChunkData0;
		byte[] bytesToSign = stringToSign.getBytes("UTF-8");
		byte[] sigBytes = serverSecurity.createSignatureOfStream(new ByteArrayInputStream(bytesToSign));
		String signature = StreamableBase64.encode(sigBytes);
		assertEquals("allowed wrong sig?", NetworkInterfaceConstants.SIG_ERROR, testServer.uploadBulletinChunk(authorId, localId, totalLength, 0, chunkLength, b1ChunkData0, signature));

		TRACE_END();
	}
	
	public void testUploadChunkIOError()
	{
		//TODO implement this
		//Should return SERVER_ERROR not INVALID_DATA;
	}

	public void testUploadDraft() throws Exception
	{
		TRACE_BEGIN("testUploadDraft");

		Bulletin draftBulletin = new Bulletin(clientSecurity);
		draftBulletin.set(Bulletin.TAGTITLE, "Title1");
		draftBulletin.set(Bulletin.TAGPUBLICINFO, "Details1");
		draftBulletin.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails1");
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(hqSecurity.getPublicKeyString());
		keys.add(key1);
		draftBulletin.setAuthorizedToReadKeys(keys);
		store.saveEncryptedBulletinForTesting(draftBulletin);
		draftBulletin = BulletinLoader.loadFromDatabase(getClientDatabase(), DatabaseKey.createDraftKey(draftBulletin.getUniversalId()), clientSecurity);
		String draftZipString = BulletinForTesting.saveToZipString(getClientDatabase(), draftBulletin, clientSecurity);
		byte[] draftZipBytes = StreamableBase64.decode(draftZipString);

		MockMartusServer tempServer = new MockMartusServer(new MockDraftDatabase());
		try
		{
			tempServer.setSecurity(serverSecurity);
			tempServer.serverForClients.loadBannedClients();
			tempServer.allowUploads(clientSecurity.getPublicKeyString());
			assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(tempServer, clientSecurity.getPublicKeyString(), draftBulletin.getLocalId(), draftZipBytes.length, 0, draftZipBytes.length, draftZipString, clientSecurity));
		}
		finally
		{
			tempServer.deleteAllFiles();
		}

		TRACE_END();
	}
	
	public void testUploadSealedStatus() throws Exception
	{
		TRACE_BEGIN("testUploadSealedStatus");

		MockMartusServer tempServer = new MockMartusServer(new MockSealedDatabase());
		try
		{
			tempServer.setSecurity(serverSecurity);
			tempServer.serverForClients.loadBannedClients();
			tempServer.allowUploads(clientSecurity.getPublicKeyString());
			assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(tempServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ZipBytes.length, b1ZipString, clientSecurity));
		}
		finally
		{
			tempServer.deleteAllFiles();
		}

		TRACE_END();
	}

	public void testUploadDuplicates() throws Exception
	{
		TRACE_BEGIN("testUploadDuplicates");

		Bulletin b = new Bulletin(clientSecurity);
		b.set(Bulletin.TAGTITLE, "Title1");
		b.set(Bulletin.TAGPUBLICINFO, "Details1");
		b.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails1");
		File attachment = createTempFile();
		FileOutputStream out = new FileOutputStream(attachment);
		out.write(b1AttachmentBytes);
		out.close();
		b.addPublicAttachment(new AttachmentProxy(attachment));
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(hqSecurity.getPublicKeyString());
		keys.add(key1);
		b.setAuthorizedToReadKeys(keys);
		b.setDraft();
		store.saveEncryptedBulletinForTesting(b);
		b = BulletinLoader.loadFromDatabase(getClientDatabase(), DatabaseKey.createDraftKey(b.getUniversalId()), clientSecurity);
		UniversalId attachmentUid1 = b.getPublicAttachments()[0].getUniversalId();
		DatabaseKey draftHeader1 = DatabaseKey.createDraftKey(b.getUniversalId());
		DatabaseKey attachmentKey1 = DatabaseKey.createDraftKey(attachmentUid1);
		String draft1ZipString = BulletinForTesting.saveToZipString(getClientDatabase(), b, clientSecurity);
		byte[] draft1ZipBytes = StreamableBase64.decode(draft1ZipString);

		b.clearPublicAttachments();
		FileOutputStream out2 = new FileOutputStream(attachment);
		out2.write(b1AttachmentBytes);
		out2.close();
		b.addPublicAttachment(new AttachmentProxy(attachment));
		store.saveEncryptedBulletinForTesting(b);
		b = BulletinLoader.loadFromDatabase(getClientDatabase(), DatabaseKey.createDraftKey(b.getUniversalId()), clientSecurity);
		UniversalId attachmentUid2 = b.getPublicAttachments()[0].getUniversalId();
		DatabaseKey attachmentKey2 = DatabaseKey.createDraftKey(attachmentUid2);
		DatabaseKey draftHeader2 = DatabaseKey.createDraftKey(b.getUniversalId());
		draftHeader2.setDraft();
		attachmentKey2.setDraft();
		String draft2ZipString = BulletinForTesting.saveToZipString(getClientDatabase(),b, clientSecurity);
		byte[] draft2ZipBytes = StreamableBase64.decode(draft2ZipString);

		b.clearPublicAttachments();
		FileOutputStream out3 = new FileOutputStream(attachment);
		out3.write(b1AttachmentBytes);
		out3.close();
		b.addPublicAttachment(new AttachmentProxy(attachment));
		b.setSealed();
		store.saveEncryptedBulletinForTesting(b);
		b = BulletinLoader.loadFromDatabase(getClientDatabase(), DatabaseKey.createSealedKey(b.getUniversalId()), clientSecurity);
		UniversalId attachmentUid3 = b.getPublicAttachments()[0].getUniversalId();
		DatabaseKey attachmentKey3 = DatabaseKey.createSealedKey(attachmentUid3);
		DatabaseKey sealedHeader3 = DatabaseKey.createSealedKey(b.getUniversalId());
		sealedHeader3.setSealed();
		attachmentKey3.setSealed();
		String sealedZipString = BulletinForTesting.saveToZipString(getClientDatabase(), b, clientSecurity);
		byte[] sealedZipBytes = StreamableBase64.decode(sealedZipString);

		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		ReadableDatabase serverDatabase = testServer.getDatabase();

		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServer, 
			clientSecurity.getPublicKeyString(), b.getLocalId(), draft1ZipBytes.length, 0, 
			draft1ZipBytes.length, draft1ZipString, clientSecurity));

		assertEquals("Attachment 1 does not exists?", true, serverDatabase.doesRecordExist(attachmentKey1));
		assertEquals("Attachment 2 exists?", false, serverDatabase.doesRecordExist(attachmentKey2));
		assertEquals("Attachment 3 exists?", false, serverDatabase.doesRecordExist(attachmentKey3));
		assertEquals("Header 1 does not exists?", true, serverDatabase.doesRecordExist(draftHeader1));
		assertEquals("Header 2 does not exists?", true, serverDatabase.doesRecordExist(draftHeader2));
		assertEquals("Header 3 exists?", false, serverDatabase.doesRecordExist(sealedHeader3));

		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServer, 
			clientSecurity.getPublicKeyString(), b.getLocalId(), draft2ZipBytes.length, 0, 
			draft2ZipBytes.length, draft2ZipString, clientSecurity));

		assertEquals("Attachment 1 still exists?", false, serverDatabase.doesRecordExist(attachmentKey1));
		assertEquals("Attachment 2 does not exists?", true, serverDatabase.doesRecordExist(attachmentKey2));
		assertEquals("Attachment 3 exists?", false, serverDatabase.doesRecordExist(attachmentKey3));
		assertEquals("Header 1 does not exists?", true, serverDatabase.doesRecordExist(draftHeader1));
		assertEquals("Header 2 does not exists?", true, serverDatabase.doesRecordExist(draftHeader2));
		assertEquals("Header 3 exists?", false, serverDatabase.doesRecordExist(sealedHeader3));

		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServer, 
			clientSecurity.getPublicKeyString(), b.getLocalId(), sealedZipBytes.length, 0, 
			sealedZipBytes.length, sealedZipString, clientSecurity));

		assertEquals("Attachment 1 still exists?", false, serverDatabase.doesRecordExist(attachmentKey1));
		assertEquals("Attachment 2 still exists?", false, serverDatabase.doesRecordExist(attachmentKey2));
		assertEquals("Attachment 3 does not exist?", true, serverDatabase.doesRecordExist(attachmentKey3));
		assertEquals("Header 1 exists?", false, serverDatabase.doesRecordExist(draftHeader1));
		assertEquals("Header 2 exists?", false, serverDatabase.doesRecordExist(draftHeader2));
		assertEquals("Header 3 does not exists?", true, serverDatabase.doesRecordExist(sealedHeader3));

		assertEquals(NetworkInterfaceConstants.SEALED_EXISTS, uploadBulletinChunk(testServer, 
			clientSecurity.getPublicKeyString(), b.getLocalId(), draft1ZipBytes.length, 0, 
			draft1ZipBytes.length, draft1ZipString, clientSecurity));

		assertEquals(NetworkInterfaceConstants.DUPLICATE, uploadBulletinChunk(testServer, 
			clientSecurity.getPublicKeyString(), b.getLocalId(), sealedZipBytes.length, 0, 
			sealedZipBytes.length, sealedZipString, clientSecurity));

		TRACE_END();
	}

	public void testBadlySignedBulletinUpload() throws Exception
	{
		TRACE_BEGIN("testBadlySignedBulletinUpload");

		testServer.allowUploads(clientSecurity.getPublicKeyString());
		MockMartusSecurity mockServerSecurity = MockMartusSecurity.createServer();
		mockServerSecurity.fakeSigVerifyFailure = true;
		testServer.setSecurity(mockServerSecurity);

		assertEquals("didn't verify sig?", NetworkInterfaceConstants.SIG_ERROR, testServer.uploadBulletin(clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipString));

		assertEquals("didn't verify sig for 1 chunk?", NetworkInterfaceConstants.SIG_ERROR, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ZipBytes.length, b1ZipString, clientSecurity));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals("didn't verify sig for chunks?", NetworkInterfaceConstants.SIG_ERROR, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));

		testServer.setSecurity(serverSecurity);
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));

		TRACE_END();
	}
		
	public void testInvalidDataUpload() throws Exception
	{
		TRACE_BEGIN("testInvalidDataUpload");

		String authorClientId = clientSecurity.getPublicKeyString();
		testServer.allowUploads(authorClientId);
		String bulletinLocalId = b1.getLocalId();

		assertEquals("not base64", NetworkInterfaceConstants.INVALID_DATA, testServer.uploadBulletin(authorClientId, bulletinLocalId, "not a valid bulletin!"));

		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals("not base64 chunk", NetworkInterfaceConstants.INVALID_DATA, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, 100, "not a valid bulletin!", clientSecurity));
		assertEquals(NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, 0, b1ChunkBytes0.length, b1ChunkData0, clientSecurity));
		assertEquals(NetworkInterfaceConstants.OK, uploadBulletinChunk(testServer, clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipBytes.length, b1ChunkBytes0.length, b1ChunkBytes1.length, b1ChunkData1, clientSecurity));

		assertEquals("empty fullupload", NetworkInterfaceConstants.INVALID_DATA, testServer.uploadBulletin(authorClientId, bulletinLocalId, ""));
		assertEquals("empty chunk", NetworkInterfaceConstants.CHUNK_OK, uploadBulletinChunk(testServer, authorClientId, bulletinLocalId, 1, 0, 0, "", clientSecurity));
		
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		ZipOutputStream zipWithBadEntry = new ZipOutputStream(out2);
		ZipEntry badEntry = new ZipEntry("blah");
		zipWithBadEntry.putNextEntry(badEntry);
		zipWithBadEntry.write(5);
		zipWithBadEntry.close();
		String zipWithBadEntryString = StreamableBase64.encode(out2.toByteArray());
		assertEquals("zip bad entry", NetworkInterfaceConstants.INVALID_DATA, testServer.uploadBulletin(authorClientId, "yah", zipWithBadEntryString));

		TRACE_END();
	}

	
	public void testExtractPacketsToZipStream() throws Exception
	{
		TRACE_BEGIN("testDownloadFieldOfficeBulletinChunkNotAuthorized");

		uploadSampleBulletin();
		DatabaseKey[] packetKeys = BulletinZipUtilities.getAllPacketKeys(b1.getBulletinHeaderPacket());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BulletinZipUtilities.extractPacketsToZipStream(testServer.getDatabase(), packetKeys, out, serverSecurity);
		assertEquals("wrong length?", b1ZipBytes.length, out.toByteArray().length);
		
		String zipString = StreamableBase64.encode(out.toByteArray());
		assertEquals("zips different?", getZipEntryNamesAndCrcs(b1ZipString), getZipEntryNamesAndCrcs(zipString));

		TRACE_END();
	}

	String getZipEntryNamesAndCrcs(String zipString) throws 
		IOException, 
		InvalidBase64Exception, 
		ZipException 
	{
		String result = "";
		File tempZipFile = StreamableBase64.decodeToTempFile(zipString);
		ZipFile zip = new ZipFile(tempZipFile);
		Enumeration entries = zip.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			result += entry.getName();
			result += ":";
			result += new Long(entry.getCrc()).toString();
			result += ",";
		}
		zip.close();
		return result;
	}
		
	public void testGetMyBulletin() throws Exception
	{
		TRACE_BEGIN("testGetMyBulletin");

		testServer.setSecurity(serverSecurity);
		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		testServer.uploadBulletin(clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipString);
		
		Vector result = getBulletinChunk(clientSecurity, testServerInterface, b1.getAccount(), b1.getLocalId(), 0, NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE);
		assertEquals("Failed? I am the author", NetworkInterfaceConstants.OK, result.get(0));

		TRACE_END();
	}


	public void testListFieldOfficeAccounts() throws Exception
	{
		TRACE_BEGIN("testListFieldOfficeAccounts");

		testServer.setSecurity(serverSecurity);

		BulletinStore otherStore = new MockBulletinStore(this);
		MartusCrypto nonFieldSecurity = MockMartusSecurity.createClient();
		testServer.allowUploads(nonFieldSecurity.getPublicKeyString());

		Bulletin b = new Bulletin(nonFieldSecurity);
		b.set(Bulletin.TAGTITLE, "Tifdfssftle3");
		b.set(Bulletin.TAGPUBLICINFO, "Detasdfsdfils1");
		b.set(Bulletin.TAGPRIVATEINFO, "PrivasdfsdfteDetails1");
		otherStore.saveEncryptedBulletinForTesting(b);
		testServer.uploadBulletin(nonFieldSecurity.getPublicKeyString(), b.getLocalId(), BulletinForTesting.saveToZipString(getClientDatabase(), b, clientSecurity));

		Vector list1 = testServer.listFieldOfficeAccounts(hqSecurity.getPublicKeyString());
		assertNotNull("listFieldOfficeAccounts returned null", list1);
		assertEquals("wrong length", 1, list1.size());
		assertNotNull("null id1 [0]", list1.get(0));
		assertEquals(NetworkInterfaceConstants.OK, list1.get(0));

		MartusCrypto fieldSecurity1 = clientSecurity;
		testServer.allowUploads(fieldSecurity1.getPublicKeyString());
		Bulletin bulletin = new Bulletin(clientSecurity);
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(hqSecurity.getPublicKeyString());
		HeadquartersKey key2 = new HeadquartersKey(otherServerSecurity.getPublicKeyString());
		keys.add(key1);
		keys.add(key2);
		bulletin.setAuthorizedToReadKeys(keys);
		store.saveEncryptedBulletinForTesting(bulletin);
		testServer.uploadBulletin(fieldSecurity1.getPublicKeyString(), bulletin.getLocalId(), BulletinForTesting.saveToZipString(getClientDatabase(), bulletin, clientSecurity));

		privateBulletin.setAuthorizedToReadKeys(keys);
		store.saveEncryptedBulletinForTesting(privateBulletin);
		testServer.uploadBulletin(fieldSecurity1.getPublicKeyString(), privateBulletin.getLocalId(), BulletinForTesting.saveToZipString(getClientDatabase(), privateBulletin, clientSecurity));
				
		Vector list2 = testServer.listFieldOfficeAccounts(hqSecurity.getPublicKeyString());
		assertEquals("wrong length", 2, list2.size());
		assertEquals(NetworkInterfaceConstants.OK, list2.get(0));
		assertEquals("Wrong Key?", fieldSecurity1.getPublicKeyString(), list2.get(1));
		
		Vector list3 = testServer.listFieldOfficeAccounts(otherServerSecurity.getPublicKeyString());
		assertEquals("wrong length hq2", 2, list3.size());
		assertEquals(NetworkInterfaceConstants.OK, list3.get(0));
		assertEquals("Wrong Key hq2?", fieldSecurity1.getPublicKeyString(), list3.get(1));

		TRACE_END();
	}

	public void testKeyBelongsToClient()
	{
		TRACE_BEGIN("testKeyBelongsToClient");

		UniversalId uid = UniversalId.createFromAccountAndLocalId("a", "b");
		DatabaseKey key = DatabaseKey.createSealedKey(uid);
		assertEquals("doesn't belong ", false, MartusServer.keyBelongsToClient(key, "b"));
		assertEquals("belongs ", true, MartusServer.keyBelongsToClient(key, "a"));

		TRACE_END();
	}

	public void testAllowUploads() throws Exception
	{
		TRACE_BEGIN("testAllowUploads");

		File uploadsFile = testServer.serverForClients.getAllowUploadFile();

		testServer.serverForClients.clearCanUploadList();
		uploadsFile.delete();
		assertFalse("Couldn't delete uploadsok?", uploadsFile.exists());

		String clientId = "some client";
		String clientId2 = "another client";

		assertEquals("clientId default", true, testServer.canClientUpload(clientId));
		assertEquals("clientId2 default", true, testServer.canClientUpload(clientId2));
		assertEquals("empty default", true, testServer.canClientUpload(""));

		testServer.allowUploads(clientId);
		assertEquals("clientId in", true, testServer.canClientUpload(clientId));
		assertEquals("clientId2 still not in", true, testServer.canClientUpload(clientId2));
		assertEquals("empty still out", true, testServer.canClientUpload(""));

		testServer.allowUploads(clientId2);
		assertEquals("clientId2", true, testServer.canClientUpload(clientId2));
		assertEquals("clientId still", true, testServer.canClientUpload(clientId));

		uploadsFile.delete();
		assertFalse("Couldn't delete uploadsok?", uploadsFile.exists());

		testServer.deleteAllFiles();

		TRACE_END();
	}

	public void testLoadUploadList() throws Exception
	{
		TRACE_BEGIN("testLoadUploadList");

		testServer.serverForClients.clearCanUploadList();

		String clientId = "some client";
		String clientId2 = "another client";
		assertEquals("clientId default", true, testServer.canClientUpload(clientId));
		assertEquals("clientId2 default", true, testServer.canClientUpload(clientId2));

		String testFileContents = "blah blah\n" + clientId2 + "\nYeah yeah\n\n";
		testServer.serverForClients.loadCanUploadList(new BufferedReader(new StringReader(testFileContents)));
		assertEquals("clientId still out", true, testServer.canClientUpload(clientId));
		assertEquals("clientId2 now in", true, testServer.canClientUpload(clientId2));
		assertEquals("empty still out", true, testServer.canClientUpload(""));

		File uploadsFile = testServer.serverForClients.getAllowUploadFile();
		uploadsFile.delete();
		assertFalse("Couldn't delete uploadsok?", uploadsFile.exists());

		TRACE_END();
	}
	
	public void testAllowUploadsWritingToDisk() throws Exception
	{
		TRACE_BEGIN("testAllowUploadsWritingToDisk");

		testServer.serverForClients.clearCanUploadList();
		
		String clientId1 = "slidfj";
		String clientId2 = "woeiruwe";
		testServer.allowUploads(clientId1);
		testServer.allowUploads(clientId2);
		File allowUploadFile = testServer.serverForClients.getAllowUploadFile();
		long lastUpdate = allowUploadFile.lastModified();
		//Thread.sleep(1000);
		
		boolean got1 = false;
		boolean got2 = false;
		UnicodeReader reader = new UnicodeReader(allowUploadFile);
		while(true)
		{
			String line = reader.readLine();
			if(line == null)
				break;
				
			if(line.equals(clientId1))
				got1 = true;
			else if(line.equals(clientId2))
				got2 = true;
			else
				fail("unknown id found!");
		}
		reader.close();
		assertTrue("missing id1?", got1);
		assertTrue("missing id2?", got2);
		
		BufferedReader reader2 = new BufferedReader(new UnicodeReader(allowUploadFile));
		testServer.serverForClients.loadCanUploadList(reader2);
		reader2.close();
		assertEquals("reading changed the file?", lastUpdate, allowUploadFile.lastModified());
		
		TRACE_END();
	}
		
	public void testAllowUploadsAuthorizeLogWritingToDisk() throws Exception
	{
		TRACE_BEGIN("testAllowUploadsAuthorizeLogWritingToDisk");

		testServer.serverForClients.clearCanUploadList();
		
		String clientId1 = "slidfj";
		String clientId2 = "woeiruwe";
		String magicWordUsed = "magic";
		testServer.serverForClients.allowUploads(clientId1,magicWordUsed);
		testServer.serverForClients.allowUploads(clientId2,magicWordUsed);
		File authorizeLogFile = testServer.serverForClients.getAuthorizeLogFile();
		
		UnicodeReader reader = new UnicodeReader(authorizeLogFile);
		boolean got1 = false;
		boolean got2 = false;
		while(true)
		{
			String line = reader.readLine();
			if(line == null)
				break;
			if(line.indexOf(testServer.getPublicCode(clientId1))>0)
				got1 = true;
			if(line.indexOf(testServer.getPublicCode(clientId2))>0)
				got2 = true;
		}
		reader.close();
		assertTrue("missing id1?", got1);
		assertTrue("missing id2?", got2);
		
		TRACE_END();
	}
	
	public void testRequestUploadRights() throws Exception
	{
		TRACE_BEGIN("testRequestUploadRights");

		String sampleId = "384759896";
		String sampleMagicWord = "bliflfji";

		testServer.serverForClients.clearCanUploadList();
		testServer.serverForClients.addMagicWordForTesting(sampleMagicWord,null);
		
		assertEquals("any upload attemps?", 0, testServer.getNumFailedUploadRequestsForIp(MockMartusServer.CLIENT_IP_ADDRESS));
		
		String failed = testServer.requestUploadRights(sampleId, sampleMagicWord + "x");
		assertEquals("didn't work?", NetworkInterfaceConstants.REJECTED, failed);
		assertEquals("incorrect upload attempt noted?", 1, testServer.getNumFailedUploadRequestsForIp(MockMartusServer.CLIENT_IP_ADDRESS));
		
		String worked = testServer.requestUploadRights(sampleId, sampleMagicWord);
		assertEquals("didn't work?", NetworkInterfaceConstants.OK, worked);

		TRACE_END();
	}
		
	public void testTooManyUploadRequests() throws Exception
	{
		TRACE_BEGIN("testTooManyUploadRequests");

		String sampleId = "384759896";
		String sampleMagicWord = "bliflfji";

		testServer.serverForClients.clearCanUploadList();
		testServer.serverForClients.addMagicWordForTesting(sampleMagicWord,null);
		
		assertEquals("counter 1?", 0, testServer.getNumFailedUploadRequestsForIp(MockMartusServer.CLIENT_IP_ADDRESS));
		
		String result = testServer.requestUploadRights(sampleId, sampleMagicWord + "x");
		assertEquals("upload request 1?", NetworkInterfaceConstants.REJECTED, result);
		assertEquals("counter 2?", 1, testServer.getNumFailedUploadRequestsForIp(MockMartusServer.CLIENT_IP_ADDRESS));
		
		result = testServer.requestUploadRights(sampleId, sampleMagicWord);
		assertEquals("upload request 2?", NetworkInterfaceConstants.OK, result);
		
		result = testServer.requestUploadRights(sampleId, sampleMagicWord + "x");
		assertEquals("upload request 3?", NetworkInterfaceConstants.REJECTED, result);
		assertEquals("counter 3?", 2, testServer.getNumFailedUploadRequestsForIp(MockMartusServer.CLIENT_IP_ADDRESS));
		
		result = testServer.requestUploadRights(sampleId, sampleMagicWord);
		assertEquals("upload request 4?", NetworkInterfaceConstants.SERVER_ERROR, result);
		
		result = testServer.requestUploadRights(sampleId, sampleMagicWord + "x");
		assertEquals("upload request 5?", NetworkInterfaceConstants.SERVER_ERROR, result);
		assertEquals("counter 4?", 3, testServer.getNumFailedUploadRequestsForIp(MockMartusServer.CLIENT_IP_ADDRESS));
		
		testServer.subtractMaxFailedUploadAttemptsFromServerCounter();
		
		assertEquals("counter 5?", 1, testServer.getNumFailedUploadRequestsForIp(MockMartusServer.CLIENT_IP_ADDRESS));
		
		result = testServer.requestUploadRights(sampleId, sampleMagicWord);
		assertEquals("upload request 6?", NetworkInterfaceConstants.OK, result);
		
		result = testServer.requestUploadRights(sampleId, sampleMagicWord+ "x");
		assertEquals("upload request 7?", NetworkInterfaceConstants.REJECTED, result);
		assertEquals("counter 6?", 2, testServer.getNumFailedUploadRequestsForIp(MockMartusServer.CLIENT_IP_ADDRESS));

		TRACE_END();
	}
	
	public void testGetAllPacketKeysSealed() throws Exception
	{
		TRACE_BEGIN("testGetAllPacketKeysSealed");

		BulletinHeaderPacket bhp = b1.getBulletinHeaderPacket();
		DatabaseKey[] keys = BulletinZipUtilities.getAllPacketKeys(bhp);
		
		assertNotNull("null ids?", keys);
		assertEquals("count?", 5, keys.length);
		boolean foundHeader = false;
		boolean foundPublicData = false;
		boolean foundPrivateData = false;
		boolean foundPublicAttachment = false;
		boolean foundPrivateAttachment = false;

		for(int i=0; i < keys.length; ++i)
		{
			assertEquals("Key " + i + " not sealed?", true, keys[i].isSealed());
			if(keys[i].getLocalId().equals(bhp.getLocalId()))
				foundHeader = true;
			if(keys[i].getLocalId().equals(bhp.getFieldDataPacketId()))
				foundPublicData = true;
			if(keys[i].getLocalId().equals(bhp.getPrivateFieldDataPacketId()))
				foundPrivateData = true;
			if(keys[i].getLocalId().equals(bhp.getPublicAttachmentIds()[0]))
				foundPublicAttachment = true;
			if(keys[i].getLocalId().equals(bhp.getPrivateAttachmentIds()[0]))
				foundPrivateAttachment = true;
		
		}		
		assertTrue("header id?", foundHeader);
		assertTrue("data id?", foundPublicData);
		assertTrue("private id?", foundPrivateData);
		assertTrue("attachment public id?", foundPublicAttachment);
		assertTrue("attachment private id?", foundPrivateAttachment);

		TRACE_END();
	}
		
	public void testGetAllPacketKeysDraft() throws Exception
	{
		TRACE_BEGIN("testGetAllPacketKeysDraft");

		BulletinHeaderPacket bhp = draft.getBulletinHeaderPacket();
		DatabaseKey[] keys = BulletinZipUtilities.getAllPacketKeys(bhp);
		
		assertNotNull("null ids?", keys);
		assertEquals("count?", 3, keys.length);
		boolean foundHeader = false;
		boolean foundPublicData = false;
		boolean foundPrivateData = false;

		for(int i=0; i < keys.length; ++i)
		{
			assertEquals("Key " + i + " not draft?", true, keys[i].isDraft());
			if(keys[i].getLocalId().equals(bhp.getLocalId()))
				foundHeader = true;
			if(keys[i].getLocalId().equals(bhp.getFieldDataPacketId()))
				foundPublicData = true;
			if(keys[i].getLocalId().equals(bhp.getPrivateFieldDataPacketId()))
				foundPrivateData = true;
		
		}		
		assertTrue("header id?", foundHeader);
		assertTrue("data id?", foundPublicData);
		assertTrue("private id?", foundPrivateData);

		TRACE_END();
	}

	public void testAuthenticateServer() throws Exception
	{
		TRACE_BEGIN("testAuthenticateServer");

		String notBase64 = "this is not base 64 ";
		String result = testServer.authenticateServer(notBase64);
		assertEquals("error not correct?", NetworkInterfaceConstants.INVALID_DATA, result);

		MockMartusServer server = new MockMartusServer();
		server.setSecurity(new MartusSecurity());
		String base64data = StreamableBase64.encode(new byte[]{1,2,3});
		result = server.authenticateServer(base64data);
		assertEquals("did not return server error?", NetworkInterfaceConstants.SERVER_ERROR, result);

		server.setSecurity(serverSecurity);
		result = server.authenticateServer(base64data);
		byte[] signature = StreamableBase64.decode(result);
		InputStream in = new ByteArrayInputStream(StreamableBase64.decode(base64data));
		assertTrue("Invalid signature?", clientSecurity.isValidSignatureOfStream(server.getSecurity().getPublicKeyString(), in, signature));
		
		server.deleteAllFiles();

		TRACE_END();
	}
	
	public void testServerShutdown() throws Exception
	{
		TRACE_BEGIN("testServerShutdown");

		String clientId = clientSecurity.getPublicKeyString();
		String bogusStringParameter = "this is never used in this call. right?";

		ServerForClients serverForClients = testServer.serverForClients;		
		assertEquals("isShutdownRequested 1", false, testServer.isShutdownRequested());
		
		assertEquals("testServerShutdown: incrementActiveClientsCounter 1", 0, serverForClients.getNumberActiveClients() );
		
		testServer.serverForClients.clientConnectionStart(clientId);
		assertEquals("testServerShutdown: incrementActiveClientsCounter 2", 1, serverForClients.getNumberActiveClients() );
		File exitFile = testServer.getShutdownFile();
		exitFile.createNewFile();
		
		testServer.allowUploads(clientId);

		assertEquals("isShutdownRequested 2", true, testServer.isShutdownRequested());

		Vector vecResult = null; 
		String strResult = testServer.requestUploadRights(clientId, bogusStringParameter);
		assertEquals("requestUploadRights", NetworkInterfaceConstants.SERVER_DOWN, strResult );
		assertEquals("requestUploadRights", 1, serverForClients.getNumberActiveClients() );
		
		strResult = uploadBulletinChunk(testServer, clientId, bogusStringParameter, 0, 0, 0, bogusStringParameter, clientSecurity);
		assertEquals("uploadBulletinChunk", NetworkInterfaceConstants.SERVER_DOWN, strResult );
		assertEquals("uploadBulletinChunk", 1, serverForClients.getNumberActiveClients() );

		strResult = testServer.putBulletinChunk(clientId, clientId, bogusStringParameter, 0, 0, 0, bogusStringParameter);
		assertEquals("putBulletinChunk", NetworkInterfaceConstants.SERVER_DOWN, strResult);
		assertEquals("putBulletinChunk", 1, serverForClients.getNumberActiveClients() );

		vecResult = testServer.getBulletinChunk(clientId, clientId, bogusStringParameter, 0, 0);
		verifyErrorResult("getBulletinChunk", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("getBulletinChunk", 1, serverForClients.getNumberActiveClients() );

		vecResult = testServer.getPacket(clientId, bogusStringParameter, bogusStringParameter, bogusStringParameter);
		verifyErrorResult("getPacket", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("getPacket", 1, serverForClients.getNumberActiveClients() );

		strResult = testServer.serverForClients.deleteDraftBulletins(clientId, new Vector(), "some signature");
		assertEquals("deleteDraftBulletins", NetworkInterfaceConstants.SERVER_DOWN, strResult);
		assertEquals("deleteDraftBulletins", 1, serverForClients.getNumberActiveClients() );

		strResult = testServer.putContactInfo(clientId, new Vector() );
		assertEquals("putContactInfo", NetworkInterfaceConstants.SERVER_DOWN, strResult);		
		assertEquals("putContactInfo", 1, serverForClients.getNumberActiveClients() );

		vecResult = testServer.listFieldOfficeAccounts(clientId);
		verifyErrorResult("listFieldOfficeAccounts", vecResult, NetworkInterfaceConstants.SERVER_DOWN );
		assertEquals("listFieldOfficeAccounts", 1, serverForClients.getNumberActiveClients() );

		exitFile.delete();

		assertEquals("isShutdownRequested 3", false, testServer.isShutdownRequested());
				
		testServer.serverForClients.clientConnectionExit();
		assertEquals("testServerShutdown: clientCount", 0, serverForClients.getNumberActiveClients() );	

		TRACE_END();
	}
	
	public void testServerConnectionDuringShutdown() throws Exception
	{
		TRACE_BEGIN("testServerConnectionDuringShutdown");

		Vector reply;
		
		testServer.serverForClients.clientConnectionStart(null);
		File exitFile = testServer.getShutdownFile();
		exitFile.createNewFile();
		
		reply = testServer.getServerInformation();
		assertEquals("getServerInformation", NetworkInterfaceConstants.SERVER_DOWN, reply.get(0) );

		exitFile.delete();
		testServer.serverForClients.clientConnectionExit();

		TRACE_END();
	}

	public void testShiftToDevelopmentPortsIfRequested() throws Exception
	{
		int ports[] = {1,2};
		
		MockMartusServer mainServer = new MockMartusServer();
		
		int[] developmentLinuxPorts = mainServer.shiftToDevelopmentPortsIfNotInSecureMode(ports);
		for(int i=0; i < ports.length; ++i)
			assertEquals("didn't shift? " + i, ports[i]+ServerCallbackInterface.DEVELOPMENT_MODE_PORT_DELTA, developmentLinuxPorts[i]);
		mainServer.deleteAllFiles();

		mainServer.enterSecureMode();
		int[] productionLinuxPorts = mainServer.shiftToDevelopmentPortsIfNotInSecureMode(ports);
		assertEquals("wrong port count?", ports.length, productionLinuxPorts.length);
		for(int i=0; i < ports.length; ++i)
			assertEquals("shifted? " + i, ports[i], productionLinuxPorts[i]);
		
	}

	Vector getBulletinChunk(MartusCrypto securityToUse, NetworkInterface server, String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize) throws Exception
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(new Integer(chunkOffset));
		parameters.add(new Integer(maxChunkSize));

		String signature = securityToUse.createSignatureOfVectorOfStrings(parameters);
		return server.getBulletinChunk(securityToUse.getPublicKeyString(), parameters, signature);
	}

	void verifyErrorResult(String label, Vector vector, String expected )
	{
		assertEquals( label + " error size not 1?", 1, vector.size());
		assertEquals( label + " error wrong result code", expected, vector.get(0));
	}

	void uploadSampleBulletin() 
	{
		testServer.setSecurity(serverSecurity);
		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		testServer.uploadBulletin(clientSecurity.getPublicKeyString(), b1.getLocalId(), b1ZipString);
	}
	
	String uploadSampleDraftBulletin(Bulletin draftBulletin) throws Exception
	{
		testServer.setSecurity(serverSecurity);
		testServer.serverForClients.clearCanUploadList();
		testServer.allowUploads(clientSecurity.getPublicKeyString());
		
		String draftZipString = BulletinForTesting.saveToZipString(getClientDatabase(), draftBulletin, clientSecurity);
		String result = testServer.uploadBulletin(clientSecurity.getPublicKeyString(), draftBulletin.getLocalId(), draftZipString);
		assertEquals("upload failed?", OK, result);
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
	
	static MockClientDatabase getClientDatabase()
	{
		return (MockClientDatabase)store.getDatabase();
	}

	class MockDraftDatabase extends MockServerDatabase
	{
		
		public void writeRecord(DatabaseKey key, InputStream record)
			throws IOException, RecordHiddenException
		{
			if(!key.isDraft())
				throw new IOException("Invalid Status");
			super.writeRecord(key, record);
		}
	}

	class MockSealedDatabase extends MockServerDatabase
	{
		
		public void writeRecord(DatabaseKey key, InputStream record)
			throws IOException, RecordHiddenException
		{
			if(!key.isSealed())
				throw new IOException("Invalid Status");
			super.writeRecord(key, record);
		}
	}

	static File tempFile;
	static Bulletin b1;
	static String b1ZipString;
	static byte[] b1ZipBytes;
	static byte[] b1ChunkBytes0;
	static byte[] b1ChunkBytes1;
	static String b1ChunkData0;
	static String b1ChunkData1;

	static Bulletin b2;
	static Bulletin privateBulletin;

	static Bulletin draft;

	static MartusCrypto clientSecurity;
	static String clientAccountId;
	static MartusCrypto serverSecurity;
	static MartusCrypto otherServerSecurity;
	static MartusCrypto hqSecurity;
	private static BulletinStore store;

	final static byte[] b1AttachmentBytes = {1,2,3,4,4,3,2,1};
	final static byte[] file1Bytes = {1,2,3,4,4,3,2,1};
	final static byte[] file2Bytes = {1,2,3,4,4,3,2,1,0};
	
	MockMartusServer testServer;
	NetworkInterface testServerInterface;
}
