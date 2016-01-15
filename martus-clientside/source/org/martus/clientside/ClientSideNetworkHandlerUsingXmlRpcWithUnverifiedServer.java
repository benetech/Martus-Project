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

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.X509TrustManager;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpc.SSLSocketSetupException;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkInterfaceXmlRpcConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.NonSSLNetworkAPI;
import org.martus.common.network.NonSSLNetworkAPIWithHelpers;
import org.martus.common.network.SimpleHostnameVerifier;
import org.martus.common.network.SimpleX509TrustManager;
import org.martus.common.network.TransportWrapper;

public class ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer extends NonSSLNetworkAPIWithHelpers implements NetworkInterfaceConstants, NetworkInterfaceXmlRpcConstants
	
{
	public ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer(String serverName, TransportWrapper transportToUse) throws Exception
	{
		this(serverName, NetworkInterfaceXmlRpcConstants.defaultSSLPorts, transportToUse);
	}
	
	public ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer(String serverName, int[] portsToUse) throws Exception
	{
		this(serverName, portsToUse, null);
	}

	public ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer(String serverName, int[] portsToUse, TransportWrapper transportToUse) throws Exception
	{
		server = serverName;
		ports = portsToUse;
		transport = transportToUse;

		try
		{
			ClientSideNetworkHandlerUsingXmlRpc.restrictCipherSuites();

			tm = new KeyCollectingX509TrustManager();
			HttpsURLConnection.setDefaultSSLSocketFactory(MartusUtilities.createSocketFactory(tm));
			HttpsURLConnection.setDefaultHostnameVerifier(new SimpleHostnameVerifier());
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			throw new SSLSocketSetupException();
		}
	}
	
	class KeyCollectingX509TrustManager implements X509TrustManager
	{
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException 
		{
			// WORKAROUND for a bug in Sun JSSE that shipped with 1.4.1_01 and earlier
			// where it would invoke this method instead of checkServerTrusted!
			checkServerTrusted(chain, authType);
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException 
		{
			if(chain.length != 3)
				throw new CertificateException("Need three certificates");
			
			// NOTE: cert2 is self-signed of and by the server Martus key
			X509Certificate cert2 = chain[2];
			serverPublicKey = cert2.getPublicKey();
		}

		public X509Certificate[] getAcceptedIssuers() 
		{
			return null;
		}
		
		public String getServerPublicKeyString() 
		{
			return SimpleX509TrustManager.getKeyString(serverPublicKey);
		}

		private PublicKey serverPublicKey;
	}

	// begin MartusXmlRpc interface
	public String ping()
	{
		Vector result = getServerInformation();
		if(result == null)
			return null;
		
		return (String)result.get(0);
	}

	public Vector getServerInformation()
	{
		logging("MartusServerProxyViaXmlRpc:getServerInformation");
		Vector params = new Vector();
		params.add(new Vector());
		Object[] resultArray = (Object[]) callServer(server, cmdGetServerInfo, params);
		Vector response = null;
		if(resultArray != null)
			response = new Vector(Arrays.asList(resultArray));
		NetworkResponse networkResponse = new NetworkResponse(response);
		if(!networkResponse.getResultCode().equals(OK))
			return null;
		
		Vector result = new Vector();
		String serverVersion = networkResponse.getResultVector().get(0);
		result.add(serverVersion);
		String publicKey = tm.getServerPublicKeyString();
		result.add(publicKey);
		
		return result;
	}

	// end MartusXmlRpc interface

	public Object callServer(String serverName, String method, Vector params)
	{
		int numPorts = ports.length;
		for(int i=0; i < numPorts; ++i)
		{
			int port = ports[indexOfPortThatWorkedLast];
			try
			{
				return callServerAtPort(serverName, method, params, port);
			}
			catch(ConnectException e)
			{
				indexOfPortThatWorkedLast = (indexOfPortThatWorkedLast+1)%numPorts;
				continue;
			}
			catch (IOException e)
			{
				if(e.getMessage().contains("Connection refused"))
					return null;

				if(e.getMessage().contains("RSA premaster"))
				{
					MartusLogger.log("Possible problem with RSA key size limitations");
					MartusLogger.logException(e);
					return null;
				}
				//TODO throw IOExceptions so caller can decide what to do.
				//This was added for connection refused: connect (no server connected)
				MartusLogger.logException(e);		
			}
			catch (XmlRpcException e)
			{
				String message = e.getMessage();
				if(message == null)
					message = "";
				boolean wasNoSuchMethodException = message.indexOf("NoSuchMethodException") >= 0;
				boolean wasTimeoutException = message.indexOf("Connection timed out") >= 0;
				boolean wasConnectionRefusedException = message.indexOf("Connection refused") >= 0;
				if(!wasNoSuchMethodException && !wasTimeoutException && !wasConnectionRefusedException)
				{
					MartusLogger.log("ClientSideNetworkHandlerUsingXmlRpcForNonSSL:callServer XmlRpcException=" + e);
					MartusLogger.logException(e);
				}
			}
			catch(Exception e)
			{
				logging("ClientSideNetworkHandlerUsingXmlRpcForNonSSL:callServer Exception=" + e);
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	public Object callServerAtPort(String serverName, String method, Vector params, int port) throws Exception
	{
		if(!transport.isOnline())
		{
			return new String[] { NetworkInterfaceConstants.TRANSPORT_OFFLINE };
		}

		try 
		{
			InetAddress address = InetAddress.getByName(serverName);
			if(address != null && address.isSiteLocalAddress() && getTransport().isTorEnabled())
			{
				MartusLogger.log("Orchid cannot reach local address: " + serverName);
				return null;
			}
		} 
		catch (UnknownHostException e) 
		{
			MartusLogger.logException(e);
			return null;
		}

		if(!transport.isReady())
		{
			MartusLogger.log("Warning: Orchid transport not ready for " + method);
			return new String[] { NetworkInterfaceConstants.TRANSPORT_NOT_READY };
		}

		if(ClientPortOverride.useInsecurePorts)
			port += 9000;
		
		final String serverUrl = "https://" + serverName + ":" + port + "/RPC2";
		MartusLogger.logVerbose("MartusServerProxyViaXmlRpc:callServer serverUrl=" + serverUrl);

		// NOTE: We **MUST** create a new XmlRpcClient for each call, because
		// there is a memory leak in apache xmlrpc 1.1 that will cause out of 
		// memory exceptions if we reuse an XmlRpcClient object
		XmlRpcClient client = new XmlRpcClient();
		XmlRpcTransportFactory transportFactory = transport.createTransport(client, tm);
		if(transportFactory != null)
			client.setTransportFactory(transportFactory);
		
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(serverUrl));
		client.setConfig(config);
		
		return client.execute("MartusServer." + method, params);
	}

	private void logging(String message)
	{
		Timestamp stamp = new Timestamp(System.currentTimeMillis());
		System.out.println(stamp + " " + message);
	}

	public static boolean isNonSSLServerAvailable(NonSSLNetworkAPI server)
	{
		String result = server.ping();
		if(result == null)
			return false;
	
		if(result.indexOf(MARTUS_SERVER_PING_RESPONSE) != 0)
			return false;
	
		return true;
	}

	protected KeyCollectingX509TrustManager getTm()
	{
		return tm;
	}

	protected TransportWrapper getTransport()
	{
		return transport;
	}

	public static final String MARTUS_SERVER_PING_RESPONSE = "MartusServer";

	String server;
	int[] ports;
	private TransportWrapper transport;
	static int indexOfPortThatWorkedLast = 0;
	boolean debugMode;
	private KeyCollectingX509TrustManager tm;
}
