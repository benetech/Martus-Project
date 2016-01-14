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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

import org.martus.client.bulletinstore.ExportBulletins;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiBulletinTitleListComponent;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiSession;
import org.martus.client.swingui.filefilters.XmlFileFilter;
import org.martus.clientside.FormatFilter;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.UiButton;
import org.martus.swing.UiCheckBox;
import org.martus.swing.UiList;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiVBox;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;

public class UiExportBulletinsDlg extends JDialog implements ActionListener
{
	public UiExportBulletinsDlg(UiMainWindow mainWindowToUse, Vector bulletinsToExport, String defaultName)
	{
		super(mainWindowToUse.getSwingFrame(), "", true);
		mainWindow = mainWindowToUse;
		bulletins = bulletinsToExport;
		defaultFileName = defaultName;
		if(defaultFileName == null)
			throw new RuntimeException("Must pass non-null defaultFileName to export dialog");
		if(!defaultFileName.endsWith(MartusApp.XML_EXTENSION))
			defaultFileName += MartusApp.XML_EXTENSION;

		constructDialog();
	}

	private void constructDialog()
	{
		UiLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle("ExportBulletins"));
		
		includeAttachments = new UiCheckBox(localization.getFieldLabel("ExportAttachments"));
		includeAllVersions = new UiCheckBox(localization.getFieldLabel("ExportAllVersions"));
		ok = new UiButton(localization.getButtonLabel("Continue"));
		ok.addActionListener(this);
		
		cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(this);
		
		
		UiList bulletinList = new UiBulletinTitleListComponent(mainWindow, bulletins);
		UiScrollPane tocMsgAreaScrollPane = new UiScrollPane(bulletinList,
				UiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				UiScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tocMsgAreaScrollPane.setPreferredSize(new Dimension(580, 100));
		
		UiVBox upperStuff = new UiVBox();
		upperStuff.addSpace();
		upperStuff.addCentered(new UiWrappedTextArea(localization.getFieldLabel("ExportBulletinDetails")));
		upperStuff.addSpace();
		upperStuff.addCentered(tocMsgAreaScrollPane);
		upperStuff.addSpace();
		upperStuff.add(includeAttachments);
		upperStuff.addSpace();
		if(UiSession.isAlphaTester)
		{
			upperStuff.add(includeAllVersions);
			upperStuff.addSpace();
		}
		
		UiVBox vBoxAll = new UiVBox();
		vBoxAll.add(upperStuff);
		
		vBoxAll.add(new Component[]{ok, cancel});
		getContentPane().add(vBoxAll);
		
		Utilities.packAndCenterWindow(this);
		setResizable(true);
		setVisible(true);
	}

	File askForDestinationFile()
	{
		FormatFilter filter = new XmlFileFilter(mainWindow.getLocalization());
		return mainWindow.showFileSaveDialog("ExportBulletins", defaultFileName, filter);
	}

	boolean userWantsToExportAttachments()
	{
		return includeAttachments.isSelected();
	}
	
	boolean userWantsToExportAllVersions()
	{
		return includeAllVersions.isSelected();
	}
	
	public void doExport(File destFile)
	{
		UiImportExportProgressMeterDlg progressDlg = new UiImportExportProgressMeterDlg(mainWindow, "ExportProgress");
		ExportBulletins exporter = new ExportBulletins(mainWindow, progressDlg);
		exporter.setExportPrivate(ALWAYS_EXPORT_PRIVATE_DATA);
		exporter.setExportAttachments(userWantsToExportAttachments());
		exporter.setExportAllVersions(userWantsToExportAllVersions());
		exporter.doExport(destFile, bulletins);
		if(exporter.didErrorOccur())
			mainWindow.notifyDlg(exporter.getExportErrorMessage(), exporter.getExportErrorMessageTokensMap());
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource().equals(ok))
		{
			boolean hasUnknown = false;
			for (int i = 0; i < bulletins.size(); i++)
			{
				Bulletin b = (Bulletin)bulletins.get(i);
				if(b.hasUnknownTags() || b.hasUnknownCustomField())
					hasUnknown = true;
			}
			if(hasUnknown)
			{
				if(!mainWindow.confirmDlg("ExportUnknownTags"))
					return;
			}
			
			File destFile = askForDestinationFile();
			if(destFile == null)
				return;

			
			doExport(destFile);
		}

		dispose();
	}

	private final boolean ALWAYS_EXPORT_PRIVATE_DATA = true;
	
	UiMainWindow mainWindow;
	Vector bulletins;
	JCheckBox includeAttachments;
	JCheckBox includeAllVersions;
	JButton ok;
	JButton cancel;
	String defaultFileName;
}
