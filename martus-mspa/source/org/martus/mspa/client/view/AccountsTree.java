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

import java.awt.Cursor;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.martus.mspa.main.UiMainWindow;

public class AccountsTree
{
	public AccountsTree(Vector accounts, UiMainWindow mainWin)
	{		

		parentWindow = mainWin;
			
		DefaultTreeModel model = null;

		DefaultMutableTreeNode top = new DefaultMutableTreeNode();		
		loadAccountsToTreeNode(accounts.toArray(), top);
		 		
		model = new DefaultTreeModel(top);		
		tree = new JTree(model);
		tree.setRootVisible(false);

		tree.addTreeSelectionListener(new AccountNodeSelectionListener());
		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); 	
		tree.setShowsRootHandles(true); 
		tree.setEditable(false);
		if(tree.getRowCount() > 0)
			tree.setSelectionRow(0);		

		scrollPane = new JScrollPane();
		scrollPane.getViewport().add(tree);
		
		tree.setSelectionRow(0);		

	}
	
	DefaultMutableTreeNode getTreeNode(TreePath path)
	{
		return (DefaultMutableTreeNode)(path.getLastPathComponent());
	}

	private void loadAccountsToTreeNode(Object[] accountArray, DefaultMutableTreeNode parent)
	{
		AccountNode[] nodes = new AccountNode[accountArray.length]; 
		for (int i=0;i<accountArray.length;i++)
			nodes[i] = new AccountNode((String) accountArray[i], "");
		
		Arrays.sort(nodes);
		for(int i = 0; i < nodes.length; ++i)
			parent.add(nodes[i]);
		
	}

	public JScrollPane getScrollPane()
	{
		return scrollPane;
	}
	
	class AccountNodeSelectionListener implements TreeSelectionListener 
	{
		public void valueChanged(TreeSelectionEvent e)
		{				
			DefaultMutableTreeNode node = getTreeNode(e.getPath());
			if (node == null)
				return;
			
			Cursor oldCursor = tree.getCursor();
			tree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try
			{
				if (node.isRoot())
				{
					parentWindow.loadEmptyAccountDetailPanel();
				}
				else	
				{
					AccountNode selectedAccountNode = (AccountNode) node;
					parentWindow.loadAccountDetailPanel(selectedAccountNode.getAccountId(), selectedAccountNode.getDisplayName());
				}
			} 
			catch (Exception e1)
			{
				e1.printStackTrace();
				parentWindow.exceptionDialog(e1);
			}		
			finally
			{
				tree.setCursor(oldCursor);
			}
		}
	}

	JTree tree;
	JScrollPane scrollPane;	
	UiMainWindow parentWindow;
}

