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

package org.martus.client.swingui.foldertree;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JViewport;
import javax.swing.tree.TreePath;
import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.swing.UiPopupMenu;
import org.martus.swing.UiScrollPane;

public class UiFolderTreePane extends UiScrollPane
{
	public UiFolderTreePane(UiMainWindow mainWindow)
	{
		super();
		parent = mainWindow;
		localization = mainWindow.getLocalization();
		store = parent.getStore();

		model = new FolderList(localization);
		model.loadFolders(store);

		tree = new UiFolderTree(this, model, store, parent);
		tree.addMouseListener(new FolderTreeMouseAdapter());

		viewport.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		viewport.add(tree);
	}

	public boolean selectFolder(String internalFolderName)
	{
		TreePath path = getPathOfFolder(internalFolderName);
		if(path == null)
			return false;

		tree.clearSelection();
		tree.addSelectionPath(path);
		return true;
	}

	public String getSelectedFolderName()
	{
		return tree.getSelectedFolderName();
	}

	public BulletinFolder getSelectedFolder()
	{
		return store.findFolder(getSelectedFolderName());
	}

	public void createNewFolder()
	{
		tree.stopEditing();

		String originalFolderName = localization.getFieldLabel("defaultFolderName");
		BulletinFolder newFolder = parent.getApp().createUniqueFolder(originalFolderName);
		if(newFolder == null)
			return;
		parent.folderTreeContentsHaveChanged();
		FolderTreeNode node = model.findFolderByInternalName(newFolder.getName());
		if(node == null)
			return;
		tree.stopEditing();
		tree.startEditingAtPath(getPathOfNode(node));
		return;
	}

	public void renameCurrentFolder()
	{
		FolderTreeNode node = getCurrentFolderNode();
		ActionRename rename = new ActionRename(node);
		rename.actionPerformed(null);
	}
	
	public Vector getAllFolders()
	{
		return store.getAllFolders();
	}
	
	public void setFolderOrder(Vector foldersInOrder) throws Exception
	{
		store.setFolderOrder(foldersInOrder);
		model.loadFolders(store);
		model.reload();
	}

	public void deleteCurrentFolderIfPossible()
	{
		deleteFolderNodeIfPossible(getCurrentFolderNode());
	}

	public FolderTreeNode getCurrentFolderNode()
	{
		FolderTreeNode node = null;
		TreePath path = tree.getSelectionPath();
		if(path != null)
			node = (FolderTreeNode)path.getLastPathComponent();
		return node;
	}

	public void folderTreeContentsHaveChanged()
	{
		String selectedName = tree.getSelectedFolderName();
		model.loadFolders(store);
		if(!selectFolder(selectedName))
			parent.selectSentFolder();
	}

	public void folderContentsHaveChanged(BulletinFolder f)
	{
		FolderTreeNode node = model.findFolderByInternalName(f.getName());
		if(node != null)
			model.nodeChanged(node);
		
		invalidate();
		repaint();
	}

	class FolderTreeMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			if(!e.isMetaDown())
				return;

			FolderTreeNode node = null;
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if(path != null)
			{
				tree.setSelectionPath(path);
				node = (FolderTreeNode)path.getLastPathComponent();
			}

			UiPopupMenu menu = new UiPopupMenu();
			menu.add(new JMenuItem(new ActionNewFolder()));
			menu.add(new JMenuItem(new ActionRename(node)));
			menu.add(new JMenuItem(new ActionDelete(node)));
			menu.addSeparator();
			menu.add(parent.getActionMenuPaste());
			menu.show(UiFolderTreePane.this, e.getX(), e.getY());
		}
	}

	// TODO: Consider merging these with the menubar and/or toolbar actions
	class ActionNewFolder extends AbstractAction
	{
		public ActionNewFolder()
		{
			super(localization.getMenuLabel("CreateNewFolder"), null);
		}

		public void actionPerformed(ActionEvent ae)
		{
			createNewFolder();
			return;
		}

		public boolean isEnabled()
		{
			return true;
		}

	}

	public boolean canDeleteFolder(FolderTreeNode nodeToDelete)
	{
		if(nodeToDelete == null)
			return false;

		BulletinFolder folder = store.findFolder(nodeToDelete.getInternalName());
		if(folder == null || !folder.canDelete())
			return false;

		return true;
	}

	public void deleteFolderNodeIfPossible(FolderTreeNode nodeToDelete)
	{
		if(!canDeleteFolder(nodeToDelete))
			return;

		parent.setWaitingCursor();
		final String internalName = nodeToDelete.getInternalName();
		final boolean isFolderEmpty = store.findFolder(internalName).getBulletinCount() == 0;
		if(isFolderEmpty || parent.confirmDlg("deletefolder"))
		{
			store.deleteFolder(internalName);
			parent.folderTreeContentsHaveChanged();
		}
		parent.resetCursor();
	}

	class ActionDelete extends AbstractAction
	{
		public ActionDelete(FolderTreeNode node)
		{
			String text = localization.getMenuLabel("DeleteFolder") + " ";
			if(node != null)
				text += node.getLocalizedName();

			putValue(NAME, text);
			nodeToDelete = node;
		}

		public void actionPerformed(ActionEvent ae)
		{
			deleteFolderNodeIfPossible(nodeToDelete);
		}

		public boolean isEnabled()
		{
			return canDeleteFolder(nodeToDelete);
		}


		FolderTreeNode nodeToDelete;
	}

	class ActionRename extends AbstractAction
	{
		public ActionRename(FolderTreeNode node)
		{
			String text = parent.getLocalization().getMenuLabel("RenameFolder") + " ";
			if(node != null)
				text += node.getLocalizedName();

			putValue(NAME, text);
			nodeToRename = node;
		}

		public void actionPerformed(ActionEvent ae)
		{
			System.out.println("Rename " + nodeToRename.getLocalizedName());
			TreePath path = getPathOfNode(nodeToRename);
			if(!tree.isPathEditable(path))
				return;

			tree.startEditingAtPath(path);
		}

		public boolean isEnabled()
		{
			if(nodeToRename == null)
				return false;

			BulletinFolder folder = store.findFolder(nodeToRename.getInternalName());
			if(folder != null && folder.canRename())
				return true;

			return false;
		}


		FolderTreeNode nodeToRename;
	}


	TreePath getPathOfNode(FolderTreeNode node)
	{
		TreePath rootPath = new TreePath(model.getRoot());
		return rootPath.pathByAddingChild(node);
	}

	private TreePath getPathOfFolder(String internalFolderName)
	{
		FolderTreeNode node = model.findFolderByInternalName(internalFolderName);
		if(node == null)
			return null;
		return getPathOfNode(node);
	}


	UiMainWindow parent;
	MartusLocalization localization;
	ClientBulletinStore store;
	FolderList model;
	UiFolderTree tree;
}
