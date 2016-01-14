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
package org.martus.client.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.martus.util.TestCaseEnhanced;


public class TestRandomAccessFileOverwrite extends TestCaseEnhanced
{
	public TestRandomAccessFileOverwrite(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		tempFile = createTempFile("$$$MartusTestRandomAccessFile");
		FileOutputStream out = new FileOutputStream(tempFile);
		out.write(sampleBytes);
		out.close();		
	}

	public void tearDown() throws Exception
	{		
		tempFile.delete();
		assertFalse("tempFile Not deleted?", tempFile.exists());
		super.tearDown();
	}

    public void testBasics() throws Exception
    {    	   
		randomFile = new RandomAccessFile(tempFile, "rw");
		
		byte firstByte = randomFile.readByte();
		assertEquals("file current pointer?", 1, randomFile.getFilePointer());
		assertEquals("first byte?", sampleBytes[0], firstByte);		
		assertEquals("original file length?", 10, randomFile.length());
		
		scrubData();		
		assertEquals("overwrite file length?", 10, randomFile.length());		
		randomFile.close();
		
		checkScrubbedData();
    }
    
    protected void scrubData() throws IOException
    {
		randomFile.seek(0);		
		for (int i=0; i< randomFile.length();++i)
		{
			randomFile.write(0x55);
		}			
    }
    
    protected void checkScrubbedData() throws Exception
    {
		randomFile = new RandomAccessFile(tempFile, "r");
		randomFile.seek(0);
		for (int i = 0; i < randomFile.length(); i++)
		{
			assertEquals("wrong byte?", 0x55, randomFile.read());
		}
		randomFile.close();
    }
    
	protected File createTempFile(String name) throws IOException
	{
		File file = File.createTempFile(name, null);
		file.deleteOnExit();
		return file;
	}	
	
	File tempFile;
	RandomAccessFile randomFile;
	static final byte[] sampleBytes = {1,2,3,4,5,6,7,8,9,0};	
}
