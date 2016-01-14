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

import org.martus.common.packet.AccountId;
import org.martus.util.Stopwatch;
import org.martus.util.TestCaseEnhanced;


public class TestAccountId extends TestCaseEnhanced
{
	public TestAccountId(String name)
	{
		super(name);
	}

	public void testAccountIdNestedClass() throws Exception
	{
		String publicKey = "lsiejflsiefjlsiefjselifjesli";
		String publicKeyCopy = new String(publicKey.toCharArray());
		assertEquals(publicKey, publicKeyCopy);
		AccountId accountId1 = AccountId.create(publicKey);
		AccountId accountId2 = AccountId.create(publicKey);
		assertEquals("not same id?", accountId1.objectId(), accountId2.objectId());
		assertEquals("not equals?", accountId1, accountId2);
		assertEquals("not same hash?", accountId1.hashCode(), accountId2.hashCode());
	}
	
	public void testAccountIdSpeed() throws Exception
	{
		for(int i = 0; i < 10000; ++i)
		{
			String fakeAccountId = UniversalIdForTesting.createDummyUniversalId().getLocalId();
			AccountId.create(fakeAccountId);
		}
		
		String accountId = "ow38wefownef8938r2hiosenfosiefh";
		Stopwatch sw = new Stopwatch();

		sw.start();
		for(int i = 0; i < 100; ++i)
			AccountId.create(accountId);
		//System.out.println(sw.elapsed());

		sw.start();
		for(int i = 0; i < 10000; ++i)
			AccountId.create(accountId);
		//.out.println(sw.elapsed());
		
			
	}
}
