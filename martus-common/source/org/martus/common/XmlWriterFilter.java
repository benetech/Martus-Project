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

package org.martus.common;

import java.io.IOException;
import java.io.Writer;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.SignatureEngine;
import org.martus.util.xml.XmlUtilities;

public class XmlWriterFilter
{
	public XmlWriterFilter(Writer writerToUse)
	{
		writer = writerToUse;
	}

	public void writeStartTag(String text) throws IOException
	{
		writeDirect(XmlUtilities.createStartElement(text));
	}

	public void writeEndTag(String text) throws IOException
	{
		writeDirect(XmlUtilities.createEndTag(text));
		writeDirect("\n");
	}

	public void writeEncoded(String text) throws IOException
	{
		writeDirect(XmlUtilities.getXmlEncoded(text));
	}

	public void writeDirect(String s) throws IOException
	{
		if(engine != null)
		{
			try
			{
				byte[] bytes = s.getBytes("UTF-8");
				engine.digest(bytes);
			}
			catch(Exception e)
			{
				throw new IOException("Signature Exception: " + e.getMessage());
			}
		}
		writer.write(s);
	}

	public void startSignature(MartusCrypto sigGenToUse) throws
				MartusCrypto.MartusSignatureException
	{
		try
		{
			engine = SignatureEngine.createSigner(sigGenToUse.getKeyPair());
		}
		catch (Exception e)
		{
			throw new MartusCrypto.MartusSignatureException();
		}
	}

	public byte[] getSignature() throws
				MartusCrypto.MartusSignatureException
	{
		try
		{
			byte[] sig = engine.getSignature();
			engine = null;
			return sig;
		}
		catch(Exception e)
		{
			throw new MartusCrypto.MartusSignatureException();
		}
	}

	private Writer writer;
	private SignatureEngine engine;
}