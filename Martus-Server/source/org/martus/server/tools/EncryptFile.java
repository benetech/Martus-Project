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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeWriter;

public class EncryptFile
{
	public static void main(String[] args)
	{
		File publicKeyFile = null;
		File plainTextFile = null;
		File cryptoFile = null;
		MartusSecurity security = null;
		
		for (int i = 0; i < args.length; i++)
		{			
			if(args[i].startsWith("--pubkey"))
			{
				publicKeyFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--plaintext-file"))
			{
				plainTextFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}

			if(args[i].startsWith("--crypto-file"))
			{
				cryptoFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
		}
		
		if(publicKeyFile == null || plainTextFile == null)
		{
			System.err.println("Incorrect arguments: EncryptFile --pubkey=<pubkey.dat> --plaintext-file=<input> --crypto-file=<output>");
			System.exit(2);
		}

		InputStream plainStream = null;
		ByteArrayOutputStream cipherByteArrayOutputStream = null;
		try
		{
			security = new MartusSecurity();
			
			byte [] plainFileContents = MartusServerUtilities.getFileContents(plainTextFile);
			String digest = StreamableBase64.encode(MartusSecurity.createDigest(plainFileContents));
			
			plainStream = new FileInputStream(plainTextFile);		
			cipherByteArrayOutputStream = new ByteArrayOutputStream();
			
			Vector publicInfo = MartusUtilities.importServerPublicKeyFromFile(publicKeyFile, security);
			String publicKeyString = (String) publicInfo.get(0);
			
			security.encrypt(plainStream, cipherByteArrayOutputStream, security.createSessionKey(), publicKeyString);
			String encodedEncryptedFile = StreamableBase64.encode(cipherByteArrayOutputStream.toByteArray());
					
			UnicodeWriter writer = new UnicodeWriter(cryptoFile);
			writer.writeln(MartusSecurity.geEncryptedFileIdentifier());
			writer.writeln(publicKeyString);
			writer.writeln(digest);
			writer.writeln(encodedEncryptedFile);
			
			writer.close();
		}
		catch (Exception e)
		{
			System.err.println("EncryptFile.main: " + e);
			e.printStackTrace();
			System.exit(3);
		}
		finally
		{
			try
			{
				cipherByteArrayOutputStream.close();
				plainStream.close();
			}
			catch(IOException ignoredException)
			{}
		}

		System.exit(0);
	}
}
