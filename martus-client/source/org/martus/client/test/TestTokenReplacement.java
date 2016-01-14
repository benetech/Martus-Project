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

package org.martus.client.test;

import java.util.HashMap;

import org.martus.util.TestCaseEnhanced;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class TestTokenReplacement extends TestCaseEnhanced
{
	public TestTokenReplacement(String name)
	{
		super(name);
	}

	public void testReplaceTokens() throws Exception
	{
		try 
		{
			TokenReplacement.replaceTokens("",null);
			fail("Null map is invalid and should throw.");
		} 
		catch (RuntimeException expected) 
		{
		}

		String nothingToReplace = "nothing to replace";
		HashMap empty = new HashMap();
		String result = TokenReplacement.replaceTokens(nothingToReplace, empty);
		assertEquals("Nothing to replace why didn't strings match?", nothingToReplace, result);

		String token = "#dd#";
		String replacementValue = "but this";
		String replaceOneItem = "nothing " + token + " to replace";
		HashMap map = new HashMap();
		map.put(token, replacementValue);
		result = TokenReplacement.replaceTokens(replaceOneItem, map);
		assertNotEquals("Did not replace token?", replaceOneItem, result);
		assertEquals("Incorrect replacement?", "nothing but this to replace", result);

		String token2 = "#ff#";
		String replacementValue2 = "when all is said and done.";
		String replaceTwoItem = replaceOneItem + " " + token2;
		map.put(token2, replacementValue2);
		result = TokenReplacement.replaceTokens(replaceTwoItem, map);
		assertNotEquals("Did not replace both tokens?", replaceTwoItem, result);
		assertEquals("Incorrect replacement of two tokens?", "nothing but this to replace when all is said and done.", result);
		
		String[] stringOriginalArray = {"#a#","#unknown#","#c#"};
		HashMap tokens = new HashMap();
		tokens.put("#a#","1");
		tokens.put("#c#","3");
		
		String[] arrayResult = TokenReplacement.replaceTokens(stringOriginalArray, tokens);
		String[] expectedArray = {"1", "#unknown#", "3"};
		assertEquals("Replace Tokens for an array[0] of strings not replaced?", expectedArray[0], arrayResult[0]);
		assertEquals("an unknown token in array[1] of strings was replaced?", expectedArray[1], arrayResult[1]);
		assertEquals("Replace Tokens for an array[2] of strings not replaced?", expectedArray[2], arrayResult[2]);
	}

	public void testReplaceToken() throws Exception
	{
		try 
		{
			TokenReplacement.replaceToken("",null,"a");
			fail("Null token is invalid and should throw.");
		} 
		catch (RuntimeException expected) 
		{
		}
		String original = "the count was ";
		String count = "3";
		String token = "#N#";
		String expected = original + count;
		String originalWithToken = original + token;
		assertEquals(expected, TokenReplacement.replaceToken(originalWithToken, token, count));
	}
	
	public void testReplaceTokensRegExpression() throws Exception
	{
		
		String[] stringOriginalArray = {"*a*","#unknown#","'c*"};
		HashMap tokens = new HashMap();
		tokens.put("*a*","1");
		
		try
		{
			TokenReplacement.replaceTokens(stringOriginalArray, tokens);
			fail("Should have thrown since token is not a reg expression");
		}
		catch (TokenInvalidException expected)
		{
		}
	}
}
