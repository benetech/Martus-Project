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
package org.martus.amplifier.velocity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.queryParser.ParseException;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.servlet.VelocityServlet;
import org.martus.amplifier.presentation.AbstractSearchResultsServlet;

abstract public class AmplifierServlet extends VelocityServlet
{
	/**
	 *   Called by the VelocityServlet
	 *   init().  We want to set a set of properties
	 *   so that templates will be found in the webapp
	 *   root.  This makes this easier to work with as 
	 *   an example, so a new user doesn't have to worry
	 *   about config issues when first figuring things
	 *   out
	 */
	protected Properties loadConfiguration(ServletConfig config )
		throws IOException, FileNotFoundException
	{
		Properties p = new Properties();
		/*
		 *  first, we set the template path for the
		 *  FileResourceLoader to the root of the 
		 *  webapp.  This probably won't work under
		 *  in a WAR under WebLogic, but should 
		 *  under tomcat :)
		 */

		String path = config.getServletContext().getRealPath("/");

		if (path == null)
		{
			System.out.println(" SampleServlet.loadConfiguration() : unable to " 
							   + "get the current webapp root.  Using '/'. Please fix.");

			path = "/";
		}

		p.setProperty( Velocity.FILE_RESOURCE_LOADER_PATH,  path );
		/**
		 *  and the same for the log file
		 */
		
		p.setProperty( "runtime.log", path + "velocity.log" );

		return p;
	}

	public String selectTemplate(AmplifierServletRequest request,
			AmplifierServletResponse response, Context context) throws Exception
	{			
		setSimpleQueryFromSession(request, context);
		return null;
	}		
			
	public Template handleRequest(HttpServletRequest request,
								HttpServletResponse response, 
								Context context)
	{
		errorMsg="";
		errorTitle="";
		try
		{			
			request.setCharacterEncoding("UTF-8");
			AmplifierServletRequest ampRequest = new WrappedServletRequest(request);
			AmplifierServletResponse ampResponse = new WrappedServletResponse(response);
			String templateName = selectTemplate(ampRequest, ampResponse, context);			
			return getTemplate(templateName, "UTF-8");
		}
		catch( ParseException e )
		{
			errorTitle = "Search Query Syntax Error:";				
			errorMsg = "Certain keywords and special characters (such as '+','-','OR','AND') are not supported.";		
			displayError("QueryParse Error", e);
		}
		catch( ResourceNotFoundException e )
		{
			displayError("template not found", e);
		}
		catch( Exception e )
		{
			displayError("Unknown error", e);					
			e.printStackTrace();
		}
		
		try
		{	
			AmplifierServletRequest ampRequest = new WrappedServletRequest(request);
			AbstractSearchResultsServlet.setInternalErrorContext(errorTitle, errorMsg, ampRequest.getSession(), context);							
			return getTemplate("InternalError.vm");
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		return null;
	}
    
	private void setSimpleQueryFromSession(AmplifierServletRequest request, Context context) 
	{
		String data = (String) request.getSession().getAttribute("simpleQuery");
		if (data == null)
			data = "";

		context.put("defaultSimpleSearch", data);
	}

	protected void displayError(String message, Exception e)
	{				
		System.out.println(getClass().getName() + ": " + message+ " "+e);
	}
	

	static public void formatDataForHtmlDisplay(Vector dataToFormat)
	{
		if(dataToFormat == null)
			return;
		for(int i = 0 ; i < dataToFormat.size(); ++i)
		{
			dataToFormat.set(i, formatDataForHtmlDisplay((String)dataToFormat.get(i)));
		}
	}

	static public void formatDataForHtmlDisplay(Map dataToFormat)
	{
		if(dataToFormat == null)
			return;
		Set tags = dataToFormat.keySet();
		for (Iterator iter = tags.iterator(); iter.hasNext();)
		{
			String fieldTag = (String) iter.next();
			String fieldData = (String)dataToFormat.get(fieldTag);
			fieldData = formatDataForHtmlDisplay(fieldData);
			dataToFormat.put(fieldTag, fieldData);
		}
	}

	private static String formatDataForHtmlDisplay(String dataToFormat)
	{
		dataToFormat = dataToFormat.replaceAll("&", "&amp;");
		dataToFormat = dataToFormat.replaceAll("<", "&lt;");
		dataToFormat = dataToFormat.replaceAll("\n", "<BR/>");
		dataToFormat = dataToFormat.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		dataToFormat = dataToFormat.replaceAll("  ", "&nbsp;&nbsp;");
		return dataToFormat;
	}
	
	String errorMsg="";
	String errorTitle="";
}
