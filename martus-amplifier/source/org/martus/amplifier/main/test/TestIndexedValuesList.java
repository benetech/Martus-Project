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

package org.martus.amplifier.main.test;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.martus.amplifier.main.IndexedValuesList;
import org.martus.common.MartusUtilities;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;


public class TestIndexedValuesList extends TestCaseEnhanced
{
	public TestIndexedValuesList(String name)
	{
		super(name);
	}
	
	public void testBasics() throws Exception
	{
		File baseDir = createTempDirectory();
		File listFile = new File(baseDir, "langList.txt");
		listFile.deleteOnExit();
		IndexedValuesList list = new IndexedValuesList(listFile);
		assertFalse("file should not exist", listFile.exists());
		assertNotNull("list is null?", list.getIndexedValues());
		assertEquals("list not empty?", 0, list.getIndexedValues().size());
		
		try
		{
			list.loadFromFile();
			fail("Should have thrown since file didn't exist");
		}
		catch (IOException expected)
		{
		}
		
		assertEquals("list should still be empty", 0, list.getIndexedValues().size());
		list.saveToFile();
		
		Vector listFromFile = MartusUtilities.loadListFromFile(listFile);
		
		assertEquals("Saved copy should be empty", 0, listFromFile.size());
		DirectoryUtils.deleteEntireDirectoryTree(baseDir);
	}

	public void testAdd() throws Exception
	{
		File baseDir = createTempDirectory();
		File listFile = new File(baseDir, "langList.txt");
		listFile.deleteOnExit();
		IndexedValuesList list = new IndexedValuesList(listFile);
		try
		{
			list.loadFromFile();
			fail("Should have thrown since file didn't exist");
		}
		catch (IOException expected)
		{
		}

		Vector updatedList = list.getIndexedValues();
		assertFalse("List should not contain en", updatedList.contains("en"));		

		list.addValue("en");
		updatedList = list.getIndexedValues();
		assertEquals("List should now 1 entry", 1, updatedList.size());		
		assertTrue("List should now contain en", updatedList.contains("en"));		

		list.addValue("en");
		updatedList = list.getIndexedValues();
		assertEquals("List should still have 1 entry", 1, updatedList.size());		

		list.addValue("fr");
		updatedList = list.getIndexedValues();
		assertEquals("List should have 2 entries", 2, updatedList.size());
		
		IndexedValuesList list2 = new IndexedValuesList(listFile);
		try
		{
			list2.loadFromFile();
		}
		catch (IOException unexpected)
		{
			fail("Should not have thrown since file should exist");
		}

		updatedList = list2.getIndexedValues();
		assertEquals("List2 should have 2 entries", 2, updatedList.size());
		assertTrue("en should be in the list", updatedList.contains("en"));		
		assertTrue("fr should be in the list", updatedList.contains("fr"));		

		listFile.delete();
		try
		{
			list2.addValue("gr");
		}
		catch (IOException unexpected)
		{
			fail("File deleted a new file should be created");
		}

		IndexedValuesList list3 = new IndexedValuesList(listFile);
		try
		{
			list3.loadFromFile();
		}
		catch (IOException unexpected)
		{
			fail("Should not have thrown since a new file should have been created");
		}

		updatedList = list3.getIndexedValues();
		assertEquals("This list should be 3 in size", 3, updatedList.size());
		
		IndexedValuesList addDirectlyToNewEmptyList = new IndexedValuesList(listFile);

		addDirectlyToNewEmptyList.addValue("it");
		updatedList = addDirectlyToNewEmptyList.getIndexedValues();
		assertEquals("should contain just it", 1, updatedList.size());
		assertTrue("it should be in the list", updatedList.contains("it"));		

		IndexedValuesList nullList = new IndexedValuesList(null);
		try
		{
			nullList.addValue("it");
			fail("Null File no file could be created");
		}
		catch (Exception expected)
		{
		}

		DirectoryUtils.deleteEntireDirectoryTree(baseDir);
	}

	

}
