/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.swingui.jfx.landing.general;

import java.io.File;
import java.util.Vector;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.filefilters.MCTFileFilter;
import org.martus.client.swingui.filefilters.XmlFileFilter;
import org.martus.clientside.FormatFilter;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.FormTemplate;

public class ExportTemplateAction implements ActionDoer
{
	public ExportTemplateAction(UiMainWindow mainWindowToUse, FormTemplate formTemplateToExportToUse)
	{
		mainWindow = mainWindowToUse;
		formTemplateToExport = formTemplateToExportToUse;
	}
	
	@Override
	public void doAction()
	{
		try
		{
			exportTemplate(formTemplateToExport);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	private void exportTemplate(FormTemplate template) throws Exception
	{
		Vector<FormatFilter> filters = new Vector();
		filters.add(new MCTFileFilter(getLocalization()));
		XmlFileFilter xmlFilter = new XmlFileFilter(getLocalization());
		filters.add(xmlFilter);

		File selectedFile = getMainWindow().showFileSaveDialog("ExportCustomization", filters);
		if(selectedFile == null)
			return;
		
		String lowerCaseFileName = selectedFile.getName().toLowerCase();
		if(lowerCaseFileName.endsWith(xmlFilter.getExtension().toLowerCase()))
			template.exportTopSection(selectedFile);
		else
			template.exportTemplate(getApp().getSecurity(), selectedFile);
	}

	private MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}

	private MartusApp getApp()
	{
		return getMainWindow().getApp();
	}
	
	private UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	private UiMainWindow mainWindow;
	private FormTemplate formTemplateToExport;
}
