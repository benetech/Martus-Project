/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2010, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.common;

import java.util.Arrays;
import java.util.Vector;

import org.martus.common.fieldspec.ChoiceItem;

public class ListOfReusableChoicesLists
{
	public ListOfReusableChoicesLists()
	{
		reusableChoicesLists = new Vector();
	}
	
	public ListOfReusableChoicesLists(ReusableChoices[] reusableChoicesListsToUse)
	{
		this();
		reusableChoicesLists.addAll(Arrays.asList(reusableChoicesListsToUse));
	}
	
	public ListOfReusableChoicesLists(ReusableChoices singleSetOfReusableChoices)
	{
		this(new ReusableChoices[] {singleSetOfReusableChoices});
	}

	public ListOfReusableChoicesLists(PoolOfReusableChoicesLists poolOfReusableChoicesLists, String[] reusableChoicesCodes)
	{
		this();
		for(int i = 0; i < reusableChoicesCodes.length; ++i)
		{
			add(poolOfReusableChoicesLists.getChoices(reusableChoicesCodes[i]));
		}
	}

	public int size()
	{
		return reusableChoicesLists.size();
	}
	
	public ReusableChoices get(int index)
	{
		return (ReusableChoices) reusableChoicesLists.get(index);
	}

	public ReusableChoices getLastLevel()
	{
		return get(size() - 1);
	}

	public void add(ReusableChoices reusableChoices)
	{
		reusableChoicesLists.add(reusableChoices);
	}
	
	public String[] getDisplayValuesAtAllLevels(String newText)
	{
		String[] displayText = new String[size()];
		for(int level = 0; level < size(); ++level)
		{
			ChoiceItem[] choices = get(level).getChoices();
			displayText[level] = findLabelByCode(choices, newText);
			if(displayText[level].length() == 0)
			{
				displayText[level] = findLabelByFullCodeAllowingPartialMatches(choices, newText);
			}
		}
		return displayText;
	}

	private String findLabelByCode(ChoiceItem[] choices, String code)
	{
		for(int index = 0; index < choices.length; ++index)
			if(code.equals(choices[index].getCode()))
				return choices[index].toString();
		
		return "";
	}

	private String findLabelByFullCodeAllowingPartialMatches(ChoiceItem[] choices, String code)
	{
		for(int index = 0; index < choices.length; ++index)
		{
			if(choices[index].codeIsAtStartOf(code))
				return choices[index].toString();
		}
		
		return "";
	}

	private Vector reusableChoicesLists;

}
