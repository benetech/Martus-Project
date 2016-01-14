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
package org.martus.amplifier.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.martus.util.DirectoryUtils;
import org.martus.util.StreamCopier;
import org.martus.util.TestCaseEnhanced;

public abstract class AbstractAmplifierTestCase extends TestCaseEnhanced
{
	public AbstractAmplifierTestCase(String name)
	{
		super(name);
	}
	
	protected void setUp() throws Exception
	{
		super.setUp();
		basePath = createTempDirectory().getAbsolutePath();
	}
	
	protected void tearDown() throws Exception
	{
		DirectoryUtils.deleteEntireDirectoryTree(new File(basePath));
		super.tearDown();
	}
	
	protected String getTestBasePath()
	{
		return basePath;
	}
	
	protected String getTestBulletinPath()
	{
		return basePath + File.separator + "bulletins";
	}
	
	protected static String inputStreamToString(InputStream in) 
		throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new StreamCopier().copyStream(in, out);
		return out.toString("UTF-8");
	}
	
	protected String basePath;
}
