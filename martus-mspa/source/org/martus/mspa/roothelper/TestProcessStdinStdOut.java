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
package org.martus.mspa.roothelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.martus.common.Version;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeWriter;


public class TestProcessStdinStdOut extends TestCaseEnhanced
{
	public TestProcessStdinStdOut(String name)
	{
		super(name);	
		setup();		
	}	
	
	public void setup()
	{		
		try
		{									
			String line2 = "set /P varname=Please enter your name:";
			String line3 = "echo Hi, %varname%";
			String shline1 = "#!/bin/bash";
			String shline2 = "echo Please enter your name:";
			String shline3 = "read varname";			
			String shline4 = "echo Hi, $varname";
			
			StringBuffer script = new StringBuffer();		
			if (Version.isRunningUnderWindows())
			{							
				script.append(line2).append("\n").append(line3).append("\n");		
				tempFile = createTempScript("testscript.bat",script.toString());
			}
			else
			{
				script.append(shline1).append("\n").append(shline2).append("\n").append(shline3).append("\n").append(shline4).append("\n");		
				tempFile = createTempScript("teststdinscript",script.toString());
			}
		}
		catch (IOException e)
		{		
			e.printStackTrace();
		}			
	}
		
	private File createTempScript(String fileName, String contents) throws IOException
	{
		File file = new File(fileName);
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.writeln(contents);
		writer.flush();
		writer.close();
		return file;
	}
	
	public void testReadingProcess()
	{
		Runtime runtime = Runtime.getRuntime();		
		Process process;
		try
		{
			String stdinOfProcess = "John Smith\r";
			if (Version.isRunningUnderWindows())
				process = runtime.exec("cmd /c "+tempFile.getName());
			else
				process = runtime.exec(tempFile.getName());

			BufferedReader buffIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader buffErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			BufferedWriter buffOut = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));			
			buffOut.write(stdinOfProcess);
			buffOut.flush();

			try
			{				
				String line=null;
				String catchStdin = null;				
				while ( (line = buffIn.readLine()) != null)
				{													
					if (!line.startsWith("echo"))
						catchStdin = line;						
				}					 
				assertEquals("greeting?", "Hi, "+ stdinOfProcess.trim(), catchStdin );
			} 
			catch (IOException ioe)
			{
				ioe.printStackTrace();  
			}			

			if (process.waitFor() != 0)
			{	
				StringBuffer rt = new StringBuffer();									
				String line=null;				
				while ( (line = buffErr.readLine()) != null)
					rt.append(line).append("\n");    					
				System.out.println(rt.toString());
				
			}
			
			buffOut.close();
			buffIn.close();
			buffErr.close();
			tempFile.delete();		
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	File tempFile;	
}
