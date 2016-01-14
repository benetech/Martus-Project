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

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.martus.common.FieldCollection;
import org.martus.common.FieldSpecCollection;
import org.martus.common.LegacyCustomFields;
import org.martus.common.LoggerToNull;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.InvalidPublicKeyFileException;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.BulletinUploadRecord;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.fieldspec.CustomFieldTemplate;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FormTemplateParsingException;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.ServerForClients;
import org.martus.server.main.ServerBulletinStore;
import org.martus.util.Base64;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;

public class TestServerForMirroring extends TestCaseEnhanced
{
	public TestServerForMirroring(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		logger = new LoggerToNull();
		MockMartusSecurity serverSecurity = MockMartusSecurity.createServer();
		coreServer = new MockMartusServer();
		coreServer.setSecurity(serverSecurity);
		server = new ServerForMirroring(coreServer, logger);
		
		clientSecurity1 = MockMartusSecurity.createClient();
		clientSecurity2 = MockMartusSecurity.createOtherClient();

		Database db = coreServer.getWriteableDatabase();

		bhp1 = new BulletinHeaderPacket(clientSecurity1);
		bhp1.setStatus(BulletinConstants.STATUSSEALED);
		DatabaseKey key1 = bhp1.createKeyWithHeaderStatus(bhp1.getUniversalId());
		bhp1.writeXmlToDatabase(db, key1, false, clientSecurity1);

		bhp2 = new BulletinHeaderPacket(clientSecurity1);
		bhp2.setStatus(BulletinConstants.STATUSSEALED);
		DatabaseKey key2 = bhp2.createKeyWithHeaderStatus(bhp2.getUniversalId());
		bhp2.writeXmlToDatabase(db, key2, false, clientSecurity1);

		bhp3 = new BulletinHeaderPacket(clientSecurity2);
		bhp3.setStatus(BulletinConstants.STATUSSEALED);
		DatabaseKey key3 = bhp3.createKeyWithHeaderStatus(bhp3.getUniversalId());
		bhp3.writeXmlToDatabase(db, key3, false, clientSecurity2);

		bhp4 = new BulletinHeaderPacket(clientSecurity2);
		bhp4.setStatus(BulletinConstants.STATUSDRAFT);
		DatabaseKey key4 = bhp4.createKeyWithHeaderStatus(bhp4.getUniversalId());
		bhp4.writeXmlToDatabase(db, key4, false, clientSecurity2);
		
		UniversalId fdpUid = FieldDataPacket.createUniversalId(clientSecurity1);
		FieldSpec[] tags = {LegacyCustomFields.createFromLegacy("whatever")};
		FieldDataPacket fdp1 = new FieldDataPacket(fdpUid, new FieldSpecCollection(tags));
		fdp1.writeXmlToClientDatabase(db, false, clientSecurity1);
		
		UniversalId otherPacketId = UniversalIdForTesting.createFromAccountAndPrefix(clientSecurity2.getPublicKeyString(), "X");
		DatabaseKey key = DatabaseKey.createSealedKey(otherPacketId);
		db.writeRecord(key, "Not a valid packet");
	}

	protected void tearDown() throws Exception
	{
		coreServer.deleteAllFiles();
		super.tearDown();
	}
	
	public void testListAvailableFormTemplateInfos() throws Exception
	{
		String clientAccountId = clientSecurity1.getPublicKeyString();
		Vector emptyResponse = server.listAvailableFormTemplateInfos(clientAccountId);
		assertEquals("Already has forms?", 0, emptyResponse.size());

		ServerBulletinStore store = coreServer.getStore();
		MartusCrypto security = server.getSecurity();
		byte[] fakeTemplateData = new byte[] { 1,2,3,4,5};
		String invalidBase64Template = Base64.encodeBytes(fakeTemplateData);
		try
		{
			ServerForClients.saveBase64FormTemplate(store, clientAccountId, invalidBase64Template, security, logger);
			fail("Should have thrown saving invalid template");
		}
		catch(FormTemplateParsingException ignoreExpected)
		{
		}

		createAndSaveSampleTemplate(store, clientAccountId, "First", security);
		createAndSaveSampleTemplate(store, clientAccountId, "Second", security);
		Vector storedTemplates = store.getListOfFormTemplatesForAccount(clientAccountId);
		assertEquals(2, storedTemplates.size());
		TemplateInfoForMirroring storedInfo1 = new TemplateInfoForMirroring((File)storedTemplates.get(0));
		TemplateInfoForMirroring storedInfo2 = new TemplateInfoForMirroring((File)storedTemplates.get(1));
		
		Vector resultVector = server.listAvailableFormTemplateInfos(clientAccountId);
		assertEquals("Wrong count?", 2, resultVector.size());
		TemplateInfoForMirroring gotInfo1 = new TemplateInfoForMirroring((String)resultVector.get(0));
		TemplateInfoForMirroring gotInfo2 = new TemplateInfoForMirroring((String)resultVector.get(1));
		
		assertEquals(storedInfo1, gotInfo1);
		assertEquals(storedInfo2, gotInfo2);
		assertTrue("Wrong order?", gotInfo1.asString().compareTo(gotInfo2.asString()) < 0);
	}
	
	public void testGetFormTemplate() throws Exception
	{
		ServerBulletinStore store = coreServer.getStore();
		String clientAccountId = clientSecurity1.getPublicKeyString();
		MartusCrypto security = server.getSecurity();

		String formTitle = "First";
		createAndSaveSampleTemplate(store, clientAccountId, formTitle, security);
		String formFilename = ServerForClients.calculateFileNameFromString(formTitle);
		File templateFile = store.getFormTemplateFileFromAccount(clientAccountId, formFilename);
		String templateFilename = templateFile.getName();
		Vector templateVector = server.getFormTemplate(clientAccountId, templateFilename);
		assertEquals("templateVector1 doesn't have the required one element?", 1, templateVector.size());
		String gotTemplateBase64 = (String) templateVector.get(0);
		String savedTemplateBase64 = StreamableBase64.readAllAndEncodeBase64(templateFile);
		assertEquals(savedTemplateBase64, gotTemplateBase64);
	}

	public void createAndSaveSampleTemplate(ServerBulletinStore store,
			String clientAccountId, String title, MartusCrypto security)
			throws Exception 
	{
		CustomFieldTemplate template = createSampleTemplate(title);
		String base64Template = template.getExportedTemplateAsBase64String(security);
		ServerForClients.saveBase64FormTemplate(store, clientAccountId, base64Template, security, logger);
	}

	public CustomFieldTemplate createSampleTemplate(String title) throws Exception 
	{
		String description = "This is a description";
		FieldCollection topSection = new FieldCollection(StandardFieldSpecs.getDefaultTopSetionFieldSpecs());
		FieldCollection bottomSection = new FieldCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		CustomFieldTemplate template = new CustomFieldTemplate(title, description, topSection, bottomSection);
		return template;
	}
	
	public void testGetPublicInfo() throws Exception
	{
		Vector publicInfo = server.getPublicInfo();
		assertEquals(2, publicInfo.size());
		String publicKey = (String)publicInfo.get(0);
		String gotSig = (String)publicInfo.get(1);
		String serverPublicKeyString = server.getSecurity().getPublicKeyString();
		MartusUtilities.validatePublicInfo(publicKey, gotSig, clientSecurity1);
		assertEquals(serverPublicKeyString, publicInfo.get(0));
		
	}
	
	public void testIsAuthorizedForMirroring() throws Exception
	{
		MockMartusServer nobodyAuthorizedCore = new MockMartusServer();
		ServerForMirroring nobodyAuthorized = new ServerForMirroring(nobodyAuthorizedCore, logger);
		nobodyAuthorized.loadConfigurationFiles();
		assertFalse("client already authorized?", nobodyAuthorized.isAuthorizedForMirroring(clientSecurity1.getPublicKeyString()));
		nobodyAuthorizedCore.deleteAllFiles();
		
		MockMartusServer twoAuthorizedCore = new MockMartusServer();
		twoAuthorizedCore.enterSecureMode();
		File mirrorsWhoCallUs = new File(twoAuthorizedCore.getStartupConfigDirectory(), "mirrorsWhoCallUs");
		mirrorsWhoCallUs.mkdirs();
		File pubKeyFile1 = new File(mirrorsWhoCallUs, "code=1.2.3.4.5-ip=1.2.3.4.txt");
		MartusUtilities.exportServerPublicKey(clientSecurity1, pubKeyFile1);
		File pubKeyFile2 = new File(mirrorsWhoCallUs, "code=2.3.4.5.6-ip=2.3.4.5.txt");
		MartusUtilities.exportServerPublicKey(clientSecurity2, pubKeyFile2);
		ServerForMirroring twoAuthorized = new ServerForMirroring(twoAuthorizedCore, logger);
		twoAuthorized.loadConfigurationFiles();
		assertTrue("client1 not authorized?", twoAuthorized.isAuthorizedForMirroring(clientSecurity1.getPublicKeyString()));
		assertTrue("client2 not authorized?", twoAuthorized.isAuthorizedForMirroring(clientSecurity2.getPublicKeyString()));
		assertFalse("ourselves authorized?", twoAuthorized.isAuthorizedForMirroring(coreServer.getAccountId()));
		mirrorsWhoCallUs.delete();
		twoAuthorizedCore.deleteAllFiles();
		
	}

	public void testListAccounts() throws Exception
	{
		Vector result = server.listAccountsForMirroring();
		assertEquals(2, result.size());
		assertContains(clientSecurity1.getPublicKeyString(), result);
		assertContains(clientSecurity2.getPublicKeyString(), result);
	}
	
	public void testOldListBulletinsForMirroring() throws Exception
	{
		internalTestSealeds();
		
		String publicKeyString2 = clientSecurity2.getPublicKeyString();
		Vector result2 = server.listBulletinsForMirroring(publicKeyString2);
		assertEquals(1, result2.size());
		Vector ids2 = new Vector();
		ids2.add(((Vector)result2.get(0)).get(0));
		assertContains(bhp3.getLocalId(), ids2);
	}
	
	public void testNewListAvailableIdsForMirroring() throws Exception
	{
		internalTestSealeds();
		
		String publicKeyString2 = clientSecurity2.getPublicKeyString();
		Vector result2 = new Vector(server.listAvailableIdsForMirroring(publicKeyString2));
		assertEquals(2, result2.size());
		Vector ids2 = new Vector();
		ids2.add(((Vector)result2.get(0)).get(0));
		ids2.add(((Vector)result2.get(1)).get(0));
		assertContains(bhp3.getLocalId(), ids2);
		assertContains(bhp4.getLocalId(), ids2);
	}

	private void internalTestSealeds()
	{
		ReadableDatabase db = coreServer.getDatabase();
		MockServerDatabase mdb = (MockServerDatabase)db;
		assertEquals(6, mdb.getRecordCount());
		Set allKeys = mdb.getAllKeys();
		int drafts = 0;
		int sealeds = 0;
		for (Iterator iter = allKeys.iterator(); iter.hasNext();)
		{
			DatabaseKey key = (DatabaseKey)iter.next();
			if(key.isDraft())
				++drafts;
			else
				++sealeds;
		}
		assertEquals(5, sealeds);
		assertEquals(1, drafts);

		String publicKeyString1 = clientSecurity1.getPublicKeyString();
		Vector result1 = new Vector(server.listAvailableIdsForMirroring(publicKeyString1));
		assertEquals(2, result1.size());
		Vector ids1 = new Vector();
		ids1.add(((Vector)result1.get(0)).get(0));
		ids1.add(((Vector)result1.get(1)).get(0));
		assertContains(bhp1.getLocalId(), ids1);
		assertContains(bhp2.getLocalId(), ids1);
	}

	public void testGetBulletinUploadRecord() throws Exception
	{
		String burNotFound = server.getBulletinUploadRecord(bhp1.getAccountId(), bhp1.getLocalId());
		assertNull("found bur?", burNotFound);

		String expectedBur = BulletinUploadRecord.createBulletinUploadRecord(bhp1.getLocalId(), server.getSecurity());
		DatabaseKey headerKey = bhp1.createKeyWithHeaderStatus(bhp1.getUniversalId());
		String bulletinLocalId = headerKey.getLocalId();
		BulletinUploadRecord.writeSpecificBurToDatabase(coreServer.getWriteableDatabase(), bhp1, expectedBur);
		String bur1 = server.getBulletinUploadRecord(bhp1.getAccountId(), bulletinLocalId);
		assertNotNull("didn't find bur1?", bur1);
		assertEquals("wrong bur?", expectedBur, bur1);
	}
	
	public void testExtractIpFromFileName() throws Exception
	{
		try
		{
			MartusUtilities.extractIpFromFileName("code=x.y.z");
			fail("Should have thrown missing ip=");
		}
		catch(InvalidPublicKeyFileException ignoreExpectedException)
		{
		}

		try
		{
			MartusUtilities.extractIpFromFileName("ip=1.2.3");
			fail("Should have thrown not enough dots");
		}
		catch(InvalidPublicKeyFileException ignoreExpectedException)
		{
		}

		assertEquals("1.2.3.4", MartusUtilities.extractIpFromFileName("ip=1.2.3.4"));
		assertEquals("2.3.4.5", MartusUtilities.extractIpFromFileName("ip=2.3.4.5.txt"));
		assertEquals("3.4.5.6", MartusUtilities.extractIpFromFileName("code=x.y.z-ip=3.4.5.6.txt"));
	}

	ServerForMirroring server;
	MockMartusServer coreServer;
	LoggerToNull logger;

	MockMartusSecurity clientSecurity1;
	MockMartusSecurity clientSecurity2;

	BulletinHeaderPacket bhp1;
	BulletinHeaderPacket bhp2;
	BulletinHeaderPacket bhp3;
	BulletinHeaderPacket bhp4;
}
