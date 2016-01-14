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
 * Comparison.java
 *
 * Created on April 19, 2002, 9:12 PM
 */

package org.hrvd.util.object;

import java.util.Comparator;

/**
 * Utility functions for doing generic and safe comparisons.
 * @author rafe
 */
public class Comparison {
    
    /** Creates a new instance of Comparison */
    private Comparison() {
    }
    
    /** Compares two objects including when one or both of them is <CODE>null</CODE>.
     * @param o1 1st object to compare
     * @param o2 2nd object to compare
     * @return <CODE>true</CODE> if objects are equal or both null,
     * otherwise <CODE>false</CODE>.
     */    
    public static boolean safeEquals( Object o1, Object o2 ) {
        if ( o1 == o2 ) return true;
        if ( o1 == null ) return false;
        if ( o2 == null ) return false;
        return o1.equals( o2 );
    }
    
    /** Does comparison with two comparable objects even if both are <CODE>null</CODE>.  An object
     * that has a value of <CODE>null</CODE> is considered of lower ordinal value than one
     * that is not regardless of its value.
     * @param c1 1st <CODE>Comparable</CODE> to compare
     * @param c2 2nd <CODE>Comparable</CODE> to compare
     * @return As per the <CODE>java.lang.Comparable</CODE> interface, except -1 if 1st object
     * is null and second object is not, but 1 if 2nd object is <CODE>null</CODE> and
     * 1st object is not.  0 if both objects are <CODE>null</CODE>.
     */    
    public static int safeCompare(Comparable c1, Comparable c2) {
        if ( c1 == c2 ) return 0;
        if ( c1 == null ) return -1;
        if ( c2 == null ) return 1;
        return c1.compareTo( c2 );
    }
    
    /** Does comparison with two objects and considers them equal if neither
     * have a null value.  This method is intended to be used in comparators
     * to determine if two objects are sorted by one of them being null.
     * @param o1 1st <CODE>Object</CODE> to compare
     * @param o2 2nd <CODE>Object</CODE> to compare
     * @return -1 if 1st object
     * is null and second object is not, but 1 if 2nd object is <CODE>null</CODE> and
     * 1st object is not.  0 if both objects are <CODE>null</CODE>.
     */    
    public static int safeAddressCompare(Object o1, Object o2) {
        if ( o1 == null && o2 == null ) return 0;
        if ( o1 == null ) return -1;
        if ( o2 == null ) return 1;
        return 0;
    }
    
    /**
     * Simple helper to compare two integers to determine ordinality.
     * @param i1 Integer 1
     * @param i2 Integer 2
     * @return Integer ordinality as defined by <CODE>Comparable</CODE> class
     */    
    public static int compare( int i1, int i2 ) {
        if ( i1 > i2 ) return 1;
        if ( i1 < i2 ) return -1;
        return 0;
    }
   
    /**
     * Simple helper to compare two integers to determine ordinality.
     * @param i1 Long 1
     * @param i2 Long 2
     * @return Integer ordinality as defined by <CODE>Comparable</CODE> class
     */    
    public static int compare( long i1, long i2 ) {
        if ( i1 > i2 ) return 1;
        if ( i1 < i2 ) return -1;
        return 0;
    }
        
    /**
     * Creates a new <CODE>Comparator</CODE> that defines the inverse ordinality for a
     * given <CODE>Comparator</CODE>
     * @param comparator Comparator to invert
     * @return Comparator that returns the inverse ordinality for the <CODE>Comparator</CODE>
     * parameter.
     */    
    public static Comparator invert( final Comparator comparator ) {
        if ( comparator == null ) throw new NullPointerException();
        return new Comparator() {
            public int compare( Object o1, Object o2 ) {
                return -comparator.compare( o1, o2 );
            }
        };
    }
    
        
    public static boolean rangeOverlap( int low1, int high1, int low2, int high2 ) {
        if ( low1 > low2 ) {
            return low1 <= high2;
        }
        
        return low2 <= high1;
    }

    public static boolean rangeOverlap(     Comparable low1,
                                            Comparable high1,
                                            Comparable low2,
                                            Comparable high2 ) {
        // Shouls have assertion here to check parameters
        // assert low1 <= high1 && low2 <= high2                                                
                                                
        if ( low1.compareTo( low2 ) > 0 ) {
            return low1.compareTo( high2 ) <= 0;
        }
        
        return low2.compareTo( high1 ) <= 0;
    }

    public static boolean rangeOverlap(     Object low1,
                                            Object high1,
                                            Object low2,
                                            Object high2,
                                            Comparator comparator) {
        // Shouls have assertion here to check parameters
        // assert low1 <= high1 && low2 <= high2                                                
                                                
        if ( comparator.compare( low1, low2 ) > 0 ) {
            return comparator.compare( low1, high2 ) <= 0;
        }
        
        return comparator.compare( low2, high1 ) <= 0;
    }
    
    public static boolean deepEquals( Object[] o1, Object[] o2 ) {
        if ( o1 == o2 ) return true;
        if ( o1 == null || o2 == null ) return false;
        if ( o1.length != o2.length ) return false;
        
        for ( int index = 0; index < o1.length; ++ index ) {
            if ( !safeEquals( o1[ index], o2[ index ] ) ) return false;
        }
        
        return true;
    }
    
    public static boolean deepEquals( int[] o1, int[] o2 ) {
        if ( o1 == o2 ) return true;
        if ( o1 == null || o2 == null ) return false;
        if ( o1.length != o2.length ) return false;
        
        for ( int index = 0; index < o1.length; ++ index ) {
            if ( o1[ index] != o2[ index ] ) return false;
        }
        
        return true;
    }
}
/*=============================================================================
 *
 *  $Log$
 *  Revision 1.1  2003/08/15 16:28:52  uid528
 *  added new package for hrvd (AAAS flexidate source and proportiy files) and added a new martus flexidate class for wrapping the flexidate.
 *
 *  Revision 1.4  2003/07/22 22:03:36  rkaplan
 *  Merged from 0.4 version
 *
 *  Revision 1.3.2.1  2003/07/22 04:18:56  rkaplan
 *  Added deep equals for integer
 *
 *  Revision 1.3  2003/06/18 20:57:43  rkaplan
 *  Stupendous merge from other branches.
 *  Seperated biography from case.
 *  Seperated folder and document and content.
 *
 *  Revision 1.2.6.1  2003/06/05 23:30:36  rkaplan
 *  Added long comparison.
 *
 *  Revision 1.2  2003/05/13 16:09:48  rkaplan
 *  Has deep equals methods for comparing arrays.
 *
 *  Revision 1.1  2002/12/09 23:50:15  rkaplan
 *  no message
 *
 *
 *****************************************************************************/
 
 