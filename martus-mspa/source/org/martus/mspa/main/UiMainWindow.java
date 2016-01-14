/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.mspa.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.martus.clientside.CurrentUiState;
import org.martus.clientside.PasswordHelper;
import org.martus.clientside.UiBasicSigninDlg;
import org.martus.clientside.UiLocalization;
import org.martus.common.MartusLogger;
import org.martus.common.Version;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.mspa.client.core.MSPAClient;
import org.martus.mspa.client.view.AccountDetailPanel;
import org.martus.mspa.client.view.AccountsTree;
import org.martus.mspa.client.view.ServerConnectionDlg;
import org.martus.mspa.client.view.menuitem.MenuItemAboutHelp;
import org.martus.mspa.client.view.menuitem.MenuItemExitApplication;
import org.martus.mspa.client.view.menuitem.MenuItemExportPublicKey;
import org.martus.mspa.client.view.menuitem.MenuItemManageMagicWords;
import org.martus.mspa.client.view.menuitem.MenuItemManagingMirrorServers;
import org.martus.mspa.client.view.menuitem.MenuItemMartusServerCompliance;
import org.martus.mspa.client.view.menuitem.MenuItemServerCommands;
import org.martus.mspa.common.ManagingMirrorServerConstants;
import org.martus.mspa.common.network.NetworkInterfaceConstants;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.Utilities;

import com.jhlabs.awt.Alignment;
import com.jhlabs.awt.GridLayoutPlus;

public class UiMainWindow extends JFrame
{
	public UiMainWindow()
	{		
		super("Martus Server Policy Administrator (MSPA)");	
		
		try
		{
			getDefaultDirectoryPath().mkdirs();
			localization  = new MSPALocalization(getDefaultDirectoryPath(), EnglishStrings.strings);
			mspaApp = new MSPAClient(localization);		
			initalizeUiState();
		}
		catch(Exception e)
		{
			initializationErrorDlg(e.getMessage());
		}
		
		currentActiveFrame = this;						
	}
	
	public UiLocalization getLocalization()
	{
		return localization;
	}
		
	public boolean run()
	{
		
		try
		{
			File keypairFile = mspaApp.getKeypairFile();
			if(!keypairFile.exists())
			{
				createInitialKeyPair(keypairFile);
			}
			
			int result = signIn(UiBasicSigninDlg.INITIAL); 
			if(result == UiBasicSigninDlg.CANCEL)
				return false;

			if (result != UiBasicSigninDlg.SIGN_IN)
			{
				String msg = "User Name and Passphrase do not match.";
				initializationErrorDlg(msg);					
				return false;
			}

			mspaApp.loadListOfConfiguredServers();
		
			if (!selectServer())
			{
				initializationErrorDlg("Exiting because no server was selected");	
				return false;
			}
			
			try
			{
				String serverStatus = mspaApp.getServerStatus();
				if(!serverStatus.equals(NetworkInterfaceConstants.OK))
				{
					initializationErrorDlg("Server ping not ok, was: " + serverStatus);
					return false;
				}
			} 
			catch (IOException e)
			{
				MartusLogger.logException(e);
				initializationErrorDlg("Unable to connect to server");
				return false;
			}
			
			setSize(1000, 700);
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());		


			JMenuBar menuBar = createMenuBar();
			setJMenuBar(menuBar);

			createTabbedPaneRight();
			Vector accounts = mspaApp.displayAccounts();			
			accountTree = new AccountsTree(accounts, this);

			JPanel leftPanel = createServerInfoPanel(mspaApp.getCurrentServerIp(), mspaApp.getCurrentServerPublicCode());								
			
			mainPanel.add(leftPanel, BorderLayout.BEFORE_LINE_BEGINS);
			mainPanel.add(tabPane, BorderLayout.CENTER);
			mainPanel.add(createStatusInfo(), BorderLayout.AFTER_LAST_LINE);	
			setStatusText(mspaApp.getStatus());

			WindowListener wndCloser = new WindowAdapter()
			{
				public void windowClosing(WindowEvent e) 
				{
					remindMightNeedRestart();
					System.exit(0);
				}
			};
			addWindowListener(wndCloser);
			getContentPane().add(mainPanel);
				
			Utilities.centerFrame(this);	
			setVisible(true);		
							
			return true;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			initializationErrorDlg("Exiting due to an unexpected error");
			return false;
		}
	}

	private void createInitialKeyPair(File keypairFile) throws Exception
	{
		if(!confirmDialog("Create Keypair", "No keypair was found. Create a new one?", "Create"))
			return;
		
		MartusLogger.log("Missing keypair file: " + keypairFile);
		if(!createKeyPair(keypairFile))
			return;
		
		notifyDialog("Create Keypair", 
				"<html>The Keypair has been created.<br>" +
				"Next steps:<ul>" +
				"<li>Sign in, and export your public key" +
				"<li>Install this client public key on the server(s)" +
				"<li>Install server public key(s) on this client" +
				"</ul>");
	}
	
	private boolean createKeyPair(File keyPairFile) throws Exception
	{
		int mode = UiBasicSigninDlg.CREATE_NEW;
		UiBasicSigninDlg signinDlg = new UiBasicSigninDlg(localization, uiState, currentActiveFrame, mode, "", new char[0]);
		if(signinDlg.getUserChoice() != UiBasicSigninDlg.SIGN_IN)
			return false;

		char[] password = signinDlg.getPassword();
		char[] passphrase = PasswordHelper.getCombinedPassPhrase(signinDlg.getNameText(), password);
		try
		{
			MartusSecurity security = new MartusSecurity();
			security.createKeyPair();
			FileOutputStream out = new FileOutputStream(keyPairFile);
			security.writeKeyPair(out, passphrase);
			out.close();
		
			System.out.println("Public Code (old): " + MartusCrypto.computeFormattedPublicCode(security.getPublicKeyString()));
			System.out.println("Public Code (new): " + MartusCrypto.computeFormattedPublicCode40(security.getPublicKeyString()));
			return true;
		}
		finally
		{
			Arrays.fill(password, 'x');
			Arrays.fill(passphrase, 'x');
		}
	}
	
	private boolean selectServer() throws Exception
	{
		ServerConnectionDlg dlg = new ServerConnectionDlg(this);
		dlg.setVisible(true);
		
		if (mspaApp.getCurrentServerPublicCode().length() <=0)
			return false;
		
		mspaApp.setXMLRpcEnviornments();				
		return true;
	}
	
	protected JPanel createServerInfoPanel(String ipAddr, String accountId) throws Exception
	{
		JTextField ipLabel = new JTextField(InetAddress.getByName(ipAddr).getHostAddress(),20);
		ipLabel.setEditable(false);
		ipLabel.setForeground(Color.BLUE);
			
		JTextField publicCodeLabel = new JTextField(mspaApp.getCurrentServerPublicCode(),20);
		publicCodeLabel.setEditable(false);
		publicCodeLabel.setForeground(Color.BLUE);

		UiParagraphPanel serverDetails = new UiParagraphPanel();
		serverDetails.addOnNewLine(new UiLabel("IP Address: "));
		serverDetails.add(ipLabel);
		
		serverDetails.addOnNewLine(new UiLabel("Public Code:"));	
		serverDetails.add(publicCodeLabel);
		
		serverDetails.setBorder(BorderFactory.createTitledBorder("Server"));
		

		JPanel serverInfoPanel = new JPanel();
		serverInfoPanel.setLayout(new GridLayoutPlus(0, 1));
		serverInfoPanel.setBorder(BorderFactory.createTitledBorder("Client Accounts on Server"));

		DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat time = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		serverInfoPanel.add(new UiLabel("<html>" +
				"&nbsp;&nbsp;&nbsp;" +
					"Last updated " +
					"" + date.format(now) + "" +
					" at " + 
					"<b>" + time.format(now) + "</b> " + 
					"<br>" +  
				"&nbsp;&nbsp;&nbsp;<i>(to update the list, exit and " +
					"restart this MSPA application)"));	
		serverInfoPanel.add(accountTree.getScrollPane()); 	
	
		GridLayoutPlus mainPanelLayout = new GridLayoutPlus(0, 1);
		mainPanelLayout.setFill(Alignment.FILL_BOTH);
		JPanel mainPanel = new JPanel(mainPanelLayout);
		mainPanel.add(serverDetails);
		mainPanel.add(serverInfoPanel);
		return mainPanel;
	}

	protected JTextField createStatusInfo()
	{
		statusField = new JTextField(" ");
		statusField.setEditable(false);
		return statusField;
	}
	
	public void setStatusText(String msg)
	{
		statusField.setText(msg);
	}
	
	protected JTabbedPane createTabbedPaneRight()
	{
		tabPane = new JTabbedPane();				

		loadEmptyAccountDetailPanel();			
		tabPane.setTabPlacement(JTabbedPane.TOP);		
		
		return tabPane;
	}
	
	public void loadAccountDetailPanel(String accountId, String publicId) throws Exception
	{
		Vector contactInfo = mspaApp.getContactInfo(accountId);
		Vector packetDir = mspaApp.getPacketDirNames(accountId);
		Vector accountAdmin = mspaApp.getAccountManageInfo(accountId);
		Vector hiddenBulletins = mspaApp.getListOfHiddenBulletins(accountId);

		tabPane.remove(0);
		AccountDetailPanel accountDetailPanel = new AccountDetailPanel(this, accountId, contactInfo, hiddenBulletins, 
							packetDir, accountAdmin);
		tabPane.add(new UiScrollPane(accountDetailPanel), "Account Detail");			
	}		
	
	public void loadEmptyAccountDetailPanel()
	{
		if (tabPane.getTabCount() > 0)
			tabPane.remove(0);
			
		tabPane.add(new JPanel(), "Account Detail");		
	}
	
	private void initializationErrorDlg(String message)
	{
		String title = "Error Starting MSPA";
		String cause = "Unable to start MSPA: " + message;
		String ok = "OK";
		String[] buttons = { ok };
		JOptionPane pane = new JOptionPane(cause, JOptionPane.INFORMATION_MESSAGE,
				 JOptionPane.DEFAULT_OPTION, null, buttons);
		JDialog dialog = pane.createDialog(null, title);
		dialog.setVisible(true);
	}
	
	public void notifyDialog(String title, String message)
	{
		JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public boolean confirmDialog(String title, String message, String okButton)
	{
		String[] buttons = { okButton, "Cancel", };
		int result = JOptionPane.showOptionDialog(this, message, title, 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE, 
				null, 
				buttons, 
				null);

		return (result == JOptionPane.YES_OPTION);
	}
	
	public void exceptionDialog(Exception e)
	{
		notifyDialog("ERROR", "<html>An unexpected error has occurred. Check the console for details.<br>" +
				"<em>" + e.getMessage());
	}
	
	public void remindNeedsRestart()
	{
		notifyDialog("Restart Needed", "<html>The change you just made will not take effect until <br>" +
				"the next time the Martus Service is started (or restarted). <br>" +
				"This can be done from the Services menu.");
	}

	public void remindMightNeedRestart()
	{
		String amplifiers = "";
		if(MSPAClient.INCLUDE_AMPLIFICATION)
			amplifiers = "or amplifiers (search engines)";
		notifyDialog("Restart Reminder", "<html>If you made changes that require a restart, <br>" +
				"but have not yet restarted the Martus service, <br>" +
				"those changes will not yet be in effect. <br>" +
				"<br>" +
				"The changes that require a restart are:" +
				"<ul>" +
				"<li>Changes to the Compliance Statement" +
				"<li>Changes to Magic Words" +
				"<li>Changes to mirroring " + amplifiers +
				"<li>Changes to account permissions (allow uploads, ban, etc)" +
				"<li>Hiding or Unhiding bulletins" +
				"</ul>");
	}

	private int signIn(int mode) throws Exception
	{
		String iniPassword="";		
		UiBasicSigninDlg signinDlg = new UiBasicSigninDlg(localization, uiState, currentActiveFrame, mode, "", iniPassword.toCharArray());
				
		String userName = signinDlg.getNameText();
		char[] password = signinDlg.getPassword();
					
		int userChoice = signinDlg.getUserChoice();
		if (userChoice != UiBasicSigninDlg.SIGN_IN)
			return userChoice;

		if(mode == UiBasicSigninDlg.INITIAL)
			mspaApp.signIn(userName, password);
	
		return UiBasicSigninDlg.SIGN_IN;
	}		

	protected JMenuBar createMenuBar()
	{
		final JMenuBar menuBar = new JMenuBar();
	
		JMenu mFile = new JMenu("File");
		mFile.setMnemonic('f');
		mFile.add(new MenuItemExportPublicKey(this, "Export Public Key"));
		mFile.addSeparator();			
		mFile.add(new MenuItemExitApplication(this));
		menuBar.add(mFile);
		
		JMenu mTool = new JMenu("Manage");
		mTool.setMnemonic('m');
		mTool.add(new MenuItemMartusServerCompliance(this,"Compliance Statement"));
		mTool.add(new MenuItemManageMagicWords(this,"Magic Words"));	
		mTool.addSeparator();
		mTool.add(new MenuItemManagingMirrorServers(this, ManagingMirrorServerConstants.SERVERS_WHOSE_DATA_WE_BACKUP));
		mTool.add(new MenuItemManagingMirrorServers(this, ManagingMirrorServerConstants.SERVERS_WHO_BACKUP_OUR_DATA));
		
		if(MSPAClient.INCLUDE_AMPLIFICATION)
		{
			mTool.addSeparator();												
			mTool.add(new MenuItemManagingMirrorServers(this, ManagingMirrorServerConstants.SERVERS_WHOSE_DATA_WE_AMPLIFY));
			mTool.add(new MenuItemManagingMirrorServers(this, ManagingMirrorServerConstants.SERVERS_WHO_AMPLIFY_OUR_DATA));
		}
		menuBar.add(mTool);

		JMenu mServices = new JMenu("Services");
		mServices.setMnemonic('s');
		mServices.add(new MenuItemServerCommands(this, STATUS_MARTUS_SERVER));
		mServices.addSeparator();
		mServices.add(new MenuItemServerCommands(this,START_MARTUS_SERVER));	
		mServices.add(new MenuItemServerCommands(this,STOP_MARTUS_SERVER));	
		mServices.add(new MenuItemServerCommands(this,RESTART_MARTUS_SERVER));	
		menuBar.add(mServices);
						
		JMenu mHelp = new JMenu("Help");
		mHelp.setMnemonic('h');
		mHelp.add(new MenuItemAboutHelp(this, "About MSPA"));
		menuBar.add(mHelp);
		
		return menuBar;
	}	
	
	
	private void initalizeUiState()
	{
		uiState = new CurrentUiState();
		File uiStateFile = mspaApp.getUiStateFile();

		if(!uiStateFile.exists())
		{
			uiState.setCurrentLanguage(localization.getCurrentLanguageCode());
			uiState.setCurrentDateFormat(localization.getCurrentDateFormatCode());
			uiState.save(uiStateFile);
			return;
		}
		uiState.load(uiStateFile);
		localization.setCurrentDateFormatCode(uiState.getCurrentDateFormat());
	}
	
	public static File getDefaultDirectoryPath()
	{
		String dataDirectory = null;
		if(Version.isRunningUnderWindows())
			dataDirectory = "C:/MSPAClient/";
		else
			dataDirectory = System.getProperty("user.home")+"/MSPAClient/";
		return new File(dataDirectory);
	}	
	
	public MSPAClient getMSPAApp()
	{
		return mspaApp;
	}
	
	public void exitNormally()
	{
		System.exit(0);
	}
	
	public static String STATUS_MARTUS_SERVER ="Query Service Status";
	public static String START_MARTUS_SERVER ="Start Services...";
	public static String STOP_MARTUS_SERVER  ="Stop Services..."; 
	public static String RESTART_MARTUS_SERVER ="Stop and Restart Services...";
	
	protected MSPAClient mspaApp;
	JFrame currentActiveFrame;	
	JTabbedPane tabPane;
	JTextField statusField;
	AccountsTree accountTree;
	MSPALocalization localization;
	CurrentUiState 	uiState;
	String serverName;
}
