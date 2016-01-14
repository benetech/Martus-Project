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
package org.martus.common.fieldspec;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Vector;

import org.martus.common.FieldCollection;
import org.martus.common.FieldCollection.CustomFieldsParseException;
import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusLogger;
import org.martus.common.MartusXml;
import org.martus.common.MiniLocalization;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeUtilities;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


public class FormTemplate
{
	public FormTemplate()
	{
		super();
		clearData();
	}

	public FormTemplate(String title, String description, FieldSpecCollection topSection, FieldSpecCollection bottomSection) throws Exception 
	{
		clearData();
		if(!isvalidTemplateXml(topSection.toXml(), bottomSection.toXml()))
			throw new CustomFieldsParseException();
		
		this.title = title;
		this.description = description;
		this.topFields = topSection;
		this.bottomFields = bottomSection;
	}
	
	@Override
	public String toString()
	{
		return getTitle();
	}

	private void clearData()
	{
		errors = new Vector();
		topFields = null;
		bottomFields = null;
		title = "";
		description = "";
	}
	
	public class FutureVersionException extends Exception
	{
	}

	public boolean importTemplate(File tempFormTemplateFile, MartusCrypto security) throws Exception 
	{
		FileInputStreamWithSeek inputStream = new FileInputStreamWithSeek(tempFormTemplateFile);
		try
		{
			return importTemplate(security, inputStream);
		}
		finally
		{
			inputStream.close();
		}
	}

	public boolean importTemplate(MartusCrypto security, InputStreamWithSeek inputStreamWithSeek) throws FutureVersionException, IOException
	{
		try
		{
			clearData();
			String templateXMLToImportTopSection = "";
			String templateXMLToImportBottomSection = "";
			
			InputStreamWithSeek dataBundleTopSectionInputStream;
			if(isLegacyTemplateFile(inputStreamWithSeek))
			{
				dataBundleTopSectionInputStream = inputStreamWithSeek;
				FieldSpecCollection defaultBottomFields = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
				templateXMLToImportBottomSection = defaultBottomFields.toXml();
			}
			else
			{
				int templateVersion;
				byte[] dataBundleBottomSection;
				byte[] dataTitle;
				byte[] dataDescription;

				DataInputStream bundleIn = new DataInputStream(inputStreamWithSeek);
				try
				{
					bundleIn.skip(versionHeader.length()); //ignore header
					templateVersion = bundleIn.readInt();
					if(templateVersion > exportVersionNumber)
						throw new FutureVersionException();
					
					int topSectionBundleLength = bundleIn.readInt();
					int bottomSectionBundleLength = bundleIn.readInt();
					int titleLength = 0;
					if(templateVersion >= 3)
						titleLength = bundleIn.readInt();
					int descriptionLength = 0;
					if(templateVersion >= 3)
						descriptionLength = bundleIn.readInt();
					
					byte[] dataBundleTopSection = new byte[topSectionBundleLength];
					dataBundleBottomSection = new byte[bottomSectionBundleLength];
					dataTitle = new byte[titleLength];
					dataDescription = new byte[descriptionLength];
					
					bundleIn.read(dataBundleTopSection,0, topSectionBundleLength);
					dataBundleTopSectionInputStream = new ByteArrayInputStreamWithSeek(dataBundleTopSection);
					dataBundleTopSectionInputStream.seek(0);
					bundleIn.read(dataBundleBottomSection,0, bottomSectionBundleLength);
					bundleIn.read(dataTitle,0, titleLength);
					bundleIn.read(dataDescription,0, descriptionLength);
				} 
				finally
				{
					bundleIn.close();
				}
				
				Vector bottomSectionSignedByKeys = getSignedByAsVector(dataBundleBottomSection, security);
				byte[] xmlBytesBottomSection = security.extractFromSignedBundle(dataBundleBottomSection, bottomSectionSignedByKeys);
				templateXMLToImportBottomSection = UnicodeUtilities.toUnicodeString(xmlBytesBottomSection);

				if(templateVersion >= 3)
				{
					Vector titleSignedByKeys = getSignedByAsVector(dataTitle, security);
					byte[] bytesTitleOnly = security.extractFromSignedBundle(dataTitle, titleSignedByKeys);
					title = UnicodeUtilities.toUnicodeString(bytesTitleOnly);
				}

				if(templateVersion >= 3)
				{
					Vector descriptionSignedByKeys = getSignedByAsVector(dataDescription, security);
					byte[] bytesDescriptionOnly = security.extractFromSignedBundle(dataDescription, descriptionSignedByKeys);
					description = UnicodeUtilities.toUnicodeString(bytesDescriptionOnly);
				}
			}

			Vector topSectionSignedByKeys = getSignedByAsVector(dataBundleTopSectionInputStream, security);
			byte[] xmlBytesTopSection = security.extractFromSignedBundle(dataBundleTopSectionInputStream, topSectionSignedByKeys);
			templateXMLToImportTopSection = UnicodeUtilities.toUnicodeString(xmlBytesTopSection);
			
			if(isvalidTemplateXml(templateXMLToImportTopSection, templateXMLToImportBottomSection))
			{
				topFields = FieldCollection.parseXml(templateXMLToImportTopSection);
				bottomFields = FieldCollection.parseXml(templateXMLToImportBottomSection);
				return true;
			}
		}
		catch(IOException e)
		{
			errors.add(CustomFieldError.errorIO(e.getMessage()));
		}
		catch(MartusSignatureException e)
		{
			errors.add(CustomFieldError.errorSignature());
		}
		catch(AuthorizationFailedException e)
		{
			errors.add(CustomFieldError.errorUnauthorizedKey());
		}
		catch(CustomFieldsParseException e)
		{
			errors.add(CustomFieldError.errorParseXml(e.getMessage()));
		}
		finally
		{
			inputStreamWithSeek.close();
		}
		return false;
	}

	public Vector getSignedByAsVector(InputStreamWithSeek dataBundleSection, MartusCrypto security) throws MartusSignatureException, IOException
	{
		String signedBy = security.getSignedBundleSigner(dataBundleSection);
		return getSignedByAsVector(signedBy);
	}
	
	public Vector getSignedByAsVector(byte[] dataBundleSection, MartusCrypto security) throws MartusSignatureException, IOException
	{
		return getSignedByAsVector(new ByteArrayInputStreamWithSeek(dataBundleSection), security);
	}

	private Vector getSignedByAsVector(String signedBy)	throws MartusSignatureException
	{
		if(signedByPublicKey == null)
			signedByPublicKey = signedBy;
		else if(!signedByPublicKey.equals(signedBy))
			throw new MartusSignatureException();
		
		String[] authorizedKeysArray = new String[] { signedBy };
		Vector authorizedKeysVector = new Vector(Arrays.asList(authorizedKeysArray));
		return authorizedKeysVector;
	}
	
	public boolean isLegacyTemplateFile(InputStreamWithSeek in) throws IOException
	{
		byte[] versionHeaderInBytes = new byte[versionHeader.length()];
		in.read(versionHeaderInBytes);
		in.seek(0);
		String versionHeaderInString = new String(versionHeaderInBytes);
		return !versionHeaderInString.equals(versionHeader);
	}
	
	public void exportTemplate(MartusCrypto security, File fileToExportXml) throws Exception
	{
		FileOutputStream out = new FileOutputStream(fileToExportXml);
		try
		{
			saveContentsToOutputStream(security, out);
		} 
		finally
		{
			out.close();
		}
	}
	
	public void exportTopSection(File fileToExportIntoXml) throws Exception 
	{
		UnicodeWriter writer = new UnicodeWriter(fileToExportIntoXml);
		try
		{
			StringBuffer xmlToExport = new StringBuffer();
			xmlToExport.append(MartusXml.getXmlSchemaElement());
			xmlToExport.append(MartusXml.getTagStartWithNewline(MartusXml.FormTemplateElementName));
			xmlToExport.append(MartusXml.getTagWithData(MartusXml.TitleElementName, getTitle()));
			xmlToExport.append(getTopSectionXml());
			xmlToExport.append(MartusXml.getTagEnd(MartusXml.FormTemplateElementName));
			
			writer.append(xmlToExport.toString());
		} 
		finally
		{
			writer.flush();
			writer.close();
		}
	}

	public String getExportedTemplateAsBase64String(MartusCrypto security)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if(saveContentsToOutputStream(security, out))
			return StreamableBase64.encode(out.toByteArray());
		return "";
	}

	public boolean saveContentsToOutputStream(MartusCrypto security, OutputStream out)
	{
		try
		{
			DataOutputStream dataOut = new DataOutputStream(out);
			dataOut.write(versionHeader.getBytes());
			dataOut.writeInt(exportVersionNumber);
			byte[] signedBundleTopSection = security.createSignedBundle(UnicodeUtilities.toUnicodeBytes(getTopSectionXml()));
			byte[] signedBundleBottomSection = security.createSignedBundle(UnicodeUtilities.toUnicodeBytes(getBottomSectionXml()));
			byte[] signedBundleTitle = security.createSignedBundle(UnicodeUtilities.toUnicodeBytes(title));
			byte[] signedBundleDescription = security.createSignedBundle(UnicodeUtilities.toUnicodeBytes(description));
			dataOut.writeInt(signedBundleTopSection.length);
			dataOut.writeInt(signedBundleBottomSection.length);
			dataOut.writeInt(signedBundleTitle.length);
			dataOut.writeInt(signedBundleDescription.length);
			dataOut.write(signedBundleTopSection);
			dataOut.write(signedBundleBottomSection);
			dataOut.write(signedBundleTitle);
			dataOut.write(signedBundleDescription);
			dataOut.flush();
			dataOut.close();
			out.flush();
			out.close();
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isvalidTemplateXml()
	{
		return isvalidTemplateXml(getTopSectionXml(), getBottomSectionXml());
	}
	
	public boolean isvalidTemplateXml(String xmlToValidateTopSection, String xmlToValidateBottomSection)
	{
		try
		{
			FieldSpecCollection newSpecsTopSection = FieldCollection.parseXml(xmlToValidateTopSection);
			FieldSpecCollection newSpecsBottomSection = FieldCollection.parseXml(xmlToValidateBottomSection);
			CustomFieldSpecValidator checker = new CustomFieldSpecValidator(newSpecsTopSection, newSpecsBottomSection);
			if(checker.isValid())
				return true;
			errors.addAll(checker.getAllErrors());
		}
		catch (InvalidIsoDateException e)
		{
			MartusLogger.logException(e);
			errors.add(CustomFieldError.errorInvalidIsoDate(e.getTag(), e.getLabel(), e.getType()));
		}
		catch (CustomFieldsParseException e)
		{
			MartusLogger.logException(e);
			errors.add(CustomFieldError.errorParseXml(e.getMessage()));
		}
		return false;
	}
	
	public Vector getErrors()
	{
		return errors;
	}
	
	public FieldSpecCollection getTopFields()
	{
		return topFields;
	}

	public String getTopSectionXml()
	{
		if(topFields == null)
			return "";
		
		return topFields.toXml();
	}
	
	public void setTopFields(FieldSpecCollection newTopFields)
	{
		topFields = newTopFields;
	}

	public FieldSpecCollection getBottomFields()
	{
		return bottomFields;
	}

	public void setBottomFields(FieldSpecCollection newBottomFields)
	{
		bottomFields = newBottomFields;
	}

	public String getBottomSectionXml()
	{
		if(bottomFields == null)
			return "";
		
		return bottomFields.toXml();
	}

	public String getSignedBy()
	{
		return signedByPublicKey;
	} 
	
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String newTitle)
	{
		title = newTitle;
	}

	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String newDescription)
	{
		description = newDescription;
	}

	public FormTemplate createCopy() throws Exception
	{
		return new FormTemplate(getTitle(), getDescription(), getTopFields(), getBottomFields());
	}

	public static String calculateFileNameFromString(String inputText) throws CreateDigestException  
	{
		String extension = CUSTOMIZATION_TEMPLATE_EXTENSION;
		return calculateFileNameFromString(inputText, extension);
	}

	public static String calculateFileNameFromString(String inputText, String extension)
	{
		return MartusCrypto.getHexDigest(inputText) + extension;
	}
	
	public static String getDisplayableTemplateName(String rawName, MiniLocalization localization)
	{
		String displayableName = rawName;
		if(displayableName.equals(MARTUS_DEFAULT_FORM_TEMPLATE_NAME))
			displayableName = localization.getFieldLabel("DisplayableDefaultFormTemplateName");
		return displayableName;
	}

	public static final String MARTUS_DEFAULT_FORM_TEMPLATE_NAME = "";

	public static final String versionHeader = "Export Version Number:";
	public static final int exportVersionNumber = 3;
	public static final String CUSTOMIZATION_TEMPLATE_EXTENSION = ".mct";
	
	private Vector errors;
	private FieldSpecCollection topFields;
	private FieldSpecCollection bottomFields;
	private String signedByPublicKey;
	//Version 3
	private String title;
	private String description;
}
