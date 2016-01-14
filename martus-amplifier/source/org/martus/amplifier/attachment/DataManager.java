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
package org.martus.amplifier.attachment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeek;

/**
 * This class defines the interface through which attachments are saved
 * on the local system and retrieved.
 * 
 * @author PDAlbora
 */
public interface DataManager 
{
	InputStream getAttachment(UniversalId attachmentId) 
		throws AttachmentStorageException;
	
	public long getAttachmentSizeInKb(UniversalId attachmentId) 
		throws AttachmentStorageException;

	public long getAttachmentSizeInBytes(UniversalId attachmentId) 
		throws AttachmentStorageException;

	void putAttachment(UniversalId attachmentId, InputStream data)
		throws AttachmentStorageException;
		
	void clearAllAttachments() throws AttachmentStorageException;
	
	public File getContactInfoFile(String accountId) throws IOException;
	public void writeContactInfoToFile(String accountId, Vector contactInfo) throws IOException;
	public Vector getContactInfo(String accountId) throws IOException;
	
	public void putDataPacket(UniversalId uid, ZipEntryInputStreamWithSeek data) throws IOException, RecordHiddenException, CryptoException;
	public FieldDataPacket getFieldDataPacket(UniversalId uid) throws Exception;
}
