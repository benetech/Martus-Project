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

package org.martus.server.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.martus.common.ContactInfo;
import org.martus.common.LoggerInterface;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusAccountAccessToken.TokenInvalidException;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.database.BulletinUploadRecord;
import org.martus.common.database.Database;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.DeleteRequestRecord;
import org.martus.common.fieldspec.CustomFieldTemplate;
import org.martus.common.fieldspec.CustomFieldTemplate.FutureVersionException;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.common.utilities.MartusServerUtilities.MartusSignatureFileAlreadyExistsException;
import org.martus.common.utilities.MartusServerUtilities.MartusSignatureFileDoesntExistsException;


public class ServerBulletinStore extends BulletinStore
{
	public void fillHistoryAndHqCache()
	{
		getHistoryAndHqCache().fillCache();
	}

	public void deleteBulletinRevision(DatabaseKey keyToDelete)
			throws IOException, CryptoException, InvalidPacketException,
			WrongPacketTypeException, SignatureVerificationException,
			DecryptionException, UnsupportedEncodingException,
			NoKeyPairException
	{
		super.deleteBulletinRevision(keyToDelete);
		DatabaseKey burKey = BulletinUploadRecord.getBurKey(keyToDelete);
		deleteSpecificPacket(burKey);			
	}

	public File getIncomingInterimFile(UniversalId uid) throws IOException, RecordHiddenException
	{
		return getWriteableDatabase().getIncomingInterimFile(uid);
	}

	public File getOutgoingInterimFile(UniversalId uid) throws IOException, RecordHiddenException
	{
		return getWriteableDatabase().getOutgoingInterimFile(uid);
	}
	
	public File getOutgoingInterimPublicOnlyFile(UniversalId uid) throws IOException, RecordHiddenException
	{
		return getWriteableDatabase().getOutgoingInterimPublicOnlyFile(uid);
	}
	
	public void writeBur(BulletinHeaderPacket bhp) throws CreateDigestException, IOException, RecordHiddenException
	{
		String localId = bhp.getLocalId();
		String bur = BulletinUploadRecord.createBulletinUploadRecord(localId, getSignatureGenerator());
		writeBur(bhp, bur);
	}
	
	public void writeBur(BulletinHeaderPacket bhp, String bur) throws IOException, RecordHiddenException
	{
		BulletinUploadRecord.writeSpecificBurToDatabase(getWriteableDatabase(), bhp, bur);
	}
	
	public void writeDel(UniversalId uid, DeleteRequestRecord delRecord) throws IOException, RecordHiddenException
	{
		delRecord.writeToDatabase(getWriteableDatabase(), uid);
	}
	
	public void deleteDel(UniversalId uid)
	{
		DatabaseKey delKey = DeleteRequestRecord.getDelKey(uid);
		Database db = getWriteableDatabase();
		if(db.doesRecordExist(delKey))
			db.discardRecord(delKey);
	}

	public boolean doesContactInfoExist(String accountId) throws IOException
	{
		File contactFile = getWriteableDatabase().getContactInfoFile(accountId);
		return contactFile.exists();
	}
	
	public Vector readContactInfo(String accountId) throws IOException
	{
		File contactFile = getWriteableDatabase().getContactInfoFile(accountId);
		return ContactInfo.loadFromFile(contactFile);
	}
	
	public void writeContactInfo(String accountId, Vector contactInfo) throws IOException
	{
		File contactFile = getWriteableDatabase().getContactInfoFile(accountId);
		MartusServerUtilities.writeContatctInfo(accountId, contactInfo, contactFile);
	}
	
	public void writeAccessTokens(String accountId, String tokenData) throws IOException, MartusSignatureException, InterruptedException, MartusSignatureFileAlreadyExistsException, TokenInvalidException
	{
		MartusAccountAccessToken currentToken = MartusAccountAccessToken.loadFromString(tokenData);		
		File tokenFile = getAccessTokenFileForAccount(accountId, currentToken);
		MartusServerUtilities.writeAccessTokenData(accountId, tokenData, tokenFile);
		MartusServerUtilities.createSignatureFileFromFileOnServer(tokenFile, getSignatureGenerator());
	}
	
	public void moveFormTemplateIntoAccount(String accountId, File fromFile, File toFile, LoggerInterface logger) throws IOException, MartusSignatureException, InterruptedException, MartusSignatureFileAlreadyExistsException, TokenInvalidException
	{
		if(toFile.exists())
		{
			if(!toFile.delete())
				logger.logError("failed to delete file: "+ toFile.toString());
		}
		
		if(!fromFile.renameTo(toFile))
		{
			String errorMsg = "Unable to Rename temp FormTemplate to account's FormTemplates directory, From TempFile = " + fromFile.getAbsolutePath() + ", To Account Template File = " + toFile.getAbsolutePath();
			throw new IOException(errorMsg);
		}
	}
	
	public File getTokenFileForAccount(String accountId) throws IOException, FileNotFoundException 
	{
		File tokensFolder = getAbsoluteAccountAccessTokenFolderForAccount(accountId);
		if(!tokensFolder.exists())
			throw new FileNotFoundException();
		File[] filesAvailable = tokensFolder.listFiles();
		for(int i = 0; i < filesAvailable.length; ++i)
		{
			if(filesAvailable[i].isFile() && filesAvailable[i].getName().endsWith(".dat"))
			{
				return filesAvailable[i];
			}
		}
		throw new FileNotFoundException();
	}
	
	public Vector getListOfFormTemplatesForAccount(String accountId) throws Exception 
	{
		Vector signatureVerifiedFormTemplateFiles = new Vector();
		File formTemplatesFolder = null;
		try 
		{
			formTemplatesFolder = getAbsoluteFormTemplatesFolderForAccount(accountId);
			if(!formTemplatesFolder.exists())
				return signatureVerifiedFormTemplateFiles;
		} 
		catch (FileNotFoundException e) 
		{
			return signatureVerifiedFormTemplateFiles;
		}
		File[] allFilesInFolder = formTemplatesFolder.listFiles();
		for(int i = 0; i < allFilesInFolder.length; ++i)
		{
			File currentFIle = allFilesInFolder[i];
			if(currentFIle.isFile() && currentFIle.getName().endsWith(CustomFieldTemplate.CUSTOMIZATION_TEMPLATE_EXTENSION))
			{
				signatureVerifiedFormTemplateFiles.add(currentFIle);
			}
		}
		Collections.sort(signatureVerifiedFormTemplateFiles);
		return signatureVerifiedFormTemplateFiles;
	}

	public File getFormTemplateFileFromAccount(String accountId, String FormFileName) throws FileNotFoundException 
	{
		File formTemplatesFolder = null;
		try 
		{
			formTemplatesFolder = getAbsoluteFormTemplatesFolderForAccount(accountId);
			if(!formTemplatesFolder.exists())
				throw new FileNotFoundException();
			File formTemplateFile = new File(formTemplatesFolder, FormFileName); 
			if(!formTemplateFile.exists())
				throw new FileNotFoundException();
			return formTemplateFile;
		} 
		catch (IOException e) 
		{
			throw new FileNotFoundException();
		}
	}

	public MartusAccountAccessToken readAccessTokens(String accountId) throws FileNotFoundException, IOException, TokenInvalidException, FileVerificationException, ParseException, MartusSignatureFileDoesntExistsException
	{
		File tokenFile = getTokenFileForAccount(accountId);
		MartusServerUtilities.verifyFileAndLatestSignatureOnServer(tokenFile, getSignatureVerifier());
		return MartusAccountAccessToken.loadFromFile(tokenFile);
	}

	public File getAbsoluteAccountAccessTokenFolderForAccount(String accountId) throws IOException 
	{
		return getWriteableDatabase().getAbsoluteAccountAccessTokenFolderForAccount(accountId);
	}
	
	public File getAbsoluteFormTemplatesFolderForAccount(String accountId) throws IOException 
	{
		return getWriteableDatabase().getAbsoluteFormTemplatesFolderForAccount(accountId);
	}

	public File getAccessTokenFileForAccount(String accountId, MartusAccountAccessToken token) throws IOException 
	{
		return getWriteableDatabase().getAccountAccessTokenFile(accountId, token);
	}
	
	public boolean isHidden(DatabaseKey key)
	{
		return getDatabase().isHidden(key);
	}
	
	public BulletinHeaderPacket saveZipFileToDatabase(File zipFile, String authorAccountId) throws
	Exception
	{
		return saveZipFileToDatabase(zipFile, authorAccountId, System.currentTimeMillis());
	}
	
	public BulletinHeaderPacket saveZipFileToDatabase(File zipFile, String authorAccountId, long mTime) throws
			Exception
	{
		ZipFile zip = null;
		try
		{
			zip = new ZipFile(zipFile);
			BulletinHeaderPacket header = validateZipFilePacketsForImport(zip, authorAccountId);
			importBulletinZipFile(zip, authorAccountId, mTime);
			return header;
		}
		finally
		{
			if(zip != null)
				zip.close();
		}
	}

	public BulletinHeaderPacket validateZipFilePacketsForImport(ZipFile zip, String authorAccountId) throws Exception 
	{
		BulletinHeaderPacket header = MartusUtilities.extractHeaderPacket(authorAccountId, zip, getSignatureVerifier());
		Enumeration entries = zip.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			UniversalId uid = UniversalId.createFromAccountAndLocalId(authorAccountId, entry.getName());
			DatabaseKey trySealedKey = DatabaseKey.createSealedKey(uid);
			if(getDatabase().doesRecordExist(trySealedKey))
			{
				DatabaseKey newKey = header.createKeyWithHeaderStatus(uid);
				if(newKey.isDraft())
					throw new SealedPacketExistsException(entry.getName());
				throw new DuplicatePacketException(entry.getName());
			}
		}
		
		return header;
	}
	
	public Vector getFieldOfficeAccountIdsWithResultCode(String hqAccountId, LoggerInterface logger)
	{
		Vector results = new Vector();
		
		try
		{
			Vector fieldOfficeAccounts = getFieldOffices(hqAccountId);
			if(hadErrorsWhileCacheing())
				throw new Exception();
			results.add(NetworkInterfaceConstants.OK);
			results.addAll(fieldOfficeAccounts);
		}
		catch(Exception e)
		{
			logger.logError(e);
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
		}

		return results;
	}

	public Vector getFormTemplateTitleAndDescriptionsForAccount(
			String accountToGetFormsFrom, MartusCrypto security) throws Exception 
	{
		Vector formsTemplateFiles = getListOfFormTemplatesForAccount(accountToGetFormsFrom);
		int numberOfForms = formsTemplateFiles.size();
		Vector formTemplatesTitleAndDescriptions = new Vector();
		for(int i = 0; i < numberOfForms; ++i)
		{
			try
			{
				CustomFieldTemplate formTemplate = new CustomFieldTemplate();
				File fileToImport = (File)formsTemplateFiles.get(i);
				formTemplate.importTemplate(fileToImport, security);
				Vector currentFormVectorToAdd = new Vector();
				currentFormVectorToAdd.add(formTemplate.getTitle());
				currentFormVectorToAdd.add(formTemplate.getDescription());
				formTemplatesTitleAndDescriptions.add(currentFormVectorToAdd.toArray());
			} 
			catch (FutureVersionException eLogExceptionButContinueWithRemainingValidForms) 
			{
				MartusLogger.logException(eLogExceptionButContinueWithRemainingValidForms);
			}
			
		}
		return formTemplatesTitleAndDescriptions;
	}

	public static class DuplicatePacketException extends Exception
	{
		public DuplicatePacketException(String message)
		{
			super(message);
		}

	}
	
	public static class SealedPacketExistsException extends Exception
	{
		public SealedPacketExistsException(String message)
		{
			super(message);
		}

	}
	
}
