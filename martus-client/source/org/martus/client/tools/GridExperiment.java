/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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
package org.martus.client.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.jhlabs.awt.Alignment;
import com.jhlabs.awt.GridLayoutPlus;

public class GridExperiment
{
	public static void main(String[] args)
	{
		int rowCount = 2;
		GridLayoutPlus mainLayout = new MyGridLayout(rowCount, 3);
		mainLayout.setAlignment(Alignment.EAST);
		mainLayout.setFill(Alignment.FILL_NONE);
	//	mainLayout.setColWeight(0, 1);
		JPanel mainGrid = new JPanel(mainLayout);
		mainGrid.setBorder(new LineBorder(Color.BLUE));
		
		mainGrid.add(new JLabel("a"));
		mainGrid.add(new JLabel("b"));
		mainGrid.add(new JLabel("c"));
		for(int row = 1; row < rowCount; ++row)
			createRow(mainGrid);
		
		JFrame mainWindow = new JFrame();
		mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = mainWindow.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(mainGrid);
		((JComponent)contentPane).setBorder(new LineBorder(Color.RED));
		mainWindow.setSize(400, 100);
		mainWindow.setVisible(true);
	}

	private static void createRow(JPanel mainGrid)
	{
		mainGrid.add(new JLabel("x"));
		JComponent left = new JLabel("Left" + random.nextInt());
		left.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
		mainGrid.add(left);
		JComponent right = new JLabel("Right" + random.nextInt());
		mainGrid.add(right);
	}
	
	public static class MyGridLayout extends GridLayoutPlus 
	{
		public MyGridLayout(int rows, int columns)
		{
			super(rows, columns);
		}
	}

	static Random random = new Random();
}
