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
package org.martus.amplifier.datasynch.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.martus.amplifier.datasynch.AmplifierNetworkGateway;
import org.martus.amplifier.datasynch.BackupServerInfo;
import org.martus.amplifier.network.AmplifierBulletinRetrieverGatewayInterface;
import org.martus.common.LoggerToNull;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;

public class TestAmplifierNetworkGateway extends TestAbstractAmplifierDataSynch
{	
	public TestAmplifierNetworkGateway(String name)
	{
		super(name);
		sampleContactInfo = new HashMap();
	}
	
	public void testGetAllAccountIds() throws Exception
	{
		//System.out.println("AmplifierNetworkGatewayTest:testGetAllAccountIds");
		AmplifierBulletinRetrieverGatewayInterface abrgi = new MockGatewayInterface();
		BackupServerInfo serverToCall = new BackupServerInfo("test", "10.1.1.1", 1, "key");
		AmplifierNetworkGateway amplifierGateway = new AmplifierNetworkGateway(abrgi, serverToCall, dummyLogger, MockMartusSecurity.createOtherServer());
		Vector list = amplifierGateway.getAllAccountIds();
		for(int i =0; i<list.size(); i++)
		{
			//System.out.println("AccountId1 = "+(list.elementAt(i)).toString() );
		}
		assertTrue(list.size() >0);	
	}
	
	public void testGetContactInfo() throws Exception
	{
		AmplifierBulletinRetrieverGatewayInterface gateway = new MockGatewayInterface();
		BackupServerInfo serverToCall = new BackupServerInfo("test", "10.1.1.1", 1, "key");
		AmplifierNetworkGateway amplifierGateway = new AmplifierNetworkGateway(gateway, serverToCall, dummyLogger, MockMartusSecurity.createOtherServer());
		Vector contactInfo = amplifierGateway.getContactInfo(sampleAccountId);
		assertNull("Should not find account since data isn't there yet", contactInfo);

		Vector info = new Vector();
		info.add(sampleAccountId);
		info.add(new Integer(1));
		info.add("data");
		info.add("signature");
		
		sampleContactInfo.put(sampleAccountId, info);
		Vector invalidSignatureForContactInfo = amplifierGateway.getContactInfo(sampleAccountId);
		assertNull("Should not return this invalid contactInfo", invalidSignatureForContactInfo);

		info.clear();
		MockMartusSecurity client = MockMartusSecurity.createClient();
		String clientId = client.getPublicKeyString();
		info.add(clientId);
		info.add(new Integer(1));
		info.add("data");
		info.add(client.createSignatureOfVectorOfStrings(info));
		sampleContactInfo.put(clientId, info);
		contactInfo = amplifierGateway.getContactInfo(clientId);
		
		assertNotNull("Didn't return the contact Info?", contactInfo);
		assertEquals("account id don't match?", info.get(0), contactInfo.get(0));		
		assertEquals("number of data entries don't match?", info.get(1), contactInfo.get(1));		
		assertEquals("data doesn't match?", info.get(2), contactInfo.get(2));		
		assertEquals("signatures don't match?", info.get(3), contactInfo.get(3));		

	}
	
	public void testGetAccountBulletinLocalIds() throws Exception
	{
		AmplifierBulletinRetrieverGatewayInterface abrgi = new MockGatewayInterface();
		BackupServerInfo serverToCall = new BackupServerInfo("test", "10.1.1.1", 1, "key");
		AmplifierNetworkGateway amplifierGateway = new AmplifierNetworkGateway(abrgi, serverToCall,  dummyLogger, MockMartusSecurity.createOtherServer());
		
		Vector list = amplifierGateway.getAccountPublicBulletinLocalIds(sampleAccountId);
		assertTrue(list.size() > 0);
		assertEquals(list.get(0), sampleLocalId);	
	}
	
	public void testgetBulletin()
	{
		//System.out.println("AmplifierNetworkGatewayTest: testGetBulletin");
		//AmplifierNetworkGateway amplifierGateway = AmplifierNetworkGateway.getInstance();
		//UniversalId uid = UniversalId.createFromAccountAndLocalId(sampleAccountId, sampleLocalId);
		//File file = amplifierGateway.getBulletin(uid);
		//assertTrue(file.length() >0);		
	}



	public void testRetrieveAndManageBulletin()
	{
		//System.out.println("AmplifierNetworkGatewayTest:testRetrieveAndManageBulletin");
		//AmplifierNetworkGateway amplifierGateway = AmplifierNetworkGateway.getInstance();		
		//UniversalId uid = UniversalId.createFromAccountAndLocalId(sampleAccountId, sampleLocalId);
		//Vector list = amplifierGateway.retrieveAndManageBulletin(uid);				
		//assertTrue(list.size() > 0);			
	}
	
	class MockGatewayInterface implements AmplifierBulletinRetrieverGatewayInterface
	{

		public NetworkResponse getAccountIds(MartusCrypto signer) throws MartusSignatureException, IOException
		{
			Vector ids = new Vector();
			ids.add(sampleAccountId);

			Vector rawData = new Vector();
			rawData.add(NetworkInterfaceConstants.OK);
			rawData.add(ids.toArray());

			return new NetworkResponse(rawData);
		}

		public NetworkResponse getContactInfo(String accountId, MartusCrypto signer) throws MartusCrypto.MartusSignatureException, IOException 
		{
			Vector contactInfo = (Vector)sampleContactInfo.get(accountId);
			
			Vector rawData = new Vector();
			rawData.add(NetworkInterfaceConstants.OK);
			rawData.add(contactInfo.toArray());
		
			return new NetworkResponse(rawData);
		}
		

		public NetworkResponse getPublicBulletinLocalIds(MartusCrypto signer, String accountId) throws MartusSignatureException, IOException
		{
			Vector ids = new Vector();
			ids.add(sampleLocalId);

			Vector rawData = new Vector();
			rawData.add(NetworkInterfaceConstants.OK);
			rawData.add(ids.toArray());

			return new NetworkResponse(rawData);
		}

		public NetworkResponse getBulletinChunk(MartusCrypto signer, String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize) throws MartusSignatureException, IOException
		{
			// TODO Auto-generated method stub
			return null;
		}
	}
		
	final String sampleAccountId = "MIIBIDANBgkqhkiG9w0BAQEFAAOCAQ0AMIIBCAKCAQEAt+9X7kLTLx8fTfXIogRK5ySJnVL1s2Wi/L9MYMxHWkddpD5XBibQjOM/RkW2tn7oXM9SdQrU16EvEJtTnIZ+z5D6uXuq37vffHcfV9x5vQ3p5PEtKLinvvbqwbVgka+OXbMsjoV6seeAtXAxop9qme9yk4d1/Pco+RdLOX/Toyt9prSqlr2epu+hpZ6Qv8X9C4IF80eajPJd0x5cKsTZPpAmC5Iy5oh2uE0dy9iP6Esz3Ob1X3dn/QLaHJhQQp49um6UCbuN57wof/m4k703txDzxpZdKYUDaCQvKslpBpfiqjLTZ2FbaUodkkcckky9U9xzMDdrNxSvuG9LpjFr0QIBEQ==";
	final String sampleLocalId = "B-111ded2-f19d90f997--7ffd";
	HashMap sampleContactInfo;
	LoggerToNull dummyLogger = new LoggerToNull();
}
