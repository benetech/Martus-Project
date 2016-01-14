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
package org.martus.client.swingui.jfx.landing.bulletins;

import java.io.File;

import javafx.application.Platform;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;

public class AddAttachmentAction implements ActionDoer
{
	public AddAttachmentAction(BulletinAttachmentsController bulletinAttachmentsContollerToUse)
	{
		bulletinAttachmentsContoller = bulletinAttachmentsContollerToUse;
	}

	@Override
	public void doAction()
	{
		File fileToAdd = getMainWindow().showFileOpenDialogWithDirectoryMemory("AddAttachment");
		if(fileToAdd == null)
			return;
		
		Platform.runLater(new AddAttachmentWorker(fileToAdd));
	}
	
	private UiMainWindow getMainWindow()
	{
		return getBulletinAttachmentsContoller().getMainWindow();
	}

	protected BulletinAttachmentsController getBulletinAttachmentsContoller()
	{
		return bulletinAttachmentsContoller;
	}
	
	protected class AddAttachmentWorker implements Runnable
	{
		public AddAttachmentWorker(File attachmentFileToUse)
		{
			attachmentFile = attachmentFileToUse;
		}
		
		@Override
		public void run()
		{
			getBulletinAttachmentsContoller().addAttachment(attachmentFile);
		}
		
		private File attachmentFile;
	}

	private BulletinAttachmentsController bulletinAttachmentsContoller;
}
