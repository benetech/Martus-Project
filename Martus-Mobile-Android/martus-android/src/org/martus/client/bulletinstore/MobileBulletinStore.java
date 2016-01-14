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

package org.martus.client.bulletinstore;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.ClientFileDatabase;
import org.martus.common.database.Database;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;


/*
	This class represents a collection of bulletins
	stored on the mobile device.
*/
public class MobileBulletinStore extends BulletinStore
{
	public MobileBulletinStore(MartusCrypto cryptoToUse)
	{
		setSignatureGenerator(cryptoToUse);
	}

	public void doAfterSigninInitialization(File dataRootDirectory) throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		File dbDirectory = new File(dataRootDirectory, "packets");
		Database db = new ClientFileDatabase(dbDirectory, getSignatureGenerator());
		doAfterSigninInitialization(dataRootDirectory, db);
	}

	public void doAfterSigninInitialization(File dataRootDirectory, Database db) throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		super.doAfterSigninInitialization(dataRootDirectory, db);

		topSectionFieldSpecs = StandardFieldSpecs.getDefaultTopSetionFieldSpecs();
		bottomSectionFieldSpecs = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();

	}

    public void saveBulletin(Bulletin b) throws
        			IOException,
        			MartusCrypto.CryptoException
    {
        MartusCrypto signer = b.getSignatureGenerator();
        BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();

        FieldDataPacket publicDataPacket = b.getFieldDataPacket();
        boolean shouldEncryptPublicData = (b.isDraft() || b.isAllPrivate());
        publicDataPacket.setEncrypted(shouldEncryptPublicData);

        byte[] dataSig = createPacketSignature(publicDataPacket, signer);
        bhp.setFieldDataSignature(dataSig);

        Packet privatePacket = b.getPrivateFieldDataPacket();
        byte[] privateDataSig = createPacketSignature(privatePacket, signer);
        bhp.setPrivateFieldDataSignature(privateDataSig);

        bhp.updateLastSavedTime();
    }

    private byte[] createPacketSignature(Packet packet, MartusCrypto signer) throws IOException {
        StringWriter headerWriter = new StringWriter();
        byte[] sig = packet.writeXml(headerWriter, signer);
        return sig;
    }

	public void deleteAllData() throws Exception
	{
		super.deleteAllData();
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


	public boolean bulletinHasCurrentFieldSpecs(Bulletin b)
	{
		return (b.getTopSectionFieldSpecs().equals(getTopSectionFieldSpecs()) &&
				b.getBottomSectionFieldSpecs().equals(getBottomSectionFieldSpecs()) );
	}

	public Bulletin createEmptyBulletin() throws Exception
	{
		return createEmptyBulletin(getTopSectionFieldSpecs(), getBottomSectionFieldSpecs());
	}

	public Bulletin createEmptyBulletin(FieldSpecCollection topSectionSpecs, FieldSpecCollection bottomSectionSpecs) throws Exception
	{
		Bulletin b = new Bulletin(getSignatureGenerator(), topSectionSpecs, bottomSectionSpecs);
		return b;
	}

	public Bulletin createEmptyClone(Bulletin original) throws Exception
	{
		FieldSpecCollection topSectionSpecs = original.getTopSectionFieldSpecs();
		FieldSpecCollection bottomSectionSpecs = original.getBottomSectionFieldSpecs();
		return createEmptyCloneWithFields(original, topSectionSpecs, bottomSectionSpecs);
	}

	public Bulletin createEmptyCloneWithFields(Bulletin original, FieldSpecCollection publicSpecs, FieldSpecCollection privateSpecs) throws Exception
	{
		UniversalId headerUid = original.getUniversalId();
		UniversalId publicDataUid = original.getFieldDataPacket().getUniversalId();
		UniversalId privateDataUid = original.getPrivateFieldDataPacket().getUniversalId();
		return new Bulletin(getSignatureGenerator(), headerUid, publicDataUid, privateDataUid, publicSpecs, privateSpecs);
	}

	public Bulletin createNewDraft(Bulletin original, FieldSpecCollection topSectionFieldSpecsToUse, FieldSpecCollection bottomSectionFieldSpecsToUse) throws Exception
	{
		Bulletin newDraftBulletin = createEmptyBulletin(topSectionFieldSpecsToUse, bottomSectionFieldSpecsToUse);
		newDraftBulletin.createDraftCopyOf(original, getDatabase());
		return newDraftBulletin;
	}

	public Bulletin createDraftClone(Bulletin original, FieldSpecCollection topSectionFieldSpecsToUse, FieldSpecCollection bottomSectionFieldSpecsToUse) throws Exception
	{
		Bulletin clone = createEmptyCloneWithFields(original, topSectionFieldSpecsToUse, bottomSectionFieldSpecsToUse);
		clone.createDraftCopyOf(original, getDatabase());
		return clone;
	}

	private FieldSpecCollection topSectionFieldSpecs;
	private FieldSpecCollection bottomSectionFieldSpecs;
}
