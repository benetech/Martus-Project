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
 * AbstractRange.java
 *
 * Created on May 15, 2002, 12:58 AM
 */

package org.hrvd.util.object;

import java.util.Comparator;

/**
 *
 * @author  rafe
 */
public class ObjectRange {
    
    private Object high;
    private Object low;
    private Comparator comparator;
    
    /** Creates a new instance of AbstractRange */
    public ObjectRange(Object o, Comparator comparator) {
        this( o, o, comparator );
    }
    
    public ObjectRange(Object o1, Object o2, Comparator comparator) {
        if ( o1 == null )
            throw new IllegalArgumentException( "Range element 1 must not be null" );
        if ( o2 == null ) 
            throw new IllegalArgumentException( "Range element 2 must not be null" );
        if ( comparator == null )
            throw new IllegalArgumentException( "Comparator must not be null" );
        
        if ( comparator.compare( o1, o2 ) >= 0 ) {
            high = o1;
            low = o2;
        }
        else {
            high = o2;
            low = o1;
        }
        
        this.comparator = comparator;
    }
    
    public final Object getHigh( ) {
        return high;
    }
    
    public final Object getLow( ) {
        return low;
    }
    
    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The <code>equals</code> method implements an equivalence relation:
     * <ul>
     * <li>It is <i>reflexive</i>: for any reference value <code>x</code>,
     *    <code>x.equals(x)</code> should return <code>true</code>.
     * <li>It is <i>symmetric</i>: for any reference values <code>x</code> and
     *    <code>y</code>, <code>x.equals(y)</code> should return
     *    <code>true</code> if and only if <code>y.equals(x)</code> returns
     *    <code>true</code>.
     * <li>It is <i>transitive</i>: for any reference values <code>x</code>,
     *    <code>y</code>, and <code>z</code>, if <code>x.equals(y)</code>
     *    returns  <code>true</code> and <code>y.equals(z)</code> returns
     *    <code>true</code>, then <code>x.equals(z)</code> should return
     *    <code>true</code>.
     * <li>It is <i>consistent</i>: for any reference values <code>x</code>
     *    and <code>y</code>, multiple invocations of <tt>x.equals(y)</tt>
     *    consistently return <code>true</code> or consistently return
     *    <code>false</code>, provided no information used in
     *    <code>equals</code> comparisons on the object is modified.
     * <li>For any non-null reference value <code>x</code>,
     *    <code>x.equals(null)</code> should return <code>false</code>.
     * </ul>
     * <p>
     * The <tt>equals</tt> method for class <code>Object</code> implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any reference values <code>x</code> and <code>y</code>,
     * this method returns <code>true</code> if and only if <code>x</code> and
     * <code>y</code> refer to the same object (<code>x==y</code> has the
     * value <code>true</code>).
     * <p>
     * Note that it is generally necessary to override the <tt>hashCode</tt>
     * method whenever this method is overridden, so as to maintain the
     * general contract for the <tt>hashCode</tt> method, which states
     * that equal objects must have equal hash codes.
     *
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *         argument; <code>false</code> otherwise.
     * @see     #hashCode()
     * @see     java.util.Hashtable
     */
    public boolean equals(Object obj) {
        if ( obj instanceof ObjectRange ) {
            ObjectRange range = (ObjectRange)obj;
            return high.equals( range.high ) && low.equals( range.low );
        }
        
        return false;
    }
    
    public boolean implies( ObjectRange range ) {
        return  comparator.compare( low, range.low ) >= 0 &&
                comparator.compare( high, range.high ) <= 0;
    }
    
    public final boolean isOverlapped( ObjectRange range ) {
        return Comparison.rangeOverlap( low, high, range.low, range.high, comparator );
    }
    
    public final boolean isWithin( Object object ) {
        int lowCheck = comparator.compare( low, object );
        int highCheck = comparator.compare( high, object );
        
        return lowCheck <= 0 && highCheck >= 0;
    }
    
    public static Predicate overlapPredicate( final ObjectRange range ) {
        return new Predicate() {
            public boolean match( Object o ) {
                if ( o instanceof ObjectRange ) {
                    return range.isOverlapped( (ObjectRange)o );
                }
                else {
                    return range.isWithin( o );
                }
            }
        };
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
 *  Revision 1.1  2002/12/09 23:50:15  rkaplan
 *  no message
 *
 *
 *****************************************************************************/
 
 