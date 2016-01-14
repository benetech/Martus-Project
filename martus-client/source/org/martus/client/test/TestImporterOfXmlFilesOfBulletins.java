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
package org.martus.client.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Vector;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.tools.ImporterOfXmlFilesOfBulletins;
import org.martus.client.tools.XmlBulletinsImporter;
import org.martus.client.tools.XmlBulletinsImporter.FieldSpecVerificationException;
import org.martus.common.FieldSpecCollection;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.PendingAttachmentList;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.fieldspec.CustomFieldError;
import org.martus.common.fieldspec.CustomFieldSpecValidator;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.packet.UniversalId;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;

public class TestImporterOfXmlFilesOfBulletins extends TestCaseEnhanced
{
	public TestImporterOfXmlFilesOfBulletins(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(security == null)
		{
			security = MockMartusSecurity.createClient();
			clientStore = new ClientBulletinStore(security);
			dataDirectory = createTempDirectory();
			clientStore.doAfterSigninInitialization(dataDirectory);
			clientStore.createFieldSpecCacheFromDatabase();
			nullPrinter = new PrintStream(new ByteArrayOutputStream());
			
		}
		importFolder = clientStore.createOrFindFolder("Import");
		xmlInputDirectory = createTempDirectory();
	}
	
	public void tearDown() throws Exception
	{
		super.tearDown();
		DirectoryUtils.deleteEntireDirectoryTree(xmlInputDirectory);
		DirectoryUtils.deleteEntireDirectoryTree(dataDirectory);
	}

	public void testImportXML() throws Exception
	{
		InputStream xmlIn = getStreamFromResource("SampleXmlBulletin.xml");
		XmlBulletinsImporter importer = new XmlBulletinsImporter(security, xmlIn);
		FieldSpecCollection mainFieldSpecs = importer.getMainFieldSpecs();
		assertNotNull(mainFieldSpecs);
		assertEquals(20, mainFieldSpecs.size());
		FieldSpec field = mainFieldSpecs.get(0);
		assertTrue(field.getType().isLanguageDropdown());
		FieldSpecCollection privateFieldSpecs = importer.getPrivateFieldSpecs();
		assertNotNull(privateFieldSpecs);
		assertEquals(1, privateFieldSpecs.size());
		field = privateFieldSpecs.get(0);
		assertTrue(field.getType().isMultiline());
		HashMap tagValues = importer.getFieldTagValuesMap();
		assertEquals("Range:1980-02-15,1980-05-22", tagValues.get("InterviewDates"));
		assertEquals("Information we want kept private\n", tagValues.get("privateinfo"));
		Bulletin[] bulletinReturned = importer.getBulletins();
		assertNotNull("No bulletin returned?", bulletinReturned);
		assertEquals(1, bulletinReturned.length);
		Bulletin b = bulletinReturned[0];
		assertEquals("Charles.", b.get(Bulletin.TAGAUTHOR));
		assertEquals("no keywords", b.get(Bulletin.TAGKEYWORDS));
		assertEquals("1970-01-01,19700101+1", b.get(Bulletin.TAGEVENTDATE));
		assertEquals("1980-02-15,19800215+97", b.get("InterviewDates"));
		assertEquals("en", b.get(Bulletin.TAGLANGUAGE));
		assertEquals("2005-11-01", b.get(Bulletin.TAGENTRYDATE));
		BulletinStore testStore = new BulletinStore();
		MockClientDatabase db = new MockClientDatabase();
		testStore.setDatabase(db);
		File tempDir = createTempDirectory();
		testStore.doAfterSigninInitialization(tempDir, db);
		testStore.saveBulletinForTesting(b);
		DatabaseKey headerKey1 = DatabaseKey.createLegacyKey(b.getBulletinHeaderPacket().getUniversalId());
		assertEquals(1,testStore.getBulletinCount());
		Bulletin bulletinFromStore = BulletinLoader.loadFromDatabase(db, headerKey1, security);
		assertEquals(b.getAccount(), bulletinFromStore.getAccount());
		assertEquals("1970-01-01,19700101+1", bulletinFromStore.get(Bulletin.TAGEVENTDATE));
		assertEquals("en", bulletinFromStore.get(Bulletin.TAGLANGUAGE));
		assertEquals("2005-11-01", bulletinFromStore.get(Bulletin.TAGENTRYDATE));

		String gridData = bulletinFromStore.get("VictimInformationGrid");
		assertEquals("Found string 'Simple' in the grid data for a date range?", -1, gridData.indexOf("Simple"));

		String gridData2 = bulletinFromStore.get("ProfessionHistoryGrid");
		assertEquals("Found ending date range '1977-04-01' in the grid data?", -1, gridData2.indexOf("1977-04-01"));
		assertEquals("Found string 'Range' in the grid data for a date range?", -1, gridData2.indexOf("Range"));
		assertNotEquals("Did not find internal date range '19700101+2647' in the grid data?", -1, gridData2.indexOf("19700101+2647"));
		
		db.deleteAllData();
		testStore.deleteAllData();
	}
	
	
	public void testImportInvalidMainFieldSpecs() throws Exception
	{
		InputStream xmlIn = getStreamFromResource("SampleInvalidFieldSpecsXmlBulletin.xml");
		try
		{
			 new XmlBulletinsImporter(security, xmlIn);
			fail("Should have thrown an exception");
		}
		catch(FieldSpecVerificationException expectedException)
		{
			Vector errors = expectedException.getErrors();
			StringBuffer validationErrorMessages = new StringBuffer();
			for(int i = 0; i<errors.size(); ++i)
			{
				CustomFieldSpecValidator currentValidator = (CustomFieldSpecValidator)errors.get(i);
				Vector validationErrors = currentValidator.getAllErrors();
				for(int j = 0; j<validationErrors.size(); ++j)
				{
					CustomFieldError thisError = (CustomFieldError)validationErrors.get(j);
					StringBuffer thisErrorMessage = new StringBuffer(thisError.getCode());
					thisErrorMessage.append(" : ");
					thisErrorMessage.append(thisError.getType());
					thisErrorMessage.append(" : ");
					thisErrorMessage.append(thisError.getTag());
					thisErrorMessage.append(" : ");
					thisErrorMessage.append(thisError.getLabel());
					validationErrorMessages.append(thisErrorMessage);
					validationErrorMessages.append('\n');
				}
			}		
			assertEquals(expectedErrorMessage, validationErrorMessages.toString());
			assertEquals("Calling the getErrors twice changed the results?", expectedErrorMessage, validationErrorMessages.toString());
		}
	}

	public void testImportInvalidXML() throws Exception
	{
		String invalidXML = "<wrong xml field expected>jflskdf</wrong xml field expected>";
		StringInputStreamWithSeek xmlInvalid = new StringInputStreamWithSeek(invalidXML);
		try
		{
			new XmlBulletinsImporter(security, xmlInvalid);
			fail("should have thrown");
		}
		catch(Exception expectedException)
		{
		}
	}

	public void testImportInvalidFieldsSpecDontMatchData() throws Exception
	{
		
		InputStream xmlIn = getStreamFromResource("SampleInvalidBulletinFieldsSpecDontMatchData.xml");
		try
		{
			new XmlBulletinsImporter(security, xmlIn);
			fail("should have thrown");
		}
		catch(FieldSpecVerificationException expectedException)
		{
			Vector errors = expectedException.getErrors();
			assertEquals(1, errors.size());
			CustomFieldSpecValidator currentValidator = (CustomFieldSpecValidator)errors.get(0);
			Vector validationErrors = currentValidator.getAllErrors();
			assertEquals(1, validationErrors.size());
			CustomFieldError thisError = (CustomFieldError)validationErrors.get(0);
			assertEquals(CustomFieldError.CODE_MISSING_CUSTOM_FIELD_IN_SPEC, thisError.getCode());
			assertEquals("IntervieweeName", thisError.getTag());
		}
	}

	public void testImportInvalidMultipleBulletins() throws Exception
	{
		
		InputStream xmlIn = getStreamFromResource("SampleInvalidFieldSpecsXmlThreeBulletins.xml");
		try
		{
			new XmlBulletinsImporter(security, xmlIn);
			fail("should have thrown");
		}
		catch(FieldSpecVerificationException expectedException)
		{
			Vector errors = expectedException.getErrors();
			assertEquals(2, errors.size());
			CustomFieldSpecValidator currentValidator = (CustomFieldSpecValidator)errors.get(0);
			Vector validationErrors = currentValidator.getAllErrors();
			assertEquals(4, validationErrors.size());
			CustomFieldError thisError = (CustomFieldError)validationErrors.get(0);
			assertEquals(CustomFieldError.CODE_REQUIRED_FIELD, thisError.getCode());

			currentValidator = (CustomFieldSpecValidator)errors.get(1);
			validationErrors = currentValidator.getAllErrors();
			assertEquals(1, validationErrors.size());
			thisError = (CustomFieldError)validationErrors.get(0);
			assertEquals(CustomFieldError.CODE_UNKNOWN_TYPE, thisError.getCode());
			assertEquals("IntervieweeName", thisError.getTag());
		}
	}
	
	public void testImportXMLWithMultipleBulletins() throws Exception
	{
		File xmlBulletin1 = new File(xmlInputDirectory, "$$$bulletin1.xml");
		copyResourceFileToLocalFile(xmlBulletin1, "SampleXmlBulletin.xml");
		xmlBulletin1.deleteOnExit();
		File xmlBulletin2 = new File(xmlInputDirectory, "$$$bulletin2.xml");
		copyResourceFileToLocalFile(xmlBulletin2, "SampleXmlTwoBulletins.xml");
		xmlBulletin2.deleteOnExit();
		File[] xmlFiles = new File[] {xmlBulletin1, xmlBulletin2};
		ImporterOfXmlFilesOfBulletins importer = new ImporterOfXmlFilesOfBulletins(xmlFiles, clientStore, importFolder, nullPrinter);
		importer.importFiles();
		xmlBulletin1.delete();
		xmlBulletin2.delete();
		
		assertEquals("Didn't get all 3 bulletins?", 3, importer.getNumberOfBulletinsImported());
		Vector bulletinIds = new Vector(clientStore.getAllBulletinLeafUids());
		
		Bulletin b1 = clientStore.getBulletinRevision((UniversalId)bulletinIds.get(0));
		Bulletin b2 = clientStore.getBulletinRevision((UniversalId)bulletinIds.get(1));
		Bulletin b3 = clientStore.getBulletinRevision((UniversalId)bulletinIds.get(2));
		Vector titles = new Vector();
		titles.add(b1.getField(Bulletin.TAGTITLE).toString());
		titles.add(b2.getField(Bulletin.TAGTITLE).toString());
		titles.add(b3.getField(Bulletin.TAGTITLE).toString());
		assertContains("Title Bulletin #1", titles);
		assertContains("Title Bulletin #2", titles);
		assertContains("import export example", titles);
	}

	public void testImportXMLWithAttachments() throws Exception
	{
		File xmlBulletinWithAttachments = new File(xmlInputDirectory, "$$$bulletinWithAttachments.xml");
		copyResourceFileToLocalFile(xmlBulletinWithAttachments, "SampleXmlBulletinWithAttachments.xml");
		xmlBulletinWithAttachments.deleteOnExit();
	
		File attachment1 = new File(xmlInputDirectory, "$$$Sample Attachment1.txt");
		copyResourceFileToLocalFile(attachment1, "Sample Attachment1.txt");
		attachment1.deleteOnExit();

		File attachment2 = new File(xmlInputDirectory, "$$$Sample Attachment2.txt");
		copyResourceFileToLocalFile(attachment2, "Sample Attachment2.txt");
		attachment2.deleteOnExit();

		File attachment3 = new File(xmlInputDirectory, "$$$Sample Attachment3.txt");
		copyResourceFileToLocalFile(attachment3, "Sample Attachment3.txt");
		attachment3.deleteOnExit();
		
		clientStore.deleteAllBulletins();
		File[] xmlFiles = new File[] {xmlBulletinWithAttachments};
		ImporterOfXmlFilesOfBulletins importer = new ImporterOfXmlFilesOfBulletins(xmlFiles, clientStore, importFolder, nullPrinter);
		importer.setAttachmentsDirectory(xmlInputDirectory);
		importer.importFiles();
		assertFalse(importer.hasMissingAttachments());

		xmlBulletinWithAttachments.delete();
		
		UnicodeReader reader = new UnicodeReader(attachment1);
		String attachment1Data = reader.readAll();
		reader.close();
		reader = new UnicodeReader(attachment2);
		String attachment2Data = reader.readAll();
		reader.close();
		reader = new UnicodeReader(attachment3);
		String attachment3Data = reader.readAll();
		reader.close();
		
		attachment1.delete();
		attachment2.delete();
		attachment3.delete();
		
		assertEquals("Didn't get 1 bulletins?", 1, importer.getNumberOfBulletinsImported());
		Vector bulletinIds = new Vector(clientStore.getAllBulletinLeafUids());
		Bulletin b1 = clientStore.getBulletinRevision((UniversalId)bulletinIds.get(0));
		AttachmentProxy[] publicAttachments = b1.getPublicAttachments();
		AttachmentProxy[] privateAttachments = b1.getPrivateAttachments();
		PendingAttachmentList pendingPublicAttachments = b1.getPendingPublicAttachments();
		assertEquals("Found pending Public attachments?",0,pendingPublicAttachments.size());
		PendingAttachmentList pendingPrivateAttachments = b1.getPendingPrivateAttachments();
		assertEquals("Found pending Private attachments?",0,pendingPrivateAttachments.size());
		
		assertEquals("Didn't find 2 public attachments?", 2, publicAttachments.length);
		assertEquals("Didn't find 2 private attachments?", 2, privateAttachments.length);
		
		assertEquals("Wrong File name Public 1?", "$$$Sample Attachment1.txt", publicAttachments[0].getLabel());
		assertEquals("Wrong File name Public 2?", "$$$Sample Attachment2.txt", publicAttachments[1].getLabel());
		assertEquals("Wrong File name Private 1?", "$$$Sample Attachment3.txt", privateAttachments[0].getLabel());
		assertEquals("Wrong File name Private 2?", "$$$Sample Attachment2.txt", privateAttachments[1].getLabel());

		File BulletinAttachment1 = b1.getAsFileProxy(publicAttachments[0], clientStore.getDatabase(), b1.getStatus()).getFile();
		
		reader = new UnicodeReader(BulletinAttachment1);
		String bulletinAttachment1Data = reader.readAll();
		reader.close();
		BulletinAttachment1.delete();
		File BulletinAttachment2 = b1.getAsFileProxy(publicAttachments[1], clientStore.getDatabase(), b1.getStatus()).getFile();
		reader = new UnicodeReader(BulletinAttachment2);
		String bulletinAttachment2Data = reader.readAll();
		reader.close();
		BulletinAttachment2.delete();
		File BulletinAttachment3 = b1.getAsFileProxy(privateAttachments[0], clientStore.getDatabase(), b1.getStatus()).getFile();
		reader = new UnicodeReader(BulletinAttachment3);
		String bulletinAttachment3Data = reader.readAll();
		reader.close();
		BulletinAttachment3.delete();
		
		assertEquals("Attachment 1 public data not equal?", attachment1Data, bulletinAttachment1Data);
		assertEquals("Attachment 2 public data not equal?", attachment2Data, bulletinAttachment2Data);
		assertEquals("Attachment 3 pravate data not equal?", attachment3Data, bulletinAttachment3Data);
	}
	
	public void testImportXMLWithAttachmentsMissing() throws Exception
	{
		File xmlBulletinWithAttachments = new File(xmlInputDirectory, "$$$bulletinWithAttachments.xml");
		copyResourceFileToLocalFile(xmlBulletinWithAttachments, "SampleXmlBulletinWithAttachments.xml");
		xmlBulletinWithAttachments.deleteOnExit();
	
		clientStore.deleteAllBulletins();
		File[] xmlFiles = new File[] {xmlBulletinWithAttachments};
		ImporterOfXmlFilesOfBulletins importer = new ImporterOfXmlFilesOfBulletins(xmlFiles, clientStore, importFolder, nullPrinter);
		importer.setAttachmentsDirectory(xmlInputDirectory);
		importer.importFiles();
		assertTrue(importer.hasMissingAttachments());
		assertEquals(1, importer.getMissingAttachmentsMap().size());
		xmlBulletinWithAttachments.delete();
	}
	
	public void testImportXMLWithFailingBulletins() throws Exception
	{
		FaultyClientBulletinStore faultyStore = new FaultyClientBulletinStore(security);
		File faultyDataDirectory = createTempDirectory();

		faultyStore.doAfterSigninInitialization(faultyDataDirectory);
		faultyStore.createFieldSpecCacheFromDatabase();
		faultyStore.saveFolders();


		File xmlBulletin1 = new File(xmlInputDirectory, "$$$bulletin1.xml");
		copyResourceFileToLocalFile(xmlBulletin1, "SampleXmlBulletin.xml");
		xmlBulletin1.deleteOnExit();
		
		ImporterOfXmlFilesOfBulletins importer = new ImporterOfXmlFilesOfBulletins(new File[]{xmlBulletin1}, faultyStore, importFolder, nullPrinter);
		importer.setAttachmentsDirectory(xmlInputDirectory);
		importer.importFiles();
		assertEquals(0, importer.getNumberOfBulletinsImported());
		assertEquals(1, importer.getTotalNumberOfBulletins());
		assertTrue(importer.hasBulletinsNotImported());
		assertEquals(1, importer.getBulletinsNotImported().size());
		HashMap errors = importer.getBulletinsNotImported();
		String bulletinSampleTitle = "import export example";
		assertTrue("Bulletin title wasn't found?", errors.containsKey(bulletinSampleTitle));
		assertEquals(ERROR_SAVING_BULLETIN_MSG, errors.get(bulletinSampleTitle));
		faultyStore.deleteAllData();
		DirectoryUtils.deleteEntireDirectoryTree(faultyDataDirectory);
	}
	
	class FaultyClientBulletinStore extends ClientBulletinStore
	{
		public FaultyClientBulletinStore(MartusCrypto cryptoToUse)
		{
			super(cryptoToUse);
		}

		public void saveBulletin(Bulletin b) throws IOException, CryptoException
		{
			throw new IOException(ERROR_SAVING_BULLETIN_MSG);
		}
	}

	final String ERROR_SAVING_BULLETIN_MSG = "Error Saving Bulletin";
	final String expectedErrorMessage = "100 :  : author : \n" +
	"100 :  : title : \n" +
	"108 : DROPDOWN : BulletinSourceDuplicateEntries : Source of bulletin information\n" +
	"102 : BOOLEAN : DuplicateTag : Does interviewee wish to remain anonymous?\n";

	static ClientBulletinStore clientStore;
	static BulletinFolder importFolder;
	static PrintStream nullPrinter;
	static File dataDirectory;
	static MartusCrypto security;
	static File xmlInputDirectory;
	
}
