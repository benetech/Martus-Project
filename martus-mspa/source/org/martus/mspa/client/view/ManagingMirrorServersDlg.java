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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.martus.mspa.client.core.MirrorServerLabelFinder;
import org.martus.mspa.client.core.MirrorServerLabelInfo;
import org.martus.mspa.main.UiMainWindow;
import org.martus.swing.MartusParagraphLayout;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.Utilities;

public class ManagingMirrorServersDlg extends JDialog
{
	public ManagingMirrorServersDlg(UiMainWindow owner, int manageType, 
			String serverToManage, String serverToManagePublicCode,
			Vector allList, Vector currentList)
	{
		super(owner);
		msgLabelInfo = MirrorServerLabelFinder.getMessageInfo(manageType);
		setTitle("Other Servers: "+ msgLabelInfo.getTitle());
		parent = owner;
		serverManageType = manageType;
		availableItems = allList;
		assignedItems = currentList;	
	
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getTopPanel(), BorderLayout.NORTH);
		getContentPane().add(getCenterPanel(), BorderLayout.CENTER);
		getContentPane().add(getCommitButtonsPanel(), BorderLayout.SOUTH);
						
		Utilities.centerDlg(this);
	}
	
	private JPanel getTopPanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(),""));
		panel.setLayout(new MartusParagraphLayout());
		
		manageIPAddr = new JTextField(20);		
		manageIPAddr.requestFocus();		
		managePublicCode = new JTextField(20);					
		
		panel.add(new UiLabel("IP Address: "), MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(manageIPAddr);
		panel.add(new UiLabel("Public Code: "), MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(managePublicCode);
		mirrorServerName = new JTextField(20);
		panel.add(new UiLabel("Server Name: "), MartusParagraphLayout.NEW_PARAGRAPH);	
		panel.add(mirrorServerName);			
				
		collectMirrorInfo(panel);
		
		return panel;				
	}	
	
	private void configureTabList()
	{
		TabListCellRenderer renderer = new TabListCellRenderer();
		renderer.setTabs(new int[] {130, 200, 300});
		availableList.setCellRenderer(renderer);
		
		TabListCellRenderer renderer2 = new TabListCellRenderer();
		renderer2.setTabs(new int[] {130, 200, 300});
		allowedList.setCellRenderer(renderer);
	}
	
	private void collectMirrorInfo(JPanel panel)
	{
		addNewMirrorServer = createButton("Add");						
		panel.add(addNewMirrorServer);				
	}
	
	private JPanel getCenterPanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder (EtchedBorder.RAISED));
		panel.setLayout(new FlowLayout());
				
		panel.add(getAvailablePanel());
		panel.add(getShiftButtons());
		panel.add(getAllowedPanel());
		
		configureTabList();	

		return panel;
	}
	
	private JPanel getAvailablePanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		availableListModel = loadElementsToList(availableItems);
		availableList = new JList(availableListModel);
		availableList.setFixedCellWidth(270);   
		 
		JScrollPane ps = createScrollPane();			
		ps.getViewport().add(availableList);
		JLabel availableLabel = new UiLabel(msgLabelInfo.getAvailableLabel());
				
		panel.add(availableLabel);
		panel.add(ps);

		return panel;
	}	
	
	private JPanel getAllowedPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		allowedListModel = loadElementsToList(assignedItems);
		allowedList = new JList(allowedListModel);
		allowedList.setFixedCellWidth(270);
		    
		JScrollPane ps = createScrollPane();
		ps.getViewport().add(allowedList);
		JLabel allowedLabel = new UiLabel( msgLabelInfo.getAllowedLabel());
				
		panel.add(allowedLabel);
		panel.add(ps);

		return panel;
	}
	
	JScrollPane createScrollPane()
	{
		JScrollPane ps = new JScrollPane();		
		ps.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		ps.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		return ps;
	}
	
	private DefaultListModel loadElementsToList(Vector items)
	{
		DefaultListModel listModel = new DefaultListModel();
		
		for (int i=0; i<items.size();++i)
			listModel.add(i, items.get(i));
			
		return listModel;
	}	
	
	private JPanel getShiftButtons()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5,5,5,5));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		addButton = createButton(">>");					
		removeButton = createButton("<<");		
		
		panel.add(addButton);
		panel.add(removeButton);
		
		return panel;

	}
	
	
	private JPanel getCommitButtonsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());		
				
		viewComplainButton = createButton("View Compliance");			
		updateButton = createButton("Save");				
		cancelButton = createButton("Cancel");						
				
//		panel.add(viewComplainButton);
		panel.add(updateButton);
		panel.add(cancelButton);
		
		return panel;
	}
	
	private JButton createButton(String label)
	{
		JButton button = new UiButton(label);
		button.addActionListener(new ButtonHandler());
		return button;
	}
	
	String generateFileName(String serverName, String ip, String publicKey)
	{
		String fileName =serverName+"-ip="+ip+"-code="+publicKey+".txt";
		return fileName.trim();
	}
	
	boolean isValidServerName(String name)
	{
		if (name == null || name.length() <=0)
			return false;
			
		if (name.indexOf("-") >0)
			return false;						
			
		char ch = name.charAt(0);				
		if (Character.isDigit(ch))
			return false;
							
		return true;	
	}
	
	void postStatus(String msgHeader)
	{
		parent.setStatusText(parent.getMSPAApp().getStatus());
	}
	
	class ButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			try
			{
				if (ae.getSource().equals(cancelButton))				
					dispose();
				else if (ae.getSource().equals(addButton))
					handleAddToAllowedList();
				else if (ae.getSource().equals(updateButton))
					handleUpdateMirrorServerInfo();
				else if (ae.getSource().equals(removeButton))
					handleRemoveFromAllowedList();
				else if (ae.getSource().equals(addNewMirrorServer))
					handleRequestAddNewMirrorServer();	
				else if (ae.getSource().equals(viewComplainButton))
					handleRequestViewCompliant();
			} 
			catch (Exception e)
			{
				parent.exceptionDialog(e);
			}			
		}
		
		private void handleRequestAddNewMirrorServer() throws Exception
		{
			Vector mirrorServerInfo = new Vector();
			String mirrorIP = manageIPAddr.getText();
			String mirrorPublicCode = managePublicCode.getText();
			String serverName = mirrorServerName.getText();
			
			if (mirrorIP.length()<=0 || 
				mirrorPublicCode.length()<=0) 
			{	
				JOptionPane.showMessageDialog(parent, "IP address, public code and server name are required.", 
					"Missing Infomation", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if (!isValidServerName(serverName))
			{
				JOptionPane.showMessageDialog(parent, "Server Name contains invalid character(s).", 
					"Illegal Server Name", JOptionPane.ERROR_MESSAGE);
				return;
			}					
			
			mirrorServerInfo.add(mirrorIP);
			mirrorServerInfo.add(mirrorPublicCode);		
			
			mirrorFileName = generateFileName(serverName, mirrorIP,mirrorPublicCode); 
			mirrorServerInfo.add(mirrorFileName);										
				
			boolean result = parent.getMSPAApp().addMirrorServer(mirrorServerInfo);
			postStatus("Request add mirror server: ");
			if (result)
			{			
				availableItems.add(mirrorFileName);
				availableListModel.addElement(mirrorFileName);
				parent.remindNeedsRestart();
			}
			else
			{
				JOptionPane.showMessageDialog(parent, "Error no response from server.", 
					"Server Info", JOptionPane.ERROR_MESSAGE);				
			}				
			manageIPAddr.setText("");
			managePublicCode.setText("");
			mirrorServerName.setText("");	
							
		}		
		
		private void handleAddToAllowedList()
		{
			int selectItem = availableList.getSelectedIndex();	
			if (!availableList.isSelectionEmpty())
			{	
				String item = (String) availableList.getSelectedValue();
				if (!allowedListModel.contains(item))
				{						
					allowedListModel.addElement(item);
					availableListModel.remove(selectItem);
				}				
			}
		}
		
		private void handleUpdateMirrorServerInfo() throws Exception
		{
			Object[] items = allowedListModel.toArray();
			Vector itemCollection = new Vector();			
			for (int i=0;i<items.length;i++)
				itemCollection.add(items[i]);
				
			parent.getMSPAApp().updateManageMirrorAccounts(itemCollection, serverManageType);
			postStatus("Update manage mirror accounts: ");
			parent.remindNeedsRestart();
		
			dispose();							
		}
		
		private void handleRemoveFromAllowedList()
		{			
			int selectItem = allowedList.getSelectedIndex();	
			if (!allowedList.isSelectionEmpty())
			{	
				String item = (String) allowedList.getSelectedValue();				
		
				allowedListModel.remove(selectItem);
				if (!availableListModel.contains(item))								
					availableListModel.addElement(item);
			}							
		}		
		
		private void handleRequestViewCompliant() throws Exception
		{			
			String compliants = parent.getMSPAApp().getServerCompliant();
			postStatus("Request Serve Compliant: ");
			ServerComplianceDlg dlg = new ServerComplianceDlg(parent, compliants);
			dlg.setVisible(true);
		}	
	}
	
	UiMainWindow parent; 	
	
	JTextField manageIPAddr;
	JTextField managePublicCode;
	JTextField mirrorServerName;
	
	JButton addButton;
	JButton removeButton;
	JButton viewComplainButton;
	JButton updateButton;
	JButton cancelButton;
	JButton addNewMirrorServer;	
	
	Vector availableItems;
	Vector assignedItems;	
	
	JList availableList;
	JList allowedList;	
	DefaultListModel availableListModel;
	DefaultListModel allowedListModel;
	
	int serverManageType;
	MirrorServerLabelInfo msgLabelInfo;
	String mirrorFileName;
	
}
