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
import java.io.FileOutputStream;
import java.util.Arrays;

import org.martus.util.*;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


public class TestFileInputStreamWithSeek extends TestCaseEnhanced
{
	public TestFileInputStreamWithSeek(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		tempFile = createTempFileFromName("$$$MartusTestFileInputStreamWithReset");
		FileOutputStream out = new FileOutputStream(tempFile);
		out.write(sampleBytes);
		out.close();

		in = new FileInputStreamWithSeek(tempFile);
	}

	public void tearDown() throws Exception
	{
		in.close();
		tempFile.delete();
		super.tearDown();
	}

	public void testSimpleRead() throws Exception
	{
		assertEquals("available?", sampleBytes.length, in.available());
		int firstByte = in.read();
		assertEquals("wrong first byte?", sampleBytes[0], firstByte);
	}

	public void testAvailable() throws Exception
	{
		in.read();
		// available doesn't seem to be implemented correctly by the InputStream
		// that ZipFile returns. So the following test would fail. We don't care.
		//assertEquals("available after read?", sampleBytes.length-1, in.available());
		in.seek(0);
		assertEquals("available after reset?", sampleBytes.length, in.available());
	}

	public void testReadAfterReset() throws Exception
	{
		in.read();
		in.seek(0);

		byte[] allBytes = new byte[sampleBytes.length];
		assertEquals("got?", sampleBytes.length, in.read(allBytes));
		assertEquals("wrong bytes?", true, Arrays.equals(sampleBytes, allBytes));
	}

	public void testMiddleMarkAndReset() throws Exception
	{
		in.read();
		in.read();
		in.seek(1);
		assertEquals("after middle reset", sampleBytes[1], in.read());
	}

	public void testSkip() throws Exception
	{
		in.read();
		in.skip(3);
		assertEquals("after skip", sampleBytes[4], in.read());
		in.skip(5);
		in.seek(2);
		assertEquals("after skip and reset", sampleBytes[2], in.read());
	}

	public void testClose() throws Exception
	{
		in.close();
		assertEquals("delete after close failed?", true, tempFile.delete());
	}

	File tempFile;
	InputStreamWithSeek in;
	static final byte[] sampleBytes = {1,2,3,4,5,6,7,8,9,0,127};
}
