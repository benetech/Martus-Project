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

package org.martus.client.swingui.fields.attachments;

import java.awt.Component;
import java.awt.dnd.DropTarget;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.martus.client.swingui.UiFocusListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.swing.UiButton;
import org.martus.swing.Utilities;



public class UiAttachmentEditor extends UiAttachmentComponent
{
	public UiAttachmentEditor(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		updateTable();
		new DropTarget(this, new AttachmentDropAdapter(this));
	}

	AbstractAttachmentPanel createAttachmentPanel(int row)
	{
		return new EditAttachmentPanel(mainWindow, this, model.getAttachment(row));
	}

	public AttachmentProxy[] getAttachments()
	{
		return model.getAttachments();
	}

	JComponent createAttachmentFooter()
	{
		JButton add = new UiButton(getLocalization().getButtonLabel("addattachment"));
		add.addFocusListener(new UiFocusListener(this));		
		add.addActionListener(new AddHandler(mainWindow, model, this));
		
		Box buttonBox = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(buttonBox, new Component[] {add, Box.createHorizontalGlue()});
		return buttonBox;
	}
}

