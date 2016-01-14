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
package org.martus.clientside;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.border.EmptyBorder;

import org.martus.common.EnglishCommonStrings;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.Utilities;

public class UiBasicSigninDlg extends JDialog
{
	public UiBasicSigninDlg(UiLocalization localizationToUse, CurrentUiState uiStateToUse, int mode, String username, char[] password)
	{
		// NOTE: Pass (Dialog)null to force this window to show up in the Task Bar
		super((Dialog)null, true);
		initalize(localizationToUse, uiStateToUse, null, mode, username, password);
	}

	public UiBasicSigninDlg(UiLocalization localizationToUse, CurrentUiState uiStateToUse, JFrame owner, int mode, String username, char[] password)
	{
		super(owner, true);
		initalize(localizationToUse, uiStateToUse, owner, mode, username, password);
	}

	public void initalize(UiLocalization localizationToUse, CurrentUiState uiStateToUse, JFrame ownerToUse, int mode, String username, char[] password)
	{
		currentMode = mode;
		owner = ownerToUse;
		localization = localizationToUse;
		uiState = uiStateToUse;
		usersChoice = CANCEL;
		setTitle(getTextForTitle(currentMode));
		
		signinPane = new UiSigninPanel(this, currentMode, username, password);
		
		ok = new UiButton(localization.getButtonLabel(EnglishCommonStrings.OK));
		ok.addActionListener(new OkHandler());
		JButton cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(new CancelHandler());
		Box buttonBox = Box.createHorizontalBox();
		JComponent languageComponent = getLanguageComponent();
		Component[] buttons = {languageComponent, Box.createHorizontalGlue(), ok, cancel};
		Utilities.addComponentsRespectingOrientation(buttonBox, buttons);

		buttonBox.add(Box.createHorizontalGlue());
		
		buttonBox.setBorder(new EmptyBorder(5,5,5,5));
		Component scrolledPanel = createMainPanel();
	
		Container scrollingPane = new UiScrollPane(scrolledPanel);
		getContentPane().add(scrollingPane);
		getContentPane().add(buttonBox, BorderLayout.SOUTH);
		
		getRootPane().setDefaultButton(ok);
		signinPane.refreshForNewVirtualMode();
		setResizable(true);
		Dimension screenSize = Utilities.getViewableScreenSize();
		if(screenSize.width < 1000)
		{	
			setSize(screenSize.width, screenSize.height * 8 / 10);
			setLocation(Utilities.getViewableRectangle().x,screenSize.height/10);
		}
		else
		{	
			Utilities.packAndCenterWindow(this);
		}
		setVisible(true);
	}
	
	protected JComponent getLanguageComponent()
	{
		return new UiLabel();
	}

	protected Component createMainPanel()
	{
		return signinPane;
	}

	public String getTextForTitle(int mode)
	{		
		switch (mode)
		{
			case SECURITY_VALIDATE:
				return localization.getWindowTitle("MartusSignInValidate"); 
		
			case RETYPE_USERNAME_PASSWORD:
				return localization.getWindowTitle("MartusSignInRetypePassword"); 
			
			default:
				return getInitialSigninTitle(localization); 
		}			
	}

	public static String getInitialSigninTitle(UiLocalization localization)
	{
		return localization.getWindowTitle("MartusSignIn");
	}

	public int getUserChoice()
	{
		return usersChoice;
	}

	public String getNameText()
	{
		return signinPane.getNameText();
	}

	public char[] getPassword()
	{
		return signinPane.getPassword();
	}

	public void sizeHasChanged()
	{
		Utilities.packAndCenterWindow(this);
	}

	public void virtualPasswordHasChanged()
	{
		getRootPane().setDefaultButton(ok);
	}

	public void handleOk()
	{
		usersChoice = SIGN_IN;
		dispose();
	}

	public UiLocalization getLocalization()
	{
		return localization;
	}

	public CurrentUiState getCurrentUiState()
	{
		return uiState;
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			handleOk();
		}
	}
	
	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			usersChoice = CANCEL;
			dispose();
		}
	}
	

	public UiSigninPanel signinPane;
	public UiLocalization localization;
	public CurrentUiState uiState;
	public JFrame owner;
	public int usersChoice;
	boolean languageChanged;
	private JButton ok;
	protected int currentMode;
	
	// modes
	public static final int INITIAL = 1;
	public static final int TIMED_OUT = 2;
	public static final int SECURITY_VALIDATE = 3;
	public static final int RETYPE_USERNAME_PASSWORD = 4;
	public static final int CREATE_NEW = 5;
	public static final int INITIAL_NEW_RECOVER_ACCOUNT = 6;
	
	// results
	public static final int CANCEL = 10;
	public static final int SIGN_IN = 11;
	public static final int NEW_ACCOUNT = 12;
	public static final int RECOVER_ACCOUNT_BY_SHARE = 13;
	public static final int RECOVER_ACCOUNT_BY_BACKUP_FILE = 14;
	public static final int LANGUAGE_CHANGED = 15;

}
