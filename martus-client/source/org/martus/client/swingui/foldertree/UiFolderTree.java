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

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.UiMainWindow;
import org.martus.swing.UiLabel;
import org.martus.swing.UiLanguageDirection;


class UiFolderTree extends JTree implements TreeSelectionListener
{
	public UiFolderTree(UiFolderTreePane parentToUse, TreeModel model, ClientBulletinStore storeToUse, UiMainWindow mainWindow)
	{
		super(model);
		parent = parentToUse;
		store = storeToUse;
		observer = mainWindow;
		setRootVisible(false);
		setComponentOrientation(UiLanguageDirection.getComponentOrientation());
		setShowsRootHandles(false);
		setEditable(true);
		setInvokesStopCellEditing(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(this);
		addKeyListener(new FolderKeyAdapter());
	
		dropTarget = new DropTarget(this, new UiFolderTreeDropAdapter(this, mainWindow));
		DefaultTreeCellRenderer renderer = new FolderTreeNodeRenderer();
		setCellRenderer(renderer);
		setCellEditor(new FolderTreeCellEditor(this, renderer));
	}

	public String getSelectedFolderName()
	{
		TreePath path = getSelectionPath();
		if(path == null)
			return "";

		BulletinFolder f = getFolderAt(path);
		if(f == null)
			return "";

		return f.getName();
	}

	public BulletinFolder getFolder(Point at)
	{
		TreePath path = getPathForLocation(at.x, at.y);
		if(path == null)
			return null;
		return getFolderAt(path);
	}

	public BulletinFolder getFolderAt(TreePath path)
	{
		FolderTreeNode node = (FolderTreeNode)path.getLastPathComponent();
		if (node == null)
		{
			return null;
		}
		if (!node.isLeaf())
		{
			return null;
		}

		String name = node.getInternalName();
		return store.findFolder(name);
	}

	// override superclass method
	public boolean isPathEditable(TreePath path)
	{
		BulletinFolder folder = getFolderAt(path);
		if(folder == null)
			return false;

		return folder.canRename();
	}
	
	class FolderKeyAdapter extends KeyAdapter
	{
		public void keyReleased(KeyEvent e)
		{
			if(e.getKeyCode() == KeyEvent.VK_DELETE)
			{
				parent.deleteCurrentFolderIfPossible();
			}
		}
	}
	

	// TreeSelectionListener interface
	public void valueChanged(TreeSelectionEvent e)
 	{
		BulletinFolder folder = getFolderAt(e.getPath());
		if(folder != null)
			observer.folderSelectionHasChanged(folder);
    }

	TreePath getPathOfNode(FolderTreeNode node)
	{
		TreePath rootPath = new TreePath(getModel().getRoot());
		return rootPath.pathByAddingChild(node);
	}

	class FolderTreeNodeRenderer extends DefaultTreeCellRenderer
	{
		FolderTreeNodeRenderer()
		{
			label = new UiLabel();
			label.setHorizontalAlignment(UiLanguageDirection.getHorizontalAlignment());
			label.setComponentOrientation(UiLanguageDirection.getComponentOrientation());
			label.setOpaque(true);

			closedIcon = getClosedIcon();
			openIcon = getOpenIcon();
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
					boolean isSelected, boolean isExpanded, boolean isLeaf,
					int row, boolean hasFocusFlag)
		{
			if(isSelected)
				label.setIcon(openIcon);
			else
				label.setIcon(closedIcon);
			label.setBorder(new EmptyBorder(0,5,0,5));
			FolderTreeNode folderNode = (FolderTreeNode)value;
			BulletinFolder folder = store.findFolder(folderNode.getInternalName());
			String show = "?";
			if(folder != null)
				show = folderNode.getLocalizedName()+ " (" + folder.getBulletinCount() + ")";
			label.setText(show);

			Color foreground = getTextNonSelectionColor();
			Color background = getBackgroundNonSelectionColor();
			if(isSelected)
			{
				foreground = getTextSelectionColor();
				background = getBackgroundSelectionColor();
			}
			label.setForeground(foreground);
			label.setBackground(background);

			return label;
		}


		JLabel label;
		Icon closedIcon;
		Icon openIcon;
	}

	class FolderTreeCellEditor extends DefaultTreeCellEditor implements CellEditorListener
	{
		FolderTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer)
		{
			super(tree, renderer);
			addCellEditorListener(this);
		}

		// begin CellEditorListener interface
		public void editingStopped(ChangeEvent e)
		{
			String newFolderName = getCellEditorValue().toString();

			if(newFolderName.equals(oldLocalizedFolderName))
			{
				newFolderName = oldInternalFolderName;
			}
			else if(store.findFolder(newFolderName)!=null)
			{
				observer.notifyDlg("ErrorRenameFolderExists");
				newFolderName = oldInternalFolderName;				
			}
			else if(!store.renameFolder(oldInternalFolderName, newFolderName))
			{
				observer.notifyDlg("ErrorRenameFolder");
				newFolderName = oldInternalFolderName;				
			}

			TreePath path = getPathOfNode(node);
			getModel().valueForPathChanged(path, newFolderName);
		}

		public Component getTreeCellEditorComponent(JTree treeToUse, Object value, boolean isSelected, boolean expanded, boolean leaf, int row)
		{
			node = (FolderTreeNode)value;
			oldLocalizedFolderName = node.getLocalizedName();
			oldInternalFolderName = node.getInternalName();
			Component textField = super.getTreeCellEditorComponent(treeToUse, value, isSelected, expanded, leaf, row);
			((JTextComponent)editingComponent).selectAll();
			return textField;
		}

		public void editingCanceled(ChangeEvent arg0)
		{
		}
		// end CellEditorListener interface

		String oldLocalizedFolderName;
		String oldInternalFolderName;
		FolderTreeNode node;
	}


	UiFolderTreePane parent;
	ClientBulletinStore store;
	UiMainWindow observer;
	DropTarget dropTarget;
}
