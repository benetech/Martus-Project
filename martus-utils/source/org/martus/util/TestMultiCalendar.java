/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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

To the extent this copyrighted software code is used in the 
Miradi project, it is subject to a royalty-free license to 
members of the Conservation Measures Partnership when 
used with the Miradi software as specified in the agreement 
between Benetech and WCS dated 5/1/05.
*/
package org.martus.util;

import java.util.GregorianCalendar;


public class TestMultiCalendar extends TestCaseEnhanced
{
	public TestMultiCalendar(String name)
	{
		super(name);
	}

	public void testEquals()
	{
		MultiCalendar a = new MultiCalendar();
		MultiCalendar b = new MultiCalendar();
		assertEquals("not identical?", a, b);
		assertEquals("hash not the same?", a.hashCode(), b.hashCode());
		
		MultiCalendar c = new MultiCalendar(a);
		c.addDays(1000);
		assertNotEquals("not different?", a, c);
		// all MultiCalendar hashCodes are identical, due to
		// policy inside Java's Calendar.hashCode method
		
		assertNotEquals("didn't check type?", a, new Object());
	}

	public void testTodaysDate()
	{
		MultiCalendar multiCalendarToday = new MultiCalendar();
		GregorianCalendar gregorianCalendarToday = new GregorianCalendar();
		assertEquals("Not the same year?", gregorianCalendarToday.get(GregorianCalendar.YEAR), multiCalendarToday.getGregorianYear());
		assertEquals("Not the same month?", getDisplayableMonthCount(gregorianCalendarToday), multiCalendarToday.getGregorianMonth());
		assertEquals("Not the same day?", gregorianCalendarToday.get(GregorianCalendar.DAY_OF_MONTH), multiCalendarToday.getGregorianDay());
		//TODO Figure out why the time is 5 PM when we set it to 12 PM.  Some issue with Java's Calendar when setting to UTC.
		//FIXME DayLight Saving issue? assertEquals("Not 5 O'clock PM?", 5, multiCalendarToday.getTime().getHours());
		assertEquals("Not 0 minutes?", 0, multiCalendarToday.getTime().getMinutes());
		assertEquals("Not 0 seconds?", 0, multiCalendarToday.getTime().getSeconds());
	}

	private int getDisplayableMonthCount(GregorianCalendar gregorianCalendarToday) 
	{
		return gregorianCalendarToday.get(GregorianCalendar.MONTH) + 1;
	}	
}
