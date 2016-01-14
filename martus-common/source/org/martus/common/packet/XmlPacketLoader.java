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

import java.util.Vector;

import org.martus.common.MartusXml;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlStringLoader;
import org.xml.sax.SAXParseException;


public class XmlPacketLoader extends SimpleXmlDefaultLoader
{
	public XmlPacketLoader(Packet packetToFill)
	{
		super(packetToFill.getPacketRootElementName());
		packet = packetToFill;
		
		startedAt = System.currentTimeMillis();
	}
	
	public void startDocument()
	{
		packet.setHasUnknownTags(false);
	}
	
	public SimpleXmlDefaultLoader startElement(String tag)
		throws SAXParseException
	{
		String tag1 = tag;
		if(getTagsContainingStrings().contains(tag1))
			return new SimpleXmlStringLoader(tag);

		return super.startElement(tag);
	}

	public void addText(char[] ch, int start, int length)
		throws SAXParseException
	{
	}

	public void endElement(String tag, SimpleXmlDefaultLoader ended)
		throws SAXParseException
	{
		if(!getTagsContainingStrings().contains(tag))
		{
			super.endElement(tag, ended);
			return;
		}

		String value = ((SimpleXmlStringLoader)ended).getText();
		
		try
		{
			if(tag.equals(MartusXml.PacketIdElementName))
				packet.setPacketId(value);
			else if(tag.equals(MartusXml.AccountElementName))
				packet.setAccountId(value);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SAXParseException(e.getMessage(), null);
		}
	}
	
	public void endDocument()
	{
		if(foundUnknownTags())
			packet.setHasUnknownTags(true);
		
		++callsToXmlPacketLoader;
		millisInXmlPacketLoader += (System.currentTimeMillis() - startedAt);
	}
	
	private Vector getTagsContainingStrings()
	{
		if(stringTags == null)
		{
			stringTags = new Vector();
			stringTags.add(MartusXml.PacketIdElementName);
			stringTags.add(MartusXml.AccountElementName);
		}
		
		return stringTags;
	}

	Packet packet;
	long startedAt;
	private static Vector stringTags;

	public static int callsToXmlPacketLoader;
	public static long millisInXmlPacketLoader;
}
