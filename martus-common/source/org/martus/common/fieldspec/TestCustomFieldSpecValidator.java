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

import org.martus.common.FieldCollection;
import org.martus.common.FieldSpecCollection;
import org.martus.common.LegacyCustomFields;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.util.TestCaseEnhanced;

public class TestCustomFieldSpecValidator extends TestCaseEnhanced
{
	public TestCustomFieldSpecValidator(String name)
	{
		super(name);
	}
	
	protected void setUp() throws Exception
	{
		super.setUp();

		specsTopSection = new FieldSpecCollection(StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray());
		specsBottomSection = new FieldSpecCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray());
	}
	
	public void testBurmeseCharactersInTags() throws Exception
	{
		String burmese = new String(new char[] {0x102B, 0x1039, 0x103F });
		String label = "whatever";
		specsTopSection = addFieldSpec(specsTopSection, FieldSpec.createCustomField(burmese, label, new FieldTypeNormal()));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("didn't catch all errors?", 1, errors.size());
		verifyExpectedError("IllegalTagCharactersTop", CustomFieldError.CODE_ILLEGAL_TAG, burmese, label, null, (CustomFieldError)errors.get(0));
	}
	
	public void testBurmeseCharactersInReusableListCode() throws Exception
	{
		String burmese = new String(new char[] {0x102B, 0x1039, 0x103F });
		String label = "whatever";
		specsTopSection.getAllReusableChoiceLists().add(new ReusableChoices(burmese, label));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("not valid?", checker.isValid());
	}
	
	public void testBurmeseCharactersInReusableItemCode() throws Exception
	{
		String burmese = new String(new char[] {0x102B, 0x1039, 0x103F });
		String label = "whatever";
		ChoiceItem choice = new ChoiceItem(burmese, label);
		ReusableChoices choices = new ReusableChoices("code", label);
		choices.add(choice);
		specsTopSection.getAllReusableChoiceLists().add(choices);
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("not valid?", checker.isValid());
	}
	
	public void testAllValid() throws Exception
	{
		String tag = "_A.-_AllValid0123456789";
		String label = "my Label";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tag+","+label));
		String tagB = "_B.-_AllValid0123456789";
		String labelB = "my Label B";
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tagB+","+labelB));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("not valid?", checker.isValid());
	}

	public void testIllegalTagCharactersTopSection() throws Exception
	{
		String label = "anything";
		int[] nepaliByteValuesWithNonBreakingSpace = new int[] {
				0xe0, 0xa5, 0x8b, 0xc2, 
				0xa0, 
				0xe0, 0xa4, 0xb6, 
				};
		byte[] nepaliBytesWithNonBreakingSpace = new byte[nepaliByteValuesWithNonBreakingSpace.length];
		for(int i = 0; i < nepaliBytesWithNonBreakingSpace.length; ++i)
			nepaliBytesWithNonBreakingSpace[i] = (byte)nepaliByteValuesWithNonBreakingSpace[i];
		String nepaliWithNonBreakingSpace = new String(nepaliBytesWithNonBreakingSpace, "UTF-8");
		String[] variousIllegalTags = {"a tag", "a&amp;b", "a=b", "a'b", ".a", nepaliWithNonBreakingSpace};
		for(int i=0; i < variousIllegalTags.length; ++i)
		{
			String thisTag = variousIllegalTags[i];
			FieldSpec thisSpec = FieldSpec.createCustomField(thisTag, label, new FieldTypeNormal());
			specsTopSection = addFieldSpec(specsTopSection, thisSpec);
		}
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("didn't catch all errors?", variousIllegalTags.length, errors.size());
		for(int i=0; i < errors.size(); ++i)
		{
			verifyExpectedError("IllegalTagCharactersTop", CustomFieldError.CODE_ILLEGAL_TAG, variousIllegalTags[i], label, null, (CustomFieldError)errors.get(i));
		}
	}
	
	public void testIllegalTagCharactersBottomSection() throws Exception
	{
		String label = "anything";
		String[] variousIllegalTags = {"a tag", "a&amp;b", "a=b", "a'b", ".a"};
		for(int i=0; i < variousIllegalTags.length; ++i)
		{
			String thisTag = variousIllegalTags[i];
			FieldSpec thisSpec = FieldSpec.createCustomField(thisTag, label, new FieldTypeNormal());
			specsBottomSection = addFieldSpec(specsBottomSection, thisSpec);
		}
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("didn't catch all errors?", variousIllegalTags.length, errors.size());
		for(int i=0; i < errors.size(); ++i)
		{
			verifyExpectedError("IllegalTagCharactersBottom", CustomFieldError.CODE_ILLEGAL_TAG, variousIllegalTags[i], label, null, (CustomFieldError)errors.get(i));
		}
	}

	public void testMissingRequiredFields() throws Exception
	{
		FieldSpecCollection emptySpecs = new FieldSpecCollection();
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(emptySpecs, emptySpecs);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		int numberOfRequiredFields = 4;
		assertEquals("Should require 4 fields", numberOfRequiredFields , errors.size());
		for (int i = 0; i<numberOfRequiredFields; ++i)
		{
			assertEquals("Incorrect Error code required "+i, CustomFieldError.CODE_REQUIRED_FIELD, ((CustomFieldError)errors.get(i)).getCode());
		}
		Vector errorFields = new Vector();
		for (int i = 0; i<numberOfRequiredFields; ++i)
		{
			errorFields.add(((CustomFieldError)errors.get(i)).getTag());
		}
		assertContains(BulletinConstants.TAGAUTHOR, errorFields);
		assertContains(BulletinConstants.TAGLANGUAGE, errorFields);
		assertContains(BulletinConstants.TAGENTRYDATE, errorFields);
		assertContains(BulletinConstants.TAGTITLE, errorFields);
	}
	
	public void testReservedFields() throws Exception
	{
		String tagStatus = BulletinConstants.TAGSTATUS;
		String labelStatus ="status";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tagStatus+","+labelStatus));
		
		String tagSent = BulletinConstants.TAGWASSENT;
		String labelSent ="sent";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tagSent+","+labelSent));
		
		String tagSaved = BulletinConstants.TAGLASTSAVED;
		String labelSaved ="saved";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tagSaved+","+labelSaved));

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have found 3 errors", 3 , errors.size());
		verifyExpectedError("Reserved Fields", CustomFieldError.CODE_RESERVED_TAG, tagStatus, labelStatus, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Reserved Fields", CustomFieldError.CODE_RESERVED_TAG, tagSent, labelSent, null, (CustomFieldError)errors.get(1));
		verifyExpectedError("Reserved Fields", CustomFieldError.CODE_RESERVED_TAG, tagSaved, labelSaved, null, (CustomFieldError)errors.get(2));
		
		
		specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tagStatus+","+labelStatus));
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tagSent+","+labelSent));
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tagSaved+","+labelSaved));

		checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		errors = checker.getAllErrors();
		assertEquals("Should have found 3 errors", 3 , errors.size());
		verifyExpectedError("Reserved Fields Bottom Section", CustomFieldError.CODE_RESERVED_TAG, tagStatus, labelStatus, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Reserved Fields Bottom Section", CustomFieldError.CODE_RESERVED_TAG, tagSent, labelSent, null, (CustomFieldError)errors.get(1));
		verifyExpectedError("Reserved Fields Bottom Section", CustomFieldError.CODE_RESERVED_TAG, tagSaved, labelSaved, null, (CustomFieldError)errors.get(2));
	}

	public void testMartusFieldsInBottomSection() throws Exception
	{
		FieldSpecCollection specsRequiredOnlyTopSection = getRequiredOnlyTopSectionFieldSpecs();
		FieldSpecCollection specsNonRequiredBottomSection = getAllNonRequiredMartusFieldSpecs();
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsRequiredOnlyTopSection, specsNonRequiredBottomSection);
		assertFalse("Valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		int numberOfMartusFields = 6;
		assertEquals("Should require 6 fields", numberOfMartusFields, errors.size());
		for (int i = 0; i<numberOfMartusFields; ++i)
		{
			assertEquals("Incorrect Error code required "+i, CustomFieldError.CODE_MARTUS_FIELD_IN_BOTTOM_SECTION, ((CustomFieldError)errors.get(i)).getCode());
		}
		Vector errorFields = new Vector();
		for (int i = 0; i<numberOfMartusFields; ++i)
		{
			errorFields.add(((CustomFieldError)errors.get(i)).getTag());
		}
		assertContains(BulletinConstants.TAGORGANIZATION, errorFields);
		assertContains(BulletinConstants.TAGLOCATION, errorFields);
		assertContains(BulletinConstants.TAGEVENTDATE, errorFields);
		assertContains(BulletinConstants.TAGKEYWORDS, errorFields);
		assertContains(BulletinConstants.TAGSUMMARY, errorFields);
		assertContains(BulletinConstants.TAGPUBLICINFO, errorFields);
	}

	public void testPrivateFieldInTopSection() throws Exception
	{
		FieldSpecCollection specsTopSectionRequirePlusPrivate = getRequiredOnlyTopSectionFieldSpecs();
		FieldSpecCollection specsEmptyBottomSection = new FieldSpecCollection();
		
		specsTopSectionRequirePlusPrivate.add(FieldSpec.createStandardField(BulletinConstants.TAGPRIVATEINFO, new FieldTypeMultiline()));

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSectionRequirePlusPrivate, specsEmptyBottomSection);
		assertTrue("Valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals(0, errors.size());
	}

	public void testMissingTag() throws Exception
	{
		String label = "my Label";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(","+label));
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(","+label));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 errors", 2, errors.size());
		verifyExpectedError("Missing Tags", CustomFieldError.CODE_MISSING_TAG, null, label, new FieldTypeNormal(), (CustomFieldError)errors.get(0));
		verifyExpectedError("Missing Tags Bottom Section", CustomFieldError.CODE_MISSING_TAG, null, label, new FieldTypeNormal(), (CustomFieldError)errors.get(1));
	}

	public void testDuplicateTags() throws Exception
	{
		String tag = "a";
		String label ="b";
		FieldSpecCollection withA = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tag+","+label));
		FieldSpecCollection withATwice = addFieldSpec(withA, LegacyCustomFields.createFromLegacy(tag+","+label));
		String tag2 = "a2";
		String label2 ="b2";
		FieldSpecCollection withA2 = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tag2+","+label2));
		FieldSpecCollection withA2Twice = addFieldSpec(withA2, LegacyCustomFields.createFromLegacy(tag2+","+label2));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(withATwice, withA2Twice);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("Duplicate Tags", CustomFieldError.CODE_DUPLICATE_FIELD, tag, label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Duplicate Tags Bottom Section", CustomFieldError.CODE_DUPLICATE_FIELD, tag2, label2, null, (CustomFieldError)errors.get(1));

		//TODO duplicate tag from top found in bottom
		FieldSpecCollection bottomWithA = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tag+","+label));
		checker = new CustomFieldSpecValidator(withA, bottomWithA);
		assertFalse("valid?", checker.isValid());
		errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("Duplicate Tags not found in Bottom?", CustomFieldError.CODE_DUPLICATE_FIELD, tag, label, null, (CustomFieldError)errors.get(0));
		
	}
	
	public void testIllegalReusableChoiceListCodes() throws Exception
	{
		ReusableChoices choices = new ReusableChoices("a 1 . /", "choices label");
		specsTopSection.addReusableChoiceList(choices);
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("invalid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("Illegal code", CustomFieldError.CODE_ILLEGAL_TAG, choices.getCode(), choices.getLabel(), null, (CustomFieldError)errors.get(0));
	}
	
	public void testIllegalReusableChoiceItemCodes() throws Exception
	{
		ReusableChoices choices = new ReusableChoices("choicescode", "choices label");
		choices.add(new ChoiceItem("US TX.SPR", "label 1"));
		choices.add(new ChoiceItem("US/TX/S2", "label 2"));
		choices.add(new ChoiceItem("<>'", "label 3"));
		choices.add(new ChoiceItem("Ma\u00F1ana-is-legal", "label 4"));
		specsTopSection.addReusableChoiceList(choices);
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("invalid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 3 errors", 3, errors.size());
		verifyExpectedError("Illegal code", CustomFieldError.CODE_ILLEGAL_TAG, choices.getCode() + "." + choices.get(0).getCode(), choices.get(0).getLabel(), null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Illegal code", CustomFieldError.CODE_ILLEGAL_TAG, choices.getCode() + "." + choices.get(1).getCode(), choices.get(1).getLabel(), null, (CustomFieldError)errors.get(1));
		verifyExpectedError("Illegal code", CustomFieldError.CODE_ILLEGAL_TAG, choices.getCode() + "." + choices.get(2).getCode(), choices.get(2).getLabel(), null, (CustomFieldError)errors.get(2));
	}

	public void testDuplicateDropDownLabelsInNestedReusableList() throws Exception
	{
		String duplicateLabel = "Springfield";
		ReusableChoices choices = new ReusableChoices("choicescode", "choices label");
		choices.add(new ChoiceItem("US.TX.SPR", duplicateLabel));
		choices.add(new ChoiceItem("US.TX.S2", duplicateLabel));
		choices.add(new ChoiceItem("US.MO.SPR", duplicateLabel));
		choices.add(new ChoiceItem("MX.TX.SPR", duplicateLabel));
		specsTopSection.addReusableChoiceList(choices);
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("invalid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("Duplicate Labels", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, choices.get(1).getCode(), choices.get(1).getLabel(), null, (CustomFieldError)errors.get(0));
	}

	public void testDuplicateDropDownLabelsInReusableList() throws Exception
	{
		String duplicateLabel = "United States";
		ReusableChoices choices = new ReusableChoices("choicescode", "choices label");
		choices.add(new ChoiceItem("US", duplicateLabel));
		choices.add(new ChoiceItem("CA", duplicateLabel));
		choices.add(new ChoiceItem("MX", "Mexico"));
		specsTopSection.addReusableChoiceList(choices);
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("invalid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("Duplicate Labels", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, choices.get(1).getCode(), choices.get(1).getLabel(), null, (CustomFieldError)errors.get(0));
	}

	public void testDuplicateDropDownCodesInsideReusableList() throws Exception
	{
		String duplicateCode = "code";
		ReusableChoices choices = new ReusableChoices("choicescode", "choices label");
		choices.add(new ChoiceItem(duplicateCode, "Label 1"));
		choices.add(new ChoiceItem(duplicateCode, "Label 2"));
		choices.add(new ChoiceItem("othercode", "Label 3"));
		specsTopSection.addReusableChoiceList(choices);
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("Duplicate codes", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, choices.get(1).getCode(), choices.get(1).getLabel(), null, (CustomFieldError)errors.get(0));
	}

	public void testDuplicateDropDownEntry() throws Exception
	{
		String tag = "dd";
		String label ="cc";
		ChoiceItem[] choicesNoDups = {new ChoiceItem("no Dup", "first item"), new ChoiceItem("second", "second item")};
		DropDownFieldSpec dropDownSpecNoDuplicates = new DropDownFieldSpec(choicesNoDups);
		dropDownSpecNoDuplicates.setTag(tag);
		dropDownSpecNoDuplicates.setLabel(label);
		specsTopSection = addFieldSpec(specsTopSection, dropDownSpecNoDuplicates);
		
		String tag2 = "dd2";
		String label2 ="cc2";
		ChoiceItem[] choicesNoDups2 = {new ChoiceItem("no Dup2", "first item2"), new ChoiceItem("second", "second item")};
		DropDownFieldSpec dropDownSpecNoDuplicates2 = new DropDownFieldSpec(choicesNoDups2);
		dropDownSpecNoDuplicates2.setTag(tag2);
		dropDownSpecNoDuplicates2.setLabel(label2);
		specsBottomSection = addFieldSpec(specsBottomSection, dropDownSpecNoDuplicates2);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("invalid?", checker.isValid());
		
		specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();

		ChoiceItem[] choicesWithDuplicate = {new ChoiceItem("duplicate", "duplicate"), new ChoiceItem("duplicate", "duplicate")};
		DropDownFieldSpec dropDownSpecWithDuplicates = new DropDownFieldSpec(choicesWithDuplicate);
		dropDownSpecWithDuplicates.setTag(tag);
		dropDownSpecWithDuplicates.setLabel(label);
		specsTopSection = addFieldSpec(specsTopSection, dropDownSpecWithDuplicates);

		ChoiceItem[] choicesWithDuplicate2 = {new ChoiceItem("duplicate2", "duplicate2"), new ChoiceItem("duplicate2", "duplicate2")};
		DropDownFieldSpec dropDownSpecWithDuplicates2 = new DropDownFieldSpec(choicesWithDuplicate2);
		dropDownSpecWithDuplicates2.setTag(tag2);
		dropDownSpecWithDuplicates2.setLabel(label2);
		specsBottomSection = addFieldSpec(specsBottomSection, dropDownSpecWithDuplicates2);
		
		checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("Duplicate Dropdown Entry", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, tag, label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Duplicate Dropdown Entry Bottom Section", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, tag2, label2, null, (CustomFieldError)errors.get(1));
	}

	public void testDuplicateDropDownEntryInSideOfAGrid() throws Exception
	{
		String tag = "dd";
		String label ="cc";

		String tag2 = "dd2";
		String label2 ="cc2";

		ChoiceItem[] choicesNoDups = {new ChoiceItem("no Dup", "first item"), new ChoiceItem("second", "second item")};
		DropDownFieldSpec dropDownSpecNoDuplicates = new DropDownFieldSpec(choicesNoDups);
		dropDownSpecNoDuplicates.setLabel("dropdown column label");
		GridFieldSpec gridWithNoDuplicateDropdownEntries = new GridFieldSpec();
		gridWithNoDuplicateDropdownEntries.setTag(tag);
		gridWithNoDuplicateDropdownEntries.setLabel(label);
		gridWithNoDuplicateDropdownEntries.addColumn(dropDownSpecNoDuplicates);
		specsTopSection = addFieldSpec(specsTopSection, gridWithNoDuplicateDropdownEntries);

		ChoiceItem[] choicesNoDups2 = {new ChoiceItem("no Dup2", "first item2"), new ChoiceItem("second2", "second item2")};
		DropDownFieldSpec dropDownSpecNoDuplicates2 = new DropDownFieldSpec(choicesNoDups2);
		dropDownSpecNoDuplicates2.setLabel("label");
		GridFieldSpec gridWithNoDuplicateDropdownEntries2 = new GridFieldSpec();
		gridWithNoDuplicateDropdownEntries2.setTag(tag2);
		gridWithNoDuplicateDropdownEntries2.setLabel(label2);
		gridWithNoDuplicateDropdownEntries2.addColumn(dropDownSpecNoDuplicates2);
		specsBottomSection = addFieldSpec(specsBottomSection, gridWithNoDuplicateDropdownEntries2);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("invalid?", checker.isValid());
		
		specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		ChoiceItem[] choicesWithDuplicate = {new ChoiceItem("duplicate", "duplicate"), new ChoiceItem("duplicate", "duplicate")};
		DropDownFieldSpec dropDownSpecWithDuplicates = new DropDownFieldSpec(choicesWithDuplicate);
		dropDownSpecWithDuplicates.setLabel("dropdown column label with dups");
		GridFieldSpec gridWithDuplicateDropdownEntries = new GridFieldSpec();
		gridWithDuplicateDropdownEntries.setTag(tag);
		gridWithDuplicateDropdownEntries.setLabel(label);
		gridWithDuplicateDropdownEntries.addColumn(dropDownSpecWithDuplicates);
		specsTopSection = addFieldSpec(specsTopSection, gridWithDuplicateDropdownEntries);
		
		specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		ChoiceItem[] choicesWithDuplicate2 = {new ChoiceItem("duplicate2", "duplicate2"), new ChoiceItem("duplicate2", "duplicate2")};
		DropDownFieldSpec dropDownSpecWithDuplicates2 = new DropDownFieldSpec(choicesWithDuplicate2);
		dropDownSpecWithDuplicates2.setLabel("Dropdown label");
		GridFieldSpec gridWithDuplicateDropdownEntries2 = new GridFieldSpec();
		gridWithDuplicateDropdownEntries2.setTag(tag2);
		gridWithDuplicateDropdownEntries2.setLabel(label2);
		gridWithDuplicateDropdownEntries2.addColumn(dropDownSpecWithDuplicates2);
		specsBottomSection = addFieldSpec(specsBottomSection, gridWithDuplicateDropdownEntries2);

		checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("Duplicate Dropdown Entry", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, tag, label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Duplicate Dropdown Entry Bottom Section", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, tag2, label2, null, (CustomFieldError)errors.get(1));
	}

	public void testDropDownWithMissingReusableChoices() throws Exception
	{
		String reusableChoicesName = "a";
		ReusableChoices reusableChoices = new ReusableChoices(reusableChoicesName, "whatever");
		specsTopSection.addReusableChoiceList(reusableChoices);
		
		CustomDropDownFieldSpec dropdown = new CustomDropDownFieldSpec();
		dropdown.setTag("tag");
		dropdown.setLabel("Label:");
		dropdown.addReusableChoicesCode(reusableChoicesName);
		dropdown.addReusableChoicesCode("Doesn't exist");
		dropdown.addReusableChoicesCode(null);
		specsTopSection.add(dropdown);
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("Should be invalid due to missing reusable choices", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Not just the missing and null reusable choices errors?", 2, errors.size());
		CustomFieldError missingError = (CustomFieldError)errors.get(0);
		assertEquals("Wrong missing error code?", CustomFieldError.CODE_MISSING_REUSABLE_CHOICES, missingError.getCode());
		assertContains("Wrong missing tag?", dropdown.getTag(), missingError.getTag());
		assertContains("Wrong missing tag?", "Doesn't exist", missingError.getTag());
		assertEquals("Wrong missing label?", dropdown.getLabel(), missingError.getLabel());
		CustomFieldError nullError = (CustomFieldError)errors.get(1);
		assertEquals("Wrong null error code?", CustomFieldError.CODE_NULL_REUSABLE_CHOICES, nullError.getCode());
		assertEquals("Wrong null tag?", dropdown.getTag(), nullError.getTag());
		assertEquals("Wrong null label?", dropdown.getLabel(), nullError.getLabel());
	}
	
	public void testDropDownWithMissingReusableChoicesInsideGrid() throws Exception
	{
		String reusableChoicesName = "a";
		ReusableChoices reusableChoices = new ReusableChoices(reusableChoicesName, "whatever");

		CustomDropDownFieldSpec dropdown = new CustomDropDownFieldSpec();
		dropdown.setTag("tag");
		dropdown.setLabel("Label:");
		dropdown.addReusableChoicesCode(reusableChoicesName);
		dropdown.addReusableChoicesCode("Doesn't exist");

		GridFieldSpec gridWithDropDownWithMissingReusableChoices = new GridFieldSpec();
		gridWithDropDownWithMissingReusableChoices.setTag("grid");
		gridWithDropDownWithMissingReusableChoices.setLabel("Grid");
		gridWithDropDownWithMissingReusableChoices.addColumn(dropdown);
		specsTopSection = addFieldSpec(specsTopSection, gridWithDropDownWithMissingReusableChoices);
		specsTopSection.addReusableChoiceList(reusableChoices);
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		
		assertFalse("Should be invalid due to missing reusable choices", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should just be the missing reusable choices error", 1, errors.size());
		CustomFieldError error = (CustomFieldError)errors.get(0);
		assertEquals("Wrong error code?", CustomFieldError.CODE_MISSING_REUSABLE_CHOICES, error.getCode());
	}
	
	public void testNestedDropdownWithInvalidChoiceInvalidParent() throws Exception
	{
		ReusableChoices outer = new ReusableChoices("outer", "Outer");
		outer.add(new ChoiceItem("1", "first"));
		specsTopSection.addReusableChoiceList(outer);
		
		ReusableChoices inner = new ReusableChoices("inner", "Inner");
		inner.add(new ChoiceItem("2.1", "bad parent"));
		specsTopSection.addReusableChoiceList(inner);
		
		CustomDropDownFieldSpec dropdownSpec = new CustomDropDownFieldSpec();
		dropdownSpec.setTag("tag");
		dropdownSpec.setLabel("Label");
		dropdownSpec.addReusableChoicesCode(outer.getCode());
		dropdownSpec.addReusableChoicesCode(inner.getCode());
		specsTopSection.add(dropdownSpec);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("Should be invalid due to invalid parent code", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals(1, errors.size());
		CustomFieldError error = (CustomFieldError)errors.get(0);
		String errorTag = dropdownSpec.getTag() + ":" + inner.get(0).getCode();
		String errorLabel = dropdownSpec.getLabel() + ":" + inner.get(0).getLabel();
		verifyExpectedError("Duplicate Dropdown Entry", CustomFieldError.CODE_IMPROPERLY_NESTED_CHOICE_CODE, errorTag, errorLabel, null, error);
	}
	
	public void testNestedDropdownWithInvalidChoiceExtraParent() throws Exception
	{
		ReusableChoices outer = new ReusableChoices("outer", "Outer");
		outer.add(new ChoiceItem("1", "first"));
		specsTopSection.addReusableChoiceList(outer);
		
		ReusableChoices inner = new ReusableChoices("inner", "Inner");
		inner.add(new ChoiceItem("1.x.1", "extra parent"));
		specsTopSection.addReusableChoiceList(inner);

		CustomDropDownFieldSpec dropdownSpec = new CustomDropDownFieldSpec();
		dropdownSpec.setTag("tag");
		dropdownSpec.setLabel("Label");
		dropdownSpec.addReusableChoicesCode(outer.getCode());
		dropdownSpec.addReusableChoicesCode(inner.getCode());
		specsTopSection.add(dropdownSpec);

		CustomDropDownFieldSpec onlySecondLevelChoices = new CustomDropDownFieldSpec();
		onlySecondLevelChoices.setTag("tag2");
		onlySecondLevelChoices.setLabel("Label2");
		onlySecondLevelChoices.addReusableChoicesCode(inner.getCode());
		specsTopSection.add(onlySecondLevelChoices);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("Should be invalid due to extra parent code", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals(2, errors.size());
		CustomFieldError error = (CustomFieldError)errors.get(0);
		String errorTag = dropdownSpec.getTag() + ":" + inner.get(0).getCode();
		String errorLabel = dropdownSpec.getLabel() + ":" + inner.get(0).getLabel();
		verifyExpectedError("Extra parent in inner level", CustomFieldError.CODE_IMPROPERLY_NESTED_CHOICE_CODE, errorTag, errorLabel, null, error);
		CustomFieldError error2 = (CustomFieldError)errors.get(1);
		String errorTag2 = onlySecondLevelChoices.getTag() + ":" + inner.get(0).getCode();
		String errorLabel2 = onlySecondLevelChoices.getLabel() + ":" + inner.get(0).getLabel();
		verifyExpectedError("Extra dot in outer level", CustomFieldError.CODE_IMPROPERLY_NESTED_CHOICE_CODE, errorTag2, errorLabel2, null, error2);
	}
	
	public void testNestedDropdownWithInvalidChoiceWrongLevel() throws Exception
	{
		ReusableChoices outer = new ReusableChoices("outer", "Outer");
		outer.add(new ChoiceItem("1", "first"));
		specsTopSection.addReusableChoiceList(outer);
		
		ReusableChoices inner = new ReusableChoices("inner", "Inner");
		inner.add(new ChoiceItem("2", "same as parent"));
		specsTopSection.addReusableChoiceList(inner);

		CustomDropDownFieldSpec dropdownSpec = new CustomDropDownFieldSpec();
		dropdownSpec.setTag("tag");
		dropdownSpec.setLabel("Label");
		dropdownSpec.addReusableChoicesCode(outer.getCode());
		dropdownSpec.addReusableChoicesCode(inner.getCode());
		specsTopSection.add(dropdownSpec);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("Should be invalid due to same as parent code", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals(1, errors.size());
		CustomFieldError error = (CustomFieldError)errors.get(0);
		String errorTag = dropdownSpec.getTag() + ":" + inner.get(0).getCode();
		String errorLabel = dropdownSpec.getLabel() + ":" + inner.get(0).getLabel();
		verifyExpectedError("Duplicate Dropdown Entry", CustomFieldError.CODE_IMPROPERLY_NESTED_CHOICE_CODE, errorTag, errorLabel, null, error);
	}
	
	public void testNestedDropdownWithInvalidChoiceSameAsParent() throws Exception
	{
		ReusableChoices outer = new ReusableChoices("outer", "Outer");
		outer.add(new ChoiceItem("1", "first"));
		specsTopSection.addReusableChoiceList(outer);
		
		ReusableChoices inner = new ReusableChoices("inner", "Inner");
		inner.add(new ChoiceItem("1", "same as parent"));
		specsTopSection.addReusableChoiceList(inner);

		CustomDropDownFieldSpec dropdownSpec = new CustomDropDownFieldSpec();
		dropdownSpec.setTag("tag");
		dropdownSpec.setLabel("Label");
		dropdownSpec.addReusableChoicesCode(outer.getCode());
		dropdownSpec.addReusableChoicesCode(inner.getCode());
		specsTopSection.add(dropdownSpec);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("Should be invalid due to same as parent code", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals(1, errors.size());
		CustomFieldError error = (CustomFieldError)errors.get(0);
		String errorTag = dropdownSpec.getTag() + ":" + inner.get(0).getCode();
		String errorLabel = dropdownSpec.getLabel() + ":" + inner.get(0).getLabel();
		verifyExpectedError("Duplicate Dropdown Entry", CustomFieldError.CODE_IMPROPERLY_NESTED_CHOICE_CODE, errorTag, errorLabel, null, error);
	}
	
	
	
	public void testNoDropDownEntries() throws Exception
	{
		String tag = "dd";
		String label ="cc";

		String tag2 = "dd2";
		String label2 ="cc2";

		DropDownFieldSpec dropDownSpecNoEntries = new DropDownFieldSpec();
		dropDownSpecNoEntries.setTag(tag);
		dropDownSpecNoEntries.setLabel(label);
		specsTopSection = addFieldSpec(specsTopSection, dropDownSpecNoEntries);

		DropDownFieldSpec dropDownSpecNoEntries2 = new DropDownFieldSpec();
		dropDownSpecNoEntries2.setTag(tag2);
		dropDownSpecNoEntries2.setLabel(label2);
		specsBottomSection = addFieldSpec(specsBottomSection, dropDownSpecNoEntries2);
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("No Dropdown Entries", CustomFieldError.CODE_NO_DROPDOWN_ENTRIES, tag, label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("No Dropdown Entries Bottom Section", CustomFieldError.CODE_NO_DROPDOWN_ENTRIES, tag2, label2, null, (CustomFieldError)errors.get(1));
	}
	
	public void testBlankReusableChoicesListLabel() throws Exception
	{
		ReusableChoices blankLabel = new ReusableChoices("code", "");
		specsTopSection.addReusableChoiceList(blankLabel);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("Blank Reusable Choice label", CustomFieldError.CODE_MISSING_LABEL, blankLabel.getCode(), "", null, (CustomFieldError)errors.get(0));
	}
	
	public void testDuplicateReusableChoicesListLabels() throws Exception
	{
		ReusableChoices list1 = new ReusableChoices("code1", "label");
		ReusableChoices list2 = new ReusableChoices("code2", list1.getLabel());
		specsTopSection.addReusableChoiceList(list1);
		specsTopSection.addReusableChoiceList(list2);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("Duplicate Reusable Choice labels", CustomFieldError.CODE_DUPLICATE_REUSABLE_CHOICES_LIST_LABELS, null, list1.getLabel(), null, (CustomFieldError)errors.get(0));
	}

	public void testNoDropDownEntriesInsideOfAGrid() throws Exception
	{
		String tag = "dd";
		String label ="cc";

		String tag2 = "dd2";
		String label2 ="cc2";

		DropDownFieldSpec dropDownSpecNoEntries = new DropDownFieldSpec();
		dropDownSpecNoEntries.setLabel("dropdown label");
		GridFieldSpec gridWithNoDropdownEntries = new GridFieldSpec();
		gridWithNoDropdownEntries.setTag(tag);
		gridWithNoDropdownEntries.setLabel(label);
		gridWithNoDropdownEntries.addColumn(dropDownSpecNoEntries);
		specsTopSection = addFieldSpec(specsTopSection, gridWithNoDropdownEntries);

		DropDownFieldSpec dropDownSpecNoEntries2 = new DropDownFieldSpec();
		dropDownSpecNoEntries2.setLabel("dropdown label 2");
		GridFieldSpec gridWithNoDropdownEntries2 = new GridFieldSpec();
		gridWithNoDropdownEntries2.setTag(tag2);
		gridWithNoDropdownEntries2.setLabel(label2);
		gridWithNoDropdownEntries2.addColumn(dropDownSpecNoEntries2);
		specsBottomSection = addFieldSpec(specsBottomSection, gridWithNoDropdownEntries2);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("No Dropdown Entries In Grids", CustomFieldError.CODE_NO_DROPDOWN_ENTRIES, tag, label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("No Dropdown Entries In Grids Bottom Section", CustomFieldError.CODE_NO_DROPDOWN_ENTRIES, tag2, label2, null, (CustomFieldError)errors.get(1));
	}
	
	public void testBlankLabelsInsideOfAGrid() throws Exception
	{
		String columnTag = "dd";
		String columnEmptyLabel ="";
		String columnSpaceLabel =" ";

		
		String gridTag = "Grid";
		String gridLabel = "Grid Label";
		GridFieldSpec gridWithEmptyColumnLabel = new GridFieldSpec();
		gridWithEmptyColumnLabel.setTag(gridTag);
		gridWithEmptyColumnLabel.setLabel(gridLabel);
		
		gridWithEmptyColumnLabel.addColumn(LegacyCustomFields.createFromLegacy(columnTag+","+columnEmptyLabel));
		gridWithEmptyColumnLabel.addColumn(LegacyCustomFields.createFromLegacy(columnTag+","+columnSpaceLabel));
		specsTopSection = addFieldSpec(specsTopSection, gridWithEmptyColumnLabel);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("Empty Column Label in Grids", CustomFieldError.CODE_MISSING_LABEL, gridTag, null, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Space Column Label in Grids", CustomFieldError.CODE_MISSING_LABEL, gridTag, null, null, (CustomFieldError)errors.get(1));
	}
	
	public void testDropdownDataSourceHasReusableCodes() throws Exception
	{
		String reusableDropdownColumnLabel = "Reusable Label";
		String reusableChoicesCode = "reusablechoicescode";

		ReusableChoices choices = new ReusableChoices(reusableChoicesCode, "Choices");
		specsTopSection.addReusableChoiceList(choices);
		
		String gridTag = "Grid";
		String gridLabel = "Grid Label";
		GridFieldSpec gridWithReusableDropdown = new GridFieldSpec();
		gridWithReusableDropdown.setTag(gridTag);
		gridWithReusableDropdown.setLabel(gridLabel);
		
		CustomDropDownFieldSpec reusableDropdownSpec = new CustomDropDownFieldSpec();
		reusableDropdownSpec.setTag("reusableTag");
		reusableDropdownSpec.setLabel(reusableDropdownColumnLabel);
		reusableDropdownSpec.addReusableChoicesCode(reusableChoicesCode);
		gridWithReusableDropdown.addColumn(reusableDropdownSpec);
		specsTopSection.add(gridWithReusableDropdown);

		CustomDropDownFieldSpec dataDrivenDropdownSpec = new CustomDropDownFieldSpec();
		dataDrivenDropdownSpec.setTag("datadriven");
		dataDrivenDropdownSpec.setLabel("Data Driven");
		dataDrivenDropdownSpec.setDataSource(gridTag, reusableDropdownColumnLabel);
		specsTopSection.add(dataDrivenDropdownSpec);
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("DDDD refers to DD w/1 reusable code", 
				CustomFieldError.CODE_NESTED_DATA_SOURCE, 
				dataDrivenDropdownSpec.getTag(), 
				dataDrivenDropdownSpec.getLabel(), 
				dataDrivenDropdownSpec.getType(), 
				(CustomFieldError)errors.get(0));
		
	}
	
	public void testDefaultValueInPlainDropdown() throws Exception
	{
		CustomDropDownFieldSpec spec = (CustomDropDownFieldSpec) FieldSpec.createCustomField("tag", "Label", new FieldTypeDropdown());
		ChoiceItem[] choices = new ChoiceItem[] {
			new ChoiceItem("a", "A"),
			new ChoiceItem("b", "B"),
		};
		spec.setChoices(choices);
		spec.setDefaultValue(choices[0].getCode());
		specsTopSection.add(spec);
		
		CustomFieldSpecValidator checkerValid = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("not valid?", checkerValid.isValid());

		spec.setDefaultValue("whatever");
		
		CustomFieldSpecValidator checkerInvalid = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checkerInvalid.isValid());
		Vector errors = checkerInvalid.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("dd default value is not a valid code", 
				CustomFieldError.CODE_INVALID_DEFAULT_VALUE,
				spec.getTag(), 
				spec.getLabel(), 
				spec.getType(), 
				(CustomFieldError)errors.get(0));
	}
	
	public void testDefaultValueInReusableDropdown() throws Exception
	{
		ReusableChoices reusableChoices = new ReusableChoices("code", "label");
		reusableChoices.add(new ChoiceItem("", "(Unspecified)"));
		reusableChoices.add(new ChoiceItem("a", "A"));
		specsTopSection.addReusableChoiceList(reusableChoices);
		
		CustomDropDownFieldSpec spec = (CustomDropDownFieldSpec) FieldSpec.createCustomField("valid", "Valid", new FieldTypeDropdown());
		spec.addReusableChoicesCode(reusableChoices.getCode());
		specsTopSection.add(spec);

		spec.setDefaultValue(reusableChoices.get(0).getCode());
		CustomFieldSpecValidator checkerValid = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("not valid?", checkerValid.isValid());
		
		spec.setDefaultValue("whatever");
		CustomFieldSpecValidator checkerInvalid = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checkerInvalid.isValid());
		Vector errors = checkerInvalid.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("dd default value is not a valid code", 
				CustomFieldError.CODE_INVALID_DEFAULT_VALUE,
				spec.getTag(), 
				spec.getLabel(), 
				spec.getType(), 
				(CustomFieldError)errors.get(0));
	}
	
	public void testDefaultValueInReusableDropdownWithMissingList() throws Exception
	{
		CustomDropDownFieldSpec spec = (CustomDropDownFieldSpec) FieldSpec.createCustomField("valid", "Valid", new FieldTypeDropdown());
		spec.addReusableChoicesCode("Missing");
		specsTopSection.add(spec);

		spec.setDefaultValue("whatever");
		CustomFieldSpecValidator checkerInvalid = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checkerInvalid.isValid());
		Vector errors = checkerInvalid.getAllErrors();
		assertEquals("Should have 2 errors (bad default and missing reusable list)", 2, errors.size());
		verifyExpectedError("dd default value is not a valid code", 
				CustomFieldError.CODE_INVALID_DEFAULT_VALUE,
				spec.getTag(), 
				spec.getLabel(), 
				spec.getType(), 
				(CustomFieldError)errors.get(1));
	}
	
	public void testDefaultValueInNestedDropdown() throws Exception
	{
		ReusableChoices reusableChoicesOuter = new ReusableChoices("outer", "Outer");
		reusableChoicesOuter.add(new ChoiceItem("", "(Unspecified)"));
		reusableChoicesOuter.add(new ChoiceItem("a", "A"));
		specsTopSection.addReusableChoiceList(reusableChoicesOuter);

		ReusableChoices reusableChoicesInner = new ReusableChoices("inner", "Inner");
		reusableChoicesInner.add(new ChoiceItem("a.", "(Unspecified)"));
		reusableChoicesInner.add(new ChoiceItem("a.1", "A1"));
		specsTopSection.addReusableChoiceList(reusableChoicesInner);

		CustomDropDownFieldSpec spec = (CustomDropDownFieldSpec) FieldSpec.createCustomField("valid", "Valid", new FieldTypeDropdown());
		spec.addReusableChoicesCode(reusableChoicesOuter.getCode());
		spec.addReusableChoicesCode(reusableChoicesInner.getCode());
		specsTopSection.add(spec);
		
		spec.setDefaultValue(reusableChoicesInner.get(0).getCode());
		CustomFieldSpecValidator checkerFullCode = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("not valid?", checkerFullCode.isValid());
		
		spec.setDefaultValue(reusableChoicesOuter.get(0).getCode());
		CustomFieldSpecValidator checkerPartialCode = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("not valid?", checkerPartialCode.isValid());
		
		spec.setDefaultValue("whatever");
		CustomFieldSpecValidator checkerInvalid = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checkerInvalid.isValid());
		Vector errors = checkerInvalid.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("default value is not a partial or full nested code", 
				CustomFieldError.CODE_INVALID_DEFAULT_VALUE,
				spec.getTag(), 
				spec.getLabel(), 
				spec.getType(), 
				(CustomFieldError)errors.get(0));
		
	}

	public void testDataDrivenDropdownDefaultValue() throws Exception
	{
		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setTag("gridtag");
		gridSpec.setLabel("grid label");
		gridSpec.addColumn(FieldSpec.createCustomField("column", "column", new FieldTypeNormal()));
		specsTopSection.add(gridSpec);
		
		CustomDropDownFieldSpec spec = (CustomDropDownFieldSpec) FieldSpec.createCustomField("tag", "Label", new FieldTypeDropdown());
		spec.setDataSource(gridSpec.getTag(), gridSpec.getColumnLabel(0));
		spec.setDefaultValue("a");
		specsTopSection.add(spec);

		CustomFieldSpecValidator checkerInvalid = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checkerInvalid.isValid());
		Vector errors = checkerInvalid.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("dddd default value is never valid", 
				CustomFieldError.CODE_INVALID_DEFAULT_VALUE,
				spec.getTag(), 
				spec.getLabel(), 
				spec.getType(), 
				(CustomFieldError)errors.get(0));
	}
	
	public void testMissingCustomLabel() throws Exception
	{
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy("a,label"));
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy("a2,label2"));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("not valid?", checker.isValid());
		String tag = "b";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tag));
		String tag1 = "ab";
		String spaceLabel = " ";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tag1+","+spaceLabel));
		String tag2 = "b2";
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tag2));
		CustomFieldSpecValidator checker2 = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker2.isValid());
		Vector errors = checker2.getAllErrors();
		assertEquals("Should have 3 error", 3, errors.size());
		verifyExpectedError("Missing Label", CustomFieldError.CODE_MISSING_LABEL, tag, null, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Label with spaces Only", CustomFieldError.CODE_MISSING_LABEL, tag1, null, null, (CustomFieldError)errors.get(1));
		verifyExpectedError("Missing Label Bottom Section", CustomFieldError.CODE_MISSING_LABEL, tag2, null, null, (CustomFieldError)errors.get(2));
	}
	
	public void testUnknownType() throws Exception
	{
		String tag = "weirdTag";
		String label = "weird Label";
		String xmlFieldUnknownType = "<CustomFields><Field><Tag>"+tag+"</Tag>" +
			"<Label>" + label + "</Label><Type>xxx</Type>" +
			"</Field></CustomFields>";
		FieldSpec badSpecTopSection = FieldCollection.parseXml(xmlFieldUnknownType).get(0); 
		specsTopSection = addFieldSpec(specsTopSection, badSpecTopSection);
		
		String tag2 = "weirdTag2";
		String label2 = "weird Label2";
		String xmlFieldUnknownType2 = "<CustomFields><Field><Tag>"+tag2+"</Tag>" +
			"<Label>" + label2 + "</Label><Type>xxx</Type>" +
			"</Field></CustomFields>";
		FieldSpec badSpecBottomSection = FieldCollection.parseXml(xmlFieldUnknownType2).get(0); 
		specsBottomSection = addFieldSpec(specsBottomSection, badSpecBottomSection);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("didn't detect unknown?", checker.isValid());

		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("Unknown Type", CustomFieldError.CODE_UNKNOWN_TYPE, tag, label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Unknown Type Bottom Section", CustomFieldError.CODE_UNKNOWN_TYPE, tag2, label2, null, (CustomFieldError)errors.get(1));
	}

	public void testUnknownTypeInsideGrids() throws Exception
	{
		String gridTag = "Tag";
		String gridLabel = "Label";

		TestGridFieldSpec gridWithUnknownColumnType = new TestGridFieldSpec();
		gridWithUnknownColumnType.setTag(gridTag);
		gridWithUnknownColumnType.setLabel(gridLabel);
		String columnTag = "weirdTag2";
		String columnLabel = "weird Label2";
		String xmlFieldUnknownType2 = "<CustomFields><Field><Tag>"+columnTag+"</Tag>" +
			"<Label>" + columnLabel + "</Label><Type>xxx</Type>" +
			"</Field></CustomFields>";
		FieldSpec badSpecBottomSection = FieldCollection.parseXml(xmlFieldUnknownType2).get(0); 
		
		gridWithUnknownColumnType.addColumn(badSpecBottomSection);

		
		specsTopSection = addFieldSpec(specsTopSection, gridWithUnknownColumnType);
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("didn't detect unknown?", checker.isValid());

		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("Unknown Type", CustomFieldError.CODE_UNKNOWN_TYPE, gridTag, columnLabel, null, (CustomFieldError)errors.get(0));
	}

	public void testStandardFieldWithLabel() throws Exception
	{
		FieldSpec[] rawSpecsTop = specsTopSection.asArray();
		String tag = rawSpecsTop[3].getTag();
		String illegal_label = "Some Label";
		rawSpecsTop[3] = LegacyCustomFields.createFromLegacy(tag + ","+ illegal_label);
		FieldSpecCollection top = new FieldSpecCollection(rawSpecsTop);

		FieldSpec[] rawSpecsBottom = specsBottomSection.asArray();
		String tag2 = rawSpecsBottom[0].getTag();
		String illegal_label2 = "Some Label2";
		rawSpecsBottom[0] = LegacyCustomFields.createFromLegacy(tag2 + ","+ illegal_label2);
		FieldSpecCollection bottom = new FieldSpecCollection(rawSpecsBottom);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(top, bottom);
		assertFalse("valid?", checker.isValid());

		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("StandardField with Label", CustomFieldError.CODE_LABEL_STANDARD_FIELD, tag, illegal_label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("StandardField with Label Bottom Section", CustomFieldError.CODE_LABEL_STANDARD_FIELD, tag2, illegal_label2, null, (CustomFieldError)errors.get(1));
	}

	public void testParseXmlError() throws Exception
	{
		CustomFieldError xmlError = CustomFieldError.errorParseXml("message");
		assertEquals("Incorrect Error code for parse XML error", CustomFieldError.CODE_PARSE_XML, xmlError.getCode());
	}

	public void testIOError() throws Exception
	{
		String errorMessage = "io message";
		CustomFieldError xmlError = CustomFieldError.errorIO(errorMessage);
		assertEquals("Incorrect Error code for IO error", CustomFieldError.CODE_IO_ERROR, xmlError.getCode());
		assertEquals("Incorrect error message for IO error", errorMessage, xmlError.getType());
	}

	public void testSignatureError() throws Exception
	{
		CustomFieldError xmlError = CustomFieldError.errorSignature();
		assertEquals("Incorrect Error code for signature error", CustomFieldError.CODE_SIGNATURE_ERROR, xmlError.getCode());
	}

	public void testUnauthorizedKeyError() throws Exception
	{
		CustomFieldError xmlError = CustomFieldError.errorUnauthorizedKey();
		assertEquals("Incorrect Error code for parse XML error", CustomFieldError.CODE_UNAUTHORIZED_KEY, xmlError.getCode());
	}

	static public FieldSpecCollection addFieldSpec(FieldSpecCollection existingFieldSpecs, FieldSpec newFieldSpec)
	{
		FieldSpecCollection newCollection = new FieldSpecCollection(existingFieldSpecs.asArray());
		newCollection.add(newFieldSpec);
		return newCollection;
	}

	private void verifyExpectedError(String reportingErrorMsg, String expectedErrorCode, String expectedTag, String expectedLabel, FieldType expectedType, CustomFieldError errorToVerify) 
	{
		assertEquals("Incorrect Error code: " + reportingErrorMsg, expectedErrorCode, (errorToVerify).getCode());
		if(expectedTag != null)
			assertEquals("Incorrect tag: " + reportingErrorMsg, expectedTag, errorToVerify.getTag());
		if(expectedLabel != null)
			assertEquals("Incorrect label: " + reportingErrorMsg, expectedLabel, errorToVerify.getLabel());
		if(expectedType != null)
			assertEquals("Incorrect type: " + reportingErrorMsg , FieldSpec.getTypeString(expectedType), errorToVerify.getType());
	}

	public static FieldSpecCollection getRequiredOnlyTopSectionFieldSpecs()
	{
		FieldSpec[] requiredOnlyTopSectionFieldSpecs = new FieldSpec[] 
			{
				FieldSpec.createStandardField(BulletinConstants.TAGLANGUAGE, new FieldTypeLanguage()),
				FieldSpec.createStandardField(BulletinConstants.TAGAUTHOR, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGTITLE, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGENTRYDATE, new FieldTypeDate()),
			};
		
		return new FieldSpecCollection(requiredOnlyTopSectionFieldSpecs);
		
	}
	
	public static FieldSpecCollection getAllNonRequiredMartusFieldSpecs()
	{
		FieldSpec[] allNonRequiredMartusFieldSpecs = new FieldSpec[] 
			{
				FieldSpec.createStandardField(BulletinConstants.TAGORGANIZATION, new FieldTypeLanguage()),
				FieldSpec.createStandardField(BulletinConstants.TAGLOCATION, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGKEYWORDS, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGEVENTDATE, new FieldTypeDate()),
				FieldSpec.createStandardField(BulletinConstants.TAGSUMMARY, new FieldTypeDate()),
				FieldSpec.createStandardField(BulletinConstants.TAGPUBLICINFO, new FieldTypeDate()),
			};
		
		return new FieldSpecCollection(allNonRequiredMartusFieldSpecs);
		
	}
	
	class TestGridFieldSpec extends GridFieldSpec
	{

		public boolean isValidColumnType(FieldType columnType)
		{
			return true;
		}
	}
	
	private FieldSpecCollection specsTopSection;
	private FieldSpecCollection specsBottomSection;
}
