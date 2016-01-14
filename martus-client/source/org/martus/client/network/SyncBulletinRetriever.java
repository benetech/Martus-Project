/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.client.network;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.DatabaseKey;
import org.martus.common.network.ClientSideNetworkInterface;
import org.martus.common.network.ServerBulletinSummary;
import org.martus.common.network.ShortServerBulletinSummary;
import org.martus.common.network.SummaryOfAvailableBulletins;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.DateUtilities;

interface ThreadMonitor
{
	void updateNextTimestamp(String nextTimestamp) throws Exception;
	void threadHasFinished();
	void threadHadException(Exception e);
}

public class SyncBulletinRetriever implements ThreadMonitor
{
	public SyncBulletinRetriever(MartusApp appToUse) throws Exception
	{
		app = appToUse;
		String jsonString = app.getConfigInfo().getSyncStatusJson();
		syncStatus = new SyncStatus(jsonString);
	}
	
	public boolean isBusy()
	{
		return (actualRetriever != null);
	}
	
	public boolean hadException()
	{
		return (exceptionToReport != null);
	}
	
	public Exception getAndClearException()
	{
		Exception e = exceptionToReport;
		exceptionToReport = null;
		return e;
	}
	
	@Override
	public void updateNextTimestamp(String nextTimestamp) throws Exception
	{
		String server = getServer();

		syncStatus.setServerTimestamp(server, nextTimestamp);
		String newSyncStatusJson = syncStatus.toJsonString();
		MartusLogger.log("UpdateNextTimestamp " + server + ": " + newSyncStatusJson);
		app.getConfigInfo().setSyncStatusJson(newSyncStatusJson);
		app.saveConfigInfo();
	}

	@Override
	public void threadHasFinished()
	{
		actualRetriever = null;
	}
	
	@Override
	public void threadHadException(Exception e)
	{
		exceptionToReport = e;
	}
	
	public String getNextTimestampToAskForAvailableBulletins()
	{
		return syncStatus.getServerTimestamp(getServer());
	}

	public void startRetrieve(SummaryOfAvailableBulletins bulletinsToRetrieve) throws Exception
	{
		if(isBusy())
			throw new AlreadyRetrievingException();
		
		actualRetriever = new ActualRetrieverThread(app, this, bulletinsToRetrieve);
		new Thread(actualRetriever).start();
	}
	
	private String getServer()
	{
		ClientSideNetworkInterface csni = app.getCurrentNetworkInterfaceGateway().getInterface();
		return csni.getServerIpAddress();
	}

	private static class ActualRetrieverThread implements Runnable
	{
		public ActualRetrieverThread(MartusApp appToUse, ThreadMonitor monitorToUse, SummaryOfAvailableBulletins bulletinsToRetrieve) throws Exception
		{
			app = appToUse;
			monitor = monitorToUse;
			summaryOfAvailableBulletins = bulletinsToRetrieve;
			toRetrieve = new Vector<ServerBulletinSummary>();
			String folderName = "%RetrievedBulletins";
			destinationFolder = getApp().getStore().createOrFindFolder(folderName);
			buildListToRetrieve();
		}
		
		@Override
		public void run()
		{
			try
			{
				retrieve();
			}
			catch(Exception e)
			{
				monitor.threadHadException(e);
			}
			finally
			{
				monitor.threadHasFinished();
			}
		}
		
		private void buildListToRetrieve() throws Exception
		{
			toRetrieve.clear();
			Iterator<String> accountsIterator = summaryOfAvailableBulletins.getAccountIds().iterator();
			while(accountsIterator.hasNext())
			{
				String accountId = accountsIterator.next();
				addAccountBulletinsToVector(accountId);
			}
			
			toRetrieve.sort(new ByServerDateComparator());
		}
		
		private void addAccountBulletinsToVector(String accountId) throws Exception
		{
			Set<ShortServerBulletinSummary> summariesForAccount = summaryOfAvailableBulletins.getSummaries(accountId);
			Iterator<ShortServerBulletinSummary> bulletinIterator = summariesForAccount.iterator();
			while(bulletinIterator.hasNext())
			{
				ShortServerBulletinSummary shortSummary = bulletinIterator.next();
				if(shouldRetrieve(accountId, shortSummary))
				{
					String localId = shortSummary.getLocalId();
					UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);
					ServerBulletinSummary fullSummary = new ServerBulletinSummary(uid, shortSummary);
					toRetrieve.add(fullSummary);
				}
			}
		}
		
		private void retrieve() throws Exception
		{
			MartusLogger.log("Sync retrieving " + toRetrieve.size() + " revisions");
			Iterator<ServerBulletinSummary> it = toRetrieve.iterator();
			while(it.hasNext())
			{
				ServerBulletinSummary summary = it.next();
				monitor.updateNextTimestamp(summary.getServerTimestamp());
				retrieveBulletin(summary);
			}

			monitor.updateNextTimestamp(summaryOfAvailableBulletins.getNextServerTimestamp());
		}
		
		private void retrieveBulletin(ServerBulletinSummary bulletinSummary) throws Exception
		{
			UniversalId uid = bulletinSummary.getUniversalId();
			String accountId = uid.getAccountId();
			String localId = uid.getLocalId();
			
			String publicCode = MartusCrypto.computeFormattedPublicCode40(accountId);
			MartusLogger.log("Retrieving " + publicCode + localId);
			
			getApp().retrieveOneBulletinToFolder(uid, destinationFolder, null);
		}

		private boolean shouldRetrieve(String accountId, ShortServerBulletinSummary summary) throws Exception
		{
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, summary.getLocalId());
			DatabaseKey key = DatabaseKey.createLegacyKey(uid);
			if(!getStore().doesBulletinRevisionExist(key))
				return true;

			String lastModifiedIsoDateTime = summary.getLastModified();
			long serverLastModified = DateUtilities.parseIsoDateTime(lastModifiedIsoDateTime).getTime();
			Bulletin localBulletin = getStore().getBulletinRevision(uid);
			long localLastModified = localBulletin.getLastSavedTime();
			
			String myAccountId = getApp().getSecurity().getPublicKeyString();
			boolean isMyBulletin = (myAccountId.equals(accountId));
			boolean serverCopyIsNewer = serverLastModified > localLastModified;
			if(serverCopyIsNewer)
				return true;
			
			boolean serverCopyIsOlder = serverLastModified < localLastModified;
			if(!isMyBulletin && serverCopyIsOlder)
				return true;
			
			return false;
		}
		
		private MartusApp getApp()
		{
			return app;
		}
		
		private ClientBulletinStore getStore()
		{
			return getApp().getStore();
		}
		
		static class ByServerDateComparator implements Comparator<ServerBulletinSummary>
		{
			@Override
			public int compare(ServerBulletinSummary sbs1, ServerBulletinSummary sbs2)
			{
				if(sbs1 == sbs2)
					return 0;
				if(sbs1 == null)
					return -1;
				if(sbs2 == null)
					return 1;
				return sbs1.getServerTimestamp().compareTo(sbs2.getServerTimestamp());
			}
			
		}

		private MartusApp app;
		private ThreadMonitor monitor;
		private SummaryOfAvailableBulletins summaryOfAvailableBulletins;
		private BulletinFolder destinationFolder;
		private Vector<ServerBulletinSummary> toRetrieve;
	}
	
	public static class AlreadyRetrievingException extends Exception
	{
	}

	private MartusApp app;
	private Runnable actualRetriever;
	private SyncStatus syncStatus; 
	private Exception exceptionToReport;
}

