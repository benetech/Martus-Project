/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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
package org.martus.mspa.client.view.menuitem;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.martus.clientside.UiFileChooser;
import org.martus.mspa.client.view.ServerConnectionDlg;
import org.martus.mspa.main.UiMainWindow;
import org.martus.util.FileTransfer;

public class ImportServerPublicKeyAction extends AbstractAction
{
	public ImportServerPublicKeyAction(ServerConnectionDlg parentDialog, File serverPublicKeysDirectory)
	{
		super("Import Server Public Key");
		connectionDialog = parentDialog;
		mainWindow = parentDialog.getMainWindow();
		serverKeysDirectory = serverPublicKeysDirectory;
	}
	
	public void actionPerformed(ActionEvent event)
	{
		File fromFile = getFileToImport();
		if(fromFile == null)
			return;
		
		while(true)
		{
			String ip = JOptionPane.showInputDialog("Enter IP address of server");
			if(ip == null)
				return;
			
			File toFile = new File(serverKeysDirectory, "ip=" + ip);
			if(toFile.exists())
			{
				mainWindow.notifyDialog("Import Server Public Key", 
						"<html>A key already exists for that IP address. <br>" +
						"You must delete it before importing a new one.");
				continue;
			}
			
			try
			{
				serverKeysDirectory.mkdirs();
				FileTransfer.copyFile(fromFile, toFile);
				connectionDialog.refreshAvailableServerList();
				break;
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				mainWindow.notifyDialog("Import Server Public Key", "Error: Import failed");
			}
			
		}
	}

	private File getFileToImport()
	{
		String continueLabel = "Continue";
		File homeDirectory = UiFileChooser.getHomeDirectoryFile();
		UiFileChooser.FileDialogResults results = UiFileChooser.displayFileOpenDialog(mainWindow, "Import Server Public Key", homeDirectory, continueLabel, null);
		if (results.wasCancelChoosen())
			return null;
	
		File importFile = results.getChosenFile();
		if(!importFile.exists() || !importFile.isFile() )
			return null;
		return importFile;
	}
	
	ServerConnectionDlg connectionDialog;
	UiMainWindow mainWindow;
	File serverKeysDirectory;
}
