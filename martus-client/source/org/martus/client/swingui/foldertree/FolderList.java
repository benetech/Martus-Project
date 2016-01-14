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

import java.util.Vector;

import javax.swing.tree.DefaultTreeModel;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.clientside.UiLocalization;

public class FolderList extends DefaultTreeModel
{
	public FolderList(UiLocalization localizationToUse)
	{
		super(new FolderTreeNode("?", localizationToUse));
		localization = localizationToUse;
		rootAsFolderTreeNode = (FolderTreeNode)getRoot();
	}

	public int getCount()
	{
		return rootAsFolderTreeNode.getChildCount();
	}

	public void loadFolders(ClientBulletinStore store)
	{
		while(getCount() > 0)
		{
			FolderTreeNode item = (FolderTreeNode)getChild(rootAsFolderTreeNode, 0);
			removeNodeFromParent(item);
		}

		Vector visibleFolderNames = store.getVisibleFolderNames();

		for(int f = 0; f < visibleFolderNames.size(); ++f)
		{
			String folderName = (String)visibleFolderNames.get(f);
			FolderTreeNode item = new FolderTreeNode(folderName, localization);
			insertNodeInto(item, rootAsFolderTreeNode, getCount());
		}
	}

	public String getName(int index)
	{
		FolderTreeNode node = getNode(index);
		return node.toString();
	}

	public FolderTreeNode getNode(int index)
	{
		return (FolderTreeNode)getChild(rootAsFolderTreeNode, index);
	}

	public FolderTreeNode findFolderByInternalName(String folderName)
	{
		for(int i = 0; i < getCount(); ++i)
		{
			FolderTreeNode node = getNode(i);
			if(folderName.equals(node.getInternalName()))
				return node;
		}
		return null;
	}


	private FolderTreeNode rootAsFolderTreeNode;
	private UiLocalization localization;
}
