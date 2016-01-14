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

package org.martus.client.core;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.LinkedList;
import java.util.Vector;

import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.ReadableDatabase;

public class TransferableAttachmentList implements Transferable
{
	public TransferableAttachmentList(ReadableDatabase dbToUse, MartusCrypto securityToUse, AttachmentProxy[] attachmentsToUse)
	{
		db = dbToUse;
		security = securityToUse;
		attachments = attachmentsToUse;
	}

	synchronized boolean createTransferableAttachmentFiles()
	{
		if(files == null)
			files = new Vector();
		for(int i = 0 ; i < attachments.length ; ++i)
		{
			AttachmentProxy attachment = attachments[i];
			if(attachment.getFile() != null)
			{
				return false; //We can enable this but not needed in a UiAttachmentViewer
				//All attachments will be in the database.
			}

			try
			{
				File directory = File.createTempFile("$$$MartusAttachments", null);
				directory.delete();
				directory.mkdirs();
				directory.deleteOnExit();

				File outputFile = new File(directory, attachment.getLabel());
				outputFile.deleteOnExit();
				files.add(outputFile);
				
				BulletinLoader.extractAttachmentToFile(db, attachment, security, outputFile);
				//System.out.println("TransferableAttachment extractAttachmentToFile: " + outputFile);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println("TransferableAttachment extractAttachmentToFile: " + e);
				return false;
			}
		}
		return true;
	}

	public void dispose()
	{
		if(files == null)
			return;

		try
		{
			for(int i =0 ; i < files.size(); ++i)
			{
				File file = (File)files.get(i);
				file.delete();
			}
		}
		catch(Exception e)
		{
			System.out.println("TransferableAttachmentList.dispose ignoring: " + e);
		}
	}
	
	
	// Transferable interface
	synchronized public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
	{
		if(!flavor.equals(DataFlavor.javaFileListFlavor))
			throw new UnsupportedFlavorException(flavor);
			
		if(files == null || files.size() == 0)
		{	
			if(!createTransferableAttachmentFiles())
				throw new UnsupportedFlavorException(flavor);
		}
		LinkedList list = new LinkedList();
		list.addAll(files);
		return list;
	}

	public DataFlavor[] getTransferDataFlavors()
	{
		DataFlavor[] flavorArray = {DataFlavor.javaFileListFlavor};
		return flavorArray;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		if(flavor.equals(DataFlavor.javaFileListFlavor))
			return true;
		return false;
	}

	Vector files;
	AttachmentProxy[] attachments;
	ReadableDatabase db;
	MartusCrypto security;
}
