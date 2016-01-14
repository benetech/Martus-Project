/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.common.database;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;

public class MSPAFileDatabase extends ServerFileDatabase
{
	public MSPAFileDatabase(File directory, MartusCrypto security)
	{
		super(directory, security);
	}

	protected Map getAccountMap()
	{
		// NOTE: MSPAServer always needs to re-read latest account map
		// from disk because the MartusServer may have modified it
		try
		{
			loadAccountMap();
		} 
		catch (Exception e)
		{
			MartusLogger.logError("Exception in getAccountMap");
			MartusLogger.logException(e);
		} 
		return super.getAccountMap();
	}

	synchronized File generateAccount(String accountString) throws IOException, TooManyAccountsException
	{
		throw new RuntimeException("MSPA is not allowed to create accounts!");
	}
	
	
}
