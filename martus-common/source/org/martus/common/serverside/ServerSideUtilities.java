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
package org.martus.common.serverside;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;

import org.martus.util.UnicodeReader;


public class ServerSideUtilities
{
	public static void writeSyncFile(File syncFile, String whoCallThisMethod) throws Exception
	{
		FileOutputStream out = new FileOutputStream(syncFile);
		out.write(0);
		out.close();
	}


	public static char[] getPassphraseFromConsole(File triggerDirectory, String whoCallThisMethod) throws Exception
	{
		System.out.print("Enter passphrase: ");
		System.out.flush();

		try
		{
			File waitingFile = new File(triggerDirectory, "waiting");
			waitingFile.delete();
			ServerSideUtilities.writeSyncFile(waitingFile, whoCallThisMethod);

			String passphrase = null;
			BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));
			try
			{
				//TODO security issue here password is a string
				passphrase = reader.readLine();
				return passphrase.toCharArray();
			}
			finally
			{
				reader.close();
			}
		} 
		catch (Exception e)
		{
			System.out.println();
			System.out.flush();
			throw(e);
		}
	}


	public static final int EXIT_CRYPTO_INITIALIZATION = 1;
	public static final int EXIT_KEYPAIR_FILE_MISSING = 2;
	public static final int EXIT_UNEXPECTED_EXCEPTION = 3;
	public static final int EXIT_UNEXPECTED_FILE_STARTUP = 4;
	public static final int EXIT_STARTUP_DIRECTORY_NOT_EMPTY = 5;
	public static final int EXIT_MISSING_DATA_DIRECTORY = 6;
	public static final int ALREADY_IN_USE = 7;
	public static final int EXIT_INVALID_COMMAND_LINE = 8;
	public static final int EXIT_NO_LISTENERS = 20;
	public static final int EXIT_INVALID_IPADDRESS = 23;
	public static final int EXIT_INVALID_PASSWORD = 73;	
	
}
