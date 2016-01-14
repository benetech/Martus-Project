/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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

import java.util.Vector;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.BulletinUploadRecord;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.DeleteRequestRecord;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.TestCaseEnhanced;

public class TestServerFileDatabase extends TestCaseEnhanced
{
	public TestServerFileDatabase(String name) throws Exception
	{
		super(name);
	}
	
	
	protected void setUp() throws Exception
	{
		super.setUp();
		if(security == null)
		{
			security = MockMartusSecurity.createServer();
			db = new ServerFileDatabase(createTempDirectory(), security);
			db.initialize();
		}		
	}


	protected void tearDown() throws Exception
	{
		db.deleteAllData();
		super.tearDown();
	}


	public void testGetmTimeNoKeyInDatabase() throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey burKey = BulletinUploadRecord.getBurKey(DatabaseKey.createMutableKey(uid));
		try
		{
			db.getmTime(burKey);
			fail("Should have thrown since not in database");
		}
		catch (Exception expected)
		{
			String expectedStart = "ServerFileDatabase.getmTime: No Bur or Del Packet: ";
			assertContains(expectedStart, expected.getMessage());
			assertContains(MartusCrypto.formatAccountIdForLog(uid.getAccountId()), expected.getMessage());
			assertContains(burKey.getLocalId(), expected.getMessage());
		}
	}
	
	public void testGetmTimeWithBurInDatabase() throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey mutableKey = DatabaseKey.createMutableKey(uid);
		String timeStamp = MartusServerUtilities.createTimeStamp();
		String burRecord = BulletinUploadRecord.createBulletinUploadRecordWithSpecificTimeStamp(uid.getLocalId(), timeStamp, security);
		DatabaseKey burKey = BulletinUploadRecord.getBurKey(mutableKey);
		db.writeRecord(burKey, burRecord);
		long timeFromRecord = db.getmTime(mutableKey);
		assertEquals(timeStamp, MartusServerUtilities.getFormattedTimeStamp(timeFromRecord));
	}
	
	public void testGetmTimeWithDelInDatabase() throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey mutableKey = DatabaseKey.createMutableKey(uid);
		DeleteRequestRecord delRecord = new DeleteRequestRecord(uid.getAccountId(), new Vector(), "signature");
		String timeStamp = delRecord.timeStamp;
		DatabaseKey delKey = DeleteRequestRecord.getDelKey(mutableKey.getUniversalId());
		db.writeRecord(delKey, delRecord.getDelData());
		long timeFromRecord = db.getmTime(mutableKey);
		assertEquals(timeStamp, MartusServerUtilities.getFormattedTimeStamp(timeFromRecord));
	}
	

	
	static MockMartusSecurity security; 
	static ServerFileDatabase db;
}
