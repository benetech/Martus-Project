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

package org.martus.mspa.client.view;

import javax.swing.tree.DefaultMutableTreeNode;

import org.martus.common.crypto.MartusCrypto;
import org.martus.util.StreamableBase64.InvalidBase64Exception;


public class AccountNode extends DefaultMutableTreeNode implements Comparable
{
	public AccountNode(String accountString, String status)
	{		
		accountId = accountString;
		try
		{																		
			publicCode = MartusCrypto.getFormattedPublicCode(accountString);
		}
		catch (InvalidBase64Exception e)
		{						
			e.printStackTrace();					
		}		
		accountStatus = status;
	}

	public String getDisplayName() 
	{ 
		return publicCode;
	}

	public String getAccountStatus()
	{
		return accountStatus;
	}
	
	public String getAccountId()
	{
		return accountId;
	}
	
	public String toString() 
	{ 
		return publicCode;
	}

	public int compareTo(Object o)
	{
		return toString().compareTo(o.toString());
	}

	protected String publicCode;
	protected String accountStatus;
	protected String accountId;
}
