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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.martus.amplifier.attachment.test.TestAllAttachment;
import org.martus.amplifier.common.test.TestAllCommon;
import org.martus.amplifier.datasynch.test.TestAllDataSynch;
import org.martus.amplifier.lucene.test.TestAllLucene;
import org.martus.amplifier.main.test.TestAllMain;
import org.martus.amplifier.network.test.TestAllNetwork;
import org.martus.amplifier.presentation.test.TestAllPresentation;
import org.martus.amplifier.search.test.TestAllSearch;
import org.martus.amplifier.velocity.test.TestAllVelocity;

public class TestAllAmplifier extends TestSuite
{
    public TestAllAmplifier() 
    {
    }

	public static void main (String[] args) 
	{
		runTests();
	}

	public static void runTests () 
	{
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite ( ) 
	{
		TestSuite suite= new TestSuite("All Martus Amplifier Tests");
		
		// example of a test suite
		suite.addTest(TestAllAttachment.suite());
		
		// FIXME: as of 2004-10-20, a DataSynch test will FAIL if
		// TestAllDataSynch is after TestaAllLucene, but will pass 
		// if they are in the opposite order.
		// I don't have time right now to figure out why, but we 
		// should really resolve it at some point. kbs.
		suite.addTest(TestAllDataSynch.suite());
		suite.addTest(TestAllLucene.suite());
		
		suite.addTest(TestAllPresentation.suite());
		suite.addTest(TestAllMain.suite());
		suite.addTest(TestAllCommon.suite());
		suite.addTest(TestAllSearch.suite());
		suite.addTest(TestAllNetwork.suite());
		suite.addTest(TestAllVelocity.suite());
	    return suite;
	}
}
