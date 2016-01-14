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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.martus.mspa.main.UiMainWindow;
import org.martus.mspa.server.LoadMartusServerArguments;
import org.martus.swing.MartusParagraphLayout;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


public class ServerArgumentsConfigDlg extends JDialog
{
	public ServerArgumentsConfigDlg(UiMainWindow owner) throws Exception
	{
		super(owner, "Martus Server Arguments Configuration", true);		
		parent = owner;				
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());		
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
								
		mainPanel.add(buildArgumentsConfig(), BorderLayout.CENTER);
		mainPanel.add(buildButtonsPanel(), BorderLayout.SOUTH);						

		getContentPane().add(mainPanel);
		Utilities.centerDlg(this);
		setResizable(true);
	}
	
	private JPanel buildArgumentsConfig() throws Exception
	{
		LoadMartusServerArguments arguments = parent.getMSPAApp().getMartusServerArguments();

		JPanel panel = new JPanel();		
		panel.setBorder(new LineBorder(Color.gray));
		panel.setLayout(new MartusParagraphLayout());
		
		JLabel listenerIpLabel = new UiLabel("Listener IP :");
		JLabel amplifierIpLabel = new UiLabel("Amplifier IP :");
		JLabel minutesLabel = new UiLabel("Amplifier-Indexing-Minutes :");
		JLabel passwordLabel = new UiLabel("Password: ");
		
		listenerIpTextField = new JTextField(arguments.getListenerIP(), 20);	
		listenerIpTextField.requestFocus();
		
		amplifierIpTextField = new JTextField(arguments.getAmplifierIP(), 20);
		
		passwordComboField = new JComboBox(passwords);		
		passwordComboField.setEditable(false);
		String passwordStatus = arguments.getPassword();

		if (passwordStatus.equalsIgnoreCase("yes"))	
			passwordComboField.setSelectedIndex(0);
		else
			passwordComboField.setSelectedIndex(1);

		minutsComboField = new JComboBox(minutes);	
		minutsComboField.setSelectedItem(arguments.getMinutes());
		
		amplifier = new JCheckBox("Amplifier",arguments.getAmplifierStatus());	
		clientListener = new JCheckBox("Client-Listener",arguments.getClientListenerStatus());		
		mirrorListener = new JCheckBox("Mirror-Listener",arguments.getMirrorListenerStatus());		
		amplifierListener = new JCheckBox("Amplifier-Listener", arguments.getAmplifierListenerStatus());
		 		
		panel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(listenerIpLabel); 
		panel.add(listenerIpTextField);
		panel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(amplifierIpLabel); 
		panel.add(amplifierIpTextField);
		panel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);

		 
		panel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(passwordLabel); 
		panel.add(passwordComboField);
		panel.add(minutesLabel); 
		panel.add(minutsComboField);
		
		panel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(amplifier); 
		panel.add(clientListener);	
		panel.add(mirrorListener); 
		panel.add(amplifierListener);			
		
		panel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(previewPanel());
		
		return panel;
	}
	
	private JPanel previewPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		
		previewButton = new UiButton("Preview");	
		previewButton.addActionListener(new CommitButtonHandler());		
		previewArea = new UiWrappedTextArea("");
		previewArea.setEditable(false);	
		JScrollPane sp = new JScrollPane(previewArea);	
		sp.setPreferredSize(new Dimension(350,70));	
		
		panel.add(sp);
		panel.add(previewButton);
		
		return panel;
	}
	
	private JPanel buildButtonsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());		
		
		saveButton = new UiButton("Save");	
		saveButton.addActionListener(new CommitButtonHandler());	
		cancelButton = new UiButton("Cancel");
		cancelButton.addActionListener(new CommitButtonHandler());

		panel.add(saveButton);
		panel.add(cancelButton);

		return panel;
	}	

	LoadMartusServerArguments populateData()
	{
		LoadMartusServerArguments arguments = new LoadMartusServerArguments();

		arguments.setProperty(LoadMartusServerArguments.LISTENER_IP, listenerIpTextField.getText());
		arguments.setProperty(LoadMartusServerArguments.AMPLIFIER_IP, amplifierIpTextField.getText());
		arguments.setProperty(LoadMartusServerArguments.PASSWORD, (String) passwordComboField.getSelectedItem());
		arguments.setProperty(LoadMartusServerArguments.AMPLIFIER_INDEXING_MINUTES, (String) minutsComboField.getSelectedItem());

		arguments.setProperty(LoadMartusServerArguments.AMPLIFIER, amplifier.isSelected()?"yes":"no");				
		arguments.setProperty(LoadMartusServerArguments.CLIENT_LISTENER, clientListener.isSelected()?"yes":"no");
		arguments.setProperty(LoadMartusServerArguments.MIRROR_LISTENER, mirrorListener.isSelected()?"yes":"no");
		arguments.setProperty(LoadMartusServerArguments.AMPLIFIER_LISTENER, amplifierListener.isSelected()?"yes":"no");

		return arguments;
	}
	
	class CommitButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			try
			{
				if (ae.getSource().equals(cancelButton))				
					dispose();
				else if (ae.getSource().equals(saveButton))
					handleSendCommand();
				else if (ae.getSource().equals(previewButton))
				{	
					LoadMartusServerArguments args = populateData();
					previewArea.setText(args.toString());
				}
			} 
			catch (Exception e)
			{
				parent.exceptionDialog(e);
			}
			
		}

		private void handleSendCommand() throws Exception
		{								
			parent.getMSPAApp().updateMartusServerArguments(populateData());
			parent.remindNeedsRestart();
			dispose();			
		}
	}
	
	UiMainWindow parent;
	JButton saveButton;
	JButton cancelButton;
	JButton previewButton;
	
	JTextField listenerIpTextField;
	JTextField amplifierIpTextField;
	JComboBox passwordComboField;
	JComboBox minutsComboField;
	UiWrappedTextArea previewArea;
	
	JCheckBox amplifier;
	JCheckBox clientListener;
	JCheckBox mirrorListener;
	JCheckBox amplifierListener; 
		
	String[] passwords = {"yes", "no"};
	String[] minutes = {"5","10","15","20","25","30","35","40","45","50","55","60"};	
}
