/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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
package org.martus.client.search;

import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.util.TestCaseEnhanced;

public class TestChoiceItemSorterByLabelTagType extends TestCaseEnhanced
{
	public TestChoiceItemSorterByLabelTagType(String name)
	{
		super(name);
	}

	public void testBasics()
	{
		FieldSpec specMiddleSplit = FieldSpec.createCustomField("belle", "Isa", new FieldTypeNormal());
		ChoiceItem middleSplit = new ChoiceItem(specMiddleSplit);
		FieldSpec specAllTag = FieldSpec.createCustomField("", "Isabelle", new FieldTypeNormal());
		ChoiceItem allTag = new ChoiceItem(specAllTag);
		FieldSpec specAllLabel = FieldSpec.createCustomField("Isabelle", "", new FieldTypeNormal());
		ChoiceItem allLabel = new ChoiceItem(specAllLabel);
		
		assertNotEquals("glomped tag and empty label together?", middleSplit, allTag);
		assertNotEquals("glomped empty tag and label together?", middleSplit, allLabel);
		
		FieldSpec specDifferentType = FieldSpec.createCustomField("belle", "Isa", new FieldTypeMultiline());
		ChoiceItem differentType = new ChoiceItem(specDifferentType);
		assertNotEquals("ignored type?", middleSplit, differentType);
		
		FieldSpec specDifferentLabel = FieldSpec.createCustomField("belle", "Wasa", new FieldTypeNormal());
		ChoiceItem differentLabel = new ChoiceItem(specDifferentLabel);
		assertNotEquals("ignored label?", middleSplit, differentLabel);
		
		FieldSpec specDifferentTag = FieldSpec.createCustomField("pearl", "Isa", new FieldTypeNormal());
		ChoiceItem differentTag = new ChoiceItem(specDifferentTag);
		assertNotEquals("ignored tag?", middleSplit, differentTag);
		
		
	}
}
