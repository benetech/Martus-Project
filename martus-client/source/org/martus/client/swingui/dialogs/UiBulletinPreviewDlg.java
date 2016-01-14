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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JViewport;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponentViewSection;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.FieldSpecCollection;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.FieldDataPacket;
import org.martus.swing.UiButton;
import org.martus.swing.UiScrollPane;
import org.martus.swing.Utilities;

public class UiBulletinPreviewDlg extends JDialog implements ActionListener
{

	public UiBulletinPreviewDlg(UiMainWindow owner, MartusLocalization localizationToUse, String windowTitleTag)
	{
		super(owner.getSwingFrame(), localizationToUse.getWindowTitle(windowTitleTag), true);	
		getContentPane().setLayout(new BorderLayout());
		localization = localizationToUse;
	}

	public UiBulletinPreviewDlg(UiMainWindow owner, FieldDataPacket fdp)
	{
		this(owner, owner.getLocalization(), "BulletinPreview");

		UiBulletinComponentViewSection view = new UiBulletinComponentViewSection(owner, Bulletin.TOP_SECTION);
		FieldSpecCollection standardFieldTags = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		
		view.createLabelsAndFields(standardFieldTags, null);
		view.copyDataFromPacket(fdp);
		view.updateEncryptedIndicator(fdp.isEncrypted());	
		
		initalizeView(view);
	}


	protected void initalizeView(JComponent view)
	{
		UiScrollPane scrollPane = new UiScrollPane();
		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		scrollPane.getViewport().add(view);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		JButton ok = new UiButton(localization.getButtonLabel(EnglishCommonStrings.OK));
		ok.addActionListener(this);
		Dimension okSize = ok.getPreferredSize();
		okSize.width += 40;
		ok.setPreferredSize(okSize);
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(ok);
		buttonPane.add(Box.createHorizontalGlue());
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(ok);
		Utilities.packAndCenterWindow(this);
		setResizable(true);
		ok.requestFocus(true);
		Utilities.forceScrollerToTop(view);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		dispose();
	}

	private MartusLocalization localization;
}
