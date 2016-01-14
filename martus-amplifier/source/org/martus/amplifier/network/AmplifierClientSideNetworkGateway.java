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

import java.util.Vector;

import org.martus.common.AmplifierNetworkInterface;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.network.NetworkResponse;

public class AmplifierClientSideNetworkGateway implements AmplifierBulletinRetrieverGatewayInterface
{
	public AmplifierClientSideNetworkGateway(AmplifierNetworkInterface serverToUse)
	{
		server = serverToUse;
	}
	
	//to check if we need signature even for no parameters
	public NetworkResponse getAccountIds(MartusCrypto signer) throws Exception
	{
		Vector parameters = new Vector();
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getAccountIds(signer.getPublicKeyString(), parameters, signature));
	}
	
	public NetworkResponse getContactInfo(String accountId, MartusCrypto signer) throws Exception 
	{
		Vector parameters = new Vector();
		parameters.add(accountId);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		
		return new NetworkResponse(server.getContactInfo(signer.getPublicKeyString(), parameters, signature));
	}
	

	public NetworkResponse getPublicBulletinLocalIds(MartusCrypto signer, String accountId) throws Exception
	{
		Vector parameters = new Vector();
		parameters.add(accountId);
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getPublicBulletinLocalIds(signer.getPublicKeyString(), parameters, signature));
			
	}
					
	public NetworkResponse getBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId, 
					int chunkOffset, int maxChunkSize) throws Exception
	{
		Vector parameters = new Vector();
		parameters.add(authorAccountId);
		parameters.add(bulletinLocalId);
		parameters.add(new Integer(chunkOffset));
		parameters.add(new Integer(maxChunkSize));
		String signature = signer.createSignatureOfVectorOfStrings(parameters);
		return new NetworkResponse(server.getAmplifierBulletinChunk(signer.getPublicKeyString(), parameters, signature));
	}

	
	
	
	AmplifierNetworkInterface server;
}
