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

package org.martus.common.fieldspec;

import java.util.Vector;

import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.ReusableChoices;
import org.martus.common.field.MartusDropdownField;
import org.martus.util.TestCaseEnhanced;

public class TestCustomDropDownFieldSpec extends TestCaseEnhanced
{
	public TestCustomDropDownFieldSpec(String name)
	{
		super(name);
	}

	public void testMandatoryBlankEntry()
	{
		CustomDropDownFieldSpec spec = new CustomDropDownFieldSpec();

		Vector valuesWithoutBlank = new Vector();
		valuesWithoutBlank.add("one");
		valuesWithoutBlank.add("two");
		spec.setChoices(spec.createValidChoiceItemArrayFromStrings(valuesWithoutBlank));
		assertEquals("didn't add a blank entry?", valuesWithoutBlank.size() + 1, spec.getCount());
		assertEquals("", spec.getValue(0));
		for(int i=0; i < valuesWithoutBlank.size(); ++i)
			assertEquals("wrong order?", valuesWithoutBlank.get(i), spec.getValue(i+1));
		
		assertEquals("two", spec.getValue(spec.findCode("two")));
		assertEquals("didn't map space to the blank choice?", 0, spec.findCode(" "));
		
		
		Vector valuesWithBlank = new Vector();
		valuesWithBlank.add("a");
		valuesWithBlank.add("b");
		valuesWithBlank.add("");
		spec.setChoices(spec.createValidChoiceItemArrayFromStrings(valuesWithBlank));
		assertEquals("added a blank entry?", valuesWithBlank.size(), spec.getCount());
		for(int i=0; i < spec.getCount(); ++i)
			assertEquals("wrong order?", valuesWithBlank.get(i), spec.getValue(i));
		
	}

	public void testXml() throws Exception
	{
		FieldSpec.XmlFieldSpecLoader loader = new FieldSpec.XmlFieldSpecLoader();
		loader.parse(SAMPLE_DROPDOWN_FIELD_XML);
		DropDownFieldSpec spec = (DropDownFieldSpec)loader.getFieldSpec();
		assertEquals(3, spec.getCount());
		assertEquals("", spec.getValue(0));
		assertEquals(SAMPLE_DROPDOWN_CHOICE1, spec.getValue(1));
		assertEquals(SAMPLE_DROPDOWN_CHOICE2, spec.getValue(2));
		assertEquals(SAMPLE_DROPDOWN_FIELD_XML, spec.toString());
		assertNull("Data source didn't default null?", spec.getDataSource());
		try
		{
			spec.getValue(3);
			fail("Should have thrown");
		}
		catch(ArrayIndexOutOfBoundsException expected)
		{
		}
	}
	
	public void testDataSource() throws Exception
	{
		FieldSpec.XmlFieldSpecLoader loader = new FieldSpec.XmlFieldSpecLoader();
		loader.parse(SAMPLE_DROPDOWN_WITH_DATA_SOURCE);
		DropDownFieldSpec spec = (DropDownFieldSpec)loader.getFieldSpec();
		assertEquals(0, spec.getCount());
		assertEquals(0, spec.getReusableChoicesCodes().length);
		assertEquals(GRID_FIELD_TAG + "." + GRID_COLUMN_LABEL, spec.getDataSource());
		assertEquals(SAMPLE_DROPDOWN_WITH_DATA_SOURCE, spec.toString());
	}
	
	public void testNestedDropDowns() throws Exception
	{
		FieldSpec.XmlFieldSpecLoader loader = new FieldSpec.XmlFieldSpecLoader();
		loader.parse(SAMPLE_NESTED_DROPDOWN);
		DropDownFieldSpec spec = (DropDownFieldSpec)loader.getFieldSpec();
		assertEquals(0, spec.getCount());
		assertNull("Data source not null?", spec.getDataSource());
		String[] codes = spec.getReusableChoicesCodes();
		assertEquals(2, codes.length);
		assertEquals("a", codes[0]);
		assertEquals("b", codes[1]);
		assertEquals(SAMPLE_NESTED_DROPDOWN, spec.toXml());
	
		PoolOfReusableChoicesLists reusableChoicesLists = new PoolOfReusableChoicesLists();
		ReusableChoices choicesA = new ReusableChoices("a", "level 1");
		choicesA.add(new ChoiceItem("1", "top first"));
		choicesA.add(new ChoiceItem("2", "top second"));
		reusableChoicesLists.add(choicesA);
		ReusableChoices choicesB = new ReusableChoices("b", "level 2");
		choicesB.add(new ChoiceItem("1.1", "inner first"));
		choicesB.add(new ChoiceItem("1.2", "inner second"));
		choicesB.add(new ChoiceItem("2.1", "other inner first"));
		reusableChoicesLists.add(choicesB);
		MartusDropdownField field = new MartusDropdownField(spec, reusableChoicesLists);
		field.setData("1.2");
		assertContains("top first", spec.convertStoredToHtml(field, new MiniLocalization()));
		assertContains("inner second", spec.convertStoredToHtml(field, new MiniLocalization()));
	}
	
	public void testEquals() throws Exception
	{
		String tagA = "a";
		String tagB = "b";
		String labelC = "c";
		String labelD = "d";
		
		CustomDropDownFieldSpec specAC = new CustomDropDownFieldSpec();
		specAC.setTag(tagA);
		specAC.setLabel(labelC);
		FieldSpec specBC = new CustomDropDownFieldSpec();
		specBC.setTag(tagB);
		specBC.setLabel(labelC);
		assertNotEquals("not checking tag?", specAC, specBC);

		FieldSpec specAD = new CustomDropDownFieldSpec();
		specAD.setTag(tagA);
		specAD.setLabel(labelD);
		assertNotEquals("not checking label?", specAC, specAD);
		
		FieldSpec notDropDown = FieldSpec.createFieldSpec(new FieldTypeNormal());
		notDropDown.setTag(tagA);
		notDropDown.setLabel(labelC);
		assertNotEquals("not checking type?", specAC, notDropDown);
		
		CustomDropDownFieldSpec withChoices = new CustomDropDownFieldSpec();
		withChoices.setChoices(choices);
		withChoices.setTag(tagA);
		withChoices.setLabel(labelC);
		assertNotEquals("not checking choices?", specAC, withChoices);
		
		CustomDropDownFieldSpec identical = new CustomDropDownFieldSpec();
		identical.setChoices(choices);
		identical.setTag(tagA);
		identical.setLabel(labelC);
		assertEquals("never equal?", withChoices, identical);
		
		ChoiceItem[] flippedChoices = {choices[1], choices[0], };
		CustomDropDownFieldSpec flipped = new CustomDropDownFieldSpec();
		flipped.setChoices(flippedChoices);
		flipped.setTag(tagA);
		flipped.setLabel(labelC);
		assertNotEquals("not checking choice order?", specAC, flipped);
		
	}

	public void testDataDrivenDropdownId()
	{
		CustomDropDownFieldSpec spec = new CustomDropDownFieldSpec();
		spec.setTag("tag");
		spec.setLabel("Label");
		spec.setDataSource("gridtag", "Grid Column");
		String emptyId = spec.getId();
		ChoiceItem choice = new ChoiceItem("a", "a");
		spec.setChoices(new ChoiceItem[] {choice});
		assertNotEquals("Current choices not included in id?", emptyId, spec.getId());
	}

	
	static final ChoiceItem[] choices = {new ChoiceItem("tag", "value"), new ChoiceItem("othertag", "othervalue"),};
	public static final String SAMPLE_DROPDOWN_CHOICE1 = "choice #1";
	public static final String SAMPLE_DROPDOWN_CHOICE2 = "choice #2";
	public static final String SAMPLE_DROPDOWN_LABEL = "Dropdown Label";
	public static final String SAMPLE_DROPDOWN_FIELD_XML = "<Field type='DROPDOWN'>\n" +
			"<Tag>custom</Tag>\n" +
			"<Label>"+SAMPLE_DROPDOWN_LABEL+"</Label>\n" +
			"<Choices>\n" +
			"<Choice>" +
			"</Choice>\n" +
			"<Choice>" +
			SAMPLE_DROPDOWN_CHOICE1 +
			"</Choice>\n" +
			"<Choice>" +
			SAMPLE_DROPDOWN_CHOICE2 +
			"</Choice>\n" +
			"</Choices>\n" +
			"</Field>\n";
	
	public static final String GRID_FIELD_TAG = "GridTag";
	public static final String GRID_COLUMN_LABEL = "Grid Column Label";
	public static final String SAMPLE_DROPDOWN_WITH_DATA_SOURCE = "<Field type='DROPDOWN'>\n" +
		"<Tag>custom</Tag>\n" +
		"<Label>"+SAMPLE_DROPDOWN_LABEL+"</Label>\n" +
		"<DataSource>\n" +
		"<GridFieldTag>" + GRID_FIELD_TAG + "</GridFieldTag>\n" +
		"<GridColumnLabel>" + GRID_COLUMN_LABEL + "</GridColumnLabel>\n" +
		"</DataSource>\n" +
		"</Field>\n";
	
	public static final String SAMPLE_NESTED_DROPDOWN = "<Field type='DROPDOWN'>\n" +
		"<Tag>nested</Tag>\n" +
		"<Label>Nested</Label>\n" +
		"<UseReusableChoices code='a'></UseReusableChoices>\n" +
		"<UseReusableChoices code='b'></UseReusableChoices>\n" +
		"</Field>\n";
}
