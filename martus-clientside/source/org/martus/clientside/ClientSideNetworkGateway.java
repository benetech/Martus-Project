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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;

import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.VersionBuildDate;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.network.BulletinRetrieverGatewayInterface;
import org.martus.common.network.ClientSideNetworkInterface;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkInterfaceXmlRpcConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.PartialUploadStatus;
import org.martus.common.network.SummaryOfAvailableBulletins;
import org.martus.common.network.TransportWrapper;
import org.martus.common.packet.UniversalId;
import org.miradi.utils.EnhancedJsonObject;

public class ClientSideNetworkGateway implements BulletinRetrieverGatewayInterface
{
	public ClientSideNetworkGateway(ClientSideNetworkInterface serverToUse)
	{
		server = serverToUse;
	}
	
	public ClientSideNetworkInterface getInterface()
	{
		return server;
	}

	public NetworkResponse getServerInfo()
	{
		Vector parameters = new Vector();
		Vector response = server.getServerInfo(parameters);
		return new NetworkResponse(response);
	}

	public NetworkResponse getUploadRights(MartusCrypto signer, String tryMagicWord) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(tryMagicWord);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getUploadRights(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse getSealedBulletinIds(MartusCrypto signer, String authorAccountId, Vector retrieveTags) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(retrieveTags);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getSealedBulletinIds(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse getDraftBulletinIds(MartusCrypto signer, String authorAccountId, Vector retrieveTags) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(retrieveTags);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getDraftBulletinIds(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse getFieldOfficeAccountIds(MartusCrypto signer, String hqAccountId) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(hqAccountId);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getFieldOfficeAccountIds(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse putBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId,
			int totalSize, int chunkOffset, int chunkSize, String data) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(new Integer(totalSize));
		parameters.add(new Integer(chunkOffset));
		parameters.add(new Integer(chunkSize));
		parameters.add(data);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.putBulletinChunk(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse getBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId,
					int chunkOffset, int maxChunkSize) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(new Integer(chunkOffset));
		parameters.add(new Integer(maxChunkSize));
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getBulletinChunk(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse getPacket(MartusCrypto signer, String authorAccountId, String bulletinLocalId,
					String packetLocalId) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(packetLocalId);
		parameters.add(NetworkInterfaceConstants.BASE_64_ENCODED);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getPacket(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse deleteServerDraftBulletins(MartusCrypto signer,
					String authorAccountId, String[] bulletinLocalIds) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(new Integer(bulletinLocalIds.length));
		for (int i = 0; i < bulletinLocalIds.length; i++)
		{
			parameters.add(bulletinLocalIds[i]);
		}
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.deleteDraftBulletins(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse	putContactInfo(MartusCrypto signer, String authorAccountId, Vector parameters) throws
			MartusCrypto.MartusSignatureException
	{
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.putContactInfo(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse	getNews(MartusCrypto signer, String versionLabel) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(versionLabel);
		parameters.add(VersionBuildDate.getVersionBuildDate());
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getNews(signer.getPublicKeyString(), parameters, signature));
	}
	
	public NetworkResponse getMartusAccountAccessToken(MartusCrypto signer) throws
	MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		String accountId = signer.getPublicKeyString();
		Vector rawResult = server.getMartusAccountAccessToken(accountId, parameters, signature);
		return new NetworkResponse(rawResult);
	}

	public NetworkResponse getMartusAccountIdFromAccessToken(MartusCrypto signer, MartusAccountAccessToken tokenToUse) throws
	MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(tokenToUse.getToken());
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getMartusAccountIdFromAccessToken(signer.getPublicKeyString(), parameters, signature));
	}
	
	public NetworkResponse getListOfFormTemplates(MartusCrypto signer, String accountToGetListOfFormTemplatesFrom) throws
	MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(accountToGetListOfFormTemplatesFrom);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getListOfFormTemplates(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse putFormTemplate(MartusCrypto signer, String formTemplate) throws
	MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(formTemplate);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.putFormTemplate(signer.getPublicKeyString(), parameters, signature));
	}
	
	public NetworkResponse getFormTemplate(MartusCrypto signer, String accountToGetFormTemplateFrom, String formTitle) throws
	MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(accountToGetFormTemplateFrom);
		parameters.add(formTitle);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getFormTemplate(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse listAvailableRevisionsSince(MartusCrypto signer, String earliestTimestamp) throws
	MartusCrypto.MartusSignatureException
	{
		EnhancedJsonObject json = new EnhancedJsonObject();
		json.put(SummaryOfAvailableBulletins.JSON_KEY_EARLIEST_SERVER_TIMESTAMP, earliestTimestamp);
		
		Vector parameters = new Vector();
		parameters.add(json.toString());
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.listAvailableRevisionsSince(signer.getPublicKeyString(), parameters, signature));
	}

	public NetworkResponse	getServerCompliance(MartusCrypto signer) throws
			MartusCrypto.MartusSignatureException
	{
		Vector parameters = new Vector();
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getServerCompliance(signer.getPublicKeyString(), parameters, signature));
	}
	
	private NetworkResponse getPartialUploadStatus(MartusCrypto signer, String uploaderId, String bulletinLocalId, Vector extraParameters) throws MartusSignatureException 
	{
		Vector parameters = new Vector();
		parameters.add(uploaderId);
		parameters.add(bulletinLocalId);
		parameters.add(extraParameters);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getPartialUploadStatus(signer.getPublicKeyString(), parameters, signature));
	}


	
	
	static public ClientSideNetworkGateway buildGateway(String serverName, String serverPublicKey, TransportWrapper transportToUse)
	{
		ClientSideNetworkInterface server = buildNetworkInterface(serverName, serverPublicKey, transportToUse);
		if(server == null)
			return null;
		
		return new ClientSideNetworkGateway(server);
	}

	public static ClientSideNetworkInterface buildNetworkInterface(String serverName, String serverPublicKey, TransportWrapper transport)
	{
		if(serverName.length() == 0)
			return null;
	
		try
		{
			int[] ports = NetworkInterfaceXmlRpcConstants.defaultSSLPorts;
			ClientSideNetworkHandlerUsingXmlRpc handler = new ClientSideNetworkHandlerUsingXmlRpc(serverName, ports, transport);
			handler.getSimpleX509TrustManager().setExpectedPublicKey(serverPublicKey);
			return handler;
		}
		catch (ClientSideNetworkHandlerUsingXmlRpc.SSLSocketSetupException e)
		{
			//TODO propagate to UI and needs a test.
			e.printStackTrace();
			return null;
		}
	}

	public Vector downloadFieldOfficeAccountIds(MartusCrypto security, String myAccountId) throws ServerErrorException
	{
		try
		{
			NetworkResponse response = getFieldOfficeAccountIds(security, myAccountId);
			String resultCode = response.getResultCode();
			if(!resultCode.equals(NetworkInterfaceConstants.OK))
				throw new ServerErrorException(resultCode);
			return response.getResultVector();
		}
		catch(MartusCrypto.MartusSignatureException e)
		{
			System.out.println("ServerUtilities.getFieldOfficeAccounts: " + e);
			throw new ServerErrorException();
		}
	}

	public File retrieveBulletin(UniversalId uid, MartusCrypto security,
			int chunkSize, ProgressMeterInterface progressMeter)
			throws Exception
	{
		File tempFile = File.createTempFile("$$$MartusRetrievedBulletin", null);
		tempFile.deleteOnExit();
		FileOutputStream outputStream = new FileOutputStream(tempFile);

		int masterTotalSize = BulletinZipUtilities.retrieveBulletinZipToStream(
				uid, outputStream, chunkSize, this, security, progressMeter);
		outputStream.close();
		if (tempFile.length() != masterTotalSize)
			throw new ServerErrorException(
					"bulletin totalSize didn't match data length");
		return tempFile;
	}

	public PartialUploadStatus getPartialUploadStatus(MartusCrypto security, String uploaderId, String bulletinLocalId) throws ServerErrorException 
	{
		try
		{
			NetworkResponse response = getPartialUploadStatus(security, uploaderId, bulletinLocalId, new Vector());
			String resultCode = response.getResultCode();
			if(!resultCode.equals(NetworkInterfaceConstants.OK))
				throw new ServerErrorException(resultCode);
			Vector result = response.getResultVector();
			long length = new Long((String)result.get(0));
			String sha256 = (String) result.get(1);
			PartialUploadStatus status = new PartialUploadStatus(length, sha256);
			return status;
		}
		catch(MartusCrypto.MartusSignatureException e)
		{
			System.out.println("ServerUtilities.getFieldOfficeAccounts: " + e);
			throw new ServerErrorException();
		}
	}

	public int getOffsetToStartUploading(UniversalId uid,	File tempFile, MartusCrypto security)
	{
		String authorId = uid.getAccountId();
		String bulletinLocalId = uid.getLocalId();
		try
		{
			PartialUploadStatus status = getPartialUploadStatus(security, authorId, bulletinLocalId);
			if(!status.hasPartialUpload())
				return 0;
			
			if(status.lengthOfPartialUpload() > Integer.MAX_VALUE)
				return 0;
			
			int partialLength = (int)status.lengthOfPartialUpload();
			MartusLogger.log("Partial upload found, length=" + partialLength);
			String sha1Base64OnServer = status.sha1OfPartialUpload();
			String sha1Base64Here = MartusCrypto.getPartialDigest(tempFile, partialLength);
			if(sha1Base64Here.equals(sha1Base64OnServer))
				return partialLength;
			
			MartusLogger.log("Partial upload mismatch; will upload from scratch");
			return 0;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return 0;
		}
	}

	final static String defaultReservedString = "";

	ClientSideNetworkInterface server;
}
