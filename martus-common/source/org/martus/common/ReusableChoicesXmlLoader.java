/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2010, Beneficent
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
package org.martus.common;

import org.martus.common.fieldspec.ChoiceItem;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

public class ReusableChoicesXmlLoader extends SimpleXmlDefaultLoader
{
	public ReusableChoicesXmlLoader(String tag)
	{
		super(tag);
	}

	public void startDocument(Attributes attrs) throws SAXParseException
	{
		String code = attrs.getValue(ATTRIBUTE_REUSABLE_CHOICES_CODE);
		String label = attrs.getValue(ATTRIBUTE_REUSABLE_CHOICES_LABEL);
		setOfChoices = new ReusableChoices(code, label);
		super.startDocument(attrs);
	}
	
	public SimpleXmlDefaultLoader startElement(String tag)
			throws SAXParseException
	{
		if(tag.equals(TAG_CHOICE))
			return new ChoiceItemXmlLoader(tag);

		return super.startElement(tag);
	}
	
	public void endElement(String tag, SimpleXmlDefaultLoader ended)
			throws SAXParseException
	{
		if(tag.equals(TAG_CHOICE))
		{
			ChoiceItemXmlLoader loader = (ChoiceItemXmlLoader)ended;
			String itemCode = loader.getCode();
			String itemLabel = loader.getLabel();
			ChoiceItem choice = new ChoiceItem(itemCode, itemLabel);
			setOfChoices.add(choice);
		}
		super.endElement(tag, ended);
	}

	public ReusableChoices getSetOfChoices()
	{
		return setOfChoices;
	}
	
	private static String ATTRIBUTE_REUSABLE_CHOICES_CODE = "code";
	private static String ATTRIBUTE_REUSABLE_CHOICES_LABEL = "label";
	private static String TAG_CHOICE = "Choice";

	private ReusableChoices setOfChoices;

}
