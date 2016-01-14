/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.util.xml;

import java.util.regex.Pattern;

public class XmlUtilities
{
	public static String getXmlEncoded(String text)
	{
		StringBuffer buf = new StringBuffer(text);
		for(int i = 0; i < buf.length(); ++i)
		{
			char c = buf.charAt(i);
			if(c == '&')
			{
				buf.replace(i, i+1, "&amp;");
			}
			else if(c == '<')
			{
				buf.replace(i, i+1, "&lt;");
			}
			else if(c == '>')
			{
				buf.replace(i, i+1, "&gt;");
			}
			else if(c == '"')
			{
				buf.replace(i, i+1, "&quot;");
			}
			else if(c == '\'')
			{
				buf.replace(i, i+1, "&#39;");
			}
		}
		return buf.toString();
	}

	public static String createStartElement(String text)
	{
		return "<" + text + ">";
	}

	public static String createEndTag(String text)
	{
		return createStartElement("/" + text);
	}

	public static String stripXmlHeader(String string)
	{
		return string.replaceAll("<\\?xml.*?\\?>", "");
	}
	
	public static String stripXmlStartEndElements(String xml, String elementNameToStrip)
	{
		return replaceXmlTags(xml, elementNameToStrip, "");
	}
	
	private static String replaceXmlTags(String text, String tagToReplace, final String replacement)
	{
		final String START_ELEMENT_REGEX = createStartTagRegex(tagToReplace);
		final String START_ELEMENT_WITH_ATRIBUTE_REGEX = createStartTagWithAttributeRegex(tagToReplace);
		final String END_ELEMENT_REGEX = createEndTagRegex(tagToReplace);
		final String EMPTY_ELEMENT_REGEX = createEmptyTagRegex(tagToReplace);
		
		final String REGEX_TO_REMOVE_START_AND_ELEMENT = START_ELEMENT_REGEX + "|" + EMPTY_ELEMENT_REGEX + "|" + END_ELEMENT_REGEX + "|" + START_ELEMENT_WITH_ATRIBUTE_REGEX; 
		
		return replaceAll(REGEX_TO_REMOVE_START_AND_ELEMENT, text, replacement);
	}

	private static String createStartTagWithAttributeRegex(String tagToReplace)
	{
		return "<\\s*" + tagToReplace + "\\s+.*?>";
	}
	
	private static String createStartTagRegex(String tagToReplace)
	{
		return "<\\s*" + tagToReplace + "\\s*>";
	}

	private static String createEmptyTagRegex(String tagToReplace)
	{
		return "<\\s*" + tagToReplace + "\\s*/\\s*>";
	}

	private static String createEndTagRegex(String tag)
	{
		return "<\\s*\\/\\s*" + tag + "\\s*>";
	}
	
	private static String replaceAll(final String regex, final String text, final String replacement)
	{
		final Pattern compiledRegex = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		
		return compiledRegex.matcher(text).replaceAll(replacement);
	}
}
