/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.client.core;

import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.WorkerThread;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class BulletinGetterThread extends WorkerThread
{
	public BulletinGetterThread(ClientBulletinStore storeToUse, UniversalId[] uidsToGet)
	{
		store = storeToUse;
		uids = uidsToGet;
		bulletins = new Vector();
	}
	
	public Vector getBulletins()
	{
		return bulletins;
	}

	public void doTheWorkWithNO_SWING_CALLS() throws Exception
	{
		for (int i = 0; i < uids.length; i++)
		{
			UniversalId uid = uids[i];
			Bulletin b = store.getBulletinRevision(uid);
			bulletins.add(b);
		}
	}

	private ClientBulletinStore store;
	private UniversalId[] uids;
	private Vector bulletins;
}