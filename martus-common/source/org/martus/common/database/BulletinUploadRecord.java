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

package org.martus.common.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeStringReader;


public class BulletinUploadRecord
{
	public static String createBulletinUploadRecord(String bulletinLocalId, MartusCrypto security) throws MartusCrypto.CreateDigestException
	{
		String timeStamp = MartusServerUtilities.createTimeStamp();
		return createBulletinUploadRecordWithSpecificTimeStamp(
			bulletinLocalId, timeStamp, security);
	}

	public static String createBulletinUploadRecordWithSpecificTimeStamp(
		String bulletinLocalId,
		String timeStamp,
		MartusCrypto security)
		throws CreateDigestException
	{
		byte[] partOfPrivateKey = security.getDigestOfPartOfPrivateKey();
		String stringToDigest = 
				BULLETIN_UPLOAD_RECORD_IDENTIFIER + newline +
				bulletinLocalId + newline +
				timeStamp + newline +
				StreamableBase64.encode(partOfPrivateKey) + newline;
		String digest = MartusCrypto.createDigestString(stringToDigest);
		return 
			BULLETIN_UPLOAD_RECORD_IDENTIFIER + newline + 
			bulletinLocalId + newline +
			timeStamp + newline +
			digest + newline;
	}
	
	public static long getTimeStamp(ReadableDatabase db, DatabaseKey key, MartusCrypto security) throws IOException, CryptoException, ParseException
	{
		String retrievedRecordString = db.readRecord(getBurKey(key), security);
		return getTimeStamp(retrievedRecordString);
	}

	public static long getTimeStamp(String retrievedRecordString) throws IOException, ParseException
	{
		UnicodeStringReader reader = new UnicodeStringReader(retrievedRecordString);
		try
		{
			reader.readLine(); //header
			reader.readLine(); //localId
			String timeStamp = reader.readLine();
			return MartusServerUtilities.getDateFromFormattedTimeStamp(timeStamp).getTime();
		}
		finally
		{
			reader.close();
		}
	}
	
	public static boolean wasBurCreatedByThisCrypto(String burToTest, MartusCrypto security)
	{
		if(burToTest == null)
			return false;
		BufferedReader reader = new BufferedReader(new StringReader(burToTest));
		String digestFromTestBur;
		String digestCreatedFromThisCrypto;
		try
		{
			String fileTypeIdentifier = reader.readLine();
			String localId = reader.readLine();
			String timeStamp = reader.readLine(); 
			digestFromTestBur = reader.readLine();

			String stringToDigest = 
					fileTypeIdentifier + newline +
					localId  + newline +
					timeStamp + newline +
					StreamableBase64.encode(security.getDigestOfPartOfPrivateKey()) + newline;

			digestCreatedFromThisCrypto = MartusCrypto.createDigestString(stringToDigest);

		}
		catch (Exception e)
		{
			return false;
		}

		return (digestCreatedFromThisCrypto.equals(digestFromTestBur));		
	}


	public static DatabaseKey getBurKey(DatabaseKey key)
	{
		UniversalId burUid = UniversalId.createFromAccountAndLocalId(key.getAccountId(), FileDatabase.BUR_PREFIX + key.getLocalId());
		
		if(key.isMutable())
			return DatabaseKey.createMutableKey(burUid);
		return DatabaseKey.createImmutableKey(burUid);
	}

	public static void writeSpecificBurToDatabase(Database db, BulletinHeaderPacket bhp, String bur)
		throws IOException, Database.RecordHiddenException
	{
		DatabaseKey headerKey = bhp.createKeyWithHeaderStatus(bhp.getUniversalId());
		DatabaseKey burKey = getBurKey(headerKey);
		db.writeRecord(burKey, bur);
	}
	
	private static final String BULLETIN_UPLOAD_RECORD_IDENTIFIER = "Martus Bulletin Upload Record 1.0";
	private final static String newline = "\n";

}
