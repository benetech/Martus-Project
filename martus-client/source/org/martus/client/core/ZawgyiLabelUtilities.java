package org.martus.client.core;

import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.swing.FontHandler;

/**
 * @author roms
 *         Date: 10/21/13
 */
public class ZawgyiLabelUtilities
{
	public static String getDisplayableLabel(FieldSpec spec, MiniLocalization localization)
	{
		FieldSpec baseSpec = spec.getParent();
		if(baseSpec == null)
			baseSpec = spec;
		boolean custom = StandardFieldSpecs.isCustomFieldTag(baseSpec.getTag());
		String label = spec.getLabel();
		if (custom)
		{
			UiFontEncodingHelper fontHelper = new UiFontEncodingHelper(FontHandler.isDoZawgyiConversion());
			label = fontHelper.getDisplayable(spec.getLabel());
		}
		else if(spec.getParent() == null)
		{
			String tag = spec.getTag();
			label = localization.getFieldLabel(tag);
		}

		return label;
	}
}
