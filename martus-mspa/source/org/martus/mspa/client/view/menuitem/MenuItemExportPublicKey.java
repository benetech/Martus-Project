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
package org.martus.mspa.client.view.menuitem;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.martus.common.crypto.MartusCrypto;
import org.martus.mspa.main.UiMainWindow;


public class MenuItemExportPublicKey extends AbstractAction
{
	public MenuItemExportPublicKey(UiMainWindow mainWindow, String label)
	{
		super(label);	
		parent = mainWindow;
	}

	public void actionPerformed(ActionEvent arg0) 
	{								
		try
		{
			File keypair = parent.getMSPAApp().getKeypairFile();
			exportPublicKey(parent, keypair);
		} 
		catch (Exception e)
		{
			parent.exceptionDialog(e);
		}
	}

	public static void exportPublicKey(UiMainWindow parent, File keypair) throws Exception
	{
		if (!keypair.exists())
		{
			JOptionPane.showMessageDialog(parent, keypair.getParent()+"Keypair not found.",
				 "MSPA Error Message", JOptionPane.ERROR_MESSAGE);
		}	
				
		File outputFile = new File(keypair.getParentFile(), "publicKey.txt");
		
		String publicCode = MartusCrypto.computeFormattedPublicCode(parent.getMSPAApp().getSecurity().getPublicKeyString());
		parent.getMSPAApp().exportServerPublicKeyFile(outputFile);		
		if (outputFile.exists())				
			JOptionPane.showMessageDialog(parent, "<html>Public key with public code <strong>" + publicCode + "</strong> has been exported at "+outputFile.getPath(),
			 "Export Public Key", JOptionPane.INFORMATION_MESSAGE);
		else
			JOptionPane.showMessageDialog(parent, "Public export FAILED: "+outputFile.getPath(),
			 "Export Error", JOptionPane.ERROR_MESSAGE);
	}
	
	UiMainWindow parent;
}
