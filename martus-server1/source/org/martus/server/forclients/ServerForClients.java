/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2014, Beneficent
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

package org.martus.server.forclients;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.MagicWordEntry;
import org.martus.common.MagicWords;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusAccountAccessToken.TokenInvalidException;
import org.martus.common.MartusAccountAccessToken.TokenNotFoundException;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.Version;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.DeleteRequestRecord;
import org.martus.common.database.FileDatabase;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.database.ReadableDatabase.AccountVisitor;
import org.martus.common.fieldspec.CustomFieldTemplate;
import org.martus.common.fieldspec.FormTemplateParsingException;
import org.martus.common.network.MartusXmlRpcServer;
import org.martus.common.network.NetworkInterface;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkInterfaceXmlRpcConstants;
import org.martus.common.network.NonSSLNetworkAPI;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.common.utilities.MartusServerUtilities.MartusSignatureFileDoesntExistsException;
import org.martus.common.xmlrpc.WebServerWithClientId;
import org.martus.server.main.MartusServer;
import org.martus.server.main.ServerBulletinStore;
import org.martus.util.DirectoryUtils;
import org.martus.util.LoggerUtil;
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.miradi.utils.EnhancedJsonObject;

public class ServerForClients implements ServerForNonSSLClientsInterface, ServerForClientsInterface
{
	public ServerForClients(MartusServer coreServerToUse)
	{
		coreServer = coreServerToUse;
		magicWords = new MagicWords(coreServer.getLogger());
		clientsThatCanUpload = new Vector();
		activeWebServers = new Vector();
		loggedNumberOfActiveClients = 0;
		newsItems = new Vector();
	}
	
	public Vector getDeleteOnStartupFiles()
	{
		Vector startupFiles = new Vector();
		startupFiles.add(getMagicWordsFile());
		startupFiles.add(getBannedFile());
		startupFiles.add(getTestAccountsFile());
		return startupFiles;
	}
	
	public Vector getDeleteOnStartupFolders()
	{
		Vector startupFolders = new Vector();
		startupFolders.add(getNewsDirectory());
		return startupFolders;
	}

	public File getNewsDirectory()
	{
		return new File(coreServer.getStartupConfigDirectory(), CLIENTNEWSDIRECTORY);
	}

	
	public void deleteStartupFiles()
	{
		MartusUtilities.deleteAllFiles(getDeleteOnStartupFiles());
		DirectoryUtils.deleteEntireDirectoryTree(getDeleteOnStartupFolders());
	}
	
	public ServerBulletinStore getStore()
	{
		return coreServer.getStore();
	}
	
	public MartusCrypto getSecurity()
	{
		return coreServer.getSecurity();
	}
	
	public String getPublicCode(String clientId)
	{
		return coreServer.getPublicCode(clientId); 
	}
	
	private File getConfigDirectory()
	{
		return coreServer.getStartupConfigDirectory();
	}
	
	private ReadableDatabase getDatabase()
	{
		return coreServer.getDatabase();
	}
	

	public void addListeners() throws UnknownHostException
	{
		logNotice("Initializing ServerForClients");
		handleSSL(getSSLPorts());
		handleNonSSL(getNonSSLPorts());
		logNotice("Client ports opened");
	}
	
	private int[] getNonSSLPorts()
	{
		int[] defaultPorts = defaultNonSSLPorts;
		return coreServer.shiftToDevelopmentPortsIfNotInSecureMode(defaultPorts);
	}

	private int[] getSSLPorts()
	{
		int[] defaultPorts = NetworkInterfaceXmlRpcConstants.defaultSSLPorts;
		return coreServer.shiftToDevelopmentPortsIfNotInSecureMode(defaultPorts);
	}

	public int[] getActiveRunnerCounts()
	{
		int[] counts = new int[activeWebServers.size()];
		for(int i = 0; i < counts.length; ++i)
		{
			WebServerWithClientId server = (WebServerWithClientId)activeWebServers.get(i);
			counts[i] = server.getActiveRunnerCount();
		}
		
		return counts;
	}

	boolean isRunningUnderWindows()
	{
		return Version.isRunningUnderWindows();
	}
	
	public synchronized void logError(String message)
	{
		coreServer.logError(createLogString(message));
	}
	
	public void logError(Exception e)
	{
		logError(LoggerUtil.getStackTrace(e));
	}
	
	public void logError(String message, Exception e)
	{
		logError(message);
		logError(e);
	}

	public synchronized void logInfo(String message)
	{
		coreServer.logInfo(createLogString(message));
	}

	public synchronized void logNotice(String message)
	{
		coreServer.logNotice(createLogString(message));
	}
	
	public synchronized void logWarning(String message)
	{
		coreServer.logWarning(createLogString(message));
	}

	public synchronized void logDebug(String message)
	{
		coreServer.logDebug(createLogString(message));
	}

	private String createLogString(String message) 
	{
		return message;
	}

	public void displayClientStatistics()
	{
		logInfo("");
		logInfo(clientsThatCanUpload.size() + " client(s) currently allowed to upload");
		logInfo(clientsBanned.size() + " client(s) are currently banned");
		logInfo(magicWords.getNumberOfActiveWords() + " active magic word(s)");
		logInfo(magicWords.getNumberOfInactiveWords() + " inactive magic word(s)");
		logInfo(getNumberOfTestAccounts() + " client(s) are known test accounts");
		logInfo(getNumberOfNewsItems() +" News items");
		logInfo("");
		try 
		{
			MartusAccountAccessToken validButImpossibleToken = new MartusAccountAccessToken("0000000");
			String result = getAccountIdForTokenFromMartusCentralTokenAuthority(validButImpossibleToken);
			logError("Token authority unexpected response: " + result);
		}
		catch (TokenNotFoundException e)
		{
			logInfo("Token authority is available.");
		}
		catch (Exception e) 
		{
			logError(e);
		} 
	}

	public void verifyConfigurationFiles()
	{
		try
		{
			MartusServerUtilities.verifyFileAndLatestSignatureOnServer(getAllowUploadFile(), getSecurity());
		}
		catch(FileVerificationException e)
		{
			logError(UPLOADSOKFILENAME + " did not verify against signature file", e);
			System.exit(7);
		}
		catch(Exception e)
		{
			if(getAllowUploadFile().exists())
			{
				logError("Unable to verify " + UPLOADSOKFILENAME + " against a signature file", e);
				System.exit(7);
			}
		}
	}

	public void loadConfigurationFiles() throws IOException
	{
		loadBannedClients();
		loadCanUploadFile();
		loadTestAccounts();
		loadNews();
		loadMagicWordsFile();
	}

	public void prepareToShutdown()
	{
		clearCanUploadList();
		for(int i = 0 ; i < activeWebServers.size(); ++i)
		{
			WebServerWithClientId server = (WebServerWithClientId)(activeWebServers.get(i));
			if(server != null)
				server.shutdown();
		}
	}

	public boolean isClientBanned(String clientId)
	{
		if(clientsBanned.contains(clientId))
		{
			logNotice("client BANNED: " + getPublicCode(clientId));
			return true;
		}
		return false;
	}
	
	public boolean isTestAccount(String clientId)
	{
		if(testAccounts.contains(clientId))
			return true;
		return false;
	}
	
	public int getNumberOfTestAccounts()
	{
		return testAccounts.size();
	}
	
	public boolean canClientUpload(String clientId)
	{
		return true;
	}
	
	public void clearCanUploadList()
	{
		clientsThatCanUpload.clear();
	}
	

	public boolean canExitNow()
	{
		int numberActiveClients = getNumberActiveClients();
		if(numberActiveClients != 0 && loggedNumberOfActiveClients != numberActiveClients)
		{	
			logNotice("Unable to exit, number of active clients =" + numberActiveClients);
			loggedNumberOfActiveClients = numberActiveClients;
		}
		return (numberActiveClients == 0);
	}
	
	synchronized int getNumberActiveClients()
	{
		return activeClientsCounter;
	}
	
	
	public synchronized void clientConnectionStart(String newCallerAccountId)
	{
		MartusServer.setThreadCallerAccountId(newCallerAccountId);
		activeClientsCounter++;
	}
	
	public synchronized void clientConnectionExit()
	{
		MartusServer.setThreadCallerAccountId(null);
		activeClientsCounter--;
	}
	
	public boolean shouldSimulateBadConnection()
	{
		return coreServer.simulateBadConnection;
	}
	
	public void handleNonSSL(int[] ports) throws UnknownHostException
	{
		ServerSideNetworkHandlerForNonSSL nonSSLServerHandler = new ServerSideNetworkHandlerForNonSSL(this);
		for(int i=0; i < ports.length; ++i)
		{	
			InetAddress mainIpAddress = MartusServer.getMainIpAddress();
			logNotice("Opening NonSSL port " + mainIpAddress +":" + ports[i] + " for clients...");
			activeWebServers.add(MartusXmlRpcServer.createNonSSLXmlRpcServer(nonSSLServerHandler, NonSSLNetworkAPI.class, "MartusServer", ports[i], mainIpAddress));
		}
	}
	
	public void handleSSL(int[] ports) throws UnknownHostException
	{
		ServerSideNetworkHandler serverHandler = new ServerSideNetworkHandler(this);
		for(int i=0; i < ports.length; ++i)
		{	
			InetAddress mainIpAddress = MartusServer.getMainIpAddress();
			logNotice("Opening SSL port " + mainIpAddress +":" + ports[i] + " for clients...");
			activeWebServers.add(MartusXmlRpcServer.createSSLXmlRpcServer(serverHandler, NetworkInterface.class, "MartusServer", ports[i], mainIpAddress));
		}
	}


	// BEGIN SSL interface
	public Vector getBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize)
	{
		return coreServer.getBulletinChunk(myAccountId, authorAccountId, bulletinLocalId, chunkOffset, maxChunkSize);
	}

	public Vector getNews(String accountId, String versionLabel, String versionBuildDate)
	{
		Vector result = new Vector();
		Vector items = new Vector();
		{
			String loggingData = "getNews: " + coreServer.getClientAliasForLogging(accountId);
			if(versionLabel.length() > 0 && versionBuildDate.length() > 0)
				loggingData = loggingData +", " + versionLabel + ", " + versionBuildDate;

			logInfo(loggingData);
		}		

		if(isClientBanned(accountId))
		{
			final String bannedText = "Your account has been blocked from accessing this server. " + 
					"Please contact the Server Policy Administrator for more information.";
			items.add(bannedText);
		}
		
		items.addAll(newsItems);
		result.add(NetworkInterfaceConstants.OK);
		result.add(items.toArray());
		return result;
	}
	
	public String getTokensFromMartusCentralTokenAuthority(String accountId) throws Exception
	{
		String encodedPublicKey = URLEncoder.encode(accountId, "UTF-8");
		String tokenAuthorityCall = coreServer.tokenAuthorityBase + "/tokens/byKey/" + encodedPublicKey;
		URL url = new URL(tokenAuthorityCall);
		return callRestJsonUtf8(url);
	}

	public String callRestJsonUtf8(URL url) throws Exception 
	{
		logInfo("Calling: " + url);
		InputStream in = url.openStream();
		UnicodeReader reader = new UnicodeReader(in);
		try
		{
			String response = reader.readAll();
			if(response == null)
				throw new IOException("No data available");
			
			EnhancedJsonObject json = new EnhancedJsonObject(response);
			String code = json.optString("Code");
			if(code.equals("200"))
				return response;
			
			if(code.equals("404"))
				throw new TokenNotFoundException();
			
			throw new IOException("Error: " + code);
		}
		finally
		{
			reader.close();
			in.close();
		}
	}
	
	public String getAccountIdForTokenFromMartusCentralTokenAuthority(MartusAccountAccessToken token) throws Exception
	{
		String encodedToken = URLEncoder.encode(token.getToken(), "UTF-8");
		String tokenAuthorityCall = coreServer.tokenAuthorityBase + "/keys/byToken/" + encodedToken;
		URL url = new URL(tokenAuthorityCall);
		return callRestJsonUtf8(url);
	}
	
	private void storeAccessTokenForAccountIfNecessary(String networkJsonData)
	{
		try
		{
			String accountId = getAccountIdFromNetworkResponse(networkJsonData);
			try
			{
				MartusAccountAccessToken currentToken = MartusAccountAccessToken.loadFromString(networkJsonData);
				String previousTokenString = getStoredAccessTokenForAccount(accountId);
				if(currentToken.getToken().equals(previousTokenString))
					return;
			}
			catch (Exception previousTokenDoesntExist)
			{
			}
			getStore().writeAccessTokens(accountId, networkJsonData);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	
	private String getStoredAccessTokenForAccount(String accountId) throws FileNotFoundException, IOException, TokenInvalidException, FileVerificationException, ParseException, MartusSignatureFileDoesntExistsException
	{
		MartusAccountAccessToken token = getStore().readAccessTokens(accountId);
		return token.getToken();
	}
	
	class AccountTokenFinder implements AccountVisitor
	{
		public AccountTokenFinder(MartusAccountAccessToken token)
		{
			tokenToFind = token.getToken();
			foundAccountIdForToken = null;
		}
		
		public void visit(String accountString)
		{
			try 
			{
				File tokenFile = getStore().getTokenFileForAccount(accountString);
				if(doesFilenameMatchToken(tokenFile, tokenToFind))
					foundAccountIdForToken = accountString;
			} 
			catch (Exception ignoredExceptionTokenDoesntExist) 
			{
			} 
		}

		public String getAccountIdForToken() throws TokenNotFoundException
		{
			if(foundAccountIdForToken == null)
				throw new TokenNotFoundException();
			return foundAccountIdForToken;
			
		}
		String tokenToFind;
		String foundAccountIdForToken;
	}

	public static boolean doesFilenameMatchToken(File tokenFile, String tokenToMatch) 
	{
		String actualBaseName = tokenFile.getName();
		String baseNameToMatch = FileDatabase.buildTokenFilename(tokenToMatch);
		return actualBaseName.equals(baseNameToMatch);
	}
	
	private String getStoredAccountIdForToken(MartusAccountAccessToken tokenToFind) throws TokenNotFoundException
	{
		AccountTokenFinder visitor = new AccountTokenFinder(tokenToFind);
		getDatabase().visitAllAccounts(visitor);
		return visitor.getAccountIdForToken();
	}
	

	private Vector getAccessTokensForAccount(String accountId) throws Exception
	{
		String networkTokenData = getTokensFromMartusCentralTokenAuthority(accountId);
		String tokenData = "";
		if(networkTokenData != null && networkTokenData.length() > 0)
		{
			tokenData = MartusAccountAccessToken.loadFromString(networkTokenData).getToken(); 
			storeAccessTokenForAccountIfNecessary(networkTokenData);
		}
		else
		{
			tokenData = getStoredAccessTokenForAccount(accountId);
		}
		Vector token = new Vector();
		if(tokenData.length() > 0)
			token.add(tokenData);
		return token;
	}

	public Vector getMartusAccountAccessToken(String accountId)
	{
		String accountAlias = coreServer.getClientAliasForLogging(accountId);
		String loggingData = "getMartusAccountAccessToken: " + accountAlias;
		logInfo(loggingData);
		Vector result = new Vector();
		if(isClientBanned(accountId))
		{
			result.add(NetworkInterfaceConstants.REJECTED);
			return result;
		}
		try 
		{
			Vector tokens = getAccessTokensForAccount(accountId);
			if(tokens.size() > 0)
				logInfo("Account " + accountAlias + "Token[0] is: " + tokens.get(0));
			else
				logInfo("No token found for " + accountAlias);
			result.add(NetworkInterfaceConstants.OK);
			result.add(tokens.toArray());
		} 
		catch (ConnectException e) 
		{
			logWarning("Token Authority unavailable");
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
		} 
		catch (FileNotFoundException e) 
		{
			logWarning("Token Authority unavailable, no access token");
			result.add(NetworkInterfaceConstants.NO_TOKEN_AVAILABLE);
		} 
		catch (Exception e) 
		{
			MartusLogger.logException(e);
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
		} 
		return result;
	}
	
	public String getAccountIdFromNetworkResponse(String networkResponseData) throws ParseException
	{
		EnhancedJsonObject jsonContainer = new EnhancedJsonObject(networkResponseData);
		EnhancedJsonObject innerPackage = jsonContainer.getJson(MartusAccountAccessToken.MARTUS_ACCOUNT_ACCESS_TOKEN_JSON_TAG);
		return innerPackage.getString(MartusAccountAccessToken.MARTUS_ACCESS_ACCOUNT_ID_JSON_TAG);
	}

	public String getTokenFromNetworkResponse(String networkResponseData) throws ParseException
	{
		EnhancedJsonObject jsonContainer = new EnhancedJsonObject(networkResponseData);
		EnhancedJsonObject innerPackage = jsonContainer.getJson(MartusAccountAccessToken.MARTUS_ACCOUNT_ACCESS_TOKEN_JSON_TAG);
		return innerPackage.getString(MartusAccountAccessToken.MARTUS_ACCESS_TOKEN_JSON_TAG);
	}

	private String getAccountIdForToken(MartusAccountAccessToken tokenToFind) throws Exception
	{
		String networkTokenAccountAccessData = getAccountIdForTokenFromMartusCentralTokenAuthority(tokenToFind);
		if(networkTokenAccountAccessData != null && networkTokenAccountAccessData.length() > 0)
		{
			try 
			{
				MartusAccountAccessToken.validateTokenFromJsonObject(networkTokenAccountAccessData);
				String accountId = getAccountIdFromNetworkResponse(networkTokenAccountAccessData);
				String tokenReturned = getTokenFromNetworkResponse(networkTokenAccountAccessData);
				if(!tokenToFind.getToken().equals(tokenReturned))
					throw new TokenNotFoundException();
				storeAccessTokenForAccountIfNecessary(networkTokenAccountAccessData);
				return accountId;
			} 
			catch (ParseException unexpectedParseErrorFromMTA) 
			{
				MartusLogger.logException(unexpectedParseErrorFromMTA);
			}
		}
		return getStoredAccountIdForToken(tokenToFind);
	}

	public Vector getMartusAccountIdFromAccessToken(String accountId, MartusAccountAccessToken tokenToUse)
	{
		String loggingData = "getMartusAccountIdFromAccessToken: " + coreServer.getClientAliasForLogging(accountId);
		logInfo(loggingData);
		Vector result = new Vector();
		if(isClientBanned(accountId))
		{
			result.add(NetworkInterfaceConstants.REJECTED);
			return result;
		}
		try 
		{
			String accountIdForToken = getAccountIdForToken(tokenToUse);
			String accountAlias = coreServer.getClientAliasForLogging(accountIdForToken);
			logInfo("Token " + tokenToUse + " is for account: " + accountAlias);
			result.add(NetworkInterfaceConstants.OK);
			Vector accountIds = new Vector();
			accountIds.add(accountIdForToken);
			result.add(accountIds.toArray());
		} 
		catch (MartusAccountAccessToken.TokenNotFoundException e) 
		{
			logWarning("Token not found");
			result.add(NetworkInterfaceConstants.NO_TOKEN_AVAILABLE);
		} 
		catch (Exception e) 
		{
			MartusLogger.logException(e);
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
		} 
		return result;
	}
	
	public static String calculateFileNameFromString(String inputText) throws CreateDigestException  
	{
		return MartusCrypto.getHexDigest(inputText) + CustomFieldTemplate.CUSTOMIZATION_TEMPLATE_EXTENSION;
	}
	
	public Vector putFormTemplate(String myAccountId, Vector formTemplateData) 
	{
		String loggingData = "putFormTemplate: " + coreServer.getClientAliasForLogging(myAccountId);
		logInfo(loggingData);
		Vector result = new Vector();
		if(isClientBanned(myAccountId))
		{
			result.add(NetworkInterfaceConstants.REJECTED);
			return result;
		}
		if(!canClientUpload(myAccountId))
		{
			result.add(NetworkInterfaceConstants.REJECTED);
			return result;
		}
		if(formTemplateData.size() != 1 || formTemplateData.get(0).toString().length() == 0)
		{
			logWarning("INVALID_DATA: form Template size incorrect");
			result.add(NetworkInterfaceConstants.INVALID_DATA);
			return result;
		}
		try 
		{
			String base64TemplateData = (String)formTemplateData.get(0);

			saveBase64FormTemplate(myAccountId, base64TemplateData);
			result.add(NetworkInterfaceConstants.OK);
			return result;
		} 
		catch (FormTemplateParsingException e)
		{
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
			return result;
		}
		catch (Exception e) 
		{
			logError(e);
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
			return result;
		}
	}

	public void saveBase64FormTemplate(String myAccountId, String base64TemplateData) throws Exception
	{
		ServerBulletinStore store = getStore();
		MartusCrypto security = getSecurity();
		LoggerInterface logger = this;

		saveBase64FormTemplate(store, myAccountId, base64TemplateData, security, logger);
	}

	public static synchronized void saveBase64FormTemplate(ServerBulletinStore store,
			String myAccountId, String base64TemplateData,
			MartusCrypto security, LoggerInterface logger) throws Exception
	{
		StringReader reader = new StringReader(base64TemplateData);
		
		// FIXME: The following line of code creates the account directory,
		// so the following call won't throw an exception
		store.getDatabase().getFolderForAccount(myAccountId);
		File accountFolderForTemplates = store.getAbsoluteFormTemplatesFolderForAccount(myAccountId);
		
		accountFolderForTemplates.mkdirs();			
		
		File tempFormTemplateFile = File.createTempFile("Temp-", null, accountFolderForTemplates);
		tempFormTemplateFile.deleteOnExit();
		FileOutputStream output = new FileOutputStream(tempFormTemplateFile);
		StreamableBase64.decode(reader, output);
		output.flush();
		output.close();
		
		CustomFieldTemplate template = new CustomFieldTemplate();
		boolean templateImported = template.importTemplate(tempFormTemplateFile, security);
		if(!templateImported)
		{
			logger.logError("Import Template Failed!");
			for (Object rawError : template.getErrors()) 
			{
				String message = "- " + rawError.toString();
				logger.logError(message);
			}
			tempFormTemplateFile.delete();
			throw new FormTemplateParsingException();
		}
		File accountsFormTemplateFile = new File(accountFolderForTemplates, calculateFileNameFromString(template.getTitle()));
		store.moveFormTemplateIntoAccount(myAccountId, tempFormTemplateFile, accountsFormTemplateFile, logger);
	}

	private Vector getFormTemplateTitleAndDescriptionsForAccount(String accountToGetFormsFrom) throws Exception 
	{
		ServerBulletinStore store = getStore();
		MartusCrypto security = getSecurity();

		return store.getFormTemplateTitleAndDescriptionsForAccount(accountToGetFormsFrom, security);
	}

	public Vector getListOfFormTemplates(String myAccountId, String accountIdToUse) 
	{
		String loggingData = "getListOfFormTemplates: " + coreServer.getClientAliasForLogging(myAccountId);
		logInfo(loggingData);
		Vector result = new Vector();
		if(isClientBanned(myAccountId))
		{
			result.add(NetworkInterfaceConstants.REJECTED);
			return result;
		}
		
		if(!doesAccountExist(accountIdToUse))
		{
			result.add(NetworkInterfaceConstants.ACCOUNT_NOT_FOUND);
			return result;
		}

		try 
		{
			Vector formTemplateTitleAndDescriptionsForAccount = getFormTemplateTitleAndDescriptionsForAccount(accountIdToUse);
			result.add(NetworkInterfaceConstants.OK);
			result.add(formTemplateTitleAndDescriptionsForAccount.toArray());
			String formTemplatesFoundInfo = "Templates Found:" + formTemplateTitleAndDescriptionsForAccount.size();
			logInfo(formTemplatesFoundInfo);
		} 
		catch (Exception e) 
		{
			MartusLogger.logException(e);
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
		}

		return result;
	}

	public boolean doesAccountExist(String accountIdToUse) 
	{
		try 
		{
			File accountTemplatesFolder = getStore().getAbsoluteFormTemplatesFolderForAccount(accountIdToUse);
			return accountTemplatesFolder.getParentFile().exists();
		} 
		catch (Exception e) 
		{
		}
		return false;
	}
	
	public Vector getFormTemplate(String myAccountId, String accountIdToUse, String formTitle)
	{
		String loggingData = "getFormTemplate: " + coreServer.getClientAliasForLogging(myAccountId);
		logInfo(loggingData);
		Vector result = new Vector();
		if(isClientBanned(myAccountId))
		{
			result.add(NetworkInterfaceConstants.REJECTED);
			return result;
		}
		try 
		{
			String formTemplateFileName = calculateFileNameFromString(formTitle);
			File formTemplateFile = getStore().getFormTemplateFileFromAccount(accountIdToUse, formTemplateFileName);
			byte [] rawFormTemplateData = MartusServerUtilities.getFileContents(formTemplateFile);
			String base64FormTemplateData = StreamableBase64.encode(rawFormTemplateData);
			result.add(NetworkInterfaceConstants.OK);
			Vector templateData = new Vector();
			templateData.add(base64FormTemplateData);
			result.add(templateData.toArray());
		} 
		catch (FileNotFoundException e) 
		{
			MartusLogger.logException(e);
			result.add(NetworkInterfaceConstants.FORM_TEMPLATE_DOES_NOT_EXIST);
		}
		catch (Exception e) 
		{
			MartusLogger.logException(e);
			result.add(NetworkInterfaceConstants.SERVER_ERROR);
		}
		return result;
	}

	public Vector getPacket(String myAccountId, String authorAccountId, String bulletinLocalId, String packetLocalId)
	{
		return coreServer.getPacket(myAccountId, authorAccountId, bulletinLocalId, packetLocalId);
	}

	public Vector getServerCompliance()
	{
		return coreServer.getServerCompliance();
	}
	
	public Vector getPartialUploadStatus(String authorAccountId, String bulletinLocalId, Vector extraParameters)
	{
		return coreServer.getPartialUploadStatus(authorAccountId, bulletinLocalId, extraParameters);
	}

	public String putBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data)
	{
		return coreServer.putBulletinChunk(myAccountId, authorAccountId, bulletinLocalId, totalSize, chunkOffset, chunkSize, data);
	}

	public String putContactInfo(String myAccountId, Vector parameters)
	{
		return coreServer.putContactInfo(myAccountId, parameters);
	}

	public Vector listMySealedBulletinIds(String myAccountId, Vector retrieveTags)
	{
		SummaryCollector summaryCollector = new MySealedSummaryCollector(coreServer, myAccountId, retrieveTags);
		return collectBulletinSummaries(summaryCollector, "listMySealedBulletinIds ");
	}

	public Vector listFieldOfficeDraftBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
	{
		SummaryCollector summaryCollector = new FieldOfficeDraftSummaryCollector(coreServer, hqAccountId, authorAccountId, retrieveTags);
		return collectBulletinSummaries(summaryCollector, "listFieldOfficeDraftBulletinIds ");
	}

	public Vector listFieldOfficeSealedBulletinIds(String hqAccountId, String authorAccountId, Vector retrieveTags)
	{
		SummaryCollector summaryCollector = new FieldOfficeSealedSummaryCollector(coreServer, hqAccountId, authorAccountId, retrieveTags);
		return collectBulletinSummaries(summaryCollector, "listFieldOfficeSealedBulletinIds ");
	}

	public Vector listMyDraftBulletinIds(String authorAccountId, Vector retrieveTags)
	{
		SummaryCollector summaryCollector = new MyDraftSummaryCollector(coreServer, authorAccountId, retrieveTags);
		return collectBulletinSummaries(summaryCollector, "listMyDraftBulletinIds ");
	}

	public String deleteDraftBulletins(String accountId, Vector originalRequest, String signature)
	{
		if(isClientBanned(accountId) )
			return NetworkInterfaceConstants.REJECTED;
		
		if( coreServer.isShutdownRequested() )
			return NetworkInterfaceConstants.SERVER_DOWN;
		
		int idCount = ((Integer)originalRequest.get(0)).intValue();
		String[] localIds = new String[idCount];
		for (int i = 0; i < localIds.length; i++)
		{
			localIds[i] = (String)originalRequest.get(1+i);
		}
			
		String result = NetworkInterfaceConstants.OK;
		for (int i = 0; i < localIds.length; i++)
		{
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localIds[i]);
			try
			{
				if(coreServer.doesDraftExist(uid))
				{
					writeDELPacket(uid, originalRequest, signature);
					DatabaseKey key = DatabaseKey.createDraftKey(uid);
					getStore().deleteBulletinRevision(key);
				}
				else
				{
					logError("deleteDraftBulletins: Draft not Found:"+accountId+" : "+localIds[i]);
					result =  NetworkInterfaceConstants.INCOMPLETE;
				}
			}
			catch (Exception e)
			{
				result = NetworkInterfaceConstants.INCOMPLETE;
				logError("deleteDraftBulletins:", e);
			}
		}
		return result;
	}
	
	private void writeDELPacket(UniversalId uid, Vector originalRequest, String signature) throws IOException, RecordHiddenException
	{
		DeleteRequestRecord delRecord = new DeleteRequestRecord(uid.getAccountId(), originalRequest, signature);
		getStore().writeDel(uid, delRecord);
	}

	public Vector listFieldOfficeAccounts(String hqAccountId)
	{
		return coreServer.listFieldOfficeAccounts(hqAccountId);
	}
	
	// begin NON-SSL interface (sort of)
	public String authenticateServer(String tokenToSign)
	{
		return coreServer.authenticateServer(tokenToSign);
	}

	public String ping()
	{
		return coreServer.ping();
	}
	
	public Vector getServerInformation()
	{
		return coreServer.getServerInformation();
	}
	
	public String requestUploadRights(String clientId, String tryMagicWord)
	{
		boolean uploadGranted = false;
		
		if(isValidMagicWord(tryMagicWord))
			uploadGranted = true;
			
		if(!coreServer.areUploadRequestsAllowedForCurrentIp())
		{
			if(!uploadGranted)
				coreServer.incrementFailedUploadRequestsForCurrentClientIp();
			return NetworkInterfaceConstants.SERVER_ERROR;
		}
		
		if( coreServer.isClientBanned(clientId) )
			return NetworkInterfaceConstants.REJECTED;
			
		if( coreServer.isShutdownRequested() )
			return NetworkInterfaceConstants.SERVER_DOWN;
			
		if(tryMagicWord.length() == 0 && coreServer.canClientUpload(clientId))
			return NetworkInterfaceConstants.OK;
		
		if(!uploadGranted)
		{
			coreServer.logError("requestUploadRights: Rejected " + coreServer.getPublicCode(clientId) + " tryMagicWord=" +tryMagicWord);
			coreServer.incrementFailedUploadRequestsForCurrentClientIp();
			return NetworkInterfaceConstants.REJECTED;
		}
		
		allowUploads(clientId, tryMagicWord);
		return NetworkInterfaceConstants.OK;
	}
	

	
	
	
	
	
	private Vector collectBulletinSummaries(SummaryCollector summaryCollector, String methodName)
	{
		String myAccountId = summaryCollector.callerAccountId();
		String clientAliasForLogging = coreServer.getClientAliasForLogging(myAccountId);
		logInfo(methodName + clientAliasForLogging);
		
		if(isClientBanned(myAccountId) )
			return coreServer.returnSingleErrorResponseAndLog("  returning REJECTED", NetworkInterfaceConstants.REJECTED);
		
		if( coreServer.isShutdownRequested() )
			return coreServer.returnSingleErrorResponseAndLog( " returning SERVER_DOWN", NetworkInterfaceConstants.SERVER_DOWN );
		
		Vector summaries = summaryCollector.collectSummaries();
		Vector result = new Vector();
		result.add(NetworkInterfaceConstants.OK);
		result.add(summaries.toArray());
		String authorAccountId = summaryCollector.authorAccountId;
		logNotice(methodName +"caller: "+clientAliasForLogging+
				 " author: " + coreServer.getClientAliasForLogging(authorAccountId) + 
				 " Exit: Ids="+summaries.size());
		return result;
	}

	File getBannedFile()
	{
		return new File(getConfigDirectory(), BANNEDCLIENTSFILENAME);
	}
	
	File getTestAccountsFile()
	{
		return new File(getConfigDirectory(), TESTACCOUNTSFILENAME);
	}

	public synchronized void loadBannedClients()
	{
		loadBannedClients(getBannedFile());
	}
	
	public void loadBannedClients(File bannedClientsFile)
	{
		clientsBanned = MartusUtilities.loadClientListAndExitOnError(bannedClientsFile);
	}	
	
	public synchronized void loadTestAccounts()
	{
		loadTestAccounts(getTestAccountsFile());
	}
	
	public void loadTestAccounts(File testAccountsFile)
	{
		testAccounts = MartusUtilities.loadClientListAndExitOnError(testAccountsFile);
	}	
	
	private void loadNews()
	{
		newsItems = new Vector();
		Vector newsItemSortedFileList = DirectoryUtils.getAllFilesLeastRecentFirst(getNewsDirectory());
		for(int i = 0; i < newsItemSortedFileList.size(); i++)
		{
			File newsFile = (File)newsItemSortedFileList.get(i);
			if(isNewsFile(newsFile))
				addNewsItem(newsFile);
		}
	}

	private void addNewsItem(File newsFile)
	{
		try
		{
			String fileContents = UnicodeReader.getFileContents(newsFile);
			Date fileDate = new Date(newsFile.lastModified());
			SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			String dateAndData = format.format(fileDate) + System.getProperty("line.separator") + fileContents; 
			newsItems.add(dateAndData);
		}
		catch(IOException e)
		{
			logError("getNews:Error reading File:" + newsFile.getAbsolutePath(), e);
		}
	}
	
	private boolean isNewsFile(File fileToTest)
	{
		String fileName = fileToTest.getName();
		if(fileName.endsWith("#"))
			return false;
		if(fileName.endsWith("~"))
			return false;
		return true;
	}
	
	public int getNumberOfNewsItems()
	{
		return newsItems.size();
	}
	
	public String getGroupNameForMagicWord(String tryMagicWord)
	{
		MagicWordEntry entry = magicWords.getMagicWordEntry(tryMagicWord);
		if(entry==null)
			return "";
		return entry.getGroupName();
	}

	public String getHumanReadableMagicWord(String magicWordToUse)
	{
		MagicWordEntry entry = magicWords.getMagicWordEntry(magicWordToUse);
		if(entry==null)
			return "";
		return entry.getMagicWord();
	}
	
	public boolean isValidMagicWord(String magicWordToUse)
	{
		return (magicWords.isValidMagicWord(magicWordToUse));
	}
	
	public void addMagicWordForTesting(String newMagicWordInfo, String groupInfo)
	{
		magicWords.add(newMagicWordInfo, groupInfo);
	}
	
	public File getMagicWordsFile()
	{
		return new File(getConfigDirectory(), MAGICWORDSFILENAME);
	}

	void loadMagicWordsFile() throws IOException
	{
		magicWords.loadMagicWords(getMagicWordsFile());
	}

	public synchronized void allowUploads(String clientId, String magicWordUsed)
	{
		String magicWord = getHumanReadableMagicWord(magicWordUsed);
		String groupName = getGroupNameForMagicWord(magicWordUsed);
		
		logNotice("allowUploads granted to: " + coreServer.getClientAliasForLogging(clientId) + " : " + clientId + " groupName= " + groupName + " with magicword=" + magicWord);
		clientsThatCanUpload.add(clientId);
		
		try
		{
			UnicodeWriter writer = new UnicodeWriter(getAllowUploadFile(), UnicodeWriter.APPEND);
			writer.writeln(clientId);
			writer.close();
			MartusCrypto security = getSecurity();
			MartusServerUtilities.createSignatureFileFromFileOnServer(getAllowUploadFile(), security);
			
			AuthorizeLog authorizeLog = new AuthorizeLog(security, coreServer.getLogger(), getAuthorizeLogFile());
			String publicCode = getPublicCode(clientId);
			authorizeLog.appendToFile(new AuthorizeLogEntry(publicCode, groupName));

			logNotice("allowUploads : Exit OK");
		}
		catch(Exception e)
		{
			logError("allowUploads", e);
			//TODO: Should report error back to user. Shouldn't update in-memory list
			// (clientsThatCanUpload) until AFTER the file has been written
		}
	}

	public File getAllowUploadFile()
	{
		return new File(coreServer.getDataDirectory(), UPLOADSOKFILENAME);
	}
	
	public File getAuthorizeLogFile()
	{
		return new File(coreServer.getDataDirectory(), AUTHORIZELOGFILENAME);
	}

	void loadCanUploadFile()
	{
		logInfo("loadCanUploadList");
		clientsThatCanUpload = MartusUtilities.loadClientList(getAllowUploadFile());
	}
	
	public synchronized void loadCanUploadList(BufferedReader canUploadInput)
	{
		logInfo("loadCanUploadList");

		try
		{
			clientsThatCanUpload = MartusUtilities.loadListFromFile(canUploadInput);
		}
		catch (IOException e)
		{
			clientsThatCanUpload = new Vector();
			logError("loadCanUploadList -- Error loading can-upload list: ", e);
		}
		
		logNotice("loadCanUploadList : Exit OK");
	}
	
	abstract class MySummaryCollector extends SummaryCollector
	{
		public MySummaryCollector(MartusServer serverToUse, String authorAccount, Vector retrieveTags) 
		{
			super(serverToUse, authorAccount, retrieveTags);
		}

		public boolean isAuthorized(BulletinHeaderPacket bhp)
		{
			return true;
		}

		public String callerAccountId()
		{
			return authorAccountId;
		}
	
	}
	

	class MySealedSummaryCollector extends MySummaryCollector
	{
		public MySealedSummaryCollector(MartusServer serverToUse, String authorAccount, Vector retrieveTags) 
		{
			super(serverToUse, authorAccount, retrieveTags);
		}

		public boolean isWanted(DatabaseKey key)
		{
			return(key.isSealed());
		}
	}

	class MyDraftSummaryCollector extends MySummaryCollector
	{
		public MyDraftSummaryCollector(MartusServer serverToUse, String authorAccount, Vector retrieveTagsToUse) 
		{
			super(serverToUse, authorAccount, retrieveTagsToUse);
		}

		public boolean isWanted(DatabaseKey key)
		{
			return(key.isDraft());
		}
	}
	
	
	
	abstract class FieldOfficeSummaryCollector extends SummaryCollector
	{
		public FieldOfficeSummaryCollector(MartusServer serverToUse, String hqAccountIdToUse, String authorAccountIdToUse, Vector retrieveTagsToUse) 
		{
			super(serverToUse, authorAccountIdToUse, retrieveTagsToUse);
			hqAccountId = hqAccountIdToUse;

		}
		
		String hqAccountId;

		public boolean isAuthorized(BulletinHeaderPacket bhp)
		{
			return(bhp.isHQAuthorizedToRead(hqAccountId));
		}

		public String callerAccountId()
		{
			return hqAccountId;
		}
	}

	class FieldOfficeSealedSummaryCollector extends FieldOfficeSummaryCollector
	{
		public FieldOfficeSealedSummaryCollector(MartusServer serverToUse, String hqAccountIdToUse, String authorAccountIdToUse, Vector retrieveTagsToUse) 
		{
			super(serverToUse, hqAccountIdToUse, authorAccountIdToUse, retrieveTagsToUse);
		}

		public boolean isWanted(DatabaseKey key)
		{
			return(key.isSealed());
		}
	}

	class FieldOfficeDraftSummaryCollector extends FieldOfficeSummaryCollector
	{
		public FieldOfficeDraftSummaryCollector(MartusServer serverToUse, String hqAccountIdToUse, String authorAccountIdToUse, Vector retrieveTagsToUse) 
		{
			super(serverToUse, hqAccountIdToUse, authorAccountIdToUse, retrieveTagsToUse);
		}

		public boolean isWanted(DatabaseKey key)
		{
			return(key.isDraft());
		}
	}

	MartusServer coreServer;
	private int activeClientsCounter;
	private int loggedNumberOfActiveClients;
	MagicWords magicWords;
	
	public Vector clientsThatCanUpload;
	public Vector clientsBanned;
	public Vector testAccounts;
	private Vector activeWebServers;
	private Vector newsItems;
	
	public static final String TESTACCOUNTSFILENAME = "isTester.txt";
	public static final String BANNEDCLIENTSFILENAME = "banned.txt";
	public static final String UPLOADSOKFILENAME = "uploadsok.txt";
	public static final String AUTHORIZELOGFILENAME = "authorizelog.txt";
	private static final String MAGICWORDSFILENAME = "magicwords.txt";
	private static final String CLIENTNEWSDIRECTORY = "news";
	
	private static final int[] defaultNonSSLPorts = {988, 80};
}
