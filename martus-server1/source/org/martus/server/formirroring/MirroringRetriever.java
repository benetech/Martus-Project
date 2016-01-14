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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.DeleteRequestRecord;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.mirroring.CallerSideMirroringGatewayInterface;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.server.forclients.ServerForClients;
import org.martus.server.main.ServerBulletinStore;
import org.martus.util.LoggerUtil;

public class MirroringRetriever implements LoggerInterface
{
	public MirroringRetriever(ServerBulletinStore storeToUse, CallerSideMirroringGatewayInterface gatewayToUse, 
						String ipToUse, LoggerInterface loggerToUse)
	{
		store = storeToUse;
		gateway = gatewayToUse;
		ip = ipToUse;
		logger = loggerToUse;
		
		itemsToRetrieve = new Vector();
		accountsToRetrieve = null;
	}
	
	public void pullEverything()
	{
		if(isSleeping())
			return;
		
		while(!isSleeping())
		{
			pullNextBulletin();
		}

		pullAllTemplates();
	}
	
	public void pullAllTemplates()
	{
		logInfo("Pulling templates");
		try 
		{
			Vector accounts = getListOfAccounts();
			for(int accountIndex = 0; accountIndex < accounts.size(); ++accountIndex)
			{
				String accountId = (String)accounts.get(accountIndex);
				Vector templates = getListOfTemplatesForAccount(accountId);
				for(int templateIndex = 0; templateIndex < templates.size(); ++templateIndex)
				{
					String templateInfo = (String)templates.get(templateIndex);
					pullTemplateIfNeeded(accountId, templateInfo);
				}
			}
		} 
		catch (ServerErrorException e)
		{
			if(e.getMessage().equals(NetworkInterfaceConstants.NO_SERVER))
				return;
		}
		catch (Exception e) 
		{
			logError("Mirror call exception ", e);
		}
	}

	public Vector getListOfAccounts() throws Exception 
	{
		NetworkResponse response = gateway.listAccountsForMirroring(getSecurity());
		String resultCode = response.getResultCode();
		if(!resultCode.equals(NetworkInterfaceConstants.OK))
		{
			logError("listAccountsForMirroring returned " + resultCode);
			throw new ServerErrorException(resultCode);
		}
		Vector accounts = response.getResultVector();
		return accounts;
	}
	
	private Vector getListOfTemplatesForAccount(String accountId)  throws Exception
	{
		NetworkResponse response = gateway.getListOfFormTemplateInfos(getSecurity(), accountId);
		String resultCode = response.getResultCode();
		if(!resultCode.equals(NetworkInterfaceConstants.OK))
		{
			logError("getListOfFormTemplates returned " + resultCode);
			throw new ServerErrorException(resultCode);
		}
		return response.getResultVector();
	}

	private void pullTemplateIfNeeded(String accountId, String templateInfoString) throws Exception
	{
		TemplateInfoForMirroring templateInfo = new TemplateInfoForMirroring(templateInfoString);
		if(shouldPullTemplate(accountId, templateInfo))
			pullAndSaveTemplate(accountId, templateInfo);
	}

	private void pullAndSaveTemplate(String accountId, TemplateInfoForMirroring templateInfo) throws Exception
	{
		String publicCode = MartusCrypto.computeFormattedPublicCode(accountId);
		logInfo("Pulling template: " + publicCode + ": " + templateInfo.getFilename());
		
		String base64Template = pullTemplate(accountId, templateInfo);
		ServerForClients.saveBase64FormTemplate(store, accountId, base64Template, getSecurity(), logger);
		File file = store.getFormTemplateFileFromAccount(accountId, templateInfo.getFilename());
		long time = templateInfo.getLastModifiedMillis();
		file.setLastModified(time);
		logDebug("Set mtime of " + file.getAbsolutePath());
		logDebug(time + "->" + file.lastModified());
	}

	public String pullTemplate(String accountId, TemplateInfoForMirroring templateInfo) throws Exception
	{
		NetworkResponse response = gateway.getFormTemplate(getSecurity(), accountId, templateInfo.getFilename());
		String resultCode = response.getResultCode();
		if(!resultCode.equals(NetworkInterfaceConstants.OK))
		{
			logError("getListOfFormTemplates returned " + resultCode);
			throw new ServerErrorException(resultCode);
		}
		Vector templateVector = response.getResultVector();
		return (String)templateVector.get(0);
	}

	protected boolean shouldPullTemplate(String accountId, TemplateInfoForMirroring templateInfo) throws Exception
	{
		String filename = templateInfo.getFilename();
		try
		{
			File file = store.getFormTemplateFileFromAccount(accountId, filename);
			if(!file.exists())
				return true;
			
			TemplateInfoForMirroring ourTemplateInfo = new TemplateInfoForMirroring(file);
			
			return shouldPullTemplate(ourTemplateInfo, templateInfo);
		}
		catch (FileNotFoundException harmlessExpected)
		{
			return true;
		}
	}

	public static boolean shouldPullTemplate(TemplateInfoForMirroring ourTemplateInfo, TemplateInfoForMirroring availableTemplateInfo) 
	{
		long availableMillis = availableTemplateInfo.getLastModifiedMillis();
		long ourMillis = ourTemplateInfo.getLastModifiedMillis();
		if (ourMillis < availableMillis)
			return true;
		if (ourMillis > availableMillis)
			return false;

		long availableSize = availableTemplateInfo.getFileSize();
		long ourSize = ourTemplateInfo.getFileSize();
		if(ourSize < availableSize)
			return true;
		
		return false;
	}

	public void pullNextBulletin()
	{
		if(isSleeping())
			return;

		BulletinMirroringInformation item = getNextItemToRetrieve();
		if(item == null)
		{
			scheduleSleep();
			return;
		}
			
		//TODO handle delete requests when we are propagating deletes.
		
		try
		{
			UniversalId uid = item.getUid();
			String publicCode = MartusCrypto.getFormattedPublicCode(uid.getAccountId());
			logNotice("Getting bulletin: " + publicCode + "->" + uid.getLocalId());
			String bur = retrieveBurFromMirror(uid);
			File zip = File.createTempFile("$$$MirroringRetriever", null);
			try
			{
				zip.deleteOnExit();
				retrieveOneBulletin(zip, uid);
				long zipSize = zip.length();
				long mTime = item.getmTime();
				BulletinHeaderPacket bhp = store.saveZipFileToDatabase(zip, uid.getAccountId(), mTime);
				store.writeBur(bhp, bur);
				store.deleteDel(bhp.getUniversalId());
				logNotice("Stored bulletin:  " + publicCode + "->" + uid.getLocalId() + " Size: " + zipSize);
			}
			finally
			{
				zip.delete();
			}
		}
		catch(ServerErrorException e)
		{
			logError("Supplier server:", e);
		}
		catch(ServerNotAvailableException e)
		{
			// TODO: Notify once per hour that something is wrong
		}
		catch (Exception e)
		{
			logError(e);
		}
		
	}

	protected BulletinMirroringInformation getNextItemToRetrieve()
	{
		try
		{
			while(itemsToRetrieve.size() == 0)
			{
				String nextAccountId = getNextAccountToRetrieve();
				if(nextAccountId == null)
					return null;
	
				int totalIdsReturned = 0;
				String mirroringCallUsed = "listAvailableIdsForMirroring"; 
				NetworkResponse response = gateway.listAvailableIdsForMirroring(getSecurity(), nextAccountId);
				if(networkResponseOk(response))
				{
					Vector listwithBulletinMirroringInfo = response.getResultVector();
					totalIdsReturned = listwithBulletinMirroringInfo.size();
					itemsToRetrieve = listOnlyPacketsThatWeWantUsingBulletinMirroringInformation(nextAccountId, listwithBulletinMirroringInfo);
				}
				else
				{
					mirroringCallUsed = "OLD MIRRORING CALL(listBulletinsForMirroring)";
					response = gateway.listBulletinsForMirroring(getSecurity(), nextAccountId);
					if(networkResponseOk(response))
					{
						Vector listWithLocalIds = response.getResultVector();
						totalIdsReturned = listWithLocalIds.size();
						itemsToRetrieve = listOnlyPacketsThatWeWantUsingLocalIds(nextAccountId, listWithLocalIds);
					}
				}
				
				if(networkResponseOk(response))
				{
					String publicCode = MartusCrypto.getFormattedPublicCode(nextAccountId);
					if(totalIdsReturned>0 || itemsToRetrieve.size()>0)
						logInfo(mirroringCallUsed+": " + publicCode + 
							" -> " + totalIdsReturned+ " -> " + itemsToRetrieve.size());
				}
				else
				{
					logWarning("MirroringRetriever.getNextItemToRetrieve: Returned NetworkResponse: " + response.getResultCode());				
				}
			}

			if(itemsToRetrieve.size() == 0)
				return null;
			
			return (BulletinMirroringInformation)itemsToRetrieve.remove(0);

		}
		catch (Exception e)
		{
			logError("MirroringRetriever.getNextUidToRetrieve: ",e);
			MartusLogger.logException(e);
			return null;
		}
	}

	protected String getNextAccountToRetrieve()
	{
		try
		{
			if(accountsToRetrieve == null)
			{
				logInfo("Getting list of accounts");
				NetworkResponse response = gateway.listAccountsForMirroring(getSecurity());
				String resultCode = response.getResultCode();
				if(resultCode.equals(NetworkInterfaceConstants.OK))
				{
					accountsToRetrieve = new Vector(response.getResultVector());
					logNotice("Account count:" + accountsToRetrieve.size());
				}
				else
				{
					logError("error returned by " + ip + ": " + resultCode);
				}
			}

			if(accountsToRetrieve == null || accountsToRetrieve.size() == 0)
			{
				accountsToRetrieve = null;
				return null;
			}
			
			return (String)accountsToRetrieve.remove(0);
		}
		catch (Exception e)
		{
			logError("getNextAccountToRetrieve: ", e);
			return null;
		}
	}


	
	private String retrieveBurFromMirror(UniversalId uid)
		throws MartusSignatureException, MissingBulletinUploadRecordException, ServerNotAvailableException
	{
		NetworkResponse response = gateway.getBulletinUploadRecord(getSecurity(), uid);
		String resultCode = response.getResultCode();
		if(resultCode.equals(NetworkInterfaceConstants.NO_SERVER))
		{
			throw new ServerNotAvailableException();
		}
		if(!resultCode.equals(NetworkInterfaceConstants.OK))
		{
			throw new MissingBulletinUploadRecordException();
		}
		String bur = (String)response.getResultVector().get(0);
		return bur;
	}
	
	private boolean networkResponseOk(NetworkResponse response)
	{
		return response.getResultCode().equals(NetworkInterfaceConstants.OK);
	}
	
	protected Vector listOnlyPacketsThatWeWantUsingLocalIds(String accountId, Vector listWithLocalIds)
	{
		Vector mirroringInfo = new Vector();
		for(int i=0; i < listWithLocalIds.size(); ++i)
		{
			Object[] infos = (Object[])listWithLocalIds.get(i);
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, (String)infos[0]);
			BulletinMirroringInformation mirroringData = new BulletinMirroringInformation(uid);
			Vector infoWithLocalId = mirroringData.getInfoWithLocalId();
			mirroringInfo.add(infoWithLocalId.toArray());
		}
		
		return listOnlyPacketsThatWeWantUsingBulletinMirroringInformation(accountId, mirroringInfo);
	}

	protected Vector listOnlyPacketsThatWeWantUsingBulletinMirroringInformation(String accountId, Vector listWithMirroringInfo)
	{
		Vector dataToRetrieve = new Vector();
		for(int i=0; i < listWithMirroringInfo.size(); ++i)
		{
			if(listWithMirroringInfo.get(i) instanceof Vector)
				MartusLogger.log("Found vector instead of array");
			Object[] infoArray = (Object[])listWithMirroringInfo.get(i);
			Vector info = new Vector(Arrays.asList(infoArray));
			BulletinMirroringInformation mirroringInfo = new BulletinMirroringInformation(accountId, info);
			if(doWeWantThis(mirroringInfo))
				dataToRetrieve.add(mirroringInfo);
		}
		return dataToRetrieve;
	}

	public boolean doWeWantThis(BulletinMirroringInformation mirroringInfo)
	{
		//TODO handle delete requests when we are propagating deletes, 
		DatabaseKey key = getDatabaseKey(mirroringInfo);
		if(store.isHidden(key))
			return false;
		
		UniversalId uid = mirroringInfo.getUid();
		DatabaseKey sealedKey = DatabaseKey.createSealedKey(uid);
		if(store.doesBulletinRevisionExist(sealedKey))
			return false;
		
		try
		{
			if(mirroringInfo.isSealed())
				return (!store.doesBulletinRevisionExist(key));
			
			if(store.doesBulletinDelRecordExist(DeleteRequestRecord.getDelKey(uid)))
			{
				DeleteRequestRecord delRecord = new DeleteRequestRecord(store.getDatabase(), uid, store.getSignatureVerifier());
				if(delRecord.isBefore(mirroringInfo.mTime))
					return true;
				return false;
			}
		
			if(!store.doesBulletinRevisionExist(key))
				return true;
	
			long currentBulletinsmTime = store.getDatabase().getmTime(key);
			if(mirroringInfo.getmTime() > currentBulletinsmTime)
				return true;
			return false;
		}
		catch (Exception e)
		{
			logError(e.getMessage(), e);
		}
		return false;
	}

	private DatabaseKey getDatabaseKey(BulletinMirroringInformation mirroringInfo)
	{
		DatabaseKey key = null;
		if(mirroringInfo.isSealed())
			key = DatabaseKey.createSealedKey(mirroringInfo.getUid());
		else if(mirroringInfo.isDraft())
			key = DatabaseKey.createDraftKey(mirroringInfo.getUid());
		return key;
	}
	
	private void scheduleSleep() 
	{
		logNotice("Scheduling mirror sleep for " + ip + " of " + inactiveSleepMillis / 1000 / 60 + " minutes");
		sleepUntil = System.currentTimeMillis() + inactiveSleepMillis;
	}
	
	protected boolean isSleeping()
	{
		return System.currentTimeMillis() < sleepUntil;
	}
	
	protected void retrieveOneBulletin(File destFile, UniversalId uid)
			throws Exception
	{
		FileOutputStream out = new FileOutputStream(destFile);

		int chunkSize = MIRRORING_MAX_CHUNK_SIZE;
		ProgressMeterInterface nullProgressMeter = null;
		int totalLength = BulletinZipUtilities.retrieveBulletinZipToStream(uid,
				out, chunkSize, gateway, getSecurity(), nullProgressMeter);

		out.close();

		if (destFile.length() != totalLength)
		{
			logError("file=" + destFile.length() + ", returned=" + totalLength);
			throw new ServerErrorException("totalSize didn't match data length");
		}
	}
	
	private MartusCrypto getSecurity()
	{
		return store.getSignatureGenerator();
	}

	private String createLogString(String message)
	{
		return "Mirror calling " + ip + ": " + message;
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
	
	static class MissingBulletinUploadRecordException extends Exception
	{
	}
	
	static class ServerNotAvailableException extends Exception 
	{
	}

	private CallerSideMirroringGatewayInterface gateway;
	private String ip;
	private LoggerInterface logger;
	
	ServerBulletinStore store;	
	Vector itemsToRetrieve;
	Vector accountsToRetrieve;

	public static long inactiveSleepMillis = 15 * 60 * 1000;

	protected long sleepUntil;
	
	static final int MIRRORING_MAX_CHUNK_SIZE = 1024 * 1024;

}
