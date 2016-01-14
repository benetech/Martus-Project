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

import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.TransferableAttachmentList;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.AttachmentProxy;

class AttachmentDragHandler implements DragGestureListener, DragSourceListener
{
	public AttachmentDragHandler(ClientBulletinStore storeToUse, AttachmentProxy proxyToUse)
	{
		store = storeToUse;
		proxy = proxyToUse;
	}
	
	public void dragGestureRecognized(DragGestureEvent dge)
	{
		MartusLogger.log("Dragging: " + proxy.getLabel());
		AttachmentProxy[] attachments = new AttachmentProxy[] {proxy};
		TransferableAttachmentList dragable = new TransferableAttachmentList(store.getDatabase(), store.getSignatureVerifier(), attachments);
		dge.startDrag(DragSource.DefaultCopyDrop, dragable, this);
	}

	public void dragEnter(DragSourceDragEvent dsde)
	{
	}

	public void dragOver(DragSourceDragEvent dsde)
	{
	}

	public void dropActionChanged(DragSourceDragEvent dsde)
	{
	}

	public void dragDropEnd(DragSourceDropEvent dsde)
	{
	}

	public void dragExit(DragSourceEvent dse)
	{
	}
	
	ClientBulletinStore store;
	AttachmentProxy proxy;
}