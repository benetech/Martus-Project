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

package org.martus.common.packet;

import org.martus.util.StreamableBase64;

public class UniversalId implements Comparable
{
	public static class NotUniversalIdException extends Exception 
	{
	}

	public static UniversalId createFromAccountAndLocalId(String accountId, String localId)
	{
		return new UniversalId(accountId, localId);
	}

	static UniversalId createDummyFromString(String uidAsString)
	{
		String accountId = uidAsString.substring(0, 1);
		String localId = uidAsString.substring(2);
		return createFromAccountAndLocalId(accountId, localId);
	}

	public static UniversalId createFromString(String uidAsString) throws
			NotUniversalIdException
	{
		int dashAt = uidAsString.indexOf("-");
		if(dashAt < 0)
			throw new NotUniversalIdException();

		String accountId = uidAsString.substring(0, dashAt);
		String localId = uidAsString.substring(dashAt + 1);
		return createFromAccountAndLocalId(accountId, localId);
	}

	protected UniversalId(String accountIdToUse, String localIdToUse)
	{
		setAccountId(accountIdToUse);
		setLocalId(localIdToUse);
	}

	public String getAccountId()
	{
		return accountId.toString();
	}

	public String getLocalId()
	{
		return localId;
	}

	public String toString()
	{
		return getAccountId() + "-" + getLocalId();
	}

	public boolean equals(Object otherObject)
	{
		if(otherObject == this)
			return true;
		if(otherObject == null)
			return false;
		if(otherObject.getClass() != getClass())
			return false;

		UniversalId otherId = (UniversalId)otherObject;
		if(!otherId.getAccountId().equals(getAccountId()))
			return false;
		if(!otherId.getLocalId().equals(getLocalId()))
			return false;

		return true;
	}

	public int hashCode()
	{
		return toString().hashCode();
	}

	public int compareTo(Object other)
	{
		return toString().compareTo(((UniversalId)other).toString());
	}

	public void setAccountId(String newAccountId)
	{
		accountId = AccountId.create(newAccountId);
	}

	public void setLocalId(String newLocalId)
	{
		localId = newLocalId.replace(':', '-');
	}
	
	public static String createLocalIdFromByteArray(String prefix, byte[] originalBytes, String suffix)
	{
		byte[] wantedBytes = new byte[UniversalId.LOCALID_RANDOM_BYTE_COUNT];
		System.arraycopy(originalBytes, 0, wantedBytes, 0, wantedBytes.length);
		String base64SessionKey = StreamableBase64.encode(wantedBytes);
		String normalizedKey = base64SessionKey.replaceAll("/",".");
		normalizedKey = normalizedKey.replaceAll("=", "-");
		String localId = prefix + normalizedKey + suffix;
		return localId;
	}

	private AccountId accountId;
	private String localId;
	public static final int LOCALID_RANDOM_BYTE_COUNT = 128/8;
}
