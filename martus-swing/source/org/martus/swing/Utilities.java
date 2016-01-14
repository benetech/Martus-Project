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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.martus.util.language.LanguageOptions;

public class Utilities 
{
	static public boolean isMacintosh()
	{
		return getOperatingSystem().startsWith("mac");
	}

	static public boolean isMSWindows()
	{
		return getOperatingSystem().startsWith("win");
	}
	
	static public boolean isLinux()
	{
		String os = getOperatingSystem();
		return os.contains("nix") || os.contains("nux"); 
	}

	private static String getOperatingSystem() {
		return System.getProperty("os.name").toLowerCase();
	}

	static public void maximizeWindow(JFrame window)
	{
		window.setVisible(true);//required for setting maximized
		window.setExtendedState(Frame.MAXIMIZED_BOTH);
	}

	static public boolean isValidScreenPosition(Dimension screenSize, Dimension objectSize, Point objectPosition)
	{
		int height = objectSize.height;
		if(height == 0 )
			return false;
		if(objectPosition.x > screenSize.width - 100)
			return false;
		if(objectPosition.y > screenSize.height - 100)
			return false;
		if(objectPosition.x < -100 || objectPosition.y < -100)
			return false;
		return true;
	}

	static public void waitForThreadToTerminate(Delay worker)
	{
		try
		{
			worker.join();
		}
		catch (InterruptedException e)
		{
			// We don't care if this gets interrupted
		}
	}

	static public void packAndCenterWindow(Window dlg)
	{
		dlg.pack();//JAVA bug requires two packs to really get the correct dimensions
		dlg.pack();
		Dimension size = dlg.getSize();
		Dimension viewableScreenSize = getViewableScreenSize();

		if(size.height > viewableScreenSize.height) 
			size.height = viewableScreenSize.height;
			
	    if (size.width > viewableScreenSize.width)
	    	size.width = viewableScreenSize.width;

		Rectangle newScreen = getViewableRectangle();
		dlg.setSize(size);
		dlg.setLocation(center(size, newScreen));

	}
	
	static public void centerFrame(Window owner)
	{
		Dimension size = owner.getSize();
		owner.setLocation(center(size, getViewableRectangle()));
	}	
	
	static public Rectangle getViewableRectangle()
	{
	    Insets insets = getSystemInsets();
		return new Rectangle(new Point(insets.left, insets.top), getViewableScreenSize());		
	}
	
	static public Dimension getViewableScreenSize()
	{
		Dimension screenSizeExcludingToolbars = Toolkit.getDefaultToolkit().getScreenSize();
	    Insets insets = getSystemInsets();
	    screenSizeExcludingToolbars.width -= (insets.left + insets.right);
	    screenSizeExcludingToolbars.height -= (insets.top + insets.bottom);
	    return screenSizeExcludingToolbars;
	}

	private static Insets getSystemInsets()
	{
		JFrame tmpFrame = new JFrame();
		GraphicsConfiguration graphicsConfig = tmpFrame.getGraphicsConfiguration();
	    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfig);
		return insets;
	}

	static public Point center(Dimension inner, Rectangle outer)
	{
		int x = (outer.width - inner.width) / 2;
		int y = (outer.height - inner.height) / 2;
		return new Point(x + outer.x, y + outer.y);
	}
	
	static public void addComponentsRespectingOrientation(JComponent component, Component[] itemsToAdd)
	{
		if(LanguageOptions.isRightToLeftLanguage())
		{
			for(int i = itemsToAdd.length -1; i >= 0; --i)
				component.add(itemsToAdd[i]);
		}
		else
		{
			for(int i = 0; i < itemsToAdd.length; ++i)
				component.add(itemsToAdd[i]);
		}
		
	}
	
	public static String createStringRespectingOrientation(Vector stringParts) 
	{
		StringBuffer result = new StringBuffer();
		int lastIndex = stringParts.size()-1;
		if(LanguageOptions.isRightToLeftLanguage())
		{
			for(int i = lastIndex; i>=0; --i)
			{
				result.append(stringParts.get(i));
			}
		}
		else
		{
			for(int i = 0; i <= lastIndex; ++i)
			{
				result.append(stringParts.get(i));
			}
		}
		return result.toString();
	}
	

	public static Dimension addCushionToHeightIfRequired(Dimension d, int extraHeight)
	{
		if(!LanguageOptions.needsLanguagePadding())
			return d;
		d.setSize(d.getWidth(), d.getHeight() + extraHeight);
		return d;
	}

	public static class Delay extends Thread
	{
		public Delay(int sec)
		{
			timeInMillis = sec * 1000;
		}

		public void run()
		{
			try
			{
				sleep(timeInMillis);
			}
			catch(InterruptedException e)
			{
			}
		}

		private int timeInMillis;
	}

	static public void forceScrollerToTop(JComponent viewToScroll)
	{
		forceScrollerToRect(viewToScroll, new Rectangle(0,0,0,0));
	}

	static public void forceScrollerToRect(JComponent viewToScroll, Rectangle rect )
	{
		//JAVA QUIRK: The Scrolling to Top must happen after construction 
		//it seems the command is ignored until after all layout has occured.
		//This is why we must create a new Runnable which is invoked after the GUI construction.
		SwingUtilities.invokeLater(new ScrollToRect(viewToScroll, rect));
	}

	public static Point createPointFromPoint2D(Point2D point2D)
	{
		// TODO: Is there any cleaner way to convert a Point2D into a Point???
		int x2D = (int)point2D.getX();
		int y2D = (int)point2D.getY();
		Point point = new Point(x2D, y2D);
		return point;
	}

	private static class ScrollToRect implements Runnable
	{
		ScrollToRect(JComponent viewToUse, Rectangle rectToUse)
		{
			viewToScroll = viewToUse;
			rect = rectToUse;
		}
		public void run()
		{
			viewToScroll.scrollRectToVisible(rect);
		}
		JComponent viewToScroll;
		Rectangle rect;
	}
	
	


    /**
     * Make sure that an on screen component fits on the screen without trying
     * to re-size it.  This is good to apply to pop-ups after moving them.
     *
     * @param window Window to fit in the screen
     */
    public static void fitInScreen( Window o ) 
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        
        Point oPoint = o.getLocation();
        int oX = (int)oPoint.getX();
        int oY = (int)oPoint.getY();
        
        int outX = oX + o.getWidth();
        int outY = oY + o.getHeight();

        if ( outX > screen.getWidth() )
            oX -= outX - screen.getWidth();
        
        if ( outY > screen.getHeight() )
            oY -= outY - screen.getHeight();
        
        oX = Math.max( oX, 0 );
        oY = Math.max( oY, 0 );
        
        o.setLocation( oX, oY );
    }
    
	static public Image getMartusIconImage()
	{
		URL imageURL = Utilities.class.getResource("/org/martus/swing/MartusLogo.png");
		if(imageURL == null)
			return null;
		ImageIcon imageicon = new ImageIcon(imageURL);
		return imageicon.getImage();
	}

  }
