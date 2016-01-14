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

package org.martus.meta;

import org.martus.client.swingui.HeadquartersSelectionListener;
import org.martus.client.swingui.SelectableHeadquartersEntry;
import org.martus.client.swingui.bulletincomponent.HeadquartersEditorTableModel;
import org.martus.client.test.MockMartusApp;
import org.martus.clientside.test.MockUiLocalization;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.util.TestCaseEnhanced;

public class TestHeadquartersEditorTableModel extends TestCaseEnhanced implements HeadquartersSelectionListener
{

	public TestHeadquartersEditorTableModel(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		numberOfSelectedHQs = 0;
		if(localization!=null)
			return;
		localization = new MockUiLocalization(getName());
		appSecurityAndHQ = MockMartusSecurity.createHQ();
		app = MockMartusApp.create(appSecurityAndHQ, localization, getName());

		modelWithData = new HeadquartersEditorTableModel(app);
		modelWithData.setHQSelectionListener(this);
		key1 = new HeadquartersKey(publicKey1, label1);
		HeadquartersKeys allHQKeys = new HeadquartersKeys(key1);
		app.setAndSaveHQKeys(allHQKeys, allHQKeys);
		app.addHQLabelsWherePossible(allHQKeys);
		
		SelectableHeadquartersEntry entry1 = new SelectableHeadquartersEntry(key1);
		modelWithData.addNewHeadQuarterEntry(entry1);
		
		key2 = new HeadquartersKey(appSecurityAndHQ.getPublicKeyString());
		key2.setLabel(app.getHQLabelIfPresent(key2));
		SelectableHeadquartersEntry entry2 = new SelectableHeadquartersEntry(key2);
		modelWithData.addNewHeadQuarterEntry(entry2);

		modelWithoutData = new HeadquartersEditorTableModel(app);
	}

	public void tearDown() throws Exception
	{
		app.deleteAllFiles();
	   	super.tearDown();
	}
	
	public void testGetColumnName()
	{
		assertEquals(localization.getFieldLabel("HeadQuartersSelected"), modelWithData.getColumnName(0));
		assertEquals(localization.getFieldLabel("BulletinHeadQuartersHQLabel"), modelWithData.getColumnName(1));
	}
	
	public void testGetColumnCount()
	{
		assertEquals(2, modelWithoutData.getColumnCount());
		assertEquals(2, modelWithData.getColumnCount());
	}
	
	public void testGetRowCount()
	{
		assertEquals(0, modelWithoutData.getRowCount());
		assertEquals(2, modelWithData.getRowCount());
	}
	
	public void testIsCellEditable()
	{
		assertEquals("select hq not editable?", true, modelWithData.isCellEditable(1,modelWithData.COLUMN_SELECTED));
		assertEquals("label is editable?", false, modelWithData.isCellEditable(1,modelWithData.COLUMN_LABEL));
	}
	
	public void testGetColumnClass()
	{
		assertEquals(Boolean.class, modelWithData.getColumnClass(modelWithData.COLUMN_SELECTED));
		assertEquals(String.class, modelWithData.getColumnClass(modelWithData.COLUMN_LABEL));
	}
	
	public void testKeyLabelNames() throws Exception
	{
		assertEquals(label1, modelWithData.getValueAt(0,1));
		String label2 = MartusCrypto.computeFormattedPublicCode40(appSecurityAndHQ.getPublicKeyString()) + " " + localization.getFieldLabel("HQNotConfigured");
		assertEquals(label2, modelWithData.getValueAt(1,1));
	}
	
	public void testGetAllSelectedHeadQuarterKeys()
	{
		assertEquals(0, modelWithData.getAllSelectedHeadQuarterKeys().size());
		assertFalse(((Boolean)modelWithData.getValueAt(0,0)).booleanValue());
		assertEquals(0, modelWithoutData.getAllSelectedHeadQuarterKeys().size());
		assertEquals(0, modelWithData.getNumberOfSelectedHQs());
		assertEquals(0, modelWithoutData.getNumberOfSelectedHQs());
		assertEquals(0, numberOfSelectedHQs);
		
		modelWithData.setValueAt(Boolean.TRUE, 0,0);
		assertEquals(1, numberOfSelectedHQs);
		assertTrue(((Boolean)modelWithData.getValueAt(0,0)).booleanValue());

		HeadquartersKeys allSelectedHeadQuarterKeys = modelWithData.getAllSelectedHeadQuarterKeys();
		assertEquals(1, allSelectedHeadQuarterKeys.size());
		assertEquals(1, modelWithData.getNumberOfSelectedHQs());
		assertEquals(key1, allSelectedHeadQuarterKeys.get(0));

		modelWithData.setValueAt(Boolean.FALSE, 0,0);
		assertEquals(0, numberOfSelectedHQs);
		assertFalse(((Boolean)modelWithData.getValueAt(0,0)).booleanValue());
		modelWithData.setValueAt(Boolean.TRUE, 1,0);
		assertEquals(1, numberOfSelectedHQs);
		assertTrue(((Boolean)modelWithData.getValueAt(1,0)).booleanValue());
		
		allSelectedHeadQuarterKeys = modelWithData.getAllSelectedHeadQuarterKeys();
		assertEquals(1, allSelectedHeadQuarterKeys.size());
		assertEquals(1, modelWithData.getNumberOfSelectedHQs());
		assertEquals(key2, allSelectedHeadQuarterKeys.get(0));
		
		modelWithData.setValueAt(Boolean.TRUE, 0,0);
		assertEquals(2, numberOfSelectedHQs);
		allSelectedHeadQuarterKeys = modelWithData.getAllSelectedHeadQuarterKeys();
		assertEquals(2, allSelectedHeadQuarterKeys.size());
		assertEquals(2, modelWithData.getNumberOfSelectedHQs());
		
	}

	public void selectedHQsChanged(int newNumberOfSelectedHQs) 
	{
		numberOfSelectedHQs = newNumberOfSelectedHQs;
		
	}
	
	static MockUiLocalization localization;
	static MockMartusApp app;
	static MartusCrypto appSecurityAndHQ;
	static HeadquartersEditorTableModel modelWithData;
	static HeadquartersEditorTableModel modelWithoutData;
	
	static String publicKey1 = "123.436";
	static String label1 = "key1 label";
	static HeadquartersKey key1; 
	static HeadquartersKey key2;
	static int numberOfSelectedHQs;
}
