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

package org.martus.server.formirroring;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.InvalidPublicKeyFileException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.BulletinUploadRecord;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.network.MartusXmlRpcServer;
import org.martus.common.network.mirroring.MirroringInterface;
import org.martus.common.network.mirroring.SupplierSideMirroringInterface;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.server.main.MartusServer;
import org.martus.server.main.ServerBulletinStore;
import org.martus.util.DirectoryUtils;
import org.martus.util.LoggerUtil;
import org.martus.util.StreamableBase64;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


public class ServerForMirroring implements ServerSupplierInterface
{
	public ServerForMirroring(MartusServer coreServerToUse, LoggerInterface loggerToUse) throws IOException, InvalidPublicKeyFileException, PublicInformationInvalidException  
	{
		coreServer = coreServerToUse;
		logger = loggerToUse;
	}
	
	public ServerBulletinStore getStore()
	{
		return coreServer.getStore();
	}

	public Vector getDeleteOnStartupFiles()
	{
		Vector startupFiles = new Vector();
		return startupFiles;
	}

	public Vector getDeleteOnStartupFolders()
	{
		Vector startupFolders = new Vector();
		startupFolders.add(getAuthorizedCallersDirectory());
		return startupFolders;
	}
	
	public void deleteStartupFiles()
	{
		DirectoryUtils.deleteEntireDirectoryTree(getDeleteOnStartupFolders());
		MartusUtilities.deleteAllFiles(getDeleteOnStartupFiles());
	}
	
	private String createLogString(String message)
	{
		return "MirrorHandler " + message;
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
	
	
	public void verifyConfigurationFiles()
	{
		// nothing to do yet
	}
	
	public void loadConfigurationFiles() throws IOException, InvalidPublicKeyFileException, PublicInformationInvalidException
	{
		File authorizedCallersDir = getAuthorizedCallersDirectory();
		authorizedCallers = coreServer.loadServerPublicKeys(authorizedCallersDir, "Mirror");
		logNotice("Authorized " + authorizedCallers.size() + " Mirrors to call us");
	}
	
	public void addListeners() throws IOException, InvalidPublicKeyFileException, PublicInformationInvalidException
	{
		logInfo("Initializing ServerForMirroring");
		
		InetAddress mainIpAddress = MartusServer.getMainIpAddress();
		int port = getPort();
		logNotice("Opening port " + mainIpAddress +":" + port + " for mirroring...");
		SupplierSideMirroringHandler supplierHandler = new SupplierSideMirroringHandler(this, getSecurity());
		MartusXmlRpcServer.createSSLXmlRpcServer(supplierHandler, SupplierSideMirroringInterface.class, MirroringInterface.DEST_OBJECT_NAME, port, mainIpAddress);

		logNotice("Mirroring port opened");
	}

	private int getPort() 
	{
		int[] ports = new int[] {MirroringInterface.MARTUS_PORT_FOR_MIRRORING};
		ports = coreServer.shiftToDevelopmentPortsIfNotInSecureMode(ports);
		return ports[0];
	}

	// Begin ServerSupplierInterface
	public Vector getPublicInfo()
	{
		try
		{
			Vector result = new Vector();
			result.add(getSecurity().getPublicKeyString());
			result.add(getSecurity().getSignatureOfPublicKey());
			return result;
		}
		catch (Exception e)
		{
			logError(e);
			return new Vector();
		}
	}
	
	public boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return authorizedCallers.contains(callerAccountId);
	}

	public Vector listAccountsForMirroring()
	{
		class Collector implements Database.AccountVisitor
		{
			public void visit(String accountId)
			{
				accounts.add(accountId);
			}
			
			Vector accounts = new Vector();
		}

		Collector collector = new Collector();		
		getDatabase().visitAllAccounts(collector);
		return collector.accounts;
	}

	public Vector listBulletinsForMirroring(String authorAccountId)
	{
		class Collector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				try
				{
					if(key.isDraft())
						return;
					if(!BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
						return;
					InputStreamWithSeek in = getDatabase().openInputStream(key, null);
					byte[] sigBytes = BulletinHeaderPacket.verifyPacketSignature(in, getSecurity());
					in.close();
					String sigString = StreamableBase64.encode(sigBytes);
					Vector info = new Vector();
					info.add(key.getLocalId());
					info.add(sigString);
					infos.add(info);
				}
				catch (Exception e)
				{
					logError("listBulletins ", e);
				}
			}
			
			Vector infos = new Vector();
		}

		Collector collector = new Collector();		
		getDatabase().visitAllRecordsForAccount(collector, authorAccountId);
		return collector.infos;
	}
	
	public Set listAvailableIdsForMirroring(String authorAccountId)
	{
		class Collector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				try
				{
					if(!BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
						return;
					InputStreamWithSeek in = getDatabase().openInputStream(key, null);
					byte[] sigBytes = BulletinHeaderPacket.verifyPacketSignature(in, getSecurity());
					in.close();
					String sigString = StreamableBase64.encode(sigBytes);
					BulletinMirroringInformation bulletinInfo = new BulletinMirroringInformation(getDatabase(), key, sigString);
					infos.add(bulletinInfo.getInfoWithLocalId());
				}
				catch (Exception e)
				{
					logError("listAvailableIdsForMirroring " + e.getMessage(), e);
				}
			}
			
			Set infos = new HashSet();
		}

		Collector collector = new Collector();		
		getDatabase().visitAllRecordsForAccount(collector, authorAccountId);
		return collector.infos;
	}

	@Override
	public Vector listAvailableFormTemplateInfos(String authorAccountId) 
	{
		Vector result = new Vector();
		try 
		{
			Vector templateFiles = getStore().getListOfFormTemplatesForAccount(authorAccountId);
			Vector templateInfosVector = extractTemplateInfosFromFiles(templateFiles);
			return templateInfosVector;
		} 
		catch (Exception e) 
		{
			logger.logError("Exception getting list of form templates", e);
		}
		return result;
	}

	public Vector getFormTemplate(String authorAccountId, String templateFilename)
	{
		Vector templateVector = new Vector();
		try {
			File templateFile = getStore().getFormTemplateFileFromAccount(authorAccountId, templateFilename);
			String base64Template = StreamableBase64.readAllAndEncodeBase64(templateFile);
			templateVector.add(base64Template);
		} 
		catch (Exception e) 
		{
			logError(e);
		}
		return templateVector;
	}
	
	private Vector<String> extractTemplateInfosFromFiles(Vector templateFiles) throws Exception
	{
		Vector<String> infos = new Vector<String>();
		for (Object rawFile : templateFiles) 
		{
			File file = (File)rawFile;
			TemplateInfoForMirroring info = new TemplateInfoForMirroring(file);
			infos.add(info.asString());
		}
		return infos;
	}

	public String getBulletinUploadRecord(String authorAccountId, String bulletinLocalId)
	{
		DatabaseKey headerKey = coreServer.findHeaderKeyInDatabase(authorAccountId, bulletinLocalId);
		DatabaseKey burKey = BulletinUploadRecord.getBurKey(headerKey);
		try
		{
			String bur = getDatabase().readRecord(burKey, getSecurity());
			return bur;
		}
		catch (Exception e)
		{
			logError(e);
		}
		return null;
	}
	
	public Vector getBulletinChunkWithoutVerifyingCaller(String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize)
	{
		return coreServer.getBulletinChunkWithoutVerifyingCaller(authorAccountId, bulletinLocalId, chunkOffset, maxChunkSize);
	}
	//End ServerSupplierInterface

	MartusCrypto getSecurity()
	{
		return coreServer.getSecurity();
	}

	ReadableDatabase getDatabase()
	{
		return coreServer.getDatabase();
	}
	
	boolean isSecureMode()
	{
		return coreServer.isSecureMode();
	}

	File getAuthorizedCallersDirectory()
	{
		return new File(coreServer.getStartupConfigDirectory(), "mirrorsWhoCallUs");
	}
	
	
	MartusServer coreServer;
	LoggerInterface logger;
	Vector authorizedCallers;
	MirroringRetriever retriever;
}
