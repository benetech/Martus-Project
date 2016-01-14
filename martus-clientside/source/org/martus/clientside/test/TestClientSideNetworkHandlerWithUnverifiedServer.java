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
package org.martus.clientside.test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcException;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer;
import org.martus.util.TestCaseEnhanced;


public class TestClientSideNetworkHandlerWithUnverifiedServer extends TestCaseEnhanced
{

	public TestClientSideNetworkHandlerWithUnverifiedServer(String name) 
	{
		super(name);
	}
	
	static class MockHandlerForNonSSL extends ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer
	{
		MockHandlerForNonSSL() throws Exception
		{
			super("", testPorts);
		}
		
		public Object callServerAtPort(String serverName, String method,
			Vector params, int port)
					throws MalformedURLException, XmlRpcException, IOException 
		{
			triedPorts.add(new Integer(port));
			if(failAll || port != goodPort)
				throw new ConnectException();
			return null;
		}
		boolean failAll;
		Vector triedPorts = new Vector(); 
		static int goodPort = 7;
		static int[] testPorts = {80, 443, goodPort, 986, 999};
	}

	public void testPortSelectionNonSSL() throws Exception
	{
		MockHandlerForNonSSL handler = new MockHandlerForNonSSL();
		handler.callServer("server", "method", null);
		assertContains("didn't try good port?", new Integer(MockHandlerForNonSSL.goodPort), handler.triedPorts);
		
		handler.triedPorts.clear();
		handler.callServer("server", "method", null);
		assertEquals("tried more than just the good port?", 1, handler.triedPorts.size());
		
		handler.triedPorts.clear();
		handler.failAll = true;
		handler.callServer("server", "method", null);
		assertEquals("didn't try all ports?", MockHandlerForNonSSL.testPorts.length, handler.triedPorts.size());
		handler.failAll = false;
		
	}
}
