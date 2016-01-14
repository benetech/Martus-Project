/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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
package org.martus.client.reports;

import org.json.JSONArray;
import org.json.JSONObject;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.MiniFieldSpec;

public class ReportAnswers
{
	public ReportAnswers(ReportType typeToUse, MiniFieldSpec[] specsToUse, MiniLocalization localizationToUse)
	{
		version = EXPECTED_VERSION;
		type = typeToUse;
		specs = specsToUse;
		languageCode = localizationToUse.getCurrentLanguageCode();
	}
	
	public ReportAnswers(JSONObject json)
	{
		version = json.getInt(TAG_VERSION);
		type = ReportType.createFromString(json.getString(TAG_TYPE));
		JSONArray jsonSpecs = json.getJSONArray(TAG_SPECS);
		specs = new MiniFieldSpec[jsonSpecs.length()];
		for(int i = 0; i < specs.length; ++i)
			specs[i] = new MiniFieldSpec(jsonSpecs.getJSONObject(i));
		languageCode = json.optString(TAG_LANGUAGE, MiniLocalization.LANGUAGE_OTHER);
	}
	
	public int getVersion()
	{
		return version;
	}
	
	public boolean isPageReport()
	{
		return type.isPage();
	}
	
	public boolean isTabularReport()
	{
		return type.isTabular();
	}
	
	public MiniFieldSpec[] getSpecs()
	{
		return specs;
	}
	
	public String getLanguageCode()
	{
		return languageCode;
	}
	
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		json.put(TAG_JSON_TYPE, JSON_TYPE);
		json.put(TAG_VERSION, EXPECTED_VERSION);
		json.put(TAG_TYPE, type.toString());
		JSONArray jsonSpecs = new JSONArray();
		for(int i = 0; i < specs.length; ++i)
			jsonSpecs.put(specs[i].toJson());
		json.put(TAG_SPECS, jsonSpecs);
		json.put(TAG_LANGUAGE, getLanguageCode());
		return json;
	}
	
	public static class ReportType
	{
		public static ReportType createFromString(String type)
		{
			if(type.equals(ReportTypePage.TYPE_STRING))
				return PAGE;
			if(type.equals(ReportTypeTabular.TYPE_STRING))
				return TABULAR;
			
			throw new RuntimeException("Unknown report type: " + type);
		}
		
		public boolean isTabular()
		{
			return false;
		}
		
		public boolean isPage()
		{
			return false;
		}
		
		public final static ReportType PAGE = new ReportTypePage();
		public final static ReportType TABULAR = new ReportTypeTabular();
	}
	
	public static class ReportTypeTabular extends ReportType
	{
		public boolean isTabular()
		{
			return true;
		}
		
		public String toString()
		{
			return TYPE_STRING;
		}

		public static final String TYPE_STRING = "Tabular";
	}
	
	public static class ReportTypePage extends ReportType
	{
		public boolean isPage()
		{
			return true;
		}

		public String toString()
		{
			return TYPE_STRING;
		}

		public static final String TYPE_STRING = "Page";
	}
	
	public final static String TAG_JSON_TYPE = "JsonType";
	public final static String TAG_TYPE = "ReportType";
	public final static String TAG_VERSION = "Version";
	public final static String TAG_SPECS = "Specs";
	public final static String TAG_LANGUAGE = "Language";
	
	public final static String JSON_TYPE = "ReportAnswers";
	public final static int EXPECTED_VERSION = 9;
	public final static ReportType PAGE_REPORT = ReportType.PAGE;
	public final static ReportType TABULAR_REPORT = ReportType.TABULAR;
	
	private int version;
	private ReportType type;
	private MiniFieldSpec[] specs;
	private String languageCode;
}
