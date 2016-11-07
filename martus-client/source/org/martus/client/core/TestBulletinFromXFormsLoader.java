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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.GridRow;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.bulletin.BulletinFromXFormsLoader;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeDropdown;
import org.martus.common.fieldspec.FieldTypeLanguage;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.MockBulletinStore;
import org.martus.util.TestCaseEnhanced;

import javafx.beans.property.ReadOnlyObjectWrapper;

public class TestBulletinFromXFormsLoader extends TestCaseEnhanced
{
	public TestBulletinFromXFormsLoader(String name)
	{
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		
		security = MockMartusSecurity.createClient();
		localization = new MiniLocalization();
		store = new MockBulletinStore(this);
		store.setSignatureGenerator(security);
	}
	
	private MiniLocalization getLocalization()
	{
		return localization;
	}
	
	public void testGroupWithoutLabel() throws Exception
	{
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(TestBulletinFromXFormsLoaderConstants.XFORMS_MODEL_WITH_GROUP_WITHOUT_LABEL);
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(TestBulletinFromXFormsLoaderConstants.XFORMS_INSTANCE_WITH_SINGLE_INPUT);
		try
		{
			BulletinFromXFormsLoader.createNewBulletinFromXFormsBulletin(bulletin);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Copying xForms Data should not have failed");
		}
	}
	
	public void testVerifyBulletinWithSomeFilledStandardFields() throws Exception
	{
		FieldSpecCollection someStandardFieldSpecs = getSomeRandomTopSectionFieldSpecs();
		Bulletin bulletin = new Bulletin(security, someStandardFieldSpecs, StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		bulletin.getFieldDataPacket().setXFormsModelAsString(getEmptyXFormsModelXmlAsString());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(getEmptyXFormsInstanceXmlAsString());
		fillStandardFieldsWithRandomValues(bulletin, someStandardFieldSpecs);
		
		bulletin = BulletinFromXFormsLoader.createNewBulletinFromXFormsBulletin(bulletin);
		FieldSpecCollection topSectionFieldSpecsWithoutSections = stripAllSectionFields(bulletin.getTopSectionFieldSpecs());
		assertEquals("Default fields were changed after loading from xforms?", someStandardFieldSpecs.size(), topSectionFieldSpecsWithoutSections.size());
		assertNotEquals("Some Default fields equalled the total # of default possible fields?", StandardFieldSpecs.getDefaultTopSectionFieldSpecs().size(), topSectionFieldSpecsWithoutSections.size());
		
		verifyFieldValues(bulletin, someStandardFieldSpecs);
	}

	public void testVerifyBulletinWithEmptyStandardFields() throws Exception
	{
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(getEmptyXFormsModelXmlAsString());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(getEmptyXFormsInstanceXmlAsString());
		
		bulletin = BulletinFromXFormsLoader.createNewBulletinFromXFormsBulletin(bulletin);
		FieldSpecCollection topSectionFieldSpecsWithoutSections = stripAllSectionFields(bulletin.getTopSectionFieldSpecs());
		assertEquals("Default fields were changed after loading from xforms?", REQUIRED_FOUR_STANDARD_FIELDS_COUNT, topSectionFieldSpecsWithoutSections.size());
	}
	
	public void testVerifyBulletinWithFilledStandardFields() throws Exception
	{
		Bulletin bulletin = new Bulletin(security);
		FieldSpecCollection standardFieldSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		fillStandardFieldsWithRandomValues(bulletin, standardFieldSpecs);
		
		bulletin.getFieldDataPacket().setXFormsModelAsString(getEmptyXFormsModelXmlAsString());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(getEmptyXFormsInstanceXmlAsString());
		bulletin = BulletinFromXFormsLoader.createNewBulletinFromXFormsBulletin(bulletin);
		FieldSpecCollection topSectionFieldSpecsWithoutSections = stripAllSectionFields(bulletin.getTopSectionFieldSpecs());
		assertEquals("Default fields were changed after loading from xforms?", standardFieldSpecs.size(), topSectionFieldSpecsWithoutSections.size());
		
		verifyFieldValues(bulletin, standardFieldSpecs);
	}
	
	private void verifyFieldValues(Bulletin bulletin, FieldSpecCollection standardFieldSpecs)
	{
		for (int index = 0; index < standardFieldSpecs.size(); ++index)
		{
			FieldSpec standardField = standardFieldSpecs.get(index);
			String value = bulletin.get(standardField.getTag());
			assertEquals("Standard field value changed after loading from xforms?", createExpectedRandomValue(standardField), value);
		}
	}

	private void fillStandardFieldsWithRandomValues(Bulletin bulletin, FieldSpecCollection standardFieldSpecs)
	{
		for (int index = 0; index < standardFieldSpecs.size(); ++index)
		{
			FieldSpec standardField = standardFieldSpecs.get(index);
			bulletin.set(standardField.getTag(), createExpectedRandomValue(standardField));
		}
	}

	private String createExpectedRandomValue(FieldSpec standardField)
	{
		return "Some Random data for" + standardField.getTag();
	}
	
	private FieldSpecCollection stripAllSectionFields(FieldSpecCollection fieldSpecs)
	{
		FieldSpecCollection fieldSpecsWithoutSections = new FieldSpecCollection();
		for (int index = 0; index < fieldSpecs.size(); ++index)
		{
			FieldSpec fieldSpec = fieldSpecs.get(index);
			if (fieldSpec.getType().isSectionStart())
				continue;
			
			fieldSpecsWithoutSections.add(fieldSpec);
		}
		
		return fieldSpecsWithoutSections;
	}

	public void testFxBulletinWithXFormsWithOneInputField() throws Exception
	{
		FxBulletin fxBulletin = new FxBulletin(getLocalization());
		assertEquals("FxBulletin field specs should be filled?", 0, fxBulletin.getFieldSpecs().size());
		
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(getXFormsModelWithOneStringInputFieldXmlAsString());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(getXFormsInstanceWithOneStringInputFieldXmlAsString());
		fxBulletin.copyDataFromBulletin(bulletin, store);
		assertEquals("FxBulletin filled from bulletin with data should have data?", getExpectedFieldCountWithNoSections(1), fxBulletin.getFieldSpecs().size());
		
		String TAG = "name";
		FieldSpec fieldSpec = fxBulletin.findFieldSpecByTag(TAG);
		assertTrue("Only field should be string?", fieldSpec.getType().isString());
		assertEquals("Incorrect field label?", FIELD_LABEL, fieldSpec.getLabel());
		assertEquals("Incorrect field tag?", TAG, fieldSpec.getTag());
		FxBulletinField field = fxBulletin.getField(fieldSpec);
		assertEquals("Incorrect field value?", FIELD_VALUE, field.getValue());
	}

	public void testFxBulletinWithXFormsWithOneDropdownFieldInsideGroup() throws Exception
	{
		FxBulletin fxBulletin = new FxBulletin(getLocalization());
		assertEquals("FxBulletin field specs should be filled?", 0, fxBulletin.getFieldSpecs().size());
		
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(getXFormsModelWithOneDropdownFieldInGroupXmlAsString());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(getXFormsInstanceWithOneDropdownFieldInGroupXmlAsString());
		fxBulletin.copyDataFromBulletin(bulletin, store);
		assertEquals("FxBulletin filled from bulletin with data should have data?", getExpectedFieldCountWithNoSections(4), fxBulletin.getFieldSpecs().size());
		
		String TAG = "MultiID";
		FieldSpec fieldSpec = fxBulletin.findFieldSpecByTag(TAG);
		assertEquals("Incorrect field label?", DROPDOWN_IN_GROUP_LABEL, fieldSpec.getLabel());
		assertEquals("Incorrect field tag?", TAG, fieldSpec.getTag());
		FxBulletinField field = fxBulletin.getField(fieldSpec);
		assertEquals("Incorrect field value?", DROPDOWN_IN_GROUP_OPTION1_LABEL, field.getValue());

		Bulletin updatedBulletin = BulletinFromXFormsLoader.createNewBulletinFromXFormsBulletin(bulletin);
		DropDownFieldSpec spec = (DropDownFieldSpec)updatedBulletin.getField(TAG).getFieldSpec();
		assertEquals(3, spec.getCount());
		assertEquals("", spec.getValue(0));
		assertEquals(DROPDOWN_IN_GROUP_OPTION1_LABEL, spec.getValue(1));
		assertEquals(DROPDOWN_IN_GROUP_OPTION2_LABEL, spec.getValue(2));
		assertEquals(XFORM_DROPDOWN_FIELD_XML, spec.toString());

	}
	
	public void testFxBulletinWithXFormsWithDropdownFieldInsideRepeatGroup() throws Exception
	{
		FxBulletin fxBulletin = new FxBulletin(getLocalization());
		assertEquals("FxBulletin field specs should be filled?", 0, fxBulletin.getFieldSpecs().size());
		
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(getXFormsModelWithDropdownFieldInRepeatGroupXmlAsString());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(getXFormsInstanceWithDropdownFieldInRepeatGroupXmlAsString());
		fxBulletin.copyDataFromBulletin(bulletin, store);
		assertEquals("FxBulletin filled from bulletin with data should have data?", getExpectedFieldCountWithNoSections(12), fxBulletin.getFieldSpecs().size());
		
		String TAG = "MultiID1";
		FieldSpec fieldSpec = fxBulletin.findFieldSpecByTag(TAG);
		assertEquals("Incorrect field label?", "MultiLabel1", fieldSpec.getLabel());
		assertEquals("Incorrect field tag?", "MultiID1", fieldSpec.getTag());
		FxBulletinField field = fxBulletin.getField(fieldSpec);
		assertEquals("Incorrect field value?", "OptionALabel1", field.getValue());

		TAG = "data_RepeatIDTagGrid";
		fieldSpec = fxBulletin.findFieldSpecByTag(TAG);
		assertEquals("Incorrect field label?", "RepeatLabel", fieldSpec.getLabel());
		assertEquals("Incorrect field tag?", "data_RepeatIDTagGrid", fieldSpec.getTag());
		 field = fxBulletin.getField(fieldSpec);
		 
		 String expectedFieldValud = "<GridData columns='4'>\n<Row>\n<Column>Repeat 1</Column>\n<Column>555</Column>\n<Column>2015-12-01</Column>\n<Column>OptionALabel2</Column>\n</Row>\n<Row>\n<Column>Item 2</Column>\n<Column>2</Column>\n<Column>2015-11-30</Column>\n<Column>OptionBLabel2</Column>\n</Row>\n</GridData>\n";
		 
		assertEquals("Incorrect field value for dropdown inside grid?", expectedFieldValud, field.getValue());
		
		Bulletin updatedBulletin = BulletinFromXFormsLoader.createNewBulletinFromXFormsBulletin(bulletin);
		GridFieldSpec gridspec = (GridFieldSpec)updatedBulletin.getField(TAG).getFieldSpec();
		CustomDropDownFieldSpec spec = (CustomDropDownFieldSpec)gridspec.getFieldSpec(3);
		assertEquals(3, spec.getCount());
		assertEquals("", spec.getValue(0));
		assertEquals("OptionALabel2", spec.getValue(1));
		assertEquals("OptionBLabel2", spec.getValue(2));
	}
	
	public void testNumberOfCustomFields() throws Exception
	{
		final int expectedNumberOfFieldTypes = 12;
		assertEquals("Unknown Field added. Must be supported in BulletinFromXFormsLoader", expectedNumberOfFieldTypes, FieldType.getNumberOfFieldTypes());
	}
	
	public void testNormalFieldTypes() throws Exception
	{
		assertTrue("Text are not Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_TEXT));
		assertTrue("Integer are not Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_INTEGER));
		assertTrue("Decimal are not Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_DECIMAL));
		assertFalse("Dates are Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_DATE));
		assertTrue("Time are not Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_TIME));
		assertTrue("Date/Time are not Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_DATE_TIME));
		assertFalse("Choice are Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_CHOICE));
		assertFalse("ChoiceList are Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_CHOICE_LIST));
		assertTrue("GeoPoint are not Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_GEOPOINT));
		assertTrue("Barcode are not Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_BARCODE));
		assertTrue("Binary are not Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_BINARY));
		assertTrue("Long are not Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_LONG));
		assertTrue("GeoSpace are not Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_GEOSHAPE));
		assertTrue("GeoTrace are not Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_GEOTRACE));
		assertFalse("Unsupported are Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_UNSUPPORTED));
		assertFalse("Null are Normal?", BulletinFromXFormsLoader.isNormalFieldType(Constants.DATATYPE_NULL));

	}
	
	public void testFxBulletinWithXFormsWithChoiceField() throws Exception
	{
		FxBulletin fxBulletin = new FxBulletin(getLocalization());
		verifyFieldSpecCount(fxBulletin, 0);
		
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(getXFormsModelWithOneChoiceInputFieldXmlAsString());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(getXFormsInstanceWithOneChoiceInputFieldXmlAsString());
		fxBulletin.copyDataFromBulletin(bulletin, store);
		verifyFieldSpecCount(fxBulletin, getExpectedFieldCountWithNoSections(1));

		FieldSpec fieldSpec = fxBulletin.findFieldSpecByTag(DROPDOWN_FIELD_TAG);
		verifyDropDownFieldSpecCreatedFromXFormsData(fieldSpec);
		verifyFieldCreatedFromXFormsData(fxBulletin.getField(fieldSpec));
	}
	
	
	public void testFxBulletinWithXFormsWithIntegerField() throws Exception
	{
		FxBulletin fxBulletin = new FxBulletin(getLocalization());
		verifyFieldSpecCount(fxBulletin, 0);
		
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(TestBulletinFromXFormsLoaderConstants.XFORMS_MODEL_INTERGER_FIELD);
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(TestBulletinFromXFormsLoaderConstants.XFORMS_INSTANCE_INTERGER_FIELD);
		fxBulletin.copyDataFromBulletin(bulletin, store);
		assertEquals("FxBulletin filled from bulletin with data should have data?", getExpectedFieldCountWithSections(1, 1), fxBulletin.getFieldSpecs().size());
		
		String TAG = "age";
		FieldSpec fieldSpec = fxBulletin.findFieldSpecByTag(TAG);
		assertTrue("Only field should be string?", fieldSpec.getType().isString());
		assertEquals("Incorrect field label?", TestBulletinFromXFormsLoaderConstants.AGE_LABEL, fieldSpec.getLabel());
		assertEquals("Incorrect field tag?", TAG, fieldSpec.getTag());
		FxBulletinField field = fxBulletin.getField(fieldSpec);
		assertEquals("Incorrect field value?", TestBulletinFromXFormsLoaderConstants.AGE_VALUE, field.getValue());
	}
	
	public void testFxBulletinWithXFormsWithMessageField() throws Exception
	{
		FxBulletin fxBulletin = new FxBulletin(getLocalization());
		verifyFieldSpecCount(fxBulletin, 0);
		
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(TestBulletinFromXFormsLoaderConstants.XFORMS_MODEL_MESSAGE_FIELD);
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(TestBulletinFromXFormsLoaderConstants.XFORMS_INSTANCE_MESSAGE_FIELD);
		fxBulletin.copyDataFromBulletin(bulletin, store);
		assertEquals("FxBulletin filled from bulletin with data should have data?", getExpectedFieldCountWithSections(1, 1), fxBulletin.getFieldSpecs().size());
		
		String TAG = "message";
		FieldSpec fieldSpec = fxBulletin.findFieldSpecByTag(TAG);
		assertTrue("Only field should be message?", fieldSpec.getType().isMessage());
		assertEquals("Incorrect field label?", TestBulletinFromXFormsLoaderConstants.MESSAGE_LABEL, fieldSpec.getLabel());
		assertEquals("Incorrect field tag?", TAG, fieldSpec.getTag());
		FxBulletinField field = fxBulletin.getField(fieldSpec);
		assertEquals("Incorrect field value?", "", field.getValue());
	}

	public void testFxBulletinWithXFormsEditing() throws Exception
	{
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(TestBulletinFromXFormsLoaderConstants.XFORMS_MODEL_INTERGER_FIELD);
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(TestBulletinFromXFormsLoaderConstants.XFORMS_INSTANCE_INTERGER_FIELD);

		FxBulletin fxBulletin = new FxBulletin(getLocalization());
		fxBulletin.copyDataFromBulletin(bulletin, store);

		final String XFORMS_AGE_TAG = "age";
		FieldSpec fieldSpec = fxBulletin.findFieldSpecByTag(XFORMS_AGE_TAG);
		assertTrue("Only field should be string?", fieldSpec.getType().isString());
		assertEquals("Incorrect field label?", TestBulletinFromXFormsLoaderConstants.AGE_LABEL, fieldSpec.getLabel());
		assertEquals("Incorrect field tag?", XFORMS_AGE_TAG, fieldSpec.getTag());
		FxBulletinField field = fxBulletin.getField(fieldSpec);
		assertEquals("Incorrect field value?", TestBulletinFromXFormsLoaderConstants.AGE_VALUE, field.getValue());

		final String newAge = "30";
		field.setValue(newAge);
		final String newTitle = "Some New Title";
		fxBulletin.getField(Bulletin.TAGTITLE).setValue(newTitle);
		
		fxBulletin.copyDataToBulletin(bulletin);
		assertFalse("xForms model/instance still exists?", bulletin.containsXFormsData());
		assertEquals("Title didn't update?", newTitle, bulletin.get(Bulletin.TAGTITLE));
		assertEquals("xForms Field wasn't updated?", newAge, bulletin.get(XFORMS_AGE_TAG));
	}

	private void verifyFieldSpecCount(FxBulletin fxBulletin, int expectedFieldSpecCount)
	{
		assertEquals("Incorrect field spec count?", expectedFieldSpecCount, fxBulletin.getFieldSpecs().size());
	}

	private void verifyDropDownFieldSpecCreatedFromXFormsData(FieldSpec fieldSpec)
	{
		assertTrue("Only field should be dropdown?", fieldSpec.getType().isDropdown());
		
		DropDownFieldSpec dropDownFieldSpec = (DropDownFieldSpec) fieldSpec;
		assertEquals("Incorrect drop down field label?", DROPDOWN_FIELD_LABEL, dropDownFieldSpec.getLabel());
		assertEquals("Incorrect drop down field tag?", DROPDOWN_FIELD_TAG, dropDownFieldSpec.getTag());
		List<ChoiceItem> expectedChoiceItems = getExpectedChoiceItems();
		List<ChoiceItem> actualChoiceItems = dropDownFieldSpec.getChoiceItemList();
		assertEquals("Incorrect choiceItem count", expectedChoiceItems.size(), actualChoiceItems.size());
		assertTrue("Incorrect choice items found in list?", expectedChoiceItems.containsAll(actualChoiceItems));
	}

	private void verifyFieldCreatedFromXFormsData(FxBulletinField field)
	{
		Vector<ObservableChoiceItemList> choiceItems = field.getChoiceItemLists();
		assertEquals("Incorrect number of choiceItems?", 1, choiceItems.size());
		assertEquals("Incorrect choice?", DROPDOWN_FIELD_CHOICE_PERSONAL_INTERVIEW_LABEL, field.getValue());
	}
	
	private List<ChoiceItem> getExpectedChoiceItems()
	{
		List<ChoiceItem> expectedChoiceItems = new ArrayList<ChoiceItem>();
		expectedChoiceItems.add(new ChoiceItem("",""));
		expectedChoiceItems.add(new ChoiceItem(DROPDOWN_FIELD_CHOICE_LEGAL_REPORT_LABEL, DROPDOWN_FIELD_CHOICE_LEGAL_REPORT_LABEL));
		expectedChoiceItems.add(new ChoiceItem(DROPDOWN_FIELD_CHOICE_MEDIA_PRESS_LABEL, DROPDOWN_FIELD_CHOICE_MEDIA_PRESS_LABEL));
		expectedChoiceItems.add(new ChoiceItem(DROPDOWN_FIELD_CHOICE_PERSONAL_INTERVIEW_LABEL, DROPDOWN_FIELD_CHOICE_PERSONAL_INTERVIEW_LABEL));
		
		return expectedChoiceItems;
	}
	
	public void testFxBulletinWithXFormsWithDateField() throws Exception
	{
		FxBulletin fxBulletin = new FxBulletin(getLocalization());

		assertEquals("FxBulletin field specs should be filled?", 0, fxBulletin.getFieldSpecs().size());
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(getXFormsModelWithDateInputField());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(getXFormsInstanceWithDateInputField());
		fxBulletin.copyDataFromBulletin(bulletin, store);
		assertEquals("FxBulletin filled from bulletin with data should have date field?", getExpectedFieldCountWithNoSections(1), fxBulletin.getFieldSpecs().size());
		
		FieldSpec fieldSpec = fxBulletin.findFieldSpecByTag("date");
		assertTrue("Incorrect field type?", fieldSpec.getType().isDate());
		
		FxBulletinField dateField = fxBulletin.getField(fieldSpec);
		assertEquals("Incorrect date?", DATE_VALUE, dateField.getValue());
	}
	
	public void testFxBulletinWithXFormsWithOptionalTopField() throws Exception
	{
		FxBulletin fxBulletin = new FxBulletin(getLocalization());

		assertEquals("FxBulletin field specs should be filled?", 0, fxBulletin.getFieldSpecs().size());
		Bulletin bulletin = new Bulletin(security);
		String keywords = "Fun Fun Fun";
		bulletin.set(Bulletin.TAGKEYWORDS, keywords);
		bulletin.getFieldDataPacket().setXFormsModelAsString(getXFormsModelWithDateInputField());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(getXFormsInstanceWithDateInputField());
		fxBulletin.copyDataFromBulletin(bulletin, store);
		assertEquals("FxBulletin filled from bulletin with data should have date field?", getExpectedFieldCountWithNoSections(2), fxBulletin.getFieldSpecs().size());
		
		FieldSpec fieldSpec = fxBulletin.findFieldSpecByTag(Bulletin.TAGKEYWORDS);
		
		FxBulletinField dataField = fxBulletin.getField(fieldSpec);
		assertEquals("Incorrect date?", keywords, dataField.getValue());
	}

	public void testFxBulletinWithXFormsBooleanField() throws Exception
	{		
		verifyBooleanFieldConversion(getXFormsInstanceWithSingleItemChoiceListAsTrueBoolean(), FieldSpec.TRUESTRING);
		verifyBooleanFieldConversion(getXFormsInstanceWithSingleItemChoiceListAsFalseBoolean(), FieldSpec.FALSESTRING);
		verifyBooleanFieldConversion(getXFormsInstanceWithSingleItemChoiceListAsNoValueBoolean(), FieldSpec.FALSESTRING);
	}
	
	public void testFxBulletinWithXFormGroupsAsSections() throws Exception
	{
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(getXFormsModelWithTwoGroups());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(getXFormsInstanceWithTwoGroups());

		FxBulletin fxBulletin = new FxBulletin(getLocalization());
		fxBulletin.copyDataFromBulletin(bulletin, store);
		Vector<FieldSpec> specs = fxBulletin.getFieldSpecs();
		boolean section1Found = false;
		boolean section2Found = false;
		FieldSpec fieldSpec1 = null;
		FieldSpec fieldSpec2 = null;
		for (Iterator iterator = specs.iterator(); iterator.hasNext();)
		{
			FieldSpec fieldSpec = (FieldSpec) iterator.next();
			if(fieldSpec.getType().isSectionStart())
			{
				if(fieldSpec.getLabel().equals(SECTION_LABEL_1))
				{
					section1Found = true;
					fieldSpec1 = fieldSpec;
				}
				if(fieldSpec.getLabel().equals(SECTION_LABEL_2))
				{
					section2Found = true;
					fieldSpec2 = fieldSpec;
				}
			}
		}
		assertTrue("Didn't find Section1?", section1Found);
		assertTrue("Didn't find Section2?", section2Found);
		assertNotEquals(fieldSpec1.getTag(), fieldSpec2.getTag());
	}

	private void verifyBooleanFieldConversion(String xFormsInstance, String expectedBooleanValue) throws Exception
	{
		FxBulletin fxBulletin = new FxBulletin(getLocalization());
		assertEquals("FxBulletin field specs should be filled?", 0, fxBulletin.getFieldSpecs().size());
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(getXFormsModelWithSingleItemChoiceListAsBoolean());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(xFormsInstance);
		fxBulletin.copyDataFromBulletin(bulletin, store);
		assertEquals("FxBulletin filled from bulletin with data should have date field?", getExpectedFieldCountWithSections(1, 1), fxBulletin.getFieldSpecs().size());
		
		FieldSpec fieldSpec = fxBulletin.findFieldSpecByTag("anonymous");
		assertTrue("Incorrect field type?", fieldSpec.getType().isBoolean());
		
		FxBulletinField dateField = fxBulletin.getField(fieldSpec);
		assertEquals("Incorrect date?", expectedBooleanValue, dateField.getValue());
	}

	private int getExpectedFieldCountWithNoSections(int expectedFieldsConverted)
	{
		int autogeneratedSectionForUngroupedFields = 1;
		return getExpectedFieldCountWithSections(expectedFieldsConverted, autogeneratedSectionForUngroupedFields);
	}
	
	private int getExpectedFieldCountWithSections(int expectedFieldsConverted, int expectedSections)
	{
		final int TOP_SECTION_DEFAULT_FIELD_COUNT = REQUIRED_FOUR_STANDARD_FIELDS_COUNT;
		return TOP_SECTION_DEFAULT_FIELD_COUNT + expectedSections + expectedFieldsConverted;
	}

	public void testFxBulletinWithXFormsRepeatField() throws Exception
	{
		FxBulletin fxBulletin = new FxBulletin(getLocalization());
		assertEquals("FxBulletin field specs should be filled?", 0, fxBulletin.getFieldSpecs().size());
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(getXFormsModelWithRepeats());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(getXFormsInstanceWithRepeats());
		fxBulletin.copyDataFromBulletin(bulletin, store);
		Vector<FieldSpec> fieldSpecs = fxBulletin.getFieldSpecs();
		assertEquals("FxBulletin filled from bulletin with data should have grid field?", getExpectedFieldCountWithNoSections(1), fieldSpecs.size());
		FieldSpec fieldSpec = fxBulletin.findFieldSpecByTag("nm_victim_informationTagGrid");
		verifyGridFieldSpec(fieldSpec);
		verifyGridFieldData(fxBulletin, fieldSpec);
	}
	
	public void testCustomDropDownFieldSpec() throws Exception
	{
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(TestBulletinFromXFormsLoaderConstants.COMPLETE_XFORMS_MODEL);
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(TestBulletinFromXFormsLoaderConstants.COMPLETE_XFORMS_INSTANCE);
	
		Bulletin converted = BulletinFromXFormsLoader.createNewBulletinFromXFormsBulletin(bulletin);
		CustomDropDownFieldSpec dropdownSpec = (CustomDropDownFieldSpec)converted.getField("sourceOfRecordInformation").getFieldSpec();
		assertEquals("Didn't get correct choice count?", 5, dropdownSpec.getCount());
	}

	
	
	public void testOwnershipOfXFormsRecordWhenCreatingACopy() throws Exception
	{
		MockMartusSecurity secureAppAccount = MockMartusSecurity.createOtherClient();
		MockMartusSecurity martusDesktopAccount = MockMartusSecurity.createHQ();
		store.setSignatureGenerator(martusDesktopAccount);	

		String secureAppPublicKey = secureAppAccount.getPublicKeyString();
		String martusDesktopPublicKey = martusDesktopAccount.getPublicKeyString();
		assertNotEquals(secureAppPublicKey, martusDesktopPublicKey);
		
		Bulletin secureAppBulletin = new Bulletin(secureAppAccount);
		secureAppBulletin.getFieldDataPacket().setXFormsModelAsString(getXFormsModelWithRepeats());
		secureAppBulletin.getFieldDataPacket().setXFormsInstanceAsString(getXFormsInstanceWithRepeats());

		HeadquartersKey desktopKey = new HeadquartersKey(martusDesktopAccount.getPublicKeyString());
		HeadquartersKeys keys = new HeadquartersKeys(desktopKey);
		secureAppBulletin.setAuthorizedToReadKeys(keys);
		store.saveBulletinForTesting(secureAppBulletin);
		UniversalId 	secureAppId = secureAppBulletin.getUniversalId();
		
		FxBulletin desktopFxBulletin = new FxBulletin(localization);
		
		assertEquals("Not secureApp Public Key?", secureAppPublicKey, secureAppBulletin.getAccount());
		desktopFxBulletin.copyDataFromBulletin(secureAppBulletin, store);
		ReadOnlyObjectWrapper<UniversalId> universalIdProperty = desktopFxBulletin.universalIdProperty();
		assertEquals("UniversalId not change after we copied data from the bulletin", secureAppId, universalIdProperty.get());
		assertEquals("Now should be Public Key after copying data.", secureAppPublicKey, secureAppBulletin.getAccount());

		Bulletin desktopEditedBulletin = new Bulletin(martusDesktopAccount);
		desktopFxBulletin.copyDataToBulletin(desktopEditedBulletin);
		assertEquals("After editing desktop should own this bulletin", martusDesktopPublicKey, desktopEditedBulletin.getAccount());
		
		store.setSignatureGenerator(security);
	}
	
	public void testXFormsRecordWithAttachments() throws Exception
	{
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(getEmptyXFormsModelXmlAsString());
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(getEmptyXFormsInstanceXmlAsString());

		verifyBulletinWithoutAttachments(bulletin);
		
		AttachmentProxy expectedPrivateAttachmentProxy = createAttachmentProxyWithTempFile();
		bulletin.addPrivateAttachment(expectedPrivateAttachmentProxy);
		
		AttachmentProxy expectedPublicAttachmentProxy = createAttachmentProxyWithTempFile();
		bulletin.addPublicAttachment(expectedPublicAttachmentProxy);
		
		verifyBulletinWithAttachments(bulletin, expectedPrivateAttachmentProxy, expectedPublicAttachmentProxy);
	}
	
	public void testFieldsAreUnderCorrectSections() throws Exception
	{
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(TestBulletinFromXFormsLoaderConstants.COMPLETE_XFORMS_MODEL);
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(TestBulletinFromXFormsLoaderConstants.COMPLETE_XFORMS_INSTANCE);

		Bulletin bulletinLoadedFromXForms = BulletinFromXFormsLoader.createNewBulletinFromXFormsBulletin(bulletin);
		FieldSpecCollection fieldSpecs = bulletinLoadedFromXForms.getFieldDataPacket().getFieldSpecs();
		
		String[] expectedFieldSpecSequence = new String[]{
		"language","author","title","entrydate",   
		"nm_Section_1__Text_fields_TagSection","name",	"nationality","age",
		"nm_Section_2__Date_field_TagSection","date",
		"nm_Section_3__Drop_down_lists_TagSection", "sourceOfRecordInformation", "eventLocation",
		"nm_Section_4__Check_boxes_TagSection", "anonymous","additionalInfo","testify",
		"nm_victim_informationTagSection","nm_victim_informationTagGrid"};
		
		for (int index = 0; index < fieldSpecs.size(); ++index)
		{
			String actualTag = fieldSpecs.get(index).getTag();
			String expectedTag = expectedFieldSpecSequence[index];
			assertEquals("Incorrect sequence position within list?", expectedTag, actualTag);
		}
	}
	
	public void testGridAppearsUnderOwnSection() throws Exception
	{
		Bulletin bulletin = new Bulletin(security);
		bulletin.getFieldDataPacket().setXFormsModelAsString(TestBulletinFromXFormsLoaderConstants.COMPLETE_XFORMS_MODEL);
		bulletin.getFieldDataPacket().setXFormsInstanceAsString(TestBulletinFromXFormsLoaderConstants.COMPLETE_XFORMS_INSTANCE);

		Bulletin bulletinLoadedFromXForms = BulletinFromXFormsLoader.createNewBulletinFromXFormsBulletin(bulletin);
		FieldSpecCollection fieldSpecs = bulletinLoadedFromXForms.getFieldDataPacket().getFieldSpecs();
		
		FieldSpecCollection sectionFieldSpecs = getOnlySectionFieldSpecs(fieldSpecs);
		
		assertEquals("Incorrect field section start types", 5, sectionFieldSpecs.size());
		assertTrue("should contain section label?", verifyContainsSectionName(sectionFieldSpecs, "Section 1 (Text fields)"));
		assertTrue("should contain section label?", verifyContainsSectionName(sectionFieldSpecs, "Section 2 (Date field)"));
		assertTrue("should contain section label?", verifyContainsSectionName(sectionFieldSpecs, "Section 3 (Drop down lists)"));
		assertTrue("should contain section label?", verifyContainsSectionName(sectionFieldSpecs, "Section 4 (Check boxes)"));
		assertTrue("should contain section label?", verifyContainsSectionName(sectionFieldSpecs, "Section 5 (Repeating group of fields)"));
		assertTrue("should contain grid label?", verifyGridContainsLabel(fieldSpecs, "Section 5 (Repeating group of fields)"));
	}

	private FieldSpecCollection getOnlySectionFieldSpecs(FieldSpecCollection fieldSpecs)
	{
		FieldSpecCollection sectionFieldSpecs = new FieldSpecCollection();
		for (int index = 0; index < fieldSpecs.size(); ++index)
		{
			FieldSpec fieldSpec = fieldSpecs.get(index);
			if (fieldSpec.getType().isSectionStart())
				sectionFieldSpecs.add(fieldSpec);
		}
		
		return sectionFieldSpecs;
	}

	private boolean verifyContainsSectionName(FieldSpecCollection sectionFieldSpecs, String sectionLabelToMatch)
	{
		for (int index = 0; index < sectionFieldSpecs.size(); ++index)
		{
			FieldSpec sectionFieldSpec = sectionFieldSpecs.get(index);
			if (sectionFieldSpec.getLabel().equals(sectionLabelToMatch))
				return true;
		}
		return false;
	}
	
	private boolean verifyGridContainsLabel(FieldSpecCollection sectionFieldSpecs, String labelToMatch)
	{
		for (int index = 0; index < sectionFieldSpecs.size(); ++index)
		{
			FieldSpec fieldSpec = sectionFieldSpecs.get(index);
			if(fieldSpec.getType().isGrid())
			{
				GridFieldSpec gridSpec = (GridFieldSpec)fieldSpec;
				if (gridSpec.getLabel().equals(labelToMatch))
					return true;
			}
		}
		return false;
	}

	private AttachmentProxy createAttachmentProxyWithTempFile() throws IOException
	{
		return new AttachmentProxy(createTempFile());
	}

	private void verifyBulletinWithAttachments(Bulletin bulletin, AttachmentProxy expectedPrivateAttachmentProxy, AttachmentProxy expectedPublicAttachmentProxy) throws Exception
	{
		bulletin = BulletinFromXFormsLoader.createNewBulletinFromXFormsBulletin(bulletin);
		AttachmentProxy[] publicAttachments = bulletin.getPublicAttachments();
		assertEquals("There should be no public attachments?", 1, publicAttachments.length);
		assertEquals("Incorrect public attachment proxy?", expectedPublicAttachmentProxy, publicAttachments[0]);
		
		AttachmentProxy[] privateAttachments = bulletin.getPrivateAttachments();
		assertEquals("There should be no private attachments?", 1, privateAttachments.length);
		assertEquals("Incorrect private attachment proxy?", expectedPrivateAttachmentProxy, privateAttachments[0]);
	}

	private void verifyBulletinWithoutAttachments(Bulletin bulletin) throws Exception
	{
		bulletin = BulletinFromXFormsLoader.createNewBulletinFromXFormsBulletin(bulletin);
		assertEquals("There should be no public attachments?", 0, bulletin.getPublicAttachments().length);
		assertEquals("There should be no private attachments?", 0, bulletin.getPrivateAttachments().length);
	}

	private void verifyGridFieldData(FxBulletin fxBulletin, FieldSpec fieldSpec) throws Exception
	{
		FxBulletinGridField fxBulletinGridField = (FxBulletinGridField) fxBulletin.getField(fieldSpec);
		GridData gridData = new GridData(fxBulletinGridField.getGridFieldSpec(), fxBulletin.getAllReusableChoicesLists());
		gridData.setFromXml(fxBulletinGridField.getValue());
		assertEquals("Incorrect grid row count?", 2, gridData.getRowCount());
	
		GridRow firstRow = gridData.getRow(0);
		assertEquals("incorrect grid column value", "John", firstRow.getCellText(0));
		assertEquals("incorrect grid column value", "Smith", firstRow.getCellText(1));
		assertEquals("incorrect grid column value", "Male", firstRow.getCellText(2));
		
		GridRow secondRow = gridData.getRow(1);
		assertEquals("incorrect grid column value", "Sunny", secondRow.getCellText(0));
		assertEquals("incorrect grid column value", "Dale", secondRow.getCellText(1));
		assertEquals("incorrect grid column value", "Other", secondRow.getCellText(2));
	}

	private void verifyGridFieldSpec(FieldSpec fieldSpec)
	{
		assertTrue("Incorrect field type?", fieldSpec.getType().isGrid());
		GridFieldSpec gridFieldSpec = (GridFieldSpec) fieldSpec;
		assertEquals("incorrect grid column count?", 3, gridFieldSpec.getColumnCount());
		assertEquals("incorrect fieldType?", new FieldTypeNormal(), gridFieldSpec.getColumnType(0));
		assertEquals("incorrect fieldType?", new FieldTypeNormal(), gridFieldSpec.getColumnType(1));
		assertEquals("incorrect fieldType?", new FieldTypeDropdown(), gridFieldSpec.getColumnType(2));
	}
	
	public static FieldSpecCollection getSomeRandomTopSectionFieldSpecs()
	{
		return new FieldSpecCollection(new FieldSpec[] 
			{
				FieldSpec.createStandardField(BulletinConstants.TAGLANGUAGE, new FieldTypeLanguage()),
				FieldSpec.createStandardField(BulletinConstants.TAGORGANIZATION, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGLOCATION, new FieldTypeNormal()), 
				FieldSpec.createStandardField(BulletinConstants.TAGEVENTDATE, new FieldTypeDateRange()),
				FieldSpec.createStandardField(BulletinConstants.TAGSUMMARY, new FieldTypeMultiline()),
			});
	}
	
	public static String getFormTitle()
	{
		return "XForms Sample";
	}
	
	public static String getEmptyXFormsModelXmlAsString()
	{
		return 	"		<xforms_model>" +
				"			<h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" >" +
				"				<h:head>" +
				"				<h:title>" + getFormTitle() + "</h:title>" +
				"					<model>" +
				"					<instance>" +
				"						<nm id=\"SampleForUnitTesting\" >" +
				"						</nm>" +
				"		            </instance>" +
				"		        </model>" +
				"		    </h:head>" +
				"		    <h:body>" +
				"		    </h:body>" +
				"		</h:html>" +
				"	</xforms_model>";
	}
	
	private static String getEmptyXFormsInstanceXmlAsString()
	{
		return "<xforms_instance>" +
				   "<nm id=\"SampleForUnitTesting\"></nm>" +
				"</xforms_instance>";
	}
	
	private static String getXFormsModelWithOneStringInputFieldXmlAsString()
	{
		return 	"		<xforms_model>" +
				"			<h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" >" +
				"				<h:head>" +
				"				<h:title>XForms Sample</h:title>" +
				"					<model>" +
				"					<instance>" +
				"						<nm id=\"SampleForUnitTesting\" >" +
				"							<name/>" +
				"						</nm>" +
				"		            </instance>" +
				"		            <bind nodeset=\"/nm/name\" type=\"string\" />" +
				"		        </model>" +
				"		    </h:head>" +
				"		    <h:body>" +
				"		            <input ref=\"name\" >" +
				"		                <label>" + FIELD_LABEL +  "</label>" +
				"		                <hint>(required)</hint>" +
				"		            </input>" +
				"		    </h:body>" +
				"		</h:html>" +
				"	</xforms_model>";
	}
	
	private static String getXFormsInstanceWithOneStringInputFieldXmlAsString()
	{
		return "<xforms_instance>" +
				   "<nm id=\"SampleForUnitTesting\">" +
				      "<name>" + FIELD_VALUE + "</name>" +
				   "</nm>" +
				"</xforms_instance>";
	}
	
	private static String DROPDOWN_IN_GROUP_LABEL = "MultiLabel";
	private static String DROPDOWN_IN_GROUP_OPTION1_LABEL = "option1Label";
	private static String DROPDOWN_IN_GROUP_OPTION2_LABEL = "option2Label";
	private static String DROPDOWN_IN_GROUP_OPTION1_CODE = "option1ID";
	private static String DROPDOWN_IN_GROUP_OPTION2_CODE = "option2ID";
	public static final String XFORM_DROPDOWN_FIELD_XML = "<Field type='DROPDOWN'>\n" +
			"<Tag>MultiID</Tag>\n" +
			"<Label>"+DROPDOWN_IN_GROUP_LABEL+"</Label>\n" +
			"<Choices>\n" +
			"<Choice>" +
			"</Choice>\n" +
			"<Choice>" +
			DROPDOWN_IN_GROUP_OPTION1_LABEL +
			"</Choice>\n" +
			"<Choice>" +
			DROPDOWN_IN_GROUP_OPTION2_LABEL +
			"</Choice>\n" +
			"</Choices>\n" +
			"</Field>\n";
	
	private static String getXFormsModelWithOneDropdownFieldInGroupXmlAsString()
	{
		return 
		"<h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:vellum=\"http://commcarehq.org/xforms/vellum\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:orx=\"http://openrosa.org/jr/xforms\" >"+
		"<h:head>"+
			"<h:title>Untitled Form</h:title>"+
			"<model>"+
				"<instance>"+
					"<data xmlns=\"http://openrosa.org/formdesigner/B1C7ECF2-1D47-4BC8-B883-C99CC14FEE90\" name=\"Untitled Form\" uiVersion=\"1\" xmlns:jrm=\"http://dev.commcarehq.org/jr/xforms\" version=\"1\" >"+
						"<GroupID>"+
							"<textID></textID>"+
							"<MultiID></MultiID>"+
							"<NumberID></NumberID>"+
							"<DateID></DateID>"+
						"</GroupID>"+
					"</data>"+
				"</instance>"+
				"<bind nodeset=\"/data/GroupID\" ></bind>"+
				"<bind nodeset=\"/data/GroupID/textID\" type=\"xsd:string\" ></bind>"+
				"<bind nodeset=\"/data/GroupID/MultiID\" ></bind>"+
				"<bind nodeset=\"/data/GroupID/NumberID\" type=\"xsd:int\" ></bind>"+
				"<bind nodeset=\"/data/GroupID/DateID\" type=\"xsd:date\" ></bind>"+
			"</model>"+
		"</h:head>"+
		"<h:body>"+
			"<group ref=\"/data/GroupID\" appearance=\"field-list\" >"+
				"<label>GroupLabel</label>"+
				"<input ref=\"/data/GroupID/textID\" >"+
					"<label>textLabel</label>"+
				"</input>"+
				"<select1 ref=\"/data/GroupID/MultiID\" >"+
					"<label>" + DROPDOWN_IN_GROUP_LABEL +"</label>"+
					"<item>"+
						"<label>"+DROPDOWN_IN_GROUP_OPTION1_LABEL+"</label>"+
						"<value>"+DROPDOWN_IN_GROUP_OPTION1_CODE+"</value>"+
					"</item>"+
					"<item>"+
					"<label>"+DROPDOWN_IN_GROUP_OPTION2_LABEL+"</label>"+
						"<value>"+DROPDOWN_IN_GROUP_OPTION2_CODE+"</value>"+
					"</item>"+
				"</select1>"+
				"<input ref=\"/data/GroupID/NumberID\" >"+
					"<label>NumberLabel</label>"+
				"</input>"+
				"<input ref=\"/data/GroupID/DateID\" >"+
					"<label>DateLabel</label>"+
				"</input>"+
			"</group>"+
		"</h:body>"+
		"</h:html>";
	}
	
	private static String getXFormsInstanceWithOneDropdownFieldInGroupXmlAsString()
	{
		return "<xforms_instance><data><GroupID><textID>Textanswer</textID><MultiID>"+DROPDOWN_IN_GROUP_OPTION1_CODE+"</MultiID><NumberID>1</NumberID><DateID>2015-12-03</DateID></GroupID></data></xforms_instance>";
	}
	
	private static String getXFormsModelWithDropdownFieldInRepeatGroupXmlAsString()
	{
		return 
		"<h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:vellum=\"http://commcarehq.org/xforms/vellum\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:orx=\"http://openrosa.org/jr/xforms\" >"+
		"<h:head>"+
		"<h:title>Untitled Form</h:title>"+
		"<model>"+
			"<instance>"+
				"<data xmlns:jrm=\"http://dev.commcarehq.org/jr/xforms\" xmlns=\"http://openrosa.org/formdesigner/4C9E839F-FA97-4755-BA92-4EBD5D6603EB\" uiVersion=\"1\" version=\"1\" name=\"Untitled Form\">"+
					"<OrphanMultiID1 />"+
					"<GroupID>"+
						"<TextID1 />"+
						"<NumberID1 />"+
						"<DateID1 />"+
						"<MultiID1 />"+
					"</GroupID>"+
					"<OrphanMultiID2 />"+
					"<RepeatID>"+
						"<TextID2 />"+
						"<NumberID2 />"+
						"<DateID2 />"+
						"<MultiID2 />"+
					"</RepeatID>"+
					"<OrphanMultiID3 />"+
				"</data>"+
			"</instance>"+
			"<bind nodeset=\"/data/OrphanMultiID1\" />"+
			"<bind nodeset=\"/data/GroupID\" />"+
			"<bind nodeset=\"/data/GroupID/TextID1\" type=\"xsd:string\" />"+
			"<bind nodeset=\"/data/GroupID/NumberID1\" type=\"xsd:int\" />"+
			"<bind nodeset=\"/data/GroupID/DateID1\" type=\"xsd:date\" />"+
			"<bind nodeset=\"/data/GroupID/MultiID1\" />"+
			"<bind nodeset=\"/data/OrphanMultiID2\" />"+
			"<bind nodeset=\"/data/RepeatID\" />"+
			"<bind nodeset=\"/data/RepeatID/TextID2\" type=\"xsd:string\" />"+
			"<bind nodeset=\"/data/RepeatID/NumberID2\" type=\"xsd:int\" />"+
			"<bind nodeset=\"/data/RepeatID/DateID2\" type=\"xsd:date\" />"+
			"<bind nodeset=\"/data/RepeatID/MultiID2\" />"+
			"<bind nodeset=\"/data/OrphanMultiID3\" />"+
		"</model>"+
	"</h:head>"+
	"<h:body>"+
		"<select1 ref=\"/data/OrphanMultiID1\">"+
			"<label>OrphanMultiLabel1</label>"+
			"<item>"+
				"<label>OrphanOptionALabel1</label>"+
				"<value>OrphanOptionAID1</value>"+
			"</item>"+
			"<item>"+
				"<label>OrphanOptionBLabel1</label>"+
				"<value>OrphanOptionBID1</value>"+
			"</item>"+
		"</select1>"+
		"<group ref=\"/data/GroupID\" appearance=\"field-list\">"+
			"<label>GroupLabel</label>"+
			"<input ref=\"/data/GroupID/TextID1\">"+
				"<label>TextLabel1</label>"+
			"</input>"+
			"<input ref=\"/data/GroupID/NumberID1\">"+
				"<label>NumberLabel1</label>"+
			"</input>"+
			"<input ref=\"/data/GroupID/DateID1\">"+
				"<label>DateLabel1</label>"+
			"</input>"+
			"<select1 ref=\"/data/GroupID/MultiID1\">"+
				"<label>MultiLabel1</label>"+
				"<item>"+
					"<label>OptionALabel1</label>"+
					"<value>OptionAID1</value>"+
				"</item>"+
				"<item>"+
					"<label>OptionBLabel1</label>"+
					"<value>OptionBID1</value>"+
				"</item>"+
			"</select1>"+
		"</group>"+
		"<select1 ref=\"/data/OrphanMultiID2\">"+
			"<label>OrphanMultiLabel2</label>"+
			"<item>"+
				"<label>OrphanOptionALabel2</label>"+
				"<value>OrphanOptionAID2</value>"+
			"</item>"+
			"<item>"+
				"<label>OrphanOptionBLabel2</label>"+
				"<value>OrphanOptionBID2</value>"+
			"</item>"+
		"</select1>"+
		"<group>"+
			"<label>RepeatLabel</label>"+
			"<repeat nodeset=\"/data/RepeatID\" appearance=\"field-list\">"+
				"<input ref=\"/data/RepeatID/TextID2\">"+
					"<label>TextLabel2</label>"+
				"</input>"+
				"<input ref=\"/data/RepeatID/NumberID2\">"+
					"<label>NumberLabel2</label>"+
				"</input>"+
				"<input ref=\"/data/RepeatID/DateID2\">"+
					"<label>DateLabel2</label>"+
				"</input>"+
				"<select1 ref=\"/data/RepeatID/MultiID2\">"+
					"<label>MultiLabel2</label>"+
					"<item>"+
						"<label>OptionALabel2</label>"+
						"<value>OptionAID2</value>"+
					"</item>"+
					"<item>"+
						"<label>OptionBLabel2</label>"+
						"<value>OptionBID2</value>"+
					"</item>"+
				"</select1>"+
			"</repeat>"+
		"</group>"+
		"<select1 ref=\"/data/OrphanMultiID3\">"+
			"<label>OrphanMultiLabel3</label>"+
			"<item>"+
				"<label>OrphanOptionALabel3</label>"+
				"<value>OrphanOptionAID3</value>"+
			"</item>"+
			"<item>"+
				"<label>OrphanOptionBLabel3</label>"+
				"<value>OrphanOptionBID3</value>"+
			"</item>"+
		"</select1>"+
	"</h:body>"+
"</h:html>";
}
	
	private static String getXFormsInstanceWithDropdownFieldInRepeatGroupXmlAsString()
	{
		return "<xforms_instance><data><OrphanMultiID1>OrphanOptionBID1</OrphanMultiID1><GroupID><TextID1>Group name</TextID1><NumberID1>123</NumberID1><DateID1>2015-12-02</DateID1><MultiID1>OptionAID1</MultiID1></GroupID><OrphanMultiID2>OrphanOptionAID2</OrphanMultiID2><RepeatID><TextID2>Repeat 1</TextID2><NumberID2>555</NumberID2><DateID2>2015-12-01</DateID2><MultiID2>OptionAID2</MultiID2></RepeatID><RepeatID><TextID2>Item 2</TextID2><NumberID2>2</NumberID2><DateID2>2015-11-30</DateID2><MultiID2>OptionBID2</MultiID2></RepeatID><OrphanMultiID3>OrphanOptionBID3</OrphanMultiID3></data></xforms_instance>";
	}
	
	private static String getXFormsModelWithOneChoiceInputFieldXmlAsString()
	{
		return 	"		<xforms_model>" +
				"			<h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" >" +
				"				<h:head>" +
				"				<h:title>XForms Sample</h:title>" +
				"					<model>" +
				"					<instance>" +
				"						<nm id=\"SampleForUnitTesting\" >" +
				" 							<" + DROPDOWN_FIELD_TAG +"/>"+			
				"						</nm>" +
				"		            </instance>" +
				" 					<bind nodeset=\"/nm/"+ DROPDOWN_FIELD_TAG + "\" type=\"select1\" ></bind>" +
				"		        </model>" +
				"		    </h:head>" +
				"		    <h:body>" +				
				" 				<select1 ref=\""+ DROPDOWN_FIELD_TAG + "\" appearance=\"minimal\" >" +
				"				<label>" + DROPDOWN_FIELD_LABEL + "</label>" +
				"					 <item>" +
				"						 <label>" + DROPDOWN_FIELD_CHOICE_MEDIA_PRESS_LABEL + "</label>" +
				"						 <value>" + DROPDOWN_FIELD_CHOICE_MEDIA_PRESS_CODE + "</value>" +
				" 					</item>" +
				" 					<item>" +
				" 						<label>" + DROPDOWN_FIELD_CHOICE_LEGAL_REPORT_LABEL + "</label>" +
				" 						<value>" + DROPDOWN_FIELD_CHOICE_LEGAL_REPORT_CODE + "</value>" +
				" 					</item>" +
				" 					<item>" +
				" 						<label>" + DROPDOWN_FIELD_CHOICE_PERSONAL_INTERVIEW_LABEL + "</label>" +
				" 						<value>" + DROPDOWN_FIELD_CHOICE_PERSONAL_INTERVIEW_CODE + "</value>" +
				" 					</item>" +
				" 				</select1>" +
				"		    </h:body>" +
				"		</h:html>" +
				"	</xforms_model>";
	}
	
	private static String getXFormsInstanceWithOneChoiceInputFieldXmlAsString()
	{
		return "<xforms_instance>" +
				   "<nm id=\"SampleForUnitTesting\">" +
				      "<sourceOfRecordInformation>" + DROPDOWN_FIELD_CHOICE_PERSONAL_INTERVIEW_CODE + "</sourceOfRecordInformation>" +
				   "</nm>" +
				"</xforms_instance>";
	}
	
	private static String getXFormsModelWithDateInputField()
	{
		return 
		"<h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" >" +
			"<h:head>" +
				"<h:title>secureApp Prototype</h:title>" +
				"<model>" +
					"<instance>" +
						"<nm id=\"VitalVoices\" >" +
							"<date></date>" +
							"</nm>" +
					"</instance>" +
				"<bind jr:constraintMsg=\"No dates before 2000-01-01 allowed\" nodeset=\"/nm/date\" constraint=\". >= date('2000-01-01')\" type=\"date\" ></bind>" +
				"</model>" +
			"</h:head>" +
			"<h:body>" +
					"<input ref=\"date\" >" +
						"<label>Date of incident</label>" +
						"<hint>(No dates before 2000-01-01 allowed)</hint>" +
					"</input>" +
			"</h:body>" +
		"</h:html>" ;
	}
	
	private static String getXFormsInstanceWithDateInputField()
	{
		return 
				"<nm id=\"VitalVoices\" >" +
				"<date>" + DATE_VALUE + "</date>" +
				"</nm>";
	}
	
	private static String getXFormsModelWithTwoGroups()
	{
		return 
		"<h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" >" +
			"<h:head>" +
				"<h:title>secureApp Prototype</h:title>" +
				"<model>" +
					"<instance>" +
						"<nm id=\"VitalVoices\" >" +
							"<date></date>" +
							"<age></age>"+
						"</nm>" +
					"</instance>" +
				"<bind jr:constraintMsg=\"No dates before 2000-01-01 allowed\" nodeset=\"/nm/date\" constraint=\". >= date('2000-01-01')\" type=\"date\" ></bind>" +
				"<bind nodeset=\"/nm/age\" type=\"integer\" ></bind>"+
				"</model>" +
			"</h:head>" +
			"<h:body>" +
		        "<group appearance=\"field-list\" >" +
	            "<label>"+SECTION_LABEL_1+"</label>" +
					"<input ref=\"date\" >" +
						"<label>Date of incident</label>" +
						"<hint>(No dates before 2000-01-01 allowed)</hint>" +
					"</input>" +
				"</group>" +
		        "<group appearance=\"field-list\" >" +
	            "<label>"+SECTION_LABEL_2+"</label>" +
					"<input ref=\"age\" >"+
						"<label>"+ TestBulletinFromXFormsLoaderConstants.AGE_LABEL +"</label>"+
					"</input>"+
				"</group>" +
			"</h:body>" +
		"</h:html>" ;
	}
	
	private static String getXFormsInstanceWithTwoGroups()
	{
		return 
				"<nm id=\"VitalVoices\" >" +
				"<date>" + DATE_VALUE + "</date>" +
				"<age>" + TestBulletinFromXFormsLoaderConstants.AGE_VALUE + "</age>"+
				"</nm>";
	}

	private static final String SECTION_LABEL_1 = "Section 4 (Check boxes)";
	private static final String SECTION_LABEL_2 = "Section 7 (Ages)";
	private static String getXFormsModelWithSingleItemChoiceListAsBoolean()
	{
		return "<h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" >" +
			    "<h:head>" +
			        "<h:title>secureApp Prototype</h:title>" +
			        "<model>" +
			            "<instance>" +
			                "<nm id=\"VitalVoices2\" >" +
			                    "<anonymous></anonymous>" +
			                "</nm>" +
			            "</instance>" +
			            "<bind nodeset=\"/nm/anonymous\" type=\"select\" ></bind>" +
			        "</model>" +
			    "</h:head>" +
			    "<h:body>" +
			        "<group appearance=\"field-list\" >" +
			            "<label>"+SECTION_LABEL_1+"</label>" +
			            "<select ref=\"anonymous\" >" +
			                "<label>Does interviewee wish to remain anonymous?</label>" +
			                "<item>" +
			                    "<label></label>" +
			                    "<value>1</value>" +
			                "</item>" +
			            "</select>" +
			        "</group>" +
			    "</h:body>" +
			"</h:html>";
	}
	
	private static String getXFormsInstanceWithSingleItemChoiceListAsTrueBoolean()
	{
		return "<nm id=\"VitalVoices2\" ><anonymous>1</anonymous></nm>";
	}
	
	private static String getXFormsInstanceWithSingleItemChoiceListAsFalseBoolean()
	{
		return "<nm id=\"VitalVoices2\" ><anonymous>0</anonymous></nm>";
	}
	
	private static String getXFormsInstanceWithSingleItemChoiceListAsNoValueBoolean()
	{
		return "<nm id=\"VitalVoices2\" ><anonymous/></nm>";
	}
	
	private static String getXFormsModelWithRepeats()
	{
		return	"<h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" >" +
			    "<h:head>" +
			        "<h:title>secureApp Prototype</h:title>" +
			        "<model>" +
			            "<instance>" +
			                "<nm id=\"VitalVoices\" >" +
			                    "<victim_information>" +
			                        "<victimFirstName></victimFirstName>" +
			                        "<victimLastName></victimLastName>" +
			                        "<sex></sex>" +
			                    "</victim_information>" +
			                "</nm>" +
			            "</instance>" +
			           	"<bind nodeset=\"/nm/victim_information/victimFirstName\" type=\"string\" ></bind>" +
			            "<bind nodeset=\"/nm/victim_information/victimLastName\" type=\"string\" ></bind>" +
			            "<bind nodeset=\"/nm/victim_information/sex\" type=\"select1\" ></bind>" +
			        "</model>" +
			    "</h:head>" +
			    "<h:body>" +
			            "<repeat nodeset=\"/nm/victim_information\" >" +
			                "<input ref=\"victimFirstName\" >" +
			                    "<label>Victim first name</label>" +
			                "</input>" +
			                "<input ref=\"victimLastName\" >" +
			                    "<label>Victim last name</label>" +
			                "</input>" +
			                "<select1 ref=\"sex\" appearance=\"minimal\" >" +
			                    "<label>Victim Sex</label>" +
			                    "<item>" +
			                        "<label>Female</label>" +
			                        "<value>female</value>" +
			                    "</item>" +
			                    "<item>" +
			                        "<label>Male</label>" +
			                        "<value>male</value>" +
			                    "</item>" +
			                    "<item>" +
			                        "<label>Other</label>" +
			                        "<value>other</value>" +
			                    "</item>" +
			                "</select1>" +
			            "</repeat>" +
			       
			    "</h:body>" +
			"</h:html>";
	}
	
	private static String getXFormsInstanceWithRepeats()
	{
		return 	"<nm id=\"VitalVoices\" >" +
				"<victim_information>" +
				"<victimFirstName>John</victimFirstName>" +
				"<victimLastName>Smith</victimLastName>" +
				"<sex>male</sex>" +
				"</victim_information>" +
				"<victim_information>" +
				"<victimFirstName>Sunny</victimFirstName>" +
				"<victimLastName>Dale</victimLastName>" +
				"<sex>other</sex>" +
				"</victim_information>" +
				"</nm>";
	}
	
	private static final String DROPDOWN_FIELD_TAG = "sourceOfRecordInformation";
	private static final String DROPDOWN_FIELD_LABEL = "Source of record information";
	private static final String DROPDOWN_FIELD_CHOICE_MEDIA_PRESS_CODE = "mediaPressCode";
	private static final String DROPDOWN_FIELD_CHOICE_LEGAL_REPORT_CODE = "legalReportCode";
	private static final String DROPDOWN_FIELD_CHOICE_PERSONAL_INTERVIEW_CODE = "personalInterviewCode";
	
	private static final String DROPDOWN_FIELD_CHOICE_MEDIA_PRESS_LABEL = "Media Press";
	private static final String DROPDOWN_FIELD_CHOICE_LEGAL_REPORT_LABEL = "Legal Report";
	private static final String DROPDOWN_FIELD_CHOICE_PERSONAL_INTERVIEW_LABEL = "Personal Interview";
	
	private static final String DATE_VALUE = "2015-03-24";
	
	private static final String FIELD_LABEL = "What is your name?";
	private static final String FIELD_VALUE = "John Johnson";

	private static final int REQUIRED_FOUR_STANDARD_FIELDS_COUNT = 4;

	private MockMartusSecurity security;
	private MiniLocalization localization;
	private MockBulletinStore store;
}
