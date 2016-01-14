/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2014, Beneficent
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
import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.InvalidPublicKeyFileException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.network.mirroring.CallerSideMirroringGateway;
import org.martus.common.network.mirroring.CallerSideMirroringGatewayForXmlRpc;
import org.martus.common.network.mirroring.MirroringInterface;
import org.martus.common.network.mirroring.CallerSideMirroringGatewayForXmlRpc.SSLSocketSetupException;
import org.martus.server.main.MartusServer;
import org.martus.server.main.ServerBulletinStore;
import org.martus.util.DirectoryUtils;
import org.martus.util.LoggerUtil;

public class MirrorPuller 
{
	public MirrorPuller(MartusServer coreServerToUse, LoggerInterface loggerToUse) throws Exception  
	{
		coreServer = coreServerToUse;
		logger = loggerToUse;
	}
	
	public void createMirroringRetrievers() throws Exception
	{
		retrieversWeWillCall = new Vector();

		File toCallDir = getMirrorsWeWillCallDirectory();
		File[] toCallFiles = toCallDir.listFiles();
		if (toCallFiles == null)
			return;
		for (int i = 0; i < toCallFiles.length; i++) 
		{
			File toCallFile = toCallFiles[i];
			retrieversWeWillCall.add(createRetrieverToCall(toCallFile));
			if (isSecureMode()) 
			{
				toCallFile.delete();
				if (toCallFile.exists())
					throw new IOException("delete failed: " + toCallFile);
			}
			logNotice("We will call: " + toCallFile.getName());
		}
		logNotice("Configured to call " + retrieversWeWillCall.size()
				+ " Mirrors");
	}

	File getMirrorsWeWillCallDirectory()
	{
		return new File(coreServer.getStartupConfigDirectory(), "mirrorsWhoWeCall");		
	}
	
	MirroringRetriever createRetrieverToCall(File publicKeyFile) throws
			IOException, 
			InvalidPublicKeyFileException, 
			PublicInformationInvalidException, 
			SSLSocketSetupException
	{
		String ip = MartusUtilities.extractIpFromFileName(publicKeyFile.getName());
		CallerSideMirroringGateway gateway = createGatewayToCall(ip, publicKeyFile);
		return new MirroringRetriever(getStore(), gateway, ip, logger);
	}
	
	CallerSideMirroringGateway createGatewayToCall(String ip, File publicKeyFile) throws 
			IOException, 
			InvalidPublicKeyFileException, 
			PublicInformationInvalidException, 
			SSLSocketSetupException
	{
		int port = getPort();
		
		Vector publicInfo = MartusUtilities.importServerPublicKeyFromFile(publicKeyFile, getSecurity());
		String publicKey = (String)publicInfo.get(0);

		CallerSideMirroringGatewayForXmlRpc xmlRpcGateway = new CallerSideMirroringGatewayForXmlRpc(ip, port); 
		xmlRpcGateway.setExpectedPublicKey(publicKey);
		return new CallerSideMirroringGateway(xmlRpcGateway);
	}

	private int getPort() 
	{
		int[] ports = new int[] {MirroringInterface.MARTUS_PORT_FOR_MIRRORING};
		ports = coreServer.shiftToDevelopmentPortsIfNotInSecureMode(ports);
		return ports[0];
	}

	public void doBackgroundTick()
	{
		for(int i = 0; i < retrieversWeWillCall.size(); ++i)
		{	
			((MirroringRetriever)retrieversWeWillCall.get(i)).pullEverything();
		}
	}
	
	public Vector getDeleteOnStartupFiles()
	{
		Vector startupFiles = new Vector();
		return startupFiles;
	}

	public Vector getDeleteOnStartupFolders()
	{
		Vector startupFolders = new Vector();
		startupFolders.add(getMirrorsWeWillCallDirectory());
		return startupFolders;
	}
	
	public void deleteStartupFiles()
	{
		DirectoryUtils.deleteEntireDirectoryTree(getDeleteOnStartupFolders());
		MartusUtilities.deleteAllFiles(getDeleteOnStartupFiles());
	}
	
	private String createLogString(String message)
	{
		return "MirrorPuller " + message;
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
	
	private boolean isSecureMode()
	{
		return coreServer.isSecureMode();
	}

	private MartusCrypto getSecurity()
	{
		return coreServer.getSecurity();
	}

	public ServerBulletinStore getStore()
	{
		return coreServer.getStore();
	}


	private MartusServer coreServer;
	Vector retrieversWeWillCall;		// NOTE: Accessed directly by tests
	private LoggerInterface logger;
}
