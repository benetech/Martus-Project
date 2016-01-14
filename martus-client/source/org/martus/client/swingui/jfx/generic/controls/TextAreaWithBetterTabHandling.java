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

import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;

import com.sun.javafx.scene.control.behavior.TextAreaBehavior;
import com.sun.javafx.scene.control.skin.TextAreaSkin;

public class TextAreaWithBetterTabHandling extends TextArea
{
	public TextAreaWithBetterTabHandling()
	{
		// NOTE: KEY_TYPED would be more appropriate for handling TAB, but 
		// for whatever reason JavaFX actually advances to the next field 
		// as soon as TAB is pressed. So we will play along. 
		// See: com.sun.javafx.scene.control.behavior.TextAreaBehavior.TEXT_AREA_BINDINGS
		addEventFilter(KeyEvent.KEY_PRESSED, new KeyTypedEventHandler());
	}

	class KeyTypedEventHandler implements EventHandler<KeyEvent>
	{
		public void handle(KeyEvent event)
		{
			String keyCode = event.getText();
			if(keyCode.equals("\t"))
			{
				onTabTyped(event);
			}
		}

		private void onTabTyped(KeyEvent event)
		{
			// NOTE: JavaFX already handles Shift-TAB correctly 
			if(event.isAltDown() || event.isControlDown() || event.isShiftDown())
				return;
			
			TextArea targetNode = (TextArea)event.getTarget();
			TextAreaSkin skin = (TextAreaSkin)targetNode.getSkin();
			TextAreaBehavior behavior = skin.getBehavior();
			behavior.callAction("TraverseNext");
			event.consume();
		}
	}

}
