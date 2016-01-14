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
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.UnicodeReader;

public class ExportPublicKey
{
	public static void main(String[] args)
	{
		File keypair = null;
		File outputfile = null;
		boolean prompt = true;
		for (int i = 0; i < args.length; i++)
		{
			if(args[i].startsWith("--no-prompt"))
			{
				prompt = false;
			}
			
			if(args[i].startsWith("--keypair"))
			{
				keypair = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--file"))
			{
				outputfile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
		}
		
		if(keypair == null || outputfile == null)
		{
			System.err.println("Incorrect arguments: ExportPublicKey [--no-prompt] --keypair=keypair.dat --file=pubkey.dat\n");
			System.exit(2);
		}
		
		if(!keypair.exists())
		{
			System.err.println("Unable to find keypair\n");
			System.exit(3);
		}
		
		if(prompt)
		{
			System.out.print("Enter server passphrase:");
			System.out.flush();
		}
		
		MartusCrypto security = null;
		try
		{
			BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));
			//TODO security issue password is a string
			String passphrase = reader.readLine();
			security = MartusServerUtilities.loadCurrentMartusSecurity(keypair, passphrase.toCharArray());
		}
		catch(Exception e)
		{
			System.err.println("ExportPublicKey.main: " + e + "\n");
			System.exit(3);
		}
		
		try
		{
			MartusUtilities.exportServerPublicKey(security, outputfile);
		}
		catch (Exception e)
		{
			System.err.println("ExportPublicKey.main: " + e + "\n");
			System.exit(3);
		}

		if(prompt)
		{
			System.out.println("Public key exported to file " + outputfile.getAbsolutePath() + "\n");
			System.out.flush();
		}
		System.exit(0);
	}
}
