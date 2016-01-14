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
package org.martus.client.tools;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class TestBlankDialog
{
	public static void main(String[] args)
	{
		testLineWrap();
	}
	private static void testLineWrap()
	{
		testDialog(new String(text), ComponentOrientation.LEFT_TO_RIGHT, false);
		testDialog(new String(text), ComponentOrientation.LEFT_TO_RIGHT, true);
		testDialog(new String(text), ComponentOrientation.RIGHT_TO_LEFT, false);
		testDialog(new String(text), ComponentOrientation.RIGHT_TO_LEFT, true);
	}

	private static void testDialog(String title, ComponentOrientation orientation, boolean shouldLineWrap)
	{
		Font font = new Font("Khmer OS", Font.PLAIN, 30);
//		Font font = new Font("Arial", Font.PLAIN, 30);

		JTextArea widget = new JTextArea(1, 30);
		widget.setFont(font);
		widget.setComponentOrientation(orientation);
		widget.setLineWrap(shouldLineWrap);
		widget.setText(new String(text));

		JLabel label = new JLabel(new String(text));
		label.setFont(font);
		label.setComponentOrientation(orientation);
		
		JButton button = new JButton(new String(text));
		button.setFont(font);
		button.setComponentOrientation(orientation);
		
		JPanel panel = new JPanel();
		panel.setFont(font);
		panel.setLayout(new FlowLayout());
		panel.setComponentOrientation(orientation);
		panel.add(label);
		panel.add(widget);
		panel.add(button);
		
		JDialog dialog = new JDialog((Frame)null, title, true);
		dialog.setFont(font);
		dialog.setComponentOrientation(orientation);
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		dialog.pack();
		dialog.setResizable(true);
		dialog.setVisible(true);
	}	
	public static char[] text = {0x1781, 0x17D2, 0x1789, 0x17BB, 0x17C6, 0x200B, 
		0x179F, 0x17D2, 0x179A, 0x17B6, 0x200B, 
		0x179C, 0x17B7, 0x1791, 0x17BC};
//	public static char[] text = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
}
