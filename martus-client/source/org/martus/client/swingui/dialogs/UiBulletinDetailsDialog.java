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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.ExtendedHistoryEntry;
import org.martus.common.packet.ExtendedHistoryList;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.BurmeseUtilities;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.TokenReplacement;


public class UiBulletinDetailsDialog extends JDialog
{
	public UiBulletinDetailsDialog(UiMainWindow mainWindowToUse, Bulletin bulletinToShow, String tagQualifierToUse) throws Exception
	{
		super(mainWindowToUse.getCurrentActiveFrame().getSwingFrame(), true);
		
		mainWindow = mainWindowToUse;
		bulletin = bulletinToShow;
		tagQualifier = tagQualifierToUse;
		
		setTitle(getLocalization().getWindowTitle("BulletinDetailsDialog"));
		UiParagraphPanel panel = new UiParagraphPanel();
		panel.addComponents(new UiLabel(getLabel("AuthorPublicCode")), createField(getPublicCode()));
		panel.addComponents(new UiLabel(getLabel("BulletinId")),createField(bulletin.getLocalId()));

		UiScrollPane historyScroller = createHistoryTable();
		panel.addComponents(new UiLabel(getLabel("History")), historyScroller);
		
		if(bulletin.getBulletinHeaderPacket().getExtendedHistory().size() > 0)
			panel.addComponents(new UiLabel(getLabel("ExtendedHistory")), createExtendedHistoryComponent());
		
		JButton closeButton = new UiButton(getLocalization().getButtonLabel("close"));
		closeButton.addActionListener(new CloseHandler());
		getRootPane().setDefaultButton(closeButton);
		closeButton.requestFocus(true);
		previewVersionButton = new UiButton(getLocalization().getButtonLabel("ViewPreviousBulletinVersion"));
		previewVersionButton.addActionListener(new previewListener());
		if(versionTable.getRowCount() < 2)
			previewVersionButton.setEnabled(false);
		panel.addComponents(closeButton, previewVersionButton);
		getContentPane().add(new UiScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		Utilities.packAndCenterWindow(this);
		setResizable(true);
		
	}
	
	private UiLabel createExtendedHistoryComponent() throws Exception
	{
		StringBuffer extendedHistoryText = new StringBuffer();
		extendedHistoryText.append("<html>");
		ExtendedHistoryList extendedHistory = bulletin.getBulletinHeaderPacket().getExtendedHistory();
		for(int clone = 0; clone < extendedHistory.size(); ++clone)
		{
			ExtendedHistoryEntry entry = extendedHistory.getHistory(clone);
			String publicCode = MartusCrypto.computeFormattedPublicCode40(entry.getClonedFromAccountId());
			String label = getLocalization().getFieldLabel("PreviousAuthor");

			HashMap tokenReplacement = new HashMap();
			tokenReplacement.put("#AUTHOR#",publicCode);
			label = TokenReplacement.replaceTokens(label, tokenReplacement);
			extendedHistoryText.append(label);

			BulletinHistory localHistory = entry.getClonedHistory();
			extendedHistoryText.append("<br>");
			for(int revision = 0; revision < localHistory.size(); ++revision)
			{
				extendedHistoryText.append("&nbsp;&nbsp;&nbsp;");
				String idTemplate = getLocalization().getFieldLabel("PreviousBulletinId");
				HashMap id = new HashMap();
				id.put("#ID#", localHistory.get(revision));
				String idRow = TokenReplacement.replaceTokens(idTemplate, id);
				extendedHistoryText.append(idRow);
				extendedHistoryText.append("<br>");
			}
			extendedHistoryText.append("</ul>");
		}
		return new UiLabel(extendedHistoryText.toString());
	}

	public void hidePreviewButton()
	{
		previewVersionButton.setVisible(false);
	}
	
	class previewListener implements ActionListener
	{

		public void actionPerformed(ActionEvent e)
		{
			int selectedRow = versionTable.getSelectedRow();
			if(selectedRow < 0)
				return;
			if(selectedRow == versionTable.getRowCount()-1)
			{
				mainWindow.notifyDlg("AlreadyViewingThisVersion");
				return;
			}
			String localId = (String)versionTable.getValueAt(selectedRow, 1);
			UniversalId uid = UniversalId.createFromAccountAndLocalId(bulletin.getAccount(), localId);
			Bulletin previousBulletinVersion = mainWindow.getStore().getBulletinRevision(uid);
			if(previousBulletinVersion == null)
			{
				mainWindow.notifyDlg("BulletinVersionNotInSystem");
				return;
			}
			new UiBulletinVersionPreviewDlg(mainWindow, previousBulletinVersion);
		}
		
	}

	private UiScrollPane createHistoryTable()
	{
		BulletinHistory history = bulletin.getHistory();
		DefaultTableModel versionModel = new DetailsTableModel(); 
		versionModel.addColumn(getLabel("VersionNumber"));
		versionModel.addColumn(getLabel("VersionId"));
		versionModel.addColumn(getLabel("VersionDate"));
		versionModel.addColumn(getLabel("VersionTitle"));
		versionModel.setRowCount(history.size() + 1);

		for(int i=0; i < history.size(); ++i)
		{
			String localId = history.get(i);
			UniversalId uid = UniversalId.createFromAccountAndLocalId(bulletin.getAccount(), localId);
			populateVersionRow(versionModel, i, uid);
		}
		populateVersionRow(versionModel, history.size(), bulletin.getUniversalId());
		versionTable = new UiTable(versionModel);
		versionTable.setMaxColumnWidthToHeaderWidth(0);
		versionTable.setColumnWidthToHeaderWidth(1);
		versionTable.setColumnWidth(2, DATE_COLUMN_WIDTH);
		versionTable.setColumnWidth(3, TITLE_COLUMN_WIDTH);
		versionTable.setAutoResizeMode(UiTable.AUTO_RESIZE_LAST_COLUMN);
		versionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		versionTable.setColumnSelectionAllowed(false);
		versionTable.setShowGrid(true);
		versionTable.resizeTable();

		UiScrollPane versionScroller = new UiScrollPane(versionTable);
		return versionScroller;
	}
	
	class DetailsTableModel extends DefaultTableModel
	{
		
		public boolean isCellEditable(int row, int column)
		{
			return false;
		}
		

	}
	
	
	private void populateVersionRow(DefaultTableModel versionModel, int i, UniversalId uid)
	{
		int column = 0;
		versionModel.setValueAt(new Integer(1+i), i, column++);
		versionModel.setValueAt(uid.getLocalId(), i, column++);
		UniversalId bulletinUid = bulletin.getUniversalId();
		versionModel.setValueAt(getSavedDateToDisplay(uid, bulletinUid, mainWindow), i, column++);
		versionModel.setValueAt(getTitleToDisplay(uid, bulletinUid, mainWindow), i, column++);
	}
	
	static public String getSavedDateToDisplay(UniversalId versionUid, UniversalId bulletinUid, UiMainWindow mainWindow)
	{
		Bulletin b = mainWindow.getStore().getBulletinRevision(versionUid);
		MartusLocalization localization = mainWindow.getLocalization();
		if(b != null)
			return localization.formatDateTime(b.getLastSavedTime());
		
		if(versionUid.equals(bulletinUid))
			return getLabel(localization, "InProgressDate");
		
		return getLabel(localization, "UnknownDate");
	}

	static public String getTitleToDisplay(UniversalId versionUid, UniversalId bulletinUid, UiMainWindow mainWindow)
	{
		Bulletin b = mainWindow.getStore().getBulletinRevision(versionUid);
		if(b != null)
		{
			final String title = b.get(Bulletin.TAGTITLE);
			return BurmeseUtilities.getDisplayable(title);
		}
		
		MartusLocalization localization = mainWindow.getLocalization();
		if(versionUid.equals(bulletinUid))
			return getLabel(localization, "InProgressTitle");
		
		return getLabel(localization, "UnknownTitle");
	}

	private JComponent createField(String text)
	{
		UiWrappedTextArea component = new UiWrappedTextArea(text);
		component.setEditable(false);
		return component;
	}
	
	private MartusLocalization getLocalization()
	{
		return mainWindow.getLocalization();
	}

	private String getPublicCode()
	{
		try
		{
			return MartusCrypto.computeFormattedPublicCode40(bulletin.getAccount());
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return "";
		}		
	}

	private String getLabel(String tag)
	{
		return getLabel(getLocalization(), tag);
	}

	static private String getLabel(MartusLocalization localization, String tag)
	{
		return localization.getFieldLabel("BulletinDetails" + tag);
	}
	
	class CloseHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			UiBulletinDetailsDialog.this.dispose();
		}
	}

	private static final int TITLE_COLUMN_WIDTH = 300;
	private static final int DATE_COLUMN_WIDTH = 125;
	
	UiMainWindow mainWindow;
	Bulletin bulletin;
	String tagQualifier;
	private JButton previewVersionButton;
	UiTable versionTable;

}
