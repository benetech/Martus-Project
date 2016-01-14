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
package org.martus.client.swingui.tablemodels;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.UniversalId;


public class AttachmentTableModel extends AbstractTableModel
{
	public AttachmentTableModel(UiMainWindow window)
	{
		attachmentList = new Vector();
		mainWindow = window;
	}

	public int getRowCount()
	{
		return attachmentList.size();
	}

	public int getColumnCount()
	{
		return 2;
	}

	public void clear()
	{
		attachmentList.clear();
		fireTableDataChanged();
	}

	public void add(AttachmentProxy a)
	{
		attachmentList.add(a);
		fireTableDataChanged();
	}

	public void remove(int row)
	{
		attachmentList.remove(row);
		fireTableDataChanged();
	}

	public String getColumnName(int column)
	{
		if(column == 0)
			return getLocalization().getButtonLabel("attachmentLabel");
		return getLocalization().getButtonLabel("attachmentSize");
	}

	public AttachmentProxy getAttachmentProxyAt(int row)
	{
		return (AttachmentProxy)attachmentList.get(row);
	}
		
	public AttachmentProxy[] getAttachments(int[] rows)
	{
		if(rows.length <= 0)
			return null;
		AttachmentProxy[] list = new AttachmentProxy[rows.length];
		for(int i = 0; i < rows.length; ++i)
			list[i] = getAttachment(i);
		return list;
	}

	public AttachmentProxy[] getAttachments()
	{
		AttachmentProxy[] list = new AttachmentProxy[attachmentList.size()];
		for(int i = 0; i < list.length; ++i)
			list[i] = getAttachment(i);
	
		return list;
	}
	
	public AttachmentProxy getAttachment(int row)
	{
		return (AttachmentProxy)attachmentList.get(row);
	}

	public String getFilenameAt(int row)
	{
		return (String)getValueAt(row, 0);
	}

	public Object getValueAt(int row, int column)
	{
		AttachmentProxy a = (AttachmentProxy)attachmentList.get(row);
		if(column == 0)
			return a.getLabel();

		return getSize(a, mainWindow.getStore().getDatabase());
		
	}

	static public String getSize(AttachmentProxy a, ReadableDatabase database)
	{
		if(a.getFile() != null)
		{	
			int size = (int)a.getFile().length();
			return getSizeInKb(size);
		}
		
		AttachmentPacket packet = a.getPendingPacket(); 
		if(packet != null)
		{
			return getSizeInKb(packet.getFileSize());
		}
		
		int size = 0;
		UniversalId id = a.getUniversalId();
		try
		{
			DatabaseKey key = DatabaseKey.createMutableKey(id);
			if(!database.doesRecordExist(key))
				key = DatabaseKey.createImmutableKey(id);
			if(!database.doesRecordExist(key))
				return "";
			size = database.getRecordSize(key);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		size -= 1024;//Public code & overhead
		size = size * 3 / 4;//Base64 overhead
		return getSizeInKb(size);
	}

	static private String getSizeInKb(long sizeBytes)
	{
		long sizeInKb = sizeBytes / 1024;
		if (sizeInKb == 0)
			sizeInKb = 1;
		return Integer.toString((int)sizeInKb);
	}

	public void setValueAt(Object value, int row, int column)
	{
	}

	public boolean isCellEditable(int row, int column)
	{
		return false;
	}
	
	private MartusLocalization getLocalization()
	{
		return mainWindow.getLocalization();
	}


	Vector attachmentList;
	UiMainWindow mainWindow;
}
