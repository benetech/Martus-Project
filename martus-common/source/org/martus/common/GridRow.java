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

import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlStringLoader;
import org.martus.util.xml.XmlUtilities;
import org.xml.sax.SAXParseException;


public class GridRow
{
	public GridRow(GridFieldSpec gridSpecToUse, PoolOfReusableChoicesLists reusableChoicesLists)
	{
		gridSpec = gridSpecToUse;
		data = new MartusField[gridSpec.getColumnCount()];
		for(int i = 0; i < getColumnCount(); ++i)
			data[i] = new MartusField(gridSpec.getFieldSpec(i), reusableChoicesLists);
	}
	
	public int getColumnCount()
	{
		return data.length;
	}
	
	static public GridRow createEmptyRow(GridFieldSpec gridSpec, PoolOfReusableChoicesLists reusableChoicesLists)
	{
		return new GridRow(gridSpec, reusableChoicesLists);
	}
	
	public boolean isEmptyRow()
	{
		GridRow emptyRow = GridRow.createEmptyRow(gridSpec, PoolOfReusableChoicesLists.EMPTY_POOL);
		for(int column = 0; column < getColumnCount(); ++column)
		{
			if(!emptyRow.getCellText(column).equals(getCellText(column)))
				return false;
		}
		return true;
	}

	public void setCellText(int column, String value) throws ArrayIndexOutOfBoundsException
	{
		data[column].setData(value);
	}
	
	public String getCellText(int column) throws ArrayIndexOutOfBoundsException
	{
		return data[column].getData();
	}
	
	public String getXmlRepresentation()
	{
		String rowXml = MartusXml.getTagStart(ROW_TAG) + MartusXml.newLine ;
		int columns = getColumnCount();
		for(int j= 0; j < columns; ++j)
		{
			String rawCellText = getCellText(j);
			rowXml += MartusXml.getTagStart(COLUMN_TAG) + XmlUtilities.getXmlEncoded(rawCellText) + MartusXml.getTagEnd(COLUMN_TAG);
		}
		rowXml += MartusXml.getTagEnd(ROW_TAG);
		return rowXml;
	}

	public static class XmlGridRowLoader extends SimpleXmlDefaultLoader
	{
		public XmlGridRowLoader(GridFieldSpec gridSpec, PoolOfReusableChoicesLists reusableChoicesLists)
		{
			super(ROW_TAG);
			thisRow = new GridRow(gridSpec, reusableChoicesLists);
		}
		
		public GridRow getGridRow()
		{
			return thisRow;
		}

		public SimpleXmlDefaultLoader startElement(String tag)
			throws SAXParseException
		{
			if(tag.equals(COLUMN_TAG))
				return new SimpleXmlStringLoader(tag);
			return super.startElement(tag);
		}
		
		public void endElement(String tag, SimpleXmlDefaultLoader ended)
				throws SAXParseException
		{
			if(tag.equals(COLUMN_TAG))
			{
				String cellText = ((SimpleXmlStringLoader)ended).getText();
				addRowIfRoom(cellText);
			}
			super.endElement(tag, ended);
		}

		//Note: Template switching may cause column counts to differ
		//FIXME: add unit tests with a template having more grid columns than the new template with the same grid field tag.
		public void addRowIfRoom(String cellText)
		{
			if(thisRow.getColumnCount() >= currentColumn+1)
				thisRow.setCellText(currentColumn++, cellText);
		}
		
		GridRow thisRow;
		int currentColumn;

	}
	
	MartusField[] data;
	public static final String COLUMN_TAG = "Column";
	public static final String ROW_TAG = "Row";
	GridFieldSpec gridSpec;
}
