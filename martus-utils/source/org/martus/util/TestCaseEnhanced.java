/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

To the extent this copyrighted software code is used in the 
Miradi project, it is subject to a royalty-free license to 
members of the Conservation Measures Partnership when 
used with the Miradi software as specified in the agreement 
between Benetech and WCS dated 5/1/05.
*/

package org.martus.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;


public class TestCaseEnhanced extends TestCase
{
	public TestCaseEnhanced(String name)
	{
		super(name);
	}

	public File createTempFile() throws IOException
	{
		final String tempFileName = "MartusTest-" + getName();
		return createTempFileFromName(tempFileName);
	}

	public File stringToFile(String fileName, String extension, String data) throws IOException 
	{
		File temp = createTempFileFromName(fileName, extension);
		InputStream in = new StringInputStreamWithSeek(data);
		OutputStream out = new FileOutputStream(temp);
		try 
		{
			new StreamCopier().copyStream(in, out);
		} 
		finally 
		{
			out.flush();
			out.close();
		}
		return temp;
	}

	public File createTempFileFromName(String name) throws IOException
	{
		return createTempFileFromName(name, null);
	}

	public File createTempFileFromName(String name, String extension) throws IOException
	{
		File file = File.createTempFile(name, extension);
		file.deleteOnExit();
		return file;
	}

	public File createTempFileWithData(String contents) throws IOException
	{
		File file = createTempFile();
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.writeln(contents);
		writer.flush();
		writer.close();
		return file;
	}

	protected File createTempFileWithData(byte[] data) throws Exception
	{
		File tempFile = createTempFile();
		FileOutputStream out = new FileOutputStream(tempFile);
		out.write(data);
		out.close();
		return tempFile;
	}
	
	public File createTempDirectory() throws IOException
	{
		File dir = createTempFile();
		dir.delete();
		dir.mkdirs();
		return dir;
	}

	public InputStream getStreamFromResource(String resourceFile) throws FileNotFoundException, IOException
	{
		InputStream in = getClass().getResource(resourceFile).openStream();
		assertNotNull(in);
		return in;
	}

	public void copyResourceFileToLocalFile(File outputFile, String resourceFile) throws FileNotFoundException, IOException
	{
		FileOutputStream out = new FileOutputStream(outputFile);
		InputStream in = getStreamFromResource(resourceFile);
		StreamCopier copier = new StreamCopier();
		copier.copyStream(in, out);
		in.close();
		out.close();
	}
	
	public static void assertFalse(boolean actual)
	{
		if(actual)
			throw new AssertionFailedError();
	}

	public static void assertFalse(String message, boolean actual)
	{
		if(actual)
			throw new AssertionFailedError(message + " expected false ");
	}

	public static void assertNotEquals(long expected, long actual)
	{
		if(actual == expected)
			throw new AssertionFailedError("Expected anything other than " + expected);
	}

	public static void assertNotEquals(String message, long expected, long actual)
	{
		if(actual == expected)
			throw new AssertionFailedError(message + "Expected anything other than " + expected);
	}

	public static void assertNotEquals(String expected, String actual)
	{
		if(expected.equals(actual))
			throw new AssertionFailedError("Expected anything other than " + expected);
	}

	public static void assertNotEquals(String message, Object expected, Object actual)
	{
		if(expected.equals(actual))
			throw new AssertionFailedError(message + ": Expected anything other than " + expected);
	}
	
	public static void assertContains(int expected, int[] array)
	{
		Integer[] integers = new Integer[array.length];
		for (int i=0; i<array.length; ++i)
		{
			integers[i]=new Integer(array[i]);
		}
		
		assertContains(new Integer(expected), integers);
	}
	
	
	public static void assertContains(String message, int expected, int[] array)
	{
		Integer[] integers = new Integer[array.length];
		for (int i=0; i<array.length; ++i)
		{
			integers[i]=new Integer(array[i]);
		}
		
		assertContains(message, new Integer(expected), integers);
	}
	
	
	public static void assertContains(Object expected, Object[] array)
	{
		assertNotNull(expected);
		HashSet hashSet = (new HashSet(Arrays.asList(array)));
		if (! hashSet.contains(expected))
			throw new AssertionFailedError("<" + expected + "> not found in " + "<" + hashSet + ">");
	}
	
	public static void assertContains(String message, Object expected, Object[] array)
	{
		assertNotNull(expected);
		HashSet hashSet = (new HashSet(Arrays.asList(array)));
		if (! hashSet.contains(expected))
			throw new AssertionFailedError(message + ": " + "<" + expected + ">" +
					" not found in " + "<" + hashSet + ">");
	}

	public static void assertContains(String expected, String container)
	{
		assertNotNull(expected);
		if (container.indexOf(expected) == -1)
			throw new AssertionFailedError("<" + expected + ">" + " not found in " + "<" + container + ">");
	}

	public static void assertContains(String message, String expected, String container)
	{
		assertNotNull(expected);
		if (container.indexOf(expected) == -1)
			throw new AssertionFailedError(message + ": " + "<" + expected + ">" +
											" not found in " + "<" + container + ">");
	}

	public static void assertNotContains(String unexpected, String container)
	{
		assertNotNull(unexpected);
		if (container.indexOf(unexpected) != -1)
			throw new AssertionFailedError("<" + unexpected + ">" + " WAS found in " + "<" + container + ">");
	}

	public static void assertNotContains(String message, String unexpected, String container)
	{
		assertNotNull(unexpected);
		if (container.indexOf(unexpected) != -1)
			throw new AssertionFailedError(message + ": " + "<" + unexpected + ">" +
											" WAS found in " + "<" + container + ">");
	}

	public static void assertContains(Object expected, Collection container)
	{
		assertNotNull(expected);
		if (!container.contains(expected))
			throw new AssertionFailedError("<" + expected + ">" + " not found in " + "<" + container + ">");
	}

	public static void assertContains(String message, Object unexpected, Collection container)
	{
		assertNotNull(unexpected);
		if (!container.contains(unexpected))
			throw new AssertionFailedError(message + ": " + "<" + unexpected + ">" +
											" not found in " + "<" + container + ">");
	}

	public static void assertNotContains(Object unexpected, Collection container)
	{
		assertNotNull(unexpected);
		if (container.contains(unexpected))
			throw new AssertionFailedError("<" + unexpected + ">" + " WAS found in " + "<" + container + ">");
	}

	public static void assertNotContains(String message, Object unexpected, Collection container)
	{
		assertNotNull(unexpected);
		if (container.contains(unexpected))
			throw new AssertionFailedError(message + ": " + "<" + unexpected + ">" +
											" WAS found in " + "<" + container + ">");
	}

	public static void assertStartsWith(String expected, String container)
	{
		assertNotNull(expected);
		if (!container.startsWith(expected))
			throw new AssertionFailedError("<" + expected + ">" + " not at start of " + "<" + container + ">");
	}

	public static void assertStartsWith(String label, String expected, String container)
	{
		assertNotNull(expected);
		if (!container.startsWith(expected))
			throw new AssertionFailedError(label + ": <" + expected + ">" + " not at start of " + "<" + container + ">");
	}

	public static void assertEndsWith(String expected, String container)
	{
		assertNotNull(expected);
		if (!container.endsWith(expected))
			throw new AssertionFailedError("<" + expected + ">" + " not at end of " + "<" + container + ">");
	}

	public static void assertEndsWith(String label, String expected, String container)
	{
		assertNotNull(expected);
		if (!container.endsWith(expected))
			throw new AssertionFailedError(label + ": <" + expected + ">" + " not at end of " + "<" + container + ">");
	}

	abstract public static class TestingThread extends Thread
	{
		public Throwable getResult()
		{
			return result;
		}

		public Throwable result;
	}

	abstract public static interface ThreadFactory
	{
		abstract public TestingThread createThread(int copies) throws Exception;
		abstract public void tearDown() throws Exception;
		abstract public int getThreadCount();
		abstract public int getIterations();

	}
	
	public void doThreadTests(ThreadFactory factory) throws Exception
	{
		try
		{
			launchTestThreads(factory);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw(new Exception(e));
		}
		finally
		{
			factory.tearDown();
		}
	}
	
	public static void launchTestThreads(ThreadFactory factory) throws Throwable
	{
		int threadCount = factory.getThreadCount() * threadTestLoadScaleFactor;
		int iterations = factory.getIterations() * threadTestLoadScaleFactor;
		TestingThread[] threads = new TestingThread[threadCount];
		for (int i = 0; i < threads.length; i++) 
		{
			threads[i] = factory.createThread(iterations);
		}
		
		for (int i = 0; i < threads.length; i++) 
		{
			threads[i].start();
		}
		
		for (int i = 0; i < threads.length; i++) 
		{
			threads[i].join();
			if(threads[i].getResult() != null)
				throw threads[i].getResult();
		}
	}
	
	public void TRACE_BEGIN(String method)
	{
		if(VERBOSE)
		{
			System.out.print(getClass() + "." + method + ": ");
			methodStartedAt = System.currentTimeMillis();
		}
	}

	public void TRACE_END()
	{
		if(VERBOSE)
			System.out.println(System.currentTimeMillis() - methodStartedAt);
	}

	protected void setUp() throws Exception
	{
		if(SHOW_MODULE_TEST_TIME && testStartTime == 0)
		{	
			testStartTime = System.currentTimeMillis();
			String classNameComplete = getClass().toString();
			String className = classNameComplete.substring(classNameComplete.lastIndexOf('.')+1);
			System.out.print(className + " " + getName() + " " );
		}
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
		if(SHOW_MODULE_TEST_TIME)
		{
			long testExecutionTimeMillis = System.currentTimeMillis() - testStartTime;
			System.out.println(testExecutionTimeMillis / 1000 + " seconds.");
		}
	}

	public final static String BAD_FILENAME = "<>//\\..??**::||";

	public static int threadTestLoadScaleFactor = 1;
	
	private long methodStartedAt;
	public boolean VERBOSE = false;
	public boolean SHOW_MODULE_TEST_TIME = false;
	public long testStartTime = 0; 
}
