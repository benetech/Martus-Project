/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2015, Beneficent
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
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.step6.FxSelectLanguageController;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.fieldspec.ChoiceItem;

abstract public class SigninController extends FxNonWizardShellController
{
	public SigninController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		
		secondsToDelay = 1;
	}
	
	public void attemptSigninOrReSignin(String userName, char[] userPassword) throws Exception
	{
		getApp().attemptSignIn(userName, userPassword);
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		ObservableList<ChoiceItem> availableLanguages = FxSelectLanguageController.getAvailableLanguages(getLocalization());
		languagesDropdown.setItems(availableLanguages);
		ChoiceItem currentLanguageChoiceItem = FxSelectLanguageController.findCurrentLanguageChoiceItem(getLocalization());
		languagesDropdown.getSelectionModel().select(currentLanguageChoiceItem);

		SingleSelectionModel<ChoiceItem> selectionModel = languagesDropdown.selectionModelProperty().getValue();
		ReadOnlyObjectProperty<ChoiceItem> selectedLanguageProperty = selectionModel.selectedItemProperty();
		selectedLanguageProperty.addListener((property, oldValue, newValue) -> languageChangedTo(newValue));
		
		BooleanBinding isUserNameEmpty = userNameField.textProperty().isEmpty();
		BooleanBinding isPasswordEmpty = passwordField.textProperty().isEmpty();
		okButton.disableProperty().bind(isUserNameEmpty.or(isPasswordEmpty));
		
		Platform.runLater(()->userNameField.requestFocus());
	}

	private void languageChangedTo(ChoiceItem newValue)
	{
		closeDialog(SigninResult.CHANGE_LANGUAGE);
	}
	
	private void closeDialog(SigninResult resultToUse)
	{
		result = resultToUse;
		getStage().close();
	}

	public SigninResult getResult()
	{
		return result;
	}
	
	public String getSelectedLanguageCode()
	{
		return languagesDropdown.getSelectionModel().getSelectedItem().getCode();
	}

	public String getUserName()
	{
		return userNameField.getText();
	}

	public char[] getUserPassword()
	{
		// NOTE: If PasswordField supported get char[], we would use it for security.
		// The caller will wipe the char[] we return, but the string could exist 
		// in memory even after Martus exits. 
		return passwordField.getText().toCharArray();
	}
	
	protected void hideLanguageDropdown()
	{
		languagesDropdown.setVisible(false);
	}
	
	@FXML
	private void onNewAccount()
	{
		closeDialog(SigninResult.CREATE_ACCOUNT);
	}

	@FXML
	private void onCancel()
	{
		closeDialog(SigninResult.CANCEL);
	}
	
	@FXML
	private void onOk()
	{
		try
		{
			String userName = getUserName();
			char[] userPassword = getUserPassword();
			attemptSigninOrReSignin(userName, userPassword);
			if(getMainWindow().isAlreadySignedIn())
				closeDialog(SigninResult.SIGNIN);
		}
		catch(AuthorizationFailedException failedSignin)
		{
			String message = getLocalization().getFieldLabel("notifyincorrectsignincause");
			punishTheUserBySleeping(message);
			secondsToDelay *= 2;
			return;
		}
		catch(Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	public void punishTheUserBySleeping(String message)
	{
		try
		{
			showBusyDialog(message, new Sleeper());
		} 
		catch (Exception notMuchWeCanDoAboutIt)
		{
			MartusLogger.logException(notMuchWeCanDoAboutIt);
		}
	}
	
	class Sleeper extends Task
	{
		@Override
		protected Object call() throws Exception
		{
			Thread.sleep(1000 * secondsToDelay);
			return null;
		}
		
	}

	public Dimension getPreferredDimension()
	{
		//TODO fix this is not getting called
		return SIGNIN_PREFERRED_DIALOG_SIZE;
	}

	@FXML
	private void onRestoreShare()
	{
		closeDialog(SigninResult.RESTORE_SHARE);
	}
	
	@FXML
	private void onRestoreFile()
	{
		closeDialog(SigninResult.RESTORE_FILE);
	}

	public void setSignInPaneVisible(boolean visible)
	{
		signInPane.setVisible(visible);
	}
	
	public static enum SigninResult { CANCEL, SIGNIN, CREATE_ACCOUNT, CHANGE_LANGUAGE, RESTORE_SHARE, RESTORE_FILE };
	public static final Dimension SIGNIN_PREFERRED_DIALOG_SIZE = new Dimension(700, 250);
	
	@FXML
	private GridPane signInPane;
	
	@FXML
	private VBox createPane;
	
	@FXML
	private TabPane tabPane;
		
	@FXML
	private TextField userNameField;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML 
	private ChoiceBox<ChoiceItem> languagesDropdown; 
	
	@FXML 
	Pane signInMessagePane;

	@FXML
	private Button okButton;
	
	@FXML
	private Button cancelButton;

	private SigninResult result;
	protected int secondsToDelay;
}
