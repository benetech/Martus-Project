/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2014, Beneficent
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

package org.martus.server.forclients;

import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.crypto.MartusCrypto;

public interface ServerForClientsInterface extends LoggerInterface 
{
	public String getPublicCode(String clientId);
	public void clientConnectionStart(String callerAccountId);
	public void clientConnectionExit();
	public MartusCrypto getSecurity();
	public String ping();
	public String deleteDraftBulletins(String myAccountId, Vector originalRequest, String signature);
	public Vector getBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId, int chunkOffset, int maxChunkSize);
	public Vector getNews(String myAccountId, String versionLabel, String versionBuildDate);
	public Vector getPacket(String myAccountId, String authorAccountId, String bulletinLocalId, String packetLocalId);
	public Vector getServerCompliance();
	public Vector listMySealedBulletinIds(String authorAccountId, Vector retrieveTags);
	public String putBulletinChunk(String myAccountId, String authorAccountId, String bulletinLocalId, int totalSize, int chunkOffset, int chunkSize, String data);
	public String putContactInfo(String myAccountId, Vector parameters);
	public String requestUploadRights(String authorAccountId, String tryMagicWord);
	public Vector listFieldOfficeAccounts(String hqAccountId);
	public Vector listFieldOfficeDraftBulletinIds(String myAccountId, String authorAccountId, Vector retrieveTags);
	public Vector listFieldOfficeSealedBulletinIds(String myAccountId, String authorAccountId, Vector retrieveTags);
	public Vector listMyDraftBulletinIds(String authorAccountId, Vector retrieveTags);
	public boolean shouldSimulateBadConnection();
	public Vector getPartialUploadStatus(String authorAccountId, String bulletinLocalId, Vector extraParameters);
	public Vector getMartusAccountAccessToken(String myAccountId);
	public Vector getMartusAccountIdFromAccessToken(String myAccountId, MartusAccountAccessToken tokenToUse);
	public Vector putFormTemplate(String myAccountId, Vector formTemplateData);
	public Vector getListOfFormTemplates(String myAccountId, String accountIdToUse);
	public Vector getFormTemplate(String myAccountId, String accountIdToUse, String formTitle);
}
