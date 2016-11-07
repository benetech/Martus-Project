/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

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
package org.martus.client.bulletinstore.converters;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.util.MultiCalendar;
import org.xml.sax.InputSource;

public class TestBulletinsAsXmlToCsvConverter extends AbstractTestBulletinAsXmlConverters
{
	public TestBulletinsAsXmlToCsvConverter(String name)
	{
		super(name);
	}
	
	public void testDateRangeField() throws Exception
	{
		Bulletin bulletinWithBasicData = new Bulletin(security);
		createEventDateToMatchApp(bulletinWithBasicData);
		String bulletinAsXmlString = exportBulletinAsXml(bulletinWithBasicData);
		
		InputSource xmlAsInputSource = new InputSource(new StringReader(bulletinAsXmlString));
		File newCsvDestinationFile = createTempFileFromName("xxx-BulletinAsCsv.csv");
		newCsvDestinationFile.deleteOnExit();
		BulletinsAsXmlToCsvConverter converter = new BulletinsAsXmlToCsvConverter(xmlAsInputSource, newCsvDestinationFile.getAbsolutePath());
		String errorMessagesDuringParsing = converter.parseAndTranslateFile();
		if (!errorMessagesDuringParsing.isEmpty())
		{
			fail("There should not be any errors during xml to csv conversion?");
			throw new Exception(errorMessagesDuringParsing);
		}
		
		String bulletinAsCsvString = converter.getCsvAsStringOutput();
		HashMap<String, String> csvColumnHeaderToCellValue = createHeaderToValueMap(converter, bulletinAsCsvString);
		assertEquals("Incorrect value for field", "1900-01-01", csvColumnHeaderToCellValue.get("eventdateStart"));
		assertEquals("Incorrect value for field", "1900-01-01", csvColumnHeaderToCellValue.get("eventdateEnd"));
		
		String expectedDateTobeToday = new MultiCalendar().toIsoDateString();
		assertEquals("Incorrect value for field", expectedDateTobeToday, csvColumnHeaderToCellValue.get("entrydate"));
	}

	private HashMap<String, String> createHeaderToValueMap(BulletinsAsXmlToCsvConverter converter, String bulletinAsCsvString)
	{
		String[] csvRows = bulletinAsCsvString.split("\n");
		assertEquals("csv contains incorrect number of rows", 2, csvRows.length);
		String csvHeaderRow = csvRows[0];
		String csvDataRow = csvRows[1];
		
		System.out.println(csvHeaderRow);
		System.out.println(csvDataRow);
		System.out.println();
		
		List<String> splitHeaders = splitIgnoringDelimeterInsideDoubleQuotes(csvHeaderRow);
		List<String> splitValues = splitIgnoringDelimeterInsideDoubleQuotes(csvDataRow); 
		HashMap< String, String> csvColumnHeaderToCellValue = new HashMap<>();
		
		assertEquals("Header and values counts should match?", splitHeaders.size(), splitValues.size());
		for (int index = 0; index < splitHeaders.size(); ++index)
		{
			String columnHeader = splitHeaders.get(index);
			String value = splitValues.get(index);
			
			value = CsvEncoder.decodeValue(value);
			csvColumnHeaderToCellValue.put(columnHeader, value);
		}
		
		return csvColumnHeaderToCellValue;
	}
	
	private List<String> splitIgnoringDelimeterInsideDoubleQuotes(String valueToSPlit)
	{
		List<String> result = new ArrayList<String>();
		int startIndex = 0;
		boolean isInsideQuotes = false;
		int valueLength = valueToSPlit.length();
		for (int index = 0; index < valueLength; ++index) 
		{
		    if (valueToSPlit.charAt(index) == '\"')
		    {
		    	isInsideQuotes = !isInsideQuotes;
		    }
		    
		    boolean isAtLastChar = (index == valueLength - 1);
		    if(isAtLastChar)
		    {
		    	String substring = valueToSPlit.substring(startIndex);
				result.add(substring);
		    }
		    else if (valueToSPlit.charAt(index) == ',' && !isInsideQuotes) 
		    {
		        String substring = valueToSPlit.substring(startIndex, index);
				result.add(substring);
		        startIndex = index + 1;
		    }
		}

		return result;
	}

	private void createEventDateToMatchApp(Bulletin bulletinWithBasicData)
	{
		bulletinWithBasicData.set(BulletinConstants.TAGEVENTDATE, "");
	}
}
