/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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
package org.martus.client.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.client.core.SafeReadableBulletin;
import org.martus.client.swingui.dialogs.UiProgressWithCancelDlg;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.field.MartusField;
import org.martus.common.field.MartusSearchableGridColumnField;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.packet.UniversalId;

public class FieldValuesLoader
{
	public FieldValuesLoader(MartusApp app)
	{
		store = app.getStore();
		localization = app.getLocalization();
	}
	
	public HashSet loadFieldValuesFromAllBulletinRevisions(UiProgressWithCancelDlg progressMeter, FieldSpec fieldSpec)
	{
		MiniFieldSpec miniSpec = new MiniFieldSpec(fieldSpec);
		HashSet choices = getFieldValueFromAllBulletinRevisions(progressMeter, miniSpec);
		progressMeter.finished();
		return choices;
	}

	private HashSet getFieldValueFromAllBulletinRevisions(UiProgressWithCancelDlg progressMeter, MiniFieldSpec miniSpec)
	{
		HashSet choices = new HashSet();
		Vector allUids = store.getUidsOfAllBulletinRevisions();
		for(int i = 0; i < allUids.size(); ++i)
		{
			progressMeter.updateProgressMeter(i, allUids.size());
			if(progressMeter.shouldExit())
				return null;

			Bulletin revision = store.getBulletinRevision((UniversalId) allUids.get(i));
			SafeReadableBulletin bulletin = new SafeReadableBulletin(revision, localization);
			MartusField field = bulletin.getPossiblyNestedField(miniSpec);
			if(field != null)
			{
				if(field.isGridColumnField())
				{
					Collection gridColumnChoices = getGridColumnChoices((MartusSearchableGridColumnField)field);
					choices.addAll(gridColumnChoices);
				}
				else
				{
					String value = field.getData();
					choices.add(createChoiceItem(value));
				}
			}
		}
		return choices;
	}

	private ChoiceItem createChoiceItem(String value)
	{
		return new ChoiceItem("\"" + value + "\"", value);
	}

	private Collection getGridColumnChoices(MartusSearchableGridColumnField field)
	{
		HashSet choices = new HashSet();
		for(int row = 0; row < field.getRowCount(); ++row)
		{
			String value = field.getData(row);
			choices.add(createChoiceItem(value));
		}
		return choices;
	}

	private ClientBulletinStore store;
	private MiniLocalization localization;
}