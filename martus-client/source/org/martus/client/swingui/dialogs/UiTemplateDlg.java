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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.filechooser.FileFilter;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiNotifyDlg;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTextArea;
import org.martus.swing.Utilities;
import org.martus.util.UnicodeReader;



public class UiTemplateDlg extends JDialog implements ActionListener
{
	public UiTemplateDlg(UiMainWindow owner, ConfigInfo infoToUse, File defaultDetailsFileToUse)
	{
		super(owner.getSwingFrame(), "", true);
		info = infoToUse;
		mainWindow = owner;
		defaultDetailsFile = defaultDetailsFileToUse;

		fontHelper = new UiFontEncodingHelper(info.getDoZawgyiConversion());
		UiLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle("BulletinTemplate"));
		okButton = new UiButton(localization.getButtonLabel(EnglishCommonStrings.OK));
		okButton.addActionListener(this);
		JButton cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(this);
		JButton help = new UiButton(localization.getButtonLabel("help"));
		help.addActionListener(new helpHandler());
		JButton loadFromFile = new UiButton(localization.getButtonLabel("ResetContents"));
		loadFromFile.addActionListener(new loadFileHandler());
		details = new UiTextArea(15, 65);
		details.setLineWrap(true);
		details.setWrapStyleWord(true);
		UiScrollPane detailScrollPane = new UiScrollPane(details, UiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				UiScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		fontHelper.setDisplayableText(details, info.getTemplateDetails());
		
		UiParagraphPanel panel = new UiParagraphPanel();
		
		panel.addComponents(new UiLabel(localization.getFieldLabel("TemplateDetails")), detailScrollPane);

		Box buttons = Box.createHorizontalBox();
		Dimension preferredSize = details.getPreferredSize();
		preferredSize.height = okButton.getPreferredSize().height;				
		buttons.setPreferredSize(preferredSize);						
		Component buttonsToAdd[] = {loadFromFile, Box.createHorizontalGlue(), okButton, cancel, help};
		Utilities.addComponentsRespectingOrientation(buttons, buttonsToAdd);
		
		panel.addOnNewLine(buttons);

		getContentPane().add(panel);
		
		getRootPane().setDefaultButton(okButton);
		Utilities.packAndCenterWindow(this);
	}


	class helpHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			UiLocalization localization = mainWindow.getLocalization();
			String title = localization.getWindowTitle("HelpDefaultDetails");
			String helpMsg = localization.getFieldLabel("HelpDefaultDetails");
			String helpMsgExample = localization.getFieldLabel("HelpExampleDefaultDetails");
			String helpMsgExample1 = localization.getFieldLabel("HelpExample1DefaultDetails");
			String helpMsgExample2 = localization.getFieldLabel("HelpExample2DefaultDetails");
			String helpMsgExampleEtc = localization.getFieldLabel("HelpExampleEtcDefaultDetails");
			String ok = localization.getButtonLabel(EnglishCommonStrings.OK);
			String[] contents = {helpMsg, "", "",helpMsgExample, helpMsgExample1, "", helpMsgExample2, "", helpMsgExampleEtc};
			String[] buttons = {ok};

			new UiNotifyDlg(title, contents, buttons);
		}
	}

	class loadFileHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(mainWindow.confirmDlg("ResetDefaultDetails"))
			{
				details.setText("");
				try
				{
					if(defaultDetailsFile.exists())
						loadFile(defaultDetailsFile);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					mainWindow.notifyDlg("ErrorReadingFile");
				}
			}
		}

	}

	public void loadFile(File fileToLoad) throws IOException
	{
		String data = "";
		BufferedReader reader = new BufferedReader(new UnicodeReader(fileToLoad));
		while(true)
		{
			String line = reader.readLine();
			if(line == null)
				break;
			data += line;
			data += "\n";
		}
		reader.close();
		fontHelper.setDisplayableText(details, data);
	}

	class DefaultDetailsFilter extends FileFilter
	{
		public boolean accept(File pathname)
		{
			if(pathname.isDirectory())
				return true;
			return(pathname.getName().endsWith(MartusApp.DEFAULT_DETAILS_EXTENSION));
		}

		public String getDescription()
		{
			return mainWindow.getLocalization().getFieldLabel("DefaultDetailFiles");
		}
	}


	public boolean getResult()
	{
		return result;
	}

	public void actionPerformed(ActionEvent ae)
	{
		result = false;
		if(ae.getSource() == okButton)
		{
			info.setTemplateDetails(fontHelper.getStorableText(details));
			result = true;
		}
		dispose();
	}


	ConfigInfo info;
	JButton okButton;
	UiTextArea details;
	boolean result;
	UiMainWindow mainWindow;
	File defaultDetailsFile;
	UiFontEncodingHelper fontHelper;
}
