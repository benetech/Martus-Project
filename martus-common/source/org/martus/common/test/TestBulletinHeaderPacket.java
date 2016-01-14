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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusXml;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.ExtendedHistoryEntry;
import org.martus.common.packet.ExtendedHistoryList;
import org.martus.common.packet.UniversalId;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


public class TestBulletinHeaderPacket extends TestCaseEnhanced
{
	public TestBulletinHeaderPacket(String name)
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
		bhp = new BulletinHeaderPacket(security);
	}

	public void testCreateUniversalId()
	{
		UniversalId uid = BulletinHeaderPacket.createUniversalId(security);
		assertEquals("account", security.getPublicKeyString(), uid.getAccountId());
		assertStartsWith("prefix", "B-", uid.getLocalId());
	}

	public void testPrefix()
	{
		assertEquals("not legal?", true, BulletinHeaderPacket.isValidLocalId("B-12345"));
		assertEquals("was legal?", false, BulletinHeaderPacket.isValidLocalId("F-12345"));
	}
	
	public void testBulletinType()
	{
		BulletinHeaderPacket hp = new BulletinHeaderPacket();
		assertEquals("Default type?", hp.getBulletinType(), Bulletin.BulletinType.LEGACY_BULLETIN);
		
		hp = new BulletinHeaderPacket(security);
		assertEquals("Default type passing in just a security?", hp.getBulletinType(), Bulletin.BulletinType.LEGACY_BULLETIN);

		UniversalId newNoteUniversalId = BulletinHeaderPacket.createUniversalId(security, Bulletin.BulletinType.NOTE);
		hp = new BulletinHeaderPacket(newNoteUniversalId);		
		assertEquals(hp.getBulletinType(), Bulletin.BulletinType.NOTE);
		
		UniversalId newRecordUniversalId = BulletinHeaderPacket.createUniversalId(security, Bulletin.BulletinType.RECORD);
		hp = new BulletinHeaderPacket(newRecordUniversalId);		
		assertEquals(hp.getBulletinType(), Bulletin.BulletinType.RECORD);
		
		hp = new BulletinHeaderPacket(security, Bulletin.BulletinType.NOTE);
		assertEquals(hp.getBulletinType(), Bulletin.BulletinType.NOTE);

		hp = new BulletinHeaderPacket(security, Bulletin.BulletinType.RECORD);		
		assertEquals(hp.getBulletinType(), Bulletin.BulletinType.RECORD);
		
		hp = new BulletinHeaderPacket(security, Bulletin.BulletinType.LEGACY_BULLETIN);		
		assertEquals(hp.getBulletinType(), Bulletin.BulletinType.LEGACY_BULLETIN);
	}

	public void testConstructorWithId()
	{
		final String accountId = "some account id";
		final String packetId = "some local id";
		UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, packetId);
		BulletinHeaderPacket p = new BulletinHeaderPacket(uid);
		assertEquals("accountId?", accountId, p.getAccountId());
		assertEquals("packetId?", packetId, p.getLocalId());
	}

	public void testGetFieldDataPacketId()
	{
		BulletinHeaderPacket simple = new BulletinHeaderPacket(security);
		assertNull("data not null?", simple.getFieldDataPacketId());
		assertNull("private data not null?", simple.getPrivateFieldDataPacketId());

		String sampleId = "this is a valid id. really.";
		bhp.setFieldDataPacketId(sampleId);
		assertEquals(sampleId, bhp.getFieldDataPacketId());

		String privateId = "private data id";
		bhp.setPrivateFieldDataPacketId(privateId);
		assertEquals(privateId, bhp.getPrivateFieldDataPacketId());
	}

	public void testAddAndGetAttachments()
	{
		bhp.clearAllUserData();
		
		assertEquals("count before adding public", 0, bhp.getPublicAttachmentIds().length);
		assertEquals("count before adding private", 0, bhp.getPrivateAttachmentIds().length);

		bhp.addPublicAttachmentLocalId(attachmentId1);
		String[] list1 = bhp.getPublicAttachmentIds();
		assertEquals("count after adding 1", 1, list1.length);
		assertEquals("list1 missing a1?", attachmentId1, list1[0]);
		assertEquals("private count after adding public", 0, bhp.getPrivateAttachmentIds().length);

		bhp.addPublicAttachmentLocalId(attachmentId2);
		String[] list2 = bhp.getPublicAttachmentIds();
		assertEquals("count after adding 2", 2, list2.length);
		assertEquals("list2 a1 in wrong position?", 1, Arrays.binarySearch(list2, attachmentId1));
		assertEquals("list2 a2 in wrong position?", 0, Arrays.binarySearch(list2, attachmentId2));

		bhp.addPublicAttachmentLocalId(attachmentId2);
		assertEquals("count after dupe", 2, bhp.getPublicAttachmentIds().length);
		assertEquals("private count after adding multiple publics", 0, bhp.getPrivateAttachmentIds().length);

		bhp.addPrivateAttachmentLocalId(attachmentId3);
		assertEquals("private count after adding 1 private", 1, bhp.getPrivateAttachmentIds().length);
		assertEquals("public count after adding private", 2, bhp.getPublicAttachmentIds().length);

		bhp.addPrivateAttachmentLocalId(attachmentId4);
		String[] list3 = bhp.getPublicAttachmentIds();
		String[] list4 = bhp.getPrivateAttachmentIds();
		assertEquals("private count after adding 2 private", 2, bhp.getPrivateAttachmentIds().length);
		assertEquals("public count after adding multiple privates", 2, bhp.getPublicAttachmentIds().length);
		assertEquals("public list3 a1 in wrong position?", 1, Arrays.binarySearch(list3, attachmentId1));
		assertEquals("public list3 a2 in wrong position?", 0, Arrays.binarySearch(list3, attachmentId2));
		assertEquals("private list4 a3 in wrong position?", 1, Arrays.binarySearch(list4, attachmentId3));
		assertEquals("private list4 a4 in wrong position?", 0, Arrays.binarySearch(list4, attachmentId4));

		bhp.clearAllUserData();
		assertEquals("private count after clear", 0, bhp.getPrivateAttachmentIds().length);
		assertEquals("public count after clear", 0, bhp.getPublicAttachmentIds().length);

	}

	public void testStatus()
	{
		assertEquals("not empty to start?", "", bhp.getStatus());
		bhp.setStatus("abc");
		assertEquals("not set right?", "abc", bhp.getStatus());
	}

	public void testAllPrivate() throws Exception
	{
		UniversalId dummyUid = UniversalIdForTesting.createDummyUniversalId();
		BulletinHeaderPacket unknownPrivacy = new BulletinHeaderPacket(dummyUid);
		assertEquals("knows privacy?", false, unknownPrivacy.hasAllPrivateFlag());
		unknownPrivacy.setAllPrivate(true);

		bhp.setAllPrivate(false);
		assertEquals("doesn't know privacy after set false?", true, bhp.hasAllPrivateFlag());
		assertEquals("private?", false, bhp.isAllPrivate());

		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		bhp.writeXml(out1, security);
		
		ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(out1.toByteArray());
		BulletinHeaderPacket loadedBhp1 = new BulletinHeaderPacket(UniversalIdForTesting.createDummyUniversalId());
		loadedBhp1.loadFromXml(in1, security);
		assertEquals("doesn't know privacy after loaded false?", true, loadedBhp1.hasAllPrivateFlag());
		assertEquals("private after load?", false, loadedBhp1.isAllPrivate());

		bhp.setAllPrivate(true);
		assertEquals("doesn't know privacy after set true?", true, bhp.hasAllPrivateFlag());
		assertEquals("not private?", true, bhp.isAllPrivate());

		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		bhp.writeXml(out2, security);
		ByteArrayInputStreamWithSeek in2 = new ByteArrayInputStreamWithSeek(out2.toByteArray());
		BulletinHeaderPacket loadedBhp2 = new BulletinHeaderPacket(UniversalIdForTesting.createDummyUniversalId());
		loadedBhp2.loadFromXml(in2, security);
		assertEquals("doesn't know privacy after loaded true?", true, loadedBhp2.hasAllPrivateFlag());
		assertEquals("not private after load?", true, loadedBhp2.isAllPrivate());

		String result2 = new String(out2.toByteArray(), "UTF-8");
		int startTagStart = result2.indexOf(MartusXml.AllPrivateElementName) - 1;
		int endTagEnd = result2.indexOf("/" + MartusXml.AllPrivateElementName) + MartusXml.AllPrivateElementName.length() + 1;
		String withoutTag = result2.substring(0, startTagStart) + result2.substring(endTagEnd);
		ByteArrayInputStreamWithSeek in3 = new ByteArrayInputStreamWithSeek(withoutTag.getBytes("UTF-8"));
		BulletinHeaderPacket loadedBhp3 = new BulletinHeaderPacket(UniversalIdForTesting.createDummyUniversalId());
		loadedBhp3.setAllPrivate(true);
		loadedBhp3.loadFromXml(in3, null);
		assertEquals("knows privacy after loaded without tag?", false, loadedBhp3.hasAllPrivateFlag());
		assertEquals("not private after load without tag?", true, loadedBhp3.isAllPrivate());
	}

	public void testAllHQsProxyUpload() throws Exception
	{
		UniversalId dummyUid = UniversalIdForTesting.createDummyUniversalId();
		BulletinHeaderPacket noProxyUploaders = new BulletinHeaderPacket(dummyUid);
		assertEquals("Can Upload?", false, noProxyUploaders.canAllHQsProxyUpload());

		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		bhp.writeXml(out1, security);
		
		ByteArrayInputStreamWithSeek in1 = new ByteArrayInputStreamWithSeek(out1.toByteArray());
		BulletinHeaderPacket loadedBhp1 = new BulletinHeaderPacket(UniversalIdForTesting.createDummyUniversalId());
		loadedBhp1.loadFromXml(in1, security);
		assertEquals("Should now have all HQs as proxy uploaders?", true, loadedBhp1.canAllHQsProxyUpload());
	}
	
	public void testWriteXml() throws Exception
	{
		String dataId = "this data id";
		String privateId = "this data id";
		bhp.clearAllUserData();
		bhp.setImmutableOnServer(true);
		bhp.updateLastSavedTime();
		bhp.setFieldDataPacketId(dataId);
		bhp.setPrivateFieldDataPacketId(privateId);
		bhp.setFieldDataSignature(sampleSig1);
		bhp.setPrivateFieldDataSignature(sampleSig2);
		bhp.addPublicAttachmentLocalId(attachmentId1);
		bhp.addPublicAttachmentLocalId(attachmentId2);
		bhp.addPrivateAttachmentLocalId(attachmentId3);
		bhp.addPrivateAttachmentLocalId(attachmentId4);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		String result = new String(out.toByteArray(), "UTF-8");
		assertContains(MartusXml.getTagStart(MartusXml.BulletinHeaderPacketElementName), result);
		assertContains(MartusXml.getTagEnd(MartusXml.BulletinHeaderPacketElementName), result);
		assertContains(bhp.getLocalId(), result);

		assertContains(MartusXml.getTagStart(MartusXml.DataPacketIdElementName), result);
		assertContains(dataId, result);
		assertContains(MartusXml.getTagEnd(MartusXml.DataPacketIdElementName), result);

		assertContains(MartusXml.getTagStart(MartusXml.DataPacketSigElementName), result);
		assertContains("missing data sig?", StreamableBase64.encode(sampleSig1), result);
		assertContains(MartusXml.getTagEnd(MartusXml.DataPacketSigElementName), result);

		assertContains(MartusXml.getTagStart(MartusXml.PrivateDataPacketIdElementName), result);
		assertContains(privateId, result);
		assertContains(MartusXml.getTagEnd(MartusXml.PrivateDataPacketIdElementName), result);

		assertContains(MartusXml.getTagStart(MartusXml.PrivateDataPacketSigElementName), result);
		assertContains("missing private data sig?", StreamableBase64.encode(sampleSig2), result);
		assertContains(MartusXml.getTagEnd(MartusXml.PrivateDataPacketSigElementName), result);

		assertContains(MartusXml.getTagStart(MartusXml.PublicAttachmentIdElementName), result);
		assertContains(attachmentId1, result);
		assertContains(MartusXml.getTagEnd(MartusXml.PublicAttachmentIdElementName), result);

		assertContains(attachmentId2, result);

		assertContains(MartusXml.getTagStart(MartusXml.PrivateAttachmentIdElementName), result);
		assertContains(attachmentId3, result);
		assertContains(MartusXml.getTagEnd(MartusXml.PrivateAttachmentIdElementName), result);

		assertContains(attachmentId4, result);

		assertContains(Long.toString(bhp.getLastSavedTime()), result);

		assertNotContains(MartusXml.getTagStart(MartusXml.HQPublicKeyElementName), result);
		
		assertNotContains(MartusXml.getTagStart(MartusXml.AccountsAuthorizedToReadElementName), result);
		assertNotContains(MartusXml.getTagStart(MartusXml.AccountsAuthorizedToReadPendingElementName), result);
		
		assertNotContains(MartusXml.getTagStart(MartusXml.HistoryElementName), result);

		assertNotContains(MartusXml.getTagStart(MartusXml.ExtendedHistorySectionName), result);
		assertNotContains(MartusXml.getTagStart(MartusXml.ExtendedHistoryEntryName), result);
		assertNotContains(MartusXml.getTagStart(MartusXml.ExtendedHistoryClonedFromAccountName), result);
		
		assertContains(MartusXml.getTagStart(MartusXml.StatusSnapshotName), result);
		assertContains(MartusXml.getTagEnd(MartusXml.StatusSnapshotName), result);

		assertContains(MartusXml.getTagStart(MartusXml.ImmutableOnServerName), result);
		assertContains(MartusXml.getTagEnd(MartusXml.ImmutableOnServerName), result);
	}
	
	public void testWriteXmlWithHistory() throws Exception
	{
		BulletinHistory history = createFakeHistory();
		bhp.setHistory(history);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		String result = new String(out.toByteArray(), "UTF-8");
		assertContains(MartusXml.getTagStart(MartusXml.HistoryElementName), result);
		assertContains(history.get(0), result);
		assertContains(history.get(1), result);
		
	}
	
	private BulletinHistory createFakeHistory()
	{
		BulletinHistory history = new BulletinHistory();
		history.add("pretend local id");
		history.add("another fake localid");
		return history;
	}

	public void testWriteXmlWithExtendedHistory() throws Exception
	{
		ExtendedHistoryList history = new ExtendedHistoryList();

		BulletinHistory firstClone = createFakeHistory();
		String firstClientAccountId = MockMartusSecurity.createClient().getPublicKeyString();
		history.add(firstClientAccountId, firstClone);

		BulletinHistory secondClone = new BulletinHistory();
		secondClone.add("random");
		String secondClientAccountId = MockMartusSecurity.createOtherClient().getPublicKeyString();
		history.add(secondClientAccountId, secondClone);

		bhp.setExtendedHistory(history);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		String result = new String(out.toByteArray(), "UTF-8");
		assertContains(MartusXml.getTagStart(MartusXml.ExtendedHistorySectionName), result);
		assertContains(MartusXml.getTagStart(MartusXml.ExtendedHistoryEntryName), result);
		assertContains(MartusXml.getTagStart(MartusXml.ExtendedHistoryClonedFromAccountName), result);
		assertContains("Missing first account id?", firstClientAccountId, result);
		assertContains("Missing second account id?", secondClientAccountId, result);
		for(int i = 0; i < firstClone.size(); ++i)
			assertContains("Missing first local id " + i + " ", firstClone.get(i), result);
		for(int i = 0; i < secondClone.size(); ++i)
			assertContains("Missing second local id " + i + " ", secondClone.get(i), result);
	}

	public void testWriteXmlWithHQKeySet() throws Exception
	{
		String dataId = "this data id";
		String privateId = "this data id";
		String hqPublicKey = "hqkey123";
		HeadquartersKeys hqKeys = new HeadquartersKeys();
		HeadquartersKey hqKey = new HeadquartersKey(hqPublicKey);
		hqKeys.add(hqKey);
		bhp.setAuthorizedToReadKeys(hqKeys);
		bhp.setFieldDataPacketId(dataId);
		bhp.setPrivateFieldDataPacketId(privateId);
		bhp.setFieldDataSignature(sampleSig1);
		bhp.setPrivateFieldDataSignature(sampleSig2);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		String result = new String(out.toByteArray(), "UTF-8");
		assertContains(MartusXml.getTagStart(MartusXml.HQPublicKeyElementName), result);
	}

	public void testWriteXmlWithNoFieldData() throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		String result = new String(out.toByteArray(), "UTF-8");
		assertContains(MartusXml.getTagStart(MartusXml.BulletinHeaderPacketElementName), result);
		assertContains(MartusXml.getTagEnd(MartusXml.BulletinHeaderPacketElementName), result);
		assertContains(bhp.getLocalId(), result);
	}

	public void testLoadXml() throws Exception
	{
		UniversalId uid = HeaderPacketWithUnknownTag.createUniversalId(security);
		HeaderPacketWithUnknownTag bhpUnknownTag = new HeaderPacketWithUnknownTag(uid);
		
		String dataId = "some id";
		String privateId = "private id";
		String sampleStatus = "draft or whatever";
		bhpUnknownTag.updateLastSavedTime();
		bhpUnknownTag.setStatus(sampleStatus);
		bhpUnknownTag.setFieldDataPacketId(dataId);
		bhpUnknownTag.setPrivateFieldDataPacketId(privateId);
		bhpUnknownTag.setFieldDataSignature(sampleSig1);
		bhpUnknownTag.setPrivateFieldDataSignature(sampleSig2);
		bhpUnknownTag.addPublicAttachmentLocalId(attachmentId1);
		bhpUnknownTag.addPublicAttachmentLocalId(attachmentId2);
		bhpUnknownTag.addPrivateAttachmentLocalId(attachmentId3);
		bhpUnknownTag.addPrivateAttachmentLocalId(attachmentId4);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhpUnknownTag.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");

		BulletinHeaderPacket loaded = new BulletinHeaderPacket(security);
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		loaded.loadFromXml(in, security);

		assertEquals("account", bhpUnknownTag.getAccountId(), loaded.getAccountId());
		assertEquals("local id", bhpUnknownTag.getLocalId(), loaded.getLocalId());
		assertEquals("time", bhpUnknownTag.getLastSavedTime(), loaded.getLastSavedTime());
		assertEquals("id", bhpUnknownTag.getLocalId(), loaded.getLocalId());
		assertEquals("data id", bhpUnknownTag.getFieldDataPacketId(), loaded.getFieldDataPacketId());
		assertEquals("private id", bhpUnknownTag.getPrivateFieldDataPacketId(), loaded.getPrivateFieldDataPacketId());
		assertEquals("status", sampleStatus, loaded.getStatus());
		assertEquals("data sig", true, Arrays.equals(sampleSig1, loaded.getFieldDataSignature()));
		assertEquals("private data sig", true, Arrays.equals(sampleSig2, loaded.getPrivateFieldDataSignature()));
		assertEquals("hqKey", "", loaded.getLegacyHQPublicKey());
		assertTrue("No unknown?", loaded.hasUnknownTags());
		assertEquals("AuthorizedToReadKeys", 0, loaded.getAuthorizedToReadKeys().size());
		assertEquals("AuthorizedToProxyUpload", 0, loaded.getAuthorizedToUploadKeys().size());
		assertEquals("has history?", 0, loaded.getHistory().size());
		assertEquals("Incorrect version #?",1, loaded.getVersionNumber());
		assertEquals("Since it has no history, should have same local id for original revision id.?", bhpUnknownTag.getLocalId(), loaded.getOriginalRevisionId());
		assertEquals("Has extended history?", 0, loaded.getExtendedHistory().size());

		String[] list = loaded.getPublicAttachmentIds();
		assertEquals("public count", 2, list.length);
		assertEquals("public attachments wrong?", true, Arrays.equals(bhpUnknownTag.getPublicAttachmentIds(), list));
		String[] list2 = loaded.getPrivateAttachmentIds();
		assertEquals("private count", 2, list2.length);
		assertEquals("private attachments wrong?", true, Arrays.equals(bhpUnknownTag.getPrivateAttachmentIds(), list2));
	}
	
	public void testLoadXmlWithHistory() throws Exception
	{
		BulletinHistory history = createFakeHistory();
		bhp.setHistory(history);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");

		BulletinHeaderPacket loaded = new BulletinHeaderPacket(security);
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		loaded.loadFromXml(in, security);
		
		BulletinHistory loadedHistory = loaded.getHistory();
		verifyEqualHistories(history, loadedHistory);
		
		assertEquals("Incorrect version #?",history.size()+1, loaded.getVersionNumber());
		assertEquals("Incorrect original revision id?", history.get(0), loaded.getOriginalRevisionId());
	}

	private void verifyEqualHistories(BulletinHistory history,
			BulletinHistory loadedHistory)
	{
		assertEquals("no history?", history.size(), loadedHistory.size());
		for(int i=0; i < history.size(); ++i)
			assertEquals("wrong history " + i + "?", history.get(i), loadedHistory.get(i));
	}

	public void testLoadXmlWithExtendedHistory() throws Exception
	{
		ExtendedHistoryList history = new ExtendedHistoryList();

		BulletinHistory firstClone = createFakeHistory();
		String firstClientAccountId = MockMartusSecurity.createClient().getPublicKeyString();
		history.add(firstClientAccountId, firstClone);

		BulletinHistory secondClone = new BulletinHistory();
		secondClone.add("random");
		String secondClientAccountId = MockMartusSecurity.createOtherClient().getPublicKeyString();
		history.add(secondClientAccountId, secondClone);

		bhp.setExtendedHistory(history);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");

		BulletinHeaderPacket loaded = new BulletinHeaderPacket(security);
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		loaded.loadFromXml(in, security);
		
		ExtendedHistoryList loadedHistory = loaded.getExtendedHistory();
		assertEquals("wrong extended history size?", history.size(), loadedHistory.size());
		for(int clone = 0; clone < history.size(); ++clone)
		{
			ExtendedHistoryEntry oldEntry = history.getHistory(clone);
			ExtendedHistoryEntry newEntry = loadedHistory.getHistory(clone);
			assertEquals("Wrong account id for " + clone + " ", oldEntry.getClonedFromAccountId(), newEntry.getClonedFromAccountId());
			verifyEqualHistories(oldEntry.getClonedHistory(), newEntry.getClonedHistory());
		}
	}

	public void testLoadXmlWithHQKey() throws Exception
	{
		String hqPublicKey = "sdjflksj";
		String hqLabel = "Should never be shown in BHP";
		HeadquartersKeys hqKeys = new HeadquartersKeys();
		HeadquartersKey hqKey = new HeadquartersKey(hqPublicKey, hqLabel);
		hqKeys.add(hqKey);
		bhp.setAuthorizedToReadKeys(hqKeys);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");

		BulletinHeaderPacket loaded = new BulletinHeaderPacket(security);
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		loaded.loadFromXml(in, security);

		assertEquals("hqKey", bhp.getLegacyHQPublicKey(), loaded.getLegacyHQPublicKey());
		assertEquals("The # of authorized accounts not set by just setting the HQ Key?", 1, loaded.getAuthorizedToReadKeys().size());
		HeadquartersKey loadedKey = loaded.getAuthorizedToReadKeys().get(0);
		assertEquals("hqKey not present in authorized list?", hqPublicKey, loadedKey.getPublicKey());
		assertNotEquals("Should not contain label", hqLabel, loadedKey.getLabel());
	}

	public void testLoadXmlWithMultipleAuthorizedToReadKeys() throws Exception
	{
		bhp.clearAllUserData();
		String hqKey1 = "Key 1";
		String hqKey2 = "Key 2";
		HeadquartersKeys hqKeys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(hqKey1);
		HeadquartersKey key2 = new HeadquartersKey(hqKey2);
		hqKeys.add(key1);
		hqKeys.add(key2);
		bhp.setAuthorizedToReadKeys(hqKeys);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");

		BulletinHeaderPacket loaded = new BulletinHeaderPacket(security);
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		loaded.loadFromXml(in, security);
		assertEquals("The # of authorized accounts not set?", hqKeys.size(), loaded.getAuthorizedToReadKeys().size());
		assertEquals("Key 1 not present?", hqKey1, (loaded.getAuthorizedToReadKeys().get(0)).getPublicKey());
		assertEquals("Key 2 not present?", hqKey2, (loaded.getAuthorizedToReadKeys().get(1)).getPublicKey());
		assertEquals("The original hqKey should be set from first key in vector", hqKey1, loaded.getLegacyHQPublicKey());
		assertEquals("Key 1 not allowed to Upload?", hqKey1, (loaded.getAuthorizedToUploadKeys().get(0)).getPublicKey());
		assertEquals("Key 2 not allowed to upload?", hqKey2, (loaded.getAuthorizedToUploadKeys().get(1)).getPublicKey());
	}
	
	public void testLoadXmlWithMultipleAuthorizedToReadKeysPending() throws Exception
	{
		bhp.clearAllUserData();
		String hqKey1 = "Key 1";
		String hqKey2 = "Key 2";
		HeadquartersKeys hqKeys = new HeadquartersKeys();
		HeadquartersKey key1 = new HeadquartersKey(hqKey1);
		HeadquartersKey key2 = new HeadquartersKey(hqKey2);
		hqKeys.add(key1);
		hqKeys.add(key2);
		bhp.setAuthorizedToReadKeysPending(hqKeys);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		String result = new String(out.toByteArray(), "UTF-8");

		BulletinHeaderPacket loaded = new BulletinHeaderPacket(security);
		byte[] bytes = result.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in = new ByteArrayInputStreamWithSeek(bytes);
		loaded.loadFromXml(in, security);
		assertEquals("The # of authorized accounts not 0?", 0, loaded.getAuthorizedToReadKeys().size());
		assertEquals("The # of pending accounts not set?", hqKeys.size(), loaded.getAuthorizedToReadKeysPending().size());
		assertEquals("Key 1 not present?", hqKey1, (loaded.getAuthorizedToReadKeysPending().get(0)).getPublicKey());
		assertEquals("Key 2 not present?", hqKey2, (loaded.getAuthorizedToReadKeysPending().get(1)).getPublicKey());
		assertEquals("The original hqKey should be blank", "", loaded.getLegacyHQPublicKey());
		assertEquals("Should not have any HQ's that can Proxy Upload?", 0, loaded.getAuthorizedToUploadKeys().size());
	}

	public void testBackwardHQCompatibility() throws Exception
	{
		String authorAccountId = "MIIBIDANBgkqhkiG9w0BAQEFAAOCAQ0AMIIBCAKCAQEAlBOc0WjiSlX6ejv6+QNfMWbVA/fZ8fQEXtvjT/hdpox6Nf02GV+t/PeMM5vf2/uvW1QBKfCzcIHbdObIOZAAwjXhoFqba6eLmaMGAvmSnPD6h2i6mL0/DkZ2QURYU+PDrSzlugIJm6rgaZxyGKdCscxf0Sb6JQPUswfl42TV8e87LlXIqAOY5UnN5DpwmgSNDE1RqVn68Z++Ez3dfFCDMe36BSkyzNXM0D+hTgjTRm0A+opUIa0f6vrUnzsUYoFeGqRMcO5SMuYkdsONrMAgGX57fOFPVEvxlwAlMq/uAPRhdFDTH77th7nIC4vitQxvifFPDJCblZ1DN46hxpQwtwIBEQ==";
		String hqAccountId = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhNrVbw9MznNACGyy0FgpE+1Qi6D68QoBribm6UQA9VBkTDY+skdXFXae3UqMtkg6S0XQEgR+KdalWk5rNiv0jmowZzjvhnyEkn8z7CRM3imdPzCO1bcoFUjH+XIpO1v8ThLnRusZXBsc5cDl0mhEoDKPVVzUgNvDGiVRz24duFo8W6qOLnMTO44MJxqN7rlrt49mV971+GnVOnaRaQfGJSEMkXcdK6WTvchwPEX1Air5S1/Jn3hIpdk3YVOdBgWNK++Vu/0B8Crzr8cBj82wp+7fvyiqB04FGHBycDjuSVNeyoRQElKvHun6m67yzMwWAvAAUSzejktANVoi5Hg20wIDAQAB";
		String fieldOfficeBulletinCreatedWithOldMartus = 
				"<!--MartusPacket;20030212;-->" + "\n" + 
				"<BulletinHeaderPacket><PacketId>B-eb9f0d-ff08384b4a--8000</PacketId>" + "\n" + 
				"<Account>" + authorAccountId + "</Account>" + "\n" + 
				"<BulletinStatus>sealed</BulletinStatus>" + "\n" + 
				"<LastSavedTime>1095354606644</LastSavedTime>" + "\n" + 
				"<AllPrivate>0</AllPrivate>" + "\n" + 
				"<HQPublicKey>" + hqAccountId + "</HQPublicKey>" + "\n" + 
				"<DataPacketId>F-eb9f0d-ff08384b4a--7fff</DataPacketId>" + "\n" + 
				"<DataPacketSig>dBZENOPohfe8+3N/F9ly9HbV/FTlUry3/V38ahrpJvSi5ZyfLa7r5tQrTqZXwbzp2q3p5OCeQf/0LQtoUeaONHwUmPCxgty6WylZflUww2lSDSF4Kb+KpLJJYQ0gf6TNd3qKJliPE0tesnsEoGhxlcO6C/u8uEgGMerJ/PZqYRYnljbA2L1ne9X61PDP5TTMN3d6xpR6N5+Y5NVom/hslw0h2VQa5UTHvsbjcorwSj+xTnX4vbq3DAWNGwsvzyF4WEPv6Ahv5AdHTCB0UeThoyRiH4rjDHwx2dP3pmSI21wxD8859BG9Lxr22knlmOypXAhC3hzGiKIG/MM6HYZNjQ==</DataPacketSig>" + "\n" + 
				"<PrivateDataPacketId>F-eb9f0d-ff08384b4a--7ffe</PrivateDataPacketId>" + "\n" + 
				"<PrivateDataPacketSig>DLUrPpgAg/fjiJ+MqdYuY68QwSOoLnP8uNIWZmTUeX63BPMdiBKZSCnysxTu/yEO2D9VMst4iegsSpiBE5cpZDy6nGt9zlPZHv/6IaZLxUZHb9agGYWo8EIHNqEk0ryHHEJOWVhmx3G9/nyhdZOe9RNj6eSt7QhppAaqCeRnV9ldTBLt4+jTTmfeehrkEbKECBxiuD9dPQtu9BpXqQ6V5EMP3pgD3IzrCtDFOHFuS/HVUlGCt+70NR0TAReiPt9kdWkbls0ir501qPDmf8XKp8TfzIyRj9iQ1RO18NJCE9GEJupgH5WAv/BXH2GTM22Z+E8RVHNMIULTqqUx9UJMKA==</PrivateDataPacketSig>" + "\n" + 
				"</BulletinHeaderPacket>" + "\n" + 
				"<!--sig=QqzQFmrA9rYo7ekVgqjtXBfhRBaVnrpRjnJfynao90pNtF0cjK9R2nYOdGuChrwI9BvEqtX26ylH8iWky9J29RZOYIGOzQwNo/63DfKi4KChhfDd7hVdi6yhfGXdiPoaF3hR4kzalgV5q415xCE+wAOza8NMzmScB8H1m2b4/pU407jVKlKHNYB12OrltEqT6k4OlbS1I6eLJ3bRjBumsgtQLZteUXusAHsI7h8w5UQ3VVaV1d7EDvX5fLAoxVE5cEUegFffIfUjXM2/cUcnqFnLCm5TkIsU5xpVTbPkrd999sMM/zU0+VKCIiDIvfh4rnW72h0jLog5GIwGBzVU+w==-->" + "\n"; 
		
		BulletinHeaderPacket bhpToTest = new BulletinHeaderPacket();
		InputStreamWithSeek in = new ByteArrayInputStreamWithSeek(fieldOfficeBulletinCreatedWithOldMartus.getBytes("UTF-8"));
		bhpToTest.loadFromXml(in, security);
		HeadquartersKeys hqKeys = bhpToTest.getAuthorizedToReadKeys();
		assertEquals(1, hqKeys.size());
		HeadquartersKey thisKey = hqKeys.get(0);
		assertEquals(hqAccountId, thisKey.getPublicKey());
	}
	
	byte[] sampleSig1 = {1,6,38,0};
	byte[] sampleSig2 = {7, 9, 9};
	final String attachmentId1 = "second alphabetically";
	final String attachmentId2 = "alphabetically first";
	final String attachmentId3 = "alphabetically after 4";
	final String attachmentId4 = "4 first";

	private BulletinHeaderPacket bhp;
	static MartusCrypto security;
}
