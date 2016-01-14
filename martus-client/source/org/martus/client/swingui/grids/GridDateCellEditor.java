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

import javax.swing.JComponent;

import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.DateFieldSpec;
import org.martus.swing.UiComboBox;

public class GridDateCellEditor extends GridCellEditorAndRenderer
{
	GridDateCellEditor(DateFieldSpec cellFieldSpec, MiniLocalization localization)
	{
		super(new UiDateEditor(cellFieldSpec, localization));
	}
	
	public void spaceWasPressed()
	{
		int hasFocus = 0;
		JComponent[] focusableComponents = ((UiDateEditor)getUiField()).getFocusableComponents();
		for(int i = 0; i < focusableComponents.length; ++i)
		{
			if(focusableComponents[i].isFocusOwner())
				hasFocus = i;
		}
		UiComboBox date = (UiComboBox)(focusableComponents[hasFocus]);
		if(!date.isPopupVisible())
			date.requestFocus();
	}
}