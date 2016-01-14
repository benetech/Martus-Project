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

package org.martus.client.search;

import java.io.File;
import java.util.Arrays;

import org.martus.client.core.SafeReadableBulletin;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.field.MartusDateRangeField;
import org.martus.common.field.MartusField;
import org.martus.common.field.MartusSearchableGridColumnField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeLanguage;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.test.UnicodeConstants;
import org.martus.util.TestCaseEnhanced;


public class TestBulletinSearcher extends TestCaseEnhanced
{

	public TestBulletinSearcher(String name)
	{
		super(name);
	}
	
	public void setUp()
	{
		localization = new MiniLocalization();
		noReusableChoices = PoolOfReusableChoicesLists.EMPTY_POOL;
	}
	
	public void testDoesMatchSpecificField() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security);

		FieldSpec fieldToSearch = FieldSpec.createStandardField(Bulletin.TAGLOCATION, new FieldTypeNormal());
		FieldSpec otherField = FieldSpec.createStandardField(Bulletin.TAGAUTHOR, new FieldTypeNormal());
		String sampleValue = "green";
		String otherValue = "ignoreme";
		realBulletin.set(fieldToSearch.getTag(), sampleValue);
		realBulletin.set(otherField.getTag(), otherValue);
		
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin, localization);
		BulletinSearcher specific = new BulletinSearcher(new SearchTreeNode(fieldToSearch, "", sampleValue));
		assertTrue("didn't find specific field?", specific.doesMatch(b, localization));
		BulletinSearcher wrongValue= new BulletinSearcher(new SearchTreeNode(fieldToSearch, "", otherValue));
		assertFalse("found wrong value?", wrongValue.doesMatch(b, localization));
		BulletinSearcher wrongField = new BulletinSearcher(new SearchTreeNode(otherField, "", sampleValue));
		assertFalse("found in wrong field?", wrongField.doesMatch(b, localization));
	}
	
	public void testDoesMatchNoSuchField() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security);
		
		FieldSpec noSuchField = FieldSpec.createStandardField("nosuchfield", new FieldTypeNormal());
		String sampleValue = "sample data";
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin, localization);
		BulletinSearcher contains = new BulletinSearcher(new SearchTreeNode(noSuchField, "", sampleValue));
		assertFalse(": matched non-existant field?", contains.doesMatch(b, localization));
		BulletinSearcher greaterThan = new BulletinSearcher(new SearchTreeNode(noSuchField, ">", sampleValue));
		assertFalse("> matched non-existant field?", greaterThan.doesMatch(b, localization));
		
	}

	public void testDoesMatchComparisons() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);

		FieldSpec fieldToSearch = FieldSpec.createStandardField(Bulletin.TAGLOCATION, new FieldTypeNormal());
		String belowSample = "blue";
		String sampleValue = "green";
		String aboveSample = "red";
		b.set(fieldToSearch.getTag(), sampleValue);

		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, ">=", belowSample, true);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, ">=", sampleValue, true);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, ">=", aboveSample, false);

		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, ">", belowSample, true);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, ">", sampleValue, false);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, ">", aboveSample, false);

		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "<=", belowSample, false);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "<=", sampleValue, true);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "<=", aboveSample, true);

		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "<", belowSample, false);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "<", sampleValue, false);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "<", aboveSample, true);

		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "=", belowSample, false);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "=", sampleValue, true);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "=", aboveSample, false);

		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "!=", belowSample, true);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "!=", sampleValue, false);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "!=", aboveSample, true);
	}

	private void verifyOperatorComparison(String caller, Bulletin realBulletin, FieldSpec fieldToSearch, String operator, String value, boolean expected)
	{
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin, localization);
		String actual = b.getPossiblyNestedField(fieldToSearch).getSearchableData(localization);
		BulletinSearcher searcher = new BulletinSearcher(new SearchTreeNode(fieldToSearch, operator, value));
		String message = caller + ": " + actual + " " + operator + value + " ";
		assertEquals(message, expected, searcher.doesMatch(b, localization));
	}
	
	public void testGetPossiblyNestedField() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security);
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin, localization);
		FieldSpec noSuchField = FieldSpec.createStandardField("no.such.field", new FieldTypeNormal());
		MartusField noSuchFieldResult = b.getPossiblyNestedField(noSuchField);
		assertEquals("didn't return empty field for bogus field?", "", noSuchFieldResult.getData());
		FieldSpec noSubField = FieldSpec.createStandardField("entrydate.no.such.subfield", new FieldTypeNormal());
		MartusField noSubfieldResult = b.getPossiblyNestedField(noSubField);
		assertNull("didn't return null for bogus subfield?", noSubfieldResult);
	}
	
	public void testGetPossiblyNestedFieldInGrid() throws Exception
	{
		String ickyLabel = "Column.Label";
		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setTag("grid");
		gridSpec.addColumn(FieldSpec.createCustomField("", ickyLabel, new FieldTypeNormal()));
		gridSpec.addColumn(FieldSpec.createCustomField("", "Second column", new FieldTypeNormal()));
		FieldSpecCollection specs = new FieldSpecCollection(new FieldSpec[] {gridSpec});
		
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security, specs, StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		GridData data = new GridData(gridSpec, noReusableChoices);
		data.addEmptyRow();
		data.setValueAt("first row", 0, 0);
		data.addEmptyRow();
		data.setValueAt("second row", 1, 0);
		realBulletin.set(gridSpec.getTag(), data.getXmlRepresentation());
		
		String sanitizedLabel = ickyLabel.replaceAll("\\.", " ");
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin, localization);
		FieldSpec firstColumn = FieldSpec.createStandardField("grid." + sanitizedLabel, new FieldTypeNormal());
		MartusSearchableGridColumnField gridColumn = (MartusSearchableGridColumnField)b.getPossiblyNestedField(firstColumn);
		assertTrue("didn't find contains in second row?", gridColumn.doesMatch(MartusField.CONTAINS, "second", localization));
		assertFalse("matched contains when it shouldn't?", gridColumn.doesMatch(MartusField.CONTAINS, "sfesfff", localization));
		assertTrue("didn't find greater in second row?", gridColumn.doesMatch(MartusField.GREATER, "m", localization));
		assertFalse("matched greater when it shouldn't?", gridColumn.doesMatch(MartusField.GREATER, "yyy", localization));
		
		FieldSpec secondColumn = FieldSpec.createStandardField("grid.Second column", new FieldTypeNormal());
		MartusSearchableGridColumnField gridSecondColumn = (MartusSearchableGridColumnField)b.getPossiblyNestedField(secondColumn);
		assertNotNull("didn't get second column?", gridSecondColumn);

		Bulletin emptyBulletin = new Bulletin(security, specs, StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		SafeReadableBulletin eb = new SafeReadableBulletin(emptyBulletin, localization);
		assertEquals("didn't return empty field for empty grid?", "", eb.getPossiblyNestedField(firstColumn).getData());
		
	}

	public void testGridGetMatchingRows() throws Exception
	{
		final String REPEATED_FIRST_NAME = "Adam";
		final String REPEATED_LAST_NAME = "Blake";
		final String NEVER_TOGETHER_FIRST_NAME = "Barbara";
		final String NEVER_TOGETHER_LAST_NAME = "Anderson";
		final String OTHER_DATA = "whatever";

		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setTag("grid");
		FieldSpec columnSpec1 = FieldSpec.createCustomField("", "first", new FieldTypeNormal());
		FieldSpec columnSpec2 = FieldSpec.createCustomField("", "last", new FieldTypeNormal());
		FieldSpec columnSpec3 = FieldSpec.createCustomField("", "other", new FieldTypeNormal());
		gridSpec.addColumn(columnSpec1);
		gridSpec.addColumn(columnSpec2);
		gridSpec.addColumn(columnSpec3);
		FieldSpecCollection specs = new FieldSpecCollection(new FieldSpec[] {gridSpec});

		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security, specs, StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		GridData data = new GridData(gridSpec, noReusableChoices);
		data.addEmptyRow();
		data.addEmptyRow();
		data.addEmptyRow();
		data.setValueAt(REPEATED_FIRST_NAME, 0, 0);
		data.setValueAt(NEVER_TOGETHER_LAST_NAME, 0, 1);
		data.setValueAt(OTHER_DATA, 0, 2);
		data.setValueAt(NEVER_TOGETHER_FIRST_NAME, 1, 0);
		data.setValueAt(REPEATED_LAST_NAME, 1, 1);
		data.setValueAt(OTHER_DATA, 1, 2);
		data.setValueAt(REPEATED_FIRST_NAME, 2, 0);
		data.setValueAt(REPEATED_LAST_NAME, 2, 1);
		data.setValueAt(OTHER_DATA, 2, 2);
		realBulletin.set(gridSpec.getTag(), data.getXmlRepresentation());
		
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin, localization);
		FieldSpec firstColumnSpec = FieldSpec.createStandardField("grid." + columnSpec1.getLabel(), new FieldTypeNormal());
		FieldSpec secondColumnSpec = FieldSpec.createStandardField("grid." + columnSpec2.getLabel(), new FieldTypeNormal());
		FieldSpec thirdColumnSpec = FieldSpec.createStandardField("grid." + columnSpec3.getLabel(), new FieldTypeNormal());
		MartusSearchableGridColumnField firstColumn = (MartusSearchableGridColumnField)b.getPossiblyNestedField(firstColumnSpec);
		MartusSearchableGridColumnField secondColumn = (MartusSearchableGridColumnField)b.getPossiblyNestedField(secondColumnSpec);

		{
			Integer[] firstNameRows = firstColumn.getMatchingRows(MartusField.CONTAINS, REPEATED_FIRST_NAME, localization);
			assertTrue("didn't match first and third rows?", Arrays.equals(firstNameRows, new Integer[] {new Integer(0), new Integer(2)}));
			Integer[] lastNameRows = secondColumn.getMatchingRows(MartusField.CONTAINS, REPEATED_LAST_NAME, localization);
			assertTrue("didn't match second and third rows?", Arrays.equals(lastNameRows, new Integer[] {new Integer(1), new Integer(2)}));
		}
		
		SearchTreeNode firstNameNode = new SearchTreeNode(firstColumnSpec, "", REPEATED_FIRST_NAME);
		BulletinSearcher firstNameSearcher = new BulletinSearcher(firstNameNode);
		assertTrue("didn't find repeated first name?", firstNameSearcher.doesMatch(b, localization));
		
		SearchTreeNode lastNameNode = new SearchTreeNode(secondColumnSpec, "", REPEATED_LAST_NAME);
		BulletinSearcher lastNameSearcher = new BulletinSearcher(lastNameNode);
		assertTrue("didn't find repeated last name?", lastNameSearcher.doesMatch(b, localization));
		
		SearchTreeNode otherFirstNameNode = new SearchTreeNode(firstColumnSpec, "", NEVER_TOGETHER_FIRST_NAME);
		SearchTreeNode otherLastNameNode = new SearchTreeNode(secondColumnSpec, "", NEVER_TOGETHER_LAST_NAME);
		SearchTreeNode firstAndLastNormalNode = new SearchTreeNode(SearchTreeNode.AND, otherFirstNameNode, otherLastNameNode);
		BulletinSearcher firstAndLastNormalSearcher = new BulletinSearcher(firstAndLastNormalNode);
		assertTrue("normal didn't match names on different rows?", firstAndLastNormalSearcher.doesMatch(b, localization));
		
		SearchTreeNode badFirstAndLastSameRowNode = new SearchTreeNode(SearchTreeNode.AND, otherFirstNameNode, otherLastNameNode);
		BulletinSearcher badFirstAndLastSameRowSearcher = new BulletinSearcher(badFirstAndLastSameRowNode, true);
		assertFalse("same row match matched different rows?", badFirstAndLastSameRowSearcher.doesMatch(b, localization));

		SearchTreeNode goodFirstAndLastSameRowNode = new SearchTreeNode(SearchTreeNode.AND, firstNameNode, lastNameNode);
		BulletinSearcher goodFirstAndLastSameRowSearcher = new BulletinSearcher(goodFirstAndLastSameRowNode, true);
		assertTrue("same row match didn't match?", goodFirstAndLastSameRowSearcher.doesMatch(b, localization));
		
		SearchTreeNode missingNameNode = new SearchTreeNode("nowhere");
		SearchTreeNode foundOrNotNode = new SearchTreeNode(SearchTreeNode.OR, otherFirstNameNode, missingNameNode);
		assertTrue("simple found OR not didn't work?", new BulletinSearcher(foundOrNotNode).doesMatch(b, localization));
		assertTrue("samerow found OR not didn't work?", new BulletinSearcher(foundOrNotNode, true).doesMatch(b, localization));
		SearchTreeNode notOrFoundNode = new SearchTreeNode(SearchTreeNode.OR, missingNameNode, otherFirstNameNode);
		assertTrue("simple not OR found didn't work?", new BulletinSearcher(notOrFoundNode).doesMatch(b, localization));
		assertTrue("samerow not OR found didn't work?", new BulletinSearcher(notOrFoundNode, true).doesMatch(b, localization));

		SearchTreeNode delayedAndNode = new SearchTreeNode(SearchTreeNode.AND, foundOrNotNode, otherLastNameNode);
		assertTrue("simple row1 OR no AND row2 didn't match?", new BulletinSearcher(delayedAndNode).doesMatch(b, localization));
		assertFalse("samerow row1 OR no AND row2 matched?", new BulletinSearcher(delayedAndNode, true).doesMatch(b, localization));

		SearchTreeNode otherDataNode = new SearchTreeNode(thirdColumnSpec, "", OTHER_DATA);
		SearchTreeNode threeSameRowNode = new SearchTreeNode(SearchTreeNode.AND, goodFirstAndLastSameRowNode, otherDataNode);
		assertTrue("samerow a AND b AND c didn't match?", new BulletinSearcher(threeSameRowNode, true).doesMatch(b, localization));
		SearchTreeNode invertedThreeSameRowNode = new SearchTreeNode(SearchTreeNode.AND, otherDataNode, goodFirstAndLastSameRowNode);
		assertTrue("samerow c AND a AND b didn't match?", new BulletinSearcher(invertedThreeSameRowNode, true).doesMatch(b, localization));
	}
	
	public void testAndMixingNonGridAndSameRowGrid() throws Exception
	{
		final String FIRST_NAME = "whatever";

		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setTag("grid");
		FieldSpec columnSpec1 = FieldSpec.createCustomField("", "first", new FieldTypeNormal());
		gridSpec.addColumn(columnSpec1);
		FieldSpecCollection specs = new FieldSpecCollection(new FieldSpec[] {gridSpec});

		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security, specs, StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		GridData data = new GridData(gridSpec, noReusableChoices);
		data.addEmptyRow();
		data.setValueAt(FIRST_NAME, 0, 0);
		realBulletin.set(gridSpec.getTag(), data.getXmlRepresentation());
		
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin, localization);
		FieldSpec firstColumnSpec = FieldSpec.createStandardField("grid." + columnSpec1.getLabel(), new FieldTypeNormal());
		
		SearchTreeNode firstNameMatchesNode = new SearchTreeNode(firstColumnSpec, "", FIRST_NAME);
		SearchTreeNode firstNameDifferentNode = new SearchTreeNode(firstColumnSpec, "", "lsijfle");
		SearchTreeNode otherFieldMatchesNode = new SearchTreeNode("");
		SearchTreeNode otherFieldDifferentNode = new SearchTreeNode("sdfsfesef");

		SearchTreeNode gridAndNonGridNode = new SearchTreeNode(SearchTreeNode.AND, firstNameMatchesNode, otherFieldMatchesNode);
		BulletinSearcher gridAndNonGridSearcher = new BulletinSearcher(gridAndNonGridNode, true);
		assertTrue("grid AND non grid didn't match?", gridAndNonGridSearcher.doesMatch(b, localization));

		SearchTreeNode nonGridAndGridNode = new SearchTreeNode(SearchTreeNode.AND, otherFieldMatchesNode, firstNameMatchesNode);
		BulletinSearcher nonGridAndGridSearcher = new BulletinSearcher(nonGridAndGridNode, true);
		assertTrue("non grid AND grid didn't match?", nonGridAndGridSearcher.doesMatch(b, localization));

		SearchTreeNode gridAndBadNonGridNode = new SearchTreeNode(SearchTreeNode.AND, firstNameMatchesNode, otherFieldDifferentNode);
		BulletinSearcher gridAndBadNonGridSearcher = new BulletinSearcher(gridAndBadNonGridNode, true);
		assertFalse("grid AND bad non grid matched?", gridAndBadNonGridSearcher.doesMatch(b, localization));

		SearchTreeNode badNonGridAndGridNode = new SearchTreeNode(SearchTreeNode.AND, otherFieldDifferentNode, firstNameMatchesNode);
		BulletinSearcher badNonGridAndGridSearcher = new BulletinSearcher(badNonGridAndGridNode, true);
		assertFalse("bad non grid AND grid matched?", badNonGridAndGridSearcher.doesMatch(b, localization));

		SearchTreeNode badGridAndNonGridNode = new SearchTreeNode(SearchTreeNode.AND, firstNameDifferentNode, otherFieldMatchesNode);
		BulletinSearcher badGridAndNonGridSearcher = new BulletinSearcher(badGridAndNonGridNode, true);
		assertFalse("bad grid AND non grid matched?", badGridAndNonGridSearcher.doesMatch(b, localization));

		SearchTreeNode nonGridAndBadGridNode = new SearchTreeNode(SearchTreeNode.AND, otherFieldMatchesNode, firstNameDifferentNode);
		BulletinSearcher nonGridAndBadGridSearcher = new BulletinSearcher(nonGridAndBadGridNode, true);
		assertFalse("non grid AND bad grid matched?", nonGridAndBadGridSearcher.doesMatch(b, localization));

		SearchTreeNode badNonGridAndBadGridNode = new SearchTreeNode(SearchTreeNode.AND, otherFieldDifferentNode, firstNameDifferentNode);
		BulletinSearcher badNonGridAndBadGridSearcher = new BulletinSearcher(badNonGridAndBadGridNode, true);
		assertFalse("bad non grid AND bad grid matched?", badNonGridAndBadGridSearcher.doesMatch(b, localization));
	}
	
	public void testOrMixingNonGridAndSameRowGrid() throws Exception
	{
		final String FIRST_NAME = "whatever";

		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setTag("grid");
		FieldSpec columnSpec1 = FieldSpec.createCustomField("", "first", new FieldTypeNormal());
		gridSpec.addColumn(columnSpec1);
		FieldSpecCollection specs = new FieldSpecCollection(new FieldSpec[] {gridSpec});

		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security, specs, StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		GridData data = new GridData(gridSpec, noReusableChoices);
		data.addEmptyRow();
		data.setValueAt(FIRST_NAME, 0, 0);
		realBulletin.set(gridSpec.getTag(), data.getXmlRepresentation());
		
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin, localization);
		FieldSpec firstColumnSpec = FieldSpec.createStandardField("grid." + columnSpec1.getLabel(), new FieldTypeNormal());
		
		SearchTreeNode firstNameMatchesNode = new SearchTreeNode(firstColumnSpec, "", FIRST_NAME);
		SearchTreeNode firstNameDifferentNode = new SearchTreeNode(firstColumnSpec, "", "lsijfle");
		SearchTreeNode otherFieldMatchesNode = new SearchTreeNode("");
		SearchTreeNode otherFieldDifferentNode = new SearchTreeNode("sdfsfesef");

		SearchTreeNode gridOrNonGridNode = new SearchTreeNode(SearchTreeNode.OR, firstNameMatchesNode, otherFieldMatchesNode);
		BulletinSearcher gridOrNonGridSearcher = new BulletinSearcher(gridOrNonGridNode, true);
		assertTrue("grid OR non grid didn't match?", gridOrNonGridSearcher.doesMatch(b, localization));

		SearchTreeNode nonGridOrGridNode = new SearchTreeNode(SearchTreeNode.OR, otherFieldMatchesNode, firstNameMatchesNode);
		BulletinSearcher nonGridOrGridSearcher = new BulletinSearcher(nonGridOrGridNode, true);
		assertTrue("non grid OR grid didn't match?", nonGridOrGridSearcher.doesMatch(b, localization));

		SearchTreeNode gridOrBadNonGridNode = new SearchTreeNode(SearchTreeNode.OR, firstNameMatchesNode, otherFieldDifferentNode);
		BulletinSearcher gridOrBadNonGridSearcher = new BulletinSearcher(gridOrBadNonGridNode, true);
		assertTrue("grid OR bad non grid didn't match?", gridOrBadNonGridSearcher.doesMatch(b, localization));

		SearchTreeNode badNonGridOrGridNode = new SearchTreeNode(SearchTreeNode.OR, otherFieldDifferentNode, firstNameMatchesNode);
		BulletinSearcher badNonGridOrGridSearcher = new BulletinSearcher(badNonGridOrGridNode, true);
		assertTrue("bad non grid OR grid didn't match?", badNonGridOrGridSearcher.doesMatch(b, localization));

		SearchTreeNode badGridOrNonGridNode = new SearchTreeNode(SearchTreeNode.OR, firstNameDifferentNode, otherFieldMatchesNode);
		BulletinSearcher badGridOrNonGridSearcher = new BulletinSearcher(badGridOrNonGridNode, true);
		assertTrue("bad grid OR non grid didn't match?", badGridOrNonGridSearcher.doesMatch(b, localization));

		SearchTreeNode nonGridOrBadGridNode = new SearchTreeNode(SearchTreeNode.OR, otherFieldMatchesNode, firstNameDifferentNode);
		BulletinSearcher nonGridOrBadGridSearcher = new BulletinSearcher(nonGridOrBadGridNode, true);
		assertTrue("non grid OR bad grid didn't match?", nonGridOrBadGridSearcher.doesMatch(b, localization));

		SearchTreeNode badNonGridOrBadGridNode = new SearchTreeNode(SearchTreeNode.OR, otherFieldDifferentNode, firstNameDifferentNode);
		BulletinSearcher badNonGridOrBadGridSearcher = new BulletinSearcher(badNonGridOrBadGridNode, true);
		assertFalse("bad non grid OR bad grid matched?", badNonGridOrBadGridSearcher.doesMatch(b, localization));
	}
	
	
	public void testDoesMatch() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security);
		realBulletin.set("author", "hello");
		realBulletin.set("summary", "summary");
		realBulletin.set("title", "Jos"+UnicodeConstants.ACCENT_E_LOWER+"e");
		realBulletin.set(Bulletin.TAGEVENTDATE, "2002-04-04");
		realBulletin.set(Bulletin.TAGENTRYDATE, "2002-10-15");
		byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
		byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};
		File tempFile1 = createTempFileWithData(sampleBytes1);
		File tempFile2 = createTempFileWithData(sampleBytes2);
		AttachmentProxy publicProxy = new AttachmentProxy(tempFile1);
		String publicProxyLabel = "publicProxy.txt";
		publicProxy.setLabel(publicProxyLabel);
		AttachmentProxy privateProxy = new AttachmentProxy(tempFile2);

		realBulletin.addPublicAttachment(publicProxy);
		realBulletin.addPrivateAttachment(privateProxy);

		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin, localization);

		BulletinSearcher helloWithAnyDate = new BulletinSearcher(new SearchTreeNode("hello"));
		assertEquals("hello", true, helloWithAnyDate.doesMatch(b, localization));

		// field names should not be searched
		BulletinSearcher fieldTagWithAnyDate = new BulletinSearcher(new SearchTreeNode("author"));
		assertEquals("author", false, fieldTagWithAnyDate.doesMatch(b, localization));
		// id should not be searched
		BulletinSearcher localIdWithAnyDate = new BulletinSearcher(new SearchTreeNode(b.getLocalId()));
		assertEquals("getLocalId()", false, localIdWithAnyDate.doesMatch(b, localization));

		BulletinSearcher noText = new BulletinSearcher(new SearchTreeNode(""));
		assertEquals("Blank must match", true, noText.doesMatch(b, localization));

		BulletinSearcher allCaps = new BulletinSearcher(new SearchTreeNode("HELLO"));
		assertEquals("HELLO", true, allCaps.doesMatch(b, localization));
		BulletinSearcher utf8 = new BulletinSearcher(new SearchTreeNode("jos"+UnicodeConstants.ACCENT_E_LOWER+"e"));
		assertEquals("jos"+UnicodeConstants.ACCENT_E_LOWER+"e", true, utf8.doesMatch(b, localization));
		BulletinSearcher utf8MixedCase = new BulletinSearcher(new SearchTreeNode("jos"+UnicodeConstants.ACCENT_E_UPPER+"e"));
		assertEquals("jos"+UnicodeConstants.ACCENT_E_UPPER+"e", true, utf8MixedCase.doesMatch(b, localization));
		BulletinSearcher nonUtf8 = new BulletinSearcher(new SearchTreeNode("josee"));
		assertEquals("josee", false, nonUtf8.doesMatch(b, localization));

		SearchParser parser = SearchParser.createEnglishParser();
		BulletinSearcher andRightFalse = new BulletinSearcher(parser.parseJustAmazonValueForTesting("hello and goodbye"));
		assertEquals("right false and", false, andRightFalse.doesMatch(b, localization));
		BulletinSearcher andLeftFalse = new BulletinSearcher(parser.parseJustAmazonValueForTesting("goodbye and hello"));
		assertEquals("left false and", false, andLeftFalse.doesMatch(b, localization));
		BulletinSearcher andBothTrue = new BulletinSearcher(parser.parseJustAmazonValueForTesting("Hello and Summary"));
		assertEquals("true and", true, andBothTrue.doesMatch(b, localization));

		BulletinSearcher orBothFalse = new BulletinSearcher(parser.parseJustAmazonValueForTesting("swinging and swaying"));
		assertEquals("false or", false, orBothFalse.doesMatch(b, localization));
		BulletinSearcher orRightFalse = new BulletinSearcher(parser.parseJustAmazonValueForTesting("hello or goodbye"));
		assertEquals("left true or", true, orRightFalse.doesMatch(b, localization));
		BulletinSearcher orLeftFalse = new BulletinSearcher(parser.parseJustAmazonValueForTesting("goodbye or hello"));
		assertEquals("right true or", true, orLeftFalse.doesMatch(b, localization));
		BulletinSearcher orBothTrue = new BulletinSearcher(parser.parseJustAmazonValueForTesting("hello or summary"));
		assertEquals("both true or", true, orBothTrue.doesMatch(b, localization));

		BulletinSearcher publicAttachmentWithAnyDate = new BulletinSearcher(new SearchTreeNode(publicProxyLabel.substring(0, publicProxyLabel.length()-4)));
		assertEquals("Public Attachment without .txt extension?", true, publicAttachmentWithAnyDate.doesMatch(b, localization));

		BulletinSearcher privateAttachmentWithAnyDate = new BulletinSearcher(new SearchTreeNode(privateProxy.getLabel().toUpperCase()));
		assertEquals("Private Attachment?", true, privateAttachmentWithAnyDate.doesMatch(b, localization));
	}
	
	public void testDateRangeShouldntMatchAnyRandomString()
	{
		FieldSpec spec = FieldSpec.createStandardField("daterange", new FieldTypeDateRange());
		MartusDateRangeField dateRange = new MartusDateRangeField(spec);
		dateRange.setData("");
		assertFalse("empty date range contains a string?", dateRange.contains("lsijflidj", localization));
	}

	public void testLocalId() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		
		FieldSpec spec = FieldSpec.createStandardField("_localId", new FieldTypeNormal());
		verifyOperatorComparison("testLocalId", b, spec, "", b.getLocalId(), true);
	}
		
	public void testDateMatchesLastSaved() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		b.getBulletinHeaderPacket().updateLastSavedTime();
		FieldSpec spec = SearchFieldChooserSpecBuilder.createLastSavedDateChoice(localization).getSpec();
		
		String rawLastSaved = b.getLastSavedDate();
		verifyOperatorComparison("testDateMatchesLastSaved", b, spec, "=", rawLastSaved, true);

		String formattedLastSaved = localization.convertStoredDateToDisplay(rawLastSaved);
		verifyOperatorComparison("testDateMatchesLastSaved", b, spec, "", formattedLastSaved, true);
		
		String differentDate = "2008-07-13";
		verifyOperatorComparison("testDateMatchesLastSaved", b, spec, "=", differentDate, false);
	}
		
	public void testFlexiDateMatches() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		b.set(Bulletin.TAGEVENTDATE, "2003-08-20,20030820+3");
		
		final FieldSpec eventDateField = b.getField(Bulletin.TAGEVENTDATE).getFieldSpec();
		verifyOperatorComparison("testFlexiDateMatches", b, eventDateField, "", "08/20/2003", true);
		verifyOperatorComparison("testFlexiDateMatches", b, eventDateField, "", "08/21/2003", false);
		verifyOperatorComparison("testFlexiDateMatches", b, eventDateField, "", "08/23/2003", true);
		verifyOperatorComparison("testFlexiDateMatches", b, eventDateField, "", "08/26/2003", false);

		FieldSpec eventDateBeginField = FieldSpec.createStandardField(Bulletin.TAGEVENTDATE + "." + MartusDateRangeField.SUBFIELD_BEGIN, new FieldTypeDate());
		FieldSpec eventDateEndField = FieldSpec.createStandardField(Bulletin.TAGEVENTDATE + "." + MartusDateRangeField.SUBFIELD_END, new FieldTypeDate());
		verifyOperatorComparison("testFlexiDateMatches", b, eventDateBeginField, "", "08/20/2003", true);
		verifyOperatorComparison("testFlexiDateMatches", b, eventDateBeginField, "", "08/21/2003", false);
		verifyOperatorComparison("testFlexiDateMatches", b, eventDateEndField, "", "08/23/2003", true);
		verifyOperatorComparison("testFlexiDateMatches", b, eventDateEndField, "", "08/22/2003", false);

		verifyOperatorComparison("testFlexiDateMatches", b, eventDateBeginField, ">=", "2003-08-20", true);
		verifyOperatorComparison("testFlexiDateMatches", b, eventDateBeginField, ">", "2003-08-20", false);
		verifyOperatorComparison("testFlexiDateMatches", b, eventDateBeginField, "<=", "2003-08-20", true);
		verifyOperatorComparison("testFlexiDateMatches", b, eventDateBeginField, "<", "2003-08-19", false);

		verifyOperatorComparison("testFlexiDateMatches", b, eventDateBeginField, "<", "2006-02-20", true);
		verifyOperatorComparison("testFlexiDateMatches", b, eventDateBeginField, ">", "2002-09-20", true);
	}
	
	public void testBooleanMatches() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		
		final FieldSpec trueField = FieldSpec.createCustomField("true", "should be true", new FieldTypeBoolean());
		final FieldSpec falseField = FieldSpec.createCustomField("false", "should be false", new FieldTypeBoolean());
		final FieldSpec blankField = FieldSpec.createCustomField("bogus", "will be blank", new FieldTypeBoolean());
		FieldSpec[] publicSpecs = new FieldSpec[] 
		{
			trueField,
			falseField,
			blankField,
		};
		
		Bulletin b = new Bulletin(security, new FieldSpecCollection(publicSpecs), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		b.set("true", FieldSpec.TRUESTRING);
		b.set("false", FieldSpec.FALSESTRING);
		b.set("bogus", "");
		
		String localizedTrue = localization.getButtonLabel(EnglishCommonStrings.YES);
		String localizedFalse = localization.getButtonLabel(EnglishCommonStrings.NO);
		
		verifyOperatorComparison("testBooleanMatches", b, trueField, "", localizedTrue, true);
		verifyOperatorComparison("testBooleanMatches", b, trueField, "", localizedFalse, false);
		verifyOperatorComparison("testBooleanMatches", b, falseField, "", localizedFalse, true);
		verifyOperatorComparison("testBooleanMatches", b, falseField, "", localizedTrue, false);
		verifyOperatorComparison("testBooleanMatches", b, blankField, "", localizedFalse, true);
		verifyOperatorComparison("testBooleanMatches", b, blankField, "", localizedTrue, false);
		verifyOperatorComparison("testBooleanMatches", b, trueField, "!=", localizedFalse, true);
		
	}
	
	public void testMatchingSearchableNotRaw() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		
		FieldSpec fieldToSearch = FieldSpec.createStandardField(Bulletin.TAGLANGUAGE, new FieldTypeLanguage());
		final String languageCode = MiniLocalization.ARABIC;
		String languageName = localization.getLanguageName(languageCode); 
		b.set(Bulletin.TAGLANGUAGE, languageCode);
		
		BulletinSearcher contains = new BulletinSearcher(new SearchTreeNode(fieldToSearch , "", languageName));
		assertTrue("not looking at searchable form?", contains.doesMatch(new SafeReadableBulletin(b, localization), localization));
		
		BulletinSearcher equals  = new BulletinSearcher(new SearchTreeNode(fieldToSearch, "=", languageCode));
		assertTrue("not looking at searchable form?", equals.doesMatch(new SafeReadableBulletin(b, localization), localization));
	}
	
	MiniLocalization localization;
	private PoolOfReusableChoicesLists noReusableChoices;
}
