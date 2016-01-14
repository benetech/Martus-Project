/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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

import org.martus.common.DammCheckDigitAlgorithm;
import org.martus.common.DammCheckDigitAlgorithm.CheckDigitInvalidException;
import org.martus.util.TestCaseEnhanced;

public class TestDammCheckDigitAlgorithm  extends TestCaseEnhanced
{

    public TestDammCheckDigitAlgorithm(String name)
	{
		super(name);
	}

	public void testBasics() throws CheckDigitInvalidException
    {
    	DammCheckDigitAlgorithm testDammCheckAlgorithm = new DammCheckDigitAlgorithm();

		String tokenData = "572";
		String checkDigit = testDammCheckAlgorithm.getCheckDigit(tokenData);
		assertEquals("For data 572 the check digit should be 4", "4", checkDigit);
		assertTrue("For complete token data 5724 should be true", testDammCheckAlgorithm.isTokenValid("5724"));

		tokenData = "3841590";
		checkDigit = testDammCheckAlgorithm.getCheckDigit(tokenData);
		assertEquals("For data 3841590 the check digit should be 0", "0", checkDigit);
		String validCompleteToken = tokenData + checkDigit;
		assertTrue("38415900 For complete token data 38415900 should be true", testDammCheckAlgorithm.isTokenValid(validCompleteToken));
		assertTrue("3841590 Is also a valid token, But user may have forgotten one digit.  Length checking will need to happen in Client.", testDammCheckAlgorithm.isTokenValid(tokenData));
		
		String singleTransposedDigit = validCompleteToken;
		singleTransposedDigit = singleTransposedDigit.replace('1', '7');
		assertEquals("For a single transposed digit token should equal 38475900", "38475900", singleTransposedDigit);
		assertFalse("For a single transposed digit token 38475900 should be false", testDammCheckAlgorithm.isTokenValid(singleTransposedDigit));
		
		String doubleTransposedDigits = validCompleteToken;
		doubleTransposedDigits = doubleTransposedDigits.replace('0', '1');
		assertEquals("For a double transposed digits token should equal 38415911", "38415911", doubleTransposedDigits);
		assertFalse("For a double transposed digits token 38415911 should be false", testDammCheckAlgorithm.isTokenValid(doubleTransposedDigits));

		String singleDigitTokenIsInvalid = "0";
		assertFalse("A 1 digit token can not be valid since we need a check digit.", testDammCheckAlgorithm.isTokenValid(singleDigitTokenIsInvalid));
		
		String twoDigitTokenValid = "00";
		assertTrue("A 2 digit valid token should be valid.", testDammCheckAlgorithm.isTokenValid(twoDigitTokenValid));
		
		String nineDigitTokenValid = "000000000";
		assertTrue("A 9 digit valid token should be valid.", testDammCheckAlgorithm.isTokenValid(nineDigitTokenValid));
    }
	
	public void testTable() throws CheckDigitInvalidException
	{
		DammCheckDigitAlgorithm dammAlgorithm = new DammCheckDigitAlgorithm();
		//Interm Row 0 =	 0 	3 	1 	7 	5 	9 	8 	6 	4 	2
		assertEquals("Number 0, with Interm 0 should 0", 0, dammAlgorithm.getCheckDigitforIndex(0, 0));
		assertEquals("Number 1, with Interm 0 should 3", 3, dammAlgorithm.getCheckDigitforIndex(1, 0));
		assertEquals("Number 2, with Interm 0 should 1", 1, dammAlgorithm.getCheckDigitforIndex(2, 0));
		assertEquals("Number 3, with Interm 0 should 7", 7, dammAlgorithm.getCheckDigitforIndex(3, 0));
		assertEquals("Number 4, with Interm 0 should 5", 5, dammAlgorithm.getCheckDigitforIndex(4, 0));
		assertEquals("Number 5, with Interm 0 should 9", 9, dammAlgorithm.getCheckDigitforIndex(5, 0));
		assertEquals("Number 6, with Interm 0 should 8", 8, dammAlgorithm.getCheckDigitforIndex(6, 0));
		assertEquals("Number 7, with Interm 0 should 6", 6, dammAlgorithm.getCheckDigitforIndex(7, 0));
		assertEquals("Number 8, with Interm 0 should 4", 4, dammAlgorithm.getCheckDigitforIndex(8, 0));
		assertEquals("Number 9, with Interm 0 should 2", 2, dammAlgorithm.getCheckDigitforIndex(9, 0));
		
		//Interm Row 1 = 	7 	0 	9 	2 	1 	5 	4 	8 	6 	3
		assertEquals("Number 0, with Interm 1 should 7", 7, dammAlgorithm.getCheckDigitforIndex(0, 1));
		assertEquals("Number 1, with Interm 1 should 0", 0, dammAlgorithm.getCheckDigitforIndex(1, 1));
		assertEquals("Number 2, with Interm 1 should 9", 9, dammAlgorithm.getCheckDigitforIndex(2, 1));
		assertEquals("Number 3, with Interm 1 should 2", 2, dammAlgorithm.getCheckDigitforIndex(3, 1));
		assertEquals("Number 4, with Interm 1 should 1", 1, dammAlgorithm.getCheckDigitforIndex(4, 1));
		assertEquals("Number 5, with Interm 1 should 5", 5, dammAlgorithm.getCheckDigitforIndex(5, 1));
		assertEquals("Number 6, with Interm 1 should 4", 4, dammAlgorithm.getCheckDigitforIndex(6, 1));
		assertEquals("Number 7, with Interm 1 should 8", 8, dammAlgorithm.getCheckDigitforIndex(7, 1));
		assertEquals("Number 8, with Interm 1 should 6", 6, dammAlgorithm.getCheckDigitforIndex(8, 1));
		assertEquals("Number 9, with Interm 1 should 3", 3, dammAlgorithm.getCheckDigitforIndex(9, 1));

		//Interm Row 2 = 	4 	2 	0 	6 	8 	7 	1 	3 	5 	9
		assertEquals("Number 0, with Interm 2 should 4", 4, dammAlgorithm.getCheckDigitforIndex(0, 2));
		assertEquals("Number 1, with Interm 2 should 2", 2, dammAlgorithm.getCheckDigitforIndex(1, 2));
		assertEquals("Number 2, with Interm 2 should 0", 0, dammAlgorithm.getCheckDigitforIndex(2, 2));
		assertEquals("Number 3, with Interm 2 should 6", 6, dammAlgorithm.getCheckDigitforIndex(3, 2));
		assertEquals("Number 4, with Interm 2 should 8", 8, dammAlgorithm.getCheckDigitforIndex(4, 2));
		assertEquals("Number 5, with Interm 2 should 7", 7, dammAlgorithm.getCheckDigitforIndex(5, 2));
		assertEquals("Number 6, with Interm 2 should 1", 1, dammAlgorithm.getCheckDigitforIndex(6, 2));
		assertEquals("Number 7, with Interm 2 should 3", 3, dammAlgorithm.getCheckDigitforIndex(7, 2));
		assertEquals("Number 8, with Interm 2 should 5", 5, dammAlgorithm.getCheckDigitforIndex(8, 2));
		assertEquals("Number 9, with Interm 2 should 9", 9, dammAlgorithm.getCheckDigitforIndex(9, 2));
		
	 	//Interm Row 3 = 	1 	7 	5 	0 	9 	8 	3 	4 	2 	6
		assertEquals("Number 0, with Interm 3 should 1", 1, dammAlgorithm.getCheckDigitforIndex(0, 3));
		assertEquals("Number 1, with Interm 3 should 7", 7, dammAlgorithm.getCheckDigitforIndex(1, 3));
		assertEquals("Number 2, with Interm 3 should 5", 5, dammAlgorithm.getCheckDigitforIndex(2, 3));
		assertEquals("Number 3, with Interm 3 should 0", 0, dammAlgorithm.getCheckDigitforIndex(3, 3));
		assertEquals("Number 4, with Interm 3 should 9", 9, dammAlgorithm.getCheckDigitforIndex(4, 3));
		assertEquals("Number 5, with Interm 3 should 8", 8, dammAlgorithm.getCheckDigitforIndex(5, 3));
		assertEquals("Number 6, with Interm 3 should 3", 3, dammAlgorithm.getCheckDigitforIndex(6, 3));
		assertEquals("Number 7, with Interm 3 should 4", 4, dammAlgorithm.getCheckDigitforIndex(7, 3));
		assertEquals("Number 8, with Interm 3 should 2", 2, dammAlgorithm.getCheckDigitforIndex(8, 3));
		assertEquals("Number 9, with Interm 3 should 6", 6, dammAlgorithm.getCheckDigitforIndex(9, 3));

		//Interm Row 4 = 	6 	1 	2 	3 	0 	4 	5 	9 	7 	8
		assertEquals("Number 0, with Interm 4 should 6", 6, dammAlgorithm.getCheckDigitforIndex(0, 4));
		assertEquals("Number 1, with Interm 4 should 1", 1, dammAlgorithm.getCheckDigitforIndex(1, 4));
		assertEquals("Number 2, with Interm 4 should 2", 2, dammAlgorithm.getCheckDigitforIndex(2, 4));
		assertEquals("Number 3, with Interm 4 should 3", 3, dammAlgorithm.getCheckDigitforIndex(3, 4));
		assertEquals("Number 4, with Interm 4 should 0", 0, dammAlgorithm.getCheckDigitforIndex(4, 4));
		assertEquals("Number 5, with Interm 4 should 4", 4, dammAlgorithm.getCheckDigitforIndex(5, 4));
		assertEquals("Number 6, with Interm 4 should 5", 5, dammAlgorithm.getCheckDigitforIndex(6, 4));
		assertEquals("Number 7, with Interm 4 should 9", 9, dammAlgorithm.getCheckDigitforIndex(7, 4));
		assertEquals("Number 8, with Interm 4 should 7", 7, dammAlgorithm.getCheckDigitforIndex(8, 4));
		assertEquals("Number 9, with Interm 4 should 8", 8, dammAlgorithm.getCheckDigitforIndex(9, 4));

		//Interm Row 5 = 	3 	6 	7 	4 	2 	0 	9 	5 	8 	1
		assertEquals("Number 0, with Interm 5 should 3", 3, dammAlgorithm.getCheckDigitforIndex(0, 5));
		assertEquals("Number 1, with Interm 5 should 6", 6, dammAlgorithm.getCheckDigitforIndex(1, 5));
		assertEquals("Number 2, with Interm 5 should 7", 7, dammAlgorithm.getCheckDigitforIndex(2, 5));
		assertEquals("Number 3, with Interm 5 should 4", 4, dammAlgorithm.getCheckDigitforIndex(3, 5));
		assertEquals("Number 4, with Interm 5 should 2", 2, dammAlgorithm.getCheckDigitforIndex(4, 5));
		assertEquals("Number 5, with Interm 5 should 0", 0, dammAlgorithm.getCheckDigitforIndex(5, 5));
		assertEquals("Number 6, with Interm 5 should 9", 9, dammAlgorithm.getCheckDigitforIndex(6, 5));
		assertEquals("Number 7, with Interm 5 should 5", 5, dammAlgorithm.getCheckDigitforIndex(7, 5));
		assertEquals("Number 8, with Interm 5 should 8", 8, dammAlgorithm.getCheckDigitforIndex(8, 5));
		assertEquals("Number 9, with Interm 5 should 1", 1, dammAlgorithm.getCheckDigitforIndex(9, 5));

		//Interm Row 6 = 	5 	8 	6 	9 	7 	2 	0 	1 	3 	4
		assertEquals("Number 0, with Interm 6 should 5", 5, dammAlgorithm.getCheckDigitforIndex(0, 6));
		assertEquals("Number 1, with Interm 6 should 8", 8, dammAlgorithm.getCheckDigitforIndex(1, 6));
		assertEquals("Number 2, with Interm 6 should 6", 6, dammAlgorithm.getCheckDigitforIndex(2, 6));
		assertEquals("Number 3, with Interm 6 should 9", 9, dammAlgorithm.getCheckDigitforIndex(3, 6));
		assertEquals("Number 4, with Interm 6 should 7", 7, dammAlgorithm.getCheckDigitforIndex(4, 6));
		assertEquals("Number 5, with Interm 6 should 2", 2, dammAlgorithm.getCheckDigitforIndex(5, 6));
		assertEquals("Number 6, with Interm 6 should 0", 0, dammAlgorithm.getCheckDigitforIndex(6, 6));
		assertEquals("Number 7, with Interm 6 should 1", 1, dammAlgorithm.getCheckDigitforIndex(7, 6));
		assertEquals("Number 8, with Interm 6 should 3", 3, dammAlgorithm.getCheckDigitforIndex(8, 6));
		assertEquals("Number 9, with Interm 6 should 4", 4, dammAlgorithm.getCheckDigitforIndex(9, 6));

		//Interm Row 7 = 	8 	9 	4 	5 	3 	6 	2 	0 	1 	7
		assertEquals("Number 0, with Interm 7 should 8", 8, dammAlgorithm.getCheckDigitforIndex(0, 7));
		assertEquals("Number 1, with Interm 7 should 9", 9, dammAlgorithm.getCheckDigitforIndex(1, 7));
		assertEquals("Number 2, with Interm 7 should 4", 4, dammAlgorithm.getCheckDigitforIndex(2, 7));
		assertEquals("Number 3, with Interm 7 should 5", 5, dammAlgorithm.getCheckDigitforIndex(3, 7));
		assertEquals("Number 4, with Interm 7 should 3", 3, dammAlgorithm.getCheckDigitforIndex(4, 7));
		assertEquals("Number 5, with Interm 7 should 6", 6, dammAlgorithm.getCheckDigitforIndex(5, 7));
		assertEquals("Number 6, with Interm 7 should 2", 2, dammAlgorithm.getCheckDigitforIndex(6, 7));
		assertEquals("Number 7, with Interm 7 should 0", 0, dammAlgorithm.getCheckDigitforIndex(7, 7));
		assertEquals("Number 8, with Interm 7 should 1", 1, dammAlgorithm.getCheckDigitforIndex(8, 7));
		assertEquals("Number 9, with Interm 7 should 7", 7, dammAlgorithm.getCheckDigitforIndex(9, 7));

		//Interm Row 8 = 	9 	4 	3 	8 	6 	1 	7 	2 	0 	5
		assertEquals("Number 0, with Interm 8 should 9", 9, dammAlgorithm.getCheckDigitforIndex(0, 8));
		assertEquals("Number 1, with Interm 8 should 4", 4, dammAlgorithm.getCheckDigitforIndex(1, 8));
		assertEquals("Number 2, with Interm 8 should 3", 3, dammAlgorithm.getCheckDigitforIndex(2, 8));
		assertEquals("Number 3, with Interm 8 should 8", 8, dammAlgorithm.getCheckDigitforIndex(3, 8));
		assertEquals("Number 4, with Interm 8 should 6", 6, dammAlgorithm.getCheckDigitforIndex(4, 8));
		assertEquals("Number 5, with Interm 8 should 1", 1, dammAlgorithm.getCheckDigitforIndex(5, 8));
		assertEquals("Number 6, with Interm 8 should 7", 7, dammAlgorithm.getCheckDigitforIndex(6, 8));
		assertEquals("Number 7, with Interm 8 should 2", 2, dammAlgorithm.getCheckDigitforIndex(7, 8));
		assertEquals("Number 8, with Interm 8 should 0", 0, dammAlgorithm.getCheckDigitforIndex(8, 8));
		assertEquals("Number 9, with Interm 8 should 5", 5, dammAlgorithm.getCheckDigitforIndex(9, 8));

		//Interm Row 9 = 	2 	5 	8 	1 	4 	3 	6 	7 	9 	0
		assertEquals("Number 0, with Interm 9 should 2", 2, dammAlgorithm.getCheckDigitforIndex(0, 9));
		assertEquals("Number 1, with Interm 9 should 5", 5, dammAlgorithm.getCheckDigitforIndex(1, 9));
		assertEquals("Number 2, with Interm 9 should 8", 8, dammAlgorithm.getCheckDigitforIndex(2, 9));
		assertEquals("Number 3, with Interm 9 should 1", 1, dammAlgorithm.getCheckDigitforIndex(3, 9));
		assertEquals("Number 4, with Interm 9 should 4", 4, dammAlgorithm.getCheckDigitforIndex(4, 9));
		assertEquals("Number 5, with Interm 9 should 3", 3, dammAlgorithm.getCheckDigitforIndex(5, 9));
		assertEquals("Number 6, with Interm 9 should 6", 6, dammAlgorithm.getCheckDigitforIndex(6, 9));
		assertEquals("Number 7, with Interm 9 should 7", 7, dammAlgorithm.getCheckDigitforIndex(7, 9));
		assertEquals("Number 8, with Interm 9 should 9", 9, dammAlgorithm.getCheckDigitforIndex(8, 9));
		assertEquals("Number 9, with Interm 9 should 0", 0, dammAlgorithm.getCheckDigitforIndex(9, 9));
		
		//invalid args
		try
		{
			dammAlgorithm.getCheckDigitforIndex(9, -2);
			fail("Number less than 0, with Interm 9 should throw an error");
		}
		catch(CheckDigitInvalidException expectedException)
		{
		}
		
		try
		{
			dammAlgorithm.getCheckDigitforIndex(-3, 2);
			fail("valid number, with Interm less than 0 should throw an error");
		}
		catch(CheckDigitInvalidException expectedException)
		{
		}

		try
		{
			dammAlgorithm.getCheckDigitforIndex(-10, -22);
			fail("Number less than 0, with Interm less than 0 should throw an error");
		}
		catch(CheckDigitInvalidException expectedException)
		{
		}

		try
		{
			dammAlgorithm.getCheckDigitforIndex(5, 10);
			fail("Number greater than 9, with Interm valid should throw an error");
		}
		catch(CheckDigitInvalidException expectedException)
		{
		}

		try
		{
			dammAlgorithm.getCheckDigitforIndex(10, 4);
			fail("Number valid, with Interm greater than 9 should throw an error");
		}
		catch(CheckDigitInvalidException expectedException)
		{
		}

		try
		{
			dammAlgorithm.getCheckDigitforIndex(19, 12);
			fail("Number greater than 9, with Interm greater than 9 should throw an error");
		}
		catch(CheckDigitInvalidException expectedException)
		{
		}
	}    

}
