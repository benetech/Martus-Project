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
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import org.martus.client.swingui.UiMainWindow;

public class PopupConfirmationController extends FxPopupController implements Initializable
{
	public PopupConfirmationController(UiMainWindow mainWindowToUse, String title, String yesLabel, String noLabel, FxController controllerForMainPane)
	{
		super(mainWindowToUse);
		this.title = title;
		this.controllerForMainPane = controllerForMainPane;
		yesButtonLabel =  yesLabel;
		noButtonLabel = noLabel;
	}
	
	@Override
	public void initialize()
	{
		fxYesButton.setText(yesButtonLabel);
		fxNoButton.setText(noButtonLabel);
		try
		{
			loadControllerAndEmbedInPane(controllerForMainPane, mainPane);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "generic/PopupConfirmation.fxml";
	}

	@Override
	public String getDialogTitle()
	{
		return title; 
	}

	@Override
	public Button getOkButton()
	{
		return fxYesButton;
	}

	@FXML
	public void yesPressed()
	{
		yesWasPressed = true;
		getStage().close();
	}

	@FXML
	public void noPressed()
	{
		getStage().close();
	}

	public boolean wasYesPressed()
	{
		return yesWasPressed;
	}

	@FXML
	private Pane mainPane;

	@FXML
	private Button fxYesButton;
	
	@FXML
	private Button fxNoButton;
	
	private String title;
	private boolean yesWasPressed;
	private FxController controllerForMainPane;
	private String yesButtonLabel;
	private String noButtonLabel;
}