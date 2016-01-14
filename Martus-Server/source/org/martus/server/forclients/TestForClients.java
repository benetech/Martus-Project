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

package org.martus.server.forclients;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.martus.common.test.TestMartusServerUtilities;

public class TestForClients
{
	public static void main(String[] args) 
	{
		runTests();
	}

	public static void runTests() 
	{
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() 
	{
		TestSuite suite = new TestSuite("All Server ForClient Tests");

		suite.addTest(new TestSuite(TestAuthorizeLog.class));
		suite.addTest(new TestSuite(TestAuthorizeLogEntry.class));
		suite.addTest(new TestSuite(TestMartusServer.class));
		suite.addTest(new TestSuite(TestMartusServerUtilities.class));
		suite.addTest(new TestSuite(TestServerForClients.class));
		suite.addTest(new TestSuite(TestServerSideNetworkHandler.class));
		suite.addTest((new TestSuite(TestSummaryCollector.class)));

		return suite;
	}
}
