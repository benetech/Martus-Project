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
package org.martus.common.test;

import java.util.Vector;

import org.martus.common.ContactKey;
import org.martus.common.ContactKeys;
import org.martus.common.ExternalPublicKeys;
import org.martus.common.MartusXml;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.xml.XmlUtilities;

public class TestContactKeys extends TestCaseEnhanced
{
	public TestContactKeys(String name)
	{
		super(name);
	}
	
	public void testBasics() throws InvalidBase64Exception
	{
		
		ContactKeys contactKeys = new ContactKeys();
		assertTrue(contactKeys.isEmpty());
		assertEquals(0, contactKeys.size());
		String publicKey1 = "123";
		ContactKey key = new ContactKey(publicKey1);
		contactKeys.add(key);
		assertEquals(1, contactKeys.size());
		assertTrue(contactKeys.containsKey(publicKey1));
		ContactKey retrieved = contactKeys.get(0);
		assertEquals(key.getPublicKey(), retrieved.getPublicKey());
		assertEquals(key.getLabel(), retrieved.getLabel());
		contactKeys.remove(0);
		assertEquals(0, contactKeys.size());

		String publicKey2 = "123";
		String label2 = "abc";
		ContactKey key2 = new ContactKey(publicKey2, label2);
		contactKeys.add(key2);
		assertEquals(label2, contactKeys.getLabelIfPresent(key2));
		assertTrue("New contacts by default now can send to anyone.", key2.getCanSendTo());
		assertTrue("New contacts by default now can receive from anyone.", key2.getCanReceiveFrom());
		assertFalse("User must select if they want sendToByDefault, so this should be false by default", key2.getSendToByDefault());
		
		key2.setSendToByDefault(true);
		assertTrue(key2.getCanSendTo());
		assertTrue(key2.getSendToByDefault());
		assertTrue(key2.getCanReceiveFrom());
		assertEquals(ContactKey.NOT_VERIFIED_UNKNOWN, key2.getVerificationStatus());
		
		
		key2.setVerificationStatus(ContactKey.VERIFIED_VISUALLY);
		key2.setSendToByDefault(false);
		assertFalse(key2.getSendToByDefault());
		assertTrue(key2.getCanSendTo());
		assertTrue(key2.getCanReceiveFrom());
		
		ContactKey duplicateKey = new ContactKey(key2);
		assertEquals(key2.getFormattedPublicCode(), duplicateKey.getFormattedPublicCode());
		assertEquals(key2.getLabel(), duplicateKey.getLabel());
		assertEquals(key2.getCanReceiveFrom(), duplicateKey.getCanReceiveFrom());
		assertEquals(key2.getCanSendTo(), duplicateKey.getCanSendTo());
		assertEquals(key2.getSendToByDefault(), duplicateKey.getSendToByDefault());
		assertEquals(key2.getVerificationStatus(), duplicateKey.getVerificationStatus());
	
		
		key2.setSendToByDefault(true);
		assertTrue("Setting this HQ to CanSendTo now does not affect sendToByDefault, CanSendTo and CanReceive From will go away at some point.", key2.getSendToByDefault());
		
		ContactKey dupKey2 = new ContactKey(key2);
		assertTrue(dupKey2.getCanSendTo());
		assertTrue(dupKey2.getSendToByDefault());
		assertTrue(dupKey2.getCanReceiveFrom());
		
		key2.setSendToByDefault(false);
		
		ContactKey dupKey3 = new ContactKey(key2);
		assertTrue(dupKey3.getCanSendTo());
		assertFalse(dupKey3.getSendToByDefault());
		assertTrue(dupKey3.getCanReceiveFrom());
		
		
	}
	
	public void testAddKeys()
	{
		ContactKeys contactKeys = new ContactKeys();
		String publicKey1 = "123";
		ContactKey key = new ContactKey(publicKey1);
		contactKeys.add(key);
		String publicKey2 = "123";
		String label2 = "abc";
		ContactKey key2 = new ContactKey(publicKey2, label2);
		contactKeys.add(key2);
		assertEquals(2, contactKeys.size());
		
		ContactKeys newKeys = new ContactKeys(contactKeys);
		assertEquals(2, newKeys.size());
		assertTrue(newKeys.containsKey(publicKey1));
		assertTrue(newKeys.containsKey(publicKey2));
		
		ContactKeys newKeys2 = new ContactKeys();
		newKeys2.add(contactKeys);
		assertEquals(2, newKeys.size());
		assertTrue(newKeys2.containsKey(publicKey1));
		assertTrue(newKeys2.containsKey(publicKey2));
	}
	
	public void testEmpty()
	{
		ContactKeys contactKeys = new ContactKeys();
		String xmlExpected = MartusXml.getTagStartWithNewline(ContactKeys.CONTACT_KEYS_TAG) +
		MartusXml.getTagEnd(ContactKeys.CONTACT_KEYS_TAG);
		assertEquals(xmlExpected, contactKeys.toString());
	}
	
	public void testXmlRepresentation()
	{
		Vector keys = new Vector();
		String key1 = "key 1";
		String label1 = "label 1";
		String key2 = "key 2";
		String label2 = "label 2 with <icky &xml stuff>";
		String key3 = "key 2";
		String label3 = "label 3";
		ContactKey contactKey1 = new ContactKey(key1, label1);
		contactKey1.setVerificationStatus(ContactKey.NOT_VERIFIED);
		keys.add(contactKey1);
		ContactKey contactKey2 = new ContactKey(key2, label2);
		contactKey2.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
		keys.add(contactKey2);
		ContactKey contactKey3 = new ContactKey(key3, label3);
		contactKey3.setSendToByDefault(true);
		contactKey3.setVerificationStatus(ContactKey.VERIFIED_VISUALLY);
		keys.add(contactKey3);
		ContactKeys contactKeys = new ContactKeys(keys);
		String xmlExpected = MartusXml.getTagStartWithNewline(ContactKeys.CONTACT_KEYS_TAG) +
		 MartusXml.getTagStart(ContactKeys.CONTACT_KEY_TAG) + 
		 MartusXml.getTagStart(ExternalPublicKeys.PUBLIC_KEY_TAG) + 
		 XmlUtilities.getXmlEncoded(key1) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.PUBLIC_KEY_TAG) +
		 MartusXml.getTagStart(ExternalPublicKeys.LABEL_TAG) + 
		 XmlUtilities.getXmlEncoded(label1) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.LABEL_TAG) +
		 MartusXml.getTagStart(ContactKeys.SEND_TO_BY_DEFAULT_TAG) + 
		 XmlUtilities.getXmlEncoded(ContactKeys.NO_DATA) +
		 MartusXml.getTagEndWithoutNewline(ContactKeys.SEND_TO_BY_DEFAULT_TAG) +
		 MartusXml.getTagStart(ContactKeys.VERIFICATION_STATUS_TAG) + 
		 XmlUtilities.getXmlEncoded(Integer.toString(ContactKey.NOT_VERIFIED)) +
		 MartusXml.getTagEndWithoutNewline(ContactKeys.VERIFICATION_STATUS_TAG) +
		 MartusXml.getTagEnd(ContactKeys.CONTACT_KEY_TAG) +

		 MartusXml.getTagStart(ContactKeys.CONTACT_KEY_TAG) + 
		 MartusXml.getTagStart(ExternalPublicKeys.PUBLIC_KEY_TAG) + 
		 XmlUtilities.getXmlEncoded(key2) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.PUBLIC_KEY_TAG) +
		 MartusXml.getTagStart(ExternalPublicKeys.LABEL_TAG) + 
		 XmlUtilities.getXmlEncoded(label2) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.LABEL_TAG) +
		 MartusXml.getTagStart(ContactKeys.SEND_TO_BY_DEFAULT_TAG) + 
		 XmlUtilities.getXmlEncoded(ContactKeys.NO_DATA) +
		 MartusXml.getTagEndWithoutNewline(ContactKeys.SEND_TO_BY_DEFAULT_TAG) +
		 MartusXml.getTagStart(ContactKeys.VERIFICATION_STATUS_TAG) + 
		 XmlUtilities.getXmlEncoded(Integer.toString(ContactKey.VERIFIED_ENTERED_20_DIGITS)) +
		 MartusXml.getTagEndWithoutNewline(ContactKeys.VERIFICATION_STATUS_TAG) +
		 MartusXml.getTagEnd(ContactKeys.CONTACT_KEY_TAG) +

		 MartusXml.getTagStart(ContactKeys.CONTACT_KEY_TAG) + 
		 MartusXml.getTagStart(ExternalPublicKeys.PUBLIC_KEY_TAG) + 
		 XmlUtilities.getXmlEncoded(key3) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.PUBLIC_KEY_TAG) +
		 MartusXml.getTagStart(ExternalPublicKeys.LABEL_TAG) + 
		 XmlUtilities.getXmlEncoded(label3) +
		 MartusXml.getTagEndWithoutNewline(ExternalPublicKeys.LABEL_TAG) +
		 MartusXml.getTagStart(ContactKeys.SEND_TO_BY_DEFAULT_TAG) + 
		 XmlUtilities.getXmlEncoded(ContactKeys.YES_DATA) +
		 MartusXml.getTagEndWithoutNewline(ContactKeys.SEND_TO_BY_DEFAULT_TAG) +
		 MartusXml.getTagStart(ContactKeys.VERIFICATION_STATUS_TAG) + 
		 XmlUtilities.getXmlEncoded(Integer.toString(ContactKey.VERIFIED_VISUALLY)) +
		 MartusXml.getTagEndWithoutNewline(ContactKeys.VERIFICATION_STATUS_TAG) +
		 MartusXml.getTagEnd(ContactKeys.CONTACT_KEY_TAG) +
		 
		 MartusXml.getTagEnd(ContactKeys.CONTACT_KEYS_TAG);
		
		assertEquals(xmlExpected, contactKeys.toString());
	}

	
	public void testParseXml() throws Exception
	{
		Vector keys = new Vector();
		String key1 = "key 1";
		String label1 = "label 1";
		String key2 = "key 2";
		String label2 = "label 2";
		ContactKey contactKey1 = new ContactKey(key1, label1);
		contactKey1.setVerificationStatus(ContactKey.NOT_VERIFIED);
		assertTrue("Key1 CanSendTo not True?", contactKey1.getCanSendTo());
		assertTrue("Key1 CanReceiveFrom not True?", contactKey1.getCanReceiveFrom());
		assertEquals("Key1 not verified?", ContactKey.NOT_VERIFIED, contactKey1.getVerificationStatus());
		keys.add(contactKey1);

		ContactKey contactKey2 = new ContactKey(key2, label2);
		contactKey2.setVerificationStatus(ContactKey.VERIFIED_VISUALLY);
		keys.add(contactKey2);
		assertTrue("Key2 CanSendTo not same?", contactKey2.getCanSendTo());
		assertTrue("Key2 CanReceiveFrom not same?", contactKey2.getCanReceiveFrom());
		assertEquals("Key2 Verified not VERIFIED_VISUALLY?", ContactKey.VERIFIED_VISUALLY, contactKey2.getVerificationStatus());
		ContactKeys contactKeys = new ContactKeys(keys);

		
		Vector newKeys = new ContactKeys().parseXml(contactKeys.toString());
		ContactKeys contactKeys2 = new ContactKeys(newKeys);
		
		assertEquals(contactKeys.toString(), contactKeys2.toString());
		
		ContactKey retrieved = contactKeys2.get(0);
		assertEquals("retrieved Key1 label not same?", contactKey1.getLabel(), retrieved.getLabel());
		assertEquals("retrieved Key1 CanSendTo not same?", contactKey1.getCanSendTo(), retrieved.getCanSendTo());
		assertEquals("retrieved Key1 CanReceiveFrom not same?", contactKey1.getCanReceiveFrom(), retrieved.getCanReceiveFrom());
		assertEquals("retrieved Key1 not verified?", ContactKey.NOT_VERIFIED, retrieved.getVerificationStatus());
		
		ContactKey retrieved2 = contactKeys2.get(1);
		assertEquals("retrieved Key2 label not same?", contactKey2.getLabel(), retrieved2.getLabel());
		assertEquals("retrieved Key2 CanSendTo not same?", contactKey2.getCanSendTo(), retrieved2.getCanSendTo());
		assertEquals("retrieved Key2 CanReceiveFrom not same?", contactKey2.getCanReceiveFrom(), retrieved2.getCanReceiveFrom());
		assertEquals("retrieved Key2 Verified not VERIFIED_VISUALLY?", ContactKey.VERIFIED_VISUALLY, retrieved2.getVerificationStatus());
	}
}
