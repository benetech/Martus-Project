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

package org.martus.client.bulletinstore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class PersistableMap extends PersistableObject
{
	public PersistableMap()
	{
		map = new HashMap();
	}
	
	public PersistableMap(DataInputStream dataIn) throws IOException
	{
		this();
		int size = dataIn.readInt();
		for(int i = 0; i < size; ++i)
		{
			PersistableObject key = PersistableObject.createFrom(dataIn);
			PersistableObject value = PersistableObject.createFrom(dataIn);
			put(key, value);
		}
	}
	
	public int getType()
	{
		return TYPE_MAP;
	}
	
	public void put(String key, PersistableObject value)
	{
		put(new PersistableString(key), value);
	}
	
	public void put(PersistableObject key, PersistableObject value)
	{
		map.put(key, value);
	}
	
	public int size()
	{
		return map.size();
	}
	
	public PersistableObject get(String key)
	{
		return get(new PersistableString(key));
	}
	
	public PersistableObject get(PersistableObject key)
	{
		return (PersistableObject)map.get(key);
	}
	
	public PersistableObject[] keys()
	{
		return (PersistableObject[])map.keySet().toArray(new PersistableObject[0]);
	}
	
	public boolean equals(Object rawOther)
	{
		if(! (rawOther instanceof PersistableInt))
			return false;
		
		PersistableMap other = (PersistableMap)rawOther;
		return (map.equals(other.map));
	}
	
	public int hashCode()
	{
		return map.hashCode();
	}

	void internalWriteTo(DataOutputStream dataOut) throws IOException 
	{
		dataOut.writeInt(size());
		Iterator iter = map.keySet().iterator();
		while(iter.hasNext())
		{
			PersistableObject key = (PersistableObject)iter.next();
			key.writeTo(dataOut);
			get(key).writeTo(dataOut);
		}
	}

	private HashMap map;
}
