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

package org.martus.common.database;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;

import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.common.utilities.MartusServerUtilities.MartusSignatureFileDoesntExistsException;
import org.martus.util.FileOutputStreamViaTemp;

public class ServerFileDatabase extends FileDatabase 
{
	public ServerFileDatabase(File directory, MartusCrypto security)
	{
		super(directory, security);
	}

	protected String getBucketPrefix(DatabaseKey key) 
	{
		if(key.isMutable())
			return mutableBucketPrefix;
		return defaultBucketPrefix;
	}

	private boolean isMutablePacketBucket(String folderName)
	{
		return folderName.startsWith(mutableBucketPrefix);
	}

	public synchronized void loadAccountMap() throws FileVerificationException, MissingAccountMapSignatureException
	{
		super.loadAccountMap();
		if(accountMapFile.exists())
		{
			try
			{
				MartusServerUtilities.verifyFileAndLatestSignatureOnServer(accountMapFile, security);
			}
			catch (IOException e)
			{
				throw new FileVerificationException();
			}
			catch (ParseException e)
			{
				throw new FileVerificationException();
			}
			catch (MartusSignatureFileDoesntExistsException e)
			{
				throw new MissingAccountMapSignatureException();
			}
		}
	}
	
	public void verifyAccountMap() throws MartusUtilities.FileVerificationException, MissingAccountMapSignatureException
	{
		File sigFile;
		try 
		{
			sigFile = MartusServerUtilities.getLatestSignatureFileFromFile(accountMapFile);
		} 
		catch (Exception e) 
		{
			throw new MissingAccountMapSignatureException();
		} 
		MartusServerUtilities.verifyFileAndSignatureOnServer(accountMapFile, sigFile, security, security.getPublicKeyString());
	}

	public void signAccountMap() throws IOException, MartusCrypto.MartusSignatureException
	{
		try
		{
			MartusServerUtilities.createSignatureFileFromFileOnServer(accountMapFile, security);
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			throw new MartusSignatureException();
		}
	}
	
	public void deleteSignaturesForFile(File origFile)
	{
		MartusServerUtilities.deleteSignaturesForFile(origFile);
	}
	
	public String getTimeStamp(DatabaseKey key) throws IOException, TooManyAccountsException
	{
		File file = getFileForRecord(key);
		long lastModifiedMillisSince1970 = file.lastModified();
		return MartusServerUtilities.getFormattedTimeStamp(lastModifiedMillisSince1970);
	}
	
	protected OutputStream createOutputStream(File file, File tempDirectory) throws IOException
	{
		return new FileOutputStreamViaTemp(file, tempDirectory);
	}
	
	protected DatabaseKey getDatabaseKey(File accountDir, String bucketName, UniversalId uid)
	{
		DatabaseKey key = null;
		if(isMutablePacketBucket(bucketName))
			key = DatabaseKey.createMutableKey(uid);
		else
			key = DatabaseKey.createImmutableKey(uid);
		return key;
	}
}
