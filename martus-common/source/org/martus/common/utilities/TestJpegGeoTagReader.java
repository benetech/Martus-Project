/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2015, Beneficent
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
package org.martus.common.utilities;

import java.io.InputStream;

import org.junit.Test;
import org.martus.util.TestCaseEnhanced;

public class TestJpegGeoTagReader extends TestCaseEnhanced
{
	public TestJpegGeoTagReader(String name)
	{
		super(name);
	}

	@Test
	public void testWithGeoTag() throws Exception
	{
		InputStream rawIn = getClass().getResourceAsStream("SampleWithGeoTag.jpg");
		try
		{
			JpegGeoTagReader reader = new JpegGeoTagReader();
			GeoTag geoTag = reader.readMetadata(rawIn);
			assertTrue(geoTag.hasData());
			assertEquals(45.50888888888889, geoTag.getLatitude(), 0.00001);
			assertEquals(-122.67361111111111, geoTag.getLongitude(), 0.00001);
			assertEquals(26.0, geoTag.getAltitude(), 0.1);
		}
		finally
		{
			rawIn.close();
		}
	}

	@Test
	public void testWithoutGeoTag() throws Exception
	{
		InputStream rawIn = getClass().getResourceAsStream("SampleWithoutGeoTag.jpg");
		try
		{
			JpegGeoTagReader reader = new JpegGeoTagReader();
			GeoTag geoTag = reader.readMetadata(rawIn);
			assertFalse(geoTag.hasData());
		}
		finally
		{
			rawIn.close();
		}
	}
}
