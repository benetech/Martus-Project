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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.martus.util.FileOutputStreamViaTemp;
import org.martus.util.TestCaseEnhanced;

public class TestFileOutputStreamViaTemp extends TestCaseEnhanced
{
	public TestFileOutputStreamViaTemp(String name)
	{
		super(name);
	}


	public void testWhenFileExists() throws Exception
	{
		File destFile = createTempFile();
		File tempDirectory = createTempDirectory();
		FileOutputStreamViaTemp out = new FileOutputStreamViaTemp(destFile, tempDirectory);
		byte[] sampleData = {1,2,3,4,5};
		out.write(sampleData);
		out.close();
		assertTrue("Didn't create dest?", destFile.exists());
		assertEquals("Wrong length?", sampleData.length, destFile.length());
		
		byte[] gotData = new byte[(int)destFile.length()];
		FileInputStream in = new FileInputStream(destFile);
		in.read(gotData);
		assertTrue("Wrong data?", Arrays.equals(sampleData, gotData));
		assertEquals("more data?", -1, in.read());
		in.close();
		
		destFile.delete();
	}

	public void testWhenFileExistsEmptyReadOnly() throws Exception
	{
		File destFile = createTempFile();
		UndeletableFile undeletableFile = new UndeletableFile(destFile);
		
		File tempDirectory = createTempDirectory();
		FileOutputStreamViaTemp out = new FileOutputStreamViaTemp(undeletableFile, tempDirectory);
		byte[] sampleData = {1,2,3,4,5};
		out.write(sampleData);
		try
		{
			out.close();
			fail("Should have thrown attempting to replace existing readonly file");
		}
		catch(IOException ignoreExpected)
		{
		}
		finally
		{
			destFile.delete();
		}
	}

	public void testWhenFileExistsReadOnlyIdenticalContents() throws Exception
	{
		byte[] sampleData = {1,2,3,4,5};

		File destFile = createTempFile();
		FileOutputStream initialOut = new FileOutputStream(destFile);
		initialOut.write(sampleData);
		initialOut.close();
		
		UndeletableFile undeletableFile = new UndeletableFile(destFile);
		
		File tempDirectory = createTempDirectory();
		FileOutputStreamViaTemp out = new FileOutputStreamViaTemp(undeletableFile, tempDirectory);
		out.write(sampleData);
		out.close();

		assertTrue("Didn't reuse existing file?", destFile.exists());
		assertEquals("Changed length?", sampleData.length, destFile.length());
		
		byte[] gotData = new byte[(int)destFile.length()];
		FileInputStream in = new FileInputStream(destFile);
		in.read(gotData);
		assertTrue("Chagned data?", Arrays.equals(sampleData, gotData));
		assertEquals("Too much data?", -1, in.read());
		in.close();
		
		destFile.delete();
	}

	public void testWhenFileDoesntExist() throws Exception
	{
		File destFile = createTempFile();
		destFile.delete();
		File tempDirectory = createTempDirectory();
		FileOutputStreamViaTemp out = new FileOutputStreamViaTemp(destFile, tempDirectory);
		assertFalse("Already created dest?", destFile.exists());
		byte[] sampleData = {1,2,3,4,5};
		out.write(sampleData);
		out.close();
		assertTrue("Didn't create dest?", destFile.exists());
		assertEquals("Wrong length?", sampleData.length, destFile.length());
		
		byte[] gotData = new byte[(int)destFile.length()];
		FileInputStream in = new FileInputStream(destFile);
		in.read(gotData);
		assertTrue("Wrong data?", Arrays.equals(sampleData, gotData));
		assertEquals("more data?", -1, in.read());
		in.close();
		
		destFile.delete();
	}

	static class UndeletableFile extends File
	{
		public UndeletableFile(File aliasOf)
		{
			super(aliasOf.getAbsolutePath());
		}
		
		public boolean delete()
		{
			return false;
		}
	}

}
