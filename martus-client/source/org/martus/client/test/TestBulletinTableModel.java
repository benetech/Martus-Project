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

package org.martus.client.test;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.bulletintable.BulletinTableModel;
import org.martus.clientside.test.MockUiLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.packet.UniversalId;
import org.martus.util.TestCaseEnhanced;

public class TestBulletinTableModel extends TestCaseEnhanced
{
    public TestBulletinTableModel(String name)
	{
        super(name);
    }

    public void setUp() throws Exception
    {
    	super.setUp();
    	localization = new MockUiLocalization(getName());
		app = MockMartusApp.create(new MockClientDatabase(), localization, getName());
	    //app.setLocalization(localization);
		app.loadSampleData();
		store = app.getStore();
		folderSaved = app.getFolderSaved();
    }

    public void tearDown() throws Exception
    {
		store.deleteAllData();
		app.deleteAllFiles();
		super.tearDown();
	}
	
	public void testColumnTags()
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folderSaved);

		assertEquals("column count", 6, list.getColumnCount());
		assertEquals("status", list.getFieldName(STATUS));
		assertEquals("_wasSent", list.getFieldName(WASSENT));
		assertEquals("eventdate", list.getFieldName(EVENTDATE));
		assertEquals("title", list.getFieldName(TITLE));
		assertEquals("author", list.getFieldName(AUTHOR));
		assertEquals("_lastSavedTimestamp", list.getFieldName(SAVEDDATE));
	}

    public void testColumnLabels()
    {
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folderSaved);

		assertEquals(localization.getFieldLabel(BulletinConstants.TAGSTATUS), list.getColumnName(STATUS));
		assertEquals(localization.getFieldLabel(Bulletin.PSEUDOFIELD_WAS_SENT), list.getColumnName(WASSENT));
		assertEquals(localization.getFieldLabel(BulletinConstants.TAGEVENTDATE), list.getColumnName(EVENTDATE));
		assertEquals(localization.getFieldLabel(BulletinConstants.TAGTITLE), list.getColumnName(TITLE));
		assertEquals(localization.getFieldLabel(BulletinConstants.TAGAUTHOR), list.getColumnName(AUTHOR));
		assertEquals(localization.getFieldLabel(Bulletin.PSEUDOFIELD_LAST_SAVED_TIMESTAMP), list.getColumnName(SAVEDDATE));
	}

	public void testRows()
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folderSaved);

		assertEquals(store.getBulletinCount(), list.getRowCount());
		Bulletin b = list.getBulletin(2);
		assertEquals(b.get(BulletinConstants.TAGAUTHOR), list.getValueAt(2, AUTHOR));

		b = list.getBulletin(4);
		String displayDate = localization.convertStoredDateToDisplay(b.get(BulletinConstants.TAGEVENTDATE));
		assertEquals(displayDate, list.getValueAt(4, EVENTDATE));
    }

	public void testGetBulletin()
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folderSaved);
		for(int i = 0; i < folderSaved.getBulletinCount(); ++i)
		{
			UniversalId folderBulletinId = folderSaved.getBulletinSorted(i).getUniversalId();
			UniversalId listBulletinId = list.getBulletin(i).getUniversalId();
			assertEquals(i + "wrong bulletin?", folderBulletinId, listBulletinId);
		}
	}

	public void testGetValueAt() throws Exception
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folderSaved);

		assertEquals("", list.getValueAt(1000, 0));

		Bulletin b = list.getBulletin(0);
		b.set("title", "xyz");
		store.saveBulletin(b);

		assertEquals(Bulletin.STATUSIMMUTABLE, b.getStatus());
		assertEquals(localization.getStatusLabel("sealed"), list.getValueAt(0,STATUS));

		b.set(BulletinConstants.TAGEVENTDATE, "1999-04-15");
		store.saveBulletin(b);
		String displayDate = localization.convertStoredDateToDisplay("1999-04-15");
		assertEquals(displayDate, list.getValueAt(0,EVENTDATE));

		assertEquals("xyz", b.get(BulletinConstants.TAGTITLE));
		assertEquals("xyz", list.getValueAt(0,TITLE));

		b.setMutable();
		store.saveBulletin(b);
		assertEquals(localization.getStatusLabel("draft"), list.getValueAt(0,STATUS));
		b.setImmutable();
		store.saveBulletin(b);
		assertEquals(localization.getStatusLabel("sealed"), list.getValueAt(0,STATUS));
	}
	
	public void testSentColumnGetValueAtSealed() throws Exception
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folderSaved);
		int row = 0;
		Bulletin b = list.getBulletin(row);
		assertTrue("not Immutable?", b.isImmutable());
		assertEquals("already sent?", "", list.getValueAt(row, WASSENT));
		
		store.setIsOnServer(b);
		assertEquals("not sent?", localization.getFieldLabel("WasSentYes"), list.getValueAt(row, WASSENT));
		
		store.setIsNotOnServer(b);
		assertEquals("sent?", localization.getFieldLabel("WasSentNo"), list.getValueAt(row, WASSENT));
	}
	
	public void testSentColumnGetValueMyDraft() throws Exception
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folderSaved);

		Bulletin myDraft = store.createEmptyBulletin();
		store.saveBulletin(myDraft);
		UniversalId uid = myDraft.getUniversalId();
		store.addBulletinToFolder(folderSaved, uid);
		int row = folderSaved.find(uid);
		assertEquals("should be unknown", "", list.getValueAt(row, WASSENT));

		BulletinFolder draftOutbox = store.getFolderDraftOutbox();

		store.setIsOnServer(myDraft);
		assertEquals("on server should be respected", localization.getFieldLabel("WasSentYes"), list.getValueAt(row, WASSENT));
		
		store.setIsNotOnServer(myDraft);
		assertEquals("not on server should be respected", localization.getFieldLabel("WasSentNo"), list.getValueAt(row, WASSENT));

		store.addBulletinToFolder(draftOutbox, uid);
		assertEquals("not on server and in outbox should be unsent", localization.getFieldLabel("WasSentNo"), list.getValueAt(row, WASSENT));

		store.setIsOnServer(myDraft);
		assertEquals("on server but in outbox should be No", localization.getFieldLabel("WasSentNo"), list.getValueAt(row, WASSENT));
	}
	
	public void testSentColumnGetValueAtNotMyDraft() throws Exception
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folderSaved);

		MockMartusSecurity otherSecurity = MockMartusSecurity.createOtherClient();
		Bulletin notMine = new Bulletin(otherSecurity);
		app.getStore().saveBulletinForTesting(notMine);
		UniversalId uid = notMine.getUniversalId();
		store.addBulletinToFolder(folderSaved, uid);
		int row = folderSaved.find(uid);
		assertEquals("should be unknown", "", list.getValueAt(row, WASSENT));

		BulletinFolder draftOutbox = store.getFolderDraftOutbox();

		store.setIsOnServer(notMine);
		assertEquals("on server should be respected", localization.getFieldLabel("WasSentYes"), list.getValueAt(row, WASSENT));
		
		store.setIsNotOnServer(notMine);
		assertEquals("not on server should be respected", localization.getFieldLabel("WasSentNo"), list.getValueAt(row, WASSENT));

		store.addBulletinToFolder(draftOutbox, uid);
		assertEquals("not on server and in outbox should be unsent", localization.getFieldLabel("WasSentNo"), list.getValueAt(row, WASSENT));

		store.setIsOnServer(notMine);
		assertEquals("on server but in outbox should be unknown", "", list.getValueAt(row, WASSENT));
	}

	public void testSetFolder()
	{
		BulletinTableModel list = new BulletinTableModel(app);
		assertEquals(0, list.getRowCount());

		list.setFolder(folderSaved);
		assertEquals(store.getBulletinCount(), folderSaved.getBulletinCount());
		assertEquals(folderSaved.getBulletinSorted(0).getLocalId(), list.getBulletin(0).getLocalId());

		BulletinFolder empty = store.createFolder("empty");
		assertEquals(0, empty.getBulletinCount());
		list.setFolder(empty);
		assertEquals(0, list.getRowCount());
	}

	public void testFindBulletin() throws Exception
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folderSaved);

		assertEquals(-1, list.findBulletin(null));

		assertTrue("Need at least two sample bulletins", list.getRowCount() >= 2);
		int last = list.getRowCount()-1;
		Bulletin bFirst = list.getBulletin(0);
		Bulletin bLast = list.getBulletin(last);
		assertEquals(0, list.findBulletin(bFirst.getUniversalId()));
		assertEquals(last, list.findBulletin(bLast.getUniversalId()));

		Bulletin b = store.createEmptyBulletin();
		assertEquals(-1, list.findBulletin(b.getUniversalId()));
		store.saveBulletin(b);
		assertEquals(-1, list.findBulletin(b.getUniversalId()));
	}

	public void testSortByColumn()
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folderSaved);

		String tag = BulletinConstants.TAGEVENTDATE;
		int col = EVENTDATE;
		assertEquals(tag, list.getFieldName(col));
		assertEquals(tag, folderSaved.sortedBy());
		String first = (String)list.getValueAt(0, col);
		list.sortByColumn(col);
		assertEquals(tag, folderSaved.sortedBy());
		assertEquals(false, first.equals(list.getValueAt(0,col)));
	}
	
	public void testHtmlTags() throws Exception
	{
		BulletinTableModel list = new BulletinTableModel(app);
		list.setFolder(folderSaved);

		Bulletin b = list.getBulletin(0);
		b.set(BulletinConstants.TAGTITLE, "<HTML><body><H1><center>Charles</center></H1></BODY></HTML>");

		store.saveBulletin(b);
		assertEquals(" <HTML><body><H1><center>Charles</center></H1></BODY></HTML>", list.getValueAt(0,TITLE));
		
		b = list.getBulletin(0);
		assertEquals("<HTML><body><H1><center>Charles</center></H1></BODY></HTML>", b.get(BulletinConstants.TAGTITLE));


	}
	
	private static final int STATUS = 0;
	private static final int WASSENT = 1;
	private static final int EVENTDATE = 2;
	private static final int TITLE = 3;
	private static final int AUTHOR = 4;
	private static final int SAVEDDATE = 5;

	MockMartusApp app;
	MockUiLocalization localization;
	ClientBulletinStore store;
	BulletinFolder folderSaved;

}
