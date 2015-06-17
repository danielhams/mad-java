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

package uk.co.modularaudio.service.renderingplan.impl;

import java.util.Set;

import uk.co.modularaudio.service.renderingplan.RenderingJob;
import uk.co.modularaudio.service.renderingplan.RenderingPlan;
import uk.co.modularaudio.service.renderingplan.profiling.RenderingPlanProfileResults;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;


public class RenderingPlanWithFanAndSync implements RenderingPlan
{
//	private static Log log = LogFactory.getLog( RenderingPlan.class.getName() );

	private final HardwareIOChannelSettings planChannelSettings;
	private final MadTimingParameters planTimingParameters;
	private final MadFrameTimeFactory planFrameTimeFactory;
	private final FinalSyncParallelRenderingJob finalSyncJob;
	private final RenderingJob[] allJobs;
	private final RenderingJob[] initialJobs;
	private final int totalNumJobs;
	private final Set<MadInstance<?,?>> allMadInstances;

	private final RenderingPlanProfileResults profileResults;

	public RenderingPlanWithFanAndSync( final HardwareIOChannelSettings planChannelSettings,
			final MadTimingParameters planTimingParameters,
			final MadFrameTimeFactory planFrameTimeFactory,
			final FinalSyncParallelRenderingJob finalSyncJob,
			final RenderingJob[] allJobs,
			final RenderingJob[] initialJobs,
			final int totalNumJobs,
			final Set<MadInstance<?,?>> allMadInstances,
			final MadChannelBuffer[] allChannelBuffers )
	{
		this.planChannelSettings = planChannelSettings;
		this.planTimingParameters = planTimingParameters;
		this.planFrameTimeFactory = planFrameTimeFactory;
		this.finalSyncJob = finalSyncJob;
		this.allJobs = allJobs;
		this.initialJobs = initialJobs;
		this.totalNumJobs = totalNumJobs;
		this.allMadInstances = allMadInstances;

		profileResults = new RenderingPlanProfileResults( allJobs );
	}

	@Override
	public HardwareIOChannelSettings getPlanChannelSettings()
	{
		return planChannelSettings;
	}

	@Override
	public MadTimingParameters getPlanTimingParameters()
	{
		return planTimingParameters;
	}

	@Override
	public MadFrameTimeFactory getPlanFrameTimeFactory()
	{
		return planFrameTimeFactory;
	}

	@Override
	public RenderingJob[] getInitialJobs()
	{
		return initialJobs;
	}

	@Override
	public int getTotalNumJobs()
	{
		return totalNumJobs;
	}

	@Override
	public Set<MadInstance<?,?>> getAllInstances()
	{
		return allMadInstances;
	}

	@Override
	public boolean getPlanUsed()
	{
		return finalSyncJob.wasExecutedAtLeastOnce();
	}

	@Override
	public RenderingJob[] getAllJobs()
	{
		return allJobs;
	}

	@Override
	public void fillProfilingIfNotFilled(
			final int numRenderingThreads,
			final long clockCallbackStart,
			final long clockCallbackPostProducer,
			final long clockCallbackPostRpFetch,
			final long clockCallbackPostLoop )
	{
		if( !profileResults.isFilled() )
		{
			profileResults.fillIn( numRenderingThreads, clockCallbackStart, clockCallbackPostProducer, clockCallbackPostRpFetch, clockCallbackPostLoop );
		}
	}

	@Override
	public boolean getProfileResultsIfFilled( final RenderingPlanProfileResults toFillIn )
	{
		if( profileResults.isFilled() )
		{
			RenderingPlanProfileResults.copyFromTo( profileResults, toFillIn );
			profileResults.setFilled( false );
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void resetPlanExecution()
	{
		finalSyncJob.resetJobExecution();
	}

	@Override
	public boolean wasPlanExecuted()
	{
		return finalSyncJob.wasJobExecuted();
	}
}
