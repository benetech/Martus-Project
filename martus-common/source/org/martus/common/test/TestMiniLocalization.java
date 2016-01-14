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

*/

package org.martus.common.test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.martus.common.DefaultLanguageSettingsProvider;
import org.martus.common.LanguageSettingsProvider;
import org.martus.common.MiniLocalization;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.DatePreference;
import org.martus.util.MultiCalendar;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.language.LanguageOptions;


public class TestMiniLocalization extends TestCaseEnhanced
{
	public TestMiniLocalization(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		localization = new MiniLocalization();
		localization.setLanguageSettingsProvider(new MutableLanguageSettingsProvider());
	}
	
	public void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testCurrentCalendarSystem() throws Exception
	{
		assertEquals("Didn't default to gregorian?", "Gregorian", localization.getCurrentCalendarSystem());
		
		localization.setCurrentCalendarSystem("Thai");
		assertEquals("Didn't set to Thai?", "Thai", localization.getCurrentCalendarSystem());
		
		localization.setCurrentCalendarSystem("Persian");
		assertEquals("Didn't set to Persian?", "Persian", localization.getCurrentCalendarSystem());
		
		try
		{
			localization.setCurrentCalendarSystem("oiwefjoiwef");
			fail("Should throw for unrecognized calendar system");
		} 
		catch (RuntimeException ignoreExpected)
		{
		}
	}
	
	public void testGetLocalizedDateFields() throws Exception
	{
		int year = 2005;
		int month = 10;
		int day = 20;
		MultiCalendar cal = MultiCalendar.createFromGregorianYearMonthDay(year, month, day);
		
		verifyGetLocalizedFields(MiniLocalization.GREGORIAN_SYSTEM, cal, year, month, day);
		verifyGetLocalizedFields(MiniLocalization.THAI_SYSTEM, cal, year + 543, month, day);
		verifyGetLocalizedFields(MiniLocalization.PERSIAN_SYSTEM, cal, 1384, 7, 28);
		
	}

	private void verifyGetLocalizedFields(String system, MultiCalendar cal, int expectedYear, int expectedMonth, int expectedDay)
	{
		localization.setCurrentCalendarSystem(system);
		assertEquals(system + " year wrong?", expectedYear, localization.getLocalizedYear(cal));
		assertEquals(system + " month wrong?", expectedMonth, localization.getLocalizedMonth(cal));
		assertEquals(system + "day wrong", expectedDay, localization.getLocalizedDay(cal));
	}

	public void testCreateCalendarFromLocalizedYearMonthDay() throws Exception
	{
		int year = 2005;
		int month = 10;
		int day = 20;
		MultiCalendar reference = MultiCalendar.createFromGregorianYearMonthDay(year, month, day);
		
		verifyCreateLocalizedCalendar(reference, MiniLocalization.GREGORIAN_SYSTEM, year, month, day);
		verifyCreateLocalizedCalendar(reference, MiniLocalization.THAI_SYSTEM, year + 543, month, day);
		verifyCreateLocalizedCalendar(reference, MiniLocalization.PERSIAN_SYSTEM, 1384, 7, 28);
	}

	private void verifyCreateLocalizedCalendar(MultiCalendar reference, String system, int year, int month, int day)
	{
		localization.setCurrentCalendarSystem(system);
		MultiCalendar cal = localization.createCalendarFromLocalizedYearMonthDay(year, month, day);
		assertEquals(system + " Not the same date?", reference, cal);
	}
	
	public void testConvertStoredDateToDisplay()
	{
		verifyConvertStoredToDisplayDate(MiniLocalization.GREGORIAN_SYSTEM, "10/20/2005", "2005-10-20");
		verifyConvertStoredToDisplayDate(MiniLocalization.THAI_SYSTEM, "10/20/2548", "2005-10-20");
		verifyConvertStoredToDisplayDate(MiniLocalization.PERSIAN_SYSTEM, "07/28/1384", "2005-10-20");
		
		LanguageOptions.setDirectionRightToLeft();
		verifyConvertStoredToDisplayDate(MiniLocalization.GREGORIAN_SYSTEM, "2005/20/10", "2005-10-20");
		LanguageOptions.setDirectionLeftToRight();
	}

	private void verifyConvertStoredToDisplayDate(String system, String expectedDate, String isoDate)
	{
		localization.setCurrentCalendarSystem(system);
		assertEquals(expectedDate, localization.convertStoredDateToDisplay(isoDate));
	}


	
	public void testConvertStoredDateToDisplayNoTimeZoneOffset() throws Exception
	{
		TimeZone defaultTimeZone = TimeZone.getDefault();
		try
		{
			verifyConvertForTimeZoneOffsetHourly(0);
		}
		finally
		{
			TimeZone.setDefault(defaultTimeZone);
		}
		
	}
	public void testConvertStoredDateToDisplayWithAllHourlyTimeZones() throws Exception
	{
		TimeZone defaultTimeZone = TimeZone.getDefault();
		try
		{
			verifyConvertForTimeZoneOffsetHourly(0);
			for(int offset = -12; offset < 12; ++offset)
				verifyConvertForTimeZoneOffsetHourly(offset);
		}
		finally
		{
			TimeZone.setDefault(defaultTimeZone);
		}
	}
	
	public void testConvertStoredDateToDisplayWithHalfHourTimeZones() throws Exception
	{
		TimeZone defaultTimeZone = TimeZone.getDefault();
		try
		{
			for(int offset = -24; offset < 24; ++offset)
				verifyConvertForTimeZoneOffsetHalfHour(offset);
		}
		finally
		{
			TimeZone.setDefault(defaultTimeZone);
		}
		
	}

	void verifyConvertForTimeZoneOffsetHourly(int offset)
	{
		MiniLocalization loc = new MiniLocalization();
		TimeZone thisTimeZone = new SimpleTimeZone(offset*1000*60*60, "martus");
		TimeZone.setDefault(thisTimeZone);
		assertEquals("didn't set time zone?", thisTimeZone, new GregorianCalendar().getTimeZone());
    	assertEquals("bad conversion UTC +" + Integer.toString(offset), "12/31/1987", loc.convertStoredDateToDisplay("1987-12-31"));
    	assertEquals("bad conversion before 1970 UTC " + Integer.toString(offset), "12/31/1947", loc.convertStoredDateToDisplay("1947-12-31"));
	}

	void verifyConvertForTimeZoneOffsetHalfHour(int offset)
	{
		MiniLocalization loc = new MiniLocalization();
		TimeZone thisTimeZone = new SimpleTimeZone(offset*1000*60*30, "martus");
		TimeZone.setDefault(thisTimeZone);
		assertEquals("didn't set 1/2 hour time zone?", thisTimeZone, new GregorianCalendar().getTimeZone());
    	assertEquals("bad conversion UTC 1/2 hour +" + Integer.toString(offset), "12/31/1987", loc.convertStoredDateToDisplay("1987-12-31"));
    	assertEquals("bad conversion before 1970 UTC 1/2 hour +" + Integer.toString(offset), "12/31/1947", loc.convertStoredDateToDisplay("1947-12-31"));
	}
	
	public void testFormatDateTime() throws Exception
	{
    	MiniLocalization loc = new MiniLocalization();
    	
    	final int june = 5;
    	GregorianCalendar leadingZeros = new GregorianCalendar(1996, june, 1);
    	leadingZeros.set(Calendar.HOUR_OF_DAY, 7);
    	leadingZeros.set(Calendar.MINUTE, 4);
    	assertEquals("06/01/1996 07:04", loc.formatDateTime(leadingZeros.getTimeInMillis()));
    	
    	final int december = 11;
    	GregorianCalendar afternoon = new GregorianCalendar(2004, december, 9);
    	afternoon.set(Calendar.HOUR_OF_DAY, 13);
    	afternoon.set(Calendar.MINUTE, 59);
    	assertEquals("12/09/2004 13:59", loc.formatDateTime(afternoon.getTimeInMillis()));
	}
    
    public void testFormatDateTimeRightToLeft() throws Exception
	{
    	MiniLocalization loc = new MiniLocalization();
    	loc.setLanguageSettingsProvider(new MutableLanguageSettingsProvider());
    	String rightToLeftLanguageCode = "cc";
    	loc.addRightToLeftLanguage(rightToLeftLanguageCode);
		loc.setCurrentLanguageCode(rightToLeftLanguageCode);
    	final int june = 5;
    	GregorianCalendar leadingZeros = new GregorianCalendar(1996, june, 1);
    	leadingZeros.set(Calendar.HOUR_OF_DAY, 7);
    	leadingZeros.set(Calendar.MINUTE, 4);
    	assertEquals("07:04 1996/01/06", loc.formatDateTime(leadingZeros.getTimeInMillis()));
    	
    	final int december = 11;
    	GregorianCalendar afternoon = new GregorianCalendar(2004, december, 9);
    	afternoon.set(Calendar.HOUR_OF_DAY, 13);
    	afternoon.set(Calendar.MINUTE, 59);
    	assertEquals("13:59 2004/09/12", loc.formatDateTime(afternoon.getTimeInMillis()));
    	LanguageOptions.setDirectionLeftToRight();
	}

    public void testDateUnknown()
    {
    	MiniLocalization loc = new MiniLocalization();
    	assertEquals("Should return '' for an unknown date", "", loc.formatDateTime(MiniLocalization.DATE_UNKNOWN));
    }
    
	public void testGetMdyOrder()
	{
		MiniLocalization loc = new MiniLocalization();
		assertEquals("mdy", loc.getMdyOrder());
	}
	
	public void testGetDateDelimiter()
	{
		MiniLocalization loc = new MiniLocalization();
		assertEquals("mdy", loc.getMdyOrder());
		assertEquals('/', loc.getDateDelimiter());
		TestLanguageSettingsProvider myProvider = new TestLanguageSettingsProvider();
		myProvider.setCurrentDateFormat("dmy.");
		assertEquals("dd.MM.yyyy", myProvider.getCurrentDateFormat());
		loc.setLanguageSettingsProvider(myProvider);
		assertEquals("dmy", loc.getMdyOrder());
		assertEquals('.', loc.getDateDelimiter());
	}
	
	private class TestLanguageSettingsProvider extends DefaultLanguageSettingsProvider
	{
		public TestLanguageSettingsProvider()
		{
			datePref = new DatePreference();
		}

		@Override
		public String getCurrentDateFormat()
		{
			return datePref.getDateTemplate();
		}

		@Override
		public void setCurrentDateFormat(String currentDateFormat)
		{
			try
			{
				datePref.setDateTemplate(currentDateFormat);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		DatePreference datePref;
	}
	
	public void testSetDateFormatFromLanguage()
	{
		MiniLocalization loc = new MiniLocalization();
		loc.setLanguageSettingsProvider(new MutableLanguageSettingsProvider());
		assertEquals("wrong default mdy?", "mdy", loc.getMdyOrder());
		loc.setCurrentLanguageCode(MiniLocalization.RUSSIAN);
		loc.setDateFormatFromLanguage();
		assertEquals("didn't set russian mdy?", "dmy", loc.getMdyOrder());
	}
	
	
	// The following test was moved from TestBulletin on 2006-03-07
	// It might be redundant. At some point, delete this comment!
	public void testStoredDateFormat()
	{
		try
		{
			MultiCalendar cal = MultiCalendar.createFromIsoDateString("2003-07-02");
			assertEquals(2003, cal.getGregorianYear());
			assertEquals(7, cal.getGregorianMonth());
			assertEquals(2, cal.getGregorianDay());
		}
		catch(Exception e)
		{
			assertTrue(false);
		}
	}

	// The following test was moved from TestBulletin on 2006-03-07
	// It might be redundant. At some point, delete this comment!
	public void testDateRangeCompatibility() throws Exception
	{
		String sampleDateRange = "2003-04-07,2003-05-17";
		MultiCalendar cal = MultiCalendar.createFromIsoDateString(sampleDateRange);
		int year = cal.getGregorianYear();
		int month = cal.getGregorianMonth();
		int day = cal.getGregorianDay();
		assertEquals(2003, year);
		assertEquals(4, month);
		assertEquals(7, day);
	}
		
	// The following test was moved from TestFieldSpec on 2006-03-07
	// It might be redundant. At some point, delete this comment!
	public void testDateFormatConversions() throws Exception
	{
		String wayOldDate = "1853-05-21";
		String oldDate = "1931-07-19";
		String recentDate = "1989-09-28";
		String nearFutureDate = "2017-06-28";
		String farFutureDate = "2876-08-16";
		
		verifyRoundTripDateConversion("recent past", recentDate);
		verifyRoundTripDateConversion("near future", nearFutureDate);
		verifyRoundTripDateConversion("after 2020", farFutureDate);
		verifyRoundTripDateConversion("before 1970", oldDate);
		verifyRoundTripDateConversion("before 1900", wayOldDate);
	}
	
	void verifyRoundTripDateConversion(String text, String dateString) throws Exception
	{
		MultiCalendar cal = MultiCalendar.createFromIsoDateString(dateString);
		String result = cal.toIsoDateString();
		assertEquals("date conversion failed: " + text, dateString, result);
	}

	public void testCreateFlexidateFromMartusString()
	{
		MartusFlexidate mfd = localization.createFlexidateFromStoredData("2000-01-10");
		assertEquals("2000-01-10", mfd.getBeginDate().toIsoDateString());	
		
		mfd = localization.createFlexidateFromStoredData("2000-01-10,20000101+0");
		assertEquals("single begin", "2000-01-01", mfd.getBeginDate().toIsoDateString());
		assertEquals("single end", "2000-01-01", mfd.getEndDate().toIsoDateString());
		
		mfd = localization.createFlexidateFromStoredData("2000-01-10,20001203+5");
		assertEquals("range begin","2000-12-03", mfd.getBeginDate().toIsoDateString());
		assertEquals("range end","2000-12-08", mfd.getEndDate().toIsoDateString());						
	}
	
	public void testCreateInvalidDateFromMartusString()
	{
		MartusFlexidate mfd = localization.createFlexidateFromStoredData("185[01-10");
		assertEquals("1900-01-01", mfd.getBeginDate().toIsoDateString());					
	}
	
	public void testLegacyThaiDates()
	{
		assertTrue("Defaulting to adjusting legacy Thai dates?", localization.getAdjustThaiLegacyDates());
		
		String legacyIso = "2548-10-20";
		
		MultiCalendar legacy = localization.createCalendarFromIsoDateString(legacyIso);
		assertEquals("didn't adjust year?", 2005, legacy.getGregorianYear());
	}

	public void testLegacyPersianDates()
	{
		assertTrue("Defaulting to adjusting legacy persian dates?", localization.getAdjustPersianLegacyDates());
		
		String legacyIso = "1384-07-28";
		
		MultiCalendar legacy = localization.createCalendarFromIsoDateString(legacyIso);
		assertEquals("didn't adjust year?", 2005, legacy.getGregorianYear());
		assertEquals("didn't adjust month?", 10, legacy.getGregorianMonth());
		assertEquals("didn't adjust day?", 20, legacy.getGregorianDay());
	}

	class MutableLanguageSettingsProvider implements LanguageSettingsProvider
	{
		
		public MutableLanguageSettingsProvider()
		{
			currentDateFormat = "MM/dd/yyyy";
			calendarSystem = MiniLocalization.GREGORIAN_SYSTEM;
			currentLanguage = "en";
		}

		@Override
		public String getCurrentDateFormat()
		{
			return currentDateFormat;
		}

		@Override
		public void setCurrentDateFormat(String currentDateFormat)
		{
			this.currentDateFormat = currentDateFormat;
			
		}

		@Override
		public String getCurrentLanguage()
		{
			return currentLanguage;
		}

		@Override
		public void setCurrentLanguage(String currentLanguage)
		{
			this.currentLanguage = currentLanguage;
		}

		@Override
		public String getCurrentCalendarSystem()
		{
			return calendarSystem;
		}

		@Override
		public void setCurrentCalendarSystem(String calendarSystem)
		{
			this.calendarSystem = calendarSystem;
		}

		@Override
		public boolean getAdjustThaiLegacyDates()
		{
			return true;
		}

		@Override
		public boolean getAdjustPersianLegacyDates()
		{
			return true;
		}

		@Override
		public void setDateFormatFromLanguage()
		{
			DatePreference preference = MiniLocalization.getDefaultDatePreferenceForLanguage(getCurrentLanguage());
			setCurrentDateFormat(preference.getDateTemplate());
		}
		
		private String calendarSystem;
		private String currentLanguage;
		private String currentDateFormat;		
	}

	MiniLocalization localization;
}
