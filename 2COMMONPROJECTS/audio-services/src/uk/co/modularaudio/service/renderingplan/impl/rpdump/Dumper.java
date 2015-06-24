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

import java.util.ArrayList;

import uk.co.modularaudio.service.renderingplan.RenderingJob;
import uk.co.modularaudio.service.renderingplan.RenderingPlan;
import uk.co.modularaudio.util.exception.DatastoreException;

public class Dumper
{
//	private static Log log = LogFactory.getLog( Dumper.class.getName() );

	public Dumper( final RenderingPlan renderingPlan )
		throws DatastoreException
	{
		final RenderingJob initialJobs[] = renderingPlan.getInitialJobs();
		final ArrayList<RenderingJob> initialJobsArray = new ArrayList<RenderingJob>();
		for( final RenderingJob job : initialJobs )
		{
			initialJobsArray.add( job );
		}

		final DumpJobQueue jobQueue = new DumpJobQueue( 500 );

		// Add two jobs - one for the initial fan, and a second for the sync job
		final int numJobsInOnePass = renderingPlan.getTotalNumJobs();

		int numDone = 0;
		final DumpAddNewTask addNewTask = new DumpAddNewTask( jobQueue );
		final InitialTask initialTask = new InitialTask( addNewTask, initialJobsArray, numJobsInOnePass );
		addNewTask.addNewTask( initialTask );
		try
		{
			// Get the initial task to run and add the first jobs to
			// our job queue.
			Runnable job = jobQueue.take();
			job.run();

			while( numDone < numJobsInOnePass )
			{
				job = jobQueue.take();
				job.run();
				numDone++;
			}
		}
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
		// Now run any remaining tasks for completeness
		for( final Runnable remainingJob : jobQueue )
		{
			remainingJob.run();
		}
	}
}
