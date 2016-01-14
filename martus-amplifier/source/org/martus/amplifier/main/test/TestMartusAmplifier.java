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
package org.martus.amplifier.main.test;

import java.io.File;
import java.util.List;

import org.martus.amplifier.ServerCallbackInterface;
import org.martus.amplifier.datasynch.BackupServerInfo;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.common.LoggerToNull;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeWriter;

public class TestMartusAmplifier extends TestCaseEnhanced
{
	public TestMartusAmplifier(String name)
	{
		super(name);
	}
	
	public void testGetBackupServersList() throws Exception
	{
		MockMartusSecurity security = MockMartusSecurity.createServer();
		File dir = createTempFile();
		dir.delete();
		dir.mkdirs();
		
		MockMartusServer server = new MockMartusServer(dir, new LoggerToNull(), security);
		MartusAmplifier amp =  new MartusAmplifier(server); 
		
		List noServers = amp.loadServersWeWillCall(dir, security);
		assertEquals(0, noServers.size());
		
		String ip = "2.4.6.8";
		File keyFile = new File(dir, "ip=" + ip);
		MartusUtilities.exportServerPublicKey(security, keyFile);
		
		List oneServer = amp.loadServersWeWillCall(dir, security);
		assertEquals(1, oneServer.size());

		BackupServerInfo info = (BackupServerInfo)oneServer.get(0);
		String result = info.getAddress();
		assertEquals("ip", ip, result);
		
		result = info.getName();
		assertEquals("name", ip, result);
		
		int intResult = info.getPort();
		int port = 985;
		if(!server.isSecureMode())
			port += ServerCallbackInterface.DEVELOPMENT_MODE_PORT_DELTA;
		assertEquals(port, intResult);
		
		keyFile.delete();
		DirectoryUtils.deleteEntireDirectoryTree(dir);
		assertFalse(dir.getPath() + " still exists?", dir.exists());	
	}
	
	public void testLoadAccountsWeWillNotAmplify() throws Exception
	{
		File unamplified = createTempFile();

		MockMartusSecurity security = MockMartusSecurity.createServer();

		MockMartusServer server = new MockMartusServer(unamplified, new LoggerToNull(), security);
		MartusAmplifier amp =  new MartusAmplifier(server); 
		
		assertNull("List should be null", amp.getListOfAccountsWeWillNotAmplify());
		
		amp.loadAccountsWeWillNotAmplify(null);
		List noAccounts = amp.getListOfAccountsWeWillNotAmplify();
		assertEquals("should be 0",0, noAccounts.size());

		UnicodeWriter writer = new UnicodeWriter(unamplified);
		String account1 = "account 1";
		String account2 = "account 2";
		writer.writeln(account1);	
		writer.writeln(account2);	
		writer.close();
		amp.loadAccountsWeWillNotAmplify(unamplified);
		
		List twoAccounts = amp.getListOfAccountsWeWillNotAmplify(); 
		assertEquals("List should have 2 entries", 2, twoAccounts.size());
		assertEquals("No account 1", account1, twoAccounts.get(0));
		assertEquals("No account 2", account2, twoAccounts.get(1));
	}
	BackupServerInfo testInfo;
}
