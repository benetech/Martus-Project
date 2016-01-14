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
package org.martus.client.swingui.jfx.setupwizard;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.common.ContactKey;
import org.martus.common.DammCheckDigitAlgorithm.CheckDigitInvalidException;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.swing.FontHandler;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

public class ContactsTableData
{
	public ContactsTableData(ContactKey contact) throws InvalidBase64Exception, CreateDigestException, CheckDigitInvalidException
	{
		publicKey = contact.getPublicKey();
		contactName = new SimpleStringProperty(getDisplayableContactName(contact));
		publicCode = new SimpleStringProperty(contact.getFormattedPublicCode40());
		sendToByDefault = new SimpleBooleanProperty(contact.getSendToByDefault());
		verificationStatus = new SimpleIntegerProperty(contact.getVerificationStatus());
		removeContact = new SimpleStringProperty("X");
	}

	public ContactKey getContact()
	{
		String storableLabel = getStorableContactName();
		ContactKey contact = new ContactKey(publicKey, storableLabel);
		contact.setSendToByDefault(sendToByDefault.get());
		contact.setVerificationStatus(verificationStatus.get());
		return contact;
	}

	private String getStorableContactName()
	{
		return getUiFontEncodingHelper().getStorable(contactName.get());
	}

	private String getDisplayableContactName(ContactKey contact)
	{
		return getUiFontEncodingHelper().getDisplayable(contact.getLabel());
	}

	private UiFontEncodingHelper getUiFontEncodingHelper()
	{
		boolean doZawgyiConversion = FontHandler.isDoZawgyiConversion();
		
		return new UiFontEncodingHelper(doZawgyiConversion);
	}
	
	public String getContactName()
	{
		return contactName.get();
	}
	
	public void setContactName(String contactNameToUse)
	{
		contactName.set(contactNameToUse);
	}
	
    public SimpleStringProperty contactNameProperty() 
    { 
        return contactName; 
    }

    public String getRemoveContact()
	{
		return removeContact.get();
	}
	
	public void setRemoveContact(String removeContactToUse)
	{
		removeContact.set(removeContactToUse);
	}

	public String getPublicCode()
	{
		return publicCode.get();
	}
	
	public void setPublicCode(String publicCodeToUse)
	{
	}
	
	public void setVerificationStatus(int verificationStatusToUse)
	{
		verificationStatus.set(verificationStatusToUse);
	}
	
	public int getVerificationStatus()
	{
		return verificationStatus.get();
	}
	
	public boolean getSendToByDefault()
	{
		return sendToByDefault.get();
	}
	
	public void setSendToByDefault(boolean sendToByDefaultToUse)
	{
		sendToByDefault.set(sendToByDefaultToUse);
	}
	
	public SimpleBooleanProperty sendToByDefaultProperty() 
	{
		return sendToByDefault;
	}
	
	public SimpleIntegerProperty verificationStatusProperty()
	{
		return verificationStatus;
	}

	public static final String CONTACT_NAME_PROPERTY_NAME = "contactName";
	public static final String PUBLIC_CODE_PROPERTY_NAME = "publicCode";
	public static final String SEND_TO_BY_DEFAULT_PROPERTY_NAME = "sendToByDefault";
	public static final String REMOVE_CONTACT_PROPERTY_NAME = "removeContact"; //THIS LOOKS WRONG
	public static final String VERIFICATION_STATUS_PROPERTY_NAME = "verificationStatus";

	private final SimpleStringProperty contactName;
	private final SimpleStringProperty publicCode;
	private final SimpleBooleanProperty sendToByDefault;
	private final SimpleStringProperty removeContact;
	private final SimpleIntegerProperty verificationStatus;

	private String publicKey;
}
