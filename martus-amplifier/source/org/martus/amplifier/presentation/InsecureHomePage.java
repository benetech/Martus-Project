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

package org.martus.amplifier.presentation;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.martus.util.UnicodeReader;


public class InsecureHomePage extends HttpServlet
{

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		Writer responseWriter = response.getWriter();
		loadDefaultPage(responseWriter, request.getServerName());				
		response.flushBuffer();
	}

	void loadDefaultPage(Writer writer, String serverName)
	{
		InputStream indexHtmlStream = InsecureHomePage.class.getResourceAsStream("index.html");		
		try
		{
			UnicodeReader reader = new UnicodeReader(indexHtmlStream);
			String entirePage = reader.readAll();
			entirePage = entirePage.replaceAll(NAME_TO_REPLACE, serverName);												
			writer.write(entirePage);
			reader.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	private static final String NAME_TO_REPLACE = "SERVERNAME";	

}
