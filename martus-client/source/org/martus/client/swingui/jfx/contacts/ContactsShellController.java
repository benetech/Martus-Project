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
package org.martus.client.swingui.jfx.contacts;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.WizardNavigationButtonsInterface;
import org.martus.client.swingui.jfx.WizardNavigationHandlerInterface;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.FxWizardShellController;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;

public class ContactsShellController extends FxWizardShellController implements WizardNavigationButtonsInterface
{
	public ContactsShellController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	protected String getCssName()
	{
		return "Contacts.css";
	}

	@Override
	public String getFxmlLocation()
	{
		return "contacts/ContactsShell.fxml";
	}

	public void loadAndIntegrateContentPane(FxController contentPaneController) throws Exception
	{
		AbstractFxSetupWizardContentController controller = (AbstractFxSetupWizardContentController) contentPaneController;
		setContentNavigationHandler(controller);

		Parent createContents = contentPaneController.createContents();
		contentPane.getChildren().addAll(createContents);
		
	}
	
	public void setContentNavigationHandler(WizardNavigationHandlerInterface contentNavigationHandlerToUse)
	{
		contentNavigationHandler = contentNavigationHandlerToUse;
		contentNavigationHandler.setNavigationHandler(this);
	}
	
	@Override
	public Button getNextButton()
	{
		return closeButton;
	}
	
	@Override
	public Button getBackButton()
	{
		return null;
	}
	
	@Override
	public void doNext()
	{
		throw new RuntimeException("Not implemented");
	}

	@FXML
	protected void onClose(ActionEvent event) throws Exception
	{
		getContentNavigationHandler().nextWasPressed();
		getStage().close();
	}
	
	@FXML
	protected void importContactFromFile(ActionEvent event)
	{
		FxManageContactsController controller = (FxManageContactsController) getContentNavigationHandler();
		controller.importContactFromFile();
	}
	
	private WizardNavigationHandlerInterface getContentNavigationHandler()
	{
		return contentNavigationHandler;
	}

	@FXML
	protected Pane contentPane;

	@FXML
	protected Button closeButton;

	private WizardNavigationHandlerInterface contentNavigationHandler;

}
