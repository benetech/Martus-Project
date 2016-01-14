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
package org.martus.amplifier.main;

import java.io.File;
import java.io.IOException;

import org.martus.amplifier.common.SearchResultConstants;


public class LanguagesIndexedList extends IndexedValuesList
{
	public LanguagesIndexedList(File languagesIndexedFileToUse)
	{
		super(languagesIndexedFileToUse);
	}

	void createInitialList() throws IOException
	{
		super.createInitialList();
		addValue(SearchResultConstants.LANGUAGE_ANYLANGUAGE_LABEL);
	}


	
	static public void initialize(File languagesIndexedFile) throws IOException
	{
		LanguagesIndexedList.languagesIndexedSingleton = new LanguagesIndexedList(languagesIndexedFile);
		LanguagesIndexedList.languagesIndexedSingleton.loadFromFile();
	}
		
	public static LanguagesIndexedList languagesIndexedSingleton;

}
