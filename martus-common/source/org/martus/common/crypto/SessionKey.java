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

package org.martus.common.crypto;

import java.util.Arrays;

import org.martus.util.StreamableBase64;


public class SessionKey
{
	public SessionKey(byte[] keyBytesToUse)
	{
		keyBytes = keyBytesToUse;
	}
	
	public byte[] getBytes()
	{
		return keyBytes;
	}
	
	public SessionKey copy()
	{
		byte[] copiedBytes = new byte[getBytes().length];
		System.arraycopy(getBytes(), 0, copiedBytes, 0, copiedBytes.length);
		return new SessionKey(copiedBytes);
	}
	
	public void wipe()
	{
		Arrays.fill(keyBytes, (byte)0x55);
	}
	
	public boolean equals(Object otherSessionKey)
	{
		SessionKey otherKey = (SessionKey)otherSessionKey;
		return Arrays.equals(getBytes(), otherKey.getBytes());
	}
	
	public int hashCode()
	{
		// Since our data IS randomly distributed, AND
		// we don't expect more than a couple thousand entries 
		// in the system, we can just return the first two bytes
		// and have an even spread across 64k buckets
		return (keyBytes[1] << 8) | keyBytes[0];
	}

	public String toString()
	{
		return StreamableBase64.encode(getBytes());
	}
	
	byte[] keyBytes;
}
