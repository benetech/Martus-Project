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

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.step3.FxSetupStorageServerController;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiTextField;
import org.martus.swing.Utilities;

public class UiConfigServerDlg extends JDialog implements ActionListener
{
	public UiConfigServerDlg(UiMainWindow owner, ConfigInfo infoToUse)
	{
		super(owner.getSwingFrame(), "", true);

		info = infoToUse;
		mainWindow = owner;
		app = owner.getApp();
		UiLocalization localization = mainWindow.getLocalization();
		
		setTitle(localization.getWindowTitle("ConfigServer"));
		fieldIPAddress = new UiTextField(25);
		fieldPublicCode = new UiTextField(40);

		UiParagraphPanel panel = new UiParagraphPanel();
		
		UiLabel defaultServerHeading = new UiLabel(localization.getFieldLabel("DefaultServerHeading"));
		defaultServerHeading.makeHeading();
		
		UiButton defaultButton = new UiButton(new ActionDefaultServer());
		panel.addLabelOnly(defaultServerHeading);
		panel.addLabelOnly(defaultButton);
		
		
		panel.addBlankLine();
		UiLabel advanceServerSetupHeading = new UiLabel(localization.getFieldLabel("AdvanceServerSetupHeading"));
		advanceServerSetupHeading.makeHeading();
		advanceServerSetupHeading.makeBold();
		panel.addLabelOnly(advanceServerSetupHeading);
		panel.addComponents(new UiLabel(localization.getFieldLabel("ServerNameEntry")), fieldIPAddress);
		panel.addComponents(new UiLabel(localization.getFieldLabel("ServerPublicCodeEntry")), fieldPublicCode);
		serverIPAddress = info.getServerName();
		serverPublicKey = info.getServerPublicKey();

		updateTextFields();

		fieldIPAddress.requestFocus();

		panel.addBlankLine();

		ok = new UiButton(localization.getButtonLabel(EnglishCommonStrings.OK));
		ok.addActionListener(this);
		JButton cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(this);
		panel.addComponents(ok, cancel);

		getContentPane().add(panel);
		getRootPane().setDefaultButton(ok);
		Utilities.packAndCenterWindow(this);
		setResizable(true);
		setVisible(true);
	}

	public void updateTextFields()
	{
		fieldIPAddress.setText(serverIPAddress);

		String knownServerPublicCode = "";
		try
		{
			if(serverPublicKey.length() > 0)
				knownServerPublicCode = MartusCrypto.computeFormattedPublicCode40(serverPublicKey);
		}
		catch (Exception e)
		{
		}
		fieldPublicCode.setText(knownServerPublicCode);
	}

	public boolean getResult()
	{
		return result;
	}

	public String getServerIPAddress()
	{
		return serverIPAddress;
	}

	public String getServerPublicKey()
	{
		return serverPublicKey;
	}

	public void actionPerformed(ActionEvent ae)
	{
		result = false;
		if(ae.getSource() == ok)
			onOk();
		else
			dispose();
	}
	
	protected void onOk()
	{
		String name = fieldIPAddress.getText();
		String publicCode = fieldPublicCode.getText();
		if(!ValidateInformation(name, publicCode))
			return;
		
		result = true;
		dispose();
	}

	private boolean ValidateInformation(String serverName, String userEnteredPublicCode)
	{
		if(serverName.length() == 0)
			return errorMessage("InvalidServerName");

		String normalizedPublicCode = MartusCrypto.removeNonDigits(userEnteredPublicCode);
		if(normalizedPublicCode.length() == 0)
			return errorMessage("InvalidServerCode");

		String serverKey = null;
		try
		{
			if(!app.isNonSSLServerAvailable(serverName))
				return errorMessage("ConfigNoServer");

			serverKey = app.getServerPublicKey(serverName);
			String serverPublicCode = MartusCrypto.computePublicCode(serverKey);
			String serverPublicCode40 = MartusCrypto.computePublicCode40(serverKey);
			if(!(serverPublicCode.equals(normalizedPublicCode)||serverPublicCode40.equals(normalizedPublicCode)))
				return errorMessage("ServerCodeWrong");
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			return errorMessage("ServerInfoInvalid");
		}

		serverIPAddress = serverName;
		serverPublicKey = serverKey;
		return true;
	}

	private boolean errorMessage(String messageTag)
	{
		mainWindow.notifyDlg(messageTag);
		return false;
	}
	
	protected class ActionDefaultServer extends AbstractAction
	{
		public ActionDefaultServer()
		{
			super(mainWindow.getLocalization().getButtonLabel("ChooseDefaultServer"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			serverIPAddress = FxSetupStorageServerController.IP_FOR_SL1_IE;
			serverPublicKey = FxSetupStorageServerController.PUBLIC_KEY_FOR_SL1_IE;
			updateTextFields();
			onOk();
		}
		
	}


	MartusApp app;
	UiMainWindow mainWindow;
	ConfigInfo info;

	JButton ok;
	UiTextField fieldIPAddress;
	UiTextField fieldPublicCode;

	String serverIPAddress;
	String serverPublicKey;

	boolean result;
}
