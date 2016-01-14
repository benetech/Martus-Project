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

package org.martus.common.field;

import org.martus.common.EnglishCommonStrings;
import org.martus.common.GridData;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeAnyField;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeLanguage;
import org.martus.common.fieldspec.FieldTypeMessage;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.GridFieldSpec.UnsupportedFieldTypeException;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;
import org.martus.util.TestCaseEnhanced;


public class TestMartusField extends TestCaseEnhanced
{
	public TestMartusField(String name)
	{
		super(name);
	}
	
	public void setUp()
	{
		localization = new MiniLocalization();
		noReusableChoices = PoolOfReusableChoicesLists.EMPTY_POOL;
	}

	public void testBasics()
	{
		FieldSpec spec = FieldSpec.createCustomField("tag", "label", new FieldTypeNormal());
		MartusField f = new MartusField(spec, noReusableChoices);
		assertEquals("wrong tag?", spec.getTag(), f.getTag());
		assertEquals("wrong label?", spec.getLabel(), f.getLabel());
		assertEquals("wrong type?", spec.getType(), f.getType());
		assertEquals("not initially blank?", "", f.getData());
		
		assertEquals("wrong spec?", spec.toString(), f.getFieldSpec().toString());
		
		final String sampleData = "test data"; 
		f.setData(sampleData);
		assertEquals("didn't set data?", sampleData, f.getData());
	}
	
	private GridFieldSpec createSampleGridSpec() throws UnsupportedFieldTypeException
	{
		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(FieldSpec.createCustomField("a", "A", new FieldTypeNormal()));
		spec.addColumn(FieldSpec.createCustomField("b", "B", new FieldTypeBoolean()));
		return spec;
	}
	
	public void testGridMissingSubField() throws Exception
	{
		GridFieldSpec gridSpec = createSampleGridSpec();
		MartusGridField gridField = new MartusGridField(gridSpec, noReusableChoices);
		MartusField noSuchColumn = gridField.getSubField("not a real column name", localization);
		assertNotNull("returned null for missing column?", noSuchColumn);
	}
	
	public void testNonGridHtml() throws Exception
	{
		MartusField languageField = new MartusField(createFieldSpec(new FieldTypeLanguage()), noReusableChoices);
		languageField.setData(MiniLocalization.ENGLISH);
		assertEquals("Didn't localize language name?", "&lt;language:en&gt;", languageField.html(localization));
		
		MartusField dateField = new MartusField(createFieldSpec(new FieldTypeDate()), noReusableChoices);
		dateField.setData("1968-02-21");
		assertEquals("Didn't localize date?", "02/21/1968", dateField.html(localization));

		MartusField dateRangeField = new MartusField(createFieldSpec(new FieldTypeDateRange()), noReusableChoices);
		dateRangeField.setData("1967-03-21,19670321+9");
		assertEquals("Didn't localize date range?", "03/21/1967 - 03/30/1967", dateRangeField.html(localization));

		DropDownFieldSpec dropdownSpec = new DropDownFieldSpec(choices);
		dropdownSpec.setTag("c");
		dropdownSpec.setLabel("C");
		MartusField dropdownField = new MartusField(dropdownSpec, noReusableChoices);
		dropdownField.setData("ampersand");
		assertEquals("Didn't decode dropdown?", "This &amp; That", dropdownField.html(localization));
		
		MartusField booleanField = new MartusField(createFieldSpec(new FieldTypeBoolean()), noReusableChoices);
		booleanField.setData(FieldSpec.TRUESTRING);
		assertEquals("Didn't htmlize?", localization.getButtonLabel(EnglishCommonStrings.YES), booleanField.html(localization));
		
		MartusField blankField = new MartusField(createFieldSpec(new FieldTypeNormal()), noReusableChoices);
		blankField.setData("");
		assertEquals("Empty not converted to nbsp?", "&nbsp;", blankField.html(localization));
		
		blankField.setData("  ");
		assertEquals("Spaces not converted to nbsp?", "&nbsp;", blankField.html(localization));
	}
	
	public void testGridHtml() throws Exception
	{
		GridFieldSpec spec = createSampleGridSpec();
		DropDownFieldSpec dropdownSpec = new DropDownFieldSpec(choices);
		dropdownSpec.setTag("c");
		dropdownSpec.setLabel("C");
		spec.addColumn(dropdownSpec);
		spec.addColumn(FieldSpec.createCustomField("d", "D", new FieldTypeDate()));
		spec.addColumn(FieldSpec.createCustomField("e", "E", new FieldTypeDateRange()));
		MartusField field = new MartusGridField(spec, noReusableChoices);
		GridData data = new GridData(spec, noReusableChoices);
		data.addEmptyRow();
		data.addEmptyRow();
		for(int row = 0; row < 2; ++row)
		{
			data.setValueAt("&" + row + "," + 0, row, 0);
			data.setValueAt(FieldSpec.TRUESTRING, row, 1);
			data.setValueAt("ampersand", row, 2);
			data.setValueAt("2004-12-30", row, 3);
			data.setValueAt("2002-07-21,20020721+5", row, 4);
		}
		field.setData(data.getXmlRepresentation());
		String expected = "<table border='1' >" +
				"<tr><td>&amp;0,0</td><td>&lt;button:yes&gt;</td><td>This &amp; That</td>" +
				"<td>12/30/2004</td><td>07/21/2002 - 07/26/2002</td></tr>" +
				"<tr><td>&amp;1,0</td><td>&lt;button:yes&gt;</td><td>This &amp; That</td>" +
				"<td>12/30/2004</td><td>07/21/2002 - 07/26/2002</td></tr>" +
				"</table>";
		assertEquals("Didn't htmlize grid?", expected, field.html(localization));
	}

	public void testGetSearchableDataForStringFields()
	{
		verifyNormalDataIsAlsoPrintable(new FieldTypeNormal(), "sample string");
		verifyNormalDataIsAlsoPrintable(new FieldTypeMultiline(), "sample string");
		verifyNormalDataIsAlsoPrintable(new FieldTypeMessage(), "sample string");
	}
	
	public void testGetSearchableDataForDateFields()
	{
		String rawDate = "2005-10-15";
		String localizedDate = localization.convertStoredDateToDisplay(rawDate);
		
		verifySearchableData(new FieldTypeDate(), rawDate, localizedDate);
	}
	
	public void testGetSearchableDataForDateRangeFields()
	{
		MultiCalendar beginDate = MultiCalendar.createFromGregorianYearMonthDay(1954, 4, 21);
		MultiCalendar endDate = MultiCalendar.createFromGregorianYearMonthDay(1972, 9, 30);

		// FIXME: Extract out to common, with tests
		String rawDateRange = MartusFlexidate.toStoredDateFormat(beginDate) + 
			MartusFlexidate.DATE_RANGE_SEPARATER +
			MartusFlexidate.toFlexidateFormat(beginDate, endDate);
		
		String localizedDateRange = localization.getViewableDateRange(rawDateRange);
		verifySearchableData(new FieldTypeDateRange(), rawDateRange, localizedDateRange);
	}

	public void testGetSearchableDataForBooleanFields()
	{
		verifySearchableData(new FieldTypeBoolean(), FieldSpec.TRUESTRING, localization.getButtonLabel(EnglishCommonStrings.YES));
	}

	public void testGetSearchableDataForLanguageFields()
	{
		String languageCode = MiniLocalization.ARABIC;
		verifySearchableData(new FieldTypeLanguage(), languageCode, localization.getLanguageName(languageCode));
	}

	public void testGetSearchableDataForDropDownFields()
	{
		DropDownFieldSpec spec = new DropDownFieldSpec(choices);
		for(int i=0; i < choices.length; ++i)
			verifySearchableData(spec, choices[i].getCode(), choices[i].toString());
	}

	public void testGetSearchableDataForGridFields() throws Exception
	{
		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(FieldSpec.createCustomField("customtag", "Custom Label", new FieldTypeNormal()));
		spec.addColumn(new DropDownFieldSpec(choices));
		
		MartusField gridField = new MartusField(spec, noReusableChoices);
		GridData data = new GridData(spec, noReusableChoices);
		data.addEmptyRow();
		data.addEmptyRow();
		data.addEmptyRow();
		data.setValueAt("abc", 0, 0);
		data.setValueAt(choices[1].getCode(), 0, 1);
		data.setValueAt("second row", 1, 0);
		gridField.setData(data.getXmlRepresentation());
		final String result = gridField.getSearchableData(localization);
		assertEquals("Grid data not searchable?", "abc\t" + choices[1].toString() + "\t\nsecond row\t" + choices[0].toString() + "\t\n", result);
	}
	
	public void testGetSearchableDataForGridFieldDateRanges() throws Exception
	{
		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(FieldSpec.createCustomField("", "Date Range", new FieldTypeDateRange()));
		
		GridData data = new GridData(spec, noReusableChoices);
		data.addEmptyRow();
		data.addEmptyRow();
		data.addEmptyRow();
		final int MARCH = 3;
		final int SEPTEMBER = 9;
		MultiCalendar begin = MultiCalendar.createFromGregorianYearMonthDay(2004, SEPTEMBER, 21);
		MultiCalendar end = MultiCalendar.createFromGregorianYearMonthDay(2005, MARCH, 18);
		String rangeString = MartusFlexidate.toBulletinFlexidateFormat(begin, end);
		data.setValueAt(rangeString, 0, 0);

		MartusGridField gridField = new MartusGridField(spec, noReusableChoices);
		gridField.setData(data.getXmlRepresentation());
		String label = spec.getFieldSpec(0).getLabel();
		MartusField columnFields = gridField.getSubField(label, localization);
		MartusField beginField = columnFields.getSubField("begin", localization);
		assertTrue("can't find begin?", beginField.doesMatch(MartusField.EQUAL, "2004-09-21", localization));
		assertFalse("found bad begin?", beginField.doesMatch(MartusField.EQUAL, "2005-03-18", localization));
		MartusField endField = columnFields.getSubField("end", localization);
		assertTrue("can't find end?", endField.doesMatch(MartusField.EQUAL, "2005-03-18", localization));
		assertFalse("found bad end?", endField.doesMatch(MartusField.EQUAL, "2005-09-21", localization));
	}

	private void verifyNormalDataIsAlsoPrintable(final FieldType type, final String rawData)
	{
		final String expectedPrintableData = rawData;
		verifySearchableData(type, rawData, expectedPrintableData);
	}

	private void verifySearchableData(final FieldType type, final String rawData, final String expectedPrintableData)
	{
		final FieldSpec spec = createFieldSpec(type);
		verifySearchableData(spec, rawData, expectedPrintableData);
	}

	private void verifySearchableData(final FieldSpec spec, final String rawData, final String expectedPrintableData)
	{
		MartusField field = new MartusField(spec, noReusableChoices);
		field.setData(rawData);
		assertEquals("Wrong printableData for " + FieldSpec.getTypeString(spec.getType()), expectedPrintableData, field.getSearchableData(localization));
	}
	
	public void testInitialValueForSimpleTypes()
	{
		verifyInitialValue(new FieldTypeNormal());
		verifyInitialValue(new FieldTypeBoolean());
		verifyInitialValue(new FieldTypeDate());
		verifyInitialValue(new FieldTypeDateRange());
		verifyInitialValue(new FieldTypeLanguage());
		verifyInitialValue(new FieldTypeMessage());
		verifyInitialValue(new FieldTypeMultiline());
	}
	
	public void testDropDownInitialValue()
	{	
		DropDownFieldSpec spec = new DropDownFieldSpec(choices);
		MartusField f = new MartusField(spec, noReusableChoices);
		assertEquals("Dropdown didn't default to first entry?", choices[0].getCode(), f.getData());
	}
	
	public void testGridInitialValue() throws Exception
	{
		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(createFieldSpec(new FieldTypeNormal()));
		spec.addColumn(createFieldSpec(new FieldTypeBoolean()));
		spec.addColumn(new DropDownFieldSpec(choices));
		MartusField f = new MartusField(spec, noReusableChoices);
		GridData data = new GridData(spec, noReusableChoices);
		data.setFromXml(f.getData());
		for(int col = 0; col < spec.getColumnCount(); ++col)
			assertEquals("Normal column wrong data?", spec.getFieldSpec(col).getDefaultValue(), data.getValueAt(0, col));
		
		try
		{
			spec.addColumn(createFieldSpec(new FieldTypeAnyField()));
			fail("Should have thrown for unsupported type");
		}
		catch(GridFieldSpec.UnsupportedFieldTypeException ignoreExpected)
		{
		}
	}
	
	public void testCompareToTrimsSpaces()
	{
		MartusField f = new MartusField(createFieldSpec(new FieldTypeNormal()), noReusableChoices);
		f.setData(" with spaces ");
		assertEquals("didn't trim data?", 0, f.compareTo("with spaces", localization));
		assertEquals("didn't trim search string?", 0, f.compareTo("   with spaces   ", localization));
	}
	
	private void verifyInitialValue(FieldType type)
	{
		FieldSpec spec = createFieldSpec(type);
		MartusField f = new MartusField(spec, noReusableChoices);
		assertEquals("wrong initial value for type " + type + ": ", spec.getDefaultValue(), f.getData());
	}

	private FieldSpec createFieldSpec(FieldType type)
	{
		FieldSpec spec = FieldSpec.createCustomField(type.getTypeName(), "label", type);
		return spec;
	}

	ChoiceItem[] choices = 
	{
		new ChoiceItem("firstcode", "First Value"),
		new ChoiceItem("secondcode", "Second Value"),
		new ChoiceItem("ampersand", "This & That"),
	};

	MiniLocalization localization;
	private PoolOfReusableChoicesLists noReusableChoices;
}
