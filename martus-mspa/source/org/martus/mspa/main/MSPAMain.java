/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2004-2007, Beneficent
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


package org.martus.mspa.main;

import java.awt.Toolkit;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.martus.common.MartusLogger;
import org.martus.mspa.client.core.MSPAClient;


public class MSPAMain
{
	public static void main(String[] args)
	{					
		final String javaVersion = System.getProperty("java.version");
		final String minimumJavaVersion = "1.4.1";
		
		if(javaVersion.compareTo(minimumJavaVersion) < 0)
		{
			final String errorMessage = "Requires Java version " + minimumJavaVersion + " or later!";
			System.out.println(errorMessage);
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(2);
		}

		if(Arrays.asList(args).contains("--highport"))
		{
			MartusLogger.log("Using port 9984");
			MSPAClient.DEFAULT_PORT = 9984;
		}
		
		try
		{		
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());										
			UiMainWindow window = new UiMainWindow();
			if(!window.run())
				System.exit(0);													
							
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}				
	}	
	

}
