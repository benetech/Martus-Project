/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2004-2007, Beneficent
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

import java.util.Vector;


public class AccountAdminOptions
{
	public boolean canUploadSelected() {return uploadOption;}
	public boolean isBannedSelected(){return bannedOption;}
	public boolean canSendToAmplifySelected() {return canSendOption;}
//	public boolean isAmplifierSelected() {return amplifierOption;}


	public void setCanUploadOption(boolean option) 
	{
		uploadOption=option;
	}

	public void setBannedOption(boolean option) 
	{
		bannedOption=option;
	}

	public void setCanSendOption(boolean option)
	{
		canSendOption = option;
	}	
	
	public Vector getOptions()
	{
		Vector options = new Vector();
		options.add(CAN_UPLOAD, new Boolean(uploadOption));
		options.add(BANNED, new Boolean(bannedOption));
		options.add(CAN_SEND, new Boolean(canSendOption));
		
		return options;
	}	
	
	public void setOptions(Vector options)
	{
		Boolean canUpload = (Boolean) options.get(CAN_UPLOAD);
		Boolean banned = (Boolean) options.get(BANNED);
		Boolean canSendToAmp = (Boolean) options.get(CAN_SEND);
							
		setBannedOption(banned.booleanValue());
		setCanSendOption(canSendToAmp.booleanValue());
		setCanUploadOption(canUpload.booleanValue());
		
	}

	public AccountAdminOptions(){}

	boolean uploadOption=false;
	boolean bannedOption=false;
	boolean canSendOption=false;
//	boolean amplifierOption=false;
	
	public static int CAN_UPLOAD =0;
	public static int BANNED	 = 1;
	public static int CAN_SEND = 2;
//	public static int AMPLIFIER = 3; 
	
}
