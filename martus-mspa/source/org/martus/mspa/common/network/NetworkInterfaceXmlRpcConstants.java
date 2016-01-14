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


package org.martus.mspa.common.network;


public interface NetworkInterfaceXmlRpcConstants
{
	public static final String serverObjectName = "MSPAServer";
	
	public final static String cmdPing = "ping";
	public final static String cmdGetCompliance = "getServerCompliance";
	public final static String cmdUpdateCompliance = "updateServerCompliance";
	public static final String cmdGetAccountIds = "getAccountIds";
	public static final String cmdGetListPackets = "getListPackets";
	public static final String cmdGetContactInfo = "getContactInfo";
	public static final String cmdGetAccountManageInfo = "getAccountManageInfo";
	
	public static final String cmdGetAllMagicWords = "getAllMagicWords";
	public static final String cmdGetActiveMagicWords = "getActiveMagicWords";
	public static final String cmdGetInActiveMagicWords = "getInactiveMagicWords";
	public static final String cmdUpdateMagicWords = "updateMagicWords";	
	
	public static final String cmdGetNumOfHiddenBulletins = "getNumOfHiddenBulletins";
	public static final String cmdGetListOfHiddenBulletinIds = "getListOfHiddenBulletinIds";
	public static final String cmdHideBulletins = "hideBulletins";	
	public static final String cmdGetListOfBulletinIds = "getListOfBulletinIds";
	public static final String cmdUnhideBulletins = "unhideBulletins";
		
	public static final String cmdUpdateAccountManageInfo = "updateAccountManageInfo";		
	public static final String cmdGetListOfAvailableServers = "getListOfAvailableServers";		
	public static final String cmdGetListOfAssignedServers = "getListOfAssignedServers";
	public static final String cmdAddAvailableServer = "addAvailableServer";
	public static final String cmdUpdateAssignedServers = "updateAssignedServers";
	
	public static final String cmdSendCommandToServer = "sendCommandToServer";
	
	public static final String cmdGetMartusServerArguments = "getMartusServerArguments";
	public static final String cmdUpdateMartusServerArguments = "updateMartusServerArguments";
		
}
