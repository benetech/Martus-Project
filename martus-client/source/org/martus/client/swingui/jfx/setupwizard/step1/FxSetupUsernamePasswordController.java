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

import java.util.Arrays;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import org.martus.client.core.MartusUserNameAndPassword;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.WizardNavigationButtonsInterface;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.client.swingui.jfx.setupwizard.StaticAccountCreationData;
import org.martus.common.MartusLogger;

public class FxSetupUsernamePasswordController extends FxStep1Controller
{
	public FxSetupUsernamePasswordController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initializeMainContentPane()
	{
		WizardNavigationButtonsInterface wizardNavigationHandler = getWizardNavigationHandler();
		wizardNavigationHandler.getBackButton().setVisible(false);
		wizardNavigationHandler.getNextButton().setDisable(true);
		userName.textProperty().addListener(new LoginChangeHandler());
		userName.focusedProperty().addListener(new UserNameFocusListener());      		
		passwordField.textProperty().addListener(new LoginChangeHandler());
		passwordField.focusedProperty().addListener(new PasswordFocusListener());      
		hintLabel.setTooltip(new Tooltip(getLocalization().getFieldLabel("PasswordTipGeneral")));
	}

	@Override
	public void initializeSidebarContentPane()
	{
		setUserNameTipVisible(true);
	}

	protected void setUserNameTipVisible(boolean bSetUserNameTipVisible)
	{
		fxVBoxUserNameTips.setVisible(bSetUserNameTipVisible);
		fxVBoxPasswordTips.setVisible(!bSetUserNameTipVisible);
	}

	protected void setPasswordTipVisible(boolean bSetPasswordTipVisible)
	{
		fxVBoxPasswordTips.setVisible(bSetPasswordTipVisible);
		fxVBoxUserNameTips.setVisible(!bSetPasswordTipVisible);
	}

	@Override
	public void nextWasPressed() throws Exception
	{
		StaticAccountCreationData.setUserName(getUserName().getText());
		StaticAccountCreationData.setPassword(getPasswordField().getText().toCharArray());
		super.nextWasPressed();
	}
	
	private Label getErrorLabel()
	{
		return errorLabel;
	}

	public void updateDisplay()
	{
		boolean canContinue = false;
		String errorMessage = "";
		
		boolean hasUserName;
		boolean isPasswordLongEnough;
		boolean doesAccountExist;
		boolean usernameSameAsPassword;
		try
		{
			String candidateUserName = getUserName().getText();
			hasUserName = candidateUserName.length() > 0;
			
			char[] candidatePassword = getPasswordField().getText().toCharArray();
			isPasswordLongEnough = (candidatePassword.length >= MartusUserNameAndPassword.BASIC_PASSWORD_LENGTH);

			doesAccountExist = getApp().doesAccountExist(candidateUserName, candidatePassword);
			usernameSameAsPassword = areSame(candidateUserName, candidatePassword);
			
			MartusLocalization localization = getLocalization();
			if (!hasUserName)
				errorMessage = localization.getFieldLabel("notifyUserNameBlankcause");
			else if(!isPasswordLongEnough)
				errorMessage = localization.getFieldLabel("notifyPasswordInvalidcause");
			else if(usernameSameAsPassword)
				errorMessage = localization.getFieldLabel("notifyPasswordMatchesUserNamecause");
			else if(doesAccountExist)
				errorMessage = localization.getFieldLabel("notifyUserAlreadyExistscause");

			canContinue = hasUserName && isPasswordLongEnough && !doesAccountExist && !usernameSameAsPassword;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			errorMessage = "Unexpected error";
		}
		
		getErrorLabel().setText(errorMessage);
		getWizardNavigationHandler().getNextButton().setDisable(!canContinue);
	}

	private boolean areSame(String candidateUserName, char[] candidatePassword)
	{
		char[] username = candidateUserName.toCharArray();
		return Arrays.equals(username, candidatePassword);
	}

	public class LoginChangeHandler implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
		{
			updateDisplay();
		}
	}
	
	
	public class PasswordFocusListener implements ChangeListener<Boolean>
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
			setPasswordTipVisible(newValue);
		}
	}
	
	public class UserNameFocusListener implements ChangeListener<Boolean>
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
			setUserNameTipVisible(newValue);
		}
	}

	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step1/SetupUsernamePassword.fxml";
	}
	
	@Override
	public String getSidebarFxmlLocation()
	{
		return "setupwizard/step1/SetupUsernamePasswordSidebar.fxml";
	}
	
	@Override
	public AbstractFxSetupWizardContentController getNextController()
	{
		return new FxVerifyAccountController(getMainWindow());
	}
	
	public PasswordField getPasswordField()
	{
		return passwordField;
	}

	public void setPasswordField(PasswordField passwordField)
	{
		this.passwordField = passwordField;
	}

	public TextField getUserName()
	{
		return userName;
	}

	public void setUserName(TextField userName)
	{
		this.userName = userName;
	}

	@FXML
	private TextField userName;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML
	private Label errorLabel;
	
	@FXML
	private Label hintLabel;
	
	@FXML
	private VBox fxVBoxUserNameTips;

	@FXML
	private VBox fxVBoxPasswordTips;
}
