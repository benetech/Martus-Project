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
package org.martus.amplifier.main;

import java.io.IOException;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.SecurityConstraint;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.util.B64Code;
import org.mortbay.util.StringUtil;

public class PasswordAuthenticationHandler extends AbstractHttpHandler
{
	
	public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response)
		throws HttpException, IOException
	{
		String credentials=request.getField(HttpFields.__Authorization);
		if (credentials==null)
		{
			returnUnauthorized(request, response);
			return;
		}

		credentials=credentials.substring(credentials.indexOf(' ')+1);
		credentials=B64Code.decode(credentials,StringUtil.__ISO_8859_1);
		int i=credentials.indexOf(':');
		String user=credentials.substring(0,i);
		String password=credentials.substring(i+1);

		if (!isAuthorized(user, password))
		{
			returnUnauthorized(request, response);
			return;
		}
		request.setAuthType(SecurityConstraint.__BASIC_AUTH);
		request.setAuthUser(user);
	}

	private boolean isAuthorized(String user, String password)
	{
		String authorizedUser = MartusAmplifier.getStaticWebAuthorizedUser();
		String authorizedPassword = MartusAmplifier.getStaticWebAuthorizedPassword();
		return !(user == null || password == null || !user.equals(authorizedUser)|| !password.equals(authorizedPassword));
	}

	private void returnUnauthorized(HttpRequest request, HttpResponse response) throws IOException
	{
		response.setField(HttpFields.__WwwAuthenticate,"basic realm="+REALM);
		response.sendError(HttpResponse.__401_Unauthorized);
		response.commit();
		request.setHandled(true);
		return;
	}

	final static String REALM = "MartusAmp";
}
