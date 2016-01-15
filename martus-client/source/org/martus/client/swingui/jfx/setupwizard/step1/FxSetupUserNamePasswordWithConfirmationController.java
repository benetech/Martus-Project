/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusUserNameAndPassword;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.WizardNavigationButtonsInterface;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.client.swingui.jfx.setupwizard.StaticAccountCreationData;
import org.martus.client.swingui.jfx.setupwizard.step2.FxSetupSettingsController;
import org.martus.client.swingui.jfx.setupwizard.tasks.CreateAccountTask;
import org.martus.common.MartusLogger;

public class FxSetupUserNamePasswordWithConfirmationController extends FxStep1Controller
{
	public FxSetupUserNamePasswordWithConfirmationController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		
		info = getApp().getConfigInfo();
		fontHelper = new UiFontEncodingHelper(getConfigInfo().getDoZawgyiConversion());
	}

	@Override
	public AbstractFxSetupWizardContentController getNextController()
	{
		return new FxSetupSettingsController(getMainWindow());
	}
	
	@Override
	public void nextWasPressed() throws Exception
	{
		createAccount();
		getConfigInfo().setAuthor(getFontHelper().getStorable(authorField.getText()));
		getConfigInfo().setOrganization(getFontHelper().getStorable(organizationField.getText()));
		getApp().saveConfigInfo();
		
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
	
	@Override
	public void initializeSidebarContentPane()
	{
		setUserNameTipVisible(true);
	}
	
	@Override
	public void initializeMainContentPane()
	{
		setupContactInforFields();
		setupLoginFields();
		setupConfirmationFields();
	}

	private void setupContactInforFields()
	{
		authorField.setText(getConfigInfo().getAuthor());
		organizationField.setText(getConfigInfo().getOrganization());
		Platform.runLater(() -> userNameField.requestFocus());
	}

	private void setupLoginFields()
	{
		WizardNavigationButtonsInterface wizardNavigationHandler = getWizardNavigationHandler();
		wizardNavigationHandler.getBackButton().setVisible(false);
		wizardNavigationHandler.getNextButton().setDisable(true);
		
		userNameField.textProperty().addListener(new LoginChangeHandler());
		userNameField.focusedProperty().addListener(new UserNameFocusListener());      		
		passwordField.textProperty().addListener(new LoginChangeHandler());
		passwordField.focusedProperty().addListener(new PasswordFocusListener());      
		confirmUserNameField.focusedProperty().addListener(new ConfirmUserFocusListener());      		
		confirmPasswordField.focusedProperty().addListener(new ConfirmPasswordFocusListener());      		

		authorField.focusedProperty().addListener(new AuthorFocusListener());      
		organizationField.focusedProperty().addListener(new OrganizationFocusListener());      
		
		errorLabel.setTooltip(new Tooltip(getLocalization().getFieldLabel("PasswordTipGeneral")));
	}
	
	private void setupConfirmationFields()
	{
		getWizardNavigationHandler().getNextButton().setDisable(true);
		confirmUserNameField.setDisable(true);
		confirmPasswordField.setDisable(true);
		
		confirmUserNameField.textProperty().addListener(new ConfirmLoginChangeHandler());
		confirmPasswordField.textProperty().addListener(new ConfirmLoginChangeHandler());
	}
	
	protected void confirmationLoginDataChanged()
	{
		try
		{
			getErrorLabel().setText("");
			String userNameValue = confirmUserNameField.getText();
			String passwordValue = confirmPasswordField.getText();
			boolean nameMatches = userNameValue.equals(getUserName().getText());
			boolean passwordMatches = passwordValue.equals(getPasswordField().getText());
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
			
			ObservableList<String> styleClassForInformationMessage = getErrorLabel().getStyleClass();
			styleClassForInformationMessage.clear();
			styleClassForInformationMessage.add(styleTag);
			getErrorLabel().setText(statusMessage);

		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	public void loginDataChanged()
	{
		clearConfirmationFields();
		
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
		enableConfirmationFields(canContinue);
	}

	private void enableConfirmationFields(boolean canContinue)
	{
		getConfirmUserNameField().setDisable(!canContinue);
		getConfirmPasswordField().setDisable(!canContinue);
	}

	private void clearConfirmationFields()
	{
		getConfirmUserNameField().setText("");
		getConfirmPasswordField().setText("");
		getErrorLabel().setText("");
	}
	
	private Label getErrorLabel()
	{
		return errorLabel;
	}

	private boolean areSame(String candidateUserName, char[] candidatePassword)
	{
		char[] username = candidateUserName.toCharArray();
		return Arrays.equals(username, candidatePassword);
	}

	
	protected void setUserNameTipVisible(boolean bSetUserNameTipVisible)
	{
		fxVBoxUserNameTips.setVisible(bSetUserNameTipVisible);
		fxVBoxPasswordTips.setVisible(!bSetUserNameTipVisible);
		fxVBoxAuthorOrganizationTips.setVisible(!bSetUserNameTipVisible);
	}

	protected void setPasswordTipVisible(boolean bSetPasswordTipVisible)
	{
		fxVBoxPasswordTips.setVisible(bSetPasswordTipVisible);
		fxVBoxUserNameTips.setVisible(!bSetPasswordTipVisible);
		fxVBoxAuthorOrganizationTips.setVisible(!bSetPasswordTipVisible);
	}
	
	protected void setAuthorOrganizationTipVisible(boolean bSetAuthorOrganizationTipVisible)
	{
		fxVBoxUserNameTips.setVisible(!bSetAuthorOrganizationTipVisible);
		fxVBoxPasswordTips.setVisible(!bSetAuthorOrganizationTipVisible);
		fxVBoxAuthorOrganizationTips.setVisible(bSetAuthorOrganizationTipVisible);
	}

	protected void hideTipFields()
	{
		fxVBoxPasswordTips.setVisible(false);
		fxVBoxUserNameTips.setVisible(false);
		fxVBoxAuthorOrganizationTips.setVisible(false);
	}

	private PasswordField getPasswordField()
	{
		return passwordField;
	}

	private TextField getUserName()
	{
		return userNameField;
	}
	
	private TextField getConfirmUserNameField()
	{
		return confirmUserNameField;
	}
	
	private PasswordField getConfirmPasswordField()
	{
		return confirmPasswordField;
	}
	
	private UiFontEncodingHelper getFontHelper()
	{
		return fontHelper;
	}

	private ConfigInfo getConfigInfo()
	{
		return info;
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step1/SetupUsernamePasswordWithVerificationFields.fxml";
	}
	
	@Override
	public String getSidebarFxmlLocation()
	{
		return "setupwizard/step1/SetupUsernamePasswordSidebar.fxml";
	}
	
	protected class ConfirmLoginChangeHandler implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
		{
			confirmationLoginDataChanged();
		}
	}
	
	protected class LoginChangeHandler implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
		{
			loginDataChanged();
		}
	}
	
	protected class UserNameFocusListener implements ChangeListener<Boolean>
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
			setUserNameTipVisible(newValue);
		}
	}
		
	protected class PasswordFocusListener implements ChangeListener<Boolean>
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
			setPasswordTipVisible(newValue);
		}
	}
	
	protected class ConfirmUserFocusListener implements ChangeListener<Boolean>
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
			hideTipFields();
		}
	}
	
	protected class ConfirmPasswordFocusListener implements ChangeListener<Boolean>
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
			hideTipFields();
		}
	}
	
	protected class AuthorFocusListener implements ChangeListener<Boolean>
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
			setAuthorOrganizationTipVisible(newValue);
		}
	}

	protected class OrganizationFocusListener implements ChangeListener<Boolean>
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
			setAuthorOrganizationTipVisible(newValue);
		}
	}

	@FXML
	private TextField userNameField;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML
	private TextField confirmUserNameField;
	
	@FXML
	private PasswordField confirmPasswordField;
	
	@FXML
	private Label errorLabel;
	
	@FXML
	private VBox fxVBoxUserNameTips;

	@FXML
	private VBox fxVBoxPasswordTips;
	
	@FXML
	private VBox fxVBoxAuthorOrganizationTips;

	@FXML
	private TextField authorField;
	
	@FXML
	private TextField organizationField;
	
	private ConfigInfo info;
	private UiFontEncodingHelper fontHelper;
}
