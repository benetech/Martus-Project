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
import java.io.FileOutputStream;
import java.io.IOException;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.SessionKey;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.util.FileUtils;
import org.martus.util.StreamableBase64;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


public class AttachmentProxy
{
	public AttachmentProxy(File fileToAttach)
	{
		file = fileToAttach;
		setLabel(file.getName());
	}

	public AttachmentProxy(UniversalId universalIdToUse, String labelToUse, SessionKey sessionKey)
	{
		setUniversalIdAndSessionKey(universalIdToUse, sessionKey);
		setLabel(labelToUse);
	}

	public AttachmentProxy(String labelToUse)
	{
		setLabel(labelToUse);
	}

	public static AttachmentProxy createFileProxyFromAttachmentPacket(InputStreamWithSeek attachmentIn, AttachmentProxy oldProxy, MartusCrypto verifier)
		throws
			IOException,
			Packet.InvalidPacketException,
			Packet.SignatureVerificationException,
			Packet.WrongPacketTypeException,
			StreamableBase64.InvalidBase64Exception
	{
		SessionKey sessionKey = oldProxy.getSessionKey();
		String extension = FileUtils.getFileExtensionIncludingPeriodIfPresent(oldProxy.getLabel());
		File tempFile = File.createTempFile("$$$MartusImportAttachment", extension);
		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);
		AttachmentPacket.exportRawFileFromXml(attachmentIn, sessionKey, verifier, out);
		out.close();
		AttachmentProxy ap = new AttachmentProxy(tempFile);
		ap.setLabel(oldProxy.getLabel());
		return ap;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String labelToUse)
	{
		label = labelToUse;
	}

	public File getFile()
	{
		return file;
	}

	public SessionKey getSessionKey()
	{
		return sessionKey;
	}

	public void setUniversalIdAndSessionKey(UniversalId universalId, SessionKey sessionKeyToUse)
	{
		uid = universalId;
		sessionKey = sessionKeyToUse;
		file = null;
	}

	public UniversalId getUniversalId()
	{
		return uid;
	}
	
	public void setPendingPacket(AttachmentPacket packet, SessionKey key)
	{
		pendingPacket = packet;
		uid = packet.getUniversalId();
		file = null;
		sessionKey = key;
	}
	
	public AttachmentPacket getPendingPacket()
	{
		return pendingPacket;
	}
	
	public int hashCode()
	{
		return uid.hashCode();
	}
	
	public boolean equals(Object rawOther)
	{
		if(! (rawOther instanceof AttachmentProxy))
			return false;
		
		AttachmentProxy other = (AttachmentProxy)rawOther;
		if(uid != null && uid.equals(other.uid))
			return true;
		
		if(file != null && file.equals(other.file))
			return true;
		
		return false;
	}

	public static String escapeFilenameForWindows(String fileName)
	{
		fileName = addQuotesAround(fileName, ' ');
		fileName = addQuotesAround(fileName, '^');
		fileName = addQuotesAround(fileName, '&');
		fileName = addQuotesAround(fileName, '%');
		fileName = addQuotesAround(fileName, '=');
		fileName = addQuotesAround(fileName, '(');
		fileName = addQuotesAround(fileName, ')');
		fileName = addQuotesAround(fileName, '|');
		fileName = addQuotesAround(fileName, ',');
		fileName = addQuotesAround(fileName, ';');
		fileName = addQuotesAround(fileName, '\'');

		return fileName;
	}

	private static String addQuotesAround(String fileName, char characterToQuote)
	{
		String from = "\\" + characterToQuote;
		String to = "\"" + characterToQuote + "\"";
		return fileName.replaceAll(from, to);
	}
	
	String label;
	File file;
	SessionKey sessionKey;
	UniversalId uid;
	AttachmentPacket pendingPacket;
}
