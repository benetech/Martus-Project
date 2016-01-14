/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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

import org.martus.util.xml.SimpleXmlParser;
import org.martus.util.xml.XmlUtilities;

public class ContactKeys extends ExternalPublicKeys
{

	public ContactKeys()
	{
		super();
	}
	
	public ContactKeys(Vector keysToUse)
	{
		super(keysToUse);
	}
	
	public ContactKeys(ContactKey key) 
	{
		add(key);
	}
	
	public ContactKeys(ContactKeys keys) 
	{
		add(keys);
	}

	public ContactKeys(String xml) throws Exception
	{
		super(xml);	
	}
	
	public Vector parseXml(String xml) throws Exception
	{
		Vector keys = new Vector();
		if(xml.length() == 0)
			return keys;
		ContactKeysXmlLoader loader = createContactKeysXmlLoader(keys);
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
	
	
	String getTopLevelXmlElementName()
	{
		return CONTACT_KEYS_TAG;
	}
	
	String getSingleEntryXmlElementName()
	{
		return CONTACT_KEY_TAG;
	}

	ContactKeysXmlLoader createContactKeysXmlLoader(Vector xmlKeys)
	{
		return createLoader(xmlKeys);
	}

	ExternalPublicKeysXmlLoader createXmlLoader(Vector xmlKeys)
	{
		return null;
	}

	public static ContactKeysXmlLoader createLoader(Vector xmlKeys)
	{
		return new ContactKeysXmlLoader(xmlKeys);
	}


	public ContactKey get(int i)
	{
		return (ContactKey)rawGet(i);
	}
	
	public void add(ContactKey key)
	{
		rawAdd(key);
	}
	
	public void add(ContactKeys keys)
	{
		rawAdd(keys);
	}
	
	public String toString()
	{
		return toStringWithLabel();
	}
	
	public String toStringWithLabel()
	{
		return getXMLRepresentation();
	}
	
	private String getXMLRepresentation()
	{
		String xmlRepresentation = MartusXml.getTagStartWithNewline(getTopLevelXmlElementName());
		for(int i = 0; i < size(); ++i)
		{
			xmlRepresentation += MartusXml.getTagStart(getSingleEntryXmlElementName());
			xmlRepresentation += MartusXml.getTagStart(PUBLIC_KEY_TAG);
			xmlRepresentation += rawGet(i).getPublicKey();
			xmlRepresentation += MartusXml.getTagEndWithoutNewline(PUBLIC_KEY_TAG);
			xmlRepresentation += MartusXml.getTagStart(LABEL_TAG);
			xmlRepresentation += XmlUtilities.getXmlEncoded(rawGet(i).getLabel());
			xmlRepresentation += MartusXml.getTagEndWithoutNewline(LABEL_TAG);

			xmlRepresentation += MartusXml.getTagStart(SEND_TO_BY_DEFAULT_TAG);
			if(((ContactKey)rawGet(i)).getSendToByDefault())
				xmlRepresentation += XmlUtilities.getXmlEncoded(YES_DATA);
			else
				xmlRepresentation += XmlUtilities.getXmlEncoded(NO_DATA);
			xmlRepresentation += MartusXml.getTagEndWithoutNewline(SEND_TO_BY_DEFAULT_TAG);

			xmlRepresentation += MartusXml.getTagStart(VERIFICATION_STATUS_TAG);
			xmlRepresentation += ((ContactKey)rawGet(i)).getVerificationStatus();
			xmlRepresentation += MartusXml.getTagEndWithoutNewline(VERIFICATION_STATUS_TAG);

			xmlRepresentation += MartusXml.getTagEnd(getSingleEntryXmlElementName());
		}
		xmlRepresentation += MartusXml.getTagEnd(getTopLevelXmlElementName());
		
		return xmlRepresentation;
	}
	
	
	public static final String CONTACT_KEYS_TAG = "Contacts";
	public static final String CONTACT_KEY_TAG = "Contact";
	public static final String SEND_TO_BY_DEFAULT_TAG = "SendToByDefault";
	public static final String VERIFICATION_STATUS_TAG = "VerificationStatus";
	public static final String YES_DATA = "YES";
	public static final String NO_DATA = "NO";
}
