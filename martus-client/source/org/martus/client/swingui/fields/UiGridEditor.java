/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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
package org.martus.client.swingui.fields;

import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.grids.EditableGridFieldTable;
import org.martus.client.swingui.grids.GridTable;
import org.martus.common.GridData;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.xml.sax.SAXException;

public class UiGridEditor extends UiEditableGrid 
{
	public UiGridEditor(UiMainWindow mainWindow, GridFieldSpec fieldSpec, UiDialogLauncher dlgLauncher, UiFieldContext context, int maxGridCharacters)
	{
		super(mainWindow, fieldSpec, dlgLauncher, context, maxGridCharacters);
	}

	protected GridTable createGridTable(UiDialogLauncher dlgLauncher, UiFieldContext context)
	{
		return new EditableGridFieldTable(model, dlgLauncher, context);
	}
	
	protected Vector createButtons()
	{
		Vector buttons = super.createButtons();
		buttons.insertElementAt(createShowExpandedButton(), 0);
		return buttons;
	}

	public void validate(FieldSpec spec, String fullLabel) throws DataInvalidException
	{
		super.validate(spec, fullLabel);
		
		try
		{
			GridFieldSpec gridSpec = (GridFieldSpec)spec;
			GridData gridData = getValidatableCopyOfData(gridSpec, getGridData());
			for(int row = 0; row < gridData.getRowCount(); ++row)
				for(int col = 0; col < gridSpec.getColumnCount(); ++col)
					validateCell(gridSpec, gridData, row, col);
		}
		catch (DataInvalidException e)
		{
			throw(e);
		}
		catch (Exception e)
		{
			throw new DataInvalidException(e);
		}
	}
	
	private GridData getValidatableCopyOfData(GridFieldSpec gridSpec, GridData realGridData)
			throws IOException, ParserConfigurationException, SAXException
	{
		GridData copyOfGridData = new GridData(gridSpec, getContext().getReusableChoicesLists());
		copyOfGridData.setFromXml(realGridData.getXmlRepresentation());
		copyOfGridData.removeTrailingBlankRows();
		return copyOfGridData;
	}	

	private void validateCell(GridFieldSpec gridSpec, GridData gridData,
			int row, int col) throws DataInvalidException
	{
		FieldSpec columnSpec = gridSpec.getFieldSpec(col);
		String value = gridData.getValueAt(row, col);
		String fullColumnLabel = gridSpec.getLabel() + ": " + columnSpec.getLabel();
		fullColumnLabel = fontHelper.getDisplayable(fullColumnLabel);
		try
		{
			columnSpec.validate(fullColumnLabel, value, getLocalization());
		}
		catch(DataInvalidException e)
		{
			table.getSelectionModel().setSelectionInterval(row, row);
			table.getColumnModel().getSelectionModel().setSelectionInterval(col, col);
			table.requestFocusInWindow();
			throw e;
		}
	}
	
	@Override
	public void updateSpellChecker(String bulletinLanguageCode)
	{
		super.updateSpellChecker(bulletinLanguageCode);
		
		if(expandedFieldRows != null)
			updateSpellCheckerForExpandedFields(bulletinLanguageCode);
		else
			getTable().updateSpellChecker(bulletinLanguageCode);
	}

	private void updateSpellCheckerForExpandedFields(String bulletinLanguageCode)
	{
		for(int row = 0; row < expandedFieldRows.size(); ++row)
		{
			UiField[] fields = (UiField[])expandedFieldRows.get(row);
			for(int fieldIndex = 0; fieldIndex < fields.length; ++fieldIndex)
			{
				UiField field = fields[fieldIndex];
				if(field != null)
					field.updateSpellChecker(bulletinLanguageCode);
			}
		}
	}
}
