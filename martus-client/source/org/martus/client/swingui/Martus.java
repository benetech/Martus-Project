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

*/

package org.martus.client.swingui;

import java.awt.Toolkit;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.martus.client.swingui.jfx.FxMartus;
import org.martus.clientside.ClientPortOverride;
import org.martus.common.MartusConstants;
import org.martus.common.MartusLogger;
import org.martus.common.VersionBuildDate;
import org.martus.swing.UiOptionPane;
import org.martus.swing.Utilities;
import org.miradi.main.RuntimeJarLoader;

public class Martus
{
    public static void main (String args[])
	{
		System.out.println(UiConstants.programName);
		System.out.println(UiConstants.versionLabel + " " + VersionBuildDate.getVersionBuildDate());
		System.out.println("Java version: " + System.getProperty("java.version"));
		System.out.println("Java vendor : " + System.getProperty("java.vendor"));
		System.out.println("Java runtime: " + System.getProperty("java.runtime.name"));

		final String javaVersion = System.getProperty("java.version");
		final String minimumJavaVersion = "1.8.0";
		if(javaVersion.compareTo(minimumJavaVersion) < 0)
		{
			final String errorMessage = "Requires Java version " + minimumJavaVersion + " or later!";
			System.out.println(errorMessage);
			Toolkit.getDefaultToolkit().beep();
			UiOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(2);
		}
		
		System.out.println(MartusLogger.getMemoryStatistics());
		try
		{
			addThirdPartyJarsToClasspath();
		} 
		catch (Exception e)
		{
			System.out.println("Error loading third-party jars");
			e.printStackTrace();
		}
		
		Vector options = new Vector(Arrays.asList(args));
		int foundTestAll = options.indexOf("--testall");
		if(foundTestAll < 0)
			foundTestAll = options.indexOf("-testall");
		if(foundTestAll >= 0)
		{
			org.martus.common.test.TestCommon.runTests();
			org.martus.client.test.TestAll.runTests();
			System.exit(0);
		}
		
		int foundFoldersUnsorted = options.indexOf("--folders-unsorted");
		if(foundFoldersUnsorted >= 0)
		{
			System.out.println(options.get(foundFoldersUnsorted));
			UiSession.defaultFoldersUnsorted = true;
			options.remove(foundFoldersUnsorted);
		}
		
		int foundAlphaTester = options.indexOf("--alpha-tester");
		if(foundAlphaTester >= 0)
		{
			System.out.println(options.get(foundAlphaTester));
			UiSession.isAlphaTester = true;
			options.remove(foundAlphaTester);
		}
		
		int foundSwing = options.indexOf("--swing");
		if(foundSwing >= 0)
		{
			System.out.println(options.get(foundSwing));
			UiSession.isSwing = true;
			options.remove(foundSwing);
		}
		
		int foundJavaFx = options.indexOf("--javafx");
		if(foundJavaFx >= 0)
		{
			System.out.println(options.get(foundJavaFx));
			UiSession.isSwing = false;
			options.remove(foundJavaFx);
		}
		
		int foundPureFx = options.indexOf("--purefx");
		if(foundPureFx >= 0)
		{
			System.out.println(options.get(foundPureFx));
			UiSession.isSwing = false;
			UiSession.isPureFx = true;
			options.remove(foundPureFx);
		}
		
		timeoutInXSeconds = DEFAULT_TIMEOUT_SECONDS;
		int foundTimeout = findOption(options, TIMEOUT_OPTION_TEXT);
		if(foundTimeout >= 0)
		{
			String fullOption = (String)options.get(foundTimeout);
			String requestedTimeoutMinutes = fullOption.substring(TIMEOUT_OPTION_TEXT.length());
			System.out.println("Requested timeout in minutes: " + requestedTimeoutMinutes);
			int timeoutMinutes = Integer.parseInt(requestedTimeoutMinutes);
			timeoutInXSeconds = 60 * timeoutMinutes;
			options.remove(foundTimeout);
		}
		
		File dataRootDirectory = MartusConstants.determineMartusDataRootDirectory();
		File timeoutDebug = new File(dataRootDirectory, "timeout.1min");
		if(timeoutDebug.exists())
		{
			timeoutInXSeconds = TESTING_TIMEOUT_60_SECONDS;
			System.out.println(timeoutDebug.toString() + " detected");
		}
		MartusLogger.log("Inactivity timeout set to " + timeoutInXSeconds + " seconds");
		
		int foundInsecurePorts = options.indexOf("--insecure-ports");
		if(foundInsecurePorts >= 0)
		{
			MartusLogger.log("WARNING: USING INSECURE PORTS (--insecure-ports)");
			ClientPortOverride.useInsecurePorts = true;
			options.remove(foundInsecurePorts);
		}
		
		if(options.size() > 0)
		{
			System.out.println("Incorrect command line parameter");
			System.out.println("The only valid options are:");
			System.out.println("--testall");
			System.out.println("--folders-unsorted");
			System.out.println("--timeout-minutes=<nn>");
			System.exit(1);
		}
		
		Martus.useSystemLookAndFeel();

		if(UiSession.isPureFx)
			FxMartus.main(args);
		else
			run();
    }

	public static void run()
	{
		try
		{
			if(Utilities.isMSWindows())
				UIManager.put("Application.useSystemFontSettings", new Boolean(false));

	        UiMainWindow window = constructMainWindow();

	        String osName = System.getProperty("os.name");
			String osVersion = System.getProperty("os.version");
			System.out.println(osName + ": " + osVersion);
			if(osName.startsWith("Windows"))
			{
				boolean isWin2KOrLater = osVersion.compareTo("5") >= 0;
				boolean isWinME = osVersion.compareTo("4.90") == 0;
				boolean isModernWindows = (isWin2KOrLater || isWinME); 
				if(!isModernWindows)
				{
					window.rawError("Martus requires Windows ME or later");
					System.exit(1);
				}
			}
			
	        if(!window.run())
	        {
	        	MartusLogger.log("Exiting after run()");
	        	window.exitWithoutSavingState();
	        }
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			System.exit(1);
		}
	}

	public static UiMainWindow constructMainWindow() throws Exception
	{
		if(UiSession.isPureFx)
			return new PureFxMainWindow();
		return new FxInSwingMainWindow();
	}

	public static void useSystemLookAndFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	private static int findOption(Vector options, String optionText)
	{
		for(int i = 0; i < options.size(); ++i)
		{
			String option = (String) options.get(i);
			if(option.startsWith(optionText))
			{
				return i;
			}
		}
		
		return -1;
	}

	public static void addThirdPartyJarsToClasspath() throws Exception
	{
		File martusJarDirectory = getMartusJarDirectory();
		if(martusJarDirectory == null)
		{
			System.out.println("Not adding thirdparty jars to classpath since not running from a jar");
			return;
		}
		
		String jarSubdirectoryName = "ThirdParty";
		System.out.println("Running Martus from " + martusJarDirectory);
		File thirdPartyDirectory = new File(martusJarDirectory, jarSubdirectoryName);
		if(thirdPartyDirectory.exists())
			RuntimeJarLoader.addJarsInSubdirectoryToClasspath(thirdPartyDirectory, getThirdPartyJarNames());
	}

	private static String[] getThirdPartyJarNames()
	{
		return new String[] {
			"bcprov-jdk15on-148.jar",
			"icu4j-3.4.4.jar",
			"jcommon-1.0.17.jar",
			"jfreechart-1.0.14.jar",
			"js-2006-03-08.jar",
			"junit-4.11.jar",
			"layouts-2006-08-10.jar",
			"orchid-1.0.0.jar",
			"persiancalendar-2.1.jar",
			"velocity-dep-1.4.jar",
			"commons-logging-1.1.jar",
			"ws-commons-util-1.0.2.jar",
			"xmlrpc-common-3.1.3.jar",
			"xmlrpc-client-3.1.3.jar",
		};
	}

	public static File getMartusJarDirectory() throws URISyntaxException
	{
		final URL url = Martus.class.getResource("Martus.class");
		String uriScheme = url.toURI().getSchemeSpecificPart();
		String jarPathString = stripPrefix(uriScheme);
		
		int bangAt = jarPathString.indexOf('!');
		boolean isInsideJar = bangAt >= 0;
		if(isInsideJar)
		{
			String jarURIString = jarPathString.substring(0, bangAt);
			File jarFile = new File(jarURIString);
			final File directory = jarFile.getParentFile();
			return directory;
		}

		return null;
	}

	private static String stripPrefix(String uri)
	{
		int startOfRealPath = uri.indexOf(':') + 1;
		return uri.substring(startOfRealPath);
	}

	private final static String TIMEOUT_OPTION_TEXT = "--timeout-minutes=";
	private final static int DEFAULT_TIMEOUT_SECONDS = (5 * 60);
	private static final int TESTING_TIMEOUT_60_SECONDS = 60;
	public static int timeoutInXSeconds;
}
