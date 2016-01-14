/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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
package org.martus.meta;

import org.martus.util.LoggerUtil;
import org.martus.util.TestCaseEnhanced;

public class TestLoggerUtil extends TestCaseEnhanced
{
	public TestLoggerUtil(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
	}

	public void tearDown() throws Exception
	{
    	super.tearDown();
    }
	
	public void testBasics()
	{
		try
		{
			int divideByZero = 23/0;
			fail("Should have thrown an exception Why did this work " + divideByZero);
		}
		catch (Exception e)
		{
			String result = LoggerUtil.getStackTrace(e);
			assertContains("java.lang.ArithmeticException: / by zero", result);
			assertContains("at org.martus.meta.TestLoggerUtil.testBasics(", result);
		}
	}
	
}
