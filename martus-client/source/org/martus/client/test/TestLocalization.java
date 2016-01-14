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

import java.awt.ComponentOrientation;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;
import java.util.zip.ZipFile;

import javax.swing.SwingConstants;

import org.martus.client.swingui.EnglishStrings;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiSession;
import org.martus.clientside.CurrentUiState;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MartusUtilities;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.jarverifier.JarVerifier;
import org.martus.swing.UiLanguageDirection;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeStringWriter;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;
import org.martus.util.language.LanguageOptions;


public class TestLocalization extends TestCaseEnhanced
{
    public TestLocalization(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		testTranslationDirectory = createTempDirectory();
		bd = new MartusLocalization(testTranslationDirectory, EnglishStrings.strings);
 	}
	
	protected void tearDown() throws Exception
	{
		super.tearDown();
		DirectoryUtils.deleteEntireDirectoryTree(testTranslationDirectory);
		assertFalse("Translation directory still exists?", testTranslationDirectory.exists());
	}
	
	public void testCalendars() throws Exception
	{
		final String JAVA_CHRONOLOGY_TYPE_CODE_FOR_GREGORIAN = "iso8601";
		final String JAVA_CHRONOLOGY_TYPE_CODE_FOR_THAI = "buddhist";
		final String JAVA_CHRONOLOGY_TYPE_CODE_FOR_PERSIAN = "islamic-umalqura";

		assertEquals(JAVA_CHRONOLOGY_TYPE_CODE_FOR_GREGORIAN, MartusLocalization.getChronology(MartusLocalization.GREGORIAN_SYSTEM).getCalendarType());
		assertEquals(JAVA_CHRONOLOGY_TYPE_CODE_FOR_THAI, MartusLocalization.getChronology(MartusLocalization.THAI_SYSTEM).getCalendarType());
		assertEquals(JAVA_CHRONOLOGY_TYPE_CODE_FOR_PERSIAN, MartusLocalization.getChronology(MartusLocalization.PERSIAN_SYSTEM).getCalendarType());
		assertEquals(JAVA_CHRONOLOGY_TYPE_CODE_FOR_PERSIAN, MartusLocalization.getChronology(MartusLocalization.AFGHAN_SYSTEM).getCalendarType());
		
		String storedDate = "2014-10-06";
		assertEquals("10/06/2014", bd.convertStoredDateToDisplay(storedDate));
		
		bd.setCurrentCalendarSystem(MartusLocalization.THAI_SYSTEM);
		assertEquals("10/06/2557", bd.convertStoredDateToDisplay(storedDate));
		assertEquals(JAVA_CHRONOLOGY_TYPE_CODE_FOR_THAI, bd.getCurrentChronology().getCalendarType());
	}
	
	public void testEnglishStringsDontStartWithAngleBrackets() throws Exception
	{
		for(int i=0; i < EnglishStrings.strings.length; ++i)
		{
			String entry = EnglishStrings.strings[i];
			String value = entry.substring(entry.indexOf("=") + 1);
			assertFalse("ERROR: English string can't start with < but does: " + entry, 
						value.startsWith("<"));
		}
	}
	
	public void testConstructor() throws Exception
	{
		assertEquals("en", bd.getCurrentLanguageCode());
		assertEquals("MM/dd/yyyy", bd.getCurrentDateTemplate());
		assertEquals("MM/dd/yyyy", bd.getCurrentDateFormatCode());
		assertEquals(MiniLocalization.GREGORIAN_SYSTEM, bd.getCurrentCalendarSystem());
	}
	
	public void testNonAsciiEnglishTranslations() throws Exception
	{
		String[] strings = EnglishStrings.strings;
		for(int i=0; i < strings.length; ++i)
		{
			String thisString = EnglishStrings.strings[i];
			char[] mtfEntry = thisString.toCharArray();
			for(int c = 0; c < mtfEntry.length - 1; ++c)
			{
				if(mtfEntry[c] == '?')
					if(Character.isLetter(mtfEntry[c+1]))
						System.out.println("Likely non-ASCII character in: " + thisString);
			}
		}
	}
	
	public void testDefaultDateFormats()
	{
		verifyDefaultDateFormat(MiniLocalization.ENGLISH, "mdy", '/');
		verifyDefaultDateFormat(MiniLocalization.SPANISH, "dmy", '/');
		verifyDefaultDateFormat(MiniLocalization.RUSSIAN, "dmy", '.');
		verifyDefaultDateFormat(MiniLocalization.THAI, "dmy", '/');
		verifyDefaultDateFormat(MiniLocalization.ARABIC, "dmy", '/');
		verifyDefaultDateFormat(MiniLocalization.FARSI, "dmy", '/');
		verifyDefaultDateFormat("UNKNOWN", "mdy", '/');
	}
	
	private void verifyDefaultDateFormat(String languageCode, String mdyOrder, char delimiter)
	{
		MiniLocalization localization = new MiniLocalization();
		localization.setLanguageSettingsProvider(new CurrentUiState());
		localization.setCurrentLanguageCode(languageCode);
		localization.setDateFormatFromLanguage();
		assertEquals("wrong mdy order for " + languageCode + "? ", mdyOrder, localization.getMdyOrder());
		assertEquals("wrong delimiter for " + languageCode + "? ", delimiter, localization.getDateDelimiter());
	}
	
	public void testIsLanguageFile()
	{
		assertTrue(MtfAwareLocalization.isLanguageFile("Martus-en.mtf"));
		assertTrue(MtfAwareLocalization.isLanguageFile("Martus-ab.mtf"));
		assertTrue(MtfAwareLocalization.isLanguageFile("martus-ab.mtf"));
		assertTrue(MtfAwareLocalization.isLanguageFile("MARTUS-ab.MTF"));
		assertTrue(MtfAwareLocalization.isLanguageFile("Martus-en.mlp"));
		assertTrue(MtfAwareLocalization.isLanguageFile("Martus-ab.mlp"));
		assertTrue(MtfAwareLocalization.isLanguageFile("martus-ab.mlp"));
		assertTrue(MtfAwareLocalization.isLanguageFile("MARTUS-ab.MLP"));
	}
	
	public void testValidLanguageCodes()
	{
		assertFalse("null should not be recognized", MtfAwareLocalization.isRecognizedLanguage(null));
		assertTrue("English should be recognized", MtfAwareLocalization.isRecognizedLanguage(MiniLocalization.ENGLISH));
		assertTrue("Spanish should be recognized", MtfAwareLocalization.isRecognizedLanguage(MiniLocalization.SPANISH));
		assertTrue("Russian should be recognized", MtfAwareLocalization.isRecognizedLanguage(MiniLocalization.RUSSIAN));
		assertFalse("Unknown should not be recognized", MtfAwareLocalization.isRecognizedLanguage("XX"));
	}
	
	public static String test = "test";
	public static String button = "button:"+test+"=";
	public static class EnglishTestStrings
	{
		public static String strings[] = {button+"Test Button English"};
	}

	public void testIsRightToLeftLanguage() throws Exception
	{
		File tmpDir = createTempDirectory();
		tmpDir.deleteOnExit();
		
		MartusLocalization directionalLanguages = new MartusLocalization(tmpDir, EnglishTestStrings.strings);
		directionalLanguages.setLanguageSettingsProvider(new CurrentUiState());
		directionalLanguages.includeOfficialLanguagesOnly = false;
		directionalLanguages.setCurrentLanguageCode("en");
		assertFalse("English is a Left To Right language.", LanguageOptions.isRightToLeftLanguage());
		assertEquals("Components for English should be Left To Right", UiLanguageDirection.getComponentOrientation(), ComponentOrientation.LEFT_TO_RIGHT);
		assertEquals("Horizontal Alignment for English should be Left", UiLanguageDirection.getHorizontalAlignment(), SwingConstants.LEFT);
		assertFalse("English should not require Text Padding", LanguageOptions.needsLanguagePadding());
		assertFalse("We should not be using a Language Pack with English", directionalLanguages.isTranslationInsideMLP());

		File spanish = new File(tmpDir, "Martus-es.mtf");
		spanish.deleteOnExit();
		UnicodeWriter writer = new UnicodeWriter(spanish);
		String spanishButtonText = "My button";
		writer.writeln(button+spanishButtonText);
		writer.close();
		directionalLanguages.setCurrentLanguageCode("es");
		assertEquals("test Button for spanish not correct?", spanishButtonText, directionalLanguages.getButtonLabel(test));
		assertFalse("Spanish should be a Left to Right language.", LanguageOptions.isRightToLeftLanguage());
		assertEquals("Components for Spanish should be Left To Right", UiLanguageDirection.getComponentOrientation(), ComponentOrientation.LEFT_TO_RIGHT);
		assertEquals("Horizontal Alignment for Spanish should be Left", UiLanguageDirection.getHorizontalAlignment(), SwingConstants.LEFT);
		assertFalse("Spanish should not require Text Padding", LanguageOptions.needsLanguagePadding());
		
		String arabicButtonText = "Some other translation";
		File arabic = new File(tmpDir, "Martus-ar.mtf");
		arabic.deleteOnExit();
		writer = new UnicodeWriter(arabic);
		writer.writeln(button+arabicButtonText);
		writer.writeln(MtfAwareLocalization.MTF_RIGHT_TO_LEFT_LANGUAGE_FLAG);
		writer.close();
		directionalLanguages.setCurrentLanguageCode("ar");
		assertEquals("test Button for arabic not correct?", arabicButtonText, directionalLanguages.getButtonLabel(test));
		assertTrue("Arabic should be a Right to Left language.", LanguageOptions.isRightToLeftLanguage());
		assertEquals("Components for Arabic should be Right To Left", UiLanguageDirection.getComponentOrientation(), ComponentOrientation.RIGHT_TO_LEFT);
		assertEquals("Horizontal Alignment for Arabic should be Right", UiLanguageDirection.getHorizontalAlignment(), SwingConstants.RIGHT);
		assertTrue("Arabic should require Text Padding", LanguageOptions.needsLanguagePadding());
		
		String farsiButtonText = "Farsi translation";
		File farsi = new File(tmpDir, "Martus-fa.mtf");
		farsi.deleteOnExit();
		writer = new UnicodeWriter(farsi);
		writer.writeln(button+farsiButtonText);
		writer.writeln(MtfAwareLocalization.MTF_RIGHT_TO_LEFT_LANGUAGE_FLAG);
		writer.close();
		directionalLanguages.setCurrentLanguageCode("fa");
		assertEquals("test Button for farsi not correct?", farsiButtonText, directionalLanguages.getButtonLabel(test));
		assertTrue("Farsi should be a Right to Left language.", LanguageOptions.isRightToLeftLanguage());
		assertEquals("Components for Farsi should be Right To Left", UiLanguageDirection.getComponentOrientation(), ComponentOrientation.RIGHT_TO_LEFT);
		assertEquals("Horizontal Alignment for Farsi should be Right", UiLanguageDirection.getHorizontalAlignment(), SwingConstants.RIGHT);
		assertTrue("Farsi should require Text Padding", LanguageOptions.needsLanguagePadding());
	}

	public void testToFileNameForeignChars()
	{
		String english = "abcdefghijklmnopqrstuvwxyz";
		assertEquals(english.substring(0, 20), MartusUtilities.toFileName(english));
		//TODO add test for russian.
	}
	
	public void testDefaultLanguages()
	{
		ChoiceItem[] languages = bd.getUiLanguages();
		assertTrue("Should have multiple languages", languages.length > 1);
		boolean foundEnglish = doesLanguageExist(bd, MtfAwareLocalization.ENGLISH);
		assertTrue("must have english", foundEnglish);
	}
	
	public void testGetAllEnglishStrings() throws Exception
	{
		MartusLocalization localization = new MartusLocalization(createTempDirectory(), UiSession.getAllEnglishStrings());
		assertEquals("Martus Information Management and Data Collection Framework", localization.getLabel("en", "wintitle", "main"));
		assertEquals("or", localization.getLabel("en", "keyword", "or"));
		assertEquals("-Other-", localization.getLabel("en", "language", "?"));
		assertEquals("Sealed", localization.getLabel("en", "status", "sealed"));
	}
	
	public void testGetTranslationFileAllowUnofficial() throws Exception
	{
		File translationDirectory = createTempDirectory();
		MartusLocalization tmpLocalization = new MartusLocalization(translationDirectory, UiSession.getAllEnglishStrings());
		tmpLocalization.includeOfficialLanguagesOnly = false;
		String languageCode = "ff";
		assertNull("Language doesn't exists should return null", tmpLocalization.getTranslationFile(languageCode));
		File mlpkTranslation = new File(translationDirectory, UiLocalization.getMlpkFilename(languageCode));
		mlpkTranslation.deleteOnExit();
		String data = "test";
		writeDataToFile(mlpkTranslation, data);
		assertEquals("MLP file exists should return it.", mlpkTranslation, tmpLocalization.getTranslationFile(languageCode));
		File mtfTranslation = new File(translationDirectory, UiLocalization.getMtfFilename(languageCode));
		mtfTranslation.deleteOnExit();
		writeDataToFile(mtfTranslation, data);
		assertEquals("MTF file exists should superceed MLP file.", mtfTranslation, tmpLocalization.getTranslationFile(languageCode));
		
	}
	
	public void testGetTranslationFileAllowOfficialOnly() throws Exception
	{
		File translationDirectory = createTempDirectory();
		MartusLocalization tmpLocalization = new MartusLocalization(translationDirectory, EnglishStrings.strings);
		String languageCode = "ff";
		assertNull("Language doesn't exists should return null", tmpLocalization.getTranslationFile(languageCode));
		File mlpkTranslation = new File(translationDirectory, UiLocalization.getMlpkFilename(languageCode));
		mlpkTranslation.deleteOnExit();
		String data = "test";
		writeDataToFile(mlpkTranslation, data);
		assertNull("MLP file exists but isn't signed should return null", tmpLocalization.getTranslationFile(languageCode));
		File mtfTranslation = new File(translationDirectory, UiLocalization.getMtfFilename(languageCode));
		mtfTranslation.deleteOnExit();
		writeDataToFile(mtfTranslation, data);
		assertNull("MTF file exists but we don't allow unofficial translations", tmpLocalization.getTranslationFile(languageCode));
		
	}

	private void writeDataToFile(File mlpkTranslation, String data) throws IOException
	{
		UnicodeWriter out = new UnicodeWriter(mlpkTranslation);
		out.write(data);
		out.close();
	}

	public void testAddedMTFLanguageFile() throws Exception
	{
		File translationDirectory = createTempDirectory();
		MartusLocalization myLocalization = new MartusLocalization(translationDirectory, UiSession.getAllEnglishStrings());
		myLocalization.setLanguageSettingsProvider(new CurrentUiState());
		myLocalization.includeOfficialLanguagesOnly = false;
		assertTrue("Default English should always be trusted.", myLocalization.isOfficialTranslation("en"));

		String someTestLanguageCode = "zz";
		boolean foundSomeTestLanguage = doesLanguageExist(myLocalization, someTestLanguageCode);
		assertFalse("must not have testLanguage yet", foundSomeTestLanguage);
		
		File someTestLanguageFile = new File(translationDirectory,UiLocalization.getMtfFilename(someTestLanguageCode));
		someTestLanguageFile.deleteOnExit();
		String buttonName = "ok";
		String someLanguageTranslationOfOk = "dkjfl";
		writeDataToFile(someTestLanguageFile, "button:"+buttonName+"="+someLanguageTranslationOfOk);
		
		foundSomeTestLanguage = doesLanguageExist(myLocalization, someTestLanguageCode);
		assertTrue("should now have testLanguage", foundSomeTestLanguage);
		assertFalse("An mtf file should always be untrusted", myLocalization.isOfficialTranslation(someTestLanguageCode));
		myLocalization.setCurrentLanguageCode(someTestLanguageCode);
		assertEquals("Incorrect translation", someLanguageTranslationOfOk, myLocalization.getButtonLabel(buttonName));
	}

	public void testAddedUnsignedMTFLanguageFileOfficialOnly() throws Exception
	{
		File translationDirectory = createTempDirectory();
		MartusLocalization myLocalization = new MartusLocalization(translationDirectory, UiSession.getAllEnglishStrings());
		myLocalization.setLanguageSettingsProvider(new CurrentUiState());

		String someTestLanguageCode = "zz";
		
		File someTestLanguageFile = new File(translationDirectory,UiLocalization.getMtfFilename(someTestLanguageCode));
		someTestLanguageFile.deleteOnExit();
		String buttonName = "ok";
		String someLanguageTranslationOfOk = "dkjfl";
		writeDataToFile(someTestLanguageFile, "button:"+buttonName+"="+someLanguageTranslationOfOk);
		
		myLocalization.setCurrentLanguageCode(someTestLanguageCode);
		assertEquals("Incorrect translation", "<OK>", myLocalization.getButtonLabel(buttonName));
	}

	public void testJarVerifier() throws Exception
	{
		
		assertEquals("no file", JarVerifier.ERROR_INVALID_JAR, JarVerifier.verify(new File("nonexistentFile"), false));
		
		File tmpResourceFile = returnTmpFileFromResourceWithDeleteOnExit("Martus-xx-noManifest.mlp");
		assertEquals("no manifest", JarVerifier.ERROR_JAR_NOT_SIGNED, JarVerifier.verify(tmpResourceFile, false));
		tmpResourceFile.delete();
		
		tmpResourceFile = returnTmpFileFromResourceWithDeleteOnExit("Martus-xx-MissingEntry.mlp");
		assertEquals("Missing Entry", JarVerifier.ERROR_MISSING_ENTRIES, JarVerifier.verify(tmpResourceFile, false));
		tmpResourceFile.delete();
		
		tmpResourceFile = returnTmpFileFromResourceWithDeleteOnExit("Martus-xx-ModifiedEntry.mlp");
		assertEquals("Modified Entry", JarVerifier.ERROR_JAR_NOT_SIGNED, JarVerifier.verify(tmpResourceFile, false));
		tmpResourceFile.delete();
		
		tmpResourceFile = returnTmpFileFromResourceWithDeleteOnExit("Martus-xx-notSigned.mlp");
		assertEquals("Not Signed", JarVerifier.ERROR_JAR_NOT_SIGNED, JarVerifier.verify(tmpResourceFile, false));
		tmpResourceFile.delete();
		
		tmpResourceFile = returnTmpFileFromResourceWithDeleteOnExit("Martus-xx-notSealed.mlp");
		assertEquals("Not sealed", JarVerifier.ERROR_JAR_NOT_SEALED, JarVerifier.verify(tmpResourceFile, false));
		tmpResourceFile.delete();
		
		tmpResourceFile = returnTmpFileFromResourceWithDeleteOnExit("Martus-xx.mlp");
		assertEquals("A valid signed jar didn't pass?", JarVerifier.JAR_VERIFIED_TRUE, JarVerifier.verify(tmpResourceFile, false));
		tmpResourceFile.delete();
	}
	
	private File returnTmpFileFromResourceWithDeleteOnExit(String resourceName) throws Exception
	{
		File tmpResourceFile = createTempFile();
		tmpResourceFile.delete();
		copyResourceFileToLocalFile(tmpResourceFile, resourceName);
		tmpResourceFile.deleteOnExit();
		
		return tmpResourceFile;
	}
	
	public void testAddedMLPLanguagePackWhenWeAllowUnofficialTranslations() throws Exception
	{
		File translationDirectory = createTempDirectory();
		MartusLocalization myLocalization = new MartusLocalization(translationDirectory, UiSession.getAllEnglishStrings());
		myLocalization.includeOfficialLanguagesOnly = false;

		String someTestLanguageCode = "xx";
		boolean foundSomeTestLanguage = doesLanguageExist(myLocalization, someTestLanguageCode);
		assertFalse("must not have testLanguage from pack yet", foundSomeTestLanguage);

		File someTestLanguage = new File(translationDirectory,UiLocalization.getMlpkFilename(someTestLanguageCode));
		someTestLanguage.deleteOnExit();
		copyResourceFileToLocalFile(someTestLanguage, "Martus-xx.mlp");
		
		foundSomeTestLanguage = doesLanguageExist(myLocalization, someTestLanguageCode);
		assertTrue("should now have testLanguage", foundSomeTestLanguage);
		myLocalization.setCurrentLanguageCode(someTestLanguageCode);
		assertEquals("Incorrect translation OK from within language pack", "OK", myLocalization.getButtonLabel(EnglishCommonStrings.OK));
		assertEquals("Incorrect translation No from within language pack", "No", myLocalization.getButtonLabel(EnglishCommonStrings.NO));
		assertTrue("A signed MLP file should be trusted", myLocalization.isOfficialTranslation(someTestLanguageCode));
		assertTrue("We should be using a Language Pack", myLocalization.isTranslationInsideMLP());

		File translationDirectory2 = createTempDirectory();
		MartusLocalization myLocalization2 = new MartusLocalization(translationDirectory2, UiSession.getAllEnglishStrings());
		myLocalization2.setLanguageSettingsProvider(new CurrentUiState());
		myLocalization2.includeOfficialLanguagesOnly = false;
		File mlpTestLanguage = new File(translationDirectory2,UiLocalization.getMlpkFilename(someTestLanguageCode));
		mlpTestLanguage.deleteOnExit();
		copyResourceFileToLocalFile(mlpTestLanguage, "Martus-xx-notSigned.mlp");
		foundSomeTestLanguage = doesLanguageExist(myLocalization2, someTestLanguageCode);
		assertTrue("should still have testLanguage even if its not signed.", foundSomeTestLanguage);
		myLocalization2.setCurrentLanguageCode(someTestLanguageCode);
		assertEquals("Incorrect translation OK from within unsigned language pack", "OK", myLocalization2.getButtonLabel(EnglishCommonStrings.OK));
		assertEquals("Incorrect translation No from within unsigned language pack", "No", myLocalization2.getButtonLabel(EnglishCommonStrings.NO));
		assertTrue("We should be still be using a Language Pack", myLocalization2.isTranslationInsideMLP());
		
		assertFalse("A unsigned MLPK file should not be trusted", myLocalization2.isOfficialTranslation(someTestLanguageCode));
		assertFalse("Current translation should not be trusted", myLocalization2.isCurrentTranslationOfficial());
		
		assertFalse("A non existant translation should not be trusted.",myLocalization2.isOfficialTranslation("dx"));
	}

	public void testAddedMLPLanguagePackWhenWeAllowONLYOfficialTranslations() throws Exception
	{
		File translationDirectory = createTempDirectory();
		MartusLocalization myLocalization = new MartusLocalization(translationDirectory, UiSession.getAllEnglishStrings());

		String someTestLanguageCode = "xx";
		boolean foundSomeTestLanguage = doesLanguageExist(myLocalization, someTestLanguageCode);
		assertFalse("must not have testLanguage from pack yet", foundSomeTestLanguage);

		File someTestLanguage = new File(translationDirectory,UiLocalization.getMlpkFilename(someTestLanguageCode));
		someTestLanguage.deleteOnExit();
		copyResourceFileToLocalFile(someTestLanguage, "Martus-xx.mlp");
		ZipFile mlp = new ZipFile(someTestLanguage);
		
		foundSomeTestLanguage = doesLanguageExist(myLocalization, someTestLanguageCode);
		assertTrue("should have testLanguage since it is official", foundSomeTestLanguage);
		myLocalization.setCurrentLanguageCode(someTestLanguageCode);
		assertEquals("Incorrect translation OK from within language pack", "OK", myLocalization.getButtonLabel(EnglishCommonStrings.OK));
		assertEquals("Incorrect translation No from within language pack", "No", myLocalization.getButtonLabel(EnglishCommonStrings.NO));
		assertTrue("A signed MLP file should be trusted", myLocalization.isOfficialTranslation(someTestLanguageCode));
		
		assertEquals("Date of MLP not the correct?", new Date(mlp.getEntry("META-INF").getTime()) , myLocalization.getMlpDate());
		mlp.close();
		someTestLanguage.delete();
		assertFalse(someTestLanguage.exists());
		translationDirectory.delete();
		assertFalse(translationDirectory.exists());

		File translationDirectory2 = createTempDirectory();
		MartusLocalization myLocalization2 = new MartusLocalization(translationDirectory2, EnglishStrings.strings);
		myLocalization2.setLanguageSettingsProvider(new CurrentUiState());
		File someTestLanguage2 = new File(translationDirectory2,UiLocalization.getMlpkFilename(someTestLanguageCode));
		someTestLanguage2.deleteOnExit();
		copyResourceFileToLocalFile(someTestLanguage2, "Martus-xx-notSigned.mlp");
		foundSomeTestLanguage = doesLanguageExist(myLocalization2, someTestLanguageCode);
		assertFalse("should not have testLanguage because its not signed.", foundSomeTestLanguage);
		assertFalse("A unsigned MLP file should not be trusted", myLocalization2.isOfficialTranslation(someTestLanguageCode));
		assertFalse("A non existant translation should not be trusted.",myLocalization2.isOfficialTranslation("dx"));
		myLocalization2.setCurrentLanguageCode(someTestLanguageCode);
		assertFalse("Current translation should be trusted", myLocalization2.isCurrentTranslationOfficial());
		assertFalse("We should not be using a Language Pack", myLocalization2.isTranslationInsideMLP());
		someTestLanguage2.delete();
		assertFalse(someTestLanguage2.exists());
		translationDirectory2.delete();
		assertFalse(translationDirectory2.exists());
	}
	
	public void testDoesTranslationMatchProgramVersion() throws Exception
	{
		MartusLocalization myLocalization = new MartusLocalization(testTranslationDirectory, UiSession.getAllEnglishStrings());
		myLocalization.setCurrentLanguageCode(MartusLocalization.ENGLISH);
		String translationVersion = myLocalization.getTranslationVersionNumber(myLocalization.getCurrentLanguageCode());
		String rawProgramVersion = UiConstants.versionLabel;
		
		String programVersion = myLocalization.extractVersionNumber(rawProgramVersion);
		assertEquals(programVersion, translationVersion);
		
		translationVersion = myLocalization.getTranslationVersionNumber("XY");
		assertEquals(programVersion, translationVersion);

		File translationDirectory = createTempDirectory();
		translationDirectory.deleteOnExit();
		MartusLocalization myLocalization2 = new MartusLocalization(translationDirectory, UiSession.getAllEnglishStrings());
		myLocalization2.setCurrentLanguageCode(MartusLocalization.ENGLISH);
		assertTrue(myLocalization2.doesTranslationVersionMatchProgramVersion(MartusLocalization.ENGLISH, UiConstants.versionLabel));
		assertTrue(myLocalization2.doesTranslationVersionMatchProgramVersion("XY", UiConstants.versionLabel));
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 0.0");
		assertFalse(myLocalization2.doesTranslationVersionMatchProgramVersion("XY", UiConstants.versionLabel));
		assertTrue(myLocalization2.doesTranslationVersionMatchProgramVersion(MartusLocalization.ENGLISH, UiConstants.versionLabel));

		String currentVersion = "Version 8";
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 9");
		assertFalse("Major Only:  Major version changes are not allowed", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 8.1");
		assertFalse("Major Only: Minor version changes are not allowed", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 8.0.1");
		assertTrue("Major Only: Incremental version changes are allowed", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 8");
		assertTrue("Major Only: Same Major allowed", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));

		currentVersion = "Version 8.1";
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 8");
		assertFalse("Translation has Major Only: but Minor of 0 is implied", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 9");
		assertFalse("Translation has Major Only:  Major version changes are not allowed", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));

		currentVersion = "Version 8.0.1";
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 8");
		assertTrue(myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 8.1");
		assertFalse(myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 8.0.2");
		assertTrue(myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));
		
		
		currentVersion = "Version 2.8";
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 3.8");
		assertFalse("Major version changes are not allowed", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 2.9");
		assertFalse("Minor version changes are not allowed", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 2.8.1");
		assertTrue("Incremental version changes are allowed", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));

		currentVersion = "Version 10.2";
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 11.2");
		assertFalse("2 digit Major version changes are not allowed", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 10.3");
		assertFalse("2 digit Minor version changes are not allowed", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));
		myLocalization2.addTranslation("XY", "field:translationVersion=Version 10.2.1");
		assertTrue("2 digit Incremental version changes are allowed", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));

		currentVersion = "Version 0.2";
		myLocalization2.addTranslation("XY", "field:translationVersion=Version");
		assertFalse("No Version Info", myLocalization2.doesTranslationVersionMatchProgramVersion("XY", currentVersion));
		assertFalse("No Version Info At all", myLocalization2.doesTranslationVersionMatchProgramVersion("YY", currentVersion));
		
		DirectoryUtils.deleteEntireDirectoryTree(translationDirectory);
	}
	
	public void testExtractVersion()
	{
		MartusLocalization myLocalization = new MartusLocalization(testTranslationDirectory, UiSession.getAllEnglishStrings());
		String englishVersion = "2.8.1";
		String englishEntireVersion = "Version: " + englishVersion + "Internal";
		String extractedVersion = myLocalization.extractVersionNumber(englishEntireVersion);
		assertEquals(englishVersion, extractedVersion);
		
		char[] thaiVersion = {0x0E40, 0x0E40, 0x0E52, '.', 0x0E58, '.', 0x0E51,  0x0E41, 0x0E41};
		extractedVersion = myLocalization.extractVersionNumber(new String(thaiVersion));
		assertEquals(englishVersion, extractedVersion);

		char[] khmerVersion = {0x17CD, 0x17CD, 0x17E2, '.', 0x17E8, '.', 0x17E1, 0X17CE, 0X17CE};
		extractedVersion = myLocalization.extractVersionNumber(new String(khmerVersion));
		assertEquals(englishVersion, extractedVersion);

	}
	
	public void testHasVersionNumber()
	{
		MartusLocalization myLocalization = new MartusLocalization(testTranslationDirectory, UiSession.getAllEnglishStrings());
		assertTrue(myLocalization.hasVersionNumber("English 2.8"));
		assertTrue(myLocalization.hasVersionNumber("English 2.8 Internal"));
		assertTrue(myLocalization.hasVersionNumber("English 4 Internal"));
		assertFalse(myLocalization.hasVersionNumber("No Version Numbers"));
	}

	private boolean doesLanguageExist(MartusLocalization dbToUse, String languageCode)
	{
		ChoiceItem[] languages = dbToUse.getUiLanguages();
		boolean foundSomeTestLanguage = false;
		for(int i = 0; i < languages.length; ++i)
		{
			String code = languages[i].getCode();
			if(code.equals(languageCode))
				foundSomeTestLanguage = true;
		}
		return foundSomeTestLanguage;
	}

	public void testLanguageCodeFromFilename()
	{
		assertEquals("", MtfAwareLocalization.getLanguageCodeFromFilename("Martus.mtf"));
		assertEquals("", MtfAwareLocalization.getLanguageCodeFromFilename("Martus-es.xyz"));
		assertEquals("es", MtfAwareLocalization.getLanguageCodeFromFilename("Martus-es.mtf"));
		assertEquals("Martus mtf files are not case Sensitive", "es", MtfAwareLocalization.getLanguageCodeFromFilename("martus-es.mtf"));
	}

	public void testTranslations()
	{
		bd.loadTranslationFile("es");

		assertEquals("Print", bd.getLabel("en", "button", "print"));
		assertEquals("No translation found", "<whatever:not in the map>", bd.getLabel("en", "whatever", "not in the map"));

		assertEquals("<category:sillytag>", bd.getLabel("en", "category", "sillytag"));
		assertEquals("<category:sillytag>", bd.getLabel("es", "category", "sillytag"));
		bd.addTranslation("en", "category:sillytag=something");
		assertEquals("<something>", bd.getLabel("es", "category", "sillytag"));
		assertEquals("something", bd.getLabel("en", "category", "sillytag"));
		bd.addTranslation("es", "category:sillytag=es/something");
		assertEquals("es/something", bd.getLabel("es", "category", "sillytag"));

		assertEquals("<Print>", bd.getLabel("xx", "button", "print"));
	}
	
	public void testLoadTranslations() throws Exception
	{
		bd.addTranslation(MtfAwareLocalization.ENGLISH, "a:b=jfjfj");
		bd.addTranslation(MtfAwareLocalization.ENGLISH, "d:e=83838");
		String sampleFileContents = 
				"# This is a comment with =\n" +
				"a:b=c\n" +
				"d:e=f";
		StringInputStreamWithSeek in = new StringInputStreamWithSeek(sampleFileContents);
		bd.loadTranslations("qq", in);
		assertEquals("c", bd.getLabel("qq", "a", "b"));
		assertEquals("f", bd.getLabel("qq", "d", "e"));
	}
	
	public void testExportTranslations() throws Exception
	{
		UnicodeStringWriter writer = UnicodeStringWriter.create();
		bd.exportTranslations("en", UiConstants.versionLabel, writer);
		String result = writer.toString();
		assertEquals("no leading ByteOrderMark?", 0xFEFF, result.charAt(0));
		assertEquals("no leading comment?", 1, result.indexOf("#"));
		String expectedTranslationVersion = "field:translationVersion=";
		assertContains("Translation Version # not exported?", expectedTranslationVersion, result);
	}

	public void testAddTranslation()
	{
		assertEquals("<b:c>", bd.getLabel("xx", "b", "c"));
		bd.addTranslation("en", "b:c=bc");
		assertEquals("<bc>", bd.getLabel("xx", "b", "c"));
		bd.addTranslation("a", "invalid=because-bad-language");
		bd.addTranslation("en", null);
		bd.addTranslation("en", "invalid-because-no-equals");
		bd.addTranslation("en", "b:c=new\\nline");
		assertEquals("new\nline", bd.getLabel("en", "b", "c"));
		
		String badHash = "ffff";
		bd.addTranslation("xx", "-" + badHash + "-b:c=def");
		assertEquals("<def>", bd.getLabel("xx", "b", "c"));
		String goodHash = bd.getHashOfEnglish("b:c");
		String goodMtfEntry = "-" + goodHash + "-b:c=def";
		bd.addTranslation("xx", goodMtfEntry);
		assertEquals("def", bd.getLabel("xx", "b", "c"));
	}

/*TODO: Evaluate whether any of these tests are still useful
 * because they are not already covered elsewhere
 *
	public void testLoadTranslations()
	{
		try
		{
			assertEquals("xx shouldn't exist yet", false, bd.isLanguageLoaded("xx"));
			bd..loadTranslationFile("xx", "@#<>%#$%#");
			assertEquals("xx should exist now", true, bd.isLanguageLoaded("xx"));

			File file = createTempFileFromName("$$$MartusTestLoadTranslations");
			UnicodeWriter writer = new UnicodeWriter(file);
			writer.write("f:g=fg\n");
			writer.write("j:k=jk\n");
			writer.close();
			assertEquals("<h>", bd.getLabel("xx", "f", "g", "h"));
			assertEquals("<l>", bd.getLabel("xx", "j", "k", "l"));
			bd.loadTranslationFile("xx", file.getCanonicalPath());
			assertEquals("fg", bd.getLabel("xx", "f", "g", "h"));
			assertEquals("jk", bd.getLabel("xx", "j", "k", "l"));
		}
		catch (IOException e)
		{
			assertTrue(e.toString(), false);
		}

		file.delete();
	}
*/

	public void testGetAllTranslationStrings() throws Exception
	{
		final String sillyEnglish = "a:b=c";
		final String sillyEsperanto = sillyEnglish + "x";

		Vector strings;
		strings = bd.getAllTranslationStrings("eo");
		assertNotNull("Null vector", strings);

		int count = strings.size();
		assertEquals("Should not contain english silly key yet", false, strings.contains(sillyEnglish));
		assertEquals("Should not contain esperanto silly key yet", false, strings.contains(sillyEsperanto));

		bd.addTranslation("eo", sillyEsperanto);
		strings = bd.getAllTranslationStrings("eo");
		assertEquals("Should not have added a string", count, strings.size());
		assertEquals("Still should not contain english silly key", false, strings.contains(sillyEnglish));
		assertEquals("Still should not contain esperanto silly key", false, strings.contains(sillyEsperanto));

		bd.addTranslation("en", sillyEnglish);
		strings = bd.getAllTranslationStrings("eo");
		assertEquals("Should have added one string", count+1, strings.size());
		assertEquals("But still no esperanto silly key", false, strings.contains(sillyEsperanto));

		final String withNewlines = "d:e=f\ng\nh";
		UiLocalization minimalLocalization = new MartusLocalization(createTempDirectory(), new String[0]);
		minimalLocalization.addTranslation("en", withNewlines);
		assertContains("-25fb-d:e=f\\ng\\nh", minimalLocalization.getAllTranslationStrings("en"));
	}

	static MartusLocalization bd;
	File testTranslationDirectory;
}
