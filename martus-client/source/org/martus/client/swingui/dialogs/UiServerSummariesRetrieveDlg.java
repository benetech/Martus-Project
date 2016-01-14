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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.tablemodels.RetrieveTableModel;
import org.martus.common.BulletinSummary;
import org.martus.common.packet.UniversalId;
import org.martus.swing.FontHandler;


public class UiServerSummariesRetrieveDlg extends UiServerSummariesDlg
{

	public UiServerSummariesRetrieveDlg(UiMainWindow owner,
			RetrieveTableModel tableModel, String windowTitleTag)
	{
		super(owner, tableModel, windowTitleTag);
	}
	
	public void initialize()
	{
		initialize("RetrieveSummariesMessage", "retrieve");
	}

	public boolean confirmIntentionsBeforeClosing()
	{
		Vector uidsSelected = getUniversalIdList();
		Set uidsBeingUpgraded = model.getUidsThatWouldBeUpgrades(uidsSelected);
		if(uidsBeingUpgraded.size() > 0)
		{
			String titles = "";
			for(Iterator iter = uidsBeingUpgraded.iterator(); iter.hasNext();)
			{
				UniversalId uid = (UniversalId) iter.next();
				titles += "   " + getDisplayableTitleFromSummary(uid) + "\n";
			}
			Map replacements = new HashMap();
			replacements.put("#Titles#", titles);
			if(!mainWindow.confirmDlg("RetrieveNewerVersions", replacements))
				return false;
		}
		
		return super.confirmIntentionsBeforeClosing();
	}
	
	private String getDisplayableTitleFromSummary(UniversalId uid)
	{
		UiFontEncodingHelper fontHelper = new UiFontEncodingHelper(FontHandler.isDoZawgyiConversion());
		Vector summaries = model.getAllSummaries();
		synchronized (summaries)
		{
			for (int j = 0; j < summaries.size(); ++j)
			{
				BulletinSummary summary = (BulletinSummary) summaries.get(j);
				if (uid.equals(summary.getUniversalId()))
				{
					String storableTitle = summary.getStorableTitle();
					return fontHelper.getDisplayable(storableTitle);
				}
			}
		}
		return "?";
	}

	String getNoneSelectedTag()
	{
		return "retrievenothing";
	}

}
