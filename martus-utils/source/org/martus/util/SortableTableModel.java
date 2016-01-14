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

To the extent this copyrighted software code is used in the 
Miradi project, it is subject to a royalty-free license to 
members of the Conservation Measures Partnership when 
used with the Miradi software as specified in the agreement 
between Benetech and WCS dated 5/1/05.
*/

package org.martus.util;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
abstract public class SortableTableModel extends AbstractTableModel
{
	public abstract Object getValueAtDirect(int rowIndex, int columnIndex);

	public Object getValueAt(int rowIndex, int columnIndex) 
	{
		int sortedRowIndex = getSortedRowIndex(rowIndex);
		return getValueAtDirect(sortedRowIndex, columnIndex);
	}

	public int getSortedRowIndex(int rowIndex)
	{
		if(sortedRowIndexes.isEmpty())
			return rowIndex;
		return ((Integer)sortedRowIndexes.get(new Integer(rowIndex))).intValue();
	}
	
	public void setSortedRowIndexes(Vector newIndexes)
	{
		clearSortedOrder();
		for(int i = 0; i < getRowCount(); ++i)
		{
			sortedRowIndexes.put(new Integer(i), newIndexes.get(i));
		}
	}
	
	public void clearSortedOrder()
	{
		sortedRowIndexes.clear();
	}
	
	HashMap sortedRowIndexes = new HashMap();
}
