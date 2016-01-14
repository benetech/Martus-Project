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
package org.martus.client.core;

import java.io.File;
import java.io.IOException;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

public class AttachmentProxyFile
{
	public static AttachmentProxyFile wrapFile(File fileToWrap)
	{
		return new AttachmentProxyFile(fileToWrap, false);
	}

	public static AttachmentProxyFile extractAttachment(ClientBulletinStore store, AttachmentProxy proxy) throws Exception
	{
		ReadableDatabase db = store.getDatabase();
		MartusCrypto security = store.getSignatureVerifier();
	
		File attachmentAlreadyAvailableAsFile = proxy.getFile();
		if(attachmentAlreadyAvailableAsFile != null)
			return new AttachmentProxyFile(attachmentAlreadyAvailableAsFile, false);
		
		AttachmentPacket pendingPacket = proxy.getPendingPacket();
		if(pendingPacket != null)
		{
			File tempFileAlreadyAvailable = pendingPacket.getRawFile();
			if(tempFileAlreadyAvailable != null)
				return new AttachmentProxyFile(tempFileAlreadyAvailable, false);
		}
		
		File tempFile = AttachmentProxyFile.extractAttachmentToTempFile(db, proxy, security);
		return new AttachmentProxyFile(tempFile, true);
	}

	public AttachmentProxyFile(File fileToWrap, boolean shouldDeleteOnRelease)
	{
		file = fileToWrap;
		shouldDelete = shouldDeleteOnRelease;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public void release()
	{
		if(shouldDelete)
			file.delete();
		
		file = null;
	}

	private static File extractAttachmentToTempFile(ReadableDatabase db, AttachmentProxy proxy, MartusCrypto security) throws IOException, InvalidBase64Exception, InvalidPacketException, SignatureVerificationException, WrongPacketTypeException, CryptoException
	{
		String fileName = proxy.getLabel();
		
		File temp = File.createTempFile(BulletinXmlExporter.extractFileNameOnly(fileName), BulletinXmlExporter.extractExtentionOnly(fileName));
		temp.deleteOnExit();
	
		BulletinLoader.extractAttachmentToFile(db, proxy, security, temp);
		return temp;
	}

	private File file;
	private boolean shouldDelete;
}
