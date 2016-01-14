/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.common.fieldspec;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import org.martus.common.MartusXml;
import org.martus.util.MultiCalendar;
import org.xml.sax.SAXException;

public class AbstractDateOrientedFieldSpec extends FieldSpec
{
	protected AbstractDateOrientedFieldSpec(FieldType typeToUse)
	{
		super(typeToUse);
	}
	
	protected boolean allowUserDefaultValue()
	{
		return false;
	}

	protected String getSystemDefaultValue()
	{
		return "";
	}

	public void setMinimumDate(String newMinimumDate)
	{
		minimumDate = newMinimumDate;
	}
	
	public String getMinimumDate()
	{
		return minimumDate;
	}
	
	public void setMaximumDate(String newMaximumDate)
	{
		maximumDate = newMaximumDate;
	}
	
	public String getMaximumDate()
	{
		return maximumDate;
	}
	
	public String getEarliestAllowedDate()
	{
		MultiCalendar minDate = AbstractDateOrientedFieldSpec.getAsDate(getMinimumDate());
		if(minDate == null)
		{
			minDate = DEFAULT_EARLIEST_ALLOWED_DATE;
		}
		return minDate.toIsoDateString();
	}

	public String getLatestAllowedDate()
	{
		MultiCalendar maxDate = AbstractDateOrientedFieldSpec.getAsDate(getMaximumDate());
		if(maxDate == null)
		{
			if(StandardFieldSpecs.isStandardFieldTag(getTag()))
				maxDate = AbstractDateOrientedFieldSpec.getAsDate("");
			else
				maxDate = tenYearsFromNow();
		}
		
		return maxDate.toIsoDateString();
	}

	public static MultiCalendar tenYearsFromNow()
	{
		MultiCalendar today = AbstractDateOrientedFieldSpec.getAsDate("");
		int year = today.getGregorianYear()+10;
		int month = today.getGregorianMonth();
		int day = today.getGregorianDay();
		MultiCalendar tenYearsOut = MultiCalendar.createFromGregorianYearMonthDay(year, month, day);
		return tenYearsOut;
	}

	protected void validateDate(String fullFieldLabel, MultiCalendar candidateDate) throws DateTooEarlyException, DateTooLateException
	{
		if (!candidateDate.isUnknown())
		{
			MultiCalendar minDate = getAsDate(getMinimumDate());
			if (minDate != null && candidateDate.before(minDate))
				throw new DateTooEarlyException(fullFieldLabel, minDate.toIsoDateString());

			MultiCalendar maxDate = getAsDate(getMaximumDate());
			if (maxDate != null && candidateDate.after(maxDate))
				throw new DateTooLateException(fullFieldLabel, maxDate.toIsoDateString());
		}
	}

	public static MultiCalendar getAsDate(String isoDateString)
	{
		if(isoDateString == null)
			return null;
		if(isoDateString.length() == 0)
			return createCalendarForToday();
		
		return MultiCalendar.createFromIsoDateString(isoDateString);
	}

	// NOTE: Copied from MultiCalendar to fix a bug where today was coming back tomorrow
	private static MultiCalendar createCalendarForToday()
	{
		final int UTC_OFFSET = 0;
		GregorianCalendar cal = new GregorianCalendar(new SimpleTimeZone(UTC_OFFSET, "martus"));
		int year = cal.get(GregorianCalendar.YEAR);
		int month = cal.get(GregorianCalendar.MONTH);
		int day = cal.get(GregorianCalendar.DAY_OF_MONTH);
		cal.set(Calendar.HOUR, 12);
		cal.set(Calendar.HOUR_OF_DAY, 12);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.AM_PM, Calendar.PM);
		
		// Java Bug Id 4846659, Java 1.5 requires calling getTime() after setting HOUR
		cal.getTime();
		
		cal.set(year, month, day);
		// Java Bug Id 4846659, Java 1.5 requires calling getTime() after setting fields
		cal.getTime();
		
		return new MultiCalendar(cal);
	}

	public String getDetailsXml()
	{
		StringBuffer xml = new StringBuffer();
		xml.append(getDetailsXml(MINIMUM_DATE, minimumDate));
		xml.append(getDetailsXml(MAXIMUM_DATE, maximumDate));
		return xml.toString();
	}
	
	private String getDetailsXml(String tag, String date)
	{
		if(date == null)
			return "";

		return MartusXml.getTagStart(tag) + date + MartusXml.getTagEnd(tag);
	}

	public static class XmlIsoDateLoaderWithSpec extends XmlIsoDateLoader
	{
		public XmlIsoDateLoaderWithSpec(AbstractDateOrientedFieldSpec specToUse, String tag)
		{
			super(tag, specToUse.getTag(), specToUse.getLabel(), specToUse.getType().getTypeName());
			spec = specToUse;
		}
		
		AbstractDateOrientedFieldSpec spec;
	}
	
	public static class MinimumDateLoader extends XmlIsoDateLoaderWithSpec
	{
		public MinimumDateLoader(AbstractDateOrientedFieldSpec specToUse)
		{
			super(specToUse, MINIMUM_DATE);
		}

		public void endDocument() throws SAXException
		{
			spec.setMinimumDate(getDateAsIsoString());
			super.endDocument();
		}
	}

	public static class MaximumDateLoader extends XmlIsoDateLoaderWithSpec
	{
		public MaximumDateLoader(AbstractDateOrientedFieldSpec specToUse)
		{
			super(specToUse, MAXIMUM_DATE);
		}

		public void endDocument() throws SAXException
		{
			spec.setMaximumDate(getDateAsIsoString());
			super.endDocument();
		}
	}

	public static final String MINIMUM_DATE = "MinimumDate";
	public static final String MAXIMUM_DATE = "MaximumDate";

	public static final MultiCalendar DEFAULT_EARLIEST_ALLOWED_DATE = MultiCalendar.createFromIsoDateString("1900-01-01");

	private String minimumDate;
	private String maximumDate;
}
