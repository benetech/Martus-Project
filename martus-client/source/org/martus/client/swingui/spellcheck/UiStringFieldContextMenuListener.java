/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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
package org.martus.client.swingui.spellcheck;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.text.JTextComponent;

import org.martus.client.swingui.fields.UiStringField;
import org.martus.clientside.UiLocalization;
import org.martus.common.MartusLogger;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

import com.inet.jortho.AddWordAction;
import com.inet.jortho.MartusSpellCheckerListener;

/**
 * NOTE: This class is required because of the odd way the jortho
 * library works. You can't just get a list of suggestions.
 * Instead, they have a menu listener which detects when the
 * menu is about to be displayed, and adds the suggestions and
 * Add Word item to that menu.
 *
 * We want it formatted a little differently, and we need our
 * normal context menu items (e.g. cut and undo), so we need
 * this class.
 */
public class UiStringFieldContextMenuListener extends MartusSpellCheckerListener
{
	public UiStringFieldContextMenuListener(JPopupMenu menuToUse, UiStringField stringFieldToUse, UiLocalization localizationToUse)
	{
		super(menuToUse, null);

		menu = menuToUse;
		field = stringFieldToUse;
		localization = localizationToUse;
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent ev)
	{
		if(field.isSpellCheckEnabled())
		{
			super.popupMenuWillBecomeVisible(ev);

			Component[] menuItems = menu.getComponents();
			boldifySuggestedWords(menuItems);
			extractMoreSuggestionsSubmenu(menuItems);
	
			if(menuItems.length > 0)
				menu.addSeparator();
		}
		
		JTextComponent editor = getTextField();
		boolean editable = editor.isEditable();
		boolean selected = (editor.getSelectionStart() != editor.getSelectionEnd());

		JMenuItem menuItemCut = new JMenuItem(new ActionCut(getTextField(), getMenuLabel("cut")));
		menuItemCut.setEnabled(editable && selected);
		menuItemCut.setAccelerator(KeyStroke.getKeyStroke('X', KeyEvent.CTRL_DOWN_MASK));
		menu.add(menuItemCut);

		JMenuItem menuItemCopy = new JMenuItem(new ActionCopy(getTextField(), getMenuLabel("copy")));
		menuItemCopy.setEnabled(selected);
		menuItemCopy.setAccelerator(KeyStroke.getKeyStroke('C', KeyEvent.CTRL_DOWN_MASK));
		menu.add(menuItemCopy);

		JMenuItem menuItemPaste = new JMenuItem(new ActionPaste(getTextField(), getMenuLabel("paste")));
		menuItemPaste.setEnabled(editable);
		menuItemPaste.setAccelerator(KeyStroke.getKeyStroke('V', KeyEvent.CTRL_DOWN_MASK));
		menu.add(menuItemPaste);

		JMenuItem menuItemDelete = new JMenuItem(new ActionDelete(getTextField(), getMenuLabel("delete")));
		menuItemDelete.setEnabled(editable && selected);
		menu.add(menuItemDelete);

		JMenuItem menuItemSelectAll= new JMenuItem(new ActionSelectAll(getTextField(), getMenuLabel("selectall")));
		menuItemSelectAll.setEnabled(true);
		menuItemSelectAll.setAccelerator(KeyStroke.getKeyStroke('A', KeyEvent.CTRL_DOWN_MASK));
		menu.add(menuItemSelectAll);

	}

	private void extractMoreSuggestionsSubmenu(Component[] menuItems)
	{
		JMenu suggestions = new JMenu(getMenuLabel("MoreSpellingSuggestions"));
		final int MAX_SUGGESTIONS_AT_TOP_LEVEL = 5;
		boolean hasMoreSuggestions = false;
		for(int i = MAX_SUGGESTIONS_AT_TOP_LEVEL; i < menuItems.length - 1; ++i)
		{
			Component suggestionToMove = menuItems[i];
			menu.remove(suggestionToMove);
			suggestions.add(suggestionToMove);
			hasMoreSuggestions = true;
		}
		if(hasMoreSuggestions)
			menu.insert(suggestions, menu.getComponentCount() - 1);
	}

	private void boldifySuggestedWords(Component[] menuItems)
	{
		int menuItemCountExcludingAddWord = menuItems.length - 1;
		for(int i = 0; i < menuItemCountExcludingAddWord; ++i)
		{
			Font oldFont = menuItems[i].getFont();
			Font newFont = oldFont.deriveFont(Font.BOLD);
			menuItems[i].setFont(newFont);
		}
	}

	@Override
	protected void addMenuItemAddToDictionary(JTextComponent editor, String word, boolean addSeparator)
	{
		String labelWithToken = localization.getMenuLabel("AddToDictionary");
		try
		{
			String wordLabel = TokenReplacement.replaceToken(labelWithToken, "#NewWord#", word);
			Action addWordAction = new AddWordAction(editor, word, wordLabel);
			menu.add(addWordAction);
		} 
		catch (TokenInvalidException e)
		{
			MartusLogger.logException(e);
		}
	}


	private JTextComponent getTextField()
	{
		return field.getTextComponent();
	}

	static abstract class TextComponentAction extends AbstractAction
	{
		public TextComponentAction(JTextComponent textComponentToUse, String label)
		{
			super(label);
			
			textComponent = textComponentToUse;
		}
		
		public JTextComponent getTextComponent()
		{
			return textComponent;
		}
		
		private JTextComponent textComponent;
	}

	static class ActionCut extends TextComponentAction
	{
		public ActionCut(JTextComponent textComponentToUse, String label)
		{
			super(textComponentToUse, label);
		}

		public void actionPerformed(ActionEvent ae)
		{
			getTextComponent().cut();
		}
	}

	static class ActionCopy extends TextComponentAction
	{
		public ActionCopy(JTextComponent textComponentToUse, String label)
		{
			super(textComponentToUse, label);
		}

		public void actionPerformed(ActionEvent ae)
		{
			getTextComponent().copy();
		}
	}

	static class ActionPaste extends TextComponentAction
	{
		public ActionPaste(JTextComponent textComponentToUse, String label)
		{
			super(textComponentToUse, label);
		}

		public void actionPerformed(ActionEvent ae)
		{
			getTextComponent().paste();
		}
	}

	static class ActionDelete extends TextComponentAction
	{
		public ActionDelete(JTextComponent textComponentToUse, String label)
		{
			super(textComponentToUse, label);
		}

		public void actionPerformed(ActionEvent ae)
		{
			getTextComponent().replaceSelection("");
		}
	}

	static class ActionSelectAll extends TextComponentAction
	{
		public ActionSelectAll(JTextComponent textComponentToUse, String label)
		{
			super(textComponentToUse, label);
		}

		public void actionPerformed(ActionEvent ae)
		{
			getTextComponent().selectAll();
		}

	}

	private String getMenuLabel(String tag)
	{
		return localization.getMenuLabel(tag);
	}

	private JPopupMenu menu;
	private UiStringField field;
	private UiLocalization localization;
}
