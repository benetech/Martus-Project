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

package org.martus.common.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.martus.common.FieldCollection;
import org.martus.common.FieldSpecCollection;
import org.martus.common.HeadquartersKeys;
import org.martus.common.LegacyCustomFields;
import org.martus.common.MartusConstants;
import org.martus.common.MartusXml;
import org.martus.common.XmlWriterFilter;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.SessionKey;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeUnknown;
import org.martus.util.StreamableBase64;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.UnicodeReader;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.xml.SimpleXmlParser;
import org.xml.sax.SAXException;



public class FieldDataPacket extends Packet
{
	public FieldDataPacket(UniversalId universalIdToUse, FieldSpecCollection fieldSpecsToUse) throws Exception
	{
		super(universalIdToUse);
		setFieldSpecs(fieldSpecsToUse);
		authorizedToReadKeys = new HeadquartersKeys();
		clearAll();
	}
	
	void setCustomFields(FieldCollection fieldsToUse)
	{
		fields = fieldsToUse;
	}

	public void setFieldSpecs(FieldSpecCollection fieldSpecsToUse) throws Exception
	{
		fields = new FieldCollection(fieldSpecsToUse);
	}
	
	void setFieldSpecsFromString(String delimitedFieldSpecs) throws Exception
	{
		setFieldSpecs(LegacyCustomFields.parseFieldSpecsFromString(delimitedFieldSpecs));
	}

	public static UniversalId createUniversalId(MartusCrypto accountSecurity)
	{
		return UniversalId.createFromAccountAndLocalId(accountSecurity.getPublicKeyString(), createLocalId(accountSecurity, prefix));
	}

	public static boolean isValidLocalId(String localId)
	{
		return localId.startsWith(prefix);
	}
	
	public String getXFormsModelAString()
	{
		return xFormsModelAsString;
	}
	
	public String getXFormsInstanceAsString()
	{
		return xFormsInstanceAsString;
	}
	
	public void setXFormsModelAsString(String xFormsModelAsStringToUse)
	{
		xFormsModelAsString = xFormsModelAsStringToUse;
	}
	
	public void setXFormsInstanceAsString(String xFormsInstanceAsStringToUse)
	{
		xFormsInstanceAsString = xFormsInstanceAsStringToUse;
	}

	public boolean isEncrypted()
	{
		return encryptedFlag;
	}

	public void setEncrypted(boolean newValue)
	{
		encryptedFlag = newValue;
	}

	public void setAuthorizedToReadKeys(HeadquartersKeys authorizedKeys)
	{
		authorizedToReadKeys = authorizedKeys;
	}

	public HeadquartersKeys getAuthorizedToReadKeys()
	{
		return authorizedToReadKeys;
	}

	public boolean isPublicData()
	{
		return !isEncrypted();
	}

	public boolean isEmpty()
	{
		if(!fields.isEmpty())
			return false;

		if(attachments.size() > 0)
			return false;

		return true;
	}

	public int getFieldCount()
	{
		return fields.count();
	}

	public FieldSpecCollection getFieldSpecs()
	{
		return fields.getSpecs();
	}
	
	
	public MartusField getField(String fieldTag)
	{
		return fields.findByTag(fieldTag);
	}

	public boolean fieldExists(String fieldTag)
	{
		return (getFieldSpec(fieldTag) != null);
	}
	
	public FieldType getFieldType(String fieldTag)
	{
		MartusField field = getField(fieldTag);
		if(field == null)
			return new FieldTypeUnknown();
		
		return field.getType();
	}
	
	private FieldSpec getFieldSpec(String fieldTag)
	{
		MartusField field = getField(fieldTag);
		if(field == null)
			return null;
		
		return field.getFieldSpec();
	}

	public String get(String fieldTag)
	{
		MartusField field = getField(fieldTag);
		if(field == null)
			return "";
		
		String value = field.getData();
		if(value == null)
			return "";

		return value;
	}

	public void set(String fieldTag, String data)
	{
		MartusField field = getField(fieldTag);
		if(field == null)
			return;

		field.setData(data);
	}

	public void clearAll()
	{
		clearXFormsData();
		fields.clearAllData();
		clearAttachments();
		clearAuthorizedToRead();
	}

	public void clearAuthorizedToRead()
	{
		authorizedToReadKeys.clear();
	}

	public void clearAttachments()
	{
		attachments = new Vector();
	}

	public AttachmentProxy[] getAttachments()
	{
		AttachmentProxy[] list = new AttachmentProxy[attachments.size()];
		for(int i = 0; i < list.length; ++i)
			list[i] = (AttachmentProxy)attachments.get(i);

		return list;
	}

	public void addAttachment(AttachmentProxy a)
	{
		attachments.add(a);
	}


	public byte[] writeXml(Writer writer, MartusCrypto signer) throws IOException
	{
		byte[] result = null;
		if(isEncrypted() && !isEmpty())
			result = writeXmlEncrypted(writer, signer);
		else
			result = writeXmlPlainText(writer, signer);
		return result;
	}

	public void loadFromXml(InputStreamWithSeek inputStream, byte[] expectedSig, MartusCrypto security) throws
		IOException,
		InvalidPacketException,
		WrongPacketTypeException,
		SignatureVerificationException,
		MartusCrypto.DecryptionException,
		MartusCrypto.NoKeyPairException
	{
		setEncrypted(false);
		clearAll();
		if(security != null)
			verifyPacketSignature(inputStream, expectedSig, security);
		try
		{
			XmlFieldDataPacketLoader loader = loadXml(inputStream);
			
			String encryptedData = loader.encryptedData;
			if(encryptedData != null)
			{
				String publicCodeOfSecurity = MartusCrypto.computePublicCode(security.getPublicKeyString());
				SessionKey encryptedHQSessionKey = loader.GetHQSessionKey(publicCodeOfSecurity);
				loadEncryptedXml(encryptedData, encryptedHQSessionKey, security);
			}
			
		}
		catch(DecryptionException e)
		{
			throw(e);
		}
		catch(Exception e)
		{
			// TODO: Be more specific with exceptions!
			//e.printStackTrace();
			throw new InvalidPacketException(e);
		}
	}

	private void loadEncryptedXml(
		String encryptedData,
		SessionKey encryptedHQSessionKey,
		MartusCrypto security)
		throws
			DecryptionException,
			InvalidBase64Exception,
			IOException,
			InvalidPacketException,
			SignatureVerificationException,
			ParserConfigurationException,
			SAXException
	{
		SessionKey sessionKey = null;
		boolean isOurBulletin = security.getPublicKeyString().equals(getAccountId());
		if(!isOurBulletin)
		{
			sessionKey = security.decryptSessionKey(encryptedHQSessionKey);
		}
		
		byte[] encryptedBytes = StreamableBase64.decode(encryptedData);
		ByteArrayInputStreamWithSeek inEncrypted = new ByteArrayInputStreamWithSeek(encryptedBytes);
		ByteArrayOutputStream outPlain = new ByteArrayOutputStream();
		security.decrypt(inEncrypted, outPlain, sessionKey);
		ByteArrayInputStreamWithSeek inDecrypted = new ByteArrayInputStreamWithSeek(outPlain.toByteArray());
		verifyPacketSignature(inDecrypted, security);
		loadXml(inDecrypted);
	}

	private XmlFieldDataPacketLoader loadXml(InputStreamWithSeek in)
		throws IOException, ParserConfigurationException, SAXException
	{
		XmlFieldDataPacketLoader loader = new XmlFieldDataPacketLoader(this);
		SimpleXmlParser.parse(loader, new UnicodeReader(in));
		return loader;
	}

	public byte[] writeXmlPlainText(Writer writer, MartusCrypto signer) throws IOException
	{
		return super.writeXml(writer, signer);
	}

	public byte[] writeXmlEncrypted(Writer writer, MartusCrypto signer) throws IOException
	{
		StringWriter plainTextWriter = new StringWriter();
		writeXmlPlainText(plainTextWriter, signer);
		String payload = plainTextWriter.toString();

		EncryptedFieldDataPacket efdp = new EncryptedFieldDataPacket(getUniversalId(), payload, signer);
		efdp.setHQPublicKeys(getAuthorizedToReadKeys());
		return efdp.writeXml(writer, signer);
	}

	protected String getPacketRootElementName()
	{
		return MartusXml.FieldDataPacketElementName;
	}

	protected void internalWriteXml(XmlWriterFilter dest) throws IOException
	{
		super.internalWriteXml(dest);
		if(isEncrypted() && !isEmpty())
			writeElement(dest, MartusXml.EncryptedFlagElementName, "");

		writeXFormsElement(dest);
		
		String xmlSpecs = fields.getSpecsXml();
		
		if(hasCustomFieldSpecs())
		{
			writeElement(dest, MartusXml.FieldListElementName, MartusConstants.deprecatedCustomFieldSpecs);
			dest.writeDirect(xmlSpecs);
		}
		else
		{
			writeElement(dest, MartusXml.FieldListElementName, LegacyCustomFields.buildFieldListString(fields.getSpecs()));
		}
		
		for(int i = 0; i < fields.count(); ++i)
		{
			MartusField field = fields.getField(i);
			String key = field.getTag();
			String xmlTag = MartusXml.FieldElementPrefix + key;
			String fieldText = field.getData();
			if(fieldText == null)
				continue;
			if(field.getType().isGrid())
				writeNonEncodedElement(dest, xmlTag, fieldText);
			else
				writeElement(dest, xmlTag, fieldText);
		}

		for(int i = 0 ; i <attachments.size(); ++i)
		{
			AttachmentProxy a = (AttachmentProxy)attachments.get(i);
			dest.writeStartTag(MartusXml.AttachmentElementName);
			writeElement(dest, MartusXml.AttachmentLocalIdElementName, a.getUniversalId().getLocalId());
			String sessionKeyString = StreamableBase64.encode(a.getSessionKey().getBytes());
			writeElement(dest, MartusXml.AttachmentKeyElementName, sessionKeyString);
			writeElement(dest, MartusXml.AttachmentLabelElementName, a.getLabel());
			dest.writeEndTag(MartusXml.AttachmentElementName);
		}
	}

	private void writeXFormsElement(XmlWriterFilter dest) throws IOException
	{
		if (!containXFormsData())
			return;

		dest.writeStartTag(MartusXml.XFormsElementName);
		writeNonEncodedElement(dest, MartusXml.XFormsModelElementName, getXFormsModelAString());
		writeNonEncodedElement(dest, MartusXml.XFormsInstanceElementName, getXFormsInstanceAsString());
		dest.writeEndTag(MartusXml.XFormsElementName);
	}

	public boolean containXFormsData()
	{
		if (getXFormsModelAString() == null || getXFormsModelAString().isEmpty())
			return false;
		
		if (getXFormsInstanceAsString() == null || getXFormsInstanceAsString().isEmpty())
			return false;
		
		return true;
	}
	
	public void clearXFormsData()
	{
		xFormsModelAsString = null;
		xFormsInstanceAsString = null;
	}

	protected String getFieldListString()
	{
		return LegacyCustomFields.buildFieldListString(getFieldSpecs());
	}

	public boolean hasCustomFieldSpecs()
	{
		return !fields.toString().equals(DEFAULT_LEGACY_SPECS_AS_XML);
	}

	private static final String DEFAULT_LEGACY_SPECS_AS_XML = 
		"<CustomFields>\n\n" +
		"<Field type='LANGUAGE'>\n" +
		"<Tag>language</Tag>\n" +
		"<Label></Label>\n" +
		"</Field>\n" +
		"\n" +
		"<Field type='STRING'>\n" +
		"<Tag>author</Tag>\n" +
		"<Label></Label>\n" +
		"</Field>\n" +
		"\n" +
		"<Field type='STRING'>\n" +
		"<Tag>organization</Tag>\n" +
		"<Label></Label>\n" +
		"</Field>\n" +
		"\n" +
		"<Field type='STRING'>\n" +
		"<Tag>title</Tag>\n" +
		"<Label></Label>\n" +
		"</Field>\n" +
		"\n" +
		"<Field type='STRING'>\n" +
		"<Tag>location</Tag>\n" +
		"<Label></Label>\n" +
		"</Field>\n" +
		"\n" +
		"<Field type='STRING'>\n" +
		"<Tag>keywords</Tag>\n" +
		"<Label></Label>\n" +
		"</Field>\n" +
		"\n" +
		"<Field type='DATERANGE'>\n" +
		"<Tag>eventdate</Tag>\n" +
		"<Label></Label>\n" +
		"</Field>\n" +
		"\n" +
		"<Field type='DATE'>\n" +
		"<Tag>entrydate</Tag>\n" +
		"<Label></Label>\n" +
		"</Field>\n" +
		"\n" +
		"<Field type='MULTILINE'>\n" +
		"<Tag>summary</Tag>\n" +
		"<Label></Label>\n" +
		"</Field>\n" +
		"\n" +
		"<Field type='MULTILINE'>\n" +
		"<Tag>publicinfo</Tag>\n" +
		"<Label></Label>\n" +
		"</Field>\n" +
		"\n" +
		"</CustomFields>\n";
	
	final String packetHeaderTag = "packet";

	private boolean encryptedFlag;
	private FieldCollection fields;
	private Vector attachments;
	private String xFormsModelAsString;
	private String xFormsInstanceAsString;

	private static final String prefix = "F-";
	private HeadquartersKeys authorizedToReadKeys;
}

