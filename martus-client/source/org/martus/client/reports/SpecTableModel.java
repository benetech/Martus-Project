/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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
package org.martus.client.reports;

import java.util.Arrays;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FieldSpec;

public class SpecTableModel extends AbstractTableModel
{
	public SpecTableModel(FieldSpec[] specsToUse, MiniLocalization localizationToUse)
	{
		specs = new Vector();
		addSpecs(specsToUse);
		localization = localizationToUse;
	}
	
	public void addSpecs(FieldSpec[] specsToAdd)
	{
		if(specsToAdd == null)
			return;
		specs.addAll(Arrays.asList(specsToAdd)); 
		fireTableDataChanged();
	}
	
	public void removeSpec(int row)
	{
		if(row == -1)
			return;
		specs.remove(row);
		fireTableRowsDeleted(row, row);
	}	
	
	public void moveSpecUp(int row)
	{
		if(row < 1)
			return;
		int destinationRow = row - 1;
		swapRows(row, destinationRow);
	}

	public void moveSpecDown(int row)
	{
		if(row < 0 || row >= (specs.size() -1))
			return;
		int destinationRow = row + 1;
		swapRows(row, destinationRow);
	}

	private void swapRows(int selectedRow, int destinationRow)
	{
		Object destination = specs.get(destinationRow);
		Object selection = specs.get(selectedRow);
		specs.setElementAt(selection, destinationRow);
		specs.setElementAt(destination, selectedRow);
		fireTableDataChanged();
	}
	
	public FieldSpec getSpec(int row)
	{
		return (FieldSpec)specs.get(row);
	}
	
	public int getColumnCount()
	{
		return columnTags.length;
	}

	public String getColumnName(int column)
	{
		return localization.getButtonLabel(columnTags[column]);
	}

	public int getRowCount()
	{
		return specs.size();
	}

	public Object getValueAt(int row, int column)
	{
		FieldSpec spec = getSpec(row);
		switch(column)
		{
			case 0: return spec.getLabel();
			case 1: return localization.getFieldLabel("FieldType" + spec.getType().getTypeName());
			case 2: return spec.getTag();
			default: throw new RuntimeException("Unknown column: " + column);
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}

	public Class getColumnClass(int columnIndex)
	{
		return String.class;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		throw new RuntimeException("Not supported");
	}
	
	static final String[] columnTags = {"FieldLabel", "FieldType", "FieldTag"};

	Vector specs;
	MiniLocalization localization;
}
