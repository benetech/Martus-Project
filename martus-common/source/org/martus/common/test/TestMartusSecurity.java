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
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;

import org.martus.common.MartusConstants;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.EncryptionException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.crypto.MartusJceKeyPair;
import org.martus.common.crypto.MartusKeyPair;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.crypto.SessionKey;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;



public class TestMartusSecurity extends TestCaseEnhanced
{
	public TestMartusSecurity(String name)
	{
		super(name);
		VERBOSE = false;
	}

	public void setUp() throws Exception
	{
		super.setUp();
		TRACE_BEGIN("setUp");
		if(security == null)
			security = new MartusSecurity();

		if(!security.hasKeyPair())
		{
			security.createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
			assertTrue("setup1: KeyPair returned NULL", security.hasKeyPair());

			MartusSecurity otherSecurity = new MartusSecurity();
			otherSecurity.createKeyPair(SMALLEST_LEGAL_KEY_FOR_TESTING);
			assertTrue("setup2: KeyPair returned NULL", security.hasKeyPair());

			invalidKeyPair = new KeyPair(security.getKeyPair().getPublicKey(), otherSecurity.getKeyPair().getPrivateKey());
			assertTrue("setup3: KeyPair returned NULL", security.hasKeyPair());
		}
		assertTrue("setup4: KeyPair returned NULL", security.hasKeyPair());
		if(securityWithoutKeyPair == null)
		{
			securityWithoutKeyPair = new MartusSecurity();
		}
		assertNotNull("setup: security NULL", security);
		assertTrue("setup: KeyPair returned NULL", security.hasKeyPair());
		assertNotNull("setup: Key returned NULL", security.getKeyPair().getPrivateKey());
		TRACE_END();
	}
	
	public void testPublicCode40WithLocale() throws Exception
	{
		
		String idOfLocaleThatUsesNonAsciiDigits = "th-TH-u-nu-thai";
		Locale localeWithNonAsciiDigits = Locale.forLanguageTag(idOfLocaleThatUsesNonAsciiDigits);
		String nonAscii = String.format(localeWithNonAsciiDigits, "%d", 1);
		assertNotEquals("Locale isn't testing non-ascii?", "1", nonAscii);

		Locale originalLocale = Locale.getDefault();
		try
		{
			String fakePublicKeyString = "This string can be anything";

			Locale localeWithAsciiDigits = Locale.forLanguageTag("en");
			Locale.setDefault(localeWithAsciiDigits);
			String expectedPublicCode40 = MartusCrypto.computeFormattedPublicCode40(fakePublicKeyString);

			Locale.setDefault(localeWithNonAsciiDigits);
			String gotPublicCode40 = MartusCrypto.computeFormattedPublicCode40(fakePublicKeyString);
			assertEquals(expectedPublicCode40, gotPublicCode40);
		}
		finally
		{
			Locale.setDefault(originalLocale);
		}
		
	}
	
	public void testCreateDigest() throws Exception
	{
		byte[] bytes1 = new byte[] {1, 5, 100, 96, 7};
		byte[] bytes2 = new byte[] {1, 5, 100, 96, 8};
		
		ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(bytes1);
		byte[] digest1 = MartusSecurity.createDigest(in1);
		ByteArrayInputStreamWithSeek in2 = new ByteArrayInputStreamWithSeek(bytes2);
		byte[] digest2 = MartusSecurity.createDigest(in2);
		assertFalse("Digests matched?", Arrays.equals(digest1, digest2));
		
		in1.seek(0);
		byte[] digest1Again = MartusSecurity.createDigest(in1);
		assertTrue("Digest changed?", Arrays.equals(digest1, digest1Again));
	}
	
	public void testCreatePartialDigest() throws Exception
	{
		verifyPartialDigest(10, 0);
		verifyPartialDigest(10, 5);
		verifyPartialDigest(10, 10);
		try
		{
			verifyPartialDigest(10, 11);
			fail("Expected exception");
		}
		catch(CreateDigestException ignoreExpected)
		{
		}
		int bufferSize = MartusConstants.digestBufferSize;
		verifyPartialDigest(bufferSize, bufferSize-1);
		verifyPartialDigest(bufferSize, bufferSize);
		verifyPartialDigest(bufferSize+1, bufferSize);
		verifyPartialDigest(bufferSize+1, bufferSize+1);
		verifyPartialDigest(bufferSize+2, bufferSize+1);
		
	}

	public void verifyPartialDigest(int fullLength, int partialLength)
			throws Exception
	{
		byte[] longerThanTheBuffer = new byte[fullLength];
		Arrays.fill(longerThanTheBuffer, (byte)2);
		byte[] longPartial = new byte[partialLength];
		int lengthToCopy = Math.min(partialLength, longerThanTheBuffer.length);
		System.arraycopy(longerThanTheBuffer, 0, longPartial, 0, lengthToCopy);
		
		ByteArrayInputStreamWithSeek longIn = new ByteArrayInputStreamWithSeek(longerThanTheBuffer);
		byte[] longPartialDigest = MartusSecurity.createPartialDigest(longIn, partialLength);
		ByteArrayInputStreamWithSeek longPartialIn = new ByteArrayInputStreamWithSeek(longPartial);
		byte[] expectedLongPartialDigest = MartusSecurity.createPartialDigest(longPartialIn, partialLength);
		assertTrue("Long digests different?", Arrays.equals(expectedLongPartialDigest, longPartialDigest));
	}
	
	public void testLargerRsaKeys() throws Exception
	{
		final int LARGE_KEY_BITS = 4096;
		MartusSecurity bigKeySecurity = new MartusSecurity();
		bigKeySecurity.createKeyPair(LARGE_KEY_BITS);
		
		byte[] data = "This is a test!".getBytes("UTF-8");
		InputStream plainStream = new ByteArrayInputStream(data);
		ByteArrayOutputStream cipherStream = new ByteArrayOutputStream();
		bigKeySecurity.encrypt(plainStream, cipherStream);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		char[] passPhrase = "whatever".toCharArray();
		bigKeySecurity.writeKeyPair(out, passPhrase);
		
		bigKeySecurity.clearKeyPair();
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		bigKeySecurity.readKeyPair(in, passPhrase);
		
		InputStreamWithSeek encryptedStream = new ByteArrayInputStreamWithSeek(cipherStream.toByteArray());
		ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();
		bigKeySecurity.decrypt(encryptedStream, decryptedStream);
		
		assertTrue(Arrays.equals(data, decryptedStream.toByteArray()));
	}
	
	public void testCacheOfDecryptedSessionKeys() throws Exception
	{
		MartusSecurity bigKeySecurity = new MartusSecurity();
		bigKeySecurity.createKeyPair();
		String publicKeyString = bigKeySecurity.getPublicKeyString();
		
		byte[] emptyCache1 = bigKeySecurity.getSessionKeyCache();
		int conservativeMaximumEmptySize = MartusSecurity.getBitsWhenCreatingKeyPair();
		assertTrue("emptyCache1 too big? (was " + emptyCache1.length + ")", emptyCache1.length < conservativeMaximumEmptySize);

		final int MAX_KEYS = 100;
		SessionKey[] plainSessionKeys = new SessionKey[MAX_KEYS];
		SessionKey[] encryptedSessionKeys = new SessionKey[MAX_KEYS];
		for(int i=0; i < MAX_KEYS; ++i)
		{
			plainSessionKeys[i] = bigKeySecurity.createSessionKey();
			encryptedSessionKeys[i] = bigKeySecurity.encryptSessionKey(plainSessionKeys[i], publicKeyString);
		}
		
		
		byte[] fullCache1 = bigKeySecurity.getSessionKeyCache();
		assertTrue("fullCache1 too small? (was " + fullCache1.length + ")", fullCache1.length > conservativeMaximumEmptySize);

		bigKeySecurity.flushSessionKeyCache();
		byte[] emptyCache2 = bigKeySecurity.getSessionKeyCache();
		assertTrue("emptyCache2 too big?", emptyCache2.length < conservativeMaximumEmptySize);

		for(int i = 0; i < MAX_KEYS; ++i)
		{
			SessionKey decrypted = bigKeySecurity.decryptSessionKey(encryptedSessionKeys[i]);
			assertEquals("bad initial decryption?", plainSessionKeys[i], decrypted);
		}
		
		byte[] fullCache2 = bigKeySecurity.getSessionKeyCache();
		assertTrue("fullCache2 too small?", fullCache2.length > conservativeMaximumEmptySize);

		for(int i=0; i < MAX_KEYS; ++i)
		{
			encryptedSessionKeys[i] = new SessionKey(encryptedSessionKeys[i].getBytes());
		}
		
		long stopTime1 = System.currentTimeMillis() + 1000;
		for(int i=0; i < MAX_KEYS; ++i)
		{
			assertTrue("After juggle, only decrypted " + i, System.currentTimeMillis() < stopTime1);
			SessionKey decrypted = bigKeySecurity.decryptSessionKey(encryptedSessionKeys[i]);
			assertEquals("after juggle, bad re-decryption?", plainSessionKeys[i], decrypted);
		}
		
		bigKeySecurity.flushSessionKeyCache();
		bigKeySecurity.setSessionKeyCache(fullCache2);
		byte[] fullCache3 = bigKeySecurity.getSessionKeyCache();
		assertTrue("fullCache3 too small?", fullCache3.length > conservativeMaximumEmptySize);

		long stopTime2 = System.currentTimeMillis() + 1000;
		for(int i=0; i < MAX_KEYS; ++i)
		{
			assertTrue("After cache restore, only decrypted " + i, System.currentTimeMillis() < stopTime2);
			SessionKey decrypted = bigKeySecurity.decryptSessionKey(encryptedSessionKeys[i]);
			assertEquals("after cache restore, bad re-decryption?", plainSessionKeys[i], decrypted);
		}
	}
	
	public void testSessionKeyCacheSigned() throws Exception
	{
		byte[] original = security.getSessionKeyCache();
		
		try
		{
			MartusSecurity otherSecurity = MockMartusSecurity.createOtherClient();
			otherSecurity.setSessionKeyCache(original);
			fail("Should have thrown for not our cache");
		}
		catch(AuthorizationFailedException ignoreExpected)
		{
			
		}
		
		try
		{
			byte[] partial = new byte[original.length-17];
			System.arraycopy(original, 0, partial, 0, partial.length);
			security.setSessionKeyCache(partial);
			fail("Should have thrown for incomplete cache");
		}
		catch(MartusSignatureException ignoreExpected)
		{
		}

		try
		{
			byte[] middleDamage = new byte[original.length];
			System.arraycopy(original, 0, middleDamage, 0, middleDamage.length);
			middleDamage[middleDamage.length/2] ^= 0xff;
			security.setSessionKeyCache(middleDamage);
			fail("Should have thrown for middle damaged cache");
		}
		catch(MartusSignatureException ignoreExpected)
		{
		}
	}

	public void testGetDigestOfPartOfPrivateKey() throws Exception
	{
		MartusCrypto knownKey = MockMartusSecurity.createClient();
		String digest = StreamableBase64.encode(knownKey.getDigestOfPartOfPrivateKey());
		assertEquals("PY7HmxJgqLy76WNx3mKfaNnxFc8=", digest);
	}

	public void testPbe()
	{
		TRACE_BEGIN("testPbe");
		byte[] original = {65,66,67,78,79};
		char[] passPhrase = "secret".toCharArray();

		byte[] salt = MartusSecurity.createRandomSalt();

		byte[] encoded = security.pbeEncrypt(original, passPhrase, salt);
		byte[] decoded = security.pbeDecrypt(encoded, passPhrase, salt);
		assertTrue("should work", Arrays.equals(original, decoded));
		TRACE_END();
	}

	public void testCreateKeyPair()
	{
		TRACE_BEGIN("testCreateKeyPair");
		assertFalse("start with no key pair", securityWithoutKeyPair.hasKeyPair());

		MartusKeyPair keyPair = security.getKeyPair();
		assertNotNull("got a key pair", keyPair);

		RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublicKey();
		BigInteger publicExp = publicKey.getPublicExponent();
		assertTrue("public non-zero", publicExp.bitLength() != 0);

		RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivateKey();
		BigInteger privateExp = privateKey.getPrivateExponent();
		assertTrue("private non-zero", privateExp.bitLength() != 0);

		String	account = security.getPublicKeyString();
		byte[] publicKeyBytes = publicKey.getEncoded();
		String publicKeyString = StreamableBase64.encode(publicKeyBytes);
		assertEquals("Public Key doesn't Match", publicKeyString, account);
		TRACE_END();
	}

	public void testWriteKeyPairBadStream()
	{
		TRACE_BEGIN("testWriteKeyPairBadStream");
		try
		{
			security.writeKeyPair(null, "whatever".toCharArray());
			fail("expected an exception");
		}
		catch(Exception e)
		{
			// expected
		}
		TRACE_END();
	}

	public void testReadKeyPairIncorrectVersion() throws Exception
	{
		TRACE_BEGIN("testReadKeyPairIncorrectVersion");
		char[] passPhrase = "newpassphase".toCharArray();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		security.writeKeyPair(outputStream, passPhrase);
		byte[] encryptedData = outputStream.toByteArray();

		MartusSecurity tempSecurity = new MartusSecurity();

		encryptedData[0] = 127;
		ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedData);
		try
		{
			tempSecurity.readKeyPair(inputStream, passPhrase);
			fail("Should have thrown exception incorrect version");
		}
		catch (MartusSecurity.InvalidKeyPairFileVersionException e)
		{
			//Expected Exception
		}

		TRACE_END();
	}

	public void testGetAndSetKeyPair() throws Exception
	{
		TRACE_BEGIN("testWriteAndReadKeyPair");
		MartusKeyPair keyPair = security.getKeyPair();
		assertEquals("no change", keyPair, security.getKeyPair());
		byte[] data = keyPair.getKeyPairData();
		assertTrue("byte compare", Arrays.equals(data, keyPair.getKeyPairData()));

		MartusSecurity tempSecurity = new MartusSecurity();
		tempSecurity.setKeyPairFromData(data);
		MartusKeyPair originalKeyPair = security.getKeyPair();
		MartusKeyPair gotKeyPair = tempSecurity.getKeyPair();
		assertNotNull("get/set null", tempSecurity);
		assertEquals("get/set public", originalKeyPair.getPublicKey(), gotKeyPair.getPublicKey());
		assertEquals("get/set private", originalKeyPair.getPrivateKey(), gotKeyPair.getPrivateKey());

		String publicKeyString = security.getPublicKeyString();
		PublicKey publicKey = MartusJceKeyPair.extractPublicKey(publicKeyString);
		assertEquals("get/extract failed?", publicKey, security.getKeyPair().getPublicKey());
	}

	public void testWriteAndReadKeyPair() throws Exception
	{
		char[] passPhrase = "My dog has fleas".toCharArray();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		security.writeKeyPair(outputStream, passPhrase);
		byte[] bytes = outputStream.toByteArray();
		assertTrue("empty", bytes.length > 0);

		MartusSecurity tempSecurity = new MartusSecurity();

		{
			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
			tempSecurity.readKeyPair(inputStream, passPhrase);
			MartusKeyPair oldKeyPair = security.getKeyPair();
			MartusKeyPair gotKeyPair = tempSecurity.getKeyPair();
			assertNotNull("good null", gotKeyPair);
			assertEquals("good public", oldKeyPair.getPublicKey(), gotKeyPair.getPublicKey());
			assertEquals("good private", oldKeyPair.getPrivateKey(), gotKeyPair.getPrivateKey());

			try
			{
				tempSecurity.readKeyPair(inputStream, passPhrase);
				fail("Reading eof should have thrown an exception");
			}
			catch(Exception e)
			{
				//This is an expected exception
			}
			assertFalse("past eof", tempSecurity.hasKeyPair());
		}

		{
			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
			try
			{
				tempSecurity.readKeyPair(inputStream, "different pass".toCharArray());
			}
			catch (MartusSecurity.AuthorizationFailedException e)
			{
				//Expected exception
			}
			assertFalse("bad passphrase", tempSecurity.hasKeyPair());
		}
		TRACE_END();
	}

	public void testIsKeyPairValid()
	{
		TRACE_BEGIN("testIsKeyPairValid");
		assertFalse("null", securityWithoutKeyPair.isKeyPairValid((KeyPair)null));
		assertTrue("created", security.getKeyPair().isKeyPairValid());
		assertFalse("invalid", security.isKeyPairValid(invalidKeyPair));
		TRACE_END();
	}

	public void testPrivateKey()
	{
		TRACE_BEGIN("testPrivateKey");
		assertNull("No Key", securityWithoutKeyPair.getKeyPair().getPrivateKey());
		assertNotNull("Key returned NULL", security.getKeyPair().getPrivateKey());
		TRACE_END();
	}

	public void testPublicKey()
	{
		TRACE_BEGIN("testPublicKey");
		assertNull("no key should return null key", securityWithoutKeyPair.getKeyPair().getPublicKey());
		assertNotNull("Key returned NULL?", security.getKeyPair().getPublicKey());

		assertNull("Should be null", securityWithoutKeyPair.getPublicKeyString());
		String publicKeyString = security.getPublicKeyString();
		assertNotNull("no key string?", security.getPublicKeyString());
		PublicKey publicKey = MartusJceKeyPair.extractPublicKey(publicKeyString);
		assertNotNull("extract failed?", publicKey);
		TRACE_END();
	}

	public void testExtractPublicKey() throws Exception
	{
		assertNull("not base64", MartusJceKeyPair.extractPublicKey("not Base64"));
		assertNull("not valid key", MartusJceKeyPair.extractPublicKey(StreamableBase64.encode(new byte[] {1,2,3})));
	}

	public void testCreateSignature() throws MartusSecurity.MartusSignatureException
	{
		TRACE_BEGIN("testCreateSignature");
		byte[] data = createSampleData(1000);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		try
		{
			securityWithoutKeyPair.createSignatureOfStream(inputStream);
			fail("Signature passed with no key pair");
		}
		catch (MartusSecurity.MartusSignatureException e)
		{
			//Expected Exception
		}

		byte[] signature = security.createSignatureOfStream(inputStream);
		assertNotNull("signature was null", signature);

		TRACE_END();
	}

	private byte[] createSampleData(int size)
	{
		byte[] data = new byte[size];
		for(int i = 0 ; i < data.length; ++i)
		{
			data[i]= (byte)(i%100);
		}
		return data;
	}

//	public void testBouncySignatureSpeed() throws Exception
//	{
//		SecureRandom rand = new SecureRandom();
//		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
//
//		int[] keySizes = {1024, 1200, 1400, 1600, 1800, 2048};
//		for(int i=0; i < keySizes.length; ++i)
//		{
//			keyPairGenerator.initialize(keySizes[i], rand);
//			KeyPair keyPair = keyPairGenerator.genKeyPair();
//
//			Signature sigEngine;
//			sigEngine = Signature.getInstance("SHA1WithRSA", "BC");
//			sigEngine.initSign(keyPair.getPrivate(), rand);
//			sigEngine.update((byte)0);
//			long start = System.currentTimeMillis();
//			sigEngine.sign();
//			long stop = System.currentTimeMillis();
//			System.out.println("3-step sig " + keySizes[i] + " of one byte: " + (stop - start) + " ms");
//		}
//	}

	public void testVerifySignature() throws Exception
	{
		TRACE_BEGIN("testVerifySignature");
		byte[] data = createRandomBytes(1000);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		byte[] signature = security.createSignatureOfStream(inputStream);
		assertNotNull("signature was null", signature);

		ByteArrayInputStream verifyStream = new ByteArrayInputStream(data);
		assertEquals("Verify failed", true, security.verifySignature(verifyStream, signature));

		data[0] = (byte)(~data[0]);
		ByteArrayInputStream corruptStream = new ByteArrayInputStream(data);
		assertEquals("Verify passed on corrupt data", false, security.verifySignature(corruptStream, signature));
		TRACE_END();
	}

	public void testEncryptWithoutKeyPair() throws Exception
	{
		TRACE_BEGIN("testEncryptWithoutKeyPair");
		byte[] data = createRandomBytes(1000);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			securityWithoutKeyPair.encrypt(inputStream, outputStream);
			fail("encrypt without keypair worked?");
		}
		catch(MartusSecurity.NoKeyPairException e)
		{
			// expected exception
		}

		TRACE_END();
	}

	public void testEncryptBadStreams() throws Exception
	{
		TRACE_BEGIN("testEncryptBadStreams");
		byte[] data = createRandomBytes(1000);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			security.encrypt(null, outputStream);
			fail("encrypt with null input worked?");
		}
		catch(MartusSecurity.EncryptionException e)
		{
			// expected exception
		}
		try
		{
			security.encrypt(inputStream, null);
			fail("encrypt with null output worked?");
		}
		catch(MartusSecurity.EncryptionException e)
		{
			// expected exception
		}

		TRACE_END();
	}

	public void testDecryptWithoutKeyPair() throws Exception
	{
		TRACE_BEGIN("testDecryptWithoutKeyPair");
		byte[] data = createRandomBytes(1000);
		ByteArrayInputStreamWithSeek inputStream = new ByteArrayInputStreamWithSeek(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			securityWithoutKeyPair.decrypt(inputStream, outputStream);
			fail("decrypt without keypair worked?");
		}
		catch(MartusSecurity.NoKeyPairException e)
		{
			// expected exception
		}

		TRACE_END();
	}

	public void testDecryptBadStreams() throws Exception
	{
		TRACE_BEGIN("testDecryptBadStreams");
		byte[] data = createRandomBytes(1000);
		ByteArrayInputStreamWithSeek inputStream = new ByteArrayInputStreamWithSeek(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			security.decrypt(null, outputStream);
			fail("decrypt with null input worked?");
		}
		catch(MartusSecurity.DecryptionException e)
		{
			// expected exception
		}
		try
		{
			security.decrypt(inputStream, null);
			fail("decrypt with null output worked?");
		}
		catch(MartusSecurity.DecryptionException e)
		{
			// expected exception
		}

		TRACE_END();
	}

	public void testEncryptAndDecrypt() throws Exception
	{
		TRACE_BEGIN("testEncryptAndDecrypt");
		byte[] data = createRandomBytes(1000);
		ByteArrayInputStream plainInputStream = new ByteArrayInputStream(data);
		ByteArrayOutputStream cipherOutputStream = new ByteArrayOutputStream();

		security.encrypt(plainInputStream, cipherOutputStream);
		byte[] encrypted = cipherOutputStream.toByteArray();
		assertTrue("unreasonably short", encrypted.length > (9 * data.length) / 10);
		assertTrue("unreasonably long", encrypted.length < 3 * data.length);
		assertEquals("not encrypted?", false, Arrays.equals(data, encrypted));

		ByteArrayInputStreamWithSeek cipherInputStream = new ByteArrayInputStreamWithSeek(encrypted);
		ByteArrayOutputStream plainOutputStream = new ByteArrayOutputStream();
		security.decrypt(cipherInputStream, plainOutputStream);
		byte[] decrypted = plainOutputStream.toByteArray();
		assertEquals("got bad data back", true, Arrays.equals(data, decrypted));

		TRACE_END();
	}

	public void testEncryptAndUnableToDecrypt() throws Exception
    {
        TRACE_BEGIN("testEncryptAndUnableToDecrypt");
        security.setShouldWriteAuthorDecryptableData(false);
        byte[] data = createRandomBytes(1000);
        ByteArrayInputStream plainInputStream = new ByteArrayInputStream(data);
        ByteArrayOutputStream cipherOutputStream = new ByteArrayOutputStream();

        security.encrypt(plainInputStream, cipherOutputStream);
        byte[] encrypted = cipherOutputStream.toByteArray();
        assertEquals("not encrypted?", false, Arrays.equals(data, encrypted));

        ByteArrayInputStreamWithSeek cipherInputStream = new ByteArrayInputStreamWithSeek(encrypted);
        ByteArrayOutputStream plainOutputStream = new ByteArrayOutputStream();
        try {
            security.decrypt(cipherInputStream, plainOutputStream);
            byte[] decrypted = plainOutputStream.toByteArray();
            assertEquals("should have gotten bad data back", false, Arrays.equals(data, decrypted));
            fail("Should have thrown exception for attempt to decrypt with author decryptable set to false");
        } catch(Exception expected) {
            //expected error
        }
        security.setShouldWriteAuthorDecryptableData(true);
        TRACE_END();
    }

	private byte[] createRandomBytes(int length)
	{
		byte[] data = new byte[length];
		for(int i = 0 ; i < data.length; ++i)
		{
			data[i]= (byte)(i%100);
		}

		return data;
	}


	public void testDigestString() throws Exception
	{
		final String textToDigest = "This is a some text";
		long start = System.currentTimeMillis();
		String digest = MartusSecurity.createDigestString(textToDigest);
		long stop = System.currentTimeMillis();
		assertTrue("took too long", (stop - start) < 1000);
		assertNotNull("null digest", digest);

		String digest2 = MartusSecurity.createDigestString(textToDigest);
		assertEquals("different?", digest, digest2);

		//String otherText = textToDigest.replaceFirst("i", "j");
		// rewrite above line in java 1.3 compatible form:
		String otherText = textToDigest; // first assume no match found
		int idx = textToDigest.indexOf("i");
		if (idx >= 0)
			otherText = textToDigest.substring(0, idx) + "j" + textToDigest.substring(idx+1);

		String digest3 = MartusSecurity.createDigestString(otherText);
		assertNotEquals("same?", digest, digest3);
	}

	public void testCreateRandomToken()
	{
		String token1 = MartusSecurity.createRandomToken();
		assertEquals("Invalid Length?", 24, token1.length());
		String token2 = MartusSecurity.createRandomToken();
		assertNotEquals("Same token?", token1, token2);
	}
	
	public void testSigningVectorsOfVectors() throws Exception
	{
		String stringWithNewlines = "abc\n\ndef\n";
		byte[] bytesWithNewlines = stringWithNewlines.getBytes("UTF-8");
		assertEquals(stringWithNewlines.length(), bytesWithNewlines.length);
		
		Vector strings = new Vector();
		strings.add(stringWithNewlines);
		strings.add(stringWithNewlines);
		String vectorToString = strings.toString();
		assertEquals("[" + stringWithNewlines + ", " + stringWithNewlines + "]", vectorToString);
	}
	
	public void testVariousAESSizes() throws Exception
	{
		Vector results = new Vector();
		String text = "This will be encrypted";
		byte[] textBytes = text.getBytes("UTF-8");
		int[] goodSizes = {128, 192, 256};
		Random random = new Random();
		for (int i = 0; i < goodSizes.length; i++)
		{
			int size = goodSizes[i];
			byte[] keyBytes = new byte[size/8];
			for(int b = 0; b < keyBytes.length; ++b)
				keyBytes[b] = (byte)random.nextInt();
			SessionKey sessionKey = new SessionKey(keyBytes);
			byte[] cipherBytes = encryptBytes(textBytes, sessionKey);
			assertFalse("Bytes not encypted?", Arrays.equals(cipherBytes,textBytes));
			assertNotContains(cipherBytes, results);
			results.add(cipherBytes);
			String plainText = decryptBytes(cipherBytes, sessionKey);
			assertEquals(text, plainText);
		}
	}
	
	public void testThatAllAESKeyBytesAreUsed() throws Exception
	{
		byte[] sampleBytes = {32,23,5,3,7,53,2,35,54,7,3,23,5,2,45,45,75,8};
		int keySize = 256;
		byte[] keyBytes = new byte[keySize/8];
		Arrays.fill(keyBytes, (byte)0);
		SessionKey key = new SessionKey(keyBytes);
		byte[] baseResult = encryptBytes(sampleBytes, key);
		for(int i=0; i < keyBytes.length; ++i)
		{
			 keyBytes[i] = (byte)0xFF;
			 byte[] thisResult = encryptBytes(sampleBytes, key);
			 assertFalse("Not encypted?", Arrays.equals(thisResult, sampleBytes));
			 assertNotEquals(StreamableBase64.encode(baseResult), StreamableBase64.encode(thisResult));
			 keyBytes[i] = 0;
		}
	}

	private String decryptBytes(byte[] cipherBytes, SessionKey key)
		throws NoKeyPairException, DecryptionException
	{
		ByteArrayInputStreamWithSeek cipherIn = new ByteArrayInputStreamWithSeek(cipherBytes);
		ByteArrayOutputStream plainOut = new ByteArrayOutputStream();
		security.decrypt(cipherIn, plainOut, key);
		String plainText = new String(plainOut.toByteArray());
		return plainText;
	}

	private byte[] encryptBytes(byte[] textBytes, SessionKey key)
		throws NoKeyPairException, EncryptionException
	{
		ByteArrayInputStream plainIn = new ByteArrayInputStream(textBytes);
		ByteArrayOutputStream cipherOut = new ByteArrayOutputStream();
		security.encrypt(plainIn, cipherOut, key);
		byte[] cipherBytes = cipherOut.toByteArray();
		return cipherBytes;
	}
/*
	public void testSignatures()
	{
		MartusSecurity security = new MartusSecurity(12345);

		assertEquals("default empty string", "", security.createSignature(null, null));

		final String textToSign = "This is just some stupid text";
		KeyPair keys = security.createKeyPair();
		long start = System.currentTimeMillis();
		String signature = security.createSignature(textToSign, keys.getPrivate());
		long stop = System.currentTimeMillis();
		assertTrue("took too long", (stop - start) < 1000);
		assertNotNull("null sig?", signature);

		start = System.currentTimeMillis();
		boolean shouldWork = security.verifySignature(textToSign, keys.getPublic(), signature);
		stop = System.currentTimeMillis();
		assertTrue("took too long", (stop - start) < 1000);
		assertTrue("should have worked", shouldWork);

		start = System.currentTimeMillis();
		boolean shouldFail = security.verifySignature("Not the same text", keys.getPublic(), signature);
		stop = System.currentTimeMillis();
		assertTrue("took too long", (stop - start) < 1000);
		assertTrue("should have failed", !shouldFail);

		start = System.currentTimeMillis();
		boolean shouldFail2 = security.verifySignature(textToSign, keys.getPublic(), "not the sig");
		stop = System.currentTimeMillis();
		assertTrue("took too long", (stop - start) < 1000);
		assertTrue("should have failed2", !shouldFail2);
	}
*/
/* experimental
	public void testCipherSimple() throws Exception
	{
		Key key = createSessionKey();

		byte[] original = {1,1,2,3,5,7,11};
		byte[] encrypted = MartusSecurity.encrypt(original, key);
		assertNotNull("null encrypt", encrypted);
		assertTrue("empty encrypt", encrypted.length > 0);
		byte[] decrypted = MartusSecurity.decrypt(encrypted, key);
		assertNotNull("null decrypt", decrypted);
		assertTrue("empty decrypt", decrypted.length > 0);

		assertTrue("symmetric encrypt/decrypt", Arrays.equals(original, decrypted));
	}

	public void testCipherLarge() throws Exception
	{
		Key key = createSessionKey();

		final int SIZE = 100000;
		byte[] original = new byte[SIZE];
		for(int b = 0; b < SIZE; ++b)
		{
			original[b] = (byte)(b%256);
		}

		long startE = System.currentTimeMillis();
		byte[] encrypted = MartusSecurity.encrypt(original, key);
		long stopE = System.currentTimeMillis();
		assertNotNull("null encrypt", encrypted);
		assertTrue("empty encrypt", encrypted.length > 0);
		long startD = System.currentTimeMillis();
		byte[] decrypted = MartusSecurity.decrypt(encrypted, key);
		long stopD = System.currentTimeMillis();
		assertNotNull("null decrypt", decrypted);
		assertTrue("empty decrypt", decrypted.length > 0);
		assertTrue("symmetric encrypt/decrypt", Arrays.equals(original, decrypted));
		//System.out.println("Encrypt " + SIZE + " bytes: " + (stopE - startE));
		//System.out.println("Decrypt " + SIZE + " bytes: " + (stopD - startD));
		assertTrue("encrypt slow", (stopE - startE) < 1000);
		assertTrue("decrypt slow", (stopD - startD) < 1000);
	}

	public void testCipherMany() throws Exception
	{
		Key key = createSessionKey();

		final int SIZE = 100;
		byte[] original = new byte[SIZE];
		for(int b = 0; b < SIZE; ++b)
		{
			original[b] = (byte)(b%256);
		}

		final int TIMES = 1000;
		long startE = System.currentTimeMillis();
		byte[] encrypted = null;
		for(int e = 0; e < TIMES; ++e)
			encrypted = MartusSecurity.encrypt(original, key);
		long stopE = System.currentTimeMillis();
		assertNotNull("null encrypt", encrypted);
		assertTrue("empty encrypt", encrypted.length > 0);
		byte[] decrypted = null;
		long startD = System.currentTimeMillis();
		for(int d = 0; d < TIMES; ++d)
			decrypted = MartusSecurity.decrypt(encrypted, key);
		long stopD = System.currentTimeMillis();
		assertNotNull("null decrypt", decrypted);
		assertTrue("empty decrypt", decrypted.length > 0);
		assertTrue("symmetric encrypt/decrypt", Arrays.equals(original, decrypted));
		//System.out.println("Encrypt " + TIMES + " times: " + (stopE - startE));
		//System.out.println("Decrypt " + TIMES + " times: " + (stopD - startD));
		assertTrue("encrypt slow", (stopE - startE) < 1000);
		assertTrue("decrypt slow", (stopD - startD) < 1000);
	}

	private Key createSessionKey()
	{
		MartusSecurity security = new MartusSecurity(12345);

		long start = System.currentTimeMillis();
		Key key = security.createSessionKey();
		long stop = System.currentTimeMillis();
		assertTrue("took too long", (stop - start) < 1000);
		assertNotNull("null session key", key);

		return key;
	}
*/

	private static MartusSecurity security;
	private static MartusSecurity securityWithoutKeyPair;
	private static KeyPair invalidKeyPair;
	final int SMALLEST_LEGAL_KEY_FOR_TESTING = 512;

}
