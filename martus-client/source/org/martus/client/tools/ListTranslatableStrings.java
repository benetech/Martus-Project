/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.client.tools;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.martus.client.swingui.UiSession;
import org.martus.common.MiniLocalization;
import org.martus.common.i18n.PoLoader;
import org.martus.common.i18n.TranslationFileContents;
import org.martus.util.UnicodeReader;

/*
 * 1. From the top-level Martus directory, run: 
 *    find -name "*.java" >~/javafiles.txt
 * 2. From the top-level Martus directory, run:
 *    xgettext --from-code=UTF-8 --extract-all --output=/home/kevins/javastrings.pot --language=Java --files-from ~/javafiles.txt
 * 3. From the top-level Martus directory, run:
 *    grep -r "\"%.*?\"" --include="*.fxml" --no-filename --only-matching --perl-regexp >~/fxmlstrings.txt
 * 
 * At that point, you should have javastrings.txt and fxmlstrings.txt in /home/kevins
 */
public class ListTranslatableStrings
{
	public static void main(String[] args) throws Exception
	{
		HashMap<String, HashSet<String>> categories = loadAvailableTranslatableKeys();
		HashSet<String> availableKeys = extractKeys(categories);
		
		HashSet<String> keysUsedInFxml = loadKeysUsedInFxml();
		HashSet<String> keysUsedInJava = loadKeysUseInJava();
		
		System.out.println("Total available translable keys: " + availableKeys.size());
		availableKeys.removeAll(keysUsedInFxml);
		System.out.println("Keys not used in FXML: " + availableKeys.size());
		availableKeys.removeAll(keysUsedInJava);
		System.out.println("Keys not used in FXML or Java: " + availableKeys.size());
		Vector<String> sortedKeys = new Vector(availableKeys);
		Collections.sort(sortedKeys);
		sortedKeys.forEach(key -> System.out.println(key));
		
//		categories.keySet().forEach(category -> displayCategory(categories, category));
	}

	private static HashSet<String> extractKeys(	HashMap<String, HashSet<String>> categories)
	{
		HashSet<String> allKeys = new HashSet();
		categories.keySet().forEach(category -> allKeys.addAll(categories.get(category)));
		return allKeys;
	}

	private static HashSet<String> loadKeysUseInJava() throws Exception
	{
		HashSet<String> keysUsedInJava = new HashSet();
		
		File fileContainingKeys = new File("/home/kevins/javastrings.pot");
		UnicodeReader reader = new UnicodeReader(fileContainingKeys);
		TranslationFileContents contents = PoLoader.read(reader, "en");
		contents.getEntries().forEach(entry -> keysUsedInJava.add(entry.getMsgid()));
//		keysUsedInJava.forEach(key -> System.out.println(key));
		return keysUsedInJava;
	}

	private static HashSet<String> loadKeysUsedInFxml() throws Exception
	{
		HashSet<String> keysUsedInFxml = new HashSet();
		
		File fileContainingKeys = new File("/home/kevins/fxmlstrings.txt");
		UnicodeReader reader = new UnicodeReader(fileContainingKeys);
		try
		{
			while(true)
			{
				String line = reader.readLine();
				if(line == null)
					break;
				
				String key = line.substring(2, line.length()-1);
				
				int dotAt = key.indexOf('.');
				if(dotAt >= 0)
					key = key.substring(dotAt+1);
				
				keysUsedInFxml.add(key);
			}
			return keysUsedInFxml;
		} 
		finally
		{
			reader.close();
		}
	}

	private static HashMap<String, HashSet<String>> loadAvailableTranslatableKeys()
	{
		// FIXME: All the methods we call really should be changed to static
		MiniLocalization localization = new MiniLocalization();
		
		HashMap<String, HashSet<String>> categories = new HashMap();
		
		String[] englishTranslations = UiSession.getAllEnglishStrings();
		for (String entryText : englishTranslations)
		{
			String key = localization.extractKeyFromEntry(entryText);
			//String value = localization.extractValueFromEntry(entryText);
			
			String prefix = "";
			int colonAt = key.indexOf(':');
			if(colonAt >= 0)
			{
				prefix = key.substring(0, colonAt);
				key = key.substring(colonAt + 1);
			}
			if(prefix.equals("wintitle"))
			{
				if(key.startsWith("notify"))
					key = key.substring("notify".length());
				else if(key.startsWith("confirm"))
					key = key.substring("confirm".length());
				else if(key.startsWith("input"))
					key = key.substring("input".length());
			}
			else if(prefix.equals("button"))
			{
				if(key.startsWith("input") && key.endsWith("ok"))
					key = key.substring("input".length(), key.lastIndexOf("ok"));
				else if(key.startsWith("FileDialogOk"))
					key = key.substring("FileDialogOk".length());
				
			}
			else if(prefix.equals("field"))
			{
				if(key.startsWith("notify") && key.endsWith("cause"))
					key = key.substring("notify".length(), key.lastIndexOf("cause"));
				else if(key.startsWith("message") && key.endsWith("cause"))
					key = key.substring("message".length(), key.lastIndexOf("cause"));
				else if(key.startsWith("confirm") && key.endsWith("cause"))
					key = key.substring("confirm".length(), key.lastIndexOf("cause"));
				else if(key.startsWith("confirm") && key.endsWith("effect"))
					key = key.substring("confirm".length(), key.lastIndexOf("effect"));
				else if(key.startsWith("input") && key.endsWith("entry"))
					key = key.substring("input".length(), key.lastIndexOf("entry"));
				else if(key.startsWith("FieldType"))
					key = key.substring("FieldType".length());
				else if(key.startsWith("ChartType"))
					key = key.substring("ChartType".length());
				else if(key.startsWith("CalendarSystem"))
					key = key.substring("CalendarSystem".length());
				else if(key.startsWith("FileDialog"))
					key = key.substring("FileDialog".length());
				else if(key.startsWith("DatePart"))
					key = key.substring("DatePart".length());
				else if(key.startsWith("_Section"))
					key = key.substring("_Section".length());
				else if(key.startsWith("BulletinDetails"))
					key = key.substring("BulletinDetails".length());
			}
			
			HashSet<String> entriesForCategory = categories.get(prefix);
			if(entriesForCategory == null)
			{
				entriesForCategory = new HashSet();
				categories.put(prefix, entriesForCategory);
			}
			
			entriesForCategory.add(key);
		}
		return categories;
	}

/*	private static void displayCategory(HashMap<String, HashSet<String>> categories, String prefix)
	{
		System.out.println(prefix);
		HashSet<String> entriesForCategory = categories.get(prefix);
		entriesForCategory.forEach(string -> System.out.println("  " + string));
	}
*/
}

