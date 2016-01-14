/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2007, Beneficent
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

package org.martus.server.tools;

import java.io.File;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.packet.Packet;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;


public class VerifyPacket
{
	public static void main(String[] args)
	{
		if(args.length != 1)
		{
			System.out.println("VerifyPacket <packet filename>");
			System.exit(1);
		}

		String localId = args[0];

		try
		{
			InputStreamWithSeek in = new FileInputStreamWithSeek(new File(localId));
			MartusCrypto security = new MartusSecurity();
			Packet.verifyPacketSignature(in, null, security);
			System.out.println("Signature OK!");
		}
		catch(Exception e)
		{
			System.out.println("Exception: " + e);
			System.out.println("           " + e.getMessage());
		}
	}
}
