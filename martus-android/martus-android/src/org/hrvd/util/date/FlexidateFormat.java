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
 * FlexidateFormat.java
 *
 * Created on May 15, 2002, 3:03 PM
 */

package org.hrvd.util.date;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.hrvd.util.res.Resources;

/**
 *
 * @author  rafe
 */
public class FlexidateFormat extends Format {
    
    private static final String COUNT_KEY = "date.format.count";
    
    private static final String [] CONTEXT_KEYS = {
        "date.format.day",
        "date.format.month",
        "date.format.year",
        "date.format.norange"
    };
    
    //private static final String NO_RANGE_KEY = "date.format.norange";
    
    private static final MessageFormat INDEX_FORMAT = new MessageFormat( "date.format.{0,number,integer}" );
    
    private static final String DATE_BUNDLE = "hrvd/date/dates";
    
    private static final Map localFormats = new HashMap( 16 );
    
    private MessageFormat formats[];
    
    private int day = 1;
    
    private int month = 1;
    
    private int year = 1;
    
    private int noRange = 1;
    
    private static final Object mutex = new Object();
    
    /** Creates a new instance of FlexidateFormat */
    public FlexidateFormat( String format ) {
        this.formats = new MessageFormat[] { new MessageFormat( format ) };
    }
    
    public FlexidateFormat( String formats[], int day, int month, int year, int noRange ) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.noRange = noRange;
        this.formats = new MessageFormat[ formats.length ];
        for ( int format = 0; format < formats.length; ++ format ) {
            this.formats[ format ] = new MessageFormat( formats[ format ] );
        }
    }
    
    protected FlexidateFormat( MessageFormat formats[], int day, int month, int year, int noRange ) {
        this.formats = formats;
        this.day = day;
        this.month = month;
        this.year = year;
        this.noRange = noRange;
    }
    
    public static FlexidateFormat getFormat( Locale locale ) {
        synchronized ( mutex ) {
            FlexidateFormat format = (FlexidateFormat)localFormats.get( locale );
            if ( format == null ) {
                ResourceBundle bundle = Resources.getBundle( DATE_BUNDLE );
                int count = Integer.parseInt( bundle.getString( COUNT_KEY ) );
                MessageFormat[] formats = new MessageFormat[ count ];
                for ( int index = 0; index < count; ++ index ) {
                    String formatString = bundle.getString( INDEX_FORMAT.format( new Object[] { new Integer( index + 1 ) } ) );
                    formats[ index ] = new MessageFormat( formatString );
                }
                int day = Integer.parseInt( bundle.getString( CONTEXT_KEYS[ Flexidate.DAY_CONTEXT ] ) );
                int month = Integer.parseInt( bundle.getString( CONTEXT_KEYS[ Flexidate.MONTH_CONTEXT ] ) );
                int year = Integer.parseInt( bundle.getString( CONTEXT_KEYS[ Flexidate.YEAR_CONTEXT ] ) );
                int noRange = Integer.parseInt( bundle.getString( CONTEXT_KEYS[ Flexidate.NO_CONTEXT ] ) );
                format = new FlexidateFormat( formats, day, month, year, noRange );
                localFormats.put( locale, format );
            }
            
            return format;
        }
    }
    
    public static FlexidateFormat getFormat( ) {
        return getFormat( Locale.getDefault() );
    }
    
    private static int toInteger( Object o ) {
        if ( o instanceof Number ) {
            return ((Number)o).intValue();
        }
        if ( o instanceof String ) {
            return Integer.parseInt( (String)o );
        }
        if ( o == null ) return 0;
        
        return Integer.parseInt( o.toString() );
    }
    
    public Flexidate parseFlexidate( String string ) throws ParseException {
        ParsePosition position = new ParsePosition( 0 );
        Flexidate date = parseFlexidate( string, position );
        
        if ( date == null )
            throw new java.text.ParseException( "Could not parse flexidate", position.getErrorIndex() );

        return date;
    }
    
    public Flexidate parseFlexidate( String string, ParsePosition position ) {
        for ( int format = 0; format < formats.length; ++format ) {
            int oldIndex = position.getIndex();
            int oldRangeIndex = position.getErrorIndex();
            Object[] parsed = formats[ format ].parse( string, position );
            if ( position.getIndex() != oldIndex ) {
                switch ( parsed.length ) {
                    case 0:
                        return Flexidate.UNKNOWN_DATE;

                    case 1:
                        return new Flexidate(   toInteger( parsed[ 0 ] ) );

                    case 2:
                        return new Flexidate(   toInteger( parsed[ 0 ] ),
                                                toInteger( parsed[ 1 ] ));

                    case 3:
                        return new Flexidate(   toInteger( parsed[ 0 ] ),
                                                toInteger( parsed[ 1 ] ),
                                                toInteger( parsed[ 2 ] ));

                    default:
                        return new Flexidate(   toInteger( parsed[ 0 ] ),
                                                toInteger( parsed[ 1 ] ),
                                                toInteger( parsed[ 2 ] ),
                                                toInteger( parsed[ 3 ] ));
                }
            }

            position.setIndex( oldIndex );
            position.setErrorIndex( oldRangeIndex );
        }
        return null;
    }

    protected String format( Flexidate date, int using ) {
        return formats[ using - 1 ].format( new Object[] {
                                            new Integer( date.getYear() ),
                                            new Integer( date.getMonth() ),
                                            new Integer( date.getDay() ),
                                            new Integer( date.getRange() )
                                        } );
    }
        
    protected StringBuffer format( Flexidate date, int using, StringBuffer toAppendTo, FieldPosition pos ) {
        return formats[ using - 1].format( new Object[] {
                                                new Integer( date.getYear() ),
                                                new Integer( date.getMonth() ),
                                                new Integer( date.getDay() ),
                                                new Integer( date.getRange() )
                                                },
                                           toAppendTo,
                                           pos );
    }
    
    public final String format( Flexidate date ) {
        if ( date.getRange() == 0 )
            return formatWithNoRange( date );
        return formatWithRange( date );
    }
    
    public final String formatWithRange( Flexidate date ) {
        switch( date.getRangeContext() ) {
            case Flexidate.DAY_CONTEXT:
                return format( date, day );
                
            case Flexidate.MONTH_CONTEXT:
                return format( date, month );
                
            case Flexidate.YEAR_CONTEXT:
                return format( date, year );
        }
        
        throw new AssertionError( "Invalid context for flexidate" );
    }
    
    public final String formatWithNoRange( Flexidate date ) {
        return format( date, noRange );
    }
    
    /**
     * Formats an object and appends the resulting text to a given string
     * buffer.
     * If the <code>pos</code> argument identifies a field used by the format,
     * then its indices are set to the beginning and end of the first such
     * field encountered.
     *
     * @param obj    The object to format
     * @param toAppendTo    where the text is to be appended
     * @param pos    A <code>FieldPosition</code> identifying a field
     *              in the formatted text
     * @return       the string buffer passed in as <code>toAppendTo</code>,
     *              with formatted text appended
     * @exception NullPointerException if <code>toAppendTo</code> or
     *           <code>pos</code> is null
     * @exception IllegalArgumentException if the Format cannot format the given
     *           object
     */
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if ( obj instanceof java.util.Date ) {
            obj = new Flexidate( (java.util.Date)obj );
        }
        if ( obj instanceof Flexidate ) {
            Flexidate date = (Flexidate)obj;
            if ( date.getRange() == 0 )
                return format( date, noRange, toAppendTo, pos );
            return format( date, 1, toAppendTo, pos );
        }
        
        throw new IllegalArgumentException( "FlexidateFormat can only accept Flexidate" );
    }
    
    /**
     * Parses text from a string to produce an object.
     * <p>
     * The method attempts to parse text starting at the index given by
     * <code>pos</code>.
     * If parsing succeeds, then the index of <code>pos</code> is updated
     * to the index after the last character used (parsing does not necessarily
     * use all characters up to the end of the string), and the parsed
     * object is returned. The updated <code>pos</code> can be used to
     * indicate the starting point for the next call to this method.
     * If an error occurs, then the index of <code>pos</code> is not
     * changed, the error index of <code>pos</code> is set to the index of
     * the character where the error occurred, and null is returned.
     *
     * @param source A <code>String</code>, part of which should be parsed.
     * @param pos A <code>ParsePosition</code> object with index and error
     *           index information as described above.
     * @return An <code>Object</code> parsed from the string. In case of
     *        error, returns null.
     * @exception NullPointerException if <code>pos</code> is null.
     */
    public Object parseObject(String source, ParsePosition pos) {
        return parseFlexidate( source, pos );
    }
    
    public static void main( String args[] ) {
        try {
            String dateString = "5/0/2002 12";
            FlexidateFormat format = getFormat();
            Flexidate date = format.parseFlexidate( dateString );
            System.out.println( format.format( date ) );
            System.out.println( format.formatWithRange( date ) );
            System.out.println( format.formatWithNoRange( date ) );
            org.hrvd.util.object.ObjectRange r = date.getCalendarRange();
            DateFormat dFormat = DateFormat.getInstance();
            
            System.out.println( "y:" + date.getYear() + " m:" + date.getMonth() + " d:" + date.getDay() );
            
            System.out.println( "Low: " + dFormat.format( ((Calendar)r.getLow()).getTime() ) );
            System.out.println( "High: " + dFormat.format( ((Calendar)r.getHigh()).getTime() ) );
        }
        catch ( Throwable t ) {
            t.printStackTrace( System.out );
        }
    }
    
}
/*=============================================================================
 *
 *  $Log$
 *  Revision 1.1  2003/08/15 16:28:52  uid528
 *  added new package for hrvd (AAAS flexidate source and proportiy files) and added a new martus flexidate class for wrapping the flexidate.
 *
 *  Revision 1.4  2003/06/18 20:57:43  rkaplan
 *  Stupendous merge from other branches.
 *  Seperated biography from case.
 *  Seperated folder and document and content.
 *
 *  Revision 1.3.8.1  2003/05/29 23:10:26  rkaplan
 *  Cleaned up a bit of code.
 *
 *  Revision 1.3  2003/04/18 18:29:17  rkaplan
 *  Formats differently for different date formats.
 *
 *  Revision 1.2  2003/02/07 23:21:34  rkaplan
 *  Will also handle dates
 *
 *  Revision 1.1  2002/12/09 23:50:15  rkaplan
 *  no message
 *
 *
 *****************************************************************************/
 
 