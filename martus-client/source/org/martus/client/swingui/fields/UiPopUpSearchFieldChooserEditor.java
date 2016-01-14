/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2011, Beneficent
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

import java.awt.Container;
import java.awt.Point;
import java.util.Vector;

import javax.swing.JDialog;

import org.martus.client.search.FancySearchTableModel;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;

public class UiPopUpSearchFieldChooserEditor extends UiPopUpFieldChooserEditor
{
	public UiPopUpSearchFieldChooserEditor(UiMainWindow mainWindowToUse, FancySearchTableModel modelToUse)
	{
		super(mainWindowToUse);
		model = modelToUse;
	}

	@Override
	protected FieldTreeDialog createFieldChooserDialog(Container topLevel, Point locationOnScreen, PopUpTreeFieldSpec treeSpec, MartusLocalization localization)
	{
		dialog = new SearchFieldTreeDialog(getMainWindow(), (JDialog)topLevel, locationOnScreen, treeSpec);
		return dialog;
	}
	
	void notifyListeners()
	{
		if(dialog == null)
			return;
		
		Vector foundValues = dialog.getFoundValues();
		model.setAvailableFieldValues(dialog.getSelectedSpec(), foundValues);
		super.notifyListeners();
	}
	
	SearchFieldTreeDialog dialog;
	private FancySearchTableModel model;
}
