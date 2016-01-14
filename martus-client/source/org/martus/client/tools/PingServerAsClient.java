/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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

package org.martus.client.tools;

import org.martus.client.network.OrchidTransportWrapperWithActiveProperty;
import org.martus.client.swingui.Martus;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.ClientSideNetworkInterface;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.OrchidTransportWrapper;

public class PingServerAsClient
{

	public static void main(String[] args) throws Exception
	{
		Martus.addThirdPartyJarsToClasspath();
		
		new PingServerAsClient(args);
	}
	
	PingServerAsClient(String[] args) throws Exception 
	{
		processArgs(args);
		
		OrchidTransportWrapper transport = OrchidTransportWrapperWithActiveProperty.createWithoutPersistentStore();
		ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer server = new ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer(ip, transport);
		if(!ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer.isNonSSLServerAvailable(server))
		{
			sleepToAvoidMixingOurOutputWithTheStackTrace();
			System.out.println("Can't contact non-ssl server");
			System.exit(2);
		}
		final String nonSSLPingResult = server.ping();
		System.out.println("nonSSL ping result: " + nonSSLPingResult);
		if(nonSSLPingResult == null)
			System.exit(2);
		
		String serverPublicKey = server.getServerPublicKey(new MartusSecurity());
		System.out.println("server public code (old): " + MartusCrypto.computeFormattedPublicCode(serverPublicKey));
		System.out.println("server public code (new): " + MartusCrypto.computeFormattedPublicCode40(serverPublicKey));

		ClientSideNetworkInterface networkInterface = ClientSideNetworkGateway.buildNetworkInterface(ip, serverPublicKey, transport);
		ClientSideNetworkGateway gateway = new ClientSideNetworkGateway(networkInterface);
		NetworkResponse response = gateway.getServerInfo();
		String sslPingResponse = response.getResultCode();
		System.out.println("ssl ping result: " + sslPingResponse);
		if(sslPingResponse == null || !sslPingResponse.equals("ok"))
			System.exit(3);

		System.exit(0);
	}

	private void sleepToAvoidMixingOurOutputWithTheStackTrace() throws InterruptedException
	{
		Thread.sleep(1000);
	}
	
	void processArgs(String[] args)
	{
		
		for (int i = 0; i < args.length; i++)
		{
			final String thisArg = args[i];
			String value = thisArg.substring(thisArg.indexOf("=")+1);
			
			if(thisArg.startsWith("--ip"))
				ip = value;		
		}

		if(ip == null)
		{
			System.err.println("Incorrect arguments: PingServerAsClient --ip=1.2.3.4 \n");
			System.exit(1);
		}
	}
	
	String ip;
}
