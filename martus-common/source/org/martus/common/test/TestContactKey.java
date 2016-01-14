/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

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

import org.martus.common.ContactKey;
import org.martus.util.TestCaseEnhanced;

public class TestContactKey extends TestCaseEnhanced
{

	public TestContactKey(String name)
	{
		super(name);
	}
	
	public void testBasics()
	{
		String account1 = "Account 1";
		String account1Name = "Dan";
		ContactKey key1 = new ContactKey(account1, account1Name);
		assertFalse(key1.getSendToByDefault());
		assertEquals(account1, key1.getPublicKey());
		assertEquals(account1Name, key1.getLabel());		
		assertTrue(key1.getCanReceiveFrom());
		assertTrue(key1.getCanSendTo());
		key1.setSendToByDefault(true);
		assertTrue(key1.getSendToByDefault());
		
		ContactKey key2 = new ContactKey(key1);
		assertTrue(key2.getSendToByDefault());
		assertEquals(key1.getPublicKey(), key2.getPublicKey());
		assertEquals(key1.getLabel(), key2.getLabel());		
	}
	
	public void testIsVerified()
	{
		assertTrue(ContactKey.isVerified(ContactKey.VERIFIED_ACCOUNT_OWNER));
		assertTrue(ContactKey.isVerified(ContactKey.VERIFIED_ENTERED_20_DIGITS));
		assertTrue(ContactKey.isVerified(ContactKey.VERIFIED_VISUALLY));
		assertFalse(ContactKey.isVerified(ContactKey.NOT_VERIFIED));
		assertFalse(ContactKey.isVerified(ContactKey.NOT_VERIFIED_UNKNOWN));
		assertFalse(ContactKey.isVerified(new Integer(5)));
	}

}
