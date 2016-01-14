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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.martus.common.FieldCollection;
import org.martus.common.GridData;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinXmlExportImportConstants;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.EncryptionException;
import org.martus.common.fieldspec.CustomFieldError;
import org.martus.common.fieldspec.CustomFieldSpecValidator;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlIntegerLoader;
import org.xml.sax.SAXParseException;

public class XmlBulletinsFileLoader extends SimpleXmlDefaultLoader
{
	public XmlBulletinsFileLoader(MartusCrypto cryptoToUse, File baseAttachmentsDirectoryToUse)
	{
		super(BulletinXmlExportImportConstants.MARTUS_BULLETINS);
		security = cryptoToUse;
		bulletins = new Vector();
		attachmentErrors = new HashMap();
		fieldSpecValidationErrors = new Vector();
		topSectionAttachments = new Vector();
		bottomSectionAttachments = new Vector();
		baseAttachmentsDirectory = baseAttachmentsDirectoryToUse;
	}
	
	public int getLoadedVersion()
	{
		return dataVersion;
	}

	public boolean isXmlVersionOlder()
	{
		return dataVersion < BulletinXmlExportImportConstants.XML_EXPORT_VERSION_NUMBER;
	}
	
	public boolean isXmlVersionNewer()
	{
		return dataVersion > BulletinXmlExportImportConstants.XML_EXPORT_VERSION_NUMBER;
	}
	
	public SimpleXmlDefaultLoader startElement(String tag)
		throws SAXParseException
	{
		if(tag.equals(BulletinXmlExportImportConstants.XML_EXPORT_VERSION))
		{
			return new SimpleXmlIntegerLoader(tag);
		}
		if(tag.equals(BulletinXmlExportImportConstants.MARTUS_BULLETIN))
		{
			currentBulletinLoader = new XmlBulletinLoader();
			return currentBulletinLoader;
		}
		return super.startElement(tag);
	}

	public void endElement(String tag, SimpleXmlDefaultLoader ended)
		throws SAXParseException
	{
		if(tag.equals(BulletinXmlExportImportConstants.XML_EXPORT_VERSION))
		{
			SimpleXmlIntegerLoader loader = (SimpleXmlIntegerLoader)ended;
			dataVersion = loader.getValue();
		}
		if(tag.equals(BulletinXmlExportImportConstants.MARTUS_BULLETIN))
		{
			mainFields = currentBulletinLoader.getMainFieldSpecs();
			privateFields = currentBulletinLoader.getPrivateFieldSpecs();
			fieldTagValuesMap = currentBulletinLoader.getFieldTagValuesMap();
			topSectionAttachments = currentBulletinLoader.getTopSectionAttachments();
			bottomSectionAttachments = currentBulletinLoader.getBottomSectionAttachments();
			validateMainFields(mainFields, privateFields);
			if(didFieldSpecVerificationErrorOccur())
				return;
			
			Bulletin createdBulletin;
			try
			{
				createdBulletin = createBulletin();
			}
			catch(IOException e)
			{
				fieldSpecValidationErrors.add(CustomFieldError.errorIO(e.getMessage()));
				return;
			}
			catch(EncryptionException e)
			{
				fieldSpecValidationErrors.add(CustomFieldError.errorSignature());
				return;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				fieldSpecValidationErrors.add(CustomFieldError.errorParseXml(e.getMessage()));
				return;
			}

			bulletins.add(createdBulletin);			
		}
		else
			super.endElement(tag, ended);
	}

	private Bulletin createBulletin() throws Exception
	{
		Bulletin bulletin = new Bulletin(security, mainFields.getSpecs(), privateFields.getSpecs());
		for (Iterator iter = fieldTagValuesMap.entrySet().iterator(); iter.hasNext();)
		{
			Map.Entry element = (Map.Entry) iter.next();
			String fieldTag = (String)element.getKey();
			String value = (String)element.getValue();
			value = convertDateFieldsToInternalFormat(fieldTag, value);
			bulletin.set(fieldTag, value);
		}
		addAttachmentsToBulletin(bulletin, topSectionAttachments, Bulletin.TOP_SECTION);
		addAttachmentsToBulletin(bulletin, bottomSectionAttachments, Bulletin.BOTTOM_SECTION);
		return bulletin;
	}
	
	private void addAttachmentsToBulletin(Bulletin bulletin, Vector attachmentFileNames, String sectionToAddAttachments) 
	{
		for(int i = 0; i < attachmentFileNames.size(); ++i)
		{
			String attachmentFileName = (String)attachmentFileNames.get(i);
			File attachmentFile;
			try
			{
				attachmentFile = getAttachmentFile(attachmentFileName);
				AttachmentProxy attachment = new AttachmentProxy(attachmentFile);
				if(sectionToAddAttachments.equals(Bulletin.TOP_SECTION))
					bulletin.addPublicAttachment(attachment);
				else
					bulletin.addPrivateAttachment(attachment);
			}
			catch (Exception e)
			{
				String title = bulletin.get(Bulletin.TAGTITLE);
				if(attachmentErrors.containsKey(title))
				{
					String previousFailingAttachments = (String)attachmentErrors.get(title);
					attachmentFileName = previousFailingAttachments + ", " + attachmentFileName;
					
				}
				attachmentErrors.put(title, attachmentFileName);
			}
		}
	}

	private File getAttachmentFile(String attachmentFileName) throws IOException
	{
		File attachmentFile = new File(baseAttachmentsDirectory, attachmentFileName);
		if(attachmentFile.isFile() && attachmentFile.canRead())
			return attachmentFile;
		throw new IOException(attachmentFile.getAbsolutePath());
	}

	public Bulletin[] getBulletins()
	{
		  return (Bulletin[])bulletins.toArray(new Bulletin[0]);
	}
	
	public HashMap getMissingAttachmentsMap()
	{
		return attachmentErrors;
	}
	
	public boolean didFieldSpecVerificationErrorOccur()
	{
		return fieldSpecValidationErrors.size()>0;
	}
	
	public Vector getErrors()
	{
		return fieldSpecValidationErrors;
	}
	
	private String convertDateFieldsToInternalFormat(String fieldTag, String value)
	{
		if(isDateField(fieldTag))
			value = extractRealDateValue(value);
		else if(isGrid(fieldTag))
			value = convertGridDatesToInternalFormat(fieldTag, value);
		return value;
	}


	private String convertGridDatesToInternalFormat(String fieldTag, String value)
	{
		GridFieldSpec gridSpec = currentBulletinLoader.getGridFieldSpec(fieldTag);
		GridData grid = new GridData(gridSpec, PoolOfReusableChoicesLists.EMPTY_POOL);
		try
		{
			grid.setFromXml(value);
			int columnCount = grid.getColumnCount();
			int rowCount = grid.getRowCount();
			for(int c = 0; c < columnCount; ++c )
			{
				if(isDateType(gridSpec.getColumnType(c)))
				{
					for(int r = 0; r < rowCount; ++r )
					{
						String rawDate = grid.getValueAt(r, c);
						grid.setValueAt(extractRealDateValue(rawDate), r, c);
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return grid.getXmlRepresentation();
	}

	boolean isGrid(String tag)
	{
		return currentBulletinLoader.getFieldFromSpecs(tag).getType().isGrid();
	}
	
	private boolean isDateField(String fieldTag)
	{
		return isDateType(currentBulletinLoader.getFieldFromSpecs(fieldTag).getType());
	}


	private boolean isDateType(FieldType type)
	{
		return type.isDate() || type.isDateRange();
	}
	
	private String extractRealDateValue(String xmlValue)
	{
		if(xmlValue.startsWith(BulletinXmlExportImportConstants.DATE_SIMPLE))
			return xmlValue.substring(BulletinXmlExportImportConstants.DATE_SIMPLE.length());
		if(xmlValue.startsWith(BulletinXmlExportImportConstants.DATE_RANGE))
		{
			String rawDateRange = xmlValue.substring(BulletinXmlExportImportConstants.DATE_RANGE.length());
			return MartusFlexidate.createMartusDateStringFromBeginAndEndDateString(rawDateRange);
		}
		return xmlValue;
	}

	private void validateMainFields(FieldCollection fieldsTopSection, FieldCollection fieldsBottomSection)
	{
		CustomFieldSpecValidator validator = new CustomFieldSpecValidator(fieldsTopSection, fieldsBottomSection, allowSpaceOnlyCustomLabels);
		for (Iterator iter = fieldTagValuesMap.entrySet().iterator(); iter.hasNext();)
		{
			Map.Entry element = (Map.Entry) iter.next();
			String fieldTag = (String)element.getKey();
			if(currentBulletinLoader.getFieldFromSpecs(fieldTag) == null)
				validator.addMissingCustomSpecError(fieldTag);
		}		
		if(!validator.isValid())
		{
			fieldSpecValidationErrors.add(validator);
		}
	}

	private int dataVersion;

	public boolean allowSpaceOnlyCustomLabels;

	private XmlBulletinLoader currentBulletinLoader;
	public FieldCollection mainFields;
	public FieldCollection privateFields;
	public HashMap fieldTagValuesMap;
	public Vector topSectionAttachments; 
	public Vector bottomSectionAttachments; 
	
	Vector bulletins;
	Vector fieldSpecValidationErrors;
	File baseAttachmentsDirectory;
	private MartusCrypto security;
	private HashMap attachmentErrors;
}
