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
package org.martus.client.swingui.jfx.landing.general;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.client.swingui.jfx.setupwizard.step3.FxAdvancedServerStorageSetupController;
import org.martus.client.swingui.jfx.setupwizard.step3.FxSetupStorageServerController;
import org.martus.client.swingui.jfx.setupwizard.tasks.ConnectToServerTask;
import org.martus.client.swingui.jfx.setupwizard.tasks.GetServerPublicKeyTask;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.fieldspec.ChoiceItem;

public class SettingsForServerController extends FxController
{
	public SettingsForServerController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		try
		{
			initializeSyncFrequency();
			initializeServerInfo();
			IpPublicCodeChangeListener ipPublicCodeChangeListener = new IpPublicCodeChangeListener();
			advanceServerIpAddress.textProperty().addListener(ipPublicCodeChangeListener);
			advanceServerPublicCode.textProperty().addListener(ipPublicCodeChangeListener);
		} 
		catch (Exception e)
		{
			getStage().logAndNotifyUnexpectedError(e);
		} 
	}

	private void initializeServerInfo() throws Exception
	{
		ConfigInfo configInfo = getApp().getConfigInfo();
		boolean onStartupServerOnlineStatus = configInfo.getOnStartupServerOnlineStatus();
		serverDefaultToOn.selectedProperty().setValue(onStartupServerOnlineStatus);

		String serverPublicKey = configInfo.getServerPublicKey();
		String ipAddress = configInfo.getServerName();
		updateUiServerInfoAndButtonStatus(ipAddress, serverPublicKey);		
	}

	private void updateUiServerInfoAndButtonStatus(String ipAddress, String publicKey) throws Exception
	{
		String publicCode = getPublicCodeFromPublicKey(publicKey);
		currentServerIp.setText(ipAddress);
		currentServerPublicCode.setText(publicCode);
		if(isDefaultServer(ipAddress))
		{
			ipAddress = "";
			publicCode = "";
		}
		advanceServerIpAddress.setText(ipAddress);
		advanceServerPublicCode.setText(publicCode);
		updateConnectToAdvanceServerButtonState();
	}

	public String getPublicCodeFromPublicKey(String publicKey) throws Exception
	{
		if(publicKey.length() == 0)
			return "";
		
		return MartusCrypto.computeFormattedPublicCode40(publicKey);
	}
	
	protected void updateConnectToAdvanceServerButtonState()
	{
		String ipAddress = advanceServerIpAddress.getText();
		String publicCode = advanceServerPublicCode.getText();
		if(isDefaultServer(ipAddress))
		{
			connectToAdvanceServer.setDisable(true);
			return;
		}
		if (isIpAndPublicCodeValid(ipAddress, publicCode))
			connectToAdvanceServer.setDisable(false);
		else
			connectToAdvanceServer.setDisable(true);
	}
	
	private boolean isIpAndPublicCodeValid(String ipAddress, String publicCode)
	{
		if(!getMainWindow().isServerAccessible(ipAddress))
				return false;

		String normalizedPublicCode = MartusCrypto.removeNonDigits(publicCode);
		int publicCodeLength = normalizedPublicCode.length();
		return (		publicCodeLength == NORMALIZED_TWENTY_DIGIT_PUBLIC_CODE_LENGTH 
				 || publicCodeLength == NORMALIZED_FORTY_DIGIT_PUBLIC_CODE_LENGTH);
	}

	private void initializeSyncFrequency()
	{
		ObservableChoiceItemList automaticSyncChoices = createSyncChoiceInterval();
		automaticSyncFrequency.setItems(automaticSyncChoices);
		ObservableChoiceItemList autoMaticSyncMinuteChoices = createSyncChoiceMinuteIntervals();
		automaticSyncFrequencyMinutes.setItems(autoMaticSyncMinuteChoices);
		
		BooleanProperty downloadFromServerProperty = automaticallyDownloadFromServer.selectedProperty();
		automaticSyncFrequency.disableProperty().bind(downloadFromServerProperty.not());
		
		BooleanProperty downloadFromServerChecked = downloadFromServerProperty;
		BooleanBinding syncMinutesSelected = Bindings.equal(automaticSyncChoices.findByCode(SYNC_FREQUENCY_MINUTES), automaticSyncFrequency.getSelectionModel().selectedItemProperty());
		BooleanBinding shouldMinutesDropdownBeEnabled = Bindings.and(downloadFromServerChecked, syncMinutesSelected);
		automaticSyncFrequencyMinutes.disableProperty().bind(shouldMinutesDropdownBeEnabled.not());
		
		String currentSyncFrequency = getApp().getConfigInfo().getSyncFrequencyMinutes();		
		selectDefaultSyncFrequency(currentSyncFrequency);
	}
	
	private void selectDefaultSyncFrequency(String syncFrequency)
	{
		boolean syncFromServer = true;
		if(syncFrequency.equals(NEVER))
			syncFromServer = false;
		automaticallyDownloadFromServer.setSelected(syncFromServer);

		String syncFrequencyInterval = DEFAULT_SYNC_FREQUENCY;
		String syncFrequencyMinutes = DEFAULT_SYNC_MINUTES_FREQUENCY;
		if(syncFrequency.equals(SYNC_FREQUENCY_ON_STARTUP))
		{
			syncFrequencyInterval = SYNC_FREQUENCY_ON_STARTUP;
		}
		else if(syncFrequency.equals(SYNC_FREQUENCY_ONCE_AN_HOUR))
		{
			syncFrequencyInterval = SYNC_FREQUENCY_ONCE_AN_HOUR;
		}
		else
		{
			syncFrequencyInterval = SYNC_FREQUENCY_MINUTES;
			syncFrequencyMinutes = syncFrequency;
		}		
		
		selectChoiceByCode(automaticSyncFrequency, syncFrequencyInterval, DEFAULT_SYNC_FREQUENCY);
		selectChoiceByCode(automaticSyncFrequencyMinutes, syncFrequencyMinutes, DEFAULT_SYNC_MINUTES_FREQUENCY);
	}

	private static void selectChoiceByCode(ChoiceBox choiceBox, String codeToFind, String defaultChoice)
	{
		ObservableChoiceItemList choices = new ObservableChoiceItemList(choiceBox.getItems());
		ChoiceItem itemToBeSelected = choices.findByCode(codeToFind);
		SingleSelectionModel model = choiceBox.getSelectionModel();
		if(itemToBeSelected == null)
			itemToBeSelected = choices.findByCode(defaultChoice);
		if(itemToBeSelected == null)
			model.clearSelection();
		else
			model.select(itemToBeSelected);
	}
	
	class IpPublicCodeChangeListener implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> observedValue,
				String oldValue, String newValue)
		{
			updateConnectToAdvanceServerButtonState();
		}
	}
	
	private ObservableChoiceItemList createSyncChoiceInterval()
	{
		ObservableChoiceItemList choices = new ObservableChoiceItemList();

		choices.add(new ChoiceItem(SYNC_FREQUENCY_MINUTES, getLocalization().getFieldLabel("SyncFrequencyMinutes")));
		choices.add(new ChoiceItem(SYNC_FREQUENCY_ON_STARTUP, getLocalization().getFieldLabel("SyncFrequencyOnStartup")));
		choices.add(new ChoiceItem(SYNC_FREQUENCY_ONCE_AN_HOUR, getLocalization().getFieldLabel("SyncFrequencyOnceAnHour")));
		return choices;
	}
	
	private ObservableChoiceItemList createSyncChoiceMinuteIntervals()
	{
		ObservableChoiceItemList choices = new ObservableChoiceItemList();

		choices.add(new ChoiceItem("1", getLocalization().getFieldLabel("SyncFrequencyOneMinute")));
		choices.add(new ChoiceItem("2", getLocalization().getFieldLabel("SyncFrequencyTwoMinutes")));
		choices.add(new ChoiceItem("5", getLocalization().getFieldLabel("SyncFrequencyFiveMinutes")));
		choices.add(new ChoiceItem("10", getLocalization().getFieldLabel("SyncFrequencyTenMinutes")));
		choices.add(new ChoiceItem("15", getLocalization().getFieldLabel("SyncFrequencyFifteenMinutes")));
		choices.add(new ChoiceItem("30", getLocalization().getFieldLabel("SyncFrequencyThirtyMinutes")));
		choices.add(new ChoiceItem("45", getLocalization().getFieldLabel("SyncFrequencyFortyFiveMinutes")));
		
		return choices;
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "landing/general/SettingsForServer.fxml";
	}
	
	@FXML
	public void onConnectToDefaultServer()
	{
		try
		{
			String ipAddress = FxSetupStorageServerController.getDefaultServerIp();
			String publicCode = getPublicCodeFromPublicKey(FxSetupStorageServerController.getDefaultServerPublicKey());
			connectToServerAndSave(ipAddress, publicCode);
		} 
		catch (Exception e)
		{
			getStage().logAndNotifyUnexpectedError(e);
		}
	}
	
	@FXML
	public void onConnectToAdvanceServer()
	{
		String ipAddress = advanceServerIpAddress.getText();
		String publicCode = advanceServerPublicCode.getText();
		connectToServerAndSave(ipAddress, publicCode);
	}

	public void connectToServerAndSave(String ipAddress, String publicCode)
	{
		boolean needsComplianceConfirmation = !isDefaultServer(ipAddress);
		String newServersPublicKey = attemptToConnect(ipAddress, publicCode, needsComplianceConfirmation);
		if(newServersPublicKey == null)
			return;

		MartusApp app = getApp();
		app.getConfigInfo().setServerPublicKey(newServersPublicKey);
		app.getConfigInfo().setServerName(ipAddress);
		try
		{
			app.saveConfigInfo();
		} 
		catch (SaveConfigInfoException e)
		{
			getStage().logAndNotifyUnexpectedError(e);
		}
	}

	public boolean isDefaultServer(String ipAddress)
	{
		return ipAddress.equals(FxSetupStorageServerController.getDefaultServerIp());
	}
	
	//TODO: look into removing duplicated code here and in FxSetupWizardAbstractServerSetupController
	private String attemptToConnect(String serverIPAddress, String publicCode, boolean needsComplianceConfirmation)
	{
		MartusLogger.log("Attempting to connect to: " + serverIPAddress);
		MartusApp app = getApp();
		getMainWindow().clearStatusMessage();
		try
		{
			GetServerPublicKeyTask getPublicKeyTask = new GetServerPublicKeyTask(getApp(), serverIPAddress);
			showTimeoutDialog(getLocalization().getFieldLabel("GettingServerInformation"), getPublicKeyTask);
			
			String newServerPublicKey = getPublicKeyTask.getPublicKey();
			if(!FxAdvancedServerStorageSetupController.doesPublicCodeMatch(newServerPublicKey, publicCode))
			{
				showNotifyDialog("ServerCodeWrong");
				return null;
			}
			ClientSideNetworkGateway gateway = ClientSideNetworkGateway.buildGateway(serverIPAddress, newServerPublicKey, getApp().getTransport());
			ConnectToServerTask connectToServerTask = new ConnectToServerTask(getApp(), gateway, "");
			MartusLocalization localization = getLocalization();
			String connectingToServerMsg = localization.getFieldLabel("AttemptToConnectToServerAndGetCompliance");
			showTimeoutDialog(connectingToServerMsg, connectToServerTask);
			if(!connectToServerTask.isAvailable())
			{
				showNotifyDialog("AdvanceServerNotResponding");
				return null; 
			}
			if(!connectToServerTask.isAllowedToUpload())
			{
				showNotifyDialog("ErrorServerOffline");
				return null;
			}
			String complianceStatement = connectToServerTask.getComplianceStatement();
			if(needsComplianceConfirmation)
			{
				if(complianceStatement.equals(""))
				{
					showNotifyDialog("ServerComplianceFailed");
					return null;
				}
				
				if(!acceptCompliance(complianceStatement))
				{
					ConfigInfo previousServerInfo = app.getConfigInfo();
					String previousServerName = previousServerInfo.getServerName();
					String previousServerKey = previousServerInfo.getServerPublicKey();
					String previousServerCompliance = previousServerInfo.getServerCompliance();
	
					//TODO:The following line shouldn't be necessary but without it, the trustmanager 
					//will reject the old server, we don't know why.
					ClientSideNetworkGateway.buildGateway(previousServerName, previousServerKey, getApp().getTransport());
					getApp().setServerInfo(previousServerName,previousServerKey,previousServerCompliance);
					return null;
				}
			}

			updateUiServerInfoAndButtonStatus(serverIPAddress, newServerPublicKey);
			app.setServerInfo(serverIPAddress, newServerPublicKey, complianceStatement);
			app.getStore().clearOnServerLists();
			
			getMainWindow().forceRecheckOfUidsOnServer();
			app.getStore().clearOnServerLists();
			getMainWindow().repaint();
			getMainWindow().setStatusMessageReady();
			return newServerPublicKey;
		}
		catch(UserCancelledException e)
		{
		}
		catch (SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorSavingFile");
		}
		catch (ServerNotAvailableException e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("AdvanceServerNotResponding");
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("UnexpectedError");
		}
		return null;
	}
	
	private boolean acceptCompliance(String newServerCompliance)
	{
		MartusLocalization localization = getLocalization();
		String complianceStatementMsg = String.format("%s\n\n%s", localization.getFieldLabel("ServerComplianceDescription"), newServerCompliance);
		if(!showConfirmationDialog("ServerCompliance", "ServerComplianceAccept", "ServerComplianceReject", complianceStatementMsg))
		{
			showNotifyDialog("UserRejectedServerCompliance");
			return false;
		}
		return true;
	}
	

	@FXML
	public void onSaveServerPreferenceChanges()
	{		
		ConfigInfo configInfo = getApp().getConfigInfo();
		String frequencyInMinutes = NEVER;
		if(automaticallyDownloadFromServer.isSelected())
		{
			frequencyInMinutes = automaticSyncFrequency.getSelectionModel().getSelectedItem().getCode();			
			if(frequencyInMinutes.equals(SYNC_FREQUENCY_MINUTES))
				frequencyInMinutes = automaticSyncFrequencyMinutes.getSelectionModel().getSelectedItem().getCode();
		}
		configInfo.setSyncFrequencyMinutes(frequencyInMinutes);
		configInfo.setOnStartupServerOnlineStatus(serverDefaultToOn.selectedProperty().getValue());
		try
		{
			getApp().saveConfigInfo();
		} 
		catch (SaveConfigInfoException e)
		{
			getStage().logAndNotifyUnexpectedError(e);
		}
		getApp().turnNetworkOnOrOffAsRequested();
	}

	public final static String SYNC_FREQUENCY_ON_STARTUP = "OnStartup";
	private final static String NEVER = "";
	private final static String SYNC_FREQUENCY_MINUTES = "Minutes";
	private final static String SYNC_FREQUENCY_ONCE_AN_HOUR = "60";
		
	private final static String DEFAULT_SYNC_FREQUENCY = SYNC_FREQUENCY_MINUTES;
	private final static String DEFAULT_SYNC_MINUTES_FREQUENCY = "5";
	
	private final static int NORMALIZED_TWENTY_DIGIT_PUBLIC_CODE_LENGTH = 20;
	private final static int NORMALIZED_FORTY_DIGIT_PUBLIC_CODE_LENGTH = 40;

	@FXML
	private TextField currentServerIp;
	@FXML
	private TextField currentServerPublicCode;
	
	@FXML
	private TextField advanceServerIpAddress;
	@FXML
	private TextField advanceServerPublicCode;
	@FXML
	private Button connectToAdvanceServer;

	@FXML
	private CheckBox serverDefaultToOn;
	@FXML
	private CheckBox automaticallyDownloadFromServer;
	@FXML
	private ChoiceBox<ChoiceItem> automaticSyncFrequency;
	@FXML
	private ChoiceBox<ChoiceItem> automaticSyncFrequencyMinutes;
}
