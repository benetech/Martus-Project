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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.BulletinUploadRecord;
import org.martus.common.database.ClientFileDatabase;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.FileDatabase;
import org.martus.common.database.MockDatabase;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeWriter;



public class TestDatabase extends TestCaseEnhanced
{
	public TestDatabase(String name) throws Exception
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		mockDb = new MockServerDatabase();
		security = MockMartusSecurity.createClient();

		goodDir1 = createTempFile();
		goodDir1.delete();
		goodDir1.mkdir();
		clientFileDb = new ClientFileDatabase(goodDir1, security);
		clientFileDb.initialize();
		
		goodDir2 = createTempFile();
		goodDir2.delete();
		goodDir2.mkdir();
		serverFileDb = new ServerFileDatabase(goodDir2, security);
		serverFileDb.initialize();
		
		largeBytes = largeString.getBytes("UTF-8");
	}

	public void tearDown() throws Exception
	{
		mockDb.deleteAllData();
		clientFileDb.deleteAllData();
		serverFileDb.deleteAllData();
		assertFalse("cleanup failed1?", goodDir1.exists());
		assertFalse("cleanup failed2?", goodDir2.exists());
		super.tearDown();
	}
	
	public void TRACE(String text)
	{
		//System.out.println(text);
	}

	////////// File
	
	public void testServerFileDbInitializerWhenNoMapSignatureExists() throws Exception
	{
		File dir = createTempFile();
		dir.delete();
		dir.mkdir();
		ServerFileDatabase sfdb = new ServerFileDatabase(dir, security);
		
		File packets = new File(dir,"packets");
		packets.delete();
		packets.mkdirs();
		
		File contents = new File(packets,"packetContents");
		UnicodeWriter writer = new UnicodeWriter(contents);
		writer.writeln("anacct=string");
		writer.close();
		
		
		File accountMap = new File(dir, "acctmap.txt");
		writer = new UnicodeWriter(accountMap);
		writer.writeln("anacct=string");
		writer.close();
		
		try
		{
			sfdb.initialize();
			fail("Server should have thrown because signature is missing");
		}
		catch(MissingAccountMapSignatureException ignoreExpectedException)
		{
		}
		finally
		{
			if(accountMap.exists())
				accountMap.delete();
			if(contents.exists())
				contents.delete();
			if(packets.exists())
				packets.delete();
			if(dir.exists())
				dir.delete();
		}
	}

	public void testServerFileDbInitializerWhenMapSignatureCorrupted() throws Exception
	{
		File dir = createTempFile();
		dir.delete();
		dir.mkdir();
		ServerFileDatabase sfdb = new ServerFileDatabase(dir, security);
		
		File packets = new File(dir,"packets");
		packets.delete();
		packets.mkdirs();
		
		File contents = new File(packets,"packetContents");
		UnicodeWriter writer = new UnicodeWriter(contents);
		writer.writeln("anacct=string");
		writer.close();
		
		
		File accountMap = new File(dir, "acctmap.txt");
		writer = new UnicodeWriter(accountMap);
		writer.writeln("anacct=string");
		writer.close();
		
		MockMartusSecurity otherSecurity = MockMartusSecurity.createOtherClient();
		MartusServerUtilities.createSignatureFileFromFileOnServer(accountMap, otherSecurity);
		
		try
		{
			sfdb.initialize();
			fail("Server should have thrown because signature is corrupted");
		}
		catch(FileVerificationException ignoreExpectedException)
		{
		}
		finally
		{
			MartusServerUtilities.deleteSignaturesForFile(accountMap);
			
			if(accountMap.exists())
				accountMap.delete();
			if(contents.exists())
				contents.delete();
			if(packets.exists())
				packets.delete();
			if(dir.exists())
				dir.delete();
		}
	}

	public void testClientFileDbInitializerWhenNoMapSignatureExists() throws Exception
	{
		File dir = createTempFile();
		dir.delete();
		dir.mkdir();
		ClientFileDatabase cfdb = new ClientFileDatabase(dir, security);
		
		File packets = new File(dir,"packets");
		packets.delete();
		packets.mkdirs();
		
		File contents = new File(packets,"packetContents");
		UnicodeWriter writer = new UnicodeWriter(contents);
		writer.writeln("anacct=string");
		writer.close();
		
		
		File accountMap = new File(dir, "acctmap.txt");
		writer = new UnicodeWriter(accountMap);
		writer.writeln("anacct=string");
		writer.close();
		
		try
		{
			cfdb.initialize();
			fail("Client should have thrown because of missing signature");
		}
		catch(FileDatabase.MissingAccountMapSignatureException expectedException)
		{
		}
		finally
		{
			if(accountMap.exists())
				accountMap.delete();
			if(contents.exists())
				contents.delete();
			if(packets.exists())
				packets.delete();
			if(dir.exists())
				dir.delete();
		}
	}

	public void testClientFileDbInitializerWhenMapSignatureCorrupted() throws Exception
	{
		File dir = createTempFile();
		dir.delete();
		dir.mkdir();
		ClientFileDatabase cfdb = new ClientFileDatabase(dir, security);
		
		File packets = new File(dir,"packets");
		packets.delete();
		packets.mkdirs();
		
		File contents = new File(packets,"packetContents");
		UnicodeWriter writer = new UnicodeWriter(contents);
		writer.writeln("anacct=string");
		writer.close();
		
		
		File accountMap = new File(dir, "acctmap.txt");
		writer = new UnicodeWriter(accountMap);
		writer.writeln("anacct=string");
		writer.close();
		
		File accountMapSig = new File(dir, "acctmap.txt.sig");
		writer = new UnicodeWriter(accountMapSig);
		writer.writeln("noacct=123456789");
		writer.close();

		try
		{
			cfdb.initialize();
			fail("Client should have thrown because of corrupted signature");
		}
		catch(FileVerificationException expectedException)
		{
		}
		finally
		{
			if(accountMap.exists())
				accountMap.delete();
			if(accountMapSig.exists())
				accountMapSig.delete();
			if(contents.exists())
				contents.delete();
			if(packets.exists())
				packets.delete();
			if(dir.exists())
				dir.delete();
		}
	}

	public void testEmptyDatabase() throws Exception
	{
		TRACE("testSmallWriteRecord");
		internalTestEmptyDatabase(mockDb);
		internalTestEmptyDatabase(clientFileDb);
		internalTestEmptyDatabase(serverFileDb);
	}
	
	public void testSmallWriteRecord() throws Exception
	{
		TRACE("testSmallWriteRecord");
		internalTestSmallWriteRecord(mockDb);
		internalTestSmallWriteRecord(clientFileDb);
		internalTestSmallWriteRecord(serverFileDb);
	}


	public void testGetRecordSize() throws Exception
	{
		TRACE("testGetRecordSize");
		internalTestGetRecordSize(mockDb);
		internalTestGetRecordSize(clientFileDb);
		internalTestGetRecordSize(serverFileDb);
	}

	public void testGetmTime() throws Exception
	{
		TRACE("testGetmTime");
		internalTestGetmTime(mockDb);
		internalTestGetmTime(clientFileDb);
		internalTestGetmTime(serverFileDb);
	}

	public void testLargeWriteRecord() throws Exception
	{
		TRACE("testLargeWriteRecord");
		internalTestLargeWriteRecord(mockDb);
		internalTestLargeWriteRecord(clientFileDb);
		internalTestLargeWriteRecord(serverFileDb);
	}

	public void testLargeRecordInputStream() throws Exception
	{
		TRACE("testLargeRecordInputStream");
		internalTestLargeRecordInputStream(mockDb);
		internalTestLargeRecordInputStream(clientFileDb);
		internalTestLargeRecordInputStream(serverFileDb);
	}

	public void testReplaceWriteRecord() throws Exception
	{
		TRACE("testReplaceWriteRecord");
		internalTestReplaceWriteRecord(mockDb);
		internalTestReplaceWriteRecord(clientFileDb);
		internalTestReplaceWriteRecord(serverFileDb);
	}
	
	public void testScrubRecord() throws Exception
	{
		TRACE("testScrubRecord");
		internalTestScrubRecord(mockDb);
		internalTestScrubRecord(clientFileDb);
		internalTestScrubRecord(serverFileDb);
	}	

	public void testDiscard() throws Exception
	{
		TRACE("testDiscard");
		internalTestDiscard(mockDb);
		internalTestDiscard(clientFileDb);
		internalTestDiscard(serverFileDb);
	}

	public void testDoesRecordExist() throws Exception
	{
		TRACE("testDoesRecordExist");
		internalTestDoesRecordExist(mockDb);
		internalTestDoesRecordExist(clientFileDb);
		internalTestDoesRecordExist(serverFileDb);
	}

	public void testVisitAllRecords() throws Exception
	{
		TRACE("testVisitAllRecords");

		internalTestVisitAllRecords(mockDb);
		internalTestVisitAllRecords(clientFileDb);
		internalTestVisitAllRecords(serverFileDb);
	}

	public void testVisitAllAccounts() throws Exception
	{
		TRACE("testVisitAllAccounts");

		internalTestVisitAllAccounts(mockDb);
		internalTestVisitAllAccounts(clientFileDb);
		internalTestVisitAllAccounts(serverFileDb);
	}

	public void testVisitAllRecordsForAccount() throws Exception
	{
		TRACE("testVisitAllRecordsForAccount");

		internalTestVisitAllRecordsForAccount(mockDb);
		internalTestVisitAllRecordsForAccount(clientFileDb);
		internalTestVisitAllRecordsForAccount(serverFileDb);
	}

	public void testVisitAllRecordsWithNull() throws Exception
	{
		TRACE("testVisitAllRecordsWithNull");
		internalTestVisitAllRecordsWithNull(mockDb);
		internalTestVisitAllRecordsWithNull(clientFileDb);
		internalTestVisitAllRecordsWithNull(serverFileDb);
	}
	
	public void testVisitAllAccountsWithNull() throws Exception
	{
		TRACE("testVisitAllAccountsWithNull");
		internalTestVisitAllAccountsWithNull(mockDb);
		internalTestVisitAllAccountsWithNull(clientFileDb);
		internalTestVisitAllAccountsWithNull(serverFileDb);
	}
	
	public void testVisitAllRecordsForAccountWithNull() throws Exception
	{
		TRACE("testVisitAllRecordsForAccountWithNull");
		internalTestVisitAllRecordsForAccountWithNull(mockDb);
		internalTestVisitAllRecordsForAccountWithNull(clientFileDb);
		internalTestVisitAllRecordsForAccountWithNull(serverFileDb);
	}
	
	public void testDeleteAllData() throws Exception
	{
		TRACE("testDeleteAllData");
		internalTestDeleteAllData(mockDb);
		internalTestDeleteAllData(clientFileDb);
		internalTestDeleteAllData(serverFileDb);
	}

	public void testSmallWriteRecordFromStream() throws Exception
	{
		TRACE("testSmallWriteRecordFromStream");
		internalTestSmallWriteRecordFromStream(mockDb);
		internalTestSmallWriteRecordFromStream(clientFileDb);
		internalTestSmallWriteRecordFromStream(serverFileDb);
	}

	public void testLargeWriteRecordFromStream() throws Exception
	{
		TRACE("testLargeWriteRecordFromStream");
		internalTestLargeWriteRecordFromStream(mockDb);
		internalTestLargeWriteRecordFromStream(clientFileDb);
		internalTestLargeWriteRecordFromStream(serverFileDb);
	}

	public void testBadStream() throws Exception
	{
		TRACE("testBadKey");
		internalTestBadStream(mockDb);
		internalTestBadStream(clientFileDb);
		internalTestBadStream(serverFileDb);
	}

	public void testInternalTestGetIncomingInterimFile() throws Exception
	{
		TRACE("testInternalTestGetIncomingInterimFile");
		internalTestGetIncomingInterimFile(mockDb);
		internalTestGetIncomingInterimFile(clientFileDb);
		internalTestGetIncomingInterimFile(serverFileDb);
	}
	
	public void testGetOutgoingInterimFile() throws Exception
	{
		TRACE("testBuildInterimFileFromBulletinPackets");
		internalTestGetOutgoingInterimFile(mockDb);
		internalTestGetOutgoingInterimPublicOnlyFile(mockDb);
		internalTestGetOutgoingInterimFile(clientFileDb);
		internalTestGetOutgoingInterimFile(serverFileDb);
	}

	public void testQuarantine() throws Exception
	{
		TRACE("testQuarantine");
		internalTestQuarantine(mockDb);
		internalTestQuarantine(clientFileDb);
		internalTestQuarantine(serverFileDb);
	}
	
	public void testFindDraft() throws Exception
	{
		TRACE("testFindDrafts");
		internalTestFindDraft(mockDb);
		internalTestFindDraft(clientFileDb);
		internalTestFindDraft(serverFileDb);
	}
	
	public void testFindSealed() throws Exception
	{
		TRACE("testFindSealed");
		internalTestFindSealed(mockDb);
		internalTestFindSealed(clientFileDb);
		internalTestFindSealed(serverFileDb);
	}
	
	public void testImportFiles() throws Exception
	{
		TRACE("testWriteRecords");
		internalTestImportFiles(mockDb);
		internalTestImportFiles(clientFileDb);
		internalTestImportFiles(serverFileDb);
	}
	
	/////////////////////////////////////////////////////////////////////

	private void internalTestEmptyDatabase(Database db) throws Exception
	{
		assertNull("found non-existant String record?", db.readRecord(smallKey, security));
		assertNull("found non-existant Stream record?", db.openInputStream(smallKey, security));
	}
	
	private void internalTestSmallWriteRecord(Database db) throws Exception
	{
		try
		{
			db.writeRecord(null, smallString);
			fail(db.toString()+"should have thrown for null key");
		}
		catch(IOException ignoreExpectedException)
		{
		}

		try
		{
			db.writeRecord(smallKey, (String)null);
			fail(db.toString()+"should have thrown for null string");
		}
		catch(NullPointerException nullPointerExpectedException)
		{
		}
		catch(IOException nullParameterExpectedException)
		{
		}

		db.writeRecord(smallKey, smallString);
		String gotBack = db.readRecord(smallKey, security);
		assertNotNull(db.toString()+"read failed", gotBack);
		assertEquals(db.toString()+"wrong data?", smallString, gotBack);
	}

	private void internalTestGetRecordSize(Database db) throws Exception
	{
		DatabaseKey shortKey = DatabaseKey.createSealedKey(UniversalIdForTesting.createFromAccountAndPrefix("myAccount" , "x"));
		DatabaseKey shortKey2 = DatabaseKey.createSealedKey(UniversalIdForTesting.createFromAccountAndPrefix("myAccount2" , "cvx"));
		DatabaseKey hiddenKey = DatabaseKey.createSealedKey(UniversalIdForTesting.createFromAccountAndPrefix("myAccount2" , "cvx"));
		String testString = "This is a test";			
		db.writeRecord(shortKey, testString);
		db.writeRecord(hiddenKey, testString);
		db.hide(hiddenKey.getUniversalId());
		
		assertEquals(db.toString()+" Mock Record size not correct?", testString.length(), db.getRecordSize(shortKey));
		assertEquals(db.toString()+" Size not zero?", 0, db.getRecordSize(shortKey2));
		try
		{
			db.getRecordSize(hiddenKey);
			fail("Should have thrown if hidden");
		}
		catch(RecordHiddenException expected)
		{
		}
	}
	
	private void internalTestGetmTime(Database db) throws Exception
	{
		DatabaseKey sealedKey = DatabaseKey.createSealedKey(UniversalIdForTesting.createFromAccountAndPrefix("myAccount" , "x"));
		DatabaseKey burSealedKey = BulletinUploadRecord.getBurKey(sealedKey);
		DatabaseKey unsavedKey = DatabaseKey.createSealedKey(UniversalIdForTesting.createFromAccountAndPrefix("myAccount2" , "cvx"));
		DatabaseKey hiddenKey = DatabaseKey.createSealedKey(UniversalIdForTesting.createFromAccountAndPrefix("myAccount2" , "cvx"));
		String testString = "This is a test";	
		db.writeRecord(sealedKey, testString);
		String bur = BulletinUploadRecord.createBulletinUploadRecord(sealedKey.getLocalId(), security);
		db.writeRecord(burSealedKey, bur);
		db.writeRecord(hiddenKey, testString);
		db.hide(hiddenKey.getUniversalId());
		long mTimeOfFile = db.getmTime(sealedKey);
		long mTimeOfBurPacket = BulletinUploadRecord.getTimeStamp(bur);
		if (db instanceof MockDatabase)
			mTimeOfBurPacket = db.getmTime(burSealedKey);
		
		assertEquals("mTimes not identical to bur time?", mTimeOfBurPacket, mTimeOfFile);
		try
		{
			db.getmTime(unsavedKey);
			fail("should have thrown an error for an mTime not found");
		}
		catch(IOException expected)
		{
		}

		try
		{
			db.getmTime(hiddenKey);
			fail("Should have thrown if hidden");
		}
		catch(RecordHiddenException expected)
		{
		}
	}

	private void internalTestLargeWriteRecord(Database db) throws Exception
	{
		db.writeRecord(largeKey, largeString);
		String gotBackLarge1 = db.readRecord(largeKey, security);
		assertNotNull(db.toString()+"read large1", gotBackLarge1);
		assertEquals(db.toString()+"large string1", largeString, gotBackLarge1);
	}

	private void internalTestLargeRecordInputStream(Database db) throws Exception
	{
		db.writeRecord(largeKey, largeString);
		InputStream in = db.openInputStream(largeKey, security);
		assertNotNull(db.toString()+"no input stream?", in);
		assertEquals(db.toString()+"wrong length?", largeBytes.length, in.available());
		byte[] got = new byte[largeBytes.length];
		in.read(got);
		in.close();
		assertEquals(db.toString()+"bad data", true, Arrays.equals(largeBytes, got));
	}
	
	private void internalTestReplaceWriteRecord(Database db) throws Exception
	{
		db.writeRecord(largeKey, largeString);
		db.writeRecord(smallKey, smallString);
		String gotBack = db.readRecord(smallKey, security);
		assertNotNull(db.toString()+"read2 failed", gotBack);
		assertEquals(db.toString()+"wrong data2?", smallString, gotBack);
	}

	private void internalTestDiscard(Database db) throws Exception
	{
		db.writeRecord(smallKey, smallString);
		db.writeRecord(largeKey, largeString);

		db.discardRecord(smallKey);
		assertNull(db.toString()+"discard failed", db.readRecord(smallKey, security));

		String gotBackLarge3 = db.readRecord(largeKey, security);
		assertNotNull(db.toString()+"read large3", gotBackLarge3);
		assertEquals(db.toString()+"large string3", largeString, gotBackLarge3);
	}

	private void internalTestDoesRecordExist(Database db) throws Exception
	{
		assertEquals(db.toString()+"database not empty", false, db.doesRecordExist(smallKey));
		db.writeRecord(smallKey, smallString);
		assertEquals(db.toString()+"record doesn't exist", true, db.doesRecordExist(smallKey));
		db.discardRecord(smallKey);
		assertEquals(db.toString()+"record didn't discard", false, db.doesRecordExist(smallKey));
	}

	class PacketCounter implements Database.PacketVisitor
	{
		PacketCounter(Database dbToUse)
		{
			db = dbToUse;
		}
		
		public void visit(DatabaseKey key)
		{
			++count;
			assertTrue(db.toString()+"bad key " + key.getLocalId() + "?", db.doesRecordExist(key));
		}
		
		public void clear()
		{
			count = 0;
		}
		
		Database db;
		int count = 0;
	}
	
	private void internalTestVisitAllRecords(Database db) throws Exception
	{
		PacketCounter counter = new PacketCounter(db);

		counter.clear();
		db.visitAllRecords(counter);
		assertEquals(db.toString()+"not empty?", 0, counter.count);
		
		db.writeRecord(smallKey, smallString);
		db.writeRecord(largeKey, largeString);
		counter.clear();
		db.visitAllRecords(counter);
		assertEquals(db.toString()+"count wrong?", 2, counter.count);

		File interimFile = db.getOutgoingInterimFile(smallKey.getUniversalId());
		interimFile.deleteOnExit();
		UnicodeWriter writer1 = new UnicodeWriter(interimFile);
		writer1.write("just some stuff");
		writer1.close();
		counter.clear();
		db.visitAllRecords(counter);
		assertEquals(db.toString()+ " counted interim file?", 2, counter.count);
		interimFile.delete();
		
		//FIXME: This test below doesn't test anything. not doing anything but clearing the counter and asserting will pass this test without doing anything with configinfo.
		File contactFile = db.getContactInfoFile(security.getPublicKeyString());
		contactFile.deleteOnExit();
		contactFile.getParentFile().deleteOnExit();
		contactFile.getParentFile().mkdirs();
		UnicodeWriter writer2 = new UnicodeWriter(contactFile);
		writer2.write("fake contact info");
		writer2.close();
		counter.clear();
		db.visitAllRecords(counter);
		contactFile.delete();
		assertEquals(db.toString()+ " counted contact info file?", 2, counter.count);
	}

	private void internalTestVisitAllAccounts(Database db) throws Exception
	{
		class AccountCounter implements Database.AccountVisitor
		{
				public void visit(String accountId)
				{
					++count;
				}
				
				public void clear()
				{
					count = 0;
				}
				
				int count;
		}
	
		AccountCounter counter = new AccountCounter();

		String account1 = "account1";
		String account2 = "account2";
		DatabaseKey key1 = DatabaseKey.createSealedKey(UniversalIdForTesting.createFromAccountAndPrefix(account1, "x"));
		DatabaseKey key2 = DatabaseKey.createSealedKey(UniversalIdForTesting.createFromAccountAndPrefix(account2, "x"));
		DatabaseKey key3 = DatabaseKey.createSealedKey(UniversalIdForTesting.createFromAccountAndPrefix(account1, "x"));

		counter.clear();
		db.visitAllAccounts(counter);
		assertEquals(db.toString(), 0, counter.count);
		
		counter.clear();
		db.writeRecord(key1, smallString);
		db.visitAllAccounts(counter);
		assertEquals(db.toString(), 1, counter.count);

		counter.clear();
		db.writeRecord(key2, smallString);
		db.visitAllAccounts(counter);
		assertEquals(db.toString(), 2, counter.count);

		counter.clear();
		db.writeRecord(key3, smallString);
		db.visitAllAccounts(counter);
		assertEquals(db.toString() + "dupe accounts?", 2, counter.count);
	}
	
	private void internalTestVisitAllRecordsForAccount(Database db) throws Exception
	{
		PacketCounter counter = new PacketCounter(db);

		UniversalId smallUid2 = UniversalIdForTesting.createFromAccountAndPrefix(smallKey.getAccountId(), "x");
		DatabaseKey smallKey2 = DatabaseKey.createSealedKey(smallUid2);
		db.writeRecord(smallKey, smallString);
		db.writeRecord(smallKey2, smallString);
		db.writeRecord(largeKey, largeString);

		counter.clear();
		db.visitAllRecordsForAccount(counter, "none found");
		assertEquals(db.toString()+" found for account with none?", 0, counter.count);

		counter.clear();
		db.visitAllRecordsForAccount(counter, smallKey.getAccountId());
		assertEquals(db.toString()+" wrong for first account?", 2, counter.count);

		counter.clear();
		db.visitAllRecordsForAccount(counter, largeKey.getAccountId());
		assertEquals(db.toString()+" wrong for second account?", 1, counter.count);
	}

	private void internalTestVisitAllRecordsWithNull(Database db) throws Exception
	{
		class PacketNullThrower implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				key = null;
				key.getAccountId();
				list.add(key);
			}
			Vector list = new Vector();
		}
		
		db.writeRecord(smallKey, smallString);
		
		PacketNullThrower ac = new PacketNullThrower();
		db.visitAllRecords(ac);
		assertEquals("count?", 0, ac.list.size());
	}
	
	private void internalTestVisitAllAccountsWithNull(Database db) throws Exception
	{
		class AccountNullThrower implements Database.AccountVisitor
		{
			public void visit(String accountString)
			{
				accountString = null;
				accountString.toString();
				list.add(accountString);
			}
			Vector list = new Vector();
		}
		
		db.writeRecord(smallKey, smallString);
		
		AccountNullThrower ac = new AccountNullThrower();
		db.visitAllAccounts(ac);
		assertEquals("count?", 0, ac.list.size());
	}
	
	private void internalTestVisitAllRecordsForAccountWithNull(Database db) throws Exception
	{
		class PacketNullThrower implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				key = null;
				key.getAccountId();
				list.add(key);
			}
			Vector list = new Vector();
		}
		
		db.writeRecord(smallKey, smallString);
		
		PacketNullThrower ac = new PacketNullThrower();
		db.visitAllRecordsForAccount(ac, smallKey.getAccountId());
		assertEquals("count?", 0, ac.list.size());
	}
	
	private void internalTestDeleteAllData(Database db) throws Exception
	{
		db.writeRecord(smallKey, smallString);
		assertNotNull(db.toString()+"didn't write", db.readRecord(smallKey, security));
		db.deleteAllData();
		assertNull(db.toString()+"didn't delete all", db.readRecord(smallKey, security));
	}

	private void internalTestSmallWriteRecordFromStream(Database db) throws Exception
	{
		byte[] bytes = smallString.getBytes();
		InputStream stream = new ByteArrayInputStream(bytes);
		try 
		{
			db.writeRecord(null, stream);
			fail(db.toString()+"should have thrown for null key");
		} 
		catch(IOException ignoreExpectedException) 
		{
		}

		try 
		{
			db.writeRecord(smallKey, (InputStream)null);
			fail(db.toString()+"should have thrown for null input stream");
		} 
		catch(IOException ignoreExpectedException) 
		{
		}

		db.writeRecord(smallKey, stream);
		String gotBack = db.readRecord(smallKey, security);
		assertNotNull(db.toString()+"read failed", gotBack);
		assertEquals(db.toString()+"wrong data?", smallString, gotBack);
	}

	private void internalTestLargeWriteRecordFromStream(Database db) throws Exception
	{
		InputStream stream = new ByteArrayInputStream(largeBytes);
		db.writeRecord(largeKey, stream);
		String gotBackLarge = db.readRecord(largeKey, security);
		assertNotNull(db.toString()+"read failed", gotBackLarge);
		assertEquals(db.toString()+"wrong data?", largeString, gotBackLarge);
	}

	private void internalTestBadStream(Database db) throws Exception
	{
		class BadInputStream extends InputStream
		{
			public int available() throws IOException
				{ throw(new IOException("Fake error")); }
			public void close() throws IOException
				{ throw(new IOException("Fake error")); }
			public void mark(int limit)
				{  }
			public boolean markSupported()
				{ return false; }
			public int read() throws IOException
				{ throw(new IOException("Fake error")); }
			public int read(byte[] b) throws IOException
				{ throw(new IOException("Fake error")); }
			public int read(byte[] b, int offset, int len) throws IOException
				{ throw(new IOException("Fake error")); }
			public long skip(long n) throws IOException
				{ throw(new IOException("Fake error")); }
		}
		InputStream badStream = new BadInputStream();
		try
		{
			db.writeRecord(smallKey, badStream);
			fail(db.toString()+"should have thrown");
		}
		catch(IOException ignoreExpectedException)
		{
		}
		//TODO deside whether to try to recover an old record after a failed write.
		//assertEquals("kept partial", null, db.readRecord(smallKey));
		//db.writeRecord(smallKey, smallString);
		//assertEquals("write bad stream2", false, db.writeRecord(smallKey, badStream));
		//assertEquals("discarded old", smallString, db.readRecord(smallKey));
	}

	private void internalTestGetIncomingInterimFile(Database db) throws Exception
	{
		File interim = db.getIncomingInterimFile(smallKey.getUniversalId());
		assertNotNull(db.toString()+"file is null?", interim);
		assertEquals(db.toString()+"interim file exists?", false, interim.exists());
		UnicodeWriter writer = new UnicodeWriter(interim);
		writer.write("hello");
		writer.close();
		long fileSize = interim.length();
		assertNotEquals(db.toString()+"Zero length?", 0, fileSize);
		
		File interimSame = db.getIncomingInterimFile(smallKey.getUniversalId());
		assertEquals(db.toString()+"Not the same file?", interim, interimSame);
		assertEquals(db.toString()+"interimSame size not the same?", fileSize, interimSame.length());

		interim.delete();
		interimSame.delete();
	}
	
	private void internalTestGetOutgoingInterimFile(Database db) throws Exception
	{
		File interim = db.getOutgoingInterimFile(smallKey.getUniversalId());
		assertNotNull(db.toString()+"file is null?", interim);
		assertEquals(db.toString()+"interim file exists?", false, interim.exists());
		UnicodeWriter writer = new UnicodeWriter(interim);
		writer.write("hello");
		writer.close();
		long fileSize = interim.length();
		assertNotEquals(db.toString()+"Zero length?", 0, fileSize);
		
		File interimSame = db.getOutgoingInterimFile(smallKey.getUniversalId());
		assertEquals(db.toString()+"Not the same file?", interim, interimSame);
		assertEquals(db.toString()+"interimSame size not the same?", fileSize, interimSame.length());

		interim.delete();
		interimSame.delete();
	}	
	
	private void internalTestGetOutgoingInterimPublicOnlyFile(Database db) throws Exception
	{
		File interim = db.getOutgoingInterimPublicOnlyFile(smallKey.getUniversalId());
		assertNotNull(db.toString()+"file is null?", interim);
		assertEquals(db.toString()+"interim file exists?", false, interim.exists());
		UnicodeWriter writer = new UnicodeWriter(interim);
		writer.write("hello");
		writer.close();
		long fileSize = interim.length();
		assertNotEquals(db.toString()+"Zero length?", 0, fileSize);
		
		File interimSame = db.getOutgoingInterimPublicOnlyFile(smallKey.getUniversalId());
		assertEquals(db.toString()+"Not the same file?", interim, interimSame);
		assertEquals(db.toString()+"interimSame size not the same?", fileSize, interimSame.length());

		interim.delete();
		interimSame.delete();
	}	

	private void internalTestQuarantine(Database db) throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey draftKey = DatabaseKey.createDraftKey(uid);

		assertFalse(db.toString()+" draft already in quarantine?", db.isInQuarantine(draftKey));
		db.moveRecordToQuarantine(draftKey);
		assertFalse(db.toString()+" non-existant draft record in quarantine?", db.isInQuarantine(draftKey));
 
		db.writeRecord(draftKey, smallString);
		assertFalse(db.toString()+" writing draft put it in quarantine?", db.isInQuarantine(draftKey));

		db.moveRecordToQuarantine(draftKey);
		assertTrue(db.toString()+" draft not moved to quarantine?", db.isInQuarantine(draftKey));
		assertFalse(db.toString()+" draft not removed from main db?", db.doesRecordExist(draftKey));
		
		DatabaseKey sealedKey = DatabaseKey.createSealedKey(uid);
		assertFalse(db.toString()+" sealed already in quarantine?", db.isInQuarantine(sealedKey));
		db.writeRecord(sealedKey, smallString);
		db.moveRecordToQuarantine(sealedKey);
		assertTrue(db.toString()+" sealed not moved to quarantine?", db.isInQuarantine(sealedKey));
		assertFalse(db.toString()+" sealed not removed from main db?", db.doesRecordExist(sealedKey));
		assertTrue(db.toString()+" draft not still to quarantine?", db.isInQuarantine(draftKey));

		db.writeRecord(sealedKey, smallString);
		db.moveRecordToQuarantine(sealedKey);
		assertTrue(db.toString()+" sealed removed from quarantine?", db.isInQuarantine(sealedKey));
		assertFalse(db.toString()+" sealed not removed from main db again?", db.doesRecordExist(sealedKey));
		
		class Counter implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				++count;
			}
			
			int count;
		}
		
		Counter counter = new Counter();
		db.visitAllRecords(counter);
		assertEquals("Visited quarantined packets?", 0, counter.count);
	}
	
	private void internalTestFindDraft(Database db) throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey draftKey = DatabaseKey.createDraftKey(uid);
		db.writeRecord(draftKey, smallString);
		InputStream in = db.openInputStream(draftKey, security);
		assertNotNull("not found?", in);
		in.close();
	}

	private void internalTestFindSealed(Database db) throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey sealedKey = DatabaseKey.createSealedKey(uid);
		db.writeRecord(sealedKey, smallString);
		InputStream in = db.openInputStream(sealedKey, security);
		assertNotNull("not found?", in);
		in.close();
	}
	
	private void internalTestImportFiles(Database db) throws Exception
	{
		File temp1 = createTempFile();
		UnicodeWriter writer = new UnicodeWriter(temp1);
		writer.write(smallString);
		writer.close();

		File temp2 = createTempFile();
		writer = new UnicodeWriter(temp2);
		writer.write(largeString);
		writer.close();

		UniversalId uid1 = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey sealedKey1 = DatabaseKey.createSealedKey(uid1);
		
		UniversalId uid2 = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey sealedKey2 = DatabaseKey.createSealedKey(uid2);

		HashMap entries = new HashMap();
		entries.put(sealedKey1, temp1);
		entries.put(sealedKey2, temp2);

		db.importFiles(entries);
		
		InputStream in = db.openInputStream(sealedKey1, security);
		assertNotNull(db.toString() + " not found 1?", in);
		in.close();
		in = db.openInputStream(sealedKey2, security);
		assertNotNull(db.toString() + " not found 2?", in);
		in.close();

		assertFalse(temp1.toString() +" file exists?", temp1.exists());
		assertFalse(temp2.toString() +" file exists?", temp2.exists());
		
		assertEquals(db.toString() + " record 1 incorrect?", smallString, db.readRecord(sealedKey1, security));
		assertEquals(db.toString() + " record 2 incorrect?", largeString, db.readRecord(sealedKey2, security));
	}
	
	private void internalTestScrubRecord(Database db) throws Exception
	{
		db.writeRecord(smallKey, smallString);		
		db.scrubRecord(smallKey);		

		String gotBack = db.readRecord(smallKey, security);
		byte[] scrubBytes = gotBack.getBytes();
		assertEquals(db.toString()+"wrong length?", smallString.length(), scrubBytes.length);
		for (int i = 0; i < scrubBytes.length; i++)
		{
			byte b = scrubBytes[i];
			assertEquals(db.toString()+"not scrubbed?", 0x55, b);
		}
		
		assertNotNull(db.toString()+"read failed", gotBack);
		assertNotEquals(db.toString()+"wrong data?", smallString, gotBack);
		
	}

	static String buildLargeString()
	{
		String result = "";
		for(int i = 0; i < 200; ++i)
			result += "The length of this string must not ?????? divide into blocksize!!!";
		return result;
	}

	MockMartusSecurity security;
	DatabaseKey smallKey = DatabaseKey.createSealedKey(UniversalIdForTesting.createFromAccountAndPrefix("small account", "x"));
	DatabaseKey largeKey = DatabaseKey.createSealedKey(UniversalIdForTesting.createFromAccountAndPrefix("large account", "x"));
	String smallString = "How are you doing?";
	String smallString2 = "Just another string 123";
	String largeString = buildLargeString();
	byte[] largeBytes;
	File goodDir1;
	File goodDir2;
	MockDatabase mockDb;
	FileDatabase clientFileDb;
	ServerFileDatabase serverFileDb;
}
