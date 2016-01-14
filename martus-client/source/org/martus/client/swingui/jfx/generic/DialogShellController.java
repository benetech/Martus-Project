/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.swingui.jfx.generic;

import java.awt.Dimension;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;

abstract public class DialogShellController extends FxShellWithSingleContentController implements ActionDoer 
{
	public DialogShellController(UiMainWindow mainWindowToUse, FxController contentController)
	{
		this(mainWindowToUse, contentController, "");
	}
	
	public DialogShellController(UiMainWindow mainWindowToUse, FxController contentController, String titleTagToUse)
	{
		super(mainWindowToUse, contentController);
		
		titleTag = titleTagToUse;
	}

	@Override
	protected Pane getContentPane()
	{
		return contentPane;
	}
	
	@Override
	public void doAction()
	{
		UiMainWindow mainWindow = getMainWindow();
		try
		{
			Dimension preferedDimension = this.getContentController().getPreferredDimension();
			mainWindow.createAndShowModalDialog(this, preferedDimension, this.getTitleTag());
		} 
		catch (Exception e)
		{
			mainWindow.unexpectedErrorDlg(e);
		}
	}

	@Override
	public String getTitleTag()
	{
		return titleTag;
	}
	
	@FXML
	Pane contentPane;
	
	private String titleTag;
}
