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

package org.martus.common.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.util.StreamableBase64;

public class MartusJceKeyPair extends MartusKeyPair
{
	public MartusJceKeyPair(KeyPair keyPair, SecurityContext securityContext)
	{
		providerAccessor = securityContext;
		setJceKeyPair(keyPair);
	}

	public MartusJceKeyPair(SecureRandom randomGenerator, SecurityContext securityContext) throws Exception
	{
		providerAccessor = securityContext;
		rand = randomGenerator;
	}
	
	public PrivateKey getPrivateKey()
	{
		KeyPair pair = getJceKeyPair();
		if(pair == null)
			return null;
		return pair.getPrivate();
	}
	
	public PublicKey getPublicKey()
	{
		KeyPair pair = getJceKeyPair();
		if(pair == null)
			return null;
		return pair.getPublic();
	}

	public String getPublicKeyString()
	{
		return(getKeyString(getPublicKey()));
	}

	public void clear()
	{
		jceKeyPair = null;
	}
	
	public boolean hasKeyPair()
	{
		return (jceKeyPair != null);
	}
	
	public boolean isKeyPairValid()
	{
		return isKeyPairValid(getJceKeyPair());
	}
	
	public void createRSA(int publicKeyBits) throws Exception
	{
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM_NAME, getProviderName());
		keyPairGenerator.initialize(publicKeyBits, rand);
		setJceKeyPair(keyPairGenerator.genKeyPair());
	}
	
	public byte[] getKeyPairData() throws Exception
	{
		KeyPair jceKeyPairToWrite = getJceKeyPair();
		return MartusJceKeyPair.getKeyPairData(jceKeyPairToWrite);
	}

	public void setFromData(byte[] data) throws Exception
	{
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		DataInputStream dataInputStream = new DataInputStream(inputStream);
		try
		{
			KeyPair candidatePair = MartusKeyPairLoader.load(dataInputStream, providerAccessor);
			if(!isKeyPairValid(candidatePair))
				throw (new AuthorizationFailedException());
			setJceKeyPair(candidatePair);
		}
		catch(RuntimeException e)
		{
			//e.printStackTrace();
			throw new AuthorizationFailedException(e);
		}
	}
	
	public byte[] encryptBytes(byte[] bytesToEncrypt, String recipientPublicKeyX509) throws Exception
	{
		PublicKey publicKey = extractPublicKey(recipientPublicKeyX509);
		return encryptBytes(bytesToEncrypt, publicKey);
	}
	
	public byte[] decryptBytes(byte[] bytesToDecrypt) throws Exception
	{
		PrivateKey privateKey = jceKeyPair.getPrivate();
		return decryptBytes(bytesToDecrypt, privateKey);
	}

	public static PublicKey extractPublicKey(String publicKeyX509)
	{
		//System.out.println("key=" + base64PublicKey);
		try
		{
			EncodedKeySpec keySpec = new X509EncodedKeySpec(StreamableBase64.decode(publicKeyX509));
			KeyFactory factory = KeyFactory.getInstance(RSA_ALGORITHM_NAME);
			PublicKey publicKey = factory.generatePublic(keySpec);
			return publicKey;
		}
		catch(NoSuchAlgorithmException e)
		{
			System.out.println("MartusSecurity.extractPublicKey: " + e);
		}
		catch(InvalidKeySpecException expectedForInvalidKey)
		{
			//System.out.println("MartusSecurity.extractPublicKey: " + e);
		}
		catch(StreamableBase64.InvalidBase64Exception expectedForInvalidKey)
		{
			//System.out.println("MartusSecurity.extractPublicKey: " + e);
		}
		catch(IllegalArgumentException expectedForInvalidKey)
		{
			//System.out.println("MartusSecurity.extractPublicKey: " + e);
		}
	
		return null;
	}
	
	public static boolean isKeyPairValid(KeyPair candidatePair)
	{
		if(candidatePair == null)
			return false;
	
		try
		{
			byte[] samplePlainText = {1,2,3,4,127};
	
			PublicKey encryptWithKey = candidatePair.getPublic();
			byte[] cipherText = encryptBytes(samplePlainText, encryptWithKey);
			
			PrivateKey decryptWithKey = candidatePair.getPrivate();
			byte[] result = decryptBytes(cipherText, decryptWithKey);
			
			if(!Arrays.equals(samplePlainText, result))
				return false;
		}
		catch(Exception e)
		{
			return false;
		}
	
		return true;
	}
	public byte[] getDigestOfPartOfPrivateKey() throws Exception
	{
		byte[] privateKey = getJceKeyPair().getPrivate().getEncoded();
		byte[] quarter = new byte[privateKey.length / 4];
		for(int i=0; i < quarter.length; ++i)
			quarter[i] = privateKey[i*4];
		return MartusCrypto.createDigest(quarter);
		
	}
	
	public KeyPair getJceKeyPair()
	{
		return jceKeyPair;
	}

	private void setJceKeyPair(KeyPair jceKeyPair)
	{
		this.jceKeyPair = jceKeyPair;
	}

	public static byte[] getKeyPairData(KeyPair jceKeyPairToWrite) throws Exception
	{
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(data);
		MartusKeyPairSaver.save(out, jceKeyPairToWrite);
		return data.toByteArray();
	}

	private static Cipher createRSAEncryptor(PublicKey key) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException
	{
		return createRSAEngine(key, Cipher.ENCRYPT_MODE);
	}

	private static Cipher createRSADecryptor(PrivateKey key) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException
	{
		return createRSAEngine(key, Cipher.DECRYPT_MODE);
	}

	private static Cipher createRSAEngine(Key key, int mode) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException
	{
		Cipher rsaCipherEngine = Cipher.getInstance(RSA_ALGORITHM, getProviderName());
		rsaCipherEngine.init(mode, key, rand);
		return rsaCipherEngine;
	}

	private static byte[] encryptBytes(byte[] bytesToEncrypt, PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher rsaCipherEngine = createRSAEncryptor(publicKey);
		return rsaCipherEngine.doFinal(bytesToEncrypt);
	}

	private static byte[] decryptBytes(byte[] bytesToDecrypt, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher rsaCipherEngine = createRSADecryptor(privateKey);
		return rsaCipherEngine.doFinal(bytesToDecrypt);
	}

	static public String getKeyString(Key key)
	{
		if(key == null)
			return null;
		return StreamableBase64.encode(key.getEncoded());
	}

	public static String getProviderName()
	{
		return providerAccessor.getSecurityProviderName();
	}

	private static SecureRandom rand;
	private KeyPair jceKeyPair;
	private static SecurityContext providerAccessor;

	static final String RSA_ALGORITHM_NAME = "RSA";
	private static final String RSA_ALGORITHM = "RSA/NONE/PKCS1Padding";
}
