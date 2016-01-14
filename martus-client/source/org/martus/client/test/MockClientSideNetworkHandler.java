/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2012, Beneficent
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
package org.martus.client.test;

import java.util.Vector;

import org.martus.common.network.ClientSideNetworkInterface;
import org.martus.common.network.ServerSideNetworkInterface;

public class MockClientSideNetworkHandler implements ClientSideNetworkInterface
{
	public MockClientSideNetworkHandler(ServerSideNetworkInterface serverToUse)
	{
		server = serverToUse;
	}

	public void setTimeoutGetServerInfo(int torGetServerInfoTimeoutSeconds)
	{
	}

	@Override
	public int getTimeoutSecondsForGetServerInfo() 
	{
		return 1;
	}

	@Override
	public int getTimeoutSecondsForOtherCalls() 
	{
		return 1;
	}
	
	@Override
	public String getServerIpAddress()
	{
		return null;
	}
	
	// begin ServerInterface
	public Vector getServerInfo(Vector reservedForFuture)
	{
		return server.getServerInfo(reservedForFuture);
	}

	public Vector getUploadRights(String myAccountId, Vector parameters, String signature)
	{
		return server.getUploadRights(myAccountId, parameters, signature);
	}

	public Vector getSealedBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		Vector fixedParameters = getFixedAuthorPlusRetrieveTagsVector(parameters);
		return server.getSealedBulletinIds(myAccountId, fixedParameters, signature);
	}

	public Vector getDraftBulletinIds(String myAccountId, Vector parameters, String signature)
	{
		Vector fixedParameters = getFixedAuthorPlusRetrieveTagsVector(parameters);
		return server.getDraftBulletinIds(myAccountId, fixedParameters, signature);
	}

	public Vector getFieldOfficeAccountIds(String myAccountId, Vector parameters, String signature)
	{
		return server.getFieldOfficeAccountIds(myAccountId, parameters, signature);
	}

	public Vector putBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		return server.putBulletinChunk(myAccountId, parameters, signature);
	}

	public Vector getBulletinChunk(String myAccountId, Vector parameters, String signature)
	{
		return server.getBulletinChunk(myAccountId, parameters, signature);
	}

	public Vector getPacket(String myAccountId, Vector parameters, String signature)
	{
		return server.getPacket(myAccountId, parameters, signature);
	}

	public Vector deleteDraftBulletins(String myAccountId, Vector parameters, String signature)
	{
		return server.deleteDraftBulletins(myAccountId, parameters, signature);
	}

	public Vector putContactInfo(String myAccountId, Vector parameters, String signature)
	{
		return server.putContactInfo(myAccountId, parameters, signature);
	}

	public Vector getNews(String myAccountId, Vector parameters, String signature)
	{
		return server.getNews(myAccountId, parameters, signature);
	}
	
	public Vector getMartusAccountAccessToken(String myAccountId, Vector parameters, String signature)
	{
		return server.getMartusAccountAccessToken(myAccountId, parameters, signature);
	}

	public Vector getMartusAccountIdFromAccessToken(String myAccountId, Vector parameters, String signature)
	{
		return server.getMartusAccountIdFromAccessToken(myAccountId, parameters, signature);
	}
	
	public Vector getServerCompliance(String myAccountId, Vector parameters, String signature)
	{
		return server.getServerCompliance(myAccountId, parameters, signature);
	}

	public Vector getListOfFormTemplates(String myAccountId, Vector parameters, String signature) 
	{
		return server.getListOfFormTemplates(myAccountId, parameters, signature);
	}

	public Vector putFormTemplate(String myAccountId, Vector parameters, String signature) 
	{
		return server.putFormTemplate(myAccountId, parameters, signature);
	}

	public Vector getFormTemplate(String myAccountId, Vector parameters, String signature) 
	{
		return server.getFormTemplate(myAccountId, parameters, signature);
	}

	public Vector getPartialUploadStatus(String publicKeyString, Vector parameters, String signature) 
	{
		return server.getPartialUploadStatus(publicKeyString, parameters, signature);
	}

	private Vector getFixedAuthorPlusRetrieveTagsVector(Vector parameters)
	{
		// NOTE: First element of parameters is Author key String, 
		// second is optional, but if present is a vector of tags to retrieve
		Vector fixedParameters = new Vector();
		fixedParameters.add(parameters.get(0));

		if(parameters.size() >= 2)
		{
			Vector vector = (Vector)parameters.get(1);
			fixedParameters.add(vector.toArray());
		}
		return fixedParameters;
	}

	@Override
	public Vector listAvailableRevisionsSince(String myAccountId, Vector parameters, String signature)
	{
		return server.listAvailableRevisionsSince(myAccountId, parameters, signature);
	}

	private ServerSideNetworkInterface server;

}
