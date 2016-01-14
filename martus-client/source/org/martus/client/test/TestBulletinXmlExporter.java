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

package org.martus.client.test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.BulletinXmlExporter;
import org.martus.client.tools.XmlBulletinsImporter;
import org.martus.common.FieldCollection;
import org.martus.common.FieldCollectionForTesting;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.GridRow;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.bulletin.BulletinXmlExportImportConstants;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.EncryptionException;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeMessage;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.fieldspec.TestCustomFieldSpecValidator;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.ExtendedHistoryList;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.DirectoryUtils;
import org.martus.util.MultiCalendar;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;
import org.martus.util.xml.XmlUtilities;

public class TestBulletinXmlExporter extends TestCaseEnhanced
{
	public TestBulletinXmlExporter(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		failingAttachments = 0;
		if(app == null)
		{
			app = MockMartusApp.create(getName());
			attachmentDirectory = createTempDirectory();
			store = app.getStore();
		}
		noReusableChoices = PoolOfReusableChoicesLists.EMPTY_POOL;
	}
	
	public void tearDown() throws Exception
	{
		DirectoryUtils.deleteAllFilesOnlyInDirectory(attachmentDirectory);
		app.deleteAllFiles();
		store.deleteAllData();
	}

	public void testBasics() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.setAllPrivate(false);

		final String sampleAuthor = "someone special";

		b.set(BulletinConstants.TAGAUTHOR, sampleAuthor);
		store.saveBulletin(b);
		Vector list = new Vector();
		list.add(b);
		String result = doExport(list, false, false, false);
		assertContains("<MartusBulletinExportFormatVersion>3</MartusBulletinExportFormatVersion>", result);
		assertContains("<MartusBulletins>", result);
		assertContains("<MartusBulletin>", result);
		assertContains("<ExportMetaData>", result);
		assertContains("<BulletinVersion>1</BulletinVersion>", result);
		assertContains("<BulletinStatus>draft</BulletinStatus>", result);
		assertNotContains("<BulletinStatus>sealed</BulletinStatus>", result);
		assertContains("<LocalizedBulletinStatus>"+draftTranslation+"</LocalizedBulletinStatus>", result);
		assertNotContains("<LocalizedBulletinStatus>"+sealedTranslation+"</LocalizedBulletinStatus>", result);

		MiniLocalization miniLocalization = new MiniLocalization();
		String lastSavedDateTime = miniLocalization.formatDateTime(b.getLastSavedTime());
		
		assertContains("<BulletinLastSavedDateTime>"+Long.toString(b.getLastSavedTime())+"</BulletinLastSavedDateTime>", result);
		assertContains("<LocalizedBulletinLastSavedDateTime>"+lastSavedDateTime+"</LocalizedBulletinLastSavedDateTime>", result);
		assertContains("<BulletinMetaData>", result);
		assertContains("<NoAttachmentsExported></NoAttachmentsExported>", result);
		assertContains(b.getAccount(), result);
		assertContains(b.getLocalId(), result);
		assertContains(sampleAuthor, result);
		assertContains("<MainFieldSpecs>", result);
		assertContains("<FieldValues>", result);
		assertNotContains("<PrivateFieldSpecs>", result);
		assertNotContains("<PrivateData>", result);
		assertNotContains("<AttachmentList>", result);
		assertNotContains("<History>", result);
		assertContains("<ExtendedHistory>", result);
	}
	
	public void testExportingFieldSpecs() throws Exception
	{
		FieldSpecCollection topSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection bottomSpecs = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		String choice1 = "choice A";
		String choice2 = "choice B";

		ChoiceItem[] choicesNoDups = {new ChoiceItem("no Dup", choice1), new ChoiceItem("second", choice2)};
		String dropdownTag = "ddTag";
		String dropdownLabel = "dropdown column label";
		DropDownFieldSpec dropDownSpecNoDuplicates = new DropDownFieldSpec(choicesNoDups);
		dropDownSpecNoDuplicates.setLabel(dropdownLabel);
		dropDownSpecNoDuplicates.setTag(dropdownTag);
		
		String booleanLabel = "Boolean Label";
		String booleanTag = "TagBoolean";
		FieldSpec booleanSpec = FieldSpec.createFieldSpec(booleanLabel, new FieldTypeBoolean());
		booleanSpec.setTag(booleanTag);
		
		GridFieldSpec gridWithNoDuplicateDropdownEntries = new GridFieldSpec();
		String gridTag = "GridTag";
		String gridLabel = "Grid Label";
		gridWithNoDuplicateDropdownEntries.setTag(gridTag);
		gridWithNoDuplicateDropdownEntries.setLabel(gridLabel);
		gridWithNoDuplicateDropdownEntries.addColumn(dropDownSpecNoDuplicates);
		gridWithNoDuplicateDropdownEntries.addColumn(booleanSpec);

		String messageLabel = "message Label";
		String messageTag = "messageTag";
		FieldSpec messageSpec = FieldSpec.createFieldSpec(messageLabel, new FieldTypeMessage());
		messageSpec.setTag(messageTag);
		
		topSpecs = TestCustomFieldSpecValidator.addFieldSpec(topSpecs, gridWithNoDuplicateDropdownEntries);
		bottomSpecs = TestCustomFieldSpecValidator.addFieldSpec(bottomSpecs,dropDownSpecNoDuplicates);
		bottomSpecs = TestCustomFieldSpecValidator.addFieldSpec(bottomSpecs,booleanSpec);
		bottomSpecs = TestCustomFieldSpecValidator.addFieldSpec(bottomSpecs, messageSpec);
		
		Bulletin b = new Bulletin(store.getSignatureGenerator(), topSpecs, bottomSpecs);
		b.setAllPrivate(false);

		final String sampleAuthor = "someone special";
		b.set(BulletinConstants.TAGAUTHOR, sampleAuthor);

		String expectedTopFieldSpecs = "<MainFieldSpecs>\n" +
			  "<Field type='LANGUAGE'>\n" +
			  "<Tag>language</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='STRING'>\n" +
			  "<Tag>author</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='STRING'>\n" +
			  "<Tag>organization</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='STRING'>\n" +
			  "<Tag>title</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='STRING'>\n" +
			  "<Tag>location</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='STRING'>\n" +
			  "<Tag>keywords</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='DATERANGE'>\n" +
			  "<Tag>eventdate</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='DATE'>\n" +
			  "<Tag>entrydate</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='MULTILINE'>\n" +
			  "<Tag>summary</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='MULTILINE'>\n" +
			  "<Tag>publicinfo</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='GRID'>\n" +
			  "<Tag>"+gridTag+"</Tag>\n" +
			  "<Label>"+gridLabel+"</Label>\n" +
			  "<GridSpecDetails>\n" +
			  "<Column type='DROPDOWN'>\n" +
			  "<Tag>"+dropdownTag+"</Tag>\n" +
			  "<Label>"+dropdownLabel+"</Label>\n" +
			  "<Choices>\n" +
			  "<Choice>"+choice1+"</Choice>\n" +
			  "<Choice>"+choice2+"</Choice>\n" +
			  "</Choices>\n" +
			  "</Column>\n" +
			  "<Column type='BOOLEAN'>\n" +
			  "<Tag>"+booleanTag+"</Tag>\n" +
			  "<Label>"+booleanLabel+"</Label>\n" +
			  "</Column>\n" +
			  "</GridSpecDetails>\n" +
			  "</Field>\n" +
			  "</MainFieldSpecs>\n\n";

		BulletinXmlExporter exporter = new BulletinXmlExporter(null, new MiniLocalization(), null);
		StringWriter writer = new StringWriter();
		exporter.writeFieldSpecs(writer, b.getTopSectionFieldSpecs(), "MainFieldSpecs");
		String result = writer.toString();
		writer.close();
		assertEquals(expectedTopFieldSpecs, result);
		
		String expectedBottomFieldSpecs = "<PrivateFieldSpecs>\n" +
		  "<Field type='MULTILINE'>\n" +
		  "<Tag>privateinfo</Tag>\n" + 
		  "<Label></Label>\n" + 
		  "</Field>\n" +
		  "<Field type='DROPDOWN'>\n" +
		  "<Tag>"+dropdownTag+"</Tag>\n" +
		  "<Label>"+dropdownLabel+"</Label>\n" +
		  "<Choices>\n" +
		  "<Choice>"+choice1+"</Choice>\n" +
		  "<Choice>"+choice2+"</Choice>\n" +
		  "</Choices>\n" +
		  "</Field>\n" +
		  "<Field type='BOOLEAN'>\n" +
		  "<Tag>"+booleanTag+"</Tag>\n" +
		  "<Label>"+booleanLabel+"</Label>\n" +
		  "</Field>\n" +
		  "<Field type='MESSAGE'>\n" +
		  "<Tag>"+messageTag+"</Tag>\n" +
		  "<Label>"+messageLabel+"</Label>\n" +
		  "<Message></Message>\n" +
		  "</Field>\n" +
		  "</PrivateFieldSpecs>\n\n";

	writer = new StringWriter();
	exporter.writeFieldSpecs(writer, b.getBottomSectionFieldSpecs(), "PrivateFieldSpecs");
	result = writer.toString();
	writer.close();
	assertEquals(expectedBottomFieldSpecs, result);
	}
	
	public void testExportGrids() throws Exception
	{
		String gridTag = "MyGridTag";
		String xmlFieldType = "<CustomFields>" +
				"<Field type='GRID'>" +
				"<Tag>"+gridTag+"</Tag>" +
				"<Label>Victim Information</Label>" +
				"<GridSpecDetails>" +
				"<Column type='STRING'>\n" +
				"<Tag></Tag>\n" +
				"<Label>Name of Victim</Label>\n" +
				"</Column>\n" +
				"<Column type='STRING'>\n" +
				"<Tag></Tag>\n" +
				"<Label>Age of Victim</Label>\n" +
				"</Column>\n" +
				"<Column type='DATE'>\n" +
				"<Tag></Tag>\n" +
				"<Label>Date Of Event</Label>\n" +
				"</Column>\n" +
				"</GridSpecDetails>" +
				"</Field></CustomFields>";
		GridFieldSpec newSpec = (GridFieldSpec)FieldCollection.parseXml(xmlFieldType).get(0); 
		FieldCollection fields = FieldCollectionForTesting.extendFields(StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray(), newSpec);				
		
		Bulletin b = new Bulletin(store.getSignatureGenerator(), fields.getSpecs(), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		b.setAllPrivate(false);
		GridData gridData = new GridData(newSpec, noReusableChoices);
		GridRow row = new GridRow(newSpec, noReusableChoices);
		row.setCellText(0, "rowData1");
		row.setCellText(1, "rowData2");
		row.setCellText(2, "20060504");
		gridData.addRow(row);
		b.set(gridTag, gridData.getXmlRepresentation());
		
		Vector list = new Vector();
		list.add(b);
		String result = doExport(list, false, false, false);
		assertContains("<Field tag='MyGridTag'>\n" +
				"<Value><GridData columns='3'>\n" +
				"<Row>\n" +
				"<Column>rowData1</Column>\n" +
				"<Column>rowData2</Column>\n" +
				"<Column>Simple:20060504</Column>\n" +
				"</Row>\n" +
				"</GridData>\n" +
				"</Value>\n" +
				"</Field>", result);
	}
	
	public void testExportReusableChoices() throws Exception
	{
		FieldSpecCollection topSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		String choicesCode = "ChoicesCode";
		String choicesLabel = "ChoicesLabel";
		String choice1Code = "a";
		String choice1Label = "A";
		ReusableChoices reusableChoices = new ReusableChoices(choicesCode, choicesLabel);
		reusableChoices.add(new ChoiceItem(choice1Code, choice1Label));
		topSpecs.addReusableChoiceList(reusableChoices);

		BulletinXmlExporter exporter = new BulletinXmlExporter(null, new MiniLocalization(), null);
		StringWriter writer = new StringWriter();
		exporter.writeFieldSpecs(writer, topSpecs, "MainFieldSpecs");
		String result = writer.toString();
		writer.close();
		
		assertContains("Didn't write choices code?", choicesCode, result);
		assertContains("Didn't write choices label?", choicesLabel, result);
		assertContains("Didn't write choice1 code?", choice1Code, result);
		assertContains("Didn't write choice1 label?", choice1Label, result);
	}
	
	public void testExportHistory() throws Exception
	{
		Bulletin version3 = createVersion3Bulletin();
		
		Vector list = new Vector();
		list.add(version3);
		String result = doExport(list, true, false, false);
		
		assertContains("<BulletinVersion>3</BulletinVersion>", result);
		assertContains("<History>", result);
		assertContains("<Ancestor>", result);
		assertContains(version3.getHistory().get(0), result);
		assertContains(version3.getHistory().get(0), result);
	}

	public void testExportAllVersions() throws Exception
	{
		Bulletin version3 = createVersion3Bulletin();
		
		Vector list = new Vector();
		list.add(version3);
		String result = doExport(list, true, false, true);
		
		assertContains(VERSION_3_TITLE, result);
		assertContains(VERSION_2_TITLE, result);
		assertContains(VERSION_1_TITLE, result);
	}

	private Bulletin createVersion3Bulletin() throws Exception,
			CryptoException, InvalidPacketException,
			SignatureVerificationException, WrongPacketTypeException,
			IOException, InvalidBase64Exception
	{
		ReadableDatabase database = app.getStore().getDatabase();

		Bulletin version1 = new Bulletin(store.getSignatureGenerator());
		version1.getField(Bulletin.TAGTITLE).setData(VERSION_1_TITLE);
		version1.setStatus(Bulletin.STATUSIMMUTABLE);
		app.getStore().saveBulletin(version1);
		
		Bulletin version2 = new Bulletin(store.getSignatureGenerator());
		version2.createDraftCopyOf(version1, database);
		version2.getField(Bulletin.TAGTITLE).setData(VERSION_2_TITLE);
		version2.setStatus(Bulletin.STATUSIMMUTABLE);
		app.getStore().saveBulletin(version2);

		Bulletin version3 = new Bulletin(store.getSignatureGenerator());
		version3.createDraftCopyOf(version2, database);
		version3.getField(Bulletin.TAGTITLE).setData(VERSION_3_TITLE);
		app.getStore().saveBulletin(version3);

		return version3;
	}
	
	public void testExportExtendedHistory() throws Exception
	{
		String localId1 = "pretend local id";
		String localId2 = "another fake local id";

		BulletinHistory fakeLocalHistory = new BulletinHistory();
		fakeLocalHistory.add(localId1);
		fakeLocalHistory.add(localId2);
		ExtendedHistoryList history = new ExtendedHistoryList();
		String otherAccountId = MockMartusSecurity.createOtherClient().getPublicKeyString();
		history.add(otherAccountId, fakeLocalHistory);

		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.getBulletinHeaderPacket().setExtendedHistory(history);
		
		Vector list = new Vector();
		list.add(b);
		String result = doExport(list, true, false, false);
		assertContains("<ExtendedHistory>", result);
		assertContains("<ExtendedHistoryEntry>", result);
		assertContains("<Author>", result);
		assertContains("Missing extended history author?", otherAccountId, result);
		assertContains("<History>", result);
		assertContains("<Ancestor>", result);
		assertContains(localId1, result);
		assertContains(localId2, result);
	}

	public void testExportWithPublicAttachments() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.setAllPrivate(false);
		final File sampleAttachmentFile1 = addNewPublicSampleAttachment(b, "Attachment 1's Data");
		final File sampleAttachmentFile2 = addNewPublicSampleAttachment(b, "Attachment 2's Data");
		File exportedAttachmentFile1 = new File(attachmentDirectory, sampleAttachmentFile1.getName()); 
		File exportedAttachmentFile2 = new File(attachmentDirectory, sampleAttachmentFile2.getName()); 
		app.getStore().saveBulletin(b);
		Vector list = new Vector();
		list.add(b);
		
		String result = doExport(list, false, false, false);
		assertContains("<NoAttachmentsExported></NoAttachmentsExported>", result);
		assertNotContains(BulletinXmlExportImportConstants.TOP_SECTION_ATTACHMENT_LIST, result);
		assertNotContains(BulletinXmlExportImportConstants.BOTTOM_SECTION_ATTACHMENT_LIST, result);
		assertNotContains(sampleAttachmentFile1.getName(), result);
		assertNotContains(sampleAttachmentFile2.getName(), result);
		assertFalse("Attachment 1 exists?", exportedAttachmentFile1.exists());
		assertFalse("Attachment 2 exists?", exportedAttachmentFile2.exists());
		
		result = doExport(list, false, true, false);
		assertNotContains("<NoAttachmentsExported></NoAttachmentsExported>", result);
		assertContains(BulletinXmlExportImportConstants.TOP_SECTION_ATTACHMENT_LIST, result);
		assertNotContains(BulletinXmlExportImportConstants.BOTTOM_SECTION_ATTACHMENT_LIST, result);

		assertContains(BulletinXmlExportImportConstants.ATTACHMENT, result);
		assertContains(BulletinXmlExportImportConstants.FILENAME, result);
		assertContains(sampleAttachmentFile1.getName(), result);
		assertContains(sampleAttachmentFile2.getName(), result);
		assertTrue("Attachment 1 doesn't exist?", exportedAttachmentFile1.exists());
		assertTrue("Attachment 2 doesn't exist?", exportedAttachmentFile2.exists());
		assertEquals("Attachment 1's data doesn't match export?", UnicodeReader.getFileContents(sampleAttachmentFile1), UnicodeReader.getFileContents(exportedAttachmentFile1));
		assertEquals("Attachment 2's data doesn't match export?", UnicodeReader.getFileContents(sampleAttachmentFile2), UnicodeReader.getFileContents(exportedAttachmentFile2));
		exportedAttachmentFile1.delete();
		exportedAttachmentFile2.delete();

	}

	public void testExportWithPrivateAttachment() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.setAllPrivate(false);
		final File sampleAttachmentFile1 = addNewPrivateSampleAttachment(b);
		File exportedAttachmentFile1 = new File(attachmentDirectory, sampleAttachmentFile1.getName()); 
		app.getStore().saveBulletin(b);
		Vector list = new Vector();
		list.add(b);

		String publicOnly = doExport(list, false, true, false);
		assertNotContains(sampleAttachmentFile1.getName(), publicOnly);
		assertNotContains(BulletinXmlExportImportConstants.TOP_SECTION_ATTACHMENT_LIST, publicOnly);
		assertNotContains(BulletinXmlExportImportConstants.BOTTOM_SECTION_ATTACHMENT_LIST, publicOnly);

		String publicAndPrivate = doExport(list, true, true, false);
		assertNotContains(BulletinXmlExportImportConstants.TOP_SECTION_ATTACHMENT_LIST, publicAndPrivate);
		assertContains(BulletinXmlExportImportConstants.BOTTOM_SECTION_ATTACHMENT_LIST, publicAndPrivate);
		assertContains(sampleAttachmentFile1.getName(), publicAndPrivate);
		assertTrue("Private Attachment 1 doesn't exist?", exportedAttachmentFile1.exists());
		assertEquals("Private Attachment 1's data doesn't match export?", UnicodeReader.getFileContents(sampleAttachmentFile1), UnicodeReader.getFileContents(exportedAttachmentFile1));
	}

	public void testExportAttachmentFailure() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.setAllPrivate(false);
		final File sampleAttachmentFile1 = addNewPrivateSampleAttachment(b);
		File exportedAttachmentFile1 = new File(attachmentDirectory, sampleAttachmentFile1.getName());
		assertFalse(exportedAttachmentFile1.exists());
		//Don't save the bulletin to ensure a failing attachment export.
		Vector list = new Vector();
		list.add(b);

		String publicAndPrivate = doExport(list, true, true, false);
		assertNotContains(BulletinXmlExportImportConstants.TOP_SECTION_ATTACHMENT_LIST, publicAndPrivate);
		assertContains(BulletinXmlExportImportConstants.BOTTOM_SECTION_ATTACHMENT_LIST, publicAndPrivate);
		assertContains(sampleAttachmentFile1.getName(), publicAndPrivate);
		assertContains("<ErrorExportingAttachment>"+sampleAttachmentFile1.getName()+"</ErrorExportingAttachment>", publicAndPrivate);
		assertFalse("exported Private Attachment 1 exists?", exportedAttachmentFile1.exists());
		assertEquals("Should have 1 failing attachment?",1, failingAttachments);
	}

	public void testExportMultipleBulletins() throws Exception
	{
		Bulletin b1 = new Bulletin(store.getSignatureGenerator());
		b1.setAllPrivate(false);
		b1.setImmutable();
		Bulletin b2 = new Bulletin(store.getSignatureGenerator());
		b2.setAllPrivate(false);

		File sampleAttachmentFile1 = addNewPublicSampleAttachment(b1, "Attachment 1's Data");
		File sampleAttachmentFile2 = addNewPublicSampleAttachment(b2, "Attachment 2's Data");

		AttachmentProxy ap1 = b1.getPublicAttachments()[0];  
		AttachmentProxy ap2 = b2.getPublicAttachments()[0];
		ap2.setLabel(ap1.getLabel()); //Now both attachments have the same name
		
		app.getStore().saveBulletin(b1);
		app.getStore().saveBulletin(b2);

		final String sampleTitle1 = "a big event took place!";
		final String sampleTitle2 = "watch this space";
		b1.set(BulletinConstants.TAGTITLE, sampleTitle1);
		b2.set(BulletinConstants.TAGTITLE, sampleTitle2);

		Vector list = new Vector();
		list.add(b1);
		list.add(b2);
		String result = doExport(list, true, true, false);

		assertContains(sampleTitle1, result);
		assertContains("<Field tag='title'", result);
		assertContains(sampleTitle2, result);
		assertContains("<BulletinStatus>draft</BulletinStatus>", result);
		assertContains("<BulletinStatus>sealed</BulletinStatus>", result);
		assertContains("<LocalizedBulletinStatus>"+draftTranslation+"</LocalizedBulletinStatus>", result);
		assertContains("<LocalizedBulletinStatus>"+sealedTranslation+"</LocalizedBulletinStatus>", result);
		assertEquals("Has failing attachments?",0, failingAttachments);
		
		StringInputStreamWithSeek stream = new StringInputStreamWithSeek(result);
		XmlBulletinsImporter importer = new XmlBulletinsImporter(store.getSignatureGenerator(), stream, attachmentDirectory);
		Bulletin[] resultingBulletins = importer.getBulletins();
		Bulletin imported1 = resultingBulletins[0];
		Bulletin imported2 = resultingBulletins[1];
		app.getStore().saveBulletin(imported1);
		app.getStore().saveBulletin(imported2);
		
		ReadableDatabase db = app.getStore().getDatabase();
		File imported1File = imported1.getAsFileProxy(imported1.getPublicAttachments()[0],db,Bulletin.STATUSMUTABLE).getFile();
		File imported2File = imported2.getAsFileProxy(imported2.getPublicAttachments()[0],db,Bulletin.STATUSMUTABLE).getFile();
		assertEquals("attachment 1's data doesn't match?", UnicodeReader.getFileContents(sampleAttachmentFile1), UnicodeReader.getFileContents(imported1File));
		assertEquals("attachment 2's data doesn't match?", UnicodeReader.getFileContents(sampleAttachmentFile2), UnicodeReader.getFileContents(imported2File));
	}

	public void testExportPrivateData() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.setAllPrivate(false);

		final String samplePublic = "someone special";
		final String samplePrivate = "shhhhh! it's private!";
		final String encodedSamplePrivate = XmlUtilities.getXmlEncoded(samplePrivate);

		b.set(BulletinConstants.TAGPUBLICINFO, samplePublic);
		b.set(BulletinConstants.TAGPRIVATEINFO, samplePrivate);		

		Vector list = new Vector();
		list.add(b);
		String publicOnly = doExport(list, false, false, false);
		assertContains("<NoAttachmentsExported></NoAttachmentsExported>", publicOnly);
		assertContains(samplePublic, publicOnly);
		assertNotContains(samplePrivate, publicOnly);

		String publicAndPrivate = doExport(list, true, false, false);
		assertContains("<PublicAndPrivateData></PublicAndPrivateData>", publicAndPrivate);
		
		assertContains("<FieldValues>", publicAndPrivate);
		assertContains("<MainFieldSpecs>", publicAndPrivate);
		assertContains("<PrivateFieldSpecs>", publicAndPrivate);
		
		assertContains(samplePublic, publicAndPrivate);
		assertContains(encodedSamplePrivate, publicAndPrivate);
	}

	public void testExportAnAllPrivateBulletin() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.setAllPrivate(true);
		final String sampleAuthor = "someone special";
		b.set(BulletinConstants.TAGAUTHOR, sampleAuthor);

		Vector list = new Vector();
		list.add(b);
		String publicOnly = doExport(list, false, false, false);

		assertNotContains(b.getAccount(), publicOnly);
		assertNotContains(b.getLocalId(), publicOnly);
		assertNotContains("<AllPrivate></AllPrivate>", publicOnly);
		assertNotContains(sampleAuthor, publicOnly);
		assertNotContains("<FieldValues>", publicOnly);
		assertContains("<!--  No Private FieldSpecs or Data was exported  -->", publicOnly);
		assertNotContains("<MainFieldSpecs>", publicOnly);
		assertNotContains("<PrivateFieldSpecs>", publicOnly);

		String publicAndPrivate = doExport(list, true, false, false);
		assertContains(b.getAccount(), publicAndPrivate);
		assertContains(b.getLocalId(), publicAndPrivate);
		assertContains("<AllPrivate></AllPrivate>", publicAndPrivate);
		assertContains(sampleAuthor, publicAndPrivate);
		assertContains("<FieldValues>", publicAndPrivate);
		assertContains("<MainFieldSpecs>", publicAndPrivate);
		assertContains("<PrivateFieldSpecs>", publicAndPrivate);
	}

	public void testExportCustomFieldValue() throws Exception
	{
		String customTag1 = "custom1";
		String customTag2 = "custom2";
		String label1 = "Witness1 name";
		String label2 = "Witness2 name";					
		
		String xmlFieldType = "<CustomFields><Field><Tag>"+customTag1+"</Tag>" +
			"<Label>" + label1 + "</Label></Field></CustomFields>";
		FieldSpec newSpec1 = FieldCollection.parseXml(xmlFieldType).get(0); 
		xmlFieldType = "<CustomFields><Field><Tag>"+customTag2+"</Tag>" +
			"<Label>" + label2 + "</Label></Field></CustomFields>";
		FieldSpec newSpec2 = FieldCollection.parseXml(xmlFieldType).get(0); 
		FieldSpec[] extraFieldSpecs = {newSpec1, newSpec2};
		FieldCollection fields = FieldCollectionForTesting.extendFields(StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray(), extraFieldSpecs);
		
		Bulletin b = new Bulletin(store.getSignatureGenerator(), fields.getSpecs(), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		b.setAllPrivate(false);
		
		FieldDataPacket fdp = b.getFieldDataPacket();
		assertTrue("contain custom fied?", fdp.fieldExists(customTag1));
		assertTrue("contain custom field?", fdp.fieldExists(customTag2));
		
		final String samplePublic = "public name";		
		b.set(BulletinConstants.TAGPUBLICINFO, samplePublic);				
		final String sampleAuthor = "John Smith";
		b.set(BulletinConstants.TAGAUTHOR, sampleAuthor);				
										
		b.set(customTag1, "a<bc");
		b.set(customTag2, "&test");			

		Vector list = new Vector();
		list.add(b);

		String result = doExport(list, false, false, false);

		assertContains(samplePublic, result);
		assertContains("<FieldValues>", result);
		assertContains("<Value>a&lt;bc</Value>", result);
		assertContains("<Value>&amp;test</Value>", result);

		b.set(customTag1, ">");
		b.set(customTag2, "&");			

		list = new Vector();
		list.add(b);

		result = doExport(list, false, false, false);

		assertContains(samplePublic, result);
		assertContains("<FieldValues>", result);
		assertContains("<Value>&gt;</Value>", result);
		assertContains("<Value>&amp;</Value>", result);						
		
	}

	public void testExportCustomFieldSingleCharacterOfTagAndLabel() throws Exception
	{
		String customTag1 = "A";
		String customTag2 = "custom";
		String label1 = "Witness1 name";
		String label2 = "N";					
		
		String xmlFieldType = "<CustomFields><Field><Tag>"+customTag1+"</Tag>" +
			"<Label>" + label1 + "</Label></Field></CustomFields>";
		FieldSpec newSpec1 = FieldCollection.parseXml(xmlFieldType).get(0); 
		xmlFieldType = "<CustomFields><Field><Tag>"+customTag2+"</Tag>" +
			"<Label>" + label2 + "</Label></Field></CustomFields>";
		FieldSpec newSpec2 = FieldCollection.parseXml(xmlFieldType).get(0); 

		FieldSpec[] extraFieldSpecs = {newSpec1, newSpec2};
		FieldCollection fields = FieldCollectionForTesting.extendFields(StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray(), extraFieldSpecs);
		
		Bulletin b = new Bulletin(store.getSignatureGenerator(), fields.getSpecs(), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		b.setAllPrivate(false);				
										
		b.set(customTag1, "abc");
		b.set(customTag2, "test");			

		Vector list = new Vector();
		list.add(b);

		String result = doExport(list, false, false, false);
		
		assertContains("<FieldValues>", result);
		assertContains("<Tag>A</Tag>", result);
		assertContains("<Label>N</Label>", result);		
	}

	public void testXmlEscaping() throws Exception
	{
		String needsEscaping = "a < b && b > c";

		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.set(Bulletin.TAGAUTHOR, needsEscaping);
		final String result = getExportedXml(b);
		assertNotContains("exported unescaped?", needsEscaping, result);
		assertContains("didn't write escaped?", "a &lt; b &amp;&amp; b &gt; c", result);
	}

	public void testExportSimpleDate() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		String entryDate = b.get(Bulletin.TAGENTRYDATE);
		final String result = getExportedXml(b);
		assertContains("didn't write good simple date?", "Simple:"+entryDate, result);
	}

	public void testExportDateRange() throws Exception
	{
		String rawDateRangeString = createSampleDateRangeString();

		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.set(Bulletin.TAGEVENTDATE, rawDateRangeString);
		final String result = getExportedXml(b);
		assertNotContains("exported raw flexidate?", rawDateRangeString, result);
		assertContains("didn't write good date range?", "Range:2005-05-01,2005-05-30", result);
	}

	private String getExportedXml(Bulletin b) throws Exception
	{
		Vector list = new Vector();
		list.add(b);
		final String result = doExport(list, true, false, false);
		return result;
	}

	private String createSampleDateRangeString()
	{
		final int MAY = 5;
		MultiCalendar beginDate = MultiCalendar.createFromGregorianYearMonthDay(2005, MAY, 1);
		MultiCalendar endDate = MultiCalendar.createFromGregorianYearMonthDay(2005, MAY, 30);
		String rawDateRangeString = MartusFlexidate.toBulletinFlexidateFormat(beginDate, endDate);
		return rawDateRangeString;
	}
	
	public void testExportGridDateRange() throws Exception
	{
		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setTag("grid");
		FieldSpec dateRangeSpec = FieldSpec.createCustomField("range", "Date Range", new FieldTypeDateRange());
		gridSpec.addColumn(dateRangeSpec);
		
		GridData data = new GridData(gridSpec, noReusableChoices);
		data.addEmptyRow();
		String rawDateRangeString = createSampleDateRangeString();
		data.setValueAt(rawDateRangeString, 0, 0);
		
		FieldSpec[] publicSpecs = new FieldSpec[] {gridSpec};
		FieldSpecCollection privateSpecs = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		Bulletin b = new Bulletin(store.getSignatureGenerator(), new FieldSpecCollection(publicSpecs), privateSpecs);
		b.set(gridSpec.getTag(), data.getXmlRepresentation());
		
		final String result = getExportedXml(b);
		assertNotContains("exported raw flexidate?", rawDateRangeString, result);
		assertContains("didn't write good date range?", "2005-05-01,2005-05-30", result);
	}

// TODO: uncomment and implement based on Barbra's decision
//	public void testExportSnapshot() throws Exception
//	{
//		Bulletin b = new Bulletin(store.getSignatureGenerator());
//		final String exportWithoutSnapshot = getExportedXml(b);
//		assertNotContains("<Snapshot></Snapshot>", exportWithoutSnapshot);
//		
//		b.changeState(BulletinState.STATE_SNAPSHOT);
//		final String exportWithSnapshot = getExportedXml(b);
//		assertContains("<Snapshot></Snapshot>", exportWithSnapshot);
//	}
//	
//	public void testExportImmutableOnServer() throws Exception
//	{
//		Bulletin b = new Bulletin(store.getSignatureGenerator());
//		final String exportWithoutSnapshot = getExportedXml(b);
//		assertNotContains("<ImmutableOnServer></ImmutableOnServer>", exportWithoutSnapshot);
//		
//		b.setImmutableOnServer(true);
//		final String exportWithSnapshot = getExportedXml(b);
//		assertContains("<ImmutableOnServer></ImmutableOnServer>", exportWithSnapshot);
//	}

	public void testEndToEndExportAndThenImport() throws Exception
	{
		FieldSpecCollection topSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection bottomSpecs = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		String choice1 = "choice A";
		String choice2 = "choice B";

		ChoiceItem[] choicesNoDups = {new ChoiceItem("", ""), new ChoiceItem("no Dup", choice1), new ChoiceItem("second", choice2)};
		String dropdownTag = "ddTag";
		String dropdownLabel = "dropdown column label";
		DropDownFieldSpec dropDownSpec = new CustomDropDownFieldSpec();
		dropDownSpec.setChoices(choicesNoDups);
		dropDownSpec.setLabel(dropdownLabel);
		dropDownSpec.setTag(dropdownTag);
		
		String booleanLabel = "Boolean Label";
		String booleanTag = "TagBoolean";
		FieldSpec booleanSpec = FieldSpec.createFieldSpec(booleanLabel, new FieldTypeBoolean());
		booleanSpec.setTag(booleanTag);
		String dateLabel = "Simple Date Label";
		String dateTag = "TagDate";
		FieldSpec dateSpec = FieldSpec.createFieldSpec(dateLabel, new FieldTypeDate());
		dateSpec.setTag(dateTag);
		
		GridFieldSpec gridSpec = new GridFieldSpec();
		String gridTag = "GridTag";
		String gridLabel = "Grid Label";
		gridSpec.setTag(gridTag);
		gridSpec.setLabel(gridLabel);
		gridSpec.addColumn(dropDownSpec);
		gridSpec.addColumn(booleanSpec);
		gridSpec.addColumn(dateSpec);

		String messageLabel = "message Label";
		String messageTag = "messageTag";
		FieldSpec messageSpec = FieldSpec.createFieldSpec(messageLabel, new FieldTypeMessage());
		messageSpec.setTag(messageTag);
		
		topSpecs = TestCustomFieldSpecValidator.addFieldSpec(topSpecs, gridSpec);
		bottomSpecs = TestCustomFieldSpecValidator.addFieldSpec(bottomSpecs,dropDownSpec);
		bottomSpecs = TestCustomFieldSpecValidator.addFieldSpec(bottomSpecs,booleanSpec);
		bottomSpecs = TestCustomFieldSpecValidator.addFieldSpec(bottomSpecs, messageSpec);
		
		Bulletin exported = new Bulletin(store.getSignatureGenerator(), topSpecs, bottomSpecs);
		exported.setAllPrivate(false);

		GridData gridData = new GridData(gridSpec, noReusableChoices);
		GridRow row = new GridRow(gridSpec, noReusableChoices);
		row.setCellText(0, choice1);
		row.setCellText(1, "True");
		row.setCellText(2, "20060504");
		gridData.addRow(row);
		exported.set(gridTag, gridData.getXmlRepresentation());
		String sampleAuthor = "someone special";
		exported.set(BulletinConstants.TAGAUTHOR, sampleAuthor);
		exported.set(BulletinConstants.TAGLANGUAGE, MiniLocalization.ENGLISH);
		exported.set(BulletinConstants.TAGEVENTDATE, "1970-01-01,19700101+0");
		String privateDate = "20060508";
		exported.set(dateTag, privateDate);
		exported.set(dropdownTag, choice2);
		exported.set(booleanTag, "False");
		exported.set(BulletinConstants.TAGPRIVATEINFO, "Private Data");
		exported.setImmutable();

		String localId1 = "pretend local id";
		String localId2 = "another fake local id";

		BulletinHistory fakeHistory = new BulletinHistory();
		fakeHistory.add(localId1);
		fakeHistory.add(localId2);
		
		exported.setHistory(fakeHistory);
		
		Vector list = new Vector();
		list.add(exported);
		String result = doExport(list, true, false, false);
		StringInputStreamWithSeek stream = new StringInputStreamWithSeek(result);
		XmlBulletinsImporter importer = new XmlBulletinsImporter(store.getSignatureGenerator(), stream);
		Bulletin[] resultingBulletins = importer.getBulletins();
		Bulletin imported = resultingBulletins[0];

		assertNotEquals("Should have created a brand new bulletin", exported.getLocalId(), imported.getLocalId());
		assertEquals(exported.getTopSectionFieldSpecs(), imported.getTopSectionFieldSpecs());
		assertEquals(exported.getBottomSectionFieldSpecs(), imported.getBottomSectionFieldSpecs());
		
		assertTrue("exported should be a immutable", exported.isImmutable());
		assertTrue("Import should always be mutable", imported.isMutable());
		assertFalse("exported should be public", exported.isAllPrivate());
		assertTrue("Import should always be private", imported.isAllPrivate());
		assertEquals("exported should be at version 3", 3, exported.getVersion());
		assertEquals("Import should start a version 1", 1, imported.getVersion());
		assertEquals("export should have a history", 2, exported.getHistory().size());
		assertEquals("Import should not have a history", 0, imported.getHistory().size());
		verifyMatchingData(topSpecs.asArray(), exported, imported);
		verifyMatchingData(bottomSpecs.asArray(), exported, imported);
	}
	
	public void testBadAttachments()throws Exception
	{
		Bulletin b1 = new Bulletin(store.getSignatureGenerator());
		b1.setAllPrivate(false);
		b1.setImmutable();

		File sampleAttachmentFile1 = addNewPublicSampleAttachment(b1, "Attachment 1's Data");
		File sampleAttachmentFile2 = addNewPublicSampleAttachment(b1, "Attachment 2's Data");
		File sampleAttachmentFile3 = addNewPublicSampleAttachment(b1, "Attachment 3's Data");

		
		app.getStore().saveBulletin(b1);

		String bulletinTitle = "attachment test";
		b1.set(BulletinConstants.TAGTITLE, bulletinTitle);

		Vector list = new Vector();
		list.add(b1);
		String result = doExport(list, true, true, false);

		assertEquals("Has failing attachments?",0, failingAttachments);
		File missingAttachment1 = new File(attachmentDirectory, sampleAttachmentFile1.getName());
		missingAttachment1.delete();
		File missingAttachment3 = new File(attachmentDirectory, sampleAttachmentFile3.getName());
		missingAttachment3.delete();
		
		StringInputStreamWithSeek stream = new StringInputStreamWithSeek(result);
		XmlBulletinsImporter importer = new XmlBulletinsImporter(store.getSignatureGenerator(), stream, attachmentDirectory);
		Bulletin[] resultingBulletins = importer.getBulletins();
		Bulletin imported1 = resultingBulletins[0];

		HashMap attachmentErrors = importer.getMissingAttachmentsMap();
		assertEquals("Should have 1 error", 1, attachmentErrors.size());
		
		String[] bulletinTitlesWithAttachmentErrors = new String[attachmentErrors.size()]; 
		attachmentErrors.keySet().toArray(bulletinTitlesWithAttachmentErrors);
		String titleOfBulletinWithMissingAttachment = bulletinTitlesWithAttachmentErrors[0];
		assertEquals("Wrong bulletin Title of failing attachment?", bulletinTitle, titleOfBulletinWithMissingAttachment);
		
		String listOfFailingAttachmentsForBulletin = sampleAttachmentFile1.getName() + ", " + sampleAttachmentFile3.getName();
		assertEquals("Wrong attachment Title of failing attachment?", listOfFailingAttachmentsForBulletin , attachmentErrors.get(titleOfBulletinWithMissingAttachment));
		
		assertEquals("Should only have 1 attachment", 1, imported1.getPublicAttachments().length);
		
		app.getStore().saveBulletin(imported1);
		ReadableDatabase db = app.getStore().getDatabase();
		File imported2File = imported1.getAsFileProxy(imported1.getPublicAttachments()[0],db,Bulletin.STATUSMUTABLE).getFile();
		assertEquals("attachment 2's data doesn't match?", UnicodeReader.getFileContents(sampleAttachmentFile2), UnicodeReader.getFileContents(imported2File));
	}
	

	private void verifyMatchingData(FieldSpec[] specs, Bulletin b, Bulletin b2)
	{
		for(int i = 0; i < specs.length; ++i)
		{
			String tag = specs[i].getTag();
			assertEquals("Data not the same for:"+tag, b.get(tag), b2.get(tag));
		}
	}
	
	
	String doExport(Vector list, boolean includePrivateData, boolean includeAttachments, boolean includeAllVersions) throws Exception
	{
		StringWriter writer = new StringWriter();
		MiniLocalization miniLocalization = new MiniLocalization();
		miniLocalization.addEnglishTranslations(new String[]{"status:draft="+draftTranslation, "status:sealed="+sealedTranslation});
		miniLocalization.setCurrentLanguageCode(MiniLocalization.ENGLISH);
		BulletinXmlExporter exporter = new BulletinXmlExporter(app, miniLocalization, null);
		exporter.exportBulletins(writer, list, includePrivateData, includeAttachments, includeAllVersions, attachmentDirectory);
		failingAttachments = exporter.getNumberOfFailingAttachments();
		String result = writer.toString();
		return result;
	}
	
	File addNewPublicSampleAttachment(Bulletin b, String attachmentData)
		throws IOException, EncryptionException
	{
		final File sampleAttachmentFile = createTempFile();
		UnicodeWriter writer = new UnicodeWriter(sampleAttachmentFile);
		writer.write(attachmentData);
		writer.close();
		AttachmentProxy ap = new AttachmentProxy(sampleAttachmentFile);
		b.addPublicAttachment(ap);
		return sampleAttachmentFile;
	}


	File addNewPrivateSampleAttachment(Bulletin b)
		throws IOException, EncryptionException
	{
		final File sampleAttachmentFile = createTempFile();
		AttachmentProxy ap = new AttachmentProxy(sampleAttachmentFile);
		b.addPrivateAttachment(ap);
		return sampleAttachmentFile;
	}
	
	private static final String VERSION_1_TITLE = "Version 1";
	private static final String VERSION_2_TITLE = "Version 2";
	private static final String VERSION_3_TITLE = "Version 3";


	static final String draftTranslation = "Draft";
	static final String sealedTranslation = "Sealed";
	static ClientBulletinStore store;
	static MockMartusApp app;
	static File attachmentDirectory;
	int failingAttachments;
	private PoolOfReusableChoicesLists noReusableChoices;
}
