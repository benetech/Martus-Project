/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.common.field;

import org.martus.common.GridData;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.xml.XmlUtilities;

public class MartusGridField extends MartusField
{
	public MartusGridField(FieldSpec specToUse, PoolOfReusableChoicesLists reusableChoicesLists)
	{
		super(specToUse, reusableChoicesLists);
	}
	
	public MartusField createClone()
	{
		MartusField clone = new MartusGridField(getFieldSpec(), getReusableChoicesLists());
		clone.setData(getData());
		return clone;
	}
	


	public MartusField getSubField(String tag, MiniLocalization localization)
	{
		String sanitizedLabel = sanitizeLabel(tag);
		try
		{
			if(getData().length() == 0)
				return new EmptyMartusFieldWithInfiniteSubFields(tag);

			GridFieldSpec gridSpec = (GridFieldSpec)spec;
			for(int i = 0; i < gridSpec.getColumnCount(); ++i)
			{
				FieldSpec thisColumnSpec = gridSpec.getFieldSpec(i);
				final String thisLabel = sanitizeLabel(thisColumnSpec.getLabel());
				if(thisLabel.equals(sanitizedLabel))
				{
					MartusField field = new MartusSearchableGridColumnField(this, i, getReusableChoicesLists());
					return field;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return new EmptyMartusFieldWithInfiniteSubFields(tag);
	}
	
	static public String sanitizeLabel(String rawLabel)
	{
		return rawLabel.replaceAll("\\.", " ");
	}
	
	public GridFieldSpec getGridFieldSpec()
	{
		return (GridFieldSpec)getFieldSpec();
	}
	
	public GridData getGridData() throws Exception
	{
		GridData gridData = new GridData(getGridFieldSpec(), getReusableChoicesLists());
		gridData.setFromXml(getData());
		return gridData;
	}
	
	protected String internalGetHtml(MiniLocalization localization) throws Exception
	{
		GridData gridData = getGridData();
		
		StringBuffer buffer = new StringBuffer();
		String border = "";
		if(gridData.getRowCount() > 1 || gridData.getColumnCount() > 1)
			border = "border='1'";
		buffer.append("<table " + border + " >");
		for(int row = 0; row < gridData.getRowCount(); ++row)
		{
			buffer.append("<tr>");
			for(int col = 0; col < gridData.getColumnCount(); ++col)
			{
				buffer.append("<td>");
				String rawCellData = gridData.getValueAt(row, col);
				FieldSpec columnSpec = getGridFieldSpec().getFieldSpec(col);
				String cellData = columnSpec.convertStoredToSearchable(rawCellData, getReusableChoicesLists(), localization);
				buffer.append(XmlUtilities.getXmlEncoded(cellData));
				buffer.append("</td>");
				
			}
			buffer.append("</tr>");
		}
		buffer.append("</table>");
		return buffer.toString();
	}


}
