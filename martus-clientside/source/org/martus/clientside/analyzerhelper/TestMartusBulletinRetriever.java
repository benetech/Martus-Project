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
package org.martus.clientside.analyzerhelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpc;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer;
import org.martus.clientside.analyzerhelper.MartusBulletinRetriever.ServerPublicCodeDoesNotMatchException;
import org.martus.clientside.test.NoServerNetworkInterfaceForNonSSLHandler;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.network.ClientSideNetworkInterface;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.NonSSLNetworkAPIWithHelpers;
import org.martus.common.network.PassThroughTransportWrapper;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.MockBulletinStore;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.StreamableBase64;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.TestCaseEnhanced;



public class TestMartusBulletinRetriever extends TestCaseEnhanced
{
	public TestMartusBulletinRetriever(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
	  	super.setUp();

	  	if(security == null)
	  	{
			security = new MartusSecurity();
			security.createKeyPair(512);
			serverSecurity = new MartusSecurity();
			serverSecurity.createKeyPair(512);
	  	}
		streamOut = new ByteArrayOutputStream();
		security.writeKeyPair(streamOut, password);
		streamOut.close();
		ClientSideNetworkHandlerUsingXmlRpc.addAllowedServer(TEST_SERVER_IP);
	}	
	
	public void testInvalidIOStream() throws Exception
	{
		char[] testPassword = "test".toCharArray();
		try
		{
			InputStream stream = new FileInputStream("");
			new MartusBulletinRetriever(stream, testPassword);
			fail("Should have thrown IO exception on null input stream");
		}
		catch(IOException expectedException)
		{
		}
	}

	public void testPassword() throws Exception
	{
		ByteArrayInputStream streamIn = new ByteArrayInputStream(streamOut.toByteArray());
		
		try
		{
			new MartusBulletinRetriever(streamIn, "invalid".toCharArray() );
			fail("Should have thrown AuthorizationFailedException on invalid password");
		}
		catch(AuthorizationFailedException expected)
		{
		}
		streamIn.reset();
		new MartusBulletinRetriever(streamIn, password );
		streamIn.close();
	}
	
	class TestServerNetworkInterfaceForNonSSLHandler extends NonSSLNetworkAPIWithHelpers
	{

		public String ping()
		{
			return ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer.MARTUS_SERVER_PING_RESPONSE;
		}

		public Vector getServerInformation()
		{
			Vector result = new Vector();
			try
			{
				byte[] publicKeyBytes = StreamableBase64.decode(publicKeyString);
				ByteArrayInputStream in = new ByteArrayInputStream(publicKeyBytes);
				byte[] sigBytes = serverSecurity.createSignatureOfStream(in);
				
				result.add(NetworkInterfaceConstants.OK);
				result.add(publicKeyString);
				result.add(StreamableBase64.encode(sigBytes));
			}
			catch(Exception e)
			{
			}
			return result;
		}
		
		public String publicKeyString;
	}
	
	public void testPingServer() throws Exception
	{
		ByteArrayInputStream streamIn = new ByteArrayInputStream(streamOut.toByteArray());
		MartusBulletinRetriever retriever = new MartusBulletinRetriever(streamIn, password );
		streamIn.close();
		
		assertFalse("server hasn't been configured yet", retriever.isServerAvailable());
		
		retriever.initalizeServerForTesting(TEST_SERVER_IP, "some random public key", new PassThroughTransportWrapper());
		retriever.serverNonSSL = new NoServerNetworkInterfaceForNonSSLHandler();
		assertFalse("invalid server should not be pingable", retriever.isServerAvailable());
		retriever.serverNonSSL = new TestServerNetworkInterfaceForNonSSLHandler();
		assertTrue("a valid server should be pingable", retriever.isServerAvailable());
	}
	
	public void testGetServerPublicKey() throws Exception
	{
		
		ByteArrayInputStream streamIn = new ByteArrayInputStream(streamOut.toByteArray());
		MartusBulletinRetriever retriever = new MartusBulletinRetriever(streamIn, password );
		streamIn.close();
		NonSSLNetworkAPIWithHelpers noServer = new NoServerNetworkInterfaceForNonSSLHandler();
		try
		{
			retriever.getServerPublicKey("Some Random code", noServer);
			fail("Server exists?");
		}
		catch(ServerNotAvailableException expected)
		{
		}
		
		TestServerNetworkInterfaceForNonSSLHandler testServerForNonSSL = new TestServerNetworkInterfaceForNonSSLHandler();
		testServerForNonSSL.publicKeyString = "some invalid keystring";
		try
		{
			retriever.getServerPublicKey("Some Random code", testServerForNonSSL);
			fail("Invalid public key strings should throw an exception");
		}
		catch(ServerErrorException expected)
		{
		}
		String serverPublicKeyString = serverSecurity.getPublicKeyString();
		testServerForNonSSL.publicKeyString = serverPublicKeyString;
		try
		{
			retriever.getServerPublicKey("Invalid code", testServerForNonSSL);
			fail("Incorrect public code.");
		}
		catch(ServerPublicCodeDoesNotMatchException expected)
		{
		}
		retriever.getServerPublicKey(MartusCrypto.computePublicCode(serverPublicKeyString), testServerForNonSSL);
		retriever.getServerPublicKey(MartusCrypto.computeFormattedPublicCode(serverPublicKeyString), testServerForNonSSL);
	}
	
	public void testGetListOfBulletinIds() throws Exception
	{
		NetworkResponse response = new NetworkResponse(null);
		ByteArrayInputStream streamIn = new ByteArrayInputStream(streamOut.toByteArray());
		MartusBulletinRetriever retriever = new MartusBulletinRetriever(streamIn, password );
		streamIn.close();
		String fieldOfficeAccountId = "SomeFieldOffice";
		try
		{
			retriever.getListOfBulletinUniversalIds(fieldOfficeAccountId, response);
			fail("Should have thrown since this is invalid response from a server.");
		}
		catch(ServerErrorException expected)
		{
		}
		
		String bulletin1Id = "bulletin Local ID 1";
		String bulletin2Id = "bulletin Local ID 2";
		String bulletin3Id = "bulletin Local ID 3";
		String summary1 = bulletin1Id +"=PacketID1";
		String summary2 = bulletin2Id +"=PacketID2";
		String summary3 = bulletin3Id +"=PacketID3";
		
		Vector testBulletinSummaries = new Vector();
		testBulletinSummaries.add(summary1);
		testBulletinSummaries.add(summary2);
		testBulletinSummaries.add(summary3);
		
		Vector bulletinSummaries = new Vector();
		bulletinSummaries.add(NetworkInterfaceConstants.OK);
		bulletinSummaries.add(testBulletinSummaries.toArray());
		response = new NetworkResponse(bulletinSummaries);
		Vector returnedBulletinIDs = retriever.getListOfBulletinUniversalIds(fieldOfficeAccountId, response);
		assertEquals("retriever should have a vector of size 3", 3, returnedBulletinIDs.size());
		assertContains(UniversalId.createFromAccountAndLocalId(fieldOfficeAccountId,bulletin1Id), returnedBulletinIDs);
		assertContains(UniversalId.createFromAccountAndLocalId(fieldOfficeAccountId,bulletin2Id), returnedBulletinIDs);
		assertContains(UniversalId.createFromAccountAndLocalId(fieldOfficeAccountId,bulletin3Id), returnedBulletinIDs);
	}
	
	TestCaseEnhanced getThis()
	{
		return this;
	}
	
	private class MockClientSideNetworkGateway extends ClientSideNetworkGateway
	{
		public MockClientSideNetworkGateway(ClientSideNetworkInterface serverToUse)
		{
			super(serverToUse);
			fieldOfficeAccountIds = new Vector();
			draftBulletins = new HashMap();
			sealedBulletins = new HashMap();
		}
		
		public void setFieldOfficeAccountIdsToReturn(Vector fieldOfficeAccountIdsToUse)
		{
			fieldOfficeAccountIds = fieldOfficeAccountIdsToUse;
		}
		
		public void setTestDraftBulletinIdsToReturn(String fieldOffice, Vector bulletinLocalIds)
		{
			draftBulletins.put(fieldOffice, bulletinLocalIds);
		}

		public void setTestSealedBulletinIdsToReturn(String fieldOffice, Vector bulletinLocalIds)
		{
			sealedBulletins.put(fieldOffice, bulletinLocalIds);
		}
		
		public void setTestBulletinToRetrieve(Bulletin bulletin)
		{
			bulletinToRetrieve = bulletin;
		}
		
		public Vector downloadFieldOfficeAccountIds(MartusCrypto securityToUse,
				String myAccountId) throws ServerErrorException
		{
			return fieldOfficeAccountIds;
		}
		
		public NetworkResponse getDraftBulletinIds(MartusCrypto signer,
				String authorAccountId, Vector retrieveTags)
				throws MartusSignatureException
		{
			Vector draftBulletinSummaries = new Vector();
			getListOfBulletinSummaries(authorAccountId, draftBulletinSummaries, draftBulletins);
			
			Vector rawResponse = new Vector();
			rawResponse.add(NetworkInterfaceConstants.OK);
			rawResponse.add(draftBulletinSummaries.toArray());
			return new NetworkResponse(rawResponse);
		}

		public NetworkResponse getSealedBulletinIds(MartusCrypto signer,
				String authorAccountId, Vector retrieveTags)
				throws MartusSignatureException
		{
			Vector sealedBulletinSummaries = new Vector();
			getListOfBulletinSummaries(authorAccountId, sealedBulletinSummaries, sealedBulletins);
			
			Vector rawResponse = new Vector();
			rawResponse.add(NetworkInterfaceConstants.OK);
			rawResponse.add(sealedBulletinSummaries.toArray());
			return new NetworkResponse(rawResponse);
		}

		private void getListOfBulletinSummaries(String authorAccountId, Vector draftBulletinSummaries, HashMap listOfBulletins)
		{
			if(listOfBulletins.containsKey(authorAccountId))
			{
				Vector draftBulletinIds = (Vector)listOfBulletins.get(authorAccountId);
				for(int i = 0; i<draftBulletinIds.size();++i)
				{
					draftBulletinSummaries.add(draftBulletinIds.get(i) + "=somePacketID");
				}
			}
		}
		
		

		public File retrieveBulletin(UniversalId uid, MartusCrypto securityToUse,
				int chunkSize, ProgressMeterInterface progressMeter)
				throws IOException, FileNotFoundException,
				MartusSignatureException, ServerErrorException,
				InvalidBase64Exception
		{
			if(!uid.equals(bulletinToRetrieve.getUniversalId()))
				throw new ServerErrorException();

			try
			{
				BulletinStore store = new MockBulletinStore(getThis());
				store.saveBulletinForTesting(bulletinToRetrieve);
				File bulletinZipFile = createTempFileFromName("$$$TestBulletinWrapperZipFile");
				DatabaseKey key = bulletinToRetrieve.getDatabaseKey();
				BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(store.getDatabase(), key, bulletinZipFile, securityToUse);
				store.deleteAllData();
				return bulletinZipFile;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException();
			}
		}
		
		private Vector fieldOfficeAccountIds;
		private HashMap draftBulletins;
		private HashMap sealedBulletins;
		private Bulletin bulletinToRetrieve;
	}
	
	public void testGetListOfAllFieldOfficeBulletinIdsOnServer() throws Exception
	{
		ClientSideNetworkInterface networkInterface = ClientSideNetworkGateway.buildNetworkInterface(TEST_SERVER_IP, serverSecurity.getPublicKeyString(), null);
		MockClientSideNetworkGateway mockGateway = new MockClientSideNetworkGateway(networkInterface);
		
		ByteArrayInputStream streamIn = new ByteArrayInputStream(streamOut.toByteArray());
		MartusBulletinRetriever retriever = new MartusBulletinRetriever(streamIn, password );
		streamIn.close();
		retriever.initalizeServerForTesting(TEST_SERVER_IP, "some random public key", new PassThroughTransportWrapper());
		retriever.serverNonSSL = new TestServerNetworkInterfaceForNonSSLHandler();
		retriever.setSSLServerToUse(mockGateway);
		List emptyList = retriever.getFieldOfficeBulletinIds();
		assertEquals("Should be empty",0, emptyList.size());
		
		String fieldOffice1 = "field office 1";
		String fieldOffice2 = "field office 2";
		Vector fieldOfficeIds = new Vector();
		fieldOfficeIds.add(fieldOffice1);
		fieldOfficeIds.add(fieldOffice2);
		mockGateway.setFieldOfficeAccountIdsToReturn(fieldOfficeIds);
		emptyList = retriever.getFieldOfficeBulletinIds();
		assertEquals("Should still be empty since there are no bulletins.",0, emptyList.size());
		
		String draftBulletinFO1LocalId = "Draft bulletin Field Office 1";
		Vector fieldOffice1DraftBulletins = new Vector();
		fieldOffice1DraftBulletins.add(draftBulletinFO1LocalId);
		mockGateway.setTestDraftBulletinIdsToReturn(fieldOffice1, fieldOffice1DraftBulletins);

		String sealed1BulletinFO1LocalId = "Sealed 1 bulletin Field Office 1";
		String sealed2BulletinFO1LocalId = "Sealed 2 bulletin Field Office 1";
		Vector fieldOffice1SealedBulletins = new Vector();
		fieldOffice1SealedBulletins.add(sealed1BulletinFO1LocalId);
		fieldOffice1SealedBulletins.add(sealed2BulletinFO1LocalId);
		mockGateway.setTestSealedBulletinIdsToReturn(fieldOffice1, fieldOffice1SealedBulletins);
		
		Vector fieldOffice2HasNoDraftBulletins = new Vector();
		mockGateway.setTestDraftBulletinIdsToReturn(fieldOffice2, fieldOffice2HasNoDraftBulletins);

		String sealed1BulletinFO2LocalId = "Sealed 1 bulletin Field Office 2";
		Vector fieldOffice2SealedBulletins = new Vector();
		fieldOffice2SealedBulletins.add(sealed1BulletinFO2LocalId);
		mockGateway.setTestSealedBulletinIdsToReturn(fieldOffice2, fieldOffice2SealedBulletins);

		List allFieldOfficesBulletinIds = retriever.getFieldOfficeBulletinIds();
		assertEquals("Should contain 4 bulletins", 4, allFieldOfficesBulletinIds.size());
		
		UniversalId bulletinId1 = UniversalId.createFromAccountAndLocalId(fieldOffice1,draftBulletinFO1LocalId);
		UniversalId bulletinId2 = UniversalId.createFromAccountAndLocalId(fieldOffice1,sealed1BulletinFO1LocalId);
		UniversalId bulletinId3 = UniversalId.createFromAccountAndLocalId(fieldOffice1,sealed2BulletinFO1LocalId);
		UniversalId bulletinId4 = UniversalId.createFromAccountAndLocalId(fieldOffice2,sealed1BulletinFO2LocalId);

		assertTrue("Should contain this draft bulletin", allFieldOfficesBulletinIds.contains(bulletinId1));
		assertTrue("Should contain this sealed 1 bulletin", allFieldOfficesBulletinIds.contains(bulletinId2));
		assertTrue("Should contain this sealed 2 bulletin", allFieldOfficesBulletinIds.contains(bulletinId3));
		assertTrue("Should contain this sealed bulletin for field office 2", allFieldOfficesBulletinIds.contains(bulletinId4));
	}
	
	class exitBulletinRetrieveProgressMeter implements ProgressMeterInterface
	{
		@Override
		public void setStatusMessage(String message)
		{
		}

		@Override
		public void updateProgressMeter(int currentValue, int maxValue)
		{
		}

		@Override
		public boolean shouldExit()
		{
			return true;
		}

		@Override
		public void hideProgressMeter()
		{
		}

		@Override
		public void finished()
		{
		}
	}

	public void testGetBulletin() throws Exception
	{
		ClientSideNetworkInterface networkInterface = ClientSideNetworkGateway.buildNetworkInterface(TEST_SERVER_IP, serverSecurity.getPublicKeyString(), null);
		MockClientSideNetworkGateway mockGateway = new MockClientSideNetworkGateway(networkInterface);
		
		ByteArrayInputStream streamIn = new ByteArrayInputStream(streamOut.toByteArray());
		MartusBulletinRetriever retriever = new MartusBulletinRetriever(streamIn, password );
		streamIn.close();
		retriever.initalizeServerForTesting(TEST_SERVER_IP, "some random public key", new PassThroughTransportWrapper());
		retriever.serverNonSSL = new TestServerNetworkInterfaceForNonSSLHandler();
		retriever.setSSLServerToUse(mockGateway);
		
		try
		{
			retriever.getBulletin(UniversalIdForTesting.createDummyUniversalId(), null);
			fail("should have thrown for invalid UId");
		}
		catch(ServerErrorException expected)
		{
		}

		Bulletin bulletin = new Bulletin(security);
		String author = "author";
		String title = "title";
		String location = "location";
		String privateData = "private";
		bulletin.set(BulletinConstants.TAGAUTHOR, author);
		bulletin.set(BulletinConstants.TAGTITLE, title);
		bulletin.set(BulletinConstants.TAGLOCATION, location);
		bulletin.set(BulletinConstants.TAGPRIVATEINFO, privateData);
		File attachmentFile = createTempFile();
		FileOutputStream out = new FileOutputStream(attachmentFile);
		out.write("This is a test file".getBytes());
		out.close();
		AttachmentProxy attachmentProxy = new AttachmentProxy(attachmentFile);
		bulletin.addPublicAttachment(attachmentProxy);
		
		mockGateway.setTestBulletinToRetrieve(bulletin);
		MartusBulletinWrapper retrievedBulletin = retriever.getBulletin(bulletin.getUniversalId(), null);
		assertEquals("Didn't get the correct author?", author, retrievedBulletin.getAuthor());
		assertEquals("Didn't get the correct title?", title, retrievedBulletin.getTitle());
		assertEquals("Didn't get the correct location?", location, retrievedBulletin.getLocation());
		assertEquals("Didn't get the correct private data?", privateData, retrievedBulletin.getPrivateInfo());
		retrievedBulletin.deleteAllData();		
		
		MartusBulletinWrapper cancelledBulletin = retriever.getBulletin(bulletin.getUniversalId(), new exitBulletinRetrieveProgressMeter());
		assertNull("Should return a null bulletin if cancelled.", cancelledBulletin);
	}
	
	private static final String TEST_SERVER_IP = "1.2.3.4";
	
	private static MartusSecurity security;
	static MartusSecurity serverSecurity;
	private static char[] password = "the password".toCharArray();
	private ByteArrayOutputStream streamOut;

}
