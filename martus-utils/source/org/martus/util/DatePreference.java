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

import org.martus.util.language.LanguageOptions;

public class DatePreference
{
	public DatePreference()
	{
		this(DEFAULT_DATE_MDY_ORDER, DEFAULT_DATE_DELIMITER);
	}
	
	public DatePreference(String mdyOrder, char delimiter)
	{
		setMdyOrder(mdyOrder);
		setDelimiter(delimiter);
	}
	
	public void fillFrom(DatePreference other)
	{
		setMdyOrder(other.getMdyOrder());
		setDelimiter(other.getDelimiter());
	}
	
	public String getMdyOrder()
	{
		return mdyOrder;
	}
	
	public String getMdyOrderForText()
	{
		char[] mdy = getMdyOrder().toCharArray();
		if(LanguageOptions.isRightToLeftLanguage())
		{
			char first = mdy[0];
			mdy[0] = mdy[2];
			mdy[2] = first;
		}
		return new String(mdy);
	}
	
	public void setMdyOrder(String newOrder)
	{
		mdyOrder = newOrder;
	}
	
	public char getDelimiter()
	{
		return delimiter;
	}
	
	public void setDelimiter(char newDelimiter)
	{
		delimiter = newDelimiter;
	}
	
	public String getDateTemplate()
	{
		return buildTemplate(getMdyOrderForText());
	}

	public String getRawDateTemplate()
	{
		return buildTemplate(getMdyOrder());
	}

	private String buildTemplate(String mdyToUse)
	{
		char[] mdy = mdyToUse.toCharArray();
		int at = 0;
		StringBuffer template = new StringBuffer();

		template.append(getTemplateField(mdy[at++]));
		template.append(getDelimiter());
		template.append(getTemplateField(mdy[at++]));
		template.append(getDelimiter());
		template.append(getTemplateField(mdy[at++]));
		
		return template.toString();
	}
	
	public void setDateTemplate(String template) throws Exception
	{
		setMdyOrder(detectMdyOrder(template));
		setDelimiter(detectDelimiter(template));
	}
	
	private static String detectMdyOrder(String template)
	{
		String result = "";
		template = template.toLowerCase();
		for(int i = 0; i < template.length(); ++i)
		{
			char c = template.charAt(i);
			if( (c != 'm' && c != 'd' && c != 'y'))
				continue;
			
			int alreadyAt = result.lastIndexOf(c);
			if(result.length() > 0 && alreadyAt == result.length() - 1)
				continue;
			
			if(alreadyAt >= 0)
				throw new RuntimeException("Duplicate date field in template: " + result);
			
			result += c;
		}

		if(result.length() != 3)
			throw new RuntimeException("Missing date field in template: " + result);
		
		return result;
	}

	private static char detectDelimiter(String template)
	{
		try 
		{
			int at = 0;
			while(Character.isLetter(template.charAt(at)))
				++at;
			return template.charAt(at);
		} catch (Exception e) 
		{
			throw new RuntimeException("Missing correct delimiter -/. field in template: " + template);
		}
	}

	private static String getTemplateField(char fieldId)
	{
		switch(fieldId)
		{
			case 'y':	return "yyyy";
			case 'm':	return "MM";
			case 'd':	return "dd";
		}
		throw new RuntimeException("Unknown date field id: " + fieldId);
	}
	
	private char delimiter;
	private String mdyOrder;
	public static final char DEFAULT_DATE_DELIMITER = '/';
	public static final String DEFAULT_DATE_MDY_ORDER = "mdy";
	
}
