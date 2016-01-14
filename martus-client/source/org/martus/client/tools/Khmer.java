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

package org.martus.client.tools;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class Khmer
{
	public static void main(String[] args)
	{
		char[] khmerText = {0x1781, 0x17D2, 0x1789, 0x17BB, 0x17C6, 0x200B, 
							0x179F, 0x17D2, 0x179A, 0x17B6, 0x200B, 
							0x179C, 0x17B7, 0x1791, 0x17BC};
		String khmerWindowsString = "Khmer Windows: " + new String(khmerText);
		String javaVersion = System.getProperty("java.version");
		String khmerSwingString = "Khmer Java " + javaVersion + " Swing: " + new String(khmerText);
		String khmerDirectString = "Khmer Java " + javaVersion + " TextLayout: " + new String(khmerText);

		JLabel khmerLabel = new JLabel(khmerSwingString);
		KhmerViaTextLayout khmerLayout = new KhmerViaTextLayout(khmerDirectString);

		Font khmerFont = new Font("Khmer OS", Font.PLAIN, 30);
		khmerLabel.setFont(khmerFont);
		khmerLayout.setFont(khmerFont);

		Box vbox = Box.createVerticalBox();
		vbox.add(khmerLabel);
		vbox.add(khmerLayout);

		JDialog sample = new JDialog();
		sample.setTitle(khmerWindowsString);
		sample.getContentPane().add(vbox);
		sample.setSize(800,300);
		sample.setModal(true);
		sample.setVisible(true);
	}
}

class KhmerViaTextLayout extends JComponent
{
	public KhmerViaTextLayout(String textToShow)
	{
		text = textToShow;
	}
	
	public void paint(Graphics graphics)
	{
		Graphics2D g = (Graphics2D)graphics;
		Point2D loc = new Point2D.Float(0, 50);
		Font font = getFont();
		FontRenderContext frc = g.getFontRenderContext();
		TextLayout layout = new TextLayout(text, font, frc);
		layout.draw(g, (float)loc.getX(), (float)loc.getY());
		
		Rectangle2D bounds = layout.getBounds();
		bounds.setRect(bounds.getX()+loc.getX(),
		bounds.getY()+loc.getY(),
		bounds.getWidth(),
		bounds.getHeight());
		g.draw(bounds);
	}
	
	String text;
}
