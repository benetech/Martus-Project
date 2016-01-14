/*
 *    Copyright 2013 Alec Dhuse
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

/* 
 * This module was obtained from:
 * https://github.com/alecdhuse/Java-Jpeg-Geotag-Reader
 * 
 * Changes made by Benetech:
 * 1. Added package declaration of org.martus.common.utilities
 * 2. Added hasData member and getter
 */

package org.martus.common.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * Represents a four dimensional point where/when an photo was taken.
 */
public final class GeoTag {
    private double              altitude, latitude, longitude;
    private GregorianCalendar   calendar;
    private String              latitudeRef, longitudeRef;
    private boolean				hasData;
    
    /**
     * Default constructor with no parameters.  Information is added through
     * the set methods.
     * 
     */
    public GeoTag() {
        calendar     = new GregorianCalendar();
        latitudeRef  = "";
        longitudeRef = "";
        
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    /**
     * 
     * @return the latitude in decimal degrees. 
     */
    public double getLatitude() {
        return this.latitude;
    }    
    
    /**
     * @return the Altitude of this location, in meters above sea level.
     */
    public double getAltitude() {
        return this.altitude;
    }

    /**     
     * 
     * @return the date as UTC milliseconds from the epoch.
     */
    public long getDateInMillis() {
        return calendar.getTimeInMillis();
    }
    
    /**
     * @return the longitude in decimal degrees.
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * 
     * @return If the Latitude is (N)orth or (S)outh.
     */
    public String getLatitudeReference() {        
        return this.latitudeRef;
    }  
    
    /**
     * 
     * @return If the Longitude is (E)ast or (W)est.
     */
    public String getLongitudeReference() {
        return this.longitudeRef;
    }      
    
    /**
     * 
     * @return the timestamp as a string in the format yyyy-MM-dd'T'hh:mm:ssZ
     */
    public String getTimestamp() {             
        SimpleDateFormat timestampDateFormat;
                
        timestampDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
        timestampDateFormat.setTimeZone(new SimpleTimeZone(0, "Z"));
                
        return timestampDateFormat.format(calendar.getTimeInMillis())  + "Z";       
    }    
    
    /**
     * Converts degrees-minutes-seconds into a single value in decimal degrees.
     * 
     * @param degs
     * @param mins
     * @param secs
     * @return Decimal degrees value from the degrees, minutes and seconds vals.
     */
    public static double degreesToDecimal(double degs, double mins, double secs) {
        double decimal = Math.abs(degs) + (mins / 60) + (secs / 3600);

        return decimal;
    }

    /**
     * Sets the altitude value.
     * 
     * @param altitude In meters above sea level.
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
        this.hasData = true;
    }
    
    /**
     * Sets the date from a String value.
     * 
     * @param date Must be in year:mm:dd format.
     */
    public void setDate(String date) {
        StringTokenizer st = new StringTokenizer(date, ":");
        
        if (st.countTokens() == 3) {
            calendar.set(Calendar.YEAR,         Integer.parseInt(st.nextToken()));
            calendar.set(Calendar.MONTH,        Integer.parseInt(st.nextToken()) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(st.nextToken()));
        }
        this.hasData = true;
    }    
    
    /**
     * Sets the latitude value.
     * 
     * @param latitude double value between -90 and 90.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
        
        if (latitudeRef.equalsIgnoreCase("N")) {
            if (this.latitude < 0)
                this.latitude *= -1;
        } else if (latitudeRef.equalsIgnoreCase("S")) {
            if (this.latitude > 0)
                this.latitude *= -1;            
        }        
        this.hasData = true;
    }    
    
    /**
     * Sets the hemisphere or reference for the latitude.
     * 
     * @param latitudeRef String value either "N" or "S".
     */
    public void setLatitudeReference(String latitudeRef) {
        this.latitudeRef = latitudeRef;
        
        if (latitudeRef.equalsIgnoreCase("N")) {
            if (latitude < 0)
                latitude *= -1;
        } else if (latitudeRef.equalsIgnoreCase("S")) {
            if (latitude > 0)
                latitude *= -1;            
        }
        this.hasData = true;
    }  
    
    /**
     * Sets the longitude value.
     * 
     * @param longitude double value between -180 and 180.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
        
        if (longitudeRef.equalsIgnoreCase("E")) {
            if (this.longitude < 0)
                this.longitude *= -1;
        } else if (longitudeRef.equalsIgnoreCase("W")) {
            if (this.longitude > 0)
                this.longitude *= -1;            
        }         
        this.hasData = true;
    }    
    
    /**
     * Sets the hemisphere or reference for the longitude.
     * 
     * @param longitudeRef String value either "E" or "W".
     */
    public void setLongitudeReference(String longitudeRef) {
        this.longitudeRef = longitudeRef;
        
        if (longitudeRef.equalsIgnoreCase("E")) {
            if (longitude < 0)
                longitude *= -1;
        } else if (longitudeRef.equalsIgnoreCase("W")) {
            if (longitude > 0)
                longitude *= -1;            
        }        
        this.hasData = true;
    }     
    
    /**
     * Sets the time for this GeoInfo Object.
     * 
     * @param hour The hour in 24 hour format.
     * @param min  The minute.
     * @param sec  The second.
     */
    public void setTime(double hour, double min, double sec) {
        calendar.set(Calendar.HOUR_OF_DAY, (int) hour);
        calendar.set(Calendar.MINUTE,      (int) min);
        calendar.set(Calendar.SECOND,      (int) sec);
        this.hasData = true;
    }
    
    /**
     * @return a string representation of this GeoInfo object.
     */
    @Override
    public String toString() {
        return latitude + ", " + longitude + ", " + altitude + " " + getTimestamp();
    }

    /**
     * Added by Benetech
     * 
     * @return true if geo data was set; otherwise false
     */
	public boolean hasData()
	{
		return hasData;
	}

}
