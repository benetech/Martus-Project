/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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
package org.martus.common.fieldspec;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class SearchFieldTreeModel extends DefaultTreeModel
{
	public SearchFieldTreeModel(TreeNode rootNode)
	{
		super(rootNode);
	}
	
	public TreePath findObject(TreePath pathToStartSearch, String code)
	{
		DefaultMutableTreeNode nodeToSearch = (DefaultMutableTreeNode)pathToStartSearch.getLastPathComponent();
		if(nodeToSearch.getChildCount() == 0)
		{
			SearchableFieldChoiceItem item = (SearchableFieldChoiceItem)nodeToSearch.getUserObject();
			if(item != null && item.getCode().equals(code))
				return pathToStartSearch;
		}
		else
		{
			for(int i = 0; i < nodeToSearch.getChildCount(); ++i)
			{
				TreeNode thisChild = nodeToSearch.getChildAt(i);
				TreePath childPath = pathToStartSearch.pathByAddingChild(thisChild);
				TreePath found = findObject(childPath, code);
				if(found != null)
					return found;
			}
		}
		
		return null;
	}
	
}
