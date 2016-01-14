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

package org.martus.common.network.mirroring;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.network.SimpleHostnameVerifier;
import org.martus.common.network.SimpleX509TrustManager;

public class CallerSideMirroringGatewayForXmlRpc implements CallerSideMirroringInterface
{
	public static class SSLSocketSetupException extends Exception
	{
	}

	public CallerSideMirroringGatewayForXmlRpc(String serverName, int portToUse) throws SSLSocketSetupException
	{
		server = serverName;
		port = portToUse;
		serverUrl = "https://" + serverName + ":" + port + "/RPC2";
	}
	
	public void setExpectedPublicCode(String newExpectedPublicCode)
	{
		expectedPublicCode = newExpectedPublicCode; 
	}

	public void setExpectedPublicKey(String newExpectedPublicKey)
	{
		expectedPublicKey = newExpectedPublicKey; 
	}

	public Vector request(String callerAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(callerAccountId);
		params.add(parameters);
		params.add(signature);
		try
		{
			Object[] resultAsArray = (Object[]) callServer("request", params);
			return new Vector(Arrays.asList(resultAsArray));
		}
		catch (XmlRpcException e)
		{
			boolean wasConnectionRefused = e.getMessage().contains("Connection refused");
			boolean wasNoRouteToHost = e.getMessage().contains("No route to host");
			boolean isExpectedException = wasConnectionRefused || wasNoRouteToHost;
			if(!isExpectedException)
			{
				MartusLogger.log("Error calling " + serverUrl);
				MartusLogger.logException(e);
			}
		}
		catch (IOException e)
		{
			MartusLogger.log("Error calling " + serverUrl);
			MartusLogger.logException(e);
		} 
		catch (SSLSocketSetupException e)
		{
			MartusLogger.log("Error calling " + serverUrl);
			MartusLogger.logException(e);
		}
		return null;
	}
	
	Object callServer(String method, Vector params) throws 
		XmlRpcException, IOException, SSLSocketSetupException
	{
		try
		{
			SimpleX509TrustManager tm = new SimpleX509TrustManager();
			if(expectedPublicKey != null)
				tm.setExpectedPublicKey(expectedPublicKey);
			else if(expectedPublicCode != null)
				tm.setExpectedPublicCode(expectedPublicCode);
			HttpsURLConnection.setDefaultSSLSocketFactory(MartusUtilities.createSocketFactory(tm));
			HttpsURLConnection.setDefaultHostnameVerifier(new SimpleHostnameVerifier());
		}
		catch (Exception e)
		{
			throw new SSLSocketSetupException();
		}
		// NOTE: We **MUST** create a new XmlRpcClient for each call, because
		// there is a memory leak in apache xmlrpc 1.1 that will cause out of 
		// memory exceptions if we reuse an XmlRpcClient object
		XmlRpcClient xmlRpc = new XmlRpcClient();

		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(serverUrl));
		xmlRpc.setConfig(config);
		
		return xmlRpc.execute(MirroringInterface.DEST_OBJECT_NAME + "." + method, params);
	}

	String serverUrl;
	String server;
	int port;
	String expectedPublicKey;
	String expectedPublicCode;
}
