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
package org.martus.mspa.client.view;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.martus.clientside.UiLocalization;
import org.martus.common.VersionBuildDate;
import org.martus.mspa.main.UiMainWindow;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiVBox;
import org.martus.swing.Utilities;

public class AboutDlg extends JDialog implements ActionListener
{
	public AboutDlg(UiMainWindow owner) throws HeadlessException
	{
		super(owner, "" , true);
		UiLocalization localization = owner.getLocalization();
		
		setTitle(localization.getWindowTitle("about"));
		
		JPanel panel = new JPanel();		
		panel.setBorder(new EmptyBorder(8,8,8,8));

		String versionInfo = "<html><b><font size='+1'>" + UiConstants.programName + "</font></b><br>";
		versionInfo += localization.getFieldLabel("aboutDlgVersionInfo");
		versionInfo += " " + UiConstants.versionLabel;

		String buildDate = localization.getFieldLabel("aboutDlgBuildDate");
		buildDate += " " + VersionBuildDate.getVersionBuildDate();

		JButton ok = new UiButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);			
				
		UiVBox vBoxVersionInfo = new UiVBox();
		vBoxVersionInfo.addCentered(new UiLabel(versionInfo));
		vBoxVersionInfo.addSpace();
		vBoxVersionInfo.addSpace();
		vBoxVersionInfo.addCentered(new UiLabel(UiConstants.copyright));
		vBoxVersionInfo.addCentered(new UiLabel(UiConstants.website));
		vBoxVersionInfo.addSpace();
		vBoxVersionInfo.addCentered(new UiLabel(buildDate, JLabel.CENTER));
		vBoxVersionInfo.addSpace();

		Box hBoxVersionAndIcon = Box.createHorizontalBox();
		hBoxVersionAndIcon.add(Box.createHorizontalGlue());
		hBoxVersionAndIcon.add(vBoxVersionInfo);
		hBoxVersionAndIcon.add(Box.createHorizontalGlue());
		
		Box hBoxOk = Box.createHorizontalBox();
		hBoxOk.add(Box.createHorizontalGlue());	
		hBoxOk.add(ok);
		hBoxOk.add(Box.createHorizontalGlue());							
		
		UiVBox vBoxAboutDialog = new UiVBox();
		vBoxAboutDialog.addCentered(hBoxVersionAndIcon);
		vBoxAboutDialog.addCentered(hBoxOk);		
		
		panel.add(vBoxAboutDialog);
		getContentPane().add(panel);
		Utilities.centerDlg(this);
		setVisible(true);
	}	

	public void actionPerformed(ActionEvent ae)
	{
		dispose();
	}

}
