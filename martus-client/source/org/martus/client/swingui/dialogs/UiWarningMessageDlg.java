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
package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.martus.swing.UiButton;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


public class UiWarningMessageDlg extends JDialog implements ActionListener
{
	public UiWarningMessageDlg(JFrame owner, String title, String okButtonLabel, String warningMessageLtoR, String warningMessageRtoL)
	{
		super(owner, title, true);

		JButton okButton = new UiButton(okButtonLabel);
		okButton.addActionListener(this);
		
		UiWrappedTextArea areaLtoR = new UiWrappedTextArea(warningMessageLtoR);
		areaLtoR.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		areaLtoR.setBorder(new EmptyBorder(5,5,5,5));
		JPanel ltorPanel = new JPanel();
		ltorPanel.add(areaLtoR);
		ltorPanel.setBorder(new LineBorder(Color.BLACK));
		
		UiWrappedTextArea areaRtoL = new UiWrappedTextArea(warningMessageRtoL);
		areaRtoL.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		areaRtoL.setBorder(new EmptyBorder(5,5,5,5));
		JPanel rtolPanel = new JPanel();
		rtolPanel.add(areaRtoL);
		rtolPanel.setBorder(new LineBorder(Color.BLACK));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(5,5,5,5));
		panel.add(ltorPanel,BorderLayout.NORTH);
		panel.add(rtolPanel,BorderLayout.SOUTH);
		getContentPane().add(new UiScrollPane(panel), BorderLayout.CENTER);
		
		JPanel pb = new JPanel();
		pb.add(okButton);
		getContentPane().add(pb, BorderLayout.SOUTH);

		Utilities.packAndCenterWindow(this);
		setResizable(true);
		getRootPane().setDefaultButton(okButton);
		okButton.requestFocus(true);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		dispose();
	}


}
