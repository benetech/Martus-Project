/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2013, Beneficent
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

import java.util.Iterator;

import org.martus.client.core.MartusApp;
import org.martus.common.FieldDeskKey;
import org.martus.common.FieldDeskKeys;

public class FieldDeskManagementTableModel extends ExternalPublicKeysTableModel
{
	public FieldDeskManagementTableModel(MartusApp app)
	{
		super(app);
	}
	
	public int getColumnCount()
	{
		return 2;
	}
	
	public int getPublicCodeColumnIndex()
	{
		return COLUMN_PUBLIC_CODE;
	}
	
	public int getLabelColumnIndex()
	{
		return COLUMN_LABEL;
	}

	public boolean isEnabled(int row)
	{
		return true;
	}

	public void addNewFieldDeskEntry(SelectableFieldDeskEntry entryToAdd)
	{
		addRawEntry(entryToAdd);
	}
	
	public FieldDeskKeys getAllKeys()
	{
		FieldDeskKeys keys = new FieldDeskKeys();
		for (Iterator iter = getRawEntries().iterator(); iter.hasNext();) 
		{
			keys.add(((SelectableFieldDeskEntry) iter.next()).getKey());
		}	
		return keys;
	}
	
	public FieldDeskKey getFieldDeskKey(int row)
	{
		return (FieldDeskKey) getRawEntry(row).getKey();
	}

	public static final int COLUMN_PUBLIC_CODE = 0;
	public static final int COLUMN_LABEL = 1;
}
