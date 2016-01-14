/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2010, Beneficent
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
package org.martus.common;

import java.util.Vector;

public class FieldDeskKeys extends ExternalPublicKeys
{
	public FieldDeskKeys()
	{
		super();
	}
	
	public FieldDeskKeys(Vector keysToUse)
	{
		super(keysToUse);
	}
	
	public FieldDeskKeys(FieldDeskKey key) 
	{
		add(key);
	}
	
	public FieldDeskKeys(FieldDeskKeys keys) 
	{
		add(keys);
	}
	

	public FieldDeskKeys(String xml) throws Exception
	{
		super(xml);	
	}
	
	String getTopLevelXmlElementName()
	{
		return FIELD_DESK_KEYS_TAG;
	}
	
	String getSingleEntryXmlElementName()
	{
		return FIELD_DESK_KEY_TAG;
	}

	ExternalPublicKeysXmlLoader createXmlLoader(Vector xmlKeys)
	{
		return createLoader(xmlKeys);
	}

	public static ExternalPublicKeysXmlLoader createLoader(Vector xmlKeys)
	{
		return new FieldDeskKeysXmlLoader(xmlKeys);
	}

	public FieldDeskKey get(int i)
	{
		return (FieldDeskKey)rawGet(i);
	}
	
	public void add(FieldDeskKey key)
	{
		rawAdd(key);
	}
	
	public void add(FieldDeskKeys keys)
	{
		rawAdd(keys);
	}
	
	public static final String FIELD_DESK_KEYS_TAG = "FieldDesks";
	public static final String FIELD_DESK_KEY_TAG = "FieldDesk";
}
