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

*/

package org.martus.common.packet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import org.martus.util.StreamableBase64;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.xml.sax.SAXParseException;


public class XmlBase64Exporter extends SimpleXmlDefaultLoader
{
	public XmlBase64Exporter(String tag, OutputStream destination)
	{
		super(tag);
		out = destination;
		
		cache = new StringBuffer();
	}
	
	public void addText(char[] ch, int start, int length)
		throws SAXParseException
	{
		for(int i = start; i < start + length; ++i)
		{
			char curChar = ch[i];
			if(curChar >= ' ')
				cache.append(curChar);
		}

		flush();
	}
	
	public void endDocument() throws SAXParseException
	{
		flush();
		if(cache.length() > 0)
			throw new SAXParseException("Bad Base64 length", null);
	}

	private void flush() throws SAXParseException
	{
		int length = roundDownToMultipleOf4(cache.length());
		String base64Text = cache.substring(0, length);
		cache.delete(0, length);
		try
		{
			StreamableBase64.decode(new StringReader(base64Text), out);
		}
		catch(IOException e)
		{
			throw new SAXParseException("IO Exception: " + e.getMessage(), null);
		} 
		catch (InvalidBase64Exception e)
		{
			throw new SAXParseException("Base64 exception: " + e.getMessage(), null);
		}
	}
	
	private int roundDownToMultipleOf4(int value)
	{
		return (value - value%4);
	}

	OutputStream out;
	StringBuffer cache;
}

