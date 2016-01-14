/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
Technology, Inc. (Benetech).

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

import java.util.Arrays;

import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;

public class TestBase64 extends TestCaseEnhanced
{
	public TestBase64(String name)
	{
		super(name);
	}

	public void testOneByte() throws Exception 
	{
		byte[] simple = new byte[] {1};
		String simpleBase64 = StreamableBase64.encode(simple);
		assertEquals("Wrong simple base64?", "AQ==", simpleBase64);
	}
	
	public void testTwoBytes() throws Exception 
	{
		byte[] simple = new byte[] {1, (byte)128};
		String simpleBase64 = StreamableBase64.encode(simple);
		assertEquals("Wrong simple base64?", "AYA=", simpleBase64);
	}
	
	public void testThreeBytes() throws Exception 
	{
		byte[] simple = new byte[] {0, 127, (byte)255};
		String simpleBase64 = StreamableBase64.encode(simple);
		assertEquals("Wrong simple base64?", "AH//", simpleBase64);
	}
	
	public void testFourBytes() throws Exception 
	{
		byte[] simple = new byte[] {0, 0, 0, 0};
		String simpleBase64 = StreamableBase64.encode(simple);
		assertEquals("Wrong simple base64?", "AAAAAA==", simpleBase64);
	}
	
	public void testFiveBytes() throws Exception 
	{
		byte[] simple = new byte[] {(byte)255, (byte)255, (byte)255, (byte)255, (byte)255};
		String simpleBase64 = StreamableBase64.encode(simple);
		assertEquals("Wrong simple base64?", "//////8=", simpleBase64);
	}
	
	public void testFull() throws Exception
	{
		byte[] full = new byte[256];
		for(int i = 0; i < full.length; ++i)
			full[i] = (byte)i;
		String fullBase64 = StreamableBase64.encode(full);
		assertTrue("Wrong full base64?", Arrays.equals(full, StreamableBase64.decode(fullBase64)));
	}
}
