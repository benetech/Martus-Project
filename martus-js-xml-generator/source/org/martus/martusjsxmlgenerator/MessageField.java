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
package org.martus.martusjsxmlgenerator;

import org.martus.common.fieldspec.MessageFieldSpec;
import org.mozilla.javascript.Scriptable;


public class MessageField extends MartusField
{
	public MessageField()
	{
		super();
	}
	
	public MessageField(String tagToUse, String labelToUse, Object valueToUse, boolean isBottomSectionFieldToUse)
	{
		super(tagToUse, labelToUse, valueToUse, isBottomSectionFieldToUse);
	}

	public String getType() 
	{
		return MESSAGE_TYPE;
	}
	
	//Actual Name called by the JavaScript
	public String getClassName() 
	{
		return "MessageField";
	}
	
	public String getFieldSpecSpecificXmlData(Scriptable scriptable) throws Exception 
	{
		String message = super.getMartusValue(scriptable);
		return getXMLData(MessageFieldSpec.MESSAGE_SPEC_MESSAGE_TAG, message);
	}
	
	public String getXmlFieldValue(Scriptable scriptable) throws Exception
	{
		return "";//Value not needed since message is in the field spec.
	}
}
