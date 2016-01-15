/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2014, Beneficent
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

package org.martus.server.main;

public interface ServerMetaDatabaseConstants 
{
	public static final int SCHEMA_VERSION = 1;
	
	public static final String CLASS_NAME_SCHEMA = "Schema";
	public static final String KEY_SCHEMA_VERSION = "Version";
	
	public static final String CLASS_NAME_ACCOUNT = "Account";
	public static final String KEY_ACCOUNT_ACCOUNT_ID = "AccountId";
	public static final String KEY_ACCOUNT_PUBLIC_CODE = "PublicCode";

	public static final String CLASS_NAME_BULLETIN = "Bulletin";
	public static final String KEY_BULLETIN_LOCAL_ID = "LocalId";
	public static final String KEY_BULLETIN_DUID = "DatabaseUid";
	public static final String KEY_BULLETIN_TIMESTAMP = "Timestamp";
	public static final String KEY_BULLETIN_LAST_MODIFIED = "LastModified";
	
	public static final String CLASS_NAME_CAN_DOWNLOAD = "CanDownload";
	public static final String KEY_CAN_DOWNLOAD_PUBCODETIMESTAMP = "PublicCodeAndTimestamp";
	
	public static final String CLASS_NAME_WAS_AUTHORED_BY = "WasAuthoredBy";

	
}
