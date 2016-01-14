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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.grids.GridCellEditorAndRenderer;
import org.martus.client.swingui.grids.GridTableModel;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.Utilities;



public abstract class UiEditableGrid extends UiGrid implements FocusListener
{
	public UiEditableGrid(UiMainWindow mainWindowToUse, GridFieldSpec fieldSpec, UiDialogLauncher dlgLauncher, UiFieldContext context, int maxGridCharacters)
	{
		super(mainWindowToUse, fieldSpec, dlgLauncher, context, new UiEditableFieldCreator(mainWindowToUse, context));
		initialize(maxGridCharacters);
	}
	
	protected UiEditableGrid(UiMainWindow mainWindowToUse, UiFieldContext contextToUse, GridTableModel modelToUse, UiDialogLauncher dlgLauncher, int maxGridCharacters)
	{
		super(mainWindowToUse, modelToUse, dlgLauncher, contextToUse, new UiEditableFieldCreator(mainWindowToUse, new UiFieldContext()));
		initialize(maxGridCharacters);
	}
	
	private void initialize(int maxGridCharacters)
	{
		addInsertAndDeleteButtons();

		table.setMaxGridWidth(maxGridCharacters);
		updateVisibleRowCount();

		bindKeys();
		addFocusListener(this);
	}
	
	void addInsertAndDeleteButtons() 
	{
		Vector buttonsToAdd = createButtons();
		setButtons(buttonsToAdd);
	}

	protected Vector createButtons() 
	{
		MiniLocalization localization = table.getDialogLauncher().getLocalization();

		Vector buttons = new Vector();
		
		UiButton deleteRow = new UiButton(localization.getButtonLabel("DeleteSelectedGridRow"));
		deleteRow.addActionListener(new DeleteRowListener(table.getDialogLauncher()));
		buttons.add(deleteRow);
		
		insertRow = new UiButton(localization.getButtonLabel("InsertEmptyGridRow"));
		insertRow.addActionListener(new InsertRowListener(table.getDialogLauncher()));
		buttons.add(insertRow);

		return buttons;
	}

	public UiButton getInsertButton()
	{
		return insertRow;
	}


	public JComponent[] getFocusableComponents()
	{
		return table.getFocusableComponents();
	}

	public void setText(String newText)
	{
		super.setText(newText);
		updateVisibleRowCount();
	}

	public void focusGained(FocusEvent arg0)
	{
	}

	public void focusLost(FocusEvent event)
	{
		// if we validate on a temporary focus loss, we end up here twice
		// because popping up a dialog temporarily loses our focus
		if(event.isTemporary())
			return;
		
		stopCellEditing();
	}
	
	void ensureSelectionIsValid()
	{
		// this little hack is necessary because even though the grid constructor 
		// sets the selection to (0,1), when the user first tabs into it, there 
		// is nothing selected according to swing
		if(table.getSelectedRow() < 0 || table.getSelectedColumn() < 0)
			moveSelectionTo(0, 1);
	}

	public void stopCellEditing()
	{
		super.stopCellEditing();
		TableCellEditor cellEditor = table.getCellEditor();
		if(cellEditor == null)
			return;
		cellEditor.stopCellEditing();
	}
	
	boolean inFirstRow()
	{
		return table.getSelectedRow() <= 0;
	}

	boolean inLastRow()
	{
		return table.getSelectedRow() >= getLastRowIndex();
	}

	boolean inFirstColumn()
	{
		return table.getSelectedColumn() <= 1;
	}

	boolean inLastColumn()
	{
		return table.getSelectedColumn() >= getLastColumnIndex();
	}

	int getLastColumnIndex()
	{
		return table.getColumnCount()-1;
	}

	int getLastRowIndex()
	{
		return table.getRowCount()-1;
	}
	
	void moveSelectionTo(int row, int column)
	{
		if(row < 0)
			row = 0;
		if(column < 1)
			column = 1;
		stopCellEditing();
		table.changeSelection(row, column, false, false);
	}

	void bindKeys()
	{
		bindKeysForComponent(table);
		JComponent[] subComponents = table.getFocusableComponents();
		for(int i=0; i < subComponents.length; ++i)
		{
			bindKeysForComponent(subComponents[i]);
		}
		
		Iterator iter = table.getAllEditors().iterator();
		while(iter.hasNext())
		{
			GridCellEditorAndRenderer editor = (GridCellEditorAndRenderer) iter.next();
			editor.setActions(new EnterAction(), new SpaceAction(), new TabAction(), new ShiftTabAction());
		}
	}
	
	void bindKeysForComponent(JComponent component)
	{
		bindKeyToAction(component, KeyEvent.VK_ENTER, NO_MODIFIERS, new EnterAction());
		bindKeyToAction(component, KeyEvent.VK_SPACE, NO_MODIFIERS, new SpaceAction());
		bindKeyToAction(component, KeyEvent.VK_TAB, NO_MODIFIERS, new TabAction());
		bindKeyToAction(component, KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK, new ShiftTabAction());
	}

	public static void bindKeyToAction(JComponent component, int key, int modifiers, ActionWithName action)
	{
		if(action == null)
			return;
		
		component.getInputMap().put(KeyStroke.getKeyStroke(key, modifiers), action.getName());
		component.getActionMap().put(action.getName(), action);
	}
	
	public void insertRow() throws ArrayIndexOutOfBoundsException
	{
		stopCellEditing();
		model.insertEmptyRow(table.getSelectedRow());
		updateVisibleRowCount();
	}

	public void deleteSelectedRow() throws ArrayIndexOutOfBoundsException
	{
		stopCellEditing();
		model.deleteSelectedRow(table.getSelectedRow());
		updateVisibleRowCount();
	}

	abstract class ActionWithName extends AbstractAction
	{
		abstract String getName();
	}
	
	void addButtonsBelowExpandedGrid(JPanel fakeTable) 
	{
		Utilities.addComponentsRespectingOrientation(fakeTable, new Component[] {new UiLabel(" "), new AppendRowButton()});
	}

	class AppendRowButton extends UiButton implements ActionListener
	{
		public AppendRowButton()
		{
			super(getLocalization().getButtonLabel("AppendEmptyGridRow"));
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) 
		{
			getGridTableModel().addEmptyRow();
			showExpanded();
			widget.getTopLevelAncestor().validate();
		}
	}
	

	
	public class EnterAction extends ActionWithName
	{
		public String getName()
		{
			return "EnterAction";
		}
		
		public void actionPerformed(ActionEvent arg0)
		{
			stopCellEditing();
			model.addEmptyRow();
			table.changeSelection(getLastRowIndex(),0,false,false);
			updateVisibleRowCount();
		}
	}
	
	public class SpaceAction extends ActionWithName
	{
		String getName()
		{
			return "SpaceAction";
		}

		public void actionPerformed(ActionEvent arg0)
		{
			if(!table.isEditing())
			{
				//System.out.println("UiGridEditor.actionPerformed: begin editing");
				ensureSelectionIsValid();
				int row = table.getSelectedRow();
				int column = table.getSelectedColumn();
				table.editCellAt(row, column);
		
			}

			GridCellEditorAndRenderer editor = (GridCellEditorAndRenderer)table.getCellEditor();
			if(editor == null)
			{
				//System.out.println("UiGridEditor.actionPerformed: still no editor!");
				return;
			}
			
			editor.spaceWasPressed();
		}
	}

	public class TabAction extends ActionWithName
	{
		String getName()
		{
			return "TabAction";
		}

		public void actionPerformed(ActionEvent arg0)
		{
			ensureSelectionIsValid();
			if(inLastColumn())
			{
				if(inLastRow())
				{
					table.transferFocus();
				}
				else
				{
					moveSelectionTo(table.getSelectedRow() + 1, 0);
				}
			}
			else
			{
				moveSelectionTo(table.getSelectedRow(), table.getSelectedColumn() + 1);
			}
		}
	}

	public class ShiftTabAction extends ActionWithName
	{
		String getName()
		{
			return "ShiftTabAction";
		}

		public void actionPerformed(ActionEvent arg0)
		{
			ensureSelectionIsValid();
			if(inFirstColumn())
			{
				if(inFirstRow())
				{
					table.transferFocusBackward();
				}
				else
				{
					moveSelectionTo(table.getSelectedRow() - 1, getLastColumnIndex());
				}
			}
			else
			{
				moveSelectionTo(table.getSelectedRow(), table.getSelectedColumn() - 1);
			}
		}
	}

	private class DeleteRowListener implements ActionListener
	{
		DeleteRowListener(UiDialogLauncher dlgLauncherToUse)
		{
			dlgLauncher = dlgLauncherToUse;
		}

		public void actionPerformed(ActionEvent e)
		{
			if(!isRowSelected())
			{
				dlgLauncher.ShowNotifyDialog("NoGridRowSelected");
				return;
			}

			try 
			{
				deleteSelectedRow();
			} 
			catch (ArrayIndexOutOfBoundsException e1) 
			{
				e1.printStackTrace();
			}
		}		
		UiDialogLauncher dlgLauncher;
	}
	
	private class InsertRowListener implements ActionListener
	{
		InsertRowListener(UiDialogLauncher dlgLauncherToUse)
		{
			dlgLauncher = dlgLauncherToUse;
		}

		public void actionPerformed(ActionEvent e)
		{
			if(!isRowSelected())
			{
				dlgLauncher.ShowNotifyDialog("NoGridRowSelected");
				return;
			}

			try 
			{
				insertRow();
			} 
			catch (ArrayIndexOutOfBoundsException e1) 
			{
				e1.printStackTrace();
			}
		}		
		UiDialogLauncher dlgLauncher;
	}
	
	public final static int NO_MODIFIERS = 0;
	
	private UiButton insertRow;

}
