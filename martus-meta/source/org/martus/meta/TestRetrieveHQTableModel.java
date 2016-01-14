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

import org.martus.client.swingui.tablemodels.RetrieveHQTableModel;
import org.martus.client.swingui.tablemodels.RetrieveTableModel;
import org.martus.client.test.MockMartusApp;
import org.martus.clientside.test.MockUiLocalization;
import org.martus.common.BulletinSummary;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusUtilities;
import org.martus.common.MiniLocalization;
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

public class TestRetrieveHQTableModel extends TestCaseEnhanced
{
	public TestRetrieveHQTableModel(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		if(localization != null)
			return;	
		MartusCrypto hqSecurity = MockMartusSecurity.createHQ();
		localization = new MockUiLocalization(getName());
		hqApp = MockMartusApp.create(hqSecurity, getName());

		MartusCrypto fieldSecurity1 = MockMartusSecurity.createClient();
		fieldApp1 = MockMartusApp.create(fieldSecurity1, getName());
		ReadableDatabase db1 = fieldApp1.getStore().getDatabase();

		MartusCrypto fieldSecurity2 = MockMartusSecurity.createOtherClient();
		fieldApp2 = MockMartusApp.create(fieldSecurity2, getName());
		ReadableDatabase db2 = fieldApp2.getStore().getDatabase();

		assertNotEquals("account Id's equal?", fieldApp1.getAccountId(), fieldApp2.getAccountId());

		b0 = fieldApp1.createBulletin();
		b0.set(Bulletin.TAGTITLE, title0);
		b0.set(Bulletin.TAGAUTHOR, author0);
		b0.setAllPrivate(true);
		HeadquartersKeys hqKeys = new HeadquartersKeys();
		HeadquartersKey key = new HeadquartersKey(hqApp.getAccountId());
		hqKeys.add(key);
		b0.setAuthorizedToReadKeys(hqKeys);
		fieldApp1.getStore().saveBulletin(b0);
		b0Size = 20;

		b1 = fieldApp1.createBulletin();
		b1.set(Bulletin.TAGTITLE, title1);
		b1.set(Bulletin.TAGAUTHOR, author1);
		b1.setAllPrivate(false);
		b1.setAuthorizedToReadKeys(hqKeys);
		fieldApp1.getStore().saveBulletin(b1);
		b1Size = MartusUtilities.getBulletinSize(fieldApp1.getStore().getDatabase(), b1.getBulletinHeaderPacket());

		b2 = fieldApp2.createBulletin();
		b2.set(Bulletin.TAGTITLE, title2);
		b2.set(Bulletin.TAGAUTHOR, author2);
		b2.setAllPrivate(true);
		b2.setAuthorizedToReadKeys(hqKeys);
		BulletinHistory history2 = new BulletinHistory();
		history2.add(b1.getLocalId());
		historyId = UniversalId.createFromAccountAndLocalId(b2.getAccount(), b1.getLocalId());
		b2.setHistory(history2);
		
		fieldApp2.getStore().saveBulletin(b2);
		b2Size = MartusUtilities.getBulletinSize(fieldApp1.getStore().getDatabase(), b2.getBulletinHeaderPacket());
	
		testServer = new MockServer();
		testServer.verifyAndLoadConfigurationFiles();
		testSSLServerInterface = new ServerSideNetworkHandler(testServer.serverForClients);
		hqApp.setSSLNetworkInterfaceHandlerForTesting(testSSLServerInterface);
		modelWithData = new RetrieveHQTableModel(hqApp, localization);
		modelWithData.initialize(null);
		modelWithData.setAllFlags(false);
		assertEquals("checkbox not false to start?", false, ((Boolean)modelWithData.getValueAt(0,modelWithData.COLUMN_RETRIEVE_FLAG)).booleanValue());

		importBulletinFromFieldOfficeToHq(db1, b0, fieldSecurity1);
		importBulletinFromFieldOfficeToHq(db1, b1, fieldSecurity1);
		importBulletinFromFieldOfficeToHq(db2, b2, fieldSecurity2);
		assertEquals("checkbox turned true by import?", false, ((Boolean)modelWithData.getValueAt(0,modelWithData.COLUMN_RETRIEVE_FLAG)).booleanValue());
		
		modelWithoutData = new RetrieveHQTableModel(hqApp, localization);
		modelWithoutData.initialize(null);
		assertEquals(0, modelWithoutData.getRowCount());

		assertEquals("checkbox turned true by the other model?", false, ((Boolean)modelWithData.getValueAt(0,modelWithData.COLUMN_RETRIEVE_FLAG)).booleanValue());
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
		assertEquals(3, modelWithData.getRowCount());
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
		// NOTE: There seems to be some data bleeding between test methods
		// This is a quick hack workaround, safe because of asserts added in setUp
		modelWithData.setAllFlags(false);

		Vector authors = new Vector();
		authors.add(modelWithData.getValueAt(0,modelWithData.COLUMN_AUTHOR));
		authors.add(modelWithData.getValueAt(1,modelWithData.COLUMN_AUTHOR));
		authors.add(modelWithData.getValueAt(2,modelWithData.COLUMN_AUTHOR));
		assertContains("Author 0 missing?", b0.get(Bulletin.TAGAUTHOR), authors);
		assertContains("Author 1 missing?", b1.get(Bulletin.TAGAUTHOR), authors);
		assertContains("Author 2 missing?", b2.get(Bulletin.TAGAUTHOR), authors);
		
		assertEquals("get caused flag to flip to true?", false, ((Boolean)modelWithData.getValueAt(0,modelWithData.COLUMN_RETRIEVE_FLAG)).booleanValue());
		modelWithData.setValueAt(new Boolean(true), 0,modelWithData.COLUMN_RETRIEVE_FLAG);
		assertEquals("setget bool", true, ((Boolean)modelWithData.getValueAt(0,modelWithData.COLUMN_RETRIEVE_FLAG)).booleanValue());

		assertEquals("start title", title2, modelWithData.getValueAt(2,modelWithData.COLUMN_TITLE));
		modelWithData.setValueAt(title2+title2, 2,modelWithData.COLUMN_TITLE);
		assertEquals("keep title", title2, modelWithData.getValueAt(2,modelWithData.COLUMN_TITLE));
		
		assertEquals("B0 Size incorrect minimum not 1?", 1, ((Integer)modelWithData.getValueAt(0,modelWithData.COLUMN_BULLETIN_SIZE)).intValue());
		assertEquals("B1 Size incorrect", RetrieveTableModel.getSizeInKbytes(b1Size), modelWithData.getValueAt(1,modelWithData.COLUMN_BULLETIN_SIZE));
		assertEquals("B2 Size incorrect", RetrieveTableModel.getSizeInKbytes(b2Size), modelWithData.getValueAt(2,modelWithData.COLUMN_BULLETIN_SIZE));

		assertEquals("start date1", "", modelWithData.getValueAt(0,modelWithData.COLUMN_LAST_DATE_SAVED));
		modelWithData.setValueAt("some date1", 0,modelWithData.COLUMN_LAST_DATE_SAVED);
		assertEquals("keep date1", "", modelWithData.getValueAt(0,modelWithData.COLUMN_LAST_DATE_SAVED));

		String expectedDateSaved = localization.formatDateTime(BulletinSummary.getLastDateTimeSaved(dateSavedInMillis2));
		assertEquals("start date2", expectedDateSaved, modelWithData.getValueAt(2,modelWithData.COLUMN_LAST_DATE_SAVED));
		modelWithData.setValueAt("some date2", 2,modelWithData.COLUMN_LAST_DATE_SAVED);
		assertEquals("keep date2", expectedDateSaved, modelWithData.getValueAt(2,modelWithData.COLUMN_LAST_DATE_SAVED));
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
	
	public void testGetIdList()
	{
		modelWithData.setAllFlags(false);
		Vector emptyList = modelWithData.getSelectedUidsLatestVersion();
		assertEquals(0, emptyList.size());
		emptyList = modelWithData.getSelectedUidsFullHistory();
		assertEquals(0, emptyList.size());
		
		modelWithData.setAllFlags(true);

		Vector fullList = modelWithData.getSelectedUidsLatestVersion();
		assertEquals(3, fullList.size());
		assertNotEquals("hq account ID0?", hqApp.getAccountId(), ((UniversalId)fullList.get(0)).getAccountId());
		assertNotEquals("hq account ID1?", hqApp.getAccountId(), ((UniversalId)fullList.get(1)).getAccountId());
		assertNotEquals("hq account ID2?", hqApp.getAccountId(), ((UniversalId)fullList.get(2)).getAccountId());

		assertContains("b0 Uid not in list?", b0.getUniversalId(), fullList);
		assertContains("b1 Uid not in list?", b1.getUniversalId(), fullList);
		assertContains("b2 Uid not in list?", b2.getUniversalId(), fullList);

		Vector fullHistoryList = modelWithData.getSelectedUidsFullHistory();
		assertEquals(4, fullHistoryList.size());
		assertNotEquals("hq account ID0 fullHistoryList?", hqApp.getAccountId(), ((UniversalId)fullHistoryList.get(0)).getAccountId());
		assertNotEquals("hq account ID1 fullHistoryList?", hqApp.getAccountId(), ((UniversalId)fullHistoryList.get(1)).getAccountId());
		assertNotEquals("hq account ID2 fullHistoryList?", hqApp.getAccountId(), ((UniversalId)fullHistoryList.get(2)).getAccountId());
		assertNotEquals("hq account ID3 fullHistoryList?", hqApp.getAccountId(), ((UniversalId)fullHistoryList.get(3)).getAccountId());

		assertContains("b0 Uid not in list?", b0.getUniversalId(), fullHistoryList);
		assertContains("b1 Uid not in list?", b1.getUniversalId(), fullHistoryList);
		assertContains("b2 Uid not in list?", b2.getUniversalId(), fullHistoryList);
		assertContains("history Uid not in list?", historyId, fullHistoryList);

		
		modelWithData.setValueAt(new Boolean(false), 1, 0);
		Vector twoList = modelWithData.getSelectedUidsLatestVersion();

		assertEquals(2, twoList.size());
		assertEquals("b0 id", fullList.get(0), twoList.get(0));
		assertEquals("b2 id", fullList.get(2), twoList.get(1));

		Vector fullHistoryListTwo = modelWithData.getSelectedUidsFullHistory();

		assertEquals(3, fullHistoryListTwo.size());
		assertEquals("b0 id", fullHistoryList.get(0), fullHistoryListTwo.get(0));
		assertEquals("b2 id", fullHistoryList.get(2), fullHistoryListTwo.get(1));
		assertEquals("history id", fullHistoryList.get(3), fullHistoryListTwo.get(2));
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

			public Vector listFieldOfficeSealedBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
			{
				Vector result = new Vector();
				result.add(NetworkInterfaceConstants.OK);
				Vector list = new Vector();
				if(authorAccountId.equals(b0.getAccount()))
					list.add(b0.getLocalId() + "=" + b0.getFieldDataPacket().getLocalId() + "=" + b0Size);
				if(authorAccountId.equals(b1.getAccount()))
					list.add(b1.getLocalId() + "=" + b1.getFieldDataPacket().getLocalId() + "=" + b1Size);
				if(authorAccountId.equals(b2.getAccount()))
					list.add(b2.getLocalId() + BulletinSummary.fieldDelimeter + 
							b2.getFieldDataPacket().getLocalId() +
							BulletinSummary.fieldDelimeter + 
							b2Size + 
							BulletinSummary.fieldDelimeter + 
							dateSavedInMillis2 +
							BulletinSummary.fieldDelimeter + 
							b2.getHistory().get(0)
							);
				result.add(list.toArray());
				return result;
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
				if(uid.equals(b0.getFieldDataPacket().getUniversalId()))
					fdp = b0.getFieldDataPacket();
				if(uid.equals(b1.getFieldDataPacket().getUniversalId()))
					fdp = b1.getFieldDataPacket();
				if(uid.equals(b2.getFieldDataPacket().getUniversalId()))
				{
					fdp = b2.getFieldDataPacket();
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
	
	final static String title0 = "cool title";
	final static String title1 = "This is a cool title";
	final static String title2 = "Even cooler";
	final static String dateSavedInMillis2 = "1083873923190";

	final static String author0 = "Fred 0";
	final static String author1 = "Betty 1";
	final static String author2 = "Donna 2";

	static int b0Size;
	static int b1Size;
	static int b2Size;

	static MockMartusServer testServer;
	static ServerSideNetworkInterface testSSLServerInterface;
	static MockMartusApp fieldApp1;
	static MockMartusApp fieldApp2;
	static MockMartusApp hqApp;
	static MiniLocalization localization;
	
	static Bulletin b0;
	static Bulletin b1;
	static Bulletin b2;
	static UniversalId historyId;

	static RetrieveHQTableModel modelWithData;
	static RetrieveHQTableModel modelWithoutData;
}
