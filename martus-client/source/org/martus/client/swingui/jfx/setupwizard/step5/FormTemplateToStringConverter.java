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
package org.martus.client.swingui.jfx.setupwizard.step5;

import javafx.util.StringConverter;

import org.martus.client.swingui.MartusLocalization;
import org.martus.common.fieldspec.FormTemplate;

public class FormTemplateToStringConverter extends StringConverter<FormTemplate>
{
	public FormTemplateToStringConverter(MartusLocalization localizationToUse)
	{
		localization = localizationToUse;
	}

	@Override
	public String toString(FormTemplate formTemplate)
	{
		if (formTemplate == null)
			return "";
		
		String title = formTemplate.getTitle();
		if (title.length() > 0)
			return title;
		
		return localization.getFieldLabel("NoFormTemplateTitle");
	}

	@Override
	public FormTemplate fromString(String string)
	{
		return null;
	}
	
	private MartusLocalization localization;
}