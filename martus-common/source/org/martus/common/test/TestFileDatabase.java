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

package org.martus.common.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.BulletinUploadRecord;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.DeleteRequestRecord;
import org.martus.common.database.FileDatabase;
import org.martus.common.packet.UniversalId;
import org.martus.util.*;


public class TestFileDatabase extends TestCaseEnhanced
{
	public TestFileDatabase(String name) throws Exception
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		security = MockMartusSecurity.createClient();
		dir = createTempFileFromName("$$$MartusTestFileDatabaseSetup");
		dir.delete();
		dir.mkdir();
		db = new MyFileDatabase(dir, security);
		db.initialize();
	}

	public void tearDown() throws Exception
	{
		db.deleteAllData();
		assertFalse("Either a test failed or a file was left open.", dir.exists());
		super.tearDown();
	}

	public void testBasics() throws Exception
	{
		assertEquals("count not 0?", 0, getRecordCount());
	}
	
	public void testGetHash() throws Exception
	{
		assertEquals("b2d", FileDatabase.getBaseBucketName("B-ETDfGg5lYthENicCASWfSw--"));
		assertEquals("bdd", FileDatabase.getBaseBucketName("BUR-B-ETDfGg5lYthENicCASWfSw--"));
		assertEquals("b3d", FileDatabase.getBaseBucketName("B-mVji1pfNcvQOAm.9TvvrDg--"));
		assertEquals("b2d", FileDatabase.getBaseBucketName("DEL-B-mVji1pfNcvQOAm.9TvvrDg--"));
	}

/*
 * This test was an attempt to figure out why the sync() call was working in the
 * unit tests, but failing in the app itself (under Win2K, anyway). Unfortunately,
 * the test passed, so it didn't really tell us anything. At some point, when the
 * sync() stuff has been fixed, this should be deleted. kbs. 2002-09-03
	public void testStupidSyncProblem() throws Exception
	{
		File dir = new File("c:/martus/packets/abed/a0000000/pb00");
		dir.deleteOnExit();
		dir.mkdirs();
		File file = new File(dir, "$$$MartusTest.tmp");
		file.deleteOnExit();

		FileOutputStream rawOut = new FileOutputStream(file);
		BufferedOutputStream out = (new BufferedOutputStream(rawOut));
		for(int i=0;i<800;++i)
			out.write(0);
		out.flush();
		rawOut.flush();
		rawOut.getFD().sync();
		out.close();
		file.delete();
	}
*/

	public void testInitializerWhenNoMapExists() throws Exception
	{
		db.getAccountDirectory("some stupid account");
		db.accountMapFile.delete();

		FileDatabase fdb = new MyFileDatabase(dir, security);
		try
		{
			fdb.initialize();
			fail("Should have thrown because map is missing");
		}
		catch(FileDatabase.MissingAccountMapException ignoreExpectedException)
		{
		}
	}
	
	public void testIsAccountMapExpected() throws Exception
	{
		File testDir = createTempFileFromName("$$$MartusTestFileDatabase");
		testDir.delete();
		testDir.mkdir();
		assertFalse("empty dir", FileDatabase.isAccountMapExpected(testDir));
		
		File irrelevantFile = new File(testDir, "test");
		createEmptyFile(irrelevantFile);
		boolean expectedWithIrrelevantFile = FileDatabase.isAccountMapExpected(testDir);
		irrelevantFile.delete();
		assertFalse("irrelevant file", expectedWithIrrelevantFile);

		File nonAccountBucket = new File(testDir, "b12345");
		nonAccountBucket.mkdir();
		assertFalse("nonAccountBucket", FileDatabase.isAccountMapExpected(testDir));
		nonAccountBucket.delete();
		
		File accountBucket = new File(testDir, "ab12345");
		accountBucket.mkdir();
		assertTrue("accountBucket", FileDatabase.isAccountMapExpected(testDir));
		accountBucket.delete();
		
		testDir.delete();
	}

	private void createEmptyFile(File irrelevantFile)
		throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(irrelevantFile);
		out.write(0);
		out.close();
	}

	public void testWriteAndReadStrings() throws Exception
	{
		db.writeRecord(shortKey, sampleString1);
		assertEquals("count not one?", 1, getRecordCount());

		assertEquals("read1", sampleString1, db.readRecord(shortKey, security));

		db.writeRecord(shortKey, sampleString2);
		assertEquals("count not still one?", 1, getRecordCount());

		assertEquals("read2", sampleString2, db.readRecord(shortKey, security));
	}

	public void testWriteAndReadStreams() throws Exception
	{
		ByteArrayInputStream streamToWrite1 = new ByteArrayInputStream(sampleBytes1);
		db.writeRecord(shortKey, streamToWrite1);
		streamToWrite1.close();
		assertEquals("count not one?", 1, getRecordCount());

		InputStream in1 = db.openInputStream(shortKey, security);
		assertNotNull("null stream?", in1);
		byte[] bytes1 = new byte[in1.available()];
		in1.read(bytes1);
		in1.close();
		assertTrue("wrong bytes?", Arrays.equals(sampleBytes1, bytes1));

		ByteArrayInputStream streamToWrite2 = new ByteArrayInputStream(sampleBytes2);
		db.writeRecord(shortKey, streamToWrite2);
		streamToWrite2.close();
		assertEquals("count not still one?", 1, getRecordCount());

		InputStream in2 = db.openInputStream(shortKey, security);
		assertNotNull("null stream?", in2);
		byte[] bytes2 = new byte[in2.available()];
		in2.read(bytes2);
		in2.close();
		assertTrue("wrong bytes?", Arrays.equals(sampleBytes2, bytes2));
	}

	public void testReadEncryptedStream() throws Exception
	{
		db.writeRecordEncrypted(shortKey, sampleString1, security);
		InputStream in1 = db.openInputStream(shortKey, security);
		assertNotNull("null stream?", in1);
		byte[] bytes1 = new byte[in1.available()];
		in1.read(bytes1);
		in1.close();
		String got = new String(bytes1, "UTF-8");
		assertEquals("wrong data?", sampleString1, got);
	}

	public void testDiscardRecord() throws Exception
	{
		assertEquals("count not 0?", 0, getRecordCount());
		assertEquals("already exists?", false, db.doesRecordExist(shortKey));

		db.writeRecord(shortKey, sampleString1);
		assertEquals("count not one?", 1, getRecordCount());
		assertEquals("wasn't created?", true, db.doesRecordExist(shortKey));

		db.discardRecord(shortKey);
		assertEquals("count not back to 0?", 0, getRecordCount());
		assertEquals("wasn't discarded?", false, db.doesRecordExist(shortKey));
	}

	public void testDeleteAllData() throws Exception
	{
		db.writeRecord(shortKey, sampleString1);
		db.deleteAllData();
		assertEquals("count not 0?", 0, getRecordCount());
		assertNull("not zero files?", db.absoluteBaseDir.list());
	}

	public void testInterimFileNames() throws Exception
	{
		File interimIn = db.getIncomingInterimFile(shortKey.getUniversalId());
		assertEndsWith(".in", interimIn.getName());
		File interimOut = db.getOutgoingInterimFile(shortKey.getUniversalId());
		assertEndsWith(".out", interimOut.getName());
	}

	public void testPersistence() throws Exception
	{
		db.writeRecord(shortKey, sampleString1);
		db.writeRecord(shortKey, sampleString1);
		ByteArrayInputStream streamToWrite1 = new ByteArrayInputStream(sampleBytes1);
		db.writeRecord(otherKey, streamToWrite1);
		streamToWrite1.close();
		assertEquals("count not two?", 2, getRecordCount());
		db.discardRecord(otherKey);

		db = new MyFileDatabase(dir, security);
		db.initialize();
		assertEquals("count not back to one?", 1, getRecordCount());

		assertTrue("missing short?", db.doesRecordExist(shortKey));
	}

	public void testWriteAndReadRecordEncrypted() throws Exception
	{
		try
		{
			db.writeRecordEncrypted(null, sampleString1, security);
			fail("should have thrown for null key");
		}
		catch(IOException ignoreExpectedException)
		{
		}

		try
		{
			db.writeRecordEncrypted(shortKey, null, security);
			fail("should have thrown for null string");
		}
		catch(NullPointerException nullPointerExpectedException)
		{
		}

		try
		{
			db.writeRecordEncrypted(shortKey, sampleString1, null);
			fail("should have thrown for null crypto");
		}
		catch(IOException ignoreExpectedException)
		{
		}

		db.writeRecordEncrypted(shortKey, sampleString1, security);
		File file = db.getFileForRecord(shortKey);

		InputStream in1 = new FileInputStream(file);
		assertNotNull("null stream?", in1);
		byte[] bytes1 = new byte[in1.available()];
		in1.read(bytes1);
		in1.close();
		assertEquals("Not Encrypted?", false, Arrays.equals(sampleString1.getBytes(), bytes1));

		String result = db.readRecord(shortKey, security);
		assertEquals("got wrong data?", sampleString1, result);
	}

	public void testHashFunction()
	{
		String s1 = "abcdefg";
		String s2 = "bcdefga";
		int hash1 = FileDatabase.getHashValue(s1);
		int hash2 = FileDatabase.getHashValue(s2);
		int hash3 = FileDatabase.getHashValue(s1);
		assertNotEquals("same?", new Integer(hash1), new Integer(hash2));
		assertEquals("not same?", new Integer(hash1), new Integer(hash3));

		Random rand = new Random(12345);
		StringBuffer buffer = new StringBuffer();
		for(int x = 0; x < 24; ++x)
		{
			char newChar = (char)('A' + rand.nextInt(26));
			buffer.append(newChar);
		}
		int[] count = new int[256];
		for(int i = 0; i < 25600; ++i)
		{
			int changeAt = rand.nextInt(buffer.length());
			char newChar = (char)('A' + rand.nextInt(26));
			buffer.setCharAt(changeAt, newChar);
			int hash = FileDatabase.getHashValue(new String(buffer));
			++count[hash&0xFF];
		}
		for(int bucket = 0; bucket < count.length; ++bucket)
		{
			assertTrue("too many in bucket?", count[bucket] < 250);
		}

	}

	public void testGetFileForRecord() throws Exception
	{
		File file = db.getFileForRecord(shortKey);
		assertEquals("filename", shortKey.getLocalId(), file.getName());
		String path = file.getPath();
		assertStartsWith("no dir?", dir.getPath(), path);

		int hash = FileDatabase.getHashValue(shortKey.getLocalId()) & 0xFF;
		String hashString = Integer.toHexString(hash + 0xb00);
		assertContains("no hash stuff?", "p" + hashString, path);

	}

	public void testGetAccountDirectory() throws Exception
	{
		Vector accountDirs = new Vector();

		String baseDir = dir.getPath().replace('\\', '/');
		for(int i = 0; i < 20; ++i)
		{
			String a1 = "account" + i;
			UniversalId uid1 = UniversalIdForTesting.createFromAccountAndPrefix(a1, "x");
			DatabaseKey key1 = DatabaseKey.createImmutableKey(uid1);
			int accountHash1 = FileDatabase.getHashValue(uid1.getAccountId()) & 0xFF;
			String accountHashString1 = Integer.toHexString(accountHash1 + 0xb00);
			String expectedAccountBucket = baseDir + "/a" + accountHashString1;
			String gotDir1 = db.getAccountDirectory(key1.getAccountId()).getPath().replace('\\', '/');
			assertContains("wrong base?", baseDir, gotDir1);
			assertContains("wrong full path?", expectedAccountBucket, gotDir1);

			String gotDirString = db.getFolderForAccount(key1.getAccountId());
			assertStartsWith("bad folder?", "a" + accountHashString1 + File.separator, gotDirString);

			assertEquals("bad reverse lookup?", key1.getAccountId(), db.getAccountString(new File(gotDir1)));
			assertNotContains("already used this accountdir?", gotDir1, accountDirs);
			accountDirs.add(gotDir1);
		}
	}

	public void testGetFolderForAccountUpdateAccountMapWhenNeeded() throws Exception
	{
		db.deleteAllData();
		File mapFile = db.accountMapFile;

		assertEquals("account file already exists?", 0, mapFile.length());

		String accountId = "some silly account";
		db.getFolderForAccount(accountId);
		assertNotEquals("account file not updated?", 0, mapFile.length());
		long lastLength = mapFile.length();
		long lastModified = mapFile.lastModified();
		Thread.sleep(2000);

		db.getFolderForAccount(accountId);
		assertEquals("account file grew?", lastLength, mapFile.length());
		assertEquals("account file touched?", lastModified, mapFile.lastModified());

		String accountId2 = "another silly account";
		db.getFolderForAccount(accountId2);
		assertNotEquals("account file not updated again?", lastLength, mapFile.length());
		assertNotEquals("account file not touched again?", lastModified, mapFile.lastModified());
	}

	public void testAddParsedAccountEntry()
	{
		HashMap map = new HashMap();
		String dirSeparator = File.separator;
		String relativeDir1 = "bucket1"+ dirSeparator +"a1";
		String absoluteDir2 = "C:" + dirSeparator + "Martus" + dirSeparator;
		String relativeDir2 = "bucket2"+ dirSeparator +"a1";
		String absoluteDir3 = dirSeparator + "home" + dirSeparator;
		String relativeDir3 = "bucket3"+ dirSeparator +"a1";
		String account1 = "account1";
		String account2 = "account2";
		String account3 = "account3";
		db.addParsedAccountEntry(map,relativeDir1+"="+account1);
		assertEquals("relative Failed to be added to map?", relativeDir1, map.get(account1));
		db.addParsedAccountEntry(map,absoluteDir2+relativeDir2+"="+account2);
		assertEquals("absoluteDir Failed to be added to map?", relativeDir2, map.get(account2));
		db.addParsedAccountEntry(map,absoluteDir3+relativeDir3+"="+account3);
		assertEquals("absoluteDir2 Failed to be added to map?", relativeDir3, map.get(account3));
	}

	public void testWriteUpdatesAccountMapWhenNeeded() throws Exception
	{
		db.deleteAllData();
		File mapFile = db.accountMapFile;
		String accountId = "accountForTesting";
		UniversalId uid = UniversalIdForTesting.createFromAccountAndPrefix(accountId, "x");
		DatabaseKey key = DatabaseKey.createImmutableKey(uid);

		db.writeRecord(key, "Some record text");
		long lastLength = mapFile.length();
		long lastModified = mapFile.lastModified();
		Thread.sleep(2000);

		db.writeRecord(key, "Other record text");
		assertEquals("account file grew?", lastLength, mapFile.length());
		assertEquals("account file touched?", lastModified, mapFile.lastModified());
	}

	public void testAccountMapSigning()  throws Exception
	{
		security = MockMartusSecurity.createClient();

		File tmpDataDir = createTempFile();
		if( tmpDataDir.exists() ) tmpDataDir.delete();
		tmpDataDir.mkdir();
		FileDatabase fileDb = new MyFileDatabase(tmpDataDir, security);
		fileDb.initialize();

		String bogusAccountId = "A false account id";
		fileDb.getFolderForAccount(bogusAccountId);

		File acctMapFile = fileDb.accountMapFile;
		assertTrue("missing acctmap?", acctMapFile.exists());

		File mapSigFile = fileDb.accountMapSignatureFile;
		assertTrue("missing acctmap signature?", mapSigFile.exists());

		MartusUtilities.verifyFileAndSignature(acctMapFile, mapSigFile, security, security.getPublicKeyString());

		FileOutputStream out = new FileOutputStream(acctMapFile.getPath(), true);
		UnicodeWriter writer = new UnicodeWriter(out);
		writer.writeln("noacct=123456789");
		writer.flush();
		out.flush();
		writer.close();

		try
		{
			MartusUtilities.verifyFileAndSignature(acctMapFile, mapSigFile, security, security.getPublicKeyString());
			fail("Verification should have failed 1.");
		}
		catch(FileVerificationException expectedException)
		{
		}

		mapSigFile.delete();
		try
		{
			MartusUtilities.verifyFileAndSignature(acctMapFile, mapSigFile, security, security.getPublicKeyString());
			fail("Verification should have failed 2.");
		}
		catch (FileVerificationException expectedException)
		{
		}

		fileDb.deleteAllData();
	}

	public void testVisitAllAccounts() throws Exception
	{
		class AccountCollector implements Database.AccountVisitor
		{
			public void visit(String accountString)
			{
				list.add(accountString);
			}
			Vector list = new Vector();
		}

		db.writeRecord(shortKey, sampleString1);
		db.writeRecord(otherKey, sampleString2);
		

		AccountCollector ac = new AccountCollector();
		db.visitAllAccounts(ac);
		assertEquals("count?", 2, ac.list.size());
		assertContains("missing 1?", shortKey.getAccountId(), ac.list);
		assertContains("missing 2?", otherKey.getAccountId(), ac.list);
	}

	public void testVisitAllPacketsForAccount() throws Exception
	{
		class PacketCollector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				list.add(key);
			}
			Vector list = new Vector();
		}

		db.writeRecord(shortKey, sampleString1);
		db.writeRecord(shortKey2, sampleString2);
		DatabaseKey shortKey3 = DatabaseKey.createImmutableKey(UniversalIdForTesting.createFromAccountAndPrefix(accountString1 , "x"));
		db.writeRecord(shortKey3, sampleString2);
		db.hide(shortKey3.getUniversalId());

		DatabaseKey burKey = BulletinUploadRecord.getBurKey(DatabaseKey.createImmutableKey(UniversalIdForTesting.createFromAccountAndPrefix(accountString1 , "dx2")));
		DatabaseKey delKey = DeleteRequestRecord.getDelKey(UniversalIdForTesting.createFromAccountAndPrefix(accountString1 , "dx3"));
		db.writeRecord(burKey, sampleString2);
		db.writeRecord(delKey, sampleString2);
		
		
		PacketCollector ac = new PacketCollector();
		db.visitAllRecordsForAccount(ac, accountString1);
		assertEquals("count?", 2, ac.list.size());
		assertContains("missing 1?", shortKey, ac.list);
		assertContains("missing 2?", shortKey2, ac.list);
		assertNotContains("Contains BUR Keys?", burKey.getAccountId(), ac.list);
		assertNotContains("Contains DEL Keys?", delKey.getAccountId(), ac.list);

	}
	
	public void testScrubRecord() throws Exception
	{
		db.writeRecord(shortKey, sampleString1);			
		db.scrubRecord(shortKey);		
		String scrubbedRecord = db.readRecord(shortKey, security);	
			
		assertNotEquals("record not match?",scrubbedRecord, sampleString1);												
	}
	
	int getRecordCount()
	{
		class PacketCounter implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				++count;
			}

			int count;
		}

		PacketCounter counter = new PacketCounter();
		db.visitAllRecords(counter);
		return counter.count;
	}

	class MyFileDatabase extends FileDatabase
	{

		public MyFileDatabase(File directory, MartusCrypto securityToUse) 
		{
			super(directory, securityToUse);
		}

		public void verifyAccountMap() throws FileVerificationException, MissingAccountMapSignatureException 
		{
		}
		
		protected DatabaseKey getDatabaseKey(File accountDir, String bucketName, UniversalId uid)
		{
			return DatabaseKey.createImmutableKey(uid);
		}

		protected String getBucketPrefix(DatabaseKey key)
		{
			return defaultBucketPrefix;
		}

	}

	MockMartusSecurity security;
	MyFileDatabase db;
	File dir;
	String accountString1 = "acct1";
	DatabaseKey shortKey = DatabaseKey.createImmutableKey(UniversalIdForTesting.createFromAccountAndPrefix(accountString1 , "x"));
	DatabaseKey shortKey2 = DatabaseKey.createImmutableKey(UniversalIdForTesting.createFromAccountAndPrefix(accountString1 , "x"));
	DatabaseKey otherKey = DatabaseKey.createImmutableKey(UniversalIdForTesting.createFromAccountAndPrefix("acct2", "x"));
	String sampleString1 = "This is just a little bit of data as a sample";
	String sampleString2 = "Here is a somewhat different sample string";
	byte[] sampleBytes1 = {127,44,17,0,27,99};
	byte[] sampleBytes2 = {88,0,127,56,21,101};
}
