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

package org.martus.server.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeReader;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;


public class DecryptFile
{
	public static class IncorrectEncryptedFileIdentifierException extends Exception 
	{
	}
	
	public static class IncorrectPublicKeyException extends Exception 
	{
	}
	
	public static class DigestFailedException extends Exception 
	{
	}
	
	public static void main(String[] args) throws IOException
	{
		File keyPairFile = null;
		File plainTextFile = null;
		File cryptoFile = null;
		MartusSecurity security = null;
		boolean prompt = true;
		
		for (int i = 0; i < args.length; i++)
		{			
			if(args[i].startsWith("--keypair"))
			{
				keyPairFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--plaintext-file"))
			{
				plainTextFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}

			if(args[i].startsWith("--crypto-file"))
			{
				cryptoFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--no-prompt"))
			{
				prompt = false;
			}
		}
		
		if(keyPairFile == null || cryptoFile == null)
		{
			System.err.println("Incorrect arguments: DecryptFile [--no-prompt] --keypair=<keypair.dat> --crypto-file=<input> --plaintext-file=<output>");
			System.exit(2);
		}
		
		if(prompt)
		{
			System.out.print("Enter passphrase: ");
			System.out.flush();
		}

		BufferedReader stdin = new BufferedReader(new UnicodeReader(System.in));
		String passphrase = null;
		try
		{
//			TODO security issue password is a string
			passphrase = stdin.readLine();
		}
		catch (IOException e)
		{
			System.err.println("Error: " + e.toString() );
			System.err.flush();
			System.exit(3);
		}

		UnicodeReader encryptedFileReader = null;
		OutputStream plainTextOutput = null;
		try
		{
			security = (MartusSecurity) MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase.toCharArray());
			
			encryptedFileReader = new UnicodeReader(cryptoFile);
			String identifierBytesRetrieved = encryptedFileReader.readLine();
			String retrievedPublicKeyString = encryptedFileReader.readLine();
			String retrievedDigest = encryptedFileReader.readLine();
			String retrievedEncryptedText = encryptedFileReader.readLine();
			
			String publicKeyString = security.getPublicKeyString();
			if(! publicKeyString.equals(retrievedPublicKeyString))
			{
				throw new IncorrectPublicKeyException();
			}
			
			String identifierBytesExpected =  MartusSecurity.geEncryptedFileIdentifier();
			if(! identifierBytesExpected.equals(identifierBytesRetrieved))
			{
				throw new IncorrectEncryptedFileIdentifierException();
			}
			
			plainTextOutput = new FileOutputStream(plainTextFile);
			decryptToFile(security, plainTextOutput, retrievedEncryptedText);
			plainTextOutput.close();
			
			byte [] plainFileContents = MartusServerUtilities.getFileContents(plainTextFile);			
			String calculatedDigest = StreamableBase64.encode(MartusSecurity.createDigest(plainFileContents));
			if(! calculatedDigest.equals(retrievedDigest))
			{
				throw new DigestFailedException();
			}			
		}
		catch (AuthorizationFailedException e)
		{
			System.err.println("Error: " + e.toString() );
			System.exit(1);
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e.toString() );
			System.exit(3);
		}
		finally
		{
			encryptedFileReader.close();
		}
		if(prompt)
		{
			System.out.println("File " + cryptoFile + " was decrypted to " + plainTextFile);
		}
		System.exit(0);
	}

	public static void decryptToFile(MartusCrypto security, OutputStream plainTextOutput, String retrievedEncryptedText)
		throws InvalidBase64Exception, NoKeyPairException, DecryptionException
	{
		byte[] encryptedBytes = StreamableBase64.decode(retrievedEncryptedText);
		ByteArrayInputStreamWithSeek inEncrypted = new ByteArrayInputStreamWithSeek(encryptedBytes);
		security.decrypt(inEncrypted, plainTextOutput);
	}
}
