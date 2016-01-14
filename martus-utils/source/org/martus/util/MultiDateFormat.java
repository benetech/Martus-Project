/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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

import java.text.DecimalFormat;
import java.util.HashMap;


public class MultiDateFormat
{
	public MultiDateFormat(DatePreference datePref)
	{
		datePreference = datePref;
	}
	
	public String format(int localizedYear, int localizedMonth, int localizedDay)
	{
		String mdyOrder = datePreference.getMdyOrderForText();
		return format(mdyOrder, localizedYear, localizedMonth, localizedDay);
	}

	public String formatIgnoringRightToLeft(int localizedYear, int localizedMonth, int localizedDay)
	{
		String mdyOrder = datePreference.getMdyOrder();
		return format(mdyOrder, localizedYear, localizedMonth, localizedDay);
	}

	private String format(String mdyOrder, int localizedYear, int localizedMonth, int localizedDay)
	{
		String delimiter = new String(new char[] {datePreference.getDelimiter()});
		return format(mdyOrder, delimiter, localizedYear, localizedMonth, localizedDay);
	}

	public static String format(String mdyOrder, String delimiter, int localizedYear, int localizedMonth, int localizedDay)
	{
		Part year = new Part(fourDigit, localizedYear);
		Part month = new Part(twoDigit, localizedMonth);
		Part day = new Part(twoDigit, localizedDay);
		
		HashMap codeToPart = new HashMap();
		codeToPart.put(new Character('y'), year);
		codeToPart.put(new Character('m'), month);
		codeToPart.put(new Character('d'), day);
		
		StringBuffer result = new StringBuffer();
		for(int i = 0; i < mdyOrder.length(); ++i)
		{
			if(i > 0)
				result.append(delimiter);
			Character thisPartCode = new Character(mdyOrder.charAt(i));
			Part thisPart = (Part)codeToPart.get(thisPartCode);
			result.append(thisPart.format.format(thisPart.value));
		}
		
		return new String(result);
	}
	
	static class Part
	{
		public Part(DecimalFormat formatToUse, int valueToUse)
		{
			format = formatToUse;
			value = valueToUse;
		}
		
		DecimalFormat format;
		int value;
	}
	
	private static DecimalFormat fourDigit = new DecimalFormat("0000");
	private static DecimalFormat twoDigit = new DecimalFormat("00");

	DatePreference datePreference;
}
