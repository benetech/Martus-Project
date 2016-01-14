/* 
Copyright 2005-2009, Foundations of Success, Bethesda, Maryland 
(on behalf of the Conservation Measures Partnership, "CMP") and 
Beneficent Technology, Inc. ("Benetech"), Palo Alto, California. 

This file is part of Miradi

Miradi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License version 3, 
as published by the Free Software Foundation.

Miradi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Miradi.  If not, see <http://www.gnu.org/licenses/>. 
*/ 

package org.miradi.utils;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.TableModel;

public class TableWithSunBugWorkarounds extends JTable
{
	public TableWithSunBugWorkarounds(TableModel model)
	{
		super(model);
		
		// this property is set due to a JTable bug#4724980 
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}

	// this is overridden to work around JTable bug#4330950
	// where if you click on a heading during a cell edit, the 
	// edits are silently discarded
	public void columnMoved(TableColumnModelEvent e) 
	{
		if (isEditing()) {
			cellEditor.stopCellEditing();
		}
		super.columnMoved(e);
	}

	// this is overridden to work around JTable bug#4330950
	// where if you click on a heading during a cell edit, the 
	// edits are silently discarded
	public void columnMarginChanged(ChangeEvent e) 
	{
		if (isEditing()) {
			cellEditor.stopCellEditing();
		}
		super.columnMarginChanged(e);
	}
	
	public int getSelectedRow()
	{
		// NOTE: Java sometimes returns non-empty selected 
		// rows when the table is empty, so check that first.
		// JAVA BUG #4247579,  Java 1.4.2 2006-10-27 kbs
		if(getRowCount() == 0)
			return -1;
		
		return super.getSelectedRow();
	}
	
	public int[] getSelectedRows()
	{
		// NOTE: Java sometimes returns non-empty selected 
		// rows when the table is empty, so check that first.
		// JAVA BUG #4247579,  Java 1.4.2 2006-10-27 kbs
		if(getRowCount() == 0)
			return new int[0];
		
		return super.getSelectedRows();
	}

	public int getSelectedRowCount()
	{
		// NOTE: Java sometimes returns non-empty selected 
		// rows when the table is empty, so check that first.
		// JAVA BUG #4247579,  Java 1.4.2 2006-10-27 kbs
		if(getRowCount() == 0)
			return 0;
		
		return super.getSelectedRowCount();
	}

}
