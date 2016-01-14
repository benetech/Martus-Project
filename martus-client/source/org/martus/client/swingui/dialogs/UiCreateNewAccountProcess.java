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

package org.martus.client.swingui.dialogs;

import java.util.Arrays;

import org.martus.client.core.MartusUserNameAndPassword;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.Exceptions.BlankUserNameException;
import org.martus.common.Exceptions.PasswordMatchedUserNameException;
import org.martus.common.Exceptions.PasswordTooShortException;
import org.martus.swing.UiPasswordField;

/**
 * UiCreateNewAccountProcess
 *
 * Class encapusulates all aspects of creating a new username and password combo
 * - Displays the username and password entry dialog
 * - Checks the username and password to make sure they meet our requirements
 * - Confirms the username and password
 * - Reminds the user to remember his/her password
 *
 * @author dchu
 *
 */
public class UiCreateNewAccountProcess
{
	public UiCreateNewAccountProcess(
		UiMainWindow window,
		String originalUserName)
	{
		mainWindow = window;
		while (true)
		{
			UiSigninDlg signinDlg1 = getSigninResults(UiSigninDlg.CREATE_NEW, originalUserName);
			if (signinDlg1.getUserChoice() != UiSigninDlg.SIGN_IN)
				return;

			userName1 = signinDlg1.getNameText();
			userPassword1 = signinDlg1.getPassword();
			
			if(!userName1.equals(originalUserName))
			{	
				boolean userAlreadyExists = false;
				try
				{
					userAlreadyExists = window.getApp().doesAccountExist(userName1, userPassword1);
				}
				catch (Exception e)
				{
					userAlreadyExists = false;
				} 

				if(userAlreadyExists)
				{	
					window.notifyDlg("UserAlreadyExists");
					continue;
				}
			}
			
			// next make sure the username and password is valid
			try
			{
				MartusUserNameAndPassword.validateUserNameAndPassword(userName1, userPassword1);
			}
			catch (BlankUserNameException bune)
			{
				mainWindow.notifyDlg("UserNameBlank");
				continue;
			}
			catch (PasswordTooShortException ptse)
			{
				mainWindow.notifyDlg("PasswordInvalid");
				continue;
			}
			catch (PasswordMatchedUserNameException pmune)
			{
				mainWindow.notifyDlg("PasswordMatchesUserName");
				continue;
			}
			
			String defaultUserName = "";
			if (userName1.equals(originalUserName))
				defaultUserName = originalUserName;

			UiSigninDlg signinDlg2 = getSigninResults( UiSigninDlg.RETYPE_USERNAME_PASSWORD, defaultUserName);
			if (signinDlg2.getUserChoice() != UiSigninDlg.SIGN_IN)
				return;

			String userName2 = signinDlg2.getNameText();
			char[] userPassword2 = signinDlg2.getPassword();

			// make sure the passwords and usernames match
			if (!Arrays.equals(userPassword1, userPassword2))
			{
				window.notifyDlg("passwordsdontmatch");
				continue;
			}
			if (!userName1.equals(userName2))
			{
				window.notifyDlg("usernamessdontmatch");
				continue;
			}

			// finally warn them if its a weak password
			if(MartusUserNameAndPassword.isWeakPassword(userPassword1))
			{
				if(!window.confirmDlg("RedoWeakPassword"))
					continue;
			}

			remindUsersToRememberPassword();
			result = true;
			break;
		}
	}

	private UiSigninDlg getSigninResults(int mode, String userName)
	{
		UiSigninDlg signinDlg = null;
		int userChoice = UiSigninDlg.LANGUAGE_CHANGED;
		char[] userPassword = "".toCharArray();
		while(userChoice == UiSigninDlg.LANGUAGE_CHANGED)
		{	
			signinDlg = new UiSigninDlg(mainWindow.getLocalization(), mainWindow.getCurrentUiState(), mainWindow.getSwingFrame(), mode, userName, userPassword);
			userChoice = signinDlg.getUserChoice();
			userName = signinDlg.getNameText();
			userPassword = signinDlg.getPassword();
		}
		UiPasswordField.scrubData(userPassword);
		return signinDlg;
	}

	private void remindUsersToRememberPassword()
	{
		mainWindow.notifyDlg("RememberPassword");
	}

	public boolean isDataValid()
	{
		return result;
	}

	public String getUserName()
	{
		return userName1;
	}

	public char[] getPassword()
	{
		return userPassword1;
	}

	private String userName1;
	private char[] userPassword1;
	private UiMainWindow mainWindow;
	private boolean result;
}
