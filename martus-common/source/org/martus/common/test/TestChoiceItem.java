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

package org.martus.common.test;

import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeDropdown;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeUnknown;
import org.martus.util.TestCaseEnhanced;

public class TestChoiceItem extends TestCaseEnhanced
{
    public TestChoiceItem(String name)
	{
		super(name);
	}

	public void testBasics()
	{
		ChoiceItem item = new ChoiceItem("a", "b");
		assertEquals("a", item.getCode());
		assertEquals("b", item.toString());
	}
	
	public void testTypes()
	{
		ChoiceItem basicItem = new ChoiceItem("a", "b");
		assertEquals(new FieldTypeUnknown(), basicItem.getType());
		
		FieldSpec spec = FieldSpec.createCustomField("tag", "label", new FieldTypeDropdown());
		ChoiceItem specItem = new ChoiceItem(spec);
		assertEquals(spec.getTag(), specItem.getCode());
		assertEquals(spec.getLabel(), specItem.toString());
		assertEquals(spec.getType(), specItem.getType());
	}
	
	public void testEquals()
	{
		String label = "Same label";
		final ChoiceItem a = new ChoiceItem("a", label);
		ChoiceItem a2 = new ChoiceItem("a", label);
		assertTrue("equals failed for identical objects?", a.equals(a2));
		
		ChoiceItem b = new ChoiceItem("b", label);
		assertFalse("didn't use tag in equals comparison?", a.equals(b));
		
		FieldSpec spec = FieldSpec.createCustomField("a", label, new FieldTypeMultiline());
		ChoiceItem c = new ChoiceItem(spec);
		assertFalse("didn't use type in equals comparison?", a.equals(c));
		
		assertFalse("equal with other type of object?", a.equals(new Object()));
		
		class SimilarToChoiceA
		{
			public String toString()
			{
				return a.toString();
			}
		}
		
		assertFalse("equal to other type with same string?", a.equals(new SimilarToChoiceA()));
	}

	public void testCompareTo()
	{
		String label = "Same label";
		ChoiceItem a = new ChoiceItem("a", label);
		ChoiceItem a2 = new ChoiceItem("a", label);
		assertEquals("compareTo failed for identical objects?", 0, a.compareTo(a2));
		
		ChoiceItem b = new ChoiceItem("b", label);
		assertNotEquals("Didn't use tag in compareTo comparison?", 0, a.compareTo(b));
		
		FieldSpec spec = FieldSpec.createCustomField("a", label, new FieldTypeMultiline());
		ChoiceItem c = new ChoiceItem(spec);
		assertNotEquals("Didn't used type in equals comparison?", 0, a.compareTo(c));
		
		assertTrue("not greater than null?", a.compareTo(null) > 0);
	}
}
