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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Vector;

public class StreamableBase64
{
	public static class InvalidBase64Exception extends Exception
	{
		public InvalidBase64Exception()
		{
			
		}
		
		public InvalidBase64Exception(Exception e)
		{
			super(e);
		}
	}

	public final static int BYTESPERLINE = 45;

	public static String encode(String raw) throws UnsupportedEncodingException
	{
		return encode(raw.getBytes("UTF-8"));
	}
	
	public static String encode(byte[] raw)
	{
		return encode(raw, 0, raw.length);
	}

	public static String encode(byte[] raw, int start, int length)
	{
		StringBuffer encoded = new StringBuffer();

		int stopAt = start + length;
		for(int i = start; i < stopAt; i += 3)
		{
			encoded.append(encodeBlock(raw, i, length));
			length -= 3;
		}

		return encoded.toString();
	}

	public static String encodeLineWrapped(byte[] raw)
	{
		StringBuffer encoded = new StringBuffer();

		for(int i = 0; i < raw.length; i += BYTESPERLINE)
		{
			int length = Math.min(BYTESPERLINE, raw.length - i);
			encoded.append(encode(raw, i, length));
			encoded.append("\r\n");
		}

		return encoded.toString();
	}

	public static byte[] decode(String base64) throws InvalidBase64Exception
	{
		if(base64.length() == 0)
			return new byte[]{};
		
		try
		{
			char[] base64Chars = base64.toCharArray();
			int pad = 0;
			for(int i = base64Chars.length - 1; base64Chars[i] == '='; --i)
				pad++;

			int length = base64Chars.length * 6 / 8 - pad;
			byte[] raw = new byte[length];
			int rawIndex = 0;
			for(int i = 0; i < base64Chars.length; i += 4)
			{
				
				// NOTE: Optimized for speed
				byte byte0 = base64Decoder[base64Chars[i]];
				if(byte0 < 0)
					throw new InvalidBase64Exception();
				byte byte1 = base64Decoder[base64Chars[i+1]];
				if(byte1 < 0)
					throw new InvalidBase64Exception();
				byte byte2 = base64Decoder[base64Chars[i+2]];
				if(byte2 < 0)
					throw new InvalidBase64Exception();
				byte byte3 = base64Decoder[base64Chars[i+3]];
				if(byte3 < 0)
					throw new InvalidBase64Exception();
				int block =	(byte0 << 18) +	(byte1 << 12) +	(byte2 << 6) +	byte3;

				// NOTE: Optimized for speed
				if(rawIndex < length)
					raw[rawIndex++] = (byte)((block >> 16) & 0xff);
				if(rawIndex < length)
					raw[rawIndex++] = (byte)((block >> 8) & 0xff);
				if(rawIndex < length)
					raw[rawIndex++] = (byte)((block) & 0xff);
			}

			return raw;
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			throw new InvalidBase64Exception(e);
		}
	}

	public static byte[] decodeLineWrapped(String base64) throws InvalidBase64Exception
	{
		Vector decoded = new Vector();
		BufferedReader reader = new BufferedReader(new StringReader(base64));
		while (true)
		{
			String currentLine = null;
			try
			{
				currentLine = reader.readLine();
			}
			catch(Exception e)
			{
				//currentLine will be null
			}
			if(currentLine == null)
				break;
			byte[] readInBytes = decode(currentLine);
			for(int i = 0 ; i < readInBytes.length ; ++i)
			{
				decoded.add(new Byte(readInBytes[i]));
			}
		}
		byte[] decodedBytes = new byte[decoded.size()];
		for(int i = 0 ; i < decoded.size() ; ++i)
		{
			Byte b = (Byte)decoded.get(i);
			decodedBytes[i] = b.byteValue();
		}
		return decodedBytes;
	}

	public static File decodeToTempFile(String base64) throws IOException, InvalidBase64Exception
	{
		File tempFile = File.createTempFile("$$$Martus-base64decode", null);
		tempFile.deleteOnExit();

		OutputStream outputStream = new FileOutputStream(tempFile);
		outputStream.write(decode(base64));
		outputStream.close();

		return tempFile;
	}

	public static void encode(InputStream rawInput, Writer encodedOut) throws
		IOException
	{
		final int chunkSize = 3 * streamBufferCopySize;
		BufferedWriter writer = new BufferedWriter(encodedOut);
		BufferedInputStream in = new BufferedInputStream(rawInput);
		byte[] data = new byte[chunkSize];
		while(true)
		{
			int got = in.read(data);
			if(got < 1)
				break;

			String encoded = encode(data, 0, got);
			writer.write(encoded);
		}
		writer.flush();
	}

	public static void decode(Reader encodedIn, OutputStream rawOut) throws
		IOException,
		InvalidBase64Exception
	{
		BufferedReader bufferedIn = new BufferedReader(encodedIn);
		BufferedOutputStream bufferedOut = new BufferedOutputStream(rawOut);
		final int blockSize = 4;
		char[] data = new char[blockSize];
		while(true)
		{
			int got = bufferedIn.read(data);
			if(got < 1)
				break;

			if(got != data.length)
				throw new InvalidBase64Exception();

			byte[] bytes = decode(new String(data));
			bufferedOut.write(bytes);
		}
		bufferedOut.flush();
	}


	public static String readAllAndEncodeBase64(File templateFile) throws Exception
	{
		InputStream in = new FileInputStream(templateFile);
		try
		{
			StringWriter writer = new StringWriter();
			encode(in, writer);
			return writer.toString();
		}
		finally
		{
			in.close();
		}
	}
	
	protected static char[] encodeBlock(byte[] raw, int offset, int length)
	{
		int block = 0;
		int slack = length - 1;
		int end = (slack >= 2) ? 2 : slack;
		for(int i = 0; i <= end; ++i)
		{
			byte b = raw[offset + i];
			int neuter = (b < 0) ? b + 256 : b;
			block += neuter << (8 * (2 - i));
		}

		char[] base64 = new char[4];
		for(int i = 0; i < 4; ++i)
		{
			int sixBit = (block >>> (6 * (3 - i))) & 0x3f;
			base64[i] = getChar(sixBit);
		}

		if(slack < 1)
			base64[2] = '=';
		if(slack < 2)
			base64[3] = '=';

		return base64;
	}

	public static char getChar(int sixBit)
	{
		if(sixBit >= 0 && sixBit <= 25)
			return (char)('A' + sixBit);

		if(sixBit >= 26 && sixBit <= 51)
			return (char)('a' + (sixBit-26));

		if(sixBit >= 52 && sixBit <= 61)
			return (char)('0' + (sixBit-52));

		if(sixBit == 62)
			return '+';

		if(sixBit == 63)
			return '/';

		return '?';
	}

	public static byte getValue(char c) throws InvalidBase64Exception
	{
		int value = base64Decoder[c];
		if(value < 0)
			throw new InvalidBase64Exception();
		return (byte)value;
	}
	
	private static byte[] createBase64Decoder()
	{
		byte[] values = new byte[256];
		Arrays.fill(values, (byte)-1);
		for(byte alpha = 'A'; alpha <= 'Z'; ++alpha)
			values[alpha] = (byte)(alpha - 'A');
		for(byte alpha = 'a'; alpha <= 'z'; ++alpha)
			values[alpha] = (byte)(alpha - 'a' + 26);
		for(byte digit = '0'; digit <= '9'; ++digit)
			values[digit] = (byte)(digit - '0' + 52);
		values['+'] = 62;
		values['/'] = 63;
		values['='] = 0;
		
		return values;
	}
	
	private static final byte[] base64Decoder = createBase64Decoder();
	
	public final static int streamBufferCopySize = 1024;
	
}
