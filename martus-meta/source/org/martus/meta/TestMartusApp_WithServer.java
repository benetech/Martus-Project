/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003-2014, Beneficent
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

package org.martus.meta;

import java.io.File;
import java.util.Date;
import java.util.Vector;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.BackgroundUploader;
import org.martus.client.swingui.Retriever;
import org.martus.client.swingui.UiConstants;
import org.martus.client.test.MockMartusApp;
import org.martus.client.test.NullProgressMeter;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.clientside.test.NoServerNetworkInterfaceForNonSSLHandler;
import org.martus.clientside.test.NoServerNetworkInterfaceHandler;
import org.martus.common.Exceptions.AccountNotFoundException;
import org.martus.common.Exceptions.NoFormsAvailableException;
import org.martus.common.Exceptions.ServerCallFailedException;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.FieldCollection;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusAccountAccessToken.TokenInvalidException;
import org.martus.common.MartusAccountAccessToken.TokenNotFoundException;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.VersionBuildDate;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.CustomFieldTemplate;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.network.ClientSideNetworkInterface;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.NonSSLNetworkAPIWithHelpers;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet.WrongAccountException;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.MockServerForClients;
import org.martus.server.forclients.ServerForClientsInterface;
import org.martus.server.forclients.ServerSideNetworkHandler;
import org.martus.server.forclients.ServerSideNetworkHandlerForNonSSL;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;

public class TestMartusApp_WithServer extends TestCaseEnhanced
{
    public TestMartusApp_WithServer(String name) throws Exception
    {
        super(name);
        VERBOSE = false;
	}

    public void setUp() throws Exception
    {
    		super.setUp();
    		TRACE_BEGIN("setUp");
		
		if(mockSecurityForApp == null)
			mockSecurityForApp = MockMartusSecurity.createClient();
		
		if(mockSecurityForServer == null)
			mockSecurityForServer = MockMartusSecurity.createServer();

		mockServer = new MockMartusServer();
		mockServer.serverForClients.loadBannedClients();
		mockServer.verifyAndLoadConfigurationFiles();
		mockServer.setSecurity(mockSecurityForServer);
		mockNonSSLServerHandler = new ServerSideNetworkHandlerForNonSSL(mockServer.serverForClients);
		mockSSLServerHandler = new MockServerInterfaceHandler(mockServer.serverForClients);

		if(appWithoutServer == null)
		{
			appWithoutServer = MockMartusApp.create(mockSecurityForApp, getName());
			ClientSideNetworkInterface noServer = new NoServerNetworkInterfaceHandler();
			appWithoutServer.setSSLNetworkInterfaceHandlerForTesting(noServer);
		}
		
		appWithServer = MockMartusApp.create(mockSecurityForApp, getName());
		appWithServer.setServerInfo("mock", mockServer.getAccountId(), "");
		appWithServer.setSSLNetworkInterfaceHandlerForTesting(mockSSLServerHandler);

		File keyPairFile = appWithServer.getCurrentKeyPairFile();
		keyPairFile.delete();
		appWithServer.getUploadInfoFile().delete();
		appWithServer.getConfigInfoFile().delete();
		appWithServer.getConfigInfoSignatureFile().delete();

		ProgressMeterInterface nullProgressMeter = new NullProgressMeter();
		uploaderWithServer = new BackgroundUploader(appWithServer, nullProgressMeter);		
		mockServer.deleteAllData();

		TRACE_END();
    }

    public void tearDown() throws Exception
    {
		mockServer.deleteAllFiles();

		appWithoutServer.deleteAllFiles();
		appWithServer.deleteAllFiles();
		super.tearDown();
	}

	public void testBasics()
	{
		TRACE_BEGIN("testBasics");

		ClientBulletinStore store = appWithServer.getStore();
		assertNotNull("BulletinStore", store);
		TRACE_END();
	}
	
	public void testDownloadFieldOfficeBulletins() throws Exception
	{
		TRACE_BEGIN("testDownloadFieldOfficeBulletins");
	
		MockMartusSecurity hqSecurity = MockMartusSecurity.createHQ();	
		MockMartusApp hqApp = MockMartusApp.create(hqSecurity, getName());
		hqApp.setServerInfo("mock", mockServer.getAccountId(), "");
		hqApp.setSSLNetworkInterfaceHandlerForTesting(mockSSLServerHandler);
		assertNotEquals("same public key?", appWithServer.getAccountId(), hqApp.getAccountId());
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key = new HeadquartersKey(hqApp.getAccountId());
		keys.add(key);
		appWithServer.setAndSaveHQKeys(keys, keys);
	
		String sampleSummary1 = "this is a basic summary";
		String sampleSummary2 = "another silly summary";
		String sampleSummary3 = "not my HQ";
		
		Bulletin b1 = appWithServer.createBulletin();
		b1.setAllPrivate(true);
		b1.set(Bulletin.TAGTITLE, sampleSummary1);
		b1.setSealed();
		appWithServer.setDefaultHQKeysInBulletin(b1);
		appWithServer.getStore().saveBulletin(b1);
		
		Bulletin b2 = appWithServer.createBulletin();
		b2.setAllPrivate(false);
		b2.set(Bulletin.TAGTITLE, sampleSummary2);
		b2.setSealed();
		appWithServer.setDefaultHQKeysInBulletin(b2);
		appWithServer.getStore().saveBulletin(b2);
		
		Bulletin b3 = appWithServer.createBulletin();
		b3.set(Bulletin.TAGTITLE, sampleSummary3);
		b3.setSealed();
		appWithServer.getStore().saveBulletin(b3);
	
		mockServer.allowUploads(appWithServer.getAccountId());
		assertEquals("failed upload1?", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b1));
		assertEquals("failed upload2?", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b2));
	
		Vector uidList = new Vector();
		uidList.add(b1.getUniversalId());
		uidList.add(b2.getUniversalId());
		Retriever retriever = new Retriever(hqApp, null);	
		retriever.retrieveBulletins(uidList, hqApp.createFolderRetrievedFieldOffice());
		assertEquals("retrieve field office bulletins failed?", NetworkInterfaceConstants.OK, retriever.getResult());
	
		uidList.clear();
		uidList.add(b3.getUniversalId());
		retriever.retrieveBulletins(uidList, hqApp.createFolderRetrievedFieldOffice());
		assertEquals("retrieve non-field office bulletins worked?", NetworkInterfaceConstants.INCOMPLETE, retriever.getResult());
	
		hqApp.deleteAllFiles();
		TRACE_END();
	}


	public void testGetMartusAccountAccessTokenFromServer() throws Exception 
	{
		try 
		{
			appWithoutServer.getMartusAccountAccessTokenFromServer();
			fail("Should have thrown an exception since we don't have a server");
		} 
		catch (ServerNotAvailableException expectedException) 
		{
		}

		MockServerForClients mockServerForClients = (MockServerForClients) mockServer.serverForClients;
		String invalidToken = "12345678";

		Vector invalidTokenResponse = new Vector();
		invalidTokenResponse.add(NetworkInterfaceConstants.OK);
		Vector invalidTokenData = new Vector();
		invalidTokenData.add(invalidToken);
		invalidTokenResponse.add(invalidTokenData.toArray());
		mockServerForClients.martusAccountAccessTokenResponse = invalidTokenResponse;
		try 
		{
			appWithServer.getMartusAccountAccessTokenFromServer();
			fail("Should have thrown an exception since server Has An Invalid Token");
		} 
		catch (TokenInvalidException expectedException) 
		{
		}
		
		Vector validTokenResponse = new Vector();
		validTokenResponse.add(NetworkInterfaceConstants.OK);
		Vector validTokenData = new Vector();
		String validToken = "55638914";
		validTokenData.add(validToken);
		validTokenResponse.add(validTokenData.toArray());
		mockServerForClients.martusAccountAccessTokenResponse = validTokenResponse;
		try 
		{
			MartusAccountAccessToken tokenRetrieved = appWithServer.getMartusAccountAccessTokenFromServer();
			assertEquals("Not the same token?", validToken, tokenRetrieved.getToken());
		} 
		catch (TokenInvalidException e) 
		{
			fail("Should not have thrown an exception since server now has a valid token for our account.");
		}
	}

	private String createJsonTokenResponse(String accountId, String token)
	{
		return "{\""+MartusAccountAccessToken.MARTUS_ACCOUNT_ACCESS_TOKEN_JSON_TAG+"\":{\""+MartusAccountAccessToken.MARTUS_ACCESS_TOKEN_CREATION_DATE_JSON_TAG+"\":\"02/15/2014 13:30:45\",\""+MartusAccountAccessToken.MARTUS_ACCESS_TOKEN_JSON_TAG+"\":\""+token+"\",\""+MartusAccountAccessToken.MARTUS_ACCESS_ACCOUNT_ID_JSON_TAG+"\":\""+accountId+"\"}}}";
	}
	
	public void testGetMartusAccountAccessAccountIdFromServer() throws Exception 
	{
		String validTokenString1 = "55638914";
		MartusAccountAccessToken validToken1 = new MartusAccountAccessToken(validTokenString1);
		String validTokenString2 = "11223344";
		MartusAccountAccessToken validToken2 = new MartusAccountAccessToken(validTokenString2);
		try 
		{
			appWithoutServer.getMartusAccountIdFromAccessTokenOnServer(validToken1);
			fail("Should have thrown an exception since we don't have a server");
		} 
		catch (ServerNotAvailableException expectedException) 
		{
		}

		MockServerForClients mockServerForClients = (MockServerForClients) mockServer.serverForClients;
		String clientId = appWithServer.getAccountId();
		mockServerForClients.martusAccountAccessJsonTokenResponse = createJsonTokenResponse(clientId, validTokenString1);
		try 
		{
			String accountIdRetrieved = appWithServer.getMartusAccountIdFromAccessTokenOnServer(validToken1);
			assertEquals("Not the same Id?", clientId, accountIdRetrieved);
		} 
		catch (TokenNotFoundException e) 
		{
			fail("Should not have thrown an exception since server now has a valid token for our account.");
		}
		
		try 
		{
			appWithServer.getMartusAccountIdFromAccessTokenOnServer(validToken2);
			fail("Should  have thrown an exception since we were looking for a Token that doesn't exist.");
		} 
		catch (TokenNotFoundException e) 
		{
		}
	}

	public void testPutAndGetListOfFormTemplates() throws Exception 
	{
		CustomFieldTemplate emptyTemplate = new CustomFieldTemplate();
		try 
		{
			appWithoutServer.putFormTemplateOnServer(emptyTemplate);
			fail("Should have thrown an exception since we don't have a server");
		} 
		catch (ServerNotAvailableException expectedException) 
		{
		}
		try 
		{
			appWithoutServer.getListOfFormTemplatesOnServer(appWithoutServer.getAccountId());
			fail("Should have also thrown an exception since we don't have a server");
		} 
		catch (ServerNotAvailableException expectedException) 
		{
		}

		try 
		{
			appWithoutServer.getFormTemplateOnServer(appWithoutServer.getAccountId(), "Some Title");
			fail("Should have also thrown an exception since we don't have a server");
		} 
		catch (ServerNotAvailableException expectedException) 
		{
		}
	
		try 
		{
			appWithServer.putFormTemplateOnServer(emptyTemplate);
			fail("Should have thrown an exception since the template is empty.");
		} 
		catch (Exception expected) 
		{
		}
	
		try 
		{
			appWithServer.getFormTemplateOnServer(appWithoutServer.getAccountId(), "Some Title");
			fail("Should have thrown an exception since the template doesn't exist on server.");
		} 
		catch (NoFormsAvailableException expected) 
		{
		}

		String formTemplateTitle1 = "New Form Title";
		String formTemplateDescription1 = "New Form Description";
		FieldCollection defaultFieldsTopSection1 = new FieldCollection(StandardFieldSpecs.getDefaultTopSetionFieldSpecs().asArray());
		FieldCollection defaultFieldsBottomSection1 = new FieldCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray());
		CustomFieldTemplate template1 = new CustomFieldTemplate(formTemplateTitle1, formTemplateDescription1, defaultFieldsTopSection1, defaultFieldsBottomSection1);
		String accountId = appWithServer.getAccountId();
		mockServer.allowUploads(accountId);
		appWithServer.putFormTemplateOnServer(template1);
		Vector returnedVectorListOfTemplatesFromServer = appWithServer.getListOfFormTemplatesOnServer(accountId);
		assertEquals("Did not return 1 item in the Vector? the title and description for this template?",1 , returnedVectorListOfTemplatesFromServer.size());
		Vector listOfTemplatesFromServer = (Vector)returnedVectorListOfTemplatesFromServer.get(0);
		assertEquals("Did not return the title for this template?",formTemplateTitle1, listOfTemplatesFromServer.get(0));
		assertEquals("Did not return the description for this template?",formTemplateDescription1, listOfTemplatesFromServer.get(1));
	
		CustomFieldTemplate templateReturned = appWithServer.getFormTemplateOnServer(accountId, formTemplateTitle1);
		assertEquals("Form title not what we asked for?",formTemplateTitle1 , templateReturned.getTitle());
		assertEquals("Form description not the same?",formTemplateDescription1 , templateReturned.getDescription());
		
		
		String formTemplateTitle2 = "New Form Title 2";
		String formTemplateDescription2 = "New Form Description 2";
		FieldCollection defaultFieldsTopSection2 = new FieldCollection(StandardFieldSpecs.getDefaultTopSetionFieldSpecs().asArray());
		FieldCollection defaultFieldsBottomSection2 = new FieldCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray());
		CustomFieldTemplate template2 = new CustomFieldTemplate(formTemplateTitle2, formTemplateDescription2, defaultFieldsTopSection2, defaultFieldsBottomSection2);
		appWithServer.putFormTemplateOnServer(template2);
		Vector returnedVectorListOfTemplatesFromServer2 = appWithServer.getListOfFormTemplatesOnServer(accountId);
		assertEquals("Did not return 2 items in the Vector? the title and description for both templates?",2 , returnedVectorListOfTemplatesFromServer2.size());
		
		Vector returnedTitles = getTitlesFromResults(returnedVectorListOfTemplatesFromServer2);
		Vector returnedDescritions = getDescriptionsFromResults(returnedVectorListOfTemplatesFromServer2);
		
		assertTrue("Did not return the title for the first template?",returnedTitles.contains(formTemplateTitle1));
		assertTrue("Did not return the title for the 2nd template?",returnedTitles.contains(formTemplateTitle2));
		assertTrue("Did not return the description for the first template?",returnedDescritions.contains(formTemplateDescription1));
		assertTrue("Did not return the description for the 2nd template?",returnedDescritions.contains(formTemplateDescription2));
		CustomFieldTemplate templateReturned2 = appWithServer.getFormTemplateOnServer(accountId, formTemplateTitle2);
		assertEquals("Form2 title not what we asked for?",formTemplateTitle2 , templateReturned2.getTitle());
		assertEquals("Form2 description not the same?",formTemplateDescription2 , templateReturned2.getDescription());
		
		String formTemplateDescription1New = "Form 1's new description";
		CustomFieldTemplate template1Updated = new CustomFieldTemplate(formTemplateTitle1, formTemplateDescription1New, defaultFieldsTopSection1, defaultFieldsBottomSection1);
		appWithServer.putFormTemplateOnServer(template1Updated);
		Vector returnedListOfTemplatesFromServer3 = appWithServer.getListOfFormTemplatesOnServer(accountId);
		assertEquals("Did not return 2 items in the Vector? the title and description for both templates?",2 , returnedListOfTemplatesFromServer3.size());
		
		Vector returnedTitles3 = getTitlesFromResults(returnedListOfTemplatesFromServer3);
		Vector returnedDescritions3 = getDescriptionsFromResults(returnedListOfTemplatesFromServer3);
		
		assertTrue("Did not return the same title for the first template?",returnedTitles3.contains(formTemplateTitle1));
		assertTrue("Did not return the same title for the 2nd template?",returnedTitles3.contains(formTemplateTitle2));
		assertFalse("Returned the old description for the first template?",returnedDescritions3.contains(formTemplateDescription1));
		assertTrue("Did not return the updated description for the first template?",returnedDescritions3.contains(formTemplateDescription1New));
		assertTrue("Did not return the same description for the 2nd template?",returnedDescritions3.contains(formTemplateDescription2));
		CustomFieldTemplate templateReturned3 = appWithServer.getFormTemplateOnServer(accountId, formTemplateTitle1);
		assertEquals("Form3 title not what we asked for?",formTemplateTitle1 , templateReturned3.getTitle());
		assertEquals("Form3 description not the same?",formTemplateDescription1New , templateReturned3.getDescription());
		
		String formTemplateTitleEmpty = "";
		String formTemplateDescriptionEmpty = "";
		FieldCollection defaultFieldsTopSection3 = new FieldCollection(StandardFieldSpecs.getDefaultTopSetionFieldSpecs().asArray());
		FieldCollection defaultFieldsBottomSection3 = new FieldCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray());
		CustomFieldTemplate template3 = new CustomFieldTemplate(formTemplateTitleEmpty, formTemplateDescriptionEmpty, defaultFieldsTopSection3, defaultFieldsBottomSection3);
		appWithServer.putFormTemplateOnServer(template3);
		Vector returnedListOfTemplatesFromServer4 = appWithServer.getListOfFormTemplatesOnServer(accountId);
		assertEquals("Did not return 3 items in the Vector? the title and description for all 3 templates?",3 , returnedListOfTemplatesFromServer4.size());
		
		Vector returnedTitles2 = getTitlesFromResults(returnedListOfTemplatesFromServer4);
		Vector returnedDescritions2 = getDescriptionsFromResults(returnedListOfTemplatesFromServer4);
		
		assertTrue("Did not return the title for the first template?",returnedTitles2.contains(formTemplateTitle1));
		assertTrue("Did not return the title for the 2nd template?",returnedTitles2.contains(formTemplateTitle2));
		assertTrue("Did not return the empty title for the 3rd template?",returnedTitles2.contains(formTemplateTitleEmpty));
		assertTrue("Did not return the description for the first template?",returnedDescritions2.contains(formTemplateDescription1New));
		assertTrue("Did not return the description for the 2nd template?",returnedDescritions2.contains(formTemplateDescription2));
		assertTrue("Did not return the empty description for the 3rd template?",returnedDescritions2.contains(formTemplateDescriptionEmpty));
		
		MockMartusSecurity otherSecurity = MockMartusSecurity.createOtherClient();	
		MockMartusApp otherClient = MockMartusApp.create(otherSecurity, "new Client");
		try 
		{
			appWithServer.getListOfFormTemplatesOnServer(otherClient.getAccountId());
			fail("Should have thrown the Account Not Found Exception.");
		} 
		catch (AccountNotFoundException expectedException) 
		{
		}
		
		try 
		{
			appWithServer.getFormTemplateOnServer(otherClient.getAccountId(), formTemplateTitle1);
			fail("Should have thrown no Forms Available Exception.");
		} 
		catch (NoFormsAvailableException expectedException)
		{
		}
	}
	
	private Vector getTitlesFromResults(Vector formTemplatesTitlesDescriptionsList)
	{
		Vector titlesOnly = new Vector();
		for(int i=0;i<formTemplatesTitlesDescriptionsList.size(); ++i)
		{
			Vector titleAndDescripton = (Vector)formTemplatesTitlesDescriptionsList.get(i);
			titlesOnly.add(titleAndDescripton.get(0));
		}
		return titlesOnly;
	}
	
	private Vector getDescriptionsFromResults(Vector formTemplatesTitlesDescriptionsList)
	{
		Vector descriptionsOnly = new Vector();
		for(int i=0;i<formTemplatesTitlesDescriptionsList.size(); ++i)
		{
			Vector titleAndDescripton = (Vector)formTemplatesTitlesDescriptionsList.get(i);
			descriptionsOnly.add(titleAndDescripton.get(1));
		}
		return descriptionsOnly;
	}

	public void testGetNewsFromServer() throws Exception
	{
		Vector noServerResult = appWithoutServer.getNewsFromServer();
		assertEquals(0, noServerResult.size());
		
		Vector noNews = new Vector();
		noNews.add(NetworkInterfaceConstants.OK);
		noNews.add(new Object[0]);
		MockServerForClients mockServerForClients = (MockServerForClients) mockServer.serverForClients;
		mockServerForClients.newsResponse = noNews;
		mockServerForClients.newsVersionLabelToCheck = UiConstants.versionLabel;
		Vector noNewsResponse = appWithServer.getNewsFromServer();
		assertEquals(0, noNewsResponse.size());
			
		Vector badNews = new Vector();
		badNews.add("Bad Response");
		Vector badNewsItems = new Vector();
		badNewsItems.add("news for you NOT");
		badNews.add(badNewsItems.toArray());
		mockServerForClients.newsResponse = badNews;
		
		
		Vector badNewsResponse = appWithServer.getNewsFromServer();
		assertEquals(0, badNewsResponse.size());

		final String firstNewsItem = "first news item";
		final String secondNewsItem = "second news item";
		Vector twoNews = new Vector();
		twoNews.add(NetworkInterfaceConstants.OK);
		Vector twoNewsItems = new Vector();
		twoNewsItems.add(firstNewsItem);
		twoNewsItems.add(secondNewsItem);
		twoNews.add(twoNewsItems.toArray());
		mockServerForClients.newsResponse = twoNews;
		
		Vector twoNewsResponseWithVersionLabelValid = appWithServer.getNewsFromServer();
		assertEquals(2, twoNewsResponseWithVersionLabelValid.size());
		assertEquals(firstNewsItem, twoNewsResponseWithVersionLabelValid.get(0));
		assertEquals(secondNewsItem, twoNewsResponseWithVersionLabelValid.get(1));

		mockServerForClients.newsVersionLabelToCheck = "";
		mockServerForClients.newsVersionBuildDateToCheck = VersionBuildDate.getVersionBuildDate();
		Vector twoNewsResponseWithBuildDateValid = appWithServer.getNewsFromServer();
		assertEquals(2, twoNewsResponseWithBuildDateValid.size());
		assertEquals(firstNewsItem, twoNewsResponseWithBuildDateValid.get(0));
		assertEquals(secondNewsItem, twoNewsResponseWithBuildDateValid.get(1));

		mockServerForClients.newsVersionLabelToCheck = "0.0.0";
		mockServerForClients.newsVersionBuildDateToCheck = "00/00/00";
		Vector twoNewsResponseWithInvalidVersionInfo = appWithServer.getNewsFromServer();
		assertEquals(0, twoNewsResponseWithInvalidVersionInfo.size());
	}
	
	public void testGetServerCompliance() throws Exception
	{
		try
		{
			appWithoutServer.getServerCompliance(appWithoutServer.getCurrentNetworkInterfaceGateway());
			fail("noServer should have thrown");
		}
		catch (ServerNotAvailableException expectedException)
		{
		}
		
		String sampleCompliance = "This server is compliant";
		Vector result = new Vector();
		result.add(NetworkInterfaceConstants.OK);
		Vector compliance = new Vector();
		compliance.add(sampleCompliance);
		result.add(compliance.toArray());
		mockServer.complianceResponse = result;

		String complianceResponse = appWithServer.getServerCompliance(appWithServer.getCurrentNetworkInterfaceGateway());
		assertEquals(sampleCompliance, complianceResponse);

		Vector failedCompliance = new Vector();
		failedCompliance.add(NetworkInterfaceConstants.NOT_AUTHORIZED);
		mockServer.complianceResponse = failedCompliance;
		try
		{
			appWithServer.getServerCompliance(appWithServer.getCurrentNetworkInterfaceGateway());
			fail("Should not have passed getServerCompliance request");
		}
		catch (ServerCallFailedException expectedException)
		{
		}

		Vector invalidResult = new Vector();
		invalidResult.add(NetworkInterfaceConstants.OK);
		invalidResult.add("bad second parameter");
		mockServer.complianceResponse = invalidResult;
		try
		{
			appWithServer.getServerCompliance(appWithServer.getCurrentNetworkInterfaceGateway());
			fail("Did not throw for invalid second parameter");
		}
		catch (ServerCallFailedException expectedException)
		{
		}

	}

	public void testSetServerInfo() throws Exception
	{
		final String server1 = "Server1";
		final String server2 = "Server2";
		final String key1 = "ServerKey1";
		final String key2 = "ServerKey2";
		final String serverCompliance1 = "Compliant1";
		final String serverCompliance2 = "Compliant2";
		
		MockMartusApp app = MockMartusApp.create(getName());
		app.setServerInfo(server1, key1, serverCompliance1);
		assertEquals("Didn't set Contactinfo name", server1, app.getConfigInfo().getServerName());
		assertEquals("Didn't set Contactinfo key", key1, app.getConfigInfo().getServerPublicKey());
		assertEquals("Didn't set Contactinfo compliance", serverCompliance1, app.getConfigInfo().getServerCompliance());
		assertNull("Should have cleared handler", app.currentNetworkInterfaceHandler);
		assertNull("Should have cleared gateway", app.currentNetworkInterfaceGateway);

		app.getCurrentNetworkInterfaceGateway();
		assertNotNull("Should have created handler", app.currentNetworkInterfaceHandler);
		assertNotNull("Should have created gateway", app.currentNetworkInterfaceGateway);

		app.setServerInfo(server2, key2, serverCompliance2);
		assertEquals("Didn't update Contactinfo name?", server2, app.getConfigInfo().getServerName());
		assertEquals("Didn't update Contactinfo key?", key2, app.getConfigInfo().getServerPublicKey());
		assertEquals("Didn't update Contactinfo compliance", serverCompliance2, app.getConfigInfo().getServerCompliance());
		assertNull("Should have re-cleared handler", app.currentNetworkInterfaceHandler);
		assertNull("Should have re-cleared gateway", app.currentNetworkInterfaceGateway);
		
		app.loadConfigInfo();
		assertEquals("Didn't save Contactinfo name?", server2, app.getConfigInfo().getServerName());
		assertEquals("Didn't save Contactinfo key?", key2, app.getConfigInfo().getServerPublicKey());
		assertEquals("Didn't save Contactinfo compliance?", serverCompliance2, app.getConfigInfo().getServerCompliance());
		
		app.deleteAllFiles();

	}
	
	public void testIsSSLServerAvailable() throws Exception
	{
		assertEquals(false, appWithoutServer.isSSLServerAvailable());
		assertEquals(true, appWithServer.isSSLServerAvailable());
		MockMartusApp appWithoutServerName = MockMartusApp.create(getName());
		assertEquals("uninitialized app server available?", false, appWithoutServerName.isSSLServerAvailable());

		ClientSideNetworkGateway gateway = ClientSideNetworkGateway.buildGateway("", "", null);
		assertNull("Empty server name", gateway);

		assertNull("No proxy?", appWithoutServerName.currentNetworkInterfaceHandler);
		appWithoutServerName.deleteAllFiles();
	}


	public void testGetServerPublicKeyNoServer() throws Exception
	{
		try
		{
			NoServerNetworkInterfaceForNonSSLHandler noServer = new NoServerNetworkInterfaceForNonSSLHandler();
			appWithoutServer.getServerPublicKey(noServer);
			fail("Should have thrown");
		}
		catch(ServerNotAvailableException expectedException)
		{
		}
	}
		
	public void testGetServerPublicKeyInvalidResponse() throws Exception
	{
		Vector response = new Vector();
		response.add(NetworkInterfaceConstants.OK);
		mockServer.infoResponse = response;
		try
		{
			appWithServer.getServerPublicKey(mockNonSSLServerHandler);
			fail("Should have thrown");
		}
		catch(PublicInformationInvalidException expectedException)
		{
		}
		mockServer.infoResponse = null;
	}
	
	public void testGetServerPublicKey() throws Exception
	{
		MockMartusSecurity securityWithAccount = MockMartusSecurity.createOtherClient();
		mockServer.setSecurity(securityWithAccount);
		String publicKey = appWithServer.getServerPublicKey(mockNonSSLServerHandler);
		assertEquals("wrong key?", securityWithAccount.getPublicKeyString(), publicKey);
		mockServer.setSecurity(mockSecurityForServer);
	}
	

	public void testRequestServerUploadRights() throws Exception
	{
		String clientId = appWithServer.getAccountId();
		mockSecurityForApp.loadSampleAccount();
		mockServer.serverForClients.addMagicWordForTesting(sampleMagicWord, null);
		
		assertEquals("can upload already?", true, mockServer.canClientUpload(clientId));
		assertEquals("wrong word worked?", false, appWithServer.requestServerUploadRights("wrong word"));
		assertEquals("empty word worked?", true, appWithServer.requestServerUploadRights(""));
		assertEquals("can upload?", true, mockServer.canClientUpload(clientId));
		
		mockServer.subtractMaxFailedUploadAttemptsFromServerCounter();
		
		assertEquals("right word failed?", true, appWithServer.requestServerUploadRights(sampleMagicWord));
		assertEquals("still can't upload?", true, mockServer.canClientUpload(clientId));
		assertEquals("empty word failed after right word passed?", true, appWithServer.requestServerUploadRights(""));
		mockServer.serverForClients.addMagicWordForTesting(null, null);
	}

	public void testGetGroupNameForMagicWord() throws Exception
	{
		String magicWord = "Magic Word";
		String groupName = "My Test Group";
		mockServer.serverForClients.addMagicWordForTesting(magicWord, groupName);
		assertEquals("Group names should match for valid magic word", groupName,mockServer.serverForClients.getGroupNameForMagicWord(magicWord));
		assertEquals("Group names should match for valid magic word", groupName,mockServer.serverForClients.getGroupNameForMagicWord(magicWord.toLowerCase()));
	}
		
	public void testGetHumanReadableMagicWord() throws Exception
	{
		String magicWord = "Magic Word";
		String groupName = "My Test Group";
		mockServer.serverForClients.addMagicWordForTesting(magicWord, groupName);
		assertEquals("Human Readable should match for valid magic word", magicWord,mockServer.serverForClients.getHumanReadableMagicWord(magicWord));
		assertEquals("Group names should match for valid magic word", magicWord,mockServer.serverForClients.getHumanReadableMagicWord(magicWord.toLowerCase()));
	}

	public void testIsValidMagicWord() throws Exception
	{
		String magicWord = "Magic Word";
		String groupName = "My Test Group";
		mockServer.serverForClients.addMagicWordForTesting(magicWord, groupName);
		assertTrue("exact magic word should match", mockServer.serverForClients.isValidMagicWord(magicWord));
		assertTrue("lowercase magic word should match", mockServer.serverForClients.isValidMagicWord(magicWord.toLowerCase()));
	}
	
	class MockMartusServerChunks extends MockMartusServer 
	{
	
		public MockMartusServerChunks() throws Exception 
		{
			super();
		}

		public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data) 
		{
			fail("Should not be called, using chunks");
			return "";
		}

		public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data, String signature) 
		{
			fail("Should not be called--use putBulletinChunk instead!");
			return "";
		}

		public String putBulletinChunk(String uploaderAccountId, String authorAccountId, String bulletinLocalId,
			int totalSize, int chunkOffset, int chunkSize, String data) 
		{
			++chunkCount;
			return super.putBulletinChunk(uploaderAccountId, authorAccountId, bulletinLocalId,
				totalSize, chunkOffset, chunkSize, data);
		}

		int chunkCount;

	}
	
	public void testUploadBulletinUsesChunks() throws Exception
	{
		ClientSideNetworkInterface oldSSLServer = appWithServer.currentNetworkInterfaceHandler;
		MockMartusServerChunks server = new MockMartusServerChunks();
		server.verifyAndLoadConfigurationFiles();
		server.setSecurity(mockSecurityForServer);
		server.serverForClients.clearCanUploadList();
		server.allowUploads(appWithServer.getAccountId());
		server.serverForClients.loadBannedClients();
		appWithServer.setSSLNetworkInterfaceHandlerForTesting(new ServerSideNetworkHandler(server.serverForClients));
		appWithServer.serverChunkSize = 100;
		Bulletin b = appWithServer.createBulletin();
		b.setSealed();
		appWithServer.getStore().saveBulletin(b);
		assertEquals("result not ok?", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b));
		assertTrue("count not > 1?", server.chunkCount > 1);

		server.uploadResponse = NetworkInterfaceConstants.INVALID_DATA;
		assertEquals("result ok?", NetworkInterfaceConstants.INVALID_DATA, uploaderWithServer.uploadBulletin(b));

		appWithServer.setSSLNetworkInterfaceHandlerForTesting(oldSSLServer);
		appWithServer.serverChunkSize = NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE;
		
		server.deleteAllFiles();
	}

	public void testRetrieveBulletinsOnlyBadId() throws Exception
	{
		TRACE_BEGIN("testRetreiveBulletinsBadId");
		mockSecurityForApp.loadSampleAccount();
		assertTrue("must be able to ping", appWithServer.isSSLServerAvailable());

		mockServer.setDownloadResponseNotFound();
		Vector badList = new Vector();
		badList.add("not an id");
		Retriever retriever = new Retriever(appWithServer, null);	
		retriever.retrieveBulletins(badList, appWithServer.createFolderRetrieved());
		assertEquals(NetworkInterfaceConstants.INCOMPLETE, retriever.getResult());
		mockServer.setDownloadResponseReal();
		TRACE_END();
	}

	public void testRetrieveBulletinsWithOneBadId() throws Exception
	{
		TRACE_BEGIN("testRetreiveBulletinsBadId");
		mockSecurityForApp.loadSampleAccount();
		assertTrue("must be able to ping", appWithServer.isSSLServerAvailable());

		Bulletin b1 = appWithServer.createBulletin();
		b1.setSealed();
		appWithServer.getStore().saveBulletin(b1);
		Bulletin b2 = appWithServer.createBulletin();
		b2.setSealed();
		appWithServer.getStore().saveBulletin(b2);
		Bulletin b3 = appWithServer.createBulletin();
		b3.set(Bulletin.TAGAUTHOR, "author");
		b3.setSealed();
		appWithServer.getStore().saveBulletin(b3);
		mockServer.allowUploads(appWithServer.getAccountId());
		assertEquals("upload b1", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b1));
		assertEquals("upload b2", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b2));
		assertEquals("upload b3", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b3));

		ClientBulletinStore store = appWithServer.getStore();
		store.destroyBulletin(b1);
		store.destroyBulletin(b2);
		store.destroyBulletin(b3);
		
		Vector withBadId = new Vector();
		withBadId.add(b1.getUniversalId());
		withBadId.add(b2.getUniversalId());
		withBadId.add(UniversalIdForTesting.createDummyUniversalId());
		withBadId.add(b3.getUniversalId());

		Retriever retriever = new Retriever(appWithServer, null);	
		retriever.retrieveBulletins(withBadId, appWithServer.createFolderRetrieved());
		assertEquals("retrieve all", NetworkInterfaceConstants.INCOMPLETE, retriever.getResult());
		assertEquals("not back to three?", 3, store.getBulletinCount());

		TRACE_END();
	}

	public void testRetrieveBulletinsEmptyList() throws Exception
	{
		TRACE_BEGIN("testRetrieveBulletinsEmptyList");

		assertTrue("must be able to ping", appWithServer.isSSLServerAvailable());
		
		appWithServer.getStore().deleteAllData();
		ClientBulletinStore store = appWithServer.getStore();
		assertEquals("No bulletins", 0, store.getBulletinCount());

		Vector empty = new Vector();
		Retriever retriever = new Retriever(appWithServer, null);	
		retriever.retrieveBulletins(empty, appWithServer.createFolderRetrieved());
		assertEquals("empty", NetworkInterfaceConstants.OK, retriever.getResult());
		assertEquals("Empty didn't even ask", null, mockServer.lastClientId);
		assertEquals("Empty didn't download", 0, store.getBulletinCount());

		TRACE_END();
	}

	public void testSendAlreadySentBulletin() throws Exception
	{
		TRACE_BEGIN("testSendAlreadySentBulletin");

		Bulletin b1 = appWithServer.createBulletin();
		b1.setSealed();
		appWithServer.getStore().saveBulletin(b1);
		mockServer.allowUploads(appWithServer.getAccountId());
		assertEquals("upload b1", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b1));

		Bulletin b2 = appWithServer.createBulletin();
		b2.setSealed();
		appWithServer.getStore().saveBulletin(b2);
		appWithServer.getStore().setIsOnServer(b2);
		assertEquals("try to upload sealded b2 that is already on server, should be rejected as duplicate.", NetworkInterfaceConstants.DUPLICATE, uploaderWithServer.uploadBulletin(b2));
		
		Bulletin b3 = appWithServer.createBulletin();
		b3.setDraft();
		appWithServer.getStore().saveBulletin(b3);
		appWithServer.getStore().setIsOnServer(b3);
		assertEquals("try to upload draft b3 that is already on server, should be ok.", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b3));

		TRACE_END();
	}
		
	public void testRetrieveBulletins() throws Exception
	{
		TRACE_BEGIN("testRetrieveBulletins");

		assertTrue("must be able to ping", appWithServer.isSSLServerAvailable());
	
		ClientBulletinStore store = appWithServer.getStore();

		Bulletin b1 = appWithServer.createBulletin();
		b1.setSealed();
		appWithServer.getStore().saveBulletin(b1);
		
		Vector justB1 = new Vector();
		justB1.add(b1.getUniversalId());
		Retriever retriever = new Retriever(appWithServer, null);	
		retriever.retrieveBulletins(justB1, appWithServer.createFolderRetrieved());
		assertEquals("justB1", NetworkInterfaceConstants.OK, retriever.getResult());
		assertEquals("justB1 didn't even ask", null, mockServer.lastClientId);
		assertEquals("justB1 didn't download", 1, store.getBulletinCount());

		Vector nonExistantUidList = new Vector();
		UniversalId uId1 = UniversalIdForTesting.createDummyUniversalId();
		nonExistantUidList.add(uId1);
		
		Vector errorResponse = new Vector();
		String errorString = "some unknown error";
		errorResponse.add(errorString);

		mockServer.downloadResponse = errorResponse;
		retriever.retrieveBulletins(nonExistantUidList, appWithServer.createFolderRetrieved());
		assertEquals("unknownId", NetworkInterfaceConstants.INCOMPLETE, retriever.getResult());
		mockServer.downloadResponse = null;

		Bulletin b2 = appWithServer.createBulletin();
		b2.setSealed();
		appWithServer.getStore().saveBulletin(b2);
		Bulletin b3 = appWithServer.createBulletin();
		b3.set(Bulletin.TAGAUTHOR, "author");
		b3.setSealed();
		appWithServer.getStore().saveBulletin(b3);
		mockServer.allowUploads(appWithServer.getAccountId());
		assertEquals("upload b1", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b1));
		assertEquals("upload b2", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b2));
		assertEquals("upload b3", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b3));
		store.destroyBulletin(b1);
		store.destroyBulletin(b3);
		assertEquals("not just one left?", 1, store.getBulletinCount());
		
		Vector allThree = new Vector();
		allThree.add(b1.getUniversalId());
		allThree.add(b2.getUniversalId());
		allThree.add(b3.getUniversalId());
		retriever.retrieveBulletins(allThree, appWithServer.createFolderRetrieved());
		assertEquals("retrieve all", NetworkInterfaceConstants.OK, retriever.getResult());
		assertEquals("not back to three?", 3, store.getBulletinCount());
		
		Bulletin b3got = store.getBulletinRevision(b3.getUniversalId());
		assertEquals("missing author?", b3.get(Bulletin.TAGAUTHOR), b3got.get(Bulletin.TAGAUTHOR));
		
		TRACE_END();
	}
	
	public void testRetrieveBulletinManyChunks() throws Exception
	{
		TRACE_BEGIN("testRetrieveBulletinThreeChunks");

		Bulletin b = createAndUploadSampleBulletin();
		
		appWithServer.serverChunkSize = 100;
		ClientBulletinStore store = appWithServer.getStore();
		appWithServer.retrieveOneBulletinToFolder(b.getUniversalId(), store.getFolderDiscarded(), null);
		
		appWithServer.serverChunkSize = NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE;
		
		TRACE_END();
	}

	public void testRetrieveBulletinResponseSimple() throws Exception
	{
		TRACE_BEGIN("testRetrieveBulletinResponseSimple");

		Bulletin b = createAndUploadSampleBulletin();
		byte[] bulletinBytes = getBulletinZipBytes(b);
		int totalSize = bulletinBytes.length;
		int chunkSize = totalSize;

		Vector response = new Vector();
		response.add(NetworkInterfaceConstants.OK);
		response.add(new Integer(totalSize));
		response.add(new Integer(chunkSize));
		response.add(StreamableBase64.encode(bulletinBytes));
		mockServer.downloadResponse = response;
		
		ClientBulletinStore store = appWithServer.getStore();
		assertFalse("we don't know it is on server.", store.isProbablyOnServer(b.getUniversalId()));
		appWithServer.retrieveOneBulletinToFolder(b.getUniversalId(), appWithServer.getFolderDiscarded(), null);
		assertTrue("didn't set on server?", store.isProbablyOnServer(b.getUniversalId()));
		assertFalse("didn't clear not on server?", store.isProbablyNotOnServer(b.getUniversalId()));
		mockServer.setDownloadResponseReal();
		
		TRACE_END();
	}
	
	public void testRetrieveBulletinResponseChunkSizeInvalid() throws Exception
	{
		TRACE_BEGIN("testRetrieveBulletinResponseChunkSizeInvalid");

		Bulletin b = createAndUploadSampleBulletin();
		byte[] bulletinBytes = getBulletinZipBytes(b);
		int totalSize = bulletinBytes.length;
		int chunkSize = -1;

		Vector response = new Vector();
		response.add(NetworkInterfaceConstants.OK);
		response.add(new Integer(totalSize));
		response.add(new Integer(chunkSize));
		response.add(BulletinForTesting.saveToZipString(appWithServer.getStore().getDatabase(), b, mockSecurityForServer));
		mockServer.downloadResponse = response;
		
		try
		{
			appWithServer.retrieveOneBulletinToFolder(b.getUniversalId(), appWithServer.getFolderDiscarded(), null);
			fail("Should have thrown");
		}
		catch(Exception ignoreExpectedException)
		{
		}
		
		mockServer.setDownloadResponseReal();
		
		TRACE_END();
	}
	
	public void testRetrieveBulletinResponseTotalSizeInvalid() throws Exception
	{
		TRACE_BEGIN("testRetrieveBulletinResponseTotalSizeInvalid");

		Bulletin b = createAndUploadSampleBulletin();
		byte[] bulletinBytes = getBulletinZipBytes(b);

		Vector response = new Vector();
		response.add(NetworkInterfaceConstants.OK);
		response.add(new Integer(-1));
		response.add(new Integer(bulletinBytes.length));
		response.add(BulletinForTesting.saveToZipString(appWithServer.getStore().getDatabase(),b, mockSecurityForServer));
		mockServer.downloadResponse = response;
		
		try
		{
			appWithServer.retrieveOneBulletinToFolder(b.getUniversalId(), appWithServer.getFolderDiscarded(), null);
			fail("Should have thrown");
		}
		catch(Exception ignoreExpectedException)
		{
		}
		
		mockServer.setDownloadResponseReal();
		
		TRACE_END();
	}
	
	public void testRetrieveBulletinResponseChunkSizeLargerThanRemainingSize() throws Exception
	{
		TRACE_BEGIN("testRetrieveBulletinResponseChunkSizeLargerThanRemainingSize");

		Bulletin b = createAndUploadSampleBulletin();
		byte[] bulletinBytes = getBulletinZipBytes(b);

		Vector response = new Vector();
		response.add(NetworkInterfaceConstants.CHUNK_OK);
		response.add(new Integer(bulletinBytes.length));
		response.add(new Integer(bulletinBytes.length / 3 * 2));
		response.add(BulletinForTesting.saveToZipString(appWithServer.getStore().getDatabase(),b, mockSecurityForServer));
		mockServer.downloadResponse = response;
		
		try
		{
			appWithServer.retrieveOneBulletinToFolder(b.getUniversalId(), appWithServer.getFolderDiscarded(), null);
			fail("Should have thrown");
		}
		catch(Exception ignoreExpectedException)
		{
		}
		
		mockServer.setDownloadResponseReal();
		
		TRACE_END();
	}
	
	public void testRetrieveBulletinResponseIncorrectChunkSize() throws Exception
	{
		TRACE_BEGIN("testRetrieveBulletinResponseIncorrectChunkSize");

		Bulletin b = createAndUploadSampleBulletin();
		byte[] sampleBytes = "Testing".getBytes();

		Vector response = new Vector();
		response.add(NetworkInterfaceConstants.CHUNK_OK);
		response.add(new Integer(sampleBytes.length));
		response.add(new Integer(sampleBytes.length-1));
		response.add(StreamableBase64.encode(sampleBytes));
		mockServer.downloadResponse = response;
		
		try
		{
			appWithServer.retrieveOneBulletinToFolder(b.getUniversalId(), appWithServer.getFolderDiscarded(), null);
			fail("Should have thrown");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}
		
		mockServer.setDownloadResponseReal();
		
		TRACE_END();
	}
	
	public void testRetrieveBulletinResponseIncorrectTotalSize() throws Exception
	{
		TRACE_BEGIN("testRetrieveBulletinResponseIncorrectTotalSize");

		Bulletin b = createAndUploadSampleBulletin();
		byte[] sampleBytes = "Testing".getBytes();

		Vector response = new Vector();
		response.add(NetworkInterfaceConstants.OK);
		response.add(new Integer(sampleBytes.length+1));
		response.add(new Integer(sampleBytes.length));
		response.add(StreamableBase64.encode(sampleBytes));
		mockServer.downloadResponse = response;
		
		try
		{
			appWithServer.retrieveOneBulletinToFolder(b.getUniversalId(), appWithServer.getFolderDiscarded(), null);
			fail("Should have thrown");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}
		
		mockServer.setDownloadResponseReal();
		
		TRACE_END();
	}
	
	public void testRetrieveBulletinsNoServer() throws Exception
	{
		TRACE_BEGIN("testRetrieveBulletinsNoServer");
		mockSecurityForApp.loadSampleAccount();

		Vector uploadedIdList = new Vector();
		uploadedIdList.add("sample id");
		Retriever retriever = new Retriever(appWithoutServer, null);	
		retriever.retrieveBulletins(uploadedIdList, appWithoutServer.createFolderRetrieved());
		assertEquals(NetworkInterfaceConstants.NO_SERVER, retriever.getResult());

		TRACE_END();
	}
	
	class MockGateway extends ClientSideNetworkGateway
	{
		MockGateway()
		{
			super(null);
		}
		
		public NetworkResponse deleteServerDraftBulletins(MartusCrypto signer, 
						String authorAccountId, String[] bulletinLocalIds) throws 
				MartusCrypto.MartusSignatureException
		{
			if(throwSigError)
				throw new MartusCrypto.MartusSignatureException();
				
			gotSigner = signer;
			gotAuthor = authorAccountId;
			gotIds = bulletinLocalIds;
			return new NetworkResponse(response);
		}
		
		public NetworkResponse	putContactInfo(MartusCrypto signer, String authorAccountId, Vector contactInfo) throws 
			MartusCrypto.MartusSignatureException
		{
			if(throwSigError)
				throw new MartusCrypto.MartusSignatureException();
			gotSigner = signer;
			gotAuthor = authorAccountId;
			gotContactInfo = contactInfo;
			return new NetworkResponse(response);
		}

		
		MartusCrypto gotSigner;
		String gotAuthor;
		String[] gotIds;
		Vector gotContactInfo;
		Vector response;
		boolean throwSigError;
	}

	public void testDeleteServerDraftBulletins() throws Exception
	{
		appWithServer.setServerInfo("mock", mockServer.getAccountId(), "");
		MockGateway gateway = new MockGateway();

		MartusCrypto security = MockMartusSecurity.createClient();
		MockMartusApp app= MockMartusApp.create(security, getName());
		app.currentNetworkInterfaceGateway = gateway;
		
		Vector uids = new Vector();
		uids.add(BulletinHeaderPacket.createUniversalId(security));

		Vector mockResponse = new Vector();
		mockResponse.clear();
		mockResponse.add(NetworkInterfaceConstants.OK);
		gateway.response = mockResponse;
		uids.add(BulletinHeaderPacket.createUniversalId(security));
		uids.add(BulletinHeaderPacket.createUniversalId(security));
		String result = app.deleteServerDraftBulletins(uids);
		assertEquals("wrong result?", mockResponse.get(0), result);
		assertEquals("wrong crypto?", app.getSecurity(), gateway.gotSigner);
		assertEquals("wrong author?", app.getAccountId(), gateway.gotAuthor);
		assertEquals("wrong id count?", uids.size(), gateway.gotIds.length);
		for (int i = 0; i < gateway.gotIds.length; i++)
		{
			assertEquals("missing id " + i, ((UniversalId)uids.get(i)).getLocalId(), gateway.gotIds[i]);
		}
		
		gateway.throwSigError = true;
		try
		{
			app.deleteServerDraftBulletins(uids);
			fail("Should have thrown for sig error (no key pair)");
		}
		catch (MartusSignatureException ignoreExpectedException)
		{
		}
		gateway.throwSigError = false;

		uids.add(BulletinHeaderPacket.createUniversalId(mockServer.getSecurity()));
		try
		{
			app.deleteServerDraftBulletins(uids);
			fail("Should have thrown for wrong account");
		}
		catch (WrongAccountException ignoreExpectedException)
		{
		}
		app.deleteAllFiles();
	}

	public void testPutContactInfo() throws Exception
	{
		appWithServer.setServerInfo("mock", mockServer.getAccountId(), "");
		MockGateway gateway = new MockGateway();

		MartusCrypto security = MockMartusSecurity.createClient();
		MockMartusApp app= MockMartusApp.create(security, getName());
		app.currentNetworkInterfaceGateway = gateway;
		
		Vector contact = new Vector();
		contact.add("PublicKey");
		contact.add(new Integer(2));
		contact.add("Author");
		contact.add("Address");
		contact.add("Signature");

		Vector mockResponse = new Vector();
		mockResponse.clear();
		mockResponse.add(NetworkInterfaceConstants.OK);
		gateway.response = mockResponse;

		ProgressMeterInterface nullProgressMeter = new NullProgressMeter();
		BackgroundUploader uploader = new BackgroundUploader(app, nullProgressMeter);
		String result = uploader.putContactInfoOnServer(contact);
		assertEquals("wrong result?", mockResponse.get(0), result);
		assertEquals("wrong crypto?", app.getSecurity(), gateway.gotSigner);
		assertEquals("wrong author?", app.getAccountId(), gateway.gotAuthor);
		assertEquals("wrong vector count?", contact.size(), gateway.gotContactInfo.size());
		for (int i = 0; i < gateway.gotContactInfo.size(); i++)
		{
			assertEquals("missing contents " + i, contact.get(i), gateway.gotContactInfo.get(i));
		}
		
		gateway.throwSigError = true;
		try
		{
			uploader.putContactInfoOnServer(contact);
			fail("Should have thrown for sig error (no key pair)");
		}
		catch (MartusSignatureException ignoreExpectedException)
		{
		}
		gateway.throwSigError = false;
		app.deleteAllFiles();
	}


	public void testGetFieldOfficeAccountsNoServer() throws Exception
	{
		TRACE_BEGIN("testGetFieldOfficeAccountsNoServer");
		try
		{
			appWithoutServer.downloadFieldOfficeAccountIds();
			fail("Got valid accounts?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}
		
		TRACE_END();
	}

	

	public void testGetFieldOfficeAccountsErrors() throws Exception
	{
		TRACE_BEGIN("testGetFieldOfficeAccountsErrors");
		assertTrue("must be able to ping", appWithServer.isSSLServerAvailable());


		mockSSLServerHandler.nullGetFieldOfficeAccountIds(true);
		try
		{
			appWithServer.downloadFieldOfficeAccountIds();
			fail("null response didn't throw?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}
		mockSSLServerHandler.nullGetFieldOfficeAccountIds(false);
				
		Vector desiredResult = new Vector();

		desiredResult.add(NetworkInterfaceConstants.REJECTED);
		mockServer.listFieldOfficeAccountsResponse = desiredResult;
		try
		{
			appWithServer.downloadFieldOfficeAccountIds();
			fail("rejected didn't throw?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}
		mockServer.listFieldOfficeAccountsResponse = null;

		TRACE_END();
	}

	public void testRetrieveHeaderPacket() throws Exception
	{
		TRACE_BEGIN("testRetrievePublicDataPacket");

		String accountId = appWithServer.getAccountId();
		mockServer.allowUploads(accountId);

		Bulletin b1 = appWithServer.createBulletin();
		UniversalId uid = b1.getUniversalId();		
		b1.setAllPrivate(true);

		b1.setDraft();
		appWithServer.getStore().saveBulletin(b1);
		assertEquals("failed upload1?", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b1));
		
		BulletinHeaderPacket bhpDraft = appWithServer.retrieveHeaderPacketFromServer(uid);
		assertEquals(BulletinConstants.STATUSDRAFT, bhpDraft.getStatus());
		
		b1.setSealed();
		appWithServer.getStore().saveBulletin(b1);
		assertEquals("failed upload2?", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b1));
		
		BulletinHeaderPacket bhpSealed = appWithServer.retrieveHeaderPacketFromServer(uid);
		assertEquals(BulletinConstants.STATUSSEALED, bhpSealed.getStatus());
		
	}

	
	public void testRetrievePublicDataPacket() throws Exception
	{
		TRACE_BEGIN("testRetrievePublicDataPacket");
		String sampleSummary1 = "this is a basic summary";
		String sampleSummary2 = "another silly summary";
		
		Bulletin b1 = appWithServer.createBulletin();
		b1.setAllPrivate(true);
		b1.set(Bulletin.TAGTITLE, sampleSummary1);
		b1.setSealed();
		appWithServer.getStore().saveBulletin(b1);
		
		Bulletin b2 = appWithServer.createBulletin();
		b2.setAllPrivate(false);
		b2.set(Bulletin.TAGTITLE, sampleSummary2);
		b2.setSealed();
		appWithServer.getStore().saveBulletin(b2);

		String accountId = appWithServer.getAccountId();
		mockServer.allowUploads(accountId);
		assertEquals("failed upload1?", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b1));
		assertEquals("failed upload2?", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b2));

		String fdpId1 = b1.getFieldDataPacket().getLocalId();		
		String fdpId2 = b2.getFieldDataPacket().getLocalId();		

		FieldDataPacket fdp1 = appWithServer.retrieveFieldDataPacketFromServer(b1.getUniversalId(), fdpId1);
		FieldDataPacket fdp2 = appWithServer.retrieveFieldDataPacketFromServer(b2.getUniversalId(), fdpId2);

		String title1 = fdp1.get(Bulletin.TAGTITLE);
		String title2 = fdp2.get(Bulletin.TAGTITLE);
		
		assertEquals("Bad title1", sampleSummary1, title1);
		assertEquals("Bad title2", sampleSummary2, title2);
		TRACE_END();
	}
	
	public void testRetrievePublicDataPacketErrors() throws Exception
	{
		TRACE_BEGIN("testRetrievePublicDataPacketErrors");
		try
		{
			UniversalId bogusUid = UniversalId.createFromAccountAndLocalId("account", "123");
			appWithServer.retrieveFieldDataPacketFromServer(bogusUid, "xyz");
			fail("Didn't throw Error for bad ID?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}
		
		try
		{
			Bulletin b1 = appWithServer.createBulletin();
			String fdpId1 = b1.getFieldDataPacket().getLocalId();		
			appWithServer.retrieveFieldDataPacketFromServer(b1.getUniversalId(), fdpId1);
			fail("Didn't throw Error for bad Missing Packet on Server");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}

		TRACE_END();
	}
	
	public void testGetFieldOfficeAccounts() throws Exception
	{
		TRACE_BEGIN("testGetFieldOfficeAccounts");
		Vector desiredResult = new Vector();

		desiredResult.add(NetworkInterfaceConstants.OK);
		desiredResult.add("Account1");
		desiredResult.add("Account2");
		
		mockServer.listFieldOfficeAccountsResponse = desiredResult;
		Vector result = appWithServer.downloadFieldOfficeAccountIds();
		mockServer.listFieldOfficeAccountsResponse = null;
		assertNotNull("Got back null?", result);
		assertEquals("Wrong size?", 2, result.size());
		assertEquals("Wrong account?", "Account1", result.get(0));
		assertEquals("Wrong 2nd account?", "Account2", result.get(1));
		TRACE_END();
	}
	
	public void testIsDraftOutboxEmpty() throws Exception
	{
		TRACE_BEGIN("testIsDraftOutboxEmpty");
		File file = appWithServer.getUploadInfoFile();
		file.delete();
		ClientBulletinStore store = appWithServer.getStore();

		store.deleteAllData();
		BulletinFolder draftOutbox = appWithServer.getFolderDraftOutbox();
		assertEquals("Draft outbox not empty", 0, draftOutbox.getBulletinCount());
		assertEquals("No file and draft outbox empty", true, appWithServer.isDraftOutboxEmpty());

		Bulletin b = appWithServer.createBulletin();
		appWithServer.getStore().saveBulletin(b);
		store.addBulletinToFolder(draftOutbox, b.getUniversalId());
		assertEquals("Draft file got created somehow?", false, file.exists());
		assertEquals("Draft outbox empty", 1, draftOutbox.getBulletinCount());
		assertEquals("No file and draft outbox contains data", false, appWithServer.isDraftOutboxEmpty());
		TRACE_END();
	}

	public void testIsSealedOutboxEmpty() throws Exception
	{
		TRACE_BEGIN("testIsSealedOutboxEmpty");
		File file = appWithServer.getUploadInfoFile();
		file.delete();
		ClientBulletinStore store = appWithServer.getStore();

		store.deleteAllData();
		BulletinFolder sealedOutbox = appWithServer.getFolderSealedOutbox();
		assertEquals("Draft outbox not empty", 0, sealedOutbox.getBulletinCount());
		assertEquals("No file and draft outbox empty", true, appWithServer.isSealedOutboxEmpty());

		Bulletin b = appWithServer.createBulletin();
		b.setSealed();
		appWithServer.getStore().saveBulletin(b);
		store.addBulletinToFolder(sealedOutbox, b.getUniversalId());
		assertEquals("Sealed file got created somehow?", false, file.exists());
		assertEquals("Sealed outbox empty", 1, sealedOutbox.getBulletinCount());
		assertEquals("No file and sealed outbox contains data", false, appWithServer.isSealedOutboxEmpty());
		TRACE_END();
	}

	public void testUploadInfo()
	{
		TRACE_BEGIN("testUploadInfo");
		File file = appWithServer.getUploadInfoFile();
		file.delete();
		assertEquals("getLastUploadedTime invalid", null, appWithServer.getLastUploadedTime());
		assertEquals("getLastUploadRemindedTime invalid", null, appWithServer.getLastUploadRemindedTime());

		Date d1 = new Date();
		appWithServer.setLastUploadedTime(d1);
		assertEquals("getLastUploadedTime not d1", d1, appWithServer.getLastUploadedTime());
		assertEquals("getLastUploadRemindedTime not null", null, appWithServer.getLastUploadRemindedTime());

		Date d2 = new Date(d1.getTime()+100);
		appWithServer.setLastUploadRemindedTime(d2);
		assertEquals("getLastUploadedTime not d1", d1, appWithServer.getLastUploadedTime());
		assertEquals("getLastUploadRemindedTime not d2", d2, appWithServer.getLastUploadRemindedTime());

		Date d3 = new Date(d2.getTime()+100);
		appWithServer.setLastUploadedTime(d3);
		assertEquals("getLastUploadedTime not d3", d3, appWithServer.getLastUploadedTime());
		assertEquals("getLastUploadRemindedTime not d2", d2, appWithServer.getLastUploadRemindedTime());

		file.delete();
		Date d4 = new Date(d3.getTime()+100);
		appWithServer.setLastUploadRemindedTime(d4);
		assertEquals("getLastUploadedTime not null", null, appWithServer.getLastUploadedTime());
		assertEquals("getLastUploadRemindedTime not d4", d4, appWithServer.getLastUploadRemindedTime());

		TRACE_END();
	}

	public void testResetUploadInfo()
	{
		TRACE_BEGIN("testResetUploadInfo");
		File file = appWithServer.getUploadInfoFile();

		file.delete();
		appWithServer.resetLastUploadedTime();
		long uploadedTime = appWithServer.getLastUploadedTime().getTime();
		long currentTime1 = System.currentTimeMillis();
		assertTrue("CurrentTime1 < uploadTime", currentTime1 >= uploadedTime);
		assertTrue("ResetLastUploadedTime", currentTime1 - uploadedTime <= 1000 );

		file.delete();
		appWithServer.resetLastUploadRemindedTime();
		long remindedTime = appWithServer.getLastUploadRemindedTime().getTime();
		long currentTime2 = System.currentTimeMillis();
		assertTrue("CurrentTime2 < remindedTime", currentTime2 >= remindedTime);
		assertTrue("ResetLastUploadRemindedTime", currentTime2 - remindedTime <= 1000 );
		TRACE_END();
	}

	public class MockServerInterfaceHandler extends ServerSideNetworkHandler
	{
		MockServerInterfaceHandler(ServerForClientsInterface serverToUse)
		{
			super(serverToUse);
		}
		
		public void nullGetFieldOfficeAccountIds(boolean shouldReturnNull)
		{
			nullGetFieldOfficeAccountIds = shouldReturnNull;
		}
		
		public Vector getFieldOfficeAccountIds(String myAccountId, Vector parameters, String signature)
		{
			if(nullGetFieldOfficeAccountIds)
				return null;
			return super.getFieldOfficeAccountIds(myAccountId, parameters, signature);
		}
		
		boolean nullGetFieldOfficeAccountIds;
	}

	Bulletin createAndUploadSampleBulletin() throws Exception
	{
		ClientBulletinStore store = appWithServer.getStore();
		mockServer.allowUploads(appWithServer.getAccountId());
		Bulletin b2 = appWithServer.createBulletin();
		b2.setSealed();
		store.saveBulletin(b2);
		assertEquals("upload b2", NetworkInterfaceConstants.OK, uploaderWithServer.uploadBulletin(b2));
		store.destroyBulletin(b2);
		return b2;
	}

	byte[] getBulletinZipBytes(Bulletin b) throws Exception
	{
		return StreamableBase64.decode(BulletinForTesting.saveToZipString(appWithServer.getStore().getDatabase(), b, mockSecurityForApp));
	}
		
	private static MockMartusSecurity mockSecurityForApp;
	private static MockMartusSecurity mockSecurityForServer;

	private static MockMartusApp appWithoutServer;
	private MockMartusApp appWithServer;

	private MockMartusServer mockServer;
	private NonSSLNetworkAPIWithHelpers mockNonSSLServerHandler;
	private MockServerInterfaceHandler mockSSLServerHandler;
	
	private BackgroundUploader uploaderWithServer;
	static final String sampleMagicWord = "beans!";
}

