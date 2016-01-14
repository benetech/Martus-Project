/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.server.main;

import java.io.File;
import java.util.Vector;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.LoggerInterface;
import org.martus.common.LoggerToNull;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.util.TestCaseEnhanced;


public class TestServerBulletinStore extends TestCaseEnhanced
{
	public TestServerBulletinStore(String name)
	{
		super(name);
	}

	public void testGetFieldOfficeAccountIds() throws Exception
	{
		LoggerInterface logger = new LoggerToNull();
		
		ServerBulletinStore store = new ServerBulletinStore();
		store.setDatabase(new MockServerDatabase());
		store.setSignatureGenerator(MockMartusSecurity.createServer());
		try
		{
			Vector none = store.getFieldOfficeAccountIdsWithResultCode("Not even a real account id", logger);
			assertEquals(1, none.size());
			assertEquals(NetworkInterfaceConstants.OK, none.get(0));
			
			MartusCrypto fieldOfficeSecurity1 = MockMartusSecurity.createClient();
			MartusCrypto hqSecurity = MockMartusSecurity.createHQ();
			
			MockClientDatabase foDatabase1 = new MockClientDatabase();
			BulletinStore foStore1 = new BulletinStore();
			foStore1.setSignatureGenerator(fieldOfficeSecurity1);
			foStore1.setDatabase(foDatabase1);
			try
			{
				Bulletin b1 = new Bulletin(fieldOfficeSecurity1);
				b1.setAuthorizedToReadKeys(new HeadquartersKeys(new HeadquartersKey(hqSecurity.getPublicKeyString())));
				foStore1.saveBulletinForTesting(b1);
				
				DatabaseKey key1 = b1.getDatabaseKey();
				File zip1 = createTempFile();
				BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(foDatabase1, key1, zip1, fieldOfficeSecurity1);
				
				store.saveZipFileToDatabase(zip1, fieldOfficeSecurity1.getPublicKeyString());
				Vector one = store.getFieldOfficeAccountIdsWithResultCode(hqSecurity.getPublicKeyString(), logger);
				assertEquals(2, one.size());
				assertEquals(NetworkInterfaceConstants.OK, none.get(0));
				assertEquals("didn't have our fo?", foStore1.getAccountId(), one.get(1));
				
				zip1.delete();
			}
			finally
			{
				foStore1.deleteAllData();
			}
		}
		finally
		{
			store.deleteAllData();
		}
	}
}
