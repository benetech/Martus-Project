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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.martus.common.AuthorizedSessionKeys;
import org.martus.common.FieldCollection;
import org.martus.common.GridData;
import org.martus.common.MartusXml;
import org.martus.common.XmlCustomFieldsLoader;
import org.martus.common.XmlXFormsLoader;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.crypto.SessionKey;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.StreamableBase64;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlMapLoader;
import org.martus.util.xml.SimpleXmlStringLoader;
import org.xml.sax.SAXParseException;


public class XmlFieldDataPacketLoader extends XmlPacketLoader
{
	public XmlFieldDataPacketLoader(FieldDataPacket packetToFill)
	{
		super(packetToFill);
		fdp = packetToFill;
		authorizedEncryptedHQSessionKeyStrings = Collections.synchronizedMap(new HashMap());
		authorizedEncryptedHQSessionKeys = Collections.synchronizedMap(new HashMap());
	}
	
	public SimpleXmlDefaultLoader startElement(String tag)
		throws SAXParseException
	{
		if(tag.startsWith(MartusXml.FieldElementPrefix))
			return new XmlFieldLoader(tag, fdp);
		else if(tag.equals(MartusXml.AttachmentElementName))
			return new XmlAttachmentLoader(tag);
		else if(tag.equals(MartusXml.CustomFieldSpecsElementName))
			return new XmlCustomFieldsLoader();
		else if(tag.equals(MartusXml.XFormsElementName))
			return new XmlXFormsLoader();
		else if(getTagsContainingStrings().contains(tag))
			return new SimpleXmlStringLoader(tag);
		else if(tag.equals(AuthorizedSessionKeys.AUTHORIZED_SESSION_KEYS_TAG))
			return new AuthorizedSessionKeys.XmlAuthorizedLoader(authorizedEncryptedHQSessionKeyStrings);
		else
			return super.startElement(tag);
	}

	public void endElement(String tag, SimpleXmlDefaultLoader ended)
		throws SAXParseException
	{
		try
		{
			if(tag.startsWith(MartusXml.FieldElementPrefix))
			{
				XmlFieldLoader loader = (XmlFieldLoader)ended;
				fdp.set(loader.getFieldNameTag(), loader.getText());
			}
			else if(tag.equals(MartusXml.AttachmentElementName))
			{
				XmlAttachmentLoader loader = (XmlAttachmentLoader)ended;
					fdp.addAttachment(loader.getAttachmentProxy(fdp.getAccountId()));
			}
			else if(tag.equals(MartusXml.CustomFieldSpecsElementName))
			{
				XmlCustomFieldsLoader loader = (XmlCustomFieldsLoader)ended;
				fdp.setCustomFields(new FieldCollection(loader.getFieldSpecs()));
				foundModernFieldSpecs = true;
			}
			else if (tag.equals(MartusXml.XFormsElementName))
			{
				XmlXFormsLoader loader = (XmlXFormsLoader)ended;
				fdp.setXFormsModelAsString(loader.getXFormsModelAsString());
				fdp.setXFormsInstanceAsString(loader.getXFormsInstanceAsString());
			}
			else if(getTagsContainingStrings().contains(tag))
			{
				String value = ((SimpleXmlStringLoader)ended).getText();
				if(tag.equals(MartusXml.EncryptedFlagElementName))
					fdp.setEncrypted(true);
				else if(tag.equals(MartusXml.FieldListElementName))
					setLegacyCustomFields(value);
				else if(tag.equals(MartusXml.EncryptedDataElementName))
					encryptedData = value;
				else if(tag.equals(MartusXml.HQSessionKeyElementName))
					encryptedHQSessionKey = new SessionKey(StreamableBase64.decode(value));
			}
			else if(tag.equals(AuthorizedSessionKeys.AUTHORIZED_SESSION_KEYS_TAG))
			{
				for (Iterator iter = authorizedEncryptedHQSessionKeyStrings.entrySet().iterator(); iter.hasNext();)
				{
					Map.Entry element = (Map.Entry) iter.next();
					String publicCode = (String)element.getKey();
					String sessionKeyString = (String)element.getValue();
					SessionKey sessionKey = new SessionKey(StreamableBase64.decode(sessionKeyString));
					authorizedEncryptedHQSessionKeys.put(publicCode, sessionKey);
				}
			}
			else
				super.endElement(tag, ended);
		}
		catch (InvalidBase64Exception e)
		{
			e.printStackTrace();
			throw new SAXParseException("Bad base64 in " + tag, null);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SAXParseException("Unexpected Exception: " + e.getMessage(), null);
		}
	}

	private void setLegacyCustomFields(String value) throws Exception
	{
		if(!foundModernFieldSpecs)
			fdp.setFieldSpecsFromString(value);
	}
	
	private Vector getTagsContainingStrings()
	{
		if(stringTags == null)
		{
			stringTags = new Vector();
			stringTags.add(MartusXml.EncryptedFlagElementName);
			stringTags.add(MartusXml.FieldListElementName);
			stringTags.add(MartusXml.HQSessionKeyElementName);
			stringTags.add(MartusXml.EncryptedDataElementName);
			stringTags.add(MartusXml.XFormsElementName);
		}
		return stringTags;
	}
	
	static class XmlFieldLoader extends SimpleXmlStringLoader
	{
		XmlFieldLoader(String tag, FieldDataPacket fdp)
		{
			super(tag);
			String fieldNameTag = getFieldNameTag();
			field = fdp.getField(fieldNameTag);
			if(field == null)
				System.out.println("ERROR: Unknown field: " + fieldNameTag + " in " + fdp.getLocalId());
			spec = field.getFieldSpec();
		}
		
		String getFieldNameTag()
		{
			int prefixLength = MartusXml.FieldElementPrefix.length();
			String fieldNameTag = getTag().substring(prefixLength);
			return fieldNameTag;
		}

		public String getText()
		{
			if(complexData != null)
				return complexData;
			return super.getText();
		}

		public SimpleXmlDefaultLoader startElement(String tag)
				throws SAXParseException
		{
			if(tag.equals(GridData.GRID_DATA_TAG))
				return new GridData.XmlGridDataLoader(new GridData((GridFieldSpec)spec, field.getReusableChoicesLists()));
			return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended)
				throws SAXParseException
		{
			if(tag.equals(GridData.GRID_DATA_TAG))
				complexData = ((GridData.XmlGridDataLoader)ended).getGridData().getXmlRepresentation(); 
			super.endElement(tag, ended);
		}
		
		private MartusField field;
		FieldSpec spec;
		String complexData;
	}
	
	static class XmlAttachmentLoader extends SimpleXmlMapLoader
	{
		public XmlAttachmentLoader(String tag)
		{
			super(tag);
		}

		public AttachmentProxy getAttachmentProxy(String accountId) throws InvalidBase64Exception
		{
			String attachmentLocalId = get(MartusXml.AttachmentLocalIdElementName);
			byte[] sessionKeyBytes = StreamableBase64.decode(get(MartusXml.AttachmentKeyElementName));
			String attachmentLabel = get(MartusXml.AttachmentLabelElementName);
			
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, attachmentLocalId);
			SessionKey sessionKey = new SessionKey(sessionKeyBytes);
			return new AttachmentProxy(uid, attachmentLabel, sessionKey);
		}

	}
	
	public SessionKey GetHQSessionKey(String hqPublicKey)
	{
		SessionKey key = (SessionKey)authorizedEncryptedHQSessionKeys.get(hqPublicKey);
		if(key == null)
			return encryptedHQSessionKey;
		return key;
	}
	
	String encryptedData;
	private boolean foundModernFieldSpecs;
	private SessionKey encryptedHQSessionKey;
	private Map authorizedEncryptedHQSessionKeys;
	private Map authorizedEncryptedHQSessionKeyStrings;
	private FieldDataPacket fdp;
	private static Vector stringTags;
}
