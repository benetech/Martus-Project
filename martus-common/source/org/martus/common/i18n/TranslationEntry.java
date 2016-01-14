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

public class TranslationEntry
{
	public TranslationEntry()
	{
		msgid = "";
		msgstr = "";
		context = "";
	}
	
	public void append(int mode, String text)
	{
		switch(mode)
		{
			case MSGCTXT:
				context += text;
				break;
			case MSGID:
				msgid += text;
				break;
			case MSGSTR:
				msgstr += text;
				break;
			default:
				throw new RuntimeException("Unknown mode: " + mode + " (" + text + ")");
		}
	}
	
	public String getMsgid()
	{
		return msgid;
	}
	
	public String getMsgstr()
	{
		return msgstr;
	}
	
	public String getHex()
	{
		return hex;
	}

	public String getContext()
	{
		return context;
	}

	public void setHex(String hexToUse)
	{
		hex = hexToUse;
	}
	
	public void setFuzzy()
	{
		fuzzy = true;
	}
	
	public boolean isFuzzy()
	{
		return fuzzy;
	}
	
	public static final int MSGCTXT = 1;
	public static final int MSGID = 2;
	public static final int MSGSTR = 3;

	private String msgid;
	private String msgstr;
	private String hex;
	private String context;
	private boolean fuzzy;
}
