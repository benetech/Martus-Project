/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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


public class TestUnicodeUtilities extends TestCaseEnhanced 
{
	public TestUnicodeUtilities(String name) 
	{
		super(name);
	}

	public void testBasics() throws Exception
	{
		byte[] rawData = {1,88,24,62,0,0,1,2,23};
		String nonUnicodeString = rawData.toString();
		String unicodeString = UnicodeUtilities.toUnicodeString(rawData);
		byte[] unicodeBytes = UnicodeUtilities.toUnicodeBytes(unicodeString);
		byte[] nonUnicodeBytes = UnicodeUtilities.toUnicodeBytes(nonUnicodeString);
		
		assertFalse(nonUnicodeString.equals(unicodeString));
		assertTrue(verifyBytes(rawData, unicodeBytes));
		assertFalse(verifyBytes(rawData,nonUnicodeBytes));
		assertFalse(verifyBytes(unicodeBytes, nonUnicodeBytes));
		
		byte[] manuallyConvertedUnicodeBytes = unicodeString.getBytes("UTF-8");
		assertTrue(verifyBytes(unicodeBytes, manuallyConvertedUnicodeBytes));
		
		String manuallyConvertedString = new String(unicodeBytes, "UTF-8");
		assertTrue(unicodeString.equals(manuallyConvertedString));
	}
	
	boolean verifyBytes (byte[] data1, byte[] data2)
	{
		if(data1.length != data2.length)
			return false;
		for(int i = 0; i < data1.length; ++i)
		{
			if(data1[i] != data2[i])
				return false;
		}
		return true;
	}
}
