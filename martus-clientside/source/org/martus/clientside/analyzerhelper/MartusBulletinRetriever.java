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
package org.martus.clientside.analyzerhelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.CryptoInitializationException;
import org.martus.common.crypto.MartusCrypto.InvalidKeyPairFileVersionException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.NonSSLNetworkAPI;
import org.martus.common.network.NonSSLNetworkAPIWithHelpers;
import org.martus.common.network.TransportWrapper;
import org.martus.common.packet.UniversalId;
import org.martus.util.StreamableBase64.InvalidBase64Exception;


public class MartusBulletinRetriever
{
	public class ServerNotConfiguredException extends Exception
	{
	}
	
	public class ServerPublicCodeDoesNotMatchException extends Exception 
	{
	}
	
	public MartusBulletinRetriever(InputStream keyPair, char[] password) throws CryptoInitializationException, InvalidKeyPairFileVersionException, AuthorizationFailedException, IOException
	{
		this(keyPair, password, null);
	}
	
	public MartusBulletinRetriever(InputStream keyPair, char[] password, TransportWrapper transportToUse) throws CryptoInitializationException, InvalidKeyPairFileVersionException, AuthorizationFailedException, IOException
	{
		security = new MartusSecurity();
		security.readKeyPair(keyPair, password);
		transport = transportToUse;
	}
	
	public void initalizeServerForTesting(String serverIPAddress, String serverPublicKeyToUse, TransportWrapper transportToUse) throws Exception
	{
		transport = transportToUse;
		serverPublicKey = serverPublicKeyToUse;
		serverNonSSL = new ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer(serverIPAddress, transport);
		serverSLL = ClientSideNetworkGateway.buildGateway(serverIPAddress, serverPublicKeyToUse, transport);
	}

	public boolean isServerAvailable()  
	{
		if(serverPublicKey==null)
			return false;
		return ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer.isNonSSLServerAvailable(serverNonSSL);
	}

	public String getServerPublicKey(String serverIPAddress, String serverPublicCode) throws Exception
	{
		ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer newServerNonSSL = new ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer(serverIPAddress, transport);
		return getServerPublicKey(serverPublicCode, newServerNonSSL);
	}
	
	public List getFieldOfficeBulletinIds() throws ServerNotConfiguredException, MartusSignatureException, ServerErrorException
	{
		if(!isServerAvailable())
			return new ArrayList();
		List allBulletins = new ArrayList();
		Vector fieldOffices = serverSLL.downloadFieldOfficeAccountIds(security, security.getPublicKeyString());
		for(int a = 0; a < fieldOffices.size(); ++a)
		{
			String fieldOfficeAccountId = (String)fieldOffices.get(a);
			allBulletins.addAll(getBulletinIdsForFieldOffice(fieldOfficeAccountId));
		}
		return allBulletins;
	}
	
	private List getBulletinIdsForFieldOffice(String fieldOfficeAccountId) throws MartusSignatureException, ServerErrorException
	{
		List allBulletinsForFieldOffice = new ArrayList();
		Vector noTags = new Vector();
		NetworkResponse response = serverSLL.getSealedBulletinIds(security, fieldOfficeAccountId, noTags);
		allBulletinsForFieldOffice.addAll(getListOfBulletinUniversalIds(fieldOfficeAccountId, response));

		response = serverSLL.getDraftBulletinIds(security, fieldOfficeAccountId, noTags);
		allBulletinsForFieldOffice.addAll(getListOfBulletinUniversalIds(fieldOfficeAccountId, response));
		return allBulletinsForFieldOffice;
	}

	public MartusBulletinWrapper getBulletin(UniversalId uid, ProgressMeterInterface progressMeter) throws ServerErrorException
	{
		try
		{
			File bulletinZipFile = serverSLL.retrieveBulletin(uid, security, NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE, progressMeter);
			if(progressMeter != null && progressMeter.shouldExit())
				return null;
			MartusBulletinWrapper bulletin = new MartusBulletinWrapper(uid, bulletinZipFile, security);
			bulletinZipFile.delete();
			return bulletin;
		}
		catch(Exception e)
		{
			if(progressMeter != null && progressMeter.shouldExit())
				return null;
			throw new ServerErrorException(e.getMessage());
		}
	}
	
	Vector getListOfBulletinUniversalIds(String fieldOfficeAccountId, NetworkResponse response) throws ServerErrorException
	{
		if(!response.getResultCode().equals(NetworkInterfaceConstants.OK))
			throw new ServerErrorException();
		Vector result = response.getResultVector();
		Vector bulletinIds = new Vector();
		for(int i = 0; i < result.size(); ++i)
		{
			String summary = (String)result.get(i);
			String[] data = summary.split("=");
			bulletinIds.add(UniversalId.createFromAccountAndLocalId(fieldOfficeAccountId,data[0]));
		}
		return bulletinIds;
	}

	String getServerPublicKey(String serverPublicCode, NonSSLNetworkAPIWithHelpers serverNonSSLToUse) throws ServerNotAvailableException, ServerPublicCodeDoesNotMatchException, ServerErrorException
	{
		String ServerPublicKey;
		try
		{
			ServerPublicKey = serverNonSSLToUse.getServerPublicKey(security);
			String serverPublicCodeToTest = MartusSecurity.computePublicCode(ServerPublicKey);
			
			if(!MartusCrypto.removeNonDigits(serverPublicCode).equals(serverPublicCodeToTest))
				throw new ServerPublicCodeDoesNotMatchException();
			return ServerPublicKey;
		}
		catch(PublicInformationInvalidException e)
		{
			throw new ServerErrorException();
		}
		catch(InvalidBase64Exception e)
		{
			e.printStackTrace();
			throw new ServerErrorException();
		}
	}
	
	void setSSLServerToUse(ClientSideNetworkGateway sslServerToUse)
	{
		serverSLL = sslServerToUse;
	}

	public NonSSLNetworkAPI serverNonSSL;
	private ClientSideNetworkGateway serverSLL;
	private MartusSecurity security;
	private String serverPublicKey;
	private TransportWrapper transport;
}
