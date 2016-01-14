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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.martus.common.Base64XmlOutputStream;
import org.martus.common.XmlWriterFilter;
import org.martus.util.*;
import org.martus.util.StreamableBase64;

public class TestBase64XmlOutputStream extends TestCaseEnhanced
{
	public TestBase64XmlOutputStream(String name)
	{
		super(name);
	}

	public void testOneByte() throws Exception
	{
		byte[] data = {1};
		String result = convertToBase64BySingleBytes(data);
		assertEquals("wrong result?", StreamableBase64.encode(data) + "\n", result);
	}

	public void testThreeBytes() throws Exception
	{
		byte[] data = {1, 127, -127};
		String result = convertToBase64BySingleBytes(data);
		assertEquals("wrong result?", StreamableBase64.encode(data) + "\n", result);
	}

	public void testFourBytes() throws Exception
	{
		byte[] data = {-128, 0, -64, 64};
		String result = convertToBase64BySingleBytes(data);
		assertEquals("wrong result?", StreamableBase64.encode(data) + "\n", result);
	}

	public void testManyBytes() throws Exception
	{
		int length = 1024;
		byte[] data = createBytes(length);

		String result = convertToBase64BySingleBytes(data);
		String expected = getExpectedResult(data);
		assertEquals("wrong result?", expected, result);
	}

	public void testBigChunk() throws Exception
	{
		int length = 50;
		byte[] data = createBytes(length);

		Writer writer = new StringWriter();
		XmlWriterFilter wf = new XmlWriterFilter(writer);
		Base64XmlOutputStream out = new Base64XmlOutputStream(wf);
		try
		{
			out.write(data, 0, data.length);
			out.flush();
			String result = writer.toString();
			String expected = getExpectedResult(data);
			assertEquals("wrong result?", expected, result);
		}
		finally
		{
			out.close();
		}
	}

	public void testBigChunkThenBytes() throws Exception
	{
		int length = 50;
		byte[] data = createBytes(length);

		Writer writer = new StringWriter();
		XmlWriterFilter wf = new XmlWriterFilter(writer);
		Base64XmlOutputStream out = new Base64XmlOutputStream(wf);
		try
		{
			out.write(data, 0, data.length-1);
			out.write(data, data.length-1, 1);
			out.flush();
			String result = writer.toString();
			String expected = getExpectedResult(data);
			assertEquals("wrong result?", expected, result);
		}
		finally
		{
			out.close();
		}
	}

	public void testBytesThenBigChunk() throws Exception
	{
		int length = 50;
		byte[] data = createBytes(length);

		Writer writer = new StringWriter();
		XmlWriterFilter wf = new XmlWriterFilter(writer);
		Base64XmlOutputStream out = new Base64XmlOutputStream(wf);
		try
		{
			out.write(data, 0, 1);
			out.write(data, 1, data.length-1);
			out.flush();
			String result = writer.toString();
			String expected = getExpectedResult(data);
			assertEquals("wrong result?", expected, result);
		}
		finally
		{
			out.close();
		}
	}

	String getExpectedResult(byte[] data)
	{
		String expected = "";
		int offset = 0;
		while (offset < data.length)
		{
			int len = StreamableBase64.BYTESPERLINE;
			if(len > data.length - offset)
				len = data.length - offset;
			expected += StreamableBase64.encode(data, offset, len);
			expected += "\n";
			offset += StreamableBase64.BYTESPERLINE;
		}
		return expected;
	}

	byte[] createBytes(int length)
	{
		byte[] data = new byte[length];
		for(int i=0; i < data.length; ++i)
			data[i] = (byte)i;
		return data;
	}

	String convertToBase64BySingleBytes(byte[] data) throws IOException
	{
		Writer writer = new StringWriter();
		XmlWriterFilter wf = new XmlWriterFilter(writer);
		Base64XmlOutputStream out = new Base64XmlOutputStream(wf);
		for(int i = 0; i < data.length; ++i)
			out.write(data[i]);
		out.close();
		String result = writer.toString();
		return result;
	}
}
