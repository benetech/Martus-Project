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
package org.martus.meta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

import org.martus.util.ScrubFile;
import org.martus.util.TestCaseEnhanced;


public class TestScrubFile extends TestCaseEnhanced
{
	public TestScrubFile(String name)
	{
		super(name);
	}
	
	public void testSmallFiles() throws Exception
	{
		File smallFile = createTempFile();
		FileOutputStream out = new FileOutputStream(smallFile);
		byte[] dataOut = {1,2,3,4,5,6,7,8,9};
		byte[] dataIn = new byte[9];
		out.write(dataOut);
		out.close();
		FileInputStream in = new FileInputStream(smallFile);
		for(int i=0 ; ; ++i)
		{
			int data = in.read();
			if(data == -1)
				break;
			dataIn[i] = (byte)data;
		}
		in.close();
		assertTrue("Data should match",Arrays.equals(dataIn, dataOut));
		ScrubFile.scrub(smallFile);

		in = new FileInputStream(smallFile);
		for(int i=0 ; ; ++i)
		{
			int data = in.read();
			if(data == -1)
				break;
			dataIn[i] = (byte)data;
		}
		in.close();
		assertFalse("Data should be different",Arrays.equals(dataIn, dataOut));
		byte[] scrubbedData = {0x55,0x55,0x55,0x55,0x55,0x55,0x55,0x55,0x55};
		assertTrue("new Data should match Scrubbed",Arrays.equals(dataIn, scrubbedData));
	}

	public void testLargeFiles() throws Exception
	{
		File largeFile = createTempFile();
		int largeFileSize = 7*1024+3*100+82;
		byte[] dataOut = new byte[largeFileSize];

		FileOutputStream out = new FileOutputStream(largeFile);
		for(int i=0;i<largeFileSize;++i)
			dataOut[i] = 20;
		out.write(dataOut);
		out.close();

		FileInputStream in = new FileInputStream(largeFile);
		byte[] dataIn = new byte[largeFileSize];
		for(int i=0 ; ; ++i)
		{
			int data = in.read();
			if(data == -1)
				break;
			dataIn[i] = (byte)data;
		}
		in.close();
		assertTrue("Large Data should match",Arrays.equals(dataIn, dataOut));
		ScrubFile.scrub(largeFile);
	
		in = new FileInputStream(largeFile);
		for(int i=0 ; ; ++i)
		{
			int data = in.read();
			if(data == -1)
				break;
			dataIn[i] = (byte)data;
		}
		in.close();
		assertFalse("Data should be different",Arrays.equals(dataIn, dataOut));
		byte[] scrubbedData = new byte[largeFileSize];
		for(int i=0;i<largeFileSize;++i)
			scrubbedData[i] = 0x55;

		assertTrue("new Data should match Scrubbed",Arrays.equals(dataIn, scrubbedData));
	}

}
