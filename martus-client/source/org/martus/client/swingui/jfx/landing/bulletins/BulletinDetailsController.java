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

import java.awt.Dimension;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.martus.client.core.FxBulletin;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiBulletinDetailsDialog;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.FxInSwingModalDialog;
import org.martus.common.ContactKeys;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.packet.UniversalId;

public class BulletinDetailsController extends FxController
{

	public BulletinDetailsController(UiMainWindow mainWindowToUse, FxBulletin bulletinToShow)
	{
		super(mainWindowToUse);
		bulletin = bulletinToShow; 
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		try
		{
			UniversalId bulletinId = bulletin.universalIdProperty().getValue();
			String accountId = bulletinId.getAccountId();
			MartusApp app = getApp();
			authorName.setText(getAuthorName(app, accountId));
			
			publicCode.setText(MartusCrypto.computeFormattedPublicCode40(accountId));
			Integer verificationStatus = app.getKeyVerificationStatus(accountId);
			Image verificationImage = AuthorVerifiedColumnHandler.getVerificationImage(verificationStatus);
			contactVerificationImage.setImage(verificationImage);
			
			bulletinLocalId.textProperty().bind(bulletin.bulletinLocalIdProperty());
			dateCreated.textProperty().bind(bulletin.fieldProperty(Bulletin.TAGENTRYDATE));
			
			String dateSaved = UiBulletinDetailsDialog.getSavedDateToDisplay(bulletinId, bulletinId, getMainWindow());
			dateLastSaved.setText(dateSaved);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	@Override
	protected Dimension getPreferredDimension()
	{
		return FxInSwingModalDialog.MEDIUM_PREFERRED_DIALOG_SIZE;
	}


	//TODO add unit tests
	static public String getAuthorName(MartusApp app, String accountId) throws Exception
	{
		String author = null;
		if(app.getAccountId().equals(accountId))
		{
			author = app.getUserName();
		}
		else
		{
			ContactKeys ourContacts = app.getContactKeys();
			String name = "";
			if(ourContacts.containsKey(accountId))
				name = ourContacts.getLabelIfPresent(accountId);
			
			if(name.isEmpty())
				name = MartusCrypto.computeFormattedPublicCode40(accountId);
			author = name;
		}
		return author;
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/FxBulletinDetails.fxml";
	}
	
	@FXML
	private Label authorName;
	
	@FXML
	private Label publicCode;

	@FXML
	private Label bulletinLocalId;

	@FXML
	private Label dateCreated;

	@FXML
	private Label dateLastSaved;
	
	@FXML
	private ImageView contactVerificationImage;

	private FxBulletin bulletin;
}
