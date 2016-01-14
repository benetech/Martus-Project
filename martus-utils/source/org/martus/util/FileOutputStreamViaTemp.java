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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileOutputStreamViaTemp extends OutputStream
{
	public FileOutputStreamViaTemp(File destFile, File tempDirectory) throws IOException
	{
		realDestFile = destFile;
		tempFile = File.createTempFile("$$$" + destFile.getName(), "", tempDirectory);
		tempFile.deleteOnExit();
		tempOutputStream = new FileOutputStream(tempFile);
	}

	public void write(int b) throws IOException
	{
		tempOutputStream.write(b);
	}

	public void close() throws IOException
	{
		tempOutputStream.close();
		if(realDestFile.exists())
		{
			if(areContentsIdentical(tempFile, realDestFile))
				return;
			if(!realDestFile.delete())
				throw new IOException("Unable to delete existing file: " + realDestFile.getAbsolutePath());
		}
		if(!tempFile.renameTo(realDestFile))
			throw new IOException("Unable to rename from " + tempFile.getAbsolutePath() + " to " + realDestFile);
	}

	private boolean areContentsIdentical(File file1, File file2) throws IOException
	{
		if(file1.length() != file2.length())
			return false;
		
		BufferedInputStream in1 = new BufferedInputStream(new FileInputStream(file1));
		BufferedInputStream in2 = new BufferedInputStream(new FileInputStream(file2));
		try
		{
			while(true)
			{
				int byte1 = in1.read();
				int byte2 = in2.read();
				if(byte1 != byte2)
					return false;
				if(byte1 < 0)
					break;
			}
			return true;
		}
		finally
		{
			in2.close();
			in1.close();
		}
	}

	public void flush() throws IOException
	{
		tempOutputStream.flush();
	}

	File realDestFile;
	File tempFile;
	OutputStream tempOutputStream;
}
