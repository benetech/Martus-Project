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

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusLogger;
import org.martus.util.UnicodeStringReader;

import com.inet.jortho.UserDictionaryProvider;

public class MartusUserDictionary implements UserDictionaryProvider
{
	public MartusUserDictionary(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		
		original = new HashSet<String>();
		
		extras = new HashSet<String>();
		extras.add("Martus");
		extras.add("Benetech");
		
		loadDictionary();
	}
	
	public Iterator<String> getWords(Locale locale)
	{
		return new WordIterator(original, extras);
	}
	
	static class WordIterator implements Iterator<String>
	{
		public WordIterator(Collection<String> original, Collection<String> extras)
		{
			originalIterator = original.iterator();
			extrasIterator = extras.iterator();
		}
		
		public boolean hasNext()
		{
			boolean result = originalIterator.hasNext();
			if(result)
				return result;
			
			return extrasIterator.hasNext();
		}

		public String next()
		{
			if(originalIterator.hasNext())
				return originalIterator.next();

			return extrasIterator.next();
		}

		public void remove()
		{
			originalIterator.remove();
		}

		private Iterator<String> originalIterator;
		private Iterator<String> extrasIterator;
	}
	

	public void addWord(String newWord)
	{
		addWordToInMemoryWordList(newWord);
		saveDictionary();
	}

	private void addWordToInMemoryWordList(String newWord)
	{
		original.add(newWord.trim());
	}
	
	private void loadDictionary()
	{
		try
		{
			String allWords = getApp().readSignedUserDictionary();
			loadDictionary(allWords);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			mainWindow.notifyDlg("ErrorLoadingDictionary");
		}
	}

	private void loadDictionary(String allWords) throws IOException
	{
		original.clear();
		UnicodeStringReader reader = new UnicodeStringReader(allWords);
		while(reader.ready())
		{
			String word = reader.readLine();
			if(extras.contains(word))
				continue;
			
			addWordToInMemoryWordList(word);
		}
		reader.close();
		MartusLogger.log("User dictionary loaded word count: " + original.size());
	}

	private void saveDictionary()
	{
		StringBuffer buffer = new StringBuffer();
		for (String word : original)
		{
			buffer.append(word);
			buffer.append('\n');
		}
		
		String words = buffer.toString();

		saveDictionary(words);
	}

	private void saveDictionary(String words)
	{
		try
		{
			getApp().writeSignedUserDictionary(words);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			mainWindow.notifyDlg("ErrorSavingDictionary");
		}
	}

	public void setUserWords(String words)
	{
		try
		{
			loadDictionary(words);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			mainWindow.notifyDlg("ErrorUpdatingDictionary");
		}
		saveDictionary();
	}
	
	public MartusApp getApp()
	{
		return mainWindow.getApp();
	}
	
	private UiMainWindow mainWindow;
	private HashSet<String> original;
	private HashSet<String> extras;
}
