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

package org.martus.amplifier.lucene.test;

import java.io.StringReader;

import org.martus.amplifier.lucene.AlphanumericTokenizer;
import org.martus.common.test.UnicodeConstants;
import org.martus.util.TestCaseEnhanced;


public class TestAlphanumericTokenizer extends TestCaseEnhanced
{

	public TestAlphanumericTokenizer(String name)
	{
		super(name);
	}

	public void testIsTokenChar()
	{
		AlphanumericTokenizer tokenizer = new AlphanumericTokenizer(new StringReader(""));
		assertFalse("punctuation is a token?", tokenizer.isTokenChar('-'));
		assertFalse("space is a token?", tokenizer.isTokenChar(' '));
		assertTrue("upper case not a token?", tokenizer.isTokenChar('A'));
		assertTrue("lower case not a token?", tokenizer.isTokenChar('b'));
		assertTrue("digit not a token?", tokenizer.isTokenChar('3'));
		assertTrue("foreign upper letter not a token?", tokenizer.isTokenChar(UnicodeConstants.ACCENT_E_UPPER));
		assertTrue("foreign lower letter not a token?", tokenizer.isTokenChar(UnicodeConstants.TILDE_N_LOWER));

		// TODO: Add checks for other foreign characters and Thai word breaks
	}
	
	public void testNormalize()
	{
		verifyNormalize("lower to lower", 'a', 'a');
		verifyNormalize("upper to lower", 'B', 'b');
		verifyNormalize("foreign lower to lower", UnicodeConstants.TILDE_N_LOWER, UnicodeConstants.TILDE_N_LOWER);
		verifyNormalize("upper to lower", UnicodeConstants.ACCENT_E_UPPER, UnicodeConstants.ACCENT_E_LOWER);
		verifyNormalize("digit", '3', '3');
		verifyNormalize("punctuation", '-', '-');
		verifyNormalize("space", ' ', ' ');

		// TODO: Add checks for other foreign characters and Thai word breaks
	}
	
	void verifyNormalize(String text, char input, char expected)
	{
		AlphanumericTokenizer tokenizer = new AlphanumericTokenizer(new StringReader(""));
		assertEquals(text, expected, tokenizer.normalize(input));
	}
}
