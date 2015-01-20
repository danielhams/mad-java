/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.service.apprenderinggraph.vos;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.thread.AbstractInterruptableThread;
import uk.co.modularaudio.util.thread.ThreadUtils.MAThreadPriority;

public class AppRenderingErrorQueue extends AbstractInterruptableThread
{
	private static Log log = LogFactory.getLog( AppRenderingErrorQueue.class.getName() );
	
	public enum ErrorSeverity
	{
		WARNING,
		FATAL
	};
	
	public class AppRenderingErrorStruct
	{
		public AppRenderingIO sourceRenderingIO;
		public ErrorSeverity severity;
		public String msg;
		
		public AppRenderingErrorStruct( AppRenderingIO sourceRenderingIO, ErrorSeverity severity, String msg )
		{
			this.sourceRenderingIO = sourceRenderingIO;
			this.severity = severity;
			this.msg = msg;
		}
		
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append( severity.toString() );
			sb.append( " " );
			sb.append( sourceRenderingIO.getClass().getSimpleName() );
			sb.append( " " );
			sb.append( msg );
			
			return sb.toString();
		}
	};
	
	private final BlockingDeque<AppRenderingErrorStruct> errorQueue = new LinkedBlockingDeque<AppRenderingErrorQueue.AppRenderingErrorStruct>();
	
	private Map<AppRenderingIO, AppRenderingErrorCallback> appRenderingToCallbackMap = new HashMap<AppRenderingIO, AppRenderingErrorCallback>();
	
	public AppRenderingErrorQueue()
	{
		super( MAThreadPriority.APPLICATION );
		this.start();
	}

	@Override
	protected void doJob() throws Exception
	{
		setName( AppRenderingErrorQueue.class.getSimpleName() );
		boolean localShouldHalt = shouldHalt;
		while( !localShouldHalt )
		{
			AppRenderingErrorStruct error = errorQueue.takeFirst();
			
			AppRenderingErrorCallback callbackForIo = appRenderingToCallbackMap.get( error.sourceRenderingIO );
			if( callbackForIo == null )
			{
				log.error("Missing map to callback: " + callbackForIo + ": " + error.msg );
			}
			else
			{
				callbackForIo.errorCallback( error );
			}
			
			localShouldHalt = shouldHalt;
		}
	}
	
	public void addCallbackForRenderingIO( AppRenderingIO sourceRenderingIO, AppRenderingErrorCallback callback )
	{
		appRenderingToCallbackMap.put( sourceRenderingIO,  callback );
		log.debug( "Adding error callback \"" + callback.getName() + "\" for " + sourceRenderingIO.getClass().getSimpleName() );
	}
	
	public void removeCallbackForRenderingIO( AppRenderingIO sourceRenderingIO )
	{
		AppRenderingErrorCallback callback = appRenderingToCallbackMap.get( sourceRenderingIO );
		if( callback == null )
		{
			log.error( "Failed to find callback to remove for renderingIO: " + sourceRenderingIO.getClass().getSimpleName() );
		}
		else
		{
			log.debug( "Removing error callback for \"" + callback.getName() + "\" for " + sourceRenderingIO.getClass().getSimpleName() );
			appRenderingToCallbackMap.remove( sourceRenderingIO );
		}
	}
	
	public void queueError( AppRenderingIO sourceRenderingIO, ErrorSeverity severity, String msg )
	{
		AppRenderingErrorStruct es = new AppRenderingErrorStruct( sourceRenderingIO, severity, msg );
		errorQueue.add( es );
	}
	
	public void shutdown()
	{
		try
		{
			this.halt();
			this.forceHalt();
			this.join();
			
			if( appRenderingToCallbackMap.size() > 0 )
			{
				log.warn("AppRenderingErrorQueue shutdown - but some error callbacks still mapped:");
				log.warn( errorQueue.toString() );
			}
		}
		catch( Exception e )
		{
			log.error("Exception caught shutting down error queue: " + e.toString(), e );
		}
	}

}
