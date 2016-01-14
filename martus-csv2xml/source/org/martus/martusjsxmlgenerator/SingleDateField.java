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

public class SingleDateField extends DateField 
{
	public SingleDateField()
	{
	}

	public SingleDateField(String tagToUse, String labelToUse, Object valueToUse, Object dateFormatToUse, boolean isBottomSectionFieldToUse)
	{
		super(tagToUse, labelToUse, valueToUse, dateFormatToUse, isBottomSectionFieldToUse);
	}
	
	public String getType() 
	{
		return DATE_TYPE;
	}
	
	public String getMartusValue( Scriptable scriptable ) throws Exception
	{
		String rawDate = super.getMartusValue(scriptable);
		String martusDate = getMartusDate(rawDate);
		return BulletinXmlExportImportConstants.DATE_SIMPLE + martusDate;
	}

	//Actual Name called by the JavaScript
	public String getClassName() 
	{
		return "SingleDateField";
	}
}
