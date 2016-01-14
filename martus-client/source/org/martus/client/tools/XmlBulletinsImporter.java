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
package org.martus.client.tools;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Vector;

import org.martus.common.FieldSpecCollection;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.util.UnicodeReader;

public class XmlBulletinsImporter
{
	public XmlBulletinsImporter(MartusCrypto security, InputStream xmlIn) throws Exception
	{
		this(security, xmlIn, null);
	}
	
	public XmlBulletinsImporter(MartusCrypto security, InputStream xmlIn, File baseAttachmentsDirectory) throws Exception
	{
		UnicodeReader reader = new UnicodeReader(xmlIn);
		bulletinsLoader = new XmlBulletinsFileLoader(security, baseAttachmentsDirectory);
		try
		{
			bulletinsLoader.allowSpaceOnlyCustomLabels = true;
			bulletinsLoader.parse(reader);
			if(bulletinsLoader.didFieldSpecVerificationErrorOccur())
				throw new FieldSpecVerificationException(bulletinsLoader.getErrors());
		}
		catch(Exception e)
		{
			if(bulletinsLoader.getLoadedVersion() == 0)
				throw(e);
			if(bulletinsLoader.isXmlVersionOlder())
				throw new XmlFileVersionTooOld(e);
			if(bulletinsLoader.isXmlVersionNewer())
				throw new XmlFileVersionTooNew(e);
			throw(e);
		}
		finally
		{
			reader.close();
		}
	}
	
	public class FieldSpecVerificationException extends Exception
	{
		public FieldSpecVerificationException(Vector errors)
		{
			verificationErrors = errors;
		}
		public Vector getErrors()
		{
			return verificationErrors;
		}
		Vector verificationErrors;
	}
	
	public Bulletin[] getBulletins()
	{
		return bulletinsLoader.getBulletins();
	}
	
	public boolean isXmlVersionOlder()
	{
		return bulletinsLoader.isXmlVersionOlder();
	}
	
	public boolean isXmlVersionNewer()
	{
		return bulletinsLoader.isXmlVersionNewer();
	}
	
	public HashMap getMissingAttachmentsMap()
	{
		return bulletinsLoader.getMissingAttachmentsMap();
	}

	//These are currently used in tests, I think its good to keep them for now
	public FieldSpecCollection getMainFieldSpecs()
	{
		return bulletinsLoader.mainFields.getSpecs();
	}

	public FieldSpecCollection getPrivateFieldSpecs()
	{
		return bulletinsLoader.privateFields.getSpecs();
	}
	
	public HashMap getFieldTagValuesMap()
	{
		return bulletinsLoader.fieldTagValuesMap;
	}

	XmlBulletinsFileLoader bulletinsLoader;
}
