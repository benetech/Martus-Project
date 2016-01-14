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

package org.martus.client.swingui.tablemodels;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.RetrieveSummariesProgressMeter;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.common.BulletinSummary;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;
import org.martus.swing.FontHandler;
import org.martus.swing.UiTableModel;

abstract public class RetrieveTableModel extends UiTableModel  
{
	public RetrieveTableModel(MartusApp appToUse, MiniLocalization localizationToUse)
	{
		app = appToUse;
		localization = localizationToUse;
		
		downloadableSummaries = new Vector();
		store = app.getStore();
		allSummaries = new Vector();
	}
	
	public void initialize(RetrieveSummariesProgressMeter progressHandlerToUse) throws Exception
	{
		MartusLogger.logBeginProcess("Initializing Retrieve dialog");
		setProgressDialog(progressHandlerToUse);
		populateAllSummariesList();
		buildDownloadableSummariesList();
		changeToDownloadableSummaries();
		MartusLogger.logEndProcess("Initializing Retrieve dialog");

		populateMissingSummaryDataFromServer(this);
	}
	
	abstract protected void populateAllSummariesList() throws Exception;
	
	public MartusApp getApp()
	{
		return app;
	}
	
	public int getColumnCount()
	{
		return columnCount;
	}
	
	public String getColumnName(int column)
	{
		if(column == COLUMN_RETRIEVE_FLAG)
			return getLocalization().getFieldLabel("retrieveflag");
		if(column == COLUMN_TITLE)
			return getLocalization().getFieldLabel(Bulletin.TAGTITLE);
		if(column == COLUMN_AUTHOR)
			return getLocalization().getFieldLabel(Bulletin.TAGAUTHOR);
		if(column == COLUMN_LAST_DATE_SAVED)
			return getLocalization().getFieldLabel(Bulletin.TAGLASTSAVED);
		if(column == COLUMN_BULLETIN_SIZE)
			return getLocalization().getFieldLabel("BulletinSize");
		if(column == COLUMN_DELETE_FLAG)
			return getLocalization().getFieldLabel("DeleteFlag");
		if(column == COLUMN_VERSION_NUMBER)
			return getLocalization().getFieldLabel("BulletinVersionNumber");
		return "";
	}

	public Object getValueAt(int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)currentSummaries.get(row);
		return getFormattedValueAt(summary, column);
	}

	private Object getFormattedValueAt(BulletinSummary summary, int column) 
	{
		Object unformatted = getUnformattedValueAt(summary, column);
		if(column == COLUMN_LAST_DATE_SAVED)
		{
			long value = ((Long)unformatted).longValue();
			return getLocalization().formatDateTime(value);
		}
		return unformatted;
	}

	Object getUnformattedValueAt(BulletinSummary summary, int column) 
	{
		if(column == COLUMN_RETRIEVE_FLAG)
			return new Boolean(summary.isChecked());
		if(column == COLUMN_TITLE)
			return getDisplayable(summary.getStorableTitle());
		if(column == COLUMN_AUTHOR)
			return getDisplayable(summary.getStorableAuthor());
		if(column == COLUMN_LAST_DATE_SAVED)
			return new Long(summary.getDateTimeSaved());
		if(column == COLUMN_BULLETIN_SIZE)
			return  getSizeInKbytes(summary.getSize());
		if(column == COLUMN_DELETE_FLAG)
			return new Boolean(summary.isChecked());
		if(column == COLUMN_VERSION_NUMBER)
			return new Integer(summary.getVersionNumber());
		return "";
	}

	private String getDisplayable(String storableTitle)
	{
		if(storableTitle == null)
			return "";
		
		UiFontEncodingHelper fontHelper = new UiFontEncodingHelper(FontHandler.isDoZawgyiConversion());
		return fontHelper.getDisplayable(storableTitle);
	}

	public void setValueAt(Object value, int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)currentSummaries.get(row);
		if(column == COLUMN_RETRIEVE_FLAG)
			summary.setChecked(((Boolean)value).booleanValue());
		if(column == COLUMN_DELETE_FLAG)
			summary.setChecked(((Boolean)value).booleanValue());
	}

	public Class getColumnClass(int column)
	{
		if(column == COLUMN_RETRIEVE_FLAG)
			return Boolean.class;
		if(column == COLUMN_TITLE)
			return String.class;
		if(column == COLUMN_AUTHOR)
			return String.class;
		if(column == COLUMN_LAST_DATE_SAVED)
			return String.class;
		if(column == COLUMN_BULLETIN_SIZE)
			return Integer.class;
		if(column == COLUMN_DELETE_FLAG)
			return Boolean.class;
		if(column == COLUMN_VERSION_NUMBER)
			return Integer.class;
		return null;
	}
	
	MiniLocalization getLocalization()
	{
		return localization;
	}

	protected void setProgressDialog(RetrieveSummariesProgressMeter progressHandlerToUse)
	{
		progressHandler = progressHandlerToUse;
	}

	public void changeToDownloadableSummaries()
	{
		currentSummaries = downloadableSummaries;
		sortCurrentSummaries();
	}

	public void changeToAllSummaries()
	{
		currentSummaries = allSummaries;
		sortCurrentSummaries();
	}
	
	public void setCurrentSortColumn(int column)
	{
		currentSortColumn = column;
		sortCurrentSummaries();
	}
	
	public void sortCurrentSummaries()
	{
		synchronized (currentSummaries)
		{
			Collections.sort(currentSummaries, new SummarySorter(currentSortColumn));
		}
		fireTableDataChanged();
	}
	
	class SummarySorter implements Comparator
	{
		public SummarySorter(int columnToSortBy)
		{
			column = columnToSortBy;
		}

		public int compare(Object first, Object second) 
		{
			BulletinSummary s1 = (BulletinSummary)first;
			BulletinSummary s2 = (BulletinSummary)second;
			
			Comparable value1 = toComparable(getUnformattedValueAt(s1, column));
			Comparable value2 = toComparable(getUnformattedValueAt(s2, column)); 
			return value1.compareTo(value2);
		}
		
		Comparable toComparable(Object value)
		{
			if(value instanceof Boolean)
				value = new ComparableBoolean((Boolean)value);
			return (Comparable)value;
		}
		
		int column;
	}

	public void setAllFlags(boolean flagState)
	{
		synchronized (currentSummaries)
		{
			for (int i = 0; i < currentSummaries.size(); ++i)
				((BulletinSummary) currentSummaries.get(i)).setChecked(flagState);
		}
		fireTableDataChanged();
	}

	public boolean isDownloadable(int row)
	{
		return((BulletinSummary)currentSummaries.get(row)).isDownloadable();
	}
	
	public boolean isEnabled(int row)
	{
		return isDownloadable(row);
	}

	public Vector getSelectedUidsLatestVersion()
	{
		return getListOfRequestedUids(false);
	}

	public Vector getSelectedUidsFullHistory()
	{
		return getListOfRequestedUids(true);
	}

	private Vector getListOfRequestedUids(boolean includeEalierVerions)
	{
		Vector uidList = new Vector();
		synchronized (currentSummaries)
		{
			for (int i = 0; i < currentSummaries.size(); ++i)
			{
				BulletinSummary summary = (BulletinSummary) currentSummaries
						.get(i);
				if (summary.isChecked())
				{
					UniversalId uid = UniversalId.createFromAccountAndLocalId(
							summary.getAccountId(), summary.getLocalId());
					uidList.add(uid);
					if (includeEalierVerions)
					{
						BulletinHistory history = summary.getHistory();
						for (int h = 0; h < history.size(); ++h)
						{
							uid = UniversalId.createFromAccountAndLocalId(
									summary.getAccountId(), history.get(h));
							uidList.add(uid);
						}
					}
				}
			}
		}
		return uidList;
	}
	
	public int getRowCount()
	{
		return currentSummaries.size();
	}

	public boolean isCellEditable(int row, int column)
	{
		if(column == COLUMN_RETRIEVE_FLAG || column == COLUMN_DELETE_FLAG)
			return true;

		return false;
	}

	public void getMySealedSummaries() throws Exception
	{
		String accountId = app.getAccountId();
		SummaryRetriever retriever = new SealedSummaryRetriever(app, accountId);
		retrieveSummaries(accountId, retriever);
	}

	public void getMyDraftSummaries() throws Exception
	{
		String accountId = app.getAccountId();
		SummaryRetriever retriever = new DraftSummaryRetriever(app, accountId);
		retrieveSummaries(accountId, retriever);
	}

	public void getFieldOfficeSealedSummaries(String fieldOfficeAccountId) throws Exception
	{
		SummaryRetriever retriever = new SealedSummaryRetriever(app, fieldOfficeAccountId);
		retrieveSummaries(fieldOfficeAccountId, retriever);
	}

	public void getFieldOfficeDraftSummaries(String fieldOfficeAccountId) throws Exception
	{
		SummaryRetriever retriever = new DraftSummaryRetriever(app, fieldOfficeAccountId);
		retrieveSummaries(fieldOfficeAccountId, retriever);
	}

	private void retrieveSummaries(String accountId, SummaryRetriever retriever) throws Exception
	{
		Vector summaryStrings = getSummaryStringsFromServer(retriever);
		Vector bulletinSummaries = buildSummariesFromStrings(accountId, summaryStrings);
		markAsOnServer(bulletinSummaries);
		updateAllDownloadableFlags(bulletinSummaries);
		allSummaries.addAll(bulletinSummaries);
	}

	private Vector getSummaryStringsFromServer(SummaryRetriever retriever) throws Exception
	{
		try
		{
			NetworkResponse response = retriever.getSummaries();
			if(!response.getResultCode().equals(NetworkInterfaceConstants.OK))
				throw new ServerErrorException();

			return response.getResultVector();
		}
		catch (MartusSignatureException e)
		{
			System.out.println("RetrieveTableModel.getFieldOfficeSummaryStringsFromServer: " + e);
			throw new ServerErrorException();
		}
	}

	private void updateAllDownloadableFlags(Vector summaries)
	{
		Iterator iterator = summaries.iterator();
		while(iterator.hasNext())
		{
			BulletinSummary currentSummary = (BulletinSummary)iterator.next();
			currentSummary.setDownloadable(isDownloadable(currentSummary));
		}
	}

	public boolean isDownloadable(BulletinSummary currentSummary)
	{
		String accountId = currentSummary.getAccountId();
		String localId = currentSummary.getLocalId();
		long lastSaved = currentSummary.getDateTimeSaved();
		
		UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);
		DatabaseKey key = DatabaseKey.createLegacyKey(uid);
		if(store.doesBulletinRevisionExist(key))
		{
			Bulletin existing = store.getBulletinRevision(uid);
			long alreadyHaveLastSaved = existing.getLastSavedTime();
			return shouldDownloadDraftWithDifferentTimestamp(lastSaved, alreadyHaveLastSaved);
		}
		
		if(store.hasNewerRevision(uid))
			return false;
		
		return true;
	}

	protected boolean shouldDownloadDraftWithDifferentTimestamp(long timestampOnServer, long timestampLocal)
	{
		return false;
	}

	protected void buildDownloadableSummariesList()
	{
		downloadableSummaries = new Vector();
		synchronized (allSummaries)
		{
			Iterator iterator = allSummaries.iterator();
			while (iterator.hasNext())
			{
				BulletinSummary currentSummary = (BulletinSummary) iterator
						.next();
				if (currentSummary.isDownloadable())
					downloadableSummaries.add(currentSummary);
			}
		}
	}

	public void populateMissingSummaryDataFromServer(RetrieveTableModel tableModelToUse)
	{
		Vector downloadableBulletinSummaries = new Vector(tableModelToUse.getDownloadableSummaries());
		
		List summariesToDownload;
		synchronized (downloadableBulletinSummaries)
		{
			summariesToDownload = Collections.synchronizedList(new Vector());
			for (Object rawBulletinSummary : downloadableBulletinSummaries)
			{
				BulletinSummary bulletinSummary = (BulletinSummary) rawBulletinSummary;
				if (!bulletinSummary.hasFieldDataPacket())
					summariesToDownload.add(bulletinSummary);
			}
		}
		
		int originalCount = summariesToDownload.size();
		final int SUMMARY_RETRIEVER_THREAD_COUNT = 10;
		RetrieveThread[] workers = new RetrieveThread[SUMMARY_RETRIEVER_THREAD_COUNT];
		for(int i = 0; i < SUMMARY_RETRIEVER_THREAD_COUNT; ++i)
		{
			workers[i] = new RetrieveThread(tableModelToUse, summariesToDownload, originalCount);
			workers[i].start();
		}

		if(progressHandler == null)
			waitForThreadsToTerminate(workers);
		else
			progressHandler.started();
	}

	public void waitForThreadsToTerminate(RetrieveThread[] workers)
	{
		try
		{
			for(int i = 0; i < workers.length; ++i)
				workers[i].join();
		}
		catch (InterruptedException e)
		{
		}
	}

	class RetrieveThread extends Thread
	{
		public RetrieveThread(RetrieveTableModel tableModelToUse, List bulletinSummariesToDownload, int originalCountToUse)
		{
			tableModel = tableModelToUse;
			summariesToDownload = bulletinSummariesToDownload;
			originalCount = originalCountToUse;
		}

		public void run()
		{
			MartusLogger.logBeginProcess("Retrieve missing summary data from server");
			retrieveMissingDetailsFromServer();
			finishedRetrieve();
			MartusLogger.logEndProcess("Retrieve missing summary data from server");
		}

		public void retrieveMissingDetailsFromServer()
		{
			while(true)
			{
				BulletinSummary summary = getNextSummaryToRetrieve();
				if(summary == null)
					return;
				
				try
				{
					app.setFieldDataPacketFromServer(summary);
					tableModel.summaryHasChanged(summary);
				}
				catch (Exception e)
				{
					errorThrown = e;
					tableModel.summaryNotAvailable(summary);
				}
		
				if(progressHandler != null)
				{
					if(progressHandler.shouldExit())
						break;
					
					int completed = originalCount - summariesToDownload.size();
					progressHandler.updateProgressMeter(completed, originalCount);
				}
			}
		}

		public void finishedRetrieve()
		{
			if(progressHandler != null)
				progressHandler.finished();
		}

		private BulletinSummary getNextSummaryToRetrieve()
		{
			synchronized (summariesToDownload)
			{
				if(summariesToDownload.size() > 0)
					return (BulletinSummary) summariesToDownload.remove(0);
			}
			
			return null;
		}
		
		private RetrieveTableModel tableModel;
		private List summariesToDownload;
		private int originalCount;
	}
	
	void summaryHasChanged(BulletinSummary summary)
	{
		int at = currentSummaries.indexOf(summary);
		fireTableRowsUpdated(at, at);
	}
	
	void summaryNotAvailable(BulletinSummary summary)
	{
		summary.setDownloadable(false);
		downloadableSummaries.remove(summary);
		synchronized (currentSummaries)
		{
			currentSummaries.remove(summary);
		}
		fireTableDataChanged();
	}

	public void checkIfErrorOccurred() throws Exception
	{
		if(errorThrown != null)
			throw (errorThrown);
	}

	public Vector getDownloadableSummaries()
	{
		return downloadableSummaries;
	}

	public Vector buildSummariesFromStrings(String accountId, Vector summaryStrings)
	{
		Vector bulletinSummaries = new Vector();
		Iterator iterator = summaryStrings.iterator();
		while(iterator.hasNext())
		{
			String pair = (String)iterator.next();
			try
			{
				BulletinSummary summary = app.createSummaryFromString(accountId, pair);
				bulletinSummaries.add(summary);
			}
			catch (Exception e)
			{
				errorThrown = e;
			}
		}
		return bulletinSummaries;
	}

	public Vector getAllSummaries()
	{
		return allSummaries;
	}

	public BulletinSummary getBulletinSummary(int row)
	{
		return (BulletinSummary)currentSummaries.get(row);
	}
	
	void markAsOnServer(Vector summaries)
	{
		synchronized (summaries)
		{
			for (int i = 0; i < summaries.size(); ++i)
			{
				BulletinSummary summary = (BulletinSummary) summaries.get(i);
				Bulletin b = app.getStore().getBulletinRevision(
						summary.getUniversalId());
				if (b != null)
					app.getStore().setIsOnServer(b);
			}
		}
	}
	
	public Set getUidsThatWouldBeUpgrades(Vector uidsSelectedForRetrieve)
	{
		Set uidsBeingUpgraded = new HashSet();
		synchronized (allSummaries)
		{
			for (int i = 0; i < allSummaries.size(); ++i)
			{
				BulletinSummary summary = (BulletinSummary) allSummaries.get(i);
				UniversalId uidBeingRetrieved = summary.getUniversalId();
				if (!uidsSelectedForRetrieve.contains(uidBeingRetrieved))
					continue;

				if (doesBulletinExist(summary))
					uidsBeingUpgraded.add(uidBeingRetrieved);
			}
		}
		return uidsBeingUpgraded;
	}

	private boolean doesBulletinExist(BulletinSummary summary)
	{
		String accountId = summary.getAccountId();
		BulletinHistory history = summary.getHistory();
		for(int j = 0; j < history.size(); ++j)
		{
			String localId = history.get(j);
			UniversalId uidBeingChecked = UniversalId.createFromAccountAndLocalId(accountId, localId);
			DatabaseKey key = DatabaseKey.createLegacyKey(uidBeingChecked);
			if(store.doesBulletinRevisionExist(key))
				return true;
		}
		return false;
	}

	public static Integer getSizeInKbytes(int sizeKb)
	{
		sizeKb /= 1000;
		if(sizeKb <= 0)
			sizeKb = 1;
		Integer sizeInK = new Integer(sizeKb);
		return sizeInK;
	}
	
	static abstract class SummaryRetriever
	{
		SummaryRetriever(MartusApp appToUse, String accountIdToUse)
		{
			app = appToUse;
			accountId = accountIdToUse;
		}
		
		NetworkResponse getSummaries() throws Exception
		{
			if(!app.isSSLServerAvailable())
				throw new ServerErrorException("No server");
			return internalGetSummaries();
		}
		
		abstract NetworkResponse internalGetSummaries() throws MartusSignatureException;
		
		protected MartusCrypto getSecurity()
		{
			MartusCrypto security = app.getSecurity();
			return security;
		}

		protected Vector getSummaryTags()
		{
			return BulletinSummary.getNormalRetrieveTags();
		}

		protected ClientSideNetworkGateway getGateway()
		{
			return app.getCurrentNetworkInterfaceGateway();
		}

		MartusApp app;
		String accountId;
	}

	static class DraftSummaryRetriever extends SummaryRetriever
	{
		DraftSummaryRetriever(MartusApp appToUse, String accountIdToUse)
		{
			super(appToUse, accountIdToUse);
		}

		NetworkResponse internalGetSummaries() throws MartusSignatureException
		{
			return getGateway().getDraftBulletinIds(getSecurity(), accountId, getSummaryTags());
		}
		
	}

	static class SealedSummaryRetriever extends SummaryRetriever
	{
		SealedSummaryRetriever(MartusApp appToUse, String accountIdToUse)
		{
			super(appToUse, accountIdToUse);
		}

		NetworkResponse internalGetSummaries() throws MartusSignatureException
		{
			return getGateway().getSealedBulletinIds(getSecurity(), accountId, getSummaryTags());
		}
		
	}
	
	static class ComparableBoolean implements Comparable
	{
		public ComparableBoolean(Boolean valueToWrap)
		{
			if(valueToWrap.booleanValue())
				value = new Integer(1);
			else
				value = new Integer(0);
		}
		
		public int compareTo(Object rawOther) 
		{
			if(! (rawOther instanceof ComparableBoolean) )
				return 0;
			ComparableBoolean other = (ComparableBoolean)rawOther;
			return value.compareTo(other.value);
		}
		
		Integer value;
	}

	MartusApp app;
	MiniLocalization localization;
	int columnCount;
	int currentSortColumn;
	
	ClientBulletinStore store;
	RetrieveSummariesProgressMeter progressHandler;
	protected Vector currentSummaries;
	private Vector downloadableSummaries;
	protected Vector allSummaries;
	Exception errorThrown;
	
	public int COLUMN_RETRIEVE_FLAG = -1;
	public int COLUMN_TITLE = -1;
	public int COLUMN_AUTHOR = -1;
	public int COLUMN_LAST_DATE_SAVED = -1;
	public int COLUMN_BULLETIN_SIZE = -1;
	public int COLUMN_DELETE_FLAG = -1;
	public int COLUMN_VERSION_NUMBER = -1;
}
