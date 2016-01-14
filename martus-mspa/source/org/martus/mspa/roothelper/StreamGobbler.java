/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.mspa.roothelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.martus.common.MartusLogger;

public class StreamGobbler extends Thread
{
	StreamGobbler(InputStream is, String nameOfStream)
	{
		reader = new BufferedReader(new InputStreamReader(is));					
		textBuffer = new StringBuffer();
		name = nameOfStream;
	}

	public void run()
	{
		try
		{
			if(!getNextLine())
				return;
		}
		catch(IOException ioe)
		{
			MartusLogger.log(name + ": Process stream closed immediately");
			return;
		}
		
		try
		{
			MartusLogger.log(name + ": starting loop");
			while (getNextLine())
				;
			MartusLogger.log(name = ": got null");
		} 
		catch (IOException ioe)
		{
			MartusLogger.logException(ioe);  
		}
	}
	
	private synchronized boolean getNextLine() throws IOException
	{
		String line = reader.readLine();
		if(line == null)
			return false;
		MartusLogger.log(name + ": got: " + line);
		textBuffer.append(line).append("\n");    
		return true;
	}
	
	public synchronized String getTextBuffer()
	{
		return textBuffer.toString();
	}
	
	public synchronized void close() throws IOException
	{
		reader.close();
	}

	BufferedReader reader;
	StringBuffer textBuffer;
	String name;
}
