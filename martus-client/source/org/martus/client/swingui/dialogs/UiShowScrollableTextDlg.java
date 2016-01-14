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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.martus.client.swingui.UiMainWindow;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.clientside.UiLocalization;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;



public class UiShowScrollableTextDlg extends JDialog implements ActionListener
{
	public UiShowScrollableTextDlg(UiMainWindow owner, String titleTag, String okButtonTag, String cancelButtonTag, String descriptionTag, String text, JComponent bottomPanel)
	{
		this(owner, titleTag, okButtonTag, cancelButtonTag, descriptionTag, text, new HashMap(), bottomPanel);
	}
	
	public UiShowScrollableTextDlg(UiMainWindow owner, String titleTag, String okButtonTag, String cancelButtonTag, String descriptionTag, String text, Map tokenReplacement, JComponent bottomPanel)
	{
		super(owner.getSwingFrame(), "", true);
		setIconImage(Utilities.getMartusIconImage());
		mainWindow = owner;

		try 
		{
			UiLocalization localization = mainWindow.getLocalization();
			String windowTitle = localization.getWindowTitle(titleTag);
			setTitle(TokenReplacement.replaceTokens(windowTitle, tokenReplacement));
			String buttonLabel = localization.getButtonLabel(okButtonTag);
			ok = new UiButton(TokenReplacement.replaceTokens(buttonLabel, tokenReplacement));
			ok.addActionListener(this);
			JButton cancel = null;
			if(!cancelButtonTag.equals(MtfAwareLocalization.UNUSED_TAG))
			{
				buttonLabel = localization.getButtonLabel(cancelButtonTag);
				cancel = new UiButton(TokenReplacement.replaceTokens(buttonLabel, tokenReplacement));
				cancel.addActionListener(this);
			}
			
			details = new UiWrappedTextArea(TokenReplacement.replaceTokens(text, tokenReplacement), 85);
			details.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			Rectangle rect = details.getVisibleRect();
			details.setEditable(false);
			UiScrollPane detailScrollPane = new UiScrollPane(details, UiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					UiScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			detailScrollPane.setPreferredSize(new Dimension(rect.x, 400));		

			JPanel panel = new JPanel();
			panel.setBorder(new EmptyBorder(10,10,10,10));
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			
			if(!descriptionTag.equals(MtfAwareLocalization.UNUSED_TAG))
			{
				String fieldLabel = localization.getFieldLabel(descriptionTag);
				fieldLabel = TokenReplacement.replaceTokens(fieldLabel, tokenReplacement);
				panel.add(new UiLabel(" "));
				panel.add(new UiWrappedTextArea(fieldLabel));
			}
			panel.add(new UiLabel(" "));
			panel.add(detailScrollPane);
			panel.add(new UiLabel(" "));
			if(bottomPanel != null)
			{
				panel.add(bottomPanel);
				panel.add(new UiLabel(" "));
			}
			
			Box buttons = Box.createHorizontalBox();
			Dimension preferredSize = details.getPreferredSize();
			preferredSize.height = ok.getPreferredSize().height;
			buttons.setPreferredSize(preferredSize);
			if(cancelButtonTag.length() != 0)
				Utilities.addComponentsRespectingOrientation(buttons, new Component[]{ok,Box.createHorizontalGlue(),cancel});
			else
				buttons.add(ok);

			panel.add(buttons);
			getContentPane().add(panel);
			getRootPane().setDefaultButton(ok);
			Utilities.packAndCenterWindow(this);
			setVisible(true);
		} 
		catch (TokenInvalidException e) 
		{
			e.printStackTrace();
		}
	}


	public void actionPerformed(ActionEvent ae)
	{
		result = false;
		if(ae.getSource() == ok)
			result = true;
		dispose();
	}

	public boolean getResult()
	{
		return result;
	}

	
	JButton ok;
	UiWrappedTextArea details;
	boolean result;
	UiMainWindow mainWindow;
}
