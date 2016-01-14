/* 
Copyright 2005-2009, Foundations of Success, Bethesda, Maryland 
(on behalf of the Conservation Measures Partnership, "CMP") and 
Beneficent Technology, Inc. ("Benetech"), Palo Alto, California. 

This file is part of Miradi

Miradi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License version 3, 
as published by the Free Software Foundation.

Miradi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Miradi.  If not, see <http://www.gnu.org/licenses/>. 
*/
/*2014 modified tests by removing Miradi specific calls*/

package org.miradi.utils;

import java.util.Calendar;
import java.util.Date;

import org.martus.util.TestCaseEnhanced;
import org.miradi.utils.EnhancedJsonArray;
import org.miradi.utils.EnhancedJsonObject;

import com.ibm.icu.text.SimpleDateFormat;

public class TestEnhancedJsonObject extends TestCaseEnhanced
{
	public TestEnhancedJsonObject(String name)
	{
		super(name);
	}
	
	public void testIntegers() throws Exception
	{
		int two = 2;
		int four = 4;
		
		EnhancedJsonObject json = new EnhancedJsonObject();
		json.put("TWO", two);
		json.put("FOUR", four);
		assertEquals("didn't get two?", two, json.get("TWO"));
		assertEquals("didn't get four?", four, json.get("FOUR"));
	}

	public void testMartusAccessTokenResponse() throws Exception
	{
		String tokenTag = "Token";
		String tokenDateTag = "DateCreated";

		 Date date = new Date();
		 Calendar cal = Calendar.getInstance();
		 cal.set(2014,01,15);
		 cal.set(Calendar.HOUR_OF_DAY, 1);
		 cal.set(Calendar.MINUTE, 30);
		 cal.set(Calendar.SECOND, 45);
		 cal.set(Calendar.MILLISECOND, 0);
		 date = cal.getTime();

		String token = "111111";
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
		String tokendate = formatter.format(date);
		EnhancedJsonObject jsonInner = new EnhancedJsonObject();
		jsonInner.put(tokenTag, token);
		jsonInner.put(tokenDateTag, tokendate);

		String martusResponseTag = "MartusAccessTokenResponse";
		EnhancedJsonObject jsonOutter = new EnhancedJsonObject();
		jsonOutter.put(martusResponseTag, jsonInner);
		EnhancedJsonObject jsonRetrievedInnter = (EnhancedJsonObject) jsonOutter.get(martusResponseTag);
		
		assertEquals("didn't get inner JSON response?", jsonInner, jsonRetrievedInnter);
		assertEquals("didn't get token?", token, jsonRetrievedInnter.get(tokenTag));
		assertEquals("didn't get token date?", tokendate, jsonRetrievedInnter.get(tokenDateTag));
	}
	
	
	public void testEnhancedJsonArray() throws Exception
	{
		EnhancedJsonObject small = new EnhancedJsonObject();
		small.put("Id", 14);
		
		EnhancedJsonArray array = new EnhancedJsonArray();
		array.put(small);
		
		EnhancedJsonObject bigObject = new EnhancedJsonObject();
		bigObject.put("Array", array);
		
		EnhancedJsonArray gotArray = bigObject.getJsonArray("Array");
		EnhancedJsonObject gotSmall = gotArray.getJson(0);
		assertEquals(gotSmall.get("Id"), small.get("Id"));
	}
}
