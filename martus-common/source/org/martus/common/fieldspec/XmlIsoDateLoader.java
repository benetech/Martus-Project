/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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
/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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

package org.martus.common.fieldspec;

import org.martus.util.MultiCalendar;
import org.martus.util.xml.SimpleXmlStringLoader;

public class XmlIsoDateLoader extends SimpleXmlStringLoader
{
	public XmlIsoDateLoader(String thisTag, String fieldTagToUse, String fieldLabelToUse, String fieldTypeToUse)
	{
		super(thisTag);
		fieldTag = fieldTagToUse;
		fieldLabel = fieldLabelToUse;
		fieldType = fieldTypeToUse;
	}
	
	String getDateAsIsoString() throws InvalidIsoDateException
	{
		String text = getText();
		if(text.length() == 0)
			return text;
		if(text.length() != 10)
			throw new InvalidIsoDateException(getFieldTag(), getFieldLabel(), getFieldType());

		try
		{
			MultiCalendar.createFromIsoDateString(text);
			return text;
		}
		catch(Exception e)
		{
			throw new InvalidIsoDateException(getFieldTag(), getFieldLabel(), getFieldType());
		}
	}

	public String getFieldTag()
	{
		return fieldTag;
	}


	public String getFieldLabel()
	{
		return fieldLabel;
	}

	public String getFieldType()
	{
		return fieldType;
	}
	
	private String fieldTag;
	private String fieldLabel;
	private String fieldType;
}
