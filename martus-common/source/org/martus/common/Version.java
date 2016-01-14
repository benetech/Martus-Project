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

public class Version
{
	public static void main(String[] args)
	{
		String date = VersionBuildDate.getVersionBuildDate();
		System.out.println(formatDateVersion(date));
	}

	static String formatDateVersion(String dateVersion)
	{
		if(dateVersion.length() != 8)
			return dateVersion;
		return dateVersion.substring(0,4) + "-" + dateVersion.substring(4,6) + "-" + dateVersion.substring(6);
	}
	
	public static boolean isRunningUnderWindows()
	{
		return System.getProperty("os.name").indexOf("Windows") >= 0;
	}
	
	public static boolean isRunningUnderOpenJDK()
	{
		String runtimeName = System.getProperty("java.runtime.name");
		if(runtimeName == null)
			return false;
		
		return runtimeName.contains("OpenJDK");
	}


}
