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
package org.martus.common.test;

import java.util.Vector;

import org.martus.common.ContactKey;
import org.martus.common.ExternalPublicKeys;
import org.martus.common.FieldDeskKey;
import org.martus.common.FieldDeskKeys;
import org.martus.common.MartusXml;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.xml.XmlUtilities;

public class TestFieldDeskKeys extends TestCaseEnhanced
{
	public TestFieldDeskKeys(String name)
	{
		super(name);
	}
	
	public void testBasics() throws InvalidBase64Exception
	{
		FieldDeskKeys keys = new FieldDeskKeys();
		assertTrue(keys.isEmpty());
		assertEquals(0, keys.size());
		String publicKey1 = "123";
		FieldDeskKey key = new FieldDeskKey(publicKey1);
		assertTrue("Should be able to receive From FD", key.getCanReceiveFrom());
		assertTrue("Should be able to Send to initially for FD", key.getCanSendTo());
		keys.add(key);
		assertEquals(1, keys.size());
		assertTrue(keys.containsKey(publicKey1));
		assertTrue(keys.contains(key));
		FieldDeskKey retrieved = keys.get(0);
		assertEquals(key.getPublicKey(), retrieved.getPublicKey());
		assertEquals(key.getLabel(), retrieved.getLabel());
		keys.remove(0);
		assertEquals(0, keys.size());

		String publicKey2 = "123";
		String label2 = "abc";
		FieldDeskKey key2 = new FieldDeskKey(publicKey2, label2);
		assertTrue("Should be able to receive From FD", key2.getCanReceiveFrom());
		assertTrue("Should be able to Send to initially for FD", key2.getCanSendTo());
		keys.add(key2);
		assertEquals(label2, keys.getLabelIfPresent(key2));

		key2.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
		FieldDeskKey duplicateKey = new FieldDeskKey(key2);
		assertEquals(duplicateKey.getCanReceiveFrom(), key2.getCanReceiveFrom());
		assertEquals(duplicateKey.getCanSendTo(), key2.getCanSendTo());
		assertEquals(duplicateKey.getLabel(), key2.getLabel());
		assertEquals(duplicateKey.getFormattedPublicCode(), key2.getFormattedPublicCode());
		assertEquals(duplicateKey.getPublicKey(), key2.getPublicKey());
		assertEquals(duplicateKey.getVerificationStatus(), key2.getVerificationStatus());
	}
	
	public void testAddKeys()
	{
		FieldDeskKeys keys = new FieldDeskKeys();
		String publicKey1 = "123";
		FieldDeskKey key = new FieldDeskKey(publicKey1);
		keys.add(key);
		String publicKey2 = "123";
		String label2 = "abc";
		FieldDeskKey key2 = new FieldDeskKey(publicKey2, label2);
		keys.add(key2);
		assertEquals(2, keys.size());
		
		FieldDeskKeys newKeys = new FieldDeskKeys(keys);
		assertEquals(2, newKeys.size());
		assertTrue(newKeys.containsKey(publicKey1));
		assertTrue(newKeys.containsKey(publicKey2));
		
		FieldDeskKeys newKeys2 = new FieldDeskKeys();
		newKeys2.add(keys);
		assertEquals(2, newKeys.size());
		assertTrue(newKeys2.containsKey(publicKey1));
		assertTrue(newKeys2.containsKey(publicKey2));
		
	}
	
	public void testEmpty()
	{
		FieldDeskKeys keys = new FieldDeskKeys();
		String xmlExpected = MartusXml.getTagStartWithNewline(FieldDeskKeys.FIELD_DESK_KEYS_TAG) +
		MartusXml.getTagEnd(FieldDeskKeys.FIELD_DESK_KEYS_TAG);
		assertEquals(xmlExpected, keys.toString());
	}
	
	public void testXmlRepresentation()
	{
		Vector keys = new Vector();
		String key1 = "key 1";
		String label1 = "label 1";
		String key2 = "key 2";
		String label2 = "label 2 with <icky &xml stuff>";
		keys.add(new FieldDeskKey(key1, label1));
		keys.add(new FieldDeskKey(key2, label2));
		FieldDeskKeys fieldDeskKeys = new FieldDeskKeys(keys);
		String xmlExpected = MartusXml.getTagStartWithNewline(FieldDeskKeys.FIELD_DESK_KEYS_TAG) +
		 MartusXml.getTagStart(FieldDeskKeys.FIELD_DESK_KEY_TAG) + 
		 MartusXml.getTagStart(ExternalPublicKeys.PUBLIC_KEY_TAG) + 
		 XmlUtilities.getXmlEncoded(key1) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.PUBLIC_KEY_TAG) +
		 MartusXml.getTagEnd(FieldDeskKeys.FIELD_DESK_KEY_TAG) +
		 MartusXml.getTagStart(FieldDeskKeys.FIELD_DESK_KEY_TAG) + 
		 MartusXml.getTagStart(ExternalPublicKeys.PUBLIC_KEY_TAG) + 
		 XmlUtilities.getXmlEncoded(key2) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.PUBLIC_KEY_TAG) +
		 MartusXml.getTagEnd(FieldDeskKeys.FIELD_DESK_KEY_TAG) +
		 MartusXml.getTagEnd(FieldDeskKeys.FIELD_DESK_KEYS_TAG);
		
		assertEquals(xmlExpected, fieldDeskKeys.toString());
	}

	public void testXmlRepresentationWithLabels()
	{
		Vector keys = new Vector();
		String key1 = "key 1";
		String label1 = "label 1";
		String key2 = "key 2";
		String label2 = "label 2 with <icky &xml stuff>";
		keys.add(new FieldDeskKey(key1, label1));
		keys.add(new FieldDeskKey(key2, label2));
		FieldDeskKeys fieldDeskKeys = new FieldDeskKeys(keys);
		String xmlExpected = MartusXml.getTagStartWithNewline(FieldDeskKeys.FIELD_DESK_KEYS_TAG) +
		 MartusXml.getTagStart(FieldDeskKeys.FIELD_DESK_KEY_TAG) + 
		 MartusXml.getTagStart(ExternalPublicKeys.PUBLIC_KEY_TAG) + 
		 XmlUtilities.getXmlEncoded(key1) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.PUBLIC_KEY_TAG) +
		 MartusXml.getTagStart(ExternalPublicKeys.LABEL_TAG) + 
		 XmlUtilities.getXmlEncoded(label1) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.LABEL_TAG) +
		 MartusXml.getTagEnd(FieldDeskKeys.FIELD_DESK_KEY_TAG) +
		 MartusXml.getTagStart(FieldDeskKeys.FIELD_DESK_KEY_TAG) + 
		 MartusXml.getTagStart(ExternalPublicKeys.PUBLIC_KEY_TAG) + 
		 XmlUtilities.getXmlEncoded(key2) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.PUBLIC_KEY_TAG) +
		 MartusXml.getTagStart(ExternalPublicKeys.LABEL_TAG) + 
		 XmlUtilities.getXmlEncoded(label2) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.LABEL_TAG) +
		 MartusXml.getTagEnd(FieldDeskKeys.FIELD_DESK_KEY_TAG) +
		 MartusXml.getTagEnd(FieldDeskKeys.FIELD_DESK_KEYS_TAG);
		
		assertEquals(xmlExpected, fieldDeskKeys.toStringWithLabel());
	}

	
	public void testParseXml() throws Exception
	{
		Vector keys = new Vector();
		String key1 = "key 1";
		String label1 = "label 1";
		String key2 = "key 2";
		String label2 = "label 2";
		keys.add(new FieldDeskKey(key1, label1));
		keys.add(new FieldDeskKey(key2, label2));
		FieldDeskKeys fieldDeskKeys = new FieldDeskKeys(keys);

		
		Vector newKeys = new FieldDeskKeys().parseXml(fieldDeskKeys.toString());
		FieldDeskKeys fieldDeskKeys2 = new FieldDeskKeys(newKeys);
		
		assertEquals(fieldDeskKeys.toString(), fieldDeskKeys2.toString());
	}
}
