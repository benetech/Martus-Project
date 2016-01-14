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

package org.martus.client.swingui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.border.LineBorder;

import org.martus.swing.UiLabel;

public class UiWarningLabel extends UiLabel
{
	public UiWarningLabel()
	{
		setHorizontalTextPosition(LEFT);
		setVerticalTextPosition(TOP);
		setFont(getFont().deriveFont(Font.BOLD));
		setForeground(Color.black);
		setOpaque(true);
		setBorder(new LineBorder(Color.black, 2));
	}
	
	public void setText()
	{
		throw new RuntimeException("Raw setText not supported by UiWarningLabel");
	}
	
	public void setWarningText(String text)
	{
		setBackground(WARNING_BACKGROUND);
		super.setText("   " + text + "   ");
	}

	public void setInformationalText(String text)
	{
		setBackground(INFORMATIONAL_BACKGROUND);
		super.setText("   " + text + "   ");
	}

	private static final Color WARNING_BACKGROUND = Color.yellow;
	private static final Color INFORMATIONAL_BACKGROUND = Color.green.brighter().brighter();
}
