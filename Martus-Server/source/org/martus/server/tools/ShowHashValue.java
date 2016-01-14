/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2003-2007, Beneficent
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

package org.martus.server.tools;

import org.martus.common.database.FileDatabase;

public class ShowHashValue 
{
	public static void main(String[] args) 
	{
		if(args.length != 1)
		{
			System.out.println("ShowHashValue <string>");
			System.out.println("   Shows the hash (packet bucket) of a string");
			System.exit(2);
		}
		
		System.out.println(FileDatabase.getBaseBucketName(args[0]));
		System.exit(0);
	}
}
