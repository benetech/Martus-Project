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
package org.martus.server.formirroring;

import java.io.IOException;
import java.util.Vector;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.packet.UniversalId;

public class BulletinMirroringInformation
{
	public BulletinMirroringInformation(ReadableDatabase db, DatabaseKey key, String signatureToUse) throws IOException, RecordHiddenException
	{
		signature = signatureToUse;
		uId = key.getUniversalId();
		mTime = db.getmTime(key);
		if(key.isDraft())
			status = BulletinConstants.STATUSDRAFT;
		else
			status = BulletinConstants.STATUSSEALED;
	}
	
	public BulletinMirroringInformation(String accountId, Vector info)
	{
		String localId = (String)info.get(0);
		uId = UniversalId.createFromAccountAndLocalId(accountId, localId);
		status = (String)info.get(1);
		mTime = Long.parseLong((String)info.get(2));
		signature = (String)info.get(3);
	}
	
	public BulletinMirroringInformation(UniversalId uIdToUse)
	{
		uId = uIdToUse;
		status = BulletinConstants.STATUSSEALED;
	}

	public UniversalId getUid()
	{
		return uId;
	}
	
	public boolean isDraft()
	{
		return status.equals(BulletinConstants.STATUSDRAFT);
	}
	
	public boolean isSealed()
	{
		return status.equals(BulletinConstants.STATUSSEALED);
	}
	
	public String getStatus()
	{
		return status;
	}

	public long getmTime()
	{
		return mTime;
	}
	
	public Vector getInfoWithLocalId()
	{
		Vector info = new Vector();
		info.add(uId.getLocalId());
		addRemainingInfo(info);
		return info; 
	}

	
	public Vector getInfoWithUniversalId()
	{
		Vector info = new Vector();
		info.add(uId);
		addRemainingInfo(info);
		return info; 
	}

	private void addRemainingInfo(Vector info)
	{
		info.add(status);
		info.add(Long.toString(mTime));
		info.add(signature);
	}

	long mTime;
	private UniversalId uId;
	String status;
	private String signature;
}
