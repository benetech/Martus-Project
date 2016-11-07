/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

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
package org.martus.client.bulletinstore.converters;

import org.martus.util.TestCaseEnhanced;

public class TestCsvEncoder extends TestCaseEnhanced
{
	public TestCsvEncoder(String name)
	{
		super(name);
	}

	public void testEncodeValue() 
	{
		verifyEncodedValue("", "");
		verifyEncodedValue("random text", "random text");
		verifyEncodedValue("\"random, text\"", "random, text");
		verifyEncodedValue("\"random with \"\"inside quotes text\"\", text\"", "random with \"inside quotes text\", text");
		verifyEncodedValue("\"inside quotes\"", "\"inside quotes\"");
		verifyEncodedValue("\"inside \"nested quotes\" quotes\"", "\"inside \"nested quotes\" quotes\"");
		verifyEncodedValue("\"inside \"nested quotes, with delimeter\" quotes\"", "\"inside \"nested quotes, with delimeter\" quotes\"");
	}
	
	public void testDecodeCsvValue()
	{
		verifyDecodedValue(null, null);
		verifyDecodedValue("", "");
		verifyDecodedValue("some random value", "some random value");
		verifyDecodedValue("some \"inside quotes\" value", "some \"inside quotes\" value");
		verifyDecodedValue("\"some, quoted value\" in the begining of sentence", "\"some, quoted value\" in the begining of sentence");
		verifyDecodedValue("value at end is wrapped \"in quotes\"", "value at end is wrapped \"in quotes\"");
		verifyDecodedValue("entire value inside quotes", "\"entire value inside quotes\"");
	}

	private void verifyDecodedValue(String expectedValue, String valueToDecode)
	{
		assertEquals("Value was not decoded correctly?", expectedValue, CsvEncoder.decodeValue(valueToDecode));
	}

	private void verifyEncodedValue(String expectedValue, String rawValueToEncode)
	{
		assertEquals("Value was not encoded correctly?", expectedValue, CsvEncoder.encodeValue(rawValueToEncode, COMMA_DELIMITER));
	}
	
	private static final String COMMA_DELIMITER = ",";
}
