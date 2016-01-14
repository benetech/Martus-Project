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

package org.martus.client.swingui.bulletintable;

import javax.swing.table.AbstractTableModel;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.packet.UniversalId;

public class BulletinTableModel extends AbstractTableModel
{
    public BulletinTableModel(MartusApp app)
    {
		localization = app.getLocalization();
	    fontHelper = new UiFontEncodingHelper(app.getConfigInfo().getDoZawgyiConversion());
    }

	public void setFolder(BulletinFolder folderToUse)
	{
		if(folder != null)
		{
			fireTableRowsDeleted(0, folder.getBulletinCount());
		}

		folder = folderToUse;
		fireTableRowsInserted(0, folder.getBulletinCount());
	}

	public BulletinFolder getFolder()
	{
		return folder;
	}

	public int getRowCount()
	{
		if(folder == null)
			return 0;

		return folder.getBulletinCount();
	}

	public int getColumnCount()
	{
		return sortableFieldTags.length;
	}

	public Bulletin getBulletin(int rowIndex)
	{
		return folder.getBulletinSorted(rowIndex);
	}

	public UniversalId getBulletinUid(int rowIndex)
	{
		return folder.getBulletinUniversalIdSorted(rowIndex);
	}

	public int findBulletin(UniversalId uid)
	{
		if(uid == null)
			return -1;

		return folder.find(uid);
	}

	public Object getValueAt(int rowIndex, int columnIndex)
	{
		UniversalId uid = folder.getBulletinUniversalIdSorted(rowIndex);
		if(uid == null)
			return "";
		
		String fieldTag = sortableFieldTags[columnIndex];
		ClientBulletinStore store = getFolder().getStore();
		String rawData = store.getFieldData(uid, fieldTag);
		
		if(fieldTag.equals(Bulletin.TAGSTATUS))
		 	return localization.getStatusLabel(rawData);	
		
		if(fieldTag.equals(Bulletin.PSEUDOFIELD_WAS_SENT))
		{
			if(rawData == null || rawData.length() == 0)
				return "";
			return localization.getFieldLabel(rawData);
		}
											
		if (fieldTag.equals(Bulletin.PSEUDOFIELD_LAST_SAVED_TIMESTAMP))
		{
			long dateTime = 0L;
			if(rawData.length() > 0)
				dateTime = Long.parseLong(rawData);
			return localization.formatDateTime(dateTime);
		}
		
		if (fieldTag.equals(Bulletin.TAGENTRYDATE) || 
			fieldTag.equals(Bulletin.TAGEVENTDATE))				
		{
			return localization.convertStoredDateToDisplay(rawData);
		}	
		
		if (rawData.startsWith("<"))
			return " "+rawData;				
			
		return fontHelper.getDisplayable(rawData);
	}


	public String getColumnName(int columnIndex)
	{
		return localization.getFieldLabel(getFieldName(columnIndex));
	}

	public String getFieldName(int columnIndex)
	{
		return sortableFieldTags[columnIndex];
	}

	public void sortByColumn(int columnIndex)
	{
		folder.sortBy(getFieldName(columnIndex));
	}
	
	public static final String[] sortableFieldTags =
	{
		BulletinConstants.TAGSTATUS, 
		Bulletin.PSEUDOFIELD_WAS_SENT, 
		BulletinConstants.TAGEVENTDATE, 
		BulletinConstants.TAGTITLE, 
		BulletinConstants.TAGAUTHOR, 
		Bulletin.PSEUDOFIELD_LAST_SAVED_TIMESTAMP,
	};


	MiniLocalization localization;
	BulletinFolder folder;
	UiFontEncodingHelper fontHelper;
}
