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

import android.util.Log;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;
import org.apache.xmlrpc.util.SAXParsers;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkInterfaceXmlRpcConstants;
import org.martus.common.network.TransportWrapper;

import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.SAXParserFactory;

public class MobileClientSideNetworkHandlerUsingXmlRpcForNonSSL extends ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer {

    private static final String TAG = "MobileClientSideNetworkHandlerUsingXmlRpcForNonSSL";
	public MobileClientSideNetworkHandlerUsingXmlRpcForNonSSL(String serverName, TransportWrapper transportToUse) throws Exception
	{
		super(serverName, NetworkInterfaceXmlRpcConstants.defaultSSLPorts, transportToUse);
	}

	@Override
	public Object callServerAtPort(String serverName, String method, Vector params, int port)
		throws Exception
	{
		if(!(getTransport().isReady()))
		{
			Log.w(TAG, "Warning: JTor transport not ready for " + method);
			return new String[] { NetworkInterfaceConstants.TRANSPORT_NOT_READY };
		}

		if(ClientPortOverride.useInsecurePorts)
			port += 9000;
		
		final String serverUrl = "https://" + serverName + ":" + port + "/RPC2";
		Log.d(TAG, "MartusServerProxyViaXmlRpc:callServer serverUrl=" + serverUrl);

		// NOTE: We **MUST** create a new XmlRpcClient for each call, because
		// there is a memory leak in apache xmlrpc 1.1 that will cause out of 
		// memory exceptions if we reuse an XmlRpcClient object
		XmlRpcClient client = new XmlRpcClient();
		XmlRpcTransportFactory transportFactory = getTransport().createTransport(client, getTm());
		if(transportFactory != null)
			client.setTransportFactory(transportFactory);

		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(serverUrl));
		SAXParsers.setSAXParserFactory(SAXParserFactory.newInstance());
		client.setConfig(config);
		
		return client.execute("MartusServer." + method, params);
	}

}
