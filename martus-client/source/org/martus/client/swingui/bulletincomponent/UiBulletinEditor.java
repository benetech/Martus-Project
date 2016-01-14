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

package org.martus.client.swingui.bulletincomponent;

import java.io.IOException;

import org.martus.client.core.BulletinLanguageChangeListener;
import org.martus.client.swingui.HeadquartersSelectionListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.HeadquartersKeys;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.swing.UiTableWithCellEditingProtection;

public class UiBulletinEditor extends UiBulletinComponent implements HeadquartersSelectionListener
{
	public UiBulletinEditor(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		// ensure that attachmentEditor gets initialized
	}

	public UiBulletinComponentDataSection createBulletinComponentDataSection(String sectionName)
	{
		return new UiBulletinComponentEditorSection(getMainWindow(), sectionName);
	}

	public void validateData() throws DataInvalidException 
	{
		publicSection.validateData();
		privateSection.validateData();
	}
	
	public boolean isBulletinModified() throws Exception
	{		
		UiTableWithCellEditingProtection.savePendingEdits();
		
		Bulletin currentStateOfBulletinBeingEdited = getMainWindow().getApp().getStore().createEmptyBulletin();					
		copyDataToBulletin(currentStateOfBulletinBeingEdited);
		Bulletin previousStateOfBulletinBeingEdited = currentBulletin;
		if(currentStateOfBulletinBeingEdited.isAllPrivate() != currentBulletin.isAllPrivate())
			return true;
		
		if(publicSection.isAnyFieldModified(previousStateOfBulletinBeingEdited, currentStateOfBulletinBeingEdited))
			return true;
			
		if(privateSection.isAnyFieldModified(previousStateOfBulletinBeingEdited, currentStateOfBulletinBeingEdited))
			return true;

		if (isHeadquartersAuthorizedModified(previousStateOfBulletinBeingEdited, currentStateOfBulletinBeingEdited))
			return true;

		if (isPublicAttachmentModified())	
			return true;						
		
		if (isPrivateAttachmentModified())
			return true;
		
			
		return false;			
	}	
	
	
	private boolean isPublicAttachmentModified()
	{
		UiBulletinComponentEditorSection section = (UiBulletinComponentEditorSection)publicSection;
		AttachmentProxy[] publicAttachments = section.attachmentEditor.getAttachments();
		AttachmentProxy[] currentAttachments = currentBulletin.getPublicAttachments();
		
		if (isAnyAttachmentModified(currentAttachments, publicAttachments))
			return true;
		return false;
	}

	private boolean isPrivateAttachmentModified()
	{
		UiBulletinComponentEditorSection section = (UiBulletinComponentEditorSection)privateSection;
		AttachmentProxy[] currentAttachments = currentBulletin.getPrivateAttachments();
		AttachmentProxy[] privateAttachments = section.attachmentEditor.getAttachments();	
			
		if (isAnyAttachmentModified(currentAttachments, privateAttachments))
			return true;
		
		return false;
	}

	private boolean isAnyAttachmentModified(AttachmentProxy[] oldProxies, AttachmentProxy[] newProxies)
	{					
		if (oldProxies.length != newProxies.length)						
			return true;
		
		for(int aIndex = 0; aIndex < oldProxies.length; ++aIndex)
		{									
			String newLocalId = newProxies[aIndex].getUniversalId().getLocalId();
			String oldLocalId = oldProxies[aIndex].getUniversalId().getLocalId();			
						
			if (!newLocalId.equals(oldLocalId))
				return true;														
		}		
		return false;	
	}		
	
	public boolean isHeadquartersAuthorizedModified(Bulletin original, Bulletin newBulletin)
	{
		HeadquartersKeys orignialHQs = original.getAuthorizedToReadKeys();
		HeadquartersKeys newHQs = newBulletin.getAuthorizedToReadKeys();
		if(!orignialHQs.toStringWithLabel().equals(newHQs.toStringWithLabel()))
			return true;
		return false;
	}

	public void copyDataToBulletin(Bulletin bulletin) throws
		IOException,
		MartusCrypto.EncryptionException
	{	
		bulletin.clearAllUserData();
			
		bulletin.setAllPrivate(true);
		
		publicSection.copyDataToBulletin(bulletin);
		privateSection.copyDataToBulletin(bulletin);
		headquartersSection.copyDataToBulletin(bulletin);
		
		UiBulletinComponentEditorSection publicEditorSection = (UiBulletinComponentEditorSection)publicSection;
		AttachmentProxy[] publicAttachments = publicEditorSection.attachmentEditor.getAttachments();
		for(int aIndex = 0; aIndex < publicAttachments.length; ++aIndex)
		{
			AttachmentProxy a = publicAttachments[aIndex];
			bulletin.addPublicAttachment(a);
		}

		UiBulletinComponentEditorSection privateEditorSection = (UiBulletinComponentEditorSection)privateSection;
		AttachmentProxy[] privateAttachments = privateEditorSection.attachmentEditor.getAttachments();
		for(int aIndex = 0; aIndex < privateAttachments.length; ++aIndex)
		{
			AttachmentProxy a = privateAttachments[aIndex];
			bulletin.addPrivateAttachment(a);
		}

	}	

	public void setLanguageChangeListener(BulletinLanguageChangeListener listener)
	{
		languageListener = listener;
	}
	

	// LanguageChangeListener Interface
	public void bulletinLanguageHasChanged(String newLanguageCode)
	{
		if(languageListener != null)
			languageListener.bulletinLanguageHasChanged(newLanguageCode);
		
		super.bulletinLanguageHasChanged(newLanguageCode);
	}

	// HeadQuartersSelectionListener Interface
	public void selectedHQsChanged(int newNumberOfSelectedHQs) 
	{
		headerSection.updateNumberOfHQs(newNumberOfSelectedHQs);
	}
	
	protected UiBulletinComponentHeaderSection createHeaderSection()
	{
		return new UiBulletinComponentHeaderSection(getMainWindow(), "Modify");
	}
	
	protected UiBulletinComponentHeadQuartersSection createHeadQuartersSection()
	{
		UiBulletinComponentHeadQuartersEditor uiBulletinComponentHeadQuartersEditor = new UiBulletinComponentHeadQuartersEditor(this, getMainWindow(), currentBulletin, "Modify");
		return uiBulletinComponentHeadQuartersEditor;
	}


	boolean wasEncrypted;
	BulletinLanguageChangeListener languageListener;
}
