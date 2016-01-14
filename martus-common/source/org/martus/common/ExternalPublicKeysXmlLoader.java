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

import java.util.Vector;

import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlMapLoader;
import org.xml.sax.SAXParseException;

abstract public class ExternalPublicKeysXmlLoader extends SimpleXmlDefaultLoader
{
	public ExternalPublicKeysXmlLoader(Vector xmlKeys, String topLevelTag, String singleEntryTag)
	{
		super(topLevelTag);
		singleEntryXmlElementName = singleEntryTag;
		keys = xmlKeys;
	}
	
	public SimpleXmlDefaultLoader startElement(String tag)
		throws SAXParseException
	{
		if(tag.equals(singleEntryXmlElementName))
			return new SimpleXmlMapLoader(tag);
		return super.startElement(tag);
	}

	public void endElement(String tag, SimpleXmlDefaultLoader ended)
		throws SAXParseException
	{
		SimpleXmlMapLoader loader = (SimpleXmlMapLoader)ended;
		String publicCode = loader.get(ExternalPublicKeys.PUBLIC_KEY_TAG);
		String label = loader.get(ExternalPublicKeys.LABEL_TAG);
		ExternalPublicKey key = createKey(publicCode, label);
		keys.add(key);
	}

	abstract ExternalPublicKey createKey(String publicCode, String label);
	
	private Vector keys;
	private String singleEntryXmlElementName;
}