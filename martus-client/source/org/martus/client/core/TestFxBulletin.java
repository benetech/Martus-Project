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

import java.io.File;
import java.util.Vector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.client.swingui.jfx.landing.bulletins.AttachmentTableRowData;
import org.martus.client.test.MockBulletinStore;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.GridRow;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.RequiredFieldIsBlankException;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.swing.FontHandler;
import org.martus.util.MultiCalendar;
import org.martus.util.TestCaseEnhanced;

public class TestFxBulletin extends TestCaseEnhanced
{
	public TestFxBulletin(String name)
	{
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		
		security = MockMartusSecurity.createClient();
		localization = new MiniLocalization();
		store = new MockBulletinStore();
	}
	
	public void testConstructorClearingMembers()
	{
		FxBulletin fxBulletin = new FxBulletin(getLocalization());
		assertEquals("Should be empty?", 0, fxBulletin.getAllReusableChoicesLists().size());
		assertEquals("Should be empty?", 0, fxBulletin.getAttachments().size());
		assertEquals("Should be empty?", 0, fxBulletin.getFieldSpecs().size());
		
		assertNull("Member should be null", fxBulletin.getHistory());
		assertNull("Member should be null", fxBulletin.getAuthorizedToReadList());
		assertNull("Member should be null", fxBulletin.bulletinLocalIdProperty());
		assertNull("Member should be null", fxBulletin.getHistory());
		assertNull("Member should be null", fxBulletin.versionProperty());

		assertFalse("Member should be false", fxBulletin.hasBeenModified());
		assertFalse("Member should be false", fxBulletin.hasBeenValidatedProperty().get());
		assertFalse("Member should be false", fxBulletin.isValidBulletin());
		assertFalse("Member should be false", fxBulletin.notAuthorizedToRead());		
	}
	
	public void testZawgyi() throws Exception
	{
		Bulletin b = new Bulletin(security);
		b.set(Bulletin.TAGAUTHOR, BURMESE_UNICODE_TEST_STRING);
		AttachmentProxy ap = new AttachmentProxy(BURMESE_UNICODE_TEST_STRING);
		UniversalId attachmentUid = UniversalId.createFromAccountAndLocalId("AccountId", "LocalId");
		ap.setUniversalIdAndSessionKey(attachmentUid, null);
		b.addPublicAttachment(ap);
		FxBulletin fxb = new FxBulletin(getLocalization());
		Bulletin copy = new Bulletin(security);
		
		FontHandler.setDoZawgyiConversion(false);
		fxb.copyDataFromBulletin(b, store);
		assertEquals(BURMESE_UNICODE_TEST_STRING, fxb.getField(Bulletin.TAGAUTHOR).getValue());
		assertEquals(BURMESE_UNICODE_TEST_STRING, fxb.getAttachments().get(0).getAttachmentProxy().getLabel());
		assertEquals(BURMESE_UNICODE_TEST_STRING, fxb.getAttachments().get(0).nameProperty().getValue());
		fxb.copyDataToBulletin(copy);
		assertEquals(BURMESE_UNICODE_TEST_STRING, copy.get(Bulletin.TAGAUTHOR));
		assertEquals(BURMESE_UNICODE_TEST_STRING, copy.getPrivateFieldDataPacket().getAttachments()[0].getLabel());
		
		try
		{
			FontHandler.setDoZawgyiConversion(true);
			fxb.copyDataFromBulletin(b, store);
			assertEquals(BURMESE_ZAWGYI_TEST_STRING, fxb.getField(Bulletin.TAGAUTHOR).getValue());
			assertEquals(BURMESE_ZAWGYI_TEST_STRING, fxb.getAttachments().get(0).getAttachmentProxy().getLabel());
			assertEquals(BURMESE_ZAWGYI_TEST_STRING, fxb.getAttachments().get(0).nameProperty().getValue());
			fxb.copyDataToBulletin(copy);
			assertEquals(BURMESE_UNICODE_TEST_STRING, copy.get(Bulletin.TAGAUTHOR));
			assertEquals(BURMESE_UNICODE_TEST_STRING, copy.getPrivateFieldDataPacket().getAttachments()[0].getLabel());
		}
		finally
		{
			FontHandler.setDoZawgyiConversion(false);
		}
	}
	
	public void testGrid() throws Exception
	{
		String gridTag = "grid";
		GridFieldSpec gridSpec2Colunns = new GridFieldSpec();
		gridSpec2Colunns.setTag(gridTag);
		gridSpec2Colunns.setLabel("Grid");
		gridSpec2Colunns.addColumn(FieldSpec.createCustomField("a", "Normal", new FieldTypeNormal()));
		gridSpec2Colunns.addColumn(FieldSpec.createCustomField("b", "Date", new FieldTypeDate()));
		gridSpec2Colunns.addColumn(FieldSpec.createCustomField("c", "Boolean", new FieldTypeBoolean()));

		FieldSpecCollection fsc = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		fsc.add(gridSpec2Colunns);

		Bulletin b = new Bulletin(security, fsc, new FieldSpecCollection());
		GridData data = createSampleGridData(gridSpec2Colunns, fsc);
		b.set(gridSpec2Colunns.getTag(), data.getXmlRepresentation());
		
		FxBulletin fxb = new FxBulletin(getLocalization());
		assertFalse(fxb.hasBeenModified());
		fxb.copyDataFromBulletin(b, store);
		assertFalse(fxb.hasBeenModified());
		fxb.validateData();
		
		try
		{
			fxb.fieldProperty(gridTag);
			fail("fieldProperty should have thrown for grid");
		}
		catch(Exception ignoreExpected)
		{
		}
		try
		{
			fxb.isValidProperty(gridTag);
			fail("isValidProperty should have thrown for grid");
		}
		catch(Exception ignoreExpected)
		{
		}
		
	}

	private GridData createSampleGridData(GridFieldSpec gridSpec2Colunns, FieldSpecCollection fsc)
	{
		GridData gridData = new GridData(gridSpec2Colunns, fsc.getAllReusableChoiceLists());
		GridRow gridRowSample = new GridRow(gridSpec2Colunns, fsc.getAllReusableChoiceLists());
		gridRowSample.setCellText(0, "Apple");
		gridRowSample.setCellText(1, "2012-03-18");
		gridRowSample.setCellText(2, FieldSpec.TRUESTRING);
		gridData.addRow(gridRowSample);
		return gridData;
	}
	
	public void testDataDrivenDropDown() throws Exception
	{
		String gridTag = "grid";
		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setTag(gridTag);
		gridSpec.setLabel("Grid");
		String firstNameColumnLabel = "First Name";
		gridSpec.addColumn(FieldSpec.createCustomField("FirstName", firstNameColumnLabel, new FieldTypeNormal()));
		
		String dropDownTag = "dropdown";
		CustomDropDownFieldSpec ddddSpec = new CustomDropDownFieldSpec();
		ddddSpec.setTag(dropDownTag);
		ddddSpec.setDataSource(gridTag, firstNameColumnLabel);
			
		FieldSpecCollection fsc = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		fsc.add(gridSpec);
		fsc.add(ddddSpec);

		Bulletin b = new Bulletin(security, fsc, new FieldSpecCollection());
		FxBulletin fxb = new FxBulletin(getLocalization());
		fxb.copyDataFromBulletin(b, store);
		Vector<ObservableChoiceItemList> listWithOneEmptyList = fxb.getField(ddddSpec).getChoiceItemLists();
		assertEquals(1, listWithOneEmptyList.size());
		ObservableChoiceItemList emptyList = listWithOneEmptyList.firstElement();
		assertEquals(1, emptyList.size());
		assertEquals("", emptyList.get(0).getCode());
		
		GridData gridData = new GridData(gridSpec, fsc.getAllReusableChoiceLists());
		assertEquals(0, gridData.getRowCount());
		String[] names = new String[] {"Chris", "Robin", "Starbuck",};
		for (String name : names)
		{
			GridRow gridRow = new GridRow(gridSpec, fsc.getAllReusableChoiceLists());
			gridRow.setCellText(0, name);
			gridData.addRow(gridRow);
		}
		assertEquals(names.length, gridData.getRowCount());
		b.set(gridTag, gridData.getXmlRepresentation());
		fxb.copyDataFromBulletin(b, store);

		Vector<ObservableChoiceItemList> listWithOneRealList = fxb.getField(ddddSpec).getChoiceItemLists();
		assertEquals(1, listWithOneRealList.size());
		ObservableChoiceItemList realList = listWithOneRealList.firstElement();
		assertEquals(names.length + 1, realList.size());
		
	}
	
	public void testGridColumnValuesProperty() throws Exception
	{
		String gridTag = "grid";
		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setTag(gridTag);
		gridSpec.setLabel("Grid");
		String firstNameColumnLabel = "First Name";
		gridSpec.addColumn(FieldSpec.createCustomField("FirstName", firstNameColumnLabel, new FieldTypeNormal()));
		
		FieldSpecCollection fsc = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		fsc.add(gridSpec);
		Bulletin b = new Bulletin(security, fsc, new FieldSpecCollection());
		FxBulletin fxb = new FxBulletin(getLocalization());
		fxb.copyDataFromBulletin(b, store);

		Vector<ObservableChoiceItemList> listOfLists = fxb.gridColumnValuesProperty(gridTag, firstNameColumnLabel);
		assertEquals(1, listOfLists.size());
		ObservableChoiceItemList realList = listOfLists.firstElement();
		final int EMPTY_CHOICE_ALWAYS_AVAILABLE = 1;
		assertEquals(EMPTY_CHOICE_ALWAYS_AVAILABLE, realList.size());
		assertEquals("", realList.get(0).getCode());
		assertEquals("", realList.get(0).getLabel());

		FxBulletinGridField grid = fxb.getGridField(gridTag);
		GridFieldData gridFieldData = grid.gridDataProperty();
		String sampleAlphabeticallyLater = "Lucy";
		gridFieldData.get(0).get(firstNameColumnLabel).setValue(sampleAlphabeticallyLater);
		assertEquals(2, realList.size());
		assertEquals("", realList.get(0).getCode());
		assertEquals("", realList.get(0).getLabel());
		assertEquals(sampleAlphabeticallyLater, realList.get(1).getCode());
		assertEquals(sampleAlphabeticallyLater, realList.get(1).getLabel());
		
		grid.appendEmptyGridRow();
		final int STILL_2_BECAUSE_WE_TRUNCATE_TRAILING_EMPTY_ROWS = 2;
		assertEquals(STILL_2_BECAUSE_WE_TRUNCATE_TRAILING_EMPTY_ROWS, realList.size());
		
		String sampleAlphabeticallyEarlier = "Fred";
		gridFieldData.get(1).get(firstNameColumnLabel).setValue(sampleAlphabeticallyEarlier);
		assertEquals(3, realList.size());
		assertEquals("", realList.get(0).getCode());
		assertEquals("", realList.get(0).getLabel());
		assertEquals(sampleAlphabeticallyEarlier, realList.get(1).getCode());
		assertEquals(sampleAlphabeticallyEarlier, realList.get(1).getLabel());
		assertEquals(sampleAlphabeticallyLater, realList.get(2).getCode());
		assertEquals(sampleAlphabeticallyLater, realList.get(2).getLabel());
		
		gridFieldData.remove(0);
		assertEquals(2, realList.size());
		assertEquals("", realList.get(0).getCode());
		assertEquals("", realList.get(0).getLabel());
		assertEquals(sampleAlphabeticallyEarlier, realList.get(1).getCode());
		assertEquals(sampleAlphabeticallyEarlier, realList.get(1).getLabel());
	}
	
	public void testBasics() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b, store);
		
		try
		{
			fxb.fieldProperty("Tag that does not exist");
			fail("fieldProperty should have thrown getting property for missing field");
		}
		catch(NullPointerException ignoreExpected)
		{
		}

		try
		{
			fxb.isValidProperty("Tag that does not exist");
			fail("isValidProperty should have thrown getting property for missing field");
		}
		catch(NullPointerException ignoreExpected)
		{
		}
	}
	
	public void testHasBeenValidated() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		assertFalse("Brand new bulletin was already validated?", fxb.hasBeenValidatedProperty().getValue());
		
		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b, store);
		assertFalse("New loaded bulletin was already validated?", fxb.hasBeenValidatedProperty().getValue());
		fxb.validateData();
		assertTrue("Validate didn't set the property?", fxb.hasBeenValidatedProperty().getValue());

		fxb.copyDataFromBulletin(b, store);
		assertFalse("Load didn't reset the property?", fxb.hasBeenValidatedProperty().getValue());
	}
	
	public void testAttachments() throws Exception

	{	
		final byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
		final byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};
		final byte[] sampleBytes3 = {6,5,0,4,7,5,5,4,4,0};
		final byte[] sampleBytes4 = {12,34,56};
	
		File tempFile1 = createTempFileWithData(sampleBytes1);
		File tempFile2 = createTempFileWithData(sampleBytes2);
		File tempFile3 = createTempFileWithData(sampleBytes3);
		File tempFile4 = createTempFileWithData(sampleBytes4);
	
		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		AttachmentProxy a3 = new AttachmentProxy(tempFile3);

		Bulletin b = new Bulletin(security);
		b.addPublicAttachment(a1);
		b.addPublicAttachment(a2);
		b.addPrivateAttachment(a3);
	
		assertEquals(2, b.getPublicAttachments().length);
		assertEquals(1, b.getPrivateAttachments().length);
	
		AttachmentProxy[] v = b.getPublicAttachments();
		assertEquals("a1 label", tempFile1.getName(), v[0].getLabel());
		assertEquals("a2 label", tempFile2.getName(), v[1].getLabel());
	
		AttachmentProxy[] vp = b.getPrivateAttachments();
		assertEquals("a3 label", tempFile3.getName(), vp[0].getLabel());
	
		FxBulletin fxb = new FxBulletin(getLocalization());
		fxb.copyDataFromBulletin(b, store);
		assertEquals(3, fxb.getAttachments().size());
	
		Bulletin modified = new Bulletin(security);
		fxb.copyDataToBulletin(modified);
		assertEquals(3, modified.getPrivateAttachments().length);
		assertEquals(0, modified.getPublicAttachments().length);
		
		ObservableList<AttachmentTableRowData> attachments = fxb.getAttachments();
		AttachmentProxy a4 = new AttachmentProxy(tempFile4);
		AttachmentTableRowData a4RowData = new AttachmentTableRowData(a4, store.getDatabase());
		attachments.add(a4RowData);
		
		Bulletin modifiedWith4Attachments = new Bulletin(security);
		fxb.copyDataToBulletin(modifiedWith4Attachments);
		assertEquals(4, modifiedWith4Attachments.getPrivateAttachments().length);
	}

	public void testDates() throws Exception
	{
		String DATE_TAG = Bulletin.TAGENTRYDATE;
		String DATE_RANGE_TAG = Bulletin.TAGEVENTDATE;

		String emptyDate = "";
		verifyDateOrDateRange(DATE_TAG, emptyDate, emptyDate);
		verifyDateOrDateRange(DATE_RANGE_TAG, emptyDate, emptyDate);
		
		String normalDate = "2012-07-16";
		verifyDateOrDateRange(DATE_TAG, normalDate, normalDate);
		verifyDateOrDateRange(DATE_RANGE_TAG, normalDate, normalDate);

		String unknownDate = "0001-01-01";
		verifyDateOrDateRange(DATE_TAG, "", unknownDate);
		verifyDateOrDateRange(DATE_RANGE_TAG, "", unknownDate);
		
		MultiCalendar unknown = MultiCalendar.createFromIsoDateString(unknownDate); 
		MartusFlexidate completelyUnknownFlexidate = new MartusFlexidate(unknown, unknown);
		verifyDateOrDateRange(DATE_RANGE_TAG, "", completelyUnknownFlexidate.getMartusFlexidateString());
		
		MultiCalendar normal = MultiCalendar.createFromIsoDateString(normalDate);
		String beginUnknownStoredRange = MartusFlexidate.toBulletinFlexidateFormat(unknown, normal);
		verifyDateOrDateRange(DATE_RANGE_TAG, beginUnknownStoredRange, beginUnknownStoredRange);

		String endUnknownStoredRange = MartusFlexidate.toBulletinFlexidateFormat(normal, unknown);
		verifyDateOrDateRange(DATE_RANGE_TAG, endUnknownStoredRange, endUnknownStoredRange);
	}

	public void verifyDateOrDateRange(String tag, String expected, String stored)
			throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		assertFalse(fxb.hasBeenModified());
		
		Bulletin b = new BulletinForTesting(security);
		b.set(tag, stored);
		fxb.copyDataFromBulletin(b, store);
		assertFalse(fxb.hasBeenModified());
		assertEquals(expected, fxb.fieldProperty(tag).getValue());
	}
	
	public void testHasBeenModified() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		assertFalse(fxb.hasBeenModified());
		
		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b, store);
		assertFalse(fxb.hasBeenModified());
		
		SimpleStringProperty authorProperty = fxb.fieldProperty(Bulletin.TAGAUTHOR);
		assertEquals("", authorProperty.getValue());
		
		authorProperty.setValue("Something else");
		assertTrue(fxb.hasBeenModified());
		
		authorProperty.setValue("");
		assertTrue(fxb.hasBeenModified());
	}
	
	public void testImmutableOnServerProperty() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		assertNull(fxb.getImmutableOnServerProperty());
		
		Bulletin bulletinWithImmutableOnServerNotSetInitially = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(bulletinWithImmutableOnServerNotSetInitially, store);
		BooleanProperty immutableOnServerProperty = fxb.getImmutableOnServerProperty();
		assertFalse(immutableOnServerProperty.get());
		immutableOnServerProperty.set(true);
		assertTrue(immutableOnServerProperty.get());
		Bulletin result1 = new Bulletin(security);
		fxb.copyDataToBulletin(result1);
		assertTrue(result1.getImmutableOnServer());
		
		FxBulletin fxb2 = new FxBulletin(getLocalization());
		Bulletin bulletinWithImmutableOnServerSetInitially = new BulletinForTesting(security);
		bulletinWithImmutableOnServerSetInitially.setImmutableOnServer(true);
		fxb2.copyDataFromBulletin(bulletinWithImmutableOnServerSetInitially, store);
		BooleanProperty immutableOnServerProperty2 = fxb2.getImmutableOnServerProperty();
		assertTrue(immutableOnServerProperty2.get());
		immutableOnServerProperty2.set(false);
		Bulletin result2 = new Bulletin(security);
		fxb2.copyDataToBulletin(result2);
		assertFalse("After a bulletin has this flag set it can be unset", result2.getImmutableOnServer());
	}
	
	public void testValidateRequiredField() throws Exception
	{
		String tag = Bulletin.TAGAUTHOR;

		FxBulletin fxb = new FxBulletin(getLocalization());
		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b, store);
		Vector<FieldSpec> specs = fxb.getFieldSpecs();
		specs.forEach(spec -> {if(spec.getTag().equals(tag)) spec.setRequired();});

		fxb.fieldProperty(tag).setValue("This is not blank");
		assertTrue(fxb.isValidProperty(tag).getValue());
		fxb.validateData();

		fxb.fieldProperty(tag).setValue("");
		assertFalse(fxb.isValidProperty(tag).getValue());
		try
		{
			fxb.validateData();
			fail("Should have thrown for blank required fields");
		}
		catch(RequiredFieldIsBlankException ignoreExpected)
		{
		}
	}
	
	public void testVersion() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		ReadOnlyIntegerProperty versionPropertyNull = fxb.versionProperty();
		assertEquals(null, versionPropertyNull);
		
		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b, store);
		ReadOnlyIntegerProperty versionProperty = fxb.versionProperty();
		assertEquals(Integer.valueOf(b.getVersion()), versionProperty.getValue());
		assertEquals(Integer.valueOf(1), versionProperty.getValue());
		
		Bulletin bulletinWith3Versions = new BulletinForTesting(security);
		bulletinWith3Versions.setImmutable();
		BulletinHistory localHistory = bulletinWith3Versions.getHistory();
		localHistory.add("history1");
		localHistory.add("history2");
		bulletinWith3Versions.setHistory(localHistory);
		assertEquals("Bulletin2 doesn't have 3 versions?", 3, bulletinWith3Versions.getVersion());
		fxb.copyDataFromBulletin(bulletinWith3Versions, store);
		assertEquals("This is a readOnlyInteger so it will not change", Integer.valueOf(1), versionProperty.getValue());
		versionProperty = fxb.versionProperty();
		assertEquals(Integer.valueOf(bulletinWith3Versions.getVersion()), versionProperty.getValue());
		assertEquals(Integer.valueOf(3), versionProperty.getValue());
	}
	
	public void testUniversalId() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		ReadOnlyObjectWrapper<UniversalId> universalIdPropertyNull = fxb.universalIdProperty();
		assertEquals(null, universalIdPropertyNull);
		
		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b, store);
		ReadOnlyObjectWrapper<UniversalId> universalIdProperty = fxb.universalIdProperty();
		assertEquals(b.getUniversalId(), universalIdProperty.getValue());
		
		Bulletin b2 = new BulletinForTesting(security);
		assertNotEquals("Bulletins have same id?", b.getUniversalId(), b2.getUniversalId());
		fxb.copyDataFromBulletin(b2, store);
		assertEquals(null, universalIdProperty.getValue());
		ReadOnlyObjectWrapper<UniversalId> universalIdProperty2 = fxb.universalIdProperty();
		assertEquals(b2.getUniversalId(), universalIdProperty2.getValue());
	}
	
	public void testAuthorizedToRead() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		ObservableList<HeadquartersKey> headquartersKeysListNull = fxb.getAuthorizedToReadList();
		assertEquals(null, headquartersKeysListNull);
		
		Bulletin b = new BulletinForTesting(security);
		HeadquartersKey key1 = new HeadquartersKey("account1");
		Vector keysToUse = new Vector();
		keysToUse.add(key1);
		HeadquartersKeys keys = new HeadquartersKeys(keysToUse);
		b.setAuthorizedToReadKeys(keys);
		
		fxb.copyDataFromBulletin(b, store);
		ObservableList<HeadquartersKey> headquartersKeysList = fxb.getAuthorizedToReadList();
		HeadquartersKeys keysFromBulletin = b.getAuthorizedToReadKeys();
		assertEquals(keysFromBulletin.size(), headquartersKeysList.size());
		assertEquals(key1, headquartersKeysList.get(0));
		
		Bulletin b2 = new BulletinForTesting(security);
		HeadquartersKey key2 = new HeadquartersKey("account2");
		HeadquartersKey key3 = new HeadquartersKey("account3");
		Vector newKeys = new Vector();
		newKeys.add(key2);
		newKeys.add(key3);
		HeadquartersKeys b2keys = new HeadquartersKeys(newKeys);
		b2.setAuthorizedToReadKeys(b2keys);

		fxb.copyDataFromBulletin(b2, store);
		ObservableList<HeadquartersKey> headquartersKeysList2 = fxb.getAuthorizedToReadList();
		assertEquals(2, b2.getAuthorizedToReadKeys().size());
		assertEquals(2, headquartersKeysList2.size());
		assertEquals(b2.getAuthorizedToReadKeys().size(), headquartersKeysList2.size());
		assertNotEquals(headquartersKeysList.size(), headquartersKeysList2.size());
		assertTrue(headquartersKeysList2.contains(key2));
		assertTrue(headquartersKeysList2.contains(key3));
		assertFalse(headquartersKeysList2.contains(key1));
		
		headquartersKeysList2.add(key1);
		assertTrue(headquartersKeysList2.contains(key1));
		assertFalse(b2.getAuthorizedToReadKeys().contains(key1));
		Bulletin copyOfModifiedBulletinB2 = new BulletinForTesting(security);
		fxb.copyDataToBulletin(copyOfModifiedBulletinB2);
		assertTrue("After copying data back into this new modified Bulletin we don't have key that was added?", copyOfModifiedBulletinB2.getAuthorizedToReadKeys().contains(key1));
		assertEquals(3, headquartersKeysList2.size());
		assertEquals(3, copyOfModifiedBulletinB2.getAuthorizedToReadKeys().size());
		assertEquals(2, b2.getAuthorizedToReadKeys().size());
	}

	public void testBulletinLocalId() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		ReadOnlyStringProperty bulletinLocalIdNull = fxb.bulletinLocalIdProperty();
		assertEquals(null, bulletinLocalIdNull);

		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b, store);

		ReadOnlyStringProperty fxLocalId = fxb.bulletinLocalIdProperty();
		assertEquals(b.getLocalId(), fxLocalId.getValue());		
		MockBulletinStore testStore = new MockBulletinStore();

    		Bulletin clone = testStore.createNewDraft(b, b.getTopSectionFieldSpecs(), b.getBottomSectionFieldSpecs());
    		assertNotEquals("not new local id?", b.getLocalId(), clone.getLocalId());
    		fxb.copyDataFromBulletin(clone, store);
    		assertEquals(clone.getLocalId(), fxb.bulletinLocalIdProperty().getValue());
    		assertNotNull("ReadOnlyStringProperty will be unchanged", fxLocalId.getValue());
    		assertNotEquals(fxLocalId.getValue(), fxb.bulletinLocalIdProperty().getValue());
 	}
	
	public void testBulletinHistory() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		ReadOnlyObjectWrapper<BulletinHistory> bulletinHistoryNull = fxb.getHistory();
		assertEquals(null, bulletinHistoryNull);

		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b, store);

		ReadOnlyObjectWrapper<BulletinHistory> fxBulletinHistory = fxb.getHistory();
		assertEquals(b.getHistory().toString(), fxBulletinHistory.getValue().toString());
		
		BulletinHistory localHistory = b.getHistory();
		String localIdHistory2 = "history2";
		localHistory.add(localIdHistory2);
		localHistory.add("history2");
		b.setHistory(localHistory);
		assertTrue(b.getHistory().contains(localIdHistory2));
		fxb.copyDataFromBulletin(b, store);
		ReadOnlyObjectWrapper<BulletinHistory> fxBulletinNewHistory = fxb.getHistory();
		assertTrue(fxBulletinNewHistory.getValue().contains(localIdHistory2));
		assertNull(fxBulletinHistory.getValue());
	}

	public void testTitle() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b, store);

		SimpleStringProperty emptyTitleProperty = fxb.fieldProperty(Bulletin.TAGTITLE);
		assertEquals("", emptyTitleProperty.getValue());
		b.set(Bulletin.TAGTITLE, "This is a title");
		fxb.copyDataFromBulletin(b, store);
		assertNull(emptyTitleProperty.getValue());
		SimpleStringProperty titleProperty = fxb.fieldProperty(Bulletin.TAGTITLE);
		assertEquals(b.get(Bulletin.TAGTITLE), titleProperty.getValue());
	}
	
	public void testBottomSectionField() throws Exception
	{
		final String PRIVATE_TAG = Bulletin.TAGPRIVATEINFO;
		final String PRIVATE_DATA_1 = "private info";
		final String PRIVATE_DATA_2 = "This is new and better private info";

		FxBulletin fxb = new FxBulletin(getLocalization());
		Bulletin b = new BulletinForTesting(security);
		b.set(PRIVATE_TAG, PRIVATE_DATA_1);
		fxb.copyDataFromBulletin(b, store);
		
		SimpleStringProperty privateInfoProperty = fxb.fieldProperty(PRIVATE_TAG);
		assertEquals(b.get(PRIVATE_TAG), privateInfoProperty.getValue());
		privateInfoProperty.setValue(PRIVATE_DATA_2);
		
		Bulletin modified = new Bulletin(security);
		fxb.copyDataToBulletin(modified);
		assertEquals(PRIVATE_DATA_2, modified.get(PRIVATE_TAG));
		assertEquals(PRIVATE_DATA_2, modified.getFieldDataPacket().get(PRIVATE_TAG));
		assertEquals("", modified.getPrivateFieldDataPacket().get(PRIVATE_TAG));
	}
	
	public void testFieldSequence() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		Bulletin before = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(before, store);
		Bulletin after = new Bulletin(security);
		fxb.copyDataToBulletin(after);
		Vector<String> beforeTags = extractFieldTags(before);
		Vector<String> afterTags = extractFieldTags(after);
		assertEquals(beforeTags, afterTags);
	}
	
	private Vector<String> extractFieldTags(Bulletin b)
	{
		Vector<String> fieldTags = new Vector<String>();
		FieldSpecCollection topSpecs = b.getTopSectionFieldSpecs();
		for(int i = 0; i < topSpecs.size(); ++i)
		{
			FieldSpec fieldSpec = topSpecs.get(i);
			fieldTags.add(fieldSpec.getTag());
		}
		FieldSpecCollection bottomSpecs = b.getBottomSectionFieldSpecs();
		for(int i = 0; i < bottomSpecs.size(); ++i)
		{
			FieldSpec fieldSpec = bottomSpecs.get(i);
			fieldTags.add(fieldSpec.getTag());
		}
		
		return fieldTags;
	}
	
	private MiniLocalization getLocalization()
	{
		return localization;
	}
	
	public static final String BURMESE_UNICODE_TEST_STRING = "\u104E\u1004\u103A\u1038";
	public static final String BURMESE_ZAWGYI_TEST_STRING = "\u104E";
	
	private MockMartusSecurity security;
	private MiniLocalization localization;
	private MockBulletinStore store;
}
