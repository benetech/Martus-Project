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
package org.martus.mspa.roothelper;

import java.io.File;
import java.net.InetAddress;
import java.util.TimerTask;

import org.martus.common.LoggerInterface;
import org.martus.common.LoggerToConsole;
import org.martus.common.MartusUtilities;
import org.martus.common.Version;
import org.martus.common.network.MartusXmlRpcServer;


public class RootHelper
{
	public static void main(String[] args) 
	{	
		try
		{
			System.out.println("MSPA RootHelper");
			new RootHelper(args);
		}
		catch(Exception e)
		{
			System.out.println("RootHelper: " + e);
			e.printStackTrace();
			System.exit(3);
		}
		
	}	
	
	public RootHelper(String[] args) throws Exception
	{			
		logger = new LoggerToConsole();	
		processCommandLine(args);		

		startBackgroundTimers();	
		InetAddress address = InetAddress.getByName(hostToBind);
		try 
		{		
			System.out.println("Creating root helper: " + hostToBind + ":" + portToUse);
			MartusXmlRpcServer.createNonSSLXmlRpcServer(new RootHelperHandler(logger), RootHelperHandler.class, RootHelperHandler.RootHelperObjectName, portToUse, address);
			System.out.println("Waiting for connections...");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(4);
		}
	}

	public static File getAuthorizedClientsFile()
	{
		return new File(getRootHelperDirectory().getPath(), AUTHORIZED_CLIENTS_FILE);
	}	
	
	public static File getRootHelperDirectory()
	{
		String appDirectory = null;
		if(Version.isRunningUnderWindows())
			appDirectory = WINDOW_ENVIRONMENT;
		else
			appDirectory = UNIX_ENVIRONMENT;
		return new File(appDirectory);
	}	
	
	private void setPortToUse(int port)
	{
		portToUse = port;
	}
	
	public synchronized void log(String message)
	{
		logger.logNotice(message);
	}

	
	public File getTriggerDirectory()
	{
		return new File(getRootHelperDirectory(), ADMINTRIGGERDIRECTORY);
	}
	
	public File getShutdownFile()
	{
		return new File(getTriggerDirectory(), SHUTDOWN_FILENAME);
	}
	
	public boolean isShutdownRequested()
	{
		boolean exitFile = getShutdownFile().exists();
		if(exitFile && !loggedShutdownRequested)
		{
			loggedShutdownRequested = true;
			log("Exit file found, attempting to shutdown.");
		}
		return(exitFile);
	}
	
	protected void startBackgroundTimers()
	{
		MartusUtilities.startTimer(new ShutdownRequestMonitor(), shutdownRequestIntervalMillis);
	}		
	
	private void processCommandLine(String[] args) 
	{			
		String portToListenTag = "--port=";	
		
		for(int arg = 0; arg < args.length; ++arg)
		{					
			String argument = args[arg];				
								
			if(argument.startsWith(portToListenTag))
			{	
				String portToListen = argument.substring(portToListenTag.length());
				setPortToUse(Integer.parseInt(portToListen));	
			}			
		}
	}
		
	class ShutdownRequestMonitor extends TimerTask
	{
		public void run()
		{
			if( isShutdownRequested())
			{
				log("Shutdown request acknowledged, preparing to shutdown.");										
				getShutdownFile().delete();
				log("RootHelper has exited.");
				try
				{
					System.exit(0);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private int portToUse = DEFAULT_PORT;	
	private String hostToBind=DEFAULT_HOSTNAME_TO_BIND;
	protected LoggerInterface logger;
	private boolean loggedShutdownRequested;
	
	private final static int DEFAULT_PORT = 983;
	private final static String DEFAULT_HOSTNAME_TO_BIND = "127.0.0.1";
	private final static String UNIX_ENVIRONMENT = "/var/RootHelper/";
	private final static String WINDOW_ENVIRONMENT = "C:/RootHelper/";
	private final static String AUTHORIZED_CLIENTS_FILE = "authorizedClient.txt";
	private static final String SHUTDOWN_FILENAME = "exit";
	private static final String ADMINTRIGGERDIRECTORY = "adminTriggers";
	private static final long shutdownRequestIntervalMillis = 1000;
}
