/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003-2007, Beneficent
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

package org.martus.meta;

import java.util.Vector;

import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpc;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.network.MartusSecureWebServer;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.SimpleX509TrustManager;
import org.martus.common.network.TorTransportWrapper;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.ServerForClients;
import org.martus.server.forclients.ServerSideNetworkHandler;
import org.martus.util.TestCaseEnhanced;



public class TestSSL extends TestCaseEnhanced 
{
	public TestSSL(String name) 
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(mockSecurityForServer == null)
		{
			int[] nonSslPorts = {1988};
			int[] sslPorts = {1987};
			mockSecurityForServer = MockMartusSecurity.createServer();
			mockServer = new MockMartusServer();
			mockServer.verifyAndLoadConfigurationFiles();
			mockServer.setSecurity(mockSecurityForServer);
			MartusSecureWebServer.security = mockSecurityForServer;
			
			serverForClients = new ServerForClients(mockServer);
			serverForClients.handleNonSSL(nonSslPorts);
			serverForClients.handleSSL(sslPorts);
			
//			XmlRpc.debug = true;
			TorTransportWrapper torTransport = TorTransportWrapper.createWithoutPersistentStore();
			proxy1 = new ClientSideNetworkHandlerUsingXmlRpc("localhost", sslPorts, torTransport);
//			proxy2 = new ClientSideNetworkHandlerUsingXmlRpc("localhost", testport);
		}
	}
	
	public void tearDown() throws Exception
	{
		mockServer.deleteAllFiles();
		serverForClients.prepareToShutdown();
		super.tearDown();
	}

	
	public void testBasics() throws Exception
	{
		verifyBadCertBeforeGoodCertHasBeenAccepted();
		verifyGoodCertAndItWillNotBeReverifiedThisSession();

	}
	
	public void verifyBadCertBeforeGoodCertHasBeenAccepted() throws Exception
	{
		SimpleX509TrustManager trustManager = proxy1.getSimpleX509TrustManager();
		assertNull("Already trusted?", trustManager.getExpectedPublicKey());

		proxy1.getSimpleX509TrustManager().setExpectedPublicCode("Not a valid code");
		trustManager.clearCalledCheckServerTrusted();
		MartusLogger.temporarilyDisableLogging();
		try
		{
			System.out.println("---------------------------------------");
			System.out.println("Ignore the following exceptions (from " + getClass().toString() + "):");
			assertNull("accepted bad cert?", proxy1.getServerInfo(new Vector()));
			assertTrue("Never checked ssl cert!", trustManager.wasCheckServerTrustedCalled());
			Thread.sleep(1000);
			System.err.flush();
			System.out.println("---------------------------------------");
		}
		finally
		{
			MartusLogger.reEnableLogging();
		}
	}
	
	public void verifyGoodCertAndItWillNotBeReverifiedThisSession()
	{
		String serverAccountId = mockSecurityForServer.getPublicKeyString();
		SimpleX509TrustManager trustManager = proxy1.getSimpleX509TrustManager();
		trustManager.setExpectedPublicKey(serverAccountId);

		Vector parameters = new Vector();
		NetworkResponse result = new NetworkResponse(proxy1.getServerInfo(parameters));
		assertEquals(NetworkInterfaceConstants.OK, result.getResultCode());
		assertEquals(NetworkInterfaceConstants.VERSION, result.getResultVector().get(0));
		assertEquals(serverAccountId, trustManager.getExpectedPublicKey());

		NetworkResponse response = new NetworkResponse(proxy1.getServerInfo(new Vector()));
		assertEquals(NetworkInterfaceConstants.OK, response.getResultCode());
		assertEquals(NetworkInterfaceConstants.VERSION, response.getResultVector().get(0));
	}
	
	static MockMartusSecurity mockSecurityForServer;
	static MockMartusServer mockServer;
	static ServerSideNetworkHandler mockSSLServerInterface;
	static ClientSideNetworkHandlerUsingXmlRpc proxy1;
	static ServerForClients serverForClients;
}
