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

import java.util.Iterator;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

public class ServerMetaDatabaseSchema implements ServerMetaDatabaseConstants
{
	public void createSchemaVertex(OrientBaseGraph graph) 
	{
		String className = classStringFromName(CLASS_NAME_SCHEMA);
		OrientVertex schemaVertex = graph.addVertex(className);
		schemaVertex.setProperty(ServerMetaDatabaseSchema.KEY_SCHEMA_VERSION, getExpectedVersion());
	}

	public int findActualSchemaVersion(OrientGraphNoTx graph) throws Exception
	{
		Iterable<Vertex> schemas = graph.getVerticesOfClass(CLASS_NAME_SCHEMA);
		Iterator<Vertex> iterator = schemas.iterator();
		Vertex schema = iterator.next();
		if(iterator.hasNext())
			throw new InvalidSchemaException("Found more than one schema vertex");
		return schema.getProperty(KEY_SCHEMA_VERSION);
	}

	public static class InvalidSchemaException extends Exception
	{
		public InvalidSchemaException(String message) 
		{
			super(message);
		}
	}
	
	public int getExpectedVersion()
	{
		return SCHEMA_VERSION;
	}

	public static String classStringFromName(String className) 
	{
		// NOTE: To specify a class in OrientDB, prefix the name with class:
		return "class:" + className;
	}

	public static String getIndexName(String className, String key) 
	{
		// NOTE: Key indexes use a naming convention of class.key, 
		// so we must avoid dots in our non-key index names
		return className + "-" + key;
	}
}
