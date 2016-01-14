/******************************************************************************
 *
 * $Id$
 *
 * $Revision$
 *
 * $Date$
 *
 *=============================================================================
 *
 *  Human Rights Violation Database Analyzer
 *
 *  Copyright (C) 2002 American Association for the Advancement of Science
 *                     (hrdag-analyzer@aaas.org)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *****************************************************************************/
/*
 * Resources.java
 *
 * Created on May 8, 2002, 1:39 PM
 */

package org.hrvd.util.res;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class to encapsulate how internationalization is done throughout the
 * whole system.  Mostly delegates to ResourceBundle.
 * @author rafe
 */
public class Resources {
    
    /** Creates a new instance of Resources */
    private Resources() {
    }
    
    /**
     * Resolve a particular string value.
     * @param resource Name of resource to get value from
     * @param name Name of value
     * @return Internationalized value of resource
     */    
    public static String resolve( String resource, String name ) {
        return ResourceBundle.getBundle( resource ).getString( name );
    }
    
    
    /**
     * Resolve a particular string value given <CODE>Locale</CODE>.
     * @param resource Name of resource to get value from
     * @param locale Locale for resource fetch
     * @param name Name of value
     * @return Internationalized value of resource
     */    
    public static String resolve( String resource, Locale locale, String name ) {
        return ResourceBundle.getBundle( resource, locale ).getString( name );
    }
    
    /**
     * Fetches a resource bundle.
     * @param resource Name of resource bundle to fetch.
     * @return Resource bundle associated with resource name
     */    
    public static ResourceBundle getBundle( String resource ) {
        return ResourceBundle.getBundle( resource );
    }
    
    public static final void main( String args[] ) {
        
        System.out.println( Thread.currentThread().getContextClassLoader().getClass().getName() );
    }
}
/*=============================================================================
 *
 *  $Log$
 *  Revision 1.1  2003/08/15 16:28:52  uid528
 *  added new package for hrvd (AAAS flexidate source and proportiy files) and added a new martus flexidate class for wrapping the flexidate.
 *
 *  Revision 1.2  2003/06/18 20:57:43  rkaplan
 *  Stupendous merge from other branches.
 *  Seperated biography from case.
 *  Seperated folder and document and content.
 *
 *  Revision 1.1.8.1  2003/05/29 23:10:26  rkaplan
 *  Cleaned up a bit of code.
 *
 *  Revision 1.1  2002/12/09 23:50:16  rkaplan
 *  no message
 *
 *
 *****************************************************************************/
 
 