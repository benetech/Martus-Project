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
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.martus.common.MiniLocalization;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiPasswordField;
import org.martus.swing.Utilities;



public class UiVirtualKeyboard
{

	public UiVirtualKeyboard(MiniLocalization localization, VirtualKeyboardHandler uiHandler, UiPasswordField passwordFieldToUse)
	{
		handler = uiHandler;
		passwordField = passwordFieldToUse;
		String keys = localization.getFieldLabel("VirtualKeyboardKeys");
		space = localization.getFieldLabel("VirtualKeyboardSpace");
		delete = localization.getFieldLabel("VirtualKeyboardBackSpace");

		UpdateHandler updateHandler = new UpdateHandler();

		Container vKeyboard = new Container();
		int columns = 13;
		if(Utilities.isMacintosh())
			columns = 10;
		int rows = keys.length() / columns;
		vKeyboard.setLayout(new GridLayout(rows, columns));
		for(int i = 0; i < keys.length(); ++i)
		{
			String text = keys.substring(i,i+1);
			JButton key = new UiButton(text);
			key.setFocusPainted(false);
			key.addActionListener(updateHandler);
			key.addMouseListener(new MouseHandler(key));
			vKeyboard.add(key);
		}

		Container bottomRow = new Container();
		bottomRow.setLayout(new GridLayout(1,3));
		JButton spaceButton = new UiButton(space);
		spaceButton.addActionListener(updateHandler);
		JButton deleteButton = new UiButton(delete);
		deleteButton.addActionListener(updateHandler);
		bottomRow.add(spaceButton);
		bottomRow.add(new UiLabel(""));
		bottomRow.add(deleteButton);

		Container entireKeyboard = new Container();
		entireKeyboard.setLayout(new BorderLayout());
		entireKeyboard.add(vKeyboard, BorderLayout.NORTH);
		entireKeyboard.add(bottomRow, BorderLayout.SOUTH);

		JPanel virtualKeyboard = new JPanel();
		virtualKeyboard.add(entireKeyboard);
		virtualKeyboard.setBorder(new LineBorder(Color.black, 1));

		handler.addKeyboard(virtualKeyboard);
	}

	public class UpdateHandler extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			JButton buttonPressed = (JButton)(e.getSource());
			String passChar = buttonPressed.getText();
			if(passChar.equals(space))
				passwordField.appendChar(' ');
			else if(passChar.equals(delete))
			{
				passwordField.deleteLastChar();
			}
			else
				passwordField.appendChar(passChar.charAt(0));
			handler.virtualPasswordHasChanged();
			ensureLastButtonPressedIsNotHighlighted();
		}
		
		void ensureLastButtonPressedIsNotHighlighted()
		{
			passwordField.requestFocus();
		}

	}
	
	public class MouseHandler implements MouseListener
	{
		MouseHandler(JButton button)
		{
			owner = button;
		}

		public void mouseReleased(MouseEvent e)
		{
			Point mousePoint = e.getPoint();
			beepIfReleaseOutsidePressedButton(mousePoint);
		}

		private void beepIfReleaseOutsidePressedButton(Point mousePoint)
		{
			Rectangle ourArea = new Rectangle(owner.getSize());
			if(!ourArea.contains(mousePoint))
				Toolkit.getDefaultToolkit().beep();
		}
		
		public void mousePressed(MouseEvent e)	{}
		public void mouseClicked(MouseEvent e)	{}
		public void mouseEntered(MouseEvent e)	{}
		public void mouseExited(MouseEvent e)	{}
		
		JButton owner;
	}
	
	VirtualKeyboardHandler handler;
	String space;
	String delete;
	UiPasswordField passwordField;
}
