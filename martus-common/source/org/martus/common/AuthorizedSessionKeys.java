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
package org.martus.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlParser;
import org.martus.util.xml.SimpleXmlStringLoader;
import org.martus.util.xml.XmlUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class AuthorizedSessionKeys
{
	public AuthorizedSessionKeys()
	{
		this(new HashMap());
	}
	
	public AuthorizedSessionKeys(HashMap mapToUse)
	{
		super();
		authorizedSessionKeys = mapToUse;
	}
	
	public void addToAuthorized(String publicCode, String sessionKey)
	{
		authorizedSessionKeys.put(publicCode, sessionKey);
	}
	
	public String getSessionKey(String publicCode)
	{
		return (String)authorizedSessionKeys.get(publicCode);
	}
	
	public String toString()
	{
		String xmlRepresentation = MartusXml.getTagStartWithNewline(AUTHORIZED_SESSION_KEYS_TAG);
		for (Iterator i = authorizedSessionKeys.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry entry = (Map.Entry) i.next();
			String publicCode = (String)entry.getKey();
			xmlRepresentation += MartusXml.getTagStart(AUTHORIZED_SESSION_KEY_TAG,AUTHORIZED_PUBLIC_CODE_ATTRIBUTE,publicCode);
			String sessionKey = (String)entry.getValue();
			xmlRepresentation += XmlUtilities.getXmlEncoded(sessionKey);
			xmlRepresentation += MartusXml.getTagEnd(AUTHORIZED_SESSION_KEY_TAG);
		}
		xmlRepresentation += MartusXml.getTagEnd(AUTHORIZED_SESSION_KEYS_TAG);
		return xmlRepresentation;
	}
	
	public static class AuthorizedSessionKeysException extends Exception 
	{
	}

	public static HashMap parseXml(String xml) throws AuthorizedSessionKeysException
	{
		HashMap authorized = new HashMap();
		if(xml.length() == 0)
			return authorized;
		XmlAuthorizedLoader loader = new XmlAuthorizedLoader(authorized);
		try
		{
			SimpleXmlParser.parse(loader, xml);
			return authorized;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new AuthorizedSessionKeysException();
		}
	}
	
	public static class XmlAuthorizedLoader extends SimpleXmlDefaultLoader
	{
		public XmlAuthorizedLoader(Map mapToUse)
		{
			super(AUTHORIZED_SESSION_KEYS_TAG);
			sessionKeys = mapToUse;
		}
		
		public SimpleXmlDefaultLoader startElement(String tag)
				throws SAXParseException
		{
			if(tag.equals(AUTHORIZED_SESSION_KEY_TAG))
				return new XmlSessionKeyLoader(sessionKeys);
			return super.startElement(tag);
		}

		Map sessionKeys;
}
	
	public static class XmlSessionKeyLoader extends SimpleXmlStringLoader
	{
		public XmlSessionKeyLoader(Map authorizedSessionKey)
		{
			super(AUTHORIZED_SESSION_KEY_TAG);
			authorizedSession = authorizedSessionKey;
		}

		public void startDocument(Attributes attrs) throws SAXParseException
		{
			publicCode = attrs.getValue(AUTHORIZED_PUBLIC_CODE_ATTRIBUTE);
			super.startDocument(attrs);
		}

		public void endDocument() throws SAXException
		{
			authorizedSession.put(publicCode, getText());
			super.endDocument();
		}
		
		String publicCode;
		Map authorizedSession;
		
	}

	public static final String AUTHORIZED_SESSION_KEYS_TAG = "AuthorizedSessionKeys";
	public static final String AUTHORIZED_SESSION_KEY_TAG = "SessionKey";
	public static final String AUTHORIZED_PUBLIC_CODE_ATTRIBUTE = "publiccode";
	HashMap authorizedSessionKeys;
}
