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

package org.martus.common.database;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.packet.UniversalId;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


abstract public class MockDatabase extends Database
{
	public MockDatabase()
	{
		deleteAllData();
	}

	public int getOpenStreamCount()
	{
		return streamsThatAreOpen.size();
	}

	// Database interface
	public void initialize() throws FileVerificationException, MissingAccountMapException
	{}

	public void signAccountMap()
	{}

	synchronized public void deleteAllData()
	{
		sealedQuarantine = new TreeMap();
		draftQuarantine = new TreeMap();
		incomingInterimMap = new TreeMap();
		outgoingInterimMap = new TreeMap();
		mTimeMap = new TreeMap();
		accounts = new HashSet<String>();
	}

	public void writeRecord(DatabaseKey key, String record) 
			throws IOException, RecordHiddenException
	{
		if(key == null || record == null)
			throw new IOException("Null parameter");

		throwIfRecordIsHidden(key);

		addKeyToMaps(key, record.getBytes("UTF-8"));
		
	}

	private void addKeyToMaps(DatabaseKey key, byte[] data) throws UnsupportedEncodingException
	{
		addKeyToMap(key, data);
		mTimeMap.put(key.uid, new Long(System.currentTimeMillis()));
		addAccount(key.getAccountId());
	}

	public void importFiles(HashMap fileMapping) throws 
		IOException, RecordHiddenException
	{
		throwIfAnyRecordsHidden(fileMapping);

		Iterator keys = fileMapping.keySet().iterator();
		while(keys.hasNext())
		{
			DatabaseKey key = (DatabaseKey) keys.next();
			File file = (File) fileMapping.get(key);

			InputStream in = new FileInputStream(file.getAbsolutePath());
			writeRecord(key,in);
			mTimeMap.put(key.uid, new Long(file.lastModified()));
			in.close();
			file.delete();
		}
	}

	public int getRecordSize(DatabaseKey key) throws IOException, RecordHiddenException
	{
		throwIfRecordIsHidden(key);
		try
		{
			return readRawRecord(key).length;
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	public long getmTime(DatabaseKey key) throws IOException, RecordHiddenException
	{
		throwIfRecordIsHidden(key);
		try
		{
			DatabaseKey burKey = BulletinUploadRecord.getBurKey(key);
			if(mTimeMap.containsKey(burKey.uid))
				return ((Long)mTimeMap.get(burKey.uid)).longValue();
			return ((Long)mTimeMap.get(key.uid)).longValue();
		}
		catch (Exception e)
		{
			throw new IOException("not found");
		}
	}
	

	public void writeRecordEncrypted(DatabaseKey key, String record, MartusCrypto encrypter) throws
			IOException, RecordHiddenException
	{
		writeRecord(key, record);
	}

	//TODO try BufferedInputStream
	public void writeRecord(DatabaseKey key, InputStream record) 
		throws IOException, RecordHiddenException
	{
		if(key == null || record == null)
			throw new IOException("Null parameter");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int theByte = 0;
		while( (theByte = record.read()) >= 0)
			out.write(theByte);

		byte[] bytes = out.toByteArray();
		addKeyToMaps(key, bytes);
	}
	
	public String readRecord(DatabaseKey key, MartusCrypto decrypter)
	{
		byte[] bytes = readRawRecord(key, decrypter);
		if(bytes == null)
			return null;
		try
		{
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public InputStreamWithSeek openInputStream(DatabaseKey key, MartusCrypto decrypter)
	{
		if(isHidden(key))
			return null;

		byte[] bytes = readRawRecord(key, decrypter);
		if(bytes == null)
			return null;

		try
		{
			MockRecordInputStream in = new MockRecordInputStream(key, bytes, streamsThatAreOpen);
			return convertToDecryptingStreamIfNecessary(in, decrypter);
		}
		catch(Exception e)
		{
			System.out.println("MockDatabase.openInputStream: " + e);
			e.printStackTrace();
			return null;
		}
	}

	public byte[] readRawRecord(DatabaseKey key, MartusCrypto decrypter)
	{
		return readRawRecord(key);
	}

	public void discardRecord(DatabaseKey key)
	{
		internalDiscardRecord(key);
	}

	public boolean doesRecordExist(DatabaseKey key)
	{
		try
		{
			return (readRawRecord(key) != null);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public void visitAllRecords(PacketVisitor visitor)
	{
		Set keys = getAllKeys();
		Iterator iterator = keys.iterator();
		while(iterator.hasNext())
		{
			DatabaseKey key = (DatabaseKey)iterator.next();
			try
			{
				if(!isHidden(key))
					visitor.visit(key);
			}
			catch (RuntimeException nothingWeCanDoAboutIt)
			{
				// nothing we can do, so ignore it
			}
		}
	}

	public void visitAllAccounts(AccountVisitor visitor)
	{
		for (String accountId : accounts)
		{
			try
			{
				visitor.visit(accountId);
			}
			catch (RuntimeException nothingWeCanDoAboutIt)
			{
				// nothing we can do, so ignore it
			}
		}
	}

	public void visitAllRecordsForAccount(PacketVisitor visitor, String accountString)
	{
		class FilterByAccount implements PacketVisitor
		{
			FilterByAccount(PacketVisitor realVisitorToUse, String accountIdToVisit)
			{
				realVisitor = realVisitorToUse;
				accountId = accountIdToVisit;
			}

			public void visit(DatabaseKey key)
			{
				if(key.getAccountId().equals(accountId))
					realVisitor.visit(key);
			}

			String accountId;
			PacketVisitor realVisitor;
		}

		FilterByAccount filter = new FilterByAccount(visitor, accountString);
		visitAllRecords(filter);
	}
	
	public void scrubRecord(DatabaseKey key) 
			throws IOException, RecordHiddenException
	{
		byte[] bytes = readRawRecord(key);
		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = 0x55;			
		}
		writeRecord(key, new String(bytes, "UTF-8"));			
	}

	public String getFolderForAccount(String accountString)
	{
		UniversalId uid = UniversalId.createFromAccountAndLocalId(accountString, "");
		File file = getInterimFile(uid, incomingInterimMap);
		file.delete();
		return file.getPath();
	}

	public File getAbsoluteAccountAccessTokenFolderForAccount(String accountString) throws
	IOException
	{
		File tokensFolder = new File( getFolderForAccount(accountString), FileDatabase.ACCESS_TOKEN_FOLDER_NAME);
		return tokensFolder;
	}
	
	public File getAbsoluteFormTemplatesFolderForAccount(String accountString) throws
	IOException
	{
		File formTemplatesFolder = new File( getFolderForAccount(accountString), FileDatabase.FORM_TEMPLATES_FOLDER_NAME);
		return formTemplatesFolder;
	}

	public File getIncomingInterimFile(UniversalId uid) throws RecordHiddenException
	{
		throwIfRecordIsHidden(uid);
		File dir = getInterimFile(uid, incomingInterimMap);
		dir.deleteOnExit();
		dir.mkdirs();
		File file = new File(dir, "$$$in"+getClass().getSimpleName());
		file.deleteOnExit();
		return file;
	}

	public File getOutgoingInterimFile(UniversalId uid) throws RecordHiddenException
	{
		throwIfRecordIsHidden(uid);
		File dir = getInterimFile(uid, outgoingInterimMap);
		dir.deleteOnExit();
		dir.mkdirs();
		File file = new File(dir, "$$$out"+getClass().getSimpleName());
		file.deleteOnExit();
		File sigFile = MartusUtilities.getSignatureFileFromFile(file);
		sigFile.deleteOnExit();
		return file;
	}

	public File getOutgoingInterimPublicOnlyFile(UniversalId uid) throws RecordHiddenException
	{
		throwIfRecordIsHidden(uid);
		File dir = getInterimFile(uid, outgoingInterimMap);
		dir.deleteOnExit();
		dir.mkdirs();
		File file = new File(dir, "$$$public"+getClass().getSimpleName());
		file.deleteOnExit();
		File sigFile = MartusUtilities.getSignatureFileFromFile(file);
		sigFile.deleteOnExit();
		return file;
	}

	public File getContactInfoFile(String accountId)
	{
		File dir = new File(getFolderForAccount(accountId));
		dir.deleteOnExit();
		dir.mkdirs();
		File file = new File(dir, "$$$"+getClass().getSimpleName()+"ContactFile.dat");
		file.deleteOnExit();
		return file;
	}
	
	public File getAccountAccessTokenFile(String accountId, MartusAccountAccessToken token) throws IOException
	{
		File dir = getAbsoluteAccountAccessTokenFolderForAccount(accountId);
		dir.deleteOnExit();
		dir.mkdirs();
		File tokenFile = new File(dir, FileDatabase.buildTokenFilename(token));
		tokenFile.deleteOnExit();
		return tokenFile;
	}

	public synchronized boolean isInQuarantine(DatabaseKey key) throws RecordHiddenException
	{
		throwIfRecordIsHidden(key);
		Map quarantine = getQuarantineFor(key);
		return quarantine.containsKey(key);
	}

	public synchronized void moveRecordToQuarantine(DatabaseKey key) throws RecordHiddenException
	{
		throwIfRecordIsHidden(key);
		if(!doesRecordExist(key))
			return;

		byte[] bytes = readRawRecord(key);
		Map quarantine = getQuarantineFor(key);
		quarantine.put(key, bytes);
		discardRecord(key);
	}

	Map getQuarantineFor(DatabaseKey key)
	{
		Map map = sealedQuarantine;
		if(key.isMutable())
			map = draftQuarantine;
		return map;
	}

	public Set getAllKeys()
	{
		return internalGetAllKeys();
	}

	public int getRecordCount()
	{
		return getAllKeys().size();
	}

	// end Database interface

	private synchronized File getInterimFile(UniversalId uid, Map map)
	{
		if(map.containsKey(uid))
			return (File)map.get(uid);

		try
		{
			File interimFile = File.createTempFile("$$$MockDbInterim_"+getClass().getSimpleName(), null);
			interimFile.deleteOnExit();
			interimFile.delete();
			map.put(uid, interimFile);
			addAccount(uid.getAccountId());
			return interimFile;
		}
		catch (IOException e)
		{
			return null;
		}
	}
	
	public void addAccount(String accountId)
	{
		accounts.add(accountId);
	}

	abstract byte[] readRawRecord(DatabaseKey key);
	abstract void addKeyToMap(DatabaseKey key, byte[] record);
	abstract Map getPacketMapFor(DatabaseKey key);
	abstract Set internalGetAllKeys();
	abstract void internalDiscardRecord(DatabaseKey key);

	Map sealedQuarantine;
	Map draftQuarantine;
	Map incomingInterimMap;
	Map outgoingInterimMap;

	HashMap streamsThatAreOpen = new HashMap();
	
	private HashSet<String> accounts;
}

class MockRecordInputStream extends ByteArrayInputStreamWithSeek
{
	MockRecordInputStream(DatabaseKey key, byte[] inputBytes, Map observer)
	{
		super(inputBytes);
		streamsThatAreOpen = observer;
	}

	public synchronized void addAsOpen(DatabaseKey key)
	{
		streamsThatAreOpen.put(this, key);
	}

	public synchronized void close()
	{
		streamsThatAreOpen.remove(this);
	}

	Map streamsThatAreOpen;
}

