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

import org.martus.amplifier.common.AmplifierLocalization;
import org.martus.amplifier.main.EventDatesIndexedList;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.util.MultiCalendar;
import org.martus.util.TestCaseEnhanced;


public class TestEventDatesIndexedList extends TestCaseEnhanced
{
	public TestEventDatesIndexedList(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		file = createTempFile();
		MartusAmplifier.localization = new AmplifierLocalization();
	}
	
	public void tearDown()
	{
		file.delete();
	}

	public void testGetEarliestYear() throws Exception
	{
		EventDatesIndexedList list = new EventDatesIndexedList(file);
		list.loadFromFile();
		
		int thisYear = new MultiCalendar().getGregorianYear();
		
		assertEquals("wrong empty earliest?", thisYear, list.getEarliestYear());
		assertEquals("wrong empty latest?", thisYear, list.getLatestYear());

		list.addValue("0001-01-01");
		
		assertEquals("unknown is earlier?", thisYear, list.getEarliestYear());
		assertEquals("unknown is later?", thisYear, list.getLatestYear());
		
		list.addValue("1990-01-01");
		list.addValue("1999-12-31");
		
		assertEquals("wrong earliest normal?", 1990, list.getEarliestYear());
		assertEquals("wrong latest normal?", 1999, list.getLatestYear());

		list.addValue("1989-12-01,19891201+300");
		list.addValue("1999-12-01,19991201+300");
		
		assertEquals("wrong earliest flexidate?", 1989, list.getEarliestYear());
		assertEquals("wrong latest flexidate?", 2000, list.getLatestYear());
	}
	
	File file;
}
