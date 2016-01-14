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
package org.martus.server.formirroring;

import java.util.Vector;

import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockDatabase;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.util.TestCaseEnhanced;

public class TestBulletinMirroringInformation extends TestCaseEnhanced
{
	public TestBulletinMirroringInformation(String name)
	{
		super(name);
	}

	public void testBasics() throws Exception
	{
		MartusCrypto authorSecurity = MockMartusSecurity.createOtherClient();
		
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(authorSecurity);
		bhp.setStatus(BulletinConstants.STATUSDRAFT);
		DatabaseKey key = DatabaseKey.createDraftKey(bhp.getUniversalId());

		String sigString = "Signature";
		MockDatabase db = new MockServerDatabase();
		db.writeRecord(key, "Some text");

		BulletinMirroringInformation bulletinInfo = new BulletinMirroringInformation(db, key, sigString);
		Vector info = bulletinInfo.getInfoWithLocalId();
		
		BulletinMirroringInformation newBulletinInfo = new BulletinMirroringInformation(bhp.getAccountId(), info);
		assertEquals(newBulletinInfo.getInfoWithLocalId(), info);
		assertEquals(newBulletinInfo.getInfoWithUniversalId(), bulletinInfo.getInfoWithUniversalId());
		assertTrue(newBulletinInfo.isDraft());
		assertEquals(newBulletinInfo.isDraft(), bulletinInfo.isDraft());
		assertEquals(newBulletinInfo.getmTime(), bulletinInfo.getmTime());
		assertEquals(newBulletinInfo.getUid(), bulletinInfo.getUid());
		
	}
}
