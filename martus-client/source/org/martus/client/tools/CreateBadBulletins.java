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

package org.martus.client.tools;

import java.io.BufferedReader;
import java.io.File;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.test.BulletinForTesting;
import org.martus.util.UnicodeReader;

public class CreateBadBulletins
{
	public static void main(String[] args) throws Exception
	{
		new CreateBadBulletins(args);
	}
	
	public CreateBadBulletins(String[] args) throws Exception
	{
		processArgs(args);
		
		System.out.println("WARNING: This program is for TEST purposes only!");
		System.out.println(" If you have any real information on this computer,");
		System.out.println(" press Ctrl-C now to cancel! Do not enter your ");
		System.out.println(" passphrase if you have real data!");
		System.out.println("This program will create and save several bulletins");
		System.out.println(" that will cause warning messages when viewed.");
		System.out.println();
		
		File codeDirectory = MartusApp.getTranslationsDirectory();
		MartusApp app = new MartusApp(new MartusLocalization(codeDirectory, new String[] {}));
	
		String userName = getUserInput("username:");
		String userPassPhrase = getUserInput("passphrase:");
		app.attemptSignIn(userName, userPassPhrase.toCharArray());
		app.doAfterSigninInitalization();
		app.loadFolders();

		BulletinForTesting.clearShoulds();
		BulletinForTesting.shouldCreateUnknownTagInHeader = true;
		createAndSaveBulletin(app, "Unknown tag in header");

		BulletinForTesting.clearShoulds();
		BulletinForTesting.shouldCreateUnknownTagInPublicSection = true;
		createAndSaveBulletin(app, "Unknown tag in public");

		BulletinForTesting.clearShoulds();
		BulletinForTesting.shouldCreateUnknownTagInPrivateSection = true;
		createAndSaveBulletin(app, "Unknown tag in private");

		BulletinForTesting.clearShoulds();
		BulletinForTesting.shouldCreateUnknownStuffInCustomField = true;
		createAndSaveBulletin(app, "Unknown custom field stuff");

		app.getStore().saveFolders();
		System.out.println("Completed successfully");
	}

	private ClientBulletinStore createAndSaveBulletin(MartusApp app, String title)
		throws Exception
	{
		MartusCrypto security = app.getSecurity();
		ClientBulletinStore store = app.getStore();
		Bulletin b = new BulletinForTesting(security);
		b.setAllPrivate(false);
		b.set(BulletinConstants.TAGTITLE, title);
		b.set("extra", "Data in custom field with unknown stuff");
		store.saveBulletin(b);
		store.createOrFindFolder("Bad Bulletins").add(b);
		return store;
	}
	
	String getUserInput(String prompt) throws Exception
	{
		System.out.print(prompt);
		System.out.flush();
		UnicodeReader rawReader = new UnicodeReader(System.in);	
		BufferedReader reader = new BufferedReader(rawReader);
		String result = reader.readLine();
		reader.close();
		return result;
	}

	void processArgs(String[] args)
	{
		// nothing to do yet
	}
	
}
