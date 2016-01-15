/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

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
package org.martus.common.utilities;

import org.martus.util.TestCaseEnhanced;

public class TestCrLfSanitizer extends TestCaseEnhanced
{
	public TestCrLfSanitizer(String name)
	{
		super(name);
	}
	
	public void testStripCrChars() {
		verifyStripCrChars(null, null);
		verifyStripCrChars("", "");
		verifyStripCrChars("some text", "some text");
		verifyStripCrChars("some text\n new line", "some text\r\n new line");
		verifyStripCrChars("some text\n new line\n", "some text\n new line\r\n");
		verifyStripCrChars("\nsome text", "\r\nsome text");
	}

	private void verifyStripCrChars(String expectedValue, String valueToSanitize)
	{
		assertEquals("Did not sanitize cr chars?", expectedValue, CrLfSanitizer.sanitize(valueToSanitize));
	}
}
