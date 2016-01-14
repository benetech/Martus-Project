/*  
 *    Copyright 2013 Alec Dhuse
 *    Copyright 2002-2012 Drew Noakes
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
 */

/* 
 * This module was obtained from:
 * https://github.com/alecdhuse/Java-Jpeg-Geotag-Reader
 * 
 * Changes made by Benetech:
 * 1. Added package declaration of org.martus.common.utilities
 * 2. Eliminated compiler warnings:
 *    a. Commented out several extraneous "else" keywords, to avoid compiler warnings
 *    b. Commented out unused local variable and all references to it: offset
 *    c. Commented out unused local variable: segmentIdentifier
 *    d. Commented out 2 unnecessary casts to long
 * 3. Refactored to expose readMetadata(inputStream)
 * 4. Throw specific exception class for non-JPEG file
 */

package org.martus.common.utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Adapted from Metadata-Extractor by Drew Noakes.  This class will read the 
 * exif headers from a jpeg file and return a GeoTag class with the location
 * and time the photo was taken. 
 *
 * @author 
 */
public class JpegGeoTagReader {

    public static final byte[] BYTES_PER_FORMAT = {1, 1, 1, 2, 4, 8};

    public static final int  GPS_INFO_OFFSET     = 34853;
    public static final int  TIFF_HEADER_OFFSET  = 6;
    public static final byte SEGMENTS_END        = (byte) 218;    
            
    public static final byte GPS_LATITUDE_REF    = 1;
    public static final byte GPS_LATITUDE        = 2;
    public static final byte GPS_LONGITUDE_REF   = 3;
    public static final byte GPS_LONGITUDE       = 4;
    public static final byte GPS_ALTITUDE        = 6;
    public static final byte GPS_TIME            = 7;
    public static final byte GPS_DATE            = 29;    
    
    private ArrayList<byte[]> segmentDataList;        
    private boolean           isBigEndianByteOrder;
    private byte[]            byteBuffer;
    
    public JpegGeoTagReader() {
        
    }   
    
    /**
     * Determine the offset at which a given tag starts.
     *
     * @param dirStartOffset the offset at which the IFD starts
     * @param entryNumber    the zero-based entry number
     */
    private static int calculateTagOffset(int startOffset, int entryNumber) {
        // add 2 bytes for the tag count
        // each entry is 12 bytes, so we skip 12 * the number seen so far
        return startOffset + 2 + (12 * entryNumber);
    }    
    
    private GeoTag extract() throws Exception {
        GeoTag geo = new GeoTag();
                
        // this should be either "MM" or "II"
        String byteOrderIdentifier = getString(TIFF_HEADER_OFFSET, 2);

        if (byteOrderIdentifier.equals("MM")) {
            //big-endian byte order 
            isBigEndianByteOrder = true;
        } else if (byteOrderIdentifier.equals("II")) {
            //little-endian byte order
            isBigEndianByteOrder = false;
        } else {
            return null;
        }

        int firstDirectoryOffset = getInt32(4 + TIFF_HEADER_OFFSET) + TIFF_HEADER_OFFSET;

        processDirectory(geo, firstDirectoryOffset);
        
        return geo;
    }    
    
    /**
     * Process one of the nested Tiff IFD directories.
     * <p/>
     * Header
     * 2 bytes: number of tags
     * <p/>
     * Then for each tag
     * 2 bytes: tag type
     * 2 bytes: format code
     * 4 bytes: component count
     */
    private void processDirectory(GeoTag geo, int directoryOffset) throws Exception {
        // First two bytes in the IFD are the number of tags in this directory
        int dirTagCount = getUnsignedInt16(directoryOffset);

        // Handle each tag in this directory
        for (int tagNumber = 0; tagNumber < dirTagCount; tagNumber++) {
             int tagOffset = calculateTagOffset(directoryOffset, tagNumber);

            int tagType        = getUnsignedInt16(tagOffset);
            int formatCode     = getUnsignedInt16(tagOffset + 2);            
            int componentCount = getInt32(tagOffset + 4);            
            int byteCount      = componentCount * BYTES_PER_FORMAT[formatCode];
            int tagValueOffset;
            
            if (byteCount > 4) {
                // If it's bigger than 4 bytes, the dir entry contains an offset.
                final int offsetVal = getInt32(tagOffset + 8);

                tagValueOffset = TIFF_HEADER_OFFSET + offsetVal;
            } else {
                // 4 bytes or less and value is in the dir entry itself
                tagValueOffset = tagOffset + 8;
            }

            switch (tagType) {
                case GPS_INFO_OFFSET: {
                    int subdirOffset = TIFF_HEADER_OFFSET + getInt32(tagValueOffset);
                    processDirectory(geo, subdirOffset);
                    continue;                
                } default: {
                    processGeoInfo(geo, tagType, tagValueOffset, componentCount);
                    break;
                }
            }
        }

        // at the end of each IFD is an optional link to the next IFD
        int finalTagOffset = calculateTagOffset(directoryOffset, dirTagCount);       
        int nextDirectoryOffset = getInt32(finalTagOffset);
        
        if (nextDirectoryOffset != 0) {
            nextDirectoryOffset += TIFF_HEADER_OFFSET;
        }
    }    
    
    private void processGeoInfo(GeoTag geo, int tagType, int tagValueOffset, int componentCount) throws Exception {                
        
        switch (tagType) {
            case GPS_ALTITUDE:                                        
                geo.setAltitude(getUnsignedInt32(tagValueOffset) / getUnsignedInt32(tagValueOffset + 4));
                break;
            case GPS_TIME:
                double[] times = new double[componentCount];

                for (int i = 0; i < componentCount; i++)
                    times[i] = getUnsignedInt32(tagValueOffset + (8 * i)) / getUnsignedInt32(tagValueOffset + 4 + (8 * i));                                                            

                geo.setTime(times[0], times[1], times[2]);
                break;
            case GPS_LONGITUDE_REF:
                geo.setLongitudeReference(getNullTerminatedString(tagValueOffset, componentCount));
                break;
            case GPS_LONGITUDE:                                        
                double[] longitudes = new double[componentCount];

                for (int i = 0; i < componentCount; i++)
                    longitudes[i] = getUnsignedInt32(tagValueOffset + (8 * i)) / getUnsignedInt32(tagValueOffset + 4 + (8 * i));

                Double lon = GeoTag.degreesToDecimal(longitudes[0], longitudes[1], longitudes[2]);
                geo.setLongitude(lon);
                break;
            case GPS_LATITUDE_REF:
                geo.setLatitudeReference(getNullTerminatedString(tagValueOffset, componentCount));
                break;                    
            case GPS_LATITUDE:                                        
                double[] latitudes = new double[componentCount];

                for (int i = 0; i < componentCount; i++)
                    latitudes[i] = getUnsignedInt32(tagValueOffset + (8 * i)) / getUnsignedInt32(tagValueOffset + 4 + (8 * i));

                Double lat = GeoTag.degreesToDecimal(latitudes[0],  latitudes[1],  latitudes[2]);
                geo.setLatitude(lat);
                break;
            case GPS_DATE:
                String date = getNullTerminatedString(tagValueOffset, componentCount);
                geo.setDate(date);
                break;                           
        }

    }    
    
    public GeoTag readMetadata(File file) throws Exception {       
        InputStream inputStream = new FileInputStream(file);
        return readMetadata(inputStream);
    }

	public GeoTag readMetadata(InputStream inputStream) throws Exception {
		GeoTag         geoLocation = new GeoTag();
		BufferedInputStream bis         = new BufferedInputStream(inputStream);
        
        //init
        segmentDataList      = new ArrayList<byte[]>();
        isBigEndianByteOrder = true;        
        
        readSegments(bis);
        bis.close();        
        
        // Loop through all segments, looking for the EXIF segment.
        for (byte[] segment : segmentDataList) {
            if ("EXIF".equalsIgnoreCase(new String(segment, 0, 4))) {
                this.byteBuffer = segment;
                geoLocation = extract();                            
            }
        }

        return geoLocation;
	}
	
	public static class NotJpegException extends IOException
	{
		public NotJpegException()
		{
			super("This file is not a jpeg file.");
		}
	}
    
	private void readSegments(BufferedInputStream inputStream) throws IOException
	{
		try	{
			boolean hasValidHeader;
//			int offset = 0;
			byte[] headerBytes = new byte[2];
			if (inputStream.read(headerBytes, 0, 2) != 2) {
				hasValidHeader = false;
			} else {
				hasValidHeader = (headerBytes[0] & 0xFF) == 0xFF
						&& (headerBytes[1] & 0xFF) == 0xD8;
			}
			if (!hasValidHeader)
				throw new NotJpegException();
//			offset += 2;
			do {
				/*byte segmentIdentifier = (byte) (*/inputStream.read() /*& 0xFF)*/;
//				offset++;
				// next byte is <segment-marker>
				byte thisSegmentMarker = (byte) (inputStream.read() & 0xFF);
//				offset++;
				// next 2-bytes are <segment-size>: [high-byte] [low-byte]
				byte[] segmentLengthBytes = new byte[2];
				if (inputStream.read(segmentLengthBytes, 0, 2) != 2)
					throw new IOException("Malformed Jpeg data.");
//				offset += 2;
				int segmentLength = ((segmentLengthBytes[0] << 8) & 0xFF00)
						| (segmentLengthBytes[1] & 0xFF);
				// segment length includes size bytes, so subtract two
				segmentLength -= 2;
				byte[] segmentBytes = new byte[segmentLength];
				if (inputStream.read(segmentBytes, 0, segmentLength) != segmentLength)
					throw new IOException("Jpeg data ended unexpectedly.");
//				offset += segmentLength;
				if (thisSegmentMarker == JpegGeoTagReader.SEGMENTS_END) 	{
					break;
				} /*else*/ {
					segmentDataList.add(segmentBytes);
				}
			} while (true);
			inputStream.close();
		} catch (NotJpegException e) {
			throw e;
		} catch (IOException ioe) {
			throw new IOException("Exception processing Jpeg file: " + ioe.getMessage(), ioe);
		}
	}
    
    private int getUnsignedInt16(int index) throws Exception {
        if (isBigEndianByteOrder) {
            // Motorola - MSB first
            return (byteBuffer[index    ] << 8 & 0xFF00) |
                   (byteBuffer[index + 1]      & 0xFF);
        } /*else*/ {
            // Intel ordering - LSB first
            return (byteBuffer[index + 1] << 8 & 0xFF00) |
                   (byteBuffer[index    ]      & 0xFF);
        }
    }

    private long getUnsignedInt32(int index) throws Exception {
        if (isBigEndianByteOrder) {
            // Motorola - MSB first (big endian)
            return  (((long) byteBuffer[index    ]) << 24 & 0xFF000000L) |
                    (((long) byteBuffer[index + 1]) << 16 & 0xFF0000L) |
                    (((long) byteBuffer[index + 2]) << 8  & 0xFF00L) |
                    ((/*(long)*/ byteBuffer[index + 3])       & 0xFFL);
        } /*else*/ {
            // Intel ordering - LSB first (little endian)
            return  (((long) byteBuffer[index + 3]) << 24 & 0xFF000000L) |
                    (((long) byteBuffer[index + 2]) << 16 & 0xFF0000L) |
                    (((long) byteBuffer[index + 1]) << 8  & 0xFF00L) |
                    ((/*(long)*/ byteBuffer[index    ])       & 0xFFL);
        }
    }

    private int getInt32(int index) throws Exception {
        if (isBigEndianByteOrder) {
            // Motorola - MSB first (big endian)
            return (byteBuffer[index    ] << 24 & 0xFF000000) |
                   (byteBuffer[index + 1] << 16 & 0xFF0000) |
                   (byteBuffer[index + 2] << 8  & 0xFF00) |
                   (byteBuffer[index + 3]       & 0xFF);
        } /*else*/ {
            // Intel ordering - LSB first (little endian)
            return (byteBuffer[index + 3] << 24 & 0xFF000000) |
                   (byteBuffer[index + 2] << 16 & 0xFF0000) |
                   (byteBuffer[index + 1] << 8  & 0xFF00) |
                   (byteBuffer[index    ]       & 0xFF);
        }
    }
    
    private String getString(int index, int length) throws Exception {
        byte[] bytes = new byte[length];
        System.arraycopy(byteBuffer, index, bytes, 0, length);
        
        return new String(bytes);
    }

    private String getNullTerminatedString(int index, int maxLengthBytes) throws Exception {
        // Check for null terminators
        int length = 0;
        
        while ((index + length) < byteBuffer.length && 
                byteBuffer[index + length] != '\0' && 
                length < maxLengthBytes)
            
            length++;

        byte[] bytes = new byte[length];
        System.arraycopy(byteBuffer, index, bytes, 0, length);        
        
        return new String(bytes);
    }    
}
