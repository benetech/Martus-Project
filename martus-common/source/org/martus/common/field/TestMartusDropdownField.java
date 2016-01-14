/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2010, Beneficent
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
package org.martus.common.field;

import org.martus.common.GridData;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.ReusableChoices;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.TestCaseEnhanced;

public class TestMartusDropdownField extends TestCaseEnhanced
{
	public TestMartusDropdownField(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		localization = new MiniLocalization();
		reusableChoicesPool = new PoolOfReusableChoicesLists();
		choicesA = new ReusableChoices("a", "Choices A");
		choicesA.add(new ChoiceItem(levelACode1, "first"));
		choicesA.add(new ChoiceItem(levelACode2, "second"));
		choicesB = new ReusableChoices("b", "Choices B");
		choicesB.add(new ChoiceItem(levelACode1 + "." + levelBCode1, "innerfirst1"));
		choicesB.add(new ChoiceItem(levelACode1 + "." + levelBCode2, "innersecond1"));
		choicesB.add(new ChoiceItem(levelACode2 + "." + levelBCode1, "innerfirst2"));
		choicesB.add(new ChoiceItem(levelACode2 + "." + levelBCode2, "innersecond2"));
		choicesC = new ReusableChoices("c", "Choices C");
		choicesC.add(new ChoiceItem(levelACode1 + "." + levelBCode1 + "." + levelCCode1, "deepest"));
		reusableChoicesPool.add(choicesA);
		reusableChoicesPool.add(choicesB);
		reusableChoicesPool.add(choicesC);

		CustomDropDownFieldSpec oneLevelSpec = new CustomDropDownFieldSpec();
		oneLevelSpec.setTag("one");
		oneLevelSpec.setLabel("One");
		oneLevelSpec.addReusableChoicesCode(choicesA.getCode());
		oneLevelField = new MartusDropdownField(oneLevelSpec, reusableChoicesPool);
		
		CustomDropDownFieldSpec twoLevelSpec = new CustomDropDownFieldSpec();
		twoLevelSpec.setTag("two");
		twoLevelSpec.setLabel("Two");
		twoLevelSpec.addReusableChoicesCode(choicesA.getCode());
		twoLevelSpec.addReusableChoicesCode(choicesB.getCode());
		twoLevelField = new MartusDropdownField(twoLevelSpec, reusableChoicesPool);

		CustomDropDownFieldSpec threeLevelSpec = new CustomDropDownFieldSpec();
		threeLevelSpec.setTag("three");
		threeLevelSpec.setLabel("Three");
		threeLevelSpec.addReusableChoicesCode(choicesA.getCode());
		threeLevelSpec.addReusableChoicesCode(choicesB.getCode());
		threeLevelSpec.addReusableChoicesCode(choicesC.getCode());
		threeLevelField = new MartusDropdownField(threeLevelSpec, reusableChoicesPool);

	}
	
	public void testContains() throws Exception
	{
		ChoiceItem sampleChoice = choicesB.get(0);
		twoLevelField.setData(sampleChoice.getCode());
		assertFalse(twoLevelField.contains(twoLevelField.getData(), localization));
	}
	
	public void testSubfields() throws Exception
	{
		ChoiceItem sampleChoice = choicesB.get(0);
		twoLevelField.setData(sampleChoice.getCode());
		
		MartusField subA = twoLevelField.getSubField(choicesA.getCode(), localization);
		assertTrue("sub a isn't a dropdown?", subA.getType().isDropdown());
		assertEquals("sub a didn't inherit full code?", twoLevelField.getData(), subA.getData());
		String exportableDataA = subA.getFieldSpec().convertStoredToExportable(subA.getData(), reusableChoicesPool, localization);
		assertEquals("sub a exportable wrong?", twoLevelField.getData(), exportableDataA);
		String htmlDataA = subA.getFieldSpec().convertStoredToHtml(subA, localization);
		assertEquals("sub a html wrong?", choicesA.get(0).toString(), htmlDataA);
		String searchableDataA = subA.getFieldSpec().convertStoredToSearchable(subA.getData(), reusableChoicesPool, localization);
		assertEquals("sub a searchable wrong?", twoLevelField.getData(), searchableDataA);

		MartusField subB = twoLevelField.getSubField(choicesB.getCode(), localization);
		assertTrue("sub b isn't a dropdown?", subB.getType().isDropdown());
		assertEquals("sub b didn't inherit full code?", twoLevelField.getData(), subB.getData());
		String exportableDataB = subB.getFieldSpec().convertStoredToExportable(subB.getData(), reusableChoicesPool, localization);
		assertEquals("sub b exportable wrong?", twoLevelField.getData(), exportableDataB);
		String htmlDataB = subB.getFieldSpec().convertStoredToHtml(subB, localization);
		assertEquals("sub b html wrong?", sampleChoice.toString(), htmlDataB);
		String searchableDataB = subB.getFieldSpec().convertStoredToSearchable(subB.getData(), reusableChoicesPool, localization);
		assertEquals("sub b searchable wrong?", twoLevelField.getData(), searchableDataB);
	}
	
	public void testDoesMatchSingleLevel() throws Exception
	{
		oneLevelField.setData("");
		verifyEquals(oneLevelField, "");
		
		String searchForValue = choicesA.get(0).getCode();
		oneLevelField.setData(searchForValue);
		verifyEquals(oneLevelField, searchForValue);
		verifyNotEquals(oneLevelField, oneLevelField.getData().substring(0, 1));
		verifyNotEquals(oneLevelField, "");
		
		assertTrue(oneLevelField.doesMatch(MartusField.EQUAL, "\t " + searchForValue + "\t ", localization));

		oneLevelField.setData("\t " + choicesA.get(0).getCode() + "\t ");
		assertTrue(oneLevelField.doesMatch(MartusField.EQUAL, searchForValue, localization));

		oneLevelField.setData("\t " + choicesA.get(0).getCode() + "\t ");
		assertTrue(oneLevelField.doesMatch(MartusField.EQUAL, "\t " + searchForValue + "\t ", localization));
	}
	
	public void testDoesMatchMultilevel() throws Exception
	{
		threeLevelField.setData("");
		verifyEquals(threeLevelField, "");

		threeLevelField.setData(choicesC.get(0).getCode());
		assertEquals(levelACode1 + "." + levelBCode1 + "." + levelCCode1, threeLevelField.getData());
		verifyEquals(threeLevelField, threeLevelField.getData());
		verifyNotEquals(threeLevelField, choicesB.get(0).getCode());
		verifyNotEquals(threeLevelField, "");

		threeLevelField.setData(choicesB.get(0).getCode());
		verifyEquals(threeLevelField, threeLevelField.getData());
		verifyEquals(threeLevelField, choicesB.get(0).getCode());
		verifyNotEquals(threeLevelField, "");

		threeLevelField.setData(choicesC.get(0).getCode() + ".OTHER");
		verifyEquals(threeLevelField, threeLevelField.getData());
		verifyNotEquals(threeLevelField, choicesC.get(0).getCode());
		verifyNotEquals(threeLevelField, "");
	}
	
	public void testDoesMatchSpecificInnermostLevel() throws Exception
	{
		MartusField specificLevelField = threeLevelField.getSubField(choicesC.getCode(), localization);

		specificLevelField.setData("");
		verifyEquals(specificLevelField, "");

		specificLevelField.setData("");
		verifyEquals(specificLevelField, "");

		specificLevelField.setData(choicesC.get(0).getCode());
		assertEquals(levelACode1 + "." + levelBCode1 + "." + levelCCode1, specificLevelField.getData());
		verifyEquals(specificLevelField, specificLevelField.getData());
		verifyNotEquals(specificLevelField, choicesB.get(0).getCode());
		verifyNotEquals(specificLevelField, "");

		specificLevelField.setData(choicesB.get(0).getCode());
		verifyEquals(specificLevelField, specificLevelField.getData());
		verifyEquals(specificLevelField, choicesB.get(0).getCode());
		verifyNotEquals(specificLevelField, "");

		specificLevelField.setData(choicesC.get(0).getCode() + ".OTHER");
		verifyNotEquals(specificLevelField, threeLevelField.getData());
		verifyEquals(specificLevelField, choicesC.get(0).getCode());
		verifyNotEquals(specificLevelField, "");
	}
	
	public void testDoesMatchSpecificMiddleLevel() throws Exception
	{
		MartusField specificLevelField = threeLevelField.getSubField(choicesB.getCode(), localization);

		specificLevelField.setData("");
		verifyEquals(specificLevelField, "");

		specificLevelField.setData(choicesC.get(0).getCode());
		verifyEquals(specificLevelField, choicesB.get(0).getCode());
		verifyNotEquals(specificLevelField, specificLevelField.getData());
		verifyNotEquals(specificLevelField, choicesA.get(0).getCode());
		verifyNotEquals(specificLevelField, "");
		
		specificLevelField.setData(choicesB.get(0).getCode());
		verifyEquals(specificLevelField, choicesB.get(0).getCode());
		verifyNotEquals(specificLevelField, choicesA.get(0).getCode());
		verifyNotEquals(specificLevelField, "");
	}
	
	public void testSearchDropdownWithinGrid() throws Exception
	{
		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.addColumn(oneLevelField.getFieldSpec());
		gridSpec.addColumn(twoLevelField.getFieldSpec());
		MartusGridField gridField = new MartusGridField(gridSpec, reusableChoicesPool);
		GridData gridData = gridField.getGridData();
		gridData.addEmptyRow();
		gridData.setValueAt(choicesA.get(0).getCode(), 0, 0);
		gridData.setValueAt(choicesA.get(0).getCode() + ".", 0, 1);
		gridField.setData(gridData.getXmlRepresentation());

		MartusField firstDropdown = gridField.getSubField(gridSpec.getColumnLabel(0), localization);
		firstDropdown.setData(choicesA.get(0).getCode());
		verifyEquals(firstDropdown, gridData.getValueAt(0, 0));

		MartusField secondDropdown = gridField.getSubField(gridSpec.getColumnLabel(1), localization);
		secondDropdown.setData(choicesB.get(0).getCode());
		verifyEquals(secondDropdown, gridData.getValueAt(0, 1));
		verifyNotEquals(secondDropdown, choicesA.get(0).getCode());
	}
	
	private void verifyEquals(MartusField fieldToSearch, String searchForValue)
	{
		assertTrue(fieldToSearch.doesMatch(MartusField.EQUAL, searchForValue, localization));
		assertFalse(fieldToSearch.doesMatch(MartusField.NOT_EQUAL, searchForValue, localization));
	}

	private void verifyNotEquals(MartusField fieldToSearch, String searchForValue)
	{
		assertFalse(fieldToSearch.doesMatch(MartusField.EQUAL, searchForValue, localization));
		assertTrue(fieldToSearch.doesMatch(MartusField.NOT_EQUAL, searchForValue, localization));
	}

	private static final String levelACode1 = "A1";
	private static final String levelACode2 = "A2";
	private static final String levelBCode1 = "B1";
	private static final String levelBCode2 = "B2";
	private static final String levelCCode1 = "C1";
	private MiniLocalization localization;
	private PoolOfReusableChoicesLists reusableChoicesPool;
	private ReusableChoices choicesA;
	private ReusableChoices choicesB;
	private ReusableChoices choicesC;
	private MartusField oneLevelField;
	private MartusField twoLevelField;
	private MartusField threeLevelField;
}
