/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.common.bulletinstore;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.UniversalId;

public abstract class BulletinStoreCache
{
	abstract public void clear();
	abstract public void storeWasCleared();
	abstract public void revisionWasSaved(UniversalId uid) throws Exception;
	abstract public void revisionWasSaved(Bulletin b) throws Exception;
	abstract public void revisionWasRemoved(UniversalId uid);
	
	public static DatabaseKey findKey(ReadableDatabase db, UniversalId uid)
	{
		DatabaseKey[] possibleKeys = 
		{
			// always check legacy before draft or sealed
			DatabaseKey.createLegacyKey(uid),
			DatabaseKey.createImmutableKey(uid),
			DatabaseKey.createMutableKey(uid),
		};
	
		for(int i=0; i < possibleKeys.length; ++i)
		{
			if(db.doesRecordExist(possibleKeys[i]))
				return possibleKeys[i];
		}
		
		return null;
	}
}