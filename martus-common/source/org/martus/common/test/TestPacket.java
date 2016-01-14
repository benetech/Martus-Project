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
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusXml;
import org.martus.common.VersionBuildDate;
import org.martus.common.XmlWriterFilter;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.WrongAccountException;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;


public class TestPacket extends TestCaseEnhanced
{
    public TestPacket(String name)
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
    }

    public void testCreateLocalId() throws Exception
    {
    	String prefix = "d-";
    	String local1 = Packet.createLocalId(security, prefix);
    	assertStartsWith(prefix, local1);
    	assertEquals(26, local1.length());
    	String local2 = Packet.createLocalId(security, prefix);
    	assertNotEquals(local1, local2);
    	assertNotContains("Should not contain a /", "/", local1);
    	assertNotContains("Should not contain a =", "=", local1);
    	assertNotContains("Local 2 Should not contain a /", "/", local2);
    	assertNotContains("Local 2 Should not contain a =", "=", local2);
    	
    }
    
	public void testIsValidVersion()
	{
		assertFalse("Valid null comment?", Packet.isValidStartComment(null));
		assertFalse("No End Comment is valid?", Packet.isValidStartComment(MartusXml.packetStartCommentStart));
		assertFalse("No Start Comment is valid?", Packet.isValidStartComment(MartusXml.packetStartCommentEnd));

		String noVersion = MartusXml.packetStartCommentStart + MartusXml.packetStartCommentEnd;
		assertTrue("no version # is valid for backward compatability.", Packet.isValidStartComment(noVersion));
	}

	public void testWriteXmlToStream() throws Exception
	{
		Packet packet = new Packet();
		try
		{
			packet.writeXml((OutputStream)null, null);
			fail("Should have been an exception");
		}
		catch(Exception e)
		{
			//Expected Exception
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		packet.writeXml(out, security);
		out.close();
		byte[] bytes = out.toByteArray();

		String result = new String(bytes, "UTF-8");
		assertStartsWith(MartusXml.packetStartCommentStart, result);

		int startCommentLength = MartusXml.packetStartCommentStart.length();
		int endCommentPosition = result.indexOf(MartusXml.packetStartCommentEnd);
		assertTrue("No end startComment?", endCommentPosition >= startCommentLength);
		String version = result.substring(startCommentLength, endCommentPosition);
		String fullExpectedVersion = VersionBuildDate.getVersionBuildDate() + MartusXml.packetFormatVersion; 
		assertEquals("Invalid Version", fullExpectedVersion, version);

		assertContains(packet.getLocalId(), result);
		assertContains(packet.getAccountId(), result);
		assertContains(MartusXml.packetSignatureStart, result);
		assertContains(MartusXml.packetSignatureEnd, result);
		//System.out.println(result);

		String newLine = "\n";
		int sigCommentIndex = result.indexOf(MartusXml.packetSignatureStart);
		int sigCommentEndLen = MartusXml.packetSignatureEnd.length();
		int sigCommentEndIndex = bytes.length - MartusXml.packetSignatureEnd.length() - newLine.length();
		int sigCommentLen = sigCommentEndIndex - sigCommentIndex;

		String sigComment = new String(bytes, sigCommentIndex, sigCommentLen + sigCommentEndLen, "UTF-8");
		//System.out.println(sigComment);
		assertStartsWith("bad sig start?", MartusXml.packetSignatureStart, sigComment);
		assertEndsWith("bad sig end?", MartusXml.packetSignatureEnd, sigComment);
		int sigIndex = MartusXml.packetSignatureStart.length();
		int sigEndIndex = sigComment.length() - MartusXml.packetSignatureEnd.length();
		sigComment.substring(sigIndex, sigEndIndex);
	}
	
	public void testWriteXmlToWriter() throws Exception
	{
		Packet packet = new Packet();
		try
		{
			packet.writeXml((Writer)null, null);
			fail("Should have been an exception");
		}
		catch(Exception e)
		{
			//Expected Exception
		}
		StringWriter writer = new StringWriter();
		packet.writeXml(writer, security);
		String result = writer.toString();
		assertStartsWith(MartusXml.packetStartCommentStart, result);
		assertContains(packet.getLocalId(), result);
		assertContains(packet.getAccountId(), result);
	}

	public void testWriteAndLoadUtf8() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		String utf8Data = "????";
		bhp.setFieldDataPacketId(utf8Data);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		bhp.setFieldDataPacketId("");
		byte[] bytes = out.toByteArray();
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		bhp.loadFromXml(in, null, security);

		assertEquals("utf-8 damaged?", utf8Data, bhp.getFieldDataPacketId());
	}

	public void testLoadMoreSpecificPacketType() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		bhp.setFieldDataPacketId("none");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] sig = bhp.writeXml(out, security);

		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(out.toByteArray());
		Packet.validateXml(in, security.getPublicKeyString(), bhp.getLocalId(), sig, security);
		try
		{
			ByteArrayInputStreamWithSeek in2 = new ByteArrayInputStreamWithSeek(out.toByteArray());
			Packet.validateXml(in2, security.getPublicKeyString(), "123444", null, security);
			fail("Didn't throw for bad localid?");
		}
		catch (Packet.InvalidPacketException expectedException)
		{
		}

		try
		{
			sig[sig.length/2] ^= 0xFF;
			ByteArrayInputStreamWithSeek in2 = new ByteArrayInputStreamWithSeek(out.toByteArray());
			Packet.validateXml(in2, security.getPublicKeyString(), bhp.getLocalId(), sig, security);
			fail("Didn't throw for bad sig?");
		}
		catch (Packet.SignatureVerificationException expectedException)
		{
		}
	}

	public void testCorruptedXML() throws Exception
	{
		class CorruptedBhp extends BulletinHeaderPacket
		{
			public CorruptedBhp(MartusCrypto accountSecurity)
			{
				super(createUniversalId(accountSecurity));
			}

			protected void internalWriteXml(XmlWriterFilter dest) throws IOException
			{
				dest.writeDirect("<");
				super.internalWriteXml(dest);
			}
		}

		CorruptedBhp bhp = new CorruptedBhp(security);
		bhp.setFieldDataPacketId("none");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		try
		{
			ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(out.toByteArray());
			Packet.validateXml(in, security.getPublicKeyString(), bhp.getLocalId(), null, security);
			fail("Didn't throw for bad xml?");
		}
		catch(Packet.InvalidPacketException expectedException)
		{
		}
	}
	
	public void testValidatePacketWithWrongAccountId() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(out.toByteArray());
		try
		{
			Packet.validateXml(in, "Bad account", bhp.getLocalId(), null, security);
			fail("Should have thrown for wrong account");
		}
		catch (WrongAccountException ignoreExpectedException)
		{
		}
	}

	public void testValidatePacketWithWrongLocalId() throws Exception
	{
		FieldSpecCollection specs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		UniversalId uid = FieldDataPacket.createUniversalId(security);
		FieldDataPacket fdp = new FieldDataPacket(uid, specs);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fdp.writeXml(out, security);

		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(out.toByteArray());
		try
		{
			Packet.validateXml(in, fdp.getAccountId(), "F-bad local id", null, security);
			fail("Should have thrown for wrong local id");
		}
		catch (Packet.InvalidPacketException ignoreExpectedException)
		{
		}
	}

	public void testVerifyPacketWithNonPacketData() throws Exception
	{
		byte[] invalidBytes = {1,2,3};
		ByteArrayInputStreamWithSeek inInvalid = new ByteArrayInputStreamWithSeek(invalidBytes);
		try
		{
			Packet.verifyPacketSignature(inInvalid, security);
			fail("invalidBytes should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException e)
		{
			// expected exception
		}
	}

	public void testVerifyGoodPacket() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		bhp.setPrivateFieldDataPacketId("Jos"+UnicodeConstants.ACCENT_E_LOWER+"e");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		byte[] bytes = out.toByteArray();
		ByteArrayInputStreamWithSeek in0 = new ByteArrayInputStreamWithSeek(bytes);
		Packet.verifyPacketSignature(in0, security);
		assertEquals("UTF", "Jos"+UnicodeConstants.ACCENT_E_LOWER+"e", bhp.getPrivateFieldDataPacketId());
	}

	public void testVerifyGoodPacketWithAnotherAccount() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		byte[] bytes = out.toByteArray();
		ByteArrayInputStreamWithSeek in0 = new ByteArrayInputStreamWithSeek(bytes);
		MartusCrypto security2 = MockMartusSecurity.createOtherClient();
		Packet.verifyPacketSignature(in0, security2);
	}

	public void testVerifyPacketWithCorruptedStartComment() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		byte[] bytes = out.toByteArray();
		bytes[5] ^= 0xFF;

		ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(bytes);
		try
		{
			Packet.verifyPacketSignature(in1, security);
			fail("verifyPacketSignature Should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException ignoreThisExpectedException)
		{
		}
	}

	public void testVerifyPacketWithCorruptedSignatureComment() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		byte[] bytes = out.toByteArray();
		String xml = new String(bytes, "UTF-8");
		assertEquals("unicode in the sample?", bytes.length, xml.length());
		int sigStart = xml.indexOf(MartusXml.packetSignatureStart);

		int corruptSigStartAt = sigStart + 1;
		try
		{
			bytes[corruptSigStartAt] ^= 0xFF;
			ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(bytes);
			Packet.verifyPacketSignature(in1, security);
			fail("corrupted sigstart should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException ignoreThisExpectedException)
		{
			bytes[corruptSigStartAt] ^= 0xFF;
		}

		int corruptSigEndAt = bytes.length - 2;
		try
		{
			bytes[corruptSigEndAt] ^= 0xFF;
			ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(bytes);
			Packet.verifyPacketSignature(in1, security);
			fail("corrupted sigend should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException ignoreThisExpectedException)
		{
		}
	}

	public void testVerifyPacketWithCorruptedData() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		byte[] bytes = out.toByteArray();

		int corruptDataAt = MartusXml.packetStartCommentStart.length() +
							20 + // allow for build date
							MartusXml.packetFormatVersion.length() + 
							MartusXml.packetStartCommentEnd.length() + 15;
		try
		{
			bytes[corruptDataAt] ^= 0xFF;
			ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(bytes);
			Packet.verifyPacketSignature(in1, security);
			fail("corrupted data should have thrown SignatureVerificationException");
		}
		catch(Packet.SignatureVerificationException ignoreThisExpectedException)
		{
		}
	}

	public void testVerifyPacketWithCorruptedSignature() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		byte[] bytes = out.toByteArray();

		int corruptSigAt = bytes.length - MartusXml.packetSignatureEnd.length() - 10;
		try
		{
			bytes[corruptSigAt] = ' ';
			ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(bytes);
			Packet.verifyPacketSignature(in1, security);
			fail("corrupted data should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException ignoreThisExpectedException)
		{
		}
	}

	public void testVerifyPacketWithNoAccountTag() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		byte[] oldBytes = out.toByteArray();
		String oldXml = new String(oldBytes,"UTF-8");

		//String newXml =	oldXml.replaceFirst(MartusXml.AccountElementName,"xxy");
		// rewrite above line in java 1.3 compatible form:
		String newXml = oldXml; // first assume no match found
		int idx = oldXml.indexOf(MartusXml.AccountElementName);
		if (idx >= 0)
			newXml = oldXml.substring(0, idx) + "xxy" + oldXml.substring(idx+MartusXml.AccountElementName.length());

		byte[] newBytes = newXml.getBytes("UTF-8");

		ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(newBytes);
		try
		{
			Packet.verifyPacketSignature(in1, security);
			fail("verifyPacketSignature Should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException ignoreThisExpectedException)
		{
		}
	}

	public void testVerifyPacketWithBadAccountElement() throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		byte[] oldBytes = out.toByteArray();
		String tagEnd = MartusXml.getTagEnd(MartusXml.AccountElementName);
		String oldXml = new String(oldBytes,"UTF-8");
		String newXml =	oldXml.replaceFirst(tagEnd, "\n" + tagEnd);
		byte[] newBytes = newXml.getBytes("UTF-8");

		ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(newBytes);
		try
		{
			Packet.verifyPacketSignature(in1, security);
			fail("verifyPacketSignature Should have thrown InvalidPacketException");
		}
		catch(Packet.InvalidPacketException e)
		{
			// expected exception
		}
	}

	public void testLoadFromEmptyStream() throws Exception
	{
		Class expected = new Packet.InvalidPacketException("a").getClass();
		verifyLoadException(new byte[0], expected);
	}

	public void testLoadFromWrongPacketType() throws Exception
	{
		SimplePacketSubtype packet = new SimplePacketSubtype();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		packet.writeXml(out, security);

		byte[] wrongType = out.toByteArray();
		ByteArrayInputStreamWithSeek inputStream = new ByteArrayInputStreamWithSeek(wrongType);
		try
		{
			AnotherSimplePacketSubtype loaded = new AnotherSimplePacketSubtype();
			loaded.loadFromXml(inputStream, null, security);
			fail("Should have thrown WrongPacketTypeException");
		}
		catch(Packet.WrongPacketTypeException e)
		{
			// expected exception
		}
	}

	public void testLoadFromInvalidPacket() throws Exception
	{
		Class expected = new Packet.InvalidPacketException("a").getClass();
		verifyLoadException(new byte[] {1,2,3}, expected);

		String xmlError = "<" + MartusXml.PacketElementName + ">" +
					"</a></" + MartusXml.PacketElementName + ">";
		byte[] xmlErrorBytes = xmlError.getBytes();
		verifyLoadException(xmlErrorBytes, expected);
	}

	void verifyLoadException(byte[] input, Class expectedExceptionClass)
	{
		ByteArrayInputStreamWithSeek inputStream = new ByteArrayInputStreamWithSeek(input);
		try
		{
			Packet.validateXml(inputStream, security.getPublicKeyString(), "", null, security);
			fail("Should have thrown " + expectedExceptionClass.getName());
		}
		catch(Exception e)
		{
			assertEquals("Wrong exception type?", expectedExceptionClass, e.getClass());
		}
	}

	class SimplePacketSubtype extends Packet
	{
		SimplePacketSubtype()
		{
			super(UniversalIdForTesting.createFromAccountAndPrefix(security.getPublicKeyString(), ""));
		}

		protected String getPacketRootElementName()
		{
			return "BogusPacket";
		}

	}

	class AnotherSimplePacketSubtype extends Packet
	{
		AnotherSimplePacketSubtype()
		{
			super(UniversalIdForTesting.createFromAccountAndPrefix(security.getPublicKeyString(), ""));
		}

		protected String getPacketRootElementName()
		{
			return "AnotherBogusPacket";
		}

	}

	static MartusCrypto security;
}
