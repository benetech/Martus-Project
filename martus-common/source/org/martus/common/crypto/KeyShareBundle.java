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

package org.martus.common.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Date;

import org.martus.common.MartusConstants;
import org.martus.common.crypto.MartusCrypto.KeyShareException;
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeStringWriter;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;



class KeyShareBundle
{
	public KeyShareBundle(String publicKeyToUse, byte[] payloadToUse)
	{
		id = MartusConstants.martusSecretShareFileID;
		timeStamp = (new Timestamp(new Date().getTime())).toString();		
		publicKey = publicKeyToUse;
		payload = StreamableBase64.encode(payloadToUse);
	}
			
	public KeyShareBundle(String bundleString) throws IOException, KeyShareException
	{
		InputStream in = new StringInputStreamWithSeek(bundleString);
		try
		{
			UnicodeReader reader = new UnicodeReader(in);
			try
			{
				id = reader.readLine();
				if(!id.equals(MartusConstants.martusSecretShareFileID))
					throw new KeyShareException();
		
				timeStamp = reader.readLine();
				publicKey = reader.readLine();
				sharePiece = reader.readLine();
				payload = reader.readLine();
			}
			finally
			{
				reader.close();
			}
		}
		finally
		{
			in.close();
		}
	}
	
	public String createBundleString(String sharePieceToWrite) throws IOException
	{
		sharePiece = sharePieceToWrite;
		
		UnicodeStringWriter writer = UnicodeStringWriter.create();
		writer.writeln(id);
		writer.writeln(timeStamp);
		writer.writeln(publicKey);
		writer.writeln(sharePiece);
		writer.writeln(payload);
		writer.close();
		return writer.toString();
	}

	public String id;
	public String timeStamp;
	public String publicKey;
	public String sharePiece;
	public String payload;
}