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

import java.util.Comparator;

import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;

class ChoiceItemSorterByLabelTagType implements Comparator
{
	public ChoiceItemSorterByLabelTagType(MiniLocalization localization)
	{
		collator = new SaneCollator(localization.getCurrentLanguageCode());
	}
	
	public int compare(Object o1, Object o2)
	{
		ChoiceItem choice1 = (ChoiceItem)o1;
		ChoiceItem choice2 = (ChoiceItem)o2;
		return compare(choice1.getSpec(), choice2.getSpec());
	}
	
	public int compare(FieldSpec spec1, FieldSpec spec2)
	{
		int anyFieldResult = compareAnyFieldNess(spec1, spec2);
		if(anyFieldResult != 0)
			return anyFieldResult;

		int labelResult = collator.compare(spec1.getLabel(), spec2.getLabel());
		if(labelResult != 0)
			return labelResult;
		
		int tagResult = spec1.getTag().compareTo(spec2.getTag());
		if(tagResult != 0)
			return tagResult;
		
		int typeResult = spec1.getType().getTypeName().compareTo(spec2.getType().getTypeName());
		if(typeResult != 0)
			return typeResult;
		
		return 0;
	}

	private int compareAnyFieldNess(FieldSpec spec1, FieldSpec spec2)
	{
		int anyFieldResult = 0;
		if(isAnyField(spec1) && !(isAnyField(spec2)))
			anyFieldResult = -1;
		if(isAnyField(spec2) && !(isAnyField(spec1)))
			anyFieldResult = 1;
		return anyFieldResult;
	}

	private boolean isAnyField(FieldSpec spec1)
	{
		return spec1.getTag().length() == 0;
	}
	
	SaneCollator collator;
}