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
package org.martus.server.forclients;

import org.apache.xmlrpc.webserver.ConnectionServerWithIpTracking;
import org.martus.util.MultiCalendar;


public class AuthorizeLogEntry
{
	public AuthorizeLogEntry(String line)
	{
		super();
		date = getDateFromLineEntry(line);
		publicCode = getPublicCodeFromLineEntry(line);
		ip = getIpFromLineEntry(line);
		groupName = getGroupNameFromLineEntry(line);
	}
	
	public AuthorizeLogEntry(String publicCodeToUse, String groupNameToUse)
	{
		date = new MultiCalendar().toIsoDateString();
		publicCode = publicCodeToUse;
		groupName = groupNameToUse;
		ip = getIpAddress();
	}
	
	public String getDate()
	{
		return date;
	}

	public String getPublicCode()
	{
		return publicCode;
	}

	public String getIp()
	{
		return ip;
	}
	
	public String getGroupName()
	{
		return groupName;
	}
	
	public String toString()
	{
		return date + FIELD_DELIMITER + publicCode + FIELD_DELIMITER + ip + FIELD_DELIMITER + groupName;
	}
	
	private String getIpAddress()
	{
		String rawIp = ConnectionServerWithIpTracking.getRemoteHostAddressAndPort();
		rawIp = extractIpAddressOnly(rawIp);
		return rawIp;
	}
	
	static public String getDateFromLineEntry(String lineEntry)
	{
		return getField(0, lineEntry);
	}

	static public String getPublicCodeFromLineEntry(String lineEntry)
	{
		return getField(1, lineEntry);
	}
	
	static public String getIpFromLineEntry(String lineEntry)
	{
		return getField(2, lineEntry);
	}
	
	static public String getGroupNameFromLineEntry(String lineEntry)
	{
		return getField(3, lineEntry);
	}
	
	static public String extractIpAddressOnly(String rawIp)
	{
		if(rawIp != null)
		{
			int index = rawIp.indexOf(":");
			if(index > 0)
				rawIp = rawIp.substring(0, index);
		}
		return rawIp;
	}

	static private String getField(int fieldNumber, String lineEntry)
	{
		String[] fields = lineEntry.split(FIELD_DELIMITER);
		return fields[fieldNumber];
	}
	
	public static final String FIELD_DELIMITER = "\t";
	
	private String date;
	private String publicCode;
	private String groupName;
	private String ip;
}
