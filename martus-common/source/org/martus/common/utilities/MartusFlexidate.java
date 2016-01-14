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

import org.martus.util.MultiCalendar;
import org.martus.util.MultiDateFormat;

public class MartusFlexidate
{
	public MartusFlexidate(MultiCalendar beginDate, MultiCalendar endDate)
	{
		if(beginDate.isDefinitelyAfter(endDate))
		{
			MultiCalendar temp = beginDate;
			beginDate = endDate;
			endDate = temp;
		}
		begin = new MultiCalendar(beginDate);
		end = new MultiCalendar(endDate);
	}
		
	/*
	 * NOTE: Only call this method if you already know that isoBeginDate is valid.
	 * Normally, you should use MiniLocalization to convert ISO to Flexidate
	 */
	public MartusFlexidate(String isoBeginDate, int range)
	{
		this(MultiCalendar.createFromIsoDateString(isoBeginDate), range);
	}
	
	public MartusFlexidate(MultiCalendar beginDate, int range)
	{
		begin = new MultiCalendar(beginDate);
		if(range == UNKNOWN_END_RANGE)
		{
			end = MultiCalendar.UNKNOWN;
		}
		else
		{
			end = new MultiCalendar(begin);
			end.addDays(range);
		}
	}
	
	/* this will convert a string in in one of these forms:
	 * 1989-12-01,1989-12-15
	 * 1989-12-15,1989-12-01
	 * and return it as a MartusFlexidate in the form 1989-12-01,19891201+15
 	 */
	public static String createMartusDateStringFromBeginAndEndDateString(String dateRange)
	{
		int comma = dateRange.indexOf(DATE_RANGE_SEPARATER);
		if (comma == -1)
			return null;
		String beginDate = dateRange.substring(0,comma);
		String endDate = dateRange.substring(comma+1);
		MultiCalendar calBeginDate = MultiCalendar.createFromIsoDateString(beginDate);
		MultiCalendar calEndDate = MultiCalendar.createFromIsoDateString(endDate);
		MartusFlexidate flexidate = new MartusFlexidate(calBeginDate, calEndDate);
		String startDate = beginDate;
		if(calBeginDate.after(calEndDate))
			startDate = endDate;
		return startDate+DATE_RANGE_SEPARATER+flexidate.getMartusFlexidateString();
	
	}
	
	
	
	public static String extractIsoDateFromStoredDate(String storedDate)
	{
		String internalFlexidateString = MartusFlexidate.extractInternalFlexidateFromStoredDate(storedDate);
		String year = internalFlexidateString.substring(0, 4);
		String month = internalFlexidateString.substring(4, 6);
		String day = internalFlexidateString.substring(6, 8);
		return year + "-" + month + "-" + day;
	}

	public static boolean isFlexidateString(String dateStr)
	{
		return dateStr.indexOf(DATE_RANGE_SEPARATER) >= 0;
	}

	public static int extractRangeFromStoredDate(String storedDate)
	{
		String internalFlexidateString = MartusFlexidate.extractInternalFlexidateFromStoredDate(storedDate);
		int plusAt = internalFlexidateString.indexOf(FLEXIDATE_RANGE_DELIMITER);
		if (plusAt < 0)
			return 0;
		
		String rangeStr = internalFlexidateString.substring(plusAt+1);
		return Integer.parseInt(rangeStr);			
	}

	public static String toBulletinFlexidateFormat(MultiCalendar beginDate, MultiCalendar endDate)
	{
		return beginDate.toIsoDateString() + 
					DATE_RANGE_SEPARATER +
					toFlexidateFormat(beginDate, endDate);
	}

	public String getMartusFlexidateString() 
	{
		int year = begin.getGregorianYear();
		int month = begin.getGregorianMonth();
		int day = begin.getGregorianDay();
		String basePart = MultiDateFormat.format("ymd", "", year, month, day);
		return basePart + FLEXIDATE_RANGE_DELIMITER + Integer.toString(getRange());
	}
	
	public MultiCalendar getBeginDate()
	{
		return begin;
	}
	
	public MultiCalendar getEndDate()
	{
		return end;
	}
	
	public int getRange()
	{
		if(end.isUnknown())
			return UNKNOWN_END_RANGE;
		return MultiCalendar.daysBetween(begin, end);
	}
	
	public boolean hasDateRange()
	{
		return !(begin.equals(end));
	}

	public static String toStoredDateFormat(MultiCalendar date)
	{		
		return date.toIsoDateString();				
	}

	public static String toFlexidateFormat(MultiCalendar beginDate, MultiCalendar endDate)
	{		
		return new MartusFlexidate(beginDate, endDate).getMartusFlexidateString();
	}		
		
	private static String extractInternalFlexidateFromStoredDate(String dateStr)
	{
		return dateStr.substring(dateStr.indexOf(DATE_RANGE_SEPARATER)+1);
	}
	
	private static final int UNKNOWN_END_RANGE = 999999;

	MultiCalendar begin;
	MultiCalendar end;
	public static final String 	FLEXIDATE_RANGE_DELIMITER = "+";	
	public static final String	DATE_RANGE_SEPARATER = ",";
}
