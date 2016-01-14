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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;


public class UiNotifyDlg extends JDialog implements ActionListener
{

	public UiNotifyDlg(Frame owner, String title, String[] contents, String[] buttons)
	{
		this(owner, title, contents, buttons, new HashMap());
	}
	
	public UiNotifyDlg(Frame owner, String title, String[] contents, String[] buttons, Map tokenReplacement)
	{
		super(owner, title , true);
		initialize(title, contents, buttons, tokenReplacement);
	}

	public UiNotifyDlg(String title, String[] contents, String[] buttons)
	{
		this(title, contents, buttons, new HashMap());
	}
	
	public UiNotifyDlg(String title, String[] contents, String[] buttons, Map tokenReplacement)
	{
		// NOTE: Pass (Dialog)null to force this window to show up in the Task Bar
		super((Dialog)null, title , true);
		setIconImage(Utilities.getMartusIconImage());
		initialize(title, contents, buttons, tokenReplacement);
	}

	public void initialize(String title, String[] contents, String[] buttons,
			Map tokenReplacement) {
		setComponentOrientation(UiLanguageDirection.getComponentOrientation());
		try
		{
			title = TokenReplacement.replaceTokens(title, tokenReplacement);
			contents = TokenReplacement.replaceTokens(contents, tokenReplacement);
			buttons = TokenReplacement.replaceTokens(buttons, tokenReplacement);
			
			setTitle(title);
			
			StringBuffer wrappedContents = new StringBuffer();
			for(int i = 0 ; i < contents.length ; ++i)
			{
				wrappedContents.append(contents[i]);
				wrappedContents.append("\n");
			}
			
			ok = new UiButton(buttons[0]);
			ok.addActionListener(this);

			int numberOfButtons = buttons.length;
			Component[] allButtonsWithCentering = new Component[numberOfButtons+2];
			allButtonsWithCentering[0] = Box.createHorizontalGlue();
			allButtonsWithCentering[1] = ok;
			for(int j = 1 ; j < numberOfButtons; ++j)
			{
				JButton button = new UiButton(buttons[j]);
				button.addActionListener(this);
				allButtonsWithCentering[j+1] = button;
			}
			allButtonsWithCentering[numberOfButtons+1] = Box.createHorizontalGlue();
			Box hbox = Box.createHorizontalBox();
			Utilities.addComponentsRespectingOrientation(hbox, allButtonsWithCentering);
			
		
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.setBorder(new EmptyBorder(5,5,5,5));
			panel.add(createWrappedTextArea(wrappedContents.toString()), BorderLayout.CENTER);
			panel.add(hbox, BorderLayout.SOUTH);
			getContentPane().add(new UiScrollPane(panel), BorderLayout.CENTER);
			
			pack();
			
			Utilities.packAndCenterWindow(this);
			setResizable(true);
			getRootPane().setDefaultButton(ok);
			ok.requestFocus(true);
			setVisible(true);
		}
		catch (TokenInvalidException e)
		{
			e.printStackTrace();
		}
	}

	private JComponent createWrappedTextArea(String message)
	{
		UiWrappedTextArea msgArea = new UiWrappedTextArea(message);
		msgArea.addKeyListener(new TabToOkButton());
		return msgArea;
	}

	public class TabToOkButton extends KeyAdapter
	{
		public void keyPressed(KeyEvent ke)
		{
			if (ke.getKeyCode() == KeyEvent.VK_TAB)
			{
				ok.requestFocus();
			}
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		exit(ae);
	}

	public void exit(ActionEvent ae)
	{
		result = ae.getActionCommand();
		dispose();
	}

	public String getResult()
	{
		return result;
	}

	String result;
	JButton ok;
}
