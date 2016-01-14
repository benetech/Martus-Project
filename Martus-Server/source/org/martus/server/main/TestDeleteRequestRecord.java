/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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
package org.martus.server.main;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Vector;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.DeleteRequestRecord;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.server.forclients.MockMartusServer;
import org.martus.util.TestCaseEnhanced;

public class TestDeleteRequestRecord extends TestCaseEnhanced
{

	public TestDeleteRequestRecord(String name)
	{
		super(name);
	}

	public void testCreateDELRecord() throws Exception
	{
		String account = "account";
		Vector delRequestData = new Vector();
		delRequestData.add(new Integer(1));
		delRequestData.add("data");
		
		String signature = "signature";
		DeleteRequestRecord delRequest = new DeleteRequestRecord(account, delRequestData, signature);
		String delData = delRequest.getDelData();
		BufferedReader reader = new BufferedReader(new StringReader(delData));
		String gotFileTypeIdentifier = reader.readLine();
		assertEquals("Martus Draft Delete Request 1.0", gotFileTypeIdentifier);
		String gotTimeStamp = reader.readLine();
		String now = MartusServerUtilities.createTimeStamp();
		assertStartsWith(now.substring(0, 13), gotTimeStamp);
		assertEquals(account, reader.readLine());
		assertEquals(new Integer(delRequestData.size()).toString(), reader.readLine());
		assertEquals(delRequestData.get(0), new Integer(reader.readLine()));
		assertEquals(delRequestData.get(1), reader.readLine());
		assertEquals(signature, reader.readLine());
		reader.close();
	}
	
	public void testGetDELKey() throws Exception
	{
		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey draftDELKey = DeleteRequestRecord.getDelKey(uid);
		assertEquals("DEL-" + uid.getLocalId(), draftDELKey.getLocalId());
	}
	
	public void testVerification() throws Exception
	{
		MockMartusSecurity clientSecurity = new MockMartusSecurity();
		clientSecurity.createKeyPair();
		MockMartusSecurity serverSecurity = new MockMartusSecurity();
		serverSecurity.createKeyPair();
		Bulletin b1 = new Bulletin(clientSecurity);
		Bulletin b2 = new Bulletin(clientSecurity);
		Bulletin b3 = new Bulletin(clientSecurity);
		Vector clientDeleteRequestParams = new Vector();
		clientDeleteRequestParams.add(new Integer(3));
		clientDeleteRequestParams.add(b1.getLocalId());
		clientDeleteRequestParams.add(b2.getLocalId());
		clientDeleteRequestParams.add(b3.getLocalId());
		String signature = clientSecurity.createSignatureOfVectorOfStrings(clientDeleteRequestParams);
		DeleteRequestRecord delRequestValid = new DeleteRequestRecord(clientSecurity.getPublicKeyString(), clientDeleteRequestParams, signature);
		assertTrue(delRequestValid.doesSignatureMatch(serverSecurity));
		signature += "a";
		DeleteRequestRecord delRequestNotValid = new DeleteRequestRecord(clientSecurity.getPublicKeyString(), clientDeleteRequestParams, signature);
		assertFalse(delRequestNotValid.doesSignatureMatch(serverSecurity));
	}
	
	public void testSaveToDatabase() throws Exception
	{
		MockMartusSecurity clientSecurity = new MockMartusSecurity();
		clientSecurity.createKeyPair();
		MockMartusSecurity serverSecurity = new MockMartusSecurity();
		serverSecurity.createKeyPair();
		Bulletin b1 = new Bulletin(clientSecurity);
		Bulletin b2 = new Bulletin(clientSecurity);
		Vector clientDeleteRequestParams = new Vector();
		clientDeleteRequestParams.add(new Integer(2));
		clientDeleteRequestParams.add(b1.getLocalId());
		clientDeleteRequestParams.add(b2.getLocalId());
		String signature = clientSecurity.createSignatureOfVectorOfStrings(clientDeleteRequestParams);
		DeleteRequestRecord delRequest = new DeleteRequestRecord(clientSecurity.getPublicKeyString(), clientDeleteRequestParams, signature);
		assertTrue(delRequest.doesSignatureMatch(serverSecurity));

		MockServerDatabase db = new MockServerDatabase();
		MockMartusServer server = new MockMartusServer(db);
		ServerBulletinStore store = server.getStore();
		store.writeDel(b1.getUniversalId(), delRequest);
		
		DeleteRequestRecord delRequestFromDatabase = new DeleteRequestRecord(db, b1.getUniversalId(), serverSecurity);		
		assertTrue("Signature verification failed?", delRequestFromDatabase.doesSignatureMatch(serverSecurity));
		assertEquals("Doesn't match?", delRequest.getDelData(), delRequestFromDatabase.getDelData());
		server.deleteAllFiles();
	}
}
