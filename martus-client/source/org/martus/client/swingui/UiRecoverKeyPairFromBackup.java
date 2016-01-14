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
import java.io.FileInputStream;
import java.io.IOException;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.dialogs.UiSigninDlg;
import org.martus.clientside.PasswordHelper;
import org.martus.swing.UiPasswordField;
import org.martus.util.StreamableBase64.InvalidBase64Exception;


public class UiRecoverKeyPairFromBackup
{
	public UiRecoverKeyPairFromBackup(UiMainWindow windowToUse)
	{
		super();
		mainWindow = windowToUse;
		app = mainWindow.getApp();
	}

	public boolean recoverPrivateKey()
	{
		mainWindow.notifyDlg("RecoveryProcessBackupFile");
		while(true)
		{
			File backupFile = mainWindow.showFileOpenDialogWithDirectoryMemory("RestoreFromKeyPair");
			if (backupFile == null)
				return false;
			
			try
			{
				attemptSignIn(backupFile);
				return saveKeyPairToAccount();
			}
			catch (AbortedSignInException e)
			{
				if(mainWindow.confirmDlg("CancelBackupRecovery"))
					return false;
			}
			catch (Exception e)
			{
				if(!mainWindow.confirmDlg("UnableToRecoverFromBackupFile"))
					return false;
			}
			finally
			{
				UiPasswordField.scrubData(userPassword);
				userPassword = null;
			}
		}
	}
	

	class AttemptedSignInFailedException extends Exception 
	{
	}
	
	class AbortedSignInException extends Exception 
	{
	}
	
	private void attemptSignIn(File backupFile) throws AttemptedSignInFailedException, AbortedSignInException, IOException
	{
		if(backupFile == null || !backupFile.isFile())
			throw new AttemptedSignInFailedException();
		
		UiSigninDlg signinDlg = null;
		int userChoice = UiSigninDlg.LANGUAGE_CHANGED;
		while(userChoice == UiSigninDlg.LANGUAGE_CHANGED)
		{	
			signinDlg = new UiSigninDlg(getLocalization(), mainWindow.getCurrentUiState(), UiSigninDlg.SECURITY_VALIDATE, userName, userPassword);
			userChoice = signinDlg.getUserChoice();
			userName = signinDlg.getNameText();
			userPassword = signinDlg.getPassword();
		}
		if(userChoice != UiSigninDlg.SIGN_IN)
			throw new AbortedSignInException();
		
		FileInputStream inputStream = new FileInputStream(backupFile);
		try
		{
			app.getSecurity().readKeyPair(inputStream, PasswordHelper.getCombinedPassPhrase(userName, userPassword));
		}
		catch (Exception e)
		{
			throw new AttemptedSignInFailedException();
		}
		finally
		{
			inputStream.close();
		}
	}
	
	private boolean saveKeyPairToAccount()
	{
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
		
		File accountsHashOfUserNameFile = app.getUserNameHashFile(keyPairFile.getParentFile());
		accountsHashOfUserNameFile.delete();
		if(!mainWindow.saveKeyPairFile(keyPairFile,userName, userPassword))
			return false;

		mainWindow.notifyDlg("RecoveryOfKeyPairComplete");
		return true;
		
	}
	
	public MartusLocalization getLocalization()
	{
		return mainWindow.getLocalization();
	}
	
	private MartusApp app;
	private UiMainWindow mainWindow;
	
	private String userName;
	private char[] userPassword;
}
