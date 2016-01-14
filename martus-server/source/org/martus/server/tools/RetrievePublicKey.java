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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.mirroring.CallerSideMirroringGateway;
import org.martus.common.network.mirroring.CallerSideMirroringGatewayForXmlRpc;
import org.martus.common.network.mirroring.MirroringInterface;
import org.martus.common.network.mirroring.CallerSideMirroringGatewayForXmlRpc.SSLSocketSetupException;
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeWriter;

public class RetrievePublicKey
{
	public static void main(String[] args)
	{
		new RetrievePublicKey(args);
	}
	
	RetrievePublicKey(String[] args)
	{
		processArgs(args);
		createGateway();
		Vector publicInfo = retrievePublicInfo();
		writePublicInfo(publicInfo);
		System.out.println("Success");
		System.exit(0);
	}

	private void createGateway()
	{
		try
		{
			gateway = RetrievePublicKey.createRealMirroringGateway(ip, port, publicCode);
		}
		catch (SSLSocketSetupException e)
		{
			e.printStackTrace();
			System.out.println("Error setting up socket");
			System.exit(3);
		}
	}

	void writePublicInfo(Vector publicInfo)
	{
		String publicKey = (String)publicInfo.get(0);
		String sig = (String)publicInfo.get(1);
		verifyPublicInfo(publicKey, sig);
		File outputFile = new File(outputFileName);
		try
		{
			UnicodeWriter writer = new UnicodeWriter(outputFile); 
			MartusUtilities.writeServerPublicKey(writer, publicKey, sig);
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Error writing output file");
			System.exit(3);
		}
		
	}
	
	void verifyPublicInfo(String publicKeyString, String sig)
	{
		try
		{
			if(!publicCode.equals(MartusCrypto.computePublicCode(publicKeyString)))
			{
				System.out.println("Error Retrieved Public Key doesn't match public code!");
				System.exit(3);
			}
			MartusCrypto security = new MartusSecurity();
			byte[] publicKeyBytes = StreamableBase64.decode(publicKeyString);
			ByteArrayInputStream in = new ByteArrayInputStream(publicKeyBytes);
			if(!security.isValidSignatureOfStream(publicKeyString, in, StreamableBase64.decode(sig)))
			{
				System.out.println("Error Retrieved Public Key bad signature!");
				System.exit(3);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Error Retrieved Public Key invalid!");
			System.exit(3);
		}
	}
	
	Vector retrievePublicInfo()
	{ 
		try
		{
			NetworkResponse response = gateway.ping();
			String resultCode = response.getResultCode();
			if(resultCode.equals(NetworkInterfaceConstants.NO_SERVER))
			{
				System.out.println("Error no response from server");
				System.exit(6);
				return null;
			}
			if(!NetworkInterfaceConstants.OK.equals(resultCode))
			{
				System.out.println("Error response from server: " + resultCode);
				System.exit(3);
				return null;
			}
			return response.getResultVector();
		}
		catch (MartusSignatureException e)
		{
			e.printStackTrace();
			System.out.println("Error signing request");
			System.exit(3);
		}
		return null;
	}
	
	void processArgs(String[] args)
	{
		port = MirroringInterface.MARTUS_PORT_FOR_MIRRORING;

		for (int i = 0; i < args.length; i++)
		{
			String value = args[i].substring(args[i].indexOf("=")+1);
			
			if(args[i].startsWith("--ip"))
				ip = value;
			
			if(args[i].startsWith("--port") && value != null)
				port = new Integer(value).intValue();
			
			if(args[i].startsWith("--public-code"))
				publicCode = MartusCrypto.removeNonDigits(value);
			
			if(args[i].startsWith("--output-file"))
				outputFileName = value;

		}

		if(ip == null || publicCode == null || outputFileName == null)
		{
			System.err.println("Incorrect arguments: RetrievePublicKey --ip=1.2.3.4 [--port=5] --public-code=6.7.8.1.2 --output-file=pubkey.txt\n");
			System.exit(2);
		}
		
	}

	public static CallerSideMirroringGateway createRealMirroringGateway(String ip, int port, String publicCode) throws CallerSideMirroringGatewayForXmlRpc.SSLSocketSetupException
	{
		CallerSideMirroringGatewayForXmlRpc xmlRpcGateway = new CallerSideMirroringGatewayForXmlRpc(ip, port); 
		xmlRpcGateway.setExpectedPublicCode(publicCode);
		return new CallerSideMirroringGateway(xmlRpcGateway);
	}

	String ip;
	int port;
	String publicCode;
	String outputFileName;

	CallerSideMirroringGateway gateway; 
}
