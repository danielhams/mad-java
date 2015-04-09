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

package uk.co.modularaudio.service.jobexecutor.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.jobexecutor.JobExecutorService;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;

public class JobExecutorServiceImpl implements JobExecutorService, ComponentWithLifecycle
{
	private static Log log = LogFactory.getLog( JobExecutorServiceImpl.class.getName() );

	private final static String CONFIG_KEY_POOL_SIZE = JobExecutorServiceImpl.class.getSimpleName() + ".PoolSize";
	private final static String CONFIG_KEY_MAXIMUM_POOL_SIZE = JobExecutorServiceImpl.class.getSimpleName() + ".MaximumPoolSize";
	private final static String CONFIG_KEY_JOB_QUEUE_SIZE = JobExecutorServiceImpl.class.getSimpleName() + ".JobQueueSize";
	private final static String CONFIG_KEY_KEEP_ALIVE_TIME = JobExecutorServiceImpl.class.getSimpleName() + ".KeepAliveTime";

	private ConfigurationService configurationService;

	private int poolSize;
	private int maximumPoolSize;
	private long keepAliveTime;
	private int jobQueueSize;
	private ThreadPoolExecutor tpe;

	public JobExecutorServiceImpl()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( configurationService == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check configuration" );
		}

		final Map<String,String> errors = new HashMap<String,String>();

		poolSize = ConfigurationServiceHelper.checkForIntKey( configurationService, CONFIG_KEY_POOL_SIZE, errors );
		maximumPoolSize = ConfigurationServiceHelper.checkForIntKey( configurationService, CONFIG_KEY_MAXIMUM_POOL_SIZE, errors );
		jobQueueSize = ConfigurationServiceHelper.checkForIntKey( configurationService, CONFIG_KEY_JOB_QUEUE_SIZE, errors );
		keepAliveTime = ConfigurationServiceHelper.checkForLongKey( configurationService, CONFIG_KEY_KEEP_ALIVE_TIME, errors );

		ConfigurationServiceHelper.errorCheck( errors );

		tpe = new ThreadPoolExecutor( poolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>( jobQueueSize ));

		final boolean firstThreadStarted = tpe.prestartCoreThread();

		if( !firstThreadStarted )
		{
			tpe.shutdown();

			final String msg = "Failed to prestart initial thread";
			log.error( msg );
			throw new ComponentConfigurationException( msg );
		}
	}

	@Override
	public void destroy()
	{
		tpe.shutdown();
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public void dumpJobs()
	{
		final String currentState = tpe.toString();
		if( log.isDebugEnabled() )
		{
			log.debug( "The thread pool executor: " + currentState );
		}
	}

	@Override
	public void submitJob( final Runnable r )
	{
		tpe.execute( r );
	}

}
