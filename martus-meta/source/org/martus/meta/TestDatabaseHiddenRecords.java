/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003-2007, Beneficent
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

package org.martus.meta;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.TestCaseEnhanced;

public class TestDatabaseHiddenRecords extends TestCaseEnhanced
{
	public TestDatabaseHiddenRecords(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		security = MockMartusSecurity.createServer(); 
		tempDirectory = createTempFile();
		tempDirectory.delete();
		tempDirectory.mkdir(); 
		fileDatabase = new ServerFileDatabase(tempDirectory, security);
		fileDatabase.initialize();
		mockDatabase = new MockServerDatabase();

		draftUid = UniversalIdForTesting.createFromAccountAndPrefix("bogus account", "G");
		draftKey = DatabaseKey.createDraftKey(draftUid);

		sealedUid = UniversalIdForTesting.createFromAccountAndPrefix("bogus account", "G");
		sealedKey = DatabaseKey.createSealedKey(sealedUid);
		
		assertNotEquals("duplicate uids?", draftUid, sealedUid);
	}

	protected void tearDown() throws Exception
	{
		fileDatabase.deleteAllData();
		tempDirectory.delete();
		super.tearDown();
	}

	public void testBasics() throws Exception
	{
		verifyBasics(mockDatabase, draftUid);
		verifyBasics(mockDatabase, sealedUid);
		verifyBasics(fileDatabase, draftUid);
		verifyBasics(fileDatabase, sealedUid);
	}

	private void verifyBasics(Database db, UniversalId uid) throws Exception
	{
		assertFalse("already hidden?", db.isHidden(uid));
		db.hide(uid);
		assertTrue("not hidden?", db.isHidden(uid));
		db.hide(uid);
		assertTrue("not hidden after two hides?", db.isHidden(uid));
		db.deleteAllData();
		assertTrue("not hidden after deleteAllData?", db.isHidden(uid));
	}
	
	public void testHiddenFromReading() throws Exception
	{
		verifyHiddenFromReading(mockDatabase, draftKey);
		verifyHiddenFromReading(mockDatabase, sealedKey);
		verifyHiddenFromReading(fileDatabase, draftKey);
		verifyHiddenFromReading(fileDatabase, sealedKey);
	}
	
	void verifyHiddenFromReading(Database db, DatabaseKey key) throws Exception
	{
		writeAndHideRecord(db, key);
		assertNull("opened stream for hidden?", db.openInputStream(key, security));
		assertNull("able to read hidden?", db.readRecord(key, security));
		assertFalse("hidden exists?", db.doesRecordExist(key));
		try
		{
			db.getRecordSize(key);
			fail("Should have thrown for getRecordSize");
		}
		catch(RecordHiddenException ignoreExpectedException)
		{
		}
		try
		{
			db.isInQuarantine(key);
			fail("Should have thrown for isInQuarantine");
		}
		catch(RecordHiddenException ignoreExpectedException)
		{
		}
		try
		{
			db.getOutgoingInterimFile(key.getUniversalId());
			fail("Should have thrown for getOutgoingInterimFile");
		}
		catch(RecordHiddenException ignoreExpectedException)
		{
		}
		db.deleteAllData();
	}

	public void testHiddenFromWriting() throws Exception
	{
		verifyHiddenFromWriting(mockDatabase, draftKey);
		verifyHiddenFromWriting(mockDatabase, sealedKey);
		verifyHiddenFromWriting(fileDatabase, draftKey);
		verifyHiddenFromWriting(fileDatabase, sealedKey);
	}

	public void testHiddenFromOverwriting() throws Exception
	{
		verifyHiddenFromOverwriting(mockDatabase, draftKey);
		verifyHiddenFromOverwriting(mockDatabase, sealedKey);
		verifyHiddenFromOverwriting(fileDatabase, draftKey);
		verifyHiddenFromOverwriting(fileDatabase, sealedKey);
	}
	
	private void verifyHiddenFromOverwriting(Database db, DatabaseKey key)  throws Exception
	{
		db.writeRecord(key, "some sample data");
		verifyHiddenFromWriting(db, key);
	}

	private void verifyHiddenFromWriting(Database db, DatabaseKey key) throws Exception
	{
		db.hide(key.getUniversalId());
		try
		{
			db.writeRecord(key, "sample data");
			fail("Should have thrown for writeRecord!");
		}
		catch(Database.RecordHiddenException ignoreExpectedException)
		{
		}
		try
		{
			db.moveRecordToQuarantine(key);
			fail("Should have thrown for moveRecordToQuarantine!");
		}
		catch(Database.RecordHiddenException ignoreExpectedException)
		{
		}
		try
		{
			db.getIncomingInterimFile(key.getUniversalId());
			fail("Should have thrown for getIncomingInterimFile");
		}
		catch(RecordHiddenException ignoreExpectedException)
		{
		}
		db.discardRecord(key);
		db.discardRecord(key);
		db.deleteAllData();
	}
	
	public void testImportHiddenRecordToServerDatabase() throws Exception
	{
		verifyImportHiddenRecord(mockDatabase);
		verifyImportHiddenRecord(fileDatabase);
	}

	private void verifyImportHiddenRecord(Database db)
		throws Exception
	{
		UniversalId visibleUid = UniversalIdForTesting.createDummyUniversalId();
		db.hide(draftUid);
		db.hide(sealedUid);
		
		DatabaseKey visibleKey = DatabaseKey.createSealedKey(visibleUid);
		DatabaseKey hiddenDraftKey = DatabaseKey.createSealedKey(draftUid);
		DatabaseKey hiddenSealedKey = DatabaseKey.createSealedKey(sealedUid);
		HashMap entries = new HashMap();
		entries.put(visibleKey, null);
		entries.put(hiddenDraftKey, null);
		entries.put(hiddenSealedKey, null);
		try
		{
			db.importFiles(entries);
			fail("Should have thrown!");
		}
		catch(Database.RecordHiddenException ignoreExpectedException)
		{
		}
		
		db.deleteAllData();
	}
	
	public void testHiddenFromVisiting() throws Exception
	{
		verifyHiddenFromVisiting(mockDatabase, draftKey);
		verifyHiddenFromVisiting(mockDatabase, sealedKey);
		verifyHiddenFromVisiting(fileDatabase, draftKey);
		verifyHiddenFromVisiting(fileDatabase, sealedKey);
		
	}
	
	void verifyHiddenFromVisiting(Database db, DatabaseKey key) throws Exception
	{
		String accountId = key.getAccountId();
		UniversalId visibleUid = UniversalId.createFromAccountAndLocalId(accountId, "Y");
		DatabaseKey visibleKey = DatabaseKey.createSealedKey(visibleUid);
		if(key.isDraft())
			visibleKey.setDraft();
		
		class Visitor implements Database.PacketVisitor
		{
			Visitor(DatabaseKey hiddenKeyToUse)
			{
				hiddenKey = hiddenKeyToUse;
			}
			
			public void visit(DatabaseKey thisKey)
			{
				assertNull("Found more than one visible key?", visitedKey);
				assertNotEquals("Visited hidden key?", hiddenKey, thisKey);
				visitedKey = thisKey;
			}
			public DatabaseKey hiddenKey;
			public DatabaseKey visitedKey;
		}
		
		{
			Visitor visitor = new Visitor(key);
			db.visitAllRecords(visitor);
			assertNull("Bulletin already exists?", visitor.visitedKey);
		}
		
		db.writeRecord(visibleKey, "some data");
		{
			Visitor visitor = new Visitor(key);
			db.visitAllRecords(visitor);
			assertEquals("written bulletin not visible?", visibleKey, visitor.visitedKey);
		}
		
		writeAndHideRecord(db, key);
		{
			Visitor visitor = new Visitor(key);
			db.visitAllRecords(visitor);
			assertEquals("visible bulletin got hidden?", visibleKey, visitor.visitedKey);
			visitor.visitedKey = null;
			db.visitAllRecordsForAccount(visitor, accountId);
			assertEquals("visible bulletin not found in account?", visibleKey, visitor.visitedKey);
		}
		
		db.deleteAllData();
	}
	
	
	
	// TODO: need to test BulletinZipUtilities.importBulletinPacketsFromZipFileToDatabase

	private void writeAndHideRecord(Database db, DatabaseKey key)
		throws IOException, RecordHiddenException
	{
		db.writeRecord(key, "test");
		db.hide(key.getUniversalId());
	}
	
	MockMartusSecurity security;
	File tempDirectory;	
	Database fileDatabase;
	Database mockDatabase;
	
	UniversalId draftUid;
	DatabaseKey draftKey;

	UniversalId sealedUid;
	DatabaseKey sealedKey;
}
