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
/* 
 Original code from 
 http://stackoverflow.com/questions/17467137/how-can-i-create-a-switch-button-in-javafx
*/
package org.martus.client.swingui.jfx.generic.controls;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.effect.InnerShadow;

public class FxSwitchButton extends Label
{
	   public FxSwitchButton()
	    {
	        Button switchButton = new Button();
	        switchButton.setStyle("-fx-background-color: lightgray; -fx-background-insets: 1; -fx-background-radius: 5;");
	        switchButton.setMinWidth(30);
	        switchButton.setPrefWidth(30);
	        InnerShadow shadow = new InnerShadow();
	        switchButton.setEffect(shadow);
	        switchButton.setOnAction(new ButtonEventHandler());
	        switchButton.setGraphicTextGap(0.2);
	        setGraphic(switchButton);
	        switchedOn.addListener(new ButtonPressedChangeListener());
	        displayOffPosition();
	    }

	    public SimpleBooleanProperty switchOnProperty() 
	    { 
	    		return switchedOn; 
	    	}
	    
	    public void setSelected(boolean isSelected)
	    {
	    		switchedOn.set(isSelected);
	    }
	    
	    public boolean isSelected()
	    {
	    		return switchedOn.get();
	    }
	    
	
    private final class ButtonEventHandler implements EventHandler<ActionEvent>
	{
		public ButtonEventHandler()
		{
		}

		@Override
		public void handle(ActionEvent t)
		{
		    switchedOn.set(!switchedOn.get());
		}
	}

	private final class ButtonPressedChangeListener implements ChangeListener<Boolean>
	{
		public ButtonPressedChangeListener()
		{
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> oldValue, Boolean currentValue, Boolean newValue)
		{
		    if (newValue)
		        displayOnPosition();
		    else
		        displayOffPosition();
		}
	}

	protected void displayOnPosition()
	{
		setText(" ON");
		setStyle("-fx-background-color: green;-fx-text-fill:darkgray;  -fx-background-radius: 10;");
		setContentDisplay(ContentDisplay.RIGHT);
	}

	protected void displayOffPosition()
	{
		InnerShadow shadow = new InnerShadow();
		setEffect(shadow);
		setText("      ");
		setStyle("-fx-background-color: #e6e7e9;-fx-text-fill:darkgray; -fx-background-radius: 10;");
		setContentDisplay(ContentDisplay.LEFT);
	}
 
    SimpleBooleanProperty switchedOn = new SimpleBooleanProperty(false);
}