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

package org.martus.server.formirroring;

import java.util.Set;
import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.mirroring.SupplierSideMirroringInterface;
import org.martus.util.LoggerUtil;

public class SupplierSideMirroringHandler implements SupplierSideMirroringInterface, NetworkInterfaceConstants, LoggerInterface
{
	public SupplierSideMirroringHandler(ServerSupplierInterface supplierToUse, MartusCrypto verifierToUse)
	{
		supplier = supplierToUse;
		verifier = verifierToUse;
	}
	
	public Vector request(String callerAccountId, Vector parameters, String signature)
	{
		try
		{
			if(!isSignatureOk(callerAccountId, parameters, signature))
			{
				logError("SupplierSideMirroringHandler Signature Failed");
				logError("Account: " + MartusCrypto.formatAccountIdForLog(callerAccountId));
				logError("parameters: " + parameters.toString());
				logError("signature: " + signature);
				Vector result = new Vector();
				result.add(SIG_ERROR);		
				return result;
			}

			return executeCommand(callerAccountId, parameters);
		}
		catch (RuntimeException e)
		{
			logError(e);
			Vector result = new Vector();
			result.add(INVALID_DATA);
			return result;
		}

	}

	private boolean isSignatureOk(String callerAccountId, Vector parameters, String signature)
	{
		int cmd = extractCommand(parameters.get(0));
		if(cmd == cmdPing)
			return true;
		return verifier.verifySignatureOfVectorOfStrings(parameters, callerAccountId, signature);
	}
	
	Vector executeCommand(String callerAccountId, Vector parameters)
	{
		Vector result = new Vector();
		int cmd = extractCommand(parameters.get(0));

		if(!isAuthorized(cmd, callerAccountId))
		{
			logError("mirroringRequest: not authorized");
			result.add(NOT_AUTHORIZED);
			return result;
		}

		switch(cmd)
		{
			case cmdPing:
			{
				logInfo("ping");
				result.add(RESULT_OK);
				result.add(supplier.getPublicInfo());
				return result;
			}
			case cmdListAccountsForMirroring:
			{
				logInfo("listAccounts");
				Vector accounts = supplier.listAccountsForMirroring();
				logNotice("listAccounts -> " + accounts.size());
	
				result.add(OK);
				result.add(accounts.toArray());
				return result;
			}
			case cmdListBulletinsForMirroring:
			{
				logInfo("listBulletins");
				String authorAccountId = (String)parameters.get(1);
				String publicCode;
				try
				{
					publicCode = MartusCrypto.getFormattedPublicCode(authorAccountId);
				}
				catch (Exception e)
				{
					
					String accountInfo = authorAccountId;
					try
					{
						accountInfo = MartusCrypto.getFormattedPublicCode(authorAccountId);
					}
					catch (Exception justUseAccountIdInstead)
					{
					}
					logError("listBulletins: Bad account:" + accountInfo);
					result.add(INVALID_DATA);
					return result;
				}
				Vector infos = supplier.listBulletinsForMirroring(authorAccountId);

				if(infos.size()>0)
					logNotice("listBulletins: " + publicCode + " -> " + infos.size());
				else
					logInfo("listBulletins: None");
				
				result.add(OK);
				result.add(infos.toArray());
				return result;
			}
			case cmdListAvailableIdsForMirroring:
			{
				logInfo("listAvailableIds");
				String authorAccountId = (String)parameters.get(1);
				String publicCode;
				try
				{
					publicCode = MartusCrypto.getFormattedPublicCode(authorAccountId);
				}
				catch (Exception e)
				{
					
					String accountInfo = authorAccountId;
					try
					{
						accountInfo = MartusCrypto.getFormattedPublicCode(authorAccountId);
					}
					catch (Exception justUseAccountIdInstead)
					{
					}
					logError("listAvailableIds: Bad account:" + accountInfo);
					result.add(INVALID_DATA);
					return result;
				}
				Set infos = supplier.listAvailableIdsForMirroring(authorAccountId);

				if(infos.size()>0)
					logNotice("listAvailableIds: " + publicCode + " -> " + infos.size());
				else
					logInfo("listAvailableIds: None");
				
				result.add(OK);
				result.add(infos.toArray());
				return result;
			}
			case cmdGetBulletinUploadRecordForMirroring:
			{
				String authorAccountId = (String)parameters.get(1);
				String bulletinLocalId = (String)parameters.get(2);
				
				logInfo("getBulletinUploadRecord: " + bulletinLocalId);
				String bur = supplier.getBulletinUploadRecord(authorAccountId, bulletinLocalId);
				if(bur == null)
				{
					logDebug("getBulletinUploadRecord: NotFound");
					result.add(ITEM_NOT_FOUND);
				}
				else
				{
					logDebug("getBulletinUploadRecord: BUR OK");
					Vector burs = new Vector();
					burs.add(bur);
					
					result.add(OK);
					result.add(burs.toArray());
				}
				return result;
			}
			case cmdGetBulletinChunkForMirroring:
			{
				String authorAccountId = (String)parameters.get(1);
				String bulletinLocalId = (String)parameters.get(2);
				logInfo("getBulletinChunk: " + bulletinLocalId);
				int offset = ((Integer)parameters.get(3)).intValue();
				int maxChunkSize = ((Integer)parameters.get(4)).intValue();

				Vector data = getBulletinChunk(authorAccountId, bulletinLocalId, offset, maxChunkSize);
				String resultTag = (String)data.remove(0);
				logNotice("getBulletinChunk: Exit");
				
				result.add(resultTag);
				result.add(data.toArray());
				return result;
			}
			case cmdGetListOfFormTemplates:
			{
				logInfo("getListOfFormTemplates");
				String authorAccountId = (String)parameters.get(1);
				Vector templateInfoVector = supplier.listAvailableFormTemplateInfos(authorAccountId);
				result.add(OK);
				result.add(templateInfoVector.toArray());
				return result;
			}
			case cmdGetFormTemplate:
			{
				logInfo("getFormTemplate");
				String authorAccountId = (String)parameters.get(1);
				String templateFilename = (String)parameters.get(2);
				Vector template = supplier.getFormTemplate(authorAccountId, templateFilename);
				result.add(OK);
				result.add(template.toArray());
				return result;
			}
			default:
			{
				logNotice("request: Unknown command");
				result = new Vector();
				result.add(UNKNOWN_COMMAND);
			}
		}
		
		return result;
	}

	boolean isAuthorized(int cmd, String callerAccountId)
	{
		if(cmd == cmdPing)
			return true;
			
		return isAuthorizedForMirroring(callerAccountId);
	}

	Vector getBulletinChunk(String authorAccountId, String bulletinLocalId, int offset, int maxChunkSize)
	{
		return supplier.getBulletinChunkWithoutVerifyingCaller(authorAccountId, bulletinLocalId, 
								offset, maxChunkSize);
	}
	
	
	int extractCommand(Object possibleCommand)
	{
		String cmdString = (String)possibleCommand;
		if(cmdString.equals(CMD_MIRRORING_PING))
			return cmdPing;

		if(cmdString.equals(CMD_MIRRORING_LIST_ACCOUNTS))
			return cmdListAccountsForMirroring;
		
		if(cmdString.equals(CMD_MIRRORING_LIST_SEALED_BULLETINS))
			return cmdListBulletinsForMirroring;
		
		if(cmdString.equals(CMD_MIRRORING_LIST_AVAILABLE_IDS))
			return cmdListAvailableIdsForMirroring;

		if(cmdString.equals(CMD_MIRRORING_GET_BULLETIN_UPLOAD_RECORD))
			return cmdGetBulletinUploadRecordForMirroring;

		if(cmdString.equals(CMD_MIRRORING_GET_BULLETIN_CHUNK))
			return cmdGetBulletinChunkForMirroring;
		if(cmdString.equals(CMD_MIRRORING_GET_BULLETIN_CHUNK_TYPO))
			return cmdGetBulletinChunkForMirroring;
		
		if(cmdString.equals(CMD_MIRRORING_GET_LIST_OF_FORM_TEMPLATES))
			return cmdGetListOfFormTemplates;
		
		if(cmdString.equals(CMD_MIRRORING_GET_FORM_TEMPLATE))
			return cmdGetFormTemplate;

		return cmdUnknown;
	}
	
	boolean isAuthorizedForMirroring(String callerAccountId)
	{
		return supplier.isAuthorizedForMirroring(callerAccountId);
	}

	private String createLogString(String message)
	{
		return "Mirror handler: " + message;
	}

	public void logError(String message)
	{
		supplier.logError(createLogString(message));
	}
	
	public void logError(Exception e)
	{
		logError(LoggerUtil.getStackTrace(e));
	}
	
	public void logError(String message, Exception e)
	{
		logError(message);
		logError(e);
	}

	public void logInfo(String message)
	{
		supplier.logInfo(createLogString(message));
	}

	public void logNotice(String message)
	{
		supplier.logNotice(createLogString(message));
	}
	
	public void logWarning(String message)
	{
		supplier.logWarning(createLogString(message));
	}

	public void logDebug(String message)
	{
		supplier.logDebug(createLogString(message));
	}

	public static class UnknownCommandException extends Exception 
	{
	}

	final static int cmdUnknown = 0;
	final static int cmdPing = 1;
	final static int cmdListAccountsForMirroring = 2;
	final static int cmdListBulletinsForMirroring = 3;
	final static int cmdGetBulletinUploadRecordForMirroring = 4;
	final static int cmdGetBulletinChunkForMirroring = 5;
	final static int cmdListAvailableIdsForMirroring = 6;
	final static int cmdGetListOfFormTemplates = 7;
	final static int cmdGetFormTemplate = 8;
	
	ServerSupplierInterface supplier;
	MartusCrypto verifier;
}
