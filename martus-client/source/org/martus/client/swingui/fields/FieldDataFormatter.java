/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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
package org.martus.client.swingui.fields;

import org.martus.common.EnglishCommonStrings;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;

public class FieldDataFormatter 
{
	public static String formatData(FieldSpec spec, String storedData, MiniLocalization localization)
	{
		String formattedData = storedData;
		FieldType type = spec.getType();
		if(type.isBoolean())
			return formatBooleanValue(storedData, localization);
		else if(type.isDate())
			return localization.convertStoredDateToDisplay(storedData);
		else if(type.isDateRange())
			return localization.getViewableDateRange(storedData);
		else if(type.isDropdown())
			return formatDropdownValue((DropDownFieldSpec)spec, storedData);
		else if(type.isLanguageDropdown())
			return localization.getLanguageName(storedData);
		
		return formattedData;
	}
	
	private static String formatBooleanValue(String storedData, MiniLocalization localization)
	{
		if(storedData.equals(FieldSpec.TRUESTRING))
			return localization.getButtonLabel(EnglishCommonStrings.YES);

		return localization.getButtonLabel(EnglishCommonStrings.NO);
	}
	
	private static String formatDropdownValue(DropDownFieldSpec spec, String storedData)
	{
		int found = spec.findCode(storedData);
		if(found < 0)
			return "";
		
		return spec.getChoice(found).toString();
	}
}
