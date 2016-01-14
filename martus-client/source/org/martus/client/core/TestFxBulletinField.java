/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.core;

import java.util.Vector;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;

import org.junit.Test;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.client.swingui.jfx.landing.bulletins.GridRowFields;
import org.martus.client.test.MockBulletinStore;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.GridRow;
import org.martus.common.MiniLocalization;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.DateFieldSpec;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeLanguage;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FieldTypeSectionStart;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.RequiredFieldIsBlankException;
import org.martus.util.TestCaseEnhanced;

public class TestFxBulletinField extends TestCaseEnhanced
{
	public TestFxBulletinField(String name)
	{
		super(name);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		
		localization = new MiniLocalization();
		security = MockMartusSecurity.createClient();
		fsc = new FieldSpecCollection();

		statesChoices = new ReusableChoices(STATES_CHOICES_TAG, "States");
		statesChoices.add(new ChoiceItem("WA", "Washington"));
		statesChoices.add(new ChoiceItem("OR", "Oregon"));
		fsc.addReusableChoiceList(statesChoices);

		citiesChoices = new ReusableChoices(CITIES_CHOICES_TAG, "Cities");
		citiesChoices.add(new ChoiceItem("SEA", "Seattle"));
		citiesChoices.add(new ChoiceItem("PDX", "Portland"));
		fsc.addReusableChoiceList(citiesChoices);
		store = new MockBulletinStore();
	}
	
	@Test
	public void testBasics() throws Exception
	{
		final String SAMPLE = "test";
		FieldSpec fieldSpec = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		fsc.add(fieldSpec);
		
		FxBulletinField field = FxBulletinField.createFxBulletinField(createFxBulletin(), fieldSpec, localization);
		assertEquals("tag", field.getTag());
		assertEquals("Label", field.getLabel());
		assertFalse(field.isRequiredField());
		assertFalse(field.isGrid());
		assertFalse(field.isSectionStart());
		assertFalse(field.isDropdown());
		assertEquals("", field.valueProperty().getValue());
		field.validate();

		field.valueProperty().setValue(SAMPLE);
		field.clear();
		assertNull(field.valueProperty().getValue());
	}
	
	public void testLanguage() throws Exception
	{
		FieldSpec languageSpec = new FieldTypeLanguage().createEmptyFieldSpec();
		fsc.add(languageSpec);

		FxBulletinField field = FxBulletinField.createFxBulletinField(createFxBulletin(), languageSpec, localization);
		Vector<ObservableChoiceItemList> choiceLists = field.getChoiceItemLists();
		assertEquals(1, choiceLists.size());
		ObservableChoiceItemList languageChoices = choiceLists.get(0);
		ChoiceItem english = languageChoices.findByCode("en");
		assertNotNull(english);
		
		field.validate();
	}
	
	public void testAddListener() throws Exception
	{
		final String SAMPLE = "test";
		FieldSpec fieldSpec = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		fsc.add(fieldSpec);
		
		FxBulletinField field = FxBulletinField.createFxBulletinField(createFxBulletin(), fieldSpec, localization);
		assertEquals("", field.valueProperty().getValue());
		field.addValueListener((observable, oldValue, newValue) -> 
		{
			assertEquals("", oldValue);
			assertEquals(SAMPLE, newValue);
		}); 
		field.valueProperty().setValue(SAMPLE);
	}
	
	public void testValidation() throws Exception
	{
		FieldSpec spec = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		spec.setRequired();
		fsc.add(spec);
		
		FxBulletinField field = FxBulletinField.createFxBulletinField(createFxBulletin(), spec, localization);
		assertTrue(field.isRequiredField());
		ObservableBooleanValue fieldIsValidProperty = field.fieldIsValidProperty();
		assertFalse(fieldIsValidProperty.getValue());

		try
		{
			field.validate();
			fail("Blank required field should have thrown");
		}
		catch(RequiredFieldIsBlankException ignoreExpected)
		{
		}
	}
	
	public void testValidateDateMinMax() throws Exception
	{
		DateFieldSpec spec = (DateFieldSpec) new FieldTypeDate().createEmptyFieldSpec();
		spec.setTag("CustomDateField");
		spec.setLabel("Custom date field");
		spec.setMinimumDate("2014-01-01");
		spec.setMaximumDate("2014-12-31");
		fsc.add(spec);
		
		FxBulletinField field = FxBulletinField.createFxBulletinField(createFxBulletin(), spec, localization);
		field.setValue("");
		field.validate();
		
		field.setValue("2014-07-01");
		field.validate();
		try
		{
			field.setValue("2013-12-31");
			field.validate();
			throw new Exception("Should have failed for blank date earlier than acceptable range");
		}
		catch(DataInvalidException ignoreExpected)
		{
		}

		try
		{
			field.setValue("2015-01-01");
			field.validate();
			throw new Exception("Should have failed for blank date later than acceptable range");
		}
		catch(DataInvalidException ignoreExpected)
		{
		}
	}

	public void testGrid() throws Exception
	{
		String gridTag = "grid";
		GridFieldSpec gridSpec2Colunns = new GridFieldSpec();
		gridSpec2Colunns.setTag(gridTag);
		gridSpec2Colunns.setLabel("Grid");
		FieldSpec normalGridColumn = FieldSpec.createCustomField("a", "Normal", new FieldTypeNormal());
		normalGridColumn.setRequired();
		gridSpec2Colunns.addColumn(normalGridColumn);
		gridSpec2Colunns.addColumn(FieldSpec.createCustomField("b", "Date", new FieldTypeDate()));
		gridSpec2Colunns.addColumn(FieldSpec.createCustomField("c", "Boolean", new FieldTypeBoolean()));
		fsc.add(gridSpec2Colunns);

		FxBulletin fxb = createFxBulletin();
		FxBulletinField field = FxBulletinField.createFxBulletinField(fxb, gridSpec2Colunns, localization);
		FxBulletinGridField gridField = (FxBulletinGridField) field;
		SimpleStringProperty gridValueProperty = gridField.valueProperty();

		try
		{
			gridField.fieldIsValidProperty();
			fail("fieldIsValidProperty should have thrown for grid");
		}
		catch(Exception ignoreExpected)
		{
		}
		
		try
		{
			gridField.gridColumnValuesProperty("No such column");
			fail("gridColumnValuesProperty should have thrown for no such column");
		}
		catch(Exception ignoreExpected)
		{
		}
		
		GridData data = createSampleGridData(gridSpec2Colunns);
		String sampleDataXml = data.getXmlRepresentation();
		assertEquals("", gridField.getValue());
		gridField.setValue(sampleDataXml);
		assertEquals(sampleDataXml, gridValueProperty.getValue());
		assertEquals(sampleDataXml, gridField.getValue());
		gridField.setValue(sampleDataXml);
		assertEquals(sampleDataXml, gridValueProperty.getValue());
		assertEquals(sampleDataXml, gridField.getValue());

		ObservableList<GridRowFields> gridData = gridField.gridDataProperty();
		assertEquals(1, gridData.size());
		GridRowFields gridRowFields = gridData.get(0);
		assertEquals(3, gridRowFields.size());
		assertEquals("Apple", gridRowFields.get("Normal").valueProperty().getValue());
		assertEquals("2012-03-18", gridRowFields.get("Date").valueProperty().getValue());
		assertEquals(FieldSpec.TRUESTRING, gridRowFields.get("Boolean").valueProperty().getValue());
		
		GridRowFields addedRow = gridField.appendEmptyGridRow();
		GridRow gridRow = FxBulletinField.convertGridRowFieldsToGridRow(gridSpec2Colunns, addedRow);
		assertTrue(gridRow.isEmptyRow());
		assertEquals(2, gridData.size());
		assertEquals(sampleDataXml, gridValueProperty.getValue());
		assertEquals(sampleDataXml, gridField.getValue());

		GridRowFields secondRow = gridData.get(1);
		secondRow.get("Date").setValue("2015-07-12");
		try
		{
			gridField.validate();
			fail("Should have thrown for blank required field inside grid");
		}
		catch(RequiredFieldIsBlankException ignoreExpected)
		{
		}
		assertNotEquals(sampleDataXml, gridValueProperty.getValue());
		assertNotEquals(sampleDataXml, gridField.getValue());
		gridField.removeGridRow(addedRow);
		assertEquals(1, gridData.size());
		assertEquals(sampleDataXml, gridValueProperty.getValue());
		assertEquals(sampleDataXml, gridField.getValue());
		try
		{
			gridField.removeGridRow(addedRow);
			fail("Should have thrown for removing a row that isn't in the grid");
		}
		catch(Exception ignoreExpected)
		{
		}
		
		gridField.removeGridRow(gridRowFields);
		assertEquals(0, gridData.size());
		assertEquals("", gridValueProperty.getValue());
		assertEquals("", gridField.getValue());
	}
	
	public void testSection() throws Exception
	{
		FieldSpec spec = FieldSpec.createCustomField("tag", "Label", new FieldTypeSectionStart());
		fsc.add(spec);
		
		FxBulletinField field = FxBulletinField.createFxBulletinField(createFxBulletin(), spec, localization);
		assertTrue(field.isSectionStart());
	}
	
	public void testSimpleDropdown() throws Exception
	{
		String simpleDropDownTag = "simple";
		ChoiceItem[] simpleChoices = new ChoiceItem[] {new ChoiceItem("a", "A"), new ChoiceItem("b", "B")};
		FieldSpec simpleDropDown = new DropDownFieldSpec(simpleChoices);
		simpleDropDown.setTag(simpleDropDownTag);
		fsc.add(simpleDropDown);
		
		FxBulletinField field = FxBulletinField.createFxBulletinField(createFxBulletin(), simpleDropDown, localization);
		assertTrue(field.isDropdown());

		Vector<ObservableChoiceItemList> simpleListOfLists = field.getChoiceItemLists();
		assertEquals(1, simpleListOfLists.size());
		ObservableChoiceItemList simpleList = simpleListOfLists.get(0);
		assertEquals(simpleChoices.length, simpleList.size());
		assertEquals(simpleChoices[0], simpleList.get(0));
	}
	
	public void testReusableDropdown() throws Exception
	{
		String reusableDropDownTag = "reusable";
		CustomDropDownFieldSpec reusableDropDown = new CustomDropDownFieldSpec();
		reusableDropDown.setTag(reusableDropDownTag);
		reusableDropDown.addReusableChoicesCode(CITIES_CHOICES_TAG);
		fsc.add(reusableDropDown);

		FxBulletin fxb = createFxBulletin();
		
		FxBulletinField field = FxBulletinField.createFxBulletinField(fxb, reusableDropDown, localization);
		assertTrue(field.isDropdown());

		Vector<ObservableChoiceItemList> reusableLists = field.getChoiceItemLists();
		assertEquals(1, reusableLists.size());
		ObservableChoiceItemList reusableList = reusableLists.get(0);
		assertEquals(citiesChoices.size()+1, reusableList.size());
		assertEquals("", reusableList.get(0).getCode());
		assertEquals(citiesChoices.get(0), reusableList.get(1));
	}
	
	public void testNestedDropdowns() throws Exception
	{
		String nestedDropDownTag = "nested";
		CustomDropDownFieldSpec nestedDropDown = new CustomDropDownFieldSpec();
		nestedDropDown.setTag(nestedDropDownTag);
		nestedDropDown.addReusableChoicesCode(STATES_CHOICES_TAG);
		nestedDropDown.addReusableChoicesCode(CITIES_CHOICES_TAG);
		fsc.add(nestedDropDown);
		
		fsc.add(nestedDropDown);
		FxBulletin fxb = createFxBulletin();
		
		FxBulletinField field = FxBulletinField.createFxBulletinField(fxb, nestedDropDown, localization);
		assertTrue(field.isDropdown());

		Vector<ObservableChoiceItemList> nestedLists = field.getChoiceItemLists();
		assertEquals(2, nestedLists.size());
		ObservableChoiceItemList nestedStatesList = nestedLists.get(0);
		assertEquals(statesChoices.size()+1, nestedStatesList.size());
		assertEquals("", nestedStatesList.get(0).getCode());
		assertEquals(statesChoices.get(0), nestedStatesList.get(1));
		ObservableChoiceItemList nestedCitiesList = nestedLists.get(1);
		assertEquals(citiesChoices.size()+1, nestedCitiesList.size());
		assertEquals("", nestedCitiesList.get(0).getCode());
		assertEquals(citiesChoices.get(0), nestedCitiesList.get(1));
		
	}

	public FxBulletin createFxBulletin() throws Exception
	{
		Bulletin b = new Bulletin(security, fsc, new FieldSpecCollection());
		FxBulletin fxb = new FxBulletin(localization);
		fxb.copyDataFromBulletin(b, store);
		return fxb;
	}

	private GridData createSampleGridData(GridFieldSpec gridSpec2Columns)
	{
		GridData gridData = new GridData(gridSpec2Columns, fsc.getAllReusableChoiceLists());
		GridRow gridRowSample = new GridRow(gridSpec2Columns, fsc.getAllReusableChoiceLists());
		gridRowSample.setCellText(0, "Apple");
		gridRowSample.setCellText(1, "2012-03-18");
		gridRowSample.setCellText(2, FieldSpec.TRUESTRING);
		gridData.addRow(gridRowSample);
		return gridData;
	}
	
	private static final String STATES_CHOICES_TAG = "states";
	private static final String CITIES_CHOICES_TAG = "cities";

	private MiniLocalization localization;
	private MartusSecurity security;
	private FieldSpecCollection fsc;
	private ReusableChoices statesChoices;
	private ReusableChoices citiesChoices;
	private MockBulletinStore store;
}
