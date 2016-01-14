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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.martus.client.core.MartusApp;
import org.martus.client.search.SaneCollator;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.grids.GridTable;
import org.martus.client.swingui.grids.GridTableModel;
import org.martus.common.GridData;
import org.martus.common.ListOfReusableChoicesLists;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.utilities.BurmeseUtilities;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.Utilities;

import com.jhlabs.awt.Alignment;
import com.jhlabs.awt.GridLayoutPlus;


abstract public class UiGrid extends UiField
{
	public UiGrid(UiMainWindow mainWindowToUse, GridFieldSpec fieldSpec, UiDialogLauncher dlgLauncher, UiFieldContext context, UiFieldCreator fieldCreatorToUse)
	{
		this(mainWindowToUse, new GridTableModel(fieldSpec, context.getReusableChoicesLists()), dlgLauncher, context, fieldCreatorToUse);
	}
	
	public UiGrid(UiMainWindow mainWindowToUse, GridTableModel modelToUse, UiDialogLauncher dlgLauncher, UiFieldContext contextToUse, UiFieldCreator fieldCreatorToUse)
	{
		super(mainWindowToUse.getLocalization());
		app = mainWindowToUse.getApp();
		model = modelToUse;
		context = contextToUse;
		fieldCreator = fieldCreatorToUse;
		fontHelper = new UiFontEncodingHelper(mainWindowToUse.getDoZawgyiConversion());
		
		table = createGridTable(dlgLauncher, context);
		table.setColumnSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setShowGrid(true);
		table.changeSelection(0, 1, false, false);
		table.setRowHeight(table.getRowHeight() + ROW_HEIGHT_PADDING);
		widget = new JPanel();
		widget.setLayout(new BorderLayout());

		buttonBox = Box.createHorizontalBox();
		buttonBox.setBorder(new EmptyBorder(10,0,0,0));
		setButtons(createButtons());
		rebuildWidget();
	}

	abstract protected GridTable createGridTable(UiDialogLauncher dlgLauncher, UiFieldContext contextToUse);
	
	public void updateDataDrivenColumnWidth(int column, ListOfReusableChoicesLists choices)
	{
		table.updateDataDrivenColumnWidth(column, choices);
	}
	
	public UiFieldContext getContext()
	{
		return context;
	}

	String getGridTag()
	{
		return model.getGridData().getSpec().getTag();
	} 
	
	protected Vector createButtons()
	{
		return new Vector();
	}
	
	protected UiButton createShowExpandedButton()
	{
		UiButton expand = new UiButton(getLocalization().getButtonLabel("ShowGridExpanded"));
		expand.addActionListener(new ExpandButtonHandler());
		return expand;
	}
	
	class ExpandButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			app.setGridExpansionState(getGridTag(), true);
			showExpanded();
			widget.getTopLevelAncestor().validate();
		}
		
	}
	
	class TwoColumnGridLayout extends GridLayoutPlus
	{
		public TwoColumnGridLayout()
		{
			super(0, 2);
			setFill(Alignment.FILL_NONE);
			
			// right-align the left column and left-align the right column
			setColAlignment(0, Alignment.NORTHEAST);
			setColAlignment(1, Alignment.NORTHWEST);
		}
	}
	
	public void dataDrivenDropdownInsideGridMayNeedToBeUpdated()
	{
		// if we are not expanded, the table will automatically refresh 
		// when the underlying model is changed
		if(expandedFieldRows == null)
			return;

		for(int row = 0; row < expandedFieldRows.size(); ++row)
		{
			UiField[] fields = (UiField[]) expandedFieldRows.get(row);
			for(int column = FIRST_REAL_FIELD_COLUMN; column < model.getColumnCount(); ++column)
			{
				FieldSpec spec = model.getFieldSpecForCell(row, column);
				if(spec.getType().isDropdown())
				{
					UiChoice choice = (UiChoice)fields[column];
					choice.setSpec(context, (DropDownFieldSpec) spec);
				}
			}
		}
	}
	
	void showExpanded()
	{
		stopCellEditing();
		widget.removeAll();
		
		expandedFieldRows = new Vector();
		JPanel fakeTable = new JPanel(new TwoColumnGridLayout());
		for(int row = 0; row < model.getRowCount(); ++row)
		{
			UiField[] rowFields = new UiField[model.getColumnCount()];
			expandedFieldRows.add(rowFields);
			
			JPanel rowPanel = new JPanel(new TwoColumnGridLayout());
			for(int column = FIRST_REAL_FIELD_COLUMN; column < model.getColumnCount(); ++column)
			{
				FieldSpec spec = model.getFieldSpecForCell(row, column);

				UiField cellField = fieldCreator.createField(spec);
				
				for(int component = 0; component < cellField.getFocusableComponents().length; ++component)
					cellField.getFocusableComponents()[component].addFocusListener(new ExpandedGridFieldFocusHandler());
				
				String value = (String)model.getValueAt(row, column);
				cellField.setText(value);
				JComponent cellComponent = cellField.getComponent();
				cellComponent.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				
				// NOTE: This little hack avoided a last-minute problem where Dates inside expanded grids
				// were being sized wrong, which caused big problems in Arabic.
				// the long-term solution is probably to have DateEditor have a panel that 
				// contains the DateEditorComponent, like DateRange already does, which works
				if(spec.getType().isDate())
				{
					JPanel wrapper = new JPanel(new BorderLayout());
					wrapper.add(cellComponent, BorderLayout.CENTER);
					cellComponent = wrapper;
				}
				String columnName = model.getColumnName(column);
				Component[] rowComponents = new Component[] {
						new UiLabel(columnName),
						cellComponent,
						};
				Utilities.addComponentsRespectingOrientation(rowPanel, rowComponents);
				rowFields[column] = cellField;
			}
			Utilities.addComponentsRespectingOrientation(fakeTable, new Component[] {new UiLabel(model.getColumnName(0) + Integer.toString(row+1)), rowPanel});
			insertBlankLineOfTwoColumns(fakeTable);
		}
		addButtonsBelowExpandedGrid(fakeTable);
		
		widget.add(fakeTable);
		UiButton showCollapsedButton = new UiButton(getLocalization().getButtonLabel("ShowGridNormal"));
		showCollapsedButton.addActionListener(new CollapseButtonHandler());
		Box box = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(box, new Component[] {showCollapsedButton, Box.createHorizontalGlue()});
		widget.add(box, BorderLayout.BEFORE_FIRST_LINE);
		
		updateSpellChecker(getContext().getCurrentBulletinLanguage());
	}

	private void insertBlankLineOfTwoColumns(JPanel fakeTable)
	{
		Utilities.addComponentsRespectingOrientation(fakeTable, new Component[] {new UiLabel(" "), new UiLabel(" ")});
	}

	void addButtonsBelowExpandedGrid(JPanel fakeTable) 
	{
		// NOTE: This method should be overridden where appropriate
	}

	class ExpandedGridFieldFocusHandler implements FocusListener
	{
		public void focusGained(FocusEvent event) 
		{
		}

		public void focusLost(FocusEvent event) 
		{
			copyExpandedFieldsToTableModel();
		}
		
	}
	
	class CollapseButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			app.setGridExpansionState(getGridTag(), false);
			showCollapsed();
			widget.getTopLevelAncestor().validate();
		}
		
	}
	
	void showCollapsed()
	{
		widget.removeAll();
		copyExpandedFieldsToTableModel();
		updateVisibleRowCount();
		expandedFieldRows = null;
		widget.add(buttonBox, BorderLayout.SOUTH);
		UiScrollPane tableScroller = new UiScrollPane(table);
		tableScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		widget.add(tableScroller, BorderLayout.CENTER);
		
		updateSpellChecker(getContext().getCurrentBulletinLanguage());
	}
	
	void copyExpandedFieldsToTableModel()
	{
		if(expandedFieldRows == null)
			return;
		
		for(int row = 0; row < expandedFieldRows.size(); ++row)
		{
			UiField[] rowFields = (UiField[])expandedFieldRows.get(row); 
			for(int column = FIRST_REAL_FIELD_COLUMN; column < rowFields.length; ++column)
			{
				String value = rowFields[column].getText();
				model.setValueAt(value, row, column);
			}
		}
	}

	void updateVisibleRowCount()
	{
		int rows = table.getRowCount();
		if(rows < MINIMUM_VISIBLE_ROWS)
			rows = MINIMUM_VISIBLE_ROWS;
		if(rows > MAXIMUM_VISIBLE_ROWS)
			rows = MAXIMUM_VISIBLE_ROWS;
		table.resizeTable(rows);
		Container topLevelAncestor = table.getTopLevelAncestor();
		if(topLevelAncestor != null)
			topLevelAncestor.validate();
	}

	protected void setButtons(Vector buttons) 
	{
		buttonBox.removeAll();
		buttons.add(Box.createHorizontalGlue());
		Component[] buttonsAsArray = (Component[])buttons.toArray(new Component[0]);
		Utilities.addComponentsRespectingOrientation(buttonBox, buttonsAsArray);
	}	
	
	public JComponent getComponent()
	{
		return widget;
	}
	
	public String getText()
	{
		copyExpandedFieldsToTableModel();
		return model.getXmlRepresentation();
	}

	public void setText(String newText)
	{
		try
		{
			model.setFromXml(newText);
			rebuildWidget();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void rebuildWidget() 
	{
		if(app.isGridExpanded(getGridTag()))
			showExpanded();
		else
			showCollapsed();
	}

	public boolean isRowSelected()
	{
		return (table.getSelectedRow() != NO_ROW_SELECTED);
	}
	
	protected void stopCellEditing() 
	{
	}
	
	public GridTableModel getGridTableModel()
	{
		return model;
	}

	public GridData getGridData()
	{
		return model.getGridData();
	}
	
	public GridTable getTable()
	{
		return table;
	}
	
	public ChoiceItem[] buildChoicesFromColumnValues(String gridColumnLabel) {
		gridColumnLabel = fontHelper.getDisplayable(gridColumnLabel);
		int gridColumn = model.findColumn(gridColumnLabel);
		FieldSpec columnSpec = model.getFieldSpecForColumn(gridColumn);
	
		HashSet existingValues = new HashSet();
		Vector values = new Vector();
		values.add(new ChoiceItem("", ""));
		existingValues.add("");
		for(int row = 0; row < model.getRowCount(); ++row)
		{
			String thisValue = (String)model.getValueAt(row, gridColumn);
			thisValue = BurmeseUtilities.getStorable(thisValue);
			if(existingValues.contains(thisValue))
				continue;
	
			String formattedValue = FieldDataFormatter.formatData(columnSpec, thisValue, getLocalization());
			
			values.add(new ChoiceItem(thisValue, formattedValue));
			existingValues.add(thisValue);
		}
		
		Collections.sort(values, new SaneCollator(getLocalization().getCurrentLanguageCode()));
		
		return (ChoiceItem[])values.toArray(new ChoiceItem[0]);
	}

	private static final int NO_ROW_SELECTED = -1;
	private static final int ROW_HEIGHT_PADDING = 10;
	int FIRST_REAL_FIELD_COLUMN = 1;
	
	private static final int MINIMUM_VISIBLE_ROWS = 1;
	private static final int MAXIMUM_VISIBLE_ROWS = 5;

	MartusApp app;
	UiFieldCreator fieldCreator;
	private UiFieldContext context;
	JPanel widget;
	Box buttonBox;
	protected GridTable table;
	protected GridTableModel model;
	Vector expandedFieldRows;
	UiFontEncodingHelper fontHelper;
}

