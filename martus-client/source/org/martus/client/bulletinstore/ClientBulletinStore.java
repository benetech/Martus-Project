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

package org.martus.client.bulletinstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipFile;

import javafx.beans.property.Property;
import javafx.collections.ObservableSet;

import org.martus.client.core.MartusClientXml;
import org.martus.client.core.templates.FormTemplateManager;
import org.martus.client.swingui.bulletintable.BulletinTableModel;
import org.martus.common.BulletinSummary;
import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusXml;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.BulletinZipImporter;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.ClientFileDatabase;
import org.martus.common.database.Database;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.ExtendedHistoryList;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.UniversalId.NotUniversalIdException;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlParser;
import org.martus.util.xml.SimpleXmlStringLoader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;



/*
	This class represents a collection of bulletins
	(and also a collection of folders) stored on the
	client pc.

	It is responsible for managing the lifetimes of
	both bulletins and folders, including saving and
	loading them to/from disk.
*/
public class ClientBulletinStore extends BulletinStore
{
	private static final String SPACE = " ";
	public ClientBulletinStore(MartusCrypto cryptoToUse)
	{
		setSignatureGenerator(cryptoToUse);
		bulletinDataCache = new PartialBulletinCache(getTagsOfCachedFields());
	}
	
	public void doAfterSigninInitialization(File dataRootDirectory) throws Exception
	{
		Database db = createDatabase(dataRootDirectory);
		doAfterSigninInitialization(dataRootDirectory, db);
	}

	public Database createDatabase(File dataRootDirectory)
	{
		File dbDirectory = new File(dataRootDirectory, "packets");
		Database db = new ClientFileDatabase(dbDirectory, getSignatureGenerator());
		return db;
	}

	public void doAfterSigninInitialization(File dataRootDirectory, Database db) throws Exception
	{
		super.doAfterSigninInitialization(dataRootDirectory, db);
		
		initializeFolders();

		loadCache();
		
		File obsoleteCacheFile = new File(getStoreRootDir(), OBSOLETE_CACHE_FILE_NAME);
		obsoleteCacheFile.delete();

		createKnownFieldSpecCache();

		initializeFormTemplateManager();
	}

	public void prepareToExitNormally() throws Exception
	{
		MartusLogger.logBeginProcess("saveSessionKeyCache");
		saveBulletinDataCache();
		MartusLogger.logEndProcess("saveSessionKeyCache");

		MartusLogger.logBeginProcess("saveFieldSpecCache");
		saveFieldSpecCache();
		MartusLogger.logEndProcess("saveFieldSpecCache");
	}

	private void saveFieldSpecCache() throws Exception
	{
		if(knownFieldSpecCache.saving)
			return;
		
		OutputStream out = new FileOutputStream(getFieldSpecCacheFile());
		knownFieldSpecCache.saveToStream(out);
		out.close();
	}
	
	public void prepareToExitWithoutSavingState()
	{
		getSignatureGenerator().flushSessionKeyCache();
	}
	
	public boolean loadFieldSpecCache()
	{
		File file = getFieldSpecCacheFile();
		if(!file.exists())
			return false;
		
		try
		{
			InputStream in = new FileInputStream(file);
			knownFieldSpecCache.loadFromStream(in);
			in.close();
			file.delete();
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public void createFieldSpecCacheFromDatabase()
	{
		knownFieldSpecCache.initializeFromDatabase();
	}
	
	private void createKnownFieldSpecCache()
	{
		if(knownFieldSpecCache != null)
			return;
		
		knownFieldSpecCache = new KnownFieldSpecCache(getDatabase(), getSignatureGenerator());
		addCache(knownFieldSpecCache);
	}
	
	public boolean mustEncryptPublicData()
	{
		return getDatabase().mustEncryptLocalData();
	}
	
	public boolean isMyBulletin(UniversalId uid)
	{
		return(uid.getAccountId().equals(getAccountId()));
	}

	public synchronized Set getSetOfBulletinUniversalIdsInFolders()
	{
		Set setOfUniversalIds = new HashSet();

		Vector visibleFolders = getAllVisibleFolders();
		for(Iterator f = visibleFolders.iterator(); f.hasNext();)
		{
			BulletinFolder folder = (BulletinFolder) f.next();
			setOfUniversalIds.addAll(folder.getAllUniversalIdsUnsorted());
		}
		BulletinFolder importFolder = findFolder(IMPORT_FOLDER);
		if(importFolder != null)
			setOfUniversalIds.addAll(importFolder.getAllUniversalIdsUnsorted());
		
		return setOfUniversalIds;
	}

	public Set getSetOfOrphanedBulletinUniversalIds()
	{
		Set possibleOrphans = getAllBulletinLeafUids();
		Set inFolders = getSetOfBulletinUniversalIdsInFolders();
		possibleOrphans.removeAll(inFolders);
		return possibleOrphans;
	}

	public synchronized void destroyBulletin(Bulletin b) throws IOException
	{
		removeBulletinFromAllFolders(b);
		bulletinDataCache.remove(b.getUniversalId());
		removeBulletinFromStore(b);
	}

	public void removeBulletinFromAllFolders(Bulletin b) throws IOException
	{
		BulletinHistory history = b.getHistory();
		for(int i = 0; i < history.size(); ++i)
		{
			String localId = history.get(i);
			UniversalId uidOfAncestor = UniversalId.createFromAccountAndLocalId(b.getAccount(), localId);
			removeRevisionFromAllFolders(uidOfAncestor);
		}
		
		removeRevisionFromAllFolders(b.getUniversalId());
	}

	private void removeRevisionFromAllFolders(UniversalId id)
	{
		for(int f = 0; f < getFolderCount(); ++f)
		{
			removeBulletinFromFolder(getFolder(f), id);
		}
	}
	
	public BulletinHistory getBulletinHistory(UniversalId uid)
	{
		DatabaseKey key = DatabaseKey.createLegacyKey(uid);
		if(!doesBulletinRevisionExist(key))
			return new BulletinHistory();
		
		try
		{
			BulletinHeaderPacket bhp = loadBulletinHeaderPacket(getDatabase(), key, getSignatureVerifier());
			return bhp.getHistory();
		}
		catch (Exception e)
		{
			//TODO: Better error handling
			System.out.println("BulletinStore.getBulletinHistory: " + e);
			e.printStackTrace();
			return null;
		}
	}

	public Bulletin getBulletinRevision(UniversalId uid)
	{
		DatabaseKey key = DatabaseKey.createLegacyKey(uid);
		if(!doesBulletinRevisionExist(key))
		{
			//System.out.println("BulletinStore.findBulletinByUniversalId: !doesRecordExist");
			return null;
		}

		try
		{
			Bulletin b = loadFromDatabase(key);
			return b;
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
			return null;
		}
		catch(Exception e)
		{
			//TODO: Better error handling
			System.out.println("BulletinStore.findBulletinByUniversalId: " + e);
			e.printStackTrace();
			return null;
		}
	}

	public String getSentTag(UniversalId uid)
	{
		boolean knownNotOnServer = isProbablyNotOnServer(uid);

		if(getFolderDraftOutbox().contains(uid))
		{
			if(isMyBulletin(uid))
				return WAS_SENT_NO;
			if(!knownNotOnServer)
				return null;
		}

		if(knownNotOnServer)
			return WAS_SENT_NO;

		if(isProbablyOnServer(uid))
			return WAS_SENT_YES;
		
		return null;
	}

	public String getFieldData(UniversalId uid, String fieldTag)
	{
		if(fieldTag.equals(Bulletin.TAGWASSENT) || fieldTag.equals(Bulletin.PSEUDOFIELD_WAS_SENT))
		{
			String tag = getSentTag(uid);
			if(tag == null)
				return "";
			return tag;
		}
			
		if(bulletinDataCache.isBulletinCached(uid))
			return bulletinDataCache.getFieldData(uid, fieldTag);
		
		Bulletin b = getBulletinRevision(uid);
		MartusField field = b.getField(fieldTag);
		if(field == null)
			return "";
		return field.getData();
	}

	public Bulletin loadFromDatabase(DatabaseKey key) throws
		Exception
	{
		Bulletin b = BulletinLoader.loadFromDatabase(getDatabase(), key, getSignatureVerifier());
		bulletinDataCache.add(b);
		return b;
	}

	public void saveBulletin(Bulletin b) throws Exception
	{
		bulletinDataCache.remove(b.getUniversalId());
		saveBulletin(b, mustEncryptPublicData());
	}

	public synchronized void discardBulletin(BulletinFolder f, UniversalId uid) throws IOException
	{
		try
		{
			if(f==null || !f.equals(folderDiscarded))
				folderDiscarded.add(uid);
		}
		catch (BulletinAlreadyExistsException saveToIgnoreException)
		{
		}
		if(f != null)
			removeBulletinFromFolder(f, uid);
		if(isOrphan(uid))
			destroyBulletin(getBulletinRevision(uid));
	}

	public Bulletin chooseBulletinToUpload(BulletinFolder hiddenFolder, int startIndex)
	{
		UniversalId[] uids = hiddenFolder.getAllUniversalIdsUnsortedAsArray();
		for(int i=0; i < uids.length; ++i)
		{
			++startIndex;
			if(startIndex >= uids.length)
				startIndex = 0;
			if(!isDiscarded(uids[startIndex]))
			{
				Bulletin b = getBulletinRevision(uids[startIndex]);
				return b;
			}
		}
		return null;
	}

	public boolean hasAnyNonDiscardedBulletins(BulletinFolder hiddenFolder)
	{
		for(int i=0; i < hiddenFolder.getBulletinCount(); ++i)
		{
			UniversalId uid = hiddenFolder.getBulletinUniversalIdSorted(i);
			if(!isDiscarded(uid))
				return true;
		}
		return false;
	}
	
	public boolean isDiscarded(UniversalId uid)
	{
		return getFolderDiscarded().contains(uid);
	}

	public synchronized BulletinFolder createFolder(String name)
	{
		BulletinFolder folder = rawCreateFolder(name);
		return folder;
	}	

	public synchronized boolean renameFolder(String oldName, String newName)
	{		
		if(!isFolderNameValid(newName))
			return false;
		
		if(findFolder(newName) != null)
			return false;

		BulletinFolder folder = findFolder(oldName);
		if(folder == null)
			return false;

		folder.setName(newName);
		saveFolders();
		return true;
	}

	public boolean isFolderNameValid(String newName)
	{
		if (newName.length() == 0 || newName.startsWith(SPACE) || newName.endsWith(SPACE))
			return false;
			
		char[] strOfArray = newName.toCharArray();								
		for(int i = 0; i < strOfArray.length; ++i)
		{			
			if (!MartusUtilities.isValidCharInFolder(strOfArray[i]))
				return false;
		}	
		return true;
	}
	
	public boolean doesFolderNameAlreadyExist(String name)
	{
		BulletinFolder folder = findFolder(name);
		if (folder == null) 
			return false;
		return true;
	}

	public synchronized boolean deleteFolder(String name)
	{
		BulletinFolder folder = findFolder(name);
		if(folder == null)
			return false;

		if(!folder.canDelete())
			return false;

		BulletinFolder discarded = getFolderDiscarded();
		discarded.prepareForBulkOperation();
		while(folder.getBulletinCount() > 0)
		{
			Bulletin b = folder.getBulletinSorted(0);
			try
			{
				discarded.add(b);
			}
			catch (BulletinAlreadyExistsException safeToIgnore)
			{
			}
			catch (IOException safeToIgnore)
			{
				safeToIgnore.printStackTrace();
			}
			folder.remove(b.getUniversalId());
		}

		folders.remove(folder);
		saveFolders();
		return true;
	}

	public void clearFolder(String folderName)
	{
		BulletinFolder folder = findFolder(folderName);
		if(folder == null)
			return;

		folder.removeAll();
		saveFolders();
	}

	public synchronized int getFolderCount()
	{
		if(folders == null)
			return 0;
		
		return folders.size();
	}

	private synchronized BulletinFolder getFolder(int index)
	{
		if(index < 0 || index >= folders.size())
			return null;

		return (BulletinFolder)folders.get(index);
	}

	public synchronized BulletinFolder findFolder(String name)
	{
		if(name == null)
			return null;
		
		for(int index=0; index < getFolderCount(); ++index)
		{
			BulletinFolder folder = getFolder(index);
			if(name.equals(folder.getName()))
				return folder;
		}
		return null;
	}

	public synchronized Vector getAllFolders()
	{
		Vector allFolders = new Vector();
		for(int f = 0; f < getFolderCount(); ++f)
		{
			BulletinFolder folder = getFolder(f);
			allFolders.add(folder);
		}
		return allFolders;
	}
	
	public synchronized Vector getAllVisibleFolders()
	{
		Vector allFolders = getAllFolders();
		Vector visibleFolders = new Vector();
		for(Iterator f = allFolders.iterator(); f.hasNext();)
		{
			BulletinFolder folder = (BulletinFolder) f.next();
			if(folder.isVisible())
				visibleFolders.add(folder);
		}
		return visibleFolders;
	}
	
	public synchronized Vector getAllFolderNames()
	{
		Vector names = new Vector();
		Vector allFolders = getAllFolders();
		for(int f = 0; f < allFolders.size(); ++f)
		{
			names.add(((BulletinFolder)allFolders.get(f)).getName());
		}
		return names;
	}
	
	public synchronized void setFolderOrder(Vector foldersInOrder) throws Exception
	{
		if(getFolderCount() != foldersInOrder.size())
			throw new Exception("Incorrect number of folders");
		folders.clear();
		folders.addAll(foldersInOrder);
		saveFolders();
	}

	public synchronized Vector getVisibleFolderNames()
	{
		Vector names = new Vector();
		Vector visibleFolders = getAllVisibleFolders();
		for(Iterator f = visibleFolders.iterator(); f.hasNext();)
		{
			BulletinFolder folder = (BulletinFolder) f.next();
			String folderName = folder.getName();
			names.add(folderName);
		}
		return names;
	}

	public String getSearchFolderName()
	{
		return SEARCH_RESULTS_BULLETIN_FOLDER;
	}

	public String getOrphanFolderName()
	{
		return RECOVERED_BULLETIN_FOLDER;
	}
	
		
	public String getNameOfFolderForAllRetrieved()
	{
		return RETRIEVED_FOLDER;
	}

	public String getNameOfFolderRetrievedSealed()
	{
		return RETRIEVE_SEALED_BULLETIN_FOLDER;
	}

	public String getNameOfFolderRetrievedDraft()
	{
		return RETRIEVE_DRAFT_BULLETIN_FOLDER;
	}

	public String getNameOfFolderRetrievedFieldOfficeSealed()
	{
		return RETRIEVE_SEALED_FIELD_OFFICE_BULLETIN_FOLDER;
	}

	public String getNameOfFolderRetrievedFieldOfficeDraft()
	{
		return RETRIEVE_DRAFT_FIELD_OFFICE_BULLETIN_FOLDER;
	}

	public String getNameOfFolderDamaged()
	{
		return DAMAGED_BULLETIN_FOLDER;
	}


	public BulletinFolder getFolderDiscarded()
	{
		return folderDiscarded;
	}

	public BulletinFolder getFolderSaved()
	{
		return folderSaved;
	}

	public BulletinFolder getFolderDraftOutbox()
	{
		return folderDraftOutbox;
	}
	
	public BulletinFolder getFolderSealedOutbox()
	{
		return folderSealedOutbox;
	}
	
	public BulletinFolder getFolderImport()
	{
		return createOrFindFolder(IMPORT_FOLDER);
	}

	private BulletinFolder getFolderOnServer()
	{
		return createOrFindFolder(ON_SERVER_FOLDER);
	}
		
	private BulletinFolder getFolderNotOnServer()
	{
		return createOrFindFolder(NOT_ON_SERVER_FOLDER);
	}
	
	public boolean needsFolderMigration()
	{
		if(findFolder(OBSOLETE_DRAFT_FOLDER) != null)
			return true;
		if(findFolder(OBSOLETE_OUTBOX_FOLDER) != null)
			return true;
		return false;
	}
	
	public boolean migrateFolders() throws IOException
	{
		// NOTE: Perform the steps from most critical to least!
		BulletinFolder oldOutbox = findFolder(OBSOLETE_OUTBOX_FOLDER);
		BulletinFolder newSealedOutbox = getFolderSealedOutbox();
		BulletinFolder saved = getFolderSaved();
		Vector oldSavedBulletinIds = pullBulletinUidsOutOfFolder(oldOutbox);
		addBulletinIdsToFolder(newSealedOutbox, oldSavedBulletinIds);
		folders.remove(oldOutbox);
		addBulletinIdsToFolder(saved, oldSavedBulletinIds);
		
		BulletinFolder oldDraftFolder = findFolder(OBSOLETE_DRAFT_FOLDER);
		Vector oldDraftBulletinIds = pullBulletinUidsOutOfFolder(oldDraftFolder);
		folders.remove(oldDraftFolder);
		addBulletinIdsToFolder(saved, oldDraftBulletinIds);
		
		saveFolders();

		return true;
	}

	public void addBulletinIdsToFolder(BulletinFolder folder, Vector bulletinUids) throws IOException
	{
		folder.prepareForBulkOperation();
		for (int i = 0; i < bulletinUids.size(); i++) 
		{
			UniversalId uid = (UniversalId)bulletinUids.get(i);
			try
			{
				ensureBulletinIsInFolder(folder, uid);
			}
			catch(AddOlderVersionToFolderFailedException harmlessException)
			{
				System.out.println("Exception: Bulletin:"+uid+" is older.");
			}
		}
	}

	private Vector pullBulletinUidsOutOfFolder(BulletinFolder folder)
	{
		Vector bulletinUids = new Vector();
		if(folder != null)
		{
			Set unsortedBulletinList = folder.getAllUniversalIdsUnsorted();
			
			for(Iterator iter = unsortedBulletinList.iterator(); iter.hasNext();)
			{
				UniversalId uid = (UniversalId) iter.next();
				bulletinUids.add(uid);
				removeBulletinFromFolder(folder, uid);
			}
		}
		return bulletinUids;
	}

	public void createSystemFolders()
	{
		folderSaved = createSystemFolder(SAVED_FOLDER);
		folderDiscarded = createSystemFolder(DISCARDED_FOLDER);
		folderDraftOutbox = createSystemFolder(DRAFT_OUTBOX);
		folderSealedOutbox = createSystemFolder(SEALED_OUTBOX);
	}

	public BulletinFolder createSystemFolder(String name)
	{
		BulletinFolder folder = rawCreateFolder(name);
		if(folder == null)
			folder = findFolder(name);
		folder.preventRename();
		folder.preventDelete();
		return folder;
	}
	
	// synchronized because updateOnServerLists is called from background thread
	public synchronized boolean isProbablyOnServer(UniversalId uid)
	{
		return getFolderOnServer().contains(uid);
	}
	
	// synchronized because updateOnServerLists is called from background thread
	public synchronized boolean isProbablyNotOnServer(UniversalId uid)
	{
		return getFolderNotOnServer().contains(uid);
	}
	
	public  void setIsOnServer(Bulletin b)
	{
		setIsOnServer(b.getUniversalId());
	}

	// synchronized because updateOnServerLists is called from background thread
	public synchronized void setIsOnServer(UniversalId uid)
	{
		removeBulletinFromFolder(getFolderNotOnServer(), uid);
		try
		{
			getFolderOnServer().add(uid);
		}
		catch(BulletinAlreadyExistsException harmless)
		{
		}
		catch(Exception ignoreForNow)
		{
			// TODO: Figure out if this should be propagated
			ignoreForNow.printStackTrace();
		}
	}

	public  void setIsNotOnServer(Bulletin b)
	{
		setIsNotOnServer(b.getUniversalId());
	}
	
	// synchronized because updateOnServerLists is called from background thread
	public synchronized void setIsNotOnServer(UniversalId uid)
	{
		removeBulletinFromFolder(getFolderOnServer(), uid);
		try
		{
			getFolderNotOnServer().add(uid);
		}
		catch(BulletinAlreadyExistsException harmless)
		{
		}
		catch(Exception ignoreForNow)
		{
			// TODO: Figure out if this should be propagated
			ignoreForNow.printStackTrace();
		}
	}

	// synchronized because updateOnServerLists is called from background thread
	public synchronized void clearOnServerLists()
	{
		getFolderOnServer().removeAll();
		getFolderNotOnServer().removeAll();
	}
	
	public void updateOnServerLists(Set summariesOnServer)
	{
		HashSet uidsOnThisComputer = new HashSet(1000);
		uidsOnThisComputer.addAll(getUidsOfAllBulletinRevisions());
		internalUpdateOnServerLists(summariesOnServer, uidsOnThisComputer);
		saveFolders();
	}
	
	//	 synchronized because updateOnServerLists is called from background thread
	private synchronized void internalUpdateOnServerLists(Set summariesOnServer, HashSet uidsInStore)
	{
		HashSet uidsNotOnServer = new HashSet(uidsInStore);
		BulletinFolder draftOutbox = getFolderDraftOutbox();
		for(Iterator iter = summariesOnServer.iterator(); iter.hasNext(); )
		{
			BulletinSummary summary = (BulletinSummary) iter.next();
			UniversalId uid = summary.getUniversalId();
			uidsNotOnServer.remove(uid);
			if(uidsInStore.contains(uid))
			{
				if(!draftOutbox.contains(uid))
					setIsOnServer(uid);
			}
		}
		for(Iterator iter = uidsNotOnServer.iterator(); iter.hasNext(); )
		{
			UniversalId uid = (UniversalId)iter.next();
			setIsNotOnServer(uid);
		}
	}

	public Bulletin copyBulletinWithoutContactsOrHistory(UniversalId bulletinId, String newTitle) throws Exception
	{
		Bulletin original = getBulletinRevision(bulletinId);
		FieldSpecCollection publicFieldSpecsToUse = original.getTopSectionFieldSpecs();
		FieldSpecCollection privateFieldSpecsToUse = original.getBottomSectionFieldSpecs();
		Bulletin copy = createNewDraft(original, publicFieldSpecsToUse, privateFieldSpecsToUse);
		copy.set(Bulletin.TAGTITLE, newTitle);
		clearAuthorizedToReadKeys(copy);
		copy.setHistory(new BulletinHistory());
		copy.getBulletinHeaderPacket().setExtendedHistory(new ExtendedHistoryList());
		return copy;
	}

	private void clearAuthorizedToReadKeys(Bulletin copy)
	{
		copy.clearAuthorizedToReadKeys();
	}

	public boolean isMyBulletin(Bulletin original)
	{
		return original.getAccount().equals(getAccountId());
	}
	
	public synchronized void moveBulletin(Bulletin b, BulletinFolder from, BulletinFolder to) throws IOException
	{
		if(linkBulletinToFolder(b, to))
		{
			removeBulletinFromFolder(from, b);
			saveFolders();
		}
	}

	public synchronized boolean linkBulletinToFolder(Bulletin b, BulletinFolder to) throws IOException
	{
		try
		{
			to.add(b);
			return true;
		}
		catch (BulletinAlreadyExistsException e)
		{
			//System.out.println("Bulletin already exists in destination folder");
		}
		return false;
	}

	public void removeBulletinFromFolder(BulletinFolder from, Bulletin b)
	{
		removeBulletinFromFolder(from, b.getUniversalId());
	}

	public synchronized void removeBulletinFromFolder(BulletinFolder from, UniversalId uid)
	{
		from.remove(uid);
	}

	public Vector findBulletinInAllVisibleFolders(UniversalId uid)
	{
		Vector allFolders= getVisibleFolderNames();
		Vector foldersContainingBulletin = new Vector();
		for(int i = 0; i < allFolders.size(); ++i)
		{
			BulletinFolder folder = findFolder((String)allFolders.get(i));
			if(folder != null && folder.contains(uid))
				foldersContainingBulletin.add(folder);
		}
		return foldersContainingBulletin;
	}

	public void deleteAllData() throws Exception
	{
		super.deleteAllData();
		deleteFoldersDatFile();
		resetFolders();
	}			
	
	public void deleteFoldersDatFile()
	{
		getFoldersFile().delete();
	}	
	
	public void resetFolders()
	{
		initializeFolders();
	}
	
	public void scrubAllData() throws Exception
	{
		class PacketScrubber implements Database.PacketVisitor 
		{
			PacketScrubber(Database databaseToUse)
			{
				db = databaseToUse;
			}
			
			public void visit(DatabaseKey key)
			{
				try
				{
					db.scrubRecord(key);
					db.discardRecord(key);
					revisionWasRemoved(key.getUniversalId());
				}
				catch (Exception e)
				{				
					e.printStackTrace();
				}				
			}
			
			Database db;
		}
	
		PacketScrubber ac = new PacketScrubber(getWriteableDatabase());
		getDatabase().visitAllRecords(ac);
		deleteFoldersDatFile();
	}	

	public void signAccountMap() throws MartusSignatureException, IOException
	{
		getWriteableDatabase().signAccountMap();
	}

	public synchronized void loadFolders()
	{
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStreamWithSeek in = new FileInputStreamWithSeek(getFoldersFile());
			getSignatureVerifier().decrypt(in, out);
			in.close();

			String folderXml = new String(out.toByteArray(), "UTF-8");
			internalLoadFolders(folderXml);
			if(needsLegacyFolderConversion())
			{
				saveFolders();
				loadedLegacyFolders = false;
			}
		}
		catch(UnsupportedEncodingException e)
		{
			System.out.println("BulletinStore.loadFolders: " + e);
		}
		catch(FileNotFoundException expectedIfFoldersDontExistYet)
		{
		}
		catch(Exception e)
		{
			// TODO: Improve error handling!!!
			System.out.println("BulletinStore.loadFolders: " + e);
			e.printStackTrace();
		}
	}

	public synchronized void saveFolders()
	{
		try
		{
			String xml = foldersToXml();
			byte[] bytes = xml.getBytes("UTF-8");
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);

			getFoldersFile().getParentFile().mkdirs();
			FileOutputStream out = new FileOutputStream(getFoldersFile());
			try
			{
				if(getSignatureGenerator() == null)
					return;
				getSignatureGenerator().encrypt(in, out);
			}
			finally
			{
				out.close();
			}
		}
		catch(UnsupportedEncodingException e)
		{
			System.out.println("BulletinStore.saveFolders: " + e);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("BulletinStore.saveFolders: " + e);
		}
	}

	public File getFoldersFile()
	{
		return getFoldersFileForAccount(getStoreRootDir());
	}

	static public File getFoldersFileForAccount(File AccountDir)
	{
		return new File(AccountDir, "MartusFolders.dat");
	}

	public String getCurrentFormTemplateName() throws Exception
	{
		return getCurrentFormTemplate().getTitle();
	}

	public FormTemplate getCurrentFormTemplate() throws Exception
	{
		return formTemplateManager.getCurrentFormTemplate();
	}

	public FieldSpecCollection getBottomSectionFieldSpecs() throws Exception
	{
		return getCurrentFormTemplate().getBottomFields();
	}

	public FieldSpecCollection getTopSectionFieldSpecs() throws Exception
	{
		return getCurrentFormTemplate().getTopFields();
	}

	public synchronized BulletinFolder createOrFindFolder(String name)
	{
		BulletinFolder result = findFolder(name);
		if(result != null)
			return result;
		return createFolder(name);
	}

	public void ensureBulletinIsInFolder(BulletinFolder folder, UniversalId uid) throws IOException, AddOlderVersionToFolderFailedException
	{
		try
		{
			addBulletinToFolder(folder, uid);
		}
		catch (BulletinAlreadyExistsException ignoreHarmless)
		{
		}
	}
	
	public synchronized void addBulletinToFolder(BulletinFolder folder, UniversalId uidToAdd) throws BulletinAlreadyExistsException, IOException, AddOlderVersionToFolderFailedException
	{
		Bulletin b = getBulletinRevision(uidToAdd);
		if(b == null)
			return;
		
		if(folder.isVisible() && !isLeaf(uidToAdd))
			throw new AddOlderVersionToFolderFailedException();
		
		folder.add(uidToAdd);

		String accountId = uidToAdd.getAccountId();
		Vector visibleFolders = getAllVisibleFolders();
		BulletinHistory history = b.getHistory();
		for(int i = 0; i < history.size(); ++i)
		{
			String localId = history.get(i);
			UniversalId uidToRemove = UniversalId.createFromAccountAndLocalId(accountId, localId);
			for(Iterator f = visibleFolders.iterator(); f.hasNext();)
			{
				BulletinFolder folderToFix = (BulletinFolder) f.next();
				if( folderToFix.contains(uidToRemove))
				{
					try
					{
						folderToFix.add(uidToAdd);
					}
					catch (BulletinAlreadyExistsException ignoreHarmless)
					{
					}
					removeBulletinFromFolder(folderToFix, uidToRemove);
				}
			}
		}
	}
	
	public void migrateFoldersForBulletinVersioning()
	{
		Vector allBulletinUids = getUidsOfAllBulletinRevisions();
		Vector visibleFolders = getAllVisibleFolders();
		for(Iterator i = allBulletinUids.iterator(); i.hasNext();)
		{
			UniversalId bId = (UniversalId) i.next();
			Bulletin b = getBulletinRevision(bId);
			if(b == null)
			{
				System.out.println("Migration Error: Unable to find bulletin: "+bId);
				continue;
			}
			if(!isLeaf(b.getUniversalId()))
			{
				for(Iterator f = visibleFolders.iterator(); f.hasNext();)
				{
					BulletinFolder folderToFix = (BulletinFolder) f.next();
					folderToFix.remove(bId);
				}
			}
		}
	}
	
	public synchronized void addRepairBulletinToFolders(UniversalId uId) throws BulletinAlreadyExistsException, IOException
	{
		Bulletin b = getBulletinRevision(uId);
		if(b == null)
			return;
			
		String name = getOrphanFolderName();
		BulletinFolder orphanFolder = createOrFindFolder(name);
		orphanFolder.prepareForBulkOperation();
		orphanFolder.add(b);
		
		BulletinFolder outboxFolder =  (b.isMutable())? getFolderDraftOutbox():getFolderSealedOutbox();
		if (outboxFolder != null)
			outboxFolder.add(b);
	}
	

	private void initializeFolders()
	{
		folders = new Vector();
		createSystemFolders();
	}
	
	public void setFormTemplate(String formTemplateTitle) throws Exception
	{
		formTemplateManager.setCurrentFormTemplate(formTemplateTitle);
	}
	
	public int quarantineUnreadableBulletins()
	{
		class Quarantiner implements Database.PacketVisitor
		{
			public Quarantiner(Database databaseToUse)
			{
				db = databaseToUse;
			}
			
			public void visit(DatabaseKey key)
			{
				InputStreamWithSeek in = null;
				try
				{
					in = db.openInputStream(key, getSignatureVerifier());
					Packet.validateXml(in, key.getAccountId(), key.getLocalId(), null, getSignatureVerifier());
					in.close();
				}
				
				catch(Exception e)
				{
					++quarantinedCount;
					if(in != null)
					{
						try { in.close(); } catch(Exception ignore) {}
					}
					try
					{
						db.moveRecordToQuarantine(key);
						revisionWasRemoved(key.getUniversalId());
					}
					catch (RecordHiddenException shouldNeverHappen)
					{
						MartusLogger.logException(shouldNeverHappen);
					}
					catch(Exception e2)
					{
						MartusLogger.logException(e2);
					}
				}
			}

			Database db;
			int quarantinedCount;
		}

		Quarantiner visitor = new Quarantiner(getWriteableDatabase());
		visitAllBulletinRevisions(visitor);
		return visitor.quarantinedCount;
	}

	public synchronized boolean isOrphan(UniversalId uid)
	{
		Vector allFolders= getVisibleFolderNames();
		for(int i = 0; i < allFolders.size(); ++i)
		{
			BulletinFolder folder = findFolder((String)allFolders.get(i));
			if(folder != null && folder.contains(uid))
				return false;
		}

		return true;
	}

	private synchronized BulletinFolder rawCreateFolder(String name)
	{
		if(findFolder(name) != null)
			return null;

		BulletinFolder folder = new BulletinFolder(this, name);
		folders.add(folder);
		return folder;
	}

	public synchronized String foldersToXml()
	{
		StringBuffer xml = new StringBuffer();
		xml.append(MartusClientXml.getFolderListTagStart());

		for(int index=0; index < getFolderCount(); ++index)
		{
			BulletinFolder folder = getFolder(index);
			xml.append(folderToXml(folder));
		}

		xml.append(MartusClientXml.getFolderListTagEnd());
		return new String(xml);
	}

	public synchronized String folderToXml(BulletinFolder folder)
	{
		StringBuffer xml = new StringBuffer();
		xml.append(MartusClientXml.getFolderTagStart(folder));
		Set unsortedBulletinList = folder.getAllUniversalIdsUnsorted();
		for(Iterator iter = unsortedBulletinList.iterator(); iter.hasNext();)
		{
			UniversalId uid = (UniversalId) iter.next();
			if(uid == null)
				System.out.println("WARNING: Unexpected null id");
			xml.append(MartusXml.getIdTag(uid.toString()));
		}
		xml.append(MartusClientXml.getFolderTagEnd());
		return new String(xml);
	}

	public synchronized void internalLoadFolders(String folderXml)
	{
		folders.clear();
		loadedLegacyFolders = false;
		XmlFolderListLoader loader = new XmlFolderListLoader(this);
		try
		{
			SimpleXmlParser.parse(loader, new StringReader(folderXml));
		}
		catch (Exception e)
		{
			// TODO Improve error handling!!!
			e.printStackTrace();
		}
		finally
		{
			createSystemFolders();
		}
	}
	
	class XmlFolderListLoader extends SimpleXmlDefaultLoader
	{
		public XmlFolderListLoader(ClientBulletinStore storeToFill)
		{
			super(MartusClientXml.tagFolderList);
			store = storeToFill;
		}
		
		public SimpleXmlDefaultLoader startElement(String tag)
			throws SAXParseException
		{
			if(tag.equals(MartusClientXml.tagFolder))
				return new XmlFolderLoader(tag, store);
			return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended)
			throws SAXParseException
		{
			if(tag.equals(MartusClientXml.tagFolder))
				;
			else
				super.endElement(tag, ended);
		}
		
		ClientBulletinStore store;
	}
	
	class XmlFolderLoader extends SimpleXmlDefaultLoader
	{
		public XmlFolderLoader(String tag, ClientBulletinStore storeToFill)
		{
			super(tag);
			store = storeToFill;
		}
		
		String convertLegacyFolder(String name)
		{
			if(name.equals("Outbox"))
				name = OBSOLETE_OUTBOX_FOLDER;
			else if(name.equals("Sent Bulletins"))
				name = SAVED_FOLDER;
			else if(name.equals("Draft Bulletins"))
				name = OBSOLETE_DRAFT_FOLDER;
			else if(name.equals("Discarded Bulletins"))
				name = DISCARDED_FOLDER;
			return name;
		}
		
		public void startDocument(Attributes attrs)
		{
			String name = attrs.getValue(MartusClientXml.attrFolderName);
			String convertedName = convertLegacyFolder(name);
			if(!convertedName.equals(name))
				store.setNeedsLegacyFolderConversion();
					
			folder = store.createOrFindFolder(convertedName);
			String closedStatus = attrs.getValue(MartusClientXml.attrFolderClosed);
			if(closedStatus != null && closedStatus.equals(Boolean.toString(true)))
				folder.setClosed();
		}

		public SimpleXmlDefaultLoader startElement(String tag)
			throws SAXParseException
		{
			if(tag.equals(MartusClientXml.tagId))
				return new SimpleXmlStringLoader(tag);
			return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended)
			throws SAXParseException
		{
			if(tag.equals(MartusClientXml.tagId))
			{
				String uidText = ((SimpleXmlStringLoader)ended).getText();
				try
				{
					UniversalId bulletinId = UniversalId.createFromString(uidText);
					folder.add(bulletinId);
				}
				catch (NotUniversalIdException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (BulletinAlreadyExistsException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
				super.endElement(tag, ended);
		}
		
		ClientBulletinStore store;
		BulletinFolder folder;
	}

	public static class BulletinAlreadyExistsException extends Exception 
	{
	}
	
	public static class AddOlderVersionToFolderFailedException extends Exception 
	{
	}

	public void importZipFileBulletin(File zipFile, BulletinFolder toFolder, boolean forceSameUids) throws
			Exception
	{
		ZipFile zip = new ZipFile(zipFile);
		try
		{
			BulletinHeaderPacket bhp = BulletinHeaderPacket.loadFromZipFile(zip, getSignatureVerifier());
			UniversalId uid = bhp.getUniversalId();

			boolean isSealed = Bulletin.isImmutable(bhp.getStatus());
			if(forceSameUids || !isMyBulletin(uid) || isSealed)
			{
				importZipFileToStoreWithSameUids(zipFile);
			}
			else
			{
				uid = importZipFileToStoreWithNewUids(zipFile);
			}

			if(!toFolder.contains(uid))
				addBulletinToFolder(toFolder, uid);
		}
		finally
		{
			zip.close();
		}

		saveFolders();
	}

	public UniversalId importZipFileToStoreWithNewUids(File inputFile) throws
		Exception
	{
		final MartusCrypto security = getSignatureGenerator();
		Bulletin imported = BulletinZipImporter.loadFromFileAsNewDraft(security, inputFile);
		saveBulletin(imported);
		return imported.getUniversalId();
	}
	
	void setNeedsLegacyFolderConversion()
	{
		loadedLegacyFolders = true;
	}
	
	public boolean needsLegacyFolderConversion()
	{
		return loadedLegacyFolders;
	}
	
	public Set getAllKnownFieldSpecs()
	{
		return knownFieldSpecCache.getAllKnownFieldSpecs();
	}
	
	public PoolOfReusableChoicesLists getAllReusableChoiceLists()
	{
		return knownFieldSpecCache.getAllReusableChoiceLists();
	}
	
	protected void loadCache()
	{
		//System.out.println("BulletinStore.loadCache");
		File cacheFile = getCacheFileForAccount(getStoreRootDir());
		if(!cacheFile.exists())
			return;
		
		byte[] sessionKeyCache = new byte[(int)cacheFile.length()];
		try
		{
			FileInputStream in = new FileInputStream(cacheFile);
			in.read(sessionKeyCache);
			in.close();
			getSignatureGenerator().setSessionKeyCache(sessionKeyCache);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			cacheFile.delete();
		}
	}

	protected void saveBulletinDataCache()
	{
		//System.out.println("BulletinStore.saveCache");
		try
		{
			byte[] sessionKeyCache = getSignatureGenerator().getSessionKeyCache();
			File cacheFile = new File(getStoreRootDir(), CACHE_FILE_NAME);
			FileOutputStream out = new FileOutputStream(cacheFile);
			out.write(sessionKeyCache);
			out.close();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PartialBulletinCache getCache()
	{
		return bulletinDataCache;
	}

	public static File getCacheFileForAccount(File accountDir)
	{
		return new File(accountDir, CACHE_FILE_NAME);
	}

	private File getFieldSpecCacheFile()
	{
		return getFieldSpecCacheFile(getStoreRootDir());
	}

	public static File getFieldSpecCacheFile(File accountDirectory)
	{
		return new File(accountDirectory, FIELD_SPEC_CACHE_FILE_NAME);
	}
	
	public boolean bulletinHasCurrentFieldSpecs(Bulletin b) throws Exception
	{
		return (b.getTopSectionFieldSpecs().equals(getTopSectionFieldSpecs()) &&
				b.getBottomSectionFieldSpecs().equals(getBottomSectionFieldSpecs()) );
	}

	public Bulletin createEmptyBulletin() throws Exception
	{
		return createEmptyBulletin(getTopSectionFieldSpecs(), getBottomSectionFieldSpecs());
	}
	
	public Bulletin createEmptyBulletin(Bulletin.BulletinType bulletinType) throws Exception
	{
		Bulletin b = new Bulletin(getSignatureGenerator(), bulletinType, getTopSectionFieldSpecs(), getBottomSectionFieldSpecs());
		return b;
	}

	public Bulletin createEmptyBulletin(FieldSpecCollection topSectionSpecs, FieldSpecCollection bottomSectionSpecs) throws Exception
	{
		Bulletin b = new Bulletin(getSignatureGenerator(), topSectionSpecs, bottomSectionSpecs);
		return b;
	}
	
	public Bulletin createNewDraftWithCurrentTemplateButIdAndDataAndHistoryFrom(Bulletin original) throws Exception
	{
		FieldSpecCollection topSpecs = getCurrentFormTemplate().getTopFields();
		FieldSpecCollection bottomSpecs = getCurrentFormTemplate().getBottomFields();
		Bulletin newDraft = createNewDraft(original, topSpecs, bottomSpecs);
		newDraft.setHistory(original.getHistory());

		BulletinHeaderPacket oldHeader = original.getBulletinHeaderPacket();
		BulletinHeaderPacket newHeader = newDraft.getBulletinHeaderPacket();
		newHeader.setUniversalId(oldHeader.getUniversalId());
		newHeader.setExtendedHistory(oldHeader.getExtendedHistory());
		newHeader.setAuthorizedToReadKeysPending(oldHeader.getAuthorizedToReadKeysPending());
		return newDraft;
	}

	public Bulletin createCloneWithTemplateAndDataFrom(Bulletin original) throws Exception
	{
		FieldSpecCollection publicFieldSpecsToUse = original.getTopSectionFieldSpecs();
		FieldSpecCollection privateFieldSpecsToUse = original.getBottomSectionFieldSpecs();
		return createNewDraft(original, publicFieldSpecsToUse, privateFieldSpecsToUse);
	}

	public Bulletin createNewDraft(Bulletin original, FieldSpecCollection topSectionFieldSpecsToUse, FieldSpecCollection bottomSectionFieldSpecsToUse) throws Exception 
	{
		Bulletin newDraftBulletin = createEmptyBulletin(topSectionFieldSpecsToUse, bottomSectionFieldSpecsToUse);
		newDraftBulletin.createDraftCopyOf(original, getDatabase());
		return newDraftBulletin;
	}
	
	public Vector getUidsOfAllBulletinRevisions()
	{
		class UidCollector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				uidList.add(key.getUniversalId());
			}
			Vector uidList = new Vector();
		}
	
		UidCollector uidCollector = new UidCollector();
		visitAllBulletinRevisions(uidCollector);
		return uidCollector.uidList;
	}
	
	private String[] getTagsOfCachedFields()
	{
		Vector tags = new Vector(Arrays.asList(BulletinTableModel.sortableFieldTags));
		tags.remove(Bulletin.PSEUDOFIELD_WAS_SENT);
		return (String[])tags.toArray(new String[0]);
	}

	public Property<String> getCurrentFormTemplateNameProperty()
	{
		return formTemplateManager.getCurrentFormTemplateNameProperty();
	}

	public ObservableSet<String> getAvailableTemplates()
	{
		return formTemplateManager.getAvailableTemplatesProperty();
	}

	public FormTemplate getFormTemplate(String title) throws Exception
	{
		return formTemplateManager.getTemplate(title);
	}

	public void saveNewFormTemplate(FormTemplate template) throws Exception
	{
		formTemplateManager.putTemplate(template);
	}

	public void selectFormTemplateAsDefault(String title) throws Exception
	{
		formTemplateManager.setCurrentFormTemplate(title);
	}

	public void deleteFormTemplate(String title) throws Exception
	{
		formTemplateManager.deleteTemplate(title);
	}

	public boolean doesFormTemplateExist(String newTitle) throws Exception
	{
		return formTemplateManager.getAvailableTemplateNames().contains(newTitle);
	}

	private void initializeFormTemplateManager() throws Exception
	{
		File templateDirectory = getTemplateDirectory();
		formTemplateManager = FormTemplateManager.createOrOpen(getSignatureGenerator(), templateDirectory);
	}

	private File getTemplateDirectory()
	{
		return new File(getStoreRootDir(), TEMPLATE_DIRECTORY_NAME);
	}
	
	public static final String SAVED_FOLDER = "%Sent";
	public static final String DISCARDED_FOLDER = "%Discarded";
	public static final String SEARCH_RESULTS_BULLETIN_FOLDER = "%SearchResults";
	public static final String RECOVERED_BULLETIN_FOLDER = "%RecoveredBulletins";
	public static final String RETRIEVE_SEALED_BULLETIN_FOLDER = "%RetrievedMyBulletin";
	public static final String RETRIEVE_SEALED_FIELD_OFFICE_BULLETIN_FOLDER = "%RetrievedFieldOfficeBulletin";
	public static final String RETRIEVE_DRAFT_BULLETIN_FOLDER = "%RetrievedMyBulletinDraft";
	public static final String RETRIEVE_DRAFT_FIELD_OFFICE_BULLETIN_FOLDER = "%RetrievedFieldOfficeBulletinDraft";
	public static final String RETRIEVED_FOLDER = "%RetrievedBulletins";
	public static final String DAMAGED_BULLETIN_FOLDER = "%DamagedBulletins";
	private static final String DRAFT_OUTBOX = "*DraftOutbox";
	private static final String SEALED_OUTBOX = "*SealedOutbox";
	private static final String IMPORT_FOLDER = "%Import";
	private static final String ON_SERVER_FOLDER = "*OnServer";
	private static final String NOT_ON_SERVER_FOLDER = "*NotOnServer";

	public static final String OBSOLETE_OUTBOX_FOLDER = "%OutBox";
	public static final String OBSOLETE_DRAFT_FOLDER = "%Draft";
	public static final String WAS_SENT_YES = "WasSentYes";
	public static final String WAS_SENT_NO = "WasSentNo";

	private static final String CACHE_FILE_NAME = "skcache.dat";
	private static final String OBSOLETE_CACHE_FILE_NAME = "sfcache.dat";
	private static final String FIELD_SPEC_CACHE_FILE_NAME = "fscache.dat";

	public static final String TEMPLATE_DIRECTORY_NAME = "templates";

	private Vector folders;
	private BulletinFolder folderSaved;
	private BulletinFolder folderDiscarded;
	private BulletinFolder folderDraftOutbox;
	private BulletinFolder folderSealedOutbox;
	private boolean loadedLegacyFolders;

	PartialBulletinCache bulletinDataCache;
	KnownFieldSpecCache knownFieldSpecCache;
	private FormTemplateManager formTemplateManager;
}
