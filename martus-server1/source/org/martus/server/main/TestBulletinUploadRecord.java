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

package org.martus.server.main;

import java.io.BufferedReader;
import java.io.StringReader;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.BulletinUploadRecord;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;


public class TestBulletinUploadRecord extends TestCaseEnhanced
{

	public TestBulletinUploadRecord(String name)
	{
		super(name);
	}

	public void testCreateBulletinUploadRecord() throws Exception
	{
		MartusCrypto serverSecurity = MockMartusSecurity.createServer();
		MockMartusSecurity clientSecurity = MockMartusSecurity.createClient();
		Bulletin b1 = new Bulletin(clientSecurity);
		b1.set(Bulletin.TAGTITLE, "Title1");

		String bur = BulletinUploadRecord.createBulletinUploadRecord(b1.getLocalId(), serverSecurity);
		String bur2 = BulletinUploadRecord.createBulletinUploadRecord(b1.getLocalId(), serverSecurity);
		assertEquals(bur, bur2);

		BufferedReader reader = new BufferedReader(new StringReader(bur));
		String gotFileTypeIdentifier = reader.readLine();
		assertEquals("Martus Bulletin Upload Record 1.0", gotFileTypeIdentifier);
		assertEquals(b1.getLocalId(), reader.readLine());
		String gotTimeStamp = reader.readLine();
		String now = MartusServerUtilities.createTimeStamp();
		assertStartsWith(now.substring(0, 13), gotTimeStamp);
		String gotDigest = reader.readLine();
		byte[] partOfPrivateKey = serverSecurity.getDigestOfPartOfPrivateKey();
		String stringToDigest = gotFileTypeIdentifier + "\n" + b1.getLocalId() + "\n" + gotTimeStamp + "\n" + StreamableBase64.encode(partOfPrivateKey) + "\n"; 
		assertEquals(gotDigest, MartusCrypto.createDigestString(stringToDigest));

		String bogusStringToDigest = gotFileTypeIdentifier + gotTimeStamp + b1.getLocalId() + StreamableBase64.encode(partOfPrivateKey); 
		assertNotEquals(MartusCrypto.createDigestString(bogusStringToDigest), gotDigest);
	}
	
	public void testGetBurKey() throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();

		DatabaseKey draftKey = DatabaseKey.createDraftKey(uid);
		assertTrue("not draft?", BulletinUploadRecord.getBurKey(draftKey).isDraft());

		DatabaseKey sealedKey = DatabaseKey.createSealedKey(uid);
		DatabaseKey sealedBurKey = BulletinUploadRecord.getBurKey(sealedKey);
		assertTrue("not sealed?", sealedBurKey.isSealed());
		
		assertEquals(uid.getAccountId(), sealedBurKey.getAccountId());
		assertEquals("BUR-" + uid.getLocalId(), sealedBurKey.getLocalId());
	}
	
	public void testWasBurCreatedByThisCrypto() throws Exception
	{
		MartusCrypto serverSecurity = MockMartusSecurity.createServer();
		MockMartusSecurity otherSecurity = MockMartusSecurity.createOtherServer();
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		String burRecord = BulletinUploadRecord.createBulletinUploadRecord(uid.getLocalId(), otherSecurity);
		assertTrue("This burRecord was not created by this security?", BulletinUploadRecord.wasBurCreatedByThisCrypto(burRecord, otherSecurity));
		assertFalse("This burRecord was created by this security?", BulletinUploadRecord.wasBurCreatedByThisCrypto(burRecord, serverSecurity));
		assertFalse("Null burRecord should be handled gracefully.", BulletinUploadRecord.wasBurCreatedByThisCrypto(null, serverSecurity));
	}

}
