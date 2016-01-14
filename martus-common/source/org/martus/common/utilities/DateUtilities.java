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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.martus.common.MiniLocalization;
import org.martus.util.MultiCalendar;


public class DateUtilities
{
	public static String getStartDateRange(String storedFlexidateString, MiniLocalization localization)
	{
		MartusFlexidate mfd = localization.createFlexidateFromStoredData(storedFlexidateString);
		return MartusFlexidate.toStoredDateFormat(mfd.getBeginDate());
	}


	public static String getEndDateRange(String storedFlexidateString, MiniLocalization localization)
	{
		MartusFlexidate mfd = localization.createFlexidateFromStoredData(storedFlexidateString);
		if (!mfd.hasDateRange())
			return "";
		return MartusFlexidate.toStoredDateFormat(mfd.getEndDate());
	}

	public static String getTodayInStoredFormat()
	{
		GregorianCalendar realTodayInOurTimeZone = new GregorianCalendar();
		MultiCalendar mc = new MultiCalendar(realTodayInOurTimeZone);
		String result = MartusFlexidate.toStoredDateFormat(mc);
		return result;
	}
	
	public static String formatIsoDateTime(Date dateTime) throws Exception
	{
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		String isoDateTime = df.format(dateTime);
		return isoDateTime;
	}
	
	public static Date parseIsoDateTime(String isoDateTime) throws Exception
	{
		if(isoDateTime.length() == 0)
			return new Date(0L);
					
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = df.parse(isoDateTime);
		return date;
	}
}
