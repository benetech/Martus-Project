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
package org.martus.mspa.server;

import java.io.File;

import org.martus.common.MartusLogger;
import org.martus.mspa.server.mail.RecipientWithSmtpHosts;
import org.martus.util.UnicodeReader;

public class EmailNotifications
{

	public RecipientWithSmtpHosts getRecipientWithHosts()
	{
		return recipientWithHosts;
	}
	
	public void loadFrom(File emailNotificationsFile) throws Exception
	{
		if(!emailNotificationsFile.exists())
		{
			MartusLogger.logWarning("Missing: " + emailNotificationsFile);
			return;
		}
		
		UnicodeReader reader = new UnicodeReader(emailNotificationsFile);
		try
		{
			String line = reader.readLine();
			while(line != null)
			{
				processLine(line);
				line = reader.readLine();
			}
		}
		finally
		{
			reader.close();
		}
		
		MartusLogger.log("Will send email notifications to " + recipientWithHosts.toString());
	}

	private void processLine(String line) throws Exception
	{
		int commentAt = line.indexOf('#');
		if(commentAt >= 0)
			line = line.substring(0, commentAt);
		
		if(line.trim().length() == 0)
			return;
		
		String[] parts = line.split(",");
		if(parts.length < 2)
		{
			MartusLogger.logError("Ignoring illegal line in email notifications: " + line);
			return;
		}

		String recipient = parts[0].trim();
		String hosts[] = new String[parts.length - 1];
		System.arraycopy(parts, 1, hosts, 0, hosts.length);
		for(int i = 0; i < hosts.length; ++i)
			hosts[i] = hosts[i].trim();
		recipientWithHosts = new RecipientWithSmtpHosts(recipient, hosts);
	}

	private RecipientWithSmtpHosts recipientWithHosts;
}
