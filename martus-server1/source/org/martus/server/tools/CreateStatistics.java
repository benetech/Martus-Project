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
package org.martus.server.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;

import org.martus.amplifier.main.MartusAmplifier;
import org.martus.common.ContactInfo;
import org.martus.common.FieldSpecCollection;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.LoggerToNull;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusXml;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.BulletinUploadRecord;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.FileDatabase;
import org.martus.common.database.FileDatabase.TooManyAccountsException;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.server.foramplifiers.ServerForAmplifiers;
import org.martus.server.forclients.AuthorizeLog;
import org.martus.server.forclients.AuthorizeLogEntry;
import org.martus.server.forclients.ServerForClients;
import org.martus.server.main.MartusServer;
import org.martus.server.main.ServerBulletinStore;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;



public class CreateStatistics
{
	public static void main(String[] args)
	{
		try
		{
			boolean prompt = true;
			boolean deletePrevious = false;
			File dataDir = null;
			File destinationDir = null;
			File keyPairFile = null;
			File adminStartupDir = null;
			File magicMapFile = null;
			String serverName = "";

			for (int i = 0; i < args.length; i++)
			{
				if(args[i].startsWith("--no-prompt"))
					prompt = false;
			
				if(args[i].startsWith("--delete-previous"))
					deletePrevious = true;
			
				String value = args[i].substring(args[i].indexOf("=")+1);
				if(args[i].startsWith("--packet-directory="))
					dataDir = new File(value);
				
				if(args[i].startsWith("--keypair"))
					keyPairFile = new File(value);
				
				if(args[i].startsWith("--destination-directory"))
					destinationDir = new File(value);

				if(args[i].startsWith("--admin-startup-directory"))
					adminStartupDir = new File(value);
				
				if(args[i].startsWith("--server-name"))
					serverName = value;
				if(args[i].startsWith("--magic-map"))
					magicMapFile = new File(value);
			}
			
			if(destinationDir == null || dataDir == null || keyPairFile == null || adminStartupDir == null)
			{
				System.err.println("Incorrect arguments: CreateStatistics [--no-prompt] [--delete-previous] [--server-name=\"name of this server\"] [--magic-map=<magicGroupTranslationFile>] --packet-directory=<packetdir> --keypair-file=<keypair> --destination-directory=<destinationDir> --admin-startup-directory=<adminStartupConfigDir>\n");
				System.exit(2);
			}
			
			if(!dataDir.exists())
			{
				System.err.println("The packets data directory " + dataDir.getAbsolutePath()+ " does not exist.\n");
				System.exit(3);
			}
			
			destinationDir.mkdirs();
			if(prompt)
			{
				System.out.print("Enter server passphrase:");
				System.out.flush();
			}
			
			BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));
			//TODO password is a string
			String passphrase = reader.readLine();
			MartusCrypto security = MartusServerUtilities.loadCurrentMartusSecurity(keyPairFile, passphrase.toCharArray());

			new CreateStatistics(security, dataDir, destinationDir, adminStartupDir, deletePrevious, serverName, magicMapFile);
		}
		catch(Exception e)
		{
			System.err.println("CreateStatistics.main: " + e);
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Done!");
		System.exit(0);
	}
	public CreateStatistics(MartusCrypto securityToUse, File dataDirToUse, File destinationDirToUse, File adminStartupDirToUse, boolean deletePreviousToUse, String serverNameToUse, File magicMapFileToUse) throws Exception
	{
		security = securityToUse;
		deletePrevious = deletePreviousToUse;
		packetsDir = dataDirToUse;
		destinationDir = destinationDirToUse;
		adminStartupDir = adminStartupDirToUse;
		serverName = serverNameToUse;
		updateMagicWordToGroup(magicMapFileToUse);
		fileDatabase = new ServerFileDatabase(dataDirToUse, security);
		fileDatabase.initialize();
		store = new ServerBulletinStore();
		store.setDatabase(fileDatabase);
		clientsThatCanUpload = MartusUtilities.loadClientListAndExitOnError(new File(packetsDir.getParentFile(), ServerForClients.UPLOADSOKFILENAME));
		bannedClients = MartusUtilities.loadClientListAndExitOnError(new File(adminStartupDir, ServerForClients.BANNEDCLIENTSFILENAME));
		testClients = MartusUtilities.loadClientListAndExitOnError(new File(adminStartupDir, ServerForClients.TESTACCOUNTSFILENAME));

		UnicodeReader reader = new UnicodeReader(new File(adminStartupDir, MartusServer.HIDDENPACKETSFILENAME));
		hiddenBulletinIds = MartusServerUtilities.getHiddenPacketsList(reader);
		
		clientsNotToAmplifyFromServer = MartusUtilities.loadClientListAndExitOnError(new File(adminStartupDir, ServerForAmplifiers.CLIENTS_NOT_TO_AMPLIFY_FILENAME));
		clientsNotToAmplifyFromAmp = MartusUtilities.loadClientListAndExitOnError(new File(adminStartupDir, MartusAmplifier.ACCOUNTS_NOT_AMPLIFIED_FILE));
		authorizeLog = new AuthorizeLog(security, new LoggerToNull(), new File(packetsDir.getParentFile(), ServerForClients.AUTHORIZELOGFILENAME));  		
		authorizeLog.loadFile();

		createAccountStatistics();
		createBulletinStatistics();
	}
	
	private void updateMagicWordToGroup(File magicWordMapFile) throws Exception
	{
		magicWordMap = new HashMap();
		if(magicWordMapFile == null)
			return;
		UnicodeReader reader = new UnicodeReader(magicWordMapFile);
		reader.readLine(); //header
		boolean errorOccuredMagicWordUpdate = false;
		while (true)
		{
			String lineIn  = reader.readLine();
			if(lineIn == null)
				break;
			String columns[] = lineIn.split("\t",6);
			String magicWord = removeQuotesAndTrim(columns[0]);
			String placeHolder = removeQuotesAndTrim(columns[1]);
			String groupName = removeQuotesAndTrim(columns[3]);
			
			if(magicWord.length() == 0)
			{
				System.err.println("Error: magic word empty :" + lineIn);
				errorOccuredMagicWordUpdate = true;	
			}
			if(placeHolder.length() == 0 && groupName.length() == 0)
			{
				System.err.println("Error: placeholder and group empty :" + lineIn);
				errorOccuredMagicWordUpdate = true;
			}
			if(groupName.length() != 0)
			{
				if(!addMagicMapping(groupName, groupName))
					errorOccuredMagicWordUpdate = true;
					
				if(!addMagicMapping(magicWord, groupName))
					errorOccuredMagicWordUpdate = true;
				if(placeHolder.length() != 0)
				{
					if(!addMagicMapping(placeHolder, groupName))
						errorOccuredMagicWordUpdate = true;
				}
			}
			else
			{
				if(!addMagicMapping(magicWord, placeHolder))
					errorOccuredMagicWordUpdate = true;
					
				if(!addMagicMapping(placeHolder, placeHolder))
					errorOccuredMagicWordUpdate = true;
			}
		}
		if(errorOccuredMagicWordUpdate)
			System.exit(4);
	}
	
	private String removeQuotesAndTrim(String data)
	{
		if(data.length() == 0)
			return data;
		String strippedData = data.substring(1, data.length()-1);
		return strippedData.trim();
	}
	
	private boolean addMagicMapping(String key, String value) 
	{
		if(magicWordMap.containsKey(key.toLowerCase().trim()))
		{
			System.err.println("Error magic word key already exists: " + key);
			return false;
		}
		magicWordMap.put(key.toLowerCase().trim(), value);
		return true;
	}

	private void createAccountStatistics() throws Exception
	{
		final File accountStatsError = new File(destinationDir,ACCOUNT_STATS_FILE_NAME + ERR_EXT + CSV_EXT);
		class AccountVisitor implements Database.AccountVisitor 
		{
			public AccountVisitor(UnicodeWriter writerToUse)
			{
				writer = writerToUse;
			}
			
			public void visit(String accountId)
			{
				errorOccured = false;
				try
				{
					File accountDir = fileDatabase.getAbsoluteAccountDirectory(accountId);
					File bucket = accountDir.getParentFile();
					getPublicCode(accountId);
					getContactInfo(accountId);
					getAuthorizedInfo(publicCode);
					String uploadOk = isAllowedToUpload(accountId);
					String banned = isBanned(accountId);
					String testAccount = isTestAccount(accountId);
					String canServerAmplify = canServerAmplify(accountId);
					String canAmpAmplify = canAmpAmplify(accountId);
					String canAmplify = ACCOUNT_TESTER_FALSE;
					if(canServerAmplify.equals(ACCOUNT_TESTER_TRUE) || 
							canAmpAmplify.equals(ACCOUNT_TESTER_TRUE))
						canAmplify = ACCOUNT_TESTER_TRUE;
					
					String accountInfo = 
						getNormalizedStringAndCheckForErrors(serverName) + DELIMITER +
						getNormalizedStringAndCheckForErrors(publicCode) + DELIMITER +
						getNormalizedStringAndCheckForErrors(testAccount) + DELIMITER +						
						getNormalizedStringAndCheckForErrors(uploadOk) + DELIMITER +
						getNormalizedStringAndCheckForErrors(banned) + DELIMITER +
						getNormalizedStringAndCheckForErrors(canServerAmplify) + DELIMITER +
						getNormalizedStringAndCheckForErrors(canAmpAmplify) + DELIMITER +
						getNormalizedStringAndCheckForErrors(canAmplify) + DELIMITER +
						getNormalizedStringAndCheckForErrors(clientAuthorizedDate) + DELIMITER +
						getNormalizedStringAndCheckForErrors(clientGroup) + DELIMITER +
						getNormalizedStringAndCheckForErrors(author) + DELIMITER +
						getNormalizedStringAndCheckForErrors(organization) + DELIMITER +
						getNormalizedStringAndCheckForErrors(email) + DELIMITER +
						getNormalizedStringAndCheckForErrors(webpage) + DELIMITER +
						getNormalizedStringAndCheckForErrors(phone) + DELIMITER +
						getNormalizedStringAndCheckForErrors(address) + DELIMITER +
						getNormalizedStringAndCheckForErrors(bucket.getName() + "/" + accountDir.getName()) + DELIMITER + 
						getNormalizedStringAndCheckForErrors(accountId);

					writer.writeln(accountInfo);
					if(errorOccured)
						writeErrorLog(accountStatsError, ACCOUNT_STATISTICS_HEADER, accountInfo);
				}
				catch(Exception e1)
				{
					try
					{
						writeErrorLog(accountStatsError, ACCOUNT_STATISTICS_HEADER, e1.getMessage());
						e1.printStackTrace();
					}
					catch(IOException e2)
					{
						e2.printStackTrace();
					}
				}
			}
			
			private void getAuthorizedInfo(String clientPublicCode)
			{
				AuthorizeLogEntry clientEntry = authorizeLog.getAuthorizedClientEntry(clientPublicCode);
				clientAuthorizedDate = "";
				clientGroup = "";
				if(clientEntry != null)
				{
					clientAuthorizedDate = clientEntry.getDate();
					String clientGroupWhenAuthorized = clientEntry.getGroupName().toLowerCase().trim();
					//clientIPAddress = clientEntry.getIp();
					clientGroup = (String)magicWordMap.get(clientGroupWhenAuthorized);
					if(clientGroup == null)
					{
						System.out.println("Warning unknown Group :" + clientGroupWhenAuthorized);
						clientGroup = clientGroupWhenAuthorized;
					}
				}
			}
			
			class NoContactInfo extends IOException
			{
			}

			class ContactInfoException extends IOException
			{
			}
			
			private void getContactInfo(String accountId)
			{
				author = ERROR_MSG;
				organization = ERROR_MSG;
				email = ERROR_MSG;
				webpage = ERROR_MSG;
				phone = ERROR_MSG;
				address = ERROR_MSG;
				
				try
				{
					File contactFile = fileDatabase.getContactInfoFile(accountId);
					if(!contactFile.exists())
						throw new NoContactInfo();
					Vector contactInfoRaw = ContactInfo.loadFromFile(contactFile);
					Vector contactInfo = ContactInfo.decodeContactInfoVectorIfNecessary(contactInfoRaw);
					int size = contactInfo.size();
					if(size>0)
					{
						String contactAccountIdInsideFile = (String)contactInfo.get(0);
						if(!security.verifySignatureOfVectorOfStrings(contactInfo, contactAccountIdInsideFile))
						{
							author = ERROR_MSG + " Signature failure contactInfo";
							throw new ContactInfoException();
						}
						
						if(!contactAccountIdInsideFile.equals(accountId))
						{
							author = ERROR_MSG + " AccountId doesn't match contactInfo's AccountId";
							throw new ContactInfoException();
						}			
					}
					
					if(size>2)
						author = (String)(contactInfo.get(2));
					if(size>3)
						organization = (String)(contactInfo.get(3));
					if(size>4)
						email = (String)(contactInfo.get(4));
					if(size>5)
						webpage = (String)(contactInfo.get(5));
					if(size>6)
						phone = (String)(contactInfo.get(6));
					if(size>7)
						address = (String)(contactInfo.get(7));
				}
				catch (NoContactInfo e)
				{
					author = "";
					organization = "";
					email = "";
					webpage = "";
					phone = "";
					address = "";
				}
				catch (ContactInfoException e)
				{
				}
				catch (IOException e)
				{
					author = ERROR_MSG + " IO exception contactInfo";
				}
				catch(InvalidBase64Exception e)
				{
					author = ERROR_MSG + " InvalidBase64Exception contactInfo";
				}
			}
			private String isAllowedToUpload(String accountId)
			{
				if(clientsThatCanUpload.contains(accountId))
					return ACCOUNT_UPLOAD_OK_TRUE;
				return	ACCOUNT_UPLOAD_OK_FALSE;
			}
			private String isBanned(String accountId)
			{
				if(bannedClients.contains(accountId))
					return ACCOUNT_BANNED_TRUE;
				return ACCOUNT_BANNED_FALSE;
			}
			private String isTestAccount(String accountId)
			{
				if(testClients.contains(accountId))
					return ACCOUNT_TESTER_TRUE;
				return ACCOUNT_TESTER_FALSE;
			}
			private String canServerAmplify(String accountId)
			{
				if(clientsNotToAmplifyFromServer.contains(accountId))
					return ACCOUNT_AMPLIFY_FALSE;
				return ACCOUNT_AMPLIFY_TRUE;
			}
			private String canAmpAmplify(String accountId)
			{
				if(clientsNotToAmplifyFromAmp.contains(accountId))
					return ACCOUNT_AMPLIFY_FALSE;
				return ACCOUNT_AMPLIFY_TRUE;
			}

			private UnicodeWriter writer;
			private String author;
			private String organization;
			private String email;
			private String webpage;
			private String phone;
			private String address;
			
			private String clientAuthorizedDate = "";
			private String clientGroup = ""; 
		}

		
		System.out.println("Creating Account Statistics");
		File accountStats = new File(destinationDir,ACCOUNT_STATS_FILE_NAME + CSV_EXT);
		if(deletePrevious)
		{
			accountStats.delete();
			accountStatsError.delete();
		}
		
		if(accountStats.exists())
			throw new Exception("File Exists.  Please delete before running: "+accountStats.getAbsolutePath());
		if(accountStatsError.exists())
			throw new Exception("File Exists.  Please delete before running: "+accountStatsError.getAbsolutePath());
		
		UnicodeWriter writer = new UnicodeWriter(accountStats);
		try
		{
			writer.writeln(ACCOUNT_STATISTICS_HEADER);
			fileDatabase.visitAllAccounts(new AccountVisitor(writer));
		}
		finally
		{
			writer.close();
		}
	}

	private void createBulletinStatistics() throws Exception
	{
		final File bulletinStatsError = new File(destinationDir, BULLETIN_STATS_FILE_NAME + ERR_EXT + CSV_EXT);
		class BulletinVisitor implements Database.PacketVisitor
		{
			public BulletinVisitor(UnicodeWriter writerToUse)
			{
				writer = writerToUse;
			}
			public void visit(DatabaseKey key)
			{
				errorOccured = false;
				try
				{
					if(!BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
						return;
					String martusVersionBulletionWasCreatedWith = getMartusBuildDateForBulletin(key);
					String martusBuildDate = getBuildDate(martusVersionBulletionWasCreatedWith);
					String martusBuildNumber = getBuildNumber(martusVersionBulletionWasCreatedWith);
					
					String testBulletin = isTestBulletin(key.getAccountId());
					String bulletinType = getBulletinType(key);
					String isBulletinHidden = getIsBulletinHidden(key.getUniversalId());
					String isFinalVersion = isFinalVersion(key.getUniversalId());
					getPublicCode(key.getAccountId());
					getBulletinHeaderInfo(key);
					DatabaseKey burKey = BulletinUploadRecord.getBurKey(key);
					String wasBurCreatedByThisServer = wasOriginalServer(burKey);
					String dateBulletinWasSavedOnServer = ERROR_MSG;
					String timeBulletinWasSavedOnServer = ERROR_MSG;
					try
					{
						Date dateTimeBulletinWasSaved = getOriginalUploadDate(burKey);
						SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_DATE_PATTERN);
						dateBulletinWasSavedOnServer = dateFormat.format(dateTimeBulletinWasSaved);
						
						dateFormat.applyPattern(ISO_TIME_PATTERN);
						timeBulletinWasSavedOnServer = dateFormat.format(dateTimeBulletinWasSaved);
					}
					catch(Exception e1)
					{
						dateBulletinWasSavedOnServer = ERROR_MSG;
						timeBulletinWasSavedOnServer = ERROR_MSG;
					}
					getPacketInfo(key);
					
					String bulletinInfo =  
					getNormalizedStringAndCheckForErrors(serverName) + DELIMITER +
					getNormalizedStringAndCheckForErrors(originalBulletinLocalId) + DELIMITER +
					getNormalizedStringAndCheckForErrors(key.getLocalId()) + DELIMITER +
					getNormalizedStringAndCheckForErrors(Integer.toString(bulletinVersionNumber)) + DELIMITER +
					getNormalizedStringAndCheckForErrors(isFinalVersion) + DELIMITER +
					getNormalizedStringAndCheckForErrors(martusBuildDate) + DELIMITER + 
					getNormalizedStringAndCheckForErrors(martusBuildNumber) + DELIMITER + 
					getNormalizedStringAndCheckForErrors(testBulletin) + DELIMITER +
					getNormalizedStringAndCheckForErrors(bulletinType) + DELIMITER +
					getNormalizedStringAndCheckForErrors(Integer.toString(bulletinSizeInKBytes)) + DELIMITER + 
					getNormalizedStringAndCheckForErrors(allPrivate) + DELIMITER +
					getNormalizedStringAndCheckForErrors(bulletinHasCustomFields) + DELIMITER +
					getNormalizedStringAndCheckForErrors(bulletinCustomFieldTypes) + DELIMITER +
					getNormalizedStringAndCheckForErrors(bulletinAuthor) + DELIMITER +
					getNormalizedStringAndCheckForErrors(bulletinTitle) + DELIMITER +
					getNormalizedStringAndCheckForErrors(bulletinSummary) + DELIMITER +
					getNormalizedStringAndCheckForErrors(bulletinLanguage) + DELIMITER +
					getNormalizedStringAndCheckForErrors(bulletinLocation) + DELIMITER +
					getNormalizedStringAndCheckForErrors(bulletinKeywords) + DELIMITER +
					getNormalizedStringAndCheckForErrors(bulletinDateCreated) + DELIMITER +
					getNormalizedStringAndCheckForErrors(bulletinDateEvent) + DELIMITER +
					getNormalizedStringAndCheckForErrors(Integer.toString(publicAttachmentCount)) + DELIMITER + 
					getNormalizedStringAndCheckForErrors(Integer.toString(privateAttachmentCount)) + DELIMITER + 
					getNormalizedStringAndCheckForErrors(wasBurCreatedByThisServer) + DELIMITER +
					getNormalizedStringAndCheckForErrors(isBulletinHidden) + DELIMITER +
					getNormalizedStringAndCheckForErrors(dateBulletinWasSavedOnServer) + DELIMITER +
					getNormalizedStringAndCheckForErrors(timeBulletinWasSavedOnServer) + DELIMITER +
					getNormalizedStringAndCheckForErrors(dateBulletinLastSaved) + DELIMITER +
					getNormalizedStringAndCheckForErrors(timeBulletinLastSaved) + DELIMITER +
					getNormalizedStringAndCheckForErrors(allHQsProxyUpload) + DELIMITER +
					getNormalizedStringAndCheckForErrors(hQsAuthorizedToRead) + DELIMITER +
					getNormalizedStringAndCheckForErrors(hQsAuthorizedToUpload) + DELIMITER +
					getNormalizedStringAndCheckForErrors(publicCode);
					
					writer.writeln(bulletinInfo);
					if(errorOccured)
						writeErrorLog(bulletinStatsError, BULLETIN_STATISTICS_HEADER, bulletinInfo);
				}
				catch(IOException e)
				{
					try
					{
						writeErrorLog(bulletinStatsError, BULLETIN_STATISTICS_HEADER, e.getMessage());
						e.printStackTrace();
					}
					catch(IOException e2)
					{
						e2.printStackTrace();
					}
				}
			}
			
			private void getPacketInfo(DatabaseKey key)
			{
				bulletinAuthor = ERROR_MSG;
				bulletinTitle = ERROR_MSG;
				bulletinSummary = ERROR_MSG;
				bulletinLanguage = ERROR_MSG;
				bulletinLocation = ERROR_MSG;
				bulletinKeywords = ERROR_MSG;
				bulletinDateCreated = ERROR_MSG;
				bulletinDateEvent = ERROR_MSG;
				bulletinHasCustomFields = ERROR_MSG;
				bulletinCustomFieldTypes = ERROR_MSG;
				
				try
				{
					BulletinHeaderPacket bhp = BulletinStore.loadBulletinHeaderPacket(fileDatabase, key, security);
					if(key.isDraft() || bhp.isAllPrivate())
					{
						bulletinAuthor = "";
						bulletinTitle = "";
						bulletinSummary = "";
						bulletinLanguage = "";
						bulletinLocation = "";
						bulletinKeywords = "";
						bulletinDateCreated = "";
						bulletinDateEvent = "";
						bulletinHasCustomFields = "";
						bulletinCustomFieldTypes = "";
						return;
					}
					String fieldDataPacketId = bhp.getFieldDataPacketId();
					DatabaseKey fieldKey = DatabaseKey.createSealedKey(UniversalId.createFromAccountAndLocalId(
						bhp.getAccountId(), fieldDataPacketId));
					FieldSpecCollection standardPublicFieldSpecs = StandardFieldSpecs.getDefaultTopSetionFieldSpecs();
					FieldDataPacket fdp = new FieldDataPacket(UniversalId.createFromAccountAndLocalId(
						bhp.getAccountId(), fieldDataPacketId), standardPublicFieldSpecs);
					FileInputStreamWithSeek in = new FileInputStreamWithSeek(fileDatabase.getFileForRecord(fieldKey));
					fdp.loadFromXml(in, bhp.getFieldDataSignature(), security);

					in.close();
					bulletinAuthor = fdp.get(BulletinConstants.TAGAUTHOR);
					bulletinTitle = fdp.get(BulletinConstants.TAGTITLE);
					bulletinSummary = fdp.get(BulletinConstants.TAGSUMMARY);
					bulletinLanguage = fdp.get(BulletinConstants.TAGLANGUAGE);
					bulletinLocation = fdp.get(BulletinConstants.TAGLOCATION);
					bulletinKeywords = fdp.get(BulletinConstants.TAGKEYWORDS);
					bulletinDateCreated = fdp.get(BulletinConstants.TAGENTRYDATE);
					String eventDate = fdp.get(BulletinConstants.TAGEVENTDATE);
					MiniLocalization localization = new MiniLocalization();
					MartusFlexidate mfd = localization.createFlexidateFromStoredData(eventDate);
					String rawBeginDate = MartusFlexidate.toStoredDateFormat(mfd.getBeginDate());
					if(mfd.hasDateRange())
					{
						String rawEndDate = MartusFlexidate.toStoredDateFormat(mfd.getEndDate());
						bulletinDateEvent = rawBeginDate + " - " + rawEndDate;
					}
					else
					{
						bulletinDateEvent = rawBeginDate;
					}
					
					FieldSpec[] fieldSpecs = fdp.getFieldSpecs().asArray();
					if(fdp.hasCustomFieldTemplate())
						bulletinHasCustomFields = BULLETIN_HAS_CUSTOM_FIELDS_TRUE;
					else
						bulletinHasCustomFields = BULLETIN_HAS_CUSTOM_FIELDS_FALSE;
										
					bulletinCustomFieldTypes = "";
					for(int i = 0 ; i < fieldSpecs.length; ++i)
					{
						FieldSpec fieldSpec = fieldSpecs[i];
						if(!StandardFieldSpecs.isStandardFieldTag(fieldSpec.getTag()))
						{
							if(bulletinCustomFieldTypes.length()>0)
								bulletinCustomFieldTypes += ", ";
							bulletinCustomFieldTypes += FieldSpec.getTypeString(fieldSpec.getType()); 
						}
					}
				}
				catch(Exception e)
				{
					bulletinTitle = ERROR_MSG + " " + e.getMessage();
				}
			}

			private void getBulletinHeaderInfo(DatabaseKey key)
			{
				allPrivate = ERROR_MSG;
				dateBulletinLastSaved = ERROR_MSG;
				timeBulletinLastSaved = ERROR_MSG;
				allHQsProxyUpload = ERROR_MSG;
				hQsAuthorizedToRead = ERROR_MSG;
				hQsAuthorizedToUpload = ERROR_MSG;
				originalBulletinLocalId = ERROR_MSG;
				publicAttachmentCount = -1;
				privateAttachmentCount = -1;
				bulletinSizeInKBytes = -1;
				bulletinVersionNumber = -1;
				
				

				try
				{
					BulletinHeaderPacket bhp = BulletinStore.loadBulletinHeaderPacket(fileDatabase, key, security);
					bulletinVersionNumber = bhp.getVersionNumber();
					originalBulletinLocalId = bhp.getOriginalRevisionId();
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTimeInMillis(bhp.getLastSavedTime());		
					try
					{
						SimpleDateFormat formatDate = new SimpleDateFormat(ISO_DATE_PATTERN);
						dateBulletinLastSaved = formatDate.format(cal.getTime());
						formatDate.applyPattern(ISO_TIME_PATTERN);
						timeBulletinLastSaved = formatDate.format(cal.getTime());
					}
					catch(RuntimeException e)
					{
						dateBulletinLastSaved = ERROR_MSG + " " + e;
					}
					
					bulletinSizeInKBytes = MartusUtilities.getBulletinSize(fileDatabase, bhp) / 1000;
					String[] publicAttachments = bhp.getPublicAttachmentIds();
					String[] privateAttachments = bhp.getPrivateAttachmentIds();

					if(bhp.canAllHQsProxyUpload())
						allHQsProxyUpload = BULLETIN_ALL_HQS_PROXY_UPLOAD_TRUE;
					else
						allHQsProxyUpload = BULLETIN_ALL_HQS_PROXY_UPLOAD_FALSE;
					
					hQsAuthorizedToRead = getListOfHQKeys(bhp.getAuthorizedToReadKeys());
					hQsAuthorizedToUpload = getListOfHQKeys(bhp.getAuthorizedToUploadKeys());
					
					if(bhp.isAllPrivate())
					{
						allPrivate = BULLETIN_ALL_PRIVATE_TRUE;
						publicAttachmentCount = 0;
						privateAttachmentCount = publicAttachments.length;
						privateAttachmentCount += privateAttachments.length;
					}
					else
					{
						allPrivate = BULLETIN_ALL_PRIVATE_FALSE;
						publicAttachmentCount = publicAttachments.length;
						privateAttachmentCount = privateAttachments.length;
					}
				}
				catch(Exception e1)
				{
					allPrivate = ERROR_MSG + " " + e1;
				}
			}
			private String getBuildDate(String martusBuildInfo) 
			{
				String rawDate = getMartusBuildInfoField(martusBuildInfo, 0);
				if(rawDate.startsWith("?"))
				{
					if(rawDate.length() == 1)
						return rawDate;
					return rawDate.substring(1);
				}
				SimpleDateFormat dateFormat = new SimpleDateFormat(MARTUS_BULLETIN_BUILD_DATE_PATTERN);
				try 
				{
					Date newDate = dateFormat.parse(rawDate);
					dateFormat.applyPattern(ISO_DATE_PATTERN);
					return dateFormat.format(newDate);
				} 
				catch (ParseException e) 
				{
				}
				return "?";
			}
			private String getBuildNumber(String martusBuildInfo)
			{
				return getMartusBuildInfoField(martusBuildInfo, 1);
			}
			private String getMartusBuildInfoField(String martusBuildInfo, int position)
			{
				if(martusBuildInfo == null)
					return "?";
				if(martusBuildInfo.length()==0)
					return "?";
				int splitAt = martusBuildInfo.indexOf(".");
				if(splitAt <= 0)
				{
					if(position == 0)
						return martusBuildInfo;
					return "?";
				}
				
				if(position == 1)
					return martusBuildInfo.substring(splitAt+1);
				return martusBuildInfo.substring(0,splitAt);
			}
			
			private String getIsBulletinHidden(UniversalId uId)
			{
				String bulletinHidden = ERROR_MSG;
				if(hiddenBulletinIds.contains(uId))
					bulletinHidden = BULLETIN_HIDDEN_TRUE;
				else
					bulletinHidden = BULLETIN_HIDDEN_FALSE;
				return bulletinHidden;
			}
			
			private String getListOfHQKeys(HeadquartersKeys keys)
			{
				String keyList = "";
				try
				{
					for(int i = 0; i < keys.size(); i++)
					{
						HeadquartersKey key = keys.get(i);
						if(keyList.length()>0)
							keyList += ", ";
						keyList += key.getFormattedPublicCode();
					}
				}
				catch(InvalidBase64Exception e)
				{
					keyList = ERROR_MSG;
				}
				return keyList;
			}
			private String isTestBulletin(String accountId)
			{
				if(testClients.contains(accountId))
					return BULLETIN_TEST_TRUE;
				return BULLETIN_TEST_FALSE;
			}
			private String getBulletinType(DatabaseKey key)
			{
				String bulletinType = ERROR_MSG + " not draft or sealed?";
				if(key.isSealed())
					bulletinType = BULLETIN_SEALED;
				else if(key.isDraft())
					bulletinType = BULLETIN_DRAFT;
				return bulletinType;
			}
			private String getMartusBuildDateForBulletin(DatabaseKey key) throws IOException, TooManyAccountsException
			{
				String martusBuildDateBulletionWasCreatedWith = ERROR_MSG;
				try
				{
					File bhpFile = fileDatabase.getFileForRecord(key);
					UnicodeReader reader = new UnicodeReader(bhpFile);
					String headerComment = reader.readLine();
					if(headerComment.startsWith(MartusXml.packetStartCommentStart))
					{
						String[] commentFields = headerComment.split(";");
						martusBuildDateBulletionWasCreatedWith =  commentFields[1];
					}
					else
						martusBuildDateBulletionWasCreatedWith = ERROR_MSG + " bhp didnot start with " + MartusXml.packetStartCommentStart;
				}
				catch(Exception e)
				{
					martusBuildDateBulletionWasCreatedWith = ERROR_MSG + " " + e.getMessage();
				}
				return martusBuildDateBulletionWasCreatedWith;
			}
			
			private String wasOriginalServer(DatabaseKey burKey)
			{
				String wasBurCreatedByThisServer = ERROR_MSG;
				try
				{
					if(!fileDatabase.getFileForRecord(burKey).exists())
					{
						wasBurCreatedByThisServer =ERROR_MSG + " missing BUR";
					}
					else
					{
						String burString = fileDatabase.readRecord(burKey, security);
						if(burString.length()==0)
							wasBurCreatedByThisServer = ERROR_MSG + " record empty?";
						else 
						{
							if(BulletinUploadRecord.wasBurCreatedByThisCrypto(burString, security))
								wasBurCreatedByThisServer = BULLETIN_ORIGINALLY_UPLOADED_TO_THIS_SERVER_TRUE;
							else
								wasBurCreatedByThisServer = BULLETIN_ORIGINALLY_UPLOADED_TO_THIS_SERVER_FALSE;
						}
					}
				}
				catch(Exception e1)
				{
					wasBurCreatedByThisServer = ERROR_MSG + " " + e1;
				}
				return wasBurCreatedByThisServer;
			}
			
			private String isFinalVersion(UniversalId uId)
			{
				String finalVersion = ERROR_MSG;
				if(store.isLeaf(uId))
					finalVersion = BULLETIN_FINAL_VERSION_TRUE;
				else
					finalVersion = BULLETIN_FINAL_VERSION_FALSE;
				return finalVersion;
			}
			
			private Date getOriginalUploadDate(DatabaseKey burKey) throws Exception
			{
				if(fileDatabase.getFileForRecord(burKey).exists())
				{
					String burString = fileDatabase.readRecord(burKey, security);
					if(burString.length()!=0)
					{
						String[] burData = burString.split("\n");
						return MartusServerUtilities.getDateFromFormattedTimeStamp(burData[2]);
					}
				}
				return null;
			}
			
			UnicodeWriter writer;
			String allPrivate;
			String dateBulletinLastSaved;
			String timeBulletinLastSaved;
			String allHQsProxyUpload;
			String hQsAuthorizedToRead;
			String hQsAuthorizedToUpload;
			String bulletinAuthor;
			String bulletinTitle;
			String bulletinSummary;
			String bulletinLanguage;
			String bulletinLocation;
			String bulletinKeywords;
			String bulletinHasCustomFields;
			String bulletinCustomFieldTypes;
			String bulletinDateCreated;
			String bulletinDateEvent;
			String originalBulletinLocalId;
			int publicAttachmentCount;
			int privateAttachmentCount;
			int bulletinSizeInKBytes;
			int bulletinVersionNumber;
		}
		
		System.out.println("Creating Bulletin Statistics");
		File bulletinStats = new File(destinationDir,BULLETIN_STATS_FILE_NAME + CSV_EXT);
		if(deletePrevious)
		{
			bulletinStats.delete();
			bulletinStatsError.delete();
		}
		if(bulletinStats.exists())
			throw new Exception("File Exists.  Please delete before running: "+bulletinStats.getAbsolutePath());
		if(bulletinStatsError.exists())
			throw new Exception("File Exists.  Please delete before running: "+bulletinStatsError.getAbsolutePath());
		
		UnicodeWriter writer = new UnicodeWriter(bulletinStats);
		try
		{
			writer.writeln(BULLETIN_STATISTICS_HEADER);
			fileDatabase.visitAllRecords(new BulletinVisitor(writer));
		}
		finally
		{
			writer.close();
		}
	}

	void getPublicCode(String accountId)
	{
		publicCode = "";
		try
		{
			publicCode = MartusCrypto.computeFormattedPublicCode(accountId);
		}
		catch(Exception e)
		{
			publicCode = ERROR_MSG + " " + e;
		}
	}
	
	void writeErrorLog(File bulletinStatsError, String headerString, String errorMsg) throws IOException
	{
		boolean includeErrorHeader = (!bulletinStatsError.exists());
		UnicodeWriter writerErr = new UnicodeWriter(bulletinStatsError, UnicodeWriter.APPEND);
		if(includeErrorHeader)
			writerErr.writeln(headerString);
		writerErr.writeln(errorMsg);
		writerErr.close();
	}
	String getNormalizedStringAndCheckForErrors(Object rawdata)
	{
		String data = (String)rawdata;
		if(data.startsWith(ERROR_MSG))
			errorOccured = true;
		String normalized = data.replaceAll("\"", "'");
		normalized = normalized.replaceAll("\n", " | ");
		return "\"" + normalized + "\"";
	}
	
	private boolean deletePrevious;
	private File packetsDir;
	private File adminStartupDir;
	String serverName;
	MartusCrypto security;
	File destinationDir;
	String publicCode;
	boolean errorOccured;
	ServerBulletinStore store;
	FileDatabase fileDatabase;
	HashMap magicWordMap;
	Vector clientsThatCanUpload;
	Vector bannedClients;
	Vector testClients;
	Vector clientsNotToAmplifyFromServer;
	Vector clientsNotToAmplifyFromAmp;
	Vector hiddenBulletinIds;
	AuthorizeLog authorizeLog;
	
	final String ISO_DATE_PATTERN = "yyyy-MM-dd";
	final String ISO_TIME_PATTERN = "HH:mm";
	final String MARTUS_BULLETIN_BUILD_DATE_PATTERN = "yyyyMMdd";
	
	final String DELIMITER = ",";
	final String ERROR_MSG = "Error:";
	final String ERR_EXT = ".err";
	final String CSV_EXT = ".csv";
	final String ACCOUNT_SERVER_NAME = "server";
	final String ACCOUNT_STATS_FILE_NAME = "accounts";
	final String ACCOUNT_PUBLIC_CODE = "public code";
	final String ACCOUNT_UPLOAD_OK = "can upload";
	final String ACCOUNT_TESTER = "tester";
	final String ACCOUNT_BANNED = "banned";
	final String ACCOUNT_SERVER_AMPLIFY = "server amplify";
	final String ACCOUNT_AMP_AMPLIFY = "amp amplify";
	final String ACCOUNT_CAN_AMPLIFY = "can amplify";
	final String ACCOUNT_DATE_AUTHORIZED = "date authorized";
	final String ACCOUNT_GROUP = "group";
	final String ACCOUNT_AUTHOR = "author name";
	final String ACCOUNT_ORGANIZATION = "organization";
	final String ACCOUNT_EMAIL = "email";
	final String ACCOUNT_WEBPAGE = "web page";
	final String ACCOUNT_PHONE = "phone";
	final String ACCOUNT_ADDRESS = "address";
	final String ACCOUNT_FOLDER = "account folder";
	final String ACCOUNT_PUBLIC_KEY = "public key";
	final String ACCOUNT_UPLOAD_OK_TRUE = "1";
	final String ACCOUNT_UPLOAD_OK_FALSE = "0";
	final String ACCOUNT_BANNED_TRUE = "1";
	final String ACCOUNT_BANNED_FALSE = "0"; 
	final String ACCOUNT_AMPLIFY_TRUE = "1";
	final String ACCOUNT_AMPLIFY_FALSE = "0";
	final String ACCOUNT_TESTER_TRUE = "1";
	final String ACCOUNT_TESTER_FALSE = "0";

	final String ACCOUNT_STATISTICS_HEADER = 
		getNormalizedStringAndCheckForErrors(ACCOUNT_SERVER_NAME) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_PUBLIC_CODE) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_TESTER) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_UPLOAD_OK) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_BANNED) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_SERVER_AMPLIFY) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_AMP_AMPLIFY) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_CAN_AMPLIFY) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_DATE_AUTHORIZED) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_GROUP) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_AUTHOR) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_ORGANIZATION) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_EMAIL) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_WEBPAGE) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_PHONE) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_ADDRESS) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_FOLDER) + DELIMITER + 
		getNormalizedStringAndCheckForErrors(ACCOUNT_PUBLIC_KEY);

	final String BULLETIN_STATS_FILE_NAME = "bulletin";
	
	final String BULLETIN_SERVER_NAME = "server";
	final String BULLETIN_ORIGINAL_LOCAL_ID = "bulletin Original id";
	final String BULLETIN_CURRENT_LOCAL_ID = "bulletin id";
	final String BULLETIN_VERSION_NUMBER = "version number";
	final String BULLETIN_FINAL_VERSION = "final version";
	final String BULLETIN_MARTUS_BUILD_DATE = "Build Date";
	final String BULLETIN_MARTUS_BUILD_NUMBER = "Build Number";
	final String BULLETIN_TESTER = "test bulletin";
	final String BULLETIN_TYPE = "type";
	final String BULLETIN_SIZE = "size (Kb)";
	final String BULLETIN_ALL_PRIVATE = "all private";
	final String BULLETIN_AUTHOR = "author";
	final String BULLETIN_TITLE = "title";
	final String BULLETIN_SUMMARY = "summary";
	final String BULLETIN_LANGUAGE = "language";
	final String BULLETIN_LOCATION = "location";
	final String BULLETIN_KEYWORDS = "keywords";
	final String BULLETIN_DATE_CREATED = "date created";
	final String BULLETIN_DATE_EVENT = "event date";

	final String BULLETIN_PUBLIC_ATTACHMENT_COUNT = "public attachments";
	final String BULLETIN_PRIVATE_ATTACHMENT_COUNT = "private attachments";
	final String BULLETIN_ORIGINALLY_UPLOADED_TO_THIS_SERVER = "original server";
	final String BULLETIN_DATE_UPLOADED = "date uploaded";
	final String BULLETIN_TIME_UPLOADED = "time uploaded";
	final String BULLETIN_DATE_LAST_SAVED = "date last saved";
	final String BULLETIN_TIME_LAST_SAVED = "time last saved";
	final String BULLETIN_HAS_CUSTOM_FIELDS = "has custom fields";
	final String BULLETIN_CUSTOM_FIELD_TYPES = "custom field types";
	final String BULLETIN_HIDDEN = "hidden on server";
	final String BULLETIN_ALL_HQS_PROXY_UPLOAD = "all HQs proxy upload";
	final String BULLETIN_AUTHORIZED_TO_READ = "HQs authorized to read";
	final String BULLETIN_AUTHORIZED_TO_UPLOAD = "HQs authorized to upload";
	
	final String BULLETIN_ORIGINALLY_UPLOADED_TO_THIS_SERVER_TRUE = "1";
	final String BULLETIN_ORIGINALLY_UPLOADED_TO_THIS_SERVER_FALSE = "0";
	final String BULLETIN_DRAFT = "draft";
	final String BULLETIN_SEALED = "sealed";
	final String BULLETIN_ALL_PRIVATE_TRUE = "1";
	final String BULLETIN_ALL_PRIVATE_FALSE = "0";
	final String BULLETIN_ALL_HQS_PROXY_UPLOAD_TRUE = "1";
	final String BULLETIN_ALL_HQS_PROXY_UPLOAD_FALSE = "0";
	final String BULLETIN_HAS_CUSTOM_FIELDS_TRUE = "1";
	final String BULLETIN_HAS_CUSTOM_FIELDS_FALSE = "0";
	final String BULLETIN_HIDDEN_TRUE = "1"; 
	final String BULLETIN_HIDDEN_FALSE = "0"; 
	final String BULLETIN_TEST_TRUE = "1"; 
	final String BULLETIN_TEST_FALSE = "0";
	final String BULLETIN_FINAL_VERSION_TRUE = "1";
	final String BULLETIN_FINAL_VERSION_FALSE = "0";
	
	final String BULLETIN_STATISTICS_HEADER = 
		getNormalizedStringAndCheckForErrors(BULLETIN_SERVER_NAME) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_ORIGINAL_LOCAL_ID) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_CURRENT_LOCAL_ID) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_VERSION_NUMBER) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_FINAL_VERSION) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_MARTUS_BUILD_DATE) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_MARTUS_BUILD_NUMBER) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_TESTER) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_TYPE) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_SIZE) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_ALL_PRIVATE) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_HAS_CUSTOM_FIELDS) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_CUSTOM_FIELD_TYPES) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_AUTHOR) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_TITLE) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_SUMMARY) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_LANGUAGE) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_LOCATION) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_KEYWORDS) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_DATE_CREATED) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_DATE_EVENT) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_PUBLIC_ATTACHMENT_COUNT) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_PRIVATE_ATTACHMENT_COUNT) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_ORIGINALLY_UPLOADED_TO_THIS_SERVER) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_HIDDEN) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_DATE_UPLOADED) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_TIME_UPLOADED) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_DATE_LAST_SAVED) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_TIME_LAST_SAVED) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_ALL_HQS_PROXY_UPLOAD) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_AUTHORIZED_TO_READ) + DELIMITER +
		getNormalizedStringAndCheckForErrors(BULLETIN_AUTHORIZED_TO_UPLOAD) + DELIMITER +
		getNormalizedStringAndCheckForErrors(ACCOUNT_PUBLIC_CODE);

}
