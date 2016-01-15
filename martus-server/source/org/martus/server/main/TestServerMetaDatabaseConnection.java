/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2014, Beneficent
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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.network.ShortServerBulletinSummary;
import org.martus.common.network.SummaryOfAvailableBulletins;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.test.BulletinForTesting;
import org.martus.server.main.ServerMetaDatabaseConnection.BulletinNotFoundException;
import org.martus.util.Stopwatch;
import org.martus.util.TestCaseEnhanced;
import org.miradi.utils.EnhancedJsonObject;

public class TestServerMetaDatabaseConnection extends TestCaseEnhanced
{
	public TestServerMetaDatabaseConnection(String name) 
	{
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		factory = ServerMetaDatabaseForTesting.getEmptyDatabase(this);
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		factory.close();
		super.tearDown();
	}
	
	interface WithFactory
	{
		public void accept(ServerMetaDatabase factory) throws Exception;
	}
	
	public void testAccounts() throws Exception
	{
		factory.doWithConnection((connection) -> verifyAccounts(connection));
	}

	public void verifyAccounts(ServerMetaDatabaseConnection connection) throws Exception 
	{
		MockMartusSecurity clientSecurity = MockMartusSecurity.createClient();
		assertEquals(0, connection.countAccounts());
		connection.putAccount(clientSecurity.getPublicKeyString());
		assertEquals(1, connection.countAccounts());
		connection.putAccount(clientSecurity.getPublicKeyString());
		assertEquals(1, connection.countAccounts());
		
		MockMartusSecurity otherSecurity = MockMartusSecurity.createOtherClient();
		connection.putAccount(otherSecurity.getPublicKeyString());
		assertEquals(2, connection.countAccounts());
	}
	
	public void testBulletins() throws Exception
	{
		factory.doWithConnection((connection) -> verifyBulletins(connection));
	}

	public void verifyBulletins(ServerMetaDatabaseConnection connection) throws Exception 
	{
		MockMartusSecurity clientSecurity = MockMartusSecurity.createClient();
		
		Bulletin b1 = new BulletinForTesting(clientSecurity);
		Instant b1Timestamp = Instant.now().plusSeconds(100);
		assertEquals(0, connection.countBulletins());
		connection.revisionWasSaved(b1.getBulletinHeaderPacket(), b1Timestamp);
		assertEquals(1, connection.countAccounts());
		assertEquals(1, connection.countBulletins());
		assertEquals(b1Timestamp, connection.getTimestamp(b1.getUniversalId()));
		connection.revisionWasSaved(b1.getBulletinHeaderPacket(), b1Timestamp);
		assertEquals(1, connection.countBulletins());
		
		Bulletin b2 = new BulletinForTesting(clientSecurity);
		Instant b2Timestamp = Instant.now().plusSeconds(200);
		connection.revisionWasSaved(b2.getBulletinHeaderPacket(), b2Timestamp);
		assertEquals(1, connection.countAccounts());
		assertEquals(2, connection.countBulletins());
		assertEquals(b2Timestamp, connection.getTimestamp(b2.getUniversalId()));
		
		MockMartusSecurity otherClient = MockMartusSecurity.createOtherClient();
		Bulletin b3 = new BulletinForTesting(otherClient);
		Instant b3Timestamp = Instant.now().plusSeconds(300);
		connection.revisionWasSaved(b3.getBulletinHeaderPacket(), b3Timestamp);
		assertEquals(2, connection.countAccounts());
		assertEquals(3, connection.countBulletins());
		assertEquals(b3Timestamp, connection.getTimestamp(b3.getUniversalId()));

	}
	
	public void testReadableBy() throws Exception
	{
		factory.doWithConnection(connection -> verifyReadableBy(connection));
	}
	
	private void verifyReadableBy(ServerMetaDatabaseConnection connection) throws Exception
	{
		SummaryOfAvailableBulletins withNoBulletins = connection.listBulletinsDownloadableBy("No such account");
		assertEquals(0, withNoBulletins.size());

		Instant serverFileTimestamp = Instant.now().plusSeconds(1000);

		MockMartusSecurity fd = MockMartusSecurity.createClient();
		BulletinForTesting b1 = new BulletinForTesting(fd);

		{
			connection.revisionWasSaved(b1.getBulletinHeaderPacket(), Instant.now());
			SummaryOfAvailableBulletins withNoHeadquarters = connection.listBulletinsDownloadableBy(fd.getPublicKeyString());
			assertEquals(1, withNoHeadquarters.size());
			Set<String> accountIds = withNoHeadquarters.getAccountIds();
			String accountId = accountIds.toArray(new String[0])[0];
			assertEquals(b1.getAccount(), accountId);
			Set<ShortServerBulletinSummary> bulletinSummaries = withNoHeadquarters.getSummaries(accountId); 
			ShortServerBulletinSummary summary = bulletinSummaries.toArray(new ShortServerBulletinSummary[0])[0];
			assertEquals(b1.getLocalId(), summary.getLocalId());
		}

		MockMartusSecurity hq = MockMartusSecurity.createHQ();
		BulletinForTesting b2 = new BulletinForTesting(fd);
		
		{
			HeadquartersKey hqKey = new HeadquartersKey(hq.getPublicKeyString());
			HeadquartersKeys hqKeys = new HeadquartersKeys(hqKey);
			b2.addAuthorizedToReadKeys(hqKeys);
			connection.revisionWasSaved(b2.getBulletinHeaderPacket(), serverFileTimestamp);
			SummaryOfAvailableBulletins withHeadquarters = connection.listBulletinsDownloadableBy(fd.getPublicKeyString());
			assertEquals(2, withHeadquarters.size());
			Set<ShortServerBulletinSummary> summaries = withHeadquarters.getSummaries(b1.getAccount());
			assertEquals(2, summaries.size());
			ShortServerBulletinSummary summaryForB1 = summaries.toArray(new ShortServerBulletinSummary[0])[0];
			ShortServerBulletinSummary summaryForB2 = summaries.toArray(new ShortServerBulletinSummary[0])[1];
			Set<String> got = new HashSet<String>();
			got.add(summaryForB1.getLocalId());
			got.add(summaryForB2.getLocalId());
			
			assertContains(b1.getLocalId(), got);
			assertContains(b2.getLocalId(), got);
		}
	}

	public void testSpeed() throws Exception
	{
		factory.doWithConnection(connection -> verifySpeed(connection));
	}
	
	private void verifySpeed(ServerMetaDatabaseConnection connection) throws Exception 
	{
		Stopwatch sw = new Stopwatch();
		MockMartusSecurity author = MockMartusSecurity.createClient();
		MockMartusSecurity hq1 = MockMartusSecurity.createHQ();
		MockMartusSecurity hq2 = MockMartusSecurity.createOtherClient();
		
		// NOTE: This has been tested with 100,000 records and a 1000 ratio
		// On Kevin's laptop, it takes 5 minutes to create the records, 
		// 1 second to report 0 matches, 2 seconds to report 100 matches, and 
		// 34 seconds to report 100000 matches.
		// (10,000 and 100 takes 52, 0, 2, 0.
		long recordCount = 1000;
		long readableRatio = 100;

		String authorAccountId = author.getPublicKeyString();
		String hq1AccountId = hq1.getPublicKeyString();
		String hq2AccountId = hq2.getPublicKeyString();
		
		Instant earliestTimestamp = Instant.now();
		int timestampIncrement = 1;
		for(long i = 0; i < recordCount; ++i)
		{
			BulletinForTesting b = new BulletinForTesting(author);
			if(i % readableRatio == 1)
				b.addAuthorizedToReadKeys(new HeadquartersKeys(new HeadquartersKey(hq1AccountId)));
			else if(i % readableRatio == 2)
				b.addAuthorizedToReadKeys(new HeadquartersKeys(new HeadquartersKey(hq2AccountId)));
				
			Instant timestamp = earliestTimestamp.plusSeconds(i * timestampIncrement);
			connection.revisionWasSaved(b.getBulletinHeaderPacket(), timestamp);
		}
		sw.stop();
//		System.out.println("Created " + recordCount + " bulletins in " + sw.elapsedInSeconds() + " seconds");
	
		assertEquals(0, connection.listBulletinsDownloadableBy("NoSuchAccountId").size());
		
		assertEquals(Math.min(100, recordCount), connection.listBulletinsDownloadableBy(authorAccountId).size());
		
		long expectedHqBulletinCount = recordCount / readableRatio;
		assertEquals(expectedHqBulletinCount, connection.listBulletinsDownloadableBy(hq1AccountId).size());
		
		int lastFew = 3;
		Instant timeStampForLastFew = earliestTimestamp.plusSeconds((recordCount - lastFew) * timestampIncrement);
		String timeStampForLastFewIso = ServerMetaDatabaseConnection.formatIsoDateTime(timeStampForLastFew);
//		sw.start();
		assertEquals(lastFew, connection.listRecentBulletinsDownloadableBy(authorAccountId, timeStampForLastFewIso).size());
//		System.out.println(sw.elapsed());
	}
	
	public void testUpdateBulletin() throws Exception
	{
		factory.doWithConnection(connection -> verifyUpdateBulletin(connection));
	}

	private void verifyUpdateBulletin(ServerMetaDatabaseConnection connection) throws Exception 
	{
		MockMartusSecurity author = MockMartusSecurity.createClient();
		BulletinForTesting b = new BulletinForTesting(author);
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
		bhp.updateLastSavedTime();
		
		Instant timestamp1 = Instant.now().plusSeconds(1000);
		connection.revisionWasSaved(bhp, timestamp1);
		assertEquals(timestamp1, connection.getTimestamp(b.getUniversalId()));
		
		Instant timestamp2 = timestamp1.plusSeconds(1000);
		connection.revisionWasSaved(bhp, timestamp2);
		assertEquals(timestamp2, connection.getTimestamp(b.getUniversalId()));
		assertEquals(1, connection.countBulletins());

		MockMartusSecurity hq = MockMartusSecurity.createHQ();
		b.addAuthorizedToReadKeys(new HeadquartersKeys(new HeadquartersKey(hq.getPublicKeyString())));
		Instant timestamp3 = timestamp2.plusSeconds(1000);
		connection.revisionWasSaved(bhp, timestamp3);
		assertEquals(timestamp3, connection.getTimestamp(b.getUniversalId()));
		assertEquals(1, connection.countBulletins());
		
		assertEquals(1, (long) connection.listBulletinsDownloadableBy(author.getPublicKeyString()).size());
		assertEquals(1, (long) connection.listBulletinsDownloadableBy(hq.getPublicKeyString()).size());
		
		b.setAuthorizedToReadKeys(new HeadquartersKeys());
		bhp.updateLastSavedTime();
		Instant timestamp4 = timestamp3.plusSeconds(1000);
		connection.revisionWasSaved(bhp, timestamp4);
		assertEquals(1, (long) connection.listBulletinsDownloadableBy(author.getPublicKeyString()).size());
		assertEquals(0, (long) connection.listBulletinsDownloadableBy(hq.getPublicKeyString()).size());
		assertEquals(ServerMetaDatabaseConnection.getLastSavedTimeInstant(bhp), connection.getLastSavedTime(bhp.getUniversalId()));
		
	}

	public void testRemoveBulletin() throws Exception
	{
		factory.doWithConnection(connection -> verifyRemoveBulletin(connection));
	}

	private void verifyRemoveBulletin(ServerMetaDatabaseConnection connection) throws Exception 
	{
		MockMartusSecurity author = MockMartusSecurity.createClient();
		BulletinForTesting b = new BulletinForTesting(author);
		Instant timestamp1 = Instant.now();
		connection.revisionWasSaved(b.getBulletinHeaderPacket(), timestamp1);
		assertEquals(1, connection.countBulletins());
		
		connection.revisionWasRemoved(b.getUniversalId());
		assertEquals(0, connection.countBulletins());
		assertEquals(0, connection.listBulletinsDownloadableBy(author.getPublicKeyString()).size());
	}
	
	public void testLastModifiedField() throws Exception
	{
		factory.doWithConnection(connection -> verifyLastModifiedField(connection));
	}

	private void verifyLastModifiedField(ServerMetaDatabaseConnection connection) throws Exception 
	{
		MockMartusSecurity author = MockMartusSecurity.createClient();
		BulletinForTesting b = new BulletinForTesting(author);
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
		bhp.updateLastSavedTime();
		
		try
		{
			connection.getLastSavedTime(bhp.getUniversalId());
			fail("Should have thrown for non-existent bulletin!");
		}
		catch(BulletinNotFoundException ignoreExpected)
		{
		}
		
		Instant timestamp1 = Instant.now().plusSeconds(1000);
		connection.revisionWasSaved(bhp, timestamp1);
		
		assertEquals(ServerMetaDatabaseConnection.getLastSavedTimeInstant(bhp), connection.getLastSavedTime(bhp.getUniversalId()));
	}

	public void testGetRecentBulletins() throws Exception
	{
		factory.doWithConnection(connection -> verifyGetRecentBulletins(connection));
	}

	private void verifyGetRecentBulletins(ServerMetaDatabaseConnection connection) throws Exception 
	{
		MockMartusSecurity author = MockMartusSecurity.createClient();
		String authorId = author.getPublicKeyString();
		
		final String FOREVER = "";
		EnhancedJsonObject emptyJson = connection.getRecentBulletinsDownloadableBy(authorId, FOREVER);
		assertEquals(0, emptyJson.getInt(SummaryOfAvailableBulletins.JSON_KEY_COUNT));
		assertEquals("", emptyJson.getString(SummaryOfAvailableBulletins.JSON_KEY_HIGHEST_SERVER_TIMESTAMP));

		Instant start = Instant.now();
		for(int i = 0; i < 10; ++i)
		{
			BulletinForTesting b = new BulletinForTesting(author);
			BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
			bhp.updateLastSavedTime();
			
			Instant timestamp = start.plusSeconds(i);
			connection.revisionWasSaved(bhp, timestamp);
		}
		
		String earliestServerTimestamp = ServerMetaDatabaseConnection.formatIsoDateTime(start.plusSeconds(5));
		EnhancedJsonObject json = connection.getRecentBulletinsDownloadableBy(authorId, earliestServerTimestamp);
		assertEquals(5, json.getInt(SummaryOfAvailableBulletins.JSON_KEY_COUNT));
		
		String expectedHighestTimeStamp = ServerMetaDatabaseConnection.formatIsoDateTime(start.plusSeconds(9));
		String highestTimestamp = json.getString(SummaryOfAvailableBulletins.JSON_KEY_HIGHEST_SERVER_TIMESTAMP);
		assertEquals(expectedHighestTimeStamp, highestTimestamp);

		String nextServerTimestamp = new SummaryOfAvailableBulletins(json).getNextServerTimestamp();
		Instant highestAsInstant = ServerMetaDatabaseConnection.parseIsoDateTime(highestTimestamp);
		Instant nextAsInstant = ServerMetaDatabaseConnection.parseIsoDateTime(nextServerTimestamp);
		long timeDelta = highestAsInstant.until(nextAsInstant, ChronoUnit.MILLIS);
		assertTrue("Next not after highest?", nextAsInstant.isAfter(highestAsInstant));
		assertTrue("Next too far in future?", timeDelta < 3);
	}
	
	public void testListAvailableTimestampCornerCases() throws Exception
	{
		factory.doWithConnection(connection -> verifyListAvailableTimestampCornerCases(connection));
	}

	private void verifyListAvailableTimestampCornerCases(ServerMetaDatabaseConnection connection) throws Exception 
	{
		MockMartusSecurity author = MockMartusSecurity.createClient();
		String authorId = author.getPublicKeyString();
		
		Instant recentPast = Instant.now().minus(Duration.ofHours(1));
		String askAboutPast = ServerMetaDatabaseConnection.formatIsoDateTime(recentPast);
		EnhancedJsonObject jsonPast = connection.getRecentBulletinsDownloadableBy(authorId, askAboutPast);
		assertEquals(0, jsonPast.getInt(SummaryOfAvailableBulletins.JSON_KEY_COUNT));
		assertEquals("", jsonPast.get(SummaryOfAvailableBulletins.JSON_KEY_HIGHEST_SERVER_TIMESTAMP));
		assertEquals(askAboutPast, new SummaryOfAvailableBulletins(jsonPast).getNextServerTimestamp());
		EnhancedJsonObject jsonPast2 = connection.getRecentBulletinsDownloadableBy(authorId, askAboutPast);
		assertEquals("", jsonPast2.get(SummaryOfAvailableBulletins.JSON_KEY_HIGHEST_SERVER_TIMESTAMP));
		assertEquals(askAboutPast, new SummaryOfAvailableBulletins(jsonPast2).getNextServerTimestamp());
		
		Instant future = Instant.now().plus(Duration.ofDays(2));
		String askAboutFuture = ServerMetaDatabaseConnection.formatIsoDateTime(future);
		EnhancedJsonObject jsonFuture = connection.getRecentBulletinsDownloadableBy(authorId, askAboutFuture);
		assertEquals(0, jsonFuture.getInt(SummaryOfAvailableBulletins.JSON_KEY_COUNT));
		assertEquals("", jsonFuture.getString(SummaryOfAvailableBulletins.JSON_KEY_HIGHEST_SERVER_TIMESTAMP));
		String nextAfterFuture = new SummaryOfAvailableBulletins(jsonFuture).getNextServerTimestamp();
		Instant nextAsInstant = ServerMetaDatabaseConnection.parseIsoDateTime(nextAfterFuture);
		Instant nowAsInstant = Instant.now();
		long timeDelta = nextAsInstant.until(nowAsInstant, ChronoUnit.SECONDS);
		assertTrue("Didn't return almost now?", timeDelta < 2);
	}
	
	private ServerMetaDatabaseForTesting factory;
}
