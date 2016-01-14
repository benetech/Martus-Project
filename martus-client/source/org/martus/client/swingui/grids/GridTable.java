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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.event.ListSelectionListener;

import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.fields.UiFieldContext;
import org.martus.common.ListOfReusableChoicesLists;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.swing.UiTableWithCellEditingProtection;
import org.martus.util.language.LanguageOptions;

public abstract class GridTable extends UiTableWithCellEditingProtection
{
	
	public GridTable(GridTableModel model, UiDialogLauncher dlgLauncherToUse, UiFieldContext contextToUse)
	{
		super(model);
		dlgLauncher = dlgLauncherToUse;
		context = contextToUse;
		
		// NOTE: We need to keep renderers and editors separate, because otherwise
		// they get confused about focus when you click on a renderer but the 
		// editor is supposed to end up getting the click because they occupy 
		// the same screen location
		createRenderers();
		createEditors();

		useMaxWidth();
		setColumnWidthsFromHeadersAndData();
		setAutoResizeMode(AUTO_RESIZE_OFF);
	}

	abstract protected void createRenderers();
	abstract protected void createEditors();

	public void addRowSelectionListener(ListSelectionListener listener)
	{
		getSelectionModel().addListSelectionListener(listener);
	}
	
	public void setColumnWidthsFromHeadersAndData() 
	{
		GridTableModel model = getGridTableModel();
		setMaxColumnWidthToHeaderWidth(0);
		for(int i = 1 ; i < model.getColumnCount(); ++i)
		{
			FieldType columnType = model.getColumnType(i);
			if(columnType.isDropdown())
			{
				int dropDownColumnWidth = getDropDownColumnWidth(i, (DropDownFieldSpec)model.getFieldSpecForColumn(i));
				setColumnMaxWidth(i, dropDownColumnWidth);
			}
			else if(columnType.isDate())
				setColumnMaxWidth(i, getDateColumnWidth(i));
			else if(columnType.isDateRange())
				setColumnMaxWidth(i, getDateRangeColumnWidth(i));
			else if(columnType.isLanguageDropdown())
				setColumnWidthToHeaderWidth(i);
			else if(columnType.isBoolean())
				setColumnWidthToHeaderWidth(i);
			else
				setColumnWidthToMinimumRequred(i);
		}
	}
	
	public void updateDataDrivenColumnWidth(int column, ListOfReusableChoicesLists allChoices)
	{
		int LAST = allChoices.size() - 1;
		ChoiceItem[] choices = allChoices.get(LAST).getChoices();

		int dropDownColumnWidth = getDropDownColumnWidth(column, choices);
		setColumnMaxWidth(column, dropDownColumnWidth);
	}
	
	public UiDialogLauncher getDialogLauncher()
	{
		return dlgLauncher;
	}

	public int getDateColumnWidth(int column)
	{
		GridCellEditorAndRenderer gridDateCellEditor = getDateEditor();
		int width = gridDateCellEditor.getMinimumCellWidth();
		
		int columnHeaderWidth = getColumnHeaderWidth(column);
		if(width < columnHeaderWidth)
			width = columnHeaderWidth;
		return width;
	}
	
	abstract protected GridCellEditorAndRenderer getDateEditor();

	private int getDateRangeColumnWidth(int column)
	{
		GridCellEditorAndRenderer gridDateRangeCellEditor = getDateRangeEditor();
		int width = gridDateRangeCellEditor.getMinimumCellWidth();
		int columnHeaderWidth = getColumnHeaderWidth(column);
		if(width < columnHeaderWidth)
			width = columnHeaderWidth;
		return width;
	}

	abstract protected GridCellEditorAndRenderer getDateRangeEditor();

	private int getDropDownColumnWidth(int column, DropDownFieldSpec spec)
	{
		int width = 0;
		ListOfReusableChoicesLists lists = context.getCurrentDropdownChoices(spec);
		for(int level = 0; level < lists.size(); ++level)
		{
			ChoiceItem[] choices = lists.get(level).getChoices();
			width += getDropDownColumnWidth(column, choices);
		}
		
		return width;
	}

	private int getDropDownColumnWidth(int column, ChoiceItem[] choices) 
	{
		final int SCROLL_BAR_ALLOWANCE = 50;
		final int DROPDOWN_LANGUAGE_PADDING = 15;
		int widestWidth = getColumnHeaderWidth(column);
		for(int i = 0; i < choices.length; ++i)
		{
			String thisValue = choices[i].toString();
			int thisWidth = getRenderedWidth(column, thisValue) + SCROLL_BAR_ALLOWANCE;
			if(thisWidth > widestWidth)
				widestWidth = thisWidth;
		}
		if(LanguageOptions.needsLanguagePadding())
			widestWidth += DROPDOWN_LANGUAGE_PADDING;
		
		return widestWidth;
	}

	public JComponent[] getFocusableComponents()
	{
		Vector components = new Vector();
		Collection editors = getAllEditors();
		Iterator iter = editors.iterator();
		while(iter.hasNext())
		{
			GridCellEditorAndRenderer editor = (GridCellEditorAndRenderer)iter.next();
			List subComponents = Arrays.asList(editor.getUiField().getFocusableComponents());
			components.addAll(subComponents);
		}
		return (JComponent[])components.toArray(new JComponent[0]);
	}

	abstract public Collection getAllEditors();

	protected GridFieldSpec getGridFieldSpec()
	{
		return getGridTableModel().getGridFieldSpec();
	}

	protected GridTableModel getGridTableModel()
	{
		return (GridTableModel)getModel();
	}
	
	FieldSpec getFieldSpecForColumn(int column)
	{
		return (getGridTableModel()).getFieldSpecForColumn(column);		
	}
	
	FieldSpec getFieldSpecForCell(int row, int column)
	{
		return (getGridTableModel()).getFieldSpecForCell(row, column);
	}
	
	public void changeSelection(int rowIndex, int columnIndex,
			boolean toggle, boolean extend)
	{
		if(columnIndex == 0)
			columnIndex = 1;
		super.changeSelection(rowIndex, columnIndex, toggle, extend);
	}
	
	public UiFieldContext getFieldContext()
	{
		return context;
	}

	public void updateSpellChecker(String bulletinLanguageCode)
	{
	}

	UiDialogLauncher dlgLauncher;
	protected UiFieldContext context;
}

