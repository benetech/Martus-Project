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
 * Flexidate.java
 *
 * Created on May 14, 2002, 7:19 PM
 */

package org.hrvd.util.date;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import org.hrvd.util.object.Comparison;
import org.hrvd.util.object.ObjectRange;
import org.hrvd.util.object.Predicate;

/**
 *
 * @author  rafe
 */
public final class Flexidate implements java.lang.Comparable {

    public static final int DAY_CONTEXT = 0;
    public static final int MONTH_CONTEXT = 1;
    public static final int YEAR_CONTEXT = 2;
    public static final int NO_CONTEXT = 3;
    
    public static final Flexidate UNKNOWN_DATE = new Flexidate( 0 );
    
    private static final int DAY_MULTIPLIER = 1;
    
    private static final int MONTH_MULTIPLIER = 100 * DAY_MULTIPLIER;

    private static final int YEAR_MULTIPLIER = 100 * MONTH_MULTIPLIER;

    
    private int range;
    
    private int date;
    
    private ObjectRange calendarRange = null;
    
    private Calendar calendarHigh = null;
    
    private Calendar calendarLow = null;
    
    private static final Comparator CALENDAR_COMPARATOR =
        new Comparator() {
            public int compare( Object o1, Object o2 ) {
                Calendar c1 = (Calendar)o1;
                Calendar c2 = (Calendar)o2;
                
                if ( c1.before( c2 ) ) return -1;
                if ( c1.after( c2 ) ) return 1;
                return 0;
            }
        };
    
    /** Creates a new instance of Flexidate */
    public Flexidate( int year, int month, int day, int range ) {
        set( year, month, day, range );
    }
    
    public Flexidate( int year, int month, int day ) {
        this( year, month, day, 0 );
    }
    
    public Flexidate( int year, int month ) {
        this( year, month, 0 );
    }
    
    public Flexidate( int year ) {
        this( year, 0 );
    }
    
    /**
     * Accepting a long as the integer encoding!  Forward thinking?  No just
     * hacking.  Barf.  - Rafe
     */
    public Flexidate( long encoding ) {
        this( encoding, 0 );
    }

    public Flexidate( long encoding, int range ) {
        this.date = (int)encoding;
        this.range = range;
    }
    
    public Flexidate( Date low, Date high ) {
        if ( high.before( low ) ) {
            Date temp = low;
            low = high;
            high = temp;
        }
        long difference = high.getTime() - low.getTime();
        int days = (int)(difference / (1000L * 60L * 60L * 24L));

        Calendar calendar = new GregorianCalendar( );
        calendar.setTime( low );
        
        set( calendar, days );
    }
    
    public Flexidate( Calendar low, Calendar high ) {
        if ( high.before( low ) ) {
            Calendar temp = low;
            low = high;
            high = temp;
        }
        long difference = high.getTime().getTime() - low.getTime().getTime();
        int days = (int)(difference / (1000L * 60L * 60L * 24L));

        set( low, days );
    }
    
    public Flexidate( ) {
        this( new GregorianCalendar() );
    }

    public Flexidate( java.util.Date date, int range ) {
        Calendar calendar = new GregorianCalendar( );
        calendar.setTime( date );
        
        set( calendar, range );
    }

    public Flexidate( java.util.Date date ) {
        this( date, 0 );
    }
    
    public Flexidate( Calendar calendar, int range ) {
        set( calendar, range );
    }
    
    public Flexidate( Calendar calendar ) {
        this( calendar, 0 );
    }

    private final void set( Calendar calendar, int range ) {
        set(    calendar.get( Calendar.YEAR ),
                calendar.get( Calendar.MONTH ) + 1,
                calendar.get( Calendar.DAY_OF_MONTH ), range );
    }
    
    private final void set( int year, int month, int day, int range ) {
        
        date =  (year * YEAR_MULTIPLIER) +
                (month * MONTH_MULTIPLIER) + 
                day;
        this.range = range;
    }
    
    public final int getDay( ) {
        return date % MONTH_MULTIPLIER;
    }
    
    public final int getMonth( ) {
        return (date % YEAR_MULTIPLIER) / MONTH_MULTIPLIER;
    }
    
    public final int getYear( ) {
        return date / YEAR_MULTIPLIER;
    }
    
    public final int getRange( ) {
        return range;
    }
    
    public final int getRangeContext( ) {
        if ( getDay() != 0 ) return DAY_CONTEXT;
        if ( getMonth() != 0 ) return MONTH_CONTEXT;
        if ( getYear() != 0 ) return YEAR_CONTEXT;
        return NO_CONTEXT;
    }
    
    public boolean equals( Object o ) {
        if ( o instanceof Flexidate ) {
            Flexidate other = (Flexidate)o;
            return other.date == date && other.range == range;
        }
        
        return false;
    }
    
    public boolean isOverlapped( Flexidate date ) {
        return getCalendarRange().isOverlapped( date.getCalendarRange() );
    }
    
    public boolean implies( Flexidate date ) {
        return getCalendarRange().implies( date.getCalendarRange() );
    }
    
    public Calendar getCalendarLow( ) {
        if ( calendarLow == null ) {
            int context = getRangeContext();
            switch ( context ) {
                case NO_CONTEXT:
                    // Need to define better lowest unknown
                    calendarLow = new GregorianCalendar( 0, Calendar.JANUARY, 1, 1, 0, 0 );
                    break;

                case YEAR_CONTEXT:
                    calendarLow = new GregorianCalendar( getYear(), Calendar.JANUARY, 1, 1, 0, 0 );
                    break;

                case MONTH_CONTEXT:
                    calendarLow = new GregorianCalendar( getYear(), getMonth() - 1, 1, 1, 0, 0 );
                    break;

                case DAY_CONTEXT:
                    calendarLow = new GregorianCalendar( getYear(), getMonth() - 1, getDay(), 1, 0 ,0 );
                    break;

                default:
                    throw new java.lang.AssertionError( "Flexidate produced invalid context" );
            }
        }
        return calendarLow;
    }
    
    public Calendar getCalendarHigh( ) {
        if ( calendarHigh == null ) {
            int context = getRangeContext();
            switch ( context ) {
                case NO_CONTEXT:
                    // Need to define better highest unknown
                    calendarHigh = new GregorianCalendar( 9999, Calendar.JANUARY, 1, 1, 0, 0 );
                    break;

                case YEAR_CONTEXT:
                    calendarHigh = new GregorianCalendar( getYear() + range, Calendar.DECEMBER + 1, 0, 1, 0 ,0 );
                    break;

                case MONTH_CONTEXT:
                    calendarHigh = new GregorianCalendar( getYear(), getMonth() + range, 0, 1, 0, 0 );
                    break;

                case DAY_CONTEXT:
                    calendarHigh = new GregorianCalendar( getYear(), getMonth() - 1, getDay() + range, 1, 0, 0 );
                    break;

                default:
                    throw new java.lang.AssertionError( "Flexidate produced invalid context" );
           }
        }
        
        return calendarHigh;
    }
        
    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     *
     * In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of <i>expression</i>
     * is negative, zero or positive.
     *
     * The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)<p>
     *
     * The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.<p>
     *
     * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.<p>
     *
     * It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * @param   o the Object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     * 		is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *        from being compared to this Object.
     */
    public int compareTo(Object o) {
        if ( o instanceof Flexidate ) {
            return Comparison.compare( date, ((Flexidate)o).date );
        }
        else if ( o instanceof java.util.Date ) {
            // Replace this with something more efficient!!
            return compareTo( new Flexidate( (Date)o ) );
        }
        else if ( o instanceof java.util.Calendar ) {
            // Replace this with something more efficient!!
            return compareTo( new Flexidate( (Calendar)o ) );
        }
        
        throw new ClassCastException( "Flexidate can only compare with Flexidate, Date or GregorianCalendar" );
    }
    
    public ObjectRange getCalendarRange( ) {
        if ( calendarRange == null ) {
            int context = getRangeContext();
            if ( range == 0 && context != NO_CONTEXT ) {
                Calendar low = getCalendarLow();
                calendarRange = new ObjectRange( low, CALENDAR_COMPARATOR );
            }

            calendarRange = new ObjectRange( getCalendarLow(), getCalendarHigh(), CALENDAR_COMPARATOR );
        }
        
        return calendarRange;
    }
    
    public static Predicate createOverlappingPredicate( final Flexidate flexidate ) {
        return new Predicate() {
            public boolean match( Object o ) {
                if ( o instanceof Flexidate ) {
                    return flexidate.isOverlapped( (Flexidate)o );
                }

                return false;
            }
        };
    }
    
    public String toString( ) {
        return FlexidateFormat.getFormat().format( this );
    }
    
    public long getDateAsNumber( ) {
        return date;
    }
    
    public static void main( String args[] ) {
        try {
            Calendar date1 = new GregorianCalendar( 1999, 5, 5 );
            Calendar date2 = new GregorianCalendar( 1999, 5, 5 );
            
            Flexidate fdate = new Flexidate( date1, date2 );
            System.out.println( fdate );
        }
        catch ( Throwable t ) {
            t.printStackTrace( System.err );
        }
    }
    
}
/*=============================================================================
 *
 *  $Log$
 *  Revision 1.2  2003/09/11 23:02:43  uid506
 *  removed unused local variables
 *
 *  Revision 1.1  2003/08/15 16:28:52  uid528
 *  added new package for hrvd (AAAS flexidate source and proportiy files) and added a new martus flexidate class for wrapping the flexidate.
 *
 *  Revision 1.3  2003/06/18 20:57:43  rkaplan
 *  Stupendous merge from other branches.
 *  Seperated biography from case.
 *  Seperated folder and document and content.
 *
 *  Revision 1.2.8.1  2003/05/29 23:10:26  rkaplan
 *  Cleaned up a bit of code.
 *
 *  Revision 1.2  2002/12/11 21:37:54  rkaplan
 *  Corrected how it reads from calendar.  java.util.Calendar returns months starting at 0.  Add one to get Flexidate storage of month.
 *
 *  Revision 1.1  2002/12/09 23:50:15  rkaplan
 *  no message
 *
 *
 *****************************************************************************/
 
 