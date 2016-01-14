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
import java.util.Vector;

import org.martus.common.MartusUtilities;
import org.martus.util.UnicodeWriter;


public class IndexedValuesList
{

	public IndexedValuesList(File fileToUse)
	{
		file = fileToUse;
		indexedValues = null;
	}
	
	void createInitialList() throws IOException
	{
		indexedValues = new Vector();
	}


	public void loadFromFile() throws IOException
	{
		try
		{
			indexedValues = MartusUtilities.loadListFromFile(file);
		}
		catch(IOException e)
		{
			createInitialList();
			throw e;
		}
	}

	public void addValue(String language) throws IOException
	{
		if(language.length() == 0)
			return;
		
		if(indexedValues == null)
			createInitialList();
		if(!indexedValues.contains(language))
		{
			indexedValues.add(language);
			saveToFile();
		}
	}

	public void saveToFile() throws IOException
	{
		UnicodeWriter writer = new UnicodeWriter(file);
		for (int i = 0; i < indexedValues.size(); i++)
		{
			writer.writeln((String)indexedValues.get(i));	
		}
		writer.close();
	}

	public Vector getIndexedValues()
	{
		if(indexedValues == null)
			return new Vector();
		
		return indexedValues;
	}

	protected File file;
	protected Vector indexedValues;

}
