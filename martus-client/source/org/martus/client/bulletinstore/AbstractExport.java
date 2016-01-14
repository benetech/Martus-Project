/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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
package org.martus.client.bulletinstore;

import java.io.File;
import java.util.Map;
import java.util.Vector;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.ProgressMeterInterface;

abstract public class AbstractExport 
{
	public AbstractExport(UiMainWindow mainWindowToUse, ProgressMeterInterface progressDlgToUse)
	{
		mainWindow = mainWindowToUse;
		progressDlg = progressDlgToUse;
	}

	abstract public void doExport(File destFile, Vector bulletinsToUse);
	
	public String getExportErrorMessage()
	{
		return exportErrorMessageTag;
	}
	
	public void setExportErrorMessage(String exportMessageTag)
	{
		this.exportErrorMessageTag = exportMessageTag;
	}
	
	public Map getExportErrorMessageTokensMap()
	{
		return exportErrorMessageTokensMap;
	}
	
	public void setExportErrorMessageTokensMap(Map exportMessageTokensMap)
	{
		this.exportErrorMessageTokensMap = exportMessageTokensMap;
	}
	
	public boolean didErrorOccur()
	{
		return errorOccured;
	}
	
	public void setErrorOccured(boolean errorOccured)
	{
		this.errorOccured = errorOccured;
	}
	
	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	public ProgressMeterInterface getProgressDlg()
	{
		return progressDlg;
	}


	public boolean isExportPrivate()
	{
		return exportPrivate;
	}

	public void setExportPrivate(boolean exportPrivate)
	{
		this.exportPrivate = exportPrivate;
	}


	public boolean isExportAttachments()
	{
		return exportAttachments;
	}

	public void setExportAttachments(boolean exportAttachments)
	{
		this.exportAttachments = exportAttachments;
	}


	public boolean isExportAllVersions()
	{
		return exportAllVersions;
	}

	public void setExportAllVersions(boolean exportAllVersions)
	{
		this.exportAllVersions = exportAllVersions;
	}


	private UiMainWindow mainWindow;
	private ProgressMeterInterface progressDlg;
	
	private String exportErrorMessageTag;
	private Map exportErrorMessageTokensMap;
	private boolean errorOccured;
	private boolean exportPrivate;
	private boolean exportAttachments;
	private boolean exportAllVersions;
}


