/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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

package org.martus.client.swingui.bulletincomponent;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.martus.client.swingui.MartusLocalization;
import org.martus.swing.UiLabel;
import org.martus.util.language.LanguageOptions;

import com.jhlabs.awt.Alignment;
import com.jhlabs.awt.GridLayoutPlus;

class FieldHolder extends JPanel
{
	public FieldHolder(MartusLocalization localizationToUse)
	{
		super(new BorderLayout());
		GridLayoutPlus gridLayout = new GridLayoutPlus(1, 0, 2, 5, 2, 5);
		gridLayout.setFill(Alignment.FILL_NONE);
		if(LanguageOptions.isRightToLeftLanguage())
			gridLayout.setAlignment(Alignment.EAST);
		panel = new JPanel(gridLayout);
		localization = localizationToUse;
		showField();
	}

	public void addField(JComponent fieldToHold) 
	{
		// NOTE: BasicGridLayout doesn't support component orientation, 
		// so we have to fake it
		int insertAt = panel.getComponentCount();
		if(LanguageOptions.isRightToLeftLanguage())
			insertAt = 0;
		panel.add(fieldToHold, insertAt);
	}
	
	void showField()
	{
		removeAll();
		add(panel);
		isShown = true;
	}
	
	void hideField()
	{
		removeAll();
		add(new UiLabel(localization.getFieldLabel("DataIsHidden")));
		isShown = false;
	}
	
	boolean isShown()
	{
		return isShown;
	}
	
	private boolean isShown;
	private JPanel panel;
	private MartusLocalization localization;
}
