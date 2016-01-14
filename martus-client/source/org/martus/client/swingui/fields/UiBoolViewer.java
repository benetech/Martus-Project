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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import org.martus.common.EnglishCommonStrings;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiLabel;

public class UiBoolViewer extends UiViewerField
{
	public UiBoolViewer(MiniLocalization localizationToUse)
	{
		super(localizationToUse);
		widget = new UiLabel();
		widget.setBorder(new LineBorder(Color.black));
	}

	public JComponent getComponent()
	{
		return widget;
	}

	public String getText()
	{
		return "";
	}

	public void setText(String newText)
	{
		String text = "";
		if(newText.equals(FieldSpec.TRUESTRING))
			text = getLocalization().getButtonLabel(EnglishCommonStrings.YES);
		else
			text = getLocalization().getButtonLabel(EnglishCommonStrings.NO);

		widget.setText(" " + text + " ");
	}

	JLabel widget;
}

