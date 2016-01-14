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

package org.martus.common.network;
public interface NetworkInterfaceXmlRpcConstants
{
	public static final int[] defaultSSLPorts = {987, 443};

	public static final String cmdGetServerInfo = "getServerInfo";
	public static final String cmdGetUploadRights = "getUploadRights";
	public static final String cmdGetSealedBulletinIds = "getSealedBulletinIds";
	public static final String cmdGetDraftBulletinIds = "getDraftBulletinIds";
	public static final String cmdGetFieldOfficeAccountIds = "getFieldOfficeAccountIds";
	public static final String cmdPutBulletinChunk = "putBulletinChunk";
	public static final String cmdGetBulletinChunk = "getBulletinChunk";
	public static final String cmdGetPacket = "getPacket";
	public static final String cmdDeleteDrafts = "deleteDraftBulletins";
	public static final String cmdPutContactInfo = "putContactInfo";
	public static final String cmdGetNews = "getNews";
	public static final String cmdGetServerCompliance = "getServerCompliance";
	public static final String cmdGetPartialUploadStatus = "getPartialUploadStatus";
	public static final String cmdGetMartusAccountAccessToken = "getMartusAccountAccessToken";
	public static final String cmdGetMartusAccountIdFromAccessToken = "getMartusAccountIdFromAccessToken";
	public static final String cmdGetListOfFormTemplates = "getListOfFormTemplates";
	public static final String cmdPutFormTemplate = "putFormTemplate";
	public static final String cmdGetFormTemplate = "getFormTemplate";
	public static final String cmdListAvailableRevisionsSince = "listAvailableRevisionsSince";

	public static final String CMD_PING = "ping";
	public static final String CMD_SERVER_INFO = "getServerInformation";
}
