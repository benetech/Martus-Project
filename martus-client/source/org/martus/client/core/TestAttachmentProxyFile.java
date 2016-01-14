/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2015, Beneficent
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
package org.martus.client.core;

import java.io.File;

import org.junit.Test;
import org.martus.client.test.MockBulletinStore;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.util.TestCaseEnhanced;

public class TestAttachmentProxyFile extends TestCaseEnhanced
{
	public TestAttachmentProxyFile(String name)
	{
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		security = MockMartusSecurity.createClient();
		store = new MockBulletinStore();
	}
	
	@Override
	protected void tearDown() throws Exception
	{
		store.deleteAllData();
		super.tearDown();
	}

	@Test
	public void testWrapFile() throws Exception
	{
		File pretendNonTemp = createTempFile();
		try
		{
			AttachmentProxyFile apf = AttachmentProxyFile.wrapFile(pretendNonTemp);
			assertEquals(pretendNonTemp, apf.getFile());
			apf.release();
			assertNull(apf.getFile());
			assertTrue(pretendNonTemp.exists());
		}
		finally
		{
			pretendNonTemp.delete();
		}
	}

	public void testExtractAttachment() throws Exception
	{
		File fileToAttach = createTempFile();
		try
		{
			BulletinForTesting original = new BulletinForTesting(security);
			AttachmentProxy originalProxy = new AttachmentProxy(fileToAttach);
			original.addPrivateAttachment(originalProxy);
			store.saveBulletinForTesting(original);
			
			Bulletin loaded = store.loadFromDatabase(original.getDatabaseKey());
			AttachmentProxy proxyFromDatabase = loaded.getPrivateAttachments()[0];

			AttachmentProxyFile apf = AttachmentProxyFile.extractAttachment(store, proxyFromDatabase);
			File file = apf.getFile();
			assertNotNull(file);
			assertTrue(file.exists());
			apf.release();
			assertNull(apf.getFile());
			assertFalse(file.exists());
		}
		finally
		{
			fileToAttach.delete();
		}
	}

	private MartusSecurity security;
	private MockBulletinStore store;
}
