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

package org.martus.server.main;

import java.io.File;
import java.util.Vector;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.BulletinUploadRecord;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MSPAFileDatabase;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.database.ReadableDatabase.AccountVisitor;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.TestCaseEnhanced;

public class TestServerFileDatabase extends TestCaseEnhanced 
{
	public TestServerFileDatabase(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		security = MockMartusSecurity.createServer();

		mockDb = new MockServerDatabase();

		File goodDir2 = createTempFile();
		goodDir2.delete();
		goodDir2.mkdir();
		serverFileDb = new ServerFileDatabase(goodDir2, security);
		serverFileDb.initialize();
	}
	
	public void testDetectAccountChangesByOtherApp() throws Exception
	{
		class AccountCounter implements AccountVisitor
		{
			public AccountCounter()
			{
				accounts = new Vector();
			}
			
			public void visit(String accountString)
			{
				accounts.add(accountString);
			}
			
			public int getAccountCount()
			{
				return accounts.size();
			}
			
			Vector accounts;
		}
		
		MSPAFileDatabase secondDatabase = new MSPAFileDatabase(serverFileDb.absoluteBaseDir, security);
		secondDatabase.initialize();

		AccountCounter initial = new AccountCounter();
		secondDatabase.visitAllAccounts(initial);
		assertEquals(0, initial.getAccountCount());
		
		DatabaseKey key = DatabaseKey.createDraftKey(UniversalIdForTesting.createDummyUniversalId());
		serverFileDb.writeRecord(key, smallString);

		AccountCounter afterWrites = new AccountCounter();
		secondDatabase.visitAllAccounts(afterWrites);
		assertEquals(1, afterWrites.getAccountCount());
	}

	public void testBURServer() throws Exception
	{
		TRACE_BEGIN("testBURHandling");

		File tmpPacketDir = createTempFileFromName("$$$MartusTestMartusServer");
		tmpPacketDir.delete();
		tmpPacketDir.mkdir();
		ServerFileDatabase db = new ServerFileDatabase(tmpPacketDir, security);
		db.initialize();

		Bulletin b = new Bulletin(security);
		b.setSealed();
		DatabaseKey key = DatabaseKey.createSealedKey(b.getUniversalId());
		key.setSealed();

		String bur = BulletinUploadRecord.createBulletinUploadRecord(b.getLocalId(), security);
		BulletinUploadRecord.writeSpecificBurToDatabase(db, b.getBulletinHeaderPacket(), bur);
		
		class PacketVisitor implements Database.PacketVisitor
		{
			PacketVisitor(Database databaseToUse)
			{
			}
			
			public void visit(DatabaseKey dbKey)
			{
				String localId = dbKey.getLocalId();
				assertFalse("should not be a BUR packet", localId.startsWith("BUR-"));
			}
		}

		PacketVisitor visitor = new PacketVisitor(db);
		db.visitAllRecords(visitor);
		
		db.deleteAllData();
		tmpPacketDir.delete();
		TRACE_END();
	}
	
	public void testBasics() throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey key = DatabaseKey.createSealedKey(uid);
		File dir = createTempFileFromName("$$$MartusTestServerFileDatabase");
		dir.delete();
		dir.mkdir();
		ServerFileDatabase db = new ServerFileDatabase(dir, security);
		db.initialize();
				
		key.setSealed();
		File sealedFile = db.getFileForRecord(key);
		File sealedBucket = sealedFile.getParentFile();
		String sealedBucketName = sealedBucket.getName();
		assertStartsWith("Wrong sealed bucket name", "pb", sealedBucketName);
		
		key.setDraft();
		File draftFile = db.getFileForRecord(key);
		File draftBucket = draftFile.getParentFile();
		String draftBucketName = draftBucket.getName();
		assertStartsWith("Wrong draft bucket name", "dpb", draftBucketName);
		
		db.deleteAllData();
		dir.delete();
	}
	
	public void testDraftsServer() throws Exception
	{
		internalTestDrafts(mockDb);
		internalTestDrafts(serverFileDb);
	}
	
	private void internalTestDrafts(Database db) throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey draftKey = DatabaseKey.createDraftKey(uid);
		DatabaseKey sealedKey = DatabaseKey.createSealedKey(uid);
		
		db.writeRecord(draftKey, smallString);
		db.writeRecord(sealedKey, smallString2);
		
		assertEquals(db.toString()+"draft wrong?", smallString, db.readRecord(draftKey, security));
		assertEquals(db.toString()+"sealed wrong?", smallString2, db.readRecord(sealedKey, security));
		
		class Counter implements Database.PacketVisitor
		{
			Counter(Database databaseToUse, Vector expected)
			{
				dbase = databaseToUse;
				expectedKeys = expected;
			}
			
			public void visit(DatabaseKey key)
			{
				assertContains(dbase.toString()+"wrong key?", key, expectedKeys);
				expectedKeys.remove(key);
			}
			
			Database dbase;
			Vector expectedKeys;
		}
		
		Vector allKeys = new Vector();
		allKeys.add(draftKey);
		allKeys.add(sealedKey);
		Counter counter = new Counter(db, allKeys);
		db.visitAllRecords(counter);
		assertEquals(db.toString()+"Not all keys visited?", 0, counter.expectedKeys.size());
		
		db.deleteAllData();
	}

	String smallString = "How are you doing?";
	String smallString2 = "Just another string 123";

	MockMartusSecurity security;
	MockServerDatabase mockDb;
	ServerFileDatabase serverFileDb;
}
