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
package org.martus.common.network;

import org.junit.Test;
import org.martus.common.packet.UniversalId;
import org.martus.util.TestCaseEnhanced;

public class TestServerBulletinSummary extends TestCaseEnhanced
{
	public TestServerBulletinSummary(String name)
	{
		super(name);
	}

	@Test
	public void testBasics()
	{
		String accountId = "account1";
		String localId = "localid1";
		String lastModified = "2014-06-07";
		String serverTimestamp = "2012-02-04";
		UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);
		ShortServerBulletinSummary shortSummary = new ShortServerBulletinSummary(localId, lastModified, serverTimestamp);
		ServerBulletinSummary summary = new ServerBulletinSummary(uid, shortSummary);
		assertEquals(uid, summary.getUniversalId());
		assertEquals(lastModified, summary.getLastModified());
		assertEquals(serverTimestamp, summary.getServerTimestamp());
	}

}
