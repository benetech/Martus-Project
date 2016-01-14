/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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

package org.martus.client.test;

import java.io.File;
import java.io.IOException;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.MockClientDatabase;


public class MockBulletinStore extends ClientBulletinStore
{
	public MockBulletinStore() throws Exception
	{
		this(MockMartusSecurity.createClient());
	}

	public MockBulletinStore(MartusCrypto crypto) throws Exception
	{
		this(new MockClientDatabase(), crypto);
	}
	
	public MockBulletinStore(Database db, MartusCrypto crypto) throws Exception
	{
		super(crypto);
		File dir = File.createTempFile("$$$MockBulletinStore", null);
		dir.deleteOnExit();
		dir.delete();
		dir.mkdirs();
		doAfterSigninInitialization(dir, db);
		createFieldSpecCacheFromDatabase();
	}
	
	public void deleteAllData() throws Exception
	{
		super.deleteAllData();
		if(getFoldersFile().exists())
			throw new IOException("Didn't delete folders.dat!");
	}
	
	public void discardBulletin(BulletinFolder folder, Bulletin b) throws IOException
	{
		super.discardBulletin(folder, b.getUniversalId());
	}
	
	public Database getWriteableDatabase()
	{
		return (Database)getDatabase();
	}
}
