/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import com.ghasemkiani.util.icu.PersianCalendar;

public class MultiCalendar
{
	public static MultiCalendar createFromGregorianYearMonthDay(int year, int month, int day)
	{
		int JANUARY = 1;
		int DECEMBER = 12;
		if(year < 0 || month < JANUARY || month > DECEMBER || day < 1 || day > 31)
			throw new RuntimeException("invalid date: " + year + "-" + month + "-" + day);

		MultiCalendar cal = new MultiCalendar();
		cal.setGregorian(year, month, day);
		return cal;
	}

	public static MultiCalendar createFromIsoDateString(String storedDateString)
	{
		int year = getYearFromIso(storedDateString);
		int month = getMonthFromIso(storedDateString);
		int day = getDayFromIso(storedDateString);
		
		return createFromGregorianYearMonthDay(year, month, day);
	}

	public static int getYearFromIso(String storedDateString)
	{
		int yearStart = 0;
		int yearEnd = yearStart + 4;
		return Integer.parseInt(storedDateString.substring(yearStart, yearEnd));
	}

	public static int getMonthFromIso(String storedDateString)
	{
		int monthStart = 5;
		int monthEnd = monthStart + 2;
		return Integer.parseInt(storedDateString.substring(monthStart, monthEnd));
	}

	public static int getDayFromIso(String storedDateString)
	{
		int dayStart = 8;
		int dayEnd = dayStart + 2;
		return Integer.parseInt(storedDateString.substring(dayStart, dayEnd));
	}

	public MultiCalendar()
	{
		set(createGregorianCalendarToday());
	}
	
	public MultiCalendar(GregorianCalendar copyFrom)
	{
		set(copyFrom);
	}

	public MultiCalendar(MultiCalendar copyFrom)
	{
		set(copyFrom.getGregorianCalendar());
	}
	
	public MultiCalendar(Date date)
	{
		setTime(date);
	}
	
	public int getGregorianYear()
	{
		return getGregorianCalendar().get(Calendar.YEAR);
	}
	
	public int getGregorianMonth()
	{
		return getCalendarMonthNumber(getGregorianCalendar());
	}

	private int getCalendarMonthNumber(GregorianCalendar calendar) 
	{
		return calendar.get(Calendar.MONTH) + 1;
	}
	
	public int getGregorianDay()
	{
		return getGregorianCalendar().get(Calendar.DAY_OF_MONTH);
	}
	
	public void setGregorian(int year, int month, int day)
	{
		gregorianYear = year;
		gregorianMonth = month;
		gregorianDay = day;
	}
	
	public void addDays(int value)
	{
		GregorianCalendar cal = getGregorianCalendar();
		cal.add(Calendar.DAY_OF_MONTH, value);
		set(cal);
	}
	
	public static int daysBetween(MultiCalendar first, MultiCalendar second)
	{
		long millisApart = second.getTime().getTime() - first.getTime().getTime();
		long millisInOneDay = 1000L * 60 * 60 * 24;
		return (int)(millisApart / millisInOneDay);
	}
	
	public boolean before(MultiCalendar other)
	{
		return getGregorianCalendar().before(other.getGregorianCalendar());
	}
	
	public boolean after(MultiCalendar other)
	{
		return getGregorianCalendar().after(other.getGregorianCalendar());
	}
	
	public boolean isDefinitelyAfter(MultiCalendar other)
	{
		if(isUnknown() || other.isUnknown())
			return false;
		return after(other);
	}
	
	public Date getTime()
	{
		return getGregorianCalendar().getTime();
	}
	
	public void setTime(Date newTime)
	{
		if(newTime.getTime() < 0)
			newTime = new Date(0);
		GregorianCalendar cal = createGregorianCalendarToday();
		cal.setTime(newTime);
		set(cal);
	}
	
	public boolean equals(Object rawOther)
	{
		if(! (rawOther instanceof MultiCalendar))
			return false;
		
		MultiCalendar other = (MultiCalendar)rawOther;
		return other.getGregorianCalendar().equals(getGregorianCalendar());
	}
	
	public int hashCode()
	{
		return getGregorianCalendar().hashCode();
	}
	
	public String toString()
	{
		return toIsoDateString();
	}

	public GregorianCalendar getGregorianCalendar()
	{
		return createGregorianCalendar(gregorianYear, gregorianMonth, gregorianDay);
	}
	
	public String toIsoDateString()
	{
		MultiDateFormat format = new MultiDateFormat(new DatePreference("ymd", '-'));
		return format.formatIgnoringRightToLeft(getGregorianYear(), getGregorianMonth(), getGregorianDay());
	}

	private static GregorianCalendar createGregorianCalendar(int year, int month, int day)
	{
		GregorianCalendar cal = createGregorianCalendarToday();
		cal.set(year, month - 1, day);
		return cal;
	}
	
	private static GregorianCalendar createGregorianCalendarToday()
	{
		GregorianCalendar cal = new GregorianCalendar(new SimpleTimeZone(UTC_OFFSET, "martus"));
		cal.set(Calendar.AM_PM, Calendar.PM);
		cal.set(Calendar.HOUR_OF_DAY, 12);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	private void set(GregorianCalendar copyFrom)
	{
		setGregorian(copyFrom.get(Calendar.YEAR), getCalendarMonthNumber(copyFrom), copyFrom.get(Calendar.DAY_OF_MONTH));
	}
	
	public static MultiCalendar createCalendarFromPersianYearMonthDay(int year, int month, int day)
	{
		PersianCalendar pc = new PersianCalendar(year, month - 1, day, 12, 0, 0);
		return new MultiCalendar(pc.getTime());
	}
	
	public boolean isUnknown()
	{
		return (getGregorianYear() == YEAR_NOT_SPECIFIED);
	}

	public static final int UTC_OFFSET = 0;
	
	int gregorianYear;
	int gregorianMonth;
	int gregorianDay;

	public static int YEAR_NOT_SPECIFIED = 1;
	public static final int THAI_YEAR_OFFSET = 543;
	
	public static final MultiCalendar UNKNOWN = createFromGregorianYearMonthDay(MultiCalendar.YEAR_NOT_SPECIFIED, 1, 1);

}
