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

package org.martus.server.main;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.Vector;
import java.util.zip.ZipFile;

import org.apache.xmlrpc.webserver.ConnectionServerWithIpTracking;
import org.martus.amplifier.ServerCallbackInterface;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.common.ContactInfo;
import org.martus.common.DammCheckDigitAlgorithm.CheckDigitInvalidException;
import org.martus.common.LoggerInterface;
import org.martus.common.LoggerToConsole;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.MartusUtilities.InvalidPublicKeyFileException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.Version;
import org.martus.common.VersionBuildDate;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.CryptoInitializationException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.InvalidKeyPairFileVersionException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.FileDatabase;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.network.MartusSecureWebServer;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.PartialUploadStatus;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongAccountException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.common.packet.UniversalId;
import org.martus.common.serverside.ServerSideUtilities;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.common.xmlrpc.XmlRpcThread;
import org.martus.server.foramplifiers.ServerForAmplifiers;
import org.martus.server.forclients.ServerForClients;
import org.martus.server.formirroring.MirroringRetriever;
import org.martus.server.formirroring.MirrorPuller;
import org.martus.server.formirroring.ServerForMirroring;
import org.martus.server.main.ServerBulletinStore.DuplicatePacketException;
import org.martus.server.main.ServerBulletinStore.SealedPacketExistsException;
import org.martus.util.DirectoryUtils;
import org.martus.util.StreamableBase64;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.UnicodeReader;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;

public class MartusServer implements NetworkInterfaceConstants, ServerCallbackInterface
{
	public static void main(String[] args)
	{
		try
		{
			displayVersion();
			System.out.println("Initializing...this will take a few seconds...");
			File directory = getDefaultDataDirectory();
			final String DATA_DIRECTORY = "--data-directory=";
			for (String arg : args) 
			{
				if(arg.startsWith(DATA_DIRECTORY))
					directory = new File(arg.substring(DATA_DIRECTORY.length()));
			}
			if(!directory.exists())
			{
				System.out.println("ERROR: Missing directory: " + directory);
				System.exit(ServerSideUtilities.EXIT_MISSING_DATA_DIRECTORY);
			}
			MartusServer server = new MartusServer(directory);

			server.processCommandLine(args);
			server.deleteRunningFile();

			if(server.anyUnexpectedFilesOrFoldersInStartupDirectory())
				System.exit(ServerSideUtilities.EXIT_UNEXPECTED_FILE_STARTUP);
			
			if(!server.hasAccount())
			{
				System.out.println("***** Key pair file not found *****");
				System.out.println(server.getKeyPairFile());
				System.exit(ServerSideUtilities.EXIT_KEYPAIR_FILE_MISSING);
			}

			char[] passphrase = server.insecurePassword;
			if(passphrase == null)
				passphrase = ServerSideUtilities.getPassphraseFromConsole(server.getTriggerDirectory(),"MartusServer.main");
			server.loadAccount(passphrase);
			server.initalizeBulletinStore();
			server.verifyAndLoadConfigurationFiles();
			server.displayStatistics();

			System.out.println("Setting up sockets (this may take up to a minute or longer)...");
		
			server.initializeServerForClients();
			server.initializeServerForMirroring();
			server.initializeServerForAmplifiers();
			server.initalizeAmplifier(passphrase);

			if(!server.deleteStartupFiles())
				System.exit(ServerSideUtilities.EXIT_STARTUP_DIRECTORY_NOT_EMPTY);
			
			server.logNotice("Initializing cache (may take a while!)...");
			server.store.fillHistoryAndHqCache();
			server.logNotice("Finished initializing cache");

			server.startBackgroundTimers();
			
			ServerSideUtilities.writeSyncFile(server.getRunningFile(), "MartusServer.main");
			server.getLogger().logNotice("Server is running");
			if(!server.isAmplifierEnabled() && !server.isAmplifierListenerEnabled() &&
			   !server.isClientListenerEnabled() && !server.isMirrorListenerEnabled())
			{				
				server. getLogger().logError("No listeners or web amplifier enabled... Exiting.");
				server.serverExit(ServerSideUtilities.EXIT_NO_LISTENERS);
			}
			server.getLogger().logNotice("Waiting for connection...");
		}
		catch(CryptoInitializationException e) 
		{
			System.out.println("Crypto Initialization Exception" + e);
			e.printStackTrace();
			System.exit(ServerSideUtilities.EXIT_CRYPTO_INITIALIZATION);			
		}
		catch (AuthorizationFailedException e)
		{
			System.out.println("Invalid password: " + e);
			e.printStackTrace();
			System.exit(ServerSideUtilities.EXIT_INVALID_PASSWORD);
		}
		catch (UnknownHostException e)
		{
			System.out.println("ipAddress invalid: " + e);
			e.printStackTrace();
			System.exit(ServerSideUtilities.EXIT_INVALID_IPADDRESS);
		}
		catch (Exception e)
		{
			System.out.println("MartusServer.main: " + e);
			e.printStackTrace();
			System.exit(ServerSideUtilities.EXIT_UNEXPECTED_EXCEPTION);
		}
			
	}

	MartusServer(File dir) throws Exception
	{
		this(dir, new LoggerToConsole());
	}

	protected MartusServer(File dir, LoggerInterface loggerToUse) throws Exception
	{
		this(dir, loggerToUse, new MartusSecurity());
	}

	public MartusServer(File dir, LoggerInterface loggerToUse, MartusCrypto securityToUse) throws Exception
	{
		dataDirectory = dir;
		setLogger(loggerToUse);
		store = new ServerBulletinStore();
		store.setSignatureGenerator(securityToUse);
		MartusSecureWebServer.security = getSecurity();
		
		getTriggerDirectory().mkdirs();
		getStartupConfigDirectory().mkdirs();
		serverForClients = createServerForClients();
		serverForMirroring = new ServerForMirroring(this, this);
		mirroringRetrieverManager = new MirrorPuller(this, this);
		serverForAmplifiers = new ServerForAmplifiers(this, this);
		amp = new MartusAmplifier(this);
		failedUploadRequestsPerIp = new Hashtable();
	}
	
	public ServerForClients createServerForClients()
	{
		return new ServerForClients(this);
	}

	public boolean anyUnexpectedFilesOrFoldersInStartupDirectory()
	{
		Vector startupFilesWeExpect = getDeleteOnStartupFiles();
		Vector startupFoldersWeExpect = getDeleteOnStartupFolders();
		File[] allFilesAndFoldersInStartupDirectory = getStartupConfigDirectory().listFiles();
		for(int i = 0; i<allFilesAndFoldersInStartupDirectory.length; ++i)
		{
			File file = allFilesAndFoldersInStartupDirectory[i];
			if(file.isFile()&&!startupFilesWeExpect.contains(file))
			{	
				logError("Startup File not expected =" + file.getAbsolutePath());
				return true;
			}
			if(file.isDirectory()&&!startupFoldersWeExpect.contains(file))
			{	
				logError("Startup Folder not expected =" + file.getAbsolutePath());
				return true;
			}
		}
		return false;
	}
	
	
	protected void startBackgroundTimers()
	{
		BackgroundServerTimerTask shutdownRequestMonitor = new ShutdownRequestMonitor();
		BackgroundServerTimerTask uploadRequestsMonitor = new UploadRequestsMonitor();
		BackgroundServerTimerTask backgroundTimerTick = new BackgroundTimerTick();
		BackgroundServerTimerTask syncAmplifierWithServersMonitor = new SyncAmplifierWithServersMonitor();
		
		Vector timers = new Vector();
		MartusUtilities.startTimer(shutdownRequestMonitor, shutdownRequestIntervalMillis);
		timers.add(shutdownRequestMonitor);
		MartusUtilities.startTimer(uploadRequestsMonitor, magicWordsGuessIntervalMillis);
		timers.add(uploadRequestsMonitor);
		MartusUtilities.startTimerWithDelayInMillis(backgroundTimerTick, MartusServer.mainTickDelayMillis, MartusServer.mainTickIntervalMillis);
		timers.add(backgroundTimerTick);
		if(isAmplifierEnabled())
		{
			MartusUtilities.startTimer(syncAmplifierWithServersMonitor, amplifierDataSynchIntervalMillis);
			timers.add(syncAmplifierWithServersMonitor);
		}

		MartusUtilities.startTimer(new TimerWatchDogTask(timers), timerWatchDogIntervalMillis);
	}

	private void displayServerPublicCode() throws InvalidBase64Exception, CreateDigestException, CheckDigitInvalidException
	{
		System.out.print("Old Server Public Code: ");
		String accountId = getAccountId();
		System.out.println(MartusCrypto.computeFormattedPublicCode(accountId));
		System.out.print("New Server Public Code: ");
		System.out.println(MartusCrypto.computeFormattedPublicCode40(accountId));
		System.out.println();
	}

	private void displayComplianceStatement()
	{
		System.out.println();
		System.out.println("Server Compliance Statement:");
		System.out.println("---");
		System.out.println(complianceStatement);
		System.out.println("---");
	}

	public void verifyAndLoadConfigurationFiles() throws Exception
	{
		verifyConfigurationFiles();
		loadConfigurationFiles();
	}

	protected void displayStatistics() throws InvalidBase64Exception, CreateDigestException, CheckDigitInvalidException
	{
		displayComplianceStatement();
		displayServerPublicCode();
	}
	
	public void verifyConfigurationFiles()
	{
		if(isClientListenerEnabled())
			serverForClients.verifyConfigurationFiles();
		if(isMirrorListenerEnabled())
			serverForMirroring.verifyConfigurationFiles();
		if(isAmplifierListenerEnabled())
			serverForAmplifiers.verifyConfigurationFiles();
	}

	public void loadConfigurationFiles() throws Exception
	{
		if(isClientListenerEnabled())
			serverForClients.loadConfigurationFiles();
		if(isMirrorListenerEnabled())
			serverForMirroring.loadConfigurationFiles();
		if(isAmplifierListenerEnabled())
			serverForAmplifiers.loadConfigurationFiles();

		//Tests will fail if compliance isn't last.
		MartusServerUtilities.loadHiddenPacketsFile(getHiddenPacketsFile(), getStore(), getLogger());
		loadComplianceStatementFile();
	}
	
	public boolean doesDraftExist(UniversalId uid)
	{
		DatabaseKey key = DatabaseKey.createDraftKey(uid);
		return getDatabase().doesRecordExist(key);
	}
	
	public ServerBulletinStore getStore()
	{
		return store;
	}

	public ReadableDatabase getDatabase()
	{
		return store.getDatabase();
	}
	
	public MartusCrypto getSecurity()
	{
		return getStore().getSignatureGenerator();
	}

	public void setAmpIpAddress(String ampIpAddress)
	{
		this.ampIpAddress = ampIpAddress;
	}

	public String getAmpIpAddress()
	{
		return ampIpAddress;
	}

	private static void setListenersIpAddress(String listenersIpAddress)
	{
		MartusServer.listenersIpAddress = listenersIpAddress;
	}

	private static String getListenersIpAddress()
	{
		return listenersIpAddress;
	}

	public void setLogger(LoggerInterface logger)
	{
		this.logger = logger;
	}

	public LoggerInterface getLogger()
	{
		return logger;
	}

	public boolean isSecureMode()
	{
		return secureMode;
	}
	
	public void enterSecureMode()
	{
		secureMode = true;
	}
	
	public boolean useEmbeddedPresentationFiles()
	{
		return useEmbeddedPresentationFiles;
	}
	
	
	private void setAmplifierEnabled(boolean amplifierEnabled)
	{
		this.amplifierEnabled = amplifierEnabled;
	}

	private boolean isAmplifierEnabled()
	{
		return amplifierEnabled;
	}

	public void setClientListenerEnabled(boolean clientListenerEnabled)
	{
		this.clientListenerEnabled = clientListenerEnabled;
	}

	private boolean isClientListenerEnabled()
	{
		return clientListenerEnabled;
	}

	private void setMirrorListenerEnabled(boolean mirrorListenerEnabled)
	{
		this.mirrorListenerEnabled = mirrorListenerEnabled;
	}

	boolean isMirrorListenerEnabled()
	{
		return mirrorListenerEnabled;
	}

	public void setAmplifierListenerEnabled(boolean amplifierListenerEnabled)
	{
		this.amplifierListenerEnabled = amplifierListenerEnabled;
	}

	private boolean isAmplifierListenerEnabled()
	{
		return amplifierListenerEnabled;
	}

	protected boolean hasAccount()
	{
		return getKeyPairFile().exists();
	}
	
	protected void loadAccount(char[] passphrase) throws AuthorizationFailedException, InvalidKeyPairFileVersionException, IOException
	{
		FileInputStream in = new FileInputStream(getKeyPairFile());
		readKeyPair(in, passphrase);
		in.close();
		System.out.println("Passphrase correct.");			
	}
	
	public String getAccountId()
	{
		return getSecurity().getPublicKeyString();
	}
	
	public String ping()
	{
		logDebug("ping request");		
		return NetworkInterfaceConstants.VERSION;
	}

	public Vector getServerInformation()
	{
		logInfo("getServerInformation");
			
		if( isShutdownRequested() )
			return returnSingleErrorResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
				
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
			logDebug("getServerInformation: Exit OK");
		}
		catch(Exception e)
		{
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
			result.add(e.toString());
			logError("getServerInformation SERVER ERROR", e);			
		}
		return result;
	}
	
	public String uploadBulletinChunk(String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data, String signature)
	{
		logInfo("uploadBulletinChunk");
		
		if(isClientBanned(authorAccountId) )
			return NetworkInterfaceConstants.REJECTED;
		
		if( isShutdownRequested() )
			return NetworkInterfaceConstants.SERVER_DOWN;
		
		String signedString = authorAccountId + "," + bulletinLocalId + "," +
					Integer.toString(totalSize) + "," + Integer.toString(chunkOffset) + "," +
					Integer.toString(chunkSize) + "," + data;
		if(!isSignatureCorrect(signedString, signature, authorAccountId))
		{
			logError("  returning SIG_ERROR");
			logError("Account: " + MartusCrypto.formatAccountIdForLog(authorAccountId));
			logError("signedString: " + signedString.toString());
			logError("signature: " + signature);
			return NetworkInterfaceConstants.SIG_ERROR;
		}
		
		String result = putBulletinChunk(authorAccountId, authorAccountId, bulletinLocalId,
									totalSize, chunkOffset, chunkSize, data);
		return result;
	}


	public String putBulletinChunk(String uploaderAccountId, String authorAccountId, String bulletinLocalId,
		int totalSize, int chunkOffset, int chunkSize, String data) 
	{
		{
			StringBuffer logMsg = new StringBuffer();
			logMsg.append("putBulletinChunk");
			if(!uploaderAccountId.equals(authorAccountId))
				logMsg.append("  Proxy Uploader:" + getClientAliasForLogging(uploaderAccountId));
			logMsg.append("  " + getClientAliasForLogging(authorAccountId) + " " + bulletinLocalId);
			logMsg.append("  Total Size=" + totalSize + ", Offset=" + chunkOffset);
			if(chunkOffset+chunkSize >= totalSize)
				logMsg.append(" Last Chunk = " + chunkSize);
			
			logDebug(logMsg.toString());
		}
		
		if(!canClientUpload(uploaderAccountId))
		{
			logError("putBulletinChunk REJECTED canClientUpload failed");
			return NetworkInterfaceConstants.REJECTED;
		}
		
		if(isClientBanned(uploaderAccountId))
		{
			logError("putBulletinChunk REJECTED isClientBanned uploaderAccountId");
			return NetworkInterfaceConstants.REJECTED;
		}
			
		if(isClientBanned(authorAccountId))
		{
			logError("putBulletinChunk REJECTED isClientBanned authorAccountId");
			return NetworkInterfaceConstants.REJECTED;
		}

		if( isShutdownRequested() )
		{
			logNotice(" returning SERVER_DOWN");
			return NetworkInterfaceConstants.SERVER_DOWN;
		}
		
		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		File interimZipFile;
		try 
		{
			interimZipFile = getStore().getIncomingInterimFile(uid);
		} 
		catch (IOException e) 
		{
			logError("putBulletinChunk Error creating interim file.", e);
			return NetworkInterfaceConstants.SERVER_ERROR;
		} 
		catch (RecordHiddenException e)
		{
			// TODO: Should return a more specific error code
			logError("putBulletinChunk for hidden file " + uid.getLocalId());
			return NetworkInterfaceConstants.INVALID_DATA;
		}
		
		if(chunkSize > NetworkInterfaceConstants.MAXIMUM_CLIENT_MAX_CHUNK_SIZE)
		{
			interimZipFile.delete();
			logError("putBulletinChunk INVALID_DATA (> MAXIMUM_CLIENT_MAX_CHUNK_SIZE)");
			return NetworkInterfaceConstants.INVALID_DATA;
		}			
		
		if(chunkOffset + chunkSize > totalSize)
		{
			interimZipFile.delete();
			logError("putBulletinChunk INVALID_DATA (chunkOffset+chunkSize > totalSize)");
			return NetworkInterfaceConstants.INVALID_DATA;
		}			
		
		if(chunkOffset == 0)
		{
			//this log made no sence. log("putBulletinChunk: restarting at zero");
			interimZipFile.delete();
		}
		
		double oldFileLength = interimZipFile.length();
		if(oldFileLength != chunkOffset)
		{
			interimZipFile.delete();
			logError("putBulletinChunk INVALID_DATA (existing interim length was " + oldFileLength + ")");
			return NetworkInterfaceConstants.INVALID_DATA;
		}
		
		if(oldFileLength + chunkSize > totalSize)
		{
			interimZipFile.delete();
			logError("putBulletinChunk INVALID_DATA (> totalSize)");
			return NetworkInterfaceConstants.INVALID_DATA;
		}			
		
		StringReader reader = null;
		FileOutputStream out = null;
		try 
		{
			reader = new StringReader(data);
			out = new FileOutputStream(interimZipFile.getPath(), true);
			StreamableBase64.decode(reader, out);
			out.close();
			reader.close();
		} 
		catch(Exception e)
		{
			try 
			{
				if(out != null)
					out.close();
			} 
			catch (IOException nothingWeCanDo) 
			{
			}
			if(reader != null)
				reader.close();
			interimZipFile.delete();
			logError("putBulletinChunk INVALID_DATA ", e);
			return NetworkInterfaceConstants.INVALID_DATA;
		}
		
		String result = NetworkInterfaceConstants.INVALID_DATA;
		double newFileLength = interimZipFile.length();
		if(chunkSize != newFileLength - oldFileLength)
		{
			interimZipFile.delete();
			logError("putBulletinChunk INVALID_DATA (chunkSize != actual dataSize)");
			return NetworkInterfaceConstants.INVALID_DATA;
		}			
		
		if(newFileLength < totalSize)
		{
			result = NetworkInterfaceConstants.CHUNK_OK;
		}
		else
		{
			//log("entering saveUploadedBulletinZipFile");
			try
			{
				if(!isAuthorizedToUpload(uploaderAccountId, authorAccountId, interimZipFile))
				{
					result = NetworkInterfaceConstants.NOTYOURBULLETIN;
					logError("putBulletinChunk NOTYOURBULLETIN isAuthorizedToUpload uploaderAccountId");
				}
				else
				{
					result = saveUploadedBulletinZipFile(authorAccountId, bulletinLocalId, interimZipFile);
				}
			}
			catch (InvalidPacketException e1)
			{
				result = NetworkInterfaceConstants.INVALID_DATA;
				logError("putBulletinChunk InvalidPacketException: ", e1);
			}
			catch (SignatureVerificationException e1)
			{
				result = NetworkInterfaceConstants.SIG_ERROR;
				logError("putBulletinChunk SignatureVerificationException: ", e1);
			}
			catch (DecryptionException e1)
			{
				result = NetworkInterfaceConstants.INVALID_DATA;
				logError("putBulletinChunk DecryptionException: ", e1);
			}
			catch (IOException e1)
			{
				result = NetworkInterfaceConstants.SERVER_ERROR;
				logError("putBulletinChunk IOException: ", e1);
			}
			catch (SealedPacketExistsException e1)
			{
				result = NetworkInterfaceConstants.DUPLICATE;
				logError("putBulletinChunk SealedPacketExistsException: ", e1);
			}
			catch (DuplicatePacketException e1)
			{
				result = NetworkInterfaceConstants.DUPLICATE;
				logError("putBulletinChunk DuplicatePacketException: ", e1);
			}
			catch (WrongAccountException e1)
			{
				result = NetworkInterfaceConstants.INVALID_DATA;
				logError("putBulletinChunk WrongAccountException: ", e1);
			}
			catch (Exception e1)
			{
				result = NetworkInterfaceConstants.INVALID_DATA;
				logError("Unknown exception: ", e1);
			}

			//log("returned from saveUploadedBulletinZipFile result =" + result);
			interimZipFile.delete();
		}
		
		logNotice("putBulletinChunk: Exit " + result);
		return result;
	}

	private boolean isAuthorizedToUpload(String uploaderAccountId, String authorAccountId, File zipFile) throws Exception
	{
		ZipFile zip = new ZipFile(zipFile);
		try
		{
			BulletinHeaderPacket header = MartusUtilities.extractHeaderPacket(authorAccountId, zip, getSecurity());
			return header.isAuthorizedToUpload(uploaderAccountId);
		}
		finally
		{
			zip.close();
		}
	}	
	
	public Vector getBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId,
		int chunkOffset, int maxChunkSize) 
	{
		{
			StringBuffer logMsg = new StringBuffer();
			logMsg.append("getBulletinChunk caller: " + getClientAliasForLogging(myAccountId));
			logMsg.append(" author: " + getClientAliasForLogging(authorAccountId) + " " + bulletinLocalId);
			logMsg.append("  Offset=" + chunkOffset + ", Max=" + maxChunkSize);
			logDebug(logMsg.toString());
		}
		
		if(isClientBanned(myAccountId) )
			return returnSingleErrorResponseAndLog( " returning REJECTED", NetworkInterfaceConstants.REJECTED );
		
		if( isShutdownRequested() )
			return returnSingleErrorResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );

		DatabaseKey headerKey =	findHeaderKeyInDatabase(authorAccountId, bulletinLocalId);
		if(headerKey == null)
		{
			logInfo(" returning NOT_FOUND");
			Vector response = new Vector();
			response.add( NetworkInterfaceConstants.ITEM_NOT_FOUND );
			return response;
		}

		if(!myAccountId.equals(authorAccountId))
		{
			try 
			{
				if( !isHQAccountAuthorizedToRead(headerKey, myAccountId))
					return returnSingleErrorResponseAndLog( " returning NOTYOURBULLETIN", NetworkInterfaceConstants.NOTYOURBULLETIN );
			} 
			catch (SignatureVerificationException e) 
			{
					return returnSingleErrorResponseAndLog( " returning SIG ERROR", NetworkInterfaceConstants.SIG_ERROR );
			} 
			catch (Exception e) 
			{
				logError(e);
				return returnSingleErrorResponseAndLog( " returning SERVER_ERROR: ", NetworkInterfaceConstants.SERVER_ERROR );
			} 
		}

		Vector result = getBulletinChunkWithoutVerifyingCaller(
					authorAccountId, bulletinLocalId,
					chunkOffset, maxChunkSize);
		
		
		logNotice("getBulletinChunk exit: " + result.get(0));
		return result;
	}


	public Vector listFieldOfficeAccounts(String hqAccountIdToUse)
	{

		String clientAliasForLogging = getClientAliasForLogging(hqAccountIdToUse);
		logInfo("listFieldOfficeAccounts " + clientAliasForLogging);
			
		if(isClientBanned(hqAccountIdToUse) )
			return returnSingleErrorResponseAndLog("  returning REJECTED", NetworkInterfaceConstants.REJECTED);
		
		if( isShutdownRequested() )
			return returnSingleErrorResponseAndLog("  returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN);

		Vector accountsWithResultCode  = getStore().getFieldOfficeAccountIdsWithResultCode(hqAccountIdToUse, getLogger());
	
		logNotice("listFieldOfficeAccounts: "+clientAliasForLogging+" Exit accounts=" + (accountsWithResultCode.size()-1));
		return accountsWithResultCode;	
	}
	
	public String putContactInfo(String accountId, Vector contactInfo)
	{
		logInfo("putContactInfo " + getClientAliasForLogging(accountId));

		if(isClientBanned(accountId) || !canClientUpload(accountId))
			return NetworkInterfaceConstants.REJECTED;
		
		if( isShutdownRequested() )
			return NetworkInterfaceConstants.SERVER_DOWN;
			
		String result = NetworkInterfaceConstants.INVALID_DATA;
		if(contactInfo == null)
			return result;
		if(contactInfo.size() <= 3)
			return result;
		try
		{
			contactInfo = ContactInfo.decodeContactInfoVectorIfNecessary(contactInfo);
		}
		catch (Exception e1)
		{
			logError(e1);
			return result;
		}
		
		String publicKey = (String)contactInfo.get(0);
		if(!publicKey.equals(accountId))
			return result;
		int contentSize = ((Integer)(contactInfo.get(1))).intValue();
		if(contentSize + 3 != contactInfo.size())
			return result;

		if(!getSecurity().verifySignatureOfVectorOfStrings(contactInfo, publicKey))
			return NetworkInterfaceConstants.SIG_ERROR;

		try
		{
			getStore().writeContactInfo(accountId, contactInfo);
		}
		catch (IOException e)
		{
			logError("putContactInfo", e);
			return NetworkInterfaceConstants.SERVER_ERROR;
		}
		return NetworkInterfaceConstants.OK;
	}

	public Vector getContactInfo(String accountId)
	{
		Vector results = new Vector();
		try
		{
			if(!getStore().doesContactInfoExist(accountId))
			{
				results.add(NetworkInterfaceConstants.ITEM_NOT_FOUND);
				return results;
			}
		}
		catch (IOException e)
		{
			results.add(NetworkInterfaceConstants.ITEM_NOT_FOUND);
			logError(e);
			return results;
		}
		
		try
		{
			Vector decodedContactInfo = getStore().readContactInfo(accountId);
			if(!getSecurity().verifySignatureOfVectorOfStrings(decodedContactInfo, accountId))
			{
				String accountInfo = MartusCrypto.formatAccountIdForLog(accountId);
				logError("getContactInfo: "+ accountInfo +": Signature failed");
				results.add(NetworkInterfaceConstants.SIG_ERROR);
				return results;
			}
			Vector encodedContactInfo = ContactInfo.encodeContactInfoVector(decodedContactInfo);
			
			results.add(NetworkInterfaceConstants.OK);
			results.add(encodedContactInfo);
			return results;
		}
		catch (Exception e1)
		{
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
			logError(e1);
			return results;
		}
	}
	
	public void setComplianceStatement(String statement)
	{
		complianceStatement = statement;
	}

	public Vector getServerCompliance()
	{
		
		logInfo("getServerCompliance");
		Vector result = new Vector();
		result.add(OK);
		Object[] compliance = new Object[] {complianceStatement};
		result.add(compliance);
		return result;
	}	

	public Vector getPartialUploadStatus(String authorAccountId, String bulletinLocalId, Vector extraParameters) 
	{
		logInfo("getPartialUploadStatus");
		UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		PartialUploadStatus status = new PartialUploadStatus(0L, "");
		
		File partialUploadFile;
		try 
		{
			partialUploadFile = getStore().getIncomingInterimFile(uid);
			status = getPartialUploadStatus(partialUploadFile);
		} 
		catch (Exception e) 
		{
			MartusLogger.logException(e);
		}
		
		Vector result = new Vector();
		result.add(OK);
		result.add(getStatusAsArray(status));
		return result;
	}

	private PartialUploadStatus getPartialUploadStatus(File partialUploadFile) 
	{
		PartialUploadStatus emptyStatus = new PartialUploadStatus(0L, "");
		
		if(!partialUploadFile.exists())
			return emptyStatus;

		try
		{
			long length = partialUploadFile.length();
			FileInputStreamWithSeek input = new FileInputStreamWithSeek(partialUploadFile);
			BufferedInputStream buffered = new BufferedInputStream(input);
			try
			{
				String partialSha1 = MartusSecurity.createBase64Digest(buffered);
				logInfo("getPartialUploadStatus found file of length " + length);
				return new PartialUploadStatus(length, partialSha1);
			}
			finally
			{
				buffered.close();
				input.close();
			}
		}
		catch(Exception e)
		{
			return emptyStatus;
		}
	}

	public Object[] getStatusAsArray(PartialUploadStatus status) 
	{
		Vector statusVector = new Vector();
		statusVector.add(new Long(status.lengthOfPartialUpload()).toString());
		statusVector.add(status.sha1OfPartialUpload());
		return statusVector.toArray();
	}

	public Vector downloadFieldDataPacket(String authorAccountId, String bulletinLocalId, String packetLocalId, String myAccountId, String signature)
	{
		logInfo("downloadFieldOfficeDataPacket: " + getClientAliasForLogging(authorAccountId) + "  " + 
				bulletinLocalId + "  packet " + packetLocalId + " requested by: " + 
				getClientAliasForLogging(myAccountId));
		
		if(isClientBanned(myAccountId) )
			return returnSingleErrorResponseAndLog( " returning REJECTED", NetworkInterfaceConstants.REJECTED );
		
		if( isShutdownRequested() )
			return returnSingleErrorResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
	
		Vector result = new Vector();

		String signedString = authorAccountId + "," + bulletinLocalId + "," + packetLocalId + "," + myAccountId;
		if(!isSignatureCorrect(signedString, signature, myAccountId))
		{
			logError("  returning SIG_ERROR");
			logError("Account: " + MartusCrypto.formatAccountIdForLog(authorAccountId));
			logError("signedString: " + signedString.toString());
			logError("signature: " + signature);
			return returnSingleErrorResponseAndLog("", NetworkInterfaceConstants.SIG_ERROR);
		}
		
		result = getPacket(myAccountId, authorAccountId, bulletinLocalId, packetLocalId);
		
		logNotice("downloadFieldDataPacket: Exit");
		return result;
	}


	public Vector getPacket(String myAccountId, String authorAccountId, String bulletinLocalId,
		String packetLocalId) 
	{
		Vector result = new Vector();
		
		if(isClientBanned(myAccountId) )
			return returnSingleErrorResponseAndLog( " returning REJECTED", NetworkInterfaceConstants.REJECTED );
		
		if( isShutdownRequested() )
			return returnSingleErrorResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
		
		boolean isHeaderPacket = BulletinHeaderPacket.isValidLocalId(packetLocalId);
		boolean isFieldDataPacket = FieldDataPacket.isValidLocalId(packetLocalId);
		boolean isAllowed = isHeaderPacket || isFieldDataPacket;
		if(!isAllowed)
			return returnSingleErrorResponseAndLog( "  attempt to download disallowed packet type: " + packetLocalId, NetworkInterfaceConstants.INVALID_DATA );
		
		ReadableDatabase db = getDatabase();
		
		UniversalId headerUid = UniversalId.createFromAccountAndLocalId(authorAccountId, bulletinLocalId);
		DatabaseKey headerKey = DatabaseKey.createSealedKey(headerUid);
		
		if(!db.doesRecordExist(headerKey))
			headerKey.setDraft();
		
		if(!db.doesRecordExist(headerKey))
		{
			return returnSingleErrorResponseAndLog( "  header packet not found", NetworkInterfaceConstants.ITEM_NOT_FOUND );
		}
		
		UniversalId dataPacketUid = UniversalId.createFromAccountAndLocalId(authorAccountId, packetLocalId);
		DatabaseKey dataPacketKey = null;
		if(headerKey.isDraft())
			dataPacketKey = DatabaseKey.createDraftKey(dataPacketUid);
		else
			dataPacketKey = DatabaseKey.createSealedKey(dataPacketUid);
			
		if(!db.doesRecordExist(dataPacketKey))
		{
			return returnSingleErrorResponseAndLog( "  data packet not found", NetworkInterfaceConstants.ITEM_NOT_FOUND );
		}
		
		try
		{
			if(!myAccountId.equals(authorAccountId) && 
				!isHQAccountAuthorizedToRead(headerKey, myAccountId))
			{
				return returnSingleErrorResponseAndLog( "  neither author nor HQ account", NetworkInterfaceConstants.NOTYOURBULLETIN );
			}
			
			String packetXml = db.readRecord(dataPacketKey, getSecurity());
		
			result.add(NetworkInterfaceConstants.OK);
			result.add(packetXml);
			return result;
		}
		catch(Exception e)
		{
			//TODO: Make sure this has a test!
			logError("error loading", e);
			result.clear();
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
			return result;
		}
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
			logError("SERVER_ERROR:", e);
			return NetworkInterfaceConstants.SERVER_ERROR;
		} 
		catch(InvalidBase64Exception e) 
		{
			logError("INVALID_DATA:", e);
			return NetworkInterfaceConstants.INVALID_DATA;
		}
	}
	
	// end MartusServerInterface interface

	public boolean canClientUpload(String clientId)
	{
		return serverForClients.canClientUpload(clientId);
	}
	
	public boolean isClientBanned(String clientId)
	{
		return serverForClients.isClientBanned(clientId);
	}

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
	
	public void loadComplianceStatementFile() throws IOException
	{
		try
		{
			UnicodeReader reader = new UnicodeReader(getComplianceFile());
			setComplianceStatement(reader.readAll());
			reader.close();
		}
		catch (IOException e)
		{
			logError("Missing or unable to read file: " + getComplianceFile().getAbsolutePath(), e);
			throw e;
		}
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
	
	void writeKeyPair(OutputStream out, char[] passphrase) throws 
		Exception
	{
		getSecurity().writeKeyPair(out, passphrase);
	}
	
	public static String getDefaultDataDirectoryPath()
	{
		String dataDirectory = null;
		if(Version.isRunningUnderWindows())
		{
			dataDirectory = "C:/MartusServer/";
		}
		else
		{
			dataDirectory = "/var/MartusServer/";
		}
		return dataDirectory;
	}
	
	public static File getDefaultDataDirectory()
	{
		File file = new File(MartusServer.getDefaultDataDirectoryPath());
		if(!file.exists())
		{
			file.mkdirs();
		}
		
		return file;
	}
	
	public static String getKeypairFilename()
	{
		return KEYPAIRFILENAME;
	}
	
	public Vector returnSingleErrorResponseAndLog( String message, String responseCode )
	{
		if( message.length() > 0 )
			logError( message.toString());
		
		Vector response = new Vector();
		response.add( responseCode );
		
		return response;
		
	}
	
	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId,
				int chunkOffset, int maxChunkSize)
	{
		DatabaseKey headerKey =	findHeaderKeyInDatabase(authorAccountId, bulletinLocalId);
		if(headerKey == null)
			return returnSingleErrorResponseAndLog("getBulletinChunkWithoutVerifyingCaller:  NOT_FOUND ", NetworkInterfaceConstants.ITEM_NOT_FOUND);
		
		try
		{
			return buildBulletinChunkResponse(headerKey, chunkOffset, maxChunkSize);
		}
		catch(RecordHiddenException e)
		{
			// TODO: Should return more specific error code
			logError(e);
			return returnSingleErrorResponseAndLog("getBulletinChunkWithoutVerifyingCaller:  SERVER_ERROR", NetworkInterfaceConstants.SERVER_ERROR);
		}
		catch(Exception e)
		{
			logError(e);
			return returnSingleErrorResponseAndLog("getBulletinChunkWithoutVerifyingCaller:  SERVER_ERROR ", NetworkInterfaceConstants.SERVER_ERROR);
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

	public String saveUploadedBulletinZipFile(String authorAccountId, String bulletinLocalId, File zipFile) 
	{
		String result = NetworkInterfaceConstants.OK;
		
		BulletinHeaderPacket bhp = null;
		try
		{
			bhp = getStore().saveZipFileToDatabase(zipFile, authorAccountId);
		}
		catch (DuplicatePacketException e)
		{
			logWarning("saveUpload DUPLICATE: ");
			result =  NetworkInterfaceConstants.DUPLICATE;
		}
		catch (SealedPacketExistsException e)
		{
			logWarning("saveUpload SEALED_EXISTS: ");
			result =  NetworkInterfaceConstants.SEALED_EXISTS;
		}
		catch (Packet.SignatureVerificationException e)
		{
			logError("saveUpload SIG_ERROR: ", e);
			result =  NetworkInterfaceConstants.SIG_ERROR;
		}
		catch (Packet.WrongAccountException e)
		{
			logError("saveUpload NOTYOURBULLETIN: WrongAccountException");
			result =  NetworkInterfaceConstants.NOTYOURBULLETIN;
		}
		catch (Exception e)
		{
			logError("saveUpload INVALID_DATA: ", e);
			result =  NetworkInterfaceConstants.INVALID_DATA;
		}
		if(result != NetworkInterfaceConstants.OK)
			return result;

		try
		{
			getStore().writeBur(bhp);
			getStore().deleteDel(bhp.getUniversalId());
		}
		catch (Exception e)
		{
			logError("saveUpload SERVER_ERROR: ",  e);
			result =  NetworkInterfaceConstants.SERVER_ERROR;
		} 
		
		return result;
	}

	private boolean isHQAccountAuthorizedToRead(DatabaseKey headerKey, String hqPublicKey) throws
			IOException,
			CryptoException,
			InvalidPacketException,
			WrongPacketTypeException,
			SignatureVerificationException,
			DecryptionException
	{
		BulletinHeaderPacket bhp = loadBulletinHeaderPacket(getDatabase(), headerKey);
		return bhp.isHQAuthorizedToRead(hqPublicKey);
	}
	
	private Vector buildBulletinChunkResponse(DatabaseKey headerKey, int chunkOffset, int maxChunkSize) throws Exception
	{
		Vector result = new Vector();
		//log("entering createInterimBulletinFile");
		File tempFile = createInterimBulletinFile(headerKey);
		//log("createInterimBulletinFile done");
		int totalLength = MartusUtilities.getCappedFileLength(tempFile);
		
		int chunkSize = totalLength - chunkOffset;
		if(chunkSize > maxChunkSize)
			chunkSize = maxChunkSize;
		
		if(chunkSize < 0)
		{
			logError("Illegal chunk request, offset=" + chunkOffset + ", totalLength=" + totalLength);
			result.add(NetworkInterfaceConstants.INVALID_DATA);
			return result;
		}
			
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
		logNotice("downloadBulletinChunk: Exit " + result.get(0));
		return result;
	}

	public File createInterimBulletinFile(DatabaseKey headerKey) throws Exception
	{
		File interimFile = getStore().getOutgoingInterimFile(headerKey.getUniversalId());
		File interimFileSignature = MartusUtilities.getSignatureFileFromFile(interimFile);
		if(interimFile.exists() && interimFileSignature.exists())
		{
			if(verifyBulletinInterimFile(interimFile, interimFileSignature, getSecurity().getPublicKeyString()))
				return interimFile;
		}
		MartusUtilities.deleteInterimFileAndSignature(interimFile);

		File tempDirectory = interimFile.getParentFile();
		File tempFile = File.createTempFile(interimFile.getName(), ".tmp", tempDirectory);
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(getDatabase(), headerKey, tempFile, getSecurity());
		File tempFileSignature = MartusUtilities.createSignatureFileFromFile(tempFile, getSecurity());

		boolean tempRenameWorked = false;
		boolean tempSignatureWorked = false;
		
		if(!interimFile.exists())
			tempRenameWorked = tempFile.renameTo(interimFile);
		if(!interimFileSignature.exists())
			tempSignatureWorked = tempFileSignature.renameTo(interimFileSignature);

		if(!verifyBulletinInterimFile(interimFile, interimFileSignature, getSecurity().getPublicKeyString()))
		{
			tempFile.delete();
			tempFileSignature.delete();
			logError("    createInterimBulletinFile failed verifyBulletinInterimFile:" + interimFile.getName());
			throw new MartusUtilities.FileVerificationException();
		}
		
		if(!tempRenameWorked)
			tempFile.delete();
		if(!tempSignatureWorked)
			tempFileSignature.delete();
		
		logDebug("    Total file size =" + interimFile.length());
		return interimFile;
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
	
	private boolean isSignatureCorrect(String signedString, String signature, String signerPublicKey)
	{
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(signedString.getBytes("UTF-8"));
			return getSecurity().isValidSignatureOfStream(signerPublicKey, in, StreamableBase64.decode(signature));
		}
		catch(Exception e)
		{
			logError("isSigCorrect exception:", e);
			return false;
		}
	}

	public String getClientAliasForLogging(String clientId)
	{
		try
		{
			return getDatabase().getFolderForAccount(clientId);
		}
		catch (IOException e)
		{
			return clientId;
		}
	}
	
	private Vector getMainServersDeleteOnStartupFiles()
	{
		Vector startupFiles = new Vector();
		startupFiles.add(getKeyPairFile());
		startupFiles.add(getHiddenPacketsFile());
		startupFiles.add(getComplianceFile());
		return startupFiles;
		
	}
	
	private Vector getMainServersDeleteOnStartupFolders()
	{
		Vector startupFolders = new Vector();
		return startupFolders;
			
	}
	
	public Vector getDeleteOnStartupFiles()
	{
		Vector startupFiles = new Vector();
		startupFiles.addAll(getMainServersDeleteOnStartupFiles());
		startupFiles.addAll(amp.getDeleteOnStartupFiles());
		startupFiles.addAll(serverForClients.getDeleteOnStartupFiles());
		startupFiles.addAll(serverForAmplifiers.getDeleteOnStartupFiles());
		startupFiles.addAll(serverForMirroring.getDeleteOnStartupFiles());
		startupFiles.addAll(mirroringRetrieverManager.getDeleteOnStartupFiles());
		return startupFiles;
	}

	public Vector getDeleteOnStartupFolders()
	{
		Vector startupFolders = new Vector();
		startupFolders.addAll(getMainServersDeleteOnStartupFolders());
		startupFolders.addAll(serverForClients.getDeleteOnStartupFolders());
		startupFolders.addAll(amp.getDeleteOnStartupFolders());
		startupFolders.addAll(serverForAmplifiers.getDeleteOnStartupFolders());
		startupFolders.addAll(serverForMirroring.getDeleteOnStartupFolders());
		startupFolders.addAll(mirroringRetrieverManager.getDeleteOnStartupFolders());
		return startupFolders;
	}
	
	public boolean deleteStartupFiles()
	{
		if(!isSecureMode())
			return true;

		logNotice("Deleting Startup Files");
		MartusUtilities.deleteAllFiles(getMainServersDeleteOnStartupFiles());
		DirectoryUtils.deleteEntireDirectoryTree(getMainServersDeleteOnStartupFolders());
		amp.deleteAmplifierStartupFiles();
		serverForClients.deleteStartupFiles();
		serverForAmplifiers.deleteStartupFiles();
		serverForMirroring.deleteStartupFiles();
		mirroringRetrieverManager.deleteStartupFiles();
		
		File startupDir = getStartupConfigDirectory();
		File[] remainingStartupFiles = startupDir.listFiles();
		if(remainingStartupFiles.length != 0)
		{
			logError("Files still exist in the folder: " + startupDir.getAbsolutePath());
			return false;
		}
		return true;
	}

	public boolean isShutdownRequested()
	{
		boolean exitFile = getShutdownFile().exists();
		if(exitFile && !loggedShutdownRequested)
		{
			loggedShutdownRequested = true;
			logNotice("Exit file found, attempting to shutdown.");
		}
		return(exitFile);
	}
	
	public boolean canExitNow()
	{
		
		if(!amp.canExitNow())
			return false;
		return serverForClients.canExitNow();
	}
	
	public synchronized void incrementFailedUploadRequestsForCurrentClientIp()
	{
		String ip = getCurrentClientIp();
		int failedUploadRequest = 1;
		if(failedUploadRequestsPerIp.containsKey(ip))
		{
			Integer currentValue = (Integer) failedUploadRequestsPerIp.get(ip);
			failedUploadRequest = currentValue.intValue() + failedUploadRequest;
		}
		failedUploadRequestsPerIp.put(ip, new Integer(failedUploadRequest));
	}
	
	public synchronized void subtractMaxFailedUploadRequestsForIp(String ip)
	{
		if(failedUploadRequestsPerIp.containsKey(ip))
		{
			Integer currentValue = (Integer) failedUploadRequestsPerIp.get(ip);
			int newValue = currentValue.intValue() - getMaxFailedUploadAllowedAttemptsPerIp();
			if(newValue < 0)
			{
				failedUploadRequestsPerIp.remove(ip);
			}
			else
			{
				failedUploadRequestsPerIp.put(ip, new Integer(newValue));
			}
		}
	}
	
	public int getMaxFailedUploadAllowedAttemptsPerIp()
	{
		return MAX_FAILED_UPLOAD_ATTEMPTS;
	}
	
	public int getNumFailedUploadRequestsForIp(String ip)
	{
		if(failedUploadRequestsPerIp.containsKey(ip))
		{
			Integer currentValue = (Integer) failedUploadRequestsPerIp.get(ip);
			return currentValue.intValue();
		}
		return 0;
	}
	
	public synchronized boolean areUploadRequestsAllowedForCurrentIp()
	{
		String ip = getCurrentClientIp();
		if(failedUploadRequestsPerIp.containsKey(ip))
		{
			return (getNumFailedUploadRequestsForIp(ip) < getMaxFailedUploadAllowedAttemptsPerIp());
		}
		return true;
	}


	protected String getCurrentClientIp()
	{
		String ip;
		Thread currThread = Thread.currentThread();
		if( XmlRpcThread.class.getName() == currThread.getClass().getName() )
		{
			ip = ((XmlRpcThread) Thread.currentThread()).getClientIp();
		}
		else
		{
			ip = Integer.toHexString(currThread.hashCode());
		}

		return ip;
	}
	
	protected String getCurrentClientAddress()
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

	private String createLogString(String message)
	{
		if(message.length() == 0)
			return message;
		
		StringBuilder result = new StringBuilder();
		appendWithColonTerminator(result, getLoggableCallerIpAndPort());
		appendWithColonTerminator(result, getThreadId());
		appendWithColonTerminator(result, getLoggableCallerPublicCode());
		if(result.length() > 0)
			result.append(' ');
		result.append(message);
		return result.toString();
	}
	
	public void appendWithColonTerminator(StringBuilder existing, String toAppend)
	{
		if(toAppend != null)
			existing.append(toAppend);
		
		existing.append(":");
	}

	public String getLoggableCallerIpAndPort() 
	{
		String remoteHostAddressAndPort = ConnectionServerWithIpTracking.getRemoteHostAddressAndPort();
		if(remoteHostAddressAndPort == null)
			return ":";
		
		return remoteHostAddressAndPort;
	}

	public String getLoggableCallerPublicCode() 
	{
		String accountId = getCallerAccountId();
		if(accountId == null)
			return "";

		try 
		{
			return MartusSecurity.computeFormattedPublicCode(accountId);
		} 
		catch (Exception e) 
		{
			// NOTE: can't call logError because it might recurse here
			e.printStackTrace();
			return "";
		}
	}

	private String getThreadId() 
	{
		String rawName = Thread.currentThread().getName();
		rawName = rawName.replaceAll(" ", "_");
		return "tname=" + rawName;
	}

	public synchronized void logError(String message)
	{
		getLogger().logError(createLogString(message));
	}
	
	public void logError(Exception e)
	{
		getLogger().logError(e);
	}
	
	public void logError(String message, Exception e)
	{
		getLogger().logError(createLogString(message), e);
	}

	public synchronized void logInfo(String message)
	{
		getLogger().logInfo(createLogString(message));
	}

	public synchronized void logNotice(String message)
	{
		getLogger().logNotice(createLogString(message));
	}
	
	public synchronized void logWarning(String message)
	{
		getLogger().logWarning(createLogString(message));
	}

	public synchronized void logDebug(String message)
	{
		getLogger().logDebug(createLogString(message));
	}
	
	
	String getServerName()
	{
		if(serverName == null)
			return "host/address";
		return serverName;
	}

	public Vector loadServerPublicKeys(File directoryContainingPublicKeyFiles, String label) throws IOException, InvalidPublicKeyFileException, PublicInformationInvalidException
	{
		Vector servers = new Vector();

		File[] files = directoryContainingPublicKeyFiles.listFiles();
		if(files == null)
			return servers;
		for (int i = 0; i < files.length; i++)
		{
			File thisFile = files[i];
			Vector publicInfo = MartusUtilities.importServerPublicKeyFromFile(thisFile, getSecurity());
			String accountId = (String)publicInfo.get(0);
			servers.add(accountId);
			if(isSecureMode())
			{
				thisFile.delete();
				if(thisFile.exists())
					throw new IOException("delete failed: " + thisFile);
			}
			logNotice(label + " authorized to call us: " + thisFile.getName());
		}
		
		return servers;
	}

	public BulletinHeaderPacket loadBulletinHeaderPacket(ReadableDatabase db, DatabaseKey key)
	throws
		IOException,
		CryptoException,
		InvalidPacketException,
		WrongPacketTypeException,
		SignatureVerificationException,
		DecryptionException
	{
		return BulletinStore.loadBulletinHeaderPacket(db, key, getSecurity());
	}
	
	public class UnexpectedExitException extends Exception
	{
	}
	
	public void serverExit(int exitCode) throws UnexpectedExitException 
	{
		System.exit(exitCode);
	}

	private void initializeServerForMirroring() throws Exception
	{
		mirroringRetrieverManager.createMirroringRetrievers();
		if(!isMirrorListenerEnabled())
			return;
		serverForMirroring.addListeners();
	}

	private void initializeServerForClients() throws UnknownHostException
	{
		if(!isClientListenerEnabled())
			return;
		serverForClients.addListeners();
		serverForClients.displayClientStatistics();
	}
	
	private void initializeServerForAmplifiers() throws UnknownHostException
	{
		if(!isAmplifierListenerEnabled())
			return;
		serverForAmplifiers.addListeners();
	}
	
	public void initalizeAmplifier(char[] keystorePassword) throws Exception
	{
		if(!isAmplifierEnabled())
			return;
		amp.initalizeAmplifier(keystorePassword);
	}

	public int[] shiftToDevelopmentPortsIfNotInSecureMode(int[] defaultPorts)
	{
		if(isSecureMode())
			return defaultPorts;
		
		int[] developmentPorts = new int[defaultPorts.length];
		for(int p = 0; p < developmentPorts.length; ++p)
			developmentPorts[p] = defaultPorts[p] + ServerCallbackInterface.DEVELOPMENT_MODE_PORT_DELTA;
		
		return developmentPorts;
	}

	protected void deleteRunningFile()
	{
		getRunningFile().delete();
	}

	protected File getRunningFile()
	{
		File runningFile = new File(getTriggerDirectory(), "running");
		return runningFile;
	}


	protected static void displayVersion()
	{
		System.out.println("MartusServer");
		System.out.println("Version " + MarketingVersionNumber.marketingVersionNumber);
		String versionInfo = VersionBuildDate.getVersionBuildDate();
		System.out.println("Build Date " + versionInfo);
	}


	public void processCommandLine(String[] args) throws Exception
	{
		long indexEveryXMinutes = 0;
		String indexEveryXMinutesTag = "--amplifier-indexing-minutes=";
		String mirrorSleepMinutesTag = "--mirror-pull-sleep-minutes=";
		String ampipTag = "--amplifier-ip=";
		String listenersIpTag = "--listeners-ip=";
		String secureModeTag = "--secure";
		String noPasswordTag = "--nopassword";
		String enableAmplifierTag = "--amplifier";
		String enableClientListenerTag = "--client-listener";
		String enableMirrorListenerTag = "--mirror-listener";
		String enableAmplifierListenerTag = "--amplifier-listener";
		String simulateBadConnectionTag = "--simulate-bad-connection";
		String embeddedPresentationFiles = "--embedded-presentation";
		String tokenAuthority = "--token-authority=";
		
		setAmplifierEnabled(false);
		String amplifierIndexingMessage = "";
		for(int arg = 0; arg < args.length; ++arg)
		{
			String argument = args[arg];
			if(argument.equals(enableAmplifierTag))
				setAmplifierEnabled(true);
			if(argument.equals(enableClientListenerTag))
				setClientListenerEnabled(true);
			if(argument.equals(enableMirrorListenerTag))
				setMirrorListenerEnabled(true);
			if(argument.equals(enableAmplifierListenerTag))
				setAmplifierListenerEnabled(true);
			if(argument.equals(secureModeTag))
				enterSecureMode();
			if(argument.startsWith(listenersIpTag))
				setListenersIpAddress(argument.substring(listenersIpTag.length()));
			if(argument.equals(noPasswordTag))
				insecurePassword = "password".toCharArray();
			if(argument.equals(simulateBadConnectionTag))
				simulateBadConnection = true;
			if(argument.equals(embeddedPresentationFiles))
				useEmbeddedPresentationFiles = true;
			if(argument.startsWith(ampipTag))
				setAmpIpAddress(argument.substring(ampipTag.length()));

			if(argument.startsWith(indexEveryXMinutesTag))
			{	
				String minutes = argument.substring(indexEveryXMinutesTag.length());
				indexEveryXMinutes = new Integer(minutes).longValue();
				amplifierIndexingMessage = "Amplifier indexing every " + indexEveryXMinutes + " minutes";
			}

			if(argument.startsWith(mirrorSleepMinutesTag))
			{	
				String minutes = argument.substring(mirrorSleepMinutesTag.length());
				MirroringRetriever.inactiveSleepMillis = new Integer(minutes).longValue() * 60 * 1000;
				logNotice("Mirror sleep duration: " + MirroringRetriever.inactiveSleepMillis/1000/60 + " minutes");
			}
			
			if(argument.startsWith(tokenAuthority))
			{
				tokenAuthorityBase = argument.substring(tokenAuthority.length());
				logNotice("Token authority: " + tokenAuthorityBase);
			}
		}
		
		if(tokenAuthorityBase == null)
		{
			logError("Must specify --token-authority");
			serverExit(ServerSideUtilities.EXIT_INVALID_COMMAND_LINE);
		}
		
		if(indexEveryXMinutes==0)
		{
			indexEveryXMinutes = MartusAmplifier.DEFAULT_MINUTES_TO_SYNC;
			amplifierIndexingMessage = "Amplifier indexing every " + indexEveryXMinutes + " minutes";
		}
		
		logNotice("MainTickInterval (millis): " + mainTickIntervalMillis);
		logNotice("MainTickDelay (millis):    " + mainTickDelayMillis);
		
		System.out.println("");
		if(isSecureMode())
			System.out.println("Running in SECURE mode");
		else
			System.out.println("***RUNNING IN INSECURE MODE***");
			
		if(simulateBadConnection)
			System.out.println("***SIMULATING BAD CONNECTIONS!!!***");
		
		if(isClientListenerEnabled())
			System.out.println("Client listener enabled on " + getListenersIpAddress());
		if(isMirrorListenerEnabled())
			System.out.println("Mirror listener enabled on " + getListenersIpAddress());
		if(isAmplifierListenerEnabled())
			System.out.println("Amplifier listener enabled on " + getListenersIpAddress());
		if(isAmplifierEnabled())
		{
			System.out.println("Web Amplifier is Enabled on " + getAmpIpAddress());
			amplifierDataSynchIntervalMillis = indexEveryXMinutes * MILLIS_PER_MINUTE;
			System.out.println(amplifierIndexingMessage);
		}
		System.out.println("");
	}

	public static InetAddress getMainIpAddress() throws UnknownHostException
	{
		return InetAddress.getByName(getListenersIpAddress());
	}

	private void initalizeBulletinStore()
	{
		File packetsDirectory = new File(getDataDirectory(), "packets");
		Database diskDatabase = new ServerFileDatabase(packetsDirectory, getSecurity());
		initializeBulletinStore(diskDatabase);
	}

	public void initializeBulletinStore(Database databaseToUse)
	{
		try
		{
			store.doAfterSigninInitialization(getDataDirectory(), databaseToUse);
		}
		catch(FileDatabase.MissingAccountMapException e)
		{
			logError("Missing Account Map File", e);
			System.exit(7);
		}
		catch(FileDatabase.MissingAccountMapSignatureException e)
		{
			logError("Missing Account Map Signature File", e);
			System.exit(7);
		}
		catch(FileVerificationException e)
		{
			logError("Account Map did not verify against signature file", e);
			System.exit(7);
		}
	}

	protected File getKeyPairFile()
	{
		return new File(getStartupConfigDirectory(), getKeypairFilename());
	}

	File getComplianceFile()
	{
		return new File(getStartupConfigDirectory(), COMPLIANCESTATEMENTFILENAME);
	}

	public File getShutdownFile()
	{
		return new File(getTriggerDirectory(), MARTUSSHUTDOWNFILENAME);
	}

	public File getTriggerDirectory()
	{
		return new File(getDataDirectory(), ADMINTRIGGERDIRECTORY);
	}

	public File getStartupConfigDirectory()
	{
		return new File(getDataDirectory(),ADMINSTARTUPCONFIGDIRECTORY);
	}

	private File getHiddenPacketsFile()
	{
		return new File(getStartupConfigDirectory(), HIDDENPACKETSFILENAME);
	}
		
	public File getDataDirectory()
	{
		return dataDirectory;
	}

	boolean isRunningUnderWindows()
	{
		return Version.isRunningUnderWindows();
	}
	
	boolean hasTimeExpired(long previousTime, long elapsedTimeInMillisToCheck)
	{
		return (System.currentTimeMillis() - previousTime) > elapsedTimeInMillisToCheck;
	}

	public static void setThreadCallerAccountId(String newCallerAccountId) 
	{
		if(callerAccountId == null)
			callerAccountId = new ThreadLocal<String>();
		
		callerAccountId.set(newCallerAccountId);
	}

	public static String getCallerAccountId()
	{
		if(callerAccountId == null)
			return null;
		
		return callerAccountId.get();
	}

	abstract private class BackgroundServerTimerTask extends TimerTask
	{
		BackgroundServerTimerTask(String threadNameToUse)
		{
			threadName = threadNameToUse;
		}

		void verifyTimerAlive()
		{
			updateLastInvokedTime();

			if(hasTimeExpired(getLastCheckedTime(), 3 * MILLIS_IN_ONE_MINUTE))
			{
				logDebug(getThreadName() + ": Still Alive");
				updateLastCheckedTime();
			}
		}

		long getLastCheckedTime()
		{
			return lastCheckedInvokedAtMillis;
		}

		long getLastInvokedAtTimeMillis()
		{
			return lastInvokedAt;
		}
		
		String getThreadName()
		{
			return threadName;
		}
		
		private void updateLastInvokedTime()
		{
			lastInvokedAt = System.currentTimeMillis();
		}
		
		private void updateLastCheckedTime()
		{
			lastCheckedInvokedAtMillis = System.currentTimeMillis();
		}
		
		private long lastInvokedAt;
		private String threadName;
		private long lastCheckedInvokedAtMillis = 0;
	}
	
	private class TimerWatchDogTask extends TimerTask
	{
		TimerWatchDogTask(Vector timers)
		{
			timersToWatch = timers;
		}
		
		public void run()
		{
			for(int i = 0; i < timersToWatch.size(); ++i)
			{
				BackgroundServerTimerTask timerToCheck = (BackgroundServerTimerTask)timersToWatch.get(i);
				long timerLastInvoked = timerToCheck.getLastInvokedAtTimeMillis();
				if(hasTimeExpired(timerLastInvoked, MILLIS_IN_ONE_HOUR))
				{
					Timestamp stamp = new Timestamp(timerLastInvoked);
					SimpleDateFormat formatDate = new SimpleDateFormat(LoggerToConsole.LOG_DATE_FORMAT);
					logError(timerToCheck.getThreadName() + ": Timer may be wedged, last invoked " +formatDate.format(stamp));
				}
			}
			
			monitorActiveRunners();
		}

		private void monitorActiveRunners()
		{
			int[] activeRunnerCounts = serverForClients.getActiveRunnerCounts();
			String countText = "Runner counts: ";
			for(int i = 0; i < activeRunnerCounts.length; ++i)
			{
				int thisCount = activeRunnerCounts[i];
				if(thisCount > 100)
					logWarning("Active runner possible leak, now at " + thisCount + " out of 255");
				countText += "  " + thisCount;
			}
			logNotice(countText);
		}
		Vector timersToWatch;
	}

	private class UploadRequestsMonitor extends BackgroundServerTimerTask
	{
		UploadRequestsMonitor()
		{
			super("UploadRequestsMonitor");
		}
		
		public void run()
		{
			verifyTimerAlive();
			Iterator failedUploadReqIps = failedUploadRequestsPerIp.keySet().iterator();
			while(failedUploadReqIps.hasNext())
			{
				String ip = (String) failedUploadReqIps.next();
				subtractMaxFailedUploadRequestsForIp(ip);
			}
		}
	}
	
	private class ShutdownRequestMonitor extends BackgroundServerTimerTask
	{
		ShutdownRequestMonitor()
		{
			super("ShutdownRequestMonitor");
		}
		
		public void run()
		{
			verifyTimerAlive();
			if( isShutdownRequested() && canExitNow() )
			{
				logNotice("Shutdown request acknowledged, preparing to shutdown.");
				
				serverForClients.prepareToShutdown();				
				getShutdownFile().delete();
				logNotice("Server has exited.");
				try
				{
					serverExit(0);
				}
				catch (Exception e)
				{
					logError(e);
				}
			}
		}
	}
	
	class SyncAmplifierWithServersMonitor extends BackgroundServerTimerTask
	{	
		SyncAmplifierWithServersMonitor()
		{
			super("SyncAmplifierWithServers");
		}
		
		public void run()
		{
			verifyTimerAlive();
			if(isShutdownRequested())
				return;

			MartusServer.needsAmpSync = true;
		}
	}

	private class BackgroundTimerTick extends BackgroundServerTimerTask
	{
		BackgroundTimerTick()
		{
			super("BackgroundTimerTick - Mirroring data pull");
		}

		public void run()
		{
			verifyTimerAlive();
			protectedRun();
		}
		
		synchronized void protectedRun()
		{
			if(isShutdownRequested())
				return;

			if(isMirrorListenerEnabled())
				mirroringRetrieverManager.doBackgroundTick();

			if(MartusServer.needsAmpSync)
			{
				logDebug("amp.pullNewDataFromServers(): Begin Pull");
				amp.pullNewDataFromNextServer();
				MartusServer.needsAmpSync = false;
				logDebug("amp.pullNewDataFromServers(): End Pull");
			}
		}
	}
	
	private static ThreadLocal<String> callerAccountId;

	ServerForMirroring serverForMirroring;
	protected MirrorPuller mirroringRetrieverManager;
	public ServerForClients serverForClients;
	public ServerForAmplifiers serverForAmplifiers;
	public MartusAmplifier amp;
	private boolean amplifierEnabled;
	static boolean needsAmpSync; 
	private boolean clientListenerEnabled;
	private boolean mirrorListenerEnabled;
	private boolean amplifierListenerEnabled;
	
	private File dataDirectory;
	private ServerBulletinStore store;
	private String complianceStatement; 
	
	Hashtable failedUploadRequestsPerIp;
	
	private LoggerInterface logger;
	String serverName;
	
	private boolean secureMode;
	private static String listenersIpAddress; 
	private String ampIpAddress;
	public boolean simulateBadConnection;
	public boolean useEmbeddedPresentationFiles;
	private boolean loggedShutdownRequested;
	public String tokenAuthorityBase;
	
	public char[] insecurePassword;
	public long amplifierDataSynchIntervalMillis;
	
	private static final String KEYPAIRFILENAME = "keypair.dat";
	public static final String HIDDENPACKETSFILENAME = "isHidden.txt";
	private static final String COMPLIANCESTATEMENTFILENAME = "compliance.txt";
	private static final String MARTUSSHUTDOWNFILENAME = "exit";
	
	private static final String ADMINTRIGGERDIRECTORY = "adminTriggers";
	private static final String ADMINSTARTUPCONFIGDIRECTORY = "deleteOnStartup";
	
	private static final long MILLIS_IN_ONE_SECOND = 1000;
	private static final long MILLIS_PER_MINUTE = 60 * MILLIS_IN_ONE_SECOND;
	private static final long MILLIS_IN_ONE_MINUTE = 1 * MILLIS_PER_MINUTE;
	private static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
	private static final long MILLIS_IN_ONE_HOUR = 1 * MILLIS_PER_HOUR;

	private final static int MAX_FAILED_UPLOAD_ATTEMPTS = 100;
	private final static long mainTickDelayMillis = 1 * MILLIS_IN_ONE_MINUTE;
	private final static long mainTickIntervalMillis = 250;
	
	private static final long shutdownRequestIntervalMillis = MILLIS_IN_ONE_SECOND;
	private static final long magicWordsGuessIntervalMillis = MILLIS_IN_ONE_MINUTE;
	private static final long timerWatchDogIntervalMillis = (long)(1.5 * MILLIS_IN_ONE_HOUR);
}
