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
package org.martus.server.forclients;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.common.utilities.MartusServerUtilities.MartusSignatureFileAlreadyExistsException;
import org.martus.common.utilities.MartusServerUtilities.MartusSignatureFileDoesntExistsException;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;


public class AuthorizeLog
{
	public AuthorizeLog(MartusCrypto securityToUse, LoggerInterface loggerToUse, File authorizeLogFileToUse)
	{
		super();
		authorizeLogFile = authorizeLogFileToUse;
		logger = loggerToUse;
		security = securityToUse;
		authorizedClients = new Vector();
	}
	
	public void loadFile() throws IOException, ParseException, MartusSignatureFileDoesntExistsException, FileVerificationException
	{
		authorizedClients.removeAllElements();
		try
		{
			UnicodeReader reader = new UnicodeReader(authorizeLogFile);
			String line = null;
			while( (line = reader.readLine()) != null)
			{
				if(line.trim().length() == 0)
					logger.logWarning("Found blank line in " + authorizeLogFile.getPath());
				else
					add(new AuthorizeLogEntry(line));
			}
			reader.close();
			
		}
		catch (FileNotFoundException e)
		{
			logger.logWarning("No authorizeLog.txt file found:" + authorizeLogFile.getPath());
			return;
		}
		MartusServerUtilities.verifyFileAndLatestSignatureOnServer(authorizeLogFile, security);
	}
	
	private void add(AuthorizeLogEntry authorizedClient)
	{
		authorizedClients.add(authorizedClient);
	}
	
	public void appendToFile(AuthorizeLogEntry authorizedClient) throws MartusSignatureException, IOException, InterruptedException, MartusSignatureFileAlreadyExistsException
	{
		add(authorizedClient);
		UnicodeWriter writer = new UnicodeWriter(authorizeLogFile, UnicodeWriter.APPEND);
		writer.writeln(authorizedClient.toString());
		writer.close();
		MartusServerUtilities.createSignatureFileFromFileOnServer(authorizeLogFile, security);
	}
	
	public AuthorizeLogEntry getAuthorizedClientEntry(String publicCode)
	{
		for(int i = 0 ; i< authorizedClients.size(); ++i)
		{
			AuthorizeLogEntry authorizeLogEntry = ((AuthorizeLogEntry)authorizedClients.get(i));
			if(authorizeLogEntry.getPublicCode().equals(publicCode))
				return authorizeLogEntry;
		}
		return null;
	}
	
	public Vector getAuthorizedClientStrings()
	{
		Vector clientStrings = new Vector();
		for(int i = 0 ; i< authorizedClients.size(); ++i)
		{
			clientStrings.add(((AuthorizeLogEntry)authorizedClients.get(i)).toString());
		}
		return clientStrings;
	}
	
	private File authorizeLogFile;
	private LoggerInterface logger;
	private Vector authorizedClients;
	private MartusCrypto security;
}
