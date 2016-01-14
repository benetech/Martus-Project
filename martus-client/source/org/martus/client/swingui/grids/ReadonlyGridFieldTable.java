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
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;

public class ReadonlyGridFieldTable extends GridFieldTable
{
	public ReadonlyGridFieldTable(GridTableModel model,
			UiDialogLauncher dlgLauncherToUse, UiFieldContext context)
	{
		super(model, dlgLauncherToUse, context);
	}

	protected void createRenderers()
	{
		initializeRenderers(createReadOnlyEditorsOrRenderers());
	}
	
	protected void createEditors()
	{
		initializeEditors(createReadOnlyEditorsOrRenderers());
	}
	
	protected GridCellEditorAndRenderer[] createReadOnlyEditorsOrRenderers()
	{
		GridCellEditorAndRenderer[] editors = new GridCellEditorAndRenderer[getColumnCount()];
		setGenericDateEditor(createViewer(new FieldTypeDate()));
		setGenericDateRangeEditor(createViewer(new FieldTypeDateRange()));
		
		GridTableModel model = getGridTableModel();
		for(int tableColumn = 0; tableColumn < getColumnCount(); ++tableColumn)
		{
			int modelColumn = convertColumnIndexToModel(tableColumn);
			FieldType type = model.getCellType(0, modelColumn);
			editors[tableColumn] = createViewer(type);
		}
		
		return editors;
	}

	private GridCellEditorAndRenderer createViewer(FieldType type)
	{
		UiLocalization localization = dlgLauncher.getLocalization();
		if(type.isBoolean())
			return new GridBooleanCellViewer(localization);
		if(type.isDate())
			return new GridDateCellViewer(localization);
		if(type.isDateRange())
			return new GridDateRangeCellViewer(localization);
		if(type.isDropdown() || type.isLanguageDropdown())
			return new GridDropDownCellViewer(context, localization);
		if(type.isPopUpTree())
			throw new RuntimeException("ReadonlyGridFieldTable does not support popup tree field types");
		
		if(type.isMultiline() || type.isAnyField() || type.isGrid())
			return new GridNormalCellEditor(localization);
			
		return new GridNormalCellEditor(localization);
	}

}
