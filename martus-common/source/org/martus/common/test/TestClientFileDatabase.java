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

package org.martus.common.test;

import java.io.File;
import java.io.InputStream;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.ClientFileDatabase;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.packet.UniversalId;
import org.martus.util.TestCaseEnhanced;

public class TestClientFileDatabase extends TestCaseEnhanced
{
	public TestClientFileDatabase(String name)
	{
		super(name);
	}


	public void testFindLegacyRecords() throws Exception
	{
		Database mockDatabase = new MockClientDatabase();
		MartusCrypto security = MockMartusSecurity.createClient();

		File tempDir = createTempFile();
		tempDir.delete();
		tempDir.mkdir();
		Database clientFileDatabase = new ClientFileDatabase(tempDir, security);
		clientFileDatabase.initialize();

		internalTestFindLegacyRecords(mockDatabase);
		internalTestFindLegacyRecords(clientFileDatabase);

	}


	private void internalTestFindLegacyRecords(Database db) throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();

		UniversalId uid = UniversalIdForTesting.createDummyUniversalId();
		DatabaseKey legacyKey = DatabaseKey.createLegacyKey(uid);
		db.writeRecord(legacyKey, smallString);
		InputStream inLegacy = db.openInputStream(legacyKey, security);
		assertNotNull("legacy not found?", inLegacy);
		inLegacy.close();

		InputStream inDraft = db.openInputStream(legacyKey, security);
		assertNotNull("draft not found?", inDraft);
		inDraft.close();

		DatabaseKey immutableKey = DatabaseKey.createImmutableKey(uid);
		InputStream inSealed = db.openInputStream(immutableKey, security);
		assertNotNull("sealed not found?", inSealed);
		inSealed.close();

		db.deleteAllData();
	}

	private static final String smallString = "some text";
}
