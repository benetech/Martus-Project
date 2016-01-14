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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.packet.UniversalId;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


abstract public class Database extends ReadableDatabase
{
	public static class RecordHiddenException extends Exception 
	{
	}

	protected Database()
	{
		final int expectedMaxEntries = 100;
		hiddenPacketUids = Collections.synchronizedSet(new HashSet(expectedMaxEntries));
	}
	
	abstract public void deleteAllData() throws Exception;
	abstract public void initialize() throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException;
	abstract public void writeRecord(DatabaseKey key, String record) throws IOException, RecordHiddenException;
	abstract public void writeRecord(DatabaseKey key, InputStream record) throws IOException, RecordHiddenException;
	abstract public void writeRecordEncrypted(DatabaseKey key, String record, MartusCrypto encrypter) throws IOException, RecordHiddenException, MartusCrypto.CryptoException;
	abstract public void importFiles(HashMap entries) throws IOException, RecordHiddenException;
	abstract public void discardRecord(DatabaseKey key);
	abstract public File getInterimDirectory(String accountId) throws IOException;
	abstract public File getIncomingInterimFile(UniversalId uid) throws IOException, RecordHiddenException;
	abstract public File getOutgoingInterimFile(UniversalId uid) throws IOException, RecordHiddenException;
	abstract public File getOutgoingInterimPublicOnlyFile(UniversalId uid) throws IOException, RecordHiddenException;

	abstract public File getContactInfoFile(String accountId) throws IOException;
	abstract public File getAbsoluteAccountAccessTokenFolderForAccount(String accountId) throws IOException;
	abstract public File getAccountAccessTokenFile(String accountId, MartusAccountAccessToken token) throws IOException;

	abstract public File getAbsoluteFormTemplatesFolderForAccount(String accountId) throws IOException;

	abstract public void moveRecordToQuarantine(DatabaseKey key) throws RecordHiddenException;

	abstract public void signAccountMap() throws IOException, MartusCrypto.MartusSignatureException;
	abstract public void scrubRecord(DatabaseKey key) throws IOException, RecordHiddenException;

	public void setmTime(DatabaseKey key, Long mTime)
	{
		mTimeMap.put(key, mTime);
	}

	public boolean isHidden(UniversalId uid)
	{
		return hiddenPacketUids.contains(uid);
	}
	
	public boolean isHidden(DatabaseKey key)
	{
		return isHidden(key.getUniversalId());
	}

	public void hide(UniversalId uid)
	{
		hiddenPacketUids.add(uid);
	}
	
	public File createTempFile(MartusCrypto account) throws IOException
	{
		return File.createTempFile("$$$Martus", null, getInterimDirectory(account.getPublicKeyString()));
	}
	
	boolean isEncryptedRecordStream(InputStreamWithSeek in) throws
			IOException
	{
		int flagByte = in.read();
		in.seek(0);
		boolean isEncrypted = false;
		if(flagByte == 0)
			isEncrypted = true;
		return isEncrypted;
	}

	InputStreamWithSeek convertToDecryptingStreamIfNecessary(
		InputStreamWithSeek in,
		MartusCrypto decrypter)
		throws IOException, NoKeyPairException, DecryptionException
	{
		if(!isEncryptedRecordStream(in))
			return in;

		in.read(); //throwAwayFlagByte
		ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
		decrypter.decrypt(in, decryptedOut);
		in.close();

		byte[] bytes = decryptedOut.toByteArray();
		return new ByteArrayInputStreamWithSeek(bytes);
	}

	protected void throwIfAnyRecordsHidden(HashMap fileMapping) throws RecordHiddenException
	{
		Iterator lookForHiddenKeys = fileMapping.keySet().iterator();
		while(lookForHiddenKeys.hasNext())
		{
			DatabaseKey key = (DatabaseKey) lookForHiddenKeys.next();
			if(isHidden(key))
				throw new RecordHiddenException();
		}
	}

	protected void throwIfRecordIsHidden(DatabaseKey key) throws RecordHiddenException
	{
		throwIfRecordIsHidden(key.getUniversalId());
	}
	
	protected void throwIfRecordIsHidden(UniversalId uid) throws RecordHiddenException
	{
		if(isHidden(uid))
			throw new RecordHiddenException();
	}

	Set hiddenPacketUids;
	protected Map mTimeMap;
	
}
