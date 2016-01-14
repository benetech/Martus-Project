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
package org.martus.client.swingui.jfx.setupwizard.tasks;

import org.martus.client.core.MartusApp;
import org.martus.clientside.ClientSideNetworkGateway;

public class ConnectToServerTask extends ServerCallTask
{
	public ConnectToServerTask(MartusApp appToUse)
	{
		this(appToUse, appToUse.getCurrentNetworkInterfaceGateway(), "");
	}

	public ConnectToServerTask(MartusApp appToUse, ClientSideNetworkGateway gatewayToUse, String magicWordToUse)
	{
		super(appToUse, gatewayToUse);
		
		gateway = gatewayToUse;
		magicWord = magicWordToUse;
	}
	
	public boolean isAvailable()
	{
		return isAvailable;
	}
	
	public String getComplianceStatement()
	{
		return complianceStatement;
	}
	
	public boolean isAllowedToUpload()
	{
		return isAllowedToUpload;
	}
	
	@Override
	public int getMaxSeconds()
	{
		return getInterface().getTimeoutSecondsForGetServerInfo() * 2;
	}

	@Override
	protected Void call() throws Exception
	{
		isAvailable = getApp().isSSLServerAvailable(gateway);
		if (isAvailable)
		{
			complianceStatement = app.getServerCompliance(getGateway());
			isAllowedToUpload = getApp().requestServerUploadRights(getGateway(), magicWord);
		}
		
		return null;
	}

	private ClientSideNetworkGateway gateway;
	private boolean isAvailable;
	private String complianceStatement;
	private String magicWord;
	private boolean isAllowedToUpload; 
}
