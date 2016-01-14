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
package org.martus.client.swingui.jfx.setupwizard.tasks;

import java.util.Vector;

import javafx.collections.ObservableList;

import org.martus.client.core.MartusApp;
import org.martus.common.ContactKey;
import org.martus.common.fieldspec.FormTemplate;

public class DownloadTemplateListForAccountTask extends ServerCallTask
{
	public DownloadTemplateListForAccountTask(MartusApp appToUse, ContactKey keyToUse, ObservableList<FormTemplate> listToUse)
	{
		super(appToUse);
		
		contactKey = keyToUse;
		formTemplates = listToUse;
	}

	@Override
	protected Void call() throws Exception
	{
		fillFormTemplates();
		
		return null;
	}

	private void fillFormTemplates() throws Exception
	{
		String publicKey = contactKey.getPublicKey();
		Vector returnedVectorListOfTemplatesFromServer = getApp().getListOfFormTemplatesOnServer(publicKey);
		
		fillFormTemplatesFromResults(publicKey, returnedVectorListOfTemplatesFromServer);
	}
	
	private void fillFormTemplatesFromResults(String publicKey, Vector<Vector<String>> returnedVectorListOfTemplatesFromServer) throws Exception
	{
		for (Vector<String> titleAndDescrptonVector : returnedVectorListOfTemplatesFromServer)
		{
			String title = titleAndDescrptonVector.get(0);
			formTemplates.add(getApp().getFormTemplateOnServer(publicKey, title));
		}
	}

	private ContactKey contactKey;
	private ObservableList<FormTemplate> formTemplates;
}
