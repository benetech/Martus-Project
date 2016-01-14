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

package org.martus.common.database;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;



public class DatabaseKey implements Comparable
{
	public static DatabaseKey createMutableKey(UniversalId uidToUse)
	{
		DatabaseKey key = new DatabaseKey(uidToUse);
		key.setMutable();
		return key;
	}

	public static DatabaseKey createImmutableKey(UniversalId uidToUse)
	{
		DatabaseKey key = new DatabaseKey(uidToUse);
		key.setImmutable();
		return key;
	}

	public static DatabaseKey createLegacyKey(UniversalId uidToUse)
	{
		DatabaseKey key = new DatabaseKey(uidToUse);
		key.setImmutable();
		return key;
	}

	public static DatabaseKey createKey(UniversalId uidToUse, String status)
	{
		if(Bulletin.isMutable(status))
			return createMutableKey(uidToUse);
		return createImmutableKey(uidToUse);
	}

	private DatabaseKey(UniversalId uidToUse)
	{
		uid = uidToUse;
		status = statusImmutable;
	}

	public UniversalId getUniversalId()
	{
		return uid;
	}

	public String getAccountId()
	{
		return getUniversalId().getAccountId();
	}

	public String getLocalId()
	{
		return getUniversalId().getLocalId();
	}

	public boolean isImmutable()
	{
		return (status == statusImmutable);
	}

	public boolean isMutable()
	{
		return (status == statusMutable);
	}

	public void setMutable()
	{
		status = statusMutable;
	}

	public void setImmutable()
	{
		status = statusImmutable;
	}

	public boolean equals(Object otherObject)
	{
		if(this == otherObject)
			return true;

		if(otherObject instanceof DatabaseKey)
		{
			DatabaseKey otherKey = (DatabaseKey)otherObject;
			return getString().equals(otherKey.getString());
		}

		return false;
	}

	public int hashCode()
	{
		return getString().hashCode();
	}

	public int compareTo(Object other)
	{
		return getString().compareTo(((DatabaseKey)other).getString());
	}

	private String getString()
	{
		String statusCode = "?";
		if(isMutable())
			statusCode = "D";
		else if(isImmutable())
			statusCode = "S";
		return statusCode + "-" + uid.toString();
	}
	
	public String toString() 
	{
		return getString();
	}

	private static final int statusImmutable = 1;
	private static final int statusMutable = 2;

	UniversalId uid;
	int status;
}
