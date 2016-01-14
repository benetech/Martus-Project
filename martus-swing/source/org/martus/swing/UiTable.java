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

To the extent this copyrighted software code is used in the 
Miradi project, it is subject to a royalty-free license to 
members of the Conservation Measures Partnership when 
used with the Miradi software as specified in the agreement 
between Benetech and WCS dated 5/1/05.
*/
package org.martus.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.martus.util.language.LanguageOptions;
import org.miradi.utils.TableWithSunBugWorkarounds;


public class UiTable extends TableWithSunBugWorkarounds
{
	public UiTable()
	{
		this(null);
	}
	
	public UiTable(TableModel model)
	{
		super(model);
		setMaxGridWidth(80);
		useMaxWidth = false;
		setComponentOrientation(UiLanguageDirection.getComponentOrientation());
		setTableHeader(new UiTableHeader(getColumnModel()));
	}
	
	public void setRenderers(UiTableModel model)
	{
		Color disabledBackgroundColor = UIManager.getColor("TextField.inactiveBackground");
		setDefaultRenderer(Boolean.class, new UiBooleanRenderer(model, disabledBackgroundColor, getDefaultRenderer(Boolean.class)));
		setDefaultRenderer(Integer.class, new UiRenderer(model, disabledBackgroundColor, getDefaultRenderer(Integer.class)));
		setDefaultRenderer(String.class, new UiRenderer(model, disabledBackgroundColor, getDefaultRenderer(String.class)));
	}
	
	public int getRowHeight() 
	{
		int defaultRowHeight = super.getRowHeight();
		defaultRowHeight += LanguageOptions.getExtraHeightIfNecessary();
		return defaultRowHeight;
	}

	public void setMaxGridWidth(int maxGridWidthInCharactersToUse)
	{
		maxGridWidthPixels = maxGridWidthInCharactersToUse * 11;
	}
	
	public void useMaxWidth()
	{
		useMaxWidth = true;
	}
	
	public void resizeTable()
	{
		resizeTable(getModel().getRowCount());
	}

	public void resizeTable(int rowCount)
	{
		Dimension d = getPreferredScrollableViewportSize();
		int constantRowHeight = getRowHeight() + getRowMargin() ;
		d.height = rowCount * constantRowHeight;
		int headerWidth = getHeaderWidth();
		if(headerWidth < maxGridWidthPixels  && !useMaxWidth)
			d.width = headerWidth;
		else
			d.width = maxGridWidthPixels;
		setPreferredScrollableViewportSize(d);
	}
	
	public void setMaxColumnWidthToHeaderWidth(int column)
	{
		setColumnMaxWidth(column, getColumnHeaderWidth(column));
	}

	public void setColumnWidthToHeaderWidth(int column)
	{
		setColumnWidth(column, getColumnHeaderWidth(column));
	}
	public void setColumnWidthToMinimumRequred(int column)
	{
		int columnWidth = getColumnHeaderWidth(column);
		if(columnWidth < MINIMUM_COLUMN_WIDTH)
			columnWidth = MINIMUM_COLUMN_WIDTH;
		setColumnWidth(column, columnWidth);
	}

	public void setColumnMaxWidth(int column, int width) 
	{
		TableColumn columnToAdjust = getColumnModel().getColumn(column);
		columnToAdjust.setMaxWidth(width);
		setColumnWidth(column, width);
	}
	
	public void setColumnWidth(int column, int width) 
	{
		TableColumn columnToAdjust = getColumnModel().getColumn(column);
		columnToAdjust.setPreferredWidth(width);
		columnToAdjust.setWidth(width);
	}

	public int getColumnHeaderWidth(int column) 
	{
		TableColumn columnToAdjust = getColumnModel().getColumn(column);
		String padding = "    ";
		String value = (String)columnToAdjust.getHeaderValue() + padding;
		return getRenderedWidth(column, value);
	}
	
	public int getRenderedWidth(int column, String value) 
	{
		TableColumn columnToAdjust = getColumnModel().getColumn(column);
		TableCellRenderer renderer = columnToAdjust.getHeaderRenderer();
		if(renderer == null)
		{
			JTableHeader header = getTableHeader();
			renderer = header.getDefaultRenderer();
		}
		Component c = renderer.getTableCellRendererComponent(this, value, true, true, -1, column);
		int width = c.getPreferredSize().width;
		return width;
	}
	
	public int getHeaderWidth()
	{
		int width = 0;
		for(int i = 0; i < getModel().getColumnCount(); ++i)
		{
			width += getColumnModel().getColumn(i).getWidth();
		}
		return width;
	}
	
	public class UiTableHeader extends JTableHeader
	{
		public UiTableHeader (TableColumnModel model)
		{
			super(model);
			setComponentOrientation(UiLanguageDirection.getComponentOrientation());
			setReorderingAllowed(false);
		}
		
		public Dimension getPreferredSize()
		{
			Dimension d = super.getPreferredSize();
			d.height += LanguageOptions.getExtraHeightIfNecessary();
			return d;
		}
	}

	public TableCellRenderer getCellRenderer(int row, int column)
	{
		TableCellRenderer renderer = super.getCellRenderer(row, column);
		if(renderer instanceof DefaultTableCellRenderer)
			((DefaultTableCellRenderer)renderer).setHorizontalAlignment(UiLanguageDirection.getHorizontalAlignment());
		return renderer;
	}
	
	public int getSelectedRow()
	{
		// NOTE: Java sometimes returns non-empty selected 
		// rows when the table is empty, so check that first.
		// JAVA BUG #4247579,  Java 1.4.2 2006-10-27 kbs
		if(getRowCount() == 0)
			return -1;
		
		return super.getSelectedRow();
	}
	
	public int[] getSelectedRows()
	{
		// NOTE: Java sometimes returns non-empty selected 
		// rows when the table is empty, so check that first.
		// JAVA BUG #4247579,  Java 1.4.2 2006-10-27 kbs
		if(getRowCount() == 0)
			return new int[0];
		
		return super.getSelectedRows();
	}

	public int getSelectedRowCount()
	{
		// NOTE: Java sometimes returns non-empty selected 
		// rows when the table is empty, so check that first.
		// JAVA BUG #4247579,  Java 1.4.2 2006-10-27 kbs
		if(getRowCount() == 0)
			return 0;
		
		return super.getSelectedRowCount();
	}
	final int MINIMUM_COLUMN_WIDTH = 100;
	int maxGridWidthPixels;
	boolean useMaxWidth;
}
