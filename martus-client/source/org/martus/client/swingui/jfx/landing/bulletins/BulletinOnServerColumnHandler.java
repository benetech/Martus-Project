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

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;


public class BulletinOnServerColumnHandler implements Callback<TableColumn<BulletinTableRowData, Boolean>, TableCell<BulletinTableRowData, Boolean>>
{
	public BulletinOnServerColumnHandler()
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
		    		Boolean onServer = (Boolean)item;
		    		if(onServer)
		    		{
		    			Image image = new Image(ON_SERVER_IMAGE_PATH);
			    		final Node statusCell = new ImageView(image);
		    			setGraphic(statusCell);
				}
		    		else
		    		{
		    			Image image = new Image(NOT_ON_SERVER_IMAGE_PATH);
			    		final Node statusCell = new ImageView(image);
		    			setGraphic(statusCell);
		    		}
	    			setAlignment(Pos.CENTER);
		    	}
		}
		protected final TableColumn tableColumn;
	}

	@Override
	public TableCell call(final TableColumn param) 
	{
		return new TableCellUpdateHandler(param);
	}	
	
	private final String ON_SERVER_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/on_server.png";
	private final String NOT_ON_SERVER_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/not_on_server.png";
}	


