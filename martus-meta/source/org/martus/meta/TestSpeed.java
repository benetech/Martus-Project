/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003-2007, Beneficent
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

package org.martus.meta;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestSuite;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.SessionKey;
import org.martus.common.database.ClientFileDatabase;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.util.Stopwatch;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;


public class TestSpeed extends TestCaseEnhanced 
{
    public TestSpeed(String name) throws Exception
	{
        super(name);
    }

	public static void main (String[] args) 
	{
		runTests();
	}

	public static void runTests () 
	{
		junit.textui.TestRunner.run (new TestSuite(TestSpeed.class));
	}
	
	public void print(String task, long time)
	{
		float timeInSeconds = (float)time / 1000;
		System.out.println(task + ": " + timeInSeconds + " sec");
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		if(security == null)
		{
			security = new MartusSecurity();


			boolean createNewKeyPair = false;
			
			if(createNewKeyPair)
			{
				Stopwatch t = new Stopwatch();
				security.createKeyPair();
				print("Create key pair", t.stop());
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				security.writeKeyPair(out, password);
				System.out.println(StreamableBase64.encode(out.toByteArray()));
			}
			else
			{
				InputStream in = new ByteArrayInputStream(StreamableBase64.decode(keyPair));
				security.readKeyPair(in, password);
			}
		}
		
	}
	
	public void testDigests() throws Exception
	{
		timeDigest(1);
		timeDigest(1);
		timeDigest(2 * 1024);
		timeDigest(100 * 1024);
		timeDigest(5 * 1024 * 1024);
	}
	
	void timeDigest(int dataSize) throws Exception
	{
		String data = createRandomString(dataSize);
		
		Stopwatch digestTimer = new Stopwatch();
		MartusCrypto.createDigestString(data);
		print("Digest " + data.length() + " characters of data", digestTimer.stop());
	}
	
	
	public void testSignatures() throws Exception
	{
		timeSignatureCreateAndVerify(1);
		timeSignatureCreateAndVerify(1);
		timeSignatureCreateAndVerify(2 * 1024);
		timeSignatureCreateAndVerify(100 * 1024);
		timeSignatureCreateAndVerify(5 * 1024 * 1024);
	}

	void timeSignatureCreateAndVerify(int dataSize) throws Exception
	{
		byte[] data = createRandomBytes(dataSize);
		
		InputStream in = new ByteArrayInputStream(data);
		Stopwatch signer = new Stopwatch();
		byte[] sig = security.createSignatureOfStream(in);
		print("Sign   " + data.length + " bytes of data", signer.stop());
		
		security.verifySignature(in, sig);
		print("Verify " + data.length + " bytes of data", signer.stop());
	}
	
	public void testEncryptDecrypt() throws Exception
	{
		timeEncryptDecrypt(1);
		timeEncryptDecrypt(1);
		timeEncryptDecrypt(2 * 1024);
		timeEncryptDecrypt(100 * 1024);
		timeEncryptDecrypt(5 * 1024 * 1024);
	}

	void timeEncryptDecrypt(int longDataSize) throws Exception
	{
		String longData = createRandomString(longDataSize);
		
		InputStream plainIn = new StringInputStreamWithSeek(longData);
		ByteArrayOutputStream cipherOut = new ByteArrayOutputStream();
		
		Stopwatch t = new Stopwatch();
		security.encrypt(plainIn, cipherOut);
		print("Encrypt " + longDataSize + " bytes of data", t.stop());
		
		InputStreamWithSeek cipherIn = new ByteArrayInputStreamWithSeek(cipherOut.toByteArray());
		OutputStream plainOut = new ByteArrayOutputStream();
		
		Stopwatch decryptTimer = new Stopwatch();
		security.decrypt(cipherIn, plainOut);
		print("Decrypt " + longDataSize + " bytes of data", decryptTimer.stop());
	}

	String createRandomString(int longDataSize) 
	{
		StringBuffer rawLongData = new StringBuffer(longDataSize);
		for(int i=0; i < rawLongData.capacity(); ++i)
			rawLongData.append('a');
		return new String(rawLongData);
	}
	
	public void testBase64() throws Exception
	{
		byte[] data = createRandomBytes(100 * 1024);

		Stopwatch encoder = new Stopwatch();
		String encoded = StreamableBase64.encode(data);
		print("Base64 encode " + data.length + " bytes of data", encoder.stop());
		
		Stopwatch decoder = new Stopwatch();
		StreamableBase64.decode(encoded);
		print("Base64 decode " + data.length + " bytes of data", decoder.stop());
	}

	byte[] createRandomBytes(int dataSize) 
	{
		byte[] data = new byte[dataSize];
		for(int i = 0; i < data.length; ++i)
			data[i] = (byte)i;
		return data;
	}
	
	public void testGetBytes() throws Exception
	{
		byte[] data = createRandomBytes(100);
		String base64 = StreamableBase64.encode(data);
		
		final int count = 100000;
		Stopwatch getBytesTimer = new Stopwatch();
		for(int i = 0; i < count; ++i)
			base64.getBytes("UTF-8");
		print("getBytes UTF-8 " + count + " iterations of " + data.length + " bytes of data", getBytesTimer.stop());
	}
	
	public void testReadLine() throws Exception
	{
		timeReader(1024);
		timeReader(100 * 1024);
		timeReader(50 * 1024 * 1024);
	}

	void timeReader(int fileLength) throws Exception, IOException
	{
		// This test may not be valid, because the packet verifier 
		// uses db.openInputStream instead of creating a UnicodeReader
		File bigFile = createRandomTextFile(fileLength);
		UnicodeReader reader = new UnicodeReader(bigFile);
		
		Stopwatch readerTimer = new Stopwatch();
		while(reader.readLine() != null)
			;
		print("reader " + bigFile.length() + " bytes of data", readerTimer.stop());
		reader.close();
		bigFile.delete();
	}
	
	public void testXmlPlain() throws Exception
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(security.getPublicKeyString(), "localId");
		FieldDataPacket fdp = new FieldDataPacket(uid, StandardFieldSpecs.getDefaultTopSetionFieldSpecs());
		
		Writer writerPlain = new StringWriter();
		
		Stopwatch writePlainTimer = new Stopwatch();
		fdp.writeXmlPlainText(writerPlain, security);
		print("Write    plaintext fdp", writePlainTimer.stop());

		InputStreamWithSeek in1 = new StringInputStreamWithSeek(writerPlain.toString());
		Stopwatch validatePlainTimer = new Stopwatch();
		FieldDataPacket.validateXml(in1, uid.getAccountId(), uid.getLocalId(), null, security);
		print("Validate plaintext fdp", validatePlainTimer.stop());

		InputStreamWithSeek in2 = new StringInputStreamWithSeek(writerPlain.toString());
		Stopwatch loadPlainTimer = new Stopwatch();
		fdp.loadFromXml(in2, security);
		print("Load     plaintext fdp", loadPlainTimer.stop());
	}
	
	public void testXmlEncrypted() throws Exception
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(security.getPublicKeyString(), "localId");
		FieldDataPacket fdp = new FieldDataPacket(uid, StandardFieldSpecs.getDefaultTopSetionFieldSpecs());
		
		Writer writerEncrypted = new StringWriter();
		
		Stopwatch writeTimer = new Stopwatch();
		fdp.writeXmlEncrypted(writerEncrypted, security);
		print("Write    encrypted fdp", writeTimer.stop());
		
		InputStreamWithSeek in1 = new StringInputStreamWithSeek(writerEncrypted.toString());
		Stopwatch validateTimer = new Stopwatch();
		FieldDataPacket.validateXml(in1, uid.getAccountId(), uid.getLocalId(), null, security);
		print("Validate encrypted fdp", validateTimer.stop());

		InputStreamWithSeek in2 = new StringInputStreamWithSeek(writerEncrypted.toString());
		Stopwatch loadTimer = new Stopwatch();
		fdp.loadFromXml(in2, security);
		print("Load     encrypted fdp", loadTimer.stop());
	}
	
	public void testAttachments() throws Exception
	{
		File dir = createTempFile();
		dir.delete();
		dir.mkdirs();
		Database db = new ClientFileDatabase(dir, security);
		db.initialize();

		timeAttachments(db, 1);
		timeAttachments(db, 100 * 1024);
		timeAttachments(db, 5 * 1024 * 1024);
		
		db.deleteAllData();
		dir.delete();
		assertFalse("cleanup error (" + dir.getPath() + ")?", dir.exists());
	}
	
	void timeAttachments(Database db, int fileLength) throws Exception
	{
		String accountId = security.getPublicKeyString();
		SessionKey sessionKeyBytes = security.createSessionKey();

		Stopwatch fileCreateTimer = new Stopwatch();
		File fileToAttach = createRandomFile(fileLength);
		print("Create " + fileLength + " byte file", fileCreateTimer.stop());
		
		AttachmentPacket ap = new AttachmentPacket(accountId, sessionKeyBytes, fileToAttach, security);
		ap.writeXmlToClientDatabase(db, false, security);
		print("Write " + fileLength + " byte attachment to db", fileCreateTimer.stop());

		fileToAttach.delete();
		assertFalse("fileToAttach exists?", fileToAttach.exists());

		DatabaseKey key = DatabaseKey.createLegacyKey(ap.getUniversalId());
		InputStreamWithSeek xmlIn = db.openInputStream(key, security);
		
		Stopwatch verifyTimer = new Stopwatch();
		Packet.verifyPacketSignature(xmlIn, null, security);
		print("Verify " + fileLength + " byte attachment", verifyTimer.stop());
		
		File destFile = createTempFile();
//		Stopwatch fileExportTimer = new Stopwatch();
//		AttachmentPacket.exportRawFileFromXml(xmlIn, sessionKeyBytes, security, destFile);
//		print("Export " + fileLength + " byte attachment", fileExportTimer.stop());

		xmlIn.close();
		destFile.delete();
		assertFalse("destFile exists?", destFile.exists());
	}
	
	File createRandomFile(int length) throws Exception
	{
		File file = createTempFile();
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		for(int i = 0; i < length; ++i)
			out.write((byte)i);
		out.close();
		return file;
	}
	
	File createRandomTextFile(int length) throws Exception
	{
		File file = createTempFile();
		final int lineLength = 100;
		final int lineCount = length/lineLength;
		UnicodeWriter writer = new UnicodeWriter(file);
		for(int line = 0; line < lineCount; ++line)
		{
			byte[] bytes = createRandomBytes(lineLength);
			String base64 = StreamableBase64.encode(bytes);
			writer.writeln(base64); 
		}
		writer.close();
		return file;
	}
	
	static final String keyPair = 
		"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" +
		"v9WPkKQixP7NxafdxT0=";
	
	static MartusCrypto security;
	static final char[] password = "password".toCharArray();
}
