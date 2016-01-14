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

import java.io.File;

public class MartusConstants
{
	// Somewhat surprisingly, a 32k buffer didn't seem to be any
	// faster than a 1k buffer.
	public final static int streamBufferCopySize = 1024;
	public final static int digestBufferSize = 128 * 1024;
	public final static String martusSecretShareFileID = "Martus-Share";
	public final static int minNumberOfFilesNeededToRecreateSecret = 2;
	public final static int numberOfFilesInShare = 3;
	public final static String NEWLINE = System.getProperty("line.separator");
	public static final String deprecatedCustomFieldSpecs = "Error,Config written by a newer version of Martus, upgrade";
	
	public static File determineMartusDataRootDirectory()
	{
		String dir;
		if(Version.isRunningUnderWindows())
		{
			dir = "C:/Martus/";
		}
		else
		{
			String userHomeDir = System.getProperty("user.home");
			dir = userHomeDir + "/.Martus/";
		}
		File file = new File(dir);
		if(!file.exists())
		{
			file.mkdirs();
		}
	
		return file;
	}
}
