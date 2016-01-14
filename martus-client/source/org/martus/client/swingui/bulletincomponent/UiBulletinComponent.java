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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Scrollable;

import org.martus.client.core.BulletinLanguageChangeListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.FieldSpecCollection;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.packet.FieldDataPacket;
import org.martus.swing.Utilities;
import org.martus.util.language.LanguageOptions;

import com.jhlabs.awt.Alignment;
import com.jhlabs.awt.GridLayoutPlus;

abstract public class UiBulletinComponent extends JPanel implements Scrollable, BulletinLanguageChangeListener, UiBulletinComponentInterface 
{
	@Override
	abstract public void setLanguageChangeListener(BulletinLanguageChangeListener listener);
	abstract protected UiBulletinComponentDataSection createBulletinComponentDataSection(String sectionName);
	@Override
	abstract public void copyDataToBulletin(Bulletin bulletin) throws
			IOException, MartusCrypto.EncryptionException;
	@Override
	abstract public void validateData() throws DataInvalidException; 
	@Override
	abstract public boolean isBulletinModified() throws Exception;
	abstract protected UiBulletinComponentHeaderSection createHeaderSection();
	abstract protected UiBulletinComponentHeadQuartersSection createHeadQuartersSection();

	@Override
	public void updateEncryptedIndicator(boolean allPrivate)
	{
		if(allPrivate)
			return;
		
		//FIXME: This is where we would provide an indication to the user that 
		// the existing data is public, but newly saved data is all private
	}
	
	// LanguageChangeListener interface
	@Override
	public void bulletinLanguageHasChanged(String newBulletinLanguageCode)
	{
		publicSection.updateSpellChecker(newBulletinLanguageCode);
		privateSection.updateSpellChecker(newBulletinLanguageCode);
	}



	protected UiBulletinComponent(UiMainWindow mainWindowToUse)
	{
		GridLayoutPlus layout = new GridLayoutPlus();
		layout.setFill(Alignment.FILL_NONE);
		if(LanguageOptions.isRightToLeftLanguage())
			layout.setAlignment(Alignment.EAST);
		setLayout(layout);
		mainWindow = mainWindowToUse;
	}

	private void createSections()
	{
		headerSection = createHeaderSection();
		publicSection = createDataSection(Bulletin.TOP_SECTION, currentBulletin.getTopSectionFieldSpecs(), ALWAYS_ENCRYPTED);
		privateSection = createDataSection(Bulletin.BOTTOM_SECTION, currentBulletin.getBottomSectionFieldSpecs(), ALWAYS_ENCRYPTED);
		headquartersSection = createHeadQuartersSection();
		
		ensureSectionsLineUp();
		
		add(headerSection);
		add(publicSection);
		add(privateSection);
		add(headquartersSection);
	}
	
	private UiBulletinComponentDataSection createDataSection(String section, 
			FieldSpecCollection fieldSpecs, int encryptionStatus)
	{
		UiBulletinComponentDataSection target = createBulletinComponentDataSection(section);
		target.createLabelsAndFields(fieldSpecs, this);

		return target;
	}
	
	private void ensureSectionsLineUp()
	{
		publicSection.matchFirstColumnWidth(privateSection);
		privateSection.matchFirstColumnWidth(publicSection);
	}

	@Override
	public void copyDataFromBulletin(Bulletin bulletinToShow) throws Exception
	{
		removeAll();
		currentBulletin = bulletinToShow;
		if(currentBulletin == null)
		{
			headerSection = null;
			publicSection = null;
			privateSection = null;
			headquartersSection = null;
			repaint();
			return;
		}
		
		createSections();
		
		headerSection.setBulletin(currentBulletin);

		FieldDataPacket publicData = currentBulletin.getFieldDataPacket();
		publicSection.clearAttachments();
		publicSection.copyDataFromPacket(publicData);
		publicSection.clearWarningIndicator();

		FieldDataPacket privateData = currentBulletin.getPrivateFieldDataPacket();
		privateSection.clearAttachments();
		privateSection.copyDataFromPacket(privateData);
		privateSection.clearWarningIndicator();

		String accountId = mainWindow.getApp().getAccountId();
		
		String authorPublicKeyString = currentBulletin.getAccount();
		boolean notYourBulletin = !authorPublicKeyString.equals(accountId);
		boolean notAuthorizedToRead = !currentBulletin.getAuthorizedToReadKeys().containsKey(accountId);
		mainWindow.setWaitingCursor();
		boolean isBulletinValid = mainWindow.getStore().isBulletinValid(currentBulletin);

		boolean isFieldDeskBulletin = mainWindow.getApp().isVerifiedFieldDeskAccount(authorPublicKeyString);
		
		mainWindow.resetCursor();
		
		if(!isBulletinValid || (notYourBulletin && notAuthorizedToRead))
		{
			String text;
			if(notYourBulletin)
			{
				text = mainWindow.getLocalization().getFieldLabel("NotAuthorizedToViewPrivate");
				if(currentBulletin.isAllPrivate())
					publicSection.setWarningText(text);
			}
			else
			{
				System.out.println("Damaged: " + currentBulletin.getLocalId());
				text = mainWindow.getLocalization().getFieldLabel("MayBeDamaged");
				publicSection.setWarningText(text);
			}
			privateSection.setWarningText(text);
		}
		else if(currentBulletin.hasUnknownTags())
		{
			System.out.println("Unknown tags: " + currentBulletin.getLocalId());
			String text = mainWindow.getLocalization().getFieldLabel("BulletinHasUnknownStuff");
			publicSection.setWarningText(text);
			privateSection.setWarningText(text);
		}
		else if(notYourBulletin && !isFieldDeskBulletin)
		{
			String text = mainWindow.getLocalization().getFieldLabel("BulletinUnverifiedFieldDesk");
			publicSection.setWarningText(text);
			privateSection.setWarningText(text);
			
		}
		else if(notYourBulletin)
		{
			String text = mainWindow.getLocalization().getFieldLabel("BulletinNotYours");
			publicSection.setInformationalText(text);
			privateSection.setInformationalText(text);
			
		}

		MartusField languageField = bulletinToShow.getField(BulletinConstants.TAGLANGUAGE);
		bulletinLanguageHasChanged(languageField.getData());
		repaint();
	}

	@Override
	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	// Scrollable interface
	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 20;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 100;
	}

	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}
	// End scrollable interface
	
	@Override
	public void scrollToTop()
	{
		Utilities.forceScrollerToTop(this);
	}
	
	@Override
	public Component getComponent()
	{
		return this;
	}
	
	public boolean confirmDlg(JFrame parent, String baseTag)
	{
		return getMainWindow().confirmDlg(parent, baseTag);
	}

	private UiMainWindow mainWindow;

	protected Bulletin currentBulletin;
	protected UiBulletinComponentHeaderSection headerSection;
	protected UiBulletinComponentDataSection publicSection;
	protected UiBulletinComponentDataSection privateSection;	
	protected UiBulletinComponentHeadQuartersSection headquartersSection;

	private static final int ALWAYS_ENCRYPTED = 2;
}
