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

package org.martus.clientside;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.crypto.MartusCrypto;
import org.martus.jarverifier.JarVerifier;
import org.martus.util.UnicodeReader;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeekThatClosesZipFile;


abstract public class MtfAwareLocalization extends MiniLocalization
{
	public MtfAwareLocalization(File directoryToUse)
	{
		super();
		directory = directoryToUse;
		includeOfficialLanguagesOnly = true;
	}
	
	abstract public String getProgramVersionLabel();

	/////////////////////////////////////////////////////////////////
	// Text-oriented stuff
	public void setCurrentLanguageCode(String newLanguageCode)
	{
		loadTranslationFile(newLanguageCode);
		super.setCurrentLanguageCode(newLanguageCode);
	}
	
	public String getMtfEntry(String languageCode, String key)
	{
		String value = getLabel(languageCode, key);
		String hash = getHashOfEnglish(key);
		value = value.replaceAll("\\n", "\\\\n");
		return "-" + hash + "-" + key + "=" + value;
	}

	public void addTranslation(String languageCode, String mtfEntryText)
	{
		if(mtfEntryText == null)
			return;
		
		if(mtfEntryText.equals(MTF_RIGHT_TO_LEFT_LANGUAGE_FLAG))
		{
			addRightToLeftLanguage(languageCode);
			return;
		}

		if(mtfEntryText.startsWith(TRANSLATEDBY_PREFIX))
		{
			translatedBy = mtfEntryText;
			return;
		}
		
		if(mtfEntryText.startsWith(MTF_COMMENT_FLAG))
			return;
		
		if(mtfEntryText.indexOf('=') < 0)
			return;
		
		String key = extractKeyFromEntry(mtfEntryText);
		Map availableTranslations = getAvailableTranslations(key);
		if(availableTranslations == null)
		{
			if(!languageCode.equals(ENGLISH))
				return;
			availableTranslations = new TreeMap();
			textResources.put(key, availableTranslations);
		}
		else
		{
			if(languageCode.equals(ENGLISH))
				MartusLogger.log("Warning Language Key already exists:" + key +" ( First Encounter= '"+textResources.get(key).toString() +"', Second Encounter='"+ extractValueFromEntry(mtfEntryText) + "')"); 
		}
		
		String translatedText = extractValueFromEntry(mtfEntryText);
		String hash = extractHashFromMtfEntry(mtfEntryText);
		if(hash != null && !hash.equals(getHashOfEnglish(key)))
			translatedText = formatAsUntranslated(translatedText);
		availableTranslations.put(languageCode, translatedText);
	}
	
	public String extractKeyFromEntry(String mtfEntryText)
	{
		int keyStart = HASH_LENGTH + 2;
		if(!mtfEntryText.startsWith("-"))
			keyStart = 0;
		
		int splitAt = mtfEntryText.indexOf('=', keyStart);
		if(splitAt < 0)
			splitAt = 0;
		return mtfEntryText.substring(keyStart, splitAt);
	}
	
	

	private String extractHashFromMtfEntry(String mtfEntryText)
	{
		if(!mtfEntryText.startsWith("-"))
			return null;
		
		return mtfEntryText.substring(1, HASH_LENGTH + 1);
	}
	
	public boolean loadTranslations(String languageCode, InputStream inputStream)
	{
		translatedBy = null;
		try
		{
			UnicodeReader reader = new UnicodeReader(inputStream);
			while(true)
			{
				String mtfEntry = reader.readLine();
				if(mtfEntry == null)
					break;
				addTranslation(languageCode, mtfEntry);
			}
			reader.close();
		}
		catch (IOException e)
		{
			System.out.println("BulletinDisplay.loadTranslations " + e);
			return false;
		}
		return true;
	}
	
	public String getHashOfEnglish(String key)
	{
		return MartusCrypto.getHexDigest(getLabel(ENGLISH, key)).substring(0,HASH_LENGTH);
	}
	
	public boolean isTranslationInsideMLP()
	{
		return mlpDate != null;
	}
	
	public Date getMlpDate()
	{
		return mlpDate;
	}


	/////////////////////////////////////////////////////////////////
	// File-oriented stuff
	
	public void loadTranslationFile(String languageCode)
	{
		mlpDate = null;
		InputStream transStream = null;
		try
		{
			File translationFile = getTranslationFile(languageCode);
			String mtfFileShortName = getMtfFilename(languageCode);

			if(translationFile == null)
			{
				transStream = getClass().getResourceAsStream(mtfFileShortName);
			}
			else if(isTranslationPackFile(translationFile))
			{
				ZipFile zip = new ZipFile(translationFile);
				ZipEntry zipEntry = zip.getEntry(mtfFileShortName);
				if(zipEntry == null)
				{
					zip.close();
					throw new IOException("Missing zip file entry: " + mtfFileShortName);
				}
				transStream = new ZipEntryInputStreamWithSeekThatClosesZipFile(zip, zipEntry);
				ZipEntry metainf = zip.getEntry("META-INF");
				mlpDate = new Date(metainf.getTime());
			}
			else
			{
				transStream = new FileInputStream(translationFile);
				MartusLogger.log("Loading unofficial MTF: " + translationFile.getAbsolutePath());
			}
			
			if(transStream == null)
				return;
			loadTranslations(languageCode, transStream);
		}
		catch (IOException e)
		{
			MartusLogger.logException(e);
			return;
		}
		finally
		{
			try
			{
				if(transStream != null)
					transStream.close();
			}
			catch(IOException e1)
			{
				e1.printStackTrace();
			}
			
		}
	}
	
	public File getMtfFile(String translationFileLanguageCode)
	{
		return new File(directory, getMtfFilename(translationFileLanguageCode));
	}

	public File getMlpkFile(String translationFileLanguageCode)
	{
		return new File(directory, getMlpkFilename(translationFileLanguageCode));
	}
	
	public static String getLanguageCodeFromFilename(String filename)
	{
		if(!isLanguageFile(filename))
			return "";
	
		int codeStart = filename.indexOf('-') + 1;
		int codeEnd = filename.indexOf('.');
		return filename.substring(codeStart, codeEnd);
	}

	public static boolean isLanguageFile(String filename)
	{
		String filenameLower = filename.toLowerCase();
		String martusLanguageFilePrefixLower = MARTUS_LANGUAGE_FILE_PREFIX.toLowerCase();
		String martusLanguageFileSufixLower = MARTUS_LANGUAGE_FILE_SUFFIX.toLowerCase();
		String martusLanguagePackSufixLower = MARTUS_LANGUAGE_PACK_SUFFIX.toLowerCase();
		return (filenameLower.startsWith(martusLanguageFilePrefixLower) 
				&&(filenameLower.endsWith(martusLanguageFileSufixLower) ||
			       filenameLower.endsWith(martusLanguagePackSufixLower)));
	}

	public static String getMtfFilename(String languageCode)
	{
		return MARTUS_LANGUAGE_FILE_PREFIX + languageCode + MARTUS_LANGUAGE_FILE_SUFFIX;
	}

	public static String getMlpkFilename(String languageCode)
	{
		return MARTUS_LANGUAGE_FILE_PREFIX + languageCode + MARTUS_LANGUAGE_PACK_SUFFIX;
	}

	/////////////////////////////////////////////////////////////////
	// Language-oriented stuff

	public static boolean isRecognizedLanguage(String testLanguageCode)
	{
		for(int i = 0 ; i < ALL_LANGUAGE_CODES.length; ++i)
		{
			if(ALL_LANGUAGE_CODES[i].equals(testLanguageCode))
				return true;
		}
		return false;
	}
	
	public File getTranslationFile(String languageCode)
	{
		if(!includeOfficialLanguagesOnly)
		{
			File mtfFile = new File(directory, getMtfFilename(languageCode));
			if(mtfFile.exists())
				return mtfFile;
		}
		File mlpFile = new File(directory, getMlpkFilename(languageCode));
		if(mlpFile.exists())
		{
			if(includeOfficialLanguagesOnly)
			{ 
				if(!isOfficialMlpTranslation(mlpFile))
					return null;
			}
			return mlpFile;
		}
		return null;
	}
	
	public boolean isTranslationPackFile(File translationFile)
	{
		return (translationFile.getName().endsWith(MARTUS_LANGUAGE_PACK_SUFFIX));
	}
	
	public boolean isOfficialTranslationFile(File translationFile)
	{
		return isOfficialMlpTranslation(translationFile);
	}
	
	public boolean isCurrentTranslationOfficial()
	{
		return isOfficialTranslation(getCurrentLanguageCode());
	}
	
	public boolean doesTranslationVersionMatchProgramVersion(String languageCodeToTest, String rawProgramVersion)
	{
		String[] translationVersion = getTranslationVersionNumber(languageCodeToTest).split("\\.");
		String[] programVersion = extractVersionNumber(rawProgramVersion).split("\\.");

		String majorTranslationVersion = translationVersion[0];
		String majorProgramVersion = programVersion[0];
		if(!majorTranslationVersion.equals(majorProgramVersion))
			return false;

		String minorTranslationVersion = "0";
		String minorProgramVersion = "0";
		if(translationVersion.length > 1)
			minorTranslationVersion = translationVersion[1];
		if(programVersion.length > 1)
			minorProgramVersion = programVersion[1];

		return (minorTranslationVersion.equals(minorProgramVersion));
	}

	public String getTranslationFullVersionInfo()
	{
		return getTranslationFullVersionInfo(getCurrentLanguageCode());
	}
	
	public String getTranslationFullVersionInfo(String candidateLanguageCode)
	{
		String translationVersionInfo = getFieldLabel(candidateLanguageCode,"translationVersion");
		if(!hasVersionNumber(translationVersionInfo))
			translationVersionInfo += addProgramVersionNumber();
		return translationVersionInfo;
	}

	public boolean hasVersionNumber(String translationVersionInfo) 
	{
		return extractVersionNumber(translationVersionInfo).length() > 0;
	}
	
	public String addProgramVersionNumber()
	{
		return " " + getProgramVersionLabel();
	}

	public String getTranslationVersionNumber(String candidateLanguageCode) 
	{
		loadTranslationFile(candidateLanguageCode);
		String translationRawVersion = extractVersionNumber(getTranslationFullVersionInfo(candidateLanguageCode));
		return extractVersionNumber(translationRawVersion);
	}

	
	public String extractVersionNumber(String rawVersion)
	{
		StringBuffer version = new StringBuffer();
		for (int i = 0; i < rawVersion.length(); ++i) 
		{
			char charAt = rawVersion.charAt(i);
			if(Character.isDigit(charAt))
				version.append((new Integer(String.valueOf(charAt))));
			else if(charAt == '.')
				version.append(charAt);
			else if(version.length()>0)
				return version.toString();
		}
		return version.toString();
	}
	
	public boolean isOfficialTranslation(String languageCode)
	{
		File translationFile = getTranslationFile(languageCode);
		if(translationFile == null)
		{
			if(languageCode.equals(ENGLISH))
				return true;
			InputStream internal = getClass().getResourceAsStream(getMtfFilename(languageCode));
			if(internal == null)
				return false;
			return true;
		}
		
		if(isTranslationPackFile(translationFile))
			return isOfficialMlpTranslation(translationFile);

		return false;
	}
	
	public String getTranslatedBy()
	{
		if(translatedBy != null)
			return translatedBy;
		return TRANSLATEDBY_PREFIX;
	}
	
	private boolean isOfficialMlpTranslation(File translationFile)
	{
		return (JarVerifier.verify(translationFile, false) == JarVerifier.JAR_VERIFIED_TRUE);
	}
	
	public void setSpecialZawgyiFlagForReportRunner(boolean doZawgyiConversion)
	{
		reportRunnerNeedsZawgyiConversion = doZawgyiConversion;
	}

	@Override
	public boolean getSpecialZawgyiFlagForReportRunner() 
	{
		return reportRunnerNeedsZawgyiConversion;
	}
	
	public File directory;

	public static final String TRANSLATEDBY_PREFIX = "# Translated by:";
	
	public static final int HASH_LENGTH  = 4;
	public static final String UNUSED_TAG = "";
	public static final String MARTUS_LANGUAGE_FILE_PREFIX = "Martus-";

	public static final String MARTUS_LANGUAGE_FILE_SUFFIX = ".mtf";
	public static final String MARTUS_LANGUAGE_PACK_SUFFIX = ".mlp";
	public static final String MTF_COMMENT_FLAG = "#";
	public static final String MTF_RIGHT_TO_LEFT_LANGUAGE_FLAG = "!right-to-left";
	
	public boolean includeOfficialLanguagesOnly;
	private Date mlpDate;
	private String translatedBy;
	private boolean reportRunnerNeedsZawgyiConversion;
}
