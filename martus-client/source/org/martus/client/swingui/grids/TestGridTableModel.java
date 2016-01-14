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
package org.martus.client.swingui.grids;

import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeDropdown;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.TestCaseEnhanced;


public class TestGridTableModel extends TestCaseEnhanced
{
	public TestGridTableModel(String name)
	{
		super(name);
	}
	
	public void testBasics() throws Exception
	{
		GridFieldSpec gridSpec = new GridFieldSpec();
		String label1 = "column 1";
		FieldSpec column1 = FieldSpec.createFieldSpec(label1, new FieldTypeNormal());

		String label2 = "column 2";
		CustomDropDownFieldSpec column2 = new CustomDropDownFieldSpec();
		Vector choices = new Vector();
		String choice1 = "choice 1";
		String choice2 = "choice 2";
		choices.add(choice1);
		choices.add(choice2);
		
		column2.setChoices(column2.createValidChoiceItemArrayFromStrings(choices));
		column2.setLabel(label2);
		gridSpec.addColumn(column1);
		gridSpec.addColumn(column2);
		GridTableModel model = new GridTableModel(gridSpec, PoolOfReusableChoicesLists.EMPTY_POOL);
		TestTableModelListener listener = new TestTableModelListener();
		model.addTableModelListener(listener);
		int columnsIncludingRowCount = 3;
		assertEquals(columnsIncludingRowCount, model.getColumnCount());
		assertEquals(" ", model.getColumnName(0));
		assertEquals(new FieldTypeNormal(), model.getColumnType(0));
		assertEquals(new FieldTypeNormal(), model.getCellType(0, 0));
		assertEquals(label1, model.getColumnName(1));
		assertEquals(new FieldTypeNormal(), model.getColumnType(1));
		assertEquals(new FieldTypeNormal(), model.getCellType(0, 1));
		assertEquals(label2, model.getColumnName(2));
		assertEquals(new FieldTypeDropdown(), model.getColumnType(2));
		assertEquals(new FieldTypeDropdown(), model.getCellType(0, 2));

		CustomDropDownFieldSpec dropDownFieldSpec = ((CustomDropDownFieldSpec)(model.getFieldSpecForColumn(2)));
		assertEquals("", dropDownFieldSpec.getValue(0));
		assertEquals(choice1, dropDownFieldSpec.getValue(1));
		assertEquals(choice2, dropDownFieldSpec.getValue(2));
		
		assertEquals(0, model.getRowCount());
		assertEquals(0, listener.insertCalls());
		model.addEmptyRow();
		assertEquals(1, listener.insertCalls());
		assertEquals(1, model.getRowCount());
		String value1 = "row 1";
		model.setValueAt(value1, 0,1);
		assertEquals(value1, model.getValueAt(0,1));
		int rowOne = 1;
		assertEquals(Integer.toString(rowOne), model.getValueAt(0,0));
		model.addEmptyRow();
		assertEquals(2, listener.insertCalls());
		int rowTwo = 2;
		assertEquals(Integer.toString(rowTwo), model.getValueAt(1,0));
		String value2 = "row 2";
		model.setValueAt(value2, 1,1);
		assertEquals(value2, model.getValueAt(1,1));

		GridFieldSpec spec2 = new GridFieldSpec();
		String ColumnZeroHeader = "column 0";
		spec2.setColumnZeroLabel(ColumnZeroHeader);
		GridTableModel model2 = new GridTableModel(spec2, PoolOfReusableChoicesLists.EMPTY_POOL);
		assertEquals(ColumnZeroHeader, model2.getColumnName(0));
		
		assertEquals(0, listener.deletedCalls());
		assertEquals(2, model.getRowCount());
		model.deleteSelectedRow(0);
		assertEquals(1,listener.deletedCalls());
		assertEquals(1, model.getRowCount());
		assertEquals(value2, model.getValueAt(0,1));
		
		assertEquals(2,listener.insertCalls());
		model.deleteSelectedRow(0);
		assertEquals(2,listener.deletedCalls());
		assertEquals("Deleting last row should replace it with an empty row", 1, model.getRowCount());
		assertEquals(3,listener.insertCalls());
		
		assertEquals(1,model.getRowCount());
		model.insertEmptyRow(0);
		assertEquals(2,model.getRowCount());
		assertEquals(4,listener.insertCalls());
		try 
		{
			model.insertEmptyRow(-1);
			fail("should have thrown for invalid row to insert at -1");
		} 
		catch (Exception expected) 
		{
		}

		try 
		{
			model.insertEmptyRow(50);
			fail("should have thrown for invalid row to insert");
		} 
		catch (Exception expected) 
		{
		}
		
		
	}
	

class TestTableModelListener implements TableModelListener
{

	public void tableChanged(TableModelEvent e) 
	{
		if(e.getType() == TableModelEvent.DELETE)
			++deletedCalls;
		if(e.getType() == TableModelEvent.INSERT)
			++insertCalls;
	}
	
	public int deletedCalls()
	{
		return deletedCalls;
	}
	public int insertCalls()
	{
		return insertCalls;
	}
	int deletedCalls = 0;
	int insertCalls = 0;
}

}
