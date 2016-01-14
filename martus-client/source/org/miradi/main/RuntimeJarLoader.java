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

/* Modified extensively by the Martus team. Modifications are 
 * released under the license above, but are Copyright 
 * (C) 2012, Beneficent Technology, Inc. (The Benetech Initiative).*/

package org.miradi.main;

import java.io.File;

public class RuntimeJarLoader
{
	public static void addJarsInSubdirectoryToClasspath(File thirdPartyDirectory) throws Exception
	{
		if(!thirdPartyDirectory.exists())
			return;

		addJarsInSubdirectoryToClasspath(thirdPartyDirectory, thirdPartyDirectory.list());
	}
	
	public static void addJarsInSubdirectoryToClasspath(File directory, String[] jarNames) throws Exception
	{
		if(jarNames == null)
			return;

		for(int i = 0; i < jarNames.length; ++i)
		{
			String jarName = jarNames[i];
			if(!jarName.endsWith(".jar"))
				continue;
			
			File jarFile = new File(directory, jarName);
			if(jarFile.exists())
			{
				ClassPathHacker.addFile(jarFile);
				System.out.println("Added jar to classpath: " + jarName);
			}
			else
			{
				System.err.println("WARNING: Cannot find: " + jarFile);
			}

		}
	}

}
