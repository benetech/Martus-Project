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

package org.martus.common;



public class MartusXml
{
	public static String getFieldTagStart(String name)	{ return getTagStart(tagField, attrField, name); }
	public static String getFieldTagEnd()				{ return getTagEnd(tagField); }

	public static String getIdTag(String id)			{ return "<Id>" + id + "</Id>\n"; }

	public static String getAttachmentTagStart(String name)
	{
		return getTagStart(tagAttachment, attrAttachmentName, name);
	}

	public static String getAttachmentTagEnd()
	{
		return getTagEnd(tagAttachment);
	}

	public static String getXmlSchemaElement() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	}

	public static String getTagStart(String tagName)
	{
		return "<" + tagName + ">";
	}

	public static String getTagStartWithNewline(String tagName)
	{
		return getTagStart(tagName) + "\n";
	}
		
	public static String getTagStart(String tagName, String attrName, String attrValue)
	{
		String[] attrNames = {attrName};
		String[] attrValues = {attrValue};
		return getTagStart(tagName, attrNames, attrValues);
	}
	
	public static String getTagStart(String tagName, String[] attrNames, String[] attrValues)
	{
		String xmlStartTag = "<" + tagName;
		int numAttributes = attrNames.length;
		for(int i = 0; i < numAttributes; ++i)
		{
			xmlStartTag += " " + attrNames[i] + "='" + attrValues[i] + "'";
		}
		xmlStartTag += ">";
		return xmlStartTag;
	}

	public static String getTagWithData(String tag, String data)
	{
		return getTagStart(tag) + data + getTagEnd(tag);
	}

	public static String getTagStartWithNewline(String tagName, String attrName, String attrValue)
	{
		return getTagStart(tagName, attrName, attrValue) + "\n";
	}
	
	
	public static String getTagEnd(String tagName)
	{
		return getTagEndWithoutNewline(tagName) + "\n";
	}
	
	public static String getTagEndWithoutNewline(String tagName)
	{
		return "</" + tagName + ">";
	}

	// NOTE: Change the version any time the packet format changes in 
	// a "substantial" way. It must start with ;
	public final static String packetFormatVersion = ";1000";
	
	public final static String packetStartCommentStart = "<!--MartusPacket;";
	public final static String packetStartCommentSigLen = "siglen=";
	public final static String packetStartCommentEnd = ";-->";

	public final static String tagField = "Field";
	public final static String attrField = "name";
	public final static String tagAttachment = "Attachment";
	public final static String attrAttachmentName = "name";

	public final static String newLine = "\n";
	public final static String packetSignatureStart = "<!--sig=";
	public final static String packetSignatureEnd = "-->";

	public final static String PacketElementName = "Packet";
	public final static String FieldListElementName = "FieldList";
	public final static String FieldDataPacketElementName = "FieldDataPacket";
	public final static String AttachmentPacketElementName = "AttachmentPacket";
	public final static String BulletinHeaderPacketElementName = "BulletinHeaderPacket";
	public final static String BulletinStatusElementName = "BulletinStatus";
	public final static String LastSavedTimeElementName = "LastSavedTime";
	public final static String AllPrivateElementName = "AllPrivate";
	public final static String PacketIdElementName = "PacketId";
	public final static String PublicAttachmentIdElementName = "AttachmentId";
	public final static String PrivateAttachmentIdElementName = "PrivateAttachmentId";
	public final static String AccountElementName = "Account";
	public final static String EncryptedFlagElementName = "Encrypted";
	public final static String HQSessionKeyElementName = "HQSessionKey";
	public final static String HQPublicKeyElementName = "HQPublicKey";
	public final static String AccountsAuthorizedToReadElementName = "AuthorizedToReadKey";
	public final static String AllHQSProxyUploadName = "AllHQsProxyUpload";
	public final static String HistoryElementName = "History";
	public final static String AncestorElementName = "Ancestor";
	public final static String ExtendedHistorySectionName = "ExtendedHistory";
	public final static String ExtendedHistoryEntryName = "HistoryOfAClone";
	public final static String ExtendedHistoryClonedFromAccountName = "ClonedFrom";
	public final static String EncryptedDataElementName = "EncryptedData";
	public final static String DataPacketIdElementName = "DataPacketId";
	public final static String PrivateDataPacketIdElementName = "PrivateDataPacketId";
	public final static String DataPacketSigElementName = "DataPacketSig";
	public final static String PrivateDataPacketSigElementName = "PrivateDataPacketSig";
	public final static String FieldElementPrefix = "Field-";
	public final static String AttachmentLabelElementName = "AttachmentLabel";
	public final static String AttachmentLocalIdElementName = "AttachmentLocalId";
	public final static String AttachmentElementName = "Attachment";
	public final static String AttachmentBytesElementName = "AttachmentData";
	public final static String AttachmentKeyElementName = "AttachmentSessionKey";
	public final static String CustomFieldSpecsElementName = "CustomFields";
	public final static String StatusSnapshotName = "Snapshot";
	public final static String ImmutableOnServerName = "ImmutableOnServer";
	public final static String AccountsAuthorizedToReadPendingElementName = "AuthorizedToReadKeyPending";
	public final static String FormTemplateElementName = "FormTemplate";
	public final static String TitleElementName = "Title";
	public final static String XFormsElementName = "XForms";
	public final static String XFormsModelElementName = "xforms_model";
	public final static String XFormsInstanceElementName = "xforms_instance";
}
