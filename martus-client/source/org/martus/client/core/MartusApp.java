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

package org.martus.client.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.json.JSONObject;
import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.bulletinstore.ClientBulletinStore.AddOlderVersionToFolderFailedException;
import org.martus.client.bulletinstore.ClientBulletinStore.BulletinAlreadyExistsException;
import org.martus.client.core.templates.FormTemplateManager.UnableToLoadCurrentTemplateException;
import org.martus.client.network.OrchidTransportWrapperWithActiveProperty;
import org.martus.client.network.RetrieveCommand;
import org.martus.client.reports.ReportFormatFilter;
import org.martus.client.search.BulletinSearcher;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.swingui.EnglishStrings;
import org.martus.client.swingui.UiConstants;
import org.martus.client.test.MockClientSideNetworkHandler;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpc;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.clientside.PasswordHelper;
import org.martus.common.BulletinSummary;
import org.martus.common.BulletinSummary.WrongValueCount;
import org.martus.common.ContactKey;
import org.martus.common.ContactKeys;
import org.martus.common.Exceptions;
import org.martus.common.Exceptions.AccountNotFoundException;
import org.martus.common.Exceptions.NetworkOfflineException;
import org.martus.common.Exceptions.NoFormsAvailableException;
import org.martus.common.Exceptions.ServerCallFailedException;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.Exceptions.ServerNotCompatibleException;
import org.martus.common.FieldCollection;
import org.martus.common.FieldCollection.CustomFieldsParseException;
import org.martus.common.FieldDeskKey;
import org.martus.common.FieldDeskKeys;
import org.martus.common.FieldSpecCollection;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.LanguageSettingsProvider;
import org.martus.common.LegacyCustomFields;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusAccountAccessToken.TokenInvalidException;
import org.martus.common.MartusAccountAccessToken.TokenNotFoundException;
import org.martus.common.MartusConstants;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.BulletinNotFoundException;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.MartusUtilities.NotYourBulletinErrorException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.MiniLocalization;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinFromXFormsLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.EncryptionException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.FormTemplate.FutureVersionException;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.network.ClientSideNetworkInterface;
import org.martus.common.network.MartusOrchidDirectoryStore;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.NonSSLNetworkAPI;
import org.martus.common.network.NonSSLNetworkAPIWithHelpers;
import org.martus.common.network.ServerSideNetworkInterface;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongAccountException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.DateUtilities;
import org.martus.jarverifier.JarVerifier;
import org.martus.swing.FontHandler;
import org.martus.util.DatePreference;
import org.martus.util.DirectoryUtils;
import org.martus.util.MultiCalendar;
import org.martus.util.Stopwatch;
import org.martus.util.StreamCopier;
import org.martus.util.StreamableBase64;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeekThatClosesZipFile;

public class MartusApp
{
	
	public MartusApp(MtfAwareLocalization localizationToUse) throws MartusAppInitializationException
	{
		this(null, MartusConstants.determineMartusDataRootDirectory(), localizationToUse);
	}

	public MartusApp(MartusCrypto cryptoToUse, File dataDirectoryToUse, MtfAwareLocalization localizationToUse) throws MartusAppInitializationException
	{
		localization = localizationToUse;

		try
		{
			martusDataRootDirectory = dataDirectoryToUse;

			if(cryptoToUse == null)
				cryptoToUse = new MartusSecurity();

			verifyJarsIfPossible();

			configInfo = new ConfigInfo();
			currentUserName = "";
			maxNewFolders = MAXFOLDERS;
			store = new ClientBulletinStore(cryptoToUse);
			fieldExpansionStates = new HashMap();
			gridExpansionStates = new HashMap();
			if(shouldUseUnofficialTranslations())
				localization.includeOfficialLanguagesOnly = false;
			currentRetrieveCommand = new RetrieveCommand();
			orchidStore = new MartusOrchidDirectoryStore();
		}
		catch(MartusCrypto.CryptoInitializationException e)
		{
			throw new MartusAppInitializationException("ErrorCryptoInitialization");
		}
//		catch (MartusCrypto.InvalidJarException e)
//		{
//			throw new MartusAppInitializationException("Invalid jar file: " + e.getMessage());
//		}
//		catch (IOException e)
//		{
//			throw new MartusAppInitializationException("Error verifying jars: " + e.getMessage());
//		}
		catch (Exception e)
		{
			throw new MartusAppInitializationException("Error verifying jars: " + e.getMessage());
		}

		UpdateDocsIfNecessaryFromMLPFiles();
	}

	public File getOrchidCacheFile()
	{
		return new File(getCurrentAccountDirectory(), "OrchidCache.dat");
	}

	public OrchidTransportWrapperWithActiveProperty getTransport()
	{
		if(transport == null)
		{
			String errorText = "getTransport called before transport created";
			MartusLogger.logError(errorText);
			throw new RuntimeException(errorText);
		}
		return transport;
	}

	public static boolean isRunningFromJar() throws MalformedURLException
	{
		return getJarURL() != null;
	}

	public static boolean isJarSigned() throws IOException
	{
		return getSignatureFileJarEntry() != null;
	}

	private void verifyJarsIfPossible() throws Exception
	{
		if(isRunningFromJar())
			MartusJarVerification.verifyJars();
	}
	
	private static URL getJarURL() throws MalformedURLException
	{
		Class c = MartusApp.class;
		String name = c.getName();
		int lastDot = name.lastIndexOf('.');
		String classFileName = name.substring(lastDot + 1) + ".class";
		URL url = c.getResource(classFileName);
		String wholePath = url.toString();
		int bangAt = wholePath.indexOf('!');
		if(bangAt < 0)
			return null;
		
		String jarPart = wholePath.substring(0, bangAt+2);
		URL jarURL = new URL(jarPart);
		return jarURL;
	}
	
	private static JarEntry getSignatureFileJarEntry() throws IOException
	{
		URL jarUrl = getJarURL();
		System.out.println("Checking sig of " + jarUrl);
		JarURLConnection jarConnection = (JarURLConnection)jarUrl.openConnection();
		JarFile jf = jarConnection.getJarFile();
		JarEntry jarEntry = jf.getJarEntry("META-INF/SSMTSJAR.SF");
		System.out.println("Found sig entry: " + jarEntry);
		return jarEntry;
	}

	static public void setInitialUiDefaultsFromFileIfPresent(LanguageSettingsProvider settings, File defaultUiFile)
	{
		if(!defaultUiFile.exists())
			return;
		try
		{
			String languageCode = null;
			UnicodeReader in = new UnicodeReader(defaultUiFile);
			languageCode = in.readLine();
			in.close();
			
			if(MtfAwareLocalization.isRecognizedLanguage(languageCode))
			{
				MartusLogger.log("Setting default language: " + languageCode);
				DatePreference datePref = MiniLocalization.getDefaultDatePreferenceForLanguage(languageCode);
				MartusLogger.log("Setting default date fmt: " + datePref.getDateTemplate());
				settings.setCurrentLanguage(languageCode);
				settings.setDateFormatFromLanguage();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setServerInfo(String serverName, String serverKey, String serverCompliance) throws SaveConfigInfoException
	{
		configInfo.setServerName(serverName);
		configInfo.setServerPublicKey(serverKey);
		configInfo.setServerCompliance(serverCompliance);
		saveConfigInfo();

		invalidateCurrentHandlerAndGateway();
	}

	public ContactKeys getContactKeys() throws Exception
	{
		ContactKeys allContacts = new ContactKeys(configInfo.getContactKeysXml());
		return allContacts;
	}
	
	public Integer getKeyVerificationStatus(String publicKeyToCheck) throws Exception
	{
		if(publicKeyToCheck.equals(getAccountId()))
				return ContactKey.VERIFIED_ACCOUNT_OWNER;
		ContactKeys contacts = getContactKeys();
		for(int i = 0; i < contacts.size(); ++i)
		{
			ContactKey key = contacts.get(i);
			if(publicKeyToCheck.equals(key.getPublicKey()))
			{
				if(key.getVerificationStatus().equals(ContactKey.NOT_VERIFIED))
					return ContactKey.NOT_VERIFIED;
				else if(key.getVerificationStatus().equals(ContactKey.VERIFIED_ENTERED_20_DIGITS))
					return ContactKey.VERIFIED_ENTERED_20_DIGITS;
				else if(key.getVerificationStatus().equals(ContactKey.VERIFIED_VISUALLY))
					return ContactKey.VERIFIED_VISUALLY;
				return ContactKey.NOT_VERIFIED_UNKNOWN;	
			}
		}
		return ContactKey.NOT_VERIFIED_UNKNOWN;
	}

	public void setContactKeys(ContactKeys newContactKeys) throws SaveConfigInfoException 
	{
		try
		{
			configInfo.setContactKeysXml(newContactKeys.toString());
			saveConfigInfo();
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			throw new SaveConfigInfoException();
		}
	}

	public HeadquartersKeys getAllHQKeys() throws Exception
	{
		ContactKeys allContacts = new ContactKeys(configInfo.getContactKeysXml());
		HeadquartersKeys hqKeys = new HeadquartersKeys();
		for(int i = 0; i < allContacts.size(); ++i)
		{
			ContactKey currentContact = allContacts.get(i);
			hqKeys.add(new HeadquartersKey(currentContact));
		}
		return hqKeys;
	}

	public HeadquartersKeys getDefaultHQKeys() throws Exception
	{
		ContactKeys allContacts = new ContactKeys(configInfo.getContactKeysXml());
		HeadquartersKeys hqKeys = new HeadquartersKeys();
		for(int i = 0; i < allContacts.size(); ++i)
		{
			ContactKey currentContact = allContacts.get(i);
			if(currentContact.getSendToByDefault())
				hqKeys.add(new HeadquartersKey(currentContact));
		}
		return hqKeys;
	}

	public HeadquartersKeys getAllHQKeysWithFallback()
	{
		try
		{
			return getAllHQKeys();
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return new HeadquartersKeys();
		}
	}
	
	public HeadquartersKeys getDefaultHQKeysWithFallback()
	{
		try
		{
			return getDefaultHQKeys();
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return new HeadquartersKeys();
		}
	}

	public void addHQLabelsWherePossible(HeadquartersKeys keys)
	{
		for(int i = 0; i < keys.size(); ++i)
		{
			HeadquartersKey key = keys.get(i);
			key.setLabel(getHQLabelIfPresent(key));
		}
	}

	
	public String getHQLabelIfPresent(HeadquartersKey hqKey)
	{
		try
		{
			String hqLabelIfPresent = getAllHQKeys().getLabelIfPresent(hqKey);
			if(hqLabelIfPresent.length() == 0)
			{
				String publicCode = hqKey.getPublicKey();
				try
				{
					publicCode = hqKey.getFormattedPublicCode40();
				}
				catch (InvalidBase64Exception e)
				{
					e.printStackTrace();
				}
				String hqNotConfigured = localization.getFieldLabel("HQNotConfigured");
				hqLabelIfPresent = publicCode + " " + hqNotConfigured;
			}
			return hqLabelIfPresent;
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return "";
		}
	}

	public Vector<String> getFieldDeskPublicKeyStrings() throws Exception
	{
		FieldDeskKeys keys = getFieldDeskKeys();
		Vector<String> keyStrings = new Vector<String>();
		for(int i = 0; i < keys.size(); ++i)
		{
			String fieldDeskPublicKeyString = keys.get(i).getPublicKey();
			keyStrings.add(fieldDeskPublicKeyString);
		}
		
		return keyStrings;
	}

	public FieldDeskKeys getFieldDeskKeys()
	{
		FieldDeskKeys fdKeys = new FieldDeskKeys();
		try
		{
			ContactKeys allContacts = new ContactKeys(configInfo.getContactKeysXml());
			for(int i = 0; i < allContacts.size(); ++i)
			{
				ContactKey currentContact = allContacts.get(i);
				fdKeys.add(new FieldDeskKey(currentContact));
			}
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
		return fdKeys;
	}

	public boolean isVerifiedFieldDeskAccount(String authorPublicKeyString) throws Exception
	{
		Vector<String> fieldDeskPublicKeyStrings = getFieldDeskPublicKeyStrings();
		boolean isFieldDeskBulletin = fieldDeskPublicKeyStrings.contains(authorPublicKeyString);
		return isFieldDeskBulletin;
	}

	public ConfigInfo getConfigInfo()
	{
		return configInfo;
	}
	
	public void setAndSaveHQKeys(HeadquartersKeys allHQKeys, HeadquartersKeys defaultHQKeys) throws SaveConfigInfoException 
	{
		try
		{
			ContactKeys originalContacts = new ContactKeys(configInfo.getContactKeysXml());
			ContactKeys updatedContacts = getContactKeysWithCanSendAdjusted(allHQKeys, defaultHQKeys, originalContacts);
			addNewHQsOnlyToContacts(defaultHQKeys, updatedContacts, true);
			addNewHQsOnlyToContacts(allHQKeys, updatedContacts, false);
			configInfo.setContactKeysXml(updatedContacts.toString());
			saveConfigInfo();
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			throw new SaveConfigInfoException();
		}
	}
	
	public void setAndSaveFDKeys(FieldDeskKeys allFDKeys)
	{
		try
		{
			ContactKeys originalContacts = new ContactKeys(configInfo.getContactKeysXml());
			ContactKeys updatedContacts = getContactKeysWithCanReceiveFromAdjusted(allFDKeys, originalContacts);
			addNewFDsOnlyToContacts(allFDKeys, updatedContacts);
			configInfo.setContactKeysXml(updatedContacts.toString());
			saveConfigInfo();
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	private void addNewHQsOnlyToContacts(HeadquartersKeys allHQKeys, ContactKeys updatedContacts, boolean isDefaultHQ)
	{
		for(int i = 0; i < allHQKeys.size(); ++i)
		{
			HeadquartersKey currentHQ = allHQKeys.get(i);
			if(!updatedContacts.containsKey(currentHQ.getPublicKey()))
			{
				ContactKey newHQContact = new ContactKey(currentHQ);
				newHQContact.setSendToByDefault(isDefaultHQ);
				updatedContacts.add(newHQContact);
			}
		}
	}

	private ContactKeys getContactKeysWithCanSendAdjusted(HeadquartersKeys allHQKeys, HeadquartersKeys defaultKeys, ContactKeys originalContacts)
	{
		ContactKeys adjustedCanSendToKeys = new ContactKeys();
		for(int i = 0; i < originalContacts.size(); ++i)
		{
			ContactKey currentContact = originalContacts.get(i);
			String publicKey = currentContact.getPublicKey();
		
			if(allHQKeys.containsKey(publicKey))
				currentContact.setLabel(allHQKeys.getLabelIfPresent(publicKey));

			if(defaultKeys.containsKey(publicKey))
			{
				currentContact.setSendToByDefault(true);
				currentContact.setLabel(defaultKeys.getLabelIfPresent(publicKey));
			}
			else
			{
				currentContact.setSendToByDefault(false);
			}
				
			adjustedCanSendToKeys.add(currentContact);
		}
		return adjustedCanSendToKeys;
	}

	private void addNewFDsOnlyToContacts(FieldDeskKeys allFDKeys, ContactKeys updatedContacts)
	{
		for(int i = 0; i < allFDKeys.size(); ++i)
		{
			FieldDeskKey currentFD = allFDKeys.get(i);
			if(!updatedContacts.containsKey(currentFD.getPublicKey()))
			{
				ContactKey newFDContact = new ContactKey(currentFD);
				updatedContacts.add(newFDContact);
			}
		}
	}

	private ContactKeys getContactKeysWithCanReceiveFromAdjusted(FieldDeskKeys allFDKeys, ContactKeys originalContacts)
	{
		ContactKeys updatedContactKeysWithCanReceiveFromAdjusted = new ContactKeys();
		for(int i = 0; i < originalContacts.size(); ++i)
		{
			ContactKey currentContact = originalContacts.get(i);
			String publicKey = currentContact.getPublicKey();
			if(allFDKeys.containsKey(publicKey))
				currentContact.setLabel(allFDKeys.getLabelIfPresent(publicKey));
			updatedContactKeysWithCanReceiveFromAdjusted.add(currentContact);
		}
	return updatedContactKeysWithCanReceiveFromAdjusted;
	}

	public void updateFormTemplate(FormTemplate updatedTemplate) throws Exception
	{
		store.saveNewFormTemplate(updatedTemplate);
		store.selectFormTemplateAsDefault(updatedTemplate.getTitle());
	}

	public void saveConfigInfo() throws SaveConfigInfoException
	{
		File file = getConfigInfoFile();
		File signatureFile = getConfigInfoSignatureFile();

		try
		{
			ByteArrayOutputStream encryptedConfigOutputStream = new ByteArrayOutputStream();
			configInfo.save(encryptedConfigOutputStream);
			byte[] encryptedInfo = encryptedConfigOutputStream.toByteArray();
			encryptAndWriteFileAndSignatureFile(file, signatureFile, encryptedInfo);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			throw new SaveConfigInfoException();
		}

		turnNetworkOnOrOffAsRequested();
		startOrStopTorAsRequested();
	}
	
	public boolean shouldWeAskForKeypairBackup()
	{
		return shouldWeAskForKeypairBackup(DateUtilities.getTodayInStoredFormat());
	}

	public boolean shouldWeAskForKeypairBackup(String today)
	{
		String dateLastAskedForKeypairBackup = configInfo.getDateLastAskedUserToBackupKeypair();
		if(dateLastAskedForKeypairBackup.isEmpty())
			return false;
		
		MultiCalendar todayCalendar = MultiCalendar.createFromIsoDateString(today);
		MultiCalendar lastNoticeCalendar = MultiCalendar.createFromIsoDateString(dateLastAskedForKeypairBackup);
		
		int daysFromLastNotice = MultiCalendar.daysBetween(lastNoticeCalendar, todayCalendar);
		return (daysFromLastNotice >= DAYS_UNTIL_WE_ASK_TO_BACKUP_KEYPAIR);
	}
	
	public void startClockToAskForKeypairBackup() throws SaveConfigInfoException
	{
		String today = DateUtilities.getTodayInStoredFormat();
		configInfo.setDateLastAskedUserToBackupKeypair(today);
		saveConfigInfo();
	}
	
	public void clearClockToAskForKeypairBackup() throws SaveConfigInfoException
	{
		configInfo.setDateLastAskedUserToBackupKeypair("");
		saveConfigInfo();
	}

	public void turnNetworkOnOrOffAsRequested()
	{
		boolean shouldBeOnline = configInfo.getOnStartupServerOnlineStatus();
		turnNetworkOnOrOff(shouldBeOnline);
	}

	public void turnNetworkOnOrOff(boolean shouldBeOnline)
	{
		getTransport().setIsOnline(shouldBeOnline);
		MartusLogger.log("Online status set to " + shouldBeOnline);
	}

	public void saveStateWithoutPrompting() throws Exception
	{
		orchidStore.saveStore(getOrchidCacheFile(), getSecurity());
	}

	public void encryptAndWriteFileAndSignatureFile(File file, File signatureFile, byte[] plainText) throws Exception
	{
		getSecurity().encryptAndWriteFileAndSignatureFile(file, signatureFile, plainText);
	}

	public void loadConfigInfo() throws LoadConfigInfoException
	{
		configInfo.clear();

		File sigFile = getConfigInfoSignatureFile();
		File dataFile = getConfigInfoFile();

		if(!dataFile.exists())
		{
			//System.out.println("MartusApp.loadConfigInfo: config file doesn't exist");
			return;
		}

		try
		{
			byte[] plainTextConfigInfo = verifyAndReadSignedFile(dataFile, sigFile);
			ByteArrayInputStream plainTextConfigInputStream = new ByteArrayInputStream(plainTextConfigInfo);
			configInfo = ConfigInfo.load(plainTextConfigInputStream);
			plainTextConfigInputStream.close();
			
			String languageCode = localization.getCurrentLanguageCode();
			
			boolean useZawgyiFont = (languageCode != null && languageCode.equals(MtfAwareLocalization.BURMESE));
			configInfo.setUseZawgyiFont(useZawgyiFont);
			FontSetter.setDefaultFont(useZawgyiFont);
			FontHandler.setDoZawgyiConversion(configInfo.getDoZawgyiConversion());
			
			if(configInfo.getVersion() < ConfigInfo.VERSION_WITH_CONTACT_KEYS)
				migrateToUsingContactKeys();
		}
		catch (Exception e)
		{
			throw new LoadConfigInfoException(e);
		}
	}
	
	public void startOrStopTorAsRequested()
	{
		boolean isTorEnabled = getConfigInfo().useInternalTor();
		int newTimeout = 0;
		if(isTorEnabled)
			newTimeout = ClientSideNetworkHandlerUsingXmlRpc.TOR_GET_SERVER_INFO_TIMEOUT_SECONDS;
		else
			newTimeout = ClientSideNetworkHandlerUsingXmlRpc.WITHOUT_TOR_GET_SERVER_INFO_TIMEOUT_SECONDS;

		// NOTE: force the handler to be created if it wasn't already
		getCurrentNetworkInterfaceHandler();
		boolean isServerConfigured = (currentNetworkInterfaceHandler != null);
		if(isServerConfigured)
			currentNetworkInterfaceHandler.setTimeoutGetServerInfo(newTimeout);

		if(isTorEnabled)
		{
			getTransport().startTor();
		}
		else
		{
			getTransport().stopTor();
		}
	}

	private byte[] verifyAndReadSignedFile(File dataFile, File sigFile) throws Exception
	{
		MartusCrypto security = getSecurity();
		
		return MartusCrypto.verifySignatureAndDecryptFile(dataFile, sigFile, security);
	}

	private boolean isSignatureFileValid(File dataFile, File sigFile, String accountId) throws Exception 
	{
		MartusCrypto security = getSecurity();
		return MartusCrypto.isSignatureFileValid(dataFile, sigFile, accountId, security);
	}

	public void writeSignedUserDictionary(String string) throws Exception
	{
		encryptAndWriteFileAndSignatureFile(getDictionaryFile(), getDictionarySignatureFile(), string.getBytes("UTF-8"));
	}
	
	public String readSignedUserDictionary() throws Exception
	{
		File dictionaryFile = getDictionaryFile();
		File dictionarySignatureFile = getDictionarySignatureFile();
		
		if(!dictionaryFile.exists())
			return "";
		
		byte[] plainText = verifyAndReadSignedFile(dictionaryFile, dictionarySignatureFile);
		return new String(plainText, "UTF-8");
	}

	public static void removeSpaceLikeCharactersFromTags(FieldSpecCollection specs)
	{
		for(int i = 0; i < specs.size(); ++i)
		{
			String tag = specs.get(i).getTag();
			String stripped = stripSpaceLikeCharacters(tag);
			specs.get(i).setTag(stripped);
		}
	}
	
	public static String stripSpaceLikeCharacters(String tag)
	{
		String NORMAL_WHITESPACE = "\\s";
		String NON_BREAKING_SPACE = "\\xa0";
		String NARROW_NO_BREAK_SPACE = "\\u202f";
		String ZERO_WIDTH_NO_BREAK_SPACE = "\\ufeff";
		String WORD_JOINER = "\\u2060";
		String regex = "[" + NORMAL_WHITESPACE + NON_BREAKING_SPACE + 
				NARROW_NO_BREAK_SPACE + ZERO_WIDTH_NO_BREAK_SPACE + WORD_JOINER + "]";
		return tag.replaceAll(regex, "");
	}

	
	private void migrateToUsingContactKeys() throws Exception
	{
		String legacyHQKey = configInfo.getLegacyHQKey();
		String ourPublicKey = getAccountId();
		if(legacyHQKey.equals(ourPublicKey))
			legacyHQKey = "";
		HeadquartersKeys deprecatedHqKeys = new HeadquartersKeys(configInfo.getAllHQKeysXml());
		HeadquartersKeys deprecatedDefaultHqKeys = new HeadquartersKeys(configInfo.getDefaultHQKeysXml());
		if(legacyHQKey.length() > 0)
		{
			HeadquartersKey legacyHQ = new HeadquartersKey(legacyHQKey);
			if(!deprecatedDefaultHqKeys.containsKey(legacyHQKey))
				deprecatedDefaultHqKeys.add(legacyHQ);
		}
		ContactKeys contactKeys = new ContactKeys();

		for(int i = 0; i < deprecatedDefaultHqKeys.size(); ++i)
		{
			HeadquartersKey defaultHQKeyToAdd = deprecatedDefaultHqKeys.get(i);
			String publicKey = defaultHQKeyToAdd.getPublicKey();
			if(!publicKey.equals(ourPublicKey) && !contactKeys.containsKey(publicKey))
			{
				ContactKey hqContactKey = new ContactKey(defaultHQKeyToAdd);
				hqContactKey.setSendToByDefault(true);
				hqContactKey.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
				contactKeys.add(hqContactKey);
			}
		}
		
		for(int i = 0; i < deprecatedHqKeys.size(); ++i)
		{
			HeadquartersKey hqKeyToAdd = deprecatedHqKeys.get(i);
			String publicKey = hqKeyToAdd.getPublicKey();
			if(!publicKey.equals(ourPublicKey)  && !contactKeys.containsKey(publicKey))
			{
				ContactKey hqContactKey = new ContactKey(hqKeyToAdd);
				hqContactKey.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
				contactKeys.add(hqContactKey);
			}
		}

		FieldDeskKeys deprecatedFieldDeskKeys = new FieldDeskKeys(configInfo.getFieldDeskKeysXml());
		for(int i = 0; i < deprecatedFieldDeskKeys.size(); ++i)
		{
			FieldDeskKey fdKeyToAdd = deprecatedFieldDeskKeys.get(i);
			String publicKey = fdKeyToAdd.getPublicKey();
			if(!publicKey.equals(ourPublicKey) && !contactKeys.containsKey(publicKey))
			{
				ContactKey fdContactKey = new ContactKey(fdKeyToAdd);
				fdContactKey.setVerificationStatus(ContactKey.VERIFIED_ENTERED_20_DIGITS);
				contactKeys.add(fdContactKey);
			}
		}
		
		configInfo.setContactKeysXml(contactKeys.toString());
		configInfo.setAllHQKeysXml("");
		configInfo.setFieldDeskKeysXml("");
		configInfo.setDefaultHQKeysXml("");
		configInfo.clearLegacyHQKey(); 
		saveConfigInfo();
		loadConfigInfo();
	}

	public static FieldSpecCollection getCustomFieldSpecsTopSection(ConfigInfo configInfo) throws Exception
	{
		String xmlSpecs = configInfo.getNoLongerUsedCustomFieldTopSectionXml();
		if(xmlSpecs.length() > 0)
			return FieldCollection.parseXml(xmlSpecs);
			
		String legacySpecs = configInfo.getCustomFieldLegacySpecs();
		if(legacySpecs.length() > 0)
			return LegacyCustomFields.parseFieldSpecsFromString(legacySpecs);
		
		return StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
	}

	public static FieldSpecCollection getCustomFieldSpecsBottomSection(ConfigInfo configInfo) throws CustomFieldsParseException
	{
		String xmlSpecs = configInfo.getNoLongerUsedCustomFieldBottomSectionXml();
		if(xmlSpecs.length() > 0)
			return FieldCollection.parseXml(xmlSpecs);
			
		FieldSpecCollection specs = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		return specs;
	}

	public void doAfterSigninInitalization() throws Exception
	{
		doAfterSigninInitalization(getCurrentAccountDirectory(), store.createDatabase(getCurrentAccountDirectory()));
	}
	
	public void doAfterSigninInitalization(File dataDirectory,	Database database) throws Exception
	{
		if(isInitialized)
		{
			MartusLogger.log("MartusApp.doAfterSigninInitalization called again");
			return;
		}
		
		initializeOrchid();

		try
		{
			store.doAfterSigninInitialization(dataDirectory, database);

			if(!configInfo.getDidTemplateMigration())
				migrateTemplateToFormTemplateManager();
		}
		catch(FileVerificationException e)
		{
			throw(e);
		}
		catch(MissingAccountMapException e)
		{
			throw(e);
		}
		catch(MissingAccountMapSignatureException e)
		{
			throw(e);
		}
		catch(UnableToLoadCurrentTemplateException e)
		{
			throw(e);
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			throw new MartusAppInitializationException("Error initializing store");
		}
		
	}

	public void initializeOrchid() throws MartusAppInitializationException
	{
		try
		{
			orchidStore.loadStore(getOrchidCacheFile(), getSecurity());
			transport = OrchidTransportWrapperWithActiveProperty.create(orchidStore);
			turnNetworkOnOrOffAsRequested();
			startOrStopTorAsRequested();

			isInitialized = true;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			throw new MartusAppInitializationException("Error initializing Tor transport");
		}
	}

	private void migrateTemplateToFormTemplateManager() throws Exception
	{
		if(configInfo.hasLegacyFormTemplate())
		{
			MartusLogger.log("Migrating template from config");
			FormTemplate template = configInfo.getLegacyFormTemplate();
			if(template.getTitle().length() == 0)
			{
				template.setTitle(localization.getFieldLabel("NoFormTemplateTitle"));
			}
			store.saveNewFormTemplate(template);
		}
		
		configInfo.setDidTemplateMigration(true);
		saveConfigInfo();
	}

	public File getFxmlDirectory()
	{
		return new File (getMartusDataRootDirectory(), FXML_DIRECTORY_NAME);
	}
	
	public File getMartusDataRootDirectory()
	{
		return martusDataRootDirectory;
	}

	public File getCurrentAccountDirectory()
	{
		return currentAccountDirectory;
	}
	
	public File getPacketsDirectory()
	{
		return new File(getCurrentAccountDirectory(), PACKETS_DIRECTORY_NAME);
	}
	
	public File getAccountsDirectory()
	{
		return new File(getMartusDataRootDirectory(), ACCOUNTS_DIRECTORY_NAME);
	}
	
	public boolean shouldUseUnofficialTranslations()
	{
		return (new File(getMartusDataRootDirectory(), USE_UNOFFICIAL_TRANSLATIONS_NAME)).exists();
	}
	
	public File getDocumentsDirectory()
	{
		return new File(getMartusDataRootDirectory(), DOCUMENTS_DIRECTORY_NAME);
	}

	public String getCurrentAccountDirectoryName()
	{
		return getCurrentAccountDirectory().getPath() + "/";
	}

	public File getConfigInfoFile()
	{
		return getConfigInfoFileForAccount(getCurrentAccountDirectory());
	}
	
	public File getConfigInfoFileForAccount(File accountDirectory)
	{
		return new File(accountDirectory, "MartusConfig.dat");
	}

	public File getConfigInfoSignatureFile()
	{
		return getConfigInfoSignatureFileForAccount(getCurrentAccountDirectory());
	}

	public File getConfigInfoSignatureFileForAccount(File accountDirectory)
	{
		return new File(accountDirectory, "MartusConfig.sig");
	}

	public File getDictionaryFile()
	{
		return getDictionaryFileForAccount(getCurrentAccountDirectory());
	}
	
	public File getDictionaryFileForAccount(File accountDirectory)
	{
		return new File(accountDirectory, "Dictionary.dat");
	}

	public File getDictionarySignatureFile()
	{
		return getDictionarySignatureFileForAccount(getCurrentAccountDirectory());
	}

	public File getDictionarySignatureFileForAccount(File accountDirectory)
	{
		return new File(accountDirectory, "Dictionary.sig");
	}

	public File getUploadInfoFile()
	{
		return getUploadInfoFileForAccount(getCurrentAccountDirectory());
	}

	public File getUploadInfoFileForAccount(File accountDirectory)
	{
		return new File(accountDirectory, "MartusUploadInfo.dat");
	}

	public File getUiStateFileForAccount(File accountDirectory)
	{
		return new File(accountDirectory, "UserUiState.dat");
	}
	
	public File getBulletinDefaultDetailsFile()
	{
		return new File(getCurrentAccountDirectoryName(), "DefaultDetails" + DEFAULT_DETAILS_EXTENSION);
	}

	public File getRetrieveFile()
	{
		return getRetrieveFile(getCurrentAccountDirectory());
	}

	private static File getRetrieveFile(File accountDirectory)
	{
		return new File(accountDirectory, "Retrieve.dat");
	}

	public String getLegacyUploadLogFilename()
	{
		return getLegacyUploadLogFileForAccount(getCurrentAccountDirectory()).getAbsolutePath();
	}

	private File getLegacyUploadLogFileForAccount(File accountDirectory)
	{
		return new File(accountDirectory, "MartusUploadLog.txt");
	}

	public InputStream getHelpMain(String currentLanguageCode)
	{
		return getHelp(currentLanguageCode, getHelpFilename(currentLanguageCode));
	}
	
	public InputStream getHelpTOC(String currentLanguageCode)
	{
		return getHelp(currentLanguageCode, getHelpTOCFilename(currentLanguageCode));
	}
	
	public URL getUrlOfDirectoryContainingDictionaries(String currentLanguageCode)
	{
		String dictionaryName = "dictionary_" + currentLanguageCode + ".ortho";
		try 
		{
			File mlpFile = localization.getMlpkFile(currentLanguageCode);
			if(mlpFile.exists() && 
			   JarVerifier.verify(mlpFile,false) == JarVerifier.JAR_VERIFIED_TRUE)
			{
				URL url = new URL("jar:file:/" + mlpFile.getAbsolutePath() + "!" + "dictionaries/" + dictionaryName);
				return url;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		String dictionaryDirectory = "";
		URL relativeURL = EnglishStrings.class.getResource(dictionaryDirectory + dictionaryName);
		if(relativeURL == null)
			dictionaryDirectory = "/";
		return EnglishStrings.class.getResource(dictionaryDirectory);
		
	}
	
	private InputStream getHelp(String currentLanguageCode, String helpFileName)
	{
		if(!localization.isOfficialTranslation(currentLanguageCode))
			return null;

		try 
		{
			File mlpFile = localization.getMlpkFile(currentLanguageCode);
			if(mlpFile.exists() && 
			   JarVerifier.verify(mlpFile,false) == JarVerifier.JAR_VERIFIED_TRUE)
			{
				ZipFile zip = new ZipFile(mlpFile);
				ZipEntry zipEntry = zip.getEntry(helpFileName);
				ZipEntryInputStreamWithSeekThatClosesZipFile stream = new ZipEntryInputStreamWithSeekThatClosesZipFile(zip, zipEntry);
				return stream;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return EnglishStrings.class.getResourceAsStream(helpFileName);
	}
	
	public String getHelpFilename(String languageCode)
	{
		String helpFile = "MartusHelp-" + languageCode + ".txt";
		return helpFile;
	}

	public String getHelpTOCFilename(String languageCode)
	{
		String helpFile = "MartusHelpTOC-" + languageCode + ".txt";
		return helpFile;
	}
	
	public void UpdateDocsIfNecessaryFromMLPFiles()
	{
		File[] mlpFiles = GetMlpFiles();
		for(int i = 0; i < mlpFiles.length; ++i)
		{
			File mlpFile = mlpFiles[i];
			extractNewerPDFDocumentation(mlpFile);
			extractNewerReadMeDocumentation(mlpFile);
		}
	}

	private void extractNewerReadMeDocumentation(File mlpFile)
	{
		File targetDirectory = getMartusDataRootDirectory();
		String readMeFiles = "README";
		String fileExtension = ".txt";
		extractMatchingFileTypesFromJar(mlpFile, targetDirectory, readMeFiles, fileExtension);
	}

	private void extractNewerPDFDocumentation(File mlpFile)
	{
		File targetDirectory = getDocumentsDirectory();
		String anyPdfFile = "";
		String fileExtension = ".pdf";
		extractMatchingFileTypesFromJar(mlpFile, targetDirectory, anyPdfFile, fileExtension);
	}

	private void extractMatchingFileTypesFromJar(File mlpFile, File targetDirectory, String filesBeginningWith, String filesEndingWith)
	{
		if(JarVerifier.verify(mlpFile, false) != JarVerifier.JAR_VERIFIED_TRUE)
		{
			MartusLogger.logError("Jar verification failed when extracting files from jar: " + filesBeginningWith + "*" + filesEndingWith);
			return;
		}
		JarFile jar = null;
		try
		{
			jar = new JarFile(mlpFile);
			Enumeration entries = jar.entries();
			while(entries.hasMoreElements())
			{
				JarEntry entry = (JarEntry) entries.nextElement();
				String jarEntryName = entry.getName();
				if(filesBeginningWith.length() > 0)
				{
					if(!jarEntryName.startsWith(filesBeginningWith))
						continue;
				}
				if(!jarEntryName.endsWith(filesEndingWith))
					continue;
				File fileOnDisk = new File(targetDirectory, jarEntryName);
				if(isFileNewerOnDisk(fileOnDisk, entry))
					continue;
					
				fileOnDisk.delete();
				targetDirectory.mkdirs();
				copyJarEntryToFile(jar, entry, fileOnDisk);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(jar != null)
					jar.close();
			}
			catch(IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}
	

	public boolean isFileNewerOnDisk(File fileToCheck, ZipEntry entry)
	{
		if(!fileToCheck.exists())
			return false;
		Date zipFileDate = new Date(entry.getTime());
		Date currentFileDate = new Date(fileToCheck.lastModified());
		return(zipFileDate.before(currentFileDate));
	}

	private void copyJarEntryToFile(JarFile jar, JarEntry entry, File outputFile) throws IOException, FileNotFoundException
	{
		InputStream in = jar.getInputStream(entry);
		FileOutputStream out = new FileOutputStream(outputFile);
		StreamCopier copier = new StreamCopier();
		copier.copyStream(in, out);
		//TODO put closes in a finally block.
		in.close();
		out.close();
		outputFile.setLastModified(entry.getTime());
	}

	public static File getTranslationsDirectory()
	{
		return MartusConstants.determineMartusDataRootDirectory();
	}

	public File getCurrentKeyPairFile()
	{
		File dir = getCurrentAccountDirectory();
		return getKeyPairFile(dir);
	}

	public File getKeyPairFile(File dir)
	{
		return new File(dir, KEYPAIR_FILENAME);
	}	

	public static File getBackupFile(File original)
	{
		return new File(original.getPath() + ".bak");
	}
	
	public String getUserName()
	{
		return currentUserName;
	}

	public void loadFolders()
	{
		store.loadFolders();
	}

	public ClientBulletinStore getStore()
	{
		return store;
	}
	
	public RetrieveCommand getCurrentRetrieveCommand()
	{
		return currentRetrieveCommand;
	}

	public MtfAwareLocalization getLocalization()
	{
		return localization;
	}

	public void startBackgroundRetrieve(RetrieveCommand rc) throws MartusSignatureException, NoKeyPairException, EncryptionException, IOException
	{
		currentRetrieveCommand = rc;
		saveRetrieveCommand();
	}
	
	public void loadRetrieveCommand() throws Exception
	{
		final File retrieveFile = getRetrieveFile();
		if(!retrieveFile.exists())
			return;

		int size = (int)retrieveFile.length();
		byte[] bundle = new byte[size];
		FileInputStream in = new FileInputStream(retrieveFile);
		try
		{
			in.read(bundle);
		}
		finally
		{
			in.close();
		}
		startBackgroundRetrieve(parseRetrieveCommandBundle(bundle));
	}

	private void saveRetrieveCommand() throws MartusSignatureException, IOException, NoKeyPairException, EncryptionException, FileNotFoundException
	{
		byte[] retrieveCommandBytes = createRetrieveCommandBundle(getCurrentRetrieveCommand());
		FileOutputStream out = new FileOutputStream(getRetrieveFile());
		try
		{
			out.write(retrieveCommandBytes);
		}
		finally
		{
			out.close();
		}
	}
	
	public void cancelBackgroundRetrieve() throws MartusSignatureException, NoKeyPairException, EncryptionException, IOException
	{
		startBackgroundRetrieve(new RetrieveCommand());
	}
	
	public byte[] createRetrieveCommandBundle(RetrieveCommand rc) throws MartusSignatureException, IOException, NoKeyPairException, EncryptionException
	{
		try
		{
			byte[] plainText = rc.toJson().toString().getBytes("UTF-8");
			ByteArrayOutputStream encryptedBytes = new ByteArrayOutputStream();
			MartusCrypto security = store.getSignatureGenerator();
			security.encrypt(new ByteArrayInputStream(plainText), encryptedBytes);
			return security.createSignedBundle(encryptedBytes.toByteArray());
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public RetrieveCommand parseRetrieveCommandBundle(byte[] bundle) throws Exception
	{
		MartusCrypto security = store.getSignatureGenerator();
		byte[] encryptedBytes = security.extractFromSignedBundle(bundle);
		ByteArrayOutputStream plainTextBytes = new ByteArrayOutputStream();
		security.decrypt(new ByteArrayInputStreamWithSeek(encryptedBytes), plainTextBytes);
		String jsonString = new String(plainTextBytes.toByteArray(), "UTF-8");
		return new RetrieveCommand(new JSONObject(jsonString));
	}

	public Bulletin createBulletin() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGAUTHOR, configInfo.getAuthor());
		b.set(Bulletin.TAGORGANIZATION, configInfo.getOrganization());
		b.set(Bulletin.TAGPUBLICINFO, configInfo.getTemplateDetails());
		b.set(Bulletin.TAGLANGUAGE, getDefaultLanguageForNewBulletin());
		setDefaultHQKeysInBulletin(b);
		b.setMutable();
		b.setAllPrivate(true);
		return b;
	}

	public String getDefaultLanguageForNewBulletin()
	{
		final String preferredLanguage = getCurrentLanguage();
		ChoiceItem[] availableLanguages = localization.getLanguageNameChoices();
		for(int i=0; i < availableLanguages.length; ++i)
		{
			ChoiceItem item = availableLanguages[i];
			if(item.getCode().equals(preferredLanguage))
				return preferredLanguage;
		}
		
		return MiniLocalization.LANGUAGE_OTHER;
	}

	public void setDefaultHQKeysInBulletin(Bulletin b)
	{
		HeadquartersKeys hqKeys = getDefaultHQKeysWithFallback();
		b.setAuthorizedToReadKeys(hqKeys);
	}

	public BulletinFolder getFolderSaved()
	{
		return store.getFolderSaved();
	}
	
	public BulletinFolder getFolderRetrieved()
	{
		return store.createOrFindFolder(getNameOfFolderForAllRetrieved());
	}

	public BulletinFolder getFolderDiscarded()
	{
		return store.getFolderDiscarded();
	}

	public BulletinFolder getFolderSealedOutbox()
	{
		return store.getFolderSealedOutbox();
	}

	public BulletinFolder getFolderDraftOutbox()
	{
		return store.getFolderDraftOutbox();
	}

	public BulletinFolder createFolderRetrieved()
	{
		String folderName = getNameOfFolderRetrievedSealed();
		return createOrFindFolder(folderName);
	}

	public BulletinFolder createFolderRetrievedFieldOffice()
	{
		String folderName = getNameOfFolderRetrievedFieldOfficeSealed();
		return createOrFindFolder(folderName);
	}
	
	public String getNameOfFolderForAllRetrieved()
	{
		return store.getNameOfFolderForAllRetrieved();
	}

	public String getNameOfFolderRetrievedSealed()
	{
		return store.getNameOfFolderRetrievedSealed();
	}

	public String getNameOfFolderRetrievedDraft()
	{
		return store.getNameOfFolderRetrievedDraft();
	}

	public String getNameOfFolderRetrievedFieldOfficeSealed()
	{
		return store.getNameOfFolderRetrievedFieldOfficeSealed();
	}

	public String getNameOfFolderRetrievedFieldOfficeDraft()
	{
		return store.getNameOfFolderRetrievedFieldOfficeDraft();
	}

	public BulletinFolder createOrFindFolder(String name)
	{
		return store.createOrFindFolder(name);
	}

	public BulletinFolder findFolder(String name)
	{
		return store.findFolder(name);
	}

	public void setMaxNewFolders(int numFolders)
	{
		maxNewFolders = numFolders;
	}

	public BulletinFolder createUniqueFolder(String originalFolderName)
	{
		BulletinFolder newFolder = null;
		String uniqueFolderName = null;
		int folderIndex = 0;
		while (newFolder == null && folderIndex < maxNewFolders)
		{
			uniqueFolderName = originalFolderName;
			if (folderIndex > 0)
				uniqueFolderName += folderIndex;
			newFolder = store.createFolder(uniqueFolderName);
			++folderIndex;
		}
		if(newFolder != null)
			store.saveFolders();
		return newFolder;
	}
	
	public boolean isAnyBulletinsUnsent(UniversalId[] bulletinIds)
	{
		BulletinFolder draftOutBox = getFolderDraftOutbox();
		BulletinFolder sealedOutBox = getFolderSealedOutbox();
		for (int i = 0; i < bulletinIds.length; i++)
		{
			UniversalId uid = bulletinIds[i];
			if(draftOutBox.contains(uid) || sealedOutBox.contains(uid))
				return true;
		}
		return false;
	}
	
	public Vector getNonDiscardedFoldersForBulletins(UniversalId[] bulletinIds)
	{
		BulletinFolder discardedFolder = getFolderDiscarded();
		Vector visibleFoldersContainingAnyBulletin = new Vector();
		for (int i = 0; i < bulletinIds.length; i++)
		{
			UniversalId uid = bulletinIds[i];
			Vector visibleFoldersContainingThisBulletin = findBulletinInAllVisibleFolders(uid);
			visibleFoldersContainingThisBulletin.remove(discardedFolder);
			addUniqueEntriesOnly(visibleFoldersContainingAnyBulletin, visibleFoldersContainingThisBulletin);
		}
		return visibleFoldersContainingAnyBulletin;
	}

	private void addUniqueEntriesOnly(Vector to, Vector from)
	{
		for(int i = 0 ; i < from.size(); ++i)
		{
			Object elementToAdd = from.get(i);
			if(!to.contains(elementToAdd))
				to.add(elementToAdd);
		}
	}
	
	public void cleanupWhenCompleteQuickErase()
	{
		store.deleteFoldersDatFile();	
	}
	
	public void deleteKeypairAndRelatedFilesForAccount(File accountDirectory)
	{
		File keyPairFile = getKeyPairFile(accountDirectory);
		DirectoryUtils.scrubAndDeleteFile(keyPairFile);
		DirectoryUtils.scrubAndDeleteFile(getBackupFile(keyPairFile));
		DirectoryUtils.scrubAndDeleteFile(getUserNameHashFile(keyPairFile.getParentFile()));
		DirectoryUtils.scrubAndDeleteFile(getConfigInfoFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(getConfigInfoSignatureFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(getUploadInfoFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(getUiStateFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(ClientBulletinStore.getFoldersFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(ClientBulletinStore.getCacheFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(ClientBulletinStore.getFieldSpecCacheFile(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(getRetrieveFile(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(getDictionaryFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(getDictionarySignatureFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(getLegacyUploadLogFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(new File(accountDirectory, "velocity.log"));

		File[] exportedKeys = exportedPublicKeyFiles(accountDirectory);
		for (int i = 0; i < exportedKeys.length; i++)
		{
			File file = exportedKeys[i];
			DirectoryUtils.scrubAndDeleteFile(file);
		}

		File[] reportFormats = reportFormatFiles(accountDirectory, getLocalization());
		for (int i = 0; i < reportFormats.length; i++)
		{
			File file = reportFormats[i];
			DirectoryUtils.scrubAndDeleteFile(file);
		}
	}

	private static File[] exportedPublicKeyFiles(File accountDir)
	{
		File[] mpiFiles = accountDir.listFiles(new FileFilter()
		{
			public boolean accept(File file)
			{
				return (file.isFile() && file.getName().endsWith(".mpi"));	
			}
		});
		return mpiFiles;
	}

	private static File[] reportFormatFiles(File accountDir, MiniLocalization localization)
	{
		File[] reportFormatFiles = accountDir.listFiles(new ReportFormatFilter(localization));
		return reportFormatFiles;
	}

	private File[] GetMlpFiles()
	{
		File[] mpiFiles = martusDataRootDirectory.listFiles(new FileFilter()
		{
			public boolean accept(File file)
			{
				return (file.isFile() && file.getName().endsWith(MtfAwareLocalization.MARTUS_LANGUAGE_PACK_SUFFIX));	
			}
		});
		return mpiFiles;
	}

	public boolean deleteAllBulletinsAndUserFolders()
	{
		try
		{											
			store.scrubAllData();
			store.deleteAllData();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public int quarantineUnreadableBulletins()
	{
		return store.quarantineUnreadableBulletins();
	}

	public int repairOrphans()
	{
		Set orphans = store.getSetOfOrphanedBulletinUniversalIds();
		int foundOrphanCount = orphans.size();
		if(foundOrphanCount == 0)
			return 0;

		Iterator it = orphans.iterator();
		while(it.hasNext())
		{
			UniversalId uid = (UniversalId)it.next();
			try
			{
				store.addRepairBulletinToFolders(uid);
			}
			catch (BulletinAlreadyExistsException e)
			{
				System.out.println("Orphan Bulletin already exists.");
			}
			catch (IOException shouldNeverHappen)
			{
				shouldNeverHappen.printStackTrace();
			}
		}

		store.saveFolders();
		return foundOrphanCount;
	}

	public boolean isFieldExpanded(String tag) 
	{
		Boolean isExpanded = (Boolean)fieldExpansionStates.get(tag);
		if(isExpanded == null)
			return true;
		return isExpanded.booleanValue();
	}

	public void setFieldExpansionState(String tag, boolean b) 
	{
		fieldExpansionStates.put(tag, new Boolean(b));
	}
	
	public boolean isGridExpanded(String tag)
	{
		Boolean isExpanded = (Boolean)gridExpansionStates.get(tag);
		if(isExpanded == null)
			return false;
		return isExpanded.booleanValue();
	}
	
	public void setGridExpansionState(String tag, boolean b)
	{
		gridExpansionStates.put(tag, new Boolean(b));
	}

	public Vector findBulletinInAllVisibleFolders(Bulletin b)
	{
		return findBulletinInAllVisibleFolders(b.getUniversalId());
	}

	public Vector findBulletinInAllVisibleFolders(UniversalId uid)
	{
		return store.findBulletinInAllVisibleFolders(uid);
	}

	public boolean isDraftOutboxEmpty()
	{
		if(getFolderDraftOutbox().getBulletinCount() == 0)
			return true;
		return false;
	}

	public boolean isSealedOutboxEmpty()
	{
		if(getFolderSealedOutbox().getBulletinCount() == 0)
			return true;
		return false;
	}
	
	public void discardBulletinsFromFolder(BulletinFolder folderToDiscardFrom, UniversalId[] bulletinIDsToDiscard) throws IOException 
	{
		store.getFolderDiscarded().prepareForBulkOperation();
		for (int i = 0; i < bulletinIDsToDiscard.length; i++)
		{
			UniversalId uid = bulletinIDsToDiscard[i];
			store.discardBulletin(folderToDiscardFrom, uid);
		}
		store.saveFolders();
	}

	public Date getUploadInfoElement(int index)
	{
		File file = getUploadInfoFile();
		if (!file.canRead())
			return null;
		Date date = null;
		try
		{
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
			for(int i = 0 ; i < index ; ++i)
			{
				stream.readObject();
			}
			date = (Date)stream.readObject();
			stream.close();
		}
		catch (Exception e)
		{
			System.out.println("Error reading from getUploadInfoElement " + index + ":" + e);
		}
		return date;

	}

	public Date getLastUploadedTime()
	{
		return(getUploadInfoElement(0));
	}

	public Date getLastUploadRemindedTime()
	{
		return(getUploadInfoElement(1));
	}


	public void setUploadInfoElements(Date uploaded, Date reminded)
	{
		File file = getUploadInfoFile();
		file.delete();
		try
		{
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
			stream.writeObject(uploaded);
			stream.writeObject(reminded);
			stream.close();
		}
		catch (Exception e)
		{
			System.out.println("Error writing to setUploadInfoElements:" + e);
		}

	}

	public void setLastUploadedTime(Date uploaded)
	{
		Date reminded = getLastUploadRemindedTime();
		setUploadInfoElements(uploaded, reminded);
	}

	public void setLastUploadRemindedTime(Date reminded)
	{
		Date uploaded = getLastUploadedTime();
		setUploadInfoElements(uploaded, reminded);
	}

	public void resetLastUploadedTime()
	{
		setLastUploadedTime(new Date());
	}

	public void resetLastUploadRemindedTime()
	{
		setLastUploadRemindedTime(new Date());
	}

	public SortableBulletinList search(SearchTreeNode searchNode, MiniFieldSpec[] specsForSorting, MiniFieldSpec[] extraSpecs, boolean searchFinalVersionsOnly, boolean searchSameRowsOnly, ProgressMeterInterface progressMeter) throws Exception
	{
		Stopwatch stopWatch = new Stopwatch();
		stopWatch.start();
		long revisionsSearched = 0;
		BulletinSearcher matcher = new BulletinSearcher(searchNode, searchSameRowsOnly);
		matcher.setDoZawgyiConversion(configInfo.getDoZawgyiConversion());
		SortableBulletinList matchedBulletinUids = new SortableBulletinList(localization, specsForSorting, extraSpecs);

		Set uids = store.getAllBulletinLeafUids();
		int totalCount = uids.size();
		int progressCount = 0;
		for(Iterator iter = uids.iterator(); iter.hasNext();)
		{
			if(progressMeter.shouldExit())
				break;
			progressMeter.updateProgressMeter(++progressCount, totalCount);
			UniversalId leafBulletinUid = (UniversalId) iter.next();
			Vector visibleFoldersContainingThisBulletin = findBulletinInAllVisibleFolders(leafBulletinUid);
			visibleFoldersContainingThisBulletin.remove(getFolderDiscarded());
			if(visibleFoldersContainingThisBulletin.size() == 0)
				continue;
			
			Vector allRevisions = getRevisionUidsToSearch(leafBulletinUid, searchFinalVersionsOnly);		
			
			for(int j = 0; j < allRevisions.size(); ++j)
			{
				Bulletin b = store.getBulletinRevision((UniversalId)allRevisions.get(j));
				if(b.containsXFormsData())
					b = BulletinFromXFormsLoader.createNewBulletinFromXFormsBulletin(b);
				++revisionsSearched;
				if(b != null && matcher.doesMatch(new SafeReadableBulletin(b, localization), localization))
				{
					Bulletin latestRevision = store.getBulletinRevision(leafBulletinUid);
					matchedBulletinUids.add(latestRevision);
					break;
				}
			}
		}
		stopWatch.stop();
		MartusLogger.log("Search took:"+stopWatch.elapsedInSeconds()+" Seconds, " + matchedBulletinUids.size() +" matches found, " +uids.size()+" leafs, "+ revisionsSearched + " revisions were searched.");
		
		return matchedBulletinUids;
	}

	private Vector getRevisionUidsToSearch(UniversalId leafBulletinUid, boolean searchFinalVersionsOnly)
	{
		Vector allRevisions = new Vector();
		allRevisions.add(leafBulletinUid);
		if(!searchFinalVersionsOnly)
		{
			String authorAccountId = leafBulletinUid.getAccountId();
			BulletinHistory history = store.getBulletinHistory(leafBulletinUid);
			for(int h=0; h<history.size(); ++h)
			{
				allRevisions.add(UniversalId.createFromAccountAndLocalId(authorAccountId, history.get(h)));
			}
		}
		return allRevisions;
	}
	
	public void updateSearchFolder(SortableBulletinList partialBulletinsToAdd)
	{
		BulletinFolder searchFolder = createOrFindFolder(store.getSearchFolderName());
		searchFolder.removeAll();
		searchFolder.prepareForBulkOperation();
		UniversalId[] uids = partialBulletinsToAdd.getUniversalIds();
		for(int i = 0; i < uids.length; ++i)
		{
			UniversalId leafBulletinUid = uids[i];
			try
			{
				store.addBulletinToFolder(searchFolder, leafBulletinUid);
			}
			catch (BulletinAlreadyExistsException safeToIgnoreException)
			{
			}
			catch (AddOlderVersionToFolderFailedException safeToIgnoreException)
			{
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		store.saveFolders();
	}

	public boolean isNonSSLServerAvailable(String serverName) throws Exception
	{
		if(serverName.length() == 0)
			return false;

		NonSSLNetworkAPI server = new ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer(serverName, getTransport());
		return ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer.isNonSSLServerAvailable(server);
	}

	public boolean isSSLServerAvailable() throws Exception
	{
		if(currentNetworkInterfaceHandler == null && !isServerConfigured())
			return false;
		
		if(!getTransport().isOnline())
			throw new NetworkOfflineException();

		try
		{
			return isSSLServerAvailable(getCurrentNetworkInterfaceGateway());
		}
		catch (NetworkOfflineException e)
		{
			return false;
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return false;
		}
	}
	
	public boolean isServerConfigured()
	{
		return (getServerName().length() > 0);
	}

	public boolean isSignedIn()
	{
		return getSecurity().hasKeyPair();
	}

	public String getServerPublicKey(String serverName) throws Exception
	{
		ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer server = new ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer(serverName, getTransport());
		return getServerPublicKey(server);
	}

	public String getServerPublicKey(NonSSLNetworkAPIWithHelpers server) throws ServerNotAvailableException, PublicInformationInvalidException
	{
		return server.getServerPublicKey(getSecurity());
	}

	public boolean requestServerUploadRights(String magicWord)
	{
		ClientSideNetworkGateway gateWay = getCurrentNetworkInterfaceGateway();
		return requestServerUploadRights(gateWay, magicWord);
	}

	public boolean requestServerUploadRights(ClientSideNetworkGateway gateWay, String magicWord)
	{
		try
		{
			NetworkResponse response = gateWay.getUploadRights(getSecurity(), magicWord);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return true;
		}
		catch(MartusCrypto.MartusSignatureException e)
		{
			System.out.println("MartusApp.requestServerUploadRights: " + e);
		}

		return false;
	}
	
	public MartusAccountAccessToken getMartusAccountAccessTokenFromServer() throws Exception 
	{
		if(!isSSLServerAvailable())
			throw new ServerNotAvailableException();

		NetworkResponse response = getCurrentNetworkInterfaceGateway().getMartusAccountAccessToken(getSecurity());
		if(response.getResultCode().equals(NetworkInterfaceConstants.SERVER_NOT_COMPATIBLE))
			throw new ServerNotCompatibleException();
		if(response.getResultCode().equals(NetworkInterfaceConstants.SERVER_ERROR))
			throw new ServerErrorException();
		if(response.getResultCode().equals(NetworkInterfaceConstants.TRANSPORT_OFFLINE))
			throw new Exceptions.NetworkOfflineException();
		if(!response.getResultCode().equals(NetworkInterfaceConstants.OK))
			throw new ServerNotAvailableException();
			
		Vector singleToken = response.getResultVector();
		if(singleToken.size() != 1)
			throw new TokenInvalidException();
		String ourTokenString = (String)singleToken.get(0);
		return new MartusAccountAccessToken(ourTokenString);
	}

	public String getMartusAccountIdFromAccessTokenOnServer(MartusAccountAccessToken tokenToUse) throws Exception 
	{
		if(!isSSLServerAvailable())
			throw new ServerNotAvailableException();

		NetworkResponse response = getCurrentNetworkInterfaceGateway().getMartusAccountIdFromAccessToken(getSecurity(), tokenToUse);
		if(response.getResultCode().equals(NetworkInterfaceConstants.NO_TOKEN_AVAILABLE))
			throw new TokenNotFoundException();
		if(response.getResultCode().equals(NetworkInterfaceConstants.SERVER_NOT_COMPATIBLE))
			throw new ServerNotCompatibleException();
		if(response.getResultCode().equals(NetworkInterfaceConstants.SERVER_ERROR))
			throw new ServerCallFailedException();
		if(!response.getResultCode().equals(NetworkInterfaceConstants.OK))
		{
			throw new ServerNotAvailableException();
		}
					
		Vector singleAccountId = response.getResultVector();
		if(singleAccountId.size() != 1)
			throw new TokenNotFoundException();
		String AccountId = (String)singleAccountId.get(0);
		
		return AccountId;
	}

	public void putFormTemplateOnServer(FormTemplate formTemplate) throws Exception 
	{
		if(!isSSLServerAvailable())
			throw new ServerNotAvailableException();
		String formTemplateData = formTemplate.getExportedTemplateAsBase64String(getSecurity());
		NetworkResponse response = getCurrentNetworkInterfaceGateway().putFormTemplate(getSecurity(), formTemplateData);
		String resultCode = response.getResultCode();
		if(resultCode.equals(NetworkInterfaceConstants.NO_SERVER) || resultCode.equals(NetworkInterfaceConstants.SERVER_DOWN))
		{
			MartusLogger.log("Server result code: " + resultCode);
			throw new ServerNotAvailableException();
		}
		if(resultCode.equals(NetworkInterfaceConstants.SERVER_NOT_COMPATIBLE))
		{
			throw new ServerNotCompatibleException();
		}
		if(!resultCode.equals(NetworkInterfaceConstants.OK))
		{
			MartusLogger.log("Server result code: " + resultCode);
			throw new ServerErrorException();
		}
	}
	
	
	public Vector getListOfFormTemplatesOnServer(String accountToRetreiveListFrom) throws Exception
	{
		if(!isSSLServerAvailable())
			throw new ServerNotAvailableException();
		NetworkResponse response = getCurrentNetworkInterfaceGateway().getListOfFormTemplates(getSecurity(), accountToRetreiveListFrom);
		if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
			return getVectorOfVectorsFromResponse(response);
		if(response.getResultCode().equals(NetworkInterfaceConstants.ACCOUNT_NOT_FOUND))
			throw new AccountNotFoundException(); 
		if(response.getResultCode().equals(NetworkInterfaceConstants.SERVER_ERROR))
			throw new ServerErrorException();
		throw new ServerNotAvailableException();
	}
	
	private Vector getVectorOfVectorsFromResponse(NetworkResponse response)
	{
		Vector vectorOfObjectArrays = response.getResultVector();
		Vector vectorOfVectors = new Vector();
		for(int i = 0; i < vectorOfObjectArrays.size(); ++i)
		{
			Object[] listOfTitleAndDescriptions = (Object[]) vectorOfObjectArrays.get(i);
			vectorOfVectors.add(new Vector(Arrays.asList(listOfTitleAndDescriptions)));
		}
		return vectorOfVectors;
	}

	
	public FormTemplate getFormTemplateOnServer(String accountId, String formTitle) throws Exception 
	{
		if(!isSSLServerAvailable())
			throw new ServerNotAvailableException();
		NetworkResponse response = getCurrentNetworkInterfaceGateway().getFormTemplate(getSecurity(), accountId, formTitle);
		if(!response.getResultCode().equals(NetworkInterfaceConstants.OK))
		{
			if(response.getResultCode().equals(NetworkInterfaceConstants.FORM_TEMPLATE_DOES_NOT_EXIST))
				throw new NoFormsAvailableException();
			throw new ServerNotAvailableException();
		}
		Vector result = response.getResultVector();
		if(result.size() != 1)
			throw new NoFormsAvailableException();
		String base64FormData = (String)result.get(0);
		StringReader reader = new StringReader(base64FormData);
		File formTemplateTempFile = File.createTempFile("$$$FormTemplate", null);
		formTemplateTempFile.deleteOnExit();
		FileOutputStream output = new FileOutputStream(formTemplateTempFile);
		StreamableBase64.decode(reader, output);
		output.flush();
		output.close();

		return importFormTemplate(formTemplateTempFile);
	}

	private FormTemplate importFormTemplate(File formTemplateTempFile) throws FutureVersionException, IOException
	{
		FormTemplate template = new FormTemplate();
		FileInputStreamWithSeek inputStream = new FileInputStreamWithSeek(formTemplateTempFile);
		try
		{
			template.importTemplate(getSecurity(), inputStream);
			formTemplateTempFile.delete();
		}
		finally
		{
			inputStream.close();
		}
		
		return template;
	}

	
	public Vector getNewsFromServer()
	{
		try
		{
			if(!isSSLServerAvailable())
				return new Vector();

			NetworkResponse response = getCurrentNetworkInterfaceGateway().getNews(getSecurity(), UiConstants.versionLabel);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return response.getResultVector();
		}
		catch (NetworkOfflineException e)
		{
			// Nothing to do 
		}
		catch (Exception e)
		{
			// TODO: Should let the user know something went wrong
			MartusLogger.logException(e);
		}
		return new Vector();
	}

	public String getServerCompliance(ClientSideNetworkGateway gateway) 
		throws ServerCallFailedException, ServerNotAvailableException
	{
		try
		{
			if(!isSSLServerAvailable(gateway))
				throw new ServerNotAvailableException();

			NetworkResponse response = gateway.getServerCompliance(getSecurity());
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return (String)response.getResultVector().get(0);
		}
		catch (ServerNotAvailableException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			//System.out.println("MartusApp.getServerCompliance :" + e);
			MartusLogger.logException(e);
			throw new ServerCallFailedException();
		}		
		throw new ServerCallFailedException();
	}

	public void moveBulletinToDamaged(BulletinFolder outbox, UniversalId uid) throws IOException
	{
		System.out.println("Moving bulletin to damaged");
		BulletinFolder damaged = createOrFindFolder(store.getNameOfFolderDamaged());
		Bulletin b = store.getBulletinRevision(uid);
		store.moveBulletin(b, outbox, damaged);		
	}

	public Vector downloadFieldOfficeAccountIds() throws Exception
	{
		if(!isSSLServerAvailable())
			throw new ServerErrorException();

		ClientSideNetworkGateway networkInterfaceGateway = getCurrentNetworkInterfaceGateway();
		MartusCrypto security = getSecurity();
		String myAccountId = getAccountId();

		return networkInterfaceGateway.downloadFieldOfficeAccountIds(security, myAccountId);
	}
	
	public BulletinHeaderPacket retrieveHeaderPacketFromServer(UniversalId bulletinId) throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(bulletinId);
		populatePacketFromServer(bhp, bulletinId.getLocalId());
		return bhp;
	}

	public FieldDataPacket retrieveFieldDataPacketFromServer(UniversalId bulletinId, String dataPacketLocalId) throws Exception
	{
		UniversalId packetUid = UniversalId.createFromAccountAndLocalId(bulletinId.getAccountId(), dataPacketLocalId);
		FieldDataPacket fdp = new FieldDataPacket(packetUid, StandardFieldSpecs.getDefaultTopSectionFieldSpecs());
		populatePacketFromServer(fdp, bulletinId.getLocalId());
		return fdp;
	}

	private void populatePacketFromServer(Packet packet, String bulletinLocalId) throws MartusSignatureException, ServerErrorException, UnsupportedEncodingException, InvalidBase64Exception, IOException, InvalidPacketException, WrongPacketTypeException, SignatureVerificationException, DecryptionException, NoKeyPairException
	{
		NetworkResponse response = getCurrentNetworkInterfaceGateway().getPacket(getSecurity(), packet.getAccountId(), bulletinLocalId, packet.getLocalId());
		String resultCode = response.getResultCode();
		if(!resultCode.equals(NetworkInterfaceConstants.OK))
			throw new ServerErrorException(resultCode);

		String xmlEncoded = (String)response.getResultVector().get(0);
		String xml = new String(StreamableBase64.decode(xmlEncoded), "UTF-8");
		byte[] xmlBytes = xml.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in =  new ByteArrayInputStreamWithSeek(xmlBytes);
		packet.loadFromXml(in, getSecurity());
	}

	public BulletinSummary createSummaryFromString(String accountId, String summaryAsString) throws WrongValueCount
	{
		BulletinSummary summary = BulletinSummary.createFromString(accountId, summaryAsString);
		Bulletin bulletin = store.getBulletinRevision(summary.getUniversalId());
		if (bulletin != null)
			summary.setFieldDataPacket(bulletin.getFieldDataPacket());
		return summary;
	}

	public void setFieldDataPacketFromServer(BulletinSummary summary) throws Exception
	{
		if(!FieldDataPacket.isValidLocalId(summary.getFieldDataPacketLocalId()))
			throw new ServerErrorException();
	
		summary.setFieldDataPacket(retrieveFieldDataPacketFromServer(summary.getUniversalId(), summary.getFieldDataPacketLocalId()));
	}

	public void retrieveNextBackgroundBulletin() throws Exception
	{
		RetrieveCommand rc = getCurrentRetrieveCommand();
		UniversalId uid = rc.getNextToRetrieve();
		BulletinFolder folder = createOrFindFolder(rc.getFolderName());
		try
		{
			retrieveOneBulletinToFolder(uid, folder, null);
		}
		catch(NotYourBulletinErrorException okIfPreviousVersionIsNotAuthorizedToRead)
		{
		}
		catch(BulletinNotFoundException okIfPreviousVersionIsNotOnServer)
		{
		}
		catch(AddOlderVersionToFolderFailedException okIfOlderVersionWasNotAddedToRetrievedFolder)
		{
		}
		finally
		{
			rc.markAsRetrieved(uid);
			saveRetrieveCommand();
		}
		
	}

	public void retrieveOneBulletinToFolder(UniversalId uid, BulletinFolder retrievedFolder, ProgressMeterInterface progressMeter) throws
		AddOlderVersionToFolderFailedException, Exception
	{
		File tempFile = getCurrentNetworkInterfaceGateway().retrieveBulletin(uid, getSecurity(), serverChunkSize, progressMeter);
		try
		{
			store.importZipFileBulletin(tempFile, retrievedFolder, true);
			Bulletin b = store.getBulletinRevision(uid);
			store.setIsOnServer(b);
		}
		finally
		{
			tempFile.delete();
		}
	}

	public String deleteServerDraftBulletins(Vector<UniversalId> uidList) throws
		MartusSignatureException,
		WrongAccountException
	{
		String[] localIds = new String[uidList.size()];
		for (int i = 0; i < localIds.length; i++)
		{
			UniversalId uid = uidList.get(i);
			if(!uid.getAccountId().equals(getAccountId()))
				throw new WrongAccountException();

			localIds[i] = uid.getLocalId();
		}
		NetworkResponse response = getCurrentNetworkInterfaceGateway().deleteServerDraftBulletins(getSecurity(), getAccountId(), localIds);
		String resultCode = response.getResultCode();
		if(resultCode.equals(NetworkInterfaceConstants.OK))
			updateOnServerFlagForLocalCopies(uidList);
		return resultCode;
	}

	private void updateOnServerFlagForLocalCopies(Vector<UniversalId> uidList)
	{
		for (UniversalId bulletinId : uidList)
		{
			if(store.doesBulletinRevisionExist(bulletinId))
				store.setIsNotOnServer(bulletinId);
		}
	}

	public static class AccountAlreadyExistsException extends Exception 
	{
	}

	public static class CannotCreateAccountFileException extends IOException 
	{
	}

	public void createAccount(String userName, char[] userPassPhrase) throws
					Exception
	{
		if(doesAccountExist(userName, userPassPhrase))
			throw new AccountAlreadyExistsException();
		
		if(doesDefaultAccountExist())
			createAdditionalAccount(userName, userPassPhrase);
		else
			createAccountInternal(getMartusDataRootDirectory(), userName, userPassPhrase);
	}

	public boolean doesAccountExist(String userName, char[] userPassPhrase) throws Exception
	{
		return (getAccountDirectoryForUser(userName, userPassPhrase) != null);
	}

	public File getAccountDirectoryForUser(String userName, char[] userPassPhrase) throws Exception
	{
		Vector allAccountDirs = getAllAccountDirectories();
		MartusCrypto tempSecurity = new MartusSecurity();
		for(int i = 0; i<allAccountDirs.size(); ++i )
		{
			File testAccountDirectory = (File)allAccountDirs.get(i);
			if(isUserOwnerOfThisAccountDirectory(tempSecurity, userName, userPassPhrase, testAccountDirectory))
				return testAccountDirectory;
		}
		return null;
	}

	private void createAdditionalAccount(String userName, char[] userPassPhrase) throws Exception
	{
		File tempAccountDir = null;
		try
		{
			File accountsDirectory = getAccountsDirectory();
			accountsDirectory.mkdirs();
			tempAccountDir = File.createTempFile("temp", null, accountsDirectory);
			tempAccountDir.delete();
			tempAccountDir.mkdirs();
			createAccountInternal(tempAccountDir, userName, userPassPhrase);
			String realAccountDirName = getAccountDirectoryName(getAccountId());
			File realAccountDir = new File(accountsDirectory, realAccountDirName);

			if(tempAccountDir.renameTo(realAccountDir))
				setCurrentAccount(userName, realAccountDir);
			else
				System.out.println("createAdditionalAccount rename failed.");
		}
		catch (Exception e)
		{
			System.out.println("createAdditionalAccount failed.");
			DirectoryUtils.deleteEntireDirectoryTree(tempAccountDir);
			throw(e);
		}
	}

	public void createAccountInternal(File accountDataDirectory, String userName, char[] userPassPhrase) throws
		Exception
	{
		MartusLogger.log("Creating account with " + MartusCrypto.getBitsWhenCreatingKeyPair() + " bits");
		File keyPairFile = getKeyPairFile(accountDataDirectory);
		if(keyPairFile.exists())
			throw(new AccountAlreadyExistsException());
		getSecurity().clearKeyPair();
		getSecurity().createKeyPair();
		try
		{
			writeKeyPairFileWithBackup(keyPairFile, userName, userPassPhrase);
			attemptSignInInternal(keyPairFile, userName, userPassPhrase);
		}
		catch(Exception e)
		{
			getSecurity().clearKeyPair();
			throw(e);
		}
	}
	
	public Vector getAllAccountDirectories()
	{
		Vector accountDirectories = new Vector();
		accountDirectories.add(getMartusDataRootDirectory());
		File accountsDirectoryRoot = getAccountsDirectory();
		File[] contents = accountsDirectoryRoot.listFiles();
		if(contents== null)
			return accountDirectories;
		for (int i = 0; i < contents.length; i++)
		{
			File thisFile = contents[i];
			try
			{
				if(!thisFile.isDirectory())
				{	
					continue;
				}
				String name = thisFile.getName();
				if(name.length() != 24)
				{	
					continue;
				}
				if(MartusCrypto.removeNonDigits(name).length() != 20)
				{	
					continue;
				}
				accountDirectories.add(thisFile);
			}
			catch (Exception notAValidAccountDirectory)
			{
			}
		}
		return accountDirectories;
	}
	
	public File getAccountDirectory(String accountId) throws InvalidBase64Exception
	{
		String name = getAccountDirectoryName(accountId);
		File proposedAccountDir = new File(getAccountsDirectory(), name);
		if(proposedAccountDir.exists() && proposedAccountDir.isDirectory())
			return proposedAccountDir;
		
		File dataRootDir = getMartusDataRootDirectory();
		if(!getKeyPairFile(dataRootDir).exists())
			return dataRootDir;
		if(doesDirectoryContainAccount(dataRootDir, accountId))
			return dataRootDir;
		
		proposedAccountDir.mkdirs();
		return proposedAccountDir;
	}

	private String getAccountDirectoryName(String accountId)
		throws InvalidBase64Exception
	{
		return MartusCrypto.getFormattedPublicCode(accountId);
	}
	
	private boolean doesDirectoryContainAccount(File dir, String accountId)
	{
		File configFile = getConfigInfoFileForAccount(dir);
		File sigFile = getConfigInfoSignatureFileForAccount(dir);
		
		try 
		{
			return(isSignatureFileValid(configFile, sigFile, accountId));
		} 
		catch (Exception e) 
		{
			return false;
		}
	}

	public boolean hasNoAccounts()
	{
		return !doesAnyAccountExist();
	}
	
	public boolean doesAnyAccountExist()
	{
		Vector accountDirectories = getAllAccountDirectories();
		for (int i = 0; i < accountDirectories.size(); i++)
		{
			File thisDirectory = (File)accountDirectories.get(i);
			if(getKeyPairFile(thisDirectory).exists())
				return true;
		}
		return false;
	}
	
	public boolean doesDefaultAccountExist()
	{
		if(getKeyPairFile(getMartusDataRootDirectory()).exists())
			return true;

		File packetsDir = new File(getMartusDataRootDirectory(), PACKETS_DIRECTORY_NAME);
		if(!packetsDir.exists())
			return false;

		return (packetsDir.listFiles().length > 0);
	}

	public void exportPublicInfo(File exportFile) throws
		IOException,
		StreamableBase64.InvalidBase64Exception,
		MartusCrypto.MartusSignatureException
	{
		MartusUtilities.exportClientPublicKey(getSecurity(), exportFile);
	}

	public String extractPublicInfo(File file) throws
		IOException,
		StreamableBase64.InvalidBase64Exception,
		PublicInformationInvalidException
	{
		Vector importedPublicKeyInfo = MartusUtilities.importClientPublicKeyFromFile(file);
		String publicKey = (String) importedPublicKeyInfo.get(0);
		String signature = (String) importedPublicKeyInfo.get(1);
		MartusUtilities.validatePublicInfo(publicKey, signature, getSecurity());
		return publicKey;
	}

	public File getPublicInfoFile(String fileName)
	{
		fileName = MartusUtilities.toFileName(fileName);
		String completeFileName = fileName + PUBLIC_INFO_EXTENSION;
		return(new File(getCurrentAccountDirectoryName(), completeFileName));
	}

	public void attemptSignIn(String userName, char[] userPassPhrase) throws Exception
	{
		File keyPairFile = getAccountDirectoryForUser(userName, userPassPhrase);
		attemptSignInInternal(getKeyPairFile(keyPairFile), userName, userPassPhrase);
	}
	
	public void attemptReSignIn(String userName, char[] userPassPhrase) throws Exception
	{
		attemptReSignInInternal(getCurrentKeyPairFile(), userName, userPassPhrase);
	}
	
	private String getCurrentLanguage()
	{
		return localization.getCurrentLanguageCode();
	}

	public String getAccountId()
	{
		return getSecurity().getPublicKeyString();
	}
	
	public void writeKeyPairFileWithBackup(File keyPairFile, String userName, char[] userPassPhrase) throws
		CannotCreateAccountFileException
	{
		writeKeyPairFileInternal(keyPairFile, userName, userPassPhrase);
		try
		{
			writeKeyPairFileInternal(getBackupFile(keyPairFile), userName, userPassPhrase);
		}
		catch (Exception e)
		{
			System.out.println("MartusApp.writeKeyPairFileWithBackup: " + e);
		}
	}

	protected void writeKeyPairFileInternal(File keyPairFile, String userName, char[] userPassPhrase) throws
		CannotCreateAccountFileException
	{
		try
		{
			FileOutputStream outputStream = new FileOutputStream(keyPairFile);
			try
			{
				getSecurity().writeKeyPair(outputStream, PasswordHelper.getCombinedPassPhrase(userName, userPassPhrase));
			}
			finally
			{
				outputStream.close();
			}
		}
		catch(Exception e)
		{
			throw(new CannotCreateAccountFileException());
		}

	}

	public void attemptSignInInternal(File keyPairFile, String userName, char[] userPassPhrase) throws Exception
	{
		try
		{
			getSecurity().readKeyPair(keyPairFile, PasswordHelper.getCombinedPassPhrase(userName, userPassPhrase));
			setCurrentAccount(userName, keyPairFile.getParentFile());
		}
		catch(IOException e)
		{
			clearCurrentUser();
			throw new AuthorizationFailedException(e);
		}
		catch(Exception e)
		{
			clearCurrentUser();
			throw e;
		}
	}

	private void clearCurrentUser()
	{
		getSecurity().clearKeyPair();
		currentUserName = "";
	}
	
	public void attemptReSignInInternal(File keyPairFile, String userName, char[] userPassPhrase) throws Exception
	{
		if(!userName.equals(currentUserName))
			throw new MartusCrypto.AuthorizationFailedException();
		MartusCrypto securityOfReSignin = new MartusSecurity();
		FileInputStream inputStream = new FileInputStream(keyPairFile);
		try
		{
			securityOfReSignin.readKeyPair(inputStream, PasswordHelper.getCombinedPassPhrase(userName, userPassPhrase));
		}
		finally
		{
			inputStream.close();
		}
	}

	public void setCurrentAccount(String userName, File accountDirectory) throws IOException
	{
		currentUserName = userName;
		currentAccountDirectory = accountDirectory;
		updateUserNameHashFile();
	}

	private void updateUserNameHashFile() throws IOException
	{
		File hashUserName = getUserNameHashFile(currentAccountDirectory);
		hashUserName.delete();
		String hashOfUserName = MartusCrypto.getHexDigest(currentUserName);
		UnicodeWriter writer = new UnicodeWriter(hashUserName);
		try
		{
			writer.writeln(hashOfUserName);
		}
		finally
		{
			writer.close();
		}
	}
	
	public boolean isUserOwnerOfThisAccountDirectory(MartusCrypto tempSecurity, String userName, char[] userPassPhrase, File accountDirectory) throws IOException
	{
		File thisAccountsHashOfUserNameFile = getUserNameHashFile(accountDirectory);
		if(thisAccountsHashOfUserNameFile.exists())
		{
			UnicodeReader reader = new UnicodeReader(thisAccountsHashOfUserNameFile);
			try
			{
				String hashOfUserName = reader.readLine();
				String hexDigest = MartusCrypto.getHexDigest(userName);
				if(hashOfUserName.equals(hexDigest))
					return true;
			}
			finally
			{
				reader.close();
			}
			return false;
		}

		File thisAccountsKeyPair = getKeyPairFile(accountDirectory);
		try
		{
			tempSecurity.readKeyPair(thisAccountsKeyPair, PasswordHelper.getCombinedPassPhrase(userName, userPassPhrase));
			return true;
		}
		catch (Exception cantBeOurAccount)
		{
			return false;
		}
	}

	public File getUserNameHashFile(File accountDirectory)
	{
		return new File(accountDirectory, "AccountToken.txt");
	}

	public MartusCrypto getSecurity()
	{
		return store.getSignatureGenerator();
	}

	public void setSSLNetworkInterfaceHandlerForTesting(ClientSideNetworkInterface server)
	{
		currentNetworkInterfaceHandler = server;
	}

	public void setSSLNetworkInterfaceHandlerForTesting(ServerSideNetworkInterface server)
	{
		setSSLNetworkInterfaceHandlerForTesting(new MockClientSideNetworkHandler(server));
	}

	public boolean isSSLServerAvailable(ClientSideNetworkGateway server) throws Exception
	{
		return isSSLServerAvailableStatic(server);
	}
	
	public static boolean isSSLServerAvailableStatic(ClientSideNetworkGateway server)
	{
		try
		{
			if(server.getInterface() == null)
				return false;
			
			NetworkResponse response = server.getServerInfo();
			if(!response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return false;

			String version = (String)response.getResultVector().get(0);
			if(version.indexOf("MartusServer") == 0)
				return true;
		}
		catch(Exception notInterestingBecauseTheServerMightJustBeDown)
		{
			//System.out.println("MartusApp.isSSLServerAvailable: " + e);
			MartusLogger.logException(notInterestingBecauseTheServerMightJustBeDown);
		}

		return false;
	}

	public ClientSideNetworkGateway getCurrentNetworkInterfaceGateway()
	{
		if(currentNetworkInterfaceGateway == null)
		{
			currentNetworkInterfaceGateway = new ClientSideNetworkGateway(getCurrentNetworkInterfaceHandler());
		}

		return currentNetworkInterfaceGateway;
	}

	private ClientSideNetworkInterface getCurrentNetworkInterfaceHandler()
	{
		if(currentNetworkInterfaceHandler == null)
		{
			currentNetworkInterfaceHandler = createXmlRpcNetworkInterfaceHandler();
		}

		return currentNetworkInterfaceHandler;
	}

	private ClientSideNetworkInterface createXmlRpcNetworkInterfaceHandler()
	{
		String ourServer = getServerName();
		String ourServerPublicKey = getConfigInfo().getServerPublicKey();
		return ClientSideNetworkGateway.buildNetworkInterface(ourServer,ourServerPublicKey, getTransport());
	}

	private void invalidateCurrentHandlerAndGateway()
	{
		currentNetworkInterfaceHandler = null;
		currentNetworkInterfaceGateway = null;
	}

	private String getServerName()
	{
		return configInfo.getServerName();
	}

	public void saveBulletin(Bulletin bulletinToSave, BulletinFolder outboxToUse) throws Exception
	{
		store.saveBulletin(bulletinToSave);
		store.ensureBulletinIsInFolder(store.getFolderSaved(), bulletinToSave.getUniversalId());
		store.ensureBulletinIsInFolder(outboxToUse, bulletinToSave.getUniversalId());
		store.removeBulletinFromFolder(store.getFolderDiscarded(), bulletinToSave);
		store.setIsNotOnServer(bulletinToSave);
		store.saveFolders();
	}

	public class SaveConfigInfoException extends Exception 
	{
	}

	public class LoadConfigInfoException extends Exception 
	{
		public LoadConfigInfoException() 
		{
		}
		
		public LoadConfigInfoException(Exception e) 
		{
			super(e);
		}
	}

	public static class MartusAppInitializationException extends Exception
	{
		MartusAppInitializationException(String message)
		{
			super(message);
		}
	}

	public File martusDataRootDirectory;
	protected File currentAccountDirectory;
	protected MtfAwareLocalization localization;
	public ClientBulletinStore store;
	private HashMap fieldExpansionStates;
	private HashMap gridExpansionStates;
	private ConfigInfo configInfo;
	public ClientSideNetworkInterface currentNetworkInterfaceHandler;
	public ClientSideNetworkGateway currentNetworkInterfaceGateway;
	public String currentUserName;
	private int maxNewFolders;
	public RetrieveCommand currentRetrieveCommand;
	private MartusOrchidDirectoryStore orchidStore;
	private OrchidTransportWrapperWithActiveProperty transport;
	private boolean isInitialized;

	public static final String PUBLIC_INFO_EXTENSION = ".mpi";
	public static final String XML_EXTENSION = ".xml";
	public static final String DEFAULT_DETAILS_EXTENSION = ".txt";
	public static final String AUTHENTICATE_SERVER_FAILED = "Failed to Authenticate Server";
	public static final String SHARE_KEYPAIR_FILENAME_EXTENSION = ".dat";
	public static final String KEYPAIR_FILENAME = "MartusKeyPair.dat";
	public static final String ACCOUNTS_DIRECTORY_NAME = "accounts";
	public static final String PACKETS_DIRECTORY_NAME = "packets";
	public static final String DOCUMENTS_DIRECTORY_NAME = "Docs";
	public static final String USE_UNOFFICIAL_TRANSLATIONS_NAME = "use_unofficial_translations.txt";
	public static final int DAYS_UNTIL_WE_ASK_TO_BACKUP_KEYPAIR = 7;
	private static final String FXML_DIRECTORY_NAME = "fxml";
	
	private final int MAXFOLDERS = 50;
	public int serverChunkSize = NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE;
}

