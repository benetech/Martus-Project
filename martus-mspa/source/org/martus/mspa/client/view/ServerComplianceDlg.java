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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.martus.mspa.main.UiMainWindow;
import org.martus.swing.MartusParagraphLayout;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


public class ServerComplianceDlg extends JDialog
{
	public ServerComplianceDlg(UiMainWindow owner, String compliants)
	{
		super(owner, "View Server Compliance", true);	
		parent = owner;				
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());		
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
								
		mainPanel.add(buildCompliantTextArea(compliants), BorderLayout.CENTER);
		mainPanel.add(buildButtonsPanel(), BorderLayout.SOUTH);						

		getContentPane().add(mainPanel);
		Utilities.centerDlg(this);
		setResizable(false);
	}
	
	private JPanel buildCompliantTextArea(String compliants)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new MartusParagraphLayout());
		
		JLabel label = new UiLabel("Compliance for this Martus Server:");
		
		complianceEditor = new UiWrappedTextArea(compliants);
		complianceEditor.setBackground(Color.WHITE);
		complianceEditor.setEditable(true);		
		
		JScrollPane sp = new JScrollPane();
		sp.getViewport().add(complianceEditor);
		sp.setPreferredSize(new Dimension(250,180));
		
		panel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(label); 
		panel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(sp);
		
		return panel;
	}
	
	private JPanel buildButtonsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());		
		
		saveButton = new UiButton("Save");	
		saveButton.addActionListener(new CommitButtonHandler());	
		closeButton = new UiButton("Cancel");
		closeButton.addActionListener(new CommitButtonHandler());

		panel.add(saveButton);
		panel.add(closeButton);

		return panel;
	}	
	
	class CommitButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			try
			{
				if (ae.getSource().equals(closeButton))				
					dispose();
				else if (ae.getSource().equals(saveButton))
					handleSave();
			} 
			catch (Exception e)
			{
				parent.exceptionDialog(e);
			}
			
		}

		private void handleSave() throws Exception
		{					
			parent.setStatusText("Update compliance to Martus server ...");
			Vector results = parent.getMSPAApp().updateServerCompliant(complianceEditor.getText().trim());
			parent.setStatusText((String) results.get(0));
			parent.remindNeedsRestart();
			dispose();			
		}
	}
	
	UiMainWindow parent;
	JButton saveButton;
	JButton closeButton;
	UiWrappedTextArea complianceEditor;

}
