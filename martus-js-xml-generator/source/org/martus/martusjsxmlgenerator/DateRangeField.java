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
package org.martus.martusjsxmlgenerator;

import org.martus.common.bulletin.BulletinXmlExportImportConstants;
import org.mozilla.javascript.Scriptable;

public class DateRangeField extends DateField 
{
	private static final String DATE_RANGE_DELIMETER = "_";

	public DateRangeField()
	{
	}

	public DateRangeField(String tagToUse, String labelToUse, Object valueToUse, Object dateFormatToUse, boolean isBottomSectionFieldToUse)
	{
		super(tagToUse, labelToUse, valueToUse, dateFormatToUse, isBottomSectionFieldToUse);
	}
	
//	public DateRangeField(String tagToUse, String labelToUse, String startDateToUse, String endDateToUse, String dateFormatToUse)
	//{
		//super(tagToUse, labelToUse, startDateToUse+endDateToUse, dateFormatToUse);
	//}

	
	
	public String getMartusValue( Scriptable scriptable ) throws Exception
	{
		String dateInformation = super.getMartusValue(scriptable);
		String[] dateRangeInfo = dateInformation.split(DATE_RANGE_DELIMETER);
		if(dateRangeInfo.length !=2)
		{
			System.out.println("DateRange Incorrect must be in the form: beginDate_endDate but ="+ dateInformation);
			return "ERROR: Date range incorrect:"+ dateInformation;
		}
		String martusStartDate = getMartusDate(dateRangeInfo[0]);
		String martusEndDate = getMartusDate(dateRangeInfo[1]);
		
		return BulletinXmlExportImportConstants.DATE_RANGE + martusStartDate + "," + martusEndDate;
	}

	public String getType() 
	{
		return DATERANGE_TYPE;
	}

	//Actual Name called by the JavaScript
	public String getClassName() 
	{
		return "DateRangeField";
	}
}
