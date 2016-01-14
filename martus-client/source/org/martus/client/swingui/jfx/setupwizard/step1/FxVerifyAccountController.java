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
package org.martus.client.swingui.jfx.setupwizard.step1;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.client.swingui.jfx.setupwizard.StaticAccountCreationData;
import org.martus.client.swingui.jfx.setupwizard.step2.FxSetupContactInfoController;
import org.martus.client.swingui.jfx.setupwizard.tasks.CreateAccountTask;
import org.martus.common.MartusLogger;

public class FxVerifyAccountController extends FxStep1Controller
{
	public FxVerifyAccountController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initializeMainContentPane()
	{
		getWizardNavigationHandler().getNextButton().setDisable(true);
		userNameField.textProperty().addListener(new LoginChangeHandler());
		passwordField.textProperty().addListener(new LoginChangeHandler());
		Platform.runLater(() -> userNameField.requestFocus());
		updateStatus();
	}

	@Override
	public void nextWasPressed() throws Exception
	{
		createAccount();
		super.nextWasPressed();
	}

	private void createAccount() throws Exception
	{
		String userNameValue = userNameField.getText();
		char[] passwordValue = passwordField.getText().toCharArray();
		
		StaticAccountCreationData.dispose();
		
		Task task = new CreateAccountTask(getApp(), userNameValue, passwordValue);
		MartusLocalization localization = getLocalization();
		String message = localization.getFieldLabel("CreatingAccount");
		showBusyDialog(message, task);
		getMainWindow().setCreatedNewAccount(true);
		
		String languageCodeUserStartedWith = getMainWindow().getLocalization().getCurrentLanguageCode();
		getMainWindow().initalizeUiState(languageCodeUserStartedWith);
		getApp().doAfterSigninInitalization();
	}
	
	protected void updateStatus()
	{
		try
		{
			getAccountConfirmLabel().setText("");
			String userNameValue = userNameField.getText();
			String passwordValue = passwordField.getText();
			boolean nameMatches = userNameValue.equals(StaticAccountCreationData.getUserName());
			boolean passwordMatches = passwordValue.equals(StaticAccountCreationData.getPassword());

			boolean canContinue = nameMatches && passwordMatches;
			getWizardNavigationHandler().getNextButton().setDisable(!canContinue);

			String statusMessage = "";
			MartusLocalization localization = getLocalization();
			String styleTag = "errorText";
			if (!nameMatches)
			{
				statusMessage = localization.getFieldLabel("notifyusernamessdontmatchcause");
			}
			else if (!passwordMatches)
			{
				statusMessage = localization.getFieldLabel("notifypasswordsdontmatchcause");
			}
			else
			{
				styleTag = "hintText";
				statusMessage = localization.getFieldLabel("UserNameAndPasswordMatches");
			}
			ObservableList<String> styleClassForInformationMessage = getAccountConfirmLabel().getStyleClass();
			styleClassForInformationMessage.clear();
			styleClassForInformationMessage.add(styleTag);
			getAccountConfirmLabel().setText(statusMessage);

		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	private Label getAccountConfirmLabel()
	{
		return accountConfirmLabel;
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step1/VerifyAccount.fxml";
	}
	
	@Override
	public AbstractFxSetupWizardContentController getNextController()
	{
		return new FxSetupContactInfoController(getMainWindow());
	}
	
	private class LoginChangeHandler implements ChangeListener<String>
	{
		public LoginChangeHandler()
		{
		}
		
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
		{
			updateStatus();
		}

	}

	@FXML
	private TextField userNameField;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML
	private Label accountConfirmLabel;
}
