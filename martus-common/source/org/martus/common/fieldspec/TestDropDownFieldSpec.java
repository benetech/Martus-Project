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
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.field.MartusField;
import org.martus.util.TestCaseEnhanced;


public class TestDropDownFieldSpec extends TestCaseEnhanced
{

	public TestDropDownFieldSpec(String name)
	{
		super(name);
	}
	
	public void testGetValueFromTag() throws Exception
	{
		MiniLocalization localization = new MiniLocalization();
		PoolOfReusableChoicesLists noReusableChoices = PoolOfReusableChoicesLists.EMPTY_POOL;
		DropDownFieldSpec spec = new DropDownFieldSpec(choices);
		String nonTag = "nontag";
		MartusField field = new MartusField(spec, noReusableChoices);
		field.setData(nonTag);
		assertEquals("Did not return code back which was not found", nonTag, spec.convertStoredToHtml(field, localization));
		String upperCaseTag = "TAG";
		field.setData(upperCaseTag);
		assertEquals("not case sensitive?", upperCaseTag, spec.convertStoredToHtml(field, localization));
		field.setData("tag");
		assertEquals("value", spec.convertStoredToHtml(field, localization));
		field.setData("othertag");
		assertEquals("othervalue", spec.convertStoredToHtml(field, localization));
	}
	
	public void testGetValueFromIndex() throws Exception
	{
		DropDownFieldSpec spec = new DropDownFieldSpec(choices);
		assertEquals("value", spec.getValue(0));
		assertEquals("othervalue", spec.getValue(1));
	}
	
	public void testGetChoice() throws Exception
	{
		DropDownFieldSpec spec = new DropDownFieldSpec(choices);
		assertEquals(choices[0], spec.getChoice(0));
		assertEquals(choices[1], spec.getChoice(1));
		
	}
	
	public void testFindCode()
	{
		DropDownFieldSpec spec = new DropDownFieldSpec(choices);
		assertEquals(0, spec.findCode(choices[0].getCode()));
		assertEquals(1, spec.findCode(choices[1].getCode()));
		assertEquals(-1, spec.findCode("no such code"));
	}
	
	public void testSetChoices()
	{
		DropDownFieldSpec spec = new DropDownFieldSpec();
		spec.setChoices(choices);
		assertEquals("didn't add all choices?", choices.length, spec.getCount());
	}
	
	public void testEquals()
	{
		ChoiceItem[] choices2 = {new ChoiceItem("tag", "value2"), new ChoiceItem("othertag", "othervalue2"),};
		DropDownFieldSpec spec = new DropDownFieldSpec(choices);
		DropDownFieldSpec similar = new DropDownFieldSpec(choices2);
		assertNotEquals("didn't use choices in comparison?", spec, similar);
	}
	
	public void testDefaultValue() throws Exception
	{
		DropDownFieldSpec spec = new DropDownFieldSpec();
		assertEquals("", spec.getDefaultValue());
		spec.setChoices(choices);
		assertEquals(choices[0].getCode(), spec.getDefaultValue());
	}
	
	static final ChoiceItem[] choices = {new ChoiceItem("tag", "value"), new ChoiceItem("othertag", "othervalue"),};
}
