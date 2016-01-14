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

package org.martus.common.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Vector;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.xmlrpc.WebServerWithSynchronousStartup;

public class MartusSecureWebServer extends WebServerWithSynchronousStartup
{
	public MartusSecureWebServer(int port) throws IOException
	{
		this(port, null);
	}

	public MartusSecureWebServer(int port, InetAddress internetAddress) throws IOException
	{
		super(port, internetAddress);
	}
	
	public ServerSocket createServerSocket(int port, int backlog, java.net.InetAddress add)
	{
		try
	    {
			SSLContext sslContext = createSSLContext();
			SSLServerSocketFactory sf = sslContext.getServerSocketFactory();

	    	SSLServerSocket ss = (SSLServerSocket)sf.createServerSocket( port, backlog, add);
	    	
	    	// NOTE: Existing clients expect "RSA", not "ECDH_RSA"
	    	String[] rawSupportedCipherSuites = ss.getSupportedCipherSuites();
	    	Vector<String> supportedCipherSuites = new Vector<String>(Arrays.asList(rawSupportedCipherSuites));
	    	ss.setEnabledCipherSuites(SSLUtilities.getAcceptableCipherSuites(supportedCipherSuites).toArray(new String[0]));
	    	return ss;
	    }
	    catch(Exception e)
	    {
	    	System.err.println("createServerSocket failed for " + add.toString() + ":" + port);
	    	System.err.flush();
	    	e.printStackTrace();
	    	throw(new RuntimeException(e));
	    }
	}
	
	SSLContext createSSLContext() throws Exception
	{
		SSLContext sslContext = SSLContext.getInstance( "TLS" );
		if(keyManagers == null)
			keyManagers = security.createKeyManagers();
		sslContext.init( keyManagers, null, null );
		return sslContext;
	}
	
	public static MartusCrypto security;
	public static KeyManager[] keyManagers;
}
