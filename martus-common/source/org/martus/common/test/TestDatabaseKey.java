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

import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.UniversalId;
import org.martus.util.*;



public class TestDatabaseKey extends TestCaseEnhanced
{
	public TestDatabaseKey(String name)
	{
		super(name);
	}

	public void TRACE(String text)
	{
		//System.out.println(text);
	}

	public void testConstructors() throws Exception
	{
		UniversalId uid1 = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey key1 = DatabaseKey.createMutableKey(uid1);
		assertEquals("bad uid1?", uid1, key1.getUniversalId());
		assertEquals("not Mutable?", true, key1.isMutable());

		DatabaseKey key2 = DatabaseKey.createImmutableKey(uid1);
		assertEquals("bad uid2?", uid1, key2.getUniversalId());
		assertEquals("not Immutable?", true, key2.isImmutable());

		DatabaseKey key3 = DatabaseKey.createLegacyKey(uid1);
		assertEquals("bad uid3?", uid1, key3.getUniversalId());
		assertEquals("Mutable?", false, key3.isMutable());
		assertEquals("not Immutable?", true, key3.isImmutable());

		DatabaseKey keySealed = DatabaseKey.createKey(uid1, BulletinConstants.STATUSIMMUTABLE);
		assertEquals("bad keyImmutable?", uid1, keySealed.getUniversalId());
		assertEquals("Mutable?", false, keySealed.isMutable());
		assertEquals("not Immutable?", true, keySealed.isImmutable());

		DatabaseKey keyDraft = DatabaseKey.createKey(uid1, BulletinConstants.STATUSMUTABLE);
		assertEquals("bad keyMutable?", uid1, keyDraft.getUniversalId());
		assertEquals("not Mutable?", true, keyDraft.isMutable());
		assertEquals("sealed?", false, keyDraft.isImmutable());
	}

	public void testEqualsStrings() throws Exception
	{
		UniversalId uid1 = UniversalIdForTesting.createDummyUniversalId();
		UniversalId uid2 = UniversalIdForTesting.createDummyUniversalId();

		DatabaseKey key1 = DatabaseKey.createImmutableKey(uid1);
		DatabaseKey key2 = DatabaseKey.createImmutableKey(UniversalId.createFromAccountAndLocalId(uid1.getAccountId(), uid1.getLocalId()));
		DatabaseKey key3 = DatabaseKey.createImmutableKey(uid2);
		DatabaseKey key4 = DatabaseKey.createImmutableKey(uid1);
		key4.setMutable();
		assertEquals("self should match", key1, key1);
		assertEquals("never match null", false, key1.equals(null));
		assertEquals("never match uid", false, key1.equals(uid1));
		assertEquals("Keys should match", key1, key2);
		assertEquals("symmetrical equals", key2, key1);
		assertEquals("should not match", false, key1.equals(key3));
		assertEquals("symmetrical not equals", false, key3.equals(key1));
		assertNotEquals("status ignored?", key4, key1);

		assertEquals("hash self should match", key1.hashCode(), key1.hashCode());
		assertEquals("hash Keys should match", key1.hashCode(), key2.hashCode());
		assertNotEquals("hash didn't use status?", key1.hashCode(), key4.hashCode());
	}

	public void testEquals() throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();

		DatabaseKey key1 = DatabaseKey.createImmutableKey(uid);
		DatabaseKey key2 = DatabaseKey.createImmutableKey(uid);
		DatabaseKey key3 = DatabaseKey.createImmutableKey(UniversalIdForTesting.createDummyUniversalId());
		assertEquals("self should match", key1, key1);
		assertEquals("never match null", false, key1.equals(null));
		assertEquals("never match string", false, key1.equals(uid));
		assertEquals("Keys should match", key1, key2);
		assertEquals("symmetrical equals", key2, key1);
		assertEquals("should not match", false, key1.equals(key3));
		assertEquals("symmetrical not equals", false, key3.equals(key1));

		assertEquals("hash self should match", key1.hashCode(), key1.hashCode());
		assertEquals("hash Keys should match", key1.hashCode(), key2.hashCode());
	}

	public void testGetAccount() throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey key = DatabaseKey.createImmutableKey(uid);
		assertEquals("wrong account?", uid.getAccountId(), key.getAccountId());
	}

	public void testStatus() throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey key = DatabaseKey.createImmutableKey(uid);
		assertEquals("Default not Immutable?", true, key.isImmutable());
		assertEquals("Default was Mutable?", false, key.isMutable());
		key.setMutable();
		assertEquals("Immutable still set?", false, key.isImmutable());
		assertEquals("Mutable not set?", true, key.isMutable());
		key.setImmutable();
		assertEquals("Immutable not set?", true, key.isImmutable());
		assertEquals("Mutable still set?", false, key.isMutable());

	}
}
