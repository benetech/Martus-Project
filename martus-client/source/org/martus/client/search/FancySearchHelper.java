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

package org.martus.client.search;

import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.clientside.UiLocalization;
import org.martus.common.GridData;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeAnyField;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.common.fieldspec.GridFieldSpec.UnsupportedFieldTypeException;
import org.martus.swing.FontHandler;

public class FancySearchHelper
{
	public FancySearchHelper(ClientBulletinStore storeToUse, UiDialogLauncher dlgLauncherToUse)
	{
		dlgLauncher = dlgLauncherToUse;
		model = new FancySearchTableModel(getGridSpec(storeToUse), storeToUse.getAllReusableChoiceLists(), dlgLauncherToUse.getLocalization());
	}
	
	UiLocalization getLocalization()
	{
		return dlgLauncher.getLocalization();
	}
	
	UiDialogLauncher getDialogLauncher()
	{
		return dlgLauncher;
	}
	
	FancySearchTableModel getModel()
	{
		return model;
	}
	
	public GridFieldSpec getGridSpec(ClientBulletinStore storeToUse)
	{
		GridFieldSpec spec = new GridFieldSpec();

		try
		{
			FieldChooserSpecBuilder builder = new SearchFieldChooserSpecBuilder(getLocalization());
			spec.addColumn(builder.createSpec(storeToUse));
			
			spec.addColumn(FancySearchTableModel.getCurrentOpColumnSpec(new FieldTypeAnyField(), getLocalization()));
			
			String valueColumnTag = "value";
			String valueColumnHeader = getLocalization().getFieldLabel("SearchGridHeaderValue");
			spec.addColumn(FieldSpec.createCustomField(valueColumnTag, valueColumnHeader, new FieldTypeNormal()));
			spec.addColumn(createAndOrColumnSpec());
		}
		catch (UnsupportedFieldTypeException e)
		{
			// TODO: better error handling?
			e.printStackTrace();
			throw new RuntimeException();
		}
		return spec;
	}
	
	public DropDownFieldSpec createAndOrColumnSpec()
	{
		ChoiceItem[] choices =
		{
			createLocalizedChoiceItem(SearchParser.ENGLISH_AND_KEYWORD),
			createLocalizedChoiceItem(SearchParser.ENGLISH_OR_KEYWORD),
		};
		return new DropDownFieldSpec(choices);
	}
	
	private ChoiceItem createLocalizedChoiceItem(String tag)
	{
		String translated = getLocalization().getKeyword(tag);
		String storableSinceThisIsInADropdown = new UiFontEncodingHelper(FontHandler.isDoZawgyiConversion()).getStorable(translated);
		return new ChoiceItem(tag, storableSinceThisIsInADropdown);
	}
	
	public void setSearchFromJson(GridData gridData, JSONObject json)
	{
		gridData.clear();
		recreateSavedRows(gridData, json);
		if(gridData.getRowCount() == 0)
			gridData.addEmptyRow();
		model.fireTableDataChanged();
	}

	private void recreateSavedRows(GridData gridData, JSONObject json)
	{
		if(!json.has(TAG_ROWS))
			return;
		
		JSONArray rows = json.getJSONArray(TAG_ROWS);
		for(int i = 0; i < rows.length(); ++i)
		{
			JSONObject row = rows.getJSONObject(i);
			JSONObject jsonMiniSpec = row.getJSONObject(TAG_FIELD_TO_SEARCH);
			String miniSpecAsJsonString = convertSearchFieldFromJsonToGridValue(jsonMiniSpec);
			gridData.addEmptyRow();
			if(doesFieldExist(gridData, miniSpecAsJsonString))
			{
				gridData.setValueAt(miniSpecAsJsonString, i, 0);
				gridData.setValueAt(row.getString(TAG_COMPARE_HOW), i, 1);
				String lookFor = row.getString(TAG_LOOK_FOR);
				gridData.setValueAt(lookFor, i, 2);
				gridData.setValueAt(row.getString(TAG_AND_OR), i, 3);
			}
		}
	}

	private boolean doesFieldExist(GridData gridData, String miniSpecAsJsonString)
	{
		PopUpTreeFieldSpec fieldColumnSpec = (PopUpTreeFieldSpec)gridData.getSpec().getFieldSpec(0);
		return (fieldColumnSpec.findCode(miniSpecAsJsonString) != null);
	}
	
	public JSONObject getSearchAsJson(GridData gridData) throws Exception
	{
		JSONObject json = new JSONObject();
		JSONArray rows = new JSONArray();
		for(int i = 0; i < gridData.getRowCount(); ++i)
		{
			String value = gridData.getValueAt(i, 0);
			JSONObject jsonMiniSpec = convertSearchFieldFromGridValueToJson(value);
			
			JSONObject row = new JSONObject();
			row.put(TAG_FIELD_TO_SEARCH, jsonMiniSpec);
			row.put(TAG_COMPARE_HOW, gridData.getValueAt(i, 1));
			row.put(TAG_LOOK_FOR, gridData.getValueAt(i, 2));
			row.put(TAG_AND_OR, gridData.getValueAt(i, 3));
			rows.put(row);
		}
		json.put(TAG_ROWS, rows);
		return json;
	}

	private String convertSearchFieldFromJsonToGridValue(JSONObject jsonMiniSpec)
	{
		String miniSpecAsJsonString = "";
		if(jsonMiniSpec.length() > 0)
		{
				MiniFieldSpec miniSpec = new MiniFieldSpec(jsonMiniSpec);
				miniSpecAsJsonString = miniSpec.toJson().toString();
		}
		return miniSpecAsJsonString;
	}

	private JSONObject convertSearchFieldFromGridValueToJson(String value) throws ParseException
	{
		JSONObject jsonMiniSpec = new JSONObject();
		if(value.length() > 0)
		{
			jsonMiniSpec = new JSONObject(value);
		}
		return jsonMiniSpec;
	}

	public SearchTreeNode getSearchTree(GridData gridData)
	{
		final int firstRow = 0;
		SearchTreeNode thisNode = createAmazonStyleNode(gridData, firstRow);
		return getSearchTree(thisNode, gridData, firstRow);
	}
	
	// loop through all rows with recursion, building a search tree that 
	// is grouped to the left, like:     (a and b) or c
	public SearchTreeNode getSearchTree(SearchTreeNode existingLeftNode, GridData gridData, int opRow)
	{
		int rightValueRow = opRow + 1;
		
		if(rightValueRow >= gridData.getRowCount())
			return existingLeftNode;
			
		int op = getAndOr(gridData, opRow);
		
		SearchTreeNode newRightNode = createAmazonStyleNode(gridData, rightValueRow);
		SearchTreeNode newOpNode = new SearchTreeNode(op, existingLeftNode, newRightNode);
		return getSearchTree(newOpNode, gridData, opRow + 1);
	}

	private int getAndOr(GridData gridData, int opRow)
	{
		String andOr = gridData.getValueAt(opRow, 3); 
		if(andOr.length() == 0)
		{
			FieldSpec fieldSpec = gridData.getSpec().getFieldSpec(3);
			DropDownFieldSpec fieldColumnSpec = (DropDownFieldSpec)fieldSpec;
			andOr = fieldColumnSpec.getChoice(0).getCode();
		}
		
		if(andOr.equals(SearchParser.ENGLISH_AND_KEYWORD))
			return SearchTreeNode.AND;
		if(andOr.equals(SearchParser.ENGLISH_OR_KEYWORD))
			return SearchTreeNode.OR;

		throw new RuntimeException("Unknown and/or keyword: " + andOr);
	}

	// Amazon style allows the user to enter something like:    a or b
	// into the value area, and the same field is applied to each value
	private SearchTreeNode createAmazonStyleNode(GridData gridData, int row)
	{
		FieldSpec specForThisValue = getFieldToSearchIn(gridData, row);

		String op = gridData.getValueAt(row, 1);
		String value = gridData.getValueAt(row, 2);
		value = value.trim();
		
		String localAnd = getLocalization().getKeyword(SearchParser.ENGLISH_AND_KEYWORD);
		String localOr = getLocalization().getKeyword(SearchParser.ENGLISH_OR_KEYWORD);
		SearchParser parser = new SearchParser(localAnd, localOr);
		return parser.parse(specForThisValue, op, value);
	}

	private FieldSpec getFieldToSearchIn(GridData gridData, int row)
	{
		PopUpTreeFieldSpec fieldColumnSpec = (PopUpTreeFieldSpec)gridData.getSpec().getFieldSpec(0);
		String value = gridData.getValueAt(row, 0);
		if(value.length() == 0)
			value = fieldColumnSpec.getFirstChoice().getCode();
		String miniSpecAsJsonString = value;
		SearchableFieldChoiceItem choice = fieldColumnSpec.findCode(miniSpecAsJsonString);
		return choice.getSpec();
	}
	
	public static SearchableFieldChoiceItem findSearchTag(PopUpTreeFieldSpec specOfFieldColumn, String code)
	{
		return specOfFieldColumn.findSearchTag(code);
	}

	public static final int COLUMN_ROW_NUMBER = 0;
	public static final int COLUMN_FIELD = 1;
	public static final int COLUMN_COMPARE_HOW = 2;
	public static final int COLUMN_VALUE = 3;
	
	public static final String TAG_ROWS = "Rows";
	public static final String TAG_FIELD_TO_SEARCH = "FieldToSearch";
	public static final String TAG_COMPARE_HOW = "CompareHow";
	public static final String TAG_LOOK_FOR = "LookFor";
	public static final String TAG_AND_OR = "AndOr";
	
	FancySearchTableModel model;
	UiDialogLauncher dlgLauncher;
}

