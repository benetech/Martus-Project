/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2007, Beneficent
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

package org.martus.amplifier.network;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import org.martus.common.CallerSideAmplifierNetworkInterface;
import org.martus.common.network.MartusXmlrpcClient;


public class AmplifierClientSideXmlrpcHandler extends MartusXmlrpcClient
	implements AmplifierNetworkInterfaceXmlRpcConstants, CallerSideAmplifierNetworkInterface
{
	public AmplifierClientSideXmlrpcHandler(String serverName, int portToUse) throws SSLSocketSetupException
	{
		super(serverName, portToUse);
	}

	public Vector getAccountIds(String myAccountId, Vector parameters, String signature) throws IOException
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(cmdGetAccountIds, params);
	}

	public Vector getContactInfo(String myAccountId, Vector parameters, String signature) throws IOException
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(cmdGetContactInfo, params);
	}

	public Vector getPublicBulletinLocalIds(String myAccountId, Vector parameters, String signature) throws IOException
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(cmdGetPublicBulletinLocalIds, params);
		
	}

	public Vector getAmplifierBulletinChunk(String myAccountId, Vector parameters, String signature) throws Exception 
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(cmdGetAmplifierBulletinChunk, params);
	}

	public Object callServer(String method, Vector params) throws IOException
	{
		String serverObjectName = "MartusAmplifierServer";
		
		Object result = callserver(serverObjectName, method, params);
		Object[] resultAsArray = (Object[]) result;
		return new Vector(Arrays.asList(resultAsArray));
	}

}
