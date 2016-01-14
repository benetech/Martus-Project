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

import java.awt.Component;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.martus.client.core.BulletinLanguageChangeListener;
import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponentEditorSection;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponentInterface;
import org.martus.client.swingui.dialogs.UiBulletinModifyDlg;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.client.swingui.jfx.generic.FxDialogHelper;
import org.martus.client.swingui.jfx.generic.FxNonWizardShellController;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.Bulletin.BulletinState;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.DateRangeInvertedException;
import org.martus.common.fieldspec.DateTooEarlyException;
import org.martus.common.fieldspec.DateTooLateException;
import org.martus.common.fieldspec.RequiredFieldIsBlankException;

public class FxBulletinEditorShellController extends FxNonWizardShellController implements UiBulletinComponentInterface
{
	public FxBulletinEditorShellController(UiMainWindow mainWindowToUse, UiBulletinModifyDlg parentDialogToUse)
	{
		super(mainWindowToUse);
	
		parentDialog = parentDialogToUse;
	}
	
	@Override
	protected String getCssName()
	{
		return "Bulletin.css";
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		try
		{
			super.initialize(location, bundle);

			headerController = new BulletinEditorHeaderController(getMainWindow());
			loadControllerAndEmbedInPane(headerController, headerPane);
			shareButton.disableProperty().bind(headerController.getToFieldProperty().isEmpty());
			
			bodyController = new BulletinEditorBodyController(getMainWindow());
			loadControllerAndEmbedInPane(bodyController, bodyPane);
			
			footerController = new BulletinEditorFooterController(getMainWindow());
			loadControllerAndEmbedInPane(footerController, footerPane);
		}
		catch(Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	@Override
	public Component getComponent()
	{
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void scrollToTop()
	{
		Platform.runLater(() -> bodyController.scrollToTop());
	}

	@Override
	public void copyDataToBulletin(Bulletin bulletin) throws Exception
	{
		fxBulletin.copyDataToBulletin(bulletin);
	}

	@Override
	public void copyDataFromBulletin(Bulletin bulletinToShow) throws Exception
	{
		Platform.runLater(() -> copyDataFromBulletinOnFxThread(bulletinToShow));
	}
	
	private void copyDataFromBulletinOnFxThread(Bulletin bulletinToShow) throws RuntimeException
	{
		// NOTE: We have to create a new fxb each time, because the old one 
		// was probably bound to a bunch of controls, and we have no way to unbind 
		fxBulletin = new FxBulletin(getLocalization());

		try
		{
			fxBulletin.copyDataFromBulletin(bulletinToShow, getApp().getStore());
		} 
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		setImmutableOnServerBinding();
		Platform.runLater(() -> headerController.showBulletin(fxBulletin));
		Platform.runLater(() -> bodyController.showBulletin(fxBulletin));
		Platform.runLater(() -> footerController.showBulletin(fxBulletin));
	}

	private void setImmutableOnServerBinding()
	{
		BooleanProperty immutableOnServerProperty = fxBulletin.getImmutableOnServerProperty();
		immutableOnServer.selectedProperty().bindBidirectional(immutableOnServerProperty);
		
		boolean alwaysSetImmutableOnServer = getApp().getConfigInfo().getAlwaysImmutableOnServer();
		if(alwaysSetImmutableOnServer)
		{
			immutableOnServerProperty.set(true);
			immutableOnServer.setDisable(true);
		}
	}

	@Override
	public void validateData() throws DataInvalidException
	{
		fxBulletin.validateData();
		// TODO: The old code requested focus. In fx, it would be easier for 
		// us to just outline the field with red or something else visual
	}

	@Override
	public boolean isBulletinModified() throws Exception
	{
		if(fxBulletin == null)
			return false;
		return fxBulletin.hasBeenModified();
	}

	@Override
	public void updateEncryptedIndicator(boolean allPrivate)
	{
		// TODO: Needs an implementation
	}

	@Override
	public void setLanguageChangeListener(BulletinLanguageChangeListener listener)
	{
		// TODO: Needs an implementation
	}

	@Override
	public void bulletinLanguageHasChanged(String newBulletinLanguageCode)
	{
		// TODO: Needs an implementation
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/BulletinEditorShell.fxml";
	}
	
	private boolean validateAndNotifyUser()
	{
		try
		{	
			validateData();
			return true;
		}
		catch(UiDateEditor.DateFutureException e)
		{
			showNotifyDialog("ErrorDateInFuture", e.getlocalizedTag());
		}
		catch(DateRangeInvertedException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			showNotifyDialog("ErrorDateRangeInverted", "", map);
		}
		catch(DateTooEarlyException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			map.put("#MinimumDate#", getLocalization().convertStoredDateToDisplay(e.getMinimumDate()));
			showNotifyDialog("ErrorDateTooEarly", "", map);
		}
		catch(DateTooLateException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			map.put("#MaximumDate#", getLocalization().convertStoredDateToDisplay(e.getMaximumDate()));
			showNotifyDialog("ErrorDateTooLate", "", map);
		}
		catch(UiBulletinComponentEditorSection.AttachmentMissingException e)
		{
			showNotifyDialog("ErrorAttachmentMissing", e.getlocalizedTag());
		}
		catch(RequiredFieldIsBlankException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			showNotifyDialog("ErrorRequiredFieldBlank", "", map);
		}
		catch (Exception e) 
		{
			logAndNotifyUnexpectedError(e);
		}
		return false;
	}

	@FXML
	private void onSaveBulletin(ActionEvent event)
	{
		saveBulletinWithState(BulletinState.STATE_SAVE);
	}

	@FXML
	private void onVersionBulletin(ActionEvent event)
	{
		saveBulletinWithState(BulletinState.STATE_SNAPSHOT);
	}
	
	@FXML
	private void onShareBulletin(ActionEvent event)
	{
		saveBulletinWithState(BulletinState.STATE_SHARED);
	}

	private void saveBulletinWithState(final BulletinState state)
	{
		if(!validateAndNotifyUser())
			return;

		boolean neverDeleteFromOurServer = shouldDisallowDeleteFromServer(state);
		SwingUtilities.invokeLater(() -> parentDialog.saveBulletin(neverDeleteFromOurServer, state));
		SwingUtilities.invokeLater(() -> parentDialog.cleanupAndExit());
	}

	private boolean shouldDisallowDeleteFromServer(final BulletinState state)
	{
		boolean neverDeleteFromServerSelected = immutableOnServer.isSelected(); 
		if(state.equals(BulletinState.STATE_SHARED) || state.equals(BulletinState.STATE_SNAPSHOT))
			return neverDeleteFromServerSelected;
		return false;
	}
	
	@Override
	public boolean confirmDlg(JFrame parent, String baseTag)
	{
		return FxDialogHelper.showConfirmationDialog(getMainWindow(), baseTag);
	}
	
	@FXML
	private Pane headerPane;

	@FXML
	private Pane bodyPane;
	
	@FXML
	private Pane footerPane;
	
	@FXML
	private Button shareButton;
	
	@FXML
	private CheckBox immutableOnServer;

	private UiBulletinModifyDlg parentDialog;
	private BulletinEditorHeaderController headerController;
	private BulletinEditorBodyController bodyController;
	private BulletinEditorFooterController footerController;
	private FxBulletin fxBulletin;
}
