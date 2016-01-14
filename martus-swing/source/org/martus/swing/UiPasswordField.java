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

To the extent this copyrighted software code is used in the 
Miradi project, it is subject to a royalty-free license to 
members of the Conservation Measures Partnership when 
used with the Miradi software as specified in the agreement 
between Benetech and WCS dated 5/1/05.
*/
package org.martus.swing;

import java.awt.Toolkit;
import java.util.Arrays;

import javax.swing.JPasswordField;


public class UiPasswordField extends JPasswordField
{
	public UiPasswordField(int columns)
	{
		super(columns);
		setComponentOrientation(UiLanguageDirection.getComponentOrientation());
		setHorizontalAlignment(UiLanguageDirection.getHorizontalAlignment());
		virtualPassword = new char[MAX_PASSWORD_LENGTH];
		clearVirtualPassword();
	}
	
	public void clearVirtualPassword()
	{
		scrubData(virtualPassword);
		virtualPasswordLength = 0;
	}

	public char[] getPassword()
	{
		if(inVirtualMode)
		{	
			char[] virtual = getVirtualPassword();
			return virtual;
		}
		return super.getPassword();
	}

	
	public void setVirtualMode(boolean virtualMode)
	{
		char[] currentPassword = getPassword();
		inVirtualMode = virtualMode;
		if(inVirtualMode)
		{
			super.setEditable(false);
			virtualPasswordLength = currentPassword.length;
			System.arraycopy(currentPassword, 0, virtualPassword, 0, virtualPasswordLength);
			scrubData(currentPassword);
			updateFakeVisualPassword();
			return;
		}
		
		super.setEditable(true);
		super.setText(new String(currentPassword));
	}
	
	private char[] getVirtualPassword()
	{
		char[] exactSizePassword = new char[virtualPasswordLength];
		System.arraycopy(virtualPassword,0,exactSizePassword,0,virtualPasswordLength);
		return exactSizePassword;
	}
	
	public void setPassword(char[] passwordToUse)
	{
		clearVirtualPassword();
		setVirtualMode(true);
		if(passwordToUse == null)
			return;
		for(int i = 0; i < passwordToUse.length; ++i)
			appendChar(passwordToUse[i]);
	}

	public void appendChar(char charToAppend)
	{
		if(!inVirtualMode || virtualPasswordLength+1 >= MAX_PASSWORD_LENGTH)
		{
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		virtualPassword[virtualPasswordLength] = charToAppend;
		++virtualPasswordLength;
		updateFakeVisualPassword();
	}

	public void deleteLastChar()
	{
		if(!inVirtualMode || virtualPasswordLength == 0)
		{
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		--virtualPasswordLength;
		updateFakeVisualPassword();
	}

	private void updateFakeVisualPassword()
	{
		String fakePasswordData = "";
		for(int i = 0; i<virtualPasswordLength; ++i)
			fakePasswordData += "*";
		super.setText(fakePasswordData);
	}

	static public void scrubData(char[] data)
	{
		final char scrubDataByte = 0x55;
		Arrays.fill(data, scrubDataByte);
	}
	
	final int MAX_PASSWORD_LENGTH = 1024;
	private boolean inVirtualMode;
	private char[] virtualPassword;
	private int virtualPasswordLength;
}
