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

import junit.framework.TestCase;

import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;


public class TestBulletinHistory extends TestCase
{
	public TestBulletinHistory(String arg0)
	{
		super(arg0);
	}

	public void testStringRepresentation() throws Exception
	{
		BulletinHistory empty = new BulletinHistory();
		BulletinHistory fromEmpty = BulletinHistory.createFromHistoryString(empty.toString());
		assertEquals(0, fromEmpty.size());
		
		UniversalId uid1 = UniversalIdForTesting.createDummyUniversalId();
		UniversalId uid2 = UniversalIdForTesting.createDummyUniversalId();
		BulletinHistory two = new BulletinHistory();
		two.add(uid1.getLocalId());
		two.add(uid2.getLocalId());
		BulletinHistory fromTwo = BulletinHistory.createFromHistoryString(two.toString());
		assertEquals(2, fromTwo.size());
		for(int i=0; i < fromTwo.size(); ++i)
			assertEquals(Integer.toString(i), two.get(0), fromTwo.get(0));
	}
}
