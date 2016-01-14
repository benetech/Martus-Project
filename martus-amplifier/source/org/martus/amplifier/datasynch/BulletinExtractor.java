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

package org.martus.amplifier.datasynch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.martus.amplifier.attachment.AttachmentStorageException;
import org.martus.amplifier.attachment.DataManager;
import org.martus.amplifier.main.EventDatesIndexedList;
import org.martus.amplifier.main.LanguagesIndexedList;
import org.martus.amplifier.search.BulletinField;
import org.martus.amplifier.search.BulletinIndexer;
import org.martus.common.FieldSpecCollection;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeek;



public class BulletinExtractor
{
	public BulletinExtractor(
		DataManager bulletinDataManager, 
		BulletinIndexer bulletinIndexer,
		MartusCrypto verifier)
	{
		this.bulletinDataManager = bulletinDataManager;
		this.bulletinIndexer = bulletinIndexer;
		this.verifier = verifier;
	}
	
	public void extractAndStoreBulletin(File bulletinFile) 
		throws Exception
	{
		ZipFile bulletinZipFile = new ZipFile(bulletinFile);
		try {
			BulletinHeaderPacket bhp = 
				BulletinHeaderPacket.loadFromZipFile(bulletinZipFile, verifier);
			String accountId = bhp.getAccountId();

			ZipEntryInputStreamWithSeek zipEntryPointForHeaderPacket = getInputStreamForZipEntry(bulletinZipFile, accountId, bhp.getLocalId());
			storeDataPacket(bhp.getUniversalId(), zipEntryPointForHeaderPacket);
			
			FieldDataPacket fdp = indexFieldData(bhp, bulletinZipFile);
			
			String fieldDataPacketId = bhp.getFieldDataPacketId();
			ZipEntryInputStreamWithSeek zipEntryPointForFieldDataPacket = getInputStreamForZipEntry(bulletinZipFile, accountId, fieldDataPacketId);
			storeDataPacket(fdp.getUniversalId(), zipEntryPointForFieldDataPacket);
			zipEntryPointForFieldDataPacket.close();
			
			storeAttachments(fdp.getAttachments(), bulletinZipFile);
		} finally {
			bulletinZipFile.close();
		}
	}
	

	private FieldDataPacket indexFieldData(BulletinHeaderPacket bhp, ZipFile bulletinZipFile) 
		throws Exception
	{
		String fieldDataPacketId = bhp.getFieldDataPacketId();
		String accountId = bhp.getAccountId();
		UniversalId fieldUid = UniversalId.createFromAccountAndLocalId(
			accountId, fieldDataPacketId);
			
		FieldSpecCollection fieldSpec = new FieldSpecCollection(BulletinField.getDefaultSearchFieldSpecs());
		FieldDataPacket fdp = new FieldDataPacket(fieldUid, fieldSpec);
		
		ZipEntryInputStreamWithSeek zipEntryPointForFieldDataPacket = getInputStreamForZipEntry(bulletinZipFile, accountId, fieldDataPacketId);

		fdp.loadFromXml(zipEntryPointForFieldDataPacket,verifier);
		bulletinIndexer.indexFieldData(bhp.getUniversalId(), fdp, bhp.getHistory());
		indexLanguage(fdp.get(BulletinConstants.TAGLANGUAGE));
		indexEventDate(fdp.get(BulletinConstants.TAGEVENTDATE));
		
		return fdp;
	}

	public static ZipEntryInputStreamWithSeek getInputStreamForZipEntry(ZipFile bulletinZipFile, String accountId, String fieldDataPacketId) throws IOException 
	{
		ZipEntry fieldDataEntry = bulletinZipFile.getEntry(fieldDataPacketId);
		if (fieldDataEntry == null) 
		{
			throw new IOException("No entry " + fieldDataPacketId + " found for account " + accountId);
		}
		return new ZipEntryInputStreamWithSeek(bulletinZipFile, fieldDataEntry);
	}
	
	public void indexLanguage(String languageCode) throws IOException
	{
		LanguagesIndexedList.languagesIndexedSingleton.addValue(languageCode);
	}
	
	public void indexEventDate(String flexidateString) throws IOException
	{
		EventDatesIndexedList.getEventDatesIndexedList().addValue(flexidateString);
	}
	
	private void storeDataPacket(UniversalId uId, ZipEntryInputStreamWithSeek data) throws IOException, RecordHiddenException, CryptoException
	{
		bulletinDataManager.putDataPacket(uId, data);	
	}
	
	private void storeAttachments(
		AttachmentProxy[] proxies, ZipFile bulletinZipFile) 
		throws IOException, WrongPacketTypeException, 
			SignatureVerificationException, InvalidPacketException, 
			InvalidBase64Exception, AttachmentStorageException
	{
		for (int i = 0; i < proxies.length; i++) 
		{
			AttachmentProxy proxy = proxies[i];
			UniversalId attachmentId = proxy.getUniversalId();
			ZipEntry attachmentEntry = bulletinZipFile.getEntry(attachmentId.getLocalId());
			if (attachmentEntry == null) 
			{
				throw new IOException(
					"No entry " + attachmentId.getLocalId() + 
					" found for account " + attachmentId.getAccountId());
			}
			proxy = AttachmentProxy.createFileProxyFromAttachmentPacket(
				new ZipEntryInputStreamWithSeek(bulletinZipFile, attachmentEntry), 
				proxy, verifier);
			InputStream attachmentData = new FileInputStream(proxy.getFile());
			try 
			{
				bulletinDataManager.putAttachment(attachmentId, attachmentData);
			} 
			finally 
			{
				attachmentData.close();
				proxy.getFile().delete();
			}
		}
	}
	
	private DataManager bulletinDataManager;
	private BulletinIndexer bulletinIndexer;
	private MartusCrypto verifier;
}