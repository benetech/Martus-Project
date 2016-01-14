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

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxInSwingWizardStage;
import org.martus.client.swingui.jfx.setupwizard.tasks.ConnectToServerTask;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusLogger;

abstract public class FxSetupWizardAbstractServerSetupController extends FxStep3Controller
{
	public FxSetupWizardAbstractServerSetupController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	public void attemptToConnect(String serverIPAddress, String serverPublicKey, boolean askComplianceAcceptance)
	{
		attemptToConnect(serverIPAddress, serverPublicKey, askComplianceAcceptance, "");
	}
	
	//TODO: look into removing duplicated code here and in SettingsForServerController
	public void attemptToConnect(String serverIPAddress, String serverPublicKey, boolean askComplianceAcceptance, String magicWord)
	{
		MartusLogger.log("Attempting to connect to: " + serverIPAddress);
		MartusApp app = getApp();
		getMainWindow().clearStatusMessage();
		ClientSideNetworkGateway gateway = ClientSideNetworkGateway.buildGateway(serverIPAddress, serverPublicKey, getApp().getTransport());

		FxInSwingWizardStage wizardStage = getWizardStage();
		wizardStage.setCurrentServerIsAvailable(false);
		try
		{
			ConnectToServerTask task = new ConnectToServerTask(getApp(), gateway, magicWord);
			MartusLocalization localization = getLocalization();
			String connectingToServerMsg = localization.getFieldLabel("AttemptToConnectToServerAndGetCompliance");
			showTimeoutDialog(connectingToServerMsg, task);
			if(!task.isAvailable())
			{
				String serverNotRespondingSaveConfigurationMessage = localization.getFieldLabel("ServerNotRespondingSaveConfiguration");
				if(showConfirmationDialog("ServerNotRespondingSaveConfiguration", serverNotRespondingSaveConfigurationMessage))
				{
					saveServerConfig(serverIPAddress, serverPublicKey, "");
					return;
				}
				return; 
			}
			if(!task.isAllowedToUpload())
			{
				showNotifyDialog("ErrorServerOffline");
				return;
			}
			String complianceStatement = task.getComplianceStatement();
			if(askComplianceAcceptance)
			{
				if(complianceStatement.equals(""))
				{
					showNotifyDialog("ServerComplianceFailed");
					saveServerConfig(serverIPAddress, serverPublicKey, "");
					return;
				}
				
				if(!acceptCompliance(complianceStatement))
				{
					ConfigInfo previousServerInfo = getApp().getConfigInfo();
					String previousServerName = previousServerInfo.getServerName();
					String previousServerKey = previousServerInfo.getServerPublicKey();
					String previousServerCompliance = previousServerInfo.getServerCompliance();
	
					//TODO:The following line shouldn't be necessary but without it, the trustmanager 
					//will reject the old server, we don't know why.
					ClientSideNetworkGateway.buildGateway(previousServerName, previousServerKey, getApp().getTransport());
					getApp().setServerInfo(previousServerName,previousServerKey,previousServerCompliance);
					return;
				}
			}

			getApp().setServerInfo(serverIPAddress, serverPublicKey, complianceStatement);
			
			app.getStore().clearOnServerLists();
			
			getMainWindow().forceRecheckOfUidsOnServer();
			app.getStore().clearOnServerLists();
			getMainWindow().repaint();
			getMainWindow().setStatusMessageReady();
			wizardStage.setCurrentServerIsAvailable(true);
		}
		catch(UserCancelledException e)
		{
		}
		catch(SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorSavingConfig");
		}
		catch(ServerNotAvailableException e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorServerOffline");
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorGettingCompliance");
		} 
	}
	
	private boolean acceptCompliance(String newServerCompliance)
	{
		MartusLocalization localization = getLocalization();
		String complianceStatementMsg = String.format("%s\n\n%s", localization.getFieldLabel("ServerComplianceDescription"), newServerCompliance);
		if(!showConfirmationDialog("ServerCompliance", complianceStatementMsg))
		{
			showNotifyDialog("UserRejectedServerCompliance");
			return false;
		}
		return true;
	}

	
	private void saveServerConfig(String serverIPAddress, String serverPublicKey, String complianceStatement) throws SaveConfigInfoException
	{
		getApp().setServerInfo(serverIPAddress, serverPublicKey, complianceStatement);
	}

}
