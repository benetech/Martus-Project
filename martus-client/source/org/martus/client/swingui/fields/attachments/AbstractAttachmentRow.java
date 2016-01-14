/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.client.swingui.fields.attachments;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.martus.common.MiniLocalization;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;

import com.jhlabs.awt.Alignment;
import com.jhlabs.awt.GridLayoutPlus;

abstract class AbstractAttachmentRow extends JPanel
{
	public AbstractAttachmentRow(Color background, MiniLocalization localizationToUse)
	{
		localization = localizationToUse;
		
		setBackground(background);
		GridLayoutPlus layout = new GridLayoutPlus(1, 0, 0, 0, 0, 0);
		layout.setFill(Alignment.FILL_VERTICAL);
		setLayout(layout);

		viewButton = new UiButton(localization.getButtonLabel("viewattachment"));
		hideButton = new UiButton(localization.getButtonLabel("hideattachment"));
		saveButton = new UiButton(localization.getButtonLabel("saveattachment"));
		removeButton = new UiButton(localization.getButtonLabel("removeattachment"));
		
		viewHidePanel = createMultiButtonPanel();
		viewHidePanel.add(viewButton, viewButton.getText());
		viewHidePanel.add(hideButton, hideButton.getText());
		
		saveRemovePanel = createMultiButtonPanel();
		saveRemovePanel.add(saveButton, saveButton.getText());
		saveRemovePanel.add(removeButton, removeButton.getText());
	}
	
	public MiniLocalization getLocalization()
	{
		return localization;
	}

	private MultiButtonPanel createMultiButtonPanel()
	{
		MultiButtonPanel panel = new MultiButtonPanel(getBackground());
		return panel;
	}
	
	public int getLabelColumnWidth()
	{
		return 400;
	}
	
	public int getSizeColumnWidth()
	{
		return 80;
	}

	void createCells(String labelColumnText, String sizeColumnText)
	{
		addCell(new UiLabel(labelColumnText), getLabelColumnWidth());
		addCell(new UiLabel(sizeColumnText), getSizeColumnWidth());
		addCell(viewHidePanel);
		addCell(saveRemovePanel);
	}
	
	JPanel addCell(JComponent contents, int preferredWidth)
	{
		JPanel cell = addCell(contents);
		cell.setPreferredSize(new Dimension(preferredWidth, 1));
		return cell;
	}
	
	JPanel addCell(JComponent contents)
	{
		Border outsideBorder = BorderFactory.createLineBorder(Color.BLACK);
		Border insideBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		JPanel cell = new JPanel();
		cell.setBackground(getBackground());
		cell.setForeground(getForeground());
		cell.setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));
		cell.add(contents);
		add(cell);
		return cell;
	}
	
	public void showViewButton()
	{
		viewHidePanel.showCard(viewButton.getText());
	}

	public void showHideButton()
	{
		viewHidePanel.showCard(hideButton.getText());
	}

	MiniLocalization localization;
	MultiButtonPanel viewHidePanel;
	MultiButtonPanel saveRemovePanel;
	UiButton viewButton;
	UiButton hideButton;
	UiButton saveButton;
	UiButton removeButton;
}