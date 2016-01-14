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
package org.martus.common;

public class MagicWordEntry
{	
	public MagicWordEntry(String magicWordEntry, String groupEntry)
	{						
		setMagicWord(magicWordEntry);
		
		if (groupEntry == null)
			groupEntry = "";			
		groupName = groupEntry;
	}
	
	public void setMagicWord(String magicWordEntry)
	{
		if (magicWordEntry == null)
			magicWordEntry = "";
					
		if (magicWordEntry.startsWith(MagicWords.INACTIVE_SIGN))
		{	
			magicWord = magicWordEntry.substring(1);
			setActive(false);
		}
		else		
			magicWord = magicWordEntry;
	}

	public String getMagicWord()
	{
		return magicWord;
	}

	public String getMagicWordWithActiveSign()
	{
		if (isActive())
			return magicWord;
			
		return MagicWords.INACTIVE_SIGN+magicWord;		
	}

	public String getGroupName()
	{
		return groupName;
	}
	
	public String getCreationDate()
	{
		return creationDate;
	}
	
	public void setGroupname(String name)
	{
		groupName = name;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean status)
	{
		active = status;
	}
	
	public void setCreationDate(String date)
	{
		creationDate = date;
	}
	
	public String getLineOfMagicWord()
	{		
		return getLine(getMagicWordWithActiveSign());
	}
	
	public String getLineOfMagicWordNoSign()
	{		
		return getLine(getMagicWord());
	}	
	
	private String getLine(String magicWordToUse)
	{
		String lineOfMagicWord = magicWordToUse + MagicWords.FIELD_DELIMITER + getGroupName()+
				MagicWords.FIELD_DELIMITER + getCreationDate();
				
		return lineOfMagicWord.trim();
	}
	
	private String magicWord;
	private String groupName;
	private boolean active=true;
	private String creationDate="";
}
	

