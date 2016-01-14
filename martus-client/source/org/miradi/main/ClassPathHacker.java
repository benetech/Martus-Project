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
package org.miradi.main;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

// This class is based on a forum posting:
//   http://forum.java.sun.com/thread.jspa?threadID=300557

public class ClassPathHacker
{
	public static void addFile(File f) throws Exception
	{
		addURL(f.toURI().toURL());
	}

	public static void addURL(URL urlToAdd) throws Exception
	{
		final Object[] urlAsObjectArray = new Object[] { urlToAdd };

		final URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		final Class<URLClassLoader> systemClassLoaderClass = URLClassLoader.class;

		final Class[] parameters = new Class[] { URL.class };
		final Method method = systemClassLoaderClass.getDeclaredMethod("addURL", parameters);
		method.setAccessible(true);
		method.invoke(systemClassLoader, urlAsObjectArray);
	}

}
