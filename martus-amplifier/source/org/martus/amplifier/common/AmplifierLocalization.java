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

package org.martus.amplifier.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.martus.amplifier.main.MartusAmplifier;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MartusUtilities;
import org.martus.common.MiniLocalization;
import org.martus.util.DatePreference;
import org.martus.util.UnicodeReader;

public class AmplifierLocalization extends MiniLocalization
{
	public AmplifierLocalization()
	{
		super(EnglishCommonStrings.strings);
		setCurrentLanguageCode(MartusAmplifier.AMP_DEFAULT_LANGUAGE);
		setCurrentDateFormatCode(new DatePreference("ymd", '-').getDateTemplate());
		setAdjustPersianLegacyDates(true);
		setAdjustThaiLegacyDates(true);
	}

	public static String getLanguageString(String code)
	{
		InputStream in = getEnglishLanguageTranslationFile();
		HashMap languages = AmplifierLocalization.buildLanguageMap(in);
		if(!languages.containsKey(code))
			return null;
		return (String)languages.get(code);		
	}

	public static InputStream getEnglishLanguageTranslationFile()
	{
		return AmplifierLocalization.class.getResourceAsStream("LanguageNames_en.txt");
	}

	public static HashMap buildLanguageMap(InputStream languageFileInputStream)
	{
		HashMap languages = new HashMap();
		if(languageFileInputStream == null)
		{
			languages.put(SearchResultConstants.LANGUAGE_ANYLANGUAGE_LABEL, SearchResultConstants.LANGUAGE_ANYLANGUAGE_LABEL);
			return languages;				
		}
		
		try
		{
			UnicodeReader reader = new UnicodeReader(languageFileInputStream);
			Vector localizedLanguages = MartusUtilities.loadListFromFile(reader);
			reader.close();
			for (Iterator iter = localizedLanguages.iterator(); iter.hasNext();)
			{
				String data = (String) iter.next();
				String[] idAndName = data.split("=");
				languages.put(idAndName[0], idAndName[1]);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return languages;
	}

}
