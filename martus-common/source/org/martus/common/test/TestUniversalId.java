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

import org.bouncycastle.util.Arrays;
import org.martus.common.packet.UniversalId;
import org.martus.util.TestCaseEnhanced;


public class TestUniversalId extends TestCaseEnhanced
{

	public TestUniversalId(String name)
	{
		super(name);
	}

	public void testConstructorWithBothIds()
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(sampleAccountId, sampleLocalId);
		assertEquals("account?", sampleAccountId, uid.getAccountId());
		assertEquals("local?", sampleLocalId, uid.getLocalId());
	}

	public void testAccountId()
	{
		final String sampleAccountId1 = "an account id";
		UniversalId uid = UniversalIdForTesting.createFromAccountAndPrefix(sampleAccountId1, "");
		assertEquals("wrong account?", sampleAccountId1, uid.getAccountId());

		final String sampleAccountId2 = "another silly account id";
		uid.setAccountId(sampleAccountId2);
		assertEquals("didn't set account?", sampleAccountId2, uid.getAccountId());
	}

	public void testLocalId()
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();

		assertNotNull("no local id?", uid.getLocalId());
		assertTrue("local id too short?", uid.getLocalId().length() > 20);
		assertTrue("local id too long?", uid.getLocalId().length() < 40);
		assertEquals("contructor didn't strip colons?", -1, uid.getLocalId().indexOf(":"));

		UniversalId uid2 = UniversalIdForTesting.createDummyUniversalId();
		assertNotEquals("dupe?", uid.getLocalId(), uid2.getLocalId());

		uid.setLocalId(sampleLocalId);
		assertEquals("didn't set local?", sampleLocalId, uid.getLocalId());

		uid.setLocalId("This:That");
		assertEquals("setter didn't strip colons?", "This-That", uid.getLocalId());
		
		String prefix = "B-";
		String suffix = "_R";
		byte[] bytes = new byte[UniversalId.LOCALID_RANDOM_BYTE_COUNT];
		Arrays.fill(bytes, (byte) 0x65);
		String localId = UniversalId.createLocalIdFromByteArray( prefix, bytes, suffix);
		assertTrue(localId.startsWith(prefix));
		assertTrue(localId.endsWith(suffix));
	}

	public void testToString()
	{
		UniversalId uid = UniversalIdForTesting.createFromAccountAndPrefix(sampleAccountId, "");

		String whole = uid.toString();
		assertContains("no account?", sampleAccountId, whole);
		assertContains("no local?", uid.getLocalId(), whole);
		assertEquals("toString didn't strip colons?", -1, whole.indexOf(":"));
	}

	public void testEquals()
	{
		UniversalId uid1 = UniversalId.createFromAccountAndLocalId(sampleAccountId, sampleLocalId);
		assertEquals("equals said false1a?", true, uid1.equals(uid1));
		assertEquals("equals said true1b?", false, uid1.equals(null));
		assertEquals("equals said true1c?", false, uid1.equals(uid1.toString()));

		UniversalId uid2 = UniversalId.createFromAccountAndLocalId(new String(sampleAccountId), new String(sampleLocalId));
		assertEquals("equals said false2a?", true, uid1.equals(uid2));
		assertEquals("equals said false2b?", true, uid2.equals(uid1));

		UniversalId uid3 = UniversalId.createFromAccountAndLocalId(sampleAccountId+"x", sampleLocalId);
		assertEquals("equals said true3a?", false, uid1.equals(uid3));
		assertEquals("equals said true3b?", false, uid3.equals(uid1));

		UniversalId uid4 = UniversalId.createFromAccountAndLocalId(sampleAccountId, sampleLocalId+"x");
		assertEquals("equals said true4a?", false, uid1.equals(uid4));
		assertEquals("equals said true4b?", false, uid4.equals(uid1));

		assertEquals("hashCode 1 2", uid1.hashCode(), uid2.hashCode());
		assertNotEquals("hashCode 1 3", uid1.hashCode(), uid3.hashCode());
		assertNotEquals("hashCode 1 4", uid1.hashCode(), uid4.hashCode());
		assertNotEquals("hashCode 3 4", uid3.hashCode(), uid4.hashCode());
	}

	public void testCreateFromString() throws Exception
	{
		UniversalId uid1 = UniversalId.createFromAccountAndLocalId(sampleAccountId, sampleLocalId);
		UniversalId uid2 = UniversalId.createFromString(uid1.toString());
		assertEquals("account?", uid1.getAccountId(), uid2.getAccountId());
		assertEquals("local?", uid1.getLocalId(), uid2.getLocalId());

		try
		{
			UniversalId.createFromString("lbisdjf");
			fail("Should have thrown!");
		}
		catch(UniversalId.NotUniversalIdException ignoreExpectedException)
		{
		}
	}
	
	final String sampleAccountId = "an account id";
	final String sampleLocalId = "a local id";
}
