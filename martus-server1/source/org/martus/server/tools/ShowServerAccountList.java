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

import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoInitializationException;
import org.martus.common.database.Database;
import org.martus.common.database.FileDatabase;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.UnicodeReader;

public class ShowServerAccountList 
{
	public static void main(String[] args)
		throws FileDatabase.MissingAccountMapException, MartusUtilities.FileVerificationException, CryptoInitializationException, MissingAccountMapSignatureException
	{
		File dataDir = null;
		File keyPairFile = null;
		boolean prompt = true;
		
		for (int i = 0; i < args.length; i++)
		{
			if(args[i].startsWith("--keypair"))
			{
				keyPairFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--packet-directory="))
			{
				dataDir = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--no-prompt"))
			{
				prompt = false;
			}
		}
		
		if(dataDir == null || keyPairFile == null )
		{
			System.err.println("\nUsage: ShowServerAccountList --packet-directory=<directory> --keypair=<pathToKeyPairFile>");
			System.exit(2);
		}
		
		if(!keyPairFile.exists() || !keyPairFile.isFile() )
		{
			System.err.println("Error: " + keyPairFile + " is not a valid keypair file.");
			System.exit(2);
		}
		
		if(!dataDir.exists() || !dataDir.isDirectory() )
		{
			System.err.println("Error: " + dataDir + " is not a valid data directory.");
			System.exit(2);
		}
		
		MartusCrypto security = null;
		
		if(prompt)
		{
			System.out.print("Enter server passphrase:");
			System.out.flush();
		}
		
		try
		{
			BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));
			//TODO password is a string
			String passphrase = reader.readLine();
			security = MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase.toCharArray());
		}
		catch(Exception e)
		{
			System.err.println("ShowServerAccountList.main: " + e);
			System.exit(3);
		}

		new ShowServerAccountList(dataDir, security);
		System.exit(0);
	}
	
	ShowServerAccountList(File dataDirectory, MartusCrypto security) throws CryptoInitializationException, MissingAccountMapException, MissingAccountMapSignatureException, FileVerificationException
	{		
		fileDatabase = new ServerFileDatabase(dataDirectory, security);
		fileDatabase.initialize();
		fileDatabase.visitAllAccounts(new AccountVisitor());
	}

	class AccountVisitor implements Database.AccountVisitor 
	{
		public void visit(String accountString)
		{
			try {
				File accountDir = fileDatabase.getAbsoluteAccountDirectory(accountString);
				File bucket = accountDir.getParentFile();
				String publicCode = "";
				try
				{
					publicCode = MartusCrypto.computeFormattedPublicCode(accountString);
				}
				catch(Exception e)
				{
					publicCode = "ERROR: " + e;
				}
				
				System.out.println(publicCode + "=" + bucket.getName() + "/" + accountDir.getName());
			} 
			catch (Exception e) 
			{
				MartusLogger.logException(e);
			}
		}
	}

	FileDatabase fileDatabase;
}
