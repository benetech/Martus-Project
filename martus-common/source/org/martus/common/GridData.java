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
package org.martus.common;

import java.io.IOException;
import java.util.Vector;
import javax.xml.parsers.ParserConfigurationException;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class GridData
{
	public GridData(GridFieldSpec spec, PoolOfReusableChoicesLists reusableChoicesListsToUse)
	{
		gridSpec = spec;
		reusableChoicesLists = reusableChoicesListsToUse;
		clear();
	}

	public void clear()
	{
		rows = new Vector();
	}

	public void addEmptyRow()
	{
		GridRow row = GridRow.createEmptyRow(gridSpec, getReusableChoicesLists());
		addRow(row);
	}
	
	public void addEmptyRowAt(int insertAt) throws ArrayIndexOutOfBoundsException
	{
		GridRow row = GridRow.createEmptyRow(gridSpec, getReusableChoicesLists());
		insertRow(row, insertAt);
	}

	
	public void deleteRow(int rowToDelete) throws ArrayIndexOutOfBoundsException
	{
		rows.remove(rowToDelete);
	}
	
	public int getRowCount()
	{
		return rows.size();
	}
	
	public int getColumnCount()
	{
		return gridSpec.getColumnCount();
	}
	
	public GridFieldSpec getSpec()
	{
		return gridSpec;
	}
	
	public PoolOfReusableChoicesLists getReusableChoicesLists()
	{
		return reusableChoicesLists;
	}

	public GridRow getRow(int row)
	{
		return (GridRow)rows.get(row);
	}
	
	public void setValueAt(String data, int row, int col) throws ArrayIndexOutOfBoundsException
	{
		GridRow rowData = getRow(row);
		rowData.setCellText(col, data);
	}

	public String getValueAt(int row, int col) throws ArrayIndexOutOfBoundsException
	{
		GridRow rowData = getRow(row);
		return rowData.getCellText(col);
	}
	
	
	public void addRow(GridRow rowToAdd) throws ArrayIndexOutOfBoundsException
	{
		ensureValidColumnCount(rowToAdd);
		rows.add(rowToAdd);
	}
	
	public void insertRow(GridRow rowToAdd, int insertAt) throws ArrayIndexOutOfBoundsException
	{
		ensureValidColumnCount(rowToAdd);
		rows.insertElementAt(rowToAdd, insertAt);
	}

	private void ensureValidColumnCount(GridRow rowToAdd) 
	{
		if(rowToAdd.getColumnCount() != getColumnCount())
			throw new ArrayIndexOutOfBoundsException("Column out of bounds");
	}
	
	public void setFromXml(String xmlData) throws IOException, ParserConfigurationException, SAXException
	{
		rows.clear();
		if(xmlData.equals(""))
		{
			addEmptyRow();
			return;
		}
		GridData.XmlGridDataLoader loader = new GridData.XmlGridDataLoader(this);
		SimpleXmlParser.parse(loader, xmlData);
	}
	
	public boolean isEmpty()
	{
		return isEmpty(rows);
	}
	
	private boolean isEmpty(Vector rowsToCheck)
	{
		for(int i = 0; i < rowsToCheck.size(); ++i)
		{
			GridRow contents = (GridRow)rowsToCheck.get(i);
			for(int j = 0; j < getColumnCount(); ++ j)
			{
				if(contents.getCellText(j).length() != 0 )
					return false;
			}
		}
		return true;
	}
	
	public void removeTrailingBlankRows()
	{
		removeTrailingBlankRows(rows);
	}
	
	public void removeTrailingBlankRows(Vector rowsToModify)
	{
		for(int i = rowsToModify.size()-1; i>=0; i--)
		{
			if(!((GridRow)rowsToModify.get(i)).isEmptyRow())
				return;
			rowsToModify.remove(i);
		}		
	}
	
	public String getXmlRepresentation()
	{
		Vector copyOfRows = new Vector(rows);
		
		removeTrailingBlankRows(copyOfRows);
		if(isEmpty(copyOfRows))
			return "";
		String result = new String();
		result += MartusXml.getTagStart(GRID_DATA_TAG, GRID_ATTRIBUTE_COLUMNS, Integer.toString(getColumnCount()))+ MartusXml.newLine;
		for(int i = 0; i< copyOfRows.size(); ++i)
		{
			GridRow contents = (GridRow)copyOfRows.get(i);
			result += contents.getXmlRepresentation();
		}
		result += MartusXml.getTagEnd(GRID_DATA_TAG);
		return result;
	}

	public static class XmlGridDataLoader extends SimpleXmlDefaultLoader
	{
		public XmlGridDataLoader(GridData gridToLoad)
		{
			super(GRID_DATA_TAG);
			grid = gridToLoad;
		}

		public GridData getGridData()
		{
			return grid;
		}
		
		public void startDocument(Attributes attrs) throws SAXParseException
		{
			String optionalColumnCount = attrs.getValue(GridData.GRID_ATTRIBUTE_COLUMNS);
			if(optionalColumnCount != null)
			{
				int gotCols = Integer.parseInt(optionalColumnCount);
				int expectedCols = grid.getColumnCount();
				if(gotCols != expectedCols)
					System.out.println("XmlGridDataLoader.startDocument: wrong column count! expected " + expectedCols + " but was " + gotCols);
			}
			super.startDocument(attrs);
		}

		public SimpleXmlDefaultLoader startElement(String tag)
				throws SAXParseException
		{
			if(tag.equals(GridRow.ROW_TAG))
				return new GridRow.XmlGridRowLoader(grid.getSpec(), grid.getReusableChoicesLists());
			return super.startElement(tag);
		}
		
		public void endElement(String tag, SimpleXmlDefaultLoader ended)
				throws SAXParseException
		{
			if(tag.equals(GridRow.ROW_TAG))
				grid.addRow(((GridRow.XmlGridRowLoader)ended).getGridRow()); 
			super.endElement(tag, ended);
		}
		GridData grid;
	}

	public static final String GRID_DATA_TAG = "GridData";
	public static final String GRID_ATTRIBUTE_COLUMNS = "columns";
	private Vector rows;
	private GridFieldSpec gridSpec;
	private PoolOfReusableChoicesLists reusableChoicesLists;
}
