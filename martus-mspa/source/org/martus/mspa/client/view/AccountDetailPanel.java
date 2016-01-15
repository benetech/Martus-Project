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


package org.martus.mspa.client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.mspa.client.core.MSPAClient;
import org.martus.mspa.common.AccountAdminOptions;
import org.martus.mspa.main.UiMainWindow;
import org.martus.swing.MartusParagraphLayout;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiTextArea;
import org.martus.swing.UiWrappedTextArea;

public class AccountDetailPanel extends JPanel
{
	public AccountDetailPanel(UiMainWindow windowToUse, String id, Vector contactInfo, Vector hiddenBulletins, 
			Vector bulletinIds, Vector manageAccount) throws Exception
	{
		accountId = id;	
		parent = windowToUse;
		app = parent.getMSPAApp();		
		hiddenBulletinIds =	hiddenBulletins;
		originalBulletinIds = bulletinIds;
		
		setBorder(new EmptyBorder(5,5,5,5));
		setLayout(new BorderLayout());

		loadAccountAdminInfo(manageAccount);		
	
		add(buildTopPanel(),BorderLayout.NORTH);		
		add(createBottomTabbedPanel(contactInfo), BorderLayout.CENTER);			
	}

	Vector loadBulletinIds(Vector bulletins)
	{
		Vector newBulletinIds = new Vector();		
		if (bulletins == null || bulletins.size() <=0)
			return newBulletinIds;
								
		for (int i=0; i<bulletins.size(); ++i)
		{	
			Vector bulletinInfo= (Vector) bulletins.get(i);
			String bulletinId = (String) bulletinInfo.get(0)+" \t"+(String)bulletinInfo.get(1);
			newBulletinIds.add(bulletinId);		
		}

		return newBulletinIds;
	}

	private void loadAccountAdminInfo(Vector accountManagement)
	{
		admOptions = new AccountAdminOptions();
		admOptions.setOptions(accountManagement);							
	} 

	private JPanel buildContactPanel(Vector contactInfo)
	{
		JPanel centerPanel = new JPanel();		
		centerPanel.setLayout(new FlowLayout());
		centerPanel.add(buildContactInfoPanel(contactInfo));		
		return centerPanel;
	}

	private JPanel buildTopPanel() throws Exception
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5,5,5,5));
		panel.setLayout(new MartusParagraphLayout());
		
		panel.add(new UiLabel(MartusCrypto.computeFormattedPublicCode(accountId)), MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(new UiLabel(MartusCrypto.computeFormattedPublicCode40(accountId)), MartusParagraphLayout.NEW_PARAGRAPH);

		panel.add(buildCheckboxes(), MartusParagraphLayout.NEW_PARAGRAPH);
		
		JLabel bulletinCountLabel = new UiLabel("Total Bulletin Count: ");
		bulletinCountField = new JTextField(Integer.toString(originalBulletinIds.size()), 5);
		bulletinCountField.setEditable(false);
		panel.add(bulletinCountLabel, MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(bulletinCountField);
		
		JLabel numOfDelBulletinLabel = new UiLabel("Number of Hidden Bulletins: ");
		numOfDelBulletineField = new JTextField(Integer.toString(hiddenBulletinIds.size()),5);
		numOfDelBulletineField.setEditable(false);		
		panel.add(numOfDelBulletinLabel, MartusParagraphLayout.NEW_PARAGRAPH);
		panel.add(numOfDelBulletineField);		

//		panel.add(new UiLabel("") , ParagraphLayout.NEW_PARAGRAPH);
//		panel.add(buildButtonsPanel());
		
		return panel;	
	}

	private JPanel buildCheckboxes()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
		panel.setBorder(new EmptyBorder(5,5,5,5));

		canUpload = new JCheckBox("Can Upload", admOptions.canUploadSelected());	
		canUpload.addActionListener(new CheckBoxHandler());	
		banned = new JCheckBox("Banned", admOptions.isBannedSelected());
		banned.addActionListener(new CheckBoxHandler());	
		canSendToAmp = new JCheckBox("Amplified", admOptions.canSendToAmplifySelected());
		canSendToAmp.addActionListener(new CheckBoxHandler());	
		
		saveButton = new UiButton("Save");
		saveButton.addActionListener(new CommitButtonHandler());
		
		panel.add(canUpload);
		panel.add(banned);
		if(MSPAClient.INCLUDE_AMPLIFICATION)
		{
			panel.add(canSendToAmp);
		}
		panel.add(saveButton);
		
		return panel;

	}
		
	private JPanel buildContactInfoPanel(Vector contactInfo)
	{
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder (new LineBorder (Color.gray, 1)," Contact Info "));
		panel.setLayout(new MartusParagraphLayout());
											
		panel.add(new UiLabel("Author: ") , MartusParagraphLayout.NEW_PARAGRAPH);			
		JTextField author = new JTextField(30);		
		author.setEditable(false);
		panel.add(author);		
		
		panel.add(new UiLabel("Organization: "),MartusParagraphLayout.NEW_PARAGRAPH);			
		JTextField  organization = new JTextField(30);			
		organization.setEditable(false);
		panel.add(organization);
						
		panel.add(new UiLabel("Email Address: "),MartusParagraphLayout.NEW_PARAGRAPH);			
		JTextField email = new JTextField(30);
		email.setEditable(false);
		panel.add(email);				
		
		panel.add(new UiLabel("Web Page: "),MartusParagraphLayout.NEW_PARAGRAPH);			
		JTextField web = new JTextField(30);
		web.setEditable(false);
		panel.add(web);	
		
		panel.add(new UiLabel("Phone #: "),MartusParagraphLayout.NEW_PARAGRAPH);			
		JTextField phone = new JTextField(30);
		phone.setEditable(false);
		panel.add(phone);		
		
		panel.add(new UiLabel("Mailing Address: "),MartusParagraphLayout.NEW_PARAGRAPH);
		UiTextArea address = new UiTextArea(4, 35);		
		address.setEditable(false);
		JScrollPane addressScrollPane = new JScrollPane(address, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(addressScrollPane,MartusParagraphLayout.NEW_LINE);
			
		if (!contactInfo.isEmpty())
		{
			int index = 2;	
			String fieldName = (String)contactInfo.get(index++);
			author.setText(fieldName);		
				
			fieldName = (String)contactInfo.get(index++);
			organization.setText(fieldName);
			
			fieldName = (String)contactInfo.get(index++);
			email.setText(fieldName);
						
			fieldName = (String)contactInfo.get(index++);
			web.setText(fieldName);
			
			fieldName = (String)contactInfo.get(index++);
			phone.setText(fieldName);	
						
			fieldName = (String)contactInfo.get(index++);			
			address.setText(fieldName);
					
		}
				
		return panel;
	}
	
//	private JPanel buildButtonsPanel()
//	{
//		JPanel panel = new JPanel();	
//		FlowLayout layout = new FlowLayout();
//		layout.setAlignment(FlowLayout.CENTER);
//		panel.setLayout(layout);
//		
//		viewStatistics = new UiButton("View Statistics");
//		viewStatistics.addActionListener(new CommitButtonHandler());									
//		viewActivity = new UiButton("View Activity");
//		viewActivity.addActionListener(new CommitButtonHandler());						
//		
//		panel.add(viewStatistics);
//		panel.add(viewActivity);		
//
//		return panel;
//	}	

	private DefaultListModel loadElementsToList(Vector items)
	{
		DefaultListModel listModel = new DefaultListModel();
		
		for (int i=0; i<items.size();++i)
			listModel.add(i, items.get(i));
			
		return listModel;
	}

	private void configureTabList(JList list)
	{
		TabListCellRenderer renderer = new TabListCellRenderer();
		renderer.setTabs(new int[] {130, 200, 300});
		list.setCellRenderer(renderer);
		
		TabListCellRenderer renderer2 = new TabListCellRenderer();
		renderer2.setTabs(new int[] {130, 200, 300});
		list.setCellRenderer(renderer);
	}	

	private JTabbedPane createBottomTabbedPanel(Vector contactInfo)
	{
		bulletinTabPane = new JTabbedPane();				
		bulletinTabPane.setTabPlacement(JTabbedPane.TOP);
		
		bulletinTabPane.add("Contact Information", buildContactPanel(contactInfo));

		Vector formattedBulletinsIds = loadBulletinIds(originalBulletinIds);
		bulletinListModel = loadElementsToList(formattedBulletinsIds);
		bulletinList = createBulletinList(bulletinListModel);
		bulletinList.setName("bulletin");
		
		viewBulletinButton = new UiButton("View Bulletin");
		bulletinTabPane.add("Active Bulletins", getDisplayBulletinPanel(bulletinList, viewBulletinButton));

		hiddenListModel = loadElementsToList(hiddenBulletinIds);
		hiddenList = createBulletinList(hiddenListModel);
		hiddenList.setName("hidden");
		hiddenList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		viewHiddenButton = new UiButton("View Hidden Bulletin");
		bulletinTabPane.add("Hidden Bulletins", getDisplayBulletinPanel(hiddenList,viewHiddenButton));

		UiWrappedTextArea fullAccountId = new UiWrappedTextArea(accountId, 40);
		fullAccountId.setEditable(false);
		bulletinTabPane.add("Full Account Id", new JScrollPane(fullAccountId));
		return bulletinTabPane;
	}

	private JList createBulletinList(DefaultListModel dataModel)
	{
		JList list = new JList(dataModel);
		list.setFixedCellWidth(220);
		configureTabList(list);

		return list;
	}

	private JPanel getDisplayBulletinPanel(JList list, JButton view)
	{
		JPanel buttonPanel = new JPanel();
		FlowLayout layout = new FlowLayout();
		layout.setAlignment(FlowLayout.RIGHT);
		buttonPanel.setLayout(layout);
		
		view.addActionListener(new CommitButtonHandler());	
//		buttonPanel.add(view);

		if (list.getName().equals("bulletin"))
		{
			delBulletins = new UiButton("Hide Bulletin");
			delBulletins.addActionListener(new CommitButtonHandler());	
			buttonPanel.add(delBulletins);
		}	
		else
		{
			recoverHiddenButton = new UiButton("Recover Hidden Bulletin");
			recoverHiddenButton.addActionListener(new CommitButtonHandler());	
			buttonPanel.add(recoverHiddenButton);
		}		

		JScrollPane ps = createScrollPane();
		ps.setPreferredSize(new Dimension(220, 150));
		ps.setMinimumSize(new Dimension(220, 150));
		ps.setAlignmentX(LEFT_ALIGNMENT);			
		ps.getViewport().add(list);	

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5,5,5,5));		
		panel.setLayout(new BorderLayout());		

		panel.add(ps, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}	

	JScrollPane createScrollPane()
	{
		JScrollPane ps = new JScrollPane();		
		ps.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		ps.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		return ps;
	}
	
	void postStatus(String msgHeader)
	{
		parent.setStatusText(msgHeader+app.getStatus());
	}
	
	class CheckBoxHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{		
			admOptions = new AccountAdminOptions();											
			admOptions.setCanUploadOption(canUpload.isSelected());
			admOptions.setBannedOption(banned.isSelected());	
			admOptions.setCanSendOption(canSendToAmp.isSelected());							
		}
	}
	
	class CommitButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			try
			{
				if (ae.getSource().equals(saveButton))				
					handleConfigurationAccountInfo();
				else if (ae.getSource().equals(delBulletins))				
					handleHideBulletin();		
				else if (ae.getSource().equals(recoverHiddenButton))				
					handleUnhideBulletin();
			}
			catch (Exception e)
			{
				parent.exceptionDialog(e);
			}					
		}

		private void handleConfigurationAccountInfo() throws Exception
		{								
			app.updateAccountManageInfo(accountId, admOptions.getOptions());
			parent.remindNeedsRestart();
		}

		private void handleUnhideBulletin() throws Exception
		{
											
			if (!hiddenList.isSelectionEmpty())
			{	
				List items = hiddenList.getSelectedValuesList();
				Vector recoverList = new Vector();
				for (int i=0;i< items.size();++i)
				{
					String item = (String) items.get(i);
					recoverList.add(item);
					hiddenListModel.removeElement(item);			
				}
				
				app.recoverHiddenBulletin(accountId, recoverList);
				postStatus("Recover Hidden Bulletin :");
				parent.remindNeedsRestart();
						
				Vector hiddenBulletins = app.getListOfHiddenBulletins(accountId);
				postStatus("List of Hidden Bulletins :");
				if (hiddenBulletins != null)
					numOfDelBulletineField.setText(Integer.toString(hiddenBulletins.size()));
					
				Vector rawBullectins = app.getPacketDirNames(accountId);
				postStatus("Get Packets directory :");	
				Vector listOfFormattedBulletins = loadBulletinIds(rawBullectins);
				bulletinListModel.removeAllElements();
				for (int i=0; i<listOfFormattedBulletins.size();++i)
					bulletinListModel.add(i, listOfFormattedBulletins.get(i));
		
			}			
		}

		private void handleHideBulletin() throws Exception
		{
											
			if (!bulletinList.isSelectionEmpty())
			{	
				List items = bulletinList.getSelectedValuesList();
				Vector hiddenSealedList = new Vector();
				for (int i=0;i< items.size();++i)
				{
					String item = (String) items.get(i);
	
					String status = item.substring(item.indexOf("\t")+1);			
					if (Bulletin.isImmutable(status))				
					{					
						hiddenSealedList.add(item);
						bulletinListModel.removeElement(item);
					}
					else
					{
						String errorMessage = "Hiding a draft bulletin is not supported at thie moment.\n" ;
						app.warningMessageDlg(errorMessage);						
					}				
				}
				
				app.removeBulletin(accountId, hiddenSealedList);
				postStatus("Hide Bulletins: ");
				parent.remindNeedsRestart();
						
				Vector hiddenBulletins = app.getListOfHiddenBulletins(accountId);
				postStatus("Get a list of Hidden Bulletins :");
				if (hiddenBulletins != null)
				{
					numOfDelBulletineField.setText(Integer.toString(hiddenBulletins.size()));
					hiddenListModel.removeAllElements();
					for (int i=0; i<hiddenBulletins.size();++i)
						hiddenListModel.add(i, hiddenBulletins.get(i));
				}		
			}			
		}
	}
	
	String accountId;
	JButton saveButton;
	AccountAdminOptions admOptions;
	
	JCheckBox canUpload;
	JCheckBox banned;	
	JCheckBox canSendToAmp;

	JButton viewBulletinButton;
	JButton viewHiddenButton;
	JButton delBulletins;
	JButton viewActivity;
	JButton viewStatistics;
	JButton recoverHiddenButton;

	JList bulletinList;	
	DefaultListModel bulletinListModel;
	JList hiddenList;
	DefaultListModel hiddenListModel;

	MSPAClient app;
	UiMainWindow parent;
	Vector originalBulletinIds;
	Vector hiddenBulletinIds;	
	JTabbedPane bulletinTabPane;
	JTextField bulletinCountField;
	JTextField numOfDelBulletineField;
}
