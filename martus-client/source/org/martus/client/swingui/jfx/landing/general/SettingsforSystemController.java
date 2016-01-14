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

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.setupwizard.step2.FxSetupSettingsController;
import org.martus.client.swingui.jfx.setupwizard.step6.FxSelectLanguageController;
import org.martus.clientside.CurrentUiState;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.ChoiceItem;

public class SettingsforSystemController extends FxController
{
	public SettingsforSystemController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		ConfigInfo configInfo = getApp().getConfigInfo();
		useZawgyiFont.selectedProperty().setValue(configInfo.getUseZawgyiFont());
		immutableOnServer.selectedProperty().setValue(configInfo.getAlwaysImmutableOnServer());
		MartusLocalization localization = getLocalization();
		initializeLanguageChoices(localization);
		initializeDateFormatChoices(localization);
		initializeDateDelimiterChoices(localization);
		initializeCalendarChoices(localization);
		hideLanguageChoice();
	}

	private void hideLanguageChoice()
	{
		languageChoiceLabel.setVisible(false);
		languageSelection.setVisible(false);
	} 
		
	private void initializeCalendarChoices(MartusLocalization localization)
	{
		ChoiceItem[] calendarChoices = localization.getAvailableCalendarSystems();
		calendarType.setItems(FXCollections.observableArrayList(calendarChoices));
		FxSetupSettingsController.selectItemByCode(calendarType, localization.getCurrentCalendarSystem());
	}

	private void initializeDateDelimiterChoices(MartusLocalization localization)
	{
		ObservableList<ChoiceItem> dateDelimeterChoices = FxSetupSettingsController.getDateDelimeterChoices(localization);
		dateDelimiter.setItems(FXCollections.observableArrayList(dateDelimeterChoices));
		String dateDelimeterCode = "" + localization.getDateDelimiter();
		FxSetupSettingsController.selectItemByCode(dateDelimiter, dateDelimeterCode);
	}

	private void initializeDateFormatChoices(MartusLocalization localization)
	{
		ObservableList<ChoiceItem> dateFormatChoices = FxSetupSettingsController.getDateFormatChoices(localization);
		dateFormat.setItems(FXCollections.observableArrayList(dateFormatChoices));
		String dateFormatCode = localization.getMdyOrder();
		FxSetupSettingsController.selectItemByCode(dateFormat, dateFormatCode);
	}

	private void initializeLanguageChoices(MartusLocalization localization)
	{
		ObservableList<ChoiceItem> availableLanguages = FXCollections.observableArrayList(FxSelectLanguageController.getAvailableLanguages(localization));
		languageSelection.setItems(availableLanguages);
		originalLanguageChoiceItem = FxSelectLanguageController.findCurrentLanguageChoiceItem(getLocalization());
		languageSelection.getSelectionModel().select(originalLanguageChoiceItem);
		languageSelection.getSelectionModel().selectedItemProperty().addListener(new LanguageSelectionListener());
		updateUseZawgyiFontButton(originalLanguageChoiceItem);
	}
	
	class LanguageSelectionListener implements ChangeListener<ChoiceItem>
	{
		@Override
		public void changed(ObservableValue<? extends ChoiceItem> observableValue,
				ChoiceItem oldItem, ChoiceItem newItem)
		{
			updateZawgyiFont(newItem);
		}
	}
	
	protected void updateZawgyiFont(ChoiceItem itemSelected)
	{
		if(itemSelected.getCode().equals(MiniLocalization.BURMESE))
			useZawgyiFont.selectedProperty().setValue(true);
		else
			useZawgyiFont.selectedProperty().setValue(false);
		updateUseZawgyiFontButton(itemSelected);
	}
	
	private void updateUseZawgyiFontButton(ChoiceItem selection)
	{
		if(selection.getCode().equals(MiniLocalization.BURMESE))
			useZawgyiFont.setDisable(true);
		else
			useZawgyiFont.setDisable(false);
	}

	@Override
	public void save()
	{
		try
		{
			getApp().saveConfigInfo();
			getMainWindow().saveCurrentUiState();
		} 
		catch (SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorSavingConfig");
		}
		super.save();
		getMainWindow().getMainStage().getBulletinsListController().updateContents();
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/general/SettingsForSystem.fxml";
	}
	
	@FXML
	public void onSaveChanges(ActionEvent event)
	{
		MartusLocalization localization = getLocalization();
		ConfigInfo configInfo = getApp().getConfigInfo();
		configInfo.setUseZawgyiFont(useZawgyiFont.selectedProperty().getValue());
		configInfo.setAlwaysImmutableOnServer(immutableOnServer.selectedProperty().getValue());
		localization.setMdyOrder(dateFormat.getSelectionModel().getSelectedItem().getCode());
		String delimiter = dateDelimiter.getSelectionModel().getSelectedItem().getCode();
		localization.setDateDelimiter(delimiter.charAt(0));
		
		CurrentUiState uiState = getMainWindow().getCurrentUiState();
		uiState.setCurrentDateFormat(localization.getCurrentDateFormatCode());
		uiState.setCurrentCalendarSystem(calendarType.getSelectionModel().getSelectedItem().getCode());
		
		String selectedLanguageCode = languageSelection.getSelectionModel().getSelectedItem().getCode();
		//TODO is this check really needed?
		if (MtfAwareLocalization.isRecognizedLanguage(selectedLanguageCode))
		{
			if(!originalLanguageChoiceItem.getCode().equals(selectedLanguageCode))
				showNotifyDialog("RestartMartusForLanguageChange");
			getStage().doAction(new ActionDisplayMTFWarningsIfNecessary(getMainWindow(), selectedLanguageCode));
			localization.setCurrentLanguageCode(selectedLanguageCode);
		}
		save();
	}
	
	public class ActionDisplayMTFWarningsIfNecessary implements ActionDoer
	{
		public ActionDisplayMTFWarningsIfNecessary(UiMainWindow mainWindowToUse, String selectedLanguageToCheck)
		{
			mainWindow = mainWindowToUse;
			selectedLanguageCode = selectedLanguageToCheck;
		}

		public void doAction()
		{
			mainWindow.displayPossibleUnofficialIncompatibleTranslationWarnings(selectedLanguageCode);
		}		
		private UiMainWindow mainWindow;
		private String selectedLanguageCode;
	}	

	@FXML 
	private CheckBox useZawgyiFont;
	
	@FXML
	private ChoiceBox<ChoiceItem> languageSelection;
	
	@FXML Label languageChoiceLabel;
	
	private ChoiceItem originalLanguageChoiceItem;
	
	@FXML
	private ChoiceBox<ChoiceItem> dateFormat;
	
	@FXML
	private ChoiceBox<ChoiceItem> dateDelimiter;
	
	@FXML
	private ChoiceBox<ChoiceItem> calendarType;

	@FXML
	private CheckBox immutableOnServer;
	
}
