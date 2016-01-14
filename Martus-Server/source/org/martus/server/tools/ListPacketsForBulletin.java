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

import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.server.main.MartusServer;
import org.martus.util.UnicodeReader;

public class ListPacketsForBulletin
{
	public static void main(String[] args)
	{
		String bulletinLocalId = null;
		String accountPublicKey = null;
		File packetDir = null;
		MartusSecurity security = null;
		File serverKeyPairFile = null;
		boolean prompt = true;
		
		for (int i = 0; i < args.length; i++)
		{			
			if(args[i].startsWith("--header-packet"))
			{
				bulletinLocalId = args[i].substring(args[i].indexOf("=") + 1);
			}
			
			if(args[i].startsWith("--packet-directory="))
			{
				packetDir = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--keypair"))
			{
				serverKeyPairFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
			
			if(args[i].startsWith("--account"))
			{
				accountPublicKey = args[i].substring(args[i].indexOf("=")+1);
			}
			
			if(args[i].startsWith("--public-key"))
			{
				accountPublicKey = args[i].substring(args[i].indexOf("=")+1);
			}
			
			if(args[i].startsWith("--no-prompt"))
			{
				prompt = false;
			}
		}
		
		try
		{
			if(packetDir == null)
			{
				packetDir = new File(MartusServer.getDefaultDataDirectoryPath(), "packets" );
				if(! packetDir.exists())
				{
					throw new IllegalArgumentException("Couldn't find packets: " + packetDir);
				}
			}
			
			if(serverKeyPairFile == null)
			{
				File configDir = new File(MartusServer.getDefaultDataDirectoryPath(), "deleteOnStartup");
				serverKeyPairFile = new File(configDir, MartusServer.getKeypairFilename());
				if(! serverKeyPairFile.exists())
				{
					throw new IllegalArgumentException("Couldn't find keypair: " + serverKeyPairFile);
				}
			}
			
			if(bulletinLocalId == null || accountPublicKey == null)
			{
				throw new IllegalArgumentException("Missing account or bulletin id");
			}
		}
		catch(IllegalArgumentException e)
		{
			System.err.println("Incorrect arguments: " + e.getMessage());
			System.err.println("ListPacketsForBulletin [--no-prompt --packet-directory=<pathToPacketsDirectory> --keypair=<pathToKeyPair>] --public-key=<accountPublicKey> --header-packet=<universalId>");
			System.exit(3);
		}
		
		try
		{
			if(prompt)
			{
				System.out.print("Enter server passphrase:");
				System.out.flush();
			}
			BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));
//			TODO security issue password is a string
			String passphrase = reader.readLine();
			security = (MartusSecurity) MartusServerUtilities.loadCurrentMartusSecurity(serverKeyPairFile, passphrase.toCharArray());			
			
			ServerFileDatabase db = new ServerFileDatabase(packetDir,security);
			db.initialize();
			
			UniversalId uId = UniversalId.createFromAccountAndLocalId(accountPublicKey, bulletinLocalId);
			DatabaseKey dbKey = DatabaseKey.createSealedKey(uId);
			System.out.println("Assuming SEALED status");
			
			BulletinHeaderPacket bhp = new BulletinHeaderPacket(uId);
			try
			{
				bhp = BulletinStore.loadBulletinHeaderPacket(db, dbKey, security);
			}
			catch(Exception e)
			{
				if(e.getClass() == NullPointerException.class)
				{
					System.err.println("ListPacketsForBulletin.main: Bulletin not Found");
					System.err.println(uId);
					System.exit(3);
				}
			}

			DatabaseKey[] keys = BulletinZipUtilities.getAllPacketKeys(bhp);
			
			for (int i = 0; i < keys.length; i++)
			{			
				System.out.println(keys[i].getLocalId());
			}
		}
		catch (Exception e)
		{
			System.err.println("ListPacketsForBulletin.main: " + e);
			e.printStackTrace();
			System.exit(3);
		}
		System.exit(0);
	}
}
