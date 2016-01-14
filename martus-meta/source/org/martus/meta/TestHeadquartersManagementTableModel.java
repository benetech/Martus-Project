/*The Martus(tm) free, social justice documentation and
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

import org.martus.client.swingui.HeadquartersManagementTableModel;
import org.martus.client.swingui.SelectableHeadquartersEntry;
import org.martus.client.test.MockMartusApp;
import org.martus.clientside.test.MockUiLocalization;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.util.StreamableBase64.InvalidBase64Exception;
import org.martus.util.TestCaseEnhanced;

public class TestHeadquartersManagementTableModel extends TestCaseEnhanced
{

	public TestHeadquartersManagementTableModel(String name) 
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		if(localization!=null)
			return;
		localization = new MockUiLocalization(getName());
		appSecurityAndHQ = MockMartusSecurity.createHQ();
		app = MockMartusApp.create(appSecurityAndHQ, localization, getName());

		modelWithData = new HeadquartersManagementTableModel(app);
		key1 = new HeadquartersKey(publicCode1, label1);
		HeadquartersKeys hQKeysAuthorized = new HeadquartersKeys(key1); 
		app.setAndSaveHQKeys(hQKeysAuthorized, hQKeysAuthorized);
		app.addHQLabelsWherePossible(hQKeysAuthorized);
		
		SelectableHeadquartersEntry entry1 = new SelectableHeadquartersEntry(key1);
		modelWithData.addNewHeadQuarterEntry(entry1);
		
		key2 = new HeadquartersKey(appSecurityAndHQ.getPublicKeyString());
		key2.setLabel(app.getHQLabelIfPresent(key2));
		SelectableHeadquartersEntry entry2 = new SelectableHeadquartersEntry(key2);
		modelWithData.addNewHeadQuarterEntry(entry2);

		modelWithoutData = new HeadquartersManagementTableModel(app);
	}

	public void tearDown() throws Exception
	{
		app.deleteAllFiles();
	   	super.tearDown();
	}
	
	public void testGetColumnName()
	{
		assertEquals(localization.getFieldLabel("ConfigureHeadQuartersDefault"), modelWithData.getColumnName(0));
		assertEquals(localization.getFieldLabel("ConfigureHQColumnHeaderPublicCode"), modelWithData.getColumnName(1));
		assertEquals(localization.getFieldLabel("BulletinHeadQuartersHQLabel"), modelWithData.getColumnName(2));
	}
	
	public void testGetColumnCount()
	{
		assertEquals(3, modelWithoutData.getColumnCount());
		assertEquals(3, modelWithData.getColumnCount());
	}
	
	public void testGetRowCount()
	{
		assertEquals(0, modelWithoutData.getRowCount());
		assertEquals(2, modelWithData.getRowCount());
	}
	
	public void testIsCellEditable()
	{
		assertEquals("select hq not editable?", true, modelWithData.isCellEditable(1,modelWithData.COLUMN_DEFAULT));
		assertEquals("Public code is editable?", false, modelWithData.isCellEditable(1,modelWithData.COLUMN_PUBLIC_CODE));
		assertEquals("label is editable?", false, modelWithData.isCellEditable(1,modelWithData.COLUMN_LABEL));
	}
	
	public void testGetColumnClass()
	{
		assertEquals(Boolean.class, modelWithData.getColumnClass(modelWithData.COLUMN_DEFAULT));
		assertEquals(String.class, modelWithData.getColumnClass(modelWithData.COLUMN_PUBLIC_CODE));
		assertEquals(String.class, modelWithData.getColumnClass(modelWithData.COLUMN_LABEL));
	}
	
	public void testKeyLabelNames() throws Exception
	{
		assertEquals(label1, modelWithData.getValueAt(0,2));
		assertEquals(label1, modelWithData.getLabel(0));
		String label2 = MartusCrypto.computeFormattedPublicCode40(appSecurityAndHQ.getPublicKeyString()) + " " + localization.getFieldLabel("HQNotConfigured");
		assertEquals(label2, modelWithData.getValueAt(1,2));
		assertEquals(label2, modelWithData.getLabel(1));
	}
	
	public void testKeyPublicCodes() throws InvalidBase64Exception
	{
		assertEquals(key1.getFormattedPublicCode(), modelWithData.getValueAt(0,1));
		assertEquals(key1.getFormattedPublicCode(), modelWithData.getPublicCode(0));
		assertEquals(key2.getFormattedPublicCode(), modelWithData.getValueAt(1,1));
		assertEquals(key2.getFormattedPublicCode(), modelWithData.getPublicCode(1));
	}

	public void testGetAllSelectedHeadQuarterKeys()
	{
		assertEquals(0, modelWithoutData.getAllSelectedHeadQuarterKeys().size());
		assertEquals(0, modelWithoutData.getAllSelectedHeadQuarterKeys().size());
		
		modelWithData.setValueAt(Boolean.TRUE, 0,0);
		HeadquartersKeys allDefaultHeadQuarterKeys = modelWithData.getAllSelectedHeadQuarterKeys();
		assertEquals(1, allDefaultHeadQuarterKeys.size());
		assertTrue(((Boolean)modelWithData.getValueAt(0,0)).booleanValue());
		assertEquals(key1, allDefaultHeadQuarterKeys.get(0));

		modelWithData.setValueAt(Boolean.FALSE, 0,0);
		assertFalse(((Boolean)modelWithData.getValueAt(0,0)).booleanValue());
		assertEquals(0, modelWithData.getAllSelectedHeadQuarterKeys().size());
		modelWithData.setValueAt(Boolean.TRUE, 1,0);
		assertTrue(((Boolean)modelWithData.getValueAt(1,0)).booleanValue());
		allDefaultHeadQuarterKeys = modelWithData.getAllSelectedHeadQuarterKeys();
		assertEquals(1, allDefaultHeadQuarterKeys.size());
		assertEquals(key2, allDefaultHeadQuarterKeys.get(0));
		
		modelWithData.setValueAt(Boolean.TRUE, 0,0);
		assertTrue(((Boolean)modelWithData.getValueAt(0,0)).booleanValue());
		assertEquals(2, modelWithData.getAllSelectedHeadQuarterKeys().size());
	}
	
	public void testGetSetHQLabels()
	{
		String newLabel1 = "new HQ Label 1";
		String newLabel2 = "new HQ Label 2";
			
		int labelColumn = modelWithData.COLUMN_LABEL;
		modelWithData.setLabel(0, newLabel1);
		assertEquals(newLabel1, modelWithData.getValueAt(0, labelColumn));
		modelWithData.setLabel(0, label1);
		assertEquals(label1, modelWithData.getValueAt(0, labelColumn));
		
		modelWithData.setLabel(1, newLabel2);
		assertEquals(newLabel2, modelWithData.getValueAt(1, labelColumn));
		modelWithData.setLabel(1, "");
		assertEquals("", modelWithData.getValueAt(1, labelColumn));
	}
	
	public void testRemoveRow()
	{
		HeadquartersKey key3 = new HeadquartersKey("123.public.key.3");
		String key3Label = "key3";
		key3.setLabel(key3Label);
		SelectableHeadquartersEntry newEntry = new SelectableHeadquartersEntry(key3);
		assertEquals(2, modelWithData.getAllKeys().size());
		modelWithData.addNewHeadQuarterEntry(newEntry);
		assertEquals(3, modelWithData.getRowCount());
		assertEquals(3, modelWithData.getAllKeys().size());
		assertEquals(key3Label, modelWithData.getValueAt(2, modelWithData.COLUMN_LABEL));
		assertEquals(key1.getLabel(), modelWithData.getHQKey(0).getLabel());
		assertEquals(key2.getLabel(), modelWithData.getHQKey(1).getLabel());
		assertEquals(key3.getLabel(), modelWithData.getHQKey(2).getLabel());
		
		modelWithData.removeRow(1);
		assertEquals(2, modelWithData.getRowCount());
		assertEquals(key1.getLabel(), modelWithData.getValueAt(0, modelWithData.COLUMN_LABEL));
		assertEquals(key3.getLabel(), modelWithData.getHQKey(1).getLabel());
		assertEquals(key3.getLabel(), modelWithData.getValueAt(1, modelWithData.COLUMN_LABEL));
		
		
	}
	
	
	
	static MockUiLocalization localization;
	static MockMartusApp app;
	static MartusCrypto appSecurityAndHQ;
	static HeadquartersManagementTableModel modelWithData;
	static HeadquartersManagementTableModel modelWithoutData;
	
	static String publicCode1 = "123.436";
	static String label1 = "key1 label";
	static HeadquartersKey key1;
	static HeadquartersKey key2;
}



