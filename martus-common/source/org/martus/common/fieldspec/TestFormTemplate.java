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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.martus.common.FieldCollection;
import org.martus.common.FieldCollectionForTesting;
import org.martus.common.FieldSpecCollection;
import org.martus.common.LegacyCustomFields;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.FormTemplate.FutureVersionException;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeUtilities;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;

public class TestFormTemplate extends TestCaseEnhanced
{
	public TestFormTemplate(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		if(security == null)
		{
			security = new MockMartusSecurity();
			security.createKeyPair(512);
		}
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testBasics() throws Exception
	{
		assertEquals("Version number not correct?", 3, FormTemplate.exportVersionNumber);
		
		String formTemplateTitle = "New Form Title";
		String formTemplateDescription = "New Form Description";
		FieldSpecCollection defaultFieldsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection defaultFieldsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		FormTemplate template = new FormTemplate(formTemplateTitle, formTemplateDescription, defaultFieldsTopSection, defaultFieldsBottomSection);
		
		assertEquals(formTemplateTitle, template.getTitle());
		assertEquals(formTemplateTitle, template.toString());
		assertEquals(formTemplateDescription, template.getDescription());
	}
	
	public void testValidateXml() throws Exception
	{
		FieldCollection defaultTopSectionFields = new FieldCollection(StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray());
		FieldCollection defaultBottomSectionFields = new FieldCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray());
		FormTemplate template = new FormTemplate();
		assertTrue("not valid?", template.isvalidTemplateXml(defaultTopSectionFields.toString(), defaultBottomSectionFields.toString()));
		assertEquals(0, template.getErrors().size());
		
		FieldSpec invalidTopSectionField = FieldSpec.createCustomField("myTag", "", new FieldTypeNormal());
		FieldCollection fields = FieldCollectionForTesting.extendFields(StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray(), invalidTopSectionField);
		assertFalse("Should not be a valid template", template.isvalidTemplateXml(fields.toString(), defaultBottomSectionFields.toString()));
		assertEquals(1, template.getErrors().size());
		assertEquals(CustomFieldError.CODE_MISSING_LABEL,((CustomFieldError)template.getErrors().get(0)).getCode());

		FormTemplate template2 = new FormTemplate();
		FieldSpec invalidBottomSectionField = FieldSpec.createCustomField("myTag", "", new FieldTypeNormal());
		fields = FieldCollectionForTesting.extendFields(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray(), invalidBottomSectionField);
		assertFalse("Should not be a valid template", template2.isvalidTemplateXml(defaultTopSectionFields.toString(), fields.toString()));
		assertEquals(1, template2.getErrors().size());
		assertEquals(CustomFieldError.CODE_MISSING_LABEL,((CustomFieldError)template2.getErrors().get(0)).getCode());
	}
	
	public void testExportXml() throws Exception
	{
		File exportFile = createTempFileFromName("$$$testExportXml");
		exportFile.deleteOnExit();
		String formTemplateTitle = "New Form Title";
		String formTemplateDescription = "New Form Description";
		FieldSpecCollection defaultFieldsTopSection = new FieldSpecCollection(StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray());
		FieldSpecCollection defaultFieldsBottomSection = new FieldSpecCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray());
		FormTemplate template = new FormTemplate(formTemplateTitle, formTemplateDescription, defaultFieldsTopSection, defaultFieldsBottomSection);
		template.exportTemplate(security, exportFile);
		assertTrue(exportFile.exists());
		FormTemplate importedTemplate = new FormTemplate();
		importTemplate(importedTemplate, exportFile);
		File exportFile2 = createTempFileFromName("$$$testExportXml2");
		exportFile2.deleteOnExit();
		importedTemplate.exportTemplate(security, exportFile2);
		FormTemplate importedTemplate2 = new FormTemplate();
		importTemplate(importedTemplate2, exportFile2);
		assertEquals("imported file 1 does not match imported file 2?", importedTemplate.getExportedTemplateAsBase64String(security), importedTemplate2.getExportedTemplateAsBase64String(security));
		
		exportFile.delete();
		exportFile2.delete();
	}
	
	public void testImportedTemplateWithDifferentSignedSections() throws Exception
	{
		String formTemplateTitle = "New Form Title";
		String formTemplateDescription = "New Form Description";
		FieldSpecCollection defaultFieldsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection defaultFieldsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();

		File exportMultipleSignersCFTFile = createTempFileFromName("$$$testExportMultipleSignersXml");
		exportMultipleSignersCFTFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(exportMultipleSignersCFTFile);		
		DataOutputStream dataOut = new DataOutputStream(out);
		dataOut.write(FormTemplate.versionHeader.getBytes());
		dataOut.writeInt(FormTemplate.exportVersionNumber);
		byte[] signedBundleTopSection = security.createSignedBundle(UnicodeUtilities.toUnicodeBytes(defaultFieldsTopSection.toXml()));
		byte[] signedBundleBottomSection = security.createSignedBundle(UnicodeUtilities.toUnicodeBytes(defaultFieldsBottomSection.toXml()));
		byte[] signedBundleTitle = security.createSignedBundle(UnicodeUtilities.toUnicodeBytes(formTemplateTitle));

		MockMartusSecurity otherSecurity = new MockMartusSecurity();
		otherSecurity.createKeyPair(512);

		byte[] signedBundleDescription = otherSecurity.createSignedBundle(UnicodeUtilities.toUnicodeBytes(formTemplateDescription));
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
		
		FormTemplate template = new FormTemplate(formTemplateTitle, formTemplateDescription, defaultFieldsTopSection, defaultFieldsBottomSection);
		assertFalse(importTemplate(template, exportMultipleSignersCFTFile));
		assertEquals(CustomFieldError.CODE_SIGNATURE_ERROR, ((CustomFieldError)template.getErrors().get(0)).getCode());
	}
	
	public void testGetExportedTemplateAsString() throws Exception
	{
		String formTemplateTitle = "New Form Title";
		String formTemplateDescription = "New Form Description";
		FieldSpecCollection defaultFieldsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection defaultFieldsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		FormTemplate template = new FormTemplate(formTemplateTitle, formTemplateDescription, defaultFieldsTopSection, defaultFieldsBottomSection);
		
		String exportedTemplateAsStringBase64= template.getExportedTemplateAsBase64String(security);
		byte[] decodedBytes = StreamableBase64.decode(exportedTemplateAsStringBase64);
		assertNotEquals(0, decodedBytes.length);
		
		File exportFile = createTempFileFromName("$$$testExportedTemplateAsString");
		exportFile.delete();
		assertFalse(exportFile.exists());
		FileOutputStream output = new FileOutputStream(exportFile);
		output.write(decodedBytes);
		output.flush();
		output.close();
		
		FormTemplate templateRetrieved = new FormTemplate();
		assertTrue("Failed to import Template from exportedString?", importTemplate(templateRetrieved, exportFile));
		assertEquals(formTemplateTitle, templateRetrieved.getTitle());
		assertEquals(formTemplateDescription, templateRetrieved.getDescription());
		assertEquals(defaultFieldsTopSection.toXml(), templateRetrieved.getTopSectionXml());
		assertEquals(defaultFieldsBottomSection.toXml(), templateRetrieved.getBottomSectionXml());
	}

	public void testImportXmlLegacy() throws Exception
	{
		FieldCollection fieldsTopSection = new FieldCollection(StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray());
		File exportFile = createTempFileFromName("$$$testImportXmlLegacy");
		exportFile.delete();
		
		FileOutputStream out = new FileOutputStream(exportFile);
		byte[] signedBundle = security.createSignedBundle(fieldsTopSection.toString().getBytes("UTF-8"));
		out.write(signedBundle);
		out.flush();
		out.close();

		FormTemplate template = new FormTemplate();
		assertEquals("", template.getTopSectionXml());
		assertTrue(importTemplate(template, exportFile));
		FieldCollection defaultBottomSectionFields = new FieldCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray());
		assertEquals(fieldsTopSection.toString(), template.getTopSectionXml());
		assertEquals(defaultBottomSectionFields.toString(), template.getBottomSectionXml());
		assertEquals(0, template.getErrors().size());
		
		UnicodeWriter writer = new UnicodeWriter(exportFile,UnicodeWriter.APPEND);
		writer.write("unauthorizedTextAppended Should not be read.");
		writer.close();
		
		assertTrue(importTemplate(template, exportFile));
		assertEquals(fieldsTopSection.toString(), template.getTopSectionXml());
		assertEquals(0, template.getErrors().size());

		exportFile.delete();
		out = new FileOutputStream(exportFile);
		byte[] tamperedBundle = security.createSignedBundle(fieldsTopSection.toString().getBytes("UTF-8"));
		tamperedBundle[tamperedBundle.length-2] = 'j';
		out.write(tamperedBundle);
		out.flush();
		out.close();
		
		assertFalse(importTemplate(template, exportFile));
		assertEquals("", template.getTopSectionXml());
		assertEquals(1, template.getErrors().size());
		assertEquals(CustomFieldError.CODE_SIGNATURE_ERROR, ((CustomFieldError)template.getErrors().get(0)).getCode());
		
		exportFile.delete();
		try
		{
			assertFalse(importTemplate(template, exportFile));
			fail("expected FNF exception");
		}
		catch(FileNotFoundException ignoreExpected)
		{
		}
	}

	public void testImportXmlFuture() throws Exception
	{
		File exportFile = createTempFileFromName("$$$testImportXmlFuture");
		exportFile.delete();
		
		FileOutputStream out = new FileOutputStream(exportFile);
		DataOutputStream dataOut = new DataOutputStream(out);
		dataOut.write(FormTemplate.versionHeader.getBytes());
		dataOut.writeInt(FormTemplate.exportVersionNumber + 1);
		dataOut.writeInt(16);
		dataOut.writeInt(0);
		dataOut.write("Some future data".getBytes());
		dataOut.flush();
		dataOut.close();

		FormTemplate template = new FormTemplate();
		try
		{
			importTemplate(template, exportFile);
			fail("Should have thrown future version Exception");
		}
		catch(FormTemplate.FutureVersionException expected)
		{
		}
	}

	public void testImportXml() throws Exception
	{
		FieldCollection fieldsTopSection = new FieldCollection(StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray());
		FieldSpecCollection fieldSpecsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		String privateTag = "a2";
		String privateLabel ="b2";
		fieldSpecsBottomSection = TestCustomFieldSpecValidator.addFieldSpec(fieldSpecsBottomSection, LegacyCustomFields.createFromLegacy(privateTag+","+privateLabel));
		FieldCollection fieldsBottomSection = new FieldCollection(fieldSpecsBottomSection);
		
		File exportFile = createTempFileFromName("$$$testImportXml");
		String formTemplateTitle = "Form Title";
		String formTemplateDescription = "Form Description";
		FormTemplate template = new FormTemplate(formTemplateTitle, formTemplateDescription, fieldsTopSection.getSpecs(), fieldsBottomSection.getSpecs());
		exportFile.delete();
		template.exportTemplate(security, exportFile);

		{
			FormTemplate importedTemplate = new FormTemplate();
			assertTrue(importTemplate(importedTemplate, exportFile));
			assertEquals(fieldsTopSection.toString(), importedTemplate.getTopSectionXml());
			assertEquals(fieldsBottomSection.toString(), importedTemplate.getBottomSectionXml());
			assertEquals(formTemplateTitle, importedTemplate.getTitle());
			assertEquals(formTemplateDescription, importedTemplate.getDescription());
			assertEquals(0, importedTemplate.getErrors().size());
			
			UnicodeWriter writer = new UnicodeWriter(exportFile,UnicodeWriter.APPEND);
			writer.write("unauthorizedTextAppended Should not be read.");
			writer.close();
		}

		{
			FormTemplate importedTemplate = new FormTemplate();
			assertTrue(importTemplate(importedTemplate, exportFile));
			assertEquals(fieldsTopSection.toString(), importedTemplate.getTopSectionXml());
			assertEquals(fieldsBottomSection.toString(), importedTemplate.getBottomSectionXml());
			assertEquals(formTemplateTitle, importedTemplate.getTitle());
			assertEquals(formTemplateDescription, importedTemplate.getDescription());
			assertEquals(0, importedTemplate.getErrors().size());
		}
		
		exportFile.delete();
		FileOutputStream out = new FileOutputStream(exportFile);
		byte[] tamperedBundle = security.createSignedBundle(fieldsTopSection.toString().getBytes("UTF-8"));
		tamperedBundle[tamperedBundle.length-2] = 'j';
		out.write(tamperedBundle);
		out.flush();
		out.close();
		
		assertFalse(importTemplate(template, exportFile));
		assertEquals("", template.getTopSectionXml());
		assertEquals("", template.getBottomSectionXml());
		assertEquals("", template.getTitle());
		assertEquals("", template.getDescription());
		assertEquals(1, template.getErrors().size());
		assertEquals(CustomFieldError.CODE_SIGNATURE_ERROR, ((CustomFieldError)template.getErrors().get(0)).getCode());
		
		exportFile.delete();
		try
		{
			assertFalse(importTemplate(template, exportFile));
			fail("expected FNF exception");
		}
		catch(FileNotFoundException ignoreExpected)
		{
		}
	}

	private boolean importTemplate(FormTemplate template, File exportFile) throws FutureVersionException, IOException
	{
		InputStreamWithSeek inputStreamWithSeek = new FileInputStreamWithSeek(exportFile);
		try
		{
			return template.importTemplate(security, inputStreamWithSeek);
		}
		finally
		{
			inputStreamWithSeek.close();
		}
	}
	
	static MockMartusSecurity security;
}
