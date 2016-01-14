/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2004-2007, Beneficent
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
import java.util.Vector;

import javax.swing.AbstractAction;

import org.martus.mspa.client.view.MagicWordsDlg;
import org.martus.mspa.main.UiMainWindow;


public class MenuItemManageMagicWords extends AbstractAction
{
	public MenuItemManageMagicWords(UiMainWindow mainWindow, String label)
	{
		super(label);
		parent = mainWindow;
	}
	
	public void actionPerformed(ActionEvent arg0) 
	{
		try
		{
			Vector magicWords = parent.getMSPAApp().getAllMagicWords();
			parent.setStatusText("Retrieve available magicwords: "+parent.getMSPAApp().getStatus());
			
			MagicWordsDlg magicWordsDlg = new MagicWordsDlg(parent, magicWords);
			magicWordsDlg.setVisible(true);
		} 
		catch (Exception e)
		{
			parent.exceptionDialog(e);
		}
	}	
	
	UiMainWindow parent;				
}
