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
package org.martus.client.swingui.jfx.generic.controls;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.util.Callback;

import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.jfx.generic.TableRowData;

public class FxImageTableCellFactory implements Callback<TableColumn<TableRowData, ReadOnlyProperty<Image>>, TableCell<TableRowData, ReadOnlyProperty<Image>>>
{
	public static FxImageTableCellFactory createNormalImageTableCellFactory(ActionDoer doerToUse)
	{
		return new FxImageTableCellFactory(doerToUse);
	}

	private FxImageTableCellFactory(ActionDoer doerToUse)
	{
		doer = doerToUse;
	}
	
	@Override
	public TableCell call(final TableColumn param) 
	{
		return FxImageTableCell.createNormalImageTableCell(doer);
	}
	
	private ActionDoer doer;
}
