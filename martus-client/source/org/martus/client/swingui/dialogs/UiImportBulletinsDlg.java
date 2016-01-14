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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.bulletinstore.ImportBulletins;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.filefilters.XmlFileFilter;
import org.martus.clientside.FormatFilter;
import org.martus.common.EnglishCommonStrings;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiTextField;
import org.martus.swing.Utilities;

public class UiImportBulletinsDlg extends JDialog implements ActionListener
{

	public UiImportBulletinsDlg(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse.getSwingFrame(), "", true);
		mainWindow = mainWindowToUse;
		
		fileToImport = getFileToImport();
		if(fileToImport == null)
			return;
		constructDialog();
	}
	
	private File getFileToImport()
	{
		FormatFilter importFilter = new XmlFileFilter(mainWindow.getLocalization());
		return mainWindow.showFileOpenDialog("ImportBulletins", importFilter);
	}	
	
	private void constructDialog()
	{
		MartusLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle(IMPORT_BULLETINS_TITLE));
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		importingFolder = new UiTextField(20);
		
		contentPane.add(new UiLabel(localization.getFieldLabel("ImportBulletinsIntoWhichFolder")), BorderLayout.NORTH);
		JPanel panel = new JPanel();
		panel. add(importingFolder);
		contentPane.add(panel, BorderLayout.CENTER);
		
		ok = new UiButton(localization.getButtonLabel("Continue"));
		ok.addActionListener(this);
		getRootPane().setDefaultButton(ok);
		
		UiButton cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(this);
		Box buttons = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(buttons, new Component[]{ok, cancel, Box.createHorizontalGlue()});
		contentPane.add(buttons, BorderLayout.SOUTH);
		
		Utilities.packAndCenterWindow(this);
		setResizable(true);
		setVisible(true);
	
	}	
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(ok))
		{
			if(!folderValid())
				return;
			doImport();
		}
		dispose();
	}
	
	private void doImport()
	{
		ImportBulletins importer = new ImportBulletins(mainWindow);
		importer.doImport(fileToImport, folderName);
	}
	
	
	private boolean folderValid()
	{
		folderName = importingFolder.getText();
		ClientBulletinStore clientStore =mainWindow.getStore();
		if(folderName.length() == 0)
		{
			mainWindow.notifyDlg("NoImportFileSpecified");
			return false;
		}
		if(!clientStore.isFolderNameValid(folderName))
		{
			mainWindow.notifyDlg("ErrorRenameFolder");
			return false;
		}
		return true;
	}
	
	private static final String IMPORT_BULLETINS_TITLE = "ImportBulletins";
	UiMainWindow mainWindow;
	private UiButton ok;
	private UiTextField importingFolder;
	private File fileToImport;
	private String folderName;
	boolean okToImport;
}
	
	
