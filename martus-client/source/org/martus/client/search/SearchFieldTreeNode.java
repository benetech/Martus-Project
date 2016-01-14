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
package org.martus.client.search;

import java.util.Collections;

import javax.swing.tree.DefaultMutableTreeNode;

import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;

public class SearchFieldTreeNode extends DefaultMutableTreeNode
{
	public SearchFieldTreeNode(String label, MiniLocalization localizationToUse)
	{
		super(label);
		localization = localizationToUse;
	}
	
	public SearchFieldTreeNode(SearchableFieldChoiceItem choiceItem, MiniLocalization localizationToUse)
	{
		super(choiceItem);
		localization = localizationToUse;
	}
	
	public void setLabel(String string)
	{
		overriddenLabel = string;
	}

	public boolean isSelectable()
	{
		return (getChildCount() == 0);
	}
	
	public SearchableFieldChoiceItem getChoiceItem()
	{
		return (SearchableFieldChoiceItem)getUserObject();
	}
	
	public void sortChildren(String languageCode)
	{
		if(children == null)
			return;
		
		Collections.sort(children, new SearchFieldTreeNodeComparator(languageCode));
	}
	
	public String toString()
	{
		if(overriddenLabel != null)
			return overriddenLabel;

		if(!isSearchableFieldChoiceItemNode())
			return getUserObject().toString();
		
		SearchableFieldChoiceItem choice = getChoiceItem();
		FieldSpec spec = choice.getSpec();
		if(!isChildNodeToDistingishSimilarFields())
			return choice.toString();
		
		String type = localization.getFieldLabel("FieldType" + choice.getType().getTypeName());
		return spec.getTag()+ ": " + type;
			
	}

	private boolean isChildNodeToDistingishSimilarFields()
	{
		return getParent() != null && getParent().getParent() != null;
	}

	public boolean isSearchableFieldChoiceItemNode()
	{
		return (getUserObject() instanceof SearchableFieldChoiceItem);
	}
	
	public String getSortValue()
	{
		if(getChildCount() > 0)
			return (String)getUserObject();

		SearchableFieldChoiceItem choice = getChoiceItem();
		return choice.getSpec().getLabel();
	}
	
	static class SearchFieldTreeNodeComparator extends SaneCollator
	{
		public SearchFieldTreeNodeComparator(String languageCode)
		{
			super(languageCode);
		}
		
		public int compare(Object o1, Object o2)
		{
			SearchFieldTreeNode node1 = (SearchFieldTreeNode)o1;
			SearchFieldTreeNode node2 = (SearchFieldTreeNode)o2;
			
			return super.compare(node1.getSortValue(), node2.getSortValue());
		}
		
	}
	
	private MiniLocalization localization;
	private String overriddenLabel;

}
