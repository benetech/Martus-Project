/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.martus.clientside.UiLocalization;
import org.martus.swing.UiTextField;

abstract public class UiSingleLineTextField extends UiStringField
{
	public UiSingleLineTextField(UiLocalization localizationToUse)
	{
		super(localizationToUse);
	}
	
	@Override
	public JTextComponent getTextComponent()
	{
		return widget;
	}
	
	public JComponent getComponent()
	{
		return widget;
	}

	public JTextComponent getEditor()
	{
		return widget;
	}

	public String getText()
	{
		return widget.getText();
	}

	public void setText(String newText)
	{
		widget.setText(newText);
	}

	UiTextField widget;
}
