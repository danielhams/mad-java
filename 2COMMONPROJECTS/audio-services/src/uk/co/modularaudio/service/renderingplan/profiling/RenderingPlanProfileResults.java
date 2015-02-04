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

package uk.co.modularaudio.service.renderingplan.profiling;

import java.util.HashMap;

import uk.co.modularaudio.service.renderingplan.AbstractParallelRenderingJob;

public class RenderingPlanProfileResults
{
	private AbstractParallelRenderingJob[] jobsToProfile;
	private int numJobs;

	private HashMap<AbstractParallelRenderingJob,JobProfileResult> jobToProfileResultMap;
	private long clockCallbackStart;
	private long clockCallbackPostProducer;
	private long clockCallbackPostRpFetch;
	private long clockCallbackPostLoop;

//	private AtomicBoolean filled = new AtomicBoolean( false );
	private volatile boolean filled = false; // NOPMD by dan on 01/02/15 07:07

	public RenderingPlanProfileResults( final AbstractParallelRenderingJob[] jobsToProfile )
	{
		this.jobsToProfile = jobsToProfile;
		numJobs = jobsToProfile.length;

		jobToProfileResultMap = new HashMap<AbstractParallelRenderingJob, JobProfileResult>( numJobs );
		for( int i = 0 ; i < numJobs ; i++ )
		{
			jobToProfileResultMap.put( jobsToProfile[ i ], new JobProfileResult() );
		}
	}

	public void fillIn( final long clockCallbackStart,
			final long clockCallbackPostProducer,
			final long clockCallbackPostRpFetch,
			final long clockCallbackPostLoop )
	{
		this.clockCallbackStart = clockCallbackStart;
		this.clockCallbackPostProducer = clockCallbackPostProducer;
		this.clockCallbackPostRpFetch = clockCallbackPostRpFetch;
		this.clockCallbackPostLoop = clockCallbackPostLoop;
		for( final AbstractParallelRenderingJob job : jobToProfileResultMap.keySet() )
		{
			final JobProfileResult jobResult = jobToProfileResultMap.get( job );
			jobResult.pullResultsFromJob( job );
		}
//		filled.set( true );
		filled = true;
	}

	public boolean isFilled()
	{
//		return filled.get();
		return filled;
	}

	public void setFilled( final boolean targetFilled )
	{
//		filled.set( targetFilled );
		filled = targetFilled;
	}

	public HashMap<AbstractParallelRenderingJob, JobProfileResult> getJobToProfileResultMap()
	{
		return jobToProfileResultMap;
	}

	public long getClockCallbackStart()
	{
		return clockCallbackStart;
	}

	public long getClockCallbackPostProducer()
	{
		return clockCallbackPostProducer;
	}

	public long getClockCallbackPostRpFetch()
	{
		return clockCallbackPostRpFetch;
	}

	public long getClockCallbackPostLoop()
	{
		return clockCallbackPostLoop;
	}

	public static void copyFromTo( final RenderingPlanProfileResults from, final RenderingPlanProfileResults to )
	{
		to.clockCallbackStart = from.clockCallbackStart;
		to.clockCallbackPostProducer = from.clockCallbackPostProducer;
		to.clockCallbackPostRpFetch = from.clockCallbackPostRpFetch;
		to.clockCallbackPostLoop = from.clockCallbackPostLoop;
		to.jobsToProfile = from.jobsToProfile;
		to.numJobs = from.numJobs;
		to.jobToProfileResultMap = new HashMap<AbstractParallelRenderingJob, JobProfileResult>( from.jobToProfileResultMap );
		// Mark the original as able to be filled again
//		from.filled = new AtomicBoolean( false );
		from.filled = false;
	}
}
