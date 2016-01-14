/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.common;
public class ContactKey extends ExternalPublicKey
{
	public ContactKey(ContactKey keyToUse)
	{
		this(keyToUse.getPublicKey(), keyToUse.getLabel());
		this.sendToByDefault = keyToUse.getSendToByDefault();
		this.verificationStatus = keyToUse.getVerificationStatus();
	}
	
	public ContactKey(String publicKey)
	{
		this(publicKey, "");
	}

	public ContactKey(String publicKey, String label)
	{
		super(publicKey, label);
		verificationStatus = NOT_VERIFIED_UNKNOWN;
	}
	
	public boolean getCanSendTo()
	{
		return true;
	}

	public boolean getCanReceiveFrom()
	{
		return true;
	}

	public boolean getSendToByDefault()
	{
		return sendToByDefault;
	}

	public void setSendToByDefault(boolean sendToByDefault)
	{
		this.sendToByDefault = sendToByDefault;
	}

	public Integer getVerificationStatus()
	{
		return verificationStatus;
	}

	public void setVerificationStatus(Integer verificationStatus)
	{
		this.verificationStatus = verificationStatus;
	}
	
	public static boolean isVerified(Integer status)
	{
		if(status.equals(VERIFIED_ACCOUNT_OWNER) || status.equals(VERIFIED_ENTERED_20_DIGITS) ||
			status.equals(VERIFIED_VISUALLY))
				return true;
		return false;
	}

	private boolean sendToByDefault;
	private Integer	verificationStatus;

    public static final Integer NOT_VERIFIED = 0;
    public static final Integer VERIFIED_VISUALLY = 1;
    public static final Integer VERIFIED_ENTERED_20_DIGITS = 2;
    public static final Integer VERIFIED_ACCOUNT_OWNER = 3;
    public static final Integer NOT_VERIFIED_UNKNOWN = 4;
}

