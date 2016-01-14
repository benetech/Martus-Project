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

import java.util.Map;
import java.util.Vector;

import org.martus.util.TestCaseEnhanced;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;


public class TestSimpleXmlParser extends TestCaseEnhanced
{
	public TestSimpleXmlParser(String name)
	{
		super(name);
	}
	
	public void testEmptyXml() throws Exception
	{
		try
		{
			SimpleXmlDefaultLoader loader = new SimpleXmlStringLoader("ExpectedRootTag");
			String emptyXmlString = "";
			SimpleXmlParser.parse(loader, emptyXmlString);
			fail("Should have thrown!");
		}
		catch(SAXParseException ignoreExpected)
		{
		}
	}
	
	public void testStringLoader() throws Exception
	{
		SimpleXmlStringLoader loader = new SimpleXmlStringLoader("name");
		SimpleXmlParser.parse(loader, "<name>testing</name>");
		assertEquals("testing", loader.getText());
		assertFalse("unknown flag set?", loader.foundUnknownTags());
	}

	public void testMapLoader() throws Exception
	{
		SimpleXmlMapLoader loader = new SimpleXmlMapLoader("SampleMap");
		SimpleXmlParser.parse(loader, "<SampleMap><key>value</key></SampleMap>");
		assertEquals("value", loader.getMap().get("key"));
	}

	public void testSimpleNesting() throws Exception
	{
		SimpleXmlMapLoader loader = new SimpleXmlMapLoader("object");
		SimpleXmlParser.parse(loader, "<object><a>text</a><big>yes</big><object>unrelated</object></object>");
		Map data = loader.getMap();
		assertEquals(3, data.size());
		assertEquals("text", data.get("a"));
		assertEquals("yes", data.get("big"));
		assertEquals("unrelated", data.get("object"));
	}
	
	public void testErrorHandling() throws Exception
	{
		CustomFieldsLoader loader = new CustomFieldsLoader(null, "fields");
		try
		{
			SimpleXmlParser.parse(loader, "<notfields></notfields>");
			fail("Should have thrown for wrong root tag");
		}
		catch(SAXParseException ignoreExpected)
		{
		}
		
		SimpleXmlParser.parse(loader, "<fields><IgnoreThis></IgnoreThis></fields>");
		assertTrue("unknown flag not set?", loader.foundUnknownTags());

		loader.throwOnUnexpectedTags();
		try
		{
			SimpleXmlParser.parse(loader, "<fields><ShouldThrow></ShouldThrow></fields>");
			fail("Should have thrown for unexpected tag");
		}
		catch(SAXParseException ignoreExpected)
		{
		}
	}
	
	public void testUnexpectedText() throws Exception
	{
		CustomFieldsLoader loader = new CustomFieldsLoader(null, "fields");
		SimpleXmlParser.parse(loader, "<fields>should be ignored</fields>");
	}

	public void testFullSample() throws Exception
	{
		CustomFields fields = new CustomFields();
		CustomFieldsLoader loader = new CustomFieldsLoader(fields, "fields");
		SimpleXmlParser.parse(loader, 
			"<fields>" + 
				"<style>bogus</style>" +
				"<field>" +
					"<tag>oh</tag><label>boo</label><type>hoo</type>" +
				"</field>" + 
				"<field>" + 
					"<tag>tweedle</tag><label>dee</label><type>tweedle</type><choices>dum</choices>" + 
				"</field>" + 
			"</fields>");

		assertEquals(2, fields.size());
		assertEquals("bogus", fields.getStyle());

		Map firstField = fields.getField(0);
		assertEquals(3, firstField.size());
		assertEquals("hoo", firstField.get("type"));

		Map secondField = fields.getField(1); 
		assertEquals(4, secondField.size());
		assertEquals("tweedle", secondField.get("tag"));
		assertEquals("tweedle", secondField.get("type"));
		assertEquals("dum", secondField.get("choices")); 
	}
	
	public void testAttributes() throws Exception
	{
		class AttributeLoader extends SimpleXmlDefaultLoader
		{
			public AttributeLoader()
			{
				super("tag");
			}
			
			public String getX()
			{
				return x;
			}
			
			public void startDocument(Attributes attrs) throws SAXParseException
			{
				x = attrs.getValue("x");
			}

			String x;
		}
		
		AttributeLoader loader = new AttributeLoader();
		SimpleXmlParser.parse(loader, "<tag x='y'></tag>");
		assertEquals("y", loader.getX());
	}
	
	class CustomFields
	{
		CustomFields()
		{
			fields = new Vector();
		}
		
		int size()
		{
			return fields.size();
		}
		
		void setStyle(String style)
		{
			this.style = style;
		}

		String getStyle()
		{
			return style;
		}
		
		void addField(Map fieldData)
		{
			fields.add(fieldData);
		}
		
		Map getField(int index)
		{
			return (Map)fields.get(index);
		}

		private String style;
		private Vector fields;
	}

	class CustomFieldsLoader extends SimpleXmlDefaultLoader
	{
		CustomFieldsLoader(CustomFields customFieldSpecToFill, String tag)
		{
			super(tag);
			customFields = customFieldSpecToFill;
		}
		
		public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
		{
			if(tag.equals(styleTag))
			{
				return new SimpleXmlStringLoader(tag); 
			}
			else if(tag.equals(fieldTag))
			{
				return new SimpleXmlMapLoader(tag);
			}
			else
				return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended) throws SAXParseException
		{
			if(tag.equals(styleTag))
			{
				SimpleXmlStringLoader styleHandler = (SimpleXmlStringLoader)ended;
				customFields.setStyle(styleHandler.getText());
				styleHandler = null;
			}
			else if(tag.equals(fieldTag))
			{
				SimpleXmlMapLoader mapHandler = (SimpleXmlMapLoader)ended; 
				customFields.addField(mapHandler.getMap());
				mapHandler = null;
			}
			else
				super.endElement(tag, ended);
		}
		
		CustomFields customFields;

		public static final String styleTag = "style";
		public static final String fieldTag = "field";
	}

}
