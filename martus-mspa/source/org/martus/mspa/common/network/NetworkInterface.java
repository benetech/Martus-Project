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

import java.io.IOException;
import java.util.Vector;


public interface NetworkInterface 
{
	public Vector getAccountIds(String myAccountId, Vector parameters, String signature) throws IOException;
	public Vector getContactInfo(String myAccountId, Vector parameters, String signature, String accountId) throws IOException;	
	public Vector getAccountManageInfo(String myAccountId, String manageAccountId) throws IOException;
	public Vector updateAccountManageInfo(String myAccount,String manageAccountId, Vector accountInfo) throws IOException;	
	
	public Vector getListOfBulletinIds(String myAccountId) throws IOException;
	public Vector getListOfHiddenBulletinIds(String myAccountId, String manageAccountId) throws IOException;
	public Vector hideBulletins(String myAccountId, String manageAccountId, Vector localIds) throws IOException;
	public Vector unhideBulletins(String myAccountId, String manageAccountId, Vector localIds) throws IOException;

	
	public Vector getInactiveMagicWords(String myAccountId) throws IOException;
	public Vector getActiveMagicWords(String myAccountId) throws IOException;
	public Vector getAllMagicWords(String myAccountId) throws IOException;
	public Vector updateMagicWords(String myAccountId, Vector magicWords) throws IOException;
	
	public Vector addAvailableServer(String myAccountId, Vector mirrorInfo) throws IOException;
	public Vector getListOfAvailableServers(String myAccountId) throws IOException;
	public Vector getListOfAssignedServers(String myAccountId,int mirrorType) throws IOException;
	public Vector updateAssignedServers(String myAccountId, Vector mirrorInfo, int mirrorType) throws IOException;
	
	public Vector getServerCompliance(String myAccountId) throws IOException;
	public Vector updateServerCompliance(String myAccountId, String compliantsMsg) throws IOException;
		
	public Vector sendCommandToServer(String myAccountId, String type, String cmd) throws IOException;
	
	public Vector getMartusServerArguments(String myAccountId) throws IOException;
	public Vector updateMartusServerArguments(String myAccountId, Vector args) throws IOException;
	
}
