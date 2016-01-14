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
package org.martus.client.test;

import org.martus.client.swingui.UiSession;
import org.martus.clientside.CurrentUiState;
import org.martus.common.MiniLocalization;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.util.TestCaseEnhanced;

public class TestUiSession extends TestCaseEnhanced
{
    public TestUiSession(String name)
	{
		super(name);
	}
    
    public void setUp() throws Exception
    {
		super.setUp();
		TRACE_BEGIN("setUp");
   		mockSecurityForApp = MockMartusSecurity.createClient();
		appWithAccount = MockMartusApp.create(mockSecurityForApp, getName());
		TRACE_END();
	}

	public void tearDown() throws Exception
	{
		appWithAccount.deleteAllFiles();
		super.tearDown();
	}

	public void testInitializeUiState() throws Exception
    {
    		UiSession sessionNoAccountSignedInDefaultLanguage = new UiSession();
    		sessionNoAccountSignedInDefaultLanguage.initalizeUiState();
    		assertEquals("Default Language not set to English?", MiniLocalization.ENGLISH, sessionNoAccountSignedInDefaultLanguage.getUiState().getCurrentLanguage());
    		assertEquals("Default Calendar not Gregorian?", MiniLocalization.GREGORIAN_SYSTEM, sessionNoAccountSignedInDefaultLanguage.getUiState().getCurrentCalendarSystem());
    		assertEquals("Default DateFormat not MM/dd/yyyy?", "MM/dd/yyyy", sessionNoAccountSignedInDefaultLanguage.getUiState().getCurrentDateFormat());
    		
    		UiSession sessionNoAccountSignedInLanguageSpecified = new UiSession();
    		sessionNoAccountSignedInLanguageSpecified.initalizeUiState(MiniLocalization.FRENCH);
    		assertEquals("Not signed in: language should not change", MiniLocalization.ENGLISH, sessionNoAccountSignedInLanguageSpecified.getUiState().getCurrentLanguage());
      	assertEquals("Not signed in: Calendar not Gregorian?", MiniLocalization.GREGORIAN_SYSTEM, sessionNoAccountSignedInLanguageSpecified.getUiState().getCurrentCalendarSystem());
    		assertEquals("Not signed in: DateFormat not MM/dd/yyyy?", "MM/dd/yyyy", sessionNoAccountSignedInLanguageSpecified.getUiState().getCurrentDateFormat());
    		
    		UiSession sessionWithAccountSignedIn = new UiSession();
    		sessionWithAccountSignedIn.setAppForUnitTests(appWithAccount);
    		sessionWithAccountSignedIn.initalizeUiState(MiniLocalization.RUSSIAN);
    		assertEquals("signed in: language should now change", MiniLocalization.RUSSIAN, sessionWithAccountSignedIn.getUiState().getCurrentLanguage());
    		assertEquals("signed in: Calendar not Gregorian?", MiniLocalization.GREGORIAN_SYSTEM, sessionWithAccountSignedIn.getUiState().getCurrentCalendarSystem());
        	assertEquals("signed in: DateFormat not MM/dd/yyyy?", "MM/dd/yyyy", sessionWithAccountSignedIn.getUiState().getCurrentDateFormat());
     		
    		CurrentUiState uiState = sessionWithAccountSignedIn.getUiState();
    		uiState.setCurrentLanguage(MiniLocalization.BURMESE);
    		uiState.setCurrentDateFormat("dd.MM.yyyy");
    		uiState.setCurrentCalendarSystem(MiniLocalization.PERSIAN_SYSTEM);
    		sessionWithAccountSignedIn.saveCurrentUiState();
    		
    		sessionWithAccountSignedIn.initalizeUiState(MiniLocalization.SPANISH);
    		assertEquals("resigned in: language should change to language passed in, not what was last used.", MiniLocalization.SPANISH, sessionWithAccountSignedIn.getUiState().getCurrentLanguage());
    		assertEquals("resigned in: Calendar not PERSIAN_SYSTEM?", MiniLocalization.PERSIAN_SYSTEM, sessionWithAccountSignedIn.getUiState().getCurrentCalendarSystem());
        	assertEquals("resigned in: DateFormat not dd.MM.yyyy?", "dd.MM.yyyy", sessionWithAccountSignedIn.getUiState().getCurrentDateFormat());
    		
       	sessionWithAccountSignedIn.getUiStateFile().delete();
   }
	
	
	private MockMartusSecurity mockSecurityForApp;
	private MockMartusApp appWithAccount;
}
