/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

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
package org.martus.client.core;

import org.martus.common.FieldSpecCollection;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.FormTemplateFromXFormsLoader;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeMessage;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FieldTypeSectionStart;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.TestCaseEnhanced;

public class TestXFormsToFormTemplateConverter extends TestCaseEnhanced
{
	public TestXFormsToFormTemplateConverter(String name)
	{
		super(name);
	}

	public void testIsXFormsXml() throws Exception
	{
		verifyIsXFormsXml(false, "");
		verifyIsXFormsXml(false, null);
		verifyIsXFormsXml(true, TestBulletinFromXFormsLoader.getEmptyXFormsModelXmlAsString());
		verifyIsXFormsXml(true, TestBulletinFromXFormsLoaderConstants.XFORMS_MODEL_MESSAGE_FIELD);
		verifyIsXFormsXml(true, TestBulletinFromXFormsLoaderConstants.COMPLETE_XFORMS_MODEL);
		verifyIsXFormsXml(true, TestBulletinFromXFormsLoaderConstants.XFORMS_WITH_TWO_GRIDS_WITH_SAME_LABELS);
	}

	private void verifyIsXFormsXml(boolean expectedResult, String possibleXFormsXml)
	{
		assertEquals("should be xforms?", expectedResult, FormTemplateFromXFormsLoader.isXFormsXml(possibleXFormsXml));
	}
	
	public void testXFormGridsWithSameLabel() 
	{
		try
		{
			FormTemplateFromXFormsLoader.createNewBulletinFromXFormsFormTemplate(TestBulletinFromXFormsLoaderConstants.XFORMS_WITH_TWO_GRIDS_WITH_SAME_LABELS);
		}
		catch (Exception e)
		{
			fail("importing xforms with two grids with the same label should not fail?");
		}
	}
	
	public void testFormTemplateFromXFormsImport() throws Exception
	{
		verifyXformsToFormTemplateConversion(TestBulletinFromXFormsLoader.getFormTitle(), createFieldSpecCollectionWithDefaultTopFields(), TestBulletinFromXFormsLoader.getEmptyXFormsModelXmlAsString());
		verifyXformsToFormTemplateConversion(TestBulletinFromXFormsLoaderConstants.SECURE_APP_FORM_TITLE, createFieldSpecCollection(), TestBulletinFromXFormsLoaderConstants.XFORMS_MODEL_MESSAGE_FIELD);
		verifyXformsToFormTemplateConversion(TestBulletinFromXFormsLoaderConstants.SECURE_APP_FORM_TITLE, createFieldSpecCollectionWithAllFields(), TestBulletinFromXFormsLoaderConstants.COMPLETE_XFORMS_MODEL);
	}

	private void verifyXformsToFormTemplateConversion(String expectedTitle, FieldSpecCollection expectedFields, String xFormsModel) throws Exception
	{
		FormTemplate formTemplate = FormTemplateFromXFormsLoader.createNewBulletinFromXFormsFormTemplate(xFormsModel);
		assertTrue("Form template generated from xforms is not valid", formTemplate.isvalidTemplateXml());
		assertFalse("Form template title should not be empty?", formTemplate.getTitle().isEmpty());
		assertEquals("Form template title does not match bulletin's title?", expectedTitle, formTemplate.getTitle());
		assertEquals("should not encounter errors during conversion?", 0, formTemplate.getErrors().size());
		assertEquals("incorrect fields were converted?", expectedFields.toXml(), formTemplate.getTopFields().toXml());
	}
	
	private FieldSpecCollection createFieldSpecCollection()
	{
		FieldSpecCollection allFields = createFieldSpecCollectionWithDefaultTopFields();
		allFields.add(FieldSpec.createCustomField("XFormUnGrouped1", "+", new FieldTypeSectionStart()));
		allFields.add(FieldSpec.createCustomField("message", "This should be a Message Field", new FieldTypeMessage()));
		
		return allFields;
	}
	
	private FieldSpecCollection createFieldSpecCollectionWithAllFields() throws Exception
	{
		FieldSpecCollection allFields = createFieldSpecCollectionWithDefaultTopFields();
		allFields.add(FieldSpec.createCustomField("nm_Section_1__Text_fields_TagSection", "Section 1 (Text fields)", new FieldTypeSectionStart()));
		allFields.add(FieldSpec.createCustomField("name", "What is your name:", new FieldTypeNormal()));
		allFields.add(FieldSpec.createCustomField("nationality", "What is your country of origin:", new FieldTypeNormal()));
		allFields.add(FieldSpec.createCustomField("age", "What is your age:", new FieldTypeNormal()));
		allFields.add(FieldSpec.createCustomField("nm_Section_2__Date_field_TagSection", "Section 2 (Date field)", new FieldTypeSectionStart()));
		allFields.add(FieldSpec.createCustomField("date", "Date of incident", new FieldTypeDate()));
		allFields.add(FieldSpec.createCustomField("nm_Section_3__Drop_down_lists_TagSection", "Section 3 (Drop down lists)", new FieldTypeSectionStart()));
		allFields.add(createSourceOfRecordInfoDropdown());
		allFields.add(createRegiondsDropDown());
		allFields.add(FieldSpec.createCustomField("nm_Section_4__Check_boxes_TagSection", "Section 4 (Check boxes)", new FieldTypeSectionStart()));
		allFields.add(FieldSpec.createCustomField("anonymous", "Does interviewee wish to remain anonymous?", new FieldTypeBoolean()));
		allFields.add(FieldSpec.createCustomField("additionalInfo", "Is interviewee willing to give additional information if needed?", new FieldTypeBoolean()));
		allFields.add(FieldSpec.createCustomField("testify", "Is interviewee willing to testify?", new FieldTypeBoolean()));
		allFields.add(FieldSpec.createCustomField("nm_victim_informationTagSection", "Section 5 (Repeating group of fields)", new FieldTypeSectionStart()));		
		allFields.add(createGrid());
		
		return allFields;
	}

	private FieldSpec createGrid() throws Exception
	{
		GridFieldSpec gridFieldSpec = new GridFieldSpec();
		gridFieldSpec.setTag("nm_victim_informationTagGrid");
		gridFieldSpec.setLabel("Section 5 (Repeating group of fields)");
		
		FieldSpecCollection columnFieldSpecs = createVictimFieldsForGrid();

		gridFieldSpec.addColumns(columnFieldSpecs);
		
		return gridFieldSpec;
	}

	private FieldSpecCollection createVictimFieldsForGrid()
	{
		FieldSpecCollection columnFieldSpecs = new FieldSpecCollection();
		columnFieldSpecs.add(FieldSpec.createCustomField("victimFirstName", "Victim first name", new FieldTypeNormal()));
		columnFieldSpecs.add(FieldSpec.createCustomField("victimLastName", "Victim last name", new FieldTypeNormal()));
		columnFieldSpecs.add(createDropdownFieldSpec("sex", "Victim Sex", new String[]{"", "Female", "Male", "Other"}));
		return columnFieldSpecs;
	}

	private DropDownFieldSpec createSourceOfRecordInfoDropdown()
	{		
		return createDropdownFieldSpec("sourceOfRecordInformation", "Source of record information", new String[]{"", "Media/Press", "Legal Report", "Personal Interview", "Other"});
	}

	private DropDownFieldSpec createRegiondsDropDown()
	{
		return createDropdownFieldSpec("eventLocation", "Event Location", new String[]{"", "Region 1", "Region 2", "Region 3",});
	}

	private DropDownFieldSpec createDropdownFieldSpec(String tag, String label, String[] choiceLabels)
	{
		ChoiceItem[] choiceItems = createChoiceItems(choiceLabels);
		DropDownFieldSpec dropdown = new DropDownFieldSpec(choiceItems);
		dropdown.setTag(tag);
		dropdown.setLabel(label);
		
		return dropdown;
	}

	private ChoiceItem[] createChoiceItems(String[] choiceLabels)
	{
		ReusableChoices choices = new ReusableChoices("", "");
		for (String choiceLabel : choiceLabels) {
			choices.add(new ChoiceItem("", choiceLabel));
		}
		
		return choices.getChoices();
	}

	private FieldSpecCollection createFieldSpecCollectionWithDefaultTopFields()
	{
		FieldSpecCollection allFields = new FieldSpecCollection();
		FieldSpecCollection defaultTopFieldSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		allFields.addAll(defaultTopFieldSpecs);

		return allFields;
	}
}
