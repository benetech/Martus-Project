/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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

package org.martus.client.swingui.fields;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.text.JTextComponent;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.spellcheck.UiStringFieldContextMenuListener;
import org.martus.clientside.UiLocalization;
import org.martus.swing.UiPopupMenu;

import com.inet.jortho.SpellChecker;

public abstract class UiStringField extends UiField
{
	public UiStringField(UiLocalization localizationToUse)
	{
		super(localizationToUse);
		localization = localizationToUse;
		mouseAdapter = new TextFieldMouseAdapter();
	}

	public void supportContextMenu()
	{
		getEditor().addMouseListener(mouseAdapter);
	}

	public void contextMenu(MouseEvent e)
	{
		UiPopupMenu menu = new UiPopupMenu();
		menu.addPopupMenuListener(new UiStringFieldContextMenuListener(menu, this, localization));
		menu.show(getEditor(), e.getX(), e.getY());
	}
	
	public abstract JTextComponent getTextComponent();

	abstract public JTextComponent getEditor();

	class TextFieldMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			super.mouseClicked(e);
			if(e.isMetaDown())
				contextMenu(e);
		}
	}

	@Override
	public void updateSpellChecker(String bulletinLanguageCode)
	{
		boolean isImplicitlyEnglish = bulletinLanguageCode.equals("?");
		boolean isExplicitlyEnglish = bulletinLanguageCode.equals(MartusLocalization.ENGLISH);
		boolean shouldSpellCheck = isImplicitlyEnglish || isExplicitlyEnglish;
		if(shouldSpellCheck)
		{
			boolean hasPopup = false;
			boolean hasShortcut = false;
			boolean hasAutospell = true;
			// NOTE: JOrtho will only show squigglies for editable fields 
			SpellChecker.register(getTextComponent(), hasPopup, hasShortcut, hasAutospell);
			isSpellCheckEnabled = true;
		} 
		else
		{
			isSpellCheckEnabled = false;
			SpellChecker.unregister(getTextComponent());
		}
	}
	
	public boolean isSpellCheckEnabled()
	{
		return isSpellCheckEnabled;
	}
	
	UiLocalization localization;
	MouseAdapter mouseAdapter;
	private boolean isSpellCheckEnabled;
}

