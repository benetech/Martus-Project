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
import java.io.FileNotFoundException;

import org.junit.Test;
import org.martus.common.LoggerToConsole;
import org.martus.server.main.ServerMetaDatabase.DirectoryAlreadyExistsException;
import org.martus.server.main.ServerMetaDatabase.InvalidDatabaseException;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;

public class TestServerMetadatabaseFactory extends TestCaseEnhanced
{
	public TestServerMetadatabaseFactory(String name)
	{
		super(name);
	}

	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		tempDirectory = createTempDirectory();
		databaseDirectory = new File(tempDirectory, "database");

		// NOTE: Memory databases don't support indexes (!)
//		ServerMetaDatabaseFactory.setStorageToMemory();
	}
	
	@Override
	protected void tearDown() throws Exception 
	{
		DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		super.tearDown();
	}
	
	@Test
	public void testCreateDatabase() throws Exception
	{
		try
		{
			ServerMetaDatabase.open(databaseDirectory);
			fail("Should have thrown opening db that doesn't exist");
		}
		catch(FileNotFoundException ignoreExpected)
		{
		}
		
		ServerMetaDatabase.create(databaseDirectory);
		try
		{
			ServerMetaDatabase.create(databaseDirectory);
		}
		catch(DirectoryAlreadyExistsException ignoreExpected)
		{
		}
		
		ServerMetaDatabase factory = ServerMetaDatabase.open(databaseDirectory);
		try
		{
			assertEquals(1, factory.getActualSchemaVersion());
		}
		finally
		{
			factory.drop();
			factory.close();
		}
	}
	
	public void testOpenClose() throws Exception
	{
		ServerMetaDatabase.create(databaseDirectory);
		ServerMetaDatabase factory = ServerMetaDatabase.open(databaseDirectory);
		factory.close();
		factory = ServerMetaDatabase.open(databaseDirectory);
		factory.drop();
		factory.close();
		try
		{
			ServerMetaDatabase.open(databaseDirectory);
			fail("Should have thrown for opening a dropped database");
		}
		catch(FileNotFoundException ignoreExpected)
		{
		}
		
		ServerMetaDatabase.create(databaseDirectory);
		factory = ServerMetaDatabase.open(databaseDirectory);
		factory.drop();
		factory.close();
	}
	
	class SchemaWithDifferentVersion extends ServerMetaDatabaseSchema
	{
		public SchemaWithDifferentVersion(int delta)
		{
			expected = super.getExpectedVersion() + delta;
		}
		@Override
		public int getExpectedVersion() 
		{
			return expected;
		}
		
		int expected;
	}
	
	public void testOpenOldSchema() throws Exception 
	{
		SchemaWithDifferentVersion schema = new SchemaWithDifferentVersion(-1);
		ServerMetaDatabase.create(databaseDirectory, schema, new LoggerToConsole());
		try
		{
			ServerMetaDatabase.open(databaseDirectory).close();
			fail("Should have thrown for schema version too low");
		}
		catch(InvalidDatabaseException ignoreExpected)
		{
		}
	}
	
	public void testOpenFutureSchema() throws Exception 
	{
		SchemaWithDifferentVersion schema = new SchemaWithDifferentVersion(1);
		ServerMetaDatabase.create(databaseDirectory, schema, new LoggerToConsole());
		try
		{
			ServerMetaDatabase.open(databaseDirectory).close();
			fail("Should have thrown schema version too high");
		}
		catch(InvalidDatabaseException ignoreExpected)
		{
		}
	}
	
	private File tempDirectory;
	private File databaseDirectory;
}
