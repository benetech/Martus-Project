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
import java.io.IOException;
import java.util.Vector;

import org.martus.common.LoggerToNull;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.server.main.MartusServer;
import org.martus.util.StreamableBase64;

public class MockMartusServer extends MartusServer
{
	public MockMartusServer() throws Exception
	{
		this(new TempDirectory(), new MockServerDatabase());
	}
	
	public MockMartusServer(Database databaseToUse) throws Exception
	{
		this(new TempDirectory(), databaseToUse);
	}
	
	public MockMartusServer(File dataDir) throws Exception
	{
		this(dataDir, new MockServerDatabase());
	}
	
	public MockMartusServer(File dataDir, Database databaseToUse) throws Exception
	{
		super(dataDir, new LoggerToNull());
		getStore().setSignatureGenerator(MockMartusSecurity.createServer());
		initializeBulletinStore(databaseToUse);
	}
	
	public ServerForClients createServerForClients()
	{
		return new MockServerForClients(this);
	}
	
	public Database getWriteableDatabase()
	{
		return (Database)getDatabase();
	}

	public void setSecurity(MartusCrypto securityToUse)
	{
		getStore().setSignatureGenerator(securityToUse);
	}
	
	public void verifyAndLoadConfigurationFiles() throws Exception
	{
		try
		{
			super.verifyAndLoadConfigurationFiles();
		}
		catch (FileNotFoundException okIfComplianceFileIsMissing)
		{
		}
	}
	
	public String ping()
	{
		return "" + super.ping();
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
		return new String(super.serverForClients.requestUploadRights(authorAccountId, tryMagicWord));
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
		return "" + super.uploadBulletinChunk(authorAccountId, bulletinLocalId, totalSize, chunkOffset, chunkSize, data, signature);
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
	
	public void deleteAllData() throws Exception
	{
		getStore().deleteAllData();
		lastClientId = null;
		lastUploadedBulletinId = null;
	}
	
	public void nullListFieldOfficeAccountsResponse(boolean nullResponse)
	{ 
		listFieldOfficeAccountsResponseNull = nullResponse;
	}
	
	public void serverExit(int exitCode) throws UnexpectedExitException
	{
		throw new UnexpectedExitException();
	}
	
	public int getMaxFailedUploadAllowedAttemptsPerIp()
	{
		return 2;
	}
	
	public synchronized void subtractMaxFailedUploadAttemptsFromCounter()
	{
		return;
	}
	
	public synchronized void subtractMaxFailedUploadAttemptsFromServerCounter()
	{
		String clientIp = getCurrentClientIp();
		super.subtractMaxFailedUploadRequestsForIp(clientIp);
	}
	
	protected String getCurrentClientIp()
	{
		return CLIENT_IP_ADDRESS;
	}

	static class TempDirectory extends File
	{
		public TempDirectory() throws IOException
		{
			super(File.createTempFile("$$$MockMartusServer", null).getPath());
			deleteOnExit();
			delete();
			mkdir();
		}

	}
	
	public void deleteAllFiles() throws IOException
	{
		File allowUploadFile = serverForClients.getAllowUploadFile();
		allowUploadFile.delete();
		if(allowUploadFile.exists())
			throw new IOException("allowUploadFile");
		MartusServerUtilities.deleteSignaturesForFile(allowUploadFile);
		
		File authorizeLogFile = serverForClients.getAuthorizeLogFile();
		authorizeLogFile.delete();
		if(authorizeLogFile.exists())
			throw new IOException("authorizeLogFile");
		MartusServerUtilities.deleteSignaturesForFile(authorizeLogFile);
			
		File magicWordsFile = serverForClients.getMagicWordsFile();
		magicWordsFile.delete();
		if(magicWordsFile.exists())
			throw new IOException("magicWordsFile");
		getKeyPairFile().delete();
		if(getKeyPairFile().exists())
			throw new IOException("keyPairFile");
			
		File magicSig = MartusUtilities.getSignatureFileFromFile(magicWordsFile);
		if(magicSig.exists())
			magicSig.delete();

		File triggerDirectory = getTriggerDirectory();
		if(triggerDirectory.exists())
			triggerDirectory.delete();
			
		File startupDirectory = getStartupConfigDirectory();
		if(startupDirectory.exists())
			startupDirectory.delete();

		getDataDirectory().delete();
		if(getDataDirectory().exists())
			throw new IOException("dataDirectory: " + getDataDirectory().getPath());
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
			result.add(SERVER_ERROR);		
			return result;
		}
		if (countDownToGetPacketFailure > 0)
			--countDownToGetPacketFailure;
		
		return super.getPacket(myAccountId, authorAccountId, bulletinLocalId, packetLocalId);
	}

	public void allowUploads(String clientId)
	{
		serverForClients.allowUploads(clientId, null);
	}

	public boolean shouldSimulateBadConnection()
	{
		return false;
	}

	public String mockUploadBulletin(String authorAccountId, String bulletinLocalId, String data)
	{
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
		String result = saveUploadedBulletinZipFile(authorAccountId, bulletinLocalId, tempFile);
		tempFile.delete();

		return result;
	}


	
	public int countDownToGetPacketFailure;
	public Vector infoResponse;
	public String uploadResponse;
	public Vector downloadResponse;
	public Vector listFieldOfficeAccountsResponse;
	public Vector complianceResponse;
	
	public String lastClientId;
	public String lastUploadedBulletinId;
	private boolean listFieldOfficeAccountsResponseNull;
	
	private String authenticateResponse;
	
	public static final String CLIENT_IP_ADDRESS = "192.168.1.123";


}
