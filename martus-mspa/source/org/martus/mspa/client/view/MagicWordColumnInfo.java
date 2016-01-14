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
package org.martus.mspa.client.view;

import javax.swing.JLabel;


public class MagicWordColumnInfo
{
	public MagicWordColumnInfo(String title, int width, int alignment) 
	{
	  fTitle = title;
	  fWidth = width;
	  fAlignment = alignment;
	}
		
	static final public MagicWordColumnInfo m_columns[] = {
	  new MagicWordColumnInfo( "Creation Date", 60, JLabel.CENTER ),
	  new MagicWordColumnInfo( "Status", 40, JLabel.CENTER ),
	  new MagicWordColumnInfo( "MagicWord", 100, JLabel.CENTER ),
	  new MagicWordColumnInfo( "Group", 100, JLabel.CENTER )	
	};
	
	public String  fTitle;
	public int     fWidth;
	public int     fAlignment;
	
	public static final int COL_DATE = 0;
	public static final int COL_STATUS = 1;
	public static final int COL_WORD = 2;
	public static final int COL_GROUPNAME = 3;
}
