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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusLogger;
import org.martus.common.MartusXml;
import org.martus.common.MiniLocalization;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.BulletinXmlExportImportConstants;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.ExtendedHistoryEntry;
import org.martus.common.packet.ExtendedHistoryList;
import org.martus.common.packet.UniversalId;
import org.martus.util.xml.XmlUtilities;

public class BulletinXmlExporter
{
	public BulletinXmlExporter(MartusApp appToUse, MiniLocalization localizationToUse, ProgressMeterInterface progressMeterToUse)
	{
		app = appToUse;
		localization = localizationToUse;
		progressMeter = progressMeterToUse;
		failingAttachments = 0;
		bulletinsExported = 0;
	}
	
	public void exportBulletins(Writer dest, Vector bulletins, boolean includePrivateData, boolean includeAttachments, boolean includeAllVersions, File attachmentsDirectory)
		throws Exception
	{
		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.MARTUS_BULLETINS));
		writeXMLVersion(dest);
		writeExportMetaData(dest, includePrivateData, includeAttachments);

		int bulletinCount = bulletins.size();
		for (int i = 0; i < bulletinCount; i++)
		{
			Bulletin b = (Bulletin)bulletins.get(i);
			if(progressMeter != null)
			{
				progressMeter.updateProgressMeter(i+1, bulletins.size());
				if(progressMeter.shouldExit())
					break;
			}
			if(!includePrivateData && b.isAllPrivate())
			{
				MartusLogger.log("Export skipping all-private bulletin");
			}
			else
			{
				exportOneBulletin(dest, b, includePrivateData, includeAttachments, attachmentsDirectory);
				if(includeAllVersions)
					exportOlderVersionsOf(dest, b, includePrivateData, includeAttachments, attachmentsDirectory);
				
				++bulletinsExported;
			}
		}
		dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.MARTUS_BULLETINS));
	}
	
	private void exportOlderVersionsOf(Writer dest, Bulletin latest, boolean includePrivateData, boolean includeAttachments, File attachmentsDirectory) throws Exception
	{
		BulletinHistory history = latest.getHistory();
		for(int i = 0; i < history.size(); ++i)
		{
			String localId = history.get(i);
			UniversalId uid = UniversalId.createFromAccountAndLocalId(latest.getAccount(), localId);
			if(!app.getStore().doesBulletinRevisionExist(uid))
			{
				MartusLogger.log("Not exporting " + uid + " because it is not available.");
				continue;
			}
			Bulletin older = app.getStore().getBulletinRevision(uid);
			exportOneBulletin(dest, older, includePrivateData, includeAttachments, attachmentsDirectory);
		}
	}

	public int getNumberOfBulletinsExported()
	{
		return bulletinsExported;
	}
	
	public int getNumberOfFailingAttachments()
	{
		return failingAttachments;
	}
	
	private void writeXMLVersion(Writer dest) throws IOException
	{
		String version = new Integer(BulletinXmlExportImportConstants.XML_EXPORT_VERSION_NUMBER).toString();
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.XML_EXPORT_VERSION, version));
	}
	
	private void writeExportMetaData(Writer dest, boolean includePrivateData, boolean includeAttachments) throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.EXPORT_META_DATA));
		if(includePrivateData)
		{
			dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.PUBLIC_AND_PRIVATE,""));
		}
		else
		{
			dest.write("<!--  No Private FieldSpecs or Data was exported  -->\n");
			dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.PUBLIC_ONLY,""));
		}
		
		if(!includeAttachments)
			dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.NO_ATTACHMENTS_EXPORTED,""));
		
		dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.EXPORT_META_DATA));
		dest.write(BulletinXmlExportImportConstants.NEW_LINE);
	}

	private void writeBulletinMetaData(Writer dest, Bulletin b) throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.BULLETIN_META_DATA));
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.ACCOUNT_ID, b.getAccount()));
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.LOCAL_ID, b.getLocalId()));
		
		long lastSavedTime = b.getLastSavedTime();
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.BULLETIN_LAST_SAVED_DATE_TIME, Long.toString(lastSavedTime)));
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.BULLETIN_LAST_SAVED_DATE_TIME_LOCALIZED, localization.formatDateTime(b.getLastSavedTime())));

		if(b.isAllPrivate())
			dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.ALL_PRIVATE, ""));
		writeBulletinStatus(dest, b);			
		writeBulletinHistory(dest, b);
		writeBulletinExtendedHistory(dest, b);
		dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.BULLETIN_META_DATA));
	}

	private void writeBulletinExtendedHistory(Writer dest, Bulletin b) throws IOException
	{
		ExtendedHistoryList history = b.getBulletinHeaderPacket().getExtendedHistory();
		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.EXTENDED_HISTORY));
		for(int i=0; i < history.size(); ++i)
		{
			ExtendedHistoryEntry entry = history.getHistory(i);

			dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.EXTENDED_HISTORY_ENTRY));
			dest.write(MartusXml.getTagWithData(BulletinXmlExportImportConstants.EXTENDED_HISTORY_AUTHOR, entry.getClonedFromAccountId()));
			writeBulletinHistory(dest, entry.getClonedHistory());
			dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.EXTENDED_HISTORY_ENTRY));
		}
		dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.EXTENDED_HISTORY));
	}

	private void writeBulletinHistory(Writer dest, Bulletin b) throws IOException
	{
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.BULLETIN_VERSION,Integer.toString(b.getVersion())));
		BulletinHistory history = b.getHistory();
		writeBulletinHistory(dest, history);
	}

	private void writeBulletinHistory(Writer dest, BulletinHistory history)
			throws IOException
	{
		if(history.size() > 0)
		{
			dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.HISTORY));
			for(int i=0; i < history.size(); ++i)
			{
				dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.ANCESTOR, history.get(i)));
			}
			dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.HISTORY));
		}
	}

	private void writeBulletinStatus(Writer dest, Bulletin b) throws IOException
	{
		String statusLocalized = localization.getStatusLabel("draft");
		String status = Bulletin.STATUSMUTABLE;
		if(b.isImmutable())
		{
			statusLocalized = localization.getStatusLabel("sealed");
			status = Bulletin.STATUSIMMUTABLE;
		}
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.BULLETIN_STATUS, status));
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.BULLETIN_STATUS_LOCALIZED, statusLocalized));
	}
	
	private void writeBulletinFieldSpecs(Writer dest, Bulletin b, boolean includePrivateData) throws Exception
	{
		if(shouldIncludeTopSection(b, includePrivateData))
			writeFieldSpecs(dest, b.getTopSectionFieldSpecs(), BulletinXmlExportImportConstants.MAIN_FIELD_SPECS);
		if(includePrivateData)
			writeFieldSpecs(dest, b.getBottomSectionFieldSpecs(), BulletinXmlExportImportConstants.PRIVATE_FIELD_SPECS);
	}

	private boolean shouldIncludeTopSection(Bulletin b, boolean includePrivateData)
	{
		return includePrivateData || !b.isAllPrivate();
	}

	public void writeFieldSpecs(Writer dest, FieldSpecCollection specs, String xmlTag) throws Exception
	{
		dest.write(MartusXml.getTagStartWithNewline(xmlTag));
		for(int i = 0; i < specs.size(); i++)
		{
			dest.write(specs.get(i).toXml(BulletinXmlExportImportConstants.FIELD));
		}
		Set reusableChoicesListNames = specs.getReusableChoiceNames();
		Iterator iter = reusableChoicesListNames.iterator();
		while(iter.hasNext())
		{
			String name = (String)iter.next();
			ReusableChoices choices = specs.getReusableChoices(name);
			dest.write(choices.toExportedXml());
		}
		dest.write(MartusXml.getTagEnd(xmlTag));
		dest.write(BulletinXmlExportImportConstants.NEW_LINE);
	}

	private void exportOneBulletin(Writer dest, Bulletin b, boolean includePrivateData, boolean includeAttachments, File attachmentsDirectory) throws Exception
	{
		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.BULLETIN));

		writeBulletinMetaData(dest, b);
		writeBulletinFieldSpecs(dest, b, includePrivateData);

		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.FIELD_VALUES));
		if(shouldIncludeTopSection(b, includePrivateData))
		{
			writeFields(dest, b, b.getTopSectionFieldSpecs().asArray());
			if(includeAttachments)
				writeAttachments(dest, attachmentsDirectory, b.getPublicAttachments(), BulletinXmlExportImportConstants.TOP_SECTION_ATTACHMENT_LIST);
		}

		if(includePrivateData)
		{
			writeFields(dest, b, b.getBottomSectionFieldSpecs().asArray());
			if(includeAttachments)
				writeAttachments(dest, attachmentsDirectory, b.getPrivateAttachments(), BulletinXmlExportImportConstants.BOTTOM_SECTION_ATTACHMENT_LIST);
		}
		dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.FIELD_VALUES));

		dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.BULLETIN));
		dest.write(BulletinXmlExportImportConstants.NEW_LINE);
	}

	private void writeAttachments(Writer dest, File attachmentsDirectory, AttachmentProxy[] attachments, String attachmentSectionTag)
		throws IOException
	{
		if(attachments.length == 0)
			return;

		dest.write(MartusXml.getTagStartWithNewline(attachmentSectionTag));
		for (int i = 0; i < attachments.length; i++)
		{
			AttachmentProxy proxy = attachments[i];
			String fileName = proxy.getLabel();
			File attachment = new File(attachmentsDirectory, fileName);
			try
			{
				if(attachment.exists())
				{
					String nameOnly = extractFileNameOnly(fileName);
					String extensionOnly = extractExtentionOnly(fileName);
					
					attachment = File.createTempFile(nameOnly, extensionOnly, attachmentsDirectory);
				}
				ReadableDatabase db = app.getStore().getDatabase();
				BulletinLoader.extractAttachmentToFile(db, proxy, app.getSecurity(), attachment);

				dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.ATTACHMENT));
				dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.FILENAME, attachment.getName()));
				dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.ATTACHMENT));
			}
			catch(Exception e)
			{
				dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.EXPORT_ERROR_ATTACHMENT_FILENAME, fileName));
				++failingAttachments;
				attachment.delete();
			}
		}
		dest.write(MartusXml.getTagEnd(attachmentSectionTag));
	}

	private void writeFields(Writer dest, Bulletin b, FieldSpec[] specs)
		throws IOException
	{
		for (int i = 0; i < specs.length; i++)
		{
			FieldSpec spec = specs[i];
			if(spec.hasUnknownStuff())
				continue;		
			final String tag = spec.getTag();
			MartusField field = b.getField(tag);
			String value = field.getExportableData(localization);
			if(spec.getType().isGrid())
			{
				String valueTagAndData = MartusXml.getTagWithData(BulletinXmlExportImportConstants.VALUE, value);
				writeElementDirect(dest, tag, valueTagAndData);
			}
			else
			{
				writeElement(dest, tag, value);
			}
		}
		
	}
	
	public static String extractFileNameOnly(String fullName)
	{
		int index = fullName.lastIndexOf('.');
		if(index == -1)
			index = fullName.length();
		String fileNameOnly = fullName.substring(0, index);
		while(fileNameOnly.length() < 3)
		{
			fileNameOnly += "_";	
		}
		return fileNameOnly;
	}

	public static String extractExtentionOnly(String fullName)
	{
		int index = fullName.lastIndexOf('.');
		if(index == -1)
			return null;
		return fullName.substring(index, fullName.length());
	}

	private static String getXmlEncodedTagWithData(String tagName, String data)
	{
		return MartusXml.getTagWithData(tagName, XmlUtilities.getXmlEncoded(data));
	}
	
	private static void writeElement(Writer dest, String tag, String fieldData) throws IOException
	{
		String xmlFieldTagWithData = getXmlEncodedTagWithData(BulletinXmlExportImportConstants.VALUE, fieldData);
		writeElementDirect(dest, tag, xmlFieldTagWithData);
	}

	private static void writeElementDirect(Writer dest, String tag, String xmlFieldData) throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline("Field "+BulletinXmlExportImportConstants.TAG_ATTRIBUTE+"='"+tag+"'"));
		dest.write(xmlFieldData);
		dest.write(MartusXml.getTagEnd(MartusXml.tagField));		
	}

	MiniLocalization localization;
	MartusApp app;
	int bulletinsExported;
	int failingAttachments;
	ProgressMeterInterface progressMeter;

}
