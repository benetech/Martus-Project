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

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import org.martus.client.swingui.UiMainWindow;

public class DialogWithOkCancelShellController extends DialogShellController
{
	public DialogWithOkCancelShellController(UiMainWindow mainWindowToUse, FxController contentController)
	{
		super(mainWindowToUse, contentController);
	}

	@Override
	public String getFxmlLocation()
	{
		return LOCATION_DIALOG_WITH_OK_CANCEL_SHELL;
	}
	
	@FXML
	public void onOkClicked()
	{
		getContentController().save();
		close();
	}
	
	@Override
	protected String getCssName()
	{
		return "MainDialog.css";
	}
	
	@FXML
	public void onCancelClicked()
	{
		close();
	}
	
	public void setOkButtonText(String newText)
	{
		ok.setText(newText);
	}
	
	public void setOkButtonDisabled(boolean isDisabled)
	{
		ok.setDisable(isDisabled);
	}
	
	public void setCancelButtonText(String buttonTag)
	{
		cancel.setText(getLocalization().getButtonLabel(buttonTag));
	}
	
	private static final String LOCATION_DIALOG_WITH_OK_CANCEL_SHELL = "generic/DialogWithOkCancelShell.fxml";
	
	@FXML
	protected Button ok;
	
	@FXML
	protected Button cancel;
}
