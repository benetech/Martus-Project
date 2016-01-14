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

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionMenuChangeUserNamePassword;
import org.martus.client.swingui.actions.ActionMenuExportMyPublicKey;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusAccountAccessToken.TokenInvalidException;
import org.martus.common.crypto.MartusCrypto;

public class AccountInformationController extends FxController
{
	public AccountInformationController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);

		setAccountPublicCodeLabel();
		setAccessTokenLabel();
	}

	private void setAccessTokenLabel()
	{
		try
		{
			String martusAccountAccessToken = getAccountAccessToken();
			accountAccessTokenLabel.setText(martusAccountAccessToken);
			accountAccessTokenLabel.setEditable(false);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private String getAccountAccessToken() throws TokenInvalidException
	{
		if (getApp().getConfigInfo().hasMartusAccountAccessToken())
		{
			MartusAccountAccessToken accountToken = getApp().getConfigInfo().getCurrentMartusAccountAccessToken();
			
			return accountToken.getToken();
		}
		
		return getLocalization().getFieldLabel("TokenNotAvailable");
	}

	private void setAccountPublicCodeLabel() 
	{
		try
		{
			String keyContents = getApp().getAccountId();
			String formattedCodeContentsNew = MartusCrypto.computeFormattedPublicCode40(keyContents);
			accountPublicCode.setText(formattedCodeContentsNew);
			accountPublicCode.setEditable(false);
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	@FXML
	private void onExportPublicKey(ActionEvent event)
	{
		doAction(new ActionMenuExportMyPublicKey(getMainWindow()));
	}

	@FXML
	public void onChangeUsernameAndPassword(ActionEvent event)
	{
		doAction(new ActionMenuChangeUserNamePassword(getMainWindow()));
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/general/AccountInformation.fxml";
	}
	
	@FXML
	private TextField accountAccessTokenLabel;
	
	@FXML
	private TextField accountPublicCode;
}
