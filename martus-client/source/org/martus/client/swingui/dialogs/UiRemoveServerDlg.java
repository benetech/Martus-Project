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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.martus.client.core.ConfigInfo;
import org.martus.client.swingui.UiMainWindow;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiTextField;
import org.martus.swing.Utilities;


public class UiRemoveServerDlg extends JDialog implements ActionListener
{
	public UiRemoveServerDlg(UiMainWindow owner,ConfigInfo info)
	{
		super(owner.getSwingFrame(), "", true);
		UiLocalization localization = owner.getLocalization();			
		
		setTitle(localization.getWindowTitle("RemoveServer"));
				
		String serverName = info.getServerName();		
		UiTextField serverField = new UiTextField(serverName);
		serverField.setEditable(false);		
		
		JLabel msgLabel1 = new UiLabel(localization.getFieldLabel("RemoveServerLabel1"));		
		JLabel msgLabel2 = new UiLabel(localization.getFieldLabel("RemoveServerLabel2"));
		
		String serverIPAddress = info.getServerName();
								
		UiTextField serversField = new UiTextField(serverIPAddress);
		serversField.setPreferredSize(new Dimension(10,20));
		serversField.setEditable(false);						

		yes = new UiButton(localization.getButtonLabel(EnglishCommonStrings.YES));		
		yes.addActionListener(this);
		JButton no = new UiButton(localization.getButtonLabel(EnglishCommonStrings.NO));
		no.addActionListener(this);

		UiParagraphPanel panel = new UiParagraphPanel();
		panel.addBlankLine();
		panel.addComponents(msgLabel1, serverField);
		panel.addLabelOnly(msgLabel2);			
		panel.addBlankLine();		
		panel.addComponents(yes, no);

		getContentPane().add(panel);
		getRootPane().setDefaultButton(yes);
		Utilities.packAndCenterWindow(this);
		setVisible(true);
	}	

	public void actionPerformed(ActionEvent ae)
	{	
		action=false;
		if(ae.getSource() == yes)
		{			
			action = true;
		}			
		dispose();
	}
		
	public boolean isYesButtonPressed()
	{
		return action;
	}

	
	boolean action;
	JButton yes;		
}
