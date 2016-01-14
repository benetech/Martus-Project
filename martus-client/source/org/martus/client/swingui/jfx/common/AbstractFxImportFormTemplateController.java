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
package org.martus.client.swingui.jfx.common;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxPopupController;
import org.martus.client.swingui.jfx.setupwizard.tasks.DownloadTemplateListForAccountTask;
import org.martus.client.swingui.jfx.setupwizard.tasks.TaskWithTimeout;
import org.martus.common.ContactKey;
import org.martus.common.fieldspec.FormTemplate;

abstract public class AbstractFxImportFormTemplateController extends FxPopupController
{
	public AbstractFxImportFormTemplateController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	protected ObservableList<FormTemplate> getFormTemplates(ContactKey contactKey) throws Exception
	{
		ObservableList<FormTemplate> formTemplates = FXCollections.observableArrayList();
		getFormTemplates(contactKey, formTemplates);

		return formTemplates;
	}

	protected void getFormTemplates(ContactKey contactKey, ObservableList<FormTemplate> formTemplates) throws Exception
	{
		TaskWithTimeout task = new DownloadTemplateListForAccountTask(getApp(), contactKey, formTemplates);
		MartusLocalization localization = getLocalization();
		String message = localization.getFieldLabel("LoadingTemplates");
		showTimeoutDialog(message, task);
	}
	
	@Override
	public String toString()
	{
		return getLabel();
	}
	
	abstract public String getLabel();
	
	abstract public FormTemplate getSelectedFormTemplate();
}
