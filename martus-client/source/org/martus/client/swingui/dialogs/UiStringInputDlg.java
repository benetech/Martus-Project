/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

package org.martus.client.swingui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.swing.UiButton;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiTextField;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;

public class UiStringInputDlg extends JDialog
{
	public UiStringInputDlg(UiMainWindow owner, String baseTag, String descriptionTag, String rawDescriptionText, String defaultText)
	{
		super(owner.getSwingFrame(), "", true);
		setIconImage(Utilities.getMartusIconImage());

		fontHelper = new UiFontEncodingHelper(owner.getDoZawgyiConversion());
		UiLocalization localization = owner.getLocalization();
		setTitle(localization.getWindowTitle("input" + baseTag));
		UiWrappedTextArea label = new UiWrappedTextArea(localization.getFieldLabel("input" + baseTag + "entry"));
		text = new UiTextField(30);
		fontHelper.setDisplayableText(text, defaultText);

		JButton ok = new UiButton(localization.getButtonLabel("input" + baseTag + "ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(new CancelHandler());

		UiParagraphPanel stringPanel = new UiParagraphPanel();
		if(descriptionTag.length() > 0)
			stringPanel.addOnNewLine(new UiWrappedTextArea(localization.getFieldLabel(descriptionTag)));
		if(rawDescriptionText.length() > 0)
			stringPanel.addOnNewLine(new UiWrappedTextArea(rawDescriptionText));
		stringPanel.addOnNewLine(label);
		stringPanel.addOnNewLine(text);
		stringPanel.addComponents(ok, cancel);
		
		getContentPane().add(stringPanel);
		getRootPane().setDefaultButton(ok);

		Utilities.packAndCenterWindow(this);
		setResizable(true);
	}
	
	public void setFocusToInputField()
	{
		text.requestFocus();
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			result = fontHelper.getStorable(text.getText());
			dispose();
		}
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}

	public String getResult()
	{
		return result;
	}


	UiTextField text;
	String result = null;
	UiFontEncodingHelper fontHelper;
}

