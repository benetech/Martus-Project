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
package org.martus.common.fieldspec;

import java.util.Iterator;
import java.util.Vector;

import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.MartusXml;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.XmlUtilities;
import org.xml.sax.SAXParseException;


public class GridFieldSpec extends FieldSpec
{
	public GridFieldSpec()
	{
		super(new FieldTypeGrid());
		columns = new Vector();
		columnZeroLabel = " ";
	}
	
	protected boolean allowUserDefaultValue()
	{
		return false;
	}

	public int getColumnCount()
	{
		return columns.size();
	}
	
	public String getColumnLabel(int column)
	{
		FieldSpec columnSpec = (FieldSpec)columns.get(column);
		return columnSpec.getLabel();
	}
	
	public FieldType getColumnType(int column)
	{
		FieldSpec columnSpec = (FieldSpec)columns.get(column);
		return columnSpec.getType();
	}
	
	public FieldSpec getFieldSpec(int column)
	{
		return (FieldSpec)columns.get(column);
	}

	public FieldSpec findColumnSpecByLabel(String gridColumnLabel)
	{
		for(int column = 0; column < getColumnCount(); ++column)
		{
			FieldSpec columnSpec = getFieldSpec(column);
			if(columnSpec.getLabel().equals(gridColumnLabel))
				return columnSpec;
		}
		
		return null;
	}

	public String convertStoredToSearchable(String storedData, PoolOfReusableChoicesLists reusableChoicesLists, MiniLocalization localization)
	{
		Formatter formatter = new FormatterForSearching();
		return getFormatted(storedData, reusableChoicesLists, localization, formatter);
	}

	public String convertStoredToExportable(String storedData, PoolOfReusableChoicesLists reusableChoicesLists, MiniLocalization localization)
	{
		Formatter formatter = new FormatterForExporting();
		return getFormatted(storedData, reusableChoicesLists, localization, formatter);
	}
	
	private String getFormatted(String storedData, PoolOfReusableChoicesLists reusableChoicesLists, MiniLocalization localization, Formatter formatter)
	{
		GridData data = new GridData(this, reusableChoicesLists);
		try
		{
			data.setFromXml(storedData);
		}
		catch (Exception e)
		{
			// FIXME: What should we really do here???
			e.printStackTrace();
			return "";
		}
		StringBuffer result = new StringBuffer();
		result.append(formatter.getVeryBeginning(this));
		for(int row = 0; row < data.getRowCount(); ++row)
		{
			result.append(formatter.getRowBeginning());
			for(int col = 0; col < data.getColumnCount(); ++col)
			{
				result.append(formatter.getCellBeginning());
				String rawData = data.getValueAt(row, col);
				final FieldSpec cellSpec = getFieldSpec(col);
				String searchableData = formatter.getFormattedCell(rawData, cellSpec, reusableChoicesLists, localization);
				result.append(searchableData);
				result.append(formatter.getCellEnd());
			}
			result.append(formatter.getRowEnd());
		}
		result.append(formatter.getVeryEnd());
		return new String(result);
	}
	
	abstract class Formatter
	{
		abstract public String getFormattedCell(String rawData, FieldSpec cellSpec, PoolOfReusableChoicesLists resuableChoicesListst, MiniLocalization localization);
		
		public String getVeryBeginning(GridFieldSpec gridSpec)
		{
			return "";
		}
		
		public String getVeryEnd()
		{
			return "";
		}
		
		public String getRowBeginning()
		{
			return "";
		}
		
		public String getRowEnd()
		{
			return "";
		}

		public String getCellBeginning()
		{
			return "";
		}
		
		public String getCellEnd()
		{
			return "";
		}
	}
	
	class FormatterForSearching extends Formatter
	{
		public String getFormattedCell(String rawData, FieldSpec cellSpec, PoolOfReusableChoicesLists reusableChoicesLists, MiniLocalization localization)
		{
			return cellSpec.convertStoredToSearchable(rawData, reusableChoicesLists, localization);
		}
		
		public String getRowEnd()
		{
			return "\n";
		}
		
		public String getCellEnd()
		{
			return "\t";
		}
	}
	
	class FormatterForExporting extends Formatter
	{
		public String getFormattedCell(String rawData, FieldSpec cellSpec, PoolOfReusableChoicesLists reusableChoicesLists, MiniLocalization localization)
		{
			return XmlUtilities.getXmlEncoded(cellSpec.convertStoredToExportable(rawData, reusableChoicesLists, localization));
		}
		
		public String getVeryBeginning(GridFieldSpec gridSpec)
		{
			return "<GridData columns='" + gridSpec.getColumnCount() + "'>\n";
		}
		
		public String getVeryEnd()
		{
			return "</GridData>\n";
		}
		
		public String getRowBeginning()
		{
			return "<Row>\n";
		}
		
		public String getRowEnd()
		{
			return "</Row>\n";
		}

		public String getCellBeginning()
		{
			return "<Column>";
		}
		
		public String getCellEnd()
		{
			return "</Column>\n";
		}
	}

	public class UnsupportedFieldTypeException extends Exception
	{
		public UnsupportedFieldTypeException(FieldType gotType)
		{
			super(gotType.toString());
		}
	}

	public void addColumns(FieldSpecCollection fieldSpecsToAdd) throws UnsupportedFieldTypeException
	{
		for (int index = 0; index < fieldSpecsToAdd.size(); ++index)
		{
			addColumn(fieldSpecsToAdd.get(index));
		}
	}
	
	public void addColumn(FieldSpec columnSpec) throws UnsupportedFieldTypeException
	{
		if(!isValidColumnType(columnSpec.getType()))
			throw new UnsupportedFieldTypeException(columnSpec.getType());
		columns.add(columnSpec);
	}
	
	public boolean isValidColumnType(FieldType columnType)
	{
		return isValidGridColumnType(columnType);
	}

	public static boolean isValidGridColumnType(FieldType columnType)
	{
		if(columnType.isString())
			return true;
		if(columnType.isDropdown())
			return true;
		if(columnType.isBoolean())
			return true;
		if(columnType.isDate())
			return true;
		if(columnType.isDateRange())
			return true;
		if(columnType.isPopUpTree())
			return true;
		
		return false;
	}
	
	public void setColumnZeroLabel(String columnZeroLabelToUse)
	{
		columnZeroLabel = columnZeroLabelToUse;
	}
	
	public String getColumnZeroLabel()
	{
		return columnZeroLabel;
	}
	
	public Vector getAllColumnLabels()
	{
		Vector columnLabels = new Vector();
		for(Iterator iter = columns.iterator(); iter.hasNext();)
		{
			FieldSpec element = (FieldSpec) iter.next();
			columnLabels.add(element.getLabel());
		}
		return columnLabels;
	}
	
	public boolean hasColumnLabel(String gridColumnLabel)
	{
		return getAllColumnLabels().contains(gridColumnLabel);
	}

	public String getDetailsXml()
	{
		StringBuffer xml = new StringBuffer(); 
		xml.append(MartusXml.getTagStartWithNewline(GRID_SPEC_DETAILS_TAG));
		for(int i = 0 ; i < getColumnCount(); ++i)
		{
			FieldSpec thisColumn = (FieldSpec)columns.get(i);
			xml.append(thisColumn.toXml(GRID_COLUMN_TAG));
		}
		xml.append(MartusXml.getTagEnd(GRID_SPEC_DETAILS_TAG));
		
		return xml.toString();
	}
	
	static class GridSpecDetailsLoader extends SimpleXmlDefaultLoader
	{
		public GridSpecDetailsLoader(GridFieldSpec spec)
		{
			super(GRID_SPEC_DETAILS_TAG);
			this.spec = spec;
		}
		
		public SimpleXmlDefaultLoader startElement(String tag)
			throws SAXParseException
		{
			if(tag.equals(GRID_COLUMN_TAG))
				return new FieldSpec.XmlFieldSpecLoader(tag);

			return super.startElement(tag);
		}

		public void endElement(String thisTag, SimpleXmlDefaultLoader ended)
			throws SAXParseException
		{
			FieldSpec specToAdd = null;
			if(thisTag.equals(GRID_COLUMN_TAG))//Legacy XML
			{
				specToAdd = ((FieldSpec.XmlFieldSpecLoader)ended).getFieldSpec();
				if(specToAdd.getType().isUnknown())
					specToAdd.setType(new FieldTypeNormal());
			}
			else if (thisTag.equals(FIELD_SPEC_XML_TAG))
			{
				specToAdd = ((FieldSpec.XmlFieldSpecLoader)ended).getFieldSpec();
			}
			else
			{
				super.endElement(thisTag, ended);
				return;
			}
			try
			{
				spec.addColumn(specToAdd);
			}
			catch(UnsupportedFieldTypeException e)
			{
				e.printStackTrace();
				throw new SAXParseException("UnsupportedFieldTypeException", null);
			}
		}		
		GridFieldSpec spec;
	}
	
	public final static String GRID_SPEC_DETAILS_TAG = "GridSpecDetails";
	public final static String GRID_COLUMN_TAG = "Column";
	public final static String GRID_COLUMN_LABEL_TAG = "Label";
	
	Vector columns;
	String columnZeroLabel;
}
