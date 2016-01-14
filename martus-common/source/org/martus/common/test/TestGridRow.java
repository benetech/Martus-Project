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

import org.martus.common.GridRow;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.xml.SimpleXmlParser;
import org.martus.util.xml.XmlUtilities;


public class TestGridRow extends TestCaseEnhanced
{
	public TestGridRow(String name)
	{
		super(name);
	}
	
	protected void setUp() throws Exception
	{
		super.setUp();
		noReusableChoices = PoolOfReusableChoicesLists.EMPTY_POOL;
	}
	
	public void testBasics() throws Exception
	{
		GridFieldSpec gridSpecWithTwoColumns = TestGridData.createSampleGridSpec();
		GridRow row = new GridRow(gridSpecWithTwoColumns, noReusableChoices);
		assertEquals ("Should start with 2 columns", 2, row.getColumnCount());
		for(int col = 0; col < row.getColumnCount(); ++col)
			assertEquals("column " + col + " not empty?", "", row.getCellText(col));
		
		String item1 = "data1";
		String item2 = "data2";
		String item3 = "data3";
		String item1b = "data1b";
		String item2b = "data2b";
		row.setCellText(0, item1);
		row.setCellText(1, item2);
		assertEquals ("Now should have 2 columns", 2, row.getColumnCount());
		assertEquals("cell 1 didn't come back with correct data", item1, row.getCellText(0));
		assertEquals("cell 2 didn't come back with correct data", item2, row.getCellText(1));

		row.setCellText(0, item1b);
		row.setCellText(1, item2b);
		assertEquals ("Should still have 2 columns", 2, row.getColumnCount());
		assertEquals("cell 1 didn't come back with correct data", item1b, row.getCellText(0));
		assertEquals("cell 2 didn't come back with correct data", item2b, row.getCellText(1));

		int testCell = 1;
		row.setCellText(testCell, item3);
		assertEquals("cell 1 didn't come back with new data", item3, row.getCellText(testCell));
		
		assertEquals("column 0 wrong value?", item1b, row.getCellText(0));
		assertEquals("column 1 wrong value?", item3, row.getCellText(1));
		
		GridRow rowEmpty = GridRow.createEmptyRow(gridSpecWithTwoColumns, noReusableChoices);
		assertEquals ("Should now have 2 empty columns", 2, rowEmpty.getColumnCount());
		for(int col = 0; col < row.getColumnCount(); ++col)
			assertEquals("column " + col + " not empty?", "", rowEmpty.getCellText(col));

	}

	public void testBoundries() throws Exception
	{
		GridFieldSpec gridSpecWithTwoColumns = TestGridData.createSampleGridSpec();
		GridRow row = new GridRow(gridSpecWithTwoColumns, noReusableChoices);
		String item1 = "data1";
		String item2 = "data2";
		String item3 = "data3";

		String[] data2Items = {item1, item2};
		row.setCellText(0, item1);
		row.setCellText(1, item2);
		
		assertEquals ("Now should have 2 columns", data2Items.length, row.getColumnCount());
		assertEquals("cell 1 didn't come back with correct data", item1, row.getCellText(0));
		assertEquals("cell 2 didn't come back with correct data", item2, row.getCellText(1));
	
	
		try
		{
			row.setCellText(-1,item3);
			fail("should have thrown invalid column -1");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}
	
		try
		{
			row.setCellText(2,item3);
			fail("should have thrown invalid column 2");
		}
		catch (ArrayIndexOutOfBoundsException expected)
		{
		}
	}
	
	public void testIsRowEmpty() throws Exception
	{
		GridFieldSpec gridSpecWithTwoColumns = TestGridData.createSampleGridSpec();
		GridRow row = new GridRow(gridSpecWithTwoColumns, noReusableChoices);
		String item1 = "data1";
		String item2 = "data2";

		assertTrue("No data, should be empty", row.isEmptyRow());
		row.setCellText(0, item1);
		row.setCellText(1, item2);
		assertFalse("with data, still empty?", row.isEmptyRow());
		
		
	}
	
	public void testXmlRowLoader() throws Exception
	{
		GridFieldSpec gridSpec = TestGridData.createSampleGridSpec();
		String data1 = "data1";
		String data2Raw = "data2";
		String data2 = XmlUtilities.getXmlEncoded(data2Raw);
		String xml = "<Row>\n<Column>" + data1 + "</Column><Column>" + data2 + "</Column></Row>";
		GridRow.XmlGridRowLoader loader = new GridRow.XmlGridRowLoader(gridSpec, noReusableChoices);
		SimpleXmlParser.parse(loader, xml);
		GridRow row = loader.getGridRow();
		assertEquals(data1, row.getCellText(0));
		assertEquals(data2Raw, row.getCellText(1));
		
	}
	
	private PoolOfReusableChoicesLists noReusableChoices;
}
