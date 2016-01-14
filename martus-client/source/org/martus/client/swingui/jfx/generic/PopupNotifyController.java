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
package org.martus.client.swingui.jfx.generic;

import java.util.Map;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MartusLogger;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class PopupNotifyController extends FxPopupController implements Initializable
{
	public PopupNotifyController(UiMainWindow mainWindowToUse, String notificationTag)
	{
		super(mainWindowToUse);
		baseTag = notificationTag;
		extraMessage = "";
		tokenReplacement = null;
	}
	
	@Override
	public void initialize()
	{
		MartusLocalization localization = getLocalization();
		fxOkButton.setText(localization.getButtonLabel(EnglishCommonStrings.OK));
		String fieldLabelRaw = localization.getFieldLabel("notify"+baseTag+"cause");
		String fieldLabel = fieldLabelRaw;
		if(tokenReplacement != null)
		{
			try
			{
				fieldLabel = TokenReplacement.replaceTokens(fieldLabelRaw, tokenReplacement);
			} 
			catch (TokenInvalidException e)
			{
				MartusLogger.logException(e);
				throw new RuntimeException(e);
			}
		}
		
		String fullMessage = fieldLabel;
		if(extraMessage.length()>0)
		{
			fullMessage += " ";
			fullMessage += extraMessage;
		}
		fxLabel.setText(fullMessage);
		
	}
	
	public void setExtraMessage(String extraMessageToUse)
	{
		extraMessage = extraMessageToUse;
	}
	
	public void setTokenReplacement(Map tokenReplacementMapToUse)
	{
		tokenReplacement = tokenReplacementMapToUse;
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/NotifyPopup.fxml";
	}

	@Override
	public String getDialogTitle()
	{
		return getLocalization().getWindowTitle("notify"+ baseTag); 
	}

	@FXML
	public void okPressed()
	{
		getStage().close();
	}

	@FXML
	private Label fxLabel;

	@FXML
	private Button fxOkButton;

	private String baseTag;
	private String extraMessage;
	private Map tokenReplacement;
}