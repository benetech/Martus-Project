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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import org.martus.client.core.ConfigInfo;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;

public class AuthorInformationController extends FxController
{
	public AuthorInformationController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		
		info = getApp().getConfigInfo();
		fontHelper = new UiFontEncodingHelper(getConfigInfo().getDoZawgyiConversion());
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		
		authorField.setText(getConfigInfo().getAuthor());
		organizationField.setText(getConfigInfo().getOrganization());
		authorField.requestFocus();
	}
	
	private ConfigInfo getConfigInfo()
	{
		return info;
	}
	
	@FXML
	public void onSaveChanges(ActionEvent event) throws Exception
	{
		getConfigInfo().setAuthor(getFontHelper().getStorable(authorField.getText()));
		getConfigInfo().setOrganization(getFontHelper().getStorable(organizationField.getText()));
		getApp().saveConfigInfo();
	}
	
	private UiFontEncodingHelper getFontHelper()
	{
		return fontHelper;
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/general/AuthorInformation.fxml";
	}
	
	@FXML
	private TextField authorField;
	
	@FXML
	private TextField organizationField;
	
	private ConfigInfo info;
	private UiFontEncodingHelper fontHelper;
}
