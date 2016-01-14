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
package org.martus.client.swingui.jfx.landing;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionMenuCharts;
import org.martus.client.swingui.actions.ActionMenuCreateNewBulletin;
import org.martus.client.swingui.actions.ActionMenuManageContactsWithoutResignIn;
import org.martus.client.swingui.actions.ActionMenuQuickEraseDeleteMyData;
import org.martus.client.swingui.actions.ActionMenuQuickSearch;
import org.martus.client.swingui.actions.ActionMenuReports;
import org.martus.client.swingui.actions.ActionMenuSearch;
import org.martus.client.swingui.jfx.generic.DialogWithNoButtonsShellController;
import org.martus.client.swingui.jfx.generic.FxNonWizardShellController;
import org.martus.client.swingui.jfx.generic.FxTabbedShellController;
import org.martus.client.swingui.jfx.landing.bulletins.BulletinListProvider;
import org.martus.client.swingui.jfx.landing.bulletins.BulletinsListController;
import org.martus.client.swingui.jfx.landing.cases.CaseListProvider;
import org.martus.client.swingui.jfx.landing.cases.FxCaseManagementController;
import org.martus.client.swingui.jfx.landing.general.AccountController;
import org.martus.client.swingui.jfx.landing.general.HelpController;
import org.martus.client.swingui.jfx.landing.general.SettingsController;
import org.martus.common.MartusLogger;
import org.martus.common.network.OrchidTransportWrapper;

public class FxLandingShellController extends FxNonWizardShellController
{
	public FxLandingShellController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		bulletinListProvider = new BulletinListProvider(getMainWindow());
		bulletinsListController = new BulletinsListController(getMainWindow(), bulletinListProvider);
		caseManagementController = new FxCaseManagementController(getMainWindow());
	}
	
	public BulletinsListController getBulletinsListController()
	{
		return bulletinsListController;
	}
	
	public CaseListProvider getAllCaseListProvider()
	{
		return caseManagementController.getAllCaseListProvider();
	}
	
	public BooleanBinding getShowTrashBinding()
	{
		return bulletinsListController.getTrashNotBeingDisplayedBinding();
	}
	
	public FxCaseManagementController getCaseManager()
	{
		return caseManagementController;
	}
	
	private void setupBindingForRecordCounts()
	{
		StringProperty recordCountProperty = bulletinListProvider.getRecordsBeingDisplayedProperty();
		titleBarNumberOfRecordsBeingShown.textProperty().bind(recordCountProperty);
	}


	public void setTitleBarToAll()
	{
		hideCloseCurrentViewButton();
		setTitleBarLabel("CaseAll");
	}
	
	public void setTitleBarToTrash()
	{
		showCloseCurrentViewButton();
		setTitleBarLabel("Trash");
	}
	
	public void setTitleBarToSent()
	{
		hideCloseCurrentViewButton();
		setTitleBarLabel("CaseSaved");
	}
	
	public void setTitleBarToReceived()
	{
		hideCloseCurrentViewButton();
		setTitleBarLabel("CaseReceived");
	}
	
	public void setTitleBarToSearch()
	{
		showCloseCurrentViewButton();
		setTitleBarLabel("notifySearchFound");
	}
	
	public void setTitleBarToCustomCase(String caseName)
	{
		hideCloseCurrentViewButton();
		setTitleBarTranslatedLabel(caseName);
	}
	
	private void setTitleBarLabel(String titleLabelTag)
	{
		String translatedLabel = getLocalization().getWindowTitle(titleLabelTag);
		setTitleBarTranslatedLabel(translatedLabel);
	}

	private void setTitleBarTranslatedLabel(String translatedLabel)
	{
		titleBarCaseNameBeingShown.setText(translatedLabel);
	}
	
	private void showCloseCurrentViewButton()
	{
		closeCurrentViewButton.setVisible(true);
	}
	
	private void hideCloseCurrentViewButton()
	{
		closeCurrentViewButton.setVisible(false);
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/LandingShell.fxml";
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		updateOnlineStatus();
		updateTorStatus();
		initializeTorListener();
		caseManagementController.addFolderSelectionListener(bulletinListProvider);
		caseManagementController.addFolderSelectionListener(bulletinsListController);
		setTitleBarToAll();
		setupBindingForRecordCounts();
	}

	private void initializeTorListener()
	{
		TorChangeListener torChangeListener = new TorChangeListener();
		Property<Boolean> configInfoUseInternalTorProperty = getApp().getConfigInfo().useInternalTorProperty();
		configInfoUseInternalTorProperty.addListener(torChangeListener);
		Property<Boolean> orchidTransportWrapperTorProperty = getApp().getTransport().getIsTorActiveProperty();
		orchidTransportWrapperTorProperty.addListener(torChangeListener);
	}

	@Override
	public Parent createContents() throws Exception
	{
		Parent contents = super.createContents();
		loadControllerAndEmbedInPane(caseManagementController, sideContentPane);
		loadControllerAndEmbedInPane(bulletinsListController, mainContentPane);
		return contents;
	}

	private void updateOnlineStatus()
	{
		boolean isOnline = getApp().getTransport().isOnline();
		toolbarImageViewOnline.setImage(getUpdatedOnOffStatusImage(isOnline));
		toolbarButtonOnline.setTooltip(getUpdatedToolTip(isOnline, "ServerCurrentlyOn", "ServerCurrentlyOff"));
		getMainWindow().updateServerStatusInStatusBar();
	}

	protected void updateTorStatus()
	{
		OrchidTransportWrapper transport = getApp().getTransport();
		boolean isTorEnabled = transport.isTorEnabled();
		toolbarImageViewTor.setImage(getUpdatedOnOffStatusImage(isTorEnabled));
		toolbarButtonTor.setTooltip(getUpdatedToolTip(isTorEnabled, "TorCurrentlyOn", "TorCurrentlyOff"));
	}

	private Tooltip getUpdatedToolTip(boolean enabled, String onMessage, String offMessage)
	{
		Tooltip tooltip = new Tooltip();
		String tooltipMessage = new String(getLocalization().getTooltipLabel(offMessage));
		if(enabled)
			tooltipMessage = getLocalization().getTooltipLabel(onMessage);
		tooltip.setText(tooltipMessage);
		return tooltip;
	}

	private Image getUpdatedOnOffStatusImage(boolean isOn)
	{
		Image onOffImage = new Image(getOnOffImagePath(isOn));
		return onOffImage;
	}

	private String getOnOffImagePath(boolean isOn)
	{
		String onOffImagePath = TOGGLE_OFF_IMAGE_PATH;
		if(isOn)
			onOffImagePath = TOGGLE_ON_IMAGE_PATH;
		return onOffImagePath;
	}
	
	class UpdateTorStatusLater implements Runnable
	{
		public void run()
		{
			updateTorStatus();
		}
	}

	
	private final class TorChangeListener implements ChangeListener<Boolean>
	{
		public TorChangeListener()
		{
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
		{
			runUpdateOnFxThread();
		}

		private void runUpdateOnFxThread()
		{
			Platform.runLater(new UpdateTorStatusLater());
		}
	}

	private void onSettings(String tabToDisplayFirst)
	{
		FxTabbedShellController settingsController = new SettingsController(getMainWindow());
		setTab(tabToDisplayFirst, settingsController);
	}

	private void onAccount(String tabToDisplayFirst)
	{
		FxTabbedShellController settingsController = new AccountController(getMainWindow());
		setTab(tabToDisplayFirst, settingsController);
	}
	
	private void setTab(String tabToDisplayFirst, FxTabbedShellController settingsController)
	{
		settingsController.setFirstTabToDisplay(tabToDisplayFirst);
		DialogWithNoButtonsShellController shellController = new DialogWithNoButtonsShellController(getMainWindow(), settingsController);
		doAction(shellController);
	}

	@FXML
	private void onConfigureServer(ActionEvent event)
	{
		//TODO remove this Old Swing doAction(new ActionMenuSelectServer(getMainWindow()));
		onSettings(SettingsController.SERVER_TAB);
	}
	
	@FXML
	private void onSystemPreferences(ActionEvent event)
	{
		//TODO remove this Old Swing doAction(new ActionMenuPreferences(getMainWindow()));
		onSettings(SettingsController.SYSTEM_TAB);
	}
	
	@FXML
	private void onTorPreferences(ActionEvent event)
	{
		onSettings(SettingsController.TOR_TAB);
	}

	@FXML
	private void onQuickSearch(ActionEvent event)
	{
		String displayableSearchText = searchText.getText();
		doAction(new  ActionMenuQuickSearch(getMainWindow(), displayableSearchText));
		setTitleBarToSearch();
	}
	
	@FXML 
	private void onAdvanceSearch(ActionEvent event)
	{
		doAction(new ActionMenuSearch(getMainWindow()));
		setTitleBarToSearch();
	}

	@FXML
	private void onCreateNewBulletin(ActionEvent event)
	{
		doAction(new ActionMenuCreateNewBulletin(getMainWindow()));
		caseManagementController.showAllCases();
	}
	
	@FXML
	private void onOnline(ActionEvent event)
	{
		boolean oldState = getApp().getTransport().isOnline();
		boolean newState = !oldState;
		getApp().turnNetworkOnOrOff(newState);
		updateOnlineStatus();
	}

	@FXML
	private void onTor(ActionEvent event)
	{
		boolean oldState = getApp().getTransport().isTorEnabled();
		try
		{
			ConfigInfo configInfo = getApp().getConfigInfo();
			boolean newState = !oldState;
			
			configInfo.setUseInternalTor(newState);
			getApp().saveConfigInfo();
		} 
		catch (SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorSavingConfig");
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	@FXML 
	private void onReports(ActionEvent event)
	{
		doAction(new ActionMenuReports(getMainWindow()));
	}
		
	@FXML 
	private void onCharts(ActionEvent event)
	{
		doAction(new ActionMenuCharts(getMainWindow()));
	}
	
	@FXML
	private void onAccountInformation(ActionEvent event)
	{
		onAccount(AccountController.ACCOUNT_INFORMATION_TAB_CODE);
	}

	@FXML
	private void onContactInformation(ActionEvent event)
	{
		onAccount(AccountController.CONTACT_INFORMATION_TAB_CODE);
	}
	
	@FXML
	public void onManageContacts(ActionEvent event)
	{
		doAction(new ActionMenuManageContactsWithoutResignIn(getMainWindow()));
	}
	
	@FXML
	private void onBackupKeypair(ActionEvent event)
	{
		onAccount(AccountController.KEY_BACKUP_TAB_CODE);
	}

	@FXML
	public void onLogoClicked(MouseEvent mouseEvent) 
	{
		try
		{
			BulletinsListController bulletinListController = getBulletinsListController();
			bulletinListController.loadAllBulletinsAndSortByMostRecent();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	@FXML
	public void onDeleteMyData(ActionEvent event)
	{
		if(showOkCancelConfirmationDialog("confirmDeleteMyData", "QuickEraseWillNotRemoveItems"))
		{
			doAction(new ActionMenuQuickEraseDeleteMyData(getMainWindow()));
		}
	}
	
	@FXML
	public void onHelpMenu(ActionEvent event)
	{
		showDialogWithClose("Help", new HelpController(getMainWindow()));	
	}
	
	@FXML
	public void onCloseCurrentView(ActionEvent event)
	{
		caseManagementController.showAllCases();
	}
	
	final private String TOGGLE_ON_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/toggle_on.png";
	final private String TOGGLE_OFF_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/toggle_off.png";
	
	@FXML
	private TextField searchText;
	
	@FXML
	private Button toolbarButtonOnline;
	
	@FXML
	private ImageView toolbarImageViewOnline;
	
	@FXML
	private Button toolbarButtonTor;
	
	@FXML
	private ImageView toolbarImageViewTor;

	@FXML
	private Pane sideContentPane;
	
	@FXML
	private Pane mainContentPane;
	
	@FXML
	private Label titleBarCaseNameBeingShown;
	
	@FXML
	private Label titleBarNumberOfRecordsBeingShown;

	@FXML
	private Button closeCurrentViewButton;
	
	private BulletinsListController bulletinsListController;
	private BulletinListProvider bulletinListProvider;
	private FxCaseManagementController caseManagementController;
}

