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
import java.util.ResourceBundle;
import java.util.Vector;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.common.HeadquartersKey;

public class BulletinViewerController extends FxController
{
	public BulletinViewerController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		try
		{
			FxFormCreator creator = new FxFormViewCreator(getLocalization());
			StackPane attachmentsPane = createAttachmentsPane(bulletin);
			StackPane detailsPane = createDetailsPane(bulletin);
			StackPane contactsPane = createContactsPane(bulletin);
			Node root = creator.createFormFromBulletin(bulletin, attachmentsPane, detailsPane, contactsPane);
			scrollPane.setContent(root);
			scrollPane.setFitToWidth(true);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	@Override
	public String getCssName()
	{
		return "Bulletin.css";
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/BulletinViewerBody.fxml";
	}

	public void setBulletin(FxBulletin bulletinToShow) throws RuntimeException
	{
		bulletin = bulletinToShow;
	}

	private StackPane createAttachmentsPane(FxBulletin bulletinToShow) throws Exception
	{
		StackPane attachmentsPane = new StackPane();
		BulletinAttachmentsController attachmentsController = new BulletinAttachmentsController(getMainWindow(), bulletinToShow);
		loadControllerAndEmbedInPane(attachmentsController, attachmentsPane);
		attachmentsController.setViewingAttachmentsOnly();
		return attachmentsPane;
	}
	
	private StackPane createDetailsPane(FxBulletin bulletinToShow) throws Exception
	{
		StackPane detailsPane = new StackPane();
		BulletinDetailsController detailsController = new BulletinDetailsController(getMainWindow(), bulletinToShow);
		loadControllerAndEmbedInPane(detailsController, detailsPane);
		return detailsPane;
	}

	private StackPane createContactsPane(FxBulletin bulletinToShow) throws Exception
	{
		StackPane contactsPane = new StackPane();
		ObservableList<HeadquartersKey>currentAuthorizedKeysToUse = bulletinToShow.getAuthorizedToReadList();
		Vector keysAllowedToRead = new Vector();
		currentAuthorizedKeysToUse.forEach(key -> keysAllowedToRead.add(key));
				
		BulletinContactsController contactsController = new BulletinContactsController(getMainWindow(), keysAllowedToRead);
		loadControllerAndEmbedInPane(contactsController, contactsPane);
		contactsController.setViewingContactsOnly();
		return contactsPane;
	}

	public void scrollToTop()
	{
		scrollPane.vvalueProperty().set(0);
	}

		
	@FXML
	private ScrollPane scrollPane;

	private FxBulletin bulletin;
}
