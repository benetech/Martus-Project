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

import junit.framework.TestCase;

import org.martus.common.MiniLocalization;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;

public class TestDateRangeFieldSpec extends TestCase
{
	protected void setUp() throws Exception
	{
		super.setUp();
		localization = new MiniLocalization();
		spec = new DateRangeFieldSpec();
		spec.setTag(TAG);
		spec.setLabel(LABEL);
		spec.setMinimumDate(MINIMUM);
		spec.setMaximumDate(MAXIMUM);
	}
	
	public void testValidateMinimumDate() throws Exception
	{
		
		validate(MINIMUM, IN);
		validate(IN, MAXIMUM);
		validate(MINIMUM, MAXIMUM);
		
		try
		{
			validate(BEFORE, IN);
			fail("Should have thrown for date too early");
		}
		catch(DateTooEarlyException expected)
		{
			assertEquals(LABEL, expected.getFieldLabel());
			assertEquals(MINIMUM, expected.getMinimumDate());
		}

		try
		{
			validate(IN, AFTER);
			fail("Should have thrown for date too late");
		}
		catch(DateTooLateException expected)
		{
			assertEquals(LABEL, expected.getFieldLabel());
			assertEquals(MAXIMUM, expected.getMaximumDate());
		}
	}
	
	private void validate(String start, String end) throws Exception
	{
		MultiCalendar beginDate = MultiCalendar.createFromIsoDateString(start);
		MultiCalendar endDate = MultiCalendar.createFromIsoDateString(end);
		spec.validate(LABEL, MartusFlexidate.toBulletinFlexidateFormat(beginDate, endDate), localization);
	}

	public void testXml() throws Exception
	{
		String ROOT_TAG = "Field";
		String xml = spec.toXml(ROOT_TAG);
		AbstractDateOrientedFieldSpec loaded = (AbstractDateOrientedFieldSpec)FieldSpec.createFromXml(xml);
		assertEquals(spec.getTag(), loaded.getTag());
		assertEquals(spec.getLabel(), loaded.getLabel());
		assertEquals(spec.getMinimumDate(), loaded.getMinimumDate());
		assertEquals(spec.getMaximumDate(), loaded.getMaximumDate());
	}

	final String TAG = "SomeTag";
	final String LABEL = "Sample Field Label";

	final String MINIMUM = "2007-04-01";
	final String MAXIMUM = "2007-07-31";
	final String IN = "2007-06-01";
	final String BEFORE = "2007-03-31";
	final String AFTER = "2007-08-01";
	
	private MiniLocalization localization;
	private AbstractDateOrientedFieldSpec spec;
}
