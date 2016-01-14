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

package org.martus.common.fieldspec;

import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.utilities.BurmeseUtilities;
import org.martus.util.xml.XmlUtilities;

public class FieldTypeLanguage extends FieldType
{
	public String getTypeName()
	{
		return getTypeNameString();
	}
	
	public boolean isLanguageDropdown()
	{
		return true;
	}
	
	public static String getTypeNameString()
	{
		return getTypeNameString(FIELD_LANGUAGE);
	}
	
	public String getDefaultValue()
	{
		return MiniLocalization.LANGUAGE_OTHER;
	}
	
	@Override
	public String[] convertStoredToHumanReadable(String storedData, PoolOfReusableChoicesLists reusableChoicesLists, MiniLocalization localization)
	{
		return new String[] { getViewableData(storedData, localization) };
	}

	public String convertStoredToSearchable(String storedData, MiniLocalization localization)
	{
		return getViewableData(storedData, localization);
	}
	
	public String convertStoredToHtml(String storedData, MiniLocalization localization)
	{
		String data = getViewableData(storedData, localization);
		
		// NOTE: This is a hack for Burmese. 
		// Language dropdown values are stored in displayable encoding, 
		// but report code expects dropdown values to be in storeable encoding,
		// so if the zawgyi flag is set in the UI, convert to storeable.
		if(localization.getSpecialZawgyiFlagForReportRunner())
			data = BurmeseUtilities.getStorable(data);
		
		return XmlUtilities.getXmlEncoded(data);
	}

	private String getViewableData(String storedData, MiniLocalization localization)
	{
		if(storedData.length() == 0)
			return "";
		
		return localization.getLanguageName(storedData);
	}
}
