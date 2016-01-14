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
package org.martus.client.bulletinstore;

import java.util.HashMap;

import org.martus.client.core.PartialBulletin;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class PartialBulletinCache
{
	public PartialBulletinCache(String[] tagsToStore)
	{
		tags = tagsToStore;
		uidsToPartialBulletins = new HashMap();
	}
	
	public void add(Bulletin b)
	{
		PartialBulletin pb = new PartialBulletin(b, tags);
		uidsToPartialBulletins.put(b.getUniversalId(), pb);
	}
	
	public void remove(UniversalId uid)
	{
		uidsToPartialBulletins.remove(uid);
	}
	
	public boolean isBulletinCached(UniversalId uid)
	{
		return getPartialBulletin(uid) != null;
	}
	
	public String getFieldData(UniversalId uid, String fieldTag)
	{
		PartialBulletin pb = getPartialBulletin(uid);
		String data = pb.getData(fieldTag);
		if(data == null)
			data = "";
		return data;
	}

	private PartialBulletin getPartialBulletin(UniversalId uid)
	{
		PartialBulletin pb = (PartialBulletin)uidsToPartialBulletins.get(uid);
		return pb;
	}
	
	String[] tags;
	HashMap uidsToPartialBulletins;
}
