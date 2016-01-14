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

package org.martus.server.foramplifiers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import org.martus.amplifier.ServerCallbackInterface;
import org.martus.common.AmplifierNetworkInterface;
import org.martus.common.LoggerInterface;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileTooLargeException;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.MartusUtilities.InvalidPublicKeyFileException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.SupplierSideAmplifierNetworkInterface;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.network.MartusSecureWebServer;
import org.martus.common.network.MartusXmlRpcServer;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.common.packet.UniversalId;
import org.martus.common.xmlrpc.XmlRpcThread;
import org.martus.server.main.MartusServer;
import org.martus.server.main.ServerBulletinStore;
import org.martus.util.DirectoryUtils;
import org.martus.util.LoggerUtil;
import org.martus.util.StreamableBase64;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

public class ServerForAmplifiers implements NetworkInterfaceConstants, LoggerInterface
{
	public ServerForAmplifiers(MartusServer coreServerToUse, LoggerInterface loggerToUse) throws MartusCrypto.CryptoInitializationException
	{
		coreServer = coreServerToUse;
		logger = loggerToUse;
		
		amplifierHandler = new ServerSideAmplifierHandler(this);
	}
	
	public Vector getDeleteOnStartupFiles()
	{
		Vector startupFiles = new Vector();
		startupFiles.add(getClientsNotToAmplifiyFile());
		startupFiles.add(getAmplifyMirroredBulletinsForTestingFile());
		return startupFiles;
	}

	public Vector getDeleteOnStartupFolders()
	{
		Vector startupFolders = new Vector();
		startupFolders.add(getAuthorizedAmplifiersDirectory());
		return startupFolders;
	}
	
	public void deleteStartupFiles()
	{
		DirectoryUtils.deleteEntireDirectoryTree(getDeleteOnStartupFolders());
		MartusUtilities.deleteAllFiles(getDeleteOnStartupFiles());
	}
	

	private String createLogString(String message)
	{
		return "ServerForAmp " + message;
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

	public void logInfo(String message)
	{
		logger.logInfo(createLogString(message));
	}

	public void logNotice(String message)
	{
		logger.logNotice(createLogString(message));
	}
	
	public void logWarning(String message)
	{
		logger.logWarning(createLogString(message));
	}

	public void logDebug(String message)
	{
		logger.logDebug(createLogString(message));
	}
	
	
	public ServerBulletinStore getStore()
	{
		return coreServer.getStore();
	}
	
	public ReadableDatabase getDatabase()
	{
		return coreServer.getDatabase();
	}
	
	SupplierSideAmplifierNetworkInterface getAmplifierHandler()
	{
		return amplifierHandler;
	}

	public boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return false;
	}
	
	public String getAccountId()
	{
		return getSecurity().getPublicKeyString();
	}
	
	public void verifyConfigurationFiles()
	{
		// nothing to do yet
	}
	
	public void loadConfigurationFiles() throws IOException, InvalidPublicKeyFileException, PublicInformationInvalidException
	{
		File authorizedAmplifiersDir = getAuthorizedAmplifiersDirectory();
		authorizedAmps = coreServer.loadServerPublicKeys(authorizedAmplifiersDir, "Amp");
		logNotice("Authorized " + authorizedAmps.size() + " amplifiers to call us");
		loadClientsNotAmplified();
		logNotice("Not authorized to amplify " + clientsNotAmplified.size() + " clients.");
		if(getAmplifyMirroredBulletinsForTestingFile().exists())
		{
			logWarning("AMPLIFYING MIRRORED BULLETINS FOR TEST.");
			amplifyMirroredBulletins = true;
		}
	}
	
	private File getClientsNotToAmplifiyFile()
	{
		return new File(coreServer.getStartupConfigDirectory(), CLIENTS_NOT_TO_AMPLIFY_FILENAME);
	}
	
	private File getAmplifyMirroredBulletinsForTestingFile()
	{
		return new File(coreServer.getStartupConfigDirectory(), "amplify_mirrored_bulletins_for_testing.txt");
	}
	
	public File getAuthorizedAmplifiersDirectory()
	{
		return new File(coreServer.getStartupConfigDirectory(), "ampsWhoCallUs");
	}
	
	public boolean canAccountBeAmplified(String accountId)
	{
		return(!clientsNotAmplified.contains(accountId));
	}
	
	public synchronized void loadClientsNotAmplified()
	{
		loadClientsNotAmplified(getClientsNotToAmplifiyFile());
	}
	
	public void loadClientsNotAmplified(File clientsNotToBeAmplifiedFile)
	{
		clientsNotAmplified = MartusUtilities.loadClientListAndExitOnError(clientsNotToBeAmplifiedFile);
	}	
	
	public void addListeners() throws UnknownHostException
	{
		logNotice("Initializing ServerForAmplifiers");
		createAmplifierXmlRpcServer();
		logNotice("Amplifier ports opened");
	}
	
	public void createAmplifierXmlRpcServer() throws UnknownHostException
	{
		int port = AmplifierInterfaceXmlRpcConstants.MARTUS_PORT_FOR_AMPLIFIER;
		if(!coreServer.isSecureMode())
			port += ServerCallbackInterface.DEVELOPMENT_MODE_PORT_DELTA;
		createAmplifierXmlRpcServerOnPort(port);
	}

	public void createAmplifierXmlRpcServerOnPort(int port) throws UnknownHostException
	{
		if(MartusSecureWebServer.security == null)
			MartusSecureWebServer.security = getSecurity();

		InetAddress mainIpAddress = MartusServer.getMainIpAddress();
		logNotice("Opening port "+ mainIpAddress + ":" + port + " for amplifiers...");
		SupplierSideAmplifierNetworkInterface handler = getAmplifierHandler();
		MartusXmlRpcServer.createSSLXmlRpcServer(handler, AmplifierNetworkInterface.class, "MartusAmplifierServer", port, mainIpAddress);
	}

	public Vector getServerInformation()
	{
		logInfo("getServerInformation");
			
		if( coreServer.isShutdownRequested() )
			return returnSingleResponseErrorAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
				
		Vector result = new Vector();
		try
		{
			String publicKeyString = getSecurity().getPublicKeyString();
			byte[] publicKeyBytes = StreamableBase64.decode(publicKeyString);
			ByteArrayInputStream in = new ByteArrayInputStream(publicKeyBytes);
			byte[] sigBytes = getSecurity().createSignatureOfStream(in);
			
			result.add(NetworkInterfaceConstants.OK);
			result.add(publicKeyString);
			result.add(StreamableBase64.encode(sigBytes));
			logNotice("getServerInformation : Exit OK");
		}
		catch(Exception e)
		{
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
			result.add(e.toString());
			logError("getServerInformation SERVER_ERROR", e);			
		}
		return result;
	}


	public Vector getBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId,
		int chunkOffset, int maxChunkSize) 
	{
		{
			StringBuffer logMsg = new StringBuffer();
			logMsg.append("getBulletinChunk  " + coreServer.getClientAliasForLogging(authorAccountId) + " " + bulletinLocalId);
			logMsg.append("  Offset=" + chunkOffset + ", Max=" + maxChunkSize);
			logDebug(logMsg.toString());
		}
		
		if( coreServer.isShutdownRequested() )
			return returnSingleResponseErrorAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );

		if(!isAuthorizedAmp(myAccountId))
			return returnSingleResponseErrorAndLog(" returning NOT_AUTHORIZED", NetworkInterfaceConstants.NOT_AUTHORIZED);

		DatabaseKey headerKey =	findHeaderKeyInDatabase(authorAccountId, bulletinLocalId);
		if(headerKey == null)
			return returnSingleResponseErrorAndLog( " returning NOT_FOUND", NetworkInterfaceConstants.ITEM_NOT_FOUND );

		Vector result = getBulletinChunkWithoutVerifyingCaller(
					authorAccountId, bulletinLocalId,
					chunkOffset, maxChunkSize);
		
		logDebug("getBulletinChunk exit: " + result.get(0));
		return result;
	}

	boolean isAuthorizedAmp(String myAccountId)
	{
		return authorizedAmps.contains(myAccountId);
	}

	public String authenticateServer(String tokenToSign)
	{
		logInfo("authenticateServer");
		try 
		{
			InputStream in = new ByteArrayInputStream(StreamableBase64.decode(tokenToSign));
			byte[] sig = getSecurity().createSignatureOfStream(in);
			return StreamableBase64.encode(sig);
		} 
		catch(MartusSignatureException e) 
		{
			logError("SERVER_ERROR", e);
			return NetworkInterfaceConstants.SERVER_ERROR;
		} 
		catch(InvalidBase64Exception e) 
		{
			logError("INVALID_DATA", e);
			return NetworkInterfaceConstants.INVALID_DATA;
		}
	}
	
	// end MartusServerInterface interface

	public String getPublicCode(String clientId) 
	{
		String formattedCode = "";
		try 
		{
			formattedCode = MartusCrypto.computeFormattedPublicCode(clientId);
		} 
		catch(InvalidBase64Exception e) 
		{
		}
		return formattedCode;
	}

	public static boolean keyBelongsToClient(DatabaseKey key, String clientId)
	{
		return clientId.equals(key.getAccountId());
	}

	void readKeyPair(InputStream in, char[] passphrase) throws 
		IOException,
		MartusCrypto.AuthorizationFailedException,
		MartusCrypto.InvalidKeyPairFileVersionException
	{
		getSecurity().readKeyPair(in, passphrase);
	}
	
	Vector returnSingleResponseErrorAndLog( String message, String responseCode )
	{
		if( message.length() > 0 )
			logError( message.toString());
		
		Vector response = new Vector();
		response.add( responseCode );
		
		return response;
		
	}
	
	public Vector getContactInfo(String accountId)
	{
		return coreServer.getContactInfo(accountId);			
	}
	
	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId,
				int chunkOffset, int maxChunkSize)
	{
		DatabaseKey headerKey =	findHeaderKeyInDatabase(authorAccountId, bulletinLocalId);
		if(headerKey == null)
			return returnSingleResponseErrorAndLog("getBulletinChunkWithoutVerifyingCaller:  NOT_FOUND ", NetworkInterfaceConstants.ITEM_NOT_FOUND);
		
		try
		{
			return buildBulletinChunkResponse(headerKey, chunkOffset, maxChunkSize);
		}
		catch(Exception e)
		{
			logError(e);
			return returnSingleResponseErrorAndLog("getBulletinChunkWithoutVerifyingCaller:  SERVER_ERROR ", NetworkInterfaceConstants.SERVER_ERROR);
		}
	}


	public DatabaseKey findHeaderKeyInDatabase(String authorAccountId,String bulletinLocalId) 
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		DatabaseKey headerKey = DatabaseKey.createSealedKey(uid);
		if(getDatabase().doesRecordExist(headerKey))
			return headerKey;

		headerKey.setDraft();
		if(getDatabase().doesRecordExist(headerKey))
			return headerKey;

		return null;
	}
	
	private Vector buildBulletinChunkResponse(DatabaseKey headerKey, int chunkOffset, int maxChunkSize) throws
			FileTooLargeException,
			InvalidPacketException, 
			WrongPacketTypeException, 
			SignatureVerificationException, 
			DecryptionException, 
			NoKeyPairException, 
			CryptoException, 
			FileVerificationException, 
			IOException, 
			RecordHiddenException 
	{
		Vector result = new Vector();
		//log("entering createInterimBulletinFile");
		File tempFile = createInterimBulletinFile(headerKey);
		//log("createInterimBulletinFile done");
		int totalLength = MartusUtilities.getCappedFileLength(tempFile);
		
		int chunkSize = totalLength - chunkOffset;
		if(chunkSize > maxChunkSize)
			chunkSize = maxChunkSize;
			
		byte[] rawData = new byte[chunkSize];
		
		FileInputStream in = new FileInputStream(tempFile);
		in.skip(chunkOffset);
		in.read(rawData);
		in.close();
		
		String zipString = StreamableBase64.encode(rawData);
		
		int endPosition = chunkOffset + chunkSize;
		if(endPosition >= totalLength)
		{
			MartusUtilities.deleteInterimFileAndSignature(tempFile);
			result.add(NetworkInterfaceConstants.OK);
		}
		else
		{
			result.add(NetworkInterfaceConstants.CHUNK_OK);
		}
		result.add(new Integer(totalLength));
		result.add(new Integer(chunkSize));
		result.add(zipString);
		logDebug("downloadBulletinChunk : Exit " + result.get(0));
		return result;
	}

	public File createInterimBulletinFile(DatabaseKey headerKey) throws
			CryptoException,
			InvalidPacketException,
			WrongPacketTypeException,
			SignatureVerificationException,
			DecryptionException,
			NoKeyPairException,
			MartusUtilities.FileVerificationException, IOException, RecordHiddenException
	{
		File tempFile = getStore().getOutgoingInterimPublicOnlyFile(headerKey.getUniversalId());
		File tempFileSignature = MartusUtilities.getSignatureFileFromFile(tempFile);
		if(tempFile.exists() && tempFileSignature.exists())
		{
			if(verifyBulletinInterimFile(tempFile, tempFileSignature, getSecurity().getPublicKeyString()))
				return tempFile;
		}
		MartusUtilities.deleteInterimFileAndSignature(tempFile);
		BulletinZipUtilities.exportPublicBulletinPacketsFromDatabaseToZipFile(getDatabase(), headerKey, tempFile, getSecurity());
		tempFileSignature = MartusUtilities.createSignatureFileFromFile(tempFile, getSecurity());
		if(!verifyBulletinInterimFile(tempFile, tempFileSignature, getSecurity().getPublicKeyString()))
			throw new MartusUtilities.FileVerificationException();
		logDebug("    Total file size =" + tempFile.length());
		
		return tempFile;
	}

	public boolean verifyBulletinInterimFile(File bulletinZipFile, File bulletinSignatureFile, String accountId)
	{
		try 
		{
			MartusUtilities.verifyFileAndSignature(bulletinZipFile, bulletinSignatureFile, getSecurity(), accountId);
			return true;
		} 
		catch (MartusUtilities.FileVerificationException e) 
		{
			logError("verifyBulletinInterimFile:", e);
		}
		return false;	
	}
	
	public synchronized void clientConnectionStart()
	{
		//logging("start");
	}
	
	public synchronized void clientConnectionExit()
	{
		//logging("exit");
	}
	
	protected String getCurrentClientIp()
	{
		String ip;
		Thread currThread = Thread.currentThread();
		if( XmlRpcThread.class.getName() == currThread.getClass().getName() )
		{
			ip = ((XmlRpcThread) Thread.currentThread()).getClientAddress();
		}
		else
		{
			ip = Integer.toHexString(currThread.hashCode());
		}

		return ip;
	}

	public MartusCrypto getSecurity()
	{
		return coreServer.getSecurity();
	}

	public static final String CLIENTS_NOT_TO_AMPLIFY_FILENAME = "clientsNotToAmplify.txt";
	
	MartusServer coreServer;
	LoggerInterface logger;

	private ServerSideAmplifierHandler amplifierHandler;
	Vector authorizedAmps;
	Vector clientsNotAmplified;
	public boolean amplifyMirroredBulletins;
}
