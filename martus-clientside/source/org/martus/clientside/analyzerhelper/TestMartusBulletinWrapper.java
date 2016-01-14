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
package org.martus.clientside.analyzerhelper;

import java.io.File;

import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.test.MockBulletinStore;
import org.martus.util.TestCaseEnhanced;


public class TestMartusBulletinWrapper extends TestCaseEnhanced
{
	public TestMartusBulletinWrapper(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
	  	super.setUp();
	  	if(security == null)
	  	{
			security = new MartusSecurity();
			security.createKeyPair(512);
			fosecurity = new MartusSecurity();
			fosecurity.createKeyPair(512);
	  	}
	}
	
	public void testBasics() throws Exception
	{
		Bulletin bulletin = new Bulletin(security);
		String author = "author";
		String title = "title";
		String location = "location";
		String privateData = "private";
		bulletin.set(BulletinConstants.TAGAUTHOR, author);
		bulletin.set(BulletinConstants.TAGTITLE, title);
		bulletin.set(BulletinConstants.TAGLOCATION, location);
		bulletin.set(BulletinConstants.TAGPRIVATEINFO, privateData);
		bulletin.setMutable();
		
		File tempDirectory = createTempFileFromName("$$$TestBulletinWrapper");
		tempDirectory.deleteOnExit();
		tempDirectory.delete();
		tempDirectory.mkdirs();

		MockBulletinStore store = new MockBulletinStore(this);
		store.saveEncryptedBulletinForTesting(bulletin);
		File bulletinZipFile = createTempFileFromName("$$$TestBulletinWrapperZipFile");
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(store.getDatabase(), bulletin.getDatabaseKey(), bulletinZipFile, security);
		
		MartusBulletinWrapper bulletinWrapper = new MartusBulletinWrapper(bulletin.getUniversalId(), bulletinZipFile, security);
		assertEquals("Data for author not correct?", author, bulletinWrapper.getAuthor());
		assertEquals("Data for title not correct?", title, bulletinWrapper.getTitle());
		assertEquals("Data for location not correct?", location, bulletinWrapper.getLocation());
		assertEquals("PrivateData not visible?", privateData, bulletinWrapper.getPrivateInfo());
		bulletinWrapper.deleteAllData();
		bulletinZipFile.delete();
		store.deleteAllData();
	}
	
	public void testHQAuthorized() throws Exception
	{
		Bulletin bulletin = new Bulletin(fosecurity);
		String author = "author";
		String title = "title";
		String location = "location";
		String privateData = "private";
		String entryDate = "2004-01-23";
		
		bulletin.set(BulletinConstants.TAGAUTHOR, author);
		bulletin.set(BulletinConstants.TAGTITLE, title);
		bulletin.set(BulletinConstants.TAGLOCATION, location);
		bulletin.set(BulletinConstants.TAGPRIVATEINFO, privateData);
		bulletin.set(BulletinConstants.TAGENTRYDATE, entryDate);
		bulletin.set(BulletinConstants.TAGEVENTDATE, "2003-08-20,20030820+3");
		bulletin.setImmutable();
		
		HeadquartersKey key = new HeadquartersKey(security.getPublicKeyString());
		HeadquartersKeys keys = new HeadquartersKeys(key);
		bulletin.setAuthorizedToReadKeys(keys);
		
		
		File tempDirectory = createTempFileFromName("$$$TestBulletinHQWrapper");
		tempDirectory.deleteOnExit();
		tempDirectory.delete();
		tempDirectory.mkdirs();

		MockBulletinStore store = new MockBulletinStore(this);
		store.saveEncryptedBulletinForTesting(bulletin);
		File bulletinZipFile = createTempFileFromName("$$$TestBulletinWrapperHQZipFile");
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(store.getDatabase(), bulletin.getDatabaseKeyForLocalId(bulletin.getLocalId()), bulletinZipFile, fosecurity);
		
		MartusBulletinWrapper bulletinWrapper = new MartusBulletinWrapper(bulletin.getUniversalId(), bulletinZipFile, security);
		assertEquals("Data for author not correct?", author, bulletinWrapper.getAuthor());
		assertEquals("Data for title not correct?", title, bulletinWrapper.getTitle());
		assertEquals("Data for location not correct?", location, bulletinWrapper.getLocation());
		assertEquals("PrivateData not visible?", privateData, bulletinWrapper.getPrivateInfo());
		assertEquals("Is All Private incorrect?", bulletin.isAllPrivate(), bulletinWrapper.isAllPrivate());
		assertEquals("Entry Date incorrect?", entryDate, bulletinWrapper.getEntryDate().toIsoDateString());
		assertEquals("Event Begin Date incorrect?", "2003-08-20", bulletinWrapper.getEventDate().getBeginDate().toIsoDateString());
		assertEquals("Event End Date incorrect?", "2003-08-23", bulletinWrapper.getEventDate().getEndDate().toIsoDateString());
		assertEquals("Has Public attachments?", 0, bulletinWrapper.getPublicAttachments().length);
		assertEquals("Has Private attachments?", 0, bulletinWrapper.getPrivateAttachments().length);
		bulletinWrapper.deleteAllData();
		bulletinZipFile.delete();
		store.deleteAllData();
	}
	
	public void testAttachments() throws Exception
	{
		Bulletin bulletin = new Bulletin(security);
		bulletin.setAllPrivate(true);
		File tempFile1 = createTempFileWithData(sampleBytes1);
		File tempFile2 = createTempFileWithData(sampleBytes2);
		File tempFile3 = createTempFileWithData(sampleBytes3);
		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		AttachmentProxy a3 = new AttachmentProxy(tempFile3);
		bulletin.addPublicAttachment(a1);
		bulletin.addPublicAttachment(a2);
		bulletin.addPrivateAttachment(a3);

		bulletin.setMutable();
		
		File tempDirectory = createTempFileFromName("$$$TestBulletinWrapperAttachments");
		tempDirectory.deleteOnExit();
		tempDirectory.delete();
		tempDirectory.mkdirs();
	
		MockBulletinStore store = new MockBulletinStore(this);
		store.saveEncryptedBulletinForTesting(bulletin);
		File bulletinZipFile = createTempFileFromName("$$$TestBulletinWrapperZipFileAttachments");
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(store.getDatabase(), bulletin.getDatabaseKey(), bulletinZipFile, security);
		
		MartusBulletinWrapper bulletinWrapper = new MartusBulletinWrapper(bulletin.getUniversalId(), bulletinZipFile, security);
		assertEquals("No Public attachments?", 2, bulletinWrapper.getPublicAttachments().length);
		assertEquals("No Private attachments?", 1, bulletinWrapper.getPrivateAttachments().length);
		assertTrue("Public attachment doesn't exist?", bulletinWrapper.getPublicAttachments()[0].exists());
		assertTrue("Private attachment doesn't exist?", bulletinWrapper.getPrivateAttachments()[0].exists());

		bulletinWrapper.deleteAllData();
		assertEquals("Public attachments still exist?", 0, bulletinWrapper.getPublicAttachments().length);
		assertEquals("No Private attachments still exist?", 0, bulletinWrapper.getPrivateAttachments().length);
	}

	public void testHTML() throws Exception
	{
		Bulletin bulletin = new Bulletin(fosecurity);
		String author = "author3";
		String title = "title4";
		String location = "location5";
		String privateData = "private6";
		String entryDate = "2004-01-23";
		
		bulletin.set(BulletinConstants.TAGAUTHOR, author);
		bulletin.set(BulletinConstants.TAGTITLE, title);
		bulletin.set(BulletinConstants.TAGLOCATION, location);
		bulletin.set(BulletinConstants.TAGPRIVATEINFO, privateData);
		bulletin.set(BulletinConstants.TAGENTRYDATE, entryDate);
		bulletin.set(BulletinConstants.TAGEVENTDATE, "2003-08-20,20030820+3");

		File tempFile1 = createTempFileWithData(sampleBytes1);
		File tempFile2 = createTempFileWithData(sampleBytes2);
		File tempFile3 = createTempFileWithData(sampleBytes3);
		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		AttachmentProxy a3 = new AttachmentProxy(tempFile3);
		bulletin.addPublicAttachment(a1);
		bulletin.addPublicAttachment(a2);
		bulletin.addPrivateAttachment(a3);
		
		bulletin.setImmutable();
		
		HeadquartersKey key = new HeadquartersKey(security.getPublicKeyString(), "My HQ");
		HeadquartersKeys keys = new HeadquartersKeys(key);
		bulletin.setAuthorizedToReadKeys(keys);
		
		
		File tempDirectory = createTempFileFromName("$$$TestBulletinHQWrapper");
		tempDirectory.deleteOnExit();
		tempDirectory.delete();
		tempDirectory.mkdirs();

		MockBulletinStore store = new MockBulletinStore(this);
		store.saveEncryptedBulletinForTesting(bulletin);
		File bulletinZipFile = createTempFileFromName("$$$TestBulletinWrapperHQZipFile");
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(store.getDatabase(), bulletin.getDatabaseKeyForLocalId(bulletin.getLocalId()), bulletinZipFile, fosecurity);
		
		MiniLocalization localization = new MiniLocalization();
		MartusBulletinWrapper bulletinWrapper = new MartusBulletinWrapper(bulletin.getUniversalId(), bulletinZipFile, security);
		String expectedHtmlResult = "<html><table width='100'>\n<tr><td width='15%'></td><td width='85%'></td></tr>\n" +
				"<tr><td align='right' valign='top'>Last Saved</td><td align='left' valign='top'>"+
				localization.formatDateTime(bulletin.getLastSavedTime())+"</td></tr>\n" +
				"<tr><td align='right' valign='top'>Version</td><td align='left' valign='top'>1</td></tr>\n"+
				"<tr><td align='right' valign='top'>Bulletin Status:</td><td align='left' valign='top'>Sealed</td></tr>\n"+
				"<tr></tr>\n" + 
				"<tr><td align='right' valign='top'>Contact Bulletin</td><td align='left' valign='top'></td></tr>\n"+
				"<tr></tr>\n" + 
				"<tr><td colspan='2' align='left'><u><b>Private Information</b></u></td></tr>\n"+
				"<tr><td align='right' valign='top'>Keep ALL Information Private</td><td align='left' valign='top'>Yes</td></tr>\n"+
				"<tr><td align='right' valign='top'>Language</td><td align='left' valign='top'>-Other-</td></tr>\n"+
				"<tr><td align='right' valign='top'>Author</td><td align='left' valign='top'>"+author+"</td></tr>\n"+
				"<tr><td align='right' valign='top'>Organization</td><td align='left' valign='top'></td></tr>\n"+
				"<tr><td align='right' valign='top'>Title</td><td align='left' valign='top'><strong>"+title+"</strong></td></tr>\n"+
				"<tr><td align='right' valign='top'>Location</td><td align='left' valign='top'>"+location+"</td></tr>\n"+
				"<tr><td align='right' valign='top'>Keywords</td><td align='left' valign='top'></td></tr>\n"+
				"<tr><td align='right' valign='top'>Date of Event</td><td align='left' valign='top'>08/20/2003 - 08/23/2003</td></tr>\n"+
				"<tr><td align='right' valign='top'>Date Created</td><td align='left' valign='top'>01/23/2004</td></tr>\n"+
				"<tr><td align='right' valign='top'>Summary</td><td align='left' valign='top'><p></p></td></tr>\n"+
				"<tr><td align='right' valign='top'>Details</td><td align='left' valign='top'><p></p></td></tr>\n"+
				"<tr><td align='right' valign='top'>Attachments</td><td align='left' valign='top'><p>"+tempFile1.getName()+"    ( 1 Kb )</p><p>"+tempFile2.getName()+"    ( 1 Kb )</p></td></tr>\n"+
				"<tr></tr>\n" + 
				"<tr><td colspan='2' align='left'><u><b>Private Information</b></u></td></tr>\n"+
				"<tr><td align='right' valign='top'>Additional Information</td><td align='left' valign='top'><p>"+privateData+"</p><p></p></td></tr>\n"+
				"<tr><td align='right' valign='top'>Attachments</td><td align='left' valign='top'><p>"+tempFile3.getName()+"    ( 1 Kb )</p></td></tr>\n"+
				"<tr></tr>\n" + 
				"<tr><td colspan='2' align='left'><u><b>Contacts</b></u></td></tr>\n"+
				"<tr><td align='right' valign='top'></td><td align='left' valign='top'>"+key.getFormattedPublicCode()+"</td></tr>\n"+
				"<p></p>" +
				"<tr></tr>\n" + 
				"<tr><td align='right' valign='top'>Bulletin Id:</td><td align='left' valign='top'>"+bulletin.getLocalId()+"</td></tr>\n"+
				"</table></html>";				
	
		assertEquals("Html not the same?", expectedHtmlResult, bulletinWrapper.getHTML());
		bulletinWrapper.deleteAllData();
		bulletinZipFile.delete();
		store.deleteAllData();
	}
	
	
	private MartusSecurity security;
	private MartusSecurity fosecurity;

	static final byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
	static final byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};
	static final byte[] sampleBytes3 = {6,5,0,4,7,5,5,4,4,0};
}
