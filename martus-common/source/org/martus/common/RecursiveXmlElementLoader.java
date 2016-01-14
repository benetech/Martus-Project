/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2015, Beneficent
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

import java.util.Map;
import java.util.Set;

import org.martus.util.xml.AttributesOnlyXmlLoader;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.xml.sax.SAXParseException;

public class RecursiveXmlElementLoader extends AttributesOnlyXmlLoader
{
	public RecursiveXmlElementLoader(String tag)
	{
		super(tag);
		
		elementValue = new StringBuilder();
	}
	
	@Override
	public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
	{
		return new RecursiveXmlElementLoader(tag);
	}
	
	@Override
	public void endElement(String tag, SimpleXmlDefaultLoader ended) throws SAXParseException
	{
		RecursiveXmlElementLoader loader = (RecursiveXmlElementLoader)ended;
		elementValue.append(loader.getBuiltElement());
		
		super.endElement(tag, ended);
	}
	
	@Override
	public void addText(char[] ch, int start, int length) throws SAXParseException
	{
		elementValue.append(ch, start, length);
	}
	
	public String getBuiltElement()
	{
		return createStartElement() + elementValue.toString() + createEndElement();
	}

	private String createStartElement()
	{
		StringBuilder startElementBuilder = new StringBuilder();
		startElementBuilder.append("<");
		startElementBuilder.append(getTag());
		startElementBuilder.append(createAttributesString());
		startElementBuilder.append(">");
		
		return startElementBuilder.toString();
	}

	private String createAttributesString()
	{
		StringBuilder attributesBuilder = new StringBuilder();
		Map elementAttributesKeyToValueMap = getAttributes();
		if (elementAttributesKeyToValueMap.isEmpty())
			return "";
		
		attributesBuilder.append(SINGLE_SPACE);	
		Set<String> attributeKeys = elementAttributesKeyToValueMap.keySet();
		for (String attributeKey : attributeKeys)
		{
			attributesBuilder.append(attributeKey);
			attributesBuilder.append("=");
			String attributeValue = elementAttributesKeyToValueMap.get(attributeKey).toString();
			attributesBuilder.append("\"" + attributeValue + "\"");
			attributesBuilder.append(SINGLE_SPACE);
		}
		
		return attributesBuilder.toString();
	}

	private String createEndElement()
	{
		return "</" + getTag() + ">";
	}
	
	private StringBuilder elementValue;
	
	private static final String SINGLE_SPACE = " ";
}
