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

package org.martus.server.foramplifiers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;

import org.martus.common.ContactInfo;
import org.martus.common.LoggerToNull;
import org.martus.common.MartusUtilities;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.BulletinUploadRecord;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.test.MockBulletinStore;
import org.martus.server.forclients.MockMartusServer;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeWriter;

public class TestServerForAmplifiers extends TestCaseEnhanced
{
	public TestServerForAmplifiers(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		if(logger == null)
			logger = new LoggerToNull();
		if(clientSecurity == null)
		{
			clientSecurity = new MockMartusSecurity();
			clientSecurity.createKeyPair();
		}
		
		if(coreServer == null)
		{
			MockMartusSecurity mockServer = MockMartusSecurity.createServer();
			coreServer = new MockMartusServer();
			coreServer.setSecurity(mockServer);
			coreServer.serverForClients.clearCanUploadList();
			coreServer.allowUploads(clientSecurity.getPublicKeyString());
		}
		
		if(otherServer == null)
		{
			MockMartusSecurity mockOtherServer = MockMartusSecurity.createOtherServer();
			otherServer = new MockMartusServer();
			otherServer.setSecurity(mockOtherServer);
			otherServer.serverForClients.clearCanUploadList();
			otherServer.allowUploads(clientSecurity.getPublicKeyString());
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
			b1.setSealed();
			store.saveEncryptedBulletinForTesting(b1);
			b1 = BulletinLoader.loadFromDatabase(getClientDatabase(), DatabaseKey.createSealedKey(b1.getUniversalId()), clientSecurity);
			b1ZipString = BulletinForTesting.saveToZipString(getClientDatabase(), b1, clientSecurity);
	
			b2 = new Bulletin(clientSecurity);
			b2.setAllPrivate(true);
			b2.set(Bulletin.TAGTITLE, "Title2");
			b2.set(Bulletin.TAGPUBLICINFO, "Details2");
			b2.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails2");
			b2.setSealed();
			store.saveEncryptedBulletinForTesting(b2);
			b2ZipString = BulletinForTesting.saveToZipString(getClientDatabase(), b2, clientSecurity);

			b3 = new Bulletin(clientSecurity);
			b3.setAllPrivate(false);
			b3.set(Bulletin.TAGTITLE, "Title1");
			b3.set(Bulletin.TAGPUBLICINFO, "Details1");
			b3.setSealed();
			store.saveEncryptedBulletinForTesting(b3);
			b3 = BulletinLoader.loadFromDatabase(getClientDatabase(), DatabaseKey.createSealedKey(b3.getUniversalId()), clientSecurity);
			b3ZipString = BulletinForTesting.saveToZipString(getClientDatabase(), b3, clientSecurity);

			b4 = new Bulletin(clientSecurity);
			b4.setAllPrivate(false);
			b4.set(Bulletin.TAGTITLE, "Title4");
			b4.set(Bulletin.TAGPUBLICINFO, "Details4");
			b4.setDraft();
			store.saveEncryptedBulletinForTesting(b4);
			b4 = BulletinLoader.loadFromDatabase(getClientDatabase(), DatabaseKey.createDraftKey(b4.getUniversalId()), clientSecurity);
			b4ZipString = BulletinForTesting.saveToZipString(getClientDatabase(), b4, clientSecurity);
		}
	}

	protected void tearDown() throws Exception
	{
		coreServer.deleteAllFiles();
		otherServer.deleteAllFiles();
		super.tearDown();
	}
	
	public void testAmplifierGetContactInfo() throws Exception
	{
		MockMartusSecurity amplifier = MockMartusSecurity.createAmplifier();

		Vector parameters = new Vector();
		parameters.add(clientSecurity.getPublicKeyString());
		String signature = amplifier.createSignatureOfVectorOfStrings(parameters);

		File compliance = new File(coreServer.getStartupConfigDirectory(), "compliance.txt");
		compliance.deleteOnExit();
		compliance.createNewFile();
		coreServer.setAmplifierListenerEnabled(true);
		coreServer.setClientListenerEnabled(true);
		coreServer.loadConfigurationFiles();

		Vector response = coreServer.serverForAmplifiers.getAmplifierHandler().getContactInfo(amplifier.getPublicKeyString(), parameters, signature);
		assertEquals("Should have rejected us since we are not authorized", ServerForAmplifiers.NOT_AUTHORIZED, response.get(0));

		File ampsWhoCallUs = new File(coreServer.getStartupConfigDirectory(), "ampsWhoCallUs");
		ampsWhoCallUs.deleteOnExit();
		ampsWhoCallUs.mkdirs();
		File pubKeyFile1 = new File(ampsWhoCallUs, "code=1.2.3.4.5-ip=1.2.3.4.txt");
		pubKeyFile1.deleteOnExit();
		MartusUtilities.exportServerPublicKey(amplifier, pubKeyFile1);
		
		coreServer.loadConfigurationFiles();
		compliance.delete();
		pubKeyFile1.delete();
		ampsWhoCallUs.delete();
		
		Vector invalidNumberOfParameters = new Vector();
		String invalidNumberOfParamsSig = amplifier.createSignatureOfVectorOfStrings(invalidNumberOfParameters);

		response = coreServer.serverForAmplifiers.getAmplifierHandler().getContactInfo(amplifier.getPublicKeyString(), invalidNumberOfParameters, invalidNumberOfParamsSig);
		assertEquals("Incomplete request should have been retuned", ServerForAmplifiers.INCOMPLETE, response.get(0));

		response = coreServer.serverForAmplifiers.getAmplifierHandler().getContactInfo(amplifier.getPublicKeyString(), parameters, "bad sig");
		assertEquals("Bad Signature should have been returned", ServerForAmplifiers.SIG_ERROR, response.get(0));

		response = coreServer.serverForAmplifiers.getAmplifierHandler().getContactInfo(amplifier.getPublicKeyString(), parameters, signature);
		assertEquals("Should not have found contact info since it hasn't been uploaded yet", ServerForAmplifiers.ITEM_NOT_FOUND, response.get(0));

		String clientId = clientSecurity.getPublicKeyString();
		String data1 = "data1";
		String data2 = "data2";
		Vector contactInfo = new Vector();
		contactInfo.add(clientId);
		contactInfo.add(new Integer(2));
		contactInfo.add(data1);
		contactInfo.add(data2);
		String infoSignature = clientSecurity.createSignatureOfVectorOfStrings(contactInfo);
		contactInfo.add(infoSignature);
		String result = coreServer.putContactInfo(clientId, contactInfo);
		assertEquals("Not ok?", NetworkInterfaceConstants.OK, result);
		
		response = coreServer.serverForAmplifiers.getAmplifierHandler().getContactInfo(amplifier.getPublicKeyString(), parameters, signature);
		assertEquals("Should have found contact info since it has been uploaded", ServerForAmplifiers.OK, response.get(0));
		Vector encodedInfoReturned = (Vector)response.get(1);
		Vector decodedInfoReturned = ContactInfo.decodeContactInfoVectorIfNecessary(encodedInfoReturned); 
		assertEquals("Should be same size as was put in", contactInfo.size(), decodedInfoReturned.size());
		assertEquals("Public key doesn't match", clientId, decodedInfoReturned.get(0));
		assertEquals("data size not two?", 2, ((Integer)decodedInfoReturned.get(1)).intValue());
		assertEquals("data not correct?", data1, decodedInfoReturned.get(2));
		assertEquals("data2 not correct?", data2, decodedInfoReturned.get(3));
		assertEquals("signature doesn't match?", infoSignature, decodedInfoReturned.get(4));
		
	}


	public void testIsAuthorizedForAmplifying() throws Exception
	{
		MockMartusServer nobodyAuthorizedCore = new MockMartusServer();
		ServerForAmplifiers nobodyAuthorized = new ServerForAmplifiers(nobodyAuthorizedCore, logger);
		nobodyAuthorizedCore.setAmplifierListenerEnabled(true);
		nobodyAuthorized.loadConfigurationFiles();
		assertFalse("client already authorized?", nobodyAuthorized.isAuthorizedAmp(clientSecurity.getPublicKeyString()));
		nobodyAuthorizedCore.deleteAllFiles();
		
		MockMartusServer oneAuthorizedCore = new MockMartusServer();
		oneAuthorizedCore.setAmplifierListenerEnabled(true);
		oneAuthorizedCore.enterSecureMode();
		File ampsWhoCallUs = new File(oneAuthorizedCore.getStartupConfigDirectory(), "ampsWhoCallUs");
		ampsWhoCallUs.deleteOnExit();
		ampsWhoCallUs.mkdirs();
		File pubKeyFile1 = new File(ampsWhoCallUs, "code=1.2.3.4.5-ip=1.2.3.4.txt");
		pubKeyFile1.deleteOnExit();
		MartusUtilities.exportServerPublicKey(clientSecurity, pubKeyFile1);
		ServerForAmplifiers oneAuthorized = new ServerForAmplifiers(oneAuthorizedCore, logger);
		oneAuthorized.loadConfigurationFiles();
		assertTrue("client1 not authorized?", oneAuthorized.isAuthorizedAmp(clientSecurity.getPublicKeyString()));
		assertFalse("ourselves authorized?", oneAuthorized.isAuthorizedForMirroring(coreServer.getAccountId()));
		ampsWhoCallUs.delete();
		oneAuthorizedCore.deleteAllFiles();
		
	}

	public void testCanAccountBeAmplified() throws Exception
	{
		MockMartusServer localCoreServer = new MockMartusServer();
		ServerForAmplifiers ampServer = new ServerForAmplifiers(localCoreServer, logger);
		ampServer.loadConfigurationFiles();
		assertTrue("client not authorized?", ampServer.canAccountBeAmplified(clientSecurity.getPublicKeyString()));
		String anotherAccountId = "any other account";
		assertTrue("another client should also be authorized?", ampServer.canAccountBeAmplified(anotherAccountId));
		
		String clientId = clientSecurity.getPublicKeyString();
		File tempNotAmplified = createTempFile();
		
		UnicodeWriter writer = new UnicodeWriter(tempNotAmplified);
		writer.writeln(clientId);
		writer.close();
		
		ampServer.loadClientsNotAmplified(tempNotAmplified);
		assertFalse("client still authorized?", ampServer.canAccountBeAmplified(clientSecurity.getPublicKeyString()));
		assertTrue("another client should be authorized?", ampServer.canAccountBeAmplified(anotherAccountId));
		localCoreServer.deleteAllFiles();
	}
	
	public void testAmplifierServer() throws Exception
	{
		MockMartusSecurity amplifier = MockMartusSecurity.createAmplifier();


		Vector parameters = new Vector();
		parameters.add(clientSecurity.getPublicKeyString());
		String signature = amplifier.createSignatureOfVectorOfStrings(parameters);

		File compliance = new File(coreServer.getStartupConfigDirectory(), "compliance.txt");
		compliance.deleteOnExit();
		compliance.createNewFile();
		File ampsWhoCallUs = new File(coreServer.getStartupConfigDirectory(), "ampsWhoCallUs");
		ampsWhoCallUs.deleteOnExit();
		ampsWhoCallUs.mkdirs();
		File pubKeyFile1 = new File(ampsWhoCallUs, "code=1.2.3.4.5-ip=1.2.3.4.txt");
		pubKeyFile1.deleteOnExit();
		MartusUtilities.exportServerPublicKey(amplifier, pubKeyFile1);
		coreServer.setAmplifierListenerEnabled(true);
		coreServer.loadConfigurationFiles();
		compliance.delete();
		pubKeyFile1.delete();
		ampsWhoCallUs.delete();
		
		// a draft should be ignored by the rest of this test
		uploadSampleBulletin(coreServer, b4.getLocalId(), b4ZipString);
		
		String ampAccountId = amplifier.getPublicKeyString();
		Vector response = coreServer.serverForAmplifiers.getAmplifierHandler().getPublicBulletinLocalIds(ampAccountId, parameters, signature);
		assertEquals("Authorized amp requested data failed?", ServerForAmplifiers.OK, response.get(0));

		assertEquals("Failed to get list of public bulletin ids?", ServerForAmplifiers.OK, response.get(0));
		Vector uIds = (Vector)response.get(1);
		assertEquals("bulletins found?", 0, uIds.size());

		uploadSampleBulletin(coreServer, b1.getLocalId(), b1ZipString);
		uploadSampleBulletin(coreServer, b2.getLocalId(), b2ZipString);
		response = coreServer.serverForAmplifiers.getAmplifierHandler().getPublicBulletinLocalIds(ampAccountId, parameters, signature);
		uIds = (Vector)response.get(1);
		assertEquals("incorect # of bulletins found after uploading?", 1, uIds.size());
		assertEquals("B1 should had been returned", b1.getLocalId(), uIds.get(0));

		uploadSampleBulletin(otherServer, b3.getLocalId(), b3ZipString);
		uploadSampleBulletin(coreServer, b3.getLocalId(), b3ZipString);
		response = coreServer.serverForAmplifiers.getAmplifierHandler().getPublicBulletinLocalIds(ampAccountId, parameters, signature);
		uIds = (Vector)response.get(1);
		assertEquals("Currently B3 is a bulletin not mirrored?", 2, uIds.size());
		
		String bulletin3LocalId = b3.getLocalId();
		String burFromOtherDatabase = BulletinUploadRecord.createBulletinUploadRecord(bulletin3LocalId, otherServer.getSecurity());
		BulletinUploadRecord.writeSpecificBurToDatabase(coreServer.getWriteableDatabase(), b3.getBulletinHeaderPacket(), burFromOtherDatabase);
		response = coreServer.serverForAmplifiers.getAmplifierHandler().getPublicBulletinLocalIds(ampAccountId, parameters, signature);
		uIds = (Vector)response.get(1);
		assertEquals("incorect # of bulletins found after mirroring, should only amplify own bulletins?", 1, uIds.size());
	}

	public void testGetAmplifierBulletinChunk() throws Exception
	{
		MockMartusSecurity amplifier = MockMartusSecurity.createAmplifier();

		Vector parameters = new Vector();
		parameters.add(clientSecurity.getPublicKeyString());
		parameters.add(clientSecurity.getPublicKeyString());
		parameters.add(new Integer(0));
		parameters.add(new Integer(0));
		String signature = amplifier.createSignatureOfVectorOfStrings(parameters);

		File compliance = new File(coreServer.getStartupConfigDirectory(), "compliance.txt");
		compliance.deleteOnExit();
		compliance.createNewFile();
		coreServer.setAmplifierListenerEnabled(true);
		coreServer.loadConfigurationFiles();
		compliance.delete();
		
		Vector response = coreServer.serverForAmplifiers.getAmplifierHandler().getAmplifierBulletinChunk(amplifier.getPublicKeyString(), parameters, signature);
		assertEquals("Should have rejected us since we are not authorized", ServerForAmplifiers.NOT_AUTHORIZED, response.get(0));
		
		//TODO:More tests needed here
	}
	
	void uploadSampleBulletin(MockMartusServer serverToUse, String bulletinLocalId, String bulletinZip ) 
	{
		serverToUse.serverForClients.clearCanUploadList();
		serverToUse.allowUploads(clientSecurity.getPublicKeyString());
		serverToUse.uploadBulletin(clientSecurity.getPublicKeyString(), bulletinLocalId, bulletinZip);
	}
	
	private static MockClientDatabase getClientDatabase()
	{
		return (MockClientDatabase)store.getDatabase();
	}

	MockMartusServer coreServer;
	MockMartusServer otherServer;
	LoggerToNull logger;
	private static Bulletin b1;
	private static String b1ZipString;

	private static Bulletin b2;
	private static String b2ZipString;

	private static Bulletin b3;
	private static String b3ZipString;

	private static Bulletin b4;
	private static String b4ZipString;

	private static MartusCrypto clientSecurity;
	private static MockBulletinStore store;

	final static byte[] b1AttachmentBytes = {1,2,3,4,4,3,2,1};
	final static byte[] file1Bytes = {1,2,3,4,4,3,2,1};
	final static byte[] file2Bytes = {1,2,3,4,4,3,2,1,0};

}

