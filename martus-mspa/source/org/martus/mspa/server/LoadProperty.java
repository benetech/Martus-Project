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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class LoadProperty 
{

	public LoadProperty()
	{
		props = new Properties();
	}

	public LoadProperty(String propName) 
	{
		props = new Properties();
		FileInputStream inputStream;
		try
		{
			inputStream = new FileInputStream(propName);
			if (inputStream.available() >0)
				props.load(inputStream);
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e1)
		{
				// TODO Auto-generated catch block
				e1.printStackTrace();
		}	
	}
	
	public void setProperty(String key, String value)
	{
		props.setProperty(key, value);		
	}

	public String getValue(String key)
	{
		return (String) props.get(key);
	}
		

	
	public void writePropertyFile(String propName) 
	{
		try
		{
			props.store(new FileOutputStream(propName), null);
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readPropertyFile(String propName) 
	{
		try
		{
			props.load(new FileInputStream(propName));
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	File file;
	Properties props;
}
