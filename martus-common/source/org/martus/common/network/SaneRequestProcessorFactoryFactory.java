/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2010, Beneficent
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
package org.martus.common.network;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory.StatelessProcessorFactoryFactory;

public class SaneRequestProcessorFactoryFactory extends StatelessProcessorFactoryFactory
{
	public SaneRequestProcessorFactoryFactory(Object serverObject, Class classThatDefinesTheApi)
	{
		server = serverObject;
		apiClass = classThatDefinesTheApi;
	}
	
	@Override
	protected Object getRequestProcessor(Class pClass) throws XmlRpcException
	{
		return server;
	}
	
	public Class getActualServerClass()
	{
		return apiClass;
	}
	
	@Override
	public RequestProcessorFactory getRequestProcessorFactory(Class serverClass) throws XmlRpcException
	{
		return new SaneRequestProcessorFactory();
	}
	
	class SaneRequestProcessorFactory implements RequestProcessorFactory
	{
		public Object getRequestProcessor(XmlRpcRequest request) throws XmlRpcException
		{
			return server;
		}
		
	}

	protected Object server;
	private Class apiClass;
}
