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
package org.martus.amplifier.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.search.AttachmentInfo;
import org.martus.amplifier.search.BulletinField;
import org.martus.amplifier.search.BulletinIndexException;
import org.martus.amplifier.search.BulletinInfo;
import org.martus.amplifier.search.Results;
import org.martus.amplifier.search.SearchConstants;
import org.martus.common.MiniLocalization;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.UniversalId.NotUniversalIdException;
import org.martus.common.utilities.MartusFlexidate;


public class LuceneResults implements Results, LuceneSearchConstants, SearchConstants
{
	public LuceneResults(Vector docsToUse) throws IOException
	{
		docs = docsToUse;
	}
		
	public int getCount()
	{
		return docs.size();
	}

	public BulletinInfo getBulletinInfo(int n)
		throws BulletinIndexException 
	{
		Document doc = (Document)docs.get(n);
		BulletinInfo info = new BulletinInfo(getBulletinId(doc));
		
		addAllEmptyFields(info);
		addFields(info, doc);
		addAttachments(info, doc);
		addContactInfo(info);
		addHistory(info, doc);
		addFieldDataPacketId(info, doc);
		return info;
	}
	
	private static BulletinField getField(String fieldId) throws BulletinIndexException
	{
		BulletinField field = BulletinField.getFieldByXmlId(fieldId);
		if (field == null) 
		{
			throw new BulletinIndexException(
				"Unknown field " + fieldId);
		}
		return field;
	}
	
	private static void addAllEmptyFields(BulletinInfo info)
		throws BulletinIndexException
	{
		String[] fieldIds = BulletinField.getSearchableXmlIds();
		for (int i = 0; i < fieldIds.length; i++) 
		{
			BulletinField field = getField(fieldIds[i]); 
			info.set(field.getIndexId(), "");
		}
	}			
	
	private static void addFields(BulletinInfo info, Document doc) 
		throws BulletinIndexException
	{
		MiniLocalization localization = MartusAmplifier.localization;
		
		String[] fieldIds = BulletinField.getSearchableXmlIds();
		for (int i = 0; i < fieldIds.length; i++) 
		{
			BulletinField field = getField(fieldIds[i]);
			
			String value = doc.get(field.getIndexId());
			
			if (value != null) 
			{
				if(field.isDateField())
				{
					value = localization.convertStoredDateToDisplay(value);
				}
				if (field.isDateRangeField())
				{
					MartusFlexidate mfd = localization.createFlexidateFromStoredData(value);
					String formattedStartDate = localization.convertStoredDateToDisplay(mfd.getBeginDate().toIsoDateString());
					info.set(field.getIndexId()+"-start", formattedStartDate);

					if(mfd.hasDateRange())
					{
						String formattedEndDate = localization.convertStoredDateToDisplay(mfd.getEndDate().toIsoDateString());
						info.set(field.getIndexId()+"-end", formattedEndDate);
					}
					continue;
				}				
				info.set(field.getIndexId(), value);
			}
		}
	}
	

	private static void addAttachments(BulletinInfo bulletinInfo, Document doc) 
		throws BulletinIndexException
	{
		String attachmentsString = doc.get(ATTACHMENT_LIST_INDEX_FIELD);
		if (attachmentsString != null) 
		{
			String[] attachmentsAssocList = 
				attachmentsString.split(ATTACHMENT_LIST_SEPARATOR);
			if ((attachmentsAssocList.length % 2) != 0) 
			{
				throw new BulletinIndexException(
					"Invalid attachments string found: " + 
					attachmentsString);
			}
			for (int i = 0; i < attachmentsAssocList.length; i += 2) 
			{
				String accountId = bulletinInfo.getAccountId();
				String localId = attachmentsAssocList[i];
				UniversalId uId = UniversalId.createFromAccountAndLocalId(accountId, localId);
				long size = getAttachmentSizeInKb(uId);
				
				String attachmentLabel = attachmentsAssocList[i + 1];
				AttachmentInfo attachmentInfo = new AttachmentInfo(uId, attachmentLabel, size);
				bulletinInfo.addAttachment(attachmentInfo);
			}
		}
	}
	
	private static void addContactInfo(BulletinInfo info)
	{
		String accountId = info.getAccountId();
		try
		{
			File contactInfoFile = MartusAmplifier.dataManager.getContactInfoFile(accountId);
			if(!contactInfoFile.exists())
				return;
			info.setContactInfoFile(contactInfoFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void addHistory(BulletinInfo info, Document doc)
	{
		info.setHistory(doc.get(HISTORY_INDEX_FIELD));
	}
	
	private static void addFieldDataPacketId(BulletinInfo info, Document doc)
	{
		info.setFieldDataPacketUId(doc.get(FIELD_DATA_PACKET_LOCAL_ID_INDEX_FIELD));
	}
	
	private static long getAttachmentSizeInKb(UniversalId uId)
	{
		long size = -1;
		try
		{
			size = MartusAmplifier.dataManager.getAttachmentSizeInKb(uId);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return size;
	}

	static UniversalId getBulletinId(Document doc) 
		throws BulletinIndexException
	{
		String bulletinIdString = doc.get(BULLETIN_UNIVERSAL_ID_INDEX_FIELD);
		if (bulletinIdString == null)
		{
			throw new BulletinIndexException("Did not find bulletin universal id");
		}

		try
		{
			return UniversalId.createFromString(bulletinIdString);
		}
		catch (NotUniversalIdException e)
		{
			throw new BulletinIndexException(
				"Invalid bulletin universal id found",
				e);
		}
	}
	
	private Vector docs;		
}
	