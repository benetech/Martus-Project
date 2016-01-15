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
package org.martus.meta;

import java.util.Vector;

import org.junit.Test;
import org.martus.client.test.MockClientSideNetworkHandler;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.SummaryOfAvailableBulletins;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.ServerForClients;
import org.martus.server.forclients.ServerSideNetworkHandler;
import org.martus.util.TestCaseEnhanced;
import org.miradi.utils.EnhancedJsonObject;

public class TestListAvailableRevisionsSince extends TestCaseEnhanced
{

	public TestListAvailableRevisionsSince(String name)
	{
		super(name);
	}

	@Test
	public void testNothingOnServer() throws Exception
	{
		MockMartusServer server = new MockMartusServer(this);
		ServerForClients serverForClients = server.serverForClients;
		serverForClients.loadBannedClients();
		ServerSideNetworkHandler serverHandler = new ServerSideNetworkHandler(serverForClients);
		MockClientSideNetworkHandler clientHandler = new MockClientSideNetworkHandler(serverHandler);
		ClientSideNetworkGateway gateway = new ClientSideNetworkGateway(clientHandler);
		
		MockMartusSecurity client = MockMartusSecurity.createClient();
		final String EVERYTHING = "";
		verifyNothingAvailable(gateway, EVERYTHING, client);
		
		final String RECENT = "2014-01-01T14:03:35.358Z";
		verifyNothingAvailable(gateway, RECENT, client);
		
	}

	public void verifyNothingAvailable(ClientSideNetworkGateway gateway, String since, MockMartusSecurity client) throws Exception 
	{
		NetworkResponse response = gateway.listAvailableRevisionsSince(client, since);
		assertEquals(NetworkInterfaceConstants.OK, response.getResultCode());
		Vector resultVector = response.getResultVector();
		assertEquals(1, resultVector.size());
		String jsonString = (String) resultVector.get(0);
		EnhancedJsonObject json = new EnhancedJsonObject(jsonString);
		assertEquals(0, json.getInt(SummaryOfAvailableBulletins.JSON_KEY_COUNT));
		assertEquals("", json.getString(SummaryOfAvailableBulletins.JSON_KEY_HIGHEST_SERVER_TIMESTAMP));
	}

}
