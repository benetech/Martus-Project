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
//Modified: original from http://stackoverflow.com/questions/7880494/tableview-better-editing-through-binding
	
package org.martus.client.swingui.jfx.generic;

import org.martus.client.swingui.jfx.generic.data.FxBindingHelpers;

import javafx.beans.property.Property;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;

public final class FxTableCellTextFieldFactory  
	implements Callback<TableColumn<Object,String>,TableCell<Object,String>> 
{
	
	@Override
	public TableCell<Object, String> call(TableColumn<Object, String> param) 
	{
	   TextFieldCell textFieldCell = new TextFieldCell();
	   return textFieldCell;
	}
	
	public static class TextFieldCell extends TableCell<Object,String> 
	{
		public TextFieldCell() 
		{
			textField = new TextField();
			textField.setStyle("-fx-background-color: #e6e7e9; -fx-text-fill: Black;");
			setGraphic(textField);
		}
	   
		@Override
		protected void updateItem(String item, boolean empty) 
		{
			super.updateItem(item, empty);  
			if(empty)
			{
				setContentDisplay(ContentDisplay.TEXT_ONLY);
			}
			else
			{
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				//NOTE: To use this TextField Factory the TableData's SimpleStringProperty must be implemented.  
				//		IE: public SimpleStringProperty <variableName>Property() { return <variableName>; }
				Property cellProperty = (Property)getTableColumn().getCellObservableValue(getIndex());
				Property currentFieldProperty = textField.textProperty();
				cellStringPropertyBoundToCurrently = FxBindingHelpers.bindToOurPropertyField(cellProperty, currentFieldProperty, cellStringPropertyBoundToCurrently);
			}
			textField.setMinHeight(15);
			if(item != null && item.length() > 0)
			{
				textField.setStyle("-fx-background-color: #e6e7e9; -fx-text-fill: Black;");
			}
			else
			{
				textField.setStyle("-fx-background-color: white; -fx-text-fill: Black;");
			}
		}
		
		private TextField textField;
		private Property cellStringPropertyBoundToCurrently;
	}
}
