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


public class HeadquartersKeys extends ExternalPublicKeys
{
	public HeadquartersKeys()
	{
		super();
	}
	
	public HeadquartersKeys(Vector keysToUse)
	{
		super(keysToUse);
	}
	
	public HeadquartersKeys(HeadquartersKey key) 
	{
		add(key);
	}
	
	public HeadquartersKeys(HeadquartersKeys keys) 
	{
		add(keys);
	}

	public HeadquartersKeys(String xml) throws Exception
	{
		super(xml);	
	}
	
	String getTopLevelXmlElementName()
	{
		return HQ_KEYS_TAG;
	}
	
	String getSingleEntryXmlElementName()
	{
		return HQ_KEY_TAG;
	}

	ExternalPublicKeysXmlLoader createXmlLoader(Vector xmlKeys)
	{
		return createLoader(xmlKeys);
	}

	public static ExternalPublicKeysXmlLoader createLoader(Vector xmlKeys)
	{
		return new HeadquartersKeysXmlLoader(xmlKeys);
	}

	public HeadquartersKey get(int i)
	{
		return (HeadquartersKey)rawGet(i);
	}
	
	public void add(HeadquartersKey key)
	{
		rawAdd(key);
	}
	
	public void add(HeadquartersKeys keys)
	{
		rawAdd(keys);
	}
	
	public static final String HQ_KEYS_TAG = "HQs";
	public static final String HQ_KEY_TAG = "HQ";
}
