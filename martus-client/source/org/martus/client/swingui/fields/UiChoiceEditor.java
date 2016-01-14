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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;

import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.fields.UiEditableGrid.EnterAction;
import org.martus.client.swingui.fields.UiEditableGrid.ShiftTabAction;
import org.martus.client.swingui.fields.UiEditableGrid.SpaceAction;
import org.martus.client.swingui.fields.UiEditableGrid.TabAction;
import org.martus.common.ListOfReusableChoicesLists;
import org.martus.common.MiniLocalization;
import org.martus.common.ReusableChoices;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.swing.FontHandler;
import org.martus.swing.UiComboBox;
import org.martus.swing.UiLanguageDirection;
import org.martus.swing.Utilities;
import org.martus.util.language.LanguageOptions;

public class UiChoiceEditor extends UiChoice implements ActionListener
{
	public UiChoiceEditor(MiniLocalization localizationToUse)
	{
		super(localizationToUse);
		fontHelper = new UiFontEncodingHelper(FontHandler.isDoZawgyiConversion());

		container = Box.createHorizontalBox();
		comboBoxes = new Vector();

		addActionListener(this);
	}
	
	public void addActionListener(ActionListener listener)
	{
		for(int i = 0; i < getLevelCount(); ++i)
			getComboBox(i).addActionListener(listener);
	}
	
	private int getLevelCount()
	{
		return comboBoxes.size();
	}

	private UiComboBox getComboBox(int i)
	{
		return (UiComboBox) comboBoxes.get(i);
	}

	class UiChoiceListCellRenderer extends DefaultListCellRenderer
	{
		
		public Component getListCellRendererComponent(JList list, Object choiceItem, int index, boolean isSelected, boolean cellHasFocus)
		{
			String spaceSoValueWontBeHiddenIfEmpty = " ";
			String choiceText = "";
			if(choiceItem != null)
				choiceText = choiceItem.toString();

			String displayString = choiceText + spaceSoValueWontBeHiddenIfEmpty;
			displayString  = getFontHelper().getDisplayable(displayString);
			Component cellRenderer = super.getListCellRendererComponent(list, displayString, index, isSelected,
					cellHasFocus);
			cellRenderer.setComponentOrientation(UiLanguageDirection.getComponentOrientation());
			return cellRenderer;
		}

		public Dimension getPreferredSize()
		{
			Dimension d = super.getPreferredSize();
			d.height += LanguageOptions.getExtraHeightIfNecessary();
			return d;
		}
	}

	public String getText()
	{
		String result = "";
		for(int level = 0; level < getLevelCount(); ++level)
		{
			UiComboBox widget = getComboBox(level);
			if(widget == null)
				System.out.println("UiChoiceEditor.getText null widget!");
			ChoiceItem choice = (ChoiceItem)widget.getSelectedItem();
			if(choice == null)
				break;
			if(choice.getCode().length() == 0)
				break;
			result = choice.getCode();
		}

		return result;
		
	}

	public void setText(String newCode)
	{
		if (getLevelCount() == 0)
		{
			if (newCode.length() > 0)
				System.out.println("Attempted to setText " + newCode + " in a choice editor with no dropdowns");
			return;
		}

		for (int level = 0; level < getLevelCount(); ++level)
		{
			UiComboBox widget = getComboBox(level);
			int rowToSelect = -1;

			String codeAtThisLevel = truncateCodeToLevel(newCode, level);
			rowToSelect = findItemByCode(widget, codeAtThisLevel);
			if (rowToSelect < 0 && codeAtThisLevel.length() > 0)
				rowToSelect = findItemByCode(widget, "");

			widget.setSelectedIndex(rowToSelect);
		}
	}

	private String truncateCodeToLevel(String newCode, int levelToTruncateTo)
	{
		++levelToTruncateTo;
		int stopAt = -1;
		for (int level = 0; level < levelToTruncateTo; ++level)
			stopAt = newCode.indexOf('.', stopAt + 1);

		if (stopAt >= 0)
			return newCode.substring(0, stopAt);

		return newCode;
	}

	int findItemByCode(UiComboBox widget, String code)
	{
		for(int row = 0; row < widget.getItemCount(); ++row)
		{
			ChoiceItem choiceItem = (ChoiceItem)widget.getItemAt(row);
			if(choiceItem.getCode().equals(code))
			{
				widget.setSelectedIndex(row);
				return row;
			}
		}
		
		return -1;
	}

	public void setChoices(ListOfReusableChoicesLists newChoices)
	{
		if(newChoices == null)
			System.out.println("UiChoiceEditor.setChoices called with null choices");
		choiceLists = newChoices;
		
		String existingValue = getText();
		for(int i = 0; i < comboBoxes.size(); ++i)
			((UiComboBox)comboBoxes.get(i)).removeActionListener(this);
		comboBoxes.clear();
		container.removeAll();

		for(int level = 0; level < newChoices.size(); ++level)
		{
			ReusableChoices reusableChoices = newChoices.get(level);
	
			UiComboBox combo = new UiComboBox();
			combo.setRenderer(new UiChoiceListCellRenderer());
			for(int i = 0; i < reusableChoices.size(); ++i)
			{
				combo.addItem(reusableChoices.get(i));
			}
			combo.addActionListener(this);
			comboBoxes.add(combo);
		}
		Utilities.addComponentsRespectingOrientation(container, (Component[])comboBoxes.toArray(new Component[0]));
		
		setText(existingValue);
		reBindKeysForFocusableComponents();
	}

	public void actionPerformed(ActionEvent e) 
	{
		updateEditabilityOfComboBoxes(e.getSource());
	}

	private void updateEditabilityOfComboBoxes(Object eventSource)
	{
		boolean shouldBeEnabled = true;

		for(int level = 0; level < comboBoxes.size(); ++level)
		{
			UiComboBox combo = (UiComboBox) comboBoxes.get(level);
			
			if(level > 0)
			{
				UiComboBox previousCombo = (UiComboBox) comboBoxes.get(level-1);
				ChoiceItem previousSelected = (ChoiceItem) previousCombo.getSelectedItem();
				String previousCode = "";
				if(previousSelected == null)
					previousSelected = (ChoiceItem) previousCombo.getItemAt(findItemByCode(previousCombo, ""));
				if(previousSelected != null)
					previousCode = previousSelected.getCode();
				
				if(previousCode.length() > 0)
				{
					updateWidgetChoices(level, previousCode);
				}
				else
				{
					shouldBeEnabled = false;
					combo.setSelectedIndex(-1);
				}
			}
			combo.setEnabled(shouldBeEnabled);
		}
		
	}

	private void updateWidgetChoices(int level, String previousCode)
	{
		if(isUpdateInProgress)
			return;
		
		isUpdateInProgress = true;
		try
		{
			UiComboBox combo = getComboBox(level);
			ChoiceItem wasSelected = (ChoiceItem) combo.getSelectedItem();
	
			ReusableChoices existingChoices = new ReusableChoices("", "");
			for(int row = 0; row < combo.getItemCount(); ++row)
			{
				ChoiceItem choice = (ChoiceItem) combo.getItemAt(row);
				existingChoices.add(choice);
			}
			ReusableChoices possibleChoices = choiceLists.get(level);
			ReusableChoices newChoices = new ReusableChoices("", "");
			for(int choiceIndex = 0; choiceIndex < possibleChoices.size(); ++choiceIndex)
			{
				ChoiceItem choice = possibleChoices.get(choiceIndex);
				if(choice.nestedCodeStartsWith(previousCode))
				if(choice.getCode().startsWith(previousCode))
					newChoices.add(choice);
			}
			
			if(newChoices.findByCode("") == null)
				newChoices.insertAtTop(new ChoiceItem("", ""));
	
			if(newChoices.equals(existingChoices))
				return;
			
			combo.removeAllItems();
			for(int choiceIndex = 0; choiceIndex < newChoices.size(); ++choiceIndex)
			{
				combo.addItem(newChoices.get(choiceIndex));
			}
			
			combo.setSelectedItem(wasSelected);
		}
		finally
		{
			isUpdateInProgress = false;
		}
	}

	public JComponent getComponent()
	{
		return container;
	}

	public JComponent[] getFocusableComponents()
	{
		return (JComponent[])comboBoxes.toArray(new JComponent[0]);
	}
	
	UiFontEncodingHelper getFontHelper()
	{
		return fontHelper;
	}

	public void setActions(EnterAction enterActionToUse, SpaceAction spaceActionToUse, TabAction tabActionToUse, ShiftTabAction shiftTabActionToUse)
	{
		enterAction = enterActionToUse;
		spaceAction = spaceActionToUse;
		tabAction = tabActionToUse;
		shiftTabAction = shiftTabActionToUse;
	}
	
	private void reBindKeysForFocusableComponents()
	{
		for(int i = 0; i < getFocusableComponents().length; ++i)
		{
			JComponent component = getFocusableComponents()[i];
			UiEditableGrid.bindKeyToAction(component, KeyEvent.VK_ENTER, UiEditableGrid.NO_MODIFIERS, enterAction);
			UiEditableGrid.bindKeyToAction(component, KeyEvent.VK_SPACE, UiEditableGrid.NO_MODIFIERS, spaceAction);
			UiEditableGrid.bindKeyToAction(component, KeyEvent.VK_TAB, UiEditableGrid.NO_MODIFIERS, tabAction);
			UiEditableGrid.bindKeyToAction(component, KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK, shiftTabAction);
		}
	}

	private Box container;
	private Vector comboBoxes;
	private ListOfReusableChoicesLists choiceLists;
	private boolean isUpdateInProgress;

	private EnterAction enterAction;
	private SpaceAction spaceAction;
	private TabAction tabAction;
	private ShiftTabAction shiftTabAction;
	private UiFontEncodingHelper fontHelper;
}

