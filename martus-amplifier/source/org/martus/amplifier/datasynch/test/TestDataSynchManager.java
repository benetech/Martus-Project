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
package org.martus.amplifier.datasynch.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.martus.amplifier.datasynch.DataSynchManager;

public class TestDataSynchManager extends TestAbstractAmplifierDataSynch
{
	public TestDataSynchManager(String name)
	{
		super(name);
	}

	public void testRemoveAccountsFromList() throws Exception
	{
		ArrayList allAccounts = new ArrayList();
		String account1 = "account1"; 
		String account2 = "account2"; 
		String account3 = "account3";
		String account4 = "account4";
		
		allAccounts.add(account1);
		allAccounts.add(account2);
		allAccounts.add(account3);
		allAccounts.add(account4);
		
		Vector removeAccounts = new Vector();
		removeAccounts.add(account4);
		removeAccounts.add(account2);

		List noAccountsToRemove = DataSynchManager.removeAccountsFromList(allAccounts, null);
		assertEquals("should contain all 4 accounts", 4, noAccountsToRemove.size());
		

		
		List remainingAccounts = DataSynchManager.removeAccountsFromList(allAccounts, removeAccounts);
		assertEquals("New size should be 2", 2, remainingAccounts.size());
		assertTrue("Should contain account 1", remainingAccounts.contains(account1));
		assertTrue("Should contain account 3", remainingAccounts.contains(account3));
	}
	
}
