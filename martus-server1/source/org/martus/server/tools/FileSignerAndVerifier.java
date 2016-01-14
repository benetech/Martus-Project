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

import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.UnicodeReader;

public class FileSignerAndVerifier
{
	public static void main(String[] args) throws Exception
	{
			System.out.println("FileSignerAndVerifier:\nUse this program to create a signature file of a specified file"
								+ " or to verify a file against it's signature file.");
			File keyPairFile = null;
			File fileForOperation = null;
			
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
			}
			
			if(keyPairFile == null || fileForOperation == null)
			{
				System.err.println("\nUsage:\n FileSignerAndVerifier --keypair=<pathOfKeyFile> --file=<pathToFileToSignOrVerify>");
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
			try
			{
				BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));
				//TODO security issue password is a string
				String passphrase = reader.readLine();
				security = MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase.toCharArray());
			}
			catch(Exception e)
			{
				System.err.println("FileSignerAndVerifier.main: " + e);
				System.exit(3);
			}
			
			File signatureFile = MartusUtilities.getSignatureFileFromFile(fileForOperation);
			
			if(signatureFile.exists())
			{
				try
				{
					MartusUtilities.verifyFileAndSignature(fileForOperation, signatureFile, security, security.getPublicKeyString());
				}
				catch(FileVerificationException e)
				{
					System.err.println("File " + fileForOperation.getAbsolutePath()
										+ " did not verify against signature file "
										+ signatureFile.getAbsolutePath() + ".");
					System.exit(3);
				}
				
				System.out.println("File " + fileForOperation.getAbsolutePath()
									+ " verified successfully against signature file "
									+ signatureFile.getAbsolutePath() + ".");
			}
			else
			{
				signatureFile = MartusUtilities.createSignatureFileFromFile(fileForOperation, security);
				System.out.println("Signature file created at " + signatureFile.getAbsolutePath());
			}
			System.exit(0);
	}
}