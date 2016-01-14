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
package org.martus.client.tools;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.UnicodeReader.BOMNotFoundException;


public class BuildHelp
{
	public static void main(String[] args) throws IOException, BOMNotFoundException
	{
		if(args.length != 1)
		{
			System.out.println("BuildHelp");
			System.out.println("");
			System.out.println("Usage:java BuildHelp xx");
			System.out.println("      where xx is the language code of the help files to buld.");
			System.out.println("Before running this program there should already be a file in the same directory as this program named help-xx.txt (in UTF-8).");
			System.out.println("This file is generated from martus_user_guide_xx.doc file, one saved as a UTF-8 text file, and stip out everything before the table of contents, including the title table of contents.");
			System.out.println("");
			System.out.println("This will create the on-line help files for Martus.");
			System.out.println("The files created will be MartusHelp-xx.txt and MartusHelpTOC-xx.txt");
			System.out.println("These two new files will then be placed in org.martus.client.swingui");
			System.exit(1);
		}
		String languageCode = args[0];
		File helpTxt = new File(("help-" + languageCode + ".txt"));
		
		if(!helpTxt.exists())
		{
			System.out.println("The file " + helpTxt.getAbsolutePath() + " does not exist.");
			System.exit(2);
		}

		File martusHelp = new File(("MartusHelp-" + languageCode + ".txt"));
		File martusHelpTOC = new File(("MartusHelpTOC-" + languageCode + ".txt"));
		martusHelp.delete();
		martusHelpTOC.delete();
		
		UnicodeReader in = new UnicodeReader(helpTxt);
		in.skipBOM();
		UnicodeWriter out = new UnicodeWriter(martusHelpTOC);
		out.writeBOM();

		Vector tableOfContents = new Vector();
		
		while(true)
		{	
			String line = in.readLine().trim();
			if(line.length()==0)
				break;
			int lastWordPos = line.lastIndexOf('\t');
			if(lastWordPos == -1)
				continue;
			String lineWithoutPage = line.substring(0,lastWordPos).trim(); 
			out.writeln(lineWithoutPage);
			tableOfContents.add(lineWithoutPage);
		}
		in.close();
		out.close();

		in = new UnicodeReader(helpTxt);
		out = new UnicodeWriter(martusHelp);
		out.writeBOM();
		
		//Skip past TOC
		while(true)
		{	
			String line = in.readLine().trim();
			if(line.length()==0)
				break;
		}
	
		while(true)
		{	
			String line = in.readLine();
			if(line == null)
				break;
			boolean inTable = false;
			int i = 0;
			for(; i < tableOfContents.size(); ++i)
			{
				if(((String)tableOfContents.get(i)).equalsIgnoreCase(line.trim()))
				{	
					inTable = true;
					break;
				}					
			}
			
			if(inTable)
			{	
				out.writeln("--------");
				out.writeln((String)tableOfContents.get(i));
			}
			else
				out.writeln(line);
		}
		in.close();
		out.close();

		
		System.out.println("Done!");
		System.exit(0);
	}
}
