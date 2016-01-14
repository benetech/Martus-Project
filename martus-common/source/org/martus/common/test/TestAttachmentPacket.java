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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Arrays;

import org.martus.common.MartusXml;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.crypto.SessionKey;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.XmlBase64Exporter;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.xml.SimpleXmlParser;




public class TestAttachmentPacket extends TestCaseEnhanced
{

	public TestAttachmentPacket(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(tempFile == null)
		{
			tempFile = createTempFileFromName("$$$MartusTestAttIn");
			FileOutputStream out = new FileOutputStream(tempFile);
			out.write(sampleBytes);
			out.close();
		}
		if(security == null)
		{
			security = MockMartusSecurity.createClient();
		}
	}

	public void testCreateUniversalId()
	{
		UniversalId uid = AttachmentPacket.createUniversalId(security);
		assertEquals("account", security.getPublicKeyString(), uid.getAccountId());
		assertStartsWith("prefix", "A-", uid.getLocalId());
	}

	public void testXmlBase64Exporter() throws Exception
	{
		String baseTag = MartusXml.AttachmentBytesElementName;
		String xml = MartusXml.getTagStart(baseTag) +
					StreamableBase64.encode(sampleBytes) + "\n" + 
					StreamableBase64.encode(sampleBytes) + "\n" + 
					MartusXml.getTagEnd(baseTag);
					
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XmlBase64Exporter exporter = new XmlBase64Exporter(baseTag, out);
		SimpleXmlParser.parse(exporter, new StringReader(xml));
		byte[] result = out.toByteArray();
		assertEquals(sampleBytes.length*2, result.length);
	}
	
	public void testCreateXmlFromFile() throws Exception
	{
		String account = security.getPublicKeyString();
		AttachmentProxy a = new AttachmentProxy(tempFile);
		AttachmentPacket ap = new AttachmentPacket(account, security.createSessionKey(), a.getFile(), security);
		assertNotNull("null packet?", ap);
		assertEquals("account", account, ap.getAccountId());

		ByteArrayOutputStream dest = new ByteArrayOutputStream();
		ap.writeXml(dest, security);
		byte[] resultBytes = dest.toByteArray();
		String result = new String(resultBytes);
		assertContains("tag start", MartusXml.getTagStart(MartusXml.AttachmentBytesElementName), result);
		assertNotContains("data", StreamableBase64.encode(sampleBytes), result);
		assertContains("tag end", MartusXml.getTagEnd(MartusXml.AttachmentBytesElementName), result);
	}

	public void testCreateFileFromXml() throws Exception
	{
		String account = security.getPublicKeyString();
		SessionKey sessionKey = security.createSessionKey();
		AttachmentProxy a = new AttachmentProxy(tempFile);
		AttachmentPacket ap1 = new AttachmentPacket(account, sessionKey, a.getFile(), security);
		ByteArrayOutputStream dest = new ByteArrayOutputStream();
		ap1.writeXml(dest, security);
		byte[] resultBytes = dest.toByteArray();

		File destFile = createTempFileFromName("$$$MartusTestAttOut");
		destFile.delete();
		ByteArrayInputStreamWithSeek inBytes = new ByteArrayInputStreamWithSeek(resultBytes);
		FileOutputStream out = new FileOutputStream(destFile);
		AttachmentPacket.exportRawFileFromXml(inBytes, sessionKey, security, out);
		out.close();
		inBytes.close();

		assertTrue("not created?", destFile.exists());
		assertEquals("length?", sampleBytes.length, destFile.length());
		byte[] fileBytes = new byte[sampleBytes.length];
		FileInputStream inFile = new FileInputStream(destFile);
		inFile.read(fileBytes);
		inFile.close();
		assertEquals("bad data?", true, Arrays.equals(sampleBytes, fileBytes));

		destFile.delete();
	}

	public void testEncrypted() throws Exception
	{
		String account = security.getPublicKeyString();
		SessionKey sessionKey = security.createSessionKey();
		AttachmentProxy a = new AttachmentProxy(tempFile);
		AttachmentPacket ap = new AttachmentPacket(account, sessionKey, a.getFile(), security);

		ByteArrayOutputStream encryptedDest = new ByteArrayOutputStream();
		ap.writeXml(encryptedDest, security);
		byte[] encryptedBytes = encryptedDest.toByteArray();
		String encryptedResult = new String(encryptedBytes);
		assertNotContains("Encrypted data", StreamableBase64.encode(sampleBytes), encryptedResult);

		security.clearKeyPair();

		File decryptedFile = createTempFileFromName("$$$MartusDecryptedAtt");
		ByteArrayInputStreamWithSeek xmlIn = new ByteArrayInputStreamWithSeek(encryptedBytes);
		FileOutputStream out = new FileOutputStream(decryptedFile);
		AttachmentPacket.exportRawFileFromXml(xmlIn, sessionKey, security, out);
		out.close();

		byte[] fileBytes = new byte[sampleBytes.length];
		FileInputStream inFile = new FileInputStream(decryptedFile);
		inFile.read(fileBytes);
		inFile.close();
		assertEquals("bad data?", true, Arrays.equals(sampleBytes, fileBytes));

		security = null;
		
		decryptedFile.delete();
	}

	public void testLargeAttachmentSpeed() throws Exception
	{
		long createRawStartedAt = System.currentTimeMillis();

		final int SIZE = 100 * 1024;
		File largeFile = createTempFileFromName("$$$MartusTestLargeAtt");
		FileOutputStream rawOut = new FileOutputStream(largeFile);
		BufferedOutputStream out = new BufferedOutputStream(rawOut);
		for(int i = 0; i < SIZE; ++i)
		{
			out.write(i % 123);
		}
		out.close();
		long createRawEndedAt = System.currentTimeMillis();
		assertTrue("Create file took too long", createRawEndedAt - createRawStartedAt < 2000);

		//long writeXmlStartedAt = System.currentTimeMillis();
		String account = security.getPublicKeyString();
		AttachmentProxy a = new AttachmentProxy(largeFile);
		SessionKey sessionKey = security.createSessionKey();
		AttachmentPacket ap = new AttachmentPacket(account, sessionKey, a.getFile(), security);

		File largeXmlFile = createTempFileFromName("$$$MartusTestLargeXmlFile");
		FileOutputStream dest = new FileOutputStream(largeXmlFile);
		ap.writeXml(dest, security);
		//long writeXmlEndedAt = System.currentTimeMillis();
		//System.out.println("Write Xml Time = " + (writeXmlEndedAt - writeXmlStartedAt));
		//assertTrue("Write Xml took too long", writeXmlEndedAt - writeXmlStartedAt < 10000);

		//long verifySigStartedAt = System.currentTimeMillis();
		FileInputStreamWithSeek verifyIn = new FileInputStreamWithSeek(largeXmlFile);
		AttachmentPacket.verifyPacketSignature(verifyIn, security);
		verifyIn.close();
		//long verifySigEndedAt = System.currentTimeMillis();
		//System.out.println("Verify Sig Time = " + (verifySigEndedAt - verifySigStartedAt));
		//assertTrue("verifySig took too long", verifySigEndedAt - verifySigStartedAt < 20000);

		//long readXmlStartedAt = System.currentTimeMillis();
		File decryptedFile = createTempFileFromName("$$$MartusDecryptedBufferedAtt");
		FileInputStreamWithSeek xmlIn = new FileInputStreamWithSeek(largeXmlFile);
		FileOutputStream finalOut = new FileOutputStream(decryptedFile);
		AttachmentPacket.exportRawFileFromXml(xmlIn, sessionKey, security, finalOut);
		finalOut.close();
		xmlIn.close();
		//long readXmlEndedAt = System.currentTimeMillis();
		//System.out.println("Read Xml Time = " + (readXmlEndedAt - readXmlStartedAt));
		//assertTrue("Read Xml took too long", readXmlEndedAt - readXmlStartedAt < 30000);

		largeFile.delete();
		largeXmlFile.delete();
		decryptedFile.delete();
	}

/*
 * TODO see if any of these tests are still valid
	public void setUp() throws Exception
	{
		FileAttachmentProxy original = new FileAttachmentProxy(sampleLabel, tempFile);
		ap = AttachmentPacket.createFromAttachment(security.getPublicKeyString(), security, original);
	}

	public void testBasics() throws Exception
	{
		assertEquals("label", sampleLabel, ap.getLabel());
		assertTrue("bytes", Arrays.equals(sampleBytes, ap.getBytes()));
	}

	public void testLoadFromXmlSimple() throws Exception
	{
		String id = "1234567";
		String simplePacket =
			"<" + MartusXml.AttachmentPacketElementName + ">\n" +
			"<" + MartusXml.PacketIdElementName + ">" + id +
			"</" + MartusXml.PacketIdElementName + ">\n" +
			"<" + MartusXml.AccountElementName + ">" + security.getPublicKeyString() +
			"</" + MartusXml.AccountElementName + ">\n" +
			"<" + MartusXml.AttachmentLabelElementName + ">" + sampleLabel +
			"</" + MartusXml.AttachmentLabelElementName + ">\n" +
			"<" + MartusXml.AttachmentBytesElementName + ">" + Base64.encode(sampleBytes) +
			"</" + MartusXml.AttachmentBytesElementName + ">\n" +
			"</" + MartusXml.AttachmentPacketElementName + ">\n";
		//System.out.println("{" + simplePacket + "}");

		byte[] bytes = simplePacket.getBytes("UTF-8");
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		AttachmentPacket got = AttachmentPacket.createFromXmlStream(in, (MartusCrypto)null);
		assertEquals("account", security.getPublicKeyString(), got.getAccountId());
		assertEquals("id", id, got.getPacketId());
		assertEquals("label", sampleLabel, got.getLabel());
		assertTrue("bytes", Arrays.equals(sampleBytes, got.getBytes()));
	}

	public void testWriteXml() throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ap.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");

		assertContains(MartusXml.getTagStart(MartusXml.AttachmentPacketElementName), result);
		assertContains(MartusXml.getTagEnd(MartusXml.AttachmentPacketElementName), result);
		assertContains(ap.getPacketId(), result);

		assertContains("label", sampleLabel, result);
		assertContains("bytes", Base64.encode(sampleBytes), result);
	}

*/
	static File tempFile;
	byte[] sampleBytes = {1,1,2,0,3,5,127,7,11};
	static MartusCrypto security;
}
