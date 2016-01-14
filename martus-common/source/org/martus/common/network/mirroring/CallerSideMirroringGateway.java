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

package org.martus.common.network.mirroring;

import java.util.Vector;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.network.NetworkResponse;
import org.martus.common.packet.UniversalId;

public class CallerSideMirroringGateway implements CallerSideMirroringGatewayInterface
{
	public CallerSideMirroringGateway(CallerSideMirroringInterface handlerToUse)
	{
		handler = handlerToUse;
	}
	
	public NetworkResponse ping() throws MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(MirroringInterface.CMD_MIRRORING_PING);
		return new NetworkResponse(handler.request("anonymous", parameters, "unsigned"));
	}

	public NetworkResponse listAccountsForMirroring(MartusCrypto signer) throws MartusSignatureException
	{
		return getNetworkResponse(signer, MirroringInterface.CMD_MIRRORING_LIST_ACCOUNTS, null, null);
	}
	
	public NetworkResponse listBulletinsForMirroring(MartusCrypto signer, String authorAccountId) throws MartusSignatureException
	{
		return getNetworkResponse(signer, MirroringInterface.CMD_MIRRORING_LIST_SEALED_BULLETINS, authorAccountId, null);
	}
	
	public NetworkResponse listAvailableIdsForMirroring(MartusCrypto signer, String authorAccountId) throws MartusSignatureException
	{
		return getNetworkResponse(signer, MirroringInterface.CMD_MIRRORING_LIST_AVAILABLE_IDS, authorAccountId, null);
	}

	public NetworkResponse getBulletinUploadRecord(MartusCrypto signer, UniversalId uid) throws MartusSignatureException
	{
		return getNetworkResponse(signer, MirroringInterface.CMD_MIRRORING_GET_BULLETIN_UPLOAD_RECORD, uid.getAccountId(), new Object[] {uid.getLocalId()});
	}

	public NetworkResponse getBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId, 
					int chunkOffset, int maxChunkSize) throws 
			MartusCrypto.MartusSignatureException
	{
		Object[] extraParameters = new Object[] {bulletinLocalId, new Integer(chunkOffset), new Integer(maxChunkSize)};
		return getNetworkResponse(signer, MirroringInterface.CMD_MIRRORING_GET_BULLETIN_CHUNK_TYPO, authorAccountId, extraParameters);
	}

	@Override
	public NetworkResponse getListOfFormTemplateInfos(MartusCrypto signer, String templateOwnerAccountId) throws MartusSignatureException
	{
		Object[] extraParameters = new Object[0];
		return getNetworkResponse(signer, MirroringInterface.CMD_MIRRORING_GET_LIST_OF_FORM_TEMPLATES, templateOwnerAccountId, extraParameters);
	}

	@Override
	public NetworkResponse getFormTemplate(MartusCrypto signer, String templateOwnerAccountId, String templateName)  throws MartusSignatureException
	{
		Object[] extraParameters = new Object[] { templateName };
		return getNetworkResponse(signer, MirroringInterface.CMD_MIRRORING_GET_FORM_TEMPLATE, templateOwnerAccountId, extraParameters);
	}
	
	private NetworkResponse getNetworkResponse(MartusCrypto signer, String command, String authorAccountId, Object[] extraParameters) throws MartusSignatureException
	{
		Vector parameters = new Vector();
		parameters.add(command);
		if(authorAccountId != null)
			parameters.add(authorAccountId);
		if(extraParameters != null)
		{
			for(int i = 0; i < extraParameters.length; i++)
			{
				parameters.add(extraParameters[i]);
			}
		}
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		Vector result = handler.request(signer.getPublicKeyString(), parameters, signature);
		return new NetworkResponse(result);
	}
					
	private CallerSideMirroringInterface handler;

}

