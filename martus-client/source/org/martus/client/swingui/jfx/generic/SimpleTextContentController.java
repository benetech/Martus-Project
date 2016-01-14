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

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import org.martus.client.swingui.UiMainWindow;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class SimpleTextContentController extends FxController
{
	public SimpleTextContentController(UiMainWindow mainWindowToUse, String confirmationMessageTagToUse)
	{
		this(mainWindowToUse, confirmationMessageTagToUse, null);
	}
	
	public SimpleTextContentController(UiMainWindow mainWindowToUse, String confirmationMessageTagToUse, Map tokensToUse)
	{
		super(mainWindowToUse);
		confirmationMessageTag = confirmationMessageTagToUse;
		tokenReplacement = tokensToUse;
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);

		messageTextArea.setWrapText(true);
		String rawMessage = getLocalization().getFieldLabel(confirmationMessageTag);
		String contents = rawMessage;
		try
		{
			if(tokenReplacement != null)
				contents = TokenReplacement.replaceTokens(rawMessage, tokenReplacement);
		} 
		catch (TokenInvalidException e)
		{
			e.printStackTrace();
		}
		messageTextArea.setText(contents);
	}

	@Override
	public String getFxmlLocation()
	{
		return "generic/Confirmation.fxml";
	}
	
	@FXML
	private Label messageTextArea;
	
	private String confirmationMessageTag;
	private Map tokenReplacement;
}
