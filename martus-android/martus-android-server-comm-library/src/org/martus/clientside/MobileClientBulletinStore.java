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

package org.martus.clientside;

import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.ClientFileDatabase;
import org.martus.common.database.Database;
import org.martus.common.fieldspec.StandardFieldSpecs;

import java.io.File;
import java.io.IOException;

/*
	This class represents a collection of bulletins
	(and also a collection of folders) stored on the
	client pc.

	It is responsible for managing the lifetimes of
	both bulletins and folders, including saving and
	loading them to/from disk.
*/
public class MobileClientBulletinStore extends BulletinStore
{
	public MobileClientBulletinStore(MartusCrypto cryptoToUse)
	{
		setSignatureGenerator(cryptoToUse);
	}

	public void doAfterSigninInitialization(File dataRootDirectory) throws Exception {
		File dbDirectory = new File(dataRootDirectory, "packets");
		Database db = new ClientFileDatabase(dbDirectory, getSignatureGenerator());
		doAfterSigninInitialization(dataRootDirectory, db);
	}

	public void doAfterSigninInitialization(File dataRootDirectory, Database db) throws Exception {
		try
		{
			super.doAfterSigninInitialization(dataRootDirectory, db);
		} catch (FileVerificationException e)
		{
			//do nothing because mobile doesn't have an account map to verify
		}

		topSectionFieldSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		bottomSectionFieldSpecs = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();

	}

	public boolean mustEncryptPublicData()
	{
		return getDatabase().mustEncryptLocalData();
	}

	public synchronized void destroyBulletin(Bulletin b) throws IOException
	{
		removeBulletinFromStore(b);
	}

    public void saveBulletin(Bulletin bulletin) throws Exception
    {
        super.saveBulletin(bulletin, false);
    }

    public FieldSpecCollection getBottomSectionFieldSpecs()
	{
		return bottomSectionFieldSpecs;
	}

	public FieldSpecCollection getTopSectionFieldSpecs()
	{
		return topSectionFieldSpecs;
	}

	public void setTopSectionFieldSpecs(FieldSpecCollection newFieldSpecs)
	{
		topSectionFieldSpecs = newFieldSpecs;
	}
	
	public void setBottomSectionFieldSpecs(FieldSpecCollection newFieldSpecs)
	{
		bottomSectionFieldSpecs = newFieldSpecs;
	}

	public Bulletin createEmptyBulletin() throws Exception
	{
		return createEmptyBulletin(getTopSectionFieldSpecs(), getBottomSectionFieldSpecs());
	}

	public Bulletin createEmptyCustomBulletin(FieldSpecCollection topSectionSpecs) throws Exception{
		return createEmptyBulletin(topSectionSpecs, getBottomSectionFieldSpecs());
	}
	
	public Bulletin createEmptyBulletin(FieldSpecCollection topSectionSpecs, FieldSpecCollection bottomSectionSpecs) throws Exception
	{
		Bulletin b = new Bulletin(getSignatureGenerator(), topSectionSpecs, bottomSectionSpecs);
		return b;
	}

	private FieldSpecCollection topSectionFieldSpecs;
	private FieldSpecCollection bottomSectionFieldSpecs;
}
