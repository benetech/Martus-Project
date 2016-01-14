/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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

import org.martus.common.fieldspec.FormTemplate;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlStringLoader;
import org.xml.sax.SAXParseException;

public class XmlFormTemplateLoader extends SimpleXmlDefaultLoader
{
	public XmlFormTemplateLoader()
	{
		super(MartusXml.FormTemplateElementName);

		formTemplate = new FormTemplate();
		addEmptyBottomToAvoidChangingPaseCode();
	}

	private void addEmptyBottomToAvoidChangingPaseCode()
	{
		formTemplate.setBottomFields(new FieldSpecCollection());
	}
	
	@Override
	public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
	{
		if(tag.equals(MartusXml.TitleElementName))
			return new SimpleXmlStringLoader(tag);

		if(tag.equals(MartusXml.CustomFieldSpecsElementName))
			return new XmlCustomFieldsLoader(tag);
		
		return super.startElement(tag);
	}

	@Override
	public void addText(char[] ch, int start, int length) throws SAXParseException
	{
		return;
	}

	@Override
	public void endElement(String tag, SimpleXmlDefaultLoader ended) throws SAXParseException
	{
		if(tag.equals(MartusXml.TitleElementName))
		{
			String title = ((SimpleXmlStringLoader)ended).getText();
			formTemplate.setTitle(title.toString());
		}
		else if(tag.equals(MartusXml.CustomFieldSpecsElementName))
		{
			
			XmlCustomFieldsLoader loader = (XmlCustomFieldsLoader)ended;
			FieldSpecCollection fieldSpecs = loader.getFieldSpecs();
			formTemplate.setTopFields(fieldSpecs);
		}
		else
		{
			throw new RuntimeException("Unexpected end tag: " + tag);
		}
	}
	
	public FormTemplate getFormTemplate()
	{
		return formTemplate;
	}

	private FormTemplate formTemplate;
}
