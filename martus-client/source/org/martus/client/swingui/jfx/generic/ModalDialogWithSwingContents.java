/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2015, Beneficent
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
package org.martus.client.swingui.jfx.generic;

import javax.swing.JDialog;

import org.martus.swing.UiButton;
import org.martus.swing.Utilities;

public class ModalDialogWithSwingContents extends JDialog
{
	public static void show(SwingDialogContentPane contents)
	{
		JDialog dialog = new ModalDialogWithSwingContents(contents);
		dialog.setVisible(true);
	}

	private ModalDialogWithSwingContents(SwingDialogContentPane contents)
	{
		super(contents.getMainWindow().getCurrentActiveFrame().getSwingFrame());
		contents.addIsActiveListener((property, oldValue, newValue) -> dispose());
		contents.addDefaultButtonListener((property, oldButton, newButton) -> setDefaultButton(newButton));
		setTitle(contents.getTitle());
		getContentPane().add(contents);
		Utilities.packAndCenterWindow(this);
		setModal(true);
		setResizable(true);
	}

	private void setDefaultButton(UiButton newDefaultButton)
	{
		getRootPane().setDefaultButton(newDefaultButton);
	}
}