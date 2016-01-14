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
package org.martus.mspa.server.test;

import java.io.File;
import java.io.IOException;

import org.martus.mspa.roothelper.Messenger;
import org.martus.mspa.server.LoadMartusServerArguments;
import org.martus.mspa.server.LoadProperty;
import org.martus.mspa.server.MSPAServer;
import org.martus.util.TestCaseEnhanced;


public class TestServerArgumentsConfig extends TestCaseEnhanced
{
	public TestServerArgumentsConfig(String name)
	{
		super(name);
		setup();
	}	
	
	private void setup()
	{		
		try
		{
			tempFile = createTempFileFromName("$$$MartusLoadPropertyFile");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}	
	
	public void testLoadDefaultArguments()
	{
		LoadMartusServerArguments arguments= MSPAServer.loadDefaultMartusServerArguments(tempFile.getPath());	
		
		assertEquals("The listener ip should be null", arguments.getListenerIP(), "");
		assertEquals("The password should be no", arguments.getPassword(), "no");
		assertEquals("The amplifier ip should be null", arguments.getAmplifierIP(), "");
		assertEquals("The amplifier indexing munutes should be 5", arguments.getMinutes(), "5");
		assertFalse("The amplifier status should be no", arguments.getAmplifierStatus());
		assertFalse("The client listener status should be no", arguments.getClientListenerStatus());
		assertFalse("The mirror status should be no", arguments.getMirrorListenerStatus());
		assertFalse("The amplifier listener should be no", arguments.getAmplifierListenerStatus());
				
		tempFile.delete(); 			
	}
	
	public void testLoadProperty()
	{
		File propFile;
		try
		{
			propFile = createTempFileFromName("$$$MartusPropertyFile");
			MSPAServer.loadDefaultMartusServerArguments(propFile.getPath());
			
			LoadProperty props = new LoadProperty(propFile.getPath());
			
			assertEquals("The listener ip", props.getValue(LoadMartusServerArguments.LISTENER_IP), "");
			assertEquals("The password", props.getValue(LoadMartusServerArguments.PASSWORD), "no");
			assertEquals("The amplifier ip", props.getValue(LoadMartusServerArguments.AMPLIFIER_IP), "");
			assertEquals("The amplifier indexing munutes", props.getValue(LoadMartusServerArguments.AMPLIFIER_INDEXING_MINUTES), "5");
			assertEquals("The amplifier status", props.getValue(LoadMartusServerArguments.AMPLIFIER), "no");
			assertEquals("The client listener status", props.getValue(LoadMartusServerArguments.CLIENT_LISTENER), "no");
			assertEquals("The mirror status", props.getValue(LoadMartusServerArguments.MIRROR_LISTENER), "no");
			assertEquals("The amplifier listener", props.getValue(LoadMartusServerArguments.AMPLIFIER_LISTENER), "no");						
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	public void tearDown() throws Exception
	{				
		super.tearDown();
	}
	
	Messenger messenger;
	File tempFile;	
	File martusDeleteOnStartDir;
}
