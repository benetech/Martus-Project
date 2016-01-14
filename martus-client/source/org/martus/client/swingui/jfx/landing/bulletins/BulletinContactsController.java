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
package org.martus.client.swingui.jfx.landing.bulletins;

import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.common.ContactKeys;
import org.martus.common.HeadquartersKey;

public class BulletinContactsController extends FxController
{

	public BulletinContactsController(UiMainWindow mainWindowToUse, Vector currentAuthorizedKeysToUse)
	{
		super(mainWindowToUse);
		initialAuthorizedKeys = currentAuthorizedKeysToUse;
		availableListAuthorizedToReadKeys = FXCollections.observableArrayList();
	}
	
	public Vector getCurrentAuthorizedKeys()
	{
		Vector newAuthorizedKeys = new Vector();
		for (Iterator newContactsList = availableListAuthorizedToReadKeys.iterator(); newContactsList.hasNext();)
		{
			ContactKeyCheckBox contact = (ContactKeyCheckBox) newContactsList.next();
			if(contact.isSelected())
				newAuthorizedKeys.add(contact.getKey());
		}
		return newAuthorizedKeys;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		try
		{
			ContactKeys ourContacts = getApp().getContactKeys();
			addPredefinedBulletinContactsToList(ourContacts);
			addRemainingContactsToList(ourContacts);
			contactsList.setItems(availableListAuthorizedToReadKeys);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	public void setViewingContactsOnly()
	{
		selectAll.setVisible(false);
		ObservableList<Label> readOnlyList = FXCollections.observableArrayList();
		for (ContactKeyCheckBox contactKeyCheckBox : availableListAuthorizedToReadKeys)
		{
			if(contactKeyCheckBox.isSelected())
				readOnlyList.add(new Label(contactKeyCheckBox.getText()));
		}
		contactsList.setItems(readOnlyList);
	}
	
	private void addPredefinedBulletinContactsToList(ContactKeys ourContacts)
	{
		initialAuthorizedKeys.forEach(key -> addKeyToAvailableList(key, ourContacts, SELECT_CONTACT_INITIALLY));
	}

	private void addRemainingContactsToList(ContactKeys ourContacts)
	{
		Vector<HeadquartersKey> remainingUnauthorizedContacts = new Vector();
		for(int i = 0; i < ourContacts.size(); ++i)
		{
			remainingUnauthorizedContacts.add(new HeadquartersKey(ourContacts.get(i)));
		}
		initialAuthorizedKeys.forEach(authorizedContactKey -> removedAlreadyAddedAuthorizedContacts(authorizedContactKey, remainingUnauthorizedContacts));
		remainingUnauthorizedContacts.forEach(unauthorizedContactKey -> addKeyToAvailableList(unauthorizedContactKey, ourContacts, ADDITIONAL_CONTACT_NOT_SELECTED_INITIALLY));
	}

	private void removedAlreadyAddedAuthorizedContacts(HeadquartersKey keyAlreadyAdded, Vector<HeadquartersKey> remainingUnauthorizedContacts)
	{
		for(int i = 0; i < remainingUnauthorizedContacts.size(); ++i)
		{
			HeadquartersKey currentKey = remainingUnauthorizedContacts.get(i);
			if(currentKey.getPublicKey().equals(keyAlreadyAdded.getPublicKey()))
				remainingUnauthorizedContacts.remove(i);
		}
	}

	private class ContactKeyCheckBox extends CheckBox
	{
		public ContactKeyCheckBox(HeadquartersKey keyToUse, String label)
		{
			super(label);
			key = keyToUse;
		}
		
		public HeadquartersKey getKey()
		{
			return key;
		}
		
		private HeadquartersKey key;
	}
	
	private void addKeyToAvailableList(HeadquartersKey key, ContactKeys ourContacts, boolean contactShouldBePreSelected)
	{
		try
		{
			String contactsName = BulletinEditorHeaderController.getContactsNameOrPublicCode(getLocalization(), getApp(), key, ourContacts);
			ContactKeyCheckBox contactCheckbox = new ContactKeyCheckBox(key, contactsName);
			contactCheckbox.selectedProperty().set(contactShouldBePreSelected);
			availableListAuthorizedToReadKeys.add(contactCheckbox);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/FxBulletinContacts.fxml";
	}

	@FXML
	private void onSelectAll(ActionEvent event) 
	{
		boolean selectAllItems = selectAll.isSelected();
		availableListAuthorizedToReadKeys.forEach(checkbox -> checkbox.selectedProperty().set(selectAllItems));
	}

	@FXML
	private CheckBox selectAll;
	
	@FXML
	private ListView contactsList;

	private final boolean SELECT_CONTACT_INITIALLY = true;
	private final boolean ADDITIONAL_CONTACT_NOT_SELECTED_INITIALLY = false;

	private Vector<HeadquartersKey> initialAuthorizedKeys;
	private ObservableList<ContactKeyCheckBox> availableListAuthorizedToReadKeys;
}
