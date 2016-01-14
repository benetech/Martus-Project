/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2014, Beneficent
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

package org.martus.client.swingui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.bulletinstore.ClientBulletinStore.AddOlderVersionToFolderFailedException;
import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.client.network.BackgroundRetriever;
import org.martus.client.network.BackgroundUploader;
import org.martus.client.network.SyncBulletinRetriever;
import org.martus.client.swingui.jfx.generic.FxDialogHelper;
import org.martus.client.swingui.jfx.landing.general.SettingsForServerController;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.common.BulletinSummary;
import org.martus.common.BulletinSummary.WrongValueCount;
import org.martus.common.Exceptions.ServerCallFailedException;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.Exceptions.ServerNotCompatibleException;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusLogger;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.SummaryOfAvailableBulletins;
import org.martus.common.packet.UniversalId;
import org.miradi.utils.EnhancedJsonObject;

class BackgroundTimerTask extends TimerTask
{
	public BackgroundTimerTask(UiMainWindow mainWindowToUse, StatusBar statusBarToUse) throws Exception
	{
		mainWindow = mainWindowToUse;
		statusBar = statusBarToUse;
		ProgressMeterInterface progressMeter = getProgressMeter();
		uploader = new BackgroundUploader(mainWindow.getApp(), mainWindow, progressMeter);
		retriever = new BackgroundRetriever(getApp(), progressMeter);
		syncRetriever = new SyncBulletinRetriever(getApp());
		if(mainWindow.isServerConfigured() && getApp().getTransport().isOnline())
			setWaitingForServer();
		isSyncEnabled = true;
	}

	public ProgressMeterInterface getProgressMeter()
	{
		if(statusBar == null)
			return null;
		
		return statusBar.getBackgroundProgressMeter();
	}
	
	public void forceRecheckOfUidsOnServer()
	{
		gotUpdatedOnServerUids = false;
	}

	public void setWaitingForServer()
	{
		mainWindow.setStatusMessageTag(UiMainWindow.STATUS_CONNECTING);
		waitingForServer = true;
	}
	
	public void run()
	{
		if(mainWindow.isMainWindowInitalizing())
		{
			MartusLogger.log("Waiting to contact server until startup is complete");
			return;
		}
		
		if(mainWindow.inConfigServer)
			return;
		if(inComplianceDialog)
			return;
		if(mainWindow.preparingToExitMartus)
			return;
		if(checkingForNewFieldOfficeBulletins)
			return;
			
		if(!getApp().isServerConfigured())
		{
			mainWindow.setStatusMessageTag(UiMainWindow.STATUS_SERVER_NOT_CONFIGURED);	
			return;
		}												
			
	
		try
		{
			if(waitingForServer)
				updateServerStatus();
			checkComplianceStatement();
			checkForNewsFromServer();
			getMartusAccountAccessToken();
			getUpdatedListOfBulletinsOnServer();
			doRetrievingOrUploading();
			checkForNewFieldOfficeBulletins();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void updateServerStatus()
	{
		if(!mainWindow.isServerConfigured())
			mainWindow.clearStatusMessage();
		else if(!getApp().getTransport().isOnline())
		{
			mainWindow.setStatusMessageTag(UiMainWindow.STATUS_SERVER_OFFLINE_MODE);
		}
		else if(isServerAvailable())
		{
			mainWindow.setStatusMessageReady();
			waitingForServer = false;
		}
		else
		{
			setWaitingForServer();
		}
	}

	private boolean isServerAvailable()
	{
		try
		{
			return (getApp().isSSLServerAvailable());
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	private void doRetrievingOrUploading() throws Exception
	{
		final ProgressMeterInterface progressMeter = getProgressMeter();
		if(retriever.hasWorkToDo())
		{
			if(!isServerAvailable())
				return;
			progressMeter.setStatusMessage(UiMainWindow.STATUS_RETRIEVING);
			doRetrieving();
			if(!retriever.hasWorkToDo())
			{
				SwingUtilities.invokeLater(new ThreadedUpdateReadyMessage());
			}
			return;
		}
		doUploading();
	}
	
	private void doRetrieving() throws Exception
	{
		String folderName = retriever.getRetrieveFolderName();
		final BulletinFolder folder = mainWindow.getApp().createOrFindFolder(folderName);
		try
		{
			retriever.retrieveNext();
		}
		catch (Exception e)
		{
			String tag = "RetrieveError";
			SwingUtilities.invokeLater(new WorkerThread.ThreadedNotifyDlg(mainWindow, tag));
			SwingUtilities.invokeLater(new ThreadedUpdateReadyMessage());
			e.printStackTrace();
		}
		mainWindow.folderContentsHaveChanged(folder);
	}
	
	private void doUploading()
		throws InterruptedException, InvocationTargetException
	{
		
		BackgroundUploader.UploadResult uploadResult = new BackgroundUploader.UploadResult();
		String tag = UiMainWindow.STATUS_READY;
		if(!mainWindow.isServerConfigured())
		{
			tag = UiMainWindow.STATUS_SERVER_NOT_CONFIGURED;
		}
		else if(!mainWindow.getApp().getTransport().isOnline())
		{
			tag = UiMainWindow.STATUS_SERVER_OFFLINE_MODE;
		}
		else
		{					
			uploadResult = uploader.backgroundUpload(); 
			mainWindow.uploadResult = uploadResult.result;	
			if(uploadResult.isHopelesslyDamaged)
			{
				ThreadedNotify damagedBulletin = new ThreadedNotify("DamagedBulletinMovedToDiscarded", uploadResult.uid);
				SwingUtilities.invokeAndWait(damagedBulletin);
				mainWindow.folderContentsHaveChanged(getStore().getFolderSealedOutbox());
				mainWindow.folderContentsHaveChanged(getStore().getFolderDraftOutbox());
				mainWindow.folderContentsHaveChanged(getApp().createOrFindFolder(getStore().getNameOfFolderDamaged()));
				mainWindow.folderTreeContentsHaveChanged();
			}
			else if(uploadResult.bulletinNotSentAndRemovedFromQueue)
			{
				tag = "UploadFailedProgressMessage"; 
				ThreadedNotify bulletinNotSent = new ThreadedNotify("UploadFailedBulletinNotSentToServer", uploadResult.uid);
				SwingUtilities.invokeAndWait(bulletinNotSent);
				updateDisplay();
			}
			else if(uploadResult.result == null)
			{
				tag = "UploadFailedProgressMessage"; 
				if(uploadResult.exceptionThrown == null)
					tag = UiMainWindow.STATUS_NO_SERVER_AVAILABLE;
			}
			else if(uploadResult.uid != null)
			{
				//System.out.println("UiMainWindow.Tick.run: " + uploadResult);
				updateDisplay();
			}
			else
				tag = "";							
		}

		if(tag.length() > 0)			
			mainWindow.setStatusMessageTag(tag);
	}
	
	private void checkForNewFieldOfficeBulletins()
	{
		String syncFrequency = getApp().getConfigInfo().getSyncFrequencyMinutes();
		if(syncFrequency.length() == 0)
			return;
		if(hasUserChangedSyncFrequency(syncFrequency))
		{
			nextCheckForFieldOfficeBulletins = 0;
			isSyncEnabled = true;
		}

		if(!isSyncEnabled)
			return;

		try
		{
			if(syncRetriever.hadException())
				throw syncRetriever.getAndClearException();
		}
		catch (AddOlderVersionToFolderFailedException ignoreOldVersionException)
		{
			MartusLogger.log("Older version not added."); 
			return;
		}
		catch (Exception e)
		{
			disableSync();
			MartusLogger.logException(e);

			String baseTag = "SyncDisabledDueToError";
			UiMainWindow.showNotifyDlgOnSwingThread(mainWindow, baseTag);
			return;
		}

		lastKnownSyncFrequencyMinutes = syncFrequency;
		if(System.currentTimeMillis() < nextCheckForFieldOfficeBulletins)
			return;
		if(!isServerAvailable())
			return;
		if(syncRetriever.isBusy())
			return;
		
		checkingForNewFieldOfficeBulletins = true;
		boolean foundNew = false;
		try
		{
			mainWindow.setStatusMessageTag("statusCheckingForNewFieldOfficeBulletins");
			ClientSideNetworkGateway gateway = getApp().getCurrentNetworkInterfaceGateway();
			MartusCrypto security = getApp().getSecurity();
			String nextTimestampToAskForAvailableBulletins = syncRetriever.getNextTimestampToAskForAvailableBulletins();
			NetworkResponse response = gateway.listAvailableRevisionsSince(security, nextTimestampToAskForAvailableBulletins);
			String resultCode = response.getResultCode();
			if(resultCode.equals(NetworkInterfaceConstants.SERVER_DOWN))
			{
				throw new ServerNotAvailableException();
			}
			else if(resultCode.equals(NetworkInterfaceConstants.SERVER_NOT_COMPATIBLE))
			{
				MartusLogger.log("Sync disabled because server does not support that feature");
				disableSync();
				return;
			}
			else if(!resultCode.equals(NetworkInterfaceConstants.OK))
			{
				MartusLogger.log("Unexpected network response: " + resultCode);
				throw new ServerCallFailedException();
			}

			String resultJson = (String) response.getResultVector().get(0);
			EnhancedJsonObject json = new EnhancedJsonObject(resultJson);
			SummaryOfAvailableBulletins summary = new SummaryOfAvailableBulletins(json);
			syncRetriever.startRetrieve(summary);
		}
		catch(Exception e)
		{
			mainWindow.unexpectedErrorDlg(e);
		}
		finally
		{
			long delayBeforeNextSyncMinutes = getSyncDelayMinutes(syncFrequency);
			MartusLogger.log("Next sync in " + delayBeforeNextSyncMinutes + " minutes");
			long delayBeforeNextSyncMillis = delayBeforeNextSyncMinutes * 60 * 1000;
			nextCheckForFieldOfficeBulletins = System.currentTimeMillis() + delayBeforeNextSyncMillis;
			checkingForNewFieldOfficeBulletins = false;

			if(foundNew)
				mainWindow.setStatusMessageTag("statusNewFieldOfficeBulletins");
			else
				mainWindow.setStatusMessageReady();
		}
	}

	public boolean hasUserChangedSyncFrequency(String syncFrequency)
	{
		return (!syncFrequency.equals(lastKnownSyncFrequencyMinutes));
	}

	private int getSyncDelayMinutes(String syncFrequency)
	{
		if(syncFrequency.equals(SettingsForServerController.SYNC_FREQUENCY_ON_STARTUP))
			return Integer.MAX_VALUE;
		
		int syncMinutes = Integer.parseInt(syncFrequency);
		return syncMinutes;
	}

	public void disableSync()
	{
		isSyncEnabled = false;
	}
	
	private void getUpdatedListOfBulletinsOnServer()
	{
		if(gotUpdatedOnServerUids)
			return;
		if(!isServerAvailable())
			return;
		mainWindow.setStatusMessageTag(UiMainWindow.STATUS_CONNECTING);
		MartusLogger.logBeginProcess("BackgroundUploadTimerTask.getUpdatedListOfBulletinsOnServer");
		String myAccountId = getApp().getAccountId();
		HashSet summariesOnServer = new HashSet(1000);
		try
		{
			summariesOnServer.addAll(getBulletinSummariesFromServer(myAccountId));
			
			summariesOnServer.addAll(getFieldOfficeSummariesOnServer());
			getStore().updateOnServerLists(summariesOnServer);

			class CurrentFolderRefresher implements Runnable
			{
				public void run()
				{
					mainWindow.allBulletinsInCurrentFolderHaveChanged();
				}
			}
			SwingUtilities.invokeLater(new CurrentFolderRefresher());
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
		}
		gotUpdatedOnServerUids = true;
		
		MartusLogger.logEndProcess("BackgroundUploadTimerTask.getUpdatedListOfBulletinsOnServer");
		mainWindow.setStatusMessageReady();
	}

	private Set getFieldOfficeSummariesOnServer() throws Exception 
	{
		HashSet summariesOnServer = new HashSet(1000);
		ClientSideNetworkGateway gateway = getApp().getCurrentNetworkInterfaceGateway();
		MartusCrypto security = getApp().getSecurity();
		NetworkResponse myFieldOfficesResponse = gateway.getFieldOfficeAccountIds(security, getApp().getAccountId());
		if(NetworkInterfaceConstants.OK.equals(myFieldOfficesResponse.getResultCode()))
		{
			Vector fieldOfficeAccounts = myFieldOfficesResponse.getResultVector();
			MartusLogger.log("My FO accounts: " + fieldOfficeAccounts.size());
			for(int i = 0; i < fieldOfficeAccounts.size(); ++i)
			{
				String fieldOfficeAccountId = (String)fieldOfficeAccounts.get(i);
				summariesOnServer.addAll(getBulletinSummariesFromServer(fieldOfficeAccountId));
			}
		}
		
		return summariesOnServer;
	}
	
	private Vector getBulletinSummariesFromServer(String accountId) throws Exception
	{
		Vector summariesOnServer = new Vector();
		Vector sealedSummaries = tryToGetSealedBulletinSummariesFromServer(accountId);
		summariesOnServer.addAll(sealedSummaries);

		Vector draftSummaries = tryToGetDraftBulletinSummariesFromServer(accountId);
		summariesOnServer.addAll(draftSummaries);
		MartusLogger.log("Adding summaries from server: " + summariesOnServer.size());
		return summariesOnServer;
	}

	private Vector tryToGetDraftBulletinSummariesFromServer(String accountId) throws Exception
	{
		ClientSideNetworkGateway gateway = getApp().getCurrentNetworkInterfaceGateway();
		MartusCrypto security = getApp().getSecurity();
		NetworkResponse myDraftResponse = gateway.getDraftBulletinIds(security, accountId, BulletinSummary.getNormalRetrieveTags());
		if(NetworkInterfaceConstants.OK.equals(myDraftResponse.getResultCode()))
			return buildBulletinSummaryVector(accountId, myDraftResponse.getResultVector());
		return new Vector();
	}

	private Vector tryToGetSealedBulletinSummariesFromServer(String accountId) throws Exception
	{
		ClientSideNetworkGateway gateway = getApp().getCurrentNetworkInterfaceGateway();
		MartusCrypto security = getApp().getSecurity();
		NetworkResponse mySealedResponse = gateway.getSealedBulletinIds(security, accountId, new Vector());
		if(NetworkInterfaceConstants.OK.equals(mySealedResponse.getResultCode()))
			return buildBulletinSummaryVector(accountId, mySealedResponse.getResultVector());
		return new Vector();
	}

	private Vector buildBulletinSummaryVector(String accountId, Vector summaryStrings) throws WrongValueCount
	{
		Vector result = new Vector();
		for(int i=0; i < summaryStrings.size(); ++i)
		{
			String summaryString = (String)summaryStrings.get(i);
			BulletinSummary summary = getApp().createSummaryFromString(accountId, summaryString);
			result.add(summary);
		}
		
		return result;
	}
		
	private void updateDisplay()
	{
		class Updater implements Runnable
		{
			public void run()
			{
				ClientBulletinStore store = getStore();
				mainWindow.folderContentsHaveChanged(store.getFolderSaved());
				mainWindow.folderContentsHaveChanged(store.getFolderSealedOutbox());
				mainWindow.folderContentsHaveChanged(store.getFolderDraftOutbox());
				BulletinFolder discardedFolder = store.findFolder(store.getNameOfFolderDamaged());
				if(discardedFolder != null)
					mainWindow.folderContentsHaveChanged(discardedFolder);
			}
		}
		Updater updater = new Updater();
		
		final boolean crashMode = false;
		if(crashMode)
		{
			updater.run();
		}
		else
		{
			try
			{
				SwingUtilities.invokeAndWait(updater);
			}
			catch (Exception notMuchWeCanDoAboutIt)
			{
				notMuchWeCanDoAboutIt.printStackTrace();
			}
		}
	}

	public void checkComplianceStatement()
	{
		if(alreadyCheckedCompliance)
			return;
		if(!isServerAvailable())
			return;
		try
		{
			ClientSideNetworkGateway gateway = getApp().getCurrentNetworkInterfaceGateway();
			String compliance = getApp().getServerCompliance(gateway);
			alreadyCheckedCompliance = true;
			if (compliance != null)
				mainWindow.setStatusMessageReady();
			
			if(!compliance.equals(getApp().getConfigInfo().getServerCompliance()))
			{
				ThreadedServerComplianceDlg dlg = new ThreadedServerComplianceDlg(compliance);
				SwingUtilities.invokeAndWait(dlg);
			}
		}
		catch (ServerCallFailedException userAlreadyKnows)
		{
			alreadyCheckedCompliance = true;			
			return;
		}
		catch (ServerNotAvailableException weWillTryAgainLater)
		{
			mainWindow.setStatusMessageTag(UiMainWindow.STATUS_NO_SERVER_AVAILABLE);
			return;
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		} 
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setNeedToGetAccessToken()
	{
		nextCheckForToken = 0;
	}
	
	public void getMartusAccountAccessToken()
	{
		if(checkingForToken)
			return;
		long now = System.currentTimeMillis();
		if(now < nextCheckForToken)
			return;
		if(!isServerAvailable())
			return;
		
		checkingForToken = true;
		try
		{
			MartusAccountAccessToken currentTokenFromServer = getApp().getMartusAccountAccessTokenFromServer();
			MartusLogger.log("Got my token from server: " + currentTokenFromServer);
			ConfigInfo config = getApp().getConfigInfo();
			config.setCurrentMartusAccountAccessToken(currentTokenFromServer);
			getApp().saveConfigInfo();
			nextCheckForToken = Long.MAX_VALUE;
		} 
		catch (ServerNotCompatibleException e)
		{
			MartusLogger.log("Server does not support getting token");
			nextCheckForToken = Long.MAX_VALUE;
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			nextCheckForToken = System.currentTimeMillis() + IN_A_FEW_MINUTES_IN_MILLIS;
		} 
		finally
		{
			checkingForToken = false;
		}
		
		MartusLogger.log("Will check for token again in " + (nextCheckForToken - now) / 60000 + " minutes"); 
	}

	public void checkForNewsFromServer()
	{
		if(alreadyGotNews)
			return;
		if(!isServerAvailable())
			return;

		MartusLogger.logBeginProcess("Checking server news");
		Vector newsItems = getApp().getNewsFromServer();
		
		int newsSize = newsItems.size();
		if (newsSize > 0)
			mainWindow.setStatusMessageReady();
			
		
		for (int i = 0; i < newsSize; ++i)
		{
			HashMap tokenReplacement = new HashMap();
			tokenReplacement.put("#CurrentNewsItem#", Integer.toString(i+1));
			tokenReplacement.put("#MaxNewsItems#", Integer.toString(newsSize));

			String newsItem = (String) newsItems.get(i);
			ThreadedMessageDlg newsDlg = new ThreadedMessageDlg("ServerNews", newsItem, tokenReplacement);
			try
			{
				SwingUtilities.invokeAndWait(newsDlg);
			}
			catch (Exception e)
			{
				mainWindow.setStatusMessageTag(UiMainWindow.STATUS_NO_SERVER_AVAILABLE);
				e.printStackTrace();
			}
		}
		alreadyGotNews = true;
		MartusLogger.logEndProcess("Checking server news");
	}

	class ThreadedNotify implements Runnable
	{
		public ThreadedNotify(String tag, UniversalId uidToUse)
		{
			notifyTag = tag;
			uid = uidToUse;
		}

		public void run()
		{
			String bulletinTitle = "";
			if(uid != null)
				bulletinTitle = mainWindow.getStore().getBulletinRevision(uid).get(Bulletin.TAGTITLE);
			
			HashMap map = new HashMap();
			map.put("#BulletinTitle#", bulletinTitle);
			if(UiSession.isJavaFx())
				FxDialogHelper.showNotificationDialog(mainWindow, notifyTag, map);
			else
				mainWindow.notifyDlg(notifyTag,map);
		}
		String notifyTag;
		UniversalId uid;
	}
		
	class ThreadedServerComplianceDlg implements Runnable
	{
		public ThreadedServerComplianceDlg(String newComplianceToUse)
		{
			UiFontEncodingHelper fontHelper = new UiFontEncodingHelper(mainWindow.getDoZawgyiConversion());
			newCompliance = fontHelper.getDisplayable(newComplianceToUse);
		}
			
		public void run()
		{
			try 
			{
				inComplianceDialog = true;
				if(mainWindow.confirmServerCompliance("ServerComplianceChangedDescription", newCompliance))
				{
					String serverAddress = getApp().getConfigInfo().getServerName();
					String serverKey = getApp().getConfigInfo().getServerPublicKey();
					getApp().setServerInfo(serverAddress, serverKey, newCompliance);
				}
				else
				{
					getApp().setServerInfo("", "", "");
					mainWindow.notifyDlg("ExistingServerRemoved");
				}
				inComplianceDialog = false;
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
			
		String newCompliance;
	}
		
	class ThreadedMessageDlg implements Runnable
	{
		public ThreadedMessageDlg(String tag, String message, HashMap tokenReplacementToUse )
		{
			titleTag = tag;
			messageContents = message;
			tokenReplacement = tokenReplacementToUse;
		}

		public void run()
		{
			mainWindow.messageDlg(titleTag, messageContents, tokenReplacement);
		}
		String titleTag;
		String messageContents;
		HashMap tokenReplacement;
	}
	
	class ThreadedUpdateReadyMessage implements Runnable
	{
		public void run()
		{
			mainWindow.setStatusMessageReady();
		}
	}
		
	MartusApp getApp()
	{
		return mainWindow.getApp();
	}
		
	ClientBulletinStore getStore()
	{
		return getApp().getStore();
	}
	
	private static final long IN_A_FEW_MINUTES_IN_MILLIS = 10 * 60 * 1000;
	
	UiMainWindow mainWindow;
	BackgroundUploader uploader;
	BackgroundRetriever retriever;
	private SyncBulletinRetriever syncRetriever;
	private StatusBar statusBar;
	
	long nextCheckForFieldOfficeBulletins;
	long nextCheckForToken;

	private boolean isSyncEnabled;
	boolean waitingForServer;
	boolean alreadyCheckedCompliance;
	boolean inComplianceDialog;
	boolean alreadyGotNews;
	boolean gotUpdatedOnServerUids;
	boolean checkingForNewFieldOfficeBulletins;
	private boolean checkingForToken;
	private String lastKnownSyncFrequencyMinutes;
}

