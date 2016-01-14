/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.EnglishCommonStrings;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiList;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiVBox;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class UiSetFolderOrderDlg extends JDialog implements ActionListener
{
	public UiSetFolderOrderDlg(UiMainWindow owner, Vector originalFolderOrderToUse)
	{
		super(owner.getSwingFrame(), "", true);
		originalFolderOrder = originalFolderOrderToUse;
		hiddenFolders = new Vector();
		okPressed = false;
		localization = owner.getLocalization();
		setTitle(localization.getWindowTitle("SetFolderOrder"));
		model = new DefaultListModel();
		for(int i = 0; i < originalFolderOrder.size(); ++i)
		{
			BulletinFolder bulletinFolder = ((BulletinFolder)originalFolderOrder.get(i));
			if(bulletinFolder.isVisible())
				model.addElement(new LocalizedBulletinFolder(bulletinFolder));
			else
				hiddenFolders.add(bulletinFolder);
		}

		up = new UiButton(localization.getButtonLabel("FolderOrderUp"));
		up.addActionListener(this);
		down = new UiButton(localization.getButtonLabel("FolderOrderDown"));
		down.addActionListener(this);
		
		UiVBox upDownPanel = new UiVBox();
		upDownPanel.add(up);
		upDownPanel.addSpace();
		upDownPanel.add(down);
		
		folderList = new UiList();
		folderList.setModel(model);
		folderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		folderList.setLayoutOrientation(JList.VERTICAL);
		
		UiScrollPane scroller = new UiScrollPane(folderList	);

		ok = new UiButton(localization.getButtonLabel(EnglishCommonStrings.OK));
		ok.addActionListener(this);
		cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(this);
		Box okCancelBox = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(okCancelBox, new Component[] {ok, new UiLabel("   "), cancel, Box.createHorizontalGlue()});

		UiParagraphPanel foldersPanel = new UiParagraphPanel();
		foldersPanel.addComponents(scroller, upDownPanel);
		foldersPanel.addBlankLine();
		
		String rawText = localization.getFieldLabel("SetFolderOrder");
		HashMap map = new HashMap();
		map.put("#MoveFolderUp#", up.getText());
		map.put("#MoveFolderDown#", down.getText());
		String information = ""; 
		try
		{
			information = TokenReplacement.replaceTokens(rawText, map);
		}
		catch(TokenInvalidException e)
		{
			e.printStackTrace();
		}
		UiWrappedTextArea info = new UiWrappedTextArea(information, 40);
		info.setBorder(new EmptyBorder(5,5,5,5));
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(info,BorderLayout.NORTH);
		contentPane.add(foldersPanel, BorderLayout.CENTER);
		contentPane.add(okCancelBox,BorderLayout.SOUTH);
		getRootPane().setDefaultButton(ok);
		
		Utilities.packAndCenterWindow(this);
		setResizable(true);
	}
	
	public Vector getNewFolderOrder()
	{
		return newFolderOrder;
	}
	
	public boolean okPressed()
	{
		return okPressed;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		if(source == up)
			moveItemUp();
		if(source == down)
			moveItemDown();
		if(source == cancel)
			dispose();
		if(source == ok)
		{
			okPressed = true;
			updateNewFolderOrder();
			dispose();
		}
	}

	private void updateNewFolderOrder()
	{
		newFolderOrder = new Vector();
		newFolderOrder.addAll(hiddenFolders);
		for(int i = 0; i < model.size(); ++i)
		{
			newFolderOrder.add(((LocalizedBulletinFolder)model.get(i)).getFolder());
		}
	}
	
	private void moveItemUp()
	{
		int currentIndex = folderList.getSelectedIndex();
		int newIndex = currentIndex - 1 ;
		moveFolder(currentIndex, newIndex);
	}

	private void moveItemDown()
	{
		int currentIndex = folderList.getSelectedIndex();
		int newIndex = currentIndex + 1;
		moveFolder(currentIndex, newIndex);
	}

	private void moveFolder(int currentIndex, int newIndex)
	{
		if(currentIndex == -1)
			return;
		if(newIndex < 0 || newIndex > model.getSize()-1)
			return;
		Object folderToMove = folderList.getSelectedValue();
		model.remove(currentIndex);
		model.add(newIndex, folderToMove);
		folderList.setSelectedIndex(newIndex);
		folderList.ensureIndexIsVisible(newIndex);
	}
	
	class LocalizedBulletinFolder 
	{
		public LocalizedBulletinFolder(BulletinFolder bulletinFolderToUse)
		{
			folder = bulletinFolderToUse;
		}
		
		public String toString()
		{
			return folder.getLocalizedName(localization);
		}
		
		public BulletinFolder getFolder()
		{
			return folder;
		}
		
		private BulletinFolder folder;
	}
	
	Vector newFolderOrder;
	Vector originalFolderOrder;
	Vector hiddenFolders;
	MartusLocalization localization;
	UiList folderList;
	UiButton ok;
	UiButton cancel;
	UiButton up;
	UiButton down;
	DefaultListModel model;
	boolean okPressed;
}
