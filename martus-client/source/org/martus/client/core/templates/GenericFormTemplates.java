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
package org.martus.client.core.templates;

import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.FormTemplate.FutureVersionException;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.inputstreamwithseek.UrlInputStreamWithSeek;

public class GenericFormTemplates
{
	public static ObservableList<FormTemplate> getDefaultFormTemplateChoices(MartusCrypto security)
	{
		try
		{
			Vector<FormTemplate> customTemplates = loadFormTemplates(security);

			return FXCollections.observableArrayList(customTemplates);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return FXCollections.observableArrayList();
		}
	}
	
	private static Vector<FormTemplate> loadFormTemplates(MartusCrypto security) throws Exception
	{
		String[] formTemplateFileNames = new String[]
		{
			"Amnesty-Urgent-Actions.mct", 
			"Journalist-Example.mct", 
			"Martus-Customization-Example.mct", 
			"UN-Disappearances.mct", 
			"UN-Special-Rapporteur-Executions.mct", 
		};
		Vector<FormTemplate> formTemplates = new Vector<FormTemplate>();
		for (String formTemplateFileName : formTemplateFileNames)
		{
			URL url = GenericFormTemplates.class.getResource(formTemplateFileName);
			FormTemplate formTemplate = importFormTemplate(url, security);
			formTemplates.add(formTemplate);
		}
		
		return formTemplates;
	}

	private static FormTemplate importFormTemplate(URL url, MartusCrypto security) throws Exception, FutureVersionException, IOException
	{
		InputStreamWithSeek withSeek = new UrlInputStreamWithSeek(url);
		try
		{
			FormTemplate formTemplate = new FormTemplate();
			formTemplate.importTemplate(security, withSeek);

			return formTemplate;
		}
		finally
		{
			withSeek.close();
		}
	}
}
