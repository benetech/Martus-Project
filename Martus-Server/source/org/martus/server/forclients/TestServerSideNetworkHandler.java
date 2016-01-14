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

package org.martus.server.forclients;

import java.util.Vector;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.util.TestCaseEnhanced;


public class TestServerSideNetworkHandler extends TestCaseEnhanced 
{
	public TestServerSideNetworkHandler(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		mySecurity = MockMartusSecurity.createServer();
		
		mockServer = new MockMartusServer();
		mockServer.serverForClients.loadBannedClients();
		mockServer.verifyAndLoadConfigurationFiles();
		mockServer.setSecurity(mySecurity);
		handler = new ServerSideNetworkHandler(mockServer.serverForClients);

		otherSecurity = MockMartusSecurity.createClient();
	}
	
	public void tearDown() throws Exception
	{
		mockServer.deleteAllFiles();
		super.tearDown();
	}

	public void testSigs() throws Exception
	{
		String myAccountId = mySecurity.getPublicKeyString();
		Vector parameters = new Vector();
		parameters.add("abc");
		parameters.add(new Integer(2));
		String badSig = "123";
		String wrongSig = otherSecurity.createSignatureOfVectorOfStrings(parameters);
		
		{
			Vector badSigResult = handler.getUploadRights(myAccountId, parameters, badSig);
			assertEquals("getUploadRights badSig length", 1, badSigResult.size());
			assertEquals("getUploadRights badSig error", NetworkInterfaceConstants.SIG_ERROR, badSigResult.get(0));
			
			Vector wrongSigResult = handler.getUploadRights(myAccountId, parameters, wrongSig);
			assertEquals("getUploadRights wrongSig length", 1, wrongSigResult.size());
			assertEquals("getUploadRights wrongSig error", NetworkInterfaceConstants.SIG_ERROR, wrongSigResult.get(0));
			
		}

		{
			Vector badSigResult = handler.getSealedBulletinIds(myAccountId, parameters, badSig);
			assertEquals("getSealedBulletinIds badSig length", 1, badSigResult.size());
			assertEquals("getSealedBulletinIds badSig error", NetworkInterfaceConstants.SIG_ERROR, badSigResult.get(0));

			Vector wrongSigResult = handler.getSealedBulletinIds(myAccountId, parameters, wrongSig);
			assertEquals("getSealedBulletinIds wrongSig length", 1, wrongSigResult.size());
			assertEquals("getSealedBulletinIds wrongSig error", NetworkInterfaceConstants.SIG_ERROR, wrongSigResult.get(0));
		}

		{
			Vector badSigResult = handler.getFieldOfficeAccountIds(myAccountId, parameters, badSig);
			assertEquals("getFieldOfficeAccountIds badSig length", 1, badSigResult.size());
			assertEquals("getFieldOfficeAccountIds badSig error", NetworkInterfaceConstants.SIG_ERROR, badSigResult.get(0));

			Vector wrongSigResult = handler.getFieldOfficeAccountIds(myAccountId, parameters, wrongSig);
			assertEquals("getFieldOfficeAccountIds wrongSig length", 1, wrongSigResult.size());
			assertEquals("getFieldOfficeAccountIds wrongSig error", NetworkInterfaceConstants.SIG_ERROR, wrongSigResult.get(0));
		}

		{
			Vector badSigResult = handler.putBulletinChunk(myAccountId, parameters, badSig);
			assertEquals("putBulletinChunk badSig length", 1, badSigResult.size());
			assertEquals("putBulletinChunk badSig error", NetworkInterfaceConstants.SIG_ERROR, badSigResult.get(0));

			Vector wrongSigResult = handler.putBulletinChunk(myAccountId, parameters, wrongSig);
			assertEquals("putBulletinChunk wrongSig length", 1, wrongSigResult.size());
			assertEquals("putBulletinChunk wrongSig error", NetworkInterfaceConstants.SIG_ERROR, wrongSigResult.get(0));
		}

		{
			Vector badSigResult = handler.getBulletinChunk(myAccountId, parameters, badSig);
			assertEquals("getBulletinChunk badSig length", 1, badSigResult.size());
			assertEquals("getBulletinChunk badSig error", NetworkInterfaceConstants.SIG_ERROR, badSigResult.get(0));

			Vector wrongSigResult = handler.getBulletinChunk(myAccountId, parameters, wrongSig);
			assertEquals("getBulletinChunk wrongSig length", 1, wrongSigResult.size());
			assertEquals("getBulletinChunk wrongSig error", NetworkInterfaceConstants.SIG_ERROR, wrongSigResult.get(0));
		}

		{
			Vector badSigResult = handler.getPacket(myAccountId, parameters, badSig);
			assertEquals("getPacket badSig length", 1, badSigResult.size());
			assertEquals("getPacket badSig error", NetworkInterfaceConstants.SIG_ERROR, badSigResult.get(0));

			Vector wrongSigResult = handler.getPacket(myAccountId, parameters, wrongSig);
			assertEquals("getPacket wrongSig length", 1, wrongSigResult.size());
			assertEquals("getPacket wrongSig error", NetworkInterfaceConstants.SIG_ERROR, wrongSigResult.get(0));
		}

	}

	public void testOctober2002ClientRetrieve() throws Exception
	{
		String myAccountId = mySecurity.getPublicKeyString();

		Vector parameters = new Vector();
		parameters.add(myAccountId);
		String sig = mySecurity.createSignatureOfVectorOfStrings(parameters);
		handler.getSealedBulletinIds(myAccountId, parameters, sig);
		
		handler.getDraftBulletinIds(myAccountId, parameters, sig);
	}
	
	ServerSideNetworkHandler handler;
	MockMartusServer mockServer;
	MartusCrypto mySecurity;
	MartusCrypto otherSecurity;
}
