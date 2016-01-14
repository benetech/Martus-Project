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

import java.util.Date;

import org.martus.util.MultiCalendar;
import org.martus.util.TestCaseEnhanced;


public class TestMartusFlexidate extends TestCaseEnhanced
{

	public TestMartusFlexidate(String name)
	{
		super(name);
	}

	public void testToStoredDateFormat()
	{
		final int APRIL = 4;
		MultiCalendar cal = new MultiCalendar();

		cal.setGregorian(2005, APRIL, 7);
		MultiCalendar goodDate = cal;
		assertEquals("2005-04-07", MartusFlexidate.toStoredDateFormat(goodDate));

		Date epoch = new Date(0);
		cal.setTime(epoch);
		assertEquals("1970-01-01", MartusFlexidate.toStoredDateFormat(cal));
		
		Date beforeEpochDate = new Date(-1234567890);
		cal.setTime(beforeEpochDate);
		assertEquals("1970-01-01", MartusFlexidate.toStoredDateFormat(cal));

		cal.setGregorian(2548, APRIL, 3);
		MultiCalendar thaiDate = cal;
		assertEquals("2548-04-03", MartusFlexidate.toStoredDateFormat(thaiDate));

		cal.setGregorian(9998, 18, 40);
		MultiCalendar wayFutureDate = cal;
		assertEquals("9999-07-10", MartusFlexidate.toStoredDateFormat(wayFutureDate));

		cal.setGregorian(8, APRIL, 3);
		MultiCalendar ancientDate = cal;
		assertEquals("0008-04-03", MartusFlexidate.toStoredDateFormat(ancientDate));

	}
	
	public void testRangesAndDaylightSavings() throws Exception
	{
		final int MAR = 3;
		final int APR = 4;
		final int MAY = 5;
		MultiCalendar marDate = MultiCalendar.createFromGregorianYearMonthDay(2005, MAR, 29);
		MultiCalendar aprDate1 = MultiCalendar.createFromGregorianYearMonthDay(2005, APR, 1);
		MultiCalendar aprDate2 = MultiCalendar.createFromGregorianYearMonthDay(2005, APR, 5);
		MultiCalendar mayDate = MultiCalendar.createFromGregorianYearMonthDay(2005, MAY, 3);
		String marToApr1String = MartusFlexidate.toBulletinFlexidateFormat(marDate, aprDate1);
		assertEquals("2005-03-29,20050329+3", marToApr1String);
		String marToApr2String = MartusFlexidate.toBulletinFlexidateFormat(marDate, aprDate2);
		assertEquals("2005-03-29,20050329+7", marToApr2String);
		String apr2ToMayString = MartusFlexidate.toBulletinFlexidateFormat(aprDate2, mayDate);
		assertEquals("2005-04-05,20050405+28", apr2ToMayString);
		String marToMayString = MartusFlexidate.toBulletinFlexidateFormat(marDate, mayDate);
		assertEquals("2005-03-29,20050329+35", marToMayString);
	}
	
	public void testDateWithRangeConstructor()
	{
		final int RANGE = 3;
		MultiCalendar cal = MultiCalendar.createFromGregorianYearMonthDay(2000, 7, 25);
		MartusFlexidate mfd = new MartusFlexidate(cal, RANGE);
		MultiCalendar expectedEnd = new MultiCalendar(cal);
		expectedEnd.addDays(RANGE);
		assertEquals("wrong begin date?", cal, mfd.getBeginDate());
		assertEquals("wrong range?", RANGE, mfd.getRange());
		assertEquals("wrong end date?", expectedEnd, mfd.getEndDate());
		assertTrue("hasDateRange failed?", mfd.hasDateRange());
	}
	
	public void testUnknownBeginDate()
	{
		MultiCalendar unknown = MultiCalendar.UNKNOWN;
		MultiCalendar normal = MultiCalendar.createFromGregorianYearMonthDay(2000, 7, 25);
		MartusFlexidate mfd = new MartusFlexidate(unknown, normal);
		assertEquals("wrong begin?", unknown, mfd.getBeginDate());
		assertEquals("wrong range?", MultiCalendar.daysBetween(unknown, normal), mfd.getRange());
		assertEquals("wrong end?", normal, mfd.getEndDate());
		assertTrue("hasDateRange failed?", mfd.hasDateRange());
	}
	
	public void testUnknownEndDate()
	{
		MultiCalendar unknown = MultiCalendar.UNKNOWN;
		MultiCalendar normal = MultiCalendar.createFromGregorianYearMonthDay(2000, 7, 25);
		MartusFlexidate mfd = new MartusFlexidate(normal, unknown);
		assertEquals("from date/date wrong begin?", normal, mfd.getBeginDate());
		assertEquals("from date/date wrong range?", 999999, mfd.getRange());
		assertEquals("from date/date wrong end?", unknown, mfd.getEndDate());
		assertTrue("from date/date hasDateRange failed?", mfd.hasDateRange());
		
		MartusFlexidate mfd2 = new MartusFlexidate(mfd.getBeginDate(), mfd.getRange());
		assertEquals("from date/range wrong begin?", normal, mfd2.getBeginDate());
		assertEquals("from date/range wrong range?", 999999, mfd2.getRange());
		assertEquals("from date/range wrong end?", unknown, mfd2.getEndDate());
		assertTrue("from date/range hasDateRange failed?", mfd2.hasDateRange());
	}
	
	public void testNonRange()
	{
		MultiCalendar normal = MultiCalendar.createFromGregorianYearMonthDay(2000, 7, 25);
		MartusFlexidate mfd = new MartusFlexidate(normal, normal);
		assertEquals("wrong begin?", normal, mfd.getBeginDate());
		assertEquals("wrong range?", 0, mfd.getRange());
		assertEquals("wrong end?", normal, mfd.getEndDate());
		assertFalse("hasDateRange failed?", mfd.hasDateRange());
	}
	
	public void testFlexiDate()
	{
		MartusFlexidate mf = new MartusFlexidate("2003-01-05", 2);	
		assertEquals("20030105+2", mf.getMartusFlexidateString());
		
		assertEquals("2003-01-05", mf.getBeginDate().toIsoDateString());
		assertEquals("2003-01-07", mf.getEndDate().toIsoDateString());																
	}
		
	public void testFlexiDateOverMonths()
	{
		MartusFlexidate mf = new MartusFlexidate("2003-01-05", 120);		
		assertEquals("20030105+120", mf.getMartusFlexidateString());

		assertEquals("2003-01-05", mf.getBeginDate().toIsoDateString());
		assertEquals("2003-05-05", mf.getEndDate().toIsoDateString());
	
	}
	
	public void testFlexiDateOverYear()
	{
		MartusFlexidate mf = new MartusFlexidate("2002-01-05", 366);		
		assertEquals("20020105+366", mf.getMartusFlexidateString());

		assertEquals("2002-01-05", mf.getBeginDate().toIsoDateString());
		assertEquals("2003-01-06", mf.getEndDate().toIsoDateString());		
	}
	
	
	public void testExactDate()
	{
		MartusFlexidate mf = new MartusFlexidate("2003-01-05", 0);
		
		assertEquals("20030105+0", mf.getMartusFlexidateString());
		
		assertEquals("2003-01-05", mf.getBeginDate().toIsoDateString());
		assertEquals("2003-01-05", mf.getEndDate().toIsoDateString());			
	}	
	
	public void testDateRange()
	{
		MultiCalendar beginDate = getDate(2000,1,10);
		MultiCalendar endDate = getDate(2000,1, 15);
						
		MartusFlexidate mf = new MartusFlexidate(beginDate, endDate);
		
		assertEquals("20000110+5", mf.getMartusFlexidateString());	
		
		assertEquals("2000-01-10", mf.getBeginDate().toIsoDateString());
		assertEquals("2000-01-15", mf.getEndDate().toIsoDateString());			
	}
	
	public void testSameDateRange()
	{
		MultiCalendar beginDate = getDate(2000,1,10);
		MultiCalendar endDate = getDate(2000,1, 10);
		
		MartusFlexidate mf = new MartusFlexidate(beginDate, endDate);

		assertEquals("20000110+0", mf.getMartusFlexidateString());	

		assertEquals("2000-01-10", mf.getBeginDate().toIsoDateString());
		assertEquals("2000-01-10", mf.getEndDate().toIsoDateString());
		
		mf = new MartusFlexidate("2003-01-05", 0);		
		assertEquals("20030105+0", mf.getMartusFlexidateString());

		assertEquals("2003-01-05", mf.getBeginDate().toIsoDateString());
		assertEquals("2003-01-05", mf.getEndDate().toIsoDateString());			
	}
	
	public void testDateRangeSwap()
	{
		MultiCalendar beginDate = getDate(2000, 1, 10);
		MultiCalendar endDate = new MultiCalendar();
		endDate.setTime(new Date(beginDate.getTime().getTime() - (360L*24*60*60*1000)));
					
		MartusFlexidate mf = new MartusFlexidate(beginDate, endDate);
	
		assertEquals("Initial date incorrect", "19990115+360", mf.getMartusFlexidateString());	
	
		assertEquals("1999-01-15", mf.getBeginDate().toIsoDateString());
		assertEquals("2000-01-10", mf.getEndDate().toIsoDateString());
	}
	
	public void testCreateMartusDateStringFromDateRange()
	{
		assertNull(MartusFlexidate.createMartusDateStringFromBeginAndEndDateString("invalidDate"));
		String standardDateRange = "1988-02-01,1988-02-05";
		assertEquals("1988-02-01,19880201+4", MartusFlexidate.createMartusDateStringFromBeginAndEndDateString(standardDateRange));

		String reversedDateRange = "1988-02-05,1988-02-01";
		assertEquals("1988-02-01,19880201+4", MartusFlexidate.createMartusDateStringFromBeginAndEndDateString(reversedDateRange));

		String noDateRange = "1988-02-05,1988-02-05";
		assertEquals("1988-02-05,19880205+0", MartusFlexidate.createMartusDateStringFromBeginAndEndDateString(noDateRange));
	}

	private MultiCalendar getDate(int year, int month, int day)
	{			
		MultiCalendar cal = MultiCalendar.createFromGregorianYearMonthDay(year, month, day);
		return cal;
	} 
}
