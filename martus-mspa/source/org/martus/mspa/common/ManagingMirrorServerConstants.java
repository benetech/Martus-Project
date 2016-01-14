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
package org.martus.mspa.common;


public interface ManagingMirrorServerConstants
{
	public static int SERVERS_WHOSE_DATA_WE_BACKUP	=100;
	public static int SERVERS_WHOSE_DATA_WE_AMPLIFY	=101;
	public static int SERVERS_WHO_AMPLIFY_OUR_DATA	=102;
	public static int SERVERS_WHO_BACKUP_OUR_DATA	=103;

	public static String SERVERS_WHOES_DATA_WE_BACKUP_TITLE		="Servers Whose Data We Backup";
	public static String SERVERS_WHOSE_DATA_WE_AMPLIFY_TITLE	="Servers Whose Data We Amplify";
	public static String SERVERS_WHO_AMPLIFY_OUR_DATA_TITLE		="Servers Who Amplify Our Data";
	public static String SERVERS_WHO_BACKUP_OUR_DATA_TITLE		="Servers Who Backup Our Data";
	
	
	public static String DATA_WE_BACKUP_LABEL					="Servers Whose Data We Backup:";	
	public static String DATA_WE_AMPLIFY_LABEL					="Servers Whose Data We Amplify:";		
	public static String CAN_AMPLIFY_OUR_DATA_LABEL				="Can Amplify Our Data:";
	public static String CAN_UPLOAD_OUR_DATA_LABEL				="Can Backup Our Data";	
	
	public static String AVAILABLE_SERVER_LABEL					="Available Servers:";
	public static String AVAILABLE_MAGIC_LABEL					="Available Magic Words:";	
	
}
