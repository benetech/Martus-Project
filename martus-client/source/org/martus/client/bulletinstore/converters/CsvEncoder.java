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

public class CsvEncoder
{
	public static String encodeValue(String valueToEncode, String delimeter) 
	{
		if (valueToEncode == null)
			return null;
		
		if (valueToEncode.isEmpty())
			return valueToEncode;
			
		if (!valueToEncode.contains(delimeter))
			return valueToEncode;

		if (valueToEncode.startsWith("\"") && valueToEncode.endsWith("\""))
			return valueToEncode;
		
		StringBuffer escapedWrappedValue = new StringBuffer();
		
		String valueWithEscapedDoubleQuotes = valueToEncode.replaceAll("\"", "\"\"");
		escapedWrappedValue.append("\"");
		escapedWrappedValue.append(valueWithEscapedDoubleQuotes);
		escapedWrappedValue.append("\"");
		
		return escapedWrappedValue.toString();
	}
	
	public static String decodeValue(String valueToDecode)
	{
		if (valueToDecode == null)
			return valueToDecode;
		
		if (valueToDecode.isEmpty())
			return valueToDecode;
		
		if (valueToDecode.startsWith("\"") && valueToDecode.endsWith("\""))
		{
			String valueUnwrappedOfQuotes = new String(valueToDecode);
			valueUnwrappedOfQuotes = valueUnwrappedOfQuotes.replaceFirst("\"", "");
			valueUnwrappedOfQuotes = valueUnwrappedOfQuotes.substring(0, valueUnwrappedOfQuotes.length() - 1);
			
			return valueUnwrappedOfQuotes;	
		}
		
		return valueToDecode;
	}
}
