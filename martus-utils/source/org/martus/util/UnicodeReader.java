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

To the extent this copyrighted software code is used in the 
Miradi project, it is subject to a royalty-free license to 
members of the Conservation Measures Partnership when 
used with the Miradi software as specified in the agreement 
between Benetech and WCS dated 5/1/05.
*/

package org.martus.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UnicodeReader extends BufferedReader
{
	public UnicodeReader(File file) throws IOException
	{
		this(new FileInputStream(file));
	}

	public UnicodeReader(InputStream inputStream) throws IOException
	{
		super(new InputStreamReader(inputStream, "UTF8"));
	}

	// TODO: try remove this and combine with the real readAll
	// NOTE: allowing unlimited length is not any riskier because an attacker
	// could feed us a 100Meg file all on one line.
	
	public String readAll(int maxLines) throws IOException
	{
		StringBuffer all = new StringBuffer();
		for(int i = 0; i < maxLines; ++i)
		{
			String line = readLine();
			if(line == null)
				break;
			all.append(line);
			all.append(NEWLINE);
		}

		return all.toString();
	}

	public String readAll() throws IOException
	{
		StringBuffer all = new StringBuffer();
		while(true)
		{
			String line = readLine();
			if(line == null)
				break;
			all.append(line);
			all.append(NEWLINE);
		}
		return all.toString();
	}
	
	public static String getFileContents(File file) throws IOException
	{
		UnicodeReader reader = new UnicodeReader(file);
		String contents = reader.readAll();
		reader.close();
		return contents;
	}
	
	public class BOMNotFoundException extends Exception 
	{
	}
	
	public void skipBOM() throws BOMNotFoundException
	{
		try
		{
			char BOM = (char)read();
			if(BOM != UnicodeWriter.BOM_UTF8)
				throw new BOMNotFoundException();
		}
		catch(IOException e)
		{
			throw new BOMNotFoundException();
		}
	}

	final String NEWLINE = System.getProperty("line.separator");
}
