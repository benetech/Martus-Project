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

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.foldertree.FolderList;
import org.martus.client.swingui.foldertree.FolderTreeNode;
import org.martus.clientside.UiLocalization;
import org.martus.clientside.test.MockUiLocalization;
import org.martus.common.database.MockClientDatabase;
import org.martus.util.TestCaseEnhanced;

public class TestFolderList extends TestCaseEnhanced
{
    public TestFolderList(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
    	super.setUp();
    	localization = new MockUiLocalization(getName());
		app = MockMartusApp.create(new MockClientDatabase(), getName());
    }

    public void tearDown() throws Exception
    {
    	app.deleteAllFiles();
    	super.tearDown();
    }

	public void testBasics() throws Exception
	{
		app.loadSampleData();
		ClientBulletinStore store = app.getStore();
		FolderList list = new FolderList(getLocalization());
		list.loadFolders(store);

		int baseCount = getVisibleFolderCount(store);
		assertEquals("Initial count", baseCount, list.getCount());

		FolderTreeNode node = list.findFolderByInternalName("lisjf;lisjef");
		assertNull("Find folder that isn't there", node);

		store.createFolder("test");
		list.loadFolders(store);
		assertEquals(baseCount+1, list.getCount());
		assertEquals("Sent/saved not first?", getLocalization().getLocalizedFolderName(app.getFolderSaved().getName()), list.getName(0));

		node = list.getNode(baseCount);
		assertEquals("test", node.toString());
		node = list.findFolderByInternalName("test");
		assertEquals("test", node.toString());

		store.renameFolder("test", "new");
		list.loadFolders(store);
		assertEquals(getVisibleFolderCount(store), list.getCount());
		assertEquals("new", list.getName(list.getCount()-1));
		node = list.findFolderByInternalName("test");
		assertNull("Find deleted folder", node);
		node = list.findFolderByInternalName("new");
		assertEquals("new", node.toString());

		store.deleteFolder("new");
		list.loadFolders(store);
		assertEquals(baseCount, list.getCount());
	}

	public void testLocalizedFolders() throws Exception
	{
		app.loadSampleData();
		ClientBulletinStore store = app.getStore();
		FolderList list = new FolderList(getLocalization());
		list.loadFolders(store);

		int baseCount = getVisibleFolderCount(store);
		assertEquals("Initial count", baseCount, list.getCount());

		store.createFolder(app.getNameOfFolderRetrievedSealed());
		list.loadFolders(store);
		assertEquals(baseCount+1, list.getCount());
		assertEquals("Outbox not first?", getLocalization().getLocalizedFolderName(app.getFolderSaved().getName()), list.getName(0));

		FolderTreeNode node = list.getNode(baseCount);
		assertEquals(app.getNameOfFolderRetrievedSealed(), node.getInternalName());

		assertEquals(getLocalization().getLocalizedFolderName(app.getNameOfFolderRetrievedSealed()), node.getLocalizedName());
		store.deleteFolder(app.getNameOfFolderRetrievedSealed());
		list.loadFolders(store);
		assertEquals(baseCount, list.getCount());

	}

	public void testLoadFolders() throws Exception
	{
		app.loadSampleData();
		ClientBulletinStore store = app.getStore();
		assertTrue("Need sample folders", getVisibleFolderCount(store) > 0);

		FolderList ourList = new FolderList(getLocalization());
		ourList.loadFolders(store);
		assertEquals("Didn't load properly", getVisibleFolderCount(store), ourList.getCount());
		ourList.loadFolders(store);
		assertEquals("Reload failed", getVisibleFolderCount(store), ourList.getCount());

	}

	private int getVisibleFolderCount(ClientBulletinStore store)
	{
		return store.getVisibleFolderNames().size();
	}
	
	private UiLocalization getLocalization()
	{
		return localization;

	}

	MockUiLocalization localization;
	MockMartusApp app;
}

