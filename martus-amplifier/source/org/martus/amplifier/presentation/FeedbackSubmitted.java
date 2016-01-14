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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import org.apache.velocity.context.Context;
import org.martus.amplifier.common.AdvancedSearchInfo;
import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.main.MartusAmplifier;
import org.martus.amplifier.velocity.AmplifierServlet;
import org.martus.amplifier.velocity.AmplifierServletRequest;
import org.martus.amplifier.velocity.AmplifierServletResponse;
import org.martus.amplifier.velocity.AmplifierServletSession;
import org.martus.util.UnicodeWriter;


public class FeedbackSubmitted extends AmplifierServlet
{

	public FeedbackSubmitted()
	{
		this(new File(MartusAmplifier.getStaticAmplifierDataPath(), "ampFeedback").getPath());
	}

	public FeedbackSubmitted(String basePathToUse)
	{
		super();
		feedbackDirectory = basePathToUse;
	}

	public String selectTemplate(AmplifierServletRequest request, AmplifierServletResponse response, Context context)
			throws Exception
	{
		super.selectTemplate(request, response, context);
		String feedbackDissatisfied = request.getParameter("userFeedbackDissatisfied");
		String feedbackProblem = request.getParameter("userFeedbackProblem");
		Vector searchedFor = new Vector(); 
		AmplifierServletSession session = request.getSession();
		String simpleQuery = null;

		String type = (String)session.getAttribute("typeOfSearch");
		if(type == null || type.equals("quick"))
		{
			String simple = (String)session.getAttribute("searchedFor");
			if(simple!=null)
			{
				simpleQuery = (String)session.getAttribute("simpleQuery");
				searchedFor.add(simple);
			}
		}
		else
		{
			AdvancedSearchInfo advancedSearchedFor = (AdvancedSearchInfo) session.getAttribute("defaultAdvancedSearch");
			searchedFor = getVectorOfAdvancedSearch(advancedSearchedFor);
		}
		
		if(feedbackDissatisfied != null)
			writeFeedback(FEEDBACK_DISSATISFIED_PREFIX, simpleQuery, searchedFor, feedbackDissatisfied);
		else if(feedbackProblem != null)
			writeFeedback(FEEDBACK_TECH_PROBLEM_PREFIX, simpleQuery, searchedFor, feedbackProblem);
		else
			return "InternalError.vm";

		return "FeedbackSubmitted.vm";
	}

	public Vector getVectorOfAdvancedSearch(AdvancedSearchInfo advancedSearchedFor)
	{
		Vector advancedSearch = new Vector();
		Map fields = advancedSearchedFor.getFields();
 		for(int i=0; i< SearchResultConstants.ADVANCED_KEYS.length; i++)
		{
			String value = (String)fields.get(SearchResultConstants.ADVANCED_KEYS[i]);
			if (value != null)
				advancedSearch.add(SearchResultConstants.ADVANCED_KEYS[i] + "=" + value);				
		}
		return advancedSearch;
	}
	

	private void writeFeedback(String fileName, String simpleQuery, Vector searchedFor, String message) throws IOException
	{
		File feedbackDirectoryFile = new File(feedbackDirectory);
		feedbackDirectoryFile.mkdirs();
		File feedback = File.createTempFile(fileName, FEEDBACK_SUFFIX, feedbackDirectoryFile);
		UnicodeWriter writer = new UnicodeWriter(feedback);
		if(searchedFor.isEmpty())
			writer.writeln("No Previous search");
		else
		{
			if(simpleQuery != null)
			{
				writer.writeln("Simple Query:");
				writer.writeln(simpleQuery);
				writer.writeln("");
			}

			writer.writeln("Searched for:");
			for(int i = 0; i < searchedFor.size(); ++i)
			{
				writer.writeln((String)searchedFor.get(i));
			}
		}
		writer.writeln("");
		writer.writeln("Message:");
		writer.writeln(message);
		writer.close();
	}

	
	static public String FEEDBACK_DISSATISFIED_PREFIX = "dissatisfied";	
	static public String FEEDBACK_TECH_PROBLEM_PREFIX = "technical";	
	final String FEEDBACK_SUFFIX = ".txt";
	private String feedbackDirectory;	
}
