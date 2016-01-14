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
package org.martus.client.swingui.grids;

import java.awt.Component;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.martus.client.core.ZawgyiLabelUtilities;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.fields.UiFlexiDateEditor;
import org.martus.client.swingui.fields.UiGridDateRangeEditorViewer;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.DateRangeFieldSpec;
import org.martus.common.fieldspec.DateRangeInvertedException;
import org.martus.common.fieldspec.DateTooEarlyException;
import org.martus.common.fieldspec.DateTooLateException;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.swing.UiComboBox;

public class GridDateRangeCellEditor extends GridCellEditorAndRenderer
{
	GridDateRangeCellEditor(UiDialogLauncher dlgLauncherToUse, GridFieldSpec gridSpecToUse, DateRangeFieldSpec cellFieldSpec, MiniLocalization localizationToUse)
	{
		super(new UiGridDateRangeEditorViewer(cellFieldSpec, dlgLauncherToUse.getLocalization()));
		dlgLauncher = dlgLauncherToUse;
		gridSpec = gridSpecToUse;
		localization = localizationToUse;
	}

	public boolean stopCellEditing()
	{
		try
		{
			String displayableGridLabel = ZawgyiLabelUtilities.getDisplayableLabel(gridSpec, getLocalization());
			String displayableColumnLabel = ZawgyiLabelUtilities.getDisplayableLabel(fieldSpecBeingEdited, getLocalization());
			String label = displayableGridLabel + ": " + displayableColumnLabel;
			uiField.validate(fieldSpecBeingEdited, label);
			return super.stopCellEditing();
		}
		catch(DateRangeInvertedException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			if(dlgLauncher.showConfirmDlg("DateRageInvalid", map))
			{
				return false;
			}
			
			return super.stopCellEditing();
		}
		catch(DateTooEarlyException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			map.put("#MinimumDate#", dlgLauncher.getLocalization().convertStoredDateToDisplay(e.getMinimumDate()));
			dlgLauncher.messageDlg(this, "ErrorDateTooEarly", "", map);
			return false;
		}
		catch(DateTooLateException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			map.put("#MaximumDate#", dlgLauncher.getLocalization().convertStoredDateToDisplay(e.getMaximumDate()));
			dlgLauncher.messageDlg(this, "ErrorDateTooLate", "", map);
			return false;
		}
		catch(DataInvalidException e)
		{
			e.printStackTrace();
			dlgLauncher.ShowNotifyDialog("UnexpectedError");
			return true;
		}
	}

	public Component getTableCellEditorComponent(JTable tableToUse, Object stringValue, boolean isSelected, int row, int column) 
	{
		originalDate = super.uiField.getText();
		fieldSpecBeingEdited = ((GridTable)tableToUse).getFieldSpecForColumn(column);
		return super.getTableCellEditorComponent(tableToUse, stringValue, isSelected,
				row, column);
	}
	
	public void spaceWasPressed()
	{
		int hasFocus = 0;
		JComponent[] focusableComponents = ((UiFlexiDateEditor)getUiField()).getFocusableComponents();
		for(int i = 0; i < focusableComponents.length; ++i)
		{
			if(focusableComponents[i].isFocusOwner())
				hasFocus = i;
		}
		UiComboBox date = (UiComboBox)(focusableComponents[hasFocus]);
		if(!date.isPopupVisible())
			date.requestFocus();
	}
	
	private MiniLocalization getLocalization()
	{
		return localization;
	}

	String originalDate;
	FieldSpec fieldSpecBeingEdited;
	UiDialogLauncher dlgLauncher;
	private GridFieldSpec gridSpec;
	private MiniLocalization localization;
}
