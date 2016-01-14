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

import java.io.File;

import javax.swing.JComponent;

import org.martus.client.core.BulletinLanguageChangeListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiEditableFieldCreator;
import org.martus.client.swingui.fields.UiField;
import org.martus.client.swingui.fields.attachments.UiAttachmentEditor;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.fieldspec.FieldSpec;

public class UiBulletinComponentEditorSection extends UiBulletinComponentDataSection
{

	public UiBulletinComponentEditorSection(UiMainWindow ownerToUse, String sectionName)
	{
		super(ownerToUse, sectionName);
		setFieldCreator(new UiEditableFieldCreator(ownerToUse, getContext()));
	}

	public void addAttachment(AttachmentProxy a)
	{
		attachmentEditor.addAttachment(a);
	}

	public void clearAttachments()
	{
		attachmentEditor.clearAttachments();
	}

	public JComponent createAttachmentTable()
	{
		attachmentEditor = new UiAttachmentEditor(getMainWindow());
		return attachmentEditor;
	}

	public void validateAttachments() throws AttachmentMissingException
	{
		AttachmentProxy[] publicAttachments = attachmentEditor.getAttachments();
		for(int aIndex = 0; aIndex < publicAttachments.length; ++aIndex)
		{
			File file = publicAttachments[aIndex].getFile();
			if (file != null)
			{
				if(!file.exists())
					throw new AttachmentMissingException(file.getAbsolutePath());
			}
		}
	}

	UiField createField(FieldSpec spec, BulletinLanguageChangeListener listener) 
	{
		UiField newField = super.createField(spec, listener);
		
		if(spec.getType().isLanguageDropdown())
			newField.setBulletinLanguageListener(listener);
		return newField;
	}


	UiAttachmentEditor attachmentEditor;
}
