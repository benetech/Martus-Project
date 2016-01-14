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

import java.awt.Color;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiCheckBox;
import org.martus.swing.UiTextField;

public class UiBoolEditor extends UiField implements ChangeListener
{
	public UiBoolEditor(MiniLocalization localizationToUse)
	{
		super(localizationToUse);
		widget = new UiCheckBox("");
		widget.addChangeListener(this);
		UiTextField tmp = new UiTextField();
		widget.setBackground(tmp.getBackground());
		widget.setForeground(tmp.getForeground());
		widget.setBorderPainted(true);
		widget.setBorder(new LineBorder(Color.BLACK, 1));
		widget.setHorizontalAlignment(JCheckBox.CENTER);
	}
	
	public void setListener(ChangeListener listener)
	{
		observer = listener;
	}

	public JComponent getComponent()
	{
		return widget;
	}

	public JComponent[] getFocusableComponents()
	{
		return new JComponent[]{widget};
	}

	public String getText()
	{
		if(widget.isSelected())
			return FieldSpec.TRUESTRING;

		return FieldSpec.FALSESTRING;
	}

	public void setText(String newText)
	{
		boolean selected = (newText.equals(FieldSpec.TRUESTRING));
		widget.setSelected(selected);
	}

	public void stateChanged(ChangeEvent event)
	{
		if(observer != null)
			observer.stateChanged(event);
	}

	JCheckBox widget;
	ChangeListener observer;
}

