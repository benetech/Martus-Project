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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.martus.common.fieldspec.ChoiceItem;


public class PoolOfReusableChoicesLists
{
	public PoolOfReusableChoicesLists()
	{
		namedReusableChoices = new HashMap();
	}
	
	public void add(ReusableChoices choices)
	{
		namedReusableChoices.put(choices.getCode(), choices);
	}

	public ChoiceItem findChoiceFromFullOrPartialCode(String[] reusableChoiceListCodes, String fullOrPartialCode)
	{
		int levelCount = reusableChoiceListCodes.length;
		for(int level = 0; level < levelCount; ++level)
		{
			ReusableChoices choices = getChoices(reusableChoiceListCodes[level]);
			if(choices == null)
				return null;
			
			int LAST = levelCount - 1;
			ChoiceItem found = null;
			if(level == LAST)
			{
				found = findItemByCode(choices, fullOrPartialCode);
			}
			else
			{
				found = findItemByPartialMatch(choices, fullOrPartialCode);
			}
			
			if(found != null)
				return found;
		}
		
		return null;
	}

	private ChoiceItem findItemByCode(ReusableChoices choices, String fullCode)
	{
		return choices.findByCode(fullCode);
	}

	private ChoiceItem findItemByPartialMatch(ReusableChoices choices, String fullOrPartialCode)
	{
		return choices.findByFullOrPartialCode(fullOrPartialCode);
	}

	public void mergeAll(PoolOfReusableChoicesLists reusableChoicesLists)
	{
		Set otherNames = reusableChoicesLists.getAvailableNames();
		Iterator it = otherNames.iterator();
		while(it.hasNext())
		{
			String name = (String)it.next();
			merge(reusableChoicesLists.getChoices(name));
		}
	}

	private void merge(ReusableChoices choices)
	{
		ReusableChoices existingChoices = getChoices(choices.getCode());
		if(existingChoices == null)
		{
			ReusableChoices copy = new ReusableChoices(choices.getCode(), choices.getLabel());
			for(int i = 0; i < choices.size(); ++i)
			{
				ChoiceItem thisChoice = choices.get(i);
				copy.add(new ChoiceItem(thisChoice.getCode(), thisChoice.getLabel()));
			}
			add(copy);
			return;
		}

		for(int i = 0; i < choices.size(); ++i)
		{
			ChoiceItem incoming = choices.get(i);
			String code = new String(incoming.getCode());
			String label = new String(incoming.toString());
			ChoiceItem existingChoice = existingChoices.findByCode(code);
			if(existingChoice == null)
			{
				existingChoices.add(new ChoiceItem(code, label));
				continue;
			}
			String existingLabel = existingChoice.toString();
			if(existingLabel.equals(label))
				continue;
			int positionAtEnd = existingLabel.length() - label.length();
			if(positionAtEnd > 0 && existingLabel.substring(positionAtEnd).equals(label))
				continue;
			String SEPARATOR = "; ";
			if(existingLabel.indexOf(label + SEPARATOR) >= 0)
				continue;
			existingChoice.setLabel(existingLabel + SEPARATOR + label); 
		}
		existingChoices.sortChoicesByLabel();
	}

	public int size()
	{
		return namedReusableChoices.size();
	}

	public Set getAvailableNames()
	{
		return namedReusableChoices.keySet();
	}

	public ReusableChoices getChoices(String name)
	{
		return (ReusableChoices)namedReusableChoices.get(name);
	}

	public String toXml() throws Exception
	{
		StringBuffer xml = new StringBuffer();
		
		Set reusableChoicesListNames = getAvailableNames();
		Iterator iter = reusableChoicesListNames.iterator();
		while(iter.hasNext())
		{
			String name = (String)iter.next();
			ReusableChoices choices = getChoices(name);
			xml.append(choices.toExportedXml());
		}
		
		return xml.toString();
	}
	
	public static final PoolOfReusableChoicesLists EMPTY_POOL = new PoolOfReusableChoicesLists();

	private Map namedReusableChoices;

}
