/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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

To the extent this copyrighted software code is used in the 
Miradi project, it is subject to a royalty-free license to 
members of the Conservation Measures Partnership when 
used with the Miradi software as specified in the agreement 
between Benetech and WCS dated 5/1/05.
*/

package org.martus.util.xml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class SimpleXmlParser extends DefaultHandler
{
	public static void parse(SimpleXmlDefaultLoader loaderToUse, String xmlText) throws 
		IOException, ParserConfigurationException, SAXException
	{
		parse(loaderToUse, new StringReader(xmlText));
	}
	
	public static void parse(SimpleXmlDefaultLoader loaderToUse, Reader xmlReader) throws 
		IOException, ParserConfigurationException, SAXException
	{
		new SimpleXmlParser(loaderToUse, xmlReader);
	}
	
	private SimpleXmlParser(SimpleXmlDefaultLoader loaderToUse, Reader xmlReader) throws 
		IOException, ParserConfigurationException, SAXException
	{
		loaders = new Vector();
		insertNewCurrentLoader(loaderToUse);
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(new InputSource(xmlReader), this);
	}
	
	public void startDocument() throws SAXParseException
	{
	}
	
	public void startElement(
		String uri,
		String localName,
		String qName,
		Attributes attributes)
		throws SAXException
	{
		if(!gotFirstTag)
		{
			String expectedTag = currentLoader.getTag();
			if(!qName.equals(expectedTag))
				throw new SAXParseException("SimpleXmlParser expected root: " + expectedTag, null);
			gotFirstTag = true;
			currentLoader.startDocument(attributes);
			return;
		}
		SimpleXmlDefaultLoader newLoader = currentLoader.startElement(qName);
		insertNewCurrentLoader(newLoader);
		currentLoader.startDocument(attributes);
	}

	public void characters(char[] ch, int start, int length)
		throws SAXException
	{
		currentLoader.addText(ch, start, length);
	}

	public void endElement(String uri, String localName, String qName)
		throws SAXException
	{
		if(loaders.size() > 1)
		{
			SimpleXmlDefaultLoader endingLoader = currentLoader;
			endingLoader.endDocument();

			removeCurrentLoader();
			currentLoader.endElement(endingLoader.getTag(), endingLoader);
		}
	}

	public void endDocument() throws SAXException
	{
		currentLoader.endDocument();
	}


	private void insertNewCurrentLoader(SimpleXmlDefaultLoader newLoader)
	{
		loaders.insertElementAt(newLoader, 0);
		currentLoader = newLoader;
	}

	private void removeCurrentLoader()
	{
		loaders.remove(0);
		if(loaders.size() == 0)
			currentLoader = null;
		else
			currentLoader = (SimpleXmlDefaultLoader)loaders.get(0);
	}

	boolean gotFirstTag;
	Vector loaders;
	SimpleXmlDefaultLoader currentLoader;

	private static SAXParserFactory factory = SAXParserFactory.newInstance();
}
