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
import javax.swing.tree.TreeNode;

public class PopUpTreeFieldSpec extends FieldSpec
{
	public PopUpTreeFieldSpec()
	{
		this(new SearchFieldTreeModel(new DefaultMutableTreeNode()));
	}

	public PopUpTreeFieldSpec(SearchFieldTreeModel modelToUse)
	{
		super(new FieldTypePopUpTree());
		model = modelToUse;
	}
	
	public SearchFieldTreeModel getTreeModel()
	{
		return model;
	}
	
	public SearchableFieldChoiceItem getFirstChoice()
	{
		TreeNode root = (TreeNode)model.getRoot();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(0);
		SearchableFieldChoiceItem item = (SearchableFieldChoiceItem)node.getUserObject();
		return item;
	}
	
	public SearchableFieldChoiceItem findSearchTag(String code)
	{
		return findCode(code);
	}
	
	public SearchableFieldChoiceItem findCode(String codeToFind)
	{
		TreeNode root = (TreeNode)model.getRoot();
		return findCode(root, codeToFind);
	}

	private SearchableFieldChoiceItem findCode(TreeNode root, String codeToFind)
	{
		for(int i = 0; i < root.getChildCount(); ++i)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(i);
			if(node.getChildCount() == 0)
			{
				SearchableFieldChoiceItem item = (SearchableFieldChoiceItem)node.getUserObject();
				String code = item.getCode();
				if(code.equals(codeToFind))
					return item;
			}
			else
			{
				SearchableFieldChoiceItem found = findCode(node, codeToFind);
				if(found != null)
					return found;
			}
		}
		return null;
	}
	
	public SearchFieldTreeModel getModel()
	{
		return model;
	}
	
	SearchFieldTreeModel model;
}
