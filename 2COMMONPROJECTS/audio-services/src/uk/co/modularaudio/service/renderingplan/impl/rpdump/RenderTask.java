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

package uk.co.modularaudio.service.renderingplan.impl.rpdump;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.renderingplan.RenderingJob;
import uk.co.modularaudio.service.renderingplan.impl.MadParallelRenderingJob;
import uk.co.modularaudio.service.renderingplan.impl.MadRenderingJob;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;

public class RenderTask implements Runnable
{
	private static Log log = LogFactory.getLog( RenderTask.class.getName() );

	private final AddNewTaskInterface addNewTaskInterface;

	private final RenderingJob parallelRenderingJob;

	private final int maxJobs;
	private final Runnable[] jobsToLaunch;

	public RenderTask( final AddNewTaskInterface addNewTaskInterface,
			final RenderingJob job,
			final int maxJobs )
	{
		this.addNewTaskInterface = addNewTaskInterface;
		this.parallelRenderingJob = job;
		this.maxJobs = maxJobs;
		this.jobsToLaunch = new Runnable[ maxJobs ];
	}


	@Override
	public void run()
	{
		final String renderJobName = parallelRenderingJob.toString();
		if( parallelRenderingJob instanceof MadParallelRenderingJob )
		{
			final MadParallelRenderingJob prj = (MadParallelRenderingJob)parallelRenderingJob;
			final MadRenderingJob auJob = prj.getRenderingJob();
			final MadInstance<?,?> renderJobInstance = auJob.getMadInstance();
			try
			{
				final MadChannelInstance[] channelInstances = renderJobInstance.getChannelInstances();
				final int numChannels = channelInstances.length;
				final MadChannelConnectedFlags channelActiveBitset = auJob.getChannelConnectedFlags();
				final StringBuilder bsBuffer = new StringBuilder();
				for( int i = 0 ; i < numChannels ; i++ )
				{
					if( channelActiveBitset.get( i ) )
					{
						final MadChannelInstance curChannel = channelInstances[ i ];
						bsBuffer.append("channel \"");
						bsBuffer.append( curChannel.definition.name );
						bsBuffer.append( "\" (" );
						bsBuffer.append( curChannel.definition.type );
						bsBuffer.append( ", " );
						bsBuffer.append("ACTIVE) ");
					}
					else
					{
	//					bsBuffer.append("inactive) ");
					}
				}
				final int cardinality = prj.getCardinality();
				if( log.isDebugEnabled() )
				{
					log.debug("MU Parallel rendering job cardinality " + cardinality + " : \"" + renderJobName + "\" Channels: " + bsBuffer.toString() );
				}
	//			renderJob.go();
			}
			catch (final Exception e1)
			{
				final String msg = "Exception caught rendering job " + renderJobInstance.getInstanceName() + ": " + e1.toString();
				log.error( msg, e1 );
				return;
			}
		}
		else
		{
			if( log.isDebugEnabled() )
			{
				log.debug( "Runtime parallel rendering task: \"" + renderJobName + "\"");
			}
		}

		// Now it's complete we atomically decrement each of the sink jobs. If any of them go to zero they can be added
		// as new jobs in the queue.
		final RenderingJob sinksToUpdate[] = parallelRenderingJob.getConsJobsThatWaitForUs();
		int curJobNum = 0;

		for( final RenderingJob dJob : sinksToUpdate )
		{
			final boolean readyToGo = dJob.markOneProducerAsCompleteCheckIfReadyToGo();
			if( readyToGo )
			{
//				log.debug("Launching new subtask " + dJob.getRenderingJob().getComponentInstance().getName());
				final RenderTask recurseRenderTask = new RenderTask( addNewTaskInterface, dJob, this.maxJobs );
				jobsToLaunch[curJobNum++] = recurseRenderTask;
			}
		}

		// Now reset the current jobs counters so the next rendering pass will work correctly
		parallelRenderingJob.forDumpResetNumProducersStillToComplete();

//		log.debug("Job " + job.getRenderingJob().getComponentInstance().getName() + " completed.");
		if( curJobNum > 0)
		{
			addNewTaskInterface.addNewTasks( jobsToLaunch, curJobNum );
		}
	}
}
