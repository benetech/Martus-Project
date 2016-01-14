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

package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.Border;

import org.martus.swing.UiLabel;
import org.martus.swing.Utilities;

public class UiModelessBusyDlg extends JDialog
{
	public UiModelessBusyDlg(Icon icon)
	{
		showDlg(new JLabel(icon, JLabel.CENTER));
	}
	
	public UiModelessBusyDlg(String message)
	{
		getContentPane().add(new UiLabel(" "), BorderLayout.NORTH);
		getContentPane().add(new UiLabel(" "), BorderLayout.SOUTH);
		getContentPane().add(new UiLabel("     "), BorderLayout.EAST);
		getContentPane().add(new UiLabel("     "), BorderLayout.WEST);
		showDlg(new UiLabel(message));
	}

	public void showDlg(JComponent displayItem)
	{
		Border blackBorder = BorderFactory.createLineBorder(Color.black, 5);
		int vPadding = 20;
		int hPadding = vPadding * 2;
		Border emptyBorder = BorderFactory.createEmptyBorder(vPadding, hPadding, vPadding, hPadding);
		Border compoundBorder = BorderFactory.createCompoundBorder(blackBorder, emptyBorder);
		getRootPane().setBorder(compoundBorder);
		getContentPane().add(displayItem, BorderLayout.CENTER);
		setUndecorated(true);
		Utilities.packAndCenterWindow(this);
		setResizable(false);
		origCursor = getCursor();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setVisible(true);
	}

	public void endDialog()
	{
		setCursor(origCursor);
		dispose();
	}

	Cursor origCursor;
}
