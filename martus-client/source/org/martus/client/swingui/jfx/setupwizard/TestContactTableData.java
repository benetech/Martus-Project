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
package org.martus.client.swingui.jfx.setupwizard;

import org.martus.common.ContactKey;
import org.martus.util.TestCaseEnhanced;

public class TestContactTableData extends TestCaseEnhanced
{
	public TestContactTableData(String name)
	{
		super(name);
	}
	
	public void testBasics() throws Exception 
	{
		String label = "My HQ";
		String publicKey = "FakeKey";
		ContactKey contactKey = new ContactKey(publicKey, label);
		contactKey.setVerificationStatus(ContactKey.VERIFIED_VISUALLY);
		ContactsTableData fxmlTableData = new ContactsTableData(contactKey);

		ContactKey keyReturned = fxmlTableData.getContact();
		assertEquals(contactKey.getPublicKey(), keyReturned.getPublicKey());
		assertEquals(publicKey, keyReturned.getPublicKey());
		assertEquals(contactKey.getFormattedPublicCode(), keyReturned.getFormattedPublicCode());
		assertEquals(contactKey.getFormattedPublicCode40(), keyReturned.getFormattedPublicCode40());
		assertEquals(contactKey.getLabel(), keyReturned.getLabel());
		assertEquals(label, keyReturned.getLabel());
		assertTrue(keyReturned.getCanSendTo());
		assertTrue("New:All HQ and FD keys now can Send To / can Receive From",keyReturned.getCanReceiveFrom());
		assertEquals(ContactKey.VERIFIED_VISUALLY, keyReturned.getVerificationStatus());
	}
}
