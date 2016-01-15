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

import org.martus.common.LoggerInterface;
import org.martus.common.LoggerToConsole;
import org.martus.common.MartusLogger;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

public class ServerMetaDatabase implements ServerMetaDatabaseConstants
{
	public static void create(File databaseDirectory) throws Exception
	{
		create(databaseDirectory, new LoggerToConsole());
	}
	
	public static void create(File databaseDirectory, LoggerInterface loggerToUse) throws Exception
	{
		create(databaseDirectory, new ServerMetaDatabaseSchema(), loggerToUse);
	}

	public static ServerMetaDatabase open(File databaseDirectory) throws Exception
	{
		return open(databaseDirectory, new LoggerToConsole());
	}
	
	public static ServerMetaDatabase open(File databaseDirectory, LoggerInterface loggerToUse) throws Exception
	{
		if(!exists(databaseDirectory))
			throw new FileNotFoundException("Directory missing: " + databaseDirectory);
		
		ServerMetaDatabaseSchema schema = new ServerMetaDatabaseSchema();
		ServerMetaDatabase factory = new ServerMetaDatabase(databaseDirectory, schema, loggerToUse);
		try
		{
			factory.throwIfNotUsable();
			return factory;
		}
		catch(Exception e)
		{
			factory.drop();
			throw(e);
		}
	}

	public static void disableSnappyCompression()
	{
		// NOTE: By default, OrientDB loads the Snappy library,
		// which is native code, into /tmp and executes from there.
		// That is a security problem, so we disable compression.
		String compressionKey = "storage.compressionMethod";
		String compressionWas = System.getProperty(compressionKey);
		System.setProperty(compressionKey, "nothing");
		MartusLogger.log("OrientDB compression disabled (was '" + compressionWas + "')");
	}

	public ServerMetaDatabaseConnection getConnection() 
	{
		OrientGraph tx = factory.getTx();
		return new ServerMetaDatabaseConnection(tx);
	}
	
	interface WithConnection
	{
		public void accept(ServerMetaDatabaseConnection database) throws Exception;
	}
	
	public void doWithConnection(WithConnection doer) throws Exception
	{
		ServerMetaDatabaseConnection connection = getConnection();
		try
		{
			doer.accept(connection);
		}
		finally
		{
			connection.close();
		}
	}

	public void reopen()
	{
		factory = new OrientGraphFactory(getStorage() + directory.getAbsolutePath());
	}

	public void close()
	{
		factory.close();
		factory = null;
	}
	
	public void deleteAllData() throws Exception
	{
		throw new Exception("deleteAllData is only supported in test classes");
	}

	public static boolean exists(File databaseDirectory) 
	{
		if(!databaseDirectory.exists())
			return false;
		
		if(databaseDirectory.listFiles().length == 0)
			return false;
		
		return true;
	}
	
	protected int getActualSchemaVersion() throws Exception
	{
		OrientGraphNoTx graph = factory.getNoTx();
		try
		{
			return schema.findActualSchemaVersion(graph);
		}
		finally
		{
			graph.shutdown();
		}
	}
	
	protected static void create(File databaseDirectory, ServerMetaDatabaseSchema schema, LoggerInterface loggerToUse) throws Exception 
	{
		if(exists(databaseDirectory))
			throw new DirectoryAlreadyExistsException();
		
		databaseDirectory.mkdirs();
		ServerMetaDatabase factory = new ServerMetaDatabase(databaseDirectory, schema, loggerToUse);
		try
		{
			factory.initializeDatabase();
		}
		finally
		{
			factory.close();
		}
	}
	
	public static class DirectoryAlreadyExistsException extends Exception
	{
	}
	
	public static class InvalidDatabaseException extends Exception
	{
		public InvalidDatabaseException(String message)
		{
			super(message);
		}
	}
	
	protected ServerMetaDatabase(File databaseDirectory, ServerMetaDatabaseSchema schemaToUse, LoggerInterface loggerToUse)
	{
		directory = databaseDirectory;
		schema = schemaToUse;
		logger = loggerToUse;

		disableSnappyCompression();

		reopen();
	}

	private void throwIfNotUsable() throws Exception
	{
		int actual = getActualSchemaVersion();
		int expected = schema.getExpectedVersion();
		if(actual != expected)
			throw new InvalidDatabaseException("Incorrect schema (was " + actual + " but expected " + expected);
	}

	protected void initializeDatabase() 
	{
		logger.logInfo("Database initializing");
		OrientBaseGraph graph = factory.getNoTx();
		try
		{
			graph.createVertexType(CLASS_NAME_SCHEMA);
			
			graph.createVertexType(CLASS_NAME_ACCOUNT);
			createUniqueVertexIndex(graph, CLASS_NAME_ACCOUNT, KEY_ACCOUNT_PUBLIC_CODE);

			graph.createVertexType(CLASS_NAME_BULLETIN);
			createUniqueVertexIndex(graph, CLASS_NAME_BULLETIN, KEY_BULLETIN_DUID);
			
			graph.createEdgeType(CLASS_NAME_CAN_DOWNLOAD);
			createEdgeIndex(graph, CLASS_NAME_CAN_DOWNLOAD, KEY_CAN_DOWNLOAD_PUBCODETIMESTAMP);
			
			graph.createEdgeType(CLASS_NAME_WAS_AUTHORED_BY);
		}
		finally
		{
			graph.shutdown();
		}
		
		graph = factory.getTx();
		try
		{
			schema.createSchemaVertex(graph);
		}
		finally
		{
			graph.shutdown();
		}
		logger.logInfo("Database initialized");
	}
	
	private void createUniqueVertexIndex(OrientBaseGraph graph, String className, String key) 
	{
		// NOTE: KeyIndexes cannot be directly manipulated
		Class<Vertex> klass = Vertex.class;
		Parameter only = createParameterOnly(className);
		Parameter unique = createParameterUniqueIndex();
		graph.createIndex(ServerMetaDatabaseSchema.getIndexName(className, key), klass, only, unique);
	}
	
	private void createEdgeIndex(OrientBaseGraph graph, String className, String key)
	{
		// NOTE: KeyIndexes cannot be directly manipulated
		Class<Edge> klass = Edge.class;
		Parameter only = createParameterOnly(className);
		Parameter unique = createParameterUniqueIndex();
		graph.createIndex(ServerMetaDatabaseSchema.getIndexName(className, key), klass, only, unique);
	}

	private Parameter createParameterOnly(String className) 
	{
		return new Parameter("class", className);
	}

	private Parameter createParameterUniqueIndex() 
	{
		return new Parameter("type", "UNIQUE");
	}

	protected File getDirectory()
	{
		return directory;
	}

	public void drop()
	{
		factory.drop();
	}
	
	private static String getStorage()
	{
		return storage;
	}
	
	protected static void setStorage(String newStorage)
	{
		storage = newStorage;
	}
	
	protected static final String STORAGE_IN_MEMORY = "memory:";
	protected static final String STORAGE_ON_DISK = "plocal:";

	private static String storage = STORAGE_ON_DISK;
	
	private ServerMetaDatabaseSchema schema;
	private OrientGraphFactory factory;
	private File directory;
	
	private LoggerInterface logger;
}
