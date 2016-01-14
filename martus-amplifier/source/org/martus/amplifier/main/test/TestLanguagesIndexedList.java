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

import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.main.LanguagesIndexedList;
import org.martus.util.TestCaseEnhanced;


public class TestLanguagesIndexedList extends TestCaseEnhanced
{
	public TestLanguagesIndexedList(String name)
	{
		super(name);
	}
	
	public void testLanguagesIndexedBasics() throws Exception
	{
		File languageListFile = createTempFile();
		languageListFile.delete();
		
		LanguagesIndexedList list = new LanguagesIndexedList(languageListFile);
		assertEquals("before load or add not empty?", 0, list.getIndexedValues().size());
		
		try
		{
			list.loadFromFile();
			fail("Should have thrown");
		}
		catch(IOException ignoreExpected)
		{
		}
		
		Vector loadedValues = list.getIndexedValues();
		assertEquals("load didn't auto-insert any?", 1, loadedValues.size());
		assertContains("missing 'any language' after load?", SearchResultConstants.LANGUAGE_ANYLANGUAGE_LABEL, loadedValues);
		
		LanguagesIndexedList list2 = new LanguagesIndexedList(languageListFile);
		list2.addValue("xx");
		Vector addedValues = list2.getIndexedValues();
		assertEquals("add didn't auto-insert any?", 2, addedValues.size());
		assertContains("missing 'any language' after add?", SearchResultConstants.LANGUAGE_ANYLANGUAGE_LABEL, addedValues);
		
		languageListFile.delete();
	}

}
