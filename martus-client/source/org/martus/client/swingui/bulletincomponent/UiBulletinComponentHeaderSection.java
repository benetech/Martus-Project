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

package org.martus.client.swingui.bulletincomponent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiBulletinDetailsDialog;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.util.TokenReplacement;


public class UiBulletinComponentHeaderSection extends UiBulletinComponentSection
{
	UiBulletinComponentHeaderSection(UiMainWindow mainWindowToUse, String tagQualifierToUse)
	{
		super(mainWindowToUse, "_BulletinSectionHeader");
		tagQualifier = tagQualifierToUse;
		MartusLocalization localization = getLocalization();

		String buttonText = localization.getButtonLabel("BulletinDetails");
		JButton detailsButton = new UiButton(buttonText);
		detailsButton.addActionListener(new DetailsListener());
		addComponents(detailsButton, new UiLabel(""));

		lastSavedLabel = new UiLabel(localization.getFieldLabel(Bulletin.TAGLASTSAVED));
		dateTime = new UiLabel("");
		EtchedBorder border = new EtchedBorder();
		dateTime.setBorder(border);
		addComponents(lastSavedLabel, dateTime);

		JLabel versionLabel = new UiLabel(localization.getFieldLabel("BulletinVersionNumber"));
		versionNumber = new UiLabel("#");
		versionNumber.setBorder(border);
		addComponents(versionLabel, versionNumber);
		
		hqLabel = new UiLabel(localization.getFieldLabel("HQSummaryLabel"));
		hqSummary = new UiLabel("");
		addComponents(hqLabel, hqSummary);
	}
	
	public void setBulletin(Bulletin bulletinToShow)
	{
		bulletin = bulletinToShow;
		int numberOfHqs = bulletin.getAuthorizedToReadKeys().size();
		updateNumberOfHQs(numberOfHqs);
		versionNumber.setText("  " + Integer.toString(bulletin.getVersion())+ "  ");

		long time = bulletin.getLastSavedTime();
		if(time == 0)
		{
			lastSavedLabel.setVisible(false);
			dateTime.setVisible(false);
		}
		else
		{
			dateTime.setText("  " + getLocalization().formatDateTime(time) + "  ");
			lastSavedLabel.setVisible(true);
			dateTime.setVisible(true);
		}
	}

	public void updateNumberOfHQs(int numberOfHqs) 
	{
		if(numberOfHqs > 0)
		{
			hqSummary.setText(getHQSummaryString(numberOfHqs));
			hqLabel.setVisible(true);
			hqSummary.setVisible(true);
		}
		else
		{
			hqSummary.setText("");
			hqLabel.setVisible(false);
			hqSummary.setVisible(false);
		}
		hqLabel.updateUI();
		hqSummary.updateUI();
	}
	
	private String getHQSummaryString(int numberOfHqs)
	{
		String summaryText = getLocalization().getFieldLabel(tagQualifier + "BulletinHQInfo");
		try
		{
			HashMap tokenReplacement = new HashMap();
			tokenReplacement.put("#NumberOfHQs#", Integer.toString(numberOfHqs));
			summaryText = TokenReplacement.replaceTokens(summaryText, tokenReplacement);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return summaryText;
	}
	
	void showBulletinDetails() throws Exception
	{
		UiBulletinDetailsDialog dlg = new UiBulletinDetailsDialog(mainWindow, bulletin, tagQualifier);
		dlg.setVisible(true);
	}

	class DetailsListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			try
			{
				showBulletinDetails();
			}
			catch(Exception e)
			{
				getMainWindow().notifyDlg("UnexpectedError");
			}
		}
		
	}


	String tagQualifier;
	Bulletin bulletin;
	private JLabel lastSavedLabel;
	private JLabel dateTime;
	private JLabel hqLabel;
	private JLabel hqSummary;
	private JLabel versionNumber;
}
