/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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
package org.martus.common.network;

import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class MspaNonSSLXmlrpcClient implements NetworkInterfaceConstants
{
	public MspaNonSSLXmlrpcClient(String serverIPAddressToUse, int portToUse)
	{
		serverIPAddress = serverIPAddressToUse;
		port = portToUse;
	}

	public Object callserver(
		String serverObjectName,
		String method,
		Vector params)
		throws IOException
	{
		final String serverUrl = "http://" + serverIPAddress + ":" + port + "/RPC2";
		Object result = null;
		try
		{
			// NOTE: We **MUST** create a new XmlRpcClient for each call, because
			// there is a memory leak in apache xmlrpc 1.1 that will cause out of 
			// memory exceptions if we reuse an XmlRpcClient object
			XmlRpcClient client = new XmlRpcClient();

			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(serverUrl));
			client.setConfig(config);

			result = client.execute(serverObjectName + "." + method, params);
		}
		catch (IOException e)
		{
			//TODO throw IOExceptions so caller can decide what to do.
			//This was added for connection refused: connect (no server connected)
			//System.out.println("ServerInterfaceXmlRpcHandler:callServer Exception=" + e);
			throw e;
		}
		catch (Exception e)
		{
			System.out.println("MartusNonSSLXmlrpcClient:callServer Exception=" + e);
			e.printStackTrace();
		}
		return result;
	}
	
	String serverIPAddress;
	int port;
}
