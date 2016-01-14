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

package org.martus.common;

import java.io.IOException;
import java.io.OutputStream;

import org.martus.util.StreamableBase64;

public class Base64XmlOutputStream extends OutputStream
{
	public Base64XmlOutputStream(XmlWriterFilter destination)
	{
		dest = destination;
		buffer = new byte[StreamableBase64.BYTESPERLINE];
		offset = 0;
	}

	public void write(int b) throws IOException
	{
		buffer[offset++] = (byte)b;
		if(offset >= buffer.length)
			flush();
	}

	public void flush() throws IOException
	{
		flushBuffer();
	}

	public void close() throws IOException
	{
		flush();
	}

	private void flushBuffer() throws IOException
	{
		String thisLine = StreamableBase64.encode(buffer, 0, offset);
		writeLine(thisLine);
		offset = 0;
	}

	private void writeLine(String thisLine) throws IOException
	{
		dest.writeDirect(thisLine);
		dest.writeDirect("\n");
	}

	XmlWriterFilter dest;
	byte[] buffer;
	int offset;
}
