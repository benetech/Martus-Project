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
/*2014 Martus Team removed Miradi specific code*/
package org.miradi.utils;

import java.text.ParseException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class EnhancedJsonObject extends JSONObject
{
        public EnhancedJsonObject()
        {
                super();
        }
       
        public EnhancedJsonObject(JSONObject json)
        {
                fillFrom(json);
        }

        private void fillFrom(JSONObject json)
        {
                removeAll();
                Iterator iter = json.keys();
                while(iter.hasNext())
                {
                        String key = (String)iter.next();
                        put(key, json.get(key));
                }
        }
       
        public EnhancedJsonObject(String jsonString) throws ParseException
        {
                super(handleEmptyString(jsonString));
        }

        // Don't use this!
        @Override
        public JSONObject getJSONObject(String key)
        {
                throw new RuntimeException("Use getJson instead!");
        }
       
        // Don't use this!
        @Override
        public JSONObject optJSONObject(String key)
        {
                throw new RuntimeException("Use optJson instead!");
        }
       
        public EnhancedJsonObject getJson(String key)
        {
                return new EnhancedJsonObject(super.getJSONObject(key));
        }
       
        // Override to return empty object instead of null if missing
        public EnhancedJsonObject optJson(String key)
        {
                if(!has(key))
                        return new EnhancedJsonObject();
               
                return getJson(key);
        }

        // Override to return empty array instead of null if missing
        @Override
        public JSONArray optJSONArray(String key)
        {
                throw new RuntimeException("Use optJsonArray instead!");
        }
       
        public EnhancedJsonArray optJsonArray(String key)
        {
                if (has(key))
                        return getJsonArray(key);
               
                return new EnhancedJsonArray();
        }
       
        @Override
        public JSONArray getJSONArray(String key)
        {
                throw new RuntimeException("Use getJsonArray instead!");
        }
       
        public EnhancedJsonArray getJsonArray(String key)
        {
                return new EnhancedJsonArray(super.getJSONArray(key));
        }
       
        @Override
        public JSONObject put(String tag, Object value)
        {
                if(value instanceof String ||
                                value instanceof JSONObject ||
                                value instanceof JSONArray ||
                                value instanceof Integer ||
                                value instanceof Boolean ||
                                value instanceof Double)
                        return super.put(tag, value);
               
                throw new RuntimeException("This cannot be used for generic Objects: " + value.getClass());
        }

        public void removeAll()
        {
                Iterator iter = keys();
                while(iter.hasNext())
                {
                        String key = (String)iter.next();
                        remove(key);
                }
        }
       
        @Override
        public boolean equals(Object rawOther)
        {
                if(! (rawOther instanceof EnhancedJsonObject))
                        return false;
               
                EnhancedJsonObject other = (EnhancedJsonObject)rawOther;
               
                // NOTE: we would love to access the hashmap directly,
                // but they used default permissions instead of protected
               
                Iterator iter = keys();
                if(length() != other.length())
                        return false;
               
                while(iter.hasNext())
                {
                        String key = (String)iter.next();
                        if(!other.has(key))
                                return false;
                        Object thisValue = get(key);
                        Object otherValue = other.get(key);
                        if(thisValue.equals(otherValue))
                                continue;
                       
                        if(thisValue instanceof String)
                        {
                                try
                                {
                                        EnhancedJsonObject thisJson = new EnhancedJsonObject((String)thisValue);
                                        EnhancedJsonObject otherJson = new EnhancedJsonObject((String)otherValue);
                                        if(!thisJson.equals(otherJson))
                                                return false;
                                }
                                catch(Exception e)
                                {
                                        return false;
                                }
                        }
                }
               
                return true;
        }

        //NOTE: could be improved at some point, but this works
        @Override
        public int hashCode()
        {
                return length();
        }
       
        private static String handleEmptyString(String possiblyEmptyJsonString)
        {
                if(possiblyEmptyJsonString.length() == 0)
                        return "{}";
                return possiblyEmptyJsonString;
        }
       
        public static Vector<String> convertToVectorOfTypeString(Iterator iterator)
        {
                Vector<String> vector = new Vector<String>();
                while(iterator.hasNext())
                {
                        vector.add(iterator.next().toString());
                }
               
                return vector;
        }
        
        public Vector<String> getSortedKeys()
        {
                Iterator keys = keys();
                Vector<String> sortedKeys = convertToVectorOfTypeString(keys);
                Collections.sort(sortedKeys);
                return sortedKeys;
        }
}
