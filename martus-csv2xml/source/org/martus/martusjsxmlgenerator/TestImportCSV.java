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

import java.io.ByteArrayOutputStream;
import java.io.File;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class TestImportCSV extends TestCaseEnhanced 
{
	public TestImportCSV(String name) 
	{
		super(name);
	}

	protected void setUp() throws Exception 
	{
		super.setUp();
		testJSFile = createTempFileFromName("$$$MARTUS_JS_TestFile");
		copyResourceFileToLocalFile(testJSFile, "test.js");
		testCSVFile = createTempFileFromName("$$$MARTUS_CSV_TestFile");
		copyResourceFileToLocalFile(testCSVFile, "test.csv");
		testGridCSVFile = new File("$$$griddata.csv");
		testGridCSVFile.deleteOnExit();
		copyResourceFileToLocalFile(testGridCSVFile, "griddata.csv");
		importer = new ImportCSV(testJSFile, testCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
		importer.getXmlFile().deleteOnExit();
		cs = Context.enter();
	}

	protected void tearDown() throws Exception 
	{
		super.tearDown();
		testJSFile.delete();
		assertFalse(testJSFile.exists());
		testCSVFile.delete();
		assertFalse(testCSVFile.exists());
		testGridCSVFile.delete();
		assertFalse(testGridCSVFile.exists());
		importer.getXmlFile().delete();
		assertFalse(importer.getXmlFile().exists());
		Context.exit();
	}
	
	public void testIncorrectDelimeter() throws Exception
	{
		try 
		{
			String INCORRECT_DELIMETER = ",";
			new ImportCSV(testJSFile, testCSVFile, INCORRECT_DELIMETER);
			fail("Should have thrown since the delimeter is incorrect");
		} 
		catch (Exception expected) 
		{
			assertContains("Only Found one column, please check your delimeter", expected.getMessage());
		}
	}

	public void testGetTabbedHeaders() throws Exception
	{

		File testCSVFileTabbed = createTempFileFromName("$$$MARTUS_CSV_TestFile_Tabbed");
		copyResourceFileToLocalFile(testCSVFileTabbed, "testTabHeaders.csv");
		ImportCSV importer2 = new ImportCSV(testJSFile, testCSVFileTabbed, "\t");
		assertEquals(5, importer2.headerLabels.length);
		testCSVFileTabbed.delete();
	}

	public void testGetHeaders() throws Exception
	{
		String[] headerLabels = importer.headerLabels;
		assertEquals(14, headerLabels.length);
		assertEquals("entrydate", headerLabels[0]);
		assertEquals("language", headerLabels[1]);
		assertEquals("author", headerLabels[2]);
		assertEquals("event_date_end", headerLabels[12]);
	}
	
	public void testHeaderCountDoesntMatchData() throws Exception
	{
		File testInvalidCSVFile = createTempFileFromName("$$$MARTUS_CSV_TestFile_HeaderCountDoesntMatchData");
		copyResourceFileToLocalFile(testInvalidCSVFile, "testInvalidcolumncount.csv");
		ImportCSV importer2 = new ImportCSV(testJSFile, testInvalidCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
		try 
		{
			importer2.doImport();
			fail("Should have thrown an exception");
		} 
		catch (Exception expected) 
		{
			assertContains("Row Data = en|John| Doe|Bulletin #1|Message 1|212|C.C.|no", expected.getMessage());
		}
		finally
		{
			testInvalidCSVFile.delete();
			importer2.getXmlFile().delete();
		}
	}

	public void testStringFields() throws Exception
	{
		UnicodeReader readerJSConfigurationFile = new UnicodeReader(testJSFile);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "20000101|fr|Dan Brown|Jane|Doe|16042001|Bulletin #2|Message 2|234|T.I..|yes|12032001|10222005|1";
		Scriptable fieldSpecs;
		try
		{
			importer.setupScopeAndExecuteScript(cs, script, scope);
			fieldSpecs = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		}
		finally
		{
			readerJSConfigurationFile.close();
			importer.cleanup(scope);
		}
		
		MartusField field1 = (MartusField)fieldSpecs.get(0, scope);
		
		assertEquals("Witness", field1.getTag());
		assertEquals("Witness", field1.getLabel());
		assertEquals("Jane Doe", field1.getMartusValue(scope));

		MartusField field2 = (MartusField)fieldSpecs.get(1, scope);
		assertEquals("WitnessComment", field2.getTag());
		assertEquals("Comment", field2.getLabel());
		assertEquals("Message 2", field2.getMartusValue(scope));

	}
	
	public void testSimpleDateFields() throws Exception
	{
		UnicodeReader readerJSConfigurationFile = new UnicodeReader(testJSFile);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "20000101|fr|Dan Brown|Jane|Doe|16042001|Bulletin #2|Message 2|234|T.I..|yes|12032001|10222005|1";
		Scriptable fieldSpecs;
		try
		{
			importer.setupScopeAndExecuteScript(cs, script, scope);
			fieldSpecs = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		}
		finally
		{
			readerJSConfigurationFile.close();
			importer.cleanup(scope);
		}
		
		MartusField dateField = (MartusField)fieldSpecs.get(5, scope);
		assertEquals("entrydate", dateField.getTag());
		assertEquals("", dateField.getLabel());
		assertEquals("Simple:2000-01-01", dateField.getMartusValue(scope));
	}

	public void testMartusDateFormat() throws Exception
	{
		File martusDateFormatJS = createTempFileFromName("$$$MARTUS_JS_MartusDefaultDateFormatFile");
		copyResourceFileToLocalFile(martusDateFormatJS, "martusDefaultDateFormat.js");

		UnicodeReader readerJSConfigurationFile = new UnicodeReader(martusDateFormatJS);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "2003-11-30|fr|Dan Brown|Jane|Doe|16042001|Bulletin #2|Message 2|234|T.I..|yes|12032001|10222005|1";
		Scriptable fieldSpecs;
		try
		{
			importer.setupScopeAndExecuteScript(cs, script, scope);
			fieldSpecs = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		}
		finally
		{
			readerJSConfigurationFile.close();
			importer.cleanup(scope);
		}
		martusDateFormatJS.delete();
		
		MartusField dateField = (MartusField)fieldSpecs.get(3, scope);
		assertEquals("entrydate", dateField.getTag());
		assertEquals("", dateField.getLabel());
		assertEquals("Simple:2003-11-30", dateField.getMartusValue(scope));
	}

	public void testDateRangeFields() throws Exception
	{
		UnicodeReader readerJSConfigurationFile = new UnicodeReader(testJSFile);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "20000101|fr|Dan Brown|Jane|Doe|16042001|Bulletin #2|Message 2|234|T.I..|yes|12032001|10222005|1";
		Scriptable fieldSpecs;
		try
		{
			importer.setupScopeAndExecuteScript(cs, script, scope);
			fieldSpecs = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		}
		finally
		{
			readerJSConfigurationFile.close();
			importer.cleanup(scope);
		}
		
		MartusField dateRangeField = (MartusField)fieldSpecs.get(11, scope);
		assertEquals("eventdate", dateRangeField.getTag());
		assertEquals("", dateRangeField.getLabel());
		assertEquals("Range:2001-12-03,2005-10-22", dateRangeField.getMartusValue(scope));

	}
	
	public void testDropDownValueNotPresent() throws Exception
	{
		UnicodeReader readerJSConfigurationFile = new UnicodeReader(testJSFile);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "20000101|fr|Dan Brown|Janice|Doe|16042001|Bulletin #2|Message 2|234|T.I..|entry not in list|12032001|10222005|1";

		importer.setupScopeAndExecuteScript(cs, script, scope);
		Scriptable bulletinData = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		ByteArrayOutputStream out = new ByteArrayOutputStream(2000);
		UnicodeWriter writer = new UnicodeWriter(out);
		try
		{
			importer.writeBulletinFieldData(writer, scope, bulletinData);
			fail("Should have thrown an expection for dropdown value not in field spec dropdown list");
		}
		catch(Exception expected)
		{
			assertContains("Dropdown value not in list", expected.getMessage());
		}
		finally
		{
			writer.close();
			out.close();
			readerJSConfigurationFile.close();
			importer.cleanup(scope);
		}
	}
	
	public void testBooleanValueNot1Or0() throws Exception
	{
		UnicodeReader readerJSConfigurationFile = new UnicodeReader(testJSFile);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "20000101|fr|Dan Brown|Janice|Doe|16042001|Bulletin #2|Message 2|234|T.I..|entry not in list|12032001|10222005|true";

		importer.setupScopeAndExecuteScript(cs, script, scope);
		Scriptable bulletinData = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		ByteArrayOutputStream out = new ByteArrayOutputStream(2000);
		UnicodeWriter writer = new UnicodeWriter(out);
		try
		{
			importer.writeBulletinFieldData(writer, scope, bulletinData);
			fail("Should have thrown an expection for dropdown value not in field spec dropdown list");
		}
		catch(Exception expected)
		{
			assertContains("Dropdown value not in list", expected.getMessage());
		}
		finally
		{
			writer.close();
			out.close();
			readerJSConfigurationFile.close();
			importer.cleanup(scope);
		}
	}

	

	public void testType() throws Exception
	{
		UnicodeReader readerJSConfigurationFile = new UnicodeReader(testJSFile);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "20000101|fr|Dan Brown|Jane|Doe|16042001|Bulletin #2|Message 2|234|T.I..|yes|12032001|10222005|1";
		Scriptable fieldSpecs;
		try
		{
			importer.setupScopeAndExecuteScript(cs, script, scope);
			fieldSpecs = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		}
		finally
		{
			readerJSConfigurationFile.close();
			importer.cleanup(scope);
		}
		
		assertEquals("STRING",((MartusField)fieldSpecs.get(0, scope)).getType());
		assertEquals("LANGUAGE",((MartusField)fieldSpecs.get(2, scope)).getType());
		assertEquals("DATE",((MartusField)fieldSpecs.get(5, scope)).getType());
		assertEquals("MULTILINE",((MartusField)fieldSpecs.get(9, scope)).getType());
		assertEquals("DATERANGE",((MartusField)fieldSpecs.get(11, scope)).getType());
		assertEquals("DROPDOWN",((MartusField)fieldSpecs.get(12, scope)).getType());
		assertEquals("BOOLEAN",((MartusField)fieldSpecs.get(13, scope)).getType());
		assertEquals("MESSAGE",((MartusField)fieldSpecs.get(15, scope)).getType());
		assertEquals("GRID",((MartusField)fieldSpecs.get(16, scope)).getType());
	}
	
	public void testMartusFieldSpec() throws Exception
	{
		UnicodeReader readerJSConfigurationFile = new UnicodeReader(testJSFile);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "20000101|fr|Dan Brown|Jane|Doe|16042001|Bulletin #2|Message 2|234|T.I..|Yes|12032001|10222005|1";

		importer.setupScopeAndExecuteScript(cs, script, scope);
		Scriptable bulletinData = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		ByteArrayOutputStream out = new ByteArrayOutputStream(2000);
		UnicodeWriter writer = new UnicodeWriter(out);
		try
		{
			importer.writeBulletinFieldSpecs(writer, scope, bulletinData);
		}
		finally
		{
			writer.close();
			out.close();
			readerJSConfigurationFile.close();
			importer.cleanup(scope);
		}
		assertEquals(MARTUS_PUBLIC_FIELD_SPEC + PRIVATE_FIELD_SPEC, out.toString());
		
	}
	
	public void testMartusXMLValues() throws Exception
	{
		UnicodeReader readerJSConfigurationFile = new UnicodeReader(testJSFile);
		Script script = cs.compileReader(readerJSConfigurationFile, testCSVFile.getName(), 1, null);
		ScriptableObject scope = cs.initStandardObjects();
		String dataRow = "20000101|fr|Dan Brown|Janice|Doe|16042001|Bulletin <#2>|Message 2&3|212|T.I..|Yes|12032001|10222005|1";

		importer.setupScopeAndExecuteScript(cs, script, scope);
		Scriptable bulletinData = importer.getFieldScriptableSpecsAndBulletinData(cs, script, scope, dataRow);
		ByteArrayOutputStream out = new ByteArrayOutputStream(2000);
		UnicodeWriter writer = new UnicodeWriter(out);
		try
		{
			importer.writeBulletinFieldData(writer, scope, bulletinData);
		}
		finally
		{
			writer.close();
			out.close();
			readerJSConfigurationFile.close();
			importer.cleanup(scope);
		}
		assertEquals(MARTUS_XML_VALUES, out.toString());
	}
	
	public void testImportMultipleBulletins()throws Exception
	{
		File testExpectedXMLFile = createTempFileFromName("$$$MARTUS_JS_testImportMultipleBulletins_EXPECTED");
		copyResourceFileToLocalFile(testExpectedXMLFile, "text_finalResult.xml");
		File xmlFile = importer.getXmlFile();
		try 
		{
			importer.doImport();
			UnicodeReader reader = new UnicodeReader(xmlFile);
			String data = reader.readAll();
			reader.close();
			
			UnicodeReader reader2 = new UnicodeReader(testExpectedXMLFile);
			String expectedData = reader2.readAll();
			reader2.close();
			
			assertEquals(expectedData,data);
		} 
		finally
		{
			testExpectedXMLFile.delete();
		}
	}
	
	public void testJSRequiredFields() throws Exception
	{
		File testJSFileMissingLanguage = createTempFileFromName("$$$MARTUS_JS_TestFile_MissingFields");
		copyResourceFileToLocalFile(testJSFileMissingLanguage, "missingFields.js");
		ImportCSV importer2 = null;
		try 
		{
			importer2 = new ImportCSV(testJSFileMissingLanguage, testCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
			importer2.getXmlFile().deleteOnExit();
			importer2.doImport();
			fail("Should have thrown exception for missing required function");
		} 
		catch (Exception expected) 
		{
			assertContains("MartusRequiredLanguageField missing.", expected.getMessage());
			assertContains("MartusRequiredAuthorField missing.", expected.getMessage());
			assertContains("MartusRequiredTitleField missing.", expected.getMessage());
			assertContains("MartusRequiredDateEntryField missing.", expected.getMessage());
			assertContains("MartusRequiredPrivateField missing.", expected.getMessage());
		}
		finally
		{
			importer2.getXmlFile().delete();
		}
	}
	
	public void testGridMultiLines() throws Exception
	{
		File testGridMultiLines = createTempFileFromName("$$$MARTUS_MULTILINE_GRID");
		copyResourceFileToLocalFile(testGridMultiLines, "multilineInsideGrid.js");
		ImportCSV importer2 = null;
		try 
		{
			importer2 = new ImportCSV(testGridMultiLines, testCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
			importer2.getXmlFile().deleteOnExit();
			importer2.doImport();
			fail("Should have thrown exception for having a multiLine inside a grid");
		} 
		catch (Exception expected) 
		{
			assertContains("Martus Grid Contains Multiline Field.", expected.getMessage());
		}
		finally
		{
			importer2.getXmlFile().delete();
		}
	}

	public void testGridMessageFields() throws Exception
	{
		File testGridMessageField = createTempFileFromName("$$$MARTUS_MESSAGE_FIELD_GRID");
		copyResourceFileToLocalFile(testGridMessageField, "messageFieldInsideGrid.js");
		ImportCSV importer2 = null;
		try 
		{
			importer2 = new ImportCSV(testGridMessageField, testCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
			importer2.getXmlFile().deleteOnExit();
			importer2.doImport();
			fail("Should have thrown exception for having a message inside a grid");
		} 
		catch (Exception expected) 
		{
			assertContains("Martus Grid Contains Message Field.", expected.getMessage());
		}
		finally
		{
			importer2.getXmlFile().delete();
		}
	}

	public void testGridInsideGrid() throws Exception
	{
		File testGridInsideGrid = createTempFileFromName("$$$MARTUS_GRID_INSIDE_GRID");
		copyResourceFileToLocalFile(testGridInsideGrid, "gridInsideGrid.js");
		ImportCSV importer2 = null;
		try 
		{
			importer2 = new ImportCSV(testGridInsideGrid, testCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
			importer2.getXmlFile().deleteOnExit();
			importer2.doImport();
			fail("Should have thrown exception for having a grid inside a grid");
		} 
		catch (Exception expected) 
		{
			assertContains("Martus Grid Contains Another Grid.", expected.getMessage());
		}
		finally
		{
			importer2.getXmlFile().delete();
		}
	}

	public void testMartusDefaultFieldInsideGrid() throws Exception
	{
		File testMartusDefaultFieldInsideGrid = createTempFileFromName("$$$MARTUS_DEFAULT_FIELD_INSIDE_GRID");
		copyResourceFileToLocalFile(testMartusDefaultFieldInsideGrid, "martusDefaultFieldInsideGrid.js");
		ImportCSV importer2 = null;
		try 
		{
			importer2 = new ImportCSV(testMartusDefaultFieldInsideGrid, testCSVFile, CSV_VERTICAL_BAR_REGEX_DELIMITER);
			importer2.getXmlFile().deleteOnExit();
			importer2.doImport();
			fail("Should have thrown exception for having a default Martus field inside a grid");
		} 
		catch (Exception expected) 
		{
			assertContains("Martus Grid Contains a Martus Default Field.", expected.getMessage());
		}
		finally
		{
			importer2.getXmlFile().delete();
		}
	}

	public void testImportBulletinsWithAttachments()throws Exception
	{
		File testExpectedXMLFile = createTempFileFromName("$$$MARTUS_JS_testImportBulletinsWithAttachments_EXPECTED");
		copyResourceFileToLocalFile(testExpectedXMLFile, "text_finalResultWithAttachments.xml");
		File testJSFileWithAttachments = createTempFileFromName("$$$MARTUS_JS_TestFile_With_Attachments");
		copyResourceFileToLocalFile(testJSFileWithAttachments, "testWithAttachments.js");
		File testCSVFileWithAttachments = createTempFileFromName("$$$MARTUS_CSV_TestFile_With_Attachments");
		copyResourceFileToLocalFile(testCSVFileWithAttachments, "testWithAttachments.csv");
		ImportCSV importerWithAttachments = new ImportCSV(testJSFileWithAttachments, testCSVFileWithAttachments, CSV_VERTICAL_BAR_REGEX_DELIMITER);
		importerWithAttachments.getXmlFile().deleteOnExit();
		cs = Context.enter();

		
		File xmlFile = importerWithAttachments.getXmlFile();
		try 
		{
			importerWithAttachments.doImport();
			UnicodeReader reader = new UnicodeReader(xmlFile);
			String data = reader.readAll();
			reader.close();
			
			UnicodeReader reader2 = new UnicodeReader(testExpectedXMLFile);
			String expectedData = reader2.readAll();
			reader2.close();
			
			assertEquals(expectedData,data);
		} 
		finally
		{
			testExpectedXMLFile.delete();
			testJSFileWithAttachments.delete();
			assertFalse(testJSFileWithAttachments.exists());
			testCSVFileWithAttachments.delete();
			assertFalse(testCSVFileWithAttachments.exists());
			importerWithAttachments.getXmlFile().delete();
			assertFalse(importerWithAttachments.getXmlFile().exists());
		}
	}
	
	
	File testJSFile;	
	File testCSVFile;
	File testGridCSVFile;
	ImportCSV importer;
	Context cs;	
	
	public final String CSV_VERTICAL_BAR_REGEX_DELIMITER = "\\|";
	public final String PRIVATE_FIELD_SPEC = 
		    "<PrivateFieldSpecs>\n"+
		    "<Field type='BOOLEAN'>\n"+
		    "<Tag>anonymous_tag_bottom_section</Tag>\n"+
		    "<Label>Does interviewee wish to remain anonymous?</Label>\n"+
		    "</Field>\n"+
			"<Field type='MULTILINE'>\n"+
			"<Tag>privateinfo</Tag>\n"+
			"<Label></Label>\n"+
			"</Field>\n"+
			"</PrivateFieldSpecs>\n\n";
	
	public final String MARTUS_PUBLIC_FIELD_SPEC =
		"<MartusBulletin>\n"+
		"<MainFieldSpecs>\n"+
		"<Field type='STRING'>\n"+
		"<Tag>Witness</Tag>\n"+
		"<Label>Witness</Label>\n"+
		"</Field>\n"+
		"<Field type='STRING'>\n"+
		"<Tag>WitnessComment</Tag>\n"+
		"<Label>Comment</Label>\n"+
		"</Field>\n"+
		"<Field type='LANGUAGE'>\n"+
		"<Tag>language</Tag>\n"+
		"<Label></Label>\n"+
		"</Field>\n"+
		"<Field type='STRING'>\n"+
		"<Tag>author</Tag>\n"+
		"<Label></Label>\n"+
		"</Field>\n"+
		"<Field type='STRING'>\n"+
		"<Tag>title</Tag>\n"+
		"<Label></Label>\n"+
		"</Field>\n"+
		"<Field type='DATE'>\n"+
		"<Tag>entrydate</Tag>\n"+
		"<Label></Label>\n"+
		"</Field>\n"+
		"<Field type='MULTILINE'>\n"+
		"<Tag>summary</Tag>\n"+
		"<Label></Label>\n"+
		"</Field>\n"+
		"<Field type='STRING'>\n"+
		"<Tag>location</Tag>\n"+
		"<Label></Label>\n"+
		"</Field>\n"+
		"<Field type='STRING'>\n"+
		"<Tag>organization</Tag>\n"+
		"<Label></Label>\n"+
		"</Field>\n"+
		"<Field type='MULTILINE'>\n"+
		"<Tag>publicinfo</Tag>\n"+
		"<Label></Label>\n"+
		"</Field>\n"+
		"<Field type='STRING'>\n"+
		"<Tag>keywords</Tag>\n"+
		"<Label></Label>\n"+
		"</Field>\n"+
		"<Field type='DATERANGE'>\n"+
		"<Tag>eventdate</Tag>\n"+
		"<Label></Label>\n"+
		"</Field>\n"+
		"<Field type='DROPDOWN'>\n"+
		"<Tag>gun_tag</Tag>\n"+
		"<Label>Where guns Used?</Label>\n"+
		"<Choices><Choice>Yes</Choice>\n"+
		"<Choice>No</Choice>\n"+
		"<Choice>Unknown</Choice>\n"+
		"</Choices>\n"+
		"</Field>\n"+
		"<Field type='MESSAGE'>\n"+
		"<Tag>MessageProfession</Tag>\n"+
		"<Label>Profession History Table Note</Label>\n"+
		"<Message>If you have &lt;information&gt; about a person who has had different professions over time, enter multiple rows with the same First and Last Names and show the date ranges for each profession on a separate row.</Message>\n"+
		"</Field>\n"+
		"<Field type='GRID'>\n"+
		"<Tag>GridTag</Tag>\n"+
		"<Label>Grid Lable</Label>\n"+
		"<GridSpecDetails>\n"+
		"<Column type='STRING'>\n"+
		"<Tag></Tag>\n"+
		"<Label>First &lt;Name&gt;</Label>\n"+
		"</Column>\n"+
		"<Column type='STRING'>\n"+
		"<Tag></Tag>\n"+
		"<Label>Last Name</Label>\n"+
		"</Column>\n"+
		"<Column type='DATE'>\n"+
		"<Tag></Tag>\n"+
		"<Label>Date of Birth</Label>\n"+
		"</Column>\n"+
		"<Column type='DATERANGE'>\n"+
		"<Tag></Tag>\n"+
		"<Label>Occured</Label>\n"+
		"</Column>\n"+
		"<Column type='DROPDOWN'>\n"+
		"<Tag></Tag>\n"+
		"<Label>Color Used</Label>\n"+
		"<Choices><Choice>red</Choice>\n"+
		"<Choice>&lt;yellow&gt;&amp;&lt;green&gt;</Choice>\n"+
		"<Choice>blue</Choice>\n"+
		"</Choices>\n"+
		"</Column>\n"+
		"<Column type='BOOLEAN'>\n"+
		"<Tag></Tag>\n"+
		"<Label>Occurred at Night?</Label>\n"+
		"</Column>\n"+
		"</GridSpecDetails>\n"+
		"</Field>\n"+
		"</MainFieldSpecs>\n\n";
	
	public final String MARTUS_XML_VALUES =
		"<FieldValues>\n" +
		"<Field tag='Witness'>\n" +
		"<Value>Janice Doe</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='WitnessComment'>\n" +
		"<Value>Message 2&amp;3</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='language'>\n" +
		"<Value>fr</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='author'>\n" +
		"<Value>Dan Brown</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='title'>\n" +
		"<Value>Bulletin &lt;#2&gt;</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='entrydate'>\n" +
		"<Value>Simple:2000-01-01</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='summary'>\n" +
		"<Value>T.I..</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='location'>\n" +
		"<Value>Message 2&amp;3</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='organization'>\n" +
		"<Value>XYZ NGO</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='publicinfo'>\n" +
		"<Value>212</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='keywords'>\n" +
		"<Value>212, T.I..</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='eventdate'>\n" +
		"<Value>Range:2001-12-03,2005-10-22</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='gun_tag'>\n" +
		"<Value>Yes</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='anonymous_tag_bottom_section'>\n" +
		"<Value>1</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='privateinfo'>\n" +
		"<Value>MY PRIVATE DATE = T.I..</Value>\n" +
		"</Field>\n\n" +
		"<Field tag='MessageProfession'>\n" +
		"</Field>\n\n" +
		"<Field tag='GridTag'>\n" +
		"<Value><GridData>\n" +
		"<Row>\n" +
		"<Column>John Grid</Column>\n" +
		"<Column>Doe</Column>\n" +
		"<Column>Simple:2000-11-01</Column>\n" +
		"<Column>Range:2002-01-01,2003-01-06</Column>\n" +
		"<Column>red</Column>\n" +
		"<Column>1</Column>\n" +
		"</Row>\n" +
		"<Row>\n" +
		"<Column>Jane Grid</Column>\n" +
		"<Column>Doe 2</Column>\n" +
		"<Column>Simple:2001-12-03</Column>\n" +
		"<Column>Range:2002-01-02,2003-01-08</Column>\n" +
		"<Column>blue</Column>\n" +
		"<Column>0</Column>\n" +
		"</Row>\n" +
		"</GridData>\n" +
		"</Value>\n" +
		"</Field>\n\n" +
		"</FieldValues>\n"+
		"</MartusBulletin>\n\n";

}
