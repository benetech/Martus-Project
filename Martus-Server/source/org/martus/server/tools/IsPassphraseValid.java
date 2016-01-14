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

import org.martus.common.DammCheckDigitAlgorithm.CheckDigitInvalidException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.UnicodeReader;

public class IsPassphraseValid
{
	public static void main(String[] args)
	{
		File keyPairFile = null;
		boolean prompt = true;
		
		for (int i = 0; i < args.length; i++)
		{
			if( args[i].startsWith("--keypair") )
			{
				keyPairFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if( args[i].startsWith("--no-prompt") )
			{
				prompt = false;
			}
		}
		
		if(keyPairFile == null)
		{
				System.err.println("Error: Incorrect argument.\nIsPassphraseValid --keypair=/path/keypair.dat [--no-prompt]" );
				System.err.flush();
				System.exit(2);
		}
		
		if(!keyPairFile.isFile() || !keyPairFile.exists() )
		{
			System.err.println("Error: " + keyPairFile.getAbsolutePath() + " is not a file" );
			System.err.flush();
			System.exit(3);
		}
		
		if(prompt)
		{
			System.out.print("Enter passphrase: ");
			System.out.flush();
		}

		String passphrase = null;
		try
		{
			BufferedReader stdin = new BufferedReader(new UnicodeReader(System.in));
//			TODO security issue password is a string
			passphrase = stdin.readLine();
		}
		catch (IOException e)
		{
			System.err.println("Error: " + e.toString() );
			System.err.flush();
			System.exit(3);
		}

		try
		{
			MartusSecurity security = (MartusSecurity) MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase.toCharArray());
			System.out.println("Public Code (Old): " + MartusCrypto.computeFormattedPublicCode(security.getPublicKeyString()));
			System.out.println("Public Code (New): " + MartusCrypto.computeFormattedPublicCode40(security.getPublicKeyString()));
			System.exit(0);
		}
		catch (AuthorizationFailedException e)
		{
			System.err.println("Error: " + e.toString() );
			System.err.flush();
			System.exit(1);
		}
		catch (CreateDigestException e)
		{
			System.err.println("Error: " + e.toString() );
			System.err.flush();
			System.exit(1);
		}
		catch (CheckDigitInvalidException e)
		{
			System.err.println("Error: " + e.toString() );
			System.err.flush();
			System.exit(1);
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e.toString() );
			System.err.flush();
			System.exit(3);
		}
	}
}