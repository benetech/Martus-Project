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

package org.martus.client.swingui.bulletincomponent;

import javax.swing.JComponent;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiReadOnlyFieldCreator;
import org.martus.client.swingui.fields.attachments.UiAttachmentViewer;
import org.martus.common.bulletin.AttachmentProxy;

public class UiBulletinComponentViewSection extends UiBulletinComponentDataSection
{

	public UiBulletinComponentViewSection(UiMainWindow ownerToUse, String sectionName)
	{
		super(ownerToUse, sectionName);
		setFieldCreator(new UiReadOnlyFieldCreator(ownerToUse, getContext()));
	}

	public JComponent createAttachmentTable()
	{
		attachmentViewer = new UiAttachmentViewer(getMainWindow());
		return attachmentViewer;
	}

	public void addAttachment(AttachmentProxy a)
	{
		attachmentViewer.addAttachment(a);
	}

	public void clearAttachments()
	{
		attachmentViewer.clearAttachments();
	}

	public void validateAttachments()
	{
		// read-only view can't have invalid attachments
	}
	

	public UiAttachmentViewer attachmentViewer;
}
