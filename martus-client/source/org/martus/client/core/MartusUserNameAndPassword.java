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

import java.util.Arrays;

import org.martus.common.Exceptions.BlankUserNameException;
import org.martus.common.Exceptions.PasswordMatchedUserNameException;
import org.martus.common.Exceptions.PasswordTooShortException;

/**
 *
 * MartusUserNameAndPassword
 *
 * @author dchu
 *
 * Encapsulates the business logic behind validating usernames and passwords
 * Supports the UiCreateNewUserNameAndPassword UI dialog
 *
 */
public class MartusUserNameAndPassword
{
	public static final void validateUserNameAndPassword(String username, char[] password)
		throws
			BlankUserNameException,
			PasswordMatchedUserNameException,
			PasswordTooShortException
	{
		if (username.length() == 0)
			throw new BlankUserNameException();
		if (password.length < BASIC_PASSWORD_LENGTH)
			throw new PasswordTooShortException();
		if (Arrays.equals(password, username.toCharArray()))
			throw new PasswordMatchedUserNameException();
	}

	public static final boolean isWeakPassword(char[] password)
	{
		if ((password.length >= STRONG_PASSWORD_LENGTH)
			&& (containsEnoughNonAlphanumbericCharacters(password)))
			return false;
		return true;
	}

	private static final boolean containsEnoughNonAlphanumbericCharacters(char[] password)
	{
		int nonAlphanumericCounter = 0;
		int passwordLength = password.length;
		for (int i = 0; i < passwordLength; i++)
		{
			if (!(Character.isLetterOrDigit(password[i])))
			{
				nonAlphanumericCounter++;
				if (nonAlphanumericCounter
					>= STRONG_PASSWORD_NUMBER_OF_NONALPHANUMERIC)
					return true;
			}
		}
		return false;
	}

	// if the strong password criteria get changed
	// the old entry in MartusLocalization (RedoWeakPassword) must be deprecated
	// and a new entry created
	private static final int STRONG_PASSWORD_NUMBER_OF_NONALPHANUMERIC = 2;
	private static final int STRONG_PASSWORD_LENGTH = 15;
	public static final int BASIC_PASSWORD_LENGTH = 8;
}
