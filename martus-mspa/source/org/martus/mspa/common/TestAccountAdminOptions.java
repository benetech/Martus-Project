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
package org.martus.mspa.common;

import java.util.Vector;

import org.martus.util.TestCaseEnhanced;


public class TestAccountAdminOptions extends TestCaseEnhanced 
{
	public TestAccountAdminOptions(String name) 
	{
		super(name);
	}
	
	public void testGetAccountIds() throws Exception
	{								
		try
		{			
			AccountAdminOptions accountOptions = new AccountAdminOptions();				
			accountOptions.setBannedOption(true);
			accountOptions.setCanSendOption(false);
			accountOptions.setCanUploadOption(false);
			
			Vector options = accountOptions.getOptions();			
			
			assertEquals("Can upload should be in index 0", false, ((Boolean)options.get(0)).booleanValue());
			assertEquals("Banned should be in index 1", true, ((Boolean)options.get(1)).booleanValue());
			assertEquals("Can Send should be in index 2", false, ((Boolean)options.get(2)).booleanValue());
			
			options = new Vector();		
			options.add(AccountAdminOptions.CAN_UPLOAD, new Boolean(true));
			options.add(AccountAdminOptions.BANNED, new Boolean(false));
			options.add(AccountAdminOptions.CAN_SEND, new Boolean(true));

			accountOptions = new AccountAdminOptions();
			accountOptions.setOptions(options);	
			
			assertEquals("Can upload ", true, accountOptions.canUploadSelected());
			assertEquals("Banned ", false, accountOptions.isBannedSelected());
			assertEquals("Can Send ", true, accountOptions.canSendToAmplifySelected());
											
		}	
		catch(Exception e)
		{
			assertTrue("options not supported", false);
		}			
	}			

}
