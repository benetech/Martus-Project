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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.martus.util.*;
import org.martus.util.UnicodeWriter;

public class TestUnicodeFileWriter extends TestCaseEnhanced
{
    public TestUnicodeFileWriter(String name)
    {
        super(name);
    }

    public void testConstructor() throws Exception
    {
		File badFile = new File(BAD_FILENAME);
		try
		{
			new UnicodeWriter(badFile);
			assertTrue("bad file", false);
		}
		catch(IOException e)
		{
			// exception was expected
		}

		try
		{
			new UnicodeWriter((File)null);
			assertTrue("bad file", false);
		}
		catch(Exception e)
		{
			// exception was expected
		}

		File tempFile = createTempFileFromName("$$$testmartusUnicodeReader");
		UnicodeWriter writer = new UnicodeWriter(tempFile);
		writer.write("hello");
		writer.close();

		UnicodeWriter appender = new UnicodeWriter(tempFile, UnicodeWriter.APPEND);
		appender.write("second");
		appender.close();

		assertEquals("File still open?", true, tempFile.delete());
	}

	public void testStreamConstructor() throws Exception
	{
		try
		{
			new UnicodeWriter((OutputStream)null);
			fail("should not have been able to create");
		}
		catch(Exception e)
		{
			// expected exception
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		UnicodeWriter writer = new UnicodeWriter(outputStream);
		writer.write("test");
		writer.close();
	}

    public void testBasics() throws Exception
    {
		File file = createTempFileFromName("$$$MartustestFileWriter");
		file.delete();
		assertTrue("Delete should work", !file.exists());

		UnicodeWriter writer = new UnicodeWriter(file);
		String text = "Test String";
		writer.write(text);
		writer.close();

		try
		{
			writer.write("Test String");
			assertTrue("Can't write to closed writer", false);
		}
		catch(IOException e)
		{
			// expected an exception
		}

		assertTrue("Should exist", file.exists());
		assertEquals(text.length(), file.length());
		file.delete();
	}

	public void testWritingUnicodeCharacters() throws Exception
	{
		String text2 = new String(new char[] {UnicodeConstants.ACCENT_I_LOWER});
		int len2 = 0;
		try
		{
			len2 = text2.getBytes("UTF8").length;
		}
		catch(Exception e)
		{
			assertTrue("UTF8 not supported", false);
		}
		assertTrue("Unicode length should be different", len2 != text2.length());

		File file = createTempFileFromName("$$$Martustestmartusunicodechars");
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.write(text2);
		writer.close();
		assertEquals(len2, file.length());
		file.delete();
    }

    public void testAppend() throws Exception
    {
		File file = createTempFileFromName("$$$Martustestappend");
		String text = "Testing";
		int len = text.length();

		UnicodeWriter create = new UnicodeWriter(file);
		create.write(text);
		create.close();
		assertTrue("created?", file.exists());
		assertEquals("create length", len, file.length());

		UnicodeWriter overwrite = new UnicodeWriter(file);
		overwrite.write(text);
		overwrite.close();
		assertTrue("still there?", file.exists());
		assertEquals("create length", len, file.length());

		UnicodeWriter append = new UnicodeWriter(file, UnicodeWriter.APPEND);
		append.write(text);
		append.close();
		assertTrue("still there?", file.exists());
		assertEquals("create length", len * 2, file.length());
	
		file.delete();
	}

	public void testWriteln() throws Exception
	{
		File file = createTempFileFromName("$$$MartustestWriteln");
		String text = "Testing";
		int len = text.length();

		UnicodeWriter create = new UnicodeWriter(file);
		create.writeln(text);
		create.close();
		assertTrue("created?", file.exists());
		assertEquals("create length", len + UnicodeWriter.NEWLINE.length(), file.length());

		file.delete();
	}

}
