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

package org.martus.client.swingui.spellcheck;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;

import com.inet.jortho.SpellChecker;
import com.inet.jortho.SpellCheckerOptions;
import com.inet.jortho.UserDictionaryProvider;

public class SpellCheckerManager
{
	public static void initializeSpellChecker(UiMainWindow mainWindowToUse) throws MalformedURLException
	{
		mainWindow = mainWindowToUse;
		
		SpellCheckerOptions options = SpellChecker.getOptions();
		options.setCaseSensitive(true);
		options.setIgnoreAllCapsWords(true);
		options.setIgnoreCapitalization(true);
		options.setIgnoreWordsWithNumbers(true);
		options.setSuggestionsLimitMenu(15);
		
		userDictionaryProvider = new MartusUserDictionary(mainWindow);
		updateUserDictionaryProvider();

		registerOfficialDictionary(mainWindow);
	}

	private static void registerOfficialDictionary(UiMainWindow mainWindowToUse)
	{
		String english = MartusLocalization.ENGLISH;
		URL dictionaryFolderURL = mainWindowToUse.getApp().getUrlOfDirectoryContainingDictionaries(english);
		MartusLogger.log("SpellCheckerManager: Looking for dictionary: " + dictionaryFolderURL);
		SpellChecker.registerDictionaries(dictionaryFolderURL, english, english);
	}

	public static void setUserWords(String wordList)
	{
		getUserDictionary().setUserWords(wordList);
		updateUserDictionaryProvider();
		registerOfficialDictionary(mainWindow);
	}
	
	public static Vector<String> getUserDictionaryWords()
	{
		Vector<String> wordList = new Vector<String>();
		
		Iterator<String> iter = getUserDictionary().getWords(new Locale(MiniLocalization.ENGLISH));
		while(iter != null && iter.hasNext())
		{
			String word = iter.next().trim();
			if(word.length() == 0)
				continue;
			wordList.add(word);
		}
		
		return wordList;
	}

	private static void updateUserDictionaryProvider()
	{
		SpellChecker.setUserDictionaryProvider(userDictionaryProvider);
	}
	
	private static UserDictionaryProvider getUserDictionary()
	{
		return userDictionaryProvider;
	}
	
	private static UiMainWindow mainWindow;
	private static MartusUserDictionary userDictionaryProvider;
}
