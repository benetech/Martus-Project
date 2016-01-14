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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.StreamEncryptor;
import org.martus.common.packet.UniversalId;
import org.martus.util.DirectoryUtils;
import org.martus.util.ScrubFile;
import org.martus.util.StreamCopier;
import org.martus.util.StreamFilter;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;



abstract public class FileDatabase extends Database
{
	abstract public void verifyAccountMap() throws MartusUtilities.FileVerificationException, MissingAccountMapSignatureException;

	public FileDatabase(File directory, MartusCrypto securityToUse)
	{
		security = securityToUse;
		absoluteBaseDir = directory;
		accountMapFile = new File(absoluteBaseDir, ACCOUNTMAP_FILENAME);
		accountMapSignatureFile = MartusUtilities.getSignatureFileFromFile(accountMapFile);
	}


	public static class MissingAccountMapException extends Exception 
	{
	}

	public static class MissingAccountMapSignatureException extends Exception 
	{
	}

	// Database interface
	public void deleteAllData() throws Exception
	{
		DirectoryUtils.deleteEntireDirectoryTree(absoluteBaseDir);
		mTimeMap.clear();
		loadAccountMap();
	}
	
	public void deleteSignaturesForFile(File origFile)
	{
		File signature = MartusUtilities.getSignatureFileFromFile(origFile);
		if(signature.exists())
		{
			signature.delete();
		}
	}

	public void initialize() throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		accountMap = Collections.synchronizedMap(new TreeMap());
		mTimeMap = Collections.synchronizedMap(new HashMap());
		loadAccountMap();
		if(isAccountMapExpected(absoluteBaseDir) && !accountMapFile.exists())
		{
			throw new MissingAccountMapException();
		}
	}

	public static boolean isAccountMapExpected(File baseDirectory)
	{
		if(!baseDirectory.exists())
			return false;
			
		File files[] = baseDirectory.listFiles();
		for (int i = 0; i < files.length; i++) 
		{
			File thisFile = files[i];
			if(thisFile.isDirectory() && thisFile.getName().startsWith("a"))
				return true; 
		}

		return false;
	}

	public void writeRecord(DatabaseKey key, String record) 
			throws IOException, RecordHiddenException
	{
		writeRecord(key, new StringInputStreamWithSeek(record));
	}

	public int getRecordSize(DatabaseKey key) 
		throws IOException, RecordHiddenException
	{
		UniversalId uid = key.getUniversalId();
		throwIfRecordIsHidden(uid);

		try
		{
			return (int)getExistingFileForRecord(uid).length();
		}
		catch (FileNotFoundException e)
		{
			return 0;
		}
		catch (TooManyAccountsException e)
		{
			System.out.println("FileDatabase:getRecordSize" + e);
		}
		return 0;
	}
	
	public long getPacketTimestamp(DatabaseKey key) throws IOException, RecordHiddenException
	{
		File file = getFileForRecord(key);
		return file.lastModified();
	}

	public long getmTime(DatabaseKey key) throws IOException, RecordHiddenException
	{
	    if(mTimeMap.containsKey(key))
	    	return ((Long)mTimeMap.get(key)).longValue();
		throwIfRecordIsHidden(key);
	
		try
		{
			long mTime =  getUploadTime(key);
			mTimeMap.put(key, new Long(mTime));
			return mTime;
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
	}

	private long getUploadTime(DatabaseKey key) throws IOException, CryptoException, ParseException, Exception
	{
		DatabaseKey burKey = BulletinUploadRecord.getBurKey(key);
		DatabaseKey delKey = DeleteRequestRecord.getDelKey(key.getUniversalId());
		if(doesRecordExist(burKey))
			return BulletinUploadRecord.getTimeStamp(this, key, security);
		else if(doesRecordExist(delKey))
			return (new DeleteRequestRecord(this,key.getUniversalId(), security).getmTime());
		else
			throw new Exception("ServerFileDatabase.getmTime: No Bur or Del Packet: " + MartusCrypto.formatAccountIdForLog(key.getAccountId()) + " " + key.getLocalId());
	}

	
	public void importFiles(HashMap fileMapping) 
			throws IOException, RecordHiddenException
	{
		throwIfAnyRecordsHidden(fileMapping);

		Iterator keys = fileMapping.keySet().iterator();
		while(keys.hasNext())
		{
			DatabaseKey key = (DatabaseKey) keys.next();
			mTimeMap.remove(key);
			File fromFile = (File) fileMapping.get(key);
			File toFile = getFileForRecord(key);
			toFile.delete();
			if(!fromFile.renameTo(toFile))
				throw new IOException("renameTo failed: " + fromFile + " -> " + toFile);
			if(!toFile.exists())
				throw new IOException("renameTo didn't work: " + toFile);
		}
	}

	public void writeRecordEncrypted(DatabaseKey key, String record, MartusCrypto encrypter) throws
			IOException,
			RecordHiddenException, 			
			MartusCrypto.CryptoException
	{
		if(encrypter == null)
			throw new IOException("Null encrypter");

		InputStream in = new StringInputStreamWithSeek(record);
		writeRecordUsingCopier(key, in, new StreamEncryptor(encrypter));
	}

	public void writeRecord(DatabaseKey key, InputStream in) 
			throws IOException, RecordHiddenException
	{
		writeRecordUsingCopier(key, in, new StreamCopier());
	}

	public String readRecord(DatabaseKey key, MartusCrypto decrypter) throws
			IOException,
			MartusCrypto.CryptoException
	{
		InputStreamWithSeek in = openInputStream(key, decrypter);
		if(in == null)
			return null;

		try
		{
			byte[] bytes = new byte[in.available()];
			in.read(bytes);
			in.close();
			return new String(bytes, "UTF-8");
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public InputStreamWithSeek openInputStream(DatabaseKey key, MartusCrypto decrypter) throws
			IOException,
			MartusCrypto.CryptoException
	{
		if(isHidden(key))
			return null;

		try
		{
			File file = getFileForRecord(key);
			InputStreamWithSeek in = new FileInputStreamWithSeek(file);

			return convertToDecryptingStreamIfNecessary(in, decrypter);
		}
		catch(TooManyAccountsException e)
		{
			System.out.println("FileDatabase.openInputStream: " + e);
		}
		catch(IOException e)
		{
			//System.out.println("FileDatabase.openInputStream: " + e);
		}

		return null;
	}

	public void discardRecord(DatabaseKey key)
	{
		try
		{
			File file = getFileForRecord(key);
			mTimeMap.remove(key);
			file.delete();
			if(file.exists())
				throw new IOException("delete failed: " + file);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("FileDatabase.discardRecord: " + e);
		}
	}

	public boolean doesRecordExist(DatabaseKey key)
	{
		if(isHidden(key))
			return false;

		try
		{
			File file = getFileForRecord(key);
			return file.exists();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public void visitAllRecords(PacketVisitor visitor)
	{
		class AccountVisitorVisitor implements AccountVisitor
		{
			AccountVisitorVisitor(PacketVisitor visitorToUse)
			{
				packetVisitor = visitorToUse;
			}

			public void visit(String accountString)
			{
				visitAllRecordsForAccount(packetVisitor, accountString);
			}
			PacketVisitor packetVisitor;
		}

		AccountVisitorVisitor accountVisitor = new AccountVisitorVisitor(visitor);
		visitAllAccounts(accountVisitor);
	}

	public String getFolderForAccount(String accountString) throws IOException
	{
		try
		{
			File dir = getAccountDirectory(accountString);
			return convertToRelativePath(dir.getPath());
		}
		catch(Exception e)
		{
			System.out.println("FileDatabase:getFolderForAccount clientId=" + accountString);
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public File getInterimDirectory(String accountString) throws
		IOException
	{
		File accountFolder = new File(absoluteBaseDir, getFolderForAccount(accountString));
		File interimFolder = new File(accountFolder, INTERIM_FOLDER_NAME);
		interimFolder.mkdirs();
		return interimFolder;
	}

	public File getAbsoluteContactInfoFolderForAccount(String accountString) throws
		IOException
	{
		File accountFolder = new File(absoluteBaseDir, getFolderForAccount(accountString));
		File ContactFolder = new File(accountFolder, CONTACTINFO_FOLDER_NAME);
		return ContactFolder;
	}

	public File getAbsoluteAccountAccessTokenFolderForAccount(String accountString) throws
	IOException
	{
		File accountFolder = new File(absoluteBaseDir, getFolderForAccount(accountString));
		File tokensFolder = new File(accountFolder, ACCESS_TOKEN_FOLDER_NAME);
		return tokensFolder;
	}	
	
	public File getAbsoluteFormTemplatesFolderForAccount(String accountString) throws
	IOException
	{
		File accountFolder = getAbsoluteAccountDirectory(accountString);
		File formTemplatesFolder = new File(accountFolder, FORM_TEMPLATES_FOLDER_NAME);
		return formTemplatesFolder;
	}

	public File getIncomingInterimFile(UniversalId uid) throws
		IOException, RecordHiddenException
	{
		return createInterimFile(uid, "in");
	}

	public File getOutgoingInterimFile(UniversalId uid) throws
		IOException, RecordHiddenException
	{
		return createInterimFile(uid, "out");
	}

	public File getOutgoingInterimPublicOnlyFile(UniversalId uid) throws
		IOException, RecordHiddenException
	{
		return createInterimFile(uid, "public");
	}

	private File createInterimFile(UniversalId uid, String extension) throws RecordHiddenException, IOException
	{
		throwIfRecordIsHidden(uid);
		File folder = getInterimDirectory(uid.getAccountId());
		return new File(folder, uid.getLocalId()+"."+extension);
	}


	public File getContactInfoFile(String accountId) throws
		IOException
	{
		File folder = getAbsoluteContactInfoFolderForAccount(accountId);
		return new File(folder, "contactInfo.dat");
	}
	
	public File getAccountAccessTokenFile(String accountId, MartusAccountAccessToken token) throws
	IOException
	{
		File folder = getAbsoluteAccountAccessTokenFolderForAccount(accountId);
		return new File(folder, buildTokenFilename(token));
	}

	public static String buildTokenFilename(MartusAccountAccessToken token)
	{
		String tokenString = token.getToken();
		return buildTokenFilename(tokenString);
	}

	public static String buildTokenFilename(String tokenString)
	{
		return "token-" + tokenString + ".dat";
	}
	
	public boolean isInQuarantine(DatabaseKey key) throws RecordHiddenException
	{
		throwIfRecordIsHidden(key.getUniversalId());
		try
		{
			return getQuarantineFileForRecord(key).exists();
		}
		catch(Exception nothingWeCanDoAboutIt)
		{
			System.out.println("FileDatabase.isInQuarantine: " + nothingWeCanDoAboutIt);
			return false;
		}
	}

	public void moveRecordToQuarantine(DatabaseKey key) throws RecordHiddenException
	{
		throwIfRecordIsHidden(key.getUniversalId());
		try
		{
			File moveFrom = getFileForRecord(key);
			if(!moveFrom.exists())
				return;
			
			File moveTo = getQuarantineFileForRecord(key);

			moveTo.mkdirs();
			moveTo.delete();
			if(!moveFrom.renameTo(moveTo))
				throw new IOException("Unable to rename from " + moveFrom.getAbsolutePath() + " to " + moveTo);
		}
		catch(Exception nothingWeCanDoAboutIt)
		{
			nothingWeCanDoAboutIt.printStackTrace();
			System.out.println("FileDatabase.moveRecordToQuarantine: " + nothingWeCanDoAboutIt);
		}
	}

	// end Database interface

	public synchronized void visitAllAccounts(AccountVisitor visitor)
	{
		Set accounts = getAccountMap().keySet();
		Iterator iterator = accounts.iterator();
		while(iterator.hasNext())
		{
			String accountString = (String)iterator.next();
			try
			{
				visitor.visit(accountString);
			}
			catch (RuntimeException nothingWeCanDoAboutIt)
			{
				// TODO: nothing we can do, so ignore it
			}
		}
	}

	public void visitAllRecordsForAccount(PacketVisitor visitor, String accountString)
	{
		File accountDir = null;
		try
		{
			accountDir = getAccountDirectory(accountString);
		}
		catch(Exception e)
		{
			System.out.println("FileDatabase.visitAllPacketsForAccount: " + e);
			return;
		}

		String[] packetBuckets = accountDir.list();
		if(packetBuckets != null)
		{
			for(int packetBucket = 0; packetBucket < packetBuckets.length; ++packetBucket)
			{
				String bucketName = packetBuckets[packetBucket];
				File bucketDir = new File(accountDir, bucketName);
				if(INTERIM_FOLDER_NAME.equals(bucketDir.getName()))
					continue;
				if(CONTACTINFO_FOLDER_NAME.equals(bucketDir.getName()))
					continue;
				if(isQuarantineBucketDirectory(bucketDir))
					continue;

				String[] files = bucketDir.list();
				if(files != null)
				{
					for(int i=0; i < files.length; ++i)
					{
						UniversalId uid = UniversalId.createFromAccountAndLocalId(accountString, files[i]);
						if(isHidden(uid))
							continue;
						String localId = uid.getLocalId();
						if(localId.startsWith(BUR_PREFIX))
							continue;
						if(localId.startsWith(DEL_PREFIX))
							continue;
							
						try
						{
							visitor.visit(getDatabaseKey(accountDir, bucketName, uid));
						}
						catch (RuntimeException nothingWeCanDoAboutIt)
						{
							// nothing we can do, so ignore it
						}
					}
				}
			}
		}
	}

	protected abstract DatabaseKey getDatabaseKey(File accountDir, String bucketName, UniversalId uid);

	public void scrubRecord(DatabaseKey key) 
			throws IOException, RecordHiddenException
	{
		File file = getFileForRecord(key);
		ScrubFile.scrub(file);		
	}


	boolean isQuarantineBucketDirectory(File bucketDir)
	{
		if(bucketDir.getName().startsWith(draftQuarantinePrefix))
			return true;
		if(bucketDir.getName().startsWith(sealedQuarantinePrefix))
			return true;

		return false;
	}

	public File getAbsoluteAccountDirectory(String accountString) throws FileNotFoundException
	{
		String accountDirectoryName = (String)getAccountMap().get(accountString);
		if(accountDirectoryName == null)
			throw new FileNotFoundException("Account does not exist");
		return new File(absoluteBaseDir, accountDirectoryName);
	}
	
	private File getExistingFileForRecord(UniversalId uid) throws IOException, TooManyAccountsException
	{
		File sealed = getFileForRecordWithPrefix(uid, defaultBucketPrefix);
		if(sealed.exists())
			return sealed;
		
		File draft = getFileForRecordWithPrefix(uid, mutableBucketPrefix);
		if(draft.exists())
			return draft;
		
		throw new FileNotFoundException();
	}

	public File getFileForRecord(DatabaseKey key) throws IOException, TooManyAccountsException
	{
		File result = getFileForRecordWithPrefix(key.getUniversalId(), getBucketPrefix(key));
		result.getParentFile().mkdirs();
		return result;
	}

	private File getQuarantineFileForRecord(DatabaseKey key)
		throws IOException, TooManyAccountsException
	{
		File result = getFileForRecordWithPrefix(key.getUniversalId(), getQuarantinePrefix(key));
		result.getParentFile().mkdirs();
		return result;
	}
	
	public File getFileForRecordWithPrefix(UniversalId uid, String bucketPrefix)
		throws IOException, TooManyAccountsException
	{
		String localId = uid.getLocalId();
		String bucketBaseName = getBaseBucketName(localId);
		String bucketName = bucketPrefix + bucketBaseName;
		String accountString = uid.getAccountId();
		File path = new File(getAccountDirectory(accountString), bucketName);
		File result = new File(path, localId); 
		return result;
	}

	public static String getBaseBucketName(String localId) 
	{
		int hashValue = getHashValue(localId) & 0xFF;
		return Integer.toHexString(0xb00 + hashValue);
	}

	private String getQuarantinePrefix(DatabaseKey key)
	{
		if(key.isMutable())
			return draftQuarantinePrefix;
		return sealedQuarantinePrefix;
	}

	public class TooManyAccountsException extends IOException 
	{
	}

	public File getAccountDirectory(String accountString) throws IOException, TooManyAccountsException
	{
		String accountDir = (String)getAccountMap().get(accountString);
		if(accountDir == null)
			return generateAccount(accountString);
		return new File(absoluteBaseDir, accountDir);
	}

	synchronized File generateAccount(String accountString)
		throws IOException, TooManyAccountsException
	{
		int hashValue = getHashValue(accountString) & 0xFF;
		String bucketName = "/a" + Integer.toHexString(0xb00 + hashValue);
		File bucketDir = new File(absoluteBaseDir, bucketName);
		int countInBucket = 0;
		String[] existingAccounts = bucketDir.list();
		if(existingAccounts != null)
			countInBucket = existingAccounts.length;
		int tryValue = countInBucket;
		for(int index = 0; index < 100000000;++index)
		{
			String tryName = Integer.toHexString(0xa0000000 + tryValue);
			File accountDir = new File(bucketDir, tryName);
			if(!accountDir.exists())
			{
				accountDir.mkdirs();
				String relativeDirString = convertToRelativePath(accountDir.getPath());
				getAccountMap().put(accountString, relativeDirString);
				appendAccountToMapFile(accountString, relativeDirString);
				return accountDir;
			}
		}
		throw new TooManyAccountsException();
	}

	public void appendAccountToMapFile(String accountString, String accountDir) throws IOException
	{
		FileOutputStream out = new FileOutputStream(accountMapFile.getPath(), true);
		UnicodeWriter writer = new UnicodeWriter(out);
		try
		{
			writer.writeln(accountDir + "=" + accountString);
		}
		finally
		{
			writer.flush();
			out.flush();
			out.getFD().sync();
			writer.close();
			out.close();

			try
			{
				signAccountMap();
			}
			catch (MartusSignatureException e)
			{
				MartusLogger.logException(e);
				throw new IOException();
			}
		}
	}

	synchronized public void loadAccountMap() throws FileVerificationException, MissingAccountMapSignatureException
	{
		accountMap.clear();
		if(!accountMapFile.exists())
			return;
		try
		{
			verifyAccountMap();
			UnicodeReader reader = new UnicodeReader(accountMapFile);
			String entry = null;
			while( (entry = reader.readLine()) != null)
			{
				addParsedAccountEntry(accountMap, entry);
			}
			reader.close();
		}
		catch(FileNotFoundException e)
		{
			// not a problem--just use the empty map
		}
		catch(IOException e)
		{
			System.out.println("FileDatabase.loadMap: " + e);
			return;
		}
	}

	public void addParsedAccountEntry(Map m, String entry)
	{
		if(entry.startsWith("#"))
			return;

		int splitAt = entry.indexOf("=");
		if(splitAt <= 0)
			return;

		String accountString = entry.substring(splitAt+1);
		String accountDir = entry.substring(0,splitAt);
		if(startsWithAbsolutePath(accountDir))
			accountDir = convertToRelativePath(accountDir);

		if(m.containsKey(accountString))
		{
			System.out.println("WARNING: Duplicate entries in account map: ");
			System.out.println(" " + accountDir + " and " + m.get(accountString));
		}
		m.put(accountString, accountDir);
	}

	boolean startsWithAbsolutePath(String accountDir)
	{
		return accountDir.startsWith(File.separator) || accountDir.startsWith(":",1);
	}

	public String convertToRelativePath(String absoluteAccountPath)
	{
		File dir = new File(absoluteAccountPath);
		File bucket = dir.getParentFile();
		return bucket.getName() + File.separator + dir.getName();
	}

	void deleteAllPacketsForAccount(File accountDir)
	{
		class PacketDeleter implements PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				discardRecord(key);
			}
		}

		PacketDeleter deleter = new PacketDeleter();
		visitAllRecordsForAccount(deleter, getAccountString(accountDir));

		accountDir.delete();
	}

	public String getAccountString(File accountDir)
	{
		try
		{
			Set accountStrings = getAccountMap().keySet();
			Iterator iterator = accountStrings.iterator();
			while(iterator.hasNext())
			{
				String accountString = (String)iterator.next();
				if(getAccountDirectory(accountString).equals(accountDir))
					return accountString;
			}
		}
		catch(Exception e)
		{
			System.out.println("FileDatabase.getAccountString: " + e);
		}
		return null;
	}

	private synchronized void writeRecordUsingCopier(DatabaseKey key, InputStream in, StreamFilter copier)
		throws IOException, RecordHiddenException
	{
		if(key == null)
			throw new IOException("Null key");

		throwIfRecordIsHidden(key.getUniversalId());

		try
		{
			File file = getFileForRecord(key);
			OutputStream rawOut = createOutputStream(file, getInterimDirectory(key.getAccountId()));
			MartusUtilities.copyStreamWithFilter(in, rawOut, copier);
			mTimeMap.remove(key);
		}
		catch(TooManyAccountsException e)
		{
			// TODO: Make sure this case is tested!
			System.out.println("FileDatabase.writeRecord1b: " + e);
			throw new IOException("Too many accounts");
		}
	}
	protected OutputStream createOutputStream(File file, File tempDirectory)
		throws IOException
	{
		return new FileOutputStream(file);
	}
	
	public static int getHashValue(String inputString)
	{
		//Linux Elf hashing algorithm
		int result = 0;
		for(int i = 0; i < inputString.length(); ++i)
		{
			char c = inputString.charAt(i);
			result = (result << 4) + c;
			int x = result & 0xF0000000;
			if(x != 0)
				result ^= (x >> 24);
			result &= ~x;
		}
		return result;
	}

	protected abstract String getBucketPrefix(DatabaseKey key);

	public void signAccountMap() throws IOException, MartusCrypto.MartusSignatureException
	{
		accountMapSignatureFile = MartusUtilities.createSignatureFileFromFile(accountMapFile, security);
	}

	public File getAccountMapFile()
	{
		return accountMapFile;
	}
	
	protected synchronized Map getAccountMap()
	{
		return accountMap;
	}

	protected static final String defaultBucketPrefix = "p";
	protected static final String mutableBucketPrefix = "d" + defaultBucketPrefix;
	protected static final String sealedQuarantinePrefix = "qs-p";
	protected static final String draftQuarantinePrefix = "qd-p";
	protected static final String INTERIM_FOLDER_NAME = "interim";
	protected static final String CONTACTINFO_FOLDER_NAME = "contactInfo";
	protected static final String ACCESS_TOKEN_FOLDER_NAME = "accessTokens";
	protected static final String FORM_TEMPLATES_FOLDER_NAME = "formTemplates";
	protected static final String ACCOUNTMAP_FILENAME = "acctmap.txt";
	public static final String BUR_PREFIX = "BUR-";
	public static final String DEL_PREFIX = "DEL-";

	public MartusCrypto security;

	public File absoluteBaseDir;
	private Map accountMap;
	public File accountMapFile;
	public File accountMapSignatureFile;
}
