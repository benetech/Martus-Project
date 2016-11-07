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
package org.martus.client.bulletinstore.converters;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.GridRow;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinXmlExportImportConstants;
import org.martus.common.field.MartusField;
import org.martus.common.field.MartusGridField;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;

public class TestBulletinAsXmlToJsonConverter extends AbstractTestBulletinAsXmlConverters
{
	public TestBulletinAsXmlToJsonConverter(String name)
	{
		super(name);
	}
	
	public void testConvertXmlToJson() throws Exception
	{
		verifySingleBulletin(getBulletinWithoutData());
		verifySingleBulletin(getBulletinWithBasicData());
		verifySingleBulletin(getBulletinWithDropDowns());
		
		Vector<Bulletin> bulletins = new Vector<>();
		bulletins.add(getBulletinWithoutData());
		bulletins.add(getBulletinWithBasicData());
		bulletins.add(getBulletinWithGridDataWithSingleRow());
		verifyMultipleBulletins(bulletins);
	}

	private void verifySingleBulletin(Bulletin bulletinToExport) throws Exception
	{
		Vector<Bulletin> bulletins = new Vector<>();
		bulletins.add(bulletinToExport);
		verifyMultipleBulletins(bulletins);
	}
	
	private void verifyMultipleBulletins(Vector<Bulletin> bulletins) throws Exception
	{
		String bulletinsAsXml = exportBulletinsAsXml(bulletins);
		String actualJsonString = new BulletinsAsXmlToJsonConverter().convertToJson(bulletinsAsXml);
		System.out.println(actualJsonString);
		JSONObject jsonObjectFromString = new JSONObject(actualJsonString);
		assertTrue("json should not be empty?", jsonObjectFromString.length() > 0);
		
		JSONObject martusBulletinsJsonObject = jsonObjectFromString.getJSONObject(BulletinXmlExportImportConstants.MARTUS_BULLETINS);
		assertNotNull("Should contain the root element?", martusBulletinsJsonObject);
		
		if (bulletins.size()  == 1)
		{
			JSONObject singleMartusBulletinJson = martusBulletinsJsonObject.getJSONObject(BulletinXmlExportImportConstants.MARTUS_BULLETIN);
			assertNotNull("incorrect number of martusbulletin elements?", singleMartusBulletinJson);
			verifyBulletin(bulletins.get(0), singleMartusBulletinJson);
		}
		else
		{
			JSONArray bulletinJsonArray = martusBulletinsJsonObject.getJSONArray(BulletinXmlExportImportConstants.MARTUS_BULLETIN);
			assertNotNull("incorrect number of martusbulletin elements?", bulletinJsonArray);
			assertEquals("Incorrect number json elments in array?", bulletins.size(), bulletinJsonArray.length());
			for (int index = 0; index < bulletins.size(); ++index)
			{
				Bulletin bulletinWithExptedData = bulletins.get(index);
				JSONObject bulletinJson = bulletinJsonArray.getJSONObject(index);
				verifyBulletin(bulletinWithExptedData, bulletinJson);
			}
		}
	}

	private void verifyBulletin(Bulletin bulletinWithExptedData, JSONObject bulletinJson) throws Exception
	{
		verifyBulletinMetadata(bulletinWithExptedData, bulletinJson);
		verifyPublicFieldSpecs(bulletinWithExptedData, bulletinJson);
		verifyPrivateFieldSpecs(bulletinJson);
		verifyFieldValues(bulletinWithExptedData, bulletinJson);
	}

	private void verifyFieldValues(Bulletin bulletinWithBasicData, JSONObject singleMartusBulletinJson) throws Exception
	{
		JSONObject fieldValuesJson = singleMartusBulletinJson.getJSONObject(BulletinXmlExportImportConstants.FIELD_VALUES);
		JSONArray fieldValuesArray = fieldValuesJson.getJSONArray(BulletinXmlExportImportConstants.FIELD);
		for (int index = 0; index < fieldValuesArray.length(); ++index)
		{
			JSONObject fieldJson = fieldValuesArray.getJSONObject(index);
			String fieldTag = fieldJson.getString(BulletinXmlExportImportConstants.TAG_ATTRIBUTE);
			String fieldValue = fieldJson.getString(BulletinXmlExportImportConstants.VALUE);
			MartusField field = bulletinWithBasicData.getField(fieldTag);
			String expectedFieldValue = field.getExportableData(miniLocalization);
			if (field.getType().isGrid())
				verifyGridField((MartusGridField)field, fieldJson);
			else
				assertEquals("incorrect field value?", expectedFieldValue, fieldValue);
		}		
	}

	private void verifyGridField(MartusGridField expectedGridField, JSONObject fieldJson) throws Exception
	{
		JSONObject valueJson = fieldJson.getJSONObject(BulletinXmlExportImportConstants.VALUE);
		JSONObject gridDataJson = valueJson.getJSONObject(GridData.GRID_DATA_TAG);
		GridData gridData = expectedGridField.getGridData();
		assertEquals("incorreect grid data column count?", gridData.getColumnCount(), gridDataJson.getInt(GridData.GRID_ATTRIBUTE_COLUMNS));
		
		JSONObject rowJson = gridDataJson.getJSONObject(GridRow.ROW_TAG);
		JSONArray jsonArray = rowJson.getJSONArray(GridRow.COLUMN_TAG);
		assertEquals("Incorrect number of grid rows?", 1, gridData.getRowCount());
		
		GridRow gridRow = gridData.getRow(0);
		for (int column = 0; column < gridRow.getColumnCount(); ++column)
		{
			String expectedGrieCellValue = gridRow.getCellText(column);
			assertEquals("Incorrect grid cell value?", expectedGrieCellValue, jsonArray.getString(column));
		}
	}

	private void verifyBulletinMetadata(Bulletin bulletinWithBasicData, JSONObject singleMartusBulletinJson)
	{
		JSONObject bulletinMetadataJsonObject = singleMartusBulletinJson.getJSONObject(BulletinXmlExportImportConstants.BULLETIN_META_DATA);
		verifyKeyValueStartsWith(bulletinMetadataJsonObject, BulletinXmlExportImportConstants.ACCOUNT_ID, bulletinWithBasicData.getBulletinHeaderPacket().getAccountId());
		verifyKeyValueStartsWith(bulletinMetadataJsonObject, BulletinXmlExportImportConstants.LOCAL_ID, bulletinWithBasicData.getLocalId());
		verifyKeyValuePairs(bulletinMetadataJsonObject, BulletinXmlExportImportConstants.BULLETIN_STATUS, bulletinWithBasicData.getStatus());
		verifyKeyValuePairs(bulletinMetadataJsonObject, BulletinXmlExportImportConstants.BULLETIN_STATUS_LOCALIZED, miniLocalization.getLabel("en", "status", bulletinWithBasicData.getStatus()));
		verifyKeyValuePairs(bulletinMetadataJsonObject, BulletinXmlExportImportConstants.BULLETIN_VERSION, bulletinWithBasicData.getVersion());
		verifyKeyValuePairs(bulletinMetadataJsonObject, BulletinXmlExportImportConstants.EXTENDED_HISTORY, "");
	}

	private void verifyPrivateFieldSpecs(JSONObject singleMartusBulletinJson)
	{
		FieldSpecCollection standardPrivateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		verifyFieldSpecs(singleMartusBulletinJson, standardPrivateFields, BulletinXmlExportImportConstants.PRIVATE_FIELD_SPECS);
	}

	private void verifyPublicFieldSpecs(Bulletin bulletin, JSONObject singleMartusBulletinJson)
	{
		FieldSpecCollection standardPublicFields = bulletin.getTopSectionFieldSpecs();
		verifyFieldSpecs(singleMartusBulletinJson, standardPublicFields, BulletinXmlExportImportConstants.MAIN_FIELD_SPECS);
	}

	private void verifyFieldSpecs(JSONObject singleMartusBulletinJson, FieldSpecCollection expectedFieldSpecs, String mainFieldSpecsTag)
	{
		JSONObject mainFieldSpecJson = singleMartusBulletinJson.getJSONObject(mainFieldSpecsTag);
		JSONArray fieldSpecArray = mainFieldSpecJson.optJSONArray(BulletinXmlExportImportConstants.FIELD);
		if (fieldSpecArray != null) 
		{
			for (int index = 0; index < expectedFieldSpecs.size(); ++index)
			{
				verifyFieldSpecElement(expectedFieldSpecs.get(index), fieldSpecArray.getJSONObject(index));	
			}
		}
		else
		{
			assertEquals("Field Specs should only contain one field?", 1, expectedFieldSpecs.size());
			JSONObject singleFieldJson = mainFieldSpecJson.getJSONObject(BulletinXmlExportImportConstants.FIELD);
			verifyFieldSpecElement(expectedFieldSpecs.get(0), singleFieldJson);
		}
	}

	private void verifyFieldSpecElement(FieldSpec expectedFieldSpec, JSONObject fieldJsonObject)
	{
		assertEquals("Field label is incorrect?", expectedFieldSpec.getLabel(), fieldJsonObject.getString(FieldSpec.FIELD_SPEC_LABEL_XML_TAG));
		assertEquals("Field label is incorrect?", expectedFieldSpec.getTag(), fieldJsonObject.getString(FieldSpec.FIELD_SPEC_TAG_XML_TAG));
		assertEquals("Field label is incorrect?", expectedFieldSpec.getType().getTypeName(), fieldJsonObject.getString(FieldSpec.FIELD_SPEC_TYPE_ATTR));
	}

	private void verifyKeyValuePairs(JSONObject bulletinMetadataJsonObject,String key, int valueAsInt)
	{
		verifyKeyValuePairs(bulletinMetadataJsonObject, key, Integer.toString(valueAsInt));
	}

	private void verifyKeyValueStartsWith(JSONObject bulletinMetadataJsonObject, String key, String expectedValue)
	{
		assertEquals("incorrect value for key", bulletinMetadataJsonObject.getString(key), expectedValue);
	}

	private void verifyKeyValuePairs(JSONObject bulletinMetadataJsonObject, String key, String expectedValue)
	{
		assertEquals("incorrect value for key", expectedValue, bulletinMetadataJsonObject.getString(key));
	}
	
	private Bulletin getBulletinWithDropDowns() throws Exception
	{
		CustomDropDownFieldSpec dropdownSpec = new CustomDropDownFieldSpec();
		ChoiceItem[] choices = new ChoiceItem[] {
			new ChoiceItem("green", "Green"),
			new ChoiceItem("blue", "Blue"),
		};
		
		dropdownSpec.setChoices(choices);
		dropdownSpec.setTag("myDropDownTag");
		dropdownSpec.setLabel("myDropdownLabel");
		FieldSpecCollection dropdownSpecs = new FieldSpecCollection(new FieldSpec[] {dropdownSpec});
		FieldSpecCollection standardPrivateFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		Bulletin bulletin = new Bulletin(security, dropdownSpecs, standardPrivateFields);
		bulletin.set(dropdownSpec.getTag(), "blue");
		
		return bulletin;
	}
	
	private Bulletin getBulletinWithGridDataWithSingleRow() throws Exception
	{
		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setTag("grid");
		FieldSpec booleanFieldSpec = FieldSpec.createCustomField("received", "Did you recieve?", new FieldTypeBoolean());
		gridSpec.addColumn(booleanFieldSpec);
		
		FieldSpec stringFieldSpec = FieldSpec.createCustomField("FirstName", "What is your first Name?", new FieldTypeNormal());
		gridSpec.addColumn(stringFieldSpec);
		
		GridData gridData = new GridData(gridSpec, PoolOfReusableChoicesLists.EMPTY_POOL);
		gridData.addEmptyRow();
		gridData.addEmptyRow();
		gridData.setValueAt(FieldSpec.TRUESTRING, 0, 0);
		gridData.setValueAt("Almond Peanuts", 0, 1);
		
		FieldSpec[] publicSpecs = new FieldSpec[] {gridSpec};
		FieldSpecCollection privateSpecs = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		
		Bulletin bulletin = new Bulletin(store.getSignatureGenerator(), new FieldSpecCollection(publicSpecs), privateSpecs);
		bulletin.set(gridSpec.getTag(), gridData.getXmlRepresentation());

		return bulletin;
	}		
}
