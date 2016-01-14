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

package org.martus.common.test;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Arrays;

import org.martus.common.XmlWriterFilter;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.util.TestCaseEnhanced;


public class TestXmlWriterFilter extends TestCaseEnhanced
{
	public TestXmlWriterFilter(String name)
	{
		super(name);
	}

	public void testBasics() throws Exception
	{
		StringWriter stringWriter = new StringWriter();
		XmlWriterFilter filter = new XmlWriterFilter(stringWriter);
		filter.writeStartTag("z");
		filter.writeEndTag("y");
		filter.writeDirect("<&a>");
		filter.writeStartTag(" <&a> ");
		filter.writeEncoded(" <&b ");
		stringWriter.close();

		String result = stringWriter.toString();
		assertEquals("<z></y>\n<&a>< <&a> > &lt;&amp;b ", result);
	}

	public void testSigningGood() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();

		String expectedText = "<a>\r\ncd\n</a>\n";
		byte[] expectedBytes = expectedText.getBytes();
		ByteArrayInputStream expectedIn = new ByteArrayInputStream(expectedBytes);
		byte[] expectedSig = security.createSignatureOfStream(expectedIn);
		expectedIn.close();

		StringWriter stringWriter = new StringWriter();
		XmlWriterFilter filter = new XmlWriterFilter(stringWriter);
		filter.writeDirect("<!--comment-->\n");
		filter.startSignature(security);
		filter.writeStartTag("a");
		filter.writeEncoded("\r\ncd\n");
		filter.writeEndTag("a");
		byte[] sig = filter.getSignature();
		assertNotNull("null sig?", sig);
		assertEquals("bad sig?", true, Arrays.equals(expectedSig, sig));
		filter.writeDirect("more stuff that isn't signed");

		try
		{
			filter.getSignature();
			fail("Should have thrown");
		}
		catch (MartusCrypto.MartusSignatureException e)
		{
			// expected exception
		}

	}

	public void testSigningNotInitialized()
	{
		try
		{
			XmlWriterFilter filter = new XmlWriterFilter(new StringWriter());
			filter.getSignature();
			fail("Should have thrown");
		}
		catch (MartusCrypto.MartusSignatureException e)
		{
			// expected exception
		}

	}

}
