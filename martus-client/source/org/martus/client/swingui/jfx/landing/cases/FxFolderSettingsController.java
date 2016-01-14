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
package org.martus.client.swingui.jfx.landing.cases;

import java.awt.Dimension;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import org.martus.client.core.ConfigInfo;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.DialogWithOkCancelContentController;
import org.martus.client.swingui.jfx.generic.FxInSwingModalDialog;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.common.fieldspec.ChoiceItem;

public class FxFolderSettingsController extends DialogWithOkCancelContentController
{
	public FxFolderSettingsController(UiMainWindow mainWindowToUse, ChangeListener folderLabelIndexListenertoUse, ChangeListener folderCustomLabelListenerToUse)
	{
		super(mainWindowToUse);
		config = mainWindowToUse.getApp().getConfigInfo();
		folderLabelIndexListener = folderLabelIndexListenertoUse;
		folderCustomLabelListener = folderCustomLabelListenerToUse;
	}
	
	public void initialize()
	{
		MartusLocalization localization = getMainWindow().getLocalization();
		ObservableChoiceItemList folderNameChoices = getFolderLabelChoices(localization);
		fxFolderChoiceBox.setItems(folderNameChoices);
		String folderNameCustom = config.getFolderLabelCustomName();

		String folderNameCode = config.getFolderLabelCode();
		if(folderNameCode.isEmpty())
			folderNameCode = FOLDER_CODE_DEFAULT;
		ChoiceItem initialChoice = folderNameChoices.findByCode(folderNameCode);
		if(initialChoice == null)
			initialChoice = folderNameChoices.get(0);
		fxFolderChoiceBox.getSelectionModel().select(initialChoice);
		updateCustomFolder();

		ReadOnlyObjectProperty<ChoiceItem> selectedItemProperty = fxFolderChoiceBox.getSelectionModel().selectedItemProperty();
		selectedItemProperty.addListener(new FolderNameChoiceBoxListener());
		selectedItemProperty.addListener(folderLabelIndexListener);
		
		fxFolderCustomTextField.setText(folderNameCustom);
		fxFolderCustomTextField.textProperty().addListener(new FolderNameCustomLabelListener());
		fxFolderCustomTextField.textProperty().addListener(folderCustomLabelListener);
	}
	
	
	protected void setFolderLabelCode(String code)
	{
		config.setFolderLabelCode(code);
		updateCustomFolder();
	}
	
	protected void setFolderLabelCustomName(String customName)
	{
		config.setFolderLabelCustomName(customName);
		getMainWindow().saveConfigInfo();
	}
	
	private final class FolderNameChoiceBoxListener implements ChangeListener<ChoiceItem>
	{
		public FolderNameChoiceBoxListener()
		{
		}

		@Override public void changed(ObservableValue<? extends ChoiceItem> observableValue, ChoiceItem originalItem, ChoiceItem newItem) 
		{
			setFolderLabelCode(newItem.getCode());
		}
	}
	
	private final class FolderNameCustomLabelListener implements ChangeListener<String>
	{
		public FolderNameCustomLabelListener()
		{
		}

		@Override public void changed(ObservableValue<? extends String> observableValue, String original, String newLabel) 
		{
			setFolderLabelCustomName(newLabel);
		}
	}
	
	protected void updateCustomFolder()
	{
		if(config.getFolderLabelCode().equals(FOLDER_CODE_CUSTOM))
		{
			fxFolderCustomTextField.setVisible(true);
		}
		else
		{
			fxFolderCustomTextField.setVisible(false);
		}
		getMainWindow().saveConfigInfo();
	}
	
	public static ObservableChoiceItemList getFolderLabelChoices(MartusLocalization localization)
	{
		ObservableChoiceItemList folderChoices = new ObservableChoiceItemList(FXCollections.observableArrayList());

		folderChoices.add(new ChoiceItem(FOLDER_CODE_CASES, localization.getFieldLabel("FolderNameCases")));
		folderChoices.add(new ChoiceItem(FOLDER_CODE_INCIDENTS, localization.getFieldLabel("FolderNameIncidents")));
		folderChoices.add(new ChoiceItem(FOLDER_CODE_PROJECTS, localization.getFieldLabel("FolderNameProjects")));
		folderChoices.add(new ChoiceItem(FOLDER_CODE_CUSTOM, localization.getFieldLabel("FolerNameUserDefined")));
		return folderChoices;
	}	
		
	public String getFolderLabel(String folderCodeToFind)
	{
		String folderLabelCustomName = config.getFolderLabelCustomName();
		MartusLocalization localization = getLocalization();
		return getFoldersHeading(folderCodeToFind, folderLabelCustomName, localization);
	}

	public static String getCurrentFoldersHeading(ConfigInfo configInfo, MartusLocalization localization)
	{
		String code = configInfo.getFolderLabelCode();
		String custom = configInfo.getFolderLabelCustomName();
		String foldersLabel = getFoldersHeading(code, custom, localization);
		return foldersLabel;
	}
	
	public static String getFoldersHeading(String folderCodeToFind, String folderLabelCustomName, MartusLocalization localization)
	{
		if(folderCodeToFind.equals(FOLDER_CODE_CUSTOM))
			return folderLabelCustomName;

		ObservableChoiceItemList folderChoices = getFolderLabelChoices(localization);
		ChoiceItem found = folderChoices.findByCode(folderCodeToFind);
		if(found == null)
			found = folderChoices.findByCode(FOLDER_CODE_DEFAULT);
		return found.getLabel();
	}
	
	@Override
	protected Dimension getPreferredDimension()
	{
		return FxInSwingModalDialog.MEDIUM_SMALL_PREFERRED_DIALOG_SIZE;
	}

	@Override
	public String getFxmlLocation()
	{
		return LOCATION_FOLDER_SETTINGS_FXML;
	}
	
	@FXML
	ChoiceBox<ChoiceItem> fxFolderChoiceBox;
	
	@FXML
	TextField fxFolderCustomTextField;
	
	public static final String FOLDER_CODE_CASES = "cases"; 
	public static final String FOLDER_CODE_INCIDENTS = "incidents"; 
	public static final String FOLDER_CODE_PROJECTS = "projects"; 
	public static final String FOLDER_CODE_CUSTOM = "custom"; 
	public static final String FOLDER_CODE_DEFAULT = FOLDER_CODE_CASES;
	
	private static final String LOCATION_FOLDER_SETTINGS_FXML = "landing/cases/FolderSettings.fxml";
	private ConfigInfo config;
	private ChangeListener folderLabelIndexListener;
	private ChangeListener folderCustomLabelListener;
}
