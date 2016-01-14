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

import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.xml.sax.SAXParseException;

public class XmlXFormsLoader extends SimpleXmlDefaultLoader
{
	public XmlXFormsLoader()
	{
		super(MartusXml.XFormsElementName);
	}
	
	@Override
	public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
	{
		if (tag.equals(MartusXml.XFormsModelElementName))
			return new XmlBlockLoader(tag);

		if (tag.equals(MartusXml.XFormsInstanceElementName))
			return new XmlBlockLoader(tag);
		
		return super.startElement(tag);
	}
	
	@Override
	public void endElement(String tag, SimpleXmlDefaultLoader ended) throws SAXParseException
	{
		if (tag.equals(MartusXml.XFormsModelElementName)) 
		{
			XmlBlockLoader xformsModelLoader = (XmlBlockLoader) ended;
			xFormsModelAsString = xformsModelLoader.getLoadedValue();
		}
		
		if (tag.equals(MartusXml.XFormsInstanceElementName))
		{
			XmlBlockLoader xformsInstanceLoader = (XmlBlockLoader) ended;
			xFormsInstanceAsString =  xformsInstanceLoader.getLoadedValue();
		}

		super.endElement(tag, ended);
	}
	
	public String getXFormsModelAsString()
	{
		return xFormsModelAsString;
	}
	
	public String getXFormsInstanceAsString()
	{
		return xFormsInstanceAsString;
	}

	private String xFormsModelAsString;
	private String xFormsInstanceAsString;
}
