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

package test.uk.co.modularaudio.service.jobexecutor;

import java.util.concurrent.CyclicBarrier;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.configuration.impl.ConfigurationServiceImpl;
import uk.co.modularaudio.service.jobexecutor.impl.JobExecutorServiceImpl;

public class TestJobExecutorService extends TestCase
{
	private static Log log = LogFactory.getLog( TestJobExecutorService.class.getName() );

	private final CyclicBarrier cb = new CyclicBarrier( 2 );
	private final JobExecutedWaitBarrier wb = new JobExecutedWaitBarrier( cb );

	public TestJobExecutorService()
	{
	}

	public void testLaunchOneJobAndGetResult() throws Exception
	{
		log.trace("Attempting to instantiate service");

		final ConfigurationServiceImpl csi = new ConfigurationServiceImpl();
		csi.setConfigResourcePath( "/jes.properties" );

		final JobExecutorServiceImpl jesi = new JobExecutorServiceImpl();
		jesi.setConfigurationService( csi );

		csi.init();
		jesi.init();

		log.trace("Dump jobs before we begin:");
		jesi.dumpJobs();

		log.trace("Adding a simple runnable with a callback");

		jesi.submitJob( wb );
		cb.await();

		log.trace("Dump jobs after tests, before we shutdown");

		jesi.dumpJobs();

		jesi.destroy();
		csi.destroy();

	}

	public static void receiveCallback()
	{
		log.trace("Received callback");
	}

}
