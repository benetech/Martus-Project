/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.client.core.templates;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.miradi.utils.EnhancedJsonObject;

public class FormTemplateManager
{
	public static FormTemplateManager createOrOpen(MartusCrypto cryptoToUse, File directoryToUse) throws Exception
	{
		directoryToUse.mkdirs();
		
		FormTemplateManager formTemplateManager = new FormTemplateManager(cryptoToUse, directoryToUse);
		
		return formTemplateManager;
	}
	
	private FormTemplateManager(MartusCrypto cryptoToUse, File directoryToUse) throws Exception
	{
		if(!directoryToUse.isDirectory())
			throw new FileNotFoundException("No such directory: " + directoryToUse.getAbsolutePath());
		
		security = cryptoToUse;
		directory = directoryToUse;

		templateNames = FXCollections.observableSet();
		templateNames.addAll(loadTemplateNames());
		currentTemplateName = new SimpleStringProperty(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME);
		
		try
		{
			loadState();
		}
		catch(Exception e)
		{
			throw new UnableToLoadCurrentTemplateException(e);
		}
	}
	
	public static class UnableToLoadCurrentTemplateException extends Exception
	{
		public UnableToLoadCurrentTemplateException(Exception causedBy)
		{
			super (causedBy);
		}
		
	}
	
	public Property<String> getCurrentFormTemplateNameProperty()
	{
		return currentTemplateName;
	}

	public FormTemplate getCurrentFormTemplate() throws Exception
	{
		return getTemplate(currentTemplateName.getValue());
	}

	public void putTemplate(FormTemplate template) throws Exception
	{
		if(template.getTitle().length() == 0)
			throw new InvalidTemplateNameException("Name cannot be blank");
		
		saveEncryptedTemplate(template);
	}
	
	public FormTemplate getTemplate(String title) throws Exception
	{
		if(title.equals(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME))
			return createDefaultFormTemplate();
		
		return loadEncryptedTemplate(getTemplateFile(title));
	}

	public FormTemplate getMartusDefaultTemplate() throws Exception
	{
		return getTemplate(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME);
	}

	public static FormTemplate createDefaultFormTemplate() throws Exception
	{
		FieldSpecCollection top = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection bottom = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		FormTemplate template = new FormTemplate("", "", top, bottom);
		return template;
	}

	public Set<String> getAvailableTemplateNames() throws Exception
	{
		return templateNames;
	}
	
	public ObservableSet<String> getAvailableTemplatesProperty()
	{
		// NOTE: Make a copy so nobody messes with the official data
		return FXCollections.observableSet(templateNames);
	}

	public Set<String> loadTemplateNames() throws Exception
	{
		HashSet<String> available = new HashSet<String>();
		available.add(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME);
		
		File[] emctFiles = directory.listFiles(file -> isEmctFile(file));
		for (File file : emctFiles)
		{
			try
			{
				FormTemplate template = loadEncryptedTemplate(file);
				available.add(template.getTitle());
			}
			catch(Exception e)
			{
				MartusLogger.logException(e);
			}
		}
		return available;
	}

	public void setCurrentFormTemplate(String newCurrentTitle) throws Exception
	{
		if(!doesTemplateExist(newCurrentTitle))
			throw new FileNotFoundException("No such template: " + newCurrentTitle);

		currentTemplateName.setValue(newCurrentTitle);
		saveState();
	}

	public void deleteTemplate(String templateName) throws Exception
	{
		if(templateName.equals(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME))
			throw new InvalidTemplateNameException("Cannot delete the default template");
		
		if(!doesTemplateExist(templateName))
			throw new FileNotFoundException("Attempted to delete non-existent template: " + templateName);
		
		if(getCurrentFormTemplateNameProperty().getValue().equals(templateName))
			setCurrentFormTemplate(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME);
		
		File file = getTemplateFile(templateName);
		file.delete();
		if(file.exists())
			throw new IOException("Failed to delete template: " + templateName);
		
		templateNames.remove(templateName);
	}

	private void saveState() throws Exception
	{
		String currentTemplateFilename = getCurrentTemplateFilename();
		EnhancedJsonObject json = new EnhancedJsonObject();
		json.put(JSON_CURRENT_TEMPLATE_FILENAME, currentTemplateFilename);
		byte[] plainTextBytes = json.toString().getBytes("UTF-8");
		File file = getCurrentTemplateFilenameFile();
		File signatureFile = getSignatureFileFor(file);
		security.encryptAndWriteFileAndSignatureFile(file, signatureFile, plainTextBytes);
	}

	public String getCurrentTemplateFilename()
	{
		String currentTemplateTitle = currentTemplateName.getValue();
		if(currentTemplateTitle.equals(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME))
			return "";
		
		return getTemplateFile(currentTemplateTitle).getName();
	}

	private void loadState() throws Exception
	{
		File file = getCurrentTemplateFilenameFile();
		if(!file.exists())
			return;
		
		File signatureFile = getSignatureFileFor(file);
		byte[] plainTextBytes = MartusSecurity.verifySignatureAndDecryptFile(file, signatureFile, security);
		String jsonAsText = new String(plainTextBytes, "UTF-8");
		EnhancedJsonObject json = new EnhancedJsonObject(jsonAsText);
		String savedCurrentTemplateFilename = json.optString(JSON_CURRENT_TEMPLATE_FILENAME);

		setCurrentFormTemplate(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME);
		if(savedCurrentTemplateFilename.equals(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME))
			return;
		
		File templateFile = new File(directory, savedCurrentTemplateFilename);
		String savedCurrentTemplateName = loadEncryptedTemplate(templateFile).getTitle();
		setCurrentFormTemplate(savedCurrentTemplateName	);
	}
	
	private File getCurrentTemplateFilenameFile()
	{
		return new File(directory, CURRENT_STATUS_FILENAME);
	}
	
	private boolean doesTemplateExist(String title)
	{
		return templateNames.contains(title);
	}

	private FormTemplate loadEncryptedTemplate(File dataFile) throws Exception
	{
		File sigFile = getSignatureFileFor(dataFile);
		byte[] plaintextTemplateBytes = MartusSecurity.verifySignatureAndDecryptFile(dataFile, sigFile, security);
		ByteArrayInputStreamWithSeek plainTextTemplateBytesIn = new ByteArrayInputStreamWithSeek(plaintextTemplateBytes);
		FormTemplate template = new FormTemplate();
		template.importTemplate(security, plainTextTemplateBytesIn);
		return template;
	}

	public static File getSignatureFileFor(File dataFile)
	{
		File sigFile = new File(dataFile.getParentFile(), dataFile.getName() + SIG_EXTENSION);
		return sigFile;
	}

	private boolean isEmctFile(File file)
	{
		return file.getName().toLowerCase().endsWith(ENCRYPTED_MCT_EXTENSION);
	}
	
	private void saveEncryptedTemplate(FormTemplate formTemplate) throws Exception
	{
		ByteArrayOutputStream plaintextSignedBytesOut = new ByteArrayOutputStream();
		formTemplate.saveContentsToOutputStream(security, plaintextSignedBytesOut);
		plaintextSignedBytesOut.close();
		byte[] plaintextSignedBytes = plaintextSignedBytesOut.toByteArray();

		String title = formTemplate.getTitle();
		File file = getTemplateFile(title);
		File signatureFile = getSignatureFileFor(file);
		security.encryptAndWriteFileAndSignatureFile(file, signatureFile, plaintextSignedBytes);
		
		templateNames.add(title);
	}

	private File getTemplateFile(String title)
	{
		return new File(directory, getTemplateFilename(title));
	}

	public static String getTemplateFilename(String title)
	{
		return FormTemplate.calculateFileNameFromString(title, ENCRYPTED_MCT_EXTENSION);
	}

	public static class InvalidTemplateNameException extends Exception
	{
		public InvalidTemplateNameException(String message)
		{
			super(message);
		}
	}

	private static final String CURRENT_STATUS_FILENAME = "Status.dat";
	private static final String JSON_CURRENT_TEMPLATE_FILENAME = "CurrentTemplateFilename";
	private static final String ENCRYPTED_MCT_EXTENSION = ".emct";
	private static final String SIG_EXTENSION = ".sig";
	
	private MartusCrypto security;
	private File directory;
	private ObservableSet<String> templateNames;
	private Property<String> currentTemplateName;
}
