/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
Technology, Inc. (Benetech).

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
/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
Technology, Inc. (Benetech).

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

package org.martus.client.swingui.bulletincomponent;

import java.awt.Container;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.martus.client.swingui.fields.UiChoice;
import org.martus.client.swingui.fields.UiField;
import org.martus.client.swingui.fields.UiFieldContext;
import org.martus.client.swingui.fields.UiGrid;
import org.martus.client.swingui.grids.GridTableModel;
import org.martus.common.ListOfReusableChoicesLists;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.GridFieldSpec;

public class GridChangeHandler implements TableModelListener
{
	public GridChangeHandler(UiGrid gridToMonitor, UiFieldContext contextToUse) 
	{
		modifiedGrid = gridToMonitor;
		context = contextToUse;
	}

	public void tableChanged(TableModelEvent event) 
	{
		updateDataDrivenDropdowns();
	}

	private void updateDataDrivenDropdowns() 
	{
		for(int i = 0; i < context.getSectionFieldCount(); ++i)
		{
			FieldSpec spec = context.getFieldSpec(i);
			FieldType type = spec.getType();
			UiField field = context.getField(spec.getTag());
			
			if(type.isGrid())
				updateDataDrivenDropdownsInsideGrid((GridFieldSpec)spec, (UiGrid)field);

			if(type.isDropdown())
				updateDataDrivenDropdown((DropDownFieldSpec)spec, field);
		}
	}

	private void updateDataDrivenDropdown(DropDownFieldSpec spec, UiField field) 
	{
		if(!isDataSourceThisGrid(spec))
			return;
		
		UiGrid dataSourceGrid = context.getGridField(spec.getDataSourceGridTag());
		if(dataSourceGrid == null)
			return;
		
		UiChoice choiceField = (UiChoice)field;
		choiceField.setSpec(context, spec);

		// NOTE: Attempted fix for TT 4123, combos blank out 
		// on Windows--may be Java6 optimization issue
		Container parent = choiceField.getComponent().getTopLevelAncestor();
		if(parent != null)
		{
			parent.invalidate();
			parent.validate();
			parent.repaint();
		}

	}

	private void updateDataDrivenDropdownsInsideGrid(GridFieldSpec gridSpecToBlankOut, UiGrid gridToBlankOut) 
	{
		boolean needsUpdate = false;
		
		GridTableModel modelToBlankOut = gridToBlankOut.getGridTableModel();
		for(int column = 0; column < modelToBlankOut.getColumnCount(); ++column)
		{
			FieldSpec columnSpec = modelToBlankOut.getFieldSpecForColumn(column);
			if(!columnSpec.getType().isDropdown())
				continue;
			
			DropDownFieldSpec dropdownSpec = (DropDownFieldSpec)columnSpec;
			if(!isDataSourceThisGrid(dropdownSpec))
				continue;
			needsUpdate = true;
			
			ListOfReusableChoicesLists choices = context.getCurrentDropdownChoices(dropdownSpec);
			if(choices == null)
				continue;
			
			gridToBlankOut.updateDataDrivenColumnWidth(column, choices);
			
			for(int row = 0; row < modelToBlankOut.getRowCount(); ++row)
			{
				String oldValue = (String)modelToBlankOut.getValueAt(row, column);
				String newValue = ensureValid(choices, oldValue);
				if(!newValue.equals(oldValue))
					modelToBlankOut.setValueAt(newValue, row, column);
			}
		}
		
		if(needsUpdate)
			gridToBlankOut.dataDrivenDropdownInsideGridMayNeedToBeUpdated();
	}

	private boolean isDataSourceThisGrid(DropDownFieldSpec spec) 
	{
		if(spec.getDataSourceGridTag() == null)
			return false;
		
		String dataSourceGridTag = spec.getDataSourceGridTag();
		String modifiedGridTag = modifiedGrid.getGridData().getSpec().getTag();
		return (dataSourceGridTag.equals(modifiedGridTag));
	}
	
	private String ensureValid(ListOfReusableChoicesLists allChoices, String text) 
	{
		int LAST = allChoices.size() - 1;
		ChoiceItem[] choices = allChoices.get(LAST).getChoices();

		for(int i = 0; i < choices.length; ++i)
			if(choices[i].getCode().equals(text))
				return text;

		return "";
	}

	private UiFieldContext context;
	UiGrid modifiedGrid;
}
