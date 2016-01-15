/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2014, Beneficent
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

import org.martus.common.LoggerInterface;
import org.martus.common.LoggerToConsole;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;

public class ServerMetaDatabaseForTesting extends ServerMetaDatabase
{
	public static ServerMetaDatabaseForTesting getEmptyDatabase(TestCaseEnhanced testCase) throws Exception
	{
		if(singletonDatabase == null)
			singletonDatabase = createFactoryForTesting(testCase);
		
		singletonDatabase.reopen();
		singletonDatabase.deleteAllData();
		
		return singletonDatabase;
	}
	
	public void deleteAllData() throws Exception
	{
		ServerMetaDatabaseConnection connection = getConnection();
		try
		{
			connection.forEachBulletin(uid -> connection.revisionWasRemoved(uid));
			connection.forEachAccount(accountId -> connection.removeAccount(accountId));
		}
		finally
		{
			connection.close();
		}
	}
	
	private static ServerMetaDatabaseForTesting createFactoryForTesting(TestCaseEnhanced testCase) throws Exception
	{
		File tempDirectory = testCase.createTempDirectory();
		
		ServerMetaDatabaseSchema schema = new ServerMetaDatabaseSchema();
		LoggerInterface logger = new LoggerToConsole();
		ServerMetaDatabaseForTesting newFactory = new ServerMetaDatabaseForTesting(tempDirectory, schema, logger);
		Runtime.getRuntime().addShutdownHook(new DeleteDatabaseThread(newFactory));
		
		newFactory.initializeDatabase();
		newFactory.close();
		
		return newFactory;
	}
	
	private static class DeleteDatabaseThread extends Thread
	{
		public DeleteDatabaseThread(ServerMetaDatabaseForTesting databaseToDelete)
		{
			database = databaseToDelete;
		}
		
		@Override
		public void run()
		{
			super.run();
			DirectoryUtils.deleteEntireDirectoryTree(database.getDirectory());
		}
		
		private ServerMetaDatabaseForTesting database;
	}
	
	private ServerMetaDatabaseForTesting(File directory, ServerMetaDatabaseSchema schema, LoggerInterface loggerToUse)
	{
		super(directory, schema, loggerToUse);
	}
	
	public static void setStorageToMemory()
	{
		setStorage(STORAGE_IN_MEMORY);
	}
	
	public static void setStorageToDisk()
	{
		setStorage(STORAGE_ON_DISK);
	}

	private static ServerMetaDatabaseForTesting singletonDatabase;
}
