/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003-2007, Beneficent
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

import java.io.IOException;
import java.util.Set;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.BackgroundUploader;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.tablemodels.RetrieveHQDraftsTableModel;
import org.martus.client.swingui.tablemodels.RetrieveHQTableModel;
import org.martus.client.swingui.tablemodels.RetrieveMyDraftsTableModel;
import org.martus.client.swingui.tablemodels.RetrieveMyTableModel;
import org.martus.client.swingui.tablemodels.RetrieveTableModel;
import org.martus.client.test.MockMartusApp;
import org.martus.client.test.NullProgressMeter;
import org.martus.clientside.test.MockUiLocalization;
import org.martus.common.BulletinSummary;
import org.martus.common.BulletinSummary.WrongValueCount;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.MiniLocalization;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.MockServerForClients;
import org.martus.server.forclients.ServerForClients;
import org.martus.server.forclients.ServerForClientsInterface;
import org.martus.server.forclients.ServerSideNetworkHandler;
import org.martus.server.forclients.SummaryCollector;
import org.martus.util.TestCaseEnhanced;

public class TestRetrieveTableModel extends TestCaseEnhanced
{

	public TestRetrieveTableModel(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		if(localization == null)
			localization = new MockUiLocalization(getName());
	 
		if(mockSecurityForApp == null)
			mockSecurityForApp = MockMartusSecurity.createClient();
		
		if(mockSecurityForServer == null)
			mockSecurityForServer = MockMartusSecurity.createServer();

		if(mockServer == null)
		{
			mockServer = new MockMartusServer();
			mockServer.serverForClients.loadBannedClients();
			mockServer.verifyAndLoadConfigurationFiles();
			mockServer.setSecurity(mockSecurityForServer);
			mockSSLServerHandler = new MockServerInterfaceHandler(mockServer.serverForClients);
		}
		
		if(appWithoutServer == null)
		{
			appWithoutServer = MockMartusApp.create(mockSecurityForApp, getName());
			mockServerNotAvailable = new MockServerNotAvailable();
			ServerSideNetworkHandler handler = new ServerSideNetworkHandler(mockServerNotAvailable.serverForClients);
			appWithoutServer.setSSLNetworkInterfaceHandlerForTesting(handler);
		}

		if(appWithServer == null)
		{
			appWithServer = MockMartusApp.create(mockSecurityForApp, getName());
			appWithServer.setServerInfo("mock", mockServer.getAccountId(), "");
			appWithServer.setSSLNetworkInterfaceHandlerForTesting(mockSSLServerHandler);
		}
		
		ProgressMeterInterface nullProgressMeter = new NullProgressMeter();
		uploader = new BackgroundUploader(appWithServer, nullProgressMeter);

		mockServer.deleteAllFiles();
	}

	public void tearDown() throws Exception
	{
		appWithoutServer.deleteAllFiles();
		appWithServer.deleteAllFiles();
		mockServer.deleteAllFiles();
		mockServerNotAvailable.deleteAllFiles();
		super.tearDown();
	}
	
	MockModel createMockModel(MiniLocalization localizationToUse) throws Exception
	{
		return new MockModel(MockMartusApp.create(getName()), localizationToUse);
	}
	
	class MockModel extends RetrieveTableModel
	{
		MockModel(MockMartusApp appToUse, MiniLocalization localizationToUse) throws Exception
		{
			super(appToUse, localizationToUse);
			app = appToUse;
			parent = createBulletin(getApp(), sampleSummary1, true, true);
			son = createClone(appToUse, parent, sampleSummary2);
			daughter = createClone(appToUse, parent, sampleSummary3);
			granddaughter = createClone(appToUse, daughter, sampleSummary4);

			allSummaries = new Vector();
			allSummaries.add(buildSummary(parent));
			allSummaries.add(buildSummary(son));
			allSummaries.add(buildSummary(daughter));
			allSummaries.add(buildSummary(granddaughter));
		}
		
		public void deleteAllFiles() throws Exception
		{
			app.deleteAllFiles();
		}
		
		protected void populateAllSummariesList() throws ServerErrorException
		{
		}
		
		private BulletinSummary buildSummary(Bulletin b) throws ServerErrorException
		{
			BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
			ReadableDatabase db = getApp().getStore().getDatabase();
			Vector tags = BulletinSummary.getNormalRetrieveTags();
			String summaryString = SummaryCollector.extractSummary(bhp, db, tags, mockServer.getLogger());
			try
			{
				return BulletinSummary.createFromString(bhp.getAccountId(), summaryString);
			}
			catch (WrongValueCount e)
			{
				throw new ServerErrorException();
			}
		}
		
		void deleteAllFromAppExcept(Bulletin keep) throws Exception
		{
			
			ClientBulletinStore store = getApp().getStore();
			if(!keep.getUniversalId().equals(parent.getUniversalId()))
				store.deleteBulletinRevision(DatabaseKey.createLegacyKey(parent.getUniversalId()));
			if(!keep.getUniversalId().equals(son.getUniversalId()))
				store.deleteBulletinRevision(DatabaseKey.createLegacyKey(son.getUniversalId()));
			if(!keep.getUniversalId().equals(daughter.getUniversalId()))
				store.deleteBulletinRevision(DatabaseKey.createLegacyKey(daughter.getUniversalId()));
			if(!keep.getUniversalId().equals(granddaughter.getUniversalId()))
				store.deleteBulletinRevision(DatabaseKey.createLegacyKey(granddaughter.getUniversalId()));
		}
		

		MockMartusApp app;
		Bulletin parent;
		Bulletin son;
		Bulletin daughter;
		Bulletin granddaughter;

	}
	
	public void testGetUidsThatWouldBeUpgrades() throws Exception
	{
		{
			MockModel model = createMockModel(localization);
			verifyDaughterWouldUpgrade(model, model.parent);
			model.deleteAllFiles();
		}
		
		{
			MockModel model = createMockModel(localization);
			verifyDaughterWouldNotUpgrade(model, model.son);
			model.deleteAllFiles();
		}
		
		{
			MockModel model = createMockModel(localization);
			verifyDaughterWouldNotUpgrade(model, model.granddaughter);
			model.deleteAllFiles();
		}
		
	}
	
	private void verifyDaughterWouldUpgrade(MockModel model, Bulletin bulletinToRetrieve) throws Exception
	{
		model.deleteAllFromAppExcept(bulletinToRetrieve);
		Vector uidToCheck = new Vector();
		uidToCheck.add(model.daughter.getUniversalId());
		Set result = model.getUidsThatWouldBeUpgrades(uidToCheck);
		assertEquals(1, result.size());
		assertTrue(result.contains(uidToCheck.get(0)));
	}

	private void verifyDaughterWouldNotUpgrade(MockModel model, Bulletin bulletinToRetrieve) throws Exception
	{
		model.deleteAllFromAppExcept(bulletinToRetrieve);
		Vector uidToCheck = new Vector();
		uidToCheck.add(model.daughter.getUniversalId());
		Set result = model.getUidsThatWouldBeUpgrades(uidToCheck);
		assertEquals(0, result.size());
	}

	public void testIsDownloadableNormal() throws Exception
	{
		MartusApp app = appWithServer;
		ClientBulletinStore store = app.getStore();
		ReadableDatabase db = store.getDatabase();

		Bulletin b1 = createBulletin(app, sampleSummary1, true, true);

		RetrieveMyTableModel model = new RetrieveMyTableModel(app, localization);
		model.initialize(null);
		assertFalse("plain bulletin exists downloadable?", model.isDownloadable(createSummary(b1, db)));
		store.destroyBulletin(b1);
		assertTrue("plain bulletin deleted not downloadable?", model.isDownloadable(createSummary(b1, db)));

	}
	
	public void testIsDownloadable() throws Exception
	{
		MockMartusApp app = appWithServer;
		ClientBulletinStore store = app.getStore();
		ReadableDatabase db = store.getDatabase();

		Bulletin original = createBulletin(app, sampleSummary2, true, true);
		Bulletin clone = createClone(app, original, sampleSummary3);
		
		RetrieveMyTableModel model = new RetrieveMyTableModel(app, localization);
		model.initialize(null);

		assertFalse("latest bulletin exists downloadable?", model.isDownloadable(createSummary(clone, db)));
		assertFalse("older bulletin exists downloadable?", model.isDownloadable(createSummary(original, db)));

		// clone is local but ancestor is not
		store.deleteBulletinRevision(original.getDatabaseKeyForLocalId(original.getLocalId()));
		assertFalse("latest bulletin exists still downloadable?", model.isDownloadable(createSummary(clone, db)));
		assertFalse("older bulletin downloadable over clone?", model.isDownloadable(createSummary(original, db)));

		// neither clone nor ancestor is local
		store.deleteBulletinRevision(clone.getDatabaseKeyForLocalId(clone.getLocalId()));
		assertTrue("latest bulletin not downloadable?", model.isDownloadable(createSummary(clone, db)));
		assertTrue("older bulletin not downloadable?", model.isDownloadable(createSummary(original, db)));
		
		// ancestor is local but clone is not
		store.saveBulletin(original);
		assertTrue("latest bulletin not downloadable?", model.isDownloadable(createSummary(clone, db)));
		assertFalse("older bulletin exists still downloadable?", model.isDownloadable(createSummary(original, db)));

	}
	
	private BulletinSummary createSummary(Bulletin b, ReadableDatabase db) throws WrongValueCount
	{
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
		Vector tags = BulletinSummary.getNormalRetrieveTags();
		String b1SummaryString = SummaryCollector.extractSummary(bhp, db, tags, mockServer.getLogger());
		BulletinSummary b1Summary = BulletinSummary.createFromString(b.getAccount(), b1SummaryString);
		return b1Summary;
	}

	public void testRetrieveMyDraftsMarksAllAsOnServer() throws Exception
	{
		MartusApp app = appWithServer;
		ClientBulletinStore store = app.getStore();
		
		Bulletin b1 = createAndUploadPrivateSealed(app, sampleSummary1);
		Bulletin b2 = createAndUploadPublicSealed(app, sampleSummary2);
		Bulletin b3 = createAndUploadPrivateDraft(app, sampleSummary3);
		Bulletin b4 = createAndUploadPublicDraft(app, sampleSummary4);

		RetrieveMyDraftsTableModel model = new RetrieveMyDraftsTableModel(app, localization);
		model.initialize(null);

		assertFalse("b1 now on?", store.isProbablyOnServer(b1.getUniversalId()));
		assertFalse("b2 now on?", store.isProbablyOnServer(b2.getUniversalId()));
		assertTrue("b3 not on?", store.isProbablyOnServer(b3.getUniversalId()));
		assertTrue("b4 not on?", store.isProbablyOnServer(b4.getUniversalId()));
		
		store.setIsNotOnServer(b3);
		store.setIsNotOnServer(b4);
		store.destroyBulletin(b3);
		store.destroyBulletin(b4);
		model.initialize(null);
		assertFalse("b3 on even though it isn't in the store?", store.isProbablyOnServer(b3.getUniversalId()));
		assertFalse("b4 on even though it isn't in the store?", store.isProbablyOnServer(b4.getUniversalId()));
	}
	
	public void testRetrieveMySealedBulletinsMarksAllAsOnServer() throws Exception
	{
		MartusApp app = appWithServer;
		ClientBulletinStore store = app.getStore();
		
		Bulletin b1 = createAndUploadPrivateSealed(app, sampleSummary1);
		Bulletin b2 = createAndUploadPublicSealed(app, sampleSummary2);
		Bulletin b3 = createAndUploadPrivateDraft(app, sampleSummary3);
		Bulletin b4 = createAndUploadPublicDraft(app, sampleSummary4);

		RetrieveMyTableModel model = new RetrieveMyTableModel(app, localization);
		model.initialize(null);

		assertTrue("b1 not on?", store.isProbablyOnServer(b1.getUniversalId()));
		assertTrue("b2 not on?", store.isProbablyOnServer(b2.getUniversalId()));
		assertFalse("b3 now on?", store.isProbablyOnServer(b3.getUniversalId()));
		assertFalse("b4 now on?", store.isProbablyOnServer(b4.getUniversalId()));
		
		store.setIsNotOnServer(b1);
		store.setIsNotOnServer(b2);
		store.destroyBulletin(b1);
		store.destroyBulletin(b2);
		model.initialize(null);
		assertFalse("b1 on even though it isn't in the store?", store.isProbablyOnServer(b1.getUniversalId()));
		assertFalse("b2 on even though it isn't in the store?", store.isProbablyOnServer(b2.getUniversalId()));
	}
	
	public void testRetrieveFieldOfficeSealedBulletinsMarksAllAsOnServer() throws Exception
	{
		MockMartusApp hqApp = MockMartusApp.create(MockMartusSecurity.createHQ(), getName());
		hqApp.setSSLNetworkInterfaceHandlerForTesting(mockSSLServerHandler);
		HeadquartersKeys hqKeys = new HeadquartersKeys();
		hqKeys.add(new HeadquartersKey(hqApp.getAccountId()));

		MartusApp fieldOfficeApp = appWithServer;
		fieldOfficeApp.setAndSaveHQKeys(hqKeys, hqKeys);
		
		Bulletin b1 = createAndUploadPrivateSealed(fieldOfficeApp, sampleSummary1);
		Bulletin b2 = createAndUploadPublicSealed(fieldOfficeApp, sampleSummary2);
		Bulletin b3 = createAndUploadPrivateDraft(fieldOfficeApp, sampleSummary3);
		Bulletin b4 = createAndUploadPublicDraft(fieldOfficeApp, sampleSummary4);
		
		ClientBulletinStore hqStore = hqApp.getStore();
		hqStore.saveBulletinForTesting(b1);
		hqStore.saveBulletinForTesting(b2);
		hqStore.saveBulletinForTesting(b3);
		hqStore.saveBulletinForTesting(b4);

		RetrieveHQTableModel model = new RetrieveHQTableModel(hqApp, localization);
		model.initialize(null);

		assertTrue("b1 not on?", hqStore.isProbablyOnServer(b1.getUniversalId()));
		assertTrue("b2 not on?", hqStore.isProbablyOnServer(b2.getUniversalId()));
		assertFalse("b3 now on?", hqStore.isProbablyOnServer(b3.getUniversalId()));
		assertFalse("b4 now on?", hqStore.isProbablyOnServer(b4.getUniversalId()));
		
		hqStore.setIsNotOnServer(b1);
		hqStore.setIsNotOnServer(b2);
		hqStore.destroyBulletin(b1);
		hqStore.destroyBulletin(b2);
		model.initialize(null);
		assertFalse("b1 on even though it isn't in the store?", hqStore.isProbablyOnServer(b1.getUniversalId()));
		assertFalse("b2 on even though it isn't in the store?", hqStore.isProbablyOnServer(b2.getUniversalId()));
		hqApp.deleteAllFiles();
	}
	
	public void testRetrieveFieldOfficeDraftBulletinsMarksAllAsOnServer() throws Exception
	{
		MockMartusApp hqApp = MockMartusApp.create(MockMartusSecurity.createHQ(), getName());
		hqApp.setSSLNetworkInterfaceHandlerForTesting(mockSSLServerHandler);
		HeadquartersKeys hqKeys = new HeadquartersKeys();
		hqKeys.add(new HeadquartersKey(hqApp.getAccountId()));

		MartusApp fieldOfficeApp = appWithServer;
		fieldOfficeApp.setAndSaveHQKeys(hqKeys, hqKeys);
		
		Bulletin b1 = createAndUploadPrivateSealed(fieldOfficeApp, sampleSummary1);
		Bulletin b2 = createAndUploadPublicSealed(fieldOfficeApp, sampleSummary2);
		Bulletin b3 = createAndUploadPrivateDraft(fieldOfficeApp, sampleSummary3);
		Bulletin b4 = createAndUploadPublicDraft(fieldOfficeApp, sampleSummary4);
		
		ClientBulletinStore hqStore = hqApp.getStore();
		hqStore.saveBulletinForTesting(b1);
		hqStore.saveBulletinForTesting(b2);
		hqStore.saveBulletinForTesting(b3);
		hqStore.saveBulletinForTesting(b4);

		RetrieveHQDraftsTableModel model = new RetrieveHQDraftsTableModel(hqApp, localization);
		model.initialize(null);

		assertFalse("b1 now on?", hqStore.isProbablyOnServer(b1.getUniversalId()));
		assertFalse("b2 now on?", hqStore.isProbablyOnServer(b2.getUniversalId()));
		assertTrue("b3 not on?", hqStore.isProbablyOnServer(b3.getUniversalId()));
		assertTrue("b4 not on?", hqStore.isProbablyOnServer(b4.getUniversalId()));
		
		hqStore.setIsNotOnServer(b3);
		hqStore.setIsNotOnServer(b4);
		hqStore.destroyBulletin(b3);
		hqStore.destroyBulletin(b4);
		model.initialize(null);
		assertFalse("b3 on even though it isn't in the store?", hqStore.isProbablyOnServer(b3.getUniversalId()));
		assertFalse("b4 on even though it isn't in the store?", hqStore.isProbablyOnServer(b4.getUniversalId()));
		hqApp.deleteAllFiles();
	}
	
	public void testGetMyBulletinSummariesWithServerError() throws Exception
	{
		createAndUploadAndDeletePrivateSealed(appWithServer, sampleSummary1);
		createAndUploadAndDeletePublicSealed(appWithServer, sampleSummary2);
		createAndUploadAndDeletePrivateSealed(appWithServer, sampleSummary3);

		mockServer.countDownToGetPacketFailure = 2;

		RetrieveMyTableModel model = new RetrieveMyTableModel(appWithServer, localization);
		try
		{
			model.initialize(null);
			model.checkIfErrorOccurred();
			fail("Didn't throw");
		}
		catch (ServerErrorException expectedExceptionToIgnore)
		{
		}
		Vector result = model.getDownloadableSummaries();
		assertEquals("wrong count?", 2, result.size());

		BulletinSummary s1 = (BulletinSummary)result.get(0);
		BulletinSummary s2 = (BulletinSummary)result.get(1);
		assertNotEquals(s1.getLocalId(), s2.getLocalId());
	}

	public void testGetMyBulletinSummariesNoServer() throws Exception
	{
		try
		{
			RetrieveMyTableModel model = new RetrieveMyTableModel(appWithoutServer, localization);
			model.initialize(null);
			model.getMySealedSummaries();
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("Got valid summaries?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}

		try
		{
			RetrieveMyDraftsTableModel model = new RetrieveMyDraftsTableModel(appWithoutServer, localization);
			model.initialize(null);
			model.getMyDraftSummaries();
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("Got valid draft summaries?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}
	}
	
	public void testGetMyBulletinSummariesErrors() throws Exception
	{
		assertTrue("must be able to ping", appWithServer.isSSLServerAvailable());

		Vector desiredResult = new Vector();

		desiredResult.add(NetworkInterfaceConstants.REJECTED);
		((MockServerForClients)mockServer.serverForClients).listMyResponse = desiredResult;
		try
		{
			RetrieveMyTableModel model = new RetrieveMyTableModel(appWithServer, localization);
			model.initialize(null);
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("rejected didn't throw?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}
		((MockServerForClients)mockServer.serverForClients).listMyResponse = null;
	}

	public void testGetMySummaries() throws Exception
	{
		Bulletin b1 = createAndUploadAndDeletePrivateSealed(appWithServer, sampleSummary1);
		Bulletin b2 = createAndUploadAndDeletePublicSealed(appWithServer, sampleSummary2);
		createAndUploadAndDeletePrivateDraft(appWithServer, sampleSummary3);
		
		RetrieveMyTableModel model = new RetrieveMyTableModel(appWithServer, localization);
		model.initialize(null);
		model.checkIfErrorOccurred();
		Vector result = model.getDownloadableSummaries();
		assertEquals("wrong count?", 2, result.size());
		
		BulletinSummary s1 = (BulletinSummary)result.get(0);
		BulletinSummary s2 = (BulletinSummary)result.get(1);
		
		Bulletin bulletins[] = new Bulletin[] {b1, b2};
		BulletinSummary summaries[] = new BulletinSummary[] {s1, s2};
		boolean found[] = new boolean[bulletins.length];
		
		for(int i = 0; i < bulletins.length; ++i)
		{
			for(int j = 0; j < summaries.length; ++j)
			{
				Bulletin b = bulletins[i];
				BulletinSummary s = summaries[j];
				if(b.getLocalId().equals(s.getLocalId()))
				{
					assertEquals(b.get(Bulletin.TAGTITLE), s.getStorableTitle());
					found[i] = true;
				}
			}
		}
		
		assertTrue("Missing 1?", found[0]);
		assertTrue("Missing 2?", found[1]);
	}
	
	public void testOnServerAfterRetrieveSummaries() throws Exception
	{
		
	}

	public void testGetMySummariesDataRetrieved() throws Exception
	{
		Bulletin b1 = createAndUploadPrivateSealed(appWithServer, sampleSummary1);
		long b1LastDateSaved = b1.getBulletinHeaderPacket().getLastSavedTime();

		ClientBulletinStore store = appWithServer.getStore();
		int b1Size = MartusUtilities.getBulletinSize(store.getDatabase(),b1.getBulletinHeaderPacket());
		store.destroyBulletin(b1);

		RetrieveMyTableModel model = new RetrieveMyTableModel(appWithServer, localization);
		model.initialize(null);
		model.checkIfErrorOccurred();
		Vector result = model.getDownloadableSummaries();
		assertEquals("wrong count?", 1, result.size());
		
		BulletinSummary s1 = (BulletinSummary)result.get(0);
		
		assertEquals(b1.getAccount(), s1.getAccountId());
		assertEquals(b1.getLocalId(), s1.getLocalId());
		assertEquals(sampleSummary1, s1.getStorableTitle());
		assertEquals(b1Size, s1.getSize());
		assertEquals(BulletinSummary.getLastDateTimeSaved(new Long(b1LastDateSaved).toString()), s1.getDateTimeSaved());
	}
	
	public void testGetAllMySummaries() throws Exception
	{
		appWithServer.getStore().deleteAllData();
		
		Bulletin b1 = createAndUploadAndDeletePrivateSealed(appWithServer, sampleSummary1);
		Bulletin b2 = createAndUploadAndDeletePublicSealed(appWithServer, sampleSummary2);
		Bulletin b3 = createAndUploadPrivateSealed(appWithServer, sampleSummary3);
		
		RetrieveMyTableModel model = new RetrieveMyTableModel(appWithServer, localization);
		model.initialize(null);
		model.checkIfErrorOccurred();
		Vector allResult = model.getAllSummaries();
		assertEquals("wrong all summaries count?", 3, allResult.size());

		BulletinSummary allS1 = (BulletinSummary)allResult.get(0);
		BulletinSummary allS2 = (BulletinSummary)allResult.get(1);
		BulletinSummary allS3 = (BulletinSummary)allResult.get(2);
		Bulletin allBulletins[] = new Bulletin[] {b1, b2, b3};
		BulletinSummary allSummaries[] = new BulletinSummary[] {allS1, allS2, allS3};
		
		for(int i = 0; i < allBulletins.length; ++i)
		{
			for(int j = 0; j < allSummaries.length; ++j)
			{
				Bulletin b = allBulletins[i];
				BulletinSummary s = allSummaries[j];
				if(b.getLocalId().equals(s.getLocalId()))
				{
					if(b.equals(b1))
						assertTrue("B1 not downloadable?", s.isDownloadable());
					if(b.equals(b2))
						assertTrue("B2 not downloadable?", s.isDownloadable());
					if(b.equals(b3))
						assertFalse("B3 downloadable?", s.isDownloadable());
				}
			}
		}
		model.checkIfErrorOccurred();
		Vector result = model.getDownloadableSummaries();
		assertEquals("wrong count?", 2, result.size());
		
		BulletinSummary s1 = (BulletinSummary)result.get(0);
		BulletinSummary s2 = (BulletinSummary)result.get(1);

		Bulletin bulletins[] = new Bulletin[] {b1, b2};
		BulletinSummary summaries[] = new BulletinSummary[] {s1, s2};
		boolean found[] = new boolean[bulletins.length];
		
		for(int i = 0; i < bulletins.length; ++i)
		{
			for(int j = 0; j < summaries.length; ++j)
			{
				Bulletin b = bulletins[i];
				BulletinSummary s = summaries[j];
				if(b.getLocalId().equals(s.getLocalId()))
				{
					assertEquals(b.get(Bulletin.TAGTITLE), s.getStorableTitle());
					found[i] = true;
				}
				assertTrue("Not downloadable?", s.isDownloadable());
			}
		}
		assertTrue("Missing 1?", found[0]);
		assertTrue("Missing 2?", found[1]);
	}


	public void testGetMyDraftBulletinSummariesErrors() throws Exception
	{
		assertTrue("must be able to ping", appWithServer.isSSLServerAvailable());

		Vector desiredResult = new Vector();

		desiredResult.add(NetworkInterfaceConstants.REJECTED);
		((MockServerForClients)mockServer.serverForClients).listMyResponse =  desiredResult;
		try
		{
			RetrieveMyDraftsTableModel model = new RetrieveMyDraftsTableModel(appWithServer, localization);
			model.initialize(null);
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("rejected didn't throw?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}
		((MockServerForClients)mockServer.serverForClients).listMyResponse =  null;
	}

	public void testGetMyDraftSummaries() throws Exception
	{
		Bulletin b1 = createAndUploadAndDeletePrivateDraft(appWithServer, sampleSummary1);
		Bulletin b2 = createAndUploadAndDeletePublicDraft(appWithServer, sampleSummary2);
		createAndUploadAndDeletePrivateSealed(appWithServer, sampleSummary3);
		
		RetrieveMyDraftsTableModel model = new RetrieveMyDraftsTableModel(appWithServer, localization);
		model.initialize(null);
		model.checkIfErrorOccurred();
		Vector result = model.getDownloadableSummaries();
		assertEquals("wrong count?", 2, result.size());
		
		BulletinSummary s1 = (BulletinSummary)result.get(0);
		BulletinSummary s2 = (BulletinSummary)result.get(1);

		Bulletin bulletins[] = new Bulletin[] {b1, b2};
		BulletinSummary summaries[] = new BulletinSummary[] {s1, s2};
		boolean found[] = new boolean[bulletins.length];
		
		for(int i = 0; i < bulletins.length; ++i)
		{
			for(int j = 0; j < summaries.length; ++j)
			{
				Bulletin b = bulletins[i];
				BulletinSummary s = summaries[j];
				if(b.getLocalId().equals(s.getLocalId()))
				{
					assertEquals(b.get(Bulletin.TAGTITLE), s.getStorableTitle());
					found[i] = true;
				}
			}
		}
		
		assertTrue("Missing 1?", found[0]);
		assertTrue("Missing 2?", found[1]);
	}
	
	public void testGetFieldOfficeBulletinSummariesNoServer() throws Exception
	{
		try
		{
			RetrieveHQTableModel model = new RetrieveHQTableModel(appWithoutServer, localization);
			model.initialize(null);
			model.getFieldOfficeSealedSummaries("");
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("Got valid sealed summaries?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}
		
		try
		{
			RetrieveHQDraftsTableModel model = new RetrieveHQDraftsTableModel(appWithoutServer, localization);
			model.initialize(null);
			model.getFieldOfficeDraftSummaries("");
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("Got valid draft summaries?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}
	}

	public void testGetFieldOfficeBulletinSummariesErrors() throws Exception
	{
		assertTrue("must be able to ping", appWithServer.isSSLServerAvailable());

		Vector desiredResult = new Vector();

		desiredResult.add(NetworkInterfaceConstants.REJECTED);
		((MockServerForClients)mockServer.serverForClients).listFieldOfficeSummariesResponse = desiredResult;
		try
		{
			RetrieveHQTableModel model = new RetrieveHQTableModel(appWithServer, localization);
			model.initialize(null);
			model.getFieldOfficeSealedSummaries("");
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("rejected sealed didn't throw?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}

		try
		{
			RetrieveHQDraftsTableModel model = new RetrieveHQDraftsTableModel(appWithServer, localization);
			model.initialize(null);
			model.getFieldOfficeDraftSummaries("");
			model.checkIfErrorOccurred();
			model.getDownloadableSummaries();
			fail("rejected draft didn't throw?");
		}
		catch(MartusUtilities.ServerErrorException ignoreExpectedException)
		{
		}

		((MockServerForClients)mockServer.serverForClients).listFieldOfficeSummariesResponse = null;
	}

	public void testGetFieldOfficeSummaries() throws Exception
	{
		MockMartusSecurity hqSecurity = MockMartusSecurity.createHQ();	
		MockMartusApp hqApp = MockMartusApp.create(hqSecurity, getName());
		hqApp.setServerInfo("mock", mockServer.getAccountId(), "");
		hqApp.setSSLNetworkInterfaceHandlerForTesting(mockSSLServerHandler);
		assertNotEquals("same public key?", appWithServer.getAccountId(), hqApp.getAccountId());
		
		MockMartusSecurity hq2Security = MockMartusSecurity.createHQ();	
		MockMartusApp hq2App = MockMartusApp.create(hq2Security, getName());
		hq2App.setServerInfo("mock", mockServer.getAccountId(), "");
		hq2App.setSSLNetworkInterfaceHandlerForTesting(mockSSLServerHandler);

		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(hqApp.getAccountId());
		keys.add(key1);
		HeadquartersKey key2 = new HeadquartersKey(hq2App.getAccountId());
		keys.add(key2);
		appWithServer.setAndSaveHQKeys(keys, keys);
		
		Bulletin b1 = createAndUploadPrivateSealed(appWithServer, sampleSummary1);
		Bulletin b2 = createAndUploadPublicSealed(appWithServer, sampleSummary2);
		Bulletin b3 = createAndUploadPublicDraft(appWithServer, sampleSummary3);

		{
			Vector desiredSealedResult = new Vector();
			desiredSealedResult.add(NetworkInterfaceConstants.OK);
			Vector list = new Vector();
			list.add(b1.getLocalId() + "=" + b1.getFieldDataPacket().getLocalId()+"=2000");
			list.add(b2.getLocalId() + "=" + b2.getFieldDataPacket().getLocalId()+"=2000");
			desiredSealedResult.add(list.toArray());
			((MockServerForClients)mockServer.serverForClients).listFieldOfficeSummariesResponse = desiredSealedResult;	
	
			RetrieveHQTableModel model = new RetrieveHQTableModel(hqApp, localization);
			model.initialize(null);
			model.checkIfErrorOccurred();
			Vector returnedSealedResults = model.getDownloadableSummaries();
			assertEquals("Wrong size?", 2, returnedSealedResults.size());
			BulletinSummary s1 = (BulletinSummary)returnedSealedResults.get(0);
			BulletinSummary s2 = (BulletinSummary)returnedSealedResults.get(1);
			boolean found1 = false;
			boolean found2 = false;
			found1 = s1.getLocalId().equals(b1.getLocalId());
			if(!found1)
				found1 = s2.getLocalId().equals(b1.getLocalId());
			found2 = s1.getLocalId().equals(b2.getLocalId());
			if(!found2)
				found2 = s2.getLocalId().equals(b2.getLocalId());
			assertTrue("not found S1?", found1);
			assertTrue("not found S2?", found2);
		}

		{
			Vector desiredDraftResult = new Vector();
			desiredDraftResult.add(NetworkInterfaceConstants.OK);
			Vector list2 = new Vector();
			list2.add(b3.getLocalId() + "=" + b3.getFieldDataPacket().getLocalId()+"=3400");
			desiredDraftResult.add(list2.toArray());
			((MockServerForClients)mockServer.serverForClients).listFieldOfficeSummariesResponse = desiredDraftResult;	
	
			RetrieveHQDraftsTableModel model2 = new RetrieveHQDraftsTableModel(hqApp, localization);
			model2.initialize(null);
			model2.checkIfErrorOccurred();
			Vector returnedDraftResults = model2.getDownloadableSummaries();
			assertEquals("Wrong draft size?", 1, returnedDraftResults.size());
			BulletinSummary s3 = (BulletinSummary)returnedDraftResults.get(0);
			boolean found3 = false;
			found3 = s3.getLocalId().equals(b3.getLocalId());
			assertTrue("not found S3?", found3);
			((MockServerForClients)mockServer.serverForClients).listFieldOfficeSummariesResponse = null;

			hqApp.deleteAllFiles();

			RetrieveHQTableModel modelhq2 = new RetrieveHQTableModel(hq2App, localization);
			modelhq2.initialize(null);
			modelhq2.checkIfErrorOccurred();
			Vector returnedSealedResults2 = modelhq2.getDownloadableSummaries();
			assertEquals("Wrong size for HQ2?", 2, returnedSealedResults2.size());
			BulletinSummary bs1 = (BulletinSummary)returnedSealedResults2.get(0);
			BulletinSummary bs2 = (BulletinSummary)returnedSealedResults2.get(1);
			boolean foundItem1 = false;
			boolean foundItem2 = false;
			foundItem1 = bs1.getLocalId().equals(b1.getLocalId());
			if(!foundItem1)
				foundItem1 = bs2.getLocalId().equals(b1.getLocalId());
			foundItem2 = bs1.getLocalId().equals(b2.getLocalId());
			if(!foundItem2)
				foundItem2 = bs2.getLocalId().equals(b2.getLocalId());
			assertTrue("not found S1?", foundItem1);
			assertTrue("not found S2?", foundItem2);
		}

		{
			Vector desired2DraftResult = new Vector();
			desired2DraftResult.add(NetworkInterfaceConstants.OK);
			Vector listOfData = new Vector();
			listOfData.add(b3.getLocalId() + "=" + b3.getFieldDataPacket().getLocalId()+"=3400");
			desired2DraftResult.add(listOfData.toArray());
			((MockServerForClients)mockServer.serverForClients).listFieldOfficeSummariesResponse = desired2DraftResult;	
	
			RetrieveHQDraftsTableModel draftHQ2Model = new RetrieveHQDraftsTableModel(hqApp, localization);
			draftHQ2Model.initialize(null);
			draftHQ2Model.checkIfErrorOccurred();
			Vector returnedHQ2DraftResults = draftHQ2Model.getDownloadableSummaries();
			assertEquals("Wrong draft size for HQ2?", 1, returnedHQ2DraftResults.size());
			BulletinSummary bsDraft3 = (BulletinSummary)returnedHQ2DraftResults.get(0);
			boolean foundItems2 = false;
			foundItems2 = bsDraft3.getLocalId().equals(b3.getLocalId());
			assertTrue("not found S3 for HQ2?", foundItems2);
			((MockServerForClients)mockServer.serverForClients).listFieldOfficeSummariesResponse = null;
		}
		
		hqApp.deleteAllFiles();
		hq2App.deleteAllFiles();
	}
	
	private Bulletin createAndUploadAndDeletePrivateSealed(MartusApp app, String title) throws Exception
	{
		return createAndUploadAndDeleteBulletin(app, title, true, true);
	}
	
	private Bulletin createAndUploadAndDeletePrivateDraft(MartusApp app, String title) throws Exception
	{
		return createAndUploadAndDeleteBulletin(app, title, true, false);
	}
	
	private Bulletin createAndUploadAndDeletePublicSealed(MartusApp app, String title) throws Exception
	{
		return createAndUploadAndDeleteBulletin(app, title, false, true);
	}
	
	private Bulletin createAndUploadAndDeletePublicDraft(MartusApp app, String title) throws Exception
	{
		return createAndUploadAndDeleteBulletin(app, title, false, false);
	}
	
	private Bulletin createAndUploadAndDeleteBulletin(MartusApp app, String title, boolean allPrivate, boolean sealed) throws Exception
	{
		Bulletin b = createAndUploadBulletin(app, title, allPrivate, sealed);
		deleteBulletin(app, b);
		return b;
	}
	
	

	private Bulletin createAndUploadPrivateSealed(MartusApp app, String title) throws Exception
	{
		return createAndUploadBulletin(app, title, true, true);
	}
	
	private Bulletin createAndUploadPublicSealed(MartusApp app, String title) throws Exception
	{
		return createAndUploadBulletin(app, title, false, true);
	}
	
	private Bulletin createAndUploadPrivateDraft(MartusApp app, String title) throws Exception
	{
		return createAndUploadBulletin(app, title, true, false);
	}
	
	private Bulletin createAndUploadPublicDraft(MartusApp app, String title) throws Exception
	{
		return createAndUploadBulletin(app, title, false, false);
	}
	
	private Bulletin createAndUploadBulletin(MartusApp app, String title, boolean allPrivate, boolean sealed) throws Exception
	{
		Bulletin b = createBulletin(app, title, allPrivate, sealed);
		uploadBulletin(app, b);
		ClientBulletinStore store = app.getStore();
		assertFalse("new bulletin is marked as being on the server?", store.isProbablyOnServer(b.getUniversalId()));
		assertFalse("new bulletin is marked as being not on the server?", store.isProbablyNotOnServer(b.getUniversalId()));
		return b;
	}
	
	

	Bulletin createBulletin(MartusApp app, String title, boolean allPrivate, boolean sealed) throws Exception
	{
		Bulletin b = app.createBulletin();
		b.setAllPrivate(allPrivate);
		b.set(Bulletin.TAGTITLE, title);
		if(sealed)
			b.setSealed();
		app.setDefaultHQKeysInBulletin(b);
		app.getStore().saveBulletin(b);
		return b;
	}

	Bulletin createClone(MockMartusApp app, Bulletin original, String summary) throws Exception
	{
		Bulletin clone = app.createBulletin();
		clone.createDraftCopyOf(original, app.getWriteableDatabase());
		clone.set(Bulletin.TAGTITLE, summary);
		clone.setSealed();
		ClientBulletinStore store = app.getStore();
		store.saveBulletin(clone);
		return clone;
	}

	private void uploadBulletin(MartusApp app, Bulletin b) throws Exception
	{
		mockServer.allowUploads(app.getAccountId());
		assertEquals("failed upload1?", NetworkInterfaceConstants.OK, uploader.uploadBulletin(b));
	}

	private void deleteBulletin(MartusApp app, Bulletin b) throws IOException
	{
		ClientBulletinStore store = app.getStore();
		store.destroyBulletin(b);
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

	public static class MockServerNotAvailable extends MockMartusServer
	{
		MockServerNotAvailable() throws Exception
		{
			super();
		}

		public ServerForClients createServerForClients()
		{
			return new ServerForClientsThatReturnsNullForPing(this);
		}
		
		static class ServerForClientsThatReturnsNullForPing extends MockServerForClients
		{
			public ServerForClientsThatReturnsNullForPing(MockMartusServer coreServer)
			{
				super(coreServer);
			}
			
			public String ping()
			{
				return null;
			}
		}
	}


	Bulletin testB0;
	Bulletin testB1;
	Bulletin testB2;

	String sampleSummary1 = "this is a basic summary";
	String sampleSummary2 = "another silly summary";
	String sampleSummary3 = "yet another!";
	String sampleSummary4 = "this is the fourth!";

	String title1 = "This is a cool title";
	String title2 = "Even cooler";

	static MiniLocalization localization;
	private static MockMartusSecurity mockSecurityForApp;
	private static MockMartusSecurity mockSecurityForServer;

	private MockMartusApp appWithServer;
	private MockMartusApp appWithoutServer;
	private MockServerNotAvailable mockServerNotAvailable;

	MockMartusServer mockServer;
	private MockServerInterfaceHandler mockSSLServerHandler;
	
	BackgroundUploader uploader;
}
