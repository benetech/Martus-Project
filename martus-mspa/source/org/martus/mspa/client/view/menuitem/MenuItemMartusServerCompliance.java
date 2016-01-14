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

import javax.swing.AbstractAction;

import org.martus.mspa.client.view.ServerComplianceDlg;
import org.martus.mspa.main.UiMainWindow;


public class MenuItemMartusServerCompliance extends AbstractAction
{
	public MenuItemMartusServerCompliance(UiMainWindow mainWindow, String label)
	{
		super(label);	
		parent = mainWindow;
		
	}

	public void actionPerformed(ActionEvent arg0) 
	{								
		try
		{
			String compliants = parent.getMSPAApp().getServerCompliant();
			parent.setStatusText(parent.getMSPAApp().getStatus());		
			ServerComplianceDlg dlg = new ServerComplianceDlg(parent, compliants);
			dlg.setVisible(true);
		} 
		catch (Exception e)
		{
			parent.exceptionDialog(e);
		}			
	}
	
	UiMainWindow parent;	
}
