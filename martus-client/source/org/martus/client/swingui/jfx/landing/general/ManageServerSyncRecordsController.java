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
package org.martus.client.swingui.jfx.landing.general;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.bulletinstore.ClientBulletinStore.BulletinAlreadyExistsException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.landing.AbstractFxLandingContentController;
import org.martus.client.swingui.tablemodels.RetrieveHQDraftsTableModel;
import org.martus.client.swingui.tablemodels.RetrieveHQTableModel;
import org.martus.client.swingui.tablemodels.RetrieveMyDraftsTableModel;
import org.martus.client.swingui.tablemodels.RetrieveMyTableModel;
import org.martus.client.swingui.tablemodels.RetrieveTableModel;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;


public class ManageServerSyncRecordsController extends AbstractFxLandingContentController
{
	public ManageServerSyncRecordsController(UiMainWindow mainWindowToUse) throws Exception
	{
		super(mainWindowToUse);
		getServerRecords();
	}

	@Override
	public void initializeMainContentPane()
	{
		initalizeColumns();
		initalizeItemsTable();

		RecordSelectedListener recordSelectedListener = new RecordSelectedListener();
		allRecordsTable.getSelectionModel().selectedItemProperty().addListener(recordSelectedListener);
		updateButtons();
	}
	
	private void initalizeItemsTable()
	{
		Label noRecords = new Label(getLocalization().getFieldLabel("NoServerSyncDataInTable"));
		allRecordsTable.setPlaceholder(noRecords);
		allRecordsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		syncRecordsTableProvider = new SyncRecordsTableProvider(getMainWindow());
		allRecordsTable.setItems(syncRecordsTableProvider);
		try
		{
			Set localRecords = getLocalRecords();
			syncRecordsTableProvider.addBulletinsAndSummaries(localRecords, serverMyDrafts, serverMySealeds, serverHQDrafts, serverHQSealeds);
			onShowAll(null);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private Set getLocalRecords()
	{
		return getApp().getStore().getAllBulletinLeafUids();
	}

	public void getServerRecords() throws Exception
	{
		showBusyDialogWithCancel(getLocalization().getFieldLabel("RetrievingRecordSummariesFromServer"), new UpdateAllRecordsTask());
	}
	
	class UpdateAllRecordsTask extends Task
	{
		@Override
		protected Object call() throws Exception
		{
			//TODO: should all be on separate background threads to improve performance 
			if(!isCancelled())
				serverMyDrafts = getServerMyDrafts();
			if(!isCancelled())
				serverMySealeds = getServerMySealeds();
			if(!isCancelled())
				serverHQDrafts = getServerHQDrafts();
			if(!isCancelled())
				serverHQSealeds = getServerHQSealeds();
			return null;
		}
			
		private Vector getServerMyDrafts() throws Exception
		{
			RetrieveTableModel model = new RetrieveMyDraftsTableModel(getApp(), getLocalization());
			model.initialize(null);
			return model.getAllSummaries();
		}

		private Vector getServerMySealeds() throws Exception
		{
			RetrieveTableModel model = new RetrieveMyTableModel(getApp(), getLocalization());
			model.initialize(null);
			return model.getAllSummaries();
		}

		private Vector getServerHQDrafts() throws Exception
		{
			RetrieveTableModel model = new RetrieveHQDraftsTableModel(getApp(), getLocalization());
			model.initialize(null);
			return model.getAllSummaries();
		}

		private Vector getServerHQSealeds() throws Exception
		{
			RetrieveTableModel model = new RetrieveHQTableModel(getApp(), getLocalization());
			model.initialize(null);
			return model.getAllSummaries();
		}
		
		
		
	}


	private void initalizeColumns()
	{
		recordLocationColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, String>(ServerSyncTableRowData.LOCATION_PROPERTY_NAME));
		recordLocationColumn.setCellFactory(TextFieldTableCell.<ServerSyncTableRowData>forTableColumn());
		recordTitleColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, String>(ServerSyncTableRowData.TITLE_PROPERTY_NAME));
		recordTitleColumn.setCellFactory(TextFieldTableCell.<ServerSyncTableRowData>forTableColumn());
		recordAuthorColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, String>(ServerSyncTableRowData.AUTHOR_PROPERTY_NAME));
		recordAuthorColumn.setCellFactory(TextFieldTableCell.<ServerSyncTableRowData>forTableColumn());
		recordLastSavedColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, String>(ServerSyncTableRowData.DATE_SAVDED_PROPERTY_NAME));
		recordLastSavedColumn.setCellFactory(TextFieldTableCell.<ServerSyncTableRowData>forTableColumn());
		recordSizeColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, Integer>(ServerSyncTableRowData.SIZE_PROPERTY_NAME));
		recordSizeColumn.setCellFactory(new RecordSizeColumnHandler());
	}

	private class RecordSelectedListener implements ChangeListener<ServerSyncTableRowData>
	{
		public RecordSelectedListener()
		{
		}

		@Override
		public void changed(ObservableValue<? extends ServerSyncTableRowData> observalue	,
				ServerSyncTableRowData previousRecord, ServerSyncTableRowData newRecord)
		{
			updateButtons();
		}
	}

	protected void updateButtons()
	{
		ObservableList<ServerSyncTableRowData> rowsSelected = allRecordsTable.getSelectionModel().getSelectedItems();
		boolean isAnythingDeleteable = false;
		boolean isAnythingUploadable = false;
		boolean isAnythingDownloadable = false;
		for (Iterator iterator = rowsSelected.iterator(); iterator.hasNext();)
		{
			ServerSyncTableRowData data = (ServerSyncTableRowData) iterator.next();
			if(data == null)
				return; 
			if(data.canDeleteFromServerProperty().getValue())
				isAnythingDeleteable = true;
			if(data.canUploadToServerProperty().getValue())
				isAnythingUploadable = true;
			if(data.isRemote().getValue())
				isAnythingDownloadable = true;
		}
		deleteButton.setDisable(!isAnythingDeleteable);
		uploadButton.setDisable(!isAnythingUploadable);
		downloadButton.setDisable(!isAnythingDownloadable);
		
		updateLocationLinks();
		updateSubFilterLinks();
	}

	private void updateLocationLinks()
	{
		int location = syncRecordsTableProvider.getLocation();
		fxLocationAll.setVisited(location == ServerSyncTableRowData.LOCATION_ANY);
		fxLocationLocalOnly.setVisited(location == ServerSyncTableRowData.LOCATION_LOCAL);
		fxLocationServerOnly.setVisited(location == ServerSyncTableRowData.LOCATION_SERVER);
		fxLocationBoth.setVisited(location == ServerSyncTableRowData.LOCATION_BOTH);
	}

	private void updateSubFilterLinks()
	{
		int subFilter = syncRecordsTableProvider.getSubFilter();
		fxSubFilterAll.setVisited(subFilter == SyncRecordsTableProvider.SUB_FILTER_ALL);
		fxSubFilterMyRecords.setVisited(subFilter == SyncRecordsTableProvider.SUB_FILTER_MY_RECORDS);
		fxSubFilterSharedWithMe.setVisited(subFilter == SyncRecordsTableProvider.SUB_FILTER_SHARED_WITH_ME);
	}

	private void closeDialog()
	{
		getStage().close();
	}

	private void DisplayWarningDialog( String warningTag, StringBuilder titlesInQuestion)
	{
		showNotifyDialog(warningTag, titlesInQuestion.toString());
	}
	
	private void updateTable(int TableToShow)
	{
		syncRecordsTableProvider.setSubFilter(SyncRecordsTableProvider.SUB_FILTER_ALL);
		syncRecordsTableProvider.show(TableToShow);
		updateButtons();
	}
	
	private void filterTable(int filter)
	{
		syncRecordsTableProvider.setSubFilter(filter);
		syncRecordsTableProvider.filterResults();
		updateButtons();
	}
		
	@Override
	public String getFxmlLocation()
	{
		return "landing/general/ManageServerSyncRecords.fxml";
	}
	
	public void addToInvalidRecords(StringBuilder serverOnlyRecords,
			ServerSyncTableRowData recordData)
	{
		serverOnlyRecords.append(TITLE_SEPARATOR);
		serverOnlyRecords.append(recordData.getTitle());
	}

	@FXML 	
	private void onUpload(ActionEvent event)
	{
		ObservableList<ServerSyncTableRowData> selectedRows = allRecordsTable.getSelectionModel().getSelectedItems();
		StringBuilder serverOnlyRecords = new StringBuilder();
		ClientBulletinStore store = getApp().getStore();
		BulletinFolder draftOutBox = store.getFolderDraftOutbox();
		BulletinFolder sealedOutBox = store.getFolderSealedOutbox();
		for (Iterator iterator = selectedRows.iterator(); iterator.hasNext();)
		{
			ServerSyncTableRowData recordData = (ServerSyncTableRowData) iterator.next();
			if(recordData.getRawLocation() == ServerSyncTableRowData.LOCATION_SERVER)
			{
				addToInvalidRecords(serverOnlyRecords, recordData);
			}
			else
			{
				String accountId = getApp().getAccountId();
				Bulletin bulletin =  store.getBulletinRevision(recordData.getUniversalId());
				if(!bulletin.getBulletinHeaderPacket().isAuthorizedToUpload(accountId))
				{
					addToInvalidRecords(serverOnlyRecords, recordData);
					continue;
				}

				try
				{
					if(bulletin.isMutable())
						draftOutBox.add(bulletin);
					if(bulletin.isImmutable())
						sealedOutBox.add(bulletin);
				} 
				catch (BulletinAlreadyExistsException ignored)
				{
				} 
				catch (IOException e)
				{
					addToInvalidRecords(serverOnlyRecords, recordData);
					logAndNotifyUnexpectedError(e);
				}
			}
		}
		if(serverOnlyRecords.length()>0)
			DisplayWarningDialog("SyncUnableToUploadServerFiles", serverOnlyRecords);

		closeDialog();
	}

	@FXML 	
	private void onDownload(ActionEvent event)
	{
		ObservableList<ServerSyncTableRowData> selectedRows = allRecordsTable.getSelectionModel().getSelectedItems();
		StringBuilder localOnlyRecords = new StringBuilder();
		Vector<UniversalId> uidsToDownload = new Vector(selectedRows.size());
		for (Iterator iterator = selectedRows.iterator(); iterator.hasNext();)
		{
			ServerSyncTableRowData recordData = (ServerSyncTableRowData) iterator.next();
			if(recordData.getRawLocation() == ServerSyncTableRowData.LOCATION_LOCAL)
				addToInvalidRecords(localOnlyRecords, recordData);
			else
				uidsToDownload.add(recordData.getUniversalId());
		}
		if(localOnlyRecords.length()>0)
			DisplayWarningDialog("SyncUnableToDownloadLocalFiles", localOnlyRecords);
		try
		{
			String retrievedFolder = getApp().getNameOfFolderForAllRetrieved();
			getMainWindow().retrieveRecordsFromServer(retrievedFolder, uidsToDownload);
			closeDialog();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	@FXML 	
	private void onDelete(ActionEvent event)
	{
		ObservableList<ServerSyncTableRowData> selectedRows = allRecordsTable.getSelectionModel().getSelectedItems();
		StringBuilder localOrImmutableRecords = new StringBuilder();
		Vector<UniversalId> uidsToDelete = new Vector(selectedRows.size());
		for (Iterator iterator = selectedRows.iterator(); iterator.hasNext();)
		{
			ServerSyncTableRowData recordData = (ServerSyncTableRowData) iterator.next();
			if(recordData.canDeleteFromServerProperty().getValue())
				uidsToDelete.add(recordData.getUniversalId());
			else
				addToInvalidRecords(localOrImmutableRecords, recordData);
		}
		if(localOrImmutableRecords.length()>0)
			DisplayWarningDialog("SyncUnableToDeleteLocalOnlyOrImmutableFiles", localOrImmutableRecords);
		try
		{
			getMainWindow().deleteMutableRecordsFromServer(uidsToDelete);
			closeDialog();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	@FXML 	
	private void onShowAll(ActionEvent event)
	{
		updateTable(ServerSyncTableRowData.LOCATION_ANY);
	}

	@FXML 	
	private void onShowLocalOnly(ActionEvent event)
	{
		updateTable(ServerSyncTableRowData.LOCATION_LOCAL);
	}

	@FXML 	
	private void onShowServerOnly(ActionEvent event)
	{
		updateTable(ServerSyncTableRowData.LOCATION_SERVER);
	}

	@FXML 	
	private void onShowBoth(ActionEvent event)
	{
		updateTable(ServerSyncTableRowData.LOCATION_BOTH);
	}

	@FXML 	
	private void onSubfilterAll(ActionEvent event)
	{
		filterTable(SyncRecordsTableProvider.SUB_FILTER_ALL);
	}

	@FXML 	
	private void onSubfilterMyRecords(ActionEvent event)
	{
		filterTable(SyncRecordsTableProvider.SUB_FILTER_MY_RECORDS);
	}

	@FXML 	
	private void onSubfilterSharedWithMe(ActionEvent event)
	{
		filterTable(SyncRecordsTableProvider.SUB_FILTER_SHARED_WITH_ME);
	}

	private final String TITLE_SEPARATOR = "\n";
	
	@FXML
	private Hyperlink fxLocationAll;
	
	@FXML
	private Hyperlink fxLocationLocalOnly;
	
	@FXML
	private Hyperlink fxLocationServerOnly;
	
	@FXML
	private Hyperlink fxLocationBoth;
	
	@FXML
	private Hyperlink fxSubFilterAll;
	
	@FXML
	private Hyperlink fxSubFilterMyRecords;
	
	@FXML
	private Hyperlink fxSubFilterSharedWithMe;
	
	@FXML
	private TableView<ServerSyncTableRowData> allRecordsTable;
	
	@FXML
	private TableColumn<ServerSyncTableRowData, String> recordLocationColumn;

	@FXML
	private TableColumn<ServerSyncTableRowData, String> recordTitleColumn;

	@FXML
	private TableColumn<ServerSyncTableRowData, String> recordAuthorColumn;

	@FXML
	private TableColumn<ServerSyncTableRowData, String> recordLastSavedColumn;

	@FXML
	private TableColumn<ServerSyncTableRowData, Integer> recordSizeColumn;
	
	@FXML 
	private Button uploadButton;
	
	@FXML 
	private Button downloadButton;

	@FXML 
	private Button deleteButton;
	
	protected Vector serverMyDrafts;
	protected Vector serverMySealeds;
	protected Vector serverHQDrafts;
	protected Vector serverHQSealeds;
	
	private SyncRecordsTableProvider syncRecordsTableProvider;
}
