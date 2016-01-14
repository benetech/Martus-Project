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
package org.martus.common.test;

import java.util.HashMap;

import org.martus.common.AuthorizedSessionKeys;
import org.martus.util.TestCaseEnhanced;


public class TestAuthorizedSessionKeys extends TestCaseEnhanced
{
	public TestAuthorizedSessionKeys(String name)
	{
		super(name);
	}
	
	public void testBasics()
	{
			
		AuthorizedSessionKeys keys = new AuthorizedSessionKeys();
		keys.addToAuthorized(publicCode1, sessionKey1);
		keys.addToAuthorized(publicCode2, sessionKey2);

		assertEquals(sessionKey1, keys.getSessionKey(publicCode1));
		assertEquals(sessionKey2, keys.getSessionKey(publicCode2));
		
		HashMap newMapOfKeys = new HashMap();
		newMapOfKeys.put(publicCode2, sessionKey2);
		AuthorizedSessionKeys sessionKeysNew = new AuthorizedSessionKeys(newMapOfKeys);
		assertEquals(sessionKey2, sessionKeysNew.getSessionKey(publicCode2));
		assertNull(sessionKeysNew.getSessionKey(publicCode1));
	}

	public void testToStringAndParse()throws Exception
	{
		AuthorizedSessionKeys keys = new AuthorizedSessionKeys();
		keys.addToAuthorized(publicCode1, sessionKey1);
		keys.addToAuthorized(publicCode2, sessionKey2);
		
		String xmlOriginal = keys.toString();
		AuthorizedSessionKeys loaded = new AuthorizedSessionKeys(AuthorizedSessionKeys.parseXml(xmlOriginal));
		
		assertEquals(xmlOriginal, loaded.toString());
	}

	final String publicCode1 = "1234.1234.1234.1234.1234";
	final String publicCode2 = "2234.2234.2234.2234.2234";
	final String sessionKey1 = "session 1";
	final String sessionKey2 = "session 2";

}
