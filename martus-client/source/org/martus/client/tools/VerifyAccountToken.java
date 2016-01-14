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

package org.martus.client.tools;

import java.io.File;

import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.MartusAppInitializationException;
import org.martus.client.swingui.Martus;
import org.martus.client.swingui.MartusLocalization;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.PassThroughTransportWrapper;
import org.martus.common.network.TransportWrapper;
import org.martus.util.UnicodeReader;

public class VerifyAccountToken
{
	public static void main(String[] args) throws Exception
	{
		new VerifyAccountToken(args);
	}
	
	public VerifyAccountToken(String[] args) throws Exception
	{
		System.out.println("Usage: VerifyAccountToken <server ip> <username>");
		System.out.println("  'password' will be used as the accounts password, this app will show the access token ");
		System.out.println("  for that account on the specified server. ");
		System.out.println();
		System.out.flush();
		
		Martus.addThirdPartyJarsToClasspath();
		
		processArgs(args);
		
		reader = new UnicodeReader(System.in);
		initializeApp();

		String publicKeyString = getSecurity().getPublicKeyString();
		String publicCode20 = MartusCrypto.computeFormattedPublicCode(publicKeyString);
		String publicCode40 = MartusCrypto.computeFormattedPublicCode40(publicKeyString);
		System.out.println("Public code 20: " + publicCode20);
		System.out.println("Public code 40: " + publicCode40);
		System.out.println();
		System.out.flush();

		try
		{
			initializeNetworkConnection();
		}
		catch(Exception e)
		{
			MartusLogger.log("Exception contacting server");
			MartusLogger.logException(e);
			System.exit(1);
		}
		
		NetworkResponse response = gateway.getMartusAccountAccessToken(getSecurity());
		String result = response.getResultCode();
		if(result == null)
		{
			System.err.println("Unable to connect to server");
			System.exit(1);
		}
		if(!result.equals(NetworkInterfaceConstants.OK))
		{
			System.err.println("Error calling server: " + result);
			System.exit(1);
		}
		
		String token = (String)response.getResultVector().get(0);
		System.out.println("Access Token: " + token);
		
		System.exit(0);
	}

	public void initializeApp() throws MartusAppInitializationException,
			Exception
	{
		File codeDirectory = MartusApp.getTranslationsDirectory();
		app = new MartusApp(new MartusLocalization(codeDirectory, new String[] {}));
	
		// For security, instead of asking for a passphrase, we will just assume it is "password"
//		String userPassPhrase = getUserInput("passphrase:");
		String userPassPhrase = "password";
		app.attemptSignIn(username, userPassPhrase.toCharArray());
		app.doAfterSigninInitalization();
		app.loadFolders();
	}

	private void initializeNetworkConnection() throws Exception
	{
		System.out.println("Connecting to: " + server);
		System.out.flush();
		
		TransportWrapper transport = new PassThroughTransportWrapper();
		ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer handler = new ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer(server, transport);
		String serverPublicKey = handler.getServerPublicKey(getSecurity());
		gateway = ClientSideNetworkGateway.buildGateway(server, serverPublicKey, transport);
	}

	String getUserInput(String prompt) throws Exception
	{
		System.out.print(prompt);
		System.out.flush();
		String result = reader.readLine();
		return result;
	}
	
	private MartusCrypto getSecurity()
	{
		return app.getSecurity();
	}

	void processArgs(String[] args)
	{
		if(args.length != 2)
		{
			System.err.println("Must specify a server IP address and username");
			System.exit(1);
		}
		
		server = args[0];
		username = args[1];
	}

	private String username;
	private UnicodeReader reader;
	private MartusApp app;
	private String server;
	private ClientSideNetworkGateway gateway;
}
