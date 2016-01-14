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
package org.martus.server.forclients;

import java.io.File;
import java.util.Vector;
import org.martus.common.LoggerToNull;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;


public class TestAuthorizeLog extends TestCaseEnhanced
{
	public TestAuthorizeLog(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		if(security == null)
		{
			security = MockMartusSecurity.createServer();
		}
		tempDir = createTempDirectory();
		File authorizeLogFile = new File(tempDir, "$$AuthorizeLog.txt");
		authorizeLogFile.deleteOnExit();
		authorized = new AuthorizeLog(security, new LoggerToNull(), authorizeLogFile);
	}
	
	public void tearDown()
	{
		DirectoryUtils.deleteEntireDirectoryTree(tempDir);
	}
	
	public void testAuthorizeLogLoadSaveFile() throws Exception
	{
		authorized.loadFile();
		String lineToAdd = "date	publicCode	ip	group";
		authorized.appendToFile(new AuthorizeLogEntry(lineToAdd));
		authorized.loadFile();
	}
	
	public void testGetAuthorizedClientStrings() throws Exception
	{
		String newClient = "date2	newClientPublicCode	ip2	group2";
		authorized.appendToFile(new AuthorizeLogEntry(newClient));
		
		Vector currentClients = authorized.getAuthorizedClientStrings();
		assertContains("new client not added?", newClient, currentClients);
		authorized.loadFile();
		Vector currentClients2 = authorized.getAuthorizedClientStrings();
		assertContains("new client not added after load?", newClient, currentClients2);
	}
	
	public void testGetAuthorizedLogEntryForClient() throws Exception
	{
		String date1 = "date1";
		String clientPublicCode1 = "publiccode1";
		String ip1 = "ip1";
		String group1 = "group1";
		String newClient = date1 + "	" + clientPublicCode1 + "	" + ip1 + "	" + group1;
		authorized.appendToFile(new AuthorizeLogEntry(newClient));
		AuthorizeLogEntry invalidEntry = authorized.getAuthorizedClientEntry("unknownPublicCode");
		assertNull("Client should not exist?", invalidEntry);
		AuthorizeLogEntry clientEntry = authorized.getAuthorizedClientEntry(clientPublicCode1);
		assertNotNull("Client doesn't exist?", clientEntry);
		assertEquals(date1, clientEntry.getDate());
		assertEquals(clientPublicCode1, clientEntry.getPublicCode());
		assertEquals(ip1, clientEntry.getIp());
		assertEquals(group1, clientEntry.getGroupName());
	}
	
	public void testLowLevelAuthorizeLogToFile() throws Exception
	{
		File tempFile1 = createTempFileFromName("$$$MartusTestFileAuthorizeLogLowLevel");
		UnicodeWriter writer = new UnicodeWriter(tempFile1);

		String date = "2004-01-01";
		String date2 = "2004-01-02";
		String code = "1234.1234.1234.1234";
		String code2 = "1234.4567.1234.1234";
		String group = "My group";
		String group2 = "My group2";
		String ip = "1.2.3.4";
		String ip2 = "2.2.2.2";
		String client1 = date + AuthorizeLogEntry.FIELD_DELIMITER + code + AuthorizeLogEntry.FIELD_DELIMITER + ip + AuthorizeLogEntry.FIELD_DELIMITER + group ;
		String client2 = date2 + AuthorizeLogEntry.FIELD_DELIMITER + code2 + AuthorizeLogEntry.FIELD_DELIMITER + ip2 + AuthorizeLogEntry.FIELD_DELIMITER + group2 ;
		
		writer.writeln(client1);
		writer.close();
		MartusServerUtilities.createSignatureFileFromFileOnServer(tempFile1, security);
		
		AuthorizeLog log = new AuthorizeLog(security, new LoggerToNull(), tempFile1);	
		log.loadFile();
		Vector clientStrings = log.getAuthorizedClientStrings();
		assertEquals("Size incorrect", 1, clientStrings.size());
		assertEquals("Client 1 not found", client1, clientStrings.get(0));
		
		AuthorizeLogEntry newClient = new AuthorizeLogEntry(client2);
		log.appendToFile(newClient);
		clientStrings = log.getAuthorizedClientStrings();
		assertEquals("new Size incorrect", 2, clientStrings.size());
		assertContains("Client 2 not found", client2, clientStrings);
		
		UnicodeReader reader = new UnicodeReader(tempFile1);
		String line1 = reader.readLine();
		assertEquals("Line 1 should match", client1, line1);
		String line2 = reader.readLine();
		assertEquals("Line 2 should match", client2, line2);
		assertNull("There shouldn't be a line 3", reader.readLine());
		reader.close();

		MartusServerUtilities.deleteSignaturesForFile(tempFile1);
		tempFile1.delete();
	}
	
	AuthorizeLog authorized;
	File tempDir;
	static MartusCrypto security;
}
