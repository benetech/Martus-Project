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
package org.martus.common.test;

import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.GridRow;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.GridFieldSpec.UnsupportedFieldTypeException;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.xml.XmlUtilities;


public class TestGridData extends TestCaseEnhanced
{
	public TestGridData(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		gridSpec2Colunns = new GridFieldSpec();
		gridSpec2Colunns.addColumn(FieldSpec.createStandardField("a", new FieldTypeNormal()));
		gridSpec2Colunns.addColumn(FieldSpec.createStandardField("b", new FieldTypeNormal()));
		noReusableChoices = PoolOfReusableChoicesLists.EMPTY_POOL;
	}
	
	public void testBasics()
	{
		GridData grid = new GridData(gridSpec2Colunns, noReusableChoices);
		int cols = grid.getColumnCount();
		assertEquals(gridSpec2Colunns.getColumnCount(), cols);

		String mustEncodeXMLData = "<&>";

		GridRow row1 = new GridRow(gridSpec2Colunns, noReusableChoices);
		String[] row1Data = {"column1a", mustEncodeXMLData};
		fillGridRow(row1, row1Data);
		
		GridRow row2 = new GridRow(gridSpec2Colunns, noReusableChoices);
		String row2DataColumn1 = "column1b"; 
		String[] row2Data = {row2DataColumn1, "column2b"};
		fillGridRow(row2, row2Data);
		
		assertEquals("row count should start at 0", 0, grid.getRowCount());
		grid.addRow(row1);
		assertEquals("row count should be at 1", 1, grid.getRowCount());
		grid.addRow(row2);
		assertEquals("row count should now be 2", 2, grid.getRowCount());
		String xmlEncodedString = XmlUtilities.getXmlEncoded(mustEncodeXMLData);

		String expectedXml = 
			"<"+GridData.GRID_DATA_TAG+ " columns='2'>\n" +
			"<"+GridRow.ROW_TAG + ">\n" + 
				"<" + GridRow.COLUMN_TAG + ">" + row1.getCellText(0) + "</" + GridRow.COLUMN_TAG + ">\n" +
				"<" + GridRow.COLUMN_TAG + ">" + xmlEncodedString + "</" + GridRow.COLUMN_TAG + ">\n" +
			"</" + GridRow.ROW_TAG + ">\n" +
			"<" + GridRow.ROW_TAG + ">\n" +
				"<" + GridRow.COLUMN_TAG + ">" + row2.getCellText(0) + "</" + GridRow.COLUMN_TAG + ">\n" +
				"<" + GridRow.COLUMN_TAG + ">" + row2.getCellText(1) + "</" + GridRow.COLUMN_TAG + ">\n" +
			"</" + GridRow.ROW_TAG + ">\n"+
			"</" + GridData.GRID_DATA_TAG+ ">\n";
			
		String xml = grid.getXmlRepresentation();
		assertEquals("xml incorrect?", expectedXml, xml);
		
		assertEquals("Show now have 2 rows", 2 , grid.getRowCount());
		grid.addEmptyRow();
		assertEquals("Show now have an empty row", 3, grid.getRowCount());
		assertEquals("should be empty", "", grid.getValueAt(2,1));
		
		assertEquals("Row 2's data should be in row 2", row2DataColumn1, grid.getValueAt(1,0));
		grid.deleteRow(0);
		assertEquals("Should only have 2 rows after a delete", 2, grid.getRowCount());
		assertEquals("Row 2's data should now be in row 1", row2DataColumn1, grid.getValueAt(0,0));

		GridRow newInsertedRow = new GridRow(gridSpec2Colunns, noReusableChoices);
		String newRowdata = "new row inserted";
		String[] newInsertedRowData = {newRowdata, mustEncodeXMLData};
		fillGridRow(newInsertedRow, newInsertedRowData);
		grid.insertRow(newInsertedRow,0);
		assertEquals("row count should be 3 with inserted row", 3, grid.getRowCount());
		assertEquals("Row 3's data should be in row 1", newRowdata, grid.getValueAt(0,0));
		assertEquals("Row 2's data should now be in row 2", row2DataColumn1, grid.getValueAt(1,0));
	
		grid.addEmptyRowAt(0);
		assertEquals("row count should be 4 with inserted empty row", 4, grid.getRowCount());
		assertEquals("Row 1 should be empty", "", grid.getValueAt(0,0));
		assertEquals("Row 3's data should be in row 2", newRowdata, grid.getValueAt(1,0));
		assertEquals("Row 2's data should now be in row 3", row2DataColumn1, grid.getValueAt(2,0));
		
	}
	
	public void testArrayBoundries() throws Exception
	{
		GridData grid = new GridData(gridSpec2Colunns, noReusableChoices);
		
		String[] row1Data = {"column1a"};
		
		GridFieldSpec gridSpecWithOneColumn = new GridFieldSpec();
		gridSpecWithOneColumn.addColumn(FieldSpec.createStandardField("a", new FieldTypeNormal()));
		GridRow row1 = new GridRow(gridSpecWithOneColumn, noReusableChoices);
		fillGridRow(row1, row1Data);
		try
		{
			grid.addRow(row1);
			fail("Should have thrown for too few columns");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}
		//assertEquals("row count should still be at 1", 1, grid.getRowCount());

		String[] row2Data = {"column1b", "column2b", "column3b"};

		GridFieldSpec gridSpecWithThreeColumns = new GridFieldSpec();
		gridSpecWithThreeColumns.addColumn(FieldSpec.createStandardField("a", new FieldTypeNormal()));
		gridSpecWithThreeColumns.addColumn(FieldSpec.createStandardField("b", new FieldTypeNormal()));
		gridSpecWithThreeColumns.addColumn(FieldSpec.createStandardField("c", new FieldTypeNormal()));
		GridRow row2 = new GridRow(gridSpecWithThreeColumns, noReusableChoices);
		fillGridRow(row2, row2Data);
			
		try
		{
			grid.addRow(row2);
			fail("Should have thrown 4 columns");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}

		try
		{
			grid.getValueAt(-1,1);
			fail("get at Row at -1 worked?");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}

		try
		{
			grid.getValueAt(2,1);
			fail("get at Row at 2 worked?");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}
		String data1 = "column1 data";
		String data2 = "column2 data";
		String[] rowValidData = {data1, data2};
		GridRow rowValid = new GridRow(gridSpec2Colunns, noReusableChoices);
		fillGridRow(rowValid, rowValidData);
		grid.addRow(rowValid);

		try
		{
			grid.getValueAt(0,-1);
			fail("get at Column at -1 worked?");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}

		try
		{
			grid.getValueAt(0,3);
			fail("get at Column at 3 worked?");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}
	
		assertEquals(data1, grid.getValueAt(0,0));
		assertEquals(data2, grid.getValueAt(0,1));

		String data1a = "column1 data";
		String data2a = "column2 data";
		String[] rowValidaData = {data1a, data2a};
		GridRow rowValida = new GridRow(gridSpec2Colunns, noReusableChoices);
		fillGridRow(rowValida, rowValidaData);
		grid.addRow(rowValida);
		assertEquals(data1a, grid.getValueAt(1,0));
		assertEquals(data2a, grid.getValueAt(1,1));

		String modifiedData = "new Text";
		try
		{
			grid.setValueAt(modifiedData,-1,1);
			fail("set at Row at -1 worked?");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}

		try
		{
			grid.setValueAt(modifiedData,2,1);
			fail("set at Row at 2 worked?");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}
		try
		{
			grid.setValueAt(modifiedData,0,-1);
			fail("set at Column at -1 worked?");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}

		try
		{
			grid.setValueAt(modifiedData,0,3);
			fail("set at Column at 3 worked?");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}
		
		assertEquals(data2, grid.getValueAt(0,1));
		grid.setValueAt(modifiedData,0,1);
		assertEquals(modifiedData, grid.getValueAt(0,1));
		
		try
		{
			grid.deleteRow(50);
			fail("Should have thrown invalid row");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}
		
		try
		{
			grid.deleteRow(-1);
			fail("Should have thrown invalid negative row");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}

		try
		{
			grid.insertRow(rowValida, 50);
			fail("Should have thrown invalid row insertion point");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}
		
		try
		{
			grid.insertRow(rowValida, -1);
			fail("Should have thrown invalid negative row insertion point");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}
	}

	private void fillGridRow(GridRow row2, String[] row2Data)
	{
		for(int col = 0; col < row2Data.length; ++col)
			row2.setCellText(col, row2Data[col]);
	}
	
	public void testXmlGridLoader() throws Exception
	{
		GridData original = createSampleGridWithData();
		String xml = original.getXmlRepresentation();
		GridData loaded = new GridData(createSampleGridSpec(), noReusableChoices);
		loaded.addEmptyRow();
		loaded.setFromXml(xml);
		assertEquals("Columns?", original.getColumnCount(), loaded.getColumnCount());
		assertEquals("Rows?", original.getRowCount(), loaded.getRowCount());
		assertEquals(xml, loaded.getXmlRepresentation());
		assertEquals("unescaped xml?", original.getValueAt(0,1), loaded.getValueAt(0,1));
	}
	
	public void testEmptyGridText() throws Exception
	{
		GridData grid = new GridData(gridSpec2Colunns, noReusableChoices);
		grid.setFromXml("");
		assertEquals("No first row?", 1, grid.getRowCount());
		assertEquals("column 1 not empty?", "", grid.getValueAt(0,0));
		assertEquals("column 2 not empty?", "", grid.getValueAt(0,1));
		assertEquals("Should return an empty string for no data", "", grid.getXmlRepresentation());
	}
	
	public void testRemoveTrailingBlankRows() throws Exception
	{
		GridData grid = new GridData(gridSpec2Colunns, noReusableChoices);
		assertTrue("empty grid should be empty", grid.isEmpty());
		assertEquals("should have 0 rows", 0, grid.getRowCount());
		grid.addEmptyRow();
		assertEquals("should now have 1 rows", 1, grid.getRowCount());
		grid.removeTrailingBlankRows();
		assertEquals("should have zero rows now after removal of trailing blank rows", 0, grid.getRowCount());
		
		GridData gridWithData = createSampleGridWithData();
		assertFalse("grid should not be empty", gridWithData.isEmpty());
		assertEquals("Should have 2 rows", 2, gridWithData.getRowCount());
		gridWithData.removeTrailingBlankRows();
		assertEquals("Should still have 2 row", 2, gridWithData.getRowCount());
		gridWithData.addEmptyRow();
		gridWithData.addEmptyRow();
		assertEquals("Should now have 4 rows", 4, gridWithData.getRowCount());
		gridWithData.removeTrailingBlankRows();
		assertEquals("Should now have 2 row after blank row removal", 2, gridWithData.getRowCount());
	}
	
	public void testAddColumns() throws Exception
	{
		GridFieldSpec spec = new GridFieldSpec();
		FieldSpecCollection expectedFieldSpecs = createSampleFieldSpecCollection();
		spec.addColumns(expectedFieldSpecs);
		
		FieldSpecCollection actualFieldSpecs = new FieldSpecCollection();
		for (int index = 0; index < spec.getColumnCount(); ++index)
		{
			actualFieldSpecs.add(spec.getFieldSpec(index));
		}
		
		assertEquals("Incorrect number of fieldspecs copied?", expectedFieldSpecs.size(), actualFieldSpecs.size());
		assertEquals("Incrrect fields specs copied?", expectedFieldSpecs, actualFieldSpecs);
	}

	public static GridData createSampleGridWithData() throws Exception
	{
		GridData grid = createSampleGrid();
		grid.addEmptyRow();
		grid.setValueAt(SAMPLE_DATA1, 0, 0);
		grid.setValueAt(SAMPLE_DATA2_RAW, 0, 1);
		grid.addEmptyRow();
		grid.setValueAt(SAMPLE_DATA3, 1, 0);
		grid.setValueAt(SAMPLE_DATA4, 1, 1);
		return grid;
	}

	public static GridData createSampleGridWithOneEmptyRow() throws Exception
	{
		GridData grid = createSampleGrid();
		GridRow row1 = GridRow.createEmptyRow(grid.getSpec(), noReusableChoices);
		grid.addRow(row1);
		return grid;
	}

	public static GridData createSampleGrid() throws UnsupportedFieldTypeException
	{
		GridFieldSpec spec = createSampleGridSpec();
		GridData grid = new GridData(spec, noReusableChoices);
		return grid;
	}

	public static GridFieldSpec createSampleGridSpec() throws UnsupportedFieldTypeException
	{
		GridFieldSpec spec = new GridFieldSpec();
		FieldSpecCollection sampleFieldSpecs = createSampleFieldSpecCollection();
		for (int index = 0; index < sampleFieldSpecs.size(); ++index)
		{
			spec.addColumn(sampleFieldSpecs.get(index));
		}
		
		return spec;
	}
	
	private static FieldSpecCollection createSampleFieldSpecCollection()
	{
		FieldSpecCollection sampleFieldSpecCollection = new FieldSpecCollection();
		sampleFieldSpecCollection.add(FieldSpec.createCustomField("a", "Column 1", new FieldTypeNormal()));
		sampleFieldSpecCollection.add(FieldSpec.createCustomField("b", "Column 2", new FieldTypeNormal()));
		
		return sampleFieldSpecCollection;
	}

	static public final String SAMPLE_DATA1 = "data1";
	static public final String SAMPLE_DATA2_RAW = "<&data2>";
	static public final String SAMPLE_DATA3 = "data3";
	static public final String SAMPLE_DATA4 = "data4";
	
	private GridFieldSpec gridSpec2Colunns;
	private static PoolOfReusableChoicesLists noReusableChoices;
}
