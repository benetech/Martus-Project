/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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
package org.martus.client.tools;

import java.util.HashMap;
import java.util.Vector;

import org.martus.common.FieldCollection;
import org.martus.common.GridData;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.XmlCustomFieldsLoader;
import org.martus.common.bulletin.BulletinXmlExportImportConstants;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlStringLoader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

public class XmlBulletinLoader extends SimpleXmlDefaultLoader
{
	
	public XmlBulletinLoader() throws SAXParseException
	{
		super(BulletinXmlExportImportConstants.MARTUS_BULLETIN);
		try
		{
			mainFieldSpecs = new FieldCollection(new FieldSpec[0]);
			privateFieldSpecs = new FieldCollection(new FieldSpec[0]);
			topSectionAttachments = new Vector();
			bottomSectionAttachments = new Vector();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SAXParseException("Unexpected Exception: " + e.getMessage(), null);
		}
	}
	
	public SimpleXmlDefaultLoader startElement(String tag)
		throws SAXParseException
	{
		if(tag.equals(BulletinXmlExportImportConstants.MAIN_FIELD_SPECS))
			return new XmlCustomFieldsLoader(tag);
		else if(tag.equals(BulletinXmlExportImportConstants.PRIVATE_FIELD_SPECS))
			return new XmlCustomFieldsLoader(tag);
		else if(tag.equals(BulletinXmlExportImportConstants.FIELD_VALUES))
			return new FieldValuesSectionLoader(tag);
		return super.startElement(tag);
	}

	public void endElement(String tag, SimpleXmlDefaultLoader ended)
		throws SAXParseException
	{
		if(tag.equals(BulletinXmlExportImportConstants.MAIN_FIELD_SPECS))
		{
			mainFieldSpecs = getFieldSpecs(ended);
		}
		else if(tag.equals(BulletinXmlExportImportConstants.PRIVATE_FIELD_SPECS))
		{
			privateFieldSpecs = getFieldSpecs(ended);
		}
		else if(tag.equals(BulletinXmlExportImportConstants.FIELD_VALUES))
		{
			FieldValuesSectionLoader fieldValuesSectionLoader = ((FieldValuesSectionLoader)ended);
			fieldTagValuesMap = fieldValuesSectionLoader.getFieldTagValueMap();
			topSectionAttachments = fieldValuesSectionLoader.getTopSectionAttachments();
			bottomSectionAttachments = fieldValuesSectionLoader.getBottomSectionAttachments();
		}
		else
			super.endElement(tag, ended);
	}

	private FieldCollection getFieldSpecs(SimpleXmlDefaultLoader ended) throws SAXParseException 
	{
		XmlCustomFieldsLoader loader = (XmlCustomFieldsLoader)ended;
		try
		{
			return new FieldCollection(loader.getFieldSpecs());
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SAXParseException("Unexpected Exception: " + e.getMessage(), null);
		}
	}
	
	public HashMap getFieldTagValuesMap()
	{
		return fieldTagValuesMap;
	}
	
	public Vector getTopSectionAttachments()
	{
		return topSectionAttachments;
	}
	
	public Vector getBottomSectionAttachments()
	{
		return bottomSectionAttachments;
	}
	
	public FieldCollection getMainFieldSpecs()
	{
		return mainFieldSpecs;
	}
	
	public FieldCollection getPrivateFieldSpecs()
	{
		return privateFieldSpecs;
	}
	
	class FieldValuesSectionLoader extends SimpleXmlDefaultLoader
	{
		public FieldValuesSectionLoader(String tag)
		{
			super(tag);
			fieldTagToValueMap = new HashMap();
			topSectionAttachmentsList = new Vector();
			bottomSectionAttachmentsList = new Vector();
		}
		
		public SimpleXmlDefaultLoader startElement(String tag)throws SAXParseException
		{
			if(tag.equals(BulletinXmlExportImportConstants.FIELD))
				return new FieldLoader(tag);
			else if(tag.equals(BulletinXmlExportImportConstants.TOP_SECTION_ATTACHMENT_LIST))
				return new FieldAttachmentSectionLoader(tag);
			else if(tag.equals(BulletinXmlExportImportConstants.BOTTOM_SECTION_ATTACHMENT_LIST))
				return new FieldAttachmentSectionLoader(tag);
			
			return super.startElement(tag);
		}
	
		public void endElement(String tag, SimpleXmlDefaultLoader ended)throws SAXParseException
		{
			if(tag.equals(BulletinXmlExportImportConstants.FIELD))
			{
				FieldLoader fieldLoader = ((FieldLoader)ended);
				String fieldTag = fieldLoader.getFieldTag();
				String fieldValue = fieldLoader.getValue();
				fieldTagToValueMap.put(fieldTag, fieldValue);
			}
			else if(tag.equals(BulletinXmlExportImportConstants.TOP_SECTION_ATTACHMENT_LIST))
			{
				topSectionAttachmentsList.addAll(((FieldAttachmentSectionLoader)ended).getAttachments());
				
			}
			else if(tag.equals(BulletinXmlExportImportConstants.BOTTOM_SECTION_ATTACHMENT_LIST))
			{
				bottomSectionAttachmentsList.addAll(((FieldAttachmentSectionLoader)ended).getAttachments());
			}
			else
				super.endElement(tag, ended);
		}

		public HashMap getFieldTagValueMap()
		{
			return fieldTagToValueMap;
		}
		
		public Vector getTopSectionAttachments()
		{
			return topSectionAttachmentsList;
		}
		
		public Vector getBottomSectionAttachments()
		{
			return bottomSectionAttachmentsList;
		}
		
		HashMap fieldTagToValueMap;
		Vector topSectionAttachmentsList;
		Vector bottomSectionAttachmentsList;
	}
	
	class FieldAttachmentSectionLoader extends SimpleXmlStringLoader
	{
		public FieldAttachmentSectionLoader(String tag)
		{
			super(tag);
			attachments = new Vector();
		}
		public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
		{
			if(tag.equals(BulletinXmlExportImportConstants.ATTACHMENT))
			{
				return new AttachmentFileLoader(BulletinXmlExportImportConstants.ATTACHMENT);
			}
			return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended) throws SAXParseException
		{
			if(tag.equals(BulletinXmlExportImportConstants.ATTACHMENT))
			{
				String attachment = ((AttachmentFileLoader)ended).getAttachment();
				attachments.add(attachment);
			}
			super.endElement(tag, ended);
		}
		
		public Vector getAttachments()
		{
			return attachments;
		}
		
		Vector attachments;
	}
	
	class AttachmentFileLoader extends SimpleXmlStringLoader
	{

		public AttachmentFileLoader(String tag)
		{
			super(tag);
		}
		
		public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
		{
			if(tag.equals(BulletinXmlExportImportConstants.FILENAME))
			{
				return new SimpleXmlStringLoader(BulletinXmlExportImportConstants.FILENAME);
			}
			return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended) throws SAXParseException
		{
			if(tag.equals(BulletinXmlExportImportConstants.FILENAME))
			{
				attachmentFileName = ((SimpleXmlStringLoader)ended).getText();
			}
				super.endElement(tag, ended);
		}
		public String getAttachment()
		{
			return attachmentFileName;
		}
		String attachmentFileName;
	}
	
	class FieldLoader extends SimpleXmlDefaultLoader
	{

		public FieldLoader(String tag)
		{
			super(tag);
		}
		
		public String getValue()
		{
			if(valueLoader != null)
				return valueLoader.getText();
			if(isMessageField(tagForField))
				return getMessageValue(tagForField);
			return null;
		}
		
		public String getFieldTag()
		{
			return tagForField;
		}
		
		public void startDocument(Attributes attrs) throws SAXParseException
		{
			tagForField = attrs.getValue(BulletinXmlExportImportConstants.TAG_ATTRIBUTE);
			super.startDocument(attrs);
		}
		
		public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
		{
			if(tag.equals(BulletinXmlExportImportConstants.VALUE))
			{
				valueLoader = new ValueLoader(tagForField);
				return valueLoader;
			}
			return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended) throws SAXParseException
		{
			super.endElement(tag, ended);
		}
		ValueLoader valueLoader;
		String tagForField;
		String value;
	}
	
	class ValueLoader extends SimpleXmlStringLoader
	{

		public ValueLoader(String currentFieldTagToUse)
		{
			super(BulletinXmlExportImportConstants.VALUE);
			tagForField = currentFieldTagToUse;
		}
		
		public String getText()
		{
			if(complexData != null)
				return complexData;
			return super.getText();
		}

		public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
		{
			if(tag.equals(GridData.GRID_DATA_TAG))
			{
				GridData gridData = new GridData(getGridFieldSpec(tagForField), PoolOfReusableChoicesLists.EMPTY_POOL);
				return new GridData.XmlGridDataLoader(gridData);
			}
			return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended) throws SAXParseException
		{
			if(tag.equals(GridData.GRID_DATA_TAG))
				complexData = ((GridData.XmlGridDataLoader)ended).getGridData().getXmlRepresentation(); 
			super.endElement(tag, ended);
		}

		ValueLoader valueLoader;
		String tagForField;
		String complexData;
	}
	
	boolean isMessageField(String tag)
	{
		return getFieldFromSpecs(tag).getType().isMessage();
	}
	
	GridFieldSpec getGridFieldSpec(String tag)
	{
		return (GridFieldSpec)getFieldFromSpecs(tag).getFieldSpec();
	}

	String getMessageValue(String messageTag)
	{
		return getFieldFromSpecs(messageTag).getData();
	}
	
	MartusField getFieldFromSpecs(String tag)
	{
		MartusField field = mainFieldSpecs.findByTag(tag);
		if(field != null)
			return field;
		return privateFieldSpecs.findByTag(tag);
	}

	private FieldCollection mainFieldSpecs;
	private FieldCollection privateFieldSpecs;
	private HashMap fieldTagValuesMap;
	private Vector topSectionAttachments;
	private Vector bottomSectionAttachments;
}
