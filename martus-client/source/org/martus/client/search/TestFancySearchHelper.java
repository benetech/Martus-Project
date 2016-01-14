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

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

import org.json.JSONArray;
import org.json.JSONObject;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.test.MockMartusApp;
import org.martus.common.GridData;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDropdown;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FieldTypePopUpTree;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.SearchFieldTreeModel;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.util.TestCaseEnhanced;

public class TestFancySearchHelper extends TestCaseEnhanced
{
	public TestFancySearchHelper(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		app = MockMartusApp.create(getName());
		getStore().createFieldSpecCacheFromDatabase();
		tempDir = createTempDirectory();
		localization = new MartusLocalization(tempDir, new String[0]);
		localization.setCurrentLanguageCode(MiniLocalization.ENGLISH);
		UiDialogLauncher nullLauncher = new UiDialogLauncher(null, localization);
		helper = new FancySearchHelper(getStore(), nullLauncher);
		noReusableChoices = PoolOfReusableChoicesLists.EMPTY_POOL;
		
	}
	
	public void tearDown()throws Exception
	{
		tempDir.delete();
		app.deleteAllFiles();
	}
	
	public void testCreateGridSpec()
	{
		GridFieldSpec spec = helper.getGridSpec(getStore());
		assertEquals(4, spec.getColumnCount());
		assertEquals("no field column?", new FieldTypePopUpTree(), spec.getColumnType(0));
		assertEquals("no op column?", new FieldTypeDropdown(), spec.getColumnType(1));
		assertEquals("no value column?", new FieldTypeNormal(), spec.getColumnType(2));
		assertEquals("no andor column?", new FieldTypeDropdown(), spec.getColumnType(3));
	}
	
	public void testAndOrColumn() throws Exception
	{
		DropDownFieldSpec spec = helper.createAndOrColumnSpec();
		assertEquals(2, spec.getCount());
		assertEquals("and", spec.getChoice(0).getCode());
		assertEquals("or", spec.getChoice(1).getCode());
	}
	
	SearchFieldTreeModel createSearchFieldModel(ChoiceItem[] choices)
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		for(int i = 0; i < choices.length; ++i)
			root.add(new DefaultMutableTreeNode(choices[i]));
		SearchFieldTreeModel model = new SearchFieldTreeModel(root);
		return model;
	}
	
	public void testGetSearchTreeBooleanValue() throws Exception
	{
		FieldSpec booleanSpec = FieldSpec.createCustomField("tag", "Label", new FieldTypeBoolean());
		ChoiceItem[] fields = new ChoiceItem[] {
			new SearchableFieldChoiceItem(booleanSpec),
		};
		PopUpTreeFieldSpec fieldColumnSpec = new PopUpTreeFieldSpec(createSearchFieldModel(fields));
		
		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(fieldColumnSpec);
		spec.addColumn(FieldSpec.createStandardField("op", new FieldTypeNormal()));
		spec.addColumn(FieldSpec.createStandardField("value", new FieldTypeNormal()));
		spec.addColumn(FieldSpec.createStandardField("andor", new FieldTypeNormal()));
		GridData data = new GridData(spec, noReusableChoices);
		addRow(data, new MiniFieldSpec(booleanSpec).toJson().toString(), "=", "1", "or");
		
		SearchTreeNode booleanEquals = helper.getSearchTree(data);
		assertEquals(SearchTreeNode.VALUE, booleanEquals.getOperation());
		assertEquals("tag", booleanEquals.getField().getTag());
		assertEquals("1", booleanEquals.getValue());
	}
	
	public void testGetSearchTreeOneRow() throws Exception
	{
		FieldSpec normalSpec = FieldSpec.createCustomField("field", "Label", new FieldTypeNormal());
		ChoiceItem[] fields = new ChoiceItem[] {
			new SearchableFieldChoiceItem(normalSpec),
		};
		PopUpTreeFieldSpec fieldColumnSpec = new PopUpTreeFieldSpec(createSearchFieldModel(fields));

		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(fieldColumnSpec);
		spec.addColumn(FieldSpec.createStandardField("op", new FieldTypeNormal()));
		spec.addColumn(FieldSpec.createStandardField("value", new FieldTypeNormal()));
		spec.addColumn(helper.createAndOrColumnSpec());
		GridData data = new GridData(spec, noReusableChoices);
		
		data.addEmptyRow();
		SearchTreeNode emptyRoot = helper.getSearchTree(data);
		FieldSpec emptySpec = emptyRoot.getField();
		assertEquals("Empty row wrong as tree?", normalSpec, emptySpec);
		
		JSONObject json = helper.getSearchAsJson(data);
		assertEquals("Empty row wrong as json?", 1, json.getJSONArray(FancySearchHelper.TAG_ROWS).length());
		helper.setSearchFromJson(data, json);
		assertEquals("Didn't save/restore 'Any Field' correctly?", "", data.getValueAt(0, 0));
		
		data.clear();
		addRow(data, fields[0].getCode(), "=", "value", "or");
		SearchTreeNode root = helper.getSearchTree(data);
		verifyFieldCompareOpValue("single row", root, normalSpec, MartusField.EQUAL, "value");
	}
	
	public void testGetSearchTreeTwoRows() throws Exception
	{
		FieldSpec a = FieldSpec.createCustomField("a", "A", new FieldTypeNormal());
		FieldSpec c = FieldSpec.createCustomField("c", "C", new FieldTypeNormal());
		ChoiceItem[] fields = new ChoiceItem[] {
			new SearchableFieldChoiceItem(a),
			new SearchableFieldChoiceItem(c),
		};
		PopUpTreeFieldSpec fieldColumnSpec = new PopUpTreeFieldSpec(createSearchFieldModel(fields));

		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(fieldColumnSpec);
		spec.addColumn(FieldSpec.createStandardField("op", new FieldTypeNormal()));
		spec.addColumn(FieldSpec.createStandardField("value", new FieldTypeNormal()));
		spec.addColumn(FieldSpec.createStandardField("andor", new FieldTypeNormal()));
		GridData data = new GridData(spec, noReusableChoices);
		addRow(data, new MiniFieldSpec(a).toJson().toString(), "=", "b", "or");
		addRow(data, new MiniFieldSpec(c).toJson().toString(), "=", "d", "or");
		
		SearchTreeNode root = helper.getSearchTree(data);
		verifyOp("top level", root, SearchTreeNode.OR);
		verifyFieldCompareOpValue("two rows left", root.getLeft(), a, MartusField.EQUAL, "b");
		verifyFieldCompareOpValue("two rows right", root.getRight(), c, MartusField.EQUAL, "d");
		
		JSONObject json = helper.getSearchAsJson(data);
		JSONArray rows = json.getJSONArray(FancySearchHelper.TAG_ROWS);
		assertEquals("didn't jsonize two rows?", 2, rows.length());
		JSONObject row1 = rows.getJSONObject(0);
		MiniFieldSpec miniSpec1 = new MiniFieldSpec(fields[0].getSpec());
		assertEquals("didn't save first code?", miniSpec1, new MiniFieldSpec(row1.getJSONObject(FancySearchHelper.TAG_FIELD_TO_SEARCH)));
		assertEquals("didn't save first comparehow?", "=", row1.getString(FancySearchHelper.TAG_COMPARE_HOW));
		assertEquals("didn't save first lookFor?", "b", row1.getString(FancySearchHelper.TAG_LOOK_FOR));
		assertEquals("didn't save first andor?", "or", row1.getString(FancySearchHelper.TAG_AND_OR));
		JSONObject row2 = rows.getJSONObject(1);
		MiniFieldSpec miniSpec2 = new MiniFieldSpec(fields[1].getSpec());
		assertEquals("didn't save second code?", miniSpec2, new MiniFieldSpec(row2.getJSONObject(FancySearchHelper.TAG_FIELD_TO_SEARCH)));
		assertEquals("didn't save second comparehow?", "=", row2.getString(FancySearchHelper.TAG_COMPARE_HOW));
		assertEquals("didn't save second lookFor?", "d", row2.getString(FancySearchHelper.TAG_LOOK_FOR));
		assertEquals("didn't save second andor?", "or", row2.getString(FancySearchHelper.TAG_AND_OR));
		
		GridData got = new GridData(data.getSpec(), noReusableChoices);
		helper.setSearchFromJson(got, json);
		assertEquals("didn't restore rows?", data.getRowCount(), got.getRowCount());
		for(int row = 0; row < got.getRowCount(); ++row)
			for(int column = 0; column < got.getColumnCount(); ++column)
				assertEquals("Bad data for row " + row + " col " + column, data.getValueAt(row, column), got.getValueAt(row, column));
	}
	
	public void testGetSearchTreeComplex() throws Exception
	{
		FieldSpec any = FieldSpec.createCustomField("", "Any", new FieldTypeNormal());
		FieldSpec a = FieldSpec.createCustomField("a", "A", new FieldTypeNormal());
		FieldSpec d = FieldSpec.createCustomField("d", "D", new FieldTypeNormal());
		FieldSpec g = FieldSpec.createCustomField("g", "G", new FieldTypeNormal());
		ChoiceItem[] fields = new ChoiceItem[] {
			new SearchableFieldChoiceItem("", any),
			new SearchableFieldChoiceItem(a),
			new SearchableFieldChoiceItem(d),
			new SearchableFieldChoiceItem(g),
		};
		PopUpTreeFieldSpec fieldColumnSpec = new PopUpTreeFieldSpec(createSearchFieldModel(fields));

		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(fieldColumnSpec);
		spec.addColumn(FieldSpec.createStandardField("op", new FieldTypeNormal()));
		spec.addColumn(FieldSpec.createStandardField("value", new FieldTypeNormal()));
		spec.addColumn(FieldSpec.createStandardField("andor", new FieldTypeNormal()));
		GridData data = new GridData(spec, noReusableChoices);
		addRow(data, "", "", "whiz", "or");
		addRow(data, fields[1].getCode(), "", "c1 and c2", "or");
		addRow(data, fields[2].getCode(), ">", " f", "and");
		addRow(data, fields[3].getCode(), "!=", "\"i i\"", "or");
		addRow(data, "", "", "j", "and");
		
		// (((any:whiz or (a~c1 and a~c2)) or d>f) and g!="ii") or any:j
		// OR  - any:j
		//  |
		// AND - g!="ii"
		//  |
		// OR  - d>f
		//  |
		// OR  - AND - a~c2
		//  |     |
		//  |    a~c1
		//  |
		// any:whiz
		SearchTreeNode beforeJ = helper.getSearchTree(data);
		verifyOp("before j", beforeJ, SearchTreeNode.OR);
		verifyFieldCompareOpValue("any:j", beforeJ.getRight(), any, MartusField.CONTAINS, "j");
		
		SearchTreeNode beforeGii = beforeJ.getLeft();
		verifyOp("before gii", beforeGii, SearchTreeNode.AND);
		verifyFieldCompareOpValue("g!=\"ii\"", beforeGii.getRight(), g, MartusField.NOT_EQUAL, "i i");
		
		SearchTreeNode beforeDf = beforeGii.getLeft();
		verifyOp("before df", beforeDf, SearchTreeNode.OR);
		verifyFieldCompareOpValue("d>f", beforeDf.getRight(), d, MartusField.GREATER, "f");
		
		SearchTreeNode beforeAandA = beforeDf.getLeft();
		verifyOp("before a a", beforeAandA, SearchTreeNode.OR);
		
		SearchTreeNode betweenAandA = beforeAandA.getRight();
		verifyOp("before a a", betweenAandA, SearchTreeNode.AND);
		verifyFieldCompareOpValue("a:c1", betweenAandA.getLeft(), a, MartusField.CONTAINS, "c1");
		verifyFieldCompareOpValue("a:c2", betweenAandA.getRight(), a, MartusField.CONTAINS, "c2");
		
		verifyFieldCompareOpValue("whiz", beforeAandA.getLeft(), any, MartusField.CONTAINS, "whiz");
	}
	
	private void verifyOp(String message, SearchTreeNode node, int expectedOp)
	{
		assertEquals(message, expectedOp, node.getOperation());
	}
	
	private void verifyFieldCompareOpValue(String message, SearchTreeNode node, FieldSpec field, int compareOp, String value)
	{
		assertEquals(message + " wrong op?", SearchTreeNode.VALUE, node.getOperation());
		assertEquals(message + " wrong field?", field, node.getField());
		assertEquals(message + " wrong compareOp?", compareOp, node.getComparisonOperator());
		assertEquals(message + " wrong value?", value, node.getValue());
	}
	
	private void addRow(GridData data, String field, String op, String value, String andOr)
	{
		int row = data.getRowCount();
		data.addEmptyRow();
		data.setValueAt(field, row, 0);
		data.setValueAt(op, row, 1);
		data.setValueAt(value, row, 2);
		data.setValueAt(andOr, row, 3);
	}

	ClientBulletinStore getStore()
	{
		return app.getStore();
	}
	
	MockMartusApp app;
	File tempDir;
	MartusLocalization localization;
	FancySearchHelper helper;
	private PoolOfReusableChoicesLists noReusableChoices;
}
