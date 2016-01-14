/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2010, Beneficent
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
package org.martus.common.i18n;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.martus.common.MartusLogger;
import org.martus.util.UnicodeReader;

public class PoEntryLoader
{
	public static TranslationEntry read(UnicodeReader reader) throws IOException
	{
		TranslationEntry entry = null;
		int mode = 0;
		String line = get_first_non_blank_line(reader);
		while(line != null)
		{
			if(entry == null)
				entry = new TranslationEntry();
			
			if(line.length() == 0)
				break;

			if(line.matches("^#.*"))
				process_comment(entry, line);
			else
			{
				if(line.matches("^msgid .*"))
					mode = TranslationEntry.MSGID;
				else if(line.matches("^msgstr .*"))
					mode = TranslationEntry.MSGSTR;
				else if(line.matches("^msgctxt .*"))
					mode = TranslationEntry.MSGCTXT;
				
				String text = extract_text(line);
				entry.append(mode, text);
			}

			line = gets_stripped(reader);
		}
		
		return entry;
	}

	private static String extract_text(String line)
	{
		int first = line.indexOf('"');
		int last = line.lastIndexOf('"');
		if (first < 0 || last < 0)
			MartusLogger.log("PoEntryReader unquoted string: " + line);
		
		String text = line.substring(first+1, last);
		return text;
	}

	private static void process_comment(TranslationEntry entry, String line)
	{
		if(line.matches("^#:.*"))
		{
			Pattern pattern = Pattern.compile("^#:\\s*(\\w\\w\\w\\w)\\s*");
			Matcher matcher = pattern.matcher(line);
			if(matcher.matches())
			{
				int start = matcher.start(1);
				int end = matcher.end(1);
				entry.setHex(line.substring(start, end));
			}
		}
		else if(line.matches("^#,\\s+fuzzy.*"))
		{
			entry.setFuzzy();
		}
		else
		{
			MartusLogger.log("Unknown comment:" + line);
		}
	}

	private static String get_first_non_blank_line(UnicodeReader input) throws IOException
	{
		String line = null;

		while(true)
		{
			line = input.readLine();
			if(line == null)
				return null;
			line = strip(line);
			if(line.length() > 0)
				break;
		}
		
		return line;
	}

	public static String gets_stripped(UnicodeReader reader) throws IOException
	{
		String line = reader.readLine();
		if(line == null)
			return null;
		return strip(line);
	}

	private static String strip(String line)
	{
		int firstNonWhitespace = 0;
		while(firstNonWhitespace < line.length() && Character.isWhitespace(line.charAt(firstNonWhitespace)))
			++firstNonWhitespace;
		int lastNonWhitespace = line.length();
		while(lastNonWhitespace > 0 && Character.isWhitespace(line.charAt(lastNonWhitespace-1)))
			--lastNonWhitespace;
		
		return line.substring(firstNonWhitespace, lastNonWhitespace);
	}
}
