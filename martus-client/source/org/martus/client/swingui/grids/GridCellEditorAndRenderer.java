/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.client.swingui.grids;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.martus.client.swingui.fields.UiField;
import org.martus.client.swingui.fields.UiEditableGrid.EnterAction;
import org.martus.client.swingui.fields.UiEditableGrid.ShiftTabAction;
import org.martus.client.swingui.fields.UiEditableGrid.SpaceAction;
import org.martus.client.swingui.fields.UiEditableGrid.TabAction;

public class GridCellEditorAndRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, FocusListener
{
	GridCellEditorAndRenderer(UiField widgetToWrap)
	{
		
		uiField = widgetToWrap;
		uiField.addFocusListener(this);
		borderWithoutFocus = new EmptyBorder(1,1,1,1);
		borderWithFocus = new LineBorder(Color.BLACK,1);
	}

	public Component getTableCellEditorComponent(JTable tableToUse, Object stringValue, boolean isSelected, int row, int column)
	{
		getUiField().setText((String)stringValue);
		JComponent component = getComponent();
		
		component.setBorder(borderWithFocus);
		return component;
	}
	
	public int getMinimumCellWidth()
	{
		return uiField.getComponent().getPreferredSize().width;
	}
	
	public void spaceWasPressed()
	{
	}
	
	public UiField getUiField()
	{
		return uiField;
	}

	public JComponent getComponent()
	{
		return getUiField().getComponent();
	}

	public JComponent[] getFocusableComponents()
	{
		return getUiField().getFocusableComponents();
	}

	public Object getCellEditorValue()
	{
		return getUiField().getText();
	}

	public Component getTableCellRendererComponent(JTable tableToUse, Object stringValue, boolean isSelected, boolean hasFocus, int row, int column)
	{
		getUiField().setText((String)stringValue);
		JComponent component = getComponent();
		Border border = borderWithoutFocus;
		if(hasFocus)
			border = borderWithFocus;
		component.setBorder(border);
		
		return component;
	}
	
	public void updateSpellChecker(String bulletinLanguageCode)
	{
		uiField.updateSpellChecker(bulletinLanguageCode);
	}

	public void setActions(EnterAction enterActionToUse, SpaceAction spaceActionToUse, TabAction tabActionToUse, ShiftTabAction shiftTabActionToUse)
	{
		getUiField().setActions(enterActionToUse, spaceActionToUse, tabActionToUse, shiftTabActionToUse);
	}
	
	public void focusGained(FocusEvent arg0)
	{
	}

	// NOTE: I would have expected Swing to do this automatically, but without 
	// this, clicking in a grid cell, making changes, and then directly clicking 
	// in some other field, or on the Save As Draft button causes the edits
	// to be discarded. See also UiTableWithCellEditingProtection for a 
	// related case with a different solution
	public void focusLost(FocusEvent arg0)
	{
		//System.out.println("GridCellEditorAndRenderer focusLost, so calling stopCellEditing");
		stopCellEditing();
	}
	
	final int INSETS = 4;

	UiField uiField;
	Border borderWithFocus;
	Border borderWithoutFocus;
}
