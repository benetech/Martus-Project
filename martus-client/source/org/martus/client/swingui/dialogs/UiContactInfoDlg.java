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
import javax.swing.JLabel;

import org.martus.client.core.ConfigInfo;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTextArea;
import org.martus.swing.UiTextField;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;

public class UiContactInfoDlg extends JDialog implements ActionListener
{
	public UiContactInfoDlg(UiMainWindow mainWindow, ConfigInfo infoToUse)
	{
		super(mainWindow.getCurrentActiveFrame().getSwingFrame(), "", true);
		info = infoToUse;
		fontHelper = new UiFontEncodingHelper(info.getDoZawgyiConversion());

		UiLocalization localization = mainWindow.getLocalization();
	
		setTitle(localization.getWindowTitle("setupcontact"));
		ok = new UiButton(localization.getButtonLabel(EnglishCommonStrings.OK));
		ok.addActionListener(this);
		JButton cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(this);

		source = new UiTextField(50);
		organization = new UiTextField(50);
		email = new UiTextField(50);
		webpage = new UiTextField(50);
		phone = new UiTextField(50);
		address = new UiTextArea(5, 50);
		address.setLineWrap(true);
		address.setWrapStyleWord(true);
		UiScrollPane addressScrollPane = new UiScrollPane(address, UiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				UiScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		setInitialValues();

		UiParagraphPanel panel = new UiParagraphPanel();

		UiWrappedTextArea infoRequired = new UiWrappedTextArea(localization.getFieldLabel("ContactInfoRequiredFields"), 60);
		JLabel space = new UiLabel(" ");
		infoRequired.setFont(space.getFont());
		infoRequired.setRows(2);
		panel.addOnNewLine(infoRequired);

		String authorPrompt = localization.getFieldLabel("Author");
		panel.addComponents(new UiLabel(authorPrompt), source);

		String organizationPrompt = localization.getFieldLabel("Organization");
		panel.addComponents(new UiLabel(organizationPrompt), organization);
		panel.addComponents(new UiLabel(localization.getFieldLabel("email")), email);
		panel.addComponents(new UiLabel(localization.getFieldLabel("webpage")), webpage);
		panel.addComponents(new UiLabel(localization.getFieldLabel("phone")), phone);
		panel.addComponents(new UiLabel(localization.getFieldLabel("address")), addressScrollPane);
		
		StringBuffer helpmsg = new StringBuffer();
		helpmsg.append(localization.getFieldLabel("ContactInfoFutureUseOfFields"));
		UiWrappedTextArea infoFuture = new UiWrappedTextArea(helpmsg.toString(), 30);
		panel.addOnNewLine(infoFuture);

		panel.addComponents(ok, cancel);
		UiScrollPane scroller = new UiScrollPane(panel);
		getContentPane().add(scroller);
		getRootPane().setDefaultButton(ok);
		Utilities.packAndCenterWindow(this);
	}

	private void setInitialValues()
	{
		fontHelper.setDisplayableText(source, info.getAuthor());
		fontHelper.setDisplayableText(organization, info.getOrganization());
		fontHelper.setDisplayableText(email, info.getEmail());
		fontHelper.setDisplayableText(webpage, info.getWebPage());
		fontHelper.setDisplayableText(phone, info.getPhone());
		fontHelper.setDisplayableText(address, info.getAddress());
	}

	public boolean getResult()
	{
		return result;
	}

	public void actionPerformed(ActionEvent ae)
	{
		result = false;
		if(ae.getSource() == ok)
		{
			info.setAuthor(fontHelper.getStorable(source.getText()));
			info.setOrganization(fontHelper.getStorable(organization.getText()));
			info.setEmail(fontHelper.getStorable(email.getText()));
			info.setWebPage(fontHelper.getStorable(webpage.getText()));
			info.setPhone(fontHelper.getStorable(phone.getText()));
			info.setAddress(fontHelper.getStorable(address.getText()));
			result = true;
		}
		dispose();
	}


	ConfigInfo info;
	boolean result;

	UiTextField source;
	UiTextField organization;
	UiTextField email;
	UiTextField webpage;
	UiTextField phone;
	UiTextArea address;
	UiFontEncodingHelper fontHelper;

	JButton ok;
}
