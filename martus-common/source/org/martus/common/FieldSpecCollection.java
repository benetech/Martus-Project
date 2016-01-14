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
package org.martus.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.util.xml.XmlUtilities;

public class FieldSpecCollection implements Comparable
{
	public FieldSpecCollection(FieldSpec[] specsToUse)
	{
		specs = new Vector(Arrays.asList(specsToUse));
		reusableChoicesPool = new PoolOfReusableChoicesLists();
	}
	
	public FieldSpecCollection()
	{
		this(new FieldSpec[0]);
	}
	
	public int size()
	{
		return specs.size();
	}
	
	public void add(FieldSpec spec)
	{
		specs.add(spec);
	}
	
	public void addAll(FieldSpecCollection fieldSpecCollectionToAdd)
	{
		for (int index = 0; index < fieldSpecCollectionToAdd.size(); ++index)
		{
			add(fieldSpecCollectionToAdd.get(index));
		}
	}
	
	public FieldSpec get(int index)
	{
		return (FieldSpec) specs.get(index);
	}
	
	public Set asSet() 
	{
		return new HashSet(specs);
	}
	
	public FieldSpec findBytag(String tagToFind)
	{
		for(int index = 0; index < size(); ++index)
		{
			FieldSpec thisSpec = get(index);
			if(thisSpec.getTag().equals(tagToFind))
				return thisSpec;
		}
		return null;
	}

	public void addReusableChoiceList(ReusableChoices setOfChoices)
	{
		reusableChoicesPool.add(setOfChoices);
	}

	public Set getReusableChoiceNames()
	{
		return reusableChoicesPool.getAvailableNames();
	}

	public ReusableChoices getReusableChoices(String name)
	{
		return reusableChoicesPool.getChoices(name);
	}

	public PoolOfReusableChoicesLists getAllReusableChoiceLists()
	{
		return reusableChoicesPool;
	}

	public FieldSpec[] asArray()
	{
		return (FieldSpec[]) specs.toArray(new FieldSpec[0]);
	}
	
	public void addAllSpecs(Set allKnownFieldSpecs)
	{
		Iterator iter = allKnownFieldSpecs.iterator();
		while(iter.hasNext())
		{
			FieldSpec spec = (FieldSpec)iter.next();
			add(spec);
		}
	}

	public void addAllReusableChoicesLists(PoolOfReusableChoicesLists allReusableChoiceLists)
	{
		Set names = allReusableChoiceLists.getAvailableNames();
		Iterator iter = names.iterator();
		while(iter.hasNext())
		{
			String name = (String) iter.next();
			ReusableChoices reusableChoices = allReusableChoiceLists.getChoices(name);
			reusableChoicesPool.add(reusableChoices);
		}
	}

	public String toXml()
	{
		StringBuffer result = new StringBuffer();
		result.append('<');
		result.append(MartusXml.CustomFieldSpecsElementName);
		result.append(">\n\n");
		
		for (int i = 0; i < specs.size(); i++)
		{
			FieldSpec spec = get(i);
			result.append(spec.toString());
			result.append('\n');
		}
		
		Vector reusableChoiceListNames = new Vector(reusableChoicesPool.getAvailableNames());
		Collections.sort(reusableChoiceListNames);
		Iterator it = reusableChoiceListNames.iterator();
		while(it.hasNext())
		{
			String name = (String)it.next();
			ReusableChoices choiceList = reusableChoicesPool.getChoices(name);
			result.append("<ReusableChoices code='" + XmlUtilities.getXmlEncoded(choiceList.getCode()) + "' label='" + XmlUtilities.getXmlEncoded(choiceList.getLabel()) + "'>");
			result.append('\n');
			for(int i = 0; i < choiceList.size(); ++i)
			{
				ChoiceItem choice = choiceList.get(i);
				String codeAttribute = "";
				String code = choice.getCode();
				if(code != null)
					codeAttribute = "code='" + XmlUtilities.getXmlEncoded(code) + "'";
				result.append("<Choice " + codeAttribute + " label='" + XmlUtilities.getXmlEncoded(choice.toString()) + "'></Choice>");
				result.append('\n');
			}
			result.append("</ReusableChoices>");
			result.append("\n\n");
		}
		result.append("</");
		result.append(MartusXml.CustomFieldSpecsElementName);
		result.append(">\n");
		return result.toString();
	}

	public int hashCode() 
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + FieldSpecCollection.hashCode(specs.toArray());
		return result;
	}

	private static int hashCode(Object[] array) 
	{
		final int PRIME = 31;
		if (array == null)
			return 0;
		int result = 1;
		for (int index = 0; index < array.length; index++) {
			result = PRIME * result + (array[index] == null ? 0 : array[index].hashCode());
		}
		return result;
	}

	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FieldSpecCollection other = (FieldSpecCollection) obj;
		return (compareTo(other) == 0);
	}

	public int compareTo(Object rawOther) 
	{
		if(!(rawOther instanceof FieldSpecCollection))
			return 0;
		FieldSpecCollection other = (FieldSpecCollection)rawOther;
		String thisXml = toXml();
		String otherXml = other.toXml();
		return thisXml.compareTo(otherXml);
	}
	
	private Vector specs;
	private PoolOfReusableChoicesLists reusableChoicesPool;
}
