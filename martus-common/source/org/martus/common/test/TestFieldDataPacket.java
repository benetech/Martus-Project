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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.martus.common.AuthorizedSessionKeys;
import org.martus.common.FieldCollection;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.LegacyCustomFields;
import org.martus.common.MartusConstants;
import org.martus.common.MartusXml;
import org.martus.common.XmlWriterFilter;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.UniversalId;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.xml.XmlUtilities;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;




public class TestFieldDataPacket extends TestCaseEnhanced
{
	public TestFieldDataPacket(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(security == null)
		{
			security = MockMartusSecurity.createClient();
		}
		if(securityHQ == null)
		{
			securityHQ = MockMartusSecurity.createHQ();
		}
		UniversalId uid = FieldDataPacket.createUniversalId(security);
		fdp = new FieldDataPacket(uid, fieldTags);
	}

	public void testBasics()
	{
		assertEquals("getFieldCount", fieldTags.size(), fdp.getFieldCount());
		assertEquals("nope", false, fdp.fieldExists("Nope"));
		assertEquals("plain tag", true, fdp.fieldExists(bTag));
		assertEquals("lower", false, fdp.fieldExists(bTag.toLowerCase()));
		assertEquals("upper", false, fdp.fieldExists(bTag.toUpperCase()));

		assertEquals("tag list", fieldTags, fdp.getFieldSpecs());
		assertEquals("HQ Keys not 0", 0, fdp.getAuthorizedToReadKeys().size());
		String hqKey = "12345";
		HeadquartersKey key = new HeadquartersKey(hqKey);
		HeadquartersKeys keys = new HeadquartersKeys(key);
		keys.add(key);
		
		fdp.setAuthorizedToReadKeys(keys);
		assertEquals("HQ Key not the same?", hqKey, fdp.getAuthorizedToReadKeys().get(0).getPublicKey());
		fdp.clearAll();
		assertEquals("HQ Key not cleared?", 0, fdp.getAuthorizedToReadKeys().size());
	}

	public void testIsEmpty()
	{
		assertEquals("didn't start out empty?", true, fdp.isEmpty());
		fdp.set(fieldTags.get(0).getTag(), "blah");
		assertEquals("still empty after field?", false, fdp.isEmpty());
		fdp.clearAll();
		assertEquals("didn't return to empty after field?", true, fdp.isEmpty());

		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		AttachmentProxy a = new AttachmentProxy(uid, "label", null);
		fdp.addAttachment(a);
		assertEquals("still empty after attachment?", false, fdp.isEmpty());
		fdp.clearAll();
		assertEquals("didn't return to empty after attachment?", true, fdp.isEmpty());
	}

	public void testCreateUniversalId()
	{
		UniversalId uid = FieldDataPacket.createUniversalId(security);
		assertEquals("account", security.getPublicKeyString(), uid.getAccountId());
		assertStartsWith("prefix", "F-", uid.getLocalId());
	}

	public void testIsEncrypted()
	{
		assertEquals("already encrypted?", false, fdp.isEncrypted());
		fdp.setEncrypted(true);
		assertEquals("not encrypted?", true, fdp.isEncrypted());
		fdp.setEncrypted(false);
		assertEquals("still encrypted?", false, fdp.isEncrypted());
	}

	public void testIsPublicData()
	{
		assertEquals("already not Public?", true, fdp.isPublicData());
		fdp.setEncrypted(true);
		assertEquals("still Public?", false, fdp.isPublicData());
		fdp.setEncrypted(false);
		assertEquals("not Back to Public?", true, fdp.isPublicData());
	}


	public void testConstructorWithUniversalId() throws Exception
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(fdp.getAccountId(), fdp.getLocalId());
		FieldDataPacket p = new FieldDataPacket(uid, fieldTags);
		assertEquals("account", fdp.getAccountId(), p.getAccountId());
		assertEquals("packet", fdp.getLocalId(), p.getLocalId());
		// check some other fields here
	}

	public void testGetSet()
	{
		assertEquals("", fdp.get("NoSuchField"));
		fdp.set("NoSuchField", "hello");
		assertEquals("", fdp.get("NoSuchField"));

		assertEquals("", fdp.get(aTag));
		fdp.set(aTag, "hello");
		assertEquals("hello", fdp.get(aTag));
		assertEquals("hello", fdp.get(aTag));
		assertEquals("hello", fdp.get(aTag));

		fdp.set(aTag.toUpperCase(), "another");
		assertEquals("not another", "hello", fdp.get(aTag));

		fdp.set(bTag, "94404");
		assertEquals("94404", fdp.get(bTag));
		assertEquals("after setting other field", "hello", fdp.get(aTag));
		fdp.set(aTag, "goodbye");
		assertEquals("goodbye", fdp.get(aTag));
	}

	public void testClear()
	{
		fdp.set(aTag, "hello");
		assertEquals("hello", fdp.get(aTag));
		fdp.clearAll();
		assertEquals("",fdp.get(aTag));
	}

	public void testAttachments()
	{
		String label1 = "Label 1";
		assertEquals("not none?", 0, fdp.getAttachments().length);
		fdp.addAttachment(new AttachmentProxy(label1));
		AttachmentProxy[] v = fdp.getAttachments();
		assertEquals("not one?", 1, v.length);
		assertEquals("wrong label", label1, v[0].getLabel());

	}

	public void testLoadFromXmlSimple() throws Exception
	{
		String account = "asbid";
		String id = "1234567";
		String data1 = "data 1?";
		String simpleFieldDataPacket =
			"<FieldDataPacket>\n" +
			"<" + MartusXml.PacketIdElementName + ">" + id +
			"</" + MartusXml.PacketIdElementName + ">\n" +
			"<" + MartusXml.AccountElementName + ">" + account +
			"</" + MartusXml.AccountElementName + ">\n" +
			"<" + MartusXml.EncryptedFlagElementName + ">" +
			"</" + MartusXml.EncryptedFlagElementName + ">" +
			fieldListForTesting + 
			"<" + MartusXml.FieldElementPrefix + aTag + ">" + data1 +
			"</" + MartusXml.FieldElementPrefix + aTag + ">\n" +
			"</FieldDataPacket>\n";
		//System.out.println("{" + simpleFieldDataPacket + "}");

		FieldSpecCollection tagsThatWillBeIgnored = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldDataPacket loaded = new FieldDataPacket(UniversalIdForTesting.createDummyUniversalId(), tagsThatWillBeIgnored);

		byte[] bytes = simpleFieldDataPacket.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		loaded.loadFromXml(in, (MartusCrypto)null);

		assertEquals("account", account, loaded.getAccountId());
		assertEquals("id", id, loaded.getLocalId());
		assertEquals("encrypted", true, loaded.isEncrypted());

		FieldSpec[] tags = loaded.getFieldSpecs().asArray();
		assertEquals("Not three fields?", 3, tags.length);
		assertEquals(aTag, tags[0].getTag());
		assertEquals(bTag, tags[1].getTag());
		assertEquals(cTag, tags[2].getTag());

		assertEquals("aTag", data1, loaded.get(aTag));
		
	}

	public void testLoadFromXmlCustomFields() throws Exception
	{
		String account = "asbid";
		String id = "1234567";
		String data1 = "data 1?";
		String label = "\"<Town> \"";
		String simpleFieldDataPacket =
			"<FieldDataPacket>\n" +
			"<" + MartusXml.PacketIdElementName + ">" + id +
			"</" + MartusXml.PacketIdElementName + ">\n" +
			"<" + MartusXml.AccountElementName + ">" + account +
			"</" + MartusXml.AccountElementName + ">\n" +
			"<" + MartusXml.EncryptedFlagElementName + ">" +
			"</" + MartusXml.EncryptedFlagElementName + ">" +
			MartusXml.getTagStart(MartusXml.FieldListElementName) +
			"title;author;custom1,\"&lt;Town&gt; \";entrydate;language" +  
			MartusXml.getTagEnd(MartusXml.FieldListElementName) + 
			"<" + MartusXml.FieldElementPrefix + "custom1>" + data1 +
			"</" + MartusXml.FieldElementPrefix + "custom1>\n" +
			"</FieldDataPacket>\n";
		//System.out.println("{" + simpleFieldDataPacket + "}");

		FieldSpecCollection specsThatWillBeIgnored = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldDataPacket loaded = new FieldDataPacket(UniversalIdForTesting.createDummyUniversalId(), specsThatWillBeIgnored);

		byte[] bytes = simpleFieldDataPacket.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		loaded.loadFromXml(in, (MartusCrypto)null);

		assertEquals("account", account, loaded.getAccountId());
		assertEquals("id", id, loaded.getLocalId());
		assertEquals("encrypted", true, loaded.isEncrypted());

		FieldSpec[] tags = loaded.getFieldSpecs().asArray();
		assertEquals("wrong field count?", 5, tags.length);
		assertEquals("title", tags[0].getTag());
		assertEquals("author", tags[1].getTag());
		assertEquals("custom1", tags[2].getTag());
		assertEquals("entrydate", tags[3].getTag());
		assertEquals("language", tags[4].getTag());

		assertEquals("standard label not empty?", "", tags[0].getLabel());
		assertEquals("custom label", label, tags[2].getLabel());

		assertEquals("custom data", data1, loaded.get("custom1"));
		
	}
	
	public void testLoadFromXmlWithUnknownTags() throws Exception
	{
		class FieldDataPacketWithUnknownTags extends FieldDataPacket
		{
			FieldDataPacketWithUnknownTags(UniversalId uid, FieldSpecCollection specs) throws Exception
			{
				super(uid, specs);
			}
			
			protected void internalWriteXml(XmlWriterFilter dest) throws IOException
			{
				super.internalWriteXml(dest);
				writeElement(dest, "UnknownTagHere", "blah");
				writeElement(dest, MartusXml.FieldElementPrefix + bTag, "data");
			}
		}
		
		UniversalId uid = FieldDataPacket.createUniversalId(security);
		FieldDataPacketWithUnknownTags fdpUnknownTags = new FieldDataPacketWithUnknownTags(uid, fieldTags);
		
		String id = "1234567";
		String data1 = "  simple  ";
		String simpleFieldDataPacket =
			"<FieldDataPacket>\n" +
				"<" + MartusXml.PacketIdElementName + ">" + id +
				"</" + MartusXml.PacketIdElementName + ">\n" +
				"<UnknownTagHere>Blah</UnknownTagHere>" + 
				"<" + MartusXml.FieldElementPrefix + aTag + ">" +
				data1 +
				"</" + MartusXml.FieldElementPrefix + aTag + ">\n" +
			"</FieldDataPacket>\n";
		byte[] bytes = simpleFieldDataPacket.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		fdpUnknownTags.loadFromXml(in, (MartusCrypto)null);
		assertTrue("no unknown tags?", fdpUnknownTags.hasUnknownTags());
		assertEquals("lost data after unknown aTag?", data1, fdpUnknownTags.get(aTag));
		
		StringWriter out = new StringWriter();
		fdpUnknownTags.writeXmlEncrypted(out, security);
		
		byte[] bytes2 = out.toString().getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in2 = new ByteArrayInputStreamWithSeek(bytes2);
		fdpUnknownTags.loadFromXml(in2, security);
		assertTrue("encrypted no unknown tags?", fdpUnknownTags.hasUnknownTags());
		assertEquals("encrypted lost data after unknown bTag?", "data", fdpUnknownTags.get(bTag));
	}

	public void testLoadFromXmlLegacyCustomAfterNewCustomFields() throws Exception
	{
		String account = "asbid";
		String id = "1234567";
		String data1 = "data 1?";
		FieldSpec field = LegacyCustomFields.createFromLegacy("custom1");
		FieldCollection fields = new FieldCollection(new FieldSpec[] {field});
		
		String simpleFieldDataPacket =
			"<FieldDataPacket>\n" +
			MartusXml.getTagStart(MartusXml.PacketIdElementName) + id +
				MartusXml.getTagEnd(MartusXml.PacketIdElementName) + 
			MartusXml.getTagStart(MartusXml.AccountElementName) + account + 
				MartusXml.getTagEnd(MartusXml.AccountElementName) + 
			MartusXml.getTagStart(MartusXml.EncryptedFlagElementName) +  
				MartusXml.getTagEnd(MartusXml.EncryptedFlagElementName) + 
			fields.toString() + 
			MartusXml.getTagStart(MartusXml.FieldListElementName) +
				MartusConstants.deprecatedCustomFieldSpecs +  
				MartusXml.getTagEnd(MartusXml.FieldListElementName) + 
			MartusXml.getTagStart(MartusXml.FieldElementPrefix + "custom1") + data1 +  
				MartusXml.getTagEnd(MartusXml.FieldElementPrefix + "custom1") + 
			"</FieldDataPacket>\n";

		FieldSpecCollection specsThatWillBeIgnored = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldDataPacket loaded = new FieldDataPacket(UniversalIdForTesting.createDummyUniversalId(), specsThatWillBeIgnored);

		byte[] bytes = simpleFieldDataPacket.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		loaded.loadFromXml(in, (MartusCrypto)null);
		
		FieldCollection loadedFields = new FieldCollection(loaded.getFieldSpecs());
		assertEquals(fields.toString(), loadedFields.toString());
	}
	
	public void testLoadFromXmlWithSpaces() throws Exception
	{
		String id = "1234567";
		String data1 = "  simple  ";
		String data2 = "This has  \nsome";
		String data3 = "plain\n  spaces";
		String simpleFieldDataPacket =
			"<FieldDataPacket>\n" +
			"<" + MartusXml.PacketIdElementName + ">" + id +
			"</" + MartusXml.PacketIdElementName + ">\n" +
			"<" + MartusXml.FieldElementPrefix + aTag + ">" +
			data1 +
			"</" + MartusXml.FieldElementPrefix + aTag + ">\n" +
			"<" + MartusXml.FieldElementPrefix + bTag + ">" +
			data2 +
			"</" + MartusXml.FieldElementPrefix + bTag + ">\n" +
			"<" + MartusXml.FieldElementPrefix + cTag + ">" +
			data3 +
			"</" + MartusXml.FieldElementPrefix + cTag + ">\n" +
			"</FieldDataPacket>\n";
		//System.out.println("{" + simpleFieldDataPacket + "}");

		byte[] bytes = simpleFieldDataPacket.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		fdp.loadFromXml(in, (MartusCrypto)null);
		assertEquals("id", id, fdp.getLocalId());
		assertEquals("aTag", data1, fdp.get(aTag));
		assertEquals("bTag", data2, fdp.get(bTag));
		assertEquals("cTag", data3, fdp.get(cTag));
	}

	public void testLoadFromXmlWithNewlines() throws Exception
	{
		String id = "1234567";
		String data1 = "leading\n    spaces";
		String data2 = "trailing newlines\n\n\n\n";
		String data3cr = "crlf\r\npairs\r\n";
		String data3 = "crlf\npairs\n";
		String simpleFieldDataPacket =
			"<FieldDataPacket>\n" +
			"<" + MartusXml.PacketIdElementName + ">" + id +
			"</" + MartusXml.PacketIdElementName + ">\n" +
			"<" + MartusXml.FieldElementPrefix + aTag + ">" +
			data1 +
			"</" + MartusXml.FieldElementPrefix + aTag + ">\n" +
			"<" + MartusXml.FieldElementPrefix + bTag + ">" +
			data2 +
			"</" + MartusXml.FieldElementPrefix + bTag + ">\n" +
			"<" + MartusXml.FieldElementPrefix + cTag + ">" +
			data3cr +
			"</" + MartusXml.FieldElementPrefix + cTag + ">\n" +
			"</FieldDataPacket>\n";
		//System.out.println("{" + simpleFieldDataPacket + "}");

		byte[] bytes = simpleFieldDataPacket.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		fdp.loadFromXml(in, (MartusCrypto)null);
		assertEquals("id", id, fdp.getLocalId());
		assertEquals("aTag", data1, fdp.get(aTag));
		assertEquals("bTag", data2, fdp.get(bTag));
		assertEquals("cTag", data3, fdp.get(cTag));
	}

	public void testLoadFromXmlWithAmps() throws Exception
	{
		String id = "1234567";
		String data1amp = "&lt;tag&gt;";
		String data1 = "<tag>";
		String data2amp = "&amp;&amp;";
		String data2 = "&&";
		String data3 = "'\"'\"\\";
		String simpleFieldDataPacket =
			"<FieldDataPacket>\n" +
			"<" + MartusXml.PacketIdElementName + ">" + id +
			"</" + MartusXml.PacketIdElementName + ">\n" +
			"<" + MartusXml.FieldElementPrefix + aTag + ">" +
			data1amp +
			"</" + MartusXml.FieldElementPrefix + aTag + ">\n" +
			"<" + MartusXml.FieldElementPrefix + bTag + ">" +
			data2amp +
			"</" + MartusXml.FieldElementPrefix + bTag + ">\n" +
			"<" + MartusXml.FieldElementPrefix + cTag + ">" +
			data3 +
			"</" + MartusXml.FieldElementPrefix + cTag + ">\n" +
			"</FieldDataPacket>\n";
		//System.out.println("{" + simpleFieldDataPacket + "}");

		byte[] bytes = simpleFieldDataPacket.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		fdp.loadFromXml(in, (MartusCrypto)null);
		assertEquals("id", id, fdp.getLocalId());
		assertEquals("aTag", data1, fdp.get(aTag));
		assertEquals("bTag", data2, fdp.get(bTag));
		assertEquals("cTag", data3, fdp.get(cTag));
	}

	public void testWriteXml() throws Exception
	{
		String data1 = "data 1";
		String data2base = "data 2";
		fdp.set(aTag, data1);
		fdp.set(bTag, data2base + "&<>");

		String result = writeFieldDataPacketAsXml();

		assertContains(MartusXml.getTagStart(MartusXml.FieldDataPacketElementName), result);
		assertContains(MartusXml.getTagEnd(MartusXml.FieldDataPacketElementName), result);
		assertContains(fdp.getLocalId(), result);
		assertNotContains("encrypted?", MartusXml.EncryptedFlagElementName, result);

		assertContains(aTag, result);
		assertContains(bTag, result);
		assertContains(data1, result);
		assertContains(data2base + xmlAmp + xmlLt + xmlGt, result);

		assertNotContains(fieldListForTesting, result);
		FieldCollection fields = new FieldCollection(fieldTags);
		assertContains(MartusConstants.deprecatedCustomFieldSpecs, result);
		assertContains(fields.toString(), result);
	}
	
	public void testWriteXmlCustomField() throws Exception
	{
		FieldSpecCollection specs = new FieldSpecCollection(new FieldSpec[] {LegacyCustomFields.createFromLegacy("tag,<label>")});
		FieldDataPacket fdpCustom = new FieldDataPacket(UniversalIdForTesting.createDummyUniversalId(), specs);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fdpCustom.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");
		out.close();
		
		String rawFieldList = LegacyCustomFields.buildFieldListString(specs);
		String encodedFieldList = XmlUtilities.getXmlEncoded(rawFieldList);
		assertNotContains(encodedFieldList, result);

		FieldCollection fields = new FieldCollection(specs);
		assertContains(MartusConstants.deprecatedCustomFieldSpecs, result);
		assertContains(fields.toString(), result);
	}

	public void testWriteXmlNoCustomFields() throws Exception
	{
		FieldSpecCollection specs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldDataPacket fdpCustom = new FieldDataPacket(UniversalIdForTesting.createDummyUniversalId(), specs);
		assertFalse("Should only have the default fields", fdpCustom.hasCustomFieldSpecs());
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fdpCustom.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");
		out.close();
		
		assertNotContains("Should not contain custom field spec for default fields", "<CustomFields>", result);
		assertContains(LegacyCustomFields.buildFieldListString(specs), result);
	}

	public void testWriteAndLoadGrids()throws Exception
	{
		UniversalId uid = FieldDataPacket.createUniversalId(security);
		GridData grid = TestGridData.createSampleGrid();
		String gridTag = "grid";
		FieldSpecCollection specs = new FieldSpecCollection(new FieldSpec[] {new GridFieldSpec()});
		FieldDataPacket fdpCustom = new FieldDataPacket(uid, specs);
		fdpCustom.set(gridTag, grid.getXmlRepresentation());
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fdpCustom.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");
		out.close();

		FieldDataPacket got = new FieldDataPacket(uid, fieldTags);

		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		got.loadFromXml(in, security);
		
		assertEquals(grid.getXmlRepresentation(), got.get(gridTag));
		
	}
	
	public void testWriteAndLoadXml() throws Exception
	{
		String account = fdp.getAccountId();
		String data1 = "  some  \n\n whitespace \n\n";
		String data2 = "<&>";
		String data3 = "";
		UniversalId uid1 = UniversalIdForTesting.createFromAccountAndPrefix(account, "A");
		UniversalId uid2 = UniversalIdForTesting.createFromAccountAndPrefix(account, "A");
		AttachmentProxy attach1 = new AttachmentProxy(new File("attachment 1"));
		AttachmentProxy attach2 = new AttachmentProxy(new File("attachment? 2"));
		attach1.setUniversalIdAndSessionKey(uid1, security.createSessionKey());
		attach2.setUniversalIdAndSessionKey(uid2, security.createSessionKey());
		fdp.set(aTag, data1);
		fdp.set(bTag, data2);
		fdp.set(cTag, data3);
		fdp.addAttachment(attach1);
		fdp.addAttachment(attach2);

		String result = writeFieldDataPacketAsXml();

		int attachmentUidAt = result.indexOf(MartusXml.AttachmentLocalIdElementName);
		int attachmentKeyAt = result.indexOf(MartusXml.AttachmentKeyElementName);
		int attachmentLabelAt = result.indexOf(MartusXml.AttachmentLabelElementName);
		assertNotContains(MartusXml.getTagStart(MartusXml.HQSessionKeyElementName), result);

		assertTrue("uid after label?", attachmentUidAt < attachmentLabelAt);
		assertTrue("key after label?", attachmentKeyAt < attachmentLabelAt);

		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		FieldDataPacket got = new FieldDataPacket(uid, fieldTags);

		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		got.loadFromXml(in, security);

		assertEquals("id", fdp.getLocalId(), got.getLocalId());
		assertEquals("a", fdp.get(aTag), got.get(aTag));
		assertEquals("b", fdp.get(bTag), got.get(bTag));
		assertEquals("c", fdp.get(cTag), got.get(cTag));

		AttachmentProxy[] attachments = got.getAttachments();
		assertEquals("Attachment count", 2, attachments.length);
		AttachmentProxy got1 = attachments[0];
		AttachmentProxy got2 = attachments[1];
		assertEquals("A1 label incorrect?", attach1.getLabel(), got1.getLabel());
		assertEquals("A2 label incorrect?", attach2.getLabel(), got2.getLabel());
		assertEquals("A1 uid incorrect?", uid1, got1.getUniversalId());
		assertEquals("A2 uid incorrect?", uid2, got2.getUniversalId());
		assertNotNull("A1 key null?", got1.getSessionKey());
		assertNotNull("A2 key null?", got2.getSessionKey());
		assertEquals("A1 key incorrect?", true, Arrays.equals(attach1.getSessionKey().getBytes(), got1.getSessionKey().getBytes()));
		assertEquals("A2 key incorrect?", true, Arrays.equals(attach2.getSessionKey().getBytes(), got2.getSessionKey().getBytes()));

	}

	public void testWriteAndLoadXmlEncrypted() throws Exception
	{
		fdp.setEncrypted(true);

		String data1 = "data 1";
		String data2base = "data 2";
		fdp.set(aTag, data1);
		fdp.set(bTag, data2base + "&<>");

		String result = writeFieldDataPacketAsXml();

		assertContains(MartusXml.getTagStart(MartusXml.FieldDataPacketElementName), result);
		assertContains(MartusXml.getTagEnd(MartusXml.FieldDataPacketElementName), result);
		assertContains(fdp.getLocalId(), result);
		assertContains("not encrypted?", MartusXml.EncryptedFlagElementName, result);
		assertNotContains(MartusXml.getTagStart(MartusXml.HQSessionKeyElementName), result);

		assertNotContains("encrypted data visible1?", aTag.toLowerCase(), result);
		assertNotContains("encrypted data visible2?", bTag.toLowerCase(), result);
		assertNotContains("encrypted data visible3?", data1, result);
		assertNotContains("encrypted data visible4?", data2base + xmlAmp + xmlLt + xmlGt, result);

		FieldDataPacket got = loadFieldDataPacketFromXml(result);

		assertEquals("account", fdp.getAccountId(), got.getAccountId());
		assertEquals("id", fdp.getLocalId(), got.getLocalId());
		assertEquals("a", fdp.get(aTag), got.get(aTag));
		assertEquals("b", fdp.get(bTag), got.get(bTag));
		assertEquals("encrypted", fdp.isEncrypted(), got.isEncrypted());

	}


	public void testWriteAndLoadXmlEncryptedWithHQ() throws Exception
	{
		fdp.setEncrypted(true);
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key = new HeadquartersKey(securityHQ.getPublicKeyString());
		keys.add(key);
		
		fdp.setAuthorizedToReadKeys(keys);

		String data1 = "data 1";
		String data2base = "data 2";
		fdp.set(aTag, data1);
		fdp.set(bTag, data2base + "&<>");

		String result = writeFieldDataPacketAsXml();

		assertContains(MartusXml.getTagStart(MartusXml.HQSessionKeyElementName), result);
		assertContains(MartusXml.getTagStart(AuthorizedSessionKeys.AUTHORIZED_SESSION_KEYS_TAG), result);

		UniversalId uid = UniversalIdForTesting.createFromAccountAndPrefix("other acct", "");
		FieldDataPacket got = new FieldDataPacket(uid, fdp.getFieldSpecs());
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		got.loadFromXml(in, securityHQ);

		assertEquals("account", fdp.getAccountId(), got.getAccountId());
		assertEquals("id", fdp.getLocalId(), got.getLocalId());
		assertEquals("a", fdp.get(aTag), got.get(aTag));
		assertEquals("b", fdp.get(bTag), got.get(bTag));
		assertEquals("encrypted", fdp.isEncrypted(), got.isEncrypted());

		ByteArrayInputStreamWithSeek in2 = new ByteArrayInputStreamWithSeek(bytes);
		got.loadFromXml(in2, security);
		assertEquals("account", fdp.getAccountId(), got.getAccountId());

		MartusCrypto otherSecurity = new MockMartusSecurity();
		otherSecurity.createKeyPair();
		try
		{
			ByteArrayInputStreamWithSeek in3 = new ByteArrayInputStreamWithSeek(bytes);
			got.loadFromXml(in3, otherSecurity);
			fail("Should have thrown decrption exception");
		}
		catch (MartusCrypto.DecryptionException expectedException)
		{
		}
	}

	public void testWriteAndLoadXmlEncryptedWithMultipleHQ() throws Exception
	{
		fdp.setEncrypted(true);
		MartusCrypto seconndHQ = MockMartusSecurity.createOtherServer();
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(securityHQ.getPublicKeyString());
		HeadquartersKey key2 = new HeadquartersKey(seconndHQ.getPublicKeyString());
		keys.add(key1);
		keys.add(key2);
		
		fdp.setAuthorizedToReadKeys(keys);

		String data1 = "data 1";
		String data2base = "data 2";
		fdp.set(aTag, data1);
		fdp.set(bTag, data2base + "&<>");

		String result = writeFieldDataPacketAsXml();

		assertContains(MartusXml.getTagStart(MartusXml.HQSessionKeyElementName), result);
		assertContains(MartusXml.getTagStart(AuthorizedSessionKeys.AUTHORIZED_SESSION_KEYS_TAG), result);

		UniversalId uid = UniversalIdForTesting.createFromAccountAndPrefix("other acct", "");
		FieldDataPacket got = new FieldDataPacket(uid, fdp.getFieldSpecs());
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		got.loadFromXml(in, securityHQ);

		assertEquals("account", fdp.getAccountId(), got.getAccountId());
		assertEquals("id", fdp.getLocalId(), got.getLocalId());
		assertEquals("a", fdp.get(aTag), got.get(aTag));
		assertEquals("b", fdp.get(bTag), got.get(bTag));
		assertEquals("encrypted", fdp.isEncrypted(), got.isEncrypted());

		ByteArrayInputStreamWithSeek in2 = new ByteArrayInputStreamWithSeek(bytes);
		got.loadFromXml(in2, security);
		assertEquals("account", fdp.getAccountId(), got.getAccountId());

		ByteArrayInputStreamWithSeek in3 = new ByteArrayInputStreamWithSeek(bytes);
		got.clearAll();
		got.loadFromXml(in3, seconndHQ);

		assertEquals("account", fdp.getAccountId(), got.getAccountId());
		assertEquals("id", fdp.getLocalId(), got.getLocalId());
		assertEquals("a", fdp.get(aTag), got.get(aTag));
		assertEquals("b", fdp.get(bTag), got.get(bTag));
		assertEquals("encrypted", fdp.isEncrypted(), got.isEncrypted());

		
		MartusCrypto otherSecurity = MockMartusSecurity.createOtherClient();
		try
		{
			ByteArrayInputStreamWithSeek in4 = new ByteArrayInputStreamWithSeek(bytes);
			got.loadFromXml(in4, otherSecurity);
			fail("Should have thrown decrption exception");
		}
		catch (MartusCrypto.DecryptionException expectedException)
		{
		}
	}

	public void testLoadDamaged() throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fdp.writeXml(out, security);
		byte[] bytes = out.toByteArray();
		bytes[50] ^= 255;

		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		UniversalId newUid = UniversalIdForTesting.createDummyUniversalId();
		FieldDataPacket loadedBad = new FieldDataPacket(newUid, fieldTags);
		try
		{
			loadedBad.loadFromXml(in, security);
			fail("Should have thrown!");
		}
		catch (SignatureVerificationException ignoreExpectedException)
		{
		}
		assertNotEquals("Set the uid?", fdp.getUniversalId(), loadedBad.getUniversalId());
	}
	
	
	public void testInvalidXFormsChildrenValues() throws Exception
	{
		verifyInvalidXForms(null, null);
		verifyInvalidXForms("", "");
		verifyInvalidXForms(getXFormsModelAsXmlString(), null);
		verifyInvalidXForms(getXFormsModelAsXmlString(), "");
		verifyInvalidXForms(null, getXFormsInstanceAsXmlString());
		verifyInvalidXForms("", getXFormsInstanceAsXmlString());
	}
	
	private void verifyInvalidXForms(String rawXFormsModelXmlAsString, String rawXFormsInstanceAsString) throws Exception
	{
		fdp.setXFormsModelAsString(rawXFormsModelXmlAsString);
		fdp.setXFormsInstanceAsString(rawXFormsInstanceAsString);
		
		String result = writeFieldDataPacketAsXml();

		assertNotContains(MartusXml.getTagStart(MartusXml.XFormsElementName), result);
		assertNotContains(MartusXml.getTagStart(MartusXml.XFormsModelElementName), result);
		assertNotContains(MartusXml.getTagStart(MartusXml.XFormsInstanceElementName), result);
	}

	public void testXForms() throws Exception
	{
		String rawXFormsModelXmlAsString = getXFormsModelAsXmlString();
		fdp.setXFormsModelAsString(rawXFormsModelXmlAsString);
		
		String rawXFormsInstanceAsString = getXFormsInstanceAsXmlString();
		fdp.setXFormsInstanceAsString(rawXFormsInstanceAsString);
		
		String result = writeFieldDataPacketAsXml();

		assertContains(MartusXml.getTagStart(MartusXml.XFormsElementName), result);

		FieldDataPacket got = loadFieldDataPacketFromXml(result);

		String actualXFormsModelXmlAsString = got.getXFormsModelAString();
		verifyNonEmptyXFormsValue(actualXFormsModelXmlAsString);
		
		String actualXFormsInstanceAsString = got.getXFormsInstanceAsString();
		verifyNonEmptyXFormsValue(actualXFormsInstanceAsString);
		
		Document expectedXFormsModelDocument = convertXmlToDocument(rawXFormsModelXmlAsString);
		verifyEqualDocuments(expectedXFormsModelDocument, actualXFormsModelXmlAsString);
		
		Document expectedXFormsInstanceDocument = convertXmlToDocument(rawXFormsInstanceAsString);
		verifyEqualDocuments(expectedXFormsInstanceDocument, actualXFormsInstanceAsString);
	}
	
	public void testContainsXForms() throws Exception
	{
		verifyContainsXForms(false, "", "");
		verifyContainsXForms(false, null, null);
		verifyContainsXForms(false, null, "");
		verifyContainsXForms(false, "", null);
		verifyContainsXForms(false, getXFormsModelAsXmlString(), "");
		verifyContainsXForms(false, getXFormsModelAsXmlString(), null);
		verifyContainsXForms(false, "", getXFormsInstanceAsXmlString());
		verifyContainsXForms(false, null, getXFormsInstanceAsXmlString());
		verifyContainsXForms(true, getXFormsModelAsXmlString(), getXFormsInstanceAsXmlString());
	}
	
	private void verifyContainsXForms(boolean expectedValue, String xFormsModelXmlAsString, String xFormsInstanceXmlAsString) 
	{
		fdp.setXFormsModelAsString(xFormsModelXmlAsString);
		fdp.setXFormsInstanceAsString(xFormsInstanceXmlAsString);
		assertEquals("XForms data not expected?", expectedValue, fdp.containXFormsData());
	}

	private void verifyNonEmptyXFormsValue(String valueToAssert)
	{
		assertFalse("Did not load xforms?", valueToAssert.isEmpty());
	}
	
	private void verifyEqualDocuments(Document expectedXFormsDocument, String actualXmlAsString) throws Exception
	{
		Document actualXFormsDocument = convertXmlToDocument(actualXmlAsString);
		assertTrue("XForms documents are not equal?", expectedXFormsDocument.isEqualNode(actualXFormsDocument));
	}

	private Document convertXmlToDocument(String xmlAsString) throws Exception
	{
		StringReader stringReader = new StringReader(xmlAsString);
		InputSource inputStream = new InputSource(stringReader);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		
		return  documentBuilder.parse(inputStream);
	}
	
	private FieldDataPacket loadFieldDataPacketFromXml(String result) throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createFromAccountAndPrefix("other acct", "");
		FieldDataPacket got = new FieldDataPacket(uid, fdp.getFieldSpecs());
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		got.loadFromXml(in, security);
		
		return got;
	}
	
	private String writeFieldDataPacketAsXml() throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fdp.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");
		out.close();

		return result;
	}

	void verifyLoadException(byte[] input, Class expectedExceptionClass)
	{
		ByteArrayInputStreamWithSeek inputStream = new ByteArrayInputStreamWithSeek(input);
		try
		{
			UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
			FieldDataPacket loaded = new FieldDataPacket(uid, fieldTags);
			loaded.loadFromXml(inputStream, security);
			fail("Should have thrown " + expectedExceptionClass.getName());
		}
		catch(Exception e)
		{
			assertEquals("Wrong exception type?", expectedExceptionClass, e.getClass());
		}
	}
	
	private String getXFormsModelAsXmlString()
	{
		return 
				"<xforms_model>" +
				"<h:html " +
				"xmlns=\"http://www.w3.org/2002/xforms\" " +
			    "xmlns:h=\"http://www.w3.org/1999/xhtml\" " +
			    "xmlns:ev=\"http://www.w3.org/2001/xml-events\" " +
			    "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
			    "xmlns:jr=\"http://openrosa.org/javarosa\" "
			    + ">" +
			    "<h:head>" +
			        "<h:title>Vital Voices Secure App</h:title>" +
			        "<model>" +
			            "<instance>" +
			                "<nm id=\"VitalVoices\">" +
			                    "<incidentCount/>" +
			                "</nm>" +
			            "</instance>" +
			            "<bind nodeset=\"/nm/incidentCount\" type=\"string\" />" +
			        "</model>" +
			    "</h:head>" +
			    "<h:body>" +
			        "<group appearance=\"field-list\">" +
			        	"<label>Group</label>" +
			        		"<input ref=\"incidentCount\">" +
			        		"<label>How many times?</label>" +
			        	"</input>" +
			        "</group>" +
			    "</h:body>" +
			"</h:html>" +
			"</xforms_model>";	
	}
	
	private String getXFormsInstanceAsXmlString()
	{
		return "<xforms_instance> " +
				"<nm id=\"VitalVoices\"> " +
				"<incidentCount>5</incidentCount> " +
				"</nm>" +
				"</xforms_instance> ";
	}

	String line1 = "This";
	String line2 = "is";
	String line3 = "data";
	String line4 = "for b";

	FieldDataPacket fdp;
	String xmlAmp = "&amp;";
	String xmlLt = "&lt;";
	String xmlGt = "&gt;";
	static String aTag = "aMonte";
	static String bTag = "Blue";
	static String cTag = "cSharp";
	String aData = "data for a";
	String bData = line1 + "\n" + line2 + "\r\n" + line3 + "\n" + line4;
	String cData = "after b";
	FieldSpecCollection fieldTags = new FieldSpecCollection(new FieldSpec[]
	{
		LegacyCustomFields.createFromLegacy(aTag),
		LegacyCustomFields.createFromLegacy(bTag),
		LegacyCustomFields.createFromLegacy(cTag)
	});

	int SHORTEST_LEGAL_KEY_SIZE = 512;
	static MartusCrypto security;
	static MartusCrypto securityHQ;
	static final String fieldListForTesting = "<FieldList>" + aTag + ";" + bTag + ";" + cTag + "</FieldList>";
}
