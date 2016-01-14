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

import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.martus.clientside.UiLocalization;
import org.martus.swing.UiTextArea;

public class UiMultilineViewer extends UiStringField
{
	public UiMultilineViewer(UiLocalization localizationToUse, int numberColumns)
	{
		super(localizationToUse);
		text = new PreviewTextArea(1, numberColumns);
		text.setEditable(false);
		supportContextMenu();
	}
	
	@Override
	public JTextComponent getTextComponent()
	{
		return text;
	}

	public JComponent getComponent()
	{
		return text;
	}

	public JTextComponent getEditor()
	{
		return text;
	}

	public String getText()
	{
		return "";
	}

	public void setText(String newText)
	{
		text.setText(newText);
	}

	public JComponent[] getFocusableComponents()
	{
		return new JComponent[0];
	}

	class PreviewTextArea extends UiTextArea
	{
		PreviewTextArea(int rows, int cols)
		{
			super(rows, cols);
			setLineWrap(true);
			setWrapStyleWord(true);
			setAutoscrolls(false);
			setEditable(false);
		}

		// overridden ONLY because setting the text to a new
		// value was causing the nearest enclosing scroll pane
		// to jump to this field
		public void scrollRectToVisible(Rectangle rect)
		{
			// do nothing!
		}

	}

	UiTextArea text;
}

