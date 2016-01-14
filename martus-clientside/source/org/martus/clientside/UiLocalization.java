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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.Vector;

import org.martus.common.VersionBuildDate;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.utilities.BurmeseUtilities;
import org.martus.util.UnicodeWriter;


abstract public class UiLocalization extends MtfAwareLocalization
{
    public void exportTranslations(String languageCode, String versionLabel, UnicodeWriter writer)
		throws IOException 
	{
		setCurrentLanguageCode("en");
		String byteOrderMark = new String(new char[] {0xFEFF});
		writer.writeln(byteOrderMark + "# Martus Client Translation File");
		writer.writeln("# Language code:  " + languageCode);
		writer.writeln("# Language name:  " + getLanguageName(languageCode));
		writer.writeln(getTranslatedBy());
		writer.writeln("# Exported date:  " + new Date().toString());
		writer.writeln("# Client version: " + versionLabel);
		writer.writeln("# Client build:   " + VersionBuildDate.getVersionBuildDate());
		writer.writeln("#");
		writer.writeln("# This file MUST be saved in UTF-8 format!");
		writer.writeln("# Lines beginning with # are comments and are ignored by Martus");
		writer.writeln("# Each entry has two lines. The first line is the current English");
		writer.writeln("#    text, and the second line consists of: KEY=VALUE");
		writer.writeln("#    (e.g. -c47a-button:customHelp=<Help> )");
		writer.writeln("# Do not modify any KEY. Do localize every VALUE.");
		writer.writeln("# Each VALUE that needs to be translated has <> around it.");
		writer.writeln("#    As you translate each VALUE, remove its <>");
		writer.writeln("# When a non-English VALUE appears inside <> it is because the English");
		writer.writeln("#    translation has changed. Verify or update the translation to ");
		writer.writeln("#    match the new English text, and remove the <>");
		writer.writeln("# If there is an English VALUE inside <> , it is because it is a field"); 
		writer.writeln("#    added after the last release. Remove the english, insert/update the"); 
		writer.writeln("#    VALUE translation to match the new English text which is in the"); 
		writer.writeln("#    comment line directly above it, and remove the <>");
		writer.writeln("# If you name it Martus-" + languageCode + ".mtf, put the file in your");
		writer.writeln("#     Martus directory, and create a file (which can be empty)");
		writer.writeln("#     called use_unofficial_translations.txt in your Martus directory,");
		writer.writeln("#     then Martus will automatically read it.  You will receive a warning");
		writer.writeln("#     message when you use this file while testing.");
		writer.writeln("# In Martus, to choose a language, pick one from the drop-down list");
		writer.writeln("#    in the lower left-hand corner of the signin screen, or after you");
		writer.writeln("#    are logged in, go to Options/Preferences.");
		writer.writeln("#");
		writer.writeln("#  1.  do NOT translate \"\\n\" (used for new lines)");
		writer.writeln("#      And do NOT put spaces before the  equal sign after field:xxx.");
		writer.writeln("#      [should be \"field:fieldtag=fieldtext\" instead of");
		writer.writeln("#  	   \"field:fieldtag = fieldtext\" (space before the equals sign)]");
		writer.writeln("#  2.  do NOT create any user-entered new-lines/carriage-returns");
		writer.writeln("#      (by using the enter key) in any mtf entry");
		writer.writeln("#      (from the field:xxx to the end of the translated text),");
		writer.writeln("#      since a continuous block of text is needed.");
		writer.writeln("#  3.  in field:VirtualKeyboardKeys, keep the english alphabet, but include any");
		writer.writeln("#      non-english characters at the end of the english alphabet/numbers/special");
		writer.writeln("#      characters (e.g. attach entire Thai alphabet at the end of the line)");
		writer.writeln("#  4.  in CreateCustomFieldsHelp2/3 fields, you can translate tags into foreign");
		writer.writeln("#      characters (but without punctuation or spaces)");
		writer.writeln("#  5.  when there are file or directory names, do not translate them");
		writer.writeln("#      (e.g.  \"acctmap.txt\" and \"packets\"");
		writer.writeln("#      in field:confirmWarnMissingAccountMapFilecause=Warning: acctmap.txt");
		writer.writeln("#      file in your account's packets directory...)");
		writer.writeln("#  6.  do not translate the words \"Martus\" or \"Benetech\" or \"Tor\"");
		writer.writeln("#  7.  Leave standard field tags in English in CreateCustomFieldsHelp1 and");
		writer.writeln("#      CreateCustomFieldsHelp2, but put translation in parentheses after");
		writer.writeln("#      english : e.g.  'author' (translation-of-author from mtf, e.g. autor in spanish), so users know what they refer to");
		writer.writeln("#  8.  Leave field types in English in CreateCustomFieldsHelp2 (e.g. BOOLEAN,");
		writer.writeln("#      DATE), but put translation in parentheses after english, so users know");
		writer.writeln("#      what they refer to."); 
		writer.writeln("#  9.  Leave field types in English in CreateCustomFieldsHelp3 examples");
		writer.writeln("#      (e.g. BOOLEAN, DATE)");
		writer.writeln("# 10.  In CreateCustomFieldsHelp3, do not translate words between angle brackets in the XML for custom fields, such as:"); 
		writer.writeln("#      <Field type='SECTION'>, <Field type='STRING'>, <Field type='BOOLEAN'>, <Field type='DATE'>, ");
		writer.writeln("#      <Field type='DATERANGE'>, <Field type='DROPDOWN'>, <Field type='MULTILINE'> ");
		writer.writeln("#      <Field type='LANGUAGE'>, <Field type='MESSAGE'>, <Field type='GRID'>, ");
		writer.writeln("#      </Field>, <Tag>, </Tag>, <Label>, </Label>,  <Message>, </Message> ");
		writer.writeln("#      <Choices>, </Choices>, <Choice>, </Choice>, <DataSource>, </DataSource>"); 
		writer.writeln("#      <GridFieldTag>, </GridFieldTag>, <GridColumnLabel>, </GridColumnLabel> ");
		writer.writeln("#      <GridSpecDetails>, </GridSpecDetails>, <Column>, </Column>, ");
		writer.writeln("#      <Column type='STRING'>, <Column type='BOOLEAN'>, <Column type='DATE'>,"); 
		writer.writeln("#      <Column type='DATERANGE'>, <Column type='DROPDOWN'> ");
		writer.writeln("#      <KeepWithPrevious/>, <RequiredField/>, <DefaultValue>, </DefaultValue>,");
		writer.writeln("#      <MinimumDate>, </MinimumDate>, <MaximumDate>, </MaximumDate>, <MaximumDate/>.");   
		writer.writeln("#      For Reusable choices sections, translate anything within single quotes '...', but not  <UseReusableChoices code= , </UseReusableChoices>"); 
		writer.writeln("#      <ReusableChoices code= , </ReusableChoices>, label= , <Choice code= . ");
		writer.writeln("# 10b.  In CreateCustomFieldsHelp2, change the \"ddd\" in \"<DefaultValue>ddd</DefaultValue>\" to whatever letter the translation of \"default\" begins with.");  
		writer.writeln("# 10c.  For  CreateCustomFieldsHelp1/2/2b/3, check the User Guide section 10b to see if the text has already been translated and use the same translation for consistency.");
		writer.writeln("# 11.  Do not translate words that are surrounded by #'s, but you may move");
		writer.writeln("#      them around as grammatically appropriate.");
		writer.writeln("#      Example: #TotalNumberOfFilesInBackup#, #Titles#, #FieldLabel#, etc.");  
		writer.writeln("#      as these words will be replaced when the program runs with");
		writer.writeln("#      a particular value.");  
		writer.writeln("#      For Example. #TotalNumberOfFilesInBackup# = '5'");
		writer.writeln("#                   #Titles# = 'A list of bulletin titles'");
		writer.writeln("# 12.  Do not translate the numbers in the field:translationVersion string.");
		writer.writeln("# 13.  Do not translate the numbers in the field:ErrorCustomFields string.");
		writer.writeln("# 14.  For Thai, Afghan and Persian months, please do the following: ");
		writer.writeln("#          - for Thai translation, Thai months should be the same as the translation");
		writer.writeln("#            of default/Gregorian months, and Afghan and Persian dates are the same as in English.");
		writer.writeln("#          - for Afghan or Persian translation, Thai and Afghan and Persian months ");
		writer.writeln("#            should be the same as the translation of default/Gregorian months");
		writer.writeln("#          - for Arabic translation, Thai months should be the same as ");
		writer.writeln("#            the translation of default/Gregorian months, but do an Arabic translation");
		writer.writeln("#            (or transliteration) of Afghan and Persian dates (not the same as in English).");
		writer.writeln("#          - for all other languages, Thai and Afghan and Persian dates can be the same");
		writer.writeln("#            as the translation of default/Gregorian months ");
		writer.writeln("# 15       - We have file filters like 'Martus Report Format (.mrf)'");
		writer.writeln("#            and 'Martus Search Specification (.mss)'.");
		writer.writeln("#            The descriptive names should be translated, but the (.mrf) and (.mss)");
		writer.writeln("#            must not be translated. Similarly for .html, .htm, .mba, .mpi, .xml, .mct, .jpeg, .jpg, and .dat file extensions.");
		if(rightToLeftLanguages.contains(languageCode))
		{
			writer.writeln("# 16.  If you want your language to appear left to right,");
			writer.writeln("#      Comment the next line (by adding a # in front of !right-to-left)");
			writer.writeln("!right-to-left");
		}
		else
		{
			writer.writeln("# 16.  If you want your language to appear right to left,");
			writer.writeln("#      Uncomment the next line (by removing the # in front of #!right-to-left)");
			writer.writeln("#!right-to-left");
		}
		writer.writeln("#");
		
		SortedSet sorted = getAllKeysSorted();
		Iterator it = sorted.iterator();
		while(it.hasNext())
		{
			String key = (String)it.next();
			String mtfEntry = getMtfEntry(languageCode, key);
			String englishMtfEntry = getMtfEntry(ENGLISH, key);
			int keyEnd = mtfEntry.indexOf('=');
			char[] filler = new char[keyEnd];
			Arrays.fill(filler, '_');
			filler[0] = '#';
			String result = new String(filler) + englishMtfEntry.substring(keyEnd);
			
			if(key.equals("field:translationVersion"))
			{
				String englishLanguageName = getLanguageName(languageCode);
				String version = addProgramVersionNumber();
				result = new String(filler) + "=" + englishLanguageName + version;

				String translatedLanguageNameAndVersion = getLabel(languageCode, key);
				if(translatedLanguageNameAndVersion.equals("<English>"))
				{
					translatedLanguageNameAndVersion = englishLanguageName + addProgramVersionNumber();
					translatedLanguageNameAndVersion = formatAsUntranslated(translatedLanguageNameAndVersion);
				}
				else 
				{
					String translatedLanguageName = getLanguageName(languageCode);
					if(!translatedLanguageNameAndVersion.equals(translatedLanguageName + version))
						translatedLanguageNameAndVersion = formatAsUntranslated(translatedLanguageNameAndVersion);
				}	
				mtfEntry = key + "=" + translatedLanguageNameAndVersion;
			}
			
			writer.writeln();
			writer.writeln(result);
			writer.writeln(mtfEntry);
			
		}
	}

	public UiLocalization (File directoryToUse, String[] englishTranslations)
	{
		super(directoryToUse);
		addEnglishTranslations(englishTranslations);
	}

	protected ChoiceItem getLanguageChoiceItem(String filename)
	{
		String code = getLanguageCodeFromFilename(filename);
		String name = getLabel(ENGLISH, "language", code);
		name = BurmeseUtilities.getStorable(name);
		return new ChoiceItem(code, name);
	}

	public ChoiceItem[] getUiLanguages()
	{
		Vector languages = new Vector();
		languages.addElement(new ChoiceItem(ENGLISH, getLabel(ENGLISH, "language", ENGLISH)));
		languages.addAll(getAllCompiledLanguageResources());
		languages.addAll(getNonDuplicateLanguageResourcesInDirectory(languages, directory));
		return (ChoiceItem[])(languages.toArray(new ChoiceItem[0]));
	}
	
	protected Vector getAllCompiledLanguageResources()
	{
		return new Vector();
	}
	
	
	Vector getNonDuplicateLanguageResourcesInDirectory(Vector currentLanguages, File languageDirectory)
	{
		Vector nonDuplicateLanguages = new Vector();
		String[] languageFiles = languageDirectory.list(new LanguageFilenameFilter());
		for(int i=0;i<languageFiles.length;++i)
		{
			if(includeOfficialLanguagesOnly && !isOfficialTranslationFile(new File(languageDirectory, languageFiles[i])))
				continue;
			ChoiceItem languageChoiceItem = getLanguageChoiceItem(languageFiles[i]);
			String languageCodeToAdd = languageChoiceItem.getCode();
			boolean nonDuplicateLanguage = true;
			for(int j=0; j<currentLanguages.size(); ++j)
			{
				ChoiceItem languageChoiceAlreadyAdded = (ChoiceItem)currentLanguages.get(j);
				if(languageChoiceAlreadyAdded.getCode().equalsIgnoreCase(languageCodeToAdd))
				{
					nonDuplicateLanguage = false;
					break;
				}
			}
			if(nonDuplicateLanguage)
				nonDuplicateLanguages.addElement(languageChoiceItem);
		}
		return nonDuplicateLanguages;
	}
	
	public ChoiceItem[] getAvailableCalendarSystems()
	{
		ChoiceItem[] calendarChoices = new ChoiceItem[ALL_CALENDAR_SYSTEMS.length];
		for(int i = 0; i < calendarChoices.length; ++i)
		{
			String system = ALL_CALENDAR_SYSTEMS[i];
			calendarChoices[i] = new ChoiceItem(system, getFieldLabel("CalendarSystem" + system));
		}
		return calendarChoices;
	}
	
	public String getLocalizedFolderName(String folderName)
	{
		return getLabel(getCurrentLanguageCode(), "folder", folderName);
	}

	public String getWindowTitle(String code)
	{
		return getLabel(getCurrentLanguageCode(), "wintitle", code);
	}

	public String getMenuLabel(String code)
	{
		return getLabel(getCurrentLanguageCode(), "menu", code);
	}
	
	public String getKeyword(String code)
	{
		return getLabel(getCurrentLanguageCode(), "keyword", code);
	}
	
	public static String createConfirmEffectTag(String fieldName)
	{
		return createTag("confirm", fieldName, "effect");
	}
	
	public static String createNotifyCauseTag(String fieldName)
	{
		return createTag("notify", fieldName, "cause");
	}
	
	private static String createTag(String prefix, String fieldName, String postfix)
	{
		return prefix + fieldName + postfix;
	}

	public Vector getAllTranslationStrings(String languageCode)
	{
		SortedSet sorted = getAllKeysSorted();
		Vector strings = new Vector();
		Iterator it = sorted.iterator();
		while(it.hasNext())
		{
			String key = (String)it.next();
			String mtfEntry = getMtfEntry(languageCode, key);
			strings.add(mtfEntry);
		}
		return strings;
	}

	public static class LanguageFilenameFilter implements FilenameFilter
	{
		public boolean accept(File dir, String name)
		{
			return isLanguageFile(name);
		}
	}
	
}
