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

package org.martus.client.bulletinstore;

import java.io.IOException;
import java.util.Set;
import java.util.Vector;
import org.martus.client.bulletinstore.ClientBulletinStore.BulletinAlreadyExistsException;
import org.martus.client.test.MockBulletinStore;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.TestCaseEnhanced;

public class TestBulletinFolder extends TestCaseEnhanced
{
    public TestBulletinFolder(String name)
	{
        super(name);
    }

    public void setUp() throws Exception
    {
    	super.setUp();
    	if(testStore == null)
    	{
			testStore = new MockBulletinStore();
			testFolder = testStore.createFolder("something");

			testBulletin1 = testStore.createEmptyBulletin();
    		testStore.saveBulletin(testBulletin1);
			testFolder.add(testBulletin1);

			testBulletin2 = testStore.createEmptyBulletin();
    		testStore.saveBulletin(testBulletin2);
			testFolder.add(testBulletin2);
    	}
    }

    public void testBasics() throws Exception
    {
		ClientBulletinStore tempStore = new MockBulletinStore();
		assertEquals(false, (tempStore == null));

		// shouldn't normally create a folder this way!
		BulletinFolder folder = new BulletinFolder(tempStore, "bad");
		assertEquals("Raw folder should start out empty\n", 0, folder.getBulletinCount());

		// this is the way to get a folder
		folder = tempStore.createFolder("blah");
		assertEquals(false, (folder == null));
		assertEquals("Store folder should start out empty\n", 0, folder.getBulletinCount());

		assertEquals(tempStore, folder.getStore());

	}

	public void testSetName() throws Exception
	{
		ClientBulletinStore store = new MockBulletinStore();

		final String name = "Interesting folder name";
		BulletinFolder folder = store.createFolder(name);
		assertEquals(name, folder.getName());

		folder.setName("Boring");
		assertEquals("Boring", folder.getName());

		assertEquals(true, folder.canRename());
		folder.preventRename();
		assertEquals(false, folder.canRename());
		folder.setName("Different");
		assertEquals("Boring", folder.getName());
	}
	
	public void testOpenClosed() throws Exception
	{
		ClientBulletinStore store = new MockBulletinStore();

		final String name = "a New folder name";
		BulletinFolder folder = store.createFolder(name);
		assertTrue("New Folder should be open", folder.isOpen());
		assertFalse("New Folder should not be closed", folder.isClosed());
		folder.setClosed();
		assertFalse("A closed Folder should not be open", folder.isOpen());
		assertTrue("A closed Folder should be closed", folder.isClosed());
		folder.setOpen();
		assertTrue("A reopened Folder should be open", folder.isOpen());
		assertFalse("A reopened Folder should not be closed", folder.isClosed());
		folder.setClosed();

		store.saveFolders();
		store.loadFolders();
		BulletinFolder savedFolder = store.findFolder(name);
		assertTrue("reloaded closed Folder not closed initially?", savedFolder.isClosed());
	}

	public void testCanDelete() throws Exception
	{
		ClientBulletinStore store = new MockBulletinStore();
		BulletinFolder folder = store.createFolder("blah");
		assertEquals(true, folder.canDelete());
		folder.preventDelete();
		assertEquals(false, folder.canDelete());
	}

	public void testIsVisible() throws Exception
	{
		ClientBulletinStore store = new MockBulletinStore();
		BulletinFolder normalFolder = store.createFolder("blah");
		assertEquals("not visible?", true, normalFolder.isVisible());

		BulletinFolder hiddenFolder = store.createFolder("*blah");
		assertEquals("visible?", false, hiddenFolder.isVisible());
	}

	public void testGetBulletin() throws Exception
	{
		BulletinFolder folder = testStore.createFolder("blah");
		assertEquals(0, folder.getBulletinCount());

		Bulletin b = folder.getBulletinSorted(-1);
		assertEquals(null, b);

		b = folder.getBulletinSorted(0);
		assertEquals(null, b);

		b = folder.getBulletinSorted(folder.getBulletinCount());
		assertEquals(null, b);

		createEmptyBulletins(folder, 6);
		assertEquals(6, folder.getBulletinCount());

		b = folder.getBulletinSorted(folder.getBulletinCount());
		assertEquals(null, b);

		b = folder.getBulletinSorted(0);
		assertEquals(false, (b == null));
	}

	public void testAdd() throws Exception
	{
		BulletinFolder folder = testStore.createFolder("a2");

		// can't add unsaved bulletin to a folder
		Bulletin b = testStore.createEmptyBulletin();
		assertTrue(b != null);
		try
		{
			folder.add(b);
			fail("Should have thrown for unsaved bulletin");
		}
		catch(IOException ignoreExpected)
		{
		}
		assertEquals(0, folder.getBulletinCount());

		final int count = 7;

		BulletinFolder scratchFolder = testStore.createFolder("b");
		createEmptyBulletins(scratchFolder, count);
		assertEquals(count, scratchFolder.getBulletinCount());
		assertEquals(0, folder.getBulletinCount());

		Set s = testStore.getAllBulletinLeafUids();
		
		Vector v = new Vector(s);
		UniversalId uid0 = (UniversalId)v.get(0);
		b = testStore.getBulletinRevision(uid0);
		folder.add(b);
		assertEquals(1, folder.getBulletinCount());
		assertEquals(true, folder.contains(b));

		try
		{
			// adding has no effect if it's already there
			folder.add(b);
			fail("should have thrown exists");
		}
		catch (BulletinAlreadyExistsException expectedException)
		{
		}
		assertEquals(1, folder.getBulletinCount());
		assertEquals(true, folder.contains(b));

		UniversalId uid1 = (UniversalId)v.get(1);
		b = testStore.getBulletinRevision(uid1);
		assertEquals("This bulletin is not in the folder\n", false, folder.contains(b));

		Bulletin b2 = folder.getBulletinSorted(0);
		assertEquals("First bulletin in folder should be valid\n", false, (b2 == null));
	}

	public void testRemove() throws Exception
	{
		assertEquals("start", 2, testFolder.getBulletinCount());
		UniversalId badId = UniversalIdForTesting.createDummyUniversalId();
		testFolder.remove(badId);

		assertEquals("after non remove", 2, testFolder.getBulletinCount());

		testFolder.remove(testBulletin1.getUniversalId());
		assertEquals(1, testFolder.getBulletinCount());
		testFolder.add(testBulletin1);
	}

	public void testAddBulletinAgainToSameFolder() throws Exception
	{
		assertTrue("Bulletin b not already in folder?", testFolder.contains(testBulletin1));
		try
		{
			testFolder.add(testBulletin1);
			fail("Should have thrown exists exception");
		}
		catch (BulletinAlreadyExistsException expectedException)
		{
		}
	}
	
	
	public void testRemoveAll()
	{
		assertTrue("Need some samples", testStore.getBulletinCount() > 0);
		assertTrue("Shouldn't be empty", testFolder.getBulletinCount() >= 2);
		testFolder.removeAll();
		assertEquals(0, testFolder.getBulletinCount());
	}

	public void testFind() throws Exception
	{
		BulletinFolder folder = testStore.createFolder("blah2");
		Bulletin b = testStore.createEmptyBulletin();
		assertEquals(-1, folder.find(b.getUniversalId()));

		createEmptyBulletins(folder, 3);
		b = folder.getBulletinSorted(2);
		assertNotNull("Can't find added bulletin", b);
		assertEquals(2, folder.find(b.getUniversalId()));
	}

	public void testSorting() throws Exception
	{
		BulletinFolder folder = testStore.createFolder("blah3");

		assertEquals("eventdate", folder.sortedBy());

		Bulletin b = testStore.createEmptyBulletin();
		b.set("eventdate","20010101");
		b.set("author","billy bob");
		testStore.saveBulletin(b);
		folder.add(b);
		b = testStore.createEmptyBulletin();
		b.set("eventdate","20010201");
		b.set("author","tom tupa");
		testStore.saveBulletin(b);
		folder.add(b);
		b = testStore.createEmptyBulletin();
		b.set("eventdate","20010301");
		b.set("author","adam ant");
		testStore.saveBulletin(b);
		folder.add(b);
		b = testStore.createEmptyBulletin();
		b.set("eventdate","20010401");
		b.set("author","nervous nellie");
		testStore.saveBulletin(b);
		folder.add(b);
		assertEquals("initial count", 4, folder.getBulletinCount());
		b = folder.getBulletinSorted(0);
		assertEquals("20010101", b.get("eventdate"));

		// sort descending
		folder.sortBy("eventdate");
		assertEquals("reverse count", 4, folder.getBulletinCount());
		assertEquals("eventdate", folder.sortedBy());
		assertEquals("Not Decending?", folder.DESCENDING, folder.getSortDirection());
		b = folder.getBulletinSorted(0);
		assertEquals("20010401", b.get("eventdate"));

		// and back to ascending
		folder.sortBy("eventdate");
		assertEquals("new field count", 4, folder.getBulletinCount());
		assertEquals("eventdate", folder.sortedBy());
		assertEquals("Not Assending?", folder.ASCENDING, folder.getSortDirection());
		b = folder.getBulletinSorted(0);
		assertEquals("20010101", b.get("eventdate"));

		// sort by other field
		folder.sortBy("author");
		assertEquals("new field count", 4, folder.getBulletinCount());
		assertEquals("author", folder.sortedBy());
		b = folder.getBulletinSorted(0);
		assertEquals("adam ant", b.get("author"));
		// and descending
		folder.sortBy("author");
		assertEquals("second reverse count", 4, folder.getBulletinCount());
		assertEquals("author", folder.sortedBy());
		b = folder.getBulletinSorted(0);
		assertEquals("tom tupa", b.get("author"));

		// add while in descending mode
		b = testStore.createEmptyBulletin();
		b.set("eventdate","20010401");
		b.set("author","zippy zorro");
		testStore.saveBulletin(b);
		folder.add(b);
		b = folder.getBulletinSorted(0);
		assertEquals("zippy zorro", b.get("author"));
	}

	void createEmptyBulletins(BulletinFolder folder, int count) throws Exception
	{
		ClientBulletinStore store = folder.getStore();
		for(int i = 0; i < count; ++i)
		{
			Bulletin b = store.createEmptyBulletin();
			store.saveBulletin(b);
			folder.add(b);
		}
	}

	static ClientBulletinStore testStore;
	static BulletinFolder testFolder;
	static Bulletin testBulletin1;
	static Bulletin testBulletin2;
}
