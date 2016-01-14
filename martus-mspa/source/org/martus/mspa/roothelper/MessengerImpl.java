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

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;

import org.martus.common.LoggerInterface;
import org.martus.common.LoggerToConsole;


public class MessengerImpl extends UnicastRemoteObject implements Messenger, MessageType 
{
	public MessengerImpl(final String _passphrase) throws RemoteException 
	{
		super();
//		passphrase = _passphrase;
		logger = new LoggerToConsole();	
	}
	
	public Status startServer(String accountKey) throws RemoteException
	{
		return callScript(SERVER_START);
	}
	
	public Status stopServer(String accountKey) throws RemoteException
	{	
		return callScript(SERVER_STOP);
	}
	
	public Status getStatus(String accountKey, int statusType) throws RemoteException
	{		
		return callScript(statusType);
	}
	
	public Status setReadOnly(String accountKey) throws RemoteException
	{	
		return callScript(READONLY);
	}
	
	public Status setReadWrite(String accountKey) throws RemoteException
	{			
		return callScript(READ_WRITE);
	}
	
	private Status callScript(int scriptType)
	{					
		return null;
//		switch (scriptType)
//		{
//			case SERVER_START:
//				return runExec("martus -p restart");
//			case SERVER_STOP:
//				return runExec("martus -p stop");
//			case READONLY:
//				return runExec("remountmartusdata ro");
//			case READ_WRITE:
//				return runExec("remountmartusdata rw");
//			case SERVER_STATE:
//				return runExec("martus -p serverstate");
//			default:
//			{
//				Status status = new Status(Status.FAILED);
//				status.setStdErrorMsg("Unkown script");
//				return status;
//			}		
//		}		
	}	
	
//	private Status runExec(String callScript)
//	{
//		return null;
//		Status status = new Status();	
//		try
//		{
//			logWhoCallThisScript(callScript);							
//			Process process = Runtime.getRuntime().exec(callScript);		
//	
//			StringBuffer errorStream = new StringBuffer();			
//			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(),errorStream);
//			
//			StringBuffer outputStream = new StringBuffer(); 
//			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), outputStream);
//			errorGobbler.start();
//			outputGobbler.start();
//			
//			if (callScript.endsWith("restart"))
//			{	
//				BufferedWriter buffStdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
//				buffStdin.write(passphrase+"\r");
//				buffStdin.flush(); 
//			}
//			
//			if (process.waitFor() != 0)
//			{															
//				status.setStdErrorMsg(errorStream.toString());			
//				status.setStatus(Status.FAILED);
//				log(errorStream.toString());
//			}
//			
//			process.getInputStream().close();
//			process.getOutputStream().close();
//			process.getErrorStream().close(); 
//							
//			status.setStdOutMsg(outputStream.toString());
//			log(outputStream.toString());
//		}
//		catch (IOException e)
//		{
//			status.setStdErrorMsg(e.toString());
//			status.setStatus(Status.FAILED);
//			log("["+callScript+"] "+status.getAllMessages());
//		}
//		catch (InterruptedException e)
//		{
//			e.printStackTrace();
//			log("["+callScript+"] "+e.toString());
//		}
//
//		return status;
//	}
	
	private void logWhoCallThisScript(String scriptType)
	{
		try
		{
			log( "["+scriptType+"] has been invoked by " + RemoteServer.getClientHost() );
		}
		catch (ServerNotActiveException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getInitMsg() throws RemoteException 
	{
		logWhoCallThisScript("Initialized Message");			
		return(CONNET_MSG);
	}		

	public Status getAdminFile(String key, String fileFrom, String fileTo) throws RemoteException 
	{
		return null;
//		Status status = new Status();		
//								
//		try
//		{
//			FileTransfer.copyFile(new File(fileFrom), new File(fileTo));			
//			status.setStatus(Status.SUCCESS);		
//		}
//		catch(FileNotFoundException nothingToWorryAbout)
//		{
//			status.setStatus(Status.FAILED);			
//			status.setStdErrorMsg(fileFrom+" not found: ");				
//		}
//		catch (IOException e)
//		{
//			status.setStatus(Status.FAILED);			
//			status.setStdOutMsg("Error loading ("+fileFrom+")file.\n"+e.toString());				
//			e.printStackTrace();			
//		}		
//				
//		return status;
	}
	
	private synchronized void log(String message)
	{
		logger.logNotice(message);
	}	
	
	class StreamGobbler extends Thread
	{
//		InputStream inputStream;
//		StringBuffer rtStatus;	
//
//		StreamGobbler(InputStream is,StringBuffer status)
//		{
//			inputStream = is;					
//			rtStatus = status;
//		}
//
//		public void run()
//		{
//			try
//			{
//				InputStreamReader isReader = new InputStreamReader(inputStream);
//				BufferedReader br = new BufferedReader(isReader);
//				String line=null;				
//				while ( (line = br.readLine()) != null)
//					rtStatus.append(line).append("\n");    
//			} 
//			catch (IOException ioe)
//			{
//				ioe.printStackTrace();  
//			}
//		}
	}
	
	private LoggerInterface logger;
//	private String passphrase;	
	public static final String CONNET_MSG = "[MessengerImpl] Connected: Ready to invoke ...\n";
}
