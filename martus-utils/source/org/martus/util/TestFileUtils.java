package org.martus.util;

public class TestFileUtils extends TestCaseEnhanced
{

	public TestFileUtils(String name) 
	{
		super(name);
	}

	public void testGetFileExtensionIncludingPeriodIfPresent()
	{
		final String jpegExtension = ".jpg";
		final String htmlExtension = ".html";
		
		String fileName = "Test" + jpegExtension;
		assertEquals(jpegExtension, FileUtils.getFileExtensionIncludingPeriodIfPresent(fileName));
		
		String fileName2 = "Test2" + htmlExtension;
		assertEquals(htmlExtension, FileUtils.getFileExtensionIncludingPeriodIfPresent(fileName2));
		
		String fileNameWithoutExtension = "NoExtension";
		assertNull(FileUtils.getFileExtensionIncludingPeriodIfPresent(fileNameWithoutExtension));
		
		String jpegFileNameWithMultipleDots = "This.Is.A.Test" + jpegExtension;
		assertEquals(jpegExtension, FileUtils.getFileExtensionIncludingPeriodIfPresent(jpegFileNameWithMultipleDots));
	}

	public void testGetFileExtensionWithoutPeriodIfPresent()
	{
		final String jpegExtension = "jpg";
		final String htmlExtension = "html";
		
		String fileName = "Test." + jpegExtension;
		assertEquals(jpegExtension, FileUtils.getFileExtensionWithoutPeriodIfPresent(fileName));
		
		String fileName2 = "Test2." + htmlExtension;
		assertEquals(htmlExtension, FileUtils.getFileExtensionWithoutPeriodIfPresent(fileName2));
		
		String fileNameWithoutExtension = "NoExtension";
		assertNull(FileUtils.getFileExtensionWithoutPeriodIfPresent(fileNameWithoutExtension));
		
		String jpegFileNameWithMultipleDots = "This.Is.A.Test." + jpegExtension;
		assertEquals(jpegExtension, FileUtils.getFileExtensionWithoutPeriodIfPresent(jpegFileNameWithMultipleDots));
	}
	
	
}
