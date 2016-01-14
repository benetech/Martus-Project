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
package org.martus.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.utilities.BurmeseUtilities;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.DatePreference;
import org.martus.util.MultiCalendar;
import org.martus.util.MultiDateFormat;
import org.martus.util.language.LanguageOptions;
import org.martus.util.xml.XmlUtilities;

import com.ghasemkiani.util.icu.PersianCalendar;
import com.ibm.icu.util.SimpleTimeZone;


public class MiniLocalization
{
	static public class NoDateSeparatorException extends Exception
	{
	}
	
	public MiniLocalization(String[] englishStrings)
	{
		this();
		addEnglishTranslations(englishStrings);
	}

	public MiniLocalization()
	{
		textResources = new TreeMap();
		rightToLeftLanguages = new Vector();
		currentDateFormat = new DatePreference();
		languageSettings = new DefaultLanguageSettingsProvider();
	}
	
	public void setLanguageSettingsProvider(LanguageSettingsProvider providerToUse)
	{
		languageSettings = providerToUse;
		
		setCurrentLanguageCode(languageSettings.getCurrentLanguage());
		setCurrentDateFormatCode(languageSettings.getCurrentDateFormat());
		setCurrentCalendarSystem(languageSettings.getCurrentCalendarSystem());
	}
	
	public void addEnglishTranslations(String[] translations)
	{
		for(int i=0; i < translations.length; ++i)
		{
			String mtfEntry = translations[i];
			addEnglishTranslation(mtfEntry);
		}
	}
	
	private void addEnglishTranslation(String entry)
	{
		addTranslation(ENGLISH, entry);
	}

	public void addTranslation(String languageCode, String entryText)
	{
		if(entryText == null)
			return;
		
		if(entryText.indexOf('=') < 0)
			return;
		
		String key = extractKeyFromEntry(entryText);
		Map availableTranslations = getAvailableTranslations(key);
		if(availableTranslations == null)
		{
			if(!languageCode.equals(ENGLISH))
				return;
			availableTranslations = new TreeMap();
			textResources.put(key, availableTranslations);
		}
		
		String translatedText = extractValueFromEntry(entryText);
		availableTranslations.put(languageCode, translatedText);
	}

	public String extractKeyFromEntry(String entryText)
	{
		int splitAt = entryText.indexOf('=', 0);
		if(splitAt < 0)
			splitAt = 0;
		return entryText.substring(0, splitAt);
	}
	
	public String extractValueFromEntry(String entryText)
	{
		int keyEnd = entryText.indexOf('=');
		if(keyEnd < 0)
			return "";
		
		String value = entryText.substring(keyEnd+1);
		value = value.replaceAll("\\\\n", "\n");
		return value;
	}

	public SortedSet getAllKeysSorted()
	{
		Set allKeys = textResources.keySet();
		SortedSet sorted = new TreeSet(allKeys);
		return sorted;
	}

	protected String formatAsUntranslated(String value)
	{
		if(value.startsWith(NotTranslatedBeginCharacter))
			return value;
		return NotTranslatedBeginCharacter + value + NotTranslatedEndCharacter;
	}
	
	protected Map getAvailableTranslations(String key)
	{
		
		return (Map)textResources.get(key);
	}

	public String getCurrentLanguageCode()
	{
		return languageSettings.getCurrentLanguage();
	}

	public void setCurrentLanguageCode(String newLanguageCode)
	{
		languageSettings.setCurrentLanguage(newLanguageCode);
		if(isRightToLeftLanguage())
			LanguageOptions.setDirectionRightToLeft();
		else
			LanguageOptions.setDirectionLeftToRight();
		if(doesLanguageRequirePadding(languageSettings.getCurrentLanguage()))
			LanguageOptions.setLanguagePaddingRequired();
		else
			LanguageOptions.setLanguagePaddingNotRequired();
	}
	
	public boolean doesLanguageRequirePadding(String languageCode)
	{	
		if(languageCode == null)
			return false;
		boolean paddingRequired = languageCode.equals(ARABIC) || languageCode.equals(FARSI) || languageCode.equals(BURMESE);
		return paddingRequired;
	}
	
	public void setDateFormatFromLanguage()
	{
		currentDateFormat.fillFrom(getDefaultDatePreferenceForLanguage(getCurrentLanguageCode()));
		updateDateFormatInLanguageProvider();
	}
	
	public void setMdyOrder(String mdyOrder)
	{
		//TODO remove this and set this directly from CurrentUiState
		currentDateFormat.setMdyOrder(mdyOrder);
		updateDateFormatInLanguageProvider();
	}
	
	public void setDateDelimiter(char delimiter)
	{
		//TODO remove this and set this directly from CurrentUiState
		currentDateFormat.setDelimiter(delimiter);
		updateDateFormatInLanguageProvider();
	}
	
	public String getCurrentDateFormatCode()
	{
		return languageSettings.getCurrentDateFormat();
	}

	public void setCurrentDateFormatCode(String code)
	{
		try
		{
			currentDateFormat.setDateTemplate(code);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			currentDateFormat.setMdyOrder(DatePreference.DEFAULT_DATE_MDY_ORDER);
			currentDateFormat.setDelimiter(DatePreference.DEFAULT_DATE_DELIMITER);
		}
		
		updateDateFormatInLanguageProvider();
	}

	private void updateDateFormatInLanguageProvider()
	{
		languageSettings.setCurrentDateFormat(currentDateFormat.getRawDateTemplate());
	}
	
	public String getCurrentDateTemplate()
	{
		return currentDateFormat.getDateTemplate();
	}
	
	public String getMdyOrder()
	{
		return currentDateFormat.getMdyOrder();
	}
	
	public char getDateDelimiter()
	{
		return currentDateFormat.getDelimiter();
	}
	
	public String getLabel(String languageCode, String key)
	{
		Map availableTranslations = getAvailableTranslations(key);
		if(availableTranslations == null)
			return formatAsUntranslated(key);
	
		String translatedText = (String)availableTranslations.get(languageCode);
		if(translatedText != null)
			return translatedText;
	
		String englishText = (String)availableTranslations.get(ENGLISH);
		if(englishText == null)
		{
			System.out.println("Error, probably an invalid Martus-en.mtf file in C:\\Martus, try removing this file.");
			System.out.println("Possibly obsolete key: " + key);
			englishText = key;
		}
		return formatAsUntranslated(englishText);
	}

	public String getLabel(String languageCode, String category, String tag)
	{
		return getLabel(languageCode, category + ":" + tag);
	}

	public String getFieldLabelHtml(String fieldName)
	{
		String fieldLabel = getFieldLabel(getCurrentLanguageCode(), fieldName);
		return XmlUtilities.getXmlEncoded(fieldLabel);
	}

	public String getFieldLabel(String fieldName)
	{
		return getFieldLabel(getCurrentLanguageCode(), fieldName);
	}

	public String getFieldLabel(String languageCode, String fieldName) 
	{
		return getLabel(languageCode, "field", fieldName);
	}

	public String getStorableFieldLabel(String fieldName)
	{
		return BurmeseUtilities.getStorable(getFieldLabel(fieldName));
	}

	public String getLanguageName(String code)
	{
		return getLabel(getCurrentLanguageCode(), "language", code);
	}
	
	//TODO: From Kevin
	//This is a nice improvement. Now I think I want them all to call
	//localization.getCancelButtonLabel();
	//I assume that would be in UiLocalization (not mini, and not Martus).
	public String getCancelButtonLabel()
	{
		return getButtonLabel(EnglishCommonStrings.CANCEL);
	}

	public String getButtonLabel(String code)
	{
		return getLabel(getCurrentLanguageCode(), "button", code);
	}

	public String getTooltipLabel(String code)
	{
		return getLabel(getCurrentLanguageCode(), "tooltip", code);
	}

	public String getStatusLabel(String code)
	{
		return getLabel(getCurrentLanguageCode(), "status", code);
	}

	private static Map getDefaultDateFormats()
	{
		Map defaultLanguageDateFormat = new HashMap();
		defaultLanguageDateFormat.put(ENGLISH, new DatePreference());
		defaultLanguageDateFormat.put(SPANISH, new DatePreference("dmy", '/'));
		defaultLanguageDateFormat.put(RUSSIAN, new DatePreference("dmy", '.'));
		defaultLanguageDateFormat.put(THAI, new DatePreference("dmy", '/'));
		defaultLanguageDateFormat.put(ARABIC, new DatePreference("dmy", '/'));
		defaultLanguageDateFormat.put(FARSI, new DatePreference("dmy", '/'));
		return defaultLanguageDateFormat;
	}
	
	public static DatePreference getDefaultDatePreferenceForLanguage(String languageCode)
	{
		Map defaultLanguageDateFormat = getDefaultDateFormats();
		if(!defaultLanguageDateFormat.containsKey(languageCode))
			languageCode = ENGLISH;
		DatePreference pref = (DatePreference)defaultLanguageDateFormat.get(languageCode);
		return pref;
	}


	/////////////////////////////////////////////////////////////////
	// Date-oriented stuff
	
	public String getCurrentCalendarSystem()
	{
		return languageSettings.getCurrentCalendarSystem();
	}
	
	public void setCurrentCalendarSystem(String newSystem)
	{
		//TODO possibly remove this and call directly from CurrentUiState
		for(int i = 0; i < ALL_CALENDAR_SYSTEMS.length; ++i)
		{
			if(ALL_CALENDAR_SYSTEMS[i].equals(newSystem))
			{
				languageSettings.setCurrentCalendarSystem(newSystem);
				return;
			}
		}
		
		throw new RuntimeException("Unknown calendar system: " + newSystem);
	}
	
	public int getLocalizedYear(MultiCalendar cal)
	{
		int gregorianYear = cal.getGregorianYear();
		if(getCurrentCalendarSystem().equals(THAI_SYSTEM))
			return gregorianYear + MultiCalendar.THAI_YEAR_OFFSET;
		if(getCurrentCalendarSystem().equals(PERSIAN_SYSTEM) || getCurrentCalendarSystem().equals(AFGHAN_SYSTEM))
			return getPersianYear(cal);
		
		return gregorianYear;
	}
	
	public int getPersianYear(MultiCalendar cal)
	{
		return getPersianCalendar(cal).get(PersianCalendar.YEAR);
	}

	private PersianCalendar getPersianCalendar(MultiCalendar cal)
	{
		PersianCalendar pc = new PersianCalendar(new SimpleTimeZone(0, "UTC"));
		pc.setTime(cal.getTime());
		return pc;
	}

	public int getLocalizedMonth(MultiCalendar cal)
	{
		if(getCurrentCalendarSystem().equals(PERSIAN_SYSTEM) || getCurrentCalendarSystem().equals(AFGHAN_SYSTEM))
			return getPersianMonth(cal);
		
		return cal.getGregorianMonth();
	}
	
	public int getPersianMonth(MultiCalendar cal)
	{
		return getPersianCalendar(cal).get(PersianCalendar.MONTH) + 1;
	}

	public int getLocalizedDay(MultiCalendar cal)
	{
		if(getCurrentCalendarSystem().equals(PERSIAN_SYSTEM) || getCurrentCalendarSystem().equals(AFGHAN_SYSTEM))
			return getPersianDay(cal);
		return cal.getGregorianDay();
	}

	public int getPersianDay(MultiCalendar cal)
	{
		return getPersianCalendar(cal).get(PersianCalendar.DAY_OF_MONTH);
	}

	public MultiCalendar createCalendarFromLocalizedYearMonthDay(int year, int month, int day)
	{
		if(getCurrentCalendarSystem().equals(THAI_SYSTEM))
			return MultiCalendar.createFromGregorianYearMonthDay(year - MultiCalendar.THAI_YEAR_OFFSET, month, day);
		if(getCurrentCalendarSystem().equals(PERSIAN_SYSTEM) || getCurrentCalendarSystem().equals(AFGHAN_SYSTEM))
			return MultiCalendar.createCalendarFromPersianYearMonthDay(year, month, day);
		return MultiCalendar.createFromGregorianYearMonthDay(year, month, day);	
	}
	
	public MultiCalendar createCalendarFromIsoDateString(String isoDate)
	{
		int year = MultiCalendar.getYearFromIso(isoDate);
		int month = MultiCalendar.getMonthFromIso(isoDate);
		int day = MultiCalendar.getDayFromIso(isoDate);
		
		if(getAdjustPersianLegacyDates() && year > 1000 && year < 1900)
			return MultiCalendar.createCalendarFromPersianYearMonthDay(year, month, day);
		
		if(getAdjustThaiLegacyDates() && year > 2400)
			year -= MultiCalendar.THAI_YEAR_OFFSET;

		return MultiCalendar.createFromGregorianYearMonthDay(year, month, day);
	}
	
	/* 
	 * this expects a string in one of these forms:
	 * 	1989-12-01
	 *  1989-12-01,19891201+300
 	 */
	public MartusFlexidate createFlexidateFromStoredData(String storedDate)
	{
		try
		{
			String isoDate = storedDate;
			int range = 0;
			
			if(MartusFlexidate.isFlexidateString(storedDate))
			{
				isoDate = MartusFlexidate.extractIsoDateFromStoredDate(storedDate);							
				range = MartusFlexidate.extractRangeFromStoredDate(storedDate);
			}
			
			return new MartusFlexidate(createCalendarFromIsoDateString(isoDate), range);
		} 
		catch (Exception e)
		{
			return new MartusFlexidate("1900-01-01", 0);
		}
	}

	public String convertStoredDateToDisplay(String storedDate)
	{
		try
		{
			MultiCalendar cal = createCalendarFromIsoDateString(storedDate);
			return toDisplayDateString(cal);
		} 
		catch (Exception e)
		{
			// unparsable dates simply become blank strings,
			// so we don't want to do anything for this exception
			//e.printStackTrace();
			return "";
		}
	}

	private String toDisplayDateString(MultiCalendar cal)
	{
		if(cal.getGregorianYear() == MultiCalendar.YEAR_NOT_SPECIFIED)
			return getFieldLabel("DateNotSpecified");

		MultiDateFormat dfDisplay = new MultiDateFormat(currentDateFormat);
		int year = getLocalizedYear(cal);
		int month = getLocalizedMonth(cal);
		int day = getLocalizedDay(cal);
		return dfDisplay.format(year, month, day);
	}
	
	public String getViewableDateRange(String newText)
	{
		MartusFlexidate mfd = createFlexidateFromStoredData(newText);
		MultiCalendar begin = mfd.getBeginDate();
	
		if (!mfd.hasDateRange())
			return toDisplayDateString(begin);
	
		String rawEndDate = MartusFlexidate.toStoredDateFormat(mfd.getEndDate());
	
		//Strange quirk with Java and displaying RToL languages with dates.
		//When there is a string with mixed RtoL and LtoR characters 
		//if there is .'s separating numbers then the date is not reversed,
		//but if the date is separated by /'s, then the date is reversed.
		String rawBeginDate = MartusFlexidate.toStoredDateFormat(mfd.getBeginDate());
		String beginDate = convertStoredDateToDisplay(rawBeginDate);
		String endDate = convertStoredDateToDisplay(rawEndDate);
			
		String display = beginDate + " - " + endDate;
		if(LanguageOptions.isRightToLeftLanguage())
			display = endDate + " - " + beginDate;
		return display;
	}

	public String formatDateTime(long dateTime)
	{
		if(dateTime == DATE_UNKNOWN)
			return "";
		DateFormat time24hour = new SimpleDateFormat("HH:mm");
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(dateTime);
		
		MultiCalendar datePart = new MultiCalendar(cal);
		String date = toDisplayDateString(datePart);
		
		String time = time24hour.format(cal.getTime());
		if(isRightToLeftLanguage())
			return time + SPACE + date;
		return date + SPACE + time;
	}

	public boolean getAdjustThaiLegacyDates()
	{
		return languageSettings.getAdjustThaiLegacyDates();
	}
	
	public boolean getAdjustPersianLegacyDates()
	{
		return languageSettings.getAdjustPersianLegacyDates();
	}
	
	private boolean isRightToLeftLanguage()
	{
		return rightToLeftLanguages.contains(getCurrentLanguageCode());
	}

	public void addRightToLeftLanguage(String languageCode)
	{
		if(rightToLeftLanguages.contains(languageCode))
			return;
		rightToLeftLanguages.add(languageCode);
	}

	public ChoiceItem[] getLanguageNameChoices()
	{
		return getLanguageNameChoices(ALL_LANGUAGE_CODES);
	}

	public ChoiceItem[] getLanguageNameChoices(String[] languageCodes)
	{
		if(languageCodes == null)
			return null;
		ChoiceItem[] tempChoicesArray = new ChoiceItem[languageCodes.length];
		for(int i = 0; i < languageCodes.length; i++)
		{
			tempChoicesArray[i] =
				new ChoiceItem(languageCodes[i], BurmeseUtilities.getStorable(getLanguageName(languageCodes[i])));
		}
		Arrays.sort(tempChoicesArray);
		return tempChoicesArray;
	}

	public String getMonthLabel(String code)
	{
		return getLabel(getCurrentLanguageCode(), "month", code);
	}

	public String[] getMonthLabels()
	{
		String[] legacyMonthTags = {
				"jan", "feb", "mar", "apr", "may", "jun",
				"jul", "aug", "sep", "oct", "nov", "dec",
		};
		int months = 12;
		String calendarSystem = getCurrentCalendarSystem();
		String[] labels = new String[months];
		for(int i = 0; i < labels.length; ++i)
		{
			String tag = calendarSystem + Integer.toString(i + 1);
			if(calendarSystem.equals(GREGORIAN_SYSTEM))
				tag = legacyMonthTags[i];
			labels[i] = getMonthLabel(tag);
		}
	
		return labels;
	}

	public boolean getSpecialZawgyiFlagForReportRunner()
	{
		return false;
	}

	public static final String ENGLISH = "en";
	public static final String LANGUAGE_OTHER = "?";
	public static final String FRENCH = "fr";
	public static final String SPANISH = "es";
	public static final String RUSSIAN = "ru";
	public static final String THAI = "th";
	public static final String ARABIC = "ar";
	public static final String FARSI = "fa";
	public static final String BURMESE = "bur";
	public static final String[] ALL_LANGUAGE_CODES = {
				LANGUAGE_OTHER, ENGLISH, ARABIC, "arm", 
				"az", "bg", "bn", BURMESE, "km","my","zh", "nl", "eo", FARSI, FRENCH, "de","gu","ha","he","hi","hu",
				"it", "ja","jv","kn","kk","ky","ko","ku","ml","mr","ne","or","pa","ps","pl","pt","ro",RUSSIAN,
				"sr", "sd","si","sq",SPANISH,"ta","tg","te",THAI,"tr","tk","uk","ur","uz","vi"};

	static public final String SPACE = " ";
	static public final long DATE_UNKNOWN = -1;
	
	public static final String GREGORIAN_SYSTEM = "Gregorian";
	public static final String THAI_SYSTEM = "Thai";
	public static final String PERSIAN_SYSTEM = "Persian";
	public static final String AFGHAN_SYSTEM = "Afghan";
	public static final String[] ALL_CALENDAR_SYSTEMS = {GREGORIAN_SYSTEM, THAI_SYSTEM, PERSIAN_SYSTEM, AFGHAN_SYSTEM, };

	public static final String NotTranslatedBeginCharacter = "<";
	public static final String NotTranslatedEndCharacter = ">";
	
	protected Map textResources;
	protected Vector rightToLeftLanguages;
	private DatePreference currentDateFormat;
	private LanguageSettingsProvider languageSettings;
}
