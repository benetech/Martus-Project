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
package org.martus.client.swingui.grids;

import java.awt.Component;

import javax.swing.JTable;

import org.martus.client.swingui.fields.UiPopUpFieldChooserEditor;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;

public class GridPopUpTreeCellEditor extends GridCellEditorAndRenderer
{
	GridPopUpTreeCellEditor(UiPopUpFieldChooserEditor fieldChooser)
	{
		super(fieldChooser);
	}
	
	public UiPopUpFieldChooserEditor getPopUpTreeEditor()
	{
		return (UiPopUpFieldChooserEditor)uiField;
	}
	
	public void setFieldSpec(JTable tableToUse, int row, int column)
	{
		GridTable gridTable = (GridTable)tableToUse;
		PopUpTreeFieldSpec treeSpec = (PopUpTreeFieldSpec)gridTable.getFieldSpecForCell(row, column);
		getPopUpTreeEditor().setSpec(treeSpec);
	}

	public Component getTableCellEditorComponent(JTable tableToUse, Object codeString, boolean isSelected, int row, int column)
	{
		setFieldSpec(tableToUse, row, column);
		return super.getTableCellEditorComponent(tableToUse, codeString, isSelected, row, column);
	}

	public Component getTableCellRendererComponent(JTable tableToUse, Object codeString, boolean isSelected, boolean hasFocus, int row, int column)
	{
		setFieldSpec(tableToUse, row, column);
		return super.getTableCellRendererComponent(tableToUse, codeString, isSelected, hasFocus, row, column);
	}
	
	public void spaceWasPressed()
	{
		((UiPopUpFieldChooserEditor)getUiField()).simulateButtonPress();
	}
}
