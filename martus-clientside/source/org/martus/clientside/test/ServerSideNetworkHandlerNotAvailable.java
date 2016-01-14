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

package org.martus.clientside.test;
import java.util.Vector;

import org.martus.common.network.ClientSideNetworkInterface;

public class ServerSideNetworkHandlerNotAvailable implements ClientSideNetworkInterface
{
	public Vector getServerInfo(Vector reservedForFuture)
	{
		return null;
	}
	
	public Vector getUploadRights(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}
	public Vector getSealedBulletinIds(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}
	public Vector getDraftBulletinIds(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}
	public Vector getFieldOfficeAccountIds(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}
	public Vector putBulletinChunk(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}
	public Vector getBulletinChunk(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}
	public Vector getPacket(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}
	public Vector deleteDraftBulletins(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}
	public Vector putContactInfo(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}
	public Vector getNews(
		String myAccountId,
		Vector parameters,
		String signature)
	{
		return null;
	}

	public Vector getMartusAccountAccessToken(String myAccountId, Vector parameters, String signature)
	{
		return null;
	}
	
	public Vector getMartusAccountIdFromAccessToken(String myAccountId, Vector parameters, String signature)
	{
		return null;
	}

	public Vector getServerCompliance(String myAccountId, Vector parameters, String signature)
	{
		return null;
	}

	public Vector getListOfFormTemplates(String myAccountId, Vector parameters, String signature) 
	{
		return null;
	}

	public Vector putFormTemplate(String myAccountId, Vector parameters, String signature) 
	{
		return null;
	}

	public Vector getFormTemplate(String myAccountId, Vector parameters, String signature) 
	{
		return null;
	}

	public Vector getPartialUploadStatus(String publicKeyString, Vector parameters, String signature) 
	{
		return null;
	}

	@Override
	public Vector listAvailableRevisionsSince(String myAccountId, Vector parameters, String signature)
	{
		return null;
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
	
}