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

package org.martus.server.forclients;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

import org.martus.common.MartusAccountAccessToken;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.util.StreamableBase64;

public class MockServerForClients extends ServerForClients implements ServerForNonSSLClientsInterface
{
	public MockServerForClients(MockMartusServer coreServer)
	{
		super(coreServer);
	}
	
	public void verifyAndLoadConfigurationFiles() throws Exception
	{
		try
		{
			coreServer.verifyAndLoadConfigurationFiles();
		}
		catch (FileNotFoundException okIfComplianceFileIsMissing)
		{
		}
	}
	
	public String ping()
	{
		return "" + NetworkInterfaceConstants.VERSION;
	}
	
	public Vector getServerInformation()
	{
		if(infoResponse != null)
			return new Vector(infoResponse);
		return (Vector)(super.getServerInformation()).clone();
	}
	
	public String authenticateServer(String tokenToSign)
	{
		if(authenticateResponse != null)
			return new String(authenticateResponse);
		return "" + super.authenticateServer(tokenToSign);
	}
	
	public String requestUploadRights(String authorAccountId, String tryMagicWord)
	{
		return new String(super.requestUploadRights(authorAccountId, tryMagicWord));
	}

	public String uploadBulletin(String authorAccountId, String bulletinLocalId, String data)
	{
		lastClientId = authorAccountId;
		lastUploadedBulletinId = bulletinLocalId;				

		if(uploadResponse != null)
			return new String(uploadResponse);

		return "" + mockUploadBulletin(authorAccountId, bulletinLocalId, data);
	}
	
	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data, String signature) 
	{
		lastClientId = authorAccountId;
		lastUploadedBulletinId = bulletinLocalId;				

		if(uploadResponse != null)
			return new String(uploadResponse);
		return "" + coreServer.uploadBulletinChunk(authorAccountId, bulletinLocalId, totalSize, chunkOffset, chunkSize, data, signature);
	}

	public String putBulletinChunk(String uploaderAccountId, String authorAccountId, String bulletinLocalId,
			int totalSize, int chunkOffset, int chunkSize, String data) 
	{
		lastClientId = authorAccountId;
		lastUploadedBulletinId = bulletinLocalId;				

		if(uploadResponse != null)
			return new String(uploadResponse);

		return "" + super.putBulletinChunk(uploaderAccountId, authorAccountId, bulletinLocalId,
										totalSize, chunkOffset, chunkSize, data);
	}

	public Vector getBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId,
		int chunkOffset, int maxChunkSize) 
	{
		lastClientId = authorAccountId;

		if(downloadResponse != null)
			return new Vector(downloadResponse);

		return super.getBulletinChunk(myAccountId, authorAccountId, bulletinLocalId, 
			chunkOffset, maxChunkSize);
	}

	public Vector listMySealedBulletinIds(String clientId, Vector retrieveTags)
	{
		lastClientId = clientId;
		if(listMyResponseNull)	
			return null;
		if(listMyResponse != null)
			return new Vector(listMyResponse);
		return (Vector)(super.listMySealedBulletinIds(clientId, retrieveTags)).clone();
		
	}
	
	public Vector listMyDraftBulletinIds(String clientId, Vector retrieveTags)
	{
		lastClientId = clientId;
		if(listMyResponseNull)	
			return null;
		if(listMyResponse != null)
			return new Vector(listMyResponse);
		return (Vector)(super.listMyDraftBulletinIds(clientId, retrieveTags)).clone();
		
	}
	
	public Vector listFieldOfficeSealedBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
	{
		lastClientId = hqAccountId;
		if(listFieldOfficeSummariesResponseNull)	
			return null;
		if(listFieldOfficeSummariesResponse != null)
			return new Vector(listFieldOfficeSummariesResponse);
		return (Vector)(super.listFieldOfficeSealedBulletinIds(hqAccountId, authorAccountId, retrieveTags)).clone();
	}

	public Vector listFieldOfficeDraftBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
	{
		lastClientId = hqAccountId;
		if(listFieldOfficeSummariesResponseNull)	
			return null;
		if(listFieldOfficeSummariesResponse != null)
			return new Vector(listFieldOfficeSummariesResponse);
		return (Vector)(super.listFieldOfficeDraftBulletinIds(hqAccountId, authorAccountId, retrieveTags)).clone();
	}

	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		lastClientId = hqAccountId;
		if(listFieldOfficeAccountsResponseNull)	
			return null;
		if(listFieldOfficeAccountsResponse != null)
			return new Vector(listFieldOfficeAccountsResponse);
		return (Vector)(super.listFieldOfficeAccounts(hqAccountId)).clone();
	}

	public void setDownloadResponseNotFound()
	{
		downloadResponse = new Vector();
		downloadResponse.add(NetworkInterfaceConstants.ITEM_NOT_FOUND);
	}
	
	public void setDownloadResponseOk()
	{
		downloadResponse = new Vector();
		downloadResponse.add(NetworkInterfaceConstants.OK);
	}
	
	public void setDownloadResponseReal()
	{
		downloadResponse = null;
	}
	
	public void setAuthenticateResponse(String response)
	{
		authenticateResponse = response;	
	}
	
	public void nullListMyResponse(boolean nullResponse)
	{ 
		listMyResponseNull = nullResponse;
	}
	
	public void nullListFieldOfficeSummariesResponse(boolean nullResponse)
	{ 
		listFieldOfficeSummariesResponseNull = nullResponse;
	}
	
	public void nullListFieldOfficeAccountsResponse(boolean nullResponse)
	{ 
		listFieldOfficeAccountsResponseNull = nullResponse;
	}
	
	public Vector getNews(String accountId, String versionLabel, String versionBuildDate)
	{
		if(newsResponse != null)
		{	
			if(versionLabel.equals(newsVersionLabelToCheck) ||
			   versionBuildDate.equals(newsVersionBuildDateToCheck))
				return newsResponse;
		}
		return super.getNews(accountId, versionLabel, versionBuildDate);
	}
	
	public String getTokensFromMartusCentralTokenAuthority(String accountId)
	{
		return martusAccountAccessJsonTokenResponse;
	}	
	
	public void setAccessAccountJsonTokenResponse(String jsonTokenToUse)
	{
		martusAccountAccessJsonTokenResponse = jsonTokenToUse;
	}
	
	public Vector getMartusAccountAccessToken(String accountId)
	{
		if(martusAccountAccessTokenResponse != null)
		{
			return martusAccountAccessTokenResponse;
		}
		return super.getMartusAccountAccessToken(accountId);
	}
	
	
	public String getAccountIdForTokenFromMartusCentralTokenAuthority(MartusAccountAccessToken token) throws Exception
	{
		if(martusAccountAccessJsonTokenResponse != null)
		{
			return martusAccountAccessJsonTokenResponse;
		}
		return super.getAccountIdForTokenFromMartusCentralTokenAuthority(token);
	
	}
	
	public Vector getMartusAccountIdFromAccessToken(String accountId, MartusAccountAccessToken tokenToUse)
	{
		return super.getMartusAccountIdFromAccessToken(accountId, tokenToUse);
	}

	public Vector getServerCompliance()
	{
		if(complianceResponse != null)
		{	
				return complianceResponse;
		}
		return super.getServerCompliance();
	}	

	public Vector getPacket(
		String myAccountId,
		String authorAccountId,
		String bulletinLocalId,
		String packetLocalId)
	{
		if(countDownToGetPacketFailure == 1)
		{
			countDownToGetPacketFailure = 0;
			Vector result = new Vector();
			result.add(NetworkInterfaceConstants.SERVER_ERROR);		
			return result;
		}
		if (countDownToGetPacketFailure > 0)
			--countDownToGetPacketFailure;
		
		return super.getPacket(myAccountId, authorAccountId, bulletinLocalId, packetLocalId);
	}

	public void allowUploads(String clientId)
	{
		allowUploads(clientId, null);
	}

	public String mockUploadBulletin(String authorAccountId, String bulletinLocalId, String data)
	{
		if(!canClientUpload(authorAccountId))
		{
			logError("uploadBulletin REJECTED (!canClientUpload)");
			return NetworkInterfaceConstants.REJECTED;
		}
		
		File tempFile = null;
		try 
		{
			tempFile = StreamableBase64.decodeToTempFile(data);
		} 
		catch(Exception e)
		{
			//System.out.println("MartusServer.uploadBulletin: " + e);
			logError("uploadBulletin INVALID_DATA " + e);
			return NetworkInterfaceConstants.INVALID_DATA;
		}
		String result = coreServer.saveUploadedBulletinZipFile(authorAccountId, bulletinLocalId, tempFile);
		tempFile.delete();

		return result;
	}


	
	public int countDownToGetPacketFailure;
	public Vector newsResponse;
	public String newsVersionLabelToCheck;
	public String newsVersionBuildDateToCheck;
	public Vector martusAccountAccessTokenResponse;
	public String martusAccountAccessJsonTokenResponse;
	public Vector infoResponse;
	public String uploadResponse;
	public Vector downloadResponse;
	public Vector listMyResponse;
	public Vector listFieldOfficeSummariesResponse;
	public Vector listFieldOfficeAccountsResponse;
	public Vector complianceResponse;
	
	public String lastClientId;
	public String lastUploadedBulletinId;
	private boolean listMyResponseNull;
	private boolean listFieldOfficeSummariesResponseNull;
	private boolean listFieldOfficeAccountsResponseNull;
	
	private String authenticateResponse;
	
}
