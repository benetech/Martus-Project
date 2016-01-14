/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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

public class BulletinXmlExportImportConstants 
{
	public final static String NEW_LINE = "\n";

	public final static String XML_EXPORT_VERSION = "MartusBulletinExportFormatVersion";
	public final static String PUBLIC_ONLY = "PublicDataOnly";
	public final static String PUBLIC_AND_PRIVATE = "PublicAndPrivateData";
	public final static String NO_ATTACHMENTS_EXPORTED = "NoAttachmentsExported";
	public final static String EXPORT_META_DATA = "ExportMetaData";
	public final static String BULLETIN = "MartusBulletin";
	public final static String BULLETIN_META_DATA = "BulletinMetaData";
	public final static String LOCAL_ID = "BulletinLocalId";
	public final static String ALL_PRIVATE = "AllPrivate";
	public final static String ACCOUNT_ID = "AuthorAccountId";
	public final static String BULLETIN_VERSION = "BulletinVersion";
	public final static String BULLETIN_LAST_SAVED_DATE_TIME = "BulletinLastSavedDateTime";
	public final static String BULLETIN_LAST_SAVED_DATE_TIME_LOCALIZED = "LocalizedBulletinLastSavedDateTime";
	public final static String BULLETIN_STATUS = "BulletinStatus";
	public final static String BULLETIN_STATUS_LOCALIZED = "LocalizedBulletinStatus";
	public final static String HISTORY = "History";
	public final static String EXTENDED_HISTORY = "ExtendedHistory";
	public final static String EXTENDED_HISTORY_ENTRY = "ExtendedHistoryEntry";
	public final static String EXTENDED_HISTORY_AUTHOR = "Author";
	public final static String ANCESTOR = "Ancestor";
	public final static String ATTACHMENT = "Attachment";
	public final static String FILENAME = "Filename";
	public final static String EXPORT_ERROR_ATTACHMENT_FILENAME = "ErrorExportingAttachment";
	public final static String TYPE = "Type";
	public final static String TAG = "Tag";
	public final static String VALUE = "Value";
	public final static String LABEL = "Label";
	public final static String FIELD = "Field";
	public final static String MARTUS_BULLETINS = "MartusBulletins";
	public final static String MARTUS_BULLETIN = "MartusBulletin";
	public final static String TOP_SECTION_ATTACHMENT_LIST = "TopAttachmentList";
	public final static String BOTTOM_SECTION_ATTACHMENT_LIST = "BottomAttachmentList";
	public final static String FIELD_VALUES = "FieldValues";
	public final static String REUSABLE_CHOICES_LIST = "ReusableChoices";
	public final static String REUSABLE_CHOICE_ITEM = "Choice";
	public final static String CHOICE_ITEM_CODE_ATTRIBUTE = "code";
	public final static String CHOICE_ITEM_LABEL_ATTRIBUTE = "label";
	public final static String MAIN_FIELD_SPECS = "MainFieldSpecs";
	public final static String PRIVATE_FIELD_SPECS = "PrivateFieldSpecs";

	public final static String TAG_ATTRIBUTE = "tag";

	public final static String DATE_SIMPLE = "Simple:";
	public final static String DATE_RANGE = "Range:";

	public final static int XML_EXPORT_VERSION_NUMBER = 3;

}
