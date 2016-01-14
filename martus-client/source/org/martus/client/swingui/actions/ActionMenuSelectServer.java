/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

package org.martus.client.swingui.actions;

import java.awt.event.ActionEvent;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiConfigServerDlg;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.Exceptions.NetworkOfflineException;

public class ActionMenuSelectServer extends UiMenuAction implements ActionDoer
{
	public ActionMenuSelectServer(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "SelectServer");
	}

	public void actionPerformed(ActionEvent ae)
	{
		doAction();
	}

	public void doAction()
	{
		getMainWindow().offerToCancelRetrieveInProgress();
		if(getMainWindow().isRetrieveInProgress())
			return;
		
		getMainWindow().inConfigServer = true;
		try
		{
			getMainWindow().clearStatusMessage();
			ConfigInfo previousServerInfo = getApp().getConfigInfo();
			UiConfigServerDlg serverInfoDlg = new UiConfigServerDlg(getMainWindow(), previousServerInfo);
			if(!serverInfoDlg.getResult())
				return;		
			String serverIPAddress = serverInfoDlg.getServerIPAddress();
			String serverPublicKey = serverInfoDlg.getServerPublicKey();
			ClientSideNetworkGateway gateway = ClientSideNetworkGateway.buildGateway(serverIPAddress, serverPublicKey, getApp().getTransport());
			
			if(!getApp().isSSLServerAvailable(gateway))
			{
				getMainWindow().notifyDlg("ServerSSLNotResponding");
				return;
			}
		
			String newServerCompliance = getMainWindow().getServerCompliance(gateway);
			if(!getMainWindow().confirmServerCompliance("ServerComplianceDescription", newServerCompliance))
			{
				//FIXME: Since TrustManager is global (part of socket factory), after 
				// contacting the potential new server, we have to reset everything 
				// back to the old server. Really, TM shouldn't behave like a global. 
				ClientSideNetworkGateway.buildGateway(previousServerInfo.getServerName(), previousServerInfo.getServerPublicKey(), getApp().getTransport());
				
				getMainWindow().notifyDlg("UserRejectedServerCompliance");
				if(serverIPAddress.equals(previousServerInfo.getServerName()) &&
				   serverPublicKey.equals(previousServerInfo.getServerPublicKey()))
				{
					getApp().setServerInfo("","","");
				}
				return;
			}
			getStore().clearOnServerLists();
			boolean magicAccepted = false;
			getApp().setServerInfo(serverIPAddress, serverPublicKey, newServerCompliance);
			getMainWindow().setNeedToGetAccessToken();
			if(getApp().requestServerUploadRights(""))
				magicAccepted = true;
			else
			{
				while (true)
				{
					String magicWord = getMainWindow().getStringInput("servermagicword", "", "", "");
					if(magicWord == null)
						break;
					if(getApp().requestServerUploadRights(magicWord))
					{
						magicAccepted = true;
						break;
					}
					getMainWindow().notifyDlg("magicwordrejected");
				}
			}
		
			String title = getLocalization().getWindowTitle("ServerSelectionResults");
			String serverSelected = getLocalization().getFieldLabel("ServerSelectionResults") + serverIPAddress;
			String uploadGranted = "";
			if(magicAccepted)
				uploadGranted = getLocalization().getFieldLabel("ServerAcceptsUploads");
			else
				uploadGranted = getLocalization().getFieldLabel("ServerDeclinesUploads");
		
			String ok = getLocalization().getButtonLabel(EnglishCommonStrings.OK);
			String[] contents = {serverSelected, uploadGranted};
			String[] buttons = {ok};
		
			getMainWindow().notifyDlg(title, contents, buttons);
			
			getMainWindow().forceRecheckOfUidsOnServer();
			getStore().clearOnServerLists();
			getMainWindow().repaint();
			getMainWindow().setStatusMessageReady();
		}
		catch(NetworkOfflineException e)
		{
			getMainWindow().notifyDlg("ErrorNetworkOffline");
		}
		catch(SaveConfigInfoException e)
		{
			e.printStackTrace();
			getMainWindow().notifyDlg("ErrorSavingConfig");
		}
		catch(Exception e)
		{
			getMainWindow().unexpectedErrorDlg(e);
		}
		finally
		{
			getMainWindow().inConfigServer = false;
		}
	}

}