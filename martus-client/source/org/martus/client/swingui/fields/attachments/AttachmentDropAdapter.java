/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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
package org.martus.client.swingui.fields.attachments;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.List;

import org.martus.common.bulletin.AttachmentProxy;

class AttachmentDropAdapter implements DropTargetListener
{
	public AttachmentDropAdapter(UiAttachmentEditor editorToUse)
	{
		editor = editorToUse;
	}
	
	public void dragEnter(DropTargetDragEvent dtde)
	{
	}

	public void dragOver(DropTargetDragEvent dtde)
	{
		if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			dtde.acceptDrag(dtde.getDropAction());
		else
			dtde.rejectDrag();
	}

	public void dropActionChanged(DropTargetDragEvent dtde)
	{
	}

	public void drop(DropTargetDropEvent dtde)
	{
		if(!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			dtde.rejectDrop();
			return;
		}

		dtde.acceptDrop(dtde.getDropAction());
		Transferable t = dtde.getTransferable();
		List list = null;
		try
		{
			list = (List)t.getTransferData(DataFlavor.javaFileListFlavor);
		}
		catch(Exception e)
		{
			System.out.println("dropFile exception: " + e);
			dtde.dropComplete(false);
			return;
		}

		if(list.size() == 0)
		{
			System.out.println("dropFile: list empty");
			dtde.dropComplete(false);
			return;
		}

		for(int i = 0; i<list.size(); ++i)
		{	
			File file = (File)list.get(i);
			AttachmentProxy a = new AttachmentProxy(file);
			editor.addAttachment(a);
		}
		dtde.dropComplete(true);
	}

	public void dragExit(DropTargetEvent dte)
	{
	}
	
	UiAttachmentEditor editor;
}