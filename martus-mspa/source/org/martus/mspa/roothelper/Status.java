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
package org.martus.mspa.roothelper;

import java.util.Vector;



public class Status
{
	public static Status createSuccess(String details)
	{
		return new Status(SUCCESS, details);
	}
	
	public static Status createFailure(String details)
	{
		return new Status(FAILED, details); 
	}
	
	private Status(String msgStatus, String details) 
	{		
		status = msgStatus;
		detailText = details;
	}
	
	public Status(Vector statusAsVector)
	{
		status = (String)statusAsVector.get(0);
		detailText = (String)statusAsVector.get(1);
	}
	
	public boolean isSuccess()
	{
		return (status.equals(SUCCESS));
	}
		
	public String getDetailText()
	{
		return detailText;
	}
	
	public String getStatus() 
	{
		return status;
	}
	
	public Vector toVector()
	{
		Vector vector = new Vector();
		vector.add(status);
		vector.add(detailText);
		return vector;
	}

	public static final String SUCCESS = "success";
	public static final String FAILED = "failed";

	private String status;
	private String detailText;
	
}
