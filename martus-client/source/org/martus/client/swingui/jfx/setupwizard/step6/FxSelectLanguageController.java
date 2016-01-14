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
package org.martus.client.swingui.jfx.setupwizard.step6;

import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.ChoiceItem;

public class FxSelectLanguageController extends FxStep6Controller
{
	public FxSelectLanguageController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void nextWasPressed() throws Exception
	{
		String selectedLanguageCode = languagesDropdown.getSelectionModel().getSelectedItem().getCode();
		
		if (MtfAwareLocalization.isRecognizedLanguage(selectedLanguageCode))
			getLocalization().setCurrentLanguageCode(selectedLanguageCode);
		super.nextWasPressed();
	}
	
	public void initializeMainContentPane()
	{
		ObservableList<ChoiceItem> availableLanguages = FXCollections.observableArrayList(getAvailableLanguages(getLocalization()));
		languagesDropdown.setItems(availableLanguages);
		ChoiceItem currentLanguageChoiceItem = findCurrentLanguageChoiceItem(getLocalization());
		languagesDropdown.getSelectionModel().select(currentLanguageChoiceItem);
		
		getWizardNavigationHandler().getBackButton().setVisible(false);
	}
	
	static public ChoiceItem findCurrentLanguageChoiceItem(MartusLocalization localization)
	{
		String currentLanguageCode = localization.getCurrentLanguageCode();

		ObservableList<ChoiceItem> availableLanguages = getAvailableLanguages(localization);
		for (ChoiceItem choiceItem : availableLanguages)
		{
			if (choiceItem.getCode().equals(currentLanguageCode))
				return choiceItem;
		}
		
		return null;
	}

	static public ObservableList<ChoiceItem> getAvailableLanguages(MartusLocalization localization)
	{
		String currentLanguageCode = localization.getCurrentLanguageCode();
		ChoiceItem[] allUILanguagesSupported = localization.getUiLanguages();
		Vector<ChoiceItem> languageChoices = new Vector<ChoiceItem>();
		for(int i = 0; i < allUILanguagesSupported.length; ++i)
		{
			String thisCode = allUILanguagesSupported[i].getCode();
			String thisLanguageName = "";
			try
			{
				localization.setCurrentLanguageCode(thisCode);
				thisLanguageName = localization.getLanguageName(thisCode);
			}
			catch(Exception e)
			{
				MartusLogger.log("Error loading language " + thisCode);
				throw(e);
			}
			finally
			{
				localization.setCurrentLanguageCode(currentLanguageCode);
			}
			String languageNameInCurrentLanguage = localization.getLanguageName(thisCode);

			String completeLanguageNativeAndInCurrentLanguage;
			if(thisCode.equals(currentLanguageCode) || thisCode.equals(MiniLocalization.BURMESE))
				completeLanguageNativeAndInCurrentLanguage = languageNameInCurrentLanguage;
			else
				completeLanguageNativeAndInCurrentLanguage = String.format("%s / %s", thisLanguageName, languageNameInCurrentLanguage);
			languageChoices.add(new ChoiceItem(thisCode, completeLanguageNativeAndInCurrentLanguage));
		}
		return FXCollections.observableArrayList(languageChoices);
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step6/SetupLanguage.fxml";
	}
	
	@Override
	public AbstractFxSetupWizardContentController getNextController()
	{
		return null;
	}
	
	@FXML 
	private ChoiceBox<ChoiceItem> languagesDropdown; 
	
}
