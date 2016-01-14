/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2007, Beneficent
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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.ContactInfo;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.common.utilities.MartusServerUtilities.MartusSignatureFileDoesntExistsException;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;


public class TestMartusServerUtilities extends TestCaseEnhanced
{
	public TestMartusServerUtilities(String name) throws Exception
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		TRACE_BEGIN("setUp");
		
		if(serverSecurity == null)
		{
			serverSecurity = MockMartusSecurity.createServer();
		}

		TRACE_END();
	}
	
	public void tearDown() throws Exception
	{
		TRACE_BEGIN("tearDown");
		TRACE_END();
		super.tearDown();
	}

	public void testServerFileSigning() throws Exception
	{
		TRACE_BEGIN("testServerFileSigning");

		File fileToSign = createTempFileWithContents("Line 1 of test text\n");
		File fileValidSignature;
		File fileInvalidSignature;

		try
		{
			MartusServerUtilities.getLatestSignatureFileFromFile(fileToSign);
			fail("Signature file should not exist.");
		}
		catch (MartusSignatureFileDoesntExistsException ignoredException)
		{}
		
		fileValidSignature = MartusServerUtilities.createSignatureFileFromFileOnServer(fileToSign, serverSecurity);
		assertTrue("createSignatureFileFromFileOnServer", fileValidSignature.exists() );
		
		try
		{
			MartusServerUtilities.verifyFileAndSignatureOnServer(fileToSign, fileValidSignature, serverSecurity, serverSecurity.getPublicKeyString());
		}
		catch (FileVerificationException e)
		{
			fail("Signature did not verify against file.");
		}
				
		File tempFile = createTempFileWithContents("Line 1 of test text\nLine 2 of test text\n");
		fileInvalidSignature = MartusServerUtilities.createSignatureFileFromFileOnServer(tempFile, serverSecurity);
		try
		{
			MartusServerUtilities.verifyFileAndSignatureOnServer(fileToSign, fileInvalidSignature, serverSecurity, serverSecurity.getPublicKeyString());
			fail("Should not verify against incorrect signature.");
		}
		catch (FileVerificationException e)
		{}
		
		fileToSign.delete();
		fileValidSignature.delete();
		tempFile.delete();
		fileInvalidSignature.delete();

		TRACE_END();
	}
	
	public void testGetLatestSignatureFile() throws Exception
	{
		File fileToSign = createTempFileWithContents("Line 1 of test text\n");
		File sigDir = MartusServerUtilities.getSignatureDirectoryForFile(fileToSign);
		sigDir.mkdirs();

		File earliestFile = new File(sigDir, fileToSign.getName() + "1.sig");
		MartusServerUtilities.writeSignatureFileWithDatestamp(earliestFile, "20010109-120001", fileToSign, serverSecurity);
		
		File newestFile = new File(sigDir, fileToSign.getName() + "2.sig");
		MartusServerUtilities.writeSignatureFileWithDatestamp(newestFile, "20040109-120001", fileToSign, serverSecurity);
		
		File validSignatureFile = MartusServerUtilities.getLatestSignatureFileFromFile(fileToSign);
		assertEquals("Incorrect signature file retrieved", validSignatureFile.getAbsolutePath(), newestFile.getAbsolutePath());
		
		fileToSign.delete(); 
		earliestFile.delete();
		newestFile.delete();
		sigDir.delete();
	}

	public void testWriteContactInfo() throws Exception
	{
		try
		{
			MartusServerUtilities.writeContatctInfo("bogusId", new Vector(), null);
			fail("Should have thrown invalid file");
		}
		catch (Exception expectedException)
		{
		}

		Vector contactInfo = new Vector();
		String clientId = "id";
		contactInfo.add(clientId);
		contactInfo.add(new Integer(2));
		String data1 = "Data";
		contactInfo.add(data1);
		String data2 = "Data2";
		contactInfo.add(data2);
		String signature = "Signature";
		contactInfo.add(signature);

		File contactInfoFile = createTempFile();
		MartusServerUtilities.writeContatctInfo(clientId, contactInfo, contactInfoFile);

		assertTrue("File Doesn't exist?", contactInfoFile.exists());

		FileInputStream contactFileInputStream = new FileInputStream(contactInfoFile);
		DataInputStream in = new DataInputStream(contactFileInputStream);

		String inputPublicKey = in.readUTF();
		int inputDataCount = in.readInt();
		String inputData =  in.readUTF();
		String inputData2 =  in.readUTF();
		String inputSig = in.readUTF();
		in.close();

		assertEquals("Public key doesn't match", clientId, inputPublicKey);
		assertEquals("data size not two?", 2, inputDataCount);
		assertEquals("data not correct?", data1, inputData);
		assertEquals("data2 not correct?", data2, inputData2);
		assertEquals("signature doesn't match?", signature, inputSig);		

		contactInfoFile.delete();
		contactInfoFile.getParentFile().delete();
	}

	public void testGetContactInfo() throws Exception
	{
		File invalidFile = createTempFile();
		try
		{
			ContactInfo.loadFromFile(invalidFile);
			fail("Should have thrown invalid file");
		}
		catch (Exception expectedException)
		{
		}

		Vector contactInfo = new Vector();
		String clientId = "id";
		contactInfo.add(clientId);
		contactInfo.add(new Integer(2));
		String data1 = "Data";
		contactInfo.add(data1);
		String data2 = "Data2";
		contactInfo.add(data2);
		String signature = "Signature";
		contactInfo.add(signature);

		File contactInfoFile = createTempFile();
		MartusServerUtilities.writeContatctInfo(clientId, contactInfo, contactInfoFile);
		Vector retrievedInfo = ContactInfo.loadFromFile(contactInfoFile);
		assertEquals("Vector wrong size", contactInfo.size(), retrievedInfo.size());
		
		String inputPublicKey = (String)retrievedInfo.get(0);
		int inputDataCount = ((Integer)retrievedInfo.get(1)).intValue();
		String inputData = (String)retrievedInfo.get(2);
		String inputData2 = (String)retrievedInfo.get(3);
		String inputSig = (String)retrievedInfo.get(4);

		assertEquals("Public key doesn't match", clientId, inputPublicKey);
		assertEquals("data size not two?", 2, inputDataCount);
		assertEquals("data not correct?", data1, inputData);
		assertEquals("data2 not correct?", data2, inputData2);
		assertEquals("signature doesn't match?", signature, inputSig);		

		contactInfoFile.delete();
		contactInfoFile.getParentFile().delete();
	}

	public void testWriteAccessToken() throws Exception
	{
		String clientId = "id";
		File accessTokenFile = createTempFile();
		String tokenData = "invalid";
		MartusServerUtilities.writeAccessTokenData(clientId, tokenData, accessTokenFile);

		assertTrue("File doesn't exist?", accessTokenFile.exists());

		FileInputStream accessTokenFileInputStream = new FileInputStream(accessTokenFile);
		UnicodeReader in = new UnicodeReader(accessTokenFileInputStream);

		String inputToken = in.readLine();
		in.close();

		assertEquals("Tokens doesn't match", tokenData, inputToken);
		accessTokenFile.delete();
		accessTokenFile.getParentFile().delete();
	}
	
	public File createTempFileWithContents(String content)
		throws IOException
	{
		File file = createTempFileFromName("$$$MartusTestMartusServerUtilities");
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.writeln(content);
		writer.flush();
		writer.close();
		
		return file;
	}

	static MartusCrypto serverSecurity;
}
