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

package org.martus.client.test;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.TransferableAttachmentList;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.util.TestCaseEnhanced;

public class TestTransferableAttachments extends TestCaseEnhanced
{
	public TestTransferableAttachments(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(security == null)
		{
			security = MockMartusSecurity.createServer();
		}
		store = new MockBulletinStore(security);
		Bulletin b = new Bulletin(security);
		File temp = createTempFile();
		FileOutputStream out = new FileOutputStream(temp);
		out.write(data);
		out.close();
		AttachmentProxy attach = new AttachmentProxy(temp);
		b.addPrivateAttachment(attach);
		b.setImmutable();

		b.getFieldDataPacket().getAttachments();
		store.saveBulletin(b);

		Bulletin bOut = store.getBulletinRevision(b.getUniversalId());
		AttachmentProxy[] attachments = bOut.getPrivateAttachments();
		assertEquals("Should only have one attachment", 1, attachments.length);
		AttachmentProxy attachment = attachments[0];
		assertNull("File should be null", attachment.getFile());
		drag = createTransferableAttachment(attachment);
	}

	public void tearDown() throws Exception
	{
		drag.dispose();
		super.tearDown();
	}
	
	public void testgetFile() throws Exception
	{
		File file = getFile(drag,"testGetFile");
		assertTrue("File should exist",file.exists());
		FileInputStream in = new FileInputStream(file);
		byte[] bytesIn = new byte[5];
		in.read(bytesIn);
		in.close();
		assertTrue("Data not the same?",Arrays.equals(data,bytesIn));
		
	}

	public void testFlavors()
	{
		DataFlavor[] flavors = drag.getTransferDataFlavors();
		assertEquals(1, flavors.length);
		assertEquals(true, drag.isDataFlavorSupported(DataFlavor.javaFileListFlavor));
	}

	private TransferableAttachmentList createTransferableAttachment(AttachmentProxy proxy) throws Exception
	{
		AttachmentProxy[] attachments = {proxy};
		TransferableAttachmentList localTA = new TransferableAttachmentList(store.getDatabase(), security, attachments );
		return localTA;
	}

	private Object getData(TransferableAttachmentList dragList, DataFlavor flavor)
	{
		Object result = null;
		try
		{
			result = dragList.getTransferData(flavor);
		}
		catch (UnsupportedFlavorException e)
		{
			result = null;
		}

		return result;
	}

	private File getFile(TransferableAttachmentList ta, String debugText)
	{
		List list = (List)getData(ta, DataFlavor.javaFileListFlavor);
		assertNotNull(debugText + " null fileListFlavor?", list);
		assertEquals(debugText, 1, list.size());
		Object dataFile = list.get(0);
		assertTrue(debugText + " not a file?", dataFile instanceof File);
		File file = (File)dataFile;
		assertTrue(debugText + " file should always exist", file.exists());
		return file;
	}

	ClientBulletinStore store;
	BulletinFolder folder;
	TransferableAttachmentList drag;
	static MartusCrypto security;
	private byte[] data = {'H','e','l','l','o'};
}
