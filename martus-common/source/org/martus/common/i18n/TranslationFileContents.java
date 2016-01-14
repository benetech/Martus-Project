/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2010, Beneficent
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
package org.martus.common.i18n;

import java.util.Vector;

public class TranslationFileContents
{
	public TranslationFileContents(String languageCodeToUse)
	{
		languageCode = languageCodeToUse;
		entries = new Vector<TranslationEntry>();
	}

	public String getLanguageCode()
	{
		return languageCode;
	}

	public void setHeader(TranslationFileHeader headerToUse)
	{
		header = headerToUse;
	}
	
	public void addEntry(TranslationEntry entry)
	{
		if(entry.getMsgid().length() == 0)
		{
			if(header == null)
				header = new TranslationFileHeader(entry.getMsgstr());
		}
		else
		{
			entries.add(entry);
		}
	}

	public Vector<TranslationEntry> getEntries()
	{
		return entries;
	}

	public TranslationFileHeader getHeader()
	{
		return header;
	}

	String languageCode;
	TranslationFileHeader header;
	Vector<TranslationEntry> entries;
}
