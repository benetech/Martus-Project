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
package org.martus.client.reports;

import java.util.Vector;

import org.martus.common.ReusableChoices;
import org.martus.common.fieldspec.ChoiceItem;

public class SummaryCount
{
	public SummaryCount(StringVector labelsToUse)
	{
		otherLabels = new StringVector(labelsToUse);
		label = "";
		value = new ChoiceItem("", "");
		children = new Vector();
	}
	
	public SummaryCount(StringVector labelsToUse, ChoiceItem valueToUse)
	{
		otherLabels = new StringVector(labelsToUse);
		label = otherLabels.remove(0);
		value = valueToUse;
		children = new Vector();
	}
	
	public String label()
	{
		return label;
	}
	
	public String getCode()
	{
		return value.getCode();
	}
	
	public String value()
	{
		return value.toString();
	}
	
	public int count()
	{
		return count;
	}
	
	public int getChildCount()
	{
		return children.size();
	}
	
	public SummaryCount getChild(int index)
	{
		return (SummaryCount)children.get(index);
	}
	
	public Vector children()
	{
		return children;
	}
	
	public void increment(ReusableChoices values)
	{
		if(values.size() > 0)
		{
			ChoiceItem thisValue = values.remove(0);
			SummaryCount sc = findOrCreateChild(thisValue);
			sc.increment(values);
		}
		++count;
	}

	private SummaryCount findOrCreateChild(ChoiceItem thisChoice)
	{
		int scIndex = findByCode(thisChoice.getCode());
		if(scIndex >= 0)
			return getChild(scIndex);
		
		SummaryCount sc = new SummaryCount(otherLabels, thisChoice);
		children.add(sc);
		return sc;
	}
	
	private int findByCode(String codeToFind)
	{
		for(int i = 0; i < getChildCount(); ++i)
		{
			SummaryCount sc = getChild(i);
			if(sc.getCode().equals(codeToFind))
				return i;
		}
		
		return -1;
	}
	
	public String toString()
	{
		StringBuffer result = new StringBuffer();
		result.append("Label: " + label + ", Value: " + value + ", Count: " + count + "\n");
		for(int i = 0; i < getChildCount(); ++i)
			result.append(getChild(i).toString());
		return result.toString();
	}
	
	String label;
	ChoiceItem value;
	int count;
	StringVector otherLabels;
	Vector children;
}
