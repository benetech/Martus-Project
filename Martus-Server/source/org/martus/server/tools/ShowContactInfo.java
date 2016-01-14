/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2007, Beneficent
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

package org.martus.server.tools;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ShowContactInfo
{
	public static void main(String[] args)
	{
		File contactInfoFile = null;
		
		for (int i = 0; i < args.length; i++)
		{
			if(args[i].startsWith("--field-names"))
			{
				System.out.println("author\torganization\temail\twebpage\tphone\taddress");
				System.out.flush();
				System.exit(0);
			}
			
			if(args[i].startsWith("--file"))
			{
				contactInfoFile = new File(args[i].substring(args[i].indexOf("=")+1));
			}
		}
		
		if(contactInfoFile == null)
		{
			System.err.println("\nUsage:\n ShowContactInfo --file=<pathToContactFile.dat>");
			System.exit(2);
		}
		
		if(!contactInfoFile.isFile() || !contactInfoFile.exists())
		{
			System.err.println("Error: " + contactInfoFile.getAbsolutePath() + " is not a file" );
			System.err.flush();
			System.exit(3);
		}
		
		StringBuffer buffer = new StringBuffer();
		FileInputStream inputStream = null;
		DataInputStream in = null;
		try
		{
			inputStream = new FileInputStream(contactInfoFile);

			in = new DataInputStream(inputStream);
			
			in.readUTF(); //ignored
			int dataSize = in.readInt();
			int count = 0;
			
			while(count < dataSize)
			{
				buffer.append( in.readUTF().replaceAll("\n", "|") + "\t");
				count++;
			}
		}
		catch(EOFException ignored)
		{
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e );
			System.err.flush();
			System.exit(3);
		}
		finally
		{
			try
			{
				if(in != null )
					in.close();
					
				if(inputStream != null )
					inputStream.close();
			}
			catch(IOException ignored)
			{
			}
		}
		
		System.out.println(buffer);
	}
}
