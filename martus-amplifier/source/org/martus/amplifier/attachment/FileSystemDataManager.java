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

import org.martus.amplifier.main.MartusAmplifier;
import org.martus.common.ContactInfo;
import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeek;


public class FileSystemDataManager implements DataManager
{
	public FileSystemDataManager(String baseDir) throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		this(baseDir, MartusAmplifier.getSecurity());
	}
	
	public FileSystemDataManager(String baseDir, MartusCrypto crypto) throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		db = new ServerFileDatabase(new File(baseDir), crypto);
		db.initialize();
	}
	public InputStream getAttachment(UniversalId attachmentId) throws AttachmentStorageException
	{
		try
		{
			return db.openInputStream(DatabaseKey.createSealedKey(attachmentId), db.security);
		}
		catch (Exception e)
		{
			throw new AttachmentStorageException(e);
		}
	}

	public long getAttachmentSizeInKb(UniversalId attachmentId) throws AttachmentStorageException
	{
		try
		{
			int sizeInBytes = db.getRecordSize(DatabaseKey.createSealedKey(attachmentId));
			int sizeInKb = sizeInBytes / Kbytes;
			if(sizeInKb == 0)
				sizeInKb = 1;
			return sizeInKb;
		}
		catch (Exception e)
		{
			throw new AttachmentStorageException(e);
		}
	}

	public long getAttachmentSizeInBytes(UniversalId attachmentId) throws AttachmentStorageException
	{
		try
		{
			return db.getRecordSize(DatabaseKey.createSealedKey(attachmentId));
		}
		catch (Exception e)
		{
			throw new AttachmentStorageException(e);
		}
	}

	public void putAttachment(UniversalId attachmentId, InputStream data) throws AttachmentStorageException
	{
		try
		{
			db.writeRecord(DatabaseKey.createSealedKey(attachmentId), data);
		}
		catch (Exception e)
		{
			throw new AttachmentStorageException(e);
		}
	}

	public void clearAllAttachments() throws AttachmentStorageException
	{
		try
		{
			db.deleteAllData();
		}
		catch (Exception e)
		{
			throw new AttachmentStorageException(e);
		}		
	}
	
	public File getContactInfoFile(String accountId) throws IOException
	{
		return db.getContactInfoFile(accountId);	
	}
	
	public void writeContactInfoToFile(String accountId, Vector contactInfo) throws IOException
	{
		MartusServerUtilities.writeContatctInfo(accountId, contactInfo, db.getContactInfoFile(accountId));
	}

	
	public Vector getContactInfo(String accountId) throws IOException
	{
		File contactFile = getContactInfoFile(accountId);
		if(!contactFile.exists())
			return null;
		
		Vector info = ContactInfo.loadFromFile(contactFile);
		if(!MartusAmplifier.getSecurity().verifySignatureOfVectorOfStrings(info, accountId))
			return null;
		removeContactInfoNonDataElements(info);		
		info = removeContactInfoBlankDataElements(info);
		
		return info;
	}

	private Vector removeContactInfoBlankDataElements(Vector info)
	{
		Vector stripped = new Vector();
		for(int i = 0; i < info.size(); ++i)
		{
			if(((String)info.get(i)).length() != 0)
				stripped.add(info.get(i));
		}
		return stripped;
	}

	private void removeContactInfoNonDataElements(Vector info)
	{
		info.remove(0);//Account ID
		info.remove(0);//# of data elements
		info.remove(info.size() - 1);//Signature
	}

	public void putDataPacket(UniversalId uid, ZipEntryInputStreamWithSeek data) throws IOException, RecordHiddenException, CryptoException
	{
		db.writeRecord(DatabaseKey.createSealedKey(uid), data);
	}

	public FieldDataPacket getFieldDataPacket(UniversalId uid) throws Exception
	{
		FieldSpecCollection standardPublicFieldSpecs = StandardFieldSpecs.getDefaultTopSetionFieldSpecs();
		FieldDataPacket fdp = new FieldDataPacket(uid, standardPublicFieldSpecs);
		InputStreamWithSeek in = null;
		try
		{
		in = db.openInputStream(DatabaseKey.createSealedKey(fdp.getUniversalId()),db.security);					
		fdp.loadFromXml(in, null);
		}
		finally
		{
			if(in != null)
				in.close();
		}
		return fdp;
	}
	
	public ServerFileDatabase getDatabase()
	{
		return db;
	}

	private ServerFileDatabase db;
	public static final int Kbytes = 1024;
}