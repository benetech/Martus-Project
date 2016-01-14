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

import java.io.File;
import java.io.IOException;

import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;

public class Po2Mtf
{
	public static void convertToMtf(File poFile, String languageCode) throws Exception
	{
		UnicodeReader reader = new UnicodeReader(poFile);
		try
		{
			UnicodeWriter writer = new UnicodeWriter(System.out);
			try
			{
				convertToMtf(reader, writer, languageCode);
			}
			finally
			{
				writer.close();
			}
		}
		finally
		{
			reader.close();
		}
	}

	public static void convertToMtf(UnicodeReader reader, UnicodeWriter writer, String languageCode)
			throws IOException
	{
		TranslationFileContents contents = PoLoader.read(reader, languageCode);
		MtfSaver.save(writer, contents);
		writer.flush();
	}
	
	public static void main(String[] args) throws Exception
	{
		File poFile = new File(args[0]);
		String languageCode = args[1];
		UnicodeReader reader = new UnicodeReader(poFile);
		UnicodeWriter writer = new UnicodeWriter(System.out);
		convertToMtf(reader, writer, languageCode);
	}
}
