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

package org.martus.amplifier.common;

import java.util.StringTokenizer;

public class CharacterUtil
{	
	public static String removeRestrictCharacters(String str)
	{	
		if(str == null)
			return "";
		insideQuotes = false;
		char[] strArray = str.toCharArray();		
		for (int j=0; j<strArray.length;j++)
		{							
			if (!isAllowed(strArray[j]) && !insideQuotes)
				strArray[j] = ' ';													
		}									
										
		return removeExtraSpaces(new String(strArray));
	}
	
	private static String removeExtraSpaces(String str)
	{
		StringTokenizer st = new StringTokenizer(str);
		StringBuffer strBuffer = new StringBuffer();
		while (st.hasMoreTokens()) 
		{
			strBuffer.append(st.nextToken()).append(" ");
		}
		return strBuffer.toString().trim();
	}
	
	public static boolean isAllowed(char ch)
	{
		if (ch == '\"')
		{	
			insideQuotes = !insideQuotes;
			return true;
		}
		
		if (Character.isLetterOrDigit(ch)|| 
				ch == '\'' || ch >= 128)
			return true;
			
		return false;	
	}
	static boolean insideQuotes;
}
