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
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.network.ClientSideNetworkInterface;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkInterfaceXmlRpcConstants;
import org.martus.common.network.SSLUtilities;
import org.martus.common.network.SimpleHostnameVerifier;
import org.martus.common.network.SimpleX509TrustManager;
import org.martus.common.network.TransportWrapper;
import org.martus.util.Stopwatch;

public class ClientSideNetworkHandlerUsingXmlRpc
	implements NetworkInterfaceConstants, NetworkInterfaceXmlRpcConstants, ClientSideNetworkInterface
{

	public static class SSLSocketSetupException extends Exception 
	{
		public SSLSocketSetupException()
		{
		}
		
		public SSLSocketSetupException(String message)
		{
			super(message);
		}
	}

	public ClientSideNetworkHandlerUsingXmlRpc(String serverIpAddressToUse, int[] portsToUse) throws SSLSocketSetupException
	{
		this(serverIpAddressToUse, portsToUse, null);
	}
	
	public ClientSideNetworkHandlerUsingXmlRpc(String serverIpAddressToUse, int[] portsToUse, TransportWrapper transportToUse) throws SSLSocketSetupException
	{
		if(!isServerAllowed(serverIpAddressToUse))
		{
			String errorText = "Blocking attempt to connect to: " + serverIpAddressToUse;
			MartusLogger.log(errorText);
			throw new SSLSocketSetupException(errorText);
		}
		serverIpAddress = serverIpAddressToUse;
		ports = portsToUse;
		transport = transportToUse;

		timeoutSecondsForGetServerInfo = DEFAULT_GET_SERVER_INFO_TIMEOUT_SECONDS;
		
		RESULT_NO_SERVER = new Vector();
		RESULT_NO_SERVER.add(NetworkInterfaceConstants.NO_SERVER);
		
		try
		{
			restrictCipherSuites();

			tm = new SimpleX509TrustManager();
			HttpsURLConnection.setDefaultSSLSocketFactory(MartusUtilities.createSocketFactory(tm));
			HttpsURLConnection.setDefaultHostnameVerifier(new SimpleHostnameVerifier());
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			throw new SSLSocketSetupException();
		}
	}
	
	public void setTimeoutGetServerInfo(int newTimeoutSeconds) 
	{
		MartusLogger.log("Setting getServerInfo timeout to " + newTimeoutSeconds + " seconds");
		timeoutSecondsForGetServerInfo = newTimeoutSeconds;
	}

	@Override
	public int getTimeoutSecondsForGetServerInfo() 
	{
		return timeoutSecondsForGetServerInfo * ports.length;
	}

	@Override
	public int getTimeoutSecondsForOtherCalls() 
	{
		// NOTE: Currently, we rely on default timeouts. We should probably 
		// use CallerWithTimeout for all calls, with timeouts that we set.
		return 30 * ports.length;
	}
	
	@Override
	public String getServerIpAddress()
	{
		return serverIpAddress;
	}
	
	public static void restrictCipherSuites() throws NoSuchAlgorithmException 
	{
		SSLSocketFactory socketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		String[] rawCipherSuites = socketFactory.getDefaultCipherSuites();
		Vector<String> supportedCipherSuites = new Vector<String>(Arrays.asList(rawCipherSuites));
		Vector<String> goodCipherSuites = SSLUtilities.getAcceptableCipherSuites(supportedCipherSuites);
		String goodCipherSuitesAsString = "";
		for (String cipher : goodCipherSuites) 
		{
			if(goodCipherSuitesAsString.length() > 0)
				goodCipherSuitesAsString += ",";
			goodCipherSuitesAsString += cipher;
		}
		MartusLogger.log("Limiting SSL cipher suites to: " + goodCipherSuitesAsString);
		// NOTE: This seems ugly, but there doesn't seem to be any cleaner way
		System.setProperty("https.cipherSuites", goodCipherSuitesAsString);
	}

	// begin ServerInterface
	public Vector getServerInfo(Vector reservedForFuture)
	{
		Vector params = new Vector();
		params.add(reservedForFuture);
		Caller caller = new CallerWithTimeout(cmdGetServerInfo, params, timeoutSecondsForGetServerInfo);
		return (Vector)callServer(serverIpAddress, caller);
	}

	public Vector getUploadRights(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetUploadRights, params);
	}

	public Vector getSealedBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetSealedBulletinIds, params);
	}

	public Vector getDraftBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetDraftBulletinIds, params);
	}

	public Vector getFieldOfficeAccountIds(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetFieldOfficeAccountIds, params);
	}

	public Vector putBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdPutBulletinChunk, params);
	}

	public Vector getBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetBulletinChunk, params);
	}

	public Vector getPacket(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetPacket, params);
	}

	public Vector deleteDraftBulletins(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdDeleteDrafts, params);
	}

	public Vector putContactInfo(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdPutContactInfo, params);
	}

	public Vector getNews(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetNews, params);
	}

	public Vector getMartusAccountAccessToken(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetMartusAccountAccessToken, params);
	}
	
	public Vector getMartusAccountIdFromAccessToken(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetMartusAccountIdFromAccessToken, params);
	}

	public Vector getServerCompliance(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetServerCompliance, params);
	}

	public Vector getPartialUploadStatus(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetPartialUploadStatus, params);
	}

	public Vector getListOfFormTemplates(String myAccountId, Vector parameters, String signature) 
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetListOfFormTemplates, params);
	}

	public Vector putFormTemplate(String myAccountId, Vector parameters, String signature) 
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdPutFormTemplate, params);
	}

	public Vector getFormTemplate(String myAccountId, Vector parameters, String signature) 
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdGetFormTemplate, params);
	}

	@Override
	public Vector listAvailableRevisionsSince(String myAccountId, Vector parameters, String signature)
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(serverIpAddress, cmdListAvailableRevisionsSince, params);
	}

	public Object callServer(String serverName, String method, Vector params)
	{
		Caller caller = new Caller(method, params);
		return callServer(serverName, caller);
	}
	
	private Object callServer(String serverName, Caller caller)
	{
		try 
		{
			InetAddress address = InetAddress.getByName(serverName);
			if(address != null && address.isSiteLocalAddress() && getTransport() != null && getTransport().isTorEnabled())
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

		int numPorts = ports.length;
		int portIndexToTryNext = indexOfPortThatWorkedLast;
		for(int i=0; i < numPorts; ++i)
		{
			int port = ports[portIndexToTryNext];
			if(ClientPortOverride.useInsecurePorts)
				port += 9000;
			
			Vector result = caller.call(this, serverName, port);
			
			if(result == null || !result.equals(RESULT_NO_SERVER))
			{
				indexOfPortThatWorkedLast = portIndexToTryNext;
				return result;
			}

			portIndexToTryNext = (portIndexToTryNext+1) % numPorts;
		}
		return null;
	}
	
	static class Caller
	{
		public Caller(String methodToCall, Vector paramsToUse)
		{
			method = methodToCall;
			params = paramsToUse;
		}
		
		public Vector call(ClientSideNetworkHandlerUsingXmlRpc handler, String serverName, int port)
		{
			return internalCall(handler, serverName, port);
		}

		Vector internalCall(ClientSideNetworkHandlerUsingXmlRpc handler, String serverName, int port)
		{
			return handler.callServerAtPort(serverName, method, params, port);
		}
		
		String method;
		Vector params;
	}
	
	static class CallerWithTimeout extends Caller
	{
		public CallerWithTimeout(String methodToCall, Vector paramsToUse, long secondsToWait)
		{
			super(methodToCall, paramsToUse);
			timeoutMillis = secondsToWait * 1000;
		}
		
		public Vector call(ClientSideNetworkHandlerUsingXmlRpc handler, String serverName, int port)
		{
			BackgroundCallerTask task = new BackgroundCallerTask(this, handler, serverName, port);
			Thread background = new Thread(task);
			try
			{
				background.start();
				background.join(timeoutMillis);
			}
			catch (InterruptedException e)
			{
				System.out.println("ERROR: Server timeout after " + timeoutMillis/1000 + " seconds");
			}
			background.interrupt();
			return task.response;
		}
		
		long timeoutMillis;
	}
	
	static class BackgroundCallerTask implements Runnable
	{
		BackgroundCallerTask(Caller callerTouse, ClientSideNetworkHandlerUsingXmlRpc handlerToUse, String serverNameToUse, int portToUse)
		{
			caller = callerTouse;
			handler = handlerToUse;
			serverName = serverNameToUse;
			port = portToUse;
			response = RESULT_NO_SERVER;
		}
			
		public void run()
		{
			response = caller.internalCall(handler, serverName, port);
		}

		Caller caller;
		ClientSideNetworkHandlerUsingXmlRpc handler;
		String serverName;
		int port;
		Vector response;
	}
	
	Vector callServerAtPort(
		String serverName,
		String method,
		Vector params,
		int port)
	{
		try
		{
			Object[] result = (Object[])executeXmlRpc(serverName, method, params, port);
			if(tm.getExpectedPublicKey() == null)
				throw new Exception("Trust Manager never called");
			if(result == null)
				return null;
			
			return new Vector(Arrays.asList(result));
		}
		catch (IOException e)
		{
			if(e.getMessage().contains("Connection refused"))
				return RESULT_NO_SERVER;

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
			if(message.startsWith("No such handler:"))
			{
				Vector result = new Vector();
				result.add(NetworkInterfaceConstants.SERVER_NOT_COMPATIBLE);
				return result;
			}
			
			if(e.getMessage().contains("Network is unreachable"))
				return RESULT_NO_SERVER;

			boolean wasNoSuchMethodException = message.indexOf("NoSuchMethodException") >= 0;
			boolean wasTimeoutException = message.indexOf("Connection timed out") >= 0;
			boolean wasConnectionRefusedException = message.indexOf("Connection refused") >= 0;
			if(!wasNoSuchMethodException && !wasTimeoutException && !wasConnectionRefusedException)
			{
				MartusLogger.log("ClientSideNetworkHandlerUsingXmlRpc:callServer XmlRpcException=" + e);
				MartusLogger.logException(e);
			}
		}
		catch (Exception e)
		{
			System.out.println("ClientSideNetworkHandlerUsingXmlRpc:callServer Exception=" + e);
			e.printStackTrace();
		}
		return null;
	}
	
	public Object executeXmlRpc(String serverName, String method, Vector params, int port) throws Exception 
	{
		if(!transport.isOnline())
		{
			return new String[] { NetworkInterfaceConstants.TRANSPORT_OFFLINE };
		}

		if(!transport.isReady())
		{
			MartusLogger.log("Warning: Orchid transport not ready for " + method);
			return new String[] { NetworkInterfaceConstants.TRANSPORT_NOT_READY };
		}
		
		final String serverUrl = "https://" + serverName + ":" + port + "/RPC2";
		MartusLogger.logVerbose("ServerInterfaceXmlRpcHandler:callServer serverUrl=" + serverUrl);
		
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
		
		Stopwatch sw = new Stopwatch();
		Object result = client.execute("MartusServer." + method, params);
		sw.stop();
		final int MAX_EXPECTED_TIME_MILLIS = 60 * 1000;
		if(sw.elapsed() > MAX_EXPECTED_TIME_MILLIS)
			System.out.println("SLOW SERVER: " + new Date() + " " + serverUrl + " " + method + " " + sw.elapsed()/1000 + " seconds");
		return result;
	}

	public SimpleX509TrustManager getSimpleX509TrustManager()
	{
		return tm;
	}

	protected TransportWrapper getTransport()
	{
		return transport;
	}

	public static boolean isServerAllowed(String serverName) 
	{
		if(allowedServers == null)
		{
			 final String IP_SEGMENT = "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
			 return serverName.matches( "\\b" + IP_SEGMENT + "\\." +
					 IP_SEGMENT + "\\." + IP_SEGMENT + "\\." + IP_SEGMENT + "\\b");
		}
		return allowedServers.contains(serverName);
	}
	
	public static void addAllowedServer(String serverName)
	{
		if(allowedServers == null)
			allowedServers = new HashSet<String>();
		
		allowedServers.add(serverName);
	}
	
	private static HashSet<String> allowedServers;

	static int indexOfPortThatWorkedLast = 0;
	private SimpleX509TrustManager tm;
	private String serverIpAddress;
	private int[] ports;
	private int timeoutSecondsForGetServerInfo;
	private TransportWrapper transport;
	
	static Vector RESULT_NO_SERVER;

	public static final int DEFAULT_GET_SERVER_INFO_TIMEOUT_SECONDS = 15;
	public static final int WITHOUT_TOR_GET_SERVER_INFO_TIMEOUT_SECONDS = 15;
	public static final int TOR_GET_SERVER_INFO_TIMEOUT_SECONDS = 60;
}
