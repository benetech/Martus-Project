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

package org.martus.common.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.martus.common.MartusConstants;
import org.martus.util.*;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.UnicodeReader.BOMNotFoundException;

public class TestUnicodeFileReader extends TestCaseEnhanced
{
    public TestUnicodeFileReader(String name)
    {
        super(name);
    }

 	public void testConstructor() throws Exception
	{
		File badFile = new File(BAD_FILENAME);
		try
		{
			new UnicodeReader(badFile);
			assertTrue("bad file", false);
		}
		catch(IOException e)
		{
			// exception was expected
		}

		try
		{
			new UnicodeReader((File)null);
			assertTrue("null file", false);
		}
		catch(Exception e)
		{
			// exception was expected
		}

		File file = createTempFileFromName("$$$MartusTestUnicodeFileReader");
		createTempFile(file);
		UnicodeReader reader = new UnicodeReader(file);

		assertEquals("Can read line 1 from open reader", text, reader.readLine());
		assertEquals("Can read line 2 from open reader", text2, reader.readLine());
		assertEquals("Null at EOF", null, reader.readLine());
		reader.close();
		try
		{
			reader.readLine();
			assertTrue("no exception from closed file read", false);
		}
		catch(Exception e)
		{
			// exception was expected
		}
		assertEquals("File still open?", true, file.delete());
	}

	public void testStreamConstructor() throws Exception
	{
		try
		{
			new UnicodeReader((InputStream)null);
			fail("should not have been able to create");
		}
		catch(Exception e)
		{
			// expected exception
		}

		byte[] bytes = {'a', 'b', 'c', 'd'};
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		UnicodeReader reader = new UnicodeReader(inputStream);
		reader.readLine();
		reader.close();
	}

    public void testReadAll() throws Exception
    {
		File file = createTempFileFromName("$$testUnicodeReadAll");
		createTempFile(file);
		UnicodeReader reader = new UnicodeReader(file);

		String result = reader.readAll();
		assertEquals(text + MartusConstants.NEWLINE + text2 + MartusConstants.NEWLINE, result);
		reader.close();

		file.delete();
	}
    
    public void testSkipBOM() throws Exception
	{
		File file = createTempFileFromName("$$testSkipBOMNonUTF8");
		createTempFile(file);
		UnicodeReader reader = new UnicodeReader(file);
		try
		{
			reader.skipBOM();
			fail("Should have thrown since file isn't UTF8");
		}
		catch(BOMNotFoundException expected)
		{
		}
		reader.close();
		
		File utf8File = createTempFile();
		UnicodeWriter writer = new UnicodeWriter(utf8File);
		writer.writeBOM();
		writer.write(text);
		writer.close();
		
		reader = new UnicodeReader(utf8File);
		reader.skipBOM();
		String resultingText = reader.readLine();
		reader.close();
		assertEquals("Text after the BOM should be idential", text, resultingText);
	}

	void createTempFile(File file) throws Exception
	{
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.write(text + MartusConstants.NEWLINE + text2 + MartusConstants.NEWLINE);
		writer.close();
	}

	final String text = "Test String";
	final String text2 = "\u0e05";
}
