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

import java.util.HashMap;

import javax.swing.JFrame;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.grids.GridDateRangeCellEditor;
import org.martus.clientside.UiLocalization;
import org.martus.clientside.UiUtilities;

public class UiDialogLauncher
{
	public UiDialogLauncher(UiMainWindow mainWindowToUse, JFrame frameToUse)
	{
		super();
		mainWindow = mainWindowToUse;
		frame = frameToUse;
	}
	
	public UiDialogLauncher(JFrame frameToUse, UiLocalization localizationToUse)
	{
		super();
		frame = frameToUse;
		localization = localizationToUse;
	}
	
	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	public boolean ShowConfirmDialog(String baseTag)
	{
		if(frame == null)
			return true;
		return UiUtilities.confirmDlg(getLocalization(), frame, baseTag);
	}

	public boolean showConfirmDlg(String baseTag, HashMap map)
	{
		return UiUtilities.confirmDlg(getLocalization(), frame, baseTag, map);
	}

	public void ShowNotifyDialog(String baseTag)
	{
		UiUtilities.notifyDlg(getLocalization(), frame, baseTag);
	}

	public void messageDlg(GridDateRangeCellEditor gridDateRangeCellEditor,
			String baseTag, String message, HashMap tokenReplacement)
	{
		UiUtilities.messageDlg(getLocalization(), frame, baseTag, message, tokenReplacement);
	}

	public UiLocalization getLocalization()
	{
		if(localization != null)
			return localization;
		return getMainWindow().getLocalization();
	}
	
	private UiMainWindow mainWindow;
	private JFrame frame;
	private UiLocalization localization;
}
