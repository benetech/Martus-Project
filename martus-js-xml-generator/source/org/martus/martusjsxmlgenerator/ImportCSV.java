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

package org.martus.martusjsxmlgenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.martus.common.bulletin.BulletinXmlExportImportConstants;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
public class ImportCSV
{
	public static void main(String[] args) throws Exception
	{
		if(args.length != 3)
		{
			System.out.println("Usage : java ImportCSV configurationFile.js fileToConvert.csv csvDelimiterRegEx" );
			System.out.println("	 csvDelimiterRegEx = the regular expression java will use to split the csv file into its columns" );
			System.out.println("	 Egg. if the values are separated by a comma you may just use ," );
			System.out.println("	 Egg. if the values are separated by a tab you will need to use \"\\t\"" );
			System.out.println("	 Egg. if the values are separated by a | you will need to use \"\\|\"" );
			System.exit(1);
		}

		ImportCSV importer = new ImportCSV(new File(args[0]), new File(args[1]), args[2]);
		importer.doImport();
		if(importer.martusXmlFile.exists())
			System.out.println("Finished. "+ importer.martusXmlFile.getAbsolutePath() + " was created.");
		else
			System.out.println("Failed.");
	}
	
	public ImportCSV(File javaScriptFile, File csvFile, String csvDelimiterToUse) throws Exception
	{
		bulletinCsvFile = csvFile;
		configurationFile = javaScriptFile;
		csvDelimeter = csvDelimiterToUse;
		MartusField.clearRequiredFields();

		initalizeHeaderValues();
		String csvFileName = csvFile.getPath();
		String xmlFileName = csvFileName.substring(0, csvFileName.length()-4);
		xmlFileName += ".xml";
		martusXmlFile = new File(xmlFileName);
	}

	private void initalizeHeaderValues() throws Exception 
	{
		UnicodeReader csvHeaderReader = new UnicodeReader(bulletinCsvFile);
		String headerInfo = csvHeaderReader.readLine();
		csvHeaderReader.close();
		headerLabels = headerInfo.split(csvDelimeter);
		if(headerLabels.length == 1)
		{
			String errorMessage ="Only Found one column, please check your delimeter";
			throw new Exception(errorMessage);
		}
	}
	
	public File getXmlFile()
	{
		return martusXmlFile;
	}
	
	public void doImport() throws Exception
	{
		Context cs = Context.enter();
		UnicodeReader readerJSConfigurationFile = null;
		UnicodeWriter writer = null;
		UnicodeReader csvReader = null;
		ScriptableObject scope = null;
		try
		{
			readerJSConfigurationFile = new UnicodeReader(configurationFile);
			Script script = cs.compileReader(readerJSConfigurationFile, configurationFile.getName(), 1, null);
			scope = cs.initStandardObjects();
			
			writer = openMartusXML();
			
			String dataRow = null;
			csvReader = new UnicodeReader(bulletinCsvFile);
			csvReader.readLine(); //skip past header;
			setupScopeAndExecuteScript(cs, script, scope);

			while((dataRow = readRow(csvReader)) != null)
			{
				Scriptable bulletinData = getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
				writeBulletinFieldSpecs(writer, scope, bulletinData);
				writeBulletinFieldData(writer, scope, bulletinData);
			}
			closeMartusXML(writer);
		}
		finally
		{
			Context.exit();
			if(readerJSConfigurationFile != null)
				readerJSConfigurationFile.close();
			if(writer != null)
				writer.close();
			if(csvReader != null)
				csvReader.close();
			if(scope != null)
				cleanup(scope);
		}
	}

	public void cleanup(ScriptableObject scope) throws IOException
	{
		Scriptable fieldSpecs = (Scriptable)scope.get("MartusFieldSpecs", scope);
		for(int i = 0; i < fieldSpecs.getIds().length; ++i)
		{
			MartusField fieldSpec = (MartusField)fieldSpecs.get(i, scope);
			fieldSpec.cleanup();
		}
	}
	
	private String readRow(UnicodeReader reader) throws IOException
	{
		String row;
		do
		{
			row = reader.readLine();
		}while (row != null && row.trim().length() == 0);
		return row;
	}

	public void setupScopeAndExecuteScript(Context cs, Script script, ScriptableObject scope) throws IllegalAccessException, InstantiationException, InvocationTargetException
	{
		ScriptableObject.defineClass(scope, StringField.class);
		ScriptableObject.defineClass(scope, MultilineField.class);
		ScriptableObject.defineClass(scope, SingleDateField.class);
		ScriptableObject.defineClass(scope, DateRangeField.class);
		ScriptableObject.defineClass(scope, DropDownField.class);
		ScriptableObject.defineClass(scope, BooleanField.class);
		ScriptableObject.defineClass(scope, MessageField.class);
		ScriptableObject.defineClass(scope, GridField.class);
		ScriptableObject.defineClass(scope, MartusDetailsField.class);
		ScriptableObject.defineClass(scope, MartusSummaryField.class);
		ScriptableObject.defineClass(scope, MartusOrganizationField.class);
		ScriptableObject.defineClass(scope, MartusLocationField.class);
		ScriptableObject.defineClass(scope, MartusKeywordsField.class);
		ScriptableObject.defineClass(scope, MartusDateOfEventField.class);
		ScriptableObject.defineClass(scope, MartusRequiredLanguageField.class);
		ScriptableObject.defineClass(scope, MartusRequiredAuthorField.class);
		ScriptableObject.defineClass(scope, MartusRequiredTitleField.class);
		ScriptableObject.defineClass(scope, MartusRequiredDateCreatedField.class);
		ScriptableObject.defineClass(scope, MartusRequiredPrivateField.class);
		ScriptableObject.defineClass(scope, MartusTopSectionAttachments.class);
		ScriptableObject.defineClass(scope, MartusBottomSectionAttachments.class);
		
		script.exec(cs, scope);
	}

	private UnicodeWriter openMartusXML() throws IOException
	{
		UnicodeWriter writerMartusXMLBulletinFile = new UnicodeWriter(martusXmlFile);
		writerMartusXMLBulletinFile.write(MartusField.getStartTagNewLine(BulletinXmlExportImportConstants.MARTUS_BULLETINS));
		return writerMartusXMLBulletinFile;
	}

	private void closeMartusXML(UnicodeWriter writerMartusXMLBulletinFile) throws IOException
	{
		writerMartusXMLBulletinFile.write(MartusField.getEndTag(BulletinXmlExportImportConstants.MARTUS_BULLETINS));
	}

	public void writeBulletinFieldSpecs(UnicodeWriter writer, ScriptableObject scope, Scriptable fieldSpecs) throws Exception
	{
		writer.write(MartusField.getStartTagNewLine(BulletinXmlExportImportConstants.MARTUS_BULLETIN));
		writeTopSectionFieldSpecs(writer, scope, fieldSpecs);
		writeBotomSectionFieldSpecs(writer, scope, fieldSpecs);
	}

	private void writeBotomSectionFieldSpecs(UnicodeWriter writer, ScriptableObject scope, Scriptable fieldSpecs) throws IOException, Exception
	{
		writer.write(MartusField.getStartTagNewLine(BulletinXmlExportImportConstants.PRIVATE_FIELD_SPECS));
		for(int i = 0; i < fieldSpecs.getIds().length; i++)
		{
			MartusField fieldSpec = (MartusField)fieldSpecs.get(i, scope);
			if(!fieldSpec.isBottomSectionField())
				continue;
			if(fieldSpec.getType().equals(MartusField.ATTACHMENT_TYPE))
				continue;//Attachments are not included in the Field Spec
			writer.write(fieldSpec.getFieldSpec(scope));
		}
		writer.write(MartusField.getEndTagWithExtraNewLine(BulletinXmlExportImportConstants.PRIVATE_FIELD_SPECS));
	}

	private void writeTopSectionFieldSpecs(UnicodeWriter writer, ScriptableObject scope, Scriptable fieldSpecs) throws IOException, Exception
	{
		writer.write(MartusField.getStartTagNewLine(BulletinXmlExportImportConstants.MAIN_FIELD_SPECS));

		for(int i = 0; i < fieldSpecs.getIds().length; i++)
		{
			MartusField fieldSpec = (MartusField)fieldSpecs.get(i, scope);
			if(fieldSpec.isBottomSectionField())
				continue;//Writen after the Public Field Spec
			if(fieldSpec.getType().equals(MartusField.ATTACHMENT_TYPE))
				continue;//Attachments are not included in the Field Spec
			writer.write(fieldSpec.getFieldSpec(scope));
		}
		writer.write(MartusField.getEndTagWithExtraNewLine(BulletinXmlExportImportConstants.MAIN_FIELD_SPECS));
	}

	public void writeBulletinFieldData(UnicodeWriter writer, ScriptableObject scope, Scriptable fieldSpecs) throws Exception
	{
		writer.write(MartusField.getStartTagNewLine(BulletinXmlExportImportConstants.FIELD_VALUES));
		
		for(int i = 0; i < fieldSpecs.getIds().length; i++)
		{
			MartusField fieldSpec = (MartusField)fieldSpecs.get(i, scope);
			writer.write(fieldSpec.getFieldData(scope));
		}
		writer.write(MartusField.getEndTag(BulletinXmlExportImportConstants.FIELD_VALUES));
		writer.write(MartusField.getEndTagWithExtraNewLine(BulletinXmlExportImportConstants.MARTUS_BULLETIN));
	}

	public Scriptable getFieldScriptableSpecsAndBulletinData(Context cs, Script script, ScriptableObject scope, String dataRow) throws Exception, IllegalAccessException, InstantiationException, InvocationTargetException 
	{
		String[] rowContents = dataRow.split(csvDelimeter, INCLUDE_BLANK_TRAILING_COLUMNS);
		if(rowContents.length != headerLabels.length)
		{
			String errorMessage ="Number of Data Fields did not match Header Fields\n" +
					"Expected column count =" + headerLabels.length + " but was :" + rowContents.length +"\n" +
					"Row Data = " + dataRow;
			throw new Exception(errorMessage);
		}
		
		for(int i = 0; i < rowContents.length; ++i)
		{
			scope.put(headerLabels[i], scope,rowContents[i]);
		}

		MartusField.verifyRequiredFields();
		Scriptable fieldSpecs = (Scriptable)scope.get("MartusFieldSpecs", scope);
		
		return fieldSpecs;
	}

	private static final int INCLUDE_BLANK_TRAILING_COLUMNS = -1;
	File configurationFile;
	File martusXmlFile;
	File bulletinCsvFile;
	private String csvDelimeter;
	String[] headerLabels;
}
