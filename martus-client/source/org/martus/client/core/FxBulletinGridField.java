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
package org.martus.client.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import javafx.collections.ListChangeListener;

import org.martus.client.search.SaneCollator;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.client.swingui.jfx.landing.bulletins.GridRowFields;
import org.martus.common.GridData;
import org.martus.common.GridRow;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;

public class FxBulletinGridField extends FxBulletinField
{
	protected FxBulletinGridField(FxBulletin bulletinToUse, GridFieldSpec fieldSpecToUse, MiniLocalization localizationToUse)
	{
		super(bulletinToUse, fieldSpecToUse, localizationToUse);

		gridDataIfApplicable = new GridFieldData();
		ListChangeListener<GridRowFields> rowChangeHandler = (change) -> updateOverallValue();
		gridDataIfApplicable.addListener(rowChangeHandler);
		gridColumnValuesMap = new HashMap<String, FxBulletinGridField.ColumnValues>();
	}
	
	@Override
	public boolean isGrid()
	{
		return true;
	}

	public GridFieldSpec getGridFieldSpec()
	{
		return (GridFieldSpec) getFieldSpec();
	}

	public GridFieldData gridDataProperty()
	{
		if(!isGrid())
			throw new RuntimeException("gridDataProperty not available for non-grid: " + getTag());
	
		return gridDataIfApplicable;
	}

	public String getValue()
	{
		updateOverallValue();
		
		return valueProperty().getValue();
	}

	public void setValue(String value)
	{
		setGridData(value);
		
		valueProperty().setValue(value);
	}

	public GridRowFields appendEmptyGridRow()
	{
		if(!isGrid())
			throw new RuntimeException("Cannot append rows to non-grid field: " + getTag());
		
		GridRowFields newRowFields = createEmptyRow();
		gridDataProperty().add(newRowFields);
		return newRowFields;
	}

	public void removeGridRow(GridRowFields rowToRemove)
	{
		GridFieldData gridData = gridDataProperty();
		if(!gridData.contains(rowToRemove))
			throw new RuntimeException("Attempted to remove grid row that didn't exist");
		
		gridData.remove(rowToRemove);
	}

	public Vector<ObservableChoiceItemList> gridColumnValuesProperty(String columnLabel)
	{
		if(!isGrid())
			throw new RuntimeException("gridColumnValues not available for non-grid: " + getTag());
		
		GridFieldSpec gridSpec = getGridFieldSpec();
		FieldSpec columnSpec = gridSpec.findColumnSpecByLabel(columnLabel);
		if(columnSpec == null)
			throw new RuntimeException("attempt to get values from non-existent grid column: " + getTag() + " . " + columnLabel);
	
		FxBulletinGridField.ColumnValues columnValues = gridColumnValuesMap.get(columnLabel);
		if(columnValues == null)
		{
			columnValues = new FxBulletinGridField.ColumnValues(this, columnLabel, getLocalization());
			gridColumnValuesMap.put(columnLabel, columnValues);
		}
		
		Vector<ObservableChoiceItemList> listOfLists = new Vector<ObservableChoiceItemList>();
		listOfLists.add(columnValues);
		return listOfLists;
	}

	private void updateOverallValue()
	{
		String newValue = getGridValue();
		valueProperty().setValue(newValue);
	}

	private void setGridData(String xmlGridData)
	{
		gridDataProperty().clear();
		
		GridFieldSpec gridSpec = getGridFieldSpec();
		PoolOfReusableChoicesLists poolOfReusableChoicesLists = getBulletin().getAllReusableChoicesLists();
		GridData data = new GridData(gridSpec, poolOfReusableChoicesLists );
		
		try
		{
			data.setFromXml(xmlGridData);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		
		for(int row = 0; row < data.getRowCount(); ++row)
		{
			GridRow gridRow = data.getRow(row);
			GridRowFields rowFields = appendEmptyGridRow();
			copyGridRowToGridRowFields(gridRow, rowFields);
		}
	}

	private String getGridValue()
	{
		PoolOfReusableChoicesLists irrelevantReusableLists = null;
		GridFieldSpec gridSpec = getGridFieldSpec();
		GridData gridData = new GridData(gridSpec, irrelevantReusableLists);
		for(int row = 0; row < gridDataProperty().size(); ++ row)
		{
			GridRowFields rowFields = gridDataProperty().get(row);
			GridRow gridRow = convertGridRowFieldsToGridRow(gridSpec, rowFields);
			if(!gridRow.isEmptyRow())
				gridData.addRow(gridRow);
		}
	
		return gridData.getXmlRepresentation();
	}

	private GridRowFields createEmptyRow()
	{
		GridFieldSpec gridFieldSpec = getGridFieldSpec();
		GridRowFields rowFields = new GridRowFields();
		for(int column = 0; column < gridFieldSpec.getColumnCount(); ++column)
		{
			String columnLabel = gridFieldSpec.getColumnLabel(column);
			FieldSpec cellSpec = gridFieldSpec.getFieldSpec(column);
			FxBulletinField cellField = createFxBulletinField(getBulletin(), cellSpec, getLocalization());
			rowFields.put(columnLabel, cellField);
			
			cellField.addValueListener((observable, oldValue, newValue) -> updateOverallValue());
		}
	
		GridRow gridRow = GridRow.createEmptyRow(getGridFieldSpec(), PoolOfReusableChoicesLists.EMPTY_POOL);
		copyGridRowToGridRowFields(gridRow, rowFields);
		return rowFields;
	}

	private void copyGridRowToGridRowFields(GridRow gridRow, GridRowFields rowFields)
	{
		for(int column = 0; column < gridRow.getColumnCount(); ++column)
		{
			String columnLabel = getGridFieldSpec().getColumnLabel(column);
			FxBulletinField cellField = rowFields.get(columnLabel);
			String value = gridRow.getCellText(column);
			cellField.setValue(value);
		}
	}

	private static class ColumnValues extends ObservableChoiceItemList
	{
		public ColumnValues(FxBulletinGridField gridField, String gridColumnLabel, MiniLocalization localizationToUse)
		{
			field = gridField;
			columnLabel = gridColumnLabel;
			localization = localizationToUse;
			
			updateChoices();
			gridField.valueProperty().addListener((observable, oldValue, newValue) -> updateChoices());
		}
		
		private void updateChoices()
		{
			HashSet<ChoiceItem> newChoices = new HashSet<ChoiceItem>(); 
			ChoiceItem alwaysIncludeEmpty = new ChoiceItem("", "");
			newChoices.add(alwaysIncludeEmpty);
			GridFieldData gridFieldData = field.gridDataProperty();
			for(int row = 0; row < gridFieldData.size(); ++row)
			{
				GridRowFields rowFields = gridFieldData.get(row);
				ChoiceItem thisChoice = createChoice(rowFields);
				ChoiceItem existing = findByCode(thisChoice.getCode());
				if(existing != null)
					thisChoice = existing;
				newChoices.add(thisChoice);
			}
	
			// NOTE: If we clear() or sort() the list, any dddd 
			// will be cleared because the item the point to will 
			// no longer be a legal choice (even for a very short time)
			updateChoicesInPlace(newChoices);
		}
	
		public void updateChoicesInPlace(HashSet<ChoiceItem> newChoices)
		{
			HashSet<ChoiceItem> oldChoices = new HashSet<ChoiceItem>(this);
			for (ChoiceItem oldChoice : oldChoices)
			{
				if(!newChoices.contains(oldChoice)) 
					remove(oldChoice);
			}
			for (ChoiceItem existingChoice : this)
			{
				if(newChoices.contains(existingChoice)) 
					newChoices.remove(existingChoice);
			}
	
			if(newChoices.size() == 0)
				return;
		
			insertNewItemsSortedWithoutCallingSort(newChoices);
	
		}
	
		public void insertNewItemsSortedWithoutCallingSort(HashSet<ChoiceItem> unsortedNewChoices)
		{
			ObservableChoiceItemList destination = this;
			ObservableChoiceItemList sortedNewChoices = new ObservableChoiceItemList();
			sortedNewChoices.addAll(unsortedNewChoices);
			sortedNewChoices.sort(new SaneCollator(localization.getCurrentLanguageCode()));
			
			int from = 0;
			int to = 0;
			while(to <= destination.size())
			{
				while(from < sortedNewChoices.size())
				{
					boolean shouldInsertHere = (to >= destination.size());
					ChoiceItem candidate = sortedNewChoices.get(from);
					
					if(!shouldInsertHere)
					{
						ChoiceItem existing = destination.get(to);
						if(candidate.compareTo(existing) < 0)
							shouldInsertHere = true;
					}
					
					if(!shouldInsertHere)
						break;
					
					destination.add(to, candidate);
					++from;
				}
				++to;
			}
		}
	
		private ChoiceItem createChoice(GridRowFields gridRowFields)
		{
			String value = gridRowFields.get(columnLabel).getValue();
			ChoiceItem choice = new ChoiceItem(value, value);
			return choice;
		}
	
		private FxBulletinGridField field;
		private String columnLabel;
		private MiniLocalization localization;
	}

	private GridFieldData gridDataIfApplicable;
	private Map<String, FxBulletinGridField.ColumnValues> gridColumnValuesMap;
}
