/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;

import org.martus.client.swingui.UiMainWindow;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.swing.UiButton;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class UiIncludePrivateDataDlg extends JDialog implements ActionListener
{
	public UiIncludePrivateDataDlg(UiMainWindow mainWindowToUse, int totalBulletins, int privateOnlyBulletins)
	{
		super(mainWindowToUse.getSwingFrame(), "", true);
		mainWindow = mainWindowToUse;
		init(totalBulletins, privateOnlyBulletins);	
	}
	
	private void init(int totalBulletins, int privateOnlyBulletins)
	{
		UiLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle("IncludePrivateData"));
		
		publicAndPrivate = new UiButton(localization.getButtonLabel("PublicAndPrivateData"));
		publicAndPrivate.addActionListener(this);		
		publicOnly = new UiButton(localization.getButtonLabel("PublicOnly"));
		publicOnly.addActionListener(this);		
		cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(this);	
		
		HashMap tokenReplacement = new HashMap();
		tokenReplacement.put("#TotalBulletins#", Integer.toString(totalBulletins));
		tokenReplacement.put("#AllPrivateBulletins#", Integer.toString(privateOnlyBulletins));
		
		String message = localization.getFieldLabel("IncludePrivateData");
		try
		{
			message = TokenReplacement.replaceTokens(message, tokenReplacement);
		}
		catch(TokenInvalidException e)
		{
			e.printStackTrace();
		}		
		
		Box buttons = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(buttons, new Component[] {publicOnly,Box.createHorizontalGlue(), publicAndPrivate,Box.createHorizontalGlue(), cancel});

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new UiWrappedTextArea(message, 40), BorderLayout.CENTER);
		getContentPane().add(buttons, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(publicAndPrivate);
		Utilities.packAndCenterWindow(this);
		setResizable(true);
	}
	
	public boolean wantsPrivateData()
	{
		return includePrivateData;
	}
	
	public boolean wasCancelButtonPressed()
	{
		return pressedCancel;
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource().equals(publicAndPrivate))
		{
			includePrivateData = true;
			pressedCancel = false;
		}
		if(ae.getSource().equals(publicOnly))
		{
			includePrivateData = false;
			pressedCancel = false;
		}
		dispose();
	}
	
	UiMainWindow mainWindow;	
	JButton publicAndPrivate;
	JButton publicOnly;
	JButton cancel;

	boolean includePrivateData = false;
	boolean pressedCancel = true;
}
