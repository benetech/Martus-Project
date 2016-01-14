package org.martus.common;
/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2013, Beneficent
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


import java.util.Iterator;
import java.util.Vector;

import org.martus.util.xml.SimpleXmlParser;
import org.martus.util.xml.XmlUtilities;

abstract public class ExternalPublicKeys
{
	public ExternalPublicKeys()
	{
		externalPublicKeys = new Vector();
	}
	
	public ExternalPublicKeys(Vector keysToUse)
	{
		externalPublicKeys = keysToUse;
	}
	
	public ExternalPublicKeys(ExternalPublicKey key) 
	{
		externalPublicKeys = new Vector();
		rawAdd(key);
	}
	
	public ExternalPublicKeys(ExternalPublicKeys keys) 
	{
		externalPublicKeys = new Vector();
		rawAdd(keys);
	}
	

	public ExternalPublicKeys(String xml) throws Exception
	{
		externalPublicKeys = parseXml(xml);	
	}
	
	public boolean isEmpty()
	{
		return externalPublicKeys.isEmpty();
	}
	
	public int size()
	{
		return externalPublicKeys.size();
	}
	
	public void rawAdd(ExternalPublicKey keyToAdd)
	{
		externalPublicKeys.add(keyToAdd);
	}
	
	public void rawAdd(ExternalPublicKeys keysToAdd)
	{
		for(int i = 0; i < keysToAdd.size(); ++i)
		{
			rawAdd(keysToAdd.rawGet(i));
		}
	}
	
	public void remove(int index)
	{
		externalPublicKeys.remove(index);
	}
	
	public void clear()
	{
		externalPublicKeys.clear();
	}
	
	public ExternalPublicKey rawGet(int index)
	{
		return (ExternalPublicKey)externalPublicKeys.get(index);
	}
	
	public String toString()
	{
		return getXMLRepresntation(DONT_INCLUDE_LABEL);
	}

	public String toStringWithLabel()
	{
		return getXMLRepresntation(INCLUDE_LABEL);
	}
	
	private String getXMLRepresntation(boolean includeLabel)
	{
		String xmlRepresentation = MartusXml.getTagStartWithNewline(getTopLevelXmlElementName());
		for(int i = 0; i < externalPublicKeys.size(); ++i)
		{
			xmlRepresentation += MartusXml.getTagStart(getSingleEntryXmlElementName());
			xmlRepresentation += MartusXml.getTagStart(PUBLIC_KEY_TAG);
			xmlRepresentation += rawGet(i).getPublicKey();
			xmlRepresentation += MartusXml.getTagEndWithoutNewline(PUBLIC_KEY_TAG);
			if(includeLabel)
			{
				xmlRepresentation += MartusXml.getTagStart(LABEL_TAG);
				xmlRepresentation += XmlUtilities.getXmlEncoded(((ExternalPublicKey)externalPublicKeys.get(i)).getLabel());
				xmlRepresentation += MartusXml.getTagEndWithoutNewline(LABEL_TAG);
			}
			xmlRepresentation += MartusXml.getTagEnd(getSingleEntryXmlElementName());
		}
		xmlRepresentation += MartusXml.getTagEnd(getTopLevelXmlElementName());
		
		return xmlRepresentation;
	}

	abstract String getTopLevelXmlElementName();
	abstract String getSingleEntryXmlElementName();

	public boolean containsKey(String publicKey)
	{
		for (Iterator iter = externalPublicKeys.iterator(); iter.hasNext();)
		{
			ExternalPublicKey key = (ExternalPublicKey) iter.next();
			if(key.getPublicKey().equals(publicKey))
				return true;
		}
		return false;
	}

	public String getLabelIfPresent(ExternalPublicKey searchForKey)
	{
		return getLabelIfPresent(searchForKey.getPublicKey());
	}

	public String getLabelIfPresent(String publicKey)
	{
		for (Iterator iter = externalPublicKeys.iterator(); iter.hasNext();)
		{
			ExternalPublicKey key = (ExternalPublicKey) iter.next();
			if(key.getPublicKey().equals(publicKey))
				return key.getLabel();
		}
		return "";
	}
	
	public boolean contains(ExternalPublicKey key)
	{
		return externalPublicKeys.contains(key);
	}

	public Vector parseXml(String xml) throws Exception
	{
		Vector keys = new Vector();
		if(xml.length() == 0)
			return keys;
		ExternalPublicKeysXmlLoader loader = createXmlLoader(keys);
		try
		{
			SimpleXmlParser.parse(loader, xml);
			return keys;
		}
		catch(Exception e)
		{
			throw new Exception(e);
		}
	}
	
	abstract ExternalPublicKeysXmlLoader createXmlLoader(Vector xmlKeys);
	
	public static final String PUBLIC_KEY_TAG = "PublicKey";
	public static final String LABEL_TAG = "Label";
	private final boolean DONT_INCLUDE_LABEL = false;
	private final boolean INCLUDE_LABEL = true;

	private Vector externalPublicKeys;
}
