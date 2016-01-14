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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiLanguageDirection;
import org.martus.swing.UiPasswordField;
import org.martus.swing.UiTextField;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.language.LanguageOptions;

import com.jhlabs.awt.Alignment;
import com.jhlabs.awt.GridLayoutPlus;

public class UiSigninPanel extends JPanel implements VirtualKeyboardHandler
{
	public UiSigninPanel(UiBasicSigninDlg dialogToUse, int mode, String username, char[] password)
	{
		GridLayoutPlus layout = new GridLayoutPlus(0, 2, 5, 5, 5, 5);
		
		// NOTE: Cheap hack: In English, this will cause the prompts in the first column
		// to float over toward the associated field. In Arabic, it will cause the fields 
		// to float over toward the prompts. The two won't be aligned the same way, but 
		// should be "ok".
		layout.setColAlignment(0, Alignment.EAST);
		layout.setFill(Alignment.FILL_NONE);
		setLayout(layout);
		owner = dialogToUse;
		localization = owner.getLocalization();
		uiState = owner.getCurrentUiState();

		if(mode == UiBasicSigninDlg.TIMED_OUT)
		{
			addOnNewLine(new UiLabel(localization.getFieldLabel("timedout1")));
			if(owner.getCurrentUiState().isModifyingBulletin())
				addOnNewLine(new UiLabel(localization.getFieldLabel("timedout2")));
		}
		else if(mode == UiBasicSigninDlg.SECURITY_VALIDATE)
		{
			addOnNewLine(new UiLabel(localization.getFieldLabel("securityServerConfigValidate")));
		}
		else if(mode == UiBasicSigninDlg.RETYPE_USERNAME_PASSWORD)
		{
			addOnNewLine(new UiLabel(localization.getFieldLabel("RetypeUserNameAndPassword")));
		}
		else if(mode == UiBasicSigninDlg.CREATE_NEW)
		{
			addOnNewLine(new UiLabel(localization.getFieldLabel("CreateNewUserNamePassword")));
			addOnNewLine(new UiWrappedTextArea(localization.getFieldLabel("HelpOnCreatingNewPassword")));
			uiState.setCurrentDefaultKeyboardVirtual(true);
			uiState.save();
		}
		
		userNameDescription = new UiLabel("");
		passwordDescription = new UiLabel("");

		nameField = new UiTextField(20);
		nameField.setText(username);
		UiLabel userNameLabel = new UiLabel(localization.getFieldLabel("username"));
		addComponents(userNameLabel, createPanel(userNameDescription, nameField));

		passwordField = new UiPasswordField(20);
		passwordField.setPassword(password);
		switchToNormalKeyboard = new UiButton(localization.getButtonLabel("VirtualKeyboardSwitchToNormal"));
		switchToNormalKeyboard.addActionListener(new SwitchKeyboardHandler());
		UiLabel passwordLabel = new UiLabel(localization.getFieldLabel("password"));
		GridLayoutPlus passwordLayout = new GridLayoutPlus(0, 1, 15, 10, 15, 10);
		passwordLayout.setFill(Alignment.FILL_NONE);
		if(LanguageOptions.isRightToLeftLanguage())
			passwordLayout.setAlignment(Alignment.EAST);
		passwordArea = new JPanel(passwordLayout);
		addComponents(passwordLabel, passwordArea);

		new UiVirtualKeyboard(localization, this, passwordField);
		UpdatePasswordArea();
		
		if(username != null && username.length() > 0)
			passwordField.requestFocus();
	}
	
	private void addOnNewLine(Component component)
	{
		addComponents(new UiLabel(" "), component);
	}
	
	private void addComponents(Component left, Component right)
	{
		if(LanguageOptions.isRightToLeftLanguage())
		{
			add(right);
			add(left);
		}
		else
		{
			add(left);
			add(right);
		}
	}
	
	public String getNameText()
	{
		return nameField.getText();
	}
	
	public char[] getPassword()
	{
		return passwordField.getPassword();
	}
	
	public void refreshForNewVirtualMode()
	{
		passwordArea.updateUI();
		userNameDescription.updateUI();
		nameField.requestFocus();
		owner.virtualPasswordHasChanged();
	}

	public void UpdatePasswordArea()
	{
		boolean viewingVirtualKeyboard = uiState.isCurrentDefaultKeyboardVirtual();
		if(viewingVirtualKeyboard)
			displayPasswordAreaUsingVirtualKeyboard();
		else
			displayPasswordAreaUsingNormalKeyboard();
	}

	public void addKeyboard(JPanel keyboard)
	{
		virtualKeyboardPanel = keyboard;
	}

	public void displayPasswordAreaUsingVirtualKeyboard()
	{
		passwordArea.removeAll();
		userNameDescription.setText(localization.getFieldLabel("VirtualUserNameDescription"));
		passwordDescription.setText(localization.getFieldLabel("VirtualPasswordDescription"));
		passwordField.setVirtualMode(true);
		passwordArea.setBorder(new LineBorder(Color.BLACK, 2));
		switchToNormalKeyboard.setText(localization.getButtonLabel("VirtualKeyboardSwitchToNormal"));

		passwordArea.add(createPanel(passwordDescription, passwordField));
		passwordArea.add(virtualKeyboardPanel);
		passwordArea.add(switchToNormalKeyboard);

		refreshForNewVirtualMode();
		owner.sizeHasChanged();
	}

	public void displayPasswordAreaUsingNormalKeyboard()
	{
		passwordArea.removeAll();
		passwordArea.updateUI();
		userNameDescription.setText("");
		passwordDescription.setText("");
		passwordArea.setBorder(new LineBorder(Color.black, 2));

		passwordField.setVirtualMode(false);
		passwordArea.add(passwordField);

		JLabel warningNormalKeyboard = new UiLabel(localization.getFieldLabel("NormalKeyboardMsg1"));
		warningNormalKeyboard.setFont(warningNormalKeyboard.getFont().deriveFont(Font.BOLD));
		warningNormalKeyboard.setAlignmentX(UiLanguageDirection.getAlignmentX());
		passwordArea.add(warningNormalKeyboard);
		UiLabel switchToNormalMessage = new UiLabel(localization.getFieldLabel("NormalKeyboardMsg2"));
		switchToNormalMessage.setAlignmentX(UiLanguageDirection.getAlignmentX());
		passwordArea.add(switchToNormalMessage);

		switchToNormalKeyboard.setText(localization.getButtonLabel("VirtualKeyboardSwitchToVirtual"));
		switchToNormalKeyboard.setAlignmentX(UiLanguageDirection.getAlignmentX());
		passwordArea.add(switchToNormalKeyboard);
		
		refreshForNewVirtualMode();
		owner.sizeHasChanged();
	}

	public void switchKeyboards()
	{
		boolean viewingVirtualKeyboard = uiState.isCurrentDefaultKeyboardVirtual();
		if(viewingVirtualKeyboard)
		{				
			if(!UiUtilities.confirmDlg(localization, (Frame)owner.getParent(), "WarningSwitchToNormalKeyboard"))
				return;
		}

		uiState.setCurrentDefaultKeyboardVirtual(!viewingVirtualKeyboard);
		uiState.save();
		UpdatePasswordArea();
	}

	public class SwitchKeyboardHandler extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			switchKeyboards();
		}

	}
	
	public void virtualPasswordHasChanged()
	{
		passwordField.updateUI();
		owner.virtualPasswordHasChanged();
	}

	private JPanel createPanel(Component component1, Component component2)
	{
		JPanel panel = new JPanel();
		Box hBox = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(hBox, new Component[] {Box.createHorizontalGlue(), component1, component2});
		panel.add(hBox);
		return panel;
	}
	

	UiBasicSigninDlg owner;
	UiLocalization localization;
	CurrentUiState uiState;
	private JLabel userNameDescription;
	private JLabel passwordDescription;
	private UiTextField nameField;
	private UiPasswordField passwordField;
	private JPanel passwordArea;
	private JPanel virtualKeyboardPanel;
	private JButton switchToNormalKeyboard;
}
