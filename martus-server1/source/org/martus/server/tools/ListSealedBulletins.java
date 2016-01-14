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

import java.io.File;
import java.util.Vector;

import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.utilities.MartusServerUtilities;

public class ListSealedBulletins
{
	public static void main(String[] args)
	{
		new ListSealedBulletins(args);
	}
	
	public ListSealedBulletins(String[] args)
	{
		try
		{
			processArgs(args);
			security = MartusServerUtilities.loadKeyPair(keyPairFileName, prompt);
			initializeDatabase();
			listAllSealedBulletins();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(3);
		}
		
	}
	
	void listAllSealedBulletins()
	{
		BulletinVisitor visitor = new BulletinVisitor(); 
		db.visitAllRecords(visitor);
	}

	void initializeDatabase()
	{
		File packetDir = new File(packetDirName);
		if(! packetDir.exists())
		{
			System.out.println("Error packets directory not found");
			System.exit(3);
		}
		
		db = new ServerFileDatabase(packetDir, security);
		try
		{
			db.initialize();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Error initializing database: " + e);
			System.exit(3);
		}
	}

	void processArgs(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			String value = args[i].substring(args[i].indexOf("=")+1);
			if(args[i].startsWith("--no-prompt"))
				prompt = false;

			if(args[i].startsWith("--packet-directory="))
				packetDirName = value;

			if(args[i].startsWith("--keypair"))
				keyPairFileName = value;
		}

		if(packetDirName == null || keyPairFileName == null)
		{
			System.err.println("Incorrect arguments: ListSealedBulletins --packet-directory=<packetdir> --keypair-file=<keypair> [--no-prompt]\n");
			System.exit(2);
		}
	}

	class BulletinVisitor implements Database.PacketVisitor
	{
		public void visit(DatabaseKey key)
		{
			if(!BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
				return;
			if(key.isDraft())
				return;
					
			try
			{
				processBulletin(key);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.exit(3);
			}
		}
		
		private void processBulletin(DatabaseKey key)
			throws Exception
		{
			BulletinHeaderPacket bhp = BulletinStore.loadBulletinHeaderPacket(db, key, security);
			displayBulletinDetails(bhp);
		}
		
		private void displayBulletinDetails(BulletinHeaderPacket bhp) throws Exception
		{
			String accountId = bhp.getAccountId();
			String publicCode = MartusCrypto.getFormattedPublicCode(accountId);
			String bhpDir = db.getFolderForAccount(accountId);
			String bulletinLocalId = bhp.getLocalId();
			String dataLocalId = bhp.getFieldDataPacketId();
			String privateLocalId = bhp.getPrivateFieldDataPacketId();
			System.out.println(publicCode + "," + 
					bhpDir + "," + bulletinLocalId + "," + 
					dataLocalId + "," + 
					privateLocalId);
		}
			
		public Vector foundKeys = new Vector();
		Exception thrownException;
	}
		
	boolean prompt = true;
	String packetDirName;
	String keyPairFileName;
	MartusCrypto security;
	ServerFileDatabase db;
}
