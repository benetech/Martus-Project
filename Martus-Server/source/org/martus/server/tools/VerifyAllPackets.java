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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.CryptoInitializationException;
import org.martus.common.crypto.MartusCrypto.InvalidKeyPairFileVersionException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.FileDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.Packet;
import org.martus.util.UnicodeReader;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


public class VerifyAllPackets
{

	public VerifyAllPackets()
	{
		super();
	}

	public static void main(String[] args)
	{		
		File dir = null;
		File keyPairFile = null;
		boolean prompt = true;
		int exitStatus = 0;

		System.out.println("VerifyAllPackets Martus Database Integrity Checker");
		System.out.println("  Runs a SAFE, non-destructive, read-only test");
				
		for (int i = 0; i < args.length; i++)
		{
			if(args[i].startsWith("--keypair"))
			{
				keyPairFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--packet-directory="))
			{
				dir = new File(args[i].substring(args[i].indexOf("=")+1));
			}

			if( args[i].startsWith("--no-prompt") )
			{
				prompt = false;
			}
		}

		if(dir == null || keyPairFile == null )
		{
			System.err.println("\nUsage: VerifyAllPackets --packet-directory=<directory> --keypair=<pathToKeyPairFile> [--no-prompt]");
			System.exit(2);
		}
		
		if(!dir.exists() || !dir.isDirectory())
		{
			System.err.println("Cannot find directory: " + dir);
			System.exit(3);
		}
		
		if(!keyPairFile.exists() || !keyPairFile.isFile())
		{
			System.err.println("Cannot find file: " + keyPairFile);
			System.exit(3);
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
			//TODO security issue here password is a string.
			String passphrase = reader.readLine();
			security = loadCurrentMartusSecurity(keyPairFile, passphrase.toCharArray());
		}
		catch(Exception e)
		{
			System.err.println("VerifyAllPackets.main: " + e);
			System.exit(3);
		}
		
		try
		{
			ServerFileDatabase db = new ServerFileDatabase(dir,security);
			db.initialize();
			exitStatus = verifyAllPackets(db, security);
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e);
			System.exit(3);
		}
		
		System.exit(exitStatus);
	}
	
	static int verifyAllPackets(Database db, MartusCrypto security)
	{
		PacketVerifier verifier = new PacketVerifier(db, security);
		db.visitAllRecords(verifier);
		System.out.println();
		
		return verifier.getExitStatus();
	}
	
	private static MartusCrypto loadCurrentMartusSecurity(File keyPairFile, char[] passphrase)
		throws CryptoInitializationException, FileNotFoundException, IOException, InvalidKeyPairFileVersionException, AuthorizationFailedException
	{
		MartusCrypto security = new MartusSecurity();
		FileInputStream in = new FileInputStream(keyPairFile);
		security.readKeyPair(in, passphrase);
		in.close();
		return security;
	}

	static class PacketVerifier implements Database.PacketVisitor
	{
		PacketVerifier(Database databaseToUse, MartusCrypto securityToUse)
		{
			db = databaseToUse;
			security = securityToUse;
			exitStatus = 0;
		}
		
		public void visit(DatabaseKey visitingKey)
		{
			System.out.print(".");
			System.out.flush();

			String visitingLocalId = visitingKey.getLocalId();
			try
			{
				InputStreamWithSeek inForValidate = db.openInputStream(visitingKey, security);
				String accountId = visitingKey.getAccountId();
				Packet.validateXml(inForValidate, accountId, visitingLocalId, null, security);
				inForValidate.close();
				
				if(BulletinHeaderPacket.isValidLocalId(visitingLocalId))
				{
					BulletinHeaderPacket bhp = BulletinStore.loadBulletinHeaderPacket(db, visitingKey, security);
					
					DatabaseKey[] keys = BulletinZipUtilities.getAllPacketKeys(bhp);
					for (int i = 0; i < keys.length; i++)
					{
						if(!db.doesRecordExist(keys[i]))
						{
							exitStatus = 1;
							System.err.println();
							System.err.println("Missing packet: " + keys[i].getLocalId());
							System.err.println("  for header: " + visitingLocalId);
							
							FileDatabase fdb = (FileDatabase) db;
							File bucket = fdb.getFileForRecord(keys[i]);
							String path = bucket.getParent().substring(bucket.getParent().indexOf("packets"));
							System.err.println("      for account bucket: " + path);
						}
					}
				}
				
			}
			catch (Exception e)
			{
				System.err.println("Exception on packet: " + visitingLocalId);
				e.printStackTrace();
			}
			
		}
		
		public int getExitStatus()
		{
			return exitStatus;
		}

		Database db;
		MartusCrypto security;
		int exitStatus;
	}
}
