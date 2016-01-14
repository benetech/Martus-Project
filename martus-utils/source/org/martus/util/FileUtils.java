package org.martus.util;

public class FileUtils 
{
	public static String getFileExtensionIncludingPeriodIfPresent(String fileName)
	{
		String extension = getFileExtensionWithoutPeriodIfPresent(fileName);
		if(extension == null)
			return null;
		return "." + extension;
	}

	public static String getFileExtensionWithoutPeriodIfPresent(String fileName)
	{
		String[] fileWithExtension = fileName.split("\\.(?=[^\\.]+$)");
		if(fileWithExtension.length == 2)
			return fileWithExtension[1];
		return null;
	}

}
