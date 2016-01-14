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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.UnicodeReader;

public class ChangeKeypairPassphrase
{
	public static void main(String[] args) throws Exception
	{
			System.out.println("ChangeServerPassphrase:\nThis program will replace your keypair.dat file."
				+ "\nWe strongly recommend that you make sure you have a backup copy before running this program. "
				+ "\nAlso, after successfully changing the password, we strongly recommend that you create a backup of the new keypair.dat file.\n");

			if( args.length == 0 || !args[0].startsWith("--keypair"))
			{
					System.err.println("Error: Incorrect argument.\nChangeServerPassphrase --keypair=/path/keypair.dat" );
					System.err.flush();
					System.exit(2);				
			}
			File keyPairFile = new File(args[0].substring(args[0].indexOf("=")+1));

			if(!keyPairFile.exists())
			{
				System.err.println("Error: There is no keypair file at location " + keyPairFile.getAbsolutePath() + "." );
				System.err.flush();
				System.exit(3);
			}

			System.out.print("Enter current passphrase:");
			System.out.flush();
			
			UnicodeReader rawReader = new UnicodeReader(System.in);	
			BufferedReader reader = new BufferedReader(rawReader);
			try
			{
				//TODO security issue here password is a string
				String oldPassphrase = reader.readLine();
				
				MartusCrypto security = MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, oldPassphrase.toCharArray());
				
				System.out.print("Enter new passphrase:");
				System.out.flush();
				//TODO security issue here password is a string
				String newPassphrase1 = reader.readLine();
				System.out.print("Re-enter the new passphrase:");
				System.out.flush();
				String newPassphrase2 = reader.readLine();
				
				if( newPassphrase1.equals(newPassphrase2) )
				{
					System.out.println("Updating passphrase...");
					System.out.flush();
					updateKeypairPassphrase(keyPairFile, newPassphrase1.toCharArray(), security);
				}
				else
				{
					System.err.println("Passwords not the same");
					System.exit(3);
				}
			}
			catch(Exception e)
			{
				System.err.println("ChangeServerPassphrase.main: " + e);
				System.exit(3);
			}
			System.out.println("Server passphrase updated.");
			System.out.flush();
			System.exit(0);
	}
	
	private static void updateKeypairPassphrase(File keyPairFile, char[] newPassphrase, MartusCrypto security)
		throws FileNotFoundException, Exception
	{
		FileOutputStream out = new FileOutputStream(keyPairFile);
		security.writeKeyPair(out, newPassphrase);
	}
}