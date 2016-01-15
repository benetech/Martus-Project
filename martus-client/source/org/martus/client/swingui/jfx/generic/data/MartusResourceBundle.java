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
import java.util.SortedSet;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiSession;
import org.martus.common.MiniLocalization;


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
			return getToolTipText(prefixAndKey);
		if(prefix.equals(BUTTON_CONTROL))
			return localization.getButtonLabel(prefixAndKey[1]);
		if(prefix.equals(TITLE_CONTROL))
			return localization.getWindowTitle(prefixAndKey[1]);
		if(prefix.equals(IS_FULL_DESKTOP_FLAVOR))
			return Boolean.toString(UiSession.isFullDesktopFlavor());
					
		return localization.getFieldLabel(key);
	}

	private String getToolTipText(String[] prefixAndKey)
	{
		String code = prefixAndKey[1];
		String tooltipCodeForDesktopFlavor = getTooltipCodeForDesktopFlavor(code);
		
		return localization.getTooltipLabel(tooltipCodeForDesktopFlavor);
	}
	
	private String getTooltipCodeForDesktopFlavor(String code)
	{
		if (UiSession.isFullDesktopFlavor())
			return code;
		
		String readonlyDesktopFlavorToolTipCode =  READ_ONLY_DESKTOP_TOOLTIP_PREFIX + code;
		if (tooltipExists(readonlyDesktopFlavorToolTipCode))
			return readonlyDesktopFlavorToolTipCode;
		
		return code;
	}

	private boolean tooltipExists(String tooltipCode)
	{
		SortedSet<String> allKeysSorted = localization.getAllKeysSorted();			
		String tooltipKey = MiniLocalization.createKey(MiniLocalization.CATEGORY_TOOLTIP_CODE, tooltipCode);
		
		return allKeysSorted.contains(tooltipKey);
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
	private static final String IS_FULL_DESKTOP_FLAVOR = "isFullDesktopFlavor";
	private static final String READ_ONLY_DESKTOP_TOOLTIP_PREFIX = "ReadonlyDesktopFlavor";
}
