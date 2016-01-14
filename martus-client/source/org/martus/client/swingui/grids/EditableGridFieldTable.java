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
package org.martus.client.swingui.grids;

import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.fields.UiFieldContext;
import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.DateFieldSpec;
import org.martus.common.fieldspec.DateRangeFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;

public class EditableGridFieldTable extends GridFieldTable
{
	public EditableGridFieldTable(GridTableModel model,
			UiDialogLauncher dlgLauncherToUse, UiFieldContext context)
	{
		super(model, dlgLauncherToUse, context);
	}

	protected void createRenderers()
	{
		initializeRenderers(createEditableEditorsOrRenderers());
	}
	
	protected void createEditors()
	{
		initializeEditors(createEditableEditorsOrRenderers());
	}
	
	protected GridCellEditorAndRenderer[] createEditableEditorsOrRenderers()
	{
		GridCellEditorAndRenderer[] editors = new GridCellEditorAndRenderer[getColumnCount()];
		GridTableModel model = getGridTableModel();
		
		setGenericDateEditor(createEditor(new FieldTypeDate().createEmptyFieldSpec()));
		setGenericDateRangeEditor(createEditor(new FieldTypeDateRange().createEmptyFieldSpec()));
		
		for(int tableColumn = 0; tableColumn < getColumnCount(); ++tableColumn)
		{
			int modelColumn = convertColumnIndexToModel(tableColumn);
			FieldSpec cellFieldSpec = model.getFieldSpecForCell(0, modelColumn);
			editors[tableColumn] = createEditor(cellFieldSpec);
		}
		
		return editors;
	}
	
	private GridCellEditorAndRenderer createEditor(FieldSpec cellFieldSpec)
	{
		FieldType type = cellFieldSpec.getType();
		UiLocalization localization = dlgLauncher.getLocalization();
		if(type.isBoolean())
			return new GridBooleanCellEditor(localization);
		if(type.isDate())
			return new GridDateCellEditor((DateFieldSpec)cellFieldSpec, localization);
		if(type.isDateRange())
			return new GridDateRangeCellEditor(dlgLauncher, getGridFieldSpec(), (DateRangeFieldSpec)cellFieldSpec, localization);
		if(type.isDropdown() || type.isLanguageDropdown())
			return new GridDropDownCellEditor(context, localization);
		if(type.isPopUpTree())
			throw new RuntimeException("EditableGridFieldTable does not support popup tree field types");
		
		GridNormalCellEditor editor = new GridNormalCellEditor(localization);
		editor.setFieldContext(getFieldContext());
		
		if(type.isMultiline() || type.isAnyField() || type.isGrid())
		{
			return editor;
		}
			
		return editor;
	}

}
