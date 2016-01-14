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

package org.martus.common.bulletin;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeek;



public class BulletinZipImporter
{

	public static void loadFromFile(Bulletin b, File inputFile, MartusCrypto verifier) throws IOException
	{
		b.clearAllUserData();
		ZipFile zip = new ZipFile(inputFile);
		try
		{

			BulletinHeaderPacket header = b.getBulletinHeaderPacket();

			ZipEntry headerEntry = null;
			Enumeration entries = zip.entries();
			while(entries.hasMoreElements())
				headerEntry = (ZipEntry)entries.nextElement();
			InputStreamWithSeek headerIn = new ZipEntryInputStreamWithSeek(zip, headerEntry);
			try
			{
				header.loadFromXml(headerIn, verifier);
				if(!header.getLocalId().equals(headerEntry.getName()))
					throw new IOException("Misnamed header entry");
			}
			catch(Exception e)
			{
				throw new IOException(e.getMessage());
			}
			finally
			{
				headerIn.close();
			}

			FieldDataPacket data = b.getFieldDataPacket();

			entries = zip.entries();
			ZipEntry dataEntry = zip.getEntry(header.getFieldDataPacketId());
			if(dataEntry == null)
				throw new IOException("Data packet not found");
			InputStreamWithSeek dataIn = new ZipEntryInputStreamWithSeek(zip, dataEntry);
			try
			{
				data.loadFromXml(dataIn, header.getFieldDataSignature(), verifier);
			}
			catch(MartusCrypto.DecryptionException e)
			{
				//TODO mark bulletin as not complete
			}
			catch(Exception e)
			{
				throw new IOException(e.getMessage());
			}
			finally
			{
				dataIn.close();
			}

			FieldDataPacket privateData = b.getPrivateFieldDataPacket();

			entries = zip.entries();
			ZipEntry privateDataEntry = zip.getEntry(header.getPrivateFieldDataPacketId());
			if(privateDataEntry == null)
				throw new IOException("Private data packet not found");
			InputStreamWithSeek privateDataIn = new ZipEntryInputStreamWithSeek(zip, privateDataEntry);
			try
			{
				privateData.loadFromXml(privateDataIn, header.getPrivateFieldDataSignature(), verifier);
			}
			catch(MartusCrypto.DecryptionException e)
			{
				//TODO Mark bulletin as not complete
			}
			catch(Exception e)
			{
				System.out.println(e);
				e.printStackTrace();
				throw new IOException(e.getMessage());
			}
			finally
			{
				privateDataIn.close();
			}

			AttachmentProxy[] attachments = b.getPublicAttachments();
			b.clearPublicAttachments();
			for(int i=0; i < attachments.length; ++i)
			{
				final AttachmentProxy ap = BulletinZipImporter.extractZipAttachmentToFileProxy(verifier, zip, attachments[i]);
				b.addPublicAttachment(ap);
			}

			AttachmentProxy[] attachmentsPrivate = b.getPrivateAttachments();
			b.clearPrivateAttachments();
			for(int i=0; i < attachmentsPrivate.length; ++i)
			{
				final AttachmentProxy ap = BulletinZipImporter.extractZipAttachmentToFileProxy(verifier, zip, attachmentsPrivate[i]);
				b.addPrivateAttachment(ap);
			}

			b.setAuthorizedToReadKeys(header.getAuthorizedToReadKeys());
		}
		catch(Exception e)
		{
			throw new IOException(e.getMessage());
		}
		finally
		{
			zip.close();
		}
	}

	public static Bulletin loadFromFileAsNewDraft(MartusCrypto security, File inputFile) throws 
		Exception
	{
		Bulletin original = new Bulletin(security);
		BulletinZipImporter.loadFromFile(original, inputFile, security);
		Bulletin imported = new Bulletin(security, original.getTopSectionFieldSpecs(), original.getBottomSectionFieldSpecs());
		imported.createDraftCopyOf(original, null);
		return imported;
	}

	public static AttachmentProxy extractZipAttachmentToFileProxy(MartusCrypto verifier, ZipFile zip, AttachmentProxy attachment) throws
	IOException
	{
		String localId = attachment.getUniversalId().getLocalId();
		ZipEntry attachmentEntry = zip.getEntry(localId);
		if(attachmentEntry == null)
			throw new IOException("Attachment packet not found: " + localId);
		InputStreamWithSeek attachmentIn = new ZipEntryInputStreamWithSeek(zip, attachmentEntry);
		try
		{
			return AttachmentProxy.createFileProxyFromAttachmentPacket(attachmentIn, attachment, verifier);
		}
		catch(Exception e)
		{
			throw new IOException(e.getMessage());
		}
		finally
		{
			attachmentIn.close();
		}
	}

}
