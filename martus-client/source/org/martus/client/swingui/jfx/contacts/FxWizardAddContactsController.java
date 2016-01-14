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
package org.martus.client.swingui.jfx.contacts;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxPopupController;
import org.martus.client.swingui.jfx.generic.FxTableCellTextFieldFactory;
import org.martus.client.swingui.jfx.generic.VirtualStage;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.client.swingui.jfx.setupwizard.ContactsTableData;
import org.martus.client.swingui.jfx.setupwizard.step4.FxStep4Controller;
import org.martus.client.swingui.jfx.setupwizard.step5.FxSetupImportTemplatesController;
import org.martus.client.swingui.jfx.setupwizard.tasks.GetAccountTokenFromServerTask;
import org.martus.client.swingui.jfx.setupwizard.tasks.LookupAccountFromTokenTask;
import org.martus.common.ContactKey;
import org.martus.common.ContactKeys;
import org.martus.common.DammCheckDigitAlgorithm.CheckDigitInvalidException;
import org.martus.common.Exceptions.NetworkOfflineException;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.Exceptions.ServerNotCompatibleException;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusAccountAccessToken.TokenNotFoundException;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.common.crypto.MartusSecurity;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class FxWizardAddContactsController extends FxStep4Controller
{
	public FxWizardAddContactsController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initializeMainContentPane()
	{
		//TODO remove this and figure out a better solution in FXML
		contactsVbox.setMaxWidth(MAX_TABLE_WIDTH_IN_WIZARD);
		
		contactNameColumn.setCellValueFactory(new PropertyValueFactory<Object, String>(ContactsTableData.CONTACT_NAME_PROPERTY_NAME));
		contactNameColumn.setCellFactory(new FxTableCellTextFieldFactory());

		publicCodeColumn.setEditable(false);
		publicCodeColumn.setCellValueFactory(new PropertyValueFactory<Object, String>(ContactsTableData.PUBLIC_CODE_PROPERTY_NAME));
	    publicCodeColumn.setCellFactory(TextFieldTableCell.forTableColumn());

		sendToByDefaultColumn.setCellValueFactory(new PropertyValueFactory<ContactsTableData, Boolean>(ContactsTableData.SEND_TO_BY_DEFAULT_PROPERTY_NAME));
		sendToByDefaultColumn.setCellFactory(CheckBoxTableCell.<ContactsTableData>forTableColumn(sendToByDefaultColumn));

		verificationStatusColumn.setCellValueFactory(new PropertyValueFactory<ContactsTableData, String>(ContactsTableData.VERIFICATION_STATUS_PROPERTY_NAME));
		verificationStatusColumn.setCellFactory(new TableColumnVerifyContactCellFactory(getLocalization()));

		removeContactColumn.setCellValueFactory(new PropertyValueFactory<ContactsTableData, String>(ContactsTableData.REMOVE_CONTACT_PROPERTY_NAME)); 
	    removeContactColumn.setCellFactory(new TableColumnRemoveButtonCellFactory(getLocalization()));

	    sendToByDefaultColumn.setVisible(false);
		contactsTable.setItems(getContactsTableData());
		Label noContacts = new Label(getLocalization().getFieldLabel("NoContactsInTable"));
		contactsTable.setPlaceholder(noContacts);
		loadExistingContactData();
		updateAddContactButtonState();
		accessTokenField.textProperty().addListener(new AccessTokenChangeHandler());
		
		getContactsTableData().addListener(new TableRowAddRemoveChangeHandler());
		addTableDataListener();
	}

	private void addTableDataListener()
	{
		for(ContactsTableData contactData : getContactsTableData())
		{
			TableDataChangeListener tableDataChangeListener = new TableDataChangeListener();
			contactData.contactNameProperty().addListener(tableDataChangeListener);
			contactData.sendToByDefaultProperty().addListener(tableDataChangeListener);
			contactData.verificationStatusProperty().addListener(tableDataChangeListener);
		}
	}
	
	protected void removeContactFromTable(ContactsTableData contactData)
	{
		getContactsTableData().remove(contactData);
	}

	protected ContactsTableData getSelectedContact()
	{
		return contactsTable.getSelectionModel().getSelectedItem();
	}
	
	@FXML
	public void addContact() 
	{
		try
		{
			MartusAccountAccessToken token = new MartusAccountAccessToken(accessTokenField.getText());
			LookupAccountFromTokenTask task = new LookupAccountFromTokenTask(getApp(), token);
			MartusLocalization localization = getLocalization();
			String message = localization.getFieldLabel("FindAccountByToken");
			showTimeoutDialog(message, task);
			String contactAccountId = task.getFoundAccountId();
			if(contactAccountId == null)
				return; 
			verifyContactAndAddToTable(contactAccountId);
		} 
		catch(NetworkOfflineException e)
		{
			showNotifyDialog("ErrorNetworkOffline");
		}
		catch(UserCancelledException e)
		{
			return;
		}
		catch (ServerNotAvailableException e)
		{
			showNotifyDialog("ContactsNoServer");
		} 
		catch (ServerNotCompatibleException e)
		{
			showNotifyDialog("ServerNotCompatible");
		}
		catch (TokenNotFoundException e)
		{
			showNotifyDialog("UnableToRetrieveContactFromServer");
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("UnexpectedError");
		} 
	}

	protected void verifyContactAndAddToTable(String contactAccountId) throws CheckDigitInvalidException, CreateDigestException, TokenInvalidException
	{
		if(contactAccountId.equals(getApp().getAccountId()))
		{
			showNotifyDialog("ContactKeyIsOurself");
			return;
		}
		String contactPublicCode = MartusSecurity.computeFormattedPublicCode40(contactAccountId);
		if(doesContactAlreadyExistInTable(contactPublicCode))
		{
			showContactAlreadyExists(contactPublicCode);
			return;
		}
		ContactsTableData newContact = verifyContact(new ContactKey(contactAccountId), false);
		if(newContact != null)
		{
			getContactsTableData().add(newContact);
			clearAccessTokenField();
		}
	}

	protected void showContactAlreadyExists(String contactPublicCode)
			throws TokenInvalidException
	{
		String contactsName = getContactsNameInTable(contactPublicCode);
		String contactExistsWithName = TokenReplacement.replaceToken(getLocalization().getFieldLabel("ContactAlreadyExistsAs"), "#Name#", contactsName);
		showNotifyDialog("ContactKeyAlreadyExists", contactExistsWithName);
	}

	protected boolean doesContactAlreadyExistInTable(String contactPublicCode)
	{
		ObservableList<ContactsTableData> contactsTableData = getContactsTableData();
		for(int i=0; i < contactsTableData.size(); ++i)
		{
			ContactsTableData contactData = contactsTableData.get(i);
			if(contactData.getPublicCode().equals(contactPublicCode))
				return true;
		}
		return false;
	}

	private String getContactsNameInTable(String contactPublicCode)
	{
		ObservableList<ContactsTableData> contactsTableData = getContactsTableData();
		for(int i=0; i < contactsTableData.size(); ++i)
		{
			ContactsTableData contactData = contactsTableData.get(i);
			if(contactData.getPublicCode().equals(contactPublicCode))
				return contactData.getContactName();
		}
		return "";
	}
	

	ContactsTableData verifyContact(ContactKey currentContact, boolean verifyOnly)
	{
		try
		{
			VerifyContactPopupController popupController = new VerifyContactPopupController(getMainWindow(), currentContact);
			if(verifyOnly)
				popupController.setVerificationOnly();
			popupController.showOldPublicCode(showOldPublicCode);
			showControllerInsideModalDialog(popupController);
			if(popupController.hasContactBeenAccepted())
			{
				int verification = popupController.getVerification();
				currentContact.setVerificationStatus(verification);
				return new ContactsTableData(currentContact); 
			}
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
		return null;
	}

	private void clearAccessTokenField()
	{
		accessTokenField.setText("");
	}
		
	final class TableColumnVerifyContactCellFactory implements Callback<TableColumn<ContactsTableData, String>, TableCell<ContactsTableData, String>>
	{
		public TableColumnVerifyContactCellFactory(MartusLocalization localizationToUse)
		{
			super();
			localization = localizationToUse;
		}
		
		final class TableCellUpdateHandler extends TableCell
		{
			final class ContactVerifierHandler implements EventHandler<ActionEvent>
			{
				@Override
				public void handle(ActionEvent event) 
				{
					int index = getIndex();
					ContactKey currentContactSelected = getContactsTableData().get(index).getContact();
					ContactsTableData contactData = verifyContact(currentContactSelected, true);
					if(contactData != null)
						getContactsTableData().set(index, contactData);

				}
			}
			
			TableCellUpdateHandler(TableColumn tableColumn)
			{
				this.tableColumn = tableColumn;
			}
			
			@Override
			public void updateItem(Object item, boolean empty) 
			{
			    super.updateItem(item, empty);
			    if (empty) 
			    {
			        setText(null);
			        setGraphic(null);
			    } 
			    else 
			    {
			    		int verificationStatus = (Integer)item;
			    		String labelText = getVerificationStatusLabel(verificationStatus);
			    		final Node verificationStatusCell;
			    		if(verificationStatus == ContactKey.NOT_VERIFIED)
			    		{
			    			verificationStatusCell = new Hyperlink(labelText);
			    			((Hyperlink)verificationStatusCell).setOnAction(new ContactVerifierHandler());
			    			verificationStatusCell.getStyleClass().add("unverified-hyperlink");

			    		}
			    		else
			    		{
			    			verificationStatusCell = new Label(labelText);
			    			verificationStatusCell.getStyleClass().add("verified-label");
			    		}
		    			setGraphic(verificationStatusCell);
			    	}
			}
			
			private String getVerificationStatusLabel(Integer verificationStatusCode)
			{
				String statusCode = null;
				if (verificationStatusCode == ContactKey.NOT_VERIFIED)
					statusCode = localization.getFieldLabel("ContactVerifyNow");
				else if (verificationStatusCode == ContactKey.VERIFIED_ENTERED_20_DIGITS
						|| verificationStatusCode == ContactKey.VERIFIED_VISUALLY)
					statusCode = localization.getFieldLabel("ContactVerified");
				else
					statusCode = "?";
				
				return statusCode;
			}
			
			protected final TableColumn tableColumn;
		}

		@Override
		public TableCell call(final TableColumn param) 
		{
			return new TableCellUpdateHandler(param);
		}	
		
		protected MartusLocalization localization;
	}	
	
	final class TableColumnRemoveButtonCellFactory implements Callback<TableColumn<ContactsTableData, String>, TableCell<ContactsTableData, String>>
	{
		public TableColumnRemoveButtonCellFactory(MartusLocalization localizationToUse)
		{
			super();
			localization = localizationToUse;
		}

		final class ButtonCellUpdateHandler extends TableCell
		{
			final class RemoveButtonHandler implements EventHandler<ActionEvent>
			{
				@Override
				public void handle(ActionEvent event) 
				{
					tableColumn.getTableView().getSelectionModel().select(getIndex());
					ContactsTableData contactData = getSelectedContact();
					String contactName = contactData.getContactName();
					String contactPublicCode = contactData.getPublicCode();
					HashMap map = new HashMap();
					map.put("#Name#", contactName);
					map.put("#PublicCode#", contactPublicCode);
					try
					{
						String confirmationMessage = TokenReplacement.replaceTokens(localization.getFieldLabel("RemoveContactLabel"), map);
						if(showConfirmationDialog("RemoveContact", confirmationMessage))
							removeContactFromTable(contactData);
					} 
					catch (TokenInvalidException e)
					{
						MartusLogger.logException(e);
						showNotifyDialog("UnexpectedError");
					}
				}
			}
			
			ButtonCellUpdateHandler(TableColumn tableColumn)
			{
				this.tableColumn = tableColumn;
			}
			
			@Override
			public void updateItem(Object item, boolean empty) 
			{
			    super.updateItem(item, empty);
			    if (empty) 
			    {
			        setText(null);
			        setGraphic(null);
			    } 
			    else 
			    {
			        final Button removeContactButton = new Button((String)item);
			        removeContactButton.getStyleClass().add("remove-contact-button");
			        removeContactButton.setOnAction(new RemoveButtonHandler());
			        setGraphic(removeContactButton);
			    	}
			}
			protected final TableColumn tableColumn;
		}

		@Override
		public TableCell call(final TableColumn param) 
		{
			return new ButtonCellUpdateHandler(param);
		}
		protected MartusLocalization localization;
	}

	public static class VerifyContactPopupController extends FxPopupController implements Initializable
	{
		public VerifyContactPopupController(UiMainWindow mainWindowToUse, ContactKey contactToVerify)
		{
			super(mainWindowToUse);
			try
			{
				contactPublicCode = contactToVerify.getFormattedPublicCode();
				contactPublicCode40 = contactToVerify.getFormattedPublicCode40();
			} catch (Exception e)
			{
				MartusLogger.logException(e);
			} 
			verification=ContactKey.NOT_VERIFIED;
			contactAccepted = false;
		}
		
		public void setVerificationOnly()
		{
			verifyContact = true;
		}
		
		public void showOldPublicCode(boolean showOldCode)
		{
			showOldPublicCode = showOldCode;
		}
		
		@Override
		public void initialize()
		{
			contactPublicCode40Label.setText(contactPublicCode40);
			contactPublicCodeLabel.setText(contactPublicCode);
			contactPublicCodeLabel.setVisible(showOldPublicCode);
			labelOldPublicCode.setVisible(showOldPublicCode);
			if(showOldPublicCode)
				verificationMessage.setText(getLocalization().getFieldLabel("VerifyPublicCodeNewAndOld"));
			else
				verificationMessage.setText(getLocalization().getFieldLabel("VerifyPublicCode"));
		}
		
		@Override
		public String getFxmlLocation()
		{
			return "contacts/VerifyContactPopup.fxml";
		}

		@Override
		public String getDialogTitle()
		{
			String title = "notifyAddContact";
			if(verifyContact)
				title = "notifyVerifyContact";
			return getLocalization().getWindowTitle(title); 
		}

		@FXML
		public void willVerifyLater()
		{
			verification=ContactKey.NOT_VERIFIED;
			contactAccepted = true;
			getStage().close();
		}
		
		@FXML
		public void verifyContact()
		{
			verification=ContactKey.VERIFIED_VISUALLY;
			contactAccepted = true;
			getStage().close();
		}

		@FXML
		private Label contactPublicCodeLabel;

		@FXML
		private Label contactPublicCode40Label;
		
		@FXML
		private Label labelOldPublicCode;
		
		@FXML
		private TextArea verificationMessage;
		
		public int getVerification()
		{
			return verification;
		}
		
		public boolean hasContactBeenAccepted()
		{
			return contactAccepted;
		}
		
		public void setFxStage(VirtualStage stageToUse)
		{
			fxStage = stageToUse;
		}

		public VirtualStage getFxStage()
		{
			return fxStage;
		}

		private String contactPublicCode;
		private String contactPublicCode40;
		private VirtualStage fxStage;
		private int verification;
		private boolean contactAccepted;
		private boolean verifyContact;
		private boolean showOldPublicCode;
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step4/ManageContacts.fxml";
	}
	
	@Override
	public String getSidebarFxmlLocation()
	{
		return "setupwizard/step4/ManageContactsSidebar.fxml";
	}
	
	@Override
	public AbstractFxSetupWizardContentController getNextController()
	{
		return new FxSetupImportTemplatesController(getMainWindow());
	}

	public void nextWasPressed() throws Exception
	{
		saveContacts();
		super.nextWasPressed();
	}
	
	public void backWasPressed() throws Exception
	{
		saveContacts();
		super.backWasPressed();
	}
	
	public void saveContacts()
	{
		ContactKeys allContactsInTable = new ContactKeys();
		ObservableList<ContactsTableData> contactsTableData = getContactsTableData();
		for(int i =0; i < contactsTableData.size(); ++i)
		{
			ContactKey contact = contactsTableData.get(i).getContact();
			allContactsInTable.add(contact);
		}
		try
		{
			getApp().setContactKeys(allContactsInTable);
		} 
		catch (SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
		}
	}
	
	
	protected class AccessTokenChangeHandler implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
		{
			updateAddContactButtonState();
		}
	}
	
	protected void updateAddContactButtonState()
	{
		String candidateToken = accessTokenField.getText();
		boolean canAdd = (isValidAccessToken(candidateToken));

		Button nextButton = getWizardNavigationHandler().getNextButton();
		if(candidateToken.length() == 0)
		{
			addContactButton.setDefaultButton(false);
			nextButton.setDefaultButton(true);
		}
		else if(canAdd)
		{
			nextButton.setDefaultButton(false);
			addContactButton.setDefaultButton(true);
		}
		else
		{
			nextButton.setDefaultButton(false);
			addContactButton.setDefaultButton(true);
		}

		addContactButton.setDisable(!canAdd);
	}
	

	private boolean isValidAccessToken(String tokenToValidate)
	{
		if(tokenToValidate.length() == 0)
			return false;
		
		return MartusAccountAccessToken.isTokenValid(tokenToValidate);
	}
	
	private void loadExistingContactData()
	{
		ObservableList<ContactsTableData> contactsTableData = getContactsTableData();
		contactsTableData.clear();
		
		try
		{
			ContactKeys keys = getApp().getContactKeys();
			for(int i = 0; i < keys.size(); ++i)
			{
				ContactKey contact = keys.get(i);
				ContactsTableData contactData = new ContactsTableData(contact); 
				contactsTableData.add(contactData);
			}
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
		
	}
	
	protected void showOldPublicCodeDuringVerification()
	{
		showOldPublicCode = true;
	}
	
	@FXML 
	void onGetToken()
	{
		MartusApp martusApp = getApp();
		if(accountToken == null)
		{
			try
			{
				GetAccountTokenFromServerTask task = new GetAccountTokenFromServerTask(martusApp);
				showTimeoutDialog(getLocalization().getFieldLabel("ConnectingToServerToRetrieveToken"), task);
				accountToken = task.getToken();
			}
			catch (NetworkOfflineException e)
			{
				showNotifyDialog("ErrorNetworkOffline");
			}
			catch (ServerNotCompatibleException e)
			{
				showNotifyDialog("ServerNotCompatible");
				return;
			}
			catch (ServerNotAvailableException e)
			{
				showNotifyDialog("ServerNotAvailable");
				return;
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
				showNotifyDialog("UnexpectedError");
				return;
			}
		}			
		try
		{
			String tokenData = accountToken.getToken();
			String publicCode = MartusCrypto.computeFormattedPublicCode40(martusApp.getAccountId());
				
			Map tokenReplacement = new HashMap();
			tokenReplacement.put("#Token#", tokenData);
			tokenReplacement.put("#PublicCode#", publicCode);
			showNotifyDialog("ShowTokenAndPublicCode", tokenReplacement);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("UnexpectedError");
		}
			
	}
	
	protected ObservableList<ContactsTableData> getContactsTableData()
	{
		return data;
	}
	
	protected void setDataChanged()
	{
		hasDataChanged = true;
	}
	
	public boolean hasContactsDataChanged()
	{
		return hasDataChanged;
	}
	
	protected class TableDataChangeListener implements ChangeListener
	{
		@Override
		public void changed(ObservableValue arg0, Object arg1, Object arg2)
		{
			setDataChanged();
		}
	}
	
	protected class TableRowAddRemoveChangeHandler implements ListChangeListener<ContactsTableData>
	{
		@Override
		public void onChanged(javafx.collections.ListChangeListener.Change<? extends ContactsTableData> arg0)
		{
			setDataChanged();
		}
	}

	@FXML 
	protected TableView<ContactsTableData> contactsTable;
	
	@FXML
	protected TableColumn<Object, String> contactNameColumn;
	
	@FXML
	protected TableColumn<ContactsTableData, Boolean> sendToByDefaultColumn;
	
	
	@FXML
	protected TableColumn<Object, String> publicCodeColumn;
	
	@FXML
	protected TableColumn<ContactsTableData, String> verificationStatusColumn;
	
	@FXML
	protected TableColumn<ContactsTableData, String> removeContactColumn;
	
	@FXML
	protected TextField accessTokenField;
	
	@FXML
	protected Button addContactButton;
	
	@FXML
	protected Label fxAddManageContactLabel;
	
	@FXML
	protected Label contactsOverviewLabel;
	
	@FXML
	protected Label sidebarHintContacts;
	
	@FXML
	protected VBox contactsVbox;
	
	private ObservableList<ContactsTableData> data = FXCollections.observableArrayList();
	
	private boolean showOldPublicCode;
	
	private MartusAccountAccessToken accountToken;
	private boolean hasDataChanged;

	private static final int MAX_TABLE_WIDTH_IN_WIZARD = 660;
}
