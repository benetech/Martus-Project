package org.martus.client.swingui;

import javax.swing.text.JTextComponent;

import org.martus.common.utilities.BurmeseUtilities;

public class UiFontEncodingHelper
{
	public UiFontEncodingHelper(boolean useZawgyi)
	{
		this.useZawgyi = useZawgyi;
	}

	public void setDisplayableText(JTextComponent textField, String value)
	{
		if (useZawgyi)
			value = BurmeseUtilities.getDisplayable(value);
		textField.setText(value);
	}

	public String getStorableText(JTextComponent textField)
	{
		String value = textField.getText();
		if (useZawgyi)
			value = BurmeseUtilities.getStorable(value);
		return value;
	}

	public String getDisplayable(String value)
	{
		if (useZawgyi)
			value = BurmeseUtilities.getDisplayable(value);
		return value;
	}

	public String getStorable(String value)
	{
		if (useZawgyi)
			value = BurmeseUtilities.getStorable(value);
		return value;
	}

	private boolean useZawgyi;
}
