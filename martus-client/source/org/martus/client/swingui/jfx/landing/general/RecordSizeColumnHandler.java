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
package org.martus.client.swingui.jfx.landing.general;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;


public class RecordSizeColumnHandler implements Callback<TableColumn<ServerSyncTableRowData, Integer>, TableCell<ServerSyncTableRowData, Integer>>
{
	public RecordSizeColumnHandler()
	{
		super();
	}
	
	final class TableCellUpdateHandler extends TableCell
	{
		
		TableCellUpdateHandler(TableColumn tableColumn)
		{
			this.tableColumn = tableColumn;
		}
		
		@Override
		public void updateItem(Object item, boolean empty) 
		{
		    super.updateItem(item, empty);
		    if (empty) 
		    {
		        setText(null);
		        setGraphic(null);
		    } 
		    else 
		    {
		    		Integer size = (Integer)item;
		    		setText(size.toString());
		    	}
		}
		protected final TableColumn tableColumn;
	}

	@Override
	public TableCell call(final TableColumn param) 
	{
		return new TableCellUpdateHandler(param);
	}	
	
}	


