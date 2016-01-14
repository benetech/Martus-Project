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

import java.util.Arrays;
import java.util.Collection;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.fields.UiFieldContext;

abstract public class GridFieldTable extends GridTable
{
	public GridFieldTable(GridTableModel model,
			UiDialogLauncher dlgLauncherToUse, UiFieldContext context)
	{
		super(model, dlgLauncherToUse, context);
	}

	public TableCellEditor getCellEditor(int row, int column)
	{
		return editors[column];
	}
	
	public TableCellRenderer getCellRenderer(int row, int column)
	{
		return renderers[column];
	}
	
	public Collection getAllEditors()
	{
		return Arrays.asList(editors);
	}

	protected GridCellEditorAndRenderer getDateEditor()
	{
		return genericDateEditor;
	}

	protected GridCellEditorAndRenderer getDateRangeEditor()
	{
		return genericDateRangeEditor;
	}
	
	protected void setGenericDateEditor(GridCellEditorAndRenderer genericDateEditor)
	{
		this.genericDateEditor = genericDateEditor;
	}
	
	protected void setGenericDateRangeEditor(
			GridCellEditorAndRenderer genericDateRangeEditor)
	{
		this.genericDateRangeEditor = genericDateRangeEditor;
	}
	
	public void initializeRenderers(GridCellEditorAndRenderer[] newRenderers)
	{
		renderers = newRenderers;
	}
	
	public void initializeEditors(GridCellEditorAndRenderer[] newEditors)
	{
		editors = newEditors;
	}

	@Override
	public void updateSpellChecker(String bulletinLanguageCode)
	{
		super.updateSpellChecker(bulletinLanguageCode);
		updateSpellChecker(renderers, bulletinLanguageCode);
		updateSpellChecker(editors, bulletinLanguageCode);
	}

	private void updateSpellChecker(GridCellEditorAndRenderer[] editorsOrRenderers, String bulletinLanguageCode)
	{
		for (GridCellEditorAndRenderer editorOrRenderer : editorsOrRenderers)
		{
			editorOrRenderer.updateSpellChecker(bulletinLanguageCode);
		}
	}

	private GridCellEditorAndRenderer genericDateEditor;
	private GridCellEditorAndRenderer genericDateRangeEditor;
	
	private GridCellEditorAndRenderer[] renderers;
	private GridCellEditorAndRenderer[] editors;
}
