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
package org.martus.mspa.client.view;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;


public class CheckCellRenderer extends JCheckBox implements TableCellRenderer
{
	protected Border noFocusBorder;
		
	public CheckCellRenderer(String label) 
	{
	  super(label);
	  noFocusBorder = new EmptyBorder(1, 2, 1, 2);
	  setOpaque(true);
	  setBorder(noFocusBorder);
	}
		
	public Component getTableCellRendererComponent(JTable table,
		  Object value, boolean isSelected, boolean hasFocus, 
		  int row, int column) 
	{	 
		if (value instanceof Boolean) 
		{
			Boolean b = (Boolean)value;
			setSelected(b.booleanValue());
		}
				
		setBackground(isSelected && !hasFocus ? table.getSelectionBackground() : table.getBackground());
		setForeground(isSelected && !hasFocus ? table.getSelectionForeground() : table.getForeground());
			        
		setFont(table.getFont());
		
		// NOTE: Rick reported a null pointer exception here.
		// Unable to reproduce, so doing this brute-force to avoid the exception
		Border rendererBorder = hasFocus ? UIManager.getBorder("Table.focusCellHighlightBorder") : noFocusBorder;
		if(rendererBorder != null)
			setBorder(rendererBorder);
			
		return this;
	}	

}
