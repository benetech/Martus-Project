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

package org.martus.amplifier.datasynch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.martus.amplifier.attachment.DataManager;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.network.AmplifierBulletinRetrieverGatewayInterface;
import org.martus.amplifier.network.AmplifierClientSideNetworkGateway;
import org.martus.amplifier.network.AmplifierClientSideXmlrpcHandler;
import org.martus.amplifier.search.BulletinIndexer;
import org.martus.common.AmplifierNetworkInterface;
import org.martus.common.ContactInfo;
import org.martus.common.LoggerInterface;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.network.MartusXmlrpcClient.SSLSocketSetupException;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.packet.UniversalId;
import org.martus.util.LoggerUtil;

public class AmplifierNetworkGateway implements LoggerInterface
{
	public AmplifierNetworkGateway(BackupServerInfo backupServerToCall, LoggerInterface loggerToUse, MartusCrypto securityToUse)
	{
		this(null, backupServerToCall, loggerToUse, securityToUse);
	}
	
	public AmplifierNetworkGateway(AmplifierBulletinRetrieverGatewayInterface gatewayToUse, 
				BackupServerInfo backupServerToCall,
				LoggerInterface loggerToUse, 
				MartusCrypto securityToUse)
	{
		super();
	
		serverToPullFrom = backupServerToCall;
		logger = loggerToUse;
		gateway = gatewayToUse;
		if(gateway == null)
			gateway = getCurrentNetworkInterfaceGateway();
			
		security = securityToUse;
	}
	
	public Vector getAllAccountIds()
	{
		class NotAuthorizedException extends Exception
		{
		}
		
		Vector result = new Vector();
		try
		{
			logNotice("getAllAccountIds");
			NetworkResponse response = gateway.getAccountIds(security);
			String resultCode = response.getResultCode();
			if(!resultCode.equals(NetworkInterfaceConstants.OK))
				throw new NotAuthorizedException();
			result= response.getResultVector();
			logDebug("getAllAccountIds: Exit");
		}
		catch(IOException e)
		{
			logError("getAllAccountIds(): No server available");
		}
		catch(NotAuthorizedException e)
		{
			logError("getAllAccountIds(): NOT AUTHORIZED");
		}
		catch(Exception e)
		{
			logError("getAllAccountIds(): " + e.getMessage());
			MartusLogger.logException(e);
		}
		return result;
	}

	public Vector getContactInfo(String accountId)
	{
		try
		{
			NetworkResponse response = gateway.getContactInfo(accountId, security);
			String resultCode = response.getResultCode();
			if(!resultCode.equals(NetworkInterfaceConstants.OK))
				return null;
			Vector encodedContactInfoResult = response.getResultVector();
			Vector decodedContactInfoResult = ContactInfo.decodeContactInfoVectorIfNecessary(encodedContactInfoResult);
			if(security.verifySignatureOfVectorOfStrings(decodedContactInfoResult, accountId))
				return decodedContactInfoResult;
		}
		catch (Exception e)
		{
			logError(e.toString());
		}
		return null;
	}	
	
	public Vector getAccountPublicBulletinLocalIds(String accountId)
	{
		Vector result = new Vector();
		try
		{
			NetworkResponse response = gateway.getPublicBulletinLocalIds(security, accountId);
			String resultCode = response.getResultCode();
			if( !resultCode.equals(NetworkInterfaceConstants.OK) )	
					throw new ServerErrorException(resultCode);
			result = response.getResultVector();		
		}	
		catch(Exception e)
		{
			String accountInfo = MartusCrypto.formatAccountIdForLog(accountId);
			logError("getAccountBulletinLocalIds(): " + e.getMessage() + ": " + accountInfo);
		}
		return result;
	}
	

	public void retrieveAndManageBulletin(
		UniversalId uid, BulletinExtractor bulletinExtractor, MartusAmplifier amp) 
		throws Exception
	{
		File bulletinFile = retrieveBulletin(uid);
		bulletinFile.deleteOnExit();
		if(amp.isShutdownRequested())
		{
			bulletinFile.delete();
			return;
		}
		amp.startSynch();
		try
		{
			bulletinExtractor.extractAndStoreBulletin(bulletinFile);
			bulletinFile.delete();
			
		}
		catch(DecryptionException e)
		{
			throw(e);
		}
		finally
		{
			amp.endSynch();
		}
	}
	
	
	public File retrieveBulletin(UniversalId uid)
	{
		File tempFile = null;
		FileOutputStream out = null;
		int chunkSize = AMPLIFIER_MAX_CHUNK_SIZE;
		int totalLength =0;
		try 
		{
			logInfo("getBulletin: " + MartusCrypto.getFormattedPublicCode(uid.getAccountId()) + 
								":" + uid.getLocalId());
			tempFile = File.createTempFile("$$$TempFile", null);
			tempFile.deleteOnExit();
        	out = new FileOutputStream(tempFile);		
		    try
		 	{	
				totalLength = BulletinZipUtilities.retrieveBulletinZipToStream
									(uid, out, chunkSize, gateway, security, null);
			}
			catch(Exception e)
			{
				logError("Unable to retrieve bulletin: " + e.getMessage());
			}
			finally
			{
				out.close();
			}
		}
		catch(Exception e)
		{
			logError(e.getMessage());	
		}

		if(tempFile.length() != totalLength)
		{
			System.out.println("file=" + tempFile.length() + ", returned=" + totalLength);
			logError("totalSize didn't match data length");
		}
		return tempFile;
	}
	
	public BulletinExtractor createBulletinExtractor(
		DataManager attachmentManager, BulletinIndexer indexer)
	{
		return new BulletinExtractor(attachmentManager, indexer, security);
	}
	
	
//methods copied/modified from MartusAPP	
	private AmplifierClientSideNetworkGateway getCurrentNetworkInterfaceGateway()
	{
		if(currentNetworkInterfaceGateway == null)
		{
			currentNetworkInterfaceGateway = new AmplifierClientSideNetworkGateway(getCurrentNetworkInterfaceHandler());
		}
		
		return currentNetworkInterfaceGateway;
	}
	
	private AmplifierNetworkInterface getCurrentNetworkInterfaceHandler()
	{
		if(currentNetworkInterfaceHandler == null)
		{
			currentNetworkInterfaceHandler = createXmlRpcNetworkInterfaceHandler();
		}

		return currentNetworkInterfaceHandler;
	}

	private AmplifierNetworkInterface createXmlRpcNetworkInterfaceHandler() 
	{
		BackupServerInfo serverInfo = serverToPullFrom;
		String ourServer = serverInfo.getName();
//		int ourPort = NetworkInterfaceXmlRpcConstants.MARTUS_PORT_FOR_SSL;
		int ourPort = serverInfo.getPort();
		try 
		{
			AmplifierClientSideXmlrpcHandler handler = new AmplifierClientSideXmlrpcHandler(ourServer, ourPort);
		//	handler.getSimpleX509TrustManager().setExpectedPublicKey(getContactInfo().getServerPublicKey());
		    handler.getSimpleX509TrustManager().setExpectedPublicKey(serverInfo.getServerPublicKey());
			return handler;
		} 
		catch (SSLSocketSetupException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private String createLogString(String message)
	{
		String serversPublicCodeWeAreCalling = MartusCrypto.formatAccountIdForLog(serverToPullFrom.getServerPublicKey());
		String serversIPAddressWeAreCalling = serverToPullFrom.getAddress();
		return "Amp calling " + serversIPAddressWeAreCalling + ":" + serverToPullFrom.getPort() + ": " + serversPublicCodeWeAreCalling +": " + message;
	}
	
	public void logError(String message)
	{
		logger.logError(createLogString(message));
	}

	public void logError(Exception e)
	{
		logError(LoggerUtil.getStackTrace(e));
	}

	public void logError(String message, Exception e)
	{
		logError(message);
		logError(e);
	}

	public void logNotice(String message)
	{
		logger.logNotice(createLogString(message));
	}

	public void logWarning(String message)
	{
		logger.logWarning(createLogString(message));
	}

	public void logInfo(String message)
	{
		logger.logInfo(createLogString(message));
	}

	public void logDebug(String message)
	{
		logger.logDebug(createLogString(message));
	}
	
	private static final int AMPLIFIER_MAX_CHUNK_SIZE = 1024 * 1024;
	
	private AmplifierBulletinRetrieverGatewayInterface gateway;
	private MartusCrypto security;
	private LoggerInterface logger;
	BackupServerInfo serverToPullFrom;
	private AmplifierNetworkInterface currentNetworkInterfaceHandler = null;
	private AmplifierClientSideNetworkGateway currentNetworkInterfaceGateway = null;
}
