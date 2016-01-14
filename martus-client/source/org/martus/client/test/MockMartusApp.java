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

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.clientside.UiLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.test.UnicodeConstants;
import org.martus.util.DirectoryUtils;

public class MockMartusApp extends MartusApp
{
	public static MockMartusApp create(MartusCrypto crypto, String testName) throws Exception
	{
		return create(createFakeDataDirectory(testName), crypto);
	}

	public static MockMartusApp create(MartusCrypto crypto, UiLocalization localization, String testName) throws Exception
	{
		return create(createFakeDataDirectory(testName), crypto, localization);
	}

	public static MockMartusApp create(Database db, String testName) throws Exception
	{
		MockMartusApp app = create(createFakeDataDirectory(testName), createFakeSecurity());
		app.store = new MockBulletinStore(db, app.getSecurity());
		return app;

	}

	public static MockMartusApp create(Database db, UiLocalization localizationToUse, String testName) throws Exception
		{
			MockMartusApp app = create(createFakeDataDirectory(testName), createFakeSecurity(), localizationToUse);
			app.store = new MockBulletinStore(db, app.getSecurity());
			return app;

		}

	private static MockMartusApp create(File fakeDataDirectory, MartusCrypto crypto) throws Exception 
	{
		UiLocalization emptyLocalization = createFakeLocalization(fakeDataDirectory);
		return create(fakeDataDirectory, crypto, emptyLocalization);
	}

	private static MockMartusApp create(File fakeDataDirectory, MartusCrypto crypto, UiLocalization emptyLocalization) throws Exception 
	{
		MockMartusApp app = new MockMartusApp(crypto, fakeDataDirectory, emptyLocalization);
		initializeMockApp(app, fakeDataDirectory);
		return app;
	}

	public static void initializeMockApp(MockMartusApp app, File fakeDataDirectory) throws Exception
	{
		app.setCurrentAccount("some user", app.getMartusDataRootDirectory());
		app.doAfterSigninInitalization(fakeDataDirectory, new MockClientDatabase());
		app.store.createFieldSpecCacheFromDatabase();
	}

	public static MockMartusApp create(String testName) throws Exception
	{
		return create(createFakeDataDirectory(testName), createFakeSecurity());
	}

	protected MockMartusApp(MartusCrypto crypto, File dataDirectoryToUse, UiLocalization localizationToUse) throws MartusAppInitializationException
	{
		super(crypto, dataDirectoryToUse, localizationToUse);
	}

	
	public static MockMartusSecurity createFakeSecurity() throws Exception
	{
		return MockMartusSecurity.createClient();
	}
	
	public static File createFakeDataDirectory(String name) throws IOException
	{
		File fakeDataDirectory = File.createTempFile("$$$MockMartusApp_" + name, null);
		fakeDataDirectory.deleteOnExit();
		fakeDataDirectory.delete();
		fakeDataDirectory.mkdir();
		return fakeDataDirectory;
	}
	
	public static MartusLocalization createFakeLocalization(File fakeDataDirectory)
	{
		return new MartusLocalization(fakeDataDirectory, new String[0]);
	}


	
	public MtfAwareLocalization getLocalization()
	{
		return localization;
	}

	public Database getWriteableDatabase()
	{
		return (Database)store.getDatabase();
	}
	
	public void deleteAllFiles() throws Exception
	{
		getStore().deleteAllData();
		
		getUploadInfoFile().delete();
		if(getUploadInfoFile().exists())
			throw new IOException("uploadInfoFile");

		File defaultKeyPair = getKeyPairFile(getMartusDataRootDirectory());
		defaultKeyPair.delete();
		if(defaultKeyPair.exists())
			throw new IOException("getKeyPairFile");

		getBackupFile(defaultKeyPair).delete();
		if(getBackupFile(defaultKeyPair).exists())
			throw new IOException("getBackupFile ");

		// NOTE: We need to delete the top-level configinfo file, 
		// not the current account confinginfo file
		File configInfo = getConfigInfoFileForAccount(getMartusDataRootDirectory());
		configInfo.delete();
		if(configInfo.exists())
			throw new IOException("configInfo");

		// NOTE: We need to delete the top-level configinfo sig file,
		// not the current account configinfo sig file
		File sigFile = getConfigInfoSignatureFileForAccount(getMartusDataRootDirectory());
		sigFile.delete();
		if(sigFile.exists())
			throw new IOException("sigFile");

		File logFile = new File(getLegacyUploadLogFilename());
		logFile.delete();
		if(logFile.exists())
			throw new IOException("logFile");

		File accountsDir = getAccountsDirectory();
		DirectoryUtils.deleteEntireDirectoryTree(accountsDir);
		if(accountsDir.exists())
			throw new IOException("AccountsDirectory");

		// NOTE: We need to delete the top-level templates directory, 
		// not the current account templates directory
		File templatesDir = new File(getMartusDataRootDirectory(), ClientBulletinStore.TEMPLATE_DIRECTORY_NAME);
		DirectoryUtils.deleteEntireDirectoryTree(templatesDir);
		if(templatesDir.exists())
			throw new IOException("TemplatesDirectory");

		File rootDir = getMartusDataRootDirectory();
		File hashFile = getUserNameHashFile(rootDir);
		hashFile.delete();
		if(hashFile.exists())
			throw new IOException("UserHashFile");
		
		File retrieveFile = getRetrieveFile();
		retrieveFile.delete();
		if(retrieveFile.exists())
			throw new IOException("RetrieveFile");
		
		File orchidCacheFile = getOrchidCacheFile();
		orchidCacheFile.delete();
		if(orchidCacheFile.exists())
			throw new IOException("Orchid cache file " + orchidCacheFile.getPath());
		
		rootDir.delete();
		if(rootDir.exists())
			throw new IOException("MartusRootDirectory " + rootDir.getPath());
	}

	public void loadSampleData() throws Exception
	{
		BulletinFolder f = this.getFolderSaved();
		{
			Bulletin b = createBulletin();
			b.set("language",	"es");
			b.set("author",		"Comisi"+UnicodeConstants.ACCENT_O_LOWER+"n para el Esclarecimiento Hist"+UnicodeConstants.ACCENT_O_LOWER+"rico");
			b.set("title",		"Santa Rosa Xeputul Bombardeo");
			b.set("location",	"Santa Rosa Xeputul, Guatemala");
			b.set("eventdate",	"1999-01-19");
			b.set("entrydate",	"1999-02-17");
			b.set("keywords",	"ejecuci"+UnicodeConstants.ACCENT_O_LOWER+"n arbitraria, bombardeo");
			b.set("summary",	"El d"+UnicodeConstants.ACCENT_I_LOWER+"a 19 de diciembre de 1989, un avi"+UnicodeConstants.ACCENT_O_LOWER+"n Cessna A-37B, " +
								"de la Fuerza A"+UnicodeConstants.ACCENT_E_LOWER+"rea Guatemalteca sobrevol"+UnicodeConstants.ACCENT_O_LOWER+" la comunidad " +
								"Santa Rosa Xeputul de la CPR de la Sierra... a las 11 de " +
								"la ma"+UnicodeConstants.TILDE_N_LOWER+"ana la unidad a"+UnicodeConstants.ACCENT_E_LOWER+"rea lanz"+UnicodeConstants.ACCENT_O_LOWER+" una bomba de 550 libras " +
								"... murieron cuatro civiles, mujeres.  Otros dos " +
								"fallecieron ese mismo d"+UnicodeConstants.ACCENT_I_LOWER+"a en Chax"+UnicodeConstants.ACCENT_A_LOWER+", aldea cercana a Santa " +
								"Rosa, a causa de otra bomba arrojada por el avi"+UnicodeConstants.ACCENT_O_LOWER+"n.");
			b.set("publicinfo",	"");
			b.set("privateinfo",	"");
			b.setImmutable();
			store.saveBulletin(b);
			f.add(b);
		}
		{
			Bulletin b = createBulletin();
			b.setAllPrivate(true);
			b.set("author",		"Truth and Reconciliation Commission, South Africa");
			b.set("title",		"GO/0092 Johannesburg");
			b.set("location",	"Moroka");
			b.set("eventdate",	"1969-03-10");
			b.set("entrydate",	"1996-04-29");
			b.set("keywords",	"Torture, Murder, Police, Abduction");
			b.set("summary",	"Emmanuel Lenkoe was detained by police on 5 March 1969.  He " +
								"died on 10 March 1969 while in custody.  The police statement " +
								"was that he had hanged himself with his own belt.  The body " +
								"was not released to the family until one month after death.  An " +
								"autopsy performed at the family's request showed signs of " +
								"torture and burn marks.");
			b.set("publicinfo",	"The police said they were from Moroka Station, but actually " +
								"were from Pretoria where they detained Lenkoe. They had a law " +
								"that allowed them to detain without trial for 180 days, extended " +
								"from 90. The police claimed Emmanuel Lenkoe hung himself with " +
								"his belt in his cell");
			b.set("privateinfo","");
			b.setImmutable();
			store.saveBulletin(b);
			f.add(b);
		}
		{
			Bulletin b = createBulletin();
			b.set("author", "Radiz");
			b.set("eventdate", "2001-04-26");
			b.set("title", "Kidnapping in town square");
			b.setImmutable();
			store.saveBulletin(b);
			f.add(b);
		}
		{
			Bulletin b = createBulletin();
			b.set("author", "Molanna");
			b.set("eventdate", "1999-02-15");
			b.set("title", "Missing child near river");
			b.setImmutable();
			store.saveBulletin(b);
			f.add(b);
		}
		{
			Bulletin b = createBulletin();
			b.set("author", "Ullan");
			b.set("eventdate", "2000-12-19");
			b.set("title", "Child with broken arm");
			b.setImmutable();
			store.saveBulletin(b);
			f.add(b);
		}
		{
			Bulletin b = createBulletin();
			b.set("author", "Cambodian Land Rights Organization");
			b.set("title", "Confiscation of Land");
			b.set("location", "Siem Reap, Cambodia");
			b.set("eventdate", "2001-08-09");
			b.set("entrydate", "2001-10-05");
			b.set("keywords", "land confiscation, land rights, confiscation");
			b.set("summary", "A small business owner in the town of Siem Reap had " +
							"his shop taken from him when developers claimed they " +
							"purchased the land.");
			b.set("publicinfo", "The small business owner has been using the land " +
							"for over ten years.  It has been in his family for as " +
							"long as he can remember.  He is not sure who the " +
							"developers purchased the land from and does not know " +
							"who in the government to seek assistance from.  His " +
							"shop is in the tourist  district, and has been confiscated.");
			b.setImmutable();
			store.saveBulletin(b);
			f.add(b);
		}

		{
			Bulletin b = createBulletin();
			b.set("author", "Sri Lanka Peace Institute");
			b.set("title", "Firebombing of NGO Office");
			b.set("location", "Colombo");
			b.set("eventdate", "2001-02-03");
			b.set("entrydate", "2001-02-05");
			b.set("keywords", "firebomb, explosion, NGO");
			b.set("summary", "The Oxfam NGO office in downtown Colombo was attacked by " +
								"men who threw a firebomb in the front windows.");
			b.set("publicinfo", "The Oxfam NGO office in downtown Colombo was attacked " +
								"by men who threw a firebomb in the front windows.  The " +
								"explosion did not hurt anyone, staff members were in " +
								"the back of the office during the attack.");
			b.setImmutable();
			store.saveBulletin(b);
			f.add(b);
		}

		{
			Bulletin b = createBulletin();
			b.set("author", "Guatemala Legal Defense");
			b.set("title", "Stolen computers");
			b.set("location", "Guatemala City");
			b.set("eventdate", "2001-03-07");
			b.set("entrydate", "2001-04-14");
			b.set("keywords", "theft, computers, legal research, war crimes tribunal");
			b.set("summary", "The Guatemala Legal Defence offices were targeted for theft of computers.  All of the legal office PCs with information pertaining to the upcoming War Crimes Tribunal were stolen.");
			b.set("publicinfo", "The legal research to be used in the upcoming War Crimes Tribunal was lost when computers with the research and strategy were stolen in March.  Government officials claim that the computers loss was due to petty theft, but many in the NGO community believe that it is a targeted campaign to thwart justice in the upcoming tribunal.");
			b.setImmutable();
			store.saveBulletin(b);
			f.add(b);
		}

		{
			Bulletin b = createBulletin();
			b.set("author", "Environmental Watch Group");
			b.set("title", "toxic storage leaks outside Saint Petersburg");
			b.set("location", "St. Petersburg, Russia");
			b.set("eventdate", "2001-07-19");
			b.set("entrydate", "2001-07-20");
			b.set("keywords", "toxic storage, leaks, environment");
			b.set("summary", "The chemical plant facilities south of St. Petersburg are using faulty storage mechanisms for storing toxic chemicals that it no longer uses.");
			b.set("publicinfo", "Toxic storage tanks used by the chemical plant facilities south of St. Petersburg are not meeting international standards.  Workers fear that contamination of air and groundwater could ensue.");
			b.setImmutable();
			store.saveBulletin(b);
			f.add(b);
		}

		{
			Bulletin b = createBulletin();
			b.setAllPrivate(true);
			b.set("author", "The Martus Project");
			b.set("title", "woman with broken arm");
			b.set("location", "Colombo");
			b.set("eventdate", "2001-01-29");
			b.set("entrydate", "2002-04-17");
			b.set("keywords", "kidnapping, battery");
			b.set("summary", "Woman's arm was broken when she tried to stop her husband from being kidnapped.");
			b.set("publicinfo", "Two men arrived in the middle of the night and pounded on the door.  The men abducted the husband and the woman tried to stop them.  They broke her arm and she has not seen her husband since.");
			b.setImmutable();
			store.saveBulletin(b);
			f.add(b);
		}
	}

	public void loadSampleDataWithXForms() throws Exception
	{
		loadSampleData();
		BulletinFolder f = this.getFolderSaved();
		Bulletin b = createBulletin();
		b.set("language",	"en");
		b.set("author",		"SecureApp");
		b.set("title",		"Secure App Title 1");
		b.set("entrydate",	"2015-03-31");
		b.getFieldDataPacket().setXFormsModelAsString(getXFormsModelWithOnStringInputFieldXmlAsString());
		b.getFieldDataPacket().setXFormsInstanceAsString(getXFormsInstanceXmlAsString());
		b.setImmutable();
		store.saveBulletin(b);
		f.add(b);
	}
	
	public static String getXFormsModelWithOnStringInputFieldXmlAsString()
	{
		return 	"<xforms_model><h:html xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3.org/2002/xforms\" xmlns:jr=\"http://openrosa.org/javarosa\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" >" +
				"<h:head><h:title>XForms Sample</h:title><model><instance><nm id=\"SampleForUnitTesting\" ><name/>" +
				"</nm></instance><bind nodeset=\"/nm/name\" type=\"string\" /></model></h:head>" +
				"<h:body><input ref=\"name\" ><label>some randon name</label><hint>(required)</hint></input></h:body></h:html></xforms_model>";
	}
	
	public static String getXFormsInstanceXmlAsString()
	{
		return "<xforms_instance><nm id=\"SampleForUnitTesting\"><name>xforms Name Field</name></nm></xforms_instance>";
	}
	
	String testDataDirectory;
}
