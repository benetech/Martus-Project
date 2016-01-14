/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.client.swingui;

import java.util.Iterator;

import org.martus.client.core.MartusApp;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;

public abstract class HeadquartersTableModel extends ExternalPublicKeysTableModel 
{
	public HeadquartersTableModel(MartusApp app)
	{
		super(app);
	}
	
	public void setHQSelectionListener(HeadquartersSelectionListener selectionListenerToUse)
	{
		selectionListener = selectionListenerToUse;
	}
	
	public void addNewHeadQuarterEntry(SelectableHeadquartersEntry entryToAdd)
	{
		addRawEntry(entryToAdd);
	}
	
	public void addKeys(HeadquartersKeys keys)
	{
		for(int j = 0; j < keys.size(); ++j)
		{
			HeadquartersKey hqKeyToCheck = keys.get(j);
			SelectableHeadquartersEntry headQuarterEntry = new SelectableHeadquartersEntry(hqKeyToCheck);
			if(!contains(headQuarterEntry))
				addNewHeadQuarterEntry(headQuarterEntry);
		}
	}

	public void selectKeys(HeadquartersKeys keys)
	{
		for(int row = 0; row < getRowCount(); ++row)
		{
			if(keys.contains(getHQKey(row)))
				selectRow(row);
		}
	}
	
	public int getNumberOfSelectedHQs()
	{
		return getAllSelectedHeadQuarterKeys().size();
	}

	public HeadquartersKeys getAllSelectedHeadQuarterKeys()
	{
		HeadquartersKeys keys = new HeadquartersKeys();
		for (Iterator iter = getRawEntries().iterator(); iter.hasNext();) 
		{
			SelectableHeadquartersEntry hqEntry = (SelectableHeadquartersEntry) iter.next();
			if(hqEntry.isSelected())
				keys.add(hqEntry.getKey());
		}
		return keys;
	}
	
	public HeadquartersKeys getAllKeys()
	{
		HeadquartersKeys keys = new HeadquartersKeys();
		for (Iterator iter = getRawEntries().iterator(); iter.hasNext();) 
		{
			keys.add(((SelectableHeadquartersEntry) iter.next()).getKey());
		}	
		return keys;
	}
	
	public HeadquartersKey getHQKey(int row)
	{
		return (HeadquartersKey) getRawEntry(row).getKey();
	}

	public int getDefaultEnabledColumnIndex()
	{
		return -1;
	}
	
	public int getIsSelectedColumnIndex()
	{
		return -1;
	}

	public String getColumnName(int column)
	{
		if(column == getIsSelectedColumnIndex())
			return getLocalization().getFieldLabel("HeadQuartersSelected");
		if(column == getDefaultEnabledColumnIndex())
			return getLocalization().getFieldLabel("ConfigureHeadQuartersDefault");

		return super.getColumnName(column);
	}

	public Object getValueAt(int row, int column)
	{
		SelectableExternalPublicKeyEntry entry = getRawEntry(row);
		if(column == getIsSelectedColumnIndex())
			return new Boolean(entry.isSelected());
		if(column == getDefaultEnabledColumnIndex())
			return new Boolean(entry.isSelected());

		return super.getValueAt(row, column);
	}
	
	public void setValueAt(Object value, int row, int column)
	{
		SelectableExternalPublicKeyEntry entry = getRawEntry(row);
		if(column == getIsSelectedColumnIndex() || column == getDefaultEnabledColumnIndex())
		{
			entry.setSelected(((Boolean)value).booleanValue());
			if(selectionListener != null)
				selectionListener.selectedHQsChanged(getNumberOfSelectedHQs());
		}
	}
	
	public Class getColumnClass(int column)
	{
		if(column == getIsSelectedColumnIndex())
			return Boolean.class;
		if(column == getDefaultEnabledColumnIndex())
			return Boolean.class;
		return super.getColumnClass(column);
	}

	public boolean isEnabled(int row) 
	{
		return true;
	}
	
	public boolean isCellEditable(int row, int column)
	{
		if(column == getIsSelectedColumnIndex() || column == getDefaultEnabledColumnIndex())
			return true;
		return false;
	}
	
	HeadquartersSelectionListener selectionListener;
}
