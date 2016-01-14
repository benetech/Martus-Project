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
package org.martus.server.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.martus.server.forclients.MockMartusServer;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;


public class TestServerDeleteStartupFilesAndFolders extends TestCaseEnhanced
{
	public TestServerDeleteStartupFilesAndFolders(String name)
	{
		super(name);
	}

	public void testStartupFiles() throws Exception
	{
		MockMartusServer testServer = new MockMartusServer();
		testServer.enterSecureMode();
		File triggerDirectory = testServer.getTriggerDirectory();
		triggerDirectory.deleteOnExit();
		triggerDirectory.mkdir();
		triggerDirectory.delete();
		
		File startupDirectory = testServer.getStartupConfigDirectory();
		startupDirectory.deleteOnExit();
		startupDirectory.mkdir();
		
		assertTrue("StartupDirectory should exist.", startupDirectory.exists());

		Vector startupFiles = testServer.getDeleteOnStartupFiles();
		createFiles(startupFiles);
		
		Vector unexpectedFile = new Vector();
		File tmpFile = new File(startupDirectory, "$$$unexpected.txt");
		tmpFile.deleteOnExit();
		unexpectedFile.add(tmpFile);
		createFiles(unexpectedFile);
		assertTrue("unexpected file doesn't exist?", tmpFile.exists());
		assertTrue("Should be an unexpected file", testServer.anyUnexpectedFilesOrFoldersInStartupDirectory());
		assertFalse("Directory will contain unexpected file", testServer.deleteStartupFiles());

		tmpFile.delete();
		createFiles(startupFiles);
		assertFalse("Should not be any unexpected files", testServer.anyUnexpectedFilesOrFoldersInStartupDirectory());
		assertTrue("Directory should be empty", testServer.deleteStartupFiles());
		
		startupDirectory.delete();
		assertFalse("StartupDirectory should not still exist.", startupDirectory.exists());
	}

	public void testStartupFolders() throws Exception
	{
		MockMartusServer testServer = new MockMartusServer();
		testServer.enterSecureMode();
		File triggerDirectory = testServer.getTriggerDirectory();
		triggerDirectory.deleteOnExit();
		triggerDirectory.mkdir();
		triggerDirectory.delete();
		
		File startupDirectory = testServer.getStartupConfigDirectory();
		startupDirectory.deleteOnExit();
		startupDirectory.mkdir();
		
		assertTrue("StartupDirectory should exist.", startupDirectory.exists());

		Vector startupFolders = testServer.getDeleteOnStartupFolders();
		createFoldersWithData(startupFolders);
		
		Vector unexpectedFolder = new Vector();
		File tmpFolder = new File(startupDirectory, "$$$unexpectedFolder");
		tmpFolder.deleteOnExit();
		unexpectedFolder.add(tmpFolder);
		createFoldersWithData(unexpectedFolder);
		assertTrue("unexpected folder doesn't exist?", tmpFolder.exists());
		assertTrue("Should be an unexpected folder", testServer.anyUnexpectedFilesOrFoldersInStartupDirectory());
		assertFalse("Directory will contain unexpected folder", testServer.deleteStartupFiles());

		DirectoryUtils.deleteEntireDirectoryTree(tmpFolder);
		createFoldersWithData(startupFolders);
		assertFalse("Should not be any unexpected folders", testServer.anyUnexpectedFilesOrFoldersInStartupDirectory());
		assertTrue("Directory should be empty", testServer.deleteStartupFiles());
		
		startupDirectory.delete();
		assertFalse("StartupDirectory should not still exist.", startupDirectory.exists());
	}
	
	
	private void createFiles(Vector startupFiles) throws FileNotFoundException, IOException
	{
		for(int i = 0; i<startupFiles.size(); ++i )
		{	
			File tmp = (File)startupFiles.get(i);
			tmp.deleteOnExit();
			FileOutputStream out = new FileOutputStream(tmp);
			out.write(1);
			out.close();
		}
	}
	
	
	private void createFoldersWithData(Vector startupFolders) throws FileNotFoundException, IOException
	{
		for(int i = 0; i<startupFolders.size(); ++i )
		{	
			File folder = (File)startupFolders.get(i);
			folder.deleteOnExit();
			folder.mkdir();
			File tmp = File.createTempFile("$$$Tmp", ".txt", folder);
			tmp.deleteOnExit();
		}
	}
}
