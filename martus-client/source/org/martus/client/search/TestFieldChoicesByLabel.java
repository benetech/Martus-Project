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

import java.util.Arrays;
import java.util.HashSet;

import javax.swing.tree.TreeNode;

import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.util.TestCaseEnhanced;

public class TestFieldChoicesByLabel extends TestCaseEnhanced
{
	public TestFieldChoicesByLabel(String name)
	{
		super(name);
	}
	
	protected void setUp() throws Exception
	{
		super.setUp();

		localization = new MiniLocalization();
		localization.setCurrentLanguageCode(MiniLocalization.ENGLISH);
	}
	
	public void testOnlyKeep()
	{
		FieldType normal = new FieldTypeNormal();
		FieldSpec specA = FieldSpec.createCustomField("a", "A", normal);
		FieldSpec specB = FieldSpec.createCustomField("b", "B", normal);
		FieldSpec specC = FieldSpec.createCustomField("c", "C", normal);
		MiniFieldSpec a = new MiniFieldSpec(specA);
		MiniFieldSpec b = new MiniFieldSpec(specB);
		MiniFieldSpec c = new MiniFieldSpec(specC);
		
		ChoiceItem[] choices = new ChoiceItem[] {
				new ChoiceItem(specA),
				new ChoiceItem(specB),
				new ChoiceItem(specC),
		};
		ChoiceItem[] bOnly = runChoicesThroughFilter(choices, new MiniFieldSpec[] {b});
		assertEquals(1, bOnly.length);
		assertEquals(choices[1], bOnly[0]);
		
		ChoiceItem[] aAndC = runChoicesThroughFilter(choices, new MiniFieldSpec[] {a, c});
		assertEquals(2, aAndC.length);
		assertContains(choices[0], Arrays.asList(aAndC));
		assertContains(choices[2], Arrays.asList(aAndC));
		
		
		
	}

	private ChoiceItem[] runChoicesThroughFilter(ChoiceItem[] choices, MiniFieldSpec[] keep)
	{
		FieldChoicesByLabel  fcbl = new FieldChoicesByLabel(localization);
		fcbl.addAll(new HashSet(Arrays.asList(choices)));
		fcbl.onlyKeep(keep);
		return fcbl.getRawChoices();
	}
	
	public void testEliminateDuplicates() throws Exception
	{
		FieldSpec a = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		FieldSpec b = FieldSpec.createCustomField(a.getTag(), a.getLabel(), new FieldTypeNormal());
		
		FieldChoicesByLabel choices = new FieldChoicesByLabel(localization);
		choices.add(new SearchableFieldChoiceItem(a));
		choices.add(new SearchableFieldChoiceItem(b));
		choices.mergeSimilarDropdowns();
		FieldSpec[] result = choices.asArray(localization);
		assertEquals("Didn't combine dupes?", 1, result.length);
	}

	public void testMergeSimilarDropdowns() throws Exception
	{
		ChoiceItem[] aChoices = new ChoiceItem[] {
			new ChoiceItem("a", "A"),
			new ChoiceItem("b", "B"),
		};
		DropDownFieldSpec a = new DropDownFieldSpec(aChoices);
		SearchableFieldChoiceItem choiceA = new SearchableFieldChoiceItem(a);
		
		ChoiceItem[] bChoices = new ChoiceItem[] {
			new ChoiceItem("b", "B"),
			new ChoiceItem("c", "C"),
		};
		DropDownFieldSpec b = new DropDownFieldSpec(bChoices);
		SearchableFieldChoiceItem choiceB = new SearchableFieldChoiceItem(b);
		
		FieldSpec nonDropDown = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		SearchableFieldChoiceItem choiceNonDropDown = new SearchableFieldChoiceItem(nonDropDown);

		a.setTag("tag");
		a.setLabel("Label");
		assertFalse("into non-dropdown mergeable?", FieldChoicesByLabel.areDropDownChoicesMergeable(choiceNonDropDown, choiceA));
		assertFalse("from non-dropdown mergeable?", FieldChoicesByLabel.areDropDownChoicesMergeable(choiceA, choiceNonDropDown));
		
		a.setTag("1");
		b.setTag("2");
		a.setLabel("Label");
		b.setLabel("Label");
		assertFalse("different tags mergeable?", FieldChoicesByLabel.areDropDownChoicesMergeable(choiceA, choiceB));
		try
		{
			FieldChoicesByLabel.mergeDropDownChoices(choiceA, choiceB);
			fail("Should have thrown for merging different tags");
		}
		catch(RuntimeException ignoreExpected)
		{
		}

		a.setTag("tag");
		b.setTag("tag");
		a.setLabel("1");
		b.setLabel("2");
		assertFalse("different labels mergeable?", FieldChoicesByLabel.areDropDownChoicesMergeable(choiceA, choiceB));
		try
		{
			FieldChoicesByLabel.mergeDropDownChoices(choiceA, choiceB);
			fail("Should have thrown for merging different labels");
		}
		catch(RuntimeException ignoreExpected)
		{
		}

		a.setTag("tag");
		b.setTag("tag");
		a.setLabel("1");
		b.setLabel("2");
		a.setParent(b);
		b.setParent(null);
		assertFalse("different parents mergeable?", FieldChoicesByLabel.areDropDownChoicesMergeable(choiceA, choiceB));
		try
		{
			FieldChoicesByLabel.mergeDropDownChoices(choiceA, choiceB);
			fail("Should have thrown for merging different parents");
		}
		catch(RuntimeException ignoreExpected)
		{
		}

		a.setTag("tag");
		b.setTag("tag");
		GridFieldSpec parent = new GridFieldSpec();
		parent.setTag("grid");
		a.setParent(parent);
		b.setParent(parent);
		a.setLabel("Label");
		b.setLabel("Label");
		
		boolean areMergeable = FieldChoicesByLabel.areDropDownChoicesMergeable(choiceA, choiceB);
		assertTrue("not mergeable?", areMergeable);
		
		SearchableFieldChoiceItem merged = FieldChoicesByLabel.mergeDropDownChoices(choiceA, choiceB);
		assertEquals("wrong search tag?", choiceA.getSearchTag(), merged.getSearchTag());
		assertEquals("wrong display?", a.getLabel(), merged.toString());
		assertEquals("wrong parent?", a.getParent(), merged.getSpec().getParent());
		DropDownFieldSpec mergedSpec = (DropDownFieldSpec)merged.getSpec();
		assertEquals("wrong choices?", 3, mergedSpec.getCount());
		for(int i = 0; i < a.getCount(); ++i)
			assertContains("Missing a[" + i + "]?", a.getChoice(i), Arrays.asList(mergedSpec.getAllChoices()));
		for(int i = 0; i < b.getCount(); ++i)
			assertContains("Missing b[" + i + "]?", b.getChoice(i), Arrays.asList(mergedSpec.getAllChoices()));
		assertEquals("wrong order?", b.getChoice(1), mergedSpec.getChoice(2));
	}
	
	public void testSortMergedChoicesInAsTree() throws Exception
	{
		CustomDropDownFieldSpec dropdown1 = new CustomDropDownFieldSpec();
		dropdown1.setTag("dd");
		dropdown1.setLabel("Label");
		dropdown1.setChoices(new ChoiceItem[] {new ChoiceItem("code1", "Zzzzz")});
		
		CustomDropDownFieldSpec dropdown2 = new CustomDropDownFieldSpec();
		dropdown2.setTag("dd");
		dropdown2.setLabel("Label");
		dropdown2.setChoices(new ChoiceItem[] {new ChoiceItem("code2", "Aaaaa")});
		
		FieldChoicesByLabel fieldChoicesByLabel = new FieldChoicesByLabel(localization);
		fieldChoicesByLabel.add(new SearchableFieldChoiceItem(dropdown1));
		fieldChoicesByLabel.add(new SearchableFieldChoiceItem(dropdown2));
		
		TreeNode root = fieldChoicesByLabel.asTree(localization);
		assertEquals(1, root.getChildCount());
		SearchFieldTreeNode onlyChild = (SearchFieldTreeNode) root.getChildAt(0);
		SearchableFieldChoiceItem nodeChoice = onlyChild.getChoiceItem();
		DropDownFieldSpec mergedDropdownSpec = (DropDownFieldSpec)nodeChoice.getSpec();
		ChoiceItem[] mergedChoices = mergedDropdownSpec.getAllChoices();
		assertEquals(2, mergedChoices.length);
		assertEquals(dropdown2.getChoice(0).getLabel(), mergedChoices[0].getLabel());
		assertEquals(dropdown1.getChoice(0).getLabel(), mergedChoices[1].getLabel());
	}
	
	public void testAnnotateSimilarDropdownsWithDifferentReusableChoices() throws Exception
	{
		CustomDropDownFieldSpec dropdown1 = new CustomDropDownFieldSpec();
		dropdown1.setTag("dd");
		dropdown1.setLabel("Label");
		dropdown1.addReusableChoicesCode("reusable");
		
		CustomDropDownFieldSpec dropdown2 = new CustomDropDownFieldSpec();
		dropdown2.setTag(dropdown1.getTag());
		dropdown2.setLabel(dropdown1.getLabel());
		dropdown2.addReusableChoicesCode("different");
		
		FieldChoicesByLabel choices = new FieldChoicesByLabel(localization);
		choices.add(new SearchableFieldChoiceItem(dropdown1));
		choices.add(new SearchableFieldChoiceItem(dropdown2));
		
		TreeNode root = choices.asTree(localization);
		assertEquals(1, root.getChildCount());
		SearchFieldTreeNode parent = (SearchFieldTreeNode) root.getChildAt(0);
		assertEquals(2, parent.getChildCount());
		SearchFieldTreeNode child1 = (SearchFieldTreeNode) parent.getChildAt(0);
		SearchFieldTreeNode child2 = (SearchFieldTreeNode) parent.getChildAt(1);
		assertNotEquals("Didn't differentiate?", child1.toString(), child2.toString());
	}

	MiniLocalization localization;
}
