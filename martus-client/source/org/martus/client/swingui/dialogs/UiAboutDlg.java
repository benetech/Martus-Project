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
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.martus.client.swingui.UiConstants;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.VersionBuildDate;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiVBox;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.MultiCalendar;

public class UiAboutDlg extends JDialog implements ActionListener
{
	public UiAboutDlg(JFrame owner, UiLocalization localization)
		throws HeadlessException
	{
		super(owner, "" , true);
		getContentPane().setLayout(new BorderLayout());
//		System.out.println("Number of calls to verifyPacketSignature " + Packet.callsToVerifyPacketSignature);
//		System.out.println("Cumulative time in verifyPacketSignature " + Packet.millisInVerifyPacketSignature);
//		System.out.println("Number of calls to XmlPacketLoader " + XmlPacketLoader.callsToXmlPacketLoader);
//		System.out.println("Cumulative time in XmlPacketLoader " + XmlPacketLoader.millisInXmlPacketLoader);

		setTitle(localization.getWindowTitle("about"));

		JLabel icon = new JLabel(new ImageIcon(UiAboutDlg.class.getResource("Martus-logo-black-text-160x72.png")),JLabel.LEFT);

		StringBuffer versionInfo = new StringBuffer(UiConstants.programName);
		versionInfo.append(" ");
		versionInfo.append(localization.getFieldLabel("aboutDlgVersionInfo"));
		versionInfo.append(" ");
		versionInfo.append(UiConstants.versionLabel);
		
		StringBuffer mlpDateInfo = new StringBuffer();
		if(localization.isTranslationInsideMLP())
		{
			mlpDateInfo.append(localization.getFieldLabel("aboutDlgMlpDateInfo"));
			mlpDateInfo.append(" ");
			MultiCalendar mlpDate = new MultiCalendar();
			mlpDate.setTime(localization.getMlpDate());
			String storedDateString = MartusFlexidate.toStoredDateFormat(mlpDate);
			mlpDateInfo.append(localization.convertStoredDateToDisplay(storedDateString));
		}
		
		StringBuffer mtfVersionInfo = new StringBuffer(localization.getFieldLabel("aboutDlgTranslationVersionInfo"));
		mtfVersionInfo.append(" ");
		mtfVersionInfo.append(localization.getTranslationFullVersionInfo());
		if(!localization.isCurrentTranslationOfficial())
			mtfVersionInfo.append("X");

		StringBuffer buildDate = new StringBuffer(localization.getFieldLabel("aboutDlgBuildDate"));
		buildDate.append(" ");
		buildDate.append(VersionBuildDate.getVersionBuildDate());

		JButton ok = new UiButton(localization.getButtonLabel(EnglishCommonStrings.OK));
		ok.addActionListener(this);
		ok.addKeyListener(new MakeEnterKeyExit());

		Box vBoxVersionInfo = new UiVBox();
		vBoxVersionInfo.add(new UiLabel(versionInfo.toString()));
		if(mlpDateInfo.length() > 0)
			vBoxVersionInfo.add(new UiLabel(mlpDateInfo.toString()));
		vBoxVersionInfo.add(new UiLabel(mtfVersionInfo.toString()));
		vBoxVersionInfo.add(new UiLabel(UiConstants.copyright));
		vBoxVersionInfo.add(new UiLabel(UiConstants.website));
		vBoxVersionInfo.add(new UiLabel(buildDate.toString()));

		Box hBoxVersionAndIcon = Box.createHorizontalBox();
		hBoxVersionAndIcon.add(Box.createHorizontalGlue());
		hBoxVersionAndIcon.add(vBoxVersionInfo);
		hBoxVersionAndIcon.add(Box.createHorizontalGlue());
		hBoxVersionAndIcon.add(icon);

		Box hBoxOk = Box.createHorizontalBox();
		hBoxOk.add(Box.createHorizontalGlue());
		hBoxOk.add(ok);
		hBoxOk.add(Box.createHorizontalGlue());

		final String disclaimer = localization.getFieldLabel("aboutDlgDisclaimer");
		final String credits = localization.getFieldLabel("aboutDlgCredits");
		final String notice = "\n" + disclaimer + "\n\n" + credits + "\n\n" + localization.getFieldLabel("aboutDlgThirdParty");

		getContentPane().add(hBoxVersionAndIcon, BorderLayout.NORTH);
		getContentPane().add(new UiWrappedTextArea(notice), BorderLayout.CENTER);
		getContentPane().add(hBoxOk, BorderLayout.SOUTH);
		
		Utilities.packAndCenterWindow(this);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent ae)
	{
		dispose();
	}

	public class MakeEnterKeyExit extends KeyAdapter
	{
		public void keyPressed(KeyEvent ke)
		{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
				dispose();
		}
	}

}
