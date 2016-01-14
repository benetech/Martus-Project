/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
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

import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.UnicodeStringReader;

public class DeleteRequestRecord
{
	
	public DeleteRequestRecord(String accountIdToUse, Vector originalRequestToUse, String signatureToUse)
	{
		timeStamp = MartusServerUtilities.createTimeStamp();
		accountId = accountIdToUse;
		originalClientRequest = originalRequestToUse;
		signature = signatureToUse;
	}
	
	public DeleteRequestRecord(ReadableDatabase db, UniversalId uid, MartusCrypto security) throws IOException, CryptoException
	{
		String retrievedRecordString = db.readRecord(getDelKey(uid), security);
		UnicodeStringReader reader = new UnicodeStringReader(retrievedRecordString);
		try
		{
			reader.readLine(); //header
			timeStamp = reader.readLine();
			accountId = reader.readLine();
			int originalClientRequestSize = new Integer(reader.readLine()).intValue();
			originalClientRequest = new Vector();
			for(int i = 0; i < originalClientRequestSize; ++i)
			{
				originalClientRequest.add(reader.readLine());
			}
			signature = reader.readLine();
		}
		finally
		{
			reader.close();
		}
	}
	
	public String getDelData() 
	{
		StringBuffer contents = new StringBuffer();
		contents.append(DRAFT_DELETE_REQUEST_IDENTIFIER);
		contents.append(newline);
		contents.append(timeStamp);
		contents.append(newline);
		contents.append(accountId);
		contents.append(newline);
		int count = originalClientRequest.size();
		contents.append(count);
		contents.append(newline);
		for(int i = 0; i < count; ++i)
		{
			contents.append(originalClientRequest.get(i));
			contents.append(newline);
		}
		contents.append(signature);
		contents.append(newline);
		
	return contents.toString();
	}
	
	public boolean doesSignatureMatch(MartusCrypto verifier)
	{
		return verifier.verifySignatureOfVectorOfStrings(originalClientRequest, accountId, signature);
	}
	
	public void writeToDatabase(Database db, UniversalId uid)
		throws IOException, Database.RecordHiddenException
	{
		db.writeRecord(getDelKey(uid), getDelData());
	}
	
	public static DatabaseKey getDelKey(UniversalId uid)
	{
		UniversalId burUid = UniversalId.createFromAccountAndLocalId(uid.getAccountId(), FileDatabase.DEL_PREFIX + uid.getLocalId());
		return DatabaseKey.createMutableKey(burUid);
	}
	
	public boolean isBefore(long timeToCompare)
	{
		try
		{
			long mTimeOfDelRequest = getmTime();
			return (timeToCompare > mTimeOfDelRequest);
		}
		catch(ParseException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public long getmTime() throws ParseException
	{
		return MartusServerUtilities.getDateFromFormattedTimeStamp(timeStamp).getTime();
	}

	private final static String DRAFT_DELETE_REQUEST_IDENTIFIER = "Martus Draft Delete Request 1.0";
	private final static String newline = "\n";

	private String accountId;
	private Vector originalClientRequest;
	private String signature;
	public String timeStamp;
}
