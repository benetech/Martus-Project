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
package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.martus.client.reports.SpecTableModel;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;

public class UiReportFieldSelectorPanel extends JPanel
{
	public UiReportFieldSelectorPanel(UiMainWindow mainWindow, FieldSpec[] rawFieldSpecs)
	{
		super(new BorderLayout());
		model = new SpecTableModel(rawFieldSpecs, mainWindow.getLocalization());
		table = new UiTable(model);
		table.setMaxGridWidth(40);
		table.useMaxWidth();
		table.setFocusable(true);
		table.createDefaultColumnsFromModel();
		table.setColumnSelectionAllowed(false);
		add(new UiScrollPane(table), BorderLayout.CENTER);
	}
	
	public UiTable getTable()
	{
		return table;
	}
	
	public void addSpecs(FieldSpec[] fieldSpecsToAdd)
	{
		model.addSpecs(fieldSpecsToAdd);		
	}
	
	public void removeSpec(int row)
	{
		model.removeSpec(row);	
	}
	
	public int getSpecCount()
	{
		return table.getRowCount();
	}

	public void selectRow(int rowToSelect)
	{
		table.getSelectionModel().setSelectionInterval(rowToSelect, rowToSelect);
	}
	
	public void moveSpecUp(int selectedRow)
	{
		model.moveSpecUp(selectedRow);
	}

	public void moveSpecDown(int selectedRow)
	{
		model.moveSpecDown(selectedRow);
	}
	
	public int getSelectedRow()
	{
		return table.getSelectedRow();
	}
	
	public FieldSpec[] getAllItems()
	{
		int rows = table.getRowCount();
		FieldSpec[] items = new FieldSpec[rows];
		for(int i = 0; i < rows; ++i)
			items[i] = model.getSpec(i);
		return items;
	}

	public FieldSpec[] getSelectedItems()
	{
		int[] selectedRows = table.getSelectedRows();
		FieldSpec[] selectedItems = new FieldSpec[selectedRows.length];
		for(int i = 0; i < selectedRows.length; ++i)
			selectedItems[i] = model.getSpec(selectedRows[i]);
		return selectedItems;
	}
	
	SpecTableModel model;
	UiTable table;
}
