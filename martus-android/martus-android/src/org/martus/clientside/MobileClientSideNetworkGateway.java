/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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

package org.martus.clientside;

import org.martus.common.network.ClientSideNetworkInterface;
import org.martus.common.network.NetworkInterface;
import org.martus.common.network.NetworkInterfaceXmlRpcConstants;
import org.martus.common.network.PassThroughTransportWrapper;

public class MobileClientSideNetworkGateway extends ClientSideNetworkGateway
{
	public MobileClientSideNetworkGateway(ClientSideNetworkInterface serverToUse)
	{
		super(serverToUse);
	}


	static public MobileClientSideNetworkGateway buildGateway(String serverName, String serverPublicKey, PassThroughTransportWrapper transportToUse)
	{
        ClientSideNetworkInterface server = buildNetworkInterface(serverName, serverPublicKey, transportToUse);
		if(server == null)
			return null;

		return new MobileClientSideNetworkGateway(server);
	}

	public static ClientSideNetworkInterface buildNetworkInterface(String serverName, String serverPublicKey, PassThroughTransportWrapper transport)
	{
		if(serverName.length() == 0)
			return null;

		try
		{
			int[] ports = NetworkInterfaceXmlRpcConstants.defaultSSLPorts;
			MobileClientSideNetworkHandlerUsingXmlRpc handler = new MobileClientSideNetworkHandlerUsingXmlRpc(serverName, ports, transport);
			handler.getSimpleX509TrustManager().setExpectedPublicKey(serverPublicKey);
			return handler;
		}
		catch (ClientSideNetworkHandlerUsingXmlRpc.SSLSocketSetupException e)
		{
			//TODO propagate to UI and needs a test.
			e.printStackTrace();
			return null;
		}
	}
}
