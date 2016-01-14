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
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.martus.mspa.common.network.NetworkInterfaceConstants;
import org.martus.mspa.main.UiMainWindow;


public class MenuItemServerCommands extends AbstractAction
{
	public MenuItemServerCommands(UiMainWindow mainWindow, String type)
	{
		super(type);	
		parent = mainWindow;
		menuType = type;		
	}

	public void actionPerformed(ActionEvent arg0) 
	{
		try
		{
			if (menuType.equals(UiMainWindow.STATUS_MARTUS_SERVER))
			{
				parent.setStatusText("Checking server status...");
				Vector results = parent.getMSPAApp().sendCommandToServer(NetworkInterfaceConstants.GET_STATUS,"");			
				handleQueryResults(results, "Status");			
			}
			if (menuType.equals(UiMainWindow.START_MARTUS_SERVER))
			{	
				String msg = "This will start the MartusServer services";
				parent.setStatusText(msg);
				int confirmation = JOptionPane.showConfirmDialog(parent, msg, "Start Server", JOptionPane.OK_CANCEL_OPTION);
				if(confirmation != JOptionPane.OK_OPTION)
					return;
				Vector results = parent.getMSPAApp().sendCommandToServer(NetworkInterfaceConstants.START_SERVER,"");			
				handleCommandResults(results, "Start");			
			}
			
			if (menuType.equals(UiMainWindow.STOP_MARTUS_SERVER))
			{	
				int answer = JOptionPane.showConfirmDialog(parent, 
					"This command will stop the server, preventing any users from accessing it until it is started again.\n\n Are you sure you want to do this?",
					"Stop Server", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION) 
				{
					parent.setStatusText("Stopping Server ...");
					Vector results = parent.getMSPAApp().sendCommandToServer(NetworkInterfaceConstants.STOP_SERVER,"");
					handleCommandResults(results, "Stop");						
				} 	
			}	
			if (menuType.equals(UiMainWindow.RESTART_MARTUS_SERVER))
			{	
				int answer = JOptionPane.showConfirmDialog(parent, 
					"This command will stop and then restart the server, " +
					"preventing any users from accessing it until the restart is complete.\n\n " +
					"Are you sure you want to do this?",
					"Restart Server", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION) 
				{
					parent.setStatusText("Stopping and Restarting Server ...");
					Vector results = parent.getMSPAApp().sendCommandToServer(NetworkInterfaceConstants.RESTART_SERVER,"");
					handleCommandResults(results, "Restart");
				} 	
			}
		}
		catch (Exception e)
		{
			parent.exceptionDialog(e);
		}	
	}
	
	private void handleCommandResults(Vector results, String type)
	{
		String status = (String) results.get(0);
		if (status.equals(NetworkInterfaceConstants.OK))
		{
			JOptionPane.showMessageDialog(parent, results.get(1), type, JOptionPane.INFORMATION_MESSAGE);
			parent.setStatusText("");
		}
		else
		{	
			JOptionPane.showMessageDialog(parent, results.get(1), type, JOptionPane.ERROR_MESSAGE);
			parent.setStatusText("");
		}
	}
	
	private void handleQueryResults(Vector results, String type)
	{
		String status = (String) results.get(0);
		if (status.equals(NetworkInterfaceConstants.OK))
		{
			JOptionPane.showMessageDialog(parent, results.get(1), type, JOptionPane.INFORMATION_MESSAGE);
			parent.setStatusText("");
		}
		else
		{	
			JOptionPane.showMessageDialog(parent, results.get(1), type, JOptionPane.ERROR_MESSAGE);
			parent.setStatusText("");
		}
	}
	
	UiMainWindow parent;
	String menuType;	
}
