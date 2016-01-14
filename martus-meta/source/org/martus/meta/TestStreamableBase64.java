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

import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;


public class TestStreamableBase64 extends TestCaseEnhanced
{
	public TestStreamableBase64(String name)
	{
		super(name);
	}
	
	public void testEncryptDecrypt() throws Exception
	{
		String data = "hello";
		String encrypted = StreamableBase64.encode(data);
		String encrypted2 = StreamableBase64.encode(data.getBytes("UTF-8"));
		assertEquals("Not the same?", encrypted, encrypted2);
		
		byte[] decrypted = StreamableBase64.decode(encrypted);
		assertEquals("decoded data not equal?", data, new String(decrypted));
	}

	public void testEncryptDecryptEmptyString() throws Exception
	{
		String data = "";
		String encrypted = StreamableBase64.encode(data);
		String encrypted2 = StreamableBase64.encode(data.getBytes("UTF-8"));
		assertEquals("Not the same?", encrypted, encrypted2);
		byte[] decrypted = StreamableBase64.decode(encrypted);
		assertEquals("decoded enpty data not equal?", data, new String(decrypted));
	}
}