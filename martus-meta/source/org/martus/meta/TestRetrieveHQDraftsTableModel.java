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

import java.io.File;
import java.io.StringWriter;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.tablemodels.RetrieveHQDraftsTableModel;
import org.martus.client.test.MockMartusApp;
import org.martus.clientside.test.MockUiLocalization;
import org.martus.common.BulletinSummary;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusUtilities;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.ServerSideNetworkInterface;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.ServerForClients;
import org.martus.server.forclients.ServerSideNetworkHandler;
import org.martus.util.TestCaseEnhanced;

public class TestRetrieveHQDraftsTableModel extends TestCaseEnhanced
{
	public TestRetrieveHQDraftsTableModel(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		if(localization!=null)
			return;
		MartusCrypto hqSecurity = MockMartusSecurity.createHQ();
		localization = new MockUiLocalization(getName());
		hqApp = MockMartusApp.create(hqSecurity, getName());

		MartusCrypto fieldSecurity1 = MockMartusSecurity.createClient();
		fieldApp1 = MockMartusApp.create(fieldSecurity1, getName());
		final ClientBulletinStore store1 = fieldApp1.getStore();
		ReadableDatabase db1 = store1.getDatabase();

		MartusCrypto fieldSecurity2 = MockMartusSecurity.createOtherClient();
		fieldApp2 = MockMartusApp.create(fieldSecurity2, getName());
		final ClientBulletinStore store2 = fieldApp2.getStore();
		ReadableDatabase db2 = store2.getDatabase();

		assertNotEquals("account Id's equal?", fieldApp1.getAccountId(), fieldApp2.getAccountId());

		normalBulletin = fieldApp1.createBulletin();
		normalBulletin.set(Bulletin.TAGTITLE, title0);
		normalBulletin.set(Bulletin.TAGAUTHOR, author0);
		normalBulletin.setAllPrivate(true);
		HeadquartersKeys hqKey = new HeadquartersKeys();
		HeadquartersKey key = new HeadquartersKey(hqApp.getAccountId());
		hqKey.add(key);
		normalBulletin.setAuthorizedToReadKeys(hqKey);
		store1.saveBulletin(normalBulletin);
		normalBulletinSize = MartusUtilities.getBulletinSize(store1.getDatabase(), normalBulletin.getBulletinHeaderPacket());

		bulletinVersion1 = fieldApp1.createBulletin();
		bulletinVersion1.set(Bulletin.TAGTITLE, title1);
		bulletinVersion1.set(Bulletin.TAGAUTHOR, author1);
		bulletinVersion1.setAllPrivate(false);
		bulletinVersion1.setAuthorizedToReadKeys(hqKey);
		bulletinVersion1.setSealed();
		store1.saveBulletin(bulletinVersion1);

		bulletinVersion2 = fieldApp2.createBulletin();
		bulletinVersion2.set(Bulletin.TAGTITLE, title2);
		bulletinVersion2.set(Bulletin.TAGAUTHOR, author2);
		bulletinVersion2.setAllPrivate(true);
		bulletinVersion2.setAuthorizedToReadKeys(hqKey);
		BulletinHistory history2 = new BulletinHistory();
		history2.add(bulletinVersion1.getLocalId());
		historyId = UniversalId.createFromAccountAndLocalId(bulletinVersion2.getAccount(), bulletinVersion1.getLocalId());
		bulletinVersion2.setHistory(history2);
		bulletinVersion2.setDraft();
		store2.saveBulletin(bulletinVersion2);
		bulletinVersion2Size = MartusUtilities.getBulletinSize(store2.getDatabase(), bulletinVersion2.getBulletinHeaderPacket());

		testServer = new MockServer();
		testServer.verifyAndLoadConfigurationFiles();
		testSSLServerInterface = new ServerSideNetworkHandler(testServer.serverForClients);
		hqApp.setSSLNetworkInterfaceHandlerForTesting(testSSLServerInterface);
		modelWithData = new RetrieveHQDraftsTableModel(hqApp,localization);
		modelWithData.initialize(null);
		
		importBulletinFromFieldOfficeToHq(db1, normalBulletin, fieldSecurity1);
		importBulletinFromFieldOfficeToHq(db1, bulletinVersion1, fieldSecurity1);
		importBulletinFromFieldOfficeToHq(db2, bulletinVersion2, fieldSecurity2);
		
		modelWithoutData = new RetrieveHQDraftsTableModel(hqApp, localization);
		modelWithoutData.initialize(null);

		store2.saveBulletin(bulletinVersion2);
		modelWithOlderDraft = new RetrieveHQDraftsTableModel(hqApp, localization);
		modelWithOlderDraft.initialize(null);
	}
	
	void importBulletinFromFieldOfficeToHq(ReadableDatabase db, Bulletin b, MartusCrypto sigVerifier) throws Exception
	{
		File tempFile = createTempFile();
		DatabaseKey headerKey = DatabaseKey.createKey(b.getUniversalId(), b.getStatus());
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, headerKey, tempFile, sigVerifier);
		hqApp.getStore().importZipFileToStoreWithSameUids(tempFile);
	}
	
	public void tearDown() throws Exception
	{
		testServer.deleteAllFiles();
    	fieldApp1.deleteAllFiles();
    	fieldApp2.deleteAllFiles();
    	hqApp.deleteAllFiles();
    	super.tearDown();
    }

	public void testGetColumnName()
	{
		assertEquals(localization.getFieldLabel("retrieveflag"), modelWithData.getColumnName(0));
		assertEquals(localization.getFieldLabel(Bulletin.TAGTITLE), modelWithData.getColumnName(1));
		assertEquals(localization.getFieldLabel(Bulletin.TAGAUTHOR), modelWithData.getColumnName(2));
		assertEquals(localization.getFieldLabel(Bulletin.TAGLASTSAVED), modelWithData.getColumnName(3));
		assertEquals(localization.getFieldLabel("BulletinVersionNumber"), modelWithData.getColumnName(4));
		assertEquals(localization.getFieldLabel("BulletinSize"), modelWithData.getColumnName(5));
	}
	
	public void testGetColumnCount()
	{
		assertEquals(6, modelWithoutData.getColumnCount());
		assertEquals(6, modelWithData.getColumnCount());
	}
	
	public void testGetRowCount()
	{
		assertEquals(0, modelWithoutData.getRowCount());
		assertEquals(1, modelWithOlderDraft.getRowCount());
		assertEquals(2, modelWithData.getRowCount());
	}
	
	public void testIsCellEditable()
	{
		assertEquals("flag", true, modelWithData.isCellEditable(1,modelWithData.COLUMN_RETRIEVE_FLAG));
		assertEquals("title", false, modelWithData.isCellEditable(1,modelWithData.COLUMN_TITLE));
		assertEquals("author", false, modelWithData.isCellEditable(1,modelWithData.COLUMN_AUTHOR));
		assertEquals("size", false, modelWithData.isCellEditable(1,modelWithData.COLUMN_LAST_DATE_SAVED));
		assertEquals("date", false, modelWithData.isCellEditable(1,modelWithData.COLUMN_BULLETIN_SIZE));
	}
	
	public void testGetColumnClass()
	{
		assertEquals(Boolean.class, modelWithData.getColumnClass(modelWithData.COLUMN_RETRIEVE_FLAG));
		assertEquals(String.class, modelWithData.getColumnClass(modelWithData.COLUMN_TITLE));
		assertEquals(String.class, modelWithData.getColumnClass(modelWithData.COLUMN_AUTHOR));
		assertEquals(String.class, modelWithData.getColumnClass(modelWithData.COLUMN_LAST_DATE_SAVED));
		assertEquals(Integer.class, modelWithData.getColumnClass(modelWithData.COLUMN_BULLETIN_SIZE));
	}
	
	public void testGetAndSetValueAt()
	{
		Vector authors = new Vector();
		authors.add(modelWithData.getValueAt(0,2));
		authors.add(modelWithData.getValueAt(1,2));
		assertContains("Author 0 missing?", normalBulletin.get(Bulletin.TAGAUTHOR), authors);
		assertContains("Author 2 missing?", bulletinVersion2.get(Bulletin.TAGAUTHOR), authors);
		
		assertEquals("start bool", false, ((Boolean)modelWithData.getValueAt(0,modelWithData.COLUMN_RETRIEVE_FLAG)).booleanValue());
		modelWithData.setValueAt(new Boolean(true), 0,0);
		assertEquals("setget bool", true, ((Boolean)modelWithData.getValueAt(0,modelWithData.COLUMN_RETRIEVE_FLAG)).booleanValue());

		assertEquals("start title", title2, modelWithData.getValueAt(1,modelWithData.COLUMN_TITLE));
		modelWithData.setValueAt(title2+title2, 1,1);
		assertEquals("keep title", title2, modelWithData.getValueAt(1,modelWithData.COLUMN_TITLE));

		assertTrue("B0 Size too small", ((Integer)(modelWithData.getValueAt(0,modelWithData.COLUMN_BULLETIN_SIZE))).intValue() > 1);
		assertTrue("B2 Size too small", ((Integer)(modelWithData.getValueAt(0,modelWithData.COLUMN_BULLETIN_SIZE))).intValue() > 1);

		assertEquals("start date1", "", modelWithData.getValueAt(0,modelWithData.COLUMN_LAST_DATE_SAVED));
		modelWithData.setValueAt("some date1", 0,modelWithData.COLUMN_LAST_DATE_SAVED);
		assertEquals("keep date1", "", modelWithData.getValueAt(0,modelWithData.COLUMN_LAST_DATE_SAVED));

		String expectedDateSaved = localization.formatDateTime(bulletinVersion2.getLastSavedTime());
		assertEquals("start date2", expectedDateSaved, modelWithData.getValueAt(1,modelWithData.COLUMN_LAST_DATE_SAVED));
		modelWithData.setValueAt("some date2", 1,modelWithData.COLUMN_LAST_DATE_SAVED);
		assertEquals("keep date2", expectedDateSaved, modelWithData.getValueAt(1,modelWithData.COLUMN_LAST_DATE_SAVED));
	}
	
	public void testSetAllFlags()
	{
		Boolean t = new Boolean(true);
		Boolean f = new Boolean(false);
		
		modelWithData.setAllFlags(true);
		for(int allTrueCounter = 0; allTrueCounter < modelWithData.getRowCount(); ++allTrueCounter)
			assertEquals("all true" + allTrueCounter, t, modelWithData.getValueAt(0,modelWithData.COLUMN_RETRIEVE_FLAG));

		modelWithData.setAllFlags(false);
		for(int allFalseCounter = 0; allFalseCounter < modelWithData.getRowCount(); ++allFalseCounter)
			assertEquals("all false" + allFalseCounter, f, modelWithData.getValueAt(0,modelWithData.COLUMN_RETRIEVE_FLAG));
	}
	
	public void testGetIdListWithUpdatedDraft()
	{
		modelWithOlderDraft.setAllFlags(true);
		Vector fullList = modelWithOlderDraft.getSelectedUidsLatestVersion();
		assertEquals("Modified draft not included?", 1, fullList.size());
	}
	
	public void testGetIdList()
	{
		modelWithData.setAllFlags(false);
		Vector emptyList = modelWithData.getSelectedUidsLatestVersion();
		assertEquals(0, emptyList.size());
		emptyList = modelWithData.getSelectedUidsFullHistory();
		assertEquals(0, emptyList.size());
		
		modelWithData.setAllFlags(true);

		Vector fullList = modelWithData.getSelectedUidsLatestVersion();
		assertEquals(2, fullList.size());
		assertNotEquals("hq account ID0?", hqApp.getAccountId(), ((UniversalId)fullList.get(0)).getAccountId());
		assertNotEquals("hq account ID2?", hqApp.getAccountId(), ((UniversalId)fullList.get(1)).getAccountId());

		assertContains("b0 Uid not in list?", normalBulletin.getUniversalId(), fullList);
		assertContains("b2 Uid not in list?", bulletinVersion2.getUniversalId(), fullList);
		Vector fullVersionList = modelWithData.getSelectedUidsFullHistory();
		assertEquals(3, fullVersionList.size());
		assertContains("History Uid not in list?", historyId, fullVersionList);
		
		
		modelWithData.setValueAt(new Boolean(false), 0, 0);
		String summary = (String)modelWithData.getValueAt(1,modelWithData.COLUMN_TITLE);
		assertEquals("Not correct summary?", title2, summary);
		Vector twoList = modelWithData.getSelectedUidsLatestVersion();
		assertEquals(1, twoList.size());
		assertEquals("b2 id not in LatestVersion", fullList.get(1), twoList.get(0));

		Vector fullHistoryList = modelWithData.getSelectedUidsFullHistory();
		assertEquals(2, fullHistoryList.size());
		assertEquals("History id not in FullHistory", historyId, fullHistoryList.get(1));
	}

	class MockServer extends MockMartusServer
	{
		MockServer() throws Exception
		{
			super();
			setSecurity(MockMartusSecurity.createServer());
		}
		
		public ServerForClients createServerForClients()
		{
			return new LocalMockServerForClients(this);
		}
		
		class LocalMockServerForClients extends ServerForClients
		{
			LocalMockServerForClients(MockMartusServer coreServer)
			{
				super(coreServer);
			}
			
			public Vector listFieldOfficeDraftBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags) 
			{			
				Vector result = new Vector();
				result.add(NetworkInterfaceConstants.OK);
				Vector list = new Vector();
				if(authorAccountId.equals(fieldApp1.getAccountId()))
					list.add(normalBulletin.getLocalId() + "=" + normalBulletin.getFieldDataPacket().getLocalId() + "=" + normalBulletinSize);
				if(authorAccountId.equals(fieldApp2.getAccountId()))
					list.add(createSummaryString(bulletinVersion2));

				result.add(list.toArray());
				return result;
			}

			private String createSummaryString(Bulletin bulletin) 
			{
				return bulletin.getLocalId() + BulletinSummary.fieldDelimeter + 
					bulletin.getFieldDataPacket().getLocalId() +
					BulletinSummary.fieldDelimeter + 
					bulletinVersion2Size + 
					BulletinSummary.fieldDelimeter + 
					bulletin.getLastSavedTime() +
					BulletinSummary.fieldDelimeter +
					bulletin.getHistory().get(0);
			}

		}
		
		public Vector listFieldOfficeAccounts(String hqAccountId) 
		{
			Vector v = new Vector();
			v.add(NetworkInterfaceConstants.OK);
			v.add(fieldApp1.getAccountId());
			v.add(fieldApp2.getAccountId());
			return v;			
		}

		public Vector getPacket(String hqAccountId, String authorAccountId, String bulletinLocalId, String packetLocalId)
		{
			Vector result = new Vector();
			try 
			{
				UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, packetLocalId);
				FieldDataPacket fdp = null;
				MartusCrypto security = fieldApp1.getSecurity();
				if(uid.equals(normalBulletin.getFieldDataPacket().getUniversalId()))
					fdp = normalBulletin.getFieldDataPacket();
				if(uid.equals(bulletinVersion1.getFieldDataPacket().getUniversalId()))
					fdp = bulletinVersion1.getFieldDataPacket();
				if(uid.equals(bulletinVersion2.getFieldDataPacket().getUniversalId()))
				{
					fdp = bulletinVersion2.getFieldDataPacket();
					security = fieldApp2.getSecurity();
				}

				StringWriter writer = new StringWriter();
				fdp.writeXml(writer, security);
				result.add(NetworkInterfaceConstants.OK);
				result.add(writer.toString());
				writer.close();
			} 
			catch (Exception e) 
			{
				result.add(NetworkInterfaceConstants.SERVER_ERROR);
			}
			return result;
		}
	}
	
	private final static String title0 = "cool title";
	private final static String title1 = "This is a cool title";
	private final static String title2 = "Even cooler";

	private final static String author0 = "Fred 0";
	private final static String author1 = "Betty 1";
	private final static String author2 = "Donna 2";

	private static MockMartusServer testServer;
	private static ServerSideNetworkInterface testSSLServerInterface;
	private static MockMartusApp hqApp;
	private static MockUiLocalization localization;

	private static UniversalId historyId;
	private static RetrieveHQDraftsTableModel modelWithData;
	private static RetrieveHQDraftsTableModel modelWithoutData;
	private static RetrieveHQDraftsTableModel modelWithOlderDraft;

	// NOTE: The following must be non-private to avoid Java warnings
	static int normalBulletinSize;
	static int bulletinVersion2Size;
	static MockMartusApp fieldApp1;
	static MockMartusApp fieldApp2;
	static Bulletin normalBulletin;
	static Bulletin bulletinVersion1;
	static Bulletin bulletinVersion2;

}
