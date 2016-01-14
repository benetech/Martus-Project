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

To the extent this copyrighted software code is used in the 
Miradi project, it is subject to a royalty-free license to 
members of the Conservation Measures Partnership when 
used with the Miradi software as specified in the agreement 
between Benetech and WCS dated 5/1/05.
*/
package org.martus.swing;

import java.util.EventObject;

import javax.swing.CellEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableModel;


public class UiTableWithCellEditingProtection extends UiTable
{

	public UiTableWithCellEditingProtection()
	{
		super();
	}
	
	public UiTableWithCellEditingProtection(TableModel model)
	{
		super(model);
	}
	
	public boolean editCellAt(int row, int col, EventObject event) 
	{
		tableBeingEdited = this;
		return super.editCellAt(row, col, event);
	}
	
	public static void savePendingEdits()
	{
		if (tableBeingEdited != null)
			tableBeingEdited.saveCellContents();
	}

	// This is needed to work around a horrible quirk in swing:
	// If an editor is active when a column is resized,
	// the editing is not stopped, so the edits are lost.
	// Even the editing component is not told that it is losing focus.
	// To reproduce: 
	// - Click once in a text cell, enter text, start widening a column
	public void columnMarginChanged(ChangeEvent event)
	{
//		System.out.println("columnMarginChanged saveCellContents");
		saveCellContents();
		super.columnMarginChanged(event);
	}
	
	private void saveCellContents()
	{
		CellEditor editor = getCellEditor();
		if(editor == null)
			return;
//		System.out.println("saveCellContents stopCellEditing");
		editor.stopCellEditing();
	}
	
	private static UiTableWithCellEditingProtection tableBeingEdited = null;
}
