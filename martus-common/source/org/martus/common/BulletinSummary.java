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

import java.util.Vector;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;

public class BulletinSummary
{
	public static Object createFromBulletin(Bulletin bulletin) throws WrongValueCount
	{
		int pretendSizeIsZero = 0;
		return createFromString(bulletin.getAccount(), bulletin.getLocalId()+fieldDelimeter+ bulletin.getFieldDataPacket().getLocalId()+fieldDelimeter+pretendSizeIsZero);
	}

	public static BulletinSummary createFromString(String accountId, String parameters) throws WrongValueCount
	{
		String args[] = parameters.split(fieldDelimeter, -1);
		if(args.length < 1 || args.length > 5)
			throw new WrongValueCount(args.length, parameters);
		
		int at = 0;
		String bulletinLocalId= args[at++];
		String fdpLocalId = args[at++];
		int size = 0;
		if(args.length > at)
			size = Integer.parseInt(args[at++]);
		String date = "";
		if(args.length > at)
			date = args[at++];
		BulletinHistory history = null;
		if(args.length > at)
			history = BulletinHistory.createFromHistoryString(args[at++]);
		
		UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, bulletinLocalId);
		return new BulletinSummary(uid, fdpLocalId, size, date, history);
	}

	private BulletinSummary(UniversalId bulletinIdToUse, String fieldDataPacketLocalIdToUse, int sizeToUse, String dateSavedToUse, BulletinHistory historyToUse)
	{
		accountId = bulletinIdToUse.getAccountId();
		localId = bulletinIdToUse.getLocalId();
		fdpLocalId = fieldDataPacketLocalIdToUse;
		size = sizeToUse;
		dateTimeSaved = dateSavedToUse;
		history = historyToUse;
		if(history == null)
			history = new BulletinHistory();
	}
	
	public boolean hasFieldDataPacket()
	{
		return (fdp != null);
	}
	
	public void setFieldDataPacket(FieldDataPacket fdpToUse)
	{
		fdp = fdpToUse;
		fdpLocalId = fdp.getLocalId();
		title = fdpToUse.get(Bulletin.TAGTITLE);
		author = fdpToUse.get(Bulletin.TAGAUTHOR);
	}

	public void setChecked(boolean newValue)
	{
		if(downloadable)
			checkedFlag = newValue;
	}

	public boolean isChecked()
	{
		return checkedFlag;
	}
	
	public UniversalId getUniversalId()
	{
		return UniversalId.createFromAccountAndLocalId(getAccountId(), getLocalId());
	}

	public String getAccountId()
	{
		return accountId;
	}

	public String getLocalId()
	{
		return localId;
	}
	
	public String getFieldDataPacketLocalId()
	{
		return fdpLocalId;
	}

	public String getStorableTitle()
	{
		return title;
	}

	public String getStorableAuthor()
	{
		return author;
	}
	
	
	public long getDateTimeSaved()
	{
		return getLastDateTimeSaved(dateTimeSaved);
	}

	static public long getLastDateTimeSaved(String dateToConvert)
	{
		if(dateToConvert.length() == 0)
			return MiniLocalization.DATE_UNKNOWN;
		return Long.parseLong(dateToConvert);
	}
	
	public int getVersionNumber()
	{
		return getHistory().size()+1;
	}
	
	public BulletinHistory getHistory()
	{
		return history;
	}

	public boolean isDownloadable()
	{
		return downloadable;
	}

	public void setDownloadable(boolean downloadable)
	{
		this.downloadable = downloadable;
	}

	public int getSize()
	{
		return size;
	}

	public FieldDataPacket getFieldDataPacket()
	{
		return fdp;
	}
	
	public static Vector getNormalRetrieveTags()
	{
		Vector tags = new Vector();
		tags.add(NetworkInterfaceConstants.TAG_BULLETIN_SIZE);
		tags.add(NetworkInterfaceConstants.TAG_BULLETIN_DATE_SAVED);
		tags.add(NetworkInterfaceConstants.TAG_BULLETIN_HISTORY);
		return tags;
	}
	
	public static class WrongValueCount extends Exception
	{
		WrongValueCount(int gotCount)
		{
			this(gotCount, "");
		}
		
		WrongValueCount(int gotCount, String message)
		{
			super(message);
			got = gotCount;
			expected = getNormalRetrieveTags().size();
		}
		
		public int got;
		public int expected;
	}

	public int hashCode()
	{
		return localId.hashCode();
	}
	
	public boolean equals(Object rawOther)
	{
		if(rawOther == this)
			return true;
		if(! (rawOther instanceof BulletinSummary))
			return false;
		BulletinSummary other = (BulletinSummary)rawOther;
		
		if(!accountId.equals(other.accountId))
			return false;
		if(!localId.equals(other.localId))
			return false;
		if(!toString().equals(other.toString()))
			return false;
		
		return true;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(accountId);
		sb.append("-" + localId);
		sb.append("-" + title);
		sb.append("-" + author);
		sb.append("-" + dateTimeSaved);
		sb.append("-" + size);
		sb.append("-" + checkedFlag);
		sb.append("-" + downloadable);
		sb.append("-" + history.toString());
		return sb.toString();
	}
	
	private FieldDataPacket fdp;
	private String accountId;
	private String fdpLocalId;
	String localId;
	String title;
	String author;
	public String dateTimeSaved;
	private BulletinHistory history;
	int size;
	boolean checkedFlag;
	boolean downloadable;
	public final static String fieldDelimeter = "=";
}
