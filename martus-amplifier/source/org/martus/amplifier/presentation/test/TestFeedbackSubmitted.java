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

package org.martus.amplifier.presentation.test;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import org.apache.velocity.context.Context;
import org.martus.amplifier.common.AdvancedSearchInfo;
import org.martus.amplifier.common.SearchResultConstants;
import org.martus.amplifier.presentation.DoSearch;
import org.martus.amplifier.presentation.FeedbackSubmitted;
import org.martus.amplifier.velocity.AmplifierServletSession;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;


public class TestFeedbackSubmitted extends TestCaseEnhanced
{
	public TestFeedbackSubmitted(String name)
	{
		super(name);
	}
	
	public void testFeedbackSubmittedBasics() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = new MockContext();
		File tempFeedbackDir = createTempDirectory();
		
		FeedbackSubmitted servlet = new FeedbackSubmitted(tempFeedbackDir.getAbsolutePath());
		String templateName = servlet.selectTemplate(request, response, context);
		assertEquals("no data for dissatisfied or technical problem", "InternalError.vm", templateName);

		request.putParameter("userFeedbackProblem","1");
		templateName = servlet.selectTemplate(request, response, context);
		assertEquals("no searched for, shoudl still save results", "FeedbackSubmitted.vm", templateName);

		request.putParameter("userFeedbackProblem",null);
		request.putParameter("userFeedbackDissatisfied","1");
		request.getSession().setAttribute("searchedFor", "1");
		templateName = servlet.selectTemplate(request, response, context);
		assertEquals("dissatisfied set should get back feedbacksubmitted", "FeedbackSubmitted.vm", templateName);

		request.putParameter("userFeedbackDissatisfied",null);
		request.putParameter("userFeedbackProblem","1");
		templateName = servlet.selectTemplate(request, response, context);
		DirectoryUtils.deleteEntireDirectoryTree(tempFeedbackDir);

		assertEquals("Technical problem set should get back feedbacksubmitted", "FeedbackSubmitted.vm", templateName);
	}

	public void testNothingSearchedFor() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = new MockContext();
		File tempFeedbackDir = createTempDirectory();
		
		FeedbackSubmitted servlet = new FeedbackSubmitted(tempFeedbackDir.getAbsolutePath());
		String data = "my message";
		request.putParameter("userFeedbackProblem",data);
		String templateName = servlet.selectTemplate(request, response, context);
		assertEquals("problem set should get back feedbacksubmitted", "FeedbackSubmitted.vm", templateName);

		File[] feedbackFiles = tempFeedbackDir.listFiles();
		assertEquals("Should only have 1 file", 1, feedbackFiles.length);
		File techProblem = feedbackFiles[0];
		assertTrue("Filename should contain problem", techProblem.getAbsolutePath().indexOf(FeedbackSubmitted.FEEDBACK_TECH_PROBLEM_PREFIX) > 0);

		UnicodeReader reader = new UnicodeReader(techProblem);
		String searchedForIn = reader.readLine();
		reader.readLine();//blank line
		reader.readLine();//message tag
		String dataIn = reader.readLine();
		reader.close();
		DirectoryUtils.deleteEntireDirectoryTree(tempFeedbackDir);
		
		assertEquals("Searched For dind't match?", "No Previous search", searchedForIn);
		assertEquals("data dind't match?", data, dataIn);
	}

	public void testDataSubmittedDissatisfied() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = new MockContext();
		File tempFeedbackDir = createTempDirectory();
		
		FeedbackSubmitted servlet = new FeedbackSubmitted(tempFeedbackDir.getAbsolutePath());
		String data = "my message";
		String basicQueryString = "?test";
		String searchedFor = "test";
		request.putParameter("userFeedbackDissatisfied",data);
		AmplifierServletSession session = request.getSession();
		session.setAttribute("typeOfSearch", "quick");
		session.setAttribute("searchedFor", searchedFor);
		session.setAttribute("simpleQuery", basicQueryString);
		String templateName = servlet.selectTemplate(request, response, context);
		assertEquals("dissatisfied set should get back feedbacksubmitted", "FeedbackSubmitted.vm", templateName);

		File[] feedbackFiles = tempFeedbackDir.listFiles();
		assertEquals("Should only have 1 file", 1, feedbackFiles.length);
		File dissatisfied = feedbackFiles[0];
		assertTrue("Filename should contain dissatisfied", dissatisfied.getAbsolutePath().indexOf(FeedbackSubmitted.FEEDBACK_DISSATISFIED_PREFIX) > 0);

		UnicodeReader reader = new UnicodeReader(dissatisfied);
		reader.readLine(); //basic query tag
		String basicQueryIn = reader.readLine();
		reader.readLine();//blank line
		reader.readLine(); //searched for tag
		String searchedForIn = reader.readLine();
		reader.readLine();//blank line
		reader.readLine();//message tag
		String dataIn = reader.readLine();
		reader.close();
		DirectoryUtils.deleteEntireDirectoryTree(tempFeedbackDir);
		assertEquals("Basic query didn't match?", basicQueryString, basicQueryIn);
		assertEquals("Searched For dind't match?", searchedFor, searchedForIn);
		assertEquals("data dind't match?", data, dataIn);
	}
	
	public void testDataSubmittedProblem() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = new MockContext();
		File tempFeedbackDir = createTempDirectory();
		
		FeedbackSubmitted servlet = new FeedbackSubmitted(tempFeedbackDir.getAbsolutePath());
		String data = "my message";
		String basicQueryString = "*test";
		String searchedFor = "test";
		request.putParameter("userFeedbackProblem",data);
		AmplifierServletSession session = request.getSession();
		session.setAttribute("simpleQuery", basicQueryString);
		session.setAttribute("searchedFor", searchedFor);
		session.setAttribute("typeOfSearch", "quick");
		String templateName = servlet.selectTemplate(request, response, context);
		assertEquals("problem set should get back feedbacksubmitted", "FeedbackSubmitted.vm", templateName);

		File[] feedbackFiles = tempFeedbackDir.listFiles();
		assertEquals("Should only have 1 file", 1, feedbackFiles.length);
		File techProblem = feedbackFiles[0];
		assertTrue("Filename should contain problem", techProblem.getAbsolutePath().indexOf(FeedbackSubmitted.FEEDBACK_TECH_PROBLEM_PREFIX) > 0);

		UnicodeReader reader = new UnicodeReader(techProblem);
		reader.readLine(); //basic query tag
		String basicQueryIn = reader.readLine();
		reader.readLine();//blank line
		reader.readLine(); //searched for tag
		String searchedForIn = reader.readLine();
		reader.readLine();//blank line
		reader.readLine();//message tag
		String dataIn = reader.readLine();
		reader.close();
		DirectoryUtils.deleteEntireDirectoryTree(tempFeedbackDir);
		
		assertEquals("Basic query didn't match?", basicQueryString, basicQueryIn);
		assertEquals("Searched For dind't match?", searchedFor, searchedForIn);
		assertEquals("data dind't match?", data, dataIn);
	}
	
	public void testAdvancdedSearch() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		MockAmplifierResponse response = null;
		Context context = new MockContext();
		File tempFeedbackDir = createTempDirectory();
	
		AdvancedSearchInfo advancedSearchedFor = createTestAdvancedSearchInfo(); 
		AmplifierServletSession session = request.getSession();
		session.setAttribute("defaultAdvancedSearch", advancedSearchedFor);
		session.setAttribute("searchedFor", "advanced search");
		session.setAttribute("typeOfSearch", "advanced");

		FeedbackSubmitted servlet = new FeedbackSubmitted(tempFeedbackDir.getAbsolutePath());
		String data = "my message";
		request.putParameter("userFeedbackProblem",data);
		String templateName = servlet.selectTemplate(request, response, context);
		assertEquals("problem set should get back feedbacksubmitted", "FeedbackSubmitted.vm", templateName);

		File[] feedbackFiles = tempFeedbackDir.listFiles();
		assertEquals("Should only have 1 file", 1, feedbackFiles.length);
		File techProblem = feedbackFiles[0];
		assertTrue("Filename should contain problem", techProblem.getAbsolutePath().indexOf(FeedbackSubmitted.FEEDBACK_TECH_PROBLEM_PREFIX) > 0);

		UnicodeReader reader = new UnicodeReader(techProblem);
		Vector vectorAdvancedSearch = servlet.getVectorOfAdvancedSearch(advancedSearchedFor);
		reader.readLine(); //Searched for tag
		for (int i = 0 ; i < advancedSearchedFor.getFields().size() ; ++i)
		{	
			String searchedForIn = reader.readLine();
			assertEquals("Search for data didn't match?", vectorAdvancedSearch.get(i), searchedForIn);
		}
		reader.readLine();//blank line
		reader.readLine();//message tag
		String dataIn = reader.readLine();
		reader.close();
		DirectoryUtils.deleteEntireDirectoryTree(tempFeedbackDir);

	
		assertEquals("data dind't match?", data, dataIn);
	}
	
	public void testTypeOfSearch() throws Exception
	{
		MockAmplifierRequest request = new MockAmplifierRequest();
		String basicSearchString = "mybasicsearch";
		request.putParameter("query", basicSearchString);
		request.putParameter("typeOfSearch", "quick");

		DoSearch servlet = new DoSearch();
		servlet.configureSessionFromRequest(request);
		AmplifierServletSession session = request.getSession();
		assertEquals("Didn't get back correct search type from session", "quick", session.getAttribute("typeOfSearch"));
	}
	
	
	AdvancedSearchInfo createTestAdvancedSearchInfo()
	{
		HashMap map = new HashMap();
		map.put(SearchResultConstants.EXACTPHRASE_TAG, "amp test");
		map.put(SearchResultConstants.ANYWORD_TAG, "amp");
		map.put(SearchResultConstants.THESE_WORD_TAG, "my test");
		map.put(SearchResultConstants.RESULT_LANGUAGE_KEY, "english");
		map.put(SearchResultConstants.RESULT_FIELDS_KEY, "title");
		return new AdvancedSearchInfo(map);		
	} 
	
}
