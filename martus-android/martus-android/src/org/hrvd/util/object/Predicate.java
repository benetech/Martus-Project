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
 * Predicate.java
 *
 * Created on January 16, 2002, 3:33 PM
 */

package org.hrvd.util.object;

/**
 * Interface to define a simple predicate that will apply itself to a single
 * argument.  Truth on each object is determined by implementation.
 *
 * @author  rafe
 * @version 
 */
public interface Predicate {
    /**
     * Method used to determine if a given object meets the predicate condition.
     *
     * @param o Object to test for condition
     */
    public boolean match( Object o );
}

/*=============================================================================
 *
 *  $Log$
 *  Revision 1.1  2003/08/15 16:28:52  uid528
 *  added new package for hrvd (AAAS flexidate source and proportiy files) and added a new martus flexidate class for wrapping the flexidate.
 *
 *  Revision 1.1  2002/12/09 23:50:15  rkaplan
 *  no message
 *
 *
 *****************************************************************************/
 
 