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

package org.martus.common;

import org.martus.common.fieldspec.FieldSpec;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.xml.sax.SAXParseException;

public class XmlCustomFieldsLoader extends SimpleXmlDefaultLoader
{
	public XmlCustomFieldsLoader()
	{
		this(MartusXml.CustomFieldSpecsElementName);
	}

	public XmlCustomFieldsLoader(String tag)
	{
		super(tag);
		fields = new FieldSpecCollection();
	}
	
	public FieldSpecCollection getFieldSpecs()
	{
		return fields;
	}

	public SimpleXmlDefaultLoader startElement(String tag)
		throws SAXParseException
	{
		if(tag.equals(FieldSpec.FIELD_SPEC_XML_TAG))
			return new FieldSpec.XmlFieldSpecLoader(tag);
		if(tag.equals(XmlCustomFieldsLoader.REUSABLE_CHOICES_XML_TAG))
			return new ReusableChoicesXmlLoader(tag);
		return super.startElement(tag);
	}

	public void addText(char[] ch, int start, int length)
		throws SAXParseException
	{
		return;
	}

	public void endElement(String tag, SimpleXmlDefaultLoader ended)
		throws SAXParseException
	{
		if(tag.equals(FieldSpec.FIELD_SPEC_XML_TAG))
		{
			FieldSpec spec = ((FieldSpec.XmlFieldSpecLoader)ended).getFieldSpec();
			fields.add(spec);
		}
		else if(tag.equals(XmlCustomFieldsLoader.REUSABLE_CHOICES_XML_TAG))
		{
			ReusableChoicesXmlLoader loader = (ReusableChoicesXmlLoader)ended;
			fields.addReusableChoiceList(loader.getSetOfChoices());
		}
		else
		{
			throw new RuntimeException("Unexpected end tag: " + tag);
		}
	}

	public static final String REUSABLE_CHOICES_XML_TAG = "ReusableChoices";

	private FieldSpecCollection fields;

}