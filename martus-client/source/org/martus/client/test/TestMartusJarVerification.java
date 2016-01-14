/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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
package org.martus.client.test;

import javax.crypto.Cipher;

import org.martus.client.core.MartusJarVerification;
import org.martus.common.crypto.MartusCrypto;
import org.martus.util.TestCaseEnhanced;


public class TestMartusJarVerification extends TestCaseEnhanced
{
	public TestMartusJarVerification(String name)
	{
		super(name);
	}

	public void testVerifyJars() throws Exception
	{
		MartusJarVerification.verifyJars();
	}
	
	public void testVerifySignedKeyFileNoReference() throws Exception
	{
		try
		{
	 		MartusJarVerification.verifySignedKeyFile("nosuch", Cipher.class, "NOSUCHFILE.SF");
	 		fail("Should have thrown because reference doesn't exist");
		}
		catch(MartusCrypto.InvalidJarException ignoreExpected)
		{
		}
	}
		
	public void testVerifySignedKeyFileDoesntMatch() throws Exception
	{
		try
		{
			// try to verify bc-jce jar using bcprov sig file
			MartusJarVerification.verifySignedKeyFile("mismatch", Cipher.class, "BCKEY");
			fail("Should have thrown because reference didn't match actual");
		}
		catch(MartusCrypto.InvalidJarException ignoreExpected)
		{
		}
	}
	
}
