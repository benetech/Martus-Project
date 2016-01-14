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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Vector;

import org.martus.common.crypto.DefaultSecurityContext;
import org.martus.common.crypto.MartusJceKeyPair;
import org.martus.common.crypto.MartusKeyPair;
import org.martus.common.crypto.MartusKeyPairLoader;
import org.martus.common.crypto.MartusKeyPairSaver;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.crypto.SecurityContext;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;

public class TestMartusKeyPair extends TestCaseEnhanced
{
	public TestMartusKeyPair(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		rand = new SecureRandom(new byte[] {1,2,3,4,5,6,7,8});
		
		objects = new Vector();

		SecurityContext providerAccessor = new DefaultSecurityContext();
		MartusJceKeyPair jceKeyPair = new MartusJceKeyPair(rand, providerAccessor);
		objects.add(jceKeyPair);

//		MartusDirectCryptoKeyPair directKeyPair = new MartusDirectCryptoKeyPair(rand);
//		objects.add(directKeyPair);

		for(int i = 0; i < objects.size(); ++i)
		{
			MartusKeyPair p = (MartusKeyPair)objects.get(i);
			assertFalse("has initial key? " + p.getClass().getName(), p.hasKeyPair());
			
			p.createRSA(512);
			assertTrue("has key failed? " + p.getClass().getName(), p.hasKeyPair());
			String publicKeyString = p.getPublicKeyString();
			StreamableBase64.decode(publicKeyString);
		}
		objects.add(MockMartusSecurity.createClient().getKeyPair());
		objects.add(MockMartusSecurity.createOtherClient().getKeyPair());
//		System.out.println("JCE:");
//		System.out.println(((RSAPublicKey)jceKeyPair.getPublicKey()).getModulus());
//		System.out.println(((RSAPublicKey)jceKeyPair.getPublicKey()).getPublicExponent());
	}
	
	public void tearDown() throws Exception
	{
		for(int i = 0; i < objects.size(); ++i)
		{
			MartusKeyPair p = (MartusKeyPair)objects.get(i);
			assertTrue("lost key? " + p.getClass().getName(), p.hasKeyPair());
			p.clear();
			assertFalse("clear failed? " + p.getClass().getName(), p.hasKeyPair());
		}
	}

	public void testBasics() throws Exception
	{
	}
	
	public void testKeyData() throws Exception
	{
		// This test is under development, comments and code will
		// be cleaned up later
		
		//MartusJceKeyPair reader = new MartusJceKeyPair(rand);
		for(int i = 0; i < objects.size(); ++i)
		{
			MartusKeyPair keyOwner = (MartusKeyPair)objects.get(i);
			byte[] data = keyOwner.getKeyPairData();
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
			
//			ByteArrayInputStream rawIn = new ByteArrayInputStream(data);
//			ObjectInputStream in = new ObjectInputStream(rawIn);
//			KeyPair got  = (KeyPair)in.readObject();
//			PrivateKey gotPrivate = got.getPrivate();
//			PublicKey gotPublic = got.getPublic();
//			System.out.println(gotPrivate.getClass().getName());
//			System.out.println(gotPublic.getClass().getName());

			SecurityContext providerAccessor = new DefaultSecurityContext();
			MartusKeyPair gotKeyPair = new MartusJceKeyPair(MartusKeyPairLoader.load(in, providerAccessor), providerAccessor);
			verifyEncryptDecrypt(keyOwner, gotKeyPair);
			verifyEncryptDecrypt(gotKeyPair, keyOwner);
	
//			http://www.macchiato.com/columns/Durable4.html
//			URL For serialized data structure
			

//			File tmpFile = createTempFile();
//			FileOutputStream out = new FileOutputStream(tmpFile);
//			out.write(data);
//			out.close();
//			System.out.println(tmpFile.getAbsolutePath());
//			while(in.available() > 0)
//			{
//				System.out.println(in.readByte());
//			}
			
			
			
//			reader.setFromData(data);
//			byte[] copiedData = reader.getKeyPairData();
//			System.out.println(Base64.encode(data));
//			System.out.println(Base64.encode(copiedData));
//			assertTrue("get data wrong? " + keyOwner.getClass().getName(), Arrays.equals(data, copiedData));
		}
	}

	public void testMartusKeyPairSaver() throws Exception
	{
		MartusJceKeyPair keyPair = (MartusJceKeyPair)MockMartusSecurity.createClient().getKeyPair();
		ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(rawOut);
		MartusKeyPairSaver.save(out, keyPair.getJceKeyPair());
		
		byte[] dataResults = rawOut.toByteArray();
		ByteArrayInputStream rawIn = new ByteArrayInputStream(dataResults);
		DataInputStream in = new DataInputStream(rawIn);
		SecurityContext providerAccessor = new DefaultSecurityContext();
		MartusJceKeyPair loaded = new MartusJceKeyPair(MartusKeyPairLoader.load(in, providerAccessor), providerAccessor);
		
		verifyEncryptDecrypt(loaded, keyPair);
		verifyEncryptDecrypt(keyPair, loaded);
	}
	
	public void testEncryption() throws Exception
	{
		for(int i = 0; i < objects.size(); ++i)
		{
			MartusKeyPair encryptor = (MartusKeyPair)objects.get(i);
			MartusKeyPair self = encryptor;
			verifyEncryptDecrypt(encryptor, self);
			
// The following test should be valid, but doesn't work yet
//			int next = (i+1)%objects.size();
//			MartusKeyPair decryptor = (MartusKeyPair)objects.get(next);
//			verifyEncryptDecrypt(encryptor, decryptor);
		}		
	}
	
	private void verifyEncryptDecrypt(MartusKeyPair encryptor, MartusKeyPair decryptor) throws Exception
	{
		byte[] sampleBytes = new byte[] {55, 99, 13, 23, };
		byte[] encrypted = encryptor.encryptBytes(sampleBytes, decryptor.getPublicKeyString());
		byte[] decrypted = decryptor.decryptBytes(encrypted);
		String label = " encryptor: " + encryptor.getClass().getName() +
						" decryptor: " + decryptor.getClass().getName();
		assertTrue("bad decrypt? " + label, Arrays.equals(sampleBytes, decrypted));
		
	}

	Vector objects;
	SecureRandom rand;
}
