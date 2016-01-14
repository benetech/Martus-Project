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

package org.martus.common.bulletin;

import java.io.File;

import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusXml;
import org.martus.common.bulletin.Bulletin.BulletinState;
import org.martus.common.bulletin.Bulletin.DamagedBulletinException;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockDatabase;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.test.MockBulletinStore;
import org.martus.util.TestCaseEnhanced;

public class TestBulletinLoader extends TestCaseEnhanced
{

	public TestBulletinLoader(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(security == null)
		{
			security = MockMartusSecurity.createClient();
		}
		store = new MockBulletinStore(this);
	}

	public void testDetectFieldPacketWithWrongSig() throws Exception
	{
		Bulletin original = new Bulletin(security);
		original.set(Bulletin.TAGPUBLICINFO, "public info");
		original.set(Bulletin.TAGPRIVATEINFO, "private info");
		original.setImmutable();
		store.saveEncryptedBulletinForTesting(original);

		Bulletin loaded = BulletinLoader.loadFromDatabase(getDatabase(), DatabaseKey.createLegacyKey(original.getUniversalId()), security);
		assertEquals("not valid?", true, loaded.isNonAttachmentDataValid());

		FieldDataPacket fdp = loaded.getFieldDataPacket();
		fdp.set(Bulletin.TAGPUBLICINFO, "different public!");
		boolean encryptPublicData = true;
		fdp.writeXmlToClientDatabase(getDatabase(), encryptPublicData, security);

		loaded = BulletinLoader.loadFromDatabase(getDatabase(), DatabaseKey.createLegacyKey(original.getUniversalId()), security);
		assertEquals("not invalid?", false, loaded.isNonAttachmentDataValid());
		assertEquals("private messed up?", original.get(Bulletin.TAGPRIVATEINFO), loaded.get(Bulletin.TAGPRIVATEINFO));
	}
	
	public void testDetectPrivateFieldPacketWithWrongSig() throws Exception
	{
		Bulletin original = new Bulletin(security);
		original.set(Bulletin.TAGPUBLICINFO, "public info");
		original.set(Bulletin.TAGPRIVATEINFO, "private info");
		original.setImmutable();
		store.saveEncryptedBulletinForTesting(original);

		Bulletin loaded = BulletinLoader.loadFromDatabase(getDatabase(), DatabaseKey.createLegacyKey(original.getUniversalId()), security);
		assertEquals("not valid?", true, loaded.isNonAttachmentDataValid());

		FieldDataPacket fdp = loaded.getPrivateFieldDataPacket();
		fdp.set(Bulletin.TAGPRIVATEINFO, "different private!");
		boolean encryptPublicData = true;
		fdp.writeXmlToClientDatabase(getDatabase(), encryptPublicData, security);

		loaded = BulletinLoader.loadFromDatabase(getDatabase(), DatabaseKey.createLegacyKey(original.getUniversalId()), security);
		assertEquals("not invalid?", false, loaded.isNonAttachmentDataValid());
		assertEquals("public messed up?", original.get(Bulletin.TAGPUBLICINFO), loaded.get(Bulletin.TAGPUBLICINFO));
	}

	public void testLoadFromDatabase() throws Exception
	{
		assertEquals(0, getDatabase().getAllKeys().size());

		Bulletin b = new Bulletin(security);
		b.set(Bulletin.TAGPUBLICINFO, "public info");
		b.set(Bulletin.TAGPRIVATEINFO, "private info");
		b.setImmutable();
		store.saveEncryptedBulletinForTesting(b);
		assertEquals("saved 1", 3, getDatabase().getAllKeys().size());

		DatabaseKey key = DatabaseKey.createLegacyKey(b.getUniversalId());
		Bulletin loaded = new Bulletin(security);
		loaded = BulletinLoader.loadFromDatabase(getDatabase(), key, security);
		assertEquals("id", b.getLocalId(), loaded.getLocalId());
		assertEquals("public info", b.get(Bulletin.TAGPUBLICINFO), loaded.get(Bulletin.TAGPUBLICINFO));
		assertEquals("private info", b.get(Bulletin.TAGPRIVATEINFO), loaded.get(Bulletin.TAGPRIVATEINFO));
		assertEquals("status", b.getStatus(), loaded.getStatus());
	}

	public void testLoadAndSaveWithHQPublicKey() throws Exception
	{
		Bulletin original = new Bulletin(security);
		original.set(Bulletin.TAGPUBLICINFO, "public info");
		String key = security.getPublicKeyString();
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(key);
		keys.add(key1);
		original.setAuthorizedToReadKeys(keys);
		original.changeState(BulletinState.STATE_SAVE);
		store.saveEncryptedBulletinForTesting(original);

		DatabaseKey dbKey = DatabaseKey.createLegacyKey(original.getUniversalId());
		Bulletin loaded = BulletinLoader.loadFromDatabase(getDatabase(), dbKey, security);
		assertEquals("Saved Bulletin has Authorized Keys?", 0, loaded.getAuthorizedToReadKeys().size());
		assertEquals("Saved Bulletin has no Pending Keys?", original.getAuthorizedToReadKeysIncludingPending().size(), loaded.getAuthorizedToReadKeysIncludingPending().size());
		
		original.changeState(BulletinState.STATE_SHARED);
		store.saveEncryptedBulletinForTesting(original);

		Bulletin loadedShared = BulletinLoader.loadFromDatabase(getDatabase(), dbKey, security);
		assertEquals("Shared Bulletin has no Authorized Keys?", original.getAuthorizedToReadKeys().size(), loadedShared.getAuthorizedToReadKeys().size());
		assertEquals("Shared Bulletin should have 1 AuthorizedIncludingPending Keys?", 1, loadedShared.getAuthorizedToReadKeysIncludingPending().size());
		assertEquals("Keys not the same?", (original.getFieldDataPacket().getAuthorizedToReadKeys().get(0)).getPublicKey(), (loadedShared.getFieldDataPacket().getAuthorizedToReadKeys().get(0)).getPublicKey());

		File tempFile = createTempFile();
		BulletinForTesting.saveToFile(getDatabase(), original, tempFile, security);
		Bulletin loaded2 = new Bulletin(security);
		BulletinZipImporter.loadFromFile(loaded2, tempFile, security);
		assertEquals("Loaded Keys not the same?", (original.getFieldDataPacket().getAuthorizedToReadKeys().get(0)).getPublicKey(), (loaded2.getFieldDataPacket().getAuthorizedToReadKeys().get(0)).getPublicKey());
	}

	
	
	public void testLoadFromDatabaseEncrypted() throws Exception
	{
		assertEquals(0, getDatabase().getAllKeys().size());

		Bulletin b = new Bulletin(security);
		b.setAllPrivate(true);
		store.saveEncryptedBulletinForTesting(b);
		assertEquals("saved 1", 3, getDatabase().getAllKeys().size());

		DatabaseKey key = DatabaseKey.createLegacyKey(b.getUniversalId());
		Bulletin loaded = new Bulletin(security);
		loaded = BulletinLoader.loadFromDatabase(getDatabase(), key, security);
		assertEquals("id", b.getLocalId(), loaded.getLocalId());

		assertEquals("not private?", b.isAllPrivate(), loaded.isAllPrivate());
	}

	public void testLoadFromDatabaseDamaged() throws Exception
	{
		Bulletin b = new Bulletin(security);
		b.set(Bulletin.TAGPUBLICINFO, samplePublic);
		b.set(Bulletin.TAGPRIVATEINFO, samplePrivate);
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(b.getAccount());
		keys.add(key1);
		b.setAuthorizedToReadKeys(keys);
		saveAndVerifyValid("freshly created", b);

		DatabaseKey headerKey = DatabaseKey.createLegacyKey(b.getBulletinHeaderPacket().getUniversalId());
		verifyVariousTypesOfDamage("bad header", b, false, headerKey, "", "");

		DatabaseKey dataKey = DatabaseKey.createLegacyKey(b.getFieldDataPacket().getUniversalId());
		verifyVariousTypesOfDamage("bad field data", b, true, dataKey, "", samplePrivate);

		DatabaseKey privateDataKey = DatabaseKey.createLegacyKey(b.getPrivateFieldDataPacket().getUniversalId());
		verifyVariousTypesOfDamage("bad private field data", b, true, privateDataKey, samplePublic, "");
	}

	void verifyVariousTypesOfDamage(String label, Bulletin b, boolean headerIsValid, DatabaseKey packetKey,
				String expectedPublic, String expectedPrivate) throws Exception
	{
		verifyCorruptByRemovingOneCharAfterHeaderComment(label + " remove one char after header comment",
					b, headerIsValid, packetKey, expectedPublic, expectedPrivate);
		verifyCorruptByDamagingHeaderComment(label + "damage header comment",
					b, headerIsValid, packetKey, expectedPublic, expectedPrivate);
		verifyCorruptByDamagingSigComment(label + "damage sig comment",
					b, headerIsValid, packetKey, expectedPublic, expectedPrivate);
		verifyCorruptByRemovingOneSigChar(label + "remove one sig char",
					b, headerIsValid, packetKey, expectedPublic, expectedPrivate);
		verifyCorruptByModifyingOneSigChar(label + "modify one sig char",
					b, headerIsValid, packetKey, expectedPublic, expectedPrivate);
	}

	void verifyCorruptByRemovingOneCharAfterHeaderComment(String label, Bulletin b, boolean headerIsValid, DatabaseKey packetKey,
				String expectedPublic, String expectedPrivate) throws Exception
	{
		saveAndVerifyValid(label, b);
		String packetContents = getDatabase().readRecord(packetKey, security);
		final int positionAfterHeaderSig = packetContents.indexOf("-->") + 20;
		int removeCharAt = positionAfterHeaderSig;
		getDatabase().writeRecord(packetKey, packetContents.substring(0,removeCharAt-1) + packetContents.substring(removeCharAt+1));
		verifyBulletinIsInvalid(label, b, headerIsValid, expectedPublic, expectedPrivate);
	}

	void verifyCorruptByModifyingOneSigChar(String label, Bulletin b, boolean headerIsValid, DatabaseKey packetKey,
				String expectedPublic, String expectedPrivate) throws Exception
	{
		saveAndVerifyValid(label, b);
		String packetContents = getDatabase().readRecord(packetKey, security);
		final int positionInsideSig = packetContents.indexOf("<!--sig=") + 20;
		int modifyCharAt = positionInsideSig;
		char charToModify = packetContents.charAt(modifyCharAt);
		if(charToModify == '2')
			charToModify = '3';
		else
			charToModify = '2';
		String newPacketContents = packetContents.substring(0,modifyCharAt) + charToModify + packetContents.substring(modifyCharAt+1);
		getDatabase().writeRecord(packetKey, newPacketContents);
		verifyBulletinIsInvalid(label, b, headerIsValid, expectedPublic, expectedPrivate);
	}

	void verifyCorruptByRemovingOneSigChar(String label, Bulletin b, boolean headerIsValid, DatabaseKey packetKey,
				String expectedPublic, String expectedPrivate) throws Exception
	{
		saveAndVerifyValid(label, b);
		String packetContents = getDatabase().readRecord(packetKey, security);
		final int positionInsideSig = packetContents.indexOf("<!--sig=") + 20;
		int removeCharAt = positionInsideSig;
		getDatabase().writeRecord(packetKey, packetContents.substring(0,removeCharAt-1) + packetContents.substring(removeCharAt+1));
		verifyBulletinIsInvalid(label, b, headerIsValid, expectedPublic, expectedPrivate);
	}

	void verifyCorruptByDamagingHeaderComment(String label, Bulletin b, boolean headerIsValid, DatabaseKey packetKey,
				String expectedPublic, String expectedPrivate) throws Exception
	{
		saveAndVerifyValid(label, b);
		String packetContents = getDatabase().readRecord(packetKey, security);
		final int positionAfterHeaderSig = packetContents.indexOf(MartusXml.packetStartCommentEnd);
		int removeCharAt = positionAfterHeaderSig;
		getDatabase().writeRecord(packetKey, packetContents.substring(0,removeCharAt-1) + packetContents.substring(removeCharAt+1));
		verifyBulletinIsInvalid(label, b, headerIsValid, expectedPublic, expectedPrivate);
	}

	void verifyCorruptByDamagingSigComment(String label, Bulletin b, boolean headerIsValid, DatabaseKey packetKey,
				String expectedPublic, String expectedPrivate) throws Exception
	{
		saveAndVerifyValid(label, b);
		String packetContents = getDatabase().readRecord(packetKey, security);
		final int positionAfterHeaderSig = packetContents.indexOf("<!--sig=");
		int removeCharAt = positionAfterHeaderSig;
		getDatabase().writeRecord(packetKey, packetContents.substring(0,removeCharAt-1) + packetContents.substring(removeCharAt+1));
		verifyBulletinIsInvalid(label, b, headerIsValid, expectedPublic, expectedPrivate);
	}

	void saveAndVerifyValid(String label, Bulletin b) throws Exception
	{
		store.saveEncryptedBulletinForTesting(b);
		DatabaseKey headerKey = b.getDatabaseKey();
		Bulletin stillValid = BulletinLoader.loadFromDatabase(getDatabase(), headerKey, security);
		assertEquals(label + " not valid after save?", true, stillValid.isNonAttachmentDataValid());
	}

	void verifyBulletinIsInvalid(String label, Bulletin b, boolean headerIsValid,
				String expectedPublic, String expectedPrivate) throws Exception
	{
		DatabaseKey headerKey = DatabaseKey.createLegacyKey(b.getBulletinHeaderPacket().getUniversalId());

		if(!headerIsValid)
		{
			try
			{
				BulletinLoader.loadFromDatabase(getDatabase(), headerKey, security);
			}
			catch (DamagedBulletinException ignoreExpectedException)
			{
			}
			return;
		}

		Bulletin invalid = BulletinLoader.loadFromDatabase(getDatabase(), headerKey, security);
		assertEquals(label + " not invalid?", false, invalid.isNonAttachmentDataValid());
		assertEquals(label + " wrong uid?", b.getUniversalId(), invalid.getUniversalId());
		assertEquals(label + " wrong fdp account?", b.getAccount(), invalid.getFieldDataPacket().getAccountId());
		assertEquals(label + " wrong private fdp account?", b.getAccount(), invalid.getPrivateFieldDataPacket().getAccountId());
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
		assertEquals(label + " wrong fdp localId?", bhp.getFieldDataPacketId(), invalid.getFieldDataPacket().getLocalId());
		assertEquals(label + " wrong private fdp localId?", bhp.getPrivateFieldDataPacketId(), invalid.getPrivateFieldDataPacket().getLocalId());
		assertEquals(label + " public info", expectedPublic, invalid.get(Bulletin.TAGPUBLICINFO));
		assertEquals(label + " private info", expectedPrivate, invalid.get(Bulletin.TAGPRIVATEINFO));
		assertEquals(label + " hq keys", 0, invalid.getAuthorizedToReadKeys().size());
	}

	static MockDatabase getDatabase()
	{
		return (MockDatabase)store.getDatabase();
	}

	static final String samplePublic = "some public text for loading";
	static final String samplePrivate = "a bit of private text for loading";
	static final byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
	static final byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};

	private static BulletinStore store;
	static MartusCrypto security;
}
