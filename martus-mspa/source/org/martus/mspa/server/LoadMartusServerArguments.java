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
package org.martus.mspa.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;



public class LoadMartusServerArguments extends LoadProperty
{
	public LoadMartusServerArguments()
	{
		super();
	}
	
	public LoadMartusServerArguments(String propFile)
	{
		super(propFile);
	}		
	
	public String getListenerIP()
	{
		return getValue(LISTENER_IP);
	}
	
	public String getPassword()
	{
		return getValue(PASSWORD);
	}
	
	public String getAmplifierIP()
	{
		return getValue(AMPLIFIER_IP);
	}
	
	public String getMinutes()
	{
		return getValue(AMPLIFIER_INDEXING_MINUTES);		
	}
	
	public boolean getAmplifierStatus()
	{
		String status = getValue(AMPLIFIER);
		return status.equalsIgnoreCase("yes")?true:false;			
	}
	
	public boolean getClientListenerStatus()
	{
		String status = getValue(CLIENT_LISTENER);
		return status.equalsIgnoreCase("yes")?true:false;
	}
	
	public boolean getMirrorListenerStatus()
	{
		String status = getValue(MIRROR_LISTENER);
		return status.equalsIgnoreCase("yes")?true:false;
	}
	
	public boolean getAmplifierListenerStatus()
	{
		String status = getValue(AMPLIFIER_LISTENER);
		return status.equalsIgnoreCase("yes")?true:false;
	}	
	
	public Vector convertToVector()
	{
		Vector entries = new Vector();
		entries.add(LISTENER_IP+"="+getListenerIP());
		entries.add(PASSWORD+"="+getPassword());
		entries.add(AMPLIFIER_IP+"="+getAmplifierIP());
		entries.add(AMPLIFIER_INDEXING_MINUTES+"="+getMinutes());
		entries.add(AMPLIFIER+"="+getValue(AMPLIFIER));
		entries.add(CLIENT_LISTENER+"="+getValue(CLIENT_LISTENER));
		entries.add(MIRROR_LISTENER+"="+getValue(MIRROR_LISTENER));
		entries.add(AMPLIFIER_LISTENER+"="+getValue(AMPLIFIER_LISTENER));
		
		return entries;	
	}
	
	public void convertFromVector(Vector entries)
	{
		int index;
		for (int i=0;i<entries.size();++i)
		{
			String entry = (String) entries.get(i);
			index = entry.indexOf('=');
			String key = entry.substring(0,index);
			String value = entry.substring(index+1);
			setProperty(key, value);			
		}		
	}
	
	public String toString()
	{
		StringBuffer argsLine = new StringBuffer();		
		ArrayList list = Collections.list(props.propertyNames());
		Collections.reverse(list);
		for (int i=0;i<list.size();++i)
		{
			String name = (String)list.get(i);
			String value = (String) props.get(name);
			if (name.equals(LISTENER_IP) || 
				name.equals(AMPLIFIER_IP) ||
				name.equals(AMPLIFIER_INDEXING_MINUTES))
			{
				argsLine.append("--").append(name).append("=").append(value).append(" ");
				continue;
			}
							
			if (name.equals(PASSWORD))
			{
				if (value.equalsIgnoreCase("yes"))
					argsLine.append("--password").append(" ");
				else
					argsLine.append("--nopassword").append(" ");
				continue;
			}	
			
			if (value.equalsIgnoreCase("yes"))
				argsLine.append("--").append(name).append(" ");	
		}
		return argsLine.toString().trim();
	}

	public static final String LISTENER_IP = "listener-ip";
	public static final String PASSWORD = "password";
	public static final String AMPLIFIER_IP = "amplifier-ip";
	public static final String AMPLIFIER_INDEXING_MINUTES = "amplifier-indexing-minutes";
	public static final String AMPLIFIER = "amplifier";
	public static final String CLIENT_LISTENER ="client-listener";
	public static final String MIRROR_LISTENER = "mirror-listener";	
	public static final String AMPLIFIER_LISTENER = "amplifier-listener";
	
}
