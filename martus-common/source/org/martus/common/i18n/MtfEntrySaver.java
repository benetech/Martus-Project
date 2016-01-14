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

import org.martus.util.UnicodeWriter;

public class MtfEntrySaver
{
	public static void save(UnicodeWriter writer, TranslationEntry entry) throws IOException
	{
		String hex = entry.getHex();
		String hex_stuff = "";
		if(hex != null)
			hex_stuff = "-" + entry.getHex() + "-";

		String before_equals = hex_stuff + entry.getContext();
		String filler = "";
		for(int i = 0; i < entry.getContext().length() + 5; ++i)
			filler += "_";
		writer.writeln("#" + filler + "=" + entry.getMsgid());
		String translated = entry.getMsgstr();
		boolean has_angle_brackets = translated.matches("^<(.*?)>$");
		if(entry.isFuzzy() && !has_angle_brackets)
			translated = "<" + translated + ">";
		writer.writeln(before_equals + "=" + translated);
		writer.writeln();
		writer.flush();
	}
}
