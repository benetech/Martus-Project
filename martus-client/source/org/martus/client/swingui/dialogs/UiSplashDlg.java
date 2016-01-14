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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MiniLocalization;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.Utilities;

public class UiSplashDlg extends JDialog implements ActionListener
{
	public UiSplashDlg(MiniLocalization localization, String text)
	{
		// NOTE: Pass (Dialog)null to force this window to show up in the Task Bar
		super((Dialog)null);
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		String versionInfo = UiMainWindow.getDisplayVersionInfo(localization);
		String copyrightInfo = UiConstants.copyright;
		String websiteInfo = UiConstants.website;
		String fullVersionInfo = "<html>" +
				"<p align='center'>" + text + "</p>" + 
				"<p align='center'></p>" + 
				"<p align='center'>" + versionInfo + "</p>" + 
				"<p align='center'>" + copyrightInfo + "</p>" + 
				"<p align='center'>" + websiteInfo + "</p>" +
				"</html>";
		
		JLabel body = new UiLabel(fullVersionInfo);
		body.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		Box hbox = Box.createHorizontalBox();
		hbox.add(Box.createHorizontalGlue());
		JButton ok = new UiButton(localization.getButtonLabel(EnglishCommonStrings.OK));
		ok.addActionListener(this);
		hbox.add(ok);
		hbox.add(Box.createHorizontalGlue());
		hbox.setBorder(new EmptyBorder(10, 10, 10, 10));
		getRootPane().setDefaultButton(ok);

		Container contents = getContentPane();
		contents.add(body);
		contents.add(hbox, BorderLayout.SOUTH);
		
		Utilities.packAndCenterWindow(this);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent ae)
	{
		dispose();
	}


}