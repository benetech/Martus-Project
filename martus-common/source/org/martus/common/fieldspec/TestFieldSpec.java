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

package org.martus.common.fieldspec;

import java.util.Vector;

import org.martus.common.LegacyCustomFields;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.test.UnicodeConstants;
import org.martus.util.TestCaseEnhanced;


public class TestFieldSpec extends TestCaseEnhanced
{
	public TestFieldSpec(String name)
	{
		super(name);
	}
	
	public void testLegacy()
	{
		FieldSpec plainField = LegacyCustomFields.createFromLegacy("a,b");
		assertFalse("has unknown?", plainField.hasUnknownStuff());
		assertEquals("a", plainField.getTag());
		assertEquals("b", plainField.getLabel());
		assertEquals("not normal?", new FieldTypeNormal(), plainField.getType());
		
		FieldSpec fieldWithExtra = LegacyCustomFields.createFromLegacy("c,d,e");
		assertTrue("doesn't have unknown?", fieldWithExtra.hasUnknownStuff());
		assertEquals("c", fieldWithExtra.getTag());
		assertEquals("d", fieldWithExtra.getLabel());
		assertEquals("not unknown?", new FieldTypeUnknown(), fieldWithExtra.getType());
		
		FieldSpec fieldWithIllegalCharacters = LegacyCustomFields.createFromLegacy("!<a9-._@#jos"+UnicodeConstants.ACCENT_E_LOWER+"e,!<a9-._@#jos"+UnicodeConstants.ACCENT_E_LOWER+"e");
		assertEquals("__a9-.___jos"+UnicodeConstants.ACCENT_E_LOWER+"e", fieldWithIllegalCharacters.getTag());
		assertEquals("!<a9-._@#jos"+UnicodeConstants.ACCENT_E_LOWER+"e", fieldWithIllegalCharacters.getLabel());

		FieldSpec fieldWithIllegalFirstCharacter = LegacyCustomFields.createFromLegacy(".ok,ok");
		assertEquals("_ok", fieldWithIllegalFirstCharacter.getTag());
	}
	
	public void testCreateFromTag()
	{
		FieldSpec plainField = LegacyCustomFields.createFromLegacy("author");
		assertFalse("has unknown?", plainField.hasUnknownStuff());
		assertEquals("author", plainField.getTag());
		assertEquals("", plainField.getLabel());
		assertEquals("not normal?", new FieldTypeNormal(), plainField.getType());

		FieldSpec dateField = LegacyCustomFields.createFromLegacy("entrydate");
		assertFalse("has unknown?", dateField.hasUnknownStuff());
		assertEquals("entrydate", dateField.getTag());
		assertEquals("", dateField.getLabel());
		assertEquals("not date?", new FieldTypeDate(), dateField.getType());
	}
	
	public void testDefaultValues()
	{
		String emptyString = "";
		FieldSpec spec = FieldSpec.createFieldSpec(new FieldTypeBoolean());
		assertEquals(FieldSpec.FALSESTRING, spec.getDefaultValue());
		
		spec = FieldSpec.createFieldSpec(new FieldTypeDate());
		assertEquals("", spec.getDefaultValue());

		spec = FieldSpec.createFieldSpec(new FieldTypeDateRange());
		assertEquals(emptyString, spec.getDefaultValue());

		spec = new GridFieldSpec();
		assertEquals(emptyString, spec.getDefaultValue());
		
		spec = FieldSpec.createFieldSpec(new FieldTypeLanguage());
		assertEquals(MiniLocalization.LANGUAGE_OTHER, spec.getDefaultValue());
		
		spec = FieldSpec.createFieldSpec(new FieldTypeMultiline());
		assertEquals(emptyString, spec.getDefaultValue());

		spec = FieldSpec.createFieldSpec(new FieldTypeNormal());
		assertEquals(emptyString, spec.getDefaultValue());

		spec = new DropDownFieldSpec(new ChoiceItem[] {new ChoiceItem("first", "First item"), new ChoiceItem("", "")});
		assertEquals("first", spec.getDefaultValue());
		
		spec = FieldSpec.createFieldSpec(new FieldTypeMessage());
		assertEquals(emptyString, spec.getDefaultValue());
	
		CustomDropDownFieldSpec dropdownSpec = new CustomDropDownFieldSpec();
		dropdownSpec.setChoices(dropdownSpec.createValidChoiceItemArrayFromStrings(new Vector()));
		assertEquals("", dropdownSpec.getDefaultValue());

		String message = "Message in FieldSpec";
		MessageFieldSpec messageSpec = new MessageFieldSpec();
		messageSpec.putMessage(message);
		assertEquals(message, messageSpec.getDefaultValue());

	}
	
	public void testToString()
	{
		FieldSpec plainField = LegacyCustomFields.createFromLegacy("a,<&b>");
		String xml = "<Field type='STRING'>\n<Tag>a</Tag>\n<Label>&lt;&amp;b&gt;</Label>\n</Field>\n";
		assertEquals(xml, plainField.toString());
	}
	
	public void testGetTypeString()
	{
		assertEquals("STRING", FieldSpec.getTypeString(new FieldTypeNormal()));
		assertEquals("MULTILINE", FieldSpec.getTypeString(new FieldTypeMultiline()));
		assertEquals("DATE", FieldSpec.getTypeString(new FieldTypeDate()));
		assertEquals("DATERANGE", FieldSpec.getTypeString(new FieldTypeDateRange()));
		assertEquals("BOOLEAN", FieldSpec.getTypeString(new FieldTypeBoolean()));
		assertEquals("LANGUAGE", FieldSpec.getTypeString(new FieldTypeLanguage()));
		assertEquals("GRID", FieldSpec.getTypeString(new FieldTypeGrid()));
		assertEquals("DROPDOWN", FieldSpec.getTypeString(new FieldTypeDropdown()));
		assertEquals("MESSAGE", FieldSpec.getTypeString(new FieldTypeMessage()));
		assertEquals("SECTION", FieldSpec.getTypeString(new FieldTypeSectionStart()));
		assertEquals("UNKNOWN", FieldSpec.getTypeString(new FieldTypeUnknown()));
		assertEquals("UNKNOWN", FieldSpec.getTypeString(new FieldTypeAnyField()));
	}
	
	public void testGetTypeCode()
	{
		verifyCreatedTypeString("STRING");
		verifyCreatedTypeString("MULTILINE");
		verifyCreatedTypeString("DATE");
		verifyCreatedTypeString("DATERANGE");
		verifyCreatedTypeString("BOOLEAN");
		verifyCreatedTypeString("LANGUAGE");
		verifyCreatedTypeString("GRID");
		verifyCreatedTypeString("DROPDOWN");
		verifyCreatedTypeString("MESSAGE");
		verifyCreatedTypeString("SECTION");
		assertEquals("UNKNOWN", FieldSpec.getTypeCode("anything else").getTypeName());
	}
	
	private void verifyCreatedTypeString(String typeString)
	{
		FieldType type = FieldSpec.getTypeCode(typeString);
		assertEquals("wrong type: " + typeString, typeString, type.getTypeName());
	}
	
	public void testKeepWithPrevious() throws Exception
	{
		String xml = "<Field Type='NORMAL'><Tag>AUTHOR</Tag><KeepWithPrevious/></Field>";
		FieldSpec spec = FieldSpec.createFromXml(xml);
		assertTrue("Didn't notice KeepWithPrevious?", spec.keepWithPrevious());
		FieldSpec reloaded = FieldSpec.createFromXml(spec.toString());
		assertTrue("Didn't save and reload KeepWithPrevious?", reloaded.keepWithPrevious());
	}
	
	public void testIsRequired() throws Exception
	{
		String xml = "<Field type='NORMAL'><Tag>AUTHOR</Tag><RequiredField/></Field>";
		FieldSpec spec = FieldSpec.createFromXml(xml);
		assertTrue("Didn't notice RequiredField?", spec.isRequiredField());
		FieldSpec reloaded = FieldSpec.createFromXml(spec.toString());
		assertTrue("Didn't save and reload RequiredField?", reloaded.isRequiredField());
		
		String sectionXml = "<Field type='SECTION'><Tag>Anything</Tag><RequiredField/></Field>";
		FieldSpec sectionSpec = FieldSpec.createFromXml(sectionXml);
		assertFalse("Section didn't ignore required?", sectionSpec.isRequiredField());
		
	}
	
	public void testAllowUserDefaultValue() throws Exception
	{
		assertTrue(FieldSpec.createStandardField(Bulletin.TAGAUTHOR, new FieldTypeNormal()).allowUserDefaultValue());
		assertTrue(FieldSpec.createCustomField("normal", "Label", new FieldTypeNormal()).allowUserDefaultValue());
		assertTrue(FieldSpec.createCustomField("multiline", "Label", new FieldTypeMultiline()).allowUserDefaultValue());
		assertTrue(FieldSpec.createCustomField("dropdown", "Label", new FieldTypeDropdown()).allowUserDefaultValue());

		assertFalse(FieldSpec.createCustomField("date", "Label", new FieldTypeDate()).allowUserDefaultValue());
		assertFalse(FieldSpec.createCustomField("daterange", "Label", new FieldTypeDateRange()).allowUserDefaultValue());
		assertFalse(FieldSpec.createCustomField("boolean", "Label", new FieldTypeBoolean()).allowUserDefaultValue());
		assertFalse(FieldSpec.createCustomField("grid", "Label", new FieldTypeGrid()).allowUserDefaultValue());
		assertFalse(FieldSpec.createCustomField("message", "Label", new FieldTypeMessage()).allowUserDefaultValue());
		assertFalse(FieldSpec.createCustomField("language", "Label", new FieldTypeLanguage()).allowUserDefaultValue());
		assertFalse(FieldSpec.createCustomField("section", "Label", new FieldTypeSectionStart()).allowUserDefaultValue());
	}
	
	public void testGetUserDefaultValue() throws Exception
	{
		String value = "xyz";
		String xml = "<Field type='NORMAL'><Tag>AUTHOR</Tag><DefaultValue>" + value + "</DefaultValue></Field>";
		FieldSpec spec = FieldSpec.createFromXml(xml);
		assertEquals("Didn't notice default value?", value, spec.getDefaultValue());
		FieldSpec reloaded = FieldSpec.createFromXml(spec.toString());
		assertEquals("Didn't save and reload default value?", value, reloaded.getDefaultValue());
	}
	
	public void testEqualsAndCompareTo()
	{
		FieldSpec a = FieldSpec.createFieldSpec(new FieldTypeNormal());
		String labelA = "label a";
		String tagA = "NewTagA";
		String labelB = "label b";
		String tagB = "NewTagB";
		a.setLabel(labelA);
		a.setTag(tagA);
		FieldSpec b = FieldSpec.createFieldSpec(new FieldTypeNormal());
		b.setLabel(labelA);
		b.setTag(tagA);
		assertTrue("A & B should be identical (equals)", a.equals(b));
		assertEquals("A & B should be identical (compareTo)", 0, a.compareTo(b));
		
		b.setLabel(labelB);
		assertFalse("B has different Label (equals)", a.equals(b));
		assertNotEquals("B has different Label (compareTo)", 0, a.compareTo(b));

		b.setLabel(labelA);
		b.setTag(tagB);
		assertFalse("B has different Tag (equals)", a.equals(b));
		assertNotEquals("B has different Tag (compareTo)", 0, a.compareTo(b));
		
		b.setTag("AAA");
		b.setLabel("zzz");
		assertNotEquals("a equals b?", 0, a.compareTo(b));
		assertEquals("reverse inconsistent?", (a.compareTo(b) > 0), (b.compareTo(a) < 0));
		
		FieldSpec c = FieldSpec.createFieldSpec(new FieldTypeMultiline());
		c.setLabel(labelA);
		c.setTag(tagA);
		assertFalse("C has different Type (equals)", a.equals(b));
		assertNotEquals("C has different Type (compareTo)", 0, a.compareTo(b));
		
		String d = "someString";
		assertFalse("FieldSpec is not a String", a.equals(d));
		
		assertTrue("not greater than null?", a.compareTo(null) > 0);
	}
	
	public void testSubFieldsGrid() throws Exception
	{
		GridFieldSpec grid = new GridFieldSpec();
		grid.setTag("grid");
		grid.setLabel("My Grid");
		grid.addColumn(FieldSpec.createCustomField("", "Column 1", new FieldTypeDateRange()));
		
		FieldSpec columnSubField = FieldSpec.createSubField(grid, "Column 1", "My Grid (Column 1)", new FieldTypeDateRange());
		assertEquals("not grid parent?", grid, columnSubField.getParent());
		FieldSpec beginSubField = FieldSpec.createSubField(columnSubField, "begin", "Column 1 (beginning)", new FieldTypeDateRange());
		assertEquals("not column parent?", columnSubField, beginSubField.getParent());
		assertEquals("bad subfield tag?", "begin", beginSubField.getSubFieldTag());
		assertEquals("bad full tag?", "grid.Column 1.begin", beginSubField.getTag());
	}
	
	public void testSubFieldsNestedDropDown() throws Exception
	{
		CustomDropDownFieldSpec parentSpec = new CustomDropDownFieldSpec();
		parentSpec.setTag("parent");
		FieldSpec subSpec = FieldSpec.createSubField(parentSpec, "a", "A", new FieldTypeDropdown());

		assertEquals(parentSpec, subSpec.getParent());
		assertEquals("parent.a", subSpec.getTag());
	}
}
