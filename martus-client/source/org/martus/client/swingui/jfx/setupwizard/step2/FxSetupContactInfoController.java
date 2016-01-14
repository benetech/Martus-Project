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
package org.martus.client.swingui.jfx.setupwizard.step2;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import org.martus.client.core.ConfigInfo;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;

public class FxSetupContactInfoController extends FxStep2Controller
{
	public FxSetupContactInfoController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		
		info = getApp().getConfigInfo();
		fontHelper = new UiFontEncodingHelper(getConfigInfo().getDoZawgyiConversion());
	}

	public void initializeMainContentPane()
	{
		// NOTE: Kind of an odd place for this, but it has to be 
		// after we have signed in, and this is the earliest 
		// point in the wizard where we know that is true.
		getMainWindow().doPostSigninAppInitialization();
		
		getWizardNavigationHandler().getBackButton().setDisable(true);

		authorField.setText(getConfigInfo().getAuthor());
		organizationField.setText(getConfigInfo().getOrganization());
		authorField.requestFocus();
	}

	@Override
	public void nextWasPressed() throws Exception 
	{
		getConfigInfo().setAuthor(getFontHelper().getStorable(authorField.getText()));
		getConfigInfo().setOrganization(getFontHelper().getStorable(organizationField.getText()));
		getApp().saveConfigInfo();
		super.nextWasPressed();
	}

	private UiFontEncodingHelper getFontHelper()
	{
		return fontHelper;
	}

	private ConfigInfo getConfigInfo()
	{
		return info;
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step2/SetupContactInfo.fxml";
	}
	
	@Override
	public AbstractFxSetupWizardContentController getNextController()
	{
		return new FxSetupSettingsController(getMainWindow());
	}

	@FXML
	private TextField authorField;
	
	@FXML
	private TextField organizationField;
	
	private ConfigInfo info;
	private UiFontEncodingHelper fontHelper;
}
