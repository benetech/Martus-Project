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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.martus.util.language.LanguageOptions;


public class UiComboBox extends JComboBox
{
	
	public UiComboBox()
	{
		initalize();
	}

	
	public UiComboBox(Object[] items)
	{
		super(items);
		initalize();
	}

	private void initalize()
	{
		setComponentOrienation();
		addKeyListener(new UiComboBoxKeyListener());
	}
	
	public void setUI(ComboBoxUI ui)
	{
		/* UGLY HACK: Under MS Windows, some fonts like Arabic extend beyond their declared bounds,
		 * so we have to pad them. If we do that in combo boxes, another Swing bug 
		 * causes the dropdown arrow to become twice as large.
		 * If we are running under MS Windows, we'll tweak the button size.
		 * But under Linux, this would cause the entire combo border to disappear, and 
		 * when using a padded language the dropdown button is half as wide as it should be */
		if(Utilities.isMSWindows() && LanguageOptions.needsLanguagePadding())
			ui = new SlimArrowComboBoxUi();
		super.setUI(ui);
	}
	
	private void setComponentOrienation()
	{
		setComponentOrientation(UiLanguageDirection.getComponentOrientation());
		setRenderer(new UiComboListCellRenderer());
	}
	
	class UiComboBoxKeyListener extends KeyAdapter
	{
		public void keyReleased(KeyEvent e)
		{
			if(e.getKeyCode() == KeyEvent.VK_SPACE)
			{
				if(isPopupVisible())
					hidePopup();
				else
					showPopup();
				e.consume();
				return;
			}
			super.keyReleased(e);
		}
	}

	class UiComboListCellRenderer extends DefaultListCellRenderer
	{
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			setComponentOrientation(UiLanguageDirection.getComponentOrientation());
			setHorizontalAlignment(UiLanguageDirection.getHorizontalAlignment());
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}

		public Dimension getPreferredSize()
		{
			Dimension dimension = super.getPreferredSize();
			dimension.height += LanguageOptions.getExtraHeightIfNecessary();
			return dimension;
		}
		
	}
	
	class SlimArrowComboBoxUi extends BasicComboBoxUI
	{
		protected LayoutManager createLayoutManager()
		{
			return new SlimArrowLayoutManager();
		}
		
		public JButton getArrowButton()
		{
			return arrowButton;
		}
		
		class SlimArrowLayoutManager extends ComboBoxLayoutManager
		{
	        public void layoutContainer(Container parent) 
	        {
	        	super.layoutContainer(parent);
	        	Rectangle rect = getArrowButton().getBounds();
	        	JScrollBar scrollbar = new JScrollBar();
	        	rect.width = scrollbar.getPreferredSize().width;		        	
	        	getArrowButton().setBounds(rect);
	        }
		}
	}

}
