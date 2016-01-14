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

import java.util.Vector;

import org.xml.sax.SAXParseException;


public class SimpleXmlVectorLoader extends SimpleXmlDefaultLoader
{
	public SimpleXmlVectorLoader(String mainTag, String entryTag)
	{
		super(mainTag);
		this.entryTag = entryTag;
		data = new Vector();
	}
	
	public Vector getVector()
	{
		return data;
	}
	
	public String get(int index)
	{
		return (String)data.get(index);
	}
	
	public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
	{
		if(tag.equals(entryTag))
			return new SimpleXmlStringLoader(tag);
		throw new SAXParseException("Expected start tag: " + entryTag, null);
	}
	
	public void endElement(String tag, SimpleXmlDefaultLoader endingHandler)
	{
		if(tag.equals(entryTag))
		{
			SimpleXmlStringLoader pendingFieldData = (SimpleXmlStringLoader)endingHandler;
			data.add(pendingFieldData.getText());
		}
	}

	String entryTag;
	Vector data;
}
