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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.martus.common.MartusConstants;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeWriter;



public class BulletinForTesting extends Bulletin
{
	public BulletinForTesting(MartusCrypto securityToUse) throws Exception
	{
		super(securityToUse);
	}

	public static void loadFromZipString(Bulletin b, String zipString, MartusCrypto sigVerifier) throws IOException, StreamableBase64.InvalidBase64Exception
	{
		File tempFile = null;
		try
		{
			tempFile = StreamableBase64.decodeToTempFile(zipString);
			BulletinZipImporter.loadFromFile(b, tempFile, sigVerifier);
		}
		finally
		{
			if(tempFile != null)
				tempFile.delete();
		}
	}

	public static String saveToZipString(ReadableDatabase db, Bulletin b, MartusCrypto security) throws
		IOException,
		MartusCrypto.CryptoException
	{
		File tempFile = File.createTempFile("$$$Martus-saveToZipString", null);
		try
		{
			tempFile.deleteOnExit();
			saveToFile(db, b, tempFile, security);
			FileInputStream inputStream = new FileInputStream(tempFile);
			int len = inputStream.available();
			byte[] rawBytes = new byte[len];
			inputStream.read(rawBytes);
			inputStream.close();
			return StreamableBase64.encode(rawBytes);
		}
		finally
		{
			tempFile.delete();
		}

	}

	public static void saveToFile(ReadableDatabase db, Bulletin b, File destFile, MartusCrypto security) throws
		IOException,
		MartusCrypto.CryptoException
	{
		BulletinHeaderPacket header = b.getBulletinHeaderPacket();

		FieldDataPacket publicDataPacket = b.getFieldDataPacket();
		boolean shouldEncryptPublicData = (b.isMutable() || b.isAllPrivate());
		publicDataPacket.setEncrypted(shouldEncryptPublicData);

		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destFile));
		ZipOutputStream zipOut = new ZipOutputStream(outputStream);
		try
		{
			byte[] dataSig = writePacketToZip(b, zipOut, b.getFieldDataPacket(), security);
			header.setFieldDataSignature(dataSig);

			byte[] privateDataSig = writePacketToZip(b, zipOut, b.getPrivateFieldDataPacket(), security);
			header.setPrivateFieldDataSignature(privateDataSig);

			writeAttachmentsToZip(db, b, zipOut, b.getPublicAttachments(), security);
			writeAttachmentsToZip(db, b, zipOut, b.getPrivateAttachments(), security);

			writePacketToZip(b, zipOut, header, security);
		}
		finally
		{
			zipOut.close();
		}
	}

	public static void writeAttachmentsToZip(ReadableDatabase db, Bulletin b, ZipOutputStream zipOut, AttachmentProxy[] attachments, MartusCrypto sigVerifier) throws
		IOException,
		CryptoException
	{
		for(int i = 0 ; i < attachments.length ; ++i)
		{
			UniversalId uid = attachments[i].getUniversalId();
			ZipEntry attachmentEntry = new ZipEntry(uid.getLocalId());
			zipOut.putNextEntry(attachmentEntry);
			DatabaseKey key = b.getDatabaseKeyForLocalId(uid.getLocalId());
			InputStream in = new BufferedInputStream(db.openInputStream(key, sigVerifier));

			byte[] bytes = new byte[MartusConstants.streamBufferCopySize];
			int got;
			while((got = in.read(bytes)) != -1)
			{
				zipOut.write(bytes, 0, got);
			}
			in.close();
			zipOut.flush();
		}
	}

	static byte[] writePacketToZip(Bulletin b, ZipOutputStream zipOut, Packet packet, MartusCrypto security) throws
		IOException
	{
		ZipEntry entry = new ZipEntry(packet.getLocalId());
		zipOut.putNextEntry(entry);

		UnicodeWriter writer = new UnicodeWriter(zipOut);
		byte[] sig = packet.writeXml(writer, security);
		writer.flush();
		return sig;
	}

}
