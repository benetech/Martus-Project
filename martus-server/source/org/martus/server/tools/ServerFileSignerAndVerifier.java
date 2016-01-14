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
import java.io.IOException;
import java.text.ParseException;

import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.common.utilities.MartusServerUtilities.MartusSignatureFileAlreadyExistsException;
import org.martus.common.utilities.MartusServerUtilities.MartusSignatureFileDoesntExistsException;
import org.martus.util.UnicodeReader;

public class ServerFileSignerAndVerifier
{
	public static void main(String[] args) throws Exception
	{
			System.out.println("ServerFileSignerAndVerifier:\nUse this program to create a signature file of a specified file"
								+ " or to verify a file against it's signature file.");
			File keyPairFile = null;
			File fileForOperation = null;
			File signatureFile = null;
			boolean isSigningOperation = true;
			
			for (int i = 0; i < args.length; i++)
			{
				if(args[i].startsWith("--keypair"))
				{
					keyPairFile = new File(args[i].substring(args[i].indexOf("=")+1));
				}
				
				if(args[i].startsWith("--file"))
				{
					fileForOperation = new File(args[i].substring(args[i].indexOf("=")+1));
				}
				
				if(args[i].startsWith("--verify"))
				{
					isSigningOperation = false;
				}
				
				if(args[i].startsWith("--signature"))
				{
					signatureFile = new File(args[i].substring(args[i].indexOf("=")+1));
				}
			}
			
			if(keyPairFile == null || fileForOperation == null )
			{
				System.err.println("\nUsage:\n FileSignerAndVerifier [--sign | --verify --signature=<pathOfFileSignature>] --keypair=<pathOfKeyFile> --file=<pathToFileToSignOrVerify>");
				System.exit(2);
			}
			
			if(!keyPairFile.isFile() || !keyPairFile.exists())
			{
				System.err.println("Error: " + keyPairFile.getAbsolutePath() + " is not a file" );
				System.err.flush();
				System.exit(3);
			}
			
			if(!fileForOperation.isFile() || !fileForOperation.exists())
			{
				System.err.println("Error: " + fileForOperation.getAbsolutePath() + " is not a file" );
				System.err.flush();
				System.exit(3);
			}

			System.out.print("Enter server passphrase:");
			System.out.flush();
			
			MartusCrypto security = null;
			BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));
			try
			{
				//TODO security issue password is a string
				String passphrase = reader.readLine();
				security = MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase.toCharArray());
			}
			catch(Exception e)
			{
				System.err.println("FileSignerAndVerifier.main: " + e);
				System.exit(3);
			}
			finally
			{
				reader.close();
			}
			
			try
			{
				if(isSigningOperation)
				{
					signFile(fileForOperation, security);
				}
				else
				{
					verifyFile(fileForOperation, signatureFile, security);
				}
			}
			catch(Exception e)
			{
				System.out.println(e.toString());
				System.exit(3);
			}
			System.exit(0);
	}

	public static void signFile(File fileForOperation, MartusCrypto security)
		throws IOException, MartusSignatureException, InterruptedException, MartusSignatureFileAlreadyExistsException
	{
		File signatureFile;
		signatureFile = MartusServerUtilities.createSignatureFileFromFileOnServer(fileForOperation, security);
		System.out.println("Signature file created at " + signatureFile.getAbsolutePath());
	}
	
	public static void verifyFile(File fileForOperation, File signatureFile, MartusCrypto security)
		throws IOException, ParseException, MartusSignatureFileDoesntExistsException, FileVerificationException
	{
		if(signatureFile == null)
		{
			signatureFile = MartusServerUtilities.getLatestSignatureFileFromFile(fileForOperation);
		}
		MartusServerUtilities.verifyFileAndSignatureOnServer(fileForOperation, signatureFile, security, security.getPublicKeyString());
		System.out.println("File " + fileForOperation.getAbsolutePath()
							+ " verified successfully against signature file "
							+ signatureFile.getAbsolutePath() + ".");
	}
}