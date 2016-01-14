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
package org.martus.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;

public class MagicWords
{
	public MagicWords(LoggerInterface loggerToUse)
	{
		logger = loggerToUse;		
		magicWordEntries = Collections.synchronizedList(new ArrayList());	
	}
	
	public synchronized void loadMagicWords(File magicWordsFile) throws IOException
	{
		magicWordEntries.clear();
		try
		{
			UnicodeReader reader = new UnicodeReader(magicWordsFile);
			String line = null;
			while( (line = reader.readLine()) != null)
			{
				if(line.trim().length() == 0)
					logger.logWarning("Found blank line in " + magicWordsFile.getPath());
				else
					add(line);					
			}
			reader.close();
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
			logger.logWarning("No magic words file found:" + magicWordsFile.getPath());
		}
	}

	public synchronized void writeMagicWords(File magicWordsFile, Vector newMagicWordsLineEntries) throws IOException
	{		
		UnicodeWriter writer = new UnicodeWriter(magicWordsFile);
		for (int i=0;i<newMagicWordsLineEntries.size();++i)
		{
			writer.writeln((String)newMagicWordsLineEntries.get(i));
		}								
		writer.close();			
	}
	
	public synchronized MagicWordEntry add(String fileLineEntry)
	{
		String magicWord = getMagicWordWithActiveSignFromLineEntry(fileLineEntry);
		String group = getGroupNameFromLineEntry(fileLineEntry);
		String date = getDateFromLineEntry(fileLineEntry);

		MagicWordEntry entry = add(magicWord, group);
		entry.setCreationDate(date);
				
		return entry;
	}
	
	public synchronized MagicWordEntry add(String magicWordEntry, String group)
	{					
		MagicWordEntry entry = 	new MagicWordEntry(magicWordEntry, group);		
		add(entry);
		return entry;				
	}
	
	public synchronized void add(MagicWordEntry wordEntry)
	{
		if(!contains(wordEntry.getMagicWord()))			
			magicWordEntries.add(wordEntry);
	}
	
	public synchronized void remove(String magicWord)
	{	
		magicWordEntries.remove(getMagicWordEntry(magicWord));
	}
	
	public boolean isValidMagicWord(String magicWordToFind)
	{
		MagicWordEntry entry = getMagicWordEntry(magicWordToFind);
		if(entry != null)
			return entry.isActive();
		return false;
	}
	
	public Vector getAllMagicWords()
	{
		Vector magicWords = new Vector();			
		Collections.reverse(magicWordEntries);
		
		for (Iterator itr = magicWordEntries.iterator(); itr.hasNext();)
		{
			MagicWordEntry entry = (MagicWordEntry) itr.next();
			magicWords.add(entry.getLineOfMagicWord());
		}					
		return magicWords;		
	}
	
	public Vector getActiveMagicWords()
	{
		Vector magicWords = new Vector();
		for (Iterator itr = magicWordEntries.iterator(); itr.hasNext();)
		{
			MagicWordEntry entry = (MagicWordEntry)itr.next();
			if(entry.isActive())
				magicWords.add(entry.getLineOfMagicWord());
		}
		return magicWords;
	}
	
	public Vector getInactiveMagicWordsWithNoSign()
	{
		Vector magicWords = new Vector();		
		for (Iterator itr = magicWordEntries.iterator(); itr.hasNext();)
		{
			MagicWordEntry entry = (MagicWordEntry) itr.next();			
			if(!entry.isActive())
				magicWords.add(entry.getLineOfMagicWordNoSign());
		}
		return magicWords;
	}
	
	public int getNumberOfAllMagicWords()
	{
		return magicWordEntries.size();
	}
	
	public int getNumberOfActiveWords()
	{
		return getActiveMagicWords().size();
	}
	
	public int getNumberOfInactiveWords()
	{
		return getInactiveMagicWordsWithNoSign().size();
	}
	
	public boolean contains(String magicWordToFind)
	{
		return (getMagicWordEntry(magicWordToFind) != null);
	}

	public MagicWordEntry getMagicWordEntry(String magicWordToFind)
	{
		String normalizedMagicWordToFind = normalizeMagicWord(magicWordToFind);
		for (Iterator itr = magicWordEntries.iterator(); itr.hasNext();)
		{
			MagicWordEntry entry = (MagicWordEntry) itr.next();
			if(normalizeMagicWord(entry.getMagicWord()).equals(normalizedMagicWordToFind))
				return entry;
		}
	
		return null;
	}
	
	public static String normalizeMagicWord(String original)
	{
		if(original == null)
			return null;
		return original.toLowerCase().trim().replaceAll("\\s", "");
	}

	public static String getGroupNameFromLineEntry(String lineEntry)
	{
		if (lineEntry == null)
			return "";
		return getFieldFromLine(lineEntry, ID_GROUP).trim();
	}
	
	public static String getDateFromLineEntry(String lineEntry)
	{				
		return getFieldFromLine(lineEntry, ID_DATE).trim();
	}
	
	public static String getMagicWordWithActiveSignFromLineEntry(String lineEntry)
	{
		if (lineEntry == null)
			return "";
			
		return getFieldFromLine(lineEntry, ID_WORD).trim();
	}
	
	
	public static String filterActiveSign(String magicWord)
	{		
		if (magicWord.startsWith(INACTIVE_SIGN))
			magicWord = magicWord.substring(1);
		
		return magicWord;
	}
	
	private static String getFieldFromLine(String lineEntry, int id)
	{		
		StringTokenizer st = new StringTokenizer(lineEntry, "\t");
		ArrayList fields = new ArrayList();		
		while (st.hasMoreTokens()) 
			fields.add(st.nextToken());
		
//		if (fields.size()== 1)
//			fields.add(filterActiveSign((String)fields.get(ID_WORD)));	
			
		return (id < fields.size())?(String) fields.get(id):MISSING_FIELD;
	}
	
	public static final char FIELD_DELIMITER = '\t';
	public static final String INACTIVE_SIGN = "#";
	
	private static final String MISSING_FIELD = "?";
	public static final int ID_WORD=0;
	public static final int ID_GROUP=1;
	public static final int ID_DATE=2;
	
	List magicWordEntries; 
	LoggerInterface logger;
}