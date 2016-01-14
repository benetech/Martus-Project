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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Vector;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.bulletinstore.ClientBulletinStore.AddOlderVersionToFolderFailedException;
import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.AccountAlreadyExistsException;
import org.martus.client.core.MartusApp.CannotCreateAccountFileException;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.network.RetrieveCommand;
import org.martus.client.search.SearchParser;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.swingui.EnglishStrings;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiSession;
import org.martus.clientside.CurrentUiState;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.clientside.PasswordHelper;
import org.martus.clientside.UiLocalization;
import org.martus.clientside.test.ServerSideNetworkHandlerNotAvailable;
import org.martus.common.ContactKey;
import org.martus.common.ContactKeys;
import org.martus.common.DammCheckDigitAlgorithm;
import org.martus.common.FieldCollection;
import org.martus.common.FieldDeskKey;
import org.martus.common.FieldDeskKeys;
import org.martus.common.FieldSpecCollection;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.LegacyCustomFields;
import org.martus.common.MartusUtilities;
import org.martus.common.MiniLocalization;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.FileDatabase;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.swing.Utilities;
import org.martus.util.DirectoryUtils;
import org.martus.util.MultiCalendar;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.language.LanguageOptions;

public class TestMartusApp_NoServer extends TestCaseEnhanced
{
	public TestMartusApp_NoServer(String name)
	{
		super(name);
		VERBOSE = false;
	}

	public void setUp() throws Exception
	{
		super.setUp();
		TRACE_BEGIN("setUp");

		mockSecurityForApp = MockMartusSecurity.createClient();

		testAppLocalization = new MartusLocalization(null, UiSession.getAllEnglishStrings());
		CurrentUiState currentUi = new CurrentUiState();
		currentUi.setCurrentLanguage(MiniLocalization.ENGLISH);
		currentUi.setCurrentDateFormat(MDY_SLASH);
		testAppLocalization.setLanguageSettingsProvider(currentUi);
		appWithAccount = MockMartusApp.create(mockSecurityForApp, getName());
		appWithAccount.setSSLNetworkInterfaceHandlerForTesting(new ServerSideNetworkHandlerNotAvailable());

		File keyPairFile = appWithAccount.getCurrentKeyPairFile();
		keyPairFile.delete();
		appWithAccount.getConfigInfoFile().delete();
		appWithAccount.getConfigInfoSignatureFile().delete();
		LanguageOptions.setDirectionLeftToRight();

		TRACE_END();
	}

	public void tearDown() throws Exception
	{
		appWithAccount.deleteAllFiles();
		super.tearDown();
	}

	public void testBasics()
	{
		TRACE_BEGIN("testBasics");

		ClientBulletinStore store = appWithAccount.getStore();
		assertNotNull("BulletinStore", store);
		TRACE_END();
	}
	
	public void testRetrieveNextBackgroundBulletin() throws Exception
	{
		class FakeRetrieveApp extends MockMartusApp
		{
			public FakeRetrieveApp(File tempDir) throws Exception
			{
				super(MockMartusSecurity.createClient(), tempDir, new MartusLocalization(tempDir, EnglishStrings.strings));
				initializeMockApp(this, tempDir);
			}

			public void retrieveOneBulletinToFolder(UniversalId uid, BulletinFolder retrievedFolder, ProgressMeterInterface progressMeter) throws AddOlderVersionToFolderFailedException, Exception
			{
				// fake it...do nothing
			}
		}

		File tempDir = createTempDirectory();
		try
		{
			Vector uids = new Vector();
			uids.add(UniversalIdForTesting.createDummyUniversalId());
			uids.add(UniversalIdForTesting.createDummyUniversalId());
			RetrieveCommand rc = new RetrieveCommand("folder", uids);
			
			FakeRetrieveApp app = new FakeRetrieveApp(tempDir);
			File retrieveFile = new File(tempDir, "Retrieve.dat");
			assertFalse("retrieve file already exists?", retrieveFile.exists());
	
			app.startBackgroundRetrieve(rc);
			assertTrue("retrieve file not created?", retrieveFile.exists());
			retrieveFile.delete();
			
			app.retrieveNextBackgroundBulletin();
			assertTrue("retrieve file not updated?", retrieveFile.exists());
			retrieveFile.delete();
	
			app.cancelBackgroundRetrieve();
			assertTrue("retrieve file not updated?", retrieveFile.exists());
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDir);
		}
		
	}
	
	public void testLoadRetrieveCommand() throws Exception
	{
		try
		{
			appWithAccount.loadRetrieveCommand();
		}
		catch(Exception e)
		{
			fail("Should not have thrown if retrieve file doesn't exist");
		}
		
		RetrieveCommand rc = new RetrieveCommand("Blah", new Vector());
		appWithAccount.startBackgroundRetrieve(rc);
		appWithAccount.currentRetrieveCommand = new RetrieveCommand();
		appWithAccount.loadRetrieveCommand();
		assertEquals("didn't load?", rc.getFolderName(), appWithAccount.getCurrentRetrieveCommand().getFolderName());
		
		FileInputStream in = new FileInputStream(appWithAccount.getRetrieveFile());
		byte[] contents = new byte[(int)appWithAccount.getRetrieveFile().length()];
		in.read(contents);
		in.close();
		
		contents[50] = (byte)(contents[50] ^ 0xFF);
		
		FileOutputStream out = new FileOutputStream(appWithAccount.getRetrieveFile());
		out.write(contents);
		out.close();
		
		try
		{
			appWithAccount.loadRetrieveCommand();
			fail("Should have thrown for corrupted file");
		}
		catch(Exception ignoreExpected)
		{
		}
		
		class RetrieveCommandWithFakeVersion extends RetrieveCommand
		{

			public int getDataVersion()
			{
				return fakeVersion;
			}
			
			public int fakeVersion;
		}
		
		RetrieveCommandWithFakeVersion tooOld = new RetrieveCommandWithFakeVersion();
		tooOld.fakeVersion = RetrieveCommand.DATA_VERSION - 1;
		appWithAccount.startBackgroundRetrieve(tooOld);
		try
		{
			appWithAccount.loadRetrieveCommand();
			fail("Should have thrown for older data version");
		}
		catch(RetrieveCommand.OlderDataVersionException ignoreExpected)
		{
		}
		
		RetrieveCommandWithFakeVersion tooNew = new RetrieveCommandWithFakeVersion();
		tooNew.fakeVersion = RetrieveCommand.DATA_VERSION + 1;
		appWithAccount.startBackgroundRetrieve(tooNew);
		try
		{
			appWithAccount.loadRetrieveCommand();
			fail("Should have thrown for older data version");
		}
		catch(RetrieveCommand.NewerDataVersionException ignoreExpected)
		{
		}
	}
	
	public void testCreateRetrieveCommandBundle() throws Exception
	{
		Vector uids = new Vector();
		uids.add(UniversalIdForTesting.createDummyUniversalId());
		uids.add(UniversalIdForTesting.createDummyUniversalId());
		RetrieveCommand rc = new RetrieveCommand("folder", uids);
		byte[] bundle = appWithAccount.createRetrieveCommandBundle(rc);
		
		RetrieveCommand got = appWithAccount.parseRetrieveCommandBundle(bundle);
		assertEquals("wrong folder?", rc.getFolderName(), got.getFolderName());
		assertEquals("wrong count?", rc.getRemainingToRetrieveCount(), got.getRemainingToRetrieveCount());
		assertEquals("wrong next uid?", rc.getNextToRetrieve(), got.getNextToRetrieve());
		
		RetrieveCommand rcEmpty = new RetrieveCommand();
		byte[] bundleEmpty = appWithAccount.createRetrieveCommandBundle(rcEmpty);
		
		RetrieveCommand gotEmpty = appWithAccount.parseRetrieveCommandBundle(bundleEmpty);
		assertEquals("wrong folder Empty?", "", gotEmpty.getFolderName());
		assertEquals("wrong count Empty?", 0, gotEmpty.getRemainingToRetrieveCount());
	}
	
	
	public void testGetDefaultLanguageForNewBulletin()
	{
		MiniLocalization localization = appWithAccount.getLocalization();
		CurrentUiState uiStateToUse = new CurrentUiState();
		uiStateToUse.setCurrentDateFormat(DMY_SLASH);
		localization.setLanguageSettingsProvider(uiStateToUse);
		String originalLanguage = localization.getCurrentLanguageCode();
		assertNull("language not null by default?", originalLanguage);
		
		final String NON_STANDARD_LANGUAGE_CODE = "soylent green";
		localization.setCurrentLanguageCode(NON_STANDARD_LANGUAGE_CODE);
		assertEquals("Didn't reject weird language?", MiniLocalization.LANGUAGE_OTHER, appWithAccount.getDefaultLanguageForNewBulletin());

		localization.setCurrentLanguageCode(MiniLocalization.SPANISH);
		assertEquals("Didn't use SPANISH?", MiniLocalization.SPANISH, appWithAccount.getDefaultLanguageForNewBulletin());
	}
	
	public void testGetHelp() throws Exception
	{
		TRACE_BEGIN("testGetHelp");
		File translationDirectory = appWithAccount.martusDataRootDirectory;
		String languageCode = "xx";
		InputStream helpMain = appWithAccount.getHelpMain(languageCode);
		assertNull("Language pack doesn't exists help should return null", helpMain);
		InputStream helpTOC = appWithAccount.getHelpTOC(languageCode);
		assertNull("Language pack doesn't exists help toc should return null", helpTOC);

		File mlpkTranslation = new File(translationDirectory, UiLocalization.getMlpkFilename(languageCode));
		copyResourceFileToLocalFile(mlpkTranslation, "Martus-xx-notSigned.mlp");
		mlpkTranslation.deleteOnExit();
		helpMain = appWithAccount.getHelpMain(languageCode);
		helpTOC = appWithAccount.getHelpTOC(languageCode);
		assertNull("Language pack exists but isn't signed help should return null", helpMain);
		assertNull("Language pack exists but isn't signed help toc should return null", helpTOC);
		mlpkTranslation.delete();

		copyResourceFileToLocalFile(mlpkTranslation, "Martus-xx.mlp");
		mlpkTranslation.deleteOnExit();
		helpMain = appWithAccount.getHelpMain(languageCode);
		UnicodeReader reader = new UnicodeReader(helpMain);
		reader.read();//unused char.
		String line1InFile = reader.readLine(); 
		reader.close();
		String helpTextInFile = "Temp Help File for testing";
		
		helpTOC = appWithAccount.getHelpTOC(languageCode);
		reader = new UnicodeReader(helpTOC);
		reader.read();//unused char.
		String line1InTOCFile = reader.readLine();
		reader.close();
		String helpTextInTOCFile = "chapter 1";

		mlpkTranslation.delete();

		assertNotNull("Language pack exists and is signed help should return not null", helpMain);
		assertNotNull("Language pack exists and is signed help toc should return not null", helpTOC);
		assertEquals("Contents of help didn't match?", helpTextInFile, line1InFile);
		assertEquals("Contents of help TOC didn't match?", helpTextInTOCFile, line1InTOCFile);
		TRACE_END();
	}	
	
	public void testUpdateDocsFromMLPFiles() throws Exception
	{
		TRACE_BEGIN("testUpdateDocsFromMLPFiles");
		File translationDirectory = appWithAccount.martusDataRootDirectory;
		File mlpkTranslationxx = new File(translationDirectory, UiLocalization.getMlpkFilename("xx"));
		copyResourceFileToLocalFile(mlpkTranslationxx, "Martus-xx-notSigned.mlp");
		mlpkTranslationxx.deleteOnExit();
		appWithAccount.UpdateDocsIfNecessaryFromMLPFiles();
		File documentsDirectory = appWithAccount.getDocumentsDirectory();
		assertFalse("Shouldn't have a docs directory yet, this mlp file was not signed.", documentsDirectory.exists());
		mlpkTranslationxx.delete();

		copyResourceFileToLocalFile(mlpkTranslationxx, "Martus-xx.mlp");
		mlpkTranslationxx.deleteOnExit();
		File mlpkTranslationyy = new File(translationDirectory, UiLocalization.getMlpkFilename("yy"));
		copyResourceFileToLocalFile(mlpkTranslationyy, "Martus-yy.mlp");
		mlpkTranslationyy.deleteOnExit();

		File readmeFile_xx = new File(translationDirectory, "README_xx.txt");
		File readmeFile_yy = new File(translationDirectory, "README_yy.txt");
		assertFalse("Should not have an xx readme file yet", readmeFile_xx.exists());
		assertFalse("Should not have a yy readme file yet", readmeFile_yy.exists());

		appWithAccount.UpdateDocsIfNecessaryFromMLPFiles();
		assertTrue("Should now have a docs directory,with 4 files within.", documentsDirectory.exists());
		File[] pdfFiles = GetPdfFiles(documentsDirectory);
		assertEquals("Should have 4 pdf files", 4, pdfFiles.length);
		appWithAccount.UpdateDocsIfNecessaryFromMLPFiles();
		pdfFiles = GetPdfFiles(documentsDirectory);
		assertEquals("Should still have just 4 pdf files", 4, pdfFiles.length);
		assertTrue("Should now have an xx readme file", readmeFile_xx.exists());
		assertTrue("Should now have a yy readme file", readmeFile_yy.exists());

		readmeFile_xx.delete();
		readmeFile_yy.delete();
		mlpkTranslationxx.delete();
		mlpkTranslationyy.delete();
		DirectoryUtils.deleteEntireDirectoryTree(documentsDirectory);

		TRACE_END();
	}	

	public void testUpdateNewerDocsFromMLPFiles() throws Exception
	{
		TRACE_BEGIN("testUpdateNewerDocsFromMLPFiles");
		File translationDirectory = appWithAccount.martusDataRootDirectory;
		File mlpkTranslationyy = new File(translationDirectory, UiLocalization.getMlpkFilename("yy"));
		File readmeFile_yy = new File(translationDirectory, "README_yy.txt");
		copyResourceFileToLocalFile(mlpkTranslationyy, "Martus-yy.mlp");
		mlpkTranslationyy.deleteOnExit();
		appWithAccount.UpdateDocsIfNecessaryFromMLPFiles();
		File documentsDirectory = appWithAccount.getDocumentsDirectory();
		mlpkTranslationyy.delete();

		File quickStartGuide_yy = new File(documentsDirectory, "quickstartguide_yy.pdf");
		String firstLineInGuide_yy = readLineFromFile(quickStartGuide_yy);
		assertEquals("not the same pdf file?",textInsideYYQuickStartGuideMLPFile, firstLineInGuide_yy);
		String firstLineInReadMe_yy = readLineFromFile(readmeFile_yy);
		assertEquals("not the same readme file?",textInsideYYReadmeMLPFile, firstLineInReadMe_yy);

		copyResourceFileToLocalFile(mlpkTranslationyy, "Martus-updated-yy.mlp");
		mlpkTranslationyy.deleteOnExit();
		appWithAccount.UpdateDocsIfNecessaryFromMLPFiles();
		mlpkTranslationyy.delete();
		String firstLineInUpdatedQuickStartGuide_yy = readLineFromFile(quickStartGuide_yy);
		String firstLineInUpdatedReadme_yy = readLineFromFile(readmeFile_yy);
		
		DirectoryUtils.deleteEntireDirectoryTree(documentsDirectory);
		readmeFile_yy.delete();

		assertEquals("not the updated pdf file?",textInsideUpdatedYYQuickStartGuideMLPFile, firstLineInUpdatedQuickStartGuide_yy);
		assertEquals("not the updated readme file?",textInsideUpdatedYYReadmeMLPFile, firstLineInUpdatedReadme_yy);
		TRACE_END();
	}	
	
	public void testDowngradingDocsFromMLPFiles() throws Exception
	{
		TRACE_BEGIN("testDowngradingDocsFromMLPFiles");
		File translationDirectory = appWithAccount.martusDataRootDirectory;
		File mlpkTranslationyy = new File(translationDirectory, UiLocalization.getMlpkFilename("yy"));
		File readmeFile_yy = new File(translationDirectory, "README_yy.txt");

		copyResourceFileToLocalFile(mlpkTranslationyy, "Martus-updated-yy.mlp");
		mlpkTranslationyy.deleteOnExit();
		appWithAccount.UpdateDocsIfNecessaryFromMLPFiles();
		mlpkTranslationyy.delete();
		File documentsDirectory = appWithAccount.getDocumentsDirectory();
		File quickStartGuide_yy = new File(documentsDirectory, "quickstartguide_yy.pdf");

		String firstLineInUpdatedQuickStartGuide_yy = readLineFromFile(quickStartGuide_yy);
		assertEquals("not the same pdf file as the updated file?",textInsideUpdatedYYQuickStartGuideMLPFile, firstLineInUpdatedQuickStartGuide_yy);
		String firstLineInUpdatedReadme_yy = readLineFromFile(readmeFile_yy);
		assertEquals("not the updated readme file?",textInsideUpdatedYYReadmeMLPFile, firstLineInUpdatedReadme_yy);

		copyResourceFileToLocalFile(mlpkTranslationyy, "Martus-yy.mlp");
		mlpkTranslationyy.deleteOnExit();
		appWithAccount.UpdateDocsIfNecessaryFromMLPFiles();
		mlpkTranslationyy.delete();

		String firstLineInGuide_yy = readLineFromFile(quickStartGuide_yy);
		String firstLineInReadMe_yy = readLineFromFile(readmeFile_yy);

		readmeFile_yy.delete();
		DirectoryUtils.deleteEntireDirectoryTree(documentsDirectory);
		assertEquals("Should still be the updated pdf file, it shouldn't downgrade?",textInsideUpdatedYYQuickStartGuideMLPFile, firstLineInGuide_yy);
		assertEquals("Should still be the updated readme file, it shouldn't downgrade?",textInsideUpdatedYYReadmeMLPFile, firstLineInReadMe_yy);
		
		
		TRACE_END();
	}	

	private String readLineFromFile(File file) throws IOException
	{
		UnicodeReader reader = new UnicodeReader(file);
		String text = reader.readLine();
		reader.close();
		return text;
	}

	
	private File[] GetPdfFiles(File documentsDirectory)
	{
		File[] mpiFiles = documentsDirectory.listFiles(new FileFilter()
		{
			public boolean accept(File file)
			{
				return (file.isFile() && file.getName().endsWith(".pdf"));	
			}
		});
		return mpiFiles;
	}
	

	public void testSaveBulletin() throws Exception
	{
		ClientBulletinStore store = appWithAccount.getStore();
		BulletinFolder outbox = store.getFolderDraftOutbox();
		BulletinFolder discarded = store.getFolderDiscarded();
		
		store.getFoldersFile().delete();
		assertFalse("couldn't delete folders?", store.getFoldersFile().exists());
		
		Bulletin b = appWithAccount.createBulletin();
		appWithAccount.saveBulletin(b, outbox);
		DatabaseKey key = DatabaseKey.createMutableKey(b.getUniversalId());
		assertTrue("didn't save?", store.getDatabase().doesRecordExist(key));
		assertTrue("didn't put in outbox?", outbox.contains(b));
		assertTrue("didn't put in saved?", appWithAccount.getFolderSaved().contains(b));
		assertTrue("didn't save folders?", store.getFoldersFile().exists());
		assertFalse("marked as sent?", store.isProbablyOnServer(b.getUniversalId()));
		assertTrue("didn't mark as unsent?", store.isProbablyNotOnServer(b.getUniversalId()));
		
		store.setIsOnServer(b);
		store.moveBulletin(b, outbox, discarded);
		appWithAccount.saveBulletin(b, outbox);
		assertFalse("didn't remove from discarded?", discarded.contains(b));
		assertFalse("not unmarked as sent?", store.isProbablyOnServer(b.getUniversalId()));
		assertTrue("didn't remark as unsent?", store.isProbablyNotOnServer(b.getUniversalId()));
	}
	
	public void testLoadOldCustomFieldConfigInfo() throws Exception
	{
		ConfigInfo infoToConvert = new ConfigInfo();
		String sampleLegacyFields = "tag1;tag2";
		infoToConvert.setCustomFieldLegacySpecs(sampleLegacyFields);
		FieldCollection fields = new FieldCollection(MartusApp.getCustomFieldSpecsTopSection(infoToConvert));

		FieldCollection expected = new FieldCollection(LegacyCustomFields.parseFieldSpecsFromString(sampleLegacyFields));
		assertEquals(expected.toString(), fields.toString());

		fields = new FieldCollection(MartusApp.getCustomFieldSpecsBottomSection(infoToConvert));
		expected = new FieldCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray());
		assertEquals(expected.toString(), fields.toString());
	}
	
	public void testLoadCustomFieldInfoWithBottomFieldSpec() throws Exception
	{
		ConfigInfo convertedInfo = new ConfigInfo();
		String newFields = "new,label;another,show";
		FieldSpecCollection newSpecs = LegacyCustomFields.parseFieldSpecsFromString(newFields);
		FieldCollection convertedFields = new FieldCollection(newSpecs.asArray());
		convertedInfo.deprecatedSetCustomFieldBottomSectionXml(convertedFields.toString());
		FieldCollection fields = new FieldCollection(MartusApp.getCustomFieldSpecsBottomSection(convertedInfo));

		FieldCollection expected = new FieldCollection(LegacyCustomFields.parseFieldSpecsFromString(newFields));
		assertEquals(expected.toString(), fields.toString());
	}

	public void testLoadConvertedCustomFieldInfo() throws Exception
	{
		ConfigInfo convertedInfo = new ConfigInfo();
		String newFields = "new,label;another,show";
		FieldSpecCollection newSpecs = LegacyCustomFields.parseFieldSpecsFromString(newFields);
		FieldCollection convertedFields = new FieldCollection(newSpecs.asArray());
		convertedInfo.deprecatedSetCustomFieldTopSectionXml(convertedFields.toString());
		FieldCollection fields = new FieldCollection(MartusApp.getCustomFieldSpecsTopSection(convertedInfo));

		FieldCollection expected = new FieldCollection(LegacyCustomFields.parseFieldSpecsFromString(newFields));
		assertEquals(expected.toString(), fields.toString());
	}
	
	public void testShouldAskUserToBackupKeypair() throws Exception
	{
		appWithAccount.startClockToAskForKeypairBackup();
		assertFalse("should not ask when we just started the clock", appWithAccount.shouldWeAskForKeypairBackup());
		assertTrue("wasn't true for a date decades into the future?",appWithAccount.shouldWeAskForKeypairBackup("2099-01-01"));

		String dateLastAskedForKeypairBackup = appWithAccount.getConfigInfo().getDateLastAskedUserToBackupKeypair();

		MultiCalendar oneDayPrior = MultiCalendar.createFromIsoDateString(dateLastAskedForKeypairBackup);
		oneDayPrior.addDays(MartusApp.DAYS_UNTIL_WE_ASK_TO_BACKUP_KEYPAIR - 1);
		assertFalse("6 days into the future we should still not ask", appWithAccount.shouldWeAskForKeypairBackup(oneDayPrior.toIsoDateString()));

		MultiCalendar sevenDaysLater = MultiCalendar.createFromIsoDateString(dateLastAskedForKeypairBackup);
		sevenDaysLater.addDays(MartusApp.DAYS_UNTIL_WE_ASK_TO_BACKUP_KEYPAIR);
		assertTrue("exactly 7 days into the future we need to ask for a backup", appWithAccount.shouldWeAskForKeypairBackup(sevenDaysLater.toIsoDateString()));
		
		appWithAccount.clearClockToAskForKeypairBackup();
		assertFalse("After we clear the clock we should not be asked for a backup", appWithAccount.shouldWeAskForKeypairBackup());
		assertFalse("After we clear the clock we should not be asked for a backup", appWithAccount.shouldWeAskForKeypairBackup(sevenDaysLater.toIsoDateString()));
	}
	
	public void testSetDefaultUiState() throws Exception
	{
		CurrentUiState currentUiState = new CurrentUiState();
		File tmpFile = createTempFile();
		MartusApp.setInitialUiDefaultsFromFileIfPresent(currentUiState, tmpFile);
		assertNull("File doesn't exist localization should not be set.  Using DefaultUi.txt depends on the language not being set in this case.", currentUiState.getCurrentLanguage());
		FileOutputStream out = new FileOutputStream(tmpFile);
		out.write("invalidLanguageCode".getBytes());
		out.close();
		MartusApp.setInitialUiDefaultsFromFileIfPresent(currentUiState, tmpFile);
		assertNull("Invalid language code, localization should not be set", currentUiState.getCurrentLanguage());
		tmpFile.delete();
		out = new FileOutputStream(tmpFile);
		out.write("en".getBytes());
		out.close();
		MartusApp.setInitialUiDefaultsFromFileIfPresent(currentUiState, tmpFile);
		assertEquals("English should be set", MtfAwareLocalization.ENGLISH, currentUiState.getCurrentLanguage());
		assertEquals("English code should set DMY correctly", MDY_SLASH, currentUiState.getCurrentDateFormat());
		tmpFile.delete();
		out = new FileOutputStream(tmpFile);
		out.write("es".getBytes());
		out.close();
		MartusApp.setInitialUiDefaultsFromFileIfPresent(currentUiState, tmpFile);
		assertEquals("Spanish should be set", MtfAwareLocalization.SPANISH, currentUiState.getCurrentLanguage());
		assertEquals("Spanish code should set MDY correctly", DMY_SLASH, currentUiState.getCurrentDateFormat());
		tmpFile.delete();
		out = new FileOutputStream(tmpFile);
		out.write("ru".getBytes());
		out.close();
		MartusApp.setInitialUiDefaultsFromFileIfPresent(currentUiState, tmpFile);
		assertEquals("Russian should be set", MtfAwareLocalization.RUSSIAN, currentUiState.getCurrentLanguage());
		assertEquals("Russian code should set MDY Dot correctly", DMY_DOT, currentUiState.getCurrentDateFormat());
		tmpFile.delete();
	}

	public void testDiscardBulletinsFromFolder() throws Exception
	{
		Bulletin b1 = appWithAccount.createBulletin();
		Bulletin b2 = appWithAccount.createBulletin();
		Bulletin b3 = appWithAccount.createBulletin();
		b3.setImmutable();
		appWithAccount.getStore().saveBulletin(b1);
		appWithAccount.getStore().saveBulletin(b2);
		appWithAccount.getStore().saveBulletin(b3);

		BulletinFolder f1 = appWithAccount.createUniqueFolder("testFolder");
		f1.add(b1);
		f1.add(b2);
		f1.add(b3);
		BulletinFolder draftOutbox = appWithAccount.getFolderDraftOutbox();
		draftOutbox.add(b1);
		BulletinFolder sealedOutbox = appWithAccount.getFolderSealedOutbox();
		sealedOutbox.add(b3);
		appWithAccount.getStore().saveBulletin(b1);
		appWithAccount.getStore().saveBulletin(b2);
		appWithAccount.getStore().saveBulletin(b3);
		
		appWithAccount.discardBulletinsFromFolder(f1, new UniversalId[] {b1.getUniversalId(), b3.getUniversalId()});
		assertEquals(3, appWithAccount.getStore().getBulletinCount());
		assertEquals(1, f1.getBulletinCount());
		assertEquals("removed from draft outbox?", 1, draftOutbox.getBulletinCount());
		assertEquals("removed from sealed outbox?", 1, sealedOutbox.getBulletinCount());
		
		Database db = appWithAccount.getWriteableDatabase();
		DatabaseKey key = DatabaseKey.createLegacyKey(b1.getBulletinHeaderPacket().getUniversalId());
		db.discardRecord(key);

		BulletinFolder savedFolder = appWithAccount.getFolderSaved();
		try
		{
			appWithAccount.discardBulletinsFromFolder(savedFolder, new UniversalId[] {b1.getUniversalId(), b3.getUniversalId()});
			fail("discard damaged record should have thrown");
		}
		catch(IOException ignoreExpectedException)
		{
		}
		BulletinFolder trash = appWithAccount.getFolderDiscarded();
		try
		{
			appWithAccount.getStore().saveBulletin(b1);
			appWithAccount.getStore().saveBulletin(b3);
			appWithAccount.discardBulletinsFromFolder(trash, new UniversalId[] {b1.getUniversalId(), b3.getUniversalId()});
		}
		catch(IOException e)
		{
			fail("Should not have thrown, discarding from trash is fine.");
		}
	}
	
	public void testDeleteAllBulletins() throws Exception
	{
		Bulletin b1 = appWithAccount.createBulletin();
		Bulletin b2 = appWithAccount.createBulletin();
		appWithAccount.getStore().saveBulletin(b1);
		appWithAccount.getStore().saveBulletin(b2);

		BulletinFolder f1 = appWithAccount.createUniqueFolder("testFolder");
		BulletinFolder f2 = appWithAccount.getFolderDraftOutbox();
		f1.add(b1);
		f2.add(b2);
		assertEquals(2, appWithAccount.getStore().getBulletinCount());		
		appWithAccount.deleteAllBulletinsAndUserFolders();
		assertEquals(0, appWithAccount.getStore().getBulletinCount());
		assertNotNull("System Folder deleted?", appWithAccount.getFolderDraftOutbox());
		assertNull("User Folder Not deleted?", appWithAccount.getStore().findFolder(f1.getName()));
	}
	

	public void testDbInitializerExceptionForMissingAccountMap() throws Exception
	{
		File fakeDataDirectory = null;
		File packetDirectory = null;
		File subdirectory = null;

		try
		{
			fakeDataDirectory = createTempFileFromName("$$$MartusTestApp");
			fakeDataDirectory.delete();
			fakeDataDirectory.mkdir();

			packetDirectory = new File(fakeDataDirectory, "packets");
			subdirectory = new File(packetDirectory, "anyfolder");
			subdirectory.mkdirs();

			try
			{
				MartusLocalization localization = new MartusLocalization(fakeDataDirectory, noEnglishStrings);
				MartusApp app = new MartusApp(mockSecurityForApp, fakeDataDirectory, localization);
				app.setCurrentAccount("some user", app.getMartusDataRootDirectory());
				app.doAfterSigninInitalization();
				fail("Should have thrown because map is missing");
			}
			catch(FileDatabase.MissingAccountMapException expectedException)
			{
			}
		}
		finally
		{
			if(subdirectory != null)
				subdirectory.delete();
			if(packetDirectory != null)
				packetDirectory.delete();
			if(fakeDataDirectory != null)
			{
				new File(fakeDataDirectory, "MartusConfig.dat").delete();
				new File(fakeDataDirectory, "MartusConfig.sig").delete();
				new File(fakeDataDirectory, "AccountToken.txt").delete();
				fakeDataDirectory.delete();
			}
		}
	}

	public void testDbInitializerExceptionForMissingAccountMapSignature() throws Exception
	{
		File fakeDataDirectory = null;
		File packetDirectory = null;
		File subdirectory = null;
		File acctMap = null;

		try
		{
			fakeDataDirectory = createTempFileFromName("$$$MartusTestApp");
			fakeDataDirectory.delete();
			fakeDataDirectory.mkdir();

			packetDirectory = new File(fakeDataDirectory, "packets");
			subdirectory = new File(packetDirectory, "anyfolder");
			subdirectory.mkdirs();

			acctMap = new File(packetDirectory,"acctmap.txt");
			acctMap.deleteOnExit();

			FileOutputStream out = new FileOutputStream(acctMap.getPath(), true);
			UnicodeWriter writer = new UnicodeWriter(out);
			writer.writeln("noacct=123456789");
			writer.flush();
			out.flush();
			writer.close();

			try
			{
				MartusLocalization localization = new MartusLocalization(fakeDataDirectory, noEnglishStrings);
				MartusApp app = new MartusApp(mockSecurityForApp, fakeDataDirectory, localization);
				app.setCurrentAccount("some user", app.getMartusDataRootDirectory());
				app.doAfterSigninInitalization();
				fail("Should have thrown because of missing map signature");
			}
			catch(FileDatabase.MissingAccountMapSignatureException expectedException)
			{
			}
		}
		finally
		{
			if(acctMap != null )
				acctMap.delete();
			if(subdirectory != null)
				subdirectory.delete();
			if(packetDirectory != null)
				packetDirectory.delete();
			if(fakeDataDirectory != null)
			{
				new File(fakeDataDirectory, "MartusConfig.dat").delete();
				new File(fakeDataDirectory, "MartusConfig.sig").delete();
				new File(fakeDataDirectory, "AccountToken.txt").delete();
				fakeDataDirectory.delete();
			}
		}
	}

	public void testDbInitializerExceptionForInvalidAccountMapSignature() throws Exception
	{
		File fakeDataDirectory = null;
		File packetDirectory = null;
		File subdirectory = null;
		File acctMap = null;
		File signatureFile = null;

		try
		{
			fakeDataDirectory = createTempFileFromName("$$$MartusTestApp");
			fakeDataDirectory.delete();
			fakeDataDirectory.mkdir();

			packetDirectory = new File(fakeDataDirectory, "packets");
			subdirectory = new File(packetDirectory, "anyfolder");
			subdirectory.mkdirs();

			acctMap = new File(packetDirectory,"acctmap.txt");
			acctMap.deleteOnExit();

			UnicodeWriter writer = new UnicodeWriter(acctMap);
			writer.writeln("noacct=123456789");
			writer.flush();
			writer.close();

			signatureFile = new File(packetDirectory,"acctmap.txt.sig");
			signatureFile.deleteOnExit();

			writer = new UnicodeWriter(signatureFile);
			writer.writeln("a fake signature");
			writer.flush();
			writer.close();

			try
			{
				MartusLocalization localization = new MartusLocalization(fakeDataDirectory, noEnglishStrings);
				MartusApp app = new MartusApp(mockSecurityForApp, fakeDataDirectory, localization);
				app.setCurrentAccount("some user", app.getMartusDataRootDirectory());
				app.doAfterSigninInitalization();
				fail("Should have thrown because of invalid map signature");
			}
			catch(MartusUtilities.FileVerificationException expectedException)
			{
			}
		}
		finally
		{
			if(acctMap != null )
				acctMap.delete();
			if(signatureFile != null)
				signatureFile.delete();
			if(subdirectory != null)
				subdirectory.delete();
			if(packetDirectory != null)
				packetDirectory.delete();
			if(fakeDataDirectory != null)
			{
				new File(fakeDataDirectory, "MartusConfig.dat").delete();
				new File(fakeDataDirectory, "MartusConfig.sig").delete();
				new File(fakeDataDirectory, "AccountToken.txt").delete();
				fakeDataDirectory.delete();
			}
		}
	}

	public void testGetClientId() throws Exception
	{
		TRACE_BEGIN("testGetClientId");
		String securityAccount = mockSecurityForApp.getPublicKeyString();
		String appAccount = appWithAccount.getAccountId();
		assertEquals("mock account wrong?", securityAccount, appAccount);
		Bulletin b = appWithAccount.createBulletin();
		assertEquals("client id wrong?", b.getAccount(), appWithAccount.getAccountId());
		TRACE_END();
	}
	
	public void testConfigInfo() throws Exception
	{
		TRACE_BEGIN("testConfigInfo");

		File file = appWithAccount.getConfigInfoFile();
		file.delete();
		assertEquals("delete didn't work", false, file.exists());
		appWithAccount.loadConfigInfo();

		ConfigInfo originalInfo = appWithAccount.getConfigInfo();
		assertEquals("should be empty", "", originalInfo.getAuthor());

		originalInfo.setAuthor("blah");
		assertEquals("should have been set", "blah", appWithAccount.getConfigInfo().getAuthor());
	
		appWithAccount.saveConfigInfo();
		assertEquals("should still be there", "blah", appWithAccount.getConfigInfo().getAuthor());
		assertEquals("save didn't work!", true, file.exists());

		originalInfo.setAuthor("something else");
		appWithAccount.loadConfigInfo();
		assertNotNull("ContactInfo null", appWithAccount.getConfigInfo());
		assertEquals("should have reloaded", "blah", appWithAccount.getConfigInfo().getAuthor());

		File sigFile = appWithAccount.getConfigInfoSignatureFile();
		sigFile.delete();
		appWithAccount.saveConfigInfo();
		assertTrue("Missing Signature file", sigFile.exists());
		appWithAccount.loadConfigInfo();
		assertEquals("blah", appWithAccount.getConfigInfo().getAuthor());
		sigFile.delete();
		try
		{
			appWithAccount.loadConfigInfo();
			fail("Should not have verified");
		}
		catch (MartusApp.LoadConfigInfoException e)
		{
			//Expected
		}
		assertEquals("", appWithAccount.getConfigInfo().getAuthor());

		TRACE_END();

	}
	
	public void testUpdateFormTemplate() throws Exception
	{
		TRACE_BEGIN("testUpdateFormTemplate");

		ClientBulletinStore store = appWithAccount.getStore();
		
		FieldSpecCollection topSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldCollection fields = new FieldCollection(topSpecs);
		String xmlTop = fields.toString();

		FieldSpecCollection bottomSpecs = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		fields = new FieldCollection(bottomSpecs);
		String xmlBottom = fields.toString();

		String title = "A new Title";
		String description = "Some Descritpion";
		FormTemplate newTemplate = new FormTemplate(title, description, topSpecs, bottomSpecs);
		appWithAccount.updateFormTemplate(newTemplate);

		FormTemplate savedTemplate = store.getFormTemplate(title);
		assertEquals(title, savedTemplate.getTitle());
		assertEquals(description, savedTemplate.getDescription());
		assertEquals(xmlTop, savedTemplate.getTopSectionXml());
		assertEquals(xmlBottom, savedTemplate.getBottomSectionXml());
		
		TRACE_END();
	}
	
	public void testContactInfoMigrationHqFdToContacts() throws Exception
	{
		TRACE_BEGIN("testContactInfoMigrationHqFdToContacts");
		File file = appWithAccount.getConfigInfoFile();
		file.delete();
		assertEquals("delete didn't work", false, file.exists());
		createOldConfigInfoFileWithSampleData(file);
		appWithAccount.loadConfigInfo();
		ConfigInfo config = appWithAccount.getConfigInfo();
		assertTrue("ConfigInfo file wasn't converted?", config.getVersion() >= ConfigInfo.VERSION_WITH_CONTACT_KEYS);
		assertEquals("LegacyHQ should be blank", "", config.getLegacyHQKey());
		assertEquals("Old Default HQ Keys should be blank", "", config.getDefaultHQKeysXml());
		assertEquals("Old HQ's should be blank", "", config.getAllHQKeysXml());
		assertEquals("Old FieldDesk's should be blank", "", config.getFieldDeskKeysXml());
		assertEquals("New Contact Keys does not match?", getNewConfigInfoContactsKeysXml(), config.getContactKeysXml());
		TRACE_END();
	}
	
	public void testContactInfoMigrationHqFdWithSelfIncludedAsHQandFD() throws Exception
	{
		TRACE_BEGIN("testContactInfoMigrationHqFdWithSelfIncludedAsHQandFD");
		File file = appWithAccount.getConfigInfoFile();
		file.delete();
		assertEquals("delete didn't work", false, file.exists());
		String ourClientPublicKey = appWithAccount.getAccountId();
		createOldConfigInfoFileWithSelfIncludedAsHQandFD(file, ourClientPublicKey);
		appWithAccount.loadConfigInfo();
		ConfigInfo config = appWithAccount.getConfigInfo();
		assertTrue("ConfigInfo file wasn't converted?", config.getVersion() >= ConfigInfo.VERSION_WITH_CONTACT_KEYS);
		assertEquals("LegacyHQ should be blank", "", config.getLegacyHQKey());
		assertEquals("Old HQ's should be blank", "", config.getAllHQKeysXml());
		assertEquals("Old Default HQ's should be blank", "", config.getDefaultHQKeysXml());
		assertEquals("Old FieldDesk's should be blank", "", config.getFieldDeskKeysXml());
		HeadquartersKeys allHQs = appWithAccount.getAllHQKeys();
		assertEquals("Should  have 5 keys (HQ1, HQ2, and LegacyHQ), + 2 FD's all of which are now contacts", 5, allHQs.size());
		assertFalse("Should not incluse ourself", allHQs.containsKey(ourClientPublicKey));
		HeadquartersKeys allDefaultHQs = appWithAccount.getDefaultHQKeys();
		assertEquals("Should only have 2 default keys (HQ1 and LegacyHQ)", 2, allDefaultHQs.size());
		assertFalse("Should not incluse ourself as default", allDefaultHQs.containsKey(ourClientPublicKey));
		FieldDeskKeys allFDs = appWithAccount.getFieldDeskKeys();
		assertEquals("Should only have 5 FD keys since all now just Contacts", 5, allFDs.size());
		assertFalse("Should not incluse ourself as a FD", allFDs.containsKey(ourClientPublicKey));
		ContactKeys allKeys = appWithAccount.getContactKeys();
		assertEquals("Should have 5 keys total", 5, allKeys.size());
		for(int i = 0; i < allKeys.size(); ++i )
		{
			ContactKey key = allKeys.get(i);
			assertEquals("All migrated Keys should have VERIFIED_ENTERED_MANUALLY", ContactKey.VERIFIED_ENTERED_20_DIGITS, key.getVerificationStatus());
		}
		
		TRACE_END();
	}

	public void testContactInfoMigrationDefaultHqAndHqFdToContacts() throws Exception
	{
		TRACE_BEGIN("testContactInfoMigrationDefaultHqAndHqFdToContacts");
		File file = appWithAccount.getConfigInfoFile();
		file.delete();
		assertEquals(false, file.exists());
		createOldV44WithSameLegacyDefaultHQConfigInfoFileWithSampleData(file);
		appWithAccount.loadConfigInfo();
		ConfigInfo config = appWithAccount.getConfigInfo();
		assertTrue("ConfigInfo file wasn't converted?", config.getVersion() >= ConfigInfo.VERSION_WITH_CONTACT_KEYS);
		assertEquals("LegacyHQ should be blank", "", config.getLegacyHQKey());
		assertEquals("Old Default HQ Keys should be blank", "", config.getDefaultHQKeysXml());
		assertEquals("Old HQ's should be blank", "", config.getAllHQKeysXml());
		assertEquals("Old FieldDesk's should be blank", "", config.getFieldDeskKeysXml());
		HeadquartersKeys hqKeys = appWithAccount.getAllHQKeys();
		assertEquals("Should have 1 Legacy/Default HQ, 2 normal HQs, and 2 FD's", 5, hqKeys.size());		
		TRACE_END();
}

	public void createOldV44WithSameLegacyDefaultHQConfigInfoFileWithSampleData(File file) throws Exception
	{
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(outputStream);
		out.writeShort(ConfigInfo.VERSION_WITH_CONTACT_KEYS - 1);
		out.writeUTF("author");
		out.writeUTF("org");
		out.writeUTF("email");
		out.writeUTF("web");
		out.writeUTF("phone");
		out.writeUTF("address\nline2");
		out.writeUTF("server name");
		out.writeUTF("details\ndetail2");
		out.writeUTF(getLegacyHQ());
		out.writeUTF("server pub key");
		out.writeBoolean(false);
		out.writeUTF("I am compliant");
		out.writeUTF("language;author;custom,Custom Field;title;entrydate");
		out.writeUTF("");
		out.writeBoolean(true);
		out.writeBoolean(true);
		out.writeBoolean(true);
		out.writeUTF(getOldConfigInfoHQKeysWithKey(getLegacyHQ(), "Legacy"));
		out.writeBoolean(true);
		out.writeUTF(getLegacyHQKeyXml()); //Default HQ
		out.writeUTF("");
		out.writeBoolean(true);
		ConfigInfo.writeLongString(out, "");
		ConfigInfo.writeLongString(out, "");
		out.writeBoolean(false);
		ConfigInfo.writeLongString(out, getOldConfigInfoFieldDeskKeysXml());
		out.writeBoolean(true);
		out.writeBoolean(false);
		out.writeInt(0);
		out.flush();
		out.close();
		byte[] encryptedInfo = outputStream.toByteArray();
		File signatureFile = appWithAccount.getConfigInfoSignatureFile();
		appWithAccount.encryptAndWriteFileAndSignatureFile(file, signatureFile, encryptedInfo);
	}

	public void createOldConfigInfoFileWithSampleData(File file)
			throws Exception
		{
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(outputStream);
			out.writeShort(ConfigInfo.VERSION_WITH_CONTACT_KEYS - 1);
			out.writeUTF("author");
			out.writeUTF("org");
			out.writeUTF("email");
			out.writeUTF("web");
			out.writeUTF("phone");
			out.writeUTF("address\nline2");
			out.writeUTF("server name");
			out.writeUTF("details\ndetail2");
			out.writeUTF(getLegacyHQ());
			out.writeUTF("server pub key");
			out.writeBoolean(false);
			out.writeUTF("I am compliant");
			out.writeUTF("language;author;custom,Custom Field;title;entrydate");
			out.writeUTF("");
			out.writeBoolean(true);
			out.writeBoolean(true);
			out.writeBoolean(true);
			out.writeUTF(getOldConfigInfoHQKeys());
			out.writeBoolean(true);
			String emptyDefaultHQKeys = "";
			out.writeUTF(emptyDefaultHQKeys);
			out.writeUTF("");
			out.writeBoolean(true);
			ConfigInfo.writeLongString(out, "");
			ConfigInfo.writeLongString(out, "");
			out.writeBoolean(false);
			ConfigInfo.writeLongString(out, getOldConfigInfoFieldDeskKeysXml());
			out.writeBoolean(true);
			out.writeBoolean(false);
			out.writeInt(0);
			out.flush();
			out.close();
			byte[] encryptedInfo = outputStream.toByteArray();
			File signatureFile = appWithAccount.getConfigInfoSignatureFile();
			appWithAccount.encryptAndWriteFileAndSignatureFile(file, signatureFile, encryptedInfo);
		}

	public void createOldConfigInfoFileWithSelfIncludedAsHQandFD(File file, String clientPublicKey)
			throws Exception
		{
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(outputStream);
			out.writeShort(ConfigInfo.VERSION_WITH_CONTACT_KEYS - 1);
			out.writeUTF("author");
			out.writeUTF("org");
			out.writeUTF("email");
			out.writeUTF("web");
			out.writeUTF("phone");
			out.writeUTF("address\nline2");
			out.writeUTF("server name");
			out.writeUTF("details\ndetail2");
			out.writeUTF(getLegacyHQ());
			out.writeUTF("server pub key");
			out.writeBoolean(false);
			out.writeUTF("I am compliant");
			out.writeUTF("language;author;custom,Custom Field;title;entrydate");
			out.writeUTF("");
			out.writeBoolean(true);
			out.writeBoolean(true);
			out.writeBoolean(true);
			out.writeUTF(getOldConfigInfoHQKeysWithKey(clientPublicKey, "Our self"));
			out.writeBoolean(true);
			out.writeUTF(getOldDefaultHQWithKey(clientPublicKey));
			out.writeUTF("");
			out.writeBoolean(true);
			ConfigInfo.writeLongString(out, "");
			ConfigInfo.writeLongString(out, "");
			out.writeBoolean(false);
			ConfigInfo.writeLongString(out, getOldConfigInfoFieldDeskKeysWithKeyXml(clientPublicKey));
			out.writeBoolean(true);
			out.writeBoolean(false);
			out.writeInt(0);
			out.flush();
			out.close();
			byte[] encryptedInfo = outputStream.toByteArray();
			File signatureFile = appWithAccount.getConfigInfoSignatureFile();
			appWithAccount.encryptAndWriteFileAndSignatureFile(file, signatureFile, encryptedInfo);
		}

	private String getLegacyHQ()
	{
		return "LegacyHQ";
	}
	
	private String getLegacyHQKeyXml()
	{
		HeadquartersKey legacyHq = new HeadquartersKey(getLegacyHQ());
		HeadquartersKeys contactKeys = new HeadquartersKeys(legacyHq);
		return contactKeys.toStringWithLabel();
	}

	private String getOldConfigInfoHQKeys()
	{
		Vector keys = new Vector();
		HeadquartersKey hq1 = new HeadquartersKey(hqKey1, hqKeylabel1);
		keys.add(hq1);
		HeadquartersKey hq2 = new HeadquartersKey(hqKey2, hqKeylabel2);
		keys.add(hq2);
		HeadquartersKeys contactKeys = new HeadquartersKeys(keys);
		return contactKeys.toStringWithLabel();
	}
	
	
	private String getOldConfigInfoHQKeysWithKey(String clientKeyToIncludeAswell, String clientLabel)
	{
		Vector keys = new Vector();
		keys.add(new HeadquartersKey(hqKey1, hqKeylabel1));
		keys.add(new HeadquartersKey(hqKey2, hqKeylabel2));
		keys.add(new HeadquartersKey(clientKeyToIncludeAswell, clientLabel));
		HeadquartersKeys contactKeys = new HeadquartersKeys(keys);
		return contactKeys.toStringWithLabel();
	}
	
	private String getOldDefaultHQWithKey(String clientKeyToIncludeAswell)
	{
		Vector keys = new Vector();
		keys.add(new HeadquartersKey(hqKey1, hqKeylabel1));
		keys.add(new HeadquartersKey(clientKeyToIncludeAswell, "Our self"));
		HeadquartersKeys contactKeys = new HeadquartersKeys(keys);
		return contactKeys.toStringWithLabel();
	}

	private String getOldConfigInfoFieldDeskKeysXml()
	{
		Vector keys = new Vector();
		keys.add(new FieldDeskKey(fdKey1, fdKeyLabel1));
		keys.add(new FieldDeskKey(fdKey2, fdKeyLabel2));
		FieldDeskKeys fieldDeskKeys = new FieldDeskKeys(keys);
		return fieldDeskKeys.toStringWithLabel();
	}

	private String getOldConfigInfoFieldDeskKeysWithKeyXml(String clientKeyToIncludeAswell)
	{
		Vector keys = new Vector();
		keys.add(new FieldDeskKey(fdKey1, fdKeyLabel1));
		keys.add(new FieldDeskKey(fdKey2, fdKeyLabel2));
		keys.add(new FieldDeskKey(clientKeyToIncludeAswell, "Our self"));
		FieldDeskKeys fieldDeskKeys = new FieldDeskKeys(keys);
		return fieldDeskKeys.toStringWithLabel();
	}

	private String getNewConfigInfoContactsKeysXml()
	{
		Vector keys = new Vector();
		ContactKey hqLegacyContactKey = new ContactKey(getLegacyHQ(), "");
		hqLegacyContactKey.setSendToByDefault(true);
		hqLegacyContactKey.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
		keys.add(hqLegacyContactKey);
		ContactKey hqContactKey1 = new ContactKey(hqKey1, hqKeylabel1);
		hqContactKey1.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
		keys.add(hqContactKey1);
		ContactKey hqContactKey2 = new ContactKey(hqKey2, hqKeylabel2);
		hqContactKey2.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
		keys.add(hqContactKey2);
		ContactKey fdContactKey1 = new ContactKey(fdKey1, fdKeyLabel1);
		fdContactKey1.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
		keys.add(fdContactKey1);
		ContactKey fdContactKey2 = new ContactKey(fdKey2, fdKeyLabel2);
		fdContactKey2.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
		keys.add(fdContactKey2);
		ContactKeys contactKeys = new ContactKeys(keys);
		return contactKeys.toStringWithLabel();
	}
	
	public void testRemoveSpaceLikeCharactersFromTags() throws Exception
	{
		String tagWithSpaceLikeCharacters = "a b\u00a0c\u202fd\ufeffe\u2060f"; 
		FieldSpec[] specs = new FieldSpec[] {
			FieldSpec.createStandardField(Bulletin.TAGAUTHOR, new FieldTypeNormal()),
			FieldSpec.createCustomField(tagWithSpaceLikeCharacters, "Label", new FieldTypeNormal()),
		};
		
		MartusApp.removeSpaceLikeCharactersFromTags(new FieldSpecCollection(specs));
		assertEquals(Bulletin.TAGAUTHOR, specs[0].getTag());
		assertEquals("abcdef", specs[1].getTag());
	}

	public void testCreateAccountBadDirectory() throws Exception
	{
		TRACE_BEGIN("testCreateAccountBadDirectory");

		mockSecurityForApp.clearKeyPair();
		try
		{

			File badDirectory = new File(BAD_FILENAME);
			appWithAccount.createAccountInternal(badDirectory, userName, userPassword);
			fail("Can't create an account if we can't write the file!");

		}
		catch(MartusApp.CannotCreateAccountFileException expected)
		{
		}
		assertEquals("store account not unset on error?", false, mockSecurityForApp.hasKeyPair());
		TRACE_END();
	}
	

	public void testCreateAccount() throws Exception
	{
		TRACE_BEGIN("testCreateAccount");
		MockMartusApp app = MockMartusApp.create(getName());
		app.createAccount(userName, userPassword);
		File keyPairFile = app.getCurrentKeyPairFile();
		assertEquals("not root dir?", app.getMartusDataRootDirectory(), keyPairFile.getParentFile());
		File backupKeyPairFile = MartusApp.getBackupFile(keyPairFile);
		assertEquals("no backup key file?", true, backupKeyPairFile.exists());

		assertEquals("store account not set?", app.getAccountId(), app.getStore().getAccountId());
		assertEquals("User name not set?",userName, app.getUserName());
		verifySignInThatWorks(app, userName, userPassword);

		try
		{
			app.createAccountInternal(app.getMartusDataRootDirectory(), userName+"a", userPassword);
			fail("Can't create an account if one already exists!");
		}
		catch(MartusApp.AccountAlreadyExistsException e)
		{
			// expected exception
		}
		assertEquals("store account not kept if already exists?", app.getAccountId(), app.getStore().getAccountId());

		app.deleteAllFiles();
		TRACE_END();
	}

	public void testMultipleCreateAccounts() throws Exception
	{
		TRACE_BEGIN("testMultipleCreateAccounts");
		MockMartusApp app = MockMartusApp.create(getName());
		String newUserName = "testName";
		char[] newUserPassword = "passWOrd".toCharArray();
		app.createAccount(newUserName, newUserPassword);
		File keyPairFile = app.getCurrentKeyPairFile();
		assertEquals("not root dir?", app.getMartusDataRootDirectory(), keyPairFile.getParentFile());
		assertEquals("accountDir not where the keypair file is?", keyPairFile.getParentFile(), app.getAccountDirectoryForUser(newUserName, newUserPassword));
		File backupKeyPairFile = MartusApp.getBackupFile(keyPairFile);
		assertEquals("no backup key file?", true, backupKeyPairFile.exists());

		String accountId1 = app.getAccountId();
		assertEquals("store account not set?", accountId1, app.getStore().getAccountId());
		assertEquals("User name not set?",newUserName, app.getUserName());
		verifySignInThatWorks(app, newUserName, newUserPassword);

		try
		{
			app.createAccount(newUserName, newUserPassword);
			fail("Should not be able to create an account with the same user name.");
		}
		catch(MartusApp.AccountAlreadyExistsException e)
		{
			// expected exception
		}
		assertEquals("store account not kept if already exists?", accountId1, app.getStore().getAccountId());

		String accountId2 = createAnotherAccount(app, userName2);
		assertNotEquals("account id's should be different", accountId1, accountId2);
		
		File account2KeypairFile = app.getKeyPairFile(app.getCurrentAccountDirectory());
		assertEquals("accountDir2 not where the keypair file is?", account2KeypairFile.getParentFile(), app.getAccountDirectoryForUser(userName2, userPassword));
		account2KeypairFile.delete();
		try
		{
			createAnotherAccount(app, userName2);
			fail("Can't create an account2 with the same user name even if keypair file is missing.");
		}
		catch(MartusApp.AccountAlreadyExistsException e)
		{
			// expected exception
		}
		
		String accountId3 = createAnotherAccount(app, "another");
		assertNotEquals("account1 id's should be different", accountId1, accountId3);
		assertNotEquals("account2 id's should be different", accountId2, accountId3);

		app.deleteAllFiles();
		TRACE_END();
	}
	
	private String createAnotherAccount(MockMartusApp app, String thisUserName) throws AccountAlreadyExistsException, CannotCreateAccountFileException, IOException, Exception
	{
		assertTrue("Must already have default account", app.doesDefaultAccountExist());
		app.createAccount(thisUserName, userPassword);
		File keyPairFile = app.getCurrentKeyPairFile();
		assertTrue("Keypair File for new user doesn't exist?", keyPairFile.exists());
		File accountDirectoryForUser = app.getAccountDirectoryForUser(thisUserName, userPassword);
		assertNotNull("accountDirectoryForUser should not be null :Username ="+thisUserName, accountDirectoryForUser);
		assertEquals("We dont own this directory?", keyPairFile.getParent(), accountDirectoryForUser.getPath());

		File currentAccountDirectory = app.getCurrentAccountDirectory();
		assertEquals("The directory holding the keypair file & current account directory should match",keyPairFile.getParent(), currentAccountDirectory.getAbsolutePath());
		assertTrue("Keypair file does not exist? " + keyPairFile.getPath(), keyPairFile.exists());
		assertNotEquals("Should not be in root directory?", app.getMartusDataRootDirectory(), keyPairFile.getParentFile());
		assertEquals("Parent of Parent should be the accounts dir.", app.getAccountsDirectory(), keyPairFile.getParentFile().getParentFile());
		File backupKeyPairFile = MartusApp.getBackupFile(keyPairFile);
		assertEquals("no backup key file?", true, backupKeyPairFile.exists());

		String accountId = app.getAccountId();
		assertEquals("store account not set?", accountId, app.getStore().getAccountId());
		assertEquals("User name not set?",thisUserName, app.getUserName());
		File currentAccountDirectory2 = app.getCurrentAccountDirectory();
		assertEquals("current account directory should still match",currentAccountDirectory2.getAbsolutePath(), currentAccountDirectory.getAbsolutePath());
		verifySignInThatWorks(app, thisUserName, userPassword);
		File currentAccountDirectory3 = app.getCurrentAccountDirectory();
		assertEquals("current account directory should still match",currentAccountDirectory3.getAbsolutePath(), currentAccountDirectory.getAbsolutePath());
		return accountId;
	}

	void verifySignInThatWorks(MartusApp appWithRealAccount, String userNameToUse, char[] userPasswordToUse) throws Exception
	{
		appWithRealAccount.attemptReSignIn(userNameToUse, userPasswordToUse);
		assertEquals("store account not set?", mockSecurityForApp.getPublicKeyString(), appWithAccount.getStore().getAccountId());
		assertEquals("wrong username?", userNameToUse, appWithRealAccount.getUserName());
	}

	public void testSetAndGetContactKeys() throws Exception
	{
		File configFile = appWithAccount.getConfigInfoFile();
		configFile.deleteOnExit();
		assertEquals("already exists?", false, configFile.exists());
		assertEquals("Should not have any contacts yet", 0, appWithAccount.getContactKeys().size());
		assertEquals("Should also not have any HQs as well", 0, appWithAccount.getAllHQKeys().size());
		assertEquals("Should also not have any FD's as well", 0, appWithAccount.getFieldDeskKeys().size());
		String sampleHQPublicKey = "HQ";
		String sampleHQLabel = "Fred";
		ContactKeys keys = new ContactKeys();
		ContactKey hqKey = new ContactKey(sampleHQPublicKey, sampleHQLabel);
		keys.add(hqKey);
		appWithAccount.setContactKeys(keys);
		ContactKeys keysReturned = appWithAccount.getContactKeys();
		assertEquals("Should now have 1 contact", 1, keysReturned.size());
		assertEquals("Should also have 1 HQ now as well", 1, appWithAccount.getAllHQKeys().size());
		assertEquals("Should still have 1 FD's as well since a contact is a HQ and a FD", 1, appWithAccount.getFieldDeskKeys().size());

		String sampleFDPublicKey = "FD";
		String sampleFDLabel = "Wilma";
		ContactKey fdKey = new ContactKey(sampleFDPublicKey, sampleFDLabel);
		keysReturned.add(fdKey);
		appWithAccount.setContactKeys(keysReturned);
		ContactKeys keysReturned2 = appWithAccount.getContactKeys();
		assertEquals("Should now have 2 contacts", 2, keysReturned2.size());
		assertEquals("Should now have 2 HQ now as well", 2, appWithAccount.getAllHQKeys().size());
		assertEquals("Should now have 2 FD's as well", 2, appWithAccount.getFieldDeskKeys().size());
		
		String sampleNonHqFdKey = "Not HQ Not FD";
		String sampleNonHqFdLable = "Pebbles";
		ContactKey normalContactKey = new ContactKey(sampleNonHqFdKey, sampleNonHqFdLable);
		keysReturned2.add(normalContactKey);
		appWithAccount.setContactKeys(keysReturned2);
		ContactKeys keysReturned3 = appWithAccount.getContactKeys();
		assertEquals("Should now have 3 contacts", 3, keysReturned3.size());
		assertEquals("Should also have 3 HQ", 3, appWithAccount.getAllHQKeys().size());
		assertEquals("Should also have 3 FD's as well", 3, appWithAccount.getFieldDeskKeys().size());
		
		ContactKeys emptyKeys = new ContactKeys();
		appWithAccount.setContactKeys(emptyKeys);
		ContactKeys keysReturned4 = appWithAccount.getContactKeys();
		assertEquals("Should now have 0 contacts", 0, keysReturned4.size());
		assertEquals("Should have 0 HQs", 0, appWithAccount.getAllHQKeys().size());
		assertEquals("Should have 0 FDs", 0, appWithAccount.getFieldDeskKeys().size());
	}

	public void testGetKeyVerificationStatus() throws Exception
	{
		assertEquals(ContactKey.VERIFIED_ACCOUNT_OWNER ,appWithAccount.getKeyVerificationStatus(appWithAccount.getAccountId()));

		File configFile = appWithAccount.getConfigInfoFile();
		configFile.deleteOnExit();
		String sampleHQPublicKeyNotVerified = "HQ";
		String sampleHQPublicKeyVerified20 = "HQ Verified 20";
		String sampleHQPublicKeyVerifiedVisually = "HQ Verified Visually";
		ContactKeys keys = new ContactKeys();
		ContactKey hqKeyNotVerified = new ContactKey(sampleHQPublicKeyNotVerified, "Not Verified");
		hqKeyNotVerified.setVerificationStatus(ContactKey.NOT_VERIFIED);
		ContactKey hqKeyVerified20 = new ContactKey(sampleHQPublicKeyVerified20, "Verified 20");
		hqKeyVerified20.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
		ContactKey hqKeyVerifiedVisually = new ContactKey(sampleHQPublicKeyVerifiedVisually, "Verified Visually");
		hqKeyVerifiedVisually.setVerificationStatus(ContactKey.VERIFIED_VISUALLY);
		keys.add(hqKeyNotVerified);
		keys.add(hqKeyVerified20);
		keys.add(hqKeyVerifiedVisually);		
		appWithAccount.setContactKeys(keys);
		
		assertEquals(ContactKey.NOT_VERIFIED ,appWithAccount.getKeyVerificationStatus(hqKeyNotVerified.getPublicKey()));
		assertEquals(ContactKey.VERIFIED_ENTERED_20_DIGITS ,appWithAccount.getKeyVerificationStatus(hqKeyVerified20.getPublicKey()));
		assertEquals(ContactKey.VERIFIED_VISUALLY ,appWithAccount.getKeyVerificationStatus(hqKeyVerifiedVisually.getPublicKey()));

		ContactKey unknownContactKey = new ContactKey("Unknown", "Not Verified");
		assertEquals(ContactKey.NOT_VERIFIED_UNKNOWN ,appWithAccount.getKeyVerificationStatus(unknownContactKey.getPublicKey()));
	}
	
	public void testSetAndGetHQKey() throws Exception
	{
		File configFile = appWithAccount.getConfigInfoFile();
		configFile.deleteOnExit();
		assertEquals("already exists?", false, configFile.exists());
		String sampleHQKey = "abc123";
		String sampleLabel = "Fred";
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key = new HeadquartersKey(sampleHQKey, sampleLabel);
		keys.add(key);
		appWithAccount.setAndSaveHQKeys(keys, keys);
		assertEquals("legacy public key now Blank", "", appWithAccount.getConfigInfo().getLegacyHQKey());
		assertEquals("Didn't save?", true, configFile.exists());
		assertTrue("HQ Key not saved", appWithAccount.getAllHQKeys().contains(key));
		
	}
	
	public void testGetAndSetMultipleHQKeys() throws Exception
	{
		File configFile = appWithAccount.getConfigInfoFile();
		configFile.deleteOnExit();
		String sampleHQKey1 = "abc123";
		String sampleLabel1 = "Fred";
		String sampleHQKey2 = "234567";
		String sampleLabel2 = "Bev";
		HeadquartersKeys allKeys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(sampleHQKey1, sampleLabel1);
		HeadquartersKey key2 = new HeadquartersKey(sampleHQKey2, sampleLabel2);
		allKeys.add(key1);
		allKeys.add(key2);
		HeadquartersKeys defaultKeys = new HeadquartersKeys(key1);
		
		appWithAccount.setAndSaveHQKeys(allKeys, defaultKeys);
		assertEquals("Incorrect Legacy default public key should now be blank", "", appWithAccount.getConfigInfo().getLegacyHQKey());
		assertTrue("public key1 not set", appWithAccount.getAllHQKeys().containsKey(sampleHQKey1));
		assertTrue("public key2 not set", appWithAccount.getAllHQKeys().containsKey(sampleHQKey2));
			
		HeadquartersKeys returnedKeys = appWithAccount.getAllHQKeys();
		assertTrue(returnedKeys.containsKey(sampleHQKey1));
		assertTrue(returnedKeys.containsKey(sampleHQKey2));
		HeadquartersKeys returnedKeysWithFallBack = appWithAccount.getAllHQKeysWithFallback();
		assertTrue(returnedKeysWithFallBack.containsKey(sampleHQKey1));
		assertTrue(returnedKeysWithFallBack.containsKey(sampleHQKey2));
		
		
		HeadquartersKeys returnedDefaultKeys = appWithAccount.getDefaultHQKeys();
		assertTrue(returnedDefaultKeys.containsKey(sampleHQKey1));
		assertFalse(returnedDefaultKeys.containsKey(sampleHQKey2));
		HeadquartersKeys returnedDefaultKeysWithFallBack = appWithAccount.getDefaultHQKeysWithFallback();
		assertTrue(returnedDefaultKeysWithFallBack.containsKey(sampleHQKey1));
		assertFalse(returnedDefaultKeysWithFallBack.containsKey(sampleHQKey2));

		HeadquartersKeys empty = new HeadquartersKeys();
		appWithAccount.setAndSaveHQKeys(empty, empty);
		HeadquartersKeys noFallbackKeys = appWithAccount.getAllHQKeysWithFallback();
		assertEquals("Even though we set HQKeys to empty they are still our contact so by default is an HQ.",2, noFallbackKeys.size());
		HeadquartersKeys noDefaultKeys = appWithAccount.getDefaultHQKeysWithFallback();
		assertEquals("since we cleared default HQ, this should be 0", 0, noDefaultKeys.size());
	}
	
	public void testGetAndSaveHQKeys() throws Exception
	{
		File configFile = appWithAccount.getConfigInfoFile();
		configFile.deleteOnExit();
		String sampleHQKey1 = "account1";
		String sampleLabel1 = "Fred";
		String sampleHQKey2 = "account2";
		String sampleLabel2 = "Wilma";
		HeadquartersKeys onlyAccount1and2areHQs = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(sampleHQKey1, sampleLabel1);
		HeadquartersKey key2 = new HeadquartersKey(sampleHQKey2, sampleLabel2);
		onlyAccount1and2areHQs.add(key1);
		onlyAccount1and2areHQs.add(key2);
		HeadquartersKeys defaultKeysOnlyAccount1 = new HeadquartersKeys(key1);
		appWithAccount.setAndSaveHQKeys(onlyAccount1and2areHQs, defaultKeysOnlyAccount1);
		ContactKeys allContacts = new ContactKeys(appWithAccount.getConfigInfo().getContactKeysXml());
		assertEquals("Should have 2 contacts total", 2, allContacts.size());
		HeadquartersKeys returnedHQKeys = appWithAccount.getAllHQKeys();
		assertEquals("Should only have 2 HQ contacts now total", 2, returnedHQKeys.size());
		assertTrue("We should still have Account1 as an HQ", returnedHQKeys.contains(key1));
		assertTrue("We should still have Account2 as an HQ", returnedHQKeys.contains(key2));
		
		String sampleHQKey3 = "account3";
		String sampleLabel3 = "Pebbles";
		HeadquartersKey key3 = new HeadquartersKey(sampleHQKey3, sampleLabel3);
		HeadquartersKeys onlyAccount1and3areHQs = new HeadquartersKeys();
		onlyAccount1and3areHQs.add(key1);
		onlyAccount1and3areHQs.add(key3);
		appWithAccount.setAndSaveHQKeys(onlyAccount1and3areHQs, defaultKeysOnlyAccount1);
		
		ContactKeys allNewContacts = new ContactKeys(appWithAccount.getConfigInfo().getContactKeysXml());
		assertEquals("Should have 3 contacts now total", 3, allNewContacts.size());
		ContactKey contactKey1 = allNewContacts.get(0);
		if(contactKey1.getLabel().equals(sampleLabel1))
			verifyContactInfo(sampleHQKey1, contactKey1);
		else if(contactKey1.getLabel().equals(sampleLabel2))
			verifyContactInfo(sampleHQKey2, contactKey1);
		else if(contactKey1.getLabel().equals(sampleLabel3))
			verifyContactInfo(sampleHQKey3, contactKey1);
		else
			fail("ContactKey1 not found?");
		
		ContactKey contactKey2 = allNewContacts.get(1);
		if(contactKey2.getLabel().equals(sampleLabel1))
			verifyContactInfo(sampleHQKey1, contactKey2);
		else if(contactKey2.getLabel().equals(sampleLabel2))
			verifyContactInfo(sampleHQKey2, contactKey2);
		else if(contactKey2.getLabel().equals(sampleLabel3))
			verifyContactInfo(sampleHQKey3, contactKey2);
		else
			fail("ContactKey2 not found?");

		ContactKey contactKey3 = allNewContacts.get(2);
		if(contactKey3.getLabel().equals(sampleLabel1))
			verifyContactInfo(sampleHQKey1, contactKey3);
		else if(contactKey3.getLabel().equals(sampleLabel2))
			verifyContactInfo(sampleHQKey2, contactKey3);
		else if(contactKey3.getLabel().equals(sampleLabel3))
			verifyContactInfo(sampleHQKey3, contactKey3);
		else
			fail("ContactKey3 not found?");
		
		HeadquartersKeys returnedKeys = appWithAccount.getAllHQKeys();
		assertTrue("We should still have Account1 as an HQ", returnedKeys.contains(key1));
		assertTrue("We should now have also Account3 as an HQ", returnedKeys.contains(key3));
		assertTrue("We should not have Account2 anymore as an HQ but new behavior states any Contact is also a HQ", returnedKeys.contains(key2));
		
		String sampleHQKey4 = "account4";
		String sampleLabel4 = "Dino";
		HeadquartersKey key4 = new HeadquartersKey(sampleHQKey4, sampleLabel4);
		HeadquartersKeys newDefaultKey4 = new HeadquartersKeys();
		newDefaultKey4.add(key4);
		appWithAccount.setAndSaveHQKeys(onlyAccount1and3areHQs, newDefaultKey4);
		HeadquartersKeys returnedKeysAfterNewDefaultAccountWasntIncluded = appWithAccount.getAllHQKeys();
		assertTrue("We should still have Account1 as an HQ", returnedKeysAfterNewDefaultAccountWasntIncluded.contains(key1));
		assertTrue("We should still have Account3 as an HQ", returnedKeysAfterNewDefaultAccountWasntIncluded.contains(key3));
		assertTrue("Account 2 should still not be an HQ, but since they are a Contact they are a HQ.", returnedKeysAfterNewDefaultAccountWasntIncluded.contains(key2));
		assertTrue("We should also have Account4 as an HQ", returnedKeysAfterNewDefaultAccountWasntIncluded.contains(key4));
		assertEquals("We should now have 4 HQ accounts, ie. 4 contacts", 4, returnedKeysAfterNewDefaultAccountWasntIncluded.size());
		
		HeadquartersKeys onlyAccount3isHQbutDefaultIsAccount1 = new HeadquartersKeys();
		onlyAccount3isHQbutDefaultIsAccount1.add(key3);
		appWithAccount.setAndSaveHQKeys(onlyAccount3isHQbutDefaultIsAccount1, defaultKeysOnlyAccount1);
		
		HeadquartersKeys returnedKeysAfterDefaultWasntIncluded = appWithAccount.getAllHQKeys();
		assertTrue("We should still have Account1 as an HQ since its a default HQ", returnedKeysAfterDefaultWasntIncluded.contains(key1));
		assertTrue("We should still have Account3 as an HQ since its included in all HQs", returnedKeysAfterDefaultWasntIncluded.contains(key3));
		assertTrue("Account 2 should not be an HQ anymore, except it is a contact still so it is.", returnedKeysAfterDefaultWasntIncluded.contains(key2));
		assertTrue("Account 4 should not be an HQ anymore, except is is a contact still so it too shoud still exist", returnedKeysAfterDefaultWasntIncluded.contains(key4));
		assertEquals("We should still only have 4 HQ accounts", 4, returnedKeysAfterDefaultWasntIncluded.size());
		
		FieldDeskKeys onlyAccount1and2areFDs = new FieldDeskKeys();
		FieldDeskKey fDkey1 = new FieldDeskKey(sampleHQKey1, sampleLabel1);
		FieldDeskKey FDkey2 = new FieldDeskKey(sampleHQKey2, sampleLabel2);
		onlyAccount1and2areFDs.add(fDkey1);
		onlyAccount1and2areFDs.add(FDkey2);
		appWithAccount.setAndSaveFDKeys(onlyAccount1and2areFDs);
		
		ContactKeys newContactsWithFDsAndHQs = new ContactKeys(appWithAccount.getConfigInfo().getContactKeysXml());
		assertEquals("Should have 4 contacts now total", 4, newContactsWithFDsAndHQs.size());
		contactKey1 = newContactsWithFDsAndHQs.get(0);
		if(contactKey1.getLabel().equals(sampleLabel1))
			verifyContactInfo(sampleHQKey1, contactKey1);
		else if(contactKey1.getLabel().equals(sampleLabel2))
			verifyContactInfo(sampleHQKey2, contactKey1);
		else if(contactKey1.getLabel().equals(sampleLabel3))
			verifyContactInfo(sampleHQKey3, contactKey1);
		else if(contactKey1.getLabel().equals(sampleLabel4))
			verifyContactInfo(sampleHQKey4, contactKey1);
		else
			fail("ContactKey1 not found?");
		
		contactKey2 = newContactsWithFDsAndHQs.get(0);
		if(contactKey2.getLabel().equals(sampleLabel1))
			verifyContactInfo(sampleHQKey1, contactKey2);
		else if(contactKey2.getLabel().equals(sampleLabel2))
			verifyContactInfo(sampleHQKey2, contactKey2);
		else if(contactKey2.getLabel().equals(sampleLabel3))
			verifyContactInfo(sampleHQKey3, contactKey2);
		else if(contactKey2.getLabel().equals(sampleLabel4))
			verifyContactInfo(sampleHQKey4, contactKey2);
		else
			fail("ContactKey2 not found?");
		
		contactKey3 = newContactsWithFDsAndHQs.get(0);
		if(contactKey3.getLabel().equals(sampleLabel1))
			verifyContactInfo(sampleHQKey1, contactKey3);
		else if(contactKey3.getLabel().equals(sampleLabel2))
			verifyContactInfo(sampleHQKey2, contactKey3);
		else if(contactKey3.getLabel().equals(sampleLabel3))
			verifyContactInfo(sampleHQKey3, contactKey3);
		else if(contactKey3.getLabel().equals(sampleLabel4))
			verifyContactInfo(sampleHQKey4, contactKey3);
		else
			fail("ContactKey3 not found?");

		ContactKey contactKey4 = newContactsWithFDsAndHQs.get(0);
		if(contactKey4.getLabel().equals(sampleLabel1))
			verifyContactInfo(sampleHQKey1, contactKey4);
		else if(contactKey4.getLabel().equals(sampleLabel2))
			verifyContactInfo(sampleHQKey2, contactKey4);
		else if(contactKey4.getLabel().equals(sampleLabel3))
			verifyContactInfo(sampleHQKey3, contactKey4);
		else if(contactKey4.getLabel().equals(sampleLabel4))
			verifyContactInfo(sampleHQKey4, contactKey4);
		else
			fail("ContactKey4 not found?");
	}

	public void testChangingHQLabels() throws Exception
	{
		File configFile = appWithAccount.getConfigInfoFile();
		configFile.deleteOnExit();
		String sampleHQKey1 = "account1";
		String sampleLabel1 = "Fred";
		String sampleHQKey2 = "account2";
		String sampleLabel2 = "Wilma";
		HeadquartersKey key1 = new HeadquartersKey(sampleHQKey1, sampleLabel1);
		HeadquartersKey key2 = new HeadquartersKey(sampleHQKey2, sampleLabel2);

		HeadquartersKeys key1andkey2 = new HeadquartersKeys();
		key1andkey2.add(key1);
		key1andkey2.add(key2);
		HeadquartersKeys defaultKeys = new HeadquartersKeys();
		defaultKeys.add(key1);
		appWithAccount.setAndSaveHQKeys(key1andkey2, defaultKeys);
		HeadquartersKeys returnedKeysAfterAddingH1andH2asBothHQandDefault = appWithAccount.getAllHQKeys();

		assertTrue("We should still have Account1 as HQ anddefault HQ", returnedKeysAfterAddingH1andH2asBothHQandDefault.contains(key1));
		assertTrue("We should still have Account2 as HQ anddefault HQ", returnedKeysAfterAddingH1andH2asBothHQandDefault.contains(key2));
		assertEquals("We should still only have 2 HQ accounts", 2, returnedKeysAfterAddingH1andH2asBothHQandDefault.size());
		HeadquartersKey testKey = returnedKeysAfterAddingH1andH2asBothHQandDefault.get(0);
		assertEquals("Key1 Label not correct?",sampleLabel1, testKey.getLabel());
		assertEquals("Key1 should not be verified", ContactKey.NOT_VERIFIED_UNKNOWN, testKey.getVerificationStatus());
		assertTrue("HQ Key1 Can't Receive From", testKey.getCanReceiveFrom());
		assertTrue("HQ Key1 Can't Send To?", testKey.getCanSendTo());

		String newLabel1 = "Flinstone";

		key1.setLabel(newLabel1);
		HeadquartersKeys updatedLabelKey1andkey2 = new HeadquartersKeys();
		updatedLabelKey1andkey2.add(key1);
		updatedLabelKey1andkey2.add(key2);

		HeadquartersKeys updatedLabelDefaultKey1 = new HeadquartersKeys();
		updatedLabelDefaultKey1.add(key1);

		
		appWithAccount.setAndSaveHQKeys(updatedLabelKey1andkey2, updatedLabelDefaultKey1);
		HeadquartersKeys returnedKeysAfterUpdatingH1andH2asBothHQandDefault = appWithAccount.getAllHQKeys();
		
		assertTrue("We should still have Account1 as HQ anddefault HQ", returnedKeysAfterUpdatingH1andH2asBothHQandDefault.contains(key1));
		assertTrue("We should still have Account2 as HQ anddefault HQ", returnedKeysAfterUpdatingH1andH2asBothHQandDefault.contains(key2));
		assertEquals("We should still only have 2 HQ accounts", 2, returnedKeysAfterUpdatingH1andH2asBothHQandDefault.size());
		HeadquartersKey testKey2 = returnedKeysAfterUpdatingH1andH2asBothHQandDefault.get(0);
		assertEquals("Key1 Label not changed?",newLabel1, testKey2.getLabel());

		HeadquartersKeys key1andUpdatedLabelkey2 = new HeadquartersKeys();
		String UpdatedKey2Label = "Rubble";
		key2.setLabel(UpdatedKey2Label);
		key1andUpdatedLabelkey2.add(key1);
		key1andUpdatedLabelkey2.add(key2);
		appWithAccount.setAndSaveHQKeys(key1andUpdatedLabelkey2, updatedLabelDefaultKey1);
		HeadquartersKeys returnedKeysAfterUpdatingKey2Label = appWithAccount.getAllHQKeys();
		
		assertTrue("We should still have Account1 as HQ anddefault HQ", returnedKeysAfterUpdatingKey2Label.contains(key1));
		assertTrue("We should still have Account2 as HQ anddefault HQ", returnedKeysAfterUpdatingKey2Label.contains(key2));
		assertEquals("We should still only have 2 HQ accounts", 2, returnedKeysAfterUpdatingKey2Label.size());
		HeadquartersKey testKey3 = returnedKeysAfterUpdatingKey2Label.get(1);
		assertEquals("Key2 Label not changed?",UpdatedKey2Label, testKey3.getLabel());
		
	}
	
	public void testSetAndSaveFDKeys() throws Exception
	{
		File configFile = appWithAccount.getConfigInfoFile();
		configFile.deleteOnExit();
		String sampleFDKey1 = "account1";
		String sampleLabel1 = "Fred";
		String sampleFDKey2 = "account2";
		String sampleLabel2 = "Wilma";
		FieldDeskKeys onlyAccount1and2areFDs = new FieldDeskKeys();
		FieldDeskKey key1 = new FieldDeskKey(sampleFDKey1, sampleLabel1);
		FieldDeskKey key2 = new FieldDeskKey(sampleFDKey2, sampleLabel2);
		onlyAccount1and2areFDs.add(key1);
		onlyAccount1and2areFDs.add(key2);
		appWithAccount.setAndSaveFDKeys(onlyAccount1and2areFDs);
		ContactKeys allContacts = new ContactKeys(appWithAccount.getConfigInfo().getContactKeysXml());
		assertEquals("Should have 2 contacts total", 2, allContacts.size());
		FieldDeskKeys returnedFDKeys = appWithAccount.getFieldDeskKeys();
		assertEquals("Should only have 2 FD contacts now total", 2, returnedFDKeys.size());
		assertTrue("We should have Account1 as an FD", returnedFDKeys.containsKey(sampleFDKey1));
		assertTrue("We should have Account2 as an FD", returnedFDKeys.containsKey(sampleFDKey2));
		
		String sampleFDKey3 = "account3";
		String sampleLabel3 = "Pebbles";
		FieldDeskKey key3 = new FieldDeskKey(sampleFDKey3, sampleLabel3);
		FieldDeskKeys onlyAccount1and3areFDs = new FieldDeskKeys();
		onlyAccount1and3areFDs.add(key1);
		onlyAccount1and3areFDs.add(key3);
		appWithAccount.setAndSaveFDKeys(onlyAccount1and3areFDs);
		
		ContactKeys allNewContacts = new ContactKeys(appWithAccount.getConfigInfo().getContactKeysXml());
		assertEquals("Should have 3 contacts now total", 3, allNewContacts.size());
		ContactKey contactKey1 = allNewContacts.get(0);
		if(contactKey1.getLabel().equals(sampleLabel1))
			verifyContactInfo(sampleFDKey1, contactKey1);
		else if(contactKey1.getLabel().equals(sampleLabel2))
			verifyContactInfo(sampleFDKey2, contactKey1);
		else if(contactKey1.getLabel().equals(sampleLabel3))
			verifyContactInfo(sampleFDKey3, contactKey1);
		else
			fail("ContactKey1 not found?");
		
		ContactKey contactKey2 = allNewContacts.get(1);
		if(contactKey2.getLabel().equals(sampleLabel1))
			verifyContactInfo(sampleFDKey1, contactKey2);
		else if(contactKey2.getLabel().equals(sampleLabel2))
			verifyContactInfo(sampleFDKey2, contactKey2);
		else if(contactKey2.getLabel().equals(sampleLabel3))
			verifyContactInfo(sampleFDKey3, contactKey2);
		else
			fail("ContactKey2 not found?");

		ContactKey contactKey3 = allNewContacts.get(2);
		if(contactKey3.getLabel().equals(sampleLabel1))
			verifyContactInfo(sampleFDKey1, contactKey3);
		else if(contactKey3.getLabel().equals(sampleLabel2))
			verifyContactInfo(sampleFDKey2, contactKey3);
		else if(contactKey3.getLabel().equals(sampleLabel3))
			verifyContactInfo(sampleFDKey3, contactKey3);
		else
			fail("ContactKey3 not found?");
		
		FieldDeskKeys returnedKeys = appWithAccount.getFieldDeskKeys();
		assertTrue("We should still have Account1 as an FD", returnedKeys.containsKey(sampleFDKey1));
		assertTrue("We should now have also Account3 as an FD", returnedKeys.containsKey(sampleFDKey3));
		assertTrue("We should not have Account2 anymore as a FD, but behavior changed now all Contacts are HQ's and FD's", returnedKeys.containsKey(sampleFDKey2));
	}

	public void testChangingFDLabels() throws Exception
	{
		File configFile = appWithAccount.getConfigInfoFile();
		configFile.deleteOnExit();
		String sampleFDKey1 = "account1";
		String sampleLabel1 = "Fred";
		String sampleFDKey2 = "account2";
		String sampleLabel2 = "Wilma";
		FieldDeskKey key1 = new FieldDeskKey(sampleFDKey1, sampleLabel1);
		FieldDeskKey key2 = new FieldDeskKey(sampleFDKey2, sampleLabel2);

		FieldDeskKeys key1andkey2 = new FieldDeskKeys();
		key1andkey2.add(key1);
		key1andkey2.add(key2);
		appWithAccount.setAndSaveFDKeys(key1andkey2);
		FieldDeskKeys returnedKeysAfterAddingBothFDs = appWithAccount.getFieldDeskKeys();

		assertTrue("We should have Account1 as FD", returnedKeysAfterAddingBothFDs.contains(key1));
		assertTrue("We should have Account2 as FD", returnedKeysAfterAddingBothFDs.contains(key2));
		assertEquals("We should only have 2 FD accounts", 2, returnedKeysAfterAddingBothFDs.size());
		FieldDeskKey testKey = returnedKeysAfterAddingBothFDs.get(0);
		assertEquals("Key1 Label not correct?",sampleLabel1, testKey.getLabel());
		assertEquals("Key1 should not be verified", ContactKey.NOT_VERIFIED_UNKNOWN, testKey.getVerificationStatus());
		assertTrue("FD Key1 Can't Receive From", testKey.getCanReceiveFrom());
		assertTrue("FD Key1 Can't Send To?", testKey.getCanSendTo());
		String newLabel1 = "Flinstone";

		key1.setLabel(newLabel1);
		FieldDeskKeys updatedLabelKey1andkey2 = new FieldDeskKeys();
		updatedLabelKey1andkey2.add(key1);
		updatedLabelKey1andkey2.add(key2);
		
		appWithAccount.setAndSaveFDKeys(updatedLabelKey1andkey2);
		FieldDeskKeys returnedKeysAfterUpdatingH1andH2 = appWithAccount.getFieldDeskKeys();
		
		assertTrue("We should still have Account1 as FD", returnedKeysAfterUpdatingH1andH2.contains(key1));
		assertTrue("We should still have Account2 as FD", returnedKeysAfterUpdatingH1andH2.contains(key2));
		assertEquals("We should still only have 2 FD accounts", 2, returnedKeysAfterUpdatingH1andH2.size());
		FieldDeskKey testKey2 = returnedKeysAfterUpdatingH1andH2.get(0);
		assertEquals("Key1 Label not changed?",newLabel1, testKey2.getLabel());
	}	
	
	private void verifyContactInfo(String publicKey, ContactKey contactKey)
	{
		assertEquals(publicKey + ": Public Code doesn't match?", publicKey, contactKey.getPublicKey());
	}
	
	public void testClearHQKey() throws Exception
	{
		File configFile = appWithAccount.getConfigInfoFile();
		configFile.deleteOnExit();
		assertEquals("already exists?", false, configFile.exists());
		HeadquartersKeys empty = new HeadquartersKeys();
		appWithAccount.setAndSaveHQKeys(empty, empty);
		assertEquals("HQ key exists?", "", appWithAccount.getConfigInfo().getLegacyHQKey());
		assertEquals("Didn't save?", true, configFile.exists());

		String sampleHQKey1 = "abc123";
		String sampleLabel1 = "Fred";
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(sampleHQKey1, sampleLabel1);
		keys.add(key1);

		appWithAccount.setAndSaveHQKeys(keys,keys);
		assertEquals("Incorrect public key", "", appWithAccount.getConfigInfo().getLegacyHQKey());
		assertEquals("all keys not set?", 1, appWithAccount.getAllHQKeys().size());
		assertEquals("Default keys not set", 1, appWithAccount.getDefaultHQKeys().size());

		appWithAccount.setAndSaveHQKeys(empty,empty);
		assertEquals("HQ not cleared", "", appWithAccount.getConfigInfo().getLegacyHQKey());
		assertEquals("All HQs not cleared? HQ's may be cleared but You still have a contact", 1, appWithAccount.getAllHQKeys().size());
		assertEquals("Default HQs not cleared", 0, appWithAccount.getDefaultHQKeys().size());
	}

	public void testGetCombinedPassPhrase()
	{
		char[] combined1 = PasswordHelper.getCombinedPassPhrase(userName, userPassword);
		char[] combined2 = PasswordHelper.getCombinedPassPhrase(userName2, userPassword);
		char[] combined3 = PasswordHelper.getCombinedPassPhrase(userName, userPassword2);
		assertFalse("username diff", Arrays.equals(combined1, combined2));
		assertFalse("password diff",  Arrays.equals(combined1, combined3));

		char[] ab_c = PasswordHelper.getCombinedPassPhrase("ab", "c".toCharArray());
		char[] a_bc = PasswordHelper.getCombinedPassPhrase("a", "bc".toCharArray());
		assertFalse("abc diff", Arrays.equals(ab_c, a_bc));
	}

	public void testAttemptSignInBadKeyPairFile() throws Exception
	{
		TRACE_BEGIN("testAttemptSignInBadKeyPairFile");

		File badFile = new File(BAD_FILENAME);
		try
		{
			appWithAccount.attemptSignInInternal(badFile, userName, userPassword);
			fail("didn't throw on bad file?");
		}
		catch (Exception expected)
		{
		}
		assertEquals("keypair not cleared?", false, mockSecurityForApp.hasKeyPair());
		assertEquals("non-blank username?", "", appWithAccount.getUserName());
		appWithAccount.getSecurity().createKeyPair();
		TRACE_END();
	}	
	
	
	public void testSignInNonExistentAccount() throws Exception
	{
		TRACE_BEGIN("testSignInNonExistentAccount");

		mockSecurityForApp.createKeyPair();
		File tempDirectory = createTempDirectory();
		tempDirectory.deleteOnExit();
		appWithAccount.setCurrentAccount(userName, tempDirectory);
		try
		{
			File noAccount = new File(NONEXISTENT_FILENAME);
			noAccount.delete();
			appWithAccount.attemptSignInInternal(noAccount, userName, userPassword);
			fail("Can't open an account that doesn't exist!");

		}
		catch(AuthorizationFailedException expected)
		{
		}
		catch(Exception e)
		{
			fail("Didn't throw AuthorizationFailedException?");
		}
		assertEquals("keypair not cleared?", false, mockSecurityForApp.hasKeyPair());
		assertEquals("non-blank username?", "", appWithAccount.getUserName());
		TRACE_END();
	}

	public void testFileOutputStreamReadOnly() throws Exception
	{
		TRACE_BEGIN("testFileOutputStreamReadOnly");
		File readOnlyFile = createTempFileFromName("FileOutputStreamReadOnly_"+ getName());
		readOnlyFile.setReadOnly();
		try
		{
			new FileOutputStream(readOnlyFile);
			fail("Should have thrown IO exception on ReadOnly File");
		}
		catch (IOException expected)
		{
		}
		readOnlyFile.delete();
		TRACE_END();
	}
	
	public void testScrubAndDeleteKeyPairFileAndRelatedFiles() throws Exception
	{
		TRACE_BEGIN("testScrubAndDeleteKeyPairFileAndRelatedFiles");

		File accountsDirectory = appWithAccount.getCurrentAccountDirectory();
		File keyPairFile = appWithAccount.getCurrentKeyPairFile();	
		File backupKeyPairFile = MartusApp.getBackupFile(keyPairFile);
		File accountTokenFile = appWithAccount.getUserNameHashFile(keyPairFile.getParentFile());
		File configInfoFile = appWithAccount.getConfigInfoFile();
		File configInfoSigFile = appWithAccount.getConfigInfoSignatureFile();
		File uploadedFile = appWithAccount.getUploadInfoFile();
		File uiStateFile = appWithAccount.getUiStateFileForAccount(accountsDirectory);
		File foldersFile = ClientBulletinStore.getFoldersFileForAccount(accountsDirectory);
		File cacheFile = ClientBulletinStore.getCacheFileForAccount(accountsDirectory);
		File key1File = new File(accountsDirectory, "Key1.mpi");
		File key2File = new File(accountsDirectory, "Key2.mpi");
		
		appWithAccount.writeKeyPairFileWithBackup(keyPairFile,"UserA", "Password".toCharArray());
		appWithAccount.saveConfigInfo();
		FileOutputStream out = new FileOutputStream(uploadedFile);
		out.write(1);
		out.close();
		FileOutputStream out2 = new FileOutputStream(uiStateFile);
		out2.write(1);
		out2.close();
		FileOutputStream out3 = new FileOutputStream(foldersFile);
		out3.write(1);
		out3.close();
		FileOutputStream out4 = new FileOutputStream(cacheFile);
		out4.write(1);
		out4.close();
		FileOutputStream out5 = new FileOutputStream(key1File);
		out5.write(1);
		out5.close();
		FileOutputStream out6 = new FileOutputStream(key2File);
		out6.write(1);
		out6.close();
		key1File.setReadOnly();
	 
		assertTrue("keypair file doesn't exist?", keyPairFile.exists());
		assertTrue("backup keypair file doesn't exist?", backupKeyPairFile.exists());
		assertTrue("account Token file doesn't exist?", accountTokenFile.exists());
		assertTrue("configInfo file doesn't exist?", configInfoFile.exists());
		assertTrue("configInfo sig file doesn't exist?", configInfoSigFile.exists());
		assertTrue("upload reminder file doesn't exist?", uploadedFile.exists());
		assertTrue("uiState file doesn't exist?", uiStateFile.exists());
		assertTrue("folders file doesn't exist?", foldersFile.exists());
		assertTrue("cache file doesn't exist?", cacheFile.exists());
		assertTrue("key1 file doesn't exist?", key1File.exists());
		assertTrue("key2 file doesn't exist?", key2File.exists());
		
		appWithAccount.deleteKeypairAndRelatedFilesForAccount(accountsDirectory);
		
		//TODO Make sure the files really get scrubbed.
		
		assertFalse("keypair file still exist?", keyPairFile.exists());
		assertFalse("backup keypair file still exist?", backupKeyPairFile.exists());
		assertFalse("account Token file still exists?", accountTokenFile.exists());
		assertFalse("configInfo file still exists?", configInfoFile.exists());
		assertFalse("configInfo sig file still exists?", configInfoSigFile.exists());
		assertFalse("upload reminder file still exists?", uploadedFile.exists());
		assertFalse("uiState file still exists?", uiStateFile.exists());
		assertFalse("folders file still exists?", foldersFile.exists());
		assertFalse("cache file still exists?", cacheFile.exists());
		assertFalse("key1 file still exists?", key1File.exists());
		assertFalse("key2 file still exists?", key2File.exists());
			
		TRACE_END();		
	}
	
	protected void checkScrubbedData(File file) throws Exception
	{
		RandomAccessFile randomFile = new RandomAccessFile(file, "r");
		randomFile.seek(0);
		for (int i = 0; i < randomFile.length(); i++)
		{
			assertEquals("wrong byte?", 0x55, randomFile.read());
		}
		randomFile.close();
	}

	public void testAttemptSignInAuthorizationFailure() throws Exception
	{
		TRACE_BEGIN("testAttemptSignInAuthorizationFailure");

		mockSecurityForApp.fakeAuthorizationFailure = true;
		try
		{
			appWithAccount.attemptSignIn(userName, userPassword);
			fail("should throw here");
		}
		catch (Exception expected)
		{
		}
		assertEquals("keypair not cleared?", false, mockSecurityForApp.hasKeyPair());
		mockSecurityForApp.fakeAuthorizationFailure = false;
		appWithAccount.getSecurity().createKeyPair();

		TRACE_END();
	}

	public void testAttemptReSignInAuthorizationFailure() throws Exception
	{
		TRACE_BEGIN("testAttemptReSignInAuthorizationFailure");
		MockMartusApp app = MockMartusApp.create(getName());
		app.createAccount(userName, userPassword);
		app.getSecurity().clearKeyPair();
		app.currentUserName = "";
		try
		{
			app.attemptReSignIn(userName, userPassword);
			fail("Before Signin should throw");
		}
		catch (Exception expected)
		{
		}		
		assertNull("keypair not empty?", app.getAccountId());
		app.attemptSignIn(userName, userPassword);
		assertNotNull("keypair empty?", app.getAccountId());
		app.attemptReSignIn(userName, userPassword);
		assertNotNull("keypair cleared?", app.getAccountId());

		String oldAccountId = app.getAccountId();
		try
		{
			app.attemptReSignIn(userName+"x", userPassword);
			fail("wrong resignin should throw");
		}
		catch (Exception expected)
		{
		}	
		assertEquals("keypair cleared for bad username?", oldAccountId, app.getAccountId());

		try
		{
			app.attemptReSignIn(userName, "wrong passphrase by a mile".toCharArray());
			fail("Should have thrown for missing keypair");
		}
		catch(Exception ignoreExpectedException)
		{
		}
		assertEquals("keypair cleared for missing keypair file?", oldAccountId, app.getAccountId());

		app.attemptReSignIn(userName, userPassword);
		assertEquals("keypair not still there?", oldAccountId, app.getAccountId());
		app.deleteAllFiles();
		TRACE_END();
	}

	public void testAttemptSignInToAdditionalAccount() throws Exception
	{
		TRACE_BEGIN("testAttemptSignInToAdditionalAccount");
		MockMartusApp app = MockMartusApp.create(getName());
		app.createAccount(userName, userPassword);
		String userNameTest2 = "user2";
		char[] userPasswordTest2 = "pass2".toCharArray();
		app.createAccount(userNameTest2, userPasswordTest2);
		
		app.attemptSignIn(userNameTest2, userPasswordTest2);
		app.attemptSignIn(userName, userPassword);
		app.deleteAllFiles();
		TRACE_END();
	}
	
	
	public void testIsUserOwnerOfThisAccountDirectory() throws Exception
	{
		MockMartusApp app = MockMartusApp.create(getName());
		File tempDirectory = createTempDirectory();
		tempDirectory.deleteOnExit();
		File hashFile = app.getUserNameHashFile(tempDirectory);
		hashFile.deleteOnExit();
		assertFalse("Should not have this hash file yet", hashFile.exists());
		String username = "chuck";
		assertFalse("This user should not own this directory", app.isUserOwnerOfThisAccountDirectory(mockSecurityForApp, username, null, app.getCurrentAccountDirectory()));
		
		app.setCurrentAccount(username, tempDirectory);
		assertEquals("Current Account Directory not set?", tempDirectory, app.getCurrentAccountDirectory());
		assertTrue("Hash File should now exist", hashFile.exists());
		assertTrue("This user should be the owner of this directory", app.isUserOwnerOfThisAccountDirectory(mockSecurityForApp, username, null, app.getCurrentAccountDirectory()));

		String myUserName = "goodMan";
		char[] myPassword = "goodM".toCharArray();
		app.createAccount(myUserName,myPassword);
		assertTrue("My new user should be the owner of this directory", app.isUserOwnerOfThisAccountDirectory(mockSecurityForApp, myUserName, myPassword, app.getCurrentAccountDirectory()));
		File myHashFile = app.getUserNameHashFile(app.getCurrentAccountDirectory()); 
		myHashFile.delete();
		assertTrue("My new user should still be the owner of this directory even without a hashFile", app.isUserOwnerOfThisAccountDirectory(mockSecurityForApp, myUserName, myPassword, app.getCurrentAccountDirectory()));
		hashFile.delete();
		tempDirectory.delete();
		app.deleteAllFiles();
	}
	
	public void testAttemptSignInKeyPairVersionFailure() throws Exception
	{
		TRACE_BEGIN("testAttemptSignInKeyPairVersionFailure");

		mockSecurityForApp.fakeKeyPairVersionFailure = true;
		try
		{
			appWithAccount.attemptSignIn(userName, userPassword);
			fail("should throw.");
		}
		catch (Exception expected)
		{
		}
		assertEquals("keypair not cleared?", false, mockSecurityForApp.hasKeyPair());
		mockSecurityForApp.fakeKeyPairVersionFailure = false;
		appWithAccount.getSecurity().createKeyPair();

		TRACE_END();
	}

	public void testDoesAccountExist() throws Exception
	{
		TRACE_BEGIN("testDoesAccountExist");

		File keyPairFile = appWithAccount.getCurrentKeyPairFile();
		keyPairFile.delete();
		assertEquals("account exists without a file?", false, appWithAccount.doesAnyAccountExist());

		FileOutputStream out = new FileOutputStream(keyPairFile);
		out.write(0);
		out.close();

		assertEquals("account doesn't exist with a file?", true, appWithAccount.doesAnyAccountExist());

		keyPairFile.delete();

		TRACE_END();
	}

	public void testGetAllAccountDirectories() throws Exception
	{
		TRACE_BEGIN("testGetAllAccountDirectories");
		File rootDir = createTempDirectory();
		MartusApp app = new MartusApp(mockSecurityForApp, rootDir, testAppLocalization);
		assertEquals("1 account should exist", 1, app.getAllAccountDirectories().size());
		assertEquals("not root dir?", rootDir, app.getAllAccountDirectories().get(0));
		
		File accountsDir = app.getAccountsDirectory();
		accountsDir.deleteOnExit();
		String directoryName1 = "1111.1111.1111.1111.1111";
		File newAccountDir = new File(accountsDir, directoryName1);
		newAccountDir.deleteOnExit();
		newAccountDir.mkdirs();
		
		String directoryName3 = "3333.3333.3333.3333.3333";
		File thirdAccountDir = new File(accountsDir, directoryName3);
		thirdAccountDir.deleteOnExit();
		thirdAccountDir.mkdirs();
		
		File nonAccountDir = new File(accountsDir, "notAPublicCodeBut24long!");
		nonAccountDir.deleteOnExit();
		nonAccountDir.mkdirs();

		File wrongAccountLengthDir = new File(accountsDir, "4444.4444");
		wrongAccountLengthDir.deleteOnExit();
		wrongAccountLengthDir.mkdirs();
		
		File notADirectory = File.createTempFile("justAFile", ".test", accountsDir);
		
		assertEquals("3 account should now exist", 3, app.getAllAccountDirectories().size());
		assertContains("no root dir?", rootDir, app.getAllAccountDirectories());
		assertContains("no newAccountDir dir?", newAccountDir, app.getAllAccountDirectories());
		assertContains("no thirdAccountDir dir?", thirdAccountDir, app.getAllAccountDirectories());
		
		wrongAccountLengthDir.delete();
		notADirectory.delete();
		nonAccountDir.delete();
		new File(thirdAccountDir, "AccountToken.txt").delete();
		thirdAccountDir.delete();
		new File(newAccountDir, "AccountToken.txt").delete();
		newAccountDir.delete();
		accountsDir.delete();
		TRACE_END();
	}

	public void testGetAccountDirectory() throws Exception
	{
		TRACE_BEGIN("testGetAccountDirectory");
		File rootDir = createTempDirectory();
		try
		{
			MartusApp app = new MartusApp(mockSecurityForApp, rootDir, testAppLocalization);
			
			assertEquals("first account not root dir?", rootDir, app.getAccountDirectory("anything"));
	
			String username1 = "name";
			createAccount(app, username1);
			String realAccountId1 = app.getAccountId();
			app.doAfterSigninInitalization();
			saveConfigInfo(app);
			
			String username2 = "other";
			createAccount(app, username2);
			String realAccountId2 = app.getAccountId();
			saveConfigInfo(app);
	
			File account2Directory = app.getAccountDirectory(realAccountId2);
			String digest2 = MartusCrypto.computeFormattedPublicCode(realAccountId2);
			assertEquals("second account not in digest dir?", digest2, account2Directory.getName());

			String sillyAccountId = "something new";
			File sillyAccountDirectory = app.getAccountDirectory(sillyAccountId);
			String sillyDigest = MartusCrypto.computeFormattedPublicCode(sillyAccountId);
			assertEquals("silly account not in digest dir?", sillyDigest, sillyAccountDirectory.getName());
			assertTrue("didn't create silly dir?", sillyAccountDirectory.exists());
			
			File rootAccountDirectory = app.getAccountDirectory(realAccountId1);
			assertEquals("not root?", rootDir.getAbsolutePath(), rootAccountDirectory.getAbsolutePath());
			
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(rootDir);
		}
		TRACE_END();
	}
	
	
	private void saveConfigInfo(MartusApp app) throws SaveConfigInfoException 
	{
		File configFile1 = app.getConfigInfoFile();
		app.saveConfigInfo();
		assertTrue("no config?", configFile1.exists());
		File sigFile1 = app.getConfigInfoSignatureFile();
		assertTrue("no config sig?", sigFile1.exists());
	}

	private void createAccount(MartusApp app, String username1) throws CannotCreateAccountFileException, Exception 
	{
		char[] password1 = "pass".toCharArray();
		app.createAccount(username1, password1);
	}

	public void testDoesAccountExistForMultipleAccounts() throws Exception
	{
		TRACE_BEGIN("testDoesAccountExistForMultipleAccounts");
		File rootDir = createTempDirectory();
		MartusApp app = new MartusApp(mockSecurityForApp, rootDir, testAppLocalization);
		
		File accountsDir = app.getAccountsDirectory();
		accountsDir.deleteOnExit();
		String accountDirectoryName = "1234.5678.9012.3456.7890";
		File newAccountDir = new File(accountsDir, accountDirectoryName);
		newAccountDir.deleteOnExit();
		newAccountDir.mkdirs();
		File keyPairFile = new File(newAccountDir,MartusApp.KEYPAIR_FILENAME);
		keyPairFile.deleteOnExit();
		
		assertEquals("account should not exist yet", false, app.doesAnyAccountExist());
		
		FileOutputStream out = new FileOutputStream(keyPairFile);
		out.write(0);
		out.close();
		assertEquals("account in a sub folder doesn't exist with a file?", true, app.doesAnyAccountExist());
		
		new File(newAccountDir, "AccountToken.txt").delete();
		keyPairFile.delete();
		newAccountDir.delete();
		accountsDir.delete();
		TRACE_END();
	}
	
	public void testDoesDefaultAccountExist() throws Exception
	{
		TRACE_BEGIN("testDoesDefaultAccountExist");

		File keyPairFile = appWithAccount.getCurrentKeyPairFile();
		keyPairFile.delete();
		assertEquals("Default account exists?", false, appWithAccount.doesDefaultAccountExist());

		FileOutputStream out = new FileOutputStream(keyPairFile);
		out.write(0);
		out.close();

		assertEquals("Default Account doesn't exist?", true, appWithAccount.doesDefaultAccountExist());
		File rootPacketsDir = new File(keyPairFile.getParentFile(), MartusApp.PACKETS_DIRECTORY_NAME);
		rootPacketsDir.deleteOnExit();
		rootPacketsDir.mkdir();
		assertEquals("Default Account should still exist?", true, appWithAccount.doesDefaultAccountExist());
		
		keyPairFile.delete();
		assertEquals("Default Account should not exist because the packets dir is empty.", false, appWithAccount.doesDefaultAccountExist());
		File anyFile = new File(rootPacketsDir,"anyFile");
		anyFile.deleteOnExit();
		FileOutputStream outFile = new FileOutputStream(anyFile);
		outFile.write(0);
		outFile.close();
		assertEquals("Default Account should now exist because the packets dir is not empty.", true, appWithAccount.doesDefaultAccountExist());
		anyFile.delete();
		
		rootPacketsDir.delete();
		assertEquals("Default account should now not exist", false, appWithAccount.doesDefaultAccountExist());
		
		TRACE_END();
	}
	
	public void testCreateBulletin() throws Exception
	{
		TRACE_BEGIN("testCreateBulletin");
		mockSecurityForApp.loadSampleAccount();
		ConfigInfo info = appWithAccount.getConfigInfo();
		String source = "who?";
		String organization = "those guys";
		String template = "Was there a bomb?";
		info.setAuthor(source);
		info.setOrganization(organization);
		info.setTemplateDetails(template);
		Bulletin b = appWithAccount.createBulletin();
		assertNotNull("null Bulletin", b);
		assertEquals(source, b.get(Bulletin.TAGAUTHOR));
		assertEquals(organization, b.get(Bulletin.TAGORGANIZATION));
		assertEquals(template, b.get(Bulletin.TAGPUBLICINFO));
		assertEquals(Bulletin.STATUSMUTABLE, b.getStatus());
		assertEquals("not automatically private?", true, b.isAllPrivate());
		TRACE_END();
	}

	public void testLoadBulletins() throws Exception
	{
		TRACE_BEGIN("testLoadBulletins");
		mockSecurityForApp.loadSampleAccount();

		appWithAccount.loadSampleData(); //SLOW!!!

		ClientBulletinStore store = appWithAccount.getStore();
		int sampleCount = store.getBulletinCount();
		assertTrue("Should start with samples", sampleCount > 0);

		appWithAccount.loadFolders();
		assertEquals("Should have loaded samples", sampleCount, store.getBulletinCount());
		BulletinFolder sent = store.getFolderSaved();
		assertEquals("Sent should have bulletins", sampleCount, sent.getBulletinCount());

		store.deleteAllData();
		assertEquals("Should have deleted samples", 0, store.getBulletinCount());
		appWithAccount.loadFolders();

		TRACE_END();
	}

	public void testSearch() throws Exception
	{
		TRACE_BEGIN("testSearch");
		ClientBulletinStore store = appWithAccount.getStore();
		assertNull("Search results already exists?", store.findFolder(store.getSearchFolderName()));

		appWithAccount.loadSampleData(); //SLOW!!!
		appWithAccount.getLocalization().setCurrentLanguageCode(MiniLocalization.ENGLISH);

		
		Bulletin b = store.getBulletinRevision((UniversalId)(new Vector(store.getAllBulletinLeafUids()).get(0)));
		String andKeyword = "and";
		String orKeyword = "or";
		search(appWithAccount, b.get("title"), andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertNotNull("Search results should have been created", store.getSearchFolderName());

		search(appWithAccount, "--not in any bulletin--", andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals("search should clear results folder", 0, store.findFolder(store.getSearchFolderName()).getBulletinCount());

		assertTrue("not enough bulletins?", appWithAccount.getStore().getBulletinCount() >= 5);
		assertTrue("too many bulletins?", appWithAccount.getStore().getBulletinCount() <= 15);
		search(appWithAccount, b.get("author"), andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, b.get(""), andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals(10, store.findFolder(store.getSearchFolderName()).getBulletinCount());

		TRACE_END();
	}
	
	public void testSearchWithXFormsData() throws Exception
	{
		TRACE_BEGIN("testSearchWithXFormsData");
		ClientBulletinStore store = appWithAccount.getStore();
		assertNull("Search results already exists?", store.findFolder(store.getSearchFolderName()));

		appWithAccount.loadSampleDataWithXForms();
		appWithAccount.getLocalization().setCurrentLanguageCode(MiniLocalization.ENGLISH);

		
		Bulletin b = store.getBulletinRevision((UniversalId)(new Vector(store.getAllBulletinLeafUids()).get(0)));
		String andKeyword = "and";
		String orKeyword = "or";
		search(appWithAccount, b.get("title"), andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertNotNull("Search results should have been created", store.getSearchFolderName());

		search(appWithAccount, "--not in any bulletin--", andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals("search should clear results folder", 0, store.findFolder(store.getSearchFolderName()).getBulletinCount());

		assertTrue("not enough bulletins?", appWithAccount.getStore().getBulletinCount() >= 5);
		assertTrue("too many bulletins?", appWithAccount.getStore().getBulletinCount() <= 15);
		String uniqueAuthor = b.get("author");
		search(appWithAccount, uniqueAuthor, andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, "xforms Name Field", andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, "", andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals(11, store.findFolder(store.getSearchFolderName()).getBulletinCount());

		TRACE_END();
	}

	public void testSkipDiscardedBulletins() throws Exception
	{
		TRACE_BEGIN("testSkipDiscardedBulletins");
		appWithAccount.getLocalization().setCurrentLanguageCode(MiniLocalization.ENGLISH);

		Bulletin b1 = appWithAccount.createBulletin();
		String originalString = "baggins";
		String commonString = "hobbit";
		
		b1.set(Bulletin.TAGPRIVATEINFO, originalString);
		b1.set(Bulletin.TAGKEYWORDS, commonString);
		appWithAccount.getStore().saveBulletin(b1);
		appWithAccount.getStore().ensureBulletinIsInFolder(appWithAccount.getFolderDiscarded(), b1.getUniversalId());
		
		String andKeyword = "and";
		String orKeyword = "or";
		SearchParser parser = new SearchParser(andKeyword, orKeyword);
		SearchTreeNode searchNode = parser.parseJustAmazonValueForTesting(originalString);
		MiniFieldSpec[] noSpecs = new MiniFieldSpec[0];
		SortableBulletinList result = appWithAccount.search(searchNode, noSpecs, noSpecs, false, false, new NullProgressMeter());
		assertEquals("found in discarded?", 0, result.size());
		TRACE_END();
	}

	public void testSearchOlderVersionsAndFinalVersions() throws Exception
	{
		TRACE_BEGIN("testSearchOlderVersionsAndFinalVersions");
		appWithAccount.getLocalization().setCurrentLanguageCode(MiniLocalization.ENGLISH);

		ClientBulletinStore store = appWithAccount.getStore();
		String andKeyword = "and";
		String orKeyword = "or";
		Bulletin b1 = appWithAccount.createBulletin();
		String originalString = "baggins";
		String commonString = "hobbit";
		
		b1.set(Bulletin.TAGPRIVATEINFO, originalString);
		b1.set(Bulletin.TAGKEYWORDS, commonString);
		b1.setImmutable();
		BulletinFolder newFolder = new BulletinFolder(store, "myFolder");
		appWithAccount.saveBulletin(b1, newFolder);
		assertNull(store.findFolder(store.getSearchFolderName()));
		
		search(appWithAccount, originalString, andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, commonString, andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, "abcdefghijklmnop", andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals(0, store.findFolder(store.getSearchFolderName()).getBulletinCount());

		search(appWithAccount, originalString, andKeyword, orKeyword, SEARCH_FINAL_BULLETIN_REVISION_ONLY);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, commonString, andKeyword, orKeyword, SEARCH_FINAL_BULLETIN_REVISION_ONLY);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, "abcdefghijklmnop", andKeyword, orKeyword, SEARCH_FINAL_BULLETIN_REVISION_ONLY);
		assertEquals(0, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		
		String newString = "bilbo";
		Bulletin b2 = store.createNewDraft(b1, b1.getTopSectionFieldSpecs(), b1.getBottomSectionFieldSpecs());
		b2.set(Bulletin.TAGPRIVATEINFO, newString);
		String publicData2 = "publicData2";
		b2.set(Bulletin.TAGPUBLICINFO, publicData2);
		b2.setImmutable();
		appWithAccount.saveBulletin(b2, newFolder);
		Bulletin b3 = store.createNewDraft(b2, b2.getTopSectionFieldSpecs(), b2.getBottomSectionFieldSpecs());
		b3.set(Bulletin.TAGPUBLICINFO, "");
		appWithAccount.saveBulletin(b3, newFolder);

		search(appWithAccount, newString, andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, originalString, andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, commonString, andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, publicData2, andKeyword, orKeyword, SEARCH_ALL_BULLETIN_REVISIONS);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		
		search(appWithAccount, newString, andKeyword, orKeyword, SEARCH_FINAL_BULLETIN_REVISION_ONLY);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, originalString, andKeyword, orKeyword, SEARCH_FINAL_BULLETIN_REVISION_ONLY);
		assertEquals(0, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, commonString, andKeyword, orKeyword, SEARCH_FINAL_BULLETIN_REVISION_ONLY);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		search(appWithAccount, publicData2, andKeyword, orKeyword, SEARCH_FINAL_BULLETIN_REVISION_ONLY);
		assertEquals(0, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		
		TRACE_END();
	}
	
	
	public void testFindBulletinInAllFolders() throws Exception
	{
		TRACE_BEGIN("testFindBulletinInAllFolders");
		MockMartusApp app = MockMartusApp.create(getName());
		Bulletin b1 = app.createBulletin();
		Bulletin b2 = app.createBulletin();
		app.getStore().saveBulletin(b1);
		app.getStore().saveBulletin(b2);

		assertEquals("Found the bulletin already in a folder?", 0, app.findBulletinInAllVisibleFolders(b1).size());
		BulletinFolder f1 = app.createUniqueFolder("testFolder");
		BulletinFolder f2 = app.createUniqueFolder("testFolder");
		BulletinFolder f3 = app.createUniqueFolder("testFolder");
		BulletinFolder f4 = app.getFolderDraftOutbox();
		f1.add(b1);
		f2.add(b2);
		f3.add(b1);
		f3.add(b2);
		f4.add(b2);
		BulletinFolder discarded = app.getFolderDiscarded();
		discarded.add(b2);

		Vector v1 = app.findBulletinInAllVisibleFolders(b1);
		Vector v2 = app.findBulletinInAllVisibleFolders(b2);
		assertEquals("Wrong # of folders for b1?", 2, v1.size());
		assertEquals("Wrong # of folders for b2?", 3, v2.size());
		assertTrue("Doesn't contain f1 for bulletin b1?", v1.contains(f1));
		assertEquals("Does contain f2 for bulletin b1?", false, v1.contains(f2));
		assertTrue("Doesn't contain f3 for bulletin b1?", v1.contains(f3));
		assertEquals("Does contain Discarded for bulletin b1?",false, v1.contains(discarded));

		assertEquals("Does contain f1 for bulletin b2?", false, v2.contains(f1));
		assertTrue("Doesn't contain f2 for bulletin b2?", v2.contains(f2));
		assertTrue("Doesn't contain f3 for bulletin b2?", v2.contains(f3));
		assertTrue("Doesn't contain Discarded for bulletin b2?", v2.contains(discarded));

		app.deleteAllFiles();
		TRACE_END();
	}

	public void testGetPublicCodeFromAccount() throws Exception
	{
		MockMartusSecurity security = MockMartusSecurity.createClient();
		String publicKeyString = security.getPublicKeyString();
		String publicCode = MartusCrypto.computePublicCode(publicKeyString);
		assertEquals("wrong code?", "71887634433124687372", publicCode);
	}

	public void testgetHexDigest() throws Exception
	{
		
		byte[] completelyNegativeString;
		completelyNegativeString = new byte[20];
		Arrays.fill(completelyNegativeString,(byte)0xff);
		String digest = MartusUtilities.byteArrayToHexString(completelyNegativeString);
		assertEquals("should still be 40 char's long", 40, digest.length());
		assertEquals("any normal string should return a digest of 40 characters", 40, MartusCrypto.getHexDigest("hi1234fdsfjlk").length());
		
	}
	public void testGetFileLength() throws Exception
	{
		class MockFile extends File
		{
			MockFile()
			{
				super(".");
			}

			public long length()
			{
				return mockLength;
			}


			long mockLength;
		}

		MockFile mockFile = new MockFile();
		final int normalLength = 555;
		mockFile.mockLength = normalLength;
		assertEquals(normalLength, MartusUtilities.getCappedFileLength(mockFile));

		mockFile.mockLength = 10L *1024*1024*1024;
		try
		{
			MartusUtilities.getCappedFileLength(mockFile);
			fail("Should have thrown too large for big number");
		}
		catch(MartusUtilities.FileTooLargeException ignoreExpectedException)
		{
		}


		mockFile.mockLength = -255;
		try
		{
			MartusUtilities.getCappedFileLength(mockFile);
			fail("Should have thrown too large for negative number");
		}
		catch(MartusUtilities.FileTooLargeException ignoreExpectedException)
		{
		}

	}

	public void testEncryptPublicData() throws Exception
	{
		TRACE_BEGIN("testEncryptPublicData");
		MartusCrypto security = MockMartusSecurity.createClient();
		MartusApp app = MockMartusApp.create(security, getName());
		try
		{
			app.doAfterSigninInitalization();
			app.getStore().createFieldSpecCacheFromDatabase();
			assertEquals("App Not Encypting Public?", true, app.getStore().mustEncryptPublicData());
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(app.getMartusDataRootDirectory());
		}

		TRACE_END();
	}

	public void testExportPublicInfo() throws Exception
	{
		File temp = createTempFile();
		temp.delete();
		appWithAccount.exportPublicInfo(temp);
		assertTrue("not created?", temp.exists());
		UnicodeReader reader = new UnicodeReader(temp);
		String publicKey = reader.readLine();
		String signature = reader.readLine();
		reader.close();
		MartusCrypto security = appWithAccount.getSecurity();
		assertEquals("Public Key wrong?", security.getPublicKeyString(), publicKey);
		MartusUtilities.validatePublicInfo(publicKey, signature, security);
	}

	public void testExtractPublicInfo() throws Exception
	{
		File temp = createTempFile();
		temp.delete();
		appWithAccount.exportPublicInfo(temp);
		String publicKey = appWithAccount.extractPublicInfo(temp);
		assertEquals("Public Key wrong?", appWithAccount.getSecurity().getPublicKeyString(), publicKey);

		UnicodeWriter writer = new UnicodeWriter(temp);
		writer.write("flkdjfl");
		writer.close();
		try
		{
			appWithAccount.extractPublicInfo(temp);
			fail("Should have thrown exception");
		}
		catch (Exception ignoreExpectedException)
		{
		}
	}

	public void testCenter()
	{
		TRACE_BEGIN("testCenter");
		{
			Point upperLeft = Utilities.center(new Dimension(800, 600), new Rectangle(0, 0, 800, 600));
			assertEquals(0, upperLeft.x);
			assertEquals(0, upperLeft.y);
		}
		{
			Point upperLeft = Utilities.center(new Dimension(400, 300), new Rectangle(0, 0, 800, 600));
			assertEquals(200, upperLeft.x);
			assertEquals(150, upperLeft.y);
		}
		TRACE_END();
	}

	public void testFieldLabels()
	{
		TRACE_BEGIN("testFieldLabels");
		assertEquals("Keep ALL Information Private", testAppLocalization.getFieldLabel("allprivate"));
		assertEquals("Author", testAppLocalization.getFieldLabel("author"));
		assertEquals("Organization", testAppLocalization.getFieldLabel("organization"));
		assertEquals("Title", testAppLocalization.getFieldLabel("title"));
		assertEquals("Location", testAppLocalization.getFieldLabel("location"));
		assertEquals("Date of Event", testAppLocalization.getFieldLabel("eventdate"));
		assertEquals("Date Created", testAppLocalization.getFieldLabel("entrydate"));
		assertEquals("Keywords", testAppLocalization.getFieldLabel("keywords"));
		assertEquals("Summary", testAppLocalization.getFieldLabel("summary"));
		assertEquals("Details", testAppLocalization.getFieldLabel("publicinfo"));
		assertEquals("Additional Information", testAppLocalization.getFieldLabel("privateinfo"));
		assertEquals("Language", testAppLocalization.getFieldLabel("language"));

		assertEquals("Keep ALL Information Private", testAppLocalization.getFieldLabel(MartusLocalization.ENGLISH, "allprivate"));
		assertEquals("<Keep ALL Information Private>", testAppLocalization.getFieldLabel("UNKNOWN_LANGUAGE_CODE", "allprivate"));
		
		TRACE_END();
	}

	public void testFolderLabels()
	{
		//assertEquals("Retrieved Bulletins", appWithAccount.getFolderLabel("%RetrievedMyBulletin"));
		//assertEquals("Field Desk Bulletins", appWithAccount.getFolderLabel("%RetrievedFieldOfficeBulletin"));
	}

	public void testLanguageNames()
	{
		TRACE_BEGIN("testLanguageNames");
		assertNotNull(testAppLocalization.getLanguageName("Not a valid code"));
		assertEquals("English", testAppLocalization.getLanguageName("en"));
		assertEquals("Albanian", testAppLocalization.getLanguageName("sq"));
		assertEquals("Arabic", testAppLocalization.getLanguageName("ar"));
		assertEquals("Azerbaijani", testAppLocalization.getLanguageName("az"));
		assertEquals("Bengali, Bangla", testAppLocalization.getLanguageName("bn"));
		assertEquals("Burmese", testAppLocalization.getLanguageName("my"));
		assertEquals("Chinese", testAppLocalization.getLanguageName("zh"));
		assertEquals("Dutch", testAppLocalization.getLanguageName("nl"));
		assertEquals("Esperanto", testAppLocalization.getLanguageName("eo"));
		assertEquals("French", testAppLocalization.getLanguageName("fr"));
		assertEquals("German", testAppLocalization.getLanguageName("de"));
		assertEquals("Gujarati", testAppLocalization.getLanguageName("gu"));
		assertEquals("Hausa", testAppLocalization.getLanguageName("ha"));
		assertEquals("Hebrew", testAppLocalization.getLanguageName("he"));
		assertEquals("Hindi", testAppLocalization.getLanguageName("hi"));
		assertEquals("Hungarian", testAppLocalization.getLanguageName("hu"));
		assertEquals("Italian", testAppLocalization.getLanguageName("it"));
		assertEquals("Japanese", testAppLocalization.getLanguageName("ja"));
		assertEquals("Javanese", testAppLocalization.getLanguageName("jv"));
		assertEquals("Kannada", testAppLocalization.getLanguageName("kn"));
		assertEquals("Korean", testAppLocalization.getLanguageName("ko"));
		assertEquals("Malayalam", testAppLocalization.getLanguageName("ml"));
		assertEquals("Marathi", testAppLocalization.getLanguageName("mr"));
		assertEquals("Oriya", testAppLocalization.getLanguageName("or"));
		assertEquals("Panjabi", testAppLocalization.getLanguageName("pa"));
		assertEquals("Polish", testAppLocalization.getLanguageName("pl"));
		assertEquals("Portuguese", testAppLocalization.getLanguageName("pt"));
		assertEquals("Romanian", testAppLocalization.getLanguageName("ro"));
		assertEquals("Russian", testAppLocalization.getLanguageName("ru"));
		assertEquals("Serbian", testAppLocalization.getLanguageName("sr"));
		assertEquals("Sindhi", testAppLocalization.getLanguageName("sd"));
		assertEquals("Sinhalese", testAppLocalization.getLanguageName("si"));
		assertEquals("Spanish", testAppLocalization.getLanguageName("es"));
		assertEquals("Tamil", testAppLocalization.getLanguageName("ta"));
		assertEquals("Telugu", testAppLocalization.getLanguageName("te"));
		assertEquals("Thai", testAppLocalization.getLanguageName("th"));
		assertEquals("Turkish", testAppLocalization.getLanguageName("tr"));
		assertEquals("Ukranian", testAppLocalization.getLanguageName("uk"));
		assertEquals("Urdu", testAppLocalization.getLanguageName("ur"));
		assertEquals("Vietnamese", testAppLocalization.getLanguageName("vi"));
		TRACE_END();
	}

	public void testGetLanguageNameChoices()
	{
		TRACE_BEGIN("testWindowTitles");
		String[] testLanguageCodes = {"es", "en", "si"};
		ChoiceItem[] languageChoicesTest = testAppLocalization.getLanguageNameChoices(testLanguageCodes);
		for(int i = 0; i < languageChoicesTest.length; ++i)
		{
			String thisCode = languageChoicesTest[i].getCode();
			String expectedString = languageChoicesTest[i].toString();
			assertEquals(expectedString, testAppLocalization.getLanguageName(thisCode));
		}
		TRACE_END();
	}

	public void testWindowTitles()
	{
		TRACE_BEGIN("testWindowTitles");
		assertEquals("Martus Information Management and Data Collection Framework", testAppLocalization.getWindowTitle("main"));
		TRACE_END();
	}

	public void testButtonLabels()
	{
		TRACE_BEGIN("testButtonLabels");
		assertEquals("Help", testAppLocalization.getButtonLabel("Help"));
		TRACE_END();
	}

	public void testMenuLabels()
	{
		TRACE_BEGIN("testMenuLabels");
		assertEquals("File", testAppLocalization.getMenuLabel("file"));
		TRACE_END();
	}

	public void testCurrentLanguage()
	{
		TRACE_BEGIN("testCurrentLanguage");

		assertEquals("en", testAppLocalization.getCurrentLanguageCode());
		assertEquals("MartusHelp-en.txt", appWithAccount.getHelpFilename("en"));
		testAppLocalization.setCurrentLanguageCode("es");
		assertEquals("es", testAppLocalization.getCurrentLanguageCode());
		assertEquals("MartusHelp-es.txt", appWithAccount.getHelpFilename("es"));
		char iWithAccentInUtf8 = 237;
		char[] titleInSpanish = {'T', iWithAccentInUtf8, 't', 'u', 'l', 'o'};
		assertEquals(new String(titleInSpanish), testAppLocalization.getFieldLabel("title"));
		testAppLocalization.setCurrentLanguageCode("en");
		TRACE_END();
	}


	public void testDateConvert()
	{
		TRACE_BEGIN("testDateConvert");
		assertEquals("12/13/1987", testAppLocalization.convertStoredDateToDisplay("1987-12-13"));
		assertEquals("", testAppLocalization.convertStoredDateToDisplay("abc"));
		assertEquals("", testAppLocalization.convertStoredDateToDisplay("-123-01-03"));
		assertEquals("", testAppLocalization.convertStoredDateToDisplay("1987-00-13"));
		assertEquals("", testAppLocalization.convertStoredDateToDisplay("1987-13-13"));
		assertEquals("", testAppLocalization.convertStoredDateToDisplay("1987-10-00"));
		assertEquals("", testAppLocalization.convertStoredDateToDisplay("1987-02-32"));
		TRACE_END();
	}
	
	public void testDateSlashSeparatedConvertReverseIfNecessary()
	{
		TRACE_BEGIN("testDateSlashSeparatedConvertReverseIfNecessary");
		assertEquals("12/13/1987", testAppLocalization.convertStoredDateToDisplay("1987-12-13"));
		assertEquals("03/01/2004", testAppLocalization.getViewableDateRange("2004-03-01,20040301+0"));
		assertEquals("01/07/2004 - 07/03/2004", testAppLocalization.getViewableDateRange("2004-01-07,20040107+178"));
		
		LanguageOptions.setDirectionRightToLeft();
		assertEquals("1987/13/12", testAppLocalization.convertStoredDateToDisplay("1987-12-13"));
		assertEquals("2004/01/03", testAppLocalization.getViewableDateRange("2004-03-01,20040301+0"));
		assertEquals("2004/03/07 - 2004/07/01", testAppLocalization.getViewableDateRange("2004-01-07,20040107+178"));
		TRACE_END();
	}

	public void testDateDotSeparatedConvertReverseIfNecessary()
	{
		TRACE_BEGIN("testDateDotSeparatedConvertReverseIfNecessary");
		testAppLocalization.setCurrentDateFormatCode(DMY_DOT);
		assertEquals("13.12.1987", testAppLocalization.convertStoredDateToDisplay("1987-12-13"));
		assertEquals("01.03.2004", testAppLocalization.getViewableDateRange("2004-03-01,20040301+0"));
		assertEquals("07.01.2004 - 03.07.2004", testAppLocalization.getViewableDateRange("2004-01-07,20040107+178"));
		
		LanguageOptions.setDirectionRightToLeft();
		assertEquals("1987.12.13", testAppLocalization.convertStoredDateToDisplay("1987-12-13"));
		assertEquals("2004.03.01", testAppLocalization.getViewableDateRange("2004-03-01,20040301+0"));
		
		//RtoL languages doesn't reverse the date when mixed strings of RtoL and LtoR when the LtoR text has dot's contained within for some strange reason
		assertEquals("2004.07.03 - 2004.01.07", testAppLocalization.getViewableDateRange("2004-01-07,20040107+178"));
		
		TRACE_END();
	}

	
	public void testCurrentDateFormatCode()
	{
		TRACE_BEGIN("testCurrentDateFormatCode");
		
		assertEquals("MM/dd/yyyy", testAppLocalization.getCurrentDateFormatCode());
		testAppLocalization.setCurrentDateFormatCode("dd.MM.yyyy");
		assertEquals("dd.MM.yyyy", testAppLocalization.getCurrentDateFormatCode());
		testAppLocalization.setCurrentDateFormatCode("MM/dd/yyyy");
		assertEquals("MM/dd/yyyy", testAppLocalization.getCurrentDateFormatCode());
		TRACE_END();
	}

	public void testMonthLabels()
	{
		TRACE_BEGIN("testMonthLabels");

		assertEquals("Mar", testAppLocalization.getMonthLabel("mar"));
		assertEquals("Mar", testAppLocalization.getMonthLabel("Thai3"));
		assertEquals("Khordad", testAppLocalization.getMonthLabel("Persian3"));
		String[] months = testAppLocalization.getMonthLabels();
		assertEquals("Jan", months[0]);
		testAppLocalization.setCurrentLanguageCode("es");
		months = testAppLocalization.getMonthLabels();
		assertEquals("Ene", months[0]);
		testAppLocalization.setCurrentLanguageCode("en");

		TRACE_END();
	}

	public void testStatusLabels()
	{
		TRACE_BEGIN("testStatusLabels");
		assertEquals("Draft", testAppLocalization.getStatusLabel(Bulletin.STATUSMUTABLE));
		assertEquals("Sealed", testAppLocalization.getStatusLabel(Bulletin.STATUSIMMUTABLE));
		TRACE_END();
	}

	public void testCreateFolders() throws Exception
	{
		TRACE_BEGIN("testCreateFolders");
		final int MAXFOLDERS = 10;
		appWithAccount.setMaxNewFolders(MAXFOLDERS);
		String baseName = "testing";
		assertNotNull("New Folder is null?", appWithAccount.createUniqueFolder(baseName));
		assertNotNull("Could not find first new folder", appWithAccount.store.findFolder(baseName));

		for(int i = 1; i < MAXFOLDERS; ++i)
		{
			assertNotNull("Folder"+i+" is null?", appWithAccount.createUniqueFolder(baseName));
			assertNotNull("Could not find new folder"+i, appWithAccount.store.findFolder(baseName+i));
		}
		assertNull("Max Folders reached, why is this not null?", appWithAccount.createUniqueFolder(baseName));
		assertNull("Found this folder"+MAXFOLDERS, appWithAccount.store.findFolder(baseName+MAXFOLDERS));
		TRACE_END();
	}

	public void testFormatPublicCode() throws Exception
	{
		TRACE_BEGIN("testCreateFolders");
		String clientId = appWithAccount.getAccountId();
		assertNotNull("clientId Null?", clientId);
		String publicCode = MartusCrypto.computePublicCode(clientId);
		assertNotNull("publicCode Null?", publicCode);
		String formattedCode = MartusCrypto.formatPublicCode(publicCode);
		assertNotEquals("formatted code is the same as the public code?", formattedCode, publicCode);
		assertEquals("Not formatted correctly", "1234.5678.9012.3456", MartusCrypto.formatPublicCode("1234567890123456"));
		String formattedCode2 = MartusCrypto.computeFormattedPublicCode(clientId);
		assertEquals("Not formatted the same", formattedCode, formattedCode2);
		TRACE_END();

	}

	public void testFormatPublicCode40() throws Exception
	{
		TRACE_BEGIN("testFormatPublicCode40");
		String clientId = appWithAccount.getAccountId();
		assertNotNull("clientId Null?", clientId);
		String publicCode = MartusCrypto.computePublicCode40(clientId);
		assertNotNull("publicCode Null?", publicCode);
		assertEquals("New Public codes should be 40 characters", 40, publicCode.length());
		assertEquals("Public Code not correct?","2148157669619698320619603004127606269691", publicCode);
		DammCheckDigitAlgorithm dammCheck = new DammCheckDigitAlgorithm();
		assertTrue("Public Code failed Damm Check?", dammCheck.isTokenValid(publicCode));
		String formattedCode = MartusCrypto.formatPublicCode(publicCode);
		assertNotEquals("formatted code is the same as the public code?", formattedCode, publicCode);
		assertEquals("Not formatted correctly", "1234.5678.9012.3456.1234.5678.9012.3456", MartusCrypto.formatPublicCode("12345678901234561234567890123456"));
		String formattedCode2 = MartusCrypto.computeFormattedPublicCode40(clientId);
		assertEquals("Not formatted the same", formattedCode, formattedCode2);
		TRACE_END();
	}

	public void testRepairOrphans() throws Exception
	{
		assertEquals("already have orphans?", 0, appWithAccount.repairOrphans());		
		assertNull("Orphan Folder exists?", appWithAccount.getStore().findFolder(ClientBulletinStore.RECOVERED_BULLETIN_FOLDER));
		int draftCount = appWithAccount.getStore().getFolderDraftOutbox().getBulletinCount();
		assertEquals("is draft outbox folder empty?", 0,draftCount);
		
		Bulletin b1 = appWithAccount.createBulletin();
		b1.setMutable();
		appWithAccount.getStore().saveBulletin(b1);
		assertEquals("didn't find the orphan?", 1, appWithAccount.repairOrphans());
		draftCount = appWithAccount.getStore().getFolderDraftOutbox().getBulletinCount();
		assertEquals("is draft outbox folder not empty?", 1, draftCount);				
		assertEquals("didn't fix the orphan?", 0, appWithAccount.repairOrphans());		

		BulletinFolder orphanFolder = appWithAccount.getStore().findFolder(ClientBulletinStore.RECOVERED_BULLETIN_FOLDER);
		assertEquals("where did the orphan go?", 1, orphanFolder.getBulletinCount());
		assertTrue("wrong bulletin?", orphanFolder.contains(b1));

		appWithAccount.loadFolders();
		BulletinFolder orphanFolder2 = appWithAccount.getStore().findFolder(ClientBulletinStore.RECOVERED_BULLETIN_FOLDER);
		assertEquals("forgot to save folders?", 1, orphanFolder2.getBulletinCount());
	}

	public void testSetBulletinHQKeyWhenDefaultIsSet() throws Exception
	{
		Bulletin b0 = appWithAccount.createBulletin();
		assertEquals("No keys should be configured", 0, b0.getAuthorizedToReadKeys().size());

		String sampleHQKey1 = "abc123";
		String sampleLabel1 = "Fred";
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(sampleHQKey1, sampleLabel1);
		
		key1.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
		keys.add(key1);
		appWithAccount.setAndSaveHQKeys(keys,keys);
		HeadquartersKeys returnedKeys = appWithAccount.getAllHQKeys();
		HeadquartersKey returnedKey1 = returnedKeys.get(0);
		assertEquals("Public Key not set?", sampleHQKey1, returnedKey1.getPublicKey());
		assertEquals("Label not set?", sampleLabel1, returnedKey1.getLabel());
		assertTrue("Should be able to SendTo", returnedKey1.getCanSendTo());
		HeadquartersKeys returnedDefaultKeys = appWithAccount.getDefaultHQKeys();
		HeadquartersKey returnedKey2 = returnedDefaultKeys.get(0);
		assertEquals("Public Key not set?", sampleHQKey1, returnedKey2.getPublicKey());
		assertEquals("Label not set?", sampleLabel1, returnedKey2.getLabel());
		assertEquals("Verifiction not set?", ContactKey.VERIFIED_ENTERED_20_DIGITS, returnedKey2.getVerificationStatus());
		assertTrue("Can Receive From not set?", returnedKey2.getCanReceiveFrom());
		assertTrue("Can Send To not set?", returnedKey2.getCanSendTo());
		
		Bulletin b1 = appWithAccount.createBulletin();
		assertEquals("HQ key not set?", 1, b1.getAuthorizedToReadKeys().size());
		assertEquals("Key not set?", sampleHQKey1, (b1.getAuthorizedToReadKeys().get(0)).getPublicKey());
		assertEquals("Label not set?", sampleLabel1, (b1.getAuthorizedToReadKeys().get(0)).getLabel());
	}

	public void testGetBulletinHQLabel() throws Exception
	{
		String sampleHQKey1 = "abc123";
		String sampleLabel1 = "Fred";
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(sampleHQKey1, sampleLabel1);
		keys.add(key1);
		appWithAccount.setAndSaveHQKeys(keys, keys);
		assertEquals("Label not the same?", sampleLabel1, appWithAccount.getHQLabelIfPresent(key1));
		HeadquartersKey missingKey = new HeadquartersKey("public key", "some label");
		assertEquals("unknown Key not configured?", missingKey.getFormattedPublicCode40()+" <field:HQNotConfigured>", appWithAccount.getHQLabelIfPresent(missingKey));
	}
	
	
	
	private void search(MartusApp app, String searchFor, String andKeyword, String orKeyword, boolean searchFinalBulletinVersionsOnly) throws Exception
	{
		SearchParser parser = new SearchParser(andKeyword, orKeyword);
		SearchTreeNode searchNode = parser.parseJustAmazonValueForTesting(searchFor);
		MiniFieldSpec[] noSpecs = new MiniFieldSpec[0];
		app.updateSearchFolder(app.search(searchNode, noSpecs, noSpecs, searchFinalBulletinVersionsOnly, false, new NullProgressMeter()));		
	}


	
	private MockMartusSecurity mockSecurityForApp;

	MartusLocalization testAppLocalization;
	private MockMartusApp appWithAccount;
	
	static final String NONEXISTENT_FILENAME = "someFileWhichDoesnotExist.key";
	
	static final boolean SEARCH_ALL_BULLETIN_REVISIONS = false; 
	static final boolean SEARCH_FINAL_BULLETIN_REVISION_ONLY = true; 

	static final String[] noEnglishStrings = {};

	static final String userName = "testuser";
	static final String userName2 = "testuse!";
	static final char[] userPassword = "12345".toCharArray();
	static final char[] userPassword2 = "12347".toCharArray();
	static final String textInsideYYQuickStartGuideMLPFile = "fake pdf file for testing";
	static final String textInsideUpdatedYYQuickStartGuideMLPFile = "updated fake pdf file for testing.";
	static final String textInsideYYReadmeMLPFile = "readme yy";
	static final String textInsideUpdatedYYReadmeMLPFile = "updated yy readme";

	static final String MDY_SLASH = "MM/dd/yyyy";
	static final String DMY_SLASH = "dd/MM/yyyy";
	static final String DMY_DOT = "dd.MM.yyyy";

	static final String hqKey1 = "HQ key 1";
	static final String hqKeylabel1 = "HQ label 1";
	static final String hqKey2 = "HQ key 2";
	static final String hqKeylabel2 = "HQ label 2";
	static final String fdKey1 = "FD key 1";
	static final String fdKeyLabel1 = "FD label 1";
	static final String fdKey2 = "FD key 2";
	static final String fdKeyLabel2 = "FD label 2";
}
