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
import org.martus.common.bulletin.BulletinXmlExportImportConstants;
import org.martus.common.utilities.DateUtilities;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.xml.XmlUtilities;

public class FieldTypeDateRange extends FieldType
{
	public String getTypeName()
	{
		return getTypeNameString();
	}
	
	public boolean isDateRange()
	{
		return true;
	}
	
	public static String getTypeNameString()
	{
		return getTypeNameString(FIELD_DATERANGE);
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
		return XmlUtilities.getXmlEncoded(getViewableData(storedData, localization));
	}

	private String getViewableData(String storedData, MiniLocalization localization)
	{
		return localization.getViewableDateRange(storedData);
	}

	public String convertStoredToExportable(String storedData, MiniLocalization localization)
	{
		String startDate = DateUtilities.getStartDateRange(storedData, localization);
		String endDate = MartusFlexidate.toStoredDateFormat(localization.createFlexidateFromStoredData(storedData).getEndDate());
		return BulletinXmlExportImportConstants.DATE_RANGE + startDate + "," + endDate;
	}

	public FieldSpec createEmptyFieldSpec()
	{
		return new DateRangeFieldSpec();
	}
}
