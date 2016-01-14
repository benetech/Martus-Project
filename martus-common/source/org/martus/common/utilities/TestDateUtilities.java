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

package org.martus.common.utilities;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.martus.util.TestCaseEnhanced;

public class TestDateUtilities extends TestCaseEnhanced
{

	public TestDateUtilities(String name)
	{
		super(name);
	}

	public void testFormatAndParseIso() throws Exception
	{
		int year = 2014;
		int JULY = 6;
		int month = JULY;
		int day = 16;
		int hourOfDay = 10;
		int minute = 19;
		int second = 53;
		int millis = 263;

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.set(year, month, day, hourOfDay, minute, second);
		cal.set(Calendar.MILLISECOND, millis);
		Date dateTime = cal.getTime();
		String isoDateTime = DateUtilities.formatIsoDateTime(dateTime);
		assertEquals("2014-07-16T10:19:53.263Z", isoDateTime);
		assertEquals(dateTime, DateUtilities.parseIsoDateTime(isoDateTime));
	}
	
}
