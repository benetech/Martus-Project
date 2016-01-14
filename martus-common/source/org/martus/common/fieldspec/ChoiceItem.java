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

package org.martus.common.fieldspec;

import java.util.Comparator;


public class ChoiceItem implements Comparable
{
	public ChoiceItem(FieldSpec specToUse)
	{
		spec = specToUse;
	}
	
	public ChoiceItem(String codeToUse, String displayToUse)
	{
		this(FieldSpec.createCustomField(codeToUse, displayToUse, new FieldTypeUnknown()));
	}
	
	public String toString()
	{
		return getLabel();
	}

	public String getLabel()
	{
		return spec.getLabel();
	}
	
	public void setLabel(String newLabel)
	{
		spec.setLabel(newLabel);
	}

	public FieldSpec getSpec()
	{
		return spec;
	}

	public String getCode()
	{
		return spec.getTag();
	}
	
	public FieldType getType()
	{
		return spec.getType();
	}
	
	public boolean nestedCodeStartsWith(String partialCode)
	{
		if(getCode().length()==0 || partialCode.length() == 0)
			return false;
		
		if(getCode().equals(partialCode))
			return true;
		
		return getCode().startsWith(partialCode + ".");
	}
	
	public boolean codeIsAtStartOf(String fullCode)
	{
		if(getCode().length()==0 || fullCode.length() == 0)
			return false;
		
		if(fullCode.equals(getCode()))
			return true;
		
		return fullCode.startsWith(getCode() + ".");
	}
	
	public boolean equals(Object other)
	{
		if(!(other instanceof ChoiceItem))
			return false;

		return compareTo(other) == 0;
	}

	public int hashCode()
	{
		return spec.hashCode();
	}

	public int compareTo(Object other)
	{
		if(other == null)
			return 1;
		
		ChoiceItem otherChoiceItem = (ChoiceItem)other;
		return getSpec().compareTo(otherChoiceItem.getSpec());
	}
	
	public static class ChoiceItemSorterByLabel implements Comparator
	{
		public int compare(Object arg0, Object arg1) 
		{
			ChoiceItem a = (ChoiceItem)arg0;
			ChoiceItem b = (ChoiceItem)arg1;
			return a.toString().compareTo(b.toString());
		}
		
	}

	private FieldSpec spec;

}

