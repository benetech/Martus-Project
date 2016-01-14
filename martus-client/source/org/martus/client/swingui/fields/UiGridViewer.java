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

*/
package org.martus.client.swingui.fields;

import java.util.Vector;

import javax.swing.JComponent;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.grids.GridTable;
import org.martus.client.swingui.grids.ReadonlyGridFieldTable;
import org.martus.common.fieldspec.GridFieldSpec;


public class UiGridViewer extends UiGrid
{
	public UiGridViewer(UiMainWindow mainWindowToUse, GridFieldSpec fieldSpec, UiDialogLauncher dlgLauncher, UiFieldContext context, int maxGridCharacters)
	{
		super(mainWindowToUse, fieldSpec, dlgLauncher, context, new UiReadOnlyFieldCreator(mainWindowToUse, context));
		table.setMaxGridWidth(maxGridCharacters);
		table.resizeTable();
		table.setEnabled(false);
	}

	protected GridTable createGridTable(UiDialogLauncher dlgLauncher, UiFieldContext context)
	{
		return new ReadonlyGridFieldTable(model, dlgLauncher, context);
	}
	
	protected Vector createButtons()
	{
		Vector buttons = super.createButtons();
		buttons.insertElementAt(createShowExpandedButton(), 0);
		return buttons;
	}

	public void setText(String newText)
	{
		if(newText.length() == 0)
			return;
		super.setText(newText);
		table.resizeTable();
	}
	
	public JComponent[] getFocusableComponents()
	{
		return new JComponent[0];
	}

	void copyExpandedFieldsToTableModel() 
	{
		// NOTE: Readonly, so never copy fields back into the model
		return;
	}

}
