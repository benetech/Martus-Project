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

import java.io.StringWriter;
import java.util.Vector;

import org.martus.client.swingui.tablemodels.RetrieveMyTableModel;
import org.martus.client.test.MockMartusApp;
import org.martus.clientside.test.MockUiLocalization;
import org.martus.common.BulletinSummary;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NonSSLNetworkAPI;
import org.martus.common.network.ServerSideNetworkInterface;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.MockServerForClients;
import org.martus.server.forclients.ServerForClients;
import org.martus.server.forclients.ServerSideNetworkHandler;
import org.martus.server.forclients.ServerSideNetworkHandlerForNonSSL;
import org.martus.util.TestCaseEnhanced;

public class TestRetrieveMyTableModel extends TestCaseEnhanced
{
	public TestRetrieveMyTableModel(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		MartusCrypto appSecurity = MockMartusSecurity.createClient();
		localization = new MockUiLocalization(getName());
		app = MockMartusApp.create(appSecurity, getName());

		b0 = app.createBulletin();
		b0.set(Bulletin.TAGTITLE, title1);
		app.getStore().saveBulletin(b0);
		b1 = app.createBulletin();
		b1.set(Bulletin.TAGTITLE, title1);
		app.getStore().saveBulletin(b1);
		b2 = app.createBulletin();
		b2.set(Bulletin.TAGTITLE, title2);
		BulletinHistory history2 = new BulletinHistory();
		history2.add(b1.getLocalId());
		historyId = UniversalId.createFromAccountAndLocalId(b2.getAccount(), b1.getLocalId());
		b2.setHistory(history2);
		app.getStore().saveBulletin(b2);

		testServer = new MockServer();
		testServer.verifyAndLoadConfigurationFiles();
		
		testServerInterface = new ServerSideNetworkHandlerForNonSSL(testServer.serverForClients);
		testSSLServerInterface = new ServerSideNetworkHandler(testServer.serverForClients);
		app.setSSLNetworkInterfaceHandlerForTesting(testSSLServerInterface);
		modelWithoutData = new RetrieveMyTableModel(app, localization);
		modelWithoutData.initialize(null);
		app.getStore().deleteAllData();
		modelWithData = new RetrieveMyTableModel(app, localization);
		modelWithData.initialize(null);
	}
	
	public void tearDown() throws Exception
	{
		testServer.deleteAllFiles();
    	app.deleteAllFiles();
    	super.tearDown();
    }

	public void testGetColumnName()
	{
		assertEquals(localization.getFieldLabel("retrieveflag"), modelWithData.getColumnName(0));
		assertEquals(localization.getFieldLabel(Bulletin.TAGTITLE), modelWithData.getColumnName(1));
		assertEquals(localization.getFieldLabel(Bulletin.TAGLASTSAVED), modelWithData.getColumnName(2));
		assertEquals(localization.getFieldLabel("BulletinVersionNumber"), modelWithData.getColumnName(3));
		assertEquals(localization.getFieldLabel("BulletinSize"), modelWithData.getColumnName(4));
	}
	
	public void testGetColumnCount()
	{
		assertEquals(5, modelWithoutData.getColumnCount());
		assertEquals(5, modelWithData.getColumnCount());
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
		assertEquals("size", false, modelWithData.isCellEditable(1,modelWithData.COLUMN_LAST_DATE_SAVED));
		assertEquals("date", false, modelWithData.isCellEditable(1,modelWithData.COLUMN_BULLETIN_SIZE));
	}
	
	public void testGetColumnClass()
	{
		assertEquals(Boolean.class, modelWithData.getColumnClass(modelWithData.COLUMN_RETRIEVE_FLAG));
		assertEquals(String.class, modelWithData.getColumnClass(modelWithData.COLUMN_TITLE));
		assertEquals(String.class, modelWithData.getColumnClass(modelWithData.COLUMN_LAST_DATE_SAVED));
		assertEquals(Integer.class, modelWithData.getColumnClass(modelWithData.COLUMN_BULLETIN_SIZE));
	}
	
	public void testGetAndSetValueAt()
	{
		assertEquals("start bool", false, ((Boolean)modelWithData.getValueAt(0,modelWithData.COLUMN_RETRIEVE_FLAG)).booleanValue());
		modelWithData.setValueAt(new Boolean(true), 0,modelWithData.COLUMN_RETRIEVE_FLAG);
		assertEquals("setget bool", true, ((Boolean)modelWithData.getValueAt(0,modelWithData.COLUMN_RETRIEVE_FLAG)).booleanValue());

		assertEquals("start title", title2, modelWithData.getValueAt(2,modelWithData.COLUMN_TITLE));
		modelWithData.setValueAt(title2+title2, 2,modelWithData.COLUMN_TITLE);
		assertEquals("keep title", title2, modelWithData.getValueAt(2,modelWithData.COLUMN_TITLE));

		assertEquals("b0 size", new Integer(b0Size/1000), modelWithData.getValueAt(0,modelWithData.COLUMN_BULLETIN_SIZE));
		assertEquals("b1 size", new Integer(b1Size/1000), modelWithData.getValueAt(1,modelWithData.COLUMN_BULLETIN_SIZE));
		assertEquals("b2 size", new Integer(b2Size/1000), modelWithData.getValueAt(2,modelWithData.COLUMN_BULLETIN_SIZE));

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
		modelWithData.setValueAt(new Boolean(false), 1, 0);
		Vector twoList = modelWithData.getSelectedUidsLatestVersion();
		assertEquals(2, twoList.size());
		assertEquals("b0 id", b0.getUniversalId(), twoList.get(0));
		assertEquals("b2 id", b2.getUniversalId(), twoList.get(1));

		Vector fullList = modelWithData.getSelectedUidsFullHistory();
		assertEquals(3, fullList.size());
		assertEquals("b0 id full History", b0.getUniversalId(), fullList.get(0));
		assertEquals("b2 id full History", b2.getUniversalId(), fullList.get(1));
		assertEquals("History id full History", historyId, fullList.get(2));
	}

	class MockServer extends MockMartusServer
	{
		MockServer() throws Exception
		{
			super();
		}
		
		public ServerForClients createServerForClients()
		{
			return new LocalMockServerForClients(this);
		}
		
		class LocalMockServerForClients extends MockServerForClients
		{
			LocalMockServerForClients(MockMartusServer coreServer)
			{
				super(coreServer);
			}

			public Vector listMySealedBulletinIds(String clientId, Vector retrieveTags)
			{
				Vector result = new Vector();
				result.add(NetworkInterfaceConstants.OK);
				Vector list = new Vector();
				list.add(b0.getLocalId() + "=" +  b0.getFieldDataPacket().getLocalId() + "=" + b0Size);
				list.add(b1.getLocalId() + "=" +  b1.getFieldDataPacket().getLocalId() + "=" + b1Size);
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
				Vector sizes = new Vector();
				if(retrieveTags.size() == 1)
				{
					sizes.add(new Integer(b0Size));
					sizes.add(new Integer(b1Size));
					sizes.add(new Integer(b2Size));
				}
				result.add(sizes);
				return result;
			}
			
		}
		
		public Vector getPacket(String hqAccountId, String authorAccountId, String bulletinLocalId, String packetLocalId)
		{
			Vector result = new Vector();
			try 
			{
				UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, packetLocalId);
				FieldDataPacket fdp = null;
				if(uid.equals(b0.getFieldDataPacket().getUniversalId()))
					fdp = b0.getFieldDataPacket();
				if(uid.equals(b1.getFieldDataPacket().getUniversalId()))
					fdp = b1.getFieldDataPacket();
				if(uid.equals(b2.getFieldDataPacket().getUniversalId()))
					fdp = b2.getFieldDataPacket();
				StringWriter writer = new StringWriter();
				MartusCrypto security = app.getSecurity();
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
	
	final static String title1 = "This is a cool title";
	final static String title2 = "Even cooler";
	final static String dateSavedInMillis2 = "1083873923190";
	final static int b0Size = 3000;
	final static int b1Size = 5000;
	final static int b2Size = 8000;

	MockMartusServer testServer;
	NonSSLNetworkAPI testServerInterface;
	ServerSideNetworkInterface testSSLServerInterface;
	MockMartusApp app;
	MiniLocalization localization;
	Bulletin b0;
	Bulletin b1;
	Bulletin b2;
	UniversalId historyId;

	RetrieveMyTableModel modelWithData;
	RetrieveMyTableModel modelWithoutData;
}
