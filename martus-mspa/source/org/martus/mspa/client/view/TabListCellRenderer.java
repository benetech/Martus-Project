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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


public class TabListCellRenderer extends JLabel implements ListCellRenderer
{
	protected Border noFocusBorder;
	protected FontMetrics fontMetrics = null;
	protected Insets insets = new Insets(0, 0, 0, 0);

	protected int m_defaultTab = 100;
	protected int[] m_tabs = null;

	public TabListCellRenderer()
	{
		super();
		noFocusBorder = new EmptyBorder(1, 1, 1, 1);
		setOpaque(true);
		setBorder(noFocusBorder);
	}

	public Component getListCellRendererComponent(JList list,
		Object value, int index, boolean isSelected, boolean cellHasFocus)     
	{         
		setText(value.toString());

		setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
		setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
		
		setFont(list.getFont());
		setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

		return this;
	}

	public void setDefaultTab(int defaultTab) 
	{ 
		m_defaultTab = defaultTab; 
	}
	
	public int getDefaultTab() 
	{ 
		return m_defaultTab; 
	}
	
	public void setTabs(int[] tabs) 
	{
		 m_tabs = tabs; 
	}
	
	public int[] getTabs() 
	{ 
		return m_tabs;
	}
	
	public int getTab(int index)
	{
		if (m_tabs == null)
		   return m_defaultTab*index;
		
		int len = m_tabs.length;
		if (index >= 0 && index < len)
		   return m_tabs[index];

		return m_tabs[len-1] + m_defaultTab*(index-len+1);
	}


	public void paint(Graphics g)
	{
		fontMetrics = g.getFontMetrics();
	
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		getBorder().paintBorder(this, g, 0, 0, getWidth(), getHeight());

		g.setColor(getForeground());
		g.setFont(getFont());
		insets = getInsets();
		int x = insets.left;
		int y = insets.top + fontMetrics.getAscent();

		StringTokenizer	st = new StringTokenizer(getText(), "\t");
		while (st.hasMoreTokens()) 
		{
			String sNext = st.nextToken();
			g.drawString(sNext, x, y);
			x += fontMetrics.stringWidth(sNext);

			if (!st.hasMoreTokens())
				break;
			int index = 0;
			while (x >= getTab(index))
				  index++;
			x = getTab(index);
		}
   }

}
