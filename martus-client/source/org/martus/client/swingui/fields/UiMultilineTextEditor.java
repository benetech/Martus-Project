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

*/

package org.martus.client.swingui.fields;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.martus.client.swingui.MartusLocalization;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTextArea;

public class UiMultilineTextEditor extends UiStringField
{
	public UiMultilineTextEditor(MartusLocalization localizationToUse, int numberColumns)
	{
		super(localizationToUse);
		editor = new UiTextArea(5, numberColumns);
		editor.setLineWrap(true);
		editor.setWrapStyleWord(true);
		editor.addKeyListener(new myKeyListener());

		widget = new UiScrollPane(editor, UiScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				UiScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		widget.getVerticalScrollBar().setFocusable(false);
		supportContextMenu();
	}

	public JComponent getComponent()
	{
		return widget;
	}

	class myKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
		}
		public void keyReleased(KeyEvent e)
		{
		}
		public void keyTyped(KeyEvent e)
		{
			editor.repaint(); //Java Bug to fix Arabic subscripts getting clipped.
		}
	}

	public JComponent[] getFocusableComponents()
	{
		return new JComponent[]{editor};
	}

	public JTextComponent getEditor()
	{
		return editor;
	}

	public String getText()
	{
		return editor.getText();
	}

	public void setText(String newText)
	{
		editor.setText(newText);
		editor.updateUI(); //Resets view position to top of scroll pane
	}
	
	public JTextComponent getTextComponent() 
	{
		return editor;
	};

	UiScrollPane widget;
	UiTextArea editor;
}

