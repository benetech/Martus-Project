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
package org.martus.client.swingui.jfx.setupwizard.step3;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.contacts.FxWizardAddContactsController;
import org.martus.client.swingui.jfx.generic.FxInSwingWizardStage;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.client.swingui.jfx.setupwizard.tasks.GetServerPublicKeyTask;
import org.martus.common.DammCheckDigitAlgorithm.CheckDigitInvalidException;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.common.crypto.MartusSecurity;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

public class FxAdvancedServerStorageSetupController extends	FxSetupWizardAbstractServerSetupController implements Initializable
{
	public FxAdvancedServerStorageSetupController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initializeMainContentPane()
	{
		ipAddressField.textProperty().addListener(new TextFieldChangeHandler());
		publicCodeField.textProperty().addListener(new TextFieldChangeHandler());
		
		ConfigInfo config = getApp().getConfigInfo();
		ipAddressField.setText(config.getServerName());
		String serverKey = config.getServerPublicKey();
		if(serverKey.length() > 0)
		{
			try
			{
				String publicCode = MartusSecurity.computeFormattedPublicCode40(serverKey);
				publicCodeField.setText(publicCode);
			}
			catch(Exception e)
			{
				MartusLogger.logException(e);
				// TODO: Should we display an error here, or just be silent?
			}
		}
		
		updateButtonStates();
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step3/AdvancedServerStorageSetup.fxml";
	}
	
	@FXML
	private void onLinkMailTo()
	{
		openDefaultEmailApp("mailto:martus@benetech.org");
	}

	@FXML
	public void connect()
	{
		try
		{
			String ip = ipAddressField.getText();
			
			GetServerPublicKeyTask task = new GetServerPublicKeyTask(getApp(), ip);
			showTimeoutDialog(getLocalization().getFieldLabel("GettingServerInformation"), task);
			
			String serverKey = task.getPublicKey();
			String userEnteredPublicCode = publicCodeField.getText();
			if(!doesPublicCodeMatch(serverKey, userEnteredPublicCode))
			{
				showError("ServerCodeWrong");
				return;
			}
			
			String magicWord = magicWordField.getText();
			attemptToConnect(ip, serverKey, true, magicWord);

			FxInSwingWizardStage wizardStage = getWizardStage();
			if(wizardStage.checkIfCurrentServerIsAvailable())
				getWizardNavigationHandler().doNext();
		} 
		catch(UserCancelledException e)
		{
			getWizardStage().setCurrentServerIsAvailable(false);
		}
		catch (SaveConfigInfoException e)
		{
			getWizardStage().setCurrentServerIsAvailable(false);
			MartusLogger.logException(e);
			showNotifyDialog("ErrorSavingFile");
		}
		catch (ServerNotAvailableException e)
		{
			getWizardStage().setCurrentServerIsAvailable(false);
			MartusLogger.logException(e);
			showNotifyDialog("AdvanceServerNotResponding");
		}
		catch (Exception e)
		{
			getWizardStage().setCurrentServerIsAvailable(false);
			MartusLogger.logException(e);
			showNotifyDialog("UnexpectedError");
		}
		finally
		{
			updateButtonStates();
		}
	}

	static public boolean doesPublicCodeMatch(String serverKey,
			String userEnteredPublicCode) throws InvalidBase64Exception,
			CheckDigitInvalidException, CreateDigestException
	{
		String serverPublicCode = MartusCrypto.computePublicCode(serverKey);
		String serverPublicCode40 = MartusCrypto.computePublicCode40(serverKey);

		String normalizedPublicCode = MartusCrypto.removeNonDigits(userEnteredPublicCode);
		return (serverPublicCode.equals(normalizedPublicCode) || serverPublicCode40.equals(normalizedPublicCode));
	}
	
	@Override
	public AbstractFxSetupWizardContentController getNextController()
	{
		return new FxWizardAddContactsController(getMainWindow());
	}
	
	protected class TextFieldChangeHandler implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2)
		{
			updateButtonStates();
		}
		
	}
	
	protected void updateButtonStates()
	{
		boolean hasIp = false;
		boolean hasPublicCode = false;
		
		String ip = ipAddressField.getText();
		if(ip.length() > 0)
			hasIp = true;
		
		String publicCode = publicCodeField.getText();
		if(publicCode.length() > 0)
			hasPublicCode = true;
		
		boolean canConnect = (hasIp && hasPublicCode);
		connectButton.setDisable(!canConnect);

		getWizardNavigationHandler().getNextButton().setDisable(!getWizardStage().checkIfCurrentServerIsAvailable());
	}
	
	private void showError(String errorTag)
	{
		showNotifyDialog(errorTag);
	}

	@FXML
	private Label statusLabel;
	
	@FXML
	private Button connectButton;
	
	@FXML
	private TextField ipAddressField;
	
	@FXML
	private TextField publicCodeField;
	
	@FXML
	private TextField magicWordField;
}
