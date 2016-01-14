/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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
package org.martus.client.swingui;

import java.util.Vector;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.swing.UiList;

public class UiBulletinTitleListComponent extends UiList
{
	public UiBulletinTitleListComponent(UiMainWindow mainWindow, Vector bulletins)
	{
		super(extractTitles(mainWindow, bulletins));
	}

	private static String[] extractTitles(UiMainWindow window, Vector bulletins)
	{
		UiFontEncodingHelper fontHelper = new UiFontEncodingHelper(window.getDoZawgyiConversion());
		String[] titles = new String[bulletins.size()];
		for (int i = 0; i < titles.length; i++)
		{
			Bulletin b = (Bulletin)bulletins.get(i);
			String bulletinTitle = b.get(BulletinConstants.TAGTITLE);
			if(bulletinTitle == null || bulletinTitle.length() == 0)
				bulletinTitle = window.getLocalization().getFieldLabel("UntitledBulletin");
			bulletinTitle = fontHelper.getDisplayable(bulletinTitle);
			titles[i] = bulletinTitle;
		}
		return titles;
	}

}
