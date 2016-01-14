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
package org.martus.client.swingui.jfx.landing.bulletins;

import org.martus.client.core.TestFxBulletin;
import org.martus.common.ContactKey;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.swing.FontHandler;
import org.martus.util.TestCaseEnhanced;

public class TestBulletinTableData extends TestCaseEnhanced
{

	public TestBulletinTableData(String name)
	{
		super(name);
	}

	public void testBasics() throws Exception
	{
		String title = "My Title";
		String author = "Goofy";
		boolean onServer = true;
		MockMartusSecurity security = new MockMartusSecurity();
		MiniLocalization localization = new MiniLocalization();
		Bulletin b = new Bulletin(security);
		b.set(Bulletin.TAGTITLE, title);
		b.set(Bulletin.TAGAUTHOR, author);
		b.getBulletinHeaderPacket().updateLastSavedTime();
		Integer verifiedAuthor = ContactKey.VERIFIED_ACCOUNT_OWNER;
		BulletinTableRowData data = new BulletinTableRowData(b, onServer, verifiedAuthor, localization);
		assertEquals(title, data.getTitle());
		assertEquals(author, data.getAuthor());
		long lastSavedTime = b.getBulletinHeaderPacket().getLastSavedTime();
		assertEquals(lastSavedTime, data.getDateSaved().longValue());
		assertEquals(onServer, data.isOnServer());
		assertEquals(verifiedAuthor, data.authorVerifiedProperty().getValue());
	}
	
	public void testZawgyi() throws Exception
	{
		MockMartusSecurity security = new MockMartusSecurity();
		MiniLocalization localization = new MiniLocalization();
		Bulletin b = new Bulletin(security);
		b.set(Bulletin.TAGAUTHOR, TestFxBulletin.BURMESE_UNICODE_TEST_STRING);
		
		FontHandler.setDoZawgyiConversion(false);
		BulletinTableRowData notConverted = new BulletinTableRowData(b, false, ContactKey.VERIFIED_ACCOUNT_OWNER, localization);
		assertEquals(TestFxBulletin.BURMESE_UNICODE_TEST_STRING, notConverted.getAuthor());

		try
		{
			FontHandler.setDoZawgyiConversion(true);
			BulletinTableRowData converted = new BulletinTableRowData(b, false, ContactKey.VERIFIED_ACCOUNT_OWNER, localization);
			assertEquals(TestFxBulletin.BURMESE_ZAWGYI_TEST_STRING, converted.getAuthor());
		}
		finally
		{
			FontHandler.setDoZawgyiConversion(false);
		}
	}
}
