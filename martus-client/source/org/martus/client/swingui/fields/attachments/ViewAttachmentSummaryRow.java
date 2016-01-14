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

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.tablemodels.AttachmentTableModel;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.UniversalId;

class ViewAttachmentSummaryRow extends AbstractAttachmentRow
{
	public ViewAttachmentSummaryRow(UiMainWindow mainWindowToUse, AttachmentTableModel model, ViewAttachmentPanel panel)
	{
		super(Color.WHITE, mainWindowToUse.getLocalization());
		AttachmentProxy proxy = panel.getAttachmentProxy();
		
		store = mainWindowToUse.getStore();

		viewHidePanel.showCard(viewButton.getText());
		saveRemovePanel.showCard(saveButton.getText());
		if(isAttachmentAvailable(proxy))
		{
			viewButton.addActionListener(new ViewAttachmentHandler(mainWindowToUse, panel));
			hideButton.addActionListener(new HideAttachmentHandler(panel));
			saveButton.addActionListener(new SaveAttachmentHandler(mainWindowToUse, proxy));
		}
		else
		{
			viewButton.setEnabled(false);
			hideButton.setEnabled(false);
			saveButton.setEnabled(false);
		}

		String labelColumnText = proxy.getLabel();
		String sizeColumnText = AttachmentTableModel.getSize(proxy, mainWindowToUse.getStore().getDatabase());
		createCells(labelColumnText, sizeColumnText);
	}

	boolean isAttachmentAvailable(AttachmentProxy proxy)
	{
		UniversalId uid = proxy.getUniversalId();
		DatabaseKey key = DatabaseKey.createLegacyKey(uid);
		return store.doesBulletinRevisionExist(key);
	}
	
	ClientBulletinStore store;
}