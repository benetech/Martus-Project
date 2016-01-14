/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2007, Beneficent
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
package org.martus.common.xmlrpc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.apache.xmlrpc.util.ThreadPool.Task;
import org.apache.xmlrpc.webserver.ConnectionServerWithIpTracking;
import org.apache.xmlrpc.webserver.ConnectionWithIpTracking;
import org.apache.xmlrpc.webserver.WebServer;

public class WebServerWithClientId extends WebServer
{
	public WebServerWithClientId(int pPort, InetAddress pAddr)
	{
		super(pPort, pAddr);
	}

	// The following methods allow a thread to know the client IP/port
	@Override
	protected XmlRpcStreamServer newXmlRpcStreamServer()
	{
		return new ConnectionServerWithIpTracking();
	}
	
	@Override
	protected Task newTask(WebServer pServer, XmlRpcStreamServer pXmlRpcServer, Socket pSocket) throws IOException
	{
		return new ConnectionWithIpTracking(pServer, pXmlRpcServer, pSocket);
	}

	// NOTE: The following method used to return the active thread count
	// from within the thread group for a given XMLRPC server
	public int getActiveRunnerCount()
	{
		log("NOTE: getActiveRunnerCount is no longer implemented");
		return 0;
	}
}
