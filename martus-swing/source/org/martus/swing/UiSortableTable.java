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

package org.martus.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import org.martus.util.SortableTableModel;

public class UiSortableTable extends UiTable 
{
	public UiSortableTable(SortableTableModel model)
	{
		super(model);
		getTableHeader().addMouseListener(new SortColumnListener(this));
	}
	
	public SortableTableModel getSortableTableModel()
	{
		return (SortableTableModel)getModel();
	}
	
	class SortColumnListener extends MouseAdapter
	{
		SortColumnListener (UiSortableTable tableToUse)
		{
			table = tableToUse;
		}
		
		public void mouseClicked(MouseEvent e) 
		{
		     int columnToSort = table.getColumnModel().getColumnIndexAtX(e.getX());
		     sortTable(columnToSort);
		}

		private void sortTable(int columnToSort) 
		{
			Vector newIndexes = getNewSortedOrderOfRows(columnToSort);
			SortableTableModel model = table.getSortableTableModel();
			model.setSortedRowIndexes(newIndexes);
			tableChanged(new TableModelEvent(model));
		}

		private Vector getNewSortedOrderOfRows(int columnToSort) 
		{
		    sortingOrder = -sortingOrder;
			return getNewSortedOrderOfRows(table.getSortableTableModel(), columnToSort);
		}
		
		private synchronized Vector getNewSortedOrderOfRows(SortableTableModel model, int columnToSort)
		{
			class Sorter implements Comparator
			{
				public Sorter(SortableTableModel modelToUse, int column, int sortDirection)
				{
					tableModel = modelToUse;
					columnToSortOn = column;
					sorterDirection = sortDirection;
				}
				public int compare(Object o1, Object o2)
				{
					Comparable obj1 = (Comparable)tableModel.getValueAtDirect(((Integer)(o1)).intValue(), columnToSortOn);
					Comparable obj2 = (Comparable)tableModel.getValueAtDirect(((Integer)(o2)).intValue(), columnToSortOn);
					return obj1.compareTo(obj2) * sorterDirection;
				}
				SortableTableModel tableModel; 
				int columnToSortOn;
				int sorterDirection;
			}

			Vector sortedRowIndexes = new Vector();
			for(int i = 0; i < model.getRowCount(); ++i)
				sortedRowIndexes.add(new Integer(i));

			Collections.sort(sortedRowIndexes, new Sorter(model, columnToSort, sortingOrder));
			return sortedRowIndexes;
		}

		UiSortableTable table;
		int sortingOrder = 1; 
	}
	
}
