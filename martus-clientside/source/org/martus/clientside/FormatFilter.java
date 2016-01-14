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

*/
package org.martus.clientside;

import java.io.File;

import javax.swing.filechooser.FileFilter;

abstract public class FormatFilter extends FileFilter implements java.io.FileFilter
{
	abstract public String getExtension();

	public String[] getExtensions()
	{
		return new String[] {getExtension()};
	}
	
	public String getWildCardExtension()
	{
		return "*" + getExtension();
	}

	public boolean accept(File f)
	{
		if(f.isDirectory())
			return true;
		
		for (String extension : getExtensions()) 
		{
			if (f.getName().toLowerCase().endsWith(extension))
				return true;
		}
		
		return false;
	}
}
