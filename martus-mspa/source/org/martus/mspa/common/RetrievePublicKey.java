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
package org.martus.mspa.common;

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
import org.martus.util.StreamableBase64;
import org.martus.util.UnicodeWriter;

public class RetrievePublicKey
{	
//	TODO: This class is duplicated class from server/tools/RetrievePublicKey due to Scott is  
//  using this class to retrieve public key in production. 
//  The refactory is required once the MSPA is ready to use.	
	public RetrievePublicKey(String mirrorIp, String mirrorPort, String mirrorPublicCode, String outputName)																						
	{
		ip = mirrorIp;
		port = new Integer(mirrorPort).intValue();
		publicCode = MartusCrypto.removeNonDigits(mirrorPublicCode);
		outputFileName = outputName;
				
		createGateway();	
	}	
	
	public boolean isSuccess() throws MartusSignatureException
	{
		Vector publicInfo = retrievePublicInfo();
		if (publicInfo == null)
			return false;
			
		return writePublicInfo(publicInfo);	
	}

	private void createGateway()
	{
		try
		{
			gateway = RetrievePublicKey.createRealMirroringGateway(ip, port, publicCode);
		}
		catch (CallerSideMirroringGatewayForXmlRpc.SSLSocketSetupException e)
		{
			e.printStackTrace();
			System.out.println("Error setting up socket");			
		}
	}

	boolean writePublicInfo(Vector publicInfo)
	{
		String publicKey = (String)publicInfo.get(0);
		String sig = (String)publicInfo.get(1);
		if (!verifyPublicInfo(publicKey, sig))
			return false;
	
		File outputFile = new File(outputFileName);
		try
		{
			UnicodeWriter writer = new UnicodeWriter(outputFile); 
			MartusUtilities.writeServerPublicKey(writer, publicKey, sig);
			writer.close();
		}
		catch (IOException e)
		{
			System.out.println("Error writing output file");
			return false;
		}
		
		return true;
	}

	boolean verifyPublicInfo(String publicKeyString, String sig)
	{
		try
		{
			if(!publicCode.equals(MartusCrypto.computePublicCode(publicKeyString)))
			{
				System.out.println("Error Retrieved Public Key doesn't match public code!");
				return false;			
			}
			MartusCrypto security = new MartusSecurity();
			byte[] publicKeyBytes = StreamableBase64.decode(publicKeyString);
			ByteArrayInputStream in = new ByteArrayInputStream(publicKeyBytes);
			if(!security.isValidSignatureOfStream(publicKeyString, in, StreamableBase64.decode(sig)))
			{
				System.out.println("Error Retrieved Public Key bad signature!");
				return false;
			}
		}
		catch (Exception e)
		{			
			System.out.println("Error Retrieved Public Key invalid!");
			return false;
		}
		return true;
	}

	Vector retrievePublicInfo() throws MartusSignatureException
	{ 		
		try
		{
			NetworkResponse response = gateway.ping();
			String resultCode = response.getResultCode();
			if(resultCode.equals(NetworkInterfaceConstants.NO_SERVER))
			{
				System.out.println("Error no response from server");			
				return null;
			}
			
			if(!NetworkInterfaceConstants.OK.equals(resultCode))
			{
				System.out.println("Error response from server: " + resultCode);				
				return null;
			}
			return response.getResultVector();
		}
		catch (MartusSignatureException e)
		{
			e.printStackTrace();
			System.out.println("Error signing request");
		}
		
		return null;
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
