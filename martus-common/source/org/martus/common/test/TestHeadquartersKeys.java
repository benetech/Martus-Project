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
package org.martus.common.test;

import java.util.Vector;

import org.martus.common.ContactKey;
import org.martus.common.ExternalPublicKeys;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusXml;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.xml.XmlUtilities;


public class TestHeadquartersKeys extends TestCaseEnhanced
{
	public TestHeadquartersKeys(String name)
	{
		super(name);
	}
	
	public void testBasics() throws InvalidBase64Exception
	{
		HeadquartersKeys hqKeys = new HeadquartersKeys();
		assertTrue(hqKeys.isEmpty());
		assertEquals(0, hqKeys.size());
		String publicKey1 = "123";
		HeadquartersKey key = new HeadquartersKey(publicKey1);
		assertTrue("Should be able to receive From HQ", key.getCanReceiveFrom());
		assertTrue("Should be able to Send to initially for HQ", key.getCanSendTo());
		hqKeys.add(key);
		assertEquals(1, hqKeys.size());
		assertTrue(hqKeys.containsKey(publicKey1));
		assertTrue(hqKeys.contains(key));
		HeadquartersKey retrieved = hqKeys.get(0);
		assertEquals(key.getPublicKey(), retrieved.getPublicKey());
		assertEquals(key.getLabel(), retrieved.getLabel());
		hqKeys.remove(0);
		assertEquals(0, hqKeys.size());

		String publicKey2 = "123";
		String label2 = "abc";
		HeadquartersKey key2 = new HeadquartersKey(publicKey2, label2);
		hqKeys.add(key2);
		assertEquals(label2, hqKeys.getLabelIfPresent(key2));
		assertTrue("Should be able to receive From HQ", key2.getCanReceiveFrom());
		assertTrue("Should be able to Send to initially for HQ", key2.getCanSendTo());
		
		key2.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
		HeadquartersKey duplicateKey = new HeadquartersKey(key2);
		assertEquals(duplicateKey.getCanReceiveFrom(), key2.getCanReceiveFrom());
		assertEquals(duplicateKey.getCanSendTo(), key2.getCanSendTo());
		assertEquals(duplicateKey.getLabel(), key2.getLabel());
		assertEquals(duplicateKey.getFormattedPublicCode(), key2.getFormattedPublicCode());
		assertEquals(duplicateKey.getPublicKey(), key2.getPublicKey());
		assertEquals(duplicateKey.getVerificationStatus(), key2.getVerificationStatus());
		
	}
	
	public void testAddKeys()
	{
		HeadquartersKeys hqKeys = new HeadquartersKeys();
		String publicKey1 = "123";
		HeadquartersKey key = new HeadquartersKey(publicKey1);
		hqKeys.add(key);
		String publicKey2 = "123";
		String label2 = "abc";
		HeadquartersKey key2 = new HeadquartersKey(publicKey2, label2);
		hqKeys.add(key2);
		assertEquals(2, hqKeys.size());
		
		HeadquartersKeys newKeys = new HeadquartersKeys(hqKeys);
		assertEquals(2, newKeys.size());
		assertTrue(newKeys.containsKey(publicKey1));
		assertTrue(newKeys.containsKey(publicKey2));
		
		HeadquartersKeys newKeys2 = new HeadquartersKeys();
		newKeys2.add(hqKeys);
		assertEquals(2, newKeys.size());
		assertTrue(newKeys2.containsKey(publicKey1));
		assertTrue(newKeys2.containsKey(publicKey2));
		
	}
	
	public void testEmpty()
	{
		HeadquartersKeys hqKeys = new HeadquartersKeys();
		String xmlExpected = MartusXml.getTagStartWithNewline(HeadquartersKeys.HQ_KEYS_TAG) +
		MartusXml.getTagEnd(HeadquartersKeys.HQ_KEYS_TAG);
		assertEquals(xmlExpected, hqKeys.toString());
	}
	
	public void testXmlRepresentation()
	{
		Vector keys = new Vector();
		String key1 = "key 1";
		String label1 = "label 1";
		String key2 = "key 2";
		String label2 = "label 2 with <icky &xml stuff>";
		keys.add(new HeadquartersKey(key1, label1));
		keys.add(new HeadquartersKey(key2, label2));
		HeadquartersKeys hqKeys = new HeadquartersKeys(keys);
		String xmlExpected = MartusXml.getTagStartWithNewline(HeadquartersKeys.HQ_KEYS_TAG) +
		 MartusXml.getTagStart(HeadquartersKeys.HQ_KEY_TAG) + 
		 MartusXml.getTagStart(ExternalPublicKeys.PUBLIC_KEY_TAG) + 
		 XmlUtilities.getXmlEncoded(key1) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.PUBLIC_KEY_TAG) +
		 MartusXml.getTagEnd(HeadquartersKeys.HQ_KEY_TAG) +
		 MartusXml.getTagStart(HeadquartersKeys.HQ_KEY_TAG) + 
		 MartusXml.getTagStart(ExternalPublicKeys.PUBLIC_KEY_TAG) + 
		 XmlUtilities.getXmlEncoded(key2) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.PUBLIC_KEY_TAG) +
		 MartusXml.getTagEnd(HeadquartersKeys.HQ_KEY_TAG) +
		 MartusXml.getTagEnd(HeadquartersKeys.HQ_KEYS_TAG);
		
		assertEquals(xmlExpected, hqKeys.toString());
	}

	public void testXmlRepresentationWithLabels()
	{
		Vector keys = new Vector();
		String key1 = "key 1";
		String label1 = "label 1";
		String key2 = "key 2";
		String label2 = "label 2 with <icky &xml stuff>";
		keys.add(new HeadquartersKey(key1, label1));
		keys.add(new HeadquartersKey(key2, label2));
		HeadquartersKeys hqKeys = new HeadquartersKeys(keys);
		String xmlExpected = MartusXml.getTagStartWithNewline(HeadquartersKeys.HQ_KEYS_TAG) +
		 MartusXml.getTagStart(HeadquartersKeys.HQ_KEY_TAG) + 
		 MartusXml.getTagStart(ExternalPublicKeys.PUBLIC_KEY_TAG) + 
		 XmlUtilities.getXmlEncoded(key1) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.PUBLIC_KEY_TAG) +
		 MartusXml.getTagStart(ExternalPublicKeys.LABEL_TAG) + 
		 XmlUtilities.getXmlEncoded(label1) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.LABEL_TAG) +
		 MartusXml.getTagEnd(HeadquartersKeys.HQ_KEY_TAG) +
		 MartusXml.getTagStart(HeadquartersKeys.HQ_KEY_TAG) + 
		 MartusXml.getTagStart(ExternalPublicKeys.PUBLIC_KEY_TAG) + 
		 XmlUtilities.getXmlEncoded(key2) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.PUBLIC_KEY_TAG) +
		 MartusXml.getTagStart(ExternalPublicKeys.LABEL_TAG) + 
		 XmlUtilities.getXmlEncoded(label2) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.LABEL_TAG) +
		 MartusXml.getTagEnd(HeadquartersKeys.HQ_KEY_TAG) +
		 MartusXml.getTagEnd(HeadquartersKeys.HQ_KEYS_TAG);
		
		assertEquals(xmlExpected, hqKeys.toStringWithLabel());
	}

	
	public void testParseXml() throws Exception
	{
		Vector keys = new Vector();
		String key1 = "key 1";
		String label1 = "label 1";
		String key2 = "key 2";
		String label2 = "label 2";
		keys.add(new HeadquartersKey(key1, label1));
		keys.add(new HeadquartersKey(key2, label2));
		HeadquartersKeys hqKeys = new HeadquartersKeys(keys);

		
		Vector newKeys = new HeadquartersKeys().parseXml(hqKeys.toString());
		HeadquartersKeys hqKeys2 = new HeadquartersKeys(newKeys);
		
		assertEquals(hqKeys.toString(), hqKeys2.toString());
	}

	
}
