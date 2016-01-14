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
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JSplitPane;

import org.martus.swing.UiWrappedTextArea;

public class KurdishTest
{
	public static void main(String[] args) throws Exception
	{
		showFonts("Arabic A", '\uFB50');
		showFonts("Arabic B", '\uFE70');
		new KurdishTest();
	}
	
	public KurdishTest() throws Exception
	{
		InputStream in = getClass().getResource("KurdishUTF8.txt").openStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int b;
		while( (b=in.read()) >= 0)
		{
			out.write(b);
		}
		in.close();
		String kurdishText = new String(out.toString("UTF-8")).substring(0, 36);

		String windowsString = "Kurdish Test";
		String javaVersion = System.getProperty("java.version");
		String swingString = "Kurdish Java " + javaVersion + " Swing: " + new String(kurdishText);
		String directString = "Kurdish Java " + javaVersion + " TextLayout: " + new String(kurdishText);

		JComponent label = new UiWrappedTextArea(swingString);
		
		JComponent viaTextLayout = new ViaTextLayout(directString);

		System.out.println("Defaulting to: " + label.getFont().getFontName());
		
//		Font khmerFont = new Font("Lucida Bright Regular", Font.PLAIN, 30);
//		label.setFont(khmerFont);
//		viaTextLayout.setFont(khmerFont);

		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.add(label, JSplitPane.TOP);
		splitter.add(viaTextLayout, JSplitPane.BOTTOM);

		JDialog sample = new JDialog();
		sample.setTitle(windowsString);
		sample.getContentPane().add(splitter);
		sample.setSize(800,300);
		sample.setModal(true);
		sample.setVisible(true);
	}
	
	public static void showFonts(String languageName, char sampleCharacter)
	{
		// Determine which fonts support Chinese here ...
		Vector fonts = new Vector();
		Font[] allfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (int j = 0; j < allfonts.length; j++) 
		{
			if (allfonts[j].canDisplay(sampleCharacter)) 
			{ 
			    fonts.add(allfonts[j].getFontName());
			}
		}
		
		int count = fonts.size();
		System.out.println("Found " + count + " " + languageName + "-capable fonts:");
		for(int i=0; i < count; ++i)
		{
			System.out.println(fonts.get(i));
		}
	}

}

class ViaTextLayout extends JComponent
{
	public ViaTextLayout(String textToShow)
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
