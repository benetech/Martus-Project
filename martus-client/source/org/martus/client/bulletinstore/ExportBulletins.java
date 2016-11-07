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
package org.martus.client.bulletinstore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.martus.client.bulletinstore.converters.BulletinsAsXmlToCsvConverter;
import org.martus.client.bulletinstore.converters.BulletinsAsXmlToJsonConverter;
import org.martus.client.core.BulletinXmlExporter;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.filefilters.CsvFileFilter;
import org.martus.client.swingui.filefilters.JsonFileFilter;
import org.martus.client.swingui.filefilters.XmlFileFilter;
import org.martus.common.MartusLogger;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.util.UnicodeWriter;
import org.xml.sax.InputSource;

import javafx.collections.ObservableSet;

public class ExportBulletins extends AbstractExport
{
	public ExportBulletins(UiMainWindow mainWindowToUse, ProgressMeterInterface progressDlgToUse)
	{
		super(mainWindowToUse, progressDlgToUse);
	}
	
	@Override
	public void doExport(File destFile, Vector bulletinsToUse) 
	{
		destinationFile = destFile;
		bulletinsToExport = bulletinsToUse;
		ExporterThread exporterThread = new ExporterThread(getProgressDlg());
		exporterThread.start();
	}
	
	protected void updateExportMessage(ExporterThread exporterThread,
			int bulletinsExported, int numberOfMissingAttachment) 
	{
		setExportErrorMessage("ExportComplete");
		if(exporterThread.didUnrecoverableErrorOccur())
		{
			setExportErrorMessage("ErrorExportingBulletins");
			setErrorOccured(true);
		}
		else if(numberOfMissingAttachment > 0)
		{
			setExportErrorMessage("ExportCompleteMissingAttachments");
			setErrorOccured(true);
		}
		setExportErrorMessageTokensMap(getTokenReplacementImporter(bulletinsExported, bulletinsToExport.size(), numberOfMissingAttachment));
	}

	Map getTokenReplacementImporter(int numberOfBulletinsExported, int totalNumberOfBulletins, int numberOfMissingAttachment) 
	{
		HashMap map = new HashMap();
		map.put("#BulletinsExported#", Integer.toString(numberOfBulletinsExported));
		map.put("#TotalBulletinsToExport#", Integer.toString(totalNumberOfBulletins));
		map.put("#AttachmentsNotExported#", Integer.toString(numberOfMissingAttachment));
		return map;
	}

	
	class ExporterThread extends Thread
	{
		public ExporterThread(ProgressMeterInterface progressRetrieveDlgToUse)
		{
			clientStore = getMainWindow().getStore();
			progressMeter = progressRetrieveDlgToUse;
			exporter = new BulletinXmlExporter(getMainWindow().getApp(), getMainWindow().getLocalization(), progressMeter);
		}

		@Override
		public void run()
		{
			try
			{
				MartusLocalization localization = getMainWindow().getLocalization();
				if (new XmlFileFilter(localization).accept(destinationFile))
				{
					possiblyCreateDestinationFile();
					exportAsXml(exportAsXml());
				}
				
				if (new JsonFileFilter(localization).accept(destinationFile))
				{
					possiblyCreateDestinationFile();
					exportAsJson(exportAsXml());
				}
				
				if (new CsvFileFilter(localization).accept(destinationFile))
				{
					exportAsCsvs();
				}
				
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
				errorOccured = true;
			}
			finally
			{
				int numberOfMissingAttachment = getNumberOfFailingAttachments();
				int bulletinsExported = getNumberOfBulletinsExported();
				updateExportMessage(this, bulletinsExported, numberOfMissingAttachment);
				progressMeter.finished();
			}
		}

		private void possiblyCreateDestinationFile() throws Exception
		{
			if(!destinationFile.createNewFile())
				throw new FileAlreadyExistsException(destinationFile.getAbsolutePath());
		}

		private void exportAsXml(String xmlAsString) throws Exception
		{
			UnicodeWriter xmlWriter = null;
			try 
			{
				xmlWriter = new UnicodeWriter(destinationFile);
				xmlWriter.append(xmlAsString);
			}
			finally
			{
				if (xmlWriter != null)
					xmlWriter.close();
			}
		}

		private String exportAsXml() throws IOException, Exception
		{
			return exportBulletins(bulletinsToExport, destinationFile.getParentFile(), false);
		}

		private String exportBulletins(Vector bulletinsToExportToUse, File parentDir, boolean shouldGroupAttachments) throws Exception
		{
			StringWriter writer = new StringWriter();
			exporter.exportBulletins(writer, bulletinsToExportToUse, isExportPrivate(), isExportAttachments(), isExportAllVersions(), parentDir, shouldGroupAttachments);
			writer.close();
			
			return writer.toString();
		}

		private void exportAsCsvs() throws Exception
		{
			HashMap<String, Vector<Bulletin>> formTemplateTitleToBulletinMap = createFormTemplateTitleToBulletinMap();			
			exportBulletinsInTemplateDirs(formTemplateTitleToBulletinMap);
		}

		private HashMap<String, Vector<Bulletin>> createFormTemplateTitleToBulletinMap() throws Exception
		{
			HashMap<String, Vector<Bulletin>> formTemplateTitleToBulletinMap = new HashMap<>();
			for (int index = 0; index < bulletinsToExport.size(); ++index) 
			{
				Bulletin bulletinToExport = (Bulletin) bulletinsToExport.get(index);
				String formTemplateTitle = findMatchingFormTemplate(bulletinToExport);
				String sanitizedFormTemplateTitle = sanitizeTemplateTitle(formTemplateTitle);
				if (!formTemplateTitleToBulletinMap.containsKey(sanitizedFormTemplateTitle))
				{					
					formTemplateTitleToBulletinMap.put(sanitizedFormTemplateTitle, new Vector<Bulletin>());
				}
				
				formTemplateTitleToBulletinMap.get(sanitizedFormTemplateTitle).add(bulletinToExport);
			}
			
			return formTemplateTitleToBulletinMap;
		}

		private String sanitizeTemplateTitle(String formTemplateTitle)
		{
			String sanitizedTitle = formTemplateTitle.replaceAll("/", "_");
			formTemplateTitle.replaceAll("\\\\", "_");
			
			return sanitizedTitle;
		}

		private void exportBulletinsInTemplateDirs(HashMap<String, Vector<Bulletin>> formTemplateTitleToBulletinMap) throws Exception
		{
			Set<String> formTemplateTitlesAsKeys = formTemplateTitleToBulletinMap.keySet();
			for (String formTemplateTitleAsKey : formTemplateTitlesAsKeys)
			{
				Vector<Bulletin> bulletinsToExportForTemplate = formTemplateTitleToBulletinMap.get(formTemplateTitleAsKey);
				exportBulletins(formTemplateTitleAsKey, bulletinsToExportForTemplate);
			}
		}

		private void exportBulletins(String formTemplateTitle, Vector<Bulletin> bulletinsToExportForTemplate) throws Exception
		{
			File destinationDir = findOrCreateDir(formTemplateTitle);
			File csvDestinationFile = new File(destinationDir, destinationFile.getName());
			String bulletinsAsXml = exportBulletins(bulletinsToExportForTemplate, destinationDir, true);
			InputSource xmlAsInputSource = new InputSource(new StringReader(bulletinsAsXml));
			BulletinsAsXmlToCsvConverter xmlToCsvConverter = new BulletinsAsXmlToCsvConverter(xmlAsInputSource, csvDestinationFile.getAbsolutePath());
			String errorMessagesDuringParsing = xmlToCsvConverter.parseAndTranslateFile();
			if (!errorMessagesDuringParsing.isEmpty())
			{
				setExportErrorMessage("ExportCompleteMissingAttachments");
				setErrorOccured(true);
				
				return;
			}
			
			HashMap<String, String> gridResults = xmlToCsvConverter.getAllGridInfo();
			saveFile(csvDestinationFile, xmlToCsvConverter.getCsvAsStringOutput());
			if (gridResults != null)
				saveGridFiles(csvDestinationFile.getAbsolutePath(),  MartusApp.CSV_EXTENSION, gridResults);

		}

		private String findMatchingFormTemplate(Bulletin bulletinToExport) throws Exception
		{
			Set bulletinTopSectionFieldSpecs = bulletinToExport.getTopSectionFieldSpecs().asSet();
			ObservableSet<String> existingTemplateTitles = getMainWindow().getStore().getAvailableTemplates();
			for (String templateTitle : existingTemplateTitles)
			{
				String parentDirName = getParentDirName(bulletinTopSectionFieldSpecs, templateTitle);
				System.out.println(templateTitle + ":   parent dir name1 " + parentDirName);
				if (parentDirName != null)
					return parentDirName;
			}

			return "RecordsWithoutMatchingTemplates";
		}

		private String getParentDirName(Set bulletinTopSectionFieldSpecs, String formTemplateTitle) throws Exception
		{
			FormTemplate formTemplate = getMainWindow().getStore().getFormTemplate(formTemplateTitle);
			Set formTemplateFieldSpecs = formTemplate.getTopFields().asSet();
			boolean topFieldSpecsAreEqual = areFieldSpecsEqual(bulletinTopSectionFieldSpecs, formTemplateFieldSpecs);
			if (topFieldSpecsAreEqual)
				return getPossibleStandardFieldsTitle(formTemplateTitle);
			
			return null;
		}

		private String getPossibleStandardFieldsTitle(String formTemplateTitle)
		{
			if (formTemplateTitle.equals(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME))
			{
				String displayableName = getMainWindow().getLocalization().getFieldLabel("DisplayableDefaultFormTemplateName");
				final String SINGLE_SPACE = " ";
				return displayableName.replace(SINGLE_SPACE, "");
			}
			
			return formTemplateTitle;
		}

		private boolean areFieldSpecsEqual(Set bulletinTopSectionFieldSpecs, Set formTemplateFieldSpecs)
		{
			formTemplateFieldSpecs.remove(FieldSpec.createStandardField(BulletinConstants.TAGPRIVATEINFO, new FieldTypeMultiline()));
			bulletinTopSectionFieldSpecs.remove(FieldSpec.createStandardField(BulletinConstants.TAGPRIVATEINFO, new FieldTypeMultiline()));
			
			return bulletinTopSectionFieldSpecs.equals(formTemplateFieldSpecs);
		}

		private File findOrCreateDir(String formTemplateTitle)
		{
			File destinationDir = new File(destinationFile.getParentFile(), formTemplateTitle);
			if (!destinationDir.exists())
				destinationDir.mkdir();

			return destinationDir;
		}
		
		private void exportAsJson(String xmlAsString)
		{
			BulletinsAsXmlToJsonConverter converter = new BulletinsAsXmlToJsonConverter();
			String bulletinsAsJson = converter.convertToJson(xmlAsString);
			saveFile(destinationFile, bulletinsAsJson);
		}

		private void saveFile(File outFile, String resultStr)
		{
			try
			{
				writeResultToFile(outFile, resultStr);
			} 
			catch (Exception e) 
			{
				MartusLogger.logException(e);
				setErrorOccured(true);
			}
		}

		private void saveGridFiles(String basePathName, String ext, HashMap<String, String> gridResult)
		{
			for (String gridName : gridResult.keySet()) 
			{
				try
				{
					String gridFileName = basePathName.replace(ext, "-" + gridName + ext);
					File gridFile = new File(gridFileName);
					writeResultToFile(gridFile, gridResult.get(gridName));
				} 
				catch (Exception e) 
				{
					MartusLogger.logException(e);
					setErrorOccured(true);
				}
			}
		}

		private void writeResultToFile(File gridFile, String resultStr) throws IOException
		{
			BufferedWriter writer = null;
			try
			{
				writer = Files.newBufferedWriter(gridFile.toPath(), StandardCharsets.UTF_8, new OpenOption[0]);
				writer.write(resultStr);
			}
			finally 
			{
				if (writer != null)
					writer.close();
			}
		}

		public int getNumberOfBulletinsExported()
		{
			return exporter.getNumberOfBulletinsExported();
		}
		
		public int getNumberOfFailingAttachments()
		{
			return exporter.getNumberOfFailingAttachments();
		}

		public boolean didUnrecoverableErrorOccur()
		{
			return errorOccured;
		}
		
		ProgressMeterInterface progressMeter;
		ClientBulletinStore clientStore;
		private BulletinXmlExporter exporter;
		private boolean errorOccured;
	}	

	Map getTokenReplacementImporter(int bulletinsImported, int totalBulletins, String folder) 
	{
		HashMap map = new HashMap();
		map.put("#BulletinsSuccessfullyImported#", Integer.toString(bulletinsImported));
		map.put("#TotalBulletinsToImport#", Integer.toString(totalBulletins));
		map.put("#ImportFolder#", folder);
		return map;
	}
		
	protected File destinationFile;
	protected Vector bulletinsToExport;
}


