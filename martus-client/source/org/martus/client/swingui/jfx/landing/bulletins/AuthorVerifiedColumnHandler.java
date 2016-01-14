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

import org.martus.common.ContactKey;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;


public class AuthorVerifiedColumnHandler implements Callback<TableColumn<BulletinTableRowData, Integer>, TableCell<BulletinTableRowData, Integer>>
{
	public AuthorVerifiedColumnHandler()
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
		    		Integer verified = (Integer)item;
		    		Image image = getVerificationImage(verified);
		    		final Node statusCell = new ImageView(image);
	    			setGraphic(statusCell);
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
	
	static public Image getVerificationImage(Integer verified)
	{
		Image image = null;
		if(verified == ContactKey.VERIFIED_ACCOUNT_OWNER ||
		   verified == ContactKey.VERIFIED_VISUALLY ||
		   verified == ContactKey.VERIFIED_ENTERED_20_DIGITS)
			image = new Image(IMAGE_CONTACT_VERIFIED_PATH);
		else if(verified == ContactKey.NOT_VERIFIED)
		image = new Image(IMAGE_CONTACT_NOT_VERIFIED_PATH);
		else
		image = new Image(IMAGE_CONTACT_UNKNOWN_PATH);
		return image;
	}

	static public final String IMAGE_CONTACT_VERIFIED_PATH = "/org/martus/client/swingui/jfx/images/contact_verified.png";
	static public final String IMAGE_CONTACT_NOT_VERIFIED_PATH = "/org/martus/client/swingui/jfx/images/contact_not_verified.png";
	static public final String IMAGE_CONTACT_UNKNOWN_PATH = "/org/martus/client/swingui/jfx/images/contact_unknown.png";
	
}	


