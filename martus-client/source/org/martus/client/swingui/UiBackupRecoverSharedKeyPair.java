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

package org.martus.client.swingui;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MartusConstants;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.Version;
import org.martus.common.crypto.MartusCrypto.KeyShareException;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;

public class UiBackupRecoverSharedKeyPair 
{
	public UiBackupRecoverSharedKeyPair(UiMainWindow windowToUse)
	{
		super();
		mainWindow = windowToUse;
		localization = mainWindow.getLocalization();
	}

	public static Map getTokenReplacement() 
	{
		HashMap map = new HashMap();
		map.put("#TotalNumberOfFilesInBackup#", Integer.toString(MartusConstants.numberOfFilesInShare));
		map.put("#MinimumNumberOfFilesNeededForRecovery#", Integer.toString(MartusConstants.minNumberOfFilesNeededToRecreateSecret));
		return map;
	}

	public boolean recoverKeyPairFromMultipleUnencryptedFiles()
	{
		mainWindow.notifyDlg("RecoveryProcessKeyShare",UiBackupRecoverSharedKeyPair.getTokenReplacement());
		
		File firstShareFile = getRecoveryDriveToUse();
		if(firstShareFile == null)
			return false;
 
		Vector shares = recoverMinimumKeySharesNeededFromFiles(firstShareFile);
		if(shares == null)
			return false;
		
		try 
		{
			mainWindow.getApp().getSecurity().recoverFromKeyShareBundles(shares);
		} 
		catch (KeyShareException e) 
		{
			e.printStackTrace();
			if(mainWindow.confirmDlg("RecoveredKeyShareFailedTryAgain"))
				return recoverKeyPairFromMultipleUnencryptedFiles();
			return false;			
		}

		return keyPairRecoveredNewUserAndPasswordRequired();
	}

	public void backupKeyPairToMultipleUnencryptedFiles() 
	{
		String message = localization.getFieldLabel("BackupKeyPairToSecretShareInformation");
		mainWindow.displayScrollableMessage("confirmBackupKeyPairInformation", message, "Continue", getTokenReplacement());

		String defaultFileName = getDefaultKeyShareFileName();
		if(defaultFileName == null)
			return;
		
		Vector keyShareBundles = mainWindow.getApp().getSecurity().buildKeyShareBundles();
		if(keyShareBundles == null)
		{
			mainWindow.notifyDlg("ErrorBackingUpKeyShare");
			return;
		}

		String driveToUse = getBackupKeyShareDriveToUse();
		if(driveToUse == null)
			return;

		if(!writeAndVerifyKeySharesToDisks(defaultFileName, keyShareBundles, driveToUse))
			return;

		message = localization.getFieldLabel("BackupSecretShareCompleteInformation");
		mainWindow.displayScrollableMessage("BackupSecretShareCompleteInformation", message, "ok", getTokenReplacement());
		try
		{
			mainWindow.getApp().getConfigInfo().setBackedUpKeypairShare(true);
			mainWindow.getApp().getConfigInfo().setBackedUpImprovedKeypairShare(true);
			mainWindow.getApp().saveConfigInfo();
		}
		catch (SaveConfigInfoException e)
		{
			mainWindow.notifyDlg("ErrorSavingConfig");
			e.printStackTrace();
		}
	}

	private Vector recoverMinimumKeySharesNeededFromFiles(File firstShareFile) 
	{
		String defaultShareFileName = getRootKeyShareFileName(firstShareFile);
		if(defaultShareFileName == null)
			return null;

		int minNumber = MartusConstants.minNumberOfFilesNeededToRecreateSecret;
		Vector shares = new Vector();
		for(int disk = 1; disk <= minNumber; ++disk )
		{
			while(true)
			{
				String[] filesMatching = firstShareFile.getParentFile().list(new BackupShareFilenameFilter(defaultShareFileName, MartusApp.SHARE_KEYPAIR_FILENAME_EXTENSION));
				
				String noFilesFoundTag = "ErrorRecoverNoAppropriateFileFound";
				if(filesMatching == null || filesMatching.length == 0)
				{
					if(!insertDisk(noFilesFoundTag,noFilesFoundTag, disk, minNumber, "CancelShareRecover"))
						return null;
					continue;
				}
				File shareFile = new File(firstShareFile.getParent(), filesMatching[0]);
				if(shareFile == null || !shareFile.isFile())
				{
					if(!insertDisk(noFilesFoundTag, noFilesFoundTag, disk, minNumber, "CancelShareRecover"))
						return null;
					continue;
				}

				try 
				{
					UnicodeReader reader = new UnicodeReader(shareFile);
					shares.add(reader.readAll(6));
					reader.close();

					if(disk == minNumber)
						break;
					String recoverShareKeyPairTag = "RecoverShareKeyPair";
					if(!insertDisk(recoverShareKeyPairTag, recoverShareKeyPairTag, disk+1, minNumber, "CancelShareRecover"))
						return null;
					break;
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
					String errorRecoveryTag = "ErrorRecoverShareDisk";
					if(!insertDisk(errorRecoveryTag, errorRecoveryTag, disk, minNumber, "CancelShareRecover"))
						return null;
					continue;
				}
			}
		}
		return shares;
	}

	private String getRootKeyShareFileName(File file) 
	{
		String completeFileName = file.getName();
		int index = completeFileName.lastIndexOf("-");
		if(index == -1)
			return null;
		return completeFileName.substring(0,index);
	}

	private File getRecoveryDriveToUse() 
	{
		File firstShareFile = null;
		while(true)
		{
			firstShareFile = mainWindow.showFileOpenDialog("RecoverSharedKeyPair", (FileFilter)null);
			if(firstShareFile != null)
			{
				if(getRootKeyShareFileName(firstShareFile) != null)
					break;

				if(!mainWindow.confirmDlg("ErrorRecoverIvalidFileName"))
					return null;

				continue;
			}
			if(mainWindow.confirmDlg("CancelShareRecover"))
				return null;
		}
		return firstShareFile;
	}

	private boolean keyPairRecoveredNewUserAndPasswordRequired() 
	{
		mainWindow.notifyDlg("RecoveredKeyShareSucceededNewUserNamePasswordRequired");
		
		while(true)
		{
			MartusApp app = mainWindow.getApp();
			String accountId = app.getAccountId();
			File accountDirectory;
			try
			{
				accountDirectory = app.getAccountDirectory(accountId);
			}
			catch (InvalidBase64Exception e)
			{
				e.printStackTrace();
				mainWindow.notifyDlg("ErrorRecoveringAccountDirectory");
				return false;
			}
			File keyPairFile = app.getKeyPairFile(accountDirectory);
			if(keyPairFile.exists())
			{
				if(!mainWindow.confirmDlg("KeyPairFileExistsOverWrite"))
					return false;
			}
			
			if(mainWindow.getAndSaveUserNamePassword(keyPairFile))
			{					
				mainWindow.notifyDlg("RecoveryOfKeyShareComplete");
				mainWindow.askToBackupKeyPairEncryptedSingleFile();
				return true;
			}	
			if(mainWindow.confirmDlg("CancelShareRecover"))
				return false;
		}	
	}

	private String getDefaultKeyShareFileName() 
	{
		String defaultInputText = "";
		String enteredFileName = mainWindow.getStringInput("GetShareFileName","GetShareFileNameDescription","", defaultInputText);
		if(enteredFileName == null)
			return null;
		String defaultFileName = MartusUtilities.createValidFileName(enteredFileName);
		return defaultFileName;
	}

	private String getBackupKeyShareDriveToUse() 
	{
		while(true)
		{
			String windowTitle = localization.getWindowTitle("SaveShareKeyPair");
			File pathChosen = mainWindow.showChooseDirectoryDialog(windowTitle);
			if(pathChosen != null)
			{	
				String pathToUse = verifyBackupShareMediaType(pathChosen);
				if(pathToUse != null)
					return pathToUse;
			}
			if(mainWindow.confirmDlg("CancelShareBackup"))
				break;
		}	
		return null;
	}

	private String verifyBackupShareMediaType(File pathChoosen)
	{
		String pathToUse = pathChoosen.getPath();
		if(!Version.isRunningUnderWindows())
			return pathToUse;
		
		File[] rootFiles = File.listRoots();
		for(int i = 0 ; i < rootFiles.length; ++i)
		{
			if(rootFiles[i].equals(pathChoosen))
				return pathToUse;
		}
		if(mainWindow.confirmDlg("WarningPathChosenMayNotBeRemoveable", UiBackupRecoverSharedKeyPair.getTokenReplacement()))
			return pathToUse;
		return null;
	}

	private boolean writeAndVerifyKeySharesToDisks(String defaultFileName, Vector keyShareBundles, String driveToUse) 
	{
		Vector shareFiles = new Vector();
		int maxFiles = MartusConstants.numberOfFilesInShare;
		for(int disk = 1; disk <= maxFiles; ++disk )
		{
			while(true)
			{
				String fileName = defaultFileName + "-" + Integer.toString(disk) + MartusApp.SHARE_KEYPAIR_FILENAME_EXTENSION;
				File currentShareFile = new File(driveToUse, fileName);
				MartusLogger.log("Attempting backup to " + currentShareFile);
				String[] otherBackupFiles = currentShareFile.getParentFile().list(new BackupShareFilenameFilter(defaultFileName, MartusApp.SHARE_KEYPAIR_FILENAME_EXTENSION));
				if(otherBackupFiles != null && otherBackupFiles.length > 0)
				{
					String previousShareExistsTag = "ErrorPreviousBackupShareExists";
					if(!insertDisk(previousShareExistsTag, previousShareExistsTag, disk, maxFiles, "CancelShareBackup"))
						return false;
					continue;
				}
		
				if(!writeSharePieceToFile(currentShareFile, (String) keyShareBundles.get(disk - 1)))
				{
					String errorBackingupTag = "ErrorBackingupKeyPair";
					if(!insertDisk(errorBackingupTag, errorBackingupTag, disk, maxFiles, "CancelShareBackup"))
						return false;
					continue;
				}
				
				shareFiles.add(currentShareFile);
				if(disk == maxFiles)
					break;
					
				if(!insertDisk("SaveShareKeyPair",MtfAwareLocalization.UNUSED_TAG, disk+1, maxFiles, "CancelShareBackup"))
					return false;
				break;
			}
		}
			
		verifyKeyShareDisks(keyShareBundles, shareFiles, MartusConstants.numberOfFilesInShare);

		return true;
	}

	private void verifyKeyShareDisks(Vector keyShareBundles, Vector shareFiles,	int maxFiles) 
	{
		boolean verifiedAll = false;
		if(mainWindow.confirmDlg("BackupKeyShareVerifyDisks"))
		{
			for(int disk = 1; disk <= maxFiles; ++disk )
			{
				String verifyShareTag = "VerifyingKeyPairShare";
				if(!insertDisk(verifyShareTag, "", disk, maxFiles, "CancelShareVerify"))
					break;
				boolean exitVerification = false;
				while(true)
				{
					if(!verifySharePieceFromFile((File)shareFiles.get(disk-1), (String) keyShareBundles.get(disk - 1)))
					{
						String errorVerifyShareTag = "ErrorVerifyingKeyPairShare";
						if(!insertDisk(errorVerifyShareTag, errorVerifyShareTag, disk, maxFiles, "CancelShareVerify"))
						{
							exitVerification = true;
							break;
						}
						continue;
					}
					if(disk == maxFiles)
						verifiedAll = true;
					break;
				}
				if(exitVerification)
					break;
			}	
			if(verifiedAll)
				mainWindow.notifyDlg("VerifyKeyPairSharePassed");	
		}
	}

	private boolean verifySharePieceFromFile(File shareFile, String dataToCompare) 
	{
		try
		{
			UnicodeReader reader = new UnicodeReader(shareFile);
			String contents = reader.readAll(6);
			reader.close();
			if(contents.compareTo(dataToCompare)==0)
				return true;
		}
		catch (IOException ioe)
		{
			System.out.println(ioe.getMessage());
		}
		return false;
	}

	private boolean insertDisk(String titleMessageTag, String fieldMessageTag, int diskNumber, int totalNumberOfDisks, String confirmCancelTag)
	{
		String windowTitle = localization.getWindowTitle(titleMessageTag);
		String message1 = "";
		if(!fieldMessageTag.equals(MtfAwareLocalization.UNUSED_TAG))
			message1 = localization.getFieldLabel(fieldMessageTag);
		String message2 = localization.getFieldLabel("BackupRecoverKeyPairInsertNextDiskMessage") +
							" " + Integer.toString(diskNumber) + " " +	
		localization.getWindowTitle("SaveRecoverShareKeyPairOf") + " " +
		Integer.toString(totalNumberOfDisks);
		String insertNextDiskMessage[] = {message1, message2};

		String buttons[] = {localization.getButtonLabel(EnglishCommonStrings.OK), 
							localization.getButtonLabel(EnglishCommonStrings.CANCEL)};			

		if(!mainWindow.confirmDlg(windowTitle, insertNextDiskMessage, buttons))
		{
			if(mainWindow.confirmDlg(confirmCancelTag))
				return false;
		}
		return true;
	}
	
	private boolean writeSharePieceToFile(File newBackupFile, String dataToSave) 
	{
		try
		{
			UnicodeWriter output = new UnicodeWriter(newBackupFile);
			output.write(dataToSave);
			output.close();
			
			return verifySharePieceFromFile(newBackupFile, dataToSave);
		}
		catch (IOException ioe)
		{
			System.out.println(ioe.getMessage());
			return false;
		}
	}

	public class BackupShareFilenameFilter implements FilenameFilter
	{
		BackupShareFilenameFilter(String name, String extension)
		{
			defaultName = name;
			defaultExtension = extension;
		}
		
		public boolean accept(File dir, String name)
		{
			if(name.startsWith(defaultName) && name.endsWith(defaultExtension))
				return true;
			return false;
		}
		String defaultName;
		String defaultExtension;
	}

	UiMainWindow mainWindow;
	MartusLocalization localization;
}
