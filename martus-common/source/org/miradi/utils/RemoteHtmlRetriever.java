/*
Copyright 2005-2009, Foundations of Success, Bethesda, Maryland
(on behalf of the Conservation Measures Partnership, "CMP") and
Beneficent Technology, Inc. ("Benetech"), Palo Alto, California.

This file is part of Miradi

Miradi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License version 3,
as published by the Free Software Foundation.

Miradi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Miradi.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.miradi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class RemoteHtmlRetriever extends Thread
{
        public RemoteHtmlRetriever(URL urlToLoad)
        {
                url = urlToLoad;
        }
       
        @Override
        public void run()
        {
                try
                {
                        results = readContext((InputStream)url.getContent());
                }
                catch(Exception e)
                {
                        results = null;
                }
        }
       
        public String getResults()
        {
                return results;
        }
       
        private String readContext(InputStream inputStream) throws IOException
        {
                InputStreamReader isr = new InputStreamReader(inputStream);
                String returnLine = "";
                String thisLine;
                BufferedReader br = new BufferedReader(isr);
                while((thisLine = br.readLine()) != null)
                {
                        returnLine = returnLine + thisLine;
                }
                return returnLine;
        }
       
        private URL url;
        private String results;
}

