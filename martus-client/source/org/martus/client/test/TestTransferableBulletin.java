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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.List;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.TransferableBulletinList;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.util.TestCaseEnhanced;

public class TestTransferableBulletin extends TestCaseEnhanced
{
	final static String TITLE = "twinkiepie";
	final static String ICKYTITLE = "w*o:r+k'e`d";
	final static String LONGTITLE = "This wonderful title is longer than twenty characters";

	public TestTransferableBulletin(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(security == null)
		{
			security = MockMartusSecurity.createClient();
		}
		store = new MockBulletinStore(security);
		folder = store.createFolder("Wow");
		drag = createTransferableBulletin(TITLE);
		dragId = drag.getBulletins()[0].getLocalId();
	}

	public void tearDown() throws Exception
	{
		drag.dispose();
		super.tearDown();
	}

	public void testBasics() throws Exception
	{
		Bulletin b1 = store.createEmptyBulletin();
		Bulletin b2 = store.createEmptyBulletin();
		Bulletin[] bulletins = {b1, b2};
		TransferableBulletinList list = new TransferableBulletinList(store, bulletins, folder);

		Bulletin[] got = list.getBulletins();
		assertEquals("wrong count?", 2, got.length);
		assertEquals("missing b1?", b1.getUniversalId(), got[0].getUniversalId());
		assertEquals("missing b2?", b2.getUniversalId(), got[1].getUniversalId());
	}

	public void testFlavors()
	{
		DataFlavor[] flavors = drag.getTransferDataFlavors();
		assertEquals(2, flavors.length);

//		assertEquals(true, drag.isDataFlavorSupported(DataFlavor.stringFlavor));
		assertEquals(true, drag.isDataFlavorSupported(DataFlavor.javaFileListFlavor));
		assertEquals(true, drag.isDataFlavorSupported(TransferableBulletinList.getBulletinListDataFlavor()));
//		assertEquals(true, drag.isDataFlavorSupported(drag.getMimeTextDataFlavor()));
	}

	public void testStringFlavor()
	{
		String data = (String)getData(drag, DataFlavor.stringFlavor);
		assertNull("found a stringFlavor?", data);
	}

	public void testFileFlavor() throws Exception
	{
		File file = getFile(drag, "basic");
		int at = file.getName().indexOf(TITLE);
		assertEquals("bad filename?", 0, at);

		TransferableBulletinList icky = createTransferableBulletin(ICKYTITLE);
		file = getFile(icky, "ickyname");
		at = file.getName().indexOf("w o r k e d");
		assertEquals("icky", 0, at);
		assertEndsWith("wrong extension for icky?", ".mba", file.getName());
		file.delete();

		TransferableBulletinList longName = createTransferableBulletin(LONGTITLE);
		file = getFile(longName, "longname");
		at = file.getName().indexOf("This wonderful title");
		assertEquals("long", 0, at);
		assertEndsWith("wrong extension for long?", ".mba", file.getName());
		file.delete();
	}

	public void testBulletinFlavor()
	{
		Object data = getData(drag, TransferableBulletinList.getBulletinListDataFlavor());
		assertNotNull("null bulletinFlavor?", data);
		TransferableBulletinList tb = (TransferableBulletinList)data;
		assertEquals("bad folder?", folder, tb.getFromFolder());
		assertEquals("bad id?", dragId, tb.getBulletins()[0].getLocalId());

		Bulletin[] bulletins = tb.getBulletins();
		assertNotNull("null bulletins?", bulletins);
		assertEquals("bad id?", dragId, bulletins[0].getLocalId());
	}

	public void testExtractFrom()
	{
		TransferableBulletinList tb = TransferableBulletinList.extractFrom(drag);
		assertNotNull("unable to extract", tb);
		assertEquals("extracted it problem", drag.getBulletins()[0].getLocalId(), tb.getBulletins()[0].getLocalId());
		StringSelection string = new StringSelection("Some data");
		tb = TransferableBulletinList.extractFrom(string);
		assertNull("should not extract", tb);
	}


	private TransferableBulletinList createTransferableBulletin(String title) throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		b.setImmutable();
		b.set(Bulletin.TAGTITLE, title);
		store.saveBulletin(b);
		Bulletin[] bulletins = new Bulletin[] {b};
		TransferableBulletinList localTB = new TransferableBulletinList(store, bulletins, folder);
		Bulletin[] got = localTB.getBulletins();
		assertEquals("id after create", b.getLocalId(), got[0].getLocalId());
		return localTB;
	}

	private Object getData(TransferableBulletinList dragList, DataFlavor flavor)
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

	private File getFile(TransferableBulletinList tb, String debugText)
	{
		List list = (List)getData(tb, DataFlavor.javaFileListFlavor);
		assertNotNull(debugText + " null fileListFlavor?", list);
		assertEquals(debugText, 1, list.size());
		Object data = list.get(0);
		assertTrue(debugText + " not a file?", data instanceof File);
		File file = (File)data;
		assertTrue(debugText + " file should always exist", file.exists());
		return file;
	}

	ClientBulletinStore store;
	BulletinFolder folder;
	TransferableBulletinList drag;
	String dragId;
	static MartusCrypto security;
}
