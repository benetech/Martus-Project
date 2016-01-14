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

package org.martus.common.bulletin;

import java.io.File;
import java.io.FileInputStream;

import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.test.MockBulletinStore;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;


public class TestBulletinZipUtilities extends TestCaseEnhanced 
{
	public TestBulletinZipUtilities(String name)
	{
		super(name);
	}

	public void testTimestamps() throws Exception
	{
		MockMartusSecurity security = MockMartusSecurity.createClient();
		MockBulletinStore store = new MockBulletinStore(this);
		Bulletin b = new Bulletin(security);
		store.saveBulletinForTesting(b);
		
		File destZipFile1 = createTempFile();
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(store.getDatabase(), b.getDatabaseKey(), destZipFile1, security);
		FileInputStream in1 = new FileInputStream(destZipFile1);
		String digest1 = StreamableBase64.encode(MartusSecurity.createDigest(in1));
		in1.close();
		
		Thread.sleep(3000);
		
		File destZipFile2 = createTempFile();
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(store.getDatabase(), b.getDatabaseKey(), destZipFile2, security);
		FileInputStream in2 = new FileInputStream(destZipFile2);
		String digest2 = StreamableBase64.encode(MartusSecurity.createDigest(in2));
		in2.close();
		
		assertEquals("Same bulletin zip has different digests?", digest1, digest2);
	}

}
