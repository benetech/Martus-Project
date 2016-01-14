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

package org.martus.common.bulletinstore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.martus.common.LoggerInterface;
import org.martus.common.MartusUtilities;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.bulletin.PendingAttachmentList;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.StreamEncryptor;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.PacketStreamOpener;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.database.ReadableDatabase.PacketVisitor;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongAccountException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.common.packet.UniversalId;
import org.martus.util.StreamCopier;
import org.martus.util.StreamFilter;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeek;



public class BulletinStore
{
	public BulletinStore()
	{
		cacheManager = new BulletinStoreCacheManager();

		bulletinHistoryAndHqCache = new BulletinHistoryAndHqCache(this);
		addCache(bulletinHistoryAndHqCache);
	}

	public void doAfterSigninInitialization(File dataRootDirectory, Database db) throws Exception
	{
		dir = dataRootDirectory;
		database = db;
		database.initialize();
	}
	
	public void close()
	{
		dir = null;
		database = null;
	}

	public void setSignatureGenerator(MartusCrypto securityToUse)
	{
		security = securityToUse;
	}
	
	public MartusCrypto getSignatureGenerator()
	{
		return security;
	}
	
	public MartusCrypto getSignatureVerifier()
	{
		return security;
	}
	
	public ReadableDatabase getDatabase()
	{
		return database;
	}
	
	protected Database getWriteableDatabase()
	{
		return database;
	}
	
	public void setDatabase(Database toUse)
	{
		database = toUse;
	}
	
	public File getStoreRootDir()
	{
		return dir;
	}

	public String getAccountId()
	{
		return security.getPublicKeyString();
	}

	public int getBulletinCount()
	{
		return getAllBulletinLeafUids().size();
	}

	class LeafFinder implements PacketVisitor
	{
		public LeafFinder()
		{
			leafUids = new HashSet();
		}
		
		public Set getLeafUids()
		{
			return leafUids;
		}
		
		public void visit(DatabaseKey key)
		{
			UniversalId uid = key.getUniversalId();
			if(isLeaf(uid))
				leafUids.add(uid);
		}
		
		private HashSet leafUids;
	}

	public Set getAllBulletinLeafUids()
	{
		LeafFinder leafFinder = new LeafFinder();
		visitAllBulletinRevisions(leafFinder);
				
		return leafFinder.getLeafUids();
	}

	private Set getAllBulletinLeafUidsForAccount(String publicKeyString)
	{
		LeafFinder leafFinder = new LeafFinder();
		visitAllBulletinRevisionsForAccount(leafFinder, publicKeyString);
				
		return leafFinder.getLeafUids();
	}

	public boolean isLeaf(UniversalId uid)
	{
		if(BulletinStoreCache.findKey(getDatabase(), uid) == null)
			return false;

		return !hasNewerRevision(uid);
	}

	public boolean hasNewerRevision(UniversalId uid)
	{
		Set descendents = bulletinHistoryAndHqCache.getAllKnownDescendents(uid);
		Iterator it = descendents.iterator();
		while(it.hasNext())
		{
			UniversalId descendent = (UniversalId)it.next();
			if(doesBulletinRevisionExist(descendent))
				return true;
		}
		
		return false;
	}

	public boolean doesBulletinRevisionExist(UniversalId descendent)
	{
		return BulletinStoreCache.findKey(getDatabase(), descendent) != null;
	}

	public boolean doesBulletinRevisionExist(DatabaseKey key)
	{
		return getDatabase().doesRecordExist(key);
	}
	
	public boolean doesBulletinDelRecordExist(DatabaseKey key)
	{
		return getDatabase().doesRecordExist(key);
	}
	
	public void deleteAllData() throws Exception
	{
		deleteAllBulletins();
	}

	public void deleteAllBulletins() throws Exception
	{
		database.deleteAllData();
		cacheManager.storeWasCleared();
	}
	
	public boolean isBulletinValid(Bulletin b)
	{
		boolean nonAttachmentDataValid = b.isNonAttachmentDataValid();
		boolean attachmentsValid = areAttachmentsValid(b);
		return nonAttachmentDataValid && attachmentsValid;
	}

	public boolean areAttachmentsValid(Bulletin b)
	{
		if(!areAttachmentsValid(b.getPublicAttachments()))
			return false;
		
		if(!areAttachmentsValid(b.getPrivateAttachments()))
			return false;
		
		return true;
	}
	
	private boolean areAttachmentsValid(AttachmentProxy[] proxies)
	{
		return (isAttachmentsValid(getDatabase(), getSignatureVerifier(), proxies));
	}

	public void importZipFileToStoreWithSameUids(File inputFile) throws Exception
	{
		ZipFile zip = new ZipFile(inputFile);
		try
		{
			importBulletinZipFile(zip);
		}
		catch (Database.RecordHiddenException shouldBeImpossible)
		{
			shouldBeImpossible.printStackTrace();
			throw new IOException(shouldBeImpossible.toString());
		}
		catch(WrongAccountException shouldBeImpossible)
		{
			throw new Packet.InvalidPacketException("Wrong account???");
		}
		finally
		{
			zip.close();
		}
	}

	public void addCache(BulletinStoreCache cacheToAdd)
	{
		cacheManager.addCache(cacheToAdd);
	}
	
	public void clearCache()
	{
		cacheManager.clearCache();
	}

	public void revisionWasSaved(BulletinHeaderPacket bhp) throws Exception
	{
		cacheManager.revisionWasSaved(bhp.getUniversalId());
	}
	
	public void revisionWasRemoved(UniversalId uid) throws Exception
	{
		cacheManager.revisionWasRemoved(uid);
	}
	
	public boolean hadErrorsWhileCacheing()
	{
		return bulletinHistoryAndHqCache.hadErrors();
	}
	
	public Vector getFieldOffices(String hqAccountId)
	{
		return bulletinHistoryAndHqCache.getFieldOffices(hqAccountId);
	}

	public void visitAllBulletins(Database.PacketVisitor visitor)
	{
		Set uids = getAllBulletinLeafUids();
		Iterator it = uids.iterator();
		while(it.hasNext())
		{
			UniversalId uid = (UniversalId)it.next();
			visitor.visit(BulletinStoreCache.findKey(getDatabase(), uid));
		}
	}
	
	public void visitAllBulletinsForAccount(Database.PacketVisitor visitor, String publicKeyString)
	{
		Set uids = getAllBulletinLeafUidsForAccount(publicKeyString);
		Iterator it = uids.iterator();
		while(it.hasNext())
		{
			UniversalId uid = (UniversalId)it.next();
			visitor.visit(BulletinStoreCache.findKey(getDatabase(), uid));
		}
	}

	public void visitAllBulletinRevisions(Database.PacketVisitor visitorToUse)
	{
		class BulletinKeyFilter implements Database.PacketVisitor
		{
			BulletinKeyFilter(ReadableDatabase db, Database.PacketVisitor visitorToUse2)
			{
				visitor = visitorToUse2;
				db.visitAllRecords(this);
			}
	
			public void visit(DatabaseKey key)
			{
				if(BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
				{
					visitor.visit(key);
				}
			}
			ReadableDatabase.PacketVisitor visitor;
		}
	
		new BulletinKeyFilter(getDatabase(), visitorToUse);
	}

	private void visitAllBulletinRevisionsForAccount(Database.PacketVisitor visitorToUse, String publicKeyString)
	{
		class BulletinKeyFilter implements Database.PacketVisitor
		{
			BulletinKeyFilter(ReadableDatabase db, Database.PacketVisitor visitorToUse2, String publicKeyString2)
			{
				visitor = visitorToUse2;
				db.visitAllRecordsForAccount(this, publicKeyString2);
			}
	
			public void visit(DatabaseKey key)
			{
				if(BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
				{
					visitor.visit(key);
				}
			}
			ReadableDatabase.PacketVisitor visitor;
		}
	
		new BulletinKeyFilter(getDatabase(), visitorToUse, publicKeyString);
	}

	public synchronized void removeBulletinFromStore(Bulletin b) throws IOException
	{
		BulletinHistory history = b.getHistory();
		try
		{
			for(int i = 0; i < history.size(); ++i)
			{
				String localIdOfAncestor = history.get(i);
				UniversalId uidOfAncestor = UniversalId.createFromAccountAndLocalId(b.getAccount(), localIdOfAncestor);
				DatabaseKey key = DatabaseKey.createImmutableKey(uidOfAncestor);
				if(doesBulletinRevisionExist(key))
					deleteBulletinRevision(key);
			}

			BulletinHeaderPacket bhpMain = b.getBulletinHeaderPacket();
			deleteBulletinRevisionFromDatabase(bhpMain);
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			throw new IOException("Unable to delete bulletin");
		}
	}

	public void deleteBulletinRevision(DatabaseKey keyToDelete) throws Exception
	{
		BulletinHeaderPacket bhp = loadBulletinHeaderPacket(getDatabase(), keyToDelete, getSignatureVerifier());
		deleteBulletinRevisionFromDatabase(bhp);
	}

	public void deleteBulletinRevisionFromDatabase(BulletinHeaderPacket bhp) throws Exception
	{
		DatabaseKey[] keys = BulletinZipUtilities.getAllPacketKeys(bhp);
		for (int i = 0; i < keys.length; i++)
		{
			deleteSpecificPacket(keys[i]);
		}
		revisionWasRemoved(bhp.getUniversalId());
	}

	public static BulletinHeaderPacket loadBulletinHeaderPacket(PacketStreamOpener db, DatabaseKey key, MartusCrypto security)
	throws
		IOException,
		CryptoException,
		InvalidPacketException,
		WrongPacketTypeException,
		SignatureVerificationException,
		DecryptionException
	{
		InputStreamWithSeek in = db.openInputStream(key, security);
		try
		{
			BulletinHeaderPacket bhp = new BulletinHeaderPacket();
			bhp.loadFromXml(in, security);
			return bhp;
		}
		finally
		{
			in.close();
		}
	}

	public void hidePackets(Vector packetsIdsToHide, LoggerInterface logger) throws Exception
	{
		Database db = getWriteableDatabase();
		for(int i = 0; i < packetsIdsToHide.size(); ++i)
		{
			UniversalId uId = (UniversalId)(packetsIdsToHide.get(i));
			db.hide(uId);
			revisionWasRemoved(uId);
			String publicCode = MartusCrypto.getFormattedPublicCode(uId.getAccountId());
			logger.logNotice("Deleting " + publicCode + ": " + uId.getLocalId());
		
		}
	}
	
	public UniversalId importBulletinZipFile(ZipFile zip) 
		throws Exception
	{
		return importBulletinZipFile(zip, null, System.currentTimeMillis());
	}

	public UniversalId importBulletinZipFile(ZipFile zip, String accountIdIfKnown, long mTime) 
		throws Exception
	{
		BulletinHeaderPacket bhp = importBulletinPacketsFromZipFileToDatabase(accountIdIfKnown, zip, mTime);
		revisionWasSaved(bhp);
		return bhp.getUniversalId();
	}

	private BulletinHeaderPacket importBulletinPacketsFromZipFileToDatabase(String authorAccountId, ZipFile zip, long mTime)
		throws Exception
	{
		BulletinHeaderPacket header = BulletinHeaderPacket.loadFromZipFile(zip, security);
		if(authorAccountId == null)
			authorAccountId = header.getAccountId();
	
		BulletinZipUtilities.validateIntegrityOfZipFilePackets(authorAccountId, zip, security);
		Database db = getWriteableDatabase();
		UniversalId headerUid = header.getUniversalId();
		DatabaseKey mutableKey = DatabaseKey.createMutableKey(headerUid);
		DatabaseKey legacyKey = DatabaseKey.createLegacyKey(headerUid);
		if(db.doesRecordExist(mutableKey) || db.doesRecordExist(legacyKey))
		{
			deleteDraftBulletinPackets(db, headerUid, security);
			revisionWasRemoved(headerUid);
		}
	
		HashMap zipEntries = new HashMap();
		StreamCopier copier = new StreamCopier();
		StreamEncryptor encryptor = new StreamEncryptor(security);
	
		DatabaseKey[] keys = BulletinZipUtilities.getAllPacketKeys(header);
		for (int i = 0; i < keys.length; i++)
		{
			String localId = keys[i].getLocalId();
			ZipEntry entry = zip.getEntry(localId);
	
			InputStreamWithSeek in = new ZipEntryInputStreamWithSeek(zip, entry);
	
			File file = db.createTempFile(getSignatureGenerator());
			file.deleteOnExit();
			FileOutputStream rawOut = new FileOutputStream(file);
	
			StreamFilter filter = copier;
			if(db.mustEncryptLocalData() && MartusUtilities.doesPacketNeedLocalEncryption(localId, header, in))
				filter = encryptor;
	
			MartusUtilities.copyStreamWithFilter(in, rawOut, filter);
	
			rawOut.close();
			in.close();
			file.setLastModified(mTime);
			UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, keys[i].getLocalId());
			DatabaseKey key = header.createKeyWithHeaderStatus(uid);
	
			zipEntries.put(key,file);
		}
		db.importFiles(zipEntries);
		return header;
	}

	private static void deleteDraftBulletinPackets(Database db, UniversalId bulletinUid, MartusCrypto security) throws
	IOException
	{
		DatabaseKey headerKey = DatabaseKey.createMutableKey(bulletinUid);
		if(!db.doesRecordExist(headerKey))
			return;
		try
		{
			BulletinHeaderPacket bhp = BulletinStore.loadBulletinHeaderPacket(db, headerKey, security);
	
			String accountId = bhp.getAccountId();
			deleteDraftPacket(db, accountId, bhp.getLocalId());
			deleteDraftPacket(db, accountId, bhp.getFieldDataPacketId());
			deleteDraftPacket(db, accountId, bhp.getPrivateFieldDataPacketId());
	
			String[] publicAttachmentIds = bhp.getPublicAttachmentIds();
			for(int i = 0; i < publicAttachmentIds.length; ++i)
			{
				deleteDraftPacket(db, accountId, publicAttachmentIds[i]);
			}
	
			String[] privateAttachmentIds = bhp.getPrivateAttachmentIds();
			for(int i = 0; i < privateAttachmentIds.length; ++i)
			{
				deleteDraftPacket(db, accountId, privateAttachmentIds[i]);
			}
		}
		catch (Exception e)
		{
			throw new IOException(e.toString());
		}
	}
	
	private static void deleteDraftPacket(Database db, String accountId, String localId)
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);
		DatabaseKey key = DatabaseKey.createMutableKey(uid);
		db.discardRecord(key);
	}
	
	
	
	protected void deleteSpecificPacket(DatabaseKey burKey)
	{
		getWriteableDatabase().discardRecord(burKey);
	}
	
	public void saveBulletinForTesting(Bulletin b) throws Exception
	{
		saveBulletin(b, false);
	}
	
	// TODO: Not sure this method is really needed. I think the tests were 
	// encrypting (mostly) for legacy reasons. We should stamp out all calls
	// to this, at which point we should be able to rename saveBulletinForTesting 
	// to simply saveBulletin, have it trust getDatabase().mustEncryptPublicData(),
	// and then ClientBulletinStore.saveBulletin can just invoke super after clearin its cache
	// kbs. 2004-10-06
	public void saveEncryptedBulletinForTesting(Bulletin b) throws Exception
	{
		saveBulletin(b, true);
	}
	
	protected void saveBulletin(Bulletin b, boolean mustEncryptPublicData) throws Exception
	{
		saveToClientDatabase(b, getWriteableDatabase(), mustEncryptPublicData, b.getSignatureGenerator());
		revisionWasSaved(b.getBulletinHeaderPacket());
	}
	
	protected BulletinHistoryAndHqCache getHistoryAndHqCache()
	{
		return bulletinHistoryAndHqCache;
	}

	private static boolean isAttachmentsValid(ReadableDatabase db, MartusCrypto verifier, AttachmentProxy[] attachmentProxies)
	{
		if(attachmentProxies == null)
			return true;
		for(int i = 0; i< attachmentProxies.length; ++i)
		{
			if(attachmentProxies[i].getPendingPacket() != null)
				continue;
			
			UniversalId id = attachmentProxies[i].getUniversalId();
			DatabaseKey key = DatabaseKey.createImmutableKey(id);
			InputStreamWithSeek in = null;
			try
			{
				in = db.openInputStream(key, verifier);
			}
			catch (Exception e)
			{
				return false;
			}
			if(in == null)
				return false;

			try
			{
				Packet.verifyPacketSignature(in,verifier);
			}
			catch (Exception e)
			{
				return false;
			}
			finally
			{
				try
				{
					in.close();
				}
				catch(IOException e)
				{
					return false;
				}
			}
		}
		return true;
	}

	private static void saveToClientDatabase(Bulletin b, Database db, boolean mustEncryptPublicData, MartusCrypto signer) throws Exception
	{
		UniversalId uid = b.getUniversalId();
		DatabaseKey key = DatabaseKey.createLegacyKey(uid);

		saveBulletinToDatabase(db, b, mustEncryptPublicData, signer, key);
	}

	public static void saveBulletinToDatabase(Database db, Bulletin b, boolean mustEncryptPublicData, MartusCrypto signer, DatabaseKey key) throws Exception
	{
		UniversalId uid = b.getUniversalId();
		BulletinHeaderPacket oldBhp = new BulletinHeaderPacket(uid);
		boolean bulletinAlreadyExisted = false;
		try
		{
			if(db.doesRecordExist(key))
			{
				oldBhp = loadBulletinHeaderPacket(db, key, signer);
				bulletinAlreadyExisted = true;
			}
		}
		catch(Exception ignoreItBecauseWeCantDoAnythingAnyway)
		{
			//e.printStackTrace();
			//System.out.println("Bulletin.saveToDatabase: " + e);
		}
	
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
	
		FieldDataPacket publicDataPacket = b.getFieldDataPacket();
		boolean shouldEncryptPublicData = (b.isMutable() || b.isAllPrivate());
		publicDataPacket.setEncrypted(shouldEncryptPublicData);
		
		Packet packet1 = publicDataPacket;
		boolean encryptPublicData = mustEncryptPublicData;
		byte[] dataSig = packet1.writeXmlToClientDatabase(db, encryptPublicData, signer);
		bhp.setFieldDataSignature(dataSig);
		
		Packet packet2 = b.getPrivateFieldDataPacket();
		boolean encryptPublicData1 = mustEncryptPublicData;
		byte[] privateDataSig = packet2.writeXmlToClientDatabase(db, encryptPublicData1, signer);
		bhp.setPrivateFieldDataSignature(privateDataSig);
	
		writePendingAttachments(b.getPendingPublicAttachments(), db, mustEncryptPublicData, signer);
		writePendingAttachments(b.getPendingPrivateAttachments(), db, mustEncryptPublicData, signer);
	
		bhp.updateLastSavedTime();
		Packet packet = bhp;
		packet.writeXmlToClientDatabase(db, mustEncryptPublicData, signer);
	
		if(bulletinAlreadyExisted)
		{
			String accountId = b.getAccount();
			String[] oldPublicAttachmentIds = oldBhp.getPublicAttachmentIds();
			String[] newPublicAttachmentIds = bhp.getPublicAttachmentIds();
			BulletinStore.deleteRemovedPackets(db, accountId, oldPublicAttachmentIds, newPublicAttachmentIds);
	
			String[] oldPrivateAttachmentIds = oldBhp.getPrivateAttachmentIds();
			String[] newPrivateAttachmentIds = bhp.getPrivateAttachmentIds();
			BulletinStore.deleteRemovedPackets(db, accountId, oldPrivateAttachmentIds, newPrivateAttachmentIds);
		}
	}

	private static void writePendingAttachments(PendingAttachmentList pendingPublicAttachments, Database db, boolean mustEncryptPublicData, MartusCrypto signer) throws IOException, CryptoException
	{
		for(int i = 0; i < pendingPublicAttachments.size(); ++i)
		{
			// TODO: Should the bhp also remember attachment sigs?
			Packet packet = pendingPublicAttachments.get(i);
			packet.writeXmlToClientDatabase(db, mustEncryptPublicData, signer);
		}
	}

	private static void deleteRemovedPackets(Database db, String accountId, String[] oldIds, String[] newIds)
	{
		for(int oldIndex = 0; oldIndex < oldIds.length; ++oldIndex)
		{
			String oldLocalId = oldIds[oldIndex];
			if(!MartusUtilities.isStringInArray(newIds, oldLocalId))
			{
				UniversalId auid = UniversalId.createFromAccountAndLocalId(accountId, oldLocalId);
				db.discardRecord(DatabaseKey.createLegacyKey(auid));
			}
		}
	}
	
	private MartusCrypto security;
	private File dir;
	private Database database;
	private BulletinHistoryAndHqCache bulletinHistoryAndHqCache;
	private BulletinStoreCacheManager cacheManager;
}

