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
import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import org.martus.common.MartusConstants;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.KeyShareException;
import org.martus.common.crypto.MartusSecretShare;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.SessionKey;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeStringWriter;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;


public class TestKeyShareSaveRestore extends TestCaseEnhanced
{
	public TestKeyShareSaveRestore(String name)
	{
		super(name);
		VERBOSE = false;
	}

	public void testSecretShare() throws Exception
	{
		SecureRandom random = new SecureRandom();
		
		byte[] secret = {1,2,3,4,0,8,5,6,7,0};
		Vector allShares = MartusSecretShare.buildShares(secret, random);
		assertNotNull("Shares null?",allShares);
		assertEquals("Incorrect number of Shares",  MartusConstants.numberOfFilesInShare, allShares.size());

		byte[] recoveredSecret = MartusSecretShare.recoverShares(allShares);
		assertNotNull("Recovered Secret Null?", recoveredSecret);
		assertTrue("Secret didn't match with allparts?", Arrays.equals(secret,recoveredSecret));
		
		Vector firstTwoShares = new Vector();
		firstTwoShares.add(allShares.get(0));
		firstTwoShares.add(allShares.get(1));
		recoveredSecret = MartusSecretShare.recoverShares(firstTwoShares);
		assertTrue("Secret didn't match with first and second share?", Arrays.equals(secret,recoveredSecret));

		Vector lastTwoShares = new Vector();
		lastTwoShares.add(allShares.get(1));
		lastTwoShares.add(allShares.get(2));
		recoveredSecret = MartusSecretShare.recoverShares(lastTwoShares);
		assertTrue("Secret didn't match with last two shares?", Arrays.equals(secret,recoveredSecret));

		Vector OneShareOnly = new Vector();
		OneShareOnly.add(allShares.get(1));
		try 
		{
			recoveredSecret = MartusSecretShare.recoverShares(OneShareOnly);
			fail("Secret returned with only one share?");
		} 
		catch (MartusCrypto.KeyShareException expectedException)
		{
		}

		Vector sameTwoShares = new Vector();
		sameTwoShares.add(allShares.get(2));
		sameTwoShares.add(allShares.get(2));
		try 
		{
			recoveredSecret = MartusSecretShare.recoverShares(sameTwoShares);
			fail("Secrets matched with only 1 share used twice?");
		} 
		catch (MartusCrypto.KeyShareException expectedException)
		{
		}

		byte[] allZeroSecret = {0,0,0,0,0,0,0};
		allShares = MartusSecretShare.buildShares(allZeroSecret, random);
		assertNotNull("Shares null for zeroSecret?",allShares);
		assertEquals("Incorrect number of Shares for zeroSecret",  MartusConstants.numberOfFilesInShare, allShares.size());
		byte[] recoveredZeroSecret = MartusSecretShare.recoverShares(allShares);
		assertNotNull("Recovered Zero Secret Null?", recoveredZeroSecret);
		assertTrue("Zero Secret didn't match with allparts?", Arrays.equals(allZeroSecret,recoveredZeroSecret));

		byte[] all255Secret = {(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};
		allShares = MartusSecretShare.buildShares(all255Secret, random);
		assertNotNull("Shares null for zeroSecret?",allShares);
		assertEquals("Incorrect number of Shares for zeroSecret",  MartusConstants.numberOfFilesInShare, allShares.size());
		byte[] recovered255Secret = MartusSecretShare.recoverShares(allShares);
		assertNotNull("Recovered 255 Secret Null?", recovered255Secret);
		assertTrue("255 Secret didn't match with allparts?", Arrays.equals(all255Secret,recovered255Secret));
	}
	
	public void testGetKeyShareBundles() throws Exception
	{
		MartusSecurity originalSecurity = new MartusSecurity();
		originalSecurity.createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
		
		Vector bundles = originalSecurity.buildKeyShareBundles();
		assertNotNull("Got a null vector?", bundles);
		assertEquals("Size of vector incorrect?", MartusConstants.numberOfFilesInShare, bundles.size());
		InputStream in = new StringInputStreamWithSeek((String)bundles.get(0));
		
		UnicodeReader reader = new UnicodeReader(in);
		String item1MartusConstant = reader.readLine();
		assertEquals("First part of file not a MartusShare ID?",MartusConstants.martusSecretShareFileID, item1MartusConstant);		

		String item2DateTimeStamp = reader.readLine();
		Thread.sleep(1000);
		Date currentDateTime = new Date();
		Timestamp dateTimeStamp = Timestamp.valueOf(item2DateTimeStamp);
		assertTrue("Second part of file not a Date Time Stamp occuring before the current time?",currentDateTime.after(dateTimeStamp));		

		String item3PublicKey = reader.readLine();
		assertTrue("Third part of file not our public key?",item3PublicKey.equals(originalSecurity.getPublicKeyString()));		
		
		String share1SessionKey = reader.readLine();

		in.close();
		reader.close();
		in = new StringInputStreamWithSeek((String)bundles.get(2));
		reader = new UnicodeReader(in);

		item1MartusConstant = reader.readLine();
		assertEquals("First part of 3rd file not a MartusShare ID?",item1MartusConstant, MartusConstants.martusSecretShareFileID);		

		item2DateTimeStamp = reader.readLine();
		dateTimeStamp = Timestamp.valueOf(item2DateTimeStamp);
		assertTrue("Second part of file not a Date Time Stamp occuring before the current time?",currentDateTime.after(dateTimeStamp));		

		item3PublicKey = reader.readLine();
		assertTrue("Third part of 3rd file not our public key?",item3PublicKey.equals(originalSecurity.getPublicKeyString()));		
		
		String share2SessionKey = reader.readLine();
		
		Vector twoShares = new Vector();
		twoShares.add(share1SessionKey);
		twoShares.add(share2SessionKey);
		SessionKey recoveredSessionKey = new SessionKey(MartusSecretShare.recoverShares(twoShares));
		String item5EncodedAndEncryptedKeyPair = reader.readLine();
		byte[] encryptedKeyPair = StreamableBase64.decode(item5EncodedAndEncryptedKeyPair);
		in.close();
		reader.close();
		
		MartusSecurity recoveredSecurity = new MartusSecurity();
		ByteArrayInputStreamWithSeek inEncryptedKeyPair = new ByteArrayInputStreamWithSeek(encryptedKeyPair);
		ByteArrayOutputStream outDecryptedKeyPair = new ByteArrayOutputStream();
		recoveredSecurity.decrypt( inEncryptedKeyPair, outDecryptedKeyPair, recoveredSessionKey);
		outDecryptedKeyPair.close();
		inEncryptedKeyPair.close();

		recoveredSecurity.clearKeyPair();
		recoveredSecurity.setKeyPairFromData(outDecryptedKeyPair.toByteArray());
		
		assertEquals("Public Keys don't match?",item3PublicKey,recoveredSecurity.getPublicKeyString());		
		assertEquals("Security Public Keys don't match?",originalSecurity.getPublicKeyString(),recoveredSecurity.getPublicKeyString());		
		assertEquals("Security Private Keys don't match?",originalSecurity.getPrivateKeyString(),recoveredSecurity.getPrivateKeyString());		
	}
	
	public void testRecoverFromKeyShareBundles() throws Exception
	{
		MartusSecurity recoveredSecurity = new MartusSecurity();
		try 
		{
			recoveredSecurity.recoverFromKeyShareBundles(null);
			fail("Did not throw with null vector of bundles?");
		} 
		catch (KeyShareException expectedException) 
		{
		}

		try 
		{
			Vector emptyBundles = new Vector();
			recoveredSecurity.recoverFromKeyShareBundles(emptyBundles);
			fail("Did not throw with an empty vector of bundles?");
		} 
		catch (KeyShareException expectedException) 
		{
		}

		try 
		{
			Vector fakeBundle = new Vector();
			fakeBundle.add(new String("fake bundle 1"));
			fakeBundle.add(new String("fake bundle 2"));
			recoveredSecurity.recoverFromKeyShareBundles(fakeBundle);
			fail("Did not throw with a fake vector of single element bundles?");
		} 
		catch (KeyShareException expectedException) 
		{
		}

		try 
		{
			Vector fakeBundle = new Vector();
			for(int i = 0; i < MartusConstants.minNumberOfFilesNeededToRecreateSecret; ++i)
			{
				UnicodeStringWriter writer = UnicodeStringWriter.create();
				writer.writeln(MartusConstants.martusSecretShareFileID+"corrupt");
				writer.writeln("corrupted Public code");
				writer.writeln("date/time stamp");
				writer.writeln("corrupted Share");
				writer.writeln(StreamableBase64.encode("Corrupted KeyPair"));
				writer.close();
				fakeBundle.add(writer.toString());
			}			
			recoveredSecurity.recoverFromKeyShareBundles(fakeBundle);
			fail("Did not throw with a fake vector of corrupted 4 element bundles?");
		} 
		catch (KeyShareException expectedException) 
		{
		}

		MartusSecurity originalSecurity = new MartusSecurity();
		originalSecurity.createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
		Vector bundles = originalSecurity.buildKeyShareBundles();
		recoveredSecurity.recoverFromKeyShareBundles(bundles);
		assertEquals("Public Keys don't Match?",originalSecurity.getPublicKeyString(), recoveredSecurity.getPublicKeyString());
		assertEquals("Private Keys don't Match?",originalSecurity.getPrivateKeyString(), recoveredSecurity.getPrivateKeyString());
	}
	final int SMALLEST_LEGAL_KEY_FOR_TESTING = 512;

}
