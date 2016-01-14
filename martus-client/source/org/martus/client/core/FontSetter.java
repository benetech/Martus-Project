package org.martus.client.core;

import java.awt.Font;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.martus.common.MartusLogger;
import org.martus.swing.FontHandler;

public class FontSetter {

	public static void setUIFont(String fontName)
	{
		createOriginalDefaults();
		Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements())
		{
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource)
			{
				memorizeOriginalFont(key, value);
				FontUIResource current = (FontUIResource) value;
				Font font = new Font(fontName, current.getStyle(), current.getSize());
				UIManager.put(key, new FontUIResource(font));
			}
		}
	 }

	public static void setDefaultFont(boolean useZawgyi)
	{
		FontHandler.setUseZawgyiFont(useZawgyi);
		if (useZawgyi)
		{
			setUIFont(FontHandler.BURMESE_FONT);
		}  else
		{
			restoreDefaults();
		}
	}

	private static void memorizeOriginalFont(Object key, Object value)
	{
		if (originalDefaults.get(key) == null)
		{
			originalDefaults.put(key, value);
		}
	}

	public static void restoreDefaults()
	{
		MartusLogger.log("FontSetter.restoreDefaults()");
		if (originalDefaults == null)
			return;
		Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements())
		{
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource)
			{
				if (originalDefaults.get(key) != null)
					UIManager.put(key, originalDefaults.get(key));
			}
		}
	}

	private static void createOriginalDefaults()
	{
		if (originalDefaults == null)
		{
			originalDefaults = new HashMap<Object,Object>();
		}
	}

	private static Map<Object, Object> originalDefaults;
}
