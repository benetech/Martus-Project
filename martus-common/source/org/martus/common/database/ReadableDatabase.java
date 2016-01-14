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

import java.io.IOException;

import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.packet.UniversalId;



public abstract class ReadableDatabase implements PacketStreamOpener
{
	abstract public boolean doesRecordExist(DatabaseKey key);
	abstract public int getRecordSize(DatabaseKey key) throws IOException, RecordHiddenException;
	abstract public long getmTime(DatabaseKey key) throws IOException, RecordHiddenException;
	abstract public String readRecord(DatabaseKey key, MartusCrypto decrypter) throws IOException, MartusCrypto.CryptoException;

	abstract public void visitAllRecords(PacketVisitor visitor);
	abstract public void visitAllAccounts(AccountVisitor visitor);
	abstract public void visitAllRecordsForAccount(PacketVisitor visitor, String accountString);
	
	abstract public boolean isHidden(UniversalId uid);
	abstract public boolean isHidden(DatabaseKey key);
	abstract public String getFolderForAccount(String accountString) throws IOException;
	abstract public void verifyAccountMap() throws MartusUtilities.FileVerificationException, MissingAccountMapSignatureException;
	abstract public boolean isInQuarantine(DatabaseKey key) throws RecordHiddenException;

	public boolean mustEncryptLocalData()
	{
		return false;
	}
	
	public boolean doesAccountMapExist()
	{
		return false;
	}
	
	public boolean doesAccountMapSignatureExist()
	{
		return false;
	}
	
	

	public interface PacketVisitor
	{
		void visit(DatabaseKey key);
	}

	public interface AccountVisitor
	{
		void visit(String accountString);
	}

}
