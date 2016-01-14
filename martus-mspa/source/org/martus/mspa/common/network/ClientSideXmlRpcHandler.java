/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2004-2007, Beneficent
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

package org.martus.mspa.common.network;

import java.io.IOException;
import java.util.Vector;

import org.martus.common.network.MartusXmlrpcClient;


public class ClientSideXmlRpcHandler 
       extends MartusXmlrpcClient 
       implements NetworkInterface, NetworkInterfaceXmlRpcConstants
{
	
	public ClientSideXmlRpcHandler(String serverIpAddr, int portToUse) throws Exception
	{
		super(serverIpAddr, portToUse);		
	}
	
	public Vector ping() throws Exception
	{
		return (Vector)callServer(cmdPing, new Vector());
	}
	
	public Vector getAccountIds(String myAccountId, Vector parameters, String signature) throws IOException
	{
		
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(cmdGetAccountIds, params);
	}	
	
	public Vector getContactInfo(String myAccountId, Vector parameters, String signature, String accountId) throws IOException
	{
		
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		params.add(accountId);
		return (Vector)callServer(cmdGetContactInfo, params);
	}	
	
	public Vector getServerCompliance(String myAccountId) throws IOException
	{
		Vector params = new Vector();
		params.add(myAccountId);	
		return (Vector)callServer(cmdGetCompliance, params);
	}
	
	public Vector updateServerCompliance(String myAccountId, String compliantsMsg) throws IOException
	{
		Vector params = new Vector();
		params.add(myAccountId);	
		params.add(compliantsMsg);
		return (Vector)callServer(cmdUpdateCompliance, params);
	}
	
	public Vector getAccountManageInfo(String myAccountId, String manageAccountId) throws IOException	
	{		
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(manageAccountId);		
		return (Vector)callServer(cmdGetAccountManageInfo, params);
	}
	
	public Vector updateAccountManageInfo(String myAccountId, String manageAccountId, 
				Vector accountInfo) throws IOException
	{	
		Vector params = new Vector();		
		params.add(myAccountId);		
		params.add(manageAccountId);
		params.add(accountInfo);
		return (Vector)callServer(cmdUpdateAccountManageInfo, params);
	}
	
	public Vector sendCommandToServer(String myAccountId, String cmdType, String cmd) throws IOException
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(cmdType);		
		params.add(cmd);
		return (Vector)callServer(cmdSendCommandToServer, params);
	}	
	
	public Vector getListOfHiddenBulletinIds(String myAccountId, String manageAccountId) throws IOException
	{
		Vector params = new Vector();
		params.add(myAccountId);		
		params.add(manageAccountId);
		return (Vector)callServer(cmdGetListOfHiddenBulletinIds, params);
	}
	
	public Vector hideBulletins(String myAccountId, String manageAccountId, Vector localIds) throws IOException
	{
		Vector params = new Vector();
		params.add(myAccountId);	
		params.add(manageAccountId);
		params.add(localIds);	
		return (Vector)callServer(cmdHideBulletins, params);
	}
	
	public Vector unhideBulletins(String myAccountId, String manageAccountId, Vector localIds) throws IOException
	{
		Vector params = new Vector();
		params.add(myAccountId);	
		params.add(manageAccountId);
		params.add(localIds);	
		return (Vector)callServer(cmdUnhideBulletins, params);
	}
	
	
	public Vector getListOfBulletinIds(String myAccountId) throws IOException	
	{		
		Vector params = new Vector();
		params.add(myAccountId);			
		return (Vector)callServer(cmdGetListOfBulletinIds, params);
	}
	
	public Vector getInactiveMagicWords(String myAccountId) throws IOException
	{
		
		Vector params = new Vector();
		params.add(myAccountId);		
		return (Vector)callServer(cmdGetInActiveMagicWords, params);
	}	
	
	public Vector getAllMagicWords(String myAccountId) throws IOException
	{
		
		Vector params = new Vector();
		params.add(myAccountId);	
		return (Vector)callServer(cmdGetAllMagicWords, params);
	}	
	
	public Vector getActiveMagicWords(String myAccountId) throws IOException
	{		
		Vector params = new Vector();
		params.add(myAccountId);	
		return (Vector)callServer(cmdGetActiveMagicWords, params);
	}				
	
	public Vector updateMagicWords(String myAccountId, Vector magicWords) throws IOException
	{	
		Vector params = new Vector();		
		params.add(myAccountId);		
		params.add(magicWords);
		return (Vector)callServer(cmdUpdateMagicWords, params);
	}	
	
	public Vector getListOfAvailableServers(String myAccountId) throws IOException
	{		
		Vector params = new Vector();
		params.add(myAccountId);	
		return (Vector)callServer(cmdGetListOfAvailableServers, params);
	}
	
	public Vector getListOfAssignedServers(String myAccountId, int mirrorType) throws IOException
	{		
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(new Integer(mirrorType));	
		return (Vector)callServer(cmdGetListOfAssignedServers, params);
	}
	
	public Vector addAvailableServer(String myAccountId, Vector mirrorInfo) throws IOException
	{	
		Vector params = new Vector();		
		params.add(myAccountId);		
		params.add(mirrorInfo);
		
		return (Vector)callServer(cmdAddAvailableServer, params);
	}	
	
	public Vector updateAssignedServers(String myAccountId, Vector mirrorInfo, int manageType) throws IOException
	{	
		Vector params = new Vector();		
		params.add(myAccountId);		
		params.add(mirrorInfo);
		params.add(new Integer(manageType));	
		
		return (Vector)callServer(cmdUpdateAssignedServers, params);
	}	
	
	public Vector getMartusServerArguments(String myAccountId) throws IOException
	{
		Vector params = new Vector();
		params.add(myAccountId);	
		return (Vector)callServer(cmdGetMartusServerArguments, params);
	}
	
	public Vector updateMartusServerArguments(String myAccountId, Vector args) throws IOException
	{
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(args);	
		return (Vector)callServer(cmdUpdateMartusServerArguments, params);
	}
		
	public Object callServer(String method, Vector params) throws IOException
	{		
		return callserver(serverObjectName, method, params);
	}	
}
