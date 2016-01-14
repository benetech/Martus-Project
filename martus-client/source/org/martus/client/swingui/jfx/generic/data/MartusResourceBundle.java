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
package org.martus.client.swingui.jfx.generic.data;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.martus.client.swingui.MartusLocalization;


public class MartusResourceBundle extends ResourceBundle
{
	public MartusResourceBundle(MartusLocalization localizationToUse)
	{
		localization = localizationToUse;
	}
	
	@Override
	public boolean containsKey(String key)
	{
		return true;
	}
	
	@Override
	protected Object handleGetObject(String key)
	{
		String[] prefixAndKey = key.split("\\.");
		String prefix = prefixAndKey[0];
		if(prefix.equals(MENU_CONTROL))
			return localization.getMenuLabel(prefixAndKey[1]);
		if(prefix.equals(TOOLTIP_CONTROL))
			return localization.getTooltipLabel(prefixAndKey[1]);
		if(prefix.equals(BUTTON_CONTROL))
			return localization.getButtonLabel(prefixAndKey[1]);
		if(prefix.equals(TITLE_CONTROL))
			return localization.getWindowTitle(prefixAndKey[1]);
		return localization.getFieldLabel(key);
	}

	@Override
	public Enumeration<String> getKeys()
	{
		return null;
	}

	private MartusLocalization localization;
	private static final String TOOLTIP_CONTROL = "Tooltip";
	private static final String BUTTON_CONTROL = "Button";
	private static final String MENU_CONTROL = "Menu";
	private static final String TITLE_CONTROL = "Title";
}
