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
package org.martus.client.swingui.jfx.generic;

import java.awt.Dialog;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.martus.client.swingui.WindowObscurer;

public class FxInSwingModalDialog extends JDialog
{
	public FxInSwingModalDialog()
	{
		// NOTE: Pass (Dialog)null to force this window to show up in the Task Bar
		super((Dialog)null);
		
		initialize();
	}
	
	public FxInSwingModalDialog(JFrame owner)
	{
		super(owner);

		initialize();
	}

	public void initialize()
	{
		setModal(true);
		setGlassPane(new WindowObscurer());
	}
	
	public static final Dimension MEDIUM_SMALL_PREFERRED_DIALOG_SIZE = new Dimension(650, 250);
	public static final Dimension MEDIUM_PREFERRED_DIALOG_SIZE = new Dimension(650, 450);
	public static String EMPTY_TITLE = "";
}
