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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.martus.common.ContactInfo;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.mspa.roothelper.Status;
import org.martus.mspa.server.LoadMartusServerArguments;
import org.martus.mspa.server.MSPAServer;

public class ServerSideHandler implements NetworkInterface
{
	public ServerSideHandler(MSPAServer serverToUse)
	{
		server = serverToUse;			
	}
	
	public Vector ping()
	{
		String version = server.ping();
		Vector data = new Vector();
		data.add(version);
		
		Vector result = new Vector();
		result.add(NetworkInterfaceConstants.OK);
		result.add(data);
			
		return result;	
	}

	public Vector getAccountIds(String myAccountId, Vector parameters, String signature)
	{
		class AccountVisitor implements Database.AccountVisitor
		{
			AccountVisitor()
			{
				accounts = new Vector();
			}
	
			public void visit(String accountString)
			{																					
				accounts.add(accountString);
				server.addAuthorizedMartusAccounts(accountString);	
			}
	
			public Vector getAccounts()
			{
				return accounts;
			}
			
			Vector accounts;
		}
				
		Vector result = new Vector();				
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			result.add(NetworkInterfaceConstants.NOT_AUTHORIZED);
			server.log("Client : "+NetworkInterfaceConstants.NOT_AUTHORIZED+" \npublic code: "+ myAccountId);				
			return result;
		}
					
		AccountVisitor visitor = new AccountVisitor();
		server.getDatabase().visitAllAccounts(visitor);
		result.add(NetworkInterfaceConstants.OK);
	
		MartusLogger.log("getAccountIds returning " + visitor.getAccounts().size());
		result.add(visitor.getAccounts());	
		return result;
	}
	
	public Vector getContactInfo(String myAccountId, Vector parameters, String signature, String accountId)
	{	
		Vector results = new Vector();
		
		File contactFile=null;		
		try		
		{
			if (!server.isAuthorizedMSPAClients(myAccountId))
			{
				results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);							
				return results;
			}

			contactFile = server.getDatabase().getContactInfoFile(accountId);		
			if(!contactFile.exists())
			{

				results.add(NetworkInterfaceConstants.NOT_FOUND);
				return results;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			results.add(NetworkInterfaceConstants.NOT_FOUND);
			return results;
		}

		try
		{
			Vector contactInfo = ContactInfo.loadFromFile(contactFile);
			results.add(NetworkInterfaceConstants.OK);
			results.add(contactInfo);		

			return results;
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
			return results;
		}											
	}
	
	public Vector getAccountManageInfo(String myAccountId, String manageAccountId)
	{	
		Vector results = new Vector();	
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
					
		Vector acccountAdminInfo = server.getAccountAdminInfo(manageAccountId);
		
		results.add(NetworkInterfaceConstants.OK);
		results.add(acccountAdminInfo);		

		return results;					
	}
	
	public Vector updateAccountManageInfo(String myAccountId, String manageAccountId, Vector accountInfo)
	{			
		Vector results = new Vector();
		try
		{
			if (!server.isAuthorizedMSPAClients(myAccountId))
			{
				results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
				return results;
			}
			
			server.updateAccountInfo(manageAccountId, accountInfo);								
			results.add(NetworkInterfaceConstants.OK);		
			return results;
		}
		
		catch (Exception e1)
		{
			e1.printStackTrace();
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
			return results;
		}							
	}	
	
	public Vector sendCommandToServer(String myAccountId, String cmdType, String cmd) throws IOException
	{
		Vector results = new Vector();		
		
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}

		try
		{
			Status status = executeCommand(cmdType);													
			if(status == null)
			{
				results.add(NetworkInterfaceConstants.UNKNOWN_COMMAND);
				return results;
			}
									
			if (status.isSuccess())
			{	
				results.add(NetworkInterfaceConstants.OK);
				results.add(status.getDetailText());
				return results;
			}

			results.add(NetworkInterfaceConstants.EXEC_ERROR);		
			results.add(status.getDetailText());
			return results;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			results.add(NetworkInterfaceConstants.EXEC_ERROR);		
			results.add(e.getMessage());
			return results;
		}	
	}

	private Status executeCommand(String cmdType) throws Exception
	{
		if (cmdType.equals(NetworkInterfaceConstants.START_SERVER))
			return server.startServer();
		
		if (cmdType.equals(NetworkInterfaceConstants.RESTART_SERVER))
			return server.restartServer();
		
		if (cmdType.equals(NetworkInterfaceConstants.STOP_SERVER))
			return server.stopServer();
		
		if (cmdType.equals(NetworkInterfaceConstants.GET_STATUS))
			return server.getServerStatus();
		
		return null;
	}
	
	public Vector hideBulletins(String myAccountId, String manageAccountId, Vector localIds)
	{			
		Vector results = new Vector();
		try
		{	
			if (!server.isAuthorizedMSPAClients(myAccountId))
			{
				results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
				return results;
			}
											
			boolean result = server.hideBulletins(manageAccountId, localIds);
			if (result)	
				results.add(NetworkInterfaceConstants.OK);
			else
				results.add(NetworkInterfaceConstants.NOT_FOUND);
	
			return results;
		}
		
		catch (Exception e1)
		{
			e1.printStackTrace();
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
			return results;
		}							
	}	
	
	public Vector unhideBulletins(String myAccountId, String manageAccountId, Vector localIds) throws IOException
	{
		Vector results = new Vector();
		try
		{	
			if (!server.isAuthorizedMSPAClients(myAccountId))
			{
				results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
				return results;
			}
											
			boolean result = server.recoverHiddenBulletins(manageAccountId, localIds);
			if (result)	
				results.add(NetworkInterfaceConstants.OK);
			else
				results.add(NetworkInterfaceConstants.NOT_FOUND);
	
			return results;
		}
		
		catch (Exception e1)
		{
			e1.printStackTrace();
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
			return results;
		}			
	}

	
	public Vector getListOfHiddenBulletinIds(String myAccountId, String manageAccountId) 
	{		
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}			
											
		results.add(NetworkInterfaceConstants.OK);
		results.add(server.getListOfHiddenBulletins(manageAccountId));		

		return results;		
	}
	
	public Vector getListOfBulletinIds(String myAccountId)
	{			

		class Collector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				try
				{					
					Vector info = new Vector();						
					String localId = key.getLocalId().trim();	
					if (BulletinHeaderPacket.isValidLocalId(localId))
					{
						if (server.containHiddenBulletin(key.getUniversalId()))
							return;
							
						info.add(key.getLocalId().trim());							
						if (key.isDraft())
							info.add(BulletinConstants.STATUSDRAFT);
						else if (key.isSealed())
							info.add(BulletinConstants.STATUSSEALED);				
	
						infos.add(info);
					}
				}
				catch (Exception e)
				{		
					server.log("ListBulletins: Problem when visited record for account."+ e.toString());
				}
			}
			
			Vector infos = new Vector();
		}
		
		Vector results = new Vector();	

		Collector collector = new Collector();		
		server.getDatabase().visitAllRecordsForAccount(collector, myAccountId);		
											
		results.add(NetworkInterfaceConstants.OK);
		results.add(collector.infos);		

		return results;					
	}
	
	public Vector getServerCompliance(String myAccountId)
	{
		Vector results = new Vector();	
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
					
		Vector compliances = server.getComplianceFile(myAccountId);
		
		results.add(NetworkInterfaceConstants.OK);
		results.add(compliances);		

		return results;		
	}
	
	public Vector updateServerCompliance(String myAccountId, String compliantsMsg)
	{		
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
				
		try
		{
			server.updateComplianceFile(myAccountId, compliantsMsg);
			results.add(NetworkInterfaceConstants.OK);
			return results;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			results.add(NetworkInterfaceConstants.EXEC_ERROR);
			results.add(e.getMessage());
			return results;
		}
	}
	
	public Vector getInactiveMagicWords(String myAccountId)
	{	
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
						
		Vector magicWords = server.getMagicWordsInfo().getInactiveMagicWordsWithNoSign();
		
		results.add(NetworkInterfaceConstants.OK);
		results.add(magicWords);		

		return results;					
	}

	public Vector getActiveMagicWords(String myAccountId)
	{	
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
						
		Vector magicWords = server.getActiveMagicWords();
		results.add(NetworkInterfaceConstants.OK);
		results.add(magicWords);

		return results;					
	}

	public Vector getAllMagicWords(String myAccountId)
	{			
		Vector results = new Vector();
		
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}		

		Vector magicWords = server.getAllMagicWords();			
		results.add(NetworkInterfaceConstants.OK);
		results.add(magicWords);		

		return results;		
	}								
	
	public Vector updateMagicWords(String myAccountId, Vector magicWords)
	{	
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			Vector results = new Vector();
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
				
		return writeMagicWords(magicWords);						
	}								
	
	private Vector writeMagicWords(Vector magicWords)
	{
		Vector results = new Vector();
		try
		{
			server.updateMagicWords(magicWords);								
			results.add(NetworkInterfaceConstants.OK);
			return results;
		}

		catch (Exception e1)
		{
			e1.printStackTrace();
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
			return results;
		}				
	}
	
	public Vector getListOfAvailableServers(String myAccountId)
	{			
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
				
		File availableDir = MSPAServer.getAvailableServerDirectory();	
		List list = Arrays.asList(availableDir.list());		
					
		results.add(NetworkInterfaceConstants.OK);
		results.add(new Vector(list));		

		return results;		
	}	
	
	public Vector getListOfAssignedServers(String myAccountId, int mirrorType)
	{			
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
				
		File mirrorDir = MSPAServer.getMirrorDirectory(mirrorType);	
		List list = Arrays.asList(mirrorDir.list());		
					
		results.add(NetworkInterfaceConstants.OK);
		results.add(new Vector(list));		

		return results;		
	}	
	
	public Vector updateAssignedServers(String myAccountId, Vector mirrorInfo, int mirrorType)
	{
		Vector results = new Vector();		
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
		
		server.updateAssignedServerInfo(mirrorInfo, mirrorType);								
		results.add(NetworkInterfaceConstants.OK);		
		return results;
	}
	
	public Vector addAvailableServer(String myAccountId, Vector mirrorInfo)
	{
		Vector results = new Vector();				
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
			
		try
		{
			if (server.addAvailableServer(mirrorInfo))
				results.add(NetworkInterfaceConstants.OK);
			else
				results.add(NetworkInterfaceConstants.NO_SERVER);
					
			return results;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			results.add(NetworkInterfaceConstants.EXEC_ERROR);
			results.add(e.getMessage());
			return results;
		}		
	}
	
	public Vector getMartusServerArguments(String myAccountId)
	{			
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}

		try
		{
			LoadMartusServerArguments arguments = MSPAServer.getMartusServerArguments();	
			results.add(NetworkInterfaceConstants.OK);
			results.add(arguments.convertToVector());
			return results;	
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			results.add(NetworkInterfaceConstants.EXEC_ERROR);
			results.add(e.getMessage());
			return results;
		}
	}	
	
	public Vector updateMartusServerArguments(String myAccountId, Vector args)
	{
		Vector results = new Vector();
		results.add(NetworkInterfaceConstants.UNKNOWN_COMMAND);
		return results;
		
		// This is not yet hooked up in the client so has not been tested!
//		if (!server.isAuthorizedMSPAClients(myAccountId))
//		{
//			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
//			return results;
//		}
//		
//		try
//		{
//			server.updateMartusServerArguments(args);					
//			results.add(NetworkInterfaceConstants.OK);		
//			return results;
//		}
//
//		catch (Exception e1)
//		{
//			e1.printStackTrace();
//			results.add(NetworkInterfaceConstants.SERVER_ERROR);
//			return results;
//		}		
	}	
			
	MSPAServer server;
}
