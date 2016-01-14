/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-20014, Beneficent
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

package org.martus.server.formirroring;

import java.io.File;

public class TemplateInfoForMirroring 
{
	public TemplateInfoForMirroring(File file) throws Exception
	{
		name = file.getName();
		lastModifiedMillis = file.lastModified();
		size = file.length();
	}
	
	public TemplateInfoForMirroring(String infoAsString)
	{
		String[] pieces = infoAsString.split("\\t");
		name = pieces[0];
		lastModifiedMillis = Long.parseLong(pieces[1]);
		size = Long.parseLong(pieces[2]);
	}

	public TemplateInfoForMirroring(String filename, long lastModifiedMillisToUse, long fileSize) 
	{
		name = filename;
		lastModifiedMillis = lastModifiedMillisToUse;
		size = fileSize;
	}

	public String asString() 
	{
		return getFilename() + TAB + getLastModifiedMillis() + TAB + getFileSize();
	}
	
	public String getFilename()
	{
		return name;
	}
	
	public long getLastModifiedMillis()
	{
		return lastModifiedMillis;
	}
	
	public long getFileSize()
	{
		return size;
	}
	
	@Override
	public String toString() 
	{
		return asString();
	}
	
	@Override
	public int hashCode() 
	{
		return asString().hashCode();
	}
	
	@Override
	public boolean equals(Object rawOther) 
	{
		if(rawOther == this)
			return true;
		if(rawOther == null)
			return false;
		if(! (rawOther instanceof TemplateInfoForMirroring) )
			return false;
		
		TemplateInfoForMirroring other = (TemplateInfoForMirroring) rawOther;
		return asString().equals(other.asString());
	}
	
	private final static char TAB = '\t'; 
	
	private String name;
	private long lastModifiedMillis;
	private long size;
}
