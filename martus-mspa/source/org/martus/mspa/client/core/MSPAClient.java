/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2004-2007, Beneficent
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


package org.martus.mspa.client.core;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.martus.clientside.PasswordHelper;
import org.martus.common.ContactInfo;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MiniLocalization;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.MartusXmlrpcClient.SSLSocketSetupException;
import org.martus.mspa.common.network.ClientSideXmlRpcHandler;
import org.martus.mspa.common.network.NetworkInterfaceConstants;
import org.martus.mspa.main.UiMainWindow;
import org.martus.mspa.server.LoadMartusServerArguments;
import org.martus.util.DirectoryUtils;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

public class MSPAClient 
{				
	public MSPAClient(MiniLocalization local) throws Exception
	{		
		security = new MartusSecurity();	
	}
	
	public void setXMLRpcEnviornments() throws Exception
	{
		handler = createXmlRpcNetworkInterfaceHandler();					
		setServerPublicCode(serverPublicCode);	
	}
	
	public ClientSideXmlRpcHandler getClientSideXmlRpcHandler()
	{	
		return handler;
	}
	
	public void setPortToUse(int port)
	{
		portToUse = port;
	}		
	
	private void setServerPublicCode(String key)
	{	

		String strippedServerPublicCode = MartusCrypto.removeNonDigits(key);
		handler.getSimpleX509TrustManager().setExpectedPublicCode(strippedServerPublicCode);	
	}
				
	private ClientSideXmlRpcHandler createXmlRpcNetworkInterfaceHandler() throws Exception
	{		
		return handler =  new ClientSideXmlRpcHandler(ipToUse, portToUse);			
	}	
	
	public File getUiStateFile()
	{
		return new File(UiMainWindow.getDefaultDirectoryPath(), "UiState.dat");
	}
	
	public MartusCrypto getSecurity()
	{
		return security;
	}
	
	public File getKeypairFile()
	{
		return new File(UiMainWindow.getDefaultDirectoryPath(), KEYPAIR_FILE);
	}
	
	public File[] loadListOfConfiguredServers() throws Exception
	{
		portToUse = DEFAULT_PORT;					
		File serverToCallDirectory = getServerToCallDirectory();
		serverToCallDirectory.mkdirs();
		return DirectoryUtils.listFiles(serverToCallDirectory);
	}
	
	public Vector getLinesOfServerIpAndPublicCode(File[] toCallFiles) throws
			IOException, 
			MartusUtilities.InvalidPublicKeyFileException, 
			MartusUtilities.PublicInformationInvalidException, 
			SSLSocketSetupException, InvalidBase64Exception
	{
		Vector listOfServers = new Vector();	

		for (int i=0; i<toCallFiles.length;i++)
		{	
			File toCallFile = toCallFiles[i];
			if(!toCallFile.isDirectory())
			{
				try
				{
					String ipToCall = MartusUtilities.extractIpFromFileName(toCallFile.getName());
					Vector publicInfo = MartusUtilities.importServerPublicKeyFromFile(toCallFile, security);
					String serverPublicKey = (String)publicInfo.get(0);	
					if (serverPublicKey != null)
					{				
						String nonFormatPublicCode = MartusCrypto.computePublicCode(serverPublicKey);
						String serverPublicCall = MartusCrypto.formatPublicCode(nonFormatPublicCode);
						listOfServers.add(ipToCall+"\t"+serverPublicCall);		
					}
				}
				catch(Exception e)
				{
					MartusLogger.logException(e);
					JOptionPane.showMessageDialog(null, "Skipping invalid server public key file: " + toCallFile);
				}
			}
		}
		
		return listOfServers;
	}	
	
	public File getServerToCallDirectory()
	{
		return new File(UiMainWindow.getDefaultDirectoryPath(), MSPA_SERVERS_DIRECTORY);
	}
	
	
	public void signIn(String userName, char[] userPassPhrase) throws Exception
	{
		getSecurity().readKeyPair(getKeypairFile(), PasswordHelper.getCombinedPassPhrase(userName, userPassPhrase));
		MartusLogger.log("MSPA Client Public Code (old): " + MartusCrypto.computeFormattedPublicCode(getSecurity().getPublicKeyString()));
		MartusLogger.log("MSPA Client Public Code (new): " + MartusCrypto.computeFormattedPublicCode40(getSecurity().getPublicKeyString()));
	}
	
	public String getStatus()
	{
		return currentStatus;
	}
	
	public String getCurrentServerPublicCode()
	{
		return serverPublicCode;
	}
	
	public String getCurrentServerIp()
	{
		return ipToUse;
	}
	
	public void setCurrentServerPublicCode(String publicCode)
	{
		serverPublicCode = publicCode;
	}
	
	public void setCurrentServerIp(String ip)
	{
		ipToUse = ip;
	}
	
	
	public String getServerStatus() throws Exception
	{
		return (String)handler.ping().get(0);
	}
	
	private Vector getAccountIds(String myAccountId, Vector parameters, String signature) throws IOException 
	{	
		return handler.getAccountIds(myAccountId, parameters, signature);
	}	
	
	public Vector displayAccounts() throws Exception
	{			
		Vector parameters = new Vector();							
		String signature = security.createSignatureOfVectorOfStrings(parameters);	
		Vector results = getAccountIds(security.getPublicKeyString(), parameters, signature);					
		currentStatus = (String) results.get(0);
		
		if (currentStatus.equals(NetworkInterfaceConstants.OK))
			return (Vector) results.get(1);

		throw new Exception("Error retrieving accounts: " + currentStatus);
	}
	
	public Vector getContactInfo(String accountId) throws Exception
	{	
		Vector parameters = new Vector();
		parameters.add(accountId);			
		String signature = security.createSignatureOfVectorOfStrings(parameters);				
		Vector results = handler.getContactInfo(security.getPublicKeyString(), parameters, signature, accountId);
		currentStatus = (String) results.get(0);				
		Vector decodedContactInfoResult = ContactInfo.decodeContactInfoVectorIfNecessary(results);
		
		if (decodedContactInfoResult != null && !decodedContactInfoResult.isEmpty())
		{				
			if (currentStatus.equals(NetworkInterfaceConstants.OK))
				return (Vector) decodedContactInfoResult.get(1);
		}	 
		return new Vector();
	}
	
	public Vector getAccountManageInfo(String manageAccountId) throws Exception
	{	
		Vector results = handler.getAccountManageInfo(security.getPublicKeyString(), manageAccountId);
		currentStatus = (String) results.get(0);
		if (results != null && currentStatus.equals(NetworkInterfaceConstants.OK))
			return (Vector) results.get(1);
		return new Vector();
	}	
	
	public String getServerCompliant() throws Exception
	{
		StringBuffer msg = new StringBuffer();
		Vector results = handler.getServerCompliance(security.getPublicKeyString());
		currentStatus = (String) results.get(0);
		if (results != null && currentStatus.equals(NetworkInterfaceConstants.OK))
		{
			Vector compliants = (Vector) results.get(1);				
			for (int i=0; i< compliants.size();++i)
				msg.append((String) compliants.get(i)).append("\n");
		}				
		return msg.toString();
	}
	
	public Vector updateServerCompliant(String msg) throws Exception
	{
		Vector results =  handler.updateServerCompliance(security.getPublicKeyString(), msg);
		currentStatus = (String) results.get(0);
		if (results != null && currentStatus.equals(NetworkInterfaceConstants.OK))
			return results;
		return new Vector();
	}
	
	public Vector sendCommandToServer(String cmdType, String cmd) throws Exception
	{		
		return handler.sendCommandToServer(security.getPublicKeyString(), cmdType, cmd);						
	}
	
	public void updateAccountManageInfo(String manageAccountId, Vector manageOptions) throws Exception
	{		
		handler.updateAccountManageInfo(security.getPublicKeyString(),
				manageAccountId, manageOptions);				
	}		
	
	public  LoadMartusServerArguments getMartusServerArguments() throws Exception
	{			
		Vector results = handler.getMartusServerArguments(security.getPublicKeyString());
		currentStatus = (String) results.get(0);		
		if (results != null && currentStatus.equals(NetworkInterfaceConstants.OK))	
		{	
			Vector args = (Vector) results.get(1);
			LoadMartusServerArguments arguments = new LoadMartusServerArguments();
			arguments.convertFromVector(args);

			return arguments;				
		}
		return null;
	}	
	
	public void updateMartusServerArguments(LoadMartusServerArguments args) throws Exception
	{		
		handler.updateMartusServerArguments(security.getPublicKeyString(), args.convertToVector());				
	}	
	
	public Vector getListOfHiddenBulletins(String accountId) throws Exception
	{	
		Vector results = handler.getListOfHiddenBulletinIds(security.getPublicKeyString(), accountId);
		currentStatus = (String) results.get(0);		
		if (results != null && currentStatus.equals(NetworkInterfaceConstants.OK))	
			return (Vector) results.get(1);
		return new Vector();
	}	
	
	public String removeBulletin(String accountId, Vector localIds) throws Exception
	{		
		Vector results = handler.hideBulletins(security.getPublicKeyString(), accountId, localIds);
		currentStatus = (String) results.get(0);			
		return currentStatus;
	}
	
	public String recoverHiddenBulletin(String accountId, Vector localIds) throws Exception
	{		
		Vector results = handler.unhideBulletins(security.getPublicKeyString(), accountId, localIds);
		currentStatus = (String) results.get(0);				
		return currentStatus;
	}
	
	public Vector getPacketDirNames(String accountId) throws Exception
	{	
		Vector results = handler.getListOfBulletinIds(accountId);		
		currentStatus = (String) results.get(0);	
		if (results != null && currentStatus.equals(NetworkInterfaceConstants.OK))
			return (Vector) results.get(1);
							
		return new Vector();
	}	
	
	public Vector getInactiveMagicWords() throws Exception
	{	
		Vector results = handler.getInactiveMagicWords(security.getPublicKeyString());
		currentStatus = (String) results.get(0);
		if (results != null && currentStatus.equals(NetworkInterfaceConstants.OK))
			return (Vector) results.get(1);
		return new Vector();
	}	
	
	public Vector getActiveMagicWords() throws Exception
	{	
		Vector results = handler.getActiveMagicWords(security.getPublicKeyString());
		currentStatus = (String) results.get(0);
		if (results != null && currentStatus.equals(NetworkInterfaceConstants.OK))
			return (Vector) results.get(1);			
		return new Vector();
	}	
	
	public Vector getAllMagicWords() throws Exception
	{	
		Vector results = handler.getAllMagicWords(security.getPublicKeyString());
		currentStatus = (String) results.get(0);
		if (results != null && currentStatus.equals(NetworkInterfaceConstants.OK))
			return (Vector) results.get(1);
		return new Vector();
	}		
	
	public void updateMagicWords(Vector magicWords)	throws Exception
	{	
		handler.updateMagicWords(security.getPublicKeyString(), magicWords);			
	}	
	
	public Vector getAvailableAccounts() throws Exception
	{	
		Vector results = handler.getListOfAvailableServers(security.getPublicKeyString());
		currentStatus = (String) results.get(0);
		if (results != null && currentStatus.equals(NetworkInterfaceConstants.OK))
			return (Vector) results.get(1);
		return new Vector();
	}	
	
	public Vector getListOfOtherServers(int mirrorType) throws Exception
	{	
		Vector results = handler.getListOfAssignedServers(security.getPublicKeyString(), mirrorType);
		currentStatus = (String) results.get(0);
		if (results != null && currentStatus.equals(NetworkInterfaceConstants.OK))
			return (Vector) results.get(1);
		return new Vector();
	}	
	
	public boolean addMirrorServer(Vector serverInfo) throws Exception
	{	
		Vector results = handler.addAvailableServer(security.getPublicKeyString(), serverInfo);
		currentStatus = (String) results.get(0);
		if (results != null)
		{
			String returnCode = (String) results.get(0);		
			if (returnCode.equals(NetworkInterfaceConstants.OK))
				return true;
		}	 
		return false;
	}	
	
	public void updateManageMirrorAccounts(Vector mirrorInfo, int manageType) throws Exception
	{	
		handler.updateAssignedServers(security.getPublicKeyString(), mirrorInfo, manageType);			
	}		
		

	public void exportServerPublicKeyFile(File outputFile) throws Exception
	{
		MartusUtilities.exportServerPublicKey(security, outputFile);			
	}
	
	public void warningMessageDlg(String message)
	{
		String title = "MSPA Client";
		String cause = message;
		String ok = "OK";
		String[] buttons = { ok };
		JOptionPane pane = new JOptionPane(cause, JOptionPane.INFORMATION_MESSAGE,
				 JOptionPane.DEFAULT_OPTION, null, buttons);
		JDialog dialog = pane.createDialog(null, title);
		dialog.setVisible(true);
	}
				
	
	ClientSideXmlRpcHandler handler;
	String ipToUse="";
	int portToUse;
	String serverPublicCode="";
	MiniLocalization localization;
	MartusCrypto security;
	File keyPairFile;	
	String currentStatus;
	
	public static int DEFAULT_PORT = 984;
	final static String DEFAULT_HOST = "localHost";	
	final static public boolean INCLUDE_AMPLIFICATION = false;
	
	private final static String KEYPAIR_FILE ="keypair.dat"; 
	private static final String MSPA_SERVERS_DIRECTORY = "knownServers";
	
}
