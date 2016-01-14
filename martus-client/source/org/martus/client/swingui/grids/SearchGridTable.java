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

import java.util.Collection;
import java.util.HashMap;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.martus.client.search.FancySearchTableModel;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.fields.UiFieldContext;
import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.DateFieldSpec;
import org.martus.common.fieldspec.DateRangeFieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeAnyField;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeDropdown;
import org.martus.common.fieldspec.FieldTypeGrid;
import org.martus.common.fieldspec.FieldTypeLanguage;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FieldTypePopUpTree;

public class SearchGridTable extends GridTable
{
	public SearchGridTable(UiMainWindow mainWindowToUse, FancySearchTableModel model, UiDialogLauncher dlgLauncherToUse, UiFieldContext context)
	{
		super(model, dlgLauncherToUse, context);
	}
	
	public FancySearchTableModel getFancySearchTableModel()
	{
		return (FancySearchTableModel) getGridTableModel();
	}

	protected void createRenderers()
	{
		renderers = new HashMap();
		createEditableEditorsOrRenderers(renderers);
	}
	
	protected void createEditors()
	{
		editors = new HashMap();
		createEditableEditorsOrRenderers(editors);
	}
	
	private void createEditableEditorsOrRenderers(HashMap map)
	{
		UiLocalization localization = dlgLauncher.getLocalization();
		map.put(new FieldTypeBoolean(), new GridBooleanCellEditor(localization));
		map.put(new FieldTypeDate(), new GridDateCellEditor((DateFieldSpec)new FieldTypeDate().createEmptyFieldSpec(), localization));
		map.put(new FieldTypeDateRange(), new GridDateRangeCellEditor(dlgLauncher, getGridFieldSpec(), (DateRangeFieldSpec) new FieldTypeDateRange().createEmptyFieldSpec(), localization));
		map.put(new FieldTypeDropdown(), new GridDropDownCellEditor(context, localization));
		map.put(new FieldTypeLanguage(), new GridDropDownCellEditor(context, localization));
		map.put(new FieldTypeNormal(), new GridNormalCellEditor(localization));
		map.put(new FieldTypeMultiline(), new GridNormalCellEditor(localization));
		map.put(new FieldTypeAnyField(), new GridNormalCellEditor(localization));
		map.put(new FieldTypeGrid(), new GridNormalCellEditor(localization));
		map.put(new FieldTypePopUpTree(), new SearchGridPopUpTreeCellEditor(dlgLauncher.getMainWindow(), getFancySearchTableModel()));
	}
	
	public Collection getAllEditors()
	{
		return editors.values();
	}

	public TableCellEditor getCellEditor(int row, int column)
	{
		return (TableCellEditor)getCellEditorOrRenderer(editors, row, column);
	}
	
	public TableCellRenderer getCellRenderer(int row, int column)
	{
		return (TableCellRenderer)getCellEditorOrRenderer(renderers, row, column);
	}
	
	private Object getCellEditorOrRenderer(HashMap map, int row, int column)
	{
		GridTableModel model = getGridTableModel();
		return getEditorOrRendererForType(map, model.getCellType(row, column));
	}

	private TableCellEditor getEditorOrRendererForType(HashMap map, FieldType type)
	{
		TableCellEditor editor = (TableCellEditor)map.get(type);
		if(editor != null)
			return editor;

		System.out.println("GridTable.getCellEditorOrRenderer Unexpected type: " + type);
		return (TableCellEditor)map.get(new FieldTypeNormal());
	}

	protected GridCellEditorAndRenderer getDateEditor()
	{
		return (GridCellEditorAndRenderer) editors.get(new FieldTypeDate());
	}

	protected GridCellEditorAndRenderer getDateRangeEditor()
	{
		return (GridCellEditorAndRenderer) editors.get(new FieldTypeDateRange());
	}

	private HashMap renderers;
	private HashMap editors;
}
