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

package org.martus.client.core;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.common.MartusXml;

public class MartusClientXml
{

	public static String getFolderListTagStart()
	{
		return MartusXml.getTagStart(MartusClientXml.tagFolderList);
	}

	public static String getFolderListTagEnd()
	{
		return MartusXml.getTagEnd(MartusClientXml.tagFolderList);
	}

	public static String getFolderTagStart(BulletinFolder folder)
	{
		String attributeNames[] = {MartusClientXml.attrFolderName, MartusClientXml.attrFolderClosed};
		String attributeValues[] = { folder.getName(), Boolean.toString(folder.isClosed())};
		return MartusXml.getTagStart(
			MartusClientXml.tagFolder,
			attributeNames,
			attributeValues);
	}

	public static String getFolderTagEnd()
	{
		return MartusXml.getTagEnd(MartusClientXml.tagFolder);
	}

	public final static String attrBulletinId = "id";

	public final static String tagFolderList = "FolderList";

	public final static String tagFolder = "Folder";
	public final static String attrFolderName = "name";
	public final static String attrFolderClosed = "closed";

	public final static String tagId = "Id";
}
