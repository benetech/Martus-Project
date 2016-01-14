/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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
package org.martus.client.swingui.fields;

import org.martus.common.ListOfReusableChoicesLists;
import org.martus.common.MiniLocalization;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;

abstract public class UiChoice extends UiField
{
	public UiChoice(MiniLocalization localizationToUse)
	{
		super(localizationToUse);
	}
	
	public void setSpec(UiFieldContext context, DropDownFieldSpec specToUse)
	{
		spec = specToUse;
		
		ListOfReusableChoicesLists newChoices = context.getCurrentDropdownChoices(spec);
		setChoices(newChoices);
	}
	
	public void setChoices(ChoiceItem[] newChoices)
	{
		ReusableChoices choices = new ReusableChoices("", "");
		choices.addAll(newChoices);
		setChoices(new ListOfReusableChoicesLists(choices));
	}

	abstract public void setChoices(ListOfReusableChoicesLists newChoices);
	
	public boolean isLanguageDropdown()
	{
		return spec.getTag().equals(BulletinConstants.TAGLANGUAGE);
	}
	
	private DropDownFieldSpec spec;
}
