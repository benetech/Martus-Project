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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import org.martus.client.core.ZawgyiLabelUtilities;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.swing.FontHandler;

public class FieldChoicesByLabel
{
	public FieldChoicesByLabel(MiniLocalization localizationToUse)
	{
		allChoices = new Vector();
		fontHelper = new UiFontEncodingHelper(FontHandler.isDoZawgyiConversion());
		localization = localizationToUse;
	}
	
	public void add(ChoiceItem itemToAdd)
	{
		allChoices.add(itemToAdd);
	}
	
	public void addAll(Collection itemsToAdd)
	{
		Iterator iter = itemsToAdd.iterator();
		while(iter.hasNext())
		{
			ChoiceItem choice = (ChoiceItem)iter.next();
			String label = ZawgyiLabelUtilities.getDisplayableLabel(choice.getSpec(), localization);
			choice.setLabel(label);
			add(choice);
		}
	}
	
	public void onlyKeep(MiniFieldSpec[] allowedSpecs)
	{
		HashSet allowedSpecSet = new HashSet();
		allowedSpecSet.addAll(Arrays.asList(allowedSpecs));
		Vector newChoices = new Vector();
		for(int i = 0; i < allChoices.size(); ++i)
		{
			ChoiceItem choice = (ChoiceItem)allChoices.get(i);
			MiniFieldSpec spec = new MiniFieldSpec(choice.getSpec());
			spec.setLabel(fontHelper.getStorable(spec.getLabel()));
			spec.setTopLevelLabel(fontHelper.getStorable(spec.getTopLevelLabel()));
			if(isSpecInAllowed(spec, allowedSpecSet))
				newChoices.add(choice);
		}
		
		allChoices = newChoices;
	}

	private boolean isSpecInAllowed(MiniFieldSpec specToFind, HashSet allowedSpecSet)
	{
		return allowedSpecSet.contains(specToFind);
	}
	
	public ChoiceItem[] getRawChoices()
	{
		return (ChoiceItem[])allChoices.toArray(new ChoiceItem[0]);
	}
	
	public FieldSpec[] asArray(MiniLocalization localizationToUse)
	{
		Collections.sort(allChoices, new ChoiceItemSorterByLabelTagType(localizationToUse));
		mergeSimilarDropdowns();

		FieldSpec[] specs = new FieldSpec[allChoices.size()]; 
		for(int i = 0; i < allChoices.size(); ++i)
		{
			ChoiceItem choice = (ChoiceItem)allChoices.get(i);
			specs[i] = choice.getSpec();
		}
		
		return specs;
	}
	
	public TreeNode asTree(MiniLocalization localizationToUse)
	{
		Collections.sort(allChoices, new ChoiceItemSorterByLabelTagType(localizationToUse));
		mergeSimilarDropdowns();
		sortAllChoicesWithinDropdowns();

		SearchFieldTreeNode root = new SearchFieldTreeNode("", localizationToUse);
		SearchableFieldChoiceItem[] choices = getChoicesAsArray();
		int index = 0;
		while(index < choices.length)
		{
			String label = choices[index].getSpec().getLabel();
			SearchFieldTreeNode node = new SearchFieldTreeNode(label, localizationToUse);
			node.add(new SearchFieldTreeNode(choices[index], localizationToUse));
			addSimilarNodes(node, choices, index + 1, localizationToUse);
			index += node.getChildCount();
			node = pullUpIfOnlyOneChild(node);
			differentiateChildNodes(node);
			root.add(node);
		}

		return root;
	}
	
	void mergeSimilarDropdowns()
	{
		int mergeInto = 0;
		while(mergeInto + 1 < allChoices.size())
		{
			int mergeFrom = mergeInto + 1;
			SearchableFieldChoiceItem into = ((SearchableFieldChoiceItem)allChoices.get(mergeInto));
			SearchableFieldChoiceItem from = ((SearchableFieldChoiceItem)allChoices.get(mergeFrom));
			if(into.getSpec().equals(from.getSpec()))
			{
				allChoices.remove(mergeFrom);
			}
			else if(areDropDownChoicesMergeable(into, from))
			{
				SearchableFieldChoiceItem result = mergeDropDownChoices(into, from);
				allChoices.set(mergeInto, result);
				allChoices.remove(mergeFrom);
			}
			else
			{
				++mergeInto;
			}
		}
	}
	
	public static boolean areDropDownChoicesMergeable(SearchableFieldChoiceItem choice1, SearchableFieldChoiceItem choice2)
	{
		FieldSpec rawSpec1 = choice1.getSpec();
		FieldSpec rawSpec2 = choice2.getSpec();
		if(!rawSpec1.getType().isDropdown())
			return false;
		if(!rawSpec2.getType().isDropdown())
			return false;
		if(!rawSpec1.getTag().equals(rawSpec2.getTag()))
			return false;
		if(!rawSpec1.getLabel().equals(rawSpec2.getLabel()))
			return false;
		
		DropDownFieldSpec spec1 = (DropDownFieldSpec)rawSpec1;
		DropDownFieldSpec spec2 = (DropDownFieldSpec)rawSpec2;
		return Arrays.equals(spec1.getReusableChoicesCodes(), spec2.getReusableChoicesCodes());
	}
	
	public static SearchableFieldChoiceItem mergeDropDownChoices(SearchableFieldChoiceItem mergeInto, SearchableFieldChoiceItem mergeFrom)
	{
		if(!areDropDownChoicesMergeable(mergeInto, mergeFrom))
			throw new RuntimeException("Attempted to merge unmergeable fieldspecs");
		
		DropDownFieldSpec specInto = (DropDownFieldSpec)mergeInto.getSpec();
		DropDownFieldSpec specFrom = (DropDownFieldSpec)mergeFrom.getSpec();
		
		Vector choices = new Vector(Arrays.asList(specInto.getAllChoices()));
		ChoiceItem[] moreChoices = specFrom.getAllChoices();
		for(int i = 0; i < moreChoices.length; ++i)
		{
			if(!choices.contains(moreChoices[i]))
				choices.add(moreChoices[i]);
		}
		
		CustomDropDownFieldSpec resultSpec = new CustomDropDownFieldSpec();
		resultSpec.setTag(mergeInto.getSpec().getSubFieldTag());
		resultSpec.setLabel(mergeInto.getSpec().getLabel());
		resultSpec.setParent(mergeInto.getSpec().getParent());
		resultSpec.pullDynamicChoiceSettingsFrom(specFrom);
		// NOTE: Must setChoices AFTER pulling dynamic choices
		resultSpec.setChoices((ChoiceItem[]) choices.toArray(new ChoiceItem[0]));
		return new SearchableFieldChoiceItem(mergeInto.getSpecialCode(), resultSpec);
	}
	
	private void sortAllChoicesWithinDropdowns()
	{
		for(int i = 0; i < allChoices.size(); ++i)
		{
			SearchableFieldChoiceItem choiceItem = (SearchableFieldChoiceItem) allChoices.get(i);
			FieldSpec spec = choiceItem.getSpec();
			if(!spec.getType().isDropdown())
				continue;
			
			DropDownFieldSpec dropdownSpec = (DropDownFieldSpec) spec;
			dropdownSpec.sortChoicesByLabel();
		}
	}

	private SearchableFieldChoiceItem[] getChoicesAsArray()
	{
		return (SearchableFieldChoiceItem[])allChoices.toArray(new SearchableFieldChoiceItem[0]);
	}
	
	private SearchFieldTreeNode pullUpIfOnlyOneChild(SearchFieldTreeNode node)
	{
		if(node.getChildCount() == 1)
			node = (SearchFieldTreeNode)node.getChildAt(0);
		return node;
	}
	
	private void differentiateChildNodes(SearchFieldTreeNode parent)
	{
		SearchFieldTreeNode[] needDifferentiation = getIndexesOfReusableDropdownChildren(parent);
		for(int i = 0; i < needDifferentiation.length; ++i)
		{
			SearchFieldTreeNode child = needDifferentiation[i];
			SearchableFieldChoiceItem choice = child.getChoiceItem();
			FieldSpec spec = choice.getSpec();
			String reusableListLabels = "";
			for(int level = 0; level < spec.getReusableChoicesCodes().length; ++level)
			{
				if(level > 0)
					reusableListLabels += ", ";
				reusableListLabels += spec.getReusableChoicesCodes()[level];
			}
			child.setLabel(child.toString() + " (" + reusableListLabels + ")");
		}
	}

	private SearchFieldTreeNode[] getIndexesOfReusableDropdownChildren(SearchFieldTreeNode parent)
	{
		Vector reusableDropdownNodes = new Vector();
		for(int i = 0; i < parent.getChildCount(); ++i)
		{
			SearchFieldTreeNode child = (SearchFieldTreeNode)parent.getChildAt(i);
			if(!child.isSearchableFieldChoiceItemNode())
				continue;
			SearchableFieldChoiceItem choice = child.getChoiceItem();
			FieldSpec spec = choice.getSpec();
			if(spec.getReusableChoicesCodes().length == 0)
				continue;
			reusableDropdownNodes.add(child);
		}
		
		return (SearchFieldTreeNode[]) reusableDropdownNodes.toArray(new SearchFieldTreeNode[0]);
	}

	private void addSimilarNodes(SearchFieldTreeNode parent, SearchableFieldChoiceItem[] choices, int startAt, MiniLocalization localizationToUse)
	{
		String label = parent.toString();
		int index = startAt;
		while(index < choices.length && choices[index].getSpec().getLabel().equals(label))
		{
			SearchableFieldChoiceItem choice = choices[index];
			parent.add(new SearchFieldTreeNode(choice, localizationToUse));
			++index;
		}
		
	}
	
	Vector allChoices;
	UiFontEncodingHelper fontHelper;
	MiniLocalization localization;
}