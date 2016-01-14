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

import java.awt.Dimension;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.FxInSwingModalDialog;
import org.martus.common.VersionBuildDate;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;

public class HelpController extends FxController
{
	public HelpController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		addVersionInfo();
	}

	private void addVersionInfo()
	{
		MartusLocalization localization = getMainWindow().getLocalization();
		StringBuffer versionInfo = new StringBuffer(UiConstants.programName);
		versionInfo.append(" ");
		versionInfo.append(localization.getFieldLabel("aboutDlgVersionInfo"));
		versionInfo.append(" ");
		versionInfo.append(UiConstants.versionLabel);
		
		StringBuffer mlpDateInfo = new StringBuffer();
		if(localization.isTranslationInsideMLP())
		{
			mlpDateInfo.append(localization.getFieldLabel("aboutDlgMlpDateInfo"));
			mlpDateInfo.append(" ");
			MultiCalendar mlpDate = new MultiCalendar();
			mlpDate.setTime(localization.getMlpDate());
			String storedDateString = MartusFlexidate.toStoredDateFormat(mlpDate);
			mlpDateInfo.append(localization.convertStoredDateToDisplay(storedDateString));
		}
		
		StringBuffer mtfVersionInfo = new StringBuffer(localization.getFieldLabel("aboutDlgTranslationVersionInfo"));
		mtfVersionInfo.append(" ");
		mtfVersionInfo.append(localization.getTranslationFullVersionInfo());
		if(!localization.isCurrentTranslationOfficial())
			mtfVersionInfo.append("X");

		StringBuffer buildDate = new StringBuffer(localization.getFieldLabel("aboutDlgBuildDate"));
		buildDate.append(" ");
		buildDate.append(VersionBuildDate.getVersionBuildDate());

		versionInfoBox.getChildren().add(new Label(""));
		versionInfoBox.getChildren().add(new Label(versionInfo.toString()));
		if(mlpDateInfo.length() > 0)
			versionInfoBox.getChildren().add(new Label(mlpDateInfo.toString()));
		versionInfoBox.getChildren().add(new Label(mtfVersionInfo.toString()));
		versionInfoBox.getChildren().add(new Label(buildDate.toString()));
		versionInfoBox.getChildren().add(new Label(""));
		versionInfoBox.getChildren().add(new Label(UiConstants.copyright));
		
		versionInfoBox.getChildren().add(new Label(""));
		
		final String disclaimer = localization.getFieldLabel("aboutDlgDisclaimer");
		final String notice = "\n" + disclaimer + "\n\n" + localization.getFieldLabel("aboutDlgThirdParty");
	
		TextArea noticeArea = new TextArea(notice);
		noticeArea.setWrapText(true);
		noticeArea.setPrefHeight(150);
		noticeArea.setEditable(false);
		versionInfoBox.getChildren().add(noticeArea);		
	}
	
	public final class OnBenetechClicked implements EventHandler<ActionEvent>
	{
		@Override
		public void handle(ActionEvent e) 
		{
			openLink(UiConstants.websiteURL);
		}
	}
	
	@Override
	protected Dimension getPreferredDimension()
	{
		return FxInSwingModalDialog.MEDIUM_PREFERRED_DIALOG_SIZE;
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/general/Help.fxml";
	}
	
	@FXML
	private void onMartusLinkClick(ActionEvent event)
	{
		openLink(UiConstants.martusWebsiteURL);
	}
	
	protected void openLink(String link)
	{
		openLinkInDefaultBrowser(link);
	}
	
	
	@FXML
	private void onLinkEMailTo(ActionEvent event)
	{
		openDefaultEmailApp("mailto:martus@benetech.org");
	}
	
	@FXML 
	private VBox versionInfoBox;
}
