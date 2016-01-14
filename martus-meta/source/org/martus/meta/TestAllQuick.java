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
package org.martus.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.martus.client.test.TestClient;
import org.martus.clientside.test.TestClientside;
import org.martus.common.MartusLogger;
import org.martus.common.TestFieldCollectionMemoryUsage;
import org.martus.common.test.TestCommon;
import org.martus.server.main.TestServer;
import org.martus.util.TestUtil;

public class TestAllQuick extends java.lang.Object 
{
    public TestAllQuick() 
    {
    }

	public static void main (String[] args) 
	{
		int loop = 0;
		if(args.length > 0 && args[0].equalsIgnoreCase("LOOP"))
			loop = 1;
		do
		{
			if(loop>0)
			{
				System.out.println("\nTo exit tests type Control + C.");
				System.out.println("Loop:"+loop);
				loop++;
			}
			runTests();
		}while(loop>0);
	}

	public static void runTests () 
	{
		junit.textui.TestRunner.run (suite());
	}

	public static Test suite ( ) 
	{
		// Turn down logging verbosity...
		// should be in main() or runTests(), but neither of those get 
		// hit if you run the tests from inside eclipse
		Logger.global.setLevel(Level.WARNING);
		MartusLogger.disableLogging();
		
		TestSuite suite= new TestSuite("All Martus Tests");

		suite.addTest(TestMetaQuick.suite());
		
		// shared stuff
		suite.addTest(TestUtil.suite());
		suite.addTest(TestCommon.suite());
		suite.addTest(TestServer.suite());
		suite.addTest(TestClient.suite());
		suite.addTest(TestClientside.suite());
		suite.addTestSuite(TestFieldCollectionMemoryUsage.class);
	    return suite;
	}
}
