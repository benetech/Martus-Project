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

/*
 The quasigroup table from http://en.wikipedia.org/wiki/Damm_algorithm
		    0 	1 	2 	3 	4 	5 	6 	7 	8 	9
	 	0 	0 	3 	1 	7 	5 	9 	8 	6 	4 	2
	 	1 	7 	0 	9 	2 	1 	5 	4 	8 	6 	3
	 	2 	4 	2 	0 	6 	8 	7 	1 	3 	5 	9
	 	3 	1 	7 	5 	0 	9 	8 	3 	4 	2 	6
	 	4 	6 	1 	2 	3 	0 	4 	5 	9 	7 	8
	 	5 	3 	6 	7 	4 	2 	0 	9 	5 	8 	1
	 	6 	5 	8 	6 	9 	7 	2 	0 	1 	3 	4
	 	7 	8 	9 	4 	5 	3 	6 	2 	0 	1 	7
	 	8 	9 	4 	3 	8 	6 	1 	7 	2 	0 	5
	 	9 	2 	5 	8 	1 	4 	3 	6 	7 	9 	0
*/
//3841590
//7,1,1,0,9,0,0
package org.martus.common;

public class DammCheckDigitAlgorithm
{

	public static class CheckDigitInvalidException extends Exception 
	{
	}

	public int getCheckDigitforIndex(int single, int previous) throws CheckDigitInvalidException
	{
		if(single < MIN_DIGIT || single > MAX_DIGIT)
			throw new CheckDigitInvalidException();
		if(previous < MIN_DIGIT || previous > MAX_DIGIT)
			throw new CheckDigitInvalidException();
		return quasiGroupTable[previous][single];
	}

	public String getCheckDigit(String originalData) throws CheckDigitInvalidException
	{
		int checkDigit = 0;
		for(int i = 0; i < originalData.length(); ++i)
		{
			int currentNumber = originalData.charAt(i)-'0';
			checkDigit = getCheckDigitforIndex(currentNumber, checkDigit);
		}
		
		return Integer.toString(checkDigit);
	}
	
	public boolean isTokenValid(String token)
	{
		int length = token.length();
		if(length <= 1)
			return false;
		String tokenDataOnly = token.substring(0,length-1);
		char checkDigitOriginal = token.charAt(length-1);
		
		String checkDigitReturned = "";
		try
		{
			checkDigitReturned = getCheckDigit(tokenDataOnly);
		} 
		catch (CheckDigitInvalidException e)
		{
			return false;
		}
		char charCheckDigitReturned = checkDigitReturned.charAt(0);
		return checkDigitOriginal == charCheckDigitReturned;
	}

	final private int[][] quasiGroupTable = {	{0,3,1,7,5,9,8,6,4,2}, 
			{7,0,9,2,1,5,4,8,6,3}, 
			{4,2,0,6,8,7,1,3,5,9}, 
			{1,7,5,0,9,8,3,4,2,6},
			{6,1,2,3,0,4,5,9,7,8},
			{3,6,7,4,2,0,9,5,8,1},
			{5,8,6,9,7,2,0,1,3,4},
			{8,9,4,5,3,6,2,0,1,7},
			{9,4,3,8,6,1,7,2,0,5},
			{2,5,8,1,4,3,6,7,9,0}
		};
	
	final private static int MIN_DIGIT = 0;
	final private static int MAX_DIGIT = 9;
}
