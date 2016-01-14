/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2013, Beneficent
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
package org.apache.xmlrpc.webserver;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.ServerStreamConnection;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;

public class ConnectionServerWithIpTracking extends ConnectionServer
{
	@Override
	public void execute(XmlRpcStreamRequestConfig pConfig,
			ServerStreamConnection pConnection) throws XmlRpcException
	{
		if(pConnection instanceof ConnectionWithIpTracking)
		{
			ConnectionWithIpTracking connection = (ConnectionWithIpTracking)pConnection;
			setRemoteHostAndPort(connection);
		}
		super.execute(pConfig, pConnection);
	}

	private static void setRemoteHostAndPort(ConnectionWithIpTracking connection)
	{
		if(remoteHostAddressAndPort == null)
			remoteHostAddressAndPort = new ThreadLocal<String>();
		remoteHostAddressAndPort.set(connection.getRemoteHostAddress() + ":" + connection.getRemotePort());
	}
	
	public static String getRemoteHostAddressAndPort()
	{
		if(remoteHostAddressAndPort == null)
			return null;
		
		return remoteHostAddressAndPort.get();
	}
	
	private static ThreadLocal<String> remoteHostAddressAndPort;
}
